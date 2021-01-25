package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class DicEventPolicy extends AManagedObject {
    public static final Parcelable.Creator<DicEventPolicy> CREATOR = new Parcelable.Creator<DicEventPolicy>() {
        /* class com.huawei.nb.model.collectencrypt.DicEventPolicy.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public DicEventPolicy createFromParcel(Parcel parcel) {
            return new DicEventPolicy(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public DicEventPolicy[] newArray(int i) {
            return new DicEventPolicy[i];
        }
    };
    private Integer mColdDownTime;
    private String mEventDesc;
    private String mEventName;
    private Integer mEventType;
    private Integer mId;
    private Integer mMaxRecordOneday;
    private Integer mReservedInt;
    private String mReservedText;

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
        return "com.huawei.nb.model.collectencrypt.DicEventPolicy";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public DicEventPolicy(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Integer num = null;
        this.mId = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.mEventType = cursor.isNull(2) ? null : Integer.valueOf(cursor.getInt(2));
        this.mEventName = cursor.getString(3);
        this.mEventDesc = cursor.getString(4);
        this.mColdDownTime = cursor.isNull(5) ? null : Integer.valueOf(cursor.getInt(5));
        this.mMaxRecordOneday = cursor.isNull(6) ? null : Integer.valueOf(cursor.getInt(6));
        this.mReservedInt = !cursor.isNull(7) ? Integer.valueOf(cursor.getInt(7)) : num;
        this.mReservedText = cursor.getString(8);
    }

    public DicEventPolicy(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.mId = null;
            parcel.readInt();
        } else {
            this.mId = Integer.valueOf(parcel.readInt());
        }
        this.mEventType = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mEventName = parcel.readByte() == 0 ? null : parcel.readString();
        this.mEventDesc = parcel.readByte() == 0 ? null : parcel.readString();
        this.mColdDownTime = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mMaxRecordOneday = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mReservedInt = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mReservedText = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private DicEventPolicy(Integer num, Integer num2, String str, String str2, Integer num3, Integer num4, Integer num5, String str3) {
        this.mId = num;
        this.mEventType = num2;
        this.mEventName = str;
        this.mEventDesc = str2;
        this.mColdDownTime = num3;
        this.mMaxRecordOneday = num4;
        this.mReservedInt = num5;
        this.mReservedText = str3;
    }

    public DicEventPolicy() {
    }

    public Integer getMId() {
        return this.mId;
    }

    public void setMId(Integer num) {
        this.mId = num;
        setValue();
    }

    public Integer getMEventType() {
        return this.mEventType;
    }

    public void setMEventType(Integer num) {
        this.mEventType = num;
        setValue();
    }

    public String getMEventName() {
        return this.mEventName;
    }

    public void setMEventName(String str) {
        this.mEventName = str;
        setValue();
    }

    public String getMEventDesc() {
        return this.mEventDesc;
    }

    public void setMEventDesc(String str) {
        this.mEventDesc = str;
        setValue();
    }

    public Integer getMColdDownTime() {
        return this.mColdDownTime;
    }

    public void setMColdDownTime(Integer num) {
        this.mColdDownTime = num;
        setValue();
    }

    public Integer getMMaxRecordOneday() {
        return this.mMaxRecordOneday;
    }

    public void setMMaxRecordOneday(Integer num) {
        this.mMaxRecordOneday = num;
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
        if (this.mEventType != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mEventType.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mEventName != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mEventName);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mEventDesc != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mEventDesc);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mColdDownTime != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mColdDownTime.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mMaxRecordOneday != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mMaxRecordOneday.intValue());
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
    public AEntityHelper<DicEventPolicy> getHelper() {
        return DicEventPolicyHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "DicEventPolicy { mId: " + this.mId + ", mEventType: " + this.mEventType + ", mEventName: " + this.mEventName + ", mEventDesc: " + this.mEventDesc + ", mColdDownTime: " + this.mColdDownTime + ", mMaxRecordOneday: " + this.mMaxRecordOneday + ", mReservedInt: " + this.mReservedInt + ", mReservedText: " + this.mReservedText + " }";
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
