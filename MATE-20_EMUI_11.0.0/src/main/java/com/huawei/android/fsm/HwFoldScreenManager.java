package com.huawei.android.fsm;

import android.hardware.display.HwFoldScreenState;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Singleton;
import com.huawei.android.fsm.IHwFoldScreenManager;

public final class HwFoldScreenManager {
    public static final int DISPLAY_MODE_COORDINATION = 4;
    public static final int DISPLAY_MODE_FULL = 1;
    public static final int DISPLAY_MODE_MAIN = 2;
    public static final int DISPLAY_MODE_SUB = 3;
    public static final int DISPLAY_MODE_UNKNOWN = 0;
    public static final int FOLD_STATE_EXPAND = 1;
    public static final int FOLD_STATE_FOLDED = 2;
    public static final int FOLD_STATE_HALF_FOLDED = 3;
    public static final int FOLD_STATE_UNKNOWN = 0;
    public static final String HWFOLD_SCREEN_SERVICE = "fold_screen";
    private static final Singleton<IHwFoldScreenManager> IHW_FOLD_SCREEN_MANAGER_SINGLETON = new Singleton<IHwFoldScreenManager>() {
        /* class com.huawei.android.fsm.HwFoldScreenManager.AnonymousClass1 */

        /* access modifiers changed from: protected */
        @Override // android.util.Singleton
        public IHwFoldScreenManager create() {
            return IHwFoldScreenManager.Stub.asInterface(ServiceManager.getService(HwFoldScreenManager.HWFOLD_SCREEN_SERVICE));
        }
    };
    private static final boolean IS_FOLDABLE = (!SystemProperties.get("ro.config.hw_fold_disp").isEmpty() || !SystemProperties.get(HwFoldScreenState.DEBUG_HW_FOLD_DISP_PROP).isEmpty());
    public static final String KEY_TIPS_INT_DISPLAY_MODE = "KEY_TIPS_INT_DISPLAY_MODE";
    public static final String KEY_TIPS_INT_REMOVED_REASON = "KEY_TIPS_INT_REMOVED_REASON";
    public static final String KEY_TIPS_INT_VIEW_TYPE = "KEY_TIPS_INT_VIEW_TYPE";
    public static final String KEY_TIPS_STR_CALLER_NAME = "KEY_TIPS_STR_CALLER_NAME";
    public static final String KEY_TIPS_STR_CAMERA_ID = "KEY_TIPS_STR_CAMERA_ID";
    public static final String KEY_TIPS_TEXT = "KEY_TIPS_TEXT";
    public static final int LISTENER_TYPE_FOLD = 1;
    public static final int LISTENER_TYPE_POSTURE = 2;
    public static final int LISTENER_TYPE_UNKNOWN = 0;
    public static final int POSTURE_FOLDED = 103;
    public static final int POSTURE_FULL = 109;
    public static final int POSTURE_HALF_FOLDED = 106;
    public static final int POSTURE_HANDHELD_FOLDED_MAIN = 104;
    public static final int POSTURE_HANDHELD_FOLDED_SUB = 105;
    public static final int POSTURE_LAY_FLAT_MAIN_UP = 101;
    public static final int POSTURE_LAY_FLAT_SUB_UP = 102;
    public static final int POSTURE_OTHER = 199;
    public static final int POSTURE_UNKNOWN = 100;
    public static final int REASON_REMOVE_TIPS_FOR_APP_REQUEST = 2;
    public static final int REASON_REMOVE_TIPS_FOR_DISPLAYMODE_CHANGE = 1;
    public static final int REASON_REMOVE_TIPS_FOR_EXCEPTION = 0;
    public static final int REASON_REMOVE_TIPS_FOR_USER_CLOSE = 3;
    public static final int REQ_BROADCAST_TIPS_REMOVED = 4;
    public static final int REQ_REMOVE_TIPS = 1;
    public static final int REQ_SHOW_TIPS = 2;
    public static final int REQ_TIPS_TYPE_UNKNOWN = 0;
    private static final String TAG = "HwFoldScreenManager";
    public static final int TIPS_ID_FOR_CALL = 1;
    public static final int TIPS_ID_FOR_CAMERA = 0;
    public static final int TIPS_ID_FOR_GENERAL = 2;
    public static final int TIPS_TYPE_ANIMATION_FLIP = 1;
    public static final int TIPS_TYPE_DIALOG_FOLD = 0;
    public static final int TIPS_TYPE_DIALOG_INTELLIGENT_FLIP = 2;

    private static IHwFoldScreenManager getService() {
        return IHW_FOLD_SCREEN_MANAGER_SINGLETON.get();
    }

    private static boolean checkFoldScreenService() {
        if (getService() != null) {
            return true;
        }
        Log.e(TAG, "checkFoldScreenService->service is not started yet");
        return false;
    }

    public static boolean isFoldable() {
        return IS_FOLDABLE;
    }

    public static int getPosture() {
        if (!IS_FOLDABLE || !checkFoldScreenService()) {
            return 100;
        }
        try {
            return getService().getPosture();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static int getFoldableState() {
        if (!IS_FOLDABLE || !checkFoldScreenService()) {
            return 0;
        }
        try {
            return getService().getFoldableState();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static void registerFoldableState(IFoldableStateListener listener, int type) {
        if (IS_FOLDABLE && checkFoldScreenService() && type > 0 && type <= 2) {
            try {
                getService().registerFoldableState(listener, type);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public static void unregisterFoldableState(IFoldableStateListener listener) {
        if (IS_FOLDABLE && checkFoldScreenService()) {
            try {
                getService().unregisterFoldableState(listener);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public static int setDisplayMode(int mode) {
        Log.d(TAG, "setDisplayMode mode=" + mode);
        if (!IS_FOLDABLE || !checkFoldScreenService()) {
            return 0;
        }
        try {
            return getService().setDisplayMode(mode);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static int getDisplayMode() {
        if (!IS_FOLDABLE || !checkFoldScreenService()) {
            return 0;
        }
        try {
            return getService().getDisplayMode();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static int lockDisplayMode(int mode) {
        if (!IS_FOLDABLE || !checkFoldScreenService()) {
            return 0;
        }
        try {
            return getService().lockDisplayMode(mode);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static int unlockDisplayMode() {
        if (!IS_FOLDABLE || !checkFoldScreenService()) {
            return 0;
        }
        try {
            return getService().unlockDisplayMode();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static void registerFoldDisplayMode(IFoldDisplayModeListener listener) {
        if (IS_FOLDABLE && checkFoldScreenService()) {
            try {
                getService().registerFoldDisplayMode(listener);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public static void unregisterFoldDisplayMode(IFoldDisplayModeListener listener) {
        if (IS_FOLDABLE && checkFoldScreenService()) {
            try {
                getService().unregisterFoldDisplayMode(listener);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public static int reqShowTipsToFsm(int reqTipsType, Bundle data) {
        if (!IS_FOLDABLE || !checkFoldScreenService()) {
            return 0;
        }
        try {
            return getService().reqShowTipsToFsm(reqTipsType, data);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static void registerFsmTipsRequestListener(IFoldFsmTipsRequestListener listener, int type) {
        if (IS_FOLDABLE && checkFoldScreenService()) {
            try {
                getService().registerFsmTipsRequestListener(listener, type);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public static void unregisterFsmTipsRequestListener(IFoldFsmTipsRequestListener listener) {
        if (IS_FOLDABLE && checkFoldScreenService()) {
            try {
                getService().unregisterFsmTipsRequestListener(listener);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }
}
