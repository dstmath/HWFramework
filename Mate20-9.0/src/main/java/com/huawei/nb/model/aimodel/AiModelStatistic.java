package com.huawei.nb.model.aimodel;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class AiModelStatistic extends AManagedObject {
    public static final Parcelable.Creator<AiModelStatistic> CREATOR = new Parcelable.Creator<AiModelStatistic>() {
        public AiModelStatistic createFromParcel(Parcel in) {
            return new AiModelStatistic(in);
        }

        public AiModelStatistic[] newArray(int size) {
            return new AiModelStatistic[size];
        }
    };
    private Long aimodel_id;
    private Long id;
    private String last_use_business;
    private Long last_use_time;
    private String reserved_1;
    private Integer use_count;

    public AiModelStatistic(Cursor cursor) {
        Integer num = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.id = cursor.isNull(1) ? null : Long.valueOf(cursor.getLong(1));
        this.aimodel_id = cursor.isNull(2) ? null : Long.valueOf(cursor.getLong(2));
        this.last_use_time = cursor.isNull(3) ? null : Long.valueOf(cursor.getLong(3));
        this.last_use_business = cursor.getString(4);
        this.use_count = !cursor.isNull(5) ? Integer.valueOf(cursor.getInt(5)) : num;
        this.reserved_1 = cursor.getString(6);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public AiModelStatistic(Parcel in) {
        super(in);
        String str = null;
        if (in.readByte() == 0) {
            this.id = null;
            in.readLong();
        } else {
            this.id = Long.valueOf(in.readLong());
        }
        this.aimodel_id = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.last_use_time = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.last_use_business = in.readByte() == 0 ? null : in.readString();
        this.use_count = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.reserved_1 = in.readByte() != 0 ? in.readString() : str;
    }

    private AiModelStatistic(Long id2, Long aimodel_id2, Long last_use_time2, String last_use_business2, Integer use_count2, String reserved_12) {
        this.id = id2;
        this.aimodel_id = aimodel_id2;
        this.last_use_time = last_use_time2;
        this.last_use_business = last_use_business2;
        this.use_count = use_count2;
        this.reserved_1 = reserved_12;
    }

    public AiModelStatistic() {
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

    public Long getAimodel_id() {
        return this.aimodel_id;
    }

    public void setAimodel_id(Long aimodel_id2) {
        this.aimodel_id = aimodel_id2;
        setValue();
    }

    public Long getLast_use_time() {
        return this.last_use_time;
    }

    public void setLast_use_time(Long last_use_time2) {
        this.last_use_time = last_use_time2;
        setValue();
    }

    public String getLast_use_business() {
        return this.last_use_business;
    }

    public void setLast_use_business(String last_use_business2) {
        this.last_use_business = last_use_business2;
        setValue();
    }

    public Integer getUse_count() {
        return this.use_count;
    }

    public void setUse_count(Integer use_count2) {
        this.use_count = use_count2;
        setValue();
    }

    public String getReserved_1() {
        return this.reserved_1;
    }

    public void setReserved_1(String reserved_12) {
        this.reserved_1 = reserved_12;
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
        if (this.aimodel_id != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.aimodel_id.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.last_use_time != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.last_use_time.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.last_use_business != null) {
            out.writeByte((byte) 1);
            out.writeString(this.last_use_business);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.use_count != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.use_count.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.reserved_1 != null) {
            out.writeByte((byte) 1);
            out.writeString(this.reserved_1);
            return;
        }
        out.writeByte((byte) 0);
    }

    public AEntityHelper<AiModelStatistic> getHelper() {
        return AiModelStatisticHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.aimodel.AiModelStatistic";
    }

    public String getDatabaseName() {
        return "dsAiModel";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("AiModelStatistic { id: ").append(this.id);
        sb.append(", aimodel_id: ").append(this.aimodel_id);
        sb.append(", last_use_time: ").append(this.last_use_time);
        sb.append(", last_use_business: ").append(this.last_use_business);
        sb.append(", use_count: ").append(this.use_count);
        sb.append(", reserved_1: ").append(this.reserved_1);
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
        return "0.0.11";
    }

    public int getDatabaseVersionCode() {
        return 11;
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }
}
