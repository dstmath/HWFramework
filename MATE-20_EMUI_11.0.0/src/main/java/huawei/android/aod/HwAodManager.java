package huawei.android.aod;

import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Slog;
import huawei.android.aod.IAodManager;

public class HwAodManager {
    public static final String AOD_PERMISSION = "com.huawei.permission.aod.UPDATE_AOD";
    public static final String TAG = "HwAodManager";
    private static volatile HwAodManager mInstance = null;

    private HwAodManager() {
    }

    public static synchronized HwAodManager getInstance() {
        HwAodManager hwAodManager;
        synchronized (HwAodManager.class) {
            if (mInstance == null) {
                mInstance = new HwAodManager();
            }
            hwAodManager = mInstance;
        }
        return hwAodManager;
    }

    private IAodManager getService() {
        return IAodManager.Stub.asInterface(ServiceManager.getService("aod_service"));
    }

    public void start() {
        IAodManager service = getService();
        if (service != null) {
            try {
                service.start();
            } catch (RemoteException e) {
                Slog.w(TAG, "start binder error!");
            }
        } else {
            Slog.e(TAG, "start with HwAodManager not exist!");
        }
    }

    public void configAndStart(AodConfigInfo aodInfo, int fileSize, ParcelFileDescriptor pfd) {
        IAodManager service = getService();
        if (service != null) {
            try {
                service.configAndStart(aodInfo, fileSize, pfd);
            } catch (RemoteException e) {
                Slog.w(TAG, "start binder error!");
            }
        } else {
            Slog.e(TAG, "start with HwAodManager not exist!");
        }
    }

    public void stop() {
        IAodManager service = getService();
        if (service != null) {
            try {
                service.stop();
            } catch (RemoteException e) {
                Slog.w(TAG, "stop binder error!");
            }
        } else {
            Slog.e(TAG, "stop with HwAodManager not exist!");
        }
    }

    public void pause() {
        IAodManager service = getService();
        if (service != null) {
            try {
                service.pause();
            } catch (RemoteException e) {
                Slog.w(TAG, "pause binder error!");
            }
        } else {
            Slog.e(TAG, "pause with HwAodManager not exist!");
        }
    }

    public void pauseAodWithScreenState(int screenState) {
        IAodManager service = getService();
        if (service != null) {
            try {
                service.pauseAodWithScreenState(screenState);
            } catch (RemoteException e) {
                Slog.w(TAG, "pauseAodWithScreenState binder error!");
            }
        } else {
            Slog.e(TAG, "pauseAodWithScreenState with HwAodManager not exist!");
        }
    }

    public void resume() {
        IAodManager service = getService();
        if (service != null) {
            try {
                service.resume();
            } catch (RemoteException e) {
                Slog.w(TAG, "resume binder error!");
            }
        } else {
            Slog.e(TAG, "resume with HwAodManager not exist!");
        }
    }

    public void beginUpdate() {
        IAodManager service = getService();
        if (service != null) {
            try {
                service.beginUpdate();
            } catch (RemoteException e) {
                Slog.w(TAG, "beginUpdate binder error!");
            }
        } else {
            Slog.e(TAG, "beginUpdate with HwAodManager not exist!");
        }
    }

    public void configAndUpdate(AodConfigInfo aodInfo, int fileSize, ParcelFileDescriptor pfd) {
        IAodManager service = getService();
        if (service != null) {
            try {
                service.configAndUpdate(aodInfo, fileSize, pfd);
            } catch (RemoteException e) {
                Slog.w(TAG, "beginUpdate binder error!");
            }
        } else {
            Slog.e(TAG, "beginUpdate with HwAodManager not exist!");
        }
    }

    public void endUpdate() {
        IAodManager service = getService();
        if (service != null) {
            try {
                service.endUpdate();
            } catch (RemoteException e) {
                Slog.w(TAG, "endUpdate binder error!");
            }
        } else {
            Slog.e(TAG, "endUpdate with HwAodManager not exist!");
        }
    }

    public void setAodConfig(AodConfigInfo aodInfo) {
        IAodManager service = getService();
        if (service != null) {
            try {
                service.setAodConfig(aodInfo);
            } catch (RemoteException e) {
                Slog.w(TAG, "setAodConfig binder error!");
            }
        } else {
            Slog.e(TAG, "setAodConfig with HwAodManager not exist!");
        }
    }

    public void updateAodVolumeInfo(AodVolumeInfo aodVolumeInfo) {
        IAodManager service = getService();
        if (service != null) {
            try {
                service.updateAodVolumeInfo(aodVolumeInfo);
            } catch (RemoteException e) {
                Slog.w(TAG, "updateAodVolumeInfo binder error!");
            }
        } else {
            Slog.e(TAG, "updateAodVolumeInfo with HwAodManager not exist!");
        }
    }

    public void setBitmapByMemoryFile(int fileSize, ParcelFileDescriptor pfd) {
        IAodManager service = getService();
        if (service != null) {
            try {
                service.setBitmapByMemoryFile(fileSize, pfd);
            } catch (RemoteException e) {
                Slog.w(TAG, "setBitmapByMemoryFile binder error!");
            }
        } else {
            Slog.e(TAG, "setBitmapByMemoryFile with HwAodManager not exist!");
        }
    }

    public int getDeviceNodeFD() {
        IAodManager service = getService();
        if (service != null) {
            try {
                return service.getDeviceNodeFD();
            } catch (RemoteException e) {
                Slog.w(TAG, "getDeviceNodeFD binder error!");
                return -2147483647;
            }
        } else {
            Slog.e(TAG, "getDeviceNodeFD with HwAodManager not exist!");
            return -2147483647;
        }
    }

    public void setPowerState(int state) {
        IAodManager service = getService();
        if (service != null) {
            try {
                service.setPowerState(state);
            } catch (RemoteException e) {
                Slog.w(TAG, "setPowerState binder error!");
            }
        } else {
            Slog.e(TAG, "setPowerState with HwAodManager not exist!");
        }
    }

    public int getAodStatus() {
        IAodManager service = getService();
        if (service != null) {
            try {
                int mStatus = service.getAodStatus();
                Slog.e(TAG, "getAodStatus mStatus " + mStatus);
                return mStatus;
            } catch (RemoteException e) {
                Slog.w(TAG, "getAodStatus binder error!");
                return -2147483647;
            }
        } else {
            Slog.e(TAG, "getAodStatus with HwAodManager not exist!");
            return -2147483647;
        }
    }

    public Bundle getAodInfo(int infoType) {
        IAodManager service = getService();
        if (service != null) {
            try {
                return service.getAodInfo(infoType);
            } catch (RemoteException ex) {
                Slog.e(TAG, "getAodInfo failed!", ex);
                return null;
            }
        } else {
            Slog.e(TAG, "getAodInfo due to null service!");
            return null;
        }
    }
}
