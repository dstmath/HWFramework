package android.bluetooth.le;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.Objects;

public final class PeriodicAdvertisingReport implements Parcelable {
    public static final Creator<PeriodicAdvertisingReport> CREATOR = new Creator<PeriodicAdvertisingReport>() {
        public PeriodicAdvertisingReport createFromParcel(Parcel source) {
            return new PeriodicAdvertisingReport(source, null);
        }

        public PeriodicAdvertisingReport[] newArray(int size) {
            return new PeriodicAdvertisingReport[size];
        }
    };
    public static final int DATA_COMPLETE = 0;
    public static final int DATA_INCOMPLETE_TRUNCATED = 2;
    private ScanRecord data;
    private int dataStatus;
    private int rssi;
    private int syncHandle;
    private long timestampNanos;
    private int txPower;

    /* synthetic */ PeriodicAdvertisingReport(Parcel in, PeriodicAdvertisingReport -this1) {
        this(in);
    }

    public PeriodicAdvertisingReport(int syncHandle, int txPower, int rssi, int dataStatus, ScanRecord data) {
        this.syncHandle = syncHandle;
        this.txPower = txPower;
        this.rssi = rssi;
        this.dataStatus = dataStatus;
        this.data = data;
    }

    private PeriodicAdvertisingReport(Parcel in) {
        readFromParcel(in);
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.syncHandle);
        dest.writeInt(this.txPower);
        dest.writeInt(this.rssi);
        dest.writeInt(this.dataStatus);
        if (this.data != null) {
            dest.writeInt(1);
            dest.writeByteArray(this.data.getBytes());
            return;
        }
        dest.writeInt(0);
    }

    private void readFromParcel(Parcel in) {
        this.syncHandle = in.readInt();
        this.txPower = in.readInt();
        this.rssi = in.readInt();
        this.dataStatus = in.readInt();
        if (in.readInt() == 1) {
            this.data = ScanRecord.parseFromBytes(in.createByteArray());
        }
    }

    public int describeContents() {
        return 0;
    }

    public int getSyncHandle() {
        return this.syncHandle;
    }

    public int getTxPower() {
        return this.txPower;
    }

    public int getRssi() {
        return this.rssi;
    }

    public int getDataStatus() {
        return this.dataStatus;
    }

    public ScanRecord getData() {
        return this.data;
    }

    public long getTimestampNanos() {
        return this.timestampNanos;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(this.syncHandle), Integer.valueOf(this.txPower), Integer.valueOf(this.rssi), Integer.valueOf(this.dataStatus), this.data, Long.valueOf(this.timestampNanos)});
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
        if (this.syncHandle != other.syncHandle || this.txPower != other.txPower || this.rssi != other.rssi || this.dataStatus != other.dataStatus || !Objects.equals(this.data, other.data)) {
            z = false;
        } else if (this.timestampNanos != other.timestampNanos) {
            z = false;
        }
        return z;
    }

    public String toString() {
        return "PeriodicAdvertisingReport{syncHandle=" + this.syncHandle + ", txPower=" + this.txPower + ", rssi=" + this.rssi + ", dataStatus=" + this.dataStatus + ", data=" + Objects.toString(this.data) + ", timestampNanos=" + this.timestampNanos + '}';
    }
}
