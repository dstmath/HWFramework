package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class DSHiTouchSwitch extends AManagedObject {
    public static final Parcelable.Creator<DSHiTouchSwitch> CREATOR = new Parcelable.Creator<DSHiTouchSwitch>() {
        /* class com.huawei.nb.model.collectencrypt.DSHiTouchSwitch.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public DSHiTouchSwitch createFromParcel(Parcel parcel) {
            return new DSHiTouchSwitch(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public DSHiTouchSwitch[] newArray(int i) {
            return new DSHiTouchSwitch[i];
        }
    };
    private Integer digest = 0;
    private Integer express = 0;
    private Integer id;
    private String reserved0;
    private String reserved1;
    private String reserved2;
    private String reserved3;
    private String reserved4;
    private String reserved5;
    private Long timestamp = 0L;

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
        return "com.huawei.nb.model.collectencrypt.DSHiTouchSwitch";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public DSHiTouchSwitch(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Integer num = null;
        this.id = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.timestamp = cursor.isNull(2) ? null : Long.valueOf(cursor.getLong(2));
        this.digest = cursor.isNull(3) ? null : Integer.valueOf(cursor.getInt(3));
        this.express = !cursor.isNull(4) ? Integer.valueOf(cursor.getInt(4)) : num;
        this.reserved0 = cursor.getString(5);
        this.reserved1 = cursor.getString(6);
        this.reserved2 = cursor.getString(7);
        this.reserved3 = cursor.getString(8);
        this.reserved4 = cursor.getString(9);
        this.reserved5 = cursor.getString(10);
    }

    public DSHiTouchSwitch(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.id = null;
            parcel.readInt();
        } else {
            this.id = Integer.valueOf(parcel.readInt());
        }
        this.timestamp = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.digest = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.express = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.reserved0 = parcel.readByte() == 0 ? null : parcel.readString();
        this.reserved1 = parcel.readByte() == 0 ? null : parcel.readString();
        this.reserved2 = parcel.readByte() == 0 ? null : parcel.readString();
        this.reserved3 = parcel.readByte() == 0 ? null : parcel.readString();
        this.reserved4 = parcel.readByte() == 0 ? null : parcel.readString();
        this.reserved5 = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private DSHiTouchSwitch(Integer num, Long l, Integer num2, Integer num3, String str, String str2, String str3, String str4, String str5, String str6) {
        this.id = num;
        this.timestamp = l;
        this.digest = num2;
        this.express = num3;
        this.reserved0 = str;
        this.reserved1 = str2;
        this.reserved2 = str3;
        this.reserved3 = str4;
        this.reserved4 = str5;
        this.reserved5 = str6;
    }

    public DSHiTouchSwitch() {
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

    public Integer getDigest() {
        return this.digest;
    }

    public void setDigest(Integer num) {
        this.digest = num;
        setValue();
    }

    public Integer getExpress() {
        return this.express;
    }

    public void setExpress(Integer num) {
        this.express = num;
        setValue();
    }

    public String getReserved0() {
        return this.reserved0;
    }

    public void setReserved0(String str) {
        this.reserved0 = str;
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

    public String getReserved5() {
        return this.reserved5;
    }

    public void setReserved5(String str) {
        this.reserved5 = str;
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
        if (this.digest != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.digest.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.express != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.express.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.reserved0 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.reserved0);
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
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.reserved5 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.reserved5);
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<DSHiTouchSwitch> getHelper() {
        return DSHiTouchSwitchHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "DSHiTouchSwitch { id: " + this.id + ", timestamp: " + this.timestamp + ", digest: " + this.digest + ", express: " + this.express + ", reserved0: " + this.reserved0 + ", reserved1: " + this.reserved1 + ", reserved2: " + this.reserved2 + ", reserved3: " + this.reserved3 + ", reserved4: " + this.reserved4 + ", reserved5: " + this.reserved5 + " }";
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
