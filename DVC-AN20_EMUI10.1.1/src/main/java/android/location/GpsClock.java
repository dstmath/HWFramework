package android.location;

import android.annotation.SystemApi;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Half;

@SystemApi
public class GpsClock implements Parcelable {
    public static final Parcelable.Creator<GpsClock> CREATOR = new Parcelable.Creator<GpsClock>() {
        /* class android.location.GpsClock.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public GpsClock createFromParcel(Parcel parcel) {
            GpsClock gpsClock = new GpsClock();
            gpsClock.mFlags = (short) parcel.readInt();
            gpsClock.mLeapSecond = (short) parcel.readInt();
            gpsClock.mType = parcel.readByte();
            gpsClock.mTimeInNs = parcel.readLong();
            gpsClock.mTimeUncertaintyInNs = parcel.readDouble();
            gpsClock.mFullBiasInNs = parcel.readLong();
            gpsClock.mBiasInNs = parcel.readDouble();
            gpsClock.mBiasUncertaintyInNs = parcel.readDouble();
            gpsClock.mDriftInNsPerSec = parcel.readDouble();
            gpsClock.mDriftUncertaintyInNsPerSec = parcel.readDouble();
            return gpsClock;
        }

        @Override // android.os.Parcelable.Creator
        public GpsClock[] newArray(int size) {
            return new GpsClock[size];
        }
    };
    private static final short HAS_BIAS = 8;
    private static final short HAS_BIAS_UNCERTAINTY = 16;
    private static final short HAS_DRIFT = 32;
    private static final short HAS_DRIFT_UNCERTAINTY = 64;
    private static final short HAS_FULL_BIAS = 4;
    private static final short HAS_LEAP_SECOND = 1;
    private static final short HAS_NO_FLAGS = 0;
    private static final short HAS_TIME_UNCERTAINTY = 2;
    public static final byte TYPE_GPS_TIME = 2;
    public static final byte TYPE_LOCAL_HW_TIME = 1;
    public static final byte TYPE_UNKNOWN = 0;
    private double mBiasInNs;
    private double mBiasUncertaintyInNs;
    private double mDriftInNsPerSec;
    private double mDriftUncertaintyInNsPerSec;
    private short mFlags;
    private long mFullBiasInNs;
    private short mLeapSecond;
    private long mTimeInNs;
    private double mTimeUncertaintyInNs;
    private byte mType;

    GpsClock() {
        initialize();
    }

    public void set(GpsClock clock) {
        this.mFlags = clock.mFlags;
        this.mLeapSecond = clock.mLeapSecond;
        this.mType = clock.mType;
        this.mTimeInNs = clock.mTimeInNs;
        this.mTimeUncertaintyInNs = clock.mTimeUncertaintyInNs;
        this.mFullBiasInNs = clock.mFullBiasInNs;
        this.mBiasInNs = clock.mBiasInNs;
        this.mBiasUncertaintyInNs = clock.mBiasUncertaintyInNs;
        this.mDriftInNsPerSec = clock.mDriftInNsPerSec;
        this.mDriftUncertaintyInNsPerSec = clock.mDriftUncertaintyInNsPerSec;
    }

    public void reset() {
        initialize();
    }

    public byte getType() {
        return this.mType;
    }

    public void setType(byte value) {
        this.mType = value;
    }

    private String getTypeString() {
        byte b = this.mType;
        if (b == 0) {
            return "Unknown";
        }
        if (b == 1) {
            return "LocalHwClock";
        }
        if (b == 2) {
            return "GpsTime";
        }
        return "<Invalid:" + ((int) this.mType) + ">";
    }

    public boolean hasLeapSecond() {
        return isFlagSet(1);
    }

    public short getLeapSecond() {
        return this.mLeapSecond;
    }

    public void setLeapSecond(short leapSecond) {
        setFlag(1);
        this.mLeapSecond = leapSecond;
    }

    public void resetLeapSecond() {
        resetFlag(1);
        this.mLeapSecond = Half.NEGATIVE_ZERO;
    }

    public long getTimeInNs() {
        return this.mTimeInNs;
    }

    public void setTimeInNs(long timeInNs) {
        this.mTimeInNs = timeInNs;
    }

    public boolean hasTimeUncertaintyInNs() {
        return isFlagSet(2);
    }

    public double getTimeUncertaintyInNs() {
        return this.mTimeUncertaintyInNs;
    }

    public void setTimeUncertaintyInNs(double timeUncertaintyInNs) {
        setFlag(2);
        this.mTimeUncertaintyInNs = timeUncertaintyInNs;
    }

    public void resetTimeUncertaintyInNs() {
        resetFlag(2);
        this.mTimeUncertaintyInNs = Double.NaN;
    }

    public boolean hasFullBiasInNs() {
        return isFlagSet(4);
    }

    public long getFullBiasInNs() {
        return this.mFullBiasInNs;
    }

    public void setFullBiasInNs(long value) {
        setFlag(4);
        this.mFullBiasInNs = value;
    }

    public void resetFullBiasInNs() {
        resetFlag(4);
        this.mFullBiasInNs = Long.MIN_VALUE;
    }

    public boolean hasBiasInNs() {
        return isFlagSet(8);
    }

    public double getBiasInNs() {
        return this.mBiasInNs;
    }

    public void setBiasInNs(double biasInNs) {
        setFlag(8);
        this.mBiasInNs = biasInNs;
    }

    public void resetBiasInNs() {
        resetFlag(8);
        this.mBiasInNs = Double.NaN;
    }

    public boolean hasBiasUncertaintyInNs() {
        return isFlagSet(16);
    }

    public double getBiasUncertaintyInNs() {
        return this.mBiasUncertaintyInNs;
    }

    public void setBiasUncertaintyInNs(double biasUncertaintyInNs) {
        setFlag(16);
        this.mBiasUncertaintyInNs = biasUncertaintyInNs;
    }

    public void resetBiasUncertaintyInNs() {
        resetFlag(16);
        this.mBiasUncertaintyInNs = Double.NaN;
    }

    public boolean hasDriftInNsPerSec() {
        return isFlagSet(HAS_DRIFT);
    }

    public double getDriftInNsPerSec() {
        return this.mDriftInNsPerSec;
    }

    public void setDriftInNsPerSec(double driftInNsPerSec) {
        setFlag(HAS_DRIFT);
        this.mDriftInNsPerSec = driftInNsPerSec;
    }

    public void resetDriftInNsPerSec() {
        resetFlag(HAS_DRIFT);
        this.mDriftInNsPerSec = Double.NaN;
    }

    public boolean hasDriftUncertaintyInNsPerSec() {
        return isFlagSet(HAS_DRIFT_UNCERTAINTY);
    }

    public double getDriftUncertaintyInNsPerSec() {
        return this.mDriftUncertaintyInNsPerSec;
    }

    public void setDriftUncertaintyInNsPerSec(double driftUncertaintyInNsPerSec) {
        setFlag(HAS_DRIFT_UNCERTAINTY);
        this.mDriftUncertaintyInNsPerSec = driftUncertaintyInNsPerSec;
    }

    public void resetDriftUncertaintyInNsPerSec() {
        resetFlag(HAS_DRIFT_UNCERTAINTY);
        this.mDriftUncertaintyInNsPerSec = Double.NaN;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(this.mFlags);
        parcel.writeInt(this.mLeapSecond);
        parcel.writeByte(this.mType);
        parcel.writeLong(this.mTimeInNs);
        parcel.writeDouble(this.mTimeUncertaintyInNs);
        parcel.writeLong(this.mFullBiasInNs);
        parcel.writeDouble(this.mBiasInNs);
        parcel.writeDouble(this.mBiasUncertaintyInNs);
        parcel.writeDouble(this.mDriftInNsPerSec);
        parcel.writeDouble(this.mDriftUncertaintyInNsPerSec);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder("GpsClock:\n");
        builder.append(String.format("   %-15s = %s\n", "Type", getTypeString()));
        Object[] objArr = new Object[2];
        objArr[0] = "LeapSecond";
        Double d = null;
        objArr[1] = hasLeapSecond() ? Short.valueOf(this.mLeapSecond) : null;
        builder.append(String.format("   %-15s = %s\n", objArr));
        Object[] objArr2 = new Object[4];
        objArr2[0] = "TimeInNs";
        objArr2[1] = Long.valueOf(this.mTimeInNs);
        objArr2[2] = "TimeUncertaintyInNs";
        objArr2[3] = hasTimeUncertaintyInNs() ? Double.valueOf(this.mTimeUncertaintyInNs) : null;
        builder.append(String.format("   %-15s = %-25s   %-26s = %s\n", objArr2));
        Object[] objArr3 = new Object[2];
        objArr3[0] = "FullBiasInNs";
        objArr3[1] = hasFullBiasInNs() ? Long.valueOf(this.mFullBiasInNs) : null;
        builder.append(String.format("   %-15s = %s\n", objArr3));
        Object[] objArr4 = new Object[4];
        objArr4[0] = "BiasInNs";
        objArr4[1] = hasBiasInNs() ? Double.valueOf(this.mBiasInNs) : null;
        objArr4[2] = "BiasUncertaintyInNs";
        objArr4[3] = hasBiasUncertaintyInNs() ? Double.valueOf(this.mBiasUncertaintyInNs) : null;
        builder.append(String.format("   %-15s = %-25s   %-26s = %s\n", objArr4));
        Object[] objArr5 = new Object[4];
        objArr5[0] = "DriftInNsPerSec";
        objArr5[1] = hasDriftInNsPerSec() ? Double.valueOf(this.mDriftInNsPerSec) : null;
        objArr5[2] = "DriftUncertaintyInNsPerSec";
        if (hasDriftUncertaintyInNsPerSec()) {
            d = Double.valueOf(this.mDriftUncertaintyInNsPerSec);
        }
        objArr5[3] = d;
        builder.append(String.format("   %-15s = %-25s   %-26s = %s\n", objArr5));
        return builder.toString();
    }

    private void initialize() {
        this.mFlags = 0;
        resetLeapSecond();
        setType((byte) 0);
        setTimeInNs(Long.MIN_VALUE);
        resetTimeUncertaintyInNs();
        resetFullBiasInNs();
        resetBiasInNs();
        resetBiasUncertaintyInNs();
        resetDriftInNsPerSec();
        resetDriftUncertaintyInNsPerSec();
    }

    private void setFlag(short flag) {
        this.mFlags = (short) (this.mFlags | flag);
    }

    private void resetFlag(short flag) {
        this.mFlags = (short) (this.mFlags & (~flag));
    }

    private boolean isFlagSet(short flag) {
        return (this.mFlags & flag) == flag;
    }
}
