package android.bluetooth.le;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Objects;

public final class PeriodicAdvertisingReport implements Parcelable {
    public static final Parcelable.Creator<PeriodicAdvertisingReport> CREATOR = new Parcelable.Creator<PeriodicAdvertisingReport>() {
        public PeriodicAdvertisingReport createFromParcel(Parcel source) {
            return new PeriodicAdvertisingReport(source);
        }

        public PeriodicAdvertisingReport[] newArray(int size) {
            return new PeriodicAdvertisingReport[size];
        }
    };
    public static final int DATA_COMPLETE = 0;
    public static final int DATA_INCOMPLETE_TRUNCATED = 2;
    private ScanRecord mData;
    private int mDataStatus;
    private int mRssi;
    private int mSyncHandle;
    private long mTimestampNanos;
    private int mTxPower;

    public PeriodicAdvertisingReport(int syncHandle, int txPower, int rssi, int dataStatus, ScanRecord data) {
        this.mSyncHandle = syncHandle;
        this.mTxPower = txPower;
        this.mRssi = rssi;
        this.mDataStatus = dataStatus;
        this.mData = data;
    }

    private PeriodicAdvertisingReport(Parcel in) {
        readFromParcel(in);
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mSyncHandle);
        dest.writeInt(this.mTxPower);
        dest.writeInt(this.mRssi);
        dest.writeInt(this.mDataStatus);
        if (this.mData != null) {
            dest.writeInt(1);
            dest.writeByteArray(this.mData.getBytes());
            return;
        }
        dest.writeInt(0);
    }

    private void readFromParcel(Parcel in) {
        this.mSyncHandle = in.readInt();
        this.mTxPower = in.readInt();
        this.mRssi = in.readInt();
        this.mDataStatus = in.readInt();
        if (in.readInt() == 1) {
            this.mData = ScanRecord.parseFromBytes(in.createByteArray());
        }
    }

    public int describeContents() {
        return 0;
    }

    public int getSyncHandle() {
        return this.mSyncHandle;
    }

    public int getTxPower() {
        return this.mTxPower;
    }

    public int getRssi() {
        return this.mRssi;
    }

    public int getDataStatus() {
        return this.mDataStatus;
    }

    public ScanRecord getData() {
        return this.mData;
    }

    public long getTimestampNanos() {
        return this.mTimestampNanos;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(this.mSyncHandle), Integer.valueOf(this.mTxPower), Integer.valueOf(this.mRssi), Integer.valueOf(this.mDataStatus), this.mData, Long.valueOf(this.mTimestampNanos)});
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        PeriodicAdvertisingReport other = (PeriodicAdvertisingReport) obj;
        if (!(this.mSyncHandle == other.mSyncHandle && this.mTxPower == other.mTxPower && this.mRssi == other.mRssi && this.mDataStatus == other.mDataStatus && Objects.equals(this.mData, other.mData) && this.mTimestampNanos == other.mTimestampNanos)) {
            z = false;
        }
        return z;
    }

    public String toString() {
        return "PeriodicAdvertisingReport{syncHandle=" + this.mSyncHandle + ", txPower=" + this.mTxPower + ", rssi=" + this.mRssi + ", dataStatus=" + this.mDataStatus + ", data=" + Objects.toString(this.mData) + ", timestampNanos=" + this.mTimestampNanos + '}';
    }
}
