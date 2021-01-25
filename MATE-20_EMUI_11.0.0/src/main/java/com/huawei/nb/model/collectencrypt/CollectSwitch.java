package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class CollectSwitch extends AManagedObject {
    public static final Parcelable.Creator<CollectSwitch> CREATOR = new Parcelable.Creator<CollectSwitch>() {
        /* class com.huawei.nb.model.collectencrypt.CollectSwitch.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public CollectSwitch createFromParcel(Parcel parcel) {
            return new CollectSwitch(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public CollectSwitch[] newArray(int i) {
            return new CollectSwitch[i];
        }
    };
    private String mDataName;
    private String mModuleName;
    private Integer mReservedInt;
    private String mReservedText;
    private String mTimeText;

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
        return "com.huawei.nb.model.collectencrypt.CollectSwitch";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public CollectSwitch(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.mDataName = cursor.getString(1);
        this.mModuleName = cursor.getString(2);
        this.mTimeText = cursor.getString(3);
        this.mReservedInt = cursor.isNull(4) ? null : Integer.valueOf(cursor.getInt(4));
        this.mReservedText = cursor.getString(5);
    }

    public CollectSwitch(Parcel parcel) {
        super(parcel);
        String str = null;
        this.mDataName = parcel.readByte() == 0 ? null : parcel.readString();
        this.mModuleName = parcel.readByte() == 0 ? null : parcel.readString();
        this.mTimeText = parcel.readByte() == 0 ? null : parcel.readString();
        this.mReservedInt = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mReservedText = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private CollectSwitch(String str, String str2, String str3, Integer num, String str4) {
        this.mDataName = str;
        this.mModuleName = str2;
        this.mTimeText = str3;
        this.mReservedInt = num;
        this.mReservedText = str4;
    }

    public CollectSwitch() {
    }

    public String getMDataName() {
        return this.mDataName;
    }

    public void setMDataName(String str) {
        this.mDataName = str;
        setValue();
    }

    public String getMModuleName() {
        return this.mModuleName;
    }

    public void setMModuleName(String str) {
        this.mModuleName = str;
        setValue();
    }

    public String getMTimeText() {
        return this.mTimeText;
    }

    public void setMTimeText(String str) {
        this.mTimeText = str;
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
        if (this.mDataName != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mDataName);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mModuleName != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mModuleName);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mTimeText != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mTimeText);
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
    public AEntityHelper<CollectSwitch> getHelper() {
        return CollectSwitchHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "CollectSwitch { mDataName: " + this.mDataName + ", mModuleName: " + this.mModuleName + ", mTimeText: " + this.mTimeText + ", mReservedInt: " + this.mReservedInt + ", mReservedText: " + this.mReservedText + " }";
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
