package ohos.dmsdp.sdk.sensor;

import android.os.Parcel;
import android.os.Parcelable;

public class SensorData implements Parcelable {
    public static final Parcelable.Creator<SensorData> CREATOR = new Parcelable.Creator<SensorData>() {
        /* class ohos.dmsdp.sdk.sensor.SensorData.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public SensorData createFromParcel(Parcel parcel) {
            return new SensorData(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public SensorData[] newArray(int i) {
            return new SensorData[i];
        }
    };
    private int mAccuracy;
    private VirtualSensor mSensor = new VirtualSensor();
    private long mTimestamp;
    private final float[] mValues;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public SensorData(int i) {
        this.mValues = new float[i];
    }

    protected SensorData(Parcel parcel) {
        this.mValues = parcel.createFloatArray();
        this.mAccuracy = parcel.readInt();
        this.mTimestamp = parcel.readLong();
        this.mSensor.setSensorId(parcel.readInt());
        this.mSensor.setDeviceId(parcel.readString());
        this.mSensor.setSensorType(parcel.readInt());
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeFloatArray(this.mValues);
        parcel.writeInt(this.mAccuracy);
        parcel.writeLong(this.mTimestamp);
        parcel.writeInt(this.mSensor.getSensorId());
        parcel.writeString(this.mSensor.getDeviceId());
        parcel.writeInt(this.mSensor.getSensorType());
    }

    public float[] getValues() {
        return (float[]) this.mValues.clone();
    }

    public VirtualSensor getSensor() {
        return this.mSensor;
    }

    public void setSensor(VirtualSensor virtualSensor) {
        this.mSensor = virtualSensor;
    }

    public int getAccuracy() {
        return this.mAccuracy;
    }

    public void setAccuracy(int i) {
        this.mAccuracy = i;
    }

    public long getTimestamp() {
        return this.mTimestamp;
    }

    public void setTimestamp(long j) {
        this.mTimestamp = j;
    }
}
