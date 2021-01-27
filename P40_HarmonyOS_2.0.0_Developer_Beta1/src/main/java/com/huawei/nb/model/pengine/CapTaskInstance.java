package com.huawei.nb.model.pengine;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class CapTaskInstance extends AManagedObject {
    public static final Parcelable.Creator<CapTaskInstance> CREATOR = new Parcelable.Creator<CapTaskInstance>() {
        /* class com.huawei.nb.model.pengine.CapTaskInstance.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public CapTaskInstance createFromParcel(Parcel parcel) {
            return new CapTaskInstance(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public CapTaskInstance[] newArray(int i) {
            return new CapTaskInstance[i];
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
        return "com.huawei.nb.model.pengine.CapTaskInstance";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public CapTaskInstance(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Long l = null;
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

    public CapTaskInstance(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.taskId = null;
            parcel.readInt();
        } else {
            this.taskId = Integer.valueOf(parcel.readInt());
        }
        this.jobId = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.taskName = parcel.readByte() == 0 ? null : parcel.readString();
        this.status = parcel.readByte() == 0 ? null : parcel.readString();
        this.result = parcel.readByte() == 0 ? null : parcel.readString();
        this.resultDesc = parcel.readByte() == 0 ? null : parcel.readString();
        this.createTime = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.lastModifyTime = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.attrs = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private CapTaskInstance(Integer num, Long l, String str, String str2, String str3, String str4, Long l2, Long l3, String str5) {
        this.taskId = num;
        this.jobId = l;
        this.taskName = str;
        this.status = str2;
        this.result = str3;
        this.resultDesc = str4;
        this.createTime = l2;
        this.lastModifyTime = l3;
        this.attrs = str5;
    }

    public CapTaskInstance() {
    }

    public Integer getTaskId() {
        return this.taskId;
    }

    public void setTaskId(Integer num) {
        this.taskId = num;
        setValue();
    }

    public Long getJobId() {
        return this.jobId;
    }

    public void setJobId(Long l) {
        this.jobId = l;
        setValue();
    }

    public String getTaskName() {
        return this.taskName;
    }

    public void setTaskName(String str) {
        this.taskName = str;
        setValue();
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String str) {
        this.status = str;
        setValue();
    }

    public String getResult() {
        return this.result;
    }

    public void setResult(String str) {
        this.result = str;
        setValue();
    }

    public String getResultDesc() {
        return this.resultDesc;
    }

    public void setResultDesc(String str) {
        this.resultDesc = str;
        setValue();
    }

    public Long getCreateTime() {
        return this.createTime;
    }

    public void setCreateTime(Long l) {
        this.createTime = l;
        setValue();
    }

    public Long getLastModifyTime() {
        return this.lastModifyTime;
    }

    public void setLastModifyTime(Long l) {
        this.lastModifyTime = l;
        setValue();
    }

    public String getAttrs() {
        return this.attrs;
    }

    public void setAttrs(String str) {
        this.attrs = str;
        setValue();
    }

    @Override // com.huawei.odmf.core.AManagedObject, android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        if (this.taskId != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.taskId.intValue());
        } else {
            parcel.writeByte((byte) 0);
            parcel.writeInt(1);
        }
        if (this.jobId != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.jobId.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.taskName != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.taskName);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.status != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.status);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.result != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.result);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.resultDesc != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.resultDesc);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.createTime != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.createTime.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.lastModifyTime != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.lastModifyTime.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.attrs != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.attrs);
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<CapTaskInstance> getHelper() {
        return CapTaskInstanceHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "CapTaskInstance { taskId: " + this.taskId + ", jobId: " + this.jobId + ", taskName: " + this.taskName + ", status: " + this.status + ", result: " + this.result + ", resultDesc: " + this.resultDesc + ", createTime: " + this.createTime + ", lastModifyTime: " + this.lastModifyTime + ", attrs: " + this.attrs + " }";
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
