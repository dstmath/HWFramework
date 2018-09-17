package android.vr;

import android.content.Context;
import android.hardware.hivrar.IHivrarService;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Slog;
import android.vr.IVRManagerService.Stub;

public class VRManagerService extends Stub {
    private static final boolean LOCAL_DEBUG = false;
    private static final String TAG = "VRManagerService";
    private Context mContext;
    private IHivrarService mHivrarService;
    private final Object mLock = new Object();

    public VRManagerService(Context context) {
        this.mContext = context;
    }

    public IHivrarService getHivrarService() {
        Slog.d(TAG, "getHivrarService");
        connectHivrarServiceLocked();
        if (this.mHivrarService == null) {
            Slog.e(TAG, "mHivrarService service is unavailable");
        }
        return this.mHivrarService;
    }

    public void resetHivrarService() {
        Slog.d(TAG, "resetHivrarService");
        this.mHivrarService = null;
    }

    private void connectHivrarServiceLocked() {
        if (this.mHivrarService != null) {
            Slog.d(TAG, "mHivrarService is not null");
            return;
        }
        Slog.d(TAG, "connectHivrarServiceLocked");
        IBinder vrarServiceBinder = ServiceManager.getService("hisi.vrar");
        if (vrarServiceBinder == null) {
            Slog.e(TAG, "ServiceManager.getService failure");
            return;
        }
        this.mHivrarService = IHivrarService.Stub.asInterface(vrarServiceBinder);
        Slog.d(TAG, "connectHivrarServiceLocked success!");
    }

    public double getVsync() {
        Slog.d(TAG, "getVsync");
        synchronized (this.mLock) {
            this.mContext.enforceCallingOrSelfPermission("com.huawei.android.permission.VR", null);
            IHivrarService hivrarService = getHivrarService();
            if (hivrarService == null) {
                Slog.e(TAG, "hivrarService service is unavailable");
                return -1.0d;
            }
            double vsync = 0.0d;
            try {
                vsync = hivrarService.vrGetVsyncTimeStamp(0);
            } catch (RemoteException e) {
                resetHivrarService();
            }
            Slog.d(TAG, "getCurrentVsync :" + vsync);
            return vsync;
        }
    }

    public boolean startFrontBufferDisplay() {
        Slog.d(TAG, "startFrontBufferDisplay");
        synchronized (this.mLock) {
            this.mContext.enforceCallingOrSelfPermission("com.huawei.android.permission.VR", null);
            IHivrarService hivrarService = getHivrarService();
            if (hivrarService == null) {
                Slog.e(TAG, "hivrarService service is unavailable");
                return false;
            }
            int r = -1;
            try {
                r = hivrarService.vrInit();
            } catch (RemoteException e) {
                Slog.e(TAG, "hivrarService service will be reset");
                resetHivrarService();
            }
            if (r != 0) {
                Slog.e(TAG, "vrInit fail");
                return false;
            }
            Slog.d(TAG, "vrInit success!");
            hivrarService = getHivrarService();
            if (hivrarService == null) {
                Slog.e(TAG, "hivrarService service is unavailable");
                return false;
            }
            r = -1;
            try {
                r = hivrarService.vrAutoRefresh(1);
            } catch (RemoteException e2) {
                Slog.e(TAG, "hivrarService service will be reset");
                resetHivrarService();
            }
            if (r != 0) {
                Slog.e(TAG, "startFrontBuffer fail");
                return false;
            }
            Slog.d(TAG, "startFrontBuffer success!");
            return true;
        }
    }

    public boolean stopFrontBufferDisplay() {
        Slog.d(TAG, "stopFrontBufferDisplay");
        synchronized (this.mLock) {
            this.mContext.enforceCallingOrSelfPermission("com.huawei.android.permission.VR", null);
            IHivrarService hivrarService = getHivrarService();
            if (hivrarService == null) {
                Slog.e(TAG, "hivrarService service is unavailable");
                return false;
            }
            int r = -1;
            try {
                r = hivrarService.vrAutoRefresh(0);
            } catch (RemoteException e) {
                Slog.e(TAG, "hivrarService service will be reset");
                resetHivrarService();
            }
            if (r != 0) {
                Slog.e(TAG, "stopFrontBuffer fail");
                return false;
            }
            Slog.d(TAG, "stopFrontBuffer success!");
            hivrarService = getHivrarService();
            if (hivrarService == null) {
                Slog.e(TAG, "hivrarService service is unavailable");
                return false;
            }
            r = -1;
            try {
                r = hivrarService.vrDeinit();
            } catch (RemoteException e2) {
                Slog.e(TAG, "hivrarService service will be reset");
                resetHivrarService();
            }
            if (r != 0) {
                Slog.e(TAG, "vrDeinit fail");
                return false;
            }
            Slog.d(TAG, "vrDeinit success!");
            return true;
        }
    }

    public int setSchedFifo(int tid, int rtPriority) {
        return -1;
    }

    public int setVrPlatPerf(int cpuLevel, int gpuLevel) {
        return -1;
    }

    public int releaseVrPlatPerf() {
        return -1;
    }

    public int[] getAvailableFreqLevels() {
        return new int[]{-1};
    }
}
