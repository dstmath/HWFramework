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
import java.io.PrintWriter;

final class WirelessChargerDetector {
    private static final boolean DEBUG = false;
    private static final double MAX_GRAVITY = 10.806650161743164d;
    private static final double MIN_GRAVITY = 8.806650161743164d;
    private static final int MIN_SAMPLES = 3;
    private static final double MOVEMENT_ANGLE_COS_THRESHOLD = 0.0d;
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
    private final SensorEventListener mListener;
    private final Object mLock;
    private int mMovingSamples;
    private boolean mMustUpdateRestPosition;
    private boolean mPoweredWirelessly;
    private float mRestX;
    private float mRestY;
    private float mRestZ;
    private final SensorManager mSensorManager;
    private final Runnable mSensorTimeout;
    private final SuspendBlocker mSuspendBlocker;
    private int mTotalSamples;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.power.WirelessChargerDetector.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.power.WirelessChargerDetector.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.power.WirelessChargerDetector.<clinit>():void");
    }

    public WirelessChargerDetector(SensorManager sensorManager, SuspendBlocker suspendBlocker, Handler handler) {
        this.mLock = new Object();
        this.mListener = new SensorEventListener() {
            public void onSensorChanged(SensorEvent event) {
                synchronized (WirelessChargerDetector.this.mLock) {
                    WirelessChargerDetector.this.processSampleLocked(event.values[0], event.values[1], event.values[2]);
                }
            }

            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
        this.mSensorTimeout = new Runnable() {
            public void run() {
                synchronized (WirelessChargerDetector.this.mLock) {
                    WirelessChargerDetector.this.finishDetectionLocked();
                }
            }
        };
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

    public boolean update(boolean isPowered, int plugType, int batteryLevel) {
        boolean z = DEBUG;
        synchronized (this.mLock) {
            boolean wasPoweredWirelessly = this.mPoweredWirelessly;
            if (isPowered && plugType == 4) {
                this.mPoweredWirelessly = true;
                this.mMustUpdateRestPosition = true;
                startDetectionLocked();
            } else {
                this.mPoweredWirelessly = DEBUG;
                if (this.mAtRest) {
                    if (plugType == 0 || plugType == 4) {
                        startDetectionLocked();
                    } else {
                        this.mMustUpdateRestPosition = DEBUG;
                        clearAtRestLocked();
                    }
                }
            }
            if (this.mPoweredWirelessly && !wasPoweredWirelessly && batteryLevel < WIRELESS_CHARGER_TURN_ON_BATTERY_LEVEL_LIMIT && !this.mAtRest) {
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

    private void finishDetectionLocked() {
        if (this.mDetectionInProgress) {
            this.mSensorManager.unregisterListener(this.mListener);
            this.mHandler.removeCallbacks(this.mSensorTimeout);
            if (this.mMustUpdateRestPosition) {
                clearAtRestLocked();
                if (this.mTotalSamples < MIN_SAMPLES) {
                    Slog.w(TAG, "Wireless charger detector is broken.  Only received " + this.mTotalSamples + " samples from the gravity sensor but we " + "need at least " + MIN_SAMPLES + " and we expect to see " + "about " + 16 + " on average.");
                } else if (this.mMovingSamples == 0) {
                    this.mAtRest = true;
                    this.mRestX = this.mLastSampleX;
                    this.mRestY = this.mLastSampleY;
                    this.mRestZ = this.mLastSampleZ;
                }
                this.mMustUpdateRestPosition = DEBUG;
            }
            this.mDetectionInProgress = DEBUG;
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
        this.mAtRest = DEBUG;
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
        return dotProduct < (mag1 * mag2) * MOVEMENT_ANGLE_COS_THRESHOLD ? true : DEBUG;
    }
}
