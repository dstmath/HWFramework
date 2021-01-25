package ohos.media.camera.mode.utils;

import java.util.HashMap;
import ohos.eventhandler.EventHandler;
import ohos.media.camera.device.Camera;
import ohos.media.camera.device.CameraManager;
import ohos.media.camera.device.CameraStateCallback;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class CameraDeviceHelper {
    private static final String CAMERA_DEVICE_IS_NULL = "camera device is null";
    private static final Logger LOGGER = LoggerFactory.getCameraLogger(CameraDeviceHelper.class);
    private static final String UNKNOWN_CAMERA_ID = "unknown cameraId";
    private static HashMap<String, CameraDeviceEx> cameraDeviceClosingList;
    private static HashMap<String, CameraDeviceEx> cameraDeviceList;

    private CameraDeviceHelper() {
    }

    public static Camera getCamera(String str, CameraStateCallback cameraStateCallback) {
        Camera camera;
        LOGGER.debug("getCamera: %{public}s", str);
        synchronized (CameraDeviceHelper.class) {
            if (cameraDeviceList != null && cameraDeviceList.containsKey(str)) {
                CameraDeviceEx cameraDeviceEx = cameraDeviceList.get(str);
                if (cameraDeviceEx.getUsedState() == 1) {
                    cameraDeviceEx.setUsedState(0);
                    cameraDeviceEx.setStateCallback(cameraStateCallback);
                    camera = cameraDeviceEx.getDevice();
                    LOGGER.debug("getCamera successfully: %{public}s", str);
                }
            }
            camera = null;
        }
        return camera;
    }

    /* access modifiers changed from: private */
    public static void onDeviceOpen(Camera camera) {
        LOGGER.debug("camera device onOpened: ", new Object[0]);
        if (camera == null) {
            LOGGER.debug(CAMERA_DEVICE_IS_NULL, new Object[0]);
            return;
        }
        CameraDeviceEx cameraDeviceEx = null;
        synchronized (CameraDeviceHelper.class) {
            if (cameraDeviceList != null && cameraDeviceList.containsKey(camera.getCameraId())) {
                cameraDeviceEx = cameraDeviceList.get(camera.getCameraId());
                cameraDeviceEx.setDevice(camera);
            }
        }
        if (cameraDeviceEx != null && cameraDeviceEx.getStateCallback() != null) {
            cameraDeviceEx.getStateCallback().onCreated(camera);
        }
    }

    /* access modifiers changed from: private */
    public static void onDeviceClose(Camera camera) {
        LOGGER.debug("camera device onClosed: ", new Object[0]);
        if (camera == null) {
            LOGGER.debug(CAMERA_DEVICE_IS_NULL, new Object[0]);
            return;
        }
        CameraDeviceEx cameraDeviceEx = null;
        synchronized (CameraDeviceHelper.class) {
            if (cameraDeviceClosingList != null && cameraDeviceClosingList.containsKey(camera.getCameraId())) {
                cameraDeviceEx = cameraDeviceClosingList.get(camera.getCameraId());
                cameraDeviceClosingList.remove(camera.getCameraId());
            }
        }
        if (cameraDeviceEx != null && cameraDeviceEx.getStateCallback() != null) {
            cameraDeviceEx.getStateCallback().onReleased(camera);
        }
    }

    private static CameraStateCallback createStateCallback() {
        return new CameraStateCallback() {
            /* class ohos.media.camera.mode.utils.CameraDeviceHelper.AnonymousClass1 */

            @Override // ohos.media.camera.device.CameraStateCallback
            public void onCreated(Camera camera) {
                CameraDeviceHelper.onDeviceOpen(camera);
            }

            @Override // ohos.media.camera.device.CameraStateCallback
            public void onReleased(Camera camera) {
                CameraDeviceHelper.onDeviceClose(camera);
            }

            @Override // ohos.media.camera.device.CameraStateCallback
            public void onCreateFailed(String str, int i) {
                CameraDeviceEx cameraDeviceEx;
                CameraDeviceHelper.LOGGER.debug("camera device create failed: ", new Object[0]);
                synchronized (CameraDeviceHelper.class) {
                    cameraDeviceEx = (CameraDeviceHelper.cameraDeviceList == null || !CameraDeviceHelper.cameraDeviceList.containsKey(str)) ? null : (CameraDeviceEx) CameraDeviceHelper.cameraDeviceList.get(str);
                }
                if (cameraDeviceEx != null && cameraDeviceEx.getStateCallback() != null) {
                    cameraDeviceEx.getStateCallback().onCreateFailed(str, i);
                }
            }

            @Override // ohos.media.camera.device.CameraStateCallback
            public void onConfigured(Camera camera) {
                CameraDeviceEx cameraDeviceEx;
                synchronized (CameraDeviceHelper.class) {
                    cameraDeviceEx = (CameraDeviceHelper.cameraDeviceList == null || !CameraDeviceHelper.cameraDeviceList.containsKey(camera.getCameraId())) ? null : (CameraDeviceEx) CameraDeviceHelper.cameraDeviceList.get(camera.getCameraId());
                }
                if (cameraDeviceEx != null && cameraDeviceEx.getStateCallback() != null) {
                    cameraDeviceEx.getStateCallback().onConfigured(camera);
                }
            }

            @Override // ohos.media.camera.device.CameraStateCallback
            public void onConfigureFailed(Camera camera, int i) {
                CameraDeviceEx cameraDeviceEx;
                CameraDeviceHelper.LOGGER.debug("camera device configure failed: ", new Object[0]);
                synchronized (CameraDeviceHelper.class) {
                    cameraDeviceEx = (CameraDeviceHelper.cameraDeviceList == null || !CameraDeviceHelper.cameraDeviceList.containsKey(camera.getCameraId())) ? null : (CameraDeviceEx) CameraDeviceHelper.cameraDeviceList.get(camera.getCameraId());
                }
                if (cameraDeviceEx != null && cameraDeviceEx.getStateCallback() != null) {
                    cameraDeviceEx.getStateCallback().onConfigureFailed(camera, i);
                }
            }

            @Override // ohos.media.camera.device.CameraStateCallback
            public void onFatalError(Camera camera, int i) {
                CameraDeviceHelper.LOGGER.debug("camera device onError: %{public}d", Integer.valueOf(i));
                if (camera == null) {
                    CameraDeviceHelper.LOGGER.debug(CameraDeviceHelper.CAMERA_DEVICE_IS_NULL, new Object[0]);
                    return;
                }
                CameraDeviceEx cameraDeviceEx = null;
                synchronized (CameraDeviceHelper.class) {
                    if (CameraDeviceHelper.cameraDeviceList != null && CameraDeviceHelper.cameraDeviceList.containsKey(camera.getCameraId())) {
                        cameraDeviceEx = (CameraDeviceEx) CameraDeviceHelper.cameraDeviceList.get(camera.getCameraId());
                        cameraDeviceEx.setDevice(camera);
                    }
                }
                if (cameraDeviceEx != null && cameraDeviceEx.getStateCallback() != null) {
                    cameraDeviceEx.getStateCallback().onFatalError(camera, i);
                }
            }
        };
    }

    public static void openCamera(String str, CameraStateCallback cameraStateCallback, EventHandler eventHandler) {
        LOGGER.debug("openCamera: %{public}s", str);
        if (!StringUtil.isEmptyString(str) && cameraStateCallback != null && eventHandler != null) {
            synchronized (CameraDeviceHelper.class) {
                if (cameraDeviceList == null) {
                    cameraDeviceList = new HashMap<>();
                    cameraDeviceClosingList = new HashMap<>();
                }
                if (cameraDeviceList.containsKey(str)) {
                    eventHandler.postTask(new Runnable(str) {
                        /* class ohos.media.camera.mode.utils.$$Lambda$CameraDeviceHelper$Laa5mEKe7GwiGOafw3I2otwepS4 */
                        private final /* synthetic */ String f$1;

                        {
                            this.f$1 = r2;
                        }

                        @Override // java.lang.Runnable
                        public final void run() {
                            CameraStateCallback.this.onCreateFailed(this.f$1, -2);
                        }
                    });
                    return;
                }
                CameraDeviceEx cameraDeviceEx = new CameraDeviceEx();
                cameraDeviceEx.setUsedState(0);
                cameraDeviceEx.setStateCallback(cameraStateCallback);
                cameraDeviceList.put(str, cameraDeviceEx);
            }
            CameraManager cameraManager = CameraManagerHelper.getCameraManager();
            if (cameraManager != null) {
                cameraManager.createCamera(str, createStateCallback(), eventHandler);
            } else {
                LOGGER.error("can not get CameraManager when openCamera!", new Object[0]);
            }
        }
    }

    public static void closeCamera(String str) {
        LOGGER.debug("closeCamera: %{public}s", str);
        synchronized (CameraDeviceHelper.class) {
            if (cameraDeviceList == null || !cameraDeviceList.containsKey(str)) {
                LOGGER.error(UNKNOWN_CAMERA_ID, new Object[0]);
            } else {
                LOGGER.debug("really closeCamera: %{public}s", str);
                CameraDeviceEx cameraDeviceEx = cameraDeviceList.get(str);
                cameraDeviceClosingList.put(str, cameraDeviceEx);
                if (cameraDeviceEx.getDevice() == null) {
                    LOGGER.debug("closeCamera: Camera Device is null.", new Object[0]);
                    cameraDeviceList.remove(str);
                    return;
                }
                cameraDeviceEx.getDevice().release();
                cameraDeviceList.remove(str);
            }
        }
    }

    public static void closeCameraStrong(String str) {
        LOGGER.debug("closeCameraStrong: ", new Object[0]);
        synchronized (CameraDeviceHelper.class) {
            if (cameraDeviceList == null || !cameraDeviceList.containsKey(str)) {
                LOGGER.error(UNKNOWN_CAMERA_ID, new Object[0]);
            } else {
                LOGGER.debug("only set WEAK state: %{public}s", str);
                cameraDeviceList.get(str).setUsedState(1);
                cameraDeviceList.get(str).setStateCallback(null);
            }
        }
    }

    public static boolean closeCameraWeak(String str) {
        LOGGER.debug("closeCameraWeak: ", new Object[0]);
        synchronized (CameraDeviceHelper.class) {
            if (cameraDeviceList == null || !cameraDeviceList.containsKey(str)) {
                LOGGER.warn(UNKNOWN_CAMERA_ID, new Object[0]);
                return true;
            }
            CameraDeviceEx cameraDeviceEx = cameraDeviceList.get(str);
            if (cameraDeviceEx.getUsedState() == 1) {
                LOGGER.debug("really closeCamera: %{public}s", str);
                cameraDeviceEx.getDevice().release();
                return false;
            }
            LOGGER.debug("is already used by other mode", new Object[0]);
            return true;
        }
    }
}
