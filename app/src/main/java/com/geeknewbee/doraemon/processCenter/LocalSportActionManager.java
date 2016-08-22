package com.geeknewbee.doraemon.processcenter;

import com.geeknewbee.doraemon.R;
import com.geeknewbee.doraemon.processcenter.command.ActionSetCommand;
import com.geeknewbee.doraemon.processcenter.command.SportAction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 本地的运动集合，因为现在服务器是返回固定的几个动作，这里和服务做对应。
 * 以后可以让服务器直接发送约定好的命令，直接执行
 */
public class LocalSportActionManager extends Thread {
    private static LocalSportActionManager instance;
    private Map<String, List<SportAction>> localActionMap;

    private LocalSportActionManager() {
        localActionMap = new HashMap<>();
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
    }
}
