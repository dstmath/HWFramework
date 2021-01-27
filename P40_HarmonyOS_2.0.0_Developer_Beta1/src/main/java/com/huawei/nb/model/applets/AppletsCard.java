package com.huawei.nb.model.applets;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class AppletsCard extends AManagedObject {
    public static final Parcelable.Creator<AppletsCard> CREATOR = new Parcelable.Creator<AppletsCard>() {
        /* class com.huawei.nb.model.applets.AppletsCard.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public AppletsCard createFromParcel(Parcel parcel) {
            return new AppletsCard(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public AppletsCard[] newArray(int i) {
            return new AppletsCard[i];
        }
    };
    private String base_info;
    private String card_id;
    private Integer card_status;
    private String card_type;
    private Date life_cycle_date;
    private String routine_id;

    @Override // com.huawei.odmf.core.AManagedObject, android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getDatabaseName() {
        return "dsServiceMetaData";
    }

    public String getDatabaseVersion() {
        return "0.0.13";
    }

    public int getDatabaseVersionCode() {
        return 13;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getEntityName() {
        return "com.huawei.nb.model.applets.AppletsCard";
    }

    public String getEntityVersion() {
        return "0.0.2";
    }

    public int getEntityVersionCode() {
        return 2;
    }

    public AppletsCard(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.routine_id = cursor.getString(1);
        this.card_id = cursor.getString(2);
        Date date = null;
        this.card_status = cursor.isNull(3) ? null : Integer.valueOf(cursor.getInt(3));
        this.card_type = cursor.getString(4);
        this.base_info = cursor.getString(5);
        this.life_cycle_date = !cursor.isNull(6) ? new Date(cursor.getLong(6)) : date;
    }

    public AppletsCard(Parcel parcel) {
        super(parcel);
        Date date = null;
        this.routine_id = parcel.readByte() == 0 ? null : parcel.readString();
        this.card_id = parcel.readByte() == 0 ? null : parcel.readString();
        this.card_status = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.card_type = parcel.readByte() == 0 ? null : parcel.readString();
        this.base_info = parcel.readByte() == 0 ? null : parcel.readString();
        this.life_cycle_date = parcel.readByte() != 0 ? new Date(parcel.readLong()) : date;
    }

    private AppletsCard(String str, String str2, Integer num, String str3, String str4, Date date) {
        this.routine_id = str;
        this.card_id = str2;
        this.card_status = num;
        this.card_type = str3;
        this.base_info = str4;
        this.life_cycle_date = date;
    }

    public AppletsCard() {
    }

    public String getRoutine_id() {
        return this.routine_id;
    }

    public void setRoutine_id(String str) {
        this.routine_id = str;
        setValue();
    }

    public String getCard_id() {
        return this.card_id;
    }

    public void setCard_id(String str) {
        this.card_id = str;
        setValue();
    }

    public Integer getCard_status() {
        return this.card_status;
    }

    public void setCard_status(Integer num) {
        this.card_status = num;
        setValue();
    }

    public String getCard_type() {
        return this.card_type;
    }

    public void setCard_type(String str) {
        this.card_type = str;
        setValue();
    }

    public String getBase_info() {
        return this.base_info;
    }

    public void setBase_info(String str) {
        this.base_info = str;
        setValue();
    }

    public Date getLife_cycle_date() {
        return this.life_cycle_date;
    }

    public void setLife_cycle_date(Date date) {
        this.life_cycle_date = date;
        setValue();
    }

    @Override // com.huawei.odmf.core.AManagedObject, android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        if (this.routine_id != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.routine_id);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.card_id != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.card_id);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.card_status != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.card_status.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.card_type != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.card_type);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.base_info != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.base_info);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.life_cycle_date != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.life_cycle_date.getTime());
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<AppletsCard> getHelper() {
        return AppletsCardHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "AppletsCard { routine_id: " + this.routine_id + ", card_id: " + this.card_id + ", card_status: " + this.card_status + ", card_type: " + this.card_type + ", base_info: " + this.base_info + ", life_cycle_date: " + this.life_cycle_date + " }";
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
