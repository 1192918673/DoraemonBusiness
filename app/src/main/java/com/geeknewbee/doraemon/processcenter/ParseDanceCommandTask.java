package com.geeknewbee.doraemon.processcenter;

import android.text.TextUtils;

import com.geeknewbee.doraemon.processcenter.command.DanceAction;
import com.geeknewbee.doraemon.processcenter.command.DanceCommand;
import com.geeknewbee.doraemonsdk.BaseApplication;
import com.geeknewbee.doraemonsdk.utils.BytesUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * 从文件中解析舞蹈动作
 * {左手舵机(90),右手舵机(90),右臂电机(180),左臂电机(180),头部旋转(120) ,头部俯仰(30),
 * 行走(1 向前，2向后，3向右30度，4向左30度),持续时间（毫秒）,行走的速度（0-1500），表情名字(可能没有)}
 * 规则：
 * 1.必须是以"{" 开头 "}"结尾
 * 1.如果角度为0 则是维持上次的角度不变  如果是1则是复位到默认位置
 * 2.所有的角度都是绝对值
 * 3.手臂的上下电机:默认角度是0，这个值一直都是正数 ，最大值是45
 * 4.手臂的前后电机:默认角度是0，向前是正数，向后是负数，最大值是180,最小值是-45
 * 5.头部水平：默认角度是0，向左是正数，向右是负数，最大值是60,最小值是-60
 * 6.头部垂直:默认角度是0，向上是正数，向下是负数,最大值是12,最小值是-12
 * 7.行走现在有4个选项(1 向前，2向后，3向右30度，4向左30度)
 * 8.行走速度一直是正数(0-1500)
 * 9.有对于表情则填写对应表情的名字，没有则不用填写
 */
public class ParseDanceCommandTask {

    public static final int FOOT_DIRECTION_UP = 1;
    public static final int FOOT_DIRECTION_DOWN = 2;
    public static final int FOOT_DIRECTION_RIGHT = 3;
    public static final int FOOT_DIRECTION_LEFT = 4;

    public static final int ROUND_COUNT = 1;
    public static final int RESET_FLAG = 1;//复位到默认位置Flag
    public static final int UNCHANGED_FLAG = 0;//角度不变的Flag

    public static char DEFAULT_ARM_ANTERIO_POSTERIOR_ANGLE = 0x0;//手臂前后方向垂直
    public static char DEFAULT_ARM_UP_DOWN_ANGLE = 0x0;//手臂上下方向垂直

    public static char DEFAULT_HEAD_HORIZONTAL_ANGLE = 0x0;//头左右方向初始向前
    public static char DEFAULT_HEAD_VERTICAL_ANGLE = 0x0;//头上下方向初始向前

    //手臂上下方向最大角度(默认垂直地面是0，只能向上运动)
    public static int MAX_ARM_UP_DOWN_ANGLE = 45;

    //手臂前后方向最大/最小角度(默认垂直地面是0，可以前后运动)
    public static int MAX_ARM_ANTERIO_POSTERIOR_ANGLE = 180;
    public static int MIN_ARM_ANTERIO_POSTERIOR_ANGLE = -45;

    //头的左右方向最大/最小角度(默认超前是0，可以左右运动)
    public static int MAX_HEAD_HORIZONTAL_ANGLE = 60;
    public static int MIN_HEAD_HORIZONTAL = -60;

    //头的上下方向最大/最小角度(默认超前是0，可以上下运动)
    public static int MAX_HEAD_VERTICAL_ANGLE = 12;
    public static int MIN_HEAD_VERTICAL_ANGLE = -12;

    private static char current_left_duoji;
    private static char current_right_duoji;
    private static char current_right_dianji;
    private static char current_left_dianji;
    private static char current_head_horizontal;
    private static char current_head_vertical;

    private ParseThread parseThread;

    public void start(int rawId) {
        if (rawId < 1)
            return;

        InputStream in = BaseApplication.mContext.getResources().openRawResource(rawId);
        InputStreamReader reader = new InputStreamReader(in);
        if (parseThread == null)
            parseThread = new ParseThread(reader);

        parseThread.start();
    }

    public void stop() {
        if (parseThread != null)
            parseThread.cancel();
    }

    private void resetData() {
        current_left_duoji = DEFAULT_ARM_ANTERIO_POSTERIOR_ANGLE;
        current_right_duoji = DEFAULT_ARM_ANTERIO_POSTERIOR_ANGLE;
        current_right_dianji = DEFAULT_ARM_UP_DOWN_ANGLE;
        current_left_dianji = DEFAULT_ARM_UP_DOWN_ANGLE;

        current_head_horizontal = DEFAULT_HEAD_HORIZONTAL_ANGLE;
        current_head_vertical = DEFAULT_HEAD_VERTICAL_ANGLE;
    }

    private DanceAction parseCommand(String line) {
        //必须是以"{" 开头 "}"结尾
        if (TextUtils.isEmpty(line) || !line.startsWith("{") || !line.endsWith("}"))
            return null;
        String content = line.substring(1, line.length() - 1);
        String[] strings = content.split(",");
        if (strings.length < 9)
            return null;

        DanceAction danceAction = new DanceAction();
        //0 左手舵机
        int leftDuojiAngle = Integer.parseInt(strings[0].trim());
        //1 右手舵机
        int rightDuojiAngle = Integer.parseInt(strings[1].trim());
        //2 右手电机
        int rightDianjiAngle = Integer.parseInt(strings[2].trim());
        //3 左手电机
        int leftDianjiAngle = Integer.parseInt(strings[3].trim());

        //4 头部水平旋转
        int headHorizontal = Integer.parseInt(strings[4].trim());
        //5 头部上下运动
        int headVertical = Integer.parseInt(strings[5].trim());

        //6 脚步运动方向
        int footDirection = Integer.parseInt(strings[6].trim());
        //8 脚步运动速度
        int footSpeed = Integer.parseInt(strings[8].trim());

        //7 整个运动持续时间
        int time = Integer.parseInt(strings[7].trim());
        danceAction.delayTime = time;

        danceAction.topCommand = getTopCommand(leftDuojiAngle, rightDuojiAngle, rightDianjiAngle, leftDianjiAngle, headHorizontal, headVertical, time);
        danceAction.footCommand = getFootCommand(footDirection, footSpeed, ROUND_COUNT);

        //9 运动的表情
        if (strings.length == 10 && !TextUtils.isEmpty(strings[9].trim())) {
            danceAction.expressionName = strings[9].trim();
        }

        return danceAction;
    }

    private String getTopCommand(int leftDuojiAngle, int rightDuojiAngle, int rightDianjiAngle, int leftDianjiAngle, int headHorizontal, int headVertical, int time) {
        current_left_duoji = calculateDuoJiValue(current_left_duoji, leftDuojiAngle);
        current_right_duoji = calculateDuoJiValue(current_right_duoji, rightDuojiAngle);

        current_left_dianji = calculateDianjiAngle(current_left_dianji, leftDianjiAngle);
        current_right_dianji = calculateDianjiAngle(current_right_dianji, rightDianjiAngle);

        current_head_horizontal = calculateHeadHorizontalAngle(current_head_horizontal, headHorizontal);
        current_head_vertical = calculateHeadVerticalAngle(current_head_vertical, headVertical);

        char[] timeChar = BytesUtils.getHighAndLowChar(time);
        char[] buf = new char[]{0x02, 0x06,
                0x01, current_left_duoji,
                0x02, current_right_duoji,
                0x03, current_right_dianji,
                0x04, current_left_dianji,
                0x05, current_head_horizontal,
                0x06, current_head_vertical,
                timeChar[0], timeChar[1], 0x00, 0x00};

        return String.valueOf(buf);
    }

    /**
     * 计算头部水平方向角度
     *
     * @param currentAngle
     * @param headVertical
     * @return
     */
    private char calculateHeadVerticalAngle(char currentAngle, int headVertical) {
        if (headVertical == RESET_FLAG)
            return DEFAULT_HEAD_VERTICAL_ANGLE;
        else if (headVertical == UNCHANGED_FLAG)
            return currentAngle;
        else if (headVertical < MIN_HEAD_VERTICAL_ANGLE)
            headVertical = MIN_HEAD_VERTICAL_ANGLE;
        else if (headVertical > MAX_HEAD_VERTICAL_ANGLE)
            headVertical = MAX_HEAD_VERTICAL_ANGLE;

        return (char) (DEFAULT_HEAD_VERTICAL_ANGLE + headVertical);
    }

    /**
     * 计算头部水平方向角度
     *
     * @param currentAngle
     * @param headHorizontal
     * @return
     */
    private char calculateHeadHorizontalAngle(char currentAngle, int headHorizontal) {
        if (headHorizontal == RESET_FLAG)
            return DEFAULT_HEAD_HORIZONTAL_ANGLE;
        else if (headHorizontal == UNCHANGED_FLAG)
            return currentAngle;
        else if (headHorizontal < MIN_HEAD_HORIZONTAL)
            headHorizontal = MIN_HEAD_HORIZONTAL;
        else if (headHorizontal > MAX_HEAD_HORIZONTAL_ANGLE)
            headHorizontal = MAX_HEAD_HORIZONTAL_ANGLE;

        return (char) (DEFAULT_HEAD_HORIZONTAL_ANGLE + headHorizontal);
    }

    /**
     * 计算手臂的前后的角度
     *
     * @param leftDianjiAngle
     * @return
     */
    private char calculateDianjiAngle(char currentAngle, int leftDianjiAngle) {
        if (leftDianjiAngle == RESET_FLAG)
            return DEFAULT_ARM_UP_DOWN_ANGLE;
        else if (leftDianjiAngle == UNCHANGED_FLAG)
            return currentAngle;
        else if (leftDianjiAngle < MIN_ARM_ANTERIO_POSTERIOR_ANGLE)
            leftDianjiAngle = MIN_ARM_ANTERIO_POSTERIOR_ANGLE;
        else if (leftDianjiAngle > MAX_ARM_ANTERIO_POSTERIOR_ANGLE)
            leftDianjiAngle = MAX_ARM_ANTERIO_POSTERIOR_ANGLE;

        return (char) (DEFAULT_ARM_UP_DOWN_ANGLE + leftDianjiAngle);
    }

    /**
     * 计算手臂的上下的角度
     *
     * @param leftDuojiAngle
     * @return
     */
    private char calculateDuoJiValue(char currentAngle, int leftDuojiAngle) {
        if (leftDuojiAngle == RESET_FLAG)
            return DEFAULT_ARM_ANTERIO_POSTERIOR_ANGLE;
        else if (leftDuojiAngle == UNCHANGED_FLAG)
            return currentAngle;
        else if (leftDuojiAngle > MAX_ARM_UP_DOWN_ANGLE)
            leftDuojiAngle = MAX_ARM_UP_DOWN_ANGLE;

        return (char) (DEFAULT_ARM_ANTERIO_POSTERIOR_ANGLE + leftDuojiAngle);
    }

    private String getFootCommand(int footDirection, int footSpeed, int roundCount) {
        if (footDirection == 0 || footSpeed == 0)
            return "";

        char hSpeedChar_1 = 0x00;
        char lSpeedChar_1 = 0x00;
        char hSpeedChar_2 = 0x00;
        char lSpeedChar_2 = 0x00;
        char round_count = (char) roundCount;

        char[] positiveHighAndLowChar;
        char[] minusHighAndLowChar;

        switch (footDirection) {
            case FOOT_DIRECTION_UP:
                positiveHighAndLowChar = BytesUtils.getHighAndLowChar(footSpeed);
                hSpeedChar_1 = hSpeedChar_2 = positiveHighAndLowChar[0];
                lSpeedChar_1 = lSpeedChar_2 = positiveHighAndLowChar[1];
                break;
            case FOOT_DIRECTION_DOWN:
                minusHighAndLowChar = BytesUtils.getHighAndLowChar(-footSpeed);
                hSpeedChar_1 = hSpeedChar_2 = minusHighAndLowChar[0];
                lSpeedChar_1 = lSpeedChar_2 = minusHighAndLowChar[1];
                break;
            case FOOT_DIRECTION_RIGHT:
                positiveHighAndLowChar = BytesUtils.getHighAndLowChar(footSpeed);
                minusHighAndLowChar = BytesUtils.getHighAndLowChar(-footSpeed);
                hSpeedChar_2 = positiveHighAndLowChar[0];
                lSpeedChar_2 = positiveHighAndLowChar[1];

                hSpeedChar_1 = minusHighAndLowChar[0];
                lSpeedChar_1 = minusHighAndLowChar[1];
                break;
            case FOOT_DIRECTION_LEFT:
                positiveHighAndLowChar = BytesUtils.getHighAndLowChar(footSpeed);
                minusHighAndLowChar = BytesUtils.getHighAndLowChar(-footSpeed);
                hSpeedChar_1 = positiveHighAndLowChar[0];
                lSpeedChar_1 = positiveHighAndLowChar[1];

                hSpeedChar_2 = minusHighAndLowChar[0];
                lSpeedChar_2 = minusHighAndLowChar[1];
                break;
        }
        char[] buf = new char[]{0x03, 0x02, 0x01, hSpeedChar_1, lSpeedChar_1, 0x00, round_count, 0x02, hSpeedChar_2, lSpeedChar_2, 0x00, round_count};
        return String.valueOf(buf);
    }

    private class ParseThread extends Thread {
        InputStreamReader inputStreamReader;
        boolean isStop = false;

        public ParseThread(InputStreamReader inputStreamReader) {
            this.inputStreamReader = inputStreamReader;
        }

        @Override
        public void run() {
            super.run();
            isStop = false;
            resetData();

            List<DanceAction> commands = new ArrayList<>();
            try {
                BufferedReader bufReader = new BufferedReader(inputStreamReader);
                String line;
                DanceAction danceAction;
                while ((line = bufReader.readLine()) != null && !isStop) {
                    danceAction = parseCommand(line);
                    commands.add(danceAction);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    inputStreamReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (!isStop)
                Doraemon.getInstance(BaseApplication.mContext).addCommand(new DanceCommand(commands));
        }

        public void cancel() {
            isStop = true;
        }
    }
}
