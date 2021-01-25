package com.android.server.fsm;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.display.HwFoldScreenState;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.Process;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Flog;
import android.util.Slog;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.os.HwVibrator;
import java.util.concurrent.atomic.AtomicBoolean;

/* access modifiers changed from: package-private */
public class SensorFoldStateManager {
    private static final int DEFAULT_EXPAND_ENGLE = 30;
    private static final int DEFAULT_FOLD_ENGLE = 40;
    private static final String ENTER_TENT_MODE_TO_SENSORHUB = "8";
    private static final String EXIT_TENT_MODE_TO_SENSORHUB = "9";
    private static final String INWARD_SCREEN_EXPAND_THRESHOLD = "inward_screen_expand_threshold";
    private static final String INWARD_SCREEN_FOLD_THRESHOLD = "inward_screen_fold_threshold";
    private static final int LOG_TIME = 1000;
    private static final int MONITOR_TENT_STATE_ID = 991331016;
    private static final int MSG_HANDLE_FOLD_STATE_SENSOR = 0;
    private static final int POSTURE_SENSOR_LENGTH = 7;
    private static final int SENSOR_RATE = (SENSOR_RATE_REDUCE_ENABLE ? 50000 : 25000);
    private static final boolean SENSOR_RATE_REDUCE_ENABLE = SystemProperties.getBoolean("hw_mc.foldscreen.reduce_sensor_rate", false);
    private static final String TAG = "Fsm_SensorFoldStateManager";
    private Sensor mAccSensor;
    private final SensorEventListener mAccSensorListener = new SensorEventListener() {
        /* class com.android.server.fsm.SensorFoldStateManager.AnonymousClass2 */

        @Override // android.hardware.SensorEventListener
        public void onSensorChanged(SensorEvent event) {
        }

        @Override // android.hardware.SensorEventListener
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    private ISensorPostureCallback mCallback;
    private Context mContext;
    private final SensorEventListener mFoldStateSensorListener = new SensorEventListener() {
        /* class com.android.server.fsm.SensorFoldStateManager.AnonymousClass1 */

        @Override // android.hardware.SensorEventListener
        public void onSensorChanged(SensorEvent event) {
            long time = SystemClock.uptimeMillis();
            if (event.values.length == 7) {
                if (time - SensorFoldStateManager.this.mLastEventTime >= 1000) {
                    SensorPostureProcess.printPostureSensor(event.values);
                    SensorFoldStateManager.this.mLastEventTime = time;
                }
                SensorFoldStateManager.this.mHandler.removeMessages(0);
                Message msg = SensorFoldStateManager.this.mHandler.obtainMessage(0);
                msg.obj = event.values;
                SensorFoldStateManager.this.mHandler.sendMessage(msg);
            }
        }

        @Override // android.hardware.SensorEventListener
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    private SensorHandler mHandler;
    private HandlerThread mHandlerThread;
    private boolean mInitPortraitState = true;
    private boolean mIsMagnWakeUp = false;
    private long mLastEventTime = 0;
    private final Object mLock = new Object();
    private MagnetometerWakeupManager mMagnetometerWakeup;
    private Sensor mPostureSensor;
    private SensorManager mSensorManager;
    private int mState = 0;
    private final TentSensorState mTentState;

    SensorFoldStateManager(Context context) {
        Slog.i("Fsm_SensorFoldStateManager", "SensorFoldStateManager init");
        this.mContext = context;
        this.mHandlerThread = new HandlerThread("Fsm_SensorFoldStateManager");
        this.mHandlerThread.start();
        this.mHandler = new SensorHandler(this.mHandlerThread.getLooper());
        this.mSensorManager = (SensorManager) this.mContext.getSystemService("sensor");
        this.mPostureSensor = this.mSensorManager.getDefaultSensor(65573);
        if (!SENSOR_RATE_REDUCE_ENABLE) {
            this.mAccSensor = this.mSensorManager.getDefaultSensor(65558);
            this.mSensorManager.registerListener(this.mAccSensorListener, this.mAccSensor, SENSOR_RATE, this.mHandler);
        }
        this.mMagnetometerWakeup = MagnetometerWakeupManager.getInstance(this.mContext);
        this.mTentState = new TentSensorState();
    }

    private void setInwardThreshold() {
        if (HwFoldScreenState.isInwardFoldDevice()) {
            int expandValue = Settings.Global.getInt(this.mContext.getContentResolver(), INWARD_SCREEN_EXPAND_THRESHOLD, 30);
            int foldValue = Settings.Global.getInt(this.mContext.getContentResolver(), INWARD_SCREEN_FOLD_THRESHOLD, 40);
            if (expandValue > foldValue || (expandValue == 30 && foldValue == 40)) {
                Slog.i("Fsm_SensorFoldStateManager", "Threshold expand angle:" + expandValue + " must less fold:" + foldValue);
                return;
            }
            SensorPostureProcess.setInwardThreshold(expandValue, foldValue);
        }
    }

    public boolean turnOnFoldStateSensor(ISensorPostureCallback callback, int wakeUpType) {
        boolean z = false;
        if (callback == null) {
            Slog.i("Fsm_SensorFoldStateManager", "turnOnFoldStateSensor callback is null");
            return false;
        }
        setInwardThreshold();
        synchronized (this.mLock) {
            boolean z2 = true;
            if (this.mCallback == null) {
                this.mSensorManager.registerListener(this.mFoldStateSensorListener, this.mPostureSensor, SENSOR_RATE, this.mHandler);
                if (wakeUpType == 4) {
                    z = true;
                }
                this.mIsMagnWakeUp = z;
                this.mCallback = callback;
                Slog.i("Fsm_SensorFoldStateManager", "registerFoldStateSensor success");
                return true;
            }
            if (wakeUpType != 4) {
                z2 = false;
            }
            this.mIsMagnWakeUp = z2;
            Slog.i("Fsm_SensorFoldStateManager", "FoldStateSensor is already registered");
            return false;
        }
    }

    public boolean turnOffFoldStateSensor(ISensorPostureCallback callback) {
        if (callback == null) {
            Slog.i("Fsm_SensorFoldStateManager", "turnOffFoldStateSensor callback is null");
            return false;
        }
        synchronized (this.mLock) {
            if (this.mCallback == null || !callback.equals(this.mCallback)) {
                Slog.i("Fsm_SensorFoldStateManager", "FoldStateSensor is not registered");
                return false;
            }
            this.mCallback = null;
            this.mSensorManager.unregisterListener(this.mFoldStateSensorListener);
            this.mState = 0;
            this.mInitPortraitState = true;
            this.mIsMagnWakeUp = false;
            Slog.i("Fsm_SensorFoldStateManager", "unregisterFoldStateSensor success");
            return true;
        }
    }

    public int getPosture() {
        int i;
        synchronized (this.mLock) {
            i = this.mState;
        }
        return i;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleFoldStateSensor(float[] data) {
        int preState;
        synchronized (this.mLock) {
            if (this.mInitPortraitState) {
                this.mInitPortraitState = SensorPostureProcess.isPortraitState(data);
            }
            if (this.mCallback != null) {
                if (this.mInitPortraitState) {
                    Slog.i("Fsm_SensorFoldStateManager", "init state is Portrait");
                    if (this.mMagnetometerWakeup.getHallData() == 1) {
                        preState = 1;
                    } else {
                        preState = 2;
                    }
                } else {
                    preState = SensorPostureProcess.getFoldableState(data, this.mState);
                }
                if (this.mIsMagnWakeUp) {
                    if (preState == 1) {
                        this.mIsMagnWakeUp = false;
                    } else {
                        return;
                    }
                }
                if (preState != this.mState) {
                    this.mState = preState;
                    this.mCallback.onPostureChange(transFoldStateToPosture(this.mState));
                }
            }
        }
    }

    private int transFoldStateToPosture(int state) {
        if (state == 1) {
            return 109;
        }
        if (state == 2) {
            return 103;
        }
        if (state != 3) {
            return 100;
        }
        return 106;
    }

    /* access modifiers changed from: private */
    public final class SensorHandler extends Handler {
        SensorHandler(Looper looper) {
            super(looper, null, true);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what != 0) {
                Slog.e("Fsm_SensorFoldStateManager", "Invalid message");
                return;
            }
            float[] data = (float[]) msg.obj;
            SensorFoldStateManager.this.handleFoldStateSensor(data);
            ReportMonitorProcess.getInstance().handerSensorAngleEvent(data[6]);
            if (SensorFoldStateManager.this.mTentState != null && HwFoldScreenState.isInwardFoldDevice()) {
                SensorFoldStateManager.this.mTentState.judgeTentState(data[6]);
            }
        }
    }

    /* access modifiers changed from: private */
    public final class TentSensorState {
        private static final int FOLD_STATE_ENTER_MOTION = 2;
        private static final int FOLD_STATE_EXPAND_MOTION = 1;
        private static final int FOLD_STATE_FOLDED_MOTION = 0;
        private static final int MOTION_STAY_TIME = 1500;
        private static final String TAG = "Fsm_TentSensorProcess";
        private static final float TENT_ENTER_ANGLE_MAX = 25.0f;
        private static final float TENT_ENTER_ANGLE_MIN = 6.0f;
        private static final float TENT_EXIT_ANGLE = 90.0f;
        private volatile AtomicBoolean mEnterTentFlag;
        private volatile long mLastEnterMotionTime;
        private PowerManager mPowerManager;
        private PosturePreprocessManager mPreprocess;
        private volatile int mStateMotion;

        private TentSensorState() {
            this.mStateMotion = -1;
            this.mLastEnterMotionTime = 0;
            this.mEnterTentFlag = new AtomicBoolean(false);
        }

        public void judgeTentState(float angle) {
            if (this.mPowerManager == null) {
                this.mPowerManager = (PowerManager) SensorFoldStateManager.this.mContext.getSystemService("power");
            }
            if (this.mPowerManager.isScreenOn()) {
                if (angle < TENT_ENTER_ANGLE_MIN) {
                    exitTentPolicy();
                    this.mStateMotion = 0;
                } else if (angle < TENT_ENTER_ANGLE_MIN || angle >= TENT_ENTER_ANGLE_MAX) {
                    this.mStateMotion = 1;
                    if (angle > TENT_EXIT_ANGLE) {
                        exitTentPolicy();
                    }
                } else {
                    long time = System.currentTimeMillis();
                    if (this.mStateMotion == 0) {
                        this.mLastEnterMotionTime = time;
                        this.mStateMotion = 2;
                        Slog.i("Fsm_TentSensorProcess", "Enter Tent motion detect begin.");
                    } else if (this.mStateMotion == 1) {
                        Slog.i("Fsm_TentSensorProcess", "from expand to fold, return");
                    } else if (time - this.mLastEnterMotionTime >= 1500 && !this.mEnterTentFlag.get()) {
                        this.mLastEnterMotionTime = time;
                        enterTentPolicy();
                    }
                }
            }
        }

        private void reportTentState(boolean state) {
            ActivityInfo ai = ActivityManagerEx.getLastResumedActivity();
            String packageName = ai != null ? ai.packageName : "";
            Flog.bdReport((int) SensorFoldStateManager.MONITOR_TENT_STATE_ID, "{tentState:" + state + ",packageName:" + packageName + "}");
        }

        private void enterTentPolicy() {
            if (!this.mEnterTentFlag.get()) {
                Slog.i("Fsm_TentSensorProcess", "enterTentPolicy");
                this.mEnterTentFlag.set(true);
                if (this.mPreprocess == null) {
                    this.mPreprocess = PosturePreprocessManager.getInstance();
                }
                reportTentState(true);
                HwVibrator.setHwVibrator(Process.myUid(), SensorFoldStateManager.this.mContext.getPackageName(), "haptic.common.fold");
                if (SensorFoldStateManager.this.mSensorManager != null) {
                    SensorFoldStateManager.this.mSensorManager.hwSetSensorConfig("setDisplayMode::8");
                }
                this.mPreprocess.updatePolicy(2);
            }
        }

        private void exitTentPolicy() {
            if (this.mEnterTentFlag.get()) {
                this.mEnterTentFlag.set(false);
                if (this.mPreprocess == null) {
                    this.mPreprocess = PosturePreprocessManager.getInstance();
                }
                reportTentState(false);
                Slog.i("Fsm_TentSensorProcess", "exitTentPolicy");
                if (SensorFoldStateManager.this.mSensorManager != null) {
                    SensorFoldStateManager.this.mSensorManager.hwSetSensorConfig("setDisplayMode::9");
                }
                this.mPreprocess.updatePolicy(0);
            }
        }
    }
}
