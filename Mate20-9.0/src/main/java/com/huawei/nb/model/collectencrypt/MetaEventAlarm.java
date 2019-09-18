package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class MetaEventAlarm extends AManagedObject {
    public static final Parcelable.Creator<MetaEventAlarm> CREATOR = new Parcelable.Creator<MetaEventAlarm>() {
        public MetaEventAlarm createFromParcel(Parcel in) {
            return new MetaEventAlarm(in);
        }

        public MetaEventAlarm[] newArray(int size) {
            return new MetaEventAlarm[size];
        }
    };
    private String mAddress;
    private Date mAlarmTime;
    private Integer mEventID;
    private String mEventInfo;
    private Integer mId;
    private Integer mReservedInt;
    private String mReservedText;
    private Date mTimeStamp;

    public MetaEventAlarm(Cursor cursor) {
        Integer num = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.mId = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.mTimeStamp = cursor.isNull(2) ? null : new Date(cursor.getLong(2));
        this.mEventID = cursor.isNull(3) ? null : Integer.valueOf(cursor.getInt(3));
        this.mAlarmTime = cursor.isNull(4) ? null : new Date(cursor.getLong(4));
        this.mEventInfo = cursor.getString(5);
        this.mAddress = cursor.getString(6);
        this.mReservedInt = !cursor.isNull(7) ? Integer.valueOf(cursor.getInt(7)) : num;
        this.mReservedText = cursor.getString(8);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public MetaEventAlarm(Parcel in) {
        super(in);
        String str = null;
        if (in.readByte() == 0) {
            this.mId = null;
            in.readInt();
        } else {
            this.mId = Integer.valueOf(in.readInt());
        }
        this.mTimeStamp = in.readByte() == 0 ? null : new Date(in.readLong());
        this.mEventID = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mAlarmTime = in.readByte() == 0 ? null : new Date(in.readLong());
        this.mEventInfo = in.readByte() == 0 ? null : in.readString();
        this.mAddress = in.readByte() == 0 ? null : in.readString();
        this.mReservedInt = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mReservedText = in.readByte() != 0 ? in.readString() : str;
    }

    private MetaEventAlarm(Integer mId2, Date mTimeStamp2, Integer mEventID2, Date mAlarmTime2, String mEventInfo2, String mAddress2, Integer mReservedInt2, String mReservedText2) {
        this.mId = mId2;
        this.mTimeStamp = mTimeStamp2;
        this.mEventID = mEventID2;
        this.mAlarmTime = mAlarmTime2;
        this.mEventInfo = mEventInfo2;
        this.mAddress = mAddress2;
        this.mReservedInt = mReservedInt2;
        this.mReservedText = mReservedText2;
    }

    public MetaEventAlarm() {
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

    public Integer getMEventID() {
        return this.mEventID;
    }

    public void setMEventID(Integer mEventID2) {
        this.mEventID = mEventID2;
        setValue();
    }

    public Date getMAlarmTime() {
        return this.mAlarmTime;
    }

    public void setMAlarmTime(Date mAlarmTime2) {
        this.mAlarmTime = mAlarmTime2;
        setValue();
    }

    public String getMEventInfo() {
        return this.mEventInfo;
    }

    public void setMEventInfo(String mEventInfo2) {
        this.mEventInfo = mEventInfo2;
        setValue();
    }

    public String getMAddress() {
        return this.mAddress;
    }

    public void setMAddress(String mAddress2) {
        this.mAddress = mAddress2;
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
        if (this.mEventID != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mEventID.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mAlarmTime != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.mAlarmTime.getTime());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mEventInfo != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mEventInfo);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mAddress != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mAddress);
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

    public AEntityHelper<MetaEventAlarm> getHelper() {
        return MetaEventAlarmHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.collectencrypt.MetaEventAlarm";
    }

    public String getDatabaseName() {
        return "dsCollectEncrypt";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("MetaEventAlarm { mId: ").append(this.mId);
        sb.append(", mTimeStamp: ").append(this.mTimeStamp);
        sb.append(", mEventID: ").append(this.mEventID);
        sb.append(", mAlarmTime: ").append(this.mAlarmTime);
        sb.append(", mEventInfo: ").append(this.mEventInfo);
        sb.append(", mAddress: ").append(this.mAddress);
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
