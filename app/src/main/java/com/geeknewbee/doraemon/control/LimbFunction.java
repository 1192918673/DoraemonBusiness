package com.geeknewbee.doraemon.control;

/**
 * 运动控制的功能
 */
public enum LimbFunction {
    READ_STATE {
        @Override
        public byte getFunctionCode() {
            return 0x01;
        }
    }, CONTROL_DUO_JI {
        @Override
        public byte getFunctionCode() {
            return 0x02;
        }
    }, CONTROL_DIAN_JI {
        @Override
        public byte getFunctionCode() {
            return 0x03;
        }
    }, STOP_ALL {
        @Override
        public byte getFunctionCode() {
            return 0x04;
        }
    };

    public abstract byte getFunctionCode();
}
