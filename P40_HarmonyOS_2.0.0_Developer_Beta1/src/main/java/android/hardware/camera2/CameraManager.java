package android.hardware.camera2;

import android.camera.IHwCameraUtil;
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
import android.media.HwMediaFactory;
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
import android.util.Size;
import android.view.Display;
import android.view.WindowManager;
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

    private Size getDisplaySize() {
        Size ret = new Size(0, 0);
        try {
            Display display = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
            int width = display.getWidth();
            int height = display.getHeight();
            if (height > width) {
                height = width;
                width = display.getHeight();
            }
            return new Size(width, height);
        } catch (Exception e) {
            Log.e(TAG, "getDisplaySize Failed. " + e.toString());
            return ret;
        }
    }

    public CameraCharacteristics getCameraCharacteristics(String cameraId) throws CameraAccessException {
        CameraCharacteristics characteristics = null;
        if (!CameraManagerGlobal.sCameraServiceDisabled) {
            synchronized (this.mLock) {
                ICameraService cameraService = CameraManagerGlobal.get().getCameraService();
                if (cameraService != null) {
                    try {
                        Size displaySize = getDisplaySize();
                        if (isHiddenPhysicalCamera(cameraId) || supportsCamera2ApiLocked(cameraId)) {
                            CameraMetadataNative info = cameraService.getCameraCharacteristics(cameraId);
                            if (info != null) {
                                try {
                                    info.setCameraId(Integer.parseInt(cameraId));
                                } catch (NumberFormatException e) {
                                    Log.e(TAG, "Failed to parse camera Id " + cameraId + " to integer");
                                }
                                info.setDisplaySize(displaySize);
                                characteristics = new CameraCharacteristics(info);
                            }
                        } else {
                            int id = Integer.parseInt(cameraId);
                            characteristics = LegacyMetadataMapper.createCharacteristics(cameraService.getLegacyParameters(id), cameraService.getCameraInfo(id), id, displaySize);
                        }
                    } catch (ServiceSpecificException e2) {
                        throwAsPublicException(e2);
                    } catch (RemoteException e3) {
                        throw new CameraAccessException(2, "Camera service is currently unavailable", e3);
                    }
                } else {
                    throw new CameraAccessException(2, "Camera service is currently unavailable");
                }
            }
            return characteristics;
        }
        throw new IllegalArgumentException("No cameras available on device");
    }

    /* JADX WARNING: Removed duplicated region for block: B:37:0x00ec A[Catch:{ ServiceSpecificException -> 0x00e3, RemoteException -> 0x00d2, all -> 0x0130 }] */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x0123 A[Catch:{ ServiceSpecificException -> 0x00e3, RemoteException -> 0x00d2, all -> 0x0130 }] */
    private CameraDevice openCameraDeviceUserAsync(String cameraId, CameraDevice.StateCallback callback, Executor executor, int uid) throws CameraAccessException {
        ServiceSpecificException e;
        CameraCharacteristics characteristics = getCameraCharacteristics(cameraId);
        synchronized (this.mLock) {
            ICameraDeviceUser cameraUser = null;
            try {
                CameraDeviceImpl deviceImpl = new CameraDeviceImpl(cameraId, callback, executor, characteristics, this.mContext.getApplicationInfo().targetSdkVersion);
                ICameraDeviceCallbacks callbacks = deviceImpl.getCallbacks();
                try {
                    if (supportsCamera2ApiLocked(cameraId)) {
                        if (!HwSystemManager.allowOp(1024)) {
                            throwAsPublicException(new ServiceSpecificException(6));
                        }
                        Log.i(TAG, "open camera: " + cameraId + ", package name: " + this.mContext.getOpPackageName());
                        HwSystemManager.notifyBackgroundMgr(this.mContext.getOpPackageName(), Binder.getCallingPid(), Binder.getCallingUid(), 0, 1);
                        ICameraService cameraService = CameraManagerGlobal.get().getCameraService();
                        if (cameraService != null) {
                            try {
                                cameraUser = cameraService.connectDevice(callbacks, cameraId, this.mContext.getOpPackageName(), uid);
                            } catch (ServiceSpecificException e2) {
                                e = e2;
                                if (e.errorCode == 9) {
                                }
                            } catch (RemoteException e3) {
                                ServiceSpecificException sse = new ServiceSpecificException(4, "Camera service is currently unavailable");
                                deviceImpl.setRemoteFailure(sse);
                                throwAsPublicException(sse);
                                deviceImpl.setRemoteDevice(cameraUser);
                                return deviceImpl;
                            }
                        } else {
                            throw new ServiceSpecificException(4, "Camera service is currently unavailable");
                        }
                    } else {
                        try {
                            int id = Integer.parseInt(cameraId);
                            Log.i(TAG, "Using legacy camera HAL.");
                            cameraUser = CameraDeviceUserShim.connectBinderShim(callbacks, id, getDisplaySize());
                        } catch (NumberFormatException e4) {
                            throw new IllegalArgumentException("Expected cameraId to be numeric, but it was: " + cameraId);
                        }
                    }
                } catch (ServiceSpecificException e5) {
                    e = e5;
                    if (e.errorCode == 9) {
                        if (!(e.errorCode == 7 || e.errorCode == 8 || e.errorCode == 6 || e.errorCode == 4)) {
                            if (e.errorCode != 10) {
                                throwAsPublicException(e);
                                deviceImpl.setRemoteDevice(cameraUser);
                                return deviceImpl;
                            }
                        }
                        deviceImpl.setRemoteFailure(e);
                        if (e.errorCode == 6 || e.errorCode == 4 || e.errorCode == 7) {
                            throwAsPublicException(e);
                        }
                        deviceImpl.setRemoteDevice(cameraUser);
                        return deviceImpl;
                    }
                    throw new AssertionError("Should've gone down the shim path");
                } catch (RemoteException e6) {
                    ServiceSpecificException sse2 = new ServiceSpecificException(4, "Camera service is currently unavailable");
                    deviceImpl.setRemoteFailure(sse2);
                    throwAsPublicException(sse2);
                    deviceImpl.setRemoteDevice(cameraUser);
                    return deviceImpl;
                } catch (Throwable th) {
                    e = th;
                    throw e;
                }
                deviceImpl.setRemoteDevice(cameraUser);
                return deviceImpl;
            } catch (Throwable th2) {
                e = th2;
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

    public static abstract class AvailabilityCallback {
        public void onCameraAvailable(String cameraId) {
        }

        public void onCameraUnavailable(String cameraId) {
        }

        public void onCameraAccessPrioritiesChanged() {
        }
    }

    public static abstract class TorchCallback {
        public void onTorchModeUnavailable(String cameraId) {
        }

        public void onTorchModeChanged(String cameraId, boolean enabled) {
        }
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
                case 5:
                default:
                    reason = 3;
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

    public static boolean isHiddenPhysicalCamera(String cameraId) {
        try {
            ICameraService cameraService = CameraManagerGlobal.get().getCameraService();
            if (cameraService == null) {
                return false;
            }
            return cameraService.isHiddenPhysicalCamera(cameraId);
        } catch (RemoteException e) {
            return false;
        }
    }

    /* access modifiers changed from: private */
    public static final class CameraManagerGlobal extends ICameraServiceListener.Stub implements IBinder.DeathRecipient {
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

        @Override // android.hardware.ICameraServiceListener.Stub, android.os.IInterface
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
                            CameraStatus[] cameraStatuses = cameraService.addListener(this);
                            if (cameraStatuses != null) {
                                for (CameraStatus c : cameraStatuses) {
                                    onStatusChangedLocked(c.status, c.cameraId);
                                }
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

        /* JADX INFO: Multiple debug info for r4v2 java.lang.String[]: [D('i' int), D('cameraIds' java.lang.String[])] */
        public String[] getCameraIdList() {
            String[] cameraIds;
            synchronized (this.mLock) {
                connectCameraServiceLocked();
                int idCount = 0;
                int deviceStatusSize = getCustDeviceSize(this.mDeviceStatus.size());
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
                /* class android.hardware.camera2.CameraManager.CameraManagerGlobal.AnonymousClass1 */

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
            if (status == -2 || status == 0 || status == 1 || status == 2) {
                return true;
            }
            return false;
        }

        private boolean validTorchStatus(int status) {
            if (status == 0 || status == 1 || status == 2) {
                return true;
            }
            return false;
        }

        private void postSingleAccessPriorityChangeUpdate(final AvailabilityCallback callback, Executor executor) {
            long ident = Binder.clearCallingIdentity();
            try {
                executor.execute(new Runnable() {
                    /* class android.hardware.camera2.CameraManager.CameraManagerGlobal.AnonymousClass2 */

                    @Override // java.lang.Runnable
                    public void run() {
                        callback.onCameraAccessPrioritiesChanged();
                    }
                });
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        private void postSingleUpdate(final AvailabilityCallback callback, Executor executor, final String id, int status) {
            if (isAvailable(status)) {
                long ident = Binder.clearCallingIdentity();
                try {
                    executor.execute(new Runnable() {
                        /* class android.hardware.camera2.CameraManager.CameraManagerGlobal.AnonymousClass3 */

                        @Override // java.lang.Runnable
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
                        /* class android.hardware.camera2.CameraManager.CameraManagerGlobal.AnonymousClass4 */

                        @Override // java.lang.Runnable
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
            if (status == 1 || status == 2) {
                long ident = Binder.clearCallingIdentity();
                try {
                    executor.execute(new Runnable(id, status) {
                        /* class android.hardware.camera2.$$Lambda$CameraManager$CameraManagerGlobal$CONvadOBAEkcHSpx8j61v67qRGM */
                        private final /* synthetic */ String f$1;
                        private final /* synthetic */ int f$2;

                        {
                            this.f$1 = r2;
                            this.f$2 = r3;
                        }

                        @Override // java.lang.Runnable
                        public final void run() {
                            CameraManager.CameraManagerGlobal.lambda$postSingleTorchUpdate$0(CameraManager.TorchCallback.this, this.f$1, this.f$2);
                        }
                    });
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            } else {
                long ident2 = Binder.clearCallingIdentity();
                try {
                    executor.execute(new Runnable(id) {
                        /* class android.hardware.camera2.$$Lambda$CameraManager$CameraManagerGlobal$6Ptxoe4wF_VCkE_pml8t66mklao */
                        private final /* synthetic */ String f$1;

                        {
                            this.f$1 = r2;
                        }

                        @Override // java.lang.Runnable
                        public final void run() {
                            CameraManager.TorchCallback.this.onTorchModeUnavailable(this.f$1);
                        }
                    });
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
                    Log.e(TAG, String.format("Ignoring invalid device %s status 0x%x", id, Integer.valueOf(status)));
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
                    Log.e(TAG, String.format("Ignoring invalid device %s torch status 0x%x", id, Integer.valueOf(status)));
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

        @Override // android.hardware.ICameraServiceListener
        public void onStatusChanged(int status, String cameraId) throws RemoteException {
            synchronized (this.mLock) {
                onStatusChangedLocked(status, cameraId);
            }
        }

        @Override // android.hardware.ICameraServiceListener
        public void onTorchStatusChanged(int status, String cameraId) throws RemoteException {
            synchronized (this.mLock) {
                onTorchStatusChangedLocked(status, cameraId);
            }
        }

        @Override // android.hardware.ICameraServiceListener
        public void onCameraAccessPrioritiesChanged() {
            synchronized (this.mLock) {
                int callbackCount = this.mCallbackMap.size();
                for (int i = 0; i < callbackCount; i++) {
                    postSingleAccessPriorityChangeUpdate(this.mCallbackMap.keyAt(i), this.mCallbackMap.valueAt(i));
                }
            }
        }

        private void scheduleCameraServiceReconnectionLocked() {
            if (!this.mCallbackMap.isEmpty() || !this.mTorchCallbackMap.isEmpty()) {
                try {
                    this.mScheduler.schedule(new Runnable() {
                        /* class android.hardware.camera2.$$Lambda$CameraManager$CameraManagerGlobal$w1y8myi6vgxAcTEs8WArINN3R0 */

                        @Override // java.lang.Runnable
                        public final void run() {
                            CameraManager.CameraManagerGlobal.this.lambda$scheduleCameraServiceReconnectionLocked$2$CameraManager$CameraManagerGlobal();
                        }
                    }, 1000, TimeUnit.MILLISECONDS);
                } catch (RejectedExecutionException e) {
                    Log.e(TAG, "Failed to schedule camera service re-connect: " + e);
                }
            }
        }

        public /* synthetic */ void lambda$scheduleCameraServiceReconnectionLocked$2$CameraManager$CameraManagerGlobal() {
            if (getCameraService() == null) {
                synchronized (this.mLock) {
                    scheduleCameraServiceReconnectionLocked();
                }
            }
        }

        @Override // android.os.IBinder.DeathRecipient
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
            IHwCameraUtil hwCameraUtil = HwMediaFactory.getHwCameraUtil();
            if (hwCameraUtil != null) {
                return hwCameraUtil.filterVirtualCamera(this.mDeviceStatus, deviceSize);
            }
            return deviceSize;
        }

        private boolean isIllegalAccessAuxCamera(int deviceSize, String cameraId) {
            IHwCameraUtil hwCameraUtil = HwMediaFactory.getHwCameraUtil();
            if (hwCameraUtil == null || !hwCameraUtil.isIllegalAccessAuxCamera(this.mDeviceStatus.size(), cameraId)) {
                return false;
            }
            Log.i(TAG, "illegalAccessAuxCamera.");
            return true;
        }
    }
}
