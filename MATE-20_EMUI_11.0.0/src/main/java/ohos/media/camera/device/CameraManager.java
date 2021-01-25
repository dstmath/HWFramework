package ohos.media.camera.device;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import ohos.app.Context;
import ohos.eventhandler.EventHandler;
import ohos.media.camera.device.CameraManager;
import ohos.media.camera.device.adapter.CameraServiceAdapter;
import ohos.media.camera.device.adapter.utils.SystemSettings;
import ohos.media.camera.device.impl.CameraAbilityImpl;
import ohos.media.camera.device.impl.CameraImpl;
import ohos.media.camera.device.impl.CameraInfoImpl;
import ohos.media.camera.exception.ConnectException;
import ohos.media.camera.params.adapter.InnerPropertyKey;
import ohos.media.camera.zidl.CameraAbilityNative;
import ohos.media.camera.zidl.ICameraService;
import ohos.media.camera.zidl.ICameraServiceStatus;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;
import ohos.media.utils.trace.Tracer;
import ohos.media.utils.trace.TracerFactory;

public class CameraManager {
    private static final Logger LOGGER = LoggerFactory.getCameraLogger(CameraManager.class);
    private static CameraAbilityAssistant cameraAbilityAssistant = CameraAbilityAssistant.INSTANCE;
    private static CameraDeviceAssistant cameraDeviceAssistant = CameraDeviceAssistant.INSTANCE;
    private static CameraInfoAssistant cameraInfoAssistant = CameraInfoAssistant.INSTANCE;
    private static CameraServiceAssistant cameraServiceAssistant = CameraServiceAssistant.INSTANCE;
    private static CameraManager instance;
    private final Context context;

    private CameraManager(Context context2) {
        Objects.requireNonNull(context2, "Context should not be null");
        this.context = context2;
        cameraServiceAssistant.initServiceWithGuard();
    }

    public static synchronized CameraManager getInstance(Context context2) {
        CameraManager cameraManager;
        synchronized (CameraManager.class) {
            if (instance == null) {
                instance = new CameraManager(context2);
            }
            cameraManager = instance;
        }
        return cameraManager;
    }

    public String[] getCameraIdList() {
        if (cameraGuard()) {
            return cameraInfoAssistant.getCameraIdList();
        }
        LOGGER.warn("Camera service is not initialized", new Object[0]);
        return new String[0];
    }

    public CameraInfoImpl getCameraInfo(String str) {
        if (cameraGuard()) {
            return cameraInfoAssistant.getCameraInfo(str).orElse(null);
        }
        LOGGER.warn("Camera service is not initialized", new Object[0]);
        return null;
    }

    public CameraAbilityImpl getCameraAbility(String str) {
        if (cameraGuard()) {
            return cameraAbilityAssistant.getCameraAbility(str).orElse(null);
        }
        LOGGER.warn("Camera service is not initialized", new Object[0]);
        return null;
    }

    public void registerCameraDeviceCallback(CameraDeviceCallback cameraDeviceCallback, EventHandler eventHandler) {
        Objects.requireNonNull(cameraDeviceCallback, "CameraDeviceCallback should not be null");
        Objects.requireNonNull(eventHandler, "Handler should not be null");
        if (!cameraGuard()) {
            LOGGER.warn("Camera service is not initialized", new Object[0]);
        } else {
            cameraServiceAssistant.registerCameraDeviceCallback(cameraDeviceCallback, eventHandler);
        }
    }

    public void unregisterCameraDeviceCallback(CameraDeviceCallback cameraDeviceCallback) {
        Objects.requireNonNull(cameraDeviceCallback, "CameraDeviceCallback should not be null");
        if (!cameraGuard()) {
            LOGGER.warn("Camera service is not initialized", new Object[0]);
        } else {
            cameraServiceAssistant.unregisterCameraDeviceCallback(cameraDeviceCallback);
        }
    }

    public void createCamera(String str, CameraStateCallback cameraStateCallback, EventHandler eventHandler) {
        Objects.requireNonNull(str, "Camera id should not be null");
        Objects.requireNonNull(cameraStateCallback, "CameraStateCallback should not be null");
        Objects.requireNonNull(eventHandler, "Handler should not be null");
        if (!cameraGuard()) {
            LOGGER.warn("Camera service is not initialized", new Object[0]);
            eventHandler.postTask(new Runnable(str, SystemSettings.isCameraServiceDisabled() ? -4 : -6) {
                /* class ohos.media.camera.device.$$Lambda$CameraManager$duRaA6eXLHGPL9oCi6M8STmaOI */
                private final /* synthetic */ String f$1;
                private final /* synthetic */ int f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    CameraStateCallback.this.onCreateFailed(this.f$1, this.f$2);
                }
            });
            return;
        }
        cameraDeviceAssistant.createCamera(str, cameraStateCallback, eventHandler, this.context.getBundleName());
    }

    public boolean cameraGuard() {
        if (this.context != null) {
            cameraServiceAssistant.initServiceWithGuard();
            return cameraServiceAssistant.isInitialized.get();
        }
        throw new IllegalStateException("Initialize camera manager with context first");
    }

    /* access modifiers changed from: private */
    public enum CameraServiceAssistant implements ICameraServiceStatus {
        INSTANCE;
        
        private static final Logger LOGGER = LoggerFactory.getCameraLogger(CameraServiceAssistant.class);
        private static final int REINITIALIZE_CAMERA_SERVICE_DELAY_MS = 1000;
        private static final int REINITIALIZE_CAMERA_SERVICE_MAX_COUNT = 20;
        private static final Tracer TRACER = TracerFactory.getCameraTracer();
        private volatile ICameraService cameraService;
        private final Map<String, Integer> cameraTorchStatusMap = new ConcurrentHashMap();
        private final Map<CameraDeviceCallback, EventHandler> deviceCallbackMap = new ConcurrentHashMap();
        private final AtomicBoolean isInitialized = new AtomicBoolean(false);
        private final AtomicBoolean isReinitializing = new AtomicBoolean(false);
        private final ScheduledExecutorService reinitializeScheduler = Executors.newScheduledThreadPool(1);
        private final AtomicInteger reinitializeTimeCount = new AtomicInteger(0);

        private boolean isTorchAvailable(int i) {
            return i == 1 || i == 2;
        }

        private CameraServiceAssistant() {
        }

        public synchronized void initServiceWithGuard() {
            if (this.isInitialized.get()) {
                LOGGER.debug("Camera service is already initialized", new Object[0]);
            } else if (this.isReinitializing.get()) {
                LOGGER.warn("Camera service is reinitializing", new Object[0]);
            } else if (SystemSettings.isCameraServiceDisabled()) {
                LOGGER.warn("Camera service is disabled", new Object[0]);
            } else {
                initialize();
            }
        }

        /* access modifiers changed from: private */
        public void initialize() {
            LOGGER.info("Begin initialize camera service", new Object[0]);
            try {
                TRACER.startTrace("init-camera-service");
                if (this.cameraService == null) {
                    this.cameraService = new CameraServiceAdapter(this);
                }
                this.cameraService.initialize();
                TRACER.finishTrace("init-camera-service");
            } catch (ConnectException e) {
                LOGGER.error("Failed to initialize the camera service, exception: %{public}s", e.getMessage());
                scheduleReinitializeCameraService();
            }
        }

        private void scheduleReinitializeCameraService() {
            LOGGER.warn("Schedule to reinitialize the camera service start", new Object[0]);
            this.isReinitializing.set(true);
            if (this.reinitializeTimeCount.get() > 20) {
                LOGGER.error("Failed to reinitialize camera service, time out of max count %{public}d", 20);
                resetReinitializeTimeCount();
                this.isReinitializing.set(false);
            } else if (this.deviceCallbackMap.isEmpty()) {
                LOGGER.warn("No need to reinitialize camera service, there is no device callback", new Object[0]);
                resetReinitializeTimeCount();
                this.isReinitializing.set(false);
            } else {
                try {
                    this.reinitializeScheduler.schedule(new Runnable() {
                        /* class ohos.media.camera.device.$$Lambda$CameraManager$CameraServiceAssistant$hKle37_sJPAPMnjBDnmGGaR7YM */

                        @Override // java.lang.Runnable
                        public final void run() {
                            CameraManager.CameraServiceAssistant.this.initialize();
                        }
                    }, 1000, TimeUnit.MILLISECONDS);
                    LOGGER.warn("Schedule to reinitialize the camera service in 1 second, times: %{public}d", Integer.valueOf(this.reinitializeTimeCount.incrementAndGet()));
                } catch (RejectedExecutionException e) {
                    LOGGER.error("Failed to schedule reinitialize camera service, exception: %{public}s", e.getMessage());
                }
            }
        }

        private void resetReinitializeTimeCount() {
            this.reinitializeTimeCount.set(0);
        }

        public ICameraService getCameraService() {
            return this.cameraService;
        }

        public void registerCameraDeviceCallback(CameraDeviceCallback cameraDeviceCallback, EventHandler eventHandler) {
            LOGGER.info("Register camera device callback: %{public}s, execute instantly", cameraDeviceCallback.toString());
            this.deviceCallbackMap.put(cameraDeviceCallback, eventHandler);
            executeCallback(cameraDeviceCallback, eventHandler);
        }

        private void executeCallback(CameraDeviceCallback cameraDeviceCallback, EventHandler eventHandler) {
            String[] cameraIdList = CameraManager.cameraInfoAssistant.getCameraIdList();
            for (String str : cameraIdList) {
                Optional<CameraInfoImpl> cameraInfo = CameraManager.cameraInfoAssistant.getCameraInfo(str);
                if (cameraInfo.isPresent()) {
                    CameraInfoImpl cameraInfoImpl = cameraInfo.get();
                    emitLogicalCameraAvailabilityEvent(cameraDeviceCallback, eventHandler, str, cameraInfoImpl.isLogicalCameraAvailable());
                    for (String str2 : cameraInfoImpl.getPhysicalIdList()) {
                        emitPhysicalCameraAvailableEvent(cameraDeviceCallback, eventHandler, str2, cameraInfoImpl.isPhysicalCameraAvailable(str2));
                    }
                }
            }
        }

        private void emitLogicalCameraAvailabilityEvent(CameraDeviceCallback cameraDeviceCallback, EventHandler eventHandler, String str, boolean z) {
            LOGGER.info("Execute logical camera available callback, logicalCameraId: %{public}s, available: %{public}b", str, Boolean.valueOf(z));
            if (z) {
                eventHandler.postTask(new Runnable(str) {
                    /* class ohos.media.camera.device.$$Lambda$CameraManager$CameraServiceAssistant$ocm8ojuuTXrU93bOCGPxxBq6z1k */
                    private final /* synthetic */ String f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        CameraDeviceCallback.this.onCameraStatus(this.f$1, 1);
                    }
                });
            } else {
                eventHandler.postTask(new Runnable(str) {
                    /* class ohos.media.camera.device.$$Lambda$CameraManager$CameraServiceAssistant$GxTc4cg_Bv2tUaDRGsXf6QmJAdw */
                    private final /* synthetic */ String f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        CameraDeviceCallback.this.onCameraStatus(this.f$1, 0);
                    }
                });
            }
        }

        private void emitPhysicalCameraAvailableEvent(CameraDeviceCallback cameraDeviceCallback, EventHandler eventHandler, String str, boolean z) {
            LOGGER.info("Execute physical camera available callback, physicalCameraId: %{public}s, available: %{public}b", str, Boolean.valueOf(z));
            if (z) {
                eventHandler.postTask(new Runnable(str) {
                    /* class ohos.media.camera.device.$$Lambda$CameraManager$CameraServiceAssistant$B5cS7M8sJ8nTvh0yGkzGzOQHQD8 */
                    private final /* synthetic */ String f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        CameraDeviceCallback.this.onPhysicalCameraStatus(this.f$1, 1);
                    }
                });
            } else {
                eventHandler.postTask(new Runnable(str) {
                    /* class ohos.media.camera.device.$$Lambda$CameraManager$CameraServiceAssistant$kDLgd1YRZeh7Z6AJuQofd6LjyO0 */
                    private final /* synthetic */ String f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        CameraDeviceCallback.this.onPhysicalCameraStatus(this.f$1, 0);
                    }
                });
            }
        }

        public void unregisterCameraDeviceCallback(CameraDeviceCallback cameraDeviceCallback) {
            LOGGER.info("Unregister camera device callback: %{public}s", cameraDeviceCallback.toString());
            this.deviceCallbackMap.remove(cameraDeviceCallback);
        }

        @Override // ohos.media.camera.zidl.ICameraServiceStatus
        public void onCameraServiceInitialized(Map<String, Integer> map) {
            LOGGER.debug("onCameraServiceInitialized", new Object[0]);
            this.isInitialized.set(true);
            resetReinitializeTimeCount();
            CameraManager.cameraInfoAssistant.initCameraInfos(map);
            emitCameraAvailabilityEvents();
        }

        private void emitCameraAvailabilityEvents() {
            LOGGER.info("Start to emit availability events to all registered callbacks", new Object[0]);
            for (Map.Entry<CameraDeviceCallback, EventHandler> entry : this.deviceCallbackMap.entrySet()) {
                executeCallback(entry.getKey(), entry.getValue());
            }
        }

        @Override // ohos.media.camera.zidl.ICameraServiceStatus
        public void onAvailabilityStatusChanged(String str, int i) {
            boolean z = false;
            LOGGER.info("onAvailabilityStatusChanged cameraId: %{public}s, availabilityStatus: %{public}d", str, Integer.valueOf(i));
            if (CameraManager.cameraInfoAssistant.isLogicalCamera(str)) {
                Optional<CameraInfoImpl> cameraInfo = CameraManager.cameraInfoAssistant.getCameraInfo(str);
                if (cameraInfo.isPresent()) {
                    CameraInfoImpl cameraInfoImpl = cameraInfo.get();
                    cameraInfoImpl.setLogicalCameraAvailability(i);
                    emitLogicalCameraAvailabilityEvent(str, cameraInfoImpl.isLogicalCameraAvailable());
                }
            } else {
                CameraManager.cameraInfoAssistant.updateCameraAvailability(str, i);
                if (i == 1) {
                    z = true;
                }
                emitPhysicalCameraAvailabilityEvent(str, z);
            }
            if (i == 3) {
                CameraManager.cameraAbilityAssistant.removeCachedCameraAbility(str);
            }
        }

        private void emitLogicalCameraAvailabilityEvent(String str, boolean z) {
            for (Map.Entry<CameraDeviceCallback, EventHandler> entry : this.deviceCallbackMap.entrySet()) {
                emitLogicalCameraAvailabilityEvent(entry.getKey(), entry.getValue(), str, z);
            }
        }

        private void emitPhysicalCameraAvailabilityEvent(String str, boolean z) {
            for (Map.Entry<CameraDeviceCallback, EventHandler> entry : this.deviceCallbackMap.entrySet()) {
                CameraDeviceCallback key = entry.getKey();
                EventHandler value = entry.getValue();
                if (z) {
                    value.postTask(new Runnable(str) {
                        /* class ohos.media.camera.device.$$Lambda$CameraManager$CameraServiceAssistant$45SLwPXIHa7ocBJJEwVgcGp3PEo */
                        private final /* synthetic */ String f$1;

                        {
                            this.f$1 = r2;
                        }

                        @Override // java.lang.Runnable
                        public final void run() {
                            CameraDeviceCallback.this.onCameraStatus(this.f$1, 1);
                        }
                    });
                } else {
                    value.postTask(new Runnable(str) {
                        /* class ohos.media.camera.device.$$Lambda$CameraManager$CameraServiceAssistant$4HaJF9qaAl04xXCvJLiNDEoFOg */
                        private final /* synthetic */ String f$1;

                        {
                            this.f$1 = r2;
                        }

                        @Override // java.lang.Runnable
                        public final void run() {
                            CameraDeviceCallback.this.onCameraStatus(this.f$1, 0);
                        }
                    });
                }
            }
        }

        @Override // ohos.media.camera.zidl.ICameraServiceStatus
        public void onFlashlightStatusChanged(String str, int i) {
            this.cameraTorchStatusMap.put(str, Integer.valueOf(i));
            emitFlashlightEvent(str, i);
        }

        @Override // ohos.media.camera.zidl.ICameraServiceStatus
        public void onCameraServiceDied() {
            LOGGER.warn("Camera service is died", new Object[0]);
            if (!this.isInitialized.get()) {
                LOGGER.warn("Camera service is not initialized, return", new Object[0]);
                return;
            }
            this.isInitialized.set(false);
            updateCameraAvailabilities();
            CameraManager.cameraInfoAssistant.clearCameraInfos();
            CameraManager.cameraAbilityAssistant.clearCameraAbilities();
            scheduleReinitializeCameraService();
        }

        private void updateCameraAvailabilities() {
            for (String str : CameraManager.cameraInfoAssistant.getPhysicalCameraIds()) {
                CameraManager.cameraInfoAssistant.updateCameraAvailability(str, 2);
            }
            emitCameraAvailabilityEvents();
        }

        private void emitFlashlightEvent(String str, int i) {
            for (Map.Entry<CameraDeviceCallback, EventHandler> entry : this.deviceCallbackMap.entrySet()) {
                CameraDeviceCallback key = entry.getKey();
                EventHandler value = entry.getValue();
                if (isTorchAvailable(i)) {
                    value.postTask(new Runnable(str, i) {
                        /* class ohos.media.camera.device.$$Lambda$CameraManager$CameraServiceAssistant$URif_Byb2T2OwslCAN2SJWuk9g */
                        private final /* synthetic */ String f$1;
                        private final /* synthetic */ int f$2;

                        {
                            this.f$1 = r2;
                            this.f$2 = r3;
                        }

                        @Override // java.lang.Runnable
                        public final void run() {
                            CameraDeviceCallback.this.onCameraStatus(this.f$1, this.f$2);
                        }
                    });
                } else {
                    value.postTask(new Runnable(str) {
                        /* class ohos.media.camera.device.$$Lambda$CameraManager$CameraServiceAssistant$qv9y2LgkWSaG7aQwT7qQFQ4VyAs */
                        private final /* synthetic */ String f$1;

                        {
                            this.f$1 = r2;
                        }

                        @Override // java.lang.Runnable
                        public final void run() {
                            CameraDeviceCallback.this.onCameraStatus(this.f$1, 0);
                        }
                    });
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public enum CameraInfoAssistant {
        INSTANCE;
        
        private static final Logger LOGGER = LoggerFactory.getCameraLogger(CameraInfoAssistant.class);
        private final Map<String, Optional<CameraInfoImpl>> cameraInfoMap = new ConcurrentHashMap();
        private final Map<Integer, String> facingType2LogicalIdMap = new ConcurrentHashMap();
        private final Map<String, String> physicalId2LogicalIdMap = new ConcurrentHashMap();

        private CameraInfoAssistant() {
        }

        public void initCameraInfos(Map<String, Integer> map) {
            LOGGER.debug("initCameraInfos start, cameraAvailabilityStatusMap: %{public}s", map);
            Set<String> filterLogicalCameraIds = filterLogicalCameraIds(map.keySet());
            for (String str : filterLogicalCameraIds) {
                int facingType = CameraManager.cameraAbilityAssistant.getFacingType(str);
                if (facingType == -1) {
                    LOGGER.info("Init camera info for %{public}s, facingType is CAMERA_FACING_OTHERS", str);
                    initCameraInfo4Others(str, facingType, map);
                } else if (this.facingType2LogicalIdMap.containsKey(Integer.valueOf(facingType))) {
                    LOGGER.warn("Multiple logical cameras for facing type %{public}d, existed logical camera id %{public}s, ignore the incoming logical camera id %{public}s", Integer.valueOf(facingType), this.facingType2LogicalIdMap.get(Integer.valueOf(facingType)), str);
                } else {
                    int intValue = map.get(str).intValue();
                    LOGGER.info("Init camera info for logical id: %{public}s, facingType: %{public}d", str, Integer.valueOf(facingType));
                    initCameraInfo(str, facingType, intValue);
                }
            }
            for (Map.Entry<String, Integer> entry : filterPhysicalCameraMap(filterLogicalCameraIds, map).entrySet()) {
                String key = entry.getKey();
                int intValue2 = entry.getValue().intValue();
                if (!CameraManager.cameraAbilityAssistant.checkCameraAbilityReady(key)) {
                    LOGGER.warn("CameraAbility is not ready for physicalCameraId: %{public}s", key);
                } else {
                    int facingType2 = CameraManager.cameraAbilityAssistant.getFacingType(key);
                    if (facingType2 == 0 || facingType2 == 1) {
                        updateCameraInfo(key, facingType2, intValue2);
                    } else {
                        initCameraInfoByPhysicalCamera(key, facingType2, intValue2);
                    }
                }
            }
            LOGGER.debug("initCameraInfos end", new Object[0]);
        }

        private Set<String> filterLogicalCameraIds(Set<String> set) {
            Set<String> set2 = (Set) set.stream().filter($$Lambda$CameraManager$CameraInfoAssistant$pXQXtKAgYNZmNaRw_xa3pXRKyvU.INSTANCE).collect(Collectors.toSet());
            LOGGER.debug("filterLogicalCameraIds logicalCameraIds: %{public}s", set2);
            return set2;
        }

        private void initCameraInfo4Others(String str, int i, Map<String, Integer> map) {
            CameraInfoImpl initCameraInfo = initCameraInfo(str, i, map.get(str).intValue());
            for (String str2 : CameraManager.cameraAbilityAssistant.getPhysicalCameraIds(str)) {
                if (!map.containsKey(str2)) {
                    LOGGER.warn("The physical camera id %{public}s does not contains in cameraAvailabilityStatusMap", str2);
                } else {
                    updateCameraInfo(initCameraInfo, str2, map.get(str2).intValue());
                }
            }
        }

        private void updateCameraInfo(CameraInfoImpl cameraInfoImpl, String str, int i) {
            cameraInfoImpl.putPhysicalIdMap(str, i);
            this.physicalId2LogicalIdMap.put(str, cameraInfoImpl.getLogicalId());
            cameraInfoImpl.setDeviceLinkType(str, CameraManager.cameraAbilityAssistant.getDeviceLinkType(str));
            LOGGER.info("Update camera info %{public}s", cameraInfoImpl);
        }

        private Map<String, Integer> filterPhysicalCameraMap(Set<String> set, Map<String, Integer> map) {
            if (set.isEmpty()) {
                return map;
            }
            HashMap hashMap = new HashMap();
            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                String key = entry.getKey();
                int intValue = entry.getValue().intValue();
                if (!set.contains(key)) {
                    hashMap.put(key, Integer.valueOf(intValue));
                }
            }
            LOGGER.debug("filterPhysicalCameraMap physicalCameraIds: %{public}s", hashMap.keySet());
            return hashMap;
        }

        private CameraInfoImpl initCameraInfo(String str, int i, int i2) {
            CameraInfoImpl cameraInfoImpl = new CameraInfoImpl(str, i, i2);
            this.cameraInfoMap.put(str, Optional.of(cameraInfoImpl));
            this.facingType2LogicalIdMap.put(Integer.valueOf(i), str);
            LOGGER.info("Add camera info %{public}s to cameraInfoMap", cameraInfoImpl);
            return cameraInfoImpl;
        }

        private void updateCameraInfo(String str, int i, int i2) {
            String str2 = this.facingType2LogicalIdMap.get(Integer.valueOf(i));
            if (str2 == null) {
                LOGGER.warn("There is no logical camera, init camera info by the first physical camera %{public}s", str);
                initCameraInfoByPhysicalCamera(str, i, i2);
                return;
            }
            Optional<CameraInfoImpl> optional = this.cameraInfoMap.get(str2);
            if (!optional.isPresent()) {
                LOGGER.warn("There is no camera info for id %{public}s, please check the initialization", str2);
            } else {
                updateCameraInfo(optional.get(), str, i2);
            }
        }

        private void initCameraInfoByPhysicalCamera(String str, int i, int i2) {
            initCameraInfo(str, i, i2);
            updateCameraInfo(str, i, i2);
        }

        public void reInitCameraInfo(String str, int i) {
            if (!CameraManager.cameraAbilityAssistant.checkCameraAbilityReady(str)) {
                LOGGER.warn("CameraAbility is not ready for cameraId: %{public}s", str);
            } else {
                initCameraInfoByPhysicalCamera(str, CameraManager.cameraAbilityAssistant.getFacingType(str), i);
            }
        }

        public String[] getCameraIdList() {
            return (String[]) this.cameraInfoMap.keySet().toArray(new String[0]);
        }

        public List<String> getPhysicalCameraIds() {
            return new ArrayList(this.physicalId2LogicalIdMap.keySet());
        }

        public void updateCameraAvailability(String str, int i) {
            Optional<CameraInfoImpl> cameraInfoByPhysicalId = getCameraInfoByPhysicalId(str);
            if (!cameraInfoByPhysicalId.isPresent()) {
                LOGGER.warn("There is no camera info contains physical camera id %{public}s, start to re-init", str);
                reInitCameraInfo(str, i);
                return;
            }
            CameraInfoImpl cameraInfoImpl = cameraInfoByPhysicalId.get();
            LOGGER.info("Update camera info %{public}s, physicalCameraId %{public}s, availability: %{public}d", cameraInfoImpl, str, Integer.valueOf(i));
            cameraInfoImpl.putPhysicalIdMap(str, i);
        }

        public boolean isLogicalCamera(String str) {
            return this.cameraInfoMap.containsKey(str);
        }

        public Optional<CameraInfoImpl> getCameraInfo(String str) {
            return this.cameraInfoMap.getOrDefault(str, Optional.empty());
        }

        public Optional<CameraInfoImpl> getCameraInfoByPhysicalId(String str) {
            return this.cameraInfoMap.get(this.physicalId2LogicalIdMap.get(str));
        }

        public void clearCameraInfos() {
            this.cameraInfoMap.clear();
            this.physicalId2LogicalIdMap.clear();
        }
    }

    /* access modifiers changed from: private */
    public enum CameraAbilityAssistant {
        INSTANCE;
        
        private static final Logger LOGGER = LoggerFactory.getCameraLogger(CameraAbilityAssistant.class);
        private final Map<String, CameraAbilityImpl> cachedCameraAbilityMap = new ConcurrentHashMap();

        private CameraAbilityAssistant() {
        }

        public int getFacingType(String str) {
            Optional<CameraAbilityImpl> cameraAbility = getCameraAbility(str);
            if (cameraAbility.isPresent()) {
                Integer num = (Integer) cameraAbility.get().getPropertyValue(InnerPropertyKey.LENS_FACING);
                if (num != null) {
                    return num.intValue();
                }
                LOGGER.error("Failed to get LENS_FACING from CameraAbilityImpl", new Object[0]);
                return -1;
            }
            LOGGER.warn("cameraAbility is null, return facing type as others", new Object[0]);
            return -1;
        }

        public int getDeviceLinkType(String str) {
            Optional<CameraAbilityImpl> cameraAbility = getCameraAbility(str);
            if (cameraAbility.isPresent()) {
                Integer num = (Integer) cameraAbility.get().getPropertyValue(InnerPropertyKey.LINK_TYPE);
                if (num != null) {
                    return num.intValue();
                }
                LOGGER.warn("camera LINK_TYPE is null.", new Object[0]);
                return -1;
            }
            LOGGER.warn("cameraAbility is null, return device link type as others", new Object[0]);
            return -1;
        }

        public boolean checkCameraAbilityReady(String str) {
            return getCameraAbility(str).isPresent();
        }

        public Optional<CameraAbilityImpl> getCameraAbility(String str) {
            if (this.cachedCameraAbilityMap.containsKey(str)) {
                return Optional.of(this.cachedCameraAbilityMap.get(str));
            }
            ICameraService cameraService = CameraManager.cameraServiceAssistant.getCameraService();
            if (cameraService == null) {
                LOGGER.warn("CameraService is not initialized", new Object[0]);
                return Optional.empty();
            }
            try {
                CameraAbilityNative cameraAbility = cameraService.getCameraAbility(str);
                if (cameraAbility == null) {
                    LOGGER.warn("CameraAbilityNative is null", new Object[0]);
                    return Optional.empty();
                }
                CameraAbilityImpl cameraAbilityImpl = new CameraAbilityImpl(str, cameraAbility);
                this.cachedCameraAbilityMap.put(str, cameraAbilityImpl);
                return Optional.of(cameraAbilityImpl);
            } catch (ConnectException e) {
                LOGGER.error("Failed to get CameraAbilityImpl of camera id: %{public}s", e, str);
                return Optional.empty();
            }
        }

        public boolean isLogicalCamera(String str) {
            Optional<CameraAbilityImpl> cameraAbility = getCameraAbility(str);
            if (!cameraAbility.isPresent()) {
                LOGGER.warn("Camera ability is not ready for %{public}s", str);
                return false;
            }
            boolean isLogicalCamera = cameraAbility.get().isLogicalCamera();
            LOGGER.info("Camera %{public}s is a logical camera: %{public}b", str, Boolean.valueOf(isLogicalCamera));
            return isLogicalCamera;
        }

        public Set<String> getPhysicalCameraIds(String str) {
            Optional<CameraAbilityImpl> cameraAbility = getCameraAbility(str);
            if (!cameraAbility.isPresent()) {
                LOGGER.warn("Camera ability is not ready for %{public}s", str);
                return Collections.emptySet();
            }
            Set<String> physicalCameraIds = cameraAbility.get().getPhysicalCameraIds();
            LOGGER.info("Logical camera contains physical camera ids: %{public}s", physicalCameraIds);
            return physicalCameraIds;
        }

        public void removeCachedCameraAbility(String str) {
            this.cachedCameraAbilityMap.remove(str);
        }

        public void clearCameraAbilities() {
            this.cachedCameraAbilityMap.clear();
        }
    }

    /* access modifiers changed from: private */
    public enum CameraDeviceAssistant {
        INSTANCE;
        
        private static final Tracer TRACER = TracerFactory.getCameraTracer();

        public CameraImpl createCamera(String str, CameraStateCallback cameraStateCallback, EventHandler eventHandler, String str2) {
            ICameraService cameraService = CameraServiceAssistant.INSTANCE.getCameraService();
            try {
                TRACER.startTrace("create-camera");
                CameraImpl cameraImpl = new CameraImpl(str, cameraStateCallback, eventHandler);
                cameraImpl.setCameraDevice(cameraService.createCamera(str, cameraImpl, str2));
                TRACER.finishTrace("create-camera");
                emitCreatedEvent(cameraImpl, cameraStateCallback, eventHandler);
                return cameraImpl;
            } catch (ConnectException e) {
                emitCreateFailedEvent(str, cameraStateCallback, eventHandler, e.getErrorCode());
                return null;
            }
        }

        private void emitCreatedEvent(Camera camera, CameraStateCallback cameraStateCallback, EventHandler eventHandler) {
            eventHandler.postTask(new Runnable(camera) {
                /* class ohos.media.camera.device.$$Lambda$CameraManager$CameraDeviceAssistant$QjtXRmO9Ri4BZM5wnlZbjDuD2k */
                private final /* synthetic */ Camera f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    CameraStateCallback.this.onCreated(this.f$1);
                }
            });
        }

        private void emitCreateFailedEvent(String str, CameraStateCallback cameraStateCallback, EventHandler eventHandler, int i) {
            eventHandler.postTask(new Runnable(str, i) {
                /* class ohos.media.camera.device.$$Lambda$CameraManager$CameraDeviceAssistant$HKUVpJwGBpkkhPnvfKPA2RBt4DE */
                private final /* synthetic */ String f$1;
                private final /* synthetic */ int f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    CameraStateCallback.this.onCreateFailed(this.f$1, this.f$2);
                }
            });
        }
    }
}
