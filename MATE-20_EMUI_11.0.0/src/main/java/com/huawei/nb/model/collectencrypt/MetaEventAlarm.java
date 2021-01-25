package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class MetaEventAlarm extends AManagedObject {
    public static final Parcelable.Creator<MetaEventAlarm> CREATOR = new Parcelable.Creator<MetaEventAlarm>() {
        /* class com.huawei.nb.model.collectencrypt.MetaEventAlarm.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public MetaEventAlarm createFromParcel(Parcel parcel) {
            return new MetaEventAlarm(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public MetaEventAlarm[] newArray(int i) {
            return new MetaEventAlarm[i];
        }
    };
    private String mAddress;
    private Date mAlarmTime;
    private Integer mEventID;
    private String mEventInfo;
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
        return "com.huawei.nb.model.collectencrypt.MetaEventAlarm";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public MetaEventAlarm(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Integer num = null;
        this.mId = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.mTimeStamp = cursor.isNull(2) ? null : new Date(cursor.getLong(2));
        this.mEventID = cursor.isNull(3) ? null : Integer.valueOf(cursor.getInt(3));
        this.mAlarmTime = cursor.isNull(4) ? null : new Date(cursor.getLong(4));
        this.mEventInfo = cursor.getString(5);
        this.mAddress = cursor.getString(6);
        this.mReservedInt = !cursor.isNull(7) ? Integer.valueOf(cursor.getInt(7)) : num;
        this.mReservedText = cursor.getString(8);
    }

    public MetaEventAlarm(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.mId = null;
            parcel.readInt();
        } else {
            this.mId = Integer.valueOf(parcel.readInt());
        }
        this.mTimeStamp = parcel.readByte() == 0 ? null : new Date(parcel.readLong());
        this.mEventID = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mAlarmTime = parcel.readByte() == 0 ? null : new Date(parcel.readLong());
        this.mEventInfo = parcel.readByte() == 0 ? null : parcel.readString();
        this.mAddress = parcel.readByte() == 0 ? null : parcel.readString();
        this.mReservedInt = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mReservedText = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private MetaEventAlarm(Integer num, Date date, Integer num2, Date date2, String str, String str2, Integer num3, String str3) {
        this.mId = num;
        this.mTimeStamp = date;
        this.mEventID = num2;
        this.mAlarmTime = date2;
        this.mEventInfo = str;
        this.mAddress = str2;
        this.mReservedInt = num3;
        this.mReservedText = str3;
    }

    public MetaEventAlarm() {
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

    public Integer getMEventID() {
        return this.mEventID;
    }

    public void setMEventID(Integer num) {
        this.mEventID = num;
        setValue();
    }

    public Date getMAlarmTime() {
        return this.mAlarmTime;
    }

    public void setMAlarmTime(Date date) {
        this.mAlarmTime = date;
        setValue();
    }

    public String getMEventInfo() {
        return this.mEventInfo;
    }

    public void setMEventInfo(String str) {
        this.mEventInfo = str;
        setValue();
    }

    public String getMAddress() {
        return this.mAddress;
    }

    public void setMAddress(String str) {
        this.mAddress = str;
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
        if (this.mEventID != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mEventID.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mAlarmTime != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.mAlarmTime.getTime());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mEventInfo != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mEventInfo);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mAddress != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mAddress);
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
    public AEntityHelper<MetaEventAlarm> getHelper() {
        return MetaEventAlarmHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "MetaEventAlarm { mId: " + this.mId + ", mTimeStamp: " + this.mTimeStamp + ", mEventID: " + this.mEventID + ", mAlarmTime: " + this.mAlarmTime + ", mEventInfo: " + this.mEventInfo + ", mAddress: " + this.mAddress + ", mReservedInt: " + this.mReservedInt + ", mReservedText: " + this.mReservedText + " }";
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
