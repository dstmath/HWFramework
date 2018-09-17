package com.huawei.okhttp3.internal.cache2;

import com.huawei.okio.Buffer;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

final class FileOperator {
    private static final int BUFFER_SIZE = 8192;
    private final byte[] byteArray = new byte[8192];
    private final ByteBuffer byteBuffer = ByteBuffer.wrap(this.byteArray);
    private final FileChannel fileChannel;

    public FileOperator(FileChannel fileChannel) {
        this.fileChannel = fileChannel;
    }

    public void write(long pos, Buffer source, long byteCount) throws IOException {
        if (byteCount < 0 || byteCount > source.size()) {
            throw new IndexOutOfBoundsException();
        }
        while (byteCount > 0) {
            try {
                int toWrite = (int) Math.min(8192, byteCount);
                source.read(this.byteArray, 0, toWrite);
                this.byteBuffer.limit(toWrite);
                while (true) {
                    pos += (long) this.fileChannel.write(this.byteBuffer, pos);
                    if (!this.byteBuffer.hasRemaining()) {
                        break;
                    }
                }
                byteCount -= (long) toWrite;
                this.byteBuffer.clear();
            } catch (Throwable th) {
                this.byteBuffer.clear();
                throw th;
            }
        }
    }

    public void read(long pos, Buffer sink, long byteCount) throws IOException {
        if (byteCount < 0) {
            throw new IndexOutOfBoundsException();
        }
        while (byteCount > 0) {
            this.byteBuffer.limit((int) Math.min(8192, byteCount));
            if (this.fileChannel.read(this.byteBuffer, pos) == -1) {
                throw new EOFException();
            }
            try {
                int bytesRead = this.byteBuffer.position();
                sink.write(this.byteArray, 0, bytesRead);
                pos += (long) bytesRead;
                byteCount -= (long) bytesRead;
                this.byteBuffer.clear();
            } catch (Throwable th) {
                this.byteBuffer.clear();
                throw th;
            }
        }
    }
}
