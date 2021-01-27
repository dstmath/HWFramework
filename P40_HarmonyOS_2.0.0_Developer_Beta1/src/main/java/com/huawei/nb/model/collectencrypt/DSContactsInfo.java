package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class DSContactsInfo extends AManagedObject {
    public static final Parcelable.Creator<DSContactsInfo> CREATOR = new Parcelable.Creator<DSContactsInfo>() {
        /* class com.huawei.nb.model.collectencrypt.DSContactsInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public DSContactsInfo createFromParcel(Parcel parcel) {
            return new DSContactsInfo(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public DSContactsInfo[] newArray(int i) {
            return new DSContactsInfo[i];
        }
    };
    private Integer callDialNum;
    private Integer callDurationTime;
    private Integer callRecvNum;
    private Integer contactNum;
    private Integer id;
    private Integer mReservedInt;
    private String mReservedText;
    private Long mTimeStamp = 0L;

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
        return "com.huawei.nb.model.collectencrypt.DSContactsInfo";
    }

    public String getEntityVersion() {
        return "0.0.2";
    }

    public int getEntityVersionCode() {
        return 2;
    }

    public DSContactsInfo(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Long l = null;
        this.id = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.contactNum = cursor.isNull(2) ? null : Integer.valueOf(cursor.getInt(2));
        this.callDialNum = cursor.isNull(3) ? null : Integer.valueOf(cursor.getInt(3));
        this.callRecvNum = cursor.isNull(4) ? null : Integer.valueOf(cursor.getInt(4));
        this.callDurationTime = cursor.isNull(5) ? null : Integer.valueOf(cursor.getInt(5));
        this.mReservedInt = cursor.isNull(6) ? null : Integer.valueOf(cursor.getInt(6));
        this.mReservedText = cursor.getString(7);
        this.mTimeStamp = !cursor.isNull(8) ? Long.valueOf(cursor.getLong(8)) : l;
    }

    public DSContactsInfo(Parcel parcel) {
        super(parcel);
        Long l = null;
        if (parcel.readByte() == 0) {
            this.id = null;
            parcel.readInt();
        } else {
            this.id = Integer.valueOf(parcel.readInt());
        }
        this.contactNum = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.callDialNum = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.callRecvNum = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.callDurationTime = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mReservedInt = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mReservedText = parcel.readByte() == 0 ? null : parcel.readString();
        this.mTimeStamp = parcel.readByte() != 0 ? Long.valueOf(parcel.readLong()) : l;
    }

    private DSContactsInfo(Integer num, Integer num2, Integer num3, Integer num4, Integer num5, Integer num6, String str, Long l) {
        this.id = num;
        this.contactNum = num2;
        this.callDialNum = num3;
        this.callRecvNum = num4;
        this.callDurationTime = num5;
        this.mReservedInt = num6;
        this.mReservedText = str;
        this.mTimeStamp = l;
    }

    public DSContactsInfo() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer num) {
        this.id = num;
        setValue();
    }

    public Integer getContactNum() {
        return this.contactNum;
    }

    public void setContactNum(Integer num) {
        this.contactNum = num;
        setValue();
    }

    public Integer getCallDialNum() {
        return this.callDialNum;
    }

    public void setCallDialNum(Integer num) {
        this.callDialNum = num;
        setValue();
    }

    public Integer getCallRecvNum() {
        return this.callRecvNum;
    }

    public void setCallRecvNum(Integer num) {
        this.callRecvNum = num;
        setValue();
    }

    public Integer getCallDurationTime() {
        return this.callDurationTime;
    }

    public void setCallDurationTime(Integer num) {
        this.callDurationTime = num;
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

    public Long getMTimeStamp() {
        return this.mTimeStamp;
    }

    public void setMTimeStamp(Long l) {
        this.mTimeStamp = l;
        setValue();
    }

    @Override // com.huawei.odmf.core.AManagedObject, android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        if (this.id != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.id.intValue());
        } else {
            parcel.writeByte((byte) 0);
            parcel.writeInt(1);
        }
        if (this.contactNum != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.contactNum.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.callDialNum != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.callDialNum.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.callRecvNum != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.callRecvNum.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.callDurationTime != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.callDurationTime.intValue());
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
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mTimeStamp != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.mTimeStamp.longValue());
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<DSContactsInfo> getHelper() {
        return DSContactsInfoHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "DSContactsInfo { id: " + this.id + ", contactNum: " + this.contactNum + ", callDialNum: " + this.callDialNum + ", callRecvNum: " + this.callRecvNum + ", callDurationTime: " + this.callDurationTime + ", mReservedInt: " + this.mReservedInt + ", mReservedText: " + this.mReservedText + ", mTimeStamp: " + this.mTimeStamp + " }";
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
