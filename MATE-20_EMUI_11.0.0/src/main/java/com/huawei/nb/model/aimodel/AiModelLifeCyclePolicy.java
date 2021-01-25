package com.huawei.nb.model.aimodel;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class AiModelLifeCyclePolicy extends AManagedObject {
    public static final Parcelable.Creator<AiModelLifeCyclePolicy> CREATOR = new Parcelable.Creator<AiModelLifeCyclePolicy>() {
        /* class com.huawei.nb.model.aimodel.AiModelLifeCyclePolicy.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public AiModelLifeCyclePolicy createFromParcel(Parcel parcel) {
            return new AiModelLifeCyclePolicy(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public AiModelLifeCyclePolicy[] newArray(int i) {
            return new AiModelLifeCyclePolicy[i];
        }
    };
    private Long aimodel_id;
    private String delete_policy;
    private Long id;
    private String reserved_1;
    private String update_policy;

    @Override // com.huawei.odmf.core.AManagedObject, android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getDatabaseName() {
        return "dsAiModel";
    }

    public String getDatabaseVersion() {
        return "0.0.13";
    }

    public int getDatabaseVersionCode() {
        return 13;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getEntityName() {
        return "com.huawei.nb.model.aimodel.AiModelLifeCyclePolicy";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public AiModelLifeCyclePolicy(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Long l = null;
        this.id = cursor.isNull(1) ? null : Long.valueOf(cursor.getLong(1));
        this.aimodel_id = !cursor.isNull(2) ? Long.valueOf(cursor.getLong(2)) : l;
        this.delete_policy = cursor.getString(3);
        this.update_policy = cursor.getString(4);
        this.reserved_1 = cursor.getString(5);
    }

    public AiModelLifeCyclePolicy(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.id = null;
            parcel.readLong();
        } else {
            this.id = Long.valueOf(parcel.readLong());
        }
        this.aimodel_id = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.delete_policy = parcel.readByte() == 0 ? null : parcel.readString();
        this.update_policy = parcel.readByte() == 0 ? null : parcel.readString();
        this.reserved_1 = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private AiModelLifeCyclePolicy(Long l, Long l2, String str, String str2, String str3) {
        this.id = l;
        this.aimodel_id = l2;
        this.delete_policy = str;
        this.update_policy = str2;
        this.reserved_1 = str3;
    }

    public AiModelLifeCyclePolicy() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long l) {
        this.id = l;
        setValue();
    }

    public Long getAimodel_id() {
        return this.aimodel_id;
    }

    public void setAimodel_id(Long l) {
        this.aimodel_id = l;
        setValue();
    }

    public String getDelete_policy() {
        return this.delete_policy;
    }

    public void setDelete_policy(String str) {
        this.delete_policy = str;
        setValue();
    }

    public String getUpdate_policy() {
        return this.update_policy;
    }

    public void setUpdate_policy(String str) {
        this.update_policy = str;
        setValue();
    }

    public String getReserved_1() {
        return this.reserved_1;
    }

    public void setReserved_1(String str) {
        this.reserved_1 = str;
        setValue();
    }

    @Override // com.huawei.odmf.core.AManagedObject, android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        if (this.id != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.id.longValue());
        } else {
            parcel.writeByte((byte) 0);
            parcel.writeLong(1);
        }
        if (this.aimodel_id != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.aimodel_id.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.delete_policy != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.delete_policy);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.update_policy != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.update_policy);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.reserved_1 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.reserved_1);
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<AiModelLifeCyclePolicy> getHelper() {
        return AiModelLifeCyclePolicyHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "AiModelLifeCyclePolicy { id: " + this.id + ", aimodel_id: " + this.aimodel_id + ", delete_policy: " + this.delete_policy + ", update_policy: " + this.update_policy + ", reserved_1: " + this.reserved_1 + " }";
    }

    @Override // com.huawei.odmf.core.AManagedObject, java.lang.Object
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override // com.huawei.odmf.core.AManagedObject, java.lang.Object
    public int hashCode() {
        return super.hashCode();
    }
}
