package com.geeknewbee.doraemon.processcenter.command;

import com.geeknewbee.doraemon.App;
import com.geeknewbee.doraemon.output.BLM;
import com.geeknewbee.doraemon.output.FaceManager;
import com.geeknewbee.doraemon.output.IOutput;
import com.geeknewbee.doraemon.output.OtherCommandManager;
import com.geeknewbee.doraemon.output.ReadFaceManager;
import com.geeknewbee.doraemon.output.queue.LimbsTaskQueue;
import com.geeknewbee.doraemon.output.queue.MouthTaskQueue;

public enum CommandType {
    PLAY_SOUND {
        @Override
        public IOutput getOutput() {
            return MouthTaskQueue.getInstance();
        }
    }, SHOW_EXPRESSION {
        @Override
        public IOutput getOutput() {
            return FaceManager.getInstance();
        }
    }, PLAY_MUSIC {
        @Override
        public IOutput getOutput() {
            return MouthTaskQueue.getInstance();
        }
    },
    SETTING_WIFI {
        @Override
        public IOutput getOutput() {
            return OtherCommandManager.getInstance();
        }
    }, SETTING_VOLUME {
        @Override
        public IOutput getOutput() {
            return OtherCommandManager.getInstance();
        }
    }, STOP {
        @Override
        public IOutput getOutput() {
            return OtherCommandManager.getInstance();
        }
    }, SPORT_ACTION_SET {
        @Override
        public IOutput getOutput() {
            return LimbsTaskQueue.getInstance();
        }
    },
    PLAY_LOCAL_RESOURCE {
        @Override
        public IOutput getOutput() {
            return MouthTaskQueue.getInstance();
        }
    }, PLAY_JOKE {
        @Override
        public IOutput getOutput() {
            return MouthTaskQueue.getInstance();
        }
    }, BLUETOOTH_CONTROL_FOOT {
        @Override
        public IOutput getOutput() {
            return LimbsTaskQueue.getInstance();
        }
    },
    BL {
        @Override
        public IOutput getOutput() {
            return BLM.getInstance();
        }
    }, BL_SP {
        @Override
        public IOutput getOutput() {
            return BLM.getInstance();
        }
    }, TAKE_PICTURE {
        @Override
        public IOutput getOutput() {
            return OtherCommandManager.getInstance();
        }
    }, PLAY_MOVIE {
        @Override
        public IOutput getOutput() {
            return MouthTaskQueue.getInstance();
        }
    }, SLEEP {
        @Override
        public IOutput getOutput() {
            return OtherCommandManager.getInstance();
        }
    }, LEARN_EN {
        @Override
        public IOutput getOutput() {
            return MouthTaskQueue.getInstance();
        }
    },
    PERSON_START {
        @Override
        public IOutput getOutput() {
            return ReadFaceManager.getInstance(App.mContext);
        }
    }, PERSON_ADD_FACE {
        @Override
        public IOutput getOutput() {
            return ReadFaceManager.getInstance(App.mContext);
        }
    }, PERSON_SET_NAME {
        @Override
        public IOutput getOutput() {
            return ReadFaceManager.getInstance(App.mContext);
        }
    }, PERSON_DELETE_ALL {
        @Override
        public IOutput getOutput() {
            return ReadFaceManager.getInstance(App.mContext);
        }
    };

    public abstract IOutput getOutput();
}
