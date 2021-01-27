package com.huawei.nb.model.meta;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class VisitStatistic extends AManagedObject {
    public static final Parcelable.Creator<VisitStatistic> CREATOR = new Parcelable.Creator<VisitStatistic>() {
        /* class com.huawei.nb.model.meta.VisitStatistic.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public VisitStatistic createFromParcel(Parcel parcel) {
            return new VisitStatistic(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public VisitStatistic[] newArray(int i) {
            return new VisitStatistic[i];
        }
    };
    private Long deleteCount;
    private Integer guestId;
    private Integer hostId;
    private Integer id;
    private Long insertCount;
    private Long queryCount;
    private Long subscribeCount;
    private Integer type;
    private Long updateCount;
    private Long updateTime;
    private Long uploadTime;

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
        return "com.huawei.nb.model.meta.VisitStatistic";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public VisitStatistic(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Long l = null;
        this.id = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.guestId = cursor.isNull(2) ? null : Integer.valueOf(cursor.getInt(2));
        this.hostId = cursor.isNull(3) ? null : Integer.valueOf(cursor.getInt(3));
        this.type = cursor.isNull(4) ? null : Integer.valueOf(cursor.getInt(4));
        this.insertCount = cursor.isNull(5) ? null : Long.valueOf(cursor.getLong(5));
        this.updateCount = cursor.isNull(6) ? null : Long.valueOf(cursor.getLong(6));
        this.deleteCount = cursor.isNull(7) ? null : Long.valueOf(cursor.getLong(7));
        this.queryCount = cursor.isNull(8) ? null : Long.valueOf(cursor.getLong(8));
        this.subscribeCount = cursor.isNull(9) ? null : Long.valueOf(cursor.getLong(9));
        this.updateTime = cursor.isNull(10) ? null : Long.valueOf(cursor.getLong(10));
        this.uploadTime = !cursor.isNull(11) ? Long.valueOf(cursor.getLong(11)) : l;
    }

    public VisitStatistic(Parcel parcel) {
        super(parcel);
        Long l = null;
        if (parcel.readByte() == 0) {
            this.id = null;
            parcel.readInt();
        } else {
            this.id = Integer.valueOf(parcel.readInt());
        }
        this.guestId = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.hostId = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.type = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.insertCount = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.updateCount = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.deleteCount = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.queryCount = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.subscribeCount = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.updateTime = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.uploadTime = parcel.readByte() != 0 ? Long.valueOf(parcel.readLong()) : l;
    }

    private VisitStatistic(Integer num, Integer num2, Integer num3, Integer num4, Long l, Long l2, Long l3, Long l4, Long l5, Long l6, Long l7) {
        this.id = num;
        this.guestId = num2;
        this.hostId = num3;
        this.type = num4;
        this.insertCount = l;
        this.updateCount = l2;
        this.deleteCount = l3;
        this.queryCount = l4;
        this.subscribeCount = l5;
        this.updateTime = l6;
        this.uploadTime = l7;
    }

    public VisitStatistic() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer num) {
        this.id = num;
        setValue();
    }

    public Integer getGuestId() {
        return this.guestId;
    }

    public void setGuestId(Integer num) {
        this.guestId = num;
        setValue();
    }

    public Integer getHostId() {
        return this.hostId;
    }

    public void setHostId(Integer num) {
        this.hostId = num;
        setValue();
    }

    public Integer getType() {
        return this.type;
    }

    public void setType(Integer num) {
        this.type = num;
        setValue();
    }

    public Long getInsertCount() {
        return this.insertCount;
    }

    public void setInsertCount(Long l) {
        this.insertCount = l;
        setValue();
    }

    public Long getUpdateCount() {
        return this.updateCount;
    }

    public void setUpdateCount(Long l) {
        this.updateCount = l;
        setValue();
    }

    public Long getDeleteCount() {
        return this.deleteCount;
    }

    public void setDeleteCount(Long l) {
        this.deleteCount = l;
        setValue();
    }

    public Long getQueryCount() {
        return this.queryCount;
    }

    public void setQueryCount(Long l) {
        this.queryCount = l;
        setValue();
    }

    public Long getSubscribeCount() {
        return this.subscribeCount;
    }

    public void setSubscribeCount(Long l) {
        this.subscribeCount = l;
        setValue();
    }

    public Long getUpdateTime() {
        return this.updateTime;
    }

    public void setUpdateTime(Long l) {
        this.updateTime = l;
        setValue();
    }

    public Long getUploadTime() {
        return this.uploadTime;
    }

    public void setUploadTime(Long l) {
        this.uploadTime = l;
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
        if (this.guestId != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.guestId.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.hostId != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.hostId.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.type != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.type.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.insertCount != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.insertCount.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.updateCount != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.updateCount.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.deleteCount != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.deleteCount.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.queryCount != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.queryCount.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.subscribeCount != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.subscribeCount.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.updateTime != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.updateTime.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.uploadTime != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.uploadTime.longValue());
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<VisitStatistic> getHelper() {
        return VisitStatisticHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "VisitStatistic { id: " + this.id + ", guestId: " + this.guestId + ", hostId: " + this.hostId + ", type: " + this.type + ", insertCount: " + this.insertCount + ", updateCount: " + this.updateCount + ", deleteCount: " + this.deleteCount + ", queryCount: " + this.queryCount + ", subscribeCount: " + this.subscribeCount + ", updateTime: " + this.updateTime + ", uploadTime: " + this.uploadTime + " }";
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
