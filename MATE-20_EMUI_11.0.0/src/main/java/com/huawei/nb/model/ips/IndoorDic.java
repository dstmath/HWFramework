package com.huawei.nb.model.ips;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import com.huawei.odmf.utils.BindUtils;
import java.sql.Blob;

public class IndoorDic extends AManagedObject {
    public static final Parcelable.Creator<IndoorDic> CREATOR = new Parcelable.Creator<IndoorDic>() {
        /* class com.huawei.nb.model.ips.IndoorDic.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public IndoorDic createFromParcel(Parcel parcel) {
            return new IndoorDic(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public IndoorDic[] newArray(int i) {
            return new IndoorDic[i];
        }
    };
    private Integer id;
    private Blob key;
    private String reserved;
    private Integer type;
    private Short value;
    private String venueId;

    @Override // com.huawei.odmf.core.AManagedObject, android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getDatabaseName() {
        return "dsIndoorFingerprint";
    }

    public String getDatabaseVersion() {
        return "0.0.5";
    }

    public int getDatabaseVersionCode() {
        return 5;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getEntityName() {
        return "com.huawei.nb.model.ips.IndoorDic";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public IndoorDic(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Short sh = null;
        this.id = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.venueId = cursor.getString(2);
        this.type = cursor.isNull(3) ? null : Integer.valueOf(cursor.getInt(3));
        this.key = cursor.isNull(4) ? null : new com.huawei.odmf.data.Blob(cursor.getBlob(4));
        this.value = !cursor.isNull(5) ? Short.valueOf(cursor.getShort(5)) : sh;
        this.reserved = cursor.getString(6);
    }

    public IndoorDic(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.id = null;
            parcel.readInt();
        } else {
            this.id = Integer.valueOf(parcel.readInt());
        }
        this.venueId = parcel.readByte() == 0 ? null : parcel.readString();
        this.type = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.key = parcel.readByte() == 0 ? null : com.huawei.odmf.data.Blob.CREATOR.createFromParcel(parcel);
        this.value = parcel.readByte() == 0 ? null : Short.valueOf((short) parcel.readInt());
        this.reserved = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private IndoorDic(Integer num, String str, Integer num2, Blob blob, Short sh, String str2) {
        this.id = num;
        this.venueId = str;
        this.type = num2;
        this.key = blob;
        this.value = sh;
        this.reserved = str2;
    }

    public IndoorDic() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer num) {
        this.id = num;
        setValue();
    }

    public String getVenueId() {
        return this.venueId;
    }

    public void setVenueId(String str) {
        this.venueId = str;
        setValue();
    }

    public Integer getType() {
        return this.type;
    }

    public void setType(Integer num) {
        this.type = num;
        setValue();
    }

    public Blob getKey() {
        return this.key;
    }

    public void setKey(Blob blob) {
        this.key = blob;
        setValue();
    }

    public Short getValue() {
        return this.value;
    }

    public void setValue(Short sh) {
        this.value = sh;
        setValue();
    }

    public String getReserved() {
        return this.reserved;
    }

    public void setReserved(String str) {
        this.reserved = str;
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
        if (this.venueId != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.venueId);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.type != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.type.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.key != null) {
            parcel.writeByte((byte) 1);
            Blob blob = this.key;
            if (blob instanceof com.huawei.odmf.data.Blob) {
                ((com.huawei.odmf.data.Blob) blob).writeToParcel(parcel, 0);
            } else {
                parcel.writeByteArray(BindUtils.bindBlob(blob));
            }
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.value != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.value.shortValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.reserved != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.reserved);
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<IndoorDic> getHelper() {
        return IndoorDicHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "IndoorDic { id: " + this.id + ", venueId: " + this.venueId + ", type: " + this.type + ", key: " + this.key + ", value: " + this.value + ", reserved: " + this.reserved + " }";
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
