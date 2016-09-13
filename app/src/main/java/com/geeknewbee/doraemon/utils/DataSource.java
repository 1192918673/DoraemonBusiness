package com.geeknewbee.doraemon.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.geeknewbee.doraemon.entity.bean.User;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by mac on 16/7/11.
 */
public class DataSource {
    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private String[] allColumns = {
            MySQLiteHelper.USER_ID,
            MySQLiteHelper.PERSON_ID,
            MySQLiteHelper.USER_NAME,
            MySQLiteHelper.USER_AGE,
            MySQLiteHelper.USER_GENDER
    };

    public DataSource(Context context) {
        dbHelper = new MySQLiteHelper(context);
    }

    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }


    public void insert(User user) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.PERSON_ID, user.getPersonId());
        values.put(MySQLiteHelper.USER_NAME, user.getName());
        values.put(MySQLiteHelper.USER_AGE, user.getAge());
        values.put(MySQLiteHelper.USER_GENDER, user.getGender());
        database.insert(MySQLiteHelper.TABLE_USER, null, values);
    }


    public List<User> getAllUser() {
        List<User> result = new ArrayList<>();
        Cursor cursor = database.query(MySQLiteHelper.TABLE_USER,
                allColumns, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            User user = new User();
            user.setUser_id(cursor.getString(0));
            user.setPersonId(cursor.getString(1));
            user.setName(cursor.getString(2));
            user.setAge(cursor.getString(3));
            user.setGender(cursor.getString(4));
            result.add(user);
            cursor.moveToNext();
        }
        cursor.close();
        return result;
    }

    public User getUserByPersonId(String person_id) {

        User user = new User();
        Cursor cursor = database.query(MySQLiteHelper.TABLE_USER,
                null,
                "person_id = ?",
                new String[]{person_id},
                null, null, null);
        if (cursor.getCount() == 1) {
            cursor.moveToFirst();
            user.setUser_id(cursor.getString(0));
            user.setPersonId(cursor.getString(1));
            user.setName(cursor.getString(2));
            user.setAge(cursor.getString(3));
            user.setGender(cursor.getString(4));
        }
        return user;
    }

    public void clearTable() {
        //执行SQL语句
        database.delete(MySQLiteHelper.TABLE_USER, "_id>?", new String[]{"-1"});
//        database.execSQL("delete from stu_table where _id  >= 0");
    }
}
