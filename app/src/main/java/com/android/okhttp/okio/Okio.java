package com.android.okhttp.okio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Okio {
    private static final Logger logger = null;

    /* renamed from: com.android.okhttp.okio.Okio.1 */
    static class AnonymousClass1 implements Sink {
        final /* synthetic */ OutputStream val$out;
        final /* synthetic */ Timeout val$timeout;

        AnonymousClass1(Timeout val$timeout, OutputStream val$out) {
            this.val$timeout = val$timeout;
            this.val$out = val$out;
        }

        public void write(Buffer source, long byteCount) throws IOException {
            Util.checkOffsetAndCount(source.size, 0, byteCount);
            while (byteCount > 0) {
                this.val$timeout.throwIfReached();
                Segment head = source.head;
                int toCopy = (int) Math.min(byteCount, (long) (head.limit - head.pos));
                this.val$out.write(head.data, head.pos, toCopy);
                head.pos += toCopy;
                byteCount -= (long) toCopy;
                source.size -= (long) toCopy;
                if (head.pos == head.limit) {
                    source.head = head.pop();
                    SegmentPool.recycle(head);
                }
            }
        }

        public void flush() throws IOException {
            this.val$out.flush();
        }

        public void close() throws IOException {
            this.val$out.close();
        }

        public Timeout timeout() {
            return this.val$timeout;
        }

        public String toString() {
            return "sink(" + this.val$out + ")";
        }
    }

    /* renamed from: com.android.okhttp.okio.Okio.2 */
    static class AnonymousClass2 implements Source {
        final /* synthetic */ InputStream val$in;
        final /* synthetic */ Timeout val$timeout;

        AnonymousClass2(Timeout val$timeout, InputStream val$in) {
            this.val$timeout = val$timeout;
            this.val$in = val$in;
        }

        public long read(Buffer sink, long byteCount) throws IOException {
            if (byteCount < 0) {
                throw new IllegalArgumentException("byteCount < 0: " + byteCount);
            } else if (byteCount == 0) {
                return 0;
            } else {
                try {
                    this.val$timeout.throwIfReached();
                    Segment tail = sink.writableSegment(1);
                    int bytesRead = this.val$in.read(tail.data, tail.limit, (int) Math.min(byteCount, (long) (8192 - tail.limit)));
                    if (bytesRead == -1) {
                        return -1;
                    }
                    tail.limit += bytesRead;
                    sink.size += (long) bytesRead;
                    return (long) bytesRead;
                } catch (AssertionError e) {
                    if (Okio.isAndroidGetsocknameError(e)) {
                        throw new IOException(e);
                    }
                    throw e;
                }
            }
        }

        public void close() throws IOException {
            this.val$in.close();
        }

        public Timeout timeout() {
            return this.val$timeout;
        }

        public String toString() {
            return "source(" + this.val$in + ")";
        }
    }

    /* renamed from: com.android.okhttp.okio.Okio.3 */
    static class AnonymousClass3 extends AsyncTimeout {
        final /* synthetic */ Socket val$socket;

        AnonymousClass3(Socket val$socket) {
            this.val$socket = val$socket;
        }

        protected IOException newTimeoutException(IOException cause) {
            InterruptedIOException ioe = new SocketTimeoutException("timeout");
            if (cause != null) {
                ioe.initCause(cause);
            }
            return ioe;
        }

        protected void timedOut() {
            try {
                this.val$socket.close();
            } catch (Exception e) {
                Okio.logger.log(Level.WARNING, "Failed to close timed out socket " + this.val$socket, e);
            } catch (AssertionError e2) {
                if (Okio.isAndroidGetsocknameError(e2)) {
                    Okio.logger.log(Level.WARNING, "Failed to close timed out socket " + this.val$socket, e2);
                    return;
                }
                throw e2;
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.okhttp.okio.Okio.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.okhttp.okio.Okio.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.okhttp.okio.Okio.<clinit>():void");
    }

    private Okio() {
    }

    public static BufferedSource buffer(Source source) {
        if (source != null) {
            return new RealBufferedSource(source);
        }
        throw new IllegalArgumentException("source == null");
    }

    public static BufferedSink buffer(Sink sink) {
        if (sink != null) {
            return new RealBufferedSink(sink);
        }
        throw new IllegalArgumentException("sink == null");
    }

    public static Sink sink(OutputStream out) {
        return sink(out, new Timeout());
    }

    private static Sink sink(OutputStream out, Timeout timeout) {
        if (out == null) {
            throw new IllegalArgumentException("out == null");
        } else if (timeout != null) {
            return new AnonymousClass1(timeout, out);
        } else {
            throw new IllegalArgumentException("timeout == null");
        }
    }

    public static Sink sink(Socket socket) throws IOException {
        if (socket == null) {
            throw new IllegalArgumentException("socket == null");
        }
        AsyncTimeout timeout = timeout(socket);
        return timeout.sink(sink(socket.getOutputStream(), timeout));
    }

    public static Source source(InputStream in) {
        return source(in, new Timeout());
    }

    private static Source source(InputStream in, Timeout timeout) {
        if (in == null) {
            throw new IllegalArgumentException("in == null");
        } else if (timeout != null) {
            return new AnonymousClass2(timeout, in);
        } else {
            throw new IllegalArgumentException("timeout == null");
        }
    }

    public static Source source(File file) throws FileNotFoundException {
        if (file != null) {
            return source(new FileInputStream(file));
        }
        throw new IllegalArgumentException("file == null");
    }

    public static Sink sink(File file) throws FileNotFoundException {
        if (file != null) {
            return sink(new FileOutputStream(file));
        }
        throw new IllegalArgumentException("file == null");
    }

    public static Sink appendingSink(File file) throws FileNotFoundException {
        if (file != null) {
            return sink(new FileOutputStream(file, true));
        }
        throw new IllegalArgumentException("file == null");
    }

    public static Source source(Socket socket) throws IOException {
        if (socket == null) {
            throw new IllegalArgumentException("socket == null");
        }
        AsyncTimeout timeout = timeout(socket);
        return timeout.source(source(socket.getInputStream(), timeout));
    }

    private static AsyncTimeout timeout(Socket socket) {
        return new AnonymousClass3(socket);
    }

    private static boolean isAndroidGetsocknameError(AssertionError e) {
        if (e.getCause() == null || e.getMessage() == null) {
            return false;
        }
        return e.getMessage().contains("getsockname failed");
    }
}
