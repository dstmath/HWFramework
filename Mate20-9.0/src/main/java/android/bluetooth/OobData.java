package android.bluetooth;

import android.os.Parcel;
import android.os.Parcelable;

public class OobData implements Parcelable {
    public static final Parcelable.Creator<OobData> CREATOR = new Parcelable.Creator<OobData>() {
        public OobData createFromParcel(Parcel in) {
            return new OobData(in);
        }

        public OobData[] newArray(int size) {
            return new OobData[size];
        }
    };
    private byte[] mLeBluetoothDeviceAddress;
    private byte[] mLeSecureConnectionsConfirmation;
    private byte[] mLeSecureConnectionsRandom;
    private byte[] mSecurityManagerTk;

    public byte[] getLeBluetoothDeviceAddress() {
        return this.mLeBluetoothDeviceAddress;
    }

    public void setLeBluetoothDeviceAddress(byte[] leBluetoothDeviceAddress) {
        this.mLeBluetoothDeviceAddress = leBluetoothDeviceAddress;
    }

    public byte[] getSecurityManagerTk() {
        return this.mSecurityManagerTk;
    }

    public void setSecurityManagerTk(byte[] securityManagerTk) {
        this.mSecurityManagerTk = securityManagerTk;
    }

    public byte[] getLeSecureConnectionsConfirmation() {
        return this.mLeSecureConnectionsConfirmation;
    }

    public void setLeSecureConnectionsConfirmation(byte[] leSecureConnectionsConfirmation) {
        this.mLeSecureConnectionsConfirmation = leSecureConnectionsConfirmation;
    }

    public byte[] getLeSecureConnectionsRandom() {
        return this.mLeSecureConnectionsRandom;
    }

    public void setLeSecureConnectionsRandom(byte[] leSecureConnectionsRandom) {
        this.mLeSecureConnectionsRandom = leSecureConnectionsRandom;
    }

    public OobData() {
    }

    private OobData(Parcel in) {
        this.mLeBluetoothDeviceAddress = in.createByteArray();
        this.mSecurityManagerTk = in.createByteArray();
        this.mLeSecureConnectionsConfirmation = in.createByteArray();
        this.mLeSecureConnectionsRandom = in.createByteArray();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeByteArray(this.mLeBluetoothDeviceAddress);
        out.writeByteArray(this.mSecurityManagerTk);
        out.writeByteArray(this.mLeSecureConnectionsConfirmation);
        out.writeByteArray(this.mLeSecureConnectionsRandom);
    }
}
