package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class MetaCellRecord extends AManagedObject {
    public static final Parcelable.Creator<MetaCellRecord> CREATOR = new Parcelable.Creator<MetaCellRecord>() {
        /* class com.huawei.nb.model.collectencrypt.MetaCellRecord.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public MetaCellRecord createFromParcel(Parcel parcel) {
            return new MetaCellRecord(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public MetaCellRecord[] newArray(int i) {
            return new MetaCellRecord[i];
        }
    };
    private Integer mCellID1;
    private Integer mCellID2;
    private Integer mCellID3;
    private Integer mCellLAC1;
    private Integer mCellLAC2;
    private Integer mCellLAC3;
    private Integer mCellMCC1;
    private Integer mCellMCC2;
    private Integer mCellMCC3;
    private Integer mCellMNC1;
    private Integer mCellMNC2;
    private Integer mCellMNC3;
    private Integer mCellRSSI1;
    private Integer mCellRSSI2;
    private Integer mCellRSSI3;
    private Integer mId;
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
        return "com.huawei.nb.model.collectencrypt.MetaCellRecord";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public MetaCellRecord(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Integer num = null;
        this.mId = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.mTimeStamp = cursor.isNull(2) ? null : new Date(cursor.getLong(2));
        this.mCellID1 = cursor.isNull(3) ? null : Integer.valueOf(cursor.getInt(3));
        this.mCellMCC1 = cursor.isNull(4) ? null : Integer.valueOf(cursor.getInt(4));
        this.mCellMNC1 = cursor.isNull(5) ? null : Integer.valueOf(cursor.getInt(5));
        this.mCellLAC1 = cursor.isNull(6) ? null : Integer.valueOf(cursor.getInt(6));
        this.mCellRSSI1 = cursor.isNull(7) ? null : Integer.valueOf(cursor.getInt(7));
        this.mCellID2 = cursor.isNull(8) ? null : Integer.valueOf(cursor.getInt(8));
        this.mCellMCC2 = cursor.isNull(9) ? null : Integer.valueOf(cursor.getInt(9));
        this.mCellMNC2 = cursor.isNull(10) ? null : Integer.valueOf(cursor.getInt(10));
        this.mCellLAC2 = cursor.isNull(11) ? null : Integer.valueOf(cursor.getInt(11));
        this.mCellRSSI2 = cursor.isNull(12) ? null : Integer.valueOf(cursor.getInt(12));
        this.mCellID3 = cursor.isNull(13) ? null : Integer.valueOf(cursor.getInt(13));
        this.mCellMCC3 = cursor.isNull(14) ? null : Integer.valueOf(cursor.getInt(14));
        this.mCellMNC3 = cursor.isNull(15) ? null : Integer.valueOf(cursor.getInt(15));
        this.mCellLAC3 = cursor.isNull(16) ? null : Integer.valueOf(cursor.getInt(16));
        this.mCellRSSI3 = cursor.isNull(17) ? null : Integer.valueOf(cursor.getInt(17));
        this.mReservedInt = !cursor.isNull(18) ? Integer.valueOf(cursor.getInt(18)) : num;
        this.mReservedText = cursor.getString(19);
    }

    public MetaCellRecord(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.mId = null;
            parcel.readInt();
        } else {
            this.mId = Integer.valueOf(parcel.readInt());
        }
        this.mTimeStamp = parcel.readByte() == 0 ? null : new Date(parcel.readLong());
        this.mCellID1 = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mCellMCC1 = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mCellMNC1 = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mCellLAC1 = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mCellRSSI1 = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mCellID2 = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mCellMCC2 = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mCellMNC2 = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mCellLAC2 = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mCellRSSI2 = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mCellID3 = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mCellMCC3 = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mCellMNC3 = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mCellLAC3 = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mCellRSSI3 = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mReservedInt = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mReservedText = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private MetaCellRecord(Integer num, Date date, Integer num2, Integer num3, Integer num4, Integer num5, Integer num6, Integer num7, Integer num8, Integer num9, Integer num10, Integer num11, Integer num12, Integer num13, Integer num14, Integer num15, Integer num16, Integer num17, String str) {
        this.mId = num;
        this.mTimeStamp = date;
        this.mCellID1 = num2;
        this.mCellMCC1 = num3;
        this.mCellMNC1 = num4;
        this.mCellLAC1 = num5;
        this.mCellRSSI1 = num6;
        this.mCellID2 = num7;
        this.mCellMCC2 = num8;
        this.mCellMNC2 = num9;
        this.mCellLAC2 = num10;
        this.mCellRSSI2 = num11;
        this.mCellID3 = num12;
        this.mCellMCC3 = num13;
        this.mCellMNC3 = num14;
        this.mCellLAC3 = num15;
        this.mCellRSSI3 = num16;
        this.mReservedInt = num17;
        this.mReservedText = str;
    }

    public MetaCellRecord() {
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

    public Integer getMCellID1() {
        return this.mCellID1;
    }

    public void setMCellID1(Integer num) {
        this.mCellID1 = num;
        setValue();
    }

    public Integer getMCellMCC1() {
        return this.mCellMCC1;
    }

    public void setMCellMCC1(Integer num) {
        this.mCellMCC1 = num;
        setValue();
    }

    public Integer getMCellMNC1() {
        return this.mCellMNC1;
    }

    public void setMCellMNC1(Integer num) {
        this.mCellMNC1 = num;
        setValue();
    }

    public Integer getMCellLAC1() {
        return this.mCellLAC1;
    }

    public void setMCellLAC1(Integer num) {
        this.mCellLAC1 = num;
        setValue();
    }

    public Integer getMCellRSSI1() {
        return this.mCellRSSI1;
    }

    public void setMCellRSSI1(Integer num) {
        this.mCellRSSI1 = num;
        setValue();
    }

    public Integer getMCellID2() {
        return this.mCellID2;
    }

    public void setMCellID2(Integer num) {
        this.mCellID2 = num;
        setValue();
    }

    public Integer getMCellMCC2() {
        return this.mCellMCC2;
    }

    public void setMCellMCC2(Integer num) {
        this.mCellMCC2 = num;
        setValue();
    }

    public Integer getMCellMNC2() {
        return this.mCellMNC2;
    }

    public void setMCellMNC2(Integer num) {
        this.mCellMNC2 = num;
        setValue();
    }

    public Integer getMCellLAC2() {
        return this.mCellLAC2;
    }

    public void setMCellLAC2(Integer num) {
        this.mCellLAC2 = num;
        setValue();
    }

    public Integer getMCellRSSI2() {
        return this.mCellRSSI2;
    }

    public void setMCellRSSI2(Integer num) {
        this.mCellRSSI2 = num;
        setValue();
    }

    public Integer getMCellID3() {
        return this.mCellID3;
    }

    public void setMCellID3(Integer num) {
        this.mCellID3 = num;
        setValue();
    }

    public Integer getMCellMCC3() {
        return this.mCellMCC3;
    }

    public void setMCellMCC3(Integer num) {
        this.mCellMCC3 = num;
        setValue();
    }

    public Integer getMCellMNC3() {
        return this.mCellMNC3;
    }

    public void setMCellMNC3(Integer num) {
        this.mCellMNC3 = num;
        setValue();
    }

    public Integer getMCellLAC3() {
        return this.mCellLAC3;
    }

    public void setMCellLAC3(Integer num) {
        this.mCellLAC3 = num;
        setValue();
    }

    public Integer getMCellRSSI3() {
        return this.mCellRSSI3;
    }

    public void setMCellRSSI3(Integer num) {
        this.mCellRSSI3 = num;
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
        if (this.mCellID1 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mCellID1.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mCellMCC1 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mCellMCC1.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mCellMNC1 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mCellMNC1.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mCellLAC1 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mCellLAC1.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mCellRSSI1 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mCellRSSI1.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mCellID2 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mCellID2.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mCellMCC2 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mCellMCC2.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mCellMNC2 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mCellMNC2.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mCellLAC2 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mCellLAC2.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mCellRSSI2 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mCellRSSI2.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mCellID3 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mCellID3.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mCellMCC3 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mCellMCC3.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mCellMNC3 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mCellMNC3.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mCellLAC3 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mCellLAC3.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mCellRSSI3 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mCellRSSI3.intValue());
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
    public AEntityHelper<MetaCellRecord> getHelper() {
        return MetaCellRecordHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "MetaCellRecord { mId: " + this.mId + ", mTimeStamp: " + this.mTimeStamp + ", mCellID1: " + this.mCellID1 + ", mCellMCC1: " + this.mCellMCC1 + ", mCellMNC1: " + this.mCellMNC1 + ", mCellLAC1: " + this.mCellLAC1 + ", mCellRSSI1: " + this.mCellRSSI1 + ", mCellID2: " + this.mCellID2 + ", mCellMCC2: " + this.mCellMCC2 + ", mCellMNC2: " + this.mCellMNC2 + ", mCellLAC2: " + this.mCellLAC2 + ", mCellRSSI2: " + this.mCellRSSI2 + ", mCellID3: " + this.mCellID3 + ", mCellMCC3: " + this.mCellMCC3 + ", mCellMNC3: " + this.mCellMNC3 + ", mCellLAC3: " + this.mCellLAC3 + ", mCellRSSI3: " + this.mCellRSSI3 + ", mReservedInt: " + this.mReservedInt + ", mReservedText: " + this.mReservedText + " }";
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
