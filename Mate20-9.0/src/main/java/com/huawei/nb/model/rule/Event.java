package com.huawei.nb.model.rule;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class Event extends AManagedObject {
    public static final Parcelable.Creator<Event> CREATOR = new Parcelable.Creator<Event>() {
        public Event createFromParcel(Parcel in) {
            return new Event(in);
        }

        public Event[] newArray(int size) {
            return new Event[size];
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

    public Event(Cursor cursor) {
        Integer num = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
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

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public Event(Parcel in) {
        super(in);
        Integer num = null;
        if (in.readByte() == 0) {
            this.id = null;
            in.readLong();
        } else {
            this.id = Long.valueOf(in.readLong());
        }
        this.name = in.readByte() == 0 ? null : in.readString();
        this.operatorId = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.itemId = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.value = in.readByte() == 0 ? null : in.readString();
        this.timeout = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.continuity = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.time = in.readByte() == 0 ? null : new Date(in.readLong());
        this.lastTime = in.readByte() == 0 ? null : new Date(in.readLong());
        this.count = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.totalCount = in.readByte() != 0 ? Integer.valueOf(in.readInt()) : num;
    }

    private Event(Long id2, String name2, Long operatorId2, Long itemId2, String value2, Long timeout2, Integer continuity2, Date time2, Date lastTime2, Integer count2, Integer totalCount2) {
        this.id = id2;
        this.name = name2;
        this.operatorId = operatorId2;
        this.itemId = itemId2;
        this.value = value2;
        this.timeout = timeout2;
        this.continuity = continuity2;
        this.time = time2;
        this.lastTime = lastTime2;
        this.count = count2;
        this.totalCount = totalCount2;
    }

    public Event() {
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

    public String getName() {
        return this.name;
    }

    public void setName(String name2) {
        this.name = name2;
        setValue();
    }

    public Long getOperatorId() {
        return this.operatorId;
    }

    public void setOperatorId(Long operatorId2) {
        this.operatorId = operatorId2;
        setValue();
    }

    public Long getItemId() {
        return this.itemId;
    }

    public void setItemId(Long itemId2) {
        this.itemId = itemId2;
        setValue();
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value2) {
        this.value = value2;
        setValue();
    }

    public Long getTimeout() {
        return this.timeout;
    }

    public void setTimeout(Long timeout2) {
        this.timeout = timeout2;
        setValue();
    }

    public Integer getContinuity() {
        return this.continuity;
    }

    public void setContinuity(Integer continuity2) {
        this.continuity = continuity2;
        setValue();
    }

    public Date getTime() {
        return this.time;
    }

    public void setTime(Date time2) {
        this.time = time2;
        setValue();
    }

    public Date getLastTime() {
        return this.lastTime;
    }

    public void setLastTime(Date lastTime2) {
        this.lastTime = lastTime2;
        setValue();
    }

    public Integer getCount() {
        return this.count;
    }

    public void setCount(Integer count2) {
        this.count = count2;
        setValue();
    }

    public Integer getTotalCount() {
        return this.totalCount;
    }

    public void setTotalCount(Integer totalCount2) {
        this.totalCount = totalCount2;
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
        if (this.name != null) {
            out.writeByte((byte) 1);
            out.writeString(this.name);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.operatorId != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.operatorId.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.itemId != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.itemId.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.value != null) {
            out.writeByte((byte) 1);
            out.writeString(this.value);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.timeout != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.timeout.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.continuity != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.continuity.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.time != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.time.getTime());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.lastTime != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.lastTime.getTime());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.count != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.count.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.totalCount != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.totalCount.intValue());
            return;
        }
        out.writeByte((byte) 0);
    }

    public AEntityHelper<Event> getHelper() {
        return EventHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.rule.Event";
    }

    public String getDatabaseName() {
        return "dsRule";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("Event { id: ").append(this.id);
        sb.append(", name: ").append(this.name);
        sb.append(", operatorId: ").append(this.operatorId);
        sb.append(", itemId: ").append(this.itemId);
        sb.append(", value: ").append(this.value);
        sb.append(", timeout: ").append(this.timeout);
        sb.append(", continuity: ").append(this.continuity);
        sb.append(", time: ").append(this.time);
        sb.append(", lastTime: ").append(this.lastTime);
        sb.append(", count: ").append(this.count);
        sb.append(", totalCount: ").append(this.totalCount);
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
