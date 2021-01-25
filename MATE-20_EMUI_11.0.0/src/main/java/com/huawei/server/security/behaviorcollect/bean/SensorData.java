package com.huawei.server.security.behaviorcollect.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class SensorData implements Parcelable {
    public static final Parcelable.Creator<SensorData> CREATOR = new Parcelable.Creator<SensorData>() {
        /* class com.huawei.server.security.behaviorcollect.bean.SensorData.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public SensorData createFromParcel(Parcel source) {
            return new SensorData(source);
        }

        @Override // android.os.Parcelable.Creator
        public SensorData[] newArray(int size) {
            return new SensorData[size];
        }
    };
    private double accX;
    private double accY;
    private double accZ;
    private long timestamp;
    private int type;

    public SensorData(long timestamp2, int type2, double accX2, double accY2, double accZ2) {
        this.timestamp = timestamp2;
        this.type = type2;
        this.accX = accX2;
        this.accY = accY2;
        this.accZ = accZ2;
    }

    public SensorData(SensorData sensorData) {
        this.timestamp = sensorData.timestamp;
        this.type = sensorData.type;
        this.accX = sensorData.accX;
        this.accY = sensorData.accY;
        this.accZ = sensorData.accZ;
    }

    public SensorData(Parcel source) {
        this.timestamp = source.readLong();
        this.type = source.readInt();
        this.accX = source.readDouble();
        this.accY = source.readDouble();
        this.accZ = source.readDouble();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.timestamp);
        dest.writeInt(this.type);
        dest.writeDouble(this.accX);
        dest.writeDouble(this.accY);
        dest.writeDouble(this.accZ);
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(long timestamp2) {
        this.timestamp = timestamp2;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type2) {
        this.type = type2;
    }

    public double getAccX() {
        return this.accX;
    }

    public void setAccX(double accX2) {
        this.accX = accX2;
    }

    public double getAccY() {
        return this.accY;
    }

    public void setAccY(double accY2) {
        this.accY = accY2;
    }

    public double getAccZ() {
        return this.accZ;
    }

    public void setAccZ(double accZ2) {
        this.accZ = accZ2;
    }
}
