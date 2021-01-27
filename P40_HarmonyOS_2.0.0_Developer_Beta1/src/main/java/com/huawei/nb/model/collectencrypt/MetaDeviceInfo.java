package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class MetaDeviceInfo extends AManagedObject {
    public static final Parcelable.Creator<MetaDeviceInfo> CREATOR = new Parcelable.Creator<MetaDeviceInfo>() {
        /* class com.huawei.nb.model.collectencrypt.MetaDeviceInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public MetaDeviceInfo createFromParcel(Parcel parcel) {
            return new MetaDeviceInfo(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public MetaDeviceInfo[] newArray(int i) {
            return new MetaDeviceInfo[i];
        }
    };
    private String mDeviceName;
    private String mHardwareVer;
    private String mIMEI1;
    private String mIMEI2;
    private String mIMSI1;
    private String mIMSI2;
    private Integer mId;
    private String mLanguageRegion;
    private Integer mReservedInt;
    private String mReservedText;
    private String mSN;
    private String mSoftwareVer;
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
        return "com.huawei.nb.model.collectencrypt.MetaDeviceInfo";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public MetaDeviceInfo(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Integer num = null;
        this.mId = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.mTimeStamp = cursor.isNull(2) ? null : new Date(cursor.getLong(2));
        this.mDeviceName = cursor.getString(3);
        this.mHardwareVer = cursor.getString(4);
        this.mSoftwareVer = cursor.getString(5);
        this.mIMEI1 = cursor.getString(6);
        this.mIMEI2 = cursor.getString(7);
        this.mIMSI1 = cursor.getString(8);
        this.mIMSI2 = cursor.getString(9);
        this.mSN = cursor.getString(10);
        this.mLanguageRegion = cursor.getString(11);
        this.mReservedInt = !cursor.isNull(12) ? Integer.valueOf(cursor.getInt(12)) : num;
        this.mReservedText = cursor.getString(13);
    }

    public MetaDeviceInfo(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.mId = null;
            parcel.readInt();
        } else {
            this.mId = Integer.valueOf(parcel.readInt());
        }
        this.mTimeStamp = parcel.readByte() == 0 ? null : new Date(parcel.readLong());
        this.mDeviceName = parcel.readByte() == 0 ? null : parcel.readString();
        this.mHardwareVer = parcel.readByte() == 0 ? null : parcel.readString();
        this.mSoftwareVer = parcel.readByte() == 0 ? null : parcel.readString();
        this.mIMEI1 = parcel.readByte() == 0 ? null : parcel.readString();
        this.mIMEI2 = parcel.readByte() == 0 ? null : parcel.readString();
        this.mIMSI1 = parcel.readByte() == 0 ? null : parcel.readString();
        this.mIMSI2 = parcel.readByte() == 0 ? null : parcel.readString();
        this.mSN = parcel.readByte() == 0 ? null : parcel.readString();
        this.mLanguageRegion = parcel.readByte() == 0 ? null : parcel.readString();
        this.mReservedInt = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mReservedText = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private MetaDeviceInfo(Integer num, Date date, String str, String str2, String str3, String str4, String str5, String str6, String str7, String str8, String str9, Integer num2, String str10) {
        this.mId = num;
        this.mTimeStamp = date;
        this.mDeviceName = str;
        this.mHardwareVer = str2;
        this.mSoftwareVer = str3;
        this.mIMEI1 = str4;
        this.mIMEI2 = str5;
        this.mIMSI1 = str6;
        this.mIMSI2 = str7;
        this.mSN = str8;
        this.mLanguageRegion = str9;
        this.mReservedInt = num2;
        this.mReservedText = str10;
    }

    public MetaDeviceInfo() {
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

    public String getMDeviceName() {
        return this.mDeviceName;
    }

    public void setMDeviceName(String str) {
        this.mDeviceName = str;
        setValue();
    }

    public String getMHardwareVer() {
        return this.mHardwareVer;
    }

    public void setMHardwareVer(String str) {
        this.mHardwareVer = str;
        setValue();
    }

    public String getMSoftwareVer() {
        return this.mSoftwareVer;
    }

    public void setMSoftwareVer(String str) {
        this.mSoftwareVer = str;
        setValue();
    }

    public String getMIMEI1() {
        return this.mIMEI1;
    }

    public void setMIMEI1(String str) {
        this.mIMEI1 = str;
        setValue();
    }

    public String getMIMEI2() {
        return this.mIMEI2;
    }

    public void setMIMEI2(String str) {
        this.mIMEI2 = str;
        setValue();
    }

    public String getMIMSI1() {
        return this.mIMSI1;
    }

    public void setMIMSI1(String str) {
        this.mIMSI1 = str;
        setValue();
    }

    public String getMIMSI2() {
        return this.mIMSI2;
    }

    public void setMIMSI2(String str) {
        this.mIMSI2 = str;
        setValue();
    }

    public String getMSN() {
        return this.mSN;
    }

    public void setMSN(String str) {
        this.mSN = str;
        setValue();
    }

    public String getMLanguageRegion() {
        return this.mLanguageRegion;
    }

    public void setMLanguageRegion(String str) {
        this.mLanguageRegion = str;
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
        if (this.mDeviceName != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mDeviceName);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mHardwareVer != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mHardwareVer);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mSoftwareVer != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mSoftwareVer);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mIMEI1 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mIMEI1);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mIMEI2 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mIMEI2);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mIMSI1 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mIMSI1);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mIMSI2 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mIMSI2);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mSN != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mSN);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mLanguageRegion != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mLanguageRegion);
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
    public AEntityHelper<MetaDeviceInfo> getHelper() {
        return MetaDeviceInfoHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "MetaDeviceInfo { mId: " + this.mId + ", mTimeStamp: " + this.mTimeStamp + ", mDeviceName: " + this.mDeviceName + ", mHardwareVer: " + this.mHardwareVer + ", mSoftwareVer: " + this.mSoftwareVer + ", mIMEI1: " + this.mIMEI1 + ", mIMEI2: " + this.mIMEI2 + ", mIMSI1: " + this.mIMSI1 + ", mIMSI2: " + this.mIMSI2 + ", mSN: " + this.mSN + ", mLanguageRegion: " + this.mLanguageRegion + ", mReservedInt: " + this.mReservedInt + ", mReservedText: " + this.mReservedText + " }";
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
