package sun.nio.fs;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.FileSystemException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import sun.misc.Unsafe;
import sun.nio.ch.DirectBuffer;

class LinuxUserDefinedFileAttributeView extends AbstractUserDefinedFileAttributeView {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final String USER_NAMESPACE = "user.";
    private static final int XATTR_NAME_MAX = 255;
    private static final Unsafe unsafe = Unsafe.getUnsafe();
    private final UnixPath file;
    private final boolean followLinks;

    private byte[] nameAsBytes(UnixPath file2, String name) throws IOException {
        if (name != null) {
            byte[] bytes = Util.toBytes(USER_NAMESPACE + name);
            if (bytes.length <= XATTR_NAME_MAX) {
                return bytes;
            }
            throw new FileSystemException(file2.getPathForExceptionMessage(), null, "'" + name + "' is too big");
        }
        throw new NullPointerException("'name' is null");
    }

    private List<String> asList(long address, int size) {
        List<String> list = new ArrayList<>();
        int start = 0;
        for (int pos = 0; pos < size; pos++) {
            if (unsafe.getByte(((long) pos) + address) == 0) {
                int len = pos - start;
                byte[] value = new byte[len];
                for (int i = 0; i < len; i++) {
                    value[i] = unsafe.getByte(((long) start) + address + ((long) i));
                }
                String s = Util.toString(value);
                if (s.startsWith(USER_NAMESPACE)) {
                    list.add(s.substring(USER_NAMESPACE.length()));
                }
                start = pos + 1;
            }
        }
        return list;
    }

    LinuxUserDefinedFileAttributeView(UnixPath file2, boolean followLinks2) {
        this.file = file2;
        this.followLinks = followLinks2;
    }

    public List<String> list() throws IOException {
        if (System.getSecurityManager() != null) {
            checkAccess(this.file.getPathForPermissionCheck(), true, $assertionsDisabled);
        }
        int fd = this.file.openForAttributeAccess(this.followLinks);
        NativeBuffer buffer = null;
        int size = 1024;
        try {
            buffer = NativeBuffers.getNativeBuffer(1024);
            while (true) {
                List<String> unmodifiableList = Collections.unmodifiableList(asList(buffer.address(), LinuxNativeDispatcher.flistxattr(fd, buffer.address(), size)));
                if (buffer != null) {
                    buffer.release();
                }
                LinuxNativeDispatcher.close(fd);
                return unmodifiableList;
            }
        } catch (UnixException x) {
            if (x.errno() != UnixConstants.ERANGE || size >= 32768) {
                throw new FileSystemException(this.file.getPathForExceptionMessage(), null, "Unable to get list of extended attributes: " + x.getMessage());
            }
            buffer.release();
            size *= 2;
            buffer = NativeBuffers.getNativeBuffer(size);
        } catch (Throwable th) {
            if (buffer != null) {
                buffer.release();
            }
            LinuxNativeDispatcher.close(fd);
            throw th;
        }
        throw new FileSystemException(this.file.getPathForExceptionMessage(), null, "Unable to get list of extended attributes: " + x.getMessage());
    }

    public int size(String name) throws IOException {
        if (System.getSecurityManager() != null) {
            checkAccess(this.file.getPathForPermissionCheck(), true, $assertionsDisabled);
        }
        int fd = this.file.openForAttributeAccess(this.followLinks);
        try {
            int fgetxattr = LinuxNativeDispatcher.fgetxattr(fd, nameAsBytes(this.file, name), 0, 0);
            LinuxNativeDispatcher.close(fd);
            return fgetxattr;
        } catch (UnixException x) {
            String pathForExceptionMessage = this.file.getPathForExceptionMessage();
            throw new FileSystemException(pathForExceptionMessage, null, "Unable to get size of extended attribute '" + name + "': " + x.getMessage());
        } catch (Throwable th) {
            LinuxNativeDispatcher.close(fd);
            throw th;
        }
    }

    public int read(String name, ByteBuffer dst) throws IOException {
        NativeBuffer nb;
        long address;
        String str = name;
        ByteBuffer byteBuffer = dst;
        if (System.getSecurityManager() != null) {
            checkAccess(this.file.getPathForPermissionCheck(), true, $assertionsDisabled);
        }
        if (!dst.isReadOnly()) {
            int pos = dst.position();
            int lim = dst.limit();
            int rem = pos <= lim ? lim - pos : 0;
            if (byteBuffer instanceof DirectBuffer) {
                nb = null;
                address = ((DirectBuffer) byteBuffer).address() + ((long) pos);
            } else {
                nb = NativeBuffers.getNativeBuffer(rem);
                address = nb.address();
            }
            NativeBuffer nb2 = nb;
            int fd = this.file.openForAttributeAccess(this.followLinks);
            try {
                int n = LinuxNativeDispatcher.fgetxattr(fd, nameAsBytes(this.file, str), address, rem);
                if (rem != 0) {
                    if (nb2 != null) {
                        for (int i = 0; i < n; i++) {
                            byteBuffer.put(unsafe.getByte(((long) i) + address));
                        }
                    }
                    byteBuffer.position(pos + n);
                    LinuxNativeDispatcher.close(fd);
                    if (nb2 != null) {
                        nb2.release();
                    }
                    return n;
                } else if (n <= 0) {
                    LinuxNativeDispatcher.close(fd);
                    if (nb2 != null) {
                        nb2.release();
                    }
                    return 0;
                } else {
                    throw new UnixException(UnixConstants.ERANGE);
                }
            } catch (UnixException x) {
                String msg = x.errno() == UnixConstants.ERANGE ? "Insufficient space in buffer" : x.getMessage();
                String pathForExceptionMessage = this.file.getPathForExceptionMessage();
                throw new FileSystemException(pathForExceptionMessage, null, "Error reading extended attribute '" + str + "': " + msg);
            } catch (Throwable th) {
                if (nb2 != null) {
                    nb2.release();
                }
                throw th;
            }
        } else {
            throw new IllegalArgumentException("Read-only buffer");
        }
    }

    public int write(String name, ByteBuffer src) throws IOException {
        long address;
        NativeBuffer nb;
        int i = 0;
        if (System.getSecurityManager() != null) {
            checkAccess(this.file.getPathForPermissionCheck(), $assertionsDisabled, true);
        }
        int pos = src.position();
        int lim = src.limit();
        int rem = pos <= lim ? lim - pos : 0;
        if (src instanceof DirectBuffer) {
            nb = null;
            address = ((DirectBuffer) src).address() + ((long) pos);
        } else {
            NativeBuffer nb2 = NativeBuffers.getNativeBuffer(rem);
            long address2 = nb2.address();
            if (src.hasArray()) {
                while (i < rem) {
                    unsafe.putByte(((long) i) + address2, src.get());
                    i++;
                }
            } else {
                byte[] tmp = new byte[rem];
                src.get(tmp);
                src.position(pos);
                while (i < rem) {
                    unsafe.putByte(((long) i) + address2, tmp[i]);
                    i++;
                }
            }
            nb = nb2;
            address = address2;
        }
        int fd = this.file.openForAttributeAccess(this.followLinks);
        try {
            LinuxNativeDispatcher.fsetxattr(fd, nameAsBytes(this.file, name), address, rem);
            src.position(pos + rem);
            LinuxNativeDispatcher.close(fd);
            if (nb != null) {
                nb.release();
            }
            return rem;
        } catch (UnixException x) {
            String pathForExceptionMessage = this.file.getPathForExceptionMessage();
            throw new FileSystemException(pathForExceptionMessage, null, "Error writing extended attribute '" + name + "': " + x.getMessage());
        } catch (Throwable th) {
            if (nb != null) {
                nb.release();
            }
            throw th;
        }
    }

    public void delete(String name) throws IOException {
        if (System.getSecurityManager() != null) {
            checkAccess(this.file.getPathForPermissionCheck(), $assertionsDisabled, true);
        }
        int fd = this.file.openForAttributeAccess(this.followLinks);
        try {
            LinuxNativeDispatcher.fremovexattr(fd, nameAsBytes(this.file, name));
            LinuxNativeDispatcher.close(fd);
        } catch (UnixException x) {
            String pathForExceptionMessage = this.file.getPathForExceptionMessage();
            throw new FileSystemException(pathForExceptionMessage, null, "Unable to delete extended attribute '" + name + "': " + x.getMessage());
        } catch (Throwable th) {
            LinuxNativeDispatcher.close(fd);
            throw th;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x001f, code lost:
        if (r8 >= r3) goto L_0x0052;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0029, code lost:
        if (unsafe.getByte(((long) r8) + r4) != 0) goto L_0x004d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x002b, code lost:
        r9 = r8 - r7;
        r10 = new byte[r9];
        r0 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0031, code lost:
        if (r0 >= r9) goto L_0x0042;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0033, code lost:
        r10[r0] = unsafe.getByte((((long) r7) + r4) + ((long) r0));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x003f, code lost:
        r0 = r0 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:?, code lost:
        copyExtendedAttribute(r1, r10, r17);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x004d, code lost:
        r11 = r17;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0052, code lost:
        r11 = r17;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0054, code lost:
        if (r2 == null) goto L_0x0059;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0056, code lost:
        r2.release();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0059, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:6:0x0014, code lost:
        r3 = sun.nio.fs.LinuxNativeDispatcher.flistxattr(r1, r2.address(), r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:?, code lost:
        r4 = r2.address();
        r7 = 0;
        r0 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x001e, code lost:
        r8 = r0;
     */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x0078  */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x0083  */
    static void copyExtendedAttributes(int ofd, int nfd) {
        int pos;
        int i = ofd;
        NativeBuffer buffer = null;
        try {
            buffer = NativeBuffers.getNativeBuffer(1024);
            int size = 1024;
            while (true) {
                try {
                    break;
                } catch (UnixException x) {
                    int i2 = nfd;
                    if (x.errno() != UnixConstants.ERANGE || size >= 32768) {
                        if (buffer != null) {
                        }
                        return;
                    }
                    buffer.release();
                    size *= 2;
                    buffer = NativeBuffers.getNativeBuffer(size);
                } catch (Throwable th) {
                    x = th;
                    if (buffer != null) {
                    }
                    throw x;
                }
            }
            if (buffer != null) {
                buffer.release();
            }
            return;
            int start = pos + 1;
            int start2 = pos + 1;
        } catch (Throwable th2) {
            x = th2;
            int i3 = nfd;
            if (buffer != null) {
                buffer.release();
            }
            throw x;
        }
    }

    private static void copyExtendedAttribute(int ofd, byte[] name, int nfd) throws UnixException {
        int size = LinuxNativeDispatcher.fgetxattr(ofd, name, 0, 0);
        NativeBuffer buffer = NativeBuffers.getNativeBuffer(size);
        try {
            long address = buffer.address();
            LinuxNativeDispatcher.fsetxattr(nfd, name, address, LinuxNativeDispatcher.fgetxattr(ofd, name, address, size));
        } finally {
            buffer.release();
        }
    }
}
