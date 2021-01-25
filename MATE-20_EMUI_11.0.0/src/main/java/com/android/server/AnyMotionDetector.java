package com.android.server;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Slog;
import com.android.server.job.controllers.JobStatus;

public class AnyMotionDetector {
    private static final long ACCELEROMETER_DATA_TIMEOUT_MILLIS = 3000;
    private static final boolean DEBUG = false;
    private static final long ORIENTATION_MEASUREMENT_DURATION_MILLIS = 2500;
    private static final long ORIENTATION_MEASUREMENT_INTERVAL_MILLIS = 5000;
    public static final int RESULT_MOVED = 1;
    public static final int RESULT_STATIONARY = 0;
    public static final int RESULT_UNKNOWN = -1;
    private static final int SAMPLING_INTERVAL_MILLIS = 40;
    private static final int STALE_MEASUREMENT_TIMEOUT_MILLIS = 120000;
    private static final int STATE_ACTIVE = 1;
    private static final int STATE_INACTIVE = 0;
    private static final String TAG = "AnyMotionDetector";
    private static final long WAKELOCK_TIMEOUT_MILLIS = 30000;
    private final float THRESHOLD_ENERGY = 5.0f;
    private Sensor mAccelSensor;
    private DeviceIdleCallback mCallback = null;
    private Vector3 mCurrentGravityVector = null;
    private final Handler mHandler;
    private final SensorEventListener mListener = new SensorEventListener() {
        /* class com.android.server.AnyMotionDetector.AnonymousClass1 */

        @Override // android.hardware.SensorEventListener
        public void onSensorChanged(SensorEvent event) {
            int status = -1;
            synchronized (AnyMotionDetector.this.mLock) {
                AnyMotionDetector.this.mRunningStats.accumulate(new Vector3(SystemClock.elapsedRealtime(), event.values[0], event.values[1], event.values[2]));
                if (AnyMotionDetector.this.mRunningStats.getSampleCount() >= AnyMotionDetector.this.mNumSufficientSamples) {
                    status = AnyMotionDetector.this.stopOrientationMeasurementLocked();
                }
            }
            if (status != -1) {
                AnyMotionDetector.this.mHandler.removeCallbacks(AnyMotionDetector.this.mWakelockTimeout);
                AnyMotionDetector.this.mWakelockTimeoutIsActive = false;
                AnyMotionDetector.this.mCallback.onAnyMotionResult(status);
            }
        }

        @Override // android.hardware.SensorEventListener
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    private final Object mLock = new Object();
    private boolean mMeasurementInProgress;
    private final Runnable mMeasurementTimeout = new Runnable() {
        /* class com.android.server.AnyMotionDetector.AnonymousClass3 */

        @Override // java.lang.Runnable
        public void run() {
            synchronized (AnyMotionDetector.this.mLock) {
                if (AnyMotionDetector.this.mMeasurementTimeoutIsActive) {
                    AnyMotionDetector.this.mMeasurementTimeoutIsActive = false;
                    int status = AnyMotionDetector.this.stopOrientationMeasurementLocked();
                    if (status != -1) {
                        AnyMotionDetector.this.mHandler.removeCallbacks(AnyMotionDetector.this.mWakelockTimeout);
                        AnyMotionDetector.this.mWakelockTimeoutIsActive = false;
                        AnyMotionDetector.this.mCallback.onAnyMotionResult(status);
                    }
                }
            }
        }
    };
    private boolean mMeasurementTimeoutIsActive;
    private int mNumSufficientSamples;
    private Vector3 mPreviousGravityVector = null;
    private RunningSignalStats mRunningStats;
    private SensorManager mSensorManager;
    private final Runnable mSensorRestart = new Runnable() {
        /* class com.android.server.AnyMotionDetector.AnonymousClass2 */

        @Override // java.lang.Runnable
        public void run() {
            synchronized (AnyMotionDetector.this.mLock) {
                if (AnyMotionDetector.this.mSensorRestartIsActive) {
                    AnyMotionDetector.this.mSensorRestartIsActive = false;
                    AnyMotionDetector.this.startOrientationMeasurementLocked();
                }
            }
        }
    };
    private boolean mSensorRestartIsActive;
    private int mState;
    private final float mThresholdAngle;
    private PowerManager.WakeLock mWakeLock;
    private final Runnable mWakelockTimeout = new Runnable() {
        /* class com.android.server.AnyMotionDetector.AnonymousClass4 */

        @Override // java.lang.Runnable
        public void run() {
            synchronized (AnyMotionDetector.this.mLock) {
                if (AnyMotionDetector.this.mWakelockTimeoutIsActive) {
                    AnyMotionDetector.this.mWakelockTimeoutIsActive = false;
                    AnyMotionDetector.this.stop();
                }
            }
        }
    };
    private boolean mWakelockTimeoutIsActive;

    /* access modifiers changed from: package-private */
    public interface DeviceIdleCallback {
        void onAnyMotionResult(int i);
    }

    public AnyMotionDetector(PowerManager pm, Handler handler, SensorManager sm, DeviceIdleCallback callback, float thresholdAngle) {
        synchronized (this.mLock) {
            this.mWakeLock = pm.newWakeLock(1, TAG);
            this.mWakeLock.setReferenceCounted(false);
            this.mHandler = handler;
            this.mSensorManager = sm;
            this.mAccelSensor = this.mSensorManager.getDefaultSensor(1);
            this.mMeasurementInProgress = false;
            this.mMeasurementTimeoutIsActive = false;
            this.mWakelockTimeoutIsActive = false;
            this.mSensorRestartIsActive = false;
            this.mState = 0;
            this.mCallback = callback;
            this.mThresholdAngle = thresholdAngle;
            this.mRunningStats = new RunningSignalStats();
            this.mNumSufficientSamples = (int) Math.ceil(62.5d);
        }
    }

    public boolean hasSensor() {
        return this.mAccelSensor != null;
    }

    public void checkForAnyMotion() {
        if (this.mState != 1) {
            synchronized (this.mLock) {
                this.mState = 1;
                this.mCurrentGravityVector = null;
                this.mPreviousGravityVector = null;
                this.mWakeLock.acquire();
                this.mHandler.sendMessageDelayed(Message.obtain(this.mHandler, this.mWakelockTimeout), 30000);
                this.mWakelockTimeoutIsActive = true;
                startOrientationMeasurementLocked();
            }
        }
    }

    public void stop() {
        synchronized (this.mLock) {
            if (this.mState == 1) {
                this.mState = 0;
            }
            this.mHandler.removeCallbacks(this.mMeasurementTimeout);
            this.mHandler.removeCallbacks(this.mSensorRestart);
            this.mMeasurementTimeoutIsActive = false;
            this.mSensorRestartIsActive = false;
            if (this.mMeasurementInProgress) {
                this.mMeasurementInProgress = false;
                this.mSensorManager.unregisterListener(this.mListener);
            }
            this.mCurrentGravityVector = null;
            this.mPreviousGravityVector = null;
            if (this.mWakeLock.isHeld()) {
                this.mHandler.removeCallbacks(this.mWakelockTimeout);
                this.mWakelockTimeoutIsActive = false;
                this.mWakeLock.release();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startOrientationMeasurementLocked() {
        Sensor sensor;
        if (!this.mMeasurementInProgress && (sensor = this.mAccelSensor) != null) {
            if (this.mSensorManager.registerListener(this.mListener, sensor, EventLogTags.VOLUME_CHANGED)) {
                this.mMeasurementInProgress = true;
                this.mRunningStats.reset();
            }
            this.mHandler.sendMessageDelayed(Message.obtain(this.mHandler, this.mMeasurementTimeout), 3000);
            this.mMeasurementTimeoutIsActive = true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int stopOrientationMeasurementLocked() {
        int status = -1;
        if (this.mMeasurementInProgress) {
            this.mHandler.removeCallbacks(this.mMeasurementTimeout);
            this.mMeasurementTimeoutIsActive = false;
            this.mSensorManager.unregisterListener(this.mListener);
            this.mMeasurementInProgress = false;
            this.mPreviousGravityVector = this.mCurrentGravityVector;
            this.mCurrentGravityVector = this.mRunningStats.getRunningAverage();
            if (this.mRunningStats.getSampleCount() == 0) {
                Slog.w(TAG, "No accelerometer data acquired for orientation measurement.");
            }
            this.mRunningStats.reset();
            status = getStationaryStatus();
            if (status != -1) {
                if (this.mWakeLock.isHeld()) {
                    this.mHandler.removeCallbacks(this.mWakelockTimeout);
                    this.mWakelockTimeoutIsActive = false;
                    this.mWakeLock.release();
                }
                this.mState = 0;
            } else {
                this.mHandler.sendMessageDelayed(Message.obtain(this.mHandler, this.mSensorRestart), ORIENTATION_MEASUREMENT_INTERVAL_MILLIS);
                this.mSensorRestartIsActive = true;
            }
        }
        return status;
    }

    public int getStationaryStatus() {
        Vector3 vector3 = this.mPreviousGravityVector;
        if (vector3 == null || this.mCurrentGravityVector == null) {
            return -1;
        }
        float angle = vector3.normalized().angleBetween(this.mCurrentGravityVector.normalized());
        if (angle < this.mThresholdAngle && this.mRunningStats.getEnergy() < 5.0f) {
            return 0;
        }
        if (!Float.isNaN(angle) && this.mCurrentGravityVector.timeMillisSinceBoot - this.mPreviousGravityVector.timeMillisSinceBoot > JobStatus.DEFAULT_TRIGGER_MAX_DELAY) {
            return -1;
        }
        return 1;
    }

    public static final class Vector3 {
        public long timeMillisSinceBoot;
        public float x;
        public float y;
        public float z;

        public Vector3(long timeMillisSinceBoot2, float x2, float y2, float z2) {
            this.timeMillisSinceBoot = timeMillisSinceBoot2;
            this.x = x2;
            this.y = y2;
            this.z = z2;
        }

        public float norm() {
            return (float) Math.sqrt((double) dotProduct(this));
        }

        public Vector3 normalized() {
            float mag = norm();
            return new Vector3(this.timeMillisSinceBoot, this.x / mag, this.y / mag, this.z / mag);
        }

        public float angleBetween(Vector3 other) {
            float degrees = Math.abs((float) Math.toDegrees(Math.atan2((double) cross(other).norm(), (double) dotProduct(other))));
            Slog.d(AnyMotionDetector.TAG, "angleBetween: this = " + toString() + ", other = " + other.toString() + ", degrees = " + degrees);
            return degrees;
        }

        public Vector3 cross(Vector3 v) {
            long j = v.timeMillisSinceBoot;
            float f = this.y;
            float f2 = v.z;
            float f3 = this.z;
            float f4 = v.y;
            float f5 = (f * f2) - (f3 * f4);
            float f6 = v.x;
            float f7 = this.x;
            return new Vector3(j, f5, (f3 * f6) - (f2 * f7), (f7 * f4) - (f * f6));
        }

        public String toString() {
            return ((("timeMillisSinceBoot=" + this.timeMillisSinceBoot) + " | x=" + this.x) + ", y=" + this.y) + ", z=" + this.z;
        }

        public float dotProduct(Vector3 v) {
            return (this.x * v.x) + (this.y * v.y) + (this.z * v.z);
        }

        public Vector3 times(float val) {
            return new Vector3(this.timeMillisSinceBoot, this.x * val, this.y * val, this.z * val);
        }

        public Vector3 plus(Vector3 v) {
            return new Vector3(v.timeMillisSinceBoot, v.x + this.x, v.y + this.y, v.z + this.z);
        }

        public Vector3 minus(Vector3 v) {
            return new Vector3(v.timeMillisSinceBoot, this.x - v.x, this.y - v.y, this.z - v.z);
        }
    }

    /* access modifiers changed from: private */
    public static class RunningSignalStats {
        Vector3 currentVector;
        float energy;
        Vector3 previousVector;
        Vector3 runningSum;
        int sampleCount;

        public RunningSignalStats() {
            reset();
        }

        public void reset() {
            this.previousVector = null;
            this.currentVector = null;
            this.runningSum = new Vector3(0, 0.0f, 0.0f, 0.0f);
            this.energy = 0.0f;
            this.sampleCount = 0;
        }

        public void accumulate(Vector3 v) {
            if (v != null) {
                this.sampleCount++;
                this.runningSum = this.runningSum.plus(v);
                this.previousVector = this.currentVector;
                this.currentVector = v;
                Vector3 vector3 = this.previousVector;
                if (vector3 != null) {
                    Vector3 dv = this.currentVector.minus(vector3);
                    this.energy += (dv.x * dv.x) + (dv.y * dv.y) + (dv.z * dv.z);
                }
            }
        }

        public Vector3 getRunningAverage() {
            int i = this.sampleCount;
            if (i > 0) {
                return this.runningSum.times(1.0f / ((float) i));
            }
            return null;
        }

        public float getEnergy() {
            return this.energy;
        }

        public int getSampleCount() {
            return this.sampleCount;
        }

        public String toString() {
            Vector3 vector3 = this.currentVector;
            String previousVectorString = "null";
            String currentVectorString = vector3 == null ? previousVectorString : vector3.toString();
            Vector3 vector32 = this.previousVector;
            if (vector32 != null) {
                previousVectorString = vector32.toString();
            }
            return ((("previousVector = " + previousVectorString) + ", currentVector = " + currentVectorString) + ", sampleCount = " + this.sampleCount) + ", energy = " + this.energy;
        }
    }
}
