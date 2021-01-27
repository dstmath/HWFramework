package com.huawei.nb.model.authority;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class ZTableInfo extends AManagedObject {
    public static final Parcelable.Creator<ZTableInfo> CREATOR = new Parcelable.Creator<ZTableInfo>() {
        /* class com.huawei.nb.model.authority.ZTableInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ZTableInfo createFromParcel(Parcel parcel) {
            return new ZTableInfo(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public ZTableInfo[] newArray(int i) {
            return new ZTableInfo[i];
        }
    };
    private Integer authorityLevel;
    private Integer authorityValue;
    private Long id;
    private String reserved;
    private String tableDesc;
    private Long tableId;
    private String tableName;

    @Override // com.huawei.odmf.core.AManagedObject, android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getDatabaseName() {
        return "dsWeather";
    }

    public String getDatabaseVersion() {
        return "0.0.17";
    }

    public int getDatabaseVersionCode() {
        return 17;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getEntityName() {
        return "com.huawei.nb.model.authority.ZTableInfo";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public ZTableInfo(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Integer num = null;
        this.id = cursor.isNull(1) ? null : Long.valueOf(cursor.getLong(1));
        this.tableId = cursor.isNull(2) ? null : Long.valueOf(cursor.getLong(2));
        this.tableName = cursor.getString(3);
        this.tableDesc = cursor.getString(4);
        this.authorityLevel = cursor.isNull(5) ? null : Integer.valueOf(cursor.getInt(5));
        this.authorityValue = !cursor.isNull(6) ? Integer.valueOf(cursor.getInt(6)) : num;
        this.reserved = cursor.getString(7);
    }

    public ZTableInfo(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.id = null;
            parcel.readLong();
        } else {
            this.id = Long.valueOf(parcel.readLong());
        }
        this.tableId = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.tableName = parcel.readByte() == 0 ? null : parcel.readString();
        this.tableDesc = parcel.readByte() == 0 ? null : parcel.readString();
        this.authorityLevel = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.authorityValue = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.reserved = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private ZTableInfo(Long l, Long l2, String str, String str2, Integer num, Integer num2, String str3) {
        this.id = l;
        this.tableId = l2;
        this.tableName = str;
        this.tableDesc = str2;
        this.authorityLevel = num;
        this.authorityValue = num2;
        this.reserved = str3;
    }

    public ZTableInfo() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long l) {
        this.id = l;
        setValue();
    }

    public Long getTableId() {
        return this.tableId;
    }

    public void setTableId(Long l) {
        this.tableId = l;
        setValue();
    }

    public String getTableName() {
        return this.tableName;
    }

    public void setTableName(String str) {
        this.tableName = str;
        setValue();
    }

    public String getTableDesc() {
        return this.tableDesc;
    }

    public void setTableDesc(String str) {
        this.tableDesc = str;
        setValue();
    }

    public Integer getAuthorityLevel() {
        return this.authorityLevel;
    }

    public void setAuthorityLevel(Integer num) {
        this.authorityLevel = num;
        setValue();
    }

    public Integer getAuthorityValue() {
        return this.authorityValue;
    }

    public void setAuthorityValue(Integer num) {
        this.authorityValue = num;
        setValue();
    }

    public String getReserved() {
        return this.reserved;
    }

    public void setReserved(String str) {
        this.reserved = str;
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
        if (this.tableId != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.tableId.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.tableName != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.tableName);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.tableDesc != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.tableDesc);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.authorityLevel != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.authorityLevel.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.authorityValue != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.authorityValue.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.reserved != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.reserved);
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<ZTableInfo> getHelper() {
        return ZTableInfoHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "ZTableInfo { id: " + this.id + ", tableId: " + this.tableId + ", tableName: " + this.tableName + ", tableDesc: " + this.tableDesc + ", authorityLevel: " + this.authorityLevel + ", authorityValue: " + this.authorityValue + ", reserved: " + this.reserved + " }";
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
