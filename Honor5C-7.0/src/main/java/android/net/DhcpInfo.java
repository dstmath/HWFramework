package android.net;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class DhcpInfo implements Parcelable {
    public static final Creator<DhcpInfo> CREATOR = null;
    public int dns1;
    public int dns2;
    public int gateway;
    public int ipAddress;
    public int leaseDuration;
    public int netmask;
    public int serverAddress;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.DhcpInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.DhcpInfo.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.net.DhcpInfo.<clinit>():void");
    }

    public DhcpInfo(DhcpInfo source) {
        if (source != null) {
            this.ipAddress = source.ipAddress;
            this.gateway = source.gateway;
            this.netmask = source.netmask;
            this.dns1 = source.dns1;
            this.dns2 = source.dns2;
            this.serverAddress = source.serverAddress;
            this.leaseDuration = source.leaseDuration;
        }
    }

    public String toString() {
        StringBuffer str = new StringBuffer();
        str.append("ipaddr ");
        putAddress(str, this.ipAddress);
        str.append(" gateway ");
        putAddress(str, this.gateway);
        str.append(" netmask ");
        putAddress(str, this.netmask);
        str.append(" dns1 ");
        putAddress(str, this.dns1);
        str.append(" dns2 ");
        putAddress(str, this.dns2);
        str.append(" DHCP server ");
        putAddress(str, this.serverAddress);
        str.append(" lease ").append(this.leaseDuration).append(" seconds");
        return str.toString();
    }

    private static void putAddress(StringBuffer buf, int addr) {
        buf.append(NetworkUtils.intToInetAddress(addr).getHostAddress());
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.ipAddress);
        dest.writeInt(this.gateway);
        dest.writeInt(this.netmask);
        dest.writeInt(this.dns1);
        dest.writeInt(this.dns2);
        dest.writeInt(this.serverAddress);
        dest.writeInt(this.leaseDuration);
    }
}
