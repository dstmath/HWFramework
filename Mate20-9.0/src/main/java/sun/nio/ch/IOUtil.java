package sun.nio.ch;

import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.ByteBuffer;

public class IOUtil {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    static final int IOV_MAX = iovMax();

    public static native void configureBlocking(FileDescriptor fileDescriptor, boolean z) throws IOException;

    static native boolean drain(int i) throws IOException;

    static native int fdLimit();

    public static native int fdVal(FileDescriptor fileDescriptor);

    static native int iovMax();

    static native long makePipe(boolean z);

    static native boolean randomBytes(byte[] bArr);

    static native void setfdVal(FileDescriptor fileDescriptor, int i);

    private IOUtil() {
    }

    static int write(FileDescriptor fd, ByteBuffer src, long position, NativeDispatcher nd) throws IOException {
        if (src instanceof DirectBuffer) {
            return writeFromNativeBuffer(fd, src, position, nd);
        }
        int pos = src.position();
        int lim = src.limit();
        ByteBuffer bb = Util.getTemporaryDirectBuffer(pos <= lim ? lim - pos : 0);
        try {
            bb.put(src);
            bb.flip();
            src.position(pos);
            int n = writeFromNativeBuffer(fd, bb, position, nd);
            if (n > 0) {
                src.position(pos + n);
            }
            return n;
        } finally {
            Util.offerFirstTemporaryDirectBuffer(bb);
        }
    }

    private static int writeFromNativeBuffer(FileDescriptor fd, ByteBuffer bb, long position, NativeDispatcher nd) throws IOException {
        int written;
        ByteBuffer byteBuffer = bb;
        int pos = byteBuffer.position();
        int lim = byteBuffer.limit();
        int rem = pos <= lim ? lim - pos : 0;
        if (rem == 0) {
            return 0;
        }
        if (position != -1) {
            NativeDispatcher nativeDispatcher = nd;
            written = nd.pwrite(fd, ((long) pos) + ((DirectBuffer) byteBuffer).address(), rem, position);
            FileDescriptor fileDescriptor = fd;
        } else {
            written = nd.write(fd, ((DirectBuffer) byteBuffer).address() + ((long) pos), rem);
        }
        if (written > 0) {
            byteBuffer.position(pos + written);
        }
        return written;
    }

    static long write(FileDescriptor fd, ByteBuffer[] bufs, NativeDispatcher nd) throws IOException {
        return write(fd, bufs, 0, bufs.length, nd);
    }

    static long write(FileDescriptor fd, ByteBuffer[] bufs, int offset, int length, NativeDispatcher nd) throws IOException {
        int count;
        IOVecWrapper vec = IOVecWrapper.get(length);
        int i = 0;
        int count2 = offset + length;
        int iov_len = 0;
        int i2 = offset;
        while (i2 < count2) {
            try {
                if (iov_len >= IOV_MAX) {
                    break;
                }
                ByteBuffer buf = bufs[i2];
                int pos = buf.position();
                int lim = buf.limit();
                int rem = pos <= lim ? lim - pos : i;
                if (rem > 0) {
                    vec.setBuffer(iov_len, buf, pos, rem);
                    if (!(buf instanceof DirectBuffer)) {
                        ByteBuffer shadow = Util.getTemporaryDirectBuffer(rem);
                        shadow.put(buf);
                        shadow.flip();
                        vec.setShadow(iov_len, shadow);
                        buf.position(pos);
                        buf = shadow;
                        pos = shadow.position();
                    }
                    vec.putBase(iov_len, ((DirectBuffer) buf).address() + ((long) pos));
                    vec.putLen(iov_len, (long) rem);
                    iov_len++;
                }
                i2++;
                i = 0;
            } catch (Throwable th) {
                if (0 == 0) {
                    int j = 0;
                    while (true) {
                        int j2 = j;
                        if (j2 >= iov_len) {
                            break;
                        }
                        ByteBuffer shadow2 = vec.getShadow(j2);
                        if (shadow2 != null) {
                            Util.offerLastTemporaryDirectBuffer(shadow2);
                        }
                        vec.clearRefs(j2);
                        j = j2 + 1;
                    }
                }
                throw th;
            }
        }
        if (iov_len == 0) {
            if (0 == 0) {
                int j3 = 0;
                while (true) {
                    int j4 = j3;
                    if (j4 >= iov_len) {
                        break;
                    }
                    ByteBuffer shadow3 = vec.getShadow(j4);
                    if (shadow3 != null) {
                        Util.offerLastTemporaryDirectBuffer(shadow3);
                    }
                    vec.clearRefs(j4);
                    j3 = j4 + 1;
                }
            }
            return 0;
        }
        long bytesWritten = nd.writev(fd, vec.address, iov_len);
        long left = bytesWritten;
        int j5 = 0;
        while (j5 < iov_len) {
            if (left > 0) {
                ByteBuffer buf2 = vec.getBuffer(j5);
                int pos2 = vec.getPosition(j5);
                count = count2;
                int rem2 = vec.getRemaining(j5);
                int n = left > ((long) rem2) ? rem2 : (int) left;
                buf2.position(pos2 + n);
                left -= (long) n;
            } else {
                count = count2;
            }
            ByteBuffer shadow4 = vec.getShadow(j5);
            if (shadow4 != null) {
                Util.offerLastTemporaryDirectBuffer(shadow4);
            }
            vec.clearRefs(j5);
            j5++;
            count2 = count;
            FileDescriptor fileDescriptor = fd;
            NativeDispatcher nativeDispatcher = nd;
        }
        if (1 == 0) {
            int j6 = 0;
            while (true) {
                int j7 = j6;
                if (j7 >= iov_len) {
                    break;
                }
                ByteBuffer shadow5 = vec.getShadow(j7);
                if (shadow5 != null) {
                    Util.offerLastTemporaryDirectBuffer(shadow5);
                }
                vec.clearRefs(j7);
                j6 = j7 + 1;
            }
        }
        return bytesWritten;
    }

    static int read(FileDescriptor fd, ByteBuffer dst, long position, NativeDispatcher nd) throws IOException {
        if (dst.isReadOnly()) {
            throw new IllegalArgumentException("Read-only buffer");
        } else if (dst instanceof DirectBuffer) {
            return readIntoNativeBuffer(fd, dst, position, nd);
        } else {
            ByteBuffer bb = Util.getTemporaryDirectBuffer(dst.remaining());
            try {
                int n = readIntoNativeBuffer(fd, bb, position, nd);
                bb.flip();
                if (n > 0) {
                    dst.put(bb);
                }
                return n;
            } finally {
                Util.offerFirstTemporaryDirectBuffer(bb);
            }
        }
    }

    private static int readIntoNativeBuffer(FileDescriptor fd, ByteBuffer bb, long position, NativeDispatcher nd) throws IOException {
        int n;
        int pos = bb.position();
        int lim = bb.limit();
        int rem = pos <= lim ? lim - pos : 0;
        if (rem == 0) {
            return 0;
        }
        if (position != -1) {
            n = nd.pread(fd, ((long) pos) + ((DirectBuffer) bb).address(), rem, position);
        } else {
            n = nd.read(fd, ((DirectBuffer) bb).address() + ((long) pos), rem);
        }
        if (n > 0) {
            bb.position(pos + n);
        }
        return n;
    }

    static long read(FileDescriptor fd, ByteBuffer[] bufs, NativeDispatcher nd) throws IOException {
        return read(fd, bufs, 0, bufs.length, nd);
    }

    static long read(FileDescriptor fd, ByteBuffer[] bufs, int offset, int length, NativeDispatcher nd) throws IOException {
        int i;
        int count;
        IOVecWrapper vec = IOVecWrapper.get(length);
        int i2 = 0;
        int count2 = offset + length;
        int iov_len = 0;
        int i3 = offset;
        while (i3 < count2) {
            try {
                if (iov_len >= IOV_MAX) {
                    break;
                }
                ByteBuffer buf = bufs[i3];
                if (!buf.isReadOnly()) {
                    int pos = buf.position();
                    int lim = buf.limit();
                    int rem = pos <= lim ? lim - pos : i2;
                    if (rem > 0) {
                        vec.setBuffer(iov_len, buf, pos, rem);
                        if (!(buf instanceof DirectBuffer)) {
                            ByteBuffer shadow = Util.getTemporaryDirectBuffer(rem);
                            vec.setShadow(iov_len, shadow);
                            buf = shadow;
                            pos = shadow.position();
                        }
                        vec.putBase(iov_len, ((DirectBuffer) buf).address() + ((long) pos));
                        vec.putLen(iov_len, (long) rem);
                        iov_len++;
                    }
                    i3++;
                    i2 = 0;
                } else {
                    throw new IllegalArgumentException("Read-only buffer");
                }
            } catch (Throwable th) {
                if (0 == 0) {
                    int j = 0;
                    while (true) {
                        int j2 = j;
                        if (j2 >= iov_len) {
                            break;
                        }
                        ByteBuffer shadow2 = vec.getShadow(j2);
                        if (shadow2 != null) {
                            Util.offerLastTemporaryDirectBuffer(shadow2);
                        }
                        vec.clearRefs(j2);
                        j = j2 + 1;
                    }
                }
                throw th;
            }
        }
        if (iov_len == 0) {
            if (0 == 0) {
                int j3 = 0;
                while (true) {
                    int j4 = j3;
                    if (j4 >= iov_len) {
                        break;
                    }
                    ByteBuffer shadow3 = vec.getShadow(j4);
                    if (shadow3 != null) {
                        Util.offerLastTemporaryDirectBuffer(shadow3);
                    }
                    vec.clearRefs(j4);
                    j3 = j4 + 1;
                }
            }
            return 0;
        }
        long bytesRead = nd.readv(fd, vec.address, iov_len);
        long left = bytesRead;
        int j5 = 0;
        while (j5 < iov_len) {
            ByteBuffer shadow4 = vec.getShadow(j5);
            if (left > 0) {
                ByteBuffer buf2 = vec.getBuffer(j5);
                count = count2;
                int rem2 = vec.getRemaining(j5);
                int n = left > ((long) rem2) ? rem2 : (int) left;
                if (shadow4 == null) {
                    int pos2 = vec.getPosition(j5);
                    i = i3;
                    int i4 = pos2;
                    buf2.position(pos2 + n);
                } else {
                    i = i3;
                    shadow4.limit(shadow4.position() + n);
                    buf2.put(shadow4);
                }
                left -= (long) n;
            } else {
                i = i3;
                count = count2;
            }
            if (shadow4 != null) {
                Util.offerLastTemporaryDirectBuffer(shadow4);
            }
            vec.clearRefs(j5);
            j5++;
            count2 = count;
            i3 = i;
            FileDescriptor fileDescriptor = fd;
            NativeDispatcher nativeDispatcher = nd;
        }
        int i5 = count2;
        if (1 == 0) {
            int j6 = 0;
            while (true) {
                int j7 = j6;
                if (j7 >= iov_len) {
                    break;
                }
                ByteBuffer shadow5 = vec.getShadow(j7);
                if (shadow5 != null) {
                    Util.offerLastTemporaryDirectBuffer(shadow5);
                }
                vec.clearRefs(j7);
                j6 = j7 + 1;
            }
        }
        return bytesRead;
    }

    public static FileDescriptor newFD(int i) {
        FileDescriptor fd = new FileDescriptor();
        setfdVal(fd, i);
        return fd;
    }
}
