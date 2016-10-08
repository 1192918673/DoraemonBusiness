package com.geeknewbee.doraemon.processcenter;

import com.geeknewbee.doraemon.processcenter.command.SportAction;
import com.geeknewbee.doraemon.processcenter.command.SportActionSetCommand;
import com.geeknewbee.doraemonsdk.BaseApplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * 从文件中解析舞蹈动作
 * {头部左右0 ,头部上下1,左手前后2,左手上下3,右手前后4,右手上下5,
 * 行走6(1 向前，2向后，3向右30度，4向左30度),持续时间（毫秒）7,表情名字(可能没有)8}
 * 规则：
 * 1.必须是以"{" 开头 "}"结尾
 * 1.如果角度为0 则是维持上次的角度不变  如果是1则是复位到默认位置
 * 2.所有的角度都是绝对值
 * 3.手臂的上下电机:默认角度是0，这个值一直都是正数 ，最大值是45
 * 4.手臂的前后电机:默认角度是0，向前是正数，向后是负数，最大值是180,最小值是-45
 * 5.头部水平：默认角度是0，向左是正数，向右是负数，最大值是60,最小值是-60
 * 6.头部垂直:默认角度是0，向上是正数，向下是负数,最大值是12,最小值是-12
 * 7.行走现在有4个选项(1 向前，2向后，3向右30度，4向左30度)
 * 8.有对于表情则填写对应表情的名字，没有则不用填写
 */
public class ParseDanceCommandTask {
    private ParseThread parseThread;

    public void start(int rawId) {
        if (rawId < 1)
            return;

        InputStream in = BaseApplication.mContext.getResources().openRawResource(rawId);
        InputStreamReader reader = new InputStreamReader(in);
        if (parseThread == null)
            parseThread = new ParseThread(reader);

        parseThread.start();
    }

    public void stop() {
        if (parseThread != null)
            parseThread.cancel();
    }

    private class ParseThread extends Thread {
        InputStreamReader inputStreamReader;
        boolean isStop = false;

        public ParseThread(InputStreamReader inputStreamReader) {
            this.inputStreamReader = inputStreamReader;
        }

        @Override
        public void run() {
            super.run();
            isStop = false;

            List<SportAction> commands = new ArrayList<>();
            try {
                BufferedReader bufReader = new BufferedReader(inputStreamReader);
                String line;
                SportAction sportAction;
                while ((line = bufReader.readLine()) != null && !isStop) {
                    sportAction = SportActionUtil.parseSportCommand(line);
                    if (sportAction != null)
                    commands.add(sportAction);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    inputStreamReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (!isStop)
                Doraemon.getInstance(BaseApplication.mContext).addCommand(new SportActionSetCommand(commands));
        }

        public void cancel() {
            isStop = true;
        }
    }
}
