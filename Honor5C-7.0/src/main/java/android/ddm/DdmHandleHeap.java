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
    public static final int CHUNK_HPDS = 0;
    public static final int CHUNK_HPDU = 0;
    public static final int CHUNK_HPGC = 0;
    public static final int CHUNK_HPIF = 0;
    public static final int CHUNK_HPSG = 0;
    public static final int CHUNK_NHSG = 0;
    public static final int CHUNK_REAE = 0;
    public static final int CHUNK_REAL = 0;
    public static final int CHUNK_REAQ = 0;
    private static DdmHandleHeap mInstance;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.ddm.DdmHandleHeap.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.ddm.DdmHandleHeap.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.ddm.DdmHandleHeap.<clinit>():void");
    }

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
        boolean enable = false;
        if (wrapChunk(request).get() != null) {
            enable = true;
        }
        DdmVmInternal.enableRecentAllocations(enable);
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
