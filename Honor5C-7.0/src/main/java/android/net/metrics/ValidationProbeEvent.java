package android.net.metrics;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.SparseArray;

public final class ValidationProbeEvent extends IpConnectivityEvent implements Parcelable {
    public static final Creator<ValidationProbeEvent> CREATOR = null;
    public static final int DNS_FAILURE = 0;
    public static final int DNS_SUCCESS = 1;
    public static final int PROBE_DNS = 0;
    public static final int PROBE_HTTP = 1;
    public static final int PROBE_HTTPS = 2;
    public static final int PROBE_PAC = 3;
    public final long durationMs;
    public final int netId;
    public final int probeType;
    public final int returnCode;

    static final class Decoder {
        static final SparseArray<String> constants = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.metrics.ValidationProbeEvent.Decoder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.metrics.ValidationProbeEvent.Decoder.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.net.metrics.ValidationProbeEvent.Decoder.<clinit>():void");
        }

        Decoder() {
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.metrics.ValidationProbeEvent.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.metrics.ValidationProbeEvent.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.net.metrics.ValidationProbeEvent.<clinit>():void");
    }

    /* synthetic */ ValidationProbeEvent(Parcel in, ValidationProbeEvent validationProbeEvent) {
        this(in);
    }

    private ValidationProbeEvent(int netId, long durationMs, int probeType, int returnCode) {
        this.netId = netId;
        this.durationMs = durationMs;
        this.probeType = probeType;
        this.returnCode = returnCode;
    }

    private ValidationProbeEvent(Parcel in) {
        this.netId = in.readInt();
        this.durationMs = in.readLong();
        this.probeType = in.readInt();
        this.returnCode = in.readInt();
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.netId);
        out.writeLong(this.durationMs);
        out.writeInt(this.probeType);
        out.writeInt(this.returnCode);
    }

    public int describeContents() {
        return PROBE_DNS;
    }

    public static String getProbeName(int probeType) {
        return (String) Decoder.constants.get(probeType, "PROBE_???");
    }

    public static void logEvent(int netId, long durationMs, int probeType, int returnCode) {
        IpConnectivityEvent.logEvent(new ValidationProbeEvent(netId, durationMs, probeType, returnCode));
    }

    public String toString() {
        return String.format("ValidationProbeEvent(%d, %s:%d, %dms)", new Object[]{Integer.valueOf(this.netId), getProbeName(this.probeType), Integer.valueOf(this.returnCode), Long.valueOf(this.durationMs)});
    }
}
