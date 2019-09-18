package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RefSMARStatus extends AManagedObject {
    public static final Parcelable.Creator<RefSMARStatus> CREATOR = new Parcelable.Creator<RefSMARStatus>() {
        public RefSMARStatus createFromParcel(Parcel in) {
            return new RefSMARStatus(in);
        }

        public RefSMARStatus[] newArray(int size) {
            return new RefSMARStatus[size];
        }
    };
    private Date mEndTime;
    private Integer mMotionType;
    private String mReserved0;
    private String mReserved1;
    private Integer mSmarId;
    private Date mStartTime;

    public RefSMARStatus(Cursor cursor) {
        Integer num = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.mSmarId = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.mStartTime = cursor.isNull(2) ? null : new Date(cursor.getLong(2));
        this.mEndTime = cursor.isNull(3) ? null : new Date(cursor.getLong(3));
        this.mMotionType = !cursor.isNull(4) ? Integer.valueOf(cursor.getInt(4)) : num;
        this.mReserved0 = cursor.getString(5);
        this.mReserved1 = cursor.getString(6);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public RefSMARStatus(Parcel in) {
        super(in);
        String str = null;
        if (in.readByte() == 0) {
            this.mSmarId = null;
            in.readInt();
        } else {
            this.mSmarId = Integer.valueOf(in.readInt());
        }
        this.mStartTime = in.readByte() == 0 ? null : new Date(in.readLong());
        this.mEndTime = in.readByte() == 0 ? null : new Date(in.readLong());
        this.mMotionType = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mReserved0 = in.readByte() == 0 ? null : in.readString();
        this.mReserved1 = in.readByte() != 0 ? in.readString() : str;
    }

    private RefSMARStatus(Integer mSmarId2, Date mStartTime2, Date mEndTime2, Integer mMotionType2, String mReserved02, String mReserved12) {
        this.mSmarId = mSmarId2;
        this.mStartTime = mStartTime2;
        this.mEndTime = mEndTime2;
        this.mMotionType = mMotionType2;
        this.mReserved0 = mReserved02;
        this.mReserved1 = mReserved12;
    }

    public RefSMARStatus() {
    }

    public int describeContents() {
        return 0;
    }

    public Integer getMSmarId() {
        return this.mSmarId;
    }

    public void setMSmarId(Integer mSmarId2) {
        this.mSmarId = mSmarId2;
        setValue();
    }

    public Date getMStartTime() {
        return this.mStartTime;
    }

    public void setMStartTime(Date mStartTime2) {
        this.mStartTime = mStartTime2;
        setValue();
    }

    public Date getMEndTime() {
        return this.mEndTime;
    }

    public void setMEndTime(Date mEndTime2) {
        this.mEndTime = mEndTime2;
        setValue();
    }

    public Integer getMMotionType() {
        return this.mMotionType;
    }

    public void setMMotionType(Integer mMotionType2) {
        this.mMotionType = mMotionType2;
        setValue();
    }

    public String getMReserved0() {
        return this.mReserved0;
    }

    public void setMReserved0(String mReserved02) {
        this.mReserved0 = mReserved02;
        setValue();
    }

    public String getMReserved1() {
        return this.mReserved1;
    }

    public void setMReserved1(String mReserved12) {
        this.mReserved1 = mReserved12;
        setValue();
    }

    public void writeToParcel(Parcel out, int ignored) {
        super.writeToParcel(out, ignored);
        if (this.mSmarId != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mSmarId.intValue());
        } else {
            out.writeByte((byte) 0);
            out.writeInt(1);
        }
        if (this.mStartTime != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.mStartTime.getTime());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mEndTime != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.mEndTime.getTime());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mMotionType != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mMotionType.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mReserved0 != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mReserved0);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mReserved1 != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mReserved1);
            return;
        }
        out.writeByte((byte) 0);
    }

    public AEntityHelper<RefSMARStatus> getHelper() {
        return RefSMARStatusHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.collectencrypt.RefSMARStatus";
    }

    public String getDatabaseName() {
        return "dsCollectEncrypt";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("RefSMARStatus { mSmarId: ").append(this.mSmarId);
        sb.append(", mStartTime: ").append(this.mStartTime);
        sb.append(", mEndTime: ").append(this.mEndTime);
        sb.append(", mMotionType: ").append(this.mMotionType);
        sb.append(", mReserved0: ").append(this.mReserved0);
        sb.append(", mReserved1: ").append(this.mReserved1);
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
