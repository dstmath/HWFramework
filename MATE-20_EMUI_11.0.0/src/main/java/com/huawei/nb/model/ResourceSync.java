package com.huawei.nb.model;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class ResourceSync extends AManagedObject {
    public static final Parcelable.Creator<ResourceSync> CREATOR = new Parcelable.Creator<ResourceSync>() {
        /* class com.huawei.nb.model.ResourceSync.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ResourceSync createFromParcel(Parcel parcel) {
            return new ResourceSync(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public ResourceSync[] newArray(int i) {
            return new ResourceSync[i];
        }
    };
    private Long dataType;
    private String dbName;
    private Integer electricity;
    private Integer id;
    private Long isAllowOverWrite;
    private Long networkMode;
    private String remoteUrl;
    private String startTime;
    private String syncField;
    private Long syncMode;
    private Long syncPoint;
    private Long syncTime;
    private String tableName;
    private Integer tilingTime;

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
        return "com.huawei.nb.model.ResourceSync";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public ResourceSync(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Integer num = null;
        this.id = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.tableName = cursor.getString(2);
        this.dbName = cursor.getString(3);
        this.syncMode = cursor.isNull(4) ? null : Long.valueOf(cursor.getLong(4));
        this.syncTime = cursor.isNull(5) ? null : Long.valueOf(cursor.getLong(5));
        this.syncPoint = cursor.isNull(6) ? null : Long.valueOf(cursor.getLong(6));
        this.remoteUrl = cursor.getString(7);
        this.dataType = cursor.isNull(8) ? null : Long.valueOf(cursor.getLong(8));
        this.isAllowOverWrite = cursor.isNull(9) ? null : Long.valueOf(cursor.getLong(9));
        this.networkMode = cursor.isNull(10) ? null : Long.valueOf(cursor.getLong(10));
        this.syncField = cursor.getString(11);
        this.startTime = cursor.getString(12);
        this.electricity = cursor.isNull(13) ? null : Integer.valueOf(cursor.getInt(13));
        this.tilingTime = !cursor.isNull(14) ? Integer.valueOf(cursor.getInt(14)) : num;
    }

    public ResourceSync(Parcel parcel) {
        super(parcel);
        Integer num = null;
        if (parcel.readByte() == 0) {
            this.id = null;
            parcel.readInt();
        } else {
            this.id = Integer.valueOf(parcel.readInt());
        }
        this.tableName = parcel.readByte() == 0 ? null : parcel.readString();
        this.dbName = parcel.readByte() == 0 ? null : parcel.readString();
        this.syncMode = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.syncTime = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.syncPoint = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.remoteUrl = parcel.readByte() == 0 ? null : parcel.readString();
        this.dataType = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.isAllowOverWrite = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.networkMode = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.syncField = parcel.readByte() == 0 ? null : parcel.readString();
        this.startTime = parcel.readByte() == 0 ? null : parcel.readString();
        this.electricity = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.tilingTime = parcel.readByte() != 0 ? Integer.valueOf(parcel.readInt()) : num;
    }

    private ResourceSync(Integer num, String str, String str2, Long l, Long l2, Long l3, String str3, Long l4, Long l5, Long l6, String str4, String str5, Integer num2, Integer num3) {
        this.id = num;
        this.tableName = str;
        this.dbName = str2;
        this.syncMode = l;
        this.syncTime = l2;
        this.syncPoint = l3;
        this.remoteUrl = str3;
        this.dataType = l4;
        this.isAllowOverWrite = l5;
        this.networkMode = l6;
        this.syncField = str4;
        this.startTime = str5;
        this.electricity = num2;
        this.tilingTime = num3;
    }

    public ResourceSync() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer num) {
        this.id = num;
        setValue();
    }

    public String getTableName() {
        return this.tableName;
    }

    public void setTableName(String str) {
        this.tableName = str;
        setValue();
    }

    public String getDbName() {
        return this.dbName;
    }

    public void setDbName(String str) {
        this.dbName = str;
        setValue();
    }

    public Long getSyncMode() {
        return this.syncMode;
    }

    public void setSyncMode(Long l) {
        this.syncMode = l;
        setValue();
    }

    public Long getSyncTime() {
        return this.syncTime;
    }

    public void setSyncTime(Long l) {
        this.syncTime = l;
        setValue();
    }

    public Long getSyncPoint() {
        return this.syncPoint;
    }

    public void setSyncPoint(Long l) {
        this.syncPoint = l;
        setValue();
    }

    public String getRemoteUrl() {
        return this.remoteUrl;
    }

    public void setRemoteUrl(String str) {
        this.remoteUrl = str;
        setValue();
    }

    public Long getDataType() {
        return this.dataType;
    }

    public void setDataType(Long l) {
        this.dataType = l;
        setValue();
    }

    public Long getIsAllowOverWrite() {
        return this.isAllowOverWrite;
    }

    public void setIsAllowOverWrite(Long l) {
        this.isAllowOverWrite = l;
        setValue();
    }

    public Long getNetworkMode() {
        return this.networkMode;
    }

    public void setNetworkMode(Long l) {
        this.networkMode = l;
        setValue();
    }

    public String getSyncField() {
        return this.syncField;
    }

    public void setSyncField(String str) {
        this.syncField = str;
        setValue();
    }

    public String getStartTime() {
        return this.startTime;
    }

    public void setStartTime(String str) {
        this.startTime = str;
        setValue();
    }

    public Integer getElectricity() {
        return this.electricity;
    }

    public void setElectricity(Integer num) {
        this.electricity = num;
        setValue();
    }

    public Integer getTilingTime() {
        return this.tilingTime;
    }

    public void setTilingTime(Integer num) {
        this.tilingTime = num;
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
        if (this.tableName != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.tableName);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.dbName != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.dbName);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.syncMode != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.syncMode.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.syncTime != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.syncTime.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.syncPoint != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.syncPoint.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.remoteUrl != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.remoteUrl);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.dataType != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.dataType.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.isAllowOverWrite != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.isAllowOverWrite.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.networkMode != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.networkMode.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.syncField != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.syncField);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.startTime != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.startTime);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.electricity != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.electricity.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.tilingTime != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.tilingTime.intValue());
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<ResourceSync> getHelper() {
        return ResourceSyncHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "ResourceSync { id: " + this.id + ", tableName: " + this.tableName + ", dbName: " + this.dbName + ", syncMode: " + this.syncMode + ", syncTime: " + this.syncTime + ", syncPoint: " + this.syncPoint + ", remoteUrl: " + this.remoteUrl + ", dataType: " + this.dataType + ", isAllowOverWrite: " + this.isAllowOverWrite + ", networkMode: " + this.networkMode + ", syncField: " + this.syncField + ", startTime: " + this.startTime + ", electricity: " + this.electricity + ", tilingTime: " + this.tilingTime + " }";
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
