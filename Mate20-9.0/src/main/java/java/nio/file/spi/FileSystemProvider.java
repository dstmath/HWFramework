package java.nio.file.spi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public abstract class FileSystemProvider {
    private static volatile List<FileSystemProvider> installedProviders;
    private static boolean loadingProviders = false;
    private static final Object lock = new Object();

    public abstract void checkAccess(Path path, AccessMode... accessModeArr) throws IOException;

    public abstract void copy(Path path, Path path2, CopyOption... copyOptionArr) throws IOException;

    public abstract void createDirectory(Path path, FileAttribute<?>... fileAttributeArr) throws IOException;

    public abstract void delete(Path path) throws IOException;

    public abstract <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> cls, LinkOption... linkOptionArr);

    public abstract FileStore getFileStore(Path path) throws IOException;

    public abstract FileSystem getFileSystem(URI uri);

    public abstract Path getPath(URI uri);

    public abstract String getScheme();

    public abstract boolean isHidden(Path path) throws IOException;

    public abstract boolean isSameFile(Path path, Path path2) throws IOException;

    public abstract void move(Path path, Path path2, CopyOption... copyOptionArr) throws IOException;

    public abstract SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> set, FileAttribute<?>... fileAttributeArr) throws IOException;

    public abstract DirectoryStream<Path> newDirectoryStream(Path path, DirectoryStream.Filter<? super Path> filter) throws IOException;

    public abstract FileSystem newFileSystem(URI uri, Map<String, ?> map) throws IOException;

    public abstract <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> cls, LinkOption... linkOptionArr) throws IOException;

    public abstract Map<String, Object> readAttributes(Path path, String str, LinkOption... linkOptionArr) throws IOException;

    public abstract void setAttribute(Path path, String str, Object obj, LinkOption... linkOptionArr) throws IOException;

    private static Void checkPermission() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new RuntimePermission("fileSystemProvider"));
        }
        return null;
    }

    private FileSystemProvider(Void ignore) {
    }

    protected FileSystemProvider() {
        this(checkPermission());
    }

    /* access modifiers changed from: private */
    public static List<FileSystemProvider> loadInstalledProviders() {
        List<FileSystemProvider> list = new ArrayList<>();
        Iterator<FileSystemProvider> it = ServiceLoader.load(FileSystemProvider.class, ClassLoader.getSystemClassLoader()).iterator();
        while (it.hasNext()) {
            FileSystemProvider provider = it.next();
            String scheme = provider.getScheme();
            if (!scheme.equalsIgnoreCase("file")) {
                boolean found = false;
                Iterator<FileSystemProvider> it2 = list.iterator();
                while (true) {
                    if (it2.hasNext()) {
                        if (it2.next().getScheme().equalsIgnoreCase(scheme)) {
                            found = true;
                            break;
                        }
                    } else {
                        break;
                    }
                }
                if (!found) {
                    list.add(provider);
                }
            }
        }
        return list;
    }

    public static List<FileSystemProvider> installedProviders() {
        if (installedProviders == null) {
            FileSystemProvider defaultProvider = FileSystems.getDefault().provider();
            synchronized (lock) {
                if (installedProviders == null) {
                    if (!loadingProviders) {
                        loadingProviders = true;
                        List<FileSystemProvider> list = (List) AccessController.doPrivileged(new PrivilegedAction<List<FileSystemProvider>>() {
                            public List<FileSystemProvider> run() {
                                return FileSystemProvider.loadInstalledProviders();
                            }
                        });
                        list.add(0, defaultProvider);
                        installedProviders = Collections.unmodifiableList(list);
                    } else {
                        throw new Error("Circular loading of installed providers detected");
                    }
                }
            }
        }
        return installedProviders;
    }

    public FileSystem newFileSystem(Path path, Map<String, ?> map) throws IOException {
        throw new UnsupportedOperationException();
    }

    public InputStream newInputStream(Path path, OpenOption... options) throws IOException {
        if (options.length > 0) {
            for (OpenOption opt : options) {
                if (opt == StandardOpenOption.APPEND || opt == StandardOpenOption.WRITE) {
                    throw new UnsupportedOperationException("'" + opt + "' not allowed");
                }
            }
        }
        return Channels.newInputStream((ReadableByteChannel) Files.newByteChannel(path, options));
    }

    public OutputStream newOutputStream(Path path, OpenOption... options) throws IOException {
        int len = options.length;
        Set<OpenOption> opts = new HashSet<>(len + 3);
        if (len == 0) {
            opts.add(StandardOpenOption.CREATE);
            opts.add(StandardOpenOption.TRUNCATE_EXISTING);
        } else {
            int length = options.length;
            int i = 0;
            while (i < length) {
                OpenOption opt = options[i];
                if (opt != StandardOpenOption.READ) {
                    opts.add(opt);
                    i++;
                } else {
                    throw new IllegalArgumentException("READ not allowed");
                }
            }
        }
        opts.add(StandardOpenOption.WRITE);
        return Channels.newOutputStream((WritableByteChannel) newByteChannel(path, opts, new FileAttribute[0]));
    }

    public FileChannel newFileChannel(Path path, Set<? extends OpenOption> set, FileAttribute<?>... fileAttributeArr) throws IOException {
        throw new UnsupportedOperationException();
    }

    public AsynchronousFileChannel newAsynchronousFileChannel(Path path, Set<? extends OpenOption> set, ExecutorService executor, FileAttribute<?>... fileAttributeArr) throws IOException {
        throw new UnsupportedOperationException();
    }

    public void createSymbolicLink(Path link, Path target, FileAttribute<?>... fileAttributeArr) throws IOException {
        throw new UnsupportedOperationException();
    }

    public void createLink(Path link, Path existing) throws IOException {
        throw new UnsupportedOperationException();
    }

    public boolean deleteIfExists(Path path) throws IOException {
        try {
            delete(path);
            return true;
        } catch (NoSuchFileException e) {
            return false;
        }
    }

    public Path readSymbolicLink(Path link) throws IOException {
        throw new UnsupportedOperationException();
    }
}
