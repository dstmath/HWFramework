package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RawTrainFlightTickInfo extends AManagedObject {
    public static final Parcelable.Creator<RawTrainFlightTickInfo> CREATOR = new Parcelable.Creator<RawTrainFlightTickInfo>() {
        public RawTrainFlightTickInfo createFromParcel(Parcel in) {
            return new RawTrainFlightTickInfo(in);
        }

        public RawTrainFlightTickInfo[] newArray(int size) {
            return new RawTrainFlightTickInfo[size];
        }
    };
    private Integer mId;
    private String mPassengerName;
    private Integer mReservedInt;
    private String mReservedText;
    private String mSeatNo;
    private Date mTimeStamp;
    private String mTrainFlightArrivalPlace;
    private Date mTrainFlightArrivalTime;
    private String mTrainFlightNo;
    private String mTrainFlightStartPlace;
    private Date mTrainFlightStartTime;

    public RawTrainFlightTickInfo(Cursor cursor) {
        Integer num = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.mId = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.mTimeStamp = cursor.isNull(2) ? null : new Date(cursor.getLong(2));
        this.mPassengerName = cursor.getString(3);
        this.mTrainFlightNo = cursor.getString(4);
        this.mSeatNo = cursor.getString(5);
        this.mTrainFlightStartTime = cursor.isNull(6) ? null : new Date(cursor.getLong(6));
        this.mTrainFlightArrivalTime = cursor.isNull(7) ? null : new Date(cursor.getLong(7));
        this.mTrainFlightStartPlace = cursor.getString(8);
        this.mTrainFlightArrivalPlace = cursor.getString(9);
        this.mReservedInt = !cursor.isNull(10) ? Integer.valueOf(cursor.getInt(10)) : num;
        this.mReservedText = cursor.getString(11);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public RawTrainFlightTickInfo(Parcel in) {
        super(in);
        String str = null;
        if (in.readByte() == 0) {
            this.mId = null;
            in.readInt();
        } else {
            this.mId = Integer.valueOf(in.readInt());
        }
        this.mTimeStamp = in.readByte() == 0 ? null : new Date(in.readLong());
        this.mPassengerName = in.readByte() == 0 ? null : in.readString();
        this.mTrainFlightNo = in.readByte() == 0 ? null : in.readString();
        this.mSeatNo = in.readByte() == 0 ? null : in.readString();
        this.mTrainFlightStartTime = in.readByte() == 0 ? null : new Date(in.readLong());
        this.mTrainFlightArrivalTime = in.readByte() == 0 ? null : new Date(in.readLong());
        this.mTrainFlightStartPlace = in.readByte() == 0 ? null : in.readString();
        this.mTrainFlightArrivalPlace = in.readByte() == 0 ? null : in.readString();
        this.mReservedInt = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mReservedText = in.readByte() != 0 ? in.readString() : str;
    }

    private RawTrainFlightTickInfo(Integer mId2, Date mTimeStamp2, String mPassengerName2, String mTrainFlightNo2, String mSeatNo2, Date mTrainFlightStartTime2, Date mTrainFlightArrivalTime2, String mTrainFlightStartPlace2, String mTrainFlightArrivalPlace2, Integer mReservedInt2, String mReservedText2) {
        this.mId = mId2;
        this.mTimeStamp = mTimeStamp2;
        this.mPassengerName = mPassengerName2;
        this.mTrainFlightNo = mTrainFlightNo2;
        this.mSeatNo = mSeatNo2;
        this.mTrainFlightStartTime = mTrainFlightStartTime2;
        this.mTrainFlightArrivalTime = mTrainFlightArrivalTime2;
        this.mTrainFlightStartPlace = mTrainFlightStartPlace2;
        this.mTrainFlightArrivalPlace = mTrainFlightArrivalPlace2;
        this.mReservedInt = mReservedInt2;
        this.mReservedText = mReservedText2;
    }

    public RawTrainFlightTickInfo() {
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

    public String getMPassengerName() {
        return this.mPassengerName;
    }

    public void setMPassengerName(String mPassengerName2) {
        this.mPassengerName = mPassengerName2;
        setValue();
    }

    public String getMTrainFlightNo() {
        return this.mTrainFlightNo;
    }

    public void setMTrainFlightNo(String mTrainFlightNo2) {
        this.mTrainFlightNo = mTrainFlightNo2;
        setValue();
    }

    public String getMSeatNo() {
        return this.mSeatNo;
    }

    public void setMSeatNo(String mSeatNo2) {
        this.mSeatNo = mSeatNo2;
        setValue();
    }

    public Date getMTrainFlightStartTime() {
        return this.mTrainFlightStartTime;
    }

    public void setMTrainFlightStartTime(Date mTrainFlightStartTime2) {
        this.mTrainFlightStartTime = mTrainFlightStartTime2;
        setValue();
    }

    public Date getMTrainFlightArrivalTime() {
        return this.mTrainFlightArrivalTime;
    }

    public void setMTrainFlightArrivalTime(Date mTrainFlightArrivalTime2) {
        this.mTrainFlightArrivalTime = mTrainFlightArrivalTime2;
        setValue();
    }

    public String getMTrainFlightStartPlace() {
        return this.mTrainFlightStartPlace;
    }

    public void setMTrainFlightStartPlace(String mTrainFlightStartPlace2) {
        this.mTrainFlightStartPlace = mTrainFlightStartPlace2;
        setValue();
    }

    public String getMTrainFlightArrivalPlace() {
        return this.mTrainFlightArrivalPlace;
    }

    public void setMTrainFlightArrivalPlace(String mTrainFlightArrivalPlace2) {
        this.mTrainFlightArrivalPlace = mTrainFlightArrivalPlace2;
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
        if (this.mPassengerName != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mPassengerName);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mTrainFlightNo != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mTrainFlightNo);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mSeatNo != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mSeatNo);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mTrainFlightStartTime != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.mTrainFlightStartTime.getTime());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mTrainFlightArrivalTime != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.mTrainFlightArrivalTime.getTime());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mTrainFlightStartPlace != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mTrainFlightStartPlace);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mTrainFlightArrivalPlace != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mTrainFlightArrivalPlace);
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

    public AEntityHelper<RawTrainFlightTickInfo> getHelper() {
        return RawTrainFlightTickInfoHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.collectencrypt.RawTrainFlightTickInfo";
    }

    public String getDatabaseName() {
        return "dsCollectEncrypt";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("RawTrainFlightTickInfo { mId: ").append(this.mId);
        sb.append(", mTimeStamp: ").append(this.mTimeStamp);
        sb.append(", mPassengerName: ").append(this.mPassengerName);
        sb.append(", mTrainFlightNo: ").append(this.mTrainFlightNo);
        sb.append(", mSeatNo: ").append(this.mSeatNo);
        sb.append(", mTrainFlightStartTime: ").append(this.mTrainFlightStartTime);
        sb.append(", mTrainFlightArrivalTime: ").append(this.mTrainFlightArrivalTime);
        sb.append(", mTrainFlightStartPlace: ").append(this.mTrainFlightStartPlace);
        sb.append(", mTrainFlightArrivalPlace: ").append(this.mTrainFlightArrivalPlace);
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
