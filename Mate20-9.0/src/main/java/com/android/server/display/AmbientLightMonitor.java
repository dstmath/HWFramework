package com.android.server.display;

import android.util.ArrayMap;
import android.util.HwLog;
import android.util.IMonitor;
import android.util.Log;
import android.util.Slog;
import com.android.server.display.DisplayEffectMonitor;
import huawei.com.android.server.policy.stylus.StylusGestureSettings;
import java.util.Arrays;

class AmbientLightMonitor implements DisplayEffectMonitor.MonitorModule {
    /* access modifiers changed from: private */
    public static final boolean HWDEBUG = (Log.HWLog || (Log.HWModuleLog && Log.isLoggable(TAG, 3)));
    /* access modifiers changed from: private */
    public static final boolean HWFLOW;
    private static final String PARAM_BRIGHTNESS_MODE = "brightnessMode";
    private static final String PARAM_COLOR_TEMP_VALUE = "colorTempValue";
    private static final String PARAM_DURATION_IN_MS = "durationInMs";
    private static final String PARAM_LIGHT_VALUE = "lightValue";
    private static final String PARAM_RAW_LIGHT_VALUE = "rawLightValue";
    private static final String TAG = "AmbientLightMonitor";
    private static final String TYPE_AMBIENT_COLOR_TEMP_COLLECTION = "ambientColorTempCollection";
    private static final String TYPE_AMBIENT_LIGHT_COLLECTION = "ambientLightCollection";
    private AmbientLightCollectionData mAmbientLightDataInAPP = new AmbientLightCollectionData(AmbientLightCollectionData.Mode.APP);
    private AmbientLightCollectionData mAmbientLightDataInAuto = new AmbientLightCollectionData(AmbientLightCollectionData.Mode.AUTO);
    private AmbientLightCollectionData mAmbientLightDataInFRONT = new AmbientLightCollectionData(AmbientLightCollectionData.Mode.FRONT);
    private AmbientLightCollectionData mAmbientLightDataInManual = new AmbientLightCollectionData(AmbientLightCollectionData.Mode.MANUAL);
    private ColorTempCollector mColorTempCollector;
    private final BackLightCommonData mCommonData;

    private static class AmbientLightCollectionData {
        private static final int UPLOAD_INTERVAL_TIME_IN_MS = 60000;
        private int lux1000To5000TimeInMs;
        private int lux200To500TimeInMs;
        private int lux500To1000TimeInMs;
        private int lux50To200TimeInMs;
        private int luxAbove5000TimeInMs;
        private int luxBelow50TimeInMs;
        private final Mode mMode;
        private int mTotalTimeInMs;

        public enum Mode {
            AUTO,
            MANUAL,
            APP,
            FRONT
        }

        public AmbientLightCollectionData(Mode mode) {
            this.mMode = mode;
        }

        public void collect(int lux, int durationInMs) {
            if (lux < 0 || durationInMs <= 0 || durationInMs >= 5000) {
                Slog.e(AmbientLightMonitor.TAG, "AmbientLightCollectionData.collect() error!input params out of range: lux=" + lux + ", durationInMs=" + durationInMs);
                return;
            }
            if (AmbientLightMonitor.HWDEBUG) {
                Slog.d(AmbientLightMonitor.TAG, "collect lux=" + lux + ", durationInMs=" + durationInMs + ", mode=" + this.mMode);
            }
            if (lux <= 50) {
                this.luxBelow50TimeInMs += durationInMs;
            } else if (lux <= 200) {
                this.lux50To200TimeInMs += durationInMs;
            } else if (lux <= 500) {
                this.lux200To500TimeInMs += durationInMs;
            } else if (lux <= 1000) {
                this.lux500To1000TimeInMs += durationInMs;
            } else if (lux <= 5000) {
                this.lux1000To5000TimeInMs += durationInMs;
            } else {
                this.luxAbove5000TimeInMs += durationInMs;
            }
            this.mTotalTimeInMs += durationInMs;
            if (this.mTotalTimeInMs >= 60000) {
                upload();
                clean();
            }
        }

        private void upload() {
            if (AmbientLightMonitor.HWDEBUG) {
                Slog.d(AmbientLightMonitor.TAG, "upload " + this);
            }
            HwLog.dubaie("DUBAI_TAG_" + this.mMode + "_AMBIENT_LIGHT", "d0=" + this.luxBelow50TimeInMs + " d1=" + this.lux50To200TimeInMs + " d2=" + this.lux200To500TimeInMs + " d3=" + this.lux500To1000TimeInMs + " d4=" + this.lux1000To5000TimeInMs + " d5=" + this.luxAbove5000TimeInMs);
        }

        private void clean() {
            this.mTotalTimeInMs = 0;
            this.luxBelow50TimeInMs = 0;
            this.lux50To200TimeInMs = 0;
            this.lux200To500TimeInMs = 0;
            this.lux500To1000TimeInMs = 0;
            this.lux1000To5000TimeInMs = 0;
            this.luxAbove5000TimeInMs = 0;
        }

        public String toString() {
            return "AmbientLightCollectionData " + this.mMode + ", lux <50:" + (this.luxBelow50TimeInMs / 1000) + "s, 50~200:" + (this.lux50To200TimeInMs / 1000) + "s, 200~500:" + (this.lux200To500TimeInMs / 1000) + "s, 500~1000:" + (this.lux500To1000TimeInMs / 1000) + "s, 1000~5000:" + (this.lux1000To5000TimeInMs / 1000) + "s, >5000:" + (this.luxAbove5000TimeInMs / 1000) + StylusGestureSettings.STYLUS_GESTURE_S_SUFFIX;
        }
    }

    private static class ColorTempCollector {
        private static final int COLOR_TEMP_MAX = 8000;
        private static final int COLOR_TEMP_MIN = 2500;
        private static final int COLOR_TEMP_MIN2 = 4000;
        private static final int LEVEL_NUM = 7;
        private static final int UPLOAD_UNIT_IN_MS = 60000;
        private int[] timeInMs;

        private ColorTempCollector() {
            this.timeInMs = new int[7];
        }

        public void collect(int colorTemp, int durationInMs) {
            int index;
            if (colorTemp > 0 && durationInMs > 0 && durationInMs < 5000) {
                if (colorTemp < 2500) {
                    index = 0;
                } else if (colorTemp < 4000) {
                    index = 1;
                } else if (colorTemp >= COLOR_TEMP_MAX) {
                    index = 6;
                } else {
                    index = ((colorTemp - 4000) / 1000) + 2;
                }
                int[] iArr = this.timeInMs;
                iArr[index] = iArr[index] + durationInMs;
                if (AmbientLightMonitor.HWDEBUG) {
                    Slog.d(AmbientLightMonitor.TAG, "ColorTempCollector.collect() colorTemp=" + colorTemp + ", index=" + index + ", durationInMs=" + durationInMs);
                }
            }
        }

        public void upload() {
            if (isEmpty()) {
                clean();
                return;
            }
            byte[] timeInMinute = new byte[7];
            for (int index = 0; index < 7; index++) {
                int minute = this.timeInMs[index] / 60000;
                byte b = Byte.MAX_VALUE;
                if (minute < 127) {
                    b = (byte) minute;
                }
                timeInMinute[index] = b;
            }
            if (AmbientLightMonitor.HWFLOW != 0) {
                Slog.i(AmbientLightMonitor.TAG, "ColorTempCollector.upload() " + Arrays.toString(timeInMinute));
            }
            IMonitor.EventStream stream = IMonitor.openEventStream(932040100);
            for (int index2 = 0; index2 < 7; index2++) {
                if (timeInMinute[index2] > 0) {
                    stream.setParam(String.format("L%d", new Object[]{Integer.valueOf(index2 + 1)}), timeInMinute[index2]);
                }
            }
            IMonitor.sendEvent(stream);
            IMonitor.closeEventStream(stream);
            clean();
        }

        private boolean isEmpty() {
            for (int time : this.timeInMs) {
                if (time >= 60000) {
                    return false;
                }
            }
            return true;
        }

        private void clean() {
            Arrays.fill(this.timeInMs, 0);
        }
    }

    static {
        boolean z = true;
        if (!Log.HWINFO && (!Log.HWModuleLog || !Log.isLoggable(TAG, 4))) {
            z = false;
        }
        HWFLOW = z;
    }

    public AmbientLightMonitor(DisplayEffectMonitor monitor, BackLightMonitorManager manager) {
        this.mCommonData = manager.getBackLightCommonData();
        if (!this.mCommonData.isCommercialVersion()) {
            this.mColorTempCollector = new ColorTempCollector();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x0029 A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x002a A[RETURN] */
    public boolean isParamOwner(String paramType) {
        char c;
        int hashCode = paramType.hashCode();
        if (hashCode == -1209196900) {
            if (paramType.equals(TYPE_AMBIENT_LIGHT_COLLECTION)) {
                c = 0;
                switch (c) {
                    case 0:
                    case 1:
                        break;
                }
            }
        } else if (hashCode == -313342435 && paramType.equals(TYPE_AMBIENT_COLOR_TEMP_COLLECTION)) {
            c = 1;
            switch (c) {
                case 0:
                case 1:
                    return true;
                default:
                    return false;
            }
        }
        c = 65535;
        switch (c) {
            case 0:
            case 1:
                break;
        }
    }

    public void sendMonitorParam(ArrayMap<String, Object> params) {
        if (params == null || !(params.get(DisplayEffectMonitor.MonitorModule.PARAM_TYPE) instanceof String)) {
            Slog.e(TAG, "sendMonitorParam() input params format error!");
            return;
        }
        String paramType = (String) params.get(DisplayEffectMonitor.MonitorModule.PARAM_TYPE);
        char c = 65535;
        int hashCode = paramType.hashCode();
        if (hashCode != -1209196900) {
            if (hashCode == -313342435 && paramType.equals(TYPE_AMBIENT_COLOR_TEMP_COLLECTION)) {
                c = 1;
            }
        } else if (paramType.equals(TYPE_AMBIENT_LIGHT_COLLECTION)) {
            c = 0;
        }
        switch (c) {
            case 0:
                ambientLightCollection(params);
                break;
            case 1:
                ambientColorTempCollection(params);
                break;
            default:
                Slog.e(TAG, "sendMonitorParam() undefine paramType: " + paramType);
                break;
        }
    }

    public void triggerUploadTimer() {
        if (this.mColorTempCollector != null) {
            this.mColorTempCollector.upload();
        }
    }

    private void ambientLightCollection(ArrayMap<String, Object> params) {
        if (ambientLightCollectionCheckParamValid(params)) {
            int lightValue = ((Integer) params.get(PARAM_LIGHT_VALUE)).intValue();
            int durationInMs = ((Integer) params.get(PARAM_DURATION_IN_MS)).intValue();
            String mode = (String) params.get(PARAM_BRIGHTNESS_MODE);
            if (this.mCommonData.isWindowManagerBrightnessMode()) {
                this.mAmbientLightDataInAPP.collect(lightValue, durationInMs);
            } else if (mode.equals("AUTO")) {
                this.mAmbientLightDataInAuto.collect(lightValue, durationInMs);
                if (params.get(PARAM_RAW_LIGHT_VALUE) instanceof Integer) {
                    this.mAmbientLightDataInFRONT.collect(((Integer) params.get(PARAM_RAW_LIGHT_VALUE)).intValue(), durationInMs);
                }
            } else {
                this.mAmbientLightDataInManual.collect(lightValue, durationInMs);
            }
        }
    }

    private boolean ambientLightCollectionCheckParamValid(ArrayMap<String, Object> params) {
        if (params == null) {
            return false;
        }
        if (!(params.get(PARAM_LIGHT_VALUE) instanceof Integer)) {
            Slog.e(TAG, "ambientLightCollectionCheckParamValid() can't get param: lightValue");
            return false;
        } else if (!(params.get(PARAM_DURATION_IN_MS) instanceof Integer)) {
            Slog.e(TAG, "ambientLightCollectionCheckParamValid() can't get param: durationInMs");
            return false;
        } else if (!(params.get(PARAM_BRIGHTNESS_MODE) instanceof String)) {
            Slog.e(TAG, "ambientLightCollectionCheckParamValid() can't get param: brightnessMode");
            return false;
        } else {
            String mode = (String) params.get(PARAM_BRIGHTNESS_MODE);
            if (mode.equals("AUTO") || mode.equals("MANUAL")) {
                return true;
            }
            Slog.e(TAG, "ambientLightCollectionCheckParamValid() brightnessMode value error: " + mode);
            return false;
        }
    }

    private void ambientColorTempCollection(ArrayMap<String, Object> params) {
        if (this.mCommonData.isProductEnable() && !this.mCommonData.isCommercialVersion() && ambientColorTempCollectionCheckParamValid(params)) {
            this.mColorTempCollector.collect(((Integer) params.get(PARAM_COLOR_TEMP_VALUE)).intValue(), ((Integer) params.get(PARAM_DURATION_IN_MS)).intValue());
        }
    }

    private boolean ambientColorTempCollectionCheckParamValid(ArrayMap<String, Object> params) {
        if (params == null) {
            return false;
        }
        if (!(params.get(PARAM_COLOR_TEMP_VALUE) instanceof Integer)) {
            Slog.e(TAG, "ambientColorTempCollectionCheckParamValid() can't get param: colorTempValue");
            return false;
        } else if (params.get(PARAM_DURATION_IN_MS) instanceof Integer) {
            return true;
        } else {
            Slog.e(TAG, "ambientColorTempCollectionCheckParamValid() can't get param: durationInMs");
            return false;
        }
    }
}
