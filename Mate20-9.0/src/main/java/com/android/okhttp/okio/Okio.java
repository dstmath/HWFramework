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
    /* access modifiers changed from: private */
    public static final Logger logger = Logger.getLogger(Okio.class.getName());

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

    private static Sink sink(final OutputStream out, final Timeout timeout) {
        if (out == null) {
            throw new IllegalArgumentException("out == null");
        } else if (timeout != null) {
            return new Sink() {
                public void write(Buffer source, long byteCount) throws IOException {
                    Util.checkOffsetAndCount(source.size, 0, byteCount);
                    while (byteCount > 0) {
                        Timeout.this.throwIfReached();
                        Segment head = source.head;
                        int toCopy = (int) Math.min(byteCount, (long) (head.limit - head.pos));
                        out.write(head.data, head.pos, toCopy);
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
                    out.flush();
                }

                public void close() throws IOException {
                    out.close();
                }

                public Timeout timeout() {
                    return Timeout.this;
                }

                public String toString() {
                    return "sink(" + out + ")";
                }
            };
        } else {
            throw new IllegalArgumentException("timeout == null");
        }
    }

    public static Sink sink(Socket socket) throws IOException {
        if (socket != null) {
            AsyncTimeout timeout = timeout(socket);
            return timeout.sink(sink(socket.getOutputStream(), timeout));
        }
        throw new IllegalArgumentException("socket == null");
    }

    public static Source source(InputStream in) {
        return source(in, new Timeout());
    }

    private static Source source(final InputStream in, final Timeout timeout) {
        if (in == null) {
            throw new IllegalArgumentException("in == null");
        } else if (timeout != null) {
            return new Source() {
                public long read(Buffer sink, long byteCount) throws IOException {
                    if (byteCount < 0) {
                        throw new IllegalArgumentException("byteCount < 0: " + byteCount);
                    } else if (byteCount == 0) {
                        return 0;
                    } else {
                        try {
                            Timeout.this.throwIfReached();
                            Segment tail = sink.writableSegment(1);
                            int bytesRead = in.read(tail.data, tail.limit, (int) Math.min(byteCount, (long) (8192 - tail.limit)));
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
                    in.close();
                }

                public Timeout timeout() {
                    return Timeout.this;
                }

                public String toString() {
                    return "source(" + in + ")";
                }
            };
        } else {
            throw new IllegalArgumentException("timeout == null");
        }
    }

    public static Source source(File file) throws FileNotFoundException {
        if (file != null) {
            return source((InputStream) new FileInputStream(file));
        }
        throw new IllegalArgumentException("file == null");
    }

    public static Sink sink(File file) throws FileNotFoundException {
        if (file != null) {
            return sink((OutputStream) new FileOutputStream(file));
        }
        throw new IllegalArgumentException("file == null");
    }

    public static Sink appendingSink(File file) throws FileNotFoundException {
        if (file != null) {
            return sink((OutputStream) new FileOutputStream(file, true));
        }
        throw new IllegalArgumentException("file == null");
    }

    public static Source source(Socket socket) throws IOException {
        if (socket != null) {
            AsyncTimeout timeout = timeout(socket);
            return timeout.source(source(socket.getInputStream(), timeout));
        }
        throw new IllegalArgumentException("socket == null");
    }

    private static AsyncTimeout timeout(final Socket socket) {
        return new AsyncTimeout() {
            /* access modifiers changed from: protected */
            public IOException newTimeoutException(IOException cause) {
                InterruptedIOException ioe = new SocketTimeoutException("timeout");
                if (cause != null) {
                    ioe.initCause(cause);
                }
                return ioe;
            }

            /* access modifiers changed from: protected */
            public void timedOut() {
                try {
                    socket.close();
                } catch (Exception e) {
                    Logger access$100 = Okio.logger;
                    Level level = Level.WARNING;
                    access$100.log(level, "Failed to close timed out socket " + socket, e);
                } catch (AssertionError e2) {
                    if (Okio.isAndroidGetsocknameError(e2)) {
                        Logger access$1002 = Okio.logger;
                        Level level2 = Level.WARNING;
                        access$1002.log(level2, "Failed to close timed out socket " + socket, e2);
                        return;
                    }
                    throw e2;
                }
            }
        };
    }

    /* access modifiers changed from: private */
    public static boolean isAndroidGetsocknameError(AssertionError e) {
        return (e.getCause() == null || e.getMessage() == null || !e.getMessage().contains("getsockname failed")) ? false : true;
    }
}
