package com.huawei.android.fsm;

import android.os.Bundle;

public abstract class HwFoldScreenManagerInternal {
    public static final int DISPLAY_MODE_COORDINATION = 4;
    public static final int DISPLAY_MODE_FULL = 1;
    public static final int DISPLAY_MODE_INNER_REFRESH = 6;
    public static final int DISPLAY_MODE_MAIN = 2;
    public static final int DISPLAY_MODE_OUTER_REFRESH = 5;
    public static final int DISPLAY_MODE_SUB = 3;
    public static final int DISPLAY_MODE_UNKNOWN = 0;
    public static final int FOLD_STATE_EXPAND = 1;
    public static final int FOLD_STATE_FOLDED = 2;
    public static final int FOLD_STATE_HALF_FOLDED = 3;
    public static final int FOLD_STATE_UNKNOWN = 0;
    public static final String KEY_PRIORITY_FOLD_TIPS = "KEY_PRIORITY_FOLD_TIPS";
    public static final String KEY_TIPS_INT_DISPLAY_MODE = "KEY_TIPS_INT_DISPLAY_MODE";
    public static final String KEY_TIPS_INT_REMOVED_REASON = "KEY_TIPS_INT_REMOVED_REASON";
    public static final String KEY_TIPS_INT_VIEW_TYPE = "KEY_TIPS_INT_VIEW_TYPE";
    public static final String KEY_TIPS_STR_CALLER_NAME = "KEY_TIPS_STR_CALLER_NAME";
    public static final String KEY_TIPS_STR_CAMERA_ID = "KEY_TIPS_STR_CAMERA_ID";
    public static final String KEY_TIPS_TEXT = "KEY_TIPS_TEXT";
    public static final int POSTURE_AGINGTEST_INNER_REFRESH = 111;
    public static final int POSTURE_AGINGTEST_OUTER_REFRESH = 110;
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
    public static final int PRIORITY_FOLD_TIPS_AUDIO = 7;
    public static final int PRIORITY_FOLD_TIPS_CALL = 9;
    public static final int PRIORITY_FOLD_TIPS_FRONT_CAMERA = 10;
    public static final int PRIORITY_FOLD_TIPS_OTHER_CALL = 8;
    public static final int PRIORITY_FOLD_TIPS_UNKNOW = 0;
    public static final int REASON_REMOVE_TIPS_FOR_APP_REQUEST = 2;
    public static final int REASON_REMOVE_TIPS_FOR_DISPLAYMODE_CHANGE = 1;
    public static final int REASON_REMOVE_TIPS_FOR_EXCEPTION = 0;
    public static final int REASON_REMOVE_TIPS_FOR_USER_CLOSE = 3;
    public static final int REQ_BROADCAST_TIPS_REMOVED = 4;
    public static final int REQ_REMOVE_TIPS = 1;
    public static final int REQ_SHOW_TIPS = 2;
    public static final int REQ_TIPS_TYPE_UNKNOWN = 0;
    public static final int TIPS_ID_FOR_CALL = 1;
    public static final int TIPS_ID_FOR_CAMERA = 0;
    public static final int TIPS_ID_FOR_GENERAL = 2;
    public static final int TIPS_TYPE_ANIMATION_FLIP = 1;
    public static final int TIPS_TYPE_DIALOG_FOLD = 0;
    public static final int TIPS_TYPE_DIALOG_INTELLIGENT_FLIP = 2;

    public interface FoldScreenOnListener {
        void onFoldScreenOn(int i, int i2);
    }

    public interface ScreenOnUnblockerCallback {
        void onScreenOnUnblocker();
    }

    public abstract boolean foldScreenTurningOn(FoldScreenOnListener foldScreenOnListener);

    public abstract int getDisplayMode();

    public abstract int getFoldableState();

    public abstract boolean getInfoDrawWindow();

    public abstract int getPosture();

    public abstract void handleDrawWindow();

    public abstract boolean isPausedDispModeChange();

    public abstract int lockDisplayMode(int i);

    public abstract void notifyLowTempWarning(int i);

    public abstract void notifyScreenOn();

    public abstract void notifyScreenOnFinished();

    public abstract void notifySleep();

    public abstract void onDoubleClick(boolean z, Bundle bundle);

    public abstract boolean onSetFoldDisplayModeFinished(int i, int i2);

    public abstract void pauseDispModeChange();

    public abstract void prepareWakeup(int i);

    public abstract boolean registerScreenOnUnBlockerCallback(ScreenOnUnblockerCallback screenOnUnblockerCallback);

    public abstract int reqShowTipsToFsm(int i, Bundle bundle);

    public abstract void resetInfoDrawWindow();

    public abstract void resumeDispModeChange();

    public abstract int setDisplayMode(int i);

    public abstract boolean shouldChangeDisplayMode();

    public abstract void startDawnAnimaiton();

    public abstract int unlockDisplayMode();
}
