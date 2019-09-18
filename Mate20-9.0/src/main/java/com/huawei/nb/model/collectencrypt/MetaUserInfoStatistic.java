package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class MetaUserInfoStatistic extends AManagedObject {
    public static final Parcelable.Creator<MetaUserInfoStatistic> CREATOR = new Parcelable.Creator<MetaUserInfoStatistic>() {
        public MetaUserInfoStatistic createFromParcel(Parcel in) {
            return new MetaUserInfoStatistic(in);
        }

        public MetaUserInfoStatistic[] newArray(int size) {
            return new MetaUserInfoStatistic[size];
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

    public MetaUserInfoStatistic(Cursor cursor) {
        Integer num = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
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

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public MetaUserInfoStatistic(Parcel in) {
        super(in);
        String str = null;
        if (in.readByte() == 0) {
            this.mId = null;
            in.readInt();
        } else {
            this.mId = Integer.valueOf(in.readInt());
        }
        this.mTimeStamp = in.readByte() == 0 ? null : new Date(in.readLong());
        this.mHWID = in.readByte() == 0 ? null : in.readString();
        this.mContactNum = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mMusicNum = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mVideoNum = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mPhotoNum = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mFirstAlarmClock = in.readByte() == 0 ? null : new Date(in.readLong());
        this.mCallDialNum = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mCallRecvNum = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mCallDurationTime = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mWifiDataTotal = in.readByte() == 0 ? null : Double.valueOf(in.readDouble());
        this.mMobileDataTotal = in.readByte() == 0 ? null : Double.valueOf(in.readDouble());
        this.mMobileDataSurplus = in.readByte() == 0 ? null : Double.valueOf(in.readDouble());
        this.mHWIDName = in.readByte() == 0 ? null : in.readString();
        this.mHWIDBirthday = in.readByte() == 0 ? null : new Date(in.readLong());
        this.mHWIDGender = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mReservedInt = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mReservedText = in.readByte() != 0 ? in.readString() : str;
    }

    private MetaUserInfoStatistic(Integer mId2, Date mTimeStamp2, String mHWID2, Integer mContactNum2, Integer mMusicNum2, Integer mVideoNum2, Integer mPhotoNum2, Date mFirstAlarmClock2, Integer mCallDialNum2, Integer mCallRecvNum2, Integer mCallDurationTime2, Double mWifiDataTotal2, Double mMobileDataTotal2, Double mMobileDataSurplus2, String mHWIDName2, Date mHWIDBirthday2, Integer mHWIDGender2, Integer mReservedInt2, String mReservedText2) {
        this.mId = mId2;
        this.mTimeStamp = mTimeStamp2;
        this.mHWID = mHWID2;
        this.mContactNum = mContactNum2;
        this.mMusicNum = mMusicNum2;
        this.mVideoNum = mVideoNum2;
        this.mPhotoNum = mPhotoNum2;
        this.mFirstAlarmClock = mFirstAlarmClock2;
        this.mCallDialNum = mCallDialNum2;
        this.mCallRecvNum = mCallRecvNum2;
        this.mCallDurationTime = mCallDurationTime2;
        this.mWifiDataTotal = mWifiDataTotal2;
        this.mMobileDataTotal = mMobileDataTotal2;
        this.mMobileDataSurplus = mMobileDataSurplus2;
        this.mHWIDName = mHWIDName2;
        this.mHWIDBirthday = mHWIDBirthday2;
        this.mHWIDGender = mHWIDGender2;
        this.mReservedInt = mReservedInt2;
        this.mReservedText = mReservedText2;
    }

    public MetaUserInfoStatistic() {
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

    public String getMHWID() {
        return this.mHWID;
    }

    public void setMHWID(String mHWID2) {
        this.mHWID = mHWID2;
        setValue();
    }

    public Integer getMContactNum() {
        return this.mContactNum;
    }

    public void setMContactNum(Integer mContactNum2) {
        this.mContactNum = mContactNum2;
        setValue();
    }

    public Integer getMMusicNum() {
        return this.mMusicNum;
    }

    public void setMMusicNum(Integer mMusicNum2) {
        this.mMusicNum = mMusicNum2;
        setValue();
    }

    public Integer getMVideoNum() {
        return this.mVideoNum;
    }

    public void setMVideoNum(Integer mVideoNum2) {
        this.mVideoNum = mVideoNum2;
        setValue();
    }

    public Integer getMPhotoNum() {
        return this.mPhotoNum;
    }

    public void setMPhotoNum(Integer mPhotoNum2) {
        this.mPhotoNum = mPhotoNum2;
        setValue();
    }

    public Date getMFirstAlarmClock() {
        return this.mFirstAlarmClock;
    }

    public void setMFirstAlarmClock(Date mFirstAlarmClock2) {
        this.mFirstAlarmClock = mFirstAlarmClock2;
        setValue();
    }

    public Integer getMCallDialNum() {
        return this.mCallDialNum;
    }

    public void setMCallDialNum(Integer mCallDialNum2) {
        this.mCallDialNum = mCallDialNum2;
        setValue();
    }

    public Integer getMCallRecvNum() {
        return this.mCallRecvNum;
    }

    public void setMCallRecvNum(Integer mCallRecvNum2) {
        this.mCallRecvNum = mCallRecvNum2;
        setValue();
    }

    public Integer getMCallDurationTime() {
        return this.mCallDurationTime;
    }

    public void setMCallDurationTime(Integer mCallDurationTime2) {
        this.mCallDurationTime = mCallDurationTime2;
        setValue();
    }

    public Double getMWifiDataTotal() {
        return this.mWifiDataTotal;
    }

    public void setMWifiDataTotal(Double mWifiDataTotal2) {
        this.mWifiDataTotal = mWifiDataTotal2;
        setValue();
    }

    public Double getMMobileDataTotal() {
        return this.mMobileDataTotal;
    }

    public void setMMobileDataTotal(Double mMobileDataTotal2) {
        this.mMobileDataTotal = mMobileDataTotal2;
        setValue();
    }

    public Double getMMobileDataSurplus() {
        return this.mMobileDataSurplus;
    }

    public void setMMobileDataSurplus(Double mMobileDataSurplus2) {
        this.mMobileDataSurplus = mMobileDataSurplus2;
        setValue();
    }

    public String getMHWIDName() {
        return this.mHWIDName;
    }

    public void setMHWIDName(String mHWIDName2) {
        this.mHWIDName = mHWIDName2;
        setValue();
    }

    public Date getMHWIDBirthday() {
        return this.mHWIDBirthday;
    }

    public void setMHWIDBirthday(Date mHWIDBirthday2) {
        this.mHWIDBirthday = mHWIDBirthday2;
        setValue();
    }

    public Integer getMHWIDGender() {
        return this.mHWIDGender;
    }

    public void setMHWIDGender(Integer mHWIDGender2) {
        this.mHWIDGender = mHWIDGender2;
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
        if (this.mHWID != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mHWID);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mContactNum != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mContactNum.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mMusicNum != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mMusicNum.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mVideoNum != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mVideoNum.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mPhotoNum != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mPhotoNum.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mFirstAlarmClock != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.mFirstAlarmClock.getTime());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mCallDialNum != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mCallDialNum.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mCallRecvNum != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mCallRecvNum.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mCallDurationTime != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mCallDurationTime.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mWifiDataTotal != null) {
            out.writeByte((byte) 1);
            out.writeDouble(this.mWifiDataTotal.doubleValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mMobileDataTotal != null) {
            out.writeByte((byte) 1);
            out.writeDouble(this.mMobileDataTotal.doubleValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mMobileDataSurplus != null) {
            out.writeByte((byte) 1);
            out.writeDouble(this.mMobileDataSurplus.doubleValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mHWIDName != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mHWIDName);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mHWIDBirthday != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.mHWIDBirthday.getTime());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mHWIDGender != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mHWIDGender.intValue());
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

    public AEntityHelper<MetaUserInfoStatistic> getHelper() {
        return MetaUserInfoStatisticHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.collectencrypt.MetaUserInfoStatistic";
    }

    public String getDatabaseName() {
        return "dsCollectEncrypt";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("MetaUserInfoStatistic { mId: ").append(this.mId);
        sb.append(", mTimeStamp: ").append(this.mTimeStamp);
        sb.append(", mHWID: ").append(this.mHWID);
        sb.append(", mContactNum: ").append(this.mContactNum);
        sb.append(", mMusicNum: ").append(this.mMusicNum);
        sb.append(", mVideoNum: ").append(this.mVideoNum);
        sb.append(", mPhotoNum: ").append(this.mPhotoNum);
        sb.append(", mFirstAlarmClock: ").append(this.mFirstAlarmClock);
        sb.append(", mCallDialNum: ").append(this.mCallDialNum);
        sb.append(", mCallRecvNum: ").append(this.mCallRecvNum);
        sb.append(", mCallDurationTime: ").append(this.mCallDurationTime);
        sb.append(", mWifiDataTotal: ").append(this.mWifiDataTotal);
        sb.append(", mMobileDataTotal: ").append(this.mMobileDataTotal);
        sb.append(", mMobileDataSurplus: ").append(this.mMobileDataSurplus);
        sb.append(", mHWIDName: ").append(this.mHWIDName);
        sb.append(", mHWIDBirthday: ").append(this.mHWIDBirthday);
        sb.append(", mHWIDGender: ").append(this.mHWIDGender);
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
