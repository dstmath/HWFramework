package com.huawei.nb.model.aimodel;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class AiModelAuthority extends AManagedObject {
    public static final Parcelable.Creator<AiModelAuthority> CREATOR = new Parcelable.Creator<AiModelAuthority>() {
        /* class com.huawei.nb.model.aimodel.AiModelAuthority.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public AiModelAuthority createFromParcel(Parcel parcel) {
            return new AiModelAuthority(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public AiModelAuthority[] newArray(int i) {
            return new AiModelAuthority[i];
        }
    };
    private Long aimodel_id;
    private Integer authority;
    private String business_attribute;
    private String business_name;
    private Long id;
    private String reserved_1;

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
        return "com.huawei.nb.model.aimodel.AiModelAuthority";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public AiModelAuthority(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Integer num = null;
        this.id = cursor.isNull(1) ? null : Long.valueOf(cursor.getLong(1));
        this.aimodel_id = cursor.isNull(2) ? null : Long.valueOf(cursor.getLong(2));
        this.business_name = cursor.getString(3);
        this.business_attribute = cursor.getString(4);
        this.authority = !cursor.isNull(5) ? Integer.valueOf(cursor.getInt(5)) : num;
        this.reserved_1 = cursor.getString(6);
    }

    public AiModelAuthority(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.id = null;
            parcel.readLong();
        } else {
            this.id = Long.valueOf(parcel.readLong());
        }
        this.aimodel_id = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.business_name = parcel.readByte() == 0 ? null : parcel.readString();
        this.business_attribute = parcel.readByte() == 0 ? null : parcel.readString();
        this.authority = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.reserved_1 = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private AiModelAuthority(Long l, Long l2, String str, String str2, Integer num, String str3) {
        this.id = l;
        this.aimodel_id = l2;
        this.business_name = str;
        this.business_attribute = str2;
        this.authority = num;
        this.reserved_1 = str3;
    }

    public AiModelAuthority() {
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

    public String getBusiness_name() {
        return this.business_name;
    }

    public void setBusiness_name(String str) {
        this.business_name = str;
        setValue();
    }

    public String getBusiness_attribute() {
        return this.business_attribute;
    }

    public void setBusiness_attribute(String str) {
        this.business_attribute = str;
        setValue();
    }

    public Integer getAuthority() {
        return this.authority;
    }

    public void setAuthority(Integer num) {
        this.authority = num;
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
        if (this.business_name != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.business_name);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.business_attribute != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.business_attribute);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.authority != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.authority.intValue());
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
    public AEntityHelper<AiModelAuthority> getHelper() {
        return AiModelAuthorityHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "AiModelAuthority { id: " + this.id + ", aimodel_id: " + this.aimodel_id + ", business_name: " + this.business_name + ", business_attribute: " + this.business_attribute + ", authority: " + this.authority + ", reserved_1: " + this.reserved_1 + " }";
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
