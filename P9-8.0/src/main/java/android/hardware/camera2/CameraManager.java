package android.hardware.camera2;

import android.app.ActivityThread;
import android.content.Context;
import android.hardware.CameraStatus;
import android.hardware.ICameraService;
import android.hardware.ICameraServiceListener.Stub;
import android.hardware.camera2.CameraDevice.StateCallback;
import android.hardware.camera2.impl.CameraDeviceImpl;
import android.hardware.camera2.impl.CameraMetadataNative;
import android.hardware.camera2.legacy.CameraDeviceUserShim;
import android.hardware.camera2.legacy.LegacyMetadataMapper;
import android.hsm.HwSystemManager;
import android.os.Binder;
import android.os.DeadObjectException;
import android.os.Handler;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.Looper;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.ServiceSpecificException;
import android.os.SystemProperties;
import android.util.ArrayMap;
import android.util.Log;
import java.util.ArrayList;

public final class CameraManager {
    private static final int API_VERSION_1 = 1;
    private static final int API_VERSION_2 = 2;
    private static final int CAMERA_TYPE_ALL = 1;
    private static final int CAMERA_TYPE_BACKWARD_COMPATIBLE = 0;
    private static final String TAG = "CameraManager";
    private static final int USE_CALLING_UID = -1;
    private final boolean DEBUG = false;
    private final Context mContext;
    private ArrayList<String> mDeviceIdList;
    private final Object mLock = new Object();

    public static abstract class AvailabilityCallback {
        public void onCameraAvailable(String cameraId) {
        }

        public void onCameraUnavailable(String cameraId) {
        }
    }

    private static final class CameraManagerGlobal extends Stub implements DeathRecipient {
        private static final String CAMERA_SERVICE_BINDER_NAME = "media.camera";
        private static final String TAG = "CameraManagerGlobal";
        private static final CameraManagerGlobal gCameraManager = new CameraManagerGlobal();
        private final int CAMERA_SERVICE_RECONNECT_DELAY_MS = 1000;
        private final boolean DEBUG = false;
        private final ArrayMap<AvailabilityCallback, Handler> mCallbackMap = new ArrayMap();
        private ICameraService mCameraService;
        private final ArrayMap<String, Integer> mDeviceStatus = new ArrayMap();
        private final Object mLock = new Object();
        private final ArrayMap<TorchCallback, Handler> mTorchCallbackMap = new ArrayMap();
        private Binder mTorchClientBinder = new Binder();
        private final ArrayMap<String, Integer> mTorchStatus = new ArrayMap();

        private CameraManagerGlobal() {
        }

        public static CameraManagerGlobal get() {
            return gCameraManager;
        }

        public IBinder asBinder() {
            return this;
        }

        public ICameraService getCameraService() {
            ICameraService iCameraService;
            synchronized (this.mLock) {
                connectCameraServiceLocked();
                if (this.mCameraService == null) {
                    Log.e(TAG, "Camera service is unavailable");
                }
                iCameraService = this.mCameraService;
            }
            return iCameraService;
        }

        private void connectCameraServiceLocked() {
            if (this.mCameraService == null) {
                Log.i(TAG, "Connecting to camera service");
                IBinder cameraServiceBinder = ServiceManager.getService(CAMERA_SERVICE_BINDER_NAME);
                if (cameraServiceBinder != null) {
                    try {
                        cameraServiceBinder.linkToDeath(this, 0);
                        ICameraService cameraService = ICameraService.Stub.asInterface(cameraServiceBinder);
                        try {
                            CameraMetadataNative.setupGlobalVendorTagDescriptor();
                        } catch (ServiceSpecificException e) {
                            handleRecoverableSetupErrors(e);
                        }
                        try {
                            for (CameraStatus c : cameraService.addListener(this)) {
                                onStatusChangedLocked(c.status, c.cameraId);
                            }
                            this.mCameraService = cameraService;
                        } catch (ServiceSpecificException e2) {
                            throw new IllegalStateException("Failed to register a camera service listener", e2);
                        } catch (RemoteException e3) {
                        }
                    } catch (RemoteException e4) {
                    }
                }
            }
        }

        public String[] getCameraIdList() {
            String[] cameraIds;
            synchronized (this.mLock) {
                int i;
                int status;
                connectCameraServiceLocked();
                int idCount = 0;
                int deviceStatusSize = this.mDeviceStatus.size();
                if (needHideAuxCamera(deviceStatusSize)) {
                    Log.i(TAG, "hide aux camera.");
                    deviceStatusSize--;
                }
                for (i = 0; i < deviceStatusSize; i++) {
                    status = ((Integer) this.mDeviceStatus.valueAt(i)).intValue();
                    if (!(status == 0 || status == 2)) {
                        idCount++;
                    }
                }
                cameraIds = new String[idCount];
                idCount = 0;
                for (i = 0; i < deviceStatusSize; i++) {
                    status = ((Integer) this.mDeviceStatus.valueAt(i)).intValue();
                    if (!(status == 0 || status == 2)) {
                        cameraIds[idCount] = (String) this.mDeviceStatus.keyAt(i);
                        idCount++;
                    }
                }
            }
            return cameraIds;
        }

        public void setTorchMode(String cameraId, boolean enabled) throws CameraAccessException {
            synchronized (this.mLock) {
                if (cameraId == null) {
                    throw new IllegalArgumentException("cameraId was null");
                } else if (!needHideAuxCamera(this.mDeviceStatus.size()) || Integer.parseInt(cameraId) < 2) {
                    ICameraService cameraService = getCameraService();
                    if (cameraService == null) {
                        throw new CameraAccessException(2, "Camera service is currently unavailable");
                    }
                    try {
                        cameraService.setTorchMode(cameraId, enabled, this.mTorchClientBinder);
                    } catch (ServiceSpecificException e) {
                        CameraManager.throwAsPublicException(e);
                    } catch (RemoteException e2) {
                        throw new CameraAccessException(2, "Camera service is currently unavailable");
                    }
                } else {
                    throw new IllegalArgumentException("invalid cameraId");
                }
            }
        }

        private void handleRecoverableSetupErrors(ServiceSpecificException e) {
            switch (e.errorCode) {
                case 4:
                    Log.w(TAG, e.getMessage());
                    return;
                default:
                    throw new IllegalStateException(e);
            }
        }

        private boolean isAvailable(int status) {
            switch (status) {
                case 1:
                    return true;
                default:
                    return false;
            }
        }

        private boolean validStatus(int status) {
            switch (status) {
                case -2:
                case 0:
                case 1:
                case 2:
                    return true;
                default:
                    return false;
            }
        }

        private boolean validTorchStatus(int status) {
            switch (status) {
                case 0:
                case 1:
                case 2:
                    return true;
                default:
                    return false;
            }
        }

        private void postSingleUpdate(final AvailabilityCallback callback, Handler handler, final String id, int status) {
            if (isAvailable(status)) {
                handler.post(new Runnable() {
                    public void run() {
                        callback.onCameraAvailable(id);
                    }
                });
            } else {
                handler.post(new Runnable() {
                    public void run() {
                        callback.onCameraUnavailable(id);
                    }
                });
            }
        }

        private void postSingleTorchUpdate(final TorchCallback callback, Handler handler, final String id, final int status) {
            switch (status) {
                case 1:
                case 2:
                    handler.post(new Runnable() {
                        public void run() {
                            callback.onTorchModeChanged(id, status == 2);
                        }
                    });
                    return;
                default:
                    handler.post(new Runnable() {
                        public void run() {
                            callback.onTorchModeUnavailable(id);
                        }
                    });
                    return;
            }
        }

        private void updateCallbackLocked(AvailabilityCallback callback, Handler handler) {
            int deviceStatusSize = this.mDeviceStatus.size();
            if (needHideAuxCamera(deviceStatusSize)) {
                Log.i(TAG, "need hide aux camera, device size: " + deviceStatusSize);
                deviceStatusSize--;
            }
            for (int i = 0; i < deviceStatusSize; i++) {
                postSingleUpdate(callback, handler, (String) this.mDeviceStatus.keyAt(i), ((Integer) this.mDeviceStatus.valueAt(i)).intValue());
            }
        }

        private void onStatusChangedLocked(int status, String id) {
            if (needHideAuxCamera(this.mDeviceStatus.size()) && Integer.parseInt(id) >= 2) {
                Log.w(TAG, "ignore the status update of camera: " + id);
            } else if (validStatus(status)) {
                Integer oldStatus = (Integer) this.mDeviceStatus.put(id, Integer.valueOf(status));
                if (oldStatus != null && oldStatus.intValue() == status) {
                    return;
                }
                if (oldStatus == null || isAvailable(status) != isAvailable(oldStatus.intValue())) {
                    int callbackCount = this.mCallbackMap.size();
                    for (int i = 0; i < callbackCount; i++) {
                        postSingleUpdate((AvailabilityCallback) this.mCallbackMap.keyAt(i), (Handler) this.mCallbackMap.valueAt(i), id, status);
                    }
                }
            } else {
                Log.e(TAG, String.format("Ignoring invalid device %s status 0x%x", new Object[]{id, Integer.valueOf(status)}));
            }
        }

        private void updateTorchCallbackLocked(TorchCallback callback, Handler handler) {
            for (int i = 0; i < this.mTorchStatus.size(); i++) {
                postSingleTorchUpdate(callback, handler, (String) this.mTorchStatus.keyAt(i), ((Integer) this.mTorchStatus.valueAt(i)).intValue());
            }
        }

        private void onTorchStatusChangedLocked(int status, String id) {
            if (needHideAuxCamera(this.mDeviceStatus.size()) && Integer.parseInt(id) >= 2) {
                Log.w(TAG, "ignore the torch status update of camera: " + id);
            } else if (validTorchStatus(status)) {
                Integer oldStatus = (Integer) this.mTorchStatus.put(id, Integer.valueOf(status));
                if (oldStatus == null || oldStatus.intValue() != status) {
                    int callbackCount = this.mTorchCallbackMap.size();
                    for (int i = 0; i < callbackCount; i++) {
                        postSingleTorchUpdate((TorchCallback) this.mTorchCallbackMap.keyAt(i), (Handler) this.mTorchCallbackMap.valueAt(i), id, status);
                    }
                }
            } else {
                Log.e(TAG, String.format("Ignoring invalid device %s torch status 0x%x", new Object[]{id, Integer.valueOf(status)}));
            }
        }

        public void registerAvailabilityCallback(AvailabilityCallback callback, Handler handler) {
            synchronized (this.mLock) {
                connectCameraServiceLocked();
                if (((Handler) this.mCallbackMap.put(callback, handler)) == null) {
                    updateCallbackLocked(callback, handler);
                }
                if (this.mCameraService == null) {
                    scheduleCameraServiceReconnectionLocked();
                }
            }
        }

        public void unregisterAvailabilityCallback(AvailabilityCallback callback) {
            synchronized (this.mLock) {
                this.mCallbackMap.remove(callback);
            }
        }

        public void registerTorchCallback(TorchCallback callback, Handler handler) {
            synchronized (this.mLock) {
                connectCameraServiceLocked();
                if (((Handler) this.mTorchCallbackMap.put(callback, handler)) == null) {
                    updateTorchCallbackLocked(callback, handler);
                }
                if (this.mCameraService == null) {
                    scheduleCameraServiceReconnectionLocked();
                }
            }
        }

        public void unregisterTorchCallback(TorchCallback callback) {
            synchronized (this.mLock) {
                this.mTorchCallbackMap.remove(callback);
            }
        }

        public void onStatusChanged(int status, String cameraId) throws RemoteException {
            synchronized (this.mLock) {
                onStatusChangedLocked(status, cameraId);
            }
        }

        public void onTorchStatusChanged(int status, String cameraId) throws RemoteException {
            synchronized (this.mLock) {
                onTorchStatusChangedLocked(status, cameraId);
            }
        }

        private void scheduleCameraServiceReconnectionLocked() {
            Handler handler;
            if (this.mCallbackMap.size() > 0) {
                handler = (Handler) this.mCallbackMap.valueAt(0);
            } else if (this.mTorchCallbackMap.size() > 0) {
                handler = (Handler) this.mTorchCallbackMap.valueAt(0);
            } else {
                return;
            }
            handler.postDelayed(new Runnable() {
                public void run() {
                    if (CameraManagerGlobal.this.getCameraService() == null) {
                        synchronized (CameraManagerGlobal.this.mLock) {
                            CameraManagerGlobal.this.scheduleCameraServiceReconnectionLocked();
                        }
                    }
                }
            }, 1000);
        }

        public void binderDied() {
            synchronized (this.mLock) {
                if (this.mCameraService == null) {
                    return;
                }
                int i;
                this.mCameraService = null;
                for (i = 0; i < this.mDeviceStatus.size(); i++) {
                    onStatusChangedLocked(0, (String) this.mDeviceStatus.keyAt(i));
                }
                for (i = 0; i < this.mTorchStatus.size(); i++) {
                    onTorchStatusChangedLocked(0, (String) this.mTorchStatus.keyAt(i));
                }
                scheduleCameraServiceReconnectionLocked();
            }
        }

        private boolean needHideAuxCamera(int deviceSize) {
            if (SystemProperties.getInt("persist.camera.expose.aux", 0) != 1) {
                Log.i(TAG, "do not need hide aux camera, device number: " + deviceSize);
                return false;
            } else if (deviceSize < 3) {
                Log.i(TAG, "single back camera, do not need hide aux camera, device number: " + deviceSize);
                return false;
            } else {
                String specialList = SystemProperties.get("vendor.camera.aux.packagelist");
                if (specialList == null) {
                    Log.i(TAG, "invalide special list in property, device number: " + deviceSize);
                    return false;
                }
                String packageName = ActivityThread.currentOpPackageName();
                if (packageName == null) {
                    Log.i(TAG, "invalide package name, device number: " + deviceSize);
                    return false;
                } else if (specialList.contains(packageName)) {
                    Log.i(TAG, "package name: " + packageName);
                    return false;
                } else {
                    Log.i(TAG, "hide aux camera for app: " + packageName);
                    return true;
                }
            }
        }
    }

    public static abstract class TorchCallback {
        public void onTorchModeUnavailable(String cameraId) {
        }

        public void onTorchModeChanged(String cameraId, boolean enabled) {
        }
    }

    public CameraManager(Context context) {
        synchronized (this.mLock) {
            this.mContext = context;
        }
    }

    public String[] getCameraIdList() throws CameraAccessException {
        return CameraManagerGlobal.get().getCameraIdList();
    }

    public void registerAvailabilityCallback(AvailabilityCallback callback, Handler handler) {
        if (handler == null) {
            Looper looper = Looper.myLooper();
            if (looper == null) {
                throw new IllegalArgumentException("No handler given, and current thread has no looper!");
            }
            handler = new Handler(looper);
        }
        CameraManagerGlobal.get().registerAvailabilityCallback(callback, handler);
    }

    public void unregisterAvailabilityCallback(AvailabilityCallback callback) {
        CameraManagerGlobal.get().unregisterAvailabilityCallback(callback);
    }

    public void registerTorchCallback(TorchCallback callback, Handler handler) {
        if (handler == null) {
            Looper looper = Looper.myLooper();
            if (looper == null) {
                throw new IllegalArgumentException("No handler given, and current thread has no looper!");
            }
            handler = new Handler(looper);
        }
        CameraManagerGlobal.get().registerTorchCallback(callback, handler);
    }

    public void unregisterTorchCallback(TorchCallback callback) {
        CameraManagerGlobal.get().unregisterTorchCallback(callback);
    }

    public CameraCharacteristics getCameraCharacteristics(String cameraId) throws CameraAccessException {
        CameraCharacteristics characteristics = null;
        synchronized (this.mLock) {
            ICameraService cameraService = CameraManagerGlobal.get().getCameraService();
            if (cameraService == null) {
                throw new CameraAccessException(2, "Camera service is currently unavailable");
            }
            try {
                if (supportsCamera2ApiLocked(cameraId)) {
                    characteristics = new CameraCharacteristics(cameraService.getCameraCharacteristics(cameraId));
                } else {
                    int id = Integer.parseInt(cameraId);
                    characteristics = LegacyMetadataMapper.createCharacteristics(cameraService.getLegacyParameters(id), cameraService.getCameraInfo(id));
                }
            } catch (ServiceSpecificException e) {
                throwAsPublicException(e);
            } catch (RemoteException e2) {
                throw new CameraAccessException(2, "Camera service is currently unavailable", e2);
            }
        }
        return characteristics;
    }

    private CameraDevice openCameraDeviceUserAsync(String cameraId, StateCallback callback, Handler handler, int uid) throws CameraAccessException {
        CameraDevice deviceImpl;
        CameraCharacteristics characteristics = getCameraCharacteristics(cameraId);
        synchronized (this.mLock) {
            ICameraDeviceUser cameraUser = null;
            deviceImpl = new CameraDeviceImpl(cameraId, callback, handler, characteristics, this.mContext.getApplicationInfo().targetSdkVersion);
            ICameraDeviceCallbacks callbacks = deviceImpl.getCallbacks();
            try {
                CameraDevice device;
                if (supportsCamera2ApiLocked(cameraId)) {
                    if (!HwSystemManager.allowOp(1024)) {
                        throwAsPublicException(new ServiceSpecificException(6));
                    }
                    ICameraService cameraService = CameraManagerGlobal.get().getCameraService();
                    if (cameraService == null) {
                        throw new ServiceSpecificException(4, "Camera service is currently unavailable");
                    }
                    cameraUser = cameraService.connectDevice(callbacks, cameraId, this.mContext.getOpPackageName(), uid);
                    deviceImpl.setRemoteDevice(cameraUser);
                    device = deviceImpl;
                } else {
                    int id = Integer.parseInt(cameraId);
                    Log.i(TAG, "Using legacy camera HAL.");
                    cameraUser = CameraDeviceUserShim.connectBinderShim(callbacks, id);
                    deviceImpl.setRemoteDevice(cameraUser);
                    device = deviceImpl;
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Expected cameraId to be numeric, but it was: " + cameraId);
            } catch (ServiceSpecificException e2) {
                if (e2.errorCode == 9) {
                    throw new AssertionError("Should've gone down the shim path");
                } else if (e2.errorCode == 7 || e2.errorCode == 8 || e2.errorCode == 6 || e2.errorCode == 4 || e2.errorCode == 10) {
                    deviceImpl.setRemoteFailure(e2);
                    if (e2.errorCode == 6 || e2.errorCode == 4 || e2.errorCode == 7) {
                        throwAsPublicException(e2);
                    }
                } else {
                    throwAsPublicException(e2);
                }
            } catch (RemoteException e3) {
                ServiceSpecificException serviceSpecificException = new ServiceSpecificException(4, "Camera service is currently unavailable");
                deviceImpl.setRemoteFailure(serviceSpecificException);
                throwAsPublicException(serviceSpecificException);
            }
        }
        return deviceImpl;
    }

    public void openCamera(String cameraId, StateCallback callback, Handler handler) throws CameraAccessException {
        openCameraForUid(cameraId, callback, handler, -1);
    }

    public void openCameraForUid(String cameraId, StateCallback callback, Handler handler, int clientUid) throws CameraAccessException {
        if (cameraId == null) {
            throw new IllegalArgumentException("cameraId was null");
        } else if (callback == null) {
            throw new IllegalArgumentException("callback was null");
        } else {
            if (handler == null) {
                if (Looper.myLooper() != null) {
                    handler = new Handler();
                } else {
                    throw new IllegalArgumentException("Handler argument is null, but no looper exists in the calling thread");
                }
            }
            openCameraDeviceUserAsync(cameraId, callback, handler, clientUid);
        }
    }

    public void setTorchMode(String cameraId, boolean enabled) throws CameraAccessException {
        CameraManagerGlobal.get().setTorchMode(cameraId, enabled);
    }

    public static void throwAsPublicException(Throwable t) throws CameraAccessException {
        if (t instanceof ServiceSpecificException) {
            int reason;
            ServiceSpecificException e = (ServiceSpecificException) t;
            switch (e.errorCode) {
                case 1:
                    throw new SecurityException(e.getMessage(), e);
                case 2:
                case 3:
                    throw new IllegalArgumentException(e.getMessage(), e);
                case 4:
                    reason = 2;
                    break;
                case 6:
                    reason = 1;
                    break;
                case 7:
                    reason = 4;
                    break;
                case 8:
                    reason = 5;
                    break;
                case 9:
                    reason = 1000;
                    break;
                default:
                    reason = 3;
                    break;
            }
            throw new CameraAccessException(reason, e.getMessage(), e);
        } else if (t instanceof DeadObjectException) {
            throw new CameraAccessException(2, "Camera service has died unexpectedly", t);
        } else if (t instanceof RemoteException) {
            throw new UnsupportedOperationException("An unknown RemoteException was thrown which should never happen.", t);
        } else if (t instanceof RuntimeException) {
            throw ((RuntimeException) t);
        }
    }

    private boolean supportsCamera2ApiLocked(String cameraId) {
        return supportsCameraApiLocked(cameraId, 2);
    }

    private boolean supportsCameraApiLocked(String cameraId, int apiVersion) {
        try {
            ICameraService cameraService = CameraManagerGlobal.get().getCameraService();
            if (cameraService == null) {
                return false;
            }
            return cameraService.supportsCameraApi(cameraId, apiVersion);
        } catch (RemoteException e) {
            return false;
        }
    }
}
