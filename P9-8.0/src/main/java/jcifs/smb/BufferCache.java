package jcifs.smb;

import jcifs.Config;

public class BufferCache {
    private static final int MAX_BUFFERS = Config.getInt("jcifs.smb.maxBuffers", 16);
    static Object[] cache = new Object[MAX_BUFFERS];
    private static int freeBuffers = 0;

    public static byte[] getBuffer() {
        synchronized (cache) {
            byte[] buf;
            if (freeBuffers > 0) {
                for (int i = 0; i < MAX_BUFFERS; i++) {
                    if (cache[i] != null) {
                        buf = (byte[]) cache[i];
                        cache[i] = null;
                        freeBuffers--;
                        return buf;
                    }
                }
            }
            buf = new byte[65535];
            return buf;
        }
    }

    static void getBuffers(SmbComTransaction req, SmbComTransactionResponse rsp) {
        synchronized (cache) {
            req.txn_buf = getBuffer();
            rsp.txn_buf = getBuffer();
        }
    }

    /* JADX WARNING: Missing block: B:20:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void releaseBuffer(byte[] buf) {
        synchronized (cache) {
            if (freeBuffers < MAX_BUFFERS) {
                for (int i = 0; i < MAX_BUFFERS; i++) {
                    if (cache[i] == null) {
                        cache[i] = buf;
                        freeBuffers++;
                        return;
                    }
                }
            }
        }
    }
}
