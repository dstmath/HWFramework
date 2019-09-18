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

        ReadableByteChannelImpl(InputStream in2) {
            this.in = in2;
        }

        /* JADX WARNING: Code restructure failed: missing block: B:36:0x005a, code lost:
            return r1;
         */
        public int read(ByteBuffer dst) throws IOException {
            boolean z;
            int len = dst.remaining();
            int totalRead = 0;
            int bytesRead = 0;
            synchronized (this.readLock) {
                while (true) {
                    if (totalRead >= len) {
                        break;
                    }
                    try {
                        int bytesToRead = Math.min(len - totalRead, 8192);
                        if (this.buf.length < bytesToRead) {
                            this.buf = new byte[bytesToRead];
                        }
                        if (totalRead > 0 && this.in.available() <= 0) {
                            break;
                        }
                        z = true;
                        begin();
                        bytesRead = this.in.read(this.buf, 0, bytesToRead);
                        if (bytesRead <= 0) {
                            z = false;
                        }
                        end(z);
                        if (bytesRead < 0) {
                            break;
                        }
                        totalRead += bytesRead;
                        dst.put(this.buf, 0, bytesRead);
                    } catch (Throwable th) {
                        throw th;
                    }
                }
                if (bytesRead < 0 && totalRead == 0) {
                    return -1;
                }
            }
        }

        /* access modifiers changed from: protected */
        public void implCloseChannel() throws IOException {
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

        WritableByteChannelImpl(OutputStream out2) {
            this.out = out2;
        }

        public int write(ByteBuffer src) throws IOException {
            int bytesToWrite;
            boolean z;
            int len = src.remaining();
            int totalWritten = 0;
            synchronized (this.writeLock) {
                while (totalWritten < len) {
                    try {
                        bytesToWrite = Math.min(len - totalWritten, 8192);
                        if (this.buf.length < bytesToWrite) {
                            this.buf = new byte[bytesToWrite];
                        }
                        src.get(this.buf, 0, bytesToWrite);
                        z = true;
                        begin();
                        this.out.write(this.buf, 0, bytesToWrite);
                        if (bytesToWrite <= 0) {
                            z = false;
                        }
                        end(z);
                        totalWritten += bytesToWrite;
                    } catch (Throwable th) {
                        throw th;
                    }
                }
            }
            return totalWritten;
        }

        /* access modifiers changed from: protected */
        public void implCloseChannel() throws IOException {
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

    /* access modifiers changed from: private */
    public static void writeFully(WritableByteChannel ch, ByteBuffer bb) throws IOException {
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

            public synchronized void write(byte[] bs2, int off, int len) throws IOException {
                ByteBuffer bb2;
                if (off >= 0) {
                    if (off <= bs2.length && len >= 0 && off + len <= bs2.length && off + len >= 0) {
                        if (len != 0) {
                            if (this.bs == bs2) {
                                bb2 = this.bb;
                            } else {
                                bb2 = ByteBuffer.wrap(bs2);
                            }
                            bb2.limit(Math.min(off + len, bb2.capacity()));
                            bb2.position(off);
                            this.bb = bb2;
                            this.bs = bs2;
                            Channels.writeFully(WritableByteChannel.this, bb2);
                            return;
                        }
                        return;
                    }
                }
                throw new IndexOutOfBoundsException();
            }

            public void close() throws IOException {
                WritableByteChannel.this.close();
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
                return this.b1[0] & Character.DIRECTIONALITY_UNDEFINED;
            }

            /* JADX WARNING: Code restructure failed: missing block: B:26:0x0050, code lost:
                return r2;
             */
            public synchronized int read(byte[] bs2, int off, int len) throws IOException {
                boolean interrupted;
                ByteBuffer bb2;
                if (off >= 0) {
                    try {
                        if (off <= bs2.length && len >= 0 && off + len <= bs2.length && off + len >= 0) {
                            interrupted = false;
                            if (len == 0) {
                                return 0;
                            }
                            if (this.bs == bs2) {
                                bb2 = this.bb;
                            } else {
                                bb2 = ByteBuffer.wrap(bs2);
                            }
                            bb2.position(off);
                            bb2.limit(Math.min(off + len, bb2.capacity()));
                            this.bb = bb2;
                            this.bs = bs2;
                            while (true) {
                                int intValue = AsynchronousByteChannel.this.read(bb2).get().intValue();
                                break;
                            }
                            if (interrupted) {
                                Thread.currentThread().interrupt();
                            }
                        }
                    } catch (ExecutionException ee) {
                        throw new IOException(ee.getCause());
                    } catch (InterruptedException e) {
                        interrupted = true;
                    } catch (Throwable th) {
                        throw th;
                    }
                }
                throw new IndexOutOfBoundsException();
            }

            public void close() throws IOException {
                AsynchronousByteChannel.this.close();
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

            /* JADX WARNING: Code restructure failed: missing block: B:35:0x005d, code lost:
                return;
             */
            public synchronized void write(byte[] bs2, int off, int len) throws IOException {
                ByteBuffer bb2;
                if (off >= 0) {
                    try {
                        if (off <= bs2.length && len >= 0 && off + len <= bs2.length && off + len >= 0) {
                            if (len != 0) {
                                if (this.bs == bs2) {
                                    bb2 = this.bb;
                                } else {
                                    bb2 = ByteBuffer.wrap(bs2);
                                }
                                bb2.limit(Math.min(off + len, bb2.capacity()));
                                bb2.position(off);
                                this.bb = bb2;
                                this.bs = bs2;
                                boolean interrupted = false;
                                while (bb2.remaining() > 0) {
                                    try {
                                        AsynchronousByteChannel.this.write(bb2).get();
                                    } catch (ExecutionException ee) {
                                        throw new IOException(ee.getCause());
                                    } catch (InterruptedException e) {
                                        interrupted = true;
                                    } catch (Throwable th) {
                                        if (interrupted) {
                                            Thread.currentThread().interrupt();
                                        }
                                        throw th;
                                    }
                                }
                                if (interrupted) {
                                    Thread.currentThread().interrupt();
                                }
                            } else {
                                return;
                            }
                        }
                    } catch (Throwable th2) {
                        throw th2;
                    }
                }
                throw new IndexOutOfBoundsException();
            }

            public void close() throws IOException {
                AsynchronousByteChannel.this.close();
            }
        };
    }

    public static ReadableByteChannel newChannel(InputStream in) {
        checkNotNull(in, "in");
        if (!(in instanceof FileInputStream) || !FileInputStream.class.equals(in.getClass())) {
            return new ReadableByteChannelImpl(in);
        }
        return ((FileInputStream) in).getChannel();
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
