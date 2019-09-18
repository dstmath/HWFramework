package com.huawei.nb.model.pengine;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class UpJobInstance extends AManagedObject {
    public static final Parcelable.Creator<UpJobInstance> CREATOR = new Parcelable.Creator<UpJobInstance>() {
        public UpJobInstance createFromParcel(Parcel in) {
            return new UpJobInstance(in);
        }

        public UpJobInstance[] newArray(int size) {
            return new UpJobInstance[size];
        }
    };
    private Long analyzeTime;
    private Long createTime;
    private Integer id;
    private Long lastModifyTime;
    private String result;
    private String resultDesc;
    private String status;
    private String tasks;
    private String type;

    public UpJobInstance(Cursor cursor) {
        Long l = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.id = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.type = cursor.getString(2);
        this.status = cursor.getString(3);
        this.tasks = cursor.getString(4);
        this.result = cursor.getString(5);
        this.resultDesc = cursor.getString(6);
        this.analyzeTime = cursor.isNull(7) ? null : Long.valueOf(cursor.getLong(7));
        this.createTime = cursor.isNull(8) ? null : Long.valueOf(cursor.getLong(8));
        this.lastModifyTime = !cursor.isNull(9) ? Long.valueOf(cursor.getLong(9)) : l;
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public UpJobInstance(Parcel in) {
        super(in);
        Long l = null;
        if (in.readByte() == 0) {
            this.id = null;
            in.readInt();
        } else {
            this.id = Integer.valueOf(in.readInt());
        }
        this.type = in.readByte() == 0 ? null : in.readString();
        this.status = in.readByte() == 0 ? null : in.readString();
        this.tasks = in.readByte() == 0 ? null : in.readString();
        this.result = in.readByte() == 0 ? null : in.readString();
        this.resultDesc = in.readByte() == 0 ? null : in.readString();
        this.analyzeTime = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.createTime = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.lastModifyTime = in.readByte() != 0 ? Long.valueOf(in.readLong()) : l;
    }

    private UpJobInstance(Integer id2, String type2, String status2, String tasks2, String result2, String resultDesc2, Long analyzeTime2, Long createTime2, Long lastModifyTime2) {
        this.id = id2;
        this.type = type2;
        this.status = status2;
        this.tasks = tasks2;
        this.result = result2;
        this.resultDesc = resultDesc2;
        this.analyzeTime = analyzeTime2;
        this.createTime = createTime2;
        this.lastModifyTime = lastModifyTime2;
    }

    public UpJobInstance() {
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

    public String getType() {
        return this.type;
    }

    public void setType(String type2) {
        this.type = type2;
        setValue();
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status2) {
        this.status = status2;
        setValue();
    }

    public String getTasks() {
        return this.tasks;
    }

    public void setTasks(String tasks2) {
        this.tasks = tasks2;
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

    public Long getAnalyzeTime() {
        return this.analyzeTime;
    }

    public void setAnalyzeTime(Long analyzeTime2) {
        this.analyzeTime = analyzeTime2;
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

    public void writeToParcel(Parcel out, int ignored) {
        super.writeToParcel(out, ignored);
        if (this.id != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.id.intValue());
        } else {
            out.writeByte((byte) 0);
            out.writeInt(1);
        }
        if (this.type != null) {
            out.writeByte((byte) 1);
            out.writeString(this.type);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.status != null) {
            out.writeByte((byte) 1);
            out.writeString(this.status);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.tasks != null) {
            out.writeByte((byte) 1);
            out.writeString(this.tasks);
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
        if (this.analyzeTime != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.analyzeTime.longValue());
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
            return;
        }
        out.writeByte((byte) 0);
    }

    public AEntityHelper<UpJobInstance> getHelper() {
        return UpJobInstanceHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.pengine.UpJobInstance";
    }

    public String getDatabaseName() {
        return "dsPengineData";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("UpJobInstance { id: ").append(this.id);
        sb.append(", type: ").append(this.type);
        sb.append(", status: ").append(this.status);
        sb.append(", tasks: ").append(this.tasks);
        sb.append(", result: ").append(this.result);
        sb.append(", resultDesc: ").append(this.resultDesc);
        sb.append(", analyzeTime: ").append(this.analyzeTime);
        sb.append(", createTime: ").append(this.createTime);
        sb.append(", lastModifyTime: ").append(this.lastModifyTime);
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
