package com.android.server.emcom.grabservice;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.text.TextUtils.SimpleStringSplitter;
import android.util.Log;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AutoGrabTools {
    private static final String DEFAULT_STRING = "";
    private static final char ENABLED_ACCESSIBILITY_SERVICES_SEPARATOR = ':';
    private static final String TAG = "GrabService";
    private static final char[] hexDigits = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    private static KeyguardLock mLock;

    public static int byteArrayToInt(byte[] bytes) {
        return ((((bytes[0] & 255) << 24) | ((bytes[1] & 255) << 16)) | ((bytes[2] & 255) << 8)) | (bytes[3] & 255);
    }

    public static String unicode2String(String unicode) {
        if (TextUtils.isEmpty(unicode)) {
            return "";
        }
        StringBuffer string = new StringBuffer();
        String[] hex = unicode.split("\\\\u");
        int i = 1;
        while (i < hex.length) {
            try {
                string.append((char) Integer.parseInt(hex[i], 16));
                i++;
            } catch (NumberFormatException e) {
                Log.e(TAG, "number format error.");
                return "";
            }
        }
        return string.toString();
    }

    public static byte[] intToByteArray(int i) {
        return new byte[]{(byte) ((i >> 24) & 255), (byte) ((i >> 16) & 255), (byte) ((i >> 8) & 255), (byte) (i & 255)};
    }

    public static String byte2HexString(byte b) {
        return new String(new char[]{hexDigits[(b & 240) >> 4], hexDigits[b & 15]});
    }

    public static String getTopActivity(Context context) {
        List<RunningTaskInfo> runningTaskInfos = ((ActivityManager) context.getSystemService("activity")).getRunningTasks(1);
        if (runningTaskInfos != null) {
            return ((RunningTaskInfo) runningTaskInfos.get(0)).topActivity.getClassName();
        }
        return "";
    }

    public static boolean unlockDevice(Context context) {
        boolean result = false;
        PowerManager pm = (PowerManager) context.getSystemService("power");
        if (pm == null) {
            Log.e(TAG, "get PowerManager error");
            return false;
        }
        WakeLock wl = pm.newWakeLock(268435462, "bright");
        KeyguardManager km = (KeyguardManager) context.getSystemService("keyguard");
        if (km == null) {
            Log.e(TAG, "get KeyguardManager error");
            return false;
        }
        if (!pm.isInteractive()) {
            wl.acquire(3000);
            result = true;
        }
        if (km.isKeyguardLocked()) {
            Log.d(TAG, "disable Keyguard");
            mLock = km.newKeyguardLock("unLock");
            mLock.disableKeyguard();
            result = true;
        }
        return result;
    }

    public static boolean lockDevice(Context context) {
        boolean result = false;
        if (mLock != null) {
            Log.d(TAG, "reenable Keyguard");
            mLock.reenableKeyguard();
            result = true;
        }
        PowerManager pm = (PowerManager) context.getSystemService("power");
        mLock = null;
        pm.goToSleep(SystemClock.uptimeMillis());
        return result;
    }

    public static Set<ComponentName> getEnabledServicesFromSettings(Context context) {
        String enabledServicesSetting = Secure.getString(context.getContentResolver(), "enabled_accessibility_services");
        if (enabledServicesSetting == null) {
            return Collections.emptySet();
        }
        Set<ComponentName> enabledServices = new HashSet();
        SimpleStringSplitter colonSplitter = new SimpleStringSplitter(ENABLED_ACCESSIBILITY_SERVICES_SEPARATOR);
        colonSplitter.setString(enabledServicesSetting);
        while (colonSplitter.hasNext()) {
            ComponentName enabledService = ComponentName.unflattenFromString(colonSplitter.next());
            if (enabledService != null) {
                enabledServices.add(enabledService);
            }
        }
        return enabledServices;
    }

    public static boolean setAccessibilityServiceEnable(Context context, String componentName, boolean enabled) {
        boolean isEnabledServiceChanged;
        Set<ComponentName> enabledServices = getEnabledServicesFromSettings(context);
        if (enabledServices == Collections.emptySet()) {
            enabledServices = new HashSet();
        }
        ComponentName multiScreenShotService = ComponentName.unflattenFromString(componentName);
        if (enabled) {
            isEnabledServiceChanged = enabledServices.add(multiScreenShotService);
        } else {
            isEnabledServiceChanged = enabledServices.remove(multiScreenShotService);
        }
        if (!isEnabledServiceChanged) {
            return false;
        }
        StringBuilder enabledServicesBuilder = new StringBuilder();
        for (ComponentName enabledService : enabledServices) {
            enabledServicesBuilder.append(enabledService.flattenToString());
            enabledServicesBuilder.append(ENABLED_ACCESSIBILITY_SERVICES_SEPARATOR);
        }
        int enabledServicesBuilderLength = enabledServicesBuilder.length();
        if (enabledServicesBuilderLength > 0) {
            enabledServicesBuilder.deleteCharAt(enabledServicesBuilderLength - 1);
        }
        Secure.putString(context.getContentResolver(), "enabled_accessibility_services", enabledServicesBuilder.toString());
        Secure.putInt(context.getContentResolver(), "accessibility_enabled", 1);
        return true;
    }

    public static String[] list2Array(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        int size = list.size();
        String[] reslut = new String[size];
        for (int i = 0; i < size; i++) {
            reslut[i] = (String) list.get(i);
        }
        return reslut;
    }

    public static void backToHome(Context context) {
        Intent i = new Intent("android.intent.action.MAIN");
        i.setFlags(270533120);
        i.addCategory("android.intent.category.HOME");
        context.startActivity(i);
    }
}
