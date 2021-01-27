package android.os.connectivity;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Arrays;

public final class CellularBatteryStats implements Parcelable {
    public static final Parcelable.Creator<CellularBatteryStats> CREATOR = new Parcelable.Creator<CellularBatteryStats>() {
        /* class android.os.connectivity.CellularBatteryStats.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public CellularBatteryStats createFromParcel(Parcel in) {
            return new CellularBatteryStats(in);
        }

        @Override // android.os.Parcelable.Creator
        public CellularBatteryStats[] newArray(int size) {
            return new CellularBatteryStats[size];
        }
    };
    private long mEnergyConsumedMaMs;
    private long mIdleTimeMs;
    private long mKernelActiveTimeMs;
    private long mLoggingDurationMs;
    private long mMonitoredRailChargeConsumedMaMs;
    private long mNumBytesRx;
    private long mNumBytesTx;
    private long mNumPacketsRx;
    private long mNumPacketsTx;
    private long mRxTimeMs;
    private long mSleepTimeMs;
    private long[] mTimeInRatMs;
    private long[] mTimeInRxSignalStrengthLevelMs;
    private long[] mTxTimeMs;

    public CellularBatteryStats() {
        initialize();
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(this.mLoggingDurationMs);
        out.writeLong(this.mKernelActiveTimeMs);
        out.writeLong(this.mNumPacketsTx);
        out.writeLong(this.mNumBytesTx);
        out.writeLong(this.mNumPacketsRx);
        out.writeLong(this.mNumBytesRx);
        out.writeLong(this.mSleepTimeMs);
        out.writeLong(this.mIdleTimeMs);
        out.writeLong(this.mRxTimeMs);
        out.writeLong(this.mEnergyConsumedMaMs);
        out.writeLongArray(this.mTimeInRatMs);
        out.writeLongArray(this.mTimeInRxSignalStrengthLevelMs);
        out.writeLongArray(this.mTxTimeMs);
        out.writeLong(this.mMonitoredRailChargeConsumedMaMs);
    }

    public void readFromParcel(Parcel in) {
        this.mLoggingDurationMs = in.readLong();
        this.mKernelActiveTimeMs = in.readLong();
        this.mNumPacketsTx = in.readLong();
        this.mNumBytesTx = in.readLong();
        this.mNumPacketsRx = in.readLong();
        this.mNumBytesRx = in.readLong();
        this.mSleepTimeMs = in.readLong();
        this.mIdleTimeMs = in.readLong();
        this.mRxTimeMs = in.readLong();
        this.mEnergyConsumedMaMs = in.readLong();
        in.readLongArray(this.mTimeInRatMs);
        in.readLongArray(this.mTimeInRxSignalStrengthLevelMs);
        in.readLongArray(this.mTxTimeMs);
        this.mMonitoredRailChargeConsumedMaMs = in.readLong();
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

    public long getIdleTimeMs() {
        return this.mIdleTimeMs;
    }

    public long getRxTimeMs() {
        return this.mRxTimeMs;
    }

    public long getEnergyConsumedMaMs() {
        return this.mEnergyConsumedMaMs;
    }

    public long[] getTimeInRatMs() {
        return this.mTimeInRatMs;
    }

    public long[] getTimeInRxSignalStrengthLevelMs() {
        return this.mTimeInRxSignalStrengthLevelMs;
    }

    public long[] getTxTimeMs() {
        return this.mTxTimeMs;
    }

    public long getMonitoredRailChargeConsumedMaMs() {
        return this.mMonitoredRailChargeConsumedMaMs;
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

    public void setIdleTimeMs(long t) {
        this.mIdleTimeMs = t;
    }

    public void setRxTimeMs(long t) {
        this.mRxTimeMs = t;
    }

    public void setEnergyConsumedMaMs(long e) {
        this.mEnergyConsumedMaMs = e;
    }

    public void setTimeInRatMs(long[] t) {
        this.mTimeInRatMs = Arrays.copyOfRange(t, 0, Math.min(t.length, 22));
    }

    public void setTimeInRxSignalStrengthLevelMs(long[] t) {
        this.mTimeInRxSignalStrengthLevelMs = Arrays.copyOfRange(t, 0, Math.min(t.length, 6));
    }

    public void setTxTimeMs(long[] t) {
        this.mTxTimeMs = Arrays.copyOfRange(t, 0, Math.min(t.length, 5));
    }

    public void setMonitoredRailChargeConsumedMaMs(long monitoredRailEnergyConsumedMaMs) {
        this.mMonitoredRailChargeConsumedMaMs = monitoredRailEnergyConsumedMaMs;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    private CellularBatteryStats(Parcel in) {
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
        this.mIdleTimeMs = 0;
        this.mRxTimeMs = 0;
        this.mEnergyConsumedMaMs = 0;
        this.mTimeInRatMs = new long[22];
        Arrays.fill(this.mTimeInRatMs, 0L);
        this.mTimeInRxSignalStrengthLevelMs = new long[6];
        Arrays.fill(this.mTimeInRxSignalStrengthLevelMs, 0L);
        this.mTxTimeMs = new long[5];
        Arrays.fill(this.mTxTimeMs, 0L);
        this.mMonitoredRailChargeConsumedMaMs = 0;
    }
}
