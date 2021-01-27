package com.huawei.nb.model.pengine;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class RoutineCoreData extends AManagedObject {
    public static final Parcelable.Creator<RoutineCoreData> CREATOR = new Parcelable.Creator<RoutineCoreData>() {
        /* class com.huawei.nb.model.pengine.RoutineCoreData.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public RoutineCoreData createFromParcel(Parcel parcel) {
            return new RoutineCoreData(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public RoutineCoreData[] newArray(int i) {
            return new RoutineCoreData[i];
        }
    };
    private String column0;
    private String column1;
    private String column2;
    private String column3;
    private String column4;
    private String coreData;
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
        return "com.huawei.nb.model.pengine.RoutineCoreData";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public RoutineCoreData(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Long l = null;
        this.id = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.timestamp = !cursor.isNull(2) ? Long.valueOf(cursor.getLong(2)) : l;
        this.coreData = cursor.getString(3);
        this.column0 = cursor.getString(4);
        this.column1 = cursor.getString(5);
        this.column2 = cursor.getString(6);
        this.column3 = cursor.getString(7);
        this.column4 = cursor.getString(8);
    }

    public RoutineCoreData(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.id = null;
            parcel.readInt();
        } else {
            this.id = Integer.valueOf(parcel.readInt());
        }
        this.timestamp = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.coreData = parcel.readByte() == 0 ? null : parcel.readString();
        this.column0 = parcel.readByte() == 0 ? null : parcel.readString();
        this.column1 = parcel.readByte() == 0 ? null : parcel.readString();
        this.column2 = parcel.readByte() == 0 ? null : parcel.readString();
        this.column3 = parcel.readByte() == 0 ? null : parcel.readString();
        this.column4 = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private RoutineCoreData(Integer num, Long l, String str, String str2, String str3, String str4, String str5, String str6) {
        this.id = num;
        this.timestamp = l;
        this.coreData = str;
        this.column0 = str2;
        this.column1 = str3;
        this.column2 = str4;
        this.column3 = str5;
        this.column4 = str6;
    }

    public RoutineCoreData() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer num) {
        this.id = num;
        setValue();
    }

    public Long getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(Long l) {
        this.timestamp = l;
        setValue();
    }

    public String getCoreData() {
        return this.coreData;
    }

    public void setCoreData(String str) {
        this.coreData = str;
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
        if (this.timestamp != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.timestamp.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.coreData != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.coreData);
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
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<RoutineCoreData> getHelper() {
        return RoutineCoreDataHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "RoutineCoreData { id: " + this.id + ", timestamp: " + this.timestamp + ", coreData: " + this.coreData + ", column0: " + this.column0 + ", column1: " + this.column1 + ", column2: " + this.column2 + ", column3: " + this.column3 + ", column4: " + this.column4 + " }";
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
