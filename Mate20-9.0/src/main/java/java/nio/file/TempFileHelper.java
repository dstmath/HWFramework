package java.nio.file;

import java.io.IOException;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.AccessController;
import java.security.SecureRandom;
import java.util.EnumSet;
import java.util.Set;
import sun.security.action.GetPropertyAction;

class TempFileHelper {
    private static final boolean isPosix = FileSystems.getDefault().supportedFileAttributeViews().contains("posix");
    private static final SecureRandom random = new SecureRandom();
    private static final Path tmpdir = Paths.get((String) AccessController.doPrivileged(new GetPropertyAction("java.io.tmpdir")), new String[0]);

    private static class PosixPermissions {
        static final FileAttribute<Set<PosixFilePermission>> dirPermissions = PosixFilePermissions.asFileAttribute(EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE, PosixFilePermission.OWNER_EXECUTE));
        static final FileAttribute<Set<PosixFilePermission>> filePermissions = PosixFilePermissions.asFileAttribute(EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE));

        private PosixPermissions() {
        }
    }

    private TempFileHelper() {
    }

    private static Path generatePath(String prefix, String suffix, Path dir) {
        long n = random.nextLong();
        long n2 = n == Long.MIN_VALUE ? 0 : Math.abs(n);
        FileSystem fileSystem = dir.getFileSystem();
        Path name = fileSystem.getPath(prefix + Long.toString(n2) + suffix, new String[0]);
        if (name.getParent() == null) {
            return dir.resolve(name);
        }
        throw new IllegalArgumentException("Invalid prefix or suffix");
    }

    /* JADX WARNING: Failed to insert additional move for type inference */
    private static Path create(Path dir, String prefix, String suffix, boolean createDirectory, FileAttribute<?>[] attrs) throws IOException {
        FileAttribute<Set<PosixFilePermission>> fileAttribute;
        FileAttribute<Set<PosixFilePermission>> fileAttribute2;
        if (prefix == null) {
            prefix = "";
        }
        if (suffix == null) {
            suffix = createDirectory ? "" : ".tmp";
        }
        if (dir == null) {
            dir = tmpdir;
        }
        attrs = attrs;
        if (isPosix) {
            attrs = attrs;
            if (dir.getFileSystem() == FileSystems.getDefault()) {
                if (attrs.length == 0) {
                    FileAttribute[] fileAttributeArr = new FileAttribute[1];
                    if (createDirectory) {
                        fileAttribute2 = PosixPermissions.dirPermissions;
                    } else {
                        fileAttribute2 = PosixPermissions.filePermissions;
                    }
                    fileAttributeArr[0] = fileAttribute2;
                    attrs = fileAttributeArr;
                } else {
                    boolean hasPermissions = false;
                    int i = 0;
                    while (true) {
                        if (i >= attrs.length) {
                            break;
                        } else if (attrs[i].name().equals("posix:permissions")) {
                            hasPermissions = true;
                            break;
                        } else {
                            i++;
                        }
                    }
                    if (!hasPermissions) {
                        FileAttribute<?>[] copy = new FileAttribute[(attrs.length + 1)];
                        System.arraycopy((Object) attrs, 0, (Object) copy, 0, attrs.length);
                        attrs = copy;
                        int length = attrs.length - 1;
                        if (createDirectory) {
                            fileAttribute = PosixPermissions.dirPermissions;
                        } else {
                            fileAttribute = PosixPermissions.filePermissions;
                        }
                        attrs[length] = fileAttribute;
                    }
                }
            }
        }
        SecurityManager sm = System.getSecurityManager();
        while (true) {
            try {
                Path f = generatePath(prefix, suffix, dir);
                if (!createDirectory) {
                    return Files.createFile(f, attrs);
                }
                try {
                    return Files.createDirectory(f, attrs);
                } catch (SecurityException e) {
                    if (dir != tmpdir || sm == null) {
                        throw e;
                    }
                    throw new SecurityException("Unable to create temporary file or directory");
                } catch (FileAlreadyExistsException e2) {
                }
            } catch (InvalidPathException e3) {
                if (sm != null) {
                    throw new IllegalArgumentException("Invalid prefix or suffix");
                }
                throw e3;
            }
        }
    }

    static Path createTempFile(Path dir, String prefix, String suffix, FileAttribute<?>[] attrs) throws IOException {
        return create(dir, prefix, suffix, false, attrs);
    }

    static Path createTempDirectory(Path dir, String prefix, FileAttribute<?>[] attrs) throws IOException {
        return create(dir, prefix, null, true, attrs);
    }
}
