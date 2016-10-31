package com.geeknewbee.doraemon.processcenter;

import android.content.Context;

import com.geeknewbee.doraemon.R;
import com.geeknewbee.doraemon.constants.Constants;
import com.geeknewbee.doraemon.entity.WeatherResponse;
import com.geeknewbee.doraemon.processcenter.command.Command;
import com.geeknewbee.doraemon.processcenter.command.LocalResourceCommand;
import com.geeknewbee.doraemon.processcenter.command.SoundCommand;
import com.geeknewbee.doraemon.processcenter.command.SyncCommand;
import com.geeknewbee.doraemon.weather.WeatherManager;

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

        commands.add(LocalResourceManager.getInstance().getActionSetCommand(LocalResourceManager.ACTION_ARM_UP_DOWN_MOVE));
        commands.add(new SoundCommand(Constants.SELF_INTRODUCTION, SoundCommand.InputSource.TIPS));
        CommandQueue.getInstance(context).addCommand(new SyncCommand.Builder().setCommandList(commands).build());

        commands = new ArrayList<>();
        commands.add(new SoundCommand(Constants.SELF_POEM, SoundCommand.InputSource.TIPS));
        CommandQueue.getInstance(context).addCommand(new SyncCommand.Builder().setCommandList(commands).build());

        WeatherResponse.Weather weather = WeatherManager.getInstance().getWeatherReport();
        if (weather != null) {
            commands = new ArrayList<>();
            commands.add(new SoundCommand(String.format(context.getString(R.string.tips_weather),
                    weather.getCity(), weather.getWeather(), weather.getWindpower(), weather.getWinddirection(),
                    weather.getTemperature(), weather.getHumidity()), SoundCommand.InputSource.TIPS));
            CommandQueue.getInstance(context).addCommand(new SyncCommand.Builder().setCommandList(commands).build());
        }

        commands = new ArrayList<>();
        commands.add(new SoundCommand("下面我给大家跳支舞", SoundCommand.InputSource.TIPS));
        CommandQueue.getInstance(context).addCommand(new SyncCommand.Builder().setCommandList(commands).build());

        commands = new ArrayList<>();
        commands.add(new LocalResourceCommand(R.raw.little_apple_short));
        commands.add(LocalResourceManager.getInstance().getDanceCommand(LocalResourceManager.XIAO_PING_GUO_SHORT));
        CommandQueue.getInstance(context).addCommand(new SyncCommand.Builder().setCommandList(commands).build());

        commands = new ArrayList<>();
        commands.add(new SoundCommand(Constants.SELF_INTRODUCTION_2, SoundCommand.InputSource.TIPS));
        CommandQueue.getInstance(context).addCommand(new SyncCommand.Builder().setCommandList(commands).build());

        commands = new ArrayList<>();
        commands.add(new SoundCommand(Constants.SELF_INTRODUCTION_3, SoundCommand.InputSource.TIPS));
        CommandQueue.getInstance(context).addCommand(new SyncCommand.Builder().setCommandList(commands).build());

        commands = new ArrayList<>();
        commands.add(new SoundCommand(Constants.SELF_INTRODUCTION_4, SoundCommand.InputSource.TIPS));
        CommandQueue.getInstance(context).addCommand(new SyncCommand.Builder().setCommandList(commands).build());

        commands = new ArrayList<>();
        commands.add(new SoundCommand(Constants.END, SoundCommand.InputSource.TIPS));
        commands.add(LocalResourceManager.getInstance().getActionSetCommand(LocalResourceManager.ACTION_THANK_YOU));
        CommandQueue.getInstance(context).addCommand(new SyncCommand.Builder().setCommandList(commands).build());
    }

    public void stop() {
        CommandQueue.getInstance(context).stop();
    }

}
