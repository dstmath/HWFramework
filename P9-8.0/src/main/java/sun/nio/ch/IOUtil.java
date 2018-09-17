package sun.nio.ch;

import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.ByteBuffer;

public class IOUtil {
    static final /* synthetic */ boolean -assertionsDisabled = (IOUtil.class.desiredAssertionStatus() ^ 1);
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
        if (-assertionsDisabled || pos <= lim) {
            ByteBuffer bb = Util.getTemporaryDirectBuffer(pos <= lim ? lim - pos : 0);
            try {
                bb.put(src);
                bb.flip();
                src.position(pos);
                int n = writeFromNativeBuffer(fd, bb, position, nd);
                if (n > 0) {
                    src.position(pos + n);
                }
                Util.offerFirstTemporaryDirectBuffer(bb);
                return n;
            } catch (Throwable th) {
                Util.offerFirstTemporaryDirectBuffer(bb);
            }
        } else {
            throw new AssertionError();
        }
    }

    private static int writeFromNativeBuffer(FileDescriptor fd, ByteBuffer bb, long position, NativeDispatcher nd) throws IOException {
        int pos = bb.position();
        int lim = bb.limit();
        if (-assertionsDisabled || pos <= lim) {
            int rem = pos <= lim ? lim - pos : 0;
            if (rem == 0) {
                return 0;
            }
            int written;
            if (position != -1) {
                written = nd.pwrite(fd, ((long) pos) + ((DirectBuffer) bb).address(), rem, position);
            } else {
                written = nd.write(fd, ((DirectBuffer) bb).address() + ((long) pos), rem);
            }
            if (written > 0) {
                bb.position(pos + written);
            }
            return written;
        }
        throw new AssertionError();
    }

    static long write(FileDescriptor fd, ByteBuffer[] bufs, NativeDispatcher nd) throws IOException {
        return write(fd, bufs, 0, bufs.length, nd);
    }

    static long write(FileDescriptor fd, ByteBuffer[] bufs, int offset, int length, NativeDispatcher nd) throws IOException {
        ByteBuffer buf;
        int pos;
        int rem;
        ByteBuffer shadow;
        int j;
        IOVecWrapper vec = IOVecWrapper.get(length);
        int iov_len = 0;
        int count = offset + length;
        int i = offset;
        while (i < count) {
            try {
                if (iov_len >= IOV_MAX) {
                    break;
                }
                buf = bufs[i];
                pos = buf.position();
                int lim = buf.limit();
                if (-assertionsDisabled || pos <= lim) {
                    rem = pos <= lim ? lim - pos : 0;
                    if (rem > 0) {
                        vec.setBuffer(iov_len, buf, pos, rem);
                        if (!(buf instanceof DirectBuffer)) {
                            shadow = Util.getTemporaryDirectBuffer(rem);
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
                    i++;
                } else {
                    throw new AssertionError();
                }
            } catch (Throwable th) {
                if (null == null) {
                    for (j = 0; j < iov_len; j++) {
                        shadow = vec.getShadow(j);
                        if (shadow != null) {
                            Util.offerLastTemporaryDirectBuffer(shadow);
                        }
                        vec.clearRefs(j);
                    }
                }
            }
        }
        if (iov_len == 0) {
            if (null == null) {
                for (j = 0; j < iov_len; j++) {
                    shadow = vec.getShadow(j);
                    if (shadow != null) {
                        Util.offerLastTemporaryDirectBuffer(shadow);
                    }
                    vec.clearRefs(j);
                }
            }
            return 0;
        }
        long bytesWritten = nd.writev(fd, vec.address, iov_len);
        long left = bytesWritten;
        for (j = 0; j < iov_len; j++) {
            if (left > 0) {
                buf = vec.getBuffer(j);
                pos = vec.getPosition(j);
                rem = vec.getRemaining(j);
                int n = left > ((long) rem) ? rem : (int) left;
                buf.position(pos + n);
                left -= (long) n;
            }
            shadow = vec.getShadow(j);
            if (shadow != null) {
                Util.offerLastTemporaryDirectBuffer(shadow);
            }
            vec.clearRefs(j);
        }
        if (!true) {
            for (j = 0; j < iov_len; j++) {
                shadow = vec.getShadow(j);
                if (shadow != null) {
                    Util.offerLastTemporaryDirectBuffer(shadow);
                }
                vec.clearRefs(j);
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
                Util.offerFirstTemporaryDirectBuffer(bb);
                return n;
            } catch (Throwable th) {
                Util.offerFirstTemporaryDirectBuffer(bb);
            }
        }
    }

    private static int readIntoNativeBuffer(FileDescriptor fd, ByteBuffer bb, long position, NativeDispatcher nd) throws IOException {
        int pos = bb.position();
        int lim = bb.limit();
        if (-assertionsDisabled || pos <= lim) {
            int rem = pos <= lim ? lim - pos : 0;
            if (rem == 0) {
                return 0;
            }
            int n;
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
        throw new AssertionError();
    }

    static long read(FileDescriptor fd, ByteBuffer[] bufs, NativeDispatcher nd) throws IOException {
        return read(fd, bufs, 0, bufs.length, nd);
    }

    static long read(FileDescriptor fd, ByteBuffer[] bufs, int offset, int length, NativeDispatcher nd) throws IOException {
        ByteBuffer buf;
        int rem;
        ByteBuffer shadow;
        int j;
        IOVecWrapper vec = IOVecWrapper.get(length);
        int iov_len = 0;
        int count = offset + length;
        int i = offset;
        while (i < count) {
            try {
                if (iov_len >= IOV_MAX) {
                    break;
                }
                buf = bufs[i];
                if (buf.isReadOnly()) {
                    throw new IllegalArgumentException("Read-only buffer");
                }
                int pos = buf.position();
                int lim = buf.limit();
                if (-assertionsDisabled || pos <= lim) {
                    rem = pos <= lim ? lim - pos : 0;
                    if (rem > 0) {
                        vec.setBuffer(iov_len, buf, pos, rem);
                        if (!(buf instanceof DirectBuffer)) {
                            shadow = Util.getTemporaryDirectBuffer(rem);
                            vec.setShadow(iov_len, shadow);
                            buf = shadow;
                            pos = shadow.position();
                        }
                        vec.putBase(iov_len, ((DirectBuffer) buf).address() + ((long) pos));
                        vec.putLen(iov_len, (long) rem);
                        iov_len++;
                    }
                    i++;
                } else {
                    throw new AssertionError();
                }
            } catch (Throwable th) {
                if (null == null) {
                    for (j = 0; j < iov_len; j++) {
                        shadow = vec.getShadow(j);
                        if (shadow != null) {
                            Util.offerLastTemporaryDirectBuffer(shadow);
                        }
                        vec.clearRefs(j);
                    }
                }
            }
        }
        if (iov_len == 0) {
            if (null == null) {
                for (j = 0; j < iov_len; j++) {
                    shadow = vec.getShadow(j);
                    if (shadow != null) {
                        Util.offerLastTemporaryDirectBuffer(shadow);
                    }
                    vec.clearRefs(j);
                }
            }
            return 0;
        }
        long bytesRead = nd.readv(fd, vec.address, iov_len);
        long left = bytesRead;
        for (j = 0; j < iov_len; j++) {
            shadow = vec.getShadow(j);
            if (left > 0) {
                buf = vec.getBuffer(j);
                rem = vec.getRemaining(j);
                int n = left > ((long) rem) ? rem : (int) left;
                if (shadow == null) {
                    buf.position(vec.getPosition(j) + n);
                } else {
                    shadow.limit(shadow.position() + n);
                    buf.put(shadow);
                }
                left -= (long) n;
            }
            if (shadow != null) {
                Util.offerLastTemporaryDirectBuffer(shadow);
            }
            vec.clearRefs(j);
        }
        if (!true) {
            for (j = 0; j < iov_len; j++) {
                shadow = vec.getShadow(j);
                if (shadow != null) {
                    Util.offerLastTemporaryDirectBuffer(shadow);
                }
                vec.clearRefs(j);
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
