package com.huawei.nb.model.pengine;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class CapCellInfo extends AManagedObject {
    public static final Parcelable.Creator<CapCellInfo> CREATOR = new Parcelable.Creator<CapCellInfo>() {
        /* class com.huawei.nb.model.pengine.CapCellInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public CapCellInfo createFromParcel(Parcel parcel) {
            return new CapCellInfo(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public CapCellInfo[] newArray(int i) {
            return new CapCellInfo[i];
        }
    };
    private String column0;
    private String column1;
    private String column10;
    private String column11;
    private String column12;
    private String column13;
    private String column14;
    private String column15;
    private String column16;
    private String column17;
    private String column18;
    private String column19;
    private String column2;
    private String column3;
    private String column4;
    private String column5;
    private String column6;
    private String column7;
    private String column8;
    private String column9;
    private Integer dataType;
    private Integer id;
    private Long timestamp;

    @Override // com.huawei.odmf.core.AManagedObject, android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getDatabaseName() {
        return "dsPengineData";
    }

    public String getDatabaseVersion() {
        return "0.0.11";
    }

    public int getDatabaseVersionCode() {
        return 11;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getEntityName() {
        return "com.huawei.nb.model.pengine.CapCellInfo";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public CapCellInfo(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Long l = null;
        this.id = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.dataType = cursor.isNull(2) ? null : Integer.valueOf(cursor.getInt(2));
        this.timestamp = !cursor.isNull(3) ? Long.valueOf(cursor.getLong(3)) : l;
        this.column0 = cursor.getString(4);
        this.column1 = cursor.getString(5);
        this.column2 = cursor.getString(6);
        this.column3 = cursor.getString(7);
        this.column4 = cursor.getString(8);
        this.column5 = cursor.getString(9);
        this.column6 = cursor.getString(10);
        this.column7 = cursor.getString(11);
        this.column8 = cursor.getString(12);
        this.column9 = cursor.getString(13);
        this.column10 = cursor.getString(14);
        this.column11 = cursor.getString(15);
        this.column12 = cursor.getString(16);
        this.column13 = cursor.getString(17);
        this.column14 = cursor.getString(18);
        this.column15 = cursor.getString(19);
        this.column16 = cursor.getString(20);
        this.column17 = cursor.getString(21);
        this.column18 = cursor.getString(22);
        this.column19 = cursor.getString(23);
    }

    public CapCellInfo(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.id = null;
            parcel.readInt();
        } else {
            this.id = Integer.valueOf(parcel.readInt());
        }
        this.dataType = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.timestamp = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.column0 = parcel.readByte() == 0 ? null : parcel.readString();
        this.column1 = parcel.readByte() == 0 ? null : parcel.readString();
        this.column2 = parcel.readByte() == 0 ? null : parcel.readString();
        this.column3 = parcel.readByte() == 0 ? null : parcel.readString();
        this.column4 = parcel.readByte() == 0 ? null : parcel.readString();
        this.column5 = parcel.readByte() == 0 ? null : parcel.readString();
        this.column6 = parcel.readByte() == 0 ? null : parcel.readString();
        this.column7 = parcel.readByte() == 0 ? null : parcel.readString();
        this.column8 = parcel.readByte() == 0 ? null : parcel.readString();
        this.column9 = parcel.readByte() == 0 ? null : parcel.readString();
        this.column10 = parcel.readByte() == 0 ? null : parcel.readString();
        this.column11 = parcel.readByte() == 0 ? null : parcel.readString();
        this.column12 = parcel.readByte() == 0 ? null : parcel.readString();
        this.column13 = parcel.readByte() == 0 ? null : parcel.readString();
        this.column14 = parcel.readByte() == 0 ? null : parcel.readString();
        this.column15 = parcel.readByte() == 0 ? null : parcel.readString();
        this.column16 = parcel.readByte() == 0 ? null : parcel.readString();
        this.column17 = parcel.readByte() == 0 ? null : parcel.readString();
        this.column18 = parcel.readByte() == 0 ? null : parcel.readString();
        this.column19 = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private CapCellInfo(Integer num, Integer num2, Long l, String str, String str2, String str3, String str4, String str5, String str6, String str7, String str8, String str9, String str10, String str11, String str12, String str13, String str14, String str15, String str16, String str17, String str18, String str19, String str20) {
        this.id = num;
        this.dataType = num2;
        this.timestamp = l;
        this.column0 = str;
        this.column1 = str2;
        this.column2 = str3;
        this.column3 = str4;
        this.column4 = str5;
        this.column5 = str6;
        this.column6 = str7;
        this.column7 = str8;
        this.column8 = str9;
        this.column9 = str10;
        this.column10 = str11;
        this.column11 = str12;
        this.column12 = str13;
        this.column13 = str14;
        this.column14 = str15;
        this.column15 = str16;
        this.column16 = str17;
        this.column17 = str18;
        this.column18 = str19;
        this.column19 = str20;
    }

    public CapCellInfo() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer num) {
        this.id = num;
        setValue();
    }

    public Integer getDataType() {
        return this.dataType;
    }

    public void setDataType(Integer num) {
        this.dataType = num;
        setValue();
    }

    public Long getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(Long l) {
        this.timestamp = l;
        setValue();
    }

    public String getColumn0() {
        return this.column0;
    }

    public void setColumn0(String str) {
        this.column0 = str;
        setValue();
    }

    public String getColumn1() {
        return this.column1;
    }

    public void setColumn1(String str) {
        this.column1 = str;
        setValue();
    }

    public String getColumn2() {
        return this.column2;
    }

    public void setColumn2(String str) {
        this.column2 = str;
        setValue();
    }

    public String getColumn3() {
        return this.column3;
    }

    public void setColumn3(String str) {
        this.column3 = str;
        setValue();
    }

    public String getColumn4() {
        return this.column4;
    }

    public void setColumn4(String str) {
        this.column4 = str;
        setValue();
    }

    public String getColumn5() {
        return this.column5;
    }

    public void setColumn5(String str) {
        this.column5 = str;
        setValue();
    }

    public String getColumn6() {
        return this.column6;
    }

    public void setColumn6(String str) {
        this.column6 = str;
        setValue();
    }

    public String getColumn7() {
        return this.column7;
    }

    public void setColumn7(String str) {
        this.column7 = str;
        setValue();
    }

    public String getColumn8() {
        return this.column8;
    }

    public void setColumn8(String str) {
        this.column8 = str;
        setValue();
    }

    public String getColumn9() {
        return this.column9;
    }

    public void setColumn9(String str) {
        this.column9 = str;
        setValue();
    }

    public String getColumn10() {
        return this.column10;
    }

    public void setColumn10(String str) {
        this.column10 = str;
        setValue();
    }

    public String getColumn11() {
        return this.column11;
    }

    public void setColumn11(String str) {
        this.column11 = str;
        setValue();
    }

    public String getColumn12() {
        return this.column12;
    }

    public void setColumn12(String str) {
        this.column12 = str;
        setValue();
    }

    public String getColumn13() {
        return this.column13;
    }

    public void setColumn13(String str) {
        this.column13 = str;
        setValue();
    }

    public String getColumn14() {
        return this.column14;
    }

    public void setColumn14(String str) {
        this.column14 = str;
        setValue();
    }

    public String getColumn15() {
        return this.column15;
    }

    public void setColumn15(String str) {
        this.column15 = str;
        setValue();
    }

    public String getColumn16() {
        return this.column16;
    }

    public void setColumn16(String str) {
        this.column16 = str;
        setValue();
    }

    public String getColumn17() {
        return this.column17;
    }

    public void setColumn17(String str) {
        this.column17 = str;
        setValue();
    }

    public String getColumn18() {
        return this.column18;
    }

    public void setColumn18(String str) {
        this.column18 = str;
        setValue();
    }

    public String getColumn19() {
        return this.column19;
    }

    public void setColumn19(String str) {
        this.column19 = str;
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
        if (this.dataType != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.dataType.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.timestamp != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.timestamp.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.column0 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.column0);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.column1 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.column1);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.column2 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.column2);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.column3 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.column3);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.column4 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.column4);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.column5 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.column5);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.column6 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.column6);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.column7 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.column7);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.column8 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.column8);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.column9 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.column9);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.column10 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.column10);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.column11 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.column11);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.column12 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.column12);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.column13 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.column13);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.column14 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.column14);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.column15 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.column15);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.column16 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.column16);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.column17 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.column17);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.column18 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.column18);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.column19 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.column19);
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<CapCellInfo> getHelper() {
        return CapCellInfoHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "CapCellInfo { id: " + this.id + ", dataType: " + this.dataType + ", timestamp: " + this.timestamp + ", column0: " + this.column0 + ", column1: " + this.column1 + ", column2: " + this.column2 + ", column3: " + this.column3 + ", column4: " + this.column4 + ", column5: " + this.column5 + ", column6: " + this.column6 + ", column7: " + this.column7 + ", column8: " + this.column8 + ", column9: " + this.column9 + ", column10: " + this.column10 + ", column11: " + this.column11 + ", column12: " + this.column12 + ", column13: " + this.column13 + ", column14: " + this.column14 + ", column15: " + this.column15 + ", column16: " + this.column16 + ", column17: " + this.column17 + ", column18: " + this.column18 + ", column19: " + this.column19 + " }";
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
