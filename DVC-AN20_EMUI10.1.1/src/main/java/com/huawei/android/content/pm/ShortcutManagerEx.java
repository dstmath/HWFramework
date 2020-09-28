package com.huawei.android.content.pm;

import android.content.pm.ShortcutManager;

public class ShortcutManagerEx {
    public static void onApplicationActive(ShortcutManager shortcutManager, String packageName, int userId) {
        if (shortcutManager != null) {
            shortcutManager.onApplicationActive(packageName, userId);
        }
    }
}
