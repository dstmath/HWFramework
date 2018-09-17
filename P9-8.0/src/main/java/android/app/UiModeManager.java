package android.app;

import android.app.IUiModeManager.Stub;
import android.content.Context;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.ServiceManager.ServiceNotFoundException;

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
    private IUiModeManager mService = Stub.asInterface(ServiceManager.getServiceOrThrow(Context.UI_MODE_SERVICE));

    UiModeManager() throws ServiceNotFoundException {
    }

    public void enableCarMode(int flags) {
        if (this.mService != null) {
            try {
                this.mService.enableCarMode(flags);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void disableCarMode(int flags) {
        if (this.mService != null) {
            try {
                this.mService.disableCarMode(flags);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public int getCurrentModeType() {
        if (this.mService == null) {
            return 1;
        }
        try {
            return this.mService.getCurrentModeType();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setNightMode(int mode) {
        if (this.mService != null) {
            try {
                this.mService.setNightMode(mode);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public int getNightMode() {
        if (this.mService == null) {
            return -1;
        }
        try {
            return this.mService.getNightMode();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isUiModeLocked() {
        if (this.mService == null) {
            return true;
        }
        try {
            return this.mService.isUiModeLocked();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isNightModeLocked() {
        if (this.mService == null) {
            return true;
        }
        try {
            return this.mService.isNightModeLocked();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }
}
