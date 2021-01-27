package com.huawei.servicehost;

import android.hardware.camera2.impl.CameraMetadataNative;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class ImageProcessManager implements IBinder.DeathRecipient {
    private static final boolean DEBUG = true;
    private static final Object LOCK = new Object();
    private static final String TAG = "ImageProcessManager";
    private static ImageProcessManager imageProcessManager;
    private IImageProcessService imageProcessService;
    private final Object lock = new Object();

    private ImageProcessManager() {
        getImageProcessService();
    }

    public static ImageProcessManager get() {
        ImageProcessManager imageProcessManager2;
        synchronized (LOCK) {
            if (imageProcessManager == null) {
                imageProcessManager = new ImageProcessManager();
            }
            imageProcessManager2 = imageProcessManager;
        }
        return imageProcessManager2;
    }

    public IImageProcessSession createIPSession(String type) {
        synchronized (this.lock) {
            try {
                if (this.imageProcessService == null) {
                    return null;
                }
                return this.imageProcessService.createIPSession(type);
            } catch (RemoteException e) {
                Log.e(TAG, "createIPSession failed.");
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    public IGlobalSession getGlobalSession() {
        synchronized (this.lock) {
            try {
                if (this.imageProcessService == null) {
                    return null;
                }
                return this.imageProcessService.getGlobalSession();
            } catch (RemoteException e) {
                Log.e(TAG, "getGlobalSession failed.");
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    public int getSupportedMode() {
        synchronized (this.lock) {
            try {
                if (this.imageProcessService == null) {
                    return 0;
                }
                return this.imageProcessService.getSupportedMode();
            } catch (RemoteException e) {
                Log.e(TAG, "getSupportedMode failed.");
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    public void queryCapability(String cameraId, CameraMetadataNative nativeMeta) {
        synchronized (this.lock) {
            try {
                if (this.imageProcessService != null) {
                    this.imageProcessService.queryCapability(cameraId, nativeMeta);
                }
            } catch (RemoteException e) {
                Log.e(TAG, "queryCapability failed.");
            }
        }
    }

    public int dualCameraMode() {
        synchronized (this.lock) {
            try {
                if (this.imageProcessService == null) {
                    return 0;
                }
                return this.imageProcessService.dualCameraMode();
            } catch (RemoteException e) {
                Log.e(TAG, "dualCameraMode failed.");
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    private IImageProcessService getImageProcessService() {
        synchronized (this.lock) {
            if (this.imageProcessService != null) {
                return this.imageProcessService;
            }
            this.imageProcessService = ServiceFetcher.get().getImageProcessService();
            if (this.imageProcessService == null) {
                Log.e(TAG, "fail to getImageProcessService.");
                return null;
            }
            try {
                Log.v(TAG, "link servicehost ok.");
                this.imageProcessService.asBinder().linkToDeath(this, 0);
                return this.imageProcessService;
            } catch (RemoteException e) {
                Log.e(TAG, "linkToDeath to camera servicehost failed.");
                return null;
            }
        }
    }

    @Override // android.os.IBinder.DeathRecipient
    public void binderDied() {
        synchronized (LOCK) {
            imageProcessManager = null;
        }
        synchronized (this.lock) {
            Log.w(TAG, "camera servicehost died.");
            this.imageProcessService = null;
        }
    }
}
