package com.huawei.nb.model.meta;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class UserInfo extends AManagedObject {
    public static final Parcelable.Creator<UserInfo> CREATOR = new Parcelable.Creator<UserInfo>() {
        /* class com.huawei.nb.model.meta.UserInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public UserInfo createFromParcel(Parcel parcel) {
            return new UserInfo(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public UserInfo[] newArray(int i) {
            return new UserInfo[i];
        }
    };
    private Integer id;
    private Integer reservedInt1;
    private Integer reservedInt2;
    private String reservedStr1;
    private String reservedStr2;
    private Integer userId;
    private Long userSn;

    @Override // com.huawei.odmf.core.AManagedObject, android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getDatabaseName() {
        return "dsMeta";
    }

    public String getDatabaseVersion() {
        return "0.0.16";
    }

    public int getDatabaseVersionCode() {
        return 16;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getEntityName() {
        return "com.huawei.nb.model.meta.UserInfo";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public UserInfo(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Integer num = null;
        this.id = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.userId = cursor.isNull(2) ? null : Integer.valueOf(cursor.getInt(2));
        this.userSn = cursor.isNull(3) ? null : Long.valueOf(cursor.getLong(3));
        this.reservedInt1 = cursor.isNull(4) ? null : Integer.valueOf(cursor.getInt(4));
        this.reservedInt2 = !cursor.isNull(5) ? Integer.valueOf(cursor.getInt(5)) : num;
        this.reservedStr1 = cursor.getString(6);
        this.reservedStr2 = cursor.getString(7);
    }

    public UserInfo(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.id = null;
            parcel.readInt();
        } else {
            this.id = Integer.valueOf(parcel.readInt());
        }
        this.userId = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.userSn = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.reservedInt1 = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.reservedInt2 = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.reservedStr1 = parcel.readByte() == 0 ? null : parcel.readString();
        this.reservedStr2 = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private UserInfo(Integer num, Integer num2, Long l, Integer num3, Integer num4, String str, String str2) {
        this.id = num;
        this.userId = num2;
        this.userSn = l;
        this.reservedInt1 = num3;
        this.reservedInt2 = num4;
        this.reservedStr1 = str;
        this.reservedStr2 = str2;
    }

    public UserInfo() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer num) {
        this.id = num;
        setValue();
    }

    public Integer getUserId() {
        return this.userId;
    }

    public void setUserId(Integer num) {
        this.userId = num;
        setValue();
    }

    public Long getUserSn() {
        return this.userSn;
    }

    public void setUserSn(Long l) {
        this.userSn = l;
        setValue();
    }

    public Integer getReservedInt1() {
        return this.reservedInt1;
    }

    public void setReservedInt1(Integer num) {
        this.reservedInt1 = num;
        setValue();
    }

    public Integer getReservedInt2() {
        return this.reservedInt2;
    }

    public void setReservedInt2(Integer num) {
        this.reservedInt2 = num;
        setValue();
    }

    public String getReservedStr1() {
        return this.reservedStr1;
    }

    public void setReservedStr1(String str) {
        this.reservedStr1 = str;
        setValue();
    }

    public String getReservedStr2() {
        return this.reservedStr2;
    }

    public void setReservedStr2(String str) {
        this.reservedStr2 = str;
        setValue();
    }

    @Override // com.huawei.odmf.core.AManagedObject, android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        if (this.id != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.id.intValue());
        } else {
            parcel.writeByte((byte) 0);
            parcel.writeInt(1);
        }
        if (this.userId != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.userId.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.userSn != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.userSn.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.reservedInt1 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.reservedInt1.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.reservedInt2 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.reservedInt2.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.reservedStr1 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.reservedStr1);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.reservedStr2 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.reservedStr2);
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<UserInfo> getHelper() {
        return UserInfoHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "UserInfo { id: " + this.id + ", userId: " + this.userId + ", userSn: " + this.userSn + ", reservedInt1: " + this.reservedInt1 + ", reservedInt2: " + this.reservedInt2 + ", reservedStr1: " + this.reservedStr1 + ", reservedStr2: " + this.reservedStr2 + " }";
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
