package java.io;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import sun.misc.Unsafe;

public class File implements Serializable, Comparable<File> {
    static final /* synthetic */ boolean -assertionsDisabled = (File.class.desiredAssertionStatus() ^ 1);
    private static final long PATH_OFFSET;
    private static final long PREFIX_LENGTH_OFFSET;
    private static final Unsafe UNSAFE;
    private static final FileSystem fs = DefaultFileSystem.getFileSystem();
    public static final String pathSeparator = ("" + pathSeparatorChar);
    public static final char pathSeparatorChar = fs.getPathSeparator();
    public static final String separator = ("" + separatorChar);
    public static final char separatorChar = fs.getSeparator();
    private static final long serialVersionUID = 301077366599181567L;
    private volatile transient Path filePath;
    private final String path;
    private final transient int prefixLength;
    private transient PathStatus status = null;

    private enum PathStatus {
        INVALID,
        CHECKED
    }

    private static class TempDirectory {
        private TempDirectory() {
        }

        static File generateFile(String prefix, String suffix, File dir) throws IOException {
            long n = Math.randomLongInternal();
            if (n == Long.MIN_VALUE) {
                n = 0;
            } else {
                n = Math.abs(n);
            }
            String name = prefix + Long.toString(n) + suffix;
            Object f = new File(dir, name);
            if (name.equals(f.getName()) && !f.isInvalid()) {
                return f;
            }
            if (System.getSecurityManager() != null) {
                throw new IOException("Unable to create temporary file");
            }
            throw new IOException("Unable to create temporary file, " + f);
        }
    }

    static {
        try {
            Unsafe unsafe = Unsafe.getUnsafe();
            PATH_OFFSET = unsafe.objectFieldOffset(File.class.getDeclaredField("path"));
            PREFIX_LENGTH_OFFSET = unsafe.objectFieldOffset(File.class.getDeclaredField("prefixLength"));
            UNSAFE = unsafe;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    final boolean isInvalid() {
        if (this.status == null) {
            PathStatus pathStatus;
            if (this.path.indexOf(0) < 0) {
                pathStatus = PathStatus.CHECKED;
            } else {
                pathStatus = PathStatus.INVALID;
            }
            this.status = pathStatus;
        }
        return this.status == PathStatus.INVALID ? true : -assertionsDisabled;
    }

    int getPrefixLength() {
        return this.prefixLength;
    }

    private File(String pathname, int prefixLength) {
        this.path = pathname;
        this.prefixLength = prefixLength;
    }

    private File(String child, File parent) {
        if (!-assertionsDisabled && parent.path == null) {
            throw new AssertionError();
        } else if (-assertionsDisabled || !parent.path.equals("")) {
            this.path = fs.resolve(parent.path, child);
            this.prefixLength = parent.prefixLength;
        } else {
            throw new AssertionError();
        }
    }

    public File(String pathname) {
        if (pathname == null) {
            throw new NullPointerException();
        }
        this.path = fs.normalize(pathname);
        this.prefixLength = fs.prefixLength(this.path);
    }

    public File(String parent, String child) {
        if (child == null) {
            throw new NullPointerException();
        }
        if (parent == null || (parent.isEmpty() ^ 1) == 0) {
            this.path = fs.normalize(child);
        } else {
            this.path = fs.resolve(fs.normalize(parent), fs.normalize(child));
        }
        this.prefixLength = fs.prefixLength(this.path);
    }

    public File(File parent, String child) {
        if (child == null) {
            throw new NullPointerException();
        }
        if (parent == null) {
            this.path = fs.normalize(child);
        } else if (parent.path.equals("")) {
            this.path = fs.resolve(fs.getDefaultParent(), fs.normalize(child));
        } else {
            this.path = fs.resolve(parent.path, fs.normalize(child));
        }
        this.prefixLength = fs.prefixLength(this.path);
    }

    public File(URI uri) {
        if (!uri.isAbsolute()) {
            throw new IllegalArgumentException("URI is not absolute");
        } else if (uri.isOpaque()) {
            throw new IllegalArgumentException("URI is not hierarchical");
        } else {
            String scheme = uri.getScheme();
            if (scheme == null || (scheme.equalsIgnoreCase("file") ^ 1) != 0) {
                throw new IllegalArgumentException("URI scheme is not \"file\"");
            } else if (uri.getAuthority() != null) {
                throw new IllegalArgumentException("URI has an authority component");
            } else if (uri.getFragment() != null) {
                throw new IllegalArgumentException("URI has a fragment component");
            } else if (uri.getQuery() != null) {
                throw new IllegalArgumentException("URI has a query component");
            } else {
                String p = uri.getPath();
                if (p.equals("")) {
                    throw new IllegalArgumentException("URI path component is empty");
                }
                p = fs.fromURIPath(p);
                if (separatorChar != '/') {
                    p = p.replace('/', separatorChar);
                }
                this.path = fs.normalize(p);
                this.prefixLength = fs.prefixLength(this.path);
            }
        }
    }

    public String getName() {
        int index = this.path.lastIndexOf(separatorChar);
        if (index < this.prefixLength) {
            return this.path.substring(this.prefixLength);
        }
        return this.path.substring(index + 1);
    }

    public String getParent() {
        int index = this.path.lastIndexOf(separatorChar);
        if (index >= this.prefixLength) {
            return this.path.substring(0, index);
        }
        if (this.prefixLength <= 0 || this.path.length() <= this.prefixLength) {
            return null;
        }
        return this.path.substring(0, this.prefixLength);
    }

    public File getParentFile() {
        String p = getParent();
        if (p == null) {
            return null;
        }
        return new File(p, this.prefixLength);
    }

    public String getPath() {
        return this.path;
    }

    public boolean isAbsolute() {
        return fs.isAbsolute(this);
    }

    public String getAbsolutePath() {
        return fs.resolve(this);
    }

    public File getAbsoluteFile() {
        String absPath = getAbsolutePath();
        return new File(absPath, fs.prefixLength(absPath));
    }

    public String getCanonicalPath() throws IOException {
        if (!isInvalid()) {
            return fs.canonicalize(fs.resolve(this));
        }
        throw new IOException("Invalid file path");
    }

    public File getCanonicalFile() throws IOException {
        String canonPath = getCanonicalPath();
        return new File(canonPath, fs.prefixLength(canonPath));
    }

    private static String slashify(String path, boolean isDirectory) {
        String p = path;
        if (separatorChar != '/') {
            p = path.replace(separatorChar, '/');
        }
        if (!p.startsWith("/")) {
            p = "/" + p;
        }
        if (p.endsWith("/") || !isDirectory) {
            return p;
        }
        return p + "/";
    }

    @Deprecated
    public URL toURL() throws MalformedURLException {
        if (!isInvalid()) {
            return new URL("file", "", slashify(getAbsolutePath(), getAbsoluteFile().isDirectory()));
        }
        throw new MalformedURLException("Invalid file path");
    }

    public URI toURI() {
        try {
            File f = getAbsoluteFile();
            String sp = slashify(f.getPath(), f.isDirectory());
            if (sp.startsWith("//")) {
                sp = "//" + sp;
            }
            return new URI("file", null, sp, null);
        } catch (Throwable x) {
            throw new Error(x);
        }
    }

    public boolean canRead() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(this.path);
        }
        if (isInvalid()) {
            return -assertionsDisabled;
        }
        return fs.checkAccess(this, 4);
    }

    public boolean canWrite() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkWrite(this.path);
        }
        if (isInvalid()) {
            return -assertionsDisabled;
        }
        return fs.checkAccess(this, 2);
    }

    public boolean exists() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(this.path);
        }
        if (isInvalid()) {
            return -assertionsDisabled;
        }
        return fs.checkAccess(this, 8);
    }

    public boolean isDirectory() {
        boolean z = -assertionsDisabled;
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(this.path);
        }
        if (isInvalid()) {
            return -assertionsDisabled;
        }
        if ((fs.getBooleanAttributes(this) & 4) != 0) {
            z = true;
        }
        return z;
    }

    public boolean isFile() {
        boolean z = -assertionsDisabled;
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(this.path);
        }
        if (isInvalid()) {
            return -assertionsDisabled;
        }
        if ((fs.getBooleanAttributes(this) & 2) != 0) {
            z = true;
        }
        return z;
    }

    public boolean isHidden() {
        boolean z = -assertionsDisabled;
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(this.path);
        }
        if (isInvalid()) {
            return -assertionsDisabled;
        }
        if ((fs.getBooleanAttributes(this) & 8) != 0) {
            z = true;
        }
        return z;
    }

    public long lastModified() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(this.path);
        }
        if (isInvalid()) {
            return 0;
        }
        return fs.getLastModifiedTime(this);
    }

    public long length() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(this.path);
        }
        if (isInvalid()) {
            return 0;
        }
        return fs.getLength(this);
    }

    public boolean createNewFile() throws IOException {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkWrite(this.path);
        }
        if (!isInvalid()) {
            return fs.createFileExclusively(this.path);
        }
        throw new IOException("Invalid file path");
    }

    public boolean delete() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkDelete(this.path);
        }
        if (isInvalid()) {
            return -assertionsDisabled;
        }
        return fs.delete(this);
    }

    public void deleteOnExit() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkDelete(this.path);
        }
        if (!isInvalid()) {
            DeleteOnExitHook.add(this.path);
        }
    }

    public String[] list() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(this.path);
        }
        if (isInvalid()) {
            return null;
        }
        return fs.list(this);
    }

    public String[] list(FilenameFilter filter) {
        String[] names = list();
        if (names == null || filter == null) {
            return names;
        }
        List<String> v = new ArrayList();
        for (int i = 0; i < names.length; i++) {
            if (filter.accept(this, names[i])) {
                v.add(names[i]);
            }
        }
        return (String[]) v.toArray(new String[v.size()]);
    }

    public File[] listFiles() {
        String[] ss = list();
        if (ss == null) {
            return null;
        }
        int n = ss.length;
        File[] fs = new File[n];
        for (int i = 0; i < n; i++) {
            fs[i] = new File(ss[i], this);
        }
        return fs;
    }

    public File[] listFiles(FilenameFilter filter) {
        String[] ss = list();
        if (ss == null) {
            return null;
        }
        ArrayList<File> files = new ArrayList();
        for (String s : ss) {
            if (filter == null || filter.accept(this, s)) {
                files.add(new File(s, this));
            }
        }
        return (File[]) files.toArray(new File[files.size()]);
    }

    public File[] listFiles(FileFilter filter) {
        String[] ss = list();
        if (ss == null) {
            return null;
        }
        ArrayList<File> files = new ArrayList();
        for (String s : ss) {
            File f = new File(s, this);
            if (filter == null || filter.accept(f)) {
                files.add(f);
            }
        }
        return (File[]) files.toArray(new File[files.size()]);
    }

    public boolean mkdir() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkWrite(this.path);
        }
        if (isInvalid()) {
            return -assertionsDisabled;
        }
        return fs.createDirectory(this);
    }

    public boolean mkdirs() {
        boolean z = -assertionsDisabled;
        if (exists()) {
            return -assertionsDisabled;
        }
        if (mkdir()) {
            return true;
        }
        try {
            File canonFile = getCanonicalFile();
            File parent = canonFile.getParentFile();
            if (parent != null && (parent.mkdirs() || parent.exists())) {
                z = canonFile.mkdir();
            }
            return z;
        } catch (IOException e) {
            return -assertionsDisabled;
        }
    }

    public boolean renameTo(File dest) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkWrite(this.path);
            security.checkWrite(dest.path);
        }
        if (dest == null) {
            throw new NullPointerException();
        } else if (isInvalid() || dest.isInvalid()) {
            return -assertionsDisabled;
        } else {
            return fs.rename(this, dest);
        }
    }

    public boolean setLastModified(long time) {
        if (time < 0) {
            throw new IllegalArgumentException("Negative time");
        }
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkWrite(this.path);
        }
        if (isInvalid()) {
            return -assertionsDisabled;
        }
        return fs.setLastModifiedTime(this, time);
    }

    public boolean setReadOnly() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkWrite(this.path);
        }
        if (isInvalid()) {
            return -assertionsDisabled;
        }
        return fs.setReadOnly(this);
    }

    public boolean setWritable(boolean writable, boolean ownerOnly) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkWrite(this.path);
        }
        if (isInvalid()) {
            return -assertionsDisabled;
        }
        return fs.setPermission(this, 2, writable, ownerOnly);
    }

    public boolean setWritable(boolean writable) {
        return setWritable(writable, true);
    }

    public boolean setReadable(boolean readable, boolean ownerOnly) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkWrite(this.path);
        }
        if (isInvalid()) {
            return -assertionsDisabled;
        }
        return fs.setPermission(this, 4, readable, ownerOnly);
    }

    public boolean setReadable(boolean readable) {
        return setReadable(readable, true);
    }

    public boolean setExecutable(boolean executable, boolean ownerOnly) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkWrite(this.path);
        }
        if (isInvalid()) {
            return -assertionsDisabled;
        }
        return fs.setPermission(this, 1, executable, ownerOnly);
    }

    public boolean setExecutable(boolean executable) {
        return setExecutable(executable, true);
    }

    public boolean canExecute() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkExec(this.path);
        }
        if (isInvalid()) {
            return -assertionsDisabled;
        }
        return fs.checkAccess(this, 1);
    }

    public static File[] listRoots() {
        return fs.listRoots();
    }

    public long getTotalSpace() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new RuntimePermission("getFileSystemAttributes"));
            sm.checkRead(this.path);
        }
        if (isInvalid()) {
            return 0;
        }
        return fs.getSpace(this, 0);
    }

    public long getFreeSpace() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new RuntimePermission("getFileSystemAttributes"));
            sm.checkRead(this.path);
        }
        if (isInvalid()) {
            return 0;
        }
        return fs.getSpace(this, 1);
    }

    public long getUsableSpace() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new RuntimePermission("getFileSystemAttributes"));
            sm.checkRead(this.path);
        }
        if (isInvalid()) {
            return 0;
        }
        return fs.getSpace(this, 2);
    }

    public static File createTempFile(String prefix, String suffix, File directory) throws IOException {
        if (prefix.length() < 3) {
            throw new IllegalArgumentException("Prefix string too short");
        }
        File tmpdir;
        File f;
        if (suffix == null) {
            suffix = ".tmp";
        }
        if (directory != null) {
            tmpdir = directory;
        } else {
            tmpdir = new File(System.getProperty("java.io.tmpdir", "."));
        }
        do {
            f = TempDirectory.generateFile(prefix, suffix, tmpdir);
        } while ((fs.getBooleanAttributes(f) & 1) != 0);
        if (fs.createFileExclusively(f.getPath())) {
            return f;
        }
        throw new IOException("Unable to create temporary file");
    }

    public static File createTempFile(String prefix, String suffix) throws IOException {
        return createTempFile(prefix, suffix, null);
    }

    public int compareTo(File pathname) {
        return fs.compare(this, pathname);
    }

    public boolean equals(Object obj) {
        boolean z = -assertionsDisabled;
        if (obj == null || !(obj instanceof File)) {
            return -assertionsDisabled;
        }
        if (compareTo((File) obj) == 0) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        return fs.hashCode(this);
    }

    public String toString() {
        return getPath();
    }

    private synchronized void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        s.writeChar(separatorChar);
    }

    private synchronized void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        String pathField = (String) s.readFields().get("path", null);
        char sep = s.readChar();
        if (sep != separatorChar) {
            pathField = pathField.replace(sep, separatorChar);
        }
        String path = fs.normalize(pathField);
        UNSAFE.putObject(this, PATH_OFFSET, path);
        UNSAFE.putIntVolatile(this, PREFIX_LENGTH_OFFSET, fs.prefixLength(path));
    }

    public Path toPath() {
        Path result = this.filePath;
        if (result == null) {
            synchronized (this) {
                result = this.filePath;
                if (result == null) {
                    result = FileSystems.getDefault().getPath(this.path, new String[0]);
                    this.filePath = result;
                }
            }
        }
        return result;
    }
}
