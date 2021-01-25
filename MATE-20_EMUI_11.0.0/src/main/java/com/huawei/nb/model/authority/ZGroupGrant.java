package com.huawei.nb.model.authority;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class ZGroupGrant extends AManagedObject {
    public static final Parcelable.Creator<ZGroupGrant> CREATOR = new Parcelable.Creator<ZGroupGrant>() {
        /* class com.huawei.nb.model.authority.ZGroupGrant.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ZGroupGrant createFromParcel(Parcel parcel) {
            return new ZGroupGrant(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public ZGroupGrant[] newArray(int i) {
            return new ZGroupGrant[i];
        }
    };
    private Integer authority;
    private String groupName;
    private Long id;
    private boolean isGroupIdentifier;
    private String member;
    private String owner;
    private String reserved;
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
        return "com.huawei.nb.model.authority.ZGroupGrant";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public ZGroupGrant(Cursor cursor) {
        boolean z = false;
        setRowId(Long.valueOf(cursor.getLong(0)));
        Integer num = null;
        this.id = cursor.isNull(1) ? null : Long.valueOf(cursor.getLong(1));
        this.groupName = cursor.getString(2);
        this.tableName = cursor.getString(3);
        this.owner = cursor.getString(4);
        this.isGroupIdentifier = cursor.getInt(5) != 0 ? true : z;
        this.member = cursor.getString(6);
        this.authority = !cursor.isNull(7) ? Integer.valueOf(cursor.getInt(7)) : num;
        this.reserved = cursor.getString(8);
    }

    public ZGroupGrant(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.id = null;
            parcel.readLong();
        } else {
            this.id = Long.valueOf(parcel.readLong());
        }
        this.groupName = parcel.readByte() == 0 ? null : parcel.readString();
        this.tableName = parcel.readByte() == 0 ? null : parcel.readString();
        this.owner = parcel.readByte() == 0 ? null : parcel.readString();
        this.isGroupIdentifier = parcel.readByte() != 0;
        this.member = parcel.readByte() == 0 ? null : parcel.readString();
        this.authority = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.reserved = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private ZGroupGrant(Long l, String str, String str2, String str3, boolean z, String str4, Integer num, String str5) {
        this.id = l;
        this.groupName = str;
        this.tableName = str2;
        this.owner = str3;
        this.isGroupIdentifier = z;
        this.member = str4;
        this.authority = num;
        this.reserved = str5;
    }

    public ZGroupGrant() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long l) {
        this.id = l;
        setValue();
    }

    public String getGroupName() {
        return this.groupName;
    }

    public void setGroupName(String str) {
        this.groupName = str;
        setValue();
    }

    public String getTableName() {
        return this.tableName;
    }

    public void setTableName(String str) {
        this.tableName = str;
        setValue();
    }

    public String getOwner() {
        return this.owner;
    }

    public void setOwner(String str) {
        this.owner = str;
        setValue();
    }

    public boolean getIsGroupIdentifier() {
        return this.isGroupIdentifier;
    }

    public void setIsGroupIdentifier(boolean z) {
        this.isGroupIdentifier = z;
        setValue();
    }

    public String getMember() {
        return this.member;
    }

    public void setMember(String str) {
        this.member = str;
        setValue();
    }

    public Integer getAuthority() {
        return this.authority;
    }

    public void setAuthority(Integer num) {
        this.authority = num;
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
        if (this.groupName != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.groupName);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.tableName != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.tableName);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.owner != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.owner);
        } else {
            parcel.writeByte((byte) 0);
        }
        parcel.writeByte(this.isGroupIdentifier ? (byte) 1 : 0);
        if (this.member != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.member);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.authority != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.authority.intValue());
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
    public AEntityHelper<ZGroupGrant> getHelper() {
        return ZGroupGrantHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "ZGroupGrant { id: " + this.id + ", groupName: " + this.groupName + ", tableName: " + this.tableName + ", owner: " + this.owner + ", isGroupIdentifier: " + this.isGroupIdentifier + ", member: " + this.member + ", authority: " + this.authority + ", reserved: " + this.reserved + " }";
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
