package com.geeknewbee.doraemon.database.upgrade;

import android.database.sqlite.SQLiteDatabase;


public class MigrateV1ToV2 extends MigrationImpl {

    @Override
    public int applyMigration(SQLiteDatabase db,
                              int currentVersion) {
        prepareMigration(db, currentVersion);
//        db.execSQL("ALTER TABLE DELIVERY_ORDER ADD COLUMN LAST_REMINDER_TIME TEXT");
        //TODO v1 dao v2 变化
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