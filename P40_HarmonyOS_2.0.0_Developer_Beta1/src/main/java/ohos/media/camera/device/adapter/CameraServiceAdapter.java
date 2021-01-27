package ohos.media.camera.device.adapter;

import android.hardware.CameraStatus;
import android.hardware.ICameraService;
import android.hardware.ICameraServiceListener;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.impl.CameraMetadataNative;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.ServiceSpecificException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import ohos.media.camera.exception.ConnectException;
import ohos.media.camera.exception.ExceptionTransfer;
import ohos.media.camera.params.adapter.CameraAbilityMaker;
import ohos.media.camera.params.adapter.StaticCameraCharacteristics;
import ohos.media.camera.zidl.CameraAbilityNative;
import ohos.media.camera.zidl.ICamera;
import ohos.media.camera.zidl.ICameraCallback;
import ohos.media.camera.zidl.ICameraService;
import ohos.media.camera.zidl.ICameraServiceStatus;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class CameraServiceAdapter extends ICameraServiceListener.Stub implements ICameraService, IBinder.DeathRecipient {
    private static final String CAMERA_SERVICE_BINDER_IDENTIFIER = "media.camera";
    private static final Logger LOGGER = LoggerFactory.getCameraLogger(CameraServiceAdapter.class);
    private static final int RPC_FLAG_NORMAL = 0;
    private final Map<String, CameraAbilityNative> cameraAbilityCacheMap = new ConcurrentHashMap();
    private final Map<String, StaticCameraCharacteristics> cameraCharacteristicsCacheMap = new ConcurrentHashMap();
    private volatile android.hardware.ICameraService cameraService;
    private final ICameraServiceStatus cameraServiceStatus;

    public void onCameraAccessPrioritiesChanged() throws RemoteException {
    }

    public CameraServiceAdapter(ICameraServiceStatus iCameraServiceStatus) {
        this.cameraServiceStatus = iCameraServiceStatus;
    }

    @Override // ohos.media.camera.zidl.ICameraService
    public void initialize() throws ConnectException {
        initCameraService();
        initCameraStatus();
    }

    @Override // ohos.media.camera.zidl.ICameraService
    public CameraAbilityNative getCameraAbility(String str) throws ConnectException {
        try {
            return getCameraAbilityCached(str).orElse(null);
        } catch (RemoteException e) {
            LOGGER.error("Call getCameraAbility failed, exception: %{public}s", e.toString());
            throw new ConnectException("Camera service is currently unavailable");
        } catch (ServiceSpecificException e2) {
            LOGGER.error("Call getCameraAbility failed for id %{public}s, exception: %{public}s", str, e2.toString());
            throw new ConnectException("Invalid cameraID for this camera service");
        }
    }

    private Optional<CameraAbilityNative> getCameraAbilityCached(String str) throws RemoteException {
        CameraAbilityNative cameraAbilityNative = this.cameraAbilityCacheMap.get(str);
        if (cameraAbilityNative != null) {
            LOGGER.debug("CameraAbility cache hit, cameraId: %{public}s", str);
            return Optional.of(cameraAbilityNative);
        }
        initCameraCache(str);
        return Optional.ofNullable(this.cameraAbilityCacheMap.get(str));
    }

    private void initCameraCache(String str) throws RemoteException {
        CameraMetadataNative cameraCharacteristics = this.cameraService.getCameraCharacteristics(str);
        if (cameraCharacteristics == null) {
            LOGGER.error("Failed to get CameraCharacteristics for cameraId: %{public}s", str);
            return;
        }
        CameraCharacteristics cameraCharacteristics2 = new CameraCharacteristics(cameraCharacteristics);
        Optional<CameraAbilityNative> makeCameraAbility = new CameraAbilityMaker(cameraCharacteristics2, str).makeCameraAbility();
        if (!makeCameraAbility.isPresent()) {
            LOGGER.warn("There is no camera ability for cameraId: %{public}s", str);
            return;
        }
        this.cameraAbilityCacheMap.put(str, makeCameraAbility.get());
        this.cameraCharacteristicsCacheMap.put(str, new StaticCameraCharacteristics(cameraCharacteristics2));
    }

    @Override // ohos.media.camera.zidl.ICameraService
    public ICamera createCamera(String str, ICameraCallback iCameraCallback, String str2) throws ConnectException {
        RemoteException e;
        CameraAdapter cameraAdapter = null;
        try {
            Optional<CameraAbilityNative> cameraAbilityCached = getCameraAbilityCached(str);
            Optional<StaticCameraCharacteristics> cameraCharacteristicsCached = getCameraCharacteristicsCached(str);
            if (!cameraAbilityCached.isPresent() || !cameraCharacteristicsCached.isPresent()) {
                return null;
            }
            CameraAdapter cameraAdapter2 = new CameraAdapter(str, cameraAbilityCached.get(), cameraCharacteristicsCached.get(), iCameraCallback);
            try {
                cameraAdapter2.setCameraDeviceUser(this.cameraService.connectDevice(cameraAdapter2.getCallbacks(), str, str2, -1));
                return cameraAdapter2;
            } catch (RemoteException | ServiceSpecificException e2) {
                e = e2;
                cameraAdapter = cameraAdapter2;
                ExceptionTransfer.trans2AccessException(e);
                return cameraAdapter;
            }
        } catch (RemoteException | ServiceSpecificException e3) {
            e = e3;
            ExceptionTransfer.trans2AccessException(e);
            return cameraAdapter;
        }
    }

    private Optional<StaticCameraCharacteristics> getCameraCharacteristicsCached(String str) throws RemoteException {
        StaticCameraCharacteristics staticCameraCharacteristics = this.cameraCharacteristicsCacheMap.get(str);
        if (staticCameraCharacteristics != null) {
            LOGGER.debug("StaticCameraCharacteristics cache hit, cameraId: %{public}s", str);
            return Optional.of(staticCameraCharacteristics);
        }
        initCameraCache(str);
        return Optional.of(this.cameraCharacteristicsCacheMap.get(str));
    }

    public void onStatusChanged(int i, String str) throws RemoteException {
        int convertCameraStatus = convertCameraStatus(i);
        if (convertCameraStatus == 3) {
            this.cameraAbilityCacheMap.remove(str);
            this.cameraCharacteristicsCacheMap.remove(str);
        }
        this.cameraServiceStatus.onAvailabilityStatusChanged(str, convertCameraStatus);
    }

    public void onTorchStatusChanged(int i, String str) throws RemoteException {
        this.cameraServiceStatus.onFlashlightStatusChanged(str, convertTorchStatus(i));
    }

    @Override // android.os.IBinder.DeathRecipient
    public void binderDied() {
        LOGGER.warn("Binder died, need reconnection", new Object[0]);
        this.cameraServiceStatus.onCameraServiceDied();
    }

    private void initCameraService() throws ConnectException {
        IBinder service = ServiceManager.getService(CAMERA_SERVICE_BINDER_IDENTIFIER);
        if (service != null) {
            try {
                service.linkToDeath(this, 0);
                this.cameraService = ICameraService.Stub.asInterface(service);
                try {
                    CameraMetadataNative.setupGlobalVendorTagDescriptor();
                } catch (ServiceSpecificException e) {
                    LOGGER.error("Failed to setupGlobalVendorTagDescriptor, exception: %{public}s", e.toString());
                    throw new ConnectException("Failed to setupGlobalVendorTagDescriptor");
                }
            } catch (RemoteException e2) {
                LOGGER.error("Camera service link to death error, exception: %{public}s", e2.toString());
                throw new ConnectException("Camera service link to death error");
            }
        } else {
            LOGGER.error("Connect to camera service binder error", new Object[0]);
            throw new ConnectException("Connect to camera service binder error");
        }
    }

    private void initCameraStatus() throws ConnectException {
        try {
            CameraStatus[] addListener = this.cameraService.addListener(this);
            HashMap hashMap = new HashMap();
            for (CameraStatus cameraStatus : addListener) {
                if (cameraStatus.cameraId == null) {
                    LOGGER.warn("cameraId is null, continue", new Object[0]);
                } else {
                    hashMap.put(cameraStatus.cameraId, Integer.valueOf(convertCameraStatus(cameraStatus.status)));
                }
            }
            this.cameraServiceStatus.onCameraServiceInitialized(hashMap);
        } catch (RemoteException | ServiceSpecificException e) {
            LOGGER.error("Call addListener failed, exception: %{public}s", e.toString());
            throw new ConnectException("Add camera service listener failed");
        }
    }

    private int convertCameraStatus(int i) {
        int i2;
        if (i == -2) {
            i2 = 0;
        } else if (i == 0) {
            i2 = 3;
        } else if (i == 1) {
            i2 = 1;
        } else if (i != 2) {
            LOGGER.warn("Camera available status is unknown", new Object[0]);
            i2 = -1;
        } else {
            i2 = 2;
        }
        LOGGER.debug("Convert camera status from %{public}d to %{public}d", Integer.valueOf(i), Integer.valueOf(i2));
        return i2;
    }

    private int convertTorchStatus(int i) {
        int i2;
        if (i == 0) {
            i2 = 0;
        } else if (i == 1) {
            i2 = 1;
        } else if (i != 2) {
            LOGGER.warn("Camera torch status is unknown", new Object[0]);
            i2 = -1;
        } else {
            i2 = 2;
        }
        LOGGER.debug("Convert torch status from %{public}d to %{public}d", Integer.valueOf(i), Integer.valueOf(i2));
        return i2;
    }
}
