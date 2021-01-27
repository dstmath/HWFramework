package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class MetaDeviceStatusInfo extends AManagedObject {
    public static final Parcelable.Creator<MetaDeviceStatusInfo> CREATOR = new Parcelable.Creator<MetaDeviceStatusInfo>() {
        /* class com.huawei.nb.model.collectencrypt.MetaDeviceStatusInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public MetaDeviceStatusInfo createFromParcel(Parcel parcel) {
            return new MetaDeviceStatusInfo(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public MetaDeviceStatusInfo[] newArray(int i) {
            return new MetaDeviceStatusInfo[i];
        }
    };
    private String mBatteryInfo;
    private Integer mBluetoohStat;
    private String mCPUInfo;
    private Integer mId;
    private String mMemoryInfo;
    private String mNetStat;
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
        return "com.huawei.nb.model.collectencrypt.MetaDeviceStatusInfo";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public MetaDeviceStatusInfo(Cursor cursor) {
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
    }

    public MetaDeviceStatusInfo(Parcel parcel) {
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
        this.mReservedText = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private MetaDeviceStatusInfo(Integer num, Date date, String str, String str2, String str3, String str4, Integer num2, Integer num3, String str5) {
        this.mId = num;
        this.mTimeStamp = date;
        this.mMemoryInfo = str;
        this.mCPUInfo = str2;
        this.mBatteryInfo = str3;
        this.mNetStat = str4;
        this.mBluetoohStat = num2;
        this.mReservedInt = num3;
        this.mReservedText = str5;
    }

    public MetaDeviceStatusInfo() {
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
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<MetaDeviceStatusInfo> getHelper() {
        return MetaDeviceStatusInfoHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "MetaDeviceStatusInfo { mId: " + this.mId + ", mTimeStamp: " + this.mTimeStamp + ", mMemoryInfo: " + this.mMemoryInfo + ", mCPUInfo: " + this.mCPUInfo + ", mBatteryInfo: " + this.mBatteryInfo + ", mNetStat: " + this.mNetStat + ", mBluetoohStat: " + this.mBluetoohStat + ", mReservedInt: " + this.mReservedInt + ", mReservedText: " + this.mReservedText + " }";
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
