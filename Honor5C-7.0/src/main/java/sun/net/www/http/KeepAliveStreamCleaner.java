package sun.net.www.http;

import java.io.IOException;
import java.util.LinkedList;

class KeepAliveStreamCleaner extends LinkedList<KeepAliveCleanerEntry> implements Runnable {
    protected static int MAX_CAPACITY = 0;
    protected static int MAX_DATA_REMAINING = 0;
    private static final int MAX_RETRIES = 5;
    protected static final int TIMEOUT = 5000;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.net.www.http.KeepAliveStreamCleaner.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.net.www.http.KeepAliveStreamCleaner.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.net.www.http.KeepAliveStreamCleaner.<clinit>():void");
    }

    KeepAliveStreamCleaner() {
    }

    public boolean offer(KeepAliveCleanerEntry e) {
        if (size() >= MAX_CAPACITY) {
            return false;
        }
        return super.offer(e);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void run() {
        KeepAliveStream kas;
        KeepAliveCleanerEntry keepAliveCleanerEntry = null;
        while (true) {
            try {
                synchronized (this) {
                    long before = System.currentTimeMillis();
                    long timeout = 5000;
                    while (true) {
                        keepAliveCleanerEntry = (KeepAliveCleanerEntry) poll();
                        if (keepAliveCleanerEntry == null) {
                            wait(timeout);
                            long after = System.currentTimeMillis();
                            long elapsed = after - before;
                            if (elapsed > timeout) {
                                break;
                            }
                            before = after;
                            timeout -= elapsed;
                        }
                        break;
                    }
                    keepAliveCleanerEntry = (KeepAliveCleanerEntry) poll();
                    break;
                }
                if (keepAliveCleanerEntry != null) {
                    kas = keepAliveCleanerEntry.getKeepAliveStream();
                    if (kas != null) {
                        synchronized (kas) {
                            HttpClient hc = keepAliveCleanerEntry.getHttpClient();
                            if (hc != null) {
                                try {
                                    if (!hc.isInKeepAliveCache()) {
                                        int oldTimeout = hc.getReadTimeout();
                                        hc.setReadTimeout(TIMEOUT);
                                        long remainingToRead = kas.remainingToRead();
                                        if (remainingToRead > 0) {
                                            long n = 0;
                                            int retries = 0;
                                            while (n < remainingToRead && retries < MAX_RETRIES) {
                                                remainingToRead -= n;
                                                n = kas.skip(remainingToRead);
                                                if (n == 0) {
                                                    retries++;
                                                }
                                            }
                                            remainingToRead -= n;
                                        }
                                        if (remainingToRead == 0) {
                                            hc.setReadTimeout(oldTimeout);
                                            hc.finished();
                                        } else {
                                            hc.closeServer();
                                        }
                                    }
                                } catch (IOException e) {
                                    hc.closeServer();
                                    kas.setClosed();
                                } catch (Throwable th) {
                                }
                            }
                            kas.setClosed();
                        }
                    }
                    if (keepAliveCleanerEntry == null) {
                        return;
                    }
                } else {
                    return;
                }
            } catch (InterruptedException e2) {
            }
        }
        kas.setClosed();
    }
}
