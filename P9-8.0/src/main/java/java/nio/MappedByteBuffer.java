package java.nio;

import java.io.FileDescriptor;
import sun.misc.Unsafe;

public abstract class MappedByteBuffer extends ByteBuffer {
    private static byte unused;
    private final FileDescriptor fd;

    private native void force0(FileDescriptor fileDescriptor, long j, long j2);

    private native boolean isLoaded0(long j, long j2, int i);

    private native void load0(long j, long j2);

    MappedByteBuffer(int mark, int pos, int lim, int cap, FileDescriptor fd) {
        super(mark, pos, lim, cap);
        this.fd = fd;
    }

    MappedByteBuffer(int mark, int pos, int lim, int cap, byte[] buf, int offset) {
        super(mark, pos, lim, cap, buf, offset);
        this.fd = null;
    }

    MappedByteBuffer(int mark, int pos, int lim, int cap) {
        super(mark, pos, lim, cap);
        this.fd = null;
    }

    private void checkMapped() {
        if (this.fd == null) {
            throw new UnsupportedOperationException();
        }
    }

    private long mappingOffset() {
        int ps = Bits.pageSize();
        long offset = this.address % ((long) ps);
        return offset >= 0 ? offset : offset + ((long) ps);
    }

    private long mappingAddress(long mappingOffset) {
        return this.address - mappingOffset;
    }

    private long mappingLength(long mappingOffset) {
        return ((long) capacity()) + mappingOffset;
    }

    public final boolean isLoaded() {
        checkMapped();
        if (this.address == 0 || capacity() == 0) {
            return true;
        }
        long offset = mappingOffset();
        long length = mappingLength(offset);
        return isLoaded0(mappingAddress(offset), length, Bits.pageCount(length));
    }

    public final MappedByteBuffer load() {
        checkMapped();
        if (this.address == 0 || capacity() == 0) {
            return this;
        }
        long offset = mappingOffset();
        long length = mappingLength(offset);
        load0(mappingAddress(offset), length);
        Unsafe unsafe = Unsafe.getUnsafe();
        int ps = Bits.pageSize();
        int count = Bits.pageCount(length);
        long a = mappingAddress(offset);
        byte x = (byte) 0;
        for (int i = 0; i < count; i++) {
            x = (byte) (unsafe.getByte(a) ^ x);
            a += (long) ps;
        }
        if (unused != (byte) 0) {
            unused = x;
        }
        return this;
    }

    public final MappedByteBuffer force() {
        checkMapped();
        if (!(this.address == 0 || capacity() == 0)) {
            long offset = mappingOffset();
            force0(this.fd, mappingAddress(offset), mappingLength(offset));
        }
        return this;
    }
}
