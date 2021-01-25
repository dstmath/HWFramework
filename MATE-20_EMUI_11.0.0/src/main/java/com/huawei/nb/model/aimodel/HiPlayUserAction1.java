package com.huawei.nb.model.aimodel;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class HiPlayUserAction1 extends AManagedObject {
    public static final Parcelable.Creator<HiPlayUserAction1> CREATOR = new Parcelable.Creator<HiPlayUserAction1>() {
        /* class com.huawei.nb.model.aimodel.HiPlayUserAction1.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public HiPlayUserAction1 createFromParcel(Parcel parcel) {
            return new HiPlayUserAction1(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public HiPlayUserAction1[] newArray(int i) {
            return new HiPlayUserAction1[i];
        }
    };
    private String business;
    private Long id;
    private String sub_business;
    private Long timestamp;

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
        return "com.huawei.nb.model.aimodel.HiPlayUserAction1";
    }

    public String getEntityVersion() {
        return "0.0.2";
    }

    public int getEntityVersionCode() {
        return 2;
    }

    public HiPlayUserAction1(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Long l = null;
        this.id = cursor.isNull(1) ? null : Long.valueOf(cursor.getLong(1));
        this.business = cursor.getString(2);
        this.sub_business = cursor.getString(3);
        this.timestamp = !cursor.isNull(4) ? Long.valueOf(cursor.getLong(4)) : l;
    }

    public HiPlayUserAction1(Parcel parcel) {
        super(parcel);
        Long l = null;
        if (parcel.readByte() == 0) {
            this.id = null;
            parcel.readLong();
        } else {
            this.id = Long.valueOf(parcel.readLong());
        }
        this.business = parcel.readByte() == 0 ? null : parcel.readString();
        this.sub_business = parcel.readByte() == 0 ? null : parcel.readString();
        this.timestamp = parcel.readByte() != 0 ? Long.valueOf(parcel.readLong()) : l;
    }

    private HiPlayUserAction1(Long l, String str, String str2, Long l2) {
        this.id = l;
        this.business = str;
        this.sub_business = str2;
        this.timestamp = l2;
    }

    public HiPlayUserAction1() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long l) {
        this.id = l;
        setValue();
    }

    public String getBusiness() {
        return this.business;
    }

    public void setBusiness(String str) {
        this.business = str;
        setValue();
    }

    public String getSub_business() {
        return this.sub_business;
    }

    public void setSub_business(String str) {
        this.sub_business = str;
        setValue();
    }

    public Long getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(Long l) {
        this.timestamp = l;
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
        if (this.business != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.business);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.sub_business != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.sub_business);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.timestamp != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.timestamp.longValue());
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<HiPlayUserAction1> getHelper() {
        return HiPlayUserAction1Helper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "HiPlayUserAction1 { id: " + this.id + ", business: " + this.business + ", sub_business: " + this.sub_business + ", timestamp: " + this.timestamp + " }";
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
