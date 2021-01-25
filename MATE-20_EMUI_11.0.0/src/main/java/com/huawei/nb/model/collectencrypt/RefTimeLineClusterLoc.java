package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RefTimeLineClusterLoc extends AManagedObject {
    public static final Parcelable.Creator<RefTimeLineClusterLoc> CREATOR = new Parcelable.Creator<RefTimeLineClusterLoc>() {
        /* class com.huawei.nb.model.collectencrypt.RefTimeLineClusterLoc.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public RefTimeLineClusterLoc createFromParcel(Parcel parcel) {
            return new RefTimeLineClusterLoc(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public RefTimeLineClusterLoc[] newArray(int i) {
            return new RefTimeLineClusterLoc[i];
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

    @Override // com.huawei.odmf.core.AManagedObject, android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getDatabaseName() {
        return "dsCollectEncrypt";
    }

    public String getDatabaseVersion() {
        return "0.0.14";
    }

    public int getDatabaseVersionCode() {
        return 14;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getEntityName() {
        return "com.huawei.nb.model.collectencrypt.RefTimeLineClusterLoc";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public RefTimeLineClusterLoc(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Date date = null;
        this.mClusterID = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.mLongitude = cursor.isNull(2) ? null : Double.valueOf(cursor.getDouble(2));
        this.mLatitude = cursor.isNull(3) ? null : Double.valueOf(cursor.getDouble(3));
        this.mRange = cursor.isNull(4) ? null : Integer.valueOf(cursor.getInt(4));
        this.mDuration = cursor.isNull(5) ? null : Integer.valueOf(cursor.getInt(5));
        this.mLastVisit = !cursor.isNull(6) ? new Date(cursor.getLong(6)) : date;
        this.mReserved0 = cursor.getString(7);
        this.mReserved1 = cursor.getString(8);
    }

    public RefTimeLineClusterLoc(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.mClusterID = null;
            parcel.readInt();
        } else {
            this.mClusterID = Integer.valueOf(parcel.readInt());
        }
        this.mLongitude = parcel.readByte() == 0 ? null : Double.valueOf(parcel.readDouble());
        this.mLatitude = parcel.readByte() == 0 ? null : Double.valueOf(parcel.readDouble());
        this.mRange = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mDuration = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mLastVisit = parcel.readByte() == 0 ? null : new Date(parcel.readLong());
        this.mReserved0 = parcel.readByte() == 0 ? null : parcel.readString();
        this.mReserved1 = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private RefTimeLineClusterLoc(Integer num, Double d, Double d2, Integer num2, Integer num3, Date date, String str, String str2) {
        this.mClusterID = num;
        this.mLongitude = d;
        this.mLatitude = d2;
        this.mRange = num2;
        this.mDuration = num3;
        this.mLastVisit = date;
        this.mReserved0 = str;
        this.mReserved1 = str2;
    }

    public RefTimeLineClusterLoc() {
    }

    public Integer getMClusterID() {
        return this.mClusterID;
    }

    public void setMClusterID(Integer num) {
        this.mClusterID = num;
        setValue();
    }

    public Double getMLongitude() {
        return this.mLongitude;
    }

    public void setMLongitude(Double d) {
        this.mLongitude = d;
        setValue();
    }

    public Double getMLatitude() {
        return this.mLatitude;
    }

    public void setMLatitude(Double d) {
        this.mLatitude = d;
        setValue();
    }

    public Integer getMRange() {
        return this.mRange;
    }

    public void setMRange(Integer num) {
        this.mRange = num;
        setValue();
    }

    public Integer getMDuration() {
        return this.mDuration;
    }

    public void setMDuration(Integer num) {
        this.mDuration = num;
        setValue();
    }

    public Date getMLastVisit() {
        return this.mLastVisit;
    }

    public void setMLastVisit(Date date) {
        this.mLastVisit = date;
        setValue();
    }

    public String getMReserved0() {
        return this.mReserved0;
    }

    public void setMReserved0(String str) {
        this.mReserved0 = str;
        setValue();
    }

    public String getMReserved1() {
        return this.mReserved1;
    }

    public void setMReserved1(String str) {
        this.mReserved1 = str;
        setValue();
    }

    @Override // com.huawei.odmf.core.AManagedObject, android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        if (this.mClusterID != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mClusterID.intValue());
        } else {
            parcel.writeByte((byte) 0);
            parcel.writeInt(1);
        }
        if (this.mLongitude != null) {
            parcel.writeByte((byte) 1);
            parcel.writeDouble(this.mLongitude.doubleValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mLatitude != null) {
            parcel.writeByte((byte) 1);
            parcel.writeDouble(this.mLatitude.doubleValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mRange != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mRange.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mDuration != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mDuration.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mLastVisit != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.mLastVisit.getTime());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mReserved0 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mReserved0);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mReserved1 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mReserved1);
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<RefTimeLineClusterLoc> getHelper() {
        return RefTimeLineClusterLocHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "RefTimeLineClusterLoc { mClusterID: " + this.mClusterID + ", mLongitude: " + this.mLongitude + ", mLatitude: " + this.mLatitude + ", mRange: " + this.mRange + ", mDuration: " + this.mDuration + ", mLastVisit: " + this.mLastVisit + ", mReserved0: " + this.mReserved0 + ", mReserved1: " + this.mReserved1 + " }";
    }

    @Override // com.huawei.odmf.core.AManagedObject, java.lang.Object
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override // com.huawei.odmf.core.AManagedObject, java.lang.Object
    public int hashCode() {
        return super.hashCode();
    }
}
