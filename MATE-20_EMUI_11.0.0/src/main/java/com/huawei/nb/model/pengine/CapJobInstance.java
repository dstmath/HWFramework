package com.huawei.nb.model.pengine;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class CapJobInstance extends AManagedObject {
    public static final Parcelable.Creator<CapJobInstance> CREATOR = new Parcelable.Creator<CapJobInstance>() {
        /* class com.huawei.nb.model.pengine.CapJobInstance.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public CapJobInstance createFromParcel(Parcel parcel) {
            return new CapJobInstance(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public CapJobInstance[] newArray(int i) {
            return new CapJobInstance[i];
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
        return "com.huawei.nb.model.pengine.CapJobInstance";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public CapJobInstance(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Long l = null;
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

    public CapJobInstance(Parcel parcel) {
        super(parcel);
        Long l = null;
        if (parcel.readByte() == 0) {
            this.id = null;
            parcel.readInt();
        } else {
            this.id = Integer.valueOf(parcel.readInt());
        }
        this.type = parcel.readByte() == 0 ? null : parcel.readString();
        this.status = parcel.readByte() == 0 ? null : parcel.readString();
        this.tasks = parcel.readByte() == 0 ? null : parcel.readString();
        this.result = parcel.readByte() == 0 ? null : parcel.readString();
        this.resultDesc = parcel.readByte() == 0 ? null : parcel.readString();
        this.analyzeTime = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.createTime = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.lastModifyTime = parcel.readByte() != 0 ? Long.valueOf(parcel.readLong()) : l;
    }

    private CapJobInstance(Integer num, String str, String str2, String str3, String str4, String str5, Long l, Long l2, Long l3) {
        this.id = num;
        this.type = str;
        this.status = str2;
        this.tasks = str3;
        this.result = str4;
        this.resultDesc = str5;
        this.analyzeTime = l;
        this.createTime = l2;
        this.lastModifyTime = l3;
    }

    public CapJobInstance() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer num) {
        this.id = num;
        setValue();
    }

    public String getType() {
        return this.type;
    }

    public void setType(String str) {
        this.type = str;
        setValue();
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String str) {
        this.status = str;
        setValue();
    }

    public String getTasks() {
        return this.tasks;
    }

    public void setTasks(String str) {
        this.tasks = str;
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

    public Long getAnalyzeTime() {
        return this.analyzeTime;
    }

    public void setAnalyzeTime(Long l) {
        this.analyzeTime = l;
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
        if (this.type != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.type);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.status != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.status);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.tasks != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.tasks);
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
        if (this.analyzeTime != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.analyzeTime.longValue());
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
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<CapJobInstance> getHelper() {
        return CapJobInstanceHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "CapJobInstance { id: " + this.id + ", type: " + this.type + ", status: " + this.status + ", tasks: " + this.tasks + ", result: " + this.result + ", resultDesc: " + this.resultDesc + ", analyzeTime: " + this.analyzeTime + ", createTime: " + this.createTime + ", lastModifyTime: " + this.lastModifyTime + " }";
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
