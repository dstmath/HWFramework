package android.location;

import android.net.LinkQualityInfo;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class GnssMeasurement implements Parcelable {
    private static final int ADR_ALL = 7;
    public static final int ADR_STATE_CYCLE_SLIP = 4;
    public static final int ADR_STATE_RESET = 2;
    public static final int ADR_STATE_UNKNOWN = 0;
    public static final int ADR_STATE_VALID = 1;
    public static final Creator<GnssMeasurement> CREATOR = null;
    private static final int HAS_CARRIER_CYCLES = 1024;
    private static final int HAS_CARRIER_FREQUENCY = 512;
    private static final int HAS_CARRIER_PHASE = 2048;
    private static final int HAS_CARRIER_PHASE_UNCERTAINTY = 4096;
    private static final int HAS_NO_FLAGS = 0;
    private static final int HAS_SNR = 1;
    public static final int MULTIPATH_INDICATOR_DETECTED = 1;
    public static final int MULTIPATH_INDICATOR_NOT_DETECTED = 2;
    public static final int MULTIPATH_INDICATOR_NOT_USED = 2;
    public static final int MULTIPATH_INDICATOR_UNKNOWN = 0;
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
    public static final int STATE_MSEC_AMBIGUOUS = 16;
    public static final int STATE_SBAS_SYNC = 8192;
    public static final int STATE_SUBFRAME_SYNC = 4;
    public static final int STATE_SYMBOL_SYNC = 32;
    public static final int STATE_TOW_DECODED = 8;
    public static final int STATE_UNKNOWN = 0;
    private double mAccumulatedDeltaRangeMeters;
    private int mAccumulatedDeltaRangeState;
    private double mAccumulatedDeltaRangeUncertaintyMeters;
    private long mCarrierCycles;
    private float mCarrierFrequencyHz;
    private double mCarrierPhase;
    private double mCarrierPhaseUncertainty;
    private double mCn0DbHz;
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.location.GnssMeasurement.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.location.GnssMeasurement.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.location.GnssMeasurement.<clinit>():void");
    }

    private void resetFlag(int r1) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.location.GnssMeasurement.resetFlag(int):void
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
        throw new UnsupportedOperationException("Method not decompiled: android.location.GnssMeasurement.resetFlag(int):void");
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
        if ((this.mState & STATE_CODE_LOCK) != 0) {
            builder.append("CodeLock|");
        }
        if ((this.mState & STATE_BIT_SYNC) != 0) {
            builder.append("BitSync|");
        }
        if ((this.mState & STATE_SUBFRAME_SYNC) != 0) {
            builder.append("SubframeSync|");
        }
        if ((this.mState & STATE_TOW_DECODED) != 0) {
            builder.append("TowDecoded|");
        }
        if ((this.mState & STATE_MSEC_AMBIGUOUS) != 0) {
            builder.append("MsecAmbiguous|");
        }
        if ((this.mState & STATE_SYMBOL_SYNC) != 0) {
            builder.append("SymbolSync|");
        }
        if ((this.mState & STATE_GLO_STRING_SYNC) != 0) {
            builder.append("GloStringSync|");
        }
        if ((this.mState & STATE_GLO_TOD_DECODED) != 0) {
            builder.append("GloTodDecoded|");
        }
        if ((this.mState & STATE_BDS_D2_BIT_SYNC) != 0) {
            builder.append("BdsD2BitSync|");
        }
        if ((this.mState & STATE_BDS_D2_SUBFRAME_SYNC) != 0) {
            builder.append("BdsD2SubframeSync|");
        }
        if ((this.mState & STATE_GAL_E1BC_CODE_LOCK) != 0) {
            builder.append("GalE1bcCodeLock|");
        }
        if ((this.mState & STATE_GAL_E1C_2ND_CODE_LOCK) != 0) {
            builder.append("E1c2ndCodeLock|");
        }
        if ((this.mState & STATE_GAL_E1B_PAGE_SYNC) != 0) {
            builder.append("GalE1bPageSync|");
        }
        if ((this.mState & STATE_SBAS_SYNC) != 0) {
            builder.append("SbasSync|");
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
        if ((this.mAccumulatedDeltaRangeState & STATE_CODE_LOCK) == STATE_CODE_LOCK) {
            builder.append("Valid|");
        }
        if ((this.mAccumulatedDeltaRangeState & STATE_BIT_SYNC) == STATE_BIT_SYNC) {
            builder.append("Reset|");
        }
        if ((this.mAccumulatedDeltaRangeState & STATE_SUBFRAME_SYNC) == STATE_SUBFRAME_SYNC) {
            builder.append("CycleSlip|");
        }
        int remainingStates = this.mAccumulatedDeltaRangeState & -8;
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
        return isFlagSet(STATE_BDS_D2_SUBFRAME_SYNC);
    }

    public float getCarrierFrequencyHz() {
        return this.mCarrierFrequencyHz;
    }

    public void setCarrierFrequencyHz(float carrierFrequencyHz) {
        setFlag(STATE_BDS_D2_SUBFRAME_SYNC);
        this.mCarrierFrequencyHz = carrierFrequencyHz;
    }

    public void resetCarrierFrequencyHz() {
        resetFlag(STATE_BDS_D2_SUBFRAME_SYNC);
        this.mCarrierFrequencyHz = Float.NaN;
    }

    public boolean hasCarrierCycles() {
        return isFlagSet(STATE_GAL_E1BC_CODE_LOCK);
    }

    public long getCarrierCycles() {
        return this.mCarrierCycles;
    }

    public void setCarrierCycles(long value) {
        setFlag(STATE_GAL_E1BC_CODE_LOCK);
        this.mCarrierCycles = value;
    }

    public void resetCarrierCycles() {
        resetFlag(STATE_GAL_E1BC_CODE_LOCK);
        this.mCarrierCycles = Long.MIN_VALUE;
    }

    public boolean hasCarrierPhase() {
        return isFlagSet(STATE_GAL_E1C_2ND_CODE_LOCK);
    }

    public double getCarrierPhase() {
        return this.mCarrierPhase;
    }

    public void setCarrierPhase(double value) {
        setFlag(STATE_GAL_E1C_2ND_CODE_LOCK);
        this.mCarrierPhase = value;
    }

    public void resetCarrierPhase() {
        resetFlag(STATE_GAL_E1C_2ND_CODE_LOCK);
        this.mCarrierPhase = Double.NaN;
    }

    public boolean hasCarrierPhaseUncertainty() {
        return isFlagSet(STATE_GAL_E1B_PAGE_SYNC);
    }

    public double getCarrierPhaseUncertainty() {
        return this.mCarrierPhaseUncertainty;
    }

    public void setCarrierPhaseUncertainty(double value) {
        setFlag(STATE_GAL_E1B_PAGE_SYNC);
        this.mCarrierPhaseUncertainty = value;
    }

    public void resetCarrierPhaseUncertainty() {
        resetFlag(STATE_GAL_E1B_PAGE_SYNC);
        this.mCarrierPhaseUncertainty = Double.NaN;
    }

    public int getMultipathIndicator() {
        return this.mMultipathIndicator;
    }

    public void setMultipathIndicator(int value) {
        this.mMultipathIndicator = value;
    }

    private String getMultipathIndicatorString() {
        switch (this.mMultipathIndicator) {
            case MULTIPATH_INDICATOR_UNKNOWN /*0*/:
                return "Unknown";
            case STATE_CODE_LOCK /*1*/:
                return "Detected";
            case STATE_BIT_SYNC /*2*/:
                return "NotUsed";
            default:
                return "<Invalid:" + this.mMultipathIndicator + ">";
        }
    }

    public boolean hasSnrInDb() {
        return isFlagSet(STATE_CODE_LOCK);
    }

    public double getSnrInDb() {
        return this.mSnrInDb;
    }

    public void setSnrInDb(double snrInDb) {
        setFlag(STATE_CODE_LOCK);
        this.mSnrInDb = snrInDb;
    }

    public void resetSnrInDb() {
        resetFlag(STATE_CODE_LOCK);
        this.mSnrInDb = Double.NaN;
    }

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
    }

    public int describeContents() {
        return MULTIPATH_INDICATOR_UNKNOWN;
    }

    public String toString() {
        Long valueOf;
        Double valueOf2;
        Double d = null;
        String format = "   %-29s = %s\n";
        String formatWithUncertainty = "   %-29s = %-25s   %-40s = %s\n";
        StringBuilder builder = new StringBuilder("GnssMeasurement:\n");
        Object[] objArr = new Object[STATE_BIT_SYNC];
        objArr[MULTIPATH_INDICATOR_UNKNOWN] = "Svid";
        objArr[STATE_CODE_LOCK] = Integer.valueOf(this.mSvid);
        builder.append(String.format("   %-29s = %s\n", objArr));
        objArr = new Object[STATE_BIT_SYNC];
        objArr[MULTIPATH_INDICATOR_UNKNOWN] = "ConstellationType";
        objArr[STATE_CODE_LOCK] = Integer.valueOf(this.mConstellationType);
        builder.append(String.format("   %-29s = %s\n", objArr));
        objArr = new Object[STATE_BIT_SYNC];
        objArr[MULTIPATH_INDICATOR_UNKNOWN] = "TimeOffsetNanos";
        objArr[STATE_CODE_LOCK] = Double.valueOf(this.mTimeOffsetNanos);
        builder.append(String.format("   %-29s = %s\n", objArr));
        objArr = new Object[STATE_BIT_SYNC];
        objArr[MULTIPATH_INDICATOR_UNKNOWN] = "State";
        objArr[STATE_CODE_LOCK] = getStateString();
        builder.append(String.format("   %-29s = %s\n", objArr));
        objArr = new Object[STATE_SUBFRAME_SYNC];
        objArr[MULTIPATH_INDICATOR_UNKNOWN] = "ReceivedSvTimeNanos";
        objArr[STATE_CODE_LOCK] = Long.valueOf(this.mReceivedSvTimeNanos);
        objArr[STATE_BIT_SYNC] = "ReceivedSvTimeUncertaintyNanos";
        objArr[3] = Long.valueOf(this.mReceivedSvTimeUncertaintyNanos);
        builder.append(String.format("   %-29s = %-25s   %-40s = %s\n", objArr));
        objArr = new Object[STATE_BIT_SYNC];
        objArr[MULTIPATH_INDICATOR_UNKNOWN] = "Cn0DbHz";
        objArr[STATE_CODE_LOCK] = Double.valueOf(this.mCn0DbHz);
        builder.append(String.format("   %-29s = %s\n", objArr));
        objArr = new Object[STATE_SUBFRAME_SYNC];
        objArr[MULTIPATH_INDICATOR_UNKNOWN] = "PseudorangeRateMetersPerSecond";
        objArr[STATE_CODE_LOCK] = Double.valueOf(this.mPseudorangeRateMetersPerSecond);
        objArr[STATE_BIT_SYNC] = "PseudorangeRateUncertaintyMetersPerSecond";
        objArr[3] = Double.valueOf(this.mPseudorangeRateUncertaintyMetersPerSecond);
        builder.append(String.format("   %-29s = %-25s   %-40s = %s\n", objArr));
        objArr = new Object[STATE_BIT_SYNC];
        objArr[MULTIPATH_INDICATOR_UNKNOWN] = "AccumulatedDeltaRangeState";
        objArr[STATE_CODE_LOCK] = getAccumulatedDeltaRangeStateString();
        builder.append(String.format("   %-29s = %s\n", objArr));
        objArr = new Object[STATE_SUBFRAME_SYNC];
        objArr[MULTIPATH_INDICATOR_UNKNOWN] = "AccumulatedDeltaRangeMeters";
        objArr[STATE_CODE_LOCK] = Double.valueOf(this.mAccumulatedDeltaRangeMeters);
        objArr[STATE_BIT_SYNC] = "AccumulatedDeltaRangeUncertaintyMeters";
        objArr[3] = Double.valueOf(this.mAccumulatedDeltaRangeUncertaintyMeters);
        builder.append(String.format("   %-29s = %-25s   %-40s = %s\n", objArr));
        String str = "   %-29s = %s\n";
        Object[] objArr2 = new Object[STATE_BIT_SYNC];
        objArr2[MULTIPATH_INDICATOR_UNKNOWN] = "CarrierFrequencyHz";
        objArr2[STATE_CODE_LOCK] = hasCarrierFrequencyHz() ? Float.valueOf(this.mCarrierFrequencyHz) : null;
        builder.append(String.format(str, objArr2));
        str = "   %-29s = %s\n";
        objArr2 = new Object[STATE_BIT_SYNC];
        objArr2[MULTIPATH_INDICATOR_UNKNOWN] = "CarrierCycles";
        if (hasCarrierCycles()) {
            valueOf = Long.valueOf(this.mCarrierCycles);
        } else {
            valueOf = null;
        }
        objArr2[STATE_CODE_LOCK] = valueOf;
        builder.append(String.format(str, objArr2));
        str = "   %-29s = %-25s   %-40s = %s\n";
        objArr2 = new Object[STATE_SUBFRAME_SYNC];
        objArr2[MULTIPATH_INDICATOR_UNKNOWN] = "CarrierPhase";
        if (hasCarrierPhase()) {
            valueOf2 = Double.valueOf(this.mCarrierPhase);
        } else {
            valueOf2 = null;
        }
        objArr2[STATE_CODE_LOCK] = valueOf2;
        objArr2[STATE_BIT_SYNC] = "CarrierPhaseUncertainty";
        if (hasCarrierPhaseUncertainty()) {
            valueOf2 = Double.valueOf(this.mCarrierPhaseUncertainty);
        } else {
            valueOf2 = null;
        }
        objArr2[3] = valueOf2;
        builder.append(String.format(str, objArr2));
        objArr = new Object[STATE_BIT_SYNC];
        objArr[MULTIPATH_INDICATOR_UNKNOWN] = "MultipathIndicator";
        objArr[STATE_CODE_LOCK] = getMultipathIndicatorString();
        builder.append(String.format("   %-29s = %s\n", objArr));
        String str2 = "   %-29s = %s\n";
        objArr = new Object[STATE_BIT_SYNC];
        objArr[MULTIPATH_INDICATOR_UNKNOWN] = "SnrInDb";
        if (hasSnrInDb()) {
            d = Double.valueOf(this.mSnrInDb);
        }
        objArr[STATE_CODE_LOCK] = d;
        builder.append(String.format(str2, objArr));
        return builder.toString();
    }

    private void initialize() {
        this.mFlags = MULTIPATH_INDICATOR_UNKNOWN;
        setSvid(MULTIPATH_INDICATOR_UNKNOWN);
        setTimeOffsetNanos(-9.223372036854776E18d);
        setState(MULTIPATH_INDICATOR_UNKNOWN);
        setReceivedSvTimeNanos(Long.MIN_VALUE);
        setReceivedSvTimeUncertaintyNanos(LinkQualityInfo.UNKNOWN_LONG);
        setCn0DbHz(Double.MIN_VALUE);
        setPseudorangeRateMetersPerSecond(Double.MIN_VALUE);
        setPseudorangeRateUncertaintyMetersPerSecond(Double.MIN_VALUE);
        setAccumulatedDeltaRangeState(MULTIPATH_INDICATOR_UNKNOWN);
        setAccumulatedDeltaRangeMeters(Double.MIN_VALUE);
        setAccumulatedDeltaRangeUncertaintyMeters(Double.MIN_VALUE);
        resetCarrierFrequencyHz();
        resetCarrierCycles();
        resetCarrierPhase();
        resetCarrierPhaseUncertainty();
        setMultipathIndicator(MULTIPATH_INDICATOR_UNKNOWN);
        resetSnrInDb();
    }

    private void setFlag(int flag) {
        this.mFlags |= flag;
    }

    private boolean isFlagSet(int flag) {
        return (this.mFlags & flag) == flag;
    }
}
