package android.os.image;

import android.gsi.GsiProgress;
import android.os.RemoteException;

public class DynamicSystemManager {
    private static final String TAG = "DynamicSystemManager";
    private final IDynamicSystemService mService;

    public DynamicSystemManager(IDynamicSystemService service) {
        this.mService = service;
    }

    public class Session {
        private Session() {
        }

        public boolean write(byte[] buf) {
            try {
                return DynamicSystemManager.this.mService.write(buf);
            } catch (RemoteException e) {
                throw new RuntimeException(e.toString());
            }
        }

        public boolean commit() {
            try {
                return DynamicSystemManager.this.mService.commit();
            } catch (RemoteException e) {
                throw new RuntimeException(e.toString());
            }
        }
    }

    public Session startInstallation(long systemSize, long userdataSize) {
        try {
            if (this.mService.startInstallation(systemSize, userdataSize)) {
                return new Session();
            }
            return null;
        } catch (RemoteException e) {
            throw new RuntimeException(e.toString());
        }
    }

    public GsiProgress getInstallationProgress() {
        try {
            return this.mService.getInstallationProgress();
        } catch (RemoteException e) {
            throw new RuntimeException(e.toString());
        }
    }

    public boolean abort() {
        try {
            return this.mService.abort();
        } catch (RemoteException e) {
            throw new RuntimeException(e.toString());
        }
    }

    public boolean isInUse() {
        try {
            return this.mService.isInUse();
        } catch (RemoteException e) {
            throw new RuntimeException(e.toString());
        }
    }

    public boolean isInstalled() {
        try {
            return this.mService.isInstalled();
        } catch (RemoteException e) {
            throw new RuntimeException(e.toString());
        }
    }

    public boolean isEnabled() {
        try {
            return this.mService.isEnabled();
        } catch (RemoteException e) {
            throw new RuntimeException(e.toString());
        }
    }

    public boolean remove() {
        try {
            return this.mService.remove();
        } catch (RemoteException e) {
            throw new RuntimeException(e.toString());
        }
    }

    public boolean setEnable(boolean enable) {
        try {
            return this.mService.setEnable(enable);
        } catch (RemoteException e) {
            throw new RuntimeException(e.toString());
        }
    }
}
