package com.geeknewbee.doraemon.database.upgrade;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.geeknewbee.doraemon.database.DaoMaster;


public class MyOpenHelper extends DaoMaster.OpenHelper {
    public MyOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch (newVersion) {
            case 2:
                new MigrateV1ToV2().applyMigration(db, oldVersion);
                break;
            case 3:
                new MigrateV2ToV3().applyMigration(db, oldVersion);
                break;
            default:
                break;
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        DaoMaster.dropAllTables(db, false);
        DaoMaster.createAllTables(db, false);
    }
}
