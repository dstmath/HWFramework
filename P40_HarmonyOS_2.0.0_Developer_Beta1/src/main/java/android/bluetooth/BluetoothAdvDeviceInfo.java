package android.bluetooth;

import android.os.Parcel;
import android.os.Parcelable;

public class BluetoothAdvDeviceInfo implements Parcelable {
    public static final Parcelable.Creator<BluetoothAdvDeviceInfo> CREATOR = new Parcelable.Creator<BluetoothAdvDeviceInfo>() {
        /* class android.bluetooth.BluetoothAdvDeviceInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public BluetoothAdvDeviceInfo createFromParcel(Parcel in) {
            return new BluetoothAdvDeviceInfo(in);
        }

        @Override // android.os.Parcelable.Creator
        public BluetoothAdvDeviceInfo[] newArray(int size) {
            return new BluetoothAdvDeviceInfo[size];
        }
    };
    public static int DEV_ID_LEN = 8;
    private static final String TAG = "BluetoothAdvDeviceInfo";
    private byte[] mDevicId;
    private short mStatus;
    private int mTimeout;

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeByteArray(this.mDevicId);
        out.writeByte((byte) (this.mStatus >> 8));
        out.writeByte((byte) (this.mStatus & 255));
        out.writeInt(this.mTimeout);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    private BluetoothAdvDeviceInfo(Parcel in) {
        this.mDevicId = new byte[DEV_ID_LEN];
        in.readByteArray(this.mDevicId);
        this.mStatus = (short) ((in.readByte() << 8) | in.readByte());
        this.mTimeout = in.readInt();
    }

    public BluetoothAdvDeviceInfo(byte[] devicId, short status, int timeout) {
        this.mDevicId = new byte[DEV_ID_LEN];
        for (int i = 0; i < DEV_ID_LEN; i++) {
            this.mDevicId[i] = devicId[i];
        }
        this.mStatus = status;
        this.mTimeout = timeout;
    }

    public byte[] getDevId() {
        return this.mDevicId;
    }

    public short getStatus() {
        return this.mStatus;
    }

    public int getTimeout() {
        return this.mTimeout;
    }
}
