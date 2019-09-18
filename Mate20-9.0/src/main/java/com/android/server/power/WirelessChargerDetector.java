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
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
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
        String str;
        synchronized (this.mLock) {
            pw.println();
            pw.println("Wireless Charger Detector State:");
            pw.println("  mGravitySensor=" + this.mGravitySensor);
            pw.println("  mPoweredWirelessly=" + this.mPoweredWirelessly);
            pw.println("  mAtRest=" + this.mAtRest);
            pw.println("  mRestX=" + this.mRestX + ", mRestY=" + this.mRestY + ", mRestZ=" + this.mRestZ);
            StringBuilder sb = new StringBuilder();
            sb.append("  mDetectionInProgress=");
            sb.append(this.mDetectionInProgress);
            pw.println(sb.toString());
            StringBuilder sb2 = new StringBuilder();
            sb2.append("  mDetectionStartTime=");
            if (this.mDetectionStartTime == 0) {
                str = "0 (never)";
            } else {
                str = TimeUtils.formatUptime(this.mDetectionStartTime);
            }
            sb2.append(str);
            pw.println(sb2.toString());
            pw.println("  mMustUpdateRestPosition=" + this.mMustUpdateRestPosition);
            pw.println("  mTotalSamples=" + this.mTotalSamples);
            pw.println("  mMovingSamples=" + this.mMovingSamples);
            pw.println("  mFirstSampleX=" + this.mFirstSampleX + ", mFirstSampleY=" + this.mFirstSampleY + ", mFirstSampleZ=" + this.mFirstSampleZ);
            pw.println("  mLastSampleX=" + this.mLastSampleX + ", mLastSampleY=" + this.mLastSampleY + ", mLastSampleZ=" + this.mLastSampleZ);
        }
    }

    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        ProtoOutputStream protoOutputStream = proto;
        long wcdToken = proto.start(fieldId);
        synchronized (this.mLock) {
            protoOutputStream.write(1133871366145L, this.mPoweredWirelessly);
            protoOutputStream.write(1133871366146L, this.mAtRest);
            long restVectorToken = protoOutputStream.start(1146756268035L);
            protoOutputStream.write(1108101562369L, this.mRestX);
            protoOutputStream.write(1108101562370L, this.mRestY);
            protoOutputStream.write(1108101562371L, this.mRestZ);
            protoOutputStream.end(restVectorToken);
            protoOutputStream.write(1133871366148L, this.mDetectionInProgress);
            protoOutputStream.write(1112396529669L, this.mDetectionStartTime);
            protoOutputStream.write(1133871366150L, this.mMustUpdateRestPosition);
            protoOutputStream.write(1120986464263L, this.mTotalSamples);
            protoOutputStream.write(1120986464264L, this.mMovingSamples);
            long firstSampleVectorToken = protoOutputStream.start(1146756268041L);
            protoOutputStream.write(1108101562369L, this.mFirstSampleX);
            protoOutputStream.write(1108101562370L, this.mFirstSampleY);
            protoOutputStream.write(1108101562371L, this.mFirstSampleZ);
            protoOutputStream.end(firstSampleVectorToken);
            long lastSampleVectorToken = protoOutputStream.start(1146756268042L);
            protoOutputStream.write(1108101562369L, this.mLastSampleX);
            protoOutputStream.write(1108101562370L, this.mLastSampleY);
            protoOutputStream.write(1108101562371L, this.mLastSampleZ);
            protoOutputStream.end(lastSampleVectorToken);
        }
        protoOutputStream.end(wcdToken);
    }

    public boolean update(boolean isPowered, int plugType) {
        boolean z;
        synchronized (this.mLock) {
            boolean wasPoweredWirelessly = this.mPoweredWirelessly;
            z = false;
            if (!isPowered || plugType != 4) {
                this.mPoweredWirelessly = false;
                if (this.mAtRest) {
                    if (plugType == 0 || plugType == 4) {
                        startDetectionLocked();
                    } else {
                        this.mMustUpdateRestPosition = false;
                        clearAtRestLocked();
                    }
                }
            } else {
                this.mPoweredWirelessly = true;
                this.mMustUpdateRestPosition = true;
                startDetectionLocked();
            }
            if (this.mPoweredWirelessly && !wasPoweredWirelessly && !this.mAtRest) {
                z = true;
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

    /* access modifiers changed from: private */
    public void finishDetectionLocked() {
        if (this.mDetectionInProgress) {
            this.mSensorManager.unregisterListener(this.mListener);
            this.mHandler.removeCallbacks(this.mSensorTimeout);
            if (this.mMustUpdateRestPosition) {
                clearAtRestLocked();
                if (this.mTotalSamples < 3) {
                    Slog.w(TAG, "Wireless charger detector is broken.  Only received " + this.mTotalSamples + " samples from the gravity sensor but we need at least " + 3 + " and we expect to see about " + 16 + " on average.");
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

    /* access modifiers changed from: private */
    public void processSampleLocked(float x, float y, float z) {
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
        double dotProduct = (double) ((x1 * x2) + (y1 * y2) + (z1 * z2));
        double mag1 = Math.sqrt((double) ((x1 * x1) + (y1 * y1) + (z1 * z1)));
        double mag2 = Math.sqrt((double) ((x2 * x2) + (y2 * y2) + (z2 * z2)));
        boolean moved = true;
        if (mag1 < MIN_GRAVITY || mag1 > MAX_GRAVITY || mag2 < MIN_GRAVITY || mag2 > MAX_GRAVITY) {
            return true;
        }
        if (dotProduct >= mag1 * mag2 * MOVEMENT_ANGLE_COS_THRESHOLD) {
            moved = false;
        }
        return moved;
    }
}
