package com.example.phonebook;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.List;

public class MyDatabase extends SQLiteOpenHelper {

    private Context context;
    private static final String DATABASE_NAME = "Contacts";
    private static final int DATABASE_VERSION = 3;
    private static final String TABLE1_NAME = "person";
    private static final String COLUMN_ID1 = "_id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_LASTNAME = "lastname";

    private static final String TABLE2_NAME = "number";
    private static final String COLUMN_ID2 = "_id";
    private static final String COLUMN_PERSON_ID = "p_id";
    private static final String COLUMN_PHONE_NUMBER = "phone";

    public MyDatabase(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("PRAGMA foreign_keys=ON;");

        String query = "CREATE TABLE " + TABLE1_NAME +
                "(" + COLUMN_ID1 + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT NOT NULL, " +
                COLUMN_LASTNAME + " TEXT);";
        db.execSQL(query);

        query = "CREATE TABLE " + TABLE2_NAME +
                "(" + COLUMN_ID2 + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_PHONE_NUMBER + " TEXT NOT NULL, " +
                COLUMN_PERSON_ID + " INT, " +
                "CHECK(" + COLUMN_PHONE_NUMBER + " GLOB '[0-9]*'), " +
                "FOREIGN KEY(" + COLUMN_PERSON_ID + ") REFERENCES " + TABLE1_NAME + "(" + COLUMN_ID1 + ") ON DELETE CASCADE" +
                ");";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE1_NAME + ";");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE2_NAME + ";");
        onCreate(db);
    }

    public void AddContact(String name, String lastname, List<String> phoneNumbers) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues cv1 = new ContentValues();
            ContentValues cv2 = new ContentValues();

            cv1.put(COLUMN_NAME, name);
            cv1.put(COLUMN_LASTNAME, lastname);
            long result1 = db.insert(TABLE1_NAME, null, cv1);

            if (result1 == -1) {
                Toast.makeText(context, "Ошибка создания контакта", Toast.LENGTH_SHORT).show();
            } else {
                for(int i = 0; i < phoneNumbers.size(); i++){
                    String phone = phoneNumbers.get(i);
                    cv2.put(COLUMN_PHONE_NUMBER, phone);
                    cv2.put(COLUMN_PERSON_ID, result1);
                    long result2 = db.insert(TABLE2_NAME, null, cv2);

                    if (result2 == -1) {
                        Toast.makeText(context, "Ошибка добавления номера телефона", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Контакт успешно создан", Toast.LENGTH_SHORT).show();
                        db.setTransactionSuccessful();
                    }
                }
            }
        } catch (Exception e) {
            Toast.makeText(context, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            db.endTransaction();
        }
    }

    public int checkEnteredNumber(String number) {
        String query = "SELECT p." + COLUMN_ID1 +
                " FROM " + TABLE1_NAME + " p " +
                " JOIN " + TABLE2_NAME + " n ON p._id = n.p_id " +
                " WHERE n." + COLUMN_PHONE_NUMBER + " = ?";

        SQLiteDatabase db = this.getReadableDatabase();
        int id = -1;

        if (db != null) {
            Cursor cursor = db.rawQuery(query, new String[]{number});
            if (cursor != null && cursor.moveToFirst()) {
                id = cursor.getInt(0);
                cursor.close();
            }
        }
        return id;
    }


    public String getFullName(int id) {
        String query = "SELECT p.name || ' ' || p.lastname AS full_name " +
                "FROM " + TABLE1_NAME + " p " +
                "WHERE " + COLUMN_ID1 + " = ?";

        SQLiteDatabase db = this.getReadableDatabase();
        String fullName = "";

        if (db != null) {
            Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(id)});

            if (cursor != null && cursor.moveToFirst()) {
                fullName = cursor.getString(cursor.getColumnIndexOrThrow("full_name"));
                cursor.close();
            }
        }

        return fullName;
    }

    public void deleteContact(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE1_NAME, COLUMN_ID1 + " = ?", new String[]{String.valueOf(id)});
    }

    Cursor readAllData(){
        String query = "SELECT p.name || ' ' || p.lastname AS full_name, COUNT(n.phone) AS phone_count, p._id " +
                "FROM " + TABLE1_NAME + " as p " +
                "JOIN " + TABLE2_NAME + " as n ON p._id = n.p_id " +
                "GROUP BY p._id";
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = null;
        if (db != null){
            cursor = db.rawQuery(query, null);
        }
        return cursor;
    }

    Cursor getContact(int id) {
        String query = "SELECT p.name, p.lastname, n.phone " +
                "FROM " + TABLE1_NAME + " as p " +
                "JOIN " + TABLE2_NAME + " as n ON p._id = n.p_id " +
                "WHERE p._id = ?";
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = null;
        if (db != null) {
            cursor = db.rawQuery(query, new String[]{String.valueOf(id)});
        }
        return cursor;
    }
    Cursor getAllNumbers(int id) {
        String query = "SELECT " + COLUMN_PHONE_NUMBER +
                " FROM " + TABLE2_NAME +
                " WHERE p_id = ?";
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = null;
        if (db != null) {
            cursor = db.rawQuery(query, new String[]{String.valueOf(id)});
        }
        return cursor;
    }

    public void editContact(int contactId, String name, String lastname, List<String> phoneNumbers) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.beginTransaction();

        try {
            String updateContactQuery = "UPDATE " + TABLE1_NAME +
                    " SET " + COLUMN_NAME + " = ?, " + COLUMN_LASTNAME + " = ? " +
                    " WHERE " + COLUMN_ID1 + " = ?";
            db.execSQL(updateContactQuery, new Object[]{name, lastname, contactId});

            String deletePhonesQuery = "DELETE FROM " + TABLE2_NAME + " WHERE " + COLUMN_PERSON_ID + " = ?";
            db.execSQL(deletePhonesQuery, new Object[]{contactId});

            String insertPhoneQuery = "INSERT INTO " + TABLE2_NAME + " (" + COLUMN_PHONE_NUMBER + ", " + COLUMN_PERSON_ID + ") VALUES (?, ?)";
            for (String phone : phoneNumbers) {
                db.execSQL(insertPhoneQuery, new Object[]{phone, contactId});
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }
}
