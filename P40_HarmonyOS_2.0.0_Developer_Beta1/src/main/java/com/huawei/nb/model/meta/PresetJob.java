package com.huawei.nb.model.meta;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class PresetJob extends AManagedObject {
    public static final Parcelable.Creator<PresetJob> CREATOR = new Parcelable.Creator<PresetJob>() {
        /* class com.huawei.nb.model.meta.PresetJob.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public PresetJob createFromParcel(Parcel parcel) {
            return new PresetJob(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public PresetJob[] newArray(int i) {
            return new PresetJob[i];
        }
    };
    private String jobInfo;
    private String jobName;
    private Integer mId;
    private String parameter;
    private Integer scheduleType;
    private Integer taskType;

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
        return "com.huawei.nb.model.meta.PresetJob";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public PresetJob(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Integer num = null;
        this.mId = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.jobName = cursor.getString(2);
        this.parameter = cursor.getString(3);
        this.scheduleType = cursor.isNull(4) ? null : Integer.valueOf(cursor.getInt(4));
        this.taskType = !cursor.isNull(5) ? Integer.valueOf(cursor.getInt(5)) : num;
        this.jobInfo = cursor.getString(6);
    }

    public PresetJob(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.mId = null;
            parcel.readInt();
        } else {
            this.mId = Integer.valueOf(parcel.readInt());
        }
        this.jobName = parcel.readByte() == 0 ? null : parcel.readString();
        this.parameter = parcel.readByte() == 0 ? null : parcel.readString();
        this.scheduleType = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.taskType = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.jobInfo = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private PresetJob(Integer num, String str, String str2, Integer num2, Integer num3, String str3) {
        this.mId = num;
        this.jobName = str;
        this.parameter = str2;
        this.scheduleType = num2;
        this.taskType = num3;
        this.jobInfo = str3;
    }

    public PresetJob() {
    }

    public Integer getMId() {
        return this.mId;
    }

    public void setMId(Integer num) {
        this.mId = num;
        setValue();
    }

    public String getJobName() {
        return this.jobName;
    }

    public void setJobName(String str) {
        this.jobName = str;
        setValue();
    }

    public String getParameter() {
        return this.parameter;
    }

    public void setParameter(String str) {
        this.parameter = str;
        setValue();
    }

    public Integer getScheduleType() {
        return this.scheduleType;
    }

    public void setScheduleType(Integer num) {
        this.scheduleType = num;
        setValue();
    }

    public Integer getTaskType() {
        return this.taskType;
    }

    public void setTaskType(Integer num) {
        this.taskType = num;
        setValue();
    }

    public String getJobInfo() {
        return this.jobInfo;
    }

    public void setJobInfo(String str) {
        this.jobInfo = str;
        setValue();
    }

    @Override // com.huawei.odmf.core.AManagedObject, android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        if (this.mId != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mId.intValue());
        } else {
            parcel.writeByte((byte) 0);
            parcel.writeInt(1);
        }
        if (this.jobName != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.jobName);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.parameter != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.parameter);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.scheduleType != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.scheduleType.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.taskType != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.taskType.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.jobInfo != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.jobInfo);
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<PresetJob> getHelper() {
        return PresetJobHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "PresetJob { mId: " + this.mId + ", jobName: " + this.jobName + ", parameter: " + this.parameter + ", scheduleType: " + this.scheduleType + ", taskType: " + this.taskType + ", jobInfo: " + this.jobInfo + " }";
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
