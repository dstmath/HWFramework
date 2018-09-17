package com.huawei.okhttp3.internal.cache2;

import com.huawei.okhttp3.internal.Util;
import com.huawei.okio.Buffer;
import com.huawei.okio.ByteString;
import com.huawei.okio.Source;
import com.huawei.okio.Timeout;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

final class Relay {
    private static final long FILE_HEADER_SIZE = 32;
    static final ByteString PREFIX_CLEAN = ByteString.encodeUtf8("OkHttp cache v1\n");
    static final ByteString PREFIX_DIRTY = ByteString.encodeUtf8("OkHttp DIRTY :(\n");
    private static final int SOURCE_FILE = 2;
    private static final int SOURCE_UPSTREAM = 1;
    final Buffer buffer = new Buffer();
    final long bufferMaxSize;
    boolean complete;
    RandomAccessFile file;
    private final ByteString metadata;
    int sourceCount;
    Source upstream;
    final Buffer upstreamBuffer = new Buffer();
    long upstreamPos;
    Thread upstreamReader;

    class RelaySource implements Source {
        private FileOperator fileOperator = new FileOperator(Relay.this.file.getChannel());
        private long sourcePos;
        private final Timeout timeout = new Timeout();

        RelaySource() {
        }

        /* JADX WARNING: Missing block: B:23:?, code:
            r28.this$0.upstreamReader = java.lang.Thread.currentThread();
     */
        /* JADX WARNING: Missing block: B:24:0x0051, code:
            r24 = 1;
     */
        /* JADX WARNING: Missing block: B:31:?, code:
            r22 = r26 - r28.this$0.buffer.size();
     */
        /* JADX WARNING: Missing block: B:33:0x0092, code:
            if (r28.sourcePos >= r22) goto L_0x0098;
     */
        /* JADX WARNING: Missing block: B:34:0x0094, code:
            r24 = 2;
     */
        /* JADX WARNING: Missing block: B:37:?, code:
            r8 = java.lang.Math.min(r30, r26 - r28.sourcePos);
            r28.this$0.buffer.copyTo(r29, r28.sourcePos - r22, r8);
            r28.sourcePos += r8;
     */
        /* JADX WARNING: Missing block: B:39:0x00bf, code:
            return r8;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public long read(Buffer sink, long byteCount) throws IOException {
            if (this.fileOperator == null) {
                throw new IllegalStateException("closed");
            }
            synchronized (Relay.this) {
                long upstreamPos;
                while (true) {
                    long j = this.sourcePos;
                    upstreamPos = Relay.this.upstreamPos;
                    if (j == upstreamPos) {
                        if (!Relay.this.complete) {
                            if (Relay.this.upstreamReader == null) {
                                break;
                            }
                            this.timeout.waitUntilNotified(Relay.this);
                        } else {
                            return -1;
                        }
                    }
                    break;
                }
                if (source == 2) {
                    long bytesToRead = Math.min(byteCount, upstreamPos - this.sourcePos);
                    this.fileOperator.read(Relay.FILE_HEADER_SIZE + this.sourcePos, sink, bytesToRead);
                    this.sourcePos += bytesToRead;
                    return bytesToRead;
                }
                try {
                    Source source = Relay.this.upstream;
                    Buffer buffer = Relay.this.upstreamBuffer;
                    Object obj = Relay.this.bufferMaxSize;
                    long upstreamBytesRead = source.read(buffer, obj);
                    if (upstreamBytesRead == -1) {
                        Relay.this.commit(upstreamPos);
                        Relay.this.upstreamReader = null;
                        Relay.this.notifyAll();
                        return -1;
                    }
                    long bytesRead = Math.min(upstreamBytesRead, byteCount);
                    Relay.this.upstreamBuffer.copyTo(sink, 0, bytesRead);
                    this.sourcePos += bytesRead;
                    this.fileOperator.write(Relay.FILE_HEADER_SIZE + upstreamPos, Relay.this.upstreamBuffer.clone(), upstreamBytesRead);
                    synchronized (Relay.this) {
                        Relay.this.buffer.write(Relay.this.upstreamBuffer, upstreamBytesRead);
                        if (Relay.this.buffer.size() > Relay.this.bufferMaxSize) {
                            Relay.this.buffer.skip(Relay.this.buffer.size() - Relay.this.bufferMaxSize);
                        }
                        Relay relay = Relay.this;
                        relay.upstreamPos += upstreamBytesRead;
                    }
                    synchronized (Relay.this) {
                        Relay.this.upstreamReader = null;
                        Relay.this.notifyAll();
                    }
                    return bytesRead;
                } finally {
                    synchronized (Relay.this) {
                        Relay.this.upstreamReader = null;
                        Relay.this.notifyAll();
                    }
                }
            }
        }

        public Timeout timeout() {
            return this.timeout;
        }

        public void close() throws IOException {
            if (this.fileOperator != null) {
                this.fileOperator = null;
                Closeable fileToClose = null;
                synchronized (Relay.this) {
                    Relay relay = Relay.this;
                    relay.sourceCount--;
                    if (Relay.this.sourceCount == 0) {
                        fileToClose = Relay.this.file;
                        Relay.this.file = null;
                    }
                }
                if (fileToClose != null) {
                    Util.closeQuietly(fileToClose);
                }
            }
        }
    }

    private Relay(RandomAccessFile file, Source upstream, long upstreamPos, ByteString metadata, long bufferMaxSize) {
        this.file = file;
        this.upstream = upstream;
        this.complete = upstream == null;
        this.upstreamPos = upstreamPos;
        this.metadata = metadata;
        this.bufferMaxSize = bufferMaxSize;
    }

    public static Relay edit(File file, Source upstream, ByteString metadata, long bufferMaxSize) throws IOException {
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
        Relay result = new Relay(randomAccessFile, upstream, 0, metadata, bufferMaxSize);
        randomAccessFile.setLength(0);
        result.writeHeader(PREFIX_DIRTY, -1, -1);
        return result;
    }

    public static Relay read(File file) throws IOException {
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
        FileOperator fileOperator = new FileOperator(randomAccessFile.getChannel());
        Buffer header = new Buffer();
        fileOperator.read(0, header, FILE_HEADER_SIZE);
        if (header.readByteString((long) PREFIX_CLEAN.size()).equals(PREFIX_CLEAN)) {
            long upstreamSize = header.readLong();
            long metadataSize = header.readLong();
            Buffer metadataBuffer = new Buffer();
            fileOperator.read(FILE_HEADER_SIZE + upstreamSize, metadataBuffer, metadataSize);
            return new Relay(randomAccessFile, null, upstreamSize, metadataBuffer.readByteString(), 0);
        }
        throw new IOException("unreadable cache file");
    }

    private void writeHeader(ByteString prefix, long upstreamSize, long metadataSize) throws IOException {
        Buffer header = new Buffer();
        header.write(prefix);
        header.writeLong(upstreamSize);
        header.writeLong(metadataSize);
        if (header.size() != FILE_HEADER_SIZE) {
            throw new IllegalArgumentException();
        }
        new FileOperator(this.file.getChannel()).write(0, header, FILE_HEADER_SIZE);
    }

    private void writeMetadata(long upstreamSize) throws IOException {
        Buffer metadataBuffer = new Buffer();
        metadataBuffer.write(this.metadata);
        new FileOperator(this.file.getChannel()).write(FILE_HEADER_SIZE + upstreamSize, metadataBuffer, (long) this.metadata.size());
    }

    void commit(long upstreamSize) throws IOException {
        writeMetadata(upstreamSize);
        this.file.getChannel().force(false);
        writeHeader(PREFIX_CLEAN, upstreamSize, (long) this.metadata.size());
        this.file.getChannel().force(false);
        synchronized (this) {
            this.complete = true;
        }
        Util.closeQuietly(this.upstream);
        this.upstream = null;
    }

    boolean isClosed() {
        return this.file == null;
    }

    public ByteString metadata() {
        return this.metadata;
    }

    public Source newSource() {
        synchronized (this) {
            if (this.file == null) {
                return null;
            }
            this.sourceCount++;
            return new RelaySource();
        }
    }
}
