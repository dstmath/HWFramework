package sun.net.www.http;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

public class ChunkedOutputStream extends PrintStream {
    private static final byte[] CRLF = null;
    private static final int CRLF_SIZE = 0;
    static final int DEFAULT_CHUNK_SIZE = 4096;
    private static final byte[] EMPTY_CHUNK_HEADER = null;
    private static final int EMPTY_CHUNK_HEADER_SIZE = 0;
    private static final byte[] FOOTER = null;
    private static final int FOOTER_SIZE = 0;
    private byte[] buf;
    private byte[] completeHeader;
    private int count;
    private PrintStream out;
    private int preferedHeaderSize;
    private int preferredChunkDataSize;
    private int preferredChunkGrossSize;
    private int size;
    private int spaceInCurrentChunk;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.net.www.http.ChunkedOutputStream.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.net.www.http.ChunkedOutputStream.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: sun.net.www.http.ChunkedOutputStream.<clinit>():void");
    }

    private static int getHeaderSize(int size) {
        return Integer.toHexString(size).length() + CRLF_SIZE;
    }

    private static byte[] getHeader(int size) {
        try {
            byte[] hexBytes = Integer.toHexString(size).getBytes("US-ASCII");
            byte[] header = new byte[getHeaderSize(size)];
            for (int i = CRLF_SIZE; i < hexBytes.length; i++) {
                header[i] = hexBytes[i];
            }
            header[hexBytes.length] = CRLF[CRLF_SIZE];
            header[hexBytes.length + 1] = CRLF[1];
            return header;
        } catch (UnsupportedEncodingException e) {
            throw new InternalError(e.getMessage());
        }
    }

    public ChunkedOutputStream(PrintStream o) {
        this(o, DEFAULT_CHUNK_SIZE);
    }

    public ChunkedOutputStream(PrintStream o, int size) {
        super((OutputStream) o);
        this.out = o;
        if (size <= 0) {
            size = DEFAULT_CHUNK_SIZE;
        }
        if (size > 0) {
            int adjusted_size = (size - getHeaderSize(size)) - FOOTER_SIZE;
            if (getHeaderSize(adjusted_size + 1) < getHeaderSize(size)) {
                adjusted_size++;
            }
            size = adjusted_size;
        }
        if (size > 0) {
            this.preferredChunkDataSize = size;
        } else {
            this.preferredChunkDataSize = (4096 - getHeaderSize(DEFAULT_CHUNK_SIZE)) - FOOTER_SIZE;
        }
        this.preferedHeaderSize = getHeaderSize(this.preferredChunkDataSize);
        this.preferredChunkGrossSize = (this.preferedHeaderSize + this.preferredChunkDataSize) + FOOTER_SIZE;
        this.completeHeader = getHeader(this.preferredChunkDataSize);
        this.buf = new byte[(this.preferredChunkDataSize + 32)];
        reset();
    }

    private void flush(boolean flushAll) {
        if (this.spaceInCurrentChunk == 0) {
            this.out.write(this.buf, CRLF_SIZE, this.preferredChunkGrossSize);
            this.out.flush();
            reset();
        } else if (flushAll) {
            if (this.size > 0) {
                int adjustedHeaderStartIndex = this.preferedHeaderSize - getHeaderSize(this.size);
                System.arraycopy(getHeader(this.size), (int) CRLF_SIZE, this.buf, adjustedHeaderStartIndex, getHeaderSize(this.size));
                byte[] bArr = this.buf;
                int i = this.count;
                this.count = i + 1;
                bArr[i] = FOOTER[CRLF_SIZE];
                bArr = this.buf;
                i = this.count;
                this.count = i + 1;
                bArr[i] = FOOTER[1];
                this.out.write(this.buf, adjustedHeaderStartIndex, this.count - adjustedHeaderStartIndex);
            } else {
                this.out.write(EMPTY_CHUNK_HEADER, CRLF_SIZE, EMPTY_CHUNK_HEADER_SIZE);
            }
            this.out.flush();
            reset();
        }
    }

    public boolean checkError() {
        return this.out.checkError();
    }

    private void ensureOpen() {
        if (this.out == null) {
            setError();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void write(byte[] b, int off, int len) {
        ensureOpen();
        if (off >= 0 && off <= b.length && len >= 0) {
            if (off + len <= b.length && off + len >= 0) {
                if (len != 0) {
                    int bytesToWrite = len;
                    int inputIndex = off;
                    while (true) {
                        if (bytesToWrite >= this.spaceInCurrentChunk) {
                            for (int i = CRLF_SIZE; i < this.completeHeader.length; i++) {
                                this.buf[i] = this.completeHeader[i];
                            }
                            System.arraycopy(b, inputIndex, this.buf, this.count, this.spaceInCurrentChunk);
                            inputIndex += this.spaceInCurrentChunk;
                            bytesToWrite -= this.spaceInCurrentChunk;
                            this.count += this.spaceInCurrentChunk;
                            byte[] bArr = this.buf;
                            int i2 = this.count;
                            this.count = i2 + 1;
                            bArr[i2] = FOOTER[CRLF_SIZE];
                            bArr = this.buf;
                            i2 = this.count;
                            this.count = i2 + 1;
                            bArr[i2] = FOOTER[1];
                            this.spaceInCurrentChunk = CRLF_SIZE;
                            flush(false);
                            if (checkError()) {
                                break;
                            }
                            if (bytesToWrite > 0) {
                                break;
                            }
                        } else {
                            System.arraycopy(b, inputIndex, this.buf, this.count, bytesToWrite);
                            this.count += bytesToWrite;
                            this.size += bytesToWrite;
                            this.spaceInCurrentChunk -= bytesToWrite;
                            bytesToWrite = CRLF_SIZE;
                            if (bytesToWrite > 0) {
                                break;
                            }
                        }
                    }
                } else {
                    return;
                }
            }
        }
        throw new IndexOutOfBoundsException();
    }

    public synchronized void write(int _b) {
        write(new byte[]{(byte) _b}, CRLF_SIZE, 1);
    }

    public synchronized void reset() {
        this.count = this.preferedHeaderSize;
        this.size = CRLF_SIZE;
        this.spaceInCurrentChunk = this.preferredChunkDataSize;
    }

    public int size() {
        return this.size;
    }

    public synchronized void close() {
        ensureOpen();
        if (this.size > 0) {
            flush(true);
        }
        flush(true);
        this.out = null;
    }

    public synchronized void flush() {
        ensureOpen();
        if (this.size > 0) {
            flush(true);
        }
    }
}
