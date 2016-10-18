package com.geeknewbee.doraemon.processcenter;

import android.content.Context;

import com.geeknewbee.doraemon.R;
import com.geeknewbee.doraemon.constants.Constants;
import com.geeknewbee.doraemon.processcenter.command.Command;
import com.geeknewbee.doraemon.processcenter.command.ExpressionCommand;
import com.geeknewbee.doraemon.processcenter.command.LocalResourceCommand;
import com.geeknewbee.doraemon.processcenter.command.SoundCommand;
import com.geeknewbee.doraemon.processcenter.command.SportActionSetCommand;
import com.geeknewbee.doraemon.processcenter.command.SyncCommand;

import java.util.ArrayList;
import java.util.List;

/**
 * 自动演示
 */
public class AutoDemonstrationManager {
    public static volatile AutoDemonstrationManager instance;
    private final Context context;

    private AutoDemonstrationManager(Context context) {
        this.context = context;
    }

    public static AutoDemonstrationManager getInstance(Context context) {
        if (instance == null) {
            synchronized (AutoDemonstrationManager.class) {
                if (instance == null) {
                    instance = new AutoDemonstrationManager(context);
                }
            }
        }
        return instance;
    }

    public void start() {
        stop();
        addAutoCommand();
    }

    /**
     * 循环添加任务
     */
    public void circle() {
        addAutoCommand();
    }

    private void addAutoCommand() {
        List<Command> commands = new ArrayList<>();

        SoundCommand soundCommand = new SoundCommand(Constants.SELF_INTRODUCTION, SoundCommand.InputSource.TIPS);
        SportActionSetCommand actionSetCommand = LocalResourceManager.getInstance().getActionSetCommand(LocalResourceManager.ACTION_THANK_YOU);
        ExpressionCommand expressionCommand = new ExpressionCommand("wei_xiao", 2);
        commands.add(soundCommand);
        commands.add(actionSetCommand);
        commands.add(expressionCommand);
        SyncCommand syncCommand = new SyncCommand(commands);
        SyncQueue.getInstance().addCommand(syncCommand);

        commands.clear();
        commands.add(new SoundCommand("今天的天气很好", SoundCommand.InputSource.TIPS));
        syncCommand = new SyncCommand(commands);
        SyncQueue.getInstance().addCommand(syncCommand);

        commands.clear();
        commands.add(new LocalResourceCommand(R.raw.little_apple));
        commands.add(LocalResourceManager.getInstance().getDanceCommand(LocalResourceManager.XIAO_PING_GUO));
        syncCommand = new SyncCommand(commands);
        SyncQueue.getInstance().addCommand(syncCommand);
    }

    public void stop() {
        SyncQueue.getInstance().stop();
    }

}
