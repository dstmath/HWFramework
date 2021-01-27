package android.location;

import android.os.Parcel;
import android.os.Parcelable;
import com.android.internal.telephony.IccCardConstants;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public final class GnssMeasurement implements Parcelable {
    public static final int ADR_STATE_ALL = 31;
    public static final int ADR_STATE_CYCLE_SLIP = 4;
    public static final int ADR_STATE_HALF_CYCLE_REPORTED = 16;
    public static final int ADR_STATE_HALF_CYCLE_RESOLVED = 8;
    public static final int ADR_STATE_RESET = 2;
    public static final int ADR_STATE_UNKNOWN = 0;
    public static final int ADR_STATE_VALID = 1;
    public static final Parcelable.Creator<GnssMeasurement> CREATOR = new Parcelable.Creator<GnssMeasurement>() {
        /* class android.location.GnssMeasurement.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public GnssMeasurement createFromParcel(Parcel parcel) {
            GnssMeasurement gnssMeasurement = new GnssMeasurement();
            gnssMeasurement.mFlags = parcel.readInt();
            gnssMeasurement.mSvid = parcel.readInt();
            gnssMeasurement.mConstellationType = parcel.readInt();
            gnssMeasurement.mTimeOffsetNanos = parcel.readDouble();
            gnssMeasurement.mState = parcel.readInt();
            gnssMeasurement.mReceivedSvTimeNanos = parcel.readLong();
            gnssMeasurement.mReceivedSvTimeUncertaintyNanos = parcel.readLong();
            gnssMeasurement.mCn0DbHz = parcel.readDouble();
            gnssMeasurement.mPseudorangeRateMetersPerSecond = parcel.readDouble();
            gnssMeasurement.mPseudorangeRateUncertaintyMetersPerSecond = parcel.readDouble();
            gnssMeasurement.mAccumulatedDeltaRangeState = parcel.readInt();
            gnssMeasurement.mAccumulatedDeltaRangeMeters = parcel.readDouble();
            gnssMeasurement.mAccumulatedDeltaRangeUncertaintyMeters = parcel.readDouble();
            gnssMeasurement.mCarrierFrequencyHz = parcel.readFloat();
            gnssMeasurement.mCarrierCycles = parcel.readLong();
            gnssMeasurement.mCarrierPhase = parcel.readDouble();
            gnssMeasurement.mCarrierPhaseUncertainty = parcel.readDouble();
            gnssMeasurement.mMultipathIndicator = parcel.readInt();
            gnssMeasurement.mSnrInDb = parcel.readDouble();
            gnssMeasurement.mAutomaticGainControlLevelInDb = parcel.readDouble();
            gnssMeasurement.mCodeType = parcel.readString();
            return gnssMeasurement;
        }

        @Override // android.os.Parcelable.Creator
        public GnssMeasurement[] newArray(int i) {
            return new GnssMeasurement[i];
        }
    };
    private static final int HAS_AUTOMATIC_GAIN_CONTROL = 8192;
    private static final int HAS_CARRIER_CYCLES = 1024;
    private static final int HAS_CARRIER_FREQUENCY = 512;
    private static final int HAS_CARRIER_PHASE = 2048;
    private static final int HAS_CARRIER_PHASE_UNCERTAINTY = 4096;
    private static final int HAS_CODE_TYPE = 16384;
    private static final int HAS_NO_FLAGS = 0;
    private static final int HAS_SNR = 1;
    public static final int MULTIPATH_INDICATOR_DETECTED = 1;
    public static final int MULTIPATH_INDICATOR_NOT_DETECTED = 2;
    public static final int MULTIPATH_INDICATOR_UNKNOWN = 0;
    public static final int STATE_2ND_CODE_LOCK = 65536;
    private static final int STATE_ALL = 16383;
    public static final int STATE_BDS_D2_BIT_SYNC = 256;
    public static final int STATE_BDS_D2_SUBFRAME_SYNC = 512;
    public static final int STATE_BIT_SYNC = 2;
    public static final int STATE_CODE_LOCK = 1;
    public static final int STATE_GAL_E1BC_CODE_LOCK = 1024;
    public static final int STATE_GAL_E1B_PAGE_SYNC = 4096;
    public static final int STATE_GAL_E1C_2ND_CODE_LOCK = 2048;
    public static final int STATE_GLO_STRING_SYNC = 64;
    public static final int STATE_GLO_TOD_DECODED = 128;
    public static final int STATE_GLO_TOD_KNOWN = 32768;
    public static final int STATE_MSEC_AMBIGUOUS = 16;
    public static final int STATE_SBAS_SYNC = 8192;
    public static final int STATE_SUBFRAME_SYNC = 4;
    public static final int STATE_SYMBOL_SYNC = 32;
    public static final int STATE_TOW_DECODED = 8;
    public static final int STATE_TOW_KNOWN = 16384;
    public static final int STATE_UNKNOWN = 0;
    private double mAccumulatedDeltaRangeMeters;
    private int mAccumulatedDeltaRangeState;
    private double mAccumulatedDeltaRangeUncertaintyMeters;
    private double mAutomaticGainControlLevelInDb;
    private long mCarrierCycles;
    private float mCarrierFrequencyHz;
    private double mCarrierPhase;
    private double mCarrierPhaseUncertainty;
    private double mCn0DbHz;
    private String mCodeType;
    private int mConstellationType;
    private int mFlags;
    private int mMultipathIndicator;
    private double mPseudorangeRateMetersPerSecond;
    private double mPseudorangeRateUncertaintyMetersPerSecond;
    private long mReceivedSvTimeNanos;
    private long mReceivedSvTimeUncertaintyNanos;
    private double mSnrInDb;
    private int mState;
    private int mSvid;
    private double mTimeOffsetNanos;

    @Retention(RetentionPolicy.SOURCE)
    public @interface AdrState {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface MultipathIndicator {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface State {
    }

    public GnssMeasurement() {
        initialize();
    }

    public void set(GnssMeasurement measurement) {
        this.mFlags = measurement.mFlags;
        this.mSvid = measurement.mSvid;
        this.mConstellationType = measurement.mConstellationType;
        this.mTimeOffsetNanos = measurement.mTimeOffsetNanos;
        this.mState = measurement.mState;
        this.mReceivedSvTimeNanos = measurement.mReceivedSvTimeNanos;
        this.mReceivedSvTimeUncertaintyNanos = measurement.mReceivedSvTimeUncertaintyNanos;
        this.mCn0DbHz = measurement.mCn0DbHz;
        this.mPseudorangeRateMetersPerSecond = measurement.mPseudorangeRateMetersPerSecond;
        this.mPseudorangeRateUncertaintyMetersPerSecond = measurement.mPseudorangeRateUncertaintyMetersPerSecond;
        this.mAccumulatedDeltaRangeState = measurement.mAccumulatedDeltaRangeState;
        this.mAccumulatedDeltaRangeMeters = measurement.mAccumulatedDeltaRangeMeters;
        this.mAccumulatedDeltaRangeUncertaintyMeters = measurement.mAccumulatedDeltaRangeUncertaintyMeters;
        this.mCarrierFrequencyHz = measurement.mCarrierFrequencyHz;
        this.mCarrierCycles = measurement.mCarrierCycles;
        this.mCarrierPhase = measurement.mCarrierPhase;
        this.mCarrierPhaseUncertainty = measurement.mCarrierPhaseUncertainty;
        this.mMultipathIndicator = measurement.mMultipathIndicator;
        this.mSnrInDb = measurement.mSnrInDb;
        this.mAutomaticGainControlLevelInDb = measurement.mAutomaticGainControlLevelInDb;
        this.mCodeType = measurement.mCodeType;
    }

    public void reset() {
        initialize();
    }

    public int getSvid() {
        return this.mSvid;
    }

    public void setSvid(int value) {
        this.mSvid = value;
    }

    public int getConstellationType() {
        return this.mConstellationType;
    }

    public void setConstellationType(int value) {
        this.mConstellationType = value;
    }

    public double getTimeOffsetNanos() {
        return this.mTimeOffsetNanos;
    }

    public void setTimeOffsetNanos(double value) {
        this.mTimeOffsetNanos = value;
    }

    public int getState() {
        return this.mState;
    }

    public void setState(int value) {
        this.mState = value;
    }

    private String getStateString() {
        if (this.mState == 0) {
            return "Unknown";
        }
        StringBuilder builder = new StringBuilder();
        if ((this.mState & 1) != 0) {
            builder.append("CodeLock|");
        }
        if ((this.mState & 2) != 0) {
            builder.append("BitSync|");
        }
        if ((this.mState & 4) != 0) {
            builder.append("SubframeSync|");
        }
        if ((this.mState & 8) != 0) {
            builder.append("TowDecoded|");
        }
        if ((this.mState & 16384) != 0) {
            builder.append("TowKnown|");
        }
        if ((this.mState & 16) != 0) {
            builder.append("MsecAmbiguous|");
        }
        if ((this.mState & 32) != 0) {
            builder.append("SymbolSync|");
        }
        if ((this.mState & 64) != 0) {
            builder.append("GloStringSync|");
        }
        if ((this.mState & 128) != 0) {
            builder.append("GloTodDecoded|");
        }
        if ((this.mState & 32768) != 0) {
            builder.append("GloTodKnown|");
        }
        if ((this.mState & 256) != 0) {
            builder.append("BdsD2BitSync|");
        }
        if ((this.mState & 512) != 0) {
            builder.append("BdsD2SubframeSync|");
        }
        if ((this.mState & 1024) != 0) {
            builder.append("GalE1bcCodeLock|");
        }
        if ((this.mState & 2048) != 0) {
            builder.append("E1c2ndCodeLock|");
        }
        if ((this.mState & 4096) != 0) {
            builder.append("GalE1bPageSync|");
        }
        if ((this.mState & 8192) != 0) {
            builder.append("SbasSync|");
        }
        if ((this.mState & 65536) != 0) {
            builder.append("2ndCodeLock|");
        }
        int remainingStates = this.mState & -16384;
        if (remainingStates > 0) {
            builder.append("Other(");
            builder.append(Integer.toBinaryString(remainingStates));
            builder.append(")|");
        }
        builder.setLength(builder.length() - 1);
        return builder.toString();
    }

    public long getReceivedSvTimeNanos() {
        return this.mReceivedSvTimeNanos;
    }

    public void setReceivedSvTimeNanos(long value) {
        this.mReceivedSvTimeNanos = value;
    }

    public long getReceivedSvTimeUncertaintyNanos() {
        return this.mReceivedSvTimeUncertaintyNanos;
    }

    public void setReceivedSvTimeUncertaintyNanos(long value) {
        this.mReceivedSvTimeUncertaintyNanos = value;
    }

    public double getCn0DbHz() {
        return this.mCn0DbHz;
    }

    public void setCn0DbHz(double value) {
        this.mCn0DbHz = value;
    }

    public double getPseudorangeRateMetersPerSecond() {
        return this.mPseudorangeRateMetersPerSecond;
    }

    public void setPseudorangeRateMetersPerSecond(double value) {
        this.mPseudorangeRateMetersPerSecond = value;
    }

    public double getPseudorangeRateUncertaintyMetersPerSecond() {
        return this.mPseudorangeRateUncertaintyMetersPerSecond;
    }

    public void setPseudorangeRateUncertaintyMetersPerSecond(double value) {
        this.mPseudorangeRateUncertaintyMetersPerSecond = value;
    }

    public int getAccumulatedDeltaRangeState() {
        return this.mAccumulatedDeltaRangeState;
    }

    public void setAccumulatedDeltaRangeState(int value) {
        this.mAccumulatedDeltaRangeState = value;
    }

    private String getAccumulatedDeltaRangeStateString() {
        if (this.mAccumulatedDeltaRangeState == 0) {
            return "Unknown";
        }
        StringBuilder builder = new StringBuilder();
        if ((this.mAccumulatedDeltaRangeState & 1) == 1) {
            builder.append("Valid|");
        }
        if ((this.mAccumulatedDeltaRangeState & 2) == 2) {
            builder.append("Reset|");
        }
        if ((this.mAccumulatedDeltaRangeState & 4) == 4) {
            builder.append("CycleSlip|");
        }
        if ((this.mAccumulatedDeltaRangeState & 8) == 8) {
            builder.append("HalfCycleResolved|");
        }
        if ((this.mAccumulatedDeltaRangeState & 16) == 16) {
            builder.append("HalfCycleReported|");
        }
        int remainingStates = this.mAccumulatedDeltaRangeState & -32;
        if (remainingStates > 0) {
            builder.append("Other(");
            builder.append(Integer.toBinaryString(remainingStates));
            builder.append(")|");
        }
        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }

    public double getAccumulatedDeltaRangeMeters() {
        return this.mAccumulatedDeltaRangeMeters;
    }

    public void setAccumulatedDeltaRangeMeters(double value) {
        this.mAccumulatedDeltaRangeMeters = value;
    }

    public double getAccumulatedDeltaRangeUncertaintyMeters() {
        return this.mAccumulatedDeltaRangeUncertaintyMeters;
    }

    public void setAccumulatedDeltaRangeUncertaintyMeters(double value) {
        this.mAccumulatedDeltaRangeUncertaintyMeters = value;
    }

    public boolean hasCarrierFrequencyHz() {
        return isFlagSet(512);
    }

    public float getCarrierFrequencyHz() {
        return this.mCarrierFrequencyHz;
    }

    public void setCarrierFrequencyHz(float carrierFrequencyHz) {
        setFlag(512);
        this.mCarrierFrequencyHz = carrierFrequencyHz;
    }

    public void resetCarrierFrequencyHz() {
        resetFlag(512);
        this.mCarrierFrequencyHz = Float.NaN;
    }

    @Deprecated
    public boolean hasCarrierCycles() {
        return isFlagSet(1024);
    }

    @Deprecated
    public long getCarrierCycles() {
        return this.mCarrierCycles;
    }

    @Deprecated
    public void setCarrierCycles(long value) {
        setFlag(1024);
        this.mCarrierCycles = value;
    }

    @Deprecated
    public void resetCarrierCycles() {
        resetFlag(1024);
        this.mCarrierCycles = Long.MIN_VALUE;
    }

    @Deprecated
    public boolean hasCarrierPhase() {
        return isFlagSet(2048);
    }

    @Deprecated
    public double getCarrierPhase() {
        return this.mCarrierPhase;
    }

    @Deprecated
    public void setCarrierPhase(double value) {
        setFlag(2048);
        this.mCarrierPhase = value;
    }

    @Deprecated
    public void resetCarrierPhase() {
        resetFlag(2048);
        this.mCarrierPhase = Double.NaN;
    }

    @Deprecated
    public boolean hasCarrierPhaseUncertainty() {
        return isFlagSet(4096);
    }

    @Deprecated
    public double getCarrierPhaseUncertainty() {
        return this.mCarrierPhaseUncertainty;
    }

    @Deprecated
    public void setCarrierPhaseUncertainty(double value) {
        setFlag(4096);
        this.mCarrierPhaseUncertainty = value;
    }

    @Deprecated
    public void resetCarrierPhaseUncertainty() {
        resetFlag(4096);
        this.mCarrierPhaseUncertainty = Double.NaN;
    }

    public int getMultipathIndicator() {
        return this.mMultipathIndicator;
    }

    public void setMultipathIndicator(int value) {
        this.mMultipathIndicator = value;
    }

    private String getMultipathIndicatorString() {
        int i = this.mMultipathIndicator;
        if (i == 0) {
            return "Unknown";
        }
        if (i == 1) {
            return "Detected";
        }
        if (i == 2) {
            return "NotDetected";
        }
        return "<Invalid: " + this.mMultipathIndicator + ">";
    }

    public boolean hasSnrInDb() {
        return isFlagSet(1);
    }

    public double getSnrInDb() {
        return this.mSnrInDb;
    }

    public void setSnrInDb(double snrInDb) {
        setFlag(1);
        this.mSnrInDb = snrInDb;
    }

    public void resetSnrInDb() {
        resetFlag(1);
        this.mSnrInDb = Double.NaN;
    }

    public boolean hasAutomaticGainControlLevelDb() {
        return isFlagSet(8192);
    }

    public double getAutomaticGainControlLevelDb() {
        return this.mAutomaticGainControlLevelInDb;
    }

    public void setAutomaticGainControlLevelInDb(double agcLevelDb) {
        setFlag(8192);
        this.mAutomaticGainControlLevelInDb = agcLevelDb;
    }

    public void resetAutomaticGainControlLevel() {
        resetFlag(8192);
        this.mAutomaticGainControlLevelInDb = Double.NaN;
    }

    public boolean hasCodeType() {
        return isFlagSet(16384);
    }

    public String getCodeType() {
        return this.mCodeType;
    }

    public void setCodeType(String codeType) {
        setFlag(16384);
        this.mCodeType = codeType;
    }

    public void resetCodeType() {
        resetFlag(16384);
        this.mCodeType = IccCardConstants.INTENT_VALUE_ICC_UNKNOWN;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(this.mFlags);
        parcel.writeInt(this.mSvid);
        parcel.writeInt(this.mConstellationType);
        parcel.writeDouble(this.mTimeOffsetNanos);
        parcel.writeInt(this.mState);
        parcel.writeLong(this.mReceivedSvTimeNanos);
        parcel.writeLong(this.mReceivedSvTimeUncertaintyNanos);
        parcel.writeDouble(this.mCn0DbHz);
        parcel.writeDouble(this.mPseudorangeRateMetersPerSecond);
        parcel.writeDouble(this.mPseudorangeRateUncertaintyMetersPerSecond);
        parcel.writeInt(this.mAccumulatedDeltaRangeState);
        parcel.writeDouble(this.mAccumulatedDeltaRangeMeters);
        parcel.writeDouble(this.mAccumulatedDeltaRangeUncertaintyMeters);
        parcel.writeFloat(this.mCarrierFrequencyHz);
        parcel.writeLong(this.mCarrierCycles);
        parcel.writeDouble(this.mCarrierPhase);
        parcel.writeDouble(this.mCarrierPhaseUncertainty);
        parcel.writeInt(this.mMultipathIndicator);
        parcel.writeDouble(this.mSnrInDb);
        parcel.writeDouble(this.mAutomaticGainControlLevelInDb);
        parcel.writeString(this.mCodeType);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0106: APUT  (r8v18 java.lang.Object[]), (1 ??[boolean, int, float, short, byte, char]), (r11v19 java.lang.Float) */
    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0123: APUT  (r8v20 java.lang.Object[]), (1 ??[boolean, int, float, short, byte, char]), (r11v22 java.lang.Long) */
    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0140: APUT  (r4v9 java.lang.Object[]), (1 ??[boolean, int, float, short, byte, char]), (r8v24 java.lang.Double) */
    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0184: APUT  (r4v13 java.lang.Object[]), (1 ??[boolean, int, float, short, byte, char]), (r8v32 java.lang.Double) */
    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x01a1: APUT  (r4v15 java.lang.Object[]), (1 ??[boolean, int, float, short, byte, char]), (r8v35 java.lang.Double) */
    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x01b8: APUT  (r3v2 java.lang.Object[]), (1 ??[boolean, int, float, short, byte, char]), (r12v1 java.lang.String) */
    public String toString() {
        StringBuilder builder = new StringBuilder("GnssMeasurement:\n");
        builder.append(String.format("   %-29s = %s\n", "Svid", Integer.valueOf(this.mSvid)));
        builder.append(String.format("   %-29s = %s\n", "ConstellationType", Integer.valueOf(this.mConstellationType)));
        builder.append(String.format("   %-29s = %s\n", "TimeOffsetNanos", Double.valueOf(this.mTimeOffsetNanos)));
        builder.append(String.format("   %-29s = %s\n", "State", getStateString()));
        builder.append(String.format("   %-29s = %-25s   %-40s = %s\n", "ReceivedSvTimeNanos", Long.valueOf(this.mReceivedSvTimeNanos), "ReceivedSvTimeUncertaintyNanos", Long.valueOf(this.mReceivedSvTimeUncertaintyNanos)));
        builder.append(String.format("   %-29s = %s\n", "Cn0DbHz", Double.valueOf(this.mCn0DbHz)));
        builder.append(String.format("   %-29s = %-25s   %-40s = %s\n", "PseudorangeRateMetersPerSecond", Double.valueOf(this.mPseudorangeRateMetersPerSecond), "PseudorangeRateUncertaintyMetersPerSecond", Double.valueOf(this.mPseudorangeRateUncertaintyMetersPerSecond)));
        builder.append(String.format("   %-29s = %s\n", "AccumulatedDeltaRangeState", getAccumulatedDeltaRangeStateString()));
        builder.append(String.format("   %-29s = %-25s   %-40s = %s\n", "AccumulatedDeltaRangeMeters", Double.valueOf(this.mAccumulatedDeltaRangeMeters), "AccumulatedDeltaRangeUncertaintyMeters", Double.valueOf(this.mAccumulatedDeltaRangeUncertaintyMeters)));
        Object[] objArr = new Object[2];
        objArr[0] = "CarrierFrequencyHz";
        String str = null;
        objArr[1] = hasCarrierFrequencyHz() ? Float.valueOf(this.mCarrierFrequencyHz) : null;
        builder.append(String.format("   %-29s = %s\n", objArr));
        Object[] objArr2 = new Object[2];
        objArr2[0] = "CarrierCycles";
        objArr2[1] = hasCarrierCycles() ? Long.valueOf(this.mCarrierCycles) : null;
        builder.append(String.format("   %-29s = %s\n", objArr2));
        Object[] objArr3 = new Object[4];
        objArr3[0] = "CarrierPhase";
        objArr3[1] = hasCarrierPhase() ? Double.valueOf(this.mCarrierPhase) : null;
        objArr3[2] = "CarrierPhaseUncertainty";
        objArr3[3] = hasCarrierPhaseUncertainty() ? Double.valueOf(this.mCarrierPhaseUncertainty) : null;
        builder.append(String.format("   %-29s = %-25s   %-40s = %s\n", objArr3));
        builder.append(String.format("   %-29s = %s\n", "MultipathIndicator", getMultipathIndicatorString()));
        Object[] objArr4 = new Object[2];
        objArr4[0] = "SnrInDb";
        objArr4[1] = hasSnrInDb() ? Double.valueOf(this.mSnrInDb) : null;
        builder.append(String.format("   %-29s = %s\n", objArr4));
        Object[] objArr5 = new Object[2];
        objArr5[0] = "AgcLevelDb";
        objArr5[1] = hasAutomaticGainControlLevelDb() ? Double.valueOf(this.mAutomaticGainControlLevelInDb) : null;
        builder.append(String.format("   %-29s = %s\n", objArr5));
        Object[] objArr6 = new Object[2];
        objArr6[0] = "CodeType";
        if (hasCodeType()) {
            str = this.mCodeType;
        }
        objArr6[1] = str;
        builder.append(String.format("   %-29s = %s\n", objArr6));
        return builder.toString();
    }

    private void initialize() {
        this.mFlags = 0;
        setSvid(0);
        setTimeOffsetNanos(-9.223372036854776E18d);
        setState(0);
        setReceivedSvTimeNanos(Long.MIN_VALUE);
        setReceivedSvTimeUncertaintyNanos(Long.MAX_VALUE);
        setCn0DbHz(Double.MIN_VALUE);
        setPseudorangeRateMetersPerSecond(Double.MIN_VALUE);
        setPseudorangeRateUncertaintyMetersPerSecond(Double.MIN_VALUE);
        setAccumulatedDeltaRangeState(0);
        setAccumulatedDeltaRangeMeters(Double.MIN_VALUE);
        setAccumulatedDeltaRangeUncertaintyMeters(Double.MIN_VALUE);
        resetCarrierFrequencyHz();
        resetCarrierCycles();
        resetCarrierPhase();
        resetCarrierPhaseUncertainty();
        setMultipathIndicator(0);
        resetSnrInDb();
        resetAutomaticGainControlLevel();
        resetCodeType();
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
