package sun.nio.fs;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.security.AccessController;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;
import sun.security.action.GetPropertyAction;

abstract class UnixFileSystem extends FileSystem {
    private static final String GLOB_SYNTAX = "glob";
    private static final String REGEX_SYNTAX = "regex";
    private final byte[] defaultDirectory;
    private final boolean needToResolveAgainstDefaultDirectory;
    private final UnixFileSystemProvider provider;
    private final UnixPath rootDirectory;

    private class FileStoreIterator implements Iterator<FileStore> {
        static final /* synthetic */ boolean -assertionsDisabled = (FileStoreIterator.class.desiredAssertionStatus() ^ 1);
        final /* synthetic */ boolean $assertionsDisabled;
        private final Iterator<UnixMountEntry> entries;
        private FileStore next;

        FileStoreIterator() {
            this.entries = UnixFileSystem.this.getMountEntries().iterator();
        }

        private FileStore readNext() {
            if (-assertionsDisabled || Thread.holdsLock(this)) {
                while (this.entries.hasNext()) {
                    UnixMountEntry entry = (UnixMountEntry) this.entries.next();
                    if (!entry.isIgnored()) {
                        SecurityManager sm = System.getSecurityManager();
                        if (sm != null) {
                            try {
                                sm.checkRead(Util.toString(entry.dir()));
                            } catch (SecurityException e) {
                            }
                        }
                        try {
                            return UnixFileSystem.this.getFileStore(entry);
                        } catch (IOException e2) {
                        }
                    }
                }
                return null;
            }
            throw new AssertionError();
        }

        /* JADX WARNING: Missing block: B:11:0x0013, code:
            return r0;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public synchronized boolean hasNext() {
            boolean z = true;
            synchronized (this) {
                if (this.next != null) {
                    return true;
                }
                this.next = readNext();
                if (this.next == null) {
                    z = false;
                }
            }
        }

        public synchronized FileStore next() {
            FileStore result;
            if (this.next == null) {
                this.next = readNext();
            }
            if (this.next == null) {
                throw new NoSuchElementException();
            }
            result = this.next;
            this.next = null;
            return result;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private static class LookupService {
        static final UserPrincipalLookupService instance = new UserPrincipalLookupService() {
            public UserPrincipal lookupPrincipalByName(String name) throws IOException {
                return UnixUserPrincipals.lookupUser(name);
            }

            public GroupPrincipal lookupPrincipalByGroupName(String group) throws IOException {
                return UnixUserPrincipals.lookupGroup(group);
            }
        };

        private LookupService() {
        }
    }

    abstract FileStore getFileStore(UnixMountEntry unixMountEntry) throws IOException;

    abstract Iterable<UnixMountEntry> getMountEntries();

    UnixFileSystem(UnixFileSystemProvider provider, String dir) {
        this.provider = provider;
        this.defaultDirectory = Util.toBytes(UnixPath.normalizeAndCheck(dir));
        if (this.defaultDirectory[0] != (byte) 47) {
            throw new RuntimeException("default directory must be absolute");
        }
        String propValue = (String) AccessController.doPrivileged(new GetPropertyAction("sun.nio.fs.chdirAllowed", "false"));
        if (propValue.length() == 0 ? true : Boolean.valueOf(propValue).booleanValue()) {
            this.needToResolveAgainstDefaultDirectory = true;
        } else {
            byte[] cwd = UnixNativeDispatcher.getcwd();
            boolean defaultIsCwd = cwd.length == this.defaultDirectory.length;
            if (defaultIsCwd) {
                for (int i = 0; i < cwd.length; i++) {
                    if (cwd[i] != this.defaultDirectory[i]) {
                        defaultIsCwd = false;
                        break;
                    }
                }
            }
            this.needToResolveAgainstDefaultDirectory = defaultIsCwd ^ 1;
        }
        this.rootDirectory = new UnixPath(this, "/");
    }

    byte[] defaultDirectory() {
        return this.defaultDirectory;
    }

    boolean needToResolveAgainstDefaultDirectory() {
        return this.needToResolveAgainstDefaultDirectory;
    }

    UnixPath rootDirectory() {
        return this.rootDirectory;
    }

    boolean isSolaris() {
        return false;
    }

    static List<String> standardFileAttributeViews() {
        return Arrays.asList("basic", "posix", "unix", "owner");
    }

    public final FileSystemProvider provider() {
        return this.provider;
    }

    public final String getSeparator() {
        return "/";
    }

    public final boolean isOpen() {
        return true;
    }

    public final boolean isReadOnly() {
        return false;
    }

    public final void close() throws IOException {
        throw new UnsupportedOperationException();
    }

    void copyNonPosixAttributes(int sfd, int tfd) {
    }

    public final Iterable<Path> getRootDirectories() {
        final List<Path> allowedList = Collections.unmodifiableList(Arrays.asList(this.rootDirectory));
        return new Iterable<Path>() {
            public Iterator<Path> iterator() {
                try {
                    SecurityManager sm = System.getSecurityManager();
                    if (sm != null) {
                        sm.checkRead(UnixFileSystem.this.rootDirectory.toString());
                    }
                    return allowedList.iterator();
                } catch (SecurityException e) {
                    return Collections.emptyList().iterator();
                }
            }
        };
    }

    public final Iterable<FileStore> getFileStores() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            try {
                sm.checkPermission(new RuntimePermission("getFileStoreAttributes"));
            } catch (SecurityException e) {
                return Collections.emptyList();
            }
        }
        return new Iterable<FileStore>() {
            public Iterator<FileStore> iterator() {
                return new FileStoreIterator();
            }
        };
    }

    public final Path getPath(String first, String... more) {
        String path;
        if (more.length == 0) {
            path = first;
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(first);
            for (String segment : more) {
                if (segment.length() > 0) {
                    if (sb.length() > 0) {
                        sb.append('/');
                    }
                    sb.append(segment);
                }
            }
            path = sb.-java_util_stream_Collectors-mthref-7();
        }
        return new UnixPath(this, path);
    }

    public PathMatcher getPathMatcher(String syntaxAndInput) {
        int pos = syntaxAndInput.indexOf(58);
        if (pos <= 0 || pos == syntaxAndInput.length()) {
            throw new IllegalArgumentException();
        }
        String expr;
        String syntax = syntaxAndInput.substring(0, pos);
        String input = syntaxAndInput.substring(pos + 1);
        if (syntax.equals(GLOB_SYNTAX)) {
            expr = Globs.toUnixRegexPattern(input);
        } else if (syntax.equals(REGEX_SYNTAX)) {
            expr = input;
        } else {
            throw new UnsupportedOperationException("Syntax '" + syntax + "' not recognized");
        }
        final Pattern pattern = compilePathMatchPattern(expr);
        return new PathMatcher() {
            public boolean matches(Path path) {
                return pattern.matcher(path.toString()).matches();
            }
        };
    }

    public final UserPrincipalLookupService getUserPrincipalLookupService() {
        return LookupService.instance;
    }

    Pattern compilePathMatchPattern(String expr) {
        return Pattern.compile(expr);
    }

    char[] normalizeNativePath(char[] path) {
        return path;
    }

    String normalizeJavaPath(String path) {
        return path;
    }
}
