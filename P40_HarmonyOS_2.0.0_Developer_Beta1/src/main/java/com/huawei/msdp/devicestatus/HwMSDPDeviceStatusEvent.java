package com.huawei.msdp.devicestatus;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import java.util.Locale;

public class HwMSDPDeviceStatusEvent implements Parcelable {
    public static final Parcelable.Creator<HwMSDPDeviceStatusEvent> CREATOR = new Parcelable.Creator<HwMSDPDeviceStatusEvent>() {
        /* class com.huawei.msdp.devicestatus.HwMSDPDeviceStatusEvent.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public HwMSDPDeviceStatusEvent createFromParcel(Parcel source) {
            String mDeviceStatus = source.readString();
            int mEventType = source.readInt();
            long mTimestampNs = source.readLong();
            Bundle bundle = source.readBundle(getClass().getClassLoader());
            HwMSDPDeviceStatusEvent mDeviceStatusEvent = new HwMSDPDeviceStatusEvent(mDeviceStatus, mEventType, mTimestampNs);
            mDeviceStatusEvent.setDeviceStatusEventExtras(bundle);
            return mDeviceStatusEvent;
        }

        @Override // android.os.Parcelable.Creator
        public HwMSDPDeviceStatusEvent[] newArray(int size) {
            return new HwMSDPDeviceStatusEvent[size];
        }
    };
    private String mDeviceStatus;
    private Bundle mDeviceStatusEventExtras;
    private int mEventType;
    private long mTimestampNs;

    public HwMSDPDeviceStatusEvent(String deviceStatus, int eventType, long reportLatencyNs) {
        this();
        this.mDeviceStatus = deviceStatus;
        this.mEventType = eventType;
        this.mTimestampNs = reportLatencyNs;
    }

    public HwMSDPDeviceStatusEvent() {
        this.mDeviceStatusEventExtras = null;
    }

    public void setmDeviceStatus(String deviceStatus) {
        this.mDeviceStatus = deviceStatus;
    }

    public void setmEventType(int eventType) {
        this.mEventType = eventType;
    }

    public void setmTimestampNs(long reportLatencyNs) {
        this.mTimestampNs = reportLatencyNs;
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

    public Bundle getDeviceStatusEventExtras() {
        return this.mDeviceStatusEventExtras;
    }

    public void setDeviceStatusEventExtras(Bundle bundle) {
        this.mDeviceStatusEventExtras = bundle;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mDeviceStatus);
        dest.writeInt(this.mEventType);
        dest.writeLong(this.mTimestampNs);
        dest.writeBundle(this.mDeviceStatusEventExtras);
    }

    @Override // java.lang.Object
    public String toString() {
        return String.format(Locale.ROOT, "DeviceStatus='%s',EventType='%s',TimestampNs='%s'", this.mDeviceStatus, Integer.valueOf(this.mEventType), Long.valueOf(this.mTimestampNs));
    }
}
