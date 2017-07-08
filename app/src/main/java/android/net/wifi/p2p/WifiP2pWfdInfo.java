package android.net.wifi.p2p;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.Locale;

public class WifiP2pWfdInfo implements Parcelable {
    private static final int COUPLED_SINK_SUPPORT_AT_SINK = 8;
    private static final int COUPLED_SINK_SUPPORT_AT_SOURCE = 4;
    public static final Creator<WifiP2pWfdInfo> CREATOR = null;
    private static final int DEVICE_TYPE = 3;
    public static final int PRIMARY_SINK = 1;
    public static final int SECONDARY_SINK = 2;
    private static final int SESSION_AVAILABLE = 48;
    private static final int SESSION_AVAILABLE_BIT1 = 16;
    private static final int SESSION_AVAILABLE_BIT2 = 32;
    public static final int SOURCE_OR_PRIMARY_SINK = 3;
    private static final String TAG = "WifiP2pWfdInfo";
    public static final int WFD_SOURCE = 0;
    private int mCtrlPort;
    private int mDeviceInfo;
    private int mMaxThroughput;
    private boolean mWfdEnabled;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.wifi.p2p.WifiP2pWfdInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.wifi.p2p.WifiP2pWfdInfo.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.p2p.WifiP2pWfdInfo.<clinit>():void");
    }

    public WifiP2pWfdInfo(int devInfo, int ctrlPort, int maxTput) {
        this.mWfdEnabled = true;
        this.mDeviceInfo = devInfo;
        this.mCtrlPort = ctrlPort;
        this.mMaxThroughput = maxTput;
    }

    public boolean isWfdEnabled() {
        return this.mWfdEnabled;
    }

    public void setWfdEnabled(boolean enabled) {
        this.mWfdEnabled = enabled;
    }

    public int getDeviceType() {
        return this.mDeviceInfo & SOURCE_OR_PRIMARY_SINK;
    }

    public boolean setDeviceType(int deviceType) {
        if (deviceType < 0 || deviceType > SOURCE_OR_PRIMARY_SINK) {
            return false;
        }
        this.mDeviceInfo &= -4;
        this.mDeviceInfo |= deviceType;
        return true;
    }

    public boolean isCoupledSinkSupportedAtSource() {
        return (this.mDeviceInfo & COUPLED_SINK_SUPPORT_AT_SINK) != 0;
    }

    public void setCoupledSinkSupportAtSource(boolean enabled) {
        if (enabled) {
            this.mDeviceInfo |= COUPLED_SINK_SUPPORT_AT_SINK;
        } else {
            this.mDeviceInfo &= -9;
        }
    }

    public boolean isCoupledSinkSupportedAtSink() {
        return (this.mDeviceInfo & COUPLED_SINK_SUPPORT_AT_SINK) != 0;
    }

    public void setCoupledSinkSupportAtSink(boolean enabled) {
        if (enabled) {
            this.mDeviceInfo |= COUPLED_SINK_SUPPORT_AT_SINK;
        } else {
            this.mDeviceInfo &= -9;
        }
    }

    public boolean isSessionAvailable() {
        return (this.mDeviceInfo & SESSION_AVAILABLE) != 0;
    }

    public void setSessionAvailable(boolean enabled) {
        if (enabled) {
            this.mDeviceInfo |= SESSION_AVAILABLE_BIT1;
            this.mDeviceInfo &= -33;
            return;
        }
        this.mDeviceInfo &= -49;
    }

    public int getControlPort() {
        return this.mCtrlPort;
    }

    public void setControlPort(int port) {
        this.mCtrlPort = port;
    }

    public void setMaxThroughput(int maxThroughput) {
        this.mMaxThroughput = maxThroughput;
    }

    public int getMaxThroughput() {
        return this.mMaxThroughput;
    }

    public String getDeviceInfoHex() {
        Object[] objArr = new Object[COUPLED_SINK_SUPPORT_AT_SOURCE];
        objArr[0] = Integer.valueOf(6);
        objArr[PRIMARY_SINK] = Integer.valueOf(this.mDeviceInfo);
        objArr[SECONDARY_SINK] = Integer.valueOf(this.mCtrlPort);
        objArr[SOURCE_OR_PRIMARY_SINK] = Integer.valueOf(this.mMaxThroughput);
        return String.format(Locale.US, "%04x%04x%04x%04x", objArr);
    }

    public String toString() {
        StringBuffer sbuf = new StringBuffer();
        sbuf.append("WFD enabled: ").append(this.mWfdEnabled);
        sbuf.append("WFD DeviceInfo: ").append(this.mDeviceInfo);
        sbuf.append("\n WFD CtrlPort: ").append(this.mCtrlPort);
        sbuf.append("\n WFD MaxThroughput: ").append(this.mMaxThroughput);
        return sbuf.toString();
    }

    public int describeContents() {
        return 0;
    }

    public WifiP2pWfdInfo(WifiP2pWfdInfo source) {
        if (source != null) {
            this.mWfdEnabled = source.mWfdEnabled;
            this.mDeviceInfo = source.mDeviceInfo;
            this.mCtrlPort = source.mCtrlPort;
            this.mMaxThroughput = source.mMaxThroughput;
        }
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mWfdEnabled ? PRIMARY_SINK : 0);
        dest.writeInt(this.mDeviceInfo);
        dest.writeInt(this.mCtrlPort);
        dest.writeInt(this.mMaxThroughput);
    }

    public void readFromParcel(Parcel in) {
        boolean z = true;
        if (in.readInt() != PRIMARY_SINK) {
            z = false;
        }
        this.mWfdEnabled = z;
        this.mDeviceInfo = in.readInt();
        this.mCtrlPort = in.readInt();
        this.mMaxThroughput = in.readInt();
    }
}
