package sun.nio.fs;

import com.sun.nio.file.ExtendedCopyOption;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.LinkOption;
import java.nio.file.LinkPermission;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

class UnixCopyFile {

    private static class Flags {
        boolean atomicMove;
        boolean copyBasicAttributes;
        boolean copyNonPosixAttributes;
        boolean copyPosixAttributes;
        boolean failIfUnableToCopyBasic;
        boolean failIfUnableToCopyNonPosix;
        boolean failIfUnableToCopyPosix;
        boolean followLinks;
        boolean interruptible;
        boolean replaceExisting;

        private Flags() {
        }

        static Flags fromCopyOptions(CopyOption... options) {
            Flags flags = new Flags();
            flags.followLinks = true;
            for (CopyOption option : options) {
                if (option == StandardCopyOption.REPLACE_EXISTING) {
                    flags.replaceExisting = true;
                } else if (option == LinkOption.NOFOLLOW_LINKS) {
                    flags.followLinks = false;
                } else if (option == StandardCopyOption.COPY_ATTRIBUTES) {
                    flags.copyBasicAttributes = true;
                    flags.copyPosixAttributes = true;
                    flags.copyNonPosixAttributes = true;
                    flags.failIfUnableToCopyBasic = true;
                } else if (option == ExtendedCopyOption.INTERRUPTIBLE) {
                    flags.interruptible = true;
                } else if (option == null) {
                    throw new NullPointerException();
                } else {
                    throw new UnsupportedOperationException("Unsupported copy option");
                }
            }
            return flags;
        }

        static Flags fromMoveOptions(CopyOption... options) {
            Flags flags = new Flags();
            for (CopyOption option : options) {
                if (option == StandardCopyOption.ATOMIC_MOVE) {
                    flags.atomicMove = true;
                } else if (option == StandardCopyOption.REPLACE_EXISTING) {
                    flags.replaceExisting = true;
                } else if (option != LinkOption.NOFOLLOW_LINKS) {
                    if (option == null) {
                        throw new NullPointerException();
                    }
                    throw new UnsupportedOperationException("Unsupported copy option");
                }
            }
            flags.copyBasicAttributes = true;
            flags.copyPosixAttributes = true;
            flags.copyNonPosixAttributes = true;
            flags.failIfUnableToCopyBasic = true;
            return flags;
        }
    }

    static native void transfer(int i, int i2, long j) throws UnixException;

    private UnixCopyFile() {
    }

    private static void copyDirectory(UnixPath source, UnixFileAttributes attrs, UnixPath target, Flags flags) throws IOException {
        try {
            UnixNativeDispatcher.mkdir(target, attrs.mode());
        } catch (UnixException x) {
            x.rethrowAsIOException(target);
        }
        if (flags.copyBasicAttributes || (flags.copyPosixAttributes ^ 1) == 0 || (flags.copyNonPosixAttributes ^ 1) == 0) {
            int dfd = -1;
            try {
                dfd = UnixNativeDispatcher.open(target, UnixConstants.O_RDONLY, 0);
            } catch (UnixException x2) {
                if (flags.copyNonPosixAttributes && flags.failIfUnableToCopyNonPosix) {
                    try {
                        UnixNativeDispatcher.rmdir(target);
                    } catch (UnixException e) {
                    }
                    x2.rethrowAsIOException(target);
                }
            }
            try {
                if (flags.copyPosixAttributes) {
                    if (dfd >= 0) {
                        UnixNativeDispatcher.fchown(dfd, attrs.uid(), attrs.gid());
                        UnixNativeDispatcher.fchmod(dfd, attrs.mode());
                    } else {
                        UnixNativeDispatcher.chown(target, attrs.uid(), attrs.gid());
                        UnixNativeDispatcher.chmod(target, attrs.mode());
                    }
                }
            } catch (UnixException x22) {
                if (flags.failIfUnableToCopyPosix) {
                    x22.rethrowAsIOException(target);
                }
            } catch (Throwable th) {
                if (dfd >= 0) {
                    UnixNativeDispatcher.close(dfd);
                }
                if (!false) {
                    try {
                        UnixNativeDispatcher.rmdir(target);
                    } catch (UnixException e2) {
                    }
                }
            }
            if (flags.copyNonPosixAttributes && dfd >= 0) {
                int sfd = -1;
                try {
                    sfd = UnixNativeDispatcher.open(source, UnixConstants.O_RDONLY, 0);
                } catch (UnixException x222) {
                    if (flags.failIfUnableToCopyNonPosix) {
                        x222.rethrowAsIOException(source);
                    }
                }
                if (sfd >= 0) {
                    source.getFileSystem().copyNonPosixAttributes(sfd, dfd);
                    UnixNativeDispatcher.close(sfd);
                }
            }
            if (flags.copyBasicAttributes) {
                if (dfd >= 0) {
                    try {
                        if (UnixNativeDispatcher.futimesSupported()) {
                            UnixNativeDispatcher.futimes(dfd, attrs.lastAccessTime().to(TimeUnit.MICROSECONDS), attrs.lastModifiedTime().to(TimeUnit.MICROSECONDS));
                        }
                    } catch (UnixException x2222) {
                        if (flags.failIfUnableToCopyBasic) {
                            x2222.rethrowAsIOException(target);
                        }
                    }
                }
                UnixNativeDispatcher.utimes(target, attrs.lastAccessTime().to(TimeUnit.MICROSECONDS), attrs.lastModifiedTime().to(TimeUnit.MICROSECONDS));
            }
            if (dfd >= 0) {
                UnixNativeDispatcher.close(dfd);
            }
            if (!true) {
                try {
                    UnixNativeDispatcher.rmdir(target);
                } catch (UnixException e3) {
                }
            }
        }
    }

    private static void copyFile(UnixPath source, UnixFileAttributes attrs, UnixPath target, Flags flags, long addressToPollForCancel) throws IOException {
        int fi = -1;
        try {
            fi = UnixNativeDispatcher.open(source, UnixConstants.O_RDONLY, 0);
        } catch (UnixException x) {
            x.rethrowAsIOException(source);
        }
        int fo = -1;
        try {
            fo = UnixNativeDispatcher.open(target, (UnixConstants.O_WRONLY | UnixConstants.O_CREAT) | UnixConstants.O_EXCL, attrs.mode());
        } catch (UnixException x2) {
            x2.rethrowAsIOException(target);
        } catch (Throwable th) {
            UnixNativeDispatcher.close(fi);
        }
        try {
            transfer(fo, fi, addressToPollForCancel);
        } catch (UnixException x22) {
            x22.rethrowAsIOException(source, target);
        } catch (Throwable th2) {
            UnixNativeDispatcher.close(fo);
            if (!false) {
                try {
                    UnixNativeDispatcher.unlink(target);
                } catch (UnixException e) {
                }
            }
        }
        if (flags.copyPosixAttributes) {
            try {
                UnixNativeDispatcher.fchown(fo, attrs.uid(), attrs.gid());
                UnixNativeDispatcher.fchmod(fo, attrs.mode());
            } catch (UnixException x222) {
                if (flags.failIfUnableToCopyPosix) {
                    x222.rethrowAsIOException(target);
                }
            }
        }
        if (flags.copyNonPosixAttributes) {
            source.getFileSystem().copyNonPosixAttributes(fi, fo);
        }
        if (flags.copyBasicAttributes) {
            try {
                if (UnixNativeDispatcher.futimesSupported()) {
                    UnixNativeDispatcher.futimes(fo, attrs.lastAccessTime().to(TimeUnit.MICROSECONDS), attrs.lastModifiedTime().to(TimeUnit.MICROSECONDS));
                } else {
                    UnixNativeDispatcher.utimes(target, attrs.lastAccessTime().to(TimeUnit.MICROSECONDS), attrs.lastModifiedTime().to(TimeUnit.MICROSECONDS));
                }
            } catch (UnixException x2222) {
                if (flags.failIfUnableToCopyBasic) {
                    x2222.rethrowAsIOException(target);
                }
            }
        }
        UnixNativeDispatcher.close(fo);
        if (!true) {
            try {
                UnixNativeDispatcher.unlink(target);
            } catch (UnixException e2) {
            }
        }
        UnixNativeDispatcher.close(fi);
    }

    private static void copyLink(UnixPath source, UnixFileAttributes attrs, UnixPath target, Flags flags) throws IOException {
        byte[] linktarget = null;
        try {
            linktarget = UnixNativeDispatcher.readlink(source);
        } catch (UnixException x) {
            x.rethrowAsIOException(source);
        }
        try {
            UnixNativeDispatcher.symlink(linktarget, target);
            if (flags.copyPosixAttributes) {
                try {
                    UnixNativeDispatcher.lchown(target, attrs.uid(), attrs.gid());
                } catch (UnixException e) {
                }
            }
        } catch (UnixException x2) {
            x2.rethrowAsIOException(target);
        }
    }

    private static void copySpecial(UnixPath source, UnixFileAttributes attrs, UnixPath target, Flags flags) throws IOException {
        try {
            UnixNativeDispatcher.mknod(target, attrs.mode(), attrs.rdev());
        } catch (UnixException x) {
            x.rethrowAsIOException(target);
        }
        try {
            if (flags.copyPosixAttributes) {
                UnixNativeDispatcher.chown(target, attrs.uid(), attrs.gid());
                UnixNativeDispatcher.chmod(target, attrs.mode());
            }
        } catch (UnixException x2) {
            if (flags.failIfUnableToCopyPosix) {
                x2.rethrowAsIOException(target);
            }
        } catch (Throwable th) {
            if (!false) {
                try {
                    UnixNativeDispatcher.unlink(target);
                } catch (UnixException e) {
                }
            }
        }
        if (flags.copyBasicAttributes) {
            try {
                UnixNativeDispatcher.utimes(target, attrs.lastAccessTime().to(TimeUnit.MICROSECONDS), attrs.lastModifiedTime().to(TimeUnit.MICROSECONDS));
            } catch (UnixException x22) {
                if (flags.failIfUnableToCopyBasic) {
                    x22.rethrowAsIOException(target);
                }
            }
        }
        if (!true) {
            try {
                UnixNativeDispatcher.unlink(target);
            } catch (UnixException e2) {
            }
        }
    }

    static void move(UnixPath source, UnixPath target, CopyOption... options) throws IOException {
        if (System.getSecurityManager() != null) {
            source.checkWrite();
            target.checkWrite();
        }
        Flags flags = Flags.fromMoveOptions(options);
        if (flags.atomicMove) {
            try {
                UnixNativeDispatcher.rename(source, target);
            } catch (UnixException x) {
                if (x.errno() == UnixConstants.EXDEV) {
                    throw new AtomicMoveNotSupportedException(source.getPathForExceptionMessage(), target.getPathForExceptionMessage(), x.errorString());
                }
                x.rethrowAsIOException(source, target);
            }
            return;
        }
        UnixFileAttributes sourceAttrs = null;
        UnixFileAttributes targetAttrs = null;
        try {
            sourceAttrs = UnixFileAttributes.get(source, false);
        } catch (UnixException x2) {
            x2.rethrowAsIOException(source);
        }
        try {
            targetAttrs = UnixFileAttributes.get(target, false);
        } catch (UnixException e) {
        }
        if (targetAttrs != null) {
            if (!sourceAttrs.isSameFile(targetAttrs)) {
                if (flags.replaceExisting) {
                    try {
                        if (targetAttrs.isDirectory()) {
                            UnixNativeDispatcher.rmdir(target);
                        } else {
                            UnixNativeDispatcher.unlink(target);
                        }
                    } catch (UnixException x22) {
                        if (targetAttrs.isDirectory() && (x22.errno() == UnixConstants.EEXIST || x22.errno() == UnixConstants.ENOTEMPTY)) {
                            throw new DirectoryNotEmptyException(target.getPathForExceptionMessage());
                        }
                        x22.rethrowAsIOException(target);
                    }
                } else {
                    throw new FileAlreadyExistsException(target.getPathForExceptionMessage());
                }
            }
            return;
        }
        try {
            UnixNativeDispatcher.rename(source, target);
        } catch (UnixException x222) {
            if (!(x222.errno() == UnixConstants.EXDEV || x222.errno() == UnixConstants.EISDIR)) {
                x222.rethrowAsIOException(source, target);
            }
            if (sourceAttrs.isDirectory()) {
                copyDirectory(source, sourceAttrs, target, flags);
            } else if (sourceAttrs.isSymbolicLink()) {
                copyLink(source, sourceAttrs, target, flags);
            } else if (sourceAttrs.isDevice()) {
                copySpecial(source, sourceAttrs, target, flags);
            } else {
                copyFile(source, sourceAttrs, target, flags, 0);
            }
            try {
                if (sourceAttrs.isDirectory()) {
                    UnixNativeDispatcher.rmdir(source);
                } else {
                    UnixNativeDispatcher.unlink(source);
                }
            } catch (UnixException x2222) {
                try {
                    if (sourceAttrs.isDirectory()) {
                        UnixNativeDispatcher.rmdir(target);
                    } else {
                        UnixNativeDispatcher.unlink(target);
                    }
                } catch (UnixException e2) {
                }
                if (sourceAttrs.isDirectory() && (x2222.errno() == UnixConstants.EEXIST || x2222.errno() == UnixConstants.ENOTEMPTY)) {
                    throw new DirectoryNotEmptyException(source.getPathForExceptionMessage());
                }
                x2222.rethrowAsIOException(source);
            }
        }
    }

    static void copy(final UnixPath source, final UnixPath target, CopyOption... options) throws IOException {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            source.checkRead();
            target.checkWrite();
        }
        final Flags flags = Flags.fromCopyOptions(options);
        UnixFileAttributes sourceAttrs = null;
        UnixFileAttributes targetAttrs = null;
        try {
            sourceAttrs = UnixFileAttributes.get(source, flags.followLinks);
        } catch (UnixException x) {
            x.rethrowAsIOException(source);
        }
        if (sm != null && sourceAttrs.isSymbolicLink()) {
            sm.checkPermission(new LinkPermission("symbolic"));
        }
        try {
            targetAttrs = UnixFileAttributes.get(target, false);
        } catch (UnixException e) {
        }
        if (targetAttrs != null) {
            if (!sourceAttrs.isSameFile(targetAttrs)) {
                if (flags.replaceExisting) {
                    try {
                        if (targetAttrs.isDirectory()) {
                            UnixNativeDispatcher.rmdir(target);
                        } else {
                            UnixNativeDispatcher.unlink(target);
                        }
                    } catch (UnixException x2) {
                        if (targetAttrs.isDirectory() && (x2.errno() == UnixConstants.EEXIST || x2.errno() == UnixConstants.ENOTEMPTY)) {
                            throw new DirectoryNotEmptyException(target.getPathForExceptionMessage());
                        }
                        x2.rethrowAsIOException(target);
                    }
                } else {
                    throw new FileAlreadyExistsException(target.getPathForExceptionMessage());
                }
            }
            return;
        }
        if (sourceAttrs.isDirectory()) {
            copyDirectory(source, sourceAttrs, target, flags);
        } else if (sourceAttrs.isSymbolicLink()) {
            copyLink(source, sourceAttrs, target, flags);
        } else if (flags.interruptible) {
            final UnixFileAttributes attrsToCopy = sourceAttrs;
            try {
                Cancellable.runInterruptibly(new Cancellable() {
                    public void implRun() throws IOException {
                        UnixCopyFile.copyFile(source, attrsToCopy, target, flags, addressToPollForCancel());
                    }
                });
            } catch (ExecutionException e2) {
                Throwable t = e2.getCause();
                if (t instanceof IOException) {
                    throw ((IOException) t);
                }
                throw new IOException(t);
            }
        } else {
            copyFile(source, sourceAttrs, target, flags, 0);
        }
    }
}
