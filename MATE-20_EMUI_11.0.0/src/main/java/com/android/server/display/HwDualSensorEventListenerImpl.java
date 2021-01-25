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
import android.os.SystemProperties;
import android.util.Log;
import android.util.Slog;
import com.android.server.display.HwDualSensorData;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class HwDualSensorEventListenerImpl {
    private static final int DATA_BUFFER_SIZE = 3;
    private static final boolean DEBUG = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final long DEBUG_PRINT_INTERVAL = 1000000000;
    private static final int DEFAULT_INT_VALUE = -1;
    private static final int DEFAULT_LONG_VALUE = -1;
    private static final String DUAL_SENSOR_XML_PATH = "/xml/lcd/DualSensorConfig.xml";
    private static final boolean IS_HWDUAL_SENSOR_THREAD_DISABLE = SystemProperties.getBoolean("ro.config.hwdualsensorlistenthread.disable", false);
    public static final int LIGHT_SENSOR_BACK = 1;
    public static final int LIGHT_SENSOR_DEFAULT = -1;
    public static final int LIGHT_SENSOR_DUAL = 2;
    public static final int LIGHT_SENSOR_FRONT = 0;
    private static final float MODIFY_FACTOR_OTHER = 0.002f;
    private static final float MODIFY_FACTOR_THRESHOLD_1 = 0.02f;
    private static final float MODIFY_FACTOR_THRESHOLD_2 = 0.01f;
    private static final float MODIFY_FACTOR_THRESHOLD_3 = 0.004f;
    private static final int PERCENTAGE_TH_0 = 0;
    private static final int PERCENTAGE_TH_1 = 20;
    private static final int PERCENTAGE_TH_2 = 50;
    private static final int PERCENTAGE_TH_3 = 90;
    private static final int PERCENTAGE_TH_4 = 95;
    private static final float ROUND_VALUE = 0.5f;
    private static final int STABILITY_LUT_SIZE = 5;
    private static final String TAG = "HwDualSensorEventListenerImpl";
    private static volatile HwDualSensorEventListenerImpl sInstance;
    private static final Object sInstanceLock = new Object();
    private String mBackCameraId;
    private long mBackEnableTime = 0;
    private Sensor mBackLightSensor;
    private SensorDataSender mBackSender;
    private SensorData mBackSensorData;
    private final SensorEventListener mBackSensorEventListener = new SensorEventListener() {
        /* class com.android.server.display.HwDualSensorEventListenerImpl.AnonymousClass3 */

        @Override // android.hardware.SensorEventListener
        public void onSensorChanged(SensorEvent event) {
            int[] arr;
            if (event != null) {
                int length = event.values.length;
                HwDualSensorData unused = HwDualSensorEventListenerImpl.this.mData;
                if (length >= 15) {
                    if (!HwDualSensorEventListenerImpl.this.mIsBackEnable) {
                        Slog.i(HwDualSensorEventListenerImpl.TAG, "SensorEventListener.onSensorChanged(): mIsBackEnable=false");
                        return;
                    }
                    long sensorClockTimeStamp = event.timestamp;
                    long systemClockTimeStamp = SystemClock.uptimeMillis();
                    int[] arr2 = {0, 0};
                    int i = HwDualSensorEventListenerImpl.this.mData.sensorVersion;
                    HwDualSensorData unused2 = HwDualSensorEventListenerImpl.this.mData;
                    if (i == 1) {
                        arr = HwDualSensorUtils.convertXyzToLuxAndCct(event, HwDualSensorEventListenerImpl.this.mData);
                    } else {
                        int i2 = HwDualSensorEventListenerImpl.this.mData.sensorVersion;
                        HwDualSensorData unused3 = HwDualSensorEventListenerImpl.this.mData;
                        if (i2 == 2) {
                            arr = HwDualSensorUtils.convertRgbToLuxAndCct(event, HwDualSensorEventListenerImpl.this.mData);
                        } else {
                            int i3 = HwDualSensorEventListenerImpl.this.mData.sensorVersion;
                            HwDualSensorData unused4 = HwDualSensorEventListenerImpl.this.mData;
                            if (i3 == 3) {
                                arr = HwDualSensorUtils.convertRgbcw2LuxAndCct(event, HwDualSensorEventListenerImpl.this.mData);
                            } else {
                                int i4 = HwDualSensorEventListenerImpl.this.mData.sensorVersion;
                                HwDualSensorData unused5 = HwDualSensorEventListenerImpl.this.mData;
                                if (i4 == 4) {
                                    int length2 = event.values.length;
                                    HwDualSensorData unused6 = HwDualSensorEventListenerImpl.this.mData;
                                    if (length2 >= 11) {
                                        float[] fArr = event.values;
                                        HwDualSensorData unused7 = HwDualSensorEventListenerImpl.this.mData;
                                        float f = fArr[6];
                                        float[] fArr2 = event.values;
                                        HwDualSensorData unused8 = HwDualSensorEventListenerImpl.this.mData;
                                        arr = HwDualSensorUtils.convertNormalizedTypeToLuxAndCct(f, fArr2[10], HwDualSensorEventListenerImpl.this.mData);
                                    }
                                } else {
                                    int i5 = HwDualSensorEventListenerImpl.this.mData.sensorVersion;
                                    HwDualSensorData unused9 = HwDualSensorEventListenerImpl.this.mData;
                                    if (i5 == 12) {
                                        arr = HwDualSensorUtils.convertSpectrumToLux(event, HwDualSensorEventListenerImpl.this.mData);
                                    } else {
                                        int i6 = HwDualSensorEventListenerImpl.this.mData.sensorVersion;
                                        HwDualSensorData unused10 = HwDualSensorEventListenerImpl.this.mData;
                                        if (i6 == 13) {
                                            arr = HwDualSensorUtils.convertOutwardSensorToLux(event, HwDualSensorEventListenerImpl.this.mData);
                                        } else {
                                            Slog.i(HwDualSensorEventListenerImpl.TAG, "Invalid mData.sensorVersion=" + HwDualSensorEventListenerImpl.this.mData.sensorVersion);
                                        }
                                    }
                                }
                                arr = arr2;
                            }
                        }
                    }
                    int lux = arr[0];
                    int cct = arr[1];
                    int i7 = HwDualSensorEventListenerImpl.this.mBackWarmUpFlg;
                    HwDualSensorData unused11 = HwDualSensorEventListenerImpl.this.mData;
                    if (i7 >= 2) {
                        HwDualSensorEventListenerImpl.this.mBackSensorData.setSensorData(lux, cct, systemClockTimeStamp, sensorClockTimeStamp);
                    } else if (sensorClockTimeStamp >= HwDualSensorEventListenerImpl.this.mBackEnableTime) {
                        HwDualSensorEventListenerImpl.this.mBackSensorData.setSensorData(lux, cct, systemClockTimeStamp, sensorClockTimeStamp);
                        HwDualSensorEventListenerImpl.this.mBackWarmUpFlg++;
                        if (HwDualSensorEventListenerImpl.this.mHandler != null) {
                            Handler handler = HwDualSensorEventListenerImpl.this.mHandler;
                            HwDualSensorData unused12 = HwDualSensorEventListenerImpl.this.mData;
                            handler.removeMessages(2);
                            Handler handler2 = HwDualSensorEventListenerImpl.this.mHandler;
                            HwDualSensorData unused13 = HwDualSensorEventListenerImpl.this.mData;
                            handler2.sendEmptyMessage(2);
                        }
                    } else if (HwDualSensorEventListenerImpl.DEBUG) {
                        Slog.d(HwDualSensorEventListenerImpl.TAG, "SensorEventListener.onSensorChanged(): Back sensor not ready yet!");
                        return;
                    } else {
                        return;
                    }
                    if (HwDualSensorEventListenerImpl.DEBUG && sensorClockTimeStamp - HwDualSensorEventListenerImpl.this.mBackEnableTime < 1000000000) {
                        printLuxAndCct(lux, cct, sensorClockTimeStamp, systemClockTimeStamp, event);
                        return;
                    }
                    return;
                }
            }
            Slog.e(HwDualSensorEventListenerImpl.TAG, "back SensorEvent is invalid!");
        }

        @Override // android.hardware.SensorEventListener
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        private void printLuxAndCct(int lux, int cct, long sensorClockTimeStamp, long systemClockTimeStamp, SensorEvent event) {
            int i = HwDualSensorEventListenerImpl.this.mData.sensorVersion;
            HwDualSensorData unused = HwDualSensorEventListenerImpl.this.mData;
            if (i == 1) {
                Slog.d(HwDualSensorEventListenerImpl.TAG, "SensorEventListener.onSensorChanged(): warmUp=" + HwDualSensorEventListenerImpl.this.mBackWarmUpFlg + " lux=" + lux + " cct=" + cct + " sensorTime=" + sensorClockTimeStamp + " systemTime=" + systemClockTimeStamp + " xRaw=" + event.values[0] + " yRaw=" + event.values[1] + " zRaw=" + event.values[2] + " irRaw=" + event.values[3]);
                return;
            }
            int i2 = HwDualSensorEventListenerImpl.this.mData.sensorVersion;
            HwDualSensorData unused2 = HwDualSensorEventListenerImpl.this.mData;
            if (i2 == 2) {
                Slog.d(HwDualSensorEventListenerImpl.TAG, "SensorEventListener.onSensorChanged(): warmUp=" + HwDualSensorEventListenerImpl.this.mBackWarmUpFlg + " lux=" + lux + " cct=" + cct + " sensorTime=" + sensorClockTimeStamp + " systemTime=" + systemClockTimeStamp + " red=" + event.values[0] + " green=" + event.values[1] + " blue=" + event.values[2] + " iR=" + event.values[3]);
                return;
            }
            int i3 = HwDualSensorEventListenerImpl.this.mData.sensorVersion;
            HwDualSensorData unused3 = HwDualSensorEventListenerImpl.this.mData;
            if (i3 == 3) {
                Slog.d(HwDualSensorEventListenerImpl.TAG, "SensorEventListener.onSensorChanged(): warmUp=" + HwDualSensorEventListenerImpl.this.mBackWarmUpFlg + " lux=" + lux + " cct=" + cct + " sensorTime=" + sensorClockTimeStamp + " systemTime=" + systemClockTimeStamp + " cRaw=" + event.values[0] + " rRaw=" + event.values[1] + " gRaw=" + event.values[2] + " bRaw=" + event.values[3] + " wRaw=" + event.values[4]);
                return;
            }
            int i4 = HwDualSensorEventListenerImpl.this.mData.sensorVersion;
            HwDualSensorData unused4 = HwDualSensorEventListenerImpl.this.mData;
            if (i4 == 4) {
                int length = event.values.length;
                HwDualSensorData unused5 = HwDualSensorEventListenerImpl.this.mData;
                if (length >= 11) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("SensorEventListener.onSensorChanged(): warmUp=");
                    sb.append(HwDualSensorEventListenerImpl.this.mBackWarmUpFlg);
                    sb.append(" lux=");
                    sb.append(lux);
                    sb.append(" cct=");
                    sb.append(cct);
                    sb.append(" sensorTime=");
                    sb.append(sensorClockTimeStamp);
                    sb.append(" systemTime=");
                    sb.append(systemClockTimeStamp);
                    sb.append(" luxRaw=");
                    float[] fArr = event.values;
                    HwDualSensorData unused6 = HwDualSensorEventListenerImpl.this.mData;
                    sb.append(fArr[6]);
                    sb.append(" cctRaw=");
                    float[] fArr2 = event.values;
                    HwDualSensorData unused7 = HwDualSensorEventListenerImpl.this.mData;
                    sb.append(fArr2[10]);
                    Slog.d(HwDualSensorEventListenerImpl.TAG, sb.toString());
                    return;
                }
                return;
            }
            Slog.d(HwDualSensorEventListenerImpl.TAG, "SensorEventListener.onSensorChanged(): warmUp=" + HwDualSensorEventListenerImpl.this.mBackWarmUpFlg + " lux=" + lux + " cct=" + cct + " sensorTime=" + sensorClockTimeStamp + " systemTime=" + systemClockTimeStamp);
        }
    };
    private final Object mBackSensorLock = new Object();
    private int mBackTimer = -1;
    private int mBackWarmUpFlg = 0;
    private CameraManager mCameraManager;
    private final Context mContext;
    private HwDualSensorData mData;
    private HandlerThread mDualSensorProcessThread;
    private volatile HwDualSensorData.FlashlightMode mFlashlightMode = HwDualSensorData.FlashlightMode.OFF;
    private volatile long mFlashlightOffTimeStampMs = 0;
    private long mFrontEnableTime = 0;
    private Sensor mFrontLightSensor;
    private SensorDataSender mFrontSender;
    private SensorData mFrontSensorData;
    private final SensorEventListener mFrontSensorEventListener = new SensorEventListener() {
        /* class com.android.server.display.HwDualSensorEventListenerImpl.AnonymousClass2 */

        @Override // android.hardware.SensorEventListener
        public void onSensorChanged(SensorEvent event) {
            long sensorClockTimeStamp;
            if (event != null) {
                int length = event.values.length;
                HwDualSensorData unused = HwDualSensorEventListenerImpl.this.mData;
                if (length >= 2) {
                    if (!HwDualSensorEventListenerImpl.this.mIsFrontEnable) {
                        Slog.i(HwDualSensorEventListenerImpl.TAG, "SensorEventListener.onSensorChanged(): mIsFrontEnable=false");
                        return;
                    }
                    long systemClockTimeStamp = SystemClock.uptimeMillis();
                    int lux = (int) (event.values[0] + 0.5f);
                    int cct = (int) (event.values[1] + 0.5f);
                    long sensorClockTimeStamp2 = event.timestamp;
                    int i = HwDualSensorEventListenerImpl.this.mFrontWarmUpFlg;
                    HwDualSensorData unused2 = HwDualSensorEventListenerImpl.this.mData;
                    if (i >= 2) {
                        sensorClockTimeStamp = sensorClockTimeStamp2;
                        HwDualSensorEventListenerImpl.this.mFrontSensorData.setSensorData(lux, cct, systemClockTimeStamp, sensorClockTimeStamp2);
                    } else if (sensorClockTimeStamp2 >= HwDualSensorEventListenerImpl.this.mFrontEnableTime) {
                        sensorClockTimeStamp = sensorClockTimeStamp2;
                        HwDualSensorEventListenerImpl.this.mFrontSensorData.setSensorData(lux, cct, systemClockTimeStamp, sensorClockTimeStamp2);
                        HwDualSensorEventListenerImpl.this.mFrontWarmUpFlg++;
                        if (HwDualSensorEventListenerImpl.this.mHandler != null) {
                            Handler handler = HwDualSensorEventListenerImpl.this.mHandler;
                            HwDualSensorData unused3 = HwDualSensorEventListenerImpl.this.mData;
                            handler.removeMessages(1);
                            Handler handler2 = HwDualSensorEventListenerImpl.this.mHandler;
                            HwDualSensorData unused4 = HwDualSensorEventListenerImpl.this.mData;
                            handler2.sendEmptyMessage(1);
                        }
                    } else if (HwDualSensorEventListenerImpl.DEBUG) {
                        Slog.d(HwDualSensorEventListenerImpl.TAG, "SensorEventListener.onSensorChanged(): Front sensor not ready yet!");
                        return;
                    } else {
                        return;
                    }
                    if (!HwDualSensorEventListenerImpl.DEBUG) {
                        return;
                    }
                    if (sensorClockTimeStamp - HwDualSensorEventListenerImpl.this.mFrontEnableTime < 1000000000) {
                        Slog.d(HwDualSensorEventListenerImpl.TAG, "SensorEventListener.onSensorChanged(): warmUp=" + HwDualSensorEventListenerImpl.this.mFrontWarmUpFlg + " lux=" + lux + " cct=" + cct + " sensorTime=" + sensorClockTimeStamp + " systemTime=" + systemClockTimeStamp);
                        return;
                    }
                    return;
                }
            }
            Slog.e(HwDualSensorEventListenerImpl.TAG, "front SensorEvent is invalid!");
        }

        @Override // android.hardware.SensorEventListener
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    private final Object mFrontSensorLock = new Object();
    private int mFrontTimer = -1;
    private int mFrontWarmUpFlg = 0;
    private long mFusedEnableTime = 0;
    private SensorDataSender mFusedSender;
    private FusedSensorData mFusedSensorData;
    private final Object mFusedSensorLock = new Object();
    private int mFusedTimer = -1;
    private int mFusedWarmUpFlg = 0;
    private Handler mHandler;
    private boolean mIsBackEnable = false;
    private boolean mIsFrontEnable = false;
    private boolean mIsFusedEnable = false;
    private ThreadPoolExecutor mPool = new ThreadPoolExecutor(2, 5, 60, TimeUnit.SECONDS, new LinkedBlockingQueue(10), new DeThreadFactory(), new ThreadPoolExecutor.DiscardOldestPolicy());
    private SensorManager mSensorManager;
    private int mTimer = -1;
    private CameraManager.TorchCallback mTorchCallback = new CameraManager.TorchCallback() {
        /* class com.android.server.display.HwDualSensorEventListenerImpl.AnonymousClass1 */

        @Override // android.hardware.camera2.CameraManager.TorchCallback
        public void onTorchModeUnavailable(String cameraId) {
            super.onTorchModeUnavailable(cameraId);
            HwDualSensorEventListenerImpl.this.mFlashlightOffTimeStampMs = -1;
            HwDualSensorEventListenerImpl.this.mFlashlightMode = HwDualSensorData.FlashlightMode.CAMERA_MODE;
            Slog.i(HwDualSensorEventListenerImpl.TAG, "onTorchModeUnavailable, mFlashlightMode = HwDualSensorData.FlashlightMode.CAMERA_MODE");
        }

        @Override // android.hardware.camera2.CameraManager.TorchCallback
        public void onTorchModeChanged(String cameraId, boolean enabled) {
            super.onTorchModeChanged(cameraId, enabled);
            if (cameraId == null) {
                Slog.e(HwDualSensorEventListenerImpl.TAG, "cameraId is null!");
            } else if (!cameraId.equals(HwDualSensorEventListenerImpl.this.mBackCameraId)) {
            } else {
                if (enabled) {
                    HwDualSensorEventListenerImpl.this.mFlashlightMode = HwDualSensorData.FlashlightMode.TORCH_MODE;
                    HwDualSensorEventListenerImpl.this.mFlashlightOffTimeStampMs = -1;
                    Slog.i(HwDualSensorEventListenerImpl.TAG, "mFlashlightMode = HwDualSensorData.FlashlightMode.TORCH_MODE;");
                    return;
                }
                HwDualSensorEventListenerImpl.this.mFlashlightMode = HwDualSensorData.FlashlightMode.OFF;
                HwDualSensorEventListenerImpl.this.mFlashlightOffTimeStampMs = SystemClock.uptimeMillis();
                Slog.i(HwDualSensorEventListenerImpl.TAG, "mFlashlightOffTimeStampMs set " + HwDualSensorEventListenerImpl.this.mFlashlightOffTimeStampMs);
            }
        }
    };

    private interface FusedSensorDataRefresher {
        void updateData(int i, int i2, long j, long j2);
    }

    /* access modifiers changed from: private */
    public interface SensorDataSender {
        int getRate();

        int getTimer();

        boolean needQueue();

        void sendData();

        void setTimer(int i);
    }

    static /* synthetic */ int access$2708(HwDualSensorEventListenerImpl x0) {
        int i = x0.mFusedWarmUpFlg;
        x0.mFusedWarmUpFlg = i + 1;
        return i;
    }

    private class DeThreadFactory implements ThreadFactory {
        private static final String NAME = "DisplayEngine_DualSensor";
        private int mCounter;

        private DeThreadFactory() {
        }

        @Override // java.util.concurrent.ThreadFactory
        public Thread newThread(Runnable runnable) {
            this.mCounter++;
            return new Thread(runnable, "DisplayEngine_DualSensor_Thread_" + this.mCounter);
        }
    }

    private HwDualSensorEventListenerImpl(Context context, SensorManager sensorManager) {
        this.mContext = context;
        this.mData = HwDualSensorXmlLoader.getData();
        this.mFrontSensorData = new SensorData(this.mData.frontRateMillis);
        this.mBackSensorData = new SensorData(this.mData.backRateMillis);
        this.mFusedSensorData = new FusedSensorData();
        this.mFrontSender = new FrontSensorDataSender();
        this.mBackSender = new BackSensorDataSender();
        this.mFusedSender = new FusedSensorDataSender();
        if (IS_HWDUAL_SENSOR_THREAD_DISABLE) {
            Slog.w(TAG, "HwDualSensorEventListenerImpl thread is disabled.");
        } else {
            this.mDualSensorProcessThread = new HandlerThread(TAG);
            this.mDualSensorProcessThread.start();
            this.mHandler = new DualSensorHandler(this.mDualSensorProcessThread.getLooper());
        }
        this.mSensorManager = sensorManager;
        int i = this.mData.flashlightDetectionMode;
        HwDualSensorData hwDualSensorData = this.mData;
        if (i == 1) {
            registerBackCameraTorchCallback();
        }
    }

    private class FrontSensorDataSender implements SensorDataSender {
        private FrontSensorDataSender() {
        }

        @Override // com.android.server.display.HwDualSensorEventListenerImpl.SensorDataSender
        public int getRate() {
            return HwDualSensorEventListenerImpl.this.mData.frontRateMillis;
        }

        @Override // com.android.server.display.HwDualSensorEventListenerImpl.SensorDataSender
        public int getTimer() {
            return HwDualSensorEventListenerImpl.this.mFrontTimer;
        }

        @Override // com.android.server.display.HwDualSensorEventListenerImpl.SensorDataSender
        public void setTimer(int timer) {
            HwDualSensorEventListenerImpl.this.mFrontTimer = timer;
        }

        @Override // com.android.server.display.HwDualSensorEventListenerImpl.SensorDataSender
        public void sendData() {
            HwDualSensorEventListenerImpl.this.mFrontSensorData.sendSensorData();
        }

        @Override // com.android.server.display.HwDualSensorEventListenerImpl.SensorDataSender
        public boolean needQueue() {
            if (HwDualSensorEventListenerImpl.this.mIsFrontEnable) {
                int i = HwDualSensorEventListenerImpl.this.mFrontWarmUpFlg;
                HwDualSensorData unused = HwDualSensorEventListenerImpl.this.mData;
                if (i >= 2) {
                    return true;
                }
            }
            return false;
        }
    }

    private class BackSensorDataSender implements SensorDataSender {
        private BackSensorDataSender() {
        }

        @Override // com.android.server.display.HwDualSensorEventListenerImpl.SensorDataSender
        public int getRate() {
            return HwDualSensorEventListenerImpl.this.mData.backRateMillis;
        }

        @Override // com.android.server.display.HwDualSensorEventListenerImpl.SensorDataSender
        public int getTimer() {
            return HwDualSensorEventListenerImpl.this.mBackTimer;
        }

        @Override // com.android.server.display.HwDualSensorEventListenerImpl.SensorDataSender
        public void setTimer(int timer) {
            HwDualSensorEventListenerImpl.this.mBackTimer = timer;
        }

        @Override // com.android.server.display.HwDualSensorEventListenerImpl.SensorDataSender
        public void sendData() {
            HwDualSensorEventListenerImpl.this.mBackSensorData.sendSensorData();
        }

        @Override // com.android.server.display.HwDualSensorEventListenerImpl.SensorDataSender
        public boolean needQueue() {
            if (HwDualSensorEventListenerImpl.this.mIsBackEnable) {
                int i = HwDualSensorEventListenerImpl.this.mBackWarmUpFlg;
                HwDualSensorData unused = HwDualSensorEventListenerImpl.this.mData;
                if (i >= 2) {
                    return true;
                }
            }
            return false;
        }
    }

    private class FusedSensorDataSender implements SensorDataSender {
        private FusedSensorDataSender() {
        }

        @Override // com.android.server.display.HwDualSensorEventListenerImpl.SensorDataSender
        public int getRate() {
            return HwDualSensorEventListenerImpl.this.mData.fusedRateMillis;
        }

        @Override // com.android.server.display.HwDualSensorEventListenerImpl.SensorDataSender
        public int getTimer() {
            return HwDualSensorEventListenerImpl.this.mFusedTimer;
        }

        @Override // com.android.server.display.HwDualSensorEventListenerImpl.SensorDataSender
        public void setTimer(int timer) {
            HwDualSensorEventListenerImpl.this.mFusedTimer = timer;
        }

        @Override // com.android.server.display.HwDualSensorEventListenerImpl.SensorDataSender
        public void sendData() {
            synchronized (HwDualSensorEventListenerImpl.this.mFusedSensorLock) {
                HwDualSensorEventListenerImpl.this.mFusedSensorData.sendFusedData();
            }
        }

        @Override // com.android.server.display.HwDualSensorEventListenerImpl.SensorDataSender
        public boolean needQueue() {
            if (HwDualSensorEventListenerImpl.this.mIsFusedEnable) {
                int i = HwDualSensorEventListenerImpl.this.mFusedWarmUpFlg;
                HwDualSensorData unused = HwDualSensorEventListenerImpl.this.mData;
                if (i >= 2) {
                    return true;
                }
            }
            return false;
        }
    }

    public static HwDualSensorEventListenerImpl getInstance(SensorManager sensorManager, Context context) {
        if (sInstance == null) {
            synchronized (sInstanceLock) {
                if (sInstance == null) {
                    sInstance = new HwDualSensorEventListenerImpl(context, sensorManager);
                }
            }
        }
        return sInstance;
    }

    private void attachFrontSensorDataInner(Observer observer) {
        SensorManager sensorManager = this.mSensorManager;
        if (sensorManager == null) {
            Slog.e(TAG, "SensorManager is null!");
            return;
        }
        if (this.mFrontLightSensor == null) {
            this.mFrontLightSensor = sensorManager.getDefaultSensor(5);
            if (DEBUG) {
                Slog.d(TAG, "Obtain mFrontLightSensor at first time.");
            }
        }
        enableFrontSensor();
        this.mFrontSensorData.addObserver(observer);
    }

    public void attachFrontSensorData(Observer observer) {
        if (observer == null) {
            Slog.e(TAG, "observer is null!");
        } else {
            this.mPool.execute(new Runnable(observer) {
                /* class com.android.server.display.$$Lambda$HwDualSensorEventListenerImpl$zVJPM7FQGIjTWf_MhwM4wSAQCKI */
                private final /* synthetic */ Observer f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    HwDualSensorEventListenerImpl.this.lambda$attachFrontSensorData$0$HwDualSensorEventListenerImpl(this.f$1);
                }
            });
        }
    }

    public /* synthetic */ void lambda$attachFrontSensorData$0$HwDualSensorEventListenerImpl(Observer observer) {
        synchronized (this.mFrontSensorLock) {
            attachFrontSensorDataInner(observer);
        }
    }

    private void attachBackSensorDataInner(Observer observer) {
        List<Sensor> lightSensorList;
        if (this.mSensorManager == null) {
            Slog.e(TAG, "SensorManager is null!");
            return;
        }
        if (this.mBackLightSensor == null) {
            if (this.mData.isOutwardLightSensor) {
                lightSensorList = this.mSensorManager.getSensorList(65568);
            } else {
                lightSensorList = this.mSensorManager.getSensorList(65554);
            }
            if (lightSensorList.size() == 1) {
                this.mBackLightSensor = lightSensorList.get(0);
                if (DEBUG) {
                    Slog.d(TAG, "Obtain mBackLightSensor at first time.");
                }
            } else if (DEBUG) {
                Slog.d(TAG, "more than one color sensor: " + lightSensorList.size());
                return;
            } else {
                return;
            }
        }
        enableBackSensor();
        this.mBackSensorData.addObserver(observer);
    }

    public void attachBackSensorData(Observer observer) {
        if (observer == null) {
            Slog.e(TAG, "observer is null!");
        } else {
            this.mPool.execute(new Runnable(observer) {
                /* class com.android.server.display.$$Lambda$HwDualSensorEventListenerImpl$bH_1ZeONSzV0wUySUmg1hmqcJQ */
                private final /* synthetic */ Observer f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    HwDualSensorEventListenerImpl.this.lambda$attachBackSensorData$1$HwDualSensorEventListenerImpl(this.f$1);
                }
            });
        }
    }

    public /* synthetic */ void lambda$attachBackSensorData$1$HwDualSensorEventListenerImpl(Observer observer) {
        synchronized (this.mBackSensorLock) {
            attachBackSensorDataInner(observer);
        }
    }

    public void attachFusedSensorData(Observer observer) {
        if (observer == null) {
            Slog.e(TAG, "observer is null!");
            return;
        }
        synchronized (this.mFusedSensorLock) {
            attachFrontSensorData(this.mFusedSensorData.frontSensorDataObserver);
            attachBackSensorData(this.mFusedSensorData.backSensorDataObserver);
            enableFusedSensor();
            this.mFusedSensorData.addObserver(observer);
        }
    }

    private void detachFrontSensorDataInner(Observer observer) {
        if (observer != null) {
            this.mFrontSensorData.deleteObserver(observer);
            if (this.mFrontSensorData.countObservers() == 0) {
                disableFrontSensor();
            }
        }
    }

    public void detachFrontSensorData(Observer observer) {
        if (observer == null) {
            Slog.e(TAG, "observer is null!");
        } else {
            this.mPool.execute(new Runnable(observer) {
                /* class com.android.server.display.$$Lambda$HwDualSensorEventListenerImpl$N_LeyumDurw4Wm2UOz2OoYm19aI */
                private final /* synthetic */ Observer f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    HwDualSensorEventListenerImpl.this.lambda$detachFrontSensorData$2$HwDualSensorEventListenerImpl(this.f$1);
                }
            });
        }
    }

    public /* synthetic */ void lambda$detachFrontSensorData$2$HwDualSensorEventListenerImpl(Observer observer) {
        synchronized (this.mFrontSensorLock) {
            detachFrontSensorDataInner(observer);
        }
    }

    private void detachBackSensorDataInner(Observer observer) {
        if (observer != null) {
            this.mBackSensorData.deleteObserver(observer);
            if (this.mBackSensorData.countObservers() == 0) {
                disableBackSensor();
            }
        }
    }

    public void detachBackSensorData(Observer observer) {
        if (observer == null) {
            Slog.e(TAG, "observer is null!");
        } else {
            this.mPool.execute(new Runnable(observer) {
                /* class com.android.server.display.$$Lambda$HwDualSensorEventListenerImpl$F3quYngYUSTesl04PM0uMoOssik */
                private final /* synthetic */ Observer f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    HwDualSensorEventListenerImpl.this.lambda$detachBackSensorData$3$HwDualSensorEventListenerImpl(this.f$1);
                }
            });
        }
    }

    public /* synthetic */ void lambda$detachBackSensorData$3$HwDualSensorEventListenerImpl(Observer observer) {
        synchronized (this.mBackSensorLock) {
            detachBackSensorDataInner(observer);
        }
    }

    public void detachFusedSensorData(Observer observer) {
        if (observer == null) {
            Slog.e(TAG, "observer is null!");
            return;
        }
        synchronized (this.mFusedSensorLock) {
            this.mFusedSensorData.deleteObserver(observer);
            if (this.mFusedSensorData.countObservers() == 0) {
                disableFusedSensor();
                detachFrontSensorData(this.mFusedSensorData.frontSensorDataObserver);
                detachBackSensorData(this.mFusedSensorData.backSensorDataObserver);
            }
        }
    }

    private void enableFrontSensor() {
        if (!this.mIsFrontEnable) {
            Slog.i(TAG, "enable front sensor...");
            this.mIsFrontEnable = true;
            this.mFrontWarmUpFlg = 0;
            this.mFrontEnableTime = SystemClock.elapsedRealtimeNanos();
            this.mFrontSensorData.setEnableTime(this.mFrontEnableTime);
            if (this.mHandler != null) {
                this.mSensorManager.registerListener(this.mFrontSensorEventListener, this.mFrontLightSensor, this.mData.frontRateMillis * 1000, this.mHandler);
            }
            Slog.i(TAG, "front sensor enabled");
        }
    }

    private void enableBackSensor() {
        if (!this.mIsBackEnable) {
            Slog.i(TAG, "enable back sensor...");
            this.mIsBackEnable = true;
            this.mBackWarmUpFlg = 0;
            this.mBackEnableTime = SystemClock.elapsedRealtimeNanos();
            this.mBackSensorData.setEnableTime(this.mBackEnableTime);
            if (this.mHandler != null) {
                this.mSensorManager.registerListener(this.mBackSensorEventListener, this.mBackLightSensor, this.mData.backRateMillis * 1000, this.mHandler);
            }
            Slog.i(TAG, "back sensor enabled");
        }
    }

    private void enableFusedSensor() {
        if (!this.mIsFusedEnable) {
            this.mIsFusedEnable = true;
            this.mFusedWarmUpFlg = 0;
            this.mFusedEnableTime = SystemClock.elapsedRealtimeNanos();
            Handler handler = this.mHandler;
            if (handler != null) {
                HwDualSensorData hwDualSensorData = this.mData;
                handler.sendEmptyMessage(3);
            }
            if (DEBUG) {
                Slog.d(TAG, "Fused sensor enabled.");
            }
            if (this.mData.backSensorBypassCount > 0) {
                this.mData.backSensorBypassCount--;
                Slog.i(TAG, "mData.backSensorBypassCount-- :" + this.mData.backSensorBypassCount);
            }
        }
    }

    private void disableFrontSensor() {
        if (this.mIsFrontEnable) {
            this.mIsFrontEnable = false;
            this.mSensorManager.unregisterListener(this.mFrontSensorEventListener);
            Handler handler = this.mHandler;
            if (handler != null) {
                HwDualSensorData hwDualSensorData = this.mData;
                handler.removeMessages(1);
            }
            this.mFrontSensorData.clearSensorData();
            if (DEBUG) {
                Slog.d(TAG, "Front sensor disabled.");
            }
        }
    }

    private void disableBackSensor() {
        if (this.mIsBackEnable) {
            this.mIsBackEnable = false;
            this.mSensorManager.unregisterListener(this.mBackSensorEventListener);
            Handler handler = this.mHandler;
            if (handler != null) {
                HwDualSensorData hwDualSensorData = this.mData;
                handler.removeMessages(2);
            }
            this.mBackSensorData.clearSensorData();
            if (DEBUG) {
                Slog.d(TAG, "Back sensor disabled.");
            }
        }
    }

    private void disableFusedSensor() {
        if (this.mIsFusedEnable) {
            this.mIsFusedEnable = false;
            Handler handler = this.mHandler;
            if (handler != null) {
                HwDualSensorData hwDualSensorData = this.mData;
                handler.removeMessages(3);
            }
            this.mFusedSensorData.clearSensorData();
            if (DEBUG) {
                Slog.d(TAG, "Fused sensor disabled.");
            }
        }
    }

    /* access modifiers changed from: private */
    public static class SensorData extends Observable {
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
            this.mLastValueSystemTime = 0;
            this.mLastValueSensorTime = 0;
            this.mEnableTime = -1;
            this.mLuxDataList = new CopyOnWriteArrayList();
            this.mCctDataList = new CopyOnWriteArrayList();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setSensorData(int lux, int cct, long systemClockTimeStamp, long sensorClockTimeStamp) {
            this.mLuxDataList.add(Integer.valueOf(lux));
            this.mCctDataList.add(Integer.valueOf(cct));
            this.mLastValueSystemTime = systemClockTimeStamp;
            this.mLastValueSensorTime = sensorClockTimeStamp;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void sendSensorData() {
            int i;
            int average;
            int average2;
            int count = 0;
            int sum = 0;
            for (Integer num : this.mLuxDataList) {
                sum += num.intValue();
                count++;
            }
            if (count != 0 && (average2 = sum / count) >= 0) {
                this.mLastSensorLuxValue = average2;
            }
            this.mLuxDataList.clear();
            int count2 = 0;
            int sum2 = 0;
            for (Integer num2 : this.mCctDataList) {
                sum2 += num2.intValue();
                count2++;
            }
            if (count2 != 0 && (average = sum2 / count2) >= 0) {
                this.mLastSensorCctValue = average;
            }
            this.mCctDataList.clear();
            int i2 = this.mLastSensorLuxValue;
            if (i2 < 0 || (i = this.mLastSensorCctValue) < 0) {
                Slog.i(HwDualSensorEventListenerImpl.TAG, "SensorData.sendSensorData(): skip. mLastSensorLuxValue=" + this.mLastSensorLuxValue + " mLastSensorCctValue=" + this.mLastSensorCctValue);
                return;
            }
            long[] data = {(long) i2, (long) i, this.mLastValueSystemTime, this.mLastValueSensorTime, (long) i2, (long) i2};
            if (HwDualSensorEventListenerImpl.DEBUG && SystemClock.elapsedRealtimeNanos() - this.mEnableTime < 1000000000) {
                Slog.d(HwDualSensorEventListenerImpl.TAG, "SensorData.sendSensorData(): lux=" + this.mLastSensorLuxValue + " cct=" + this.mLastSensorCctValue + " systemTime=" + this.mLastValueSystemTime + " sensorTime=" + this.mLastValueSensorTime);
            }
            setChanged();
            notifyObservers(data);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setEnableTime(long timeNanos) {
            this.mEnableTime = timeNanos;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void clearSensorData() {
            this.mLuxDataList.clear();
            this.mCctDataList.clear();
            this.mLastSensorLuxValue = -1;
            this.mLastSensorCctValue = -1;
            this.mLastValueSystemTime = -1;
            this.mLastValueSensorTime = -1;
            this.mEnableTime = -1;
        }
    }

    /* access modifiers changed from: private */
    public class FusedSensorData extends Observable {
        SensorDataObserver backSensorDataObserver;
        SensorDataObserver frontSensorDataObserver;
        private float mAdjustMin;
        private float mBackFloorThresh;
        private long mBackLastValueSystemTime;
        private int[] mBackLuxArray = {-1, -1, -1};
        private long mBackLuxDeviation;
        private int mBackLuxFiltered;
        private int mBackLuxIndex;
        private float mBackLuxRelativeChange;
        private int mBackLuxStaProInd;
        private float mBackRoofThresh;
        private int mCurBackCct;
        private float mCurBackFiltered;
        private int mCurBackLux;
        private int mCurFrontCct;
        private float mCurFrontFiltered;
        private int mCurFrontLux;
        private float mDarkRoomDelta;
        private float mDarkRoomDelta1;
        private float mDarkRoomDelta2;
        private float mDarkRoomThresh;
        private float mDarkRoomThresh1;
        private float mDarkRoomThresh2;
        private float mDarkRoomThresh3;
        private long mDebugPrintTime;
        private long mFrontLastValueSystemTime;
        private int[] mFrontLuxArray = {-1, -1, -1};
        private int mFrontLuxFiltered;
        private int mFrontLuxIndex;
        private float mFrontLuxRelativeChange;
        private int mFrontLuxStaProInd;
        private int mFusedCct;
        private int mFusedLux;
        private boolean mIsBackUpdated;
        private boolean mIsFrontUpdated;
        private long mLastValueBackSensorTime;
        private long mLastValueFrontSensorTime;
        private float mModifyFactor;
        private float mPreBackFiltered;
        private float mPreFrontFiltered;
        private float mStaProb;
        private int[][] mStaProbLut;

        FusedSensorData() {
            clearSensorData();
            initLutParam();
            this.mBackRoofThresh = HwDualSensorEventListenerImpl.this.mData.backRoofThresh;
            this.mBackFloorThresh = HwDualSensorEventListenerImpl.this.mData.backFloorThresh;
            this.mDarkRoomThresh = HwDualSensorEventListenerImpl.this.mData.darkRoomThresh;
            this.mDarkRoomDelta = HwDualSensorEventListenerImpl.this.mData.darkRoomDelta;
            this.mDarkRoomThresh1 = HwDualSensorEventListenerImpl.this.mData.darkRoomThresh1;
            this.mDarkRoomThresh2 = HwDualSensorEventListenerImpl.this.mData.darkRoomThresh2;
            this.mDarkRoomThresh3 = HwDualSensorEventListenerImpl.this.mData.darkRoomThresh3;
            this.mDarkRoomDelta1 = HwDualSensorEventListenerImpl.this.mData.darkRoomDelta1;
            this.mDarkRoomDelta2 = HwDualSensorEventListenerImpl.this.mData.darkRoomDelta2;
            this.frontSensorDataObserver = new SensorDataObserver(new FusedSensorDataRefresher(HwDualSensorEventListenerImpl.this) {
                /* class com.android.server.display.HwDualSensorEventListenerImpl.FusedSensorData.AnonymousClass1 */

                @Override // com.android.server.display.HwDualSensorEventListenerImpl.FusedSensorDataRefresher
                public void updateData(int lux, int cct, long systemTimeStamp, long sensorTimeStamp) {
                    FusedSensorData.this.setFrontData(lux, cct, systemTimeStamp, sensorTimeStamp);
                }
            }, "FrontSensor");
            this.backSensorDataObserver = new SensorDataObserver(new FusedSensorDataRefresher(HwDualSensorEventListenerImpl.this) {
                /* class com.android.server.display.HwDualSensorEventListenerImpl.FusedSensorData.AnonymousClass2 */

                @Override // com.android.server.display.HwDualSensorEventListenerImpl.FusedSensorDataRefresher
                public void updateData(int lux, int cct, long systemTimeStamp, long sensorTimeStamp) {
                    FusedSensorData.this.setBackData(lux, cct, systemTimeStamp, sensorTimeStamp);
                }
            }, "BackSensor");
            this.mIsFrontUpdated = false;
            this.mIsBackUpdated = false;
            setStabilityProbabilityLut();
        }

        /* access modifiers changed from: package-private */
        public class SensorDataObserver implements Observer {
            private final String mName;
            private final FusedSensorDataRefresher mRefresher;

            SensorDataObserver(FusedSensorDataRefresher refresher, String name) {
                this.mRefresher = refresher;
                this.mName = name;
            }

            @Override // java.util.Observer
            public void update(Observable observable, Object arg) {
                if (this.mRefresher == null) {
                    Slog.e(HwDualSensorEventListenerImpl.TAG, "mRefresher is null!");
                } else if (arg == null) {
                    Slog.e(HwDualSensorEventListenerImpl.TAG, "observable arg is null!");
                } else {
                    long[] data = (long[]) arg;
                    int length = data.length;
                    HwDualSensorData unused = HwDualSensorEventListenerImpl.this.mData;
                    if (length < 4) {
                        Slog.e(HwDualSensorEventListenerImpl.TAG, "data.length is invalid!");
                        return;
                    }
                    int lux = (int) data[0];
                    int cct = (int) data[1];
                    long systemTimeStamp = data[2];
                    long sensorTimeStamp = data[3];
                    FusedSensorData.this.mDebugPrintTime = SystemClock.elapsedRealtimeNanos();
                    if (HwDualSensorEventListenerImpl.DEBUG && FusedSensorData.this.mDebugPrintTime - HwDualSensorEventListenerImpl.this.mFusedEnableTime < 1000000000) {
                        Slog.d(HwDualSensorEventListenerImpl.TAG, "FusedSensorData.update(): " + this.mName + " warmUp=" + HwDualSensorEventListenerImpl.this.mFusedWarmUpFlg + " lux=" + lux + " cct=" + cct + " systemTime=" + systemTimeStamp + " sensorTime=" + sensorTimeStamp);
                    }
                    this.mRefresher.updateData(lux, cct, systemTimeStamp, sensorTimeStamp);
                    int i = HwDualSensorEventListenerImpl.this.mFusedWarmUpFlg;
                    HwDualSensorData unused2 = HwDualSensorEventListenerImpl.this.mData;
                    if (i < 2) {
                        if (FusedSensorData.this.mIsFrontUpdated && FusedSensorData.this.mIsBackUpdated) {
                            HwDualSensorEventListenerImpl.access$2708(HwDualSensorEventListenerImpl.this);
                        }
                        if (HwDualSensorEventListenerImpl.this.mHandler != null) {
                            Handler handler = HwDualSensorEventListenerImpl.this.mHandler;
                            HwDualSensorData unused3 = HwDualSensorEventListenerImpl.this.mData;
                            handler.removeMessages(3);
                            Handler handler2 = HwDualSensorEventListenerImpl.this.mHandler;
                            HwDualSensorData unused4 = HwDualSensorEventListenerImpl.this.mData;
                            handler2.sendEmptyMessage(3);
                        }
                    }
                }
            }
        }

        private void initLutParam() {
            this.mCurFrontFiltered = 0.0f;
            this.mPreFrontFiltered = 0.0f;
            this.mCurBackFiltered = 0.0f;
            this.mPreBackFiltered = 0.0f;
            this.mFrontLuxIndex = 0;
            this.mBackLuxIndex = 0;
            this.mFrontLuxRelativeChange = 0.0f;
            this.mBackLuxRelativeChange = 0.0f;
            this.mStaProbLut = new int[][]{new int[]{0, 20, HwDualSensorEventListenerImpl.PERCENTAGE_TH_3, 20, 0}, new int[]{20, 50, HwDualSensorEventListenerImpl.PERCENTAGE_TH_3, 50, 20}, new int[]{HwDualSensorEventListenerImpl.PERCENTAGE_TH_3, HwDualSensorEventListenerImpl.PERCENTAGE_TH_3, 95, HwDualSensorEventListenerImpl.PERCENTAGE_TH_3, HwDualSensorEventListenerImpl.PERCENTAGE_TH_3}, new int[]{20, 50, HwDualSensorEventListenerImpl.PERCENTAGE_TH_3, 50, 20}, new int[]{0, 20, HwDualSensorEventListenerImpl.PERCENTAGE_TH_3, 20, 0}};
        }

        /* access modifiers changed from: package-private */
        public void setFrontData(int lux, int cct, long systemTimeStamp, long sensorTimeStamp) {
            this.mFrontLuxStaProInd = getLuxStableProbabilityIndex(lux, true);
            this.mCurFrontCct = cct;
            this.mFrontLastValueSystemTime = systemTimeStamp;
            this.mLastValueFrontSensorTime = sensorTimeStamp;
            this.mIsFrontUpdated = true;
            if (HwDualSensorEventListenerImpl.DEBUG && this.mDebugPrintTime - HwDualSensorEventListenerImpl.this.mFusedEnableTime < 1000000000) {
                Slog.d(HwDualSensorEventListenerImpl.TAG, "FusedSensorData.setFrontData(): mCurFrontFiltered=" + this.mCurFrontFiltered + " mFrontLuxRelativeChange=" + this.mFrontLuxRelativeChange + " lux=" + lux + " cct=" + cct + " systemTime=" + this.mFrontLastValueSystemTime + " sensorTime=" + this.mLastValueFrontSensorTime);
            }
        }

        /* access modifiers changed from: package-private */
        public void setBackData(int lux, int cct, long systemTimeStamp, long sensorTimeStamp) {
            this.mBackLuxStaProInd = getLuxStableProbabilityIndex(lux, false);
            this.mCurBackCct = cct;
            this.mBackLastValueSystemTime = systemTimeStamp;
            this.mLastValueBackSensorTime = sensorTimeStamp;
            this.mIsBackUpdated = true;
            if (HwDualSensorEventListenerImpl.DEBUG && this.mDebugPrintTime - HwDualSensorEventListenerImpl.this.mFusedEnableTime < 1000000000) {
                Slog.d(HwDualSensorEventListenerImpl.TAG, "FusedSensorData.setBackData(): mCurBackFiltered=" + this.mCurBackFiltered + " mBackLuxRelativeChange=" + this.mBackLuxRelativeChange + " lux=" + lux + " cct=" + cct + " systemTime=" + this.mBackLastValueSystemTime + " sensorTime=" + this.mLastValueBackSensorTime);
            }
        }

        private int calculateIndex(boolean frontSensorIsSelected, float luxRelativeChange) {
            int[] stabilityThresholdList;
            int targetIndex = 0;
            if (frontSensorIsSelected) {
                stabilityThresholdList = HwDualSensorEventListenerImpl.this.mData.frontSensorStabilityThreshold;
            } else {
                stabilityThresholdList = HwDualSensorEventListenerImpl.this.mData.backSensorStabilityThreshold;
            }
            int stabilityThresholdListSize = stabilityThresholdList.length;
            while (targetIndex < stabilityThresholdListSize && Float.compare(luxRelativeChange, (float) stabilityThresholdList[targetIndex]) > 0) {
                targetIndex++;
            }
            return targetIndex > stabilityThresholdListSize ? stabilityThresholdListSize : targetIndex;
        }

        private int getLuxStableProbabilityIndex(int lux, boolean frontSensorIsSelected) {
            if (lux < 0 && HwDualSensorEventListenerImpl.DEBUG) {
                Slog.d(HwDualSensorEventListenerImpl.TAG, "getLuxStableProbabilityIndex exception: lux=" + lux + " frontSensorIsSelected=" + frontSensorIsSelected);
            }
            int[] luxArray = frontSensorIsSelected ? this.mFrontLuxArray : this.mBackLuxArray;
            int luxArrayIndex = frontSensorIsSelected ? this.mFrontLuxIndex : this.mBackLuxIndex;
            luxArray[luxArrayIndex] = lux;
            int luxArrayIndex2 = (luxArrayIndex + 1) % 3;
            int sum = 0;
            int count = 0;
            for (int i = 0; i < 3; i++) {
                if (luxArray[i] > 0) {
                    sum += luxArray[i];
                    count++;
                }
            }
            float curFiltered = (!HwDualSensorEventListenerImpl.this.mData.isFilterOn || count <= 0) ? (float) lux : ((float) sum) / ((float) count);
            if (!frontSensorIsSelected) {
                this.mBackLuxDeviation = 0;
                for (int curInd = 0; curInd < luxArray.length; curInd++) {
                    this.mBackLuxDeviation += Math.abs(((long) luxArray[curInd]) - ((long) curFiltered));
                }
                this.mBackLuxDeviation /= (long) luxArray.length;
            }
            float luxRelativeChange = 100.0f;
            float preFiltered = frontSensorIsSelected ? this.mPreFrontFiltered : this.mPreBackFiltered;
            if (Float.compare(preFiltered, 0.0f) != 0) {
                luxRelativeChange = ((curFiltered - preFiltered) * 100.0f) / preFiltered;
            }
            if (frontSensorIsSelected) {
                this.mCurFrontFiltered = curFiltered;
                this.mFrontLuxRelativeChange = luxRelativeChange;
                this.mFrontLuxIndex = luxArrayIndex2;
            } else {
                this.mCurBackFiltered = curFiltered;
                this.mBackLuxRelativeChange = luxRelativeChange;
                this.mBackLuxIndex = luxArrayIndex2;
            }
            return calculateIndex(frontSensorIsSelected, luxRelativeChange);
        }

        private void updateFusedData() {
            getStabilizedLuxAndCct();
        }

        private void handleFrontLarger() {
            this.mCurFrontLux = (int) this.mCurFrontFiltered;
            this.mCurBackLux = (int) this.mCurBackFiltered;
            if (this.mCurFrontLux >= 0 || this.mCurBackLux >= 0) {
                if (this.mCurFrontLux < 0) {
                    this.mCurFrontLux = this.mCurBackLux;
                    this.mCurFrontCct = this.mCurBackCct;
                }
                if (this.mCurBackLux < 0) {
                    this.mCurBackLux = this.mCurFrontLux;
                    this.mCurBackCct = this.mCurFrontCct;
                }
                this.mFusedLux = this.mCurFrontLux;
                int i = this.mCurFrontCct;
                int i2 = this.mCurBackCct;
                if (i <= i2) {
                    i = i2;
                }
                this.mFusedCct = i;
            } else if (HwDualSensorEventListenerImpl.DEBUG) {
                Slog.d(HwDualSensorEventListenerImpl.TAG, "FusedSensorData.updateFusedData(): updateFusedData returned, mCurFrontLux=" + this.mCurFrontLux + " mCurBackLux=" + this.mCurBackLux);
            }
        }

        private void handleFlashOn() {
            this.mBackLuxFiltered = this.mFrontLuxFiltered;
            handleFrontLarger();
            if (HwDualSensorEventListenerImpl.DEBUG && this.mDebugPrintTime - HwDualSensorEventListenerImpl.this.mFusedEnableTime < 1000000000) {
                Slog.d(HwDualSensorEventListenerImpl.TAG, "FusedSensorData.updateFusedData(): mFusedLux=" + this.mFusedLux + " mCurFrontLux=" + this.mCurFrontLux + " mCurBackLux=" + this.mCurBackLux + " mFusedCct=" + this.mFusedCct + " mCurFrontCct=" + this.mCurFrontCct + " mCurBackCct=" + this.mCurBackCct);
            }
        }

        private void handleStability() {
            float f = this.mStaProb;
            if (f == 0.0f) {
                this.mPreFrontFiltered = this.mCurFrontFiltered;
                this.mPreBackFiltered = this.mCurBackFiltered;
                return;
            }
            if (f == 20.0f) {
                this.mModifyFactor = HwDualSensorEventListenerImpl.MODIFY_FACTOR_THRESHOLD_1;
            } else if (f == 50.0f) {
                this.mModifyFactor = HwDualSensorEventListenerImpl.MODIFY_FACTOR_THRESHOLD_2;
            } else if (f == 90.0f) {
                this.mModifyFactor = HwDualSensorEventListenerImpl.MODIFY_FACTOR_THRESHOLD_3;
            } else {
                this.mModifyFactor = 0.002f;
            }
            float frontAdjustment = this.mModifyFactor * this.mPreFrontFiltered;
            if (frontAdjustment < this.mAdjustMin) {
                frontAdjustment = this.mAdjustMin;
            }
            float backAdjustment = this.mModifyFactor * this.mPreBackFiltered;
            if (backAdjustment < this.mAdjustMin) {
                backAdjustment = this.mAdjustMin;
            }
            float f2 = this.mCurFrontFiltered;
            float f3 = this.mPreFrontFiltered;
            if (f2 < f3) {
                this.mPreFrontFiltered = f3 - frontAdjustment;
            }
            float f4 = this.mCurFrontFiltered;
            float f5 = this.mPreFrontFiltered;
            if (f4 > f5) {
                this.mPreFrontFiltered = f5 + frontAdjustment;
            }
            float f6 = this.mCurBackFiltered;
            float f7 = this.mPreBackFiltered;
            if (f6 < f7) {
                this.mPreBackFiltered = f7 - backAdjustment;
            }
            float f8 = this.mCurBackFiltered;
            float f9 = this.mPreBackFiltered;
            if (f8 > f9) {
                this.mPreBackFiltered = f9 + backAdjustment;
            }
        }

        private void handleBackLarger() {
            float curDarkRoomDelta;
            float f = this.mCurBackFiltered;
            float f2 = this.mCurFrontFiltered;
            float mDarkRoomDelta3 = f - f2;
            if (f2 >= 0.0f && f2 <= this.mDarkRoomThresh) {
                curDarkRoomDelta = this.mDarkRoomDelta;
            } else if (Float.compare(this.mCurFrontFiltered, this.mDarkRoomThresh1) <= 0) {
                float f3 = this.mCurFrontFiltered;
                float f4 = this.mDarkRoomThresh;
                float f5 = this.mDarkRoomDelta1;
                float f6 = this.mDarkRoomDelta;
                curDarkRoomDelta = (((f3 - f4) * (f5 - f6)) / (this.mDarkRoomThresh1 - f4)) + f6;
            } else if (Float.compare(this.mCurFrontFiltered, this.mDarkRoomThresh2) < 0) {
                float f7 = this.mCurFrontFiltered;
                float f8 = this.mDarkRoomThresh1;
                float f9 = this.mDarkRoomDelta2;
                float f10 = this.mDarkRoomDelta1;
                curDarkRoomDelta = (((f7 - f8) * (f9 - f10)) / (this.mDarkRoomThresh2 - f8)) + f10;
            } else if (Float.compare(this.mCurFrontFiltered, this.mDarkRoomThresh3) < 0) {
                float f11 = this.mCurFrontFiltered;
                float f12 = this.mDarkRoomThresh2;
                float f13 = this.mDarkRoomDelta2;
                curDarkRoomDelta = (((f11 - f12) * (mDarkRoomDelta3 - f13)) / (this.mDarkRoomThresh3 - f12)) + f13;
            } else {
                curDarkRoomDelta = mDarkRoomDelta3;
            }
            this.mBackFloorThresh = this.mCurFrontFiltered + curDarkRoomDelta;
            float f14 = this.mCurBackFiltered;
            float f15 = this.mBackFloorThresh;
            if (f14 >= f15) {
                f14 = f15;
            }
            this.mCurBackFiltered = f14;
            this.mStaProb = (float) this.mStaProbLut[this.mBackLuxStaProInd][this.mFrontLuxStaProInd];
            handleStability();
        }

        private void getStabilizedLuxAndCct() {
            float f = this.mCurFrontFiltered;
            this.mFrontLuxFiltered = (int) f;
            float f2 = this.mCurBackFiltered;
            this.mBackLuxFiltered = (int) f2;
            if (Float.compare(f, f2) >= 0) {
                handleFrontLarger();
            } else if (isFlashlightOn()) {
                handleFlashOn();
            } else {
                handleBackLarger();
                this.mCurFrontLux = (int) this.mPreFrontFiltered;
                this.mCurBackLux = (int) this.mPreBackFiltered;
                if (this.mCurFrontLux >= 0 || this.mCurBackLux >= 0) {
                    if (this.mCurFrontLux < 0) {
                        this.mCurFrontLux = this.mCurBackLux;
                        this.mCurFrontCct = this.mCurBackCct;
                    }
                    if (this.mCurBackLux < 0) {
                        this.mCurBackLux = this.mCurFrontLux;
                        this.mCurBackCct = this.mCurFrontCct;
                    }
                    int i = this.mCurFrontLux;
                    int i2 = this.mCurBackLux;
                    if (i <= i2) {
                        i = i2;
                    }
                    this.mFusedLux = i;
                    int i3 = this.mCurFrontCct;
                    int i4 = this.mCurBackCct;
                    if (i3 <= i4) {
                        i3 = i4;
                    }
                    this.mFusedCct = i3;
                } else if (HwDualSensorEventListenerImpl.DEBUG) {
                    Slog.d(HwDualSensorEventListenerImpl.TAG, "FusedSensorData.updateFusedData(): updateFusedData returned, mCurFrontLux=" + this.mCurFrontLux + " mCurBackLux=" + this.mCurBackLux);
                    return;
                } else {
                    return;
                }
            }
            if (HwDualSensorEventListenerImpl.DEBUG && this.mDebugPrintTime - HwDualSensorEventListenerImpl.this.mFusedEnableTime < 1000000000) {
                Slog.d(HwDualSensorEventListenerImpl.TAG, "FusedSensorData.updateFusedData(): mFusedLux=" + this.mFusedLux + " mCurFrontLux=" + this.mCurFrontLux + " mCurBackLux=" + this.mCurBackLux + " mFusedCct=" + this.mFusedCct + " mCurFrontCct=" + this.mCurFrontCct + " mCurBackCct=" + this.mCurBackCct);
            }
        }

        /* access modifiers changed from: package-private */
        public void sendFusedData() {
            if (this.mIsFrontUpdated) {
                if (HwDualSensorEventListenerImpl.this.mData.backSensorBypassCount <= 0 && !this.mIsBackUpdated) {
                    HwDualSensorEventListenerImpl.this.mData.backTimeOutCount++;
                    if (HwDualSensorEventListenerImpl.this.mData.backTimeOutCount >= HwDualSensorEventListenerImpl.this.mData.backSensorTimeOutTh) {
                        HwDualSensorEventListenerImpl.this.mData.backSensorBypassCount = HwDualSensorEventListenerImpl.this.mData.backSensorBypassCountMax;
                        Slog.i(HwDualSensorEventListenerImpl.TAG, "mData.backTimeOutCount start: " + HwDualSensorEventListenerImpl.this.mData.backTimeOutCount);
                    } else if (HwDualSensorEventListenerImpl.DEBUG && this.mDebugPrintTime - HwDualSensorEventListenerImpl.this.mFusedEnableTime < 1000000000) {
                        Slog.d(HwDualSensorEventListenerImpl.TAG, "Skip this sendFusedData! mIsFrontUpdated=" + this.mIsFrontUpdated + " mIsBackUpdated=" + this.mIsBackUpdated);
                        return;
                    } else {
                        return;
                    }
                }
                HwDualSensorEventListenerImpl.this.mData.backTimeOutCount = 0;
                updateFusedData();
                long lastValueSensorTime = this.mLastValueFrontSensorTime;
                long j = this.mLastValueBackSensorTime;
                if (lastValueSensorTime <= j) {
                    lastValueSensorTime = j;
                }
                if (this.mFusedLux < 0 || this.mFusedCct < 0) {
                    Slog.i(HwDualSensorEventListenerImpl.TAG, "FusedSensorData.sendFusedData(): skip. mFusedLux=" + this.mFusedLux + " mFusedCct=" + this.mFusedCct);
                    return;
                }
                if (HwDualSensorEventListenerImpl.DEBUG && this.mDebugPrintTime - HwDualSensorEventListenerImpl.this.mFusedEnableTime < 1000000000) {
                    Slog.d(HwDualSensorEventListenerImpl.TAG, "FusedSensorData.sendFusedData(): lux=" + this.mFusedLux + " cct=" + this.mFusedCct + " sensorTime=" + lastValueSensorTime + " FrontSensorTime=" + this.mLastValueFrontSensorTime + " BackSensorTime=" + this.mLastValueBackSensorTime);
                }
                long lastValueSystemTime = this.mFrontLastValueSystemTime;
                long j2 = this.mBackLastValueSystemTime;
                if (lastValueSystemTime <= j2) {
                    lastValueSystemTime = j2;
                }
                setChanged();
                notifyObservers(new long[]{(long) this.mFusedLux, (long) this.mFusedCct, lastValueSystemTime, lastValueSensorTime, (long) this.mFrontLuxFiltered, (long) this.mBackLuxFiltered});
            } else if (HwDualSensorEventListenerImpl.DEBUG) {
                Slog.d(HwDualSensorEventListenerImpl.TAG, "Skip this sendFusedData! mIsFrontUpdated=" + this.mIsFrontUpdated + " mIsBackUpdated=" + this.mIsBackUpdated);
            }
        }

        /* access modifiers changed from: package-private */
        public long getSensorDataTime() {
            long j = this.mFrontLastValueSystemTime;
            long j2 = this.mBackLastValueSystemTime;
            return j > j2 ? j : j2;
        }

        /* access modifiers changed from: package-private */
        public final void clearSensorData() {
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
            this.mFrontLuxFiltered = 0;
            this.mBackLuxFiltered = 0;
            this.mCurFrontFiltered = 0.0f;
            this.mPreFrontFiltered = 0.0f;
            this.mCurBackFiltered = 0.0f;
            this.mPreBackFiltered = 0.0f;
            this.mFrontLuxIndex = 0;
            this.mBackLuxIndex = 0;
            this.mFrontLuxRelativeChange = 0.0f;
            this.mBackLuxRelativeChange = 0.0f;
            this.mFrontLuxStaProInd = 0;
            this.mBackLuxStaProInd = 0;
            this.mAdjustMin = 1.0f;
            this.mIsFrontUpdated = false;
            this.mIsBackUpdated = false;
            this.mBackLuxDeviation = 0;
            for (int i = 0; i < 3; i++) {
                this.mFrontLuxArray[i] = -1;
                this.mBackLuxArray[i] = -1;
            }
        }

        private void setStabilityProbabilityLut() {
            if (HwDualSensorEventListenerImpl.this.mData.stabilizedProbabilityLut == null) {
                if (HwDualSensorEventListenerImpl.DEBUG) {
                    Slog.i(HwDualSensorEventListenerImpl.TAG, "mData.stabilizedProbabilityLut is null");
                }
            } else if (HwDualSensorEventListenerImpl.this.mData.stabilizedProbabilityLut.length == 25) {
                for (int rowInd = 0; rowInd < 5; rowInd++) {
                    for (int colInd = 0; colInd < 5; colInd++) {
                        this.mStaProbLut[rowInd][colInd] = HwDualSensorEventListenerImpl.this.mData.stabilizedProbabilityLut[(rowInd * 5) + colInd];
                    }
                }
            } else if (HwDualSensorEventListenerImpl.DEBUG) {
                Slog.i(HwDualSensorEventListenerImpl.TAG, "mData.stabilizedProbabilityLut.length=" + HwDualSensorEventListenerImpl.this.mData.stabilizedProbabilityLut.length);
            }
        }

        private boolean isFlashlightOn() {
            int i = HwDualSensorEventListenerImpl.this.mData.flashlightDetectionMode;
            if (i == 0) {
                return this.mCurBackFiltered > this.mBackRoofThresh || this.mBackLuxDeviation > HwDualSensorEventListenerImpl.this.mData.backLuxDeviationThresh;
            }
            if (i != 1) {
                return false;
            }
            if (HwDualSensorEventListenerImpl.this.mFlashlightMode == HwDualSensorData.FlashlightMode.TORCH_MODE || SystemClock.uptimeMillis() - HwDualSensorEventListenerImpl.this.mFlashlightOffTimeStampMs < ((long) HwDualSensorEventListenerImpl.this.mData.flashlightOffTimeThMs)) {
                return true;
            }
            if (HwDualSensorEventListenerImpl.this.mFlashlightMode == HwDualSensorData.FlashlightMode.CAMERA_MODE) {
                return this.mCurBackFiltered > this.mBackRoofThresh || this.mBackLuxDeviation > HwDualSensorEventListenerImpl.this.mData.backLuxDeviationThresh;
            }
            return false;
        }
    }

    private final class DualSensorHandler extends Handler {
        private long mTimestamp = 0;

        DualSensorHandler(Looper looper) {
            super(looper, null, true);
        }

        private void updateTimer(int newTimer) {
            if (newTimer == -1) {
                return;
            }
            if (HwDualSensorEventListenerImpl.this.mTimer == -1) {
                HwDualSensorEventListenerImpl.this.mTimer = newTimer;
            } else if (newTimer < HwDualSensorEventListenerImpl.this.mTimer) {
                HwDualSensorEventListenerImpl.this.mTimer = newTimer;
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
            HwDualSensorEventListenerImpl.this.mTimer = timer;
            queueMessage();
        }

        private void queueMessage() {
            this.mTimestamp = SystemClock.uptimeMillis();
            HwDualSensorData unused = HwDualSensorEventListenerImpl.this.mData;
            removeMessages(0);
            HwDualSensorData unused2 = HwDualSensorEventListenerImpl.this.mData;
            sendEmptyMessageDelayed(0, (long) HwDualSensorEventListenerImpl.this.mTimer);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg == null) {
                Slog.e(HwDualSensorEventListenerImpl.TAG, "handleMessage: msg is null!");
                return;
            }
            int i = msg.what;
            if (i == 0) {
                processTimer(HwDualSensorEventListenerImpl.this.mFrontSender);
                processTimer(HwDualSensorEventListenerImpl.this.mBackSender);
                processTimer(HwDualSensorEventListenerImpl.this.mFusedSender);
                HwDualSensorEventListenerImpl.this.mTimer = -1;
                updateTimer(HwDualSensorEventListenerImpl.this.mFrontTimer);
                updateTimer(HwDualSensorEventListenerImpl.this.mBackTimer);
                updateTimer(HwDualSensorEventListenerImpl.this.mFusedTimer);
                if (HwDualSensorEventListenerImpl.this.mTimer != -1) {
                    queueMessage();
                }
            } else if (i == 1) {
                processTimerAsTrigger(HwDualSensorEventListenerImpl.this.mFrontSender);
            } else if (i == 2) {
                processTimerAsTrigger(HwDualSensorEventListenerImpl.this.mBackSender);
            } else if (i != 3) {
                Slog.i(HwDualSensorEventListenerImpl.TAG, "handleMessage: Invalid message");
            } else {
                processTimerAsTrigger(HwDualSensorEventListenerImpl.this.mFusedSender);
            }
        }
    }

    public int getModuleSensorOption(String moduleName) {
        if (this.mData.moduleSensorMap == null || !this.mData.moduleSensorMap.containsKey(moduleName)) {
            Slog.i(TAG, moduleName + " getModuleSensorOption=-1");
            return -1;
        }
        Slog.i(TAG, moduleName + " getModuleSensorOption=" + this.mData.moduleSensorMap.get(moduleName));
        return this.mData.moduleSensorMap.get(moduleName).intValue();
    }

    private void registerBackCameraTorchCallback() {
        try {
            this.mCameraManager = (CameraManager) this.mContext.getSystemService("camera");
            if (this.mCameraManager == null) {
                Slog.w(TAG, "mCameraManager is null");
                HwDualSensorData hwDualSensorData = this.mData;
                HwDualSensorData hwDualSensorData2 = this.mData;
                hwDualSensorData.flashlightDetectionMode = 0;
                return;
            }
            Slog.i(TAG, "mCameraManager successfully obtained");
            String[] ids = this.mCameraManager.getCameraIdList();
            for (String id : ids) {
                CameraCharacteristics charact = this.mCameraManager.getCameraCharacteristics(id);
                Boolean isFlashAvailable = (Boolean) charact.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                Integer lensFacing = (Integer) charact.get(CameraCharacteristics.LENS_FACING);
                if (isFlashAvailable != null && isFlashAvailable.booleanValue() && lensFacing != null && lensFacing.intValue() == 1) {
                    this.mBackCameraId = id;
                }
            }
            if (this.mBackCameraId != null) {
                try {
                    this.mCameraManager.registerTorchCallback(this.mTorchCallback, (Handler) null);
                } catch (IllegalArgumentException e) {
                    HwDualSensorData hwDualSensorData3 = this.mData;
                    HwDualSensorData hwDualSensorData4 = this.mData;
                    hwDualSensorData3.flashlightDetectionMode = 0;
                    Slog.w(TAG, "IllegalArgumentException " + e);
                }
            } else {
                Slog.w(TAG, "mBackCameraId is null");
                HwDualSensorData hwDualSensorData5 = this.mData;
                HwDualSensorData hwDualSensorData6 = this.mData;
                hwDualSensorData5.flashlightDetectionMode = 0;
            }
        } catch (CameraAccessException e2) {
            this.mData.flashlightDetectionMode = 0;
            Slog.w(TAG, "CameraAccessException " + e2);
        }
    }

    public boolean isInwardFoldScreenEnable() {
        HwDualSensorData hwDualSensorData = this.mData;
        if (hwDualSensorData == null) {
            return false;
        }
        return hwDualSensorData.isInwardFoldScreen;
    }
}
