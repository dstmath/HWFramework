package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RawSettingProbeInfo extends AManagedObject {
    public static final Parcelable.Creator<RawSettingProbeInfo> CREATOR = new Parcelable.Creator<RawSettingProbeInfo>() {
        /* class com.huawei.nb.model.collectencrypt.RawSettingProbeInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public RawSettingProbeInfo createFromParcel(Parcel parcel) {
            return new RawSettingProbeInfo(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public RawSettingProbeInfo[] newArray(int i) {
            return new RawSettingProbeInfo[i];
        }
    };
    private Integer mId;
    private Integer mOperateType;
    private Integer mReservedInt;
    private String mReservedText;
    private Long mSettingsStart;
    private Integer mSettingsType;
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
        return "com.huawei.nb.model.collectencrypt.RawSettingProbeInfo";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public RawSettingProbeInfo(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Integer num = null;
        this.mId = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.mTimeStamp = cursor.isNull(2) ? null : new Date(cursor.getLong(2));
        this.mSettingsStart = cursor.isNull(3) ? null : Long.valueOf(cursor.getLong(3));
        this.mSettingsType = cursor.isNull(4) ? null : Integer.valueOf(cursor.getInt(4));
        this.mOperateType = cursor.isNull(5) ? null : Integer.valueOf(cursor.getInt(5));
        this.mReservedInt = !cursor.isNull(6) ? Integer.valueOf(cursor.getInt(6)) : num;
        this.mReservedText = cursor.getString(7);
    }

    public RawSettingProbeInfo(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.mId = null;
            parcel.readInt();
        } else {
            this.mId = Integer.valueOf(parcel.readInt());
        }
        this.mTimeStamp = parcel.readByte() == 0 ? null : new Date(parcel.readLong());
        this.mSettingsStart = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.mSettingsType = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mOperateType = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mReservedInt = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mReservedText = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private RawSettingProbeInfo(Integer num, Date date, Long l, Integer num2, Integer num3, Integer num4, String str) {
        this.mId = num;
        this.mTimeStamp = date;
        this.mSettingsStart = l;
        this.mSettingsType = num2;
        this.mOperateType = num3;
        this.mReservedInt = num4;
        this.mReservedText = str;
    }

    public RawSettingProbeInfo() {
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

    public Long getMSettingsStart() {
        return this.mSettingsStart;
    }

    public void setMSettingsStart(Long l) {
        this.mSettingsStart = l;
        setValue();
    }

    public Integer getMSettingsType() {
        return this.mSettingsType;
    }

    public void setMSettingsType(Integer num) {
        this.mSettingsType = num;
        setValue();
    }

    public Integer getMOperateType() {
        return this.mOperateType;
    }

    public void setMOperateType(Integer num) {
        this.mOperateType = num;
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
        if (this.mSettingsStart != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.mSettingsStart.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mSettingsType != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mSettingsType.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mOperateType != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mOperateType.intValue());
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
    public AEntityHelper<RawSettingProbeInfo> getHelper() {
        return RawSettingProbeInfoHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "RawSettingProbeInfo { mId: " + this.mId + ", mTimeStamp: " + this.mTimeStamp + ", mSettingsStart: " + this.mSettingsStart + ", mSettingsType: " + this.mSettingsType + ", mOperateType: " + this.mOperateType + ", mReservedInt: " + this.mReservedInt + ", mReservedText: " + this.mReservedText + " }";
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
