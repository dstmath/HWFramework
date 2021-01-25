package ohos.account;

import ohos.rpc.MessageParcel;

public class DistributedInfo {
    private String id;
    private String name;
    private int status;

    public boolean marshalling(MessageParcel messageParcel) {
        if (messageParcel != null && messageParcel.writeString(this.name) && messageParcel.writeString(this.id) && messageParcel.writeInt(this.status)) {
            return true;
        }
        return false;
    }

    public boolean unmarshalling(MessageParcel messageParcel) {
        if (messageParcel == null) {
            return false;
        }
        this.name = messageParcel.readString();
        this.id = messageParcel.readString();
        this.status = messageParcel.readInt();
        return true;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String str) {
        this.name = str;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String str) {
        this.id = str;
    }

    public int getStatus() {
        return this.status;
    }

    public void setStatus(int i) {
        this.status = i;
    }
}
