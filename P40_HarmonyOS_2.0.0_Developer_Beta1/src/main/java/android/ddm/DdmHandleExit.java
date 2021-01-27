package android.ddm;

import org.apache.harmony.dalvik.ddmc.Chunk;
import org.apache.harmony.dalvik.ddmc.ChunkHandler;
import org.apache.harmony.dalvik.ddmc.DdmServer;

public class DdmHandleExit extends ChunkHandler {
    public static final int CHUNK_EXIT = type("EXIT");
    private static DdmHandleExit mInstance = new DdmHandleExit();

    private DdmHandleExit() {
    }

    public static void register() {
        DdmServer.registerHandler(CHUNK_EXIT, mInstance);
    }

    public void connected() {
    }

    public void disconnected() {
    }

    public Chunk handleChunk(Chunk request) {
        Runtime.getRuntime().halt(wrapChunk(request).getInt());
        return null;
    }
}
