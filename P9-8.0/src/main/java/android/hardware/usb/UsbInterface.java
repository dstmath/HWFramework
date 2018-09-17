package android.hardware.usb;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.android.internal.util.Preconditions;

public class UsbInterface implements Parcelable {
    public static final Creator<UsbInterface> CREATOR = new Creator<UsbInterface>() {
        public UsbInterface createFromParcel(Parcel in) {
            int id = in.readInt();
            int alternateSetting = in.readInt();
            String name = in.readString();
            int Class = in.readInt();
            int subClass = in.readInt();
            int protocol = in.readInt();
            Parcelable[] endpoints = in.readParcelableArray(UsbEndpoint.class.getClassLoader());
            UsbInterface intf = new UsbInterface(id, alternateSetting, name, Class, subClass, protocol);
            intf.setEndpoints(endpoints);
            return intf;
        }

        public UsbInterface[] newArray(int size) {
            return new UsbInterface[size];
        }
    };
    private final int mAlternateSetting;
    private final int mClass;
    private Parcelable[] mEndpoints;
    private final int mId;
    private final String mName;
    private final int mProtocol;
    private final int mSubclass;

    public UsbInterface(int id, int alternateSetting, String name, int Class, int subClass, int protocol) {
        this.mId = id;
        this.mAlternateSetting = alternateSetting;
        this.mName = name;
        this.mClass = Class;
        this.mSubclass = subClass;
        this.mProtocol = protocol;
    }

    public int getId() {
        return this.mId;
    }

    public int getAlternateSetting() {
        return this.mAlternateSetting;
    }

    public String getName() {
        return this.mName;
    }

    public int getInterfaceClass() {
        return this.mClass;
    }

    public int getInterfaceSubclass() {
        return this.mSubclass;
    }

    public int getInterfaceProtocol() {
        return this.mProtocol;
    }

    public int getEndpointCount() {
        return this.mEndpoints.length;
    }

    public UsbEndpoint getEndpoint(int index) {
        return (UsbEndpoint) this.mEndpoints[index];
    }

    public void setEndpoints(Parcelable[] endpoints) {
        this.mEndpoints = (Parcelable[]) Preconditions.checkArrayElementsNotNull(endpoints, "endpoints");
    }

    public String toString() {
        StringBuilder builder = new StringBuilder("UsbInterface[mId=" + this.mId + ",mAlternateSetting=" + this.mAlternateSetting + ",mName=" + this.mName + ",mClass=" + this.mClass + ",mSubclass=" + this.mSubclass + ",mProtocol=" + this.mProtocol + ",mEndpoints=[");
        for (Object obj : this.mEndpoints) {
            builder.append("\n");
            builder.append(obj.toString());
        }
        builder.append("]");
        return builder.toString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(this.mId);
        parcel.writeInt(this.mAlternateSetting);
        parcel.writeString(this.mName);
        parcel.writeInt(this.mClass);
        parcel.writeInt(this.mSubclass);
        parcel.writeInt(this.mProtocol);
        parcel.writeParcelableArray(this.mEndpoints, 0);
    }
}
