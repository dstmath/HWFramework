package android.os.connectivity;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Arrays;

public final class WifiBatteryStats implements Parcelable {
    public static final Parcelable.Creator<WifiBatteryStats> CREATOR = new Parcelable.Creator<WifiBatteryStats>() {
        public WifiBatteryStats createFromParcel(Parcel in) {
            return new WifiBatteryStats(in);
        }

        public WifiBatteryStats[] newArray(int size) {
            return new WifiBatteryStats[size];
        }
    };
    private long mEnergyConsumedMaMs;
    private long mIdleTimeMs;
    private long mKernelActiveTimeMs;
    private long mLoggingDurationMs;
    private long mNumAppScanRequest;
    private long mNumBytesRx;
    private long mNumBytesTx;
    private long mNumPacketsRx;
    private long mNumPacketsTx;
    private long mRxTimeMs;
    private long mScanTimeMs;
    private long mSleepTimeMs;
    private long[] mTimeInRxSignalStrengthLevelMs;
    private long[] mTimeInStateMs;
    private long[] mTimeInSupplicantStateMs;
    private long mTxTimeMs;

    public WifiBatteryStats() {
        initialize();
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(this.mLoggingDurationMs);
        out.writeLong(this.mKernelActiveTimeMs);
        out.writeLong(this.mNumPacketsTx);
        out.writeLong(this.mNumBytesTx);
        out.writeLong(this.mNumPacketsRx);
        out.writeLong(this.mNumBytesRx);
        out.writeLong(this.mSleepTimeMs);
        out.writeLong(this.mScanTimeMs);
        out.writeLong(this.mIdleTimeMs);
        out.writeLong(this.mRxTimeMs);
        out.writeLong(this.mTxTimeMs);
        out.writeLong(this.mEnergyConsumedMaMs);
        out.writeLong(this.mNumAppScanRequest);
        out.writeLongArray(this.mTimeInStateMs);
        out.writeLongArray(this.mTimeInRxSignalStrengthLevelMs);
        out.writeLongArray(this.mTimeInSupplicantStateMs);
    }

    public void readFromParcel(Parcel in) {
        this.mLoggingDurationMs = in.readLong();
        this.mKernelActiveTimeMs = in.readLong();
        this.mNumPacketsTx = in.readLong();
        this.mNumBytesTx = in.readLong();
        this.mNumPacketsRx = in.readLong();
        this.mNumBytesRx = in.readLong();
        this.mSleepTimeMs = in.readLong();
        this.mScanTimeMs = in.readLong();
        this.mIdleTimeMs = in.readLong();
        this.mRxTimeMs = in.readLong();
        this.mTxTimeMs = in.readLong();
        this.mEnergyConsumedMaMs = in.readLong();
        this.mNumAppScanRequest = in.readLong();
        in.readLongArray(this.mTimeInStateMs);
        in.readLongArray(this.mTimeInRxSignalStrengthLevelMs);
        in.readLongArray(this.mTimeInSupplicantStateMs);
    }

    public long getLoggingDurationMs() {
        return this.mLoggingDurationMs;
    }

    public long getKernelActiveTimeMs() {
        return this.mKernelActiveTimeMs;
    }

    public long getNumPacketsTx() {
        return this.mNumPacketsTx;
    }

    public long getNumBytesTx() {
        return this.mNumBytesTx;
    }

    public long getNumPacketsRx() {
        return this.mNumPacketsRx;
    }

    public long getNumBytesRx() {
        return this.mNumBytesRx;
    }

    public long getSleepTimeMs() {
        return this.mSleepTimeMs;
    }

    public long getScanTimeMs() {
        return this.mScanTimeMs;
    }

    public long getIdleTimeMs() {
        return this.mIdleTimeMs;
    }

    public long getRxTimeMs() {
        return this.mRxTimeMs;
    }

    public long getTxTimeMs() {
        return this.mTxTimeMs;
    }

    public long getEnergyConsumedMaMs() {
        return this.mEnergyConsumedMaMs;
    }

    public long getNumAppScanRequest() {
        return this.mNumAppScanRequest;
    }

    public long[] getTimeInStateMs() {
        return this.mTimeInStateMs;
    }

    public long[] getTimeInRxSignalStrengthLevelMs() {
        return this.mTimeInRxSignalStrengthLevelMs;
    }

    public long[] getTimeInSupplicantStateMs() {
        return this.mTimeInSupplicantStateMs;
    }

    public void setLoggingDurationMs(long t) {
        this.mLoggingDurationMs = t;
    }

    public void setKernelActiveTimeMs(long t) {
        this.mKernelActiveTimeMs = t;
    }

    public void setNumPacketsTx(long n) {
        this.mNumPacketsTx = n;
    }

    public void setNumBytesTx(long b) {
        this.mNumBytesTx = b;
    }

    public void setNumPacketsRx(long n) {
        this.mNumPacketsRx = n;
    }

    public void setNumBytesRx(long b) {
        this.mNumBytesRx = b;
    }

    public void setSleepTimeMs(long t) {
        this.mSleepTimeMs = t;
    }

    public void setScanTimeMs(long t) {
        this.mScanTimeMs = t;
    }

    public void setIdleTimeMs(long t) {
        this.mIdleTimeMs = t;
    }

    public void setRxTimeMs(long t) {
        this.mRxTimeMs = t;
    }

    public void setTxTimeMs(long t) {
        this.mTxTimeMs = t;
    }

    public void setEnergyConsumedMaMs(long e) {
        this.mEnergyConsumedMaMs = e;
    }

    public void setNumAppScanRequest(long n) {
        this.mNumAppScanRequest = n;
    }

    public void setTimeInStateMs(long[] t) {
        this.mTimeInStateMs = Arrays.copyOfRange(t, 0, Math.min(t.length, 8));
    }

    public void setTimeInRxSignalStrengthLevelMs(long[] t) {
        this.mTimeInRxSignalStrengthLevelMs = Arrays.copyOfRange(t, 0, Math.min(t.length, 5));
    }

    public void setTimeInSupplicantStateMs(long[] t) {
        this.mTimeInSupplicantStateMs = Arrays.copyOfRange(t, 0, Math.min(t.length, 13));
    }

    public int describeContents() {
        return 0;
    }

    private WifiBatteryStats(Parcel in) {
        initialize();
        readFromParcel(in);
    }

    private void initialize() {
        this.mLoggingDurationMs = 0;
        this.mKernelActiveTimeMs = 0;
        this.mNumPacketsTx = 0;
        this.mNumBytesTx = 0;
        this.mNumPacketsRx = 0;
        this.mNumBytesRx = 0;
        this.mSleepTimeMs = 0;
        this.mScanTimeMs = 0;
        this.mIdleTimeMs = 0;
        this.mRxTimeMs = 0;
        this.mTxTimeMs = 0;
        this.mEnergyConsumedMaMs = 0;
        this.mNumAppScanRequest = 0;
        this.mTimeInStateMs = new long[8];
        Arrays.fill(this.mTimeInStateMs, 0);
        this.mTimeInRxSignalStrengthLevelMs = new long[5];
        Arrays.fill(this.mTimeInRxSignalStrengthLevelMs, 0);
        this.mTimeInSupplicantStateMs = new long[13];
        Arrays.fill(this.mTimeInSupplicantStateMs, 0);
    }
}
