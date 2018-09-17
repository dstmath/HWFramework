package com.android.server.power;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Slog;
import android.util.TimeUtils;
import android.util.proto.ProtoOutputStream;
import java.io.PrintWriter;

final class WirelessChargerDetector {
    private static final boolean DEBUG = false;
    private static final double MAX_GRAVITY = 10.806650161743164d;
    private static final double MIN_GRAVITY = 8.806650161743164d;
    private static final int MIN_SAMPLES = 3;
    private static final double MOVEMENT_ANGLE_COS_THRESHOLD = Math.cos(0.08726646259971647d);
    private static final int SAMPLING_INTERVAL_MILLIS = 50;
    private static final long SETTLE_TIME_MILLIS = 800;
    private static final String TAG = "WirelessChargerDetector";
    private static final int WIRELESS_CHARGER_TURN_ON_BATTERY_LEVEL_LIMIT = 95;
    private boolean mAtRest;
    private boolean mDetectionInProgress;
    private long mDetectionStartTime;
    private float mFirstSampleX;
    private float mFirstSampleY;
    private float mFirstSampleZ;
    private Sensor mGravitySensor;
    private final Handler mHandler;
    private float mLastSampleX;
    private float mLastSampleY;
    private float mLastSampleZ;
    private final SensorEventListener mListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            synchronized (WirelessChargerDetector.this.mLock) {
                WirelessChargerDetector.this.processSampleLocked(event.values[0], event.values[1], event.values[2]);
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    private final Object mLock = new Object();
    private int mMovingSamples;
    private boolean mMustUpdateRestPosition;
    private boolean mPoweredWirelessly;
    private float mRestX;
    private float mRestY;
    private float mRestZ;
    private final SensorManager mSensorManager;
    private final Runnable mSensorTimeout = new Runnable() {
        public void run() {
            synchronized (WirelessChargerDetector.this.mLock) {
                WirelessChargerDetector.this.finishDetectionLocked();
            }
        }
    };
    private final SuspendBlocker mSuspendBlocker;
    private int mTotalSamples;

    public WirelessChargerDetector(SensorManager sensorManager, SuspendBlocker suspendBlocker, Handler handler) {
        this.mSensorManager = sensorManager;
        this.mSuspendBlocker = suspendBlocker;
        this.mHandler = handler;
        this.mGravitySensor = sensorManager.getDefaultSensor(9);
    }

    public void dump(PrintWriter pw) {
        synchronized (this.mLock) {
            String str;
            pw.println();
            pw.println("Wireless Charger Detector State:");
            pw.println("  mGravitySensor=" + this.mGravitySensor);
            pw.println("  mPoweredWirelessly=" + this.mPoweredWirelessly);
            pw.println("  mAtRest=" + this.mAtRest);
            pw.println("  mRestX=" + this.mRestX + ", mRestY=" + this.mRestY + ", mRestZ=" + this.mRestZ);
            pw.println("  mDetectionInProgress=" + this.mDetectionInProgress);
            StringBuilder append = new StringBuilder().append("  mDetectionStartTime=");
            if (this.mDetectionStartTime == 0) {
                str = "0 (never)";
            } else {
                str = TimeUtils.formatUptime(this.mDetectionStartTime);
            }
            pw.println(append.append(str).toString());
            pw.println("  mMustUpdateRestPosition=" + this.mMustUpdateRestPosition);
            pw.println("  mTotalSamples=" + this.mTotalSamples);
            pw.println("  mMovingSamples=" + this.mMovingSamples);
            pw.println("  mFirstSampleX=" + this.mFirstSampleX + ", mFirstSampleY=" + this.mFirstSampleY + ", mFirstSampleZ=" + this.mFirstSampleZ);
            pw.println("  mLastSampleX=" + this.mLastSampleX + ", mLastSampleY=" + this.mLastSampleY + ", mLastSampleZ=" + this.mLastSampleZ);
        }
    }

    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        long wcdToken = proto.start(fieldId);
        synchronized (this.mLock) {
            proto.write(1155346202625L, this.mPoweredWirelessly);
            proto.write(1155346202626L, this.mAtRest);
            long restVectorToken = proto.start(1172526071811L);
            proto.write(1108101562369L, this.mRestX);
            proto.write(1108101562370L, this.mRestY);
            proto.write(1108101562371L, this.mRestZ);
            proto.end(restVectorToken);
            proto.write(1155346202628L, this.mDetectionInProgress);
            proto.write(1116691496965L, this.mDetectionStartTime);
            proto.write(1155346202630L, this.mMustUpdateRestPosition);
            proto.write(1112396529671L, this.mTotalSamples);
            proto.write(1112396529672L, this.mMovingSamples);
            long firstSampleVectorToken = proto.start(1172526071817L);
            proto.write(1108101562369L, this.mFirstSampleX);
            proto.write(1108101562370L, this.mFirstSampleY);
            proto.write(1108101562371L, this.mFirstSampleZ);
            proto.end(firstSampleVectorToken);
            long lastSampleVectorToken = proto.start(1172526071818L);
            proto.write(1108101562369L, this.mLastSampleX);
            proto.write(1108101562370L, this.mLastSampleY);
            proto.write(1108101562371L, this.mLastSampleZ);
            proto.end(lastSampleVectorToken);
        }
        proto.end(wcdToken);
    }

    public boolean update(boolean isPowered, int plugType, int batteryLevel) {
        boolean z = false;
        synchronized (this.mLock) {
            boolean wasPoweredWirelessly = this.mPoweredWirelessly;
            if (isPowered && plugType == 4) {
                this.mPoweredWirelessly = true;
                this.mMustUpdateRestPosition = true;
                startDetectionLocked();
            } else {
                this.mPoweredWirelessly = false;
                if (this.mAtRest) {
                    if (plugType == 0 || plugType == 4) {
                        startDetectionLocked();
                    } else {
                        this.mMustUpdateRestPosition = false;
                        clearAtRestLocked();
                    }
                }
            }
            if (this.mPoweredWirelessly && (wasPoweredWirelessly ^ 1) != 0 && batteryLevel < WIRELESS_CHARGER_TURN_ON_BATTERY_LEVEL_LIMIT) {
                z = this.mAtRest ^ 1;
            }
        }
        return z;
    }

    private void startDetectionLocked() {
        if (!this.mDetectionInProgress && this.mGravitySensor != null && this.mSensorManager.registerListener(this.mListener, this.mGravitySensor, 50000)) {
            this.mSuspendBlocker.acquire();
            this.mDetectionInProgress = true;
            this.mDetectionStartTime = SystemClock.uptimeMillis();
            this.mTotalSamples = 0;
            this.mMovingSamples = 0;
            Message msg = Message.obtain(this.mHandler, this.mSensorTimeout);
            msg.setAsynchronous(true);
            this.mHandler.sendMessageDelayed(msg, SETTLE_TIME_MILLIS);
        }
    }

    private void finishDetectionLocked() {
        if (this.mDetectionInProgress) {
            this.mSensorManager.unregisterListener(this.mListener);
            this.mHandler.removeCallbacks(this.mSensorTimeout);
            if (this.mMustUpdateRestPosition) {
                clearAtRestLocked();
                if (this.mTotalSamples < 3) {
                    Slog.w(TAG, "Wireless charger detector is broken.  Only received " + this.mTotalSamples + " samples from the gravity sensor but we " + "need at least " + 3 + " and we expect to see " + "about " + 16 + " on average.");
                } else if (this.mMovingSamples == 0) {
                    this.mAtRest = true;
                    this.mRestX = this.mLastSampleX;
                    this.mRestY = this.mLastSampleY;
                    this.mRestZ = this.mLastSampleZ;
                }
                this.mMustUpdateRestPosition = false;
            }
            this.mDetectionInProgress = false;
            this.mSuspendBlocker.release();
        }
    }

    private void processSampleLocked(float x, float y, float z) {
        if (this.mDetectionInProgress) {
            this.mLastSampleX = x;
            this.mLastSampleY = y;
            this.mLastSampleZ = z;
            this.mTotalSamples++;
            if (this.mTotalSamples == 1) {
                this.mFirstSampleX = x;
                this.mFirstSampleY = y;
                this.mFirstSampleZ = z;
            } else if (hasMoved(this.mFirstSampleX, this.mFirstSampleY, this.mFirstSampleZ, x, y, z)) {
                this.mMovingSamples++;
            }
            if (this.mAtRest && hasMoved(this.mRestX, this.mRestY, this.mRestZ, x, y, z)) {
                clearAtRestLocked();
            }
        }
    }

    private void clearAtRestLocked() {
        this.mAtRest = false;
        this.mRestX = 0.0f;
        this.mRestY = 0.0f;
        this.mRestZ = 0.0f;
    }

    private static boolean hasMoved(float x1, float y1, float z1, float x2, float y2, float z2) {
        double dotProduct = (double) (((x1 * x2) + (y1 * y2)) + (z1 * z2));
        double mag1 = Math.sqrt((double) (((x1 * x1) + (y1 * y1)) + (z1 * z1)));
        double mag2 = Math.sqrt((double) (((x2 * x2) + (y2 * y2)) + (z2 * z2)));
        if (mag1 < MIN_GRAVITY || mag1 > MAX_GRAVITY || mag2 < MIN_GRAVITY || mag2 > MAX_GRAVITY) {
            return true;
        }
        return dotProduct < (mag1 * mag2) * MOVEMENT_ANGLE_COS_THRESHOLD;
    }
}
