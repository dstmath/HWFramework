package com.huawei.android.view;

import android.view.KeyEvent;
import com.huawei.annotation.HwSystemApi;

public class KeyEventEx {
    public static final int getKeycodeFingerprintLeft() {
        return 513;
    }

    public static final int getKeycodeFingerprintRight() {
        return 514;
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
