package com.huawei.nearbysdk.closeRange;

import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseArray;

public final class CloseRangeResult implements Parcelable {
    public static final Parcelable.Creator<CloseRangeResult> CREATOR = new Parcelable.Creator<CloseRangeResult>() {
        /* class com.huawei.nearbysdk.closeRange.CloseRangeResult.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public CloseRangeResult createFromParcel(Parcel source) {
            CloseRangeBusinessType businessType = (CloseRangeBusinessType) source.readParcelable(CloseRangeBusinessType.class.getClassLoader());
            String deviceId = source.readString();
            byte[] businessData = source.createByteArray();
            CloseRangeResult closeRangeResult = new CloseRangeResult(source.readLong(), businessType, (CloseRangeDevice) source.readParcelable(CloseRangeDevice.class.getClassLoader()), source.readInt(), source.readInt(), (ScanResult) source.readParcelable(ScanResult.class.getClassLoader()));
            closeRangeResult.setDeviceId(deviceId);
            closeRangeResult.setBusinessData(businessData);
            return closeRangeResult;
        }

        @Override // android.os.Parcelable.Creator
        public CloseRangeResult[] newArray(int size) {
            return new CloseRangeResult[size];
        }
    };
    private byte[] businessData;
    private CloseRangeBusinessType businessType;
    private int callbackType;
    private CloseRangeDevice device;
    private String deviceId;
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

    public String getDeviceId() {
        return this.deviceId;
    }

    public void setDeviceId(String deviceId2) {
        this.deviceId = deviceId2;
    }

    public byte[] getBusinessData() {
        return this.businessData;
    }

    public void setBusinessData(byte[] businessData2) {
        this.businessData = businessData2;
    }

    public synchronized SparseArray<byte[]> getHwSpecDataArray() {
        SparseArray<byte[]> sparseArray;
        if (this.hwSpecDataArray == null) {
            if (this.result == null) {
                this.hwSpecDataArray = new SparseArray<>();
                sparseArray = this.hwSpecDataArray;
            } else {
                ScanRecord record = this.result.getScanRecord();
                if (record == null) {
                    this.hwSpecDataArray = new SparseArray<>();
                    sparseArray = this.hwSpecDataArray;
                } else {
                    byte[] hwSpecRawData = record.getServiceData(CloseRangeProtocol.PARCEL_UUID_CLOSERANGE);
                    if (hwSpecRawData == null || hwSpecRawData.length == 0) {
                        sparseArray = this.hwSpecDataArray;
                    } else {
                        this.hwSpecDataArray = CloseRangeProtocol.parseCloseRangeServiceData(hwSpecRawData);
                    }
                }
            }
        }
        sparseArray = this.hwSpecDataArray;
        return sparseArray;
    }

    public synchronized SparseArray<byte[]> getManufacturerDataArray() {
        SparseArray<byte[]> sparseArray;
        if (this.manufacturerDataArray == null) {
            if (this.result == null) {
                this.manufacturerDataArray = new SparseArray<>();
                sparseArray = this.manufacturerDataArray;
            } else {
                ScanRecord record = this.result.getScanRecord();
                if (record == null) {
                    this.manufacturerDataArray = new SparseArray<>();
                    sparseArray = this.manufacturerDataArray;
                } else {
                    this.manufacturerDataArray = record.getManufacturerSpecificData();
                }
            }
        }
        sparseArray = this.manufacturerDataArray;
        return sparseArray;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.timeStamp);
        dest.writeParcelable(this.device, flags);
        dest.writeParcelable(this.businessType, flags);
        dest.writeInt(this.callbackType);
        dest.writeInt(this.errorCode);
        dest.writeParcelable(this.result, flags);
        dest.writeString(this.deviceId);
        dest.writeByteArray(this.businessData);
    }

    @Override // java.lang.Object
    public String toString() {
        return "CloseRangeResult{timeStamp=" + this.timeStamp + ", businessType=" + this.businessType + ", callbackType=" + this.callbackType + ", errorCode=" + this.errorCode + '}';
    }
}
