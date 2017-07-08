package android.location;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.security.keymaster.KeymasterDefs;

public final class GnssClock implements Parcelable {
    public static final Creator<GnssClock> CREATOR = null;
    private static final int HAS_BIAS = 8;
    private static final int HAS_BIAS_UNCERTAINTY = 16;
    private static final int HAS_DRIFT = 32;
    private static final int HAS_DRIFT_UNCERTAINTY = 64;
    private static final int HAS_FULL_BIAS = 4;
    private static final int HAS_LEAP_SECOND = 1;
    private static final int HAS_NO_FLAGS = 0;
    private static final int HAS_TIME_UNCERTAINTY = 2;
    private double mBiasNanos;
    private double mBiasUncertaintyNanos;
    private double mDriftNanosPerSecond;
    private double mDriftUncertaintyNanosPerSecond;
    private int mFlags;
    private long mFullBiasNanos;
    private int mHardwareClockDiscontinuityCount;
    private int mLeapSecond;
    private long mTimeNanos;
    private double mTimeUncertaintyNanos;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.location.GnssClock.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.location.GnssClock.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.location.GnssClock.<clinit>():void");
    }

    private void resetFlag(int r1) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.location.GnssClock.resetFlag(int):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 5 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.location.GnssClock.resetFlag(int):void");
    }

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
        return isFlagSet(HAS_LEAP_SECOND);
    }

    public int getLeapSecond() {
        return this.mLeapSecond;
    }

    public void setLeapSecond(int leapSecond) {
        setFlag(HAS_LEAP_SECOND);
        this.mLeapSecond = leapSecond;
    }

    public void resetLeapSecond() {
        resetFlag(HAS_LEAP_SECOND);
        this.mLeapSecond = KeymasterDefs.KM_BIGNUM;
    }

    public long getTimeNanos() {
        return this.mTimeNanos;
    }

    public void setTimeNanos(long timeNanos) {
        this.mTimeNanos = timeNanos;
    }

    public boolean hasTimeUncertaintyNanos() {
        return isFlagSet(HAS_TIME_UNCERTAINTY);
    }

    public double getTimeUncertaintyNanos() {
        return this.mTimeUncertaintyNanos;
    }

    public void setTimeUncertaintyNanos(double timeUncertaintyNanos) {
        setFlag(HAS_TIME_UNCERTAINTY);
        this.mTimeUncertaintyNanos = timeUncertaintyNanos;
    }

    public void resetTimeUncertaintyNanos() {
        resetFlag(HAS_TIME_UNCERTAINTY);
        this.mTimeUncertaintyNanos = Double.NaN;
    }

    public boolean hasFullBiasNanos() {
        return isFlagSet(HAS_FULL_BIAS);
    }

    public long getFullBiasNanos() {
        return this.mFullBiasNanos;
    }

    public void setFullBiasNanos(long value) {
        setFlag(HAS_FULL_BIAS);
        this.mFullBiasNanos = value;
    }

    public void resetFullBiasNanos() {
        resetFlag(HAS_FULL_BIAS);
        this.mFullBiasNanos = Long.MIN_VALUE;
    }

    public boolean hasBiasNanos() {
        return isFlagSet(HAS_BIAS);
    }

    public double getBiasNanos() {
        return this.mBiasNanos;
    }

    public void setBiasNanos(double biasNanos) {
        setFlag(HAS_BIAS);
        this.mBiasNanos = biasNanos;
    }

    public void resetBiasNanos() {
        resetFlag(HAS_BIAS);
        this.mBiasNanos = Double.NaN;
    }

    public boolean hasBiasUncertaintyNanos() {
        return isFlagSet(HAS_BIAS_UNCERTAINTY);
    }

    public double getBiasUncertaintyNanos() {
        return this.mBiasUncertaintyNanos;
    }

    public void setBiasUncertaintyNanos(double biasUncertaintyNanos) {
        setFlag(HAS_BIAS_UNCERTAINTY);
        this.mBiasUncertaintyNanos = biasUncertaintyNanos;
    }

    public void resetBiasUncertaintyNanos() {
        resetFlag(HAS_BIAS_UNCERTAINTY);
        this.mBiasUncertaintyNanos = Double.NaN;
    }

    public boolean hasDriftNanosPerSecond() {
        return isFlagSet(HAS_DRIFT);
    }

    public double getDriftNanosPerSecond() {
        return this.mDriftNanosPerSecond;
    }

    public void setDriftNanosPerSecond(double driftNanosPerSecond) {
        setFlag(HAS_DRIFT);
        this.mDriftNanosPerSecond = driftNanosPerSecond;
    }

    public void resetDriftNanosPerSecond() {
        resetFlag(HAS_DRIFT);
        this.mDriftNanosPerSecond = Double.NaN;
    }

    public boolean hasDriftUncertaintyNanosPerSecond() {
        return isFlagSet(HAS_DRIFT_UNCERTAINTY);
    }

    public double getDriftUncertaintyNanosPerSecond() {
        return this.mDriftUncertaintyNanosPerSecond;
    }

    public void setDriftUncertaintyNanosPerSecond(double driftUncertaintyNanosPerSecond) {
        setFlag(HAS_DRIFT_UNCERTAINTY);
        this.mDriftUncertaintyNanosPerSecond = driftUncertaintyNanosPerSecond;
    }

    public void resetDriftUncertaintyNanosPerSecond() {
        resetFlag(HAS_DRIFT_UNCERTAINTY);
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
        return HAS_NO_FLAGS;
    }

    public String toString() {
        Double valueOf;
        Long valueOf2;
        Double d = null;
        String format = "   %-15s = %s\n";
        String formatWithUncertainty = "   %-15s = %-25s   %-26s = %s\n";
        StringBuilder builder = new StringBuilder("GnssClock:\n");
        String str = "   %-15s = %s\n";
        Object[] objArr = new Object[HAS_TIME_UNCERTAINTY];
        objArr[HAS_NO_FLAGS] = "LeapSecond";
        objArr[HAS_LEAP_SECOND] = hasLeapSecond() ? Integer.valueOf(this.mLeapSecond) : null;
        builder.append(String.format(str, objArr));
        str = "   %-15s = %-25s   %-26s = %s\n";
        objArr = new Object[HAS_FULL_BIAS];
        objArr[HAS_NO_FLAGS] = "TimeNanos";
        objArr[HAS_LEAP_SECOND] = Long.valueOf(this.mTimeNanos);
        objArr[HAS_TIME_UNCERTAINTY] = "TimeUncertaintyNanos";
        if (hasTimeUncertaintyNanos()) {
            valueOf = Double.valueOf(this.mTimeUncertaintyNanos);
        } else {
            valueOf = null;
        }
        objArr[3] = valueOf;
        builder.append(String.format(str, objArr));
        str = "   %-15s = %s\n";
        objArr = new Object[HAS_TIME_UNCERTAINTY];
        objArr[HAS_NO_FLAGS] = "FullBiasNanos";
        if (hasFullBiasNanos()) {
            valueOf2 = Long.valueOf(this.mFullBiasNanos);
        } else {
            valueOf2 = null;
        }
        objArr[HAS_LEAP_SECOND] = valueOf2;
        builder.append(String.format(str, objArr));
        str = "   %-15s = %-25s   %-26s = %s\n";
        objArr = new Object[HAS_FULL_BIAS];
        objArr[HAS_NO_FLAGS] = "BiasNanos";
        if (hasBiasNanos()) {
            valueOf = Double.valueOf(this.mBiasNanos);
        } else {
            valueOf = null;
        }
        objArr[HAS_LEAP_SECOND] = valueOf;
        objArr[HAS_TIME_UNCERTAINTY] = "BiasUncertaintyNanos";
        if (hasBiasUncertaintyNanos()) {
            valueOf = Double.valueOf(this.mBiasUncertaintyNanos);
        } else {
            valueOf = null;
        }
        objArr[3] = valueOf;
        builder.append(String.format(str, objArr));
        str = "   %-15s = %-25s   %-26s = %s\n";
        objArr = new Object[HAS_FULL_BIAS];
        objArr[HAS_NO_FLAGS] = "DriftNanosPerSecond";
        if (hasDriftNanosPerSecond()) {
            valueOf = Double.valueOf(this.mDriftNanosPerSecond);
        } else {
            valueOf = null;
        }
        objArr[HAS_LEAP_SECOND] = valueOf;
        objArr[HAS_TIME_UNCERTAINTY] = "DriftUncertaintyNanosPerSecond";
        if (hasDriftUncertaintyNanosPerSecond()) {
            d = Double.valueOf(this.mDriftUncertaintyNanosPerSecond);
        }
        objArr[3] = d;
        builder.append(String.format(str, objArr));
        Object[] objArr2 = new Object[HAS_TIME_UNCERTAINTY];
        objArr2[HAS_NO_FLAGS] = "HardwareClockDiscontinuityCount";
        objArr2[HAS_LEAP_SECOND] = Integer.valueOf(this.mHardwareClockDiscontinuityCount);
        builder.append(String.format("   %-15s = %s\n", objArr2));
        return builder.toString();
    }

    private void initialize() {
        this.mFlags = HAS_NO_FLAGS;
        resetLeapSecond();
        setTimeNanos(Long.MIN_VALUE);
        resetTimeUncertaintyNanos();
        resetFullBiasNanos();
        resetBiasNanos();
        resetBiasUncertaintyNanos();
        resetDriftNanosPerSecond();
        resetDriftUncertaintyNanosPerSecond();
        setHardwareClockDiscontinuityCount(KeymasterDefs.KM_BIGNUM);
    }

    private void setFlag(int flag) {
        this.mFlags |= flag;
    }

    private boolean isFlagSet(int flag) {
        return (this.mFlags & flag) == flag;
    }
}
