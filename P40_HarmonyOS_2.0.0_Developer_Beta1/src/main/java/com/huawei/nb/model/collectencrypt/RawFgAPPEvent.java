package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RawFgAPPEvent extends AManagedObject {
    public static final Parcelable.Creator<RawFgAPPEvent> CREATOR = new Parcelable.Creator<RawFgAPPEvent>() {
        /* class com.huawei.nb.model.collectencrypt.RawFgAPPEvent.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public RawFgAPPEvent createFromParcel(Parcel parcel) {
            return new RawFgAPPEvent(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public RawFgAPPEvent[] newArray(int i) {
            return new RawFgAPPEvent[i];
        }
    };
    private String mActivityName;
    private Integer mId;
    private String mPackageName;
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
        return "com.huawei.nb.model.collectencrypt.RawFgAPPEvent";
    }

    public String getEntityVersion() {
        return "0.0.2";
    }

    public int getEntityVersionCode() {
        return 2;
    }

    public RawFgAPPEvent(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Integer num = null;
        this.mId = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.mTimeStamp = cursor.isNull(2) ? null : new Date(cursor.getLong(2));
        this.mPackageName = cursor.getString(3);
        this.mStatus = cursor.isNull(4) ? null : Integer.valueOf(cursor.getInt(4));
        this.mReservedInt = !cursor.isNull(5) ? Integer.valueOf(cursor.getInt(5)) : num;
        this.mReservedText = cursor.getString(6);
        this.mActivityName = cursor.getString(7);
    }

    public RawFgAPPEvent(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.mId = null;
            parcel.readInt();
        } else {
            this.mId = Integer.valueOf(parcel.readInt());
        }
        this.mTimeStamp = parcel.readByte() == 0 ? null : new Date(parcel.readLong());
        this.mPackageName = parcel.readByte() == 0 ? null : parcel.readString();
        this.mStatus = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mReservedInt = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mReservedText = parcel.readByte() == 0 ? null : parcel.readString();
        this.mActivityName = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private RawFgAPPEvent(Integer num, Date date, String str, Integer num2, Integer num3, String str2, String str3) {
        this.mId = num;
        this.mTimeStamp = date;
        this.mPackageName = str;
        this.mStatus = num2;
        this.mReservedInt = num3;
        this.mReservedText = str2;
        this.mActivityName = str3;
    }

    public RawFgAPPEvent() {
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

    public String getMPackageName() {
        return this.mPackageName;
    }

    public void setMPackageName(String str) {
        this.mPackageName = str;
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

    public String getMActivityName() {
        return this.mActivityName;
    }

    public void setMActivityName(String str) {
        this.mActivityName = str;
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
        if (this.mPackageName != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mPackageName);
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
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mActivityName != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mActivityName);
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<RawFgAPPEvent> getHelper() {
        return RawFgAPPEventHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "RawFgAPPEvent { mId: " + this.mId + ", mTimeStamp: " + this.mTimeStamp + ", mPackageName: " + this.mPackageName + ", mStatus: " + this.mStatus + ", mReservedInt: " + this.mReservedInt + ", mReservedText: " + this.mReservedText + ", mActivityName: " + this.mActivityName + " }";
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
