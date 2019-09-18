package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RawBankInfo extends AManagedObject {
    public static final Parcelable.Creator<RawBankInfo> CREATOR = new Parcelable.Creator<RawBankInfo>() {
        public RawBankInfo createFromParcel(Parcel in) {
            return new RawBankInfo(in);
        }

        public RawBankInfo[] newArray(int size) {
            return new RawBankInfo[size];
        }
    };
    private String mBankInfo;
    private Integer mId;
    private String mLastNo;
    private Double mRepayAmountUSD;
    private Double mRepayLowestCNY;
    private Double mRepayLowestUSD;
    private Double mRepaymentAmountCNY;
    private Date mRepaymentDate;
    private Integer mReservedInt;
    private String mReservedText;
    private Date mTimeStamp;

    public RawBankInfo(Cursor cursor) {
        Integer num = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.mId = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.mTimeStamp = cursor.isNull(2) ? null : new Date(cursor.getLong(2));
        this.mBankInfo = cursor.getString(3);
        this.mLastNo = cursor.getString(4);
        this.mRepaymentDate = cursor.isNull(5) ? null : new Date(cursor.getLong(5));
        this.mRepaymentAmountCNY = cursor.isNull(6) ? null : Double.valueOf(cursor.getDouble(6));
        this.mRepayLowestCNY = cursor.isNull(7) ? null : Double.valueOf(cursor.getDouble(7));
        this.mRepayAmountUSD = cursor.isNull(8) ? null : Double.valueOf(cursor.getDouble(8));
        this.mRepayLowestUSD = cursor.isNull(9) ? null : Double.valueOf(cursor.getDouble(9));
        this.mReservedInt = !cursor.isNull(10) ? Integer.valueOf(cursor.getInt(10)) : num;
        this.mReservedText = cursor.getString(11);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public RawBankInfo(Parcel in) {
        super(in);
        String str = null;
        if (in.readByte() == 0) {
            this.mId = null;
            in.readInt();
        } else {
            this.mId = Integer.valueOf(in.readInt());
        }
        this.mTimeStamp = in.readByte() == 0 ? null : new Date(in.readLong());
        this.mBankInfo = in.readByte() == 0 ? null : in.readString();
        this.mLastNo = in.readByte() == 0 ? null : in.readString();
        this.mRepaymentDate = in.readByte() == 0 ? null : new Date(in.readLong());
        this.mRepaymentAmountCNY = in.readByte() == 0 ? null : Double.valueOf(in.readDouble());
        this.mRepayLowestCNY = in.readByte() == 0 ? null : Double.valueOf(in.readDouble());
        this.mRepayAmountUSD = in.readByte() == 0 ? null : Double.valueOf(in.readDouble());
        this.mRepayLowestUSD = in.readByte() == 0 ? null : Double.valueOf(in.readDouble());
        this.mReservedInt = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mReservedText = in.readByte() != 0 ? in.readString() : str;
    }

    private RawBankInfo(Integer mId2, Date mTimeStamp2, String mBankInfo2, String mLastNo2, Date mRepaymentDate2, Double mRepaymentAmountCNY2, Double mRepayLowestCNY2, Double mRepayAmountUSD2, Double mRepayLowestUSD2, Integer mReservedInt2, String mReservedText2) {
        this.mId = mId2;
        this.mTimeStamp = mTimeStamp2;
        this.mBankInfo = mBankInfo2;
        this.mLastNo = mLastNo2;
        this.mRepaymentDate = mRepaymentDate2;
        this.mRepaymentAmountCNY = mRepaymentAmountCNY2;
        this.mRepayLowestCNY = mRepayLowestCNY2;
        this.mRepayAmountUSD = mRepayAmountUSD2;
        this.mRepayLowestUSD = mRepayLowestUSD2;
        this.mReservedInt = mReservedInt2;
        this.mReservedText = mReservedText2;
    }

    public RawBankInfo() {
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

    public String getMBankInfo() {
        return this.mBankInfo;
    }

    public void setMBankInfo(String mBankInfo2) {
        this.mBankInfo = mBankInfo2;
        setValue();
    }

    public String getMLastNo() {
        return this.mLastNo;
    }

    public void setMLastNo(String mLastNo2) {
        this.mLastNo = mLastNo2;
        setValue();
    }

    public Date getMRepaymentDate() {
        return this.mRepaymentDate;
    }

    public void setMRepaymentDate(Date mRepaymentDate2) {
        this.mRepaymentDate = mRepaymentDate2;
        setValue();
    }

    public Double getMRepaymentAmountCNY() {
        return this.mRepaymentAmountCNY;
    }

    public void setMRepaymentAmountCNY(Double mRepaymentAmountCNY2) {
        this.mRepaymentAmountCNY = mRepaymentAmountCNY2;
        setValue();
    }

    public Double getMRepayLowestCNY() {
        return this.mRepayLowestCNY;
    }

    public void setMRepayLowestCNY(Double mRepayLowestCNY2) {
        this.mRepayLowestCNY = mRepayLowestCNY2;
        setValue();
    }

    public Double getMRepayAmountUSD() {
        return this.mRepayAmountUSD;
    }

    public void setMRepayAmountUSD(Double mRepayAmountUSD2) {
        this.mRepayAmountUSD = mRepayAmountUSD2;
        setValue();
    }

    public Double getMRepayLowestUSD() {
        return this.mRepayLowestUSD;
    }

    public void setMRepayLowestUSD(Double mRepayLowestUSD2) {
        this.mRepayLowestUSD = mRepayLowestUSD2;
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
        if (this.mBankInfo != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mBankInfo);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mLastNo != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mLastNo);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mRepaymentDate != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.mRepaymentDate.getTime());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mRepaymentAmountCNY != null) {
            out.writeByte((byte) 1);
            out.writeDouble(this.mRepaymentAmountCNY.doubleValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mRepayLowestCNY != null) {
            out.writeByte((byte) 1);
            out.writeDouble(this.mRepayLowestCNY.doubleValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mRepayAmountUSD != null) {
            out.writeByte((byte) 1);
            out.writeDouble(this.mRepayAmountUSD.doubleValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mRepayLowestUSD != null) {
            out.writeByte((byte) 1);
            out.writeDouble(this.mRepayLowestUSD.doubleValue());
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

    public AEntityHelper<RawBankInfo> getHelper() {
        return RawBankInfoHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.collectencrypt.RawBankInfo";
    }

    public String getDatabaseName() {
        return "dsCollectEncrypt";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("RawBankInfo { mId: ").append(this.mId);
        sb.append(", mTimeStamp: ").append(this.mTimeStamp);
        sb.append(", mBankInfo: ").append(this.mBankInfo);
        sb.append(", mLastNo: ").append(this.mLastNo);
        sb.append(", mRepaymentDate: ").append(this.mRepaymentDate);
        sb.append(", mRepaymentAmountCNY: ").append(this.mRepaymentAmountCNY);
        sb.append(", mRepayLowestCNY: ").append(this.mRepayLowestCNY);
        sb.append(", mRepayAmountUSD: ").append(this.mRepayAmountUSD);
        sb.append(", mRepayLowestUSD: ").append(this.mRepayLowestUSD);
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
