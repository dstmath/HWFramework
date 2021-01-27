package ohos.dmsdp.sdk.sensor;

import android.os.Parcel;
import android.os.Parcelable;

public class VirtualSensor implements Parcelable {
    public static final Parcelable.Creator<VirtualSensor> CREATOR = new Parcelable.Creator<VirtualSensor>() {
        /* class ohos.dmsdp.sdk.sensor.VirtualSensor.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public VirtualSensor createFromParcel(Parcel parcel) {
            return new VirtualSensor(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public VirtualSensor[] newArray(int i) {
            return new VirtualSensor[i];
        }
    };
    public static final String STRING_TYPE_HEART_RATE = "huawei.dmsdp.sensor.heart_rate";
    public static final String STRING_TYPE_PPG = "huawei.dmsdp.sensor.ppg";
    public static final int TYPE_HEART_RATE = 1;
    public static final int TYPE_HEART_RATE_PPG = 2;
    private String mDeviceId;
    private int mSensorId;
    private int mSensorType;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public VirtualSensor() {
    }

    public VirtualSensor(String str, int i, int i2) {
        this.mDeviceId = str;
        this.mSensorId = i;
        this.mSensorType = i2;
    }

    protected VirtualSensor(Parcel parcel) {
        this.mDeviceId = parcel.readString();
        this.mSensorId = parcel.readInt();
        this.mSensorType = parcel.readInt();
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.mDeviceId);
        parcel.writeInt(this.mSensorId);
        parcel.writeInt(this.mSensorType);
    }

    public String getDeviceId() {
        return this.mDeviceId;
    }

    public void setDeviceId(String str) {
        this.mDeviceId = str;
    }

    public int getSensorId() {
        return this.mSensorId;
    }

    public void setSensorId(int i) {
        this.mSensorId = i;
    }

    public int getSensorType() {
        return this.mSensorType;
    }

    public void setSensorType(int i) {
        this.mSensorType = i;
    }
}
