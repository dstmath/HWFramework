package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RawCalenderTip extends AManagedObject {
    public static final Parcelable.Creator<RawCalenderTip> CREATOR = new Parcelable.Creator<RawCalenderTip>() {
        /* class com.huawei.nb.model.collectencrypt.RawCalenderTip.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public RawCalenderTip createFromParcel(Parcel parcel) {
            return new RawCalenderTip(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public RawCalenderTip[] newArray(int i) {
            return new RawCalenderTip[i];
        }
    };
    private Integer mId;
    private Integer mReservedInt;
    private String mReservedText;
    private Date mTimeStamp;
    private Date mTipAlarmTime;
    private String mTipContent;
    private Date mTipEndTime;
    private Date mTipStartTime;
    private String mTipTitle;

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
        return "com.huawei.nb.model.collectencrypt.RawCalenderTip";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public RawCalenderTip(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Integer num = null;
        this.mId = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.mTimeStamp = cursor.isNull(2) ? null : new Date(cursor.getLong(2));
        this.mTipTitle = cursor.getString(3);
        this.mTipContent = cursor.getString(4);
        this.mTipStartTime = cursor.isNull(5) ? null : new Date(cursor.getLong(5));
        this.mTipEndTime = cursor.isNull(6) ? null : new Date(cursor.getLong(6));
        this.mTipAlarmTime = cursor.isNull(7) ? null : new Date(cursor.getLong(7));
        this.mReservedInt = !cursor.isNull(8) ? Integer.valueOf(cursor.getInt(8)) : num;
        this.mReservedText = cursor.getString(9);
    }

    public RawCalenderTip(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.mId = null;
            parcel.readInt();
        } else {
            this.mId = Integer.valueOf(parcel.readInt());
        }
        this.mTimeStamp = parcel.readByte() == 0 ? null : new Date(parcel.readLong());
        this.mTipTitle = parcel.readByte() == 0 ? null : parcel.readString();
        this.mTipContent = parcel.readByte() == 0 ? null : parcel.readString();
        this.mTipStartTime = parcel.readByte() == 0 ? null : new Date(parcel.readLong());
        this.mTipEndTime = parcel.readByte() == 0 ? null : new Date(parcel.readLong());
        this.mTipAlarmTime = parcel.readByte() == 0 ? null : new Date(parcel.readLong());
        this.mReservedInt = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mReservedText = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private RawCalenderTip(Integer num, Date date, String str, String str2, Date date2, Date date3, Date date4, Integer num2, String str3) {
        this.mId = num;
        this.mTimeStamp = date;
        this.mTipTitle = str;
        this.mTipContent = str2;
        this.mTipStartTime = date2;
        this.mTipEndTime = date3;
        this.mTipAlarmTime = date4;
        this.mReservedInt = num2;
        this.mReservedText = str3;
    }

    public RawCalenderTip() {
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

    public String getMTipTitle() {
        return this.mTipTitle;
    }

    public void setMTipTitle(String str) {
        this.mTipTitle = str;
        setValue();
    }

    public String getMTipContent() {
        return this.mTipContent;
    }

    public void setMTipContent(String str) {
        this.mTipContent = str;
        setValue();
    }

    public Date getMTipStartTime() {
        return this.mTipStartTime;
    }

    public void setMTipStartTime(Date date) {
        this.mTipStartTime = date;
        setValue();
    }

    public Date getMTipEndTime() {
        return this.mTipEndTime;
    }

    public void setMTipEndTime(Date date) {
        this.mTipEndTime = date;
        setValue();
    }

    public Date getMTipAlarmTime() {
        return this.mTipAlarmTime;
    }

    public void setMTipAlarmTime(Date date) {
        this.mTipAlarmTime = date;
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
        if (this.mTipTitle != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mTipTitle);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mTipContent != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mTipContent);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mTipStartTime != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.mTipStartTime.getTime());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mTipEndTime != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.mTipEndTime.getTime());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mTipAlarmTime != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.mTipAlarmTime.getTime());
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
    public AEntityHelper<RawCalenderTip> getHelper() {
        return RawCalenderTipHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "RawCalenderTip { mId: " + this.mId + ", mTimeStamp: " + this.mTimeStamp + ", mTipTitle: " + this.mTipTitle + ", mTipContent: " + this.mTipContent + ", mTipStartTime: " + this.mTipStartTime + ", mTipEndTime: " + this.mTipEndTime + ", mTipAlarmTime: " + this.mTipAlarmTime + ", mReservedInt: " + this.mReservedInt + ", mReservedText: " + this.mReservedText + " }";
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
