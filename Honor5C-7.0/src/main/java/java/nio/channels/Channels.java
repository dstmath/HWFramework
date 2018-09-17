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
import sun.nio.ch.ChannelInputStream;
import sun.nio.cs.StreamDecoder;
import sun.nio.cs.StreamEncoder;

public final class Channels {

    /* renamed from: java.nio.channels.Channels.1 */
    static class AnonymousClass1 extends OutputStream {
        private byte[] b1;
        private ByteBuffer bb;
        private byte[] bs;
        final /* synthetic */ WritableByteChannel val$ch;

        AnonymousClass1(WritableByteChannel val$ch) {
            this.val$ch = val$ch;
            this.bb = null;
            this.bs = null;
            this.b1 = null;
        }

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
                            Channels.writeFully(this.val$ch, bb);
                            return;
                        }
                        return;
                    }
                }
            }
            throw new IndexOutOfBoundsException();
        }

        public void close() throws IOException {
            this.val$ch.close();
        }
    }

    private static class ReadableByteChannelImpl extends AbstractInterruptibleChannel implements ReadableByteChannel {
        private static final int TRANSFER_SIZE = 8192;
        private byte[] buf;
        InputStream in;
        private boolean open;
        private Object readLock;

        ReadableByteChannelImpl(InputStream in) {
            this.buf = new byte[0];
            this.open = true;
            this.readLock = new Object();
            this.in = in;
        }

        public int read(ByteBuffer dst) throws IOException {
            boolean z = true;
            int len = dst.remaining();
            int totalRead = 0;
            int bytesRead = 0;
            synchronized (this.readLock) {
                while (totalRead < len) {
                    int bytesToRead = Math.min(len - totalRead, (int) TRANSFER_SIZE);
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
                    return totalRead;
                }
                return -1;
            }
        }

        protected void implCloseChannel() throws IOException {
            this.in.close();
            this.open = false;
        }
    }

    private static class WritableByteChannelImpl extends AbstractInterruptibleChannel implements WritableByteChannel {
        private static final int TRANSFER_SIZE = 8192;
        private byte[] buf;
        private boolean open;
        OutputStream out;
        private Object writeLock;

        WritableByteChannelImpl(OutputStream out) {
            this.buf = new byte[0];
            this.open = true;
            this.writeLock = new Object();
            this.out = out;
        }

        public int write(ByteBuffer src) throws IOException {
            boolean z = true;
            int len = src.remaining();
            int totalWritten = 0;
            synchronized (this.writeLock) {
                while (totalWritten < len) {
                    int bytesToWrite = Math.min(len - totalWritten, (int) TRANSFER_SIZE);
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

    public static OutputStream newOutputStream(WritableByteChannel ch) {
        checkNotNull(ch, "ch");
        return new AnonymousClass1(ch);
    }

    public static ReadableByteChannel newChannel(InputStream in) {
        checkNotNull(in, "in");
        if ((in instanceof FileInputStream) && FileInputStream.class.equals(in.getClass())) {
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
