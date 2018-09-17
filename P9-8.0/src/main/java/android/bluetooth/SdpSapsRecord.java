package android.bluetooth;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class SdpSapsRecord implements Parcelable {
    public static final Creator CREATOR = new Creator() {
        public SdpSapsRecord createFromParcel(Parcel in) {
            return new SdpSapsRecord(in);
        }

        public SdpRecord[] newArray(int size) {
            return new SdpRecord[size];
        }
    };
    private final int mProfileVersion;
    private final int mRfcommChannelNumber;
    private final String mServiceName;

    public SdpSapsRecord(int rfcomm_channel_number, int profile_version, String service_name) {
        this.mRfcommChannelNumber = rfcomm_channel_number;
        this.mProfileVersion = profile_version;
        this.mServiceName = service_name;
    }

    public SdpSapsRecord(Parcel in) {
        this.mRfcommChannelNumber = in.readInt();
        this.mProfileVersion = in.readInt();
        this.mServiceName = in.readString();
    }

    public int describeContents() {
        return 0;
    }

    public int getRfcommCannelNumber() {
        return this.mRfcommChannelNumber;
    }

    public int getProfileVersion() {
        return this.mProfileVersion;
    }

    public String getServiceName() {
        return this.mServiceName;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mRfcommChannelNumber);
        dest.writeInt(this.mProfileVersion);
        dest.writeString(this.mServiceName);
    }

    public String toString() {
        String ret = "Bluetooth MAS SDP Record:\n";
        if (this.mRfcommChannelNumber != -1) {
            ret = ret + "RFCOMM Chan Number: " + this.mRfcommChannelNumber + "\n";
        }
        if (this.mServiceName != null) {
            ret = ret + "Service Name: " + this.mServiceName + "\n";
        }
        if (this.mProfileVersion != -1) {
            return ret + "Profile version: " + this.mProfileVersion + "\n";
        }
        return ret;
    }
}
