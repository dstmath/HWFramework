package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RawRecomIntegratedInfo extends AManagedObject {
    public static final Parcelable.Creator<RawRecomIntegratedInfo> CREATOR = new Parcelable.Creator<RawRecomIntegratedInfo>() {
        /* class com.huawei.nb.model.collectencrypt.RawRecomIntegratedInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public RawRecomIntegratedInfo createFromParcel(Parcel parcel) {
            return new RawRecomIntegratedInfo(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public RawRecomIntegratedInfo[] newArray(int i) {
            return new RawRecomIntegratedInfo[i];
        }
    };
    private String mApkName;
    private Integer mArActivityType;
    private String mBatteryStatus;
    private Integer mCellId;
    private Integer mCurrentTemperature;
    private Date mDateTime;
    private Integer mHeadset;
    private Integer mId;
    private String mLatitude;
    private Integer mLocationType;
    private String mLongitude;
    private Integer mNetworkType;
    private Integer mReservedInt;
    private String mReservedText;
    private String mService;
    private Date mTimeStamp;
    private String mTotalTime;
    private Integer mWeatherIcon;
    private Integer mWeek;
    private String mWifiBssid;

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
        return "com.huawei.nb.model.collectencrypt.RawRecomIntegratedInfo";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public RawRecomIntegratedInfo(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Integer num = null;
        this.mId = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.mTimeStamp = cursor.isNull(2) ? null : new Date(cursor.getLong(2));
        this.mDateTime = cursor.isNull(3) ? null : new Date(cursor.getLong(3));
        this.mApkName = cursor.getString(4);
        this.mArActivityType = cursor.isNull(5) ? null : Integer.valueOf(cursor.getInt(5));
        this.mHeadset = cursor.isNull(6) ? null : Integer.valueOf(cursor.getInt(6));
        this.mWeek = cursor.isNull(7) ? null : Integer.valueOf(cursor.getInt(7));
        this.mNetworkType = cursor.isNull(8) ? null : Integer.valueOf(cursor.getInt(8));
        this.mLocationType = cursor.isNull(9) ? null : Integer.valueOf(cursor.getInt(9));
        this.mBatteryStatus = cursor.getString(10);
        this.mWeatherIcon = cursor.isNull(11) ? null : Integer.valueOf(cursor.getInt(11));
        this.mCurrentTemperature = cursor.isNull(12) ? null : Integer.valueOf(cursor.getInt(12));
        this.mLatitude = cursor.getString(13);
        this.mLongitude = cursor.getString(14);
        this.mTotalTime = cursor.getString(15);
        this.mCellId = cursor.isNull(16) ? null : Integer.valueOf(cursor.getInt(16));
        this.mWifiBssid = cursor.getString(17);
        this.mService = cursor.getString(18);
        this.mReservedInt = !cursor.isNull(19) ? Integer.valueOf(cursor.getInt(19)) : num;
        this.mReservedText = cursor.getString(20);
    }

    public RawRecomIntegratedInfo(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.mId = null;
            parcel.readInt();
        } else {
            this.mId = Integer.valueOf(parcel.readInt());
        }
        this.mTimeStamp = parcel.readByte() == 0 ? null : new Date(parcel.readLong());
        this.mDateTime = parcel.readByte() == 0 ? null : new Date(parcel.readLong());
        this.mApkName = parcel.readByte() == 0 ? null : parcel.readString();
        this.mArActivityType = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mHeadset = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mWeek = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mNetworkType = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mLocationType = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mBatteryStatus = parcel.readByte() == 0 ? null : parcel.readString();
        this.mWeatherIcon = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mCurrentTemperature = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mLatitude = parcel.readByte() == 0 ? null : parcel.readString();
        this.mLongitude = parcel.readByte() == 0 ? null : parcel.readString();
        this.mTotalTime = parcel.readByte() == 0 ? null : parcel.readString();
        this.mCellId = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mWifiBssid = parcel.readByte() == 0 ? null : parcel.readString();
        this.mService = parcel.readByte() == 0 ? null : parcel.readString();
        this.mReservedInt = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mReservedText = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private RawRecomIntegratedInfo(Integer num, Date date, Date date2, String str, Integer num2, Integer num3, Integer num4, Integer num5, Integer num6, String str2, Integer num7, Integer num8, String str3, String str4, String str5, Integer num9, String str6, String str7, Integer num10, String str8) {
        this.mId = num;
        this.mTimeStamp = date;
        this.mDateTime = date2;
        this.mApkName = str;
        this.mArActivityType = num2;
        this.mHeadset = num3;
        this.mWeek = num4;
        this.mNetworkType = num5;
        this.mLocationType = num6;
        this.mBatteryStatus = str2;
        this.mWeatherIcon = num7;
        this.mCurrentTemperature = num8;
        this.mLatitude = str3;
        this.mLongitude = str4;
        this.mTotalTime = str5;
        this.mCellId = num9;
        this.mWifiBssid = str6;
        this.mService = str7;
        this.mReservedInt = num10;
        this.mReservedText = str8;
    }

    public RawRecomIntegratedInfo() {
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

    public Date getMDateTime() {
        return this.mDateTime;
    }

    public void setMDateTime(Date date) {
        this.mDateTime = date;
        setValue();
    }

    public String getMApkName() {
        return this.mApkName;
    }

    public void setMApkName(String str) {
        this.mApkName = str;
        setValue();
    }

    public Integer getMArActivityType() {
        return this.mArActivityType;
    }

    public void setMArActivityType(Integer num) {
        this.mArActivityType = num;
        setValue();
    }

    public Integer getMHeadset() {
        return this.mHeadset;
    }

    public void setMHeadset(Integer num) {
        this.mHeadset = num;
        setValue();
    }

    public Integer getMWeek() {
        return this.mWeek;
    }

    public void setMWeek(Integer num) {
        this.mWeek = num;
        setValue();
    }

    public Integer getMNetworkType() {
        return this.mNetworkType;
    }

    public void setMNetworkType(Integer num) {
        this.mNetworkType = num;
        setValue();
    }

    public Integer getMLocationType() {
        return this.mLocationType;
    }

    public void setMLocationType(Integer num) {
        this.mLocationType = num;
        setValue();
    }

    public String getMBatteryStatus() {
        return this.mBatteryStatus;
    }

    public void setMBatteryStatus(String str) {
        this.mBatteryStatus = str;
        setValue();
    }

    public Integer getMWeatherIcon() {
        return this.mWeatherIcon;
    }

    public void setMWeatherIcon(Integer num) {
        this.mWeatherIcon = num;
        setValue();
    }

    public Integer getMCurrentTemperature() {
        return this.mCurrentTemperature;
    }

    public void setMCurrentTemperature(Integer num) {
        this.mCurrentTemperature = num;
        setValue();
    }

    public String getMLatitude() {
        return this.mLatitude;
    }

    public void setMLatitude(String str) {
        this.mLatitude = str;
        setValue();
    }

    public String getMLongitude() {
        return this.mLongitude;
    }

    public void setMLongitude(String str) {
        this.mLongitude = str;
        setValue();
    }

    public String getMTotalTime() {
        return this.mTotalTime;
    }

    public void setMTotalTime(String str) {
        this.mTotalTime = str;
        setValue();
    }

    public Integer getMCellId() {
        return this.mCellId;
    }

    public void setMCellId(Integer num) {
        this.mCellId = num;
        setValue();
    }

    public String getMWifiBssid() {
        return this.mWifiBssid;
    }

    public void setMWifiBssid(String str) {
        this.mWifiBssid = str;
        setValue();
    }

    public String getMService() {
        return this.mService;
    }

    public void setMService(String str) {
        this.mService = str;
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
        if (this.mDateTime != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.mDateTime.getTime());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mApkName != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mApkName);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mArActivityType != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mArActivityType.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mHeadset != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mHeadset.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mWeek != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mWeek.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mNetworkType != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mNetworkType.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mLocationType != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mLocationType.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mBatteryStatus != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mBatteryStatus);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mWeatherIcon != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mWeatherIcon.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mCurrentTemperature != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mCurrentTemperature.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mLatitude != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mLatitude);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mLongitude != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mLongitude);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mTotalTime != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mTotalTime);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mCellId != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mCellId.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mWifiBssid != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mWifiBssid);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mService != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mService);
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
    public AEntityHelper<RawRecomIntegratedInfo> getHelper() {
        return RawRecomIntegratedInfoHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "RawRecomIntegratedInfo { mId: " + this.mId + ", mTimeStamp: " + this.mTimeStamp + ", mDateTime: " + this.mDateTime + ", mApkName: " + this.mApkName + ", mArActivityType: " + this.mArActivityType + ", mHeadset: " + this.mHeadset + ", mWeek: " + this.mWeek + ", mNetworkType: " + this.mNetworkType + ", mLocationType: " + this.mLocationType + ", mBatteryStatus: " + this.mBatteryStatus + ", mWeatherIcon: " + this.mWeatherIcon + ", mCurrentTemperature: " + this.mCurrentTemperature + ", mLatitude: " + this.mLatitude + ", mLongitude: " + this.mLongitude + ", mTotalTime: " + this.mTotalTime + ", mCellId: " + this.mCellId + ", mWifiBssid: " + this.mWifiBssid + ", mService: " + this.mService + ", mReservedInt: " + this.mReservedInt + ", mReservedText: " + this.mReservedText + " }";
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
