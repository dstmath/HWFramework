package ohos.msdp.devicestatus;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import java.util.Locale;
import ohos.sysappcomponents.contact.Attribute;

public class HwMSDPDeviceStatusEvent implements Parcelable {
    public static final Parcelable.Creator<HwMSDPDeviceStatusEvent> CREATOR = new Parcelable.Creator<HwMSDPDeviceStatusEvent>() {
        /* class ohos.msdp.devicestatus.HwMSDPDeviceStatusEvent.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public HwMSDPDeviceStatusEvent createFromParcel(Parcel parcel) {
            String readString = parcel.readString();
            int readInt = parcel.readInt();
            long readLong = parcel.readLong();
            Bundle readBundle = parcel.readBundle(getClass().getClassLoader());
            HwMSDPDeviceStatusEvent hwMSDPDeviceStatusEvent = new HwMSDPDeviceStatusEvent(readString, readInt, readLong);
            hwMSDPDeviceStatusEvent.setMwMSDPDeviceStatusEventExtras(readBundle);
            return hwMSDPDeviceStatusEvent;
        }

        @Override // android.os.Parcelable.Creator
        public HwMSDPDeviceStatusEvent[] newArray(int i) {
            return new HwMSDPDeviceStatusEvent[i];
        }
    };
    private String mDeviceStatus;
    private int mEventType;
    private long mTimestampNs;
    private Bundle mwMSDPDeviceStatusEventExtras = null;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public HwMSDPDeviceStatusEvent(String str, int i, long j) {
        this.mDeviceStatus = str;
        this.mEventType = i;
        this.mTimestampNs = j;
    }

    public HwMSDPDeviceStatusEvent() {
    }

    public String getDeviceStatus() {
        return this.mDeviceStatus;
    }

    public void setDeviceStatus(String str) {
        this.mDeviceStatus = str;
    }

    public int getEventType() {
        return this.mEventType;
    }

    public void setEventType(int i) {
        this.mEventType = i;
    }

    public long getTimestampNs() {
        return this.mTimestampNs;
    }

    public void setTimestampNs(long j) {
        this.mTimestampNs = j;
    }

    public Bundle getMwMSDPDeviceStatusEventExtras() {
        return this.mwMSDPDeviceStatusEventExtras;
    }

    public void setMwMSDPDeviceStatusEventExtras(Bundle bundle) {
        this.mwMSDPDeviceStatusEventExtras = bundle;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.mDeviceStatus);
        parcel.writeInt(this.mEventType);
        parcel.writeLong(this.mTimestampNs);
        parcel.writeBundle(this.mwMSDPDeviceStatusEventExtras);
    }

    public HwMSDPDeviceStatusMotion getMotionExtras() {
        Parcelable parcelable;
        Bundle bundle = this.mwMSDPDeviceStatusEventExtras;
        if (bundle == null || (parcelable = this.mwMSDPDeviceStatusEventExtras.getParcelable(bundle.getString(Attribute.PhoneFinder.TYPE))) == null || !(parcelable instanceof HwMSDPDeviceStatusMotion)) {
            return null;
        }
        return (HwMSDPDeviceStatusMotion) parcelable;
    }

    @Override // java.lang.Object
    public String toString() {
        return String.format(Locale.ENGLISH, "DeviceStatus='%s',EventType='%s',TimestampNs=%s,MSDPDeviceStatusEventExtras=%s", this.mDeviceStatus, Integer.valueOf(this.mEventType), Long.valueOf(this.mTimestampNs), getMotionExtras());
    }
}
