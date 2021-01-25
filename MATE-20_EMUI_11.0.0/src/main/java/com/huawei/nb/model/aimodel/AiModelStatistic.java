package com.huawei.nb.model.aimodel;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class AiModelStatistic extends AManagedObject {
    public static final Parcelable.Creator<AiModelStatistic> CREATOR = new Parcelable.Creator<AiModelStatistic>() {
        /* class com.huawei.nb.model.aimodel.AiModelStatistic.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public AiModelStatistic createFromParcel(Parcel parcel) {
            return new AiModelStatistic(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public AiModelStatistic[] newArray(int i) {
            return new AiModelStatistic[i];
        }
    };
    private Long aimodel_id;
    private Long id;
    private String last_use_business;
    private Long last_use_time;
    private String reserved_1;
    private Integer use_count;

    @Override // com.huawei.odmf.core.AManagedObject, android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getDatabaseName() {
        return "dsAiModel";
    }

    public String getDatabaseVersion() {
        return "0.0.13";
    }

    public int getDatabaseVersionCode() {
        return 13;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getEntityName() {
        return "com.huawei.nb.model.aimodel.AiModelStatistic";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public AiModelStatistic(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Integer num = null;
        this.id = cursor.isNull(1) ? null : Long.valueOf(cursor.getLong(1));
        this.aimodel_id = cursor.isNull(2) ? null : Long.valueOf(cursor.getLong(2));
        this.last_use_time = cursor.isNull(3) ? null : Long.valueOf(cursor.getLong(3));
        this.last_use_business = cursor.getString(4);
        this.use_count = !cursor.isNull(5) ? Integer.valueOf(cursor.getInt(5)) : num;
        this.reserved_1 = cursor.getString(6);
    }

    public AiModelStatistic(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.id = null;
            parcel.readLong();
        } else {
            this.id = Long.valueOf(parcel.readLong());
        }
        this.aimodel_id = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.last_use_time = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.last_use_business = parcel.readByte() == 0 ? null : parcel.readString();
        this.use_count = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.reserved_1 = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private AiModelStatistic(Long l, Long l2, Long l3, String str, Integer num, String str2) {
        this.id = l;
        this.aimodel_id = l2;
        this.last_use_time = l3;
        this.last_use_business = str;
        this.use_count = num;
        this.reserved_1 = str2;
    }

    public AiModelStatistic() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long l) {
        this.id = l;
        setValue();
    }

    public Long getAimodel_id() {
        return this.aimodel_id;
    }

    public void setAimodel_id(Long l) {
        this.aimodel_id = l;
        setValue();
    }

    public Long getLast_use_time() {
        return this.last_use_time;
    }

    public void setLast_use_time(Long l) {
        this.last_use_time = l;
        setValue();
    }

    public String getLast_use_business() {
        return this.last_use_business;
    }

    public void setLast_use_business(String str) {
        this.last_use_business = str;
        setValue();
    }

    public Integer getUse_count() {
        return this.use_count;
    }

    public void setUse_count(Integer num) {
        this.use_count = num;
        setValue();
    }

    public String getReserved_1() {
        return this.reserved_1;
    }

    public void setReserved_1(String str) {
        this.reserved_1 = str;
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
        if (this.aimodel_id != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.aimodel_id.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.last_use_time != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.last_use_time.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.last_use_business != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.last_use_business);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.use_count != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.use_count.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.reserved_1 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.reserved_1);
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<AiModelStatistic> getHelper() {
        return AiModelStatisticHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "AiModelStatistic { id: " + this.id + ", aimodel_id: " + this.aimodel_id + ", last_use_time: " + this.last_use_time + ", last_use_business: " + this.last_use_business + ", use_count: " + this.use_count + ", reserved_1: " + this.reserved_1 + " }";
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
