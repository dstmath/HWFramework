package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RawLocationRecord extends AManagedObject {
    public static final Parcelable.Creator<RawLocationRecord> CREATOR = new Parcelable.Creator<RawLocationRecord>() {
        /* class com.huawei.nb.model.collectencrypt.RawLocationRecord.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public RawLocationRecord createFromParcel(Parcel parcel) {
            return new RawLocationRecord(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public RawLocationRecord[] newArray(int i) {
            return new RawLocationRecord[i];
        }
    };
    private String geodeticSystem;
    private Double mAltitude;
    private Integer mCellID;
    private Integer mCellLAC;
    private Integer mCellMCC;
    private Integer mCellMNC;
    private Integer mCellRSSI;
    private String mCity;
    private String mCountry;
    private String mDetailAddress;
    private String mDistrict;
    private Integer mId;
    private Double mLatitude;
    private Character mLocationType;
    private Double mLongitude;
    private String mProvince;
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
        return "com.huawei.nb.model.collectencrypt.RawLocationRecord";
    }

    public String getEntityVersion() {
        return "0.0.3";
    }

    public int getEntityVersionCode() {
        return 3;
    }

    public RawLocationRecord(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Integer num = null;
        this.mId = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.mTimeStamp = cursor.isNull(2) ? null : new Date(cursor.getLong(2));
        this.mLocationType = cursor.isNull(3) ? null : Character.valueOf(cursor.getString(3).charAt(0));
        this.mLongitude = cursor.isNull(4) ? null : Double.valueOf(cursor.getDouble(4));
        this.mLatitude = cursor.isNull(5) ? null : Double.valueOf(cursor.getDouble(5));
        this.mAltitude = cursor.isNull(6) ? null : Double.valueOf(cursor.getDouble(6));
        this.mCity = cursor.getString(7);
        this.mCountry = cursor.getString(8);
        this.mDetailAddress = cursor.getString(9);
        this.mDistrict = cursor.getString(10);
        this.mProvince = cursor.getString(11);
        this.mCellID = cursor.isNull(12) ? null : Integer.valueOf(cursor.getInt(12));
        this.mCellMCC = cursor.isNull(13) ? null : Integer.valueOf(cursor.getInt(13));
        this.mCellMNC = cursor.isNull(14) ? null : Integer.valueOf(cursor.getInt(14));
        this.mCellLAC = cursor.isNull(15) ? null : Integer.valueOf(cursor.getInt(15));
        this.mCellRSSI = cursor.isNull(16) ? null : Integer.valueOf(cursor.getInt(16));
        this.mWifiBSSID = cursor.getString(17);
        this.mWifiLevel = cursor.isNull(18) ? null : Integer.valueOf(cursor.getInt(18));
        this.mReservedInt = !cursor.isNull(19) ? Integer.valueOf(cursor.getInt(19)) : num;
        this.mReservedText = cursor.getString(20);
        this.geodeticSystem = cursor.getString(21);
    }

    public RawLocationRecord(Parcel parcel) {
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
        this.mAltitude = parcel.readByte() == 0 ? null : Double.valueOf(parcel.readDouble());
        this.mCity = parcel.readByte() == 0 ? null : parcel.readString();
        this.mCountry = parcel.readByte() == 0 ? null : parcel.readString();
        this.mDetailAddress = parcel.readByte() == 0 ? null : parcel.readString();
        this.mDistrict = parcel.readByte() == 0 ? null : parcel.readString();
        this.mProvince = parcel.readByte() == 0 ? null : parcel.readString();
        this.mCellID = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mCellMCC = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mCellMNC = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mCellLAC = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mCellRSSI = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mWifiBSSID = parcel.readByte() == 0 ? null : parcel.readString();
        this.mWifiLevel = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mReservedInt = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mReservedText = parcel.readByte() == 0 ? null : parcel.readString();
        this.geodeticSystem = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private RawLocationRecord(Integer num, Date date, Character ch, Double d, Double d2, Double d3, String str, String str2, String str3, String str4, String str5, Integer num2, Integer num3, Integer num4, Integer num5, Integer num6, String str6, Integer num7, Integer num8, String str7, String str8) {
        this.mId = num;
        this.mTimeStamp = date;
        this.mLocationType = ch;
        this.mLongitude = d;
        this.mLatitude = d2;
        this.mAltitude = d3;
        this.mCity = str;
        this.mCountry = str2;
        this.mDetailAddress = str3;
        this.mDistrict = str4;
        this.mProvince = str5;
        this.mCellID = num2;
        this.mCellMCC = num3;
        this.mCellMNC = num4;
        this.mCellLAC = num5;
        this.mCellRSSI = num6;
        this.mWifiBSSID = str6;
        this.mWifiLevel = num7;
        this.mReservedInt = num8;
        this.mReservedText = str7;
        this.geodeticSystem = str8;
    }

    public RawLocationRecord() {
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

    public Double getMAltitude() {
        return this.mAltitude;
    }

    public void setMAltitude(Double d) {
        this.mAltitude = d;
        setValue();
    }

    public String getMCity() {
        return this.mCity;
    }

    public void setMCity(String str) {
        this.mCity = str;
        setValue();
    }

    public String getMCountry() {
        return this.mCountry;
    }

    public void setMCountry(String str) {
        this.mCountry = str;
        setValue();
    }

    public String getMDetailAddress() {
        return this.mDetailAddress;
    }

    public void setMDetailAddress(String str) {
        this.mDetailAddress = str;
        setValue();
    }

    public String getMDistrict() {
        return this.mDistrict;
    }

    public void setMDistrict(String str) {
        this.mDistrict = str;
        setValue();
    }

    public String getMProvince() {
        return this.mProvince;
    }

    public void setMProvince(String str) {
        this.mProvince = str;
        setValue();
    }

    public Integer getMCellID() {
        return this.mCellID;
    }

    public void setMCellID(Integer num) {
        this.mCellID = num;
        setValue();
    }

    public Integer getMCellMCC() {
        return this.mCellMCC;
    }

    public void setMCellMCC(Integer num) {
        this.mCellMCC = num;
        setValue();
    }

    public Integer getMCellMNC() {
        return this.mCellMNC;
    }

    public void setMCellMNC(Integer num) {
        this.mCellMNC = num;
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

    public String getGeodeticSystem() {
        return this.geodeticSystem;
    }

    public void setGeodeticSystem(String str) {
        this.geodeticSystem = str;
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
        if (this.mAltitude != null) {
            parcel.writeByte((byte) 1);
            parcel.writeDouble(this.mAltitude.doubleValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mCity != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mCity);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mCountry != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mCountry);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mDetailAddress != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mDetailAddress);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mDistrict != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mDistrict);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mProvince != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mProvince);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mCellID != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mCellID.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mCellMCC != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mCellMCC.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mCellMNC != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mCellMNC.intValue());
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
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.geodeticSystem != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.geodeticSystem);
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<RawLocationRecord> getHelper() {
        return RawLocationRecordHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "RawLocationRecord { mId: " + this.mId + ", mTimeStamp: " + this.mTimeStamp + ", mLocationType: " + this.mLocationType + ", mLongitude: " + this.mLongitude + ", mLatitude: " + this.mLatitude + ", mAltitude: " + this.mAltitude + ", mCity: " + this.mCity + ", mCountry: " + this.mCountry + ", mDetailAddress: " + this.mDetailAddress + ", mDistrict: " + this.mDistrict + ", mProvince: " + this.mProvince + ", mCellID: " + this.mCellID + ", mCellMCC: " + this.mCellMCC + ", mCellMNC: " + this.mCellMNC + ", mCellLAC: " + this.mCellLAC + ", mCellRSSI: " + this.mCellRSSI + ", mWifiBSSID: " + this.mWifiBSSID + ", mWifiLevel: " + this.mWifiLevel + ", mReservedInt: " + this.mReservedInt + ", mReservedText: " + this.mReservedText + ", geodeticSystem: " + this.geodeticSystem + " }";
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
