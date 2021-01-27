package com.huawei.nb.model.policy;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class PolicyRuntimeData extends AManagedObject {
    public static final Parcelable.Creator<PolicyRuntimeData> CREATOR = new Parcelable.Creator<PolicyRuntimeData>() {
        /* class com.huawei.nb.model.policy.PolicyRuntimeData.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public PolicyRuntimeData createFromParcel(Parcel parcel) {
            return new PolicyRuntimeData(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public PolicyRuntimeData[] newArray(int i) {
            return new PolicyRuntimeData[i];
        }
    };
    private String category;
    private Long id;
    private String name;
    private String serviceName;
    private Long timeStamp;
    private String value;

    @Override // com.huawei.odmf.core.AManagedObject, android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getDatabaseName() {
        return "dsServiceMetaData";
    }

    public String getDatabaseVersion() {
        return "0.0.13";
    }

    public int getDatabaseVersionCode() {
        return 13;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getEntityName() {
        return "com.huawei.nb.model.policy.PolicyRuntimeData";
    }

    public String getEntityVersion() {
        return "0.0.2";
    }

    public int getEntityVersionCode() {
        return 2;
    }

    public PolicyRuntimeData(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Long l = null;
        this.id = cursor.isNull(1) ? null : Long.valueOf(cursor.getLong(1));
        this.serviceName = cursor.getString(2);
        this.category = cursor.getString(3);
        this.name = cursor.getString(4);
        this.value = cursor.getString(5);
        this.timeStamp = !cursor.isNull(6) ? Long.valueOf(cursor.getLong(6)) : l;
    }

    public PolicyRuntimeData(Parcel parcel) {
        super(parcel);
        Long l = null;
        if (parcel.readByte() == 0) {
            this.id = null;
            parcel.readLong();
        } else {
            this.id = Long.valueOf(parcel.readLong());
        }
        this.serviceName = parcel.readByte() == 0 ? null : parcel.readString();
        this.category = parcel.readByte() == 0 ? null : parcel.readString();
        this.name = parcel.readByte() == 0 ? null : parcel.readString();
        this.value = parcel.readByte() == 0 ? null : parcel.readString();
        this.timeStamp = parcel.readByte() != 0 ? Long.valueOf(parcel.readLong()) : l;
    }

    private PolicyRuntimeData(Long l, String str, String str2, String str3, String str4, Long l2) {
        this.id = l;
        this.serviceName = str;
        this.category = str2;
        this.name = str3;
        this.value = str4;
        this.timeStamp = l2;
    }

    public PolicyRuntimeData() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long l) {
        this.id = l;
        setValue();
    }

    public String getServiceName() {
        return this.serviceName;
    }

    public void setServiceName(String str) {
        this.serviceName = str;
        setValue();
    }

    public String getCategory() {
        return this.category;
    }

    public void setCategory(String str) {
        this.category = str;
        setValue();
    }

    public String getName() {
        return this.name;
    }

    public void setName(String str) {
        this.name = str;
        setValue();
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String str) {
        this.value = str;
        setValue();
    }

    public Long getTimeStamp() {
        return this.timeStamp;
    }

    public void setTimeStamp(Long l) {
        this.timeStamp = l;
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
        if (this.serviceName != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.serviceName);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.category != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.category);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.name != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.name);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.value != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.value);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.timeStamp != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.timeStamp.longValue());
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<PolicyRuntimeData> getHelper() {
        return PolicyRuntimeDataHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "PolicyRuntimeData { id: " + this.id + ", serviceName: " + this.serviceName + ", category: " + this.category + ", name: " + this.name + ", value: " + this.value + ", timeStamp: " + this.timeStamp + " }";
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
