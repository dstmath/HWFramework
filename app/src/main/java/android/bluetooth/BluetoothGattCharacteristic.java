package android.bluetooth;

import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.Process;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BluetoothGattCharacteristic implements Parcelable {
    public static final Creator<BluetoothGattCharacteristic> CREATOR = null;
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.bluetooth.BluetoothGattCharacteristic.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.bluetooth.BluetoothGattCharacteristic.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.bluetooth.BluetoothGattCharacteristic.<clinit>():void");
    }

    public BluetoothGattCharacteristic(UUID uuid, int properties, int permissions) {
        this.mKeySize = PROPERTY_NOTIFY;
        initCharacteristic(null, uuid, 0, properties, permissions);
    }

    BluetoothGattCharacteristic(BluetoothGattService service, UUID uuid, int instanceId, int properties, int permissions) {
        this.mKeySize = PROPERTY_NOTIFY;
        initCharacteristic(service, uuid, instanceId, properties, permissions);
    }

    public BluetoothGattCharacteristic(UUID uuid, int instanceId, int properties, int permissions) {
        this.mKeySize = PROPERTY_NOTIFY;
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
        if ((this.mProperties & WRITE_TYPE_SIGNED) != 0) {
            this.mWriteType = WRITE_TYPE_NO_RESPONSE;
        } else {
            this.mWriteType = WRITE_TYPE_DEFAULT;
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
        this.mKeySize = PROPERTY_NOTIFY;
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

    int getKeySize() {
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
            case FORMAT_UINT8 /*17*/:
                return Integer.valueOf(unsignedByteToInt(this.mValue[offset]));
            case FORMAT_UINT16 /*18*/:
                return Integer.valueOf(unsignedBytesToInt(this.mValue[offset], this.mValue[offset + WRITE_TYPE_NO_RESPONSE]));
            case FORMAT_UINT32 /*20*/:
                return Integer.valueOf(unsignedBytesToInt(this.mValue[offset], this.mValue[offset + WRITE_TYPE_NO_RESPONSE], this.mValue[offset + WRITE_TYPE_DEFAULT], this.mValue[offset + 3]));
            case FORMAT_SINT8 /*33*/:
                return Integer.valueOf(unsignedToSigned(unsignedByteToInt(this.mValue[offset]), PROPERTY_WRITE));
            case FORMAT_SINT16 /*34*/:
                return Integer.valueOf(unsignedToSigned(unsignedBytesToInt(this.mValue[offset], this.mValue[offset + WRITE_TYPE_NO_RESPONSE]), PROPERTY_NOTIFY));
            case FORMAT_SINT32 /*36*/:
                return Integer.valueOf(unsignedToSigned(unsignedBytesToInt(this.mValue[offset], this.mValue[offset + WRITE_TYPE_NO_RESPONSE], this.mValue[offset + WRITE_TYPE_DEFAULT], this.mValue[offset + 3]), PROPERTY_INDICATE));
            default:
                return null;
        }
    }

    public Float getFloatValue(int formatType, int offset) {
        if (getTypeLen(formatType) + offset > this.mValue.length) {
            return null;
        }
        switch (formatType) {
            case FORMAT_SFLOAT /*50*/:
                return Float.valueOf(bytesToFloat(this.mValue[offset], this.mValue[offset + WRITE_TYPE_NO_RESPONSE]));
            case FORMAT_FLOAT /*52*/:
                return Float.valueOf(bytesToFloat(this.mValue[offset], this.mValue[offset + WRITE_TYPE_NO_RESPONSE], this.mValue[offset + WRITE_TYPE_DEFAULT], this.mValue[offset + 3]));
            default:
                return null;
        }
    }

    public String getStringValue(int offset) {
        if (this.mValue == null || offset > this.mValue.length) {
            return null;
        }
        byte[] strBytes = new byte[(this.mValue.length - offset)];
        for (int i = 0; i != this.mValue.length - offset; i += WRITE_TYPE_NO_RESPONSE) {
            strBytes[i] = this.mValue[offset + i];
        }
        return new String(strBytes);
    }

    public boolean setValue(byte[] value) {
        this.mValue = value;
        return true;
    }

    /* JADX WARNING: inconsistent code. */
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
            case FORMAT_UINT8 /*17*/:
                break;
            case FORMAT_UINT16 /*18*/:
                break;
            case FORMAT_UINT32 /*20*/:
                break;
            case FORMAT_SINT8 /*33*/:
                value = intToSignedBits(value, PROPERTY_WRITE);
                break;
            case FORMAT_SINT16 /*34*/:
                value = intToSignedBits(value, PROPERTY_NOTIFY);
                break;
            case FORMAT_SINT32 /*36*/:
                value = intToSignedBits(value, PROPERTY_INDICATE);
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
            case FORMAT_SFLOAT /*50*/:
                mantissa = intToSignedBits(mantissa, 12);
                exponent = intToSignedBits(exponent, WRITE_TYPE_SIGNED);
                offset2 = offset + WRITE_TYPE_NO_RESPONSE;
                this.mValue[offset] = (byte) (mantissa & Process.PROC_TERM_MASK);
                this.mValue[offset2] = (byte) ((mantissa >> PROPERTY_WRITE) & 15);
                bArr = this.mValue;
                bArr[offset2] = (byte) (bArr[offset2] + ((byte) ((exponent & 15) << WRITE_TYPE_SIGNED)));
                offset = offset2;
                break;
            case FORMAT_FLOAT /*52*/:
                mantissa = intToSignedBits(mantissa, 24);
                exponent = intToSignedBits(exponent, PROPERTY_WRITE);
                offset2 = offset + WRITE_TYPE_NO_RESPONSE;
                this.mValue[offset] = (byte) (mantissa & Process.PROC_TERM_MASK);
                offset = offset2 + WRITE_TYPE_NO_RESPONSE;
                this.mValue[offset2] = (byte) ((mantissa >> PROPERTY_WRITE) & Process.PROC_TERM_MASK);
                offset2 = offset + WRITE_TYPE_NO_RESPONSE;
                this.mValue[offset] = (byte) ((mantissa >> PROPERTY_NOTIFY) & Process.PROC_TERM_MASK);
                bArr = this.mValue;
                bArr[offset2] = (byte) (bArr[offset2] + ((byte) (exponent & Process.PROC_TERM_MASK)));
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
        return b & Process.PROC_TERM_MASK;
    }

    private int unsignedBytesToInt(byte b0, byte b1) {
        return unsignedByteToInt(b0) + (unsignedByteToInt(b1) << PROPERTY_WRITE);
    }

    private int unsignedBytesToInt(byte b0, byte b1, byte b2, byte b3) {
        return ((unsignedByteToInt(b0) + (unsignedByteToInt(b1) << PROPERTY_WRITE)) + (unsignedByteToInt(b2) << PROPERTY_NOTIFY)) + (unsignedByteToInt(b3) << 24);
    }

    private float bytesToFloat(byte b0, byte b1) {
        return (float) (((double) unsignedToSigned(unsignedByteToInt(b0) + ((unsignedByteToInt(b1) & 15) << PROPERTY_WRITE), 12)) * Math.pow(10.0d, (double) unsignedToSigned(unsignedByteToInt(b1) >> WRITE_TYPE_SIGNED, WRITE_TYPE_SIGNED)));
    }

    private float bytesToFloat(byte b0, byte b1, byte b2, byte b3) {
        return (float) (((double) unsignedToSigned((unsignedByteToInt(b0) + (unsignedByteToInt(b1) << PROPERTY_WRITE)) + (unsignedByteToInt(b2) << PROPERTY_NOTIFY), 24)) * Math.pow(10.0d, (double) b3));
    }

    private int unsignedToSigned(int unsigned, int size) {
        if (((WRITE_TYPE_NO_RESPONSE << (size - 1)) & unsigned) != 0) {
            return ((WRITE_TYPE_NO_RESPONSE << (size - 1)) - (((WRITE_TYPE_NO_RESPONSE << (size - 1)) - 1) & unsigned)) * -1;
        }
        return unsigned;
    }

    private int intToSignedBits(int i, int size) {
        if (i < 0) {
            return (WRITE_TYPE_NO_RESPONSE << (size - 1)) + (((WRITE_TYPE_NO_RESPONSE << (size - 1)) - 1) & i);
        }
        return i;
    }
}
