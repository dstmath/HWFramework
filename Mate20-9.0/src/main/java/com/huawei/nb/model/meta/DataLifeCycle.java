package com.huawei.nb.model.meta;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class DataLifeCycle extends AManagedObject {
    public static final Parcelable.Creator<DataLifeCycle> CREATOR = new Parcelable.Creator<DataLifeCycle>() {
        public DataLifeCycle createFromParcel(Parcel in) {
            return new DataLifeCycle(in);
        }

        public DataLifeCycle[] newArray(int size) {
            return new DataLifeCycle[size];
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

    public DataLifeCycle(Cursor cursor) {
        Integer num = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
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

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public DataLifeCycle(Parcel in) {
        super(in);
        Integer num = null;
        if (in.readByte() == 0) {
            this.mId = null;
            in.readInt();
        } else {
            this.mId = Integer.valueOf(in.readInt());
        }
        this.mDBName = in.readByte() == 0 ? null : in.readString();
        this.mTableName = in.readByte() == 0 ? null : in.readString();
        this.mMode = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mFieldName = in.readByte() == 0 ? null : in.readString();
        this.mCount = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mDBRekeyTime = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.mThreshold = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mUnit = in.readByte() != 0 ? Integer.valueOf(in.readInt()) : num;
    }

    private DataLifeCycle(Integer mId2, String mDBName2, String mTableName2, Integer mMode2, String mFieldName2, Integer mCount2, Long mDBRekeyTime2, Integer mThreshold2, Integer mUnit2) {
        this.mId = mId2;
        this.mDBName = mDBName2;
        this.mTableName = mTableName2;
        this.mMode = mMode2;
        this.mFieldName = mFieldName2;
        this.mCount = mCount2;
        this.mDBRekeyTime = mDBRekeyTime2;
        this.mThreshold = mThreshold2;
        this.mUnit = mUnit2;
    }

    public DataLifeCycle() {
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

    public String getMDBName() {
        return this.mDBName;
    }

    public void setMDBName(String mDBName2) {
        this.mDBName = mDBName2;
        setValue();
    }

    public String getMTableName() {
        return this.mTableName;
    }

    public void setMTableName(String mTableName2) {
        this.mTableName = mTableName2;
        setValue();
    }

    public Integer getMMode() {
        return this.mMode;
    }

    public void setMMode(Integer mMode2) {
        this.mMode = mMode2;
        setValue();
    }

    public String getMFieldName() {
        return this.mFieldName;
    }

    public void setMFieldName(String mFieldName2) {
        this.mFieldName = mFieldName2;
        setValue();
    }

    public Integer getMCount() {
        return this.mCount;
    }

    public void setMCount(Integer mCount2) {
        this.mCount = mCount2;
        setValue();
    }

    public Long getMDBRekeyTime() {
        return this.mDBRekeyTime;
    }

    public void setMDBRekeyTime(Long mDBRekeyTime2) {
        this.mDBRekeyTime = mDBRekeyTime2;
        setValue();
    }

    public Integer getMThreshold() {
        return this.mThreshold;
    }

    public void setMThreshold(Integer mThreshold2) {
        this.mThreshold = mThreshold2;
        setValue();
    }

    public Integer getMUnit() {
        return this.mUnit;
    }

    public void setMUnit(Integer mUnit2) {
        this.mUnit = mUnit2;
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
        if (this.mDBName != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mDBName);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mTableName != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mTableName);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mMode != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mMode.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mFieldName != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mFieldName);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mCount != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mCount.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mDBRekeyTime != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.mDBRekeyTime.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mThreshold != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mThreshold.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mUnit != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mUnit.intValue());
            return;
        }
        out.writeByte((byte) 0);
    }

    public AEntityHelper<DataLifeCycle> getHelper() {
        return DataLifeCycleHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.meta.DataLifeCycle";
    }

    public String getDatabaseName() {
        return "dsMeta";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("DataLifeCycle { mId: ").append(this.mId);
        sb.append(", mDBName: ").append(this.mDBName);
        sb.append(", mTableName: ").append(this.mTableName);
        sb.append(", mMode: ").append(this.mMode);
        sb.append(", mFieldName: ").append(this.mFieldName);
        sb.append(", mCount: ").append(this.mCount);
        sb.append(", mDBRekeyTime: ").append(this.mDBRekeyTime);
        sb.append(", mThreshold: ").append(this.mThreshold);
        sb.append(", mUnit: ").append(this.mUnit);
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
        return "0.0.16";
    }

    public int getDatabaseVersionCode() {
        return 16;
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }
}
