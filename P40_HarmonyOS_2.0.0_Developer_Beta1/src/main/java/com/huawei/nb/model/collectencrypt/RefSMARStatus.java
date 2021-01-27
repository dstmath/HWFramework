package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RefSMARStatus extends AManagedObject {
    public static final Parcelable.Creator<RefSMARStatus> CREATOR = new Parcelable.Creator<RefSMARStatus>() {
        /* class com.huawei.nb.model.collectencrypt.RefSMARStatus.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public RefSMARStatus createFromParcel(Parcel parcel) {
            return new RefSMARStatus(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public RefSMARStatus[] newArray(int i) {
            return new RefSMARStatus[i];
        }
    };
    private Date mEndTime;
    private Integer mMotionType;
    private String mReserved0;
    private String mReserved1;
    private Integer mSmarId;
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
        return "com.huawei.nb.model.collectencrypt.RefSMARStatus";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public RefSMARStatus(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Integer num = null;
        this.mSmarId = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.mStartTime = cursor.isNull(2) ? null : new Date(cursor.getLong(2));
        this.mEndTime = cursor.isNull(3) ? null : new Date(cursor.getLong(3));
        this.mMotionType = !cursor.isNull(4) ? Integer.valueOf(cursor.getInt(4)) : num;
        this.mReserved0 = cursor.getString(5);
        this.mReserved1 = cursor.getString(6);
    }

    public RefSMARStatus(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.mSmarId = null;
            parcel.readInt();
        } else {
            this.mSmarId = Integer.valueOf(parcel.readInt());
        }
        this.mStartTime = parcel.readByte() == 0 ? null : new Date(parcel.readLong());
        this.mEndTime = parcel.readByte() == 0 ? null : new Date(parcel.readLong());
        this.mMotionType = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mReserved0 = parcel.readByte() == 0 ? null : parcel.readString();
        this.mReserved1 = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private RefSMARStatus(Integer num, Date date, Date date2, Integer num2, String str, String str2) {
        this.mSmarId = num;
        this.mStartTime = date;
        this.mEndTime = date2;
        this.mMotionType = num2;
        this.mReserved0 = str;
        this.mReserved1 = str2;
    }

    public RefSMARStatus() {
    }

    public Integer getMSmarId() {
        return this.mSmarId;
    }

    public void setMSmarId(Integer num) {
        this.mSmarId = num;
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

    public Integer getMMotionType() {
        return this.mMotionType;
    }

    public void setMMotionType(Integer num) {
        this.mMotionType = num;
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
        if (this.mSmarId != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mSmarId.intValue());
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
        if (this.mMotionType != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mMotionType.intValue());
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
    public AEntityHelper<RefSMARStatus> getHelper() {
        return RefSMARStatusHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "RefSMARStatus { mSmarId: " + this.mSmarId + ", mStartTime: " + this.mStartTime + ", mEndTime: " + this.mEndTime + ", mMotionType: " + this.mMotionType + ", mReserved0: " + this.mReserved0 + ", mReserved1: " + this.mReserved1 + " }";
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
