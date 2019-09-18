package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class MetaBankInfo extends AManagedObject {
    public static final Parcelable.Creator<MetaBankInfo> CREATOR = new Parcelable.Creator<MetaBankInfo>() {
        public MetaBankInfo createFromParcel(Parcel in) {
            return new MetaBankInfo(in);
        }

        public MetaBankInfo[] newArray(int size) {
            return new MetaBankInfo[size];
        }
    };
    private Integer mId;
    private Integer mInRange;
    private Integer mOutRange;
    private Integer mReservedInt;
    private String mReservedText;
    private Date mTimeStamp;

    public MetaBankInfo(Cursor cursor) {
        Integer num = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.mId = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.mTimeStamp = cursor.isNull(2) ? null : new Date(cursor.getLong(2));
        this.mInRange = cursor.isNull(3) ? null : Integer.valueOf(cursor.getInt(3));
        this.mOutRange = cursor.isNull(4) ? null : Integer.valueOf(cursor.getInt(4));
        this.mReservedInt = !cursor.isNull(5) ? Integer.valueOf(cursor.getInt(5)) : num;
        this.mReservedText = cursor.getString(6);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public MetaBankInfo(Parcel in) {
        super(in);
        String str = null;
        if (in.readByte() == 0) {
            this.mId = null;
            in.readInt();
        } else {
            this.mId = Integer.valueOf(in.readInt());
        }
        this.mTimeStamp = in.readByte() == 0 ? null : new Date(in.readLong());
        this.mInRange = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mOutRange = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mReservedInt = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mReservedText = in.readByte() != 0 ? in.readString() : str;
    }

    private MetaBankInfo(Integer mId2, Date mTimeStamp2, Integer mInRange2, Integer mOutRange2, Integer mReservedInt2, String mReservedText2) {
        this.mId = mId2;
        this.mTimeStamp = mTimeStamp2;
        this.mInRange = mInRange2;
        this.mOutRange = mOutRange2;
        this.mReservedInt = mReservedInt2;
        this.mReservedText = mReservedText2;
    }

    public MetaBankInfo() {
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

    public Integer getMInRange() {
        return this.mInRange;
    }

    public void setMInRange(Integer mInRange2) {
        this.mInRange = mInRange2;
        setValue();
    }

    public Integer getMOutRange() {
        return this.mOutRange;
    }

    public void setMOutRange(Integer mOutRange2) {
        this.mOutRange = mOutRange2;
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
        if (this.mInRange != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mInRange.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mOutRange != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mOutRange.intValue());
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

    public AEntityHelper<MetaBankInfo> getHelper() {
        return MetaBankInfoHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.collectencrypt.MetaBankInfo";
    }

    public String getDatabaseName() {
        return "dsCollectEncrypt";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("MetaBankInfo { mId: ").append(this.mId);
        sb.append(", mTimeStamp: ").append(this.mTimeStamp);
        sb.append(", mInRange: ").append(this.mInRange);
        sb.append(", mOutRange: ").append(this.mOutRange);
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
