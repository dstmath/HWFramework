package com.huawei.nb.model.rule;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class Recommendation extends AManagedObject {
    public static final Parcelable.Creator<Recommendation> CREATOR = new Parcelable.Creator<Recommendation>() {
        /* class com.huawei.nb.model.rule.Recommendation.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public Recommendation createFromParcel(Parcel parcel) {
            return new Recommendation(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public Recommendation[] newArray(int i) {
            return new Recommendation[i];
        }
    };
    private Long businessId;
    private Long id;
    private Long itemId;
    private String message;
    private Long ruleId;
    private Date timeStamp;

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
        return "com.huawei.nb.model.rule.Recommendation";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public Recommendation(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Date date = null;
        this.id = cursor.isNull(1) ? null : Long.valueOf(cursor.getLong(1));
        this.businessId = cursor.isNull(2) ? null : Long.valueOf(cursor.getLong(2));
        this.itemId = cursor.isNull(3) ? null : Long.valueOf(cursor.getLong(3));
        this.ruleId = cursor.isNull(4) ? null : Long.valueOf(cursor.getLong(4));
        this.message = cursor.getString(5);
        this.timeStamp = !cursor.isNull(6) ? new Date(cursor.getLong(6)) : date;
    }

    public Recommendation(Parcel parcel) {
        super(parcel);
        Date date = null;
        if (parcel.readByte() == 0) {
            this.id = null;
            parcel.readLong();
        } else {
            this.id = Long.valueOf(parcel.readLong());
        }
        this.businessId = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.itemId = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.ruleId = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.message = parcel.readByte() == 0 ? null : parcel.readString();
        this.timeStamp = parcel.readByte() != 0 ? new Date(parcel.readLong()) : date;
    }

    private Recommendation(Long l, Long l2, Long l3, Long l4, String str, Date date) {
        this.id = l;
        this.businessId = l2;
        this.itemId = l3;
        this.ruleId = l4;
        this.message = str;
        this.timeStamp = date;
    }

    public Recommendation() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long l) {
        this.id = l;
        setValue();
    }

    public Long getBusinessId() {
        return this.businessId;
    }

    public void setBusinessId(Long l) {
        this.businessId = l;
        setValue();
    }

    public Long getItemId() {
        return this.itemId;
    }

    public void setItemId(Long l) {
        this.itemId = l;
        setValue();
    }

    public Long getRuleId() {
        return this.ruleId;
    }

    public void setRuleId(Long l) {
        this.ruleId = l;
        setValue();
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String str) {
        this.message = str;
        setValue();
    }

    public Date getTimeStamp() {
        return this.timeStamp;
    }

    public void setTimeStamp(Date date) {
        this.timeStamp = date;
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
        if (this.businessId != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.businessId.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.itemId != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.itemId.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.ruleId != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.ruleId.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.message != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.message);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.timeStamp != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.timeStamp.getTime());
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<Recommendation> getHelper() {
        return RecommendationHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "Recommendation { id: " + this.id + ", businessId: " + this.businessId + ", itemId: " + this.itemId + ", ruleId: " + this.ruleId + ", message: " + this.message + ", timeStamp: " + this.timeStamp + " }";
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
