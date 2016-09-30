package com.geeknewbee.doraemon.processcenter.command;

import com.geeknewbee.doraemon.output.AddFaceType;

public class AddFaceCommand extends Command {
    public byte[] data;
    public AddFaceType faceType;


    public AddFaceCommand(AddFaceType faceType, byte[] data) {
        super(CommandType.PERSON_ADD_FACE);
        this.data = data;
        this.faceType = faceType;
    }

}
