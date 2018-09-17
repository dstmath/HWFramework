package sun.nio.fs;

import java.io.FilePermission;
import java.io.IOException;
import java.net.URI;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.LinkOption;
import java.nio.file.LinkPermission;
import java.nio.file.NotDirectoryException;
import java.nio.file.NotLinkException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.ProviderMismatchException;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.spi.FileTypeDetector;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import sun.nio.ch.ThreadPool;
import sun.security.util.SecurityConstants;

public abstract class UnixFileSystemProvider extends AbstractFileSystemProvider {
    private static final /* synthetic */ int[] -java-nio-file-AccessModeSwitchesValues = null;
    private static final String USER_DIR = "user.dir";
    private final UnixFileSystem theFileSystem = newFileSystem(System.getProperty(USER_DIR));

    private static /* synthetic */ int[] -getjava-nio-file-AccessModeSwitchesValues() {
        if (-java-nio-file-AccessModeSwitchesValues != null) {
            return -java-nio-file-AccessModeSwitchesValues;
        }
        int[] iArr = new int[AccessMode.values().length];
        try {
            iArr[AccessMode.EXECUTE.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[AccessMode.READ.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[AccessMode.WRITE.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        -java-nio-file-AccessModeSwitchesValues = iArr;
        return iArr;
    }

    abstract FileStore getFileStore(UnixPath unixPath) throws IOException;

    abstract UnixFileSystem newFileSystem(String str);

    public final String getScheme() {
        return "file";
    }

    private void checkUri(URI uri) {
        if (!uri.getScheme().equalsIgnoreCase(getScheme())) {
            throw new IllegalArgumentException("URI does not match this provider");
        } else if (uri.getAuthority() != null) {
            throw new IllegalArgumentException("Authority component present");
        } else if (uri.getPath() == null) {
            throw new IllegalArgumentException("Path component is undefined");
        } else if (!uri.getPath().equals("/")) {
            throw new IllegalArgumentException("Path component should be '/'");
        } else if (uri.getQuery() != null) {
            throw new IllegalArgumentException("Query component present");
        } else if (uri.getFragment() != null) {
            throw new IllegalArgumentException("Fragment component present");
        }
    }

    public final FileSystem newFileSystem(URI uri, Map<String, ?> map) {
        checkUri(uri);
        throw new FileSystemAlreadyExistsException();
    }

    public final FileSystem getFileSystem(URI uri) {
        checkUri(uri);
        return this.theFileSystem;
    }

    public Path getPath(URI uri) {
        return UnixUriUtils.fromUri(this.theFileSystem, uri);
    }

    UnixPath checkPath(Path obj) {
        if (obj == null) {
            throw new NullPointerException();
        } else if (obj instanceof UnixPath) {
            return (UnixPath) obj;
        } else {
            throw new ProviderMismatchException();
        }
    }

    public <V extends FileAttributeView> V getFileAttributeView(Path obj, Class<V> type, LinkOption... options) {
        UnixPath file = UnixPath.toUnixPath(obj);
        boolean followLinks = Util.followLinks(options);
        if (type == BasicFileAttributeView.class) {
            return UnixFileAttributeViews.createBasicView(file, followLinks);
        }
        if (type == PosixFileAttributeView.class) {
            return UnixFileAttributeViews.createPosixView(file, followLinks);
        }
        if (type == FileOwnerAttributeView.class) {
            return UnixFileAttributeViews.createOwnerView(file, followLinks);
        }
        if (type != null) {
            return (FileAttributeView) null;
        }
        throw new NullPointerException();
    }

    public <A extends BasicFileAttributes> A readAttributes(Path file, Class<A> type, LinkOption... options) throws IOException {
        Class view;
        if (type == BasicFileAttributes.class) {
            view = BasicFileAttributeView.class;
        } else if (type == PosixFileAttributes.class) {
            view = PosixFileAttributeView.class;
        } else if (type == null) {
            throw new NullPointerException();
        } else {
            throw new UnsupportedOperationException();
        }
        return ((BasicFileAttributeView) getFileAttributeView(file, view, options)).readAttributes();
    }

    protected DynamicFileAttributeView getFileAttributeView(Path obj, String name, LinkOption... options) {
        UnixPath file = UnixPath.toUnixPath(obj);
        boolean followLinks = Util.followLinks(options);
        if (name.equals("basic")) {
            return UnixFileAttributeViews.createBasicView(file, followLinks);
        }
        if (name.equals("posix")) {
            return UnixFileAttributeViews.createPosixView(file, followLinks);
        }
        if (name.equals("unix")) {
            return UnixFileAttributeViews.createUnixView(file, followLinks);
        }
        if (name.equals("owner")) {
            return UnixFileAttributeViews.createOwnerView(file, followLinks);
        }
        return null;
    }

    public FileChannel newFileChannel(Path obj, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
        UnixPath file = checkPath(obj);
        try {
            return UnixChannelFactory.newFileChannel(file, options, UnixFileModeAttribute.toUnixMode(UnixFileModeAttribute.ALL_READWRITE, attrs));
        } catch (UnixException x) {
            x.rethrowAsIOException(file);
            return null;
        }
    }

    public AsynchronousFileChannel newAsynchronousFileChannel(Path obj, Set<? extends OpenOption> options, ExecutorService executor, FileAttribute<?>... attrs) throws IOException {
        UnixPath file = checkPath(obj);
        try {
            return UnixChannelFactory.newAsynchronousFileChannel(file, options, UnixFileModeAttribute.toUnixMode(UnixFileModeAttribute.ALL_READWRITE, attrs), executor == null ? null : ThreadPool.wrap(executor, 0));
        } catch (UnixException x) {
            x.rethrowAsIOException(file);
            return null;
        }
    }

    public SeekableByteChannel newByteChannel(Path obj, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
        UnixPath file = UnixPath.toUnixPath(obj);
        try {
            return UnixChannelFactory.newFileChannel(file, options, UnixFileModeAttribute.toUnixMode(UnixFileModeAttribute.ALL_READWRITE, attrs));
        } catch (UnixException x) {
            x.rethrowAsIOException(file);
            return null;
        }
    }

    boolean implDelete(Path obj, boolean failIfNotExists) throws IOException {
        UnixPath file = UnixPath.toUnixPath(obj);
        file.checkDelete();
        UnixFileAttributes unixFileAttributes = null;
        try {
            if (UnixFileAttributes.get(file, false).isDirectory()) {
                UnixNativeDispatcher.rmdir(file);
            } else {
                UnixNativeDispatcher.unlink(file);
            }
            return true;
        } catch (UnixException x) {
            if (!failIfNotExists && x.errno() == UnixConstants.ENOENT) {
                return false;
            }
            if (unixFileAttributes != null && unixFileAttributes.isDirectory() && (x.errno() == UnixConstants.EEXIST || x.errno() == UnixConstants.ENOTEMPTY)) {
                throw new DirectoryNotEmptyException(file.getPathForExceptionMessage());
            }
            x.rethrowAsIOException(file);
            return false;
        }
    }

    public void copy(Path source, Path target, CopyOption... options) throws IOException {
        UnixCopyFile.copy(UnixPath.toUnixPath(source), UnixPath.toUnixPath(target), options);
    }

    public void move(Path source, Path target, CopyOption... options) throws IOException {
        UnixCopyFile.move(UnixPath.toUnixPath(source), UnixPath.toUnixPath(target), options);
    }

    public void checkAccess(Path obj, AccessMode... modes) throws IOException {
        int i = 0;
        UnixPath file = UnixPath.toUnixPath(obj);
        boolean e = false;
        boolean r = false;
        if (modes.length == 0) {
            e = true;
        } else {
            int length = modes.length;
            while (i < length) {
                switch (-getjava-nio-file-AccessModeSwitchesValues()[modes[i].ordinal()]) {
                    case 1:
                    case 2:
                    case 3:
                        r = true;
                        i++;
                    default:
                        throw new AssertionError((Object) "Should not get here");
                }
            }
        }
        int mode = 0;
        if (e || r) {
            file.checkRead();
            mode = (r ? UnixConstants.R_OK : UnixConstants.F_OK) | 0;
        }
        if (false) {
            file.checkWrite();
            mode |= UnixConstants.W_OK;
        }
        if (false) {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                sm.checkExec(file.getPathForPermissionCheck());
            }
            mode |= UnixConstants.X_OK;
        }
        try {
            UnixNativeDispatcher.access(file, mode);
        } catch (UnixException exc) {
            exc.rethrowAsIOException(file);
        }
    }

    public boolean isSameFile(Path obj1, Path obj2) throws IOException {
        UnixPath file1 = UnixPath.toUnixPath(obj1);
        if (file1.equals(obj2)) {
            return true;
        }
        if (obj2 == null) {
            throw new NullPointerException();
        } else if (!(obj2 instanceof UnixPath)) {
            return false;
        } else {
            UnixPath file2 = (UnixPath) obj2;
            file1.checkRead();
            file2.checkRead();
            try {
                try {
                    return UnixFileAttributes.get(file1, true).isSameFile(UnixFileAttributes.get(file2, true));
                } catch (UnixException x) {
                    x.rethrowAsIOException(file2);
                    return false;
                }
            } catch (UnixException x2) {
                x2.rethrowAsIOException(file1);
                return false;
            }
        }
    }

    public boolean isHidden(Path obj) {
        boolean z = false;
        UnixPath file = UnixPath.toUnixPath(obj);
        file.checkRead();
        UnixPath name = file.getFileName();
        if (name == null) {
            return false;
        }
        if (name.asByteArray()[0] == (byte) 46) {
            z = true;
        }
        return z;
    }

    public FileStore getFileStore(Path obj) throws IOException {
        throw new SecurityException("getFileStore");
    }

    public void createDirectory(Path obj, FileAttribute<?>... attrs) throws IOException {
        UnixPath dir = UnixPath.toUnixPath(obj);
        dir.checkWrite();
        try {
            UnixNativeDispatcher.mkdir(dir, UnixFileModeAttribute.toUnixMode(UnixFileModeAttribute.ALL_PERMISSIONS, attrs));
        } catch (UnixException x) {
            if (x.errno() == UnixConstants.EISDIR) {
                throw new FileAlreadyExistsException(dir.toString());
            }
            x.rethrowAsIOException(dir);
        }
    }

    public DirectoryStream<Path> newDirectoryStream(Path obj, Filter<? super Path> filter) throws IOException {
        UnixPath dir = UnixPath.toUnixPath(obj);
        dir.checkRead();
        if (filter == null) {
            throw new NullPointerException();
        }
        if (!UnixNativeDispatcher.openatSupported() || UnixConstants.O_NOFOLLOW == 0) {
            try {
                return new UnixDirectoryStream(dir, UnixNativeDispatcher.opendir(dir), filter);
            } catch (UnixException x) {
                if (x.errno() == UnixConstants.ENOTDIR) {
                    throw new NotDirectoryException(dir.getPathForExceptionMessage());
                }
                x.rethrowAsIOException(dir);
            }
        }
        int dfd1 = -1;
        int dfd2 = -1;
        long dp = 0;
        try {
            dfd1 = UnixNativeDispatcher.open(dir, UnixConstants.O_RDONLY, 0);
            dfd2 = UnixNativeDispatcher.dup(dfd1);
            dp = UnixNativeDispatcher.fdopendir(dfd1);
        } catch (UnixException x2) {
            if (dfd1 != -1) {
                UnixNativeDispatcher.close(dfd1);
            }
            if (dfd2 != -1) {
                UnixNativeDispatcher.close(dfd2);
            }
            if (x2.errno() == UnixConstants.ENOTDIR) {
                throw new NotDirectoryException(dir.getPathForExceptionMessage());
            }
            x2.rethrowAsIOException(dir);
        }
        return new UnixSecureDirectoryStream(dir, dp, dfd2, filter);
    }

    public void createSymbolicLink(Path obj1, Path obj2, FileAttribute<?>... attrs) throws IOException {
        UnixPath link = UnixPath.toUnixPath(obj1);
        UnixPath target = UnixPath.toUnixPath(obj2);
        if (attrs.length > 0) {
            UnixFileModeAttribute.toUnixMode(0, attrs);
            throw new UnsupportedOperationException("Initial file attributesnot supported when creating symbolic link");
        }
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new LinkPermission("symbolic"));
            link.checkWrite();
        }
        try {
            UnixNativeDispatcher.symlink(target.asByteArray(), link);
        } catch (UnixException x) {
            x.rethrowAsIOException(link);
        }
    }

    public void createLink(Path obj1, Path obj2) throws IOException {
        UnixPath link = UnixPath.toUnixPath(obj1);
        UnixPath existing = UnixPath.toUnixPath(obj2);
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new LinkPermission("hard"));
            link.checkWrite();
            existing.checkWrite();
        }
        try {
            UnixNativeDispatcher.link(existing, link);
        } catch (UnixException x) {
            x.rethrowAsIOException(link, existing);
        }
    }

    public Path readSymbolicLink(Path obj1) throws IOException {
        UnixPath link = UnixPath.toUnixPath(obj1);
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new FilePermission(link.getPathForPermissionCheck(), SecurityConstants.FILE_READLINK_ACTION));
        }
        try {
            return new UnixPath(link.getFileSystem(), UnixNativeDispatcher.readlink(link));
        } catch (UnixException x) {
            if (x.errno() == UnixConstants.EINVAL) {
                throw new NotLinkException(link.getPathForExceptionMessage());
            }
            x.rethrowAsIOException(link);
            return null;
        }
    }

    FileTypeDetector getFileTypeDetector() {
        return new AbstractFileTypeDetector() {
            public String implProbeContentType(Path file) {
                return null;
            }
        };
    }

    final FileTypeDetector chain(final AbstractFileTypeDetector... detectors) {
        return new AbstractFileTypeDetector() {
            protected String implProbeContentType(Path file) throws IOException {
                for (AbstractFileTypeDetector detector : detectors) {
                    String result = detector.implProbeContentType(file);
                    if (result != null && (result.isEmpty() ^ 1) != 0) {
                        return result;
                    }
                }
                return null;
            }
        };
    }
}
