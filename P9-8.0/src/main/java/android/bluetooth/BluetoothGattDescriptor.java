package android.bluetooth;

import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.UUID;

public class BluetoothGattDescriptor implements Parcelable {
    public static final Creator<BluetoothGattDescriptor> CREATOR = new Creator<BluetoothGattDescriptor>() {
        public BluetoothGattDescriptor createFromParcel(Parcel in) {
            return new BluetoothGattDescriptor(in, null);
        }

        public BluetoothGattDescriptor[] newArray(int size) {
            return new BluetoothGattDescriptor[size];
        }
    };
    public static final byte[] DISABLE_NOTIFICATION_VALUE = new byte[]{(byte) 0, (byte) 0};
    public static final byte[] ENABLE_INDICATION_VALUE = new byte[]{(byte) 2, (byte) 0};
    public static final byte[] ENABLE_NOTIFICATION_VALUE = new byte[]{(byte) 1, (byte) 0};
    public static final int PERMISSION_READ = 1;
    public static final int PERMISSION_READ_ENCRYPTED = 2;
    public static final int PERMISSION_READ_ENCRYPTED_MITM = 4;
    public static final int PERMISSION_WRITE = 16;
    public static final int PERMISSION_WRITE_ENCRYPTED = 32;
    public static final int PERMISSION_WRITE_ENCRYPTED_MITM = 64;
    public static final int PERMISSION_WRITE_SIGNED = 128;
    public static final int PERMISSION_WRITE_SIGNED_MITM = 256;
    protected BluetoothGattCharacteristic mCharacteristic;
    protected int mInstance;
    protected int mPermissions;
    protected UUID mUuid;
    protected byte[] mValue;

    public BluetoothGattDescriptor(UUID uuid, int permissions) {
        initDescriptor(null, uuid, 0, permissions);
    }

    BluetoothGattDescriptor(BluetoothGattCharacteristic characteristic, UUID uuid, int instance, int permissions) {
        initDescriptor(characteristic, uuid, instance, permissions);
    }

    public BluetoothGattDescriptor(UUID uuid, int instance, int permissions) {
        initDescriptor(null, uuid, instance, permissions);
    }

    private void initDescriptor(BluetoothGattCharacteristic characteristic, UUID uuid, int instance, int permissions) {
        this.mCharacteristic = characteristic;
        this.mUuid = uuid;
        this.mInstance = instance;
        this.mPermissions = permissions;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeParcelable(new ParcelUuid(this.mUuid), 0);
        out.writeInt(this.mInstance);
        out.writeInt(this.mPermissions);
    }

    private BluetoothGattDescriptor(Parcel in) {
        this.mUuid = ((ParcelUuid) in.readParcelable(null)).getUuid();
        this.mInstance = in.readInt();
        this.mPermissions = in.readInt();
    }

    public BluetoothGattCharacteristic getCharacteristic() {
        return this.mCharacteristic;
    }

    void setCharacteristic(BluetoothGattCharacteristic characteristic) {
        this.mCharacteristic = characteristic;
    }

    public UUID getUuid() {
        return this.mUuid;
    }

    public int getInstanceId() {
        return this.mInstance;
    }

    public void setInstanceId(int instanceId) {
        this.mInstance = instanceId;
    }

    public int getPermissions() {
        return this.mPermissions;
    }

    public byte[] getValue() {
        return this.mValue;
    }

    public boolean setValue(byte[] value) {
        this.mValue = value;
        return true;
    }
}
