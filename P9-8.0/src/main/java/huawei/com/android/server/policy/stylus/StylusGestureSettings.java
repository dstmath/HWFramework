package huawei.com.android.server.policy.stylus;

import android.content.Intent;
import android.gesture.Gesture;

public class StylusGestureSettings {
    private static final String EXTRA_GESTURE = "com.qeexo.syswideactions.gesture";
    private static final String EXTRA_GESTURE_NAME = "com.qeexo.syswideactions.gesture.name";
    private static final String EXTRA_GESTURE_PREDICTION_SCORE = "com.qeexo.syswideactions.gesture.score";
    public static final String FLOAT_ENTRANCE_CLASSNAME = "com.huawei.stylus.floatmenu.FloatMenuService";
    public static final String FLOAT_ENTRANCE_PACKAGE_NAME = "com.huawei.stylus.floatmenu";
    private static final String MULTISCREENSHOT_ACTION = "com.huawei.HwMultiScreenShot.start";
    private static final String MULTISCREENSHOT_PREFIX = "com.huawei.HwMultiScreenShot";
    private static final String MULTISCREENSHOT_SERVICE = "com.huawei.HwMultiScreenShot.MultiScreenShotService";
    private static final String SCREENSHOT_REGION_INTENT = "com.qeexo.smartshot.CropActivity";
    public static final String STYLUS_GESTURE_C_SUFFIX = "c";
    public static final String STYLUS_GESTURE_M_SUFFIX = "m";
    public static final String STYLUS_GESTURE_REGION_SUFFIX = "region";
    public static final String STYLUS_GESTURE_S_SUFFIX = "s";
    public static final String STYLUS_GESTURE_W_SUFFIX = "w";

    private static Intent getIntentForMultiScreenShot() {
        Intent intent = new Intent(MULTISCREENSHOT_ACTION);
        intent.setClassName(MULTISCREENSHOT_PREFIX, MULTISCREENSHOT_SERVICE);
        return intent;
    }

    private static Intent getIntentForCustomGesture(String gestureName, Gesture gesture, double predictionScore) {
        Intent intent = new Intent(SCREENSHOT_REGION_INTENT);
        intent.addFlags(268435456);
        intent.putExtra("com.qeexo.syswideactions.gesture", gesture);
        intent.putExtra("com.qeexo.syswideactions.gesture.name", gestureName);
        intent.putExtra("com.qeexo.syswideactions.gesture.score", predictionScore);
        return intent;
    }

    public static Intent getIntentForStylusGesture(String gestureName, Gesture gesture, double predictionScore) {
        if (gestureName != null) {
            String gestureSuffix = gestureName.toLowerCase();
            if (STYLUS_GESTURE_S_SUFFIX.equals(gestureSuffix)) {
                return getIntentForMultiScreenShot();
            }
            if (STYLUS_GESTURE_REGION_SUFFIX.equals(gestureSuffix)) {
                return getIntentForCustomGesture(gestureName, gesture, predictionScore);
            }
        }
        return null;
    }
}
