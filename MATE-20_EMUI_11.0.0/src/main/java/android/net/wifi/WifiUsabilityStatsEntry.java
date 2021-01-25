package android.net.wifi;

import android.annotation.SystemApi;
import android.os.Parcel;
import android.os.Parcelable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@SystemApi
public final class WifiUsabilityStatsEntry implements Parcelable {
    public static final Parcelable.Creator<WifiUsabilityStatsEntry> CREATOR = new Parcelable.Creator<WifiUsabilityStatsEntry>() {
        /* class android.net.wifi.WifiUsabilityStatsEntry.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public WifiUsabilityStatsEntry createFromParcel(Parcel in) {
            return new WifiUsabilityStatsEntry(in.readLong(), in.readInt(), in.readInt(), in.readLong(), in.readLong(), in.readLong(), in.readLong(), in.readLong(), in.readLong(), in.readLong(), in.readLong(), in.readLong(), in.readLong(), in.readLong(), in.readLong(), in.readLong(), in.readLong(), in.readLong(), in.readLong(), in.readInt(), in.readInt(), in.readInt(), in.readInt(), in.readInt(), in.readInt(), in.readInt(), in.readBoolean());
        }

        @Override // android.os.Parcelable.Creator
        public WifiUsabilityStatsEntry[] newArray(int size) {
            return new WifiUsabilityStatsEntry[size];
        }
    };
    public static final int PROBE_STATUS_FAILURE = 3;
    public static final int PROBE_STATUS_NO_PROBE = 1;
    public static final int PROBE_STATUS_SUCCESS = 2;
    public static final int PROBE_STATUS_UNKNOWN = 0;
    private final int mCellularDataNetworkType;
    private final int mCellularSignalStrengthDb;
    private final int mCellularSignalStrengthDbm;
    private final boolean mIsSameRegisteredCell;
    private final int mLinkSpeedMbps;
    private final int mProbeElapsedTimeSinceLastUpdateMillis;
    private final int mProbeMcsRateSinceLastUpdate;
    private final int mProbeStatusSinceLastUpdate;
    private final int mRssi;
    private final int mRxLinkSpeedMbps;
    private final long mTimeStampMillis;
    private final long mTotalBackgroundScanTimeMillis;
    private final long mTotalBeaconRx;
    private final long mTotalCcaBusyFreqTimeMillis;
    private final long mTotalHotspot2ScanTimeMillis;
    private final long mTotalNanScanTimeMillis;
    private final long mTotalPnoScanTimeMillis;
    private final long mTotalRadioOnFreqTimeMillis;
    private final long mTotalRadioOnTimeMillis;
    private final long mTotalRadioRxTimeMillis;
    private final long mTotalRadioTxTimeMillis;
    private final long mTotalRoamScanTimeMillis;
    private final long mTotalRxSuccess;
    private final long mTotalScanTimeMillis;
    private final long mTotalTxBad;
    private final long mTotalTxRetries;
    private final long mTotalTxSuccess;

    @Retention(RetentionPolicy.SOURCE)
    public @interface ProbeStatus {
    }

    public WifiUsabilityStatsEntry(long timeStampMillis, int rssi, int linkSpeedMbps, long totalTxSuccess, long totalTxRetries, long totalTxBad, long totalRxSuccess, long totalRadioOnTimeMillis, long totalRadioTxTimeMillis, long totalRadioRxTimeMillis, long totalScanTimeMillis, long totalNanScanTimeMillis, long totalBackgroundScanTimeMillis, long totalRoamScanTimeMillis, long totalPnoScanTimeMillis, long totalHotspot2ScanTimeMillis, long totalCcaBusyFreqTimeMillis, long totalRadioOnFreqTimeMillis, long totalBeaconRx, int probeStatusSinceLastUpdate, int probeElapsedTimeSinceLastUpdateMillis, int probeMcsRateSinceLastUpdate, int rxLinkSpeedMbps, int cellularDataNetworkType, int cellularSignalStrengthDbm, int cellularSignalStrengthDb, boolean isSameRegisteredCell) {
        this.mTimeStampMillis = timeStampMillis;
        this.mRssi = rssi;
        this.mLinkSpeedMbps = linkSpeedMbps;
        this.mTotalTxSuccess = totalTxSuccess;
        this.mTotalTxRetries = totalTxRetries;
        this.mTotalTxBad = totalTxBad;
        this.mTotalRxSuccess = totalRxSuccess;
        this.mTotalRadioOnTimeMillis = totalRadioOnTimeMillis;
        this.mTotalRadioTxTimeMillis = totalRadioTxTimeMillis;
        this.mTotalRadioRxTimeMillis = totalRadioRxTimeMillis;
        this.mTotalScanTimeMillis = totalScanTimeMillis;
        this.mTotalNanScanTimeMillis = totalNanScanTimeMillis;
        this.mTotalBackgroundScanTimeMillis = totalBackgroundScanTimeMillis;
        this.mTotalRoamScanTimeMillis = totalRoamScanTimeMillis;
        this.mTotalPnoScanTimeMillis = totalPnoScanTimeMillis;
        this.mTotalHotspot2ScanTimeMillis = totalHotspot2ScanTimeMillis;
        this.mTotalCcaBusyFreqTimeMillis = totalCcaBusyFreqTimeMillis;
        this.mTotalRadioOnFreqTimeMillis = totalRadioOnFreqTimeMillis;
        this.mTotalBeaconRx = totalBeaconRx;
        this.mProbeStatusSinceLastUpdate = probeStatusSinceLastUpdate;
        this.mProbeElapsedTimeSinceLastUpdateMillis = probeElapsedTimeSinceLastUpdateMillis;
        this.mProbeMcsRateSinceLastUpdate = probeMcsRateSinceLastUpdate;
        this.mRxLinkSpeedMbps = rxLinkSpeedMbps;
        this.mCellularDataNetworkType = cellularDataNetworkType;
        this.mCellularSignalStrengthDbm = cellularSignalStrengthDbm;
        this.mCellularSignalStrengthDb = cellularSignalStrengthDb;
        this.mIsSameRegisteredCell = isSameRegisteredCell;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.mTimeStampMillis);
        dest.writeInt(this.mRssi);
        dest.writeInt(this.mLinkSpeedMbps);
        dest.writeLong(this.mTotalTxSuccess);
        dest.writeLong(this.mTotalTxRetries);
        dest.writeLong(this.mTotalTxBad);
        dest.writeLong(this.mTotalRxSuccess);
        dest.writeLong(this.mTotalRadioOnTimeMillis);
        dest.writeLong(this.mTotalRadioTxTimeMillis);
        dest.writeLong(this.mTotalRadioRxTimeMillis);
        dest.writeLong(this.mTotalScanTimeMillis);
        dest.writeLong(this.mTotalNanScanTimeMillis);
        dest.writeLong(this.mTotalBackgroundScanTimeMillis);
        dest.writeLong(this.mTotalRoamScanTimeMillis);
        dest.writeLong(this.mTotalPnoScanTimeMillis);
        dest.writeLong(this.mTotalHotspot2ScanTimeMillis);
        dest.writeLong(this.mTotalCcaBusyFreqTimeMillis);
        dest.writeLong(this.mTotalRadioOnFreqTimeMillis);
        dest.writeLong(this.mTotalBeaconRx);
        dest.writeInt(this.mProbeStatusSinceLastUpdate);
        dest.writeInt(this.mProbeElapsedTimeSinceLastUpdateMillis);
        dest.writeInt(this.mProbeMcsRateSinceLastUpdate);
        dest.writeInt(this.mRxLinkSpeedMbps);
        dest.writeInt(this.mCellularDataNetworkType);
        dest.writeInt(this.mCellularSignalStrengthDbm);
        dest.writeInt(this.mCellularSignalStrengthDb);
        dest.writeBoolean(this.mIsSameRegisteredCell);
    }

    public long getTimeStampMillis() {
        return this.mTimeStampMillis;
    }

    public int getRssi() {
        return this.mRssi;
    }

    public int getLinkSpeedMbps() {
        return this.mLinkSpeedMbps;
    }

    public long getTotalTxSuccess() {
        return this.mTotalTxSuccess;
    }

    public long getTotalTxRetries() {
        return this.mTotalTxRetries;
    }

    public long getTotalTxBad() {
        return this.mTotalTxBad;
    }

    public long getTotalRxSuccess() {
        return this.mTotalRxSuccess;
    }

    public long getTotalRadioOnTimeMillis() {
        return this.mTotalRadioOnTimeMillis;
    }

    public long getTotalRadioTxTimeMillis() {
        return this.mTotalRadioTxTimeMillis;
    }

    public long getTotalRadioRxTimeMillis() {
        return this.mTotalRadioRxTimeMillis;
    }

    public long getTotalScanTimeMillis() {
        return this.mTotalScanTimeMillis;
    }

    public long getTotalNanScanTimeMillis() {
        return this.mTotalNanScanTimeMillis;
    }

    public long getTotalBackgroundScanTimeMillis() {
        return this.mTotalBackgroundScanTimeMillis;
    }

    public long getTotalRoamScanTimeMillis() {
        return this.mTotalRoamScanTimeMillis;
    }

    public long getTotalPnoScanTimeMillis() {
        return this.mTotalPnoScanTimeMillis;
    }

    public long getTotalHotspot2ScanTimeMillis() {
        return this.mTotalHotspot2ScanTimeMillis;
    }

    public long getTotalCcaBusyFreqTimeMillis() {
        return this.mTotalCcaBusyFreqTimeMillis;
    }

    public long getTotalRadioOnFreqTimeMillis() {
        return this.mTotalRadioOnFreqTimeMillis;
    }

    public long getTotalBeaconRx() {
        return this.mTotalBeaconRx;
    }

    public int getProbeStatusSinceLastUpdate() {
        return this.mProbeStatusSinceLastUpdate;
    }

    public int getProbeElapsedTimeSinceLastUpdateMillis() {
        return this.mProbeElapsedTimeSinceLastUpdateMillis;
    }

    public int getProbeMcsRateSinceLastUpdate() {
        return this.mProbeMcsRateSinceLastUpdate;
    }

    public int getRxLinkSpeedMbps() {
        return this.mRxLinkSpeedMbps;
    }

    public int getCellularDataNetworkType() {
        return this.mCellularDataNetworkType;
    }

    public int getCellularSignalStrengthDbm() {
        return this.mCellularSignalStrengthDbm;
    }

    public int getCellularSignalStrengthDb() {
        return this.mCellularSignalStrengthDb;
    }

    public boolean isSameRegisteredCell() {
        return this.mIsSameRegisteredCell;
    }
}
