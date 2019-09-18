package com.huawei.nb.model.search;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class IndexSearchStatistic extends AManagedObject {
    public static final Parcelable.Creator<IndexSearchStatistic> CREATOR = new Parcelable.Creator<IndexSearchStatistic>() {
        public IndexSearchStatistic createFromParcel(Parcel in) {
            return new IndexSearchStatistic(in);
        }

        public IndexSearchStatistic[] newArray(int size) {
            return new IndexSearchStatistic[size];
        }
    };
    private Integer appId;
    private Integer avgExecTime;
    private String description;
    private Integer eventNum;
    private Integer eventType;
    private Boolean hasReported;
    private Integer id;
    private Integer indexDatabaseSize;
    private Integer lifeTime;
    private Integer maxExecTime;
    private Integer minExecTime;
    private Integer operateType;
    private Long reportTime;

    public IndexSearchStatistic(Cursor cursor) {
        Boolean bool = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.id = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.appId = cursor.isNull(2) ? null : Integer.valueOf(cursor.getInt(2));
        this.eventType = cursor.isNull(3) ? null : Integer.valueOf(cursor.getInt(3));
        this.operateType = cursor.isNull(4) ? null : Integer.valueOf(cursor.getInt(4));
        this.eventNum = cursor.isNull(5) ? null : Integer.valueOf(cursor.getInt(5));
        this.maxExecTime = cursor.isNull(6) ? null : Integer.valueOf(cursor.getInt(6));
        this.minExecTime = cursor.isNull(7) ? null : Integer.valueOf(cursor.getInt(7));
        this.avgExecTime = cursor.isNull(8) ? null : Integer.valueOf(cursor.getInt(8));
        this.indexDatabaseSize = cursor.isNull(9) ? null : Integer.valueOf(cursor.getInt(9));
        this.lifeTime = cursor.isNull(10) ? null : Integer.valueOf(cursor.getInt(10));
        this.description = cursor.getString(11);
        this.reportTime = cursor.isNull(12) ? null : Long.valueOf(cursor.getLong(12));
        if (!cursor.isNull(13)) {
            bool = Boolean.valueOf(cursor.getInt(13) != 0);
        }
        this.hasReported = bool;
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public IndexSearchStatistic(Parcel in) {
        super(in);
        Boolean bool = null;
        if (in.readByte() == 0) {
            this.id = null;
            in.readInt();
        } else {
            this.id = Integer.valueOf(in.readInt());
        }
        this.appId = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.eventType = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.operateType = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.eventNum = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.maxExecTime = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.minExecTime = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.avgExecTime = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.indexDatabaseSize = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.lifeTime = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.description = in.readByte() == 0 ? null : in.readString();
        this.reportTime = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        if (in.readByte() != 0) {
            bool = Boolean.valueOf(in.readByte() != 0);
        }
        this.hasReported = bool;
    }

    private IndexSearchStatistic(Integer id2, Integer appId2, Integer eventType2, Integer operateType2, Integer eventNum2, Integer maxExecTime2, Integer minExecTime2, Integer avgExecTime2, Integer indexDatabaseSize2, Integer lifeTime2, String description2, Long reportTime2, Boolean hasReported2) {
        this.id = id2;
        this.appId = appId2;
        this.eventType = eventType2;
        this.operateType = operateType2;
        this.eventNum = eventNum2;
        this.maxExecTime = maxExecTime2;
        this.minExecTime = minExecTime2;
        this.avgExecTime = avgExecTime2;
        this.indexDatabaseSize = indexDatabaseSize2;
        this.lifeTime = lifeTime2;
        this.description = description2;
        this.reportTime = reportTime2;
        this.hasReported = hasReported2;
    }

    public IndexSearchStatistic() {
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

    public Integer getAppId() {
        return this.appId;
    }

    public void setAppId(Integer appId2) {
        this.appId = appId2;
        setValue();
    }

    public Integer getEventType() {
        return this.eventType;
    }

    public void setEventType(Integer eventType2) {
        this.eventType = eventType2;
        setValue();
    }

    public Integer getOperateType() {
        return this.operateType;
    }

    public void setOperateType(Integer operateType2) {
        this.operateType = operateType2;
        setValue();
    }

    public Integer getEventNum() {
        return this.eventNum;
    }

    public void setEventNum(Integer eventNum2) {
        this.eventNum = eventNum2;
        setValue();
    }

    public Integer getMaxExecTime() {
        return this.maxExecTime;
    }

    public void setMaxExecTime(Integer maxExecTime2) {
        this.maxExecTime = maxExecTime2;
        setValue();
    }

    public Integer getMinExecTime() {
        return this.minExecTime;
    }

    public void setMinExecTime(Integer minExecTime2) {
        this.minExecTime = minExecTime2;
        setValue();
    }

    public Integer getAvgExecTime() {
        return this.avgExecTime;
    }

    public void setAvgExecTime(Integer avgExecTime2) {
        this.avgExecTime = avgExecTime2;
        setValue();
    }

    public Integer getIndexDatabaseSize() {
        return this.indexDatabaseSize;
    }

    public void setIndexDatabaseSize(Integer indexDatabaseSize2) {
        this.indexDatabaseSize = indexDatabaseSize2;
        setValue();
    }

    public Integer getLifeTime() {
        return this.lifeTime;
    }

    public void setLifeTime(Integer lifeTime2) {
        this.lifeTime = lifeTime2;
        setValue();
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description2) {
        this.description = description2;
        setValue();
    }

    public Long getReportTime() {
        return this.reportTime;
    }

    public void setReportTime(Long reportTime2) {
        this.reportTime = reportTime2;
        setValue();
    }

    public Boolean getHasReported() {
        return this.hasReported;
    }

    public void setHasReported(Boolean hasReported2) {
        this.hasReported = hasReported2;
        setValue();
    }

    public void writeToParcel(Parcel out, int ignored) {
        byte b = 1;
        super.writeToParcel(out, ignored);
        if (this.id != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.id.intValue());
        } else {
            out.writeByte((byte) 0);
            out.writeInt(1);
        }
        if (this.appId != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.appId.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.eventType != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.eventType.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.operateType != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.operateType.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.eventNum != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.eventNum.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.maxExecTime != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.maxExecTime.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.minExecTime != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.minExecTime.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.avgExecTime != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.avgExecTime.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.indexDatabaseSize != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.indexDatabaseSize.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.lifeTime != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.lifeTime.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.description != null) {
            out.writeByte((byte) 1);
            out.writeString(this.description);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.reportTime != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.reportTime.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.hasReported != null) {
            out.writeByte((byte) 1);
            if (!this.hasReported.booleanValue()) {
                b = 0;
            }
            out.writeByte(b);
            return;
        }
        out.writeByte((byte) 0);
    }

    public AEntityHelper<IndexSearchStatistic> getHelper() {
        return IndexSearchStatisticHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.search.IndexSearchStatistic";
    }

    public String getDatabaseName() {
        return "dsSearch";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("IndexSearchStatistic { id: ").append(this.id);
        sb.append(", appId: ").append(this.appId);
        sb.append(", eventType: ").append(this.eventType);
        sb.append(", operateType: ").append(this.operateType);
        sb.append(", eventNum: ").append(this.eventNum);
        sb.append(", maxExecTime: ").append(this.maxExecTime);
        sb.append(", minExecTime: ").append(this.minExecTime);
        sb.append(", avgExecTime: ").append(this.avgExecTime);
        sb.append(", indexDatabaseSize: ").append(this.indexDatabaseSize);
        sb.append(", lifeTime: ").append(this.lifeTime);
        sb.append(", description: ").append(this.description);
        sb.append(", reportTime: ").append(this.reportTime);
        sb.append(", hasReported: ").append(this.hasReported);
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
        return "0.0.7";
    }

    public int getDatabaseVersionCode() {
        return 7;
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }
}
