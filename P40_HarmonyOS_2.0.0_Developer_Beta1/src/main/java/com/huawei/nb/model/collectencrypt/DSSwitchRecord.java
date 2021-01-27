package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class DSSwitchRecord extends AManagedObject {
    public static final Parcelable.Creator<DSSwitchRecord> CREATOR = new Parcelable.Creator<DSSwitchRecord>() {
        /* class com.huawei.nb.model.collectencrypt.DSSwitchRecord.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public DSSwitchRecord createFromParcel(Parcel parcel) {
            return new DSSwitchRecord(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public DSSwitchRecord[] newArray(int i) {
            return new DSSwitchRecord[i];
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

    @Override // com.huawei.odmf.core.AManagedObject, android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getDatabaseName() {
        return "dsCollectEncrypt";
    }

    public String getDatabaseVersion() {
        return "0.0.14";
    }

    public int getDatabaseVersionCode() {
        return 14;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getEntityName() {
        return "com.huawei.nb.model.collectencrypt.DSSwitchRecord";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public DSSwitchRecord(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Long l = null;
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

    public DSSwitchRecord(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.id = null;
            parcel.readInt();
        } else {
            this.id = Integer.valueOf(parcel.readInt());
        }
        this.timeStamp = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.switchStatus = parcel.readByte() == 0 ? null : parcel.readString();
        this.switchName = parcel.readByte() == 0 ? null : parcel.readString();
        this.packageName = parcel.readByte() == 0 ? null : parcel.readString();
        this.reserved1 = parcel.readByte() == 0 ? null : parcel.readString();
        this.reserved2 = parcel.readByte() == 0 ? null : parcel.readString();
        this.reserved3 = parcel.readByte() == 0 ? null : parcel.readString();
        this.reserved4 = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private DSSwitchRecord(Integer num, Long l, String str, String str2, String str3, String str4, String str5, String str6, String str7) {
        this.id = num;
        this.timeStamp = l;
        this.switchStatus = str;
        this.switchName = str2;
        this.packageName = str3;
        this.reserved1 = str4;
        this.reserved2 = str5;
        this.reserved3 = str6;
        this.reserved4 = str7;
    }

    public DSSwitchRecord() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer num) {
        this.id = num;
        setValue();
    }

    public Long getTimeStamp() {
        return this.timeStamp;
    }

    public void setTimeStamp(Long l) {
        this.timeStamp = l;
        setValue();
    }

    public String getSwitchStatus() {
        return this.switchStatus;
    }

    public void setSwitchStatus(String str) {
        this.switchStatus = str;
        setValue();
    }

    public String getSwitchName() {
        return this.switchName;
    }

    public void setSwitchName(String str) {
        this.switchName = str;
        setValue();
    }

    public String getPackageName() {
        return this.packageName;
    }

    public void setPackageName(String str) {
        this.packageName = str;
        setValue();
    }

    public String getReserved1() {
        return this.reserved1;
    }

    public void setReserved1(String str) {
        this.reserved1 = str;
        setValue();
    }

    public String getReserved2() {
        return this.reserved2;
    }

    public void setReserved2(String str) {
        this.reserved2 = str;
        setValue();
    }

    public String getReserved3() {
        return this.reserved3;
    }

    public void setReserved3(String str) {
        this.reserved3 = str;
        setValue();
    }

    public String getReserved4() {
        return this.reserved4;
    }

    public void setReserved4(String str) {
        this.reserved4 = str;
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
        if (this.timeStamp != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.timeStamp.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.switchStatus != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.switchStatus);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.switchName != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.switchName);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.packageName != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.packageName);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.reserved1 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.reserved1);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.reserved2 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.reserved2);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.reserved3 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.reserved3);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.reserved4 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.reserved4);
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<DSSwitchRecord> getHelper() {
        return DSSwitchRecordHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "DSSwitchRecord { id: " + this.id + ", timeStamp: " + this.timeStamp + ", switchStatus: " + this.switchStatus + ", switchName: " + this.switchName + ", packageName: " + this.packageName + ", reserved1: " + this.reserved1 + ", reserved2: " + this.reserved2 + ", reserved3: " + this.reserved3 + ", reserved4: " + this.reserved4 + " }";
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
