package android.ddm;

import java.nio.ByteBuffer;
import org.apache.harmony.dalvik.ddmc.Chunk;
import org.apache.harmony.dalvik.ddmc.ChunkHandler;
import org.apache.harmony.dalvik.ddmc.DdmServer;

public class DdmHandleAppName extends ChunkHandler {
    public static final int CHUNK_APNM = 0;
    private static volatile String mAppName;
    private static DdmHandleAppName mInstance;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.ddm.DdmHandleAppName.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.ddm.DdmHandleAppName.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.ddm.DdmHandleAppName.<clinit>():void");
    }

    private DdmHandleAppName() {
    }

    public static void register() {
    }

    public void connected() {
    }

    public void disconnected() {
    }

    public Chunk handleChunk(Chunk request) {
        return null;
    }

    public static void setAppName(String name, int userId) {
        if (name != null && name.length() != 0) {
            mAppName = name;
            sendAPNM(name, userId);
        }
    }

    public static String getAppName() {
        return mAppName;
    }

    private static void sendAPNM(String appName, int userId) {
        ByteBuffer out = ByteBuffer.allocate(((appName.length() * 2) + 4) + 4);
        out.order(ChunkHandler.CHUNK_ORDER);
        out.putInt(appName.length());
        putString(out, appName);
        out.putInt(userId);
        DdmServer.sendChunk(new Chunk(CHUNK_APNM, out));
    }
}
