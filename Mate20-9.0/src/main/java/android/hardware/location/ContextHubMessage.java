package android.hardware.location;

import android.annotation.SystemApi;
import android.os.Parcel;
import android.os.Parcelable;
import java.util.Arrays;

@SystemApi
@Deprecated
public class ContextHubMessage implements Parcelable {
    public static final Parcelable.Creator<ContextHubMessage> CREATOR = new Parcelable.Creator<ContextHubMessage>() {
        public ContextHubMessage createFromParcel(Parcel in) {
            return new ContextHubMessage(in);
        }

        public ContextHubMessage[] newArray(int size) {
            return new ContextHubMessage[size];
        }
    };
    private static final int DEBUG_LOG_NUM_BYTES = 16;
    private byte[] mData;
    private int mType;
    private int mVersion;

    public int getMsgType() {
        return this.mType;
    }

    public int getVersion() {
        return this.mVersion;
    }

    public byte[] getData() {
        return Arrays.copyOf(this.mData, this.mData.length);
    }

    public void setMsgType(int msgType) {
        this.mType = msgType;
    }

    public void setVersion(int version) {
        this.mVersion = version;
    }

    public void setMsgData(byte[] data) {
        this.mData = Arrays.copyOf(data, data.length);
    }

    public ContextHubMessage(int msgType, int version, byte[] data) {
        this.mType = msgType;
        this.mVersion = version;
        this.mData = Arrays.copyOf(data, data.length);
    }

    public int describeContents() {
        return 0;
    }

    private ContextHubMessage(Parcel in) {
        this.mType = in.readInt();
        this.mVersion = in.readInt();
        this.mData = new byte[in.readInt()];
        in.readByteArray(this.mData);
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.mType);
        out.writeInt(this.mVersion);
        out.writeInt(this.mData.length);
        out.writeByteArray(this.mData);
    }

    public String toString() {
        String ret;
        int length = this.mData.length;
        String ret2 = "ContextHubMessage[type = " + this.mType + ", length = " + this.mData.length + " bytes](";
        if (length > 0) {
            ret2 = ret2 + "data = 0x";
        }
        for (int i = 0; i < Math.min(length, 16); i++) {
            ret = ret + Byte.toHexString(this.mData[i], true);
            if ((i + 1) % 4 == 0) {
                ret = ret + " ";
            }
        }
        if (length > 16) {
            ret = ret + "...";
        }
        return ret + ")";
    }
}
