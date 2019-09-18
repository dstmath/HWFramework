package com.huawei.nb.model.policy;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class PolicyRuntimeData extends AManagedObject {
    public static final Parcelable.Creator<PolicyRuntimeData> CREATOR = new Parcelable.Creator<PolicyRuntimeData>() {
        public PolicyRuntimeData createFromParcel(Parcel in) {
            return new PolicyRuntimeData(in);
        }

        public PolicyRuntimeData[] newArray(int size) {
            return new PolicyRuntimeData[size];
        }
    };
    private String category;
    private Long id;
    private String name;
    private String serviceName;
    private Long timeStamp;
    private String value;

    public PolicyRuntimeData(Cursor cursor) {
        Long l = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.id = cursor.isNull(1) ? null : Long.valueOf(cursor.getLong(1));
        this.serviceName = cursor.getString(2);
        this.category = cursor.getString(3);
        this.name = cursor.getString(4);
        this.value = cursor.getString(5);
        this.timeStamp = !cursor.isNull(6) ? Long.valueOf(cursor.getLong(6)) : l;
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public PolicyRuntimeData(Parcel in) {
        super(in);
        Long l = null;
        if (in.readByte() == 0) {
            this.id = null;
            in.readLong();
        } else {
            this.id = Long.valueOf(in.readLong());
        }
        this.serviceName = in.readByte() == 0 ? null : in.readString();
        this.category = in.readByte() == 0 ? null : in.readString();
        this.name = in.readByte() == 0 ? null : in.readString();
        this.value = in.readByte() == 0 ? null : in.readString();
        this.timeStamp = in.readByte() != 0 ? Long.valueOf(in.readLong()) : l;
    }

    private PolicyRuntimeData(Long id2, String serviceName2, String category2, String name2, String value2, Long timeStamp2) {
        this.id = id2;
        this.serviceName = serviceName2;
        this.category = category2;
        this.name = name2;
        this.value = value2;
        this.timeStamp = timeStamp2;
    }

    public PolicyRuntimeData() {
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

    public String getServiceName() {
        return this.serviceName;
    }

    public void setServiceName(String serviceName2) {
        this.serviceName = serviceName2;
        setValue();
    }

    public String getCategory() {
        return this.category;
    }

    public void setCategory(String category2) {
        this.category = category2;
        setValue();
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name2) {
        this.name = name2;
        setValue();
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value2) {
        this.value = value2;
        setValue();
    }

    public Long getTimeStamp() {
        return this.timeStamp;
    }

    public void setTimeStamp(Long timeStamp2) {
        this.timeStamp = timeStamp2;
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
        if (this.serviceName != null) {
            out.writeByte((byte) 1);
            out.writeString(this.serviceName);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.category != null) {
            out.writeByte((byte) 1);
            out.writeString(this.category);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.name != null) {
            out.writeByte((byte) 1);
            out.writeString(this.name);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.value != null) {
            out.writeByte((byte) 1);
            out.writeString(this.value);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.timeStamp != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.timeStamp.longValue());
            return;
        }
        out.writeByte((byte) 0);
    }

    public AEntityHelper<PolicyRuntimeData> getHelper() {
        return PolicyRuntimeDataHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.policy.PolicyRuntimeData";
    }

    public String getDatabaseName() {
        return "dsServiceMetaData";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("PolicyRuntimeData { id: ").append(this.id);
        sb.append(", serviceName: ").append(this.serviceName);
        sb.append(", category: ").append(this.category);
        sb.append(", name: ").append(this.name);
        sb.append(", value: ").append(this.value);
        sb.append(", timeStamp: ").append(this.timeStamp);
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
        return "0.0.12";
    }

    public int getDatabaseVersionCode() {
        return 12;
    }

    public String getEntityVersion() {
        return "0.0.2";
    }

    public int getEntityVersionCode() {
        return 2;
    }
}
