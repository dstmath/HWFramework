package com.huawei.nearbysdk.closeRange;

import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseArray;

public final class CloseRangeResult implements Parcelable {
    public static final Parcelable.Creator<CloseRangeResult> CREATOR = new Parcelable.Creator<CloseRangeResult>() {
        public CloseRangeResult createFromParcel(Parcel source) {
            Parcel parcel = source;
            CloseRangeResult closeRangeResult = new CloseRangeResult(source.readLong(), (CloseRangeBusinessType) parcel.readParcelable(CloseRangeBusinessType.class.getClassLoader()), (CloseRangeDevice) parcel.readParcelable(CloseRangeDevice.class.getClassLoader()), source.readInt(), source.readInt(), (ScanResult) parcel.readParcelable(ScanResult.class.getClassLoader()));
            return closeRangeResult;
        }

        public CloseRangeResult[] newArray(int size) {
            return new CloseRangeResult[size];
        }
    };
    private CloseRangeBusinessType businessType;
    private int callbackType;
    private CloseRangeDevice device;
    private int errorCode;
    private SparseArray<byte[]> hwSpecDataArray = null;
    private SparseArray<byte[]> manufacturerDataArray = null;
    private ScanResult result;
    private long timeStamp;

    public CloseRangeResult(long timeStamp2, CloseRangeBusinessType businessType2, CloseRangeDevice device2, int callbackType2, int errorCode2, ScanResult result2) {
        this.timeStamp = timeStamp2;
        this.businessType = businessType2;
        this.device = device2;
        this.callbackType = callbackType2;
        this.errorCode = errorCode2;
        this.result = result2;
    }

    public final CloseRangeBusinessType getBusinessType() {
        return this.businessType;
    }

    public final CloseRangeDevice getDevice() {
        return this.device;
    }

    public long getTimeStamp() {
        return this.timeStamp;
    }

    public int getCallbackType() {
        return this.callbackType;
    }

    public void setCallbackType(int callbackType2) {
        this.callbackType = callbackType2;
    }

    public int getErrorCode() {
        return this.errorCode;
    }

    public ScanResult getResult() {
        return this.result;
    }

    public synchronized SparseArray<byte[]> getHwSpecDataArray() {
        if (this.hwSpecDataArray == null) {
            if (this.result == null) {
                this.hwSpecDataArray = new SparseArray<>();
                return this.hwSpecDataArray;
            }
            ScanRecord record = this.result.getScanRecord();
            if (record == null) {
                this.hwSpecDataArray = new SparseArray<>();
                return this.hwSpecDataArray;
            }
            byte[] hwSpecRawData = record.getServiceData(CloseRangeProtocol.PARCEL_UUID_CLOSERANGE);
            if (hwSpecRawData != null) {
                if (hwSpecRawData.length != 0) {
                    this.hwSpecDataArray = CloseRangeProtocol.parseCloseRangeServiceData(hwSpecRawData);
                }
            }
            return this.hwSpecDataArray;
        }
        return this.hwSpecDataArray;
    }

    public synchronized SparseArray<byte[]> getManufacturerDataArray() {
        if (this.manufacturerDataArray == null) {
            if (this.result == null) {
                this.manufacturerDataArray = new SparseArray<>();
                return this.manufacturerDataArray;
            }
            ScanRecord record = this.result.getScanRecord();
            if (record == null) {
                this.manufacturerDataArray = new SparseArray<>();
                return this.manufacturerDataArray;
            }
            this.manufacturerDataArray = record.getManufacturerSpecificData();
        }
        return this.manufacturerDataArray;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.timeStamp);
        dest.writeParcelable(this.device, flags);
        dest.writeParcelable(this.businessType, flags);
        dest.writeInt(this.callbackType);
        dest.writeInt(this.errorCode);
        dest.writeParcelable(this.result, flags);
    }

    public String toString() {
        return "CloseRangeResult{timeStamp=" + this.timeStamp + ", businessType=" + this.businessType + ", callbackType=" + this.callbackType + ", errorCode=" + this.errorCode + '}';
    }
}
