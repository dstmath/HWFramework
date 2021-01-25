package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class MetaUserInfoStatistic extends AManagedObject {
    public static final Parcelable.Creator<MetaUserInfoStatistic> CREATOR = new Parcelable.Creator<MetaUserInfoStatistic>() {
        /* class com.huawei.nb.model.collectencrypt.MetaUserInfoStatistic.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public MetaUserInfoStatistic createFromParcel(Parcel parcel) {
            return new MetaUserInfoStatistic(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public MetaUserInfoStatistic[] newArray(int i) {
            return new MetaUserInfoStatistic[i];
        }
    };
    private Integer mCallDialNum;
    private Integer mCallDurationTime;
    private Integer mCallRecvNum;
    private Integer mContactNum;
    private Date mFirstAlarmClock;
    private String mHWID;
    private Date mHWIDBirthday;
    private Integer mHWIDGender;
    private String mHWIDName;
    private Integer mId;
    private Double mMobileDataSurplus;
    private Double mMobileDataTotal;
    private Integer mMusicNum;
    private Integer mPhotoNum;
    private Integer mReservedInt;
    private String mReservedText;
    private Date mTimeStamp;
    private Integer mVideoNum;
    private Double mWifiDataTotal;

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
        return "com.huawei.nb.model.collectencrypt.MetaUserInfoStatistic";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public MetaUserInfoStatistic(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Integer num = null;
        this.mId = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.mTimeStamp = cursor.isNull(2) ? null : new Date(cursor.getLong(2));
        this.mHWID = cursor.getString(3);
        this.mContactNum = cursor.isNull(4) ? null : Integer.valueOf(cursor.getInt(4));
        this.mMusicNum = cursor.isNull(5) ? null : Integer.valueOf(cursor.getInt(5));
        this.mVideoNum = cursor.isNull(6) ? null : Integer.valueOf(cursor.getInt(6));
        this.mPhotoNum = cursor.isNull(7) ? null : Integer.valueOf(cursor.getInt(7));
        this.mFirstAlarmClock = cursor.isNull(8) ? null : new Date(cursor.getLong(8));
        this.mCallDialNum = cursor.isNull(9) ? null : Integer.valueOf(cursor.getInt(9));
        this.mCallRecvNum = cursor.isNull(10) ? null : Integer.valueOf(cursor.getInt(10));
        this.mCallDurationTime = cursor.isNull(11) ? null : Integer.valueOf(cursor.getInt(11));
        this.mWifiDataTotal = cursor.isNull(12) ? null : Double.valueOf(cursor.getDouble(12));
        this.mMobileDataTotal = cursor.isNull(13) ? null : Double.valueOf(cursor.getDouble(13));
        this.mMobileDataSurplus = cursor.isNull(14) ? null : Double.valueOf(cursor.getDouble(14));
        this.mHWIDName = cursor.getString(15);
        this.mHWIDBirthday = cursor.isNull(16) ? null : new Date(cursor.getLong(16));
        this.mHWIDGender = cursor.isNull(17) ? null : Integer.valueOf(cursor.getInt(17));
        this.mReservedInt = !cursor.isNull(18) ? Integer.valueOf(cursor.getInt(18)) : num;
        this.mReservedText = cursor.getString(19);
    }

    public MetaUserInfoStatistic(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.mId = null;
            parcel.readInt();
        } else {
            this.mId = Integer.valueOf(parcel.readInt());
        }
        this.mTimeStamp = parcel.readByte() == 0 ? null : new Date(parcel.readLong());
        this.mHWID = parcel.readByte() == 0 ? null : parcel.readString();
        this.mContactNum = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mMusicNum = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mVideoNum = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mPhotoNum = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mFirstAlarmClock = parcel.readByte() == 0 ? null : new Date(parcel.readLong());
        this.mCallDialNum = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mCallRecvNum = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mCallDurationTime = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mWifiDataTotal = parcel.readByte() == 0 ? null : Double.valueOf(parcel.readDouble());
        this.mMobileDataTotal = parcel.readByte() == 0 ? null : Double.valueOf(parcel.readDouble());
        this.mMobileDataSurplus = parcel.readByte() == 0 ? null : Double.valueOf(parcel.readDouble());
        this.mHWIDName = parcel.readByte() == 0 ? null : parcel.readString();
        this.mHWIDBirthday = parcel.readByte() == 0 ? null : new Date(parcel.readLong());
        this.mHWIDGender = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mReservedInt = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mReservedText = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private MetaUserInfoStatistic(Integer num, Date date, String str, Integer num2, Integer num3, Integer num4, Integer num5, Date date2, Integer num6, Integer num7, Integer num8, Double d, Double d2, Double d3, String str2, Date date3, Integer num9, Integer num10, String str3) {
        this.mId = num;
        this.mTimeStamp = date;
        this.mHWID = str;
        this.mContactNum = num2;
        this.mMusicNum = num3;
        this.mVideoNum = num4;
        this.mPhotoNum = num5;
        this.mFirstAlarmClock = date2;
        this.mCallDialNum = num6;
        this.mCallRecvNum = num7;
        this.mCallDurationTime = num8;
        this.mWifiDataTotal = d;
        this.mMobileDataTotal = d2;
        this.mMobileDataSurplus = d3;
        this.mHWIDName = str2;
        this.mHWIDBirthday = date3;
        this.mHWIDGender = num9;
        this.mReservedInt = num10;
        this.mReservedText = str3;
    }

    public MetaUserInfoStatistic() {
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

    public String getMHWID() {
        return this.mHWID;
    }

    public void setMHWID(String str) {
        this.mHWID = str;
        setValue();
    }

    public Integer getMContactNum() {
        return this.mContactNum;
    }

    public void setMContactNum(Integer num) {
        this.mContactNum = num;
        setValue();
    }

    public Integer getMMusicNum() {
        return this.mMusicNum;
    }

    public void setMMusicNum(Integer num) {
        this.mMusicNum = num;
        setValue();
    }

    public Integer getMVideoNum() {
        return this.mVideoNum;
    }

    public void setMVideoNum(Integer num) {
        this.mVideoNum = num;
        setValue();
    }

    public Integer getMPhotoNum() {
        return this.mPhotoNum;
    }

    public void setMPhotoNum(Integer num) {
        this.mPhotoNum = num;
        setValue();
    }

    public Date getMFirstAlarmClock() {
        return this.mFirstAlarmClock;
    }

    public void setMFirstAlarmClock(Date date) {
        this.mFirstAlarmClock = date;
        setValue();
    }

    public Integer getMCallDialNum() {
        return this.mCallDialNum;
    }

    public void setMCallDialNum(Integer num) {
        this.mCallDialNum = num;
        setValue();
    }

    public Integer getMCallRecvNum() {
        return this.mCallRecvNum;
    }

    public void setMCallRecvNum(Integer num) {
        this.mCallRecvNum = num;
        setValue();
    }

    public Integer getMCallDurationTime() {
        return this.mCallDurationTime;
    }

    public void setMCallDurationTime(Integer num) {
        this.mCallDurationTime = num;
        setValue();
    }

    public Double getMWifiDataTotal() {
        return this.mWifiDataTotal;
    }

    public void setMWifiDataTotal(Double d) {
        this.mWifiDataTotal = d;
        setValue();
    }

    public Double getMMobileDataTotal() {
        return this.mMobileDataTotal;
    }

    public void setMMobileDataTotal(Double d) {
        this.mMobileDataTotal = d;
        setValue();
    }

    public Double getMMobileDataSurplus() {
        return this.mMobileDataSurplus;
    }

    public void setMMobileDataSurplus(Double d) {
        this.mMobileDataSurplus = d;
        setValue();
    }

    public String getMHWIDName() {
        return this.mHWIDName;
    }

    public void setMHWIDName(String str) {
        this.mHWIDName = str;
        setValue();
    }

    public Date getMHWIDBirthday() {
        return this.mHWIDBirthday;
    }

    public void setMHWIDBirthday(Date date) {
        this.mHWIDBirthday = date;
        setValue();
    }

    public Integer getMHWIDGender() {
        return this.mHWIDGender;
    }

    public void setMHWIDGender(Integer num) {
        this.mHWIDGender = num;
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
        if (this.mHWID != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mHWID);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mContactNum != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mContactNum.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mMusicNum != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mMusicNum.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mVideoNum != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mVideoNum.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mPhotoNum != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mPhotoNum.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mFirstAlarmClock != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.mFirstAlarmClock.getTime());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mCallDialNum != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mCallDialNum.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mCallRecvNum != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mCallRecvNum.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mCallDurationTime != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mCallDurationTime.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mWifiDataTotal != null) {
            parcel.writeByte((byte) 1);
            parcel.writeDouble(this.mWifiDataTotal.doubleValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mMobileDataTotal != null) {
            parcel.writeByte((byte) 1);
            parcel.writeDouble(this.mMobileDataTotal.doubleValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mMobileDataSurplus != null) {
            parcel.writeByte((byte) 1);
            parcel.writeDouble(this.mMobileDataSurplus.doubleValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mHWIDName != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mHWIDName);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mHWIDBirthday != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.mHWIDBirthday.getTime());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mHWIDGender != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mHWIDGender.intValue());
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
    public AEntityHelper<MetaUserInfoStatistic> getHelper() {
        return MetaUserInfoStatisticHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "MetaUserInfoStatistic { mId: " + this.mId + ", mTimeStamp: " + this.mTimeStamp + ", mHWID: " + this.mHWID + ", mContactNum: " + this.mContactNum + ", mMusicNum: " + this.mMusicNum + ", mVideoNum: " + this.mVideoNum + ", mPhotoNum: " + this.mPhotoNum + ", mFirstAlarmClock: " + this.mFirstAlarmClock + ", mCallDialNum: " + this.mCallDialNum + ", mCallRecvNum: " + this.mCallRecvNum + ", mCallDurationTime: " + this.mCallDurationTime + ", mWifiDataTotal: " + this.mWifiDataTotal + ", mMobileDataTotal: " + this.mMobileDataTotal + ", mMobileDataSurplus: " + this.mMobileDataSurplus + ", mHWIDName: " + this.mHWIDName + ", mHWIDBirthday: " + this.mHWIDBirthday + ", mHWIDGender: " + this.mHWIDGender + ", mReservedInt: " + this.mReservedInt + ", mReservedText: " + this.mReservedText + " }";
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
