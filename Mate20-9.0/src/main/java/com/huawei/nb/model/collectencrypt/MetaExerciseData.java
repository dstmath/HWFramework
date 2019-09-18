package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class MetaExerciseData extends AManagedObject {
    public static final Parcelable.Creator<MetaExerciseData> CREATOR = new Parcelable.Creator<MetaExerciseData>() {
        public MetaExerciseData createFromParcel(Parcel in) {
            return new MetaExerciseData(in);
        }

        public MetaExerciseData[] newArray(int size) {
            return new MetaExerciseData[size];
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

    public MetaExerciseData(Cursor cursor) {
        Integer num = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
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

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public MetaExerciseData(Parcel in) {
        super(in);
        String str = null;
        if (in.readByte() == 0) {
            this.mId = null;
            in.readInt();
        } else {
            this.mId = Integer.valueOf(in.readInt());
        }
        this.mTimeStamp = in.readByte() == 0 ? null : new Date(in.readLong());
        this.mSportHeat = in.readByte() == 0 ? null : Double.valueOf(in.readDouble());
        this.mClimb = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mDecline = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mSportDistance = in.readByte() == 0 ? null : Double.valueOf(in.readDouble());
        this.mSleep = in.readByte() == 0 ? null : Double.valueOf(in.readDouble());
        this.mSportPaces = in.readByte() == 0 ? null : Double.valueOf(in.readDouble());
        this.mHeight = in.readByte() == 0 ? null : Double.valueOf(in.readDouble());
        this.mWeight = in.readByte() == 0 ? null : Double.valueOf(in.readDouble());
        this.mSportAR = in.readByte() == 0 ? null : in.readString();
        this.mWalk = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mRun = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mCycling = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mReservedInt = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mReservedText = in.readByte() != 0 ? in.readString() : str;
    }

    private MetaExerciseData(Integer mId2, Date mTimeStamp2, Double mSportHeat2, Integer mClimb2, Integer mDecline2, Double mSportDistance2, Double mSleep2, Double mSportPaces2, Double mHeight2, Double mWeight2, String mSportAR2, Integer mWalk2, Integer mRun2, Integer mCycling2, Integer mReservedInt2, String mReservedText2) {
        this.mId = mId2;
        this.mTimeStamp = mTimeStamp2;
        this.mSportHeat = mSportHeat2;
        this.mClimb = mClimb2;
        this.mDecline = mDecline2;
        this.mSportDistance = mSportDistance2;
        this.mSleep = mSleep2;
        this.mSportPaces = mSportPaces2;
        this.mHeight = mHeight2;
        this.mWeight = mWeight2;
        this.mSportAR = mSportAR2;
        this.mWalk = mWalk2;
        this.mRun = mRun2;
        this.mCycling = mCycling2;
        this.mReservedInt = mReservedInt2;
        this.mReservedText = mReservedText2;
    }

    public MetaExerciseData() {
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

    public Double getMSportHeat() {
        return this.mSportHeat;
    }

    public void setMSportHeat(Double mSportHeat2) {
        this.mSportHeat = mSportHeat2;
        setValue();
    }

    public Integer getMClimb() {
        return this.mClimb;
    }

    public void setMClimb(Integer mClimb2) {
        this.mClimb = mClimb2;
        setValue();
    }

    public Integer getMDecline() {
        return this.mDecline;
    }

    public void setMDecline(Integer mDecline2) {
        this.mDecline = mDecline2;
        setValue();
    }

    public Double getMSportDistance() {
        return this.mSportDistance;
    }

    public void setMSportDistance(Double mSportDistance2) {
        this.mSportDistance = mSportDistance2;
        setValue();
    }

    public Double getMSleep() {
        return this.mSleep;
    }

    public void setMSleep(Double mSleep2) {
        this.mSleep = mSleep2;
        setValue();
    }

    public Double getMSportPaces() {
        return this.mSportPaces;
    }

    public void setMSportPaces(Double mSportPaces2) {
        this.mSportPaces = mSportPaces2;
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

    public String getMSportAR() {
        return this.mSportAR;
    }

    public void setMSportAR(String mSportAR2) {
        this.mSportAR = mSportAR2;
        setValue();
    }

    public Integer getMWalk() {
        return this.mWalk;
    }

    public void setMWalk(Integer mWalk2) {
        this.mWalk = mWalk2;
        setValue();
    }

    public Integer getMRun() {
        return this.mRun;
    }

    public void setMRun(Integer mRun2) {
        this.mRun = mRun2;
        setValue();
    }

    public Integer getMCycling() {
        return this.mCycling;
    }

    public void setMCycling(Integer mCycling2) {
        this.mCycling = mCycling2;
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
        if (this.mSportHeat != null) {
            out.writeByte((byte) 1);
            out.writeDouble(this.mSportHeat.doubleValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mClimb != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mClimb.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mDecline != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mDecline.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mSportDistance != null) {
            out.writeByte((byte) 1);
            out.writeDouble(this.mSportDistance.doubleValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mSleep != null) {
            out.writeByte((byte) 1);
            out.writeDouble(this.mSleep.doubleValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mSportPaces != null) {
            out.writeByte((byte) 1);
            out.writeDouble(this.mSportPaces.doubleValue());
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
        if (this.mSportAR != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mSportAR);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mWalk != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mWalk.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mRun != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mRun.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mCycling != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mCycling.intValue());
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

    public AEntityHelper<MetaExerciseData> getHelper() {
        return MetaExerciseDataHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.collectencrypt.MetaExerciseData";
    }

    public String getDatabaseName() {
        return "dsCollectEncrypt";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("MetaExerciseData { mId: ").append(this.mId);
        sb.append(", mTimeStamp: ").append(this.mTimeStamp);
        sb.append(", mSportHeat: ").append(this.mSportHeat);
        sb.append(", mClimb: ").append(this.mClimb);
        sb.append(", mDecline: ").append(this.mDecline);
        sb.append(", mSportDistance: ").append(this.mSportDistance);
        sb.append(", mSleep: ").append(this.mSleep);
        sb.append(", mSportPaces: ").append(this.mSportPaces);
        sb.append(", mHeight: ").append(this.mHeight);
        sb.append(", mWeight: ").append(this.mWeight);
        sb.append(", mSportAR: ").append(this.mSportAR);
        sb.append(", mWalk: ").append(this.mWalk);
        sb.append(", mRun: ").append(this.mRun);
        sb.append(", mCycling: ").append(this.mCycling);
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
