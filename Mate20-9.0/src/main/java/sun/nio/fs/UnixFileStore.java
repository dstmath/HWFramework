package sun.nio.fs;

import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.FileStoreAttributeView;
import java.nio.file.attribute.PosixFileAttributeView;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Properties;

abstract class UnixFileStore extends FileStore {
    private static final Object loadLock = new Object();
    private static volatile Properties props;
    private final long dev;
    private final UnixMountEntry entry;
    private final UnixPath file;

    enum FeatureStatus {
        PRESENT,
        NOT_PRESENT,
        UNKNOWN
    }

    /* access modifiers changed from: package-private */
    public abstract UnixMountEntry findMountEntry() throws IOException;

    private static long devFor(UnixPath file2) throws IOException {
        try {
            return UnixFileAttributes.get(file2, true).dev();
        } catch (UnixException x) {
            x.rethrowAsIOException(file2);
            return 0;
        }
    }

    UnixFileStore(UnixPath file2) throws IOException {
        this.file = file2;
        this.dev = devFor(file2);
        this.entry = findMountEntry();
    }

    UnixFileStore(UnixFileSystem fs, UnixMountEntry entry2) throws IOException {
        this.file = new UnixPath(fs, entry2.dir());
        this.dev = entry2.dev() == 0 ? devFor(this.file) : entry2.dev();
        this.entry = entry2;
    }

    /* access modifiers changed from: package-private */
    public UnixPath file() {
        return this.file;
    }

    /* access modifiers changed from: package-private */
    public long dev() {
        return this.dev;
    }

    /* access modifiers changed from: package-private */
    public UnixMountEntry entry() {
        return this.entry;
    }

    public String name() {
        return this.entry.name();
    }

    public String type() {
        return this.entry.fstype();
    }

    public boolean isReadOnly() {
        return this.entry.isReadOnly();
    }

    private UnixFileStoreAttributes readAttributes() throws IOException {
        try {
            return UnixFileStoreAttributes.get(this.file);
        } catch (UnixException x) {
            x.rethrowAsIOException(this.file);
            return null;
        }
    }

    public long getTotalSpace() throws IOException {
        UnixFileStoreAttributes attrs = readAttributes();
        return attrs.blockSize() * attrs.totalBlocks();
    }

    public long getUsableSpace() throws IOException {
        UnixFileStoreAttributes attrs = readAttributes();
        return attrs.blockSize() * attrs.availableBlocks();
    }

    public long getUnallocatedSpace() throws IOException {
        UnixFileStoreAttributes attrs = readAttributes();
        return attrs.blockSize() * attrs.freeBlocks();
    }

    public <V extends FileStoreAttributeView> V getFileStoreAttributeView(Class<V> view) {
        if (view != null) {
            return (FileStoreAttributeView) null;
        }
        throw new NullPointerException();
    }

    public Object getAttribute(String attribute) throws IOException {
        if (attribute.equals("totalSpace")) {
            return Long.valueOf(getTotalSpace());
        }
        if (attribute.equals("usableSpace")) {
            return Long.valueOf(getUsableSpace());
        }
        if (attribute.equals("unallocatedSpace")) {
            return Long.valueOf(getUnallocatedSpace());
        }
        throw new UnsupportedOperationException("'" + attribute + "' not recognized");
    }

    public boolean supportsFileAttributeView(Class<? extends FileAttributeView> type) {
        if (type != null) {
            boolean z = true;
            if (type == BasicFileAttributeView.class) {
                return true;
            }
            if (type != PosixFileAttributeView.class && type != FileOwnerAttributeView.class) {
                return false;
            }
            if (checkIfFeaturePresent("posix") == FeatureStatus.NOT_PRESENT) {
                z = false;
            }
            return z;
        }
        throw new NullPointerException();
    }

    public boolean supportsFileAttributeView(String name) {
        if (name.equals("basic") || name.equals("unix")) {
            return true;
        }
        if (name.equals("posix")) {
            return supportsFileAttributeView((Class<? extends FileAttributeView>) PosixFileAttributeView.class);
        }
        if (name.equals("owner")) {
            return supportsFileAttributeView((Class<? extends FileAttributeView>) FileOwnerAttributeView.class);
        }
        return false;
    }

    public boolean equals(Object ob) {
        boolean z = true;
        if (ob == this) {
            return true;
        }
        if (!(ob instanceof UnixFileStore)) {
            return false;
        }
        UnixFileStore other = (UnixFileStore) ob;
        if (this.dev != other.dev || !Arrays.equals(this.entry.dir(), other.entry.dir())) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return ((int) (this.dev ^ (this.dev >>> 32))) ^ Arrays.hashCode(this.entry.dir());
    }

    public String toString() {
        return Util.toString(this.entry.dir()) + " (" + this.entry.name() + ")";
    }

    /* access modifiers changed from: package-private */
    public FeatureStatus checkIfFeaturePresent(String feature) {
        if (props == null) {
            synchronized (loadLock) {
                if (props == null) {
                    props = (Properties) AccessController.doPrivileged(new PrivilegedAction<Properties>() {
                        public Properties run() {
                            return UnixFileStore.loadProperties();
                        }
                    });
                }
            }
        }
        String value = props.getProperty(type());
        if (value != null) {
            for (String s : value.split("\\s")) {
                String s2 = s.trim().toLowerCase();
                if (s2.equals(feature)) {
                    return FeatureStatus.PRESENT;
                }
                if (s2.startsWith("no") && s2.substring(2).equals(feature)) {
                    return FeatureStatus.NOT_PRESENT;
                }
            }
        }
        return FeatureStatus.UNKNOWN;
    }

    /* access modifiers changed from: private */
    public static Properties loadProperties() {
        ReadableByteChannel rbc;
        Properties result = new Properties();
        try {
            rbc = Files.newByteChannel(Paths.get(System.getProperty("java.home") + "/lib/fstypes.properties", new String[0]), new OpenOption[0]);
            result.load(Channels.newReader(rbc, "UTF-8"));
            if (rbc != null) {
                rbc.close();
            }
        } catch (IOException e) {
        } catch (Throwable th) {
            r4.addSuppressed(th);
        }
        return result;
        throw th;
    }
}
