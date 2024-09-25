package com.example.phonebook;

import android.app.AlertDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ContactsFragment extends Fragment {

    RecyclerView listContacts;
    ArrayList<String> name, numbers_count;
    ArrayList<Integer> contacts_ids;
    MyDatabase myDB;
    CustomAdapter customAdapter;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    public ContactsFragment() {

    }

    public static ContactsFragment newInstance(String param1, String param2) {
        ContactsFragment fragment = new ContactsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("ContactsFragment", "onCreateView called");
        return inflater.inflate(R.layout.fragment_contacts, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("ContactsFragment", "onViewCreated called");

        myDB = new MyDatabase(getActivity());

        listContacts = view.findViewById(R.id.listContacts);
        listContacts.setLayoutManager(new LinearLayoutManager(getActivity()));

        name = new ArrayList<>();
        numbers_count = new ArrayList<>();
        contacts_ids = new ArrayList<>();
        storeDataInArrays();

        customAdapter = new CustomAdapter(getActivity(), contacts_ids, name, numbers_count, new CustomAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                ContactDetailsFragment detailsFragment = ContactDetailsFragment.newInstance(contacts_ids.get(position));

                getParentFragmentManager().beginTransaction()
                        .replace(R.id.frame_layout, detailsFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        listContacts.setAdapter(customAdapter);
    }

    void storeDataInArrays() {
        Cursor cursor = myDB.readAllData();
        if (cursor.getCount() == 0) {
            Toast.makeText(getActivity(), "Список контактов пуст", Toast.LENGTH_SHORT).show();
        } else {
            while (cursor.moveToNext()) {
                name.add(cursor.getString(0));
                numbers_count.add(cursor.getString(1));
                contacts_ids.add(cursor.getInt(2));
            }
        }
        cursor.close();
    }
}
