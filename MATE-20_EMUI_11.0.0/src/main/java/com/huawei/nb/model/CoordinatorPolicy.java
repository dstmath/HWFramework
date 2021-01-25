package com.huawei.nb.model;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class CoordinatorPolicy extends AManagedObject {
    public static final Parcelable.Creator<CoordinatorPolicy> CREATOR = new Parcelable.Creator<CoordinatorPolicy>() {
        /* class com.huawei.nb.model.CoordinatorPolicy.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public CoordinatorPolicy createFromParcel(Parcel parcel) {
            return new CoordinatorPolicy(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public CoordinatorPolicy[] newArray(int i) {
            return new CoordinatorPolicy[i];
        }
    };
    private Long dataTrafficSyncTime;
    private Long dataType;
    private String dbName;
    private Integer electricity;
    private Long isAllowOverWrite;
    private Long networkMode;
    private Integer policyNo;
    private String remoteUrl;
    private String startTime;
    private String syncField;
    private Long syncMode;
    private Integer syncPeriod;
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
        return "com.huawei.nb.model.CoordinatorPolicy";
    }

    public String getEntityVersion() {
        return "0.0.2";
    }

    public int getEntityVersionCode() {
        return 2;
    }

    public CoordinatorPolicy(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Integer num = null;
        this.policyNo = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
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
        this.tilingTime = cursor.isNull(14) ? null : Integer.valueOf(cursor.getInt(14));
        this.dataTrafficSyncTime = cursor.isNull(15) ? null : Long.valueOf(cursor.getLong(15));
        this.syncPeriod = !cursor.isNull(16) ? Integer.valueOf(cursor.getInt(16)) : num;
    }

    public CoordinatorPolicy(Parcel parcel) {
        super(parcel);
        Integer num = null;
        if (parcel.readByte() == 0) {
            this.policyNo = null;
            parcel.readInt();
        } else {
            this.policyNo = Integer.valueOf(parcel.readInt());
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
        this.tilingTime = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.dataTrafficSyncTime = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.syncPeriod = parcel.readByte() != 0 ? Integer.valueOf(parcel.readInt()) : num;
    }

    private CoordinatorPolicy(Integer num, String str, String str2, Long l, Long l2, Long l3, String str3, Long l4, Long l5, Long l6, String str4, String str5, Integer num2, Integer num3, Long l7, Integer num4) {
        this.policyNo = num;
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
        this.dataTrafficSyncTime = l7;
        this.syncPeriod = num4;
    }

    public CoordinatorPolicy() {
    }

    public Integer getPolicyNo() {
        return this.policyNo;
    }

    public void setPolicyNo(Integer num) {
        this.policyNo = num;
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

    public Long getDataTrafficSyncTime() {
        return this.dataTrafficSyncTime;
    }

    public void setDataTrafficSyncTime(Long l) {
        this.dataTrafficSyncTime = l;
        setValue();
    }

    public Integer getSyncPeriod() {
        return this.syncPeriod;
    }

    public void setSyncPeriod(Integer num) {
        this.syncPeriod = num;
        setValue();
    }

    @Override // com.huawei.odmf.core.AManagedObject, android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        if (this.policyNo != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.policyNo.intValue());
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
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.dataTrafficSyncTime != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.dataTrafficSyncTime.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.syncPeriod != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.syncPeriod.intValue());
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<CoordinatorPolicy> getHelper() {
        return CoordinatorPolicyHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "CoordinatorPolicy { policyNo: " + this.policyNo + ", tableName: " + this.tableName + ", dbName: " + this.dbName + ", syncMode: " + this.syncMode + ", syncTime: " + this.syncTime + ", syncPoint: " + this.syncPoint + ", remoteUrl: " + this.remoteUrl + ", dataType: " + this.dataType + ", isAllowOverWrite: " + this.isAllowOverWrite + ", networkMode: " + this.networkMode + ", syncField: " + this.syncField + ", startTime: " + this.startTime + ", electricity: " + this.electricity + ", tilingTime: " + this.tilingTime + ", dataTrafficSyncTime: " + this.dataTrafficSyncTime + ", syncPeriod: " + this.syncPeriod + " }";
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
