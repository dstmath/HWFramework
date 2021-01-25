package ohos.media.camera.mode;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import ohos.app.Context;
import ohos.eventhandler.EventHandler;
import ohos.media.camera.device.adapter.utils.SystemSettings;
import ohos.media.camera.device.impl.CameraAbilityImpl;
import ohos.media.camera.device.impl.CameraInfoImpl;
import ohos.media.camera.mode.Mode;
import ohos.media.camera.mode.impl.ModeAbilityImpl;
import ohos.media.camera.mode.impl.ModeImpl;
import ohos.media.camera.mode.impl.ModeInfoImpl;
import ohos.media.camera.mode.utils.CameraAbilityHelper;
import ohos.media.camera.mode.utils.CameraManagerHelper;
import ohos.media.camera.mode.utils.CollectionUtil;
import ohos.media.camera.mode.utils.DeviceUtil;
import ohos.media.camera.mode.utils.KitUtil;
import ohos.media.camera.mode.utils.StringUtil;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;
import ohos.media.utils.trace.Tracer;
import ohos.media.utils.trace.TracerFactory;

public class ModeManager {
    private static final Logger LOGGER = LoggerFactory.getCameraLogger(ModeManager.class);
    private static final Map<Integer, String> MODE_MAP = new HashMap(2);
    private static final String MODE_PACKAGE = "ohos.media.camera.mode.modes.";
    @Mode.Type
    private static final int[] SUPPORTED_MODES = {1, 5};
    private static final Tracer TRACER = TracerFactory.getCameraTracer(LOGGER);
    private static ModeManager instance;
    private final Map<String, ModeAbilityImpl> cachedModeAbilityMap = new ConcurrentHashMap();
    private final Context context;
    private final AtomicBoolean isInitialized = new AtomicBoolean(false);
    private final Map<String, ModeInfoImpl> modeInfoMap = new ConcurrentHashMap();
    private final Map<Integer, ModeMapQueryState> modeMapQueryResult = new ConcurrentHashMap();

    /* access modifiers changed from: private */
    public enum ModeMapQueryState {
        UNCHECKED,
        STANDARD,
        SERVICEHOST
    }

    static {
        MODE_MAP.put(1, "NormalMode");
        MODE_MAP.put(5, "VideoMode");
    }

    private ModeManager(Context context2) {
        Objects.requireNonNull(context2, "Context should not be null");
        this.context = context2;
        initModeWithGuard();
    }

    public static synchronized ModeManager getInstance(Context context2) {
        ModeManager modeManager;
        synchronized (ModeManager.class) {
            if (instance == null) {
                instance = new ModeManager(context2);
            }
            modeManager = instance;
        }
        return modeManager;
    }

    private synchronized void initModeWithGuard() {
        if (this.isInitialized.get()) {
            LOGGER.debug("Mode manager is already initialized", new Object[0]);
        } else if (SystemSettings.isCameraServiceDisabled()) {
            LOGGER.warn("Camera service is disabled", new Object[0]);
        } else {
            initialize();
        }
    }

    private void initialize() {
        CameraManagerHelper.initialize(this.context);
        DeviceUtil.initialize(this.context);
        KitUtil.initialize(this.context);
        for (int i : SUPPORTED_MODES) {
            this.modeMapQueryResult.put(Integer.valueOf(i), ModeMapQueryState.UNCHECKED);
        }
        initializeModeInfo();
        this.isInitialized.set(true);
    }

    private void initializeModeInfo() {
        for (String str : CameraManagerHelper.getCameraManager().getCameraIdList()) {
            addModeInfo(str);
        }
    }

    private void addModeInfo(String str) {
        CameraAbilityImpl cameraAbility = CameraAbilityHelper.getCameraAbility(str);
        CameraInfoImpl cameraInfo = CameraManagerHelper.getCameraManager().getCameraInfo(str);
        if (cameraInfo == null) {
            LOGGER.warn("Camera info is null for id: %{public}s", str);
            return;
        }
        ModeInfoImpl modeInfoImpl = new ModeInfoImpl(cameraInfo);
        modeInfoImpl.setSupportedModes(getSupportedModes(cameraAbility));
        this.modeInfoMap.put(str, modeInfoImpl);
    }

    public ModeInfoImpl getModeInfo(String str) {
        return this.modeInfoMap.getOrDefault(str, null);
    }

    private static String getCachedModeAbilityKey(String str, int i) {
        return str + "_" + i;
    }

    /* JADX WARNING: Removed duplicated region for block: B:29:0x00d9  */
    public ModeAbilityImpl getModeAbility(String str, int i) {
        Objects.requireNonNull(str, "Camera id should not be null");
        if (!modeGuard()) {
            LOGGER.warn("Camera service is not initialized", new Object[0]);
            return null;
        }
        LOGGER.debug("getModeAbility: cameraInfoList = %{public}s", this.modeInfoMap.keySet());
        if (!this.modeInfoMap.containsKey(str)) {
            LOGGER.warn("getModeAbility error due to not contain the camera id: %{public}s", str);
            return null;
        }
        LOGGER.debug("getModeAbility: cameraId: %{public}s, modeType: %{public}d", str, Integer.valueOf(i));
        ModeAbilityImpl modeAbilityImpl = this.cachedModeAbilityMap.get(getCachedModeAbilityKey(str, i));
        if (modeAbilityImpl != null) {
            LOGGER.debug("getModeAbility: cachedModeAbilityMap hit", new Object[0]);
            return modeAbilityImpl;
        }
        if (CollectionUtil.contains(this.modeInfoMap.get(str).getSupportedModes(), i)) {
            String modeNameFromModeType = getModeNameFromModeType(i);
            CameraAbilityImpl cameraAbility = CameraAbilityHelper.getCameraAbility(str);
            try {
                ModeAbilityImpl modeAbilityImpl2 = (ModeAbilityImpl) Class.forName(MODE_PACKAGE + modeNameFromModeType).getMethod("getModeAbility", CameraAbilityImpl.class).invoke(null, cameraAbility);
                try {
                    this.cachedModeAbilityMap.put(getCachedModeAbilityKey(str, i), modeAbilityImpl2);
                    LOGGER.debug("getModeAbility: add to cachedModeAbilityMap done.", new Object[0]);
                    modeAbilityImpl = modeAbilityImpl2;
                } catch (ClassNotFoundException unused) {
                    modeAbilityImpl = modeAbilityImpl2;
                    LOGGER.error("getModeAbility: mode not found!", new Object[0]);
                    if (modeAbilityImpl == null) {
                    }
                    return modeAbilityImpl;
                } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException unused2) {
                    modeAbilityImpl = modeAbilityImpl2;
                    LOGGER.error("getModeAbility: method not found!", new Object[0]);
                    if (modeAbilityImpl == null) {
                    }
                    return modeAbilityImpl;
                }
            } catch (ClassNotFoundException unused3) {
                LOGGER.error("getModeAbility: mode not found!", new Object[0]);
                if (modeAbilityImpl == null) {
                }
                return modeAbilityImpl;
            } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException unused4) {
                LOGGER.error("getModeAbility: method not found!", new Object[0]);
                if (modeAbilityImpl == null) {
                }
                return modeAbilityImpl;
            }
        }
        if (modeAbilityImpl == null) {
            LOGGER.warn("getModeAbility: unsupported mode!", new Object[0]);
        }
        return modeAbilityImpl;
    }

    public int[] getSupportedModes(String str) {
        Objects.requireNonNull(str, "Camera id should not be null");
        if (!modeGuard()) {
            LOGGER.warn("Camera service is not initialized", new Object[0]);
            return new int[0];
        }
        ModeInfoImpl modeInfo = getModeInfo(str);
        return modeInfo != null ? modeInfo.getSupportedModes() : new int[0];
    }

    @Mode.Type
    private int[] getSupportedModes(CameraAbilityImpl cameraAbilityImpl) {
        LOGGER.debug("getSupportedModes: %{public}s", Arrays.toString(SUPPORTED_MODES));
        int[] iArr = SUPPORTED_MODES;
        int[] iArr2 = new int[iArr.length];
        int i = 0;
        for (int i2 : iArr) {
            if (isAvailableMode(i2, cameraAbilityImpl)) {
                iArr2[i] = i2;
                i++;
            }
        }
        int[] iArr3 = new int[i];
        System.arraycopy(iArr2, 0, iArr3, 0, i);
        LOGGER.debug("getSupportedModes: availableModes num =  %{public}d", Integer.valueOf(iArr3.length));
        return iArr3;
    }

    private boolean isValidMode(String str, @Mode.Type int i) {
        ModeInfoImpl modeInfoImpl = this.modeInfoMap.get(str);
        if (modeInfoImpl == null) {
            return false;
        }
        for (int i2 : modeInfoImpl.getSupportedModes()) {
            if (i2 == i) {
                return true;
            }
        }
        return false;
    }

    private boolean isAvailableMode(@Mode.Type int i, CameraAbilityImpl cameraAbilityImpl) {
        String modeNameFromModeType = getModeNameFromModeType(i);
        LOGGER.debug("isAvailableMode: modeName = %{public}s", modeNameFromModeType);
        if (this.modeMapQueryResult.get(Integer.valueOf(i)) == ModeMapQueryState.SERVICEHOST) {
            return true;
        }
        try {
            return ((Boolean) Class.forName(MODE_PACKAGE + modeNameFromModeType).getMethod("isAvailable", CameraAbilityImpl.class).invoke(null, cameraAbilityImpl)).booleanValue();
        } catch (ClassNotFoundException unused) {
            LOGGER.error("isAvailableMode: ClassNotFoundException", new Object[0]);
            return false;
        } catch (NoSuchMethodException e) {
            LOGGER.error("isAvailableMode: method not found, %{public}s", e.getMessage());
            return false;
        } catch (IllegalAccessException | InvocationTargetException e2) {
            LOGGER.error("isAvailableMode: should not invoke method, %{public}s", e2.getMessage());
            return false;
        }
    }

    private String getModeNameFromModeType(@Mode.Type int i) {
        LOGGER.debug("getModeNameFromModeType: modeType = %{public}s", Integer.valueOf(i));
        String str = "";
        if (this.modeMapQueryResult.get(Integer.valueOf(i)) == ModeMapQueryState.UNCHECKED) {
            if (MODE_MAP.containsKey(Integer.valueOf(i))) {
                str = MODE_MAP.get(Integer.valueOf(i));
                this.modeMapQueryResult.put(Integer.valueOf(i), ModeMapQueryState.STANDARD);
            } else {
                LOGGER.warn("getModeNameFromModeType: modeType not found!", new Object[0]);
            }
        } else if (AnonymousClass1.$SwitchMap$ohos$media$camera$mode$ModeManager$ModeMapQueryState[this.modeMapQueryResult.get(Integer.valueOf(i)).ordinal()] == 1) {
            str = MODE_MAP.get(Integer.valueOf(i));
        }
        LOGGER.debug("getModeNameFromModeType: modeName = %{public}s", str);
        return str;
    }

    /* access modifiers changed from: package-private */
    /* renamed from: ohos.media.camera.mode.ModeManager$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$ohos$media$camera$mode$ModeManager$ModeMapQueryState = new int[ModeMapQueryState.values().length];

        static {
            try {
                $SwitchMap$ohos$media$camera$mode$ModeManager$ModeMapQueryState[ModeMapQueryState.STANDARD.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
        }
    }

    private ModeImpl createModeInternal(String str, @Mode.Type int i, ModeStateCallback modeStateCallback, EventHandler eventHandler) {
        TRACER.startTrace("create-mode-internal");
        String modeNameFromModeType = getModeNameFromModeType(i);
        LOGGER.debug("createModeInternal: creating mode %{public}d, mode name: %{public}s", Integer.valueOf(i), modeNameFromModeType);
        ModeImpl modeImpl = null;
        try {
            Object newInstance = Class.forName(MODE_PACKAGE + modeNameFromModeType).getConstructor(String.class, ModeStateCallback.class, EventHandler.class).newInstance(str, modeStateCallback, eventHandler);
            ModeImpl modeImpl2 = newInstance instanceof ModeImpl ? (ModeImpl) newInstance : null;
            LOGGER.debug("createMode: %{public}s has been created!", modeNameFromModeType);
            modeImpl = modeImpl2;
        } catch (ClassNotFoundException unused) {
            LOGGER.error("createMode: modeName = null!", new Object[0]);
        } catch (NoSuchMethodException unused2) {
            LOGGER.error("createMode: constructor not found!", new Object[0]);
        } catch (IllegalAccessException | IllegalArgumentException | InstantiationException | InvocationTargetException e) {
            LOGGER.error("createMode: newInstance got error!", e, new Object[0]);
        }
        TRACER.finishTrace("create-mode-internal");
        return modeImpl;
    }

    public void createMode(String str, @Mode.Type int i, ModeStateCallback modeStateCallback, EventHandler eventHandler) {
        TRACER.startTrace("create-mode");
        Objects.requireNonNull(str, "Camera id should not be null!");
        Objects.requireNonNull(modeStateCallback, "Mode callback should not be null!");
        Objects.requireNonNull(eventHandler, "Mode callback handler should not be null!");
        if (!modeGuard()) {
            LOGGER.warn("createMode: camera service is not initialized", new Object[0]);
            emitCreateFailedEvent(str, i, modeStateCallback, eventHandler);
        } else if (!isValidCameraId(str) || !isValidMode(str, i)) {
            LOGGER.warn("createMode: invalid camera id %{public}s or mode type: %{public}d", str, Integer.valueOf(i));
            throw new IllegalArgumentException(String.format(Locale.ENGLISH, "Invalid camera id:%s or type:%d", str, Integer.valueOf(i)));
        } else {
            ModeImpl createModeInternal = createModeInternal(str, i, modeStateCallback, eventHandler);
            if (createModeInternal != null) {
                createModeInternal.open();
                TRACER.finishTrace("create-mode");
                return;
            }
            LOGGER.warn("createMode: mode is null", new Object[0]);
            throw new IllegalArgumentException("Can not create mode internal");
        }
    }

    private void emitCreateFailedEvent(String str, @Mode.Type int i, ModeStateCallback modeStateCallback, EventHandler eventHandler) {
        eventHandler.postTask(new Runnable(str, i, SystemSettings.isCameraServiceDisabled() ? 3 : 5) {
            /* class ohos.media.camera.mode.$$Lambda$ModeManager$iPpw6ndtd2DRPr3GyXPFZozckM */
            private final /* synthetic */ String f$1;
            private final /* synthetic */ int f$2;
            private final /* synthetic */ int f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            @Override // java.lang.Runnable
            public final void run() {
                ModeStateCallback.this.onCreateFailed(this.f$1, this.f$2, this.f$3);
            }
        });
    }

    private boolean isValidCameraId(String str) {
        if (StringUtil.isEmptyString(str)) {
            LOGGER.warn("createMode: cameraId is empty string", new Object[0]);
            return false;
        } else if (this.modeInfoMap.containsKey(str)) {
            return true;
        } else {
            LOGGER.warn("createMode: cameraId is not contains in modeInfoMap", new Object[0]);
            return false;
        }
    }

    public void changeMode(Mode mode, @Mode.Type int i, ModeStateCallback modeStateCallback) {
        TRACER.startTrace("change-mode");
        Objects.requireNonNull(mode, "Error in mode implementation!");
        Objects.requireNonNull(modeStateCallback, "Mode callback should not be null!");
        if (mode instanceof ModeImpl) {
            ModeImpl modeImpl = (ModeImpl) mode;
            String cameraId = modeImpl.getCameraId();
            EventHandler handler = modeImpl.getHandler();
            if (!modeGuard()) {
                LOGGER.warn("changeMode: camera service is not initialized", new Object[0]);
                emitCreateFailedEvent(cameraId, i, modeStateCallback, handler);
            } else if (!isValidCameraId(cameraId) || !isValidMode(modeImpl.getCameraId(), i)) {
                LOGGER.warn("changeMode: invalid camera id %{public}s or mode type: %{public}d", cameraId, Integer.valueOf(i));
                throw new IllegalArgumentException(String.format(Locale.ENGLISH, "Invalid camera id:%s or type:%d", cameraId, Integer.valueOf(i)));
            } else if (modeImpl.getType() == 6 || i == 5) {
                LOGGER.debug("changeMode by createMode. preModeType: %{public}d, curModeType: %{public}d", Integer.valueOf(modeImpl.getType()), Integer.valueOf(i));
                modeImpl.release();
                createMode(cameraId, i, modeStateCallback, handler);
            } else {
                modeImpl.deactive();
                modeImpl.releaseStrong();
                ModeImpl createModeInternal = createModeInternal(cameraId, i, modeStateCallback, handler);
                modeImpl.releaseWeak();
                if (createModeInternal != null) {
                    createModeInternal.open();
                    TRACER.finishTrace("change-mode");
                    return;
                }
                LOGGER.warn("changeMode: create mode is null", new Object[0]);
                throw new IllegalArgumentException("Can not change mode internal");
            }
        } else {
            LOGGER.warn("changeMode: Incorrect type", new Object[0]);
            throw new IllegalArgumentException("Incorrect type");
        }
    }

    private boolean modeGuard() {
        if (this.context == null) {
            throw new IllegalStateException("Initialize mode manager with context first");
        } else if (!CameraManagerHelper.getCameraManager().cameraGuard()) {
            return false;
        } else {
            initModeWithGuard();
            return this.isInitialized.get();
        }
    }
}
