package android.view;

import com.android.internal.telephony.RILConstants;
import com.huawei.android.statistical.StatisticalConstant;
import com.huawei.pgmng.log.LogPower;

public class SoundEffectConstants {
    public static final int CLICK = 0;
    public static final int NAVIGATION_DOWN = 4;
    public static final int NAVIGATION_LEFT = 1;
    public static final int NAVIGATION_RIGHT = 3;
    public static final int NAVIGATION_UP = 2;

    private SoundEffectConstants() {
    }

    public static int getContantForFocusDirection(int direction) {
        switch (direction) {
            case NAVIGATION_LEFT /*1*/:
            case StatisticalConstant.TYPE_MULTIWINDOW_BUBBLE_MENU_CLOSE /*33*/:
                return NAVIGATION_UP;
            case NAVIGATION_UP /*2*/:
            case LogPower.END_CHG_ROTATION /*130*/:
                return NAVIGATION_DOWN;
            case StatisticalConstant.TYPE_TRIKEY_RIGHT_RECENT /*17*/:
                return NAVIGATION_LEFT;
            case RILConstants.RIL_REQUEST_QUERY_AVAILABLE_BAND_MODE /*66*/:
                return NAVIGATION_RIGHT;
            default:
                throw new IllegalArgumentException("direction must be one of {FOCUS_UP, FOCUS_DOWN, FOCUS_LEFT, FOCUS_RIGHT, FOCUS_FORWARD, FOCUS_BACKWARD}.");
        }
    }
}
