package android.net.metrics;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class DhcpClientEvent extends IpConnectivityEvent implements Parcelable {
    public static final Creator<DhcpClientEvent> CREATOR = null;
    public final String ifName;
    public final String msg;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.metrics.DhcpClientEvent.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.metrics.DhcpClientEvent.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.net.metrics.DhcpClientEvent.<clinit>():void");
    }

    private DhcpClientEvent(String ifName, String msg) {
        this.ifName = ifName;
        this.msg = msg;
    }

    private DhcpClientEvent(Parcel in) {
        this.ifName = in.readString();
        this.msg = in.readString();
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.ifName);
        out.writeString(this.msg);
    }

    public int describeContents() {
        return 0;
    }

    public String toString() {
        return String.format("DhcpClientEvent(%s, %s)", new Object[]{this.ifName, this.msg});
    }

    public static void logStateEvent(String ifName, String state) {
        IpConnectivityEvent.logEvent(new DhcpClientEvent(ifName, state));
    }
}
