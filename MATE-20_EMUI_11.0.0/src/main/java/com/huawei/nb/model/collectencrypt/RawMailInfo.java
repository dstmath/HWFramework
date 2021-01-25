package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RawMailInfo extends AManagedObject {
    public static final Parcelable.Creator<RawMailInfo> CREATOR = new Parcelable.Creator<RawMailInfo>() {
        /* class com.huawei.nb.model.collectencrypt.RawMailInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public RawMailInfo createFromParcel(Parcel parcel) {
            return new RawMailInfo(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public RawMailInfo[] newArray(int i) {
            return new RawMailInfo[i];
        }
    };
    private Integer mId;
    private String mMailAddress;
    private String mMailClientName;
    private String mMailContent;
    private String mMailFrom;
    private String mMailSubject;
    private Date mMailTime;
    private String mMailTo;
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
        return "com.huawei.nb.model.collectencrypt.RawMailInfo";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public RawMailInfo(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Integer num = null;
        this.mId = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.mTimeStamp = cursor.isNull(2) ? null : new Date(cursor.getLong(2));
        this.mMailClientName = cursor.getString(3);
        this.mMailAddress = cursor.getString(4);
        this.mMailSubject = cursor.getString(5);
        this.mMailContent = cursor.getString(6);
        this.mMailTime = cursor.isNull(7) ? null : new Date(cursor.getLong(7));
        this.mMailFrom = cursor.getString(8);
        this.mMailTo = cursor.getString(9);
        this.mReservedInt = !cursor.isNull(10) ? Integer.valueOf(cursor.getInt(10)) : num;
        this.mReservedText = cursor.getString(11);
    }

    public RawMailInfo(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.mId = null;
            parcel.readInt();
        } else {
            this.mId = Integer.valueOf(parcel.readInt());
        }
        this.mTimeStamp = parcel.readByte() == 0 ? null : new Date(parcel.readLong());
        this.mMailClientName = parcel.readByte() == 0 ? null : parcel.readString();
        this.mMailAddress = parcel.readByte() == 0 ? null : parcel.readString();
        this.mMailSubject = parcel.readByte() == 0 ? null : parcel.readString();
        this.mMailContent = parcel.readByte() == 0 ? null : parcel.readString();
        this.mMailTime = parcel.readByte() == 0 ? null : new Date(parcel.readLong());
        this.mMailFrom = parcel.readByte() == 0 ? null : parcel.readString();
        this.mMailTo = parcel.readByte() == 0 ? null : parcel.readString();
        this.mReservedInt = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mReservedText = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private RawMailInfo(Integer num, Date date, String str, String str2, String str3, String str4, Date date2, String str5, String str6, Integer num2, String str7) {
        this.mId = num;
        this.mTimeStamp = date;
        this.mMailClientName = str;
        this.mMailAddress = str2;
        this.mMailSubject = str3;
        this.mMailContent = str4;
        this.mMailTime = date2;
        this.mMailFrom = str5;
        this.mMailTo = str6;
        this.mReservedInt = num2;
        this.mReservedText = str7;
    }

    public RawMailInfo() {
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

    public String getMMailClientName() {
        return this.mMailClientName;
    }

    public void setMMailClientName(String str) {
        this.mMailClientName = str;
        setValue();
    }

    public String getMMailAddress() {
        return this.mMailAddress;
    }

    public void setMMailAddress(String str) {
        this.mMailAddress = str;
        setValue();
    }

    public String getMMailSubject() {
        return this.mMailSubject;
    }

    public void setMMailSubject(String str) {
        this.mMailSubject = str;
        setValue();
    }

    public String getMMailContent() {
        return this.mMailContent;
    }

    public void setMMailContent(String str) {
        this.mMailContent = str;
        setValue();
    }

    public Date getMMailTime() {
        return this.mMailTime;
    }

    public void setMMailTime(Date date) {
        this.mMailTime = date;
        setValue();
    }

    public String getMMailFrom() {
        return this.mMailFrom;
    }

    public void setMMailFrom(String str) {
        this.mMailFrom = str;
        setValue();
    }

    public String getMMailTo() {
        return this.mMailTo;
    }

    public void setMMailTo(String str) {
        this.mMailTo = str;
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
        if (this.mMailClientName != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mMailClientName);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mMailAddress != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mMailAddress);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mMailSubject != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mMailSubject);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mMailContent != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mMailContent);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mMailTime != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.mMailTime.getTime());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mMailFrom != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mMailFrom);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mMailTo != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mMailTo);
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
    public AEntityHelper<RawMailInfo> getHelper() {
        return RawMailInfoHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "RawMailInfo { mId: " + this.mId + ", mTimeStamp: " + this.mTimeStamp + ", mMailClientName: " + this.mMailClientName + ", mMailAddress: " + this.mMailAddress + ", mMailSubject: " + this.mMailSubject + ", mMailContent: " + this.mMailContent + ", mMailTime: " + this.mMailTime + ", mMailFrom: " + this.mMailFrom + ", mMailTo: " + this.mMailTo + ", mReservedInt: " + this.mReservedInt + ", mReservedText: " + this.mReservedText + " }";
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
