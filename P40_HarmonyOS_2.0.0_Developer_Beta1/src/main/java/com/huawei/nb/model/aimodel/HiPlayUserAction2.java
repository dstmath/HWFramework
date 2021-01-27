package com.huawei.nb.model.aimodel;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class HiPlayUserAction2 extends AManagedObject {
    public static final Parcelable.Creator<HiPlayUserAction2> CREATOR = new Parcelable.Creator<HiPlayUserAction2>() {
        /* class com.huawei.nb.model.aimodel.HiPlayUserAction2.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public HiPlayUserAction2 createFromParcel(Parcel parcel) {
            return new HiPlayUserAction2(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public HiPlayUserAction2[] newArray(int i) {
            return new HiPlayUserAction2[i];
        }
    };
    private String business;
    private String content_main_type;
    private String device_id;
    private String device_type;
    private Long id;
    private String reserver1;
    private String reserver2;
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
        return "com.huawei.nb.model.aimodel.HiPlayUserAction2";
    }

    public String getEntityVersion() {
        return "0.0.2";
    }

    public int getEntityVersionCode() {
        return 2;
    }

    public HiPlayUserAction2(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Long l = null;
        this.id = cursor.isNull(1) ? null : Long.valueOf(cursor.getLong(1));
        this.business = cursor.getString(2);
        this.sub_business = cursor.getString(3);
        this.content_main_type = cursor.getString(4);
        this.device_id = cursor.getString(5);
        this.device_type = cursor.getString(6);
        this.reserver1 = cursor.getString(7);
        this.reserver2 = cursor.getString(8);
        this.timestamp = !cursor.isNull(9) ? Long.valueOf(cursor.getLong(9)) : l;
    }

    public HiPlayUserAction2(Parcel parcel) {
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
        this.content_main_type = parcel.readByte() == 0 ? null : parcel.readString();
        this.device_id = parcel.readByte() == 0 ? null : parcel.readString();
        this.device_type = parcel.readByte() == 0 ? null : parcel.readString();
        this.reserver1 = parcel.readByte() == 0 ? null : parcel.readString();
        this.reserver2 = parcel.readByte() == 0 ? null : parcel.readString();
        this.timestamp = parcel.readByte() != 0 ? Long.valueOf(parcel.readLong()) : l;
    }

    private HiPlayUserAction2(Long l, String str, String str2, String str3, String str4, String str5, String str6, String str7, Long l2) {
        this.id = l;
        this.business = str;
        this.sub_business = str2;
        this.content_main_type = str3;
        this.device_id = str4;
        this.device_type = str5;
        this.reserver1 = str6;
        this.reserver2 = str7;
        this.timestamp = l2;
    }

    public HiPlayUserAction2() {
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

    public String getContent_main_type() {
        return this.content_main_type;
    }

    public void setContent_main_type(String str) {
        this.content_main_type = str;
        setValue();
    }

    public String getDevice_id() {
        return this.device_id;
    }

    public void setDevice_id(String str) {
        this.device_id = str;
        setValue();
    }

    public String getDevice_type() {
        return this.device_type;
    }

    public void setDevice_type(String str) {
        this.device_type = str;
        setValue();
    }

    public String getReserver1() {
        return this.reserver1;
    }

    public void setReserver1(String str) {
        this.reserver1 = str;
        setValue();
    }

    public String getReserver2() {
        return this.reserver2;
    }

    public void setReserver2(String str) {
        this.reserver2 = str;
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
        if (this.content_main_type != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.content_main_type);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.device_id != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.device_id);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.device_type != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.device_type);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.reserver1 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.reserver1);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.reserver2 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.reserver2);
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
    public AEntityHelper<HiPlayUserAction2> getHelper() {
        return HiPlayUserAction2Helper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "HiPlayUserAction2 { id: " + this.id + ", business: " + this.business + ", sub_business: " + this.sub_business + ", content_main_type: " + this.content_main_type + ", device_id: " + this.device_id + ", device_type: " + this.device_type + ", reserver1: " + this.reserver1 + ", reserver2: " + this.reserver2 + ", timestamp: " + this.timestamp + " }";
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
