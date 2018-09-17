package android.hardware.hdmi;

import android.net.ProxyInfo;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class HdmiDeviceInfo implements Parcelable {
    public static final int ADDR_INTERNAL = 0;
    public static final Creator<HdmiDeviceInfo> CREATOR = null;
    public static final int DEVICE_AUDIO_SYSTEM = 5;
    public static final int DEVICE_INACTIVE = -1;
    public static final int DEVICE_PLAYBACK = 4;
    public static final int DEVICE_PURE_CEC_SWITCH = 6;
    public static final int DEVICE_RECORDER = 1;
    public static final int DEVICE_RESERVED = 2;
    public static final int DEVICE_TUNER = 3;
    public static final int DEVICE_TV = 0;
    public static final int DEVICE_VIDEO_PROCESSOR = 7;
    private static final int HDMI_DEVICE_TYPE_CEC = 0;
    private static final int HDMI_DEVICE_TYPE_HARDWARE = 2;
    private static final int HDMI_DEVICE_TYPE_INACTIVE = 100;
    private static final int HDMI_DEVICE_TYPE_MHL = 1;
    public static final int ID_INVALID = 65535;
    private static final int ID_OFFSET_CEC = 0;
    private static final int ID_OFFSET_HARDWARE = 192;
    private static final int ID_OFFSET_MHL = 128;
    public static final HdmiDeviceInfo INACTIVE_DEVICE = null;
    public static final int PATH_INTERNAL = 0;
    public static final int PATH_INVALID = 65535;
    public static final int PORT_INVALID = -1;
    private final int mAdopterId;
    private final int mDeviceId;
    private final int mDevicePowerStatus;
    private final int mDeviceType;
    private final String mDisplayName;
    private final int mHdmiDeviceType;
    private final int mId;
    private final int mLogicalAddress;
    private final int mPhysicalAddress;
    private final int mPortId;
    private final int mVendorId;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.hardware.hdmi.HdmiDeviceInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.hardware.hdmi.HdmiDeviceInfo.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.hdmi.HdmiDeviceInfo.<clinit>():void");
    }

    public HdmiDeviceInfo(int logicalAddress, int physicalAddress, int portId, int deviceType, int vendorId, String displayName, int powerStatus) {
        this.mHdmiDeviceType = PATH_INTERNAL;
        this.mPhysicalAddress = physicalAddress;
        this.mPortId = portId;
        this.mId = idForCecDevice(logicalAddress);
        this.mLogicalAddress = logicalAddress;
        this.mDeviceType = deviceType;
        this.mVendorId = vendorId;
        this.mDevicePowerStatus = powerStatus;
        this.mDisplayName = displayName;
        this.mDeviceId = PORT_INVALID;
        this.mAdopterId = PORT_INVALID;
    }

    public HdmiDeviceInfo(int logicalAddress, int physicalAddress, int portId, int deviceType, int vendorId, String displayName) {
        this(logicalAddress, physicalAddress, portId, deviceType, vendorId, displayName, PORT_INVALID);
    }

    public HdmiDeviceInfo(int physicalAddress, int portId) {
        this.mHdmiDeviceType = HDMI_DEVICE_TYPE_HARDWARE;
        this.mPhysicalAddress = physicalAddress;
        this.mPortId = portId;
        this.mId = idForHardware(portId);
        this.mLogicalAddress = PORT_INVALID;
        this.mDeviceType = HDMI_DEVICE_TYPE_HARDWARE;
        this.mVendorId = PATH_INTERNAL;
        this.mDevicePowerStatus = PORT_INVALID;
        this.mDisplayName = "HDMI" + portId;
        this.mDeviceId = PORT_INVALID;
        this.mAdopterId = PORT_INVALID;
    }

    public HdmiDeviceInfo(int physicalAddress, int portId, int adopterId, int deviceId) {
        this.mHdmiDeviceType = HDMI_DEVICE_TYPE_MHL;
        this.mPhysicalAddress = physicalAddress;
        this.mPortId = portId;
        this.mId = idForMhlDevice(portId);
        this.mLogicalAddress = PORT_INVALID;
        this.mDeviceType = HDMI_DEVICE_TYPE_HARDWARE;
        this.mVendorId = PATH_INTERNAL;
        this.mDevicePowerStatus = PORT_INVALID;
        this.mDisplayName = "Mobile";
        this.mDeviceId = adopterId;
        this.mAdopterId = deviceId;
    }

    public HdmiDeviceInfo() {
        this.mHdmiDeviceType = HDMI_DEVICE_TYPE_INACTIVE;
        this.mPhysicalAddress = PATH_INVALID;
        this.mId = PATH_INVALID;
        this.mLogicalAddress = PORT_INVALID;
        this.mDeviceType = PORT_INVALID;
        this.mPortId = PORT_INVALID;
        this.mDevicePowerStatus = PORT_INVALID;
        this.mDisplayName = "Inactive";
        this.mVendorId = PATH_INTERNAL;
        this.mDeviceId = PORT_INVALID;
        this.mAdopterId = PORT_INVALID;
    }

    public int getId() {
        return this.mId;
    }

    public static int idForCecDevice(int address) {
        return address + PATH_INTERNAL;
    }

    public static int idForMhlDevice(int portId) {
        return portId + ID_OFFSET_MHL;
    }

    public static int idForHardware(int portId) {
        return portId + ID_OFFSET_HARDWARE;
    }

    public int getLogicalAddress() {
        return this.mLogicalAddress;
    }

    public int getPhysicalAddress() {
        return this.mPhysicalAddress;
    }

    public int getPortId() {
        return this.mPortId;
    }

    public int getDeviceType() {
        return this.mDeviceType;
    }

    public int getDevicePowerStatus() {
        return this.mDevicePowerStatus;
    }

    public int getDeviceId() {
        return this.mDeviceId;
    }

    public int getAdopterId() {
        return this.mAdopterId;
    }

    public boolean isSourceType() {
        boolean z = true;
        if (!isCecDevice()) {
            return isMhlDevice();
        } else {
            if (!(this.mDeviceType == DEVICE_PLAYBACK || this.mDeviceType == HDMI_DEVICE_TYPE_MHL || this.mDeviceType == DEVICE_TUNER)) {
                z = false;
            }
            return z;
        }
    }

    public boolean isCecDevice() {
        return this.mHdmiDeviceType == 0;
    }

    public boolean isMhlDevice() {
        return this.mHdmiDeviceType == HDMI_DEVICE_TYPE_MHL;
    }

    public boolean isInactivated() {
        return this.mHdmiDeviceType == HDMI_DEVICE_TYPE_INACTIVE;
    }

    public String getDisplayName() {
        return this.mDisplayName;
    }

    public int getVendorId() {
        return this.mVendorId;
    }

    public int describeContents() {
        return PATH_INTERNAL;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mHdmiDeviceType);
        dest.writeInt(this.mPhysicalAddress);
        dest.writeInt(this.mPortId);
        switch (this.mHdmiDeviceType) {
            case PATH_INTERNAL /*0*/:
                dest.writeInt(this.mLogicalAddress);
                dest.writeInt(this.mDeviceType);
                dest.writeInt(this.mVendorId);
                dest.writeInt(this.mDevicePowerStatus);
                dest.writeString(this.mDisplayName);
            case HDMI_DEVICE_TYPE_MHL /*1*/:
                dest.writeInt(this.mDeviceId);
                dest.writeInt(this.mAdopterId);
            default:
        }
    }

    public String toString() {
        StringBuffer append;
        Object[] objArr;
        StringBuffer s = new StringBuffer();
        switch (this.mHdmiDeviceType) {
            case PATH_INTERNAL /*0*/:
                s.append("CEC: ");
                append = s.append("logical_address: ");
                objArr = new Object[HDMI_DEVICE_TYPE_MHL];
                objArr[PATH_INTERNAL] = Integer.valueOf(this.mLogicalAddress);
                append.append(String.format("0x%02X", objArr));
                s.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                s.append("device_type: ").append(this.mDeviceType).append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                s.append("vendor_id: ").append(this.mVendorId).append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                s.append("display_name: ").append(this.mDisplayName).append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                s.append("power_status: ").append(this.mDevicePowerStatus).append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                break;
            case HDMI_DEVICE_TYPE_MHL /*1*/:
                s.append("MHL: ");
                append = s.append("device_id: ");
                objArr = new Object[HDMI_DEVICE_TYPE_MHL];
                objArr[PATH_INTERNAL] = Integer.valueOf(this.mDeviceId);
                append.append(String.format("0x%04X", objArr)).append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                append = s.append("adopter_id: ");
                objArr = new Object[HDMI_DEVICE_TYPE_MHL];
                objArr[PATH_INTERNAL] = Integer.valueOf(this.mAdopterId);
                append.append(String.format("0x%04X", objArr)).append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                break;
            case HDMI_DEVICE_TYPE_HARDWARE /*2*/:
                s.append("Hardware: ");
                break;
            case HDMI_DEVICE_TYPE_INACTIVE /*100*/:
                s.append("Inactivated: ");
                break;
            default:
                return ProxyInfo.LOCAL_EXCL_LIST;
        }
        append = s.append("physical_address: ");
        objArr = new Object[HDMI_DEVICE_TYPE_MHL];
        objArr[PATH_INTERNAL] = Integer.valueOf(this.mPhysicalAddress);
        append.append(String.format("0x%04X", objArr));
        s.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
        s.append("port_id: ").append(this.mPortId);
        return s.toString();
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (!(obj instanceof HdmiDeviceInfo)) {
            return false;
        }
        HdmiDeviceInfo other = (HdmiDeviceInfo) obj;
        if (this.mHdmiDeviceType == other.mHdmiDeviceType && this.mPhysicalAddress == other.mPhysicalAddress && this.mPortId == other.mPortId && this.mLogicalAddress == other.mLogicalAddress && this.mDeviceType == other.mDeviceType && this.mVendorId == other.mVendorId && this.mDevicePowerStatus == other.mDevicePowerStatus && this.mDisplayName.equals(other.mDisplayName) && this.mDeviceId == other.mDeviceId && this.mAdopterId == other.mAdopterId) {
            z = true;
        }
        return z;
    }
}
