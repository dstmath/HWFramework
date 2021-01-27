package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class RawAppDataOrigin extends AManagedObject {
    public static final Parcelable.Creator<RawAppDataOrigin> CREATOR = new Parcelable.Creator<RawAppDataOrigin>() {
        /* class com.huawei.nb.model.collectencrypt.RawAppDataOrigin.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public RawAppDataOrigin createFromParcel(Parcel parcel) {
            return new RawAppDataOrigin(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public RawAppDataOrigin[] newArray(int i) {
            return new RawAppDataOrigin[i];
        }
    };
    private String column1;
    private String column2;
    private Long dataSerialNumber;
    private Integer id;
    private String jsonData;
    private String packageName;
    private Long timestamp;

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
        return "com.huawei.nb.model.collectencrypt.RawAppDataOrigin";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public RawAppDataOrigin(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Long l = null;
        this.id = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.dataSerialNumber = cursor.isNull(2) ? null : Long.valueOf(cursor.getLong(2));
        this.timestamp = !cursor.isNull(3) ? Long.valueOf(cursor.getLong(3)) : l;
        this.packageName = cursor.getString(4);
        this.jsonData = cursor.getString(5);
        this.column1 = cursor.getString(6);
        this.column2 = cursor.getString(7);
    }

    public RawAppDataOrigin(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.id = null;
            parcel.readInt();
        } else {
            this.id = Integer.valueOf(parcel.readInt());
        }
        this.dataSerialNumber = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.timestamp = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.packageName = parcel.readByte() == 0 ? null : parcel.readString();
        this.jsonData = parcel.readByte() == 0 ? null : parcel.readString();
        this.column1 = parcel.readByte() == 0 ? null : parcel.readString();
        this.column2 = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private RawAppDataOrigin(Integer num, Long l, Long l2, String str, String str2, String str3, String str4) {
        this.id = num;
        this.dataSerialNumber = l;
        this.timestamp = l2;
        this.packageName = str;
        this.jsonData = str2;
        this.column1 = str3;
        this.column2 = str4;
    }

    public RawAppDataOrigin() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer num) {
        this.id = num;
        setValue();
    }

    public Long getDataSerialNumber() {
        return this.dataSerialNumber;
    }

    public void setDataSerialNumber(Long l) {
        this.dataSerialNumber = l;
        setValue();
    }

    public Long getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(Long l) {
        this.timestamp = l;
        setValue();
    }

    public String getPackageName() {
        return this.packageName;
    }

    public void setPackageName(String str) {
        this.packageName = str;
        setValue();
    }

    public String getJsonData() {
        return this.jsonData;
    }

    public void setJsonData(String str) {
        this.jsonData = str;
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
        if (this.dataSerialNumber != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.dataSerialNumber.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.timestamp != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.timestamp.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.packageName != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.packageName);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.jsonData != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.jsonData);
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
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<RawAppDataOrigin> getHelper() {
        return RawAppDataOriginHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "RawAppDataOrigin { id: " + this.id + ", dataSerialNumber: " + this.dataSerialNumber + ", timestamp: " + this.timestamp + ", packageName: " + this.packageName + ", jsonData: " + this.jsonData + ", column1: " + this.column1 + ", column2: " + this.column2 + " }";
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
