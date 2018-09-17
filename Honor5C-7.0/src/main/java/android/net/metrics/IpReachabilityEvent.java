package android.net.metrics;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.Process;
import android.util.SparseArray;

public final class IpReachabilityEvent extends IpConnectivityEvent implements Parcelable {
    public static final Creator<IpReachabilityEvent> CREATOR = null;
    public static final int NUD_FAILED = 512;
    public static final int PROBE = 256;
    public static final int PROVISIONING_LOST = 768;
    public final int eventType;
    public final String ifName;

    static final class Decoder {
        static final SparseArray<String> constants = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.metrics.IpReachabilityEvent.Decoder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.metrics.IpReachabilityEvent.Decoder.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.net.metrics.IpReachabilityEvent.Decoder.<clinit>():void");
        }

        Decoder() {
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.metrics.IpReachabilityEvent.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.metrics.IpReachabilityEvent.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.net.metrics.IpReachabilityEvent.<clinit>():void");
    }

    /* synthetic */ IpReachabilityEvent(Parcel in, IpReachabilityEvent ipReachabilityEvent) {
        this(in);
    }

    private IpReachabilityEvent(String ifName, int eventType) {
        this.ifName = ifName;
        this.eventType = eventType;
    }

    private IpReachabilityEvent(Parcel in) {
        this.ifName = in.readString();
        this.eventType = in.readInt();
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.ifName);
        out.writeInt(this.eventType);
    }

    public int describeContents() {
        return 0;
    }

    public static void logProbeEvent(String ifName, int nlErrorCode) {
        IpConnectivityEvent.logEvent(new IpReachabilityEvent(ifName, (nlErrorCode & Process.PROC_TERM_MASK) | PROBE));
    }

    public static void logNudFailed(String ifName) {
        IpConnectivityEvent.logEvent(new IpReachabilityEvent(ifName, (int) NUD_FAILED));
    }

    public static void logProvisioningLost(String ifName) {
        IpConnectivityEvent.logEvent(new IpReachabilityEvent(ifName, (int) PROVISIONING_LOST));
    }

    public String toString() {
        return String.format("IpReachabilityEvent(%s, %s)", new Object[]{this.ifName, Decoder.constants.get(this.eventType)});
    }
}
