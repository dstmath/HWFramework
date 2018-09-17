package android.bluetooth;

import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BluetoothGattCharacteristic implements Parcelable {
    public static final Creator<BluetoothGattCharacteristic> CREATOR = new Creator<BluetoothGattCharacteristic>() {
        public BluetoothGattCharacteristic createFromParcel(Parcel in) {
            return new BluetoothGattCharacteristic(in, null);
        }

        public BluetoothGattCharacteristic[] newArray(int size) {
            return new BluetoothGattCharacteristic[size];
        }
    };
    public static final int FORMAT_FLOAT = 52;
    public static final int FORMAT_SFLOAT = 50;
    public static final int FORMAT_SINT16 = 34;
    public static final int FORMAT_SINT32 = 36;
    public static final int FORMAT_SINT8 = 33;
    public static final int FORMAT_UINT16 = 18;
    public static final int FORMAT_UINT32 = 20;
    public static final int FORMAT_UINT8 = 17;
    public static final int PERMISSION_READ = 1;
    public static final int PERMISSION_READ_ENCRYPTED = 2;
    public static final int PERMISSION_READ_ENCRYPTED_MITM = 4;
    public static final int PERMISSION_WRITE = 16;
    public static final int PERMISSION_WRITE_ENCRYPTED = 32;
    public static final int PERMISSION_WRITE_ENCRYPTED_MITM = 64;
    public static final int PERMISSION_WRITE_SIGNED = 128;
    public static final int PERMISSION_WRITE_SIGNED_MITM = 256;
    public static final int PROPERTY_BROADCAST = 1;
    public static final int PROPERTY_EXTENDED_PROPS = 128;
    public static final int PROPERTY_INDICATE = 32;
    public static final int PROPERTY_NOTIFY = 16;
    public static final int PROPERTY_READ = 2;
    public static final int PROPERTY_SIGNED_WRITE = 64;
    public static final int PROPERTY_WRITE = 8;
    public static final int PROPERTY_WRITE_NO_RESPONSE = 4;
    public static final int WRITE_TYPE_DEFAULT = 2;
    public static final int WRITE_TYPE_NO_RESPONSE = 1;
    public static final int WRITE_TYPE_SIGNED = 4;
    protected List<BluetoothGattDescriptor> mDescriptors;
    protected int mInstance;
    protected int mKeySize;
    protected int mPermissions;
    protected int mProperties;
    protected BluetoothGattService mService;
    protected UUID mUuid;
    protected byte[] mValue;
    protected int mWriteType;

    /* synthetic */ BluetoothGattCharacteristic(Parcel in, BluetoothGattCharacteristic -this1) {
        this(in);
    }

    public BluetoothGattCharacteristic(UUID uuid, int properties, int permissions) {
        this.mKeySize = 16;
        initCharacteristic(null, uuid, 0, properties, permissions);
    }

    BluetoothGattCharacteristic(BluetoothGattService service, UUID uuid, int instanceId, int properties, int permissions) {
        this.mKeySize = 16;
        initCharacteristic(service, uuid, instanceId, properties, permissions);
    }

    public BluetoothGattCharacteristic(UUID uuid, int instanceId, int properties, int permissions) {
        this.mKeySize = 16;
        initCharacteristic(null, uuid, instanceId, properties, permissions);
    }

    private void initCharacteristic(BluetoothGattService service, UUID uuid, int instanceId, int properties, int permissions) {
        this.mUuid = uuid;
        this.mInstance = instanceId;
        this.mProperties = properties;
        this.mPermissions = permissions;
        this.mService = service;
        this.mValue = null;
        this.mDescriptors = new ArrayList();
        if ((this.mProperties & 4) != 0) {
            this.mWriteType = 1;
        } else {
            this.mWriteType = 2;
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeParcelable(new ParcelUuid(this.mUuid), 0);
        out.writeInt(this.mInstance);
        out.writeInt(this.mProperties);
        out.writeInt(this.mPermissions);
        out.writeInt(this.mKeySize);
        out.writeInt(this.mWriteType);
        out.writeTypedList(this.mDescriptors);
    }

    private BluetoothGattCharacteristic(Parcel in) {
        this.mKeySize = 16;
        this.mUuid = ((ParcelUuid) in.readParcelable(null)).getUuid();
        this.mInstance = in.readInt();
        this.mProperties = in.readInt();
        this.mPermissions = in.readInt();
        this.mKeySize = in.readInt();
        this.mWriteType = in.readInt();
        this.mDescriptors = new ArrayList();
        ArrayList<BluetoothGattDescriptor> descs = in.createTypedArrayList(BluetoothGattDescriptor.CREATOR);
        if (descs != null) {
            for (BluetoothGattDescriptor desc : descs) {
                desc.setCharacteristic(this);
                this.mDescriptors.add(desc);
            }
        }
    }

    public int getKeySize() {
        return this.mKeySize;
    }

    public boolean addDescriptor(BluetoothGattDescriptor descriptor) {
        this.mDescriptors.add(descriptor);
        descriptor.setCharacteristic(this);
        return true;
    }

    BluetoothGattDescriptor getDescriptor(UUID uuid, int instanceId) {
        for (BluetoothGattDescriptor descriptor : this.mDescriptors) {
            if (descriptor.getUuid().equals(uuid) && descriptor.getInstanceId() == instanceId) {
                return descriptor;
            }
        }
        return null;
    }

    public BluetoothGattService getService() {
        return this.mService;
    }

    void setService(BluetoothGattService service) {
        this.mService = service;
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

    public int getProperties() {
        return this.mProperties;
    }

    public int getPermissions() {
        return this.mPermissions;
    }

    public int getWriteType() {
        return this.mWriteType;
    }

    public void setWriteType(int writeType) {
        this.mWriteType = writeType;
    }

    public void setKeySize(int keySize) {
        this.mKeySize = keySize;
    }

    public List<BluetoothGattDescriptor> getDescriptors() {
        return this.mDescriptors;
    }

    public BluetoothGattDescriptor getDescriptor(UUID uuid) {
        for (BluetoothGattDescriptor descriptor : this.mDescriptors) {
            if (descriptor.getUuid().equals(uuid)) {
                return descriptor;
            }
        }
        return null;
    }

    public byte[] getValue() {
        return this.mValue;
    }

    public Integer getIntValue(int formatType, int offset) {
        if (getTypeLen(formatType) + offset > this.mValue.length) {
            return null;
        }
        switch (formatType) {
            case 17:
                return Integer.valueOf(unsignedByteToInt(this.mValue[offset]));
            case 18:
                return Integer.valueOf(unsignedBytesToInt(this.mValue[offset], this.mValue[offset + 1]));
            case 20:
                return Integer.valueOf(unsignedBytesToInt(this.mValue[offset], this.mValue[offset + 1], this.mValue[offset + 2], this.mValue[offset + 3]));
            case 33:
                return Integer.valueOf(unsignedToSigned(unsignedByteToInt(this.mValue[offset]), 8));
            case 34:
                return Integer.valueOf(unsignedToSigned(unsignedBytesToInt(this.mValue[offset], this.mValue[offset + 1]), 16));
            case 36:
                return Integer.valueOf(unsignedToSigned(unsignedBytesToInt(this.mValue[offset], this.mValue[offset + 1], this.mValue[offset + 2], this.mValue[offset + 3]), 32));
            default:
                return null;
        }
    }

    public Float getFloatValue(int formatType, int offset) {
        if (getTypeLen(formatType) + offset > this.mValue.length) {
            return null;
        }
        switch (formatType) {
            case 50:
                return Float.valueOf(bytesToFloat(this.mValue[offset], this.mValue[offset + 1]));
            case 52:
                return Float.valueOf(bytesToFloat(this.mValue[offset], this.mValue[offset + 1], this.mValue[offset + 2], this.mValue[offset + 3]));
            default:
                return null;
        }
    }

    public String getStringValue(int offset) {
        if (this.mValue == null || offset > this.mValue.length) {
            return null;
        }
        byte[] strBytes = new byte[(this.mValue.length - offset)];
        for (int i = 0; i != this.mValue.length - offset; i++) {
            strBytes[i] = this.mValue[offset + i];
        }
        return new String(strBytes);
    }

    public boolean setValue(byte[] value) {
        this.mValue = value;
        return true;
    }

    /* JADX WARNING: Missing block: B:9:0x001f, code:
            r4.mValue[r7] = (byte) (r5 & 255);
     */
    /* JADX WARNING: Missing block: B:11:0x0027, code:
            return true;
     */
    /* JADX WARNING: Missing block: B:13:0x002e, code:
            r1 = r7 + 1;
            r4.mValue[r7] = (byte) (r5 & 255);
            r4.mValue[r1] = (byte) ((r5 >> 8) & 255);
            r7 = r1;
     */
    /* JADX WARNING: Missing block: B:15:0x0048, code:
            r1 = r7 + 1;
            r4.mValue[r7] = (byte) (r5 & 255);
            r7 = r1 + 1;
            r4.mValue[r1] = (byte) ((r5 >> 8) & 255);
            r1 = r7 + 1;
            r4.mValue[r7] = (byte) ((r5 >> 16) & 255);
            r4.mValue[r1] = (byte) ((r5 >> 24) & 255);
            r7 = r1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean setValue(int value, int formatType, int offset) {
        int len = offset + getTypeLen(formatType);
        if (this.mValue == null) {
            this.mValue = new byte[len];
        }
        if (len > this.mValue.length) {
            return false;
        }
        switch (formatType) {
            case 17:
                break;
            case 18:
                break;
            case 20:
                break;
            case 33:
                value = intToSignedBits(value, 8);
                break;
            case 34:
                value = intToSignedBits(value, 16);
                break;
            case 36:
                value = intToSignedBits(value, 32);
                break;
            default:
                return false;
        }
    }

    public boolean setValue(int mantissa, int exponent, int formatType, int offset) {
        int len = offset + getTypeLen(formatType);
        if (this.mValue == null) {
            this.mValue = new byte[len];
        }
        if (len > this.mValue.length) {
            return false;
        }
        int offset2;
        byte[] bArr;
        switch (formatType) {
            case 50:
                mantissa = intToSignedBits(mantissa, 12);
                exponent = intToSignedBits(exponent, 4);
                offset2 = offset + 1;
                this.mValue[offset] = (byte) (mantissa & 255);
                this.mValue[offset2] = (byte) ((mantissa >> 8) & 15);
                bArr = this.mValue;
                bArr[offset2] = (byte) (bArr[offset2] + ((byte) ((exponent & 15) << 4)));
                offset = offset2;
                break;
            case 52:
                mantissa = intToSignedBits(mantissa, 24);
                exponent = intToSignedBits(exponent, 8);
                offset2 = offset + 1;
                this.mValue[offset] = (byte) (mantissa & 255);
                offset = offset2 + 1;
                this.mValue[offset2] = (byte) ((mantissa >> 8) & 255);
                offset2 = offset + 1;
                this.mValue[offset] = (byte) ((mantissa >> 16) & 255);
                bArr = this.mValue;
                bArr[offset2] = (byte) (bArr[offset2] + ((byte) (exponent & 255)));
                offset = offset2;
                break;
            default:
                return false;
        }
        return true;
    }

    public boolean setValue(String value) {
        this.mValue = value.getBytes();
        return true;
    }

    private int getTypeLen(int formatType) {
        return formatType & 15;
    }

    private int unsignedByteToInt(byte b) {
        return b & 255;
    }

    private int unsignedBytesToInt(byte b0, byte b1) {
        return unsignedByteToInt(b0) + (unsignedByteToInt(b1) << 8);
    }

    private int unsignedBytesToInt(byte b0, byte b1, byte b2, byte b3) {
        return ((unsignedByteToInt(b0) + (unsignedByteToInt(b1) << 8)) + (unsignedByteToInt(b2) << 16)) + (unsignedByteToInt(b3) << 24);
    }

    private float bytesToFloat(byte b0, byte b1) {
        return (float) (((double) unsignedToSigned(unsignedByteToInt(b0) + ((unsignedByteToInt(b1) & 15) << 8), 12)) * Math.pow(10.0d, (double) unsignedToSigned(unsignedByteToInt(b1) >> 4, 4)));
    }

    private float bytesToFloat(byte b0, byte b1, byte b2, byte b3) {
        return (float) (((double) unsignedToSigned((unsignedByteToInt(b0) + (unsignedByteToInt(b1) << 8)) + (unsignedByteToInt(b2) << 16), 24)) * Math.pow(10.0d, (double) b3));
    }

    private int unsignedToSigned(int unsigned, int size) {
        if (((1 << (size - 1)) & unsigned) != 0) {
            return ((1 << (size - 1)) - (((1 << (size - 1)) - 1) & unsigned)) * -1;
        }
        return unsigned;
    }

    private int intToSignedBits(int i, int size) {
        if (i < 0) {
            return (1 << (size - 1)) + (((1 << (size - 1)) - 1) & i);
        }
        return i;
    }
}
