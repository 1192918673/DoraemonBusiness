package com.geeknewbee.doraemon.input;

import com.geeknewbee.doraemon.processcenter.command.Command;

import java.util.List;

/**
 * 接受后台服务器消息
 */
public interface IMessageReceive {

    void setMessageListener(MessageListener listener);

    interface MessageListener {

        /**
         * 收到消息回调
         *
         * @param commands
         */
        void onReceivedMessage(List<Command> commands);
    }

    void destroy();
}
