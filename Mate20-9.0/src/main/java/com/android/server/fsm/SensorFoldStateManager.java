package com.android.server.fsm;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Slog;

class SensorFoldStateManager {
    private static final int MSG_HANDLE_FOLD_STATE_SENSOR = 0;
    private static final int SENSOR_RATE = 50000;
    private static final int STABLE_POSTURE_TIMES = 3;
    private static final String TAG = "Fsm_SensorFoldStateManager";
    private ISensorPostureCallback mCallback;
    private Context mContext;
    private final SensorEventListener mFoldStateSensorListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            long uptimeMillis = SystemClock.uptimeMillis();
            if (event.values.length == 7) {
                SensorFoldStateManager.this.mHandler.removeMessages(0);
                Message msg = SensorFoldStateManager.this.mHandler.obtainMessage(0);
                msg.obj = event.values;
                SensorFoldStateManager.this.mHandler.sendMessage(msg);
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    /* access modifiers changed from: private */
    public SensorHandler mHandler;
    private HandlerThread mHandlerThread;
    private boolean mInitPortraitState = true;
    private boolean mIsMagnWakeUp = false;
    private final Object mLock = new Object();
    private Sensor mPostureSensor;
    private int mRecentState = 0;
    private int mRrecentStateTimes = 0;
    private SensorManager mSensorManager;
    private int mState = 0;

    private final class SensorHandler extends Handler {
        public SensorHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            if (msg.what != 0) {
                Slog.e("Fsm_SensorFoldStateManager", "Invalid message");
                return;
            }
            SensorFoldStateManager.this.handleFoldStateSensor((float[]) msg.obj);
        }
    }

    SensorFoldStateManager(Context context) {
        Slog.i("Fsm_SensorFoldStateManager", "SensorFoldStateManager init");
        this.mContext = context;
        this.mHandlerThread = new HandlerThread("Fsm_SensorFoldStateManager");
        this.mHandlerThread.start();
        this.mHandler = new SensorHandler(this.mHandlerThread.getLooper());
        this.mSensorManager = (SensorManager) this.mContext.getSystemService("sensor");
        this.mPostureSensor = this.mSensorManager.getDefaultSensor(65573);
    }

    public boolean turnOnFoldStateSensor(ISensorPostureCallback callback, int wakeUpType) {
        boolean z = false;
        if (callback == null) {
            Slog.i("Fsm_SensorFoldStateManager", "turnOnFoldStateSensor callback is null");
            return false;
        }
        synchronized (this.mLock) {
            boolean z2 = true;
            if (this.mCallback == null) {
                this.mSensorManager.registerListener(this.mFoldStateSensorListener, this.mPostureSensor, SENSOR_RATE, null);
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
            this.mRecentState = 0;
            this.mRrecentStateTimes = 0;
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
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0059, code lost:
        r0 = r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x005a, code lost:
        return;
     */
    public void handleFoldStateSensor(float[] data) {
        int preState;
        synchronized (this.mLock) {
            if (this.mInitPortraitState) {
                this.mInitPortraitState = SensorPostureProcess.isPortraitState(data);
            }
            if (this.mCallback != null) {
                if (this.mInitPortraitState) {
                    Slog.i("Fsm_SensorFoldStateManager", "init state is Portrait");
                    preState = 3;
                } else {
                    preState = SensorPostureProcess.getFoldableState(data[6], this.mState);
                }
                if (preState == this.mRecentState) {
                    this.mRrecentStateTimes++;
                    if (this.mRrecentStateTimes == 3) {
                        this.mRrecentStateTimes = 0;
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
                } else {
                    this.mRecentState = preState;
                    this.mRrecentStateTimes = 1;
                }
            }
        }
    }

    private int transFoldStateToPosture(int state) {
        switch (state) {
            case 1:
                return 109;
            case 2:
                return 103;
            case 3:
                return 106;
            default:
                return 100;
        }
    }
}
