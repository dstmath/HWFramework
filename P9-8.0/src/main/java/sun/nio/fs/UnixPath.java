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
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Arrays;
import java.util.Objects;

class UnixPath extends AbstractPath {
    static final /* synthetic */ boolean -assertionsDisabled = (UnixPath.class.desiredAssertionStatus() ^ 1);
    private static ThreadLocal<SoftReference<CharsetEncoder>> encoder = new ThreadLocal();
    private final UnixFileSystem fs;
    private int hash;
    private volatile int[] offsets;
    private final byte[] path;
    private volatile String stringValue;

    UnixPath(UnixFileSystem fs, byte[] path) {
        this.fs = fs;
        this.path = path;
    }

    UnixPath(UnixFileSystem fs, String input) {
        this(fs, encode(fs, normalizeAndCheck(input)));
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
        return sb.-java_util_stream_Collectors-mthref-7();
    }

    private static byte[] encode(UnixFileSystem fs, String input) {
        boolean error;
        SoftReference<CharsetEncoder> ref = (SoftReference) encoder.get();
        CharsetEncoder ce = ref != null ? (CharsetEncoder) ref.get() : null;
        if (ce == null) {
            ce = Util.jnuEncoding().newEncoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT);
            encoder.set(new SoftReference(ce));
        }
        char[] ca = fs.normalizeNativePath(input.toCharArray());
        byte[] ba = new byte[((int) (((double) ca.length) * ((double) ce.maxBytesPerChar())))];
        ByteBuffer bb = ByteBuffer.wrap(ba);
        CharBuffer cb = CharBuffer.wrap(ca);
        ce.reset();
        if (ce.encode(cb, bb, true).isUnderflow()) {
            error = ce.flush(bb).isUnderflow() ^ 1;
        } else {
            error = true;
        }
        if (error) {
            throw new InvalidPathException(input, "Malformed input or input contains unmappable characters");
        }
        int len = bb.position();
        if (len != ba.length) {
            return Arrays.copyOf(ba, len);
        }
        return ba;
    }

    byte[] asByteArray() {
        return this.path;
    }

    byte[] getByteArrayForSysCalls() {
        if (getFileSystem().needToResolveAgainstDefaultDirectory()) {
            return resolve(getFileSystem().defaultDirectory(), this.path);
        }
        if (!isEmpty()) {
            return this.path;
        }
        return new byte[]{(byte) 46};
    }

    String getPathForExceptionMessage() {
        return toString();
    }

    String getPathForPermissionCheck() {
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
        if (this.offsets == null) {
            int index;
            int count = 0;
            int index2 = 0;
            if (isEmpty()) {
                count = 1;
            } else {
                while (index2 < this.path.length) {
                    index = index2 + 1;
                    if (this.path[index2] != (byte) 47) {
                        count++;
                        index2 = index;
                        while (index2 < this.path.length && this.path[index2] != (byte) 47) {
                            index2++;
                        }
                    } else {
                        index2 = index;
                    }
                }
            }
            int[] result = new int[count];
            count = 0;
            index2 = 0;
            while (index2 < this.path.length) {
                if (this.path[index2] == (byte) 47) {
                    index2++;
                } else {
                    int count2 = count + 1;
                    index = index2 + 1;
                    result[count] = index2;
                    index2 = index;
                    while (index2 < this.path.length && this.path[index2] != (byte) 47) {
                        index2++;
                    }
                    count = count2;
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
        if (this.path.length <= 0 || this.path[0] != (byte) 47) {
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
        if (count == 1 && this.path.length > 0 && this.path[0] != (byte) 47) {
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
        int count = this.offsets.length;
        if (count == 0) {
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
        initOffsets();
        if (index < 0) {
            throw new IllegalArgumentException();
        } else if (index >= this.offsets.length) {
            throw new IllegalArgumentException();
        } else {
            int len;
            int begin = this.offsets[index];
            if (index == this.offsets.length - 1) {
                len = this.path.length - begin;
            } else {
                len = (this.offsets[index + 1] - begin) - 1;
            }
            byte[] result = new byte[len];
            System.arraycopy(this.path, begin, result, 0, len);
            return new UnixPath(getFileSystem(), result);
        }
    }

    public UnixPath subpath(int beginIndex, int endIndex) {
        initOffsets();
        if (beginIndex < 0) {
            throw new IllegalArgumentException();
        } else if (beginIndex >= this.offsets.length) {
            throw new IllegalArgumentException();
        } else if (endIndex > this.offsets.length) {
            throw new IllegalArgumentException();
        } else if (beginIndex >= endIndex) {
            throw new IllegalArgumentException();
        } else {
            int len;
            int begin = this.offsets[beginIndex];
            if (endIndex == this.offsets.length) {
                len = this.path.length - begin;
            } else {
                len = (this.offsets[endIndex] - begin) - 1;
            }
            byte[] result = new byte[len];
            System.arraycopy(this.path, begin, result, 0, len);
            return new UnixPath(getFileSystem(), result);
        }
    }

    public boolean isAbsolute() {
        return this.path.length > 0 && this.path[0] == (byte) 47;
    }

    private static byte[] resolve(byte[] base, byte[] child) {
        int baseLength = base.length;
        int childLength = child.length;
        if (childLength == 0) {
            return base;
        }
        if (baseLength == 0 || child[0] == (byte) 47) {
            return child;
        }
        byte[] result;
        if (baseLength == 1 && base[0] == (byte) 47) {
            result = new byte[(childLength + 1)];
            result[0] = (byte) 47;
            System.arraycopy(child, 0, result, 1, childLength);
        } else {
            result = new byte[((baseLength + 1) + childLength)];
            System.arraycopy(base, 0, result, 0, baseLength);
            result[base.length] = (byte) 47;
            System.arraycopy(child, 0, result, baseLength + 1, childLength);
        }
        return result;
    }

    public UnixPath resolve(Path obj) {
        byte[] other = toUnixPath(obj).path;
        if (other.length > 0 && other[0] == (byte) 47) {
            return (UnixPath) obj;
        }
        return new UnixPath(getFileSystem(), resolve(this.path, other));
    }

    UnixPath resolve(byte[] other) {
        return resolve(new UnixPath(getFileSystem(), other));
    }

    public UnixPath relativize(Path obj) {
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
            int i = 0;
            while (i < n && getName(i).equals(other.getName(i))) {
                i++;
            }
            int dotdots = bn - i;
            byte[] result;
            int pos;
            int i2;
            if (i < cn) {
                UnixPath remainder = other.subpath(i, cn);
                if (dotdots == 0) {
                    return remainder;
                }
                boolean isOtherEmpty = other.isEmpty();
                int len = (dotdots * 3) + remainder.path.length;
                if (isOtherEmpty) {
                    if (-assertionsDisabled || remainder.isEmpty()) {
                        len--;
                    } else {
                        throw new AssertionError();
                    }
                }
                result = new byte[len];
                pos = 0;
                while (dotdots > 0) {
                    i2 = pos + 1;
                    result[pos] = (byte) 46;
                    pos = i2 + 1;
                    result[i2] = (byte) 46;
                    if (!isOtherEmpty) {
                        i2 = pos + 1;
                        result[pos] = (byte) 47;
                    } else if (dotdots > 1) {
                        i2 = pos + 1;
                        result[pos] = (byte) 47;
                    } else {
                        i2 = pos;
                    }
                    dotdots--;
                    pos = i2;
                }
                System.arraycopy(remainder.path, 0, result, pos, remainder.path.length);
                return new UnixPath(getFileSystem(), result);
            }
            result = new byte[((dotdots * 3) - 1)];
            pos = 0;
            while (dotdots > 0) {
                i2 = pos + 1;
                result[pos] = (byte) 46;
                pos = i2 + 1;
                result[i2] = (byte) 46;
                if (dotdots > 1) {
                    i2 = pos + 1;
                    result[pos] = (byte) 47;
                } else {
                    i2 = pos;
                }
                dotdots--;
                pos = i2;
            }
            return new UnixPath(getFileSystem(), result);
        }
    }

    public Path normalize() {
        int count = getNameCount();
        if (count == 0 || isEmpty()) {
            return this;
        }
        int i;
        int begin;
        int len;
        boolean[] ignore = new boolean[count];
        int[] size = new int[count];
        int remaining = count;
        boolean hasDotDot = false;
        boolean isAbsolute = isAbsolute();
        for (i = 0; i < count; i++) {
            begin = this.offsets[i];
            if (i == this.offsets.length - 1) {
                len = this.path.length - begin;
            } else {
                len = (this.offsets[i + 1] - begin) - 1;
            }
            size[i] = len;
            if (this.path[begin] == (byte) 46) {
                if (len == 1) {
                    ignore[i] = true;
                    remaining--;
                } else if (this.path[begin + 1] == (byte) 46) {
                    hasDotDot = true;
                }
            }
        }
        if (hasDotDot) {
            int prevRemaining;
            do {
                prevRemaining = remaining;
                int prevName = -1;
                for (i = 0; i < count; i++) {
                    if (!ignore[i]) {
                        if (size[i] != 2) {
                            prevName = i;
                        } else {
                            begin = this.offsets[i];
                            if (this.path[begin] != (byte) 46 || this.path[begin + 1] != (byte) 46) {
                                prevName = i;
                            } else if (prevName >= 0) {
                                ignore[prevName] = true;
                                ignore[i] = true;
                                remaining -= 2;
                                prevName = -1;
                            } else if (isAbsolute) {
                                boolean hasPrevious = false;
                                for (int j = 0; j < i; j++) {
                                    if (!ignore[j]) {
                                        hasPrevious = true;
                                        break;
                                    }
                                }
                                if (!hasPrevious) {
                                    ignore[i] = true;
                                    remaining--;
                                }
                            }
                        }
                    }
                }
            } while (prevRemaining > remaining);
        }
        if (remaining == count) {
            return this;
        }
        if (remaining == 0) {
            return isAbsolute ? getFileSystem().rootDirectory() : emptyPath();
        }
        len = remaining - 1;
        if (isAbsolute) {
            len++;
        }
        for (i = 0; i < count; i++) {
            if (!ignore[i]) {
                len += size[i];
            }
        }
        byte[] result = new byte[len];
        int pos = 0;
        if (isAbsolute) {
            pos = 1;
            result[0] = (byte) 47;
        }
        for (i = 0; i < count; i++) {
            if (!ignore[i]) {
                System.arraycopy(this.path, this.offsets[i], result, pos, size[i]);
                pos += size[i];
                remaining--;
                if (remaining > 0) {
                    int pos2 = pos + 1;
                    result[pos] = (byte) 47;
                    pos = pos2;
                }
            }
        }
        return new UnixPath(getFileSystem(), result);
    }

    public boolean startsWith(Path other) {
        boolean z = false;
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
            if (!that.isEmpty()) {
                z = true;
            }
            return z;
        } else if (thatOffsetCount > thisOffsetCount) {
            return false;
        } else {
            if (thatOffsetCount == thisOffsetCount && this.path.length != that.path.length) {
                return false;
            }
            int i;
            for (i = 0; i < thatOffsetCount; i++) {
                if (!Integer.valueOf(this.offsets[i]).equals(Integer.valueOf(that.offsets[i]))) {
                    return false;
                }
            }
            i = 0;
            while (i < that.path.length) {
                if (this.path[i] != that.path[i]) {
                    return false;
                }
                i++;
            }
            return i >= this.path.length || this.path[i] == (byte) 47;
        }
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
        if (that.isAbsolute() && (isAbsolute() ^ 1) != 0) {
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
            if (isAbsolute() && (that.isAbsolute() ^ 1) != 0) {
                expectedLen = thisLen - 1;
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
        int thisPos2;
        int thatPos2;
        do {
            thatPos2 = thatPos;
            thisPos2 = thisPos;
            if (thatPos2 >= thatLen) {
                return true;
            }
            thisPos = thisPos2 + 1;
            thatPos = thatPos2 + 1;
        } while (this.path[thisPos2] == that.path[thatPos2]);
        return false;
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
                h = (h * 31) + (b & 255);
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

    int openForAttributeAccess(boolean followLinks) throws IOException {
        int flags = UnixConstants.O_RDONLY;
        if (!followLinks) {
            if (UnixConstants.O_NOFOLLOW == 0) {
                throw new IOException("NOFOLLOW_LINKS is not supported on this platform");
            }
            flags |= UnixConstants.O_NOFOLLOW;
        }
        try {
            return UnixNativeDispatcher.open(this, flags, 0);
        } catch (UnixException x) {
            if (getFileSystem().isSolaris() && x.errno() == UnixConstants.EINVAL) {
                x.setError(UnixConstants.ELOOP);
            }
            if (x.errno() == UnixConstants.ELOOP) {
                throw new FileSystemException(getPathForExceptionMessage(), null, x.getMessage() + " or unable to access attributes of symbolic link");
            }
            x.rethrowAsIOException(this);
            return -1;
        }
    }

    void checkRead() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkRead(getPathForPermissionCheck());
        }
    }

    void checkWrite() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkWrite(getPathForPermissionCheck());
        }
    }

    void checkDelete() {
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
            Path element = absolute.getName(i);
            if (element.asByteArray().length != 1 || element.asByteArray()[0] != (byte) 46) {
                if (element.asByteArray().length == 2 && element.asByteArray()[0] == (byte) 46 && element.asByteArray()[1] == (byte) 46) {
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
                result = result.resolve(element);
            }
        }
        try {
            UnixFileAttributes.get(result, false);
        } catch (UnixException x22) {
            x22.rethrowAsIOException(result);
        }
        return result;
    }

    public URI toUri() {
        return UnixUriUtils.toUri(this);
    }

    public WatchKey register(WatchService watcher, Kind<?>[] events, Modifier... modifiers) throws IOException {
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
