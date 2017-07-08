package sun.nio.ch;

import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.ByteBuffer;

class IOUtil {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    static final int IOV_MAX = 0;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.nio.ch.IOUtil.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.nio.ch.IOUtil.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.nio.ch.IOUtil.<clinit>():void");
    }

    static native void configureBlocking(FileDescriptor fileDescriptor, boolean z) throws IOException;

    static native boolean drain(int i) throws IOException;

    static native int fdLimit();

    static native int fdVal(FileDescriptor fileDescriptor);

    static native int iovMax();

    static native long makePipe(boolean z);

    static native boolean randomBytes(byte[] bArr);

    static native void setfdVal(FileDescriptor fileDescriptor, int i);

    private IOUtil() {
    }

    static int write(FileDescriptor fd, ByteBuffer src, long position, NativeDispatcher nd) throws IOException {
        int rem = 0;
        if (src instanceof DirectBuffer) {
            return writeFromNativeBuffer(fd, src, position, nd);
        }
        int pos = src.position();
        int lim = src.limit();
        if (!-assertionsDisabled) {
            if ((pos <= lim ? 1 : 0) == 0) {
                throw new AssertionError();
            }
        }
        if (pos <= lim) {
            rem = lim - pos;
        }
        ByteBuffer bb = Util.getTemporaryDirectBuffer(rem);
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
    }

    private static int writeFromNativeBuffer(FileDescriptor fd, ByteBuffer bb, long position, NativeDispatcher nd) throws IOException {
        int rem;
        int pos = bb.position();
        int lim = bb.limit();
        if (!-assertionsDisabled) {
            if ((pos <= lim ? 1 : 0) == 0) {
                throw new AssertionError();
            }
        }
        if (pos <= lim) {
            rem = lim - pos;
        } else {
            rem = 0;
        }
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

    static long write(FileDescriptor fd, ByteBuffer[] bufs, NativeDispatcher nd) throws IOException {
        return write(fd, bufs, 0, bufs.length, nd);
    }

    static long write(FileDescriptor fd, ByteBuffer[] bufs, int offset, int length, NativeDispatcher nd) throws IOException {
        ByteBuffer shadow;
        int j;
        IOVecWrapper vec = IOVecWrapper.get(length);
        int iov_len = 0;
        int count = offset + length;
        for (int i = offset; i < count; i++) {
            if (iov_len >= IOV_MAX) {
                break;
            }
            ByteBuffer buf = bufs[i];
            int pos = buf.position();
            int lim = buf.limit();
            if (!-assertionsDisabled) {
                Object obj;
                if (pos <= lim) {
                    obj = 1;
                } else {
                    obj = null;
                }
                if (obj == null) {
                    throw new AssertionError();
                }
            }
            int rem = pos <= lim ? lim - pos : 0;
            if (rem > 0) {
                try {
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
        int rem;
        int pos = bb.position();
        int lim = bb.limit();
        if (!-assertionsDisabled) {
            if ((pos <= lim ? 1 : 0) == 0) {
                throw new AssertionError();
            }
        }
        if (pos <= lim) {
            rem = lim - pos;
        } else {
            rem = 0;
        }
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

    static long read(FileDescriptor fd, ByteBuffer[] bufs, NativeDispatcher nd) throws IOException {
        return read(fd, bufs, 0, bufs.length, nd);
    }

    static long read(FileDescriptor fd, ByteBuffer[] bufs, int offset, int length, NativeDispatcher nd) throws IOException {
        ByteBuffer buf;
        ByteBuffer shadow;
        int j;
        IOVecWrapper vec = IOVecWrapper.get(length);
        int iov_len = 0;
        int count = offset + length;
        int i = offset;
        while (i < count) {
            int rem;
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
                if (!-assertionsDisabled) {
                    if ((pos <= lim ? 1 : null) == null) {
                        throw new AssertionError();
                    }
                }
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

    static FileDescriptor newFD(int i) {
        FileDescriptor fd = new FileDescriptor();
        setfdVal(fd, i);
        return fd;
    }
}
