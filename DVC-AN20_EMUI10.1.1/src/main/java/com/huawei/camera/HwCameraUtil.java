package com.huawei.camera;

import android.hardware.ICameraService;
import android.media.ImageReader;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.util.Log;

public final class HwCameraUtil {
    private static final String TAG = "HwCameraUtil";

    private HwCameraUtil() {
    }

    public static synchronized boolean requestPreviewImage(String cameraId, ImageReader imageReader) {
        synchronized (HwCameraUtil.class) {
            if (cameraId != null) {
                if (cameraId.length() != 0) {
                    if (imageReader == null) {
                        Log.e(TAG, "requestPreviewImage imageReader is null");
                        return false;
                    } else if (imageReader.getSurface() == null) {
                        Log.e(TAG, "requestPreviewImage imageReader surface is null");
                        return false;
                    } else if (!imageReader.getSurface().isValid()) {
                        Log.e(TAG, "requestPreviewImage imageReader surface is invalid");
                        return false;
                    } else if (imageReader.getImageFormat() != 35) {
                        Log.e(TAG, "requestPreviewImage unsupported format " + imageReader.getImageFormat());
                        return false;
                    } else {
                        ICameraService cameraService = CameraManagerGlobal.get().getCameraService();
                        if (cameraService == null) {
                            Log.e(TAG, "getCameraService is null");
                            return false;
                        }
                        try {
                            return cameraService.requestPreviewImage(cameraId, imageReader.getSurface());
                        } catch (RemoteException e) {
                            Log.e(TAG, "requestPreviewImage RemoteException " + e.getMessage());
                            return false;
                        }
                    }
                }
            }
            Log.e(TAG, "requestPreviewImage cameraId illegal");
            return false;
        }
    }

    private static final class CameraManagerGlobal implements IBinder.DeathRecipient {
        private static final CameraManagerGlobal CAMERA_MANAGER_GLOBAL = new CameraManagerGlobal();
        private static final String CAMERA_SERVICE_BINDER_NAME = "media.camera";
        private static final boolean IS_CAMERA_SERVICE_DISABLED = SystemProperties.getBoolean("config.disable_cameraservice", false);
        private static final String TAG = "CameraManagerGlobal";
        private final Object lock = new Object();
        private ICameraService mCameraService;

        private CameraManagerGlobal() {
        }

        public static CameraManagerGlobal get() {
            return CAMERA_MANAGER_GLOBAL;
        }

        public ICameraService getCameraService() {
            ICameraService iCameraService;
            synchronized (this.lock) {
                connectCameraServiceLocked();
                if (this.mCameraService == null && !IS_CAMERA_SERVICE_DISABLED) {
                    Log.e(TAG, "Camera service is unavailable");
                }
                iCameraService = this.mCameraService;
            }
            return iCameraService;
        }

        private void connectCameraServiceLocked() {
            if (this.mCameraService == null && !IS_CAMERA_SERVICE_DISABLED) {
                Log.i(TAG, "Connecting to camera service");
                IBinder cameraServiceBinder = ServiceManager.getService(CAMERA_SERVICE_BINDER_NAME);
                if (cameraServiceBinder == null) {
                    Log.e(TAG, "Camera service is now down, leave mCameraService as null");
                    return;
                }
                try {
                    cameraServiceBinder.linkToDeath(this, 0);
                    this.mCameraService = ICameraService.Stub.asInterface(cameraServiceBinder);
                } catch (RemoteException e) {
                    Log.e(TAG, "linkToDeath failed");
                }
            }
        }

        public void binderDied() {
            synchronized (this.lock) {
                if (this.mCameraService != null) {
                    this.mCameraService = null;
                }
            }
        }
    }
}
