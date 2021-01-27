package com.huawei.nb.model.authority;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class ZTableAuthorityGrant extends AManagedObject {
    public static final Parcelable.Creator<ZTableAuthorityGrant> CREATOR = new Parcelable.Creator<ZTableAuthorityGrant>() {
        /* class com.huawei.nb.model.authority.ZTableAuthorityGrant.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ZTableAuthorityGrant createFromParcel(Parcel parcel) {
            return new ZTableAuthorityGrant(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public ZTableAuthorityGrant[] newArray(int i) {
            return new ZTableAuthorityGrant[i];
        }
    };
    private Integer authority;
    private Long id;
    private String packageName;
    private Long packageUid;
    private String reserved;
    private Boolean supportGroupAuthority = true;
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
        return "com.huawei.nb.model.authority.ZTableAuthorityGrant";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public ZTableAuthorityGrant(Cursor cursor) {
        boolean z = true;
        setRowId(Long.valueOf(cursor.getLong(0)));
        Boolean bool = null;
        this.id = cursor.isNull(1) ? null : Long.valueOf(cursor.getLong(1));
        this.tableId = cursor.isNull(2) ? null : Long.valueOf(cursor.getLong(2));
        this.tableName = cursor.getString(3);
        this.packageUid = cursor.isNull(4) ? null : Long.valueOf(cursor.getLong(4));
        this.packageName = cursor.getString(5);
        this.authority = cursor.isNull(6) ? null : Integer.valueOf(cursor.getInt(6));
        if (!cursor.isNull(7)) {
            bool = Boolean.valueOf(cursor.getInt(7) == 0 ? false : z);
        }
        this.supportGroupAuthority = bool;
        this.reserved = cursor.getString(8);
    }

    public ZTableAuthorityGrant(Parcel parcel) {
        super(parcel);
        Boolean bool;
        boolean z = true;
        String str = null;
        if (parcel.readByte() == 0) {
            this.id = null;
            parcel.readLong();
        } else {
            this.id = Long.valueOf(parcel.readLong());
        }
        this.tableId = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.tableName = parcel.readByte() == 0 ? null : parcel.readString();
        this.packageUid = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.packageName = parcel.readByte() == 0 ? null : parcel.readString();
        this.authority = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        if (parcel.readByte() == 0) {
            bool = null;
        } else {
            bool = Boolean.valueOf(parcel.readByte() == 0 ? false : z);
        }
        this.supportGroupAuthority = bool;
        this.reserved = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private ZTableAuthorityGrant(Long l, Long l2, String str, Long l3, String str2, Integer num, Boolean bool, String str3) {
        this.id = l;
        this.tableId = l2;
        this.tableName = str;
        this.packageUid = l3;
        this.packageName = str2;
        this.authority = num;
        this.supportGroupAuthority = bool;
        this.reserved = str3;
    }

    public ZTableAuthorityGrant() {
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

    public Long getPackageUid() {
        return this.packageUid;
    }

    public void setPackageUid(Long l) {
        this.packageUid = l;
        setValue();
    }

    public String getPackageName() {
        return this.packageName;
    }

    public void setPackageName(String str) {
        this.packageName = str;
        setValue();
    }

    public Integer getAuthority() {
        return this.authority;
    }

    public void setAuthority(Integer num) {
        this.authority = num;
        setValue();
    }

    public Boolean getSupportGroupAuthority() {
        return this.supportGroupAuthority;
    }

    public void setSupportGroupAuthority(Boolean bool) {
        this.supportGroupAuthority = bool;
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
        if (this.packageUid != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.packageUid.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.packageName != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.packageName);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.authority != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.authority.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.supportGroupAuthority != null) {
            parcel.writeByte((byte) 1);
            parcel.writeByte(this.supportGroupAuthority.booleanValue() ? (byte) 1 : 0);
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
    public AEntityHelper<ZTableAuthorityGrant> getHelper() {
        return ZTableAuthorityGrantHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "ZTableAuthorityGrant { id: " + this.id + ", tableId: " + this.tableId + ", tableName: " + this.tableName + ", packageUid: " + this.packageUid + ", packageName: " + this.packageName + ", authority: " + this.authority + ", supportGroupAuthority: " + this.supportGroupAuthority + ", reserved: " + this.reserved + " }";
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
