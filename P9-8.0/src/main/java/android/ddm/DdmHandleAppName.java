package android.ddm;

import android.net.ProxyInfo;
import java.nio.ByteBuffer;
import org.apache.harmony.dalvik.ddmc.Chunk;
import org.apache.harmony.dalvik.ddmc.ChunkHandler;
import org.apache.harmony.dalvik.ddmc.DdmServer;

public class DdmHandleAppName extends ChunkHandler {
    public static final int CHUNK_APNM = type("APNM");
    private static volatile String mAppName = ProxyInfo.LOCAL_EXCL_LIST;
    private static DdmHandleAppName mInstance = new DdmHandleAppName();

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
