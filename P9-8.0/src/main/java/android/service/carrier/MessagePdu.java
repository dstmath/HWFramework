package android.service.carrier;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.ArrayList;
import java.util.List;

public final class MessagePdu implements Parcelable {
    public static final Creator<MessagePdu> CREATOR = new Creator<MessagePdu>() {
        public MessagePdu createFromParcel(Parcel source) {
            List pduList;
            int size = source.readInt();
            if (size == -1) {
                pduList = null;
            } else {
                pduList = new ArrayList(size);
                for (int i = 0; i < size; i++) {
                    pduList.add(source.createByteArray());
                }
            }
            return new MessagePdu(pduList);
        }

        public MessagePdu[] newArray(int size) {
            return new MessagePdu[size];
        }
    };
    private static final int NULL_LENGTH = -1;
    private final List<byte[]> mPduList;

    public MessagePdu(List<byte[]> pduList) {
        if (pduList == null || pduList.contains(null)) {
            throw new IllegalArgumentException("pduList must not be null or contain nulls");
        }
        this.mPduList = pduList;
    }

    public List<byte[]> getPdus() {
        return this.mPduList;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        if (this.mPduList == null) {
            dest.writeInt(-1);
            return;
        }
        dest.writeInt(this.mPduList.size());
        for (byte[] messagePdu : this.mPduList) {
            dest.writeByteArray(messagePdu);
        }
    }
}
