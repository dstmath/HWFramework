package java.nio.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.FileTreeWalker;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.spi.FileSystemProvider;
import java.nio.file.spi.FileTypeDetector;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.Spliterators;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import sun.nio.fs.DefaultFileTypeDetector;

public final class Files {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final int BUFFER_SIZE = 8192;
    private static final int MAX_BUFFER_SIZE = 2147483639;

    private static class AcceptAllFilter implements DirectoryStream.Filter<Path> {
        static final AcceptAllFilter FILTER = new AcceptAllFilter();

        private AcceptAllFilter() {
        }

        public boolean accept(Path entry) {
            return true;
        }
    }

    private static class FileTypeDetectors {
        static final FileTypeDetector defaultFileTypeDetector = createDefaultFileTypeDetector();
        static final List<FileTypeDetector> installeDetectors = loadInstalledDetectors();

        private FileTypeDetectors() {
        }

        private static FileTypeDetector createDefaultFileTypeDetector() {
            return (FileTypeDetector) AccessController.doPrivileged(new PrivilegedAction<FileTypeDetector>() {
                public FileTypeDetector run() {
                    return DefaultFileTypeDetector.create();
                }
            });
        }

        private static List<FileTypeDetector> loadInstalledDetectors() {
            return (List) AccessController.doPrivileged(new PrivilegedAction<List<FileTypeDetector>>() {
                public List<FileTypeDetector> run() {
                    List<FileTypeDetector> list = new ArrayList<>();
                    Iterator<FileTypeDetector> it = ServiceLoader.load(FileTypeDetector.class, ClassLoader.getSystemClassLoader()).iterator();
                    while (it.hasNext()) {
                        list.add(it.next());
                    }
                    return list;
                }
            });
        }
    }

    private Files() {
    }

    private static FileSystemProvider provider(Path path) {
        return path.getFileSystem().provider();
    }

    private static Runnable asUncheckedRunnable(Closeable c) {
        return new Runnable() {
            public final void run() {
                Files.lambda$asUncheckedRunnable$0(Closeable.this);
            }
        };
    }

    static /* synthetic */ void lambda$asUncheckedRunnable$0(Closeable c) {
        try {
            c.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static InputStream newInputStream(Path path, OpenOption... options) throws IOException {
        return provider(path).newInputStream(path, options);
    }

    public static OutputStream newOutputStream(Path path, OpenOption... options) throws IOException {
        return provider(path).newOutputStream(path, options);
    }

    public static SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
        return provider(path).newByteChannel(path, options, attrs);
    }

    public static SeekableByteChannel newByteChannel(Path path, OpenOption... options) throws IOException {
        Set<OpenOption> set = new HashSet<>(options.length);
        Collections.addAll(set, options);
        return newByteChannel(path, set, new FileAttribute[0]);
    }

    public static DirectoryStream<Path> newDirectoryStream(Path dir) throws IOException {
        return provider(dir).newDirectoryStream(dir, AcceptAllFilter.FILTER);
    }

    public static DirectoryStream<Path> newDirectoryStream(Path dir, String glob) throws IOException {
        if (glob.equals("*")) {
            return newDirectoryStream(dir);
        }
        FileSystem fs = dir.getFileSystem();
        final PathMatcher matcher = fs.getPathMatcher("glob:" + glob);
        return fs.provider().newDirectoryStream(dir, new DirectoryStream.Filter<Path>() {
            public boolean accept(Path entry) {
                return PathMatcher.this.matches(entry.getFileName());
            }
        });
    }

    public static DirectoryStream<Path> newDirectoryStream(Path dir, DirectoryStream.Filter<? super Path> filter) throws IOException {
        return provider(dir).newDirectoryStream(dir, filter);
    }

    public static Path createFile(Path path, FileAttribute<?>... attrs) throws IOException {
        newByteChannel(path, EnumSet.of(StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE), attrs).close();
        return path;
    }

    public static Path createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException {
        provider(dir).createDirectory(dir, attrs);
        return dir;
    }

    public static Path createDirectories(Path dir, FileAttribute<?>... attrs) throws IOException {
        try {
            createAndCheckIsDirectory(dir, attrs);
            return dir;
        } catch (FileAlreadyExistsException x) {
            throw x;
        } catch (IOException e) {
            SecurityException se = null;
            try {
                dir = dir.toAbsolutePath();
            } catch (SecurityException x2) {
                se = x2;
            }
            Path parent = dir.getParent();
            while (parent != null) {
                try {
                    provider(parent).checkAccess(parent, new AccessMode[0]);
                    break;
                } catch (NoSuchFileException e2) {
                    parent = parent.getParent();
                }
            }
            if (parent != null) {
                Path child = parent;
                for (Path name : parent.relativize(dir)) {
                    child = child.resolve(name);
                    createAndCheckIsDirectory(child, attrs);
                }
                return dir;
            } else if (se == null) {
                throw new FileSystemException(dir.toString(), null, "Unable to determine if root directory exists");
            } else {
                throw se;
            }
        }
    }

    private static void createAndCheckIsDirectory(Path dir, FileAttribute<?>... attrs) throws IOException {
        try {
            createDirectory(dir, attrs);
        } catch (FileAlreadyExistsException x) {
            if (!isDirectory(dir, LinkOption.NOFOLLOW_LINKS)) {
                throw x;
            }
        }
    }

    public static Path createTempFile(Path dir, String prefix, String suffix, FileAttribute<?>... attrs) throws IOException {
        return TempFileHelper.createTempFile((Path) Objects.requireNonNull(dir), prefix, suffix, attrs);
    }

    public static Path createTempFile(String prefix, String suffix, FileAttribute<?>... attrs) throws IOException {
        return TempFileHelper.createTempFile(null, prefix, suffix, attrs);
    }

    public static Path createTempDirectory(Path dir, String prefix, FileAttribute<?>... attrs) throws IOException {
        return TempFileHelper.createTempDirectory((Path) Objects.requireNonNull(dir), prefix, attrs);
    }

    public static Path createTempDirectory(String prefix, FileAttribute<?>... attrs) throws IOException {
        return TempFileHelper.createTempDirectory(null, prefix, attrs);
    }

    public static Path createSymbolicLink(Path link, Path target, FileAttribute<?>... attrs) throws IOException {
        provider(link).createSymbolicLink(link, target, attrs);
        return link;
    }

    public static Path createLink(Path link, Path existing) throws IOException {
        provider(link).createLink(link, existing);
        return link;
    }

    public static void delete(Path path) throws IOException {
        provider(path).delete(path);
    }

    public static boolean deleteIfExists(Path path) throws IOException {
        return provider(path).deleteIfExists(path);
    }

    public static Path copy(Path source, Path target, CopyOption... options) throws IOException {
        FileSystemProvider provider = provider(source);
        if (provider(target) == provider) {
            provider.copy(source, target, options);
        } else {
            CopyMoveHelper.copyToForeignTarget(source, target, options);
        }
        return target;
    }

    public static Path move(Path source, Path target, CopyOption... options) throws IOException {
        FileSystemProvider provider = provider(source);
        if (provider(target) == provider) {
            provider.move(source, target, options);
        } else {
            CopyMoveHelper.moveToForeignTarget(source, target, options);
        }
        return target;
    }

    public static Path readSymbolicLink(Path link) throws IOException {
        return provider(link).readSymbolicLink(link);
    }

    public static FileStore getFileStore(Path path) throws IOException {
        return provider(path).getFileStore(path);
    }

    public static boolean isSameFile(Path path, Path path2) throws IOException {
        return provider(path).isSameFile(path, path2);
    }

    public static boolean isHidden(Path path) throws IOException {
        return provider(path).isHidden(path);
    }

    public static String probeContentType(Path path) throws IOException {
        for (FileTypeDetector detector : FileTypeDetectors.installeDetectors) {
            String result = detector.probeContentType(path);
            if (result != null) {
                return result;
            }
        }
        return FileTypeDetectors.defaultFileTypeDetector.probeContentType(path);
    }

    public static <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options) {
        return provider(path).getFileAttributeView(path, type, options);
    }

    public static <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options) throws IOException {
        return provider(path).readAttributes(path, type, options);
    }

    public static Path setAttribute(Path path, String attribute, Object value, LinkOption... options) throws IOException {
        provider(path).setAttribute(path, attribute, value, options);
        return path;
    }

    public static Object getAttribute(Path path, String attribute, LinkOption... options) throws IOException {
        String name;
        if (attribute.indexOf(42) >= 0 || attribute.indexOf(44) >= 0) {
            throw new IllegalArgumentException(attribute);
        }
        Map<String, Object> map = readAttributes(path, attribute, options);
        int pos = attribute.indexOf(58);
        if (pos == -1) {
            name = attribute;
        } else {
            name = pos == attribute.length() ? "" : attribute.substring(pos + 1);
        }
        return map.get(name);
    }

    public static Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) throws IOException {
        return provider(path).readAttributes(path, attributes, options);
    }

    public static Set<PosixFilePermission> getPosixFilePermissions(Path path, LinkOption... options) throws IOException {
        return ((PosixFileAttributes) readAttributes(path, PosixFileAttributes.class, options)).permissions();
    }

    public static Path setPosixFilePermissions(Path path, Set<PosixFilePermission> perms) throws IOException {
        PosixFileAttributeView view = (PosixFileAttributeView) getFileAttributeView(path, PosixFileAttributeView.class, new LinkOption[0]);
        if (view != null) {
            view.setPermissions(perms);
            return path;
        }
        throw new UnsupportedOperationException();
    }

    public static UserPrincipal getOwner(Path path, LinkOption... options) throws IOException {
        FileOwnerAttributeView view = (FileOwnerAttributeView) getFileAttributeView(path, FileOwnerAttributeView.class, options);
        if (view != null) {
            return view.getOwner();
        }
        throw new UnsupportedOperationException();
    }

    public static Path setOwner(Path path, UserPrincipal owner) throws IOException {
        FileOwnerAttributeView view = (FileOwnerAttributeView) getFileAttributeView(path, FileOwnerAttributeView.class, new LinkOption[0]);
        if (view != null) {
            view.setOwner(owner);
            return path;
        }
        throw new UnsupportedOperationException();
    }

    public static boolean isSymbolicLink(Path path) {
        try {
            return readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS).isSymbolicLink();
        } catch (IOException e) {
            return $assertionsDisabled;
        }
    }

    public static boolean isDirectory(Path path, LinkOption... options) {
        try {
            return readAttributes(path, BasicFileAttributes.class, options).isDirectory();
        } catch (IOException e) {
            return $assertionsDisabled;
        }
    }

    public static boolean isRegularFile(Path path, LinkOption... options) {
        try {
            return readAttributes(path, BasicFileAttributes.class, options).isRegularFile();
        } catch (IOException e) {
            return $assertionsDisabled;
        }
    }

    public static FileTime getLastModifiedTime(Path path, LinkOption... options) throws IOException {
        return readAttributes(path, BasicFileAttributes.class, options).lastModifiedTime();
    }

    public static Path setLastModifiedTime(Path path, FileTime time) throws IOException {
        ((BasicFileAttributeView) getFileAttributeView(path, BasicFileAttributeView.class, new LinkOption[0])).setTimes(time, null, null);
        return path;
    }

    public static long size(Path path) throws IOException {
        return readAttributes(path, BasicFileAttributes.class, new LinkOption[0]).size();
    }

    private static boolean followLinks(LinkOption... options) {
        boolean followLinks = true;
        int length = options.length;
        int i = 0;
        while (i < length) {
            LinkOption opt = options[i];
            if (opt == LinkOption.NOFOLLOW_LINKS) {
                followLinks = $assertionsDisabled;
                i++;
            } else if (opt == null) {
                throw new NullPointerException();
            } else {
                throw new AssertionError((Object) "Should not get here");
            }
        }
        return followLinks;
    }

    public static boolean exists(Path path, LinkOption... options) {
        try {
            if (followLinks(options)) {
                provider(path).checkAccess(path, new AccessMode[0]);
            } else {
                readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
            }
            return true;
        } catch (IOException e) {
            return $assertionsDisabled;
        }
    }

    public static boolean notExists(Path path, LinkOption... options) {
        try {
            if (followLinks(options)) {
                provider(path).checkAccess(path, new AccessMode[0]);
            } else {
                readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
            }
            return $assertionsDisabled;
        } catch (NoSuchFileException e) {
            return true;
        } catch (IOException e2) {
            return $assertionsDisabled;
        }
    }

    private static boolean isAccessible(Path path, AccessMode... modes) {
        try {
            provider(path).checkAccess(path, modes);
            return true;
        } catch (IOException e) {
            return $assertionsDisabled;
        }
    }

    public static boolean isReadable(Path path) {
        return isAccessible(path, AccessMode.READ);
    }

    public static boolean isWritable(Path path) {
        return isAccessible(path, AccessMode.WRITE);
    }

    public static boolean isExecutable(Path path) {
        return isAccessible(path, AccessMode.EXECUTE);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:36:0x008c, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x0090, code lost:
        $closeResource(r1, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x0093, code lost:
        throw r2;
     */
    public static Path walkFileTree(Path start, Set<FileVisitOption> options, int maxDepth, FileVisitor<? super Path> visitor) throws IOException {
        FileVisitResult result;
        FileVisitResult result2;
        FileTreeWalker walker = new FileTreeWalker(options, maxDepth);
        FileTreeWalker.Event ev = walker.walk(start);
        while (true) {
            switch (ev.type()) {
                case ENTRY:
                    IOException ioe = ev.ioeException();
                    if (ioe == null) {
                        result2 = visitor.visitFile(ev.file(), ev.attributes());
                    } else {
                        result2 = visitor.visitFileFailed(ev.file(), ioe);
                    }
                    result = result2;
                    break;
                case START_DIRECTORY:
                    result = visitor.preVisitDirectory(ev.file(), ev.attributes());
                    if (result == FileVisitResult.SKIP_SUBTREE || result == FileVisitResult.SKIP_SIBLINGS) {
                        walker.pop();
                        break;
                    }
                case END_DIRECTORY:
                    result = visitor.postVisitDirectory(ev.file(), ev.ioeException());
                    if (result == FileVisitResult.SKIP_SIBLINGS) {
                        result = FileVisitResult.CONTINUE;
                        break;
                    }
                    break;
                default:
                    throw new AssertionError((Object) "Should not get here");
            }
            if (Objects.requireNonNull(result) != FileVisitResult.CONTINUE) {
                if (result != FileVisitResult.TERMINATE) {
                    if (result == FileVisitResult.SKIP_SIBLINGS) {
                        walker.skipRemainingSiblings();
                    }
                }
            }
            ev = walker.next();
            if (ev == null) {
            }
        }
        $closeResource(null, walker);
        return start;
    }

    private static /* synthetic */ void $closeResource(Throwable x0, AutoCloseable x1) {
        if (x0 != null) {
            try {
                x1.close();
            } catch (Throwable th) {
                x0.addSuppressed(th);
            }
        } else {
            x1.close();
        }
    }

    public static Path walkFileTree(Path start, FileVisitor<? super Path> visitor) throws IOException {
        return walkFileTree(start, EnumSet.noneOf(FileVisitOption.class), Integer.MAX_VALUE, visitor);
    }

    public static BufferedReader newBufferedReader(Path path, Charset cs) throws IOException {
        return new BufferedReader(new InputStreamReader(newInputStream(path, new OpenOption[0]), cs.newDecoder()));
    }

    public static BufferedReader newBufferedReader(Path path) throws IOException {
        return newBufferedReader(path, StandardCharsets.UTF_8);
    }

    public static BufferedWriter newBufferedWriter(Path path, Charset cs, OpenOption... options) throws IOException {
        return new BufferedWriter(new OutputStreamWriter(newOutputStream(path, options), cs.newEncoder()));
    }

    public static BufferedWriter newBufferedWriter(Path path, OpenOption... options) throws IOException {
        return newBufferedWriter(path, StandardCharsets.UTF_8, options);
    }

    private static long copy(InputStream source, OutputStream sink) throws IOException {
        long nread = 0;
        byte[] buf = new byte[8192];
        while (true) {
            int read = source.read(buf);
            int n = read;
            if (read <= 0) {
                return nread;
            }
            sink.write(buf, 0, n);
            nread += (long) n;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:25:0x005c, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0060, code lost:
        if (r2 != null) goto L_0x0062;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0062, code lost:
        $closeResource(r4, r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0065, code lost:
        throw r5;
     */
    public static long copy(InputStream in, Path target, CopyOption... options) throws IOException {
        Objects.requireNonNull(in);
        int length = options.length;
        boolean replaceExisting = false;
        int i = 0;
        while (i < length) {
            CopyOption opt = options[i];
            if (opt == StandardCopyOption.REPLACE_EXISTING) {
                replaceExisting = true;
                i++;
            } else if (opt == null) {
                throw new NullPointerException("options contains 'null'");
            } else {
                throw new UnsupportedOperationException(opt + " not supported");
            }
        }
        SecurityException se = null;
        if (replaceExisting) {
            try {
                deleteIfExists(target);
            } catch (SecurityException x) {
                se = x;
            }
        }
        try {
            OutputStream out = newOutputStream(target, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
            long copy = copy(in, out);
            if (out != null) {
                $closeResource(null, out);
            }
            return copy;
        } catch (FileAlreadyExistsException x2) {
            if (se != null) {
                throw se;
            }
            throw x2;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0019, code lost:
        if (r0 != null) goto L_0x001b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x001b, code lost:
        $closeResource(r1, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001e, code lost:
        throw r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:6:0x0015, code lost:
        r2 = move-exception;
     */
    public static long copy(Path source, OutputStream out) throws IOException {
        Objects.requireNonNull(out);
        InputStream in = newInputStream(source, new OpenOption[0]);
        long copy = copy(in, out);
        if (in != null) {
            $closeResource(null, in);
        }
        return copy;
    }

    private static byte[] read(InputStream source, int initialSize) throws IOException {
        int capacity = initialSize;
        byte[] buf = new byte[capacity];
        int nread = 0;
        while (true) {
            int read = source.read(buf, nread, capacity - nread);
            int n = read;
            if (read <= 0) {
                if (n < 0) {
                    break;
                }
                int read2 = source.read();
                int n2 = read2;
                if (read2 < 0) {
                    break;
                }
                if (capacity <= MAX_BUFFER_SIZE - capacity) {
                    capacity = Math.max(capacity << 1, 8192);
                } else if (capacity != MAX_BUFFER_SIZE) {
                    capacity = MAX_BUFFER_SIZE;
                } else {
                    throw new OutOfMemoryError("Required array size too large");
                }
                buf = Arrays.copyOf(buf, capacity);
                buf[nread] = (byte) n2;
                nread++;
            } else {
                nread += n;
            }
        }
        return capacity == nread ? buf : Arrays.copyOf(buf, nread);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0030, code lost:
        r3 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0031, code lost:
        r4 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0035, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0036, code lost:
        r7 = r4;
        r4 = r3;
        r3 = r7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x003f, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0043, code lost:
        if (r0 != null) goto L_0x0045;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0045, code lost:
        $closeResource(r1, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0048, code lost:
        throw r2;
     */
    public static byte[] readAllBytes(Path path) throws IOException {
        Throwable th;
        Throwable th2;
        SeekableByteChannel sbc = newByteChannel(path, new OpenOption[0]);
        InputStream in = Channels.newInputStream((ReadableByteChannel) sbc);
        long size = sbc.size();
        if (size <= 2147483639) {
            byte[] read = read(in, (int) size);
            if (in != null) {
                $closeResource(null, in);
            }
            if (sbc != null) {
                $closeResource(null, sbc);
            }
            return read;
        }
        throw new OutOfMemoryError("Required array size too large");
        if (in != null) {
            $closeResource(th, in);
        }
        throw th2;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x001c, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0020, code lost:
        if (r0 != null) goto L_0x0022;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0022, code lost:
        $closeResource(r1, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0025, code lost:
        throw r2;
     */
    public static List<String> readAllLines(Path path, Charset cs) throws IOException {
        BufferedReader reader = newBufferedReader(path, cs);
        List<String> result = new ArrayList<>();
        while (true) {
            String line = reader.readLine();
            if (line == null) {
                if (reader != null) {
                    $closeResource(null, reader);
                }
                return result;
            }
            result.add(line);
        }
    }

    public static List<String> readAllLines(Path path) throws IOException {
        return readAllLines(path, StandardCharsets.UTF_8);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0023, code lost:
        if (r0 != null) goto L_0x0025;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0025, code lost:
        $closeResource(r1, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0028, code lost:
        throw r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x001f, code lost:
        r2 = move-exception;
     */
    public static Path write(Path path, byte[] bytes, OpenOption... options) throws IOException {
        Objects.requireNonNull(bytes);
        OutputStream out = newOutputStream(path, options);
        int len = bytes.length;
        int rem = len;
        while (rem > 0) {
            int n = Math.min(rem, 8192);
            out.write(bytes, len - rem, n);
            rem -= n;
        }
        if (out != null) {
            $closeResource(null, out);
        }
        return path;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0035, code lost:
        $closeResource(r3, r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0038, code lost:
        throw r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0031, code lost:
        r4 = move-exception;
     */
    public static Path write(Path path, Iterable<? extends CharSequence> lines, Charset cs, OpenOption... options) throws IOException {
        Objects.requireNonNull(lines);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(newOutputStream(path, options), cs.newEncoder()));
        for (CharSequence line : lines) {
            writer.append(line);
            writer.newLine();
        }
        $closeResource(null, writer);
        return path;
    }

    public static Path write(Path path, Iterable<? extends CharSequence> lines, OpenOption... options) throws IOException {
        return write(path, lines, StandardCharsets.UTF_8, options);
    }

    public static Stream<Path> list(Path dir) throws IOException {
        DirectoryStream<Path> ds = newDirectoryStream(dir);
        try {
            final Iterator<Path> delegate = ds.iterator();
            return (Stream) StreamSupport.stream(Spliterators.spliteratorUnknownSize(new Iterator<Path>() {
                public boolean hasNext() {
                    try {
                        return Iterator.this.hasNext();
                    } catch (DirectoryIteratorException e) {
                        throw new UncheckedIOException(e.getCause());
                    }
                }

                public Path next() {
                    try {
                        return (Path) Iterator.this.next();
                    } catch (DirectoryIteratorException e) {
                        throw new UncheckedIOException(e.getCause());
                    }
                }
            }, 1), $assertionsDisabled).onClose(asUncheckedRunnable(ds));
        } catch (Error | RuntimeException e) {
            try {
                ds.close();
            } catch (IOException ex) {
                e.addSuppressed(ex);
            } catch (Throwable th) {
            }
            throw e;
        }
    }

    public static Stream<Path> walk(Path start, int maxDepth, FileVisitOption... options) throws IOException {
        FileTreeIterator iterator = new FileTreeIterator(start, maxDepth, options);
        try {
            Stream stream = StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, 1), $assertionsDisabled);
            Objects.requireNonNull(iterator);
            return ((Stream) stream.onClose(new Runnable() {
                public final void run() {
                    FileTreeIterator.this.close();
                }
            })).map($$Lambda$Files$troLqSRHugOdjQwE7dW2qp22ctc.INSTANCE);
        } catch (Error | RuntimeException e) {
            iterator.close();
            throw e;
        }
    }

    public static Stream<Path> walk(Path start, FileVisitOption... options) throws IOException {
        return walk(start, Integer.MAX_VALUE, options);
    }

    public static Stream<Path> find(Path start, int maxDepth, BiPredicate<Path, BasicFileAttributes> matcher, FileVisitOption... options) throws IOException {
        FileTreeIterator iterator = new FileTreeIterator(start, maxDepth, options);
        try {
            Stream stream = StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, 1), $assertionsDisabled);
            Objects.requireNonNull(iterator);
            return ((Stream) stream.onClose(new Runnable() {
                public final void run() {
                    FileTreeIterator.this.close();
                }
            })).filter(new Predicate() {
                public final boolean test(Object obj) {
                    return BiPredicate.this.test(((FileTreeWalker.Event) obj).file(), ((FileTreeWalker.Event) obj).attributes());
                }
            }).map($$Lambda$Files$cNMxoBpYNc_xj_crDjR6l6JHUZ0.INSTANCE);
        } catch (Error | RuntimeException e) {
            iterator.close();
            throw e;
        }
    }

    public static Stream<String> lines(Path path, Charset cs) throws IOException {
        BufferedReader br = newBufferedReader(path, cs);
        try {
            return (Stream) br.lines().onClose(asUncheckedRunnable(br));
        } catch (Error | RuntimeException e) {
            try {
                br.close();
            } catch (IOException ex) {
                e.addSuppressed(ex);
            } catch (Throwable th) {
            }
            throw e;
        }
    }

    public static Stream<String> lines(Path path) throws IOException {
        return lines(path, StandardCharsets.UTF_8);
    }
}
