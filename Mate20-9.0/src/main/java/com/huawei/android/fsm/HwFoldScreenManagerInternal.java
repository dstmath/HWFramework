package com.huawei.android.fsm;

import android.graphics.Point;
import android.os.Bundle;

public abstract class HwFoldScreenManagerInternal {
    public static final int DISPLAY_MODE_COORDINATION = 4;
    public static final int DISPLAY_MODE_FULL = 1;
    public static final int DISPLAY_MODE_MAIN = 2;
    public static final int DISPLAY_MODE_SUB = 3;
    public static final int DISPLAY_MODE_UNKNOWN = 0;
    public static final int FOLD_STATE_EXPAND = 1;
    public static final int FOLD_STATE_FOLDED = 2;
    public static final int FOLD_STATE_HALF_FOLDED = 3;
    public static final int FOLD_STATE_UNKNOWN = 0;
    public static final int POSTURE_FOLDED = 103;
    public static final int POSTURE_FULCRUM_MAIN = -1;
    public static final int POSTURE_FULCRUM_SUB = -1;
    public static final int POSTURE_FULL = 109;
    public static final int POSTURE_HALF_FOLDED = 106;
    public static final int POSTURE_HANDHELD_FOLDED = -1;
    public static final int POSTURE_HANDHELD_FOLDED_MAIN = 104;
    public static final int POSTURE_HANDHELD_FOLDED_SUB = 105;
    public static final int POSTURE_HANDHELD_HALF_FOLD = -1;
    public static final int POSTURE_LAY_FLAT_MAIN_UP = 101;
    public static final int POSTURE_LAY_FLAT_SUB_UP = 102;
    public static final int POSTURE_OTHER = 199;
    public static final int POSTURE_TWO_FULCRUM_LANDSCAPE = -1;
    public static final int POSTURE_TWO_FULCRUM_PORTRAIT = -1;
    public static final int POSTURE_UNKNOWN = 100;

    public abstract int doubleClickToSetDisplayMode(Point point);

    public abstract int getDisplayMode();

    public abstract int getFoldableState();

    public abstract int getPosture();

    public abstract int lockDisplayMode(int i);

    public abstract void notifyFlip();

    public abstract void notifySleep();

    public abstract void prepareWakeup(int i, Bundle bundle);

    public abstract int setDisplayMode(int i);

    public abstract void startWakeup(int i, Bundle bundle);

    public abstract int unlockDisplayMode();
}
