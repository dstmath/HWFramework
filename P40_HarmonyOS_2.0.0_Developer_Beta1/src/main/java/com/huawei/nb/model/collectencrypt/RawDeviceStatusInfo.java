package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RawDeviceStatusInfo extends AManagedObject {
    public static final Parcelable.Creator<RawDeviceStatusInfo> CREATOR = new Parcelable.Creator<RawDeviceStatusInfo>() {
        /* class com.huawei.nb.model.collectencrypt.RawDeviceStatusInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public RawDeviceStatusInfo createFromParcel(Parcel parcel) {
            return new RawDeviceStatusInfo(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public RawDeviceStatusInfo[] newArray(int i) {
            return new RawDeviceStatusInfo[i];
        }
    };
    private String mAppInstalled;
    private String mAppUsageTime;
    private String mBatteryInfo;
    private Integer mBluetoohStat;
    private String mCPUInfo;
    private Integer mId;
    private String mMemoryInfo;
    private String mMobileDataSurplus;
    private String mMobileDataTotal;
    private String mNetStat;
    private Integer mReservedInt;
    private String mReservedText;
    private Date mTimeStamp;
    private String mWifiDataTotal;

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
        return "com.huawei.nb.model.collectencrypt.RawDeviceStatusInfo";
    }

    public String getEntityVersion() {
        return "0.0.2";
    }

    public int getEntityVersionCode() {
        return 2;
    }

    public RawDeviceStatusInfo(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Integer num = null;
        this.mId = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.mTimeStamp = cursor.isNull(2) ? null : new Date(cursor.getLong(2));
        this.mMemoryInfo = cursor.getString(3);
        this.mCPUInfo = cursor.getString(4);
        this.mBatteryInfo = cursor.getString(5);
        this.mNetStat = cursor.getString(6);
        this.mBluetoohStat = cursor.isNull(7) ? null : Integer.valueOf(cursor.getInt(7));
        this.mReservedInt = !cursor.isNull(8) ? Integer.valueOf(cursor.getInt(8)) : num;
        this.mReservedText = cursor.getString(9);
        this.mAppInstalled = cursor.getString(10);
        this.mAppUsageTime = cursor.getString(11);
        this.mWifiDataTotal = cursor.getString(12);
        this.mMobileDataTotal = cursor.getString(13);
        this.mMobileDataSurplus = cursor.getString(14);
    }

    public RawDeviceStatusInfo(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.mId = null;
            parcel.readInt();
        } else {
            this.mId = Integer.valueOf(parcel.readInt());
        }
        this.mTimeStamp = parcel.readByte() == 0 ? null : new Date(parcel.readLong());
        this.mMemoryInfo = parcel.readByte() == 0 ? null : parcel.readString();
        this.mCPUInfo = parcel.readByte() == 0 ? null : parcel.readString();
        this.mBatteryInfo = parcel.readByte() == 0 ? null : parcel.readString();
        this.mNetStat = parcel.readByte() == 0 ? null : parcel.readString();
        this.mBluetoohStat = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mReservedInt = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mReservedText = parcel.readByte() == 0 ? null : parcel.readString();
        this.mAppInstalled = parcel.readByte() == 0 ? null : parcel.readString();
        this.mAppUsageTime = parcel.readByte() == 0 ? null : parcel.readString();
        this.mWifiDataTotal = parcel.readByte() == 0 ? null : parcel.readString();
        this.mMobileDataTotal = parcel.readByte() == 0 ? null : parcel.readString();
        this.mMobileDataSurplus = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private RawDeviceStatusInfo(Integer num, Date date, String str, String str2, String str3, String str4, Integer num2, Integer num3, String str5, String str6, String str7, String str8, String str9, String str10) {
        this.mId = num;
        this.mTimeStamp = date;
        this.mMemoryInfo = str;
        this.mCPUInfo = str2;
        this.mBatteryInfo = str3;
        this.mNetStat = str4;
        this.mBluetoohStat = num2;
        this.mReservedInt = num3;
        this.mReservedText = str5;
        this.mAppInstalled = str6;
        this.mAppUsageTime = str7;
        this.mWifiDataTotal = str8;
        this.mMobileDataTotal = str9;
        this.mMobileDataSurplus = str10;
    }

    public RawDeviceStatusInfo() {
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

    public String getMMemoryInfo() {
        return this.mMemoryInfo;
    }

    public void setMMemoryInfo(String str) {
        this.mMemoryInfo = str;
        setValue();
    }

    public String getMCPUInfo() {
        return this.mCPUInfo;
    }

    public void setMCPUInfo(String str) {
        this.mCPUInfo = str;
        setValue();
    }

    public String getMBatteryInfo() {
        return this.mBatteryInfo;
    }

    public void setMBatteryInfo(String str) {
        this.mBatteryInfo = str;
        setValue();
    }

    public String getMNetStat() {
        return this.mNetStat;
    }

    public void setMNetStat(String str) {
        this.mNetStat = str;
        setValue();
    }

    public Integer getMBluetoohStat() {
        return this.mBluetoohStat;
    }

    public void setMBluetoohStat(Integer num) {
        this.mBluetoohStat = num;
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

    public String getMAppInstalled() {
        return this.mAppInstalled;
    }

    public void setMAppInstalled(String str) {
        this.mAppInstalled = str;
        setValue();
    }

    public String getMAppUsageTime() {
        return this.mAppUsageTime;
    }

    public void setMAppUsageTime(String str) {
        this.mAppUsageTime = str;
        setValue();
    }

    public String getMWifiDataTotal() {
        return this.mWifiDataTotal;
    }

    public void setMWifiDataTotal(String str) {
        this.mWifiDataTotal = str;
        setValue();
    }

    public String getMMobileDataTotal() {
        return this.mMobileDataTotal;
    }

    public void setMMobileDataTotal(String str) {
        this.mMobileDataTotal = str;
        setValue();
    }

    public String getMMobileDataSurplus() {
        return this.mMobileDataSurplus;
    }

    public void setMMobileDataSurplus(String str) {
        this.mMobileDataSurplus = str;
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
        if (this.mMemoryInfo != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mMemoryInfo);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mCPUInfo != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mCPUInfo);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mBatteryInfo != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mBatteryInfo);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mNetStat != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mNetStat);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mBluetoohStat != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mBluetoohStat.intValue());
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
        if (this.mAppInstalled != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mAppInstalled);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mAppUsageTime != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mAppUsageTime);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mWifiDataTotal != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mWifiDataTotal);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mMobileDataTotal != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mMobileDataTotal);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mMobileDataSurplus != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mMobileDataSurplus);
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<RawDeviceStatusInfo> getHelper() {
        return RawDeviceStatusInfoHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "RawDeviceStatusInfo { mId: " + this.mId + ", mTimeStamp: " + this.mTimeStamp + ", mMemoryInfo: " + this.mMemoryInfo + ", mCPUInfo: " + this.mCPUInfo + ", mBatteryInfo: " + this.mBatteryInfo + ", mNetStat: " + this.mNetStat + ", mBluetoohStat: " + this.mBluetoohStat + ", mReservedInt: " + this.mReservedInt + ", mReservedText: " + this.mReservedText + ", mAppInstalled: " + this.mAppInstalled + ", mAppUsageTime: " + this.mAppUsageTime + ", mWifiDataTotal: " + this.mWifiDataTotal + ", mMobileDataTotal: " + this.mMobileDataTotal + ", mMobileDataSurplus: " + this.mMobileDataSurplus + " }";
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
