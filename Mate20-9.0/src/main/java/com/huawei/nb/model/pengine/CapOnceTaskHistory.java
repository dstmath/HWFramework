package com.huawei.nb.model.pengine;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class CapOnceTaskHistory extends AManagedObject {
    public static final Parcelable.Creator<CapOnceTaskHistory> CREATOR = new Parcelable.Creator<CapOnceTaskHistory>() {
        public CapOnceTaskHistory createFromParcel(Parcel in) {
            return new CapOnceTaskHistory(in);
        }

        public CapOnceTaskHistory[] newArray(int size) {
            return new CapOnceTaskHistory[size];
        }
    };
    private Long executeTime;
    private Integer hisId;
    private String taskName;
    private String taskType;
    private Integer version;

    public CapOnceTaskHistory(Cursor cursor) {
        Long l = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.hisId = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.taskName = cursor.getString(2);
        this.version = cursor.isNull(3) ? null : Integer.valueOf(cursor.getInt(3));
        this.taskType = cursor.getString(4);
        this.executeTime = !cursor.isNull(5) ? Long.valueOf(cursor.getLong(5)) : l;
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public CapOnceTaskHistory(Parcel in) {
        super(in);
        Long l = null;
        if (in.readByte() == 0) {
            this.hisId = null;
            in.readInt();
        } else {
            this.hisId = Integer.valueOf(in.readInt());
        }
        this.taskName = in.readByte() == 0 ? null : in.readString();
        this.version = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.taskType = in.readByte() == 0 ? null : in.readString();
        this.executeTime = in.readByte() != 0 ? Long.valueOf(in.readLong()) : l;
    }

    private CapOnceTaskHistory(Integer hisId2, String taskName2, Integer version2, String taskType2, Long executeTime2) {
        this.hisId = hisId2;
        this.taskName = taskName2;
        this.version = version2;
        this.taskType = taskType2;
        this.executeTime = executeTime2;
    }

    public CapOnceTaskHistory() {
    }

    public int describeContents() {
        return 0;
    }

    public Integer getHisId() {
        return this.hisId;
    }

    public void setHisId(Integer hisId2) {
        this.hisId = hisId2;
        setValue();
    }

    public String getTaskName() {
        return this.taskName;
    }

    public void setTaskName(String taskName2) {
        this.taskName = taskName2;
        setValue();
    }

    public Integer getVersion() {
        return this.version;
    }

    public void setVersion(Integer version2) {
        this.version = version2;
        setValue();
    }

    public String getTaskType() {
        return this.taskType;
    }

    public void setTaskType(String taskType2) {
        this.taskType = taskType2;
        setValue();
    }

    public Long getExecuteTime() {
        return this.executeTime;
    }

    public void setExecuteTime(Long executeTime2) {
        this.executeTime = executeTime2;
        setValue();
    }

    public void writeToParcel(Parcel out, int ignored) {
        super.writeToParcel(out, ignored);
        if (this.hisId != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.hisId.intValue());
        } else {
            out.writeByte((byte) 0);
            out.writeInt(1);
        }
        if (this.taskName != null) {
            out.writeByte((byte) 1);
            out.writeString(this.taskName);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.version != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.version.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.taskType != null) {
            out.writeByte((byte) 1);
            out.writeString(this.taskType);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.executeTime != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.executeTime.longValue());
            return;
        }
        out.writeByte((byte) 0);
    }

    public AEntityHelper<CapOnceTaskHistory> getHelper() {
        return CapOnceTaskHistoryHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.pengine.CapOnceTaskHistory";
    }

    public String getDatabaseName() {
        return "dsPengineData";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("CapOnceTaskHistory { hisId: ").append(this.hisId);
        sb.append(", taskName: ").append(this.taskName);
        sb.append(", version: ").append(this.version);
        sb.append(", taskType: ").append(this.taskType);
        sb.append(", executeTime: ").append(this.executeTime);
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
        return "0.0.8";
    }

    public int getDatabaseVersionCode() {
        return 8;
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }
}
