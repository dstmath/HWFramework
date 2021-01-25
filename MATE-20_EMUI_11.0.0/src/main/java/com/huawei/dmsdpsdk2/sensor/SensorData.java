package com.huawei.dmsdpsdk2.sensor;

import android.os.Parcel;
import android.os.Parcelable;

public class SensorData implements Parcelable {
    public static final Parcelable.Creator<SensorData> CREATOR = new Parcelable.Creator<SensorData>() {
        /* class com.huawei.dmsdpsdk2.sensor.SensorData.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public SensorData createFromParcel(Parcel in) {
            return new SensorData(in);
        }

        @Override // android.os.Parcelable.Creator
        public SensorData[] newArray(int size) {
            return new SensorData[size];
        }
    };
    private int mAccuracy;
    private VirtualSensor mSensor = new VirtualSensor();
    private long mTimestamp;
    private final float[] mValues;

    public SensorData(int valueSize) {
        this.mValues = new float[valueSize];
    }

    protected SensorData(Parcel in) {
        this.mValues = in.createFloatArray();
        this.mAccuracy = in.readInt();
        this.mTimestamp = in.readLong();
        this.mSensor.setSensorId(in.readInt());
        this.mSensor.setDeviceId(in.readString());
        this.mSensor.setSensorType(in.readInt());
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloatArray(this.mValues);
        dest.writeInt(this.mAccuracy);
        dest.writeLong(this.mTimestamp);
        dest.writeInt(this.mSensor.getSensorId());
        dest.writeString(this.mSensor.getDeviceId());
        dest.writeInt(this.mSensor.getSensorType());
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public float[] getValues() {
        return (float[]) this.mValues.clone();
    }

    public VirtualSensor getSensor() {
        return this.mSensor;
    }

    public void setSensor(VirtualSensor sensor) {
        this.mSensor = sensor;
    }

    public int getAccuracy() {
        return this.mAccuracy;
    }

    public void setAccuracy(int accuracy) {
        this.mAccuracy = accuracy;
    }

    public long getTimestamp() {
        return this.mTimestamp;
    }

    public void setTimestamp(long timestamp) {
        this.mTimestamp = timestamp;
    }
}
