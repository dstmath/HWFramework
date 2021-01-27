package com.huawei.nb.model.meta;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class DataLifeCycle extends AManagedObject {
    public static final Parcelable.Creator<DataLifeCycle> CREATOR = new Parcelable.Creator<DataLifeCycle>() {
        /* class com.huawei.nb.model.meta.DataLifeCycle.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public DataLifeCycle createFromParcel(Parcel parcel) {
            return new DataLifeCycle(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public DataLifeCycle[] newArray(int i) {
            return new DataLifeCycle[i];
        }
    };
    private Integer mCount;
    private String mDBName;
    private Long mDBRekeyTime;
    private String mFieldName;
    private Integer mId;
    private Integer mMode;
    private String mTableName;
    private Integer mThreshold;
    private Integer mUnit;

    @Override // com.huawei.odmf.core.AManagedObject, android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getDatabaseName() {
        return "dsMeta";
    }

    public String getDatabaseVersion() {
        return "0.0.16";
    }

    public int getDatabaseVersionCode() {
        return 16;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getEntityName() {
        return "com.huawei.nb.model.meta.DataLifeCycle";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public DataLifeCycle(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Integer num = null;
        this.mId = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.mDBName = cursor.getString(2);
        this.mTableName = cursor.getString(3);
        this.mMode = cursor.isNull(4) ? null : Integer.valueOf(cursor.getInt(4));
        this.mFieldName = cursor.getString(5);
        this.mCount = cursor.isNull(6) ? null : Integer.valueOf(cursor.getInt(6));
        this.mDBRekeyTime = cursor.isNull(7) ? null : Long.valueOf(cursor.getLong(7));
        this.mThreshold = cursor.isNull(8) ? null : Integer.valueOf(cursor.getInt(8));
        this.mUnit = !cursor.isNull(9) ? Integer.valueOf(cursor.getInt(9)) : num;
    }

    public DataLifeCycle(Parcel parcel) {
        super(parcel);
        Integer num = null;
        if (parcel.readByte() == 0) {
            this.mId = null;
            parcel.readInt();
        } else {
            this.mId = Integer.valueOf(parcel.readInt());
        }
        this.mDBName = parcel.readByte() == 0 ? null : parcel.readString();
        this.mTableName = parcel.readByte() == 0 ? null : parcel.readString();
        this.mMode = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mFieldName = parcel.readByte() == 0 ? null : parcel.readString();
        this.mCount = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mDBRekeyTime = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.mThreshold = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mUnit = parcel.readByte() != 0 ? Integer.valueOf(parcel.readInt()) : num;
    }

    private DataLifeCycle(Integer num, String str, String str2, Integer num2, String str3, Integer num3, Long l, Integer num4, Integer num5) {
        this.mId = num;
        this.mDBName = str;
        this.mTableName = str2;
        this.mMode = num2;
        this.mFieldName = str3;
        this.mCount = num3;
        this.mDBRekeyTime = l;
        this.mThreshold = num4;
        this.mUnit = num5;
    }

    public DataLifeCycle() {
    }

    public Integer getMId() {
        return this.mId;
    }

    public void setMId(Integer num) {
        this.mId = num;
        setValue();
    }

    public String getMDBName() {
        return this.mDBName;
    }

    public void setMDBName(String str) {
        this.mDBName = str;
        setValue();
    }

    public String getMTableName() {
        return this.mTableName;
    }

    public void setMTableName(String str) {
        this.mTableName = str;
        setValue();
    }

    public Integer getMMode() {
        return this.mMode;
    }

    public void setMMode(Integer num) {
        this.mMode = num;
        setValue();
    }

    public String getMFieldName() {
        return this.mFieldName;
    }

    public void setMFieldName(String str) {
        this.mFieldName = str;
        setValue();
    }

    public Integer getMCount() {
        return this.mCount;
    }

    public void setMCount(Integer num) {
        this.mCount = num;
        setValue();
    }

    public Long getMDBRekeyTime() {
        return this.mDBRekeyTime;
    }

    public void setMDBRekeyTime(Long l) {
        this.mDBRekeyTime = l;
        setValue();
    }

    public Integer getMThreshold() {
        return this.mThreshold;
    }

    public void setMThreshold(Integer num) {
        this.mThreshold = num;
        setValue();
    }

    public Integer getMUnit() {
        return this.mUnit;
    }

    public void setMUnit(Integer num) {
        this.mUnit = num;
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
        if (this.mDBName != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mDBName);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mTableName != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mTableName);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mMode != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mMode.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mFieldName != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mFieldName);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mCount != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mCount.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mDBRekeyTime != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.mDBRekeyTime.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mThreshold != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mThreshold.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mUnit != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mUnit.intValue());
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<DataLifeCycle> getHelper() {
        return DataLifeCycleHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "DataLifeCycle { mId: " + this.mId + ", mDBName: " + this.mDBName + ", mTableName: " + this.mTableName + ", mMode: " + this.mMode + ", mFieldName: " + this.mFieldName + ", mCount: " + this.mCount + ", mDBRekeyTime: " + this.mDBRekeyTime + ", mThreshold: " + this.mThreshold + ", mUnit: " + this.mUnit + " }";
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
