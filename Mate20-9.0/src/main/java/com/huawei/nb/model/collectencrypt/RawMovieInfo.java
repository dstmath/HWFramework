package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RawMovieInfo extends AManagedObject {
    public static final Parcelable.Creator<RawMovieInfo> CREATOR = new Parcelable.Creator<RawMovieInfo>() {
        public RawMovieInfo createFromParcel(Parcel in) {
            return new RawMovieInfo(in);
        }

        public RawMovieInfo[] newArray(int size) {
            return new RawMovieInfo[size];
        }
    };
    private String mCinemaAddr;
    private String mCinemaRoomNo;
    private String mCinemaSeatNo;
    private Integer mId;
    private Date mMovideStartTime;
    private String mMovieName;
    private Integer mReservedInt;
    private String mReservedText;
    private Date mTimeStamp;

    public RawMovieInfo(Cursor cursor) {
        Integer num = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.mId = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.mTimeStamp = cursor.isNull(2) ? null : new Date(cursor.getLong(2));
        this.mMovieName = cursor.getString(3);
        this.mMovideStartTime = cursor.isNull(4) ? null : new Date(cursor.getLong(4));
        this.mCinemaAddr = cursor.getString(5);
        this.mCinemaRoomNo = cursor.getString(6);
        this.mCinemaSeatNo = cursor.getString(7);
        this.mReservedInt = !cursor.isNull(8) ? Integer.valueOf(cursor.getInt(8)) : num;
        this.mReservedText = cursor.getString(9);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public RawMovieInfo(Parcel in) {
        super(in);
        String str = null;
        if (in.readByte() == 0) {
            this.mId = null;
            in.readInt();
        } else {
            this.mId = Integer.valueOf(in.readInt());
        }
        this.mTimeStamp = in.readByte() == 0 ? null : new Date(in.readLong());
        this.mMovieName = in.readByte() == 0 ? null : in.readString();
        this.mMovideStartTime = in.readByte() == 0 ? null : new Date(in.readLong());
        this.mCinemaAddr = in.readByte() == 0 ? null : in.readString();
        this.mCinemaRoomNo = in.readByte() == 0 ? null : in.readString();
        this.mCinemaSeatNo = in.readByte() == 0 ? null : in.readString();
        this.mReservedInt = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mReservedText = in.readByte() != 0 ? in.readString() : str;
    }

    private RawMovieInfo(Integer mId2, Date mTimeStamp2, String mMovieName2, Date mMovideStartTime2, String mCinemaAddr2, String mCinemaRoomNo2, String mCinemaSeatNo2, Integer mReservedInt2, String mReservedText2) {
        this.mId = mId2;
        this.mTimeStamp = mTimeStamp2;
        this.mMovieName = mMovieName2;
        this.mMovideStartTime = mMovideStartTime2;
        this.mCinemaAddr = mCinemaAddr2;
        this.mCinemaRoomNo = mCinemaRoomNo2;
        this.mCinemaSeatNo = mCinemaSeatNo2;
        this.mReservedInt = mReservedInt2;
        this.mReservedText = mReservedText2;
    }

    public RawMovieInfo() {
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

    public String getMMovieName() {
        return this.mMovieName;
    }

    public void setMMovieName(String mMovieName2) {
        this.mMovieName = mMovieName2;
        setValue();
    }

    public Date getMMovideStartTime() {
        return this.mMovideStartTime;
    }

    public void setMMovideStartTime(Date mMovideStartTime2) {
        this.mMovideStartTime = mMovideStartTime2;
        setValue();
    }

    public String getMCinemaAddr() {
        return this.mCinemaAddr;
    }

    public void setMCinemaAddr(String mCinemaAddr2) {
        this.mCinemaAddr = mCinemaAddr2;
        setValue();
    }

    public String getMCinemaRoomNo() {
        return this.mCinemaRoomNo;
    }

    public void setMCinemaRoomNo(String mCinemaRoomNo2) {
        this.mCinemaRoomNo = mCinemaRoomNo2;
        setValue();
    }

    public String getMCinemaSeatNo() {
        return this.mCinemaSeatNo;
    }

    public void setMCinemaSeatNo(String mCinemaSeatNo2) {
        this.mCinemaSeatNo = mCinemaSeatNo2;
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
        if (this.mMovieName != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mMovieName);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mMovideStartTime != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.mMovideStartTime.getTime());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mCinemaAddr != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mCinemaAddr);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mCinemaRoomNo != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mCinemaRoomNo);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mCinemaSeatNo != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mCinemaSeatNo);
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

    public AEntityHelper<RawMovieInfo> getHelper() {
        return RawMovieInfoHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.collectencrypt.RawMovieInfo";
    }

    public String getDatabaseName() {
        return "dsCollectEncrypt";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("RawMovieInfo { mId: ").append(this.mId);
        sb.append(", mTimeStamp: ").append(this.mTimeStamp);
        sb.append(", mMovieName: ").append(this.mMovieName);
        sb.append(", mMovideStartTime: ").append(this.mMovideStartTime);
        sb.append(", mCinemaAddr: ").append(this.mCinemaAddr);
        sb.append(", mCinemaRoomNo: ").append(this.mCinemaRoomNo);
        sb.append(", mCinemaSeatNo: ").append(this.mCinemaSeatNo);
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
