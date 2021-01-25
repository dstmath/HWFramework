package com.huawei.nb.model.ips;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import com.huawei.odmf.utils.BindUtils;
import java.sql.Blob;

public class IndoorDotSet extends AManagedObject {
    public static final Parcelable.Creator<IndoorDotSet> CREATOR = new Parcelable.Creator<IndoorDotSet>() {
        /* class com.huawei.nb.model.ips.IndoorDotSet.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public IndoorDotSet createFromParcel(Parcel parcel) {
            return new IndoorDotSet(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public IndoorDotSet[] newArray(int i) {
            return new IndoorDotSet[i];
        }
    };
    private Blob data;
    private Integer dataType;
    private Short floorNum;
    private Integer id;
    private Double latitude;
    private Double longitude;
    private String reserved;
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
        return "com.huawei.nb.model.ips.IndoorDotSet";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public IndoorDotSet(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        com.huawei.odmf.data.Blob blob = null;
        this.id = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.venueId = cursor.getString(2);
        this.floorNum = cursor.isNull(3) ? null : Short.valueOf(cursor.getShort(3));
        this.longitude = cursor.isNull(4) ? null : Double.valueOf(cursor.getDouble(4));
        this.latitude = cursor.isNull(5) ? null : Double.valueOf(cursor.getDouble(5));
        this.dataType = cursor.isNull(6) ? null : Integer.valueOf(cursor.getInt(6));
        this.data = !cursor.isNull(7) ? new com.huawei.odmf.data.Blob(cursor.getBlob(7)) : blob;
        this.reserved = cursor.getString(8);
    }

    public IndoorDotSet(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.id = null;
            parcel.readInt();
        } else {
            this.id = Integer.valueOf(parcel.readInt());
        }
        this.venueId = parcel.readByte() == 0 ? null : parcel.readString();
        this.floorNum = parcel.readByte() == 0 ? null : Short.valueOf((short) parcel.readInt());
        this.longitude = parcel.readByte() == 0 ? null : Double.valueOf(parcel.readDouble());
        this.latitude = parcel.readByte() == 0 ? null : Double.valueOf(parcel.readDouble());
        this.dataType = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.data = parcel.readByte() == 0 ? null : com.huawei.odmf.data.Blob.CREATOR.createFromParcel(parcel);
        this.reserved = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private IndoorDotSet(Integer num, String str, Short sh, Double d, Double d2, Integer num2, Blob blob, String str2) {
        this.id = num;
        this.venueId = str;
        this.floorNum = sh;
        this.longitude = d;
        this.latitude = d2;
        this.dataType = num2;
        this.data = blob;
        this.reserved = str2;
    }

    public IndoorDotSet() {
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

    public Short getFloorNum() {
        return this.floorNum;
    }

    public void setFloorNum(Short sh) {
        this.floorNum = sh;
        setValue();
    }

    public Double getLongitude() {
        return this.longitude;
    }

    public void setLongitude(Double d) {
        this.longitude = d;
        setValue();
    }

    public Double getLatitude() {
        return this.latitude;
    }

    public void setLatitude(Double d) {
        this.latitude = d;
        setValue();
    }

    public Integer getDataType() {
        return this.dataType;
    }

    public void setDataType(Integer num) {
        this.dataType = num;
        setValue();
    }

    public Blob getData() {
        return this.data;
    }

    public void setData(Blob blob) {
        this.data = blob;
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
        if (this.floorNum != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.floorNum.shortValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.longitude != null) {
            parcel.writeByte((byte) 1);
            parcel.writeDouble(this.longitude.doubleValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.latitude != null) {
            parcel.writeByte((byte) 1);
            parcel.writeDouble(this.latitude.doubleValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.dataType != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.dataType.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.data != null) {
            parcel.writeByte((byte) 1);
            Blob blob = this.data;
            if (blob instanceof com.huawei.odmf.data.Blob) {
                ((com.huawei.odmf.data.Blob) blob).writeToParcel(parcel, 0);
            } else {
                parcel.writeByteArray(BindUtils.bindBlob(blob));
            }
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
    public AEntityHelper<IndoorDotSet> getHelper() {
        return IndoorDotSetHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "IndoorDotSet { id: " + this.id + ", venueId: " + this.venueId + ", floorNum: " + this.floorNum + ", longitude: " + this.longitude + ", latitude: " + this.latitude + ", dataType: " + this.dataType + ", data: " + this.data + ", reserved: " + this.reserved + " }";
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
