package android.net.metrics;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.SparseArray;

public final class NetworkEvent extends IpConnectivityEvent implements Parcelable {
    public static final Creator<NetworkEvent> CREATOR = null;
    public static final int NETWORK_CAPTIVE_PORTAL_FOUND = 4;
    public static final int NETWORK_CONNECTED = 1;
    public static final int NETWORK_DISCONNECTED = 7;
    public static final int NETWORK_LINGER = 5;
    public static final int NETWORK_UNLINGER = 6;
    public static final int NETWORK_VALIDATED = 2;
    public static final int NETWORK_VALIDATION_FAILED = 3;
    public final long durationMs;
    public final int eventType;
    public final int netId;

    static final class Decoder {
        static final SparseArray<String> constants = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.metrics.NetworkEvent.Decoder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.metrics.NetworkEvent.Decoder.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.metrics.NetworkEvent.Decoder.<clinit>():void");
        }

        Decoder() {
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.metrics.NetworkEvent.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.metrics.NetworkEvent.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.net.metrics.NetworkEvent.<clinit>():void");
    }

    /* synthetic */ NetworkEvent(Parcel in, NetworkEvent networkEvent) {
        this(in);
    }

    private NetworkEvent(int netId, int eventType, long durationMs) {
        this.netId = netId;
        this.eventType = eventType;
        this.durationMs = durationMs;
    }

    private NetworkEvent(Parcel in) {
        this.netId = in.readInt();
        this.eventType = in.readInt();
        this.durationMs = in.readLong();
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.netId);
        out.writeInt(this.eventType);
        out.writeLong(this.durationMs);
    }

    public int describeContents() {
        return 0;
    }

    public static void logEvent(int netId, int eventType) {
        IpConnectivityEvent.logEvent(new NetworkEvent(netId, eventType, 0));
    }

    public static void logValidated(int netId, long durationMs) {
        IpConnectivityEvent.logEvent(new NetworkEvent(netId, NETWORK_VALIDATED, durationMs));
    }

    public static void logCaptivePortalFound(int netId, long durationMs) {
        IpConnectivityEvent.logEvent(new NetworkEvent(netId, NETWORK_CAPTIVE_PORTAL_FOUND, durationMs));
    }

    public String toString() {
        Object[] objArr = new Object[NETWORK_VALIDATION_FAILED];
        objArr[0] = Integer.valueOf(this.netId);
        objArr[NETWORK_CONNECTED] = Decoder.constants.get(this.eventType);
        objArr[NETWORK_VALIDATED] = Long.valueOf(this.durationMs);
        return String.format("NetworkEvent(%d, %s, %dms)", objArr);
    }
}
