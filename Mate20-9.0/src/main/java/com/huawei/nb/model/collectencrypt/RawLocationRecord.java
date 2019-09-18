package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RawLocationRecord extends AManagedObject {
    public static final Parcelable.Creator<RawLocationRecord> CREATOR = new Parcelable.Creator<RawLocationRecord>() {
        public RawLocationRecord createFromParcel(Parcel in) {
            return new RawLocationRecord(in);
        }

        public RawLocationRecord[] newArray(int size) {
            return new RawLocationRecord[size];
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

    public RawLocationRecord(Cursor cursor) {
        Integer num = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
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

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public RawLocationRecord(Parcel in) {
        super(in);
        String str = null;
        if (in.readByte() == 0) {
            this.mId = null;
            in.readInt();
        } else {
            this.mId = Integer.valueOf(in.readInt());
        }
        this.mTimeStamp = in.readByte() == 0 ? null : new Date(in.readLong());
        this.mLocationType = in.readByte() == 0 ? null : Character.valueOf(in.createCharArray()[0]);
        this.mLongitude = in.readByte() == 0 ? null : Double.valueOf(in.readDouble());
        this.mLatitude = in.readByte() == 0 ? null : Double.valueOf(in.readDouble());
        this.mAltitude = in.readByte() == 0 ? null : Double.valueOf(in.readDouble());
        this.mCity = in.readByte() == 0 ? null : in.readString();
        this.mCountry = in.readByte() == 0 ? null : in.readString();
        this.mDetailAddress = in.readByte() == 0 ? null : in.readString();
        this.mDistrict = in.readByte() == 0 ? null : in.readString();
        this.mProvince = in.readByte() == 0 ? null : in.readString();
        this.mCellID = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mCellMCC = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mCellMNC = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mCellLAC = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mCellRSSI = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mWifiBSSID = in.readByte() == 0 ? null : in.readString();
        this.mWifiLevel = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mReservedInt = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mReservedText = in.readByte() == 0 ? null : in.readString();
        this.geodeticSystem = in.readByte() != 0 ? in.readString() : str;
    }

    private RawLocationRecord(Integer mId2, Date mTimeStamp2, Character mLocationType2, Double mLongitude2, Double mLatitude2, Double mAltitude2, String mCity2, String mCountry2, String mDetailAddress2, String mDistrict2, String mProvince2, Integer mCellID2, Integer mCellMCC2, Integer mCellMNC2, Integer mCellLAC2, Integer mCellRSSI2, String mWifiBSSID2, Integer mWifiLevel2, Integer mReservedInt2, String mReservedText2, String geodeticSystem2) {
        this.mId = mId2;
        this.mTimeStamp = mTimeStamp2;
        this.mLocationType = mLocationType2;
        this.mLongitude = mLongitude2;
        this.mLatitude = mLatitude2;
        this.mAltitude = mAltitude2;
        this.mCity = mCity2;
        this.mCountry = mCountry2;
        this.mDetailAddress = mDetailAddress2;
        this.mDistrict = mDistrict2;
        this.mProvince = mProvince2;
        this.mCellID = mCellID2;
        this.mCellMCC = mCellMCC2;
        this.mCellMNC = mCellMNC2;
        this.mCellLAC = mCellLAC2;
        this.mCellRSSI = mCellRSSI2;
        this.mWifiBSSID = mWifiBSSID2;
        this.mWifiLevel = mWifiLevel2;
        this.mReservedInt = mReservedInt2;
        this.mReservedText = mReservedText2;
        this.geodeticSystem = geodeticSystem2;
    }

    public RawLocationRecord() {
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

    public Character getMLocationType() {
        return this.mLocationType;
    }

    public void setMLocationType(Character mLocationType2) {
        this.mLocationType = mLocationType2;
        setValue();
    }

    public Double getMLongitude() {
        return this.mLongitude;
    }

    public void setMLongitude(Double mLongitude2) {
        this.mLongitude = mLongitude2;
        setValue();
    }

    public Double getMLatitude() {
        return this.mLatitude;
    }

    public void setMLatitude(Double mLatitude2) {
        this.mLatitude = mLatitude2;
        setValue();
    }

    public Double getMAltitude() {
        return this.mAltitude;
    }

    public void setMAltitude(Double mAltitude2) {
        this.mAltitude = mAltitude2;
        setValue();
    }

    public String getMCity() {
        return this.mCity;
    }

    public void setMCity(String mCity2) {
        this.mCity = mCity2;
        setValue();
    }

    public String getMCountry() {
        return this.mCountry;
    }

    public void setMCountry(String mCountry2) {
        this.mCountry = mCountry2;
        setValue();
    }

    public String getMDetailAddress() {
        return this.mDetailAddress;
    }

    public void setMDetailAddress(String mDetailAddress2) {
        this.mDetailAddress = mDetailAddress2;
        setValue();
    }

    public String getMDistrict() {
        return this.mDistrict;
    }

    public void setMDistrict(String mDistrict2) {
        this.mDistrict = mDistrict2;
        setValue();
    }

    public String getMProvince() {
        return this.mProvince;
    }

    public void setMProvince(String mProvince2) {
        this.mProvince = mProvince2;
        setValue();
    }

    public Integer getMCellID() {
        return this.mCellID;
    }

    public void setMCellID(Integer mCellID2) {
        this.mCellID = mCellID2;
        setValue();
    }

    public Integer getMCellMCC() {
        return this.mCellMCC;
    }

    public void setMCellMCC(Integer mCellMCC2) {
        this.mCellMCC = mCellMCC2;
        setValue();
    }

    public Integer getMCellMNC() {
        return this.mCellMNC;
    }

    public void setMCellMNC(Integer mCellMNC2) {
        this.mCellMNC = mCellMNC2;
        setValue();
    }

    public Integer getMCellLAC() {
        return this.mCellLAC;
    }

    public void setMCellLAC(Integer mCellLAC2) {
        this.mCellLAC = mCellLAC2;
        setValue();
    }

    public Integer getMCellRSSI() {
        return this.mCellRSSI;
    }

    public void setMCellRSSI(Integer mCellRSSI2) {
        this.mCellRSSI = mCellRSSI2;
        setValue();
    }

    public String getMWifiBSSID() {
        return this.mWifiBSSID;
    }

    public void setMWifiBSSID(String mWifiBSSID2) {
        this.mWifiBSSID = mWifiBSSID2;
        setValue();
    }

    public Integer getMWifiLevel() {
        return this.mWifiLevel;
    }

    public void setMWifiLevel(Integer mWifiLevel2) {
        this.mWifiLevel = mWifiLevel2;
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

    public String getGeodeticSystem() {
        return this.geodeticSystem;
    }

    public void setGeodeticSystem(String geodeticSystem2) {
        this.geodeticSystem = geodeticSystem2;
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
        if (this.mLocationType != null) {
            out.writeByte((byte) 1);
            out.writeCharArray(new char[]{this.mLocationType.charValue()});
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mLongitude != null) {
            out.writeByte((byte) 1);
            out.writeDouble(this.mLongitude.doubleValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mLatitude != null) {
            out.writeByte((byte) 1);
            out.writeDouble(this.mLatitude.doubleValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mAltitude != null) {
            out.writeByte((byte) 1);
            out.writeDouble(this.mAltitude.doubleValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mCity != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mCity);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mCountry != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mCountry);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mDetailAddress != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mDetailAddress);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mDistrict != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mDistrict);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mProvince != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mProvince);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mCellID != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mCellID.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mCellMCC != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mCellMCC.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mCellMNC != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mCellMNC.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mCellLAC != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mCellLAC.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mCellRSSI != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mCellRSSI.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mWifiBSSID != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mWifiBSSID);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mWifiLevel != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mWifiLevel.intValue());
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
        if (this.geodeticSystem != null) {
            out.writeByte((byte) 1);
            out.writeString(this.geodeticSystem);
            return;
        }
        out.writeByte((byte) 0);
    }

    public AEntityHelper<RawLocationRecord> getHelper() {
        return RawLocationRecordHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.collectencrypt.RawLocationRecord";
    }

    public String getDatabaseName() {
        return "dsCollectEncrypt";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("RawLocationRecord { mId: ").append(this.mId);
        sb.append(", mTimeStamp: ").append(this.mTimeStamp);
        sb.append(", mLocationType: ").append(this.mLocationType);
        sb.append(", mLongitude: ").append(this.mLongitude);
        sb.append(", mLatitude: ").append(this.mLatitude);
        sb.append(", mAltitude: ").append(this.mAltitude);
        sb.append(", mCity: ").append(this.mCity);
        sb.append(", mCountry: ").append(this.mCountry);
        sb.append(", mDetailAddress: ").append(this.mDetailAddress);
        sb.append(", mDistrict: ").append(this.mDistrict);
        sb.append(", mProvince: ").append(this.mProvince);
        sb.append(", mCellID: ").append(this.mCellID);
        sb.append(", mCellMCC: ").append(this.mCellMCC);
        sb.append(", mCellMNC: ").append(this.mCellMNC);
        sb.append(", mCellLAC: ").append(this.mCellLAC);
        sb.append(", mCellRSSI: ").append(this.mCellRSSI);
        sb.append(", mWifiBSSID: ").append(this.mWifiBSSID);
        sb.append(", mWifiLevel: ").append(this.mWifiLevel);
        sb.append(", mReservedInt: ").append(this.mReservedInt);
        sb.append(", mReservedText: ").append(this.mReservedText);
        sb.append(", geodeticSystem: ").append(this.geodeticSystem);
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
        return "0.0.3";
    }

    public int getEntityVersionCode() {
        return 3;
    }
}
