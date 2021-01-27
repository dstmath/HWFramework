package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RefTimeLineLocationRecord extends AManagedObject {
    public static final Parcelable.Creator<RefTimeLineLocationRecord> CREATOR = new Parcelable.Creator<RefTimeLineLocationRecord>() {
        /* class com.huawei.nb.model.collectencrypt.RefTimeLineLocationRecord.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public RefTimeLineLocationRecord createFromParcel(Parcel parcel) {
            return new RefTimeLineLocationRecord(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public RefTimeLineLocationRecord[] newArray(int i) {
            return new RefTimeLineLocationRecord[i];
        }
    };
    private Integer mClusterLocId;
    private Date mEndTime;
    private Integer mRecordId;
    private String mReserved0;
    private String mReserved1;
    private Date mStartTime;

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
        return "com.huawei.nb.model.collectencrypt.RefTimeLineLocationRecord";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public RefTimeLineLocationRecord(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Integer num = null;
        this.mRecordId = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.mStartTime = cursor.isNull(2) ? null : new Date(cursor.getLong(2));
        this.mEndTime = cursor.isNull(3) ? null : new Date(cursor.getLong(3));
        this.mClusterLocId = !cursor.isNull(4) ? Integer.valueOf(cursor.getInt(4)) : num;
        this.mReserved0 = cursor.getString(5);
        this.mReserved1 = cursor.getString(6);
    }

    public RefTimeLineLocationRecord(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.mRecordId = null;
            parcel.readInt();
        } else {
            this.mRecordId = Integer.valueOf(parcel.readInt());
        }
        this.mStartTime = parcel.readByte() == 0 ? null : new Date(parcel.readLong());
        this.mEndTime = parcel.readByte() == 0 ? null : new Date(parcel.readLong());
        this.mClusterLocId = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mReserved0 = parcel.readByte() == 0 ? null : parcel.readString();
        this.mReserved1 = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private RefTimeLineLocationRecord(Integer num, Date date, Date date2, Integer num2, String str, String str2) {
        this.mRecordId = num;
        this.mStartTime = date;
        this.mEndTime = date2;
        this.mClusterLocId = num2;
        this.mReserved0 = str;
        this.mReserved1 = str2;
    }

    public RefTimeLineLocationRecord() {
    }

    public Integer getMRecordId() {
        return this.mRecordId;
    }

    public void setMRecordId(Integer num) {
        this.mRecordId = num;
        setValue();
    }

    public Date getMStartTime() {
        return this.mStartTime;
    }

    public void setMStartTime(Date date) {
        this.mStartTime = date;
        setValue();
    }

    public Date getMEndTime() {
        return this.mEndTime;
    }

    public void setMEndTime(Date date) {
        this.mEndTime = date;
        setValue();
    }

    public Integer getMClusterLocId() {
        return this.mClusterLocId;
    }

    public void setMClusterLocId(Integer num) {
        this.mClusterLocId = num;
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
        if (this.mRecordId != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mRecordId.intValue());
        } else {
            parcel.writeByte((byte) 0);
            parcel.writeInt(1);
        }
        if (this.mStartTime != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.mStartTime.getTime());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mEndTime != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.mEndTime.getTime());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mClusterLocId != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mClusterLocId.intValue());
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
    public AEntityHelper<RefTimeLineLocationRecord> getHelper() {
        return RefTimeLineLocationRecordHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "RefTimeLineLocationRecord { mRecordId: " + this.mRecordId + ", mStartTime: " + this.mStartTime + ", mEndTime: " + this.mEndTime + ", mClusterLocId: " + this.mClusterLocId + ", mReserved0: " + this.mReserved0 + ", mReserved1: " + this.mReserved1 + " }";
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
