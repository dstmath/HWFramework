package android.net.wifi.p2p;

import android.net.ProxyInfo;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.WpsInfo;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.telecom.AudioState;

public class WifiP2pConfig implements Parcelable {
    public static final Creator<WifiP2pConfig> CREATOR = null;
    public static final int MAX_GROUP_OWNER_INTENT = 15;
    public static final int MIN_GROUP_OWNER_INTENT = 0;
    public String deviceAddress;
    public int groupOwnerIntent;
    public int netId;
    public WpsInfo wps;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.wifi.p2p.WifiP2pConfig.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.wifi.p2p.WifiP2pConfig.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.p2p.WifiP2pConfig.<clinit>():void");
    }

    public WifiP2pConfig() {
        this.deviceAddress = ProxyInfo.LOCAL_EXCL_LIST;
        this.groupOwnerIntent = -1;
        this.netId = -2;
        this.wps = new WpsInfo();
        this.wps.setup = 0;
    }

    public void invalidate() {
        this.deviceAddress = ProxyInfo.LOCAL_EXCL_LIST;
    }

    public WifiP2pConfig(String supplicantEvent) throws IllegalArgumentException {
        this.deviceAddress = ProxyInfo.LOCAL_EXCL_LIST;
        this.groupOwnerIntent = -1;
        this.netId = -2;
        String[] tokens = supplicantEvent.split(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
        if (tokens.length < 2 || !tokens[0].equals("P2P-GO-NEG-REQUEST")) {
            throw new IllegalArgumentException("Malformed supplicant event");
        }
        this.deviceAddress = tokens[1];
        this.wps = new WpsInfo();
        if (tokens.length > 2) {
            int devPasswdId;
            try {
                devPasswdId = Integer.parseInt(tokens[2].split("=")[1]);
            } catch (NumberFormatException e) {
                devPasswdId = 0;
            }
            switch (devPasswdId) {
                case AudioState.ROUTE_EARPIECE /*1*/:
                    this.wps.setup = 1;
                case AudioState.ROUTE_WIRED_HEADSET /*4*/:
                    this.wps.setup = 0;
                case AudioState.ROUTE_WIRED_OR_EARPIECE /*5*/:
                    this.wps.setup = 2;
                default:
                    this.wps.setup = 0;
            }
        }
    }

    public String toString() {
        StringBuffer sbuf = new StringBuffer();
        sbuf.append("\n address: ").append(this.deviceAddress);
        sbuf.append("\n wps: ").append(this.wps);
        sbuf.append("\n groupOwnerIntent: ").append(this.groupOwnerIntent);
        sbuf.append("\n persist: ").append(this.netId);
        return sbuf.toString();
    }

    public int describeContents() {
        return 0;
    }

    public WifiP2pConfig(WifiP2pConfig source) {
        this.deviceAddress = ProxyInfo.LOCAL_EXCL_LIST;
        this.groupOwnerIntent = -1;
        this.netId = -2;
        if (source != null) {
            this.deviceAddress = source.deviceAddress;
            this.wps = new WpsInfo(source.wps);
            this.groupOwnerIntent = source.groupOwnerIntent;
            this.netId = source.netId;
        }
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.deviceAddress);
        dest.writeParcelable(this.wps, flags);
        dest.writeInt(this.groupOwnerIntent);
        dest.writeInt(this.netId);
    }
}
