package com.huawei.nb.model.pengine;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class Parameter extends AManagedObject {
    public static final Parcelable.Creator<Parameter> CREATOR = new Parcelable.Creator<Parameter>() {
        public Parameter createFromParcel(Parcel in) {
            return new Parameter(in);
        }

        public Parameter[] newArray(int size) {
            return new Parameter[size];
        }
    };
    private Integer id;
    private String module;
    private String name;
    private String value;

    public Parameter(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.id = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.module = cursor.getString(2);
        this.name = cursor.getString(3);
        this.value = cursor.getString(4);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public Parameter(Parcel in) {
        super(in);
        String str = null;
        if (in.readByte() == 0) {
            this.id = null;
            in.readInt();
        } else {
            this.id = Integer.valueOf(in.readInt());
        }
        this.module = in.readByte() == 0 ? null : in.readString();
        this.name = in.readByte() == 0 ? null : in.readString();
        this.value = in.readByte() != 0 ? in.readString() : str;
    }

    private Parameter(Integer id2, String module2, String name2, String value2) {
        this.id = id2;
        this.module = module2;
        this.name = name2;
        this.value = value2;
    }

    public Parameter() {
    }

    public int describeContents() {
        return 0;
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id2) {
        this.id = id2;
        setValue();
    }

    public String getModule() {
        return this.module;
    }

    public void setModule(String module2) {
        this.module = module2;
        setValue();
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name2) {
        this.name = name2;
        setValue();
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value2) {
        this.value = value2;
        setValue();
    }

    public void writeToParcel(Parcel out, int ignored) {
        super.writeToParcel(out, ignored);
        if (this.id != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.id.intValue());
        } else {
            out.writeByte((byte) 0);
            out.writeInt(1);
        }
        if (this.module != null) {
            out.writeByte((byte) 1);
            out.writeString(this.module);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.name != null) {
            out.writeByte((byte) 1);
            out.writeString(this.name);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.value != null) {
            out.writeByte((byte) 1);
            out.writeString(this.value);
            return;
        }
        out.writeByte((byte) 0);
    }

    public AEntityHelper<Parameter> getHelper() {
        return ParameterHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.pengine.Parameter";
    }

    public String getDatabaseName() {
        return "dsPengineData";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("Parameter { id: ").append(this.id);
        sb.append(", module: ").append(this.module);
        sb.append(", name: ").append(this.name);
        sb.append(", value: ").append(this.value);
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
        return "0.0.8";
    }

    public int getDatabaseVersionCode() {
        return 8;
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }
}
