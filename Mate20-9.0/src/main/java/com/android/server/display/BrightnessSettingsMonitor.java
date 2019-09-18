package com.android.server.display;

import android.util.ArrayMap;
import android.util.IMonitor;
import android.util.Log;
import android.util.Slog;
import com.android.server.display.BackLightCommonData;
import com.android.server.display.DisplayEffectMonitor;
import java.util.List;
import java.util.Objects;

class BrightnessSettingsMonitor implements DisplayEffectMonitor.MonitorModule {
    private static final int CURVE_MAX_SIZE = 38;
    private static final boolean HWDEBUG = (Log.HWLog || (Log.HWModuleLog && Log.isLoggable(TAG, 3)));
    private static final boolean HWFLOW;
    private static final String PARAM_BRIGHTNESS = "brightness";
    private static final String PARAM_BRIGHTNESS_MODE = "brightnessMode";
    private static final String PARAM_PERSONALIZED_CURVE = "personalizedCurve";
    private static final String PARAM_PERSONALIZED_PARAM = "personalizedParam";
    private static final int PARAM_SIZE = 9;
    private static final String TAG = "BrightnessSettingsMonitor";
    private static final String TYPE_BRIGHTNESS_MODE = "brightnessMode";
    private static final String TYPE_MANUAL_BRIGHTNESS = "manualBrightness";
    private static final String TYPE_PERSONALIZED_CURVE_AND_PARAM = "personalizedCurveAndParam";
    private BackLightCommonData.BrightnessMode mBrightnessModeLast;
    private final BackLightCommonData mCommonData;
    private DisplayEffectMonitor.ParamLogPrinter mManualBrightnessPrinter;
    private final DisplayEffectMonitor mMonitor;

    static {
        boolean z = true;
        if (!Log.HWINFO && (!Log.HWModuleLog || !Log.isLoggable(TAG, 4))) {
            z = false;
        }
        HWFLOW = z;
    }

    public BrightnessSettingsMonitor(DisplayEffectMonitor monitor, BackLightMonitorManager manager) {
        this.mMonitor = monitor;
        this.mCommonData = manager.getBackLightCommonData();
        if (!this.mCommonData.isCommercialVersion()) {
            DisplayEffectMonitor displayEffectMonitor = this.mMonitor;
            Objects.requireNonNull(displayEffectMonitor);
            this.mManualBrightnessPrinter = new DisplayEffectMonitor.ParamLogPrinter(TYPE_MANUAL_BRIGHTNESS, TAG);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x0039 A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x003a A[RETURN] */
    public boolean isParamOwner(String paramType) {
        char c;
        int hashCode = paramType.hashCode();
        if (hashCode == -138212075) {
            if (paramType.equals(TYPE_PERSONALIZED_CURVE_AND_PARAM)) {
                c = 2;
                switch (c) {
                    case 0:
                    case 1:
                    case 2:
                        break;
                }
            }
        } else if (hashCode == 1984317844) {
            if (paramType.equals("brightnessMode")) {
                c = 1;
                switch (c) {
                    case 0:
                    case 1:
                    case 2:
                        break;
                }
            }
        } else if (hashCode == 2125230263 && paramType.equals(TYPE_MANUAL_BRIGHTNESS)) {
            c = 0;
            switch (c) {
                case 0:
                case 1:
                case 2:
                    return true;
                default:
                    return false;
            }
        }
        c = 65535;
        switch (c) {
            case 0:
            case 1:
            case 2:
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
        switch (c) {
            case 0:
                manualBrightness(params);
                break;
            case 1:
                brightnessMode(params);
                break;
            case 2:
                personalizedCurveAndParam(params);
                break;
            default:
                Slog.e(TAG, "sendMonitorParam() undefine paramType: " + paramType);
                break;
        }
    }

    public void triggerUploadTimer() {
    }

    private void manualBrightness(ArrayMap<String, Object> params) {
        if (this.mCommonData.isProductEnable() && !this.mCommonData.isCommercialVersion() && manualBrightnessCheckParamValid(params)) {
            this.mManualBrightnessPrinter.updateParam(((Integer) params.get(PARAM_BRIGHTNESS)).intValue(), "unknow");
        }
    }

    private boolean manualBrightnessCheckParamValid(ArrayMap<String, Object> params) {
        if (params == null) {
            return false;
        }
        if (params.get(PARAM_BRIGHTNESS) instanceof Integer) {
            return true;
        }
        Slog.e(TAG, "manualBrightnessCheckParamValid() can't get param: brightness");
        return false;
    }

    private void brightnessMode(ArrayMap<String, Object> params) {
        if (!this.mCommonData.isCommercialVersion() && brightnessModeCheckParamValid(params)) {
            String modeName = (String) params.get("brightnessMode");
            try {
                BackLightCommonData.BrightnessMode newMode = BackLightCommonData.BrightnessMode.valueOf(modeName);
                brightnessModePrint(newMode);
                this.mBrightnessModeLast = newMode;
                this.mCommonData.setBrightnessMode(newMode);
            } catch (IllegalArgumentException e) {
                Slog.e(TAG, "brightnessMode() input error! brightnessMode=" + modeName);
            }
        }
    }

    private boolean brightnessModeCheckParamValid(ArrayMap<String, Object> params) {
        if (params == null) {
            return false;
        }
        if (params.get("brightnessMode") instanceof String) {
            return true;
        }
        Slog.e(TAG, "brightnessModeCheckParamValid() can't get param: brightnessMode");
        return false;
    }

    private void brightnessModePrint(BackLightCommonData.BrightnessMode mode) {
        if (this.mBrightnessModeLast == null) {
            if (HWFLOW) {
                Slog.i(TAG, "brightnessMode init " + mode);
            }
            return;
        }
        if (this.mBrightnessModeLast != mode && HWFLOW) {
            Slog.i(TAG, "brightnessMode " + this.mBrightnessModeLast + " -> " + mode);
        }
    }

    private void personalizedCurveAndParam(ArrayMap<String, Object> params) {
        if (this.mCommonData.isProductEnable() && !this.mCommonData.isCommercialVersion() && personalizedCurveAndParamCheckParamValid(params)) {
            personalizedCurveAndParamUpload((List) params.get(PARAM_PERSONALIZED_CURVE), (List) params.get(PARAM_PERSONALIZED_PARAM));
        }
    }

    private boolean personalizedCurveAndParamCheckParamValid(ArrayMap<String, Object> params) {
        if (params == null) {
            return false;
        }
        if (!(params.get(PARAM_PERSONALIZED_CURVE) instanceof List)) {
            Slog.e(TAG, "personalizedCurveAndParamCheckParamValid() can't get param: personalizedCurve");
            return false;
        }
        List<Short> curveList = (List) params.get(PARAM_PERSONALIZED_CURVE);
        int curveListSize = curveList.size();
        if (curveListSize == 0 || curveListSize > 38) {
            Slog.e(TAG, "personalizedCurveAndParamCheckParamValid() curveList size error: " + curveListSize);
            return false;
        }
        try {
            for (Object obj : curveList) {
                if (!(obj instanceof Short)) {
                    Slog.e(TAG, "personalizedCurveAndParamCheckParamValid() curveList type error obj = " + obj);
                    return false;
                }
            }
            if (!(params.get(PARAM_PERSONALIZED_PARAM) instanceof List)) {
                Slog.e(TAG, "personalizedCurveAndParamCheckParamValid() can't get param: personalizedParam");
                return false;
            }
            List<Float> paramList = (List) params.get(PARAM_PERSONALIZED_PARAM);
            int paramListSize = paramList.size();
            if (paramListSize != 9) {
                Slog.e(TAG, "personalizedCurveAndParamCheckParamValid() paramList size error: " + paramListSize);
                return false;
            }
            try {
                for (Object obj2 : paramList) {
                    if (!(obj2 instanceof Float)) {
                        Slog.e(TAG, "personalizedCurveAndParamCheckParamValid() paramList type error obj = " + obj2);
                        return false;
                    }
                }
                return true;
            } catch (ClassCastException e) {
                Slog.e(TAG, "personalizedCurveAndParamCheckParamValid() paramList type error");
                return false;
            }
        } catch (ClassCastException e2) {
            Slog.e(TAG, "personalizedCurveAndParamCheckParamValid() curveList type error");
            return false;
        }
    }

    private void personalizedCurveAndParamUpload(List<Short> curveList, List<Float> paramList) {
        IMonitor.EventStream stream = IMonitor.openEventStream(932010700);
        int i = 0;
        for (Short level : curveList) {
            i++;
            stream.setParam(String.format("B%02d", new Object[]{Integer.valueOf(i)}), level.shortValue());
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
                Slog.i(TAG, "personalizedCurveAndParamUpload done");
            }
        } catch (IndexOutOfBoundsException e) {
            Slog.e(TAG, "personalizedCurveAndParamUpload() IndexOutOfBoundsException paramList.size()=" + paramList.size());
            IMonitor.closeEventStream(stream);
        }
    }
}
