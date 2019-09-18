package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class MetaLocationRecord extends AManagedObject {
    public static final Parcelable.Creator<MetaLocationRecord> CREATOR = new Parcelable.Creator<MetaLocationRecord>() {
        public MetaLocationRecord createFromParcel(Parcel in) {
            return new MetaLocationRecord(in);
        }

        public MetaLocationRecord[] newArray(int size) {
            return new MetaLocationRecord[size];
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

    public MetaLocationRecord(Cursor cursor) {
        Integer num = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
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

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public MetaLocationRecord(Parcel in) {
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
        this.mCellID = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mCellLAC = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mCellRSSI = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mWifiBSSID = in.readByte() == 0 ? null : in.readString();
        this.mWifiLevel = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mReservedInt = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mReservedText = in.readByte() != 0 ? in.readString() : str;
    }

    private MetaLocationRecord(Integer mId2, Date mTimeStamp2, Character mLocationType2, Double mLongitude2, Double mLatitude2, Integer mCellID2, Integer mCellLAC2, Integer mCellRSSI2, String mWifiBSSID2, Integer mWifiLevel2, Integer mReservedInt2, String mReservedText2) {
        this.mId = mId2;
        this.mTimeStamp = mTimeStamp2;
        this.mLocationType = mLocationType2;
        this.mLongitude = mLongitude2;
        this.mLatitude = mLatitude2;
        this.mCellID = mCellID2;
        this.mCellLAC = mCellLAC2;
        this.mCellRSSI = mCellRSSI2;
        this.mWifiBSSID = mWifiBSSID2;
        this.mWifiLevel = mWifiLevel2;
        this.mReservedInt = mReservedInt2;
        this.mReservedText = mReservedText2;
    }

    public MetaLocationRecord() {
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

    public Integer getMCellID() {
        return this.mCellID;
    }

    public void setMCellID(Integer mCellID2) {
        this.mCellID = mCellID2;
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
        if (this.mCellID != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mCellID.intValue());
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
            return;
        }
        out.writeByte((byte) 0);
    }

    public AEntityHelper<MetaLocationRecord> getHelper() {
        return MetaLocationRecordHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.collectencrypt.MetaLocationRecord";
    }

    public String getDatabaseName() {
        return "dsCollectEncrypt";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("MetaLocationRecord { mId: ").append(this.mId);
        sb.append(", mTimeStamp: ").append(this.mTimeStamp);
        sb.append(", mLocationType: ").append(this.mLocationType);
        sb.append(", mLongitude: ").append(this.mLongitude);
        sb.append(", mLatitude: ").append(this.mLatitude);
        sb.append(", mCellID: ").append(this.mCellID);
        sb.append(", mCellLAC: ").append(this.mCellLAC);
        sb.append(", mCellRSSI: ").append(this.mCellRSSI);
        sb.append(", mWifiBSSID: ").append(this.mWifiBSSID);
        sb.append(", mWifiLevel: ").append(this.mWifiLevel);
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
