package com.android.server.display;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.ArrayMap;
import android.util.Slog;
import com.android.server.display.DisplayEffectMonitor.ParamLogPrinter;

public class BackLightBrightnessMonitor {
    private static final boolean HWDEBUG = false;
    private static final boolean HWFLOW = false;
    private static final boolean HWLOGWE = true;
    private static final String PARAM_BRIGHTNESS = "brightness";
    private static final String PARAM_BRIGHTNESS_MODE = "brightnessMode";
    private static final int PARAM_MSG = 1;
    private static final String PARAM_PACKAGE_NAME = "packageName";
    private static final String PARAM_TYPE = "paramType";
    private static final String TAG = "BackLightBrightnessMonitor";
    private static final String TYPE_BRIGHTNESS_MODE = "brightnessMode";
    private static final String TYPE_MANUAL_BRIGHTNESS = "manualBrightness";
    private static final String TYPE_TEMP_AUTO_BRIGHTNESS = "tempAutoBrightness";
    private static final String TYPE_TEMP_MANUAL_BRIGHTNESS = "tempManualBrightness";
    private static final String TYPE_WINDOW_MANAGER_BRIGHTNESS = "windowManagerBrightness";
    private HandlerThread mHandlerThread;
    private boolean mInManualBrightnessMode;
    private ParamLogPrinter mManualBrightnessPrinter;
    private DisplayEffectMonitor mMonitor;
    private ParamReceiveHandler mParamReceiveHandler;
    private String mTempAutoBrightnessPackageNameLast;
    private ParamLogPrinter mTempAutoBrightnessPrinter;
    private String mTempManualBrightnessPackageNameLast;
    private ParamLogPrinter mTempManualBrightnessPrinter;
    private String mWindowManagerBrightnessMonitorPackageName;
    private String mWindowManagerBrightnessPackageNameLast;
    private ParamLogPrinter mWindowManagerBrightnessPrinter;

    private class ParamReceiveHandler extends Handler {
        public ParamReceiveHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (msg.what == BackLightBrightnessMonitor.PARAM_MSG) {
                BackLightBrightnessMonitor.this.processMonitorParam((ArrayMap) msg.obj);
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.display.BackLightBrightnessMonitor.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.display.BackLightBrightnessMonitor.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.display.BackLightBrightnessMonitor.<clinit>():void");
    }

    public BackLightBrightnessMonitor(DisplayEffectMonitor monitor) {
        this.mInManualBrightnessMode = HWLOGWE;
        if (monitor != null) {
            this.mMonitor = monitor;
            DisplayEffectMonitor displayEffectMonitor = this.mMonitor;
            displayEffectMonitor.getClass();
            this.mWindowManagerBrightnessPrinter = new ParamLogPrinter("WindowManagerBrightness", TAG);
            displayEffectMonitor = this.mMonitor;
            displayEffectMonitor.getClass();
            this.mTempManualBrightnessPrinter = new ParamLogPrinter("TempManualBrightness", TAG);
            displayEffectMonitor = this.mMonitor;
            displayEffectMonitor.getClass();
            this.mTempAutoBrightnessPrinter = new ParamLogPrinter("TempAutoBrightness", TAG);
            displayEffectMonitor = this.mMonitor;
            displayEffectMonitor.getClass();
            this.mManualBrightnessPrinter = new ParamLogPrinter("ManualBrightness", TAG);
            this.mHandlerThread = new HandlerThread(TAG);
            this.mHandlerThread.start();
            this.mParamReceiveHandler = new ParamReceiveHandler(this.mHandlerThread.getLooper());
            if (HWFLOW) {
                Slog.i(TAG, "new instance success");
            }
        }
    }

    public boolean isParamOwner(String paramType) {
        if (paramType == null) {
            return HWFLOW;
        }
        if (paramType.equals(TYPE_WINDOW_MANAGER_BRIGHTNESS) || paramType.equals(TYPE_TEMP_MANUAL_BRIGHTNESS) || paramType.equals(TYPE_TEMP_AUTO_BRIGHTNESS) || paramType.equals(TYPE_BRIGHTNESS_MODE) || paramType.equals(TYPE_MANUAL_BRIGHTNESS)) {
            return HWLOGWE;
        }
        return HWFLOW;
    }

    public void sendMonitorParam(ArrayMap<String, Object> params) {
        if (this.mParamReceiveHandler != null) {
            this.mParamReceiveHandler.sendMessage(this.mParamReceiveHandler.obtainMessage(PARAM_MSG, params));
        }
    }

    private void processMonitorParam(ArrayMap<String, Object> params) {
        if (params == null || !(params.get(PARAM_TYPE) instanceof String)) {
            Slog.e(TAG, "processMonitorParam() input params format error!");
            return;
        }
        String paramType = (String) params.get(PARAM_TYPE);
        if (HWDEBUG) {
            Slog.d(TAG, "processMonitorParam() paramType: " + paramType);
        }
        if (paramType.equals(TYPE_WINDOW_MANAGER_BRIGHTNESS)) {
            recordWindowManagerBrightness(params);
        } else if (paramType.equals(TYPE_TEMP_MANUAL_BRIGHTNESS)) {
            recordTempManualBrightness(params);
        } else if (paramType.equals(TYPE_TEMP_AUTO_BRIGHTNESS)) {
            recordTempAutoBrightness(params);
        } else if (paramType.equals(TYPE_BRIGHTNESS_MODE)) {
            recordBrightnessMode(params);
        } else if (paramType.equals(TYPE_MANUAL_BRIGHTNESS)) {
            recordManualBrightness(params);
        } else {
            Slog.e(TAG, "processMonitorParam() undefine paramType: " + paramType);
        }
    }

    private void recordWindowManagerBrightness(ArrayMap<String, Object> params) {
        if (params != null && this.mMonitor != null) {
            if (!(params.get(PARAM_BRIGHTNESS) instanceof Integer)) {
                Slog.e(TAG, "recordWindowManagerBrightness() can't get param: packageName");
            } else if (params.get(PARAM_PACKAGE_NAME) instanceof String) {
                int brightness = ((Integer) params.get(PARAM_BRIGHTNESS)).intValue();
                String packageName = (String) params.get(PARAM_PACKAGE_NAME);
                if (this.mWindowManagerBrightnessPackageNameLast == null) {
                    this.mWindowManagerBrightnessPackageNameLast = packageName;
                }
                if (this.mWindowManagerBrightnessPackageNameLast.equals(packageName)) {
                    if (HWDEBUG) {
                        Slog.d(TAG, "recordWindowManagerBrightness() update brightness=" + brightness + ", packageName=" + packageName);
                    }
                    this.mWindowManagerBrightnessPrinter.updateParam(brightness, packageName);
                } else if (brightness == -255 && packageName.equals("android")) {
                    if (HWDEBUG) {
                        Slog.d(TAG, "recordWindowManagerBrightness() brightness reset to normal");
                    }
                    stopMonitorWindowManagerBrightness();
                    this.mWindowManagerBrightnessPrinter.resetParam(brightness, packageName);
                    this.mWindowManagerBrightnessPackageNameLast = packageName;
                } else {
                    if (HWDEBUG) {
                        Slog.d(TAG, "recordWindowManagerBrightness() start brightness=" + brightness + ", packageName=" + packageName);
                    }
                    startMonitorWindowManagerBrightness(packageName);
                    if (this.mWindowManagerBrightnessPackageNameLast.equals("android")) {
                        this.mWindowManagerBrightnessPrinter.updateParam(brightness, packageName);
                    } else {
                        this.mWindowManagerBrightnessPrinter.changeName(brightness, packageName);
                    }
                    this.mWindowManagerBrightnessPackageNameLast = packageName;
                }
            } else {
                Slog.e(TAG, "recordWindowManagerBrightness() can't get param: packageName");
            }
        }
    }

    private void startMonitorWindowManagerBrightness(String packageName) {
        this.mWindowManagerBrightnessMonitorPackageName = packageName;
    }

    private void stopMonitorWindowManagerBrightness() {
        this.mWindowManagerBrightnessMonitorPackageName = null;
    }

    private void checkMonitorWindowManagerBrightness() {
        if (this.mWindowManagerBrightnessMonitorPackageName != null) {
            if (!this.mMonitor.isAppAlive(this.mWindowManagerBrightnessMonitorPackageName)) {
                Slog.e(TAG, "checkMonitorWindowManagerBrightness() error! " + this.mWindowManagerBrightnessMonitorPackageName + " is not in foreground");
                this.mWindowManagerBrightnessMonitorPackageName = null;
            } else if (HWDEBUG) {
                Slog.d(TAG, "checkMonitorWindowManagerBrightness() " + this.mWindowManagerBrightnessMonitorPackageName + " is still in foreground");
            }
        }
    }

    private void recordTempManualBrightness(ArrayMap<String, Object> params) {
        if (params != null && this.mMonitor != null) {
            if (!(params.get(PARAM_BRIGHTNESS) instanceof Integer)) {
                Slog.e(TAG, "recordTempManualBrightness() can't get param: brightness");
            } else if (params.get(PARAM_PACKAGE_NAME) instanceof String) {
                int brightness = ((Integer) params.get(PARAM_BRIGHTNESS)).intValue();
                String packageName = (String) params.get(PARAM_PACKAGE_NAME);
                if (HWDEBUG) {
                    Slog.d(TAG, "recordTempManualBrightness() brightness=" + brightness + ", packageName=" + packageName);
                }
                if (this.mTempManualBrightnessPackageNameLast == null) {
                    this.mTempManualBrightnessPackageNameLast = packageName;
                }
                if (this.mTempManualBrightnessPackageNameLast.equals(packageName)) {
                    this.mTempManualBrightnessPrinter.updateParam(brightness, packageName);
                } else {
                    this.mTempManualBrightnessPrinter.changeName(brightness, packageName);
                    this.mTempManualBrightnessPackageNameLast = packageName;
                }
                checkMonitorWindowManagerBrightness();
            } else {
                Slog.e(TAG, "recordTempManualBrightness() can't get param: packageName");
            }
        }
    }

    private void recordTempAutoBrightness(ArrayMap<String, Object> params) {
        if (params != null && this.mMonitor != null) {
            if (!(params.get(PARAM_BRIGHTNESS) instanceof Integer)) {
                Slog.e(TAG, "recordTempAutoBrightness() can't get param: brightness");
            } else if (params.get(PARAM_PACKAGE_NAME) instanceof String) {
                int brightness = ((Integer) params.get(PARAM_BRIGHTNESS)).intValue();
                String packageName = (String) params.get(PARAM_PACKAGE_NAME);
                if (HWDEBUG) {
                    Slog.d(TAG, "recordTempAutoBrightness() brightness=" + brightness + ", packageName=" + packageName);
                }
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
                checkMonitorWindowManagerBrightness();
            } else {
                Slog.e(TAG, "recordTempAutoBrightness() can't get param: packageName");
            }
        }
    }

    private void recordBrightnessMode(ArrayMap<String, Object> params) {
        if (params != null && this.mMonitor != null) {
            if (params.get(TYPE_BRIGHTNESS_MODE) instanceof Boolean) {
                boolean manualMode = ((Boolean) params.get(TYPE_BRIGHTNESS_MODE)).booleanValue();
                if (this.mInManualBrightnessMode != manualMode) {
                    if (HWFLOW) {
                        Slog.i(TAG, "BrightnessMode " + (manualMode ? "MANUAL" : "AUTO"));
                    }
                    this.mInManualBrightnessMode = manualMode;
                }
                return;
            }
            Slog.e(TAG, "recordBrightnessMode() can't get param: brightnessMode");
        }
    }

    private void recordManualBrightness(ArrayMap<String, Object> params) {
        if (params != null && this.mMonitor != null) {
            if (params.get(PARAM_BRIGHTNESS) instanceof Integer) {
                int brightness = ((Integer) params.get(PARAM_BRIGHTNESS)).intValue();
                if (HWDEBUG) {
                    Slog.d(TAG, "recordManualBrightness() brightness=" + brightness);
                }
                this.mManualBrightnessPrinter.updateParam(brightness, null);
                return;
            }
            Slog.e(TAG, "recordManualBrightness() can't get param: brightness");
        }
    }
}
