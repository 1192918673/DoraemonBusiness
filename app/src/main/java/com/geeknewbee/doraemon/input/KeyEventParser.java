package com.geeknewbee.doraemon.input;

import android.view.KeyEvent;

import com.geeknewbee.doraemon.App;
import com.geeknewbee.doraemon.entity.event.PressNoseEvent;
import com.geeknewbee.doraemon.processcenter.PressNoseType;
import com.geeknewbee.doraemonsdk.utils.LogUtils;

import org.greenrobot.eventbus.EventBus;

/**
 * 猫的很多外部触摸事件是上报键值的方式
 */
public class KeyEventParser {
    private static final int KEY_NOSE_PRESS = KeyEvent.KEYCODE_D;
    private static boolean isLongPressOfNosePress = false;

    public static boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KEY_NOSE_PRESS:
                event.startTracking();
                LogUtils.d(App.TAG, "鼻子按下事件");
                if (event.getRepeatCount() == 0) {
                    isLongPressOfNosePress = false;
                }
                return true;
        }
        return false;
    }

    public static boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KEY_NOSE_PRESS:
                LogUtils.d(App.TAG, "鼻子抬起事件");
                EventBus.getDefault().post(new PressNoseEvent(isLongPressOfNosePress ? PressNoseType.LONG_PRESS : PressNoseType.SHORT_PRESS));
                isLongPressOfNosePress = false;
                return true;
        }
        return false;
    }

    public static boolean onKeyLongPress(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KEY_NOSE_PRESS:
                isLongPressOfNosePress = true;
                return true;
        }
        return false;
    }

}
