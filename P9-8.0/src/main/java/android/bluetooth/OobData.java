package android.bluetooth;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class OobData implements Parcelable {
    public static final Creator<OobData> CREATOR = new Creator<OobData>() {
        public OobData createFromParcel(Parcel in) {
            return new OobData(in, null);
        }

        public OobData[] newArray(int size) {
            return new OobData[size];
        }
    };
    private byte[] leBluetoothDeviceAddress;
    private byte[] leSecureConnectionsConfirmation;
    private byte[] leSecureConnectionsRandom;
    private byte[] securityManagerTk;

    /* synthetic */ OobData(Parcel in, OobData -this1) {
        this(in);
    }

    public byte[] getLeBluetoothDeviceAddress() {
        return this.leBluetoothDeviceAddress;
    }

    public void setLeBluetoothDeviceAddress(byte[] leBluetoothDeviceAddress) {
        this.leBluetoothDeviceAddress = leBluetoothDeviceAddress;
    }

    public byte[] getSecurityManagerTk() {
        return this.securityManagerTk;
    }

    public void setSecurityManagerTk(byte[] securityManagerTk) {
        this.securityManagerTk = securityManagerTk;
    }

    public byte[] getLeSecureConnectionsConfirmation() {
        return this.leSecureConnectionsConfirmation;
    }

    public void setLeSecureConnectionsConfirmation(byte[] leSecureConnectionsConfirmation) {
        this.leSecureConnectionsConfirmation = leSecureConnectionsConfirmation;
    }

    public byte[] getLeSecureConnectionsRandom() {
        return this.leSecureConnectionsRandom;
    }

    public void setLeSecureConnectionsRandom(byte[] leSecureConnectionsRandom) {
        this.leSecureConnectionsRandom = leSecureConnectionsRandom;
    }

    private OobData(Parcel in) {
        this.leBluetoothDeviceAddress = in.createByteArray();
        this.securityManagerTk = in.createByteArray();
        this.leSecureConnectionsConfirmation = in.createByteArray();
        this.leSecureConnectionsRandom = in.createByteArray();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeByteArray(this.leBluetoothDeviceAddress);
        out.writeByteArray(this.securityManagerTk);
        out.writeByteArray(this.leSecureConnectionsConfirmation);
        out.writeByteArray(this.leSecureConnectionsRandom);
    }
}
