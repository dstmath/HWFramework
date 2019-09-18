package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RefTimeLineLocationRecord extends AManagedObject {
    public static final Parcelable.Creator<RefTimeLineLocationRecord> CREATOR = new Parcelable.Creator<RefTimeLineLocationRecord>() {
        public RefTimeLineLocationRecord createFromParcel(Parcel in) {
            return new RefTimeLineLocationRecord(in);
        }

        public RefTimeLineLocationRecord[] newArray(int size) {
            return new RefTimeLineLocationRecord[size];
        }
    };
    private Integer mClusterLocId;
    private Date mEndTime;
    private Integer mRecordId;
    private String mReserved0;
    private String mReserved1;
    private Date mStartTime;

    public RefTimeLineLocationRecord(Cursor cursor) {
        Integer num = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.mRecordId = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.mStartTime = cursor.isNull(2) ? null : new Date(cursor.getLong(2));
        this.mEndTime = cursor.isNull(3) ? null : new Date(cursor.getLong(3));
        this.mClusterLocId = !cursor.isNull(4) ? Integer.valueOf(cursor.getInt(4)) : num;
        this.mReserved0 = cursor.getString(5);
        this.mReserved1 = cursor.getString(6);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public RefTimeLineLocationRecord(Parcel in) {
        super(in);
        String str = null;
        if (in.readByte() == 0) {
            this.mRecordId = null;
            in.readInt();
        } else {
            this.mRecordId = Integer.valueOf(in.readInt());
        }
        this.mStartTime = in.readByte() == 0 ? null : new Date(in.readLong());
        this.mEndTime = in.readByte() == 0 ? null : new Date(in.readLong());
        this.mClusterLocId = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mReserved0 = in.readByte() == 0 ? null : in.readString();
        this.mReserved1 = in.readByte() != 0 ? in.readString() : str;
    }

    private RefTimeLineLocationRecord(Integer mRecordId2, Date mStartTime2, Date mEndTime2, Integer mClusterLocId2, String mReserved02, String mReserved12) {
        this.mRecordId = mRecordId2;
        this.mStartTime = mStartTime2;
        this.mEndTime = mEndTime2;
        this.mClusterLocId = mClusterLocId2;
        this.mReserved0 = mReserved02;
        this.mReserved1 = mReserved12;
    }

    public RefTimeLineLocationRecord() {
    }

    public int describeContents() {
        return 0;
    }

    public Integer getMRecordId() {
        return this.mRecordId;
    }

    public void setMRecordId(Integer mRecordId2) {
        this.mRecordId = mRecordId2;
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

    public Integer getMClusterLocId() {
        return this.mClusterLocId;
    }

    public void setMClusterLocId(Integer mClusterLocId2) {
        this.mClusterLocId = mClusterLocId2;
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
        if (this.mRecordId != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mRecordId.intValue());
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
        if (this.mClusterLocId != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mClusterLocId.intValue());
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

    public AEntityHelper<RefTimeLineLocationRecord> getHelper() {
        return RefTimeLineLocationRecordHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.collectencrypt.RefTimeLineLocationRecord";
    }

    public String getDatabaseName() {
        return "dsCollectEncrypt";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("RefTimeLineLocationRecord { mRecordId: ").append(this.mRecordId);
        sb.append(", mStartTime: ").append(this.mStartTime);
        sb.append(", mEndTime: ").append(this.mEndTime);
        sb.append(", mClusterLocId: ").append(this.mClusterLocId);
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
