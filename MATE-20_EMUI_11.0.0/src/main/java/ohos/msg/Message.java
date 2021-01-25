package ohos.msg;

import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.Parcel;

public class Message {
    private static final HiLogLabel TAG = new HiLogLabel(3, 0, "IPCMessage");
    private String mData = "";

    public Message() {
    }

    public Message(String str) {
        this.mData = str;
    }

    public boolean marshalling(Parcel parcel) throws MessengerException {
        if (parcel != null) {
            parcel.writeString(this.mData);
            return true;
        }
        throw new MessengerException("get parcel is null in Message Marshalling()", -1);
    }
}
