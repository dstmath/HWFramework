package ohos.ai.engine.aimodel;

import java.util.ArrayList;
import java.util.List;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class CallbackBean implements Sequenceable {
    private static final int INVALID_SIZE = 0;
    private String businessDomain;
    private List<Long> deleteOriginIds = new ArrayList();
    private List<Long> insertOriginIds = new ArrayList();
    private List<Long> updateOriginIds = new ArrayList();

    public String getBusiDomain() {
        return this.businessDomain;
    }

    public void setBusiDomain(String str) {
        this.businessDomain = str;
    }

    public List<Long> getDeleteOriginIds() {
        return this.deleteOriginIds;
    }

    public void setDeleteOriginId(Long l) {
        this.deleteOriginIds.add(l);
    }

    public List<Long> getInsertOriginIds() {
        return this.insertOriginIds;
    }

    public void setInsertOriginId(Long l) {
        this.insertOriginIds.add(l);
    }

    public List<Long> getUpdateOriginIds() {
        return this.updateOriginIds;
    }

    public void setUpdateOriginId(Long l) {
        this.updateOriginIds.add(l);
    }

    public boolean marshalling(Parcel parcel) {
        parcel.writeString(this.businessDomain);
        parcel.writeInt(this.deleteOriginIds.size());
        for (Long l : this.deleteOriginIds) {
            parcel.writeLong(l.longValue());
        }
        parcel.writeInt(this.insertOriginIds.size());
        for (Long l2 : this.insertOriginIds) {
            parcel.writeLong(l2.longValue());
        }
        parcel.writeInt(this.updateOriginIds.size());
        for (Long l3 : this.updateOriginIds) {
            parcel.writeLong(l3.longValue());
        }
        return true;
    }

    public boolean unmarshalling(Parcel parcel) {
        this.businessDomain = parcel.readString();
        int readInt = parcel.readInt();
        for (int i = 0; i < readInt; i++) {
            this.deleteOriginIds.add(Long.valueOf(parcel.readLong()));
        }
        int readInt2 = parcel.readInt();
        for (int i2 = 0; i2 < readInt2; i2++) {
            this.insertOriginIds.add(Long.valueOf(parcel.readLong()));
        }
        int readInt3 = parcel.readInt();
        for (int i3 = 0; i3 < readInt3; i3++) {
            this.updateOriginIds.add(Long.valueOf(parcel.readLong()));
        }
        return true;
    }

    public boolean isModelChange() {
        return (this.deleteOriginIds.size() == 0 && this.insertOriginIds.size() == 0 && this.updateOriginIds.size() == 0) ? false : true;
    }
}
