package com.huawei.nb.model.rule;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class EventTimer extends AManagedObject {
    public static final Parcelable.Creator<EventTimer> CREATOR = new Parcelable.Creator<EventTimer>() {
        /* class com.huawei.nb.model.rule.EventTimer.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public EventTimer createFromParcel(Parcel parcel) {
            return new EventTimer(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public EventTimer[] newArray(int i) {
            return new EventTimer[i];
        }
    };
    private Long id;
    private Long length;
    private Long ruleId;
    private Integer switchOn;

    @Override // com.huawei.odmf.core.AManagedObject, android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getDatabaseName() {
        return "dsRule";
    }

    public String getDatabaseVersion() {
        return "0.0.4";
    }

    public int getDatabaseVersionCode() {
        return 4;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getEntityName() {
        return "com.huawei.nb.model.rule.EventTimer";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public EventTimer(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Long l = null;
        this.id = cursor.isNull(1) ? null : Long.valueOf(cursor.getLong(1));
        this.length = cursor.isNull(2) ? null : Long.valueOf(cursor.getLong(2));
        this.switchOn = cursor.isNull(3) ? null : Integer.valueOf(cursor.getInt(3));
        this.ruleId = !cursor.isNull(4) ? Long.valueOf(cursor.getLong(4)) : l;
    }

    public EventTimer(Parcel parcel) {
        super(parcel);
        Long l = null;
        if (parcel.readByte() == 0) {
            this.id = null;
            parcel.readLong();
        } else {
            this.id = Long.valueOf(parcel.readLong());
        }
        this.length = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.switchOn = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.ruleId = parcel.readByte() != 0 ? Long.valueOf(parcel.readLong()) : l;
    }

    private EventTimer(Long l, Long l2, Integer num, Long l3) {
        this.id = l;
        this.length = l2;
        this.switchOn = num;
        this.ruleId = l3;
    }

    public EventTimer() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long l) {
        this.id = l;
        setValue();
    }

    public Long getLength() {
        return this.length;
    }

    public void setLength(Long l) {
        this.length = l;
        setValue();
    }

    public Integer getSwitchOn() {
        return this.switchOn;
    }

    public void setSwitchOn(Integer num) {
        this.switchOn = num;
        setValue();
    }

    public Long getRuleId() {
        return this.ruleId;
    }

    public void setRuleId(Long l) {
        this.ruleId = l;
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
        if (this.length != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.length.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.switchOn != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.switchOn.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.ruleId != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.ruleId.longValue());
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<EventTimer> getHelper() {
        return EventTimerHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "EventTimer { id: " + this.id + ", length: " + this.length + ", switchOn: " + this.switchOn + ", ruleId: " + this.ruleId + " }";
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
