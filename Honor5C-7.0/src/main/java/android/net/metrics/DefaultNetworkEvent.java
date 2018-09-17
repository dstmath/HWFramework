package android.net.metrics;

import android.net.NetworkCapabilities;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.security.keystore.KeyProperties;

public final class DefaultNetworkEvent extends IpConnectivityEvent implements Parcelable {
    public static final Creator<DefaultNetworkEvent> CREATOR = null;
    public final int netId;
    public final boolean prevIPv4;
    public final boolean prevIPv6;
    public final int prevNetId;
    public final int[] transportTypes;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.metrics.DefaultNetworkEvent.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.metrics.DefaultNetworkEvent.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.net.metrics.DefaultNetworkEvent.<clinit>():void");
    }

    private DefaultNetworkEvent(int netId, int[] transportTypes, int prevNetId, boolean prevIPv4, boolean prevIPv6) {
        this.netId = netId;
        this.transportTypes = transportTypes;
        this.prevNetId = prevNetId;
        this.prevIPv4 = prevIPv4;
        this.prevIPv6 = prevIPv6;
    }

    private DefaultNetworkEvent(Parcel in) {
        boolean z;
        boolean z2 = true;
        this.netId = in.readInt();
        this.transportTypes = in.createIntArray();
        this.prevNetId = in.readInt();
        if (in.readByte() > null) {
            z = true;
        } else {
            z = false;
        }
        this.prevIPv4 = z;
        if (in.readByte() <= null) {
            z2 = false;
        }
        this.prevIPv6 = z2;
    }

    public void writeToParcel(Parcel out, int flags) {
        byte b;
        byte b2 = (byte) 1;
        out.writeInt(this.netId);
        out.writeIntArray(this.transportTypes);
        out.writeInt(this.prevNetId);
        if (this.prevIPv4) {
            b = (byte) 1;
        } else {
            b = (byte) 0;
        }
        out.writeByte(b);
        if (!this.prevIPv6) {
            b2 = (byte) 0;
        }
        out.writeByte(b2);
    }

    public int describeContents() {
        return 0;
    }

    public String toString() {
        String prevNetwork = String.valueOf(this.prevNetId);
        String newNetwork = String.valueOf(this.netId);
        if (this.prevNetId != 0) {
            prevNetwork = prevNetwork + ":" + ipSupport();
        }
        if (this.netId != 0) {
            newNetwork = newNetwork + ":" + NetworkCapabilities.transportNamesOf(this.transportTypes);
        }
        return String.format("DefaultNetworkEvent(%s -> %s)", new Object[]{prevNetwork, newNetwork});
    }

    private String ipSupport() {
        if (this.prevIPv4 && this.prevIPv6) {
            return "DUAL";
        }
        if (this.prevIPv6) {
            return "IPv6";
        }
        if (this.prevIPv4) {
            return "IPv4";
        }
        return KeyProperties.DIGEST_NONE;
    }

    public static void logEvent(int netId, int[] transports, int prevNetId, boolean hadIPv4, boolean hadIPv6) {
        IpConnectivityEvent.logEvent(new DefaultNetworkEvent(netId, transports, prevNetId, hadIPv4, hadIPv6));
    }
}
