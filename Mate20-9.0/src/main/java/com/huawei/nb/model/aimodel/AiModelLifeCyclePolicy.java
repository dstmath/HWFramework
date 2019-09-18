package com.huawei.nb.model.aimodel;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class AiModelLifeCyclePolicy extends AManagedObject {
    public static final Parcelable.Creator<AiModelLifeCyclePolicy> CREATOR = new Parcelable.Creator<AiModelLifeCyclePolicy>() {
        public AiModelLifeCyclePolicy createFromParcel(Parcel in) {
            return new AiModelLifeCyclePolicy(in);
        }

        public AiModelLifeCyclePolicy[] newArray(int size) {
            return new AiModelLifeCyclePolicy[size];
        }
    };
    private Long aimodel_id;
    private String delete_policy;
    private Long id;
    private String reserved_1;
    private String update_policy;

    public AiModelLifeCyclePolicy(Cursor cursor) {
        Long l = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.id = cursor.isNull(1) ? null : Long.valueOf(cursor.getLong(1));
        this.aimodel_id = !cursor.isNull(2) ? Long.valueOf(cursor.getLong(2)) : l;
        this.delete_policy = cursor.getString(3);
        this.update_policy = cursor.getString(4);
        this.reserved_1 = cursor.getString(5);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public AiModelLifeCyclePolicy(Parcel in) {
        super(in);
        String str = null;
        if (in.readByte() == 0) {
            this.id = null;
            in.readLong();
        } else {
            this.id = Long.valueOf(in.readLong());
        }
        this.aimodel_id = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.delete_policy = in.readByte() == 0 ? null : in.readString();
        this.update_policy = in.readByte() == 0 ? null : in.readString();
        this.reserved_1 = in.readByte() != 0 ? in.readString() : str;
    }

    private AiModelLifeCyclePolicy(Long id2, Long aimodel_id2, String delete_policy2, String update_policy2, String reserved_12) {
        this.id = id2;
        this.aimodel_id = aimodel_id2;
        this.delete_policy = delete_policy2;
        this.update_policy = update_policy2;
        this.reserved_1 = reserved_12;
    }

    public AiModelLifeCyclePolicy() {
    }

    public int describeContents() {
        return 0;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id2) {
        this.id = id2;
        setValue();
    }

    public Long getAimodel_id() {
        return this.aimodel_id;
    }

    public void setAimodel_id(Long aimodel_id2) {
        this.aimodel_id = aimodel_id2;
        setValue();
    }

    public String getDelete_policy() {
        return this.delete_policy;
    }

    public void setDelete_policy(String delete_policy2) {
        this.delete_policy = delete_policy2;
        setValue();
    }

    public String getUpdate_policy() {
        return this.update_policy;
    }

    public void setUpdate_policy(String update_policy2) {
        this.update_policy = update_policy2;
        setValue();
    }

    public String getReserved_1() {
        return this.reserved_1;
    }

    public void setReserved_1(String reserved_12) {
        this.reserved_1 = reserved_12;
        setValue();
    }

    public void writeToParcel(Parcel out, int ignored) {
        super.writeToParcel(out, ignored);
        if (this.id != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.id.longValue());
        } else {
            out.writeByte((byte) 0);
            out.writeLong(1);
        }
        if (this.aimodel_id != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.aimodel_id.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.delete_policy != null) {
            out.writeByte((byte) 1);
            out.writeString(this.delete_policy);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.update_policy != null) {
            out.writeByte((byte) 1);
            out.writeString(this.update_policy);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.reserved_1 != null) {
            out.writeByte((byte) 1);
            out.writeString(this.reserved_1);
            return;
        }
        out.writeByte((byte) 0);
    }

    public AEntityHelper<AiModelLifeCyclePolicy> getHelper() {
        return AiModelLifeCyclePolicyHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.aimodel.AiModelLifeCyclePolicy";
    }

    public String getDatabaseName() {
        return "dsAiModel";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("AiModelLifeCyclePolicy { id: ").append(this.id);
        sb.append(", aimodel_id: ").append(this.aimodel_id);
        sb.append(", delete_policy: ").append(this.delete_policy);
        sb.append(", update_policy: ").append(this.update_policy);
        sb.append(", reserved_1: ").append(this.reserved_1);
        sb.append(" }");
        return sb.toString();
    }

    public boolean equals(Object o) {
        return super.equals(o);
    }

    public int hashCode() {
        return super.hashCode();
    }

    public String getDatabaseVersion() {
        return "0.0.11";
    }

    public int getDatabaseVersionCode() {
        return 11;
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }
}
