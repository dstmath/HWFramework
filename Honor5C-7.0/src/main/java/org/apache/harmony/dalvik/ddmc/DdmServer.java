package org.apache.harmony.dalvik.ddmc;

import java.util.HashMap;

public class DdmServer {
    public static final int CLIENT_PROTOCOL_VERSION = 1;
    private static final int CONNECTED = 1;
    private static final int DISCONNECTED = 2;
    private static HashMap<Integer, ChunkHandler> mHandlerMap;
    private static volatile boolean mRegistrationComplete;
    private static boolean mRegistrationTimedOut;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: org.apache.harmony.dalvik.ddmc.DdmServer.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: org.apache.harmony.dalvik.ddmc.DdmServer.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.dalvik.ddmc.DdmServer.<clinit>():void");
    }

    private static native void nativeSendChunk(int i, byte[] bArr, int i2, int i3);

    private DdmServer() {
    }

    public static void registerHandler(int type, ChunkHandler handler) {
        if (handler == null) {
            throw new NullPointerException("handler == null");
        }
        synchronized (mHandlerMap) {
            if (mHandlerMap.get(Integer.valueOf(type)) != null) {
                throw new RuntimeException("type " + Integer.toHexString(type) + " already registered");
            }
            mHandlerMap.put(Integer.valueOf(type), handler);
        }
    }

    public static ChunkHandler unregisterHandler(int type) {
        ChunkHandler chunkHandler;
        synchronized (mHandlerMap) {
            chunkHandler = (ChunkHandler) mHandlerMap.remove(Integer.valueOf(type));
        }
        return chunkHandler;
    }

    public static void registrationComplete() {
        synchronized (mHandlerMap) {
            mRegistrationComplete = true;
            mHandlerMap.notifyAll();
        }
    }

    public static void sendChunk(Chunk chunk) {
        nativeSendChunk(chunk.type, chunk.data, chunk.offset, chunk.length);
    }

    private static void broadcast(int event) {
        synchronized (mHandlerMap) {
            for (ChunkHandler handler : mHandlerMap.values()) {
                switch (event) {
                    case CONNECTED /*1*/:
                        handler.connected();
                        continue;
                    case DISCONNECTED /*2*/:
                        handler.disconnected();
                        continue;
                    default:
                        throw new UnsupportedOperationException();
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static Chunk dispatch(int type, byte[] data, int offset, int length) {
        ChunkHandler handler;
        synchronized (mHandlerMap) {
            while (true) {
                if (mRegistrationComplete || mRegistrationTimedOut) {
                    handler = (ChunkHandler) mHandlerMap.get(Integer.valueOf(type));
                } else {
                    try {
                        mHandlerMap.wait(1000);
                        if (!mRegistrationComplete) {
                            mRegistrationTimedOut = true;
                        }
                    } catch (InterruptedException e) {
                    }
                }
            }
            handler = (ChunkHandler) mHandlerMap.get(Integer.valueOf(type));
        }
        if (handler == null) {
            return null;
        }
        return handler.handleChunk(new Chunk(type, data, offset, length));
    }
}
