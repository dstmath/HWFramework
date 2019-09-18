package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class RawAppDataOrigin extends AManagedObject {
    public static final Parcelable.Creator<RawAppDataOrigin> CREATOR = new Parcelable.Creator<RawAppDataOrigin>() {
        public RawAppDataOrigin createFromParcel(Parcel in) {
            return new RawAppDataOrigin(in);
        }

        public RawAppDataOrigin[] newArray(int size) {
            return new RawAppDataOrigin[size];
        }
    };
    private String column1;
    private String column2;
    private Long dataSerialNumber;
    private Integer id;
    private String jsonData;
    private String packageName;
    private Long timestamp;

    public RawAppDataOrigin(Cursor cursor) {
        Long l = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.id = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.dataSerialNumber = cursor.isNull(2) ? null : Long.valueOf(cursor.getLong(2));
        this.timestamp = !cursor.isNull(3) ? Long.valueOf(cursor.getLong(3)) : l;
        this.packageName = cursor.getString(4);
        this.jsonData = cursor.getString(5);
        this.column1 = cursor.getString(6);
        this.column2 = cursor.getString(7);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public RawAppDataOrigin(Parcel in) {
        super(in);
        String str = null;
        if (in.readByte() == 0) {
            this.id = null;
            in.readInt();
        } else {
            this.id = Integer.valueOf(in.readInt());
        }
        this.dataSerialNumber = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.timestamp = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.packageName = in.readByte() == 0 ? null : in.readString();
        this.jsonData = in.readByte() == 0 ? null : in.readString();
        this.column1 = in.readByte() == 0 ? null : in.readString();
        this.column2 = in.readByte() != 0 ? in.readString() : str;
    }

    private RawAppDataOrigin(Integer id2, Long dataSerialNumber2, Long timestamp2, String packageName2, String jsonData2, String column12, String column22) {
        this.id = id2;
        this.dataSerialNumber = dataSerialNumber2;
        this.timestamp = timestamp2;
        this.packageName = packageName2;
        this.jsonData = jsonData2;
        this.column1 = column12;
        this.column2 = column22;
    }

    public RawAppDataOrigin() {
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

    public Long getDataSerialNumber() {
        return this.dataSerialNumber;
    }

    public void setDataSerialNumber(Long dataSerialNumber2) {
        this.dataSerialNumber = dataSerialNumber2;
        setValue();
    }

    public Long getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(Long timestamp2) {
        this.timestamp = timestamp2;
        setValue();
    }

    public String getPackageName() {
        return this.packageName;
    }

    public void setPackageName(String packageName2) {
        this.packageName = packageName2;
        setValue();
    }

    public String getJsonData() {
        return this.jsonData;
    }

    public void setJsonData(String jsonData2) {
        this.jsonData = jsonData2;
        setValue();
    }

    public String getColumn1() {
        return this.column1;
    }

    public void setColumn1(String column12) {
        this.column1 = column12;
        setValue();
    }

    public String getColumn2() {
        return this.column2;
    }

    public void setColumn2(String column22) {
        this.column2 = column22;
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
        if (this.dataSerialNumber != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.dataSerialNumber.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.timestamp != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.timestamp.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.packageName != null) {
            out.writeByte((byte) 1);
            out.writeString(this.packageName);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.jsonData != null) {
            out.writeByte((byte) 1);
            out.writeString(this.jsonData);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.column1 != null) {
            out.writeByte((byte) 1);
            out.writeString(this.column1);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.column2 != null) {
            out.writeByte((byte) 1);
            out.writeString(this.column2);
            return;
        }
        out.writeByte((byte) 0);
    }

    public AEntityHelper<RawAppDataOrigin> getHelper() {
        return RawAppDataOriginHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.collectencrypt.RawAppDataOrigin";
    }

    public String getDatabaseName() {
        return "dsCollectEncrypt";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("RawAppDataOrigin { id: ").append(this.id);
        sb.append(", dataSerialNumber: ").append(this.dataSerialNumber);
        sb.append(", timestamp: ").append(this.timestamp);
        sb.append(", packageName: ").append(this.packageName);
        sb.append(", jsonData: ").append(this.jsonData);
        sb.append(", column1: ").append(this.column1);
        sb.append(", column2: ").append(this.column2);
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
