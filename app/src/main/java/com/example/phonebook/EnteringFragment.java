package com.example.phonebook;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;

public class EnteringFragment extends Fragment {
    MyDatabase myDB = new MyDatabase(getActivity());
    private EditText enteredNumberView;
    private Button button0, button1, button2,
            button3, button4, button5, button6, button7,
            button8, button9, buttonContact;

    public EnteringFragment() {

    }
    public static EnteringFragment newInstance(String param1, String param2) {
        EnteringFragment fragment = new EnteringFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_entering, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        myDB = new MyDatabase(getActivity());

        enteredNumberView = view.findViewById(R.id.enter_number);
        button0 = view.findViewById(R.id.button0);
        button1 = view.findViewById(R.id.button1);
        button2 = view.findViewById(R.id.button2);
        button3 = view.findViewById(R.id.button3);
        button4 = view.findViewById(R.id.button4);
        button5 = view.findViewById(R.id.button5);
        button6 = view.findViewById(R.id.button6);
        button7 = view.findViewById(R.id.button7);
        button8 = view.findViewById(R.id.button8);
        button9 = view.findViewById(R.id.button9);
        buttonContact = view.findViewById(R.id.buttonContact);

        View.OnClickListener numberClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Button button = (Button) view;
                appendNumber(button.getText().toString());
            }
        };

        button0.setOnClickListener(numberClickListener);
        button1.setOnClickListener(numberClickListener);
        button2.setOnClickListener(numberClickListener);
        button3.setOnClickListener(numberClickListener);
        button4.setOnClickListener(numberClickListener);
        button5.setOnClickListener(numberClickListener);
        button6.setOnClickListener(numberClickListener);
        button7.setOnClickListener(numberClickListener);
        button8.setOnClickListener(numberClickListener);
        button9.setOnClickListener(numberClickListener);

        buttonContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addContactsMenu("Добавить контакт", myDB);
            }
        });

        enteredNumberView.addTextChangedListener(new TextWatcher(){
            @Override
            public void afterTextChanged(Editable s) {
                int contact = myDB.checkEnteredNumber(s.toString());
                if (contact >= 0) {
                    String fullName = myDB.getFullName(contact);
                    buttonContact.setText(fullName);
                    buttonContact.setClickable(false);
                } else {
                    buttonContact.setText("Создать контакт");
                    buttonContact.setClickable(true);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
    }

    private void appendNumber(String number) {
        String temp = enteredNumberView.getText() != null ? enteredNumberView.getText().toString() : "";

        if (temp.isEmpty() && number.equals("0")) {
            return;
        }
        temp += number;
        enteredNumberView.setText(temp);
    }

    public void addContactsMenu(String text, MyDatabase dbHelper) {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_contact, null);

        EditText firstNameInput = dialogView.findViewById(R.id.first_name_input);
        EditText lastNameInput = dialogView.findViewById(R.id.last_name_input);
        EditText phoneNumberInput = dialogView.findViewById(R.id.phone_number_input);
        LinearLayout phoneContainer = dialogView.findViewById(R.id.phone_container);
        Button addPhoneButton = dialogView.findViewById(R.id.add_phone_button);

        if(enteredNumberView.getText() != null){
            phoneNumberInput.setText(enteredNumberView.getText());
        }

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
        builder.setTitle("Новый контакт");
        builder.setMessage(text);
        builder.setView(dialogView);
        builder.setCancelable(true);

        builder.setPositiveButton("Сохранить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String firstName = firstNameInput.getText().toString().trim();
                String lastName = lastNameInput.getText().toString().trim();
                String primaryPhoneNumber = phoneNumberInput.getText().toString().trim();

                int contact = myDB.checkEnteredNumber(primaryPhoneNumber);
                if (contact >= 0) {
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
                            if (contact >= 0) {
                                Toast.makeText(getActivity(), "Один из номеров уже существует", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                    }
                }
                if (!firstName.isEmpty() && !primaryPhoneNumber.isEmpty()) {
                    dbHelper.AddContact(firstName, lastName, phoneNumbers);
                    Toast.makeText(getActivity(), "Контакт успешно сохранен", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Заполните обязательные поля", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Отмена", null);
        builder.create().show();
    }
}
