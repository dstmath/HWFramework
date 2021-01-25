package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RawCalenderConf extends AManagedObject {
    public static final Parcelable.Creator<RawCalenderConf> CREATOR = new Parcelable.Creator<RawCalenderConf>() {
        /* class com.huawei.nb.model.collectencrypt.RawCalenderConf.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public RawCalenderConf createFromParcel(Parcel parcel) {
            return new RawCalenderConf(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public RawCalenderConf[] newArray(int i) {
            return new RawCalenderConf[i];
        }
    };
    private String mConfAddr;
    private Date mConfBeginTime;
    private Date mConfEndTime;
    private String mConfSponsor;
    private Integer mConfStat;
    private String mConfTopic;
    private Integer mId;
    private Integer mReservedInt;
    private String mReservedText;
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
        return "com.huawei.nb.model.collectencrypt.RawCalenderConf";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public RawCalenderConf(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Integer num = null;
        this.mId = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.mTimeStamp = cursor.isNull(2) ? null : new Date(cursor.getLong(2));
        this.mConfTopic = cursor.getString(3);
        this.mConfBeginTime = cursor.isNull(4) ? null : new Date(cursor.getLong(4));
        this.mConfEndTime = cursor.isNull(5) ? null : new Date(cursor.getLong(5));
        this.mConfAddr = cursor.getString(6);
        this.mConfSponsor = cursor.getString(7);
        this.mConfStat = cursor.isNull(8) ? null : Integer.valueOf(cursor.getInt(8));
        this.mReservedInt = !cursor.isNull(9) ? Integer.valueOf(cursor.getInt(9)) : num;
        this.mReservedText = cursor.getString(10);
    }

    public RawCalenderConf(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.mId = null;
            parcel.readInt();
        } else {
            this.mId = Integer.valueOf(parcel.readInt());
        }
        this.mTimeStamp = parcel.readByte() == 0 ? null : new Date(parcel.readLong());
        this.mConfTopic = parcel.readByte() == 0 ? null : parcel.readString();
        this.mConfBeginTime = parcel.readByte() == 0 ? null : new Date(parcel.readLong());
        this.mConfEndTime = parcel.readByte() == 0 ? null : new Date(parcel.readLong());
        this.mConfAddr = parcel.readByte() == 0 ? null : parcel.readString();
        this.mConfSponsor = parcel.readByte() == 0 ? null : parcel.readString();
        this.mConfStat = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mReservedInt = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mReservedText = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private RawCalenderConf(Integer num, Date date, String str, Date date2, Date date3, String str2, String str3, Integer num2, Integer num3, String str4) {
        this.mId = num;
        this.mTimeStamp = date;
        this.mConfTopic = str;
        this.mConfBeginTime = date2;
        this.mConfEndTime = date3;
        this.mConfAddr = str2;
        this.mConfSponsor = str3;
        this.mConfStat = num2;
        this.mReservedInt = num3;
        this.mReservedText = str4;
    }

    public RawCalenderConf() {
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

    public String getMConfTopic() {
        return this.mConfTopic;
    }

    public void setMConfTopic(String str) {
        this.mConfTopic = str;
        setValue();
    }

    public Date getMConfBeginTime() {
        return this.mConfBeginTime;
    }

    public void setMConfBeginTime(Date date) {
        this.mConfBeginTime = date;
        setValue();
    }

    public Date getMConfEndTime() {
        return this.mConfEndTime;
    }

    public void setMConfEndTime(Date date) {
        this.mConfEndTime = date;
        setValue();
    }

    public String getMConfAddr() {
        return this.mConfAddr;
    }

    public void setMConfAddr(String str) {
        this.mConfAddr = str;
        setValue();
    }

    public String getMConfSponsor() {
        return this.mConfSponsor;
    }

    public void setMConfSponsor(String str) {
        this.mConfSponsor = str;
        setValue();
    }

    public Integer getMConfStat() {
        return this.mConfStat;
    }

    public void setMConfStat(Integer num) {
        this.mConfStat = num;
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
        if (this.mConfTopic != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mConfTopic);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mConfBeginTime != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.mConfBeginTime.getTime());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mConfEndTime != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.mConfEndTime.getTime());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mConfAddr != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mConfAddr);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mConfSponsor != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mConfSponsor);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mConfStat != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mConfStat.intValue());
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
    public AEntityHelper<RawCalenderConf> getHelper() {
        return RawCalenderConfHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "RawCalenderConf { mId: " + this.mId + ", mTimeStamp: " + this.mTimeStamp + ", mConfTopic: " + this.mConfTopic + ", mConfBeginTime: " + this.mConfBeginTime + ", mConfEndTime: " + this.mConfEndTime + ", mConfAddr: " + this.mConfAddr + ", mConfSponsor: " + this.mConfSponsor + ", mConfStat: " + this.mConfStat + ", mReservedInt: " + this.mReservedInt + ", mReservedText: " + this.mReservedText + " }";
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
