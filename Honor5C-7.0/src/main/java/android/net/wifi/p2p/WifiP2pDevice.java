package android.net.wifi.p2p;

import android.net.ProxyInfo;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.Log;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WifiP2pDevice implements Parcelable {
    public static final int AVAILABLE = 3;
    public static final int CONNECTED = 0;
    public static final Creator<WifiP2pDevice> CREATOR = null;
    private static final int DEVICE_CAPAB_CLIENT_DISCOVERABILITY = 2;
    private static final int DEVICE_CAPAB_CONCURRENT_OPER = 4;
    private static final int DEVICE_CAPAB_DEVICE_LIMIT = 16;
    private static final int DEVICE_CAPAB_INFRA_MANAGED = 8;
    private static final int DEVICE_CAPAB_INVITATION_PROCEDURE = 32;
    private static final int DEVICE_CAPAB_SERVICE_DISCOVERY = 1;
    public static final int FAILED = 2;
    private static final int GROUP_CAPAB_CROSS_CONN = 16;
    private static final int GROUP_CAPAB_GROUP_FORMATION = 64;
    private static final int GROUP_CAPAB_GROUP_LIMIT = 4;
    private static final int GROUP_CAPAB_GROUP_OWNER = 1;
    private static final int GROUP_CAPAB_INTRA_BSS_DIST = 8;
    private static final int GROUP_CAPAB_PERSISTENT_GROUP = 2;
    private static final int GROUP_CAPAB_PERSISTENT_RECONN = 32;
    public static final int INVITED = 1;
    private static final String TAG = "WifiP2pDevice";
    public static final int UNAVAILABLE = 4;
    private static final int WPS_CONFIG_DISPLAY = 8;
    private static final int WPS_CONFIG_KEYPAD = 256;
    private static final int WPS_CONFIG_PUSHBUTTON = 128;
    private static final Pattern detailedDevicePattern = null;
    private static final Pattern threeTokenPattern = null;
    private static final Pattern twoTokenPattern = null;
    public String deviceAddress;
    public int deviceCapability;
    public String deviceName;
    public int groupCapability;
    public String primaryDeviceType;
    public String secondaryDeviceType;
    public int status;
    public WifiP2pWfdInfo wfdInfo;
    public int wpsConfigMethodsSupported;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.wifi.p2p.WifiP2pDevice.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.wifi.p2p.WifiP2pDevice.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.p2p.WifiP2pDevice.<clinit>():void");
    }

    public WifiP2pDevice() {
        this.deviceName = ProxyInfo.LOCAL_EXCL_LIST;
        this.deviceAddress = ProxyInfo.LOCAL_EXCL_LIST;
        this.status = UNAVAILABLE;
    }

    public WifiP2pDevice(String string) throws IllegalArgumentException {
        this.deviceName = ProxyInfo.LOCAL_EXCL_LIST;
        this.deviceAddress = ProxyInfo.LOCAL_EXCL_LIST;
        this.status = UNAVAILABLE;
        String[] tokens = string.split("[ \n]");
        if (tokens.length < INVITED) {
            throw new IllegalArgumentException("Malformed supplicant event");
        }
        Matcher match;
        switch (tokens.length) {
            case INVITED /*1*/:
                this.deviceAddress = string;
            case GROUP_CAPAB_PERSISTENT_GROUP /*2*/:
                match = twoTokenPattern.matcher(string);
                if (match.find()) {
                    this.deviceAddress = match.group(GROUP_CAPAB_PERSISTENT_GROUP);
                    return;
                }
                throw new IllegalArgumentException("Malformed supplicant event");
            case AVAILABLE /*3*/:
                match = threeTokenPattern.matcher(string);
                if (match.find()) {
                    this.deviceAddress = match.group(INVITED);
                    return;
                }
                throw new IllegalArgumentException("Malformed supplicant event");
            default:
                match = detailedDevicePattern.matcher(string);
                if (match.find()) {
                    this.deviceAddress = match.group(AVAILABLE);
                    this.primaryDeviceType = match.group(UNAVAILABLE);
                    this.deviceName = match.group(5);
                    this.wpsConfigMethodsSupported = parseHex(match.group(6));
                    this.deviceCapability = parseHex(match.group(7));
                    this.groupCapability = parseHex(match.group(WPS_CONFIG_DISPLAY));
                    if (match.group(9) != null) {
                        String str = match.group(10);
                        this.wfdInfo = new WifiP2pWfdInfo(parseHex(str.substring(CONNECTED, UNAVAILABLE)), parseHex(str.substring(UNAVAILABLE, WPS_CONFIG_DISPLAY)), parseHex(str.substring(WPS_CONFIG_DISPLAY, 12)));
                    }
                    if (tokens[CONNECTED].startsWith("P2P-DEVICE-FOUND")) {
                        this.status = AVAILABLE;
                    }
                    return;
                }
                throw new IllegalArgumentException("Malformed supplicant event");
        }
    }

    public boolean wpsPbcSupported() {
        return (this.wpsConfigMethodsSupported & WPS_CONFIG_PUSHBUTTON) != 0;
    }

    public boolean wpsKeypadSupported() {
        return (this.wpsConfigMethodsSupported & WPS_CONFIG_KEYPAD) != 0;
    }

    public boolean wpsDisplaySupported() {
        return (this.wpsConfigMethodsSupported & WPS_CONFIG_DISPLAY) != 0;
    }

    public boolean isServiceDiscoveryCapable() {
        return (this.deviceCapability & INVITED) != 0;
    }

    public boolean isInvitationCapable() {
        return (this.deviceCapability & GROUP_CAPAB_PERSISTENT_RECONN) != 0;
    }

    public boolean isDeviceLimit() {
        return (this.deviceCapability & GROUP_CAPAB_CROSS_CONN) != 0;
    }

    public boolean isGroupOwner() {
        return (this.groupCapability & INVITED) != 0;
    }

    public boolean isGroupLimit() {
        return (this.groupCapability & UNAVAILABLE) != 0;
    }

    public void update(WifiP2pDevice device) {
        updateSupplicantDetails(device);
        this.status = device.status;
    }

    public void updateSupplicantDetails(WifiP2pDevice device) {
        if (device == null) {
            throw new IllegalArgumentException("device is null");
        } else if (device.deviceAddress == null) {
            throw new IllegalArgumentException("deviceAddress is null");
        } else if (this.deviceAddress.equals(device.deviceAddress)) {
            this.deviceName = device.deviceName;
            this.primaryDeviceType = device.primaryDeviceType;
            this.secondaryDeviceType = device.secondaryDeviceType;
            this.wpsConfigMethodsSupported = device.wpsConfigMethodsSupported;
            this.deviceCapability = device.deviceCapability;
            this.groupCapability = device.groupCapability;
            this.wfdInfo = device.wfdInfo;
        } else {
            throw new IllegalArgumentException("deviceAddress does not match");
        }
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof WifiP2pDevice)) {
            return false;
        }
        WifiP2pDevice other = (WifiP2pDevice) obj;
        if (other != null && other.deviceAddress != null) {
            return other.deviceAddress.equals(this.deviceAddress);
        }
        if (this.deviceAddress != null) {
            z = false;
        }
        return z;
    }

    public String toString() {
        StringBuffer sbuf = new StringBuffer();
        sbuf.append("Device: ").append(this.deviceName);
        sbuf.append("\n primary type: ").append(this.primaryDeviceType);
        sbuf.append("\n secondary type: ").append(this.secondaryDeviceType);
        sbuf.append("\n wps: ").append(this.wpsConfigMethodsSupported);
        sbuf.append("\n grpcapab: ").append(this.groupCapability);
        sbuf.append("\n devcapab: ").append(this.deviceCapability);
        sbuf.append("\n status: ").append(this.status);
        sbuf.append("\n wfdInfo: ").append(this.wfdInfo);
        return sbuf.toString();
    }

    public int describeContents() {
        return CONNECTED;
    }

    public WifiP2pDevice(WifiP2pDevice source) {
        this.deviceName = ProxyInfo.LOCAL_EXCL_LIST;
        this.deviceAddress = ProxyInfo.LOCAL_EXCL_LIST;
        this.status = UNAVAILABLE;
        if (source != null) {
            this.deviceName = source.deviceName;
            this.deviceAddress = source.deviceAddress;
            this.primaryDeviceType = source.primaryDeviceType;
            this.secondaryDeviceType = source.secondaryDeviceType;
            this.wpsConfigMethodsSupported = source.wpsConfigMethodsSupported;
            this.deviceCapability = source.deviceCapability;
            this.groupCapability = source.groupCapability;
            this.status = source.status;
            this.wfdInfo = new WifiP2pWfdInfo(source.wfdInfo);
        }
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.deviceName);
        dest.writeString(this.deviceAddress);
        dest.writeString(this.primaryDeviceType);
        dest.writeString(this.secondaryDeviceType);
        dest.writeInt(this.wpsConfigMethodsSupported);
        dest.writeInt(this.deviceCapability);
        dest.writeInt(this.groupCapability);
        dest.writeInt(this.status);
        if (this.wfdInfo != null) {
            dest.writeInt(INVITED);
            this.wfdInfo.writeToParcel(dest, flags);
            return;
        }
        dest.writeInt(CONNECTED);
    }

    private int parseHex(String hexString) {
        int num = CONNECTED;
        if (hexString.startsWith("0x") || hexString.startsWith("0X")) {
            hexString = hexString.substring(GROUP_CAPAB_PERSISTENT_GROUP);
        }
        try {
            num = Integer.parseInt(hexString, GROUP_CAPAB_CROSS_CONN);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Failed to parse hex string " + hexString);
        }
        return num;
    }
}
