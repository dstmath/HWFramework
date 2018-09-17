package android.rog;

import android.os.RemoteException;
import android.os.ServiceManager;
import android.rog.IHwRogManager.Stub;
import android.util.Slog;
import android.view.SurfaceControl;
import java.util.List;

public class HwRogManager implements IRogManager {
    private static final String TAG = "HwRogManager";
    private static HwRogManager sInstance;
    private IHwRogManager mService;

    private HwRogManager() {
    }

    private boolean checkRogManager() {
        if (this.mService == null) {
            this.mService = Stub.asInterface(ServiceManager.getService("rog_service"));
        }
        if (this.mService == null) {
            return false;
        }
        return true;
    }

    public static synchronized HwRogManager getDefault() {
        HwRogManager hwRogManager;
        synchronized (HwRogManager.class) {
            if (sInstance == null) {
                sInstance = new HwRogManager();
            }
            hwRogManager = sInstance;
        }
        return hwRogManager;
    }

    public boolean registerRogListener(IHwRogListener listener) {
        if (!checkRogManager() || listener == null) {
            return false;
        }
        boolean rogEnable = false;
        try {
            rogEnable = this.mService.registerRogListener(listener, listener.getPackageName());
        } catch (RemoteException ex) {
            Slog.w(TAG, "registerRogListener,ex:" + ex);
        }
        return rogEnable;
    }

    public void unRegisterRogListener(IHwRogListener listener) {
        if (checkRogManager()) {
            try {
                this.mService.unRegisterRogListener(listener);
            } catch (RemoteException ex) {
                Slog.w(TAG, "unRegisterRogListener,ex:" + ex);
            }
        }
    }

    public void setRogSwitchState(boolean open) {
        if (checkRogManager()) {
            try {
                this.mService.setRogSwitchState(open);
            } catch (RemoteException ex) {
                Slog.w(TAG, "setRogSwitchState,ex:" + ex);
            }
        }
    }

    public boolean getRogSwitchState() {
        if (!checkRogManager()) {
            return false;
        }
        boolean result = false;
        try {
            result = this.mService.getRogSwitchState();
        } catch (RemoteException ex) {
            Slog.w(TAG, "getRogSwitchState,ex:" + ex);
        }
        return result;
    }

    public AppRogInfo getOwnAppRogInfo(IHwRogListener listener) {
        if (!checkRogManager()) {
            return null;
        }
        AppRogInfo info = null;
        try {
            info = this.mService.getOwnAppRogInfo(listener);
        } catch (RemoteException ex) {
            Slog.w(TAG, "getOwnAppRogInfo,ex:" + ex);
        }
        return info;
    }

    public AppRogInfo getSpecifiedAppRogInfo(String packageName) {
        if (!checkRogManager()) {
            return null;
        }
        AppRogInfo info = null;
        try {
            info = this.mService.getSpecifiedAppRogInfo(packageName);
        } catch (RemoteException ex) {
            Slog.w(TAG, "getSpecifiedAppRogInfo,ex:" + ex);
        }
        return info;
    }

    public List<AppRogInfo> getAppRogInfos() {
        if (!checkRogManager()) {
            return null;
        }
        List<AppRogInfo> result = null;
        try {
            result = this.mService.getAppRogInfos();
        } catch (RemoteException ex) {
            Slog.w(TAG, "getAppRogInfos,ex:" + ex);
        }
        return result;
    }

    public List<AppRogInfo> updateBatchAppRogInfo(List<AppRogInfo> newRogInfos) {
        if (!checkRogManager()) {
            return null;
        }
        List<AppRogInfo> finalList = newRogInfos;
        try {
            finalList = this.mService.updateBatchAppRogInfo(newRogInfos);
        } catch (RemoteException ex) {
            Slog.w(TAG, "updateBatchAppRogInfo,ex:" + ex);
        }
        return finalList;
    }

    public AppRogInfo updateAppRogInfo(AppRogInfo newRogInfo) {
        if (!checkRogManager()) {
            return newRogInfo;
        }
        AppRogInfo finalInfo = newRogInfo;
        try {
            finalInfo = this.mService.updateAppRogInfo(newRogInfo);
        } catch (RemoteException ex) {
            Slog.w(TAG, "updateAppRogInfo,ex:" + ex);
        }
        return finalInfo;
    }

    public boolean isRogSupported() {
        int supported = SurfaceControl.isRogSupport();
        Slog.i(TAG, "isRogSupported->supported:" + supported);
        if (supported == 1) {
            return true;
        }
        return false;
    }
}
