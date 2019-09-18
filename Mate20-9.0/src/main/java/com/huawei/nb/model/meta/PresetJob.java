package com.huawei.nb.model.meta;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class PresetJob extends AManagedObject {
    public static final Parcelable.Creator<PresetJob> CREATOR = new Parcelable.Creator<PresetJob>() {
        public PresetJob createFromParcel(Parcel in) {
            return new PresetJob(in);
        }

        public PresetJob[] newArray(int size) {
            return new PresetJob[size];
        }
    };
    private String jobInfo;
    private String jobName;
    private Integer mId;
    private String parameter;
    private Integer scheduleType;
    private Integer taskType;

    public PresetJob(Cursor cursor) {
        Integer num = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.mId = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.jobName = cursor.getString(2);
        this.parameter = cursor.getString(3);
        this.scheduleType = cursor.isNull(4) ? null : Integer.valueOf(cursor.getInt(4));
        this.taskType = !cursor.isNull(5) ? Integer.valueOf(cursor.getInt(5)) : num;
        this.jobInfo = cursor.getString(6);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public PresetJob(Parcel in) {
        super(in);
        String str = null;
        if (in.readByte() == 0) {
            this.mId = null;
            in.readInt();
        } else {
            this.mId = Integer.valueOf(in.readInt());
        }
        this.jobName = in.readByte() == 0 ? null : in.readString();
        this.parameter = in.readByte() == 0 ? null : in.readString();
        this.scheduleType = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.taskType = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.jobInfo = in.readByte() != 0 ? in.readString() : str;
    }

    private PresetJob(Integer mId2, String jobName2, String parameter2, Integer scheduleType2, Integer taskType2, String jobInfo2) {
        this.mId = mId2;
        this.jobName = jobName2;
        this.parameter = parameter2;
        this.scheduleType = scheduleType2;
        this.taskType = taskType2;
        this.jobInfo = jobInfo2;
    }

    public PresetJob() {
    }

    public int describeContents() {
        return 0;
    }

    public Integer getMId() {
        return this.mId;
    }

    public void setMId(Integer mId2) {
        this.mId = mId2;
        setValue();
    }

    public String getJobName() {
        return this.jobName;
    }

    public void setJobName(String jobName2) {
        this.jobName = jobName2;
        setValue();
    }

    public String getParameter() {
        return this.parameter;
    }

    public void setParameter(String parameter2) {
        this.parameter = parameter2;
        setValue();
    }

    public Integer getScheduleType() {
        return this.scheduleType;
    }

    public void setScheduleType(Integer scheduleType2) {
        this.scheduleType = scheduleType2;
        setValue();
    }

    public Integer getTaskType() {
        return this.taskType;
    }

    public void setTaskType(Integer taskType2) {
        this.taskType = taskType2;
        setValue();
    }

    public String getJobInfo() {
        return this.jobInfo;
    }

    public void setJobInfo(String jobInfo2) {
        this.jobInfo = jobInfo2;
        setValue();
    }

    public void writeToParcel(Parcel out, int ignored) {
        super.writeToParcel(out, ignored);
        if (this.mId != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mId.intValue());
        } else {
            out.writeByte((byte) 0);
            out.writeInt(1);
        }
        if (this.jobName != null) {
            out.writeByte((byte) 1);
            out.writeString(this.jobName);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.parameter != null) {
            out.writeByte((byte) 1);
            out.writeString(this.parameter);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.scheduleType != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.scheduleType.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.taskType != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.taskType.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.jobInfo != null) {
            out.writeByte((byte) 1);
            out.writeString(this.jobInfo);
            return;
        }
        out.writeByte((byte) 0);
    }

    public AEntityHelper<PresetJob> getHelper() {
        return PresetJobHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.meta.PresetJob";
    }

    public String getDatabaseName() {
        return "dsMeta";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("PresetJob { mId: ").append(this.mId);
        sb.append(", jobName: ").append(this.jobName);
        sb.append(", parameter: ").append(this.parameter);
        sb.append(", scheduleType: ").append(this.scheduleType);
        sb.append(", taskType: ").append(this.taskType);
        sb.append(", jobInfo: ").append(this.jobInfo);
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
