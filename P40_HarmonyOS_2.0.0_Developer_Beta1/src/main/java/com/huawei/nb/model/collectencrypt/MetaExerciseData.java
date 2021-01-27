package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class MetaExerciseData extends AManagedObject {
    public static final Parcelable.Creator<MetaExerciseData> CREATOR = new Parcelable.Creator<MetaExerciseData>() {
        /* class com.huawei.nb.model.collectencrypt.MetaExerciseData.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public MetaExerciseData createFromParcel(Parcel parcel) {
            return new MetaExerciseData(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public MetaExerciseData[] newArray(int i) {
            return new MetaExerciseData[i];
        }
    };
    private Integer mClimb;
    private Integer mCycling;
    private Integer mDecline;
    private Double mHeight;
    private Integer mId;
    private Integer mReservedInt;
    private String mReservedText;
    private Integer mRun;
    private Double mSleep;
    private String mSportAR;
    private Double mSportDistance;
    private Double mSportHeat;
    private Double mSportPaces;
    private Date mTimeStamp;
    private Integer mWalk;
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
        return "com.huawei.nb.model.collectencrypt.MetaExerciseData";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public MetaExerciseData(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Integer num = null;
        this.mId = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.mTimeStamp = cursor.isNull(2) ? null : new Date(cursor.getLong(2));
        this.mSportHeat = cursor.isNull(3) ? null : Double.valueOf(cursor.getDouble(3));
        this.mClimb = cursor.isNull(4) ? null : Integer.valueOf(cursor.getInt(4));
        this.mDecline = cursor.isNull(5) ? null : Integer.valueOf(cursor.getInt(5));
        this.mSportDistance = cursor.isNull(6) ? null : Double.valueOf(cursor.getDouble(6));
        this.mSleep = cursor.isNull(7) ? null : Double.valueOf(cursor.getDouble(7));
        this.mSportPaces = cursor.isNull(8) ? null : Double.valueOf(cursor.getDouble(8));
        this.mHeight = cursor.isNull(9) ? null : Double.valueOf(cursor.getDouble(9));
        this.mWeight = cursor.isNull(10) ? null : Double.valueOf(cursor.getDouble(10));
        this.mSportAR = cursor.getString(11);
        this.mWalk = cursor.isNull(12) ? null : Integer.valueOf(cursor.getInt(12));
        this.mRun = cursor.isNull(13) ? null : Integer.valueOf(cursor.getInt(13));
        this.mCycling = cursor.isNull(14) ? null : Integer.valueOf(cursor.getInt(14));
        this.mReservedInt = !cursor.isNull(15) ? Integer.valueOf(cursor.getInt(15)) : num;
        this.mReservedText = cursor.getString(16);
    }

    public MetaExerciseData(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.mId = null;
            parcel.readInt();
        } else {
            this.mId = Integer.valueOf(parcel.readInt());
        }
        this.mTimeStamp = parcel.readByte() == 0 ? null : new Date(parcel.readLong());
        this.mSportHeat = parcel.readByte() == 0 ? null : Double.valueOf(parcel.readDouble());
        this.mClimb = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mDecline = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mSportDistance = parcel.readByte() == 0 ? null : Double.valueOf(parcel.readDouble());
        this.mSleep = parcel.readByte() == 0 ? null : Double.valueOf(parcel.readDouble());
        this.mSportPaces = parcel.readByte() == 0 ? null : Double.valueOf(parcel.readDouble());
        this.mHeight = parcel.readByte() == 0 ? null : Double.valueOf(parcel.readDouble());
        this.mWeight = parcel.readByte() == 0 ? null : Double.valueOf(parcel.readDouble());
        this.mSportAR = parcel.readByte() == 0 ? null : parcel.readString();
        this.mWalk = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mRun = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mCycling = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mReservedInt = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mReservedText = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private MetaExerciseData(Integer num, Date date, Double d, Integer num2, Integer num3, Double d2, Double d3, Double d4, Double d5, Double d6, String str, Integer num4, Integer num5, Integer num6, Integer num7, String str2) {
        this.mId = num;
        this.mTimeStamp = date;
        this.mSportHeat = d;
        this.mClimb = num2;
        this.mDecline = num3;
        this.mSportDistance = d2;
        this.mSleep = d3;
        this.mSportPaces = d4;
        this.mHeight = d5;
        this.mWeight = d6;
        this.mSportAR = str;
        this.mWalk = num4;
        this.mRun = num5;
        this.mCycling = num6;
        this.mReservedInt = num7;
        this.mReservedText = str2;
    }

    public MetaExerciseData() {
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

    public Double getMSportHeat() {
        return this.mSportHeat;
    }

    public void setMSportHeat(Double d) {
        this.mSportHeat = d;
        setValue();
    }

    public Integer getMClimb() {
        return this.mClimb;
    }

    public void setMClimb(Integer num) {
        this.mClimb = num;
        setValue();
    }

    public Integer getMDecline() {
        return this.mDecline;
    }

    public void setMDecline(Integer num) {
        this.mDecline = num;
        setValue();
    }

    public Double getMSportDistance() {
        return this.mSportDistance;
    }

    public void setMSportDistance(Double d) {
        this.mSportDistance = d;
        setValue();
    }

    public Double getMSleep() {
        return this.mSleep;
    }

    public void setMSleep(Double d) {
        this.mSleep = d;
        setValue();
    }

    public Double getMSportPaces() {
        return this.mSportPaces;
    }

    public void setMSportPaces(Double d) {
        this.mSportPaces = d;
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

    public String getMSportAR() {
        return this.mSportAR;
    }

    public void setMSportAR(String str) {
        this.mSportAR = str;
        setValue();
    }

    public Integer getMWalk() {
        return this.mWalk;
    }

    public void setMWalk(Integer num) {
        this.mWalk = num;
        setValue();
    }

    public Integer getMRun() {
        return this.mRun;
    }

    public void setMRun(Integer num) {
        this.mRun = num;
        setValue();
    }

    public Integer getMCycling() {
        return this.mCycling;
    }

    public void setMCycling(Integer num) {
        this.mCycling = num;
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
        if (this.mSportHeat != null) {
            parcel.writeByte((byte) 1);
            parcel.writeDouble(this.mSportHeat.doubleValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mClimb != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mClimb.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mDecline != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mDecline.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mSportDistance != null) {
            parcel.writeByte((byte) 1);
            parcel.writeDouble(this.mSportDistance.doubleValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mSleep != null) {
            parcel.writeByte((byte) 1);
            parcel.writeDouble(this.mSleep.doubleValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mSportPaces != null) {
            parcel.writeByte((byte) 1);
            parcel.writeDouble(this.mSportPaces.doubleValue());
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
        if (this.mSportAR != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mSportAR);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mWalk != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mWalk.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mRun != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mRun.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mCycling != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mCycling.intValue());
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
    public AEntityHelper<MetaExerciseData> getHelper() {
        return MetaExerciseDataHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "MetaExerciseData { mId: " + this.mId + ", mTimeStamp: " + this.mTimeStamp + ", mSportHeat: " + this.mSportHeat + ", mClimb: " + this.mClimb + ", mDecline: " + this.mDecline + ", mSportDistance: " + this.mSportDistance + ", mSleep: " + this.mSleep + ", mSportPaces: " + this.mSportPaces + ", mHeight: " + this.mHeight + ", mWeight: " + this.mWeight + ", mSportAR: " + this.mSportAR + ", mWalk: " + this.mWalk + ", mRun: " + this.mRun + ", mCycling: " + this.mCycling + ", mReservedInt: " + this.mReservedInt + ", mReservedText: " + this.mReservedText + " }";
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
