package com.huawei.nb.model.pengine;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class PengineSwitch extends AManagedObject {
    public static final Parcelable.Creator<PengineSwitch> CREATOR = new Parcelable.Creator<PengineSwitch>() {
        /* class com.huawei.nb.model.pengine.PengineSwitch.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public PengineSwitch createFromParcel(Parcel parcel) {
            return new PengineSwitch(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public PengineSwitch[] newArray(int i) {
            return new PengineSwitch[i];
        }
    };
    private String column0;
    private String column1;
    private String column2;
    private String column3;
    private String column4;
    private String column5;
    private String column6;
    private String column7;
    private Integer dataType;
    private Integer id;
    private Long timestamp;
    private String upProperty;

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
        return "com.huawei.nb.model.pengine.PengineSwitch";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public PengineSwitch(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Long l = null;
        this.id = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.dataType = cursor.isNull(2) ? null : Integer.valueOf(cursor.getInt(2));
        this.timestamp = !cursor.isNull(3) ? Long.valueOf(cursor.getLong(3)) : l;
        this.upProperty = cursor.getString(4);
        this.column0 = cursor.getString(5);
        this.column1 = cursor.getString(6);
        this.column2 = cursor.getString(7);
        this.column3 = cursor.getString(8);
        this.column4 = cursor.getString(9);
        this.column5 = cursor.getString(10);
        this.column6 = cursor.getString(11);
        this.column7 = cursor.getString(12);
    }

    public PengineSwitch(Parcel parcel) {
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
        this.upProperty = parcel.readByte() == 0 ? null : parcel.readString();
        this.column0 = parcel.readByte() == 0 ? null : parcel.readString();
        this.column1 = parcel.readByte() == 0 ? null : parcel.readString();
        this.column2 = parcel.readByte() == 0 ? null : parcel.readString();
        this.column3 = parcel.readByte() == 0 ? null : parcel.readString();
        this.column4 = parcel.readByte() == 0 ? null : parcel.readString();
        this.column5 = parcel.readByte() == 0 ? null : parcel.readString();
        this.column6 = parcel.readByte() == 0 ? null : parcel.readString();
        this.column7 = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private PengineSwitch(Integer num, Integer num2, Long l, String str, String str2, String str3, String str4, String str5, String str6, String str7, String str8, String str9) {
        this.id = num;
        this.dataType = num2;
        this.timestamp = l;
        this.upProperty = str;
        this.column0 = str2;
        this.column1 = str3;
        this.column2 = str4;
        this.column3 = str5;
        this.column4 = str6;
        this.column5 = str7;
        this.column6 = str8;
        this.column7 = str9;
    }

    public PengineSwitch() {
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

    public String getUpProperty() {
        return this.upProperty;
    }

    public void setUpProperty(String str) {
        this.upProperty = str;
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
        if (this.upProperty != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.upProperty);
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
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<PengineSwitch> getHelper() {
        return PengineSwitchHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "PengineSwitch { id: " + this.id + ", dataType: " + this.dataType + ", timestamp: " + this.timestamp + ", upProperty: " + this.upProperty + ", column0: " + this.column0 + ", column1: " + this.column1 + ", column2: " + this.column2 + ", column3: " + this.column3 + ", column4: " + this.column4 + ", column5: " + this.column5 + ", column6: " + this.column6 + ", column7: " + this.column7 + " }";
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
