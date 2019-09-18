package com.huawei.nb.model.rule;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class EventTimer extends AManagedObject {
    public static final Parcelable.Creator<EventTimer> CREATOR = new Parcelable.Creator<EventTimer>() {
        public EventTimer createFromParcel(Parcel in) {
            return new EventTimer(in);
        }

        public EventTimer[] newArray(int size) {
            return new EventTimer[size];
        }
    };
    private Long id;
    private Long length;
    private Long ruleId;
    private Integer switchOn;

    public EventTimer(Cursor cursor) {
        Long l = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.id = cursor.isNull(1) ? null : Long.valueOf(cursor.getLong(1));
        this.length = cursor.isNull(2) ? null : Long.valueOf(cursor.getLong(2));
        this.switchOn = cursor.isNull(3) ? null : Integer.valueOf(cursor.getInt(3));
        this.ruleId = !cursor.isNull(4) ? Long.valueOf(cursor.getLong(4)) : l;
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public EventTimer(Parcel in) {
        super(in);
        Long l = null;
        if (in.readByte() == 0) {
            this.id = null;
            in.readLong();
        } else {
            this.id = Long.valueOf(in.readLong());
        }
        this.length = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.switchOn = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.ruleId = in.readByte() != 0 ? Long.valueOf(in.readLong()) : l;
    }

    private EventTimer(Long id2, Long length2, Integer switchOn2, Long ruleId2) {
        this.id = id2;
        this.length = length2;
        this.switchOn = switchOn2;
        this.ruleId = ruleId2;
    }

    public EventTimer() {
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

    public Long getLength() {
        return this.length;
    }

    public void setLength(Long length2) {
        this.length = length2;
        setValue();
    }

    public Integer getSwitchOn() {
        return this.switchOn;
    }

    public void setSwitchOn(Integer switchOn2) {
        this.switchOn = switchOn2;
        setValue();
    }

    public Long getRuleId() {
        return this.ruleId;
    }

    public void setRuleId(Long ruleId2) {
        this.ruleId = ruleId2;
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
        if (this.length != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.length.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.switchOn != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.switchOn.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.ruleId != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.ruleId.longValue());
            return;
        }
        out.writeByte((byte) 0);
    }

    public AEntityHelper<EventTimer> getHelper() {
        return EventTimerHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.rule.EventTimer";
    }

    public String getDatabaseName() {
        return "dsRule";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("EventTimer { id: ").append(this.id);
        sb.append(", length: ").append(this.length);
        sb.append(", switchOn: ").append(this.switchOn);
        sb.append(", ruleId: ").append(this.ruleId);
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
        return "0.0.4";
    }

    public int getDatabaseVersionCode() {
        return 4;
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }
}
