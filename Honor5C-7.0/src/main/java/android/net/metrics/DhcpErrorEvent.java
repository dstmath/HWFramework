package android.net.metrics;

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.Process;
import android.util.SparseArray;

public final class DhcpErrorEvent extends IpConnectivityEvent implements Parcelable {
    public static final int BOOTP_TOO_SHORT = 0;
    public static final int BUFFER_UNDERFLOW = 0;
    public static final Creator<DhcpErrorEvent> CREATOR = null;
    public static final int DHCP_BAD_MAGIC_COOKIE = 0;
    public static final int DHCP_ERROR = 4;
    public static final int DHCP_INVALID_OPTION_LENGTH = 0;
    public static final int DHCP_NO_MSG_TYPE = 0;
    public static final int DHCP_UNKNOWN_MSG_TYPE = 0;
    public static final int L2_ERROR = 1;
    public static final int L2_TOO_SHORT = 0;
    public static final int L2_WRONG_ETH_TYPE = 0;
    public static final int L3_ERROR = 2;
    public static final int L3_INVALID_IP = 0;
    public static final int L3_NOT_IPV4 = 0;
    public static final int L3_TOO_SHORT = 0;
    public static final int L4_ERROR = 3;
    public static final int L4_NOT_UDP = 0;
    public static final int L4_WRONG_PORT = 0;
    public static final int MISC_ERROR = 5;
    public static final int RECEIVE_ERROR = 0;
    public final int errorCode;
    public final String ifName;

    static final class Decoder {
        static final SparseArray<String> constants = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.metrics.DhcpErrorEvent.Decoder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.metrics.DhcpErrorEvent.Decoder.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.net.metrics.DhcpErrorEvent.Decoder.<clinit>():void");
        }

        Decoder() {
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.metrics.DhcpErrorEvent.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.metrics.DhcpErrorEvent.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.net.metrics.DhcpErrorEvent.<clinit>():void");
    }

    /* synthetic */ DhcpErrorEvent(Parcel in, DhcpErrorEvent dhcpErrorEvent) {
        this(in);
    }

    private DhcpErrorEvent(String ifName, int errorCode) {
        this.ifName = ifName;
        this.errorCode = errorCode;
    }

    private DhcpErrorEvent(Parcel in) {
        this.ifName = in.readString();
        this.errorCode = in.readInt();
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.ifName);
        out.writeInt(this.errorCode);
    }

    public int describeContents() {
        return L4_WRONG_PORT;
    }

    public static void logParseError(String ifName, int errorCode) {
        IpConnectivityEvent.logEvent(new DhcpErrorEvent(ifName, errorCode));
    }

    public static void logReceiveError(String ifName) {
        IpConnectivityEvent.logEvent(new DhcpErrorEvent(ifName, RECEIVE_ERROR));
    }

    public static int errorCodeWithOption(int errorCode, int option) {
        return (Color.RED & errorCode) | (option & Process.PROC_TERM_MASK);
    }

    private static int makeErrorCode(int type, int subtype) {
        return (type << 24) | ((subtype & Process.PROC_TERM_MASK) << 16);
    }

    public String toString() {
        Object[] objArr = new Object[L3_ERROR];
        objArr[L4_WRONG_PORT] = this.ifName;
        objArr[L2_ERROR] = Decoder.constants.get(this.errorCode);
        return String.format("DhcpErrorEvent(%s, %s)", objArr);
    }
}
