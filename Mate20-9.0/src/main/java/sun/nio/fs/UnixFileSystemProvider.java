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
    private static final String USER_DIR = "user.dir";
    private final UnixFileSystem theFileSystem = newFileSystem(System.getProperty(USER_DIR));

    /* access modifiers changed from: package-private */
    public abstract FileStore getFileStore(UnixPath unixPath) throws IOException;

    /* access modifiers changed from: package-private */
    public abstract UnixFileSystem newFileSystem(String str);

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

    /* access modifiers changed from: package-private */
    public UnixPath checkPath(Path obj) {
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
        Class cls;
        if (type == BasicFileAttributes.class) {
            cls = BasicFileAttributeView.class;
        } else if (type == PosixFileAttributes.class) {
            cls = PosixFileAttributeView.class;
        } else if (type == null) {
            throw new NullPointerException();
        } else {
            throw new UnsupportedOperationException();
        }
        return ((BasicFileAttributeView) getFileAttributeView(file, cls, options)).readAttributes();
    }

    /* access modifiers changed from: protected */
    public DynamicFileAttributeView getFileAttributeView(Path obj, String name, LinkOption... options) {
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

    /* access modifiers changed from: package-private */
    public boolean implDelete(Path obj, boolean failIfNotExists) throws IOException {
        UnixPath file = UnixPath.toUnixPath(obj);
        file.checkDelete();
        UnixFileAttributes attrs = null;
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
            if (attrs == null || !attrs.isDirectory() || !(x.errno() == UnixConstants.EEXIST || x.errno() == UnixConstants.ENOTEMPTY)) {
                x.rethrowAsIOException(file);
                return false;
            }
            throw new DirectoryNotEmptyException(file.getPathForExceptionMessage());
        }
    }

    public void copy(Path source, Path target, CopyOption... options) throws IOException {
        UnixCopyFile.copy(UnixPath.toUnixPath(source), UnixPath.toUnixPath(target), options);
    }

    public void move(Path source, Path target, CopyOption... options) throws IOException {
        UnixCopyFile.move(UnixPath.toUnixPath(source), UnixPath.toUnixPath(target), options);
    }

    public void checkAccess(Path obj, AccessMode... modes) throws IOException {
        UnixPath file = UnixPath.toUnixPath(obj);
        boolean e = false;
        boolean r = false;
        boolean w = false;
        boolean x = false;
        if (modes.length == 0) {
            e = true;
        } else {
            for (AccessMode mode : modes) {
                switch (mode) {
                    case READ:
                        r = true;
                        break;
                    case WRITE:
                        w = true;
                        break;
                    case EXECUTE:
                        x = true;
                        break;
                    default:
                        throw new AssertionError((Object) "Should not get here");
                }
            }
        }
        int mode2 = 0;
        if (e || r) {
            file.checkRead();
            mode2 = 0 | (r ? UnixConstants.R_OK : UnixConstants.F_OK);
        }
        if (w) {
            file.checkWrite();
            mode2 |= UnixConstants.W_OK;
        }
        if (x) {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                sm.checkExec(file.getPathForPermissionCheck());
            }
            mode2 |= UnixConstants.X_OK;
        }
        try {
            UnixNativeDispatcher.access(file, mode2);
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
        UnixPath file = UnixPath.toUnixPath(obj);
        file.checkRead();
        UnixPath name = file.getFileName();
        boolean z = false;
        if (name == null) {
            return false;
        }
        if (name.asByteArray()[0] == 46) {
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
            if (x.errno() != UnixConstants.EISDIR) {
                x.rethrowAsIOException(dir);
                return;
            }
            throw new FileAlreadyExistsException(dir.toString());
        }
    }

    public DirectoryStream<Path> newDirectoryStream(Path obj, DirectoryStream.Filter<? super Path> filter) throws IOException {
        UnixPath dir = UnixPath.toUnixPath(obj);
        dir.checkRead();
        if (filter != null) {
            if (!UnixNativeDispatcher.openatSupported() || UnixConstants.O_NOFOLLOW == 0) {
                try {
                    return new UnixDirectoryStream(dir, UnixNativeDispatcher.opendir(dir), filter);
                } catch (UnixException x) {
                    if (x.errno() != UnixConstants.ENOTDIR) {
                        x.rethrowAsIOException(dir);
                    } else {
                        throw new NotDirectoryException(dir.getPathForExceptionMessage());
                    }
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
                if (-1 != -1) {
                    UnixNativeDispatcher.close(-1);
                }
                if (-1 != -1) {
                    UnixNativeDispatcher.close(-1);
                }
                if (x2.errno() != UnixConstants.ENOTDIR) {
                    x2.rethrowAsIOException(dir);
                } else {
                    throw new NotDirectoryException(dir.getPathForExceptionMessage());
                }
            }
            UnixSecureDirectoryStream unixSecureDirectoryStream = new UnixSecureDirectoryStream(dir, dp, dfd2, filter);
            return unixSecureDirectoryStream;
        }
        throw new NullPointerException();
    }

    public void createSymbolicLink(Path obj1, Path obj2, FileAttribute<?>... attrs) throws IOException {
        UnixPath link = UnixPath.toUnixPath(obj1);
        UnixPath target = UnixPath.toUnixPath(obj2);
        if (attrs.length <= 0) {
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
        } else {
            UnixFileModeAttribute.toUnixMode(0, attrs);
            throw new UnsupportedOperationException("Initial file attributesnot supported when creating symbolic link");
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
            if (x.errno() != UnixConstants.EINVAL) {
                x.rethrowAsIOException(link);
                return null;
            }
            throw new NotLinkException(link.getPathForExceptionMessage());
        }
    }

    /* access modifiers changed from: package-private */
    public FileTypeDetector getFileTypeDetector() {
        return new AbstractFileTypeDetector() {
            public String implProbeContentType(Path file) {
                return null;
            }
        };
    }

    /* access modifiers changed from: package-private */
    public final FileTypeDetector chain(final AbstractFileTypeDetector... detectors) {
        return new AbstractFileTypeDetector() {
            /* access modifiers changed from: protected */
            public String implProbeContentType(Path file) throws IOException {
                for (AbstractFileTypeDetector detector : detectors) {
                    String result = detector.implProbeContentType(file);
                    if (result != null && !result.isEmpty()) {
                        return result;
                    }
                }
                return null;
            }
        };
    }
}
