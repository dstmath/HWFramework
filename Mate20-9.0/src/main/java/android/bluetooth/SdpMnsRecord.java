package android.bluetooth;

import android.os.Parcel;
import android.os.Parcelable;

public class SdpMnsRecord implements Parcelable {
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public SdpMnsRecord createFromParcel(Parcel in) {
            return new SdpMnsRecord(in);
        }

        public SdpMnsRecord[] newArray(int size) {
            return new SdpMnsRecord[size];
        }
    };
    private final int mL2capPsm;
    private final int mProfileVersion;
    private final int mRfcommChannelNumber;
    private final String mServiceName;
    private final int mSupportedFeatures;

    public SdpMnsRecord(int l2capPsm, int rfcommChannelNumber, int profileVersion, int supportedFeatures, String serviceName) {
        this.mL2capPsm = l2capPsm;
        this.mRfcommChannelNumber = rfcommChannelNumber;
        this.mSupportedFeatures = supportedFeatures;
        this.mServiceName = serviceName;
        this.mProfileVersion = profileVersion;
    }

    public SdpMnsRecord(Parcel in) {
        this.mRfcommChannelNumber = in.readInt();
        this.mL2capPsm = in.readInt();
        this.mServiceName = in.readString();
        this.mSupportedFeatures = in.readInt();
        this.mProfileVersion = in.readInt();
    }

    public int describeContents() {
        return 0;
    }

    public int getL2capPsm() {
        return this.mL2capPsm;
    }

    public int getRfcommChannelNumber() {
        return this.mRfcommChannelNumber;
    }

    public int getSupportedFeatures() {
        return this.mSupportedFeatures;
    }

    public String getServiceName() {
        return this.mServiceName;
    }

    public int getProfileVersion() {
        return this.mProfileVersion;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mRfcommChannelNumber);
        dest.writeInt(this.mL2capPsm);
        dest.writeString(this.mServiceName);
        dest.writeInt(this.mSupportedFeatures);
        dest.writeInt(this.mProfileVersion);
    }

    public String toString() {
        String ret = "Bluetooth MNS SDP Record:\n";
        if (this.mRfcommChannelNumber != -1) {
            ret = ret + "RFCOMM Chan Number: " + this.mRfcommChannelNumber + "\n";
        }
        if (this.mL2capPsm != -1) {
            ret = ret + "L2CAP PSM: " + this.mL2capPsm + "\n";
        }
        if (this.mServiceName != null) {
            ret = ret + "Service Name: " + this.mServiceName + "\n";
        }
        if (this.mSupportedFeatures != -1) {
            ret = ret + "Supported features: " + this.mSupportedFeatures + "\n";
        }
        if (this.mProfileVersion == -1) {
            return ret;
        }
        return ret + "Profile_version: " + this.mProfileVersion + "\n";
    }
}
