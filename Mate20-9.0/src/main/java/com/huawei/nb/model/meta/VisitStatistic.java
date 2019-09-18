package com.huawei.nb.model.meta;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class VisitStatistic extends AManagedObject {
    public static final Parcelable.Creator<VisitStatistic> CREATOR = new Parcelable.Creator<VisitStatistic>() {
        public VisitStatistic createFromParcel(Parcel in) {
            return new VisitStatistic(in);
        }

        public VisitStatistic[] newArray(int size) {
            return new VisitStatistic[size];
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

    public VisitStatistic(Cursor cursor) {
        Long l = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
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

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public VisitStatistic(Parcel in) {
        super(in);
        Long l = null;
        if (in.readByte() == 0) {
            this.id = null;
            in.readInt();
        } else {
            this.id = Integer.valueOf(in.readInt());
        }
        this.guestId = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.hostId = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.type = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.insertCount = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.updateCount = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.deleteCount = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.queryCount = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.subscribeCount = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.updateTime = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.uploadTime = in.readByte() != 0 ? Long.valueOf(in.readLong()) : l;
    }

    private VisitStatistic(Integer id2, Integer guestId2, Integer hostId2, Integer type2, Long insertCount2, Long updateCount2, Long deleteCount2, Long queryCount2, Long subscribeCount2, Long updateTime2, Long uploadTime2) {
        this.id = id2;
        this.guestId = guestId2;
        this.hostId = hostId2;
        this.type = type2;
        this.insertCount = insertCount2;
        this.updateCount = updateCount2;
        this.deleteCount = deleteCount2;
        this.queryCount = queryCount2;
        this.subscribeCount = subscribeCount2;
        this.updateTime = updateTime2;
        this.uploadTime = uploadTime2;
    }

    public VisitStatistic() {
    }

    public int describeContents() {
        return 0;
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id2) {
        this.id = id2;
        setValue();
    }

    public Integer getGuestId() {
        return this.guestId;
    }

    public void setGuestId(Integer guestId2) {
        this.guestId = guestId2;
        setValue();
    }

    public Integer getHostId() {
        return this.hostId;
    }

    public void setHostId(Integer hostId2) {
        this.hostId = hostId2;
        setValue();
    }

    public Integer getType() {
        return this.type;
    }

    public void setType(Integer type2) {
        this.type = type2;
        setValue();
    }

    public Long getInsertCount() {
        return this.insertCount;
    }

    public void setInsertCount(Long insertCount2) {
        this.insertCount = insertCount2;
        setValue();
    }

    public Long getUpdateCount() {
        return this.updateCount;
    }

    public void setUpdateCount(Long updateCount2) {
        this.updateCount = updateCount2;
        setValue();
    }

    public Long getDeleteCount() {
        return this.deleteCount;
    }

    public void setDeleteCount(Long deleteCount2) {
        this.deleteCount = deleteCount2;
        setValue();
    }

    public Long getQueryCount() {
        return this.queryCount;
    }

    public void setQueryCount(Long queryCount2) {
        this.queryCount = queryCount2;
        setValue();
    }

    public Long getSubscribeCount() {
        return this.subscribeCount;
    }

    public void setSubscribeCount(Long subscribeCount2) {
        this.subscribeCount = subscribeCount2;
        setValue();
    }

    public Long getUpdateTime() {
        return this.updateTime;
    }

    public void setUpdateTime(Long updateTime2) {
        this.updateTime = updateTime2;
        setValue();
    }

    public Long getUploadTime() {
        return this.uploadTime;
    }

    public void setUploadTime(Long uploadTime2) {
        this.uploadTime = uploadTime2;
        setValue();
    }

    public void writeToParcel(Parcel out, int ignored) {
        super.writeToParcel(out, ignored);
        if (this.id != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.id.intValue());
        } else {
            out.writeByte((byte) 0);
            out.writeInt(1);
        }
        if (this.guestId != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.guestId.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.hostId != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.hostId.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.type != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.type.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.insertCount != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.insertCount.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.updateCount != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.updateCount.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.deleteCount != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.deleteCount.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.queryCount != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.queryCount.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.subscribeCount != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.subscribeCount.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.updateTime != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.updateTime.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.uploadTime != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.uploadTime.longValue());
            return;
        }
        out.writeByte((byte) 0);
    }

    public AEntityHelper<VisitStatistic> getHelper() {
        return VisitStatisticHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.meta.VisitStatistic";
    }

    public String getDatabaseName() {
        return "dsMeta";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("VisitStatistic { id: ").append(this.id);
        sb.append(", guestId: ").append(this.guestId);
        sb.append(", hostId: ").append(this.hostId);
        sb.append(", type: ").append(this.type);
        sb.append(", insertCount: ").append(this.insertCount);
        sb.append(", updateCount: ").append(this.updateCount);
        sb.append(", deleteCount: ").append(this.deleteCount);
        sb.append(", queryCount: ").append(this.queryCount);
        sb.append(", subscribeCount: ").append(this.subscribeCount);
        sb.append(", updateTime: ").append(this.updateTime);
        sb.append(", uploadTime: ").append(this.uploadTime);
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
