package android.ddm;

import android.util.Log;
import org.apache.harmony.dalvik.ddmc.Chunk;
import org.apache.harmony.dalvik.ddmc.ChunkHandler;
import org.apache.harmony.dalvik.ddmc.DdmServer;

public class DdmHandleNativeHeap extends ChunkHandler {
    public static final int CHUNK_NHGT = 0;
    private static DdmHandleNativeHeap mInstance;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.ddm.DdmHandleNativeHeap.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.ddm.DdmHandleNativeHeap.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.ddm.DdmHandleNativeHeap.<clinit>():void");
    }

    private native byte[] getLeakInfo();

    private DdmHandleNativeHeap() {
    }

    public static void register() {
        DdmServer.registerHandler(CHUNK_NHGT, mInstance);
    }

    public void connected() {
    }

    public void disconnected() {
    }

    public Chunk handleChunk(Chunk request) {
        Log.i("ddm-nativeheap", "Handling " + name(request.type) + " chunk");
        int type = request.type;
        if (type == CHUNK_NHGT) {
            return handleNHGT(request);
        }
        throw new RuntimeException("Unknown packet " + ChunkHandler.name(type));
    }

    private Chunk handleNHGT(Chunk request) {
        byte[] data = getLeakInfo();
        if (data == null) {
            return createFailChunk(1, "Something went wrong");
        }
        Log.i("ddm-nativeheap", "Sending " + data.length + " bytes");
        return new Chunk(ChunkHandler.type("NHGT"), data, 0, data.length);
    }
}
