package libcore.io;

import android.system.ErrnoException;
import android.system.OsConstants;
import java.io.FileDescriptor;
import java.nio.ByteOrder;

public final class MemoryMappedFile implements AutoCloseable {
    private long address;
    private final long size;

    public MemoryMappedFile(long address, long size) {
        this.address = address;
        this.size = size;
    }

    public static MemoryMappedFile mmapRO(String path) throws ErrnoException {
        FileDescriptor fd = Libcore.os.open(path, OsConstants.O_RDONLY, 0);
        long size = Libcore.os.fstat(fd).st_size;
        long address = Libcore.os.mmap(0, size, OsConstants.PROT_READ, OsConstants.MAP_SHARED, fd, 0);
        Libcore.os.close(fd);
        return new MemoryMappedFile(address, size);
    }

    public synchronized void close() throws ErrnoException {
        if (this.address != 0) {
            Libcore.os.munmap(this.address, this.size);
            this.address = 0;
        }
    }

    public BufferIterator bigEndianIterator() {
        return new NioBufferIterator(this.address, (int) this.size, ByteOrder.nativeOrder() != ByteOrder.BIG_ENDIAN);
    }

    public BufferIterator littleEndianIterator() {
        return new NioBufferIterator(this.address, (int) this.size, ByteOrder.nativeOrder() != ByteOrder.LITTLE_ENDIAN);
    }

    public long size() {
        return this.size;
    }
}
