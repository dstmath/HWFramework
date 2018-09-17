package java.nio.channels;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.channels.spi.AbstractInterruptibleChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.concurrent.ExecutionException;
import sun.nio.ch.ChannelInputStream;
import sun.nio.cs.StreamDecoder;
import sun.nio.cs.StreamEncoder;

public final class Channels {

    private static class ReadableByteChannelImpl extends AbstractInterruptibleChannel implements ReadableByteChannel {
        private static final int TRANSFER_SIZE = 8192;
        private byte[] buf = new byte[0];
        InputStream in;
        private boolean open = true;
        private Object readLock = new Object();

        ReadableByteChannelImpl(InputStream in) {
            this.in = in;
        }

        /* JADX WARNING: Missing block: B:36:0x005a, code:
            return r3;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public int read(ByteBuffer dst) throws IOException {
            boolean z = true;
            int len = dst.remaining();
            int totalRead = 0;
            int bytesRead = 0;
            synchronized (this.readLock) {
                while (totalRead < len) {
                    int bytesToRead = Math.min(len - totalRead, 8192);
                    if (this.buf.length < bytesToRead) {
                        this.buf = new byte[bytesToRead];
                    }
                    if (totalRead > 0 && this.in.available() <= 0) {
                        break;
                    }
                    try {
                        boolean z2;
                        begin();
                        bytesRead = this.in.read(this.buf, 0, bytesToRead);
                        if (bytesRead > 0) {
                            z2 = true;
                        } else {
                            z2 = false;
                        }
                        end(z2);
                        if (bytesRead < 0) {
                            break;
                        }
                        totalRead += bytesRead;
                        dst.put(this.buf, 0, bytesRead);
                    } catch (Throwable th) {
                        if (bytesRead <= 0) {
                            z = false;
                        }
                        end(z);
                    }
                }
                if (bytesRead >= 0 || totalRead != 0) {
                } else {
                    return -1;
                }
            }
        }

        protected void implCloseChannel() throws IOException {
            this.in.close();
            this.open = false;
        }
    }

    private static class WritableByteChannelImpl extends AbstractInterruptibleChannel implements WritableByteChannel {
        private static final int TRANSFER_SIZE = 8192;
        private byte[] buf = new byte[0];
        private boolean open = true;
        OutputStream out;
        private Object writeLock = new Object();

        WritableByteChannelImpl(OutputStream out) {
            this.out = out;
        }

        public int write(ByteBuffer src) throws IOException {
            boolean z = true;
            int len = src.remaining();
            int totalWritten = 0;
            synchronized (this.writeLock) {
                while (totalWritten < len) {
                    int bytesToWrite = Math.min(len - totalWritten, 8192);
                    if (this.buf.length < bytesToWrite) {
                        this.buf = new byte[bytesToWrite];
                    }
                    src.get(this.buf, 0, bytesToWrite);
                    try {
                        boolean z2;
                        begin();
                        this.out.write(this.buf, 0, bytesToWrite);
                        if (bytesToWrite > 0) {
                            z2 = true;
                        } else {
                            z2 = false;
                        }
                        end(z2);
                        totalWritten += bytesToWrite;
                    } catch (Throwable th) {
                        if (bytesToWrite <= 0) {
                            z = false;
                        }
                        end(z);
                    }
                }
            }
            return totalWritten;
        }

        protected void implCloseChannel() throws IOException {
            this.out.close();
            this.open = false;
        }
    }

    private Channels() {
    }

    private static void checkNotNull(Object o, String name) {
        if (o == null) {
            throw new NullPointerException("\"" + name + "\" is null!");
        }
    }

    private static void writeFullyImpl(WritableByteChannel ch, ByteBuffer bb) throws IOException {
        while (bb.remaining() > 0) {
            if (ch.write(bb) <= 0) {
                throw new RuntimeException("no bytes written");
            }
        }
    }

    private static void writeFully(WritableByteChannel ch, ByteBuffer bb) throws IOException {
        if (ch instanceof SelectableChannel) {
            SelectableChannel sc = (SelectableChannel) ch;
            synchronized (sc.blockingLock()) {
                if (sc.isBlocking()) {
                    writeFullyImpl(ch, bb);
                } else {
                    throw new IllegalBlockingModeException();
                }
            }
            return;
        }
        writeFullyImpl(ch, bb);
    }

    public static InputStream newInputStream(ReadableByteChannel ch) {
        checkNotNull(ch, "ch");
        return new ChannelInputStream(ch);
    }

    public static OutputStream newOutputStream(final WritableByteChannel ch) {
        checkNotNull(ch, "ch");
        return new OutputStream() {
            private byte[] b1 = null;
            private ByteBuffer bb = null;
            private byte[] bs = null;

            public synchronized void write(int b) throws IOException {
                if (this.b1 == null) {
                    this.b1 = new byte[1];
                }
                this.b1[0] = (byte) b;
                write(this.b1);
            }

            public synchronized void write(byte[] bs, int off, int len) throws IOException {
                if (off >= 0) {
                    if (off <= bs.length && len >= 0) {
                        if (off + len <= bs.length && off + len >= 0) {
                            if (len != 0) {
                                ByteBuffer bb;
                                if (this.bs == bs) {
                                    bb = this.bb;
                                } else {
                                    bb = ByteBuffer.wrap(bs);
                                }
                                bb.limit(Math.min(off + len, bb.capacity()));
                                bb.position(off);
                                this.bb = bb;
                                this.bs = bs;
                                Channels.writeFully(ch, bb);
                                return;
                            }
                            return;
                        }
                    }
                }
                throw new IndexOutOfBoundsException();
            }

            public void close() throws IOException {
                ch.close();
            }
        };
    }

    public static InputStream newInputStream(final AsynchronousByteChannel ch) {
        checkNotNull(ch, "ch");
        return new InputStream() {
            private byte[] b1 = null;
            private ByteBuffer bb = null;
            private byte[] bs = null;

            public synchronized int read() throws IOException {
                if (this.b1 == null) {
                    this.b1 = new byte[1];
                }
                if (read(this.b1) != 1) {
                    return -1;
                }
                return this.b1[0] & 255;
            }

            /* JADX WARNING: Missing block: B:29:0x004a, code:
            if (r3 == false) goto L_0x0053;
     */
            /* JADX WARNING: Missing block: B:31:?, code:
            java.lang.Thread.currentThread().interrupt();
     */
            /* JADX WARNING: Missing block: B:33:0x0054, code:
            return r4;
     */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public synchronized int read(byte[] bs, int off, int len) throws IOException {
                if (off >= 0) {
                    if (off <= bs.length && len >= 0) {
                        if (off + len <= bs.length && off + len >= 0) {
                            if (len == 0) {
                                return 0;
                            }
                            ByteBuffer bb;
                            if (this.bs == bs) {
                                bb = this.bb;
                            } else {
                                bb = ByteBuffer.wrap(bs);
                            }
                            bb.position(off);
                            bb.limit(Math.min(off + len, bb.capacity()));
                            this.bb = bb;
                            this.bs = bs;
                            boolean interrupted = false;
                            while (true) {
                                try {
                                    int intValue = ((Integer) ch.read(bb).get()).intValue();
                                    break;
                                } catch (ExecutionException ee) {
                                    throw new IOException(ee.getCause());
                                } catch (InterruptedException e) {
                                    interrupted = true;
                                } catch (Throwable th) {
                                    if (interrupted) {
                                        Thread.currentThread().interrupt();
                                    }
                                }
                            }
                        }
                    }
                }
                throw new IndexOutOfBoundsException();
            }

            public void close() throws IOException {
                ch.close();
            }
        };
    }

    public static OutputStream newOutputStream(final AsynchronousByteChannel ch) {
        checkNotNull(ch, "ch");
        return new OutputStream() {
            private byte[] b1 = null;
            private ByteBuffer bb = null;
            private byte[] bs = null;

            public synchronized void write(int b) throws IOException {
                if (this.b1 == null) {
                    this.b1 = new byte[1];
                }
                this.b1[0] = (byte) b;
                write(this.b1);
            }

            /* JADX WARNING: Missing block: B:47:0x0071, code:
            return;
     */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public synchronized void write(byte[] bs, int off, int len) throws IOException {
                if (off >= 0) {
                    if (off <= bs.length && len >= 0) {
                        if (off + len <= bs.length && off + len >= 0) {
                            if (len != 0) {
                                ByteBuffer bb;
                                if (this.bs == bs) {
                                    bb = this.bb;
                                } else {
                                    bb = ByteBuffer.wrap(bs);
                                }
                                bb.limit(Math.min(off + len, bb.capacity()));
                                bb.position(off);
                                this.bb = bb;
                                this.bs = bs;
                                boolean interrupted = false;
                                while (bb.remaining() > 0) {
                                    try {
                                        ch.write(bb).get();
                                    } catch (ExecutionException ee) {
                                        throw new IOException(ee.getCause());
                                    } catch (InterruptedException e) {
                                        interrupted = true;
                                    } catch (Throwable th) {
                                        if (interrupted) {
                                            Thread.currentThread().interrupt();
                                        }
                                    }
                                }
                                if (interrupted) {
                                    Thread.currentThread().interrupt();
                                }
                            } else {
                                return;
                            }
                        }
                    }
                }
                throw new IndexOutOfBoundsException();
            }

            public void close() throws IOException {
                ch.close();
            }
        };
    }

    public static ReadableByteChannel newChannel(InputStream in) {
        checkNotNull(in, "in");
        if ((in instanceof FileInputStream) && FileInputStream.class.lambda$-java_util_function_Predicate_4628(in.getClass())) {
            return ((FileInputStream) in).getChannel();
        }
        return new ReadableByteChannelImpl(in);
    }

    public static WritableByteChannel newChannel(OutputStream out) {
        checkNotNull(out, "out");
        return new WritableByteChannelImpl(out);
    }

    public static Reader newReader(ReadableByteChannel ch, CharsetDecoder dec, int minBufferCap) {
        checkNotNull(ch, "ch");
        return StreamDecoder.forDecoder(ch, dec.reset(), minBufferCap);
    }

    public static Reader newReader(ReadableByteChannel ch, String csName) {
        checkNotNull(csName, "csName");
        return newReader(ch, Charset.forName(csName).newDecoder(), -1);
    }

    public static Writer newWriter(WritableByteChannel ch, CharsetEncoder enc, int minBufferCap) {
        checkNotNull(ch, "ch");
        return StreamEncoder.forEncoder(ch, enc.reset(), minBufferCap);
    }

    public static Writer newWriter(WritableByteChannel ch, String csName) {
        checkNotNull(csName, "csName");
        return newWriter(ch, Charset.forName(csName).newEncoder(), -1);
    }
}
