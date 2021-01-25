package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RawARStatus extends AManagedObject {
    public static final Parcelable.Creator<RawARStatus> CREATOR = new Parcelable.Creator<RawARStatus>() {
        /* class com.huawei.nb.model.collectencrypt.RawARStatus.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public RawARStatus createFromParcel(Parcel parcel) {
            return new RawARStatus(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public RawARStatus[] newArray(int i) {
            return new RawARStatus[i];
        }
    };
    private Integer mId;
    private Integer mMotionType;
    private Integer mReservedInt;
    private String mReservedText;
    private Integer mStatus;
    private Date mTimeStamp;

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
        return "com.huawei.nb.model.collectencrypt.RawARStatus";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public RawARStatus(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Integer num = null;
        this.mId = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.mTimeStamp = cursor.isNull(2) ? null : new Date(cursor.getLong(2));
        this.mMotionType = cursor.isNull(3) ? null : Integer.valueOf(cursor.getInt(3));
        this.mStatus = cursor.isNull(4) ? null : Integer.valueOf(cursor.getInt(4));
        this.mReservedInt = !cursor.isNull(5) ? Integer.valueOf(cursor.getInt(5)) : num;
        this.mReservedText = cursor.getString(6);
    }

    public RawARStatus(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.mId = null;
            parcel.readInt();
        } else {
            this.mId = Integer.valueOf(parcel.readInt());
        }
        this.mTimeStamp = parcel.readByte() == 0 ? null : new Date(parcel.readLong());
        this.mMotionType = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mStatus = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mReservedInt = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mReservedText = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private RawARStatus(Integer num, Date date, Integer num2, Integer num3, Integer num4, String str) {
        this.mId = num;
        this.mTimeStamp = date;
        this.mMotionType = num2;
        this.mStatus = num3;
        this.mReservedInt = num4;
        this.mReservedText = str;
    }

    public RawARStatus() {
    }

    public Integer getMId() {
        return this.mId;
    }

    public void setMId(Integer num) {
        this.mId = num;
        setValue();
    }

    public Date getMTimeStamp() {
        return this.mTimeStamp;
    }

    public void setMTimeStamp(Date date) {
        this.mTimeStamp = date;
        setValue();
    }

    public Integer getMMotionType() {
        return this.mMotionType;
    }

    public void setMMotionType(Integer num) {
        this.mMotionType = num;
        setValue();
    }

    public Integer getMStatus() {
        return this.mStatus;
    }

    public void setMStatus(Integer num) {
        this.mStatus = num;
        setValue();
    }

    public Integer getMReservedInt() {
        return this.mReservedInt;
    }

    public void setMReservedInt(Integer num) {
        this.mReservedInt = num;
        setValue();
    }

    public String getMReservedText() {
        return this.mReservedText;
    }

    public void setMReservedText(String str) {
        this.mReservedText = str;
        setValue();
    }

    @Override // com.huawei.odmf.core.AManagedObject, android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        if (this.mId != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mId.intValue());
        } else {
            parcel.writeByte((byte) 0);
            parcel.writeInt(1);
        }
        if (this.mTimeStamp != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.mTimeStamp.getTime());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mMotionType != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mMotionType.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mStatus != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mStatus.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mReservedInt != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mReservedInt.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mReservedText != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mReservedText);
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<RawARStatus> getHelper() {
        return RawARStatusHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "RawARStatus { mId: " + this.mId + ", mTimeStamp: " + this.mTimeStamp + ", mMotionType: " + this.mMotionType + ", mStatus: " + this.mStatus + ", mReservedInt: " + this.mReservedInt + ", mReservedText: " + this.mReservedText + " }";
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
