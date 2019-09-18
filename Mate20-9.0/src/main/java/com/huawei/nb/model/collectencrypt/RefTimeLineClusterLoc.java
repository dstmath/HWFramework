package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RefTimeLineClusterLoc extends AManagedObject {
    public static final Parcelable.Creator<RefTimeLineClusterLoc> CREATOR = new Parcelable.Creator<RefTimeLineClusterLoc>() {
        public RefTimeLineClusterLoc createFromParcel(Parcel in) {
            return new RefTimeLineClusterLoc(in);
        }

        public RefTimeLineClusterLoc[] newArray(int size) {
            return new RefTimeLineClusterLoc[size];
        }
    };
    private Integer mClusterID;
    private Integer mDuration;
    private Date mLastVisit;
    private Double mLatitude;
    private Double mLongitude;
    private Integer mRange;
    private String mReserved0;
    private String mReserved1;

    public RefTimeLineClusterLoc(Cursor cursor) {
        Date date = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.mClusterID = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.mLongitude = cursor.isNull(2) ? null : Double.valueOf(cursor.getDouble(2));
        this.mLatitude = cursor.isNull(3) ? null : Double.valueOf(cursor.getDouble(3));
        this.mRange = cursor.isNull(4) ? null : Integer.valueOf(cursor.getInt(4));
        this.mDuration = cursor.isNull(5) ? null : Integer.valueOf(cursor.getInt(5));
        this.mLastVisit = !cursor.isNull(6) ? new Date(cursor.getLong(6)) : date;
        this.mReserved0 = cursor.getString(7);
        this.mReserved1 = cursor.getString(8);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public RefTimeLineClusterLoc(Parcel in) {
        super(in);
        String str = null;
        if (in.readByte() == 0) {
            this.mClusterID = null;
            in.readInt();
        } else {
            this.mClusterID = Integer.valueOf(in.readInt());
        }
        this.mLongitude = in.readByte() == 0 ? null : Double.valueOf(in.readDouble());
        this.mLatitude = in.readByte() == 0 ? null : Double.valueOf(in.readDouble());
        this.mRange = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mDuration = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mLastVisit = in.readByte() == 0 ? null : new Date(in.readLong());
        this.mReserved0 = in.readByte() == 0 ? null : in.readString();
        this.mReserved1 = in.readByte() != 0 ? in.readString() : str;
    }

    private RefTimeLineClusterLoc(Integer mClusterID2, Double mLongitude2, Double mLatitude2, Integer mRange2, Integer mDuration2, Date mLastVisit2, String mReserved02, String mReserved12) {
        this.mClusterID = mClusterID2;
        this.mLongitude = mLongitude2;
        this.mLatitude = mLatitude2;
        this.mRange = mRange2;
        this.mDuration = mDuration2;
        this.mLastVisit = mLastVisit2;
        this.mReserved0 = mReserved02;
        this.mReserved1 = mReserved12;
    }

    public RefTimeLineClusterLoc() {
    }

    public int describeContents() {
        return 0;
    }

    public Integer getMClusterID() {
        return this.mClusterID;
    }

    public void setMClusterID(Integer mClusterID2) {
        this.mClusterID = mClusterID2;
        setValue();
    }

    public Double getMLongitude() {
        return this.mLongitude;
    }

    public void setMLongitude(Double mLongitude2) {
        this.mLongitude = mLongitude2;
        setValue();
    }

    public Double getMLatitude() {
        return this.mLatitude;
    }

    public void setMLatitude(Double mLatitude2) {
        this.mLatitude = mLatitude2;
        setValue();
    }

    public Integer getMRange() {
        return this.mRange;
    }

    public void setMRange(Integer mRange2) {
        this.mRange = mRange2;
        setValue();
    }

    public Integer getMDuration() {
        return this.mDuration;
    }

    public void setMDuration(Integer mDuration2) {
        this.mDuration = mDuration2;
        setValue();
    }

    public Date getMLastVisit() {
        return this.mLastVisit;
    }

    public void setMLastVisit(Date mLastVisit2) {
        this.mLastVisit = mLastVisit2;
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
        if (this.mClusterID != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mClusterID.intValue());
        } else {
            out.writeByte((byte) 0);
            out.writeInt(1);
        }
        if (this.mLongitude != null) {
            out.writeByte((byte) 1);
            out.writeDouble(this.mLongitude.doubleValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mLatitude != null) {
            out.writeByte((byte) 1);
            out.writeDouble(this.mLatitude.doubleValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mRange != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mRange.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mDuration != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mDuration.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mLastVisit != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.mLastVisit.getTime());
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

    public AEntityHelper<RefTimeLineClusterLoc> getHelper() {
        return RefTimeLineClusterLocHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.collectencrypt.RefTimeLineClusterLoc";
    }

    public String getDatabaseName() {
        return "dsCollectEncrypt";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("RefTimeLineClusterLoc { mClusterID: ").append(this.mClusterID);
        sb.append(", mLongitude: ").append(this.mLongitude);
        sb.append(", mLatitude: ").append(this.mLatitude);
        sb.append(", mRange: ").append(this.mRange);
        sb.append(", mDuration: ").append(this.mDuration);
        sb.append(", mLastVisit: ").append(this.mLastVisit);
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
