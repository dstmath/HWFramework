package com.huawei.nb.model.pengine;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class UpTaskInstance extends AManagedObject {
    public static final Parcelable.Creator<UpTaskInstance> CREATOR = new Parcelable.Creator<UpTaskInstance>() {
        public UpTaskInstance createFromParcel(Parcel in) {
            return new UpTaskInstance(in);
        }

        public UpTaskInstance[] newArray(int size) {
            return new UpTaskInstance[size];
        }
    };
    private String attrs;
    private Long createTime;
    private Long jobId;
    private Long lastModifyTime;
    private String result;
    private String resultDesc;
    private String status;
    private Integer taskId;
    private String taskName;

    public UpTaskInstance(Cursor cursor) {
        Long l = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.taskId = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.jobId = cursor.isNull(2) ? null : Long.valueOf(cursor.getLong(2));
        this.taskName = cursor.getString(3);
        this.status = cursor.getString(4);
        this.result = cursor.getString(5);
        this.resultDesc = cursor.getString(6);
        this.createTime = cursor.isNull(7) ? null : Long.valueOf(cursor.getLong(7));
        this.lastModifyTime = !cursor.isNull(8) ? Long.valueOf(cursor.getLong(8)) : l;
        this.attrs = cursor.getString(9);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public UpTaskInstance(Parcel in) {
        super(in);
        String str = null;
        if (in.readByte() == 0) {
            this.taskId = null;
            in.readInt();
        } else {
            this.taskId = Integer.valueOf(in.readInt());
        }
        this.jobId = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.taskName = in.readByte() == 0 ? null : in.readString();
        this.status = in.readByte() == 0 ? null : in.readString();
        this.result = in.readByte() == 0 ? null : in.readString();
        this.resultDesc = in.readByte() == 0 ? null : in.readString();
        this.createTime = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.lastModifyTime = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.attrs = in.readByte() != 0 ? in.readString() : str;
    }

    private UpTaskInstance(Integer taskId2, Long jobId2, String taskName2, String status2, String result2, String resultDesc2, Long createTime2, Long lastModifyTime2, String attrs2) {
        this.taskId = taskId2;
        this.jobId = jobId2;
        this.taskName = taskName2;
        this.status = status2;
        this.result = result2;
        this.resultDesc = resultDesc2;
        this.createTime = createTime2;
        this.lastModifyTime = lastModifyTime2;
        this.attrs = attrs2;
    }

    public UpTaskInstance() {
    }

    public int describeContents() {
        return 0;
    }

    public Integer getTaskId() {
        return this.taskId;
    }

    public void setTaskId(Integer taskId2) {
        this.taskId = taskId2;
        setValue();
    }

    public Long getJobId() {
        return this.jobId;
    }

    public void setJobId(Long jobId2) {
        this.jobId = jobId2;
        setValue();
    }

    public String getTaskName() {
        return this.taskName;
    }

    public void setTaskName(String taskName2) {
        this.taskName = taskName2;
        setValue();
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status2) {
        this.status = status2;
        setValue();
    }

    public String getResult() {
        return this.result;
    }

    public void setResult(String result2) {
        this.result = result2;
        setValue();
    }

    public String getResultDesc() {
        return this.resultDesc;
    }

    public void setResultDesc(String resultDesc2) {
        this.resultDesc = resultDesc2;
        setValue();
    }

    public Long getCreateTime() {
        return this.createTime;
    }

    public void setCreateTime(Long createTime2) {
        this.createTime = createTime2;
        setValue();
    }

    public Long getLastModifyTime() {
        return this.lastModifyTime;
    }

    public void setLastModifyTime(Long lastModifyTime2) {
        this.lastModifyTime = lastModifyTime2;
        setValue();
    }

    public String getAttrs() {
        return this.attrs;
    }

    public void setAttrs(String attrs2) {
        this.attrs = attrs2;
        setValue();
    }

    public void writeToParcel(Parcel out, int ignored) {
        super.writeToParcel(out, ignored);
        if (this.taskId != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.taskId.intValue());
        } else {
            out.writeByte((byte) 0);
            out.writeInt(1);
        }
        if (this.jobId != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.jobId.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.taskName != null) {
            out.writeByte((byte) 1);
            out.writeString(this.taskName);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.status != null) {
            out.writeByte((byte) 1);
            out.writeString(this.status);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.result != null) {
            out.writeByte((byte) 1);
            out.writeString(this.result);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.resultDesc != null) {
            out.writeByte((byte) 1);
            out.writeString(this.resultDesc);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.createTime != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.createTime.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.lastModifyTime != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.lastModifyTime.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.attrs != null) {
            out.writeByte((byte) 1);
            out.writeString(this.attrs);
            return;
        }
        out.writeByte((byte) 0);
    }

    public AEntityHelper<UpTaskInstance> getHelper() {
        return UpTaskInstanceHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.pengine.UpTaskInstance";
    }

    public String getDatabaseName() {
        return "dsPengineData";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("UpTaskInstance { taskId: ").append(this.taskId);
        sb.append(", jobId: ").append(this.jobId);
        sb.append(", taskName: ").append(this.taskName);
        sb.append(", status: ").append(this.status);
        sb.append(", result: ").append(this.result);
        sb.append(", resultDesc: ").append(this.resultDesc);
        sb.append(", createTime: ").append(this.createTime);
        sb.append(", lastModifyTime: ").append(this.lastModifyTime);
        sb.append(", attrs: ").append(this.attrs);
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
