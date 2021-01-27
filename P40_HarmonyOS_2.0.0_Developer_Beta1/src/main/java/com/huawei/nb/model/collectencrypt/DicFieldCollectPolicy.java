package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class DicFieldCollectPolicy extends AManagedObject {
    public static final Parcelable.Creator<DicFieldCollectPolicy> CREATOR = new Parcelable.Creator<DicFieldCollectPolicy>() {
        /* class com.huawei.nb.model.collectencrypt.DicFieldCollectPolicy.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public DicFieldCollectPolicy createFromParcel(Parcel parcel) {
            return new DicFieldCollectPolicy(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public DicFieldCollectPolicy[] newArray(int i) {
            return new DicFieldCollectPolicy[i];
        }
    };
    private Integer mCollectMethod;
    private String mFieldName;
    private Integer mId;
    private Integer mReservedInt;
    private String mReservedText;
    private String mTableName;

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
        return "com.huawei.nb.model.collectencrypt.DicFieldCollectPolicy";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public DicFieldCollectPolicy(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Integer num = null;
        this.mId = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.mTableName = cursor.getString(2);
        this.mFieldName = cursor.getString(3);
        this.mCollectMethod = cursor.isNull(4) ? null : Integer.valueOf(cursor.getInt(4));
        this.mReservedInt = !cursor.isNull(5) ? Integer.valueOf(cursor.getInt(5)) : num;
        this.mReservedText = cursor.getString(6);
    }

    public DicFieldCollectPolicy(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.mId = null;
            parcel.readInt();
        } else {
            this.mId = Integer.valueOf(parcel.readInt());
        }
        this.mTableName = parcel.readByte() == 0 ? null : parcel.readString();
        this.mFieldName = parcel.readByte() == 0 ? null : parcel.readString();
        this.mCollectMethod = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mReservedInt = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mReservedText = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private DicFieldCollectPolicy(Integer num, String str, String str2, Integer num2, Integer num3, String str3) {
        this.mId = num;
        this.mTableName = str;
        this.mFieldName = str2;
        this.mCollectMethod = num2;
        this.mReservedInt = num3;
        this.mReservedText = str3;
    }

    public DicFieldCollectPolicy() {
    }

    public Integer getMId() {
        return this.mId;
    }

    public void setMId(Integer num) {
        this.mId = num;
        setValue();
    }

    public String getMTableName() {
        return this.mTableName;
    }

    public void setMTableName(String str) {
        this.mTableName = str;
        setValue();
    }

    public String getMFieldName() {
        return this.mFieldName;
    }

    public void setMFieldName(String str) {
        this.mFieldName = str;
        setValue();
    }

    public Integer getMCollectMethod() {
        return this.mCollectMethod;
    }

    public void setMCollectMethod(Integer num) {
        this.mCollectMethod = num;
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
        if (this.mTableName != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mTableName);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mFieldName != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mFieldName);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mCollectMethod != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mCollectMethod.intValue());
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
    public AEntityHelper<DicFieldCollectPolicy> getHelper() {
        return DicFieldCollectPolicyHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "DicFieldCollectPolicy { mId: " + this.mId + ", mTableName: " + this.mTableName + ", mFieldName: " + this.mFieldName + ", mCollectMethod: " + this.mCollectMethod + ", mReservedInt: " + this.mReservedInt + ", mReservedText: " + this.mReservedText + " }";
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
