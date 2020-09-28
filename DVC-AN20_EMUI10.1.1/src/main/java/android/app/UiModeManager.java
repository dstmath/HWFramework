package android.app;

import android.annotation.UnsupportedAppUsage;
import android.app.IUiModeManager;
import android.content.Context;
import android.os.RemoteException;
import android.os.ServiceManager;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class UiModeManager {
    public static String ACTION_ENTER_CAR_MODE = "android.app.action.ENTER_CAR_MODE";
    public static String ACTION_ENTER_DESK_MODE = "android.app.action.ENTER_DESK_MODE";
    public static String ACTION_EXIT_CAR_MODE = "android.app.action.EXIT_CAR_MODE";
    public static String ACTION_EXIT_DESK_MODE = "android.app.action.EXIT_DESK_MODE";
    public static final int DISABLE_CAR_MODE_GO_HOME = 1;
    public static final int ENABLE_CAR_MODE_ALLOW_SLEEP = 2;
    public static final int ENABLE_CAR_MODE_GO_CAR_HOME = 1;
    public static final int MODE_NIGHT_AUTO = 0;
    public static final int MODE_NIGHT_NO = 1;
    public static final int MODE_NIGHT_YES = 2;
    private static final String TAG = "UiModeManager";
    private IUiModeManager mService = IUiModeManager.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.UI_MODE_SERVICE));

    @Retention(RetentionPolicy.SOURCE)
    public @interface NightMode {
    }

    @UnsupportedAppUsage
    UiModeManager() throws ServiceManager.ServiceNotFoundException {
    }

    public void enableCarMode(int flags) {
        IUiModeManager iUiModeManager = this.mService;
        if (iUiModeManager != null) {
            try {
                iUiModeManager.enableCarMode(flags);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void disableCarMode(int flags) {
        IUiModeManager iUiModeManager = this.mService;
        if (iUiModeManager != null) {
            try {
                iUiModeManager.disableCarMode(flags);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public int getCurrentModeType() {
        IUiModeManager iUiModeManager = this.mService;
        if (iUiModeManager == null) {
            return 1;
        }
        try {
            return iUiModeManager.getCurrentModeType();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setNightMode(int mode) {
        IUiModeManager iUiModeManager = this.mService;
        if (iUiModeManager != null) {
            try {
                iUiModeManager.setNightMode(mode);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public int getNightMode() {
        IUiModeManager iUiModeManager = this.mService;
        if (iUiModeManager == null) {
            return -1;
        }
        try {
            return iUiModeManager.getNightMode();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isUiModeLocked() {
        IUiModeManager iUiModeManager = this.mService;
        if (iUiModeManager == null) {
            return true;
        }
        try {
            return iUiModeManager.isUiModeLocked();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isNightModeLocked() {
        IUiModeManager iUiModeManager = this.mService;
        if (iUiModeManager == null) {
            return true;
        }
        try {
            return iUiModeManager.isNightModeLocked();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }
}
