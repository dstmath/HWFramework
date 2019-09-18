package com.huawei.nb.model.trajectory;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class ApTrajectoryData extends AManagedObject {
    public static final Parcelable.Creator<ApTrajectoryData> CREATOR = new Parcelable.Creator<ApTrajectoryData>() {
        public ApTrajectoryData createFromParcel(Parcel in) {
            return new ApTrajectoryData(in);
        }

        public ApTrajectoryData[] newArray(int size) {
            return new ApTrajectoryData[size];
        }
    };
    private Integer cellId;
    private Integer cellLac;
    private Integer cellRssi;
    private Integer id;
    private Double latitude;
    private Character loncationType;
    private Double longitude;
    private Integer reserved1;
    private String reserved2;
    private Date timeStamp;
    private String wifiBssId;
    private Integer wifiRssi;

    public ApTrajectoryData(Cursor cursor) {
        Integer num = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.id = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.timeStamp = cursor.isNull(2) ? null : new Date(cursor.getLong(2));
        this.loncationType = cursor.isNull(3) ? null : Character.valueOf(cursor.getString(3).charAt(0));
        this.longitude = cursor.isNull(4) ? null : Double.valueOf(cursor.getDouble(4));
        this.latitude = cursor.isNull(5) ? null : Double.valueOf(cursor.getDouble(5));
        this.cellId = cursor.isNull(6) ? null : Integer.valueOf(cursor.getInt(6));
        this.cellLac = cursor.isNull(7) ? null : Integer.valueOf(cursor.getInt(7));
        this.cellRssi = cursor.isNull(8) ? null : Integer.valueOf(cursor.getInt(8));
        this.wifiBssId = cursor.getString(9);
        this.wifiRssi = cursor.isNull(10) ? null : Integer.valueOf(cursor.getInt(10));
        this.reserved1 = !cursor.isNull(11) ? Integer.valueOf(cursor.getInt(11)) : num;
        this.reserved2 = cursor.getString(12);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public ApTrajectoryData(Parcel in) {
        super(in);
        String str = null;
        if (in.readByte() == 0) {
            this.id = null;
            in.readInt();
        } else {
            this.id = Integer.valueOf(in.readInt());
        }
        this.timeStamp = in.readByte() == 0 ? null : new Date(in.readLong());
        this.loncationType = in.readByte() == 0 ? null : Character.valueOf(in.createCharArray()[0]);
        this.longitude = in.readByte() == 0 ? null : Double.valueOf(in.readDouble());
        this.latitude = in.readByte() == 0 ? null : Double.valueOf(in.readDouble());
        this.cellId = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.cellLac = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.cellRssi = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.wifiBssId = in.readByte() == 0 ? null : in.readString();
        this.wifiRssi = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.reserved1 = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.reserved2 = in.readByte() != 0 ? in.readString() : str;
    }

    private ApTrajectoryData(Integer id2, Date timeStamp2, Character loncationType2, Double longitude2, Double latitude2, Integer cellId2, Integer cellLac2, Integer cellRssi2, String wifiBssId2, Integer wifiRssi2, Integer reserved12, String reserved22) {
        this.id = id2;
        this.timeStamp = timeStamp2;
        this.loncationType = loncationType2;
        this.longitude = longitude2;
        this.latitude = latitude2;
        this.cellId = cellId2;
        this.cellLac = cellLac2;
        this.cellRssi = cellRssi2;
        this.wifiBssId = wifiBssId2;
        this.wifiRssi = wifiRssi2;
        this.reserved1 = reserved12;
        this.reserved2 = reserved22;
    }

    public ApTrajectoryData() {
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

    public Date getTimeStamp() {
        return this.timeStamp;
    }

    public void setTimeStamp(Date timeStamp2) {
        this.timeStamp = timeStamp2;
        setValue();
    }

    public Character getLoncationType() {
        return this.loncationType;
    }

    public void setLoncationType(Character loncationType2) {
        this.loncationType = loncationType2;
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

    public Integer getCellId() {
        return this.cellId;
    }

    public void setCellId(Integer cellId2) {
        this.cellId = cellId2;
        setValue();
    }

    public Integer getCellLac() {
        return this.cellLac;
    }

    public void setCellLac(Integer cellLac2) {
        this.cellLac = cellLac2;
        setValue();
    }

    public Integer getCellRssi() {
        return this.cellRssi;
    }

    public void setCellRssi(Integer cellRssi2) {
        this.cellRssi = cellRssi2;
        setValue();
    }

    public String getWifiBssId() {
        return this.wifiBssId;
    }

    public void setWifiBssId(String wifiBssId2) {
        this.wifiBssId = wifiBssId2;
        setValue();
    }

    public Integer getWifiRssi() {
        return this.wifiRssi;
    }

    public void setWifiRssi(Integer wifiRssi2) {
        this.wifiRssi = wifiRssi2;
        setValue();
    }

    public Integer getReserved1() {
        return this.reserved1;
    }

    public void setReserved1(Integer reserved12) {
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
            out.writeLong(this.timeStamp.getTime());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.loncationType != null) {
            out.writeByte((byte) 1);
            out.writeCharArray(new char[]{this.loncationType.charValue()});
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
        if (this.cellId != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.cellId.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.cellLac != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.cellLac.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.cellRssi != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.cellRssi.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.wifiBssId != null) {
            out.writeByte((byte) 1);
            out.writeString(this.wifiBssId);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.wifiRssi != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.wifiRssi.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.reserved1 != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.reserved1.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.reserved2 != null) {
            out.writeByte((byte) 1);
            out.writeString(this.reserved2);
            return;
        }
        out.writeByte((byte) 0);
    }

    public AEntityHelper<ApTrajectoryData> getHelper() {
        return ApTrajectoryDataHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.trajectory.ApTrajectoryData";
    }

    public String getDatabaseName() {
        return "dsServiceMetaData";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("ApTrajectoryData { id: ").append(this.id);
        sb.append(", timeStamp: ").append(this.timeStamp);
        sb.append(", loncationType: ").append(this.loncationType);
        sb.append(", longitude: ").append(this.longitude);
        sb.append(", latitude: ").append(this.latitude);
        sb.append(", cellId: ").append(this.cellId);
        sb.append(", cellLac: ").append(this.cellLac);
        sb.append(", cellRssi: ").append(this.cellRssi);
        sb.append(", wifiBssId: ").append(this.wifiBssId);
        sb.append(", wifiRssi: ").append(this.wifiRssi);
        sb.append(", reserved1: ").append(this.reserved1);
        sb.append(", reserved2: ").append(this.reserved2);
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
        return "0.0.12";
    }

    public int getDatabaseVersionCode() {
        return 12;
    }

    public String getEntityVersion() {
        return "0.0.2";
    }

    public int getEntityVersionCode() {
        return 2;
    }
}
