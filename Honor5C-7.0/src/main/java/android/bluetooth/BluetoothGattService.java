package android.bluetooth;

import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BluetoothGattService implements Parcelable {
    public static final Creator<BluetoothGattService> CREATOR = null;
    public static final int SERVICE_TYPE_PRIMARY = 0;
    public static final int SERVICE_TYPE_SECONDARY = 1;
    private boolean mAdvertisePreferred;
    protected List<BluetoothGattCharacteristic> mCharacteristics;
    protected BluetoothDevice mDevice;
    protected int mHandles;
    protected List<BluetoothGattService> mIncludedServices;
    protected int mInstanceId;
    protected int mServiceType;
    protected UUID mUuid;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.bluetooth.BluetoothGattService.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.bluetooth.BluetoothGattService.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.bluetooth.BluetoothGattService.<clinit>():void");
    }

    public BluetoothGattService(UUID uuid, int serviceType) {
        this.mHandles = SERVICE_TYPE_PRIMARY;
        this.mDevice = null;
        this.mUuid = uuid;
        this.mInstanceId = SERVICE_TYPE_PRIMARY;
        this.mServiceType = serviceType;
        this.mCharacteristics = new ArrayList();
        this.mIncludedServices = new ArrayList();
    }

    BluetoothGattService(BluetoothDevice device, UUID uuid, int instanceId, int serviceType) {
        this.mHandles = SERVICE_TYPE_PRIMARY;
        this.mDevice = device;
        this.mUuid = uuid;
        this.mInstanceId = instanceId;
        this.mServiceType = serviceType;
        this.mCharacteristics = new ArrayList();
        this.mIncludedServices = new ArrayList();
    }

    public BluetoothGattService(UUID uuid, int instanceId, int serviceType) {
        this.mHandles = SERVICE_TYPE_PRIMARY;
        this.mDevice = null;
        this.mUuid = uuid;
        this.mInstanceId = instanceId;
        this.mServiceType = serviceType;
        this.mCharacteristics = new ArrayList();
        this.mIncludedServices = new ArrayList();
    }

    public int describeContents() {
        return SERVICE_TYPE_PRIMARY;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeParcelable(new ParcelUuid(this.mUuid), SERVICE_TYPE_PRIMARY);
        out.writeInt(this.mInstanceId);
        out.writeInt(this.mServiceType);
        out.writeTypedList(this.mCharacteristics);
        ArrayList<BluetoothGattIncludedService> includedServices = new ArrayList(this.mIncludedServices.size());
        for (BluetoothGattService s : this.mIncludedServices) {
            includedServices.add(new BluetoothGattIncludedService(s.getUuid(), s.getInstanceId(), s.getType()));
        }
        out.writeTypedList(includedServices);
    }

    private BluetoothGattService(Parcel in) {
        this.mHandles = SERVICE_TYPE_PRIMARY;
        this.mUuid = ((ParcelUuid) in.readParcelable(null)).getUuid();
        this.mInstanceId = in.readInt();
        this.mServiceType = in.readInt();
        this.mCharacteristics = new ArrayList();
        ArrayList<BluetoothGattCharacteristic> chrcs = in.createTypedArrayList(BluetoothGattCharacteristic.CREATOR);
        if (chrcs != null) {
            for (BluetoothGattCharacteristic chrc : chrcs) {
                chrc.setService(this);
                this.mCharacteristics.add(chrc);
            }
        }
        this.mIncludedServices = new ArrayList();
        ArrayList<BluetoothGattIncludedService> inclSvcs = in.createTypedArrayList(BluetoothGattIncludedService.CREATOR);
        if (chrcs != null) {
            for (BluetoothGattIncludedService isvc : inclSvcs) {
                this.mIncludedServices.add(new BluetoothGattService(null, isvc.getUuid(), isvc.getInstanceId(), isvc.getType()));
            }
        }
    }

    BluetoothDevice getDevice() {
        return this.mDevice;
    }

    void setDevice(BluetoothDevice device) {
        this.mDevice = device;
    }

    public boolean addService(BluetoothGattService service) {
        this.mIncludedServices.add(service);
        return true;
    }

    public boolean addCharacteristic(BluetoothGattCharacteristic characteristic) {
        this.mCharacteristics.add(characteristic);
        characteristic.setService(this);
        return true;
    }

    BluetoothGattCharacteristic getCharacteristic(UUID uuid, int instanceId) {
        for (BluetoothGattCharacteristic characteristic : this.mCharacteristics) {
            if (uuid.equals(characteristic.getUuid()) && characteristic.getInstanceId() == instanceId) {
                return characteristic;
            }
        }
        return null;
    }

    public void setInstanceId(int instanceId) {
        this.mInstanceId = instanceId;
    }

    int getHandles() {
        return this.mHandles;
    }

    public void setHandles(int handles) {
        this.mHandles = handles;
    }

    public void addIncludedService(BluetoothGattService includedService) {
        this.mIncludedServices.add(includedService);
    }

    public UUID getUuid() {
        return this.mUuid;
    }

    public int getInstanceId() {
        return this.mInstanceId;
    }

    public int getType() {
        return this.mServiceType;
    }

    public List<BluetoothGattService> getIncludedServices() {
        return this.mIncludedServices;
    }

    public List<BluetoothGattCharacteristic> getCharacteristics() {
        return this.mCharacteristics;
    }

    public BluetoothGattCharacteristic getCharacteristic(UUID uuid) {
        for (BluetoothGattCharacteristic characteristic : this.mCharacteristics) {
            if (uuid.equals(characteristic.getUuid())) {
                return characteristic;
            }
        }
        return null;
    }

    public boolean isAdvertisePreferred() {
        return this.mAdvertisePreferred;
    }

    public void setAdvertisePreferred(boolean advertisePreferred) {
        this.mAdvertisePreferred = advertisePreferred;
    }
}
