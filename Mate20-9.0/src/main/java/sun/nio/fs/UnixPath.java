package sun.nio.fs;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.nio.file.FileSystemException;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.ProviderMismatchException;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Arrays;
import java.util.Objects;

class UnixPath extends AbstractPath {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static ThreadLocal<SoftReference<CharsetEncoder>> encoder = new ThreadLocal<>();
    private final UnixFileSystem fs;
    private int hash;
    private volatile int[] offsets;
    private final byte[] path;
    private volatile String stringValue;

    UnixPath(UnixFileSystem fs2, byte[] path2) {
        this.fs = fs2;
        this.path = path2;
    }

    UnixPath(UnixFileSystem fs2, String input) {
        this(fs2, encode(fs2, normalizeAndCheck(input)));
    }

    static String normalizeAndCheck(String input) {
        int n = input.length();
        char prevChar = 0;
        for (int i = 0; i < n; i++) {
            char c = input.charAt(i);
            if (c == '/' && prevChar == '/') {
                return normalize(input, n, i - 1);
            }
            checkNotNul(input, c);
            prevChar = c;
        }
        if (prevChar == '/') {
            return normalize(input, n, n - 1);
        }
        return input;
    }

    private static void checkNotNul(String input, char c) {
        if (c == 0) {
            throw new InvalidPathException(input, "Nul character not allowed");
        }
    }

    private static String normalize(String input, int len, int off) {
        if (len == 0) {
            return input;
        }
        int n = len;
        while (n > 0 && input.charAt(n - 1) == '/') {
            n--;
        }
        if (n == 0) {
            return "/";
        }
        StringBuilder sb = new StringBuilder(input.length());
        if (off > 0) {
            sb.append(input.substring(0, off));
        }
        char prevChar = 0;
        for (int i = off; i < n; i++) {
            char c = input.charAt(i);
            if (c != '/' || prevChar != '/') {
                checkNotNul(input, c);
                sb.append(c);
                prevChar = c;
            }
        }
        return sb.toString();
    }

    private static byte[] encode(UnixFileSystem fs2, String input) {
        boolean error;
        SoftReference<CharsetEncoder> ref = encoder.get();
        CharsetEncoder ce = ref != null ? ref.get() : null;
        if (ce == null) {
            ce = Util.jnuEncoding().newEncoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT);
            encoder.set(new SoftReference(ce));
        }
        char[] ca = fs2.normalizeNativePath(input.toCharArray());
        byte[] ba = new byte[((int) (((double) ca.length) * ((double) ce.maxBytesPerChar())))];
        ByteBuffer bb = ByteBuffer.wrap(ba);
        CharBuffer cb = CharBuffer.wrap(ca);
        ce.reset();
        if (!ce.encode(cb, bb, true).isUnderflow()) {
            error = true;
        } else {
            error = true ^ ce.flush(bb).isUnderflow();
        }
        if (!error) {
            int len = bb.position();
            if (len != ba.length) {
                return Arrays.copyOf(ba, len);
            }
            return ba;
        }
        throw new InvalidPathException(input, "Malformed input or input contains unmappable characters");
    }

    /* access modifiers changed from: package-private */
    public byte[] asByteArray() {
        return this.path;
    }

    /* access modifiers changed from: package-private */
    public byte[] getByteArrayForSysCalls() {
        if (getFileSystem().needToResolveAgainstDefaultDirectory()) {
            return resolve(getFileSystem().defaultDirectory(), this.path);
        }
        if (!isEmpty()) {
            return this.path;
        }
        return new byte[]{46};
    }

    /* access modifiers changed from: package-private */
    public String getPathForExceptionMessage() {
        return toString();
    }

    /* access modifiers changed from: package-private */
    public String getPathForPermissionCheck() {
        if (getFileSystem().needToResolveAgainstDefaultDirectory()) {
            return Util.toString(getByteArrayForSysCalls());
        }
        return toString();
    }

    static UnixPath toUnixPath(Path obj) {
        if (obj == null) {
            throw new NullPointerException();
        } else if (obj instanceof UnixPath) {
            return (UnixPath) obj;
        } else {
            throw new ProviderMismatchException();
        }
    }

    private void initOffsets() {
        int index;
        if (this.offsets == null) {
            int count = 0;
            if (isEmpty()) {
                count = 1;
            } else {
                for (int index2 = 0; index2 < this.path.length; index2 = index) {
                    index = index2 + 1;
                    if (this.path[index2] != 47) {
                        count++;
                        while (index < this.path.length && this.path[index] != 47) {
                            index++;
                        }
                    }
                }
            }
            int[] result = new int[count];
            int count2 = 0;
            int index3 = 0;
            while (index3 < this.path.length) {
                if (this.path[index3] == 47) {
                    index3++;
                } else {
                    int count3 = count2 + 1;
                    int index4 = index3 + 1;
                    result[count2] = index3;
                    while (index4 < this.path.length && this.path[index4] != 47) {
                        index4++;
                    }
                    count2 = count3;
                    index3 = index4;
                }
            }
            synchronized (this) {
                if (this.offsets == null) {
                    this.offsets = result;
                }
            }
        }
    }

    private boolean isEmpty() {
        return this.path.length == 0;
    }

    private UnixPath emptyPath() {
        return new UnixPath(getFileSystem(), new byte[0]);
    }

    public UnixFileSystem getFileSystem() {
        return this.fs;
    }

    public UnixPath getRoot() {
        if (this.path.length <= 0 || this.path[0] != 47) {
            return null;
        }
        return getFileSystem().rootDirectory();
    }

    public UnixPath getFileName() {
        initOffsets();
        int count = this.offsets.length;
        if (count == 0) {
            return null;
        }
        if (count == 1 && this.path.length > 0 && this.path[0] != 47) {
            return this;
        }
        int lastOffset = this.offsets[count - 1];
        int len = this.path.length - lastOffset;
        byte[] result = new byte[len];
        System.arraycopy(this.path, lastOffset, result, 0, len);
        return new UnixPath(getFileSystem(), result);
    }

    public UnixPath getParent() {
        initOffsets();
        if (this.offsets.length == 0) {
            return null;
        }
        int len = this.offsets[count - 1] - 1;
        if (len <= 0) {
            return getRoot();
        }
        byte[] result = new byte[len];
        System.arraycopy(this.path, 0, result, 0, len);
        return new UnixPath(getFileSystem(), result);
    }

    public int getNameCount() {
        initOffsets();
        return this.offsets.length;
    }

    public UnixPath getName(int index) {
        int len;
        initOffsets();
        if (index < 0) {
            throw new IllegalArgumentException();
        } else if (index < this.offsets.length) {
            int begin = this.offsets[index];
            if (index == this.offsets.length - 1) {
                len = this.path.length - begin;
            } else {
                len = (this.offsets[index + 1] - begin) - 1;
            }
            byte[] result = new byte[len];
            System.arraycopy(this.path, begin, result, 0, len);
            return new UnixPath(getFileSystem(), result);
        } else {
            throw new IllegalArgumentException();
        }
    }

    public UnixPath subpath(int beginIndex, int endIndex) {
        int len;
        initOffsets();
        if (beginIndex < 0) {
            throw new IllegalArgumentException();
        } else if (beginIndex >= this.offsets.length) {
            throw new IllegalArgumentException();
        } else if (endIndex > this.offsets.length) {
            throw new IllegalArgumentException();
        } else if (beginIndex < endIndex) {
            int begin = this.offsets[beginIndex];
            if (endIndex == this.offsets.length) {
                len = this.path.length - begin;
            } else {
                len = (this.offsets[endIndex] - begin) - 1;
            }
            byte[] result = new byte[len];
            System.arraycopy(this.path, begin, result, 0, len);
            return new UnixPath(getFileSystem(), result);
        } else {
            throw new IllegalArgumentException();
        }
    }

    public boolean isAbsolute() {
        return this.path.length > 0 && this.path[0] == 47;
    }

    private static byte[] resolve(byte[] base, byte[] child) {
        byte[] result;
        int baseLength = base.length;
        int childLength = child.length;
        if (childLength == 0) {
            return base;
        }
        if (baseLength == 0 || child[0] == 47) {
            return child;
        }
        if (baseLength == 1 && base[0] == 47) {
            result = new byte[(childLength + 1)];
            result[0] = 47;
            System.arraycopy(child, 0, result, 1, childLength);
        } else {
            result = new byte[(baseLength + 1 + childLength)];
            System.arraycopy(base, 0, result, 0, baseLength);
            result[base.length] = 47;
            System.arraycopy(child, 0, result, baseLength + 1, childLength);
        }
        return result;
    }

    public UnixPath resolve(Path obj) {
        byte[] other = toUnixPath(obj).path;
        if (other.length > 0 && other[0] == 47) {
            return (UnixPath) obj;
        }
        return new UnixPath(getFileSystem(), resolve(this.path, other));
    }

    /* access modifiers changed from: package-private */
    public UnixPath resolve(byte[] other) {
        return resolve((Path) new UnixPath(getFileSystem(), other));
    }

    public UnixPath relativize(Path obj) {
        int pos;
        UnixPath other = toUnixPath(obj);
        if (other.equals(this)) {
            return emptyPath();
        }
        if (isAbsolute() != other.isAbsolute()) {
            throw new IllegalArgumentException("'other' is different type of Path");
        } else if (isEmpty()) {
            return other;
        } else {
            int bn = getNameCount();
            int cn = other.getNameCount();
            int n = bn > cn ? cn : bn;
            int pos2 = 0;
            int i = 0;
            while (i < n && getName(i).equals(other.getName(i))) {
                i++;
            }
            int dotdots = bn - i;
            if (i < cn) {
                UnixPath remainder = other.subpath(i, cn);
                if (dotdots == 0) {
                    return remainder;
                }
                boolean isOtherEmpty = other.isEmpty();
                int len = (dotdots * 3) + remainder.path.length;
                if (isOtherEmpty) {
                    len--;
                }
                byte[] result = new byte[len];
                int pos3 = 0;
                for (int dotdots2 = dotdots; dotdots2 > 0; dotdots2--) {
                    int pos4 = pos3 + 1;
                    result[pos3] = 46;
                    pos3 = pos4 + 1;
                    result[pos4] = 46;
                    if (!isOtherEmpty) {
                        pos = pos3 + 1;
                        result[pos3] = 47;
                    } else if (dotdots2 > 1) {
                        pos = pos3 + 1;
                        result[pos3] = 47;
                    }
                    pos3 = pos;
                }
                System.arraycopy(remainder.path, 0, result, pos3, remainder.path.length);
                return new UnixPath(getFileSystem(), result);
            }
            byte[] result2 = new byte[((dotdots * 3) - 1)];
            while (dotdots > 0) {
                int pos5 = pos2 + 1;
                result2[pos2] = 46;
                pos2 = pos5 + 1;
                result2[pos5] = 46;
                if (dotdots > 1) {
                    result2[pos2] = 47;
                    pos2++;
                }
                dotdots--;
            }
            return new UnixPath(getFileSystem(), result2);
        }
    }

    public Path normalize() {
        int remaining;
        int len;
        int count = getNameCount();
        if (count == 0 || isEmpty()) {
            return this;
        }
        boolean[] ignore = new boolean[count];
        int[] size = new int[count];
        boolean hasDotDot = false;
        boolean isAbsolute = isAbsolute();
        int remaining2 = count;
        for (int i = 0; i < count; i++) {
            int begin = this.offsets[i];
            if (i == this.offsets.length - 1) {
                len = this.path.length - begin;
            } else {
                len = (this.offsets[i + 1] - begin) - 1;
            }
            size[i] = len;
            if (this.path[begin] == 46) {
                if (len == 1) {
                    ignore[i] = true;
                    remaining2--;
                } else if (this.path[begin + 1] == 46) {
                    hasDotDot = true;
                }
            }
        }
        if (hasDotDot) {
            while (true) {
                int prevRemaining = remaining2;
                int prevName = -1;
                remaining = remaining2;
                for (int i2 = 0; i2 < count; i2++) {
                    if (!ignore[i2]) {
                        if (size[i2] != 2) {
                            prevName = i2;
                        } else {
                            int begin2 = this.offsets[i2];
                            if (this.path[begin2] != 46 || this.path[begin2 + 1] != 46) {
                                prevName = i2;
                            } else if (prevName >= 0) {
                                ignore[prevName] = true;
                                ignore[i2] = true;
                                remaining -= 2;
                                prevName = -1;
                            } else if (isAbsolute) {
                                boolean hasPrevious = false;
                                int j = 0;
                                while (true) {
                                    if (j >= i2) {
                                        break;
                                    } else if (!ignore[j]) {
                                        hasPrevious = true;
                                        break;
                                    } else {
                                        j++;
                                    }
                                }
                                if (!hasPrevious) {
                                    ignore[i2] = true;
                                    remaining--;
                                }
                            }
                        }
                    }
                }
                if (prevRemaining <= remaining) {
                    break;
                }
                remaining2 = remaining;
            }
            remaining2 = remaining;
        }
        if (remaining2 == count) {
            return this;
        }
        if (remaining2 == 0) {
            return isAbsolute ? getFileSystem().rootDirectory() : emptyPath();
        }
        int len2 = remaining2 - 1;
        if (isAbsolute) {
            len2++;
        }
        int len3 = len2;
        for (int i3 = 0; i3 < count; i3++) {
            if (!ignore[i3]) {
                len3 += size[i3];
            }
        }
        byte[] result = new byte[len3];
        int pos = 0;
        if (isAbsolute) {
            result[0] = 47;
            pos = 0 + 1;
        }
        for (int i4 = 0; i4 < count; i4++) {
            if (!ignore[i4]) {
                System.arraycopy(this.path, this.offsets[i4], result, pos, size[i4]);
                pos += size[i4];
                remaining2--;
                if (remaining2 > 0) {
                    result[pos] = 47;
                    pos++;
                }
            }
        }
        return new UnixPath(getFileSystem(), result);
    }

    public boolean startsWith(Path other) {
        if (!(Objects.requireNonNull(other) instanceof UnixPath)) {
            return false;
        }
        UnixPath that = (UnixPath) other;
        if (that.path.length > this.path.length) {
            return false;
        }
        int thisOffsetCount = getNameCount();
        int thatOffsetCount = that.getNameCount();
        if (thatOffsetCount == 0 && isAbsolute()) {
            return !that.isEmpty();
        }
        if (thatOffsetCount > thisOffsetCount) {
            return false;
        }
        if (thatOffsetCount == thisOffsetCount && this.path.length != that.path.length) {
            return false;
        }
        for (int i = 0; i < thatOffsetCount; i++) {
            if (!Integer.valueOf(this.offsets[i]).equals(Integer.valueOf(that.offsets[i]))) {
                return false;
            }
        }
        int i2 = 0;
        while (i2 < that.path.length) {
            if (this.path[i2] != that.path[i2]) {
                return false;
            }
            i2++;
        }
        if (i2 >= this.path.length || this.path[i2] == 47) {
            return true;
        }
        return false;
    }

    public boolean endsWith(Path other) {
        if (!(Objects.requireNonNull(other) instanceof UnixPath)) {
            return false;
        }
        UnixPath that = (UnixPath) other;
        int thisLen = this.path.length;
        int thatLen = that.path.length;
        if (thatLen > thisLen) {
            return false;
        }
        if (thisLen > 0 && thatLen == 0) {
            return false;
        }
        if (that.isAbsolute() && !isAbsolute()) {
            return false;
        }
        int thisOffsetCount = getNameCount();
        int thatOffsetCount = that.getNameCount();
        if (thatOffsetCount > thisOffsetCount) {
            return false;
        }
        if (thatOffsetCount == thisOffsetCount) {
            if (thisOffsetCount == 0) {
                return true;
            }
            int expectedLen = thisLen;
            if (isAbsolute() && !that.isAbsolute()) {
                expectedLen--;
            }
            if (thatLen != expectedLen) {
                return false;
            }
        } else if (that.isAbsolute()) {
            return false;
        }
        int thisPos = this.offsets[thisOffsetCount - thatOffsetCount];
        int thatPos = that.offsets[0];
        if (thatLen - thatPos != thisLen - thisPos) {
            return false;
        }
        while (thatPos < thatLen) {
            int thisPos2 = thisPos + 1;
            int thatPos2 = thatPos + 1;
            if (this.path[thisPos] != that.path[thatPos]) {
                return false;
            }
            thisPos = thisPos2;
            thatPos = thatPos2;
        }
        return true;
    }

    public int compareTo(Path other) {
        int len1 = this.path.length;
        int len2 = ((UnixPath) other).path.length;
        int n = Math.min(len1, len2);
        byte[] v1 = this.path;
        byte[] v2 = ((UnixPath) other).path;
        for (int k = 0; k < n; k++) {
            int c1 = v1[k] & 255;
            int c2 = v2[k] & 255;
            if (c1 != c2) {
                return c1 - c2;
            }
        }
        return len1 - len2;
    }

    public boolean equals(Object ob) {
        boolean z = false;
        if (ob == null || !(ob instanceof UnixPath)) {
            return false;
        }
        if (compareTo((Path) ob) == 0) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        int h = this.hash;
        if (h == 0) {
            for (byte b : this.path) {
                h = (31 * h) + (b & Character.DIRECTIONALITY_UNDEFINED);
            }
            this.hash = h;
        }
        return h;
    }

    public String toString() {
        if (this.stringValue == null) {
            this.stringValue = this.fs.normalizeJavaPath(Util.toString(this.path));
        }
        return this.stringValue;
    }

    /* access modifiers changed from: package-private */
    public int openForAttributeAccess(boolean followLinks) throws IOException {
        int flags = UnixConstants.O_RDONLY;
        if (!followLinks) {
            if (UnixConstants.O_NOFOLLOW != 0) {
                flags |= UnixConstants.O_NOFOLLOW;
            } else {
                throw new IOException("NOFOLLOW_LINKS is not supported on this platform");
            }
        }
        try {
            return UnixNativeDispatcher.open(this, flags, 0);
        } catch (UnixException x) {
            if (getFileSystem().isSolaris() && x.errno() == UnixConstants.EINVAL) {
                x.setError(UnixConstants.ELOOP);
            }
            if (x.errno() != UnixConstants.ELOOP) {
                x.rethrowAsIOException(this);
                return -1;
            }
            String pathForExceptionMessage = getPathForExceptionMessage();
            throw new FileSystemException(pathForExceptionMessage, null, x.getMessage() + " or unable to access attributes of symbolic link");
        }
    }

    /* access modifiers changed from: package-private */
    public void checkRead() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkRead(getPathForPermissionCheck());
        }
    }

    /* access modifiers changed from: package-private */
    public void checkWrite() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkWrite(getPathForPermissionCheck());
        }
    }

    /* access modifiers changed from: package-private */
    public void checkDelete() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkDelete(getPathForPermissionCheck());
        }
    }

    public UnixPath toAbsolutePath() {
        if (isAbsolute()) {
            return this;
        }
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPropertyAccess("user.dir");
        }
        return new UnixPath(getFileSystem(), resolve(getFileSystem().defaultDirectory(), this.path));
    }

    public Path toRealPath(LinkOption... options) throws IOException {
        checkRead();
        UnixPath absolute = toAbsolutePath();
        if (Util.followLinks(options)) {
            try {
                return new UnixPath(getFileSystem(), UnixNativeDispatcher.realpath(absolute));
            } catch (UnixException x) {
                x.rethrowAsIOException(this);
            }
        }
        UnixPath result = this.fs.rootDirectory();
        for (int i = 0; i < absolute.getNameCount(); i++) {
            UnixPath element = absolute.getName(i);
            if (element.asByteArray().length != 1 || element.asByteArray()[0] != 46) {
                if (element.asByteArray().length == 2 && element.asByteArray()[0] == 46 && element.asByteArray()[1] == 46) {
                    UnixFileAttributes attrs = null;
                    try {
                        attrs = UnixFileAttributes.get(result, false);
                    } catch (UnixException x2) {
                        x2.rethrowAsIOException(result);
                    }
                    if (!attrs.isSymbolicLink()) {
                        result = result.getParent();
                        if (result == null) {
                            result = this.fs.rootDirectory();
                        }
                    }
                }
                result = result.resolve((Path) element);
            }
        }
        try {
            UnixFileAttributes.get(result, false);
        } catch (UnixException x3) {
            x3.rethrowAsIOException(result);
        }
        return result;
    }

    public URI toUri() {
        return UnixUriUtils.toUri(this);
    }

    public WatchKey register(WatchService watcher, WatchEvent.Kind<?>[] events, WatchEvent.Modifier... modifiers) throws IOException {
        if (watcher == null) {
            throw new NullPointerException();
        } else if (watcher instanceof AbstractWatchService) {
            checkRead();
            return ((AbstractWatchService) watcher).register(this, events, modifiers);
        } else {
            throw new ProviderMismatchException();
        }
    }
}
