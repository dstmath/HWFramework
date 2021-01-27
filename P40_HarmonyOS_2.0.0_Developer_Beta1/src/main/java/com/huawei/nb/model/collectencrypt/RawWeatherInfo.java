package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RawWeatherInfo extends AManagedObject {
    public static final Parcelable.Creator<RawWeatherInfo> CREATOR = new Parcelable.Creator<RawWeatherInfo>() {
        /* class com.huawei.nb.model.collectencrypt.RawWeatherInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public RawWeatherInfo createFromParcel(Parcel parcel) {
            return new RawWeatherInfo(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public RawWeatherInfo[] newArray(int i) {
            return new RawWeatherInfo[i];
        }
    };
    private Integer mId;
    private Double mLatitude;
    private Double mLongitude;
    private Integer mReservedInt;
    private String mReservedText;
    private Integer mTemprature;
    private Date mTimeStamp;
    private Integer mWeatherIcon;

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
        return "com.huawei.nb.model.collectencrypt.RawWeatherInfo";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public RawWeatherInfo(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Integer num = null;
        this.mId = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.mTimeStamp = cursor.isNull(2) ? null : new Date(cursor.getLong(2));
        this.mLongitude = cursor.isNull(3) ? null : Double.valueOf(cursor.getDouble(3));
        this.mLatitude = cursor.isNull(4) ? null : Double.valueOf(cursor.getDouble(4));
        this.mWeatherIcon = cursor.isNull(5) ? null : Integer.valueOf(cursor.getInt(5));
        this.mTemprature = cursor.isNull(6) ? null : Integer.valueOf(cursor.getInt(6));
        this.mReservedInt = !cursor.isNull(7) ? Integer.valueOf(cursor.getInt(7)) : num;
        this.mReservedText = cursor.getString(8);
    }

    public RawWeatherInfo(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.mId = null;
            parcel.readInt();
        } else {
            this.mId = Integer.valueOf(parcel.readInt());
        }
        this.mTimeStamp = parcel.readByte() == 0 ? null : new Date(parcel.readLong());
        this.mLongitude = parcel.readByte() == 0 ? null : Double.valueOf(parcel.readDouble());
        this.mLatitude = parcel.readByte() == 0 ? null : Double.valueOf(parcel.readDouble());
        this.mWeatherIcon = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mTemprature = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mReservedInt = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mReservedText = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private RawWeatherInfo(Integer num, Date date, Double d, Double d2, Integer num2, Integer num3, Integer num4, String str) {
        this.mId = num;
        this.mTimeStamp = date;
        this.mLongitude = d;
        this.mLatitude = d2;
        this.mWeatherIcon = num2;
        this.mTemprature = num3;
        this.mReservedInt = num4;
        this.mReservedText = str;
    }

    public RawWeatherInfo() {
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

    public Double getMLongitude() {
        return this.mLongitude;
    }

    public void setMLongitude(Double d) {
        this.mLongitude = d;
        setValue();
    }

    public Double getMLatitude() {
        return this.mLatitude;
    }

    public void setMLatitude(Double d) {
        this.mLatitude = d;
        setValue();
    }

    public Integer getMWeatherIcon() {
        return this.mWeatherIcon;
    }

    public void setMWeatherIcon(Integer num) {
        this.mWeatherIcon = num;
        setValue();
    }

    public Integer getMTemprature() {
        return this.mTemprature;
    }

    public void setMTemprature(Integer num) {
        this.mTemprature = num;
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
        if (this.mLongitude != null) {
            parcel.writeByte((byte) 1);
            parcel.writeDouble(this.mLongitude.doubleValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mLatitude != null) {
            parcel.writeByte((byte) 1);
            parcel.writeDouble(this.mLatitude.doubleValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mWeatherIcon != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mWeatherIcon.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mTemprature != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mTemprature.intValue());
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
    public AEntityHelper<RawWeatherInfo> getHelper() {
        return RawWeatherInfoHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "RawWeatherInfo { mId: " + this.mId + ", mTimeStamp: " + this.mTimeStamp + ", mLongitude: " + this.mLongitude + ", mLatitude: " + this.mLatitude + ", mWeatherIcon: " + this.mWeatherIcon + ", mTemprature: " + this.mTemprature + ", mReservedInt: " + this.mReservedInt + ", mReservedText: " + this.mReservedText + " }";
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
