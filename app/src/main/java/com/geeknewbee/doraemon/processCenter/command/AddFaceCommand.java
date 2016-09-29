package com.geeknewbee.doraemon.processcenter.command;

public class AddFaceCommand extends Command {
    public byte[] data;


    public AddFaceCommand(byte[] content) {
        super(CommandType.PERSON_ADD_FACE);
        this.data = content;
    }
}
