package huawei.android.provider;

import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import android.util.Flog;
import huawei.android.app.admin.ConstantValue;

public class FingerSenseSettings {
    public static final String INTENT_EXTRA_STARTFLG = "startflg";
    public static final String INTENT_EXTRA_STARTFLG_EASYWAKEUP = "easywakeup";
    private static final String MULTISCREENSHOT_ACTION = "com.huawei.HwMultiScreenShot.start";
    private static final String MULTISCREENSHOT_PREFIX = "com.huawei.HwMultiScreenShot";
    private static final String MULTISCREENSHOT_SERVICE = "com.huawei.HwMultiScreenShot.MultiScreenShotService";
    public static final String SCREENSHOT_REGION_INTENT = "com.qeexo.smartshot.CropActivity";
    private static boolean isDrawGestureEnabled;
    private static boolean isLineGestureEnabled;
    private static boolean isSmartshotEnabled;
    private static String mRunmode;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: huawei.android.provider.FingerSenseSettings.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: huawei.android.provider.FingerSenseSettings.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: huawei.android.provider.FingerSenseSettings.<clinit>():void");
    }

    public static synchronized void updateSmartshotEnabled(ContentResolver resolver) {
        boolean z = true;
        synchronized (FingerSenseSettings.class) {
            if (System.getIntForUser(resolver, HwSettings.System.FINGERSENSE_SMARTSHOT_ENABLED, 1, ActivityManager.getCurrentUser()) != 1) {
                z = false;
            }
            isSmartshotEnabled = z;
            Flog.i(ConstantValue.transaction_isRooted, "updateSmartshotEnabled set to " + isSmartshotEnabled);
            updateFingerSenseEnable(resolver);
        }
    }

    public static synchronized void updateLineGestureEnabled(ContentResolver resolver) {
        boolean z = true;
        synchronized (FingerSenseSettings.class) {
            if (System.getIntForUser(resolver, HwSettings.System.FINGERSENSE_LINE_GESTURE_ENABLED, 1, ActivityManager.getCurrentUser()) != 1) {
                z = false;
            }
            isLineGestureEnabled = z;
            Flog.i(ConstantValue.transaction_isRooted, "updateLineGestureEnabled set to " + isLineGestureEnabled);
            updateFingerSenseEnable(resolver);
        }
    }

    public static synchronized void updateDrawGestureEnabled(ContentResolver resolver) {
        boolean z = true;
        synchronized (FingerSenseSettings.class) {
            if (System.getIntForUser(resolver, HwSettings.System.FINGERSENSE_LETTERS_ENABLED, 1, ActivityManager.getCurrentUser()) != 1) {
                z = false;
            }
            isDrawGestureEnabled = z;
            Flog.i(ConstantValue.transaction_isRooted, "updateDrawGestureEnabled set to " + isDrawGestureEnabled);
            updateFingerSenseEnable(resolver);
        }
    }

    public static synchronized void updateFingerSenseEnable(ContentResolver resolver) {
        synchronized (FingerSenseSettings.class) {
            if (isSmartshotEnabled || isDrawGestureEnabled || isLineGestureEnabled) {
                Global.putInt(resolver, HwSettings.System.FINGERSENSE_ENABLED, 1);
            } else {
                Global.putInt(resolver, HwSettings.System.FINGERSENSE_ENABLED, 0);
            }
        }
    }

    public static synchronized boolean isFingerSenseEnabled(ContentResolver resolver) {
        boolean z = true;
        synchronized (FingerSenseSettings.class) {
            if ("factory".equals(mRunmode) || SystemProperties.getBoolean("sys.super_power_save", false)) {
                return false;
            }
            if (Global.getInt(resolver, HwSettings.System.FINGERSENSE_ENABLED, 1) != 1) {
                z = false;
            }
            return z;
        }
    }

    public static boolean isFingerSenseSmartshotEnabled(ContentResolver resolver) {
        return System.getIntForUser(resolver, HwSettings.System.FINGERSENSE_SMARTSHOT_ENABLED, 1, ActivityManager.getCurrentUser()) == 1;
    }

    public static boolean isFingerSenseDoubleKnuckleEnabled(ContentResolver resolver) {
        return System.getIntForUser(resolver, HwSettings.System.FINGERSENSE_DOUBLE_KNUCKLE_ENABLED, 1, ActivityManager.getCurrentUser()) == 1;
    }

    public static boolean areDrawGesturesEnabled(ContentResolver resolver) {
        return System.getIntForUser(resolver, HwSettings.System.FINGERSENSE_LETTERS_ENABLED, 1, ActivityManager.getCurrentUser()) == 1;
    }

    public static boolean isKnuckleGestureEnable(String gestureName, ContentResolver resolver) {
        if (gestureName.equals(HwSettings.System.FINGERSENSE_KNUCKLE_GESTURE_REGION_SUFFIX)) {
            return isFingerSenseSmartshotEnabled(resolver);
        }
        if (areDrawGesturesEnabled(resolver)) {
            return isValidLetterGestureAppInfo(getLetterGestureAppInfo(gestureName, resolver));
        }
        return false;
    }

    public static boolean isFingerSenseLineGestureEnabled(ContentResolver resolver) {
        return System.getIntForUser(resolver, HwSettings.System.FINGERSENSE_LINE_GESTURE_ENABLED, 1, ActivityManager.getCurrentUser()) == 1;
    }

    private static boolean isValidLetterGestureAppInfo(String[] appInfo) {
        boolean z = false;
        if (appInfo == null || appInfo.length != 2) {
            return false;
        }
        if (!(appInfo[0].equals("null") || appInfo[1].equals("null"))) {
            z = true;
        }
        return z;
    }

    private static String[] getLetterGestureAppInfo(String gestureName, ContentResolver resolver) {
        String gestureValue = System.getStringForUser(resolver, HwSettings.System.EASYFINGER_LETTER_SETTING_PREFIX + gestureName, ActivityManager.getCurrentUser());
        if (gestureValue == null) {
            return new String[0];
        }
        return gestureValue.split(";");
    }

    public static Intent getIntentForGesture(String gestureName, Context context) {
        if (gestureName.equals(HwSettings.System.FINGERSENSE_KNUCKLE_GESTURE_REGION_SUFFIX)) {
            return new Intent(SCREENSHOT_REGION_INTENT);
        }
        String[] appInfo = getLetterGestureAppInfo(gestureName, context.getContentResolver());
        if (!isValidLetterGestureAppInfo(appInfo)) {
            return null;
        }
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.LAUNCHER");
        intent.setClassName(appInfo[0], appInfo[1]);
        intent.putExtra(INTENT_EXTRA_STARTFLG, INTENT_EXTRA_STARTFLG_EASYWAKEUP);
        return intent;
    }

    public static Intent getIntentForMultiScreenShot(Context context) {
        Intent startIntent = new Intent(MULTISCREENSHOT_ACTION);
        startIntent.setClassName(MULTISCREENSHOT_PREFIX, MULTISCREENSHOT_SERVICE);
        return startIntent;
    }
}
