package com.android.server.display;

import android.os.SystemClock;
import android.util.ArrayMap;
import android.util.IMonitor;
import android.util.IMonitor.EventStream;
import android.util.Log;
import android.util.Slog;
import com.android.server.display.DisplayEffectMonitor.MonitorModule;
import com.android.server.display.DisplayEffectMonitor.ParamLogPrinter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

class BrightnessSeekBarMonitor implements MonitorModule {
    private static final boolean HWDEBUG;
    private static final boolean HWFLOW;
    private static final boolean HWLOGWE;
    private static final String MONITOR_TARGET_PACKAGE = "android.uid.systemui";
    private static final long MOVE_INTERVAL_TIME_IN_MS = 2000;
    private static final long MOVE_LIST_INTERVAL_TIME_IN_MS = 15000;
    private static final int MOVE_MERGE_LUX_CHANGE_MIN_THRESHOLD = 5;
    private static final int MOVE_MERGE_LUX_CHANGE_PERCENT = 50;
    private static final String PARAM_BRIGHTNESS = "brightness";
    private static final String PARAM_BRIGHTNESS_MODE = "brightnessMode";
    private static final String PARAM_LIGHT_VALUE = "lightValue";
    private static final String PARAM_PACKAGE_NAME = "packageName";
    private static final String TAG = "BrightnessSeekBarMonitor";
    private static final String TYPE_ALGO_DEFAULT_BRIGHTNESS = "algoDefaultBrightness";
    private static final String TYPE_TEMP_AUTO_BRIGHTNESS = "tempAutoBrightness";
    private static final String TYPE_TEMP_MANUAL_BRIGHTNESS = "tempManualBrightness";
    private static final String TYPE_WINDOW_MANAGER_BRIGHTNESS = "windowManagerBrightness";
    private static final Comparator<SeekBarMovement> mComparator = new Comparator<SeekBarMovement>() {
        public int compare(SeekBarMovement a, SeekBarMovement b) {
            return (int) (a.startTime - b.startTime);
        }
    };
    private int mAlgoDefaultBrightness;
    private int mAlgoSmoothLightValue;
    private SeekBarMovement mAutoModeMovement;
    private List<SeekBarMovement> mAutoModeMovementList = new ArrayList();
    private boolean mInWindowManagerBrightnessMode;
    private boolean mIsManualModeReceiveAmbientLight;
    private SeekBarMovement mManualModeMovement;
    private List<SeekBarMovement> mManualModeMovementList = new ArrayList();
    private final DisplayEffectMonitor mMonitor;
    private SeekBarMovementUploader mSeekBarMovementUploader;
    private String mTempAutoBrightnessPackageNameLast;
    private ParamLogPrinter mTempAutoBrightnessPrinter;
    private String mTempManualBrightnessPackageNameLast;
    private ParamLogPrinter mTempManualBrightnessPrinter;

    private static class SeekBarMovement {
        public int algoDefaultLevel;
        public int endLevel;
        public long endTime;
        public String foregroundPackageName;
        public boolean isAutoMode;
        public int startLevel;
        public int startLux;
        public long startTime;

        /* synthetic */ SeekBarMovement(SeekBarMovement -this0) {
            this();
        }

        private SeekBarMovement() {
        }

        public String toString() {
            return "SeekBarMovement " + (this.isAutoMode ? "auto" : "manual") + ", " + this.startLevel + " -> " + this.endLevel + ", lux " + this.startLux + ", last " + (this.endTime - this.startTime) + "ms" + ", default " + this.algoDefaultLevel + ", app " + this.foregroundPackageName;
        }
    }

    private static class SeekBarMovementUploader {
        private static final byte MODE_AUTO = (byte) 1;
        private static final byte MODE_MANUAL = (byte) 2;
        private static final int UPLOAD_TIMES_LIMIT_PER_HOUR = 3;
        private List<SeekBarMovementUploadData> mUploadList = new ArrayList();

        private static class SeekBarMovementUploadData {
            public short adjustEndLevel;
            public short adjustStartLevel;
            public short algoCalcDefaultLevel;
            public short ambientLightLux;
            public String foregroundPackageName;
            public byte mode;

            /* synthetic */ SeekBarMovementUploadData(SeekBarMovementUploadData -this0) {
                this();
            }

            private SeekBarMovementUploadData() {
            }

            public String toString() {
                return "SeekBarMovementUploadData mode " + this.mode + ", " + this.adjustStartLevel + " -> " + this.adjustEndLevel + ", ambientLightLux " + this.ambientLightLux + ", algoCalcDefaultLevel " + this.algoCalcDefaultLevel + ", app " + this.foregroundPackageName;
            }
        }

        public void addData(SeekBarMovement movement) {
            if (this.mUploadList.size() < 3) {
                int i;
                SeekBarMovementUploadData data = new SeekBarMovementUploadData();
                data.mode = movement.isAutoMode ? (byte) 1 : (byte) 2;
                data.adjustStartLevel = (short) movement.startLevel;
                data.adjustEndLevel = (short) movement.endLevel;
                if (movement.startLux < 32767) {
                    i = movement.startLux;
                } else {
                    i = 32767;
                }
                data.ambientLightLux = (short) i;
                data.algoCalcDefaultLevel = (short) movement.algoDefaultLevel;
                data.foregroundPackageName = movement.foregroundPackageName;
                this.mUploadList.add(data);
            }
        }

        public void upload() {
            if (!this.mUploadList.isEmpty()) {
                for (SeekBarMovementUploadData data : this.mUploadList) {
                    EventStream stream = IMonitor.openEventStream(932010102);
                    stream.setParam((short) 0, data.mode);
                    stream.setParam((short) 1, data.adjustStartLevel);
                    stream.setParam((short) 2, data.adjustEndLevel);
                    stream.setParam((short) 3, data.ambientLightLux);
                    stream.setParam((short) 4, data.algoCalcDefaultLevel);
                    stream.setParam((short) 5, data.foregroundPackageName);
                    IMonitor.sendEvent(stream);
                    IMonitor.closeEventStream(stream);
                    if (BrightnessSeekBarMonitor.HWFLOW) {
                        Slog.i(BrightnessSeekBarMonitor.TAG, "SeekBarMovementUploader.upload() " + data);
                    }
                }
                this.mUploadList.clear();
            }
        }
    }

    static {
        boolean z = true;
        boolean isLoggable = !Log.HWLog ? Log.HWModuleLog ? Log.isLoggable(TAG, 3) : false : true;
        HWDEBUG = isLoggable;
        isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false : true;
        HWFLOW = isLoggable;
        if (!Log.HWINFO) {
            if (Log.HWModuleLog) {
                z = Log.isLoggable(TAG, 6);
            } else {
                z = false;
            }
        }
        HWLOGWE = z;
    }

    public BrightnessSeekBarMonitor(DisplayEffectMonitor monitor) {
        this.mMonitor = monitor;
        DisplayEffectMonitor displayEffectMonitor = this.mMonitor;
        displayEffectMonitor.getClass();
        this.mTempManualBrightnessPrinter = new ParamLogPrinter(TYPE_TEMP_MANUAL_BRIGHTNESS, TAG);
        displayEffectMonitor = this.mMonitor;
        displayEffectMonitor.getClass();
        this.mTempAutoBrightnessPrinter = new ParamLogPrinter(TYPE_TEMP_AUTO_BRIGHTNESS, TAG);
        this.mSeekBarMovementUploader = new SeekBarMovementUploader();
    }

    public boolean isParamOwner(String paramType) {
        if (paramType.equals(TYPE_TEMP_MANUAL_BRIGHTNESS) || paramType.equals(TYPE_TEMP_AUTO_BRIGHTNESS) || paramType.equals(TYPE_ALGO_DEFAULT_BRIGHTNESS) || paramType.equals(TYPE_WINDOW_MANAGER_BRIGHTNESS)) {
            return true;
        }
        return false;
    }

    public void sendMonitorParam(ArrayMap<String, Object> params) {
        if (params == null || ((params.get(MonitorModule.PARAM_TYPE) instanceof String) ^ 1) != 0) {
            if (HWLOGWE) {
                Slog.e(TAG, "sendMonitorParam() input params format error!");
            }
            return;
        }
        String paramType = (String) params.get(MonitorModule.PARAM_TYPE);
        if (paramType.equals(TYPE_TEMP_MANUAL_BRIGHTNESS)) {
            tempManualBrightness(params);
        } else if (paramType.equals(TYPE_TEMP_AUTO_BRIGHTNESS)) {
            tempAutoBrightness(params);
        } else if (paramType.equals(TYPE_ALGO_DEFAULT_BRIGHTNESS)) {
            algoDefaultBrightness(params);
        } else if (paramType.equals(TYPE_WINDOW_MANAGER_BRIGHTNESS)) {
            windowManagerBrightness(params);
        } else if (HWLOGWE) {
            Slog.e(TAG, "sendMonitorParam() undefine paramType: " + paramType);
        }
    }

    public void triggerUploadTimer() {
        seekBarMovementUploader();
    }

    private void tempManualBrightness(ArrayMap<String, Object> params) {
        if (tempManualBrightnessCheckParamValid(params)) {
            int brightness = ((Integer) params.get(PARAM_BRIGHTNESS)).intValue();
            String packageName = (String) params.get("packageName");
            tempManualBrightnessPrint(brightness, packageName);
            if (packageName.equals(MONITOR_TARGET_PACKAGE)) {
                recordSeekBarMovement(false, brightness);
            }
        }
    }

    private boolean tempManualBrightnessCheckParamValid(ArrayMap<String, Object> params) {
        if (params == null) {
            return false;
        }
        if (!(params.get(PARAM_BRIGHTNESS) instanceof Integer)) {
            if (HWLOGWE) {
                Slog.e(TAG, "tempManualBrightnessCheckParamValid() can't get param: brightness");
            }
            return false;
        } else if (params.get("packageName") instanceof String) {
            return true;
        } else {
            if (HWLOGWE) {
                Slog.e(TAG, "tempManualBrightnessCheckParamValid() can't get param: packageName");
            }
            return false;
        }
    }

    private void tempManualBrightnessPrint(int brightness, String packageName) {
        if (this.mTempManualBrightnessPackageNameLast == null) {
            this.mTempManualBrightnessPackageNameLast = packageName;
        }
        if (this.mTempManualBrightnessPackageNameLast.equals(packageName)) {
            this.mTempManualBrightnessPrinter.updateParam(brightness, packageName);
            return;
        }
        this.mTempManualBrightnessPrinter.changeName(brightness, packageName);
        this.mTempManualBrightnessPackageNameLast = packageName;
    }

    private void tempAutoBrightness(ArrayMap<String, Object> params) {
        if (tempAutoBrightnessCheckParamValid(params)) {
            int brightness = ((Integer) params.get(PARAM_BRIGHTNESS)).intValue();
            String packageName = (String) params.get("packageName");
            tempAutoBrightnessPrint(brightness, packageName);
            if (brightness != -1 && packageName.equals(MONITOR_TARGET_PACKAGE)) {
                recordSeekBarMovement(true, brightness);
            }
        }
    }

    private boolean tempAutoBrightnessCheckParamValid(ArrayMap<String, Object> params) {
        if (params == null) {
            return false;
        }
        if (!(params.get(PARAM_BRIGHTNESS) instanceof Integer)) {
            if (HWLOGWE) {
                Slog.e(TAG, "tempAutoBrightnessCheckParamValid() can't get param: brightness");
            }
            return false;
        } else if (params.get("packageName") instanceof String) {
            return true;
        } else {
            if (HWLOGWE) {
                Slog.e(TAG, "tempAutoBrightnessCheckParamValid() can't get param: packageName");
            }
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

    private void algoDefaultBrightness(ArrayMap<String, Object> params) {
        if (algoDefaultBrightnessCheckParamValid(params)) {
            this.mAlgoSmoothLightValue = ((Integer) params.get(PARAM_LIGHT_VALUE)).intValue();
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
            if (HWLOGWE) {
                Slog.e(TAG, "algoDefaultBrightnessCheckParamValid() can't get param: lightValue");
            }
            return false;
        } else if (!(params.get(PARAM_BRIGHTNESS) instanceof Integer)) {
            if (HWLOGWE) {
                Slog.e(TAG, "algoDefaultBrightnessCheckParamValid() can't get param: brightness");
            }
            return false;
        } else if (params.get(PARAM_BRIGHTNESS_MODE) instanceof String) {
            return true;
        } else {
            if (HWLOGWE) {
                Slog.e(TAG, "algoDefaultBrightnessCheckParamValid() can't get param: brightnessMode");
            }
            return false;
        }
    }

    private void windowManagerBrightness(ArrayMap<String, Object> params) {
        if (windowManagerBrightnessCheckParamValid(params)) {
            int brightness = ((Integer) params.get(PARAM_BRIGHTNESS)).intValue();
            String packageName = (String) params.get("packageName");
            if (brightness == -255 && packageName.equals("android")) {
                this.mInWindowManagerBrightnessMode = false;
            } else {
                this.mInWindowManagerBrightnessMode = true;
            }
            if (HWDEBUG) {
                Slog.d(TAG, "windowManagerBrightness() brightness=" + brightness + ", packageName=" + packageName + ", mInWindowManagerBrightnessMode=" + this.mInWindowManagerBrightnessMode);
            }
        }
    }

    private boolean windowManagerBrightnessCheckParamValid(ArrayMap<String, Object> params) {
        if (params == null) {
            return false;
        }
        if (!(params.get(PARAM_BRIGHTNESS) instanceof Integer)) {
            if (HWLOGWE) {
                Slog.e(TAG, "windowManagerBrightnessCheckParamValid() can't get param: brightness");
            }
            return false;
        } else if (params.get("packageName") instanceof String) {
            return true;
        } else {
            if (HWLOGWE) {
                Slog.e(TAG, "windowManagerBrightnessCheckParamValid() can't get param: packageName");
            }
            return false;
        }
    }

    private void recordSeekBarMovement(boolean isAutoMode, int level) {
        if ((isAutoMode || (this.mIsManualModeReceiveAmbientLight ^ 1) == 0) && !this.mInWindowManagerBrightnessMode) {
            long currentTime = SystemClock.elapsedRealtime();
            SeekBarMovement movement = isAutoMode ? this.mAutoModeMovement : this.mManualModeMovement;
            if (movement == null) {
                recordSeekBarMovementStart(new SeekBarMovement(), isAutoMode, level, currentTime);
                return;
            }
            if (movement.endTime == 0) {
                if (currentTime - movement.startTime > MOVE_INTERVAL_TIME_IN_MS) {
                    recordSeekBarMovementStart(movement, isAutoMode, level, currentTime);
                } else {
                    movement.endTime = currentTime;
                    movement.endLevel = level;
                }
            } else if (currentTime - movement.endTime > MOVE_INTERVAL_TIME_IN_MS) {
                recordSeekBarMovementEnd(movement, isAutoMode);
                recordSeekBarMovementStart(new SeekBarMovement(), isAutoMode, level, currentTime);
            } else {
                movement.endTime = currentTime;
                movement.endLevel = level;
            }
        }
    }

    private void recordSeekBarMovementStart(SeekBarMovement movement, boolean isAutoMode, int level, long time) {
        movement.isAutoMode = isAutoMode;
        movement.startTime = time;
        movement.startLevel = level;
        movement.startLux = this.mAlgoSmoothLightValue;
        movement.algoDefaultLevel = isAutoMode ? this.mAlgoDefaultBrightness : 0;
        movement.foregroundPackageName = this.mMonitor.getCurrentTopAppName();
        if (isAutoMode) {
            this.mAutoModeMovement = movement;
        } else {
            this.mManualModeMovement = movement;
        }
    }

    private void recordSeekBarMovementEnd(SeekBarMovement movement, boolean isAutoMode) {
        Object movement2;
        if (HWFLOW) {
            Slog.i(TAG, "recordSeekBarMovementEnd() " + (isAutoMode ? "AUTO" : "MANUAL") + " " + movement2);
        }
        if (movement2.startLevel == movement2.endLevel) {
            movement2 = null;
        }
        if (isAutoMode) {
            if (movement2 != null) {
                this.mAutoModeMovementList.add(movement2);
            }
            this.mAutoModeMovement = null;
            return;
        }
        if (movement2 != null) {
            this.mManualModeMovementList.add(movement2);
        }
        this.mManualModeMovement = null;
    }

    private void seekBarMovementUploader() {
        checkUnfinishedMovement(true);
        checkUnfinishedMovement(false);
        checkMovementListInterval(true);
        checkMovementListInterval(false);
        uploadMovementList();
    }

    private void checkUnfinishedMovement(boolean isAutoMode) {
        SeekBarMovement movement = isAutoMode ? this.mAutoModeMovement : this.mManualModeMovement;
        if (movement != null) {
            long currentTime = SystemClock.elapsedRealtime();
            if (movement.endTime != 0 && currentTime - movement.endTime > MOVE_INTERVAL_TIME_IN_MS) {
                recordSeekBarMovementEnd(movement, isAutoMode);
            }
        }
    }

    private void checkMovementListInterval(boolean isAutoMode) {
        List<SeekBarMovement> movementList = isAutoMode ? this.mAutoModeMovementList : this.mManualModeMovementList;
        if (movementList.size() > 1) {
            if (HWDEBUG) {
                for (SeekBarMovement movement : movementList) {
                    Slog.d(TAG, "checkMovementListInterval() before " + movement);
                }
            }
            boolean isMerged = false;
            SeekBarMovement pre = null;
            SeekBarMovement now = null;
            Iterator<SeekBarMovement> it = movementList.iterator();
            while (it.hasNext()) {
                if (pre == null) {
                    pre = (SeekBarMovement) it.next();
                } else if (now != null) {
                    pre = now;
                }
                now = (SeekBarMovement) it.next();
                if (isMerge2Movement(pre, now)) {
                    if (HWFLOW) {
                        Slog.i(TAG, "checkMovementListInterval() merge " + pre + " and " + now);
                    }
                    pre.endTime = now.endTime;
                    pre.endLevel = now.endLevel;
                    it.remove();
                    now = null;
                    isMerged = true;
                }
            }
            if (isMerged && HWDEBUG) {
                for (SeekBarMovement movement2 : movementList) {
                    Slog.d(TAG, "checkMovementListInterval() after " + movement2);
                }
            }
        }
    }

    private boolean isMerge2Movement(SeekBarMovement pre, SeekBarMovement now) {
        if (now.startTime - pre.endTime > MOVE_LIST_INTERVAL_TIME_IN_MS) {
            return false;
        }
        int deltaLevelThresholdTmp = (pre.startLux * 50) / 100;
        if (Math.abs(now.startLux - pre.startLux) >= (deltaLevelThresholdTmp > 5 ? deltaLevelThresholdTmp : 5)) {
            return false;
        }
        return true;
    }

    private void uploadMovementList() {
        if (!this.mAutoModeMovementList.isEmpty() || !this.mManualModeMovementList.isEmpty()) {
            List<SeekBarMovement> allMovementList = new ArrayList();
            allMovementList.addAll(this.mAutoModeMovementList);
            allMovementList.addAll(this.mManualModeMovementList);
            Collections.sort(allMovementList, mComparator);
            int addDataTimes = 0;
            for (SeekBarMovement movement : allMovementList) {
                this.mSeekBarMovementUploader.addData(movement);
                addDataTimes++;
                if (addDataTimes >= 3) {
                    break;
                }
            }
            if (addDataTimes > 0) {
                this.mSeekBarMovementUploader.upload();
            }
            this.mAutoModeMovementList.clear();
            this.mManualModeMovementList.clear();
        }
    }
}
