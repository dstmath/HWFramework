package java.nio.file;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;

class CopyMoveHelper {

    private static class CopyOptions {
        boolean copyAttributes = false;
        boolean followLinks = true;
        boolean replaceExisting = false;

        private CopyOptions() {
        }

        static CopyOptions parse(CopyOption... options) {
            CopyOptions result = new CopyOptions();
            for (Object option : options) {
                if (option == StandardCopyOption.REPLACE_EXISTING) {
                    result.replaceExisting = true;
                } else if (option == LinkOption.NOFOLLOW_LINKS) {
                    result.followLinks = false;
                } else if (option == StandardCopyOption.COPY_ATTRIBUTES) {
                    result.copyAttributes = true;
                } else if (option == null) {
                    throw new NullPointerException();
                } else {
                    throw new UnsupportedOperationException("'" + option + "' is not a recognized copy option");
                }
            }
            return result;
        }
    }

    private CopyMoveHelper() {
    }

    private static CopyOption[] convertMoveToCopyOptions(CopyOption... options) throws AtomicMoveNotSupportedException {
        int len = options.length;
        CopyOption[] newOptions = new CopyOption[(len + 2)];
        for (int i = 0; i < len; i++) {
            CopyOption option = options[i];
            if (option == StandardCopyOption.ATOMIC_MOVE) {
                throw new AtomicMoveNotSupportedException(null, null, "Atomic move between providers is not supported");
            }
            newOptions[i] = option;
        }
        newOptions[len] = LinkOption.NOFOLLOW_LINKS;
        newOptions[len + 1] = StandardCopyOption.COPY_ATTRIBUTES;
        return newOptions;
    }

    static void copyToForeignTarget(Path source, Path target, CopyOption... options) throws IOException {
        InputStream inputStream;
        Throwable th;
        Throwable th2 = null;
        CopyOptions opts = CopyOptions.parse(options);
        BasicFileAttributes attrs = Files.readAttributes(source, BasicFileAttributes.class, opts.followLinks ? new LinkOption[0] : new LinkOption[]{LinkOption.NOFOLLOW_LINKS});
        if (attrs.isSymbolicLink()) {
            throw new IOException("Copying of symbolic links not supported");
        }
        if (opts.replaceExisting) {
            Files.deleteIfExists(target);
        } else if (Files.exists(target, new LinkOption[0])) {
            throw new FileAlreadyExistsException(target.toString());
        }
        if (attrs.isDirectory()) {
            Files.createDirectory(target, new FileAttribute[0]);
        } else {
            inputStream = null;
            try {
                inputStream = Files.newInputStream(source, new OpenOption[0]);
                Files.copy(inputStream, target, new CopyOption[0]);
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (Throwable th3) {
                        th2 = th3;
                    }
                }
                if (th2 != null) {
                    throw th2;
                }
            } catch (Throwable th22) {
                Throwable th4 = th22;
                th22 = th;
                th = th4;
            }
        }
        if (opts.copyAttributes) {
            try {
                ((BasicFileAttributeView) Files.getFileAttributeView(target, BasicFileAttributeView.class, new LinkOption[0])).setTimes(attrs.lastModifiedTime(), attrs.lastAccessTime(), attrs.creationTime());
                return;
            } catch (Throwable suppressed) {
                x.addSuppressed(suppressed);
            }
        } else {
            return;
        }
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (Throwable th5) {
                if (th22 == null) {
                    th22 = th5;
                } else if (th22 != th5) {
                    th22.addSuppressed(th5);
                }
            }
        }
        if (th22 != null) {
            throw th22;
        }
        throw th;
    }

    static void moveToForeignTarget(Path source, Path target, CopyOption... options) throws IOException {
        copyToForeignTarget(source, target, convertMoveToCopyOptions(options));
        Files.delete(source);
    }
}
