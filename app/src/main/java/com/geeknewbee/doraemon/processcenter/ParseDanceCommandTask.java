package com.geeknewbee.doraemon.processcenter;

import android.text.TextUtils;

import com.geeknewbee.doraemon.output.action.LeXingUtil;
import com.geeknewbee.doraemon.processcenter.command.DanceAction;
import com.geeknewbee.doraemon.processcenter.command.DanceCommand;
import com.geeknewbee.doraemonsdk.BaseApplication;
import com.geeknewbee.doraemonsdk.utils.BytesUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 从文件中解析舞蹈动作
 * {头部左右0 ,头部上下1,左手前后2,左手上下3,右手前后4,右手上下5,
 * 行走6(1 向前，2向后，3向右30度，4向左30度),持续时间（毫秒）7,表情名字(可能没有)8}
 * 规则：
 * 1.必须是以"{" 开头 "}"结尾
 * 1.如果角度为0 则是维持上次的角度不变  如果是1则是复位到默认位置
 * 2.所有的角度都是绝对值
 * 3.手臂的上下电机:默认角度是0，这个值一直都是正数 ，最大值是45
 * 4.手臂的前后电机:默认角度是0，向前是正数，向后是负数，最大值是180,最小值是-45
 * 5.头部水平：默认角度是0，向左是正数，向右是负数，最大值是60,最小值是-60
 * 6.头部垂直:默认角度是0，向上是正数，向下是负数,最大值是12,最小值是-12
 * 7.行走现在有4个选项(1 向前，2向后，3向右30度，4向左30度)
 * 8.有对于表情则填写对应表情的名字，没有则不用填写
 */
public class ParseDanceCommandTask {

    public static final int FOOT_DIRECTION_UP = 1;
    public static final int FOOT_DIRECTION_DOWN = 2;
    public static final int FOOT_DIRECTION_RIGHT = 3;
    public static final int FOOT_DIRECTION_LEFT = 4;

    public static final int RESET_FLAG = 1;//复位到默认位置Flag
    public static final int UNCHANGED_FLAG = 0;//角度不变的Flag

    public static char DEFAULT_ARM_ANTERIO_POSTERIOR_ANGLE = 0x0;//手臂前后方向垂直
    public static char DEFAULT_ARM_UP_DOWN_ANGLE = 0x0;//手臂上下方向垂直

    public static char DEFAULT_HEAD_HORIZONTAL_ANGLE = 0x0;//头左右方向初始向前
    public static char DEFAULT_HEAD_VERTICAL_ANGLE = 0x0;//头上下方向初始向前

    public static char UNCHANGE_ANGLE = 0xFFFF;//当前不改变的时候返回该值

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

    private DanceAction parseCommand(String line) {
        //必须是以"{" 开头 "}"结尾
        if (TextUtils.isEmpty(line) || !line.startsWith("{") || !line.endsWith("}"))
            return null;
        String content = line.substring(1, line.length() - 1);
        String[] strings = content.split(",");
        if (strings.length < 8)
            return null;

        DanceAction danceAction = new DanceAction();

        //0 头部水平旋转
        int headHorizontal = Integer.parseInt(strings[0].trim());
        //1 头部上下运动
        int headVertical = Integer.parseInt(strings[1].trim());
        //2 左手前后
        int leftAPAngle = Integer.parseInt(strings[2].trim());
        //3 左手上下
        int leftUpAnDownAngle = Integer.parseInt(strings[3].trim());
        //4 右手前后
        int rightAPAngle = Integer.parseInt(strings[4].trim());
        //5 右手上下
        int rightUpAndDownAngle = Integer.parseInt(strings[5].trim());

        //6 脚步运动方向
        int footDirection = Integer.parseInt(strings[6].trim());

        //7 整个运动持续时间
        int time = Integer.parseInt(strings[7].trim());
        danceAction.delayTime = time;

        danceAction.topCommand = getTopCommand(leftUpAnDownAngle, rightUpAndDownAngle, rightAPAngle, leftAPAngle, headHorizontal, headVertical, time);
        danceAction.footCommand = getFootCommandOfLeXing(footDirection);

        //8 运动的表情
        if (strings.length == 9 && !TextUtils.isEmpty(strings[8].trim())) {
            danceAction.expressionName = strings[8].trim();
        }

        return danceAction;
    }

    /**
     * 获取头和手的动作指令
     * 运动关节		协议中电机号
     * 脑袋	左右	电机1
     * 俯仰	电机2
     * 左手	前后	电机3
     * 上下	电机4
     * 右手	前后	电机5
     * 上下	电机6
     *
     * @param leftUpAndDownAngle
     * @param rightUpAndDownAngle
     * @param rightAPAngle
     * @param leftAPAngle
     * @param headHorizontal
     * @param headVertical
     * @param time
     * @return
     */
    private String getTopCommand(int leftUpAndDownAngle, int rightUpAndDownAngle, int rightAPAngle, int leftAPAngle, int headHorizontal, int headVertical, int time) {
        Map<Character, Character> contentMap = new HashMap<>();
        char result;
        result = calculateHeadHorizontalAngle(headHorizontal);
        if (result != UNCHANGE_ANGLE)
            contentMap.put((char) 0x01, result);

        result = calculateHeadVerticalAngle(headVertical);
        if (result != UNCHANGE_ANGLE)
            contentMap.put((char) 0x02, result);

        result = calculateArmAnterioPosteriorAngle(leftAPAngle);
        if (result != UNCHANGE_ANGLE)
            contentMap.put((char) 0x03, result);

        result = calculateArmUpAndDownAngle(leftUpAndDownAngle);
        if (result != UNCHANGE_ANGLE)
            contentMap.put((char) 0x04, result);

        result = calculateArmAnterioPosteriorAngle(rightAPAngle);
        if (result != UNCHANGE_ANGLE)
            contentMap.put((char) 0x05, result);

        result = calculateArmUpAndDownAngle(rightUpAndDownAngle);
        if (result != UNCHANGE_ANGLE)
            contentMap.put((char) 0x06, result);

        char[] timeChar = BytesUtils.getHighAndLowChar(time);
        char[] buf = new char[contentMap.size() * 2 + 6];

        buf[0] = 0x02;
        buf[1] = (char) contentMap.size();
        int index = 1;
        for (Character key : contentMap.keySet()) {
            buf[index + 1] = key;
            buf[index + 2] = contentMap.get(key);
            index += 2;
        }
        buf[index + 1] = timeChar[0];
        buf[index + 2] = timeChar[1];
        buf[index + 3] = 0x00;
        buf[index + 4] = 0x00;

        return String.valueOf(buf);
    }

    /**
     * 计算头部水平方向角度
     *
     * @param headVertical
     * @return
     */
    private char calculateHeadVerticalAngle(int headVertical) {
        if (headVertical == RESET_FLAG)
            return DEFAULT_HEAD_VERTICAL_ANGLE;
        else if (headVertical == UNCHANGED_FLAG)
            return UNCHANGE_ANGLE;
        else if (headVertical < MIN_HEAD_VERTICAL_ANGLE)
            headVertical = MIN_HEAD_VERTICAL_ANGLE;
        else if (headVertical > MAX_HEAD_VERTICAL_ANGLE)
            headVertical = MAX_HEAD_VERTICAL_ANGLE;

        return (char) (DEFAULT_HEAD_VERTICAL_ANGLE + headVertical);
    }

    /**
     * 计算头部水平方向角度
     *
     * @param headHorizontal
     * @return
     */
    private char calculateHeadHorizontalAngle(int headHorizontal) {
        if (headHorizontal == RESET_FLAG)
            return DEFAULT_HEAD_HORIZONTAL_ANGLE;
        else if (headHorizontal == UNCHANGED_FLAG)
            return UNCHANGE_ANGLE;
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
    private char calculateArmAnterioPosteriorAngle(int leftDianjiAngle) {
        if (leftDianjiAngle == RESET_FLAG)
            return DEFAULT_ARM_UP_DOWN_ANGLE;
        else if (leftDianjiAngle == UNCHANGED_FLAG)
            return UNCHANGE_ANGLE;
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
    private char calculateArmUpAndDownAngle(int leftDuojiAngle) {
        if (leftDuojiAngle == RESET_FLAG)
            return DEFAULT_ARM_ANTERIO_POSTERIOR_ANGLE;
        else if (leftDuojiAngle == UNCHANGED_FLAG)
            return UNCHANGE_ANGLE;
        else if (leftDuojiAngle > MAX_ARM_UP_DOWN_ANGLE)
            leftDuojiAngle = MAX_ARM_UP_DOWN_ANGLE;

        return (char) (DEFAULT_ARM_ANTERIO_POSTERIOR_ANGLE + leftDuojiAngle);
    }

    /**
     * 以前的的中空板的脚步命令
     *
     * @param footDirection
     * @param footSpeed
     * @param roundCount
     * @return
     */
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

    /**
     * 乐行的脚步命令
     *
     * @param footDirection
     * @return
     */
    private String getFootCommandOfLeXing(int footDirection) {
        if (footDirection == 0)
            return "";

        int[] result = new int[2];
        int distance = 100;
        int angle = 30;
        switch (footDirection) {
            case FOOT_DIRECTION_UP:
                result = LeXingUtil.getSpeed(LeXingUtil.DIRECTION_FORE, distance, footDirection);
                break;
            case FOOT_DIRECTION_DOWN:
                result = LeXingUtil.getSpeed(LeXingUtil.DIRECTION_BACK, distance, footDirection);
                break;
            case FOOT_DIRECTION_RIGHT:
                result = LeXingUtil.getSpeed(LeXingUtil.DIRECTION_RIGHT, LeXingUtil.DIRECTION_CLOCKWISE, angle, 0, footDirection);
                break;
            case FOOT_DIRECTION_LEFT:
                result = LeXingUtil.getSpeed(LeXingUtil.DIRECTION_LEFT, LeXingUtil.DIRECTION_EASTERN, angle, 0, footDirection);
                break;
        }
        return String.format("%d|%d", result[0], result[0]);
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
