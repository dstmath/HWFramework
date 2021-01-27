package com.huawei.android.view.accessibility;

import android.view.accessibility.AccessibilityManager;

public class AccessibilityManagerEx {
    public static boolean sendFingerprintGesture(AccessibilityManager manager, int keyCode) {
        return manager.sendFingerprintGesture(keyCode);
    }
}
