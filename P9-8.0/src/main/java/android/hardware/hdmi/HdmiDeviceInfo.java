package android.hardware.hdmi;

import android.net.ProxyInfo;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class HdmiDeviceInfo implements Parcelable {
    public static final int ADDR_INTERNAL = 0;
    public static final Creator<HdmiDeviceInfo> CREATOR = new Creator<HdmiDeviceInfo>() {
        public HdmiDeviceInfo createFromParcel(Parcel source) {
            int hdmiDeviceType = source.readInt();
            int physicalAddress = source.readInt();
            int portId = source.readInt();
            switch (hdmiDeviceType) {
                case 0:
                    return new HdmiDeviceInfo(source.readInt(), physicalAddress, portId, source.readInt(), source.readInt(), source.readString(), source.readInt());
                case 1:
                    return new HdmiDeviceInfo(physicalAddress, portId, source.readInt(), source.readInt());
                case 2:
                    return new HdmiDeviceInfo(physicalAddress, portId);
                case 100:
                    return HdmiDeviceInfo.INACTIVE_DEVICE;
                default:
                    return null;
            }
        }

        public HdmiDeviceInfo[] newArray(int size) {
            return new HdmiDeviceInfo[size];
        }
    };
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
    public static final HdmiDeviceInfo INACTIVE_DEVICE = new HdmiDeviceInfo();
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

    public HdmiDeviceInfo(int logicalAddress, int physicalAddress, int portId, int deviceType, int vendorId, String displayName, int powerStatus) {
        this.mHdmiDeviceType = 0;
        this.mPhysicalAddress = physicalAddress;
        this.mPortId = portId;
        this.mId = idForCecDevice(logicalAddress);
        this.mLogicalAddress = logicalAddress;
        this.mDeviceType = deviceType;
        this.mVendorId = vendorId;
        this.mDevicePowerStatus = powerStatus;
        this.mDisplayName = displayName;
        this.mDeviceId = -1;
        this.mAdopterId = -1;
    }

    public HdmiDeviceInfo(int logicalAddress, int physicalAddress, int portId, int deviceType, int vendorId, String displayName) {
        this(logicalAddress, physicalAddress, portId, deviceType, vendorId, displayName, -1);
    }

    public HdmiDeviceInfo(int physicalAddress, int portId) {
        this.mHdmiDeviceType = 2;
        this.mPhysicalAddress = physicalAddress;
        this.mPortId = portId;
        this.mId = idForHardware(portId);
        this.mLogicalAddress = -1;
        this.mDeviceType = 2;
        this.mVendorId = 0;
        this.mDevicePowerStatus = -1;
        this.mDisplayName = "HDMI" + portId;
        this.mDeviceId = -1;
        this.mAdopterId = -1;
    }

    public HdmiDeviceInfo(int physicalAddress, int portId, int adopterId, int deviceId) {
        this.mHdmiDeviceType = 1;
        this.mPhysicalAddress = physicalAddress;
        this.mPortId = portId;
        this.mId = idForMhlDevice(portId);
        this.mLogicalAddress = -1;
        this.mDeviceType = 2;
        this.mVendorId = 0;
        this.mDevicePowerStatus = -1;
        this.mDisplayName = "Mobile";
        this.mDeviceId = adopterId;
        this.mAdopterId = deviceId;
    }

    public HdmiDeviceInfo() {
        this.mHdmiDeviceType = 100;
        this.mPhysicalAddress = 65535;
        this.mId = 65535;
        this.mLogicalAddress = -1;
        this.mDeviceType = -1;
        this.mPortId = -1;
        this.mDevicePowerStatus = -1;
        this.mDisplayName = "Inactive";
        this.mVendorId = 0;
        this.mDeviceId = -1;
        this.mAdopterId = -1;
    }

    public int getId() {
        return this.mId;
    }

    public static int idForCecDevice(int address) {
        return address + 0;
    }

    public static int idForMhlDevice(int portId) {
        return portId + 128;
    }

    public static int idForHardware(int portId) {
        return portId + 192;
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
            if (!(this.mDeviceType == 4 || this.mDeviceType == 1 || this.mDeviceType == 3)) {
                z = false;
            }
            return z;
        }
    }

    public boolean isCecDevice() {
        return this.mHdmiDeviceType == 0;
    }

    public boolean isMhlDevice() {
        return this.mHdmiDeviceType == 1;
    }

    public boolean isInactivated() {
        return this.mHdmiDeviceType == 100;
    }

    public String getDisplayName() {
        return this.mDisplayName;
    }

    public int getVendorId() {
        return this.mVendorId;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mHdmiDeviceType);
        dest.writeInt(this.mPhysicalAddress);
        dest.writeInt(this.mPortId);
        switch (this.mHdmiDeviceType) {
            case 0:
                dest.writeInt(this.mLogicalAddress);
                dest.writeInt(this.mDeviceType);
                dest.writeInt(this.mVendorId);
                dest.writeInt(this.mDevicePowerStatus);
                dest.writeString(this.mDisplayName);
                return;
            case 1:
                dest.writeInt(this.mDeviceId);
                dest.writeInt(this.mAdopterId);
                return;
            default:
                return;
        }
    }

    public String toString() {
        StringBuffer s = new StringBuffer();
        switch (this.mHdmiDeviceType) {
            case 0:
                s.append("CEC: ");
                s.append("logical_address: ").append(String.format("0x%02X", new Object[]{Integer.valueOf(this.mLogicalAddress)}));
                s.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                s.append("device_type: ").append(this.mDeviceType).append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                s.append("vendor_id: ").append(this.mVendorId).append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                s.append("display_name: ").append(this.mDisplayName).append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                s.append("power_status: ").append(this.mDevicePowerStatus).append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                break;
            case 1:
                s.append("MHL: ");
                s.append("device_id: ").append(String.format("0x%04X", new Object[]{Integer.valueOf(this.mDeviceId)})).append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                s.append("adopter_id: ").append(String.format("0x%04X", new Object[]{Integer.valueOf(this.mAdopterId)})).append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                break;
            case 2:
                s.append("Hardware: ");
                break;
            case 100:
                s.append("Inactivated: ");
                break;
            default:
                return ProxyInfo.LOCAL_EXCL_LIST;
        }
        s.append("physical_address: ").append(String.format("0x%04X", new Object[]{Integer.valueOf(this.mPhysicalAddress)}));
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
