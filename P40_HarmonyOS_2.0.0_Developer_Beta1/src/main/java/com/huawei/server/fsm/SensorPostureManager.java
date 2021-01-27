package com.huawei.server.fsm;

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
import com.huawei.android.os.HandlerAdapter;
import com.huawei.android.util.SlogEx;

/* access modifiers changed from: package-private */
public class SensorPostureManager {
    private static final int LOG_TIME = 1000;
    private static final int MSG_HANDLE_POSTURE_SENSOR = 0;
    private static final int POSTURE_SENSOR_LENGTH = 7;
    private static final int SENSOR_RATE = 50000;
    private static final int STABLE_LAY_POSTURE_TIMES = 20;
    private static final String TAG = "Fsm_SensorPostureManager";
    private ISensorPostureCallback mCallback;
    private Context mContext;
    private SensorHandler mHandler;
    private HandlerThread mHandlerThread;
    private boolean mInitPortraitState = true;
    private boolean mIsMagnWakeUp = false;
    private long mLastEventTime = 0;
    private final Object mLock = new Object();
    private MagnetometerWakeupManager mMagnetometerWakeup;
    private int mPosture = 100;
    private Sensor mPostureSensor;
    private final SensorEventListener mPostureSensorListener = new SensorEventListener() {
        /* class com.huawei.server.fsm.SensorPostureManager.AnonymousClass1 */

        @Override // android.hardware.SensorEventListener
        public void onSensorChanged(SensorEvent event) {
            long time = SystemClock.uptimeMillis();
            if (event.values.length == SensorPostureManager.POSTURE_SENSOR_LENGTH) {
                if (time - SensorPostureManager.this.mLastEventTime >= 1000) {
                    SensorPostureProcess.printPostureSensor(event.values);
                    SensorPostureManager.this.mLastEventTime = time;
                }
                SensorPostureManager.this.mHandler.removeMessages(0);
                Message msg = SensorPostureManager.this.mHandler.obtainMessage(0);
                msg.obj = event.values;
                SensorPostureManager.this.mHandler.sendMessage(msg);
            }
        }

        @Override // android.hardware.SensorEventListener
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    private int mRecentPosture = 100;
    private int mRecentPostureTimes = 0;
    private SensorManager mSensorManager;
    private int mTempPosture = 100;

    SensorPostureManager(Context context) {
        SlogEx.i("Fsm_SensorPostureManager", "SensorPostureManager init");
        this.mContext = context;
        this.mHandlerThread = new HandlerThread("Fsm_SensorPostureManager");
        this.mHandlerThread.start();
        this.mHandler = new SensorHandler(this.mHandlerThread.getLooper());
        this.mSensorManager = (SensorManager) this.mContext.getSystemService("sensor");
        this.mPostureSensor = this.mSensorManager.getDefaultSensor(65573);
        this.mMagnetometerWakeup = MagnetometerWakeupManager.getInstance(this.mContext);
    }

    public boolean turnOnPostureSensor(ISensorPostureCallback callback, int wakeUpType) {
        boolean z = false;
        if (callback == null) {
            SlogEx.i("Fsm_SensorPostureManager", "turnOnPostureSensor callback is null");
            return false;
        }
        synchronized (this.mLock) {
            boolean z2 = true;
            if (this.mCallback == null) {
                this.mSensorManager.registerListener(this.mPostureSensorListener, this.mPostureSensor, SENSOR_RATE, (Handler) null);
                if (wakeUpType == 4) {
                    z = true;
                }
                this.mIsMagnWakeUp = z;
                this.mCallback = callback;
                SlogEx.i("Fsm_SensorPostureManager", "registerPostureSensor success");
                return true;
            }
            if (wakeUpType != 4) {
                z2 = false;
            }
            this.mIsMagnWakeUp = z2;
            SlogEx.i("Fsm_SensorPostureManager", "PostureSensor is already registered");
            return false;
        }
    }

    public boolean turnOffPostureSensor(ISensorPostureCallback callback) {
        if (callback == null) {
            SlogEx.i("Fsm_SensorPostureManager", "turnOffPostureSensor callback is null");
            return false;
        }
        synchronized (this.mLock) {
            if (this.mCallback == null || !callback.equals(this.mCallback)) {
                SlogEx.i("Fsm_SensorPostureManager", "PostureSensor is not registered");
                return false;
            }
            this.mCallback = null;
            this.mSensorManager.unregisterListener(this.mPostureSensorListener);
            this.mPosture = 100;
            this.mRecentPosture = 100;
            this.mTempPosture = 100;
            this.mRecentPostureTimes = 0;
            this.mInitPortraitState = true;
            this.mIsMagnWakeUp = false;
            SlogEx.i("Fsm_SensorPostureManager", "unregisterPostureSensor success");
            return true;
        }
    }

    public int getPosture() {
        int i;
        synchronized (this.mLock) {
            i = this.mPosture;
        }
        return i;
    }

    private boolean isStablePostureSensor(float[] data) {
        int prePosture = SensorPostureProcess.handlePostureSensor(data, this.mPosture);
        if (prePosture == 100) {
            prePosture = this.mRecentPosture;
        }
        if (prePosture == this.mRecentPosture) {
            this.mRecentPostureTimes++;
        } else {
            this.mRecentPosture = prePosture;
            this.mRecentPostureTimes = 1;
        }
        if (this.mPosture == 100) {
            this.mTempPosture = prePosture;
            this.mRecentPostureTimes = 0;
            SlogEx.i("Fsm_SensorPostureManager", "Get posture from sensor date in ScrrenOn scene");
            return true;
        } else if ((prePosture == 101 || prePosture == 102) && this.mRecentPostureTimes != STABLE_LAY_POSTURE_TIMES) {
            return false;
        } else {
            this.mTempPosture = prePosture;
            this.mRecentPostureTimes = 0;
            return true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handlePostureSensor(float[] data) {
        synchronized (this.mLock) {
            if (this.mInitPortraitState) {
                this.mInitPortraitState = SensorPostureProcess.isPortraitState(data);
            }
            if (this.mCallback != null) {
                if (this.mInitPortraitState) {
                    SlogEx.i("Fsm_SensorPostureManager", "init state is Portrait");
                    if (this.mMagnetometerWakeup.getHallData() == 1) {
                        this.mTempPosture = 109;
                    } else {
                        this.mTempPosture = 106;
                    }
                } else if (!isStablePostureSensor(data)) {
                    return;
                }
                if (this.mIsMagnWakeUp) {
                    if (this.mTempPosture == 109) {
                        this.mIsMagnWakeUp = false;
                    } else {
                        return;
                    }
                }
                if (this.mTempPosture != this.mPosture) {
                    this.mPosture = this.mTempPosture;
                    int mPostureTemp = this.mPosture;
                    this.mCallback.onPostureChange(mPostureTemp);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public final class SensorHandler extends HandlerAdapter {
        SensorHandler(Looper looper) {
            super(looper, (Handler.Callback) null, true);
        }

        public void handleMessage(Message msg) {
            if (msg.what != 0) {
                SlogEx.e("Fsm_SensorPostureManager", "Invalid message");
                return;
            }
            SensorPostureManager.this.handlePostureSensor((float[]) msg.obj);
        }
    }
}
