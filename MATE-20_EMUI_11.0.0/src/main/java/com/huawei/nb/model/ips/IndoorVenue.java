package com.huawei.nb.model.ips;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class IndoorVenue extends AManagedObject {
    public static final Parcelable.Creator<IndoorVenue> CREATOR = new Parcelable.Creator<IndoorVenue>() {
        /* class com.huawei.nb.model.ips.IndoorVenue.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public IndoorVenue createFromParcel(Parcel parcel) {
            return new IndoorVenue(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public IndoorVenue[] newArray(int i) {
            return new IndoorVenue[i];
        }
    };
    private String blockId;
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
        return "com.huawei.nb.model.ips.IndoorVenue";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public IndoorVenue(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Double d = null;
        this.id = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.venueId = cursor.getString(2);
        this.blockId = cursor.getString(3);
        this.longitude = cursor.isNull(4) ? null : Double.valueOf(cursor.getDouble(4));
        this.latitude = !cursor.isNull(5) ? Double.valueOf(cursor.getDouble(5)) : d;
        this.reserved = cursor.getString(6);
    }

    public IndoorVenue(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.id = null;
            parcel.readInt();
        } else {
            this.id = Integer.valueOf(parcel.readInt());
        }
        this.venueId = parcel.readByte() == 0 ? null : parcel.readString();
        this.blockId = parcel.readByte() == 0 ? null : parcel.readString();
        this.longitude = parcel.readByte() == 0 ? null : Double.valueOf(parcel.readDouble());
        this.latitude = parcel.readByte() == 0 ? null : Double.valueOf(parcel.readDouble());
        this.reserved = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private IndoorVenue(Integer num, String str, String str2, Double d, Double d2, String str3) {
        this.id = num;
        this.venueId = str;
        this.blockId = str2;
        this.longitude = d;
        this.latitude = d2;
        this.reserved = str3;
    }

    public IndoorVenue() {
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

    public String getBlockId() {
        return this.blockId;
    }

    public void setBlockId(String str) {
        this.blockId = str;
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
        if (this.blockId != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.blockId);
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
        if (this.reserved != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.reserved);
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<IndoorVenue> getHelper() {
        return IndoorVenueHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "IndoorVenue { id: " + this.id + ", venueId: " + this.venueId + ", blockId: " + this.blockId + ", longitude: " + this.longitude + ", latitude: " + this.latitude + ", reserved: " + this.reserved + " }";
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
