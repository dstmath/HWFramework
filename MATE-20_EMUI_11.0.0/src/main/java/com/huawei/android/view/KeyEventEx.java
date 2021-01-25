package com.huawei.android.view;

import android.view.KeyEvent;
import com.huawei.annotation.HwSystemApi;

public class KeyEventEx {
    @HwSystemApi
    public static final int FINGERPRINT_DOUBLE_TAP = 501;
    @HwSystemApi
    public static final int FINGERPRINT_SINGLE_TAP = 601;
    @HwSystemApi
    public static final int FLAG_FROM_FINGERPRINT = 2048;
    @HwSystemApi
    public static final int KEYCODE_FINGERPRINT_DOWN = 512;
    @HwSystemApi
    public static final int KEYCODE_FINGERPRINT_LEFT = 513;
    @HwSystemApi
    public static final int KEYCODE_FINGERPRINT_LONGPRESS = 502;
    @HwSystemApi
    public static final int KEYCODE_FINGERPRINT_RIGHT = 514;
    @HwSystemApi
    public static final int KEYCODE_FINGERPRINT_UP = 511;
    public static final int KEYCODE_SWING_SWIPE_DOWN = 713;
    public static final int KEYCODE_SWING_SWIPE_LEFT = 710;
    public static final int KEYCODE_SWING_SWIPE_PUSH = 714;
    public static final int KEYCODE_SWING_SWIPE_RIGHT = 711;
    public static final int KEYCODE_SWING_SWIPE_UP = 712;

    public static final int getKeycodeFingerprintLeft() {
        return KEYCODE_FINGERPRINT_LEFT;
    }

    public static final int getKeycodeFingerprintRight() {
        return KEYCODE_FINGERPRINT_RIGHT;
    }

    @HwSystemApi
    public static void setDisplayId(KeyEvent keyEvent, int displayId) {
        if (keyEvent != null) {
            keyEvent.setDisplayId(displayId);
        }
    }

    @HwSystemApi
    public static final boolean isSystemKey(int keyCode) {
        return KeyEvent.isSystemKey(keyCode);
    }
}
