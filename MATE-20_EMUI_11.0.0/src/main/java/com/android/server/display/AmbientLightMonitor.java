package com.android.server.display;

import android.util.ArrayMap;
import android.util.HwLog;
import android.util.IMonitor;
import android.util.Log;
import android.util.Slog;
import com.android.server.display.DisplayEffectMonitor;
import com.android.server.gesture.DefaultGestureNavConst;
import com.android.server.hidata.appqoe.HwAPPQoEUtils;
import com.android.server.wm.HwSplitBarConstants;
import huawei.com.android.server.policy.stylus.StylusGestureSettings;
import java.util.Arrays;
import java.util.Locale;

/* access modifiers changed from: package-private */
public class AmbientLightMonitor implements DisplayEffectMonitor.MonitorModule {
    private static final boolean HWDEBUG = (Log.HWLog || (Log.HWModuleLog && Log.isLoggable(TAG, 3)));
    private static final boolean HWFLOW;
    private static final String PARAM_BRIGHTNESS_MODE = "brightnessMode";
    private static final String PARAM_COLOR_TEMP_VALUE = "colorTempValue";
    private static final String PARAM_DURATION_IN_MS = "durationInMs";
    private static final String PARAM_LIGHT_VALUE = "lightValue";
    private static final String PARAM_RAW_LIGHT_VALUE = "rawLightValue";
    private static final String TAG = "AmbientLightMonitor";
    private static final String TYPE_AMBIENT_COLOR_TEMP_COLLECTION = "ambientColorTempCollection";
    private static final String TYPE_AMBIENT_LIGHT_COLLECTION = "ambientLightCollection";
    private final AmbientLightCollectionData mAmbientLightDataInApp = new AmbientLightCollectionData(AmbientLightCollectionData.Mode.APP);
    private final AmbientLightCollectionData mAmbientLightDataInAuto = new AmbientLightCollectionData(AmbientLightCollectionData.Mode.AUTO);
    private final AmbientLightCollectionData mAmbientLightDataInFront = new AmbientLightCollectionData(AmbientLightCollectionData.Mode.FRONT);
    private final AmbientLightCollectionData mAmbientLightDataInManual = new AmbientLightCollectionData(AmbientLightCollectionData.Mode.MANUAL);
    private final ColorTempCollector mColorTempCollector;
    private final BackLightCommonData mCommonData;

    static {
        boolean z = false;
        if (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4))) {
            z = true;
        }
        HWFLOW = z;
    }

    AmbientLightMonitor(BackLightMonitorManager manager) {
        this.mCommonData = manager.getBackLightCommonData();
        if (this.mCommonData.isCommercialVersion()) {
            this.mColorTempCollector = null;
        } else {
            this.mColorTempCollector = new ColorTempCollector();
        }
    }

    @Override // com.android.server.display.DisplayEffectMonitor.MonitorModule
    public boolean isParamOwner(String paramType) {
        if (paramType == null) {
            return false;
        }
        char c = 65535;
        int hashCode = paramType.hashCode();
        if (hashCode != -1209196900) {
            if (hashCode == -313342435 && paramType.equals(TYPE_AMBIENT_COLOR_TEMP_COLLECTION)) {
                c = 1;
            }
        } else if (paramType.equals(TYPE_AMBIENT_LIGHT_COLLECTION)) {
            c = 0;
        }
        if (c == 0 || c == 1) {
            return true;
        }
        return false;
    }

    @Override // com.android.server.display.DisplayEffectMonitor.MonitorModule
    public void sendMonitorParam(String paramType, ArrayMap<String, Object> params) {
        if (paramType == null) {
            Slog.e(TAG, "paramType is null");
            return;
        }
        char c = 65535;
        int hashCode = paramType.hashCode();
        if (hashCode != -1209196900) {
            if (hashCode == -313342435 && paramType.equals(TYPE_AMBIENT_COLOR_TEMP_COLLECTION)) {
                c = 1;
            }
        } else if (paramType.equals(TYPE_AMBIENT_LIGHT_COLLECTION)) {
            c = 0;
        }
        if (c == 0) {
            receiveParamForAmbientLightCollection(params);
        } else if (c != 1) {
            Slog.e(TAG, "sendMonitorParam() undefine paramType: " + paramType);
        } else {
            receiveParamForAmbientColorTempCollection(params);
        }
    }

    private void receiveParamForAmbientLightCollection(ArrayMap<String, Object> params) {
        Object lightValueObj = params.get(PARAM_LIGHT_VALUE);
        if (!(lightValueObj instanceof Integer)) {
            Slog.e(TAG, "receiveParamForAmbientLightCollection() can't get param: lightValue");
            return;
        }
        int lightValue = ((Integer) lightValueObj).intValue();
        Object durationInMsObj = params.get(PARAM_DURATION_IN_MS);
        if (!(durationInMsObj instanceof Integer)) {
            Slog.e(TAG, "receiveParamForAmbientLightCollection() can't get param: durationInMs");
            return;
        }
        int durationInMs = ((Integer) durationInMsObj).intValue();
        if (this.mCommonData.isWindowManagerBrightnessMode()) {
            this.mAmbientLightDataInApp.collect(lightValue, durationInMs);
            return;
        }
        Object modeObj = params.get(PARAM_BRIGHTNESS_MODE);
        if (!(modeObj instanceof String)) {
            Slog.e(TAG, "receiveParamForAmbientLightCollection() can't get param: brightnessMode");
            return;
        }
        String mode = (String) modeObj;
        if ("AUTO".equals(mode)) {
            this.mAmbientLightDataInAuto.collect(lightValue, durationInMs);
            Object rawLightValueObj = params.get(PARAM_RAW_LIGHT_VALUE);
            if (rawLightValueObj instanceof Integer) {
                this.mAmbientLightDataInFront.collect(((Integer) rawLightValueObj).intValue(), durationInMs);
            }
        } else if ("MANUAL".equals(mode)) {
            this.mAmbientLightDataInManual.collect(lightValue, durationInMs);
        } else {
            Slog.e(TAG, "receiveParamForAmbientLightCollection() undefine mode: " + mode);
        }
    }

    private void receiveParamForAmbientColorTempCollection(ArrayMap<String, Object> params) {
        if (this.mCommonData.isProductEnable() && !this.mCommonData.isCommercialVersion()) {
            Object colorTempValueObj = params.get(PARAM_COLOR_TEMP_VALUE);
            if (!(colorTempValueObj instanceof Integer)) {
                Slog.e(TAG, "receiveParamForAmbientColorTempCollection() can't get param: colorTempValue");
                return;
            }
            int colorTempValue = ((Integer) colorTempValueObj).intValue();
            Object durationInMsObj = params.get(PARAM_DURATION_IN_MS);
            if (!(durationInMsObj instanceof Integer)) {
                Slog.e(TAG, "receiveParamForAmbientColorTempCollection() can't get param: durationInMs");
                return;
            }
            this.mColorTempCollector.collect(colorTempValue, ((Integer) durationInMsObj).intValue());
        }
    }

    @Override // com.android.server.display.DisplayEffectMonitor.MonitorModule
    public void triggerUploadTimer() {
        ColorTempCollector colorTempCollector = this.mColorTempCollector;
        if (colorTempCollector != null) {
            colorTempCollector.upload();
        }
    }

    /* access modifiers changed from: private */
    public static class AmbientLightCollectionData {
        private static final int MS_PER_S = 1000;
        private static final int UPLOAD_INTERVAL_TIME_IN_MS = 60000;
        private int mLux1000To5000TimeInMs;
        private int mLux200To500TimeInMs;
        private int mLux500To1000TimeInMs;
        private int mLux50To200TimeInMs;
        private int mLuxAbove5000TimeInMs;
        private int mLuxBelow50TimeInMs;
        private final Mode mMode;
        private int mTotalTimeInMs;

        /* access modifiers changed from: private */
        public enum Mode {
            AUTO,
            MANUAL,
            APP,
            FRONT
        }

        AmbientLightCollectionData(Mode mode) {
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
                this.mLuxBelow50TimeInMs += durationInMs;
            } else if (lux <= 200) {
                this.mLux50To200TimeInMs += durationInMs;
            } else if (lux <= 500) {
                this.mLux200To500TimeInMs += durationInMs;
            } else if (lux <= 1000) {
                this.mLux500To1000TimeInMs += durationInMs;
            } else if (lux <= 5000) {
                this.mLux1000To5000TimeInMs += durationInMs;
            } else {
                this.mLuxAbove5000TimeInMs += durationInMs;
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
            HwLog.dubaie("DUBAI_TAG_" + this.mMode + "_AMBIENT_LIGHT", "d0=" + this.mLuxBelow50TimeInMs + " d1=" + this.mLux50To200TimeInMs + " d2=" + this.mLux200To500TimeInMs + " d3=" + this.mLux500To1000TimeInMs + " d4=" + this.mLux1000To5000TimeInMs + " d5=" + this.mLuxAbove5000TimeInMs);
        }

        private void clean() {
            this.mTotalTimeInMs = 0;
            this.mLuxBelow50TimeInMs = 0;
            this.mLux50To200TimeInMs = 0;
            this.mLux200To500TimeInMs = 0;
            this.mLux500To1000TimeInMs = 0;
            this.mLux1000To5000TimeInMs = 0;
            this.mLuxAbove5000TimeInMs = 0;
        }

        public String toString() {
            StringBuilder result = new StringBuilder(200);
            result.append("AmbientLightCollectionData ");
            result.append(this.mMode);
            result.append(", lux <50:");
            result.append(this.mLuxBelow50TimeInMs / 1000);
            result.append(StylusGestureSettings.STYLUS_GESTURE_S_SUFFIX);
            result.append(", lux 50~200:");
            result.append(this.mLux50To200TimeInMs / 1000);
            result.append(StylusGestureSettings.STYLUS_GESTURE_S_SUFFIX);
            result.append(", lux 200~500:");
            result.append(this.mLux200To500TimeInMs / 1000);
            result.append(StylusGestureSettings.STYLUS_GESTURE_S_SUFFIX);
            result.append(", lux 500~1000:");
            result.append(this.mLux500To1000TimeInMs / 1000);
            result.append(StylusGestureSettings.STYLUS_GESTURE_S_SUFFIX);
            result.append(", lux >5000:");
            result.append(this.mLuxAbove5000TimeInMs / 1000);
            result.append(StylusGestureSettings.STYLUS_GESTURE_S_SUFFIX);
            return result.toString();
        }
    }

    /* access modifiers changed from: private */
    public static class ColorTempCollector {
        private static final int[] COLOR_TEMP_LEVELS = {DefaultGestureNavConst.CHECK_AFT_TIMEOUT, HwAPPQoEUtils.APP_TYPE_STREAMING, HwSplitBarConstants.DARK_MODE_DELAY, 6000, 7000, 8000, Integer.MAX_VALUE};
        private static final int IMONITOR_EVENT_ID = 932040100;
        private static final int SAMPLING_INTERVAL_MAX_MS = 10000;
        private static final int UPLOAD_UNIT_IN_MS = 60000;
        private int[] mTimeInMs;

        private ColorTempCollector() {
            this.mTimeInMs = new int[COLOR_TEMP_LEVELS.length];
        }

        public void collect(int colorTemp, int durationInMs) {
            if (colorTemp > 0 && durationInMs > 0 && durationInMs < 10000) {
                int index = 0;
                while (true) {
                    int[] iArr = COLOR_TEMP_LEVELS;
                    if (index >= iArr.length) {
                        return;
                    }
                    if (colorTemp <= iArr[index]) {
                        int[] iArr2 = this.mTimeInMs;
                        iArr2[index] = iArr2[index] + durationInMs;
                        if (AmbientLightMonitor.HWDEBUG) {
                            Slog.d(AmbientLightMonitor.TAG, "ColorTempCollector.collect() colorTemp=" + colorTemp + ", index=" + index + ", durationInMs=" + durationInMs);
                            return;
                        }
                        return;
                    }
                    index++;
                }
            }
        }

        public void upload() {
            if (isEmpty()) {
                clean();
                return;
            }
            byte[] timeInMinute = new byte[this.mTimeInMs.length];
            int index = 0;
            while (true) {
                int[] iArr = this.mTimeInMs;
                if (index >= iArr.length) {
                    break;
                }
                int minute = iArr[index] / 60000;
                timeInMinute[index] = (byte) (minute < 127 ? minute : 32767);
                index++;
            }
            if (AmbientLightMonitor.HWFLOW) {
                Slog.i(AmbientLightMonitor.TAG, "ColorTempCollector.upload() " + Arrays.toString(timeInMinute));
            }
            IMonitor.EventStream stream = IMonitor.openEventStream((int) IMONITOR_EVENT_ID);
            for (int index2 = 0; index2 < this.mTimeInMs.length; index2++) {
                if (timeInMinute[index2] > 0) {
                    stream.setParam(String.format(Locale.ROOT, "L%d", Integer.valueOf(index2 + 1)), timeInMinute[index2]);
                }
            }
            IMonitor.sendEvent(stream);
            IMonitor.closeEventStream(stream);
            clean();
        }

        private boolean isEmpty() {
            for (int time : this.mTimeInMs) {
                if (time >= 60000) {
                    return false;
                }
            }
            return true;
        }

        private void clean() {
            Arrays.fill(this.mTimeInMs, 0);
        }
    }
}
