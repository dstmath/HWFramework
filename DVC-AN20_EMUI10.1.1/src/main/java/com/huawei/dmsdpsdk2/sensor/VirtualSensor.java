package com.huawei.dmsdpsdk2.sensor;

import android.os.Parcel;
import android.os.Parcelable;

public class VirtualSensor implements Parcelable {
    public static final Parcelable.Creator<VirtualSensor> CREATOR = new Parcelable.Creator<VirtualSensor>() {
        /* class com.huawei.dmsdpsdk2.sensor.VirtualSensor.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public VirtualSensor createFromParcel(Parcel in) {
            return new VirtualSensor(in);
        }

        @Override // android.os.Parcelable.Creator
        public VirtualSensor[] newArray(int size) {
            return new VirtualSensor[size];
        }
    };
    public static final String STRING_TYPE_HEART_RATE = "huawei.dmsdp.sensor.heart_rate";
    public static final String STRING_TYPE_PPG = "huawei.dmsdp.sensor.ppg";
    public static final int TYPE_HEART_RATE = 1;
    public static final int TYPE_HEART_RATE_PPG = 2;
    private String mDeviceId;
    private int mSensorId;
    private int mSensorType;

    public VirtualSensor() {
    }

    public VirtualSensor(String deviceId, int sensorId, int sensorType) {
        this.mDeviceId = deviceId;
        this.mSensorId = sensorId;
        this.mSensorType = sensorType;
    }

    protected VirtualSensor(Parcel in) {
        this.mDeviceId = in.readString();
        this.mSensorId = in.readInt();
        this.mSensorType = in.readInt();
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mDeviceId);
        dest.writeInt(this.mSensorId);
        dest.writeInt(this.mSensorType);
    }

    public int describeContents() {
        return 0;
    }

    public String getDeviceId() {
        return this.mDeviceId;
    }

    public int getSensorId() {
        return this.mSensorId;
    }

    public int getSensorType() {
        return this.mSensorType;
    }

    public void setDeviceId(String deviceId) {
        this.mDeviceId = deviceId;
    }

    public void setSensorId(int sensorId) {
        this.mSensorId = sensorId;
    }

    public void setSensorType(int sensorType) {
        this.mSensorType = sensorType;
    }
}
