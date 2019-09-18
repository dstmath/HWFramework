package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RawRecomIntegratedInfo extends AManagedObject {
    public static final Parcelable.Creator<RawRecomIntegratedInfo> CREATOR = new Parcelable.Creator<RawRecomIntegratedInfo>() {
        public RawRecomIntegratedInfo createFromParcel(Parcel in) {
            return new RawRecomIntegratedInfo(in);
        }

        public RawRecomIntegratedInfo[] newArray(int size) {
            return new RawRecomIntegratedInfo[size];
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

    public RawRecomIntegratedInfo(Cursor cursor) {
        Integer num = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
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

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public RawRecomIntegratedInfo(Parcel in) {
        super(in);
        String str = null;
        if (in.readByte() == 0) {
            this.mId = null;
            in.readInt();
        } else {
            this.mId = Integer.valueOf(in.readInt());
        }
        this.mTimeStamp = in.readByte() == 0 ? null : new Date(in.readLong());
        this.mDateTime = in.readByte() == 0 ? null : new Date(in.readLong());
        this.mApkName = in.readByte() == 0 ? null : in.readString();
        this.mArActivityType = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mHeadset = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mWeek = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mNetworkType = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mLocationType = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mBatteryStatus = in.readByte() == 0 ? null : in.readString();
        this.mWeatherIcon = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mCurrentTemperature = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mLatitude = in.readByte() == 0 ? null : in.readString();
        this.mLongitude = in.readByte() == 0 ? null : in.readString();
        this.mTotalTime = in.readByte() == 0 ? null : in.readString();
        this.mCellId = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mWifiBssid = in.readByte() == 0 ? null : in.readString();
        this.mService = in.readByte() == 0 ? null : in.readString();
        this.mReservedInt = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mReservedText = in.readByte() != 0 ? in.readString() : str;
    }

    private RawRecomIntegratedInfo(Integer mId2, Date mTimeStamp2, Date mDateTime2, String mApkName2, Integer mArActivityType2, Integer mHeadset2, Integer mWeek2, Integer mNetworkType2, Integer mLocationType2, String mBatteryStatus2, Integer mWeatherIcon2, Integer mCurrentTemperature2, String mLatitude2, String mLongitude2, String mTotalTime2, Integer mCellId2, String mWifiBssid2, String mService2, Integer mReservedInt2, String mReservedText2) {
        this.mId = mId2;
        this.mTimeStamp = mTimeStamp2;
        this.mDateTime = mDateTime2;
        this.mApkName = mApkName2;
        this.mArActivityType = mArActivityType2;
        this.mHeadset = mHeadset2;
        this.mWeek = mWeek2;
        this.mNetworkType = mNetworkType2;
        this.mLocationType = mLocationType2;
        this.mBatteryStatus = mBatteryStatus2;
        this.mWeatherIcon = mWeatherIcon2;
        this.mCurrentTemperature = mCurrentTemperature2;
        this.mLatitude = mLatitude2;
        this.mLongitude = mLongitude2;
        this.mTotalTime = mTotalTime2;
        this.mCellId = mCellId2;
        this.mWifiBssid = mWifiBssid2;
        this.mService = mService2;
        this.mReservedInt = mReservedInt2;
        this.mReservedText = mReservedText2;
    }

    public RawRecomIntegratedInfo() {
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

    public Date getMDateTime() {
        return this.mDateTime;
    }

    public void setMDateTime(Date mDateTime2) {
        this.mDateTime = mDateTime2;
        setValue();
    }

    public String getMApkName() {
        return this.mApkName;
    }

    public void setMApkName(String mApkName2) {
        this.mApkName = mApkName2;
        setValue();
    }

    public Integer getMArActivityType() {
        return this.mArActivityType;
    }

    public void setMArActivityType(Integer mArActivityType2) {
        this.mArActivityType = mArActivityType2;
        setValue();
    }

    public Integer getMHeadset() {
        return this.mHeadset;
    }

    public void setMHeadset(Integer mHeadset2) {
        this.mHeadset = mHeadset2;
        setValue();
    }

    public Integer getMWeek() {
        return this.mWeek;
    }

    public void setMWeek(Integer mWeek2) {
        this.mWeek = mWeek2;
        setValue();
    }

    public Integer getMNetworkType() {
        return this.mNetworkType;
    }

    public void setMNetworkType(Integer mNetworkType2) {
        this.mNetworkType = mNetworkType2;
        setValue();
    }

    public Integer getMLocationType() {
        return this.mLocationType;
    }

    public void setMLocationType(Integer mLocationType2) {
        this.mLocationType = mLocationType2;
        setValue();
    }

    public String getMBatteryStatus() {
        return this.mBatteryStatus;
    }

    public void setMBatteryStatus(String mBatteryStatus2) {
        this.mBatteryStatus = mBatteryStatus2;
        setValue();
    }

    public Integer getMWeatherIcon() {
        return this.mWeatherIcon;
    }

    public void setMWeatherIcon(Integer mWeatherIcon2) {
        this.mWeatherIcon = mWeatherIcon2;
        setValue();
    }

    public Integer getMCurrentTemperature() {
        return this.mCurrentTemperature;
    }

    public void setMCurrentTemperature(Integer mCurrentTemperature2) {
        this.mCurrentTemperature = mCurrentTemperature2;
        setValue();
    }

    public String getMLatitude() {
        return this.mLatitude;
    }

    public void setMLatitude(String mLatitude2) {
        this.mLatitude = mLatitude2;
        setValue();
    }

    public String getMLongitude() {
        return this.mLongitude;
    }

    public void setMLongitude(String mLongitude2) {
        this.mLongitude = mLongitude2;
        setValue();
    }

    public String getMTotalTime() {
        return this.mTotalTime;
    }

    public void setMTotalTime(String mTotalTime2) {
        this.mTotalTime = mTotalTime2;
        setValue();
    }

    public Integer getMCellId() {
        return this.mCellId;
    }

    public void setMCellId(Integer mCellId2) {
        this.mCellId = mCellId2;
        setValue();
    }

    public String getMWifiBssid() {
        return this.mWifiBssid;
    }

    public void setMWifiBssid(String mWifiBssid2) {
        this.mWifiBssid = mWifiBssid2;
        setValue();
    }

    public String getMService() {
        return this.mService;
    }

    public void setMService(String mService2) {
        this.mService = mService2;
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
        if (this.mDateTime != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.mDateTime.getTime());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mApkName != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mApkName);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mArActivityType != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mArActivityType.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mHeadset != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mHeadset.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mWeek != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mWeek.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mNetworkType != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mNetworkType.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mLocationType != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mLocationType.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mBatteryStatus != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mBatteryStatus);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mWeatherIcon != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mWeatherIcon.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mCurrentTemperature != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mCurrentTemperature.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mLatitude != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mLatitude);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mLongitude != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mLongitude);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mTotalTime != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mTotalTime);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mCellId != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mCellId.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mWifiBssid != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mWifiBssid);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mService != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mService);
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

    public AEntityHelper<RawRecomIntegratedInfo> getHelper() {
        return RawRecomIntegratedInfoHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.collectencrypt.RawRecomIntegratedInfo";
    }

    public String getDatabaseName() {
        return "dsCollectEncrypt";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("RawRecomIntegratedInfo { mId: ").append(this.mId);
        sb.append(", mTimeStamp: ").append(this.mTimeStamp);
        sb.append(", mDateTime: ").append(this.mDateTime);
        sb.append(", mApkName: ").append(this.mApkName);
        sb.append(", mArActivityType: ").append(this.mArActivityType);
        sb.append(", mHeadset: ").append(this.mHeadset);
        sb.append(", mWeek: ").append(this.mWeek);
        sb.append(", mNetworkType: ").append(this.mNetworkType);
        sb.append(", mLocationType: ").append(this.mLocationType);
        sb.append(", mBatteryStatus: ").append(this.mBatteryStatus);
        sb.append(", mWeatherIcon: ").append(this.mWeatherIcon);
        sb.append(", mCurrentTemperature: ").append(this.mCurrentTemperature);
        sb.append(", mLatitude: ").append(this.mLatitude);
        sb.append(", mLongitude: ").append(this.mLongitude);
        sb.append(", mTotalTime: ").append(this.mTotalTime);
        sb.append(", mCellId: ").append(this.mCellId);
        sb.append(", mWifiBssid: ").append(this.mWifiBssid);
        sb.append(", mService: ").append(this.mService);
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
