package com.huawei.nb.model.pengine;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class Parameter extends AManagedObject {
    public static final Parcelable.Creator<Parameter> CREATOR = new Parcelable.Creator<Parameter>() {
        /* class com.huawei.nb.model.pengine.Parameter.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public Parameter createFromParcel(Parcel parcel) {
            return new Parameter(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public Parameter[] newArray(int i) {
            return new Parameter[i];
        }
    };
    private Integer id;
    private String module;
    private String name;
    private String value;

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
        return "com.huawei.nb.model.pengine.Parameter";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public Parameter(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.id = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.module = cursor.getString(2);
        this.name = cursor.getString(3);
        this.value = cursor.getString(4);
    }

    public Parameter(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.id = null;
            parcel.readInt();
        } else {
            this.id = Integer.valueOf(parcel.readInt());
        }
        this.module = parcel.readByte() == 0 ? null : parcel.readString();
        this.name = parcel.readByte() == 0 ? null : parcel.readString();
        this.value = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private Parameter(Integer num, String str, String str2, String str3) {
        this.id = num;
        this.module = str;
        this.name = str2;
        this.value = str3;
    }

    public Parameter() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer num) {
        this.id = num;
        setValue();
    }

    public String getModule() {
        return this.module;
    }

    public void setModule(String str) {
        this.module = str;
        setValue();
    }

    public String getName() {
        return this.name;
    }

    public void setName(String str) {
        this.name = str;
        setValue();
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String str) {
        this.value = str;
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
        if (this.module != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.module);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.name != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.name);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.value != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.value);
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<Parameter> getHelper() {
        return ParameterHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "Parameter { id: " + this.id + ", module: " + this.module + ", name: " + this.name + ", value: " + this.value + " }";
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
