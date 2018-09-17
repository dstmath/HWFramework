package android.bluetooth;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.Arrays;

public class SdpOppOpsRecord implements Parcelable {
    public static final Creator CREATOR = new Creator() {
        public SdpOppOpsRecord createFromParcel(Parcel in) {
            return new SdpOppOpsRecord(in);
        }

        public SdpOppOpsRecord[] newArray(int size) {
            return new SdpOppOpsRecord[size];
        }
    };
    private final byte[] mFormatsList;
    private final int mL2capPsm;
    private final int mProfileVersion;
    private final int mRfcommChannel;
    private final String mServiceName;

    public SdpOppOpsRecord(String serviceName, int rfcommChannel, int l2capPsm, int version, byte[] formatsList) {
        this.mServiceName = serviceName;
        this.mRfcommChannel = rfcommChannel;
        this.mL2capPsm = l2capPsm;
        this.mProfileVersion = version;
        this.mFormatsList = formatsList;
    }

    public String getServiceName() {
        return this.mServiceName;
    }

    public int getRfcommChannel() {
        return this.mRfcommChannel;
    }

    public int getL2capPsm() {
        return this.mL2capPsm;
    }

    public int getProfileVersion() {
        return this.mProfileVersion;
    }

    public byte[] getFormatsList() {
        return this.mFormatsList;
    }

    public int describeContents() {
        return 0;
    }

    public SdpOppOpsRecord(Parcel in) {
        this.mRfcommChannel = in.readInt();
        this.mL2capPsm = in.readInt();
        this.mProfileVersion = in.readInt();
        this.mServiceName = in.readString();
        int arrayLength = in.readInt();
        if (arrayLength > 0) {
            byte[] bytes = new byte[arrayLength];
            in.readByteArray(bytes);
            this.mFormatsList = bytes;
            return;
        }
        this.mFormatsList = null;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mRfcommChannel);
        dest.writeInt(this.mL2capPsm);
        dest.writeInt(this.mProfileVersion);
        dest.writeString(this.mServiceName);
        if (this.mFormatsList == null || this.mFormatsList.length <= 0) {
            dest.writeInt(0);
            return;
        }
        dest.writeInt(this.mFormatsList.length);
        dest.writeByteArray(this.mFormatsList);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("Bluetooth OPP Server SDP Record:\n");
        sb.append("  RFCOMM Chan Number: ").append(this.mRfcommChannel);
        sb.append("\n  L2CAP PSM: ").append(this.mL2capPsm);
        sb.append("\n  Profile version: ").append(this.mProfileVersion);
        sb.append("\n  Service Name: ").append(this.mServiceName);
        sb.append("\n  Formats List: ").append(Arrays.toString(this.mFormatsList));
        return sb.toString();
    }
}
