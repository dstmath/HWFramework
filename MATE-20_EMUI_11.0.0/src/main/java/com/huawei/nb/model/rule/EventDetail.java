package com.huawei.nb.model.rule;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class EventDetail extends AManagedObject {
    public static final Parcelable.Creator<EventDetail> CREATOR = new Parcelable.Creator<EventDetail>() {
        /* class com.huawei.nb.model.rule.EventDetail.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public EventDetail createFromParcel(Parcel parcel) {
            return new EventDetail(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public EventDetail[] newArray(int i) {
            return new EventDetail[i];
        }
    };
    private Long id;
    private Long itemId;
    private Long operatorId;
    private Date startTime;

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
        return "com.huawei.nb.model.rule.EventDetail";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public EventDetail(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Date date = null;
        this.id = cursor.isNull(1) ? null : Long.valueOf(cursor.getLong(1));
        this.operatorId = cursor.isNull(2) ? null : Long.valueOf(cursor.getLong(2));
        this.itemId = cursor.isNull(3) ? null : Long.valueOf(cursor.getLong(3));
        this.startTime = !cursor.isNull(4) ? new Date(cursor.getLong(4)) : date;
    }

    public EventDetail(Parcel parcel) {
        super(parcel);
        Date date = null;
        if (parcel.readByte() == 0) {
            this.id = null;
            parcel.readLong();
        } else {
            this.id = Long.valueOf(parcel.readLong());
        }
        this.operatorId = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.itemId = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.startTime = parcel.readByte() != 0 ? new Date(parcel.readLong()) : date;
    }

    private EventDetail(Long l, Long l2, Long l3, Date date) {
        this.id = l;
        this.operatorId = l2;
        this.itemId = l3;
        this.startTime = date;
    }

    public EventDetail() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long l) {
        this.id = l;
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

    public Date getStartTime() {
        return this.startTime;
    }

    public void setStartTime(Date date) {
        this.startTime = date;
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
        if (this.startTime != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.startTime.getTime());
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<EventDetail> getHelper() {
        return EventDetailHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "EventDetail { id: " + this.id + ", operatorId: " + this.operatorId + ", itemId: " + this.itemId + ", startTime: " + this.startTime + " }";
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
