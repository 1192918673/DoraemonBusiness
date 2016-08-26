package com.geeknewbee.doraemon.processcenter;

import com.geeknewbee.doraemon.R;
import com.geeknewbee.doraemon.processcenter.command.ActionSetCommand;
import com.geeknewbee.doraemon.processcenter.command.SportAction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 本地的运动集合，因为现在服务器是返回固定的几个动作，这里和服务做对应。
 * 以后可以让服务器直接发送约定好的命令，直接执行
 */
public class LocalSportActionManager extends Thread {
    public static final String XIAO_PING_GUO = "xiao_ping_guo";
    private static LocalSportActionManager instance;
    private Map<String, List<SportAction>> localActionMap;

    private LocalSportActionManager() {
        localActionMap = new HashMap<>();
        //初始化跳舞时对应的表情
        eyeMaps.put('h', "gan_ga");
        eyeMaps.put('p', "ke_lian_meng");
        eyeMaps.put('t', "se");
        eyeMaps.put('i', "yi_wen");
        eyeMaps.put('x', "yun");
        eyeMaps.put('y', "hai_xiu");
        eyeMaps.put('b', "ku");
    }

    public static LocalSportActionManager getInstance() {
        if (instance == null)
            instance = new LocalSportActionManager();

        return instance;
    }

    /**
     * 初始化本地动作库
     */
    public void initLocalAction() {
        if (!isAlive())
            start();
    }

    /**
     * 获取对应的动作集合命令
     *
     * @param actionNameList
     * @return
     */
    public ActionSetCommand getActionSetCommand(List<String> actionNameList) {
        if (actionNameList == null || actionNameList.isEmpty())
            return null;
        ActionSetCommand command = new ActionSetCommand();
        for (String action : actionNameList) {
            if (localActionMap.containsKey(action)) {
                command.addSportAction(localActionMap.get(action));
            }
        }
        return command;
    }

    @Override
    public void run() {
        super.run();
        List<SportAction> actions;
        actions = SportActionUtil.parseSportCommand(R.raw.action_head_up);
        localActionMap.put("head_up", actions);
        actions = SportActionUtil.parseSportCommand(R.raw.action_head_down);
        localActionMap.put("head_down", actions);
        actions = SportActionUtil.parseSportCommand(R.raw.action_head_front);
        localActionMap.put("head_front", actions);
        actions = SportActionUtil.parseSportCommand(R.raw.action_r_arm_down);
        localActionMap.put("r_arm_down", actions);
        actions = SportActionUtil.parseSportCommand(R.raw.action_r_arm_up);
        localActionMap.put("r_arm_up", actions);
        actions = SportActionUtil.parseSportCommand(R.raw.action_r_arm_end);
        localActionMap.put("r_arm_end", actions);

        actions = SportActionUtil.parseSportCommand(R.raw.action_r_arm_front);
        localActionMap.put("r_arm_front", actions);

        actions = SportActionUtil.parseSportCommand(R.raw.action_l_arm_down);
        localActionMap.put("l_arm_down", actions);

        actions = SportActionUtil.parseSportCommand(R.raw.action_l_arm_up);
        localActionMap.put("l_arm_up", actions);

        actions = SportActionUtil.parseSportCommand(R.raw.action_l_arm_end);
        localActionMap.put("l_arm_end", actions);

        actions = SportActionUtil.parseSportCommand(R.raw.action_l_arm_front);
        localActionMap.put("l_arm_front", actions);

        actions = SportActionUtil.parseSportCommand(R.raw.action_head_right);
        localActionMap.put("head_right", actions);

        actions = SportActionUtil.parseSportCommand(R.raw.action_head_left);
        localActionMap.put("head_left", actions);

        actions = SportActionUtil.parseSportCommand(R.raw.action_foot_right);
        localActionMap.put("right", actions);

        actions = SportActionUtil.parseSportCommand(R.raw.action_foot_left);
        localActionMap.put("left", actions);

        actions = SportActionUtil.parseSportCommand(R.raw.action_foot_backward);
        localActionMap.put("backward", actions);

        actions = SportActionUtil.parseSportCommand(R.raw.action_foot_forward);
        localActionMap.put("forward", actions);

        actions = getXiaoPingGuoDance();
        localActionMap.put(XIAO_PING_GUO, actions);
    }

    private List<SportAction> getXiaoPingGuoDance() {
        List<SportAction> actionList = new ArrayList<>();
        char[] script = null;
        SportAction action;
        for (char[] dance_script : dance_scripts) {
            action = new SportAction();
            script = dance_script;
            action.delayTime = script[7];
            action.topCommand = robot_base_action(script[0], script[1], script[2], script[3], script[4], script[5], script[6], script[8]);
            action.footCommand = SportActionUtil.getFootCommandOfLeXing(script[6], script[7]);
            if (script.length > 9) {
                if (eyeMaps.containsKey(script[9])) {
                    action.expressionName = eyeMaps.get(script[9]);
                }
            }
            actionList.add(action);
        }

        return actionList;
    }

    public static final char CONTROL_MOTOR_HEAD_LEFT_OR_RIGHT = 1;//控制舵机 头 左右
    public static final char CONTROL_MOTOR_HEAD_UP_OR_DOWN = 2;//控制舵机 头 上下
    public static final char CONTROL_MOTOR_LEFT_ARM_STRETCH = 3;//控制舵机 左手前后
    public static final char CONTROL_MOTOR_LEFT_ARM_TURN = 4;//控制舵机 左手上下
    public static final char CONTROL_MOTOR_RIGHT_ARM_STRETCH = 5;//控制舵机 右手 前后
    public static final char CONTROL_MOTOR_RIGHT_ARM_TURN = 6;//控制舵机 右手上下

    /**
     * 让机器人动作
     *
     * @param motor1Val 左手伸展度数
     * @param motor2Val 右手伸展度数
     * @param motor3Val 右手前后转动度数
     * @param motor4Val 左手前后转动度数
     * @param motor5Val 头旋转度数
     * @param motor6Val 头上下度数
     * @return 执行结果
     */
    public String robot_base_action(char motor1Val, char motor2Val, char motor3Val, char motor4Val, char motor5Val, char motor6Val, int walk_direction, int walk_speed) {
        char[] buf = {0x02, 0x06,
                CONTROL_MOTOR_RIGHT_ARM_TURN, (char) (compute_motor_val(2, motor2Val) == 0 ? 0 : 135 - compute_motor_val(2, motor2Val)),
                CONTROL_MOTOR_LEFT_ARM_TURN, (char) (compute_motor_val(1, motor1Val) == 0 ? 0 : 135 - compute_motor_val(1, motor1Val)),
                CONTROL_MOTOR_LEFT_ARM_STRETCH, (char) (compute_motor_val(4, motor4Val) == 0 ? 0 : 180 - compute_motor_val(4, motor4Val)),
                CONTROL_MOTOR_RIGHT_ARM_STRETCH, (char) (compute_motor_val(3, motor3Val) == 0 ? 0 : 180 - compute_motor_val(3, motor3Val)),
                CONTROL_MOTOR_HEAD_LEFT_OR_RIGHT, (char) (compute_motor_val(5, motor5Val) == 0 ? 0 : 131 - compute_motor_val(5, motor5Val)),
                CONTROL_MOTOR_HEAD_UP_OR_DOWN, (char) (compute_motor_val(6, motor6Val) == 0 ? 0 : -(128 - compute_motor_val(6, motor6Val))), 0x03, 0xe8, 0x00, 0x01};//步进电机控制

        return String.valueOf(buf);
    }

    /**
     * 计算角度对应的值
     *
     * @param idx      索引
     * @param motorVal 值
     * @return char
     */
    private int compute_motor_val(int idx, char motorVal) {
        int tIdx = (idx - 1) * 4;
        if (motorVal == 0) {
            return 0;//返回默认的位置 都是0度
        }
        float temp = (float) motorVal / MOTOR_INIT_VALS[tIdx + 3];
//        if(idx==0 || idx==1){// 左手和右手伸展的值是从大到小 0x87-0x5a
//            temp = MOTOR_INIT_VALS[tIdx + 1] - temp * (MOTOR_INIT_VALS[tIdx + 2] - MOTOR_INIT_VALS[tIdx + 1]);
//        }else {
        temp = MOTOR_INIT_VALS[tIdx + 1] + temp * (MOTOR_INIT_VALS[tIdx + 2] - MOTOR_INIT_VALS[tIdx + 1]);
//        }
//        LogUtils.d("=========计算角度对应的值:角度="+(int)motorVal+"，16进制值=" +Integer.toHexString(ret));
        return Math.round(temp);
    }

    public final char[] MOTOR_INIT_VALS = new char[]{
            0x87, 0x87, 0x5a, 90, //右手伸展 初始值，最小值，最大值，最大度数
            0x87, 0x87, 0x5a, 90, //左手伸展
            0xc9, 0x30, 0xc9, 180,//右手前后
            0xc9, 0x30, 0xc9, 180,//左手前后
            0x83, 0x2e, 0xcb, 120,//头部旋转
            0x80, 0x30, 0xd0, 24//头部上下
    };

    private final char[][] dance_scripts = new char[][]{
            //
//头先下15度，向上45度后向下45度，向上45度，再向下30度
            {0, 0, 0, 0, 0, 0, 0, 0, 1200, 'b'},
//胳膊向前方抬至90度后上下摆动两次，向前行2秒
            {0, 0, 90, 90, 0, 0, 0, 1250, 1200},
            {90, 0, 0, 0, 0, 0, 0, 1250, 1200},
//头向下15度，再向上45度后向下30度，胳膊抬至90度，90度上下摆动2次
            {0, 0, 0, 90, 0, 0, 3, 1000, 1200},
            {0, 0, 90, 0, 0, 0, 4, 1000, 1200},
            {0, 0, 0, 90, 0, 0, 0, 1250, 1200},
            {0, 0, 0, 0, 90, 0, 0, 1250, 1200},
//胳膊向前方抬至90度后上下摆动两次，向后行2秒
            {90, 90, 0, 0, 0, 0, 4, 1000, 1200},
            {0, 0, 180, 180, 0, 0, 3, 1000, 1200},
            //桃心眼,胳膊向外抬起上下摆动三次，身体转720度
            {0, 0, 0, 150, 150, 0, 3, 1000, 1200},
            {90, 90, 0, 0, 0, 0, 4, 1000, 1200},
            {0, 0, 90, 90, 0, 0, 1, 1720, 1000},
            {90, 90, 0, 0, 0, 0, 2, 1720, 1000},
            {0, 0, 180, 180, 0, 0, 0, 1000, 1200},
            {0, 0, 0, 0, 0, 0, 0, 720, 1200},
            // 1头向右转60度，右胳膊向外抬高60度，向前走1.5秒
            {0, 90, 0, 0, 90, 0, 3, 1538, 1000, 'p'},
            {0, 90, 0, 0, 90, 0, 4, 1538, 1000},
            {90, 0, 0, 0, 90, 0, 4, 1538, 1000},
            {90, 0, 0, 0, 30, 0, 3, 1538, 1000},
            {0, 90, 0, 0, 90, 0, 0, 1538, 1200},
            // 2头向右转60度，右胳膊向上抬高180度（星星3000）
            {0, 0, 0, 180, 30, 0, 0, 1915, 1200, 'x'},
            {0, 0, 180, 0, 90, 0, 0, 1915, 1200},
            //头向上抬30度，胳膊向前抬至150度（太阳，3000）
            {90, 0, 0, 0, 0, 30, 1, 1623, 1000, 't'},
            {0, 0, 180, 0, 0, 0, 2, 1623, 1000},
            {90, 90, 0, 0, 0, 0, 0, 1624, 1200},
            //3向前走1.5秒，胳膊向前方抬至90度，90度上下摆动2次
            {0, 0, 180, 0, 0, 0, 1, 1900, 1000, 'b'},
            {0, 0, 0, 180, 0, 0, 0, 1915, 1200},
            {0, 90, 0, 0, 0, 0, 2, 1900, 1000},
            {0, 0, 180, 180, 0, 0, 0, 1915, 1200},
            //4先前走1.5秒，右胳膊向前抬高，90度
            {0, 90, 0, 0, 0, 0, 3, 1000, 1200},
            {90, 0, 0, 0, 0, 0, 4, 1000, 1200},
            {0, 90, 0, 0, 0, 0, 0, 1000, 1200},
            {0, 0, 0, 0, 0, 0, 0, 820, 1200},

            //5双胳膊向前抬,120度，向后走1.5秒
            {90, 90, 0, 0, 120, 0, 0, 1870, 1200},
            {0, 0, 180, 180, 0, 0, 0, 1000, 1200},
            {0, 0, 0, 0, 0, 0, 0, 500, 1200},
            //6右胳膊向前抬起90度，向前走1.5秒（苹果3500）
            {0, 0, 150, 0, 0, 30, 1, 1885, 1000, 'p'},
            //胳膊恢复正常体位
            {90, 90, 0, 0, 0, 30, 2, 1885, 1000},
            //7脸变红色，头先向左转60度再向右120度，再向左60度。双胳膊向前抬起180度（心3000）
            {0, 0, 90, 0, 90, 0, 3, 1160, 1200, 'i'},
            {0, 0, 0, 90, 30, 0, 4, 1160, 1200},
            {0, 0, 90, 0, 90, 0, 0, 1160, 1200},
            //桃心眼,胳膊向外抬起上下摆动三次，身体转720度
            {90, 0, 0, 0, 0, 0, 1, 1000, 1000, 'b'},
            {0, 90, 0, 0, 0, 0, 2, 1000, 1000},
            {90, 0, 0, 0, 0, 0, 0, 1000, 1200},
            {0, 90, 0, 0, 0, 0, 0, 1000, 1200},
            //头向上抬30度，桃心眼，胳膊向前抬至150度（烟火，4000）
            {0, 0, 90, 90, 0, 0, 3, 500, 1200, 'y'},
            {0, 0, 120, 120, 0, 0, 4, 500, 1200},
            {0, 0, 150, 150, 0, 0, 0, 500, 1200},
            {0, 0, 120, 120, 0, 0, 0, 500, 1200},
            //低头30度，胳膊下降150度（头胳膊恢复正常体位）
            {0, 0, 150, 150, 0, 0, 0, 500, 1200},
            {0, 0, 120, 120, 0, 0, 0, 500, 1200},
            {0, 0, 0, 0, 0, 30, 0, 560, 1200},
            //右胳膊向前抬起90度，向前走1秒,，再向后走1秒（苹果3000）
            {90, 0, 0, 0, 1, 0, 1, 1283, 1000, 'p'},
            {90, 0, 0, 0, 0, 0, 2, 1283, 1000},
            {0, 0, 0, 0, 0, 0, 0, 1284, 1200},
            //脸变红色，头先向左转60度，再右转120度，再左转60度，双胳膊向前抬起150度
            {0, 0, 150, 150, 30, 0, 1, 1700, 1000, 'b'},
            {0, 0, 90, 90, 0, 0, 2, 1710, 1000},
            //桃心眼,胳膊向外抬起上下摆动三次，身体转720度（花4000）
            {0, 90, 0, 0, 0, 0, 1, 1000, 1000, 'h'},
            {90, 0, 0, 0, 0, 30, 0, 1000, 1200},
            {0, 90, 0, 0, 0, 1, 2, 1000, 1000},
            {0, 0, 0, 0, 0, 30, 0, 1070, 1200},
            //桃心眼，胳膊向前抬至150度（心2000）
            {90, 0, 0, 0, 0, 1, 0, 2000, 1200, 'i'},
            //头先下15度，再向上45度后向下45度，再向上15度
            {0, 90, 0, 0, 0, 30, 0, 1500, 1200},
            {90, 0, 0, 0, 0, 1, 1, 1500, 1000},
            //头先下15度，再向上45度后向下45度，再向上15度，胳膊向前方抬至90度，再90度上下摆动3次
            {0, 90, 0, 0, 0, 0, 0, 1500, 1200},
            {90, 0, 0, 0, 0, 30, 2, 1500, 1000},
//头先下15度，再向上45度后向下45度，再向上15度，胳膊向前方抬至90度，再90度上下摆动3次
            {0, 0, 90, 0, 0, 0, 1, 1000, 1000, 'b'},
            {0, 0, 0, 90, 0, 0, 0, 1000, 1200},
            {0, 0, 90, 0, 0, 30, 2, 1000, 1000},
            {0, 0, 0, 90, 0, 0, 3, 1000, 1200},
            {0, 0, 90, 0, 0, 5, 4, 1000, 1200},
            {0, 0, 0, 90, 0, 0, 0, 1000, 1200},
            {0, 0, 90, 0, 0, 30, 1, 1000, 1000},
            {0, 0, 0, 90, 0, 0, 2, 1000, 1000},
            //恢复正常体位
            {0, 0, 90, 0, 0, 0, 0, 1500, 1200},
            {0, 0, 0, 90, 0, 0, 3, 1000, 1200},
            {0, 0, 0, 0, 0, 0, 4, 1000, 1200},
            //脸变红色3秒，双胳膊向前抬起150度
            {0, 0, 180, 180, 0, 0, 0, 1810, 1200},
            //桃心眼，胳膊向前抬起转一圈，身体旋转360度（心1950）
            {90, 90, 0, 0, 0, 0, 0, 1250, 1200, 'i'},
            {0, 0, 0, 0, 0, 0, 0, 700, 1200},
            //头向上抬30度，胳膊向前抬至150度
            {0, 0, 180, 180, 0, 1, 0, 1500, 1200, 'b'},
            {90, 90, 0, 0, 0, 0, 0, 1500, 1200},
            {0, 0, 0, 0, 0, 0, 0, 880, 1200},
            // 头向右转60度，右胳膊向外抬高60度，向右转30度后向前走1秒
            {0, 90, 0, 0, 100, 0, 0, 1880, 1200, 't'},
            // 头向左转60度，左胳膊向外抬高60度，向左转30度，向后走1秒
            {0, 0, 180, 0, 0, 0, 0, 1000, 1200, 'b'},
            {0, 0, 0, 0, 0, 0, 0, 920, 1200},
            //头向上抬，胳膊向前抬至150度
            {0, 0, 120, 100, 0, 0, 0, 1570, 1200},
            {0, 0, 0, 0, 0, 0, 0, 1000, 1200},
            //先向下点头15度再向上15度，胳膊向前方抬至90度后放下
            {0, 0, 90, 90, 0, 0, 0, 1300, 1200},
            // 头向右转60度，右胳膊向外抬高60度，右转90度，后向前1秒（花3000）
            {90, 0, 0, 0, 0, 0, 1, 1000, 1000, 'h'},
            {0, 90, 0, 0, 0, 0, 2, 1000, 1000, 'h'},
            {0, 0, 0, 0, 1, 0, 0, 840, 1200, 'h'},
            // 头向左转60度，左胳膊向外抬高60度，向左转30度，向后走1秒（星星3000）
            {0, 0, 90, 0, 0, 0, 0, 1820, 1200, 'x'},
            {0, 0, 0, 90, 0, 0, 1, 1000, 1000},
            {0, 0, 0, 0, 0, 0, 2, 1000, 1000},
            // 头向右转60度，右胳膊向外抬高60度，右转90度，后向前1秒
            {0, 0, 90, 90, 0, 0, 0, 1850, 1500, 'b'},
            {90, 90, 0, 0, 0, 0, 1, 1000, 1000},
            {0, 0, 0, 0, 0, 0, 2, 1000, 1000},
            // 头向左转60度，左胳膊向外抬高60度，向左转30度，向后走1秒
            {90, 0, 0, 0, 5, 0, 0, 1500, 1200},
            {0, 90, 0, 0, 100, 0, 0, 1400, 1200},
            {0, 0, 0, 0, 100, 0, 0, 1000, 1200},
            //右胳膊向前抬起90度，向前走1秒（苹果3000）
            {0, 90, 0, 0, 0, 0, 1, 1835, 1000, 'p'},
            //向后走1秒
            {0, 90, 0, 0, 0, 0, 2, 1835, 1000},
            //脸变红色，头先向左转60度再向右120度，再向左60度。双胳膊向前抬起180度
            {0, 0, 180, 180, 5, 0, 0, 1745, 1200, 'i'},
            {0, 0, 180, 180, 100, 0, 0, 1745, 1200},
            //桃心眼,胳膊向外抬起上下摆动三次，身体转720度
            {90, 0, 90, 0, 0, 0, 3, 1000, 1200, 'b'},
            {0, 90, 0, 90, 0, 0, 4, 1000, 1200},
            {90, 0, 90, 0, 0, 30, 0, 1000, 1000},
            {0, 90, 0, 90, 0, 1, 0, 1050, 1000},
            //桃心眼，胳膊向前抬至150度（烟花4000）
            {0, 0, 150, 150, 0, 30, 1, 1000, 1000, 'y'},
            {0, 0, 120, 120, 0, 0, 2, 1000, 1000},
            //停1.5秒
            {0, 0, 150, 150, 0, 1, 3, 1050, 1200},
            {0, 0, 120, 120, 0, 0, 4, 1050, 1200},
            //右胳膊向前抬起90度，向前走1秒
            {0, 0, 90, 0, 120, 0, 0, 1790, 1200, 'p'},
            //向后走1秒
            {90, 90, 0, 0, 0, 0, 4, 1000, 1200},
            {0, 0, 90, 0, 0, 30, 3, 1000, 1200},
            //脸变红色，头先向左转60度，再右转120度，后左转60度，双胳膊向前抬起150度
            {0, 0, 150, 150, 5, 0, 1, 1000, 1000, 'b'},
            {90, 90, 0, 0, 100, 0, 2, 1000, 1000},
            {0, 0, 150, 150, 60, 0, 0, 1400, 1200},
            //桃心眼,胳膊向外抬起上下摆动三次，身体转720度（花4000）
            {0, 90, 0, 0, 0, 0, 4, 1000, 1200, 'h'},
            {90, 0, 0, 0, 0, 0, 3, 1000, 1200},
            {0, 90, 0, 0, 0, 0, 0, 1000, 1200},
            {0, 0, 180, 180, 0, 0, 0, 1080, 1200},
            //桃心眼，胳膊向前抬至150度
            {90, 0, 0, 0, 0, 0, 1, 1000, 1000, 'b'},
            {0, 90, 0, 0, 0, 0, 2, 1000, 1000, 'b'},
            //头先下15度，再向上45度后向下45度，再向上15度
            {90, 0, 0, 0, 30, 0, 0, 1000, 1200},
            {0, 90, 0, 0, 90, 0, 4, 1000, 1200},
            {90, 0, 0, 0, 30, 0, 3, 1000, 1200},
            //头先下15度，再向上45度后向下45度，再向上15度，胳膊向前方抬至90度，再90度上下摆动3次
            {0, 0, 90, 90, 5, 0, 1, 1500, 1000},
            {0, 0, 0, 0, 5, 0, 2, 1000, 1000},
            {90, 90, 0, 0, 5, 0, 0, 500, 1200},
//头先下15度，再向上45度后向下45度，再向上15度，胳膊向前方抬至90度，再90度上下摆动3次
            {0, 0, 90, 0, 90, 0, 1, 1000, 1000},
            {0, 0, 0, 90, 30, 0, 2, 1000, 1000},
            {0, 0, 90, 0, 90, 0, 3, 1000, 1200},
            {0, 0, 90, 0, 30, 0, 0, 980, 1200},
            {0, 0, 0, 90, 90, 0, 4, 1000, 1200},
            {0, 0, 90, 0, 30, 0, 0, 1000, 1200},
            {0, 0, 90, 0, 90, 0, 4, 1000, 1200},
            {0, 0, 0, 90, 30, 0, 3, 1000, 1200},
            {0, 0, 90, 0, 90, 0, 0, 1000, 1200},
            {0, 0, 0, 90, 30, 0, 1, 1000, 1000},
            {0, 0, 90, 0, 90, 0, 2, 1000, 1000},
            {0, 0, 0, 0, 0, 0, 0, 500, 1200},
            //右胳膊向前抬起90度，向前走1秒（苹果3000）
            {0, 0, 180, 0, 0, 0, 1, 1000, 1000, 'p'},
            //向后走1秒
            {0, 0, 180, 0, 0, 0, 2, 1000, 1000},
            //胳膊恢复正常体位
            {90, 90, 0, 0, 0, 0, 0, 1000, 1200},
            {0, 0, 0, 0, 0, 0, 0, 710, 1200},
            {0, 0, 0, 90, 0, 30, 0, 1400, 1200, 'i'},
            {0, 0, 90, 0, 0, 0, 3, 1000, 1200},
            {0, 0, 0, 90, 0, 30, 4, 1000, 1200},
            //桃心眼,胳膊向外抬起上下摆动三次，身体转720度（心6000）
            {0, 0, 90, 0, 0, 0, 4, 1000, 1200, 'b'},
            {0, 0, 0, 90, 0, 0, 3, 1000, 1200},
            {0, 0, 90, 0, 0, 0, 0, 1000, 1200},
            //桃心眼，胳膊向前抬至150度
            {0, 0, 0, 0, 0, 0, 0, 1080, 1200},
            //桃心眼,胳膊向外抬起上下摆动三次，身体转360度（烟花4000）
            {0, 0, 180, 180, 0, 0, 3, 1000, 1200, 'y'},
            {0, 0, 150, 150, 0, 0, 4, 1000, 1200},
            //停1.5秒
            {0, 0, 180, 180, 0, 0, 4, 1050, 1200},
            {0, 0, 0, 0, 0, 0, 3, 1050, 1200},
            //桃心眼，胳膊向前抬至90度（苹果3000）
            {0, 0, 90, 0, 1, 0, 1, 1000, 1000, 'p'},
            {0, 0, 0, 90, 1, 0, 2, 1000, 1000},
            {0, 0, 90, 0, 0, 0, 0, 1000, 1200},
            {0, 0, 0, 0, 0, 0, 0, 2, 870, 1200},
            //右胳膊向前抬起90度，向前走1.5秒
            {0, 0, 0, 90, 0, 30, 1, 1675, 1000, 'b'},
            //向后走1.5秒
            {0, 90, 0, 0, 0, 0, 2, 1675, 1000},
            //脸变红色，头先向左转60度再向右120度，再向左60度。双胳膊向前抬起180度（花4000）
            {0, 90, 0, 0, 5, 0, 3, 1000, 1200, 'h'},
            {90, 0, 0, 0, 60, 0, 4, 1000, 1200},
            //左胳膊向外抬起，上下挥动3次
            {0, 90, 0, 0, 5, 0, 0, 1045, 1200},
            {90, 0, 0, 0, 0, 0, 0, 1045, 1200},
            //桃心眼,胳膊向外抬起上下摆动三次，身体转720度（桃心5000）
            {0, 0, 90, 0, 120, 0, 3, 1000, 1200, 'i'},
            {0, 0, 0, 90, 0, 0, 3, 1000, 1200},
            {0, 0, 90, 0, 120, 3, 3, 1000, 1200},
            {0, 0, 150, 150, 0, 0, 3, 1050, 1200},
            //桃心眼，胳膊向前抬至150度
            {0, 0, 0, 90, 0, 1, 3, 1000, 1200, 'p'},
            {0, 0, 90, 0, 0, 30, 3, 1000, 1200},
            {0, 0, 0, 90, 0, 1, 3, 1000, 1200},
            {0, 0, 90, 0, 0, 30, 3, 1000, 1200},
            {0, 0, 0, 90, 0, 1, 3, 1000, 1200},
            {0, 0, 90, 0, 0, 30, 3, 1000, 1200},
            //桃心眼，头向上抬30度，胳膊向前抬至150度
            {0, 0, 0, 90, 0, 1, 3, 1000, 1200},
            {0, 0, 180, 180, 0, 30, 3, 1000, 1200},
            //恢复正常
            {0, 0, 180, 180, 0, 0, 0, 2000, 1200, 'b'},
    };

    String[] eyeDrawables = new String[]
            {
                    "eye_hua",
                    "eye_pingguo",
                    "eye_bianyan",
                    "eye_taiyang",
                    "eye_xingxing",
                    "eye_yanhua"
            };
    HashMap<Character, String> eyeMaps = new HashMap<>(eyeDrawables.length);
}
