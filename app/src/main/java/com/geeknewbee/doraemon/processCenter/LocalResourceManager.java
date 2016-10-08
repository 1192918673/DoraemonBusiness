package com.geeknewbee.doraemon.processcenter;

import android.text.TextUtils;

import com.geeknewbee.doraemon.App;
import com.geeknewbee.doraemon.R;
import com.geeknewbee.doraemon.processcenter.command.DanceCommand;
import com.geeknewbee.doraemon.processcenter.command.SportAction;
import com.geeknewbee.doraemon.processcenter.command.SportActionSetCommand;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 本地的运动集合，因为现在服务器是返回固定的几个动作，这里和服务做对应。
 * 以后可以让服务器直接发送约定好的命令，直接执行
 */
public class LocalResourceManager extends Thread {
    public static final String XIAO_PING_GUO = "xiao_ping_guo";
    public static final String NO_ANSWER = "no_answer";
    public static final String ACTION_ARM_MOVE = "arm_move";
    public static final String ACTION_ARM_UP_DOWN_MOVE = "action_arm_up_down_move";
    public static final String ACTION_THANK_YOU = "action_thank_you";
    private volatile static LocalResourceManager instance;
    private final String[] noAnswerList;
    private final String[] defaultAnswerList;
    private Map<String, List<SportAction>> localActionMap;
    private boolean isRunning = false;

    private LocalResourceManager() {
        localActionMap = new HashMap<>();
        noAnswerList = App.mContext.getResources().getStringArray(R.array.no_answer);
        defaultAnswerList = App.mContext.getResources().getStringArray(R.array.default_answer);
    }

    public static LocalResourceManager getInstance() {
        if (instance == null)
            synchronized (LocalResourceManager.class) {
                if (instance == null)
                    instance = new LocalResourceManager();
            }

        return instance;
    }

    /**
     * 初始化本地动作库
     */
    public void initLocalAction() {
        new Thread(this).start();
    }

    /**
     * 获取对应的动作集合命令
     *
     * @param actionNameList
     * @return
     */
    public SportActionSetCommand getActionSetCommand(List<String> actionNameList) {
        if (actionNameList == null || actionNameList.isEmpty())
            return null;
        SportActionSetCommand command = new SportActionSetCommand();
        for (String action : actionNameList) {
            if (localActionMap.containsKey(action)) {
                command.addSportAction(localActionMap.get(action));
            }
        }
        return command;
    }

    public SportActionSetCommand getActionSetCommand(String actionName) {
        if (TextUtils.isEmpty(actionName))
            return null;
        SportActionSetCommand command = new SportActionSetCommand();
        command.addSportAction(localActionMap.get(actionName));
        return command;
    }

    public DanceCommand getDanceCommand(String actionName) {
        if (TextUtils.isEmpty(actionName))
            return null;
        DanceCommand command = new DanceCommand();
        command.addSportAction(localActionMap.get(actionName));
        return command;
    }

    /**
     * 本地是否有对应的动作
     *
     * @param actionName 动作名
     * @return
     */
    public boolean containsAction(String actionName) {
        if (TextUtils.isEmpty(actionName))
            return false;

        return localActionMap.containsKey(actionName);
    }

    @Override
    public void run() {
        isRunning = true;
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

        actions = SportActionUtil.parseSportCommand(R.raw.action_arm_move);
        localActionMap.put(ACTION_ARM_MOVE, actions);

        actions = SportActionUtil.parseSportCommand(R.raw.action_no_answer);
        localActionMap.put(NO_ANSWER, actions);

        actions = SportActionUtil.parseSportCommand(R.raw.action_arm_up_down_move);
        localActionMap.put(ACTION_ARM_UP_DOWN_MOVE, actions);

        actions = SportActionUtil.parseSportCommand(R.raw.action_thank_you);
        localActionMap.put(ACTION_THANK_YOU, actions);

        final OldSportActionUtil oldSportActionUtil = new OldSportActionUtil();
        actions = oldSportActionUtil.parseOldActionScript(oldSportActionUtil.xiao_ping_guo_dance_scripts);
        localActionMap.put(XIAO_PING_GUO, actions);
        isRunning = false;
    }

    /**
     * 获取当没有回答时候的默认回答
     *
     * @return
     */
    public String getNoAnswerString() {
        return noAnswerList[getRandom(noAnswerList.length)];
    }

    /**
     * 获取默认的回答
     *
     * @return
     */
    public String getDefaultString() {
        return defaultAnswerList[getRandom(defaultAnswerList.length)];
    }

    /**
     * 生成指定范围的随机数
     *
     * @return int随机数
     */
    private int getRandom(int max) {
        Random random = new Random();
        return random.nextInt(max);
    }

}
