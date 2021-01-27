package com.huawei.nb.model.ips;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import com.huawei.odmf.utils.BindUtils;
import java.sql.Blob;

public class IndoorTrack extends AManagedObject {
    public static final Parcelable.Creator<IndoorTrack> CREATOR = new Parcelable.Creator<IndoorTrack>() {
        /* class com.huawei.nb.model.ips.IndoorTrack.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public IndoorTrack createFromParcel(Parcel parcel) {
            return new IndoorTrack(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public IndoorTrack[] newArray(int i) {
            return new IndoorTrack[i];
        }
    };
    private String areaCode;
    private Blob dataAdd;
    private Blob dataDel;
    private Blob dataUpdate;
    private Short floorNum;
    private Integer id;
    private Double latitude;
    private Double longitude;
    private String reserved;
    private Long timestamp;
    private Integer type;

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
        return "com.huawei.nb.model.ips.IndoorTrack";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public IndoorTrack(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Long l = null;
        this.id = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.areaCode = cursor.getString(2);
        this.type = cursor.isNull(3) ? null : Integer.valueOf(cursor.getInt(3));
        this.floorNum = cursor.isNull(4) ? null : Short.valueOf(cursor.getShort(4));
        this.longitude = cursor.isNull(5) ? null : Double.valueOf(cursor.getDouble(5));
        this.latitude = cursor.isNull(6) ? null : Double.valueOf(cursor.getDouble(6));
        this.dataAdd = cursor.isNull(7) ? null : new com.huawei.odmf.data.Blob(cursor.getBlob(7));
        this.dataDel = cursor.isNull(8) ? null : new com.huawei.odmf.data.Blob(cursor.getBlob(8));
        this.dataUpdate = cursor.isNull(9) ? null : new com.huawei.odmf.data.Blob(cursor.getBlob(9));
        this.timestamp = !cursor.isNull(10) ? Long.valueOf(cursor.getLong(10)) : l;
        this.reserved = cursor.getString(11);
    }

    public IndoorTrack(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.id = null;
            parcel.readInt();
        } else {
            this.id = Integer.valueOf(parcel.readInt());
        }
        this.areaCode = parcel.readByte() == 0 ? null : parcel.readString();
        this.type = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.floorNum = parcel.readByte() == 0 ? null : Short.valueOf((short) parcel.readInt());
        this.longitude = parcel.readByte() == 0 ? null : Double.valueOf(parcel.readDouble());
        this.latitude = parcel.readByte() == 0 ? null : Double.valueOf(parcel.readDouble());
        this.dataAdd = parcel.readByte() == 0 ? null : com.huawei.odmf.data.Blob.CREATOR.createFromParcel(parcel);
        this.dataDel = parcel.readByte() == 0 ? null : com.huawei.odmf.data.Blob.CREATOR.createFromParcel(parcel);
        this.dataUpdate = parcel.readByte() == 0 ? null : com.huawei.odmf.data.Blob.CREATOR.createFromParcel(parcel);
        this.timestamp = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.reserved = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private IndoorTrack(Integer num, String str, Integer num2, Short sh, Double d, Double d2, Blob blob, Blob blob2, Blob blob3, Long l, String str2) {
        this.id = num;
        this.areaCode = str;
        this.type = num2;
        this.floorNum = sh;
        this.longitude = d;
        this.latitude = d2;
        this.dataAdd = blob;
        this.dataDel = blob2;
        this.dataUpdate = blob3;
        this.timestamp = l;
        this.reserved = str2;
    }

    public IndoorTrack() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer num) {
        this.id = num;
        setValue();
    }

    public String getAreaCode() {
        return this.areaCode;
    }

    public void setAreaCode(String str) {
        this.areaCode = str;
        setValue();
    }

    public Integer getType() {
        return this.type;
    }

    public void setType(Integer num) {
        this.type = num;
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

    public Blob getDataAdd() {
        return this.dataAdd;
    }

    public void setDataAdd(Blob blob) {
        this.dataAdd = blob;
        setValue();
    }

    public Blob getDataDel() {
        return this.dataDel;
    }

    public void setDataDel(Blob blob) {
        this.dataDel = blob;
        setValue();
    }

    public Blob getDataUpdate() {
        return this.dataUpdate;
    }

    public void setDataUpdate(Blob blob) {
        this.dataUpdate = blob;
        setValue();
    }

    public Long getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(Long l) {
        this.timestamp = l;
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
        if (this.areaCode != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.areaCode);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.type != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.type.intValue());
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
        if (this.dataAdd != null) {
            parcel.writeByte((byte) 1);
            Blob blob = this.dataAdd;
            if (blob instanceof com.huawei.odmf.data.Blob) {
                ((com.huawei.odmf.data.Blob) blob).writeToParcel(parcel, 0);
            } else {
                parcel.writeByteArray(BindUtils.bindBlob(blob));
            }
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.dataDel != null) {
            parcel.writeByte((byte) 1);
            Blob blob2 = this.dataDel;
            if (blob2 instanceof com.huawei.odmf.data.Blob) {
                ((com.huawei.odmf.data.Blob) blob2).writeToParcel(parcel, 0);
            } else {
                parcel.writeByteArray(BindUtils.bindBlob(blob2));
            }
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.dataUpdate != null) {
            parcel.writeByte((byte) 1);
            Blob blob3 = this.dataUpdate;
            if (blob3 instanceof com.huawei.odmf.data.Blob) {
                ((com.huawei.odmf.data.Blob) blob3).writeToParcel(parcel, 0);
            } else {
                parcel.writeByteArray(BindUtils.bindBlob(blob3));
            }
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.timestamp != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.timestamp.longValue());
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
    public AEntityHelper<IndoorTrack> getHelper() {
        return IndoorTrackHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "IndoorTrack { id: " + this.id + ", areaCode: " + this.areaCode + ", type: " + this.type + ", floorNum: " + this.floorNum + ", longitude: " + this.longitude + ", latitude: " + this.latitude + ", dataAdd: " + this.dataAdd + ", dataDel: " + this.dataDel + ", dataUpdate: " + this.dataUpdate + ", timestamp: " + this.timestamp + ", reserved: " + this.reserved + " }";
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
