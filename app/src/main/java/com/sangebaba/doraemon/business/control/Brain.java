package com.sangebaba.doraemon.business.control;

/**
 * 大脑中枢
 * 对声音、人脸输入 进行各种响应
 */
public class Brain {
    private ILimbs limbs;
    private IMouth mouth;

    public Brain(IMouth mouth, ILimbs limbs) {
        this.limbs = limbs;
        this.mouth = mouth;
    }

    //针对不同的输出有不同的处理队列，分别对两个队列进行处理
}
