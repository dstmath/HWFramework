package com.huawei.android.fsm;

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
    private static final Singleton<IHwFoldScreenManager> IHwFoldScreenManagerSingleton = new Singleton<IHwFoldScreenManager>() {
        /* access modifiers changed from: protected */
        public IHwFoldScreenManager create() {
            return IHwFoldScreenManager.Stub.asInterface(ServiceManager.getService(HwFoldScreenManager.HWFOLD_SCREEN_SERVICE));
        }
    };
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
    private static final String TAG = "HwFoldScreenManager";
    private static final boolean mIsFoldable = (!SystemProperties.get("ro.config.hw_fold_disp").isEmpty() || !SystemProperties.get("persist.sys.fold.disp.size").isEmpty());

    private static IHwFoldScreenManager getService() {
        return (IHwFoldScreenManager) IHwFoldScreenManagerSingleton.get();
    }

    private static boolean checkFoldScreenService() {
        if (getService() != null) {
            return true;
        }
        Log.e(TAG, "checkFoldScreenService->service is not started yet");
        return false;
    }

    public static boolean isFoldable() {
        return mIsFoldable;
    }

    public static int getPosture() {
        if (!mIsFoldable || !checkFoldScreenService()) {
            return 100;
        }
        try {
            return getService().getPosture();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static int getFoldableState() {
        if (!mIsFoldable || !checkFoldScreenService()) {
            return 0;
        }
        try {
            return getService().getFoldableState();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static void registerFoldableState(IFoldableStateListener listener, int type) {
        if (mIsFoldable && checkFoldScreenService() && type > 0 && type <= 2) {
            try {
                getService().registerFoldableState(listener, type);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public static void unregisterFoldableState(IFoldableStateListener listener) {
        if (mIsFoldable && checkFoldScreenService()) {
            try {
                getService().unregisterFoldableState(listener);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public static int setDisplayMode(int mode) {
        Log.d(TAG, "setDisplayMode mode=" + mode);
        if (!mIsFoldable || !checkFoldScreenService()) {
            return 0;
        }
        try {
            return getService().setDisplayMode(mode);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static int getDisplayMode() {
        if (!mIsFoldable || !checkFoldScreenService()) {
            return 0;
        }
        try {
            return getService().getDisplayMode();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static int lockDisplayMode(int mode) {
        if (!mIsFoldable || !checkFoldScreenService()) {
            return 0;
        }
        try {
            return getService().lockDisplayMode(mode);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static int unlockDisplayMode() {
        if (!mIsFoldable || !checkFoldScreenService()) {
            return 0;
        }
        try {
            return getService().unlockDisplayMode();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static void registerFoldDisplayMode(IFoldDisplayModeListener listener) {
        if (mIsFoldable && checkFoldScreenService()) {
            try {
                getService().registerFoldDisplayMode(listener);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public static void unregisterFoldDisplayMode(IFoldDisplayModeListener listener) {
        if (mIsFoldable && checkFoldScreenService()) {
            try {
                getService().unregisterFoldDisplayMode(listener);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }
}
