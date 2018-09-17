package android.bluetooth;

import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.UUID;

public class BluetoothGattDescriptor implements Parcelable {
    public static final Creator<BluetoothGattDescriptor> CREATOR = null;
    public static final byte[] DISABLE_NOTIFICATION_VALUE = null;
    public static final byte[] ENABLE_INDICATION_VALUE = null;
    public static final byte[] ENABLE_NOTIFICATION_VALUE = null;
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.bluetooth.BluetoothGattDescriptor.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.bluetooth.BluetoothGattDescriptor.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.bluetooth.BluetoothGattDescriptor.<clinit>():void");
    }

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
