package android.ddm;

import android.os.Debug;
import android.util.Log;
import java.nio.ByteBuffer;
import org.apache.harmony.dalvik.ddmc.Chunk;
import org.apache.harmony.dalvik.ddmc.ChunkHandler;
import org.apache.harmony.dalvik.ddmc.DdmServer;

public class DdmHandleProfiling extends ChunkHandler {
    public static final int CHUNK_MPRE = 0;
    public static final int CHUNK_MPRQ = 0;
    public static final int CHUNK_MPRS = 0;
    public static final int CHUNK_MPSE = 0;
    public static final int CHUNK_MPSS = 0;
    public static final int CHUNK_SPSE = 0;
    public static final int CHUNK_SPSS = 0;
    private static final boolean DEBUG = false;
    private static DdmHandleProfiling mInstance;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.ddm.DdmHandleProfiling.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.ddm.DdmHandleProfiling.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.ddm.DdmHandleProfiling.<clinit>():void");
    }

    private DdmHandleProfiling() {
    }

    public static void register() {
        DdmServer.registerHandler(CHUNK_MPRS, mInstance);
        DdmServer.registerHandler(CHUNK_MPRE, mInstance);
        DdmServer.registerHandler(CHUNK_MPSS, mInstance);
        DdmServer.registerHandler(CHUNK_MPSE, mInstance);
        DdmServer.registerHandler(CHUNK_MPRQ, mInstance);
        DdmServer.registerHandler(CHUNK_SPSS, mInstance);
        DdmServer.registerHandler(CHUNK_SPSE, mInstance);
    }

    public void connected() {
    }

    public void disconnected() {
    }

    public Chunk handleChunk(Chunk request) {
        int type = request.type;
        if (type == CHUNK_MPRS) {
            return handleMPRS(request);
        }
        if (type == CHUNK_MPRE) {
            return handleMPRE(request);
        }
        if (type == CHUNK_MPSS) {
            return handleMPSS(request);
        }
        if (type == CHUNK_MPSE) {
            return handleMPSEOrSPSE(request, "Method");
        }
        if (type == CHUNK_MPRQ) {
            return handleMPRQ(request);
        }
        if (type == CHUNK_SPSS) {
            return handleSPSS(request);
        }
        if (type == CHUNK_SPSE) {
            return handleMPSEOrSPSE(request, "Sample");
        }
        throw new RuntimeException("Unknown packet " + ChunkHandler.name(type));
    }

    private Chunk handleMPRS(Chunk request) {
        ByteBuffer in = wrapChunk(request);
        try {
            Debug.startMethodTracing(getString(in, in.getInt()), in.getInt(), in.getInt());
            return null;
        } catch (RuntimeException re) {
            return createFailChunk(1, re.getMessage());
        }
    }

    private Chunk handleMPRE(Chunk request) {
        byte result;
        try {
            Debug.stopMethodTracing();
            result = (byte) 0;
        } catch (RuntimeException re) {
            Log.w("ddm-heap", "Method profiling end failed: " + re.getMessage());
            result = (byte) 1;
        }
        byte[] reply = new byte[]{result};
        return new Chunk(CHUNK_MPRE, reply, 0, reply.length);
    }

    private Chunk handleMPSS(Chunk request) {
        ByteBuffer in = wrapChunk(request);
        try {
            Debug.startMethodTracingDdms(in.getInt(), in.getInt(), false, 0);
            return null;
        } catch (RuntimeException re) {
            return createFailChunk(1, re.getMessage());
        }
    }

    private Chunk handleMPSEOrSPSE(Chunk request, String type) {
        try {
            Debug.stopMethodTracing();
            return null;
        } catch (RuntimeException re) {
            Log.w("ddm-heap", type + " prof stream end failed: " + re.getMessage());
            return createFailChunk(1, re.getMessage());
        }
    }

    private Chunk handleMPRQ(Chunk request) {
        byte[] reply = new byte[]{(byte) Debug.getMethodTracingMode()};
        return new Chunk(CHUNK_MPRQ, reply, 0, reply.length);
    }

    private Chunk handleSPSS(Chunk request) {
        ByteBuffer in = wrapChunk(request);
        try {
            Debug.startMethodTracingDdms(in.getInt(), in.getInt(), true, in.getInt());
            return null;
        } catch (RuntimeException re) {
            return createFailChunk(1, re.getMessage());
        }
    }
}
