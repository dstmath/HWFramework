package com.android.server.display;

import android.util.ArrayMap;
import android.util.IMonitor;
import android.util.IMonitor.EventStream;
import android.util.Log;
import android.util.Slog;
import com.android.server.display.DisplayEffectMonitor.MonitorModule;
import com.android.server.display.DisplayEffectMonitor.ParamLogPrinter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class BrightnessSettingsMonitor implements MonitorModule {
    private static final boolean HWDEBUG;
    private static final boolean HWFLOW;
    private static final boolean HWLOGWE;
    private static final String PARAM_BRIGHTNESS = "brightness";
    private static final String PARAM_BRIGHTNESS_MODE = "brightnessMode";
    private static final String PARAM_PACKAGE_NAME = "packageName";
    private static final String TAG = "BrightnessSettingsMonitor";
    private static final String TYPE_BRIGHTNESS_MODE = "brightnessMode";
    private static final String TYPE_MANUAL_BRIGHTNESS = "manualBrightness";
    private static final String TYPE_WINDOW_MANAGER_BRIGHTNESS = "windowManagerBrightness";
    private String mBrightnessModeBackup;
    private String mBrightnessModeLast;
    private String mBrightnessModePackageNameLast;
    private List<String> mBrightnessModePackageNameUploadedList = new ArrayList();
    private List<String> mBrightnessModePackageNameWhiteList = new ArrayList(Arrays.asList(new String[]{"com.android.systemui", "com.android.settings"}));
    private String mManualBrightnessPackageNameLast;
    private ParamLogPrinter mManualBrightnessPrinter;
    private final DisplayEffectMonitor mMonitor;
    private String mWindowManagerBrightnessPackageNameLast;
    private ParamLogPrinter mWindowManagerBrightnessPrinter;

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

    public BrightnessSettingsMonitor(DisplayEffectMonitor monitor) {
        this.mMonitor = monitor;
        DisplayEffectMonitor displayEffectMonitor = this.mMonitor;
        displayEffectMonitor.getClass();
        this.mWindowManagerBrightnessPrinter = new ParamLogPrinter(TYPE_WINDOW_MANAGER_BRIGHTNESS, TAG);
        displayEffectMonitor = this.mMonitor;
        displayEffectMonitor.getClass();
        this.mManualBrightnessPrinter = new ParamLogPrinter(TYPE_MANUAL_BRIGHTNESS, TAG);
    }

    public boolean isParamOwner(String paramType) {
        if (paramType.equals(TYPE_WINDOW_MANAGER_BRIGHTNESS) || paramType.equals(TYPE_MANUAL_BRIGHTNESS) || paramType.equals("brightnessMode")) {
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
        if (paramType.equals(TYPE_WINDOW_MANAGER_BRIGHTNESS)) {
            windowManagerBrightness(params);
        } else if (paramType.equals(TYPE_MANUAL_BRIGHTNESS)) {
            manualBrightness(params);
        } else if (paramType.equals("brightnessMode")) {
            brightnessMode(params);
        } else if (HWLOGWE) {
            Slog.e(TAG, "sendMonitorParam() undefine paramType: " + paramType);
        }
    }

    public void triggerUploadTimer() {
    }

    private void windowManagerBrightness(ArrayMap<String, Object> params) {
        if (windowManagerBrightnessCheckParamValid(params)) {
            windowManagerBrightnessPrint(((Integer) params.get(PARAM_BRIGHTNESS)).intValue(), (String) params.get("packageName"));
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

    private void windowManagerBrightnessPrint(int brightness, String packageName) {
        if (this.mWindowManagerBrightnessPackageNameLast == null) {
            this.mWindowManagerBrightnessPackageNameLast = packageName;
        }
        if (this.mWindowManagerBrightnessPackageNameLast.equals(packageName)) {
            if (HWDEBUG) {
                Slog.d(TAG, "windowManagerBrightnessPrint() update brightness=" + brightness + ", packageName=" + packageName);
            }
            this.mWindowManagerBrightnessPrinter.updateParam(brightness, packageName);
        } else if (brightness == -255 && packageName.equals("android")) {
            if (HWDEBUG) {
                Slog.d(TAG, "windowManagerBrightnessPrint() brightness reset to normal");
            }
            this.mWindowManagerBrightnessPrinter.resetParam(brightness, packageName);
            this.mWindowManagerBrightnessPackageNameLast = packageName;
        } else {
            if (HWDEBUG) {
                Slog.d(TAG, "windowManagerBrightnessPrint() start brightness=" + brightness + ", packageName=" + packageName);
            }
            if (this.mWindowManagerBrightnessPackageNameLast.equals("android")) {
                this.mWindowManagerBrightnessPrinter.updateParam(brightness, packageName);
            } else {
                this.mWindowManagerBrightnessPrinter.changeName(brightness, packageName);
            }
            this.mWindowManagerBrightnessPackageNameLast = packageName;
        }
    }

    private void manualBrightness(ArrayMap<String, Object> params) {
        if (manualBrightnessCheckParamValid(params)) {
            manualBrightnessPrint(((Integer) params.get(PARAM_BRIGHTNESS)).intValue(), (String) params.get("packageName"));
        }
    }

    private boolean manualBrightnessCheckParamValid(ArrayMap<String, Object> params) {
        if (params == null) {
            return false;
        }
        if (!(params.get(PARAM_BRIGHTNESS) instanceof Integer)) {
            if (HWLOGWE) {
                Slog.e(TAG, "manualBrightnessCheckParamValid() can't get param: brightness");
            }
            return false;
        } else if (params.get("packageName") instanceof String) {
            return true;
        } else {
            if (HWLOGWE) {
                Slog.e(TAG, "manualBrightnessCheckParamValid() can't get param: packageName");
            }
            return false;
        }
    }

    private void manualBrightnessPrint(int brightness, String packageName) {
        if (this.mManualBrightnessPackageNameLast == null) {
            this.mManualBrightnessPackageNameLast = packageName;
        }
        if (this.mManualBrightnessPackageNameLast.equals(packageName)) {
            this.mManualBrightnessPrinter.updateParam(brightness, packageName);
            return;
        }
        this.mManualBrightnessPrinter.changeName(brightness, packageName);
        this.mManualBrightnessPackageNameLast = packageName;
    }

    private void brightnessMode(ArrayMap<String, Object> params) {
        if (brightnessModeCheckParamValid(params)) {
            String mode = (String) params.get("brightnessMode");
            String packageName = (String) params.get("packageName");
            brightnessModePrint(mode, packageName);
            brightnessModeCheckRecovered(mode, packageName);
            this.mBrightnessModeLast = mode;
            this.mBrightnessModePackageNameLast = packageName;
        }
    }

    private boolean brightnessModeCheckParamValid(ArrayMap<String, Object> params) {
        if (params == null) {
            return false;
        }
        if (!(params.get("brightnessMode") instanceof String)) {
            if (HWLOGWE) {
                Slog.e(TAG, "brightnessModeCheckParamValid() can't get param: brightnessMode");
            }
            return false;
        } else if (params.get("packageName") instanceof String) {
            return true;
        } else {
            if (HWLOGWE) {
                Slog.e(TAG, "brightnessModeCheckParamValid() can't get param: packageName");
            }
            return false;
        }
    }

    private void brightnessModePrint(String mode, String packageName) {
        if (this.mBrightnessModeLast == null || this.mBrightnessModePackageNameLast == null) {
            if (HWFLOW) {
                Slog.i(TAG, "brightnessMode init " + mode + " by " + packageName);
            }
            return;
        }
        boolean modeChanged = this.mBrightnessModeLast.equals(mode) ^ 1;
        boolean packageNameChanged = this.mBrightnessModePackageNameLast.equals(packageName) ^ 1;
        if (modeChanged && packageNameChanged) {
            if (HWFLOW) {
                Slog.i(TAG, "brightnessMode " + this.mBrightnessModeLast + " by " + this.mBrightnessModePackageNameLast + " -> " + mode + " by " + packageName);
            }
        } else if (modeChanged) {
            if (HWFLOW) {
                Slog.i(TAG, "brightnessMode " + this.mBrightnessModeLast + " -> " + mode + " by " + packageName);
            }
        } else if (packageNameChanged && HWFLOW) {
            Slog.i(TAG, "brightnessMode " + this.mBrightnessModeLast + " by " + this.mBrightnessModePackageNameLast + " -> " + packageName);
        }
    }

    /* JADX WARNING: Missing block: B:4:0x0008, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void brightnessModeCheckRecovered(String mode, String packageName) {
        if (this.mBrightnessModeLast != null && this.mBrightnessModePackageNameLast != null && !this.mBrightnessModePackageNameLast.equals(packageName)) {
            if (!(this.mBrightnessModeBackup == null || (this.mBrightnessModeBackup.equals(this.mBrightnessModeLast) ^ 1) == 0)) {
                brightnessModeUploadPackageNameIfNeed(this.mBrightnessModePackageNameLast);
            }
            this.mBrightnessModeBackup = this.mBrightnessModeLast;
        }
    }

    private void brightnessModeUploadPackageNameIfNeed(String packageName) {
        if (this.mBrightnessModePackageNameWhiteList.contains(packageName)) {
            if (HWDEBUG) {
                Slog.d(TAG, "brightnessModeUploadPackageNameIfNeed " + packageName + " is in white list");
            }
        } else if (this.mBrightnessModePackageNameUploadedList.contains(packageName)) {
            if (HWFLOW) {
                Slog.i(TAG, "brightnessModeUploadPackageNameIfNeed " + packageName + " already uploaded");
            }
        } else {
            if (!this.mMonitor.isAppForeground(packageName)) {
                this.mBrightnessModePackageNameUploadedList.add(packageName);
                brightnessModeUploadPackageName(packageName);
            } else if (HWFLOW) {
                Slog.i(TAG, "brightnessModeUploadPackageNameIfNeed " + packageName + " is still in foreground");
            }
        }
    }

    private void brightnessModeUploadPackageName(String packageName) {
        EventStream stream = IMonitor.openEventStream(932010103);
        stream.setParam((short) 0, packageName);
        IMonitor.sendEvent(stream);
        IMonitor.closeEventStream(stream);
        if (HWFLOW) {
            Slog.i(TAG, "brightnessModeUploadPackageName() " + packageName);
        }
    }
}
