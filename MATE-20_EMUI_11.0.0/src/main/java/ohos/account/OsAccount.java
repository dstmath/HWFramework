package ohos.account;

import java.util.List;
import ohos.rpc.MessageParcel;

public class OsAccount {
    private List<String> constraints;
    private DistributedInfo distributedInfo;
    private int localId;
    private String localName;
    private OsAccountStatus status;
    private OsAccountType type;

    public OsAccount() {
        this(null, OsAccountType.INVALID);
    }

    public OsAccount(String str, OsAccountType osAccountType) {
        this.status = OsAccountStatus.INVALID;
        this.distributedInfo = null;
        this.localName = str;
        this.type = osAccountType;
    }

    public boolean marshalling(MessageParcel messageParcel) {
        if (messageParcel != null && messageParcel.writeInt(this.localId) && OsAccountType.marshallingEnum(messageParcel, this.type) && messageParcel.writeString(this.localName) && OsAccountStatus.marshallingEnum(messageParcel, this.status)) {
            return true;
        }
        return false;
    }

    public boolean unmarshalling(MessageParcel messageParcel) {
        if (messageParcel == null) {
            return false;
        }
        this.localId = messageParcel.readInt();
        this.type = OsAccountType.unmarshallingEnum(messageParcel);
        this.localName = messageParcel.readString();
        this.status = OsAccountStatus.unmarshallingEnum(messageParcel);
        return true;
    }

    public String getLocalName() {
        return this.localName;
    }

    public void setLocalName(String str) {
        this.localName = str;
    }

    public int getLocalId() {
        return this.localId;
    }

    public void setLocalId(int i) {
        this.localId = i;
    }

    public OsAccountType getType() {
        return this.type;
    }

    public void setType(OsAccountType osAccountType) {
        this.type = osAccountType;
    }

    public List<String> getConstraints() {
        return this.constraints;
    }

    public void setConstraints(List<String> list) {
        this.constraints = list;
    }

    public DistributedInfo getDistributedInfo() {
        return this.distributedInfo;
    }

    public void setDistributedInfo(DistributedInfo distributedInfo2) {
        this.distributedInfo = distributedInfo2;
    }
}
