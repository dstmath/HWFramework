package com.huawei.nb.model.meta;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class ZMetaData extends AManagedObject {
    public static final Parcelable.Creator<ZMetaData> CREATOR = new Parcelable.Creator<ZMetaData>() {
        public ZMetaData createFromParcel(Parcel in) {
            return new ZMetaData(in);
        }

        public ZMetaData[] newArray(int size) {
            return new ZMetaData[size];
        }
    };
    private Integer id;
    private String name;
    private String value;

    public ZMetaData(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.id = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.name = cursor.getString(2);
        this.value = cursor.getString(3);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public ZMetaData(Parcel in) {
        super(in);
        String str = null;
        if (in.readByte() == 0) {
            this.id = null;
            in.readInt();
        } else {
            this.id = Integer.valueOf(in.readInt());
        }
        this.name = in.readByte() == 0 ? null : in.readString();
        this.value = in.readByte() != 0 ? in.readString() : str;
    }

    private ZMetaData(Integer id2, String name2, String value2) {
        this.id = id2;
        this.name = name2;
        this.value = value2;
    }

    public ZMetaData() {
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
            return;
        }
        out.writeByte((byte) 0);
    }

    public AEntityHelper<ZMetaData> getHelper() {
        return ZMetaDataHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.meta.ZMetaData";
    }

    public String getDatabaseName() {
        return "dsWeather";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("ZMetaData { id: ").append(this.id);
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
        return "0.0.17";
    }

    public int getDatabaseVersionCode() {
        return 17;
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }
}
