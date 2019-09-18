package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RawWeatherInfo extends AManagedObject {
    public static final Parcelable.Creator<RawWeatherInfo> CREATOR = new Parcelable.Creator<RawWeatherInfo>() {
        public RawWeatherInfo createFromParcel(Parcel in) {
            return new RawWeatherInfo(in);
        }

        public RawWeatherInfo[] newArray(int size) {
            return new RawWeatherInfo[size];
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

    public RawWeatherInfo(Cursor cursor) {
        Integer num = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.mId = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.mTimeStamp = cursor.isNull(2) ? null : new Date(cursor.getLong(2));
        this.mLongitude = cursor.isNull(3) ? null : Double.valueOf(cursor.getDouble(3));
        this.mLatitude = cursor.isNull(4) ? null : Double.valueOf(cursor.getDouble(4));
        this.mWeatherIcon = cursor.isNull(5) ? null : Integer.valueOf(cursor.getInt(5));
        this.mTemprature = cursor.isNull(6) ? null : Integer.valueOf(cursor.getInt(6));
        this.mReservedInt = !cursor.isNull(7) ? Integer.valueOf(cursor.getInt(7)) : num;
        this.mReservedText = cursor.getString(8);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public RawWeatherInfo(Parcel in) {
        super(in);
        String str = null;
        if (in.readByte() == 0) {
            this.mId = null;
            in.readInt();
        } else {
            this.mId = Integer.valueOf(in.readInt());
        }
        this.mTimeStamp = in.readByte() == 0 ? null : new Date(in.readLong());
        this.mLongitude = in.readByte() == 0 ? null : Double.valueOf(in.readDouble());
        this.mLatitude = in.readByte() == 0 ? null : Double.valueOf(in.readDouble());
        this.mWeatherIcon = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mTemprature = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mReservedInt = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mReservedText = in.readByte() != 0 ? in.readString() : str;
    }

    private RawWeatherInfo(Integer mId2, Date mTimeStamp2, Double mLongitude2, Double mLatitude2, Integer mWeatherIcon2, Integer mTemprature2, Integer mReservedInt2, String mReservedText2) {
        this.mId = mId2;
        this.mTimeStamp = mTimeStamp2;
        this.mLongitude = mLongitude2;
        this.mLatitude = mLatitude2;
        this.mWeatherIcon = mWeatherIcon2;
        this.mTemprature = mTemprature2;
        this.mReservedInt = mReservedInt2;
        this.mReservedText = mReservedText2;
    }

    public RawWeatherInfo() {
    }

    public int describeContents() {
        return 0;
    }

    public Integer getMId() {
        return this.mId;
    }

    public void setMId(Integer mId2) {
        this.mId = mId2;
        setValue();
    }

    public Date getMTimeStamp() {
        return this.mTimeStamp;
    }

    public void setMTimeStamp(Date mTimeStamp2) {
        this.mTimeStamp = mTimeStamp2;
        setValue();
    }

    public Double getMLongitude() {
        return this.mLongitude;
    }

    public void setMLongitude(Double mLongitude2) {
        this.mLongitude = mLongitude2;
        setValue();
    }

    public Double getMLatitude() {
        return this.mLatitude;
    }

    public void setMLatitude(Double mLatitude2) {
        this.mLatitude = mLatitude2;
        setValue();
    }

    public Integer getMWeatherIcon() {
        return this.mWeatherIcon;
    }

    public void setMWeatherIcon(Integer mWeatherIcon2) {
        this.mWeatherIcon = mWeatherIcon2;
        setValue();
    }

    public Integer getMTemprature() {
        return this.mTemprature;
    }

    public void setMTemprature(Integer mTemprature2) {
        this.mTemprature = mTemprature2;
        setValue();
    }

    public Integer getMReservedInt() {
        return this.mReservedInt;
    }

    public void setMReservedInt(Integer mReservedInt2) {
        this.mReservedInt = mReservedInt2;
        setValue();
    }

    public String getMReservedText() {
        return this.mReservedText;
    }

    public void setMReservedText(String mReservedText2) {
        this.mReservedText = mReservedText2;
        setValue();
    }

    public void writeToParcel(Parcel out, int ignored) {
        super.writeToParcel(out, ignored);
        if (this.mId != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mId.intValue());
        } else {
            out.writeByte((byte) 0);
            out.writeInt(1);
        }
        if (this.mTimeStamp != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.mTimeStamp.getTime());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mLongitude != null) {
            out.writeByte((byte) 1);
            out.writeDouble(this.mLongitude.doubleValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mLatitude != null) {
            out.writeByte((byte) 1);
            out.writeDouble(this.mLatitude.doubleValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mWeatherIcon != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mWeatherIcon.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mTemprature != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mTemprature.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mReservedInt != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mReservedInt.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mReservedText != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mReservedText);
            return;
        }
        out.writeByte((byte) 0);
    }

    public AEntityHelper<RawWeatherInfo> getHelper() {
        return RawWeatherInfoHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.collectencrypt.RawWeatherInfo";
    }

    public String getDatabaseName() {
        return "dsCollectEncrypt";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("RawWeatherInfo { mId: ").append(this.mId);
        sb.append(", mTimeStamp: ").append(this.mTimeStamp);
        sb.append(", mLongitude: ").append(this.mLongitude);
        sb.append(", mLatitude: ").append(this.mLatitude);
        sb.append(", mWeatherIcon: ").append(this.mWeatherIcon);
        sb.append(", mTemprature: ").append(this.mTemprature);
        sb.append(", mReservedInt: ").append(this.mReservedInt);
        sb.append(", mReservedText: ").append(this.mReservedText);
        sb.append(" }");
        return sb.toString();
    }

    public boolean equals(Object o) {
        return super.equals(o);
    }

    public int hashCode() {
        return super.hashCode();
    }

    public String getDatabaseVersion() {
        return "0.0.14";
    }

    public int getDatabaseVersionCode() {
        return 14;
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }
}
