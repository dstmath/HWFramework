package com.huawei.msdp.devicestatus;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class HwMSDPDeviceStatusEvent implements Parcelable {
    public static final Creator<HwMSDPDeviceStatusEvent> CREATOR = new Creator<HwMSDPDeviceStatusEvent>() {
        public HwMSDPDeviceStatusEvent createFromParcel(Parcel source) {
            return new HwMSDPDeviceStatusEvent(source.readString(), source.readInt(), source.readLong());
        }

        public HwMSDPDeviceStatusEvent[] newArray(int size) {
            return new HwMSDPDeviceStatusEvent[size];
        }
    };
    private String mDeviceStatus;
    private int mEventType;
    private long mTimestampNs;

    public HwMSDPDeviceStatusEvent(String mDeviceStatus, int mEventType, long mTimestampNs) {
        this.mDeviceStatus = mDeviceStatus;
        this.mEventType = mEventType;
        this.mTimestampNs = mTimestampNs;
    }

    public void setmDeviceStatus(String mDeviceStatus) {
        this.mDeviceStatus = mDeviceStatus;
    }

    public void setmEventType(int mEventType) {
        this.mEventType = mEventType;
    }

    public void setmTimestampNs(long mTimestampNs) {
        this.mTimestampNs = mTimestampNs;
    }

    public String getmDeviceStatus() {
        return this.mDeviceStatus;
    }

    public int getmEventType() {
        return this.mEventType;
    }

    public long getmTimestampNs() {
        return this.mTimestampNs;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mDeviceStatus);
        dest.writeInt(this.mEventType);
        dest.writeLong(this.mTimestampNs);
    }

    public String toString() {
        return String.format("DeviceStatus='%s',EventType='%s',TimestampNs=%s", new Object[]{this.mDeviceStatus, Integer.valueOf(this.mEventType), Long.valueOf(this.mTimestampNs)});
    }
}
