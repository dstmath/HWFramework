package com.android.server.policy;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Slog;
import android.util.proto.ProtoOutputStream;
import android.view.Surface;
import com.android.server.HwServiceFactory;
import com.android.server.policy.IHWExtMotionRotationProcessor;
import java.io.PrintWriter;

public abstract class WindowOrientationListener {
    private static final int DEFAULT_BATCH_LATENCY = 100000;
    private static final boolean LOG = SystemProperties.getBoolean("debug.orientation.log", false);
    private static boolean McuSwitch = false;
    private static final String TAG = "WindowOrientationListener";
    private static final boolean USE_GRAVITY_SENSOR = false;
    private int mCurrentRotation;
    private boolean mEnabled;
    private IHWExtMotionRotationProcessor mHWEMRProcessor;
    private Handler mHandler;
    private final Object mLock;
    private OrientationJudge mOrientationJudge;
    private int mRate;
    private Sensor mSensor;
    private SensorManager mSensorManager;
    private String mSensorType;
    private IHWExtMotionRotationProcessor.WindowOrientationListenerProxy mWOLProxy;

    public abstract void onProposedRotationChanged(int i);

    static {
        boolean z = false;
        if ("true".equals(SystemProperties.get("ro.config.hw_sensorhub", "false")) || "true".equals(SystemProperties.get("ro.config.hw_motion_reco_qcom", "false"))) {
            z = true;
        }
        McuSwitch = z;
    }

    public WindowOrientationListener(Context context, Handler handler) {
        this(context, handler, 2);
    }

    private WindowOrientationListener(Context context, Handler handler, int rate) {
        this.mCurrentRotation = -1;
        this.mLock = new Object();
        this.mWOLProxy = new IHWExtMotionRotationProcessor.WindowOrientationListenerProxy() {
            /* class com.android.server.policy.WindowOrientationListener.AnonymousClass1 */

            @Override // com.android.server.policy.IHWExtMotionRotationProcessor.WindowOrientationListenerProxy
            public void setCurrentOrientation(int proposedRotation) {
                WindowOrientationListener.this.mCurrentRotation = proposedRotation;
            }

            @Override // com.android.server.policy.IHWExtMotionRotationProcessor.WindowOrientationListenerProxy
            public void notifyProposedRotation(int proposedRotation) {
                WindowOrientationListener.this.onProposedRotationChanged(proposedRotation);
            }
        };
        if (McuSwitch) {
            this.mHandler = handler;
            this.mHWEMRProcessor = HwServiceFactory.getHWExtMotionRotationProcessor(this.mWOLProxy);
            return;
        }
        this.mHandler = handler;
        this.mSensorManager = (SensorManager) context.getSystemService("sensor");
        this.mRate = rate;
        Sensor wakeUpDeviceOrientationSensor = null;
        Sensor nonWakeUpDeviceOrientationSensor = null;
        for (Sensor s : this.mSensorManager.getSensorList(27)) {
            if (s.isWakeUpSensor()) {
                wakeUpDeviceOrientationSensor = s;
            } else {
                nonWakeUpDeviceOrientationSensor = s;
            }
        }
        if (wakeUpDeviceOrientationSensor != null) {
            this.mSensor = wakeUpDeviceOrientationSensor;
        } else {
            this.mSensor = nonWakeUpDeviceOrientationSensor;
        }
        if (this.mSensor != null) {
            this.mOrientationJudge = new OrientationSensorJudge();
        }
        if (this.mOrientationJudge == null) {
            this.mSensor = this.mSensorManager.getDefaultSensor(1);
            if (this.mSensor != null) {
                this.mOrientationJudge = new AccelSensorJudge(context);
            }
        }
    }

    public void enable() {
        enable(true);
    }

    public void enable(boolean clearCurrentRotation) {
        synchronized (this.mLock) {
            if (!this.mEnabled) {
                if (LOG) {
                    Slog.d(TAG, "WindowOrientationListener enabled clearCurrentRotation=" + clearCurrentRotation);
                }
                if (McuSwitch) {
                    this.mHWEMRProcessor.enableMotionRotation(this.mHandler);
                } else if (this.mSensor == null) {
                    Slog.w(TAG, "Cannot detect sensors. Not enabled");
                    return;
                } else {
                    this.mOrientationJudge.resetLocked(clearCurrentRotation);
                    if (this.mSensor.getType() == 1) {
                        this.mSensorManager.registerListener(this.mOrientationJudge, this.mSensor, this.mRate, DEFAULT_BATCH_LATENCY, this.mHandler);
                    } else {
                        this.mSensorManager.registerListener(this.mOrientationJudge, this.mSensor, this.mRate, this.mHandler);
                    }
                }
                this.mEnabled = true;
            }
        }
    }

    public void disable() {
        synchronized (this.mLock) {
            if (this.mEnabled) {
                if (LOG) {
                    Slog.d(TAG, "WindowOrientationListener disabled");
                }
                if (McuSwitch) {
                    this.mHWEMRProcessor.disableMotionRotation();
                } else if (this.mSensor == null) {
                    Slog.w(TAG, "Cannot detect sensors. Invalid disable");
                    return;
                } else {
                    this.mSensorManager.unregisterListener(this.mOrientationJudge);
                }
                this.mEnabled = false;
            }
        }
    }

    public void onTouchStart() {
        synchronized (this.mLock) {
            if (this.mOrientationJudge != null) {
                this.mOrientationJudge.onTouchStartLocked();
            }
        }
    }

    public void onTouchEnd() {
        long whenElapsedNanos = SystemClock.elapsedRealtimeNanos();
        synchronized (this.mLock) {
            if (this.mOrientationJudge != null) {
                this.mOrientationJudge.onTouchEndLocked(whenElapsedNanos);
            }
        }
    }

    public Handler getHandler() {
        return this.mHandler;
    }

    public void setCurrentRotation(int rotation) {
        synchronized (this.mLock) {
            this.mCurrentRotation = rotation;
        }
    }

    public int getProposedRotation() {
        synchronized (this.mLock) {
            if (!this.mEnabled) {
                return -1;
            }
            if (McuSwitch) {
                return this.mHWEMRProcessor.getProposedRotation();
            }
            return this.mOrientationJudge.getProposedRotationLocked();
        }
    }

    public boolean canDetectOrientation() {
        synchronized (this.mLock) {
            boolean z = true;
            if (McuSwitch) {
                if (this.mHWEMRProcessor == null) {
                    z = false;
                }
                return z;
            }
            if (this.mSensor == null) {
                z = false;
            }
            return z;
        }
    }

    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        long token = proto.start(fieldId);
        synchronized (this.mLock) {
            proto.write(1133871366145L, this.mEnabled);
            proto.write(1159641169922L, this.mCurrentRotation);
        }
        proto.end(token);
    }

    public void dump(PrintWriter pw, String prefix) {
        synchronized (this.mLock) {
            pw.println(prefix + TAG);
            String prefix2 = prefix + "  ";
            pw.println(prefix2 + "mEnabled=" + this.mEnabled);
            pw.println(prefix2 + "mCurrentRotation=" + Surface.rotationToString(this.mCurrentRotation));
            pw.println(prefix2 + "mSensorType=" + this.mSensorType);
            pw.println(prefix2 + "mSensor=" + this.mSensor);
            pw.println(prefix2 + "mRate=" + this.mRate);
            if (this.mOrientationJudge != null) {
                this.mOrientationJudge.dumpLocked(pw, prefix2);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public abstract class OrientationJudge implements SensorEventListener {
        protected static final float MILLIS_PER_NANO = 1.0E-6f;
        protected static final long NANOS_PER_MS = 1000000;
        protected static final long PROPOSAL_MIN_TIME_SINCE_TOUCH_END_NANOS = 500000000;

        public abstract void dumpLocked(PrintWriter printWriter, String str);

        public abstract int getProposedRotationLocked();

        @Override // android.hardware.SensorEventListener
        public abstract void onAccuracyChanged(Sensor sensor, int i);

        @Override // android.hardware.SensorEventListener
        public abstract void onSensorChanged(SensorEvent sensorEvent);

        public abstract void onTouchEndLocked(long j);

        public abstract void onTouchStartLocked();

        public abstract void resetLocked(boolean z);

        OrientationJudge() {
        }
    }

    final class AccelSensorJudge extends OrientationJudge {
        private static final float ACCELERATION_TOLERANCE = 4.0f;
        private static final int ACCELEROMETER_DATA_X = 0;
        private static final int ACCELEROMETER_DATA_Y = 1;
        private static final int ACCELEROMETER_DATA_Z = 2;
        private static final int ADJACENT_ORIENTATION_ANGLE_GAP = 45;
        private static final float FILTER_TIME_CONSTANT_MS = 200.0f;
        private static final float FLAT_ANGLE = 80.0f;
        private static final long FLAT_TIME_NANOS = 1000000000;
        private static final float MAX_ACCELERATION_MAGNITUDE = 13.80665f;
        private static final long MAX_FILTER_DELTA_TIME_NANOS = 1000000000;
        private static final int MAX_TILT = 80;
        private static final float MIN_ACCELERATION_MAGNITUDE = 5.80665f;
        private static final float NEAR_ZERO_MAGNITUDE = 1.0f;
        private static final long PROPOSAL_MIN_TIME_SINCE_ACCELERATION_ENDED_NANOS = 500000000;
        private static final long PROPOSAL_MIN_TIME_SINCE_FLAT_ENDED_NANOS = 500000000;
        private static final long PROPOSAL_MIN_TIME_SINCE_SWING_ENDED_NANOS = 300000000;
        private static final long PROPOSAL_SETTLE_TIME_NANOS = 40000000;
        private static final float RADIANS_TO_DEGREES = 57.29578f;
        private static final float SWING_AWAY_ANGLE_DELTA = 20.0f;
        private static final long SWING_TIME_NANOS = 300000000;
        private static final int TILT_HISTORY_SIZE = 200;
        private static final int TILT_OVERHEAD_ENTER = -40;
        private static final int TILT_OVERHEAD_EXIT = -15;
        private boolean mAccelerating;
        private long mAccelerationTimestampNanos;
        private boolean mFlat;
        private long mFlatTimestampNanos;
        private long mLastFilteredTimestampNanos;
        private float mLastFilteredX;
        private float mLastFilteredY;
        private float mLastFilteredZ;
        private boolean mOverhead;
        private int mPredictedRotation;
        private long mPredictedRotationTimestampNanos;
        private int mProposedRotation;
        private long mSwingTimestampNanos;
        private boolean mSwinging;
        private float[] mTiltHistory = new float[200];
        private int mTiltHistoryIndex;
        private long[] mTiltHistoryTimestampNanos = new long[200];
        private final int[][] mTiltToleranceConfig = {new int[]{-25, 70}, new int[]{-25, 65}, new int[]{-25, 60}, new int[]{-25, 65}};
        private long mTouchEndedTimestampNanos = Long.MIN_VALUE;
        private boolean mTouched;

        public AccelSensorJudge(Context context) {
            super();
            int[] tiltTolerance = context.getResources().getIntArray(17235990);
            if (tiltTolerance.length == 8) {
                for (int i = 0; i < 4; i++) {
                    int min = tiltTolerance[i * 2];
                    int max = tiltTolerance[(i * 2) + 1];
                    if (min < -90 || min > max || max > 90) {
                        Slog.wtf(WindowOrientationListener.TAG, "config_autoRotationTiltTolerance contains invalid range: min=" + min + ", max=" + max);
                    } else {
                        int[][] iArr = this.mTiltToleranceConfig;
                        iArr[i][0] = min;
                        iArr[i][1] = max;
                    }
                }
                return;
            }
            Slog.wtf(WindowOrientationListener.TAG, "config_autoRotationTiltTolerance should have exactly 8 elements");
        }

        @Override // com.android.server.policy.WindowOrientationListener.OrientationJudge
        public int getProposedRotationLocked() {
            return this.mProposedRotation;
        }

        @Override // com.android.server.policy.WindowOrientationListener.OrientationJudge
        public void dumpLocked(PrintWriter pw, String prefix) {
            pw.println(prefix + "AccelSensorJudge");
            String prefix2 = prefix + "  ";
            pw.println(prefix2 + "mProposedRotation=" + this.mProposedRotation);
            pw.println(prefix2 + "mPredictedRotation=" + this.mPredictedRotation);
            pw.println(prefix2 + "mLastFilteredX=" + this.mLastFilteredX);
            pw.println(prefix2 + "mLastFilteredY=" + this.mLastFilteredY);
            pw.println(prefix2 + "mLastFilteredZ=" + this.mLastFilteredZ);
            long delta = SystemClock.elapsedRealtimeNanos() - this.mLastFilteredTimestampNanos;
            pw.println(prefix2 + "mLastFilteredTimestampNanos=" + this.mLastFilteredTimestampNanos + " (" + (((float) delta) * 1.0E-6f) + "ms ago)");
            StringBuilder sb = new StringBuilder();
            sb.append(prefix2);
            sb.append("mTiltHistory={last: ");
            sb.append(getLastTiltLocked());
            sb.append("}");
            pw.println(sb.toString());
            pw.println(prefix2 + "mFlat=" + this.mFlat);
            pw.println(prefix2 + "mSwinging=" + this.mSwinging);
            pw.println(prefix2 + "mAccelerating=" + this.mAccelerating);
            pw.println(prefix2 + "mOverhead=" + this.mOverhead);
            pw.println(prefix2 + "mTouched=" + this.mTouched);
            StringBuilder sb2 = new StringBuilder();
            sb2.append(prefix2);
            sb2.append("mTiltToleranceConfig=[");
            pw.print(sb2.toString());
            for (int i = 0; i < 4; i++) {
                if (i != 0) {
                    pw.print(", ");
                }
                pw.print("[");
                pw.print(this.mTiltToleranceConfig[i][0]);
                pw.print(", ");
                pw.print(this.mTiltToleranceConfig[i][1]);
                pw.print("]");
            }
            pw.println("]");
        }

        @Override // com.android.server.policy.WindowOrientationListener.OrientationJudge, android.hardware.SensorEventListener
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        /* JADX WARNING: Removed duplicated region for block: B:28:0x010b  */
        /* JADX WARNING: Removed duplicated region for block: B:82:0x028e  */
        /* JADX WARNING: Removed duplicated region for block: B:91:0x02b5  */
        /* JADX WARNING: Removed duplicated region for block: B:92:0x0372  */
        @Override // com.android.server.policy.WindowOrientationListener.OrientationJudge, android.hardware.SensorEventListener
        public void onSensorChanged(SensorEvent event) {
            boolean skipSample;
            boolean isFlat;
            int oldProposedRotation;
            int proposedRotation;
            boolean isSwinging;
            float z;
            synchronized (WindowOrientationListener.this.mLock) {
                float x = event.values[0];
                float y = event.values[1];
                float z2 = event.values[2];
                if (WindowOrientationListener.LOG) {
                    Slog.v(WindowOrientationListener.TAG, "Raw acceleration vector: x=" + x + ", y=" + y + ", z=" + z2 + ", magnitude=" + Math.sqrt((double) ((x * x) + (y * y) + (z2 * z2))));
                }
                long now = event.timestamp;
                long then = this.mLastFilteredTimestampNanos;
                float timeDeltaMS = ((float) (now - then)) * 1.0E-6f;
                if (now >= then && now <= 1000000000 + then) {
                    if (x != 0.0f || y != 0.0f || z2 != 0.0f) {
                        float alpha = timeDeltaMS / (FILTER_TIME_CONSTANT_MS + timeDeltaMS);
                        x = ((x - this.mLastFilteredX) * alpha) + this.mLastFilteredX;
                        y = ((y - this.mLastFilteredY) * alpha) + this.mLastFilteredY;
                        float z3 = ((z2 - this.mLastFilteredZ) * alpha) + this.mLastFilteredZ;
                        if (WindowOrientationListener.LOG) {
                            StringBuilder sb = new StringBuilder();
                            sb.append("Filtered acceleration vector: x=");
                            sb.append(x);
                            sb.append(", y=");
                            sb.append(y);
                            sb.append(", z=");
                            sb.append(z3);
                            sb.append(", magnitude=");
                            z = z3;
                            sb.append(Math.sqrt((double) ((x * x) + (y * y) + (z3 * z3))));
                            Slog.v(WindowOrientationListener.TAG, sb.toString());
                        } else {
                            z = z3;
                        }
                        skipSample = false;
                        z2 = z;
                        this.mLastFilteredTimestampNanos = now;
                        this.mLastFilteredX = x;
                        this.mLastFilteredY = y;
                        this.mLastFilteredZ = z2;
                        boolean isAccelerating = false;
                        boolean isFlat2 = false;
                        boolean isSwinging2 = false;
                        if (skipSample) {
                            float magnitude = (float) Math.sqrt((double) ((x * x) + (y * y) + (z2 * z2)));
                            if (magnitude < 1.0f) {
                                if (WindowOrientationListener.LOG) {
                                    Slog.v(WindowOrientationListener.TAG, "Ignoring sensor data, magnitude too close to zero.");
                                }
                                clearPredictedRotationLocked();
                            } else {
                                if (isAcceleratingLocked(magnitude)) {
                                    isAccelerating = true;
                                    this.mAccelerationTimestampNanos = now;
                                }
                                int tiltAngle = (int) Math.round(Math.asin((double) (z2 / magnitude)) * 57.295780181884766d);
                                addTiltHistoryEntryLocked(now, (float) tiltAngle);
                                if (isFlatLocked(now)) {
                                    isFlat2 = true;
                                    this.mFlatTimestampNanos = now;
                                }
                                if (isSwingingLocked(now, (float) tiltAngle)) {
                                    isSwinging2 = true;
                                    this.mSwingTimestampNanos = now;
                                }
                                if (tiltAngle <= TILT_OVERHEAD_ENTER) {
                                    this.mOverhead = true;
                                } else if (tiltAngle >= TILT_OVERHEAD_EXIT) {
                                    this.mOverhead = false;
                                }
                                if (this.mOverhead) {
                                    if (WindowOrientationListener.LOG) {
                                        Slog.v(WindowOrientationListener.TAG, "Ignoring sensor data, device is overhead: tiltAngle=" + tiltAngle);
                                    }
                                    clearPredictedRotationLocked();
                                    isFlat = isFlat2;
                                    isSwinging = isSwinging2;
                                } else if (Math.abs(tiltAngle) > 80) {
                                    if (WindowOrientationListener.LOG) {
                                        Slog.v(WindowOrientationListener.TAG, "Ignoring sensor data, tilt angle too high: tiltAngle=" + tiltAngle);
                                    }
                                    clearPredictedRotationLocked();
                                    isFlat = isFlat2;
                                    isSwinging = isSwinging2;
                                } else {
                                    isFlat = isFlat2;
                                    isSwinging = isSwinging2;
                                    int orientationAngle = (int) Math.round((-Math.atan2((double) (-x), (double) y)) * 57.295780181884766d);
                                    if (orientationAngle < 0) {
                                        orientationAngle += 360;
                                    }
                                    int nearestRotation = (orientationAngle + 45) / 90;
                                    if (nearestRotation == 4) {
                                        nearestRotation = 0;
                                    }
                                    if (isTiltAngleAcceptableLocked(nearestRotation, tiltAngle)) {
                                        if (isOrientationAngleAcceptableLocked(nearestRotation, orientationAngle)) {
                                            updatePredictedRotationLocked(now, nearestRotation);
                                            if (WindowOrientationListener.LOG) {
                                                Slog.v(WindowOrientationListener.TAG, "Predicted: tiltAngle=" + tiltAngle + ", orientationAngle=" + orientationAngle + ", predictedRotation=" + this.mPredictedRotation + ", predictedRotationAgeMS=" + (((float) (now - this.mPredictedRotationTimestampNanos)) * 1.0E-6f));
                                            }
                                        }
                                    }
                                    if (WindowOrientationListener.LOG) {
                                        Slog.v(WindowOrientationListener.TAG, "Ignoring sensor data, no predicted rotation: tiltAngle=" + tiltAngle + ", orientationAngle=" + orientationAngle);
                                    }
                                    clearPredictedRotationLocked();
                                }
                                isSwinging2 = isSwinging;
                                isAccelerating = isAccelerating;
                                this.mFlat = isFlat;
                                this.mSwinging = isSwinging2;
                                this.mAccelerating = isAccelerating;
                                oldProposedRotation = this.mProposedRotation;
                                if (this.mPredictedRotation < 0 || isPredictedRotationAcceptableLocked(now)) {
                                    this.mProposedRotation = this.mPredictedRotation;
                                }
                                proposedRotation = this.mProposedRotation;
                                if (WindowOrientationListener.LOG) {
                                    Slog.v(WindowOrientationListener.TAG, "Result: currentRotation=" + WindowOrientationListener.this.mCurrentRotation + ", proposedRotation=" + proposedRotation + ", predictedRotation=" + this.mPredictedRotation + ", timeDeltaMS=" + timeDeltaMS + ", isAccelerating=" + isAccelerating + ", isFlat=" + isFlat + ", isSwinging=" + isSwinging2 + ", isOverhead=" + this.mOverhead + ", isTouched=" + this.mTouched + ", timeUntilSettledMS=" + remainingMS(now, this.mPredictedRotationTimestampNanos + PROPOSAL_SETTLE_TIME_NANOS) + ", timeUntilAccelerationDelayExpiredMS=" + remainingMS(now, this.mAccelerationTimestampNanos + 500000000) + ", timeUntilFlatDelayExpiredMS=" + remainingMS(now, this.mFlatTimestampNanos + 500000000) + ", timeUntilSwingDelayExpiredMS=" + remainingMS(now, this.mSwingTimestampNanos + 300000000) + ", timeUntilTouchDelayExpiredMS=" + remainingMS(now, this.mTouchEndedTimestampNanos + 500000000));
                                }
                            }
                        }
                        isFlat = false;
                        this.mFlat = isFlat;
                        this.mSwinging = isSwinging2;
                        this.mAccelerating = isAccelerating;
                        oldProposedRotation = this.mProposedRotation;
                        this.mProposedRotation = this.mPredictedRotation;
                        proposedRotation = this.mProposedRotation;
                        if (WindowOrientationListener.LOG) {
                        }
                    }
                }
                if (WindowOrientationListener.LOG) {
                    Slog.v(WindowOrientationListener.TAG, "Resetting orientation listener.");
                }
                resetLocked(true);
                skipSample = true;
                this.mLastFilteredTimestampNanos = now;
                this.mLastFilteredX = x;
                this.mLastFilteredY = y;
                this.mLastFilteredZ = z2;
                boolean isAccelerating2 = false;
                boolean isFlat22 = false;
                boolean isSwinging22 = false;
                if (skipSample) {
                }
                isFlat = false;
                this.mFlat = isFlat;
                this.mSwinging = isSwinging22;
                this.mAccelerating = isAccelerating2;
                oldProposedRotation = this.mProposedRotation;
                this.mProposedRotation = this.mPredictedRotation;
                proposedRotation = this.mProposedRotation;
                if (WindowOrientationListener.LOG) {
                }
            }
            if (proposedRotation != oldProposedRotation && proposedRotation >= 0) {
                if (WindowOrientationListener.LOG) {
                    Slog.v(WindowOrientationListener.TAG, "Proposed rotation changed!  proposedRotation=" + proposedRotation + ", oldProposedRotation=" + oldProposedRotation);
                }
                WindowOrientationListener.this.onProposedRotationChanged(proposedRotation);
            }
        }

        @Override // com.android.server.policy.WindowOrientationListener.OrientationJudge
        public void onTouchStartLocked() {
            this.mTouched = true;
        }

        @Override // com.android.server.policy.WindowOrientationListener.OrientationJudge
        public void onTouchEndLocked(long whenElapsedNanos) {
            this.mTouched = false;
            this.mTouchEndedTimestampNanos = whenElapsedNanos;
        }

        @Override // com.android.server.policy.WindowOrientationListener.OrientationJudge
        public void resetLocked(boolean clearCurrentRotation) {
            this.mLastFilteredTimestampNanos = Long.MIN_VALUE;
            if (clearCurrentRotation) {
                this.mProposedRotation = -1;
            }
            this.mFlatTimestampNanos = Long.MIN_VALUE;
            this.mFlat = false;
            this.mSwingTimestampNanos = Long.MIN_VALUE;
            this.mSwinging = false;
            this.mAccelerationTimestampNanos = Long.MIN_VALUE;
            this.mAccelerating = false;
            this.mOverhead = false;
            clearPredictedRotationLocked();
            clearTiltHistoryLocked();
        }

        private boolean isTiltAngleAcceptableLocked(int rotation, int tiltAngle) {
            int[][] iArr = this.mTiltToleranceConfig;
            return tiltAngle >= iArr[rotation][0] && tiltAngle <= iArr[rotation][1];
        }

        private boolean isOrientationAngleAcceptableLocked(int rotation, int orientationAngle) {
            int currentRotation = WindowOrientationListener.this.mCurrentRotation;
            if (currentRotation < 0) {
                return true;
            }
            if (rotation == currentRotation || rotation == (currentRotation + 1) % 4) {
                int lowerBound = ((rotation * 90) - 45) + 22;
                if (rotation == 0) {
                    if (orientationAngle >= 315 && orientationAngle < lowerBound + 360) {
                        return false;
                    }
                } else if (orientationAngle < lowerBound) {
                    return false;
                }
            }
            if (rotation != currentRotation && rotation != (currentRotation + 3) % 4) {
                return true;
            }
            int upperBound = ((rotation * 90) + 45) - 22;
            if (rotation == 0) {
                if (orientationAngle > 45 || orientationAngle <= upperBound) {
                    return true;
                }
                return false;
            } else if (orientationAngle > upperBound) {
                return false;
            } else {
                return true;
            }
        }

        private boolean isPredictedRotationAcceptableLocked(long now) {
            if (now >= this.mPredictedRotationTimestampNanos + PROPOSAL_SETTLE_TIME_NANOS && now >= this.mFlatTimestampNanos + 500000000 && now >= this.mSwingTimestampNanos + 300000000 && now >= this.mAccelerationTimestampNanos + 500000000 && !this.mTouched && now >= this.mTouchEndedTimestampNanos + 500000000) {
                return true;
            }
            return false;
        }

        private void clearPredictedRotationLocked() {
            this.mPredictedRotation = -1;
            this.mPredictedRotationTimestampNanos = Long.MIN_VALUE;
        }

        private void updatePredictedRotationLocked(long now, int rotation) {
            if (this.mPredictedRotation != rotation) {
                this.mPredictedRotation = rotation;
                this.mPredictedRotationTimestampNanos = now;
            }
        }

        private boolean isAcceleratingLocked(float magnitude) {
            return magnitude < MIN_ACCELERATION_MAGNITUDE || magnitude > MAX_ACCELERATION_MAGNITUDE;
        }

        private void clearTiltHistoryLocked() {
            this.mTiltHistoryTimestampNanos[0] = Long.MIN_VALUE;
            this.mTiltHistoryIndex = 1;
        }

        private void addTiltHistoryEntryLocked(long now, float tilt) {
            float[] fArr = this.mTiltHistory;
            int i = this.mTiltHistoryIndex;
            fArr[i] = tilt;
            long[] jArr = this.mTiltHistoryTimestampNanos;
            jArr[i] = now;
            this.mTiltHistoryIndex = (i + 1) % 200;
            jArr[this.mTiltHistoryIndex] = Long.MIN_VALUE;
        }

        private boolean isFlatLocked(long now) {
            int i = this.mTiltHistoryIndex;
            do {
                int nextTiltHistoryIndexLocked = nextTiltHistoryIndexLocked(i);
                i = nextTiltHistoryIndexLocked;
                if (nextTiltHistoryIndexLocked < 0 || this.mTiltHistory[i] < FLAT_ANGLE) {
                    return false;
                }
            } while (this.mTiltHistoryTimestampNanos[i] + 1000000000 > now);
            return true;
        }

        private boolean isSwingingLocked(long now, float tilt) {
            int i = this.mTiltHistoryIndex;
            do {
                int nextTiltHistoryIndexLocked = nextTiltHistoryIndexLocked(i);
                i = nextTiltHistoryIndexLocked;
                if (nextTiltHistoryIndexLocked < 0 || this.mTiltHistoryTimestampNanos[i] + 300000000 < now) {
                    return false;
                }
            } while (this.mTiltHistory[i] + SWING_AWAY_ANGLE_DELTA > tilt);
            return true;
        }

        private int nextTiltHistoryIndexLocked(int index) {
            int index2 = (index == 0 ? 200 : index) - 1;
            if (this.mTiltHistoryTimestampNanos[index2] != Long.MIN_VALUE) {
                return index2;
            }
            return -1;
        }

        private float getLastTiltLocked() {
            int index = nextTiltHistoryIndexLocked(this.mTiltHistoryIndex);
            if (index >= 0) {
                return this.mTiltHistory[index];
            }
            return Float.NaN;
        }

        private float remainingMS(long now, long until) {
            if (now >= until) {
                return 0.0f;
            }
            return ((float) (until - now)) * 1.0E-6f;
        }
    }

    final class OrientationSensorJudge extends OrientationJudge {
        private int mDesiredRotation = -1;
        private int mProposedRotation = -1;
        private boolean mRotationEvaluationScheduled;
        private Runnable mRotationEvaluator = new Runnable() {
            /* class com.android.server.policy.WindowOrientationListener.OrientationSensorJudge.AnonymousClass1 */

            @Override // java.lang.Runnable
            public void run() {
                int newRotation;
                synchronized (WindowOrientationListener.this.mLock) {
                    OrientationSensorJudge.this.mRotationEvaluationScheduled = false;
                    newRotation = OrientationSensorJudge.this.evaluateRotationChangeLocked();
                }
                if (newRotation >= 0) {
                    WindowOrientationListener.this.onProposedRotationChanged(newRotation);
                }
            }
        };
        private long mTouchEndedTimestampNanos = Long.MIN_VALUE;
        private boolean mTouching;

        OrientationSensorJudge() {
            super();
        }

        @Override // com.android.server.policy.WindowOrientationListener.OrientationJudge
        public int getProposedRotationLocked() {
            return this.mProposedRotation;
        }

        @Override // com.android.server.policy.WindowOrientationListener.OrientationJudge
        public void onTouchStartLocked() {
            this.mTouching = true;
        }

        @Override // com.android.server.policy.WindowOrientationListener.OrientationJudge
        public void onTouchEndLocked(long whenElapsedNanos) {
            this.mTouching = false;
            this.mTouchEndedTimestampNanos = whenElapsedNanos;
            if (this.mDesiredRotation != this.mProposedRotation) {
                scheduleRotationEvaluationIfNecessaryLocked(SystemClock.elapsedRealtimeNanos());
            }
        }

        @Override // com.android.server.policy.WindowOrientationListener.OrientationJudge, android.hardware.SensorEventListener
        public void onSensorChanged(SensorEvent event) {
            int newRotation;
            synchronized (WindowOrientationListener.this.mLock) {
                this.mDesiredRotation = (int) event.values[0];
                newRotation = evaluateRotationChangeLocked();
            }
            if (newRotation >= 0) {
                WindowOrientationListener.this.onProposedRotationChanged(newRotation);
            }
        }

        @Override // com.android.server.policy.WindowOrientationListener.OrientationJudge, android.hardware.SensorEventListener
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @Override // com.android.server.policy.WindowOrientationListener.OrientationJudge
        public void dumpLocked(PrintWriter pw, String prefix) {
            pw.println(prefix + "OrientationSensorJudge");
            String prefix2 = prefix + "  ";
            pw.println(prefix2 + "mDesiredRotation=" + Surface.rotationToString(this.mDesiredRotation));
            pw.println(prefix2 + "mProposedRotation=" + Surface.rotationToString(this.mProposedRotation));
            pw.println(prefix2 + "mTouching=" + this.mTouching);
            pw.println(prefix2 + "mTouchEndedTimestampNanos=" + this.mTouchEndedTimestampNanos);
        }

        @Override // com.android.server.policy.WindowOrientationListener.OrientationJudge
        public void resetLocked(boolean clearCurrentRotation) {
            if (clearCurrentRotation) {
                this.mProposedRotation = -1;
                this.mDesiredRotation = -1;
            }
            this.mTouching = false;
            this.mTouchEndedTimestampNanos = Long.MIN_VALUE;
            unscheduleRotationEvaluationLocked();
        }

        public int evaluateRotationChangeLocked() {
            unscheduleRotationEvaluationLocked();
            if (this.mDesiredRotation == this.mProposedRotation) {
                return -1;
            }
            long now = SystemClock.elapsedRealtimeNanos();
            if (isDesiredRotationAcceptableLocked(now)) {
                this.mProposedRotation = this.mDesiredRotation;
                return this.mProposedRotation;
            }
            scheduleRotationEvaluationIfNecessaryLocked(now);
            return -1;
        }

        private boolean isDesiredRotationAcceptableLocked(long now) {
            if (!this.mTouching && now >= this.mTouchEndedTimestampNanos + 500000000) {
                return true;
            }
            return false;
        }

        private void scheduleRotationEvaluationIfNecessaryLocked(long now) {
            if (this.mRotationEvaluationScheduled || this.mDesiredRotation == this.mProposedRotation) {
                if (WindowOrientationListener.LOG) {
                    Slog.d(WindowOrientationListener.TAG, "scheduleRotationEvaluationLocked: ignoring, an evaluation is already scheduled or is unnecessary.");
                }
            } else if (!this.mTouching) {
                long timeOfNextPossibleRotationNanos = this.mTouchEndedTimestampNanos + 500000000;
                if (now < timeOfNextPossibleRotationNanos) {
                    WindowOrientationListener.this.mHandler.postDelayed(this.mRotationEvaluator, (long) Math.ceil((double) (((float) (timeOfNextPossibleRotationNanos - now)) * 1.0E-6f)));
                    this.mRotationEvaluationScheduled = true;
                } else if (WindowOrientationListener.LOG) {
                    Slog.d(WindowOrientationListener.TAG, "scheduleRotationEvaluationLocked: ignoring, already past the next possible time of rotation.");
                }
            } else if (WindowOrientationListener.LOG) {
                Slog.d(WindowOrientationListener.TAG, "scheduleRotationEvaluationLocked: ignoring, user is still touching the screen.");
            }
        }

        private void unscheduleRotationEvaluationLocked() {
            if (this.mRotationEvaluationScheduled) {
                WindowOrientationListener.this.mHandler.removeCallbacks(this.mRotationEvaluator);
                this.mRotationEvaluationScheduled = false;
            }
        }
    }
}
