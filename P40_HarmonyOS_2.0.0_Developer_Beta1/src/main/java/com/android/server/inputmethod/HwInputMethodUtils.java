package com.android.server.inputmethod;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.Context;
import android.provider.Settings;

public class HwInputMethodUtils {
    private static String SETTINGS_CHOOSE_LOCK_PASSWORD_ACTIVITY = "com.android.settings.password.ChooseLockPassword";
    private static String SETTINGS_CONFIRM_LOCK_PASSWORD_ACTIVITY = "com.android.settings.password.ConfirmLockPassword$InternalActivity";
    public static final String SETTINGS_SECURE_KEYBOARD_CONTROL = "secure_keyboard";
    private static final String TAG = "HwInputMethodManagerUtils";

    public static boolean isNeedSecIMEInSpecialScenes(Context context) {
        Object object = context.getSystemService("keyguard");
        if (!(object instanceof KeyguardManager)) {
            return false;
        }
        boolean isKeyguardLock = false;
        KeyguardManager keyguardManager = (KeyguardManager) object;
        if (keyguardManager.isKeyguardLocked() && keyguardManager.isKeyguardSecure()) {
            isKeyguardLock = true;
        }
        boolean isPasswordSettingActivity = isPasswordSettingsActivity(context);
        if (isKeyguardLock || isPasswordSettingActivity) {
            return true;
        }
        return false;
    }

    private static boolean isPasswordSettingsActivity(Context context) {
        Object object = context.getSystemService("activity");
        if (!(object instanceof ActivityManager)) {
            return false;
        }
        ActivityManager am = (ActivityManager) object;
        if (am.getRunningTasks(1) == null || am.getRunningTasks(1).size() == 0) {
            return false;
        }
        String className = am.getRunningTasks(1).get(0).topActivity.getClassName();
        if (className.contains(SETTINGS_CHOOSE_LOCK_PASSWORD_ACTIVITY) || className.contains(SETTINGS_CONFIRM_LOCK_PASSWORD_ACTIVITY)) {
            return true;
        }
        return false;
    }

    public static boolean isSecureIMEEnable(Context context, int userId) {
        return Settings.Secure.getIntForUser(context.getContentResolver(), SETTINGS_SECURE_KEYBOARD_CONTROL, 1, userId) == 1;
    }
}
