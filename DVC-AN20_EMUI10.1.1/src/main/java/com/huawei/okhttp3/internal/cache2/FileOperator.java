package com.huawei.okhttp3.internal.cache2;

import com.huawei.okio.Buffer;
import java.io.IOException;
import java.nio.channels.FileChannel;

final class FileOperator {
    private final FileChannel fileChannel;

    FileOperator(FileChannel fileChannel2) {
        this.fileChannel = fileChannel2;
    }

    public void write(long pos, Buffer source, long byteCount) throws IOException {
        if (byteCount < 0 || byteCount > source.size()) {
            throw new IndexOutOfBoundsException();
        }
        while (byteCount > 0) {
            long bytesWritten = this.fileChannel.transferFrom(source, pos, byteCount);
            pos += bytesWritten;
            byteCount -= bytesWritten;
        }
    }

    public void read(long pos, Buffer sink, long byteCount) throws IOException {
        if (byteCount >= 0) {
            while (byteCount > 0) {
                long bytesRead = this.fileChannel.transferTo(pos, byteCount, sink);
                pos += bytesRead;
                byteCount -= bytesRead;
            }
            return;
        }
        throw new IndexOutOfBoundsException();
    }
}
