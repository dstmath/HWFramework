package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class MetaCellRecord extends AManagedObject {
    public static final Parcelable.Creator<MetaCellRecord> CREATOR = new Parcelable.Creator<MetaCellRecord>() {
        public MetaCellRecord createFromParcel(Parcel in) {
            return new MetaCellRecord(in);
        }

        public MetaCellRecord[] newArray(int size) {
            return new MetaCellRecord[size];
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

    public MetaCellRecord(Cursor cursor) {
        Integer num = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
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

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public MetaCellRecord(Parcel in) {
        super(in);
        String str = null;
        if (in.readByte() == 0) {
            this.mId = null;
            in.readInt();
        } else {
            this.mId = Integer.valueOf(in.readInt());
        }
        this.mTimeStamp = in.readByte() == 0 ? null : new Date(in.readLong());
        this.mCellID1 = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mCellMCC1 = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mCellMNC1 = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mCellLAC1 = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mCellRSSI1 = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mCellID2 = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mCellMCC2 = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mCellMNC2 = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mCellLAC2 = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mCellRSSI2 = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mCellID3 = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mCellMCC3 = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mCellMNC3 = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mCellLAC3 = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mCellRSSI3 = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mReservedInt = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mReservedText = in.readByte() != 0 ? in.readString() : str;
    }

    private MetaCellRecord(Integer mId2, Date mTimeStamp2, Integer mCellID12, Integer mCellMCC12, Integer mCellMNC12, Integer mCellLAC12, Integer mCellRSSI12, Integer mCellID22, Integer mCellMCC22, Integer mCellMNC22, Integer mCellLAC22, Integer mCellRSSI22, Integer mCellID32, Integer mCellMCC32, Integer mCellMNC32, Integer mCellLAC32, Integer mCellRSSI32, Integer mReservedInt2, String mReservedText2) {
        this.mId = mId2;
        this.mTimeStamp = mTimeStamp2;
        this.mCellID1 = mCellID12;
        this.mCellMCC1 = mCellMCC12;
        this.mCellMNC1 = mCellMNC12;
        this.mCellLAC1 = mCellLAC12;
        this.mCellRSSI1 = mCellRSSI12;
        this.mCellID2 = mCellID22;
        this.mCellMCC2 = mCellMCC22;
        this.mCellMNC2 = mCellMNC22;
        this.mCellLAC2 = mCellLAC22;
        this.mCellRSSI2 = mCellRSSI22;
        this.mCellID3 = mCellID32;
        this.mCellMCC3 = mCellMCC32;
        this.mCellMNC3 = mCellMNC32;
        this.mCellLAC3 = mCellLAC32;
        this.mCellRSSI3 = mCellRSSI32;
        this.mReservedInt = mReservedInt2;
        this.mReservedText = mReservedText2;
    }

    public MetaCellRecord() {
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

    public Date getMTimeStamp() {
        return this.mTimeStamp;
    }

    public void setMTimeStamp(Date mTimeStamp2) {
        this.mTimeStamp = mTimeStamp2;
        setValue();
    }

    public Integer getMCellID1() {
        return this.mCellID1;
    }

    public void setMCellID1(Integer mCellID12) {
        this.mCellID1 = mCellID12;
        setValue();
    }

    public Integer getMCellMCC1() {
        return this.mCellMCC1;
    }

    public void setMCellMCC1(Integer mCellMCC12) {
        this.mCellMCC1 = mCellMCC12;
        setValue();
    }

    public Integer getMCellMNC1() {
        return this.mCellMNC1;
    }

    public void setMCellMNC1(Integer mCellMNC12) {
        this.mCellMNC1 = mCellMNC12;
        setValue();
    }

    public Integer getMCellLAC1() {
        return this.mCellLAC1;
    }

    public void setMCellLAC1(Integer mCellLAC12) {
        this.mCellLAC1 = mCellLAC12;
        setValue();
    }

    public Integer getMCellRSSI1() {
        return this.mCellRSSI1;
    }

    public void setMCellRSSI1(Integer mCellRSSI12) {
        this.mCellRSSI1 = mCellRSSI12;
        setValue();
    }

    public Integer getMCellID2() {
        return this.mCellID2;
    }

    public void setMCellID2(Integer mCellID22) {
        this.mCellID2 = mCellID22;
        setValue();
    }

    public Integer getMCellMCC2() {
        return this.mCellMCC2;
    }

    public void setMCellMCC2(Integer mCellMCC22) {
        this.mCellMCC2 = mCellMCC22;
        setValue();
    }

    public Integer getMCellMNC2() {
        return this.mCellMNC2;
    }

    public void setMCellMNC2(Integer mCellMNC22) {
        this.mCellMNC2 = mCellMNC22;
        setValue();
    }

    public Integer getMCellLAC2() {
        return this.mCellLAC2;
    }

    public void setMCellLAC2(Integer mCellLAC22) {
        this.mCellLAC2 = mCellLAC22;
        setValue();
    }

    public Integer getMCellRSSI2() {
        return this.mCellRSSI2;
    }

    public void setMCellRSSI2(Integer mCellRSSI22) {
        this.mCellRSSI2 = mCellRSSI22;
        setValue();
    }

    public Integer getMCellID3() {
        return this.mCellID3;
    }

    public void setMCellID3(Integer mCellID32) {
        this.mCellID3 = mCellID32;
        setValue();
    }

    public Integer getMCellMCC3() {
        return this.mCellMCC3;
    }

    public void setMCellMCC3(Integer mCellMCC32) {
        this.mCellMCC3 = mCellMCC32;
        setValue();
    }

    public Integer getMCellMNC3() {
        return this.mCellMNC3;
    }

    public void setMCellMNC3(Integer mCellMNC32) {
        this.mCellMNC3 = mCellMNC32;
        setValue();
    }

    public Integer getMCellLAC3() {
        return this.mCellLAC3;
    }

    public void setMCellLAC3(Integer mCellLAC32) {
        this.mCellLAC3 = mCellLAC32;
        setValue();
    }

    public Integer getMCellRSSI3() {
        return this.mCellRSSI3;
    }

    public void setMCellRSSI3(Integer mCellRSSI32) {
        this.mCellRSSI3 = mCellRSSI32;
        setValue();
    }

    public Integer getMReservedInt() {
        return this.mReservedInt;
    }

    public void setMReservedInt(Integer mReservedInt2) {
        this.mReservedInt = mReservedInt2;
        setValue();
    }

    public String getMReservedText() {
        return this.mReservedText;
    }

    public void setMReservedText(String mReservedText2) {
        this.mReservedText = mReservedText2;
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
        if (this.mTimeStamp != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.mTimeStamp.getTime());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mCellID1 != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mCellID1.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mCellMCC1 != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mCellMCC1.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mCellMNC1 != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mCellMNC1.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mCellLAC1 != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mCellLAC1.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mCellRSSI1 != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mCellRSSI1.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mCellID2 != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mCellID2.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mCellMCC2 != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mCellMCC2.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mCellMNC2 != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mCellMNC2.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mCellLAC2 != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mCellLAC2.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mCellRSSI2 != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mCellRSSI2.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mCellID3 != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mCellID3.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mCellMCC3 != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mCellMCC3.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mCellMNC3 != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mCellMNC3.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mCellLAC3 != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mCellLAC3.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mCellRSSI3 != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mCellRSSI3.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mReservedInt != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mReservedInt.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mReservedText != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mReservedText);
            return;
        }
        out.writeByte((byte) 0);
    }

    public AEntityHelper<MetaCellRecord> getHelper() {
        return MetaCellRecordHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.collectencrypt.MetaCellRecord";
    }

    public String getDatabaseName() {
        return "dsCollectEncrypt";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("MetaCellRecord { mId: ").append(this.mId);
        sb.append(", mTimeStamp: ").append(this.mTimeStamp);
        sb.append(", mCellID1: ").append(this.mCellID1);
        sb.append(", mCellMCC1: ").append(this.mCellMCC1);
        sb.append(", mCellMNC1: ").append(this.mCellMNC1);
        sb.append(", mCellLAC1: ").append(this.mCellLAC1);
        sb.append(", mCellRSSI1: ").append(this.mCellRSSI1);
        sb.append(", mCellID2: ").append(this.mCellID2);
        sb.append(", mCellMCC2: ").append(this.mCellMCC2);
        sb.append(", mCellMNC2: ").append(this.mCellMNC2);
        sb.append(", mCellLAC2: ").append(this.mCellLAC2);
        sb.append(", mCellRSSI2: ").append(this.mCellRSSI2);
        sb.append(", mCellID3: ").append(this.mCellID3);
        sb.append(", mCellMCC3: ").append(this.mCellMCC3);
        sb.append(", mCellMNC3: ").append(this.mCellMNC3);
        sb.append(", mCellLAC3: ").append(this.mCellLAC3);
        sb.append(", mCellRSSI3: ").append(this.mCellRSSI3);
        sb.append(", mReservedInt: ").append(this.mReservedInt);
        sb.append(", mReservedText: ").append(this.mReservedText);
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
        return "0.0.14";
    }

    public int getDatabaseVersionCode() {
        return 14;
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }
}
