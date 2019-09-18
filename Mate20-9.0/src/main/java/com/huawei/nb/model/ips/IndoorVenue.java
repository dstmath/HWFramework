package com.huawei.nb.model.ips;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class IndoorVenue extends AManagedObject {
    public static final Parcelable.Creator<IndoorVenue> CREATOR = new Parcelable.Creator<IndoorVenue>() {
        public IndoorVenue createFromParcel(Parcel in) {
            return new IndoorVenue(in);
        }

        public IndoorVenue[] newArray(int size) {
            return new IndoorVenue[size];
        }
    };
    private String blockId;
    private Integer id;
    private Double latitude;
    private Double longitude;
    private String reserved;
    private String venueId;

    public IndoorVenue(Cursor cursor) {
        Double d = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.id = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.venueId = cursor.getString(2);
        this.blockId = cursor.getString(3);
        this.longitude = cursor.isNull(4) ? null : Double.valueOf(cursor.getDouble(4));
        this.latitude = !cursor.isNull(5) ? Double.valueOf(cursor.getDouble(5)) : d;
        this.reserved = cursor.getString(6);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public IndoorVenue(Parcel in) {
        super(in);
        String str = null;
        if (in.readByte() == 0) {
            this.id = null;
            in.readInt();
        } else {
            this.id = Integer.valueOf(in.readInt());
        }
        this.venueId = in.readByte() == 0 ? null : in.readString();
        this.blockId = in.readByte() == 0 ? null : in.readString();
        this.longitude = in.readByte() == 0 ? null : Double.valueOf(in.readDouble());
        this.latitude = in.readByte() == 0 ? null : Double.valueOf(in.readDouble());
        this.reserved = in.readByte() != 0 ? in.readString() : str;
    }

    private IndoorVenue(Integer id2, String venueId2, String blockId2, Double longitude2, Double latitude2, String reserved2) {
        this.id = id2;
        this.venueId = venueId2;
        this.blockId = blockId2;
        this.longitude = longitude2;
        this.latitude = latitude2;
        this.reserved = reserved2;
    }

    public IndoorVenue() {
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

    public String getVenueId() {
        return this.venueId;
    }

    public void setVenueId(String venueId2) {
        this.venueId = venueId2;
        setValue();
    }

    public String getBlockId() {
        return this.blockId;
    }

    public void setBlockId(String blockId2) {
        this.blockId = blockId2;
        setValue();
    }

    public Double getLongitude() {
        return this.longitude;
    }

    public void setLongitude(Double longitude2) {
        this.longitude = longitude2;
        setValue();
    }

    public Double getLatitude() {
        return this.latitude;
    }

    public void setLatitude(Double latitude2) {
        this.latitude = latitude2;
        setValue();
    }

    public String getReserved() {
        return this.reserved;
    }

    public void setReserved(String reserved2) {
        this.reserved = reserved2;
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
        if (this.venueId != null) {
            out.writeByte((byte) 1);
            out.writeString(this.venueId);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.blockId != null) {
            out.writeByte((byte) 1);
            out.writeString(this.blockId);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.longitude != null) {
            out.writeByte((byte) 1);
            out.writeDouble(this.longitude.doubleValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.latitude != null) {
            out.writeByte((byte) 1);
            out.writeDouble(this.latitude.doubleValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.reserved != null) {
            out.writeByte((byte) 1);
            out.writeString(this.reserved);
            return;
        }
        out.writeByte((byte) 0);
    }

    public AEntityHelper<IndoorVenue> getHelper() {
        return IndoorVenueHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.ips.IndoorVenue";
    }

    public String getDatabaseName() {
        return "dsIndoorFingerprint";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("IndoorVenue { id: ").append(this.id);
        sb.append(", venueId: ").append(this.venueId);
        sb.append(", blockId: ").append(this.blockId);
        sb.append(", longitude: ").append(this.longitude);
        sb.append(", latitude: ").append(this.latitude);
        sb.append(", reserved: ").append(this.reserved);
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
        return "0.0.5";
    }

    public int getDatabaseVersionCode() {
        return 5;
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }
}
