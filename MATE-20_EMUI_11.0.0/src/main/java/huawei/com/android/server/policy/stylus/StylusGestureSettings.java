package huawei.com.android.server.policy.stylus;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.gesture.Gesture;
import java.util.Locale;

public class StylusGestureSettings {
    private static final String ACTION_FLOATSERVICE = "com.huawei.smartshot.floatmode.start";
    private static final String EXTRA_GESTURE = "com.qeexo.syswideactions.gesture";
    private static final String EXTRA_GESTURE_NAME = "com.qeexo.syswideactions.gesture.name";
    private static final String EXTRA_GESTURE_PREDICTION_SCORE = "com.qeexo.syswideactions.gesture.score";
    public static final String FLOAT_ENTRANCE_CLASSNAME = "com.huawei.stylus.floatmenu.FloatMenuService";
    public static final String FLOAT_ENTRANCE_PACKAGE_NAME = "com.huawei.stylus.floatmenu";
    private static final String HUAWEI_SCREENSHOT_REGION_INTENT = "com.huawei.smartshot.CropActivity";
    private static final String MULTISCREENSHOT_ACTION = "com.huawei.HwMultiScreenShot.start";
    private static final String MULTISCREENSHOT_FLOATSERVICE = "com.huawei.smartshot.floatmode.FloatService";
    private static final String MULTISCREENSHOT_PREFIX = "com.huawei.HwMultiScreenShot";
    private static final String MULTISCREENSHOT_SERVICE = "com.huawei.HwMultiScreenShot.MultiScreenShotService";
    public static final String PKG_HUAWEI_SMARTSHOT = "com.huawei.smartshot";
    public static final String PKG_QEEXO_SMARTSHOT = "com.qeexo.smartshot";
    private static final String QEEXO_SCREENSHOT_REGION_INTENT = "com.qeexo.smartshot.CropActivity";
    public static final String STYLUS_GESTURE_C_SUFFIX = "c";
    public static final String STYLUS_GESTURE_M_SUFFIX = "m";
    public static final String STYLUS_GESTURE_REGION_SUFFIX = "region";
    public static final String STYLUS_GESTURE_S_SUFFIX = "s";
    public static final String STYLUS_GESTURE_W_SUFFIX = "w";

    private StylusGestureSettings() {
    }

    private static Intent getIntentForMultiScreenShot() {
        Intent intent = new Intent(MULTISCREENSHOT_ACTION);
        intent.setClassName(MULTISCREENSHOT_PREFIX, MULTISCREENSHOT_SERVICE);
        return intent;
    }

    private static Intent getIntentForCustomGesture(Context context, String gestureName, Gesture gesture, double predictionScore) {
        Intent intent;
        if (checkPackageInstalled(context, PKG_QEEXO_SMARTSHOT)) {
            intent = new Intent(QEEXO_SCREENSHOT_REGION_INTENT);
        } else if (!checkPackageInstalled(context, PKG_HUAWEI_SMARTSHOT)) {
            return null;
        } else {
            intent = new Intent(HUAWEI_SCREENSHOT_REGION_INTENT);
        }
        intent.addFlags(268435456);
        intent.putExtra(EXTRA_GESTURE, gesture);
        intent.putExtra(EXTRA_GESTURE_NAME, gestureName);
        intent.putExtra(EXTRA_GESTURE_PREDICTION_SCORE, predictionScore);
        return intent;
    }

    public static Intent getIntentForStylusGesture(String gestureName, Gesture gesture, double predictionScore, Context context) {
        if (gestureName == null || gesture == null || context == null) {
            return null;
        }
        String gestureSuffix = gestureName.toLowerCase(Locale.ENGLISH);
        if (STYLUS_GESTURE_S_SUFFIX.equals(gestureSuffix)) {
            return getIntentForMultiScreenShot();
        }
        if (STYLUS_GESTURE_REGION_SUFFIX.equals(gestureSuffix)) {
            return getIntentForCustomGesture(context, gestureName, gesture, predictionScore);
        }
        return null;
    }

    public static Intent getFloatServiceIntentForStylusGesture(Gesture gesture) {
        Intent intent = new Intent(ACTION_FLOATSERVICE);
        intent.setClassName(MULTISCREENSHOT_PREFIX, MULTISCREENSHOT_FLOATSERVICE);
        intent.setPackage(MULTISCREENSHOT_PREFIX);
        if (gesture == null) {
            intent.putExtra(ACTION_FLOATSERVICE, "screenshot");
        } else {
            intent.putExtra(ACTION_FLOATSERVICE, "show");
            intent.putExtra(EXTRA_GESTURE, gesture);
        }
        return intent;
    }

    static boolean checkPackageInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getPackageInfo(packageName, 128);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
