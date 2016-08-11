package com.geeknewbee.doraemon;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.multidex.MultiDex;

import com.facebook.stetho.Stetho;
import com.geeknewbee.doraemon.database.DaoMaster;
import com.geeknewbee.doraemon.database.DaoSession;
import com.geeknewbee.doraemon.database.upgrade.MyOpenHelper;
import com.geeknewbee.doraemonsdk.BaseApplication;
import com.geeknewbee.doraemonsdk.input.AISpeechAuth;
import com.geeknewbee.doraemonsdk.utils.LogUtils;


public class App extends BaseApplication {

    private static final String TAG = App.class.getSimpleName();
    private DaoSession daoSession;
    public static App instance;

    @Override
    public void onCreate() {
        super.onCreate();
        init();
        instance = this;
        setupDatabase();
        LogUtils.LOG_DEBUG = BuildConfig.NEED_DEBUG;

        //方便调试
        if (BuildConfig.NEED_DEBUG)
            Stetho.initialize(Stetho.newInitializerBuilder(this).
                    enableDumpapp(Stetho.defaultDumperPluginsProvider(this)).
                    enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this)).build());
    }

    @Override
    protected void init() {
        boolean result = new AISpeechAuth().auth();
        LogUtils.d(TAG, "AISpeech auth result:" + result);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public DaoSession getDaoSession() {
        return daoSession;
    }

    private void setupDatabase() {
        MyOpenHelper helper = new MyOpenHelper(this, "DORAEMON_KERNEL_DB", null);
        SQLiteDatabase db = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();
    }
}
