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
            for (CopyOption option : options) {
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
        int i = 0;
        while (i < len) {
            CopyOption option = options[i];
            if (option != StandardCopyOption.ATOMIC_MOVE) {
                newOptions[i] = option;
                i++;
            } else {
                throw new AtomicMoveNotSupportedException(null, null, "Atomic move between providers is not supported");
            }
        }
        newOptions[len] = LinkOption.NOFOLLOW_LINKS;
        newOptions[len + 1] = StandardCopyOption.COPY_ATTRIBUTES;
        return newOptions;
    }

    static void copyToForeignTarget(Path source, Path target, CopyOption... options) throws IOException {
        Throwable th;
        CopyOptions opts = CopyOptions.parse(options);
        BasicFileAttributes attrs = Files.readAttributes(source, BasicFileAttributes.class, opts.followLinks ? new LinkOption[0] : new LinkOption[]{LinkOption.NOFOLLOW_LINKS});
        if (!attrs.isSymbolicLink()) {
            if (opts.replaceExisting) {
                Files.deleteIfExists(target);
            } else if (Files.exists(target, new LinkOption[0])) {
                throw new FileAlreadyExistsException(target.toString());
            }
            if (attrs.isDirectory()) {
                Files.createDirectory(target, new FileAttribute[0]);
            } else {
                InputStream in = Files.newInputStream(source, new OpenOption[0]);
                try {
                    Files.copy(in, target, new CopyOption[0]);
                    if (in != null) {
                        in.close();
                    }
                } catch (Throwable th2) {
                    th.addSuppressed(th2);
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
        } else {
            throw new IOException("Copying of symbolic links not supported");
        }
        throw x;
        throw th;
    }

    static void moveToForeignTarget(Path source, Path target, CopyOption... options) throws IOException {
        copyToForeignTarget(source, target, convertMoveToCopyOptions(options));
        Files.delete(source);
    }
}
