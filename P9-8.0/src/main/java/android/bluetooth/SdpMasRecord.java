package android.bluetooth;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class SdpMasRecord implements Parcelable {
    public static final Creator CREATOR = new Creator() {
        public SdpMasRecord createFromParcel(Parcel in) {
            return new SdpMasRecord(in);
        }

        public SdpRecord[] newArray(int size) {
            return new SdpRecord[size];
        }
    };
    private final int mL2capPsm;
    private final int mMasInstanceId;
    private final int mProfileVersion;
    private final int mRfcommChannelNumber;
    private final String mServiceName;
    private final int mSupportedFeatures;
    private final int mSupportedMessageTypes;

    public static final class MessageType {
        public static final int EMAIL = 1;
        public static final int MMS = 8;
        public static final int SMS_CDMA = 4;
        public static final int SMS_GSM = 2;
    }

    public SdpMasRecord(int mas_instance_id, int l2cap_psm, int rfcomm_channel_number, int profile_version, int supported_features, int supported_message_types, String service_name) {
        this.mMasInstanceId = mas_instance_id;
        this.mL2capPsm = l2cap_psm;
        this.mRfcommChannelNumber = rfcomm_channel_number;
        this.mProfileVersion = profile_version;
        this.mSupportedFeatures = supported_features;
        this.mSupportedMessageTypes = supported_message_types;
        this.mServiceName = service_name;
    }

    public SdpMasRecord(Parcel in) {
        this.mMasInstanceId = in.readInt();
        this.mL2capPsm = in.readInt();
        this.mRfcommChannelNumber = in.readInt();
        this.mProfileVersion = in.readInt();
        this.mSupportedFeatures = in.readInt();
        this.mSupportedMessageTypes = in.readInt();
        this.mServiceName = in.readString();
    }

    public int describeContents() {
        return 0;
    }

    public int getMasInstanceId() {
        return this.mMasInstanceId;
    }

    public int getL2capPsm() {
        return this.mL2capPsm;
    }

    public int getRfcommCannelNumber() {
        return this.mRfcommChannelNumber;
    }

    public int getProfileVersion() {
        return this.mProfileVersion;
    }

    public int getSupportedFeatures() {
        return this.mSupportedFeatures;
    }

    public int getSupportedMessageTypes() {
        return this.mSupportedMessageTypes;
    }

    public boolean msgSupported(int msg) {
        return (this.mSupportedMessageTypes & msg) != 0;
    }

    public String getServiceName() {
        return this.mServiceName;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mMasInstanceId);
        dest.writeInt(this.mL2capPsm);
        dest.writeInt(this.mRfcommChannelNumber);
        dest.writeInt(this.mProfileVersion);
        dest.writeInt(this.mSupportedFeatures);
        dest.writeInt(this.mSupportedMessageTypes);
        dest.writeString(this.mServiceName);
    }

    public String toString() {
        String ret = "Bluetooth MAS SDP Record:\n";
        if (this.mMasInstanceId != -1) {
            ret = ret + "Mas Instance Id: " + this.mMasInstanceId + "\n";
        }
        if (this.mRfcommChannelNumber != -1) {
            ret = ret + "RFCOMM Chan Number: " + this.mRfcommChannelNumber + "\n";
        }
        if (this.mL2capPsm != -1) {
            ret = ret + "L2CAP PSM: " + this.mL2capPsm + "\n";
        }
        if (this.mServiceName != null) {
            ret = ret + "Service Name: " + this.mServiceName + "\n";
        }
        if (this.mProfileVersion != -1) {
            ret = ret + "Profile version: " + this.mProfileVersion + "\n";
        }
        if (this.mSupportedMessageTypes != -1) {
            ret = ret + "Supported msg types: " + this.mSupportedMessageTypes + "\n";
        }
        if (this.mSupportedFeatures != -1) {
            return ret + "Supported features: " + this.mSupportedFeatures + "\n";
        }
        return ret;
    }
}
