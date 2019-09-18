package com.huawei.nb.model.coordinator;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class PushMsgControl extends AManagedObject {
    public static final Parcelable.Creator<PushMsgControl> CREATOR = new Parcelable.Creator<PushMsgControl>() {
        public PushMsgControl createFromParcel(Parcel in) {
            return new PushMsgControl(in);
        }

        public PushMsgControl[] newArray(int size) {
            return new PushMsgControl[size];
        }
    };
    private Integer count;
    private Long id;
    private Integer maxReportInterval;
    private String msgType;
    private Integer presetCount;
    private String updateTime;

    public PushMsgControl(Cursor cursor) {
        Integer num = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.id = cursor.isNull(1) ? null : Long.valueOf(cursor.getLong(1));
        this.msgType = cursor.getString(2);
        this.presetCount = cursor.isNull(3) ? null : Integer.valueOf(cursor.getInt(3));
        this.count = cursor.isNull(4) ? null : Integer.valueOf(cursor.getInt(4));
        this.maxReportInterval = !cursor.isNull(5) ? Integer.valueOf(cursor.getInt(5)) : num;
        this.updateTime = cursor.getString(6);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public PushMsgControl(Parcel in) {
        super(in);
        String str = null;
        if (in.readByte() == 0) {
            this.id = null;
            in.readLong();
        } else {
            this.id = Long.valueOf(in.readLong());
        }
        this.msgType = in.readByte() == 0 ? null : in.readString();
        this.presetCount = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.count = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.maxReportInterval = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.updateTime = in.readByte() != 0 ? in.readString() : str;
    }

    private PushMsgControl(Long id2, String msgType2, Integer presetCount2, Integer count2, Integer maxReportInterval2, String updateTime2) {
        this.id = id2;
        this.msgType = msgType2;
        this.presetCount = presetCount2;
        this.count = count2;
        this.maxReportInterval = maxReportInterval2;
        this.updateTime = updateTime2;
    }

    public PushMsgControl() {
    }

    public int describeContents() {
        return 0;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id2) {
        this.id = id2;
        setValue();
    }

    public String getMsgType() {
        return this.msgType;
    }

    public void setMsgType(String msgType2) {
        this.msgType = msgType2;
        setValue();
    }

    public Integer getPresetCount() {
        return this.presetCount;
    }

    public void setPresetCount(Integer presetCount2) {
        this.presetCount = presetCount2;
        setValue();
    }

    public Integer getCount() {
        return this.count;
    }

    public void setCount(Integer count2) {
        this.count = count2;
        setValue();
    }

    public Integer getMaxReportInterval() {
        return this.maxReportInterval;
    }

    public void setMaxReportInterval(Integer maxReportInterval2) {
        this.maxReportInterval = maxReportInterval2;
        setValue();
    }

    public String getUpdateTime() {
        return this.updateTime;
    }

    public void setUpdateTime(String updateTime2) {
        this.updateTime = updateTime2;
        setValue();
    }

    public void writeToParcel(Parcel out, int ignored) {
        super.writeToParcel(out, ignored);
        if (this.id != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.id.longValue());
        } else {
            out.writeByte((byte) 0);
            out.writeLong(1);
        }
        if (this.msgType != null) {
            out.writeByte((byte) 1);
            out.writeString(this.msgType);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.presetCount != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.presetCount.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.count != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.count.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.maxReportInterval != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.maxReportInterval.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.updateTime != null) {
            out.writeByte((byte) 1);
            out.writeString(this.updateTime);
            return;
        }
        out.writeByte((byte) 0);
    }

    public AEntityHelper<PushMsgControl> getHelper() {
        return PushMsgControlHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.coordinator.PushMsgControl";
    }

    public String getDatabaseName() {
        return "dsMeta";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("PushMsgControl { id: ").append(this.id);
        sb.append(", msgType: ").append(this.msgType);
        sb.append(", presetCount: ").append(this.presetCount);
        sb.append(", count: ").append(this.count);
        sb.append(", maxReportInterval: ").append(this.maxReportInterval);
        sb.append(", updateTime: ").append(this.updateTime);
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
