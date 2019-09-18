package com.android.server.display;

import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.ArrayMap;
import android.util.IMonitor;
import android.util.Log;
import android.util.Slog;
import com.android.server.display.BackLightCommonData;
import com.android.server.display.DisplayEffectMonitor;
import com.huawei.dubai.DubaiConstants;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

class BrightnessSeekBarMonitor implements DisplayEffectMonitor.MonitorModule {
    /* access modifiers changed from: private */
    public static final boolean HWDEBUG = (Log.HWLog || (Log.HWModuleLog && Log.isLoggable(TAG, 3)));
    /* access modifiers changed from: private */
    public static final boolean HWFLOW;
    private static final String MONITOR_TARGET_PACKAGE = "android.uid.systemui";
    private static final String PARAM_BRIGHTNESS = "brightness";
    private static final String PARAM_BRIGHTNESS_MODE = "brightnessMode";
    private static final String PARAM_IS_ENABLE = "isEnable";
    private static final String PARAM_LIGHT_VALUE = "lightValue";
    private static final String PARAM_PACKAGE_NAME = "packageName";
    private static final String PARAM_USER_SCENE = "userScene";
    private static final int POWER_MODE_DEFAULT = 2;
    private static final String POWER_MODE_PROP = "persist.sys.smart_power";
    private static final String TAG = "BrightnessSeekBarMonitor";
    private static final String TYPE_ALGO_DEFAULT_BRIGHTNESS = "algoDefaultBrightness";
    private static final String TYPE_EYE_PROTECT = "eyeProtect";
    private static final String TYPE_TEMP_AUTO_BRIGHTNESS = "tempAutoBrightness";
    private static final String TYPE_TEMP_MANUAL_BRIGHTNESS = "tempManualBrightness";
    private static final String TYPE_USER_SCENE = "userScene";
    private static final String TYPE_WINDOW_MANAGER_BRIGHTNESS = "windowManagerBrightness";
    private static final Comparator<SeekBarMovement> mComparator = new Comparator<SeekBarMovement>() {
        public int compare(SeekBarMovement a, SeekBarMovement b) {
            return (int) (a.startTime - b.startTime);
        }
    };
    /* access modifiers changed from: private */
    public int mAlgoDefaultBrightness;
    /* access modifiers changed from: private */
    public final BackLightCommonData mCommonData;
    /* access modifiers changed from: private */
    public boolean mIsEyeProtectEnable;
    /* access modifiers changed from: private */
    public boolean mIsManualModeReceiveAmbientLight;
    /* access modifiers changed from: private */
    public final DisplayEffectMonitor mMonitor;
    private SeekBarMovementUploader mSeekBarMovementUploader;
    private SeekBarRecoderApp mSeekBarRecoderApp;
    private SeekBarRecoderAuto mSeekBarRecoderAuto;
    private SeekBarRecoderManual mSeekBarRecoderManual;
    private String mTempAutoBrightnessPackageNameLast;
    private DisplayEffectMonitor.ParamLogPrinter mTempAutoBrightnessPrinter;
    private String mTempManualBrightnessPackageNameLast;
    private DisplayEffectMonitor.ParamLogPrinter mTempManualBrightnessPrinter;
    /* access modifiers changed from: private */
    public int mUserScene = -1;
    /* access modifiers changed from: private */
    public String mWindowManagerBrightnessPackageNameLast = "android";
    private DisplayEffectMonitor.ParamLogPrinter mWindowManagerBrightnessPrinter;

    private enum SeekBarBrightMode {
        AUTO((byte) 1),
        MANUAL((byte) 2),
        APP((byte) 3);
        
        private final byte mId;

        private SeekBarBrightMode(byte id) {
            this.mId = id;
        }

        public byte getId() {
            return this.mId;
        }
    }

    private static class SeekBarMovement {
        public int algoDefaultLevel;
        public int endLevel;
        public long endTime;
        public String foregroundPackageName;
        public SeekBarBrightMode mode;
        public boolean powerSave;
        public boolean protectEye;
        public int startLevel;
        public int startLux;
        public long startTime;
        public boolean thermalLimited;
        public int userScene;

        private SeekBarMovement() {
        }

        public String toString() {
            return "SeekBarMovement " + this.mode + ", " + this.startLevel + " -> " + this.endLevel + ", lux " + this.startLux + ", last " + (this.endTime - this.startTime) + "ms, default " + this.algoDefaultLevel + ", app " + this.foregroundPackageName + ", protectEye " + this.protectEye + ", powerSave " + this.powerSave + ", thermalLimited " + this.thermalLimited + ", userScene " + this.userScene;
        }
    }

    private static class SeekBarMovementUploader {
        private static final int HOURS_PER_DAY = 24;
        private static final byte MODE_AUTO = 1;
        private static final byte MODE_MANUAL = 2;
        private static final int UPLOAD_TIMES_LIMIT_PER_DAY = 30;
        private static final int UPLOAD_TIMES_LIMIT_PER_HOUR = 10;
        private int mHourCount;
        private List<SeekBarMovementUploadData> mUploadList = new ArrayList();
        private int mUploadTimesPerDay;

        private static class SeekBarMovementUploadData {
            public short adjustEndLevel;
            public short adjustStartLevel;
            public short algoCalcDefaultLevel;
            public short ambientLightLux;
            public String foregroundPackageName;
            public byte mode;
            public boolean powerSave;
            public boolean protectEye;
            public boolean thermalLimited;
            public short userScene;

            private SeekBarMovementUploadData() {
            }

            public String toString() {
                return "SeekBarMovementUploadData mode " + this.mode + ", " + this.adjustStartLevel + " -> " + this.adjustEndLevel + ", ambientLightLux " + this.ambientLightLux + ", algoCalcDefaultLevel " + this.algoCalcDefaultLevel + ", app " + this.foregroundPackageName + ", protectEye " + this.protectEye + ", powerSave " + this.powerSave + ", thermalLimited " + this.thermalLimited + ", userScene " + this.userScene;
            }
        }

        public void addData(SeekBarMovement movement) {
            if (this.mUploadList.size() < 10 && this.mUploadTimesPerDay < 30) {
                this.mUploadTimesPerDay++;
                SeekBarMovementUploadData data = new SeekBarMovementUploadData();
                data.mode = movement.mode.getId();
                data.adjustStartLevel = (short) movement.startLevel;
                data.adjustEndLevel = (short) movement.endLevel;
                int i = movement.startLux;
                int i2 = DubaiConstants.MASK_MODULE_ALL;
                if (i < 32767) {
                    i2 = movement.startLux;
                }
                data.ambientLightLux = (short) i2;
                data.algoCalcDefaultLevel = (short) movement.algoDefaultLevel;
                data.foregroundPackageName = movement.foregroundPackageName;
                data.protectEye = movement.protectEye;
                data.powerSave = movement.powerSave;
                data.thermalLimited = movement.thermalLimited;
                data.userScene = (short) movement.userScene;
                this.mUploadList.add(data);
            }
        }

        public void upload() {
            if (!this.mUploadList.isEmpty()) {
                for (SeekBarMovementUploadData data : this.mUploadList) {
                    IMonitor.EventStream stream = IMonitor.openEventStream(932010102);
                    stream.setParam("Mode", data.mode);
                    stream.setParam("AdjustStartLevel", data.adjustStartLevel);
                    stream.setParam("AdjustEndLevel", data.adjustEndLevel);
                    stream.setParam("AmbientLightLux", data.ambientLightLux);
                    stream.setParam("AlgoCalcDefaultLevel", data.algoCalcDefaultLevel);
                    stream.setParam("ForegroundPackageName", data.foregroundPackageName);
                    stream.setParam("ProtectEye", Boolean.valueOf(data.protectEye));
                    stream.setParam("PowerSave", Boolean.valueOf(data.powerSave));
                    stream.setParam("Thermal", Boolean.valueOf(data.thermalLimited));
                    if (data.userScene >= 0) {
                        stream.setParam("Scene", data.userScene);
                    }
                    IMonitor.sendEvent(stream);
                    IMonitor.closeEventStream(stream);
                    if (BrightnessSeekBarMonitor.HWFLOW) {
                        Slog.i(BrightnessSeekBarMonitor.TAG, "SeekBarMovementUploader.upload() " + data);
                    }
                }
                this.mUploadList.clear();
            }
        }

        public void hourlyTrigger() {
            int i = this.mHourCount + 1;
            this.mHourCount = i;
            if (i >= 24) {
                this.mHourCount = 0;
                this.mUploadTimesPerDay = 0;
            }
        }
    }

    private abstract class SeekBarRecoder {
        private static final long MOVE_INTERVAL_TIME_IN_MS = 2000;
        private static final long MOVE_LIST_INTERVAL_TIME_IN_MS = 15000;
        private static final int MOVE_MERGE_LUX_CHANGE_MIN_THRESHOLD = 5;
        private static final int MOVE_MERGE_LUX_CHANGE_PERCENT = 50;
        private SeekBarMovement mCurrentMovement;
        private final List<SeekBarMovement> mMovementList;

        /* access modifiers changed from: protected */
        public abstract boolean bypassUpdate();

        /* access modifiers changed from: protected */
        public abstract void start(SeekBarMovement seekBarMovement, int i, long j);

        private SeekBarRecoder() {
            this.mMovementList = new ArrayList();
        }

        public void update(int level) {
            if (!bypassUpdate()) {
                long currentTime = SystemClock.elapsedRealtime();
                if (this.mCurrentMovement == null) {
                    this.mCurrentMovement = new SeekBarMovement();
                    start(this.mCurrentMovement, level, currentTime);
                    return;
                }
                if (this.mCurrentMovement.endTime == 0) {
                    if (currentTime - this.mCurrentMovement.startTime > MOVE_INTERVAL_TIME_IN_MS) {
                        start(this.mCurrentMovement, level, currentTime);
                    } else {
                        this.mCurrentMovement.endTime = currentTime;
                        this.mCurrentMovement.endLevel = level;
                    }
                } else if (currentTime - this.mCurrentMovement.endTime > MOVE_INTERVAL_TIME_IN_MS) {
                    end(this.mCurrentMovement);
                    this.mCurrentMovement = new SeekBarMovement();
                    start(this.mCurrentMovement, level, currentTime);
                } else {
                    this.mCurrentMovement.endTime = currentTime;
                    this.mCurrentMovement.endLevel = level;
                }
            }
        }

        public List<SeekBarMovement> getUploadList() {
            checkUnfinished();
            checkInterval();
            return this.mMovementList;
        }

        private void end(SeekBarMovement movement) {
            if (movement.startLevel != movement.endLevel) {
                this.mMovementList.add(movement);
                if (BrightnessSeekBarMonitor.HWFLOW) {
                    Slog.i(BrightnessSeekBarMonitor.TAG, "SeekBarRecoder recode " + movement);
                }
            }
        }

        private void checkUnfinished() {
            if (this.mCurrentMovement != null) {
                long currentTime = SystemClock.elapsedRealtime();
                if (this.mCurrentMovement.endTime != 0 && currentTime - this.mCurrentMovement.endTime > MOVE_INTERVAL_TIME_IN_MS) {
                    end(this.mCurrentMovement);
                    this.mCurrentMovement = null;
                }
            }
        }

        private void checkInterval() {
            if (this.mMovementList.size() > 1) {
                if (BrightnessSeekBarMonitor.HWDEBUG) {
                    Iterator<SeekBarMovement> it = this.mMovementList.iterator();
                    while (it.hasNext()) {
                        Slog.d(BrightnessSeekBarMonitor.TAG, "checkInterval() before " + it.next());
                    }
                }
                boolean isMerged = false;
                SeekBarMovement pre = null;
                SeekBarMovement now = null;
                Iterator<SeekBarMovement> it2 = this.mMovementList.iterator();
                while (it2.hasNext()) {
                    if (pre == null) {
                        pre = it2.next();
                    } else if (now != null) {
                        pre = now;
                    }
                    now = it2.next();
                    if (isMerge2Movement(pre, now)) {
                        if (BrightnessSeekBarMonitor.HWDEBUG) {
                            Slog.d(BrightnessSeekBarMonitor.TAG, "checkInterval() merge " + pre + " and " + now);
                        }
                        pre.endTime = now.endTime;
                        pre.endLevel = now.endLevel;
                        it2.remove();
                        now = null;
                        isMerged = true;
                    }
                }
                if (isMerged && BrightnessSeekBarMonitor.HWDEBUG) {
                    Iterator<SeekBarMovement> it3 = this.mMovementList.iterator();
                    while (it3.hasNext()) {
                        Slog.d(BrightnessSeekBarMonitor.TAG, "checkInterval() after " + it3.next());
                    }
                }
            }
        }

        private boolean isMerge2Movement(SeekBarMovement pre, SeekBarMovement now) {
            if (now.startTime - pre.endTime > MOVE_LIST_INTERVAL_TIME_IN_MS) {
                return false;
            }
            int deltaLevel = Math.abs(now.startLux - pre.startLux);
            int deltaLevelThresholdTmp = (pre.startLux * 50) / 100;
            int deltaLevelThreshold = 5;
            if (deltaLevelThresholdTmp > 5) {
                deltaLevelThreshold = deltaLevelThresholdTmp;
            }
            if (deltaLevel >= deltaLevelThreshold) {
                return false;
            }
            return true;
        }
    }

    private class SeekBarRecoderApp extends SeekBarRecoder {
        private SeekBarRecoderApp() {
            super();
        }

        /* access modifiers changed from: protected */
        public boolean bypassUpdate() {
            return BrightnessSeekBarMonitor.this.mCommonData.getBrightnessMode() == BackLightCommonData.BrightnessMode.MANUAL;
        }

        /* access modifiers changed from: protected */
        public void start(SeekBarMovement movement, int level, long time) {
            movement.mode = SeekBarBrightMode.APP;
            movement.startTime = time;
            movement.startLevel = level;
            movement.startLux = BrightnessSeekBarMonitor.this.mCommonData.getSmoothAmbientLight();
            movement.algoDefaultLevel = BrightnessSeekBarMonitor.this.mAlgoDefaultBrightness;
            movement.foregroundPackageName = BrightnessSeekBarMonitor.this.mWindowManagerBrightnessPackageNameLast;
            movement.endTime = time;
            movement.endLevel = level;
            movement.protectEye = BrightnessSeekBarMonitor.this.mIsEyeProtectEnable;
            movement.powerSave = BrightnessSeekBarMonitor.this.isPowerSaveMode();
            movement.thermalLimited = BrightnessSeekBarMonitor.this.mCommonData.isThermalLimited();
            movement.userScene = BrightnessSeekBarMonitor.this.mUserScene;
        }
    }

    private class SeekBarRecoderAuto extends SeekBarRecoder {
        private SeekBarRecoderAuto() {
            super();
        }

        /* access modifiers changed from: protected */
        public boolean bypassUpdate() {
            return BrightnessSeekBarMonitor.this.mCommonData.isWindowManagerBrightnessMode();
        }

        /* access modifiers changed from: protected */
        public void start(SeekBarMovement movement, int level, long time) {
            movement.mode = SeekBarBrightMode.AUTO;
            movement.startTime = time;
            movement.startLevel = level;
            movement.startLux = BrightnessSeekBarMonitor.this.mCommonData.getSmoothAmbientLight();
            movement.algoDefaultLevel = BrightnessSeekBarMonitor.this.mAlgoDefaultBrightness;
            movement.foregroundPackageName = BrightnessSeekBarMonitor.this.mMonitor.getCurrentTopAppName();
            movement.protectEye = BrightnessSeekBarMonitor.this.mIsEyeProtectEnable;
            movement.powerSave = BrightnessSeekBarMonitor.this.isPowerSaveMode();
            movement.thermalLimited = BrightnessSeekBarMonitor.this.mCommonData.isThermalLimited();
            movement.userScene = BrightnessSeekBarMonitor.this.mUserScene;
        }
    }

    private class SeekBarRecoderManual extends SeekBarRecoder {
        private SeekBarRecoderManual() {
            super();
        }

        /* access modifiers changed from: protected */
        public boolean bypassUpdate() {
            return !BrightnessSeekBarMonitor.this.mIsManualModeReceiveAmbientLight || BrightnessSeekBarMonitor.this.mCommonData.isWindowManagerBrightnessMode();
        }

        /* access modifiers changed from: protected */
        public void start(SeekBarMovement movement, int level, long time) {
            movement.mode = SeekBarBrightMode.MANUAL;
            movement.startTime = time;
            movement.startLevel = level;
            movement.startLux = 0;
            movement.algoDefaultLevel = BrightnessSeekBarMonitor.this.mAlgoDefaultBrightness;
            movement.foregroundPackageName = BrightnessSeekBarMonitor.this.mMonitor.getCurrentTopAppName();
            movement.protectEye = BrightnessSeekBarMonitor.this.mIsEyeProtectEnable;
            movement.powerSave = BrightnessSeekBarMonitor.this.isPowerSaveMode();
            movement.thermalLimited = BrightnessSeekBarMonitor.this.mCommonData.isThermalLimited();
            movement.userScene = BrightnessSeekBarMonitor.this.mUserScene;
        }
    }

    static {
        boolean z = true;
        if (!Log.HWINFO && (!Log.HWModuleLog || !Log.isLoggable(TAG, 4))) {
            z = false;
        }
        HWFLOW = z;
    }

    public BrightnessSeekBarMonitor(DisplayEffectMonitor monitor, BackLightMonitorManager manager) {
        this.mMonitor = monitor;
        this.mCommonData = manager.getBackLightCommonData();
        if (!this.mCommonData.isCommercialVersion()) {
            this.mSeekBarRecoderAuto = new SeekBarRecoderAuto();
            this.mSeekBarRecoderManual = new SeekBarRecoderManual();
            this.mSeekBarRecoderApp = new SeekBarRecoderApp();
            DisplayEffectMonitor displayEffectMonitor = this.mMonitor;
            Objects.requireNonNull(displayEffectMonitor);
            this.mTempManualBrightnessPrinter = new DisplayEffectMonitor.ParamLogPrinter(TYPE_TEMP_MANUAL_BRIGHTNESS, TAG);
            DisplayEffectMonitor displayEffectMonitor2 = this.mMonitor;
            Objects.requireNonNull(displayEffectMonitor2);
            this.mTempAutoBrightnessPrinter = new DisplayEffectMonitor.ParamLogPrinter(TYPE_TEMP_AUTO_BRIGHTNESS, TAG);
            DisplayEffectMonitor displayEffectMonitor3 = this.mMonitor;
            Objects.requireNonNull(displayEffectMonitor3);
            this.mWindowManagerBrightnessPrinter = new DisplayEffectMonitor.ParamLogPrinter(TYPE_WINDOW_MANAGER_BRIGHTNESS, "BrightnessSettingsMonitor");
            this.mSeekBarMovementUploader = new SeekBarMovementUploader();
        }
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    public boolean isParamOwner(String paramType) {
        char c;
        switch (paramType.hashCode()) {
            case -1659644065:
                if (paramType.equals(TYPE_ALGO_DEFAULT_BRIGHTNESS)) {
                    c = 3;
                    break;
                }
            case -1040827554:
                if (paramType.equals(TYPE_EYE_PROTECT)) {
                    c = 4;
                    break;
                }
            case -212802098:
                if (paramType.equals(TYPE_WINDOW_MANAGER_BRIGHTNESS)) {
                    c = 2;
                    break;
                }
            case 327934849:
                if (paramType.equals("userScene")) {
                    c = 5;
                    break;
                }
            case 1149740116:
                if (paramType.equals(TYPE_TEMP_AUTO_BRIGHTNESS)) {
                    c = 1;
                    break;
                }
            case 1651298475:
                if (paramType.equals(TYPE_TEMP_MANUAL_BRIGHTNESS)) {
                    c = 0;
                    break;
                }
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
                return true;
            default:
                return false;
        }
    }

    public void sendMonitorParam(ArrayMap<String, Object> params) {
        if (params == null || !(params.get(DisplayEffectMonitor.MonitorModule.PARAM_TYPE) instanceof String)) {
            Slog.e(TAG, "sendMonitorParam() input params format error!");
            return;
        }
        String paramType = (String) params.get(DisplayEffectMonitor.MonitorModule.PARAM_TYPE);
        char c = 65535;
        switch (paramType.hashCode()) {
            case -1659644065:
                if (paramType.equals(TYPE_ALGO_DEFAULT_BRIGHTNESS)) {
                    c = 3;
                    break;
                }
                break;
            case -1040827554:
                if (paramType.equals(TYPE_EYE_PROTECT)) {
                    c = 4;
                    break;
                }
                break;
            case -212802098:
                if (paramType.equals(TYPE_WINDOW_MANAGER_BRIGHTNESS)) {
                    c = 2;
                    break;
                }
                break;
            case 327934849:
                if (paramType.equals("userScene")) {
                    c = 5;
                    break;
                }
                break;
            case 1149740116:
                if (paramType.equals(TYPE_TEMP_AUTO_BRIGHTNESS)) {
                    c = 1;
                    break;
                }
                break;
            case 1651298475:
                if (paramType.equals(TYPE_TEMP_MANUAL_BRIGHTNESS)) {
                    c = 0;
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
                tempManualBrightness(params);
                break;
            case 1:
                tempAutoBrightness(params);
                break;
            case 2:
                windowManagerBrightness(params);
                break;
            case 3:
                algoDefaultBrightness(params);
                break;
            case 4:
                eyeProtect(params);
                break;
            case 5:
                userScene(params);
                break;
            default:
                Slog.e(TAG, "sendMonitorParam() undefine paramType: " + paramType);
                break;
        }
    }

    public void triggerUploadTimer() {
        seekBarMovementUploader();
    }

    private void tempManualBrightness(ArrayMap<String, Object> params) {
        if (this.mCommonData.isProductEnable() && !this.mCommonData.isCommercialVersion() && tempManualBrightnessCheckParamValid(params)) {
            int brightness = ((Integer) params.get(PARAM_BRIGHTNESS)).intValue();
            String packageName = (String) params.get("packageName");
            tempManualBrightnessPrint(brightness, packageName);
            if (packageName.equals(MONITOR_TARGET_PACKAGE)) {
                this.mSeekBarRecoderManual.update(brightness);
            }
        }
    }

    private boolean tempManualBrightnessCheckParamValid(ArrayMap<String, Object> params) {
        if (params == null) {
            return false;
        }
        if (!(params.get(PARAM_BRIGHTNESS) instanceof Integer)) {
            Slog.e(TAG, "tempManualBrightnessCheckParamValid() can't get param: brightness");
            return false;
        } else if (params.get("packageName") instanceof String) {
            return true;
        } else {
            Slog.e(TAG, "tempManualBrightnessCheckParamValid() can't get param: packageName");
            return false;
        }
    }

    private void tempManualBrightnessPrint(int brightness, String packageName) {
        if (this.mTempManualBrightnessPackageNameLast == null) {
            this.mTempManualBrightnessPackageNameLast = packageName;
        }
        if (!this.mTempManualBrightnessPackageNameLast.equals(packageName)) {
            this.mTempManualBrightnessPrinter.changeName(brightness, packageName);
            this.mTempManualBrightnessPackageNameLast = packageName;
            return;
        }
        this.mTempManualBrightnessPrinter.updateParam(brightness, packageName);
    }

    private void tempAutoBrightness(ArrayMap<String, Object> params) {
        if (this.mCommonData.isProductEnable() && !this.mCommonData.isCommercialVersion() && tempAutoBrightnessCheckParamValid(params)) {
            int brightness = ((Integer) params.get(PARAM_BRIGHTNESS)).intValue();
            String packageName = (String) params.get("packageName");
            tempAutoBrightnessPrint(brightness, packageName);
            if (brightness != -1 && packageName.equals(MONITOR_TARGET_PACKAGE)) {
                this.mSeekBarRecoderAuto.update(brightness);
            }
        }
    }

    private boolean tempAutoBrightnessCheckParamValid(ArrayMap<String, Object> params) {
        if (params == null) {
            return false;
        }
        if (!(params.get(PARAM_BRIGHTNESS) instanceof Integer)) {
            Slog.e(TAG, "tempAutoBrightnessCheckParamValid() can't get param: brightness");
            return false;
        } else if (params.get("packageName") instanceof String) {
            return true;
        } else {
            Slog.e(TAG, "tempAutoBrightnessCheckParamValid() can't get param: packageName");
            return false;
        }
    }

    private void tempAutoBrightnessPrint(int brightness, String packageName) {
        if (this.mTempAutoBrightnessPackageNameLast == null) {
            this.mTempAutoBrightnessPackageNameLast = packageName;
        }
        if (!this.mTempAutoBrightnessPackageNameLast.equals(packageName)) {
            this.mTempAutoBrightnessPrinter.changeName(brightness, packageName);
            this.mTempAutoBrightnessPackageNameLast = packageName;
        } else if (brightness == -1) {
            this.mTempAutoBrightnessPrinter.resetParam(brightness, packageName);
        } else {
            this.mTempAutoBrightnessPrinter.updateParam(brightness, packageName);
        }
    }

    private void windowManagerBrightness(ArrayMap<String, Object> params) {
        if (this.mCommonData.isProductEnable() && !this.mCommonData.isCommercialVersion() && windowManagerBrightnessCheckParamValid(params)) {
            int brightness = ((Integer) params.get(PARAM_BRIGHTNESS)).intValue();
            String packageName = (String) params.get("packageName");
            windowManagerBrightnessPrint(brightness, packageName);
            if (!"android".equals(packageName) && brightness <= 255) {
                this.mSeekBarRecoderApp.update(this.mCommonData.getDiscountBrightness());
            }
        }
    }

    private boolean windowManagerBrightnessCheckParamValid(ArrayMap<String, Object> params) {
        if (params == null) {
            return false;
        }
        if (!(params.get(PARAM_BRIGHTNESS) instanceof Integer)) {
            Slog.e(TAG, "windowManagerBrightnessCheckParamValid() can't get param: brightness");
            return false;
        } else if (params.get("packageName") instanceof String) {
            return true;
        } else {
            Slog.e(TAG, "windowManagerBrightnessCheckParamValid() can't get param: packageName");
            return false;
        }
    }

    private void windowManagerBrightnessPrint(int brightness, String packageName) {
        if (this.mWindowManagerBrightnessPackageNameLast.equals(packageName)) {
            if (HWDEBUG) {
                Slog.d(TAG, "windowManagerBrightnessPrint() update brightness=" + brightness + ", packageName=" + packageName);
            }
            this.mWindowManagerBrightnessPrinter.updateParam(brightness, packageName);
        } else if (brightness != -255 || !packageName.equals("android")) {
            if (HWDEBUG) {
                Slog.d(TAG, "windowManagerBrightnessPrint() start brightness=" + brightness + ", packageName=" + packageName);
            }
            if (this.mWindowManagerBrightnessPackageNameLast.equals("android")) {
                this.mWindowManagerBrightnessPrinter.updateParam(brightness, packageName);
            } else {
                this.mWindowManagerBrightnessPrinter.changeName(brightness, packageName);
            }
            this.mWindowManagerBrightnessPackageNameLast = packageName;
            this.mCommonData.setWindowManagerBrightnessMode(true);
        } else {
            if (HWDEBUG) {
                Slog.d(TAG, "windowManagerBrightnessPrint() brightness reset to normal");
            }
            this.mWindowManagerBrightnessPrinter.resetParam(brightness, packageName);
            this.mWindowManagerBrightnessPackageNameLast = packageName;
            this.mCommonData.setWindowManagerBrightnessMode(false);
        }
    }

    private void algoDefaultBrightness(ArrayMap<String, Object> params) {
        if (this.mCommonData.isProductEnable() && !this.mCommonData.isCommercialVersion() && algoDefaultBrightnessCheckParamValid(params)) {
            this.mCommonData.setSmoothAmbientLight(((Integer) params.get(PARAM_LIGHT_VALUE)).intValue());
            this.mAlgoDefaultBrightness = ((Integer) params.get(PARAM_BRIGHTNESS)).intValue();
            String mode = (String) params.get(PARAM_BRIGHTNESS_MODE);
            if (HWDEBUG) {
                Slog.d(TAG, "algoDefaultBrightness() mAlgoDefaultBrightness=" + this.mAlgoDefaultBrightness);
            }
            if (mode.equals("MANUAL")) {
                this.mIsManualModeReceiveAmbientLight = true;
            }
        }
    }

    private boolean algoDefaultBrightnessCheckParamValid(ArrayMap<String, Object> params) {
        if (params == null) {
            return false;
        }
        if (!(params.get(PARAM_LIGHT_VALUE) instanceof Integer)) {
            Slog.e(TAG, "algoDefaultBrightnessCheckParamValid() can't get param: lightValue");
            return false;
        } else if (!(params.get(PARAM_BRIGHTNESS) instanceof Integer)) {
            Slog.e(TAG, "algoDefaultBrightnessCheckParamValid() can't get param: brightness");
            return false;
        } else if (params.get(PARAM_BRIGHTNESS_MODE) instanceof String) {
            return true;
        } else {
            Slog.e(TAG, "algoDefaultBrightnessCheckParamValid() can't get param: brightnessMode");
            return false;
        }
    }

    private void eyeProtect(ArrayMap<String, Object> params) {
        if (this.mCommonData.isProductEnable() && !this.mCommonData.isCommercialVersion() && params != null) {
            Object enable = params.get(PARAM_IS_ENABLE);
            if (!(enable instanceof Boolean)) {
                Slog.e(TAG, "eyeProtect() can't get param: isEnable");
                return;
            }
            this.mIsEyeProtectEnable = ((Boolean) enable).booleanValue();
            if (HWDEBUG) {
                Slog.d(TAG, "eyeProtect() mIsEyeProtectEnable=" + this.mIsEyeProtectEnable);
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean isPowerSaveMode() {
        return 2 != SystemProperties.getInt(POWER_MODE_PROP, 2);
    }

    private void userScene(ArrayMap<String, Object> params) {
        if (this.mCommonData.isProductEnable() && !this.mCommonData.isCommercialVersion() && params != null) {
            Object userScene = params.get("userScene");
            if (!(userScene instanceof Integer)) {
                Slog.e(TAG, "userScene() can't get param: userScene");
                return;
            }
            this.mUserScene = ((Integer) userScene).intValue();
            if (HWDEBUG) {
                Slog.d(TAG, "userScene() mUserScene=" + this.mUserScene);
            }
        }
    }

    private void seekBarMovementUploader() {
        if (!this.mCommonData.isCommercialVersion()) {
            List<SeekBarMovement> autoList = this.mSeekBarRecoderAuto.getUploadList();
            List<SeekBarMovement> manualList = this.mSeekBarRecoderManual.getUploadList();
            List<SeekBarMovement> appList = this.mSeekBarRecoderApp.getUploadList();
            List<SeekBarMovement> uploadList = new ArrayList<>();
            uploadList.addAll(autoList);
            uploadList.addAll(manualList);
            uploadList.addAll(appList);
            Collections.sort(uploadList, mComparator);
            autoList.clear();
            manualList.clear();
            appList.clear();
            if (!uploadList.isEmpty()) {
                for (SeekBarMovement movement : uploadList) {
                    if (movement.startLevel != movement.endLevel) {
                        this.mSeekBarMovementUploader.addData(movement);
                    }
                }
                this.mSeekBarMovementUploader.upload();
            }
            this.mSeekBarMovementUploader.hourlyTrigger();
        }
    }
}
