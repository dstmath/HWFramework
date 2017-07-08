package android.net.wifi.p2p;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.TextUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

public class WifiP2pDeviceList implements Parcelable {
    public static final Creator<WifiP2pDeviceList> CREATOR = null;
    private final HashMap<String, WifiP2pDevice> mDevices;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.wifi.p2p.WifiP2pDeviceList.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.wifi.p2p.WifiP2pDeviceList.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.p2p.WifiP2pDeviceList.<clinit>():void");
    }

    public WifiP2pDeviceList() {
        this.mDevices = new HashMap();
    }

    public WifiP2pDeviceList(WifiP2pDeviceList source) {
        this.mDevices = new HashMap();
        if (source != null) {
            for (WifiP2pDevice d : source.getDeviceList()) {
                this.mDevices.put(d.deviceAddress, new WifiP2pDevice(d));
            }
        }
    }

    public WifiP2pDeviceList(ArrayList<WifiP2pDevice> devices) {
        this.mDevices = new HashMap();
        for (WifiP2pDevice device : devices) {
            if (device.deviceAddress != null) {
                this.mDevices.put(device.deviceAddress, new WifiP2pDevice(device));
            }
        }
    }

    private void validateDevice(WifiP2pDevice device) {
        if (device == null) {
            throw new IllegalArgumentException("Null device");
        } else if (TextUtils.isEmpty(device.deviceAddress)) {
            throw new IllegalArgumentException("Empty deviceAddress");
        }
    }

    private void validateDeviceAddress(String deviceAddress) {
        if (TextUtils.isEmpty(deviceAddress)) {
            throw new IllegalArgumentException("Empty deviceAddress");
        }
    }

    public boolean clear() {
        if (this.mDevices.isEmpty()) {
            return false;
        }
        this.mDevices.clear();
        return true;
    }

    public void update(WifiP2pDevice device) {
        updateSupplicantDetails(device);
        ((WifiP2pDevice) this.mDevices.get(device.deviceAddress)).status = device.status;
    }

    public void updateSupplicantDetails(WifiP2pDevice device) {
        validateDevice(device);
        WifiP2pDevice d = (WifiP2pDevice) this.mDevices.get(device.deviceAddress);
        if (d != null) {
            d.deviceName = device.deviceName;
            d.primaryDeviceType = device.primaryDeviceType;
            d.secondaryDeviceType = device.secondaryDeviceType;
            d.wpsConfigMethodsSupported = device.wpsConfigMethodsSupported;
            d.deviceCapability = device.deviceCapability;
            d.groupCapability = device.groupCapability;
            d.wfdInfo = device.wfdInfo;
            return;
        }
        this.mDevices.put(device.deviceAddress, device);
    }

    public void updateGroupCapability(String deviceAddress, int groupCapab) {
        validateDeviceAddress(deviceAddress);
        WifiP2pDevice d = (WifiP2pDevice) this.mDevices.get(deviceAddress);
        if (d != null) {
            d.groupCapability = groupCapab;
        }
    }

    public void updateStatus(String deviceAddress, int status) {
        validateDeviceAddress(deviceAddress);
        WifiP2pDevice d = (WifiP2pDevice) this.mDevices.get(deviceAddress);
        if (d != null) {
            d.status = status;
        }
    }

    public WifiP2pDevice get(String deviceAddress) {
        validateDeviceAddress(deviceAddress);
        return (WifiP2pDevice) this.mDevices.get(deviceAddress);
    }

    public boolean remove(WifiP2pDevice device) {
        validateDevice(device);
        return this.mDevices.remove(device.deviceAddress) != null;
    }

    public WifiP2pDevice remove(String deviceAddress) {
        validateDeviceAddress(deviceAddress);
        return (WifiP2pDevice) this.mDevices.remove(deviceAddress);
    }

    public boolean remove(WifiP2pDeviceList list) {
        boolean ret = false;
        for (WifiP2pDevice d : list.mDevices.values()) {
            if (remove(d)) {
                ret = true;
            }
        }
        return ret;
    }

    public Collection<WifiP2pDevice> getDeviceList() {
        return Collections.unmodifiableCollection(this.mDevices.values());
    }

    public boolean isGroupOwner(String deviceAddress) {
        validateDeviceAddress(deviceAddress);
        WifiP2pDevice device = (WifiP2pDevice) this.mDevices.get(deviceAddress);
        if (device != null) {
            return device.isGroupOwner();
        }
        throw new IllegalArgumentException("Device not found " + deviceAddress);
    }

    public String toString() {
        StringBuffer sbuf = new StringBuffer();
        for (WifiP2pDevice device : this.mDevices.values()) {
            sbuf.append("\n").append(device);
        }
        return sbuf.toString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mDevices.size());
        for (WifiP2pDevice device : this.mDevices.values()) {
            dest.writeParcelable(device, flags);
        }
    }
}
