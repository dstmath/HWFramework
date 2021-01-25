package com.android.server.display;

import android.util.ArrayMap;
import android.util.IMonitor;
import android.util.Log;
import android.util.Slog;
import com.android.server.display.BackLightCommonData;
import com.android.server.display.DisplayEffectMonitor;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/* access modifiers changed from: package-private */
public class BrightnessSettingsMonitor implements DisplayEffectMonitor.MonitorModule {
    private static final boolean HWFLOW = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final int IMONITOR_PERSONALIZED_CURVE_EVENT_ID = 932010700;
    private static final String PARAM_BRIGHTNESS = "brightness";
    private static final String PARAM_BRIGHTNESS_MODE = "brightnessMode";
    private static final String PARAM_PERSONALIZED_CURVE = "personalizedCurve";
    private static final String PARAM_PERSONALIZED_PARAM = "personalizedParam";
    private static final int PERSONALIZED_CURVE_MAX_SIZE = 38;
    private static final int PERSONALIZED_PARAM_SIZE = 9;
    private static final String TAG = "BrightnessSettingsMonitor";
    private static final String TYPE_BRIGHTNESS_MODE = "brightnessMode";
    private static final String TYPE_MANUAL_BRIGHTNESS = "manualBrightness";
    private static final String TYPE_PERSONALIZED_CURVE_AND_PARAM = "personalizedCurveAndParam";
    private BackLightCommonData.BrightnessMode mBrightnessModeLast;
    private final BackLightCommonData mCommonData;
    private final DisplayEffectMonitor.ParamLogPrinter mManualBrightnessPrinter;
    private final DisplayEffectMonitor mMonitor;

    BrightnessSettingsMonitor(DisplayEffectMonitor monitor, BackLightMonitorManager manager) {
        this.mMonitor = monitor;
        this.mCommonData = manager.getBackLightCommonData();
        if (this.mCommonData.isCommercialVersion()) {
            this.mManualBrightnessPrinter = null;
            return;
        }
        DisplayEffectMonitor displayEffectMonitor = this.mMonitor;
        Objects.requireNonNull(displayEffectMonitor);
        this.mManualBrightnessPrinter = new DisplayEffectMonitor.ParamLogPrinter(TYPE_MANUAL_BRIGHTNESS, TAG);
    }

    @Override // com.android.server.display.DisplayEffectMonitor.MonitorModule
    public boolean isParamOwner(String paramType) {
        if (paramType == null) {
            return false;
        }
        char c = 65535;
        int hashCode = paramType.hashCode();
        if (hashCode != -138212075) {
            if (hashCode != 1984317844) {
                if (hashCode == 2125230263 && paramType.equals(TYPE_MANUAL_BRIGHTNESS)) {
                    c = 0;
                }
            } else if (paramType.equals("brightnessMode")) {
                c = 1;
            }
        } else if (paramType.equals(TYPE_PERSONALIZED_CURVE_AND_PARAM)) {
            c = 2;
        }
        if (c == 0 || c == 1 || c == 2) {
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
        if (hashCode != -138212075) {
            if (hashCode != 1984317844) {
                if (hashCode == 2125230263 && paramType.equals(TYPE_MANUAL_BRIGHTNESS)) {
                    c = 0;
                }
            } else if (paramType.equals("brightnessMode")) {
                c = 1;
            }
        } else if (paramType.equals(TYPE_PERSONALIZED_CURVE_AND_PARAM)) {
            c = 2;
        }
        if (c == 0) {
            receiveParamForManualBrightness(params);
        } else if (c == 1) {
            receiveParamForBrightnessMode(params);
        } else if (c != 2) {
            Slog.e(TAG, "sendMonitorParam() undefine paramType: " + paramType);
        } else {
            receiveParamForPersonalizedCurve(params);
        }
    }

    @Override // com.android.server.display.DisplayEffectMonitor.MonitorModule
    public void triggerUploadTimer() {
    }

    private void receiveParamForManualBrightness(ArrayMap<String, Object> params) {
        if (this.mCommonData.isProductEnable() && !this.mCommonData.isCommercialVersion()) {
            Object brightnessObj = params.get(PARAM_BRIGHTNESS);
            if (!(brightnessObj instanceof Integer)) {
                Slog.e(TAG, "receiveParamForManualBrightness() can't get param: brightness");
                return;
            }
            this.mManualBrightnessPrinter.updateParam(((Integer) brightnessObj).intValue(), "unknow");
        }
    }

    private void receiveParamForBrightnessMode(ArrayMap<String, Object> params) {
        if (!this.mCommonData.isCommercialVersion()) {
            Object modeNameObj = params.get("brightnessMode");
            if (!(modeNameObj instanceof String)) {
                Slog.e(TAG, "receiveParamForBrightnessMode() can't get param: brightnessMode");
                return;
            }
            String modeName = (String) modeNameObj;
            try {
                BackLightCommonData.BrightnessMode newMode = BackLightCommonData.BrightnessMode.valueOf(modeName);
                printBrightnessMode(newMode);
                this.mBrightnessModeLast = newMode;
                this.mCommonData.setBrightnessMode(newMode);
            } catch (IllegalArgumentException e) {
                Slog.e(TAG, "receiveParamForBrightnessMode() input error! brightnessMode=" + modeName);
            }
        }
    }

    private void printBrightnessMode(BackLightCommonData.BrightnessMode mode) {
        BackLightCommonData.BrightnessMode brightnessMode = this.mBrightnessModeLast;
        if (brightnessMode == null) {
            if (HWFLOW) {
                Slog.i(TAG, "brightnessMode init " + mode);
            }
        } else if (brightnessMode != mode && HWFLOW) {
            Slog.i(TAG, "brightnessMode " + this.mBrightnessModeLast + " -> " + mode);
        }
    }

    private void receiveParamForPersonalizedCurve(ArrayMap<String, Object> params) {
        if (this.mCommonData.isProductEnable() && !this.mCommonData.isCommercialVersion()) {
            List<Short> curveList = BackLightMonitorManager.getParamList(params, Short.class, PARAM_PERSONALIZED_CURVE, 38);
            if (!curveList.isEmpty()) {
                List<Float> paramList = BackLightMonitorManager.getParamList(params, Float.class, PARAM_PERSONALIZED_PARAM, 9);
                if (!paramList.isEmpty()) {
                    uploadPersonalizedCurve(curveList, paramList);
                }
            }
        }
    }

    private void uploadPersonalizedCurve(List<Short> curveList, List<Float> paramList) {
        IMonitor.EventStream stream = IMonitor.openEventStream((int) IMONITOR_PERSONALIZED_CURVE_EVENT_ID);
        int paramIndex = 1;
        for (Short level : curveList) {
            stream.setParam(String.format(Locale.ROOT, "B%02d", Integer.valueOf(paramIndex)), level.shortValue());
            paramIndex++;
        }
        try {
            stream.setParam("P01EnvAdapt", paramList.get(0).floatValue());
            stream.setParam("P02ContAdapt", paramList.get(1).floatValue());
            stream.setParam("P03VisualAcuity", paramList.get(2).floatValue());
            stream.setParam("P04RecogTime", paramList.get(3).floatValue());
            stream.setParam("P05Tolerance", paramList.get(4).floatValue());
            stream.setParam("P06Reflect", paramList.get(7).floatValue());
            stream.setParam("P07Power", paramList.get(8).floatValue());
            IMonitor.sendEvent(stream);
            IMonitor.closeEventStream(stream);
            if (HWFLOW) {
                Slog.i(TAG, "uploadPersonalizedCurve done");
            }
        } catch (IndexOutOfBoundsException e) {
            Slog.e(TAG, "uploadPersonalizedCurve() IndexOutOfBoundsException paramList.size()=" + paramList.size());
            IMonitor.closeEventStream(stream);
        }
    }
}
