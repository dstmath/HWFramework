package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class MetaDeviceStatusInfo extends AManagedObject {
    public static final Parcelable.Creator<MetaDeviceStatusInfo> CREATOR = new Parcelable.Creator<MetaDeviceStatusInfo>() {
        public MetaDeviceStatusInfo createFromParcel(Parcel in) {
            return new MetaDeviceStatusInfo(in);
        }

        public MetaDeviceStatusInfo[] newArray(int size) {
            return new MetaDeviceStatusInfo[size];
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

    public MetaDeviceStatusInfo(Cursor cursor) {
        Integer num = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
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

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public MetaDeviceStatusInfo(Parcel in) {
        super(in);
        String str = null;
        if (in.readByte() == 0) {
            this.mId = null;
            in.readInt();
        } else {
            this.mId = Integer.valueOf(in.readInt());
        }
        this.mTimeStamp = in.readByte() == 0 ? null : new Date(in.readLong());
        this.mMemoryInfo = in.readByte() == 0 ? null : in.readString();
        this.mCPUInfo = in.readByte() == 0 ? null : in.readString();
        this.mBatteryInfo = in.readByte() == 0 ? null : in.readString();
        this.mNetStat = in.readByte() == 0 ? null : in.readString();
        this.mBluetoohStat = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mReservedInt = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mReservedText = in.readByte() != 0 ? in.readString() : str;
    }

    private MetaDeviceStatusInfo(Integer mId2, Date mTimeStamp2, String mMemoryInfo2, String mCPUInfo2, String mBatteryInfo2, String mNetStat2, Integer mBluetoohStat2, Integer mReservedInt2, String mReservedText2) {
        this.mId = mId2;
        this.mTimeStamp = mTimeStamp2;
        this.mMemoryInfo = mMemoryInfo2;
        this.mCPUInfo = mCPUInfo2;
        this.mBatteryInfo = mBatteryInfo2;
        this.mNetStat = mNetStat2;
        this.mBluetoohStat = mBluetoohStat2;
        this.mReservedInt = mReservedInt2;
        this.mReservedText = mReservedText2;
    }

    public MetaDeviceStatusInfo() {
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

    public String getMMemoryInfo() {
        return this.mMemoryInfo;
    }

    public void setMMemoryInfo(String mMemoryInfo2) {
        this.mMemoryInfo = mMemoryInfo2;
        setValue();
    }

    public String getMCPUInfo() {
        return this.mCPUInfo;
    }

    public void setMCPUInfo(String mCPUInfo2) {
        this.mCPUInfo = mCPUInfo2;
        setValue();
    }

    public String getMBatteryInfo() {
        return this.mBatteryInfo;
    }

    public void setMBatteryInfo(String mBatteryInfo2) {
        this.mBatteryInfo = mBatteryInfo2;
        setValue();
    }

    public String getMNetStat() {
        return this.mNetStat;
    }

    public void setMNetStat(String mNetStat2) {
        this.mNetStat = mNetStat2;
        setValue();
    }

    public Integer getMBluetoohStat() {
        return this.mBluetoohStat;
    }

    public void setMBluetoohStat(Integer mBluetoohStat2) {
        this.mBluetoohStat = mBluetoohStat2;
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
        if (this.mMemoryInfo != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mMemoryInfo);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mCPUInfo != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mCPUInfo);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mBatteryInfo != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mBatteryInfo);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mNetStat != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mNetStat);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mBluetoohStat != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mBluetoohStat.intValue());
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
            return;
        }
        out.writeByte((byte) 0);
    }

    public AEntityHelper<MetaDeviceStatusInfo> getHelper() {
        return MetaDeviceStatusInfoHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.collectencrypt.MetaDeviceStatusInfo";
    }

    public String getDatabaseName() {
        return "dsCollectEncrypt";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("MetaDeviceStatusInfo { mId: ").append(this.mId);
        sb.append(", mTimeStamp: ").append(this.mTimeStamp);
        sb.append(", mMemoryInfo: ").append(this.mMemoryInfo);
        sb.append(", mCPUInfo: ").append(this.mCPUInfo);
        sb.append(", mBatteryInfo: ").append(this.mBatteryInfo);
        sb.append(", mNetStat: ").append(this.mNetStat);
        sb.append(", mBluetoohStat: ").append(this.mBluetoohStat);
        sb.append(", mReservedInt: ").append(this.mReservedInt);
        sb.append(", mReservedText: ").append(this.mReservedText);
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
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }
}
