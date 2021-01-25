package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class DSHiTouchPhoneToken extends AManagedObject {
    public static final Parcelable.Creator<DSHiTouchPhoneToken> CREATOR = new Parcelable.Creator<DSHiTouchPhoneToken>() {
        /* class com.huawei.nb.model.collectencrypt.DSHiTouchPhoneToken.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public DSHiTouchPhoneToken createFromParcel(Parcel parcel) {
            return new DSHiTouchPhoneToken(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public DSHiTouchPhoneToken[] newArray(int i) {
            return new DSHiTouchPhoneToken[i];
        }
    };
    private Integer compat = 0;
    private Integer id;
    private String reserved0;
    private String reserved1;
    private String reserved2;
    private String reserved3;
    private String reserved4;
    private String reserved5;
    private Long timestamp = 0L;
    private String tokencodes;

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
        return "com.huawei.nb.model.collectencrypt.DSHiTouchPhoneToken";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public DSHiTouchPhoneToken(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Integer num = null;
        this.id = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.timestamp = cursor.isNull(2) ? null : Long.valueOf(cursor.getLong(2));
        this.compat = !cursor.isNull(3) ? Integer.valueOf(cursor.getInt(3)) : num;
        this.tokencodes = cursor.getString(4);
        this.reserved0 = cursor.getString(5);
        this.reserved1 = cursor.getString(6);
        this.reserved2 = cursor.getString(7);
        this.reserved3 = cursor.getString(8);
        this.reserved4 = cursor.getString(9);
        this.reserved5 = cursor.getString(10);
    }

    public DSHiTouchPhoneToken(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.id = null;
            parcel.readInt();
        } else {
            this.id = Integer.valueOf(parcel.readInt());
        }
        this.timestamp = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.compat = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.tokencodes = parcel.readByte() == 0 ? null : parcel.readString();
        this.reserved0 = parcel.readByte() == 0 ? null : parcel.readString();
        this.reserved1 = parcel.readByte() == 0 ? null : parcel.readString();
        this.reserved2 = parcel.readByte() == 0 ? null : parcel.readString();
        this.reserved3 = parcel.readByte() == 0 ? null : parcel.readString();
        this.reserved4 = parcel.readByte() == 0 ? null : parcel.readString();
        this.reserved5 = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private DSHiTouchPhoneToken(Integer num, Long l, Integer num2, String str, String str2, String str3, String str4, String str5, String str6, String str7) {
        this.id = num;
        this.timestamp = l;
        this.compat = num2;
        this.tokencodes = str;
        this.reserved0 = str2;
        this.reserved1 = str3;
        this.reserved2 = str4;
        this.reserved3 = str5;
        this.reserved4 = str6;
        this.reserved5 = str7;
    }

    public DSHiTouchPhoneToken() {
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

    public Integer getCompat() {
        return this.compat;
    }

    public void setCompat(Integer num) {
        this.compat = num;
        setValue();
    }

    public String getTokencodes() {
        return this.tokencodes;
    }

    public void setTokencodes(String str) {
        this.tokencodes = str;
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
        if (this.compat != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.compat.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.tokencodes != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.tokencodes);
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
    public AEntityHelper<DSHiTouchPhoneToken> getHelper() {
        return DSHiTouchPhoneTokenHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "DSHiTouchPhoneToken { id: " + this.id + ", timestamp: " + this.timestamp + ", compat: " + this.compat + ", tokencodes: " + this.tokencodes + ", reserved0: " + this.reserved0 + ", reserved1: " + this.reserved1 + ", reserved2: " + this.reserved2 + ", reserved3: " + this.reserved3 + ", reserved4: " + this.reserved4 + ", reserved5: " + this.reserved5 + " }";
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
