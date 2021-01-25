package com.huawei.nb.model.rule;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class Business extends AManagedObject {
    public static final Parcelable.Creator<Business> CREATOR = new Parcelable.Creator<Business>() {
        /* class com.huawei.nb.model.rule.Business.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public Business createFromParcel(Parcel parcel) {
            return new Business(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public Business[] newArray(int i) {
            return new Business[i];
        }
    };
    private Integer businessType;
    private String description;
    private Long id;
    private Integer level;
    private String name;
    private Long parentId;

    @Override // com.huawei.odmf.core.AManagedObject, android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getDatabaseName() {
        return "dsRule";
    }

    public String getDatabaseVersion() {
        return "0.0.4";
    }

    public int getDatabaseVersionCode() {
        return 4;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getEntityName() {
        return "com.huawei.nb.model.rule.Business";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public Business(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Long l = null;
        this.id = cursor.isNull(1) ? null : Long.valueOf(cursor.getLong(1));
        this.name = cursor.getString(2);
        this.businessType = cursor.isNull(3) ? null : Integer.valueOf(cursor.getInt(3));
        this.level = cursor.isNull(4) ? null : Integer.valueOf(cursor.getInt(4));
        this.description = cursor.getString(5);
        this.parentId = !cursor.isNull(6) ? Long.valueOf(cursor.getLong(6)) : l;
    }

    public Business(Parcel parcel) {
        super(parcel);
        Long l = null;
        if (parcel.readByte() == 0) {
            this.id = null;
            parcel.readLong();
        } else {
            this.id = Long.valueOf(parcel.readLong());
        }
        this.name = parcel.readByte() == 0 ? null : parcel.readString();
        this.businessType = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.level = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.description = parcel.readByte() == 0 ? null : parcel.readString();
        this.parentId = parcel.readByte() != 0 ? Long.valueOf(parcel.readLong()) : l;
    }

    private Business(Long l, String str, Integer num, Integer num2, String str2, Long l2) {
        this.id = l;
        this.name = str;
        this.businessType = num;
        this.level = num2;
        this.description = str2;
        this.parentId = l2;
    }

    public Business() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long l) {
        this.id = l;
        setValue();
    }

    public String getName() {
        return this.name;
    }

    public void setName(String str) {
        this.name = str;
        setValue();
    }

    public Integer getBusinessType() {
        return this.businessType;
    }

    public void setBusinessType(Integer num) {
        this.businessType = num;
        setValue();
    }

    public Integer getLevel() {
        return this.level;
    }

    public void setLevel(Integer num) {
        this.level = num;
        setValue();
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String str) {
        this.description = str;
        setValue();
    }

    public Long getParentId() {
        return this.parentId;
    }

    public void setParentId(Long l) {
        this.parentId = l;
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
        if (this.name != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.name);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.businessType != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.businessType.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.level != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.level.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.description != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.description);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.parentId != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.parentId.longValue());
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<Business> getHelper() {
        return BusinessHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "Business { id: " + this.id + ", name: " + this.name + ", businessType: " + this.businessType + ", level: " + this.level + ", description: " + this.description + ", parentId: " + this.parentId + " }";
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
