package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class DicTableCollectPolicy extends AManagedObject {
    public static final Parcelable.Creator<DicTableCollectPolicy> CREATOR = new Parcelable.Creator<DicTableCollectPolicy>() {
        /* class com.huawei.nb.model.collectencrypt.DicTableCollectPolicy.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public DicTableCollectPolicy createFromParcel(Parcel parcel) {
            return new DicTableCollectPolicy(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public DicTableCollectPolicy[] newArray(int i) {
            return new DicTableCollectPolicy[i];
        }
    };
    private Integer mColdDownTime;
    private Integer mId;
    private Integer mMaxRecordOneday;
    private Integer mReservedInt;
    private String mReservedText;
    private String mTblName;
    private Integer mTblType;
    private String mTriggerPolicy;

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
        return "com.huawei.nb.model.collectencrypt.DicTableCollectPolicy";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public DicTableCollectPolicy(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Integer num = null;
        this.mId = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.mTblName = cursor.getString(2);
        this.mTblType = cursor.isNull(3) ? null : Integer.valueOf(cursor.getInt(3));
        this.mTriggerPolicy = cursor.getString(4);
        this.mMaxRecordOneday = cursor.isNull(5) ? null : Integer.valueOf(cursor.getInt(5));
        this.mColdDownTime = cursor.isNull(6) ? null : Integer.valueOf(cursor.getInt(6));
        this.mReservedInt = !cursor.isNull(7) ? Integer.valueOf(cursor.getInt(7)) : num;
        this.mReservedText = cursor.getString(8);
    }

    public DicTableCollectPolicy(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.mId = null;
            parcel.readInt();
        } else {
            this.mId = Integer.valueOf(parcel.readInt());
        }
        this.mTblName = parcel.readByte() == 0 ? null : parcel.readString();
        this.mTblType = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mTriggerPolicy = parcel.readByte() == 0 ? null : parcel.readString();
        this.mMaxRecordOneday = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mColdDownTime = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mReservedInt = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mReservedText = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private DicTableCollectPolicy(Integer num, String str, Integer num2, String str2, Integer num3, Integer num4, Integer num5, String str3) {
        this.mId = num;
        this.mTblName = str;
        this.mTblType = num2;
        this.mTriggerPolicy = str2;
        this.mMaxRecordOneday = num3;
        this.mColdDownTime = num4;
        this.mReservedInt = num5;
        this.mReservedText = str3;
    }

    public DicTableCollectPolicy() {
    }

    public Integer getMId() {
        return this.mId;
    }

    public void setMId(Integer num) {
        this.mId = num;
        setValue();
    }

    public String getMTblName() {
        return this.mTblName;
    }

    public void setMTblName(String str) {
        this.mTblName = str;
        setValue();
    }

    public Integer getMTblType() {
        return this.mTblType;
    }

    public void setMTblType(Integer num) {
        this.mTblType = num;
        setValue();
    }

    public String getMTriggerPolicy() {
        return this.mTriggerPolicy;
    }

    public void setMTriggerPolicy(String str) {
        this.mTriggerPolicy = str;
        setValue();
    }

    public Integer getMMaxRecordOneday() {
        return this.mMaxRecordOneday;
    }

    public void setMMaxRecordOneday(Integer num) {
        this.mMaxRecordOneday = num;
        setValue();
    }

    public Integer getMColdDownTime() {
        return this.mColdDownTime;
    }

    public void setMColdDownTime(Integer num) {
        this.mColdDownTime = num;
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
        if (this.mTblName != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mTblName);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mTblType != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mTblType.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mTriggerPolicy != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mTriggerPolicy);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mMaxRecordOneday != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mMaxRecordOneday.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mColdDownTime != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mColdDownTime.intValue());
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
    public AEntityHelper<DicTableCollectPolicy> getHelper() {
        return DicTableCollectPolicyHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "DicTableCollectPolicy { mId: " + this.mId + ", mTblName: " + this.mTblName + ", mTblType: " + this.mTblType + ", mTriggerPolicy: " + this.mTriggerPolicy + ", mMaxRecordOneday: " + this.mMaxRecordOneday + ", mColdDownTime: " + this.mColdDownTime + ", mReservedInt: " + this.mReservedInt + ", mReservedText: " + this.mReservedText + " }";
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
