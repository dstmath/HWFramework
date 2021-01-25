package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RawSportHealth extends AManagedObject {
    public static final Parcelable.Creator<RawSportHealth> CREATOR = new Parcelable.Creator<RawSportHealth>() {
        /* class com.huawei.nb.model.collectencrypt.RawSportHealth.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public RawSportHealth createFromParcel(Parcel parcel) {
            return new RawSportHealth(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public RawSportHealth[] newArray(int i) {
            return new RawSportHealth[i];
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
        return "com.huawei.nb.model.collectencrypt.RawSportHealth";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public RawSportHealth(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Integer num = null;
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

    public RawSportHealth(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.mId = null;
            parcel.readInt();
        } else {
            this.mId = Integer.valueOf(parcel.readInt());
        }
        this.mTimeStamp = parcel.readByte() == 0 ? null : new Date(parcel.readLong());
        this.mHeight = parcel.readByte() == 0 ? null : Double.valueOf(parcel.readDouble());
        this.mWeight = parcel.readByte() == 0 ? null : Double.valueOf(parcel.readDouble());
        this.mHeartRat = parcel.readByte() == 0 ? null : Double.valueOf(parcel.readDouble());
        this.mBloodPressureLow = parcel.readByte() == 0 ? null : Double.valueOf(parcel.readDouble());
        this.mBloodPressureHigh = parcel.readByte() == 0 ? null : Double.valueOf(parcel.readDouble());
        this.mBloodSugar = parcel.readByte() == 0 ? null : Double.valueOf(parcel.readDouble());
        this.mSportDistance = parcel.readByte() == 0 ? null : Double.valueOf(parcel.readDouble());
        this.mSportHeight = parcel.readByte() == 0 ? null : Double.valueOf(parcel.readDouble());
        this.mSportHeat = parcel.readByte() == 0 ? null : Double.valueOf(parcel.readDouble());
        this.mSportPaces = parcel.readByte() == 0 ? null : Double.valueOf(parcel.readDouble());
        this.mSleep = parcel.readByte() == 0 ? null : Double.valueOf(parcel.readDouble());
        this.mSportAR = parcel.readByte() == 0 ? null : parcel.readString();
        this.mReservedInt = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mReservedText = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private RawSportHealth(Integer num, Date date, Double d, Double d2, Double d3, Double d4, Double d5, Double d6, Double d7, Double d8, Double d9, Double d10, Double d11, String str, Integer num2, String str2) {
        this.mId = num;
        this.mTimeStamp = date;
        this.mHeight = d;
        this.mWeight = d2;
        this.mHeartRat = d3;
        this.mBloodPressureLow = d4;
        this.mBloodPressureHigh = d5;
        this.mBloodSugar = d6;
        this.mSportDistance = d7;
        this.mSportHeight = d8;
        this.mSportHeat = d9;
        this.mSportPaces = d10;
        this.mSleep = d11;
        this.mSportAR = str;
        this.mReservedInt = num2;
        this.mReservedText = str2;
    }

    public RawSportHealth() {
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

    public Double getMHeight() {
        return this.mHeight;
    }

    public void setMHeight(Double d) {
        this.mHeight = d;
        setValue();
    }

    public Double getMWeight() {
        return this.mWeight;
    }

    public void setMWeight(Double d) {
        this.mWeight = d;
        setValue();
    }

    public Double getMHeartRat() {
        return this.mHeartRat;
    }

    public void setMHeartRat(Double d) {
        this.mHeartRat = d;
        setValue();
    }

    public Double getMBloodPressureLow() {
        return this.mBloodPressureLow;
    }

    public void setMBloodPressureLow(Double d) {
        this.mBloodPressureLow = d;
        setValue();
    }

    public Double getMBloodPressureHigh() {
        return this.mBloodPressureHigh;
    }

    public void setMBloodPressureHigh(Double d) {
        this.mBloodPressureHigh = d;
        setValue();
    }

    public Double getMBloodSugar() {
        return this.mBloodSugar;
    }

    public void setMBloodSugar(Double d) {
        this.mBloodSugar = d;
        setValue();
    }

    public Double getMSportDistance() {
        return this.mSportDistance;
    }

    public void setMSportDistance(Double d) {
        this.mSportDistance = d;
        setValue();
    }

    public Double getMSportHeight() {
        return this.mSportHeight;
    }

    public void setMSportHeight(Double d) {
        this.mSportHeight = d;
        setValue();
    }

    public Double getMSportHeat() {
        return this.mSportHeat;
    }

    public void setMSportHeat(Double d) {
        this.mSportHeat = d;
        setValue();
    }

    public Double getMSportPaces() {
        return this.mSportPaces;
    }

    public void setMSportPaces(Double d) {
        this.mSportPaces = d;
        setValue();
    }

    public Double getMSleep() {
        return this.mSleep;
    }

    public void setMSleep(Double d) {
        this.mSleep = d;
        setValue();
    }

    public String getMSportAR() {
        return this.mSportAR;
    }

    public void setMSportAR(String str) {
        this.mSportAR = str;
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
        if (this.mHeight != null) {
            parcel.writeByte((byte) 1);
            parcel.writeDouble(this.mHeight.doubleValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mWeight != null) {
            parcel.writeByte((byte) 1);
            parcel.writeDouble(this.mWeight.doubleValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mHeartRat != null) {
            parcel.writeByte((byte) 1);
            parcel.writeDouble(this.mHeartRat.doubleValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mBloodPressureLow != null) {
            parcel.writeByte((byte) 1);
            parcel.writeDouble(this.mBloodPressureLow.doubleValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mBloodPressureHigh != null) {
            parcel.writeByte((byte) 1);
            parcel.writeDouble(this.mBloodPressureHigh.doubleValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mBloodSugar != null) {
            parcel.writeByte((byte) 1);
            parcel.writeDouble(this.mBloodSugar.doubleValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mSportDistance != null) {
            parcel.writeByte((byte) 1);
            parcel.writeDouble(this.mSportDistance.doubleValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mSportHeight != null) {
            parcel.writeByte((byte) 1);
            parcel.writeDouble(this.mSportHeight.doubleValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mSportHeat != null) {
            parcel.writeByte((byte) 1);
            parcel.writeDouble(this.mSportHeat.doubleValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mSportPaces != null) {
            parcel.writeByte((byte) 1);
            parcel.writeDouble(this.mSportPaces.doubleValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mSleep != null) {
            parcel.writeByte((byte) 1);
            parcel.writeDouble(this.mSleep.doubleValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mSportAR != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mSportAR);
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
    public AEntityHelper<RawSportHealth> getHelper() {
        return RawSportHealthHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "RawSportHealth { mId: " + this.mId + ", mTimeStamp: " + this.mTimeStamp + ", mHeight: " + this.mHeight + ", mWeight: " + this.mWeight + ", mHeartRat: " + this.mHeartRat + ", mBloodPressureLow: " + this.mBloodPressureLow + ", mBloodPressureHigh: " + this.mBloodPressureHigh + ", mBloodSugar: " + this.mBloodSugar + ", mSportDistance: " + this.mSportDistance + ", mSportHeight: " + this.mSportHeight + ", mSportHeat: " + this.mSportHeat + ", mSportPaces: " + this.mSportPaces + ", mSleep: " + this.mSleep + ", mSportAR: " + this.mSportAR + ", mReservedInt: " + this.mReservedInt + ", mReservedText: " + this.mReservedText + " }";
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
