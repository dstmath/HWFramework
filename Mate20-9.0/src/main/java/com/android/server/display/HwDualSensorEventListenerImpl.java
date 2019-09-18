package com.android.server.display;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.util.Slog;
import android.util.Xml;
import com.android.server.gesture.GestureNavConst;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.CopyOnWriteArrayList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class HwDualSensorEventListenerImpl {
    private static float AGAIN = 16.0f;
    private static float AGAIN_RGBCW = 128.0f;
    private static float ATIME = 100.008f;
    private static float ATIME_RGBCW = 100.0f;
    /* access modifiers changed from: private */
    public static float BACK_FLOOR_THRESH = 5.0f;
    /* access modifiers changed from: private */
    public static long BACK_LUX_DEVIATION_THRESH = MemoryConstant.MIN_INTERVAL_OP_TIMEOUT;
    private static final int BACK_MSG_TIMER = 2;
    /* access modifiers changed from: private */
    public static float BACK_ROOF_THRESH = 400000.0f;
    /* access modifiers changed from: private */
    public static int[] BACK_SENSOR_STABILITY_THRESHOLD = {-30, -4, 4, 30};
    private static float COLOR_MAT_HIGH_11 = -0.85740393f;
    private static float COLOR_MAT_HIGH_12 = 3.8810537f;
    private static float COLOR_MAT_HIGH_13 = -1.2211744f;
    private static float COLOR_MAT_HIGH_14 = 0.047183935f;
    private static float COLOR_MAT_HIGH_21 = -2.835158f;
    private static float COLOR_MAT_HIGH_22 = 6.03384f;
    private static float COLOR_MAT_HIGH_23 = -0.9592755f;
    private static float COLOR_MAT_HIGH_24 = 0.06024589f;
    private static float COLOR_MAT_HIGH_31 = -1.5958017f;
    private static float COLOR_MAT_HIGH_32 = 2.4951355f;
    private static float COLOR_MAT_HIGH_33 = 5.1863265f;
    private static float COLOR_MAT_HIGH_34 = -0.26762903f;
    private static float COLOR_MAT_LOW_11 = -0.85740393f;
    private static float COLOR_MAT_LOW_12 = 3.8810537f;
    private static float COLOR_MAT_LOW_13 = -1.2211744f;
    private static float COLOR_MAT_LOW_14 = 0.6837388f;
    private static float COLOR_MAT_LOW_21 = -2.835158f;
    private static float COLOR_MAT_LOW_22 = 6.03384f;
    private static float COLOR_MAT_LOW_23 = -0.9592755f;
    private static float COLOR_MAT_LOW_24 = 1.2456132f;
    private static float COLOR_MAT_LOW_31 = -1.5958017f;
    private static float COLOR_MAT_LOW_32 = 2.4951355f;
    private static float COLOR_MAT_LOW_33 = 5.1863265f;
    private static float COLOR_MAT_LOW_34 = -4.55602f;
    private static float COLOR_MAT_RGBCW_HIGH_11 = 0.057276506f;
    private static float COLOR_MAT_RGBCW_HIGH_12 = 0.14116754f;
    private static float COLOR_MAT_RGBCW_HIGH_13 = -0.15786317f;
    private static float COLOR_MAT_RGBCW_HIGH_14 = 0.15437019f;
    private static float COLOR_MAT_RGBCW_HIGH_15 = -0.083569825f;
    private static float COLOR_MAT_RGBCW_HIGH_21 = 0.05078651f;
    private static float COLOR_MAT_RGBCW_HIGH_22 = 0.08124515f;
    private static float COLOR_MAT_RGBCW_HIGH_23 = -0.06590506f;
    private static float COLOR_MAT_RGBCW_HIGH_24 = 0.11051277f;
    private static float COLOR_MAT_RGBCW_HIGH_25 = -0.07230203f;
    private static float COLOR_MAT_RGBCW_HIGH_31 = 0.068063304f;
    private static float COLOR_MAT_RGBCW_HIGH_32 = -0.0063071745f;
    private static float COLOR_MAT_RGBCW_HIGH_33 = -0.1753012f;
    private static float COLOR_MAT_RGBCW_HIGH_34 = 0.23409711f;
    private static float COLOR_MAT_RGBCW_HIGH_35 = -0.0483615f;
    private static float COLOR_MAT_RGBCW_LOW_11 = 0.028766254f;
    private static float COLOR_MAT_RGBCW_LOW_12 = 0.06764202f;
    private static float COLOR_MAT_RGBCW_LOW_13 = 0.04326469f;
    private static float COLOR_MAT_RGBCW_LOW_14 = 0.009134116f;
    private static float COLOR_MAT_RGBCW_LOW_15 = -0.054076713f;
    private static float COLOR_MAT_RGBCW_LOW_21 = 0.023575246f;
    private static float COLOR_MAT_RGBCW_LOW_22 = 0.030006304f;
    private static float COLOR_MAT_RGBCW_LOW_23 = 0.097151525f;
    private static float COLOR_MAT_RGBCW_LOW_24 = -0.0017650401f;
    private static float COLOR_MAT_RGBCW_LOW_25 = -0.05097885f;
    private static float COLOR_MAT_RGBCW_LOW_31 = 0.0585f;
    private static float COLOR_MAT_RGBCW_LOW_32 = -0.041188527f;
    private static float COLOR_MAT_RGBCW_LOW_33 = -0.082708225f;
    private static float COLOR_MAT_RGBCW_LOW_34 = 0.17139152f;
    private static float COLOR_MAT_RGBCW_LOW_35 = -0.03970633f;
    /* access modifiers changed from: private */
    public static float DARK_ROOM_DELTA = 5.0f;
    /* access modifiers changed from: private */
    public static float DARK_ROOM_DELTA1 = 10.0f;
    /* access modifiers changed from: private */
    public static float DARK_ROOM_DELTA2 = 30.0f;
    /* access modifiers changed from: private */
    public static float DARK_ROOM_THRESH = 3.0f;
    /* access modifiers changed from: private */
    public static float DARK_ROOM_THRESH1 = 15.0f;
    /* access modifiers changed from: private */
    public static float DARK_ROOM_THRESH2 = 35.0f;
    /* access modifiers changed from: private */
    public static float DARK_ROOM_THRESH3 = 75.0f;
    /* access modifiers changed from: private */
    public static boolean DEBUG = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static String DUAL_SENSOR_XML_PATH = "/product/etc/xml/lcd/DualSensorConfig.xml";
    private static final int FLASHLIGHT_DETECTION_MODE_CALLBACK = 1;
    private static final int FLASHLIGHT_DETECTION_MODE_THRESHOLD = 0;
    /* access modifiers changed from: private */
    public static int FLASHLIGHT_OFF_TIME_THRESHOLD_MS = 600;
    private static final int FRONT_MSG_TIMER = 1;
    /* access modifiers changed from: private */
    public static int[] FRONT_SENSOR_STABILITY_THRESHOLD = {-30, -4, 4, 30};
    private static final int FUSED_MSG_TIMER = 3;
    private static float IR_BOUNDRY = 1.0f;
    private static float IR_BOUNDRY_RGBCW = 0.3f;
    public static final int LIGHT_SENSOR_BACK = 1;
    public static final int LIGHT_SENSOR_DEFAULT = -1;
    public static final int LIGHT_SENSOR_DUAL = 2;
    public static final int LIGHT_SENSOR_FRONT = 0;
    private static float LUX_COEF_HIGH_1 = -0.004354524f;
    private static float LUX_COEF_HIGH_2 = -0.06864114f;
    private static float LUX_COEF_HIGH_3 = 0.24784172f;
    private static float LUX_COEF_HIGH_4 = -0.15217727f;
    private static float LUX_COEF_HIGH_5 = -6.927572E-4f;
    private static float LUX_COEF_HIGH_BLUE = -0.06468f;
    private static float LUX_COEF_HIGH_GREEN = 0.19239f;
    private static float LUX_COEF_HIGH_IR = 0.5164043f;
    private static float LUX_COEF_HIGH_OFFSET = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
    private static float LUX_COEF_HIGH_RED = 0.02673f;
    private static float LUX_COEF_HIGH_X = -11.499684f;
    private static float LUX_COEF_HIGH_Y = 16.098469f;
    private static float LUX_COEF_HIGH_Z = -3.7601638f;
    private static float LUX_COEF_LOW_1 = 0.058521573f;
    private static float LUX_COEF_LOW_2 = -0.028247027f;
    private static float LUX_COEF_LOW_3 = -0.0530197f;
    private static float LUX_COEF_LOW_4 = -0.006297543f;
    private static float LUX_COEF_LOW_5 = 0.005755076f;
    private static float LUX_COEF_LOW_BLUE = -0.09306f;
    private static float LUX_COEF_LOW_GREEN = 0.02112f;
    private static float LUX_COEF_LOW_IR = -3.1549554f;
    private static float LUX_COEF_LOW_OFFSET = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
    private static float LUX_COEF_LOW_RED = 0.02442f;
    private static float LUX_COEF_LOW_X = -3.0825195f;
    private static float LUX_COEF_LOW_Y = 7.662419f;
    private static float LUX_COEF_LOW_Z = -6.400353f;
    private static final int MSG_TIMER = 0;
    private static float PRODUCTION_CALIBRATION_B = 1.0f;
    private static float PRODUCTION_CALIBRATION_C = 1.0f;
    private static float PRODUCTION_CALIBRATION_G = 1.0f;
    private static float PRODUCTION_CALIBRATION_IR1 = 1.0f;
    private static float PRODUCTION_CALIBRATION_R = 1.0f;
    private static float PRODUCTION_CALIBRATION_W = 1.0f;
    private static float PRODUCTION_CALIBRATION_X = 1.0f;
    private static float PRODUCTION_CALIBRATION_Y = 1.0f;
    private static float PRODUCTION_CALIBRATION_Z = 1.0f;
    private static float RATIO_IR_GREEN = 0.5f;
    /* access modifiers changed from: private */
    public static int[] STABILIZED_PROBABILITY_LUT = new int[25];
    private static final String TAG = "HwDualSensorEventListenerImpl";
    private static final int TIMER_DISABLE = -1;
    /* access modifiers changed from: private */
    public static int VERSION_SENSOR = 1;
    private static final int WARM_UP_FLAG = 2;
    private static float X_CONSTANT = 0.56695f;
    private static float X_G_COEF = -1.33815f;
    private static float X_G_QUADRATIC_COEF = 1.36349f;
    private static float X_RG_COEF = 2.5769f;
    private static float X_R_COEF = -1.20192f;
    private static float X_R_QUADRATIC_COEF = 1.17303f;
    private static float Y_CONSTANT = -0.43006f;
    private static float Y_G_COEF = 1.6651f;
    private static float Y_G_QUADRATIC_COEF = -0.7337f;
    private static float Y_RG_COEF = -0.5513f;
    private static float Y_R_COEF = 0.77904f;
    private static float Y_R_QUADRATIC_COEF = 0.03152f;
    /* access modifiers changed from: private */
    public static int mBackRateMillis = 300;
    private static final long mDebugPrintInterval = 1000000000;
    /* access modifiers changed from: private */
    public static int mFlashlightDetectionMode = 0;
    /* access modifiers changed from: private */
    public static int mFrontRateMillis = 300;
    /* access modifiers changed from: private */
    public static int mFusedRateMillis = 300;
    private static volatile HwDualSensorEventListenerImpl mInstance = null;
    private static Hashtable<String, Integer> mModuleSensorMap = new Hashtable<>();
    private static final int norm_coefficient = 12800;
    /* access modifiers changed from: private */
    public String mBackCameraId;
    /* access modifiers changed from: private */
    public boolean mBackEnable = false;
    /* access modifiers changed from: private */
    public long mBackEnableTime = -1;
    private Sensor mBackLightSensor;
    /* access modifiers changed from: private */
    public SensorDataSender mBackSender;
    /* access modifiers changed from: private */
    public int mBackSensorBypassCount = 0;
    /* access modifiers changed from: private */
    public int mBackSensorBypassCountMax = 3;
    /* access modifiers changed from: private */
    public SensorData mBackSensorData;
    private final SensorEventListener mBackSensorEventListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            int cct;
            int lux;
            int i;
            SensorEvent sensorEvent = event;
            if (!HwDualSensorEventListenerImpl.this.mBackEnable) {
                Slog.i(HwDualSensorEventListenerImpl.TAG, "SensorEventListener.onSensorChanged(): mBackEnable=false");
                return;
            }
            long sensorClockTimeStamp = sensorEvent.timestamp;
            long systemClockTimeStamp = SystemClock.uptimeMillis();
            int[] arr = {0, 0};
            if (HwDualSensorEventListenerImpl.VERSION_SENSOR == 1) {
                arr = HwDualSensorEventListenerImpl.this.convertXYZ2LuxAndCct(Float.valueOf(sensorEvent.values[0]), Float.valueOf(sensorEvent.values[1]), Float.valueOf(sensorEvent.values[2]), Float.valueOf(sensorEvent.values[3]));
            } else if (HwDualSensorEventListenerImpl.VERSION_SENSOR == 2) {
                arr = HwDualSensorEventListenerImpl.this.convertRGB2LuxAndCct(sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2], sensorEvent.values[3]);
            } else if (HwDualSensorEventListenerImpl.VERSION_SENSOR == 3) {
                arr = HwDualSensorEventListenerImpl.this.convertRGBCW2LuxAndCct(Float.valueOf(sensorEvent.values[0]), Float.valueOf(sensorEvent.values[1]), Float.valueOf(sensorEvent.values[2]), Float.valueOf(sensorEvent.values[3]), Float.valueOf(sensorEvent.values[4]));
            } else if (HwDualSensorEventListenerImpl.DEBUG) {
                Slog.i(HwDualSensorEventListenerImpl.TAG, "Invalid VERSION_SENSOR=" + HwDualSensorEventListenerImpl.VERSION_SENSOR);
            }
            int[] arr2 = arr;
            int lux2 = arr2[0];
            int cct2 = arr2[1];
            if (HwDualSensorEventListenerImpl.this.mBackWarmUpFlg >= 2) {
                lux = lux2;
                cct = cct2;
                i = 1;
                HwDualSensorEventListenerImpl.this.mBackSensorData.setSensorData(lux, cct, systemClockTimeStamp, sensorClockTimeStamp);
            } else if (sensorClockTimeStamp < HwDualSensorEventListenerImpl.this.mBackEnableTime) {
                if (HwDualSensorEventListenerImpl.DEBUG) {
                    Slog.d(HwDualSensorEventListenerImpl.TAG, "SensorEventListener.onSensorChanged(): Back sensor not ready yet!");
                }
                return;
            } else {
                lux = lux2;
                cct = cct2;
                i = 1;
                HwDualSensorEventListenerImpl.this.mBackSensorData.setSensorData(lux2, cct2, systemClockTimeStamp, sensorClockTimeStamp);
                int unused = HwDualSensorEventListenerImpl.this.mBackWarmUpFlg = HwDualSensorEventListenerImpl.this.mBackWarmUpFlg + 1;
                HwDualSensorEventListenerImpl.this.mHandler.removeMessages(2);
                HwDualSensorEventListenerImpl.this.mHandler.sendEmptyMessage(2);
            }
            if (!HwDualSensorEventListenerImpl.DEBUG || sensorClockTimeStamp - HwDualSensorEventListenerImpl.this.mBackEnableTime >= 1000000000) {
                int i2 = cct;
            } else if (HwDualSensorEventListenerImpl.VERSION_SENSOR == i) {
                Slog.d(HwDualSensorEventListenerImpl.TAG, "SensorEventListener.onSensorChanged(): warmUp=" + HwDualSensorEventListenerImpl.this.mBackWarmUpFlg + " lux=" + lux + " cct=" + cct + " sensorTime=" + sensorClockTimeStamp + " systemTime=" + systemClockTimeStamp + " Xraw=" + sensorEvent.values[0] + " Yraw=" + sensorEvent.values[i] + " Zraw=" + sensorEvent.values[2] + " IRraw=" + sensorEvent.values[3]);
            } else {
                if (HwDualSensorEventListenerImpl.VERSION_SENSOR == 2) {
                    Slog.d(HwDualSensorEventListenerImpl.TAG, "SensorEventListener.onSensorChanged(): warmUp=" + HwDualSensorEventListenerImpl.this.mBackWarmUpFlg + " lux=" + lux + " cct=" + cct + " sensorTime=" + sensorClockTimeStamp + " systemTime=" + systemClockTimeStamp + " red=" + sensorEvent.values[0] + " green=" + sensorEvent.values[i] + " blue=" + sensorEvent.values[2] + " IR=" + sensorEvent.values[3]);
                } else if (HwDualSensorEventListenerImpl.VERSION_SENSOR == 3) {
                    Slog.d(HwDualSensorEventListenerImpl.TAG, "SensorEventListener.onSensorChanged(): warmUp=" + HwDualSensorEventListenerImpl.this.mBackWarmUpFlg + " lux=" + lux + " cct=" + cct + " sensorTime=" + sensorClockTimeStamp + " systemTime=" + systemClockTimeStamp + " Craw=" + sensorEvent.values[0] + " Rraw=" + sensorEvent.values[i] + " Graw=" + sensorEvent.values[2] + " Braw=" + sensorEvent.values[3] + " Wraw=" + sensorEvent.values[4]);
                }
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    private final Object mBackSensorLock = new Object();
    /* access modifiers changed from: private */
    public int mBackSensorTimeOutTH = 5;
    /* access modifiers changed from: private */
    public int mBackTimeOutCount = 0;
    /* access modifiers changed from: private */
    public int mBackTimer = -1;
    /* access modifiers changed from: private */
    public int mBackWarmUpFlg = 0;
    private CameraManager mCameraManager;
    private final Context mContext;
    private final HandlerThread mDualSensorProcessThread;
    /* access modifiers changed from: private */
    public volatile long mFlashLightOffTimeStampMs = 0;
    /* access modifiers changed from: private */
    public boolean mFrontEnable = false;
    /* access modifiers changed from: private */
    public long mFrontEnableTime = -1;
    private Sensor mFrontLightSensor;
    /* access modifiers changed from: private */
    public SensorDataSender mFrontSender;
    /* access modifiers changed from: private */
    public SensorData mFrontSensorData;
    private final SensorEventListener mFrontSensorEventListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            if (!HwDualSensorEventListenerImpl.this.mFrontEnable) {
                Slog.i(HwDualSensorEventListenerImpl.TAG, "SensorEventListener.onSensorChanged(): mFrontEnable=false");
                return;
            }
            long systemClockTimeStamp = SystemClock.uptimeMillis();
            int lux = (int) (event.values[0] + 0.5f);
            int cct = (int) (event.values[1] + 0.5f);
            long sensorClockTimeStamp = event.timestamp;
            if (HwDualSensorEventListenerImpl.this.mFrontWarmUpFlg >= 2) {
                HwDualSensorEventListenerImpl.this.mFrontSensorData.setSensorData(lux, cct, systemClockTimeStamp, sensorClockTimeStamp);
            } else if (sensorClockTimeStamp < HwDualSensorEventListenerImpl.this.mFrontEnableTime) {
                if (HwDualSensorEventListenerImpl.DEBUG) {
                    Slog.d(HwDualSensorEventListenerImpl.TAG, "SensorEventListener.onSensorChanged(): Front sensor not ready yet!");
                }
                return;
            } else {
                HwDualSensorEventListenerImpl.this.mFrontSensorData.setSensorData(lux, cct, systemClockTimeStamp, sensorClockTimeStamp);
                int unused = HwDualSensorEventListenerImpl.this.mFrontWarmUpFlg = HwDualSensorEventListenerImpl.this.mFrontWarmUpFlg + 1;
                HwDualSensorEventListenerImpl.this.mHandler.removeMessages(1);
                HwDualSensorEventListenerImpl.this.mHandler.sendEmptyMessage(1);
            }
            if (HwDualSensorEventListenerImpl.DEBUG && sensorClockTimeStamp - HwDualSensorEventListenerImpl.this.mFrontEnableTime < 1000000000) {
                Slog.d(HwDualSensorEventListenerImpl.TAG, "SensorEventListener.onSensorChanged(): warmUp=" + HwDualSensorEventListenerImpl.this.mFrontWarmUpFlg + " lux=" + lux + " cct=" + cct + " sensorTime=" + sensorClockTimeStamp + " systemTime=" + systemClockTimeStamp);
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    private final Object mFrontSensorLock = new Object();
    /* access modifiers changed from: private */
    public int mFrontTimer = -1;
    /* access modifiers changed from: private */
    public int mFrontWarmUpFlg = 0;
    /* access modifiers changed from: private */
    public boolean mFusedEnable = false;
    /* access modifiers changed from: private */
    public long mFusedEnableTime = -1;
    /* access modifiers changed from: private */
    public SensorDataSender mFusedSender;
    /* access modifiers changed from: private */
    public FusedSensorData mFusedSensorData;
    /* access modifiers changed from: private */
    public final Object mFusedSensorLock = new Object();
    /* access modifiers changed from: private */
    public int mFusedTimer = -1;
    /* access modifiers changed from: private */
    public int mFusedWarmUpFlg = 0;
    /* access modifiers changed from: private */
    public final Handler mHandler;
    private SensorManager mSensorManager;
    /* access modifiers changed from: private */
    public int mTimer = -1;
    private CameraManager.TorchCallback mTorchCallback = new CameraManager.TorchCallback() {
        public void onTorchModeUnavailable(String cameraId) {
            super.onTorchModeUnavailable(cameraId);
            long unused = HwDualSensorEventListenerImpl.this.mFlashLightOffTimeStampMs = -2;
            Slog.i(HwDualSensorEventListenerImpl.TAG, "onTorchModeUnavailable, mFlashLightOffTimeStampMs set -2");
        }

        public void onTorchModeChanged(String cameraId, boolean enabled) {
            super.onTorchModeChanged(cameraId, enabled);
            if (!cameraId.equals(HwDualSensorEventListenerImpl.this.mBackCameraId)) {
                return;
            }
            if (enabled) {
                long unused = HwDualSensorEventListenerImpl.this.mFlashLightOffTimeStampMs = -1;
                Slog.i(HwDualSensorEventListenerImpl.TAG, "mFlashLightOffTimeStampMs set -1");
                return;
            }
            long unused2 = HwDualSensorEventListenerImpl.this.mFlashLightOffTimeStampMs = SystemClock.uptimeMillis();
            Slog.i(HwDualSensorEventListenerImpl.TAG, "mFlashLightOffTimeStampMs set " + HwDualSensorEventListenerImpl.this.mFlashLightOffTimeStampMs);
        }
    };

    private final class DualSensorHandler extends Handler {
        private long mTimestamp = 0;

        public DualSensorHandler(Looper looper) {
            super(looper, null, true);
        }

        private void updateTimer(int newTimer) {
            if (newTimer == -1) {
                return;
            }
            if (HwDualSensorEventListenerImpl.this.mTimer == -1) {
                int unused = HwDualSensorEventListenerImpl.this.mTimer = newTimer;
            } else if (newTimer < HwDualSensorEventListenerImpl.this.mTimer) {
                int unused2 = HwDualSensorEventListenerImpl.this.mTimer = newTimer;
            }
        }

        private void updateTimerAndSendData(SensorDataSender sender) {
            sender.sendData();
            if (sender.needQueue()) {
                sender.setTimer(sender.getRate());
            } else {
                sender.setTimer(-1);
            }
        }

        private void processTimer(SensorDataSender sender) {
            int timer = sender.getTimer();
            if (timer == -1) {
                return;
            }
            if (HwDualSensorEventListenerImpl.this.mTimer != -1) {
                int timer2 = timer - HwDualSensorEventListenerImpl.this.mTimer;
                if (timer2 < 0) {
                    timer2 = 0;
                }
                if (timer2 == 0) {
                    updateTimerAndSendData(sender);
                    return;
                }
                return;
            }
            sender.setTimer(-1);
            Slog.w(HwDualSensorEventListenerImpl.TAG, "Timer has been queued, but mTimer is invalid!");
        }

        private void processTimerAsTrigger(SensorDataSender sender) {
            updateTimerAndSendData(sender);
            if (sender.getTimer() == -1) {
                return;
            }
            if (HwDualSensorEventListenerImpl.this.mTimer == -1) {
                queueMessage(sender.getTimer());
                return;
            }
            if (HwDualSensorEventListenerImpl.this.mTimer - ((int) (SystemClock.uptimeMillis() - this.mTimestamp)) > sender.getTimer()) {
                queueMessage(sender.getTimer());
            }
        }

        private void queueMessage(int timer) {
            int unused = HwDualSensorEventListenerImpl.this.mTimer = timer;
            queueMessage();
        }

        private void queueMessage() {
            this.mTimestamp = SystemClock.uptimeMillis();
            removeMessages(0);
            sendEmptyMessageDelayed(0, (long) HwDualSensorEventListenerImpl.this.mTimer);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    processTimer(HwDualSensorEventListenerImpl.this.mFrontSender);
                    processTimer(HwDualSensorEventListenerImpl.this.mBackSender);
                    processTimer(HwDualSensorEventListenerImpl.this.mFusedSender);
                    int unused = HwDualSensorEventListenerImpl.this.mTimer = -1;
                    updateTimer(HwDualSensorEventListenerImpl.this.mFrontTimer);
                    updateTimer(HwDualSensorEventListenerImpl.this.mBackTimer);
                    updateTimer(HwDualSensorEventListenerImpl.this.mFusedTimer);
                    if (HwDualSensorEventListenerImpl.this.mTimer != -1) {
                        queueMessage();
                        return;
                    }
                    return;
                case 1:
                    processTimerAsTrigger(HwDualSensorEventListenerImpl.this.mFrontSender);
                    return;
                case 2:
                    processTimerAsTrigger(HwDualSensorEventListenerImpl.this.mBackSender);
                    return;
                case 3:
                    processTimerAsTrigger(HwDualSensorEventListenerImpl.this.mFusedSender);
                    return;
                default:
                    Slog.i(HwDualSensorEventListenerImpl.TAG, "handleMessage: Invalid message");
                    return;
            }
        }
    }

    private class FusedSensorData extends Observable {
        private float mAdjustMin;
        private float mBackFloorThresh;
        private long mBackLastValueSystemTime = -1;
        private int[] mBackLuxArray = {-1, -1, -1};
        private long mBackLuxDeviation;
        private int mBackLuxFiltered = -1;
        private int mBackLuxIndex = 0;
        private float mBackLuxRelativeChange = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        private int mBackLuxStaProInd;
        private float mBackRoofThresh;
        public SensorDataObserver mBackSensorDataObserver;
        /* access modifiers changed from: private */
        public boolean mBackUpdated;
        private int mCurBackCct = -1;
        private float mCurBackFiltered = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        private int mCurBackLux = -1;
        private int mCurFrontCct = -1;
        private float mCurFrontFiltered = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        private int mCurFrontLux = -1;
        private float mDarkRoomDelta;
        private float mDarkRoomDelta1;
        private float mDarkRoomDelta2;
        private float mDarkRoomThresh;
        private float mDarkRoomThresh1;
        private float mDarkRoomThresh2;
        private float mDarkRoomThresh3;
        /* access modifiers changed from: private */
        public long mDebugPrintTime = -1;
        private long mFrontLastValueSystemTime = -1;
        private int[] mFrontLuxArray = {-1, -1, -1};
        private int mFrontLuxFiltered = -1;
        private int mFrontLuxIndex = 0;
        private float mFrontLuxRelativeChange = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        private int mFrontLuxStaProInd;
        public SensorDataObserver mFrontSensorDataObserver;
        /* access modifiers changed from: private */
        public boolean mFrontUpdated;
        private int mFusedCct = -1;
        private int mFusedLux = -1;
        private long mLastValueBackSensorTime = -1;
        private long mLastValueFrontSensorTime = -1;
        private float mModifyFactor;
        private float mPreBackFiltered = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        private float mPreFrontFiltered = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        private float mStaProb;
        private int[][] mStaProbLUT = ((int[][]) Array.newInstance(int.class, new int[]{5, 5}));

        public class SensorDataObserver implements Observer {
            private final String mName;
            private final FusedSensorDataRefresher mRefresher;

            public SensorDataObserver(FusedSensorDataRefresher refresher, String name) {
                this.mRefresher = refresher;
                this.mName = name;
            }

            public void update(Observable o, Object arg) {
                long[] data = (long[]) arg;
                int lux = (int) data[0];
                int cct = (int) data[1];
                long systemTimeStamp = data[2];
                long sensorTimeStamp = data[3];
                long unused = FusedSensorData.this.mDebugPrintTime = SystemClock.elapsedRealtimeNanos();
                if (HwDualSensorEventListenerImpl.DEBUG && FusedSensorData.this.mDebugPrintTime - HwDualSensorEventListenerImpl.this.mFusedEnableTime < 1000000000) {
                    Slog.d(HwDualSensorEventListenerImpl.TAG, "FusedSensorData.update(): " + this.mName + " warmUp=" + HwDualSensorEventListenerImpl.this.mFusedWarmUpFlg + " lux=" + lux + " cct=" + cct + " systemTime=" + systemTimeStamp + " sensorTime=" + sensorTimeStamp);
                }
                long j = sensorTimeStamp;
                this.mRefresher.updateData(lux, cct, systemTimeStamp, sensorTimeStamp);
                if (HwDualSensorEventListenerImpl.this.mFusedWarmUpFlg < 2) {
                    if (FusedSensorData.this.mFrontUpdated && FusedSensorData.this.mBackUpdated) {
                        int unused2 = HwDualSensorEventListenerImpl.this.mFusedWarmUpFlg = HwDualSensorEventListenerImpl.this.mFusedWarmUpFlg + 1;
                    }
                    HwDualSensorEventListenerImpl.this.mHandler.removeMessages(3);
                    HwDualSensorEventListenerImpl.this.mHandler.sendEmptyMessage(3);
                }
            }
        }

        public FusedSensorData() {
            this.mStaProbLUT[0][0] = 0;
            this.mStaProbLUT[0][1] = 20;
            this.mStaProbLUT[0][2] = 90;
            this.mStaProbLUT[0][3] = 20;
            this.mStaProbLUT[0][4] = 0;
            this.mStaProbLUT[1][0] = 20;
            this.mStaProbLUT[1][1] = 50;
            this.mStaProbLUT[1][2] = 90;
            this.mStaProbLUT[1][3] = 50;
            this.mStaProbLUT[1][4] = 20;
            this.mStaProbLUT[2][0] = 90;
            this.mStaProbLUT[2][1] = 90;
            this.mStaProbLUT[2][2] = 95;
            this.mStaProbLUT[2][3] = 90;
            this.mStaProbLUT[2][4] = 90;
            this.mStaProbLUT[3][0] = 20;
            this.mStaProbLUT[3][1] = 50;
            this.mStaProbLUT[3][2] = 90;
            this.mStaProbLUT[3][3] = 50;
            this.mStaProbLUT[3][4] = 20;
            this.mStaProbLUT[4][0] = 0;
            this.mStaProbLUT[4][1] = 20;
            this.mStaProbLUT[4][2] = 90;
            this.mStaProbLUT[4][3] = 20;
            this.mStaProbLUT[4][4] = 0;
            this.mFrontLuxStaProInd = 0;
            this.mBackLuxStaProInd = 0;
            this.mAdjustMin = 1.0f;
            this.mBackRoofThresh = HwDualSensorEventListenerImpl.BACK_ROOF_THRESH;
            this.mBackFloorThresh = HwDualSensorEventListenerImpl.BACK_FLOOR_THRESH;
            this.mDarkRoomThresh = HwDualSensorEventListenerImpl.DARK_ROOM_THRESH;
            this.mDarkRoomDelta = HwDualSensorEventListenerImpl.DARK_ROOM_DELTA;
            this.mDarkRoomThresh1 = HwDualSensorEventListenerImpl.DARK_ROOM_THRESH1;
            this.mDarkRoomThresh2 = HwDualSensorEventListenerImpl.DARK_ROOM_THRESH2;
            this.mDarkRoomThresh3 = HwDualSensorEventListenerImpl.DARK_ROOM_THRESH3;
            this.mDarkRoomDelta1 = HwDualSensorEventListenerImpl.DARK_ROOM_DELTA1;
            this.mDarkRoomDelta2 = HwDualSensorEventListenerImpl.DARK_ROOM_DELTA2;
            this.mBackLuxDeviation = 0;
            this.mFrontSensorDataObserver = new SensorDataObserver(new FusedSensorDataRefresher(HwDualSensorEventListenerImpl.this) {
                public void updateData(int lux, int cct, long systemTimeStamp, long sensorTimeStamp) {
                    FusedSensorData.this.setFrontData(lux, cct, systemTimeStamp, sensorTimeStamp);
                }
            }, "FrontSensor");
            this.mBackSensorDataObserver = new SensorDataObserver(new FusedSensorDataRefresher(HwDualSensorEventListenerImpl.this) {
                public void updateData(int lux, int cct, long systemTimeStamp, long sensorTimeStamp) {
                    FusedSensorData.this.setBackData(lux, cct, systemTimeStamp, sensorTimeStamp);
                }
            }, "BackSensor");
            this.mFrontUpdated = false;
            this.mBackUpdated = false;
            setStabilityProbabilityLUT();
        }

        public void setFrontData(int lux, int cct, long systemTimeStamp, long sensorTimeStamp) {
            this.mFrontLuxStaProInd = getLuxStableProbabilityIndex(lux, true);
            this.mCurFrontCct = cct;
            this.mFrontLastValueSystemTime = systemTimeStamp;
            this.mLastValueFrontSensorTime = sensorTimeStamp;
            this.mFrontUpdated = true;
            if (HwDualSensorEventListenerImpl.DEBUG && this.mDebugPrintTime - HwDualSensorEventListenerImpl.this.mFusedEnableTime < 1000000000) {
                Slog.d(HwDualSensorEventListenerImpl.TAG, "FusedSensorData.setFrontData(): mCurFrontFiltered=" + this.mCurFrontFiltered + " mFrontLuxRelativeChange= " + this.mFrontLuxRelativeChange + " lux=" + lux + " cct=" + cct + " systemTime=" + this.mFrontLastValueSystemTime + " sensorTime=" + this.mLastValueFrontSensorTime);
            }
        }

        public void setBackData(int lux, int cct, long systemTimeStamp, long sensorTimeStamp) {
            this.mBackLuxStaProInd = getLuxStableProbabilityIndex(lux, false);
            this.mCurBackCct = cct;
            this.mBackLastValueSystemTime = systemTimeStamp;
            this.mLastValueBackSensorTime = sensorTimeStamp;
            this.mBackUpdated = true;
            if (HwDualSensorEventListenerImpl.DEBUG && this.mDebugPrintTime - HwDualSensorEventListenerImpl.this.mFusedEnableTime < 1000000000) {
                Slog.d(HwDualSensorEventListenerImpl.TAG, "FusedSensorData.setBackData(): mCurBackFiltered=" + this.mCurBackFiltered + " mBackLuxRelativeChange=" + this.mBackLuxRelativeChange + " lux=" + lux + " cct=" + cct + " systemTime=" + this.mBackLastValueSystemTime + " sensorTime=" + this.mLastValueBackSensorTime);
            }
        }

        private int getLuxStableProbabilityIndex(int lux, boolean frontSensorIsSelected) {
            int i = lux;
            boolean z = frontSensorIsSelected;
            if (i < 0 && HwDualSensorEventListenerImpl.DEBUG) {
                Slog.d(HwDualSensorEventListenerImpl.TAG, "getLuxStableProbabilityIndex exception: lux=" + i + " frontSensorIsSelected=" + z);
            }
            int[] luxArray = z ? this.mFrontLuxArray : this.mBackLuxArray;
            int luxArrayIndex = z ? this.mFrontLuxIndex : this.mBackLuxIndex;
            float preFiltered = z ? this.mPreFrontFiltered : this.mPreBackFiltered;
            int[] stabilityThresholdList = z ? HwDualSensorEventListenerImpl.FRONT_SENSOR_STABILITY_THRESHOLD : HwDualSensorEventListenerImpl.BACK_SENSOR_STABILITY_THRESHOLD;
            luxArray[luxArrayIndex] = i;
            int luxArrayIndex2 = (luxArrayIndex + 1) % 3;
            int count = 0;
            int curInd = 0;
            int sum = 0;
            for (int i2 = 0; i2 < 3; i2++) {
                if (luxArray[i2] > 0) {
                    sum += luxArray[i2];
                    count++;
                }
            }
            float curFiltered = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
            if (count > 0) {
                curFiltered = ((float) sum) / ((float) count);
            }
            if (!z) {
                this.mBackLuxDeviation = 0;
                while (true) {
                    int curInd2 = curInd;
                    if (curInd2 >= luxArray.length) {
                        break;
                    }
                    this.mBackLuxDeviation += Math.abs(((long) luxArray[curInd2]) - ((long) curFiltered));
                    curInd = curInd2 + 1;
                    count = count;
                }
                this.mBackLuxDeviation /= (long) luxArray.length;
            }
            float luxRelativeChange = 100.0f;
            if (Float.compare(preFiltered, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO) != 0) {
                luxRelativeChange = (100.0f * (curFiltered - preFiltered)) / preFiltered;
            }
            int targetIndex = 0;
            int stabilityThresholdListSize = stabilityThresholdList.length;
            while (targetIndex < stabilityThresholdListSize && Float.compare(luxRelativeChange, (float) stabilityThresholdList[targetIndex]) > 0) {
                targetIndex++;
            }
            if (targetIndex > stabilityThresholdListSize) {
                targetIndex = stabilityThresholdListSize;
            }
            if (z) {
                this.mCurFrontFiltered = curFiltered;
                this.mFrontLuxRelativeChange = luxRelativeChange;
                this.mFrontLuxIndex = luxArrayIndex2;
            } else {
                this.mCurBackFiltered = curFiltered;
                this.mBackLuxRelativeChange = luxRelativeChange;
                this.mBackLuxIndex = luxArrayIndex2;
            }
            return targetIndex;
        }

        private void updateFusedData() {
            getStabilizedLuxAndCct();
        }

        private void getStabilizedLuxAndCct() {
            float curDarkRoomDelta;
            this.mFrontLuxFiltered = (int) this.mCurFrontFiltered;
            this.mBackLuxFiltered = (int) this.mCurBackFiltered;
            if (Float.compare(this.mCurFrontFiltered, this.mCurBackFiltered) >= 0) {
                this.mCurFrontLux = (int) this.mCurFrontFiltered;
                this.mCurBackLux = (int) this.mCurBackFiltered;
                if (this.mCurFrontLux >= 0 || this.mCurBackLux >= 0) {
                    if (this.mCurFrontLux < 0) {
                        this.mCurFrontLux = this.mCurBackLux;
                        this.mCurFrontCct = this.mCurBackCct;
                    } else if (this.mCurBackLux < 0) {
                        this.mCurBackLux = this.mCurFrontLux;
                        this.mCurBackCct = this.mCurFrontCct;
                    }
                    this.mFusedLux = this.mCurFrontLux;
                    this.mFusedCct = this.mCurFrontCct > this.mCurBackCct ? this.mCurFrontCct : this.mCurBackCct;
                } else {
                    if (HwDualSensorEventListenerImpl.DEBUG) {
                        Slog.d(HwDualSensorEventListenerImpl.TAG, "FusedSensorData.updateFusedData(): updateFusedData returned, mCurFrontLux=" + this.mCurFrontLux + " mCurBackLux=" + this.mCurBackLux);
                    }
                    return;
                }
            } else if (isFlashlightOn()) {
                this.mBackLuxFiltered = this.mFrontLuxFiltered;
                this.mCurFrontLux = (int) this.mCurFrontFiltered;
                this.mCurBackLux = (int) this.mCurBackFiltered;
                if (this.mCurFrontLux >= 0 || this.mCurBackLux >= 0) {
                    if (this.mCurFrontLux < 0) {
                        this.mCurFrontLux = this.mCurBackLux;
                        this.mCurFrontCct = this.mCurBackCct;
                    } else if (this.mCurBackLux < 0) {
                        this.mCurBackLux = this.mCurFrontLux;
                        this.mCurBackCct = this.mCurFrontCct;
                    }
                    this.mFusedLux = this.mCurFrontLux;
                    this.mFusedCct = this.mCurFrontCct > this.mCurBackCct ? this.mCurFrontCct : this.mCurBackCct;
                    if (HwDualSensorEventListenerImpl.DEBUG && this.mDebugPrintTime - HwDualSensorEventListenerImpl.this.mFusedEnableTime < 1000000000) {
                        Slog.d(HwDualSensorEventListenerImpl.TAG, "FusedSensorData.updateFusedData(): mFusedLux=" + this.mFusedLux + " mCurFrontLux=" + this.mCurFrontLux + " mCurBackLux=" + this.mCurBackLux + " mFusedCct=" + this.mFusedCct + " mCurFrontCct=" + this.mCurFrontCct + " mCurBackCct=" + this.mCurBackCct);
                    }
                } else {
                    if (HwDualSensorEventListenerImpl.DEBUG) {
                        Slog.d(HwDualSensorEventListenerImpl.TAG, "FusedSensorData.updateFusedData(): updateFusedData returned, mCurFrontLux=" + this.mCurFrontLux + " mCurBackLux=" + this.mCurBackLux);
                    }
                    return;
                }
            } else {
                float darkRoomDelta3 = this.mCurBackFiltered - this.mCurFrontFiltered;
                if (this.mCurFrontFiltered >= GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO && this.mCurFrontFiltered <= this.mDarkRoomThresh) {
                    curDarkRoomDelta = this.mDarkRoomDelta;
                } else if (Float.compare(this.mCurFrontFiltered, this.mDarkRoomThresh1) <= 0) {
                    curDarkRoomDelta = (((this.mCurFrontFiltered - this.mDarkRoomThresh) * (this.mDarkRoomDelta1 - this.mDarkRoomDelta)) / (this.mDarkRoomThresh1 - this.mDarkRoomThresh)) + this.mDarkRoomDelta;
                } else if (Float.compare(this.mCurFrontFiltered, this.mDarkRoomThresh2) < 0) {
                    curDarkRoomDelta = (((this.mCurFrontFiltered - this.mDarkRoomThresh1) * (this.mDarkRoomDelta2 - this.mDarkRoomDelta1)) / (this.mDarkRoomThresh2 - this.mDarkRoomThresh1)) + this.mDarkRoomDelta1;
                } else if (Float.compare(this.mCurFrontFiltered, this.mDarkRoomThresh3) < 0) {
                    curDarkRoomDelta = (((this.mCurFrontFiltered - this.mDarkRoomThresh2) * (darkRoomDelta3 - this.mDarkRoomDelta2)) / (this.mDarkRoomThresh3 - this.mDarkRoomThresh2)) + this.mDarkRoomDelta2;
                } else {
                    curDarkRoomDelta = darkRoomDelta3;
                }
                this.mBackFloorThresh = this.mCurFrontFiltered + curDarkRoomDelta;
                this.mCurBackFiltered = this.mCurBackFiltered < this.mBackFloorThresh ? this.mCurBackFiltered : this.mBackFloorThresh;
                this.mStaProb = (float) this.mStaProbLUT[this.mBackLuxStaProInd][this.mFrontLuxStaProInd];
                if (this.mStaProb == GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO) {
                    this.mPreFrontFiltered = this.mCurFrontFiltered;
                    this.mPreBackFiltered = this.mCurBackFiltered;
                } else {
                    if (this.mStaProb == 20.0f) {
                        this.mModifyFactor = 0.02f;
                    } else if (this.mStaProb == 50.0f) {
                        this.mModifyFactor = 0.01f;
                    } else if (this.mStaProb == 90.0f) {
                        this.mModifyFactor = 0.004f;
                    } else {
                        this.mModifyFactor = 0.002f;
                    }
                    float FrontAdjustment = this.mModifyFactor * this.mPreFrontFiltered;
                    if (FrontAdjustment < this.mAdjustMin) {
                        FrontAdjustment = this.mAdjustMin;
                    }
                    float BackAdjustment = this.mModifyFactor * this.mPreBackFiltered;
                    if (BackAdjustment < this.mAdjustMin) {
                        BackAdjustment = this.mAdjustMin;
                    }
                    if (this.mCurFrontFiltered < this.mPreFrontFiltered) {
                        this.mPreFrontFiltered -= FrontAdjustment;
                    } else if (this.mCurFrontFiltered > this.mPreFrontFiltered) {
                        this.mPreFrontFiltered += FrontAdjustment;
                    }
                    if (this.mCurBackFiltered < this.mPreBackFiltered) {
                        this.mPreBackFiltered -= BackAdjustment;
                    } else if (this.mCurFrontFiltered > this.mPreFrontFiltered) {
                        this.mPreBackFiltered += BackAdjustment;
                    }
                }
                this.mCurFrontLux = (int) this.mPreFrontFiltered;
                this.mCurBackLux = (int) this.mPreBackFiltered;
                if (this.mCurFrontLux >= 0 || this.mCurBackLux >= 0) {
                    if (this.mCurFrontLux < 0) {
                        this.mCurFrontLux = this.mCurBackLux;
                        this.mCurFrontCct = this.mCurBackCct;
                    } else if (this.mCurBackLux < 0) {
                        this.mCurBackLux = this.mCurFrontLux;
                        this.mCurBackCct = this.mCurFrontCct;
                    }
                    this.mFusedLux = this.mCurFrontLux > this.mCurBackLux ? this.mCurFrontLux : this.mCurBackLux;
                    this.mFusedCct = this.mCurFrontCct > this.mCurBackCct ? this.mCurFrontCct : this.mCurBackCct;
                } else {
                    if (HwDualSensorEventListenerImpl.DEBUG) {
                        Slog.d(HwDualSensorEventListenerImpl.TAG, "FusedSensorData.updateFusedData(): updateFusedData returned, mCurFrontLux=" + this.mCurFrontLux + " mCurBackLux=" + this.mCurBackLux);
                    }
                    return;
                }
            }
            if (HwDualSensorEventListenerImpl.DEBUG && this.mDebugPrintTime - HwDualSensorEventListenerImpl.this.mFusedEnableTime < 1000000000) {
                Slog.d(HwDualSensorEventListenerImpl.TAG, "FusedSensorData.updateFusedData(): mFusedLux=" + this.mFusedLux + " mCurFrontLux=" + this.mCurFrontLux + " mCurBackLux=" + this.mCurBackLux + " mFusedCct=" + this.mFusedCct + " mCurFrontCct=" + this.mCurFrontCct + " mCurBackCct=" + this.mCurBackCct);
            }
        }

        public void sendFusedData() {
            if (!this.mFrontUpdated) {
                if (HwDualSensorEventListenerImpl.DEBUG) {
                    Slog.d(HwDualSensorEventListenerImpl.TAG, "Skip this sendFusedData! mFrontUpdated=" + this.mFrontUpdated + " mBackUpdated=" + this.mBackUpdated);
                }
                return;
            }
            if (HwDualSensorEventListenerImpl.this.mBackSensorBypassCount <= 0 && !this.mBackUpdated) {
                int unused = HwDualSensorEventListenerImpl.this.mBackTimeOutCount = HwDualSensorEventListenerImpl.this.mBackTimeOutCount + 1;
                if (HwDualSensorEventListenerImpl.this.mBackTimeOutCount >= HwDualSensorEventListenerImpl.this.mBackSensorTimeOutTH) {
                    int unused2 = HwDualSensorEventListenerImpl.this.mBackSensorBypassCount = HwDualSensorEventListenerImpl.this.mBackSensorBypassCountMax;
                    Slog.i(HwDualSensorEventListenerImpl.TAG, "mBackTimeOutCount start: " + HwDualSensorEventListenerImpl.this.mBackTimeOutCount);
                } else {
                    if (HwDualSensorEventListenerImpl.DEBUG && this.mDebugPrintTime - HwDualSensorEventListenerImpl.this.mFusedEnableTime < 1000000000) {
                        Slog.d(HwDualSensorEventListenerImpl.TAG, "Skip this sendFusedData! mFrontUpdated=" + this.mFrontUpdated + " mBackUpdated=" + this.mBackUpdated);
                    }
                    return;
                }
            }
            int unused3 = HwDualSensorEventListenerImpl.this.mBackTimeOutCount = 0;
            updateFusedData();
            long lastValueSensorTime = this.mLastValueFrontSensorTime > this.mLastValueBackSensorTime ? this.mLastValueFrontSensorTime : this.mLastValueBackSensorTime;
            Object dataToSend = {(long) this.mFusedLux, (long) this.mFusedCct, this.mFrontLastValueSystemTime > this.mBackLastValueSystemTime ? this.mFrontLastValueSystemTime : this.mBackLastValueSystemTime, lastValueSensorTime, (long) this.mFrontLuxFiltered, (long) this.mBackLuxFiltered};
            if (this.mFusedLux < 0 || this.mFusedCct < 0) {
                Slog.i(HwDualSensorEventListenerImpl.TAG, "FusedSensorData.sendFusedData(): skip. mFusedLux=" + this.mFusedLux + " mFusedCct=" + this.mFusedCct);
                return;
            }
            if (HwDualSensorEventListenerImpl.DEBUG && this.mDebugPrintTime - HwDualSensorEventListenerImpl.this.mFusedEnableTime < 1000000000) {
                Slog.d(HwDualSensorEventListenerImpl.TAG, "FusedSensorData.sendFusedData(): lux=" + this.mFusedLux + " cct=" + this.mFusedCct + " sensorTime=" + lastValueSensorTime + " FrontSensorTime=" + this.mLastValueFrontSensorTime + " BackSensorTime=" + this.mLastValueBackSensorTime);
            }
            setChanged();
            notifyObservers(dataToSend);
        }

        public int getSensorLuxData() {
            return this.mFusedLux;
        }

        public int getSensorCctData() {
            return this.mFusedCct;
        }

        public long getSensorDataTime() {
            return this.mFrontLastValueSystemTime > this.mBackLastValueSystemTime ? this.mFrontLastValueSystemTime : this.mBackLastValueSystemTime;
        }

        public void clearSensorData() {
            this.mCurFrontLux = -1;
            this.mCurFrontCct = -1;
            this.mCurBackLux = -1;
            this.mCurBackCct = -1;
            this.mFusedLux = -1;
            this.mFusedCct = -1;
            this.mLastValueFrontSensorTime = -1;
            this.mLastValueBackSensorTime = -1;
            this.mFrontLastValueSystemTime = -1;
            this.mBackLastValueSystemTime = -1;
            this.mDebugPrintTime = -1;
            this.mCurFrontFiltered = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
            this.mPreFrontFiltered = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
            this.mCurBackFiltered = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
            this.mPreBackFiltered = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
            this.mFrontLuxIndex = 0;
            this.mBackLuxIndex = 0;
            this.mFrontLuxRelativeChange = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
            this.mBackLuxRelativeChange = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
            this.mFrontLuxStaProInd = 0;
            this.mBackLuxStaProInd = 0;
            this.mAdjustMin = 1.0f;
            this.mFrontUpdated = false;
            this.mBackUpdated = false;
            this.mFrontLuxArray = new int[]{-1, -1, -1};
            this.mBackLuxArray = new int[]{-1, -1, -1};
        }

        private void setStabilityProbabilityLUT() {
            if (HwDualSensorEventListenerImpl.STABILIZED_PROBABILITY_LUT == null) {
                if (HwDualSensorEventListenerImpl.DEBUG) {
                    Slog.i(HwDualSensorEventListenerImpl.TAG, "STABILIZED_PROBABILITY_LUT is null");
                }
            } else if (HwDualSensorEventListenerImpl.STABILIZED_PROBABILITY_LUT.length != 25) {
                if (HwDualSensorEventListenerImpl.DEBUG) {
                    Slog.i(HwDualSensorEventListenerImpl.TAG, "STABILIZED_PROBABILITY_LUT.length=" + HwDualSensorEventListenerImpl.STABILIZED_PROBABILITY_LUT.length);
                }
            } else {
                for (int rowInd = 0; rowInd < 5; rowInd++) {
                    for (int colInd = 0; colInd < 5; colInd++) {
                        this.mStaProbLUT[rowInd][colInd] = HwDualSensorEventListenerImpl.STABILIZED_PROBABILITY_LUT[(rowInd * 5) + colInd];
                    }
                }
            }
        }

        private boolean isFlashlightOn() {
            boolean z = true;
            switch (HwDualSensorEventListenerImpl.mFlashlightDetectionMode) {
                case 0:
                    if (this.mCurBackFiltered <= this.mBackRoofThresh && this.mBackLuxDeviation <= HwDualSensorEventListenerImpl.BACK_LUX_DEVIATION_THRESH) {
                        z = false;
                    }
                    return z;
                case 1:
                    if (HwDualSensorEventListenerImpl.this.mFlashLightOffTimeStampMs == -1 || SystemClock.uptimeMillis() - HwDualSensorEventListenerImpl.this.mFlashLightOffTimeStampMs < ((long) HwDualSensorEventListenerImpl.FLASHLIGHT_OFF_TIME_THRESHOLD_MS)) {
                        return true;
                    }
                    if (HwDualSensorEventListenerImpl.this.mFlashLightOffTimeStampMs == -2) {
                        if (this.mCurBackFiltered <= this.mBackRoofThresh && this.mBackLuxDeviation <= HwDualSensorEventListenerImpl.BACK_LUX_DEVIATION_THRESH) {
                            z = false;
                        }
                        return z;
                    }
                    break;
            }
            return false;
        }
    }

    private interface FusedSensorDataRefresher {
        void updateData(int i, int i2, long j, long j2);
    }

    private static class SensorData extends Observable {
        private List<Integer> mCctDataList;
        private long mEnableTime;
        private int mLastSensorCctValue;
        private int mLastSensorLuxValue;
        private long mLastValueSensorTime;
        private long mLastValueSystemTime;
        private List<Integer> mLuxDataList;

        private SensorData(int rateMillis) {
            this.mLastSensorLuxValue = -1;
            this.mLastSensorCctValue = -1;
            this.mLastValueSystemTime = -1;
            this.mLastValueSensorTime = -1;
            this.mEnableTime = -1;
            this.mLuxDataList = new CopyOnWriteArrayList();
            this.mCctDataList = new CopyOnWriteArrayList();
        }

        /* access modifiers changed from: private */
        public void setSensorData(int lux, int cct, long systemClockTimeStamp, long sensorClockTimeStamp) {
            this.mLuxDataList.add(Integer.valueOf(lux));
            this.mCctDataList.add(Integer.valueOf(cct));
            this.mLastValueSystemTime = systemClockTimeStamp;
            this.mLastValueSensorTime = sensorClockTimeStamp;
        }

        /* access modifiers changed from: private */
        public void sendSensorData() {
            int count = 0;
            int sum = 0;
            for (Integer data : this.mLuxDataList) {
                sum += data.intValue();
                count++;
            }
            if (count != 0) {
                int average = sum / count;
                if (average >= 0) {
                    this.mLastSensorLuxValue = average;
                }
            }
            this.mLuxDataList.clear();
            int count2 = 0;
            int sum2 = 0;
            for (Integer data2 : this.mCctDataList) {
                sum2 += data2.intValue();
                count2++;
            }
            if (count2 != 0) {
                int average2 = sum2 / count2;
                if (average2 >= 0) {
                    this.mLastSensorCctValue = average2;
                }
            }
            this.mCctDataList.clear();
            if (this.mLastSensorLuxValue < 0 || this.mLastSensorCctValue < 0) {
                Slog.i(HwDualSensorEventListenerImpl.TAG, "SensorData.sendSensorData(): skip. mLastSensorLuxValue=" + this.mLastSensorLuxValue + " mLastSensorCctValue=" + this.mLastSensorCctValue);
                return;
            }
            long[] data3 = {(long) this.mLastSensorLuxValue, (long) this.mLastSensorCctValue, this.mLastValueSystemTime, this.mLastValueSensorTime};
            if (HwDualSensorEventListenerImpl.DEBUG && SystemClock.elapsedRealtimeNanos() - this.mEnableTime < 1000000000) {
                Slog.d(HwDualSensorEventListenerImpl.TAG, "SensorData.sendSensorData(): lux=" + this.mLastSensorLuxValue + " cct=" + this.mLastSensorCctValue + " systemTime=" + this.mLastValueSystemTime + " sensorTime=" + this.mLastValueSensorTime);
            }
            setChanged();
            notifyObservers(data3);
        }

        /* access modifiers changed from: private */
        public void setEnableTime(long timeNanos) {
            this.mEnableTime = timeNanos;
        }

        public int getSensorLuxData() {
            return this.mLastSensorLuxValue;
        }

        public int getSensorCctData() {
            return this.mLastSensorCctValue;
        }

        public long getSensorDataTime() {
            return this.mLastValueSystemTime;
        }

        /* access modifiers changed from: private */
        public void clearSensorData() {
            this.mLuxDataList.clear();
            this.mCctDataList.clear();
            this.mLastSensorLuxValue = -1;
            this.mLastSensorCctValue = -1;
            this.mLastValueSystemTime = -1;
            this.mLastValueSensorTime = -1;
            this.mEnableTime = -1;
        }
    }

    private interface SensorDataSender {
        int getRate();

        int getTimer();

        boolean needQueue();

        void sendData();

        void setTimer(int i);
    }

    private HwDualSensorEventListenerImpl(Context context, SensorManager sensorManager) {
        this.mContext = context;
        if (!parseParameters()) {
            Slog.i(TAG, "parse parameters failed!");
        }
        this.mFrontSensorData = new SensorData(mFrontRateMillis);
        this.mBackSensorData = new SensorData(mBackRateMillis);
        this.mFusedSensorData = new FusedSensorData();
        this.mFrontSender = new SensorDataSender() {
            public int getRate() {
                return HwDualSensorEventListenerImpl.mFrontRateMillis;
            }

            public int getTimer() {
                return HwDualSensorEventListenerImpl.this.mFrontTimer;
            }

            public void setTimer(int timer) {
                int unused = HwDualSensorEventListenerImpl.this.mFrontTimer = timer;
            }

            public void sendData() {
                HwDualSensorEventListenerImpl.this.mFrontSensorData.sendSensorData();
            }

            public boolean needQueue() {
                return HwDualSensorEventListenerImpl.this.mFrontEnable && HwDualSensorEventListenerImpl.this.mFrontWarmUpFlg >= 2;
            }
        };
        this.mBackSender = new SensorDataSender() {
            public int getRate() {
                return HwDualSensorEventListenerImpl.mBackRateMillis;
            }

            public int getTimer() {
                return HwDualSensorEventListenerImpl.this.mBackTimer;
            }

            public void setTimer(int timer) {
                int unused = HwDualSensorEventListenerImpl.this.mBackTimer = timer;
            }

            public void sendData() {
                HwDualSensorEventListenerImpl.this.mBackSensorData.sendSensorData();
            }

            public boolean needQueue() {
                return HwDualSensorEventListenerImpl.this.mBackEnable && HwDualSensorEventListenerImpl.this.mBackWarmUpFlg >= 2;
            }
        };
        this.mFusedSender = new SensorDataSender() {
            public int getRate() {
                return HwDualSensorEventListenerImpl.mFusedRateMillis;
            }

            public int getTimer() {
                return HwDualSensorEventListenerImpl.this.mFusedTimer;
            }

            public void setTimer(int timer) {
                int unused = HwDualSensorEventListenerImpl.this.mFusedTimer = timer;
            }

            public void sendData() {
                synchronized (HwDualSensorEventListenerImpl.this.mFusedSensorLock) {
                    HwDualSensorEventListenerImpl.this.mFusedSensorData.sendFusedData();
                }
            }

            public boolean needQueue() {
                return HwDualSensorEventListenerImpl.this.mFusedEnable && HwDualSensorEventListenerImpl.this.mFusedWarmUpFlg >= 2;
            }
        };
        this.mDualSensorProcessThread = new HandlerThread(TAG);
        this.mDualSensorProcessThread.start();
        this.mHandler = new DualSensorHandler(this.mDualSensorProcessThread.getLooper());
        this.mSensorManager = sensorManager;
        if (mFlashlightDetectionMode == 1) {
            registerBackCameraTorchCallback();
        }
    }

    public static HwDualSensorEventListenerImpl getInstance(SensorManager sensorManager, Context context) {
        if (mInstance == null) {
            synchronized (HwDualSensorEventListenerImpl.class) {
                if (mInstance == null) {
                    mInstance = new HwDualSensorEventListenerImpl(context, sensorManager);
                }
            }
        }
        return mInstance;
    }

    public void attachFrontSensorData(Observer observer) {
        if (this.mSensorManager == null) {
            Slog.e(TAG, "SensorManager is null!");
            return;
        }
        synchronized (this.mFrontSensorLock) {
            if (this.mFrontLightSensor == null) {
                this.mFrontLightSensor = this.mSensorManager.getDefaultSensor(5);
                if (DEBUG) {
                    Slog.d(TAG, "Obtain mFrontLightSensor at firt time.");
                }
            }
            enableFrontSensor();
            this.mFrontSensorData.addObserver(observer);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0042, code lost:
        return;
     */
    public void attachBackSensorData(Observer observer) {
        if (this.mSensorManager == null) {
            Slog.e(TAG, "SensorManager is null!");
            return;
        }
        synchronized (this.mBackSensorLock) {
            if (this.mBackLightSensor == null) {
                List<Sensor> LightSensorList = this.mSensorManager.getSensorList(65554);
                if (LightSensorList.size() == 1) {
                    this.mBackLightSensor = LightSensorList.get(0);
                    if (DEBUG) {
                        Slog.d(TAG, "Obtain mBackLightSensor at firt time.");
                    }
                } else if (DEBUG) {
                    Slog.d(TAG, "more than one color sensor: " + LightSensorList.size());
                }
            }
            enableBackSensor();
            this.mBackSensorData.addObserver(observer);
        }
    }

    public void attachFusedSensorData(Observer observer) {
        synchronized (this.mFusedSensorLock) {
            attachFrontSensorData(this.mFusedSensorData.mFrontSensorDataObserver);
            attachBackSensorData(this.mFusedSensorData.mBackSensorDataObserver);
            enableFusedSensor();
            this.mFusedSensorData.addObserver(observer);
        }
    }

    public void detachFrontSensorData(Observer observer) {
        if (observer != null) {
            synchronized (this.mFrontSensorLock) {
                this.mFrontSensorData.deleteObserver(observer);
                if (this.mFrontSensorData.countObservers() == 0) {
                    disableFrontSensor();
                }
            }
        }
    }

    public void detachBackSensorData(Observer observer) {
        if (observer != null) {
            synchronized (this.mBackSensorLock) {
                this.mBackSensorData.deleteObserver(observer);
                if (this.mBackSensorData.countObservers() == 0) {
                    disableBackSensor();
                }
            }
        }
    }

    public void detachFusedSensorData(Observer observer) {
        if (observer != null) {
            synchronized (this.mFusedSensorLock) {
                this.mFusedSensorData.deleteObserver(observer);
                if (this.mFusedSensorData.countObservers() == 0) {
                    disableFusedSensor();
                    detachFrontSensorData(this.mFusedSensorData.mFrontSensorDataObserver);
                    detachBackSensorData(this.mFusedSensorData.mBackSensorDataObserver);
                }
            }
        }
    }

    private void enableFrontSensor() {
        if (!this.mFrontEnable) {
            this.mFrontEnable = true;
            this.mFrontWarmUpFlg = 0;
            this.mFrontEnableTime = SystemClock.elapsedRealtimeNanos();
            this.mFrontSensorData.setEnableTime(this.mFrontEnableTime);
            this.mSensorManager.registerListener(this.mFrontSensorEventListener, this.mFrontLightSensor, mFrontRateMillis * 1000, this.mHandler);
            if (DEBUG) {
                Slog.d(TAG, "Front sensor enabled.");
            }
        }
    }

    private void enableBackSensor() {
        if (!this.mBackEnable) {
            this.mBackEnable = true;
            this.mBackWarmUpFlg = 0;
            this.mBackEnableTime = SystemClock.elapsedRealtimeNanos();
            this.mBackSensorData.setEnableTime(this.mBackEnableTime);
            this.mSensorManager.registerListener(this.mBackSensorEventListener, this.mBackLightSensor, mBackRateMillis * 1000, this.mHandler);
            if (DEBUG) {
                Slog.d(TAG, "Back sensor enabled.");
            }
        }
    }

    private void enableFusedSensor() {
        if (!this.mFusedEnable) {
            this.mFusedEnable = true;
            this.mFusedWarmUpFlg = 0;
            this.mFusedEnableTime = SystemClock.elapsedRealtimeNanos();
            this.mHandler.sendEmptyMessage(3);
            if (DEBUG) {
                Slog.d(TAG, "Fused sensor enabled.");
            }
            if (this.mBackSensorBypassCount > 0) {
                this.mBackSensorBypassCount--;
                Slog.i(TAG, "mBackSensorBypassCount-- :" + this.mBackSensorBypassCount);
            }
        }
    }

    private void disableFrontSensor() {
        if (this.mFrontEnable) {
            this.mFrontEnable = false;
            this.mSensorManager.unregisterListener(this.mFrontSensorEventListener);
            this.mHandler.removeMessages(1);
            this.mFrontSensorData.clearSensorData();
            if (DEBUG) {
                Slog.d(TAG, "Front sensor disabled.");
            }
        }
    }

    private void disableBackSensor() {
        if (this.mBackEnable) {
            this.mBackEnable = false;
            this.mSensorManager.unregisterListener(this.mBackSensorEventListener);
            this.mHandler.removeMessages(2);
            this.mBackSensorData.clearSensorData();
            if (DEBUG) {
                Slog.d(TAG, "Back sensor disabled.");
            }
        }
    }

    private void disableFusedSensor() {
        if (this.mFusedEnable) {
            this.mFusedEnable = false;
            this.mHandler.removeMessages(3);
            this.mFusedSensorData.clearSensorData();
            if (DEBUG) {
                Slog.d(TAG, "Fused sensor disabled.");
            }
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x017a  */
    public int[] convertRGBCW2LuxAndCct(Float Craw, Float Rraw, Float Graw, Float Braw, Float Wraw) {
        float Z;
        float Y;
        float X;
        float lux_f;
        if (Float.compare(ATIME_RGBCW * AGAIN_RGBCW, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO) == 0) {
            Slog.d(TAG, " ATIME_RGBCW=" + ATIME_RGBCW + " AGAIN_RGBCW=" + AGAIN_RGBCW);
            return new int[]{0, 0};
        }
        float Cnorm = ((PRODUCTION_CALIBRATION_C * 12800.0f) * Craw.floatValue()) / (ATIME_RGBCW * AGAIN_RGBCW);
        float Rnorm = ((PRODUCTION_CALIBRATION_R * 12800.0f) * Rraw.floatValue()) / (ATIME_RGBCW * AGAIN_RGBCW);
        float Gnorm = ((PRODUCTION_CALIBRATION_G * 12800.0f) * Graw.floatValue()) / (ATIME_RGBCW * AGAIN_RGBCW);
        float Bnorm = ((PRODUCTION_CALIBRATION_B * 12800.0f) * Braw.floatValue()) / (ATIME_RGBCW * AGAIN_RGBCW);
        float Wnorm = ((12800.0f * PRODUCTION_CALIBRATION_W) * Wraw.floatValue()) / (ATIME_RGBCW * AGAIN_RGBCW);
        int lux = 0;
        int cct = 0;
        if (Float.compare(Wnorm, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO) != 0) {
            if (((2.6f * Wnorm) - Cnorm) / (2.6f * Wnorm) > IR_BOUNDRY_RGBCW) {
                lux_f = (LUX_COEF_HIGH_1 * Cnorm) + (LUX_COEF_HIGH_2 * Rnorm) + (LUX_COEF_HIGH_3 * Gnorm) + (LUX_COEF_HIGH_4 * Bnorm) + (LUX_COEF_HIGH_5 * Wnorm);
                X = (COLOR_MAT_RGBCW_HIGH_11 * Cnorm) + (COLOR_MAT_RGBCW_HIGH_12 * Rnorm) + (COLOR_MAT_RGBCW_HIGH_13 * Gnorm) + (COLOR_MAT_RGBCW_HIGH_14 * Bnorm) + (COLOR_MAT_RGBCW_HIGH_15 * Wnorm);
                Y = (COLOR_MAT_RGBCW_HIGH_21 * Cnorm) + (COLOR_MAT_RGBCW_HIGH_22 * Rnorm) + (COLOR_MAT_RGBCW_HIGH_23 * Gnorm) + (COLOR_MAT_RGBCW_HIGH_24 * Bnorm) + (COLOR_MAT_RGBCW_HIGH_25 * Wnorm);
                Z = (COLOR_MAT_RGBCW_HIGH_31 * Cnorm) + (COLOR_MAT_RGBCW_HIGH_32 * Rnorm) + (COLOR_MAT_RGBCW_HIGH_33 * Gnorm) + (COLOR_MAT_RGBCW_HIGH_34 * Bnorm) + (COLOR_MAT_RGBCW_HIGH_35 * Wnorm);
            } else {
                lux_f = (LUX_COEF_LOW_1 * Cnorm) + (LUX_COEF_LOW_2 * Rnorm) + (LUX_COEF_LOW_3 * Gnorm) + (LUX_COEF_LOW_4 * Bnorm) + (LUX_COEF_LOW_5 * Wnorm);
                X = (COLOR_MAT_RGBCW_LOW_11 * Cnorm) + (COLOR_MAT_RGBCW_LOW_12 * Rnorm) + (COLOR_MAT_RGBCW_LOW_13 * Gnorm) + (COLOR_MAT_RGBCW_LOW_14 * Bnorm) + (COLOR_MAT_RGBCW_LOW_15 * Wnorm);
                Y = (COLOR_MAT_RGBCW_LOW_21 * Cnorm) + (COLOR_MAT_RGBCW_LOW_22 * Rnorm) + (COLOR_MAT_RGBCW_LOW_23 * Gnorm) + (COLOR_MAT_RGBCW_LOW_24 * Bnorm) + (COLOR_MAT_RGBCW_LOW_25 * Wnorm);
                Z = (COLOR_MAT_RGBCW_LOW_31 * Cnorm) + (COLOR_MAT_RGBCW_LOW_32 * Rnorm) + (COLOR_MAT_RGBCW_LOW_33 * Gnorm) + (COLOR_MAT_RGBCW_LOW_34 * Bnorm) + (COLOR_MAT_RGBCW_LOW_35 * Wnorm);
            }
            float XYZ = X + Y + Z;
            if (Float.compare(XYZ, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO) != 0) {
                float x = X / XYZ;
                float y = Y / XYZ;
                if (Float.compare(y - 0.1858f, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO) != 0) {
                    float tempvalue = (1.0f / (y - 0.1858f)) * (x - 0.332f);
                    float f = Cnorm;
                    cct = (int) ((((((-499.0f * tempvalue) * tempvalue) * tempvalue) + ((3525.0f * tempvalue) * tempvalue)) - (6823.3f * tempvalue)) + 5520.33f + 0.5f);
                    lux = (int) (lux_f + 0.5f);
                    if (lux < 0) {
                        lux = 0;
                    }
                }
            }
            lux = (int) (lux_f + 0.5f);
            if (lux < 0) {
            }
        }
        return new int[]{lux, cct};
    }

    /* access modifiers changed from: private */
    public int[] convertXYZ2LuxAndCct(Float Xraw, Float Yraw, Float Zraw, Float IRraw) {
        float Z;
        float Y;
        float lux_f;
        float X;
        float f;
        float Xnorm = ((PRODUCTION_CALIBRATION_X * 1600.128f) * Xraw.floatValue()) / (ATIME * AGAIN);
        float Ynorm = ((PRODUCTION_CALIBRATION_Y * 1600.128f) * Yraw.floatValue()) / (ATIME * AGAIN);
        float Znorm = ((PRODUCTION_CALIBRATION_Z * 1600.128f) * Zraw.floatValue()) / (ATIME * AGAIN);
        float IR1norm = ((1600.128f * PRODUCTION_CALIBRATION_IR1) * IRraw.floatValue()) / (ATIME * AGAIN);
        int lux = 0;
        int cct = 0;
        if (Float.compare(Ynorm, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO) != 0) {
            if (IR1norm / Ynorm > IR_BOUNDRY) {
                lux_f = (LUX_COEF_HIGH_X * Xnorm) + (LUX_COEF_HIGH_Y * Ynorm) + (LUX_COEF_HIGH_Z * Znorm) + (LUX_COEF_HIGH_IR * IR1norm) + LUX_COEF_HIGH_OFFSET;
                X = (COLOR_MAT_HIGH_11 * Xnorm) + (COLOR_MAT_HIGH_12 * Ynorm) + (COLOR_MAT_HIGH_13 * Znorm) + (COLOR_MAT_HIGH_14 * IR1norm);
                Y = (COLOR_MAT_HIGH_21 * Xnorm) + (COLOR_MAT_HIGH_22 * Ynorm) + (COLOR_MAT_HIGH_23 * Znorm) + (COLOR_MAT_HIGH_24 * IR1norm);
                Z = (COLOR_MAT_HIGH_31 * Xnorm) + (COLOR_MAT_HIGH_32 * Ynorm) + (COLOR_MAT_HIGH_33 * Znorm) + (COLOR_MAT_HIGH_34 * IR1norm);
            } else {
                lux_f = (LUX_COEF_LOW_X * Xnorm) + (LUX_COEF_LOW_Y * Ynorm) + (LUX_COEF_LOW_Z * Znorm) + (LUX_COEF_LOW_IR * IR1norm) + LUX_COEF_LOW_OFFSET;
                X = (COLOR_MAT_LOW_11 * Xnorm) + (COLOR_MAT_LOW_12 * Ynorm) + (COLOR_MAT_LOW_13 * Znorm) + (COLOR_MAT_LOW_14 * IR1norm);
                Y = (COLOR_MAT_LOW_21 * Xnorm) + (COLOR_MAT_LOW_22 * Ynorm) + (COLOR_MAT_LOW_23 * Znorm) + (COLOR_MAT_LOW_24 * IR1norm);
                Z = (COLOR_MAT_LOW_31 * Xnorm) + (COLOR_MAT_LOW_32 * Ynorm) + (COLOR_MAT_LOW_33 * Znorm) + (COLOR_MAT_LOW_34 * IR1norm);
            }
            float XYZ = X + Y + Z;
            if (Float.compare(XYZ, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO) != 0) {
                float x = X / XYZ;
                float y = Y / XYZ;
                if (Float.compare(y - 0.1858f, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO) != 0) {
                    float tempvalue = (1.0f / (y - 0.1858f)) * (x - 0.332f);
                    float f2 = Xnorm;
                    f = 0.5f;
                    cct = (int) ((((((-499.0f * tempvalue) * tempvalue) * tempvalue) + ((3525.0f * tempvalue) * tempvalue)) - (6823.3f * tempvalue)) + 5520.33f + 0.5f);
                    lux = (int) (lux_f + f);
                }
            }
            f = 0.5f;
            lux = (int) (lux_f + f);
        }
        return new int[]{lux, cct};
    }

    /* access modifiers changed from: private */
    public int[] convertRGB2LuxAndCct(float red, float green, float blue, float IR) {
        float lux_f;
        float lux_f2;
        float f = green;
        float cct_f = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        if (Float.compare(f, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO) <= 0) {
            return new int[]{0, 0};
        }
        if (Float.compare(IR / f, 0.5f) > 0) {
            lux_f = (LUX_COEF_HIGH_RED * red) + (LUX_COEF_HIGH_GREEN * f) + (LUX_COEF_HIGH_BLUE * blue);
        } else {
            lux_f = (LUX_COEF_LOW_RED * red) + (LUX_COEF_LOW_GREEN * f) + (LUX_COEF_LOW_BLUE * blue);
        }
        float rgbSum = red + f + blue;
        if (Float.compare(rgbSum, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO) <= 0) {
            if (DEBUG) {
                Slog.d(TAG, "convertRGB2LuxAndCct: Illegal sensor value, rgbSum <= 0!");
            }
            return new int[]{0, 0};
        }
        float redRatio = red / rgbSum;
        float greenRatio = f / rgbSum;
        float x = X_CONSTANT + (X_R_COEF * redRatio) + (X_R_QUADRATIC_COEF * redRatio * redRatio) + (X_G_COEF * greenRatio) + (X_RG_COEF * greenRatio * redRatio) + (X_G_QUADRATIC_COEF * greenRatio * greenRatio);
        float y = Y_CONSTANT + (Y_R_COEF * redRatio) + (Y_R_QUADRATIC_COEF * redRatio * redRatio) + (Y_G_COEF * greenRatio) + (Y_RG_COEF * redRatio * greenRatio) + (Y_G_QUADRATIC_COEF * greenRatio * greenRatio);
        if (Float.compare(y, 0.1858f) >= 0) {
            float n = (x - 0.332f) / (y - 0.1858f);
            float f2 = redRatio;
            lux_f2 = lux_f;
            float cct_f2 = (((-449.0f * ((float) Math.pow((double) n, 3.0d))) + (3525.0f * ((float) Math.pow((double) n, 2.0d)))) - (6823.3f * n)) + 5520.33f;
            float cct_f3 = 13000.0f;
            if (Float.compare(cct_f2, 13000.0f) < 0) {
                cct_f3 = cct_f2;
            }
            float cct_f4 = cct_f3;
            cct_f = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
            if (Float.compare(cct_f4, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO) > 0) {
                cct_f = cct_f4;
            }
        } else {
            lux_f2 = lux_f;
            float f3 = redRatio;
        }
        return new int[]{(int) (lux_f2 + 0.5f), (int) (cct_f + 0.5f)};
    }

    /* JADX WARNING: Removed duplicated region for block: B:30:0x006f  */
    private boolean parseParameters() {
        String str;
        StringBuilder sb;
        boolean parseResult = false;
        FileInputStream inputStream = null;
        try {
            FileInputStream inputStream2 = new FileInputStream(DUAL_SENSOR_XML_PATH);
            parseResult = parseParametersFromXML(inputStream2);
            try {
                inputStream2.close();
            } catch (IOException e) {
                e = e;
                str = TAG;
                sb = new StringBuilder();
            }
        } catch (FileNotFoundException e2) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e3) {
                    e = e3;
                    str = TAG;
                    sb = new StringBuilder();
                }
            }
        } catch (Exception e4) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e5) {
                    e = e5;
                    str = TAG;
                    sb = new StringBuilder();
                }
            }
        } catch (Throwable th) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e6) {
                    Slog.i(TAG, "input stream close uncorrectly: " + e6);
                }
            }
            throw th;
        }
        if (DEBUG) {
            Slog.d(TAG, "parseResult=" + parseResult);
        }
        printParameters();
        return parseResult;
        sb.append("input stream close uncorrectly: ");
        sb.append(e);
        Slog.i(str, sb.toString());
        if (DEBUG) {
        }
        printParameters();
        return parseResult;
    }

    private boolean parseParametersFromXML(InputStream inStream) {
        if (DEBUG) {
            Slog.d(TAG, "parse dual sensor parameters from xml: " + DUAL_SENSOR_XML_PATH);
        }
        boolean backSensorParamLoadStarted = false;
        boolean moduleSensorOptionsLoadStarted = false;
        boolean algorithmParametersLoadStarted = false;
        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(inStream, "UTF-8");
            for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                switch (eventType) {
                    case 2:
                        String nodeName = parser.getName();
                        if (!nodeName.equals("BackSensorParameters")) {
                            if (!backSensorParamLoadStarted || !nodeName.equals("IR_BOUNDRY_RGBCW")) {
                                if (!backSensorParamLoadStarted || !nodeName.equals("LuxrgbcwCoefficientsLow")) {
                                    if (!backSensorParamLoadStarted || !nodeName.equals("ColorrgbcwMatrixLow")) {
                                        if (!backSensorParamLoadStarted || !nodeName.equals("LuxrgbcwCoefficientsHigh")) {
                                            if (!backSensorParamLoadStarted || !nodeName.equals("ColorrgbcwMatrixHigh")) {
                                                if (!backSensorParamLoadStarted || !nodeName.equals("ATime_rgbcw")) {
                                                    if (!backSensorParamLoadStarted || !nodeName.equals("AGain_rgbcw")) {
                                                        if (!backSensorParamLoadStarted || !nodeName.equals("ProductionCalibrationR")) {
                                                            if (!backSensorParamLoadStarted || !nodeName.equals("ProductionCalibrationG")) {
                                                                if (!backSensorParamLoadStarted || !nodeName.equals("ProductionCalibrationB")) {
                                                                    if (!backSensorParamLoadStarted || !nodeName.equals("ProductionCalibrationC")) {
                                                                        if (!backSensorParamLoadStarted || !nodeName.equals("ProductionCalibrationW")) {
                                                                            if (!backSensorParamLoadStarted || !nodeName.equals("IRBoundry")) {
                                                                                if (!backSensorParamLoadStarted || !nodeName.equals("LuxCoefficientsLow")) {
                                                                                    if (!backSensorParamLoadStarted || !nodeName.equals("ColorMatrixLow")) {
                                                                                        if (!backSensorParamLoadStarted || !nodeName.equals("LuxCoefficientsHigh")) {
                                                                                            if (!backSensorParamLoadStarted || !nodeName.equals("ColorMatrixHigh")) {
                                                                                                if (!backSensorParamLoadStarted || !nodeName.equals("ATime")) {
                                                                                                    if (!backSensorParamLoadStarted || !nodeName.equals("AGain")) {
                                                                                                        if (!backSensorParamLoadStarted || !nodeName.equals("ProductionCalibrationX")) {
                                                                                                            if (!backSensorParamLoadStarted || !nodeName.equals("ProductionCalibrationY")) {
                                                                                                                if (!backSensorParamLoadStarted || !nodeName.equals("ProductionCalibrationZ")) {
                                                                                                                    if (!backSensorParamLoadStarted || !nodeName.equals("ProductionCalibrationIR1")) {
                                                                                                                        if (!backSensorParamLoadStarted || !nodeName.equals("FrontSensorRateMillis")) {
                                                                                                                            if (!backSensorParamLoadStarted || !nodeName.equals("BackSensorRateMillis")) {
                                                                                                                                if (!backSensorParamLoadStarted || !nodeName.equals("FusedSensorRateMillis")) {
                                                                                                                                    if (!backSensorParamLoadStarted || !nodeName.equals("SensorVersion")) {
                                                                                                                                        if (!backSensorParamLoadStarted || !nodeName.equals("RatioIRGreen")) {
                                                                                                                                            if (!backSensorParamLoadStarted || !nodeName.equals("LuxCoefHighRed")) {
                                                                                                                                                if (!backSensorParamLoadStarted || !nodeName.equals("LuxCoefHighGreen")) {
                                                                                                                                                    if (!backSensorParamLoadStarted || !nodeName.equals("LuxCoefHighBlue")) {
                                                                                                                                                        if (!backSensorParamLoadStarted || !nodeName.equals("LuxCoefLowRed")) {
                                                                                                                                                            if (!backSensorParamLoadStarted || !nodeName.equals("LuxCoefLowGreen")) {
                                                                                                                                                                if (!backSensorParamLoadStarted || !nodeName.equals("LuxCoefLowBlue")) {
                                                                                                                                                                    if (!backSensorParamLoadStarted || !nodeName.equals("XConstant")) {
                                                                                                                                                                        if (!backSensorParamLoadStarted || !nodeName.equals("XRCoef")) {
                                                                                                                                                                            if (!backSensorParamLoadStarted || !nodeName.equals("XRQuadCoef")) {
                                                                                                                                                                                if (!backSensorParamLoadStarted || !nodeName.equals("XGCoef")) {
                                                                                                                                                                                    if (!backSensorParamLoadStarted || !nodeName.equals("XRGCoef")) {
                                                                                                                                                                                        if (!backSensorParamLoadStarted || !nodeName.equals("XGQuadCoef")) {
                                                                                                                                                                                            if (!backSensorParamLoadStarted || !nodeName.equals("YConstant")) {
                                                                                                                                                                                                if (!backSensorParamLoadStarted || !nodeName.equals("YRCoef")) {
                                                                                                                                                                                                    if (!backSensorParamLoadStarted || !nodeName.equals("YRQuadCoef")) {
                                                                                                                                                                                                        if (!backSensorParamLoadStarted || !nodeName.equals("YGCoef")) {
                                                                                                                                                                                                            if (!backSensorParamLoadStarted || !nodeName.equals("YRGCoef")) {
                                                                                                                                                                                                                if (!backSensorParamLoadStarted || !nodeName.equals("YGQuadCoef")) {
                                                                                                                                                                                                                    if (!nodeName.equals("ModuleSensorOptions")) {
                                                                                                                                                                                                                        if (!moduleSensorOptionsLoadStarted) {
                                                                                                                                                                                                                            if (!nodeName.equals("AlgorithmParameters")) {
                                                                                                                                                                                                                                if (!algorithmParametersLoadStarted || !nodeName.equals("FrontSensorStabilityThresholdList")) {
                                                                                                                                                                                                                                    if (!algorithmParametersLoadStarted || !nodeName.equals("BackSensorStabilityThresholdList")) {
                                                                                                                                                                                                                                        if (!algorithmParametersLoadStarted || !nodeName.equals("StabilizedProbabilityLUT")) {
                                                                                                                                                                                                                                            if (!algorithmParametersLoadStarted || !nodeName.equals("BackRoofThresh")) {
                                                                                                                                                                                                                                                if (!algorithmParametersLoadStarted || !nodeName.equals("BackFloorThresh")) {
                                                                                                                                                                                                                                                    if (!algorithmParametersLoadStarted || !nodeName.equals("DarkRoomThresh")) {
                                                                                                                                                                                                                                                        if (!algorithmParametersLoadStarted || !nodeName.equals("DarkRoomDelta")) {
                                                                                                                                                                                                                                                            if (!algorithmParametersLoadStarted || !nodeName.equals("DarkRoomDelta1")) {
                                                                                                                                                                                                                                                                if (!algorithmParametersLoadStarted || !nodeName.equals("DarkRoomDelta2")) {
                                                                                                                                                                                                                                                                    if (!algorithmParametersLoadStarted || !nodeName.equals("DarkRoomThresh1")) {
                                                                                                                                                                                                                                                                        if (!algorithmParametersLoadStarted || !nodeName.equals("DarkRoomThresh2")) {
                                                                                                                                                                                                                                                                            if (!algorithmParametersLoadStarted || !nodeName.equals("DarkRoomThresh3")) {
                                                                                                                                                                                                                                                                                if (!algorithmParametersLoadStarted || !nodeName.equals("BackLuxDeviationThresh")) {
                                                                                                                                                                                                                                                                                    if (!algorithmParametersLoadStarted || !nodeName.equals("FlashlightDetectionMode")) {
                                                                                                                                                                                                                                                                                        if (!algorithmParametersLoadStarted || !nodeName.equals("FlashlightOffTimeThresholdMs")) {
                                                                                                                                                                                                                                                                                            if (!algorithmParametersLoadStarted || !nodeName.equals("BackSensorTimeOutTH")) {
                                                                                                                                                                                                                                                                                                if (algorithmParametersLoadStarted && nodeName.equals("BackSensorBypassCountMax")) {
                                                                                                                                                                                                                                                                                                    this.mBackSensorBypassCountMax = Integer.parseInt(parser.nextText());
                                                                                                                                                                                                                                                                                                    break;
                                                                                                                                                                                                                                                                                                }
                                                                                                                                                                                                                                                                                            } else {
                                                                                                                                                                                                                                                                                                this.mBackSensorTimeOutTH = Integer.parseInt(parser.nextText());
                                                                                                                                                                                                                                                                                                break;
                                                                                                                                                                                                                                                                                            }
                                                                                                                                                                                                                                                                                        } else {
                                                                                                                                                                                                                                                                                            FLASHLIGHT_OFF_TIME_THRESHOLD_MS = Integer.parseInt(parser.nextText());
                                                                                                                                                                                                                                                                                            break;
                                                                                                                                                                                                                                                                                        }
                                                                                                                                                                                                                                                                                    } else {
                                                                                                                                                                                                                                                                                        mFlashlightDetectionMode = Integer.parseInt(parser.nextText());
                                                                                                                                                                                                                                                                                        break;
                                                                                                                                                                                                                                                                                    }
                                                                                                                                                                                                                                                                                } else {
                                                                                                                                                                                                                                                                                    BACK_LUX_DEVIATION_THRESH = Long.parseLong(parser.nextText());
                                                                                                                                                                                                                                                                                    break;
                                                                                                                                                                                                                                                                                }
                                                                                                                                                                                                                                                                            } else {
                                                                                                                                                                                                                                                                                DARK_ROOM_THRESH3 = Float.parseFloat(parser.nextText());
                                                                                                                                                                                                                                                                                break;
                                                                                                                                                                                                                                                                            }
                                                                                                                                                                                                                                                                        } else {
                                                                                                                                                                                                                                                                            DARK_ROOM_THRESH2 = Float.parseFloat(parser.nextText());
                                                                                                                                                                                                                                                                            break;
                                                                                                                                                                                                                                                                        }
                                                                                                                                                                                                                                                                    } else {
                                                                                                                                                                                                                                                                        DARK_ROOM_THRESH1 = Float.parseFloat(parser.nextText());
                                                                                                                                                                                                                                                                        break;
                                                                                                                                                                                                                                                                    }
                                                                                                                                                                                                                                                                } else {
                                                                                                                                                                                                                                                                    DARK_ROOM_DELTA2 = Float.parseFloat(parser.nextText());
                                                                                                                                                                                                                                                                    break;
                                                                                                                                                                                                                                                                }
                                                                                                                                                                                                                                                            } else {
                                                                                                                                                                                                                                                                DARK_ROOM_DELTA1 = Float.parseFloat(parser.nextText());
                                                                                                                                                                                                                                                                break;
                                                                                                                                                                                                                                                            }
                                                                                                                                                                                                                                                        } else {
                                                                                                                                                                                                                                                            DARK_ROOM_DELTA = Float.parseFloat(parser.nextText());
                                                                                                                                                                                                                                                            break;
                                                                                                                                                                                                                                                        }
                                                                                                                                                                                                                                                    } else {
                                                                                                                                                                                                                                                        DARK_ROOM_THRESH = Float.parseFloat(parser.nextText());
                                                                                                                                                                                                                                                        break;
                                                                                                                                                                                                                                                    }
                                                                                                                                                                                                                                                } else {
                                                                                                                                                                                                                                                    BACK_FLOOR_THRESH = Float.parseFloat(parser.nextText());
                                                                                                                                                                                                                                                    break;
                                                                                                                                                                                                                                                }
                                                                                                                                                                                                                                            } else {
                                                                                                                                                                                                                                                BACK_ROOF_THRESH = Float.parseFloat(parser.nextText());
                                                                                                                                                                                                                                                break;
                                                                                                                                                                                                                                            }
                                                                                                                                                                                                                                        } else {
                                                                                                                                                                                                                                            parseStabilizedProbabilityLUT(parser.nextText());
                                                                                                                                                                                                                                            break;
                                                                                                                                                                                                                                        }
                                                                                                                                                                                                                                    } else {
                                                                                                                                                                                                                                        parseSensorStabilityThresholdList(parser.nextText(), false);
                                                                                                                                                                                                                                        break;
                                                                                                                                                                                                                                    }
                                                                                                                                                                                                                                } else {
                                                                                                                                                                                                                                    parseSensorStabilityThresholdList(parser.nextText(), true);
                                                                                                                                                                                                                                    break;
                                                                                                                                                                                                                                }
                                                                                                                                                                                                                            } else {
                                                                                                                                                                                                                                algorithmParametersLoadStarted = true;
                                                                                                                                                                                                                                break;
                                                                                                                                                                                                                            }
                                                                                                                                                                                                                        } else {
                                                                                                                                                                                                                            int sensorOption = Integer.parseInt(parser.nextText());
                                                                                                                                                                                                                            mModuleSensorMap.put(nodeName, Integer.valueOf(sensorOption));
                                                                                                                                                                                                                            break;
                                                                                                                                                                                                                        }
                                                                                                                                                                                                                    } else {
                                                                                                                                                                                                                        moduleSensorOptionsLoadStarted = true;
                                                                                                                                                                                                                        break;
                                                                                                                                                                                                                    }
                                                                                                                                                                                                                } else {
                                                                                                                                                                                                                    Y_G_QUADRATIC_COEF = Float.parseFloat(parser.nextText());
                                                                                                                                                                                                                    break;
                                                                                                                                                                                                                }
                                                                                                                                                                                                            } else {
                                                                                                                                                                                                                Y_RG_COEF = Float.parseFloat(parser.nextText());
                                                                                                                                                                                                                break;
                                                                                                                                                                                                            }
                                                                                                                                                                                                        } else {
                                                                                                                                                                                                            Y_G_COEF = Float.parseFloat(parser.nextText());
                                                                                                                                                                                                            break;
                                                                                                                                                                                                        }
                                                                                                                                                                                                    } else {
                                                                                                                                                                                                        Y_R_QUADRATIC_COEF = Float.parseFloat(parser.nextText());
                                                                                                                                                                                                        break;
                                                                                                                                                                                                    }
                                                                                                                                                                                                } else {
                                                                                                                                                                                                    Y_R_COEF = Float.parseFloat(parser.nextText());
                                                                                                                                                                                                    break;
                                                                                                                                                                                                }
                                                                                                                                                                                            } else {
                                                                                                                                                                                                Y_CONSTANT = Float.parseFloat(parser.nextText());
                                                                                                                                                                                                break;
                                                                                                                                                                                            }
                                                                                                                                                                                        } else {
                                                                                                                                                                                            X_G_QUADRATIC_COEF = Float.parseFloat(parser.nextText());
                                                                                                                                                                                            break;
                                                                                                                                                                                        }
                                                                                                                                                                                    } else {
                                                                                                                                                                                        X_RG_COEF = Float.parseFloat(parser.nextText());
                                                                                                                                                                                        break;
                                                                                                                                                                                    }
                                                                                                                                                                                } else {
                                                                                                                                                                                    X_G_COEF = Float.parseFloat(parser.nextText());
                                                                                                                                                                                    break;
                                                                                                                                                                                }
                                                                                                                                                                            } else {
                                                                                                                                                                                X_R_QUADRATIC_COEF = Float.parseFloat(parser.nextText());
                                                                                                                                                                                break;
                                                                                                                                                                            }
                                                                                                                                                                        } else {
                                                                                                                                                                            X_R_COEF = Float.parseFloat(parser.nextText());
                                                                                                                                                                            break;
                                                                                                                                                                        }
                                                                                                                                                                    } else {
                                                                                                                                                                        X_CONSTANT = Float.parseFloat(parser.nextText());
                                                                                                                                                                        break;
                                                                                                                                                                    }
                                                                                                                                                                } else {
                                                                                                                                                                    LUX_COEF_LOW_BLUE = Float.parseFloat(parser.nextText());
                                                                                                                                                                    break;
                                                                                                                                                                }
                                                                                                                                                            } else {
                                                                                                                                                                LUX_COEF_LOW_GREEN = Float.parseFloat(parser.nextText());
                                                                                                                                                                break;
                                                                                                                                                            }
                                                                                                                                                        } else {
                                                                                                                                                            LUX_COEF_LOW_RED = Float.parseFloat(parser.nextText());
                                                                                                                                                            break;
                                                                                                                                                        }
                                                                                                                                                    } else {
                                                                                                                                                        LUX_COEF_HIGH_BLUE = Float.parseFloat(parser.nextText());
                                                                                                                                                        break;
                                                                                                                                                    }
                                                                                                                                                } else {
                                                                                                                                                    LUX_COEF_HIGH_GREEN = Float.parseFloat(parser.nextText());
                                                                                                                                                    break;
                                                                                                                                                }
                                                                                                                                            } else {
                                                                                                                                                LUX_COEF_HIGH_RED = Float.parseFloat(parser.nextText());
                                                                                                                                                break;
                                                                                                                                            }
                                                                                                                                        } else {
                                                                                                                                            RATIO_IR_GREEN = Float.parseFloat(parser.nextText());
                                                                                                                                            break;
                                                                                                                                        }
                                                                                                                                    } else {
                                                                                                                                        VERSION_SENSOR = Integer.parseInt(parser.nextText());
                                                                                                                                        break;
                                                                                                                                    }
                                                                                                                                } else {
                                                                                                                                    mFusedRateMillis = Integer.parseInt(parser.nextText());
                                                                                                                                    break;
                                                                                                                                }
                                                                                                                            } else {
                                                                                                                                mBackRateMillis = Integer.parseInt(parser.nextText());
                                                                                                                                break;
                                                                                                                            }
                                                                                                                        } else {
                                                                                                                            mFrontRateMillis = Integer.parseInt(parser.nextText());
                                                                                                                            break;
                                                                                                                        }
                                                                                                                    } else {
                                                                                                                        PRODUCTION_CALIBRATION_IR1 = Float.parseFloat(parser.nextText());
                                                                                                                        break;
                                                                                                                    }
                                                                                                                } else {
                                                                                                                    PRODUCTION_CALIBRATION_Z = Float.parseFloat(parser.nextText());
                                                                                                                    break;
                                                                                                                }
                                                                                                            } else {
                                                                                                                PRODUCTION_CALIBRATION_Y = Float.parseFloat(parser.nextText());
                                                                                                                break;
                                                                                                            }
                                                                                                        } else {
                                                                                                            PRODUCTION_CALIBRATION_X = Float.parseFloat(parser.nextText());
                                                                                                            break;
                                                                                                        }
                                                                                                    } else {
                                                                                                        AGAIN = Float.parseFloat(parser.nextText());
                                                                                                        break;
                                                                                                    }
                                                                                                } else {
                                                                                                    ATIME = Float.parseFloat(parser.nextText());
                                                                                                    break;
                                                                                                }
                                                                                            } else {
                                                                                                parseColorMatrix(parser.nextText(), false);
                                                                                                break;
                                                                                            }
                                                                                        } else {
                                                                                            parseLuxCoefficients(parser.nextText(), false);
                                                                                            break;
                                                                                        }
                                                                                    } else {
                                                                                        parseColorMatrix(parser.nextText(), true);
                                                                                        break;
                                                                                    }
                                                                                } else {
                                                                                    parseLuxCoefficients(parser.nextText(), true);
                                                                                    break;
                                                                                }
                                                                            } else {
                                                                                IR_BOUNDRY = Float.parseFloat(parser.nextText());
                                                                                break;
                                                                            }
                                                                        } else {
                                                                            PRODUCTION_CALIBRATION_W = Float.parseFloat(parser.nextText());
                                                                            break;
                                                                        }
                                                                    } else {
                                                                        PRODUCTION_CALIBRATION_C = Float.parseFloat(parser.nextText());
                                                                        break;
                                                                    }
                                                                } else {
                                                                    PRODUCTION_CALIBRATION_B = Float.parseFloat(parser.nextText());
                                                                    break;
                                                                }
                                                            } else {
                                                                PRODUCTION_CALIBRATION_G = Float.parseFloat(parser.nextText());
                                                                break;
                                                            }
                                                        } else {
                                                            PRODUCTION_CALIBRATION_R = Float.parseFloat(parser.nextText());
                                                            break;
                                                        }
                                                    } else {
                                                        AGAIN_RGBCW = Float.parseFloat(parser.nextText());
                                                        break;
                                                    }
                                                } else {
                                                    ATIME_RGBCW = Float.parseFloat(parser.nextText());
                                                    break;
                                                }
                                            } else {
                                                parsergbcwColorMatrix(parser.nextText(), false);
                                                break;
                                            }
                                        } else {
                                            parsergbcwLuxCoefficients(parser.nextText(), false);
                                            break;
                                        }
                                    } else {
                                        parsergbcwColorMatrix(parser.nextText(), true);
                                        break;
                                    }
                                } else {
                                    parsergbcwLuxCoefficients(parser.nextText(), true);
                                    break;
                                }
                            } else {
                                IR_BOUNDRY_RGBCW = Float.parseFloat(parser.nextText());
                                break;
                            }
                        } else {
                            backSensorParamLoadStarted = true;
                            break;
                        }
                        break;
                    case 3:
                        String nodeName2 = parser.getName();
                        if (!nodeName2.equals("BackSensorParameters")) {
                            if (!nodeName2.equals("ModuleSensorOptions")) {
                                if (nodeName2.equals("AlgorithmParameters")) {
                                    algorithmParametersLoadStarted = false;
                                    break;
                                }
                            } else {
                                moduleSensorOptionsLoadStarted = false;
                                break;
                            }
                        } else {
                            backSensorParamLoadStarted = false;
                            break;
                        }
                        break;
                }
            }
            return true;
        } catch (XmlPullParserException e) {
            Slog.i(TAG, "XmlPullParserException: " + e);
            return false;
        } catch (IOException e2) {
            Slog.i(TAG, "IOException: " + e2);
            return false;
        } catch (NumberFormatException e3) {
            Slog.i(TAG, "NumberFormatException: " + e3);
            return false;
        } catch (RuntimeException e4) {
            Slog.i(TAG, "RuntimeException: " + e4);
            return false;
        } catch (Exception e5) {
            Slog.i(TAG, "Exception: " + e5);
            return false;
        }
    }

    private boolean parsergbcwLuxCoefficients(String nodeContent, boolean isLow) {
        if (nodeContent == null) {
            Slog.e(TAG, "nodeContent is null");
            return false;
        }
        String[] splitedContent = nodeContent.split(",", 5);
        if (splitedContent == null) {
            Slog.e(TAG, "splitedContent is null");
            return false;
        } else if (splitedContent.length != 5) {
            Slog.e(TAG, "splitedContent.length=" + splitedContent.length);
            return false;
        } else {
            if (isLow) {
                LUX_COEF_LOW_1 = Float.parseFloat(splitedContent[0]);
                LUX_COEF_LOW_2 = Float.parseFloat(splitedContent[1]);
                LUX_COEF_LOW_3 = Float.parseFloat(splitedContent[2]);
                LUX_COEF_LOW_4 = Float.parseFloat(splitedContent[3]);
                LUX_COEF_LOW_5 = Float.parseFloat(splitedContent[4]);
            } else {
                LUX_COEF_HIGH_1 = Float.parseFloat(splitedContent[0]);
                LUX_COEF_HIGH_2 = Float.parseFloat(splitedContent[1]);
                LUX_COEF_HIGH_3 = Float.parseFloat(splitedContent[2]);
                LUX_COEF_HIGH_4 = Float.parseFloat(splitedContent[3]);
                LUX_COEF_HIGH_5 = Float.parseFloat(splitedContent[4]);
            }
            return true;
        }
    }

    private boolean parsergbcwColorMatrix(String nodeContent, boolean isLow) {
        String str = nodeContent;
        if (str == null) {
            Slog.e(TAG, "nodeContent is null");
            return false;
        }
        String[] splitedContent = str.split(",", 15);
        if (splitedContent == null) {
            Slog.e(TAG, "splitedContent is null");
            return false;
        } else if (splitedContent.length != 15) {
            Slog.e(TAG, "splitedContent.length=" + splitedContent.length);
            return false;
        } else {
            if (isLow) {
                COLOR_MAT_RGBCW_LOW_11 = Float.parseFloat(splitedContent[0]);
                COLOR_MAT_RGBCW_LOW_12 = Float.parseFloat(splitedContent[1]);
                COLOR_MAT_RGBCW_LOW_13 = Float.parseFloat(splitedContent[2]);
                COLOR_MAT_RGBCW_LOW_14 = Float.parseFloat(splitedContent[3]);
                COLOR_MAT_RGBCW_LOW_15 = Float.parseFloat(splitedContent[4]);
                COLOR_MAT_RGBCW_LOW_21 = Float.parseFloat(splitedContent[5]);
                COLOR_MAT_RGBCW_LOW_22 = Float.parseFloat(splitedContent[6]);
                COLOR_MAT_RGBCW_LOW_23 = Float.parseFloat(splitedContent[7]);
                COLOR_MAT_RGBCW_LOW_24 = Float.parseFloat(splitedContent[8]);
                COLOR_MAT_RGBCW_LOW_25 = Float.parseFloat(splitedContent[9]);
                COLOR_MAT_RGBCW_LOW_31 = Float.parseFloat(splitedContent[10]);
                COLOR_MAT_RGBCW_LOW_32 = Float.parseFloat(splitedContent[11]);
                COLOR_MAT_RGBCW_LOW_33 = Float.parseFloat(splitedContent[12]);
                COLOR_MAT_RGBCW_LOW_34 = Float.parseFloat(splitedContent[13]);
                COLOR_MAT_RGBCW_LOW_35 = Float.parseFloat(splitedContent[14]);
            } else {
                COLOR_MAT_RGBCW_HIGH_11 = Float.parseFloat(splitedContent[0]);
                COLOR_MAT_RGBCW_HIGH_12 = Float.parseFloat(splitedContent[1]);
                COLOR_MAT_RGBCW_HIGH_13 = Float.parseFloat(splitedContent[2]);
                COLOR_MAT_RGBCW_HIGH_14 = Float.parseFloat(splitedContent[3]);
                COLOR_MAT_RGBCW_HIGH_15 = Float.parseFloat(splitedContent[4]);
                COLOR_MAT_RGBCW_HIGH_21 = Float.parseFloat(splitedContent[5]);
                COLOR_MAT_RGBCW_HIGH_22 = Float.parseFloat(splitedContent[6]);
                COLOR_MAT_RGBCW_HIGH_23 = Float.parseFloat(splitedContent[7]);
                COLOR_MAT_RGBCW_HIGH_24 = Float.parseFloat(splitedContent[8]);
                COLOR_MAT_RGBCW_HIGH_25 = Float.parseFloat(splitedContent[9]);
                COLOR_MAT_RGBCW_HIGH_31 = Float.parseFloat(splitedContent[10]);
                COLOR_MAT_RGBCW_HIGH_32 = Float.parseFloat(splitedContent[11]);
                COLOR_MAT_RGBCW_HIGH_33 = Float.parseFloat(splitedContent[12]);
                COLOR_MAT_RGBCW_HIGH_34 = Float.parseFloat(splitedContent[13]);
                COLOR_MAT_RGBCW_HIGH_35 = Float.parseFloat(splitedContent[14]);
            }
            return true;
        }
    }

    private boolean parseLuxCoefficients(String nodeContent, boolean isLow) {
        if (nodeContent == null) {
            return false;
        }
        String[] splitedContent = nodeContent.split(",", 5);
        if (isLow) {
            LUX_COEF_LOW_X = Float.parseFloat(splitedContent[0]);
            LUX_COEF_LOW_Y = Float.parseFloat(splitedContent[1]);
            LUX_COEF_LOW_Z = Float.parseFloat(splitedContent[2]);
            LUX_COEF_LOW_IR = Float.parseFloat(splitedContent[3]);
            LUX_COEF_LOW_OFFSET = Float.parseFloat(splitedContent[4]);
        } else {
            LUX_COEF_HIGH_X = Float.parseFloat(splitedContent[0]);
            LUX_COEF_HIGH_Y = Float.parseFloat(splitedContent[1]);
            LUX_COEF_HIGH_Z = Float.parseFloat(splitedContent[2]);
            LUX_COEF_HIGH_IR = Float.parseFloat(splitedContent[3]);
            LUX_COEF_HIGH_OFFSET = Float.parseFloat(splitedContent[4]);
        }
        return true;
    }

    private boolean parseColorMatrix(String nodeContent, boolean isLow) {
        if (nodeContent == null) {
            return false;
        }
        String[] splitedContent = nodeContent.split(",", 12);
        if (isLow) {
            COLOR_MAT_LOW_11 = Float.parseFloat(splitedContent[0]);
            COLOR_MAT_LOW_12 = Float.parseFloat(splitedContent[1]);
            COLOR_MAT_LOW_13 = Float.parseFloat(splitedContent[2]);
            COLOR_MAT_LOW_14 = Float.parseFloat(splitedContent[3]);
            COLOR_MAT_LOW_21 = Float.parseFloat(splitedContent[4]);
            COLOR_MAT_LOW_22 = Float.parseFloat(splitedContent[5]);
            COLOR_MAT_LOW_23 = Float.parseFloat(splitedContent[6]);
            COLOR_MAT_LOW_24 = Float.parseFloat(splitedContent[7]);
            COLOR_MAT_LOW_31 = Float.parseFloat(splitedContent[8]);
            COLOR_MAT_LOW_32 = Float.parseFloat(splitedContent[9]);
            COLOR_MAT_LOW_33 = Float.parseFloat(splitedContent[10]);
            COLOR_MAT_LOW_34 = Float.parseFloat(splitedContent[11]);
        } else {
            COLOR_MAT_HIGH_11 = Float.parseFloat(splitedContent[0]);
            COLOR_MAT_HIGH_12 = Float.parseFloat(splitedContent[1]);
            COLOR_MAT_HIGH_13 = Float.parseFloat(splitedContent[2]);
            COLOR_MAT_HIGH_14 = Float.parseFloat(splitedContent[3]);
            COLOR_MAT_HIGH_21 = Float.parseFloat(splitedContent[4]);
            COLOR_MAT_HIGH_22 = Float.parseFloat(splitedContent[5]);
            COLOR_MAT_HIGH_23 = Float.parseFloat(splitedContent[6]);
            COLOR_MAT_HIGH_24 = Float.parseFloat(splitedContent[7]);
            COLOR_MAT_HIGH_31 = Float.parseFloat(splitedContent[8]);
            COLOR_MAT_HIGH_32 = Float.parseFloat(splitedContent[9]);
            COLOR_MAT_HIGH_33 = Float.parseFloat(splitedContent[10]);
            COLOR_MAT_HIGH_34 = Float.parseFloat(splitedContent[11]);
        }
        return true;
    }

    public int getModuleSensorOption(String moduleName) {
        if (mModuleSensorMap == null || !mModuleSensorMap.containsKey(moduleName)) {
            Slog.i(TAG, moduleName + " getModuleSensorOption=" + -1);
            return -1;
        }
        Slog.i(TAG, moduleName + " getModuleSensorOption=" + mModuleSensorMap.get(moduleName));
        return mModuleSensorMap.get(moduleName).intValue();
    }

    private boolean parseSensorStabilityThresholdList(String nodeContent, boolean isFrontSensor) {
        if (nodeContent == null) {
            Slog.i(TAG, "parseSensorStabilityThresholdList nodeContent is null.");
            return false;
        }
        String[] splitedContent = nodeContent.split(",", 4);
        int[] parsedArray = new int[4];
        for (int index = 0; index < 4; index++) {
            parsedArray[index] = Integer.parseInt(splitedContent[index]);
        }
        if (isFrontSensor) {
            FRONT_SENSOR_STABILITY_THRESHOLD = Arrays.copyOf(parsedArray, parsedArray.length);
        } else {
            BACK_SENSOR_STABILITY_THRESHOLD = Arrays.copyOf(parsedArray, parsedArray.length);
        }
        return true;
    }

    private boolean parseStabilizedProbabilityLUT(String nodeContent) {
        if (nodeContent == null) {
            Slog.d(TAG, "parseStabilizedProbabilityLUT nodeContent is null.");
            return false;
        }
        String[] splitedContent = nodeContent.split(",", 25);
        int[] parsedArray = new int[25];
        for (int index = 0; index < 25; index++) {
            parsedArray[index] = Integer.parseInt(splitedContent[index]);
        }
        STABILIZED_PROBABILITY_LUT = Arrays.copyOf(parsedArray, parsedArray.length);
        return true;
    }

    private void printParameters() {
        if (DEBUG) {
            if (VERSION_SENSOR == 1) {
                Slog.d(TAG, "VERSION_SENSOR=" + VERSION_SENSOR + " IR_BOUNDRY=" + IR_BOUNDRY);
                Slog.d(TAG, "LUX_COEF_LOW_X=" + LUX_COEF_LOW_X + " LUX_COEF_LOW_Y=" + LUX_COEF_LOW_Y + " LUX_COEF_LOW_Z=" + LUX_COEF_LOW_Z + " LUX_COEF_LOW_IR=" + LUX_COEF_LOW_IR + " LUX_COEF_LOW_OFFSET=" + LUX_COEF_LOW_OFFSET);
                Slog.d(TAG, "LUX_COEF_HIGH_X=" + LUX_COEF_HIGH_X + " LUX_COEF_HIGH_Y=" + LUX_COEF_HIGH_Y + " LUX_COEF_HIGH_Z=" + LUX_COEF_HIGH_Z + " LUX_COEF_HIGH_IR=" + LUX_COEF_HIGH_IR + " LUX_COEF_HIGH_OFFSET=" + LUX_COEF_HIGH_OFFSET);
                Slog.d(TAG, "COLOR_MAT_LOW_11=" + COLOR_MAT_LOW_11 + " COLOR_MAT_LOW_12=" + COLOR_MAT_LOW_12 + " COLOR_MAT_LOW_13=" + COLOR_MAT_LOW_13 + " COLOR_MAT_LOW_14=" + COLOR_MAT_LOW_14 + " COLOR_MAT_LOW_21=" + COLOR_MAT_LOW_21 + " COLOR_MAT_LOW_22=" + COLOR_MAT_LOW_22 + " COLOR_MAT_LOW_23=" + COLOR_MAT_LOW_23 + " COLOR_MAT_LOW_24=" + COLOR_MAT_LOW_24 + " COLOR_MAT_LOW_31=" + COLOR_MAT_LOW_31 + " COLOR_MAT_LOW_32=" + COLOR_MAT_LOW_32 + " COLOR_MAT_LOW_33=" + COLOR_MAT_LOW_33 + " COLOR_MAT_LOW_34=" + COLOR_MAT_LOW_34);
                Slog.d(TAG, "COLOR_MAT_HIGH_11=" + COLOR_MAT_HIGH_11 + " COLOR_MAT_HIGH_12=" + COLOR_MAT_HIGH_12 + " COLOR_MAT_HIGH_13=" + COLOR_MAT_HIGH_13 + " COLOR_MAT_HIGH_14=" + COLOR_MAT_HIGH_14 + " COLOR_MAT_HIGH_21=" + COLOR_MAT_HIGH_21 + " COLOR_MAT_HIGH_22=" + COLOR_MAT_HIGH_22 + " COLOR_MAT_HIGH_23=" + COLOR_MAT_HIGH_23 + " COLOR_MAT_HIGH_24=" + COLOR_MAT_HIGH_24 + " COLOR_MAT_HIGH_31=" + COLOR_MAT_HIGH_31 + " COLOR_MAT_HIGH_32=" + COLOR_MAT_HIGH_32 + " COLOR_MAT_HIGH_33=" + COLOR_MAT_HIGH_33 + " COLOR_MAT_HIGH_34=" + COLOR_MAT_HIGH_34);
                StringBuilder sb = new StringBuilder();
                sb.append("ATime=");
                sb.append(ATIME);
                sb.append(" AGain=");
                sb.append(AGAIN);
                Slog.d(TAG, sb.toString());
                StringBuilder sb2 = new StringBuilder();
                sb2.append("ProductionCalibration:");
                sb2.append(PRODUCTION_CALIBRATION_X);
                sb2.append(" ");
                sb2.append(PRODUCTION_CALIBRATION_Y);
                sb2.append(" ");
                sb2.append(PRODUCTION_CALIBRATION_Z);
                sb2.append(" ");
                sb2.append(PRODUCTION_CALIBRATION_IR1);
                Slog.d(TAG, sb2.toString());
            } else if (VERSION_SENSOR == 2) {
                Slog.d(TAG, "VERSION_SENSOR=" + VERSION_SENSOR + " RATIO_IR_GREEN=" + RATIO_IR_GREEN);
                Slog.d(TAG, "LUX_COEF_HIGH_RED=" + LUX_COEF_HIGH_RED + " LUX_COEF_HIGH_GREEN=" + LUX_COEF_HIGH_GREEN + " LUX_COEF_HIGH_BLUE=" + LUX_COEF_HIGH_BLUE);
                Slog.d(TAG, "LUX_COEF_LOW_RED=" + LUX_COEF_LOW_RED + " LUX_COEF_LOW_GREEN=" + LUX_COEF_LOW_GREEN + " LUX_COEF_LOW_BLUE=" + LUX_COEF_LOW_BLUE);
                Slog.d(TAG, "X_CONSTANT=" + X_CONSTANT + " X_R_COEF=" + X_R_COEF + " X_R_QUADRATIC_COEF=" + X_R_QUADRATIC_COEF + " X_G_COEF=" + X_G_COEF + " X_RG_COEF=" + X_RG_COEF + " X_G_QUADRATIC_COEF=" + X_G_QUADRATIC_COEF);
                Slog.d(TAG, "Y_CONSTANT=" + Y_CONSTANT + " Y_R_COEF=" + Y_R_COEF + " Y_R_QUADRATIC_COEF=" + Y_R_QUADRATIC_COEF + " Y_G_COEF=" + Y_G_COEF + " Y_RG_COEF=" + Y_RG_COEF + " Y_G_QUADRATIC_COEF=" + Y_G_QUADRATIC_COEF);
            } else if (VERSION_SENSOR == 3) {
                Slog.d(TAG, "VERSION_SENSOR=" + VERSION_SENSOR + " IR_BOUNDRY_RGBCW=" + IR_BOUNDRY_RGBCW);
                Slog.d(TAG, "LOW_1=" + LUX_COEF_LOW_1 + " LOW_2=" + LUX_COEF_LOW_2 + " LOW_3=" + LUX_COEF_LOW_3 + " LOW_4=" + LUX_COEF_LOW_4 + " LOW_5=" + LUX_COEF_LOW_5);
                Slog.d(TAG, "HIGH_1=" + LUX_COEF_HIGH_1 + " HIGH_2=" + LUX_COEF_HIGH_2 + " HIGH_3=" + LUX_COEF_HIGH_3 + " HIGH_4=" + LUX_COEF_HIGH_4 + " HIGH_5=" + LUX_COEF_HIGH_5);
                Slog.d(TAG, "LOW_11=" + COLOR_MAT_RGBCW_LOW_11 + " LOW_12=" + COLOR_MAT_RGBCW_LOW_12 + " LOW_13=" + COLOR_MAT_RGBCW_LOW_13 + " LOW_14=" + COLOR_MAT_RGBCW_LOW_14 + " LOW_15=" + COLOR_MAT_RGBCW_LOW_15 + " LOW_21=" + COLOR_MAT_RGBCW_LOW_21 + " LOW_22=" + COLOR_MAT_RGBCW_LOW_22 + " LOW_23=" + COLOR_MAT_RGBCW_LOW_23 + " LOW_24=" + COLOR_MAT_RGBCW_LOW_24 + " LOW_25=" + COLOR_MAT_RGBCW_LOW_25 + " LOW_31=" + COLOR_MAT_RGBCW_LOW_31 + " LOW_32=" + COLOR_MAT_RGBCW_LOW_32 + " LOW_33=" + COLOR_MAT_RGBCW_LOW_33 + " LOW_34=" + COLOR_MAT_RGBCW_LOW_34 + " LOW_35=" + COLOR_MAT_RGBCW_LOW_35);
                Slog.d(TAG, "HIGH_11=" + COLOR_MAT_RGBCW_HIGH_11 + " HIGH_12=" + COLOR_MAT_RGBCW_HIGH_12 + " HIGH_13=" + COLOR_MAT_RGBCW_HIGH_13 + " HIGH_14=" + COLOR_MAT_RGBCW_HIGH_14 + " HIGH_15=" + COLOR_MAT_RGBCW_HIGH_15 + " HIGH_21=" + COLOR_MAT_RGBCW_HIGH_21 + " HIGH_22=" + COLOR_MAT_RGBCW_HIGH_22 + " HIGH_23=" + COLOR_MAT_RGBCW_HIGH_23 + " HIGH_24=" + COLOR_MAT_RGBCW_HIGH_24 + " HIGH_25=" + COLOR_MAT_RGBCW_HIGH_25 + " HIGH_31=" + COLOR_MAT_RGBCW_HIGH_31 + " HIGH_32=" + COLOR_MAT_RGBCW_HIGH_32 + " HIGH_33=" + COLOR_MAT_RGBCW_HIGH_33 + " HIGH_34=" + COLOR_MAT_RGBCW_HIGH_34 + " HIGH_35=" + COLOR_MAT_RGBCW_HIGH_35);
                StringBuilder sb3 = new StringBuilder();
                sb3.append("ATIME_RGBCW=");
                sb3.append(ATIME_RGBCW);
                sb3.append(" AGAIN_RGBCW=");
                sb3.append(AGAIN_RGBCW);
                Slog.d(TAG, sb3.toString());
                StringBuilder sb4 = new StringBuilder();
                sb4.append("ProductionCalibration:");
                sb4.append(PRODUCTION_CALIBRATION_C);
                sb4.append(" ");
                sb4.append(PRODUCTION_CALIBRATION_R);
                sb4.append(" ");
                sb4.append(PRODUCTION_CALIBRATION_G);
                sb4.append(" ");
                sb4.append(PRODUCTION_CALIBRATION_B);
                sb4.append(" ");
                sb4.append(PRODUCTION_CALIBRATION_W);
                Slog.d(TAG, sb4.toString());
            }
            Slog.d(TAG, "mFrontRateMillis=" + mFrontRateMillis + " mBackRateMillis=" + mBackRateMillis + " mFusedRateMillis=" + mFusedRateMillis);
            StringBuilder sb5 = new StringBuilder();
            sb5.append("FRONT_SENSOR_STABILITY_THRESHOLD:");
            sb5.append(Arrays.toString(FRONT_SENSOR_STABILITY_THRESHOLD));
            Slog.d(TAG, sb5.toString());
            Slog.d(TAG, "BACK_SENSOR_STABILITY_THRESHOLD:" + Arrays.toString(BACK_SENSOR_STABILITY_THRESHOLD));
            Slog.d(TAG, "STABILIZED_PROBABILITY_LUT:" + Arrays.toString(STABILIZED_PROBABILITY_LUT));
            Slog.d(TAG, "BACK_ROOF_THRESH=" + BACK_ROOF_THRESH + " BACK_FLOOR_THRESH=" + BACK_FLOOR_THRESH + " DARK_ROOM_THRESH=" + DARK_ROOM_THRESH + " DARK_ROOM_DELTA=" + DARK_ROOM_DELTA);
            Slog.d(TAG, "DARK_ROOM_THRESH1=" + DARK_ROOM_THRESH1 + " DARK_ROOM_THRESH2=" + DARK_ROOM_THRESH2 + " DARK_ROOM_THRESH3=" + DARK_ROOM_THRESH3 + " DARK_ROOM_DELTA1=" + DARK_ROOM_DELTA1 + " DARK_ROOM_DELTA2=" + DARK_ROOM_DELTA2);
            StringBuilder sb6 = new StringBuilder();
            sb6.append("mFlashlightDetectionMode=");
            sb6.append(mFlashlightDetectionMode);
            sb6.append("FLASHLIGHT_OFF_TIME_THRESHOLD_MS=");
            sb6.append(FLASHLIGHT_OFF_TIME_THRESHOLD_MS);
            Slog.d(TAG, sb6.toString());
            Slog.d(TAG, "mBackSensorTimeOutTH=" + this.mBackSensorTimeOutTH + "mBackSensorBypassCountMax=" + this.mBackSensorBypassCountMax);
        }
    }

    private void registerBackCameraTorchCallback() {
        try {
            this.mCameraManager = (CameraManager) this.mContext.getSystemService("camera");
            if (this.mCameraManager == null) {
                Slog.w(TAG, "mCameraManager is null");
                mFlashlightDetectionMode = 0;
                return;
            }
            Slog.i(TAG, "mCameraManager successfully obtained");
            for (String id : this.mCameraManager.getCameraIdList()) {
                CameraCharacteristics c = this.mCameraManager.getCameraCharacteristics(id);
                Boolean flashAvailable = (Boolean) c.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                Integer lensFacing = (Integer) c.get(CameraCharacteristics.LENS_FACING);
                if (flashAvailable != null && flashAvailable.booleanValue() && lensFacing != null && lensFacing.intValue() == 1) {
                    this.mBackCameraId = id;
                }
            }
            if (this.mBackCameraId != null) {
                try {
                    this.mCameraManager.registerTorchCallback(this.mTorchCallback, null);
                } catch (IllegalArgumentException e) {
                    mFlashlightDetectionMode = 0;
                    Slog.w(TAG, "IllegalArgumentException " + e);
                }
            } else {
                Slog.w(TAG, "mBackCameraId is null");
                mFlashlightDetectionMode = 0;
            }
        } catch (CameraAccessException e2) {
            mFlashlightDetectionMode = 0;
            Slog.w(TAG, "CameraAccessException " + e2);
        }
    }
}
