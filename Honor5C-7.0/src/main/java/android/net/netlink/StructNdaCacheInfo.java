package android.net.netlink;

import java.nio.ByteBuffer;

public class StructNdaCacheInfo {
    private static final long CLOCK_TICKS_PER_SECOND = 0;
    public static final int STRUCT_SIZE = 16;
    public int ndm_confirmed;
    public int ndm_refcnt;
    public int ndm_updated;
    public int ndm_used;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.netlink.StructNdaCacheInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.netlink.StructNdaCacheInfo.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.net.netlink.StructNdaCacheInfo.<clinit>():void");
    }

    private static boolean hasAvailableSpace(ByteBuffer byteBuffer) {
        return byteBuffer != null && byteBuffer.remaining() >= STRUCT_SIZE;
    }

    public static StructNdaCacheInfo parse(ByteBuffer byteBuffer) {
        if (!hasAvailableSpace(byteBuffer)) {
            return null;
        }
        StructNdaCacheInfo struct = new StructNdaCacheInfo();
        struct.ndm_used = byteBuffer.getInt();
        struct.ndm_confirmed = byteBuffer.getInt();
        struct.ndm_updated = byteBuffer.getInt();
        struct.ndm_refcnt = byteBuffer.getInt();
        return struct;
    }

    private static long ticksToMilliSeconds(int intClockTicks) {
        return (1000 * (((long) intClockTicks) & -1)) / CLOCK_TICKS_PER_SECOND;
    }

    public long lastUsed() {
        return ticksToMilliSeconds(this.ndm_used);
    }

    public long lastConfirmed() {
        return ticksToMilliSeconds(this.ndm_confirmed);
    }

    public long lastUpdated() {
        return ticksToMilliSeconds(this.ndm_updated);
    }

    public String toString() {
        return "NdaCacheInfo{ ndm_used{" + lastUsed() + "}, " + "ndm_confirmed{" + lastConfirmed() + "}, " + "ndm_updated{" + lastUpdated() + "}, " + "ndm_refcnt{" + this.ndm_refcnt + "} " + "}";
    }
}
