package com.huawei.nb.model;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class ResourceSync extends AManagedObject {
    public static final Parcelable.Creator<ResourceSync> CREATOR = new Parcelable.Creator<ResourceSync>() {
        public ResourceSync createFromParcel(Parcel in) {
            return new ResourceSync(in);
        }

        public ResourceSync[] newArray(int size) {
            return new ResourceSync[size];
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

    public ResourceSync(Cursor cursor) {
        Integer num = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
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

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public ResourceSync(Parcel in) {
        super(in);
        Integer num = null;
        if (in.readByte() == 0) {
            this.id = null;
            in.readInt();
        } else {
            this.id = Integer.valueOf(in.readInt());
        }
        this.tableName = in.readByte() == 0 ? null : in.readString();
        this.dbName = in.readByte() == 0 ? null : in.readString();
        this.syncMode = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.syncTime = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.syncPoint = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.remoteUrl = in.readByte() == 0 ? null : in.readString();
        this.dataType = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.isAllowOverWrite = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.networkMode = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.syncField = in.readByte() == 0 ? null : in.readString();
        this.startTime = in.readByte() == 0 ? null : in.readString();
        this.electricity = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.tilingTime = in.readByte() != 0 ? Integer.valueOf(in.readInt()) : num;
    }

    private ResourceSync(Integer id2, String tableName2, String dbName2, Long syncMode2, Long syncTime2, Long syncPoint2, String remoteUrl2, Long dataType2, Long isAllowOverWrite2, Long networkMode2, String syncField2, String startTime2, Integer electricity2, Integer tilingTime2) {
        this.id = id2;
        this.tableName = tableName2;
        this.dbName = dbName2;
        this.syncMode = syncMode2;
        this.syncTime = syncTime2;
        this.syncPoint = syncPoint2;
        this.remoteUrl = remoteUrl2;
        this.dataType = dataType2;
        this.isAllowOverWrite = isAllowOverWrite2;
        this.networkMode = networkMode2;
        this.syncField = syncField2;
        this.startTime = startTime2;
        this.electricity = electricity2;
        this.tilingTime = tilingTime2;
    }

    public ResourceSync() {
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

    public String getTableName() {
        return this.tableName;
    }

    public void setTableName(String tableName2) {
        this.tableName = tableName2;
        setValue();
    }

    public String getDbName() {
        return this.dbName;
    }

    public void setDbName(String dbName2) {
        this.dbName = dbName2;
        setValue();
    }

    public Long getSyncMode() {
        return this.syncMode;
    }

    public void setSyncMode(Long syncMode2) {
        this.syncMode = syncMode2;
        setValue();
    }

    public Long getSyncTime() {
        return this.syncTime;
    }

    public void setSyncTime(Long syncTime2) {
        this.syncTime = syncTime2;
        setValue();
    }

    public Long getSyncPoint() {
        return this.syncPoint;
    }

    public void setSyncPoint(Long syncPoint2) {
        this.syncPoint = syncPoint2;
        setValue();
    }

    public String getRemoteUrl() {
        return this.remoteUrl;
    }

    public void setRemoteUrl(String remoteUrl2) {
        this.remoteUrl = remoteUrl2;
        setValue();
    }

    public Long getDataType() {
        return this.dataType;
    }

    public void setDataType(Long dataType2) {
        this.dataType = dataType2;
        setValue();
    }

    public Long getIsAllowOverWrite() {
        return this.isAllowOverWrite;
    }

    public void setIsAllowOverWrite(Long isAllowOverWrite2) {
        this.isAllowOverWrite = isAllowOverWrite2;
        setValue();
    }

    public Long getNetworkMode() {
        return this.networkMode;
    }

    public void setNetworkMode(Long networkMode2) {
        this.networkMode = networkMode2;
        setValue();
    }

    public String getSyncField() {
        return this.syncField;
    }

    public void setSyncField(String syncField2) {
        this.syncField = syncField2;
        setValue();
    }

    public String getStartTime() {
        return this.startTime;
    }

    public void setStartTime(String startTime2) {
        this.startTime = startTime2;
        setValue();
    }

    public Integer getElectricity() {
        return this.electricity;
    }

    public void setElectricity(Integer electricity2) {
        this.electricity = electricity2;
        setValue();
    }

    public Integer getTilingTime() {
        return this.tilingTime;
    }

    public void setTilingTime(Integer tilingTime2) {
        this.tilingTime = tilingTime2;
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
        if (this.tableName != null) {
            out.writeByte((byte) 1);
            out.writeString(this.tableName);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.dbName != null) {
            out.writeByte((byte) 1);
            out.writeString(this.dbName);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.syncMode != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.syncMode.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.syncTime != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.syncTime.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.syncPoint != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.syncPoint.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.remoteUrl != null) {
            out.writeByte((byte) 1);
            out.writeString(this.remoteUrl);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.dataType != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.dataType.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.isAllowOverWrite != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.isAllowOverWrite.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.networkMode != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.networkMode.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.syncField != null) {
            out.writeByte((byte) 1);
            out.writeString(this.syncField);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.startTime != null) {
            out.writeByte((byte) 1);
            out.writeString(this.startTime);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.electricity != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.electricity.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.tilingTime != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.tilingTime.intValue());
            return;
        }
        out.writeByte((byte) 0);
    }

    public AEntityHelper<ResourceSync> getHelper() {
        return ResourceSyncHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.ResourceSync";
    }

    public String getDatabaseName() {
        return "dsMeta";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("ResourceSync { id: ").append(this.id);
        sb.append(", tableName: ").append(this.tableName);
        sb.append(", dbName: ").append(this.dbName);
        sb.append(", syncMode: ").append(this.syncMode);
        sb.append(", syncTime: ").append(this.syncTime);
        sb.append(", syncPoint: ").append(this.syncPoint);
        sb.append(", remoteUrl: ").append(this.remoteUrl);
        sb.append(", dataType: ").append(this.dataType);
        sb.append(", isAllowOverWrite: ").append(this.isAllowOverWrite);
        sb.append(", networkMode: ").append(this.networkMode);
        sb.append(", syncField: ").append(this.syncField);
        sb.append(", startTime: ").append(this.startTime);
        sb.append(", electricity: ").append(this.electricity);
        sb.append(", tilingTime: ").append(this.tilingTime);
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
