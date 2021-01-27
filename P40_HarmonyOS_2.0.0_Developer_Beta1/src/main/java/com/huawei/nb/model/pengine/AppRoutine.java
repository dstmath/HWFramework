package com.huawei.nb.model.pengine;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class AppRoutine extends AManagedObject {
    public static final Parcelable.Creator<AppRoutine> CREATOR = new Parcelable.Creator<AppRoutine>() {
        /* class com.huawei.nb.model.pengine.AppRoutine.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public AppRoutine createFromParcel(Parcel parcel) {
            return new AppRoutine(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public AppRoutine[] newArray(int i) {
            return new AppRoutine[i];
        }
    };
    private String column0;
    private String column1;
    private String column2;
    private String column3;
    private String column4;
    private String endTime;
    private Integer id;
    private String packageName;
    private String poi;
    private String startTime;
    private String support;
    private String timestamp;

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
        return "com.huawei.nb.model.pengine.AppRoutine";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public AppRoutine(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.id = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.timestamp = cursor.getString(2);
        this.startTime = cursor.getString(3);
        this.endTime = cursor.getString(4);
        this.poi = cursor.getString(5);
        this.packageName = cursor.getString(6);
        this.support = cursor.getString(7);
        this.column0 = cursor.getString(8);
        this.column1 = cursor.getString(9);
        this.column2 = cursor.getString(10);
        this.column3 = cursor.getString(11);
        this.column4 = cursor.getString(12);
    }

    public AppRoutine(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.id = null;
            parcel.readInt();
        } else {
            this.id = Integer.valueOf(parcel.readInt());
        }
        this.timestamp = parcel.readByte() == 0 ? null : parcel.readString();
        this.startTime = parcel.readByte() == 0 ? null : parcel.readString();
        this.endTime = parcel.readByte() == 0 ? null : parcel.readString();
        this.poi = parcel.readByte() == 0 ? null : parcel.readString();
        this.packageName = parcel.readByte() == 0 ? null : parcel.readString();
        this.support = parcel.readByte() == 0 ? null : parcel.readString();
        this.column0 = parcel.readByte() == 0 ? null : parcel.readString();
        this.column1 = parcel.readByte() == 0 ? null : parcel.readString();
        this.column2 = parcel.readByte() == 0 ? null : parcel.readString();
        this.column3 = parcel.readByte() == 0 ? null : parcel.readString();
        this.column4 = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private AppRoutine(Integer num, String str, String str2, String str3, String str4, String str5, String str6, String str7, String str8, String str9, String str10, String str11) {
        this.id = num;
        this.timestamp = str;
        this.startTime = str2;
        this.endTime = str3;
        this.poi = str4;
        this.packageName = str5;
        this.support = str6;
        this.column0 = str7;
        this.column1 = str8;
        this.column2 = str9;
        this.column3 = str10;
        this.column4 = str11;
    }

    public AppRoutine() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer num) {
        this.id = num;
        setValue();
    }

    public String getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(String str) {
        this.timestamp = str;
        setValue();
    }

    public String getStartTime() {
        return this.startTime;
    }

    public void setStartTime(String str) {
        this.startTime = str;
        setValue();
    }

    public String getEndTime() {
        return this.endTime;
    }

    public void setEndTime(String str) {
        this.endTime = str;
        setValue();
    }

    public String getPoi() {
        return this.poi;
    }

    public void setPoi(String str) {
        this.poi = str;
        setValue();
    }

    public String getPackageName() {
        return this.packageName;
    }

    public void setPackageName(String str) {
        this.packageName = str;
        setValue();
    }

    public String getSupport() {
        return this.support;
    }

    public void setSupport(String str) {
        this.support = str;
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
            parcel.writeString(this.timestamp);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.startTime != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.startTime);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.endTime != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.endTime);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.poi != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.poi);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.packageName != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.packageName);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.support != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.support);
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
    public AEntityHelper<AppRoutine> getHelper() {
        return AppRoutineHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "AppRoutine { id: " + this.id + ", timestamp: " + this.timestamp + ", startTime: " + this.startTime + ", endTime: " + this.endTime + ", poi: " + this.poi + ", packageName: " + this.packageName + ", support: " + this.support + ", column0: " + this.column0 + ", column1: " + this.column1 + ", column2: " + this.column2 + ", column3: " + this.column3 + ", column4: " + this.column4 + " }";
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
