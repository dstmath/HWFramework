package com.android.server;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
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

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    private final Object mLock = new Object();
    private boolean mMeasurementInProgress;
    private final Runnable mMeasurementTimeout = new Runnable() {
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
    private WakeLock mWakeLock;
    private final Runnable mWakelockTimeout = new Runnable() {
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

    interface DeviceIdleCallback {
        void onAnyMotionResult(int i);
    }

    private static class RunningSignalStats {
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
                if (this.previousVector != null) {
                    Vector3 dv = this.currentVector.minus(this.previousVector);
                    this.energy += ((dv.x * dv.x) + (dv.y * dv.y)) + (dv.z * dv.z);
                }
            }
        }

        public Vector3 getRunningAverage() {
            if (this.sampleCount > 0) {
                return this.runningSum.times(1.0f / ((float) this.sampleCount));
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
            return ((("" + "previousVector = " + (this.previousVector == null ? "null" : this.previousVector.toString())) + ", currentVector = " + (this.currentVector == null ? "null" : this.currentVector.toString())) + ", sampleCount = " + this.sampleCount) + ", energy = " + this.energy;
        }
    }

    public static final class Vector3 {
        public long timeMillisSinceBoot;
        public float x;
        public float y;
        public float z;

        public Vector3(long timeMillisSinceBoot, float x, float y, float z) {
            this.timeMillisSinceBoot = timeMillisSinceBoot;
            this.x = x;
            this.y = y;
            this.z = z;
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
            return new Vector3(v.timeMillisSinceBoot, (this.y * v.z) - (this.z * v.y), (this.z * v.x) - (this.x * v.z), (this.x * v.y) - (this.y * v.x));
        }

        public String toString() {
            return ((("" + "timeMillisSinceBoot=" + this.timeMillisSinceBoot) + " | x=" + this.x) + ", y=" + this.y) + ", z=" + this.z;
        }

        public float dotProduct(Vector3 v) {
            return ((this.x * v.x) + (this.y * v.y)) + (this.z * v.z);
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

    public void checkForAnyMotion() {
        if (this.mState != 1) {
            synchronized (this.mLock) {
                this.mState = 1;
                this.mCurrentGravityVector = null;
                this.mPreviousGravityVector = null;
                this.mWakeLock.acquire();
                this.mHandler.sendMessageDelayed(Message.obtain(this.mHandler, this.mWakelockTimeout), WAKELOCK_TIMEOUT_MILLIS);
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

    private void startOrientationMeasurementLocked() {
        if (!this.mMeasurementInProgress && this.mAccelSensor != null) {
            if (this.mSensorManager.registerListener(this.mListener, this.mAccelSensor, EventLogTags.VOLUME_CHANGED)) {
                this.mMeasurementInProgress = true;
                this.mRunningStats.reset();
            }
            this.mHandler.sendMessageDelayed(Message.obtain(this.mHandler, this.mMeasurementTimeout), ACCELEROMETER_DATA_TIMEOUT_MILLIS);
            this.mMeasurementTimeoutIsActive = true;
        }
    }

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
        if (this.mPreviousGravityVector == null || this.mCurrentGravityVector == null) {
            return -1;
        }
        float angle = this.mPreviousGravityVector.normalized().angleBetween(this.mCurrentGravityVector.normalized());
        if (angle >= this.mThresholdAngle || this.mRunningStats.getEnergy() >= 5.0f) {
            return (!Float.isNaN(angle) && this.mCurrentGravityVector.timeMillisSinceBoot - this.mPreviousGravityVector.timeMillisSinceBoot > JobStatus.DEFAULT_TRIGGER_MAX_DELAY) ? -1 : 1;
        } else {
            return 0;
        }
    }
}
