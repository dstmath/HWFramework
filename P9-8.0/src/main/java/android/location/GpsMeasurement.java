package android.location;

import android.bluetooth.BluetoothInputHost;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class GpsMeasurement implements Parcelable {
    private static final short ADR_ALL = (short) 7;
    public static final short ADR_STATE_CYCLE_SLIP = (short) 4;
    public static final short ADR_STATE_RESET = (short) 2;
    public static final short ADR_STATE_UNKNOWN = (short) 0;
    public static final short ADR_STATE_VALID = (short) 1;
    public static final Creator<GpsMeasurement> CREATOR = new Creator<GpsMeasurement>() {
        public GpsMeasurement createFromParcel(Parcel parcel) {
            boolean z = false;
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
            if (parcel.readInt() != 0) {
                z = true;
            }
            gpsMeasurement.mUsedInFix = z;
            return gpsMeasurement;
        }

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
    public static final byte LOSS_OF_LOCK_CYCLE_SLIP = (byte) 2;
    public static final byte LOSS_OF_LOCK_OK = (byte) 1;
    public static final byte LOSS_OF_LOCK_UNKNOWN = (byte) 0;
    public static final byte MULTIPATH_INDICATOR_DETECTED = (byte) 1;
    public static final byte MULTIPATH_INDICATOR_NOT_USED = (byte) 2;
    public static final byte MULTIPATH_INDICATOR_UNKNOWN = (byte) 0;
    private static final short STATE_ALL = (short) 31;
    public static final short STATE_BIT_SYNC = (short) 2;
    public static final short STATE_CODE_LOCK = (short) 1;
    public static final short STATE_MSEC_AMBIGUOUS = (short) 16;
    public static final short STATE_SUBFRAME_SYNC = (short) 4;
    public static final short STATE_TOW_DECODED = (short) 8;
    public static final short STATE_UNKNOWN = (short) 0;
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
        if (this.mState == (short) 0) {
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
        return isFlagSet(262144) ^ 1;
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
        if (this.mAccumulatedDeltaRangeState == (short) 0) {
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
        switch (this.mLossOfLock) {
            case (byte) 0:
                return "Unknown";
            case (byte) 1:
                return "Ok";
            case (byte) 2:
                return "CycleSlip";
            default:
                return "<Invalid:" + this.mLossOfLock + ">";
        }
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
        this.mTimeFromLastBitInMs = Short.MIN_VALUE;
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
        switch (this.mMultipathIndicator) {
            case (byte) 0:
                return "Unknown";
            case (byte) 1:
                return "Detected";
            case (byte) 2:
                return "NotUsed";
            default:
                return "<Invalid:" + this.mMultipathIndicator + ">";
        }
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

    public int describeContents() {
        return 0;
    }

    public String toString() {
        Double valueOf;
        Float valueOf2;
        Long valueOf3;
        Integer valueOf4;
        Short valueOf5;
        Double d = null;
        String format = "   %-29s = %s\n";
        String formatWithUncertainty = "   %-29s = %-25s   %-40s = %s\n";
        StringBuilder builder = new StringBuilder("GpsMeasurement:\n");
        builder.append(String.format("   %-29s = %s\n", new Object[]{"Prn", Byte.valueOf(this.mPrn)}));
        builder.append(String.format("   %-29s = %s\n", new Object[]{"TimeOffsetInNs", Double.valueOf(this.mTimeOffsetInNs)}));
        builder.append(String.format("   %-29s = %s\n", new Object[]{"State", getStateString()}));
        builder.append(String.format("   %-29s = %-25s   %-40s = %s\n", new Object[]{"ReceivedGpsTowInNs", Long.valueOf(this.mReceivedGpsTowInNs), "ReceivedGpsTowUncertaintyInNs", Long.valueOf(this.mReceivedGpsTowUncertaintyInNs)}));
        builder.append(String.format("   %-29s = %s\n", new Object[]{"Cn0InDbHz", Double.valueOf(this.mCn0InDbHz)}));
        builder.append(String.format("   %-29s = %-25s   %-40s = %s\n", new Object[]{"PseudorangeRateInMetersPerSec", Double.valueOf(this.mPseudorangeRateInMetersPerSec), "PseudorangeRateUncertaintyInMetersPerSec", Double.valueOf(this.mPseudorangeRateUncertaintyInMetersPerSec)}));
        builder.append(String.format("   %-29s = %s\n", new Object[]{"PseudorangeRateIsCorrected", Boolean.valueOf(isPseudorangeRateCorrected())}));
        builder.append(String.format("   %-29s = %s\n", new Object[]{"AccumulatedDeltaRangeState", getAccumulatedDeltaRangeStateString()}));
        builder.append(String.format("   %-29s = %-25s   %-40s = %s\n", new Object[]{"AccumulatedDeltaRangeInMeters", Double.valueOf(this.mAccumulatedDeltaRangeInMeters), "AccumulatedDeltaRangeUncertaintyInMeters", Double.valueOf(this.mAccumulatedDeltaRangeUncertaintyInMeters)}));
        String str = "   %-29s = %-25s   %-40s = %s\n";
        Object[] objArr = new Object[4];
        objArr[0] = "PseudorangeInMeters";
        objArr[1] = hasPseudorangeInMeters() ? Double.valueOf(this.mPseudorangeInMeters) : null;
        objArr[2] = "PseudorangeUncertaintyInMeters";
        if (hasPseudorangeUncertaintyInMeters()) {
            valueOf = Double.valueOf(this.mPseudorangeUncertaintyInMeters);
        } else {
            valueOf = null;
        }
        objArr[3] = valueOf;
        builder.append(String.format(str, objArr));
        str = "   %-29s = %-25s   %-40s = %s\n";
        objArr = new Object[4];
        objArr[0] = "CodePhaseInChips";
        if (hasCodePhaseInChips()) {
            valueOf = Double.valueOf(this.mCodePhaseInChips);
        } else {
            valueOf = null;
        }
        objArr[1] = valueOf;
        objArr[2] = "CodePhaseUncertaintyInChips";
        if (hasCodePhaseUncertaintyInChips()) {
            valueOf = Double.valueOf(this.mCodePhaseUncertaintyInChips);
        } else {
            valueOf = null;
        }
        objArr[3] = valueOf;
        builder.append(String.format(str, objArr));
        str = "   %-29s = %s\n";
        objArr = new Object[2];
        objArr[0] = "CarrierFrequencyInHz";
        if (hasCarrierFrequencyInHz()) {
            valueOf2 = Float.valueOf(this.mCarrierFrequencyInHz);
        } else {
            valueOf2 = null;
        }
        objArr[1] = valueOf2;
        builder.append(String.format(str, objArr));
        str = "   %-29s = %s\n";
        objArr = new Object[2];
        objArr[0] = "CarrierCycles";
        if (hasCarrierCycles()) {
            valueOf3 = Long.valueOf(this.mCarrierCycles);
        } else {
            valueOf3 = null;
        }
        objArr[1] = valueOf3;
        builder.append(String.format(str, objArr));
        str = "   %-29s = %-25s   %-40s = %s\n";
        objArr = new Object[4];
        objArr[0] = "CarrierPhase";
        if (hasCarrierPhase()) {
            valueOf = Double.valueOf(this.mCarrierPhase);
        } else {
            valueOf = null;
        }
        objArr[1] = valueOf;
        objArr[2] = "CarrierPhaseUncertainty";
        if (hasCarrierPhaseUncertainty()) {
            valueOf = Double.valueOf(this.mCarrierPhaseUncertainty);
        } else {
            valueOf = null;
        }
        objArr[3] = valueOf;
        builder.append(String.format(str, objArr));
        builder.append(String.format("   %-29s = %s\n", new Object[]{"LossOfLock", getLossOfLockString()}));
        str = "   %-29s = %s\n";
        objArr = new Object[2];
        objArr[0] = "BitNumber";
        if (hasBitNumber()) {
            valueOf4 = Integer.valueOf(this.mBitNumber);
        } else {
            valueOf4 = null;
        }
        objArr[1] = valueOf4;
        builder.append(String.format(str, objArr));
        str = "   %-29s = %s\n";
        objArr = new Object[2];
        objArr[0] = "TimeFromLastBitInMs";
        if (hasTimeFromLastBitInMs()) {
            valueOf5 = Short.valueOf(this.mTimeFromLastBitInMs);
        } else {
            valueOf5 = null;
        }
        objArr[1] = valueOf5;
        builder.append(String.format(str, objArr));
        str = "   %-29s = %-25s   %-40s = %s\n";
        objArr = new Object[4];
        objArr[0] = "DopplerShiftInHz";
        if (hasDopplerShiftInHz()) {
            valueOf = Double.valueOf(this.mDopplerShiftInHz);
        } else {
            valueOf = null;
        }
        objArr[1] = valueOf;
        objArr[2] = "DopplerShiftUncertaintyInHz";
        if (hasDopplerShiftUncertaintyInHz()) {
            valueOf = Double.valueOf(this.mDopplerShiftUncertaintyInHz);
        } else {
            valueOf = null;
        }
        objArr[3] = valueOf;
        builder.append(String.format(str, objArr));
        builder.append(String.format("   %-29s = %s\n", new Object[]{"MultipathIndicator", getMultipathIndicatorString()}));
        str = "   %-29s = %s\n";
        objArr = new Object[2];
        objArr[0] = "SnrInDb";
        if (hasSnrInDb()) {
            valueOf = Double.valueOf(this.mSnrInDb);
        } else {
            valueOf = null;
        }
        objArr[1] = valueOf;
        builder.append(String.format(str, objArr));
        str = "   %-29s = %-25s   %-40s = %s\n";
        objArr = new Object[4];
        objArr[0] = "ElevationInDeg";
        if (hasElevationInDeg()) {
            valueOf = Double.valueOf(this.mElevationInDeg);
        } else {
            valueOf = null;
        }
        objArr[1] = valueOf;
        objArr[2] = "ElevationUncertaintyInDeg";
        if (hasElevationUncertaintyInDeg()) {
            valueOf = Double.valueOf(this.mElevationUncertaintyInDeg);
        } else {
            valueOf = null;
        }
        objArr[3] = valueOf;
        builder.append(String.format(str, objArr));
        str = "   %-29s = %-25s   %-40s = %s\n";
        objArr = new Object[4];
        objArr[0] = "AzimuthInDeg";
        if (hasAzimuthInDeg()) {
            valueOf = Double.valueOf(this.mAzimuthInDeg);
        } else {
            valueOf = null;
        }
        objArr[1] = valueOf;
        objArr[2] = "AzimuthUncertaintyInDeg";
        if (hasAzimuthUncertaintyInDeg()) {
            d = Double.valueOf(this.mAzimuthUncertaintyInDeg);
        }
        objArr[3] = d;
        builder.append(String.format(str, objArr));
        builder.append(String.format("   %-29s = %s\n", new Object[]{"UsedInFix", Boolean.valueOf(this.mUsedInFix)}));
        return builder.toString();
    }

    private void initialize() {
        this.mFlags = 0;
        setPrn(BluetoothInputHost.SUBCLASS1_MOUSE);
        setTimeOffsetInNs(-9.223372036854776E18d);
        setState((short) 0);
        setReceivedGpsTowInNs(Long.MIN_VALUE);
        setReceivedGpsTowUncertaintyInNs(Long.MAX_VALUE);
        setCn0InDbHz(Double.MIN_VALUE);
        setPseudorangeRateInMetersPerSec(Double.MIN_VALUE);
        setPseudorangeRateUncertaintyInMetersPerSec(Double.MIN_VALUE);
        setAccumulatedDeltaRangeState((short) 0);
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
