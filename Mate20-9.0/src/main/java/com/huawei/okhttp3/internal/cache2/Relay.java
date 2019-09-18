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

        /* JADX WARNING: Code restructure failed: missing block: B:17:0x0039, code lost:
            r7 = r9 - r1.this$0.buffer.size();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:18:0x0047, code lost:
            if (r1.sourcePos >= r7) goto L_0x0128;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:19:0x0049, code lost:
            r0 = 2;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:79:?, code lost:
            r5 = java.lang.Math.min(r2, r9 - r1.sourcePos);
            r1.this$0.buffer.copyTo(r23, r1.sourcePos - r7, r5);
            r1.sourcePos += r5;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:81:0x0145, code lost:
            return r5;
         */
        public long read(Buffer sink, long byteCount) throws IOException {
            long upstreamPos;
            int source;
            long j = byteCount;
            if (this.fileOperator != null) {
                synchronized (Relay.this) {
                    while (true) {
                        long j2 = this.sourcePos;
                        long j3 = Relay.this.upstreamPos;
                        upstreamPos = j3;
                        if (j2 != j3) {
                            break;
                        } else if (Relay.this.complete) {
                            return -1;
                        } else {
                            if (Relay.this.upstreamReader != null) {
                                this.timeout.waitUntilNotified(Relay.this);
                            } else {
                                Relay.this.upstreamReader = Thread.currentThread();
                                source = 1;
                            }
                        }
                    }
                    long bufferPos = upstreamPos;
                    if (source == 2) {
                        long bytesToRead = Math.min(j, bufferPos - this.sourcePos);
                        this.fileOperator.read(this.sourcePos + Relay.FILE_HEADER_SIZE, sink, bytesToRead);
                        this.sourcePos += bytesToRead;
                        return bytesToRead;
                    }
                    try {
                        long upstreamBytesRead = Relay.this.upstream.read(Relay.this.upstreamBuffer, Relay.this.bufferMaxSize);
                        if (upstreamBytesRead == -1) {
                            Relay.this.commit(bufferPos);
                            synchronized (Relay.this) {
                                Relay.this.upstreamReader = null;
                                Relay.this.notifyAll();
                            }
                            return -1;
                        }
                        long bytesRead = Math.min(upstreamBytesRead, j);
                        Relay.this.upstreamBuffer.copyTo(sink, 0, bytesRead);
                        this.sourcePos += bytesRead;
                        this.fileOperator.write(Relay.FILE_HEADER_SIZE + bufferPos, Relay.this.upstreamBuffer.clone(), upstreamBytesRead);
                        synchronized (Relay.this) {
                            try {
                                Relay.this.buffer.write(Relay.this.upstreamBuffer, upstreamBytesRead);
                                long bytesRead2 = bytesRead;
                                try {
                                    if (Relay.this.buffer.size() > Relay.this.bufferMaxSize) {
                                        Relay.this.buffer.skip(Relay.this.buffer.size() - Relay.this.bufferMaxSize);
                                    }
                                    Relay.this.upstreamPos += upstreamBytesRead;
                                    synchronized (Relay.this) {
                                        Relay.this.upstreamReader = null;
                                        Relay.this.notifyAll();
                                    }
                                    return bytesRead2;
                                } catch (Throwable th) {
                                    th = th;
                                    throw th;
                                }
                            } catch (Throwable th2) {
                                th = th2;
                                long j4 = bytesRead;
                                throw th;
                            }
                        }
                    } catch (Throwable th3) {
                        synchronized (Relay.this) {
                            Relay.this.upstreamReader = null;
                            Relay.this.notifyAll();
                            throw th3;
                        }
                    }
                }
            } else {
                throw new IllegalStateException("closed");
            }
        }

        public Timeout timeout() {
            return this.timeout;
        }

        public void close() throws IOException {
            if (this.fileOperator != null) {
                this.fileOperator = null;
                RandomAccessFile fileToClose = null;
                synchronized (Relay.this) {
                    Relay relay = Relay.this;
                    relay.sourceCount--;
                    if (Relay.this.sourceCount == 0) {
                        fileToClose = Relay.this.file;
                        Relay.this.file = null;
                    }
                }
                if (fileToClose != null) {
                    Util.closeQuietly((Closeable) fileToClose);
                }
            }
        }
    }

    private Relay(RandomAccessFile file2, Source upstream2, long upstreamPos2, ByteString metadata2, long bufferMaxSize2) {
        this.file = file2;
        this.upstream = upstream2;
        this.complete = upstream2 == null;
        this.upstreamPos = upstreamPos2;
        this.metadata = metadata2;
        this.bufferMaxSize = bufferMaxSize2;
    }

    public static Relay edit(File file2, Source upstream2, ByteString metadata2, long bufferMaxSize2) throws IOException {
        RandomAccessFile randomAccessFile = new RandomAccessFile(file2, "rw");
        Relay relay = new Relay(randomAccessFile, upstream2, 0, metadata2, bufferMaxSize2);
        randomAccessFile.setLength(0);
        relay.writeHeader(PREFIX_DIRTY, -1, -1);
        return relay;
    }

    public static Relay read(File file2) throws IOException {
        RandomAccessFile randomAccessFile = new RandomAccessFile(file2, "rw");
        FileOperator fileOperator = new FileOperator(randomAccessFile.getChannel());
        Buffer header = new Buffer();
        fileOperator.read(0, header, FILE_HEADER_SIZE);
        if (header.readByteString((long) PREFIX_CLEAN.size()).equals(PREFIX_CLEAN)) {
            long upstreamSize = header.readLong();
            long metadataSize = header.readLong();
            Buffer metadataBuffer = new Buffer();
            fileOperator.read(FILE_HEADER_SIZE + upstreamSize, metadataBuffer, metadataSize);
            Buffer buffer2 = metadataBuffer;
            Relay relay = new Relay(randomAccessFile, null, upstreamSize, metadataBuffer.readByteString(), 0);
            return relay;
        }
        throw new IOException("unreadable cache file");
    }

    private void writeHeader(ByteString prefix, long upstreamSize, long metadataSize) throws IOException {
        Buffer header = new Buffer();
        header.write(prefix);
        header.writeLong(upstreamSize);
        header.writeLong(metadataSize);
        if (header.size() == FILE_HEADER_SIZE) {
            new FileOperator(this.file.getChannel()).write(0, header, FILE_HEADER_SIZE);
            return;
        }
        throw new IllegalArgumentException();
    }

    private void writeMetadata(long upstreamSize) throws IOException {
        Buffer metadataBuffer = new Buffer();
        metadataBuffer.write(this.metadata);
        new FileOperator(this.file.getChannel()).write(FILE_HEADER_SIZE + upstreamSize, metadataBuffer, (long) this.metadata.size());
    }

    /* access modifiers changed from: package-private */
    public void commit(long upstreamSize) throws IOException {
        writeMetadata(upstreamSize);
        this.file.getChannel().force(false);
        writeHeader(PREFIX_CLEAN, upstreamSize, (long) this.metadata.size());
        this.file.getChannel().force(false);
        synchronized (this) {
            this.complete = true;
        }
        Util.closeQuietly((Closeable) this.upstream);
        this.upstream = null;
    }

    /* access modifiers changed from: package-private */
    public boolean isClosed() {
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
