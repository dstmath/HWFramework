package android.location;

import android.annotation.SystemApi;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Half;

@SystemApi
public class GpsMeasurement implements Parcelable {
    private static final short ADR_ALL = 7;
    public static final short ADR_STATE_CYCLE_SLIP = 4;
    public static final short ADR_STATE_RESET = 2;
    public static final short ADR_STATE_UNKNOWN = 0;
    public static final short ADR_STATE_VALID = 1;
    public static final Parcelable.Creator<GpsMeasurement> CREATOR = new Parcelable.Creator<GpsMeasurement>() {
        /* class android.location.GpsMeasurement.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public GpsMeasurement createFromParcel(Parcel parcel) {
            GpsMeasurement gpsMeasurement = new GpsMeasurement();
            gpsMeasurement.mFlags = parcel.readInt();
            gpsMeasurement.mPrn = parcel.readByte();
            gpsMeasurement.mTimeOffsetInNs = parcel.readDouble();
            gpsMeasurement.mState = (short) parcel.readInt();
            gpsMeasurement.mReceivedGpsTowInNs = parcel.readLong();
            gpsMeasurement.mReceivedGpsTowUncertaintyInNs = parcel.readLong();
            gpsMeasurement.mCn0InDbHz = parcel.readDouble();
            gpsMeasurement.mPseudorangeRateInMetersPerSec = parcel.readDouble();
            gpsMeasurement.mPseudorangeRateUncertaintyInMetersPerSec = parcel.readDouble();
            gpsMeasurement.mAccumulatedDeltaRangeState = (short) parcel.readInt();
            gpsMeasurement.mAccumulatedDeltaRangeInMeters = parcel.readDouble();
            gpsMeasurement.mAccumulatedDeltaRangeUncertaintyInMeters = parcel.readDouble();
            gpsMeasurement.mPseudorangeInMeters = parcel.readDouble();
            gpsMeasurement.mPseudorangeUncertaintyInMeters = parcel.readDouble();
            gpsMeasurement.mCodePhaseInChips = parcel.readDouble();
            gpsMeasurement.mCodePhaseUncertaintyInChips = parcel.readDouble();
            gpsMeasurement.mCarrierFrequencyInHz = parcel.readFloat();
            gpsMeasurement.mCarrierCycles = parcel.readLong();
            gpsMeasurement.mCarrierPhase = parcel.readDouble();
            gpsMeasurement.mCarrierPhaseUncertainty = parcel.readDouble();
            gpsMeasurement.mLossOfLock = parcel.readByte();
            gpsMeasurement.mBitNumber = parcel.readInt();
            gpsMeasurement.mTimeFromLastBitInMs = (short) parcel.readInt();
            gpsMeasurement.mDopplerShiftInHz = parcel.readDouble();
            gpsMeasurement.mDopplerShiftUncertaintyInHz = parcel.readDouble();
            gpsMeasurement.mMultipathIndicator = parcel.readByte();
            gpsMeasurement.mSnrInDb = parcel.readDouble();
            gpsMeasurement.mElevationInDeg = parcel.readDouble();
            gpsMeasurement.mElevationUncertaintyInDeg = parcel.readDouble();
            gpsMeasurement.mAzimuthInDeg = parcel.readDouble();
            gpsMeasurement.mAzimuthUncertaintyInDeg = parcel.readDouble();
            gpsMeasurement.mUsedInFix = parcel.readInt() != 0;
            return gpsMeasurement;
        }

        @Override // android.os.Parcelable.Creator
        public GpsMeasurement[] newArray(int i) {
            return new GpsMeasurement[i];
        }
    };
    private static final int GPS_MEASUREMENT_HAS_UNCORRECTED_PSEUDORANGE_RATE = 262144;
    private static final int HAS_AZIMUTH = 8;
    private static final int HAS_AZIMUTH_UNCERTAINTY = 16;
    private static final int HAS_BIT_NUMBER = 8192;
    private static final int HAS_CARRIER_CYCLES = 1024;
    private static final int HAS_CARRIER_FREQUENCY = 512;
    private static final int HAS_CARRIER_PHASE = 2048;
    private static final int HAS_CARRIER_PHASE_UNCERTAINTY = 4096;
    private static final int HAS_CODE_PHASE = 128;
    private static final int HAS_CODE_PHASE_UNCERTAINTY = 256;
    private static final int HAS_DOPPLER_SHIFT = 32768;
    private static final int HAS_DOPPLER_SHIFT_UNCERTAINTY = 65536;
    private static final int HAS_ELEVATION = 2;
    private static final int HAS_ELEVATION_UNCERTAINTY = 4;
    private static final int HAS_NO_FLAGS = 0;
    private static final int HAS_PSEUDORANGE = 32;
    private static final int HAS_PSEUDORANGE_UNCERTAINTY = 64;
    private static final int HAS_SNR = 1;
    private static final int HAS_TIME_FROM_LAST_BIT = 16384;
    private static final int HAS_USED_IN_FIX = 131072;
    public static final byte LOSS_OF_LOCK_CYCLE_SLIP = 2;
    public static final byte LOSS_OF_LOCK_OK = 1;
    public static final byte LOSS_OF_LOCK_UNKNOWN = 0;
    public static final byte MULTIPATH_INDICATOR_DETECTED = 1;
    public static final byte MULTIPATH_INDICATOR_NOT_USED = 2;
    public static final byte MULTIPATH_INDICATOR_UNKNOWN = 0;
    private static final short STATE_ALL = 31;
    public static final short STATE_BIT_SYNC = 2;
    public static final short STATE_CODE_LOCK = 1;
    public static final short STATE_MSEC_AMBIGUOUS = 16;
    public static final short STATE_SUBFRAME_SYNC = 4;
    public static final short STATE_TOW_DECODED = 8;
    public static final short STATE_UNKNOWN = 0;
    private double mAccumulatedDeltaRangeInMeters;
    private short mAccumulatedDeltaRangeState;
    private double mAccumulatedDeltaRangeUncertaintyInMeters;
    private double mAzimuthInDeg;
    private double mAzimuthUncertaintyInDeg;
    private int mBitNumber;
    private long mCarrierCycles;
    private float mCarrierFrequencyInHz;
    private double mCarrierPhase;
    private double mCarrierPhaseUncertainty;
    private double mCn0InDbHz;
    private double mCodePhaseInChips;
    private double mCodePhaseUncertaintyInChips;
    private double mDopplerShiftInHz;
    private double mDopplerShiftUncertaintyInHz;
    private double mElevationInDeg;
    private double mElevationUncertaintyInDeg;
    private int mFlags;
    private byte mLossOfLock;
    private byte mMultipathIndicator;
    private byte mPrn;
    private double mPseudorangeInMeters;
    private double mPseudorangeRateInMetersPerSec;
    private double mPseudorangeRateUncertaintyInMetersPerSec;
    private double mPseudorangeUncertaintyInMeters;
    private long mReceivedGpsTowInNs;
    private long mReceivedGpsTowUncertaintyInNs;
    private double mSnrInDb;
    private short mState;
    private short mTimeFromLastBitInMs;
    private double mTimeOffsetInNs;
    private boolean mUsedInFix;

    GpsMeasurement() {
        initialize();
    }

    public void set(GpsMeasurement measurement) {
        this.mFlags = measurement.mFlags;
        this.mPrn = measurement.mPrn;
        this.mTimeOffsetInNs = measurement.mTimeOffsetInNs;
        this.mState = measurement.mState;
        this.mReceivedGpsTowInNs = measurement.mReceivedGpsTowInNs;
        this.mReceivedGpsTowUncertaintyInNs = measurement.mReceivedGpsTowUncertaintyInNs;
        this.mCn0InDbHz = measurement.mCn0InDbHz;
        this.mPseudorangeRateInMetersPerSec = measurement.mPseudorangeRateInMetersPerSec;
        this.mPseudorangeRateUncertaintyInMetersPerSec = measurement.mPseudorangeRateUncertaintyInMetersPerSec;
        this.mAccumulatedDeltaRangeState = measurement.mAccumulatedDeltaRangeState;
        this.mAccumulatedDeltaRangeInMeters = measurement.mAccumulatedDeltaRangeInMeters;
        this.mAccumulatedDeltaRangeUncertaintyInMeters = measurement.mAccumulatedDeltaRangeUncertaintyInMeters;
        this.mPseudorangeInMeters = measurement.mPseudorangeInMeters;
        this.mPseudorangeUncertaintyInMeters = measurement.mPseudorangeUncertaintyInMeters;
        this.mCodePhaseInChips = measurement.mCodePhaseInChips;
        this.mCodePhaseUncertaintyInChips = measurement.mCodePhaseUncertaintyInChips;
        this.mCarrierFrequencyInHz = measurement.mCarrierFrequencyInHz;
        this.mCarrierCycles = measurement.mCarrierCycles;
        this.mCarrierPhase = measurement.mCarrierPhase;
        this.mCarrierPhaseUncertainty = measurement.mCarrierPhaseUncertainty;
        this.mLossOfLock = measurement.mLossOfLock;
        this.mBitNumber = measurement.mBitNumber;
        this.mTimeFromLastBitInMs = measurement.mTimeFromLastBitInMs;
        this.mDopplerShiftInHz = measurement.mDopplerShiftInHz;
        this.mDopplerShiftUncertaintyInHz = measurement.mDopplerShiftUncertaintyInHz;
        this.mMultipathIndicator = measurement.mMultipathIndicator;
        this.mSnrInDb = measurement.mSnrInDb;
        this.mElevationInDeg = measurement.mElevationInDeg;
        this.mElevationUncertaintyInDeg = measurement.mElevationUncertaintyInDeg;
        this.mAzimuthInDeg = measurement.mAzimuthInDeg;
        this.mAzimuthUncertaintyInDeg = measurement.mAzimuthUncertaintyInDeg;
        this.mUsedInFix = measurement.mUsedInFix;
    }

    public void reset() {
        initialize();
    }

    public byte getPrn() {
        return this.mPrn;
    }

    public void setPrn(byte value) {
        this.mPrn = value;
    }

    public double getTimeOffsetInNs() {
        return this.mTimeOffsetInNs;
    }

    public void setTimeOffsetInNs(double value) {
        this.mTimeOffsetInNs = value;
    }

    public short getState() {
        return this.mState;
    }

    public void setState(short value) {
        this.mState = value;
    }

    private String getStateString() {
        if (this.mState == 0) {
            return "Unknown";
        }
        StringBuilder builder = new StringBuilder();
        if ((this.mState & 1) == 1) {
            builder.append("CodeLock|");
        }
        if ((this.mState & 2) == 2) {
            builder.append("BitSync|");
        }
        if ((this.mState & 4) == 4) {
            builder.append("SubframeSync|");
        }
        if ((this.mState & 8) == 8) {
            builder.append("TowDecoded|");
        }
        if ((this.mState & 16) == 16) {
            builder.append("MsecAmbiguous");
        }
        int remainingStates = this.mState & -32;
        if (remainingStates > 0) {
            builder.append("Other(");
            builder.append(Integer.toBinaryString(remainingStates));
            builder.append(")|");
        }
        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }

    public long getReceivedGpsTowInNs() {
        return this.mReceivedGpsTowInNs;
    }

    public void setReceivedGpsTowInNs(long value) {
        this.mReceivedGpsTowInNs = value;
    }

    public long getReceivedGpsTowUncertaintyInNs() {
        return this.mReceivedGpsTowUncertaintyInNs;
    }

    public void setReceivedGpsTowUncertaintyInNs(long value) {
        this.mReceivedGpsTowUncertaintyInNs = value;
    }

    public double getCn0InDbHz() {
        return this.mCn0InDbHz;
    }

    public void setCn0InDbHz(double value) {
        this.mCn0InDbHz = value;
    }

    public double getPseudorangeRateInMetersPerSec() {
        return this.mPseudorangeRateInMetersPerSec;
    }

    public void setPseudorangeRateInMetersPerSec(double value) {
        this.mPseudorangeRateInMetersPerSec = value;
    }

    public boolean isPseudorangeRateCorrected() {
        return !isFlagSet(262144);
    }

    public double getPseudorangeRateUncertaintyInMetersPerSec() {
        return this.mPseudorangeRateUncertaintyInMetersPerSec;
    }

    public void setPseudorangeRateUncertaintyInMetersPerSec(double value) {
        this.mPseudorangeRateUncertaintyInMetersPerSec = value;
    }

    public short getAccumulatedDeltaRangeState() {
        return this.mAccumulatedDeltaRangeState;
    }

    public void setAccumulatedDeltaRangeState(short value) {
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
        int remainingStates = this.mAccumulatedDeltaRangeState & -8;
        if (remainingStates > 0) {
            builder.append("Other(");
            builder.append(Integer.toBinaryString(remainingStates));
            builder.append(")|");
        }
        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }

    public double getAccumulatedDeltaRangeInMeters() {
        return this.mAccumulatedDeltaRangeInMeters;
    }

    public void setAccumulatedDeltaRangeInMeters(double value) {
        this.mAccumulatedDeltaRangeInMeters = value;
    }

    public double getAccumulatedDeltaRangeUncertaintyInMeters() {
        return this.mAccumulatedDeltaRangeUncertaintyInMeters;
    }

    public void setAccumulatedDeltaRangeUncertaintyInMeters(double value) {
        this.mAccumulatedDeltaRangeUncertaintyInMeters = value;
    }

    public boolean hasPseudorangeInMeters() {
        return isFlagSet(32);
    }

    public double getPseudorangeInMeters() {
        return this.mPseudorangeInMeters;
    }

    public void setPseudorangeInMeters(double value) {
        setFlag(32);
        this.mPseudorangeInMeters = value;
    }

    public void resetPseudorangeInMeters() {
        resetFlag(32);
        this.mPseudorangeInMeters = Double.NaN;
    }

    public boolean hasPseudorangeUncertaintyInMeters() {
        return isFlagSet(64);
    }

    public double getPseudorangeUncertaintyInMeters() {
        return this.mPseudorangeUncertaintyInMeters;
    }

    public void setPseudorangeUncertaintyInMeters(double value) {
        setFlag(64);
        this.mPseudorangeUncertaintyInMeters = value;
    }

    public void resetPseudorangeUncertaintyInMeters() {
        resetFlag(64);
        this.mPseudorangeUncertaintyInMeters = Double.NaN;
    }

    public boolean hasCodePhaseInChips() {
        return isFlagSet(128);
    }

    public double getCodePhaseInChips() {
        return this.mCodePhaseInChips;
    }

    public void setCodePhaseInChips(double value) {
        setFlag(128);
        this.mCodePhaseInChips = value;
    }

    public void resetCodePhaseInChips() {
        resetFlag(128);
        this.mCodePhaseInChips = Double.NaN;
    }

    public boolean hasCodePhaseUncertaintyInChips() {
        return isFlagSet(256);
    }

    public double getCodePhaseUncertaintyInChips() {
        return this.mCodePhaseUncertaintyInChips;
    }

    public void setCodePhaseUncertaintyInChips(double value) {
        setFlag(256);
        this.mCodePhaseUncertaintyInChips = value;
    }

    public void resetCodePhaseUncertaintyInChips() {
        resetFlag(256);
        this.mCodePhaseUncertaintyInChips = Double.NaN;
    }

    public boolean hasCarrierFrequencyInHz() {
        return isFlagSet(512);
    }

    public float getCarrierFrequencyInHz() {
        return this.mCarrierFrequencyInHz;
    }

    public void setCarrierFrequencyInHz(float carrierFrequencyInHz) {
        setFlag(512);
        this.mCarrierFrequencyInHz = carrierFrequencyInHz;
    }

    public void resetCarrierFrequencyInHz() {
        resetFlag(512);
        this.mCarrierFrequencyInHz = Float.NaN;
    }

    public boolean hasCarrierCycles() {
        return isFlagSet(1024);
    }

    public long getCarrierCycles() {
        return this.mCarrierCycles;
    }

    public void setCarrierCycles(long value) {
        setFlag(1024);
        this.mCarrierCycles = value;
    }

    public void resetCarrierCycles() {
        resetFlag(1024);
        this.mCarrierCycles = Long.MIN_VALUE;
    }

    public boolean hasCarrierPhase() {
        return isFlagSet(2048);
    }

    public double getCarrierPhase() {
        return this.mCarrierPhase;
    }

    public void setCarrierPhase(double value) {
        setFlag(2048);
        this.mCarrierPhase = value;
    }

    public void resetCarrierPhase() {
        resetFlag(2048);
        this.mCarrierPhase = Double.NaN;
    }

    public boolean hasCarrierPhaseUncertainty() {
        return isFlagSet(4096);
    }

    public double getCarrierPhaseUncertainty() {
        return this.mCarrierPhaseUncertainty;
    }

    public void setCarrierPhaseUncertainty(double value) {
        setFlag(4096);
        this.mCarrierPhaseUncertainty = value;
    }

    public void resetCarrierPhaseUncertainty() {
        resetFlag(4096);
        this.mCarrierPhaseUncertainty = Double.NaN;
    }

    public byte getLossOfLock() {
        return this.mLossOfLock;
    }

    public void setLossOfLock(byte value) {
        this.mLossOfLock = value;
    }

    private String getLossOfLockString() {
        byte b = this.mLossOfLock;
        if (b == 0) {
            return "Unknown";
        }
        if (b == 1) {
            return "Ok";
        }
        if (b == 2) {
            return "CycleSlip";
        }
        return "<Invalid:" + ((int) this.mLossOfLock) + ">";
    }

    public boolean hasBitNumber() {
        return isFlagSet(8192);
    }

    public int getBitNumber() {
        return this.mBitNumber;
    }

    public void setBitNumber(int bitNumber) {
        setFlag(8192);
        this.mBitNumber = bitNumber;
    }

    public void resetBitNumber() {
        resetFlag(8192);
        this.mBitNumber = Integer.MIN_VALUE;
    }

    public boolean hasTimeFromLastBitInMs() {
        return isFlagSet(16384);
    }

    public short getTimeFromLastBitInMs() {
        return this.mTimeFromLastBitInMs;
    }

    public void setTimeFromLastBitInMs(short value) {
        setFlag(16384);
        this.mTimeFromLastBitInMs = value;
    }

    public void resetTimeFromLastBitInMs() {
        resetFlag(16384);
        this.mTimeFromLastBitInMs = Half.NEGATIVE_ZERO;
    }

    public boolean hasDopplerShiftInHz() {
        return isFlagSet(32768);
    }

    public double getDopplerShiftInHz() {
        return this.mDopplerShiftInHz;
    }

    public void setDopplerShiftInHz(double value) {
        setFlag(32768);
        this.mDopplerShiftInHz = value;
    }

    public void resetDopplerShiftInHz() {
        resetFlag(32768);
        this.mDopplerShiftInHz = Double.NaN;
    }

    public boolean hasDopplerShiftUncertaintyInHz() {
        return isFlagSet(65536);
    }

    public double getDopplerShiftUncertaintyInHz() {
        return this.mDopplerShiftUncertaintyInHz;
    }

    public void setDopplerShiftUncertaintyInHz(double value) {
        setFlag(65536);
        this.mDopplerShiftUncertaintyInHz = value;
    }

    public void resetDopplerShiftUncertaintyInHz() {
        resetFlag(65536);
        this.mDopplerShiftUncertaintyInHz = Double.NaN;
    }

    public byte getMultipathIndicator() {
        return this.mMultipathIndicator;
    }

    public void setMultipathIndicator(byte value) {
        this.mMultipathIndicator = value;
    }

    private String getMultipathIndicatorString() {
        byte b = this.mMultipathIndicator;
        if (b == 0) {
            return "Unknown";
        }
        if (b == 1) {
            return "Detected";
        }
        if (b == 2) {
            return "NotUsed";
        }
        return "<Invalid:" + ((int) this.mMultipathIndicator) + ">";
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

    public boolean hasElevationInDeg() {
        return isFlagSet(2);
    }

    public double getElevationInDeg() {
        return this.mElevationInDeg;
    }

    public void setElevationInDeg(double elevationInDeg) {
        setFlag(2);
        this.mElevationInDeg = elevationInDeg;
    }

    public void resetElevationInDeg() {
        resetFlag(2);
        this.mElevationInDeg = Double.NaN;
    }

    public boolean hasElevationUncertaintyInDeg() {
        return isFlagSet(4);
    }

    public double getElevationUncertaintyInDeg() {
        return this.mElevationUncertaintyInDeg;
    }

    public void setElevationUncertaintyInDeg(double value) {
        setFlag(4);
        this.mElevationUncertaintyInDeg = value;
    }

    public void resetElevationUncertaintyInDeg() {
        resetFlag(4);
        this.mElevationUncertaintyInDeg = Double.NaN;
    }

    public boolean hasAzimuthInDeg() {
        return isFlagSet(8);
    }

    public double getAzimuthInDeg() {
        return this.mAzimuthInDeg;
    }

    public void setAzimuthInDeg(double value) {
        setFlag(8);
        this.mAzimuthInDeg = value;
    }

    public void resetAzimuthInDeg() {
        resetFlag(8);
        this.mAzimuthInDeg = Double.NaN;
    }

    public boolean hasAzimuthUncertaintyInDeg() {
        return isFlagSet(16);
    }

    public double getAzimuthUncertaintyInDeg() {
        return this.mAzimuthUncertaintyInDeg;
    }

    public void setAzimuthUncertaintyInDeg(double value) {
        setFlag(16);
        this.mAzimuthUncertaintyInDeg = value;
    }

    public void resetAzimuthUncertaintyInDeg() {
        resetFlag(16);
        this.mAzimuthUncertaintyInDeg = Double.NaN;
    }

    public boolean isUsedInFix() {
        return this.mUsedInFix;
    }

    public void setUsedInFix(boolean value) {
        this.mUsedInFix = value;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(this.mFlags);
        parcel.writeByte(this.mPrn);
        parcel.writeDouble(this.mTimeOffsetInNs);
        parcel.writeInt(this.mState);
        parcel.writeLong(this.mReceivedGpsTowInNs);
        parcel.writeLong(this.mReceivedGpsTowUncertaintyInNs);
        parcel.writeDouble(this.mCn0InDbHz);
        parcel.writeDouble(this.mPseudorangeRateInMetersPerSec);
        parcel.writeDouble(this.mPseudorangeRateUncertaintyInMetersPerSec);
        parcel.writeInt(this.mAccumulatedDeltaRangeState);
        parcel.writeDouble(this.mAccumulatedDeltaRangeInMeters);
        parcel.writeDouble(this.mAccumulatedDeltaRangeUncertaintyInMeters);
        parcel.writeDouble(this.mPseudorangeInMeters);
        parcel.writeDouble(this.mPseudorangeUncertaintyInMeters);
        parcel.writeDouble(this.mCodePhaseInChips);
        parcel.writeDouble(this.mCodePhaseUncertaintyInChips);
        parcel.writeFloat(this.mCarrierFrequencyInHz);
        parcel.writeLong(this.mCarrierCycles);
        parcel.writeDouble(this.mCarrierPhase);
        parcel.writeDouble(this.mCarrierPhaseUncertainty);
        parcel.writeByte(this.mLossOfLock);
        parcel.writeInt(this.mBitNumber);
        parcel.writeInt(this.mTimeFromLastBitInMs);
        parcel.writeDouble(this.mDopplerShiftInHz);
        parcel.writeDouble(this.mDopplerShiftUncertaintyInHz);
        parcel.writeByte(this.mMultipathIndicator);
        parcel.writeDouble(this.mSnrInDb);
        parcel.writeDouble(this.mElevationInDeg);
        parcel.writeDouble(this.mElevationUncertaintyInDeg);
        parcel.writeDouble(this.mAzimuthInDeg);
        parcel.writeDouble(this.mAzimuthUncertaintyInDeg);
        parcel.writeInt(this.mUsedInFix ? 1 : 0);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder("GpsMeasurement:\n");
        builder.append(String.format("   %-29s = %s\n", "Prn", Byte.valueOf(this.mPrn)));
        builder.append(String.format("   %-29s = %s\n", "TimeOffsetInNs", Double.valueOf(this.mTimeOffsetInNs)));
        builder.append(String.format("   %-29s = %s\n", "State", getStateString()));
        builder.append(String.format("   %-29s = %-25s   %-40s = %s\n", "ReceivedGpsTowInNs", Long.valueOf(this.mReceivedGpsTowInNs), "ReceivedGpsTowUncertaintyInNs", Long.valueOf(this.mReceivedGpsTowUncertaintyInNs)));
        builder.append(String.format("   %-29s = %s\n", "Cn0InDbHz", Double.valueOf(this.mCn0InDbHz)));
        builder.append(String.format("   %-29s = %-25s   %-40s = %s\n", "PseudorangeRateInMetersPerSec", Double.valueOf(this.mPseudorangeRateInMetersPerSec), "PseudorangeRateUncertaintyInMetersPerSec", Double.valueOf(this.mPseudorangeRateUncertaintyInMetersPerSec)));
        builder.append(String.format("   %-29s = %s\n", "PseudorangeRateIsCorrected", Boolean.valueOf(isPseudorangeRateCorrected())));
        builder.append(String.format("   %-29s = %s\n", "AccumulatedDeltaRangeState", getAccumulatedDeltaRangeStateString()));
        builder.append(String.format("   %-29s = %-25s   %-40s = %s\n", "AccumulatedDeltaRangeInMeters", Double.valueOf(this.mAccumulatedDeltaRangeInMeters), "AccumulatedDeltaRangeUncertaintyInMeters", Double.valueOf(this.mAccumulatedDeltaRangeUncertaintyInMeters)));
        Object[] objArr = new Object[4];
        objArr[0] = "PseudorangeInMeters";
        Double d = null;
        objArr[1] = hasPseudorangeInMeters() ? Double.valueOf(this.mPseudorangeInMeters) : null;
        objArr[2] = "PseudorangeUncertaintyInMeters";
        objArr[3] = hasPseudorangeUncertaintyInMeters() ? Double.valueOf(this.mPseudorangeUncertaintyInMeters) : null;
        builder.append(String.format("   %-29s = %-25s   %-40s = %s\n", objArr));
        Object[] objArr2 = new Object[4];
        objArr2[0] = "CodePhaseInChips";
        objArr2[1] = hasCodePhaseInChips() ? Double.valueOf(this.mCodePhaseInChips) : null;
        objArr2[2] = "CodePhaseUncertaintyInChips";
        objArr2[3] = hasCodePhaseUncertaintyInChips() ? Double.valueOf(this.mCodePhaseUncertaintyInChips) : null;
        builder.append(String.format("   %-29s = %-25s   %-40s = %s\n", objArr2));
        Object[] objArr3 = new Object[2];
        objArr3[0] = "CarrierFrequencyInHz";
        objArr3[1] = hasCarrierFrequencyInHz() ? Float.valueOf(this.mCarrierFrequencyInHz) : null;
        builder.append(String.format("   %-29s = %s\n", objArr3));
        Object[] objArr4 = new Object[2];
        objArr4[0] = "CarrierCycles";
        objArr4[1] = hasCarrierCycles() ? Long.valueOf(this.mCarrierCycles) : null;
        builder.append(String.format("   %-29s = %s\n", objArr4));
        Object[] objArr5 = new Object[4];
        objArr5[0] = "CarrierPhase";
        objArr5[1] = hasCarrierPhase() ? Double.valueOf(this.mCarrierPhase) : null;
        objArr5[2] = "CarrierPhaseUncertainty";
        objArr5[3] = hasCarrierPhaseUncertainty() ? Double.valueOf(this.mCarrierPhaseUncertainty) : null;
        builder.append(String.format("   %-29s = %-25s   %-40s = %s\n", objArr5));
        builder.append(String.format("   %-29s = %s\n", "LossOfLock", getLossOfLockString()));
        Object[] objArr6 = new Object[2];
        objArr6[0] = "BitNumber";
        objArr6[1] = hasBitNumber() ? Integer.valueOf(this.mBitNumber) : null;
        builder.append(String.format("   %-29s = %s\n", objArr6));
        Object[] objArr7 = new Object[2];
        objArr7[0] = "TimeFromLastBitInMs";
        objArr7[1] = hasTimeFromLastBitInMs() ? Short.valueOf(this.mTimeFromLastBitInMs) : null;
        builder.append(String.format("   %-29s = %s\n", objArr7));
        Object[] objArr8 = new Object[4];
        objArr8[0] = "DopplerShiftInHz";
        objArr8[1] = hasDopplerShiftInHz() ? Double.valueOf(this.mDopplerShiftInHz) : null;
        objArr8[2] = "DopplerShiftUncertaintyInHz";
        objArr8[3] = hasDopplerShiftUncertaintyInHz() ? Double.valueOf(this.mDopplerShiftUncertaintyInHz) : null;
        builder.append(String.format("   %-29s = %-25s   %-40s = %s\n", objArr8));
        builder.append(String.format("   %-29s = %s\n", "MultipathIndicator", getMultipathIndicatorString()));
        Object[] objArr9 = new Object[2];
        objArr9[0] = "SnrInDb";
        objArr9[1] = hasSnrInDb() ? Double.valueOf(this.mSnrInDb) : null;
        builder.append(String.format("   %-29s = %s\n", objArr9));
        Object[] objArr10 = new Object[4];
        objArr10[0] = "ElevationInDeg";
        objArr10[1] = hasElevationInDeg() ? Double.valueOf(this.mElevationInDeg) : null;
        objArr10[2] = "ElevationUncertaintyInDeg";
        objArr10[3] = hasElevationUncertaintyInDeg() ? Double.valueOf(this.mElevationUncertaintyInDeg) : null;
        builder.append(String.format("   %-29s = %-25s   %-40s = %s\n", objArr10));
        Object[] objArr11 = new Object[4];
        objArr11[0] = "AzimuthInDeg";
        objArr11[1] = hasAzimuthInDeg() ? Double.valueOf(this.mAzimuthInDeg) : null;
        objArr11[2] = "AzimuthUncertaintyInDeg";
        if (hasAzimuthUncertaintyInDeg()) {
            d = Double.valueOf(this.mAzimuthUncertaintyInDeg);
        }
        objArr11[3] = d;
        builder.append(String.format("   %-29s = %-25s   %-40s = %s\n", objArr11));
        builder.append(String.format("   %-29s = %s\n", "UsedInFix", Boolean.valueOf(this.mUsedInFix)));
        return builder.toString();
    }

    private void initialize() {
        this.mFlags = 0;
        setPrn(Byte.MIN_VALUE);
        setTimeOffsetInNs(-9.223372036854776E18d);
        setState(0);
        setReceivedGpsTowInNs(Long.MIN_VALUE);
        setReceivedGpsTowUncertaintyInNs(Long.MAX_VALUE);
        setCn0InDbHz(Double.MIN_VALUE);
        setPseudorangeRateInMetersPerSec(Double.MIN_VALUE);
        setPseudorangeRateUncertaintyInMetersPerSec(Double.MIN_VALUE);
        setAccumulatedDeltaRangeState(0);
        setAccumulatedDeltaRangeInMeters(Double.MIN_VALUE);
        setAccumulatedDeltaRangeUncertaintyInMeters(Double.MIN_VALUE);
        resetPseudorangeInMeters();
        resetPseudorangeUncertaintyInMeters();
        resetCodePhaseInChips();
        resetCodePhaseUncertaintyInChips();
        resetCarrierFrequencyInHz();
        resetCarrierCycles();
        resetCarrierPhase();
        resetCarrierPhaseUncertainty();
        setLossOfLock((byte) 0);
        resetBitNumber();
        resetTimeFromLastBitInMs();
        resetDopplerShiftInHz();
        resetDopplerShiftUncertaintyInHz();
        setMultipathIndicator((byte) 0);
        resetSnrInDb();
        resetElevationInDeg();
        resetElevationUncertaintyInDeg();
        resetAzimuthInDeg();
        resetAzimuthUncertaintyInDeg();
        setUsedInFix(false);
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
