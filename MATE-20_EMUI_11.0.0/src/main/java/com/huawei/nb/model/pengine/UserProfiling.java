package com.huawei.nb.model.pengine;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class UserProfiling extends AManagedObject {
    public static final Parcelable.Creator<UserProfiling> CREATOR = new Parcelable.Creator<UserProfiling>() {
        /* class com.huawei.nb.model.pengine.UserProfiling.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public UserProfiling createFromParcel(Parcel parcel) {
            return new UserProfiling(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public UserProfiling[] newArray(int i) {
            return new UserProfiling[i];
        }
    };
    private Integer id;
    private Integer level;
    private String parent;
    private Long timestamp;
    private String uriKey;
    private String uriValue;

    @Override // com.huawei.odmf.core.AManagedObject, android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getDatabaseName() {
        return "dsPengineData";
    }

    public String getDatabaseVersion() {
        return "0.0.11";
    }

    public int getDatabaseVersionCode() {
        return 11;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getEntityName() {
        return "com.huawei.nb.model.pengine.UserProfiling";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public UserProfiling(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Long l = null;
        this.id = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.parent = cursor.getString(2);
        this.level = cursor.isNull(3) ? null : Integer.valueOf(cursor.getInt(3));
        this.uriKey = cursor.getString(4);
        this.uriValue = cursor.getString(5);
        this.timestamp = !cursor.isNull(6) ? Long.valueOf(cursor.getLong(6)) : l;
    }

    public UserProfiling(Parcel parcel) {
        super(parcel);
        Long l = null;
        if (parcel.readByte() == 0) {
            this.id = null;
            parcel.readInt();
        } else {
            this.id = Integer.valueOf(parcel.readInt());
        }
        this.parent = parcel.readByte() == 0 ? null : parcel.readString();
        this.level = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.uriKey = parcel.readByte() == 0 ? null : parcel.readString();
        this.uriValue = parcel.readByte() == 0 ? null : parcel.readString();
        this.timestamp = parcel.readByte() != 0 ? Long.valueOf(parcel.readLong()) : l;
    }

    private UserProfiling(Integer num, String str, Integer num2, String str2, String str3, Long l) {
        this.id = num;
        this.parent = str;
        this.level = num2;
        this.uriKey = str2;
        this.uriValue = str3;
        this.timestamp = l;
    }

    public UserProfiling() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer num) {
        this.id = num;
        setValue();
    }

    public String getParent() {
        return this.parent;
    }

    public void setParent(String str) {
        this.parent = str;
        setValue();
    }

    public Integer getLevel() {
        return this.level;
    }

    public void setLevel(Integer num) {
        this.level = num;
        setValue();
    }

    public String getUriKey() {
        return this.uriKey;
    }

    public void setUriKey(String str) {
        this.uriKey = str;
        setValue();
    }

    public String getUriValue() {
        return this.uriValue;
    }

    public void setUriValue(String str) {
        this.uriValue = str;
        setValue();
    }

    public Long getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(Long l) {
        this.timestamp = l;
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
        if (this.parent != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.parent);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.level != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.level.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.uriKey != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.uriKey);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.uriValue != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.uriValue);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.timestamp != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.timestamp.longValue());
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<UserProfiling> getHelper() {
        return UserProfilingHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "UserProfiling { id: " + this.id + ", parent: " + this.parent + ", level: " + this.level + ", uriKey: " + this.uriKey + ", uriValue: " + this.uriValue + ", timestamp: " + this.timestamp + " }";
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
