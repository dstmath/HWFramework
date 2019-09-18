package android.hardware.location;

import android.annotation.SystemApi;
import android.os.Parcel;
import android.os.Parcelable;

@SystemApi
public final class NanoAppMessage implements Parcelable {
    public static final Parcelable.Creator<NanoAppMessage> CREATOR = new Parcelable.Creator<NanoAppMessage>() {
        public NanoAppMessage createFromParcel(Parcel in) {
            return new NanoAppMessage(in);
        }

        public NanoAppMessage[] newArray(int size) {
            return new NanoAppMessage[size];
        }
    };
    private static final int DEBUG_LOG_NUM_BYTES = 16;
    private boolean mIsBroadcasted;
    private byte[] mMessageBody;
    private int mMessageType;
    private long mNanoAppId;

    private NanoAppMessage(long nanoAppId, int messageType, byte[] messageBody, boolean broadcasted) {
        this.mNanoAppId = nanoAppId;
        this.mMessageType = messageType;
        this.mMessageBody = messageBody;
        this.mIsBroadcasted = broadcasted;
    }

    public static NanoAppMessage createMessageToNanoApp(long targetNanoAppId, int messageType, byte[] messageBody) {
        NanoAppMessage nanoAppMessage = new NanoAppMessage(targetNanoAppId, messageType, messageBody, false);
        return nanoAppMessage;
    }

    public static NanoAppMessage createMessageFromNanoApp(long sourceNanoAppId, int messageType, byte[] messageBody, boolean broadcasted) {
        NanoAppMessage nanoAppMessage = new NanoAppMessage(sourceNanoAppId, messageType, messageBody, broadcasted);
        return nanoAppMessage;
    }

    public long getNanoAppId() {
        return this.mNanoAppId;
    }

    public int getMessageType() {
        return this.mMessageType;
    }

    public byte[] getMessageBody() {
        return this.mMessageBody;
    }

    public boolean isBroadcastMessage() {
        return this.mIsBroadcasted;
    }

    private NanoAppMessage(Parcel in) {
        this.mNanoAppId = in.readLong();
        this.mIsBroadcasted = in.readInt() != 1 ? false : true;
        this.mMessageType = in.readInt();
        this.mMessageBody = new byte[in.readInt()];
        in.readByteArray(this.mMessageBody);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(this.mNanoAppId);
        out.writeInt(this.mIsBroadcasted ? 1 : 0);
        out.writeInt(this.mMessageType);
        out.writeInt(this.mMessageBody.length);
        out.writeByteArray(this.mMessageBody);
    }

    public String toString() {
        String ret;
        int length = this.mMessageBody.length;
        StringBuilder sb = new StringBuilder();
        sb.append("NanoAppMessage[type = ");
        sb.append(this.mMessageType);
        sb.append(", length = ");
        sb.append(this.mMessageBody.length);
        sb.append(" bytes, ");
        sb.append(this.mIsBroadcasted ? "broadcast" : "unicast");
        sb.append(", nanoapp = 0x");
        sb.append(Long.toHexString(this.mNanoAppId));
        sb.append("](");
        String ret2 = sb.toString();
        if (length > 0) {
            ret2 = ret2 + "data = 0x";
        }
        for (int i = 0; i < Math.min(length, 16); i++) {
            ret = ret + Byte.toHexString(this.mMessageBody[i], true);
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
