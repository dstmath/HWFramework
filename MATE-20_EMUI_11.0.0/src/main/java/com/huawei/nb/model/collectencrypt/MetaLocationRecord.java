package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class MetaLocationRecord extends AManagedObject {
    public static final Parcelable.Creator<MetaLocationRecord> CREATOR = new Parcelable.Creator<MetaLocationRecord>() {
        /* class com.huawei.nb.model.collectencrypt.MetaLocationRecord.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public MetaLocationRecord createFromParcel(Parcel parcel) {
            return new MetaLocationRecord(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public MetaLocationRecord[] newArray(int i) {
            return new MetaLocationRecord[i];
        }
    };
    private Integer mCellID;
    private Integer mCellLAC;
    private Integer mCellRSSI;
    private Integer mId;
    private Double mLatitude;
    private Character mLocationType;
    private Double mLongitude;
    private Integer mReservedInt;
    private String mReservedText;
    private Date mTimeStamp;
    private String mWifiBSSID;
    private Integer mWifiLevel;

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
        return "com.huawei.nb.model.collectencrypt.MetaLocationRecord";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public MetaLocationRecord(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Integer num = null;
        this.mId = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.mTimeStamp = cursor.isNull(2) ? null : new Date(cursor.getLong(2));
        this.mLocationType = cursor.isNull(3) ? null : Character.valueOf(cursor.getString(3).charAt(0));
        this.mLongitude = cursor.isNull(4) ? null : Double.valueOf(cursor.getDouble(4));
        this.mLatitude = cursor.isNull(5) ? null : Double.valueOf(cursor.getDouble(5));
        this.mCellID = cursor.isNull(6) ? null : Integer.valueOf(cursor.getInt(6));
        this.mCellLAC = cursor.isNull(7) ? null : Integer.valueOf(cursor.getInt(7));
        this.mCellRSSI = cursor.isNull(8) ? null : Integer.valueOf(cursor.getInt(8));
        this.mWifiBSSID = cursor.getString(9);
        this.mWifiLevel = cursor.isNull(10) ? null : Integer.valueOf(cursor.getInt(10));
        this.mReservedInt = !cursor.isNull(11) ? Integer.valueOf(cursor.getInt(11)) : num;
        this.mReservedText = cursor.getString(12);
    }

    public MetaLocationRecord(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.mId = null;
            parcel.readInt();
        } else {
            this.mId = Integer.valueOf(parcel.readInt());
        }
        this.mTimeStamp = parcel.readByte() == 0 ? null : new Date(parcel.readLong());
        this.mLocationType = parcel.readByte() == 0 ? null : Character.valueOf(parcel.createCharArray()[0]);
        this.mLongitude = parcel.readByte() == 0 ? null : Double.valueOf(parcel.readDouble());
        this.mLatitude = parcel.readByte() == 0 ? null : Double.valueOf(parcel.readDouble());
        this.mCellID = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mCellLAC = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mCellRSSI = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mWifiBSSID = parcel.readByte() == 0 ? null : parcel.readString();
        this.mWifiLevel = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mReservedInt = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mReservedText = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private MetaLocationRecord(Integer num, Date date, Character ch, Double d, Double d2, Integer num2, Integer num3, Integer num4, String str, Integer num5, Integer num6, String str2) {
        this.mId = num;
        this.mTimeStamp = date;
        this.mLocationType = ch;
        this.mLongitude = d;
        this.mLatitude = d2;
        this.mCellID = num2;
        this.mCellLAC = num3;
        this.mCellRSSI = num4;
        this.mWifiBSSID = str;
        this.mWifiLevel = num5;
        this.mReservedInt = num6;
        this.mReservedText = str2;
    }

    public MetaLocationRecord() {
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

    public Character getMLocationType() {
        return this.mLocationType;
    }

    public void setMLocationType(Character ch) {
        this.mLocationType = ch;
        setValue();
    }

    public Double getMLongitude() {
        return this.mLongitude;
    }

    public void setMLongitude(Double d) {
        this.mLongitude = d;
        setValue();
    }

    public Double getMLatitude() {
        return this.mLatitude;
    }

    public void setMLatitude(Double d) {
        this.mLatitude = d;
        setValue();
    }

    public Integer getMCellID() {
        return this.mCellID;
    }

    public void setMCellID(Integer num) {
        this.mCellID = num;
        setValue();
    }

    public Integer getMCellLAC() {
        return this.mCellLAC;
    }

    public void setMCellLAC(Integer num) {
        this.mCellLAC = num;
        setValue();
    }

    public Integer getMCellRSSI() {
        return this.mCellRSSI;
    }

    public void setMCellRSSI(Integer num) {
        this.mCellRSSI = num;
        setValue();
    }

    public String getMWifiBSSID() {
        return this.mWifiBSSID;
    }

    public void setMWifiBSSID(String str) {
        this.mWifiBSSID = str;
        setValue();
    }

    public Integer getMWifiLevel() {
        return this.mWifiLevel;
    }

    public void setMWifiLevel(Integer num) {
        this.mWifiLevel = num;
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
        if (this.mLocationType != null) {
            parcel.writeByte((byte) 1);
            parcel.writeCharArray(new char[]{this.mLocationType.charValue()});
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mLongitude != null) {
            parcel.writeByte((byte) 1);
            parcel.writeDouble(this.mLongitude.doubleValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mLatitude != null) {
            parcel.writeByte((byte) 1);
            parcel.writeDouble(this.mLatitude.doubleValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mCellID != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mCellID.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mCellLAC != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mCellLAC.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mCellRSSI != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mCellRSSI.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mWifiBSSID != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mWifiBSSID);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mWifiLevel != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mWifiLevel.intValue());
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
    public AEntityHelper<MetaLocationRecord> getHelper() {
        return MetaLocationRecordHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "MetaLocationRecord { mId: " + this.mId + ", mTimeStamp: " + this.mTimeStamp + ", mLocationType: " + this.mLocationType + ", mLongitude: " + this.mLongitude + ", mLatitude: " + this.mLatitude + ", mCellID: " + this.mCellID + ", mCellLAC: " + this.mCellLAC + ", mCellRSSI: " + this.mCellRSSI + ", mWifiBSSID: " + this.mWifiBSSID + ", mWifiLevel: " + this.mWifiLevel + ", mReservedInt: " + this.mReservedInt + ", mReservedText: " + this.mReservedText + " }";
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
