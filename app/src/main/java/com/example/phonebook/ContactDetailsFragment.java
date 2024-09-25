package com.example.phonebook;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;

public class ContactDetailsFragment extends Fragment {

    private static final String ARG_ID = "contact_id";
    MyDatabase myDB;
    private int contactId;
    private ArrayList<String> phoneNumbers;


    public static ContactDetailsFragment newInstance(int id) {
        ContactDetailsFragment fragment = new ContactDetailsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ID, id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            contactId = getArguments().getInt(ARG_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact_details, container, false);

        TextView nameTextView = view.findViewById(R.id.contact_name);
        TextView lastnameTextView = view.findViewById(R.id.contact_lastname);
        Button backButton = view.findViewById(R.id.button_back);
        Button editButton = view.findViewById(R.id.button_edit);
        Button deleteButton = view.findViewById(R.id.button_delete);
        LinearLayout detailsList = view.findViewById(R.id.details_layout);

        myDB = new MyDatabase(getActivity());
        phoneNumbers = new ArrayList<>();

        Cursor cursor = myDB.getContact(contactId);
        if (cursor != null && cursor.moveToFirst()) {
            nameTextView.setText("Имя: " + cursor.getString(0));
            lastnameTextView.setText("Фамилия: " + cursor.getString(1));
            cursor.close();
        }

        Cursor numbersCursor = myDB.getAllNumbers(contactId);
        if (numbersCursor != null) {
            while (numbersCursor.moveToNext()) {
                String phoneNumber = numbersCursor.getString(0);
                phoneNumbers.add(phoneNumber);
            }
            numbersCursor.close();
        } else {
            Toast.makeText(getActivity(), "Нет номеров телефонов", Toast.LENGTH_SHORT).show();
        }

        for (int i = 0; i < phoneNumbers.size(); i++) {
            TextView newPhoneOutput = new TextView(getActivity());
            newPhoneOutput.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            newPhoneOutput.setText(phoneNumbers.get(i));
            newPhoneOutput.setTextSize(24);
            detailsList.addView(newPhoneOutput);
        }

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity activity = (MainActivity) getActivity();
                activity.replaceFragment(new ContactsFragment());
            }
        });
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editContactMenu(contactId, myDB);
            }
        });
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDB.deleteContact(contactId);
                getParentFragmentManager().popBackStack();
                Toast.makeText(getActivity(), "Контакт успешно удалён", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    public void editContactMenu(int id, MyDatabase dbHelper) {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_contact, null);
        EditText FirstNameEdit = dialogView.findViewById(R.id.first_name_input);
        EditText LastNameEdit = dialogView.findViewById(R.id.last_name_input);
        EditText primaryPhoneNumberEdit = dialogView.findViewById(R.id.phone_number_input);  // Основной номер
        Button addPhoneButton = dialogView.findViewById(R.id.add_phone_button);
        LinearLayout phoneContainer = dialogView.findViewById(R.id.phone_container);

        Cursor contact = dbHelper.getContact(id);
        Cursor numbersCursor = dbHelper.getAllNumbers(id);

        addPhoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText newPhoneInput = new EditText(getActivity());
                newPhoneInput.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                ));
                newPhoneInput.setHint("Дополнительный телефон");
                newPhoneInput.setInputType(android.text.InputType.TYPE_CLASS_PHONE);

                phoneContainer.addView(newPhoneInput);
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Редактирование контакта");
        builder.setView(dialogView);
        builder.setCancelable(true);

        if(contact.moveToFirst()){
            FirstNameEdit.setText(contact.getString(0));
            LastNameEdit.setText(contact.getString(1));
        }

        if (numbersCursor != null && numbersCursor.moveToFirst()) {
            primaryPhoneNumberEdit.setText(numbersCursor.getString(0));
            while (numbersCursor.moveToNext()) {
                EditText additionalPhoneInput = new EditText(getActivity());
                additionalPhoneInput.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                ));
                additionalPhoneInput.setText(numbersCursor.getString(0));
                additionalPhoneInput.setInputType(android.text.InputType.TYPE_CLASS_PHONE);

                phoneContainer.addView(additionalPhoneInput);
            }
            numbersCursor.close();
        }
        builder.setPositiveButton("Сохранить", new DialogInterface.OnClickListener() {
            @SuppressLint("DetachAndAttachSameFragment")
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String firstName = FirstNameEdit.getText().toString().trim();
                String lastName = LastNameEdit.getText().toString().trim();
                String primaryPhoneNumber = primaryPhoneNumberEdit.getText().toString().trim();

                int contact = myDB.checkEnteredNumber(primaryPhoneNumber);

                if (contact >= 0 && contact != contactId) {
                    Toast.makeText(getActivity(), "Основной номер  уже существует", Toast.LENGTH_SHORT).show();
                    return;
                }

                ArrayList<String> phoneNumbers = new ArrayList<>();
                phoneNumbers.add(primaryPhoneNumber);

                for (int i = 0; i < phoneContainer.getChildCount(); i++) {
                    View child = phoneContainer.getChildAt(i);
                    if (child instanceof EditText) {
                        String additionalPhone = ((EditText) child).getText().toString();
                        if (!additionalPhone.isEmpty()) {
                            phoneNumbers.add(additionalPhone);
                            contact = myDB.checkEnteredNumber(additionalPhone);
                            if (contact >= 0 && contact != contactId) {
                                Toast.makeText(getActivity(), "Один из номеров уже существует", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                    }
                }
                if (!firstName.isEmpty() && !primaryPhoneNumber.isEmpty()) {
                    dbHelper.editContact(contactId, firstName, lastName, phoneNumbers);
                    Toast.makeText(getActivity(), "Изменения успешно сохранены", Toast.LENGTH_SHORT).show();
                    ContactDetailsFragment detailsFragment = ContactDetailsFragment.newInstance(contactId);

                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.frame_layout, detailsFragment)
                            .addToBackStack(null)
                            .commit();
                } else {
                    Toast.makeText(getActivity(), "Заполните обязательные поля", Toast.LENGTH_SHORT).show();
                }

            }
        });

        builder.setNegativeButton("Отмена", null);
        builder.create().show();
    }
}
