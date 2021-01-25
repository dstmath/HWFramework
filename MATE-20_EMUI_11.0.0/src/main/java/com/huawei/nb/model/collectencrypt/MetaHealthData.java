package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class MetaHealthData extends AManagedObject {
    public static final Parcelable.Creator<MetaHealthData> CREATOR = new Parcelable.Creator<MetaHealthData>() {
        /* class com.huawei.nb.model.collectencrypt.MetaHealthData.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public MetaHealthData createFromParcel(Parcel parcel) {
            return new MetaHealthData(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public MetaHealthData[] newArray(int i) {
            return new MetaHealthData[i];
        }
    };
    private Double mBloodPressure_high;
    private Double mBloodPressure_low;
    private Double mBloodSugar;
    private Double mHeartRat;
    private Integer mId;
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
        return "com.huawei.nb.model.collectencrypt.MetaHealthData";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public MetaHealthData(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Integer num = null;
        this.mId = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.mTimeStamp = cursor.isNull(2) ? null : new Date(cursor.getLong(2));
        this.mHeartRat = cursor.isNull(3) ? null : Double.valueOf(cursor.getDouble(3));
        this.mBloodPressure_low = cursor.isNull(4) ? null : Double.valueOf(cursor.getDouble(4));
        this.mBloodPressure_high = cursor.isNull(5) ? null : Double.valueOf(cursor.getDouble(5));
        this.mBloodSugar = cursor.isNull(6) ? null : Double.valueOf(cursor.getDouble(6));
        this.mReservedInt = !cursor.isNull(7) ? Integer.valueOf(cursor.getInt(7)) : num;
        this.mReservedText = cursor.getString(8);
    }

    public MetaHealthData(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.mId = null;
            parcel.readInt();
        } else {
            this.mId = Integer.valueOf(parcel.readInt());
        }
        this.mTimeStamp = parcel.readByte() == 0 ? null : new Date(parcel.readLong());
        this.mHeartRat = parcel.readByte() == 0 ? null : Double.valueOf(parcel.readDouble());
        this.mBloodPressure_low = parcel.readByte() == 0 ? null : Double.valueOf(parcel.readDouble());
        this.mBloodPressure_high = parcel.readByte() == 0 ? null : Double.valueOf(parcel.readDouble());
        this.mBloodSugar = parcel.readByte() == 0 ? null : Double.valueOf(parcel.readDouble());
        this.mReservedInt = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mReservedText = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private MetaHealthData(Integer num, Date date, Double d, Double d2, Double d3, Double d4, Integer num2, String str) {
        this.mId = num;
        this.mTimeStamp = date;
        this.mHeartRat = d;
        this.mBloodPressure_low = d2;
        this.mBloodPressure_high = d3;
        this.mBloodSugar = d4;
        this.mReservedInt = num2;
        this.mReservedText = str;
    }

    public MetaHealthData() {
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

    public Double getMHeartRat() {
        return this.mHeartRat;
    }

    public void setMHeartRat(Double d) {
        this.mHeartRat = d;
        setValue();
    }

    public Double getMBloodPressure_low() {
        return this.mBloodPressure_low;
    }

    public void setMBloodPressure_low(Double d) {
        this.mBloodPressure_low = d;
        setValue();
    }

    public Double getMBloodPressure_high() {
        return this.mBloodPressure_high;
    }

    public void setMBloodPressure_high(Double d) {
        this.mBloodPressure_high = d;
        setValue();
    }

    public Double getMBloodSugar() {
        return this.mBloodSugar;
    }

    public void setMBloodSugar(Double d) {
        this.mBloodSugar = d;
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
        if (this.mHeartRat != null) {
            parcel.writeByte((byte) 1);
            parcel.writeDouble(this.mHeartRat.doubleValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mBloodPressure_low != null) {
            parcel.writeByte((byte) 1);
            parcel.writeDouble(this.mBloodPressure_low.doubleValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mBloodPressure_high != null) {
            parcel.writeByte((byte) 1);
            parcel.writeDouble(this.mBloodPressure_high.doubleValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mBloodSugar != null) {
            parcel.writeByte((byte) 1);
            parcel.writeDouble(this.mBloodSugar.doubleValue());
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
    public AEntityHelper<MetaHealthData> getHelper() {
        return MetaHealthDataHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "MetaHealthData { mId: " + this.mId + ", mTimeStamp: " + this.mTimeStamp + ", mHeartRat: " + this.mHeartRat + ", mBloodPressure_low: " + this.mBloodPressure_low + ", mBloodPressure_high: " + this.mBloodPressure_high + ", mBloodSugar: " + this.mBloodSugar + ", mReservedInt: " + this.mReservedInt + ", mReservedText: " + this.mReservedText + " }";
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
