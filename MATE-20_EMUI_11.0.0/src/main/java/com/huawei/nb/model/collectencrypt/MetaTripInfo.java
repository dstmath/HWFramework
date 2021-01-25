package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class MetaTripInfo extends AManagedObject {
    public static final Parcelable.Creator<MetaTripInfo> CREATOR = new Parcelable.Creator<MetaTripInfo>() {
        /* class com.huawei.nb.model.collectencrypt.MetaTripInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public MetaTripInfo createFromParcel(Parcel parcel) {
            return new MetaTripInfo(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public MetaTripInfo[] newArray(int i) {
            return new MetaTripInfo[i];
        }
    };
    private String mArrivalPlace;
    private Integer mId;
    private String mProvider;
    private Integer mReservedInt;
    private String mReservedText;
    private String mSeatNo;
    private String mStartPlace;
    private Date mStartTime;
    private Date mTimeStamp;
    private String mTripNo;
    private String mTripSeat;
    private String mTripType;

    @Override // com.huawei.odmf.core.AManagedObject, android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getDatabaseName() {
        return "dsCollectEncrypt";
    }

    public String getDatabaseVersion() {
        return "0.0.14";
    }

    public int getDatabaseVersionCode() {
        return 14;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getEntityName() {
        return "com.huawei.nb.model.collectencrypt.MetaTripInfo";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public MetaTripInfo(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Integer num = null;
        this.mId = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.mTimeStamp = cursor.isNull(2) ? null : new Date(cursor.getLong(2));
        this.mTripType = cursor.getString(3);
        this.mTripNo = cursor.getString(4);
        this.mSeatNo = cursor.getString(5);
        this.mStartTime = cursor.isNull(6) ? null : new Date(cursor.getLong(6));
        this.mStartPlace = cursor.getString(7);
        this.mArrivalPlace = cursor.getString(8);
        this.mProvider = cursor.getString(9);
        this.mTripSeat = cursor.getString(10);
        this.mReservedInt = !cursor.isNull(11) ? Integer.valueOf(cursor.getInt(11)) : num;
        this.mReservedText = cursor.getString(12);
    }

    public MetaTripInfo(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.mId = null;
            parcel.readInt();
        } else {
            this.mId = Integer.valueOf(parcel.readInt());
        }
        this.mTimeStamp = parcel.readByte() == 0 ? null : new Date(parcel.readLong());
        this.mTripType = parcel.readByte() == 0 ? null : parcel.readString();
        this.mTripNo = parcel.readByte() == 0 ? null : parcel.readString();
        this.mSeatNo = parcel.readByte() == 0 ? null : parcel.readString();
        this.mStartTime = parcel.readByte() == 0 ? null : new Date(parcel.readLong());
        this.mStartPlace = parcel.readByte() == 0 ? null : parcel.readString();
        this.mArrivalPlace = parcel.readByte() == 0 ? null : parcel.readString();
        this.mProvider = parcel.readByte() == 0 ? null : parcel.readString();
        this.mTripSeat = parcel.readByte() == 0 ? null : parcel.readString();
        this.mReservedInt = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mReservedText = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private MetaTripInfo(Integer num, Date date, String str, String str2, String str3, Date date2, String str4, String str5, String str6, String str7, Integer num2, String str8) {
        this.mId = num;
        this.mTimeStamp = date;
        this.mTripType = str;
        this.mTripNo = str2;
        this.mSeatNo = str3;
        this.mStartTime = date2;
        this.mStartPlace = str4;
        this.mArrivalPlace = str5;
        this.mProvider = str6;
        this.mTripSeat = str7;
        this.mReservedInt = num2;
        this.mReservedText = str8;
    }

    public MetaTripInfo() {
    }

    public Integer getMId() {
        return this.mId;
    }

    public void setMId(Integer num) {
        this.mId = num;
        setValue();
    }

    public Date getMTimeStamp() {
        return this.mTimeStamp;
    }

    public void setMTimeStamp(Date date) {
        this.mTimeStamp = date;
        setValue();
    }

    public String getMTripType() {
        return this.mTripType;
    }

    public void setMTripType(String str) {
        this.mTripType = str;
        setValue();
    }

    public String getMTripNo() {
        return this.mTripNo;
    }

    public void setMTripNo(String str) {
        this.mTripNo = str;
        setValue();
    }

    public String getMSeatNo() {
        return this.mSeatNo;
    }

    public void setMSeatNo(String str) {
        this.mSeatNo = str;
        setValue();
    }

    public Date getMStartTime() {
        return this.mStartTime;
    }

    public void setMStartTime(Date date) {
        this.mStartTime = date;
        setValue();
    }

    public String getMStartPlace() {
        return this.mStartPlace;
    }

    public void setMStartPlace(String str) {
        this.mStartPlace = str;
        setValue();
    }

    public String getMArrivalPlace() {
        return this.mArrivalPlace;
    }

    public void setMArrivalPlace(String str) {
        this.mArrivalPlace = str;
        setValue();
    }

    public String getMProvider() {
        return this.mProvider;
    }

    public void setMProvider(String str) {
        this.mProvider = str;
        setValue();
    }

    public String getMTripSeat() {
        return this.mTripSeat;
    }

    public void setMTripSeat(String str) {
        this.mTripSeat = str;
        setValue();
    }

    public Integer getMReservedInt() {
        return this.mReservedInt;
    }

    public void setMReservedInt(Integer num) {
        this.mReservedInt = num;
        setValue();
    }

    public String getMReservedText() {
        return this.mReservedText;
    }

    public void setMReservedText(String str) {
        this.mReservedText = str;
        setValue();
    }

    @Override // com.huawei.odmf.core.AManagedObject, android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        if (this.mId != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mId.intValue());
        } else {
            parcel.writeByte((byte) 0);
            parcel.writeInt(1);
        }
        if (this.mTimeStamp != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.mTimeStamp.getTime());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mTripType != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mTripType);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mTripNo != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mTripNo);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mSeatNo != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mSeatNo);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mStartTime != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.mStartTime.getTime());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mStartPlace != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mStartPlace);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mArrivalPlace != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mArrivalPlace);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mProvider != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mProvider);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mTripSeat != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mTripSeat);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mReservedInt != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mReservedInt.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mReservedText != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mReservedText);
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<MetaTripInfo> getHelper() {
        return MetaTripInfoHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "MetaTripInfo { mId: " + this.mId + ", mTimeStamp: " + this.mTimeStamp + ", mTripType: " + this.mTripType + ", mTripNo: " + this.mTripNo + ", mSeatNo: " + this.mSeatNo + ", mStartTime: " + this.mStartTime + ", mStartPlace: " + this.mStartPlace + ", mArrivalPlace: " + this.mArrivalPlace + ", mProvider: " + this.mProvider + ", mTripSeat: " + this.mTripSeat + ", mReservedInt: " + this.mReservedInt + ", mReservedText: " + this.mReservedText + " }";
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
