package libcore.io;

import android.system.ErrnoException;
import android.system.OsConstants;
import java.io.FileDescriptor;
import java.nio.ByteOrder;

public final class MemoryMappedFile implements AutoCloseable {
    private final long address;
    private boolean closed;
    private final int size;

    public MemoryMappedFile(long address2, long size2) {
        this.address = address2;
        if (size2 < 0 || size2 > 2147483647L) {
            throw new IllegalArgumentException("Unsupported file size=" + size2);
        }
        this.size = (int) size2;
    }

    public static MemoryMappedFile mmapRO(String path) throws ErrnoException {
        FileDescriptor fd = Libcore.os.open(path, OsConstants.O_RDONLY, 0);
        try {
            long size2 = Libcore.os.fstat(fd).st_size;
            return new MemoryMappedFile(Libcore.os.mmap(0, size2, OsConstants.PROT_READ, OsConstants.MAP_SHARED, fd, 0), size2);
        } finally {
            Libcore.os.close(fd);
        }
    }

    public void close() throws ErrnoException {
        if (!this.closed) {
            this.closed = true;
            Libcore.os.munmap(this.address, (long) this.size);
        }
    }

    public boolean isClosed() {
        return this.closed;
    }

    public BufferIterator bigEndianIterator() {
        NioBufferIterator nioBufferIterator = new NioBufferIterator(this, this.address, this.size, ByteOrder.nativeOrder() != ByteOrder.BIG_ENDIAN);
        return nioBufferIterator;
    }

    public BufferIterator littleEndianIterator() {
        NioBufferIterator nioBufferIterator = new NioBufferIterator(this, this.address, this.size, ByteOrder.nativeOrder() != ByteOrder.LITTLE_ENDIAN);
        return nioBufferIterator;
    }

    /* access modifiers changed from: package-private */
    public void checkNotClosed() {
        if (this.closed) {
            throw new IllegalStateException("MemoryMappedFile is closed");
        }
    }

    public int size() {
        checkNotClosed();
        return this.size;
    }
}
