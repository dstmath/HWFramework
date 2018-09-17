package android.ddm;

import android.os.Debug;
import android.util.Log;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.apache.harmony.dalvik.ddmc.Chunk;
import org.apache.harmony.dalvik.ddmc.ChunkHandler;
import org.apache.harmony.dalvik.ddmc.DdmServer;
import org.apache.harmony.dalvik.ddmc.DdmVmInternal;

public class DdmHandleHeap extends ChunkHandler {
    public static final int CHUNK_HPDS = type("HPDS");
    public static final int CHUNK_HPDU = type("HPDU");
    public static final int CHUNK_HPGC = type("HPGC");
    public static final int CHUNK_HPIF = type("HPIF");
    public static final int CHUNK_HPSG = type("HPSG");
    public static final int CHUNK_NHSG = type("NHSG");
    public static final int CHUNK_REAE = type("REAE");
    public static final int CHUNK_REAL = type("REAL");
    public static final int CHUNK_REAQ = type("REAQ");
    private static DdmHandleHeap mInstance = new DdmHandleHeap();

    private DdmHandleHeap() {
    }

    public static void register() {
        DdmServer.registerHandler(CHUNK_HPIF, mInstance);
        DdmServer.registerHandler(CHUNK_HPSG, mInstance);
        DdmServer.registerHandler(CHUNK_HPDU, mInstance);
        DdmServer.registerHandler(CHUNK_HPDS, mInstance);
        DdmServer.registerHandler(CHUNK_NHSG, mInstance);
        DdmServer.registerHandler(CHUNK_HPGC, mInstance);
        DdmServer.registerHandler(CHUNK_REAE, mInstance);
        DdmServer.registerHandler(CHUNK_REAQ, mInstance);
        DdmServer.registerHandler(CHUNK_REAL, mInstance);
    }

    public void connected() {
    }

    public void disconnected() {
    }

    public Chunk handleChunk(Chunk request) {
        int type = request.type;
        if (type == CHUNK_HPIF) {
            return handleHPIF(request);
        }
        if (type == CHUNK_HPSG) {
            return handleHPSGNHSG(request, false);
        }
        if (type == CHUNK_HPDU) {
            return handleHPDU(request);
        }
        if (type == CHUNK_HPDS) {
            return handleHPDS(request);
        }
        if (type == CHUNK_NHSG) {
            return handleHPSGNHSG(request, true);
        }
        if (type == CHUNK_HPGC) {
            return handleHPGC(request);
        }
        if (type == CHUNK_REAE) {
            return handleREAE(request);
        }
        if (type == CHUNK_REAQ) {
            return handleREAQ(request);
        }
        if (type == CHUNK_REAL) {
            return handleREAL(request);
        }
        throw new RuntimeException("Unknown packet " + ChunkHandler.name(type));
    }

    private Chunk handleHPIF(Chunk request) {
        if (DdmVmInternal.heapInfoNotify(wrapChunk(request).get())) {
            return null;
        }
        return createFailChunk(1, "Unsupported HPIF what");
    }

    private Chunk handleHPSGNHSG(Chunk request, boolean isNative) {
        ByteBuffer in = wrapChunk(request);
        if (DdmVmInternal.heapSegmentNotify(in.get(), in.get(), isNative)) {
            return null;
        }
        return createFailChunk(1, "Unsupported HPSG what/when");
    }

    private Chunk handleHPDU(Chunk request) {
        byte result;
        ByteBuffer in = wrapChunk(request);
        try {
            Debug.dumpHprofData(getString(in, in.getInt()));
            result = (byte) 0;
        } catch (UnsupportedOperationException e) {
            Log.w("ddm-heap", "hprof dumps not supported in this VM");
            result = (byte) -1;
        } catch (IOException e2) {
            result = (byte) -1;
        } catch (RuntimeException e3) {
            result = (byte) -1;
        }
        byte[] reply = new byte[]{result};
        return new Chunk(CHUNK_HPDU, reply, 0, reply.length);
    }

    private Chunk handleHPDS(Chunk request) {
        ByteBuffer in = wrapChunk(request);
        String failMsg = null;
        try {
            Debug.dumpHprofDataDdms();
        } catch (UnsupportedOperationException e) {
            failMsg = "hprof dumps not supported in this VM";
        } catch (RuntimeException re) {
            failMsg = "Exception: " + re.getMessage();
        }
        if (failMsg == null) {
            return null;
        }
        Log.w("ddm-heap", failMsg);
        return createFailChunk(1, failMsg);
    }

    private Chunk handleHPGC(Chunk request) {
        Runtime.getRuntime().gc();
        return null;
    }

    private Chunk handleREAE(Chunk request) {
        DdmVmInternal.enableRecentAllocations(wrapChunk(request).get() != (byte) 0);
        return null;
    }

    private Chunk handleREAQ(Chunk request) {
        byte b = (byte) 1;
        byte[] reply = new byte[1];
        if (!DdmVmInternal.getRecentAllocationStatus()) {
            b = (byte) 0;
        }
        reply[0] = b;
        return new Chunk(CHUNK_REAQ, reply, 0, reply.length);
    }

    private Chunk handleREAL(Chunk request) {
        byte[] reply = DdmVmInternal.getRecentAllocations();
        return new Chunk(CHUNK_REAL, reply, 0, reply.length);
    }
}
