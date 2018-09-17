package libcore.io;

import android.system.ErrnoException;
import android.system.OsConstants;
import java.io.FileDescriptor;
import java.nio.ByteOrder;

public final class MemoryMappedFile implements AutoCloseable {
    private final long address;
    private boolean closed;
    private final int size;

    public MemoryMappedFile(long address, long size) {
        this.address = address;
        if (size < 0 || size > 2147483647L) {
            throw new IllegalArgumentException("Unsupported file size=" + size);
        }
        this.size = (int) size;
    }

    public static MemoryMappedFile mmapRO(String path) throws ErrnoException {
        FileDescriptor fd = Libcore.os.open(path, OsConstants.O_RDONLY, 0);
        try {
            long size = Libcore.os.fstat(fd).st_size;
            MemoryMappedFile memoryMappedFile = new MemoryMappedFile(Libcore.os.mmap(0, size, OsConstants.PROT_READ, OsConstants.MAP_SHARED, fd, 0), size);
            return memoryMappedFile;
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
        return new NioBufferIterator(this, this.address, this.size, ByteOrder.nativeOrder() != ByteOrder.BIG_ENDIAN);
    }

    public BufferIterator littleEndianIterator() {
        return new NioBufferIterator(this, this.address, this.size, ByteOrder.nativeOrder() != ByteOrder.LITTLE_ENDIAN);
    }

    void checkNotClosed() {
        if (this.closed) {
            throw new IllegalStateException("MemoryMappedFile is closed");
        }
    }

    public int size() {
        checkNotClosed();
        return this.size;
    }
}
