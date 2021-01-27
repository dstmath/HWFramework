package com.huawei.nb.model.rule;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class Event extends AManagedObject {
    public static final Parcelable.Creator<Event> CREATOR = new Parcelable.Creator<Event>() {
        /* class com.huawei.nb.model.rule.Event.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public Event createFromParcel(Parcel parcel) {
            return new Event(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public Event[] newArray(int i) {
            return new Event[i];
        }
    };
    private Integer continuity;
    private Integer count;
    private Long id;
    private Long itemId;
    private Date lastTime;
    private String name;
    private Long operatorId;
    private Date time;
    private Long timeout;
    private Integer totalCount;
    private String value;

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
        return "com.huawei.nb.model.rule.Event";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public Event(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Integer num = null;
        this.id = cursor.isNull(1) ? null : Long.valueOf(cursor.getLong(1));
        this.name = cursor.getString(2);
        this.operatorId = cursor.isNull(3) ? null : Long.valueOf(cursor.getLong(3));
        this.itemId = cursor.isNull(4) ? null : Long.valueOf(cursor.getLong(4));
        this.value = cursor.getString(5);
        this.timeout = cursor.isNull(6) ? null : Long.valueOf(cursor.getLong(6));
        this.continuity = cursor.isNull(7) ? null : Integer.valueOf(cursor.getInt(7));
        this.time = cursor.isNull(8) ? null : new Date(cursor.getLong(8));
        this.lastTime = cursor.isNull(9) ? null : new Date(cursor.getLong(9));
        this.count = cursor.isNull(10) ? null : Integer.valueOf(cursor.getInt(10));
        this.totalCount = !cursor.isNull(11) ? Integer.valueOf(cursor.getInt(11)) : num;
    }

    public Event(Parcel parcel) {
        super(parcel);
        Integer num = null;
        if (parcel.readByte() == 0) {
            this.id = null;
            parcel.readLong();
        } else {
            this.id = Long.valueOf(parcel.readLong());
        }
        this.name = parcel.readByte() == 0 ? null : parcel.readString();
        this.operatorId = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.itemId = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.value = parcel.readByte() == 0 ? null : parcel.readString();
        this.timeout = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.continuity = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.time = parcel.readByte() == 0 ? null : new Date(parcel.readLong());
        this.lastTime = parcel.readByte() == 0 ? null : new Date(parcel.readLong());
        this.count = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.totalCount = parcel.readByte() != 0 ? Integer.valueOf(parcel.readInt()) : num;
    }

    private Event(Long l, String str, Long l2, Long l3, String str2, Long l4, Integer num, Date date, Date date2, Integer num2, Integer num3) {
        this.id = l;
        this.name = str;
        this.operatorId = l2;
        this.itemId = l3;
        this.value = str2;
        this.timeout = l4;
        this.continuity = num;
        this.time = date;
        this.lastTime = date2;
        this.count = num2;
        this.totalCount = num3;
    }

    public Event() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long l) {
        this.id = l;
        setValue();
    }

    public String getName() {
        return this.name;
    }

    public void setName(String str) {
        this.name = str;
        setValue();
    }

    public Long getOperatorId() {
        return this.operatorId;
    }

    public void setOperatorId(Long l) {
        this.operatorId = l;
        setValue();
    }

    public Long getItemId() {
        return this.itemId;
    }

    public void setItemId(Long l) {
        this.itemId = l;
        setValue();
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String str) {
        this.value = str;
        setValue();
    }

    public Long getTimeout() {
        return this.timeout;
    }

    public void setTimeout(Long l) {
        this.timeout = l;
        setValue();
    }

    public Integer getContinuity() {
        return this.continuity;
    }

    public void setContinuity(Integer num) {
        this.continuity = num;
        setValue();
    }

    public Date getTime() {
        return this.time;
    }

    public void setTime(Date date) {
        this.time = date;
        setValue();
    }

    public Date getLastTime() {
        return this.lastTime;
    }

    public void setLastTime(Date date) {
        this.lastTime = date;
        setValue();
    }

    public Integer getCount() {
        return this.count;
    }

    public void setCount(Integer num) {
        this.count = num;
        setValue();
    }

    public Integer getTotalCount() {
        return this.totalCount;
    }

    public void setTotalCount(Integer num) {
        this.totalCount = num;
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
        if (this.name != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.name);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.operatorId != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.operatorId.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.itemId != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.itemId.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.value != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.value);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.timeout != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.timeout.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.continuity != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.continuity.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.time != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.time.getTime());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.lastTime != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.lastTime.getTime());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.count != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.count.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.totalCount != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.totalCount.intValue());
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<Event> getHelper() {
        return EventHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "Event { id: " + this.id + ", name: " + this.name + ", operatorId: " + this.operatorId + ", itemId: " + this.itemId + ", value: " + this.value + ", timeout: " + this.timeout + ", continuity: " + this.continuity + ", time: " + this.time + ", lastTime: " + this.lastTime + ", count: " + this.count + ", totalCount: " + this.totalCount + " }";
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
