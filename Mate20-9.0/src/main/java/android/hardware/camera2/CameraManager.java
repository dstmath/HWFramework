package android.hardware.camera2;

import android.camera.IHwCameraUtil;
import android.common.HwFrameworkFactory;
import android.content.Context;
import android.hardware.CameraStatus;
import android.hardware.ICameraService;
import android.hardware.ICameraServiceListener;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.impl.CameraDeviceImpl;
import android.hardware.camera2.impl.CameraMetadataNative;
import android.hardware.camera2.legacy.CameraDeviceUserShim;
import android.hardware.camera2.legacy.LegacyMetadataMapper;
import android.hsm.HwSystemManager;
import android.os.Binder;
import android.os.DeadObjectException;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.ServiceSpecificException;
import android.os.SystemProperties;
import android.util.ArrayMap;
import android.util.Log;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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

    private static final class CameraManagerGlobal extends ICameraServiceListener.Stub implements IBinder.DeathRecipient {
        private static final String CAMERA_SERVICE_BINDER_NAME = "media.camera";
        private static final String TAG = "CameraManagerGlobal";
        private static final CameraManagerGlobal gCameraManager = new CameraManagerGlobal();
        public static final boolean sCameraServiceDisabled = SystemProperties.getBoolean("config.disable_cameraservice", false);
        private final int CAMERA_SERVICE_RECONNECT_DELAY_MS = 1000;
        private final boolean DEBUG = false;
        private final ArrayMap<AvailabilityCallback, Executor> mCallbackMap = new ArrayMap<>();
        private ICameraService mCameraService;
        private final ArrayMap<String, Integer> mDeviceStatus = new ArrayMap<>();
        private final Object mLock = new Object();
        private final ScheduledExecutorService mScheduler = Executors.newScheduledThreadPool(1);
        private final ArrayMap<TorchCallback, Executor> mTorchCallbackMap = new ArrayMap<>();
        private Binder mTorchClientBinder = new Binder();
        private final ArrayMap<String, Integer> mTorchStatus = new ArrayMap<>();

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
                if (this.mCameraService == null && !sCameraServiceDisabled) {
                    Log.e(TAG, "Camera service is unavailable");
                }
                iCameraService = this.mCameraService;
            }
            return iCameraService;
        }

        private void connectCameraServiceLocked() {
            if (this.mCameraService == null && !sCameraServiceDisabled) {
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
                connectCameraServiceLocked();
                int deviceStatusSize = getCustDeviceSize(this.mDeviceStatus.size());
                int idCount = 0;
                for (int i = 0; i < deviceStatusSize; i++) {
                    int status = this.mDeviceStatus.valueAt(i).intValue();
                    if (status != 0) {
                        if (status != 2) {
                            idCount++;
                        }
                    }
                }
                cameraIds = new String[idCount];
                int idCount2 = 0;
                for (int i2 = 0; i2 < deviceStatusSize; i2++) {
                    int status2 = this.mDeviceStatus.valueAt(i2).intValue();
                    if (status2 != 0) {
                        if (status2 != 2) {
                            cameraIds[idCount2] = this.mDeviceStatus.keyAt(i2);
                            idCount2++;
                        }
                    }
                }
            }
            Arrays.sort(cameraIds, new Comparator<String>() {
                public int compare(String s1, String s2) {
                    int s1Int;
                    int s2Int;
                    try {
                        s1Int = Integer.parseInt(s1);
                    } catch (NumberFormatException e) {
                        s1Int = -1;
                    }
                    try {
                        s2Int = Integer.parseInt(s2);
                    } catch (NumberFormatException e2) {
                        s2Int = -1;
                    }
                    if (s1Int >= 0 && s2Int >= 0) {
                        return s1Int - s2Int;
                    }
                    if (s1Int >= 0) {
                        return -1;
                    }
                    if (s2Int >= 0) {
                        return 1;
                    }
                    return s1.compareTo(s2);
                }
            });
            return cameraIds;
        }

        public void setTorchMode(String cameraId, boolean enabled) throws CameraAccessException {
            synchronized (this.mLock) {
                if (cameraId == null) {
                    throw new IllegalArgumentException("cameraId was null");
                } else if (!isIllegalAccessAuxCamera(this.mDeviceStatus.size(), cameraId)) {
                    ICameraService cameraService = getCameraService();
                    if (cameraService != null) {
                        try {
                            cameraService.setTorchMode(cameraId, enabled, this.mTorchClientBinder);
                        } catch (ServiceSpecificException e) {
                            CameraManager.throwAsPublicException(e);
                        } catch (RemoteException e2) {
                            throw new CameraAccessException(2, "Camera service is currently unavailable");
                        }
                    } else {
                        throw new CameraAccessException(2, "Camera service is currently unavailable");
                    }
                } else {
                    throw new IllegalArgumentException("invalid cameraId");
                }
            }
        }

        private void handleRecoverableSetupErrors(ServiceSpecificException e) {
            if (e.errorCode == 4) {
                Log.w(TAG, e.getMessage());
                return;
            }
            throw new IllegalStateException(e);
        }

        private boolean isAvailable(int status) {
            if (status != 1) {
                return false;
            }
            return true;
        }

        private boolean validStatus(int status) {
            if (status != -2) {
                switch (status) {
                    case 0:
                    case 1:
                    case 2:
                        break;
                    default:
                        return false;
                }
            }
            return true;
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

        private void postSingleUpdate(final AvailabilityCallback callback, Executor executor, final String id, int status) {
            if (isAvailable(status)) {
                long ident = Binder.clearCallingIdentity();
                try {
                    executor.execute(new Runnable() {
                        public void run() {
                            callback.onCameraAvailable(id);
                        }
                    });
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            } else {
                long ident2 = Binder.clearCallingIdentity();
                try {
                    executor.execute(new Runnable() {
                        public void run() {
                            callback.onCameraUnavailable(id);
                        }
                    });
                } finally {
                    Binder.restoreCallingIdentity(ident2);
                }
            }
        }

        private void postSingleTorchUpdate(TorchCallback callback, Executor executor, String id, int status) {
            switch (status) {
                case 1:
                case 2:
                    long ident = Binder.clearCallingIdentity();
                    try {
                        executor.execute(new Runnable(id, status) {
                            private final /* synthetic */ String f$1;
                            private final /* synthetic */ int f$2;

                            {
                                this.f$1 = r2;
                                this.f$2 = r3;
                            }

                            public final void run() {
                                CameraManager.CameraManagerGlobal.lambda$postSingleTorchUpdate$0(CameraManager.TorchCallback.this, this.f$1, this.f$2);
                            }
                        });
                        return;
                    } finally {
                        Binder.restoreCallingIdentity(ident);
                    }
                default:
                    long ident2 = Binder.clearCallingIdentity();
                    try {
                        executor.execute(new Runnable(id) {
                            private final /* synthetic */ String f$1;

                            {
                                this.f$1 = r2;
                            }

                            public final void run() {
                                CameraManager.TorchCallback.this.onTorchModeUnavailable(this.f$1);
                            }
                        });
                        return;
                    } finally {
                        Binder.restoreCallingIdentity(ident2);
                    }
            }
        }

        static /* synthetic */ void lambda$postSingleTorchUpdate$0(TorchCallback callback, String id, int status) {
            callback.onTorchModeChanged(id, status == 2);
        }

        private void updateCallbackLocked(AvailabilityCallback callback, Executor executor) {
            int deviceStatusSize = getCustDeviceSize(this.mDeviceStatus.size());
            for (int i = 0; i < deviceStatusSize; i++) {
                postSingleUpdate(callback, executor, this.mDeviceStatus.keyAt(i), this.mDeviceStatus.valueAt(i).intValue());
            }
        }

        private void onStatusChangedLocked(int status, String id) {
            Integer oldStatus;
            if (!isIllegalAccessAuxCamera(this.mDeviceStatus.size(), id)) {
                if (!validStatus(status)) {
                    Log.e(TAG, String.format("Ignoring invalid device %s status 0x%x", new Object[]{id, Integer.valueOf(status)}));
                    return;
                }
                if (status == 0) {
                    oldStatus = this.mDeviceStatus.remove(id);
                } else {
                    oldStatus = this.mDeviceStatus.put(id, Integer.valueOf(status));
                }
                if (oldStatus != null && oldStatus.intValue() == status) {
                    return;
                }
                if (oldStatus == null || isAvailable(status) != isAvailable(oldStatus.intValue())) {
                    int callbackCount = this.mCallbackMap.size();
                    for (int i = 0; i < callbackCount; i++) {
                        postSingleUpdate(this.mCallbackMap.keyAt(i), this.mCallbackMap.valueAt(i), id, status);
                    }
                }
            }
        }

        private void updateTorchCallbackLocked(TorchCallback callback, Executor executor) {
            for (int i = 0; i < this.mTorchStatus.size(); i++) {
                postSingleTorchUpdate(callback, executor, this.mTorchStatus.keyAt(i), this.mTorchStatus.valueAt(i).intValue());
            }
        }

        private void onTorchStatusChangedLocked(int status, String id) {
            if (!isIllegalAccessAuxCamera(this.mDeviceStatus.size(), id)) {
                if (!validTorchStatus(status)) {
                    Log.e(TAG, String.format("Ignoring invalid device %s torch status 0x%x", new Object[]{id, Integer.valueOf(status)}));
                    return;
                }
                Integer oldStatus = this.mTorchStatus.put(id, Integer.valueOf(status));
                if (oldStatus == null || oldStatus.intValue() != status) {
                    int callbackCount = this.mTorchCallbackMap.size();
                    for (int i = 0; i < callbackCount; i++) {
                        postSingleTorchUpdate(this.mTorchCallbackMap.keyAt(i), this.mTorchCallbackMap.valueAt(i), id, status);
                    }
                }
            }
        }

        public void registerAvailabilityCallback(AvailabilityCallback callback, Executor executor) {
            synchronized (this.mLock) {
                connectCameraServiceLocked();
                if (this.mCallbackMap.put(callback, executor) == null) {
                    updateCallbackLocked(callback, executor);
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

        public void registerTorchCallback(TorchCallback callback, Executor executor) {
            synchronized (this.mLock) {
                connectCameraServiceLocked();
                if (this.mTorchCallbackMap.put(callback, executor) == null) {
                    updateTorchCallbackLocked(callback, executor);
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
            if (!this.mCallbackMap.isEmpty() || !this.mTorchCallbackMap.isEmpty()) {
                try {
                    this.mScheduler.schedule(new Runnable() {
                        public final void run() {
                            CameraManager.CameraManagerGlobal.lambda$scheduleCameraServiceReconnectionLocked$2(CameraManager.CameraManagerGlobal.this);
                        }
                    }, 1000, TimeUnit.MILLISECONDS);
                } catch (RejectedExecutionException e) {
                    Log.e(TAG, "Failed to schedule camera service re-connect: " + e);
                }
            }
        }

        public static /* synthetic */ void lambda$scheduleCameraServiceReconnectionLocked$2(CameraManagerGlobal cameraManagerGlobal) {
            if (cameraManagerGlobal.getCameraService() == null) {
                synchronized (cameraManagerGlobal.mLock) {
                    cameraManagerGlobal.scheduleCameraServiceReconnectionLocked();
                }
            }
        }

        public void binderDied() {
            synchronized (this.mLock) {
                if (this.mCameraService != null) {
                    this.mCameraService = null;
                    for (int i = 0; i < this.mDeviceStatus.size(); i++) {
                        onStatusChangedLocked(0, this.mDeviceStatus.keyAt(i));
                    }
                    for (int i2 = 0; i2 < this.mTorchStatus.size(); i2++) {
                        onTorchStatusChangedLocked(0, this.mTorchStatus.keyAt(i2));
                    }
                    scheduleCameraServiceReconnectionLocked();
                }
            }
        }

        private int getCustDeviceSize(int deviceSize) {
            IHwCameraUtil hwCameraUtil = HwFrameworkFactory.getHwCameraUtil();
            if (hwCameraUtil == null || !hwCameraUtil.needHideAuxCamera(deviceSize)) {
                return deviceSize;
            }
            Log.i(TAG, "hide aux camera.");
            return deviceSize - 1;
        }

        private boolean isIllegalAccessAuxCamera(int deviceSize, String cameraId) {
            IHwCameraUtil hwCameraUtil = HwFrameworkFactory.getHwCameraUtil();
            if (hwCameraUtil == null || !hwCameraUtil.isIllegalAccessAuxCamera(this.mDeviceStatus.size(), cameraId)) {
                return false;
            }
            Log.i(TAG, "illegalAccessAuxCamera.");
            return true;
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
        CameraManagerGlobal.get().registerAvailabilityCallback(callback, CameraDeviceImpl.checkAndWrapHandler(handler));
    }

    public void registerAvailabilityCallback(Executor executor, AvailabilityCallback callback) {
        if (executor != null) {
            CameraManagerGlobal.get().registerAvailabilityCallback(callback, executor);
            return;
        }
        throw new IllegalArgumentException("executor was null");
    }

    public void unregisterAvailabilityCallback(AvailabilityCallback callback) {
        CameraManagerGlobal.get().unregisterAvailabilityCallback(callback);
    }

    public void registerTorchCallback(TorchCallback callback, Handler handler) {
        CameraManagerGlobal.get().registerTorchCallback(callback, CameraDeviceImpl.checkAndWrapHandler(handler));
    }

    public void registerTorchCallback(Executor executor, TorchCallback callback) {
        if (executor != null) {
            CameraManagerGlobal.get().registerTorchCallback(callback, executor);
            return;
        }
        throw new IllegalArgumentException("executor was null");
    }

    public void unregisterTorchCallback(TorchCallback callback) {
        CameraManagerGlobal.get().unregisterTorchCallback(callback);
    }

    public CameraCharacteristics getCameraCharacteristics(String cameraId) throws CameraAccessException {
        CameraCharacteristics characteristics = null;
        if (!CameraManagerGlobal.sCameraServiceDisabled) {
            synchronized (this.mLock) {
                ICameraService cameraService = CameraManagerGlobal.get().getCameraService();
                if (cameraService != null) {
                    try {
                        if (!supportsCamera2ApiLocked(cameraId)) {
                            int id = Integer.parseInt(cameraId);
                            characteristics = LegacyMetadataMapper.createCharacteristics(cameraService.getLegacyParameters(id), cameraService.getCameraInfo(id));
                        } else {
                            characteristics = new CameraCharacteristics(cameraService.getCameraCharacteristics(cameraId));
                        }
                    } catch (ServiceSpecificException e) {
                        throwAsPublicException(e);
                    } catch (RemoteException e2) {
                        throw new CameraAccessException(2, "Camera service is currently unavailable", e2);
                    }
                } else {
                    throw new CameraAccessException(2, "Camera service is currently unavailable");
                }
            }
            return characteristics;
        }
        throw new IllegalArgumentException("No cameras available on device");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:37:0x00d2, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x00d4, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00d5, code lost:
        r6 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00ec, code lost:
        throw new java.lang.IllegalArgumentException("Expected cameraId to be numeric, but it was: " + r8);
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [B:23:0x00a9, B:28:0x00bb] */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x0107 A[Catch:{ ServiceSpecificException -> 0x00fe, RemoteException -> 0x00ed, all -> 0x014b }] */
    /* JADX WARNING: Removed duplicated region for block: B:72:0x013e A[Catch:{ ServiceSpecificException -> 0x00fe, RemoteException -> 0x00ed, all -> 0x014b }] */
    private CameraDevice openCameraDeviceUserAsync(String cameraId, CameraDevice.StateCallback callback, Executor executor, int uid) throws CameraAccessException {
        CameraDeviceUserShim cameraDeviceUserShim;
        String str = cameraId;
        CameraCharacteristics characteristics = getCameraCharacteristics(cameraId);
        synchronized (this.mLock) {
            ICameraDeviceUser cameraUser = null;
            try {
                CameraDeviceImpl cameraDeviceImpl = new CameraDeviceImpl(str, callback, executor, characteristics, this.mContext.getApplicationInfo().targetSdkVersion);
                CameraDeviceImpl deviceImpl = cameraDeviceImpl;
                ICameraDeviceCallbacks callbacks = deviceImpl.getCallbacks();
                try {
                    if (supportsCamera2ApiLocked(cameraId)) {
                        if (!HwSystemManager.allowOp(1024)) {
                            throwAsPublicException(new ServiceSpecificException(6));
                        }
                        Log.i(TAG, "open camera: " + str + ", package name: " + this.mContext.getOpPackageName());
                        boolean z = false;
                        HwSystemManager.notifyBackgroundMgr(this.mContext.getOpPackageName(), Binder.getCallingPid(), Binder.getCallingUid(), 0, 1);
                        IHwCameraUtil hwCameraUtil = HwFrameworkFactory.getHwCameraUtil();
                        if (hwCameraUtil != null) {
                            if (((Integer) characteristics.get(CameraCharacteristics.LENS_FACING)).intValue() == 0) {
                                z = true;
                            }
                            hwCameraUtil.notifySurfaceFlingerCameraStatus(z, true);
                        } else {
                            Log.e(TAG, "HwFrameworkFactory.getHwCameraUtil is NULL");
                        }
                        ICameraService cameraService = CameraManagerGlobal.get().getCameraService();
                        if (cameraService != null) {
                            cameraDeviceUserShim = cameraService.connectDevice(callbacks, str, this.mContext.getOpPackageName(), uid);
                        } else {
                            int i = uid;
                            throw new ServiceSpecificException(4, "Camera service is currently unavailable");
                        }
                    } else {
                        int i2 = uid;
                        int id = Integer.parseInt(cameraId);
                        Log.i(TAG, "Using legacy camera HAL.");
                        cameraDeviceUserShim = CameraDeviceUserShim.connectBinderShim(callbacks, id);
                    }
                    cameraUser = cameraDeviceUserShim;
                } catch (ServiceSpecificException e) {
                    e = e;
                    int i3 = uid;
                    if (e.errorCode == 9) {
                        if (!(e.errorCode == 7 || e.errorCode == 8 || e.errorCode == 6 || e.errorCode == 4)) {
                            if (e.errorCode != 10) {
                                throwAsPublicException(e);
                                deviceImpl.setRemoteDevice(cameraUser);
                                CameraDevice device = deviceImpl;
                                return device;
                            }
                        }
                        deviceImpl.setRemoteFailure(e);
                        if (e.errorCode == 6 || e.errorCode == 4 || e.errorCode == 7) {
                            throwAsPublicException(e);
                        }
                        deviceImpl.setRemoteDevice(cameraUser);
                        CameraDevice device2 = deviceImpl;
                        return device2;
                    }
                    throw new AssertionError("Should've gone down the shim path");
                } catch (RemoteException e2) {
                    int i4 = uid;
                    ServiceSpecificException sse = new ServiceSpecificException(4, "Camera service is currently unavailable");
                    deviceImpl.setRemoteFailure(sse);
                    throwAsPublicException(sse);
                    deviceImpl.setRemoteDevice(cameraUser);
                    CameraDevice device22 = deviceImpl;
                    return device22;
                } catch (Throwable th) {
                    e = th;
                    throw e;
                }
                deviceImpl.setRemoteDevice(cameraUser);
                CameraDevice device222 = deviceImpl;
                return device222;
            } catch (Throwable th2) {
                e = th2;
                int i5 = uid;
                throw e;
            }
        }
    }

    public void openCamera(String cameraId, CameraDevice.StateCallback callback, Handler handler) throws CameraAccessException {
        openCameraForUid(cameraId, callback, CameraDeviceImpl.checkAndWrapHandler(handler), -1);
    }

    public void openCamera(String cameraId, Executor executor, CameraDevice.StateCallback callback) throws CameraAccessException {
        if (executor != null) {
            openCameraForUid(cameraId, callback, executor, -1);
            return;
        }
        throw new IllegalArgumentException("executor was null");
    }

    public void openCameraForUid(String cameraId, CameraDevice.StateCallback callback, Executor executor, int clientUid) throws CameraAccessException {
        if (cameraId == null) {
            throw new IllegalArgumentException("cameraId was null");
        } else if (callback == null) {
            throw new IllegalArgumentException("callback was null");
        } else if (!CameraManagerGlobal.sCameraServiceDisabled) {
            openCameraDeviceUserAsync(cameraId, callback, executor, clientUid);
        } else {
            throw new IllegalArgumentException("No cameras available on device");
        }
    }

    public void setTorchMode(String cameraId, boolean enabled) throws CameraAccessException {
        if (!CameraManagerGlobal.sCameraServiceDisabled) {
            CameraManagerGlobal.get().setTorchMode(cameraId, enabled);
            return;
        }
        throw new IllegalArgumentException("No cameras available on device");
    }

    public static void throwAsPublicException(Throwable t) throws CameraAccessException {
        int reason;
        if (t instanceof ServiceSpecificException) {
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
