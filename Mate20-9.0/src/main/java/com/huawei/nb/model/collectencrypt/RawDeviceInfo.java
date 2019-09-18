package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RawDeviceInfo extends AManagedObject {
    public static final Parcelable.Creator<RawDeviceInfo> CREATOR = new Parcelable.Creator<RawDeviceInfo>() {
        public RawDeviceInfo createFromParcel(Parcel in) {
            return new RawDeviceInfo(in);
        }

        public RawDeviceInfo[] newArray(int size) {
            return new RawDeviceInfo[size];
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
    private String mPhoneNum;
    private Integer mReservedInt;
    private String mReservedText;
    private String mSN;
    private String mSoftwareVer;
    private Date mTimeStamp;

    public RawDeviceInfo(Cursor cursor) {
        Integer num = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
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
        this.mPhoneNum = cursor.getString(14);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public RawDeviceInfo(Parcel in) {
        super(in);
        String str = null;
        if (in.readByte() == 0) {
            this.mId = null;
            in.readInt();
        } else {
            this.mId = Integer.valueOf(in.readInt());
        }
        this.mTimeStamp = in.readByte() == 0 ? null : new Date(in.readLong());
        this.mDeviceName = in.readByte() == 0 ? null : in.readString();
        this.mHardwareVer = in.readByte() == 0 ? null : in.readString();
        this.mSoftwareVer = in.readByte() == 0 ? null : in.readString();
        this.mIMEI1 = in.readByte() == 0 ? null : in.readString();
        this.mIMEI2 = in.readByte() == 0 ? null : in.readString();
        this.mIMSI1 = in.readByte() == 0 ? null : in.readString();
        this.mIMSI2 = in.readByte() == 0 ? null : in.readString();
        this.mSN = in.readByte() == 0 ? null : in.readString();
        this.mLanguageRegion = in.readByte() == 0 ? null : in.readString();
        this.mReservedInt = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mReservedText = in.readByte() == 0 ? null : in.readString();
        this.mPhoneNum = in.readByte() != 0 ? in.readString() : str;
    }

    private RawDeviceInfo(Integer mId2, Date mTimeStamp2, String mDeviceName2, String mHardwareVer2, String mSoftwareVer2, String mIMEI12, String mIMEI22, String mIMSI12, String mIMSI22, String mSN2, String mLanguageRegion2, Integer mReservedInt2, String mReservedText2, String mPhoneNum2) {
        this.mId = mId2;
        this.mTimeStamp = mTimeStamp2;
        this.mDeviceName = mDeviceName2;
        this.mHardwareVer = mHardwareVer2;
        this.mSoftwareVer = mSoftwareVer2;
        this.mIMEI1 = mIMEI12;
        this.mIMEI2 = mIMEI22;
        this.mIMSI1 = mIMSI12;
        this.mIMSI2 = mIMSI22;
        this.mSN = mSN2;
        this.mLanguageRegion = mLanguageRegion2;
        this.mReservedInt = mReservedInt2;
        this.mReservedText = mReservedText2;
        this.mPhoneNum = mPhoneNum2;
    }

    public RawDeviceInfo() {
    }

    public int describeContents() {
        return 0;
    }

    public Integer getMId() {
        return this.mId;
    }

    public void setMId(Integer mId2) {
        this.mId = mId2;
        setValue();
    }

    public Date getMTimeStamp() {
        return this.mTimeStamp;
    }

    public void setMTimeStamp(Date mTimeStamp2) {
        this.mTimeStamp = mTimeStamp2;
        setValue();
    }

    public String getMDeviceName() {
        return this.mDeviceName;
    }

    public void setMDeviceName(String mDeviceName2) {
        this.mDeviceName = mDeviceName2;
        setValue();
    }

    public String getMHardwareVer() {
        return this.mHardwareVer;
    }

    public void setMHardwareVer(String mHardwareVer2) {
        this.mHardwareVer = mHardwareVer2;
        setValue();
    }

    public String getMSoftwareVer() {
        return this.mSoftwareVer;
    }

    public void setMSoftwareVer(String mSoftwareVer2) {
        this.mSoftwareVer = mSoftwareVer2;
        setValue();
    }

    public String getMIMEI1() {
        return this.mIMEI1;
    }

    public void setMIMEI1(String mIMEI12) {
        this.mIMEI1 = mIMEI12;
        setValue();
    }

    public String getMIMEI2() {
        return this.mIMEI2;
    }

    public void setMIMEI2(String mIMEI22) {
        this.mIMEI2 = mIMEI22;
        setValue();
    }

    public String getMIMSI1() {
        return this.mIMSI1;
    }

    public void setMIMSI1(String mIMSI12) {
        this.mIMSI1 = mIMSI12;
        setValue();
    }

    public String getMIMSI2() {
        return this.mIMSI2;
    }

    public void setMIMSI2(String mIMSI22) {
        this.mIMSI2 = mIMSI22;
        setValue();
    }

    public String getMSN() {
        return this.mSN;
    }

    public void setMSN(String mSN2) {
        this.mSN = mSN2;
        setValue();
    }

    public String getMLanguageRegion() {
        return this.mLanguageRegion;
    }

    public void setMLanguageRegion(String mLanguageRegion2) {
        this.mLanguageRegion = mLanguageRegion2;
        setValue();
    }

    public Integer getMReservedInt() {
        return this.mReservedInt;
    }

    public void setMReservedInt(Integer mReservedInt2) {
        this.mReservedInt = mReservedInt2;
        setValue();
    }

    public String getMReservedText() {
        return this.mReservedText;
    }

    public void setMReservedText(String mReservedText2) {
        this.mReservedText = mReservedText2;
        setValue();
    }

    public String getMPhoneNum() {
        return this.mPhoneNum;
    }

    public void setMPhoneNum(String mPhoneNum2) {
        this.mPhoneNum = mPhoneNum2;
        setValue();
    }

    public void writeToParcel(Parcel out, int ignored) {
        super.writeToParcel(out, ignored);
        if (this.mId != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mId.intValue());
        } else {
            out.writeByte((byte) 0);
            out.writeInt(1);
        }
        if (this.mTimeStamp != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.mTimeStamp.getTime());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mDeviceName != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mDeviceName);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mHardwareVer != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mHardwareVer);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mSoftwareVer != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mSoftwareVer);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mIMEI1 != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mIMEI1);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mIMEI2 != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mIMEI2);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mIMSI1 != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mIMSI1);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mIMSI2 != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mIMSI2);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mSN != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mSN);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mLanguageRegion != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mLanguageRegion);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mReservedInt != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mReservedInt.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mReservedText != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mReservedText);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mPhoneNum != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mPhoneNum);
            return;
        }
        out.writeByte((byte) 0);
    }

    public AEntityHelper<RawDeviceInfo> getHelper() {
        return RawDeviceInfoHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.collectencrypt.RawDeviceInfo";
    }

    public String getDatabaseName() {
        return "dsCollectEncrypt";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("RawDeviceInfo { mId: ").append(this.mId);
        sb.append(", mTimeStamp: ").append(this.mTimeStamp);
        sb.append(", mDeviceName: ").append(this.mDeviceName);
        sb.append(", mHardwareVer: ").append(this.mHardwareVer);
        sb.append(", mSoftwareVer: ").append(this.mSoftwareVer);
        sb.append(", mIMEI1: ").append(this.mIMEI1);
        sb.append(", mIMEI2: ").append(this.mIMEI2);
        sb.append(", mIMSI1: ").append(this.mIMSI1);
        sb.append(", mIMSI2: ").append(this.mIMSI2);
        sb.append(", mSN: ").append(this.mSN);
        sb.append(", mLanguageRegion: ").append(this.mLanguageRegion);
        sb.append(", mReservedInt: ").append(this.mReservedInt);
        sb.append(", mReservedText: ").append(this.mReservedText);
        sb.append(", mPhoneNum: ").append(this.mPhoneNum);
        sb.append(" }");
        return sb.toString();
    }

    public boolean equals(Object o) {
        return super.equals(o);
    }

    public int hashCode() {
        return super.hashCode();
    }

    public String getDatabaseVersion() {
        return "0.0.14";
    }

    public int getDatabaseVersionCode() {
        return 14;
    }

    public String getEntityVersion() {
        return "0.0.2";
    }

    public int getEntityVersionCode() {
        return 2;
    }
}
