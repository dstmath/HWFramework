package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RawTrainFlightTickInfo extends AManagedObject {
    public static final Parcelable.Creator<RawTrainFlightTickInfo> CREATOR = new Parcelable.Creator<RawTrainFlightTickInfo>() {
        /* class com.huawei.nb.model.collectencrypt.RawTrainFlightTickInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public RawTrainFlightTickInfo createFromParcel(Parcel parcel) {
            return new RawTrainFlightTickInfo(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public RawTrainFlightTickInfo[] newArray(int i) {
            return new RawTrainFlightTickInfo[i];
        }
    };
    private Integer mId;
    private String mPassengerName;
    private Integer mReservedInt;
    private String mReservedText;
    private String mSeatNo;
    private Date mTimeStamp;
    private String mTrainFlightArrivalPlace;
    private Date mTrainFlightArrivalTime;
    private String mTrainFlightNo;
    private String mTrainFlightStartPlace;
    private Date mTrainFlightStartTime;

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
        return "com.huawei.nb.model.collectencrypt.RawTrainFlightTickInfo";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public RawTrainFlightTickInfo(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Integer num = null;
        this.mId = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.mTimeStamp = cursor.isNull(2) ? null : new Date(cursor.getLong(2));
        this.mPassengerName = cursor.getString(3);
        this.mTrainFlightNo = cursor.getString(4);
        this.mSeatNo = cursor.getString(5);
        this.mTrainFlightStartTime = cursor.isNull(6) ? null : new Date(cursor.getLong(6));
        this.mTrainFlightArrivalTime = cursor.isNull(7) ? null : new Date(cursor.getLong(7));
        this.mTrainFlightStartPlace = cursor.getString(8);
        this.mTrainFlightArrivalPlace = cursor.getString(9);
        this.mReservedInt = !cursor.isNull(10) ? Integer.valueOf(cursor.getInt(10)) : num;
        this.mReservedText = cursor.getString(11);
    }

    public RawTrainFlightTickInfo(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.mId = null;
            parcel.readInt();
        } else {
            this.mId = Integer.valueOf(parcel.readInt());
        }
        this.mTimeStamp = parcel.readByte() == 0 ? null : new Date(parcel.readLong());
        this.mPassengerName = parcel.readByte() == 0 ? null : parcel.readString();
        this.mTrainFlightNo = parcel.readByte() == 0 ? null : parcel.readString();
        this.mSeatNo = parcel.readByte() == 0 ? null : parcel.readString();
        this.mTrainFlightStartTime = parcel.readByte() == 0 ? null : new Date(parcel.readLong());
        this.mTrainFlightArrivalTime = parcel.readByte() == 0 ? null : new Date(parcel.readLong());
        this.mTrainFlightStartPlace = parcel.readByte() == 0 ? null : parcel.readString();
        this.mTrainFlightArrivalPlace = parcel.readByte() == 0 ? null : parcel.readString();
        this.mReservedInt = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mReservedText = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private RawTrainFlightTickInfo(Integer num, Date date, String str, String str2, String str3, Date date2, Date date3, String str4, String str5, Integer num2, String str6) {
        this.mId = num;
        this.mTimeStamp = date;
        this.mPassengerName = str;
        this.mTrainFlightNo = str2;
        this.mSeatNo = str3;
        this.mTrainFlightStartTime = date2;
        this.mTrainFlightArrivalTime = date3;
        this.mTrainFlightStartPlace = str4;
        this.mTrainFlightArrivalPlace = str5;
        this.mReservedInt = num2;
        this.mReservedText = str6;
    }

    public RawTrainFlightTickInfo() {
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

    public String getMPassengerName() {
        return this.mPassengerName;
    }

    public void setMPassengerName(String str) {
        this.mPassengerName = str;
        setValue();
    }

    public String getMTrainFlightNo() {
        return this.mTrainFlightNo;
    }

    public void setMTrainFlightNo(String str) {
        this.mTrainFlightNo = str;
        setValue();
    }

    public String getMSeatNo() {
        return this.mSeatNo;
    }

    public void setMSeatNo(String str) {
        this.mSeatNo = str;
        setValue();
    }

    public Date getMTrainFlightStartTime() {
        return this.mTrainFlightStartTime;
    }

    public void setMTrainFlightStartTime(Date date) {
        this.mTrainFlightStartTime = date;
        setValue();
    }

    public Date getMTrainFlightArrivalTime() {
        return this.mTrainFlightArrivalTime;
    }

    public void setMTrainFlightArrivalTime(Date date) {
        this.mTrainFlightArrivalTime = date;
        setValue();
    }

    public String getMTrainFlightStartPlace() {
        return this.mTrainFlightStartPlace;
    }

    public void setMTrainFlightStartPlace(String str) {
        this.mTrainFlightStartPlace = str;
        setValue();
    }

    public String getMTrainFlightArrivalPlace() {
        return this.mTrainFlightArrivalPlace;
    }

    public void setMTrainFlightArrivalPlace(String str) {
        this.mTrainFlightArrivalPlace = str;
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
        if (this.mPassengerName != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mPassengerName);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mTrainFlightNo != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mTrainFlightNo);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mSeatNo != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mSeatNo);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mTrainFlightStartTime != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.mTrainFlightStartTime.getTime());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mTrainFlightArrivalTime != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.mTrainFlightArrivalTime.getTime());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mTrainFlightStartPlace != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mTrainFlightStartPlace);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mTrainFlightArrivalPlace != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mTrainFlightArrivalPlace);
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
    public AEntityHelper<RawTrainFlightTickInfo> getHelper() {
        return RawTrainFlightTickInfoHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "RawTrainFlightTickInfo { mId: " + this.mId + ", mTimeStamp: " + this.mTimeStamp + ", mPassengerName: " + this.mPassengerName + ", mTrainFlightNo: " + this.mTrainFlightNo + ", mSeatNo: " + this.mSeatNo + ", mTrainFlightStartTime: " + this.mTrainFlightStartTime + ", mTrainFlightArrivalTime: " + this.mTrainFlightArrivalTime + ", mTrainFlightStartPlace: " + this.mTrainFlightStartPlace + ", mTrainFlightArrivalPlace: " + this.mTrainFlightArrivalPlace + ", mReservedInt: " + this.mReservedInt + ", mReservedText: " + this.mReservedText + " }";
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
