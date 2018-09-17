package com.android.server.display;

import android.util.ArrayMap;
import android.util.HwLog;
import android.util.IMonitor;
import android.util.IMonitor.EventStream;
import android.util.Log;
import android.util.Slog;
import com.android.server.am.HwActivityManagerService;
import com.android.server.display.DisplayEffectMonitor.MonitorModule;
import huawei.com.android.server.policy.stylus.StylusGestureSettings;
import java.util.ArrayList;
import java.util.List;

class AmbientLightMonitor implements MonitorModule {
    private static final boolean HWDEBUG;
    private static final boolean HWFLOW;
    private static final boolean HWLOGWE;
    private static final String PARAM_BRIGHTNESS_MODE = "brightnessMode";
    private static final String PARAM_DURATION_IN_MS = "durationInMs";
    private static final String PARAM_LIGHT_VALUE = "lightValue";
    private static final String PARAM_LIGHT_VALUE_LIST = "lightValueList";
    private static final String TAG = "AmbientLightMonitor";
    private static final String TYPE_AMBIENT_LIGHT_COLLECTION = "ambientLightCollection";
    private static final String TYPE_POWER_ON_BRIGHTNESS_CHANGED = "powerOnBrightnessChanged";
    private AmbientLightCollectionData mAmbientLightDataInAuto = new AmbientLightCollectionData(true);
    private AmbientLightCollectionData mAmbientLightDataInManual = new AmbientLightCollectionData(false);
    private int mPowerOnBrightnessChangedTimes;
    private PowerOnBrightnessChangedUploader mPowerOnBrightnessChangedUploader = new PowerOnBrightnessChangedUploader();
    private List<Integer> mPowerOnBrightnessChangedValue;

    private static class AmbientLightCollectionData {
        private static final int UPLOAD_INTERVAL_TIME_IN_MS = 60000;
        private int lux1000To5000TimeInMs;
        private int lux200To500TimeInMs;
        private int lux500To1000TimeInMs;
        private int lux50To200TimeInMs;
        private int luxAbove5000TimeInMs;
        private int luxBelow50TimeInMs;
        private final boolean mIsAutoMode;
        private int mTotalTimeInMs;

        public AmbientLightCollectionData(boolean isAutoMode) {
            this.mIsAutoMode = isAutoMode;
        }

        public void collect(int lux, int durationInMs) {
            if (lux < 0 || durationInMs <= 0 || durationInMs >= 5000) {
                if (AmbientLightMonitor.HWLOGWE) {
                    Slog.e(AmbientLightMonitor.TAG, "AmbientLightCollectionData.collect() error!input params out of range: lux=" + lux + ", durationInMs=" + durationInMs);
                }
                return;
            }
            if (lux <= 50) {
                this.luxBelow50TimeInMs += durationInMs;
            } else if (lux <= 200) {
                this.lux50To200TimeInMs += durationInMs;
            } else if (lux <= HwActivityManagerService.SERVICE_ADJ) {
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
            if (this.mIsAutoMode) {
                HwLog.dubaie("DUBAI_TAG_AUTO_AMBIENT_LIGHT", "d0=" + this.luxBelow50TimeInMs + " d1=" + this.lux50To200TimeInMs + " d2=" + this.lux200To500TimeInMs + " d3=" + this.lux500To1000TimeInMs + " d4=" + this.lux1000To5000TimeInMs + " d5=" + this.luxAbove5000TimeInMs);
            } else {
                HwLog.dubaie("DUBAI_TAG_MANUAL_AMBIENT_LIGHT", "d0=" + this.luxBelow50TimeInMs + " d1=" + this.lux50To200TimeInMs + " d2=" + this.lux200To500TimeInMs + " d3=" + this.lux500To1000TimeInMs + " d4=" + this.lux1000To5000TimeInMs + " d5=" + this.luxAbove5000TimeInMs);
            }
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
            return "AmbientLightCollectionData " + (this.mIsAutoMode ? "auto" : "manual") + ", lux <50:" + (this.luxBelow50TimeInMs / 1000) + StylusGestureSettings.STYLUS_GESTURE_S_SUFFIX + ", 50~200:" + (this.lux50To200TimeInMs / 1000) + StylusGestureSettings.STYLUS_GESTURE_S_SUFFIX + ", 200~500:" + (this.lux200To500TimeInMs / 1000) + StylusGestureSettings.STYLUS_GESTURE_S_SUFFIX + ", 500~1000:" + (this.lux500To1000TimeInMs / 1000) + StylusGestureSettings.STYLUS_GESTURE_S_SUFFIX + ", 1000~5000:" + (this.lux1000To5000TimeInMs / 1000) + StylusGestureSettings.STYLUS_GESTURE_S_SUFFIX + ", >5000:" + (this.luxAbove5000TimeInMs / 1000) + StylusGestureSettings.STYLUS_GESTURE_S_SUFFIX;
        }
    }

    private static class PowerOnBrightnessChangedUploader {
        public static final int UPLOAD_VALUES_NUM = 10;
        private PowerOnBrightnessChangedUploadData mUploadData;

        private static class PowerOnBrightnessChangedUploadData {
            public short alsVal0;
            public short alsVal1;
            public short alsVal2;
            public short alsVal3;
            public short alsVal4;
            public short alsVal5;
            public short alsVal6;
            public short alsVal7;
            public short alsVal8;
            public short alsVal9;
            public byte times;

            /* synthetic */ PowerOnBrightnessChangedUploadData(PowerOnBrightnessChangedUploadData -this0) {
                this();
            }

            private PowerOnBrightnessChangedUploadData() {
            }

            public String toString() {
                return "PowerOnBrightnessChangedUploadData times = " + this.times + ", alsVal = " + this.alsVal0 + ", " + this.alsVal1 + ", " + this.alsVal2 + ", " + this.alsVal3 + ", " + this.alsVal4 + ", " + this.alsVal5 + ", " + this.alsVal6 + ", " + this.alsVal7 + ", " + this.alsVal8 + ", " + this.alsVal9;
            }
        }

        /* synthetic */ PowerOnBrightnessChangedUploader(PowerOnBrightnessChangedUploader -this0) {
            this();
        }

        private PowerOnBrightnessChangedUploader() {
        }

        public void addData(int times, List<Integer> data) {
            int i = 32767;
            if (times > 0 && data != null && data.size() == 10) {
                int intValue;
                if (this.mUploadData == null) {
                    this.mUploadData = new PowerOnBrightnessChangedUploadData();
                }
                PowerOnBrightnessChangedUploadData powerOnBrightnessChangedUploadData = this.mUploadData;
                if (times >= 127) {
                    times = 127;
                }
                powerOnBrightnessChangedUploadData.times = (byte) times;
                PowerOnBrightnessChangedUploadData powerOnBrightnessChangedUploadData2 = this.mUploadData;
                if (((Integer) data.get(0)).intValue() < 32767) {
                    intValue = ((Integer) data.get(0)).intValue();
                } else {
                    intValue = 32767;
                }
                powerOnBrightnessChangedUploadData2.alsVal0 = (short) intValue;
                powerOnBrightnessChangedUploadData2 = this.mUploadData;
                if (((Integer) data.get(1)).intValue() < 32767) {
                    intValue = ((Integer) data.get(1)).intValue();
                } else {
                    intValue = 32767;
                }
                powerOnBrightnessChangedUploadData2.alsVal1 = (short) intValue;
                powerOnBrightnessChangedUploadData2 = this.mUploadData;
                if (((Integer) data.get(2)).intValue() < 32767) {
                    intValue = ((Integer) data.get(2)).intValue();
                } else {
                    intValue = 32767;
                }
                powerOnBrightnessChangedUploadData2.alsVal2 = (short) intValue;
                powerOnBrightnessChangedUploadData2 = this.mUploadData;
                if (((Integer) data.get(3)).intValue() < 32767) {
                    intValue = ((Integer) data.get(3)).intValue();
                } else {
                    intValue = 32767;
                }
                powerOnBrightnessChangedUploadData2.alsVal3 = (short) intValue;
                powerOnBrightnessChangedUploadData2 = this.mUploadData;
                if (((Integer) data.get(4)).intValue() < 32767) {
                    intValue = ((Integer) data.get(4)).intValue();
                } else {
                    intValue = 32767;
                }
                powerOnBrightnessChangedUploadData2.alsVal4 = (short) intValue;
                powerOnBrightnessChangedUploadData2 = this.mUploadData;
                if (((Integer) data.get(5)).intValue() < 32767) {
                    intValue = ((Integer) data.get(5)).intValue();
                } else {
                    intValue = 32767;
                }
                powerOnBrightnessChangedUploadData2.alsVal5 = (short) intValue;
                powerOnBrightnessChangedUploadData2 = this.mUploadData;
                if (((Integer) data.get(6)).intValue() < 32767) {
                    intValue = ((Integer) data.get(6)).intValue();
                } else {
                    intValue = 32767;
                }
                powerOnBrightnessChangedUploadData2.alsVal6 = (short) intValue;
                powerOnBrightnessChangedUploadData2 = this.mUploadData;
                if (((Integer) data.get(7)).intValue() < 32767) {
                    intValue = ((Integer) data.get(7)).intValue();
                } else {
                    intValue = 32767;
                }
                powerOnBrightnessChangedUploadData2.alsVal7 = (short) intValue;
                powerOnBrightnessChangedUploadData2 = this.mUploadData;
                if (((Integer) data.get(8)).intValue() < 32767) {
                    intValue = ((Integer) data.get(8)).intValue();
                } else {
                    intValue = 32767;
                }
                powerOnBrightnessChangedUploadData2.alsVal8 = (short) intValue;
                powerOnBrightnessChangedUploadData2 = this.mUploadData;
                if (((Integer) data.get(9)).intValue() < 32767) {
                    i = ((Integer) data.get(9)).intValue();
                }
                powerOnBrightnessChangedUploadData2.alsVal9 = (short) i;
            }
        }

        public void upload() {
            if (this.mUploadData != null) {
                EventStream stream = IMonitor.openEventStream(932010100);
                stream.setParam((short) 0, this.mUploadData.times);
                stream.setParam((short) 1, this.mUploadData.alsVal0);
                stream.setParam((short) 2, this.mUploadData.alsVal1);
                stream.setParam((short) 3, this.mUploadData.alsVal2);
                stream.setParam((short) 4, this.mUploadData.alsVal3);
                stream.setParam((short) 5, this.mUploadData.alsVal4);
                stream.setParam((short) 6, this.mUploadData.alsVal5);
                stream.setParam((short) 7, this.mUploadData.alsVal6);
                stream.setParam((short) 8, this.mUploadData.alsVal7);
                stream.setParam((short) 9, this.mUploadData.alsVal8);
                stream.setParam((short) 10, this.mUploadData.alsVal9);
                IMonitor.sendEvent(stream);
                IMonitor.closeEventStream(stream);
                if (AmbientLightMonitor.HWFLOW) {
                    Slog.i(AmbientLightMonitor.TAG, "PowerOnBrightnessChangedUploader.upload() " + this.mUploadData);
                }
                this.mUploadData = null;
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

    public AmbientLightMonitor(DisplayEffectMonitor monitor) {
    }

    public boolean isParamOwner(String paramType) {
        if (paramType.equals(TYPE_AMBIENT_LIGHT_COLLECTION) || paramType.equals(TYPE_POWER_ON_BRIGHTNESS_CHANGED)) {
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
        if (paramType.equals(TYPE_AMBIENT_LIGHT_COLLECTION)) {
            ambientLightCollection(params);
        } else if (paramType.equals(TYPE_POWER_ON_BRIGHTNESS_CHANGED)) {
            powerOnBrightnessChanged(params);
        } else if (HWLOGWE) {
            Slog.e(TAG, "sendMonitorParam() undefine paramType: " + paramType);
        }
    }

    public void triggerUploadTimer() {
        powerOnBrightnessChangedUploader();
    }

    private void ambientLightCollection(ArrayMap<String, Object> params) {
        if (ambientLightCollectionCheckParamValid(params)) {
            int lightValue = ((Integer) params.get(PARAM_LIGHT_VALUE)).intValue();
            int durationInMs = ((Integer) params.get(PARAM_DURATION_IN_MS)).intValue();
            String mode = (String) params.get(PARAM_BRIGHTNESS_MODE);
            if (HWDEBUG) {
                Slog.d(TAG, "ambientLightCollection() lightValue=" + lightValue + ", durationInMs=" + durationInMs + ", mode=" + mode);
            }
            if (mode.equals("AUTO")) {
                this.mAmbientLightDataInAuto.collect(lightValue, durationInMs);
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
            if (HWLOGWE) {
                Slog.e(TAG, "ambientLightCollectionCheckParamValid() can't get param: lightValue");
            }
            return false;
        } else if (!(params.get(PARAM_DURATION_IN_MS) instanceof Integer)) {
            if (HWLOGWE) {
                Slog.e(TAG, "ambientLightCollectionCheckParamValid() can't get param: durationInMs");
            }
            return false;
        } else if (params.get(PARAM_BRIGHTNESS_MODE) instanceof String) {
            String mode = (String) params.get(PARAM_BRIGHTNESS_MODE);
            if (mode.equals("AUTO") || (mode.equals("MANUAL") ^ 1) == 0) {
                return true;
            }
            if (HWLOGWE) {
                Slog.e(TAG, "ambientLightCollectionCheckParamValid() brightnessMode value error: " + mode);
            }
            return false;
        } else {
            if (HWLOGWE) {
                Slog.e(TAG, "ambientLightCollectionCheckParamValid() can't get param: brightnessMode");
            }
            return false;
        }
    }

    private void powerOnBrightnessChanged(ArrayMap<String, Object> params) {
        if (powerOnBrightnessChangedCheckParamValid(params)) {
            List<Integer> lightValueList = (List) params.get(PARAM_LIGHT_VALUE_LIST);
            this.mPowerOnBrightnessChangedTimes++;
            if (this.mPowerOnBrightnessChangedTimes == 1) {
                this.mPowerOnBrightnessChangedValue = new ArrayList(lightValueList);
            }
            if (HWFLOW) {
                Slog.i(TAG, "powerOnBrightnessChanged() mPowerOnBrightnessChangedTimes=" + this.mPowerOnBrightnessChangedTimes + ", lightValueList=" + lightValueList);
            }
        }
    }

    private boolean powerOnBrightnessChangedCheckParamValid(ArrayMap<String, Object> params) {
        if (params == null) {
            return false;
        }
        if (params.get(PARAM_LIGHT_VALUE_LIST) instanceof List) {
            List<Integer> lightValueList = (List) params.get(PARAM_LIGHT_VALUE_LIST);
            if (lightValueList == null) {
                if (HWLOGWE) {
                    Slog.e(TAG, "powerOnBrightnessChangedCheckParamValid() error lightValueList is null");
                }
                return false;
            } else if (lightValueList.size() != 10) {
                if (HWLOGWE) {
                    Slog.e(TAG, "powerOnBrightnessChangedCheckParamValid() lightValueList size error: " + lightValueList);
                }
                return false;
            } else {
                try {
                    for (Object obj : lightValueList) {
                        if (!(obj instanceof Integer)) {
                            if (HWLOGWE) {
                                Slog.e(TAG, "powerOnBrightnessChangedCheckParamValid() lightValueList type error obj = " + obj);
                            }
                            return false;
                        }
                    }
                    return true;
                } catch (ClassCastException e) {
                    if (HWLOGWE) {
                        Slog.e(TAG, "powerOnBrightnessChangedCheckParamValid() lightValueList type error ");
                    }
                    return false;
                }
            }
        }
        if (HWLOGWE) {
            Slog.e(TAG, "powerOnBrightnessChangedCheckParamValid() can't get param: lightValueList");
        }
        return false;
    }

    private void powerOnBrightnessChangedUploader() {
        if (this.mPowerOnBrightnessChangedTimes > 0 && this.mPowerOnBrightnessChangedValue != null) {
            this.mPowerOnBrightnessChangedUploader.addData(this.mPowerOnBrightnessChangedTimes, this.mPowerOnBrightnessChangedValue);
            this.mPowerOnBrightnessChangedUploader.upload();
        }
        this.mPowerOnBrightnessChangedTimes = 0;
        this.mPowerOnBrightnessChangedValue = null;
    }
}
