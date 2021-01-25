package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RawBankInfo extends AManagedObject {
    public static final Parcelable.Creator<RawBankInfo> CREATOR = new Parcelable.Creator<RawBankInfo>() {
        /* class com.huawei.nb.model.collectencrypt.RawBankInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public RawBankInfo createFromParcel(Parcel parcel) {
            return new RawBankInfo(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public RawBankInfo[] newArray(int i) {
            return new RawBankInfo[i];
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
        return "com.huawei.nb.model.collectencrypt.RawBankInfo";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public RawBankInfo(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Integer num = null;
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

    public RawBankInfo(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.mId = null;
            parcel.readInt();
        } else {
            this.mId = Integer.valueOf(parcel.readInt());
        }
        this.mTimeStamp = parcel.readByte() == 0 ? null : new Date(parcel.readLong());
        this.mBankInfo = parcel.readByte() == 0 ? null : parcel.readString();
        this.mLastNo = parcel.readByte() == 0 ? null : parcel.readString();
        this.mRepaymentDate = parcel.readByte() == 0 ? null : new Date(parcel.readLong());
        this.mRepaymentAmountCNY = parcel.readByte() == 0 ? null : Double.valueOf(parcel.readDouble());
        this.mRepayLowestCNY = parcel.readByte() == 0 ? null : Double.valueOf(parcel.readDouble());
        this.mRepayAmountUSD = parcel.readByte() == 0 ? null : Double.valueOf(parcel.readDouble());
        this.mRepayLowestUSD = parcel.readByte() == 0 ? null : Double.valueOf(parcel.readDouble());
        this.mReservedInt = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mReservedText = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private RawBankInfo(Integer num, Date date, String str, String str2, Date date2, Double d, Double d2, Double d3, Double d4, Integer num2, String str3) {
        this.mId = num;
        this.mTimeStamp = date;
        this.mBankInfo = str;
        this.mLastNo = str2;
        this.mRepaymentDate = date2;
        this.mRepaymentAmountCNY = d;
        this.mRepayLowestCNY = d2;
        this.mRepayAmountUSD = d3;
        this.mRepayLowestUSD = d4;
        this.mReservedInt = num2;
        this.mReservedText = str3;
    }

    public RawBankInfo() {
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

    public String getMBankInfo() {
        return this.mBankInfo;
    }

    public void setMBankInfo(String str) {
        this.mBankInfo = str;
        setValue();
    }

    public String getMLastNo() {
        return this.mLastNo;
    }

    public void setMLastNo(String str) {
        this.mLastNo = str;
        setValue();
    }

    public Date getMRepaymentDate() {
        return this.mRepaymentDate;
    }

    public void setMRepaymentDate(Date date) {
        this.mRepaymentDate = date;
        setValue();
    }

    public Double getMRepaymentAmountCNY() {
        return this.mRepaymentAmountCNY;
    }

    public void setMRepaymentAmountCNY(Double d) {
        this.mRepaymentAmountCNY = d;
        setValue();
    }

    public Double getMRepayLowestCNY() {
        return this.mRepayLowestCNY;
    }

    public void setMRepayLowestCNY(Double d) {
        this.mRepayLowestCNY = d;
        setValue();
    }

    public Double getMRepayAmountUSD() {
        return this.mRepayAmountUSD;
    }

    public void setMRepayAmountUSD(Double d) {
        this.mRepayAmountUSD = d;
        setValue();
    }

    public Double getMRepayLowestUSD() {
        return this.mRepayLowestUSD;
    }

    public void setMRepayLowestUSD(Double d) {
        this.mRepayLowestUSD = d;
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
        if (this.mBankInfo != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mBankInfo);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mLastNo != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mLastNo);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mRepaymentDate != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.mRepaymentDate.getTime());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mRepaymentAmountCNY != null) {
            parcel.writeByte((byte) 1);
            parcel.writeDouble(this.mRepaymentAmountCNY.doubleValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mRepayLowestCNY != null) {
            parcel.writeByte((byte) 1);
            parcel.writeDouble(this.mRepayLowestCNY.doubleValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mRepayAmountUSD != null) {
            parcel.writeByte((byte) 1);
            parcel.writeDouble(this.mRepayAmountUSD.doubleValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mRepayLowestUSD != null) {
            parcel.writeByte((byte) 1);
            parcel.writeDouble(this.mRepayLowestUSD.doubleValue());
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
    public AEntityHelper<RawBankInfo> getHelper() {
        return RawBankInfoHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "RawBankInfo { mId: " + this.mId + ", mTimeStamp: " + this.mTimeStamp + ", mBankInfo: " + this.mBankInfo + ", mLastNo: " + this.mLastNo + ", mRepaymentDate: " + this.mRepaymentDate + ", mRepaymentAmountCNY: " + this.mRepaymentAmountCNY + ", mRepayLowestCNY: " + this.mRepayLowestCNY + ", mRepayAmountUSD: " + this.mRepayAmountUSD + ", mRepayLowestUSD: " + this.mRepayLowestUSD + ", mReservedInt: " + this.mReservedInt + ", mReservedText: " + this.mReservedText + " }";
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
