package com.geeknewbee.doraemon.processcenter.command;

import com.geeknewbee.doraemon.entity.GetAnswerResponse;

/**
 * Created by GYY on 2016/9/6.
 */
public class BLCommand extends Command {

    private GetAnswerResponse response;

    public BLCommand(GetAnswerResponse response) {
        super(CommandType.BL);
        this.response = response;
    }

    public GetAnswerResponse getResponse() {
        return response;
    }
}
