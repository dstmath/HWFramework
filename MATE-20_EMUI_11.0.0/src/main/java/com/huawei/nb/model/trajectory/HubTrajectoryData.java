package com.huawei.nb.model.trajectory;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class HubTrajectoryData extends AManagedObject {
    public static final Parcelable.Creator<HubTrajectoryData> CREATOR = new Parcelable.Creator<HubTrajectoryData>() {
        /* class com.huawei.nb.model.trajectory.HubTrajectoryData.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public HubTrajectoryData createFromParcel(Parcel parcel) {
            return new HubTrajectoryData(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public HubTrajectoryData[] newArray(int i) {
            return new HubTrajectoryData[i];
        }
    };
    private Integer id;
    private Integer mCellID;
    private Integer mCellID1;
    private Integer mCellID2;
    private Character mCellLAC;
    private Character mCellLAC1;
    private Character mCellLAC2;
    private Integer mCellRssi;
    private Integer mCellRssi1;
    private Integer mCellRssi2;
    private Character mMCC;
    private Character mMCC1;
    private Character mMCC2;
    private Character mMNC;
    private Character mMNC1;
    private Character mMNC2;
    private Integer mReserved1;
    private String mReserved2;
    private Date mTimeStamp;

    @Override // com.huawei.odmf.core.AManagedObject, android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getDatabaseName() {
        return "dsServiceMetaData";
    }

    public String getDatabaseVersion() {
        return "0.0.13";
    }

    public int getDatabaseVersionCode() {
        return 13;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getEntityName() {
        return "com.huawei.nb.model.trajectory.HubTrajectoryData";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public HubTrajectoryData(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Integer num = null;
        this.id = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.mTimeStamp = cursor.isNull(2) ? null : new Date(cursor.getLong(2));
        this.mCellID = cursor.isNull(3) ? null : Integer.valueOf(cursor.getInt(3));
        this.mCellLAC = cursor.isNull(4) ? null : Character.valueOf(cursor.getString(4).charAt(0));
        this.mCellRssi = cursor.isNull(5) ? null : Integer.valueOf(cursor.getInt(5));
        this.mMCC = cursor.isNull(6) ? null : Character.valueOf(cursor.getString(6).charAt(0));
        this.mMNC = cursor.isNull(7) ? null : Character.valueOf(cursor.getString(7).charAt(0));
        this.mCellID1 = cursor.isNull(8) ? null : Integer.valueOf(cursor.getInt(8));
        this.mCellLAC1 = cursor.isNull(9) ? null : Character.valueOf(cursor.getString(9).charAt(0));
        this.mCellRssi1 = cursor.isNull(10) ? null : Integer.valueOf(cursor.getInt(10));
        this.mMCC1 = cursor.isNull(11) ? null : Character.valueOf(cursor.getString(11).charAt(0));
        this.mMNC1 = cursor.isNull(12) ? null : Character.valueOf(cursor.getString(12).charAt(0));
        this.mCellID2 = cursor.isNull(13) ? null : Integer.valueOf(cursor.getInt(13));
        this.mCellLAC2 = cursor.isNull(14) ? null : Character.valueOf(cursor.getString(14).charAt(0));
        this.mCellRssi2 = cursor.isNull(15) ? null : Integer.valueOf(cursor.getInt(15));
        this.mMCC2 = cursor.isNull(16) ? null : Character.valueOf(cursor.getString(16).charAt(0));
        this.mMNC2 = cursor.isNull(17) ? null : Character.valueOf(cursor.getString(17).charAt(0));
        this.mReserved1 = !cursor.isNull(18) ? Integer.valueOf(cursor.getInt(18)) : num;
        this.mReserved2 = cursor.getString(19);
    }

    public HubTrajectoryData(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.id = null;
            parcel.readInt();
        } else {
            this.id = Integer.valueOf(parcel.readInt());
        }
        this.mTimeStamp = parcel.readByte() == 0 ? null : new Date(parcel.readLong());
        this.mCellID = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mCellLAC = parcel.readByte() == 0 ? null : Character.valueOf(parcel.createCharArray()[0]);
        this.mCellRssi = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mMCC = parcel.readByte() == 0 ? null : Character.valueOf(parcel.createCharArray()[0]);
        this.mMNC = parcel.readByte() == 0 ? null : Character.valueOf(parcel.createCharArray()[0]);
        this.mCellID1 = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mCellLAC1 = parcel.readByte() == 0 ? null : Character.valueOf(parcel.createCharArray()[0]);
        this.mCellRssi1 = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mMCC1 = parcel.readByte() == 0 ? null : Character.valueOf(parcel.createCharArray()[0]);
        this.mMNC1 = parcel.readByte() == 0 ? null : Character.valueOf(parcel.createCharArray()[0]);
        this.mCellID2 = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mCellLAC2 = parcel.readByte() == 0 ? null : Character.valueOf(parcel.createCharArray()[0]);
        this.mCellRssi2 = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mMCC2 = parcel.readByte() == 0 ? null : Character.valueOf(parcel.createCharArray()[0]);
        this.mMNC2 = parcel.readByte() == 0 ? null : Character.valueOf(parcel.createCharArray()[0]);
        this.mReserved1 = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mReserved2 = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private HubTrajectoryData(Integer num, Date date, Integer num2, Character ch, Integer num3, Character ch2, Character ch3, Integer num4, Character ch4, Integer num5, Character ch5, Character ch6, Integer num6, Character ch7, Integer num7, Character ch8, Character ch9, Integer num8, String str) {
        this.id = num;
        this.mTimeStamp = date;
        this.mCellID = num2;
        this.mCellLAC = ch;
        this.mCellRssi = num3;
        this.mMCC = ch2;
        this.mMNC = ch3;
        this.mCellID1 = num4;
        this.mCellLAC1 = ch4;
        this.mCellRssi1 = num5;
        this.mMCC1 = ch5;
        this.mMNC1 = ch6;
        this.mCellID2 = num6;
        this.mCellLAC2 = ch7;
        this.mCellRssi2 = num7;
        this.mMCC2 = ch8;
        this.mMNC2 = ch9;
        this.mReserved1 = num8;
        this.mReserved2 = str;
    }

    public HubTrajectoryData() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer num) {
        this.id = num;
        setValue();
    }

    public Date getMTimeStamp() {
        return this.mTimeStamp;
    }

    public void setMTimeStamp(Date date) {
        this.mTimeStamp = date;
        setValue();
    }

    public Integer getMCellID() {
        return this.mCellID;
    }

    public void setMCellID(Integer num) {
        this.mCellID = num;
        setValue();
    }

    public Character getMCellLAC() {
        return this.mCellLAC;
    }

    public void setMCellLAC(Character ch) {
        this.mCellLAC = ch;
        setValue();
    }

    public Integer getMCellRssi() {
        return this.mCellRssi;
    }

    public void setMCellRssi(Integer num) {
        this.mCellRssi = num;
        setValue();
    }

    public Character getMMCC() {
        return this.mMCC;
    }

    public void setMMCC(Character ch) {
        this.mMCC = ch;
        setValue();
    }

    public Character getMMNC() {
        return this.mMNC;
    }

    public void setMMNC(Character ch) {
        this.mMNC = ch;
        setValue();
    }

    public Integer getMCellID1() {
        return this.mCellID1;
    }

    public void setMCellID1(Integer num) {
        this.mCellID1 = num;
        setValue();
    }

    public Character getMCellLAC1() {
        return this.mCellLAC1;
    }

    public void setMCellLAC1(Character ch) {
        this.mCellLAC1 = ch;
        setValue();
    }

    public Integer getMCellRssi1() {
        return this.mCellRssi1;
    }

    public void setMCellRssi1(Integer num) {
        this.mCellRssi1 = num;
        setValue();
    }

    public Character getMMCC1() {
        return this.mMCC1;
    }

    public void setMMCC1(Character ch) {
        this.mMCC1 = ch;
        setValue();
    }

    public Character getMMNC1() {
        return this.mMNC1;
    }

    public void setMMNC1(Character ch) {
        this.mMNC1 = ch;
        setValue();
    }

    public Integer getMCellID2() {
        return this.mCellID2;
    }

    public void setMCellID2(Integer num) {
        this.mCellID2 = num;
        setValue();
    }

    public Character getMCellLAC2() {
        return this.mCellLAC2;
    }

    public void setMCellLAC2(Character ch) {
        this.mCellLAC2 = ch;
        setValue();
    }

    public Integer getMCellRssi2() {
        return this.mCellRssi2;
    }

    public void setMCellRssi2(Integer num) {
        this.mCellRssi2 = num;
        setValue();
    }

    public Character getMMCC2() {
        return this.mMCC2;
    }

    public void setMMCC2(Character ch) {
        this.mMCC2 = ch;
        setValue();
    }

    public Character getMMNC2() {
        return this.mMNC2;
    }

    public void setMMNC2(Character ch) {
        this.mMNC2 = ch;
        setValue();
    }

    public Integer getMReserved1() {
        return this.mReserved1;
    }

    public void setMReserved1(Integer num) {
        this.mReserved1 = num;
        setValue();
    }

    public String getMReserved2() {
        return this.mReserved2;
    }

    public void setMReserved2(String str) {
        this.mReserved2 = str;
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
        if (this.mTimeStamp != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.mTimeStamp.getTime());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mCellID != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mCellID.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mCellLAC != null) {
            parcel.writeByte((byte) 1);
            parcel.writeCharArray(new char[]{this.mCellLAC.charValue()});
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mCellRssi != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mCellRssi.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mMCC != null) {
            parcel.writeByte((byte) 1);
            parcel.writeCharArray(new char[]{this.mMCC.charValue()});
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mMNC != null) {
            parcel.writeByte((byte) 1);
            parcel.writeCharArray(new char[]{this.mMNC.charValue()});
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mCellID1 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mCellID1.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mCellLAC1 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeCharArray(new char[]{this.mCellLAC1.charValue()});
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mCellRssi1 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mCellRssi1.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mMCC1 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeCharArray(new char[]{this.mMCC1.charValue()});
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mMNC1 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeCharArray(new char[]{this.mMNC1.charValue()});
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mCellID2 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mCellID2.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mCellLAC2 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeCharArray(new char[]{this.mCellLAC2.charValue()});
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mCellRssi2 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mCellRssi2.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mMCC2 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeCharArray(new char[]{this.mMCC2.charValue()});
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mMNC2 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeCharArray(new char[]{this.mMNC2.charValue()});
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mReserved1 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mReserved1.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mReserved2 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mReserved2);
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<HubTrajectoryData> getHelper() {
        return HubTrajectoryDataHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "HubTrajectoryData { id: " + this.id + ", mTimeStamp: " + this.mTimeStamp + ", mCellID: " + this.mCellID + ", mCellLAC: " + this.mCellLAC + ", mCellRssi: " + this.mCellRssi + ", mMCC: " + this.mMCC + ", mMNC: " + this.mMNC + ", mCellID1: " + this.mCellID1 + ", mCellLAC1: " + this.mCellLAC1 + ", mCellRssi1: " + this.mCellRssi1 + ", mMCC1: " + this.mMCC1 + ", mMNC1: " + this.mMNC1 + ", mCellID2: " + this.mCellID2 + ", mCellLAC2: " + this.mCellLAC2 + ", mCellRssi2: " + this.mCellRssi2 + ", mMCC2: " + this.mMCC2 + ", mMNC2: " + this.mMNC2 + ", mReserved1: " + this.mReserved1 + ", mReserved2: " + this.mReserved2 + " }";
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
