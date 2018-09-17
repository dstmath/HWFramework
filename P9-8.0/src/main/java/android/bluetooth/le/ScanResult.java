package android.bluetooth.le;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.Objects;

public final class ScanResult implements Parcelable {
    public static final Creator<ScanResult> CREATOR = new Creator<ScanResult>() {
        public ScanResult createFromParcel(Parcel source) {
            return new ScanResult(source, null);
        }

        public ScanResult[] newArray(int size) {
            return new ScanResult[size];
        }
    };
    public static final int DATA_COMPLETE = 0;
    public static final int DATA_TRUNCATED = 2;
    private static final int ET_CONNECTABLE_MASK = 1;
    private static final int ET_LEGACY_MASK = 16;
    public static final int PERIODIC_INTERVAL_NOT_PRESENT = 0;
    public static final int PHY_UNUSED = 0;
    public static final int SID_NOT_PRESENT = 255;
    public static final int TX_POWER_NOT_PRESENT = 127;
    private int mAdvertisingSid;
    private BluetoothDevice mDevice;
    private int mEventType;
    private int mPeriodicAdvertisingInterval;
    private int mPrimaryPhy;
    private int mRssi;
    private ScanRecord mScanRecord;
    private int mSecondaryPhy;
    private long mTimestampNanos;
    private int mTxPower;

    /* synthetic */ ScanResult(Parcel in, ScanResult -this1) {
        this(in);
    }

    public ScanResult(BluetoothDevice device, ScanRecord scanRecord, int rssi, long timestampNanos) {
        this.mDevice = device;
        this.mScanRecord = scanRecord;
        this.mRssi = rssi;
        this.mTimestampNanos = timestampNanos;
        this.mEventType = 17;
        this.mPrimaryPhy = 1;
        this.mSecondaryPhy = 0;
        this.mAdvertisingSid = 255;
        this.mTxPower = 127;
        this.mPeriodicAdvertisingInterval = 0;
    }

    public ScanResult(BluetoothDevice device, int eventType, int primaryPhy, int secondaryPhy, int advertisingSid, int txPower, int rssi, int periodicAdvertisingInterval, ScanRecord scanRecord, long timestampNanos) {
        this.mDevice = device;
        this.mEventType = eventType;
        this.mPrimaryPhy = primaryPhy;
        this.mSecondaryPhy = secondaryPhy;
        this.mAdvertisingSid = advertisingSid;
        this.mTxPower = txPower;
        this.mRssi = rssi;
        this.mPeriodicAdvertisingInterval = periodicAdvertisingInterval;
        this.mScanRecord = scanRecord;
        this.mTimestampNanos = timestampNanos;
    }

    private ScanResult(Parcel in) {
        readFromParcel(in);
    }

    public void writeToParcel(Parcel dest, int flags) {
        if (this.mDevice != null) {
            dest.writeInt(1);
            this.mDevice.writeToParcel(dest, flags);
        } else {
            dest.writeInt(0);
        }
        if (this.mScanRecord != null) {
            dest.writeInt(1);
            dest.writeByteArray(this.mScanRecord.getBytes());
        } else {
            dest.writeInt(0);
        }
        dest.writeInt(this.mRssi);
        dest.writeLong(this.mTimestampNanos);
        dest.writeInt(this.mEventType);
        dest.writeInt(this.mPrimaryPhy);
        dest.writeInt(this.mSecondaryPhy);
        dest.writeInt(this.mAdvertisingSid);
        dest.writeInt(this.mTxPower);
        dest.writeInt(this.mPeriodicAdvertisingInterval);
    }

    private void readFromParcel(Parcel in) {
        if (in.readInt() == 1) {
            this.mDevice = (BluetoothDevice) BluetoothDevice.CREATOR.createFromParcel(in);
        }
        if (in.readInt() == 1) {
            this.mScanRecord = ScanRecord.parseFromBytes(in.createByteArray());
        }
        this.mRssi = in.readInt();
        this.mTimestampNanos = in.readLong();
        this.mEventType = in.readInt();
        this.mPrimaryPhy = in.readInt();
        this.mSecondaryPhy = in.readInt();
        this.mAdvertisingSid = in.readInt();
        this.mTxPower = in.readInt();
        this.mPeriodicAdvertisingInterval = in.readInt();
    }

    public int describeContents() {
        return 0;
    }

    public BluetoothDevice getDevice() {
        return this.mDevice;
    }

    public ScanRecord getScanRecord() {
        return this.mScanRecord;
    }

    public int getRssi() {
        return this.mRssi;
    }

    public long getTimestampNanos() {
        return this.mTimestampNanos;
    }

    public boolean isLegacy() {
        return (this.mEventType & 16) != 0;
    }

    public boolean isConnectable() {
        return (this.mEventType & 1) != 0;
    }

    public int getDataStatus() {
        return (this.mEventType >> 5) & 3;
    }

    public int getPrimaryPhy() {
        return this.mPrimaryPhy;
    }

    public int getSecondaryPhy() {
        return this.mSecondaryPhy;
    }

    public int getAdvertisingSid() {
        return this.mAdvertisingSid;
    }

    public int getTxPower() {
        return this.mTxPower;
    }

    public int getPeriodicAdvertisingInterval() {
        return this.mPeriodicAdvertisingInterval;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.mDevice, Integer.valueOf(this.mRssi), this.mScanRecord, Long.valueOf(this.mTimestampNanos), Integer.valueOf(this.mEventType), Integer.valueOf(this.mPrimaryPhy), Integer.valueOf(this.mSecondaryPhy), Integer.valueOf(this.mAdvertisingSid), Integer.valueOf(this.mTxPower), Integer.valueOf(this.mPeriodicAdvertisingInterval)});
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ScanResult other = (ScanResult) obj;
        if (!Objects.equals(this.mDevice, other.mDevice) || this.mRssi != other.mRssi || !Objects.equals(this.mScanRecord, other.mScanRecord) || this.mTimestampNanos != other.mTimestampNanos || this.mEventType != other.mEventType || this.mPrimaryPhy != other.mPrimaryPhy || this.mSecondaryPhy != other.mSecondaryPhy || this.mAdvertisingSid != other.mAdvertisingSid || this.mTxPower != other.mTxPower) {
            z = false;
        } else if (this.mPeriodicAdvertisingInterval != other.mPeriodicAdvertisingInterval) {
            z = false;
        }
        return z;
    }

    public String toString() {
        return "ScanResult{device=" + this.mDevice + ", scanRecord=" + Objects.toString(this.mScanRecord) + ", rssi=" + this.mRssi + ", timestampNanos=" + this.mTimestampNanos + ", eventType=" + this.mEventType + ", primaryPhy=" + this.mPrimaryPhy + ", secondaryPhy=" + this.mSecondaryPhy + ", advertisingSid=" + this.mAdvertisingSid + ", txPower=" + this.mTxPower + ", periodicAdvertisingInterval=" + this.mPeriodicAdvertisingInterval + '}';
    }
}
