package com.geeknewbee.doraemon.processcenter.command;

/**
 * Created by ACER on 2016/8/12.
 */
public class WeatherCommand extends Command {

    public WeatherCommand(String cityId) {
        super(CommandType.WEATHER, cityId);
    }
}
