package com.huawei.nb.model.search;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class UniqueSearchConfig extends AManagedObject {
    public static final Parcelable.Creator<UniqueSearchConfig> CREATOR = new Parcelable.Creator<UniqueSearchConfig>() {
        public UniqueSearchConfig createFromParcel(Parcel in) {
            return new UniqueSearchConfig(in);
        }

        public UniqueSearchConfig[] newArray(int size) {
            return new UniqueSearchConfig[size];
        }
    };
    private Integer id;
    private String module;
    private String name;
    private Integer process;
    private String value;

    public UniqueSearchConfig(Cursor cursor) {
        Integer num = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.id = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.name = cursor.getString(2);
        this.value = cursor.getString(3);
        this.module = cursor.getString(4);
        this.process = !cursor.isNull(5) ? Integer.valueOf(cursor.getInt(5)) : num;
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public UniqueSearchConfig(Parcel in) {
        super(in);
        Integer num = null;
        if (in.readByte() == 0) {
            this.id = null;
            in.readInt();
        } else {
            this.id = Integer.valueOf(in.readInt());
        }
        this.name = in.readByte() == 0 ? null : in.readString();
        this.value = in.readByte() == 0 ? null : in.readString();
        this.module = in.readByte() == 0 ? null : in.readString();
        this.process = in.readByte() != 0 ? Integer.valueOf(in.readInt()) : num;
    }

    private UniqueSearchConfig(Integer id2, String name2, String value2, String module2, Integer process2) {
        this.id = id2;
        this.name = name2;
        this.value = value2;
        this.module = module2;
        this.process = process2;
    }

    public UniqueSearchConfig() {
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

    public String getModule() {
        return this.module;
    }

    public void setModule(String module2) {
        this.module = module2;
        setValue();
    }

    public Integer getProcess() {
        return this.process;
    }

    public void setProcess(Integer process2) {
        this.process = process2;
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
        if (this.name != null) {
            out.writeByte((byte) 1);
            out.writeString(this.name);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.value != null) {
            out.writeByte((byte) 1);
            out.writeString(this.value);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.module != null) {
            out.writeByte((byte) 1);
            out.writeString(this.module);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.process != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.process.intValue());
            return;
        }
        out.writeByte((byte) 0);
    }

    public AEntityHelper<UniqueSearchConfig> getHelper() {
        return UniqueSearchConfigHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.search.UniqueSearchConfig";
    }

    public String getDatabaseName() {
        return "dsSearch";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("UniqueSearchConfig { id: ").append(this.id);
        sb.append(", name: ").append(this.name);
        sb.append(", value: ").append(this.value);
        sb.append(", module: ").append(this.module);
        sb.append(", process: ").append(this.process);
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
        return "0.0.7";
    }

    public int getDatabaseVersionCode() {
        return 7;
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }
}
