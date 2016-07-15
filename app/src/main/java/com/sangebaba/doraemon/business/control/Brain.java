package com.sangebaba.doraemon.business.control;

/**
 * 大脑中枢
 * 对声音、人脸输入 进行各种响应
 * 输出行为有 语音| 四肢运动|显示表情|播放电影等
 * 输出终端有 喇叭/肢体/屏幕等。 每个终端保持一个 priority queue，每个终端的task任务必须串行。
 */
public class Brain {
    private ILimbs limbs;
    private IMouth mouth;

    public Brain(IMouth mouth, ILimbs limbs) {
        this.limbs = limbs;
        this.mouth = mouth;
    }

    public void addCommand(Command command) {
    }

    //针对不同的输出有不同的处理队列，分别对两个队列进行处理
}
