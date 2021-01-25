package com.android.server;

import android.content.Context;
import android.gsi.GsiInstallParams;
import android.gsi.GsiProgress;
import android.gsi.IGsiService;
import android.net.INetd;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.image.IDynamicSystemService;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.util.Slog;
import java.io.File;

public class DynamicSystemService extends IDynamicSystemService.Stub implements IBinder.DeathRecipient {
    private static final int GSID_ROUGH_TIMEOUT_MS = 8192;
    private static final String NO_SERVICE_ERROR = "no gsiservice";
    private static final String PATH_DEFAULT = "/data/gsi";
    private static final String TAG = "DynamicSystemService";
    private Context mContext;
    private volatile IGsiService mGsiService;

    DynamicSystemService(Context context) {
        this.mContext = context;
    }

    private static IGsiService connect(IBinder.DeathRecipient recipient) throws RemoteException {
        IBinder binder = ServiceManager.getService("gsiservice");
        if (binder == null) {
            return null;
        }
        binder.linkToDeath(recipient, 0);
        return IGsiService.Stub.asInterface(binder);
    }

    @Override // android.os.IBinder.DeathRecipient
    public void binderDied() {
        Slog.w(TAG, "gsiservice died; reconnecting");
        synchronized (this) {
            this.mGsiService = null;
        }
    }

    private IGsiService getGsiService() throws RemoteException {
        checkPermission();
        if (!INetd.IF_FLAG_RUNNING.equals(SystemProperties.get("init.svc.gsid"))) {
            SystemProperties.set("ctl.start", "gsid");
        }
        for (int sleepMs = 64; sleepMs <= 16384; sleepMs <<= 1) {
            synchronized (this) {
                if (this.mGsiService == null) {
                    this.mGsiService = connect(this);
                }
                if (this.mGsiService != null) {
                    return this.mGsiService;
                }
            }
            try {
                Slog.d(TAG, "GsiService is not ready, wait for " + sleepMs + "ms");
                Thread.sleep((long) sleepMs);
            } catch (InterruptedException e) {
                Slog.e(TAG, "Interrupted when waiting for GSID");
                return null;
            }
        }
        throw new RemoteException(NO_SERVICE_ERROR);
    }

    private void checkPermission() {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.MANAGE_DYNAMIC_SYSTEM") != 0) {
            throw new SecurityException("Requires MANAGE_DYNAMIC_SYSTEM permission");
        }
    }

    public boolean startInstallation(long systemSize, long userdataSize) throws RemoteException {
        String path = SystemProperties.get("os.aot.path");
        if (path.isEmpty()) {
            StorageVolume[] volumes = StorageManager.getVolumeList(UserHandle.myUserId(), 256);
            int length = volumes.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                }
                StorageVolume volume = volumes[i];
                if (!volume.isEmulated() && volume.isRemovable() && "mounted".equals(volume.getState())) {
                    File sdCard = volume.getPathFile();
                    if (sdCard.isDirectory()) {
                        path = sdCard.getPath();
                        break;
                    }
                }
                i++;
            }
            if (path.isEmpty()) {
                path = PATH_DEFAULT;
            }
            Slog.i(TAG, "startInstallation -> " + path);
        }
        GsiInstallParams installParams = new GsiInstallParams();
        installParams.installDir = path;
        installParams.gsiSize = systemSize;
        installParams.userdataSize = userdataSize;
        if (getGsiService().beginGsiInstall(installParams) == 0) {
            return true;
        }
        return false;
    }

    public GsiProgress getInstallationProgress() throws RemoteException {
        return getGsiService().getInstallProgress();
    }

    public boolean abort() throws RemoteException {
        return getGsiService().cancelGsiInstall();
    }

    public boolean isInUse() throws RemoteException {
        boolean gsidWasRunning = INetd.IF_FLAG_RUNNING.equals(SystemProperties.get("init.svc.gsid"));
        boolean isInUse = false;
        try {
            isInUse = getGsiService().isGsiRunning();
            return isInUse;
        } finally {
            if (!gsidWasRunning && !isInUse) {
                SystemProperties.set("ctl.stop", "gsid");
            }
        }
    }

    public boolean isInstalled() throws RemoteException {
        return getGsiService().isGsiInstalled();
    }

    public boolean isEnabled() throws RemoteException {
        return getGsiService().isGsiEnabled();
    }

    public boolean remove() throws RemoteException {
        return getGsiService().removeGsiInstall();
    }

    public boolean setEnable(boolean enable) throws RemoteException {
        IGsiService gsiService = getGsiService();
        if (!enable) {
            return gsiService.disableGsiInstall();
        }
        return gsiService.setGsiBootable(gsiService.getGsiBootStatus() == 2) == 0;
    }

    public boolean write(byte[] buf) throws RemoteException {
        return getGsiService().commitGsiChunkFromMemory(buf);
    }

    public boolean commit() throws RemoteException {
        return getGsiService().setGsiBootable(true) == 0;
    }
}
