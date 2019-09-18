package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RawHotelInfo extends AManagedObject {
    public static final Parcelable.Creator<RawHotelInfo> CREATOR = new Parcelable.Creator<RawHotelInfo>() {
        public RawHotelInfo createFromParcel(Parcel in) {
            return new RawHotelInfo(in);
        }

        public RawHotelInfo[] newArray(int size) {
            return new RawHotelInfo[size];
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

    public RawHotelInfo(Cursor cursor) {
        Integer num = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.mId = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.mTimeStamp = cursor.isNull(2) ? null : new Date(cursor.getLong(2));
        this.mHotelTelNo = cursor.getString(3);
        this.mHotelAddr = cursor.getString(4);
        this.mHotelName = cursor.getString(5);
        this.mCheckinTime = cursor.isNull(6) ? null : new Date(cursor.getLong(6));
        this.mReservedInt = !cursor.isNull(7) ? Integer.valueOf(cursor.getInt(7)) : num;
        this.mReservedText = cursor.getString(8);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public RawHotelInfo(Parcel in) {
        super(in);
        String str = null;
        if (in.readByte() == 0) {
            this.mId = null;
            in.readInt();
        } else {
            this.mId = Integer.valueOf(in.readInt());
        }
        this.mTimeStamp = in.readByte() == 0 ? null : new Date(in.readLong());
        this.mHotelTelNo = in.readByte() == 0 ? null : in.readString();
        this.mHotelAddr = in.readByte() == 0 ? null : in.readString();
        this.mHotelName = in.readByte() == 0 ? null : in.readString();
        this.mCheckinTime = in.readByte() == 0 ? null : new Date(in.readLong());
        this.mReservedInt = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mReservedText = in.readByte() != 0 ? in.readString() : str;
    }

    private RawHotelInfo(Integer mId2, Date mTimeStamp2, String mHotelTelNo2, String mHotelAddr2, String mHotelName2, Date mCheckinTime2, Integer mReservedInt2, String mReservedText2) {
        this.mId = mId2;
        this.mTimeStamp = mTimeStamp2;
        this.mHotelTelNo = mHotelTelNo2;
        this.mHotelAddr = mHotelAddr2;
        this.mHotelName = mHotelName2;
        this.mCheckinTime = mCheckinTime2;
        this.mReservedInt = mReservedInt2;
        this.mReservedText = mReservedText2;
    }

    public RawHotelInfo() {
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

    public String getMHotelTelNo() {
        return this.mHotelTelNo;
    }

    public void setMHotelTelNo(String mHotelTelNo2) {
        this.mHotelTelNo = mHotelTelNo2;
        setValue();
    }

    public String getMHotelAddr() {
        return this.mHotelAddr;
    }

    public void setMHotelAddr(String mHotelAddr2) {
        this.mHotelAddr = mHotelAddr2;
        setValue();
    }

    public String getMHotelName() {
        return this.mHotelName;
    }

    public void setMHotelName(String mHotelName2) {
        this.mHotelName = mHotelName2;
        setValue();
    }

    public Date getMCheckinTime() {
        return this.mCheckinTime;
    }

    public void setMCheckinTime(Date mCheckinTime2) {
        this.mCheckinTime = mCheckinTime2;
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
        if (this.mHotelTelNo != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mHotelTelNo);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mHotelAddr != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mHotelAddr);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mHotelName != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mHotelName);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mCheckinTime != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.mCheckinTime.getTime());
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

    public AEntityHelper<RawHotelInfo> getHelper() {
        return RawHotelInfoHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.collectencrypt.RawHotelInfo";
    }

    public String getDatabaseName() {
        return "dsCollectEncrypt";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("RawHotelInfo { mId: ").append(this.mId);
        sb.append(", mTimeStamp: ").append(this.mTimeStamp);
        sb.append(", mHotelTelNo: ").append(this.mHotelTelNo);
        sb.append(", mHotelAddr: ").append(this.mHotelAddr);
        sb.append(", mHotelName: ").append(this.mHotelName);
        sb.append(", mCheckinTime: ").append(this.mCheckinTime);
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
