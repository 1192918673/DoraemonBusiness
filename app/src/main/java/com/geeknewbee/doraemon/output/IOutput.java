package com.geeknewbee.doraemon.output;

import com.geeknewbee.doraemon.processcenter.command.Command;

public interface IOutput {
    void addCommand(Command command);

    boolean isBusy();

    void setBusy(boolean isBusy);
}
