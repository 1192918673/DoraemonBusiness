package com.geeknewbee.doraemon.database.upgrade;

import android.database.sqlite.SQLiteDatabase;

import com.geeknewbee.doraemon.database.PersonDao;


public class MigrateV1ToV2 extends MigrationImpl {

    @Override
    public int applyMigration(SQLiteDatabase db,
                              int currentVersion) {
        super.prepareMigration(db, currentVersion);
//        db.execSQL("ALTER TABLE DELIVERY_ORDER ADD COLUMN LAST_REMINDER_TIME TEXT");
        PersonDao.createTable(db, true);
        return getMigratedVersion();
    }

    @Override
    public int getTargetVersion() {
        return 1;
    }

    @Override
    public int getMigratedVersion() {
        return 2;
    }

    @Override
    public Migration getPreviousMigration() {
        return null;
    }
}