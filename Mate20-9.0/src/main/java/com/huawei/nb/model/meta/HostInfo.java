package com.huawei.nb.model.meta;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class HostInfo extends AManagedObject {
    public static final Parcelable.Creator<HostInfo> CREATOR = new Parcelable.Creator<HostInfo>() {
        public HostInfo createFromParcel(Parcel in) {
            return new HostInfo(in);
        }

        public HostInfo[] newArray(int size) {
            return new HostInfo[size];
        }
    };
    private String dbName;
    private Integer id;
    private String recordName;
    private String tableName;

    public HostInfo(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.id = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.dbName = cursor.getString(2);
        this.tableName = cursor.getString(3);
        this.recordName = cursor.getString(4);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public HostInfo(Parcel in) {
        super(in);
        String str = null;
        if (in.readByte() == 0) {
            this.id = null;
            in.readInt();
        } else {
            this.id = Integer.valueOf(in.readInt());
        }
        this.dbName = in.readByte() == 0 ? null : in.readString();
        this.tableName = in.readByte() == 0 ? null : in.readString();
        this.recordName = in.readByte() != 0 ? in.readString() : str;
    }

    private HostInfo(Integer id2, String dbName2, String tableName2, String recordName2) {
        this.id = id2;
        this.dbName = dbName2;
        this.tableName = tableName2;
        this.recordName = recordName2;
    }

    public HostInfo() {
    }

    public int describeContents() {
        return 0;
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id2) {
        this.id = id2;
        setValue();
    }

    public String getDbName() {
        return this.dbName;
    }

    public void setDbName(String dbName2) {
        this.dbName = dbName2;
        setValue();
    }

    public String getTableName() {
        return this.tableName;
    }

    public void setTableName(String tableName2) {
        this.tableName = tableName2;
        setValue();
    }

    public String getRecordName() {
        return this.recordName;
    }

    public void setRecordName(String recordName2) {
        this.recordName = recordName2;
        setValue();
    }

    public void writeToParcel(Parcel out, int ignored) {
        super.writeToParcel(out, ignored);
        if (this.id != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.id.intValue());
        } else {
            out.writeByte((byte) 0);
            out.writeInt(1);
        }
        if (this.dbName != null) {
            out.writeByte((byte) 1);
            out.writeString(this.dbName);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.tableName != null) {
            out.writeByte((byte) 1);
            out.writeString(this.tableName);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.recordName != null) {
            out.writeByte((byte) 1);
            out.writeString(this.recordName);
            return;
        }
        out.writeByte((byte) 0);
    }

    public AEntityHelper<HostInfo> getHelper() {
        return HostInfoHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.meta.HostInfo";
    }

    public String getDatabaseName() {
        return "dsMeta";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("HostInfo { id: ").append(this.id);
        sb.append(", dbName: ").append(this.dbName);
        sb.append(", tableName: ").append(this.tableName);
        sb.append(", recordName: ").append(this.recordName);
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
        return "0.0.16";
    }

    public int getDatabaseVersionCode() {
        return 16;
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }
}
