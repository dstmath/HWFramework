package android.net.metrics;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class DnsEvent extends IpConnectivityEvent implements Parcelable {
    public static final Creator<DnsEvent> CREATOR = null;
    public final byte[] eventTypes;
    public final int[] latenciesMs;
    public final int netId;
    public final byte[] returnCodes;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.metrics.DnsEvent.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.metrics.DnsEvent.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.net.metrics.DnsEvent.<clinit>():void");
    }

    private DnsEvent(int netId, byte[] eventTypes, byte[] returnCodes, int[] latenciesMs) {
        this.netId = netId;
        this.eventTypes = eventTypes;
        this.returnCodes = returnCodes;
        this.latenciesMs = latenciesMs;
    }

    private DnsEvent(Parcel in) {
        this.netId = in.readInt();
        this.eventTypes = in.createByteArray();
        this.returnCodes = in.createByteArray();
        this.latenciesMs = in.createIntArray();
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.netId);
        out.writeByteArray(this.eventTypes);
        out.writeByteArray(this.returnCodes);
        out.writeIntArray(this.latenciesMs);
    }

    public int describeContents() {
        return 0;
    }

    public String toString() {
        return String.format("DnsEvent(%d, %d events)", new Object[]{Integer.valueOf(this.netId), Integer.valueOf(this.eventTypes.length)});
    }

    public static void logEvent(int netId, byte[] eventTypes, byte[] returnCodes, int[] latenciesMs) {
        IpConnectivityEvent.logEvent(new DnsEvent(netId, eventTypes, returnCodes, latenciesMs));
    }
}
