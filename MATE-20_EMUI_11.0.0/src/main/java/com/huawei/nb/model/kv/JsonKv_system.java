package com.huawei.nb.model.kv;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class JsonKv_system extends AManagedObject {
    public static final Parcelable.Creator<JsonKv_system> CREATOR = new Parcelable.Creator<JsonKv_system>() {
        /* class com.huawei.nb.model.kv.JsonKv_system.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public JsonKv_system createFromParcel(Parcel parcel) {
            return new JsonKv_system(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public JsonKv_system[] newArray(int i) {
            return new JsonKv_system[i];
        }
    };
    private int clearStatus = 0;
    private int clone = 0;
    private Long id;
    private String owner;
    private String primaryKey;
    private String reserved1;
    private String reserved2;
    private String reserved3;
    private String tag;
    private String value;
    private String version;

    @Override // com.huawei.odmf.core.AManagedObject, android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getDatabaseName() {
        return "dsKvData";
    }

    public String getDatabaseVersion() {
        return "0.0.3";
    }

    public int getDatabaseVersionCode() {
        return 3;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getEntityName() {
        return "com.huawei.nb.model.kv.JsonKv_system";
    }

    public String getEntityVersion() {
        return "0.0.2";
    }

    public int getEntityVersionCode() {
        return 2;
    }

    public JsonKv_system(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.id = cursor.isNull(1) ? null : Long.valueOf(cursor.getLong(1));
        this.primaryKey = cursor.getString(2);
        this.value = cursor.getString(3);
        this.version = cursor.getString(4);
        this.owner = cursor.getString(5);
        this.tag = cursor.getString(6);
        this.clone = cursor.getInt(7);
        this.reserved1 = cursor.getString(8);
        this.reserved2 = cursor.getString(9);
        this.reserved3 = cursor.getString(10);
        this.clearStatus = cursor.getInt(11);
    }

    public JsonKv_system(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.id = null;
            parcel.readLong();
        } else {
            this.id = Long.valueOf(parcel.readLong());
        }
        this.primaryKey = parcel.readByte() == 0 ? null : parcel.readString();
        this.value = parcel.readByte() == 0 ? null : parcel.readString();
        this.version = parcel.readByte() == 0 ? null : parcel.readString();
        this.owner = parcel.readByte() == 0 ? null : parcel.readString();
        this.tag = parcel.readByte() == 0 ? null : parcel.readString();
        this.clone = parcel.readInt();
        this.reserved1 = parcel.readByte() == 0 ? null : parcel.readString();
        this.reserved2 = parcel.readByte() == 0 ? null : parcel.readString();
        this.reserved3 = parcel.readByte() != 0 ? parcel.readString() : str;
        this.clearStatus = parcel.readInt();
    }

    private JsonKv_system(Long l, String str, String str2, String str3, String str4, String str5, int i, String str6, String str7, String str8, int i2) {
        this.id = l;
        this.primaryKey = str;
        this.value = str2;
        this.version = str3;
        this.owner = str4;
        this.tag = str5;
        this.clone = i;
        this.reserved1 = str6;
        this.reserved2 = str7;
        this.reserved3 = str8;
        this.clearStatus = i2;
    }

    public JsonKv_system() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long l) {
        this.id = l;
        setValue();
    }

    public String getPrimaryKey() {
        return this.primaryKey;
    }

    public void setPrimaryKey(String str) {
        this.primaryKey = str;
        setValue();
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String str) {
        this.value = str;
        setValue();
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String str) {
        this.version = str;
        setValue();
    }

    public String getOwner() {
        return this.owner;
    }

    public void setOwner(String str) {
        this.owner = str;
        setValue();
    }

    public String getTag() {
        return this.tag;
    }

    public void setTag(String str) {
        this.tag = str;
        setValue();
    }

    public int getClone() {
        return this.clone;
    }

    public void setClone(int i) {
        this.clone = i;
        setValue();
    }

    public String getReserved1() {
        return this.reserved1;
    }

    public void setReserved1(String str) {
        this.reserved1 = str;
        setValue();
    }

    public String getReserved2() {
        return this.reserved2;
    }

    public void setReserved2(String str) {
        this.reserved2 = str;
        setValue();
    }

    public String getReserved3() {
        return this.reserved3;
    }

    public void setReserved3(String str) {
        this.reserved3 = str;
        setValue();
    }

    public int getClearStatus() {
        return this.clearStatus;
    }

    public void setClearStatus(int i) {
        this.clearStatus = i;
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
        if (this.primaryKey != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.primaryKey);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.value != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.value);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.version != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.version);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.owner != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.owner);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.tag != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.tag);
        } else {
            parcel.writeByte((byte) 0);
        }
        parcel.writeInt(this.clone);
        if (this.reserved1 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.reserved1);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.reserved2 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.reserved2);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.reserved3 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.reserved3);
        } else {
            parcel.writeByte((byte) 0);
        }
        parcel.writeInt(this.clearStatus);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<JsonKv_system> getHelper() {
        return JsonKv_systemHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "JsonKv_system { id: " + this.id + ", primaryKey: " + this.primaryKey + ", value: " + this.value + ", version: " + this.version + ", owner: " + this.owner + ", tag: " + this.tag + ", clone: " + this.clone + ", reserved1: " + this.reserved1 + ", reserved2: " + this.reserved2 + ", reserved3: " + this.reserved3 + ", clearStatus: " + this.clearStatus + " }";
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
