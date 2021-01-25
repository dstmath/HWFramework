package com.huawei.nb.model.authority;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class ZDatabaseAuthority extends AManagedObject {
    public static final Parcelable.Creator<ZDatabaseAuthority> CREATOR = new Parcelable.Creator<ZDatabaseAuthority>() {
        /* class com.huawei.nb.model.authority.ZDatabaseAuthority.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ZDatabaseAuthority createFromParcel(Parcel parcel) {
            return new ZDatabaseAuthority(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public ZDatabaseAuthority[] newArray(int i) {
            return new ZDatabaseAuthority[i];
        }
    };
    private Integer authority;
    private Long dbId;
    private String dbName;
    private Long id;
    private String packageName;
    private Long packageUid;
    private String reserved;
    private Boolean supportGroupAuthority = true;

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
        return "com.huawei.nb.model.authority.ZDatabaseAuthority";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public ZDatabaseAuthority(Cursor cursor) {
        boolean z = true;
        setRowId(Long.valueOf(cursor.getLong(0)));
        Boolean bool = null;
        this.id = cursor.isNull(1) ? null : Long.valueOf(cursor.getLong(1));
        this.dbId = cursor.isNull(2) ? null : Long.valueOf(cursor.getLong(2));
        this.dbName = cursor.getString(3);
        this.packageUid = cursor.isNull(4) ? null : Long.valueOf(cursor.getLong(4));
        this.packageName = cursor.getString(5);
        this.authority = cursor.isNull(6) ? null : Integer.valueOf(cursor.getInt(6));
        if (!cursor.isNull(7)) {
            bool = Boolean.valueOf(cursor.getInt(7) == 0 ? false : z);
        }
        this.supportGroupAuthority = bool;
        this.reserved = cursor.getString(8);
    }

    public ZDatabaseAuthority(Parcel parcel) {
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
        this.dbId = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.dbName = parcel.readByte() == 0 ? null : parcel.readString();
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

    private ZDatabaseAuthority(Long l, Long l2, String str, Long l3, String str2, Integer num, Boolean bool, String str3) {
        this.id = l;
        this.dbId = l2;
        this.dbName = str;
        this.packageUid = l3;
        this.packageName = str2;
        this.authority = num;
        this.supportGroupAuthority = bool;
        this.reserved = str3;
    }

    public ZDatabaseAuthority() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long l) {
        this.id = l;
        setValue();
    }

    public Long getDbId() {
        return this.dbId;
    }

    public void setDbId(Long l) {
        this.dbId = l;
        setValue();
    }

    public String getDbName() {
        return this.dbName;
    }

    public void setDbName(String str) {
        this.dbName = str;
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
        if (this.dbId != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.dbId.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.dbName != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.dbName);
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
    public AEntityHelper<ZDatabaseAuthority> getHelper() {
        return ZDatabaseAuthorityHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "ZDatabaseAuthority { id: " + this.id + ", dbId: " + this.dbId + ", dbName: " + this.dbName + ", packageUid: " + this.packageUid + ", packageName: " + this.packageName + ", authority: " + this.authority + ", supportGroupAuthority: " + this.supportGroupAuthority + ", reserved: " + this.reserved + " }";
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
