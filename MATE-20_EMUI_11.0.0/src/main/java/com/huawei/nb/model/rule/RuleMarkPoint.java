package com.huawei.nb.model.rule;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RuleMarkPoint extends AManagedObject {
    public static final Parcelable.Creator<RuleMarkPoint> CREATOR = new Parcelable.Creator<RuleMarkPoint>() {
        /* class com.huawei.nb.model.rule.RuleMarkPoint.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public RuleMarkPoint createFromParcel(Parcel parcel) {
            return new RuleMarkPoint(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public RuleMarkPoint[] newArray(int i) {
            return new RuleMarkPoint[i];
        }
    };
    private String businessName;
    private Integer category;
    private Long id;
    private String itemName;
    private String operatorName;
    private Integer recommendedCount;
    private String ruleName;
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
        return "com.huawei.nb.model.rule.RuleMarkPoint";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public RuleMarkPoint(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Date date = null;
        this.id = cursor.isNull(1) ? null : Long.valueOf(cursor.getLong(1));
        this.ruleName = cursor.getString(2);
        this.businessName = cursor.getString(3);
        this.operatorName = cursor.getString(4);
        this.itemName = cursor.getString(5);
        this.recommendedCount = cursor.isNull(6) ? null : Integer.valueOf(cursor.getInt(6));
        this.category = cursor.isNull(7) ? null : Integer.valueOf(cursor.getInt(7));
        this.timeStamp = !cursor.isNull(8) ? new Date(cursor.getLong(8)) : date;
    }

    public RuleMarkPoint(Parcel parcel) {
        super(parcel);
        Date date = null;
        if (parcel.readByte() == 0) {
            this.id = null;
            parcel.readLong();
        } else {
            this.id = Long.valueOf(parcel.readLong());
        }
        this.ruleName = parcel.readByte() == 0 ? null : parcel.readString();
        this.businessName = parcel.readByte() == 0 ? null : parcel.readString();
        this.operatorName = parcel.readByte() == 0 ? null : parcel.readString();
        this.itemName = parcel.readByte() == 0 ? null : parcel.readString();
        this.recommendedCount = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.category = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.timeStamp = parcel.readByte() != 0 ? new Date(parcel.readLong()) : date;
    }

    private RuleMarkPoint(Long l, String str, String str2, String str3, String str4, Integer num, Integer num2, Date date) {
        this.id = l;
        this.ruleName = str;
        this.businessName = str2;
        this.operatorName = str3;
        this.itemName = str4;
        this.recommendedCount = num;
        this.category = num2;
        this.timeStamp = date;
    }

    public RuleMarkPoint() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long l) {
        this.id = l;
        setValue();
    }

    public String getRuleName() {
        return this.ruleName;
    }

    public void setRuleName(String str) {
        this.ruleName = str;
        setValue();
    }

    public String getBusinessName() {
        return this.businessName;
    }

    public void setBusinessName(String str) {
        this.businessName = str;
        setValue();
    }

    public String getOperatorName() {
        return this.operatorName;
    }

    public void setOperatorName(String str) {
        this.operatorName = str;
        setValue();
    }

    public String getItemName() {
        return this.itemName;
    }

    public void setItemName(String str) {
        this.itemName = str;
        setValue();
    }

    public Integer getRecommendedCount() {
        return this.recommendedCount;
    }

    public void setRecommendedCount(Integer num) {
        this.recommendedCount = num;
        setValue();
    }

    public Integer getCategory() {
        return this.category;
    }

    public void setCategory(Integer num) {
        this.category = num;
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
        if (this.ruleName != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.ruleName);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.businessName != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.businessName);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.operatorName != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.operatorName);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.itemName != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.itemName);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.recommendedCount != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.recommendedCount.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.category != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.category.intValue());
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
    public AEntityHelper<RuleMarkPoint> getHelper() {
        return RuleMarkPointHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "RuleMarkPoint { id: " + this.id + ", ruleName: " + this.ruleName + ", businessName: " + this.businessName + ", operatorName: " + this.operatorName + ", itemName: " + this.itemName + ", recommendedCount: " + this.recommendedCount + ", category: " + this.category + ", timeStamp: " + this.timeStamp + " }";
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
