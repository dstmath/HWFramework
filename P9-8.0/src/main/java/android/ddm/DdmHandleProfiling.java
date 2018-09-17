package android.ddm;

import android.os.Debug;
import android.util.Log;
import java.nio.ByteBuffer;
import org.apache.harmony.dalvik.ddmc.Chunk;
import org.apache.harmony.dalvik.ddmc.ChunkHandler;
import org.apache.harmony.dalvik.ddmc.DdmServer;

public class DdmHandleProfiling extends ChunkHandler {
    public static final int CHUNK_MPRE = type("MPRE");
    public static final int CHUNK_MPRQ = type("MPRQ");
    public static final int CHUNK_MPRS = type("MPRS");
    public static final int CHUNK_MPSE = type("MPSE");
    public static final int CHUNK_MPSS = type("MPSS");
    public static final int CHUNK_SPSE = type("SPSE");
    public static final int CHUNK_SPSS = type("SPSS");
    private static final boolean DEBUG = false;
    private static DdmHandleProfiling mInstance = new DdmHandleProfiling();

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
