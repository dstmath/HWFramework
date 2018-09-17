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
import com.android.server.display.Utils;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AutoGrabTools {
    private static final String DEFAULT_STRING = "";
    private static final char ENABLED_ACCESSIBILITY_SERVICES_SEPARATOR = ':';
    private static final String TAG = "GrabService";
    private static final char[] hexDigits = null;
    private static KeyguardLock mLock;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.emcom.grabservice.AutoGrabTools.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.emcom.grabservice.AutoGrabTools.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.emcom.grabservice.AutoGrabTools.<clinit>():void");
    }

    public static int byteArrayToInt(byte[] bytes) {
        return ((((bytes[0] & Utils.MAXINUM_TEMPERATURE) << 24) | ((bytes[1] & Utils.MAXINUM_TEMPERATURE) << 16)) | ((bytes[2] & Utils.MAXINUM_TEMPERATURE) << 8)) | (bytes[3] & Utils.MAXINUM_TEMPERATURE);
    }

    public static String unicode2String(String unicode) {
        if (TextUtils.isEmpty(unicode)) {
            return DEFAULT_STRING;
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
                return DEFAULT_STRING;
            }
        }
        return string.toString();
    }

    public static byte[] intToByteArray(int i) {
        return new byte[]{(byte) ((i >> 24) & Utils.MAXINUM_TEMPERATURE), (byte) ((i >> 16) & Utils.MAXINUM_TEMPERATURE), (byte) ((i >> 8) & Utils.MAXINUM_TEMPERATURE), (byte) (i & Utils.MAXINUM_TEMPERATURE)};
    }

    public static String byte2HexString(byte b) {
        return new String(new char[]{hexDigits[(b & 240) >> 4], hexDigits[b & 15]});
    }

    public static String getTopActivity(Context context) {
        List<RunningTaskInfo> runningTaskInfos = ((ActivityManager) context.getSystemService("activity")).getRunningTasks(1);
        if (runningTaskInfos != null) {
            return ((RunningTaskInfo) runningTaskInfos.get(0)).topActivity.getClassName();
        }
        return DEFAULT_STRING;
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
            mLock = km.newKeyguardLock("unLock");
            mLock.disableKeyguard();
            result = true;
        }
        return result;
    }

    public static boolean lockDevice(Context context) {
        boolean result = false;
        if (mLock != null) {
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
