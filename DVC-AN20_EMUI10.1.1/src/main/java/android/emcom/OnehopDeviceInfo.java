package android.emcom;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Arrays;

public class OnehopDeviceInfo implements Parcelable {
    public static final Parcelable.Creator<OnehopDeviceInfo> CREATOR = new Parcelable.Creator<OnehopDeviceInfo>() {
        /* class android.emcom.OnehopDeviceInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public OnehopDeviceInfo createFromParcel(Parcel pa) {
            return new OnehopDeviceInfo(pa);
        }

        @Override // android.os.Parcelable.Creator
        public OnehopDeviceInfo[] newArray(int size) {
            return new OnehopDeviceInfo[size];
        }
    };
    public int[] capabilityBitmap;
    public int capabilityBitmapNum;
    public String deviceId;
    public String deviceName;
    public int deviceType;
    public String reservedInfo;

    public OnehopDeviceInfo() {
    }

    public OnehopDeviceInfo(String deviceName2, String deviceId2, int deviceType2, int capabilityBitmapNum2, int[] capabilityBitmap2, String reservedInfo2) {
        this.deviceName = deviceName2;
        this.deviceId = deviceId2;
        this.deviceType = deviceType2;
        this.capabilityBitmapNum = capabilityBitmapNum2;
        this.capabilityBitmap = capabilityBitmap2;
        this.reservedInfo = reservedInfo2;
    }

    public OnehopDeviceInfo(Parcel pa) {
        this.deviceName = pa.readString();
        this.deviceId = pa.readString();
        this.deviceType = pa.readInt();
        this.capabilityBitmapNum = pa.readInt();
        this.capabilityBitmap = pa.createIntArray();
        this.reservedInfo = pa.readString();
    }

    public void writeToParcel(Parcel pa, int flags) {
        pa.writeString(this.deviceName);
        pa.writeString(this.deviceId);
        pa.writeInt(this.deviceType);
        pa.writeInt(this.capabilityBitmapNum);
        pa.writeIntArray(this.capabilityBitmap);
        pa.writeString(this.reservedInfo);
    }

    public void readFromParcel(Parcel pa) {
        this.deviceName = pa.readString();
        this.deviceId = pa.readString();
        this.deviceType = pa.readInt();
        this.capabilityBitmapNum = pa.readInt();
        this.capabilityBitmap = pa.createIntArray();
        this.reservedInfo = pa.readString();
    }

    public int describeContents() {
        return 0;
    }

    public String toString() {
        return "hash: " + hashCode() + ",deviceType: " + this.deviceType + ",capabilityBitmapNum: " + this.capabilityBitmapNum + ",capabilityBitmap: " + Arrays.toString(this.capabilityBitmap);
    }
}
