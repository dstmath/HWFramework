package com.huawei.nb.model.kv;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class JsonKv_system extends AManagedObject {
    public static final Parcelable.Creator<JsonKv_system> CREATOR = new Parcelable.Creator<JsonKv_system>() {
        public JsonKv_system createFromParcel(Parcel in) {
            return new JsonKv_system(in);
        }

        public JsonKv_system[] newArray(int size) {
            return new JsonKv_system[size];
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

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public JsonKv_system(Parcel in) {
        super(in);
        String str = null;
        if (in.readByte() == 0) {
            this.id = null;
            in.readLong();
        } else {
            this.id = Long.valueOf(in.readLong());
        }
        this.primaryKey = in.readByte() == 0 ? null : in.readString();
        this.value = in.readByte() == 0 ? null : in.readString();
        this.version = in.readByte() == 0 ? null : in.readString();
        this.owner = in.readByte() == 0 ? null : in.readString();
        this.tag = in.readByte() == 0 ? null : in.readString();
        this.clone = in.readInt();
        this.reserved1 = in.readByte() == 0 ? null : in.readString();
        this.reserved2 = in.readByte() == 0 ? null : in.readString();
        this.reserved3 = in.readByte() != 0 ? in.readString() : str;
        this.clearStatus = in.readInt();
    }

    private JsonKv_system(Long id2, String primaryKey2, String value2, String version2, String owner2, String tag2, int clone2, String reserved12, String reserved22, String reserved32, int clearStatus2) {
        this.id = id2;
        this.primaryKey = primaryKey2;
        this.value = value2;
        this.version = version2;
        this.owner = owner2;
        this.tag = tag2;
        this.clone = clone2;
        this.reserved1 = reserved12;
        this.reserved2 = reserved22;
        this.reserved3 = reserved32;
        this.clearStatus = clearStatus2;
    }

    public JsonKv_system() {
    }

    public int describeContents() {
        return 0;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id2) {
        this.id = id2;
        setValue();
    }

    public String getPrimaryKey() {
        return this.primaryKey;
    }

    public void setPrimaryKey(String primaryKey2) {
        this.primaryKey = primaryKey2;
        setValue();
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value2) {
        this.value = value2;
        setValue();
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version2) {
        this.version = version2;
        setValue();
    }

    public String getOwner() {
        return this.owner;
    }

    public void setOwner(String owner2) {
        this.owner = owner2;
        setValue();
    }

    public String getTag() {
        return this.tag;
    }

    public void setTag(String tag2) {
        this.tag = tag2;
        setValue();
    }

    public int getClone() {
        return this.clone;
    }

    public void setClone(int clone2) {
        this.clone = clone2;
        setValue();
    }

    public String getReserved1() {
        return this.reserved1;
    }

    public void setReserved1(String reserved12) {
        this.reserved1 = reserved12;
        setValue();
    }

    public String getReserved2() {
        return this.reserved2;
    }

    public void setReserved2(String reserved22) {
        this.reserved2 = reserved22;
        setValue();
    }

    public String getReserved3() {
        return this.reserved3;
    }

    public void setReserved3(String reserved32) {
        this.reserved3 = reserved32;
        setValue();
    }

    public int getClearStatus() {
        return this.clearStatus;
    }

    public void setClearStatus(int clearStatus2) {
        this.clearStatus = clearStatus2;
        setValue();
    }

    public void writeToParcel(Parcel out, int ignored) {
        super.writeToParcel(out, ignored);
        if (this.id != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.id.longValue());
        } else {
            out.writeByte((byte) 0);
            out.writeLong(1);
        }
        if (this.primaryKey != null) {
            out.writeByte((byte) 1);
            out.writeString(this.primaryKey);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.value != null) {
            out.writeByte((byte) 1);
            out.writeString(this.value);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.version != null) {
            out.writeByte((byte) 1);
            out.writeString(this.version);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.owner != null) {
            out.writeByte((byte) 1);
            out.writeString(this.owner);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.tag != null) {
            out.writeByte((byte) 1);
            out.writeString(this.tag);
        } else {
            out.writeByte((byte) 0);
        }
        out.writeInt(this.clone);
        if (this.reserved1 != null) {
            out.writeByte((byte) 1);
            out.writeString(this.reserved1);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.reserved2 != null) {
            out.writeByte((byte) 1);
            out.writeString(this.reserved2);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.reserved3 != null) {
            out.writeByte((byte) 1);
            out.writeString(this.reserved3);
        } else {
            out.writeByte((byte) 0);
        }
        out.writeInt(this.clearStatus);
    }

    public AEntityHelper<JsonKv_system> getHelper() {
        return JsonKv_systemHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.kv.JsonKv_system";
    }

    public String getDatabaseName() {
        return "dsKvData";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("JsonKv_system { id: ").append(this.id);
        sb.append(", primaryKey: ").append(this.primaryKey);
        sb.append(", value: ").append(this.value);
        sb.append(", version: ").append(this.version);
        sb.append(", owner: ").append(this.owner);
        sb.append(", tag: ").append(this.tag);
        sb.append(", clone: ").append(this.clone);
        sb.append(", reserved1: ").append(this.reserved1);
        sb.append(", reserved2: ").append(this.reserved2);
        sb.append(", reserved3: ").append(this.reserved3);
        sb.append(", clearStatus: ").append(this.clearStatus);
        sb.append(" }");
        return sb.toString();
    }

    public boolean equals(Object o) {
        return super.equals(o);
    }

    public int hashCode() {
        return super.hashCode();
    }

    public String getDatabaseVersion() {
        return "0.0.3";
    }

    public int getDatabaseVersionCode() {
        return 3;
    }

    public String getEntityVersion() {
        return "0.0.2";
    }

    public int getEntityVersionCode() {
        return 2;
    }
}
