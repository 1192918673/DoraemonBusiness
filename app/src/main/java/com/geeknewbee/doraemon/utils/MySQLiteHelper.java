package com.geeknewbee.doraemon.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by mac on 16/7/11.
 */
public class MySQLiteHelper extends SQLiteOpenHelper {

    public static final String TABLE_USER = "users";
    public static final String USER_ID = "_id";
    public static final String PERSON_ID = "person_id";
    public static final String USER_NAME = "name";
    public static final String USER_AGE = "age";
    public static final String USER_GENDER = "gender";

    private static final String DATABASE_NAME = "user.db";
    private static final int DATABASE_VERSION = 1;

    //Database creation SQL statement.
    private static final String DATABASE_CREATE = "create table " + TABLE_USER +
            "(" + USER_ID + " integer primary key autoincrement,"
            + PERSON_ID + " text not null,"
            + USER_NAME + " text not null," + USER_AGE
            + " text not null," + USER_GENDER + " text not null);";

    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(MySQLiteHelper.class.getName(), "Upgrading database from version" +
                oldVersion + "to" + newVersion + ",which will destroy all the old data");
        db.execSQL("DROP TABLE IF EXISTS" + TABLE_USER);
        onCreate(db);
    }
}
