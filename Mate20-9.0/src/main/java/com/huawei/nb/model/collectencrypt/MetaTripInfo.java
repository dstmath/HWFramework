package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class MetaTripInfo extends AManagedObject {
    public static final Parcelable.Creator<MetaTripInfo> CREATOR = new Parcelable.Creator<MetaTripInfo>() {
        public MetaTripInfo createFromParcel(Parcel in) {
            return new MetaTripInfo(in);
        }

        public MetaTripInfo[] newArray(int size) {
            return new MetaTripInfo[size];
        }
    };
    private String mArrivalPlace;
    private Integer mId;
    private String mProvider;
    private Integer mReservedInt;
    private String mReservedText;
    private String mSeatNo;
    private String mStartPlace;
    private Date mStartTime;
    private Date mTimeStamp;
    private String mTripNo;
    private String mTripSeat;
    private String mTripType;

    public MetaTripInfo(Cursor cursor) {
        Integer num = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.mId = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.mTimeStamp = cursor.isNull(2) ? null : new Date(cursor.getLong(2));
        this.mTripType = cursor.getString(3);
        this.mTripNo = cursor.getString(4);
        this.mSeatNo = cursor.getString(5);
        this.mStartTime = cursor.isNull(6) ? null : new Date(cursor.getLong(6));
        this.mStartPlace = cursor.getString(7);
        this.mArrivalPlace = cursor.getString(8);
        this.mProvider = cursor.getString(9);
        this.mTripSeat = cursor.getString(10);
        this.mReservedInt = !cursor.isNull(11) ? Integer.valueOf(cursor.getInt(11)) : num;
        this.mReservedText = cursor.getString(12);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public MetaTripInfo(Parcel in) {
        super(in);
        String str = null;
        if (in.readByte() == 0) {
            this.mId = null;
            in.readInt();
        } else {
            this.mId = Integer.valueOf(in.readInt());
        }
        this.mTimeStamp = in.readByte() == 0 ? null : new Date(in.readLong());
        this.mTripType = in.readByte() == 0 ? null : in.readString();
        this.mTripNo = in.readByte() == 0 ? null : in.readString();
        this.mSeatNo = in.readByte() == 0 ? null : in.readString();
        this.mStartTime = in.readByte() == 0 ? null : new Date(in.readLong());
        this.mStartPlace = in.readByte() == 0 ? null : in.readString();
        this.mArrivalPlace = in.readByte() == 0 ? null : in.readString();
        this.mProvider = in.readByte() == 0 ? null : in.readString();
        this.mTripSeat = in.readByte() == 0 ? null : in.readString();
        this.mReservedInt = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mReservedText = in.readByte() != 0 ? in.readString() : str;
    }

    private MetaTripInfo(Integer mId2, Date mTimeStamp2, String mTripType2, String mTripNo2, String mSeatNo2, Date mStartTime2, String mStartPlace2, String mArrivalPlace2, String mProvider2, String mTripSeat2, Integer mReservedInt2, String mReservedText2) {
        this.mId = mId2;
        this.mTimeStamp = mTimeStamp2;
        this.mTripType = mTripType2;
        this.mTripNo = mTripNo2;
        this.mSeatNo = mSeatNo2;
        this.mStartTime = mStartTime2;
        this.mStartPlace = mStartPlace2;
        this.mArrivalPlace = mArrivalPlace2;
        this.mProvider = mProvider2;
        this.mTripSeat = mTripSeat2;
        this.mReservedInt = mReservedInt2;
        this.mReservedText = mReservedText2;
    }

    public MetaTripInfo() {
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

    public String getMTripType() {
        return this.mTripType;
    }

    public void setMTripType(String mTripType2) {
        this.mTripType = mTripType2;
        setValue();
    }

    public String getMTripNo() {
        return this.mTripNo;
    }

    public void setMTripNo(String mTripNo2) {
        this.mTripNo = mTripNo2;
        setValue();
    }

    public String getMSeatNo() {
        return this.mSeatNo;
    }

    public void setMSeatNo(String mSeatNo2) {
        this.mSeatNo = mSeatNo2;
        setValue();
    }

    public Date getMStartTime() {
        return this.mStartTime;
    }

    public void setMStartTime(Date mStartTime2) {
        this.mStartTime = mStartTime2;
        setValue();
    }

    public String getMStartPlace() {
        return this.mStartPlace;
    }

    public void setMStartPlace(String mStartPlace2) {
        this.mStartPlace = mStartPlace2;
        setValue();
    }

    public String getMArrivalPlace() {
        return this.mArrivalPlace;
    }

    public void setMArrivalPlace(String mArrivalPlace2) {
        this.mArrivalPlace = mArrivalPlace2;
        setValue();
    }

    public String getMProvider() {
        return this.mProvider;
    }

    public void setMProvider(String mProvider2) {
        this.mProvider = mProvider2;
        setValue();
    }

    public String getMTripSeat() {
        return this.mTripSeat;
    }

    public void setMTripSeat(String mTripSeat2) {
        this.mTripSeat = mTripSeat2;
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
        if (this.mTripType != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mTripType);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mTripNo != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mTripNo);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mSeatNo != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mSeatNo);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mStartTime != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.mStartTime.getTime());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mStartPlace != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mStartPlace);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mArrivalPlace != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mArrivalPlace);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mProvider != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mProvider);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mTripSeat != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mTripSeat);
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

    public AEntityHelper<MetaTripInfo> getHelper() {
        return MetaTripInfoHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.collectencrypt.MetaTripInfo";
    }

    public String getDatabaseName() {
        return "dsCollectEncrypt";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("MetaTripInfo { mId: ").append(this.mId);
        sb.append(", mTimeStamp: ").append(this.mTimeStamp);
        sb.append(", mTripType: ").append(this.mTripType);
        sb.append(", mTripNo: ").append(this.mTripNo);
        sb.append(", mSeatNo: ").append(this.mSeatNo);
        sb.append(", mStartTime: ").append(this.mStartTime);
        sb.append(", mStartPlace: ").append(this.mStartPlace);
        sb.append(", mArrivalPlace: ").append(this.mArrivalPlace);
        sb.append(", mProvider: ").append(this.mProvider);
        sb.append(", mTripSeat: ").append(this.mTripSeat);
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
