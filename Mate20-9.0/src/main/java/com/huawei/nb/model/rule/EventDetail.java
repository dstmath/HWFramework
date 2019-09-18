package com.huawei.nb.model.rule;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class EventDetail extends AManagedObject {
    public static final Parcelable.Creator<EventDetail> CREATOR = new Parcelable.Creator<EventDetail>() {
        public EventDetail createFromParcel(Parcel in) {
            return new EventDetail(in);
        }

        public EventDetail[] newArray(int size) {
            return new EventDetail[size];
        }
    };
    private Long id;
    private Long itemId;
    private Long operatorId;
    private Date startTime;

    public EventDetail(Cursor cursor) {
        Date date = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.id = cursor.isNull(1) ? null : Long.valueOf(cursor.getLong(1));
        this.operatorId = cursor.isNull(2) ? null : Long.valueOf(cursor.getLong(2));
        this.itemId = cursor.isNull(3) ? null : Long.valueOf(cursor.getLong(3));
        this.startTime = !cursor.isNull(4) ? new Date(cursor.getLong(4)) : date;
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public EventDetail(Parcel in) {
        super(in);
        Date date = null;
        if (in.readByte() == 0) {
            this.id = null;
            in.readLong();
        } else {
            this.id = Long.valueOf(in.readLong());
        }
        this.operatorId = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.itemId = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.startTime = in.readByte() != 0 ? new Date(in.readLong()) : date;
    }

    private EventDetail(Long id2, Long operatorId2, Long itemId2, Date startTime2) {
        this.id = id2;
        this.operatorId = operatorId2;
        this.itemId = itemId2;
        this.startTime = startTime2;
    }

    public EventDetail() {
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

    public Date getStartTime() {
        return this.startTime;
    }

    public void setStartTime(Date startTime2) {
        this.startTime = startTime2;
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
        if (this.startTime != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.startTime.getTime());
            return;
        }
        out.writeByte((byte) 0);
    }

    public AEntityHelper<EventDetail> getHelper() {
        return EventDetailHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.rule.EventDetail";
    }

    public String getDatabaseName() {
        return "dsRule";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("EventDetail { id: ").append(this.id);
        sb.append(", operatorId: ").append(this.operatorId);
        sb.append(", itemId: ").append(this.itemId);
        sb.append(", startTime: ").append(this.startTime);
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
