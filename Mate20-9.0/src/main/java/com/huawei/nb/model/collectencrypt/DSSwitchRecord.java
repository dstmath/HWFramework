package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class DSSwitchRecord extends AManagedObject {
    public static final Parcelable.Creator<DSSwitchRecord> CREATOR = new Parcelable.Creator<DSSwitchRecord>() {
        public DSSwitchRecord createFromParcel(Parcel in) {
            return new DSSwitchRecord(in);
        }

        public DSSwitchRecord[] newArray(int size) {
            return new DSSwitchRecord[size];
        }
    };
    private Integer id;
    private String packageName;
    private String reserved1;
    private String reserved2;
    private String reserved3;
    private String reserved4;
    private String switchName;
    private String switchStatus;
    private Long timeStamp;

    public DSSwitchRecord(Cursor cursor) {
        Long l = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.id = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.timeStamp = !cursor.isNull(2) ? Long.valueOf(cursor.getLong(2)) : l;
        this.switchStatus = cursor.getString(3);
        this.switchName = cursor.getString(4);
        this.packageName = cursor.getString(5);
        this.reserved1 = cursor.getString(6);
        this.reserved2 = cursor.getString(7);
        this.reserved3 = cursor.getString(8);
        this.reserved4 = cursor.getString(9);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public DSSwitchRecord(Parcel in) {
        super(in);
        String str = null;
        if (in.readByte() == 0) {
            this.id = null;
            in.readInt();
        } else {
            this.id = Integer.valueOf(in.readInt());
        }
        this.timeStamp = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.switchStatus = in.readByte() == 0 ? null : in.readString();
        this.switchName = in.readByte() == 0 ? null : in.readString();
        this.packageName = in.readByte() == 0 ? null : in.readString();
        this.reserved1 = in.readByte() == 0 ? null : in.readString();
        this.reserved2 = in.readByte() == 0 ? null : in.readString();
        this.reserved3 = in.readByte() == 0 ? null : in.readString();
        this.reserved4 = in.readByte() != 0 ? in.readString() : str;
    }

    private DSSwitchRecord(Integer id2, Long timeStamp2, String switchStatus2, String switchName2, String packageName2, String reserved12, String reserved22, String reserved32, String reserved42) {
        this.id = id2;
        this.timeStamp = timeStamp2;
        this.switchStatus = switchStatus2;
        this.switchName = switchName2;
        this.packageName = packageName2;
        this.reserved1 = reserved12;
        this.reserved2 = reserved22;
        this.reserved3 = reserved32;
        this.reserved4 = reserved42;
    }

    public DSSwitchRecord() {
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

    public Long getTimeStamp() {
        return this.timeStamp;
    }

    public void setTimeStamp(Long timeStamp2) {
        this.timeStamp = timeStamp2;
        setValue();
    }

    public String getSwitchStatus() {
        return this.switchStatus;
    }

    public void setSwitchStatus(String switchStatus2) {
        this.switchStatus = switchStatus2;
        setValue();
    }

    public String getSwitchName() {
        return this.switchName;
    }

    public void setSwitchName(String switchName2) {
        this.switchName = switchName2;
        setValue();
    }

    public String getPackageName() {
        return this.packageName;
    }

    public void setPackageName(String packageName2) {
        this.packageName = packageName2;
        setValue();
    }

    public String getReserved1() {
        return this.reserved1;
    }

    public void setReserved1(String reserved12) {
        this.reserved1 = reserved12;
        setValue();
    }

    public String getReserved2() {
        return this.reserved2;
    }

    public void setReserved2(String reserved22) {
        this.reserved2 = reserved22;
        setValue();
    }

    public String getReserved3() {
        return this.reserved3;
    }

    public void setReserved3(String reserved32) {
        this.reserved3 = reserved32;
        setValue();
    }

    public String getReserved4() {
        return this.reserved4;
    }

    public void setReserved4(String reserved42) {
        this.reserved4 = reserved42;
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
        if (this.timeStamp != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.timeStamp.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.switchStatus != null) {
            out.writeByte((byte) 1);
            out.writeString(this.switchStatus);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.switchName != null) {
            out.writeByte((byte) 1);
            out.writeString(this.switchName);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.packageName != null) {
            out.writeByte((byte) 1);
            out.writeString(this.packageName);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.reserved1 != null) {
            out.writeByte((byte) 1);
            out.writeString(this.reserved1);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.reserved2 != null) {
            out.writeByte((byte) 1);
            out.writeString(this.reserved2);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.reserved3 != null) {
            out.writeByte((byte) 1);
            out.writeString(this.reserved3);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.reserved4 != null) {
            out.writeByte((byte) 1);
            out.writeString(this.reserved4);
            return;
        }
        out.writeByte((byte) 0);
    }

    public AEntityHelper<DSSwitchRecord> getHelper() {
        return DSSwitchRecordHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.collectencrypt.DSSwitchRecord";
    }

    public String getDatabaseName() {
        return "dsCollectEncrypt";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("DSSwitchRecord { id: ").append(this.id);
        sb.append(", timeStamp: ").append(this.timeStamp);
        sb.append(", switchStatus: ").append(this.switchStatus);
        sb.append(", switchName: ").append(this.switchName);
        sb.append(", packageName: ").append(this.packageName);
        sb.append(", reserved1: ").append(this.reserved1);
        sb.append(", reserved2: ").append(this.reserved2);
        sb.append(", reserved3: ").append(this.reserved3);
        sb.append(", reserved4: ").append(this.reserved4);
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
