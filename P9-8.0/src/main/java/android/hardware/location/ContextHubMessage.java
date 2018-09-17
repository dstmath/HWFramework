package android.hardware.location;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import java.util.Arrays;

public class ContextHubMessage {
    public static final Creator<ContextHubMessage> CREATOR = new Creator<ContextHubMessage>() {
        public ContextHubMessage createFromParcel(Parcel in) {
            return new ContextHubMessage(in, null);
        }

        public ContextHubMessage[] newArray(int size) {
            return new ContextHubMessage[size];
        }
    };
    private static final String TAG = "ContextHubMessage";
    private byte[] mData;
    private int mType;
    private int mVersion;

    /* synthetic */ ContextHubMessage(Parcel in, ContextHubMessage -this1) {
        this(in);
    }

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
}
