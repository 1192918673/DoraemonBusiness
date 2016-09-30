package com.geeknewbee.doraemon.constants;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Constants {
    public static final String BLUETOOTH_NAME = "DoraemonTestLX";     //DoraemonTest
    public static final int RESPONSE_STATUS_SUCCESS = 200;
    public static final int HTTP_TIME_OUT = 30 * 1000;
    public static final String TAG_COMMAND = "ROBOT_COMMAND";
    public static final String TAG_SOUND = "ROBOT_SOUND";
    public static final String TAG_MUSIC = "ROBOT_MUSIC";
    public static final String EMPTY_STRING = "";
    public static final String READSENSE_BROADCAST_TIPS_ACTION = "com.geeknewbee.doraemon.READ_SENSE_TIPS";
    public static final String READSENSE_BROADCAST_TAKE_PICTURE_ACTION = "com.geeknewbee.doraemon.READ_SENSE_TAKE_PICTURE";

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final int MESSAGE_READ_SOUND = 6;
    public static final int MESSAGE_READ_COMMAND = 7;
    public static final int MESSAGE_ANDROID_CONTROL = 8;
    public static final int MESSAGE_BLE_TTS = 9;
    public static final int MESSAGE_BLE_CONTROL = 10;

    //Http
    public static final String API_SECRET = "l5+z87&&szj_^$$7";

    public static final String HTTP_TAG = "http";
    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    public static final String DEFAULT_GIF = "default_gif";
    public static final String LISTENNING_GIF = "eyegif_fa_dai";

    public static final String STOP_FLAG = "停";
    //    public static final String COMMAND_ROBOT_PREFIX = "DRC";
//    public static final String COMMAND_ROBOT_SUFFIX = "DRC_SUFFIX";
    public static final String COMMAND_ROBOT_PREFIX = new String(new byte[]{0x02});
    public static final String COMMAND_ROBOT_SUFFIX = new String(new byte[]{0x03});
    public static final String COMMAND_ROBOT_PREFIX_FOR_SOCKET = "ROBOT_PREFIX";
    public static final String COMMAND_ROBOT_SUFFIX_FOR_SOCKET = "ROBOT_SUFFIX";
    public static final String MOJI_WEATHER_API_TOKEN = "c4adee396b1260222696";
    //墨迹天气
    public static final String MOJI_WEATHER_API_PWD = "mojisgbb20151207";
    //SharedPreferences
    public static final String KEY_TOKEN = "key_token";
    public static final String KEY_HX_USERNAME = "key_hx_username";

    public static final String KEY_HX_USERPWD = "key_hx_user_pwd";

    //BL许可
    public static final String BROADLINK_USER_LICENSE = "aiotYtzPTYvo0c1rKoyS8zXkWr/yKN794" +
            "M4SVRZVeMLbA8E2qFczFkycKEfgNBzd+djigmirvq2LpcLbVA7rhydxZwqKchdcUQzPWM4OqJES0e3uls8=";

    public static final String BROADLINK_TYPE_LICENSE = "hJ2tiIpCJavZZMaZe+yWEYx7Fi3Euw07m4" +
            "+vxSi/3aGItD+NviHtOKoOgbm/PYkbN+i36skA/Emzn32DSPvDY4FDbPAGyN7IIh1xTZpgrUogDtG9m" +
            "XJsA1Htm+2xqw7CfaRZ2iZbtdGtM76e0yF4Rg==";


    // 有限歌曲随机库
    public static final Map<String, String> map1 = new HashMap<String, String>() {
        {
            put("starName", "张学友");
            put("musicName", "吻别");
        }
    };
    public static final Map<String, String> map2 = new HashMap<String, String>() {
        {
            put("starName", "凤凰传奇");
            put("musicName", "最炫民族风");
        }
    };
    public static final Map<String, String> map3 = new HashMap<String, String>() {
        {
            put("starName", "薛之谦");
            put("musicName", "演员");
        }
    };
    public static final Map<String, String> map4 = new HashMap<String, String>() {
        {
            put("starName", "陈奕迅");
            put("musicName", "");
        }
    };
    public static final Map<String, String> map5 = new HashMap<String, String>() {
        {
            put("starName", "韩磊");
            put("musicName", "花房姑娘");
        }
    };
    public static final Map<String, String> map6 = new HashMap<String, String>() {
        {
            put("starName", "金志文");
            put("musicName", "往事只能回味");
        }
    };
    public static final Map<String, String> map7 = new HashMap<String, String>() {
        {
            put("starName", "");
            put("musicName", "风中的承诺");
        }
    };
    public static final Map<String, String> map8 = new HashMap<String, String>() {
        {
            put("starName", "那英");
            put("musicName", "默");
        }
    };
    public static final Map<String, String> map9 = new HashMap<String, String>() {
        {
            put("starName", "汪峰");
            put("musicName", "花火");
        }
    };

    public static final Map<String, String> map10 = new HashMap<String, String>() {
        {
            put("starName", "张学友");
            put("musicName", "一千个伤心的理由");
        }
    };

    public static final List<Map<String, String>> musics = Arrays.asList(map1, map2, map3, map4, map5, map6, map7, map8, map9, map10);

    // 本地语义对应的回答
    public static final String SELF_INTRODUCTION = "我叫哆啦a梦，来自22世纪。如果你们以为我只有颜值那可就错啦，" +
            "我可不是那种肤浅的猫，我上知天文，下知地理，数学计算，语文国学这些都难不倒我。我还喜欢摄影，" +
            "别看我身材不好，我可是只能歌善舞的猫哦。";

    public static final String HELLO = "大家好";
    public static final String END = "谢谢大家，期待越来越多的朋友帮我变得更聪明";
    public static final String TANG_SHI = "《登鹳雀楼》白日依山尽，黄河入海流。欲穷千里目，更上一层楼。";
    public static final String TIPS_SET_WIFI = "正在设置wifi";
    public static final String TIPS_CONNECT_WIFI_FAIL = "连接wifi失败";
    public static final String TIP_BEFORE_PLAY_MOVIE = "现在开始播放电影，请打开后盖！";
    public static final String EXTRA_PERSON_ID = "Extra_Person_ID";
    public static final String ACTION_DORAEMON_DISCOVERY_PERSON = "Action_Doraemon_discovery_person";
    public static String READ_NEWS_TIPS = "小朋友声音读新闻";
    public static final String ACTION_DORAEMON_REINIT_FACE_TRACK = "action_doraemon_reinit_face_track";
}
