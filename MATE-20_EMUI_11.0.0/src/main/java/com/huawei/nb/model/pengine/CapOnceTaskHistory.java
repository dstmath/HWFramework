package com.huawei.nb.model.pengine;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class CapOnceTaskHistory extends AManagedObject {
    public static final Parcelable.Creator<CapOnceTaskHistory> CREATOR = new Parcelable.Creator<CapOnceTaskHistory>() {
        /* class com.huawei.nb.model.pengine.CapOnceTaskHistory.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public CapOnceTaskHistory createFromParcel(Parcel parcel) {
            return new CapOnceTaskHistory(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public CapOnceTaskHistory[] newArray(int i) {
            return new CapOnceTaskHistory[i];
        }
    };
    private Long executeTime;
    private Integer hisId;
    private String taskName;
    private String taskType;
    private Integer version;

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
        return "com.huawei.nb.model.pengine.CapOnceTaskHistory";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public CapOnceTaskHistory(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Long l = null;
        this.hisId = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.taskName = cursor.getString(2);
        this.version = cursor.isNull(3) ? null : Integer.valueOf(cursor.getInt(3));
        this.taskType = cursor.getString(4);
        this.executeTime = !cursor.isNull(5) ? Long.valueOf(cursor.getLong(5)) : l;
    }

    public CapOnceTaskHistory(Parcel parcel) {
        super(parcel);
        Long l = null;
        if (parcel.readByte() == 0) {
            this.hisId = null;
            parcel.readInt();
        } else {
            this.hisId = Integer.valueOf(parcel.readInt());
        }
        this.taskName = parcel.readByte() == 0 ? null : parcel.readString();
        this.version = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.taskType = parcel.readByte() == 0 ? null : parcel.readString();
        this.executeTime = parcel.readByte() != 0 ? Long.valueOf(parcel.readLong()) : l;
    }

    private CapOnceTaskHistory(Integer num, String str, Integer num2, String str2, Long l) {
        this.hisId = num;
        this.taskName = str;
        this.version = num2;
        this.taskType = str2;
        this.executeTime = l;
    }

    public CapOnceTaskHistory() {
    }

    public Integer getHisId() {
        return this.hisId;
    }

    public void setHisId(Integer num) {
        this.hisId = num;
        setValue();
    }

    public String getTaskName() {
        return this.taskName;
    }

    public void setTaskName(String str) {
        this.taskName = str;
        setValue();
    }

    public Integer getVersion() {
        return this.version;
    }

    public void setVersion(Integer num) {
        this.version = num;
        setValue();
    }

    public String getTaskType() {
        return this.taskType;
    }

    public void setTaskType(String str) {
        this.taskType = str;
        setValue();
    }

    public Long getExecuteTime() {
        return this.executeTime;
    }

    public void setExecuteTime(Long l) {
        this.executeTime = l;
        setValue();
    }

    @Override // com.huawei.odmf.core.AManagedObject, android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        if (this.hisId != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.hisId.intValue());
        } else {
            parcel.writeByte((byte) 0);
            parcel.writeInt(1);
        }
        if (this.taskName != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.taskName);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.version != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.version.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.taskType != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.taskType);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.executeTime != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.executeTime.longValue());
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<CapOnceTaskHistory> getHelper() {
        return CapOnceTaskHistoryHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "CapOnceTaskHistory { hisId: " + this.hisId + ", taskName: " + this.taskName + ", version: " + this.version + ", taskType: " + this.taskType + ", executeTime: " + this.executeTime + " }";
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
