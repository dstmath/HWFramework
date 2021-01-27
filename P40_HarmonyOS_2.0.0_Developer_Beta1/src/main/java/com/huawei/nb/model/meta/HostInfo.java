package com.huawei.nb.model.meta;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class HostInfo extends AManagedObject {
    public static final Parcelable.Creator<HostInfo> CREATOR = new Parcelable.Creator<HostInfo>() {
        /* class com.huawei.nb.model.meta.HostInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public HostInfo createFromParcel(Parcel parcel) {
            return new HostInfo(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public HostInfo[] newArray(int i) {
            return new HostInfo[i];
        }
    };
    private String dbName;
    private Integer id;
    private String recordName;
    private String tableName;

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
        return "com.huawei.nb.model.meta.HostInfo";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public HostInfo(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.id = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.dbName = cursor.getString(2);
        this.tableName = cursor.getString(3);
        this.recordName = cursor.getString(4);
    }

    public HostInfo(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.id = null;
            parcel.readInt();
        } else {
            this.id = Integer.valueOf(parcel.readInt());
        }
        this.dbName = parcel.readByte() == 0 ? null : parcel.readString();
        this.tableName = parcel.readByte() == 0 ? null : parcel.readString();
        this.recordName = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private HostInfo(Integer num, String str, String str2, String str3) {
        this.id = num;
        this.dbName = str;
        this.tableName = str2;
        this.recordName = str3;
    }

    public HostInfo() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer num) {
        this.id = num;
        setValue();
    }

    public String getDbName() {
        return this.dbName;
    }

    public void setDbName(String str) {
        this.dbName = str;
        setValue();
    }

    public String getTableName() {
        return this.tableName;
    }

    public void setTableName(String str) {
        this.tableName = str;
        setValue();
    }

    public String getRecordName() {
        return this.recordName;
    }

    public void setRecordName(String str) {
        this.recordName = str;
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
        if (this.dbName != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.dbName);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.tableName != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.tableName);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.recordName != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.recordName);
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<HostInfo> getHelper() {
        return HostInfoHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "HostInfo { id: " + this.id + ", dbName: " + this.dbName + ", tableName: " + this.tableName + ", recordName: " + this.recordName + " }";
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
