package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RawSportHealth extends AManagedObject {
    public static final Parcelable.Creator<RawSportHealth> CREATOR = new Parcelable.Creator<RawSportHealth>() {
        public RawSportHealth createFromParcel(Parcel in) {
            return new RawSportHealth(in);
        }

        public RawSportHealth[] newArray(int size) {
            return new RawSportHealth[size];
        }
    };
    private Double mBloodPressureHigh;
    private Double mBloodPressureLow;
    private Double mBloodSugar;
    private Double mHeartRat;
    private Double mHeight;
    private Integer mId;
    private Integer mReservedInt;
    private String mReservedText;
    private Double mSleep;
    private String mSportAR;
    private Double mSportDistance;
    private Double mSportHeat;
    private Double mSportHeight;
    private Double mSportPaces;
    private Date mTimeStamp;
    private Double mWeight;

    public RawSportHealth(Cursor cursor) {
        Integer num = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.mId = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.mTimeStamp = cursor.isNull(2) ? null : new Date(cursor.getLong(2));
        this.mHeight = cursor.isNull(3) ? null : Double.valueOf(cursor.getDouble(3));
        this.mWeight = cursor.isNull(4) ? null : Double.valueOf(cursor.getDouble(4));
        this.mHeartRat = cursor.isNull(5) ? null : Double.valueOf(cursor.getDouble(5));
        this.mBloodPressureLow = cursor.isNull(6) ? null : Double.valueOf(cursor.getDouble(6));
        this.mBloodPressureHigh = cursor.isNull(7) ? null : Double.valueOf(cursor.getDouble(7));
        this.mBloodSugar = cursor.isNull(8) ? null : Double.valueOf(cursor.getDouble(8));
        this.mSportDistance = cursor.isNull(9) ? null : Double.valueOf(cursor.getDouble(9));
        this.mSportHeight = cursor.isNull(10) ? null : Double.valueOf(cursor.getDouble(10));
        this.mSportHeat = cursor.isNull(11) ? null : Double.valueOf(cursor.getDouble(11));
        this.mSportPaces = cursor.isNull(12) ? null : Double.valueOf(cursor.getDouble(12));
        this.mSleep = cursor.isNull(13) ? null : Double.valueOf(cursor.getDouble(13));
        this.mSportAR = cursor.getString(14);
        this.mReservedInt = !cursor.isNull(15) ? Integer.valueOf(cursor.getInt(15)) : num;
        this.mReservedText = cursor.getString(16);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public RawSportHealth(Parcel in) {
        super(in);
        String str = null;
        if (in.readByte() == 0) {
            this.mId = null;
            in.readInt();
        } else {
            this.mId = Integer.valueOf(in.readInt());
        }
        this.mTimeStamp = in.readByte() == 0 ? null : new Date(in.readLong());
        this.mHeight = in.readByte() == 0 ? null : Double.valueOf(in.readDouble());
        this.mWeight = in.readByte() == 0 ? null : Double.valueOf(in.readDouble());
        this.mHeartRat = in.readByte() == 0 ? null : Double.valueOf(in.readDouble());
        this.mBloodPressureLow = in.readByte() == 0 ? null : Double.valueOf(in.readDouble());
        this.mBloodPressureHigh = in.readByte() == 0 ? null : Double.valueOf(in.readDouble());
        this.mBloodSugar = in.readByte() == 0 ? null : Double.valueOf(in.readDouble());
        this.mSportDistance = in.readByte() == 0 ? null : Double.valueOf(in.readDouble());
        this.mSportHeight = in.readByte() == 0 ? null : Double.valueOf(in.readDouble());
        this.mSportHeat = in.readByte() == 0 ? null : Double.valueOf(in.readDouble());
        this.mSportPaces = in.readByte() == 0 ? null : Double.valueOf(in.readDouble());
        this.mSleep = in.readByte() == 0 ? null : Double.valueOf(in.readDouble());
        this.mSportAR = in.readByte() == 0 ? null : in.readString();
        this.mReservedInt = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mReservedText = in.readByte() != 0 ? in.readString() : str;
    }

    private RawSportHealth(Integer mId2, Date mTimeStamp2, Double mHeight2, Double mWeight2, Double mHeartRat2, Double mBloodPressureLow2, Double mBloodPressureHigh2, Double mBloodSugar2, Double mSportDistance2, Double mSportHeight2, Double mSportHeat2, Double mSportPaces2, Double mSleep2, String mSportAR2, Integer mReservedInt2, String mReservedText2) {
        this.mId = mId2;
        this.mTimeStamp = mTimeStamp2;
        this.mHeight = mHeight2;
        this.mWeight = mWeight2;
        this.mHeartRat = mHeartRat2;
        this.mBloodPressureLow = mBloodPressureLow2;
        this.mBloodPressureHigh = mBloodPressureHigh2;
        this.mBloodSugar = mBloodSugar2;
        this.mSportDistance = mSportDistance2;
        this.mSportHeight = mSportHeight2;
        this.mSportHeat = mSportHeat2;
        this.mSportPaces = mSportPaces2;
        this.mSleep = mSleep2;
        this.mSportAR = mSportAR2;
        this.mReservedInt = mReservedInt2;
        this.mReservedText = mReservedText2;
    }

    public RawSportHealth() {
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

    public Double getMHeight() {
        return this.mHeight;
    }

    public void setMHeight(Double mHeight2) {
        this.mHeight = mHeight2;
        setValue();
    }

    public Double getMWeight() {
        return this.mWeight;
    }

    public void setMWeight(Double mWeight2) {
        this.mWeight = mWeight2;
        setValue();
    }

    public Double getMHeartRat() {
        return this.mHeartRat;
    }

    public void setMHeartRat(Double mHeartRat2) {
        this.mHeartRat = mHeartRat2;
        setValue();
    }

    public Double getMBloodPressureLow() {
        return this.mBloodPressureLow;
    }

    public void setMBloodPressureLow(Double mBloodPressureLow2) {
        this.mBloodPressureLow = mBloodPressureLow2;
        setValue();
    }

    public Double getMBloodPressureHigh() {
        return this.mBloodPressureHigh;
    }

    public void setMBloodPressureHigh(Double mBloodPressureHigh2) {
        this.mBloodPressureHigh = mBloodPressureHigh2;
        setValue();
    }

    public Double getMBloodSugar() {
        return this.mBloodSugar;
    }

    public void setMBloodSugar(Double mBloodSugar2) {
        this.mBloodSugar = mBloodSugar2;
        setValue();
    }

    public Double getMSportDistance() {
        return this.mSportDistance;
    }

    public void setMSportDistance(Double mSportDistance2) {
        this.mSportDistance = mSportDistance2;
        setValue();
    }

    public Double getMSportHeight() {
        return this.mSportHeight;
    }

    public void setMSportHeight(Double mSportHeight2) {
        this.mSportHeight = mSportHeight2;
        setValue();
    }

    public Double getMSportHeat() {
        return this.mSportHeat;
    }

    public void setMSportHeat(Double mSportHeat2) {
        this.mSportHeat = mSportHeat2;
        setValue();
    }

    public Double getMSportPaces() {
        return this.mSportPaces;
    }

    public void setMSportPaces(Double mSportPaces2) {
        this.mSportPaces = mSportPaces2;
        setValue();
    }

    public Double getMSleep() {
        return this.mSleep;
    }

    public void setMSleep(Double mSleep2) {
        this.mSleep = mSleep2;
        setValue();
    }

    public String getMSportAR() {
        return this.mSportAR;
    }

    public void setMSportAR(String mSportAR2) {
        this.mSportAR = mSportAR2;
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
        if (this.mHeight != null) {
            out.writeByte((byte) 1);
            out.writeDouble(this.mHeight.doubleValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mWeight != null) {
            out.writeByte((byte) 1);
            out.writeDouble(this.mWeight.doubleValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mHeartRat != null) {
            out.writeByte((byte) 1);
            out.writeDouble(this.mHeartRat.doubleValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mBloodPressureLow != null) {
            out.writeByte((byte) 1);
            out.writeDouble(this.mBloodPressureLow.doubleValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mBloodPressureHigh != null) {
            out.writeByte((byte) 1);
            out.writeDouble(this.mBloodPressureHigh.doubleValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mBloodSugar != null) {
            out.writeByte((byte) 1);
            out.writeDouble(this.mBloodSugar.doubleValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mSportDistance != null) {
            out.writeByte((byte) 1);
            out.writeDouble(this.mSportDistance.doubleValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mSportHeight != null) {
            out.writeByte((byte) 1);
            out.writeDouble(this.mSportHeight.doubleValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mSportHeat != null) {
            out.writeByte((byte) 1);
            out.writeDouble(this.mSportHeat.doubleValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mSportPaces != null) {
            out.writeByte((byte) 1);
            out.writeDouble(this.mSportPaces.doubleValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mSleep != null) {
            out.writeByte((byte) 1);
            out.writeDouble(this.mSleep.doubleValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mSportAR != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mSportAR);
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

    public AEntityHelper<RawSportHealth> getHelper() {
        return RawSportHealthHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.collectencrypt.RawSportHealth";
    }

    public String getDatabaseName() {
        return "dsCollectEncrypt";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("RawSportHealth { mId: ").append(this.mId);
        sb.append(", mTimeStamp: ").append(this.mTimeStamp);
        sb.append(", mHeight: ").append(this.mHeight);
        sb.append(", mWeight: ").append(this.mWeight);
        sb.append(", mHeartRat: ").append(this.mHeartRat);
        sb.append(", mBloodPressureLow: ").append(this.mBloodPressureLow);
        sb.append(", mBloodPressureHigh: ").append(this.mBloodPressureHigh);
        sb.append(", mBloodSugar: ").append(this.mBloodSugar);
        sb.append(", mSportDistance: ").append(this.mSportDistance);
        sb.append(", mSportHeight: ").append(this.mSportHeight);
        sb.append(", mSportHeat: ").append(this.mSportHeat);
        sb.append(", mSportPaces: ").append(this.mSportPaces);
        sb.append(", mSleep: ").append(this.mSleep);
        sb.append(", mSportAR: ").append(this.mSportAR);
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
