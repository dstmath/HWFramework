package android.location;

import android.os.Parcel;
import android.os.Parcelable;

public final class GnssClock implements Parcelable {
    public static final Parcelable.Creator<GnssClock> CREATOR = new Parcelable.Creator<GnssClock>() {
        public GnssClock createFromParcel(Parcel parcel) {
            GnssClock gpsClock = new GnssClock();
            int unused = gpsClock.mFlags = parcel.readInt();
            int unused2 = gpsClock.mLeapSecond = parcel.readInt();
            long unused3 = gpsClock.mTimeNanos = parcel.readLong();
            double unused4 = gpsClock.mTimeUncertaintyNanos = parcel.readDouble();
            long unused5 = gpsClock.mFullBiasNanos = parcel.readLong();
            double unused6 = gpsClock.mBiasNanos = parcel.readDouble();
            double unused7 = gpsClock.mBiasUncertaintyNanos = parcel.readDouble();
            double unused8 = gpsClock.mDriftNanosPerSecond = parcel.readDouble();
            double unused9 = gpsClock.mDriftUncertaintyNanosPerSecond = parcel.readDouble();
            int unused10 = gpsClock.mHardwareClockDiscontinuityCount = parcel.readInt();
            return gpsClock;
        }

        public GnssClock[] newArray(int size) {
            return new GnssClock[size];
        }
    };
    private static final int HAS_BIAS = 8;
    private static final int HAS_BIAS_UNCERTAINTY = 16;
    private static final int HAS_DRIFT = 32;
    private static final int HAS_DRIFT_UNCERTAINTY = 64;
    private static final int HAS_FULL_BIAS = 4;
    private static final int HAS_LEAP_SECOND = 1;
    private static final int HAS_NO_FLAGS = 0;
    private static final int HAS_TIME_UNCERTAINTY = 2;
    /* access modifiers changed from: private */
    public double mBiasNanos;
    /* access modifiers changed from: private */
    public double mBiasUncertaintyNanos;
    /* access modifiers changed from: private */
    public double mDriftNanosPerSecond;
    /* access modifiers changed from: private */
    public double mDriftUncertaintyNanosPerSecond;
    /* access modifiers changed from: private */
    public int mFlags;
    /* access modifiers changed from: private */
    public long mFullBiasNanos;
    /* access modifiers changed from: private */
    public int mHardwareClockDiscontinuityCount;
    /* access modifiers changed from: private */
    public int mLeapSecond;
    /* access modifiers changed from: private */
    public long mTimeNanos;
    /* access modifiers changed from: private */
    public double mTimeUncertaintyNanos;

    public GnssClock() {
        initialize();
    }

    public void set(GnssClock clock) {
        this.mFlags = clock.mFlags;
        this.mLeapSecond = clock.mLeapSecond;
        this.mTimeNanos = clock.mTimeNanos;
        this.mTimeUncertaintyNanos = clock.mTimeUncertaintyNanos;
        this.mFullBiasNanos = clock.mFullBiasNanos;
        this.mBiasNanos = clock.mBiasNanos;
        this.mBiasUncertaintyNanos = clock.mBiasUncertaintyNanos;
        this.mDriftNanosPerSecond = clock.mDriftNanosPerSecond;
        this.mDriftUncertaintyNanosPerSecond = clock.mDriftUncertaintyNanosPerSecond;
        this.mHardwareClockDiscontinuityCount = clock.mHardwareClockDiscontinuityCount;
    }

    public void reset() {
        initialize();
    }

    public boolean hasLeapSecond() {
        return isFlagSet(1);
    }

    public int getLeapSecond() {
        return this.mLeapSecond;
    }

    public void setLeapSecond(int leapSecond) {
        setFlag(1);
        this.mLeapSecond = leapSecond;
    }

    public void resetLeapSecond() {
        resetFlag(1);
        this.mLeapSecond = Integer.MIN_VALUE;
    }

    public long getTimeNanos() {
        return this.mTimeNanos;
    }

    public void setTimeNanos(long timeNanos) {
        this.mTimeNanos = timeNanos;
    }

    public boolean hasTimeUncertaintyNanos() {
        return isFlagSet(2);
    }

    public double getTimeUncertaintyNanos() {
        return this.mTimeUncertaintyNanos;
    }

    public void setTimeUncertaintyNanos(double timeUncertaintyNanos) {
        setFlag(2);
        this.mTimeUncertaintyNanos = timeUncertaintyNanos;
    }

    public void resetTimeUncertaintyNanos() {
        resetFlag(2);
        this.mTimeUncertaintyNanos = Double.NaN;
    }

    public boolean hasFullBiasNanos() {
        return isFlagSet(4);
    }

    public long getFullBiasNanos() {
        return this.mFullBiasNanos;
    }

    public void setFullBiasNanos(long value) {
        setFlag(4);
        this.mFullBiasNanos = value;
    }

    public void resetFullBiasNanos() {
        resetFlag(4);
        this.mFullBiasNanos = Long.MIN_VALUE;
    }

    public boolean hasBiasNanos() {
        return isFlagSet(8);
    }

    public double getBiasNanos() {
        return this.mBiasNanos;
    }

    public void setBiasNanos(double biasNanos) {
        setFlag(8);
        this.mBiasNanos = biasNanos;
    }

    public void resetBiasNanos() {
        resetFlag(8);
        this.mBiasNanos = Double.NaN;
    }

    public boolean hasBiasUncertaintyNanos() {
        return isFlagSet(16);
    }

    public double getBiasUncertaintyNanos() {
        return this.mBiasUncertaintyNanos;
    }

    public void setBiasUncertaintyNanos(double biasUncertaintyNanos) {
        setFlag(16);
        this.mBiasUncertaintyNanos = biasUncertaintyNanos;
    }

    public void resetBiasUncertaintyNanos() {
        resetFlag(16);
        this.mBiasUncertaintyNanos = Double.NaN;
    }

    public boolean hasDriftNanosPerSecond() {
        return isFlagSet(32);
    }

    public double getDriftNanosPerSecond() {
        return this.mDriftNanosPerSecond;
    }

    public void setDriftNanosPerSecond(double driftNanosPerSecond) {
        setFlag(32);
        this.mDriftNanosPerSecond = driftNanosPerSecond;
    }

    public void resetDriftNanosPerSecond() {
        resetFlag(32);
        this.mDriftNanosPerSecond = Double.NaN;
    }

    public boolean hasDriftUncertaintyNanosPerSecond() {
        return isFlagSet(64);
    }

    public double getDriftUncertaintyNanosPerSecond() {
        return this.mDriftUncertaintyNanosPerSecond;
    }

    public void setDriftUncertaintyNanosPerSecond(double driftUncertaintyNanosPerSecond) {
        setFlag(64);
        this.mDriftUncertaintyNanosPerSecond = driftUncertaintyNanosPerSecond;
    }

    public void resetDriftUncertaintyNanosPerSecond() {
        resetFlag(64);
        this.mDriftUncertaintyNanosPerSecond = Double.NaN;
    }

    public int getHardwareClockDiscontinuityCount() {
        return this.mHardwareClockDiscontinuityCount;
    }

    public void setHardwareClockDiscontinuityCount(int value) {
        this.mHardwareClockDiscontinuityCount = value;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(this.mFlags);
        parcel.writeInt(this.mLeapSecond);
        parcel.writeLong(this.mTimeNanos);
        parcel.writeDouble(this.mTimeUncertaintyNanos);
        parcel.writeLong(this.mFullBiasNanos);
        parcel.writeDouble(this.mBiasNanos);
        parcel.writeDouble(this.mBiasUncertaintyNanos);
        parcel.writeDouble(this.mDriftNanosPerSecond);
        parcel.writeDouble(this.mDriftUncertaintyNanosPerSecond);
        parcel.writeInt(this.mHardwareClockDiscontinuityCount);
    }

    public int describeContents() {
        return 0;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder("GnssClock:\n");
        Object[] objArr = new Object[2];
        objArr[0] = "LeapSecond";
        Double d = null;
        objArr[1] = hasLeapSecond() ? Integer.valueOf(this.mLeapSecond) : null;
        builder.append(String.format("   %-15s = %s\n", objArr));
        Object[] objArr2 = new Object[4];
        objArr2[0] = "TimeNanos";
        objArr2[1] = Long.valueOf(this.mTimeNanos);
        objArr2[2] = "TimeUncertaintyNanos";
        objArr2[3] = hasTimeUncertaintyNanos() ? Double.valueOf(this.mTimeUncertaintyNanos) : null;
        builder.append(String.format("   %-15s = %-25s   %-26s = %s\n", objArr2));
        Object[] objArr3 = new Object[2];
        objArr3[0] = "FullBiasNanos";
        objArr3[1] = hasFullBiasNanos() ? Long.valueOf(this.mFullBiasNanos) : null;
        builder.append(String.format("   %-15s = %s\n", objArr3));
        Object[] objArr4 = new Object[4];
        objArr4[0] = "BiasNanos";
        objArr4[1] = hasBiasNanos() ? Double.valueOf(this.mBiasNanos) : null;
        objArr4[2] = "BiasUncertaintyNanos";
        objArr4[3] = hasBiasUncertaintyNanos() ? Double.valueOf(this.mBiasUncertaintyNanos) : null;
        builder.append(String.format("   %-15s = %-25s   %-26s = %s\n", objArr4));
        Object[] objArr5 = new Object[4];
        objArr5[0] = "DriftNanosPerSecond";
        objArr5[1] = hasDriftNanosPerSecond() ? Double.valueOf(this.mDriftNanosPerSecond) : null;
        objArr5[2] = "DriftUncertaintyNanosPerSecond";
        if (hasDriftUncertaintyNanosPerSecond()) {
            d = Double.valueOf(this.mDriftUncertaintyNanosPerSecond);
        }
        objArr5[3] = d;
        builder.append(String.format("   %-15s = %-25s   %-26s = %s\n", objArr5));
        builder.append(String.format("   %-15s = %s\n", new Object[]{"HardwareClockDiscontinuityCount", Integer.valueOf(this.mHardwareClockDiscontinuityCount)}));
        return builder.toString();
    }

    private void initialize() {
        this.mFlags = 0;
        resetLeapSecond();
        setTimeNanos(Long.MIN_VALUE);
        resetTimeUncertaintyNanos();
        resetFullBiasNanos();
        resetBiasNanos();
        resetBiasUncertaintyNanos();
        resetDriftNanosPerSecond();
        resetDriftUncertaintyNanosPerSecond();
        setHardwareClockDiscontinuityCount(Integer.MIN_VALUE);
    }

    private void setFlag(int flag) {
        this.mFlags |= flag;
    }

    private void resetFlag(int flag) {
        this.mFlags &= ~flag;
    }

    private boolean isFlagSet(int flag) {
        return (this.mFlags & flag) == flag;
    }
}
