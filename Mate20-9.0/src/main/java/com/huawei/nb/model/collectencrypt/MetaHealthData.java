package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class MetaHealthData extends AManagedObject {
    public static final Parcelable.Creator<MetaHealthData> CREATOR = new Parcelable.Creator<MetaHealthData>() {
        public MetaHealthData createFromParcel(Parcel in) {
            return new MetaHealthData(in);
        }

        public MetaHealthData[] newArray(int size) {
            return new MetaHealthData[size];
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

    public MetaHealthData(Cursor cursor) {
        Integer num = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.mId = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.mTimeStamp = cursor.isNull(2) ? null : new Date(cursor.getLong(2));
        this.mHeartRat = cursor.isNull(3) ? null : Double.valueOf(cursor.getDouble(3));
        this.mBloodPressure_low = cursor.isNull(4) ? null : Double.valueOf(cursor.getDouble(4));
        this.mBloodPressure_high = cursor.isNull(5) ? null : Double.valueOf(cursor.getDouble(5));
        this.mBloodSugar = cursor.isNull(6) ? null : Double.valueOf(cursor.getDouble(6));
        this.mReservedInt = !cursor.isNull(7) ? Integer.valueOf(cursor.getInt(7)) : num;
        this.mReservedText = cursor.getString(8);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public MetaHealthData(Parcel in) {
        super(in);
        String str = null;
        if (in.readByte() == 0) {
            this.mId = null;
            in.readInt();
        } else {
            this.mId = Integer.valueOf(in.readInt());
        }
        this.mTimeStamp = in.readByte() == 0 ? null : new Date(in.readLong());
        this.mHeartRat = in.readByte() == 0 ? null : Double.valueOf(in.readDouble());
        this.mBloodPressure_low = in.readByte() == 0 ? null : Double.valueOf(in.readDouble());
        this.mBloodPressure_high = in.readByte() == 0 ? null : Double.valueOf(in.readDouble());
        this.mBloodSugar = in.readByte() == 0 ? null : Double.valueOf(in.readDouble());
        this.mReservedInt = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mReservedText = in.readByte() != 0 ? in.readString() : str;
    }

    private MetaHealthData(Integer mId2, Date mTimeStamp2, Double mHeartRat2, Double mBloodPressure_low2, Double mBloodPressure_high2, Double mBloodSugar2, Integer mReservedInt2, String mReservedText2) {
        this.mId = mId2;
        this.mTimeStamp = mTimeStamp2;
        this.mHeartRat = mHeartRat2;
        this.mBloodPressure_low = mBloodPressure_low2;
        this.mBloodPressure_high = mBloodPressure_high2;
        this.mBloodSugar = mBloodSugar2;
        this.mReservedInt = mReservedInt2;
        this.mReservedText = mReservedText2;
    }

    public MetaHealthData() {
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

    public Double getMHeartRat() {
        return this.mHeartRat;
    }

    public void setMHeartRat(Double mHeartRat2) {
        this.mHeartRat = mHeartRat2;
        setValue();
    }

    public Double getMBloodPressure_low() {
        return this.mBloodPressure_low;
    }

    public void setMBloodPressure_low(Double mBloodPressure_low2) {
        this.mBloodPressure_low = mBloodPressure_low2;
        setValue();
    }

    public Double getMBloodPressure_high() {
        return this.mBloodPressure_high;
    }

    public void setMBloodPressure_high(Double mBloodPressure_high2) {
        this.mBloodPressure_high = mBloodPressure_high2;
        setValue();
    }

    public Double getMBloodSugar() {
        return this.mBloodSugar;
    }

    public void setMBloodSugar(Double mBloodSugar2) {
        this.mBloodSugar = mBloodSugar2;
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
        if (this.mHeartRat != null) {
            out.writeByte((byte) 1);
            out.writeDouble(this.mHeartRat.doubleValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mBloodPressure_low != null) {
            out.writeByte((byte) 1);
            out.writeDouble(this.mBloodPressure_low.doubleValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mBloodPressure_high != null) {
            out.writeByte((byte) 1);
            out.writeDouble(this.mBloodPressure_high.doubleValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mBloodSugar != null) {
            out.writeByte((byte) 1);
            out.writeDouble(this.mBloodSugar.doubleValue());
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

    public AEntityHelper<MetaHealthData> getHelper() {
        return MetaHealthDataHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.collectencrypt.MetaHealthData";
    }

    public String getDatabaseName() {
        return "dsCollectEncrypt";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("MetaHealthData { mId: ").append(this.mId);
        sb.append(", mTimeStamp: ").append(this.mTimeStamp);
        sb.append(", mHeartRat: ").append(this.mHeartRat);
        sb.append(", mBloodPressure_low: ").append(this.mBloodPressure_low);
        sb.append(", mBloodPressure_high: ").append(this.mBloodPressure_high);
        sb.append(", mBloodSugar: ").append(this.mBloodSugar);
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
