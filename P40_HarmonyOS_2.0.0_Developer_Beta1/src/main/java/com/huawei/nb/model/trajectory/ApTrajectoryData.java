package com.huawei.nb.model.trajectory;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class ApTrajectoryData extends AManagedObject {
    public static final Parcelable.Creator<ApTrajectoryData> CREATOR = new Parcelable.Creator<ApTrajectoryData>() {
        /* class com.huawei.nb.model.trajectory.ApTrajectoryData.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ApTrajectoryData createFromParcel(Parcel parcel) {
            return new ApTrajectoryData(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public ApTrajectoryData[] newArray(int i) {
            return new ApTrajectoryData[i];
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

    @Override // com.huawei.odmf.core.AManagedObject, android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getDatabaseName() {
        return "dsServiceMetaData";
    }

    public String getDatabaseVersion() {
        return "0.0.13";
    }

    public int getDatabaseVersionCode() {
        return 13;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getEntityName() {
        return "com.huawei.nb.model.trajectory.ApTrajectoryData";
    }

    public String getEntityVersion() {
        return "0.0.2";
    }

    public int getEntityVersionCode() {
        return 2;
    }

    public ApTrajectoryData(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Integer num = null;
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

    public ApTrajectoryData(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.id = null;
            parcel.readInt();
        } else {
            this.id = Integer.valueOf(parcel.readInt());
        }
        this.timeStamp = parcel.readByte() == 0 ? null : new Date(parcel.readLong());
        this.loncationType = parcel.readByte() == 0 ? null : Character.valueOf(parcel.createCharArray()[0]);
        this.longitude = parcel.readByte() == 0 ? null : Double.valueOf(parcel.readDouble());
        this.latitude = parcel.readByte() == 0 ? null : Double.valueOf(parcel.readDouble());
        this.cellId = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.cellLac = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.cellRssi = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.wifiBssId = parcel.readByte() == 0 ? null : parcel.readString();
        this.wifiRssi = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.reserved1 = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.reserved2 = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private ApTrajectoryData(Integer num, Date date, Character ch, Double d, Double d2, Integer num2, Integer num3, Integer num4, String str, Integer num5, Integer num6, String str2) {
        this.id = num;
        this.timeStamp = date;
        this.loncationType = ch;
        this.longitude = d;
        this.latitude = d2;
        this.cellId = num2;
        this.cellLac = num3;
        this.cellRssi = num4;
        this.wifiBssId = str;
        this.wifiRssi = num5;
        this.reserved1 = num6;
        this.reserved2 = str2;
    }

    public ApTrajectoryData() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer num) {
        this.id = num;
        setValue();
    }

    public Date getTimeStamp() {
        return this.timeStamp;
    }

    public void setTimeStamp(Date date) {
        this.timeStamp = date;
        setValue();
    }

    public Character getLoncationType() {
        return this.loncationType;
    }

    public void setLoncationType(Character ch) {
        this.loncationType = ch;
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

    public Integer getCellId() {
        return this.cellId;
    }

    public void setCellId(Integer num) {
        this.cellId = num;
        setValue();
    }

    public Integer getCellLac() {
        return this.cellLac;
    }

    public void setCellLac(Integer num) {
        this.cellLac = num;
        setValue();
    }

    public Integer getCellRssi() {
        return this.cellRssi;
    }

    public void setCellRssi(Integer num) {
        this.cellRssi = num;
        setValue();
    }

    public String getWifiBssId() {
        return this.wifiBssId;
    }

    public void setWifiBssId(String str) {
        this.wifiBssId = str;
        setValue();
    }

    public Integer getWifiRssi() {
        return this.wifiRssi;
    }

    public void setWifiRssi(Integer num) {
        this.wifiRssi = num;
        setValue();
    }

    public Integer getReserved1() {
        return this.reserved1;
    }

    public void setReserved1(Integer num) {
        this.reserved1 = num;
        setValue();
    }

    public String getReserved2() {
        return this.reserved2;
    }

    public void setReserved2(String str) {
        this.reserved2 = str;
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
            parcel.writeLong(this.timeStamp.getTime());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.loncationType != null) {
            parcel.writeByte((byte) 1);
            parcel.writeCharArray(new char[]{this.loncationType.charValue()});
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
        if (this.cellId != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.cellId.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.cellLac != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.cellLac.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.cellRssi != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.cellRssi.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.wifiBssId != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.wifiBssId);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.wifiRssi != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.wifiRssi.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.reserved1 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.reserved1.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.reserved2 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.reserved2);
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<ApTrajectoryData> getHelper() {
        return ApTrajectoryDataHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "ApTrajectoryData { id: " + this.id + ", timeStamp: " + this.timeStamp + ", loncationType: " + this.loncationType + ", longitude: " + this.longitude + ", latitude: " + this.latitude + ", cellId: " + this.cellId + ", cellLac: " + this.cellLac + ", cellRssi: " + this.cellRssi + ", wifiBssId: " + this.wifiBssId + ", wifiRssi: " + this.wifiRssi + ", reserved1: " + this.reserved1 + ", reserved2: " + this.reserved2 + " }";
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
