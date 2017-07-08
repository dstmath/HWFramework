package android.net.netlink;

import android.system.OsConstants;
import com.android.server.wm.WindowManagerService.H;
import com.android.server.wm.WindowState;
import java.nio.ByteBuffer;

public class StructNdMsg {
    public static byte NTF_MASTER = (byte) 0;
    public static byte NTF_PROXY = (byte) 0;
    public static byte NTF_ROUTER = (byte) 0;
    public static byte NTF_SELF = (byte) 0;
    public static byte NTF_USE = (byte) 0;
    public static final short NUD_DELAY = (short) 8;
    public static final short NUD_FAILED = (short) 32;
    public static final short NUD_INCOMPLETE = (short) 1;
    public static final short NUD_NOARP = (short) 64;
    public static final short NUD_NONE = (short) 0;
    public static final short NUD_PERMANENT = (short) 128;
    public static final short NUD_PROBE = (short) 16;
    public static final short NUD_REACHABLE = (short) 2;
    public static final short NUD_STALE = (short) 4;
    public static final int STRUCT_SIZE = 12;
    public byte ndm_family;
    public byte ndm_flags;
    public int ndm_ifindex;
    public short ndm_state;
    public byte ndm_type;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.netlink.StructNdMsg.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.netlink.StructNdMsg.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.net.netlink.StructNdMsg.<clinit>():void");
    }

    public static String stringForNudState(short nudState) {
        switch (nudState) {
            case WindowState.LOW_RESOLUTION_FEATURE_OFF /*0*/:
                return "NUD_NONE";
            case WindowState.LOW_RESOLUTION_COMPOSITION_OFF /*1*/:
                return "NUD_INCOMPLETE";
            case WindowState.LOW_RESOLUTION_COMPOSITION_ON /*2*/:
                return "NUD_REACHABLE";
            case H.DO_TRAVERSAL /*4*/:
                return "NUD_STALE";
            case H.REPORT_APPLICATION_TOKEN_WINDOWS /*8*/:
                return "NUD_DELAY";
            case H.ENABLE_SCREEN /*16*/:
                return "NUD_PROBE";
            case H.NOTIFY_ACTIVITY_DRAWN /*32*/:
                return "NUD_FAILED";
            case DumpState.DUMP_PERMISSIONS /*64*/:
                return "NUD_NOARP";
            case DumpState.DUMP_PACKAGES /*128*/:
                return "NUD_PERMANENT";
            default:
                return "unknown NUD state: " + String.valueOf(nudState);
        }
    }

    public static boolean isNudStateConnected(short nudState) {
        return (nudState & HdmiCecKeycode.UI_SOUND_PRESENTATION_TREBLE_NEUTRAL) != 0;
    }

    public static String stringForNudFlags(byte flags) {
        StringBuilder sb = new StringBuilder();
        if ((NTF_USE & flags) != 0) {
            sb.append("NTF_USE");
        }
        if ((NTF_SELF & flags) != 0) {
            if (sb.length() > 0) {
                sb.append("|");
            }
            sb.append("NTF_SELF");
        }
        if ((NTF_MASTER & flags) != 0) {
            if (sb.length() > 0) {
                sb.append("|");
            }
            sb.append("NTF_MASTER");
        }
        if ((NTF_PROXY & flags) != 0) {
            if (sb.length() > 0) {
                sb.append("|");
            }
            sb.append("NTF_PROXY");
        }
        if ((NTF_ROUTER & flags) != 0) {
            if (sb.length() > 0) {
                sb.append("|");
            }
            sb.append("NTF_ROUTER");
        }
        return sb.toString();
    }

    private static boolean hasAvailableSpace(ByteBuffer byteBuffer) {
        return byteBuffer != null && byteBuffer.remaining() >= STRUCT_SIZE;
    }

    public static StructNdMsg parse(ByteBuffer byteBuffer) {
        if (!hasAvailableSpace(byteBuffer)) {
            return null;
        }
        StructNdMsg struct = new StructNdMsg();
        struct.ndm_family = byteBuffer.get();
        byte pad1 = byteBuffer.get();
        short pad2 = byteBuffer.getShort();
        struct.ndm_ifindex = byteBuffer.getInt();
        struct.ndm_state = byteBuffer.getShort();
        struct.ndm_flags = byteBuffer.get();
        struct.ndm_type = byteBuffer.get();
        return struct;
    }

    public StructNdMsg() {
        this.ndm_family = (byte) OsConstants.AF_UNSPEC;
    }

    public void pack(ByteBuffer byteBuffer) {
        byteBuffer.put(this.ndm_family);
        byteBuffer.put((byte) 0);
        byteBuffer.putShort(NUD_NONE);
        byteBuffer.putInt(this.ndm_ifindex);
        byteBuffer.putShort(this.ndm_state);
        byteBuffer.put(this.ndm_flags);
        byteBuffer.put(this.ndm_type);
    }

    public boolean nudConnected() {
        return isNudStateConnected(this.ndm_state);
    }

    public boolean nudValid() {
        return nudConnected() || (this.ndm_state & 28) != 0;
    }

    public String toString() {
        String stateStr = "" + this.ndm_state + " (" + stringForNudState(this.ndm_state) + ")";
        return "StructNdMsg{ family{" + NetlinkConstants.stringForAddressFamily(this.ndm_family) + "}, " + "ifindex{" + this.ndm_ifindex + "}, " + "state{" + stateStr + "}, " + "flags{" + ("" + this.ndm_flags + " (" + stringForNudFlags(this.ndm_flags) + ")") + "}, " + "type{" + this.ndm_type + "} " + "}";
    }
}
