package com.geeknewbee.doraemon;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.support.multidex.MultiDex;

import com.aispeech.common.AIConstant;
import com.facebook.stetho.Stetho;
import com.geeknewbee.doraemon.BL.BLM;
import com.geeknewbee.doraemon.database.DaoMaster;
import com.geeknewbee.doraemon.database.DaoSession;
import com.geeknewbee.doraemon.database.upgrade.MyOpenHelper;
import com.geeknewbee.doraemon.input.AISpeechAuth;
import com.geeknewbee.doraemon.output.action.YouKuPlayerActivity;
import com.geeknewbee.doraemonsdk.BaseApplication;
import com.geeknewbee.doraemonsdk.utils.LogUtils;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMOptions;
import com.youku.player.YoukuPlayerBaseConfiguration;

import java.util.Iterator;
import java.util.List;


public class App extends BaseApplication {

    public static final String TAG = "Doraemon_App";
    public static App instance;
    private DaoSession daoSession;
    public static YoukuPlayerBaseConfiguration configuration;

    @Override
    public void onCreate() {
        super.onCreate();
        int pid = android.os.Process.myPid();
        String processAppName = getAppName(pid);
        // 如果app启用了远程的service，此application:onCreate会被调用多次
        // 默认的app会在以包名为默认的process name下运行，如果查到的process name不是app的process name就立即返回
        if (processAppName == null || !processAppName.equalsIgnoreCase(getPackageName())) {
            LogUtils.d(TAG, "enter the service process!");
            // 则此application::onCreate 是被service 调用的，直接返回
            //防止多次初始化
            return;
        }
        LogUtils.d(TAG, "enter the App process!");
        create();
    }

    private void create() {
        init();
        instance = this;
        setupDatabase();
        initHuanXinSDK();
        initBroadLink();

        LogUtils.LOG_DEBUG = BuildConfig.NEED_DEBUG;
//        Thread.setDefaultUncaughtExceptionHandler(new DefaultExceptionHandler(this));
        AIConstant.setUseSpi(true);
        //方便调试
        if (BuildConfig.NEED_DEBUG)
            Stetho.initialize(Stetho.newInitializerBuilder(this).
                    enableDumpapp(Stetho.defaultDumperPluginsProvider(this)).
                    enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this)).build());
    }


    /**
     * 初始化博联设备
     */
    private void initBroadLink() {
        BLM.initBroadLink(this);
    }

    /**
     * 环信初始化
     */
    private void initHuanXinSDK() {
        EMOptions options = new EMOptions();
        // 默认添加好友时，是不需要验证的，改成需要验证
        options.setAcceptInvitationAlways(true);
        options.setAutoLogin(true);
        //初始化
        EMClient.getInstance().init(getApplicationContext(), options);
        //在做打包混淆时，关闭debug模式，避免消耗不必要的资源
        EMClient.getInstance().setDebugMode(false);
    }

    @Override
    protected void init() {
        boolean result = new AISpeechAuth().auth();
        LogUtils.d(TAG, "AISpeech auth result:" + result);
        configuration = new YoukuPlayerBaseConfiguration(this) {
            /**
             * 通过覆写该方法，返回“正在缓存视频信息的界面”，
             * 则在状态栏点击下载信息时可以自动跳转到所设定的界面.
             * 用户需要定义自己的缓存界面
             */
            @Override
            public Class<? extends Activity> getCachingActivityClass() {
                return YouKuPlayerActivity.class;
            }

            /**
             * 通过覆写该方法，返回“已经缓存视频信息的界面”，
             * 则在状态栏点击下载信息时可以自动跳转到所设定的界面.
             * 用户需要定义自己的已缓存界面
             */

            @Override
            public Class<? extends Activity> getCachedActivityClass() {
                return YouKuPlayerActivity.class;
            }

            /**
             * 配置视频的缓存路径，格式举例： /appname/videocache/
             * 如果返回空，则视频默认缓存路径为： /应用程序包名/videocache/
             */
            @Override
            public String configDownloadPath() {
                return null;
            }
        };
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

    /**
     * 环信获取processAppName
     *
     * @param pID
     * @return
     */
    private String getAppName(int pID) {
        String processName = null;
        ActivityManager am = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        List l = am.getRunningAppProcesses();
        Iterator i = l.iterator();
        PackageManager pm = this.getPackageManager();
        while (i.hasNext()) {
            ActivityManager.RunningAppProcessInfo info = (ActivityManager.RunningAppProcessInfo) (i.next());
            try {
                if (info.pid == pID) {
                    processName = info.processName;
                    return processName;
                }
            } catch (Exception e) {
                // Log.d("Process", "Error>> :"+ e.toString());
            }
        }
        return processName;
    }
}
