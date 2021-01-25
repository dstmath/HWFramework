package com.huawei.nb.model.policy;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class ObservedGeoFence extends AManagedObject {
    public static final Parcelable.Creator<ObservedGeoFence> CREATOR = new Parcelable.Creator<ObservedGeoFence>() {
        /* class com.huawei.nb.model.policy.ObservedGeoFence.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ObservedGeoFence createFromParcel(Parcel parcel) {
            return new ObservedGeoFence(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public ObservedGeoFence[] newArray(int i) {
            return new ObservedGeoFence[i];
        }
    };
    private String mCategory;
    private String mFenceID;
    private String mGeoValue;
    private Long mID;
    private String mMaxTriggersPerDay;
    private String mName;
    private String mReserve;
    private String mSameFenceMaxTriggersPerDay;
    private String mSameFenceMinTriggerInterval;
    private Integer mShape;
    private Short mStatus;
    private String mSubCategory;
    private String mWorkTime;

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
        return "com.huawei.nb.model.policy.ObservedGeoFence";
    }

    public String getEntityVersion() {
        return "0.0.2";
    }

    public int getEntityVersionCode() {
        return 2;
    }

    public ObservedGeoFence(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Short sh = null;
        this.mID = cursor.isNull(1) ? null : Long.valueOf(cursor.getLong(1));
        this.mFenceID = cursor.getString(2);
        this.mName = cursor.getString(3);
        this.mCategory = cursor.getString(4);
        this.mSubCategory = cursor.getString(5);
        this.mShape = cursor.isNull(6) ? null : Integer.valueOf(cursor.getInt(6));
        this.mGeoValue = cursor.getString(7);
        this.mStatus = !cursor.isNull(8) ? Short.valueOf(cursor.getShort(8)) : sh;
        this.mReserve = cursor.getString(9);
        this.mWorkTime = cursor.getString(10);
        this.mSameFenceMaxTriggersPerDay = cursor.getString(11);
        this.mSameFenceMinTriggerInterval = cursor.getString(12);
        this.mMaxTriggersPerDay = cursor.getString(13);
    }

    public ObservedGeoFence(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.mID = null;
            parcel.readLong();
        } else {
            this.mID = Long.valueOf(parcel.readLong());
        }
        this.mFenceID = parcel.readByte() == 0 ? null : parcel.readString();
        this.mName = parcel.readByte() == 0 ? null : parcel.readString();
        this.mCategory = parcel.readByte() == 0 ? null : parcel.readString();
        this.mSubCategory = parcel.readByte() == 0 ? null : parcel.readString();
        this.mShape = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mGeoValue = parcel.readByte() == 0 ? null : parcel.readString();
        this.mStatus = parcel.readByte() == 0 ? null : Short.valueOf((short) parcel.readInt());
        this.mReserve = parcel.readByte() == 0 ? null : parcel.readString();
        this.mWorkTime = parcel.readByte() == 0 ? null : parcel.readString();
        this.mSameFenceMaxTriggersPerDay = parcel.readByte() == 0 ? null : parcel.readString();
        this.mSameFenceMinTriggerInterval = parcel.readByte() == 0 ? null : parcel.readString();
        this.mMaxTriggersPerDay = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private ObservedGeoFence(Long l, String str, String str2, String str3, String str4, Integer num, String str5, Short sh, String str6, String str7, String str8, String str9, String str10) {
        this.mID = l;
        this.mFenceID = str;
        this.mName = str2;
        this.mCategory = str3;
        this.mSubCategory = str4;
        this.mShape = num;
        this.mGeoValue = str5;
        this.mStatus = sh;
        this.mReserve = str6;
        this.mWorkTime = str7;
        this.mSameFenceMaxTriggersPerDay = str8;
        this.mSameFenceMinTriggerInterval = str9;
        this.mMaxTriggersPerDay = str10;
    }

    public ObservedGeoFence() {
    }

    public Long getMID() {
        return this.mID;
    }

    public void setMID(Long l) {
        this.mID = l;
        setValue();
    }

    public String getMFenceID() {
        return this.mFenceID;
    }

    public void setMFenceID(String str) {
        this.mFenceID = str;
        setValue();
    }

    public String getMName() {
        return this.mName;
    }

    public void setMName(String str) {
        this.mName = str;
        setValue();
    }

    public String getMCategory() {
        return this.mCategory;
    }

    public void setMCategory(String str) {
        this.mCategory = str;
        setValue();
    }

    public String getMSubCategory() {
        return this.mSubCategory;
    }

    public void setMSubCategory(String str) {
        this.mSubCategory = str;
        setValue();
    }

    public Integer getMShape() {
        return this.mShape;
    }

    public void setMShape(Integer num) {
        this.mShape = num;
        setValue();
    }

    public String getMGeoValue() {
        return this.mGeoValue;
    }

    public void setMGeoValue(String str) {
        this.mGeoValue = str;
        setValue();
    }

    public Short getMStatus() {
        return this.mStatus;
    }

    public void setMStatus(Short sh) {
        this.mStatus = sh;
        setValue();
    }

    public String getMReserve() {
        return this.mReserve;
    }

    public void setMReserve(String str) {
        this.mReserve = str;
        setValue();
    }

    public String getMWorkTime() {
        return this.mWorkTime;
    }

    public void setMWorkTime(String str) {
        this.mWorkTime = str;
        setValue();
    }

    public String getMSameFenceMaxTriggersPerDay() {
        return this.mSameFenceMaxTriggersPerDay;
    }

    public void setMSameFenceMaxTriggersPerDay(String str) {
        this.mSameFenceMaxTriggersPerDay = str;
        setValue();
    }

    public String getMSameFenceMinTriggerInterval() {
        return this.mSameFenceMinTriggerInterval;
    }

    public void setMSameFenceMinTriggerInterval(String str) {
        this.mSameFenceMinTriggerInterval = str;
        setValue();
    }

    public String getMMaxTriggersPerDay() {
        return this.mMaxTriggersPerDay;
    }

    public void setMMaxTriggersPerDay(String str) {
        this.mMaxTriggersPerDay = str;
        setValue();
    }

    @Override // com.huawei.odmf.core.AManagedObject, android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        if (this.mID != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.mID.longValue());
        } else {
            parcel.writeByte((byte) 0);
            parcel.writeLong(1);
        }
        if (this.mFenceID != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mFenceID);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mName != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mName);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mCategory != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mCategory);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mSubCategory != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mSubCategory);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mShape != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mShape.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mGeoValue != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mGeoValue);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mStatus != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mStatus.shortValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mReserve != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mReserve);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mWorkTime != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mWorkTime);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mSameFenceMaxTriggersPerDay != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mSameFenceMaxTriggersPerDay);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mSameFenceMinTriggerInterval != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mSameFenceMinTriggerInterval);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mMaxTriggersPerDay != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mMaxTriggersPerDay);
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<ObservedGeoFence> getHelper() {
        return ObservedGeoFenceHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "ObservedGeoFence { mID: " + this.mID + ", mFenceID: " + this.mFenceID + ", mName: " + this.mName + ", mCategory: " + this.mCategory + ", mSubCategory: " + this.mSubCategory + ", mShape: " + this.mShape + ", mGeoValue: " + this.mGeoValue + ", mStatus: " + this.mStatus + ", mReserve: " + this.mReserve + ", mWorkTime: " + this.mWorkTime + ", mSameFenceMaxTriggersPerDay: " + this.mSameFenceMaxTriggersPerDay + ", mSameFenceMinTriggerInterval: " + this.mSameFenceMinTriggerInterval + ", mMaxTriggersPerDay: " + this.mMaxTriggersPerDay + " }";
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
