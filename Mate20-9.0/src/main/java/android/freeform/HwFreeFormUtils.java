package android.freeform;

import android.graphics.Point;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.HwPCUtils;
import android.util.Log;
import java.util.HashSet;
import java.util.Set;

public final class HwFreeFormUtils {
    public static final int BLUE = 2;
    public static final int BLUE_DIM = 4;
    private static final boolean ENABLED = SystemProperties.getBoolean("ro.config.hw_freeform_enable", false);
    public static final int GRAY = 1;
    private static final boolean IS_FACTORY_MODE = SystemProperties.get("ro.runmode", "normal").equals("factory");
    static final float MAX_DRAG_PERCENT_PHONE = 0.95f;
    static final float MAX_DRAG_PERCENT_TABLET = 0.65f;
    static final float MIN_DRAG_PERCENT_PHONE = 0.54f;
    static final float MIN_DRAG_PERCENT_TABLET = 0.36f;
    public static final int RED = 3;
    private static final String TAG = "HwFreeFormUtils#";
    private static int mFreeformMaxLength = 0;
    private static int mFreeformMinLength = 0;
    private static boolean mIsFreeFormStackVisible = false;
    private static boolean mIsHideAnimator = false;
    public static final Set<String> sExitFreeformActivity = new HashSet();
    public static final Set<String> sHideCaptionActivity = new HashSet();

    static {
        sHideCaptionActivity.add("com.tencent.mm/com.tencent.mm.plugin.scanner.ui.BaseScanUI");
        sHideCaptionActivity.add("com.tencent.mm/com.tencent.mm.plugin.mmsight.ui.SightCaptureUI");
        sExitFreeformActivity.add("com.tencent.mm/.plugin.scanner.ui.BaseScanUI");
        sExitFreeformActivity.add("com.tencent.mm/.plugin.voip.ui.VideoActivity");
        sExitFreeformActivity.add("com.tencent.mm/.plugin.mmsight.ui.SightCaptureUI");
        sExitFreeformActivity.add("com.huawei.camera/.ThirdCamera");
        sExitFreeformActivity.add("com.tencent.mobileqq/com.tencent.av.ui.AVActivity");
    }

    public static final boolean isFreeFormEnable() {
        if (HwPCUtils.isPcCastModeInServer()) {
            return false;
        }
        return ENABLED;
    }

    public static int log(String subTag, String msg) {
        if (!isFreeFormEnable()) {
            return 0;
        }
        String tag = TAG;
        if (!TextUtils.isEmpty(subTag)) {
            tag = tag + subTag;
        }
        return Log.i(tag, msg);
    }

    public static String intToColor(int touchingState) {
        if (touchingState == 1) {
            return "gray";
        }
        if (touchingState == 2) {
            return "blue";
        }
        if (touchingState == 3) {
            return "red";
        }
        return "unkown";
    }

    public static boolean isTablet() {
        return "tablet".equals(SystemProperties.get("ro.build.charateristics"));
    }

    public static void computeFreeFormSize(Point MaxVisibleSize) {
        if (isFreeFormEnable() && mFreeformMaxLength == 0 && mFreeformMinLength == 0) {
            int minLength = 0;
            if (!(MaxVisibleSize.x == 0 || MaxVisibleSize.y == 0)) {
                minLength = MaxVisibleSize.x > MaxVisibleSize.y ? MaxVisibleSize.y : MaxVisibleSize.x;
            }
            if (isTablet()) {
                mFreeformMaxLength = (int) (((float) minLength) * MAX_DRAG_PERCENT_TABLET);
                mFreeformMinLength = (int) (((float) minLength) * MIN_DRAG_PERCENT_TABLET);
            } else {
                mFreeformMaxLength = (int) (((float) minLength) * MAX_DRAG_PERCENT_PHONE);
                mFreeformMinLength = (int) (((float) minLength) * MIN_DRAG_PERCENT_PHONE);
            }
            log("computeFreeFormSize", "MaxLength = " + mFreeformMaxLength + ",MinLength = " + mFreeformMinLength);
        }
    }

    public static int getFreeformMaxLength() {
        return mFreeformMaxLength;
    }

    public static int getFreeformMinLength() {
        return mFreeformMinLength;
    }

    public static void setFreeFormStackVisible(boolean visible) {
        mIsFreeFormStackVisible = visible;
    }

    public static boolean getFreeFormStackVisible() {
        return mIsFreeFormStackVisible;
    }

    public static void setHideAnimator(boolean show) {
        mIsHideAnimator = show;
    }

    public static boolean getHideAnimator() {
        return mIsHideAnimator;
    }
}
