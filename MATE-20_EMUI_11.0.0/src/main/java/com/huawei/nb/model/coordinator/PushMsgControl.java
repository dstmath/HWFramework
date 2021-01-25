package com.huawei.nb.model.coordinator;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class PushMsgControl extends AManagedObject {
    public static final Parcelable.Creator<PushMsgControl> CREATOR = new Parcelable.Creator<PushMsgControl>() {
        /* class com.huawei.nb.model.coordinator.PushMsgControl.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public PushMsgControl createFromParcel(Parcel parcel) {
            return new PushMsgControl(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public PushMsgControl[] newArray(int i) {
            return new PushMsgControl[i];
        }
    };
    private Integer count;
    private Long id;
    private Integer maxReportInterval;
    private String msgType;
    private Integer presetCount;
    private String updateTime;

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
        return "com.huawei.nb.model.coordinator.PushMsgControl";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public PushMsgControl(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Integer num = null;
        this.id = cursor.isNull(1) ? null : Long.valueOf(cursor.getLong(1));
        this.msgType = cursor.getString(2);
        this.presetCount = cursor.isNull(3) ? null : Integer.valueOf(cursor.getInt(3));
        this.count = cursor.isNull(4) ? null : Integer.valueOf(cursor.getInt(4));
        this.maxReportInterval = !cursor.isNull(5) ? Integer.valueOf(cursor.getInt(5)) : num;
        this.updateTime = cursor.getString(6);
    }

    public PushMsgControl(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.id = null;
            parcel.readLong();
        } else {
            this.id = Long.valueOf(parcel.readLong());
        }
        this.msgType = parcel.readByte() == 0 ? null : parcel.readString();
        this.presetCount = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.count = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.maxReportInterval = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.updateTime = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private PushMsgControl(Long l, String str, Integer num, Integer num2, Integer num3, String str2) {
        this.id = l;
        this.msgType = str;
        this.presetCount = num;
        this.count = num2;
        this.maxReportInterval = num3;
        this.updateTime = str2;
    }

    public PushMsgControl() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long l) {
        this.id = l;
        setValue();
    }

    public String getMsgType() {
        return this.msgType;
    }

    public void setMsgType(String str) {
        this.msgType = str;
        setValue();
    }

    public Integer getPresetCount() {
        return this.presetCount;
    }

    public void setPresetCount(Integer num) {
        this.presetCount = num;
        setValue();
    }

    public Integer getCount() {
        return this.count;
    }

    public void setCount(Integer num) {
        this.count = num;
        setValue();
    }

    public Integer getMaxReportInterval() {
        return this.maxReportInterval;
    }

    public void setMaxReportInterval(Integer num) {
        this.maxReportInterval = num;
        setValue();
    }

    public String getUpdateTime() {
        return this.updateTime;
    }

    public void setUpdateTime(String str) {
        this.updateTime = str;
        setValue();
    }

    @Override // com.huawei.odmf.core.AManagedObject, android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        if (this.id != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.id.longValue());
        } else {
            parcel.writeByte((byte) 0);
            parcel.writeLong(1);
        }
        if (this.msgType != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.msgType);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.presetCount != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.presetCount.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.count != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.count.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.maxReportInterval != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.maxReportInterval.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.updateTime != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.updateTime);
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<PushMsgControl> getHelper() {
        return PushMsgControlHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "PushMsgControl { id: " + this.id + ", msgType: " + this.msgType + ", presetCount: " + this.presetCount + ", count: " + this.count + ", maxReportInterval: " + this.maxReportInterval + ", updateTime: " + this.updateTime + " }";
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
