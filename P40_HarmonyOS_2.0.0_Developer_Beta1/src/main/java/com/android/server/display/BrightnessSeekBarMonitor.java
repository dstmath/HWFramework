package com.android.server.display;

import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.ArrayMap;
import android.util.IMonitor;
import android.util.Log;
import android.util.Slog;
import com.android.server.display.BackLightCommonData;
import com.android.server.display.DisplayEffectMonitor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/* access modifiers changed from: package-private */
public class BrightnessSeekBarMonitor implements DisplayEffectMonitor.MonitorModule {
    private static final int BRIGHTNESS_ADJUST_END = -1;
    private static final boolean HWDEBUG = (Log.HWLog || (Log.HWModuleLog && Log.isLoggable(TAG, 3)));
    private static final boolean HWFLOW;
    private static final int INVALID_USER_SCENE = -1;
    private static final String MONITOR_TARGET_PACKAGE = "android.uid.systemui";
    private static final Comparator<SeekBarMovement> MOVEMENT_COMPARATOR = new Comparator<SeekBarMovement>() {
        /* class com.android.server.display.BrightnessSeekBarMonitor.AnonymousClass1 */

        public int compare(SeekBarMovement a, SeekBarMovement b) {
            return (int) (a.startTime - b.startTime);
        }
    };
    private static final String PARAM_BRIGHTNESS = "brightness";
    private static final String PARAM_BRIGHTNESS_MODE = "brightnessMode";
    private static final String PARAM_DISPLAY_MODE = "displayMode";
    private static final String PARAM_IS_ENABLE = "isEnable";
    private static final String PARAM_LIGHT_VALUE = "lightValue";
    private static final String PARAM_PACKAGE_NAME = "packageName";
    private static final String PARAM_USER_SCENE = "userScene";
    private static final String TAG = "BrightnessSeekBarMonitor";
    private static final String TYPE_ALGO_DEFAULT_BRIGHTNESS = "algoDefaultBrightness";
    private static final String TYPE_DISPLAY_MODE = "displayMode";
    private static final String TYPE_EYE_PROTECT = "eyeProtect";
    private static final String TYPE_TEMP_AUTO_BRIGHTNESS = "tempAutoBrightness";
    private static final String TYPE_TEMP_MANUAL_BRIGHTNESS = "tempManualBrightness";
    private static final String TYPE_USER_SCENE = "userScene";
    private static final String TYPE_WINDOW_MANAGER_BRIGHTNESS = "windowManagerBrightness";
    private static final String WINDOW_MANAGER_RESET_PACKAGE = "android";
    private int mAlgoDefaultBrightness;
    private final BackLightCommonData mCommonData;
    private int mDisplayMode = 0;
    private boolean mIsEyeProtectEnable;
    private boolean mIsManualModeReceiveAmbientLight;
    private final DisplayEffectMonitor mMonitor;
    private SeekBarMovementUploader mSeekBarMovementUploader;
    private SeekBarRecorderApp mSeekBarRecorderApp;
    private SeekBarRecorderAuto mSeekBarRecorderAuto;
    private SeekBarRecorderManual mSeekBarRecorderManual;
    private String mTempAutoBrightnessPackageNameLast;
    private DisplayEffectMonitor.ParamLogPrinter mTempAutoBrightnessPrinter;
    private String mTempManualBrightnessPackageNameLast;
    private DisplayEffectMonitor.ParamLogPrinter mTempManualBrightnessPrinter;
    private int mUserScene = -1;
    private String mWindowManagerBrightnessPackageNameLast = "android";
    private DisplayEffectMonitor.ParamLogPrinter mWindowManagerBrightnessPrinter;

    static {
        boolean z = false;
        if (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4))) {
            z = true;
        }
        HWFLOW = z;
    }

    BrightnessSeekBarMonitor(DisplayEffectMonitor monitor, BackLightMonitorManager manager) {
        this.mMonitor = monitor;
        this.mCommonData = manager.getBackLightCommonData();
        if (!this.mCommonData.isCommercialVersion()) {
            this.mSeekBarRecorderAuto = new SeekBarRecorderAuto();
            this.mSeekBarRecorderManual = new SeekBarRecorderManual();
            this.mSeekBarRecorderApp = new SeekBarRecorderApp();
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

    @Override // com.android.server.display.DisplayEffectMonitor.MonitorModule
    public boolean isParamOwner(String paramType) {
        if (paramType == null) {
            return false;
        }
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
            case 1714132357:
                if (paramType.equals("displayMode")) {
                    c = 6;
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
                return true;
            default:
                return false;
        }
    }

    @Override // com.android.server.display.DisplayEffectMonitor.MonitorModule
    public void sendMonitorParam(String paramType, ArrayMap<String, Object> params) {
        if (paramType == null) {
            Slog.e(TAG, "paramType is null");
            return;
        }
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
            case 1714132357:
                if (paramType.equals("displayMode")) {
                    c = 6;
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
                receiveParamForTempManualBrightness(params);
                return;
            case 1:
                receiveParamForTempAutoBrightness(params);
                return;
            case 2:
                receiveParamForWindowManagerBrightness(params);
                return;
            case 3:
                receiveParamForAlgoDefaultBrightness(params);
                return;
            case 4:
                receiveParamForEyeProtect(params);
                return;
            case 5:
                receiveParamForUserScene(params);
                return;
            case 6:
                receiveParamForDisplayMode(params);
                return;
            default:
                Slog.e(TAG, "sendMonitorParam() undefine paramType: " + paramType);
                return;
        }
    }

    @Override // com.android.server.display.DisplayEffectMonitor.MonitorModule
    public void triggerUploadTimer() {
        uploadSeekBarMovement();
    }

    private void receiveParamForTempManualBrightness(ArrayMap<String, Object> params) {
        if (this.mCommonData.isProductEnable() && !this.mCommonData.isCommercialVersion()) {
            Object brightnessObj = params.get(PARAM_BRIGHTNESS);
            if (!(brightnessObj instanceof Integer)) {
                Slog.e(TAG, "receiveParamForTempManualBrightness() can't get param: brightness");
                return;
            }
            int brightness = ((Integer) brightnessObj).intValue();
            Object packageNameObj = params.get("packageName");
            if (!(packageNameObj instanceof String)) {
                Slog.e(TAG, "receiveParamForTempManualBrightness() can't get param: packageName");
                return;
            }
            String packageName = (String) packageNameObj;
            printTempManualBrightness(brightness, packageName);
            if ("android.uid.systemui".equals(packageName)) {
                this.mSeekBarRecorderManual.update(brightness);
            }
        }
    }

    private void printTempManualBrightness(int brightness, String packageName) {
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

    private void receiveParamForTempAutoBrightness(ArrayMap<String, Object> params) {
        if (this.mCommonData.isProductEnable() && !this.mCommonData.isCommercialVersion()) {
            Object brightnessObj = params.get(PARAM_BRIGHTNESS);
            if (!(brightnessObj instanceof Integer)) {
                Slog.e(TAG, "receiveParamForTempAutoBrightness() can't get param: brightness");
                return;
            }
            int brightness = ((Integer) brightnessObj).intValue();
            Object packageNameObj = params.get("packageName");
            if (!(packageNameObj instanceof String)) {
                Slog.e(TAG, "receiveParamForTempAutoBrightness() can't get param: packageName");
                return;
            }
            String packageName = (String) packageNameObj;
            printTempAutoBrightness(brightness, packageName);
            if (brightness != -1 && "android.uid.systemui".equals(packageName)) {
                this.mSeekBarRecorderAuto.update(brightness);
            }
        }
    }

    private void printTempAutoBrightness(int brightness, String packageName) {
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

    private void receiveParamForWindowManagerBrightness(ArrayMap<String, Object> params) {
        if (this.mCommonData.isProductEnable() && !this.mCommonData.isCommercialVersion()) {
            Object brightnessObj = params.get(PARAM_BRIGHTNESS);
            if (!(brightnessObj instanceof Integer)) {
                Slog.e(TAG, "receiveParamForWindowManagerBrightness() can't get param: brightness");
                return;
            }
            int brightness = ((Integer) brightnessObj).intValue();
            Object packageNameObj = params.get("packageName");
            if (!(packageNameObj instanceof String)) {
                Slog.e(TAG, "receiveParamForWindowManagerBrightness() can't get param: packageName");
                return;
            }
            String packageName = (String) packageNameObj;
            printWindowManagerBrightness(brightness, packageName);
            if (!"android".equals(packageName) && brightness <= 255) {
                this.mSeekBarRecorderApp.update(this.mCommonData.getDiscountBrightness());
            }
        }
    }

    private void printWindowManagerBrightness(int brightness, String packageName) {
        if (this.mWindowManagerBrightnessPackageNameLast.equals(packageName)) {
            if (HWDEBUG) {
                Slog.d(TAG, "printWindowManagerBrightness() update brightness=" + brightness + ", packageName=" + packageName);
            }
            this.mWindowManagerBrightnessPrinter.updateParam(brightness, packageName);
        } else if (brightness != -255 || !"android".equals(packageName)) {
            if (HWDEBUG) {
                Slog.d(TAG, "printWindowManagerBrightness() start brightness=" + brightness + ", packageName=" + packageName);
            }
            if ("android".equals(this.mWindowManagerBrightnessPackageNameLast)) {
                this.mWindowManagerBrightnessPrinter.updateParam(brightness, packageName);
            } else {
                this.mWindowManagerBrightnessPrinter.changeName(brightness, packageName);
            }
            this.mWindowManagerBrightnessPackageNameLast = packageName;
            this.mCommonData.setWindowManagerBrightnessMode(true);
        } else {
            if (HWDEBUG) {
                Slog.d(TAG, "printWindowManagerBrightness() brightness reset to normal");
            }
            this.mWindowManagerBrightnessPrinter.resetParam(brightness, packageName);
            this.mWindowManagerBrightnessPackageNameLast = packageName;
            this.mCommonData.setWindowManagerBrightnessMode(false);
        }
    }

    private void receiveParamForAlgoDefaultBrightness(ArrayMap<String, Object> params) {
        if (this.mCommonData.isProductEnable() && !this.mCommonData.isCommercialVersion()) {
            Object lightValueObj = params.get(PARAM_LIGHT_VALUE);
            if (!(lightValueObj instanceof Integer)) {
                Slog.e(TAG, "receiveParamForAlgoDefaultBrightness() can't get param: lightValue");
                return;
            }
            Object brightnessObj = params.get(PARAM_BRIGHTNESS);
            if (!(brightnessObj instanceof Integer)) {
                Slog.e(TAG, "receiveParamForAlgoDefaultBrightness() can't get param: brightness");
                return;
            }
            Object brightnessModeObj = params.get(PARAM_BRIGHTNESS_MODE);
            if (!(brightnessModeObj instanceof String)) {
                Slog.e(TAG, "receiveParamForAlgoDefaultBrightness() can't get param: brightnessMode");
                return;
            }
            this.mCommonData.setSmoothAmbientLight(((Integer) lightValueObj).intValue());
            this.mAlgoDefaultBrightness = ((Integer) brightnessObj).intValue();
            String brightnessMode = (String) brightnessModeObj;
            if (HWDEBUG) {
                Slog.d(TAG, "receiveParamForAlgoDefaultBrightness() mAlgoDefaultBrightness=" + this.mAlgoDefaultBrightness);
            }
            if ("MANUAL".equals(brightnessMode)) {
                this.mIsManualModeReceiveAmbientLight = true;
            }
        }
    }

    private void receiveParamForEyeProtect(ArrayMap<String, Object> params) {
        if (this.mCommonData.isProductEnable() && !this.mCommonData.isCommercialVersion()) {
            Object enableObj = params.get(PARAM_IS_ENABLE);
            if (!(enableObj instanceof Boolean)) {
                Slog.e(TAG, "receiveParamForEyeProtect() can't get param: isEnable");
                return;
            }
            this.mIsEyeProtectEnable = ((Boolean) enableObj).booleanValue();
            if (HWDEBUG) {
                Slog.d(TAG, "receiveParamForEyeProtect() mIsEyeProtectEnable=" + this.mIsEyeProtectEnable);
            }
        }
    }

    private void receiveParamForUserScene(ArrayMap<String, Object> params) {
        if (this.mCommonData.isProductEnable() && !this.mCommonData.isCommercialVersion()) {
            Object userSceneObj = params.get("userScene");
            if (!(userSceneObj instanceof Integer)) {
                Slog.e(TAG, "receiveParamForUserScene() can't get param: userScene");
                return;
            }
            this.mUserScene = ((Integer) userSceneObj).intValue();
            if (HWDEBUG) {
                Slog.d(TAG, "receiveParamForUserScene() mUserScene=" + this.mUserScene);
            }
        }
    }

    private void receiveParamForDisplayMode(ArrayMap<String, Object> params) {
        if (params != null && this.mCommonData.isProductEnable() && !this.mCommonData.isCommercialVersion()) {
            Object displayModeObj = params.get("displayMode");
            if (!(displayModeObj instanceof Integer)) {
                Slog.e(TAG, "receiveParamForDisplayMode() can't get param: displayMode");
                return;
            }
            this.mDisplayMode = ((Integer) displayModeObj).intValue();
            if (HWDEBUG) {
                Slog.d(TAG, "receiveParamForDisplayMode() mDisplayMode=" + this.mDisplayMode);
            }
        }
    }

    /* access modifiers changed from: private */
    public enum SeekBarBrightMode {
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

    /* access modifiers changed from: private */
    public static class SeekBarMovement {
        int algoDefaultLevel;
        int displayMode;
        int endLevel;
        long endTime;
        String foregroundPackageName;
        boolean isEyeProtectEnable;
        boolean isInPowerSave;
        boolean isThermalLimited;
        SeekBarBrightMode mode;
        int startLevel;
        int startLux;
        long startTime;
        long startTimeRtc;
        int userScene;

        private SeekBarMovement() {
        }

        public String toString() {
            StringBuilder result = new StringBuilder(200);
            result.append("SeekBarMovement ");
            result.append(this.mode);
            result.append(", ");
            result.append(this.startLevel);
            result.append(" -> ");
            result.append(this.endLevel);
            result.append(", lux ");
            result.append(this.startLux);
            result.append(", last ");
            result.append(this.endTime - this.startTime);
            result.append("ms");
            result.append(", default ");
            result.append(this.algoDefaultLevel);
            result.append(", app ");
            result.append(this.foregroundPackageName);
            result.append(", protectEye ");
            result.append(this.isEyeProtectEnable);
            result.append(", powerSave ");
            result.append(this.isInPowerSave);
            result.append(", thermalLimited ");
            result.append(this.isThermalLimited);
            result.append(", userScene ");
            result.append(this.userScene);
            result.append(", displayMode ");
            result.append(this.displayMode);
            return result.toString();
        }
    }

    /* access modifiers changed from: private */
    public class SeekBarRecorderAuto extends SeekBarRecorder {
        private static final int UI_NIGHT_MODE_ON_SCENE = 100;

        private SeekBarRecorderAuto() {
            super();
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.display.BrightnessSeekBarMonitor.SeekBarRecorder
        public boolean shouldIgnoreUpdate() {
            return BrightnessSeekBarMonitor.this.mCommonData.isWindowManagerBrightnessMode();
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.display.BrightnessSeekBarMonitor.SeekBarRecorder
        public void start(SeekBarMovement movement, int level, long time) {
            movement.mode = SeekBarBrightMode.AUTO;
            movement.startTime = time;
            movement.startTimeRtc = System.currentTimeMillis();
            movement.startLevel = level;
            movement.startLux = BrightnessSeekBarMonitor.this.mCommonData.getSmoothAmbientLight();
            movement.algoDefaultLevel = BrightnessSeekBarMonitor.this.mAlgoDefaultBrightness;
            movement.foregroundPackageName = BrightnessSeekBarMonitor.this.mMonitor.getCurrentTopAppName();
            movement.isEyeProtectEnable = BrightnessSeekBarMonitor.this.mIsEyeProtectEnable;
            movement.isInPowerSave = isPowerSaveMode();
            movement.isThermalLimited = BrightnessSeekBarMonitor.this.mCommonData.isThermalLimited();
            movement.userScene = BrightnessSeekBarMonitor.this.mUserScene;
            if (BrightnessSeekBarMonitor.this.mMonitor.isUiNightModeOn()) {
                movement.userScene += 100;
            }
            movement.displayMode = BrightnessSeekBarMonitor.this.mDisplayMode;
        }
    }

    /* access modifiers changed from: private */
    public class SeekBarRecorderManual extends SeekBarRecorder {
        private SeekBarRecorderManual() {
            super();
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.display.BrightnessSeekBarMonitor.SeekBarRecorder
        public boolean shouldIgnoreUpdate() {
            return !BrightnessSeekBarMonitor.this.mIsManualModeReceiveAmbientLight || BrightnessSeekBarMonitor.this.mCommonData.isWindowManagerBrightnessMode();
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.display.BrightnessSeekBarMonitor.SeekBarRecorder
        public void start(SeekBarMovement movement, int level, long time) {
            movement.mode = SeekBarBrightMode.MANUAL;
            movement.startTime = time;
            movement.startTimeRtc = System.currentTimeMillis();
            movement.startLevel = level;
            movement.startLux = 0;
            movement.algoDefaultLevel = BrightnessSeekBarMonitor.this.mAlgoDefaultBrightness;
            movement.foregroundPackageName = BrightnessSeekBarMonitor.this.mMonitor.getCurrentTopAppName();
            movement.isEyeProtectEnable = BrightnessSeekBarMonitor.this.mIsEyeProtectEnable;
            movement.isInPowerSave = isPowerSaveMode();
            movement.isThermalLimited = BrightnessSeekBarMonitor.this.mCommonData.isThermalLimited();
            movement.userScene = BrightnessSeekBarMonitor.this.mUserScene;
            movement.displayMode = BrightnessSeekBarMonitor.this.mDisplayMode;
        }
    }

    /* access modifiers changed from: private */
    public class SeekBarRecorderApp extends SeekBarRecorder {
        private SeekBarRecorderApp() {
            super();
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.display.BrightnessSeekBarMonitor.SeekBarRecorder
        public boolean shouldIgnoreUpdate() {
            return BrightnessSeekBarMonitor.this.mCommonData.getBrightnessMode() == BackLightCommonData.BrightnessMode.MANUAL;
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.display.BrightnessSeekBarMonitor.SeekBarRecorder
        public void start(SeekBarMovement movement, int level, long time) {
            movement.mode = SeekBarBrightMode.APP;
            movement.startTime = time;
            movement.startTimeRtc = System.currentTimeMillis();
            movement.startLevel = level;
            movement.startLux = BrightnessSeekBarMonitor.this.mCommonData.getSmoothAmbientLight();
            movement.algoDefaultLevel = BrightnessSeekBarMonitor.this.mAlgoDefaultBrightness;
            movement.foregroundPackageName = BrightnessSeekBarMonitor.this.mWindowManagerBrightnessPackageNameLast;
            movement.endTime = time;
            movement.endLevel = level;
            movement.isEyeProtectEnable = BrightnessSeekBarMonitor.this.mIsEyeProtectEnable;
            movement.isInPowerSave = isPowerSaveMode();
            movement.isThermalLimited = BrightnessSeekBarMonitor.this.mCommonData.isThermalLimited();
            movement.userScene = BrightnessSeekBarMonitor.this.mUserScene;
            movement.displayMode = BrightnessSeekBarMonitor.this.mDisplayMode;
        }
    }

    private abstract class SeekBarRecorder {
        private static final int MOVE_INTERVAL_TIME_IN_MS = 2000;
        private static final int MOVE_LIST_INTERVAL_TIME_IN_MS = 15000;
        private static final int MOVE_MERGE_LUX_CHANGE_MIN_THRESHOLD = 5;
        private static final float MOVE_MERGE_LUX_CHANGE_RATIO = 0.5f;
        private static final int POWER_MODE_DEFAULT = 2;
        private static final String POWER_MODE_PROP = "persist.sys.smart_power";
        private SeekBarMovement mCurrentMovement;
        private final List<SeekBarMovement> mMovementList;

        /* access modifiers changed from: protected */
        public abstract boolean shouldIgnoreUpdate();

        /* access modifiers changed from: protected */
        public abstract void start(SeekBarMovement seekBarMovement, int i, long j);

        private SeekBarRecorder() {
            this.mMovementList = new ArrayList();
        }

        public void update(int level) {
            if (!shouldIgnoreUpdate()) {
                long currentTime = SystemClock.elapsedRealtime();
                SeekBarMovement seekBarMovement = this.mCurrentMovement;
                if (seekBarMovement == null) {
                    this.mCurrentMovement = new SeekBarMovement();
                    start(this.mCurrentMovement, level, currentTime);
                } else if (seekBarMovement.endTime == 0) {
                    if (currentTime - this.mCurrentMovement.startTime > 2000) {
                        start(this.mCurrentMovement, level, currentTime);
                        return;
                    }
                    SeekBarMovement seekBarMovement2 = this.mCurrentMovement;
                    seekBarMovement2.endTime = currentTime;
                    seekBarMovement2.endLevel = level;
                } else if (currentTime - this.mCurrentMovement.endTime > 2000) {
                    end(this.mCurrentMovement);
                    this.mCurrentMovement = new SeekBarMovement();
                    start(this.mCurrentMovement, level, currentTime);
                } else {
                    SeekBarMovement seekBarMovement3 = this.mCurrentMovement;
                    seekBarMovement3.endTime = currentTime;
                    seekBarMovement3.endLevel = level;
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
                    Slog.i(BrightnessSeekBarMonitor.TAG, "SeekBarRecorder record " + movement);
                }
            }
        }

        private void checkUnfinished() {
            if (this.mCurrentMovement != null) {
                long currentTime = SystemClock.elapsedRealtime();
                if (this.mCurrentMovement.endTime != 0 && currentTime - this.mCurrentMovement.endTime > 2000) {
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
                    }
                    if (now != null) {
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
            if (now.startTime - pre.endTime > 15000) {
                return false;
            }
            int deltaLevel = Math.abs(now.startLux - pre.startLux);
            int deltaLevelThresholdTmp = (int) (((float) pre.startLux) * 0.5f);
            int deltaLevelThreshold = 5;
            if (deltaLevelThresholdTmp > 5) {
                deltaLevelThreshold = deltaLevelThresholdTmp;
            }
            if (deltaLevel >= deltaLevelThreshold) {
                return false;
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean isPowerSaveMode() {
            return SystemProperties.getInt(POWER_MODE_PROP, 2) != 2;
        }
    }

    private void uploadSeekBarMovement() {
        if (!this.mCommonData.isCommercialVersion()) {
            List<SeekBarMovement> autoList = this.mSeekBarRecorderAuto.getUploadList();
            List<SeekBarMovement> manualList = this.mSeekBarRecorderManual.getUploadList();
            List<SeekBarMovement> appList = this.mSeekBarRecorderApp.getUploadList();
            List<SeekBarMovement> uploadList = new ArrayList<>();
            uploadList.addAll(autoList);
            uploadList.addAll(manualList);
            uploadList.addAll(appList);
            Collections.sort(uploadList, MOVEMENT_COMPARATOR);
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
            this.mSeekBarMovementUploader.triggerEveryHour();
        }
    }

    /* access modifiers changed from: private */
    public static class SeekBarMovementUploader {
        private static final int HOURS_PER_DAY = 24;
        private static final int IMONITOR_EVENT_ID = 932010102;
        private static final int UPLOAD_TIMES_LIMIT_PER_DAY = 30;
        private static final int UPLOAD_TIMES_LIMIT_PER_HOUR = 10;
        private int mHourCount;
        private final List<SeekBarMovementUploadData> mUploadList = new ArrayList();
        private int mUploadTimesPerDay;

        /* access modifiers changed from: private */
        public static class SeekBarMovementUploadData {
            short adjustEndLevel;
            short adjustStartLevel;
            short algoCalcDefaultLevel;
            short ambientLightLux;
            short displayMode;
            String foregroundPackageName;
            byte mode;
            boolean powerSave;
            boolean protectEye;
            long startTimeRtc;
            boolean thermalLimited;
            short userScene;

            private SeekBarMovementUploadData() {
            }

            public String toString() {
                StringBuilder result = new StringBuilder(200);
                result.append("SeekBarMovementUploadData mode ");
                result.append((int) this.mode);
                result.append(", ");
                result.append((int) this.adjustStartLevel);
                result.append(" -> ");
                result.append((int) this.adjustEndLevel);
                result.append(", ambientLightLux ");
                result.append((int) this.ambientLightLux);
                result.append(", algoCalcDefaultLevel ");
                result.append((int) this.algoCalcDefaultLevel);
                result.append(", app ");
                result.append(this.foregroundPackageName);
                result.append(", protectEye ");
                result.append(this.protectEye);
                result.append(", powerSave ");
                result.append(this.powerSave);
                result.append(", thermalLimited ");
                result.append(this.thermalLimited);
                result.append(", userScene ");
                result.append((int) this.userScene);
                result.append(", displayMode ");
                result.append((int) this.displayMode);
                return result.toString();
            }
        }

        SeekBarMovementUploader() {
        }

        public void addData(SeekBarMovement movement) {
            int i;
            if (this.mUploadList.size() < 10 && (i = this.mUploadTimesPerDay) < 30) {
                this.mUploadTimesPerDay = i + 1;
                SeekBarMovementUploadData data = new SeekBarMovementUploadData();
                data.mode = movement.mode.getId();
                data.startTimeRtc = movement.startTimeRtc;
                data.adjustStartLevel = (short) movement.startLevel;
                data.adjustEndLevel = (short) movement.endLevel;
                int i2 = 32767;
                if (movement.startLux < 32767) {
                    i2 = movement.startLux;
                }
                data.ambientLightLux = (short) i2;
                data.algoCalcDefaultLevel = (short) movement.algoDefaultLevel;
                data.foregroundPackageName = movement.foregroundPackageName;
                data.protectEye = movement.isEyeProtectEnable;
                data.powerSave = movement.isInPowerSave;
                data.thermalLimited = movement.isThermalLimited;
                data.userScene = (short) movement.userScene;
                data.displayMode = (short) movement.displayMode;
                this.mUploadList.add(data);
            }
        }

        public void upload() {
            if (!this.mUploadList.isEmpty()) {
                for (SeekBarMovementUploadData data : this.mUploadList) {
                    IMonitor.EventStream stream = IMonitor.openEventStream((int) IMONITOR_EVENT_ID);
                    stream.setParam("Mode", data.mode);
                    stream.setParam("AdjustStartLevel", data.adjustStartLevel);
                    stream.setParam("AdjustEndLevel", data.adjustEndLevel);
                    stream.setParam("AmbientLightLux", data.ambientLightLux);
                    stream.setParam("AlgoCalcDefaultLevel", data.algoCalcDefaultLevel);
                    stream.setParam("ForegroundPackageName", data.foregroundPackageName);
                    stream.setParam("ProtectEye", Boolean.valueOf(data.protectEye));
                    stream.setParam("PowerSave", Boolean.valueOf(data.powerSave));
                    stream.setParam("Thermal", Boolean.valueOf(data.thermalLimited));
                    if (data.userScene != -1) {
                        stream.setParam("Scene", data.userScene);
                    }
                    stream.setParam("DisplayMode", data.displayMode);
                    stream.setTime(data.startTimeRtc);
                    IMonitor.sendEvent(stream);
                    IMonitor.closeEventStream(stream);
                    if (BrightnessSeekBarMonitor.HWFLOW) {
                        Slog.i(BrightnessSeekBarMonitor.TAG, "SeekBarMovementUploader.upload() " + data);
                    }
                }
                this.mUploadList.clear();
            }
        }

        public void triggerEveryHour() {
            int i = this.mHourCount + 1;
            this.mHourCount = i;
            if (i >= 24) {
                this.mHourCount = 0;
                this.mUploadTimesPerDay = 0;
            }
        }
    }
}
