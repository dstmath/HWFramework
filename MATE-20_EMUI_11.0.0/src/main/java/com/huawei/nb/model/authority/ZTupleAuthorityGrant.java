package com.huawei.nb.model.authority;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class ZTupleAuthorityGrant extends AManagedObject {
    public static final Parcelable.Creator<ZTupleAuthorityGrant> CREATOR = new Parcelable.Creator<ZTupleAuthorityGrant>() {
        /* class com.huawei.nb.model.authority.ZTupleAuthorityGrant.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ZTupleAuthorityGrant createFromParcel(Parcel parcel) {
            return new ZTupleAuthorityGrant(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public ZTupleAuthorityGrant[] newArray(int i) {
            return new ZTupleAuthorityGrant[i];
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
    private Long tupleId;
    private String tupleName;

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
        return "com.huawei.nb.model.authority.ZTupleAuthorityGrant";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public ZTupleAuthorityGrant(Cursor cursor) {
        boolean z = true;
        setRowId(Long.valueOf(cursor.getLong(0)));
        Boolean bool = null;
        this.id = cursor.isNull(1) ? null : Long.valueOf(cursor.getLong(1));
        this.tableId = cursor.isNull(2) ? null : Long.valueOf(cursor.getLong(2));
        this.tableName = cursor.getString(3);
        this.tupleId = cursor.isNull(4) ? null : Long.valueOf(cursor.getLong(4));
        this.tupleName = cursor.getString(5);
        this.packageUid = cursor.isNull(6) ? null : Long.valueOf(cursor.getLong(6));
        this.packageName = cursor.getString(7);
        this.authority = cursor.isNull(8) ? null : Integer.valueOf(cursor.getInt(8));
        if (!cursor.isNull(9)) {
            bool = Boolean.valueOf(cursor.getInt(9) == 0 ? false : z);
        }
        this.supportGroupAuthority = bool;
        this.reserved = cursor.getString(10);
    }

    public ZTupleAuthorityGrant(Parcel parcel) {
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
        this.tupleId = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.tupleName = parcel.readByte() == 0 ? null : parcel.readString();
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

    private ZTupleAuthorityGrant(Long l, Long l2, String str, Long l3, String str2, Long l4, String str3, Integer num, Boolean bool, String str4) {
        this.id = l;
        this.tableId = l2;
        this.tableName = str;
        this.tupleId = l3;
        this.tupleName = str2;
        this.packageUid = l4;
        this.packageName = str3;
        this.authority = num;
        this.supportGroupAuthority = bool;
        this.reserved = str4;
    }

    public ZTupleAuthorityGrant() {
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

    public Long getTupleId() {
        return this.tupleId;
    }

    public void setTupleId(Long l) {
        this.tupleId = l;
        setValue();
    }

    public String getTupleName() {
        return this.tupleName;
    }

    public void setTupleName(String str) {
        this.tupleName = str;
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
        if (this.tupleId != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.tupleId.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.tupleName != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.tupleName);
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
    public AEntityHelper<ZTupleAuthorityGrant> getHelper() {
        return ZTupleAuthorityGrantHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "ZTupleAuthorityGrant { id: " + this.id + ", tableId: " + this.tableId + ", tableName: " + this.tableName + ", tupleId: " + this.tupleId + ", tupleName: " + this.tupleName + ", packageUid: " + this.packageUid + ", packageName: " + this.packageName + ", authority: " + this.authority + ", supportGroupAuthority: " + this.supportGroupAuthority + ", reserved: " + this.reserved + " }";
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
