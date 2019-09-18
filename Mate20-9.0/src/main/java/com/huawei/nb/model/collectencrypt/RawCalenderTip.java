package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RawCalenderTip extends AManagedObject {
    public static final Parcelable.Creator<RawCalenderTip> CREATOR = new Parcelable.Creator<RawCalenderTip>() {
        public RawCalenderTip createFromParcel(Parcel in) {
            return new RawCalenderTip(in);
        }

        public RawCalenderTip[] newArray(int size) {
            return new RawCalenderTip[size];
        }
    };
    private Integer mId;
    private Integer mReservedInt;
    private String mReservedText;
    private Date mTimeStamp;
    private Date mTipAlarmTime;
    private String mTipContent;
    private Date mTipEndTime;
    private Date mTipStartTime;
    private String mTipTitle;

    public RawCalenderTip(Cursor cursor) {
        Integer num = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.mId = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.mTimeStamp = cursor.isNull(2) ? null : new Date(cursor.getLong(2));
        this.mTipTitle = cursor.getString(3);
        this.mTipContent = cursor.getString(4);
        this.mTipStartTime = cursor.isNull(5) ? null : new Date(cursor.getLong(5));
        this.mTipEndTime = cursor.isNull(6) ? null : new Date(cursor.getLong(6));
        this.mTipAlarmTime = cursor.isNull(7) ? null : new Date(cursor.getLong(7));
        this.mReservedInt = !cursor.isNull(8) ? Integer.valueOf(cursor.getInt(8)) : num;
        this.mReservedText = cursor.getString(9);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public RawCalenderTip(Parcel in) {
        super(in);
        String str = null;
        if (in.readByte() == 0) {
            this.mId = null;
            in.readInt();
        } else {
            this.mId = Integer.valueOf(in.readInt());
        }
        this.mTimeStamp = in.readByte() == 0 ? null : new Date(in.readLong());
        this.mTipTitle = in.readByte() == 0 ? null : in.readString();
        this.mTipContent = in.readByte() == 0 ? null : in.readString();
        this.mTipStartTime = in.readByte() == 0 ? null : new Date(in.readLong());
        this.mTipEndTime = in.readByte() == 0 ? null : new Date(in.readLong());
        this.mTipAlarmTime = in.readByte() == 0 ? null : new Date(in.readLong());
        this.mReservedInt = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mReservedText = in.readByte() != 0 ? in.readString() : str;
    }

    private RawCalenderTip(Integer mId2, Date mTimeStamp2, String mTipTitle2, String mTipContent2, Date mTipStartTime2, Date mTipEndTime2, Date mTipAlarmTime2, Integer mReservedInt2, String mReservedText2) {
        this.mId = mId2;
        this.mTimeStamp = mTimeStamp2;
        this.mTipTitle = mTipTitle2;
        this.mTipContent = mTipContent2;
        this.mTipStartTime = mTipStartTime2;
        this.mTipEndTime = mTipEndTime2;
        this.mTipAlarmTime = mTipAlarmTime2;
        this.mReservedInt = mReservedInt2;
        this.mReservedText = mReservedText2;
    }

    public RawCalenderTip() {
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

    public String getMTipTitle() {
        return this.mTipTitle;
    }

    public void setMTipTitle(String mTipTitle2) {
        this.mTipTitle = mTipTitle2;
        setValue();
    }

    public String getMTipContent() {
        return this.mTipContent;
    }

    public void setMTipContent(String mTipContent2) {
        this.mTipContent = mTipContent2;
        setValue();
    }

    public Date getMTipStartTime() {
        return this.mTipStartTime;
    }

    public void setMTipStartTime(Date mTipStartTime2) {
        this.mTipStartTime = mTipStartTime2;
        setValue();
    }

    public Date getMTipEndTime() {
        return this.mTipEndTime;
    }

    public void setMTipEndTime(Date mTipEndTime2) {
        this.mTipEndTime = mTipEndTime2;
        setValue();
    }

    public Date getMTipAlarmTime() {
        return this.mTipAlarmTime;
    }

    public void setMTipAlarmTime(Date mTipAlarmTime2) {
        this.mTipAlarmTime = mTipAlarmTime2;
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
        if (this.mTipTitle != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mTipTitle);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mTipContent != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mTipContent);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mTipStartTime != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.mTipStartTime.getTime());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mTipEndTime != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.mTipEndTime.getTime());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mTipAlarmTime != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.mTipAlarmTime.getTime());
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

    public AEntityHelper<RawCalenderTip> getHelper() {
        return RawCalenderTipHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.collectencrypt.RawCalenderTip";
    }

    public String getDatabaseName() {
        return "dsCollectEncrypt";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("RawCalenderTip { mId: ").append(this.mId);
        sb.append(", mTimeStamp: ").append(this.mTimeStamp);
        sb.append(", mTipTitle: ").append(this.mTipTitle);
        sb.append(", mTipContent: ").append(this.mTipContent);
        sb.append(", mTipStartTime: ").append(this.mTipStartTime);
        sb.append(", mTipEndTime: ").append(this.mTipEndTime);
        sb.append(", mTipAlarmTime: ").append(this.mTipAlarmTime);
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
