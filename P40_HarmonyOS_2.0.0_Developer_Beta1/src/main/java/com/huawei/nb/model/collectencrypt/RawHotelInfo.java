package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RawHotelInfo extends AManagedObject {
    public static final Parcelable.Creator<RawHotelInfo> CREATOR = new Parcelable.Creator<RawHotelInfo>() {
        /* class com.huawei.nb.model.collectencrypt.RawHotelInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public RawHotelInfo createFromParcel(Parcel parcel) {
            return new RawHotelInfo(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public RawHotelInfo[] newArray(int i) {
            return new RawHotelInfo[i];
        }
    };
    private Date mCheckinTime;
    private String mHotelAddr;
    private String mHotelName;
    private String mHotelTelNo;
    private Integer mId;
    private Integer mReservedInt;
    private String mReservedText;
    private Date mTimeStamp;

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
        return "com.huawei.nb.model.collectencrypt.RawHotelInfo";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public RawHotelInfo(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Integer num = null;
        this.mId = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.mTimeStamp = cursor.isNull(2) ? null : new Date(cursor.getLong(2));
        this.mHotelTelNo = cursor.getString(3);
        this.mHotelAddr = cursor.getString(4);
        this.mHotelName = cursor.getString(5);
        this.mCheckinTime = cursor.isNull(6) ? null : new Date(cursor.getLong(6));
        this.mReservedInt = !cursor.isNull(7) ? Integer.valueOf(cursor.getInt(7)) : num;
        this.mReservedText = cursor.getString(8);
    }

    public RawHotelInfo(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.mId = null;
            parcel.readInt();
        } else {
            this.mId = Integer.valueOf(parcel.readInt());
        }
        this.mTimeStamp = parcel.readByte() == 0 ? null : new Date(parcel.readLong());
        this.mHotelTelNo = parcel.readByte() == 0 ? null : parcel.readString();
        this.mHotelAddr = parcel.readByte() == 0 ? null : parcel.readString();
        this.mHotelName = parcel.readByte() == 0 ? null : parcel.readString();
        this.mCheckinTime = parcel.readByte() == 0 ? null : new Date(parcel.readLong());
        this.mReservedInt = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mReservedText = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private RawHotelInfo(Integer num, Date date, String str, String str2, String str3, Date date2, Integer num2, String str4) {
        this.mId = num;
        this.mTimeStamp = date;
        this.mHotelTelNo = str;
        this.mHotelAddr = str2;
        this.mHotelName = str3;
        this.mCheckinTime = date2;
        this.mReservedInt = num2;
        this.mReservedText = str4;
    }

    public RawHotelInfo() {
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

    public String getMHotelTelNo() {
        return this.mHotelTelNo;
    }

    public void setMHotelTelNo(String str) {
        this.mHotelTelNo = str;
        setValue();
    }

    public String getMHotelAddr() {
        return this.mHotelAddr;
    }

    public void setMHotelAddr(String str) {
        this.mHotelAddr = str;
        setValue();
    }

    public String getMHotelName() {
        return this.mHotelName;
    }

    public void setMHotelName(String str) {
        this.mHotelName = str;
        setValue();
    }

    public Date getMCheckinTime() {
        return this.mCheckinTime;
    }

    public void setMCheckinTime(Date date) {
        this.mCheckinTime = date;
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
        if (this.mHotelTelNo != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mHotelTelNo);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mHotelAddr != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mHotelAddr);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mHotelName != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mHotelName);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mCheckinTime != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.mCheckinTime.getTime());
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
    public AEntityHelper<RawHotelInfo> getHelper() {
        return RawHotelInfoHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "RawHotelInfo { mId: " + this.mId + ", mTimeStamp: " + this.mTimeStamp + ", mHotelTelNo: " + this.mHotelTelNo + ", mHotelAddr: " + this.mHotelAddr + ", mHotelName: " + this.mHotelName + ", mCheckinTime: " + this.mCheckinTime + ", mReservedInt: " + this.mReservedInt + ", mReservedText: " + this.mReservedText + " }";
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
