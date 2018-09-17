package android.bluetooth;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class SdpPseRecord implements Parcelable {
    public static final Creator CREATOR = new Creator() {
        public SdpPseRecord createFromParcel(Parcel in) {
            return new SdpPseRecord(in);
        }

        public SdpPseRecord[] newArray(int size) {
            return new SdpPseRecord[size];
        }
    };
    private final int mL2capPsm;
    private final int mProfileVersion;
    private final int mRfcommChannelNumber;
    private final String mServiceName;
    private final int mSupportedFeatures;
    private final int mSupportedRepositories;

    public SdpPseRecord(int l2cap_psm, int rfcomm_channel_number, int profile_version, int supported_features, int supported_repositories, String service_name) {
        this.mL2capPsm = l2cap_psm;
        this.mRfcommChannelNumber = rfcomm_channel_number;
        this.mProfileVersion = profile_version;
        this.mSupportedFeatures = supported_features;
        this.mSupportedRepositories = supported_repositories;
        this.mServiceName = service_name;
    }

    public SdpPseRecord(Parcel in) {
        this.mRfcommChannelNumber = in.readInt();
        this.mL2capPsm = in.readInt();
        this.mProfileVersion = in.readInt();
        this.mSupportedFeatures = in.readInt();
        this.mSupportedRepositories = in.readInt();
        this.mServiceName = in.readString();
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

    public int getSupportedRepositories() {
        return this.mSupportedRepositories;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mRfcommChannelNumber);
        dest.writeInt(this.mL2capPsm);
        dest.writeInt(this.mProfileVersion);
        dest.writeInt(this.mSupportedFeatures);
        dest.writeInt(this.mSupportedRepositories);
        dest.writeString(this.mServiceName);
    }

    public String toString() {
        String ret = "Bluetooth MNS SDP Record:\n";
        if (this.mRfcommChannelNumber != -1) {
            ret = ret + "RFCOMM Chan Number: " + this.mRfcommChannelNumber + "\n";
        }
        if (this.mL2capPsm != -1) {
            ret = ret + "L2CAP PSM: " + this.mL2capPsm + "\n";
        }
        if (this.mProfileVersion != -1) {
            ret = ret + "profile version: " + this.mProfileVersion + "\n";
        }
        if (this.mServiceName != null) {
            ret = ret + "Service Name: " + this.mServiceName + "\n";
        }
        if (this.mSupportedFeatures != -1) {
            ret = ret + "Supported features: " + this.mSupportedFeatures + "\n";
        }
        if (this.mSupportedRepositories != -1) {
            return ret + "Supported repositories: " + this.mSupportedRepositories + "\n";
        }
        return ret;
    }
}
