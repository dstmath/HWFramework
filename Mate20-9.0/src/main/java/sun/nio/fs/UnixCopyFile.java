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
        if (flags.copyBasicAttributes || flags.copyPosixAttributes || flags.copyNonPosixAttributes) {
            int sfd = -1;
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
            } catch (UnixException x3) {
                if (flags.failIfUnableToCopyPosix) {
                    x3.rethrowAsIOException(target);
                }
            } catch (Throwable th) {
                if (dfd >= 0) {
                    UnixNativeDispatcher.close(dfd);
                }
                if (0 == 0) {
                    try {
                        UnixNativeDispatcher.rmdir(target);
                    } catch (UnixException e2) {
                    }
                }
                throw th;
            }
            if (flags.copyNonPosixAttributes && dfd >= 0) {
                try {
                    sfd = UnixNativeDispatcher.open(source, UnixConstants.O_RDONLY, 0);
                } catch (UnixException x4) {
                    if (flags.failIfUnableToCopyNonPosix) {
                        x4.rethrowAsIOException(source);
                    }
                }
                if (sfd >= 0) {
                    source.getFileSystem().copyNonPosixAttributes(sfd, dfd);
                    UnixNativeDispatcher.close(sfd);
                }
            }
            if (flags.copyBasicAttributes != 0) {
                if (dfd >= 0) {
                    try {
                        if (UnixNativeDispatcher.futimesSupported()) {
                            UnixNativeDispatcher.futimes(dfd, attrs.lastAccessTime().to(TimeUnit.MICROSECONDS), attrs.lastModifiedTime().to(TimeUnit.MICROSECONDS));
                        }
                    } catch (UnixException x5) {
                        if (flags.failIfUnableToCopyBasic) {
                            x5.rethrowAsIOException(target);
                        }
                    }
                }
                UnixNativeDispatcher.utimes(target, attrs.lastAccessTime().to(TimeUnit.MICROSECONDS), attrs.lastModifiedTime().to(TimeUnit.MICROSECONDS));
            }
            if (dfd >= 0) {
                UnixNativeDispatcher.close(dfd);
            }
            if (1 == 0) {
                try {
                    UnixNativeDispatcher.rmdir(target);
                } catch (UnixException e3) {
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public static void copyFile(UnixPath source, UnixFileAttributes attrs, UnixPath target, Flags flags, long addressToPollForCancel) throws IOException {
        int fo = -1;
        int fi = -1;
        try {
            fi = UnixNativeDispatcher.open(source, UnixConstants.O_RDONLY, 0);
        } catch (UnixException x) {
            x.rethrowAsIOException(source);
        }
        try {
            fo = UnixNativeDispatcher.open(target, UnixConstants.O_WRONLY | UnixConstants.O_CREAT | UnixConstants.O_EXCL, attrs.mode());
        } catch (UnixException x2) {
            x2.rethrowAsIOException(target);
        } catch (Throwable th) {
            UnixNativeDispatcher.close(fi);
            throw th;
        }
        try {
            transfer(fo, fi, addressToPollForCancel);
        } catch (UnixException x3) {
            x3.rethrowAsIOException(source, target);
        }
        if (flags.copyPosixAttributes) {
            try {
                UnixNativeDispatcher.fchown(fo, attrs.uid(), attrs.gid());
                UnixNativeDispatcher.fchmod(fo, attrs.mode());
            } catch (UnixException x4) {
                if (flags.failIfUnableToCopyPosix) {
                    x4.rethrowAsIOException(target);
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
            } catch (UnixException x5) {
                if (flags.failIfUnableToCopyBasic) {
                    x5.rethrowAsIOException(target);
                }
            }
        }
        UnixNativeDispatcher.close(fo);
        if (1 == 0) {
            try {
                UnixNativeDispatcher.unlink(target);
            } catch (UnixException e) {
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
            if (0 == 0) {
                try {
                    UnixNativeDispatcher.unlink(target);
                } catch (UnixException e) {
                }
            }
            throw th;
        }
        if (flags.copyBasicAttributes) {
            try {
                UnixNativeDispatcher.utimes(target, attrs.lastAccessTime().to(TimeUnit.MICROSECONDS), attrs.lastModifiedTime().to(TimeUnit.MICROSECONDS));
            } catch (UnixException x3) {
                if (flags.failIfUnableToCopyBasic) {
                    x3.rethrowAsIOException(target);
                }
            }
        }
        if (1 == 0) {
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
                if (x.errno() != UnixConstants.EXDEV) {
                    x.rethrowAsIOException(source, target);
                } else {
                    throw new AtomicMoveNotSupportedException(source.getPathForExceptionMessage(), target.getPathForExceptionMessage(), x.errorString());
                }
            }
            return;
        }
        UnixFileAttributes sourceAttrs = null;
        UnixFileAttributes targetAttrs = null;
        boolean targetExists = false;
        try {
            sourceAttrs = UnixFileAttributes.get(source, false);
        } catch (UnixException x2) {
            x2.rethrowAsIOException(source);
        }
        UnixFileAttributes sourceAttrs2 = sourceAttrs;
        try {
            targetAttrs = UnixFileAttributes.get(target, false);
        } catch (UnixException e) {
        }
        UnixFileAttributes targetAttrs2 = targetAttrs;
        if (targetAttrs2 != null) {
            targetExists = true;
        }
        if (targetExists) {
            if (!sourceAttrs2.isSameFile(targetAttrs2)) {
                if (flags.replaceExisting) {
                    try {
                        if (targetAttrs2.isDirectory()) {
                            UnixNativeDispatcher.rmdir(target);
                        } else {
                            UnixNativeDispatcher.unlink(target);
                        }
                    } catch (UnixException x3) {
                        if (!targetAttrs2.isDirectory() || !(x3.errno() == UnixConstants.EEXIST || x3.errno() == UnixConstants.ENOTEMPTY)) {
                            x3.rethrowAsIOException(target);
                        } else {
                            throw new DirectoryNotEmptyException(target.getPathForExceptionMessage());
                        }
                    }
                } else {
                    throw new FileAlreadyExistsException(target.getPathForExceptionMessage());
                }
            } else {
                return;
            }
        }
        try {
            UnixNativeDispatcher.rename(source, target);
        } catch (UnixException x4) {
            if (!(x4.errno() == UnixConstants.EXDEV || x4.errno() == UnixConstants.EISDIR)) {
                x4.rethrowAsIOException(source, target);
            }
            if (sourceAttrs2.isDirectory()) {
                copyDirectory(source, sourceAttrs2, target, flags);
            } else if (sourceAttrs2.isSymbolicLink()) {
                copyLink(source, sourceAttrs2, target, flags);
            } else if (sourceAttrs2.isDevice()) {
                copySpecial(source, sourceAttrs2, target, flags);
            } else {
                copyFile(source, sourceAttrs2, target, flags, 0);
            }
            try {
                if (sourceAttrs2.isDirectory()) {
                    UnixNativeDispatcher.rmdir(source);
                } else {
                    UnixNativeDispatcher.unlink(source);
                }
            } catch (UnixException x5) {
                try {
                    if (sourceAttrs2.isDirectory()) {
                        UnixNativeDispatcher.rmdir(target);
                    } else {
                        UnixNativeDispatcher.unlink(target);
                    }
                } catch (UnixException e2) {
                }
                if (!sourceAttrs2.isDirectory() || !(x5.errno() == UnixConstants.EEXIST || x5.errno() == UnixConstants.ENOTEMPTY)) {
                    x5.rethrowAsIOException(source);
                } else {
                    throw new DirectoryNotEmptyException(source.getPathForExceptionMessage());
                }
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
        UnixFileAttributes sourceAttrs2 = sourceAttrs;
        if (sm != null && sourceAttrs2.isSymbolicLink()) {
            sm.checkPermission(new LinkPermission("symbolic"));
        }
        boolean targetExists = false;
        try {
            targetAttrs = UnixFileAttributes.get(target, false);
        } catch (UnixException e) {
        }
        UnixFileAttributes targetAttrs2 = targetAttrs;
        if (targetAttrs2 != null) {
            targetExists = true;
        }
        if (targetExists) {
            if (!sourceAttrs2.isSameFile(targetAttrs2)) {
                if (flags.replaceExisting) {
                    try {
                        if (targetAttrs2.isDirectory()) {
                            UnixNativeDispatcher.rmdir(target);
                        } else {
                            UnixNativeDispatcher.unlink(target);
                        }
                    } catch (UnixException x2) {
                        if (!targetAttrs2.isDirectory() || !(x2.errno() == UnixConstants.EEXIST || x2.errno() == UnixConstants.ENOTEMPTY)) {
                            x2.rethrowAsIOException(target);
                        } else {
                            throw new DirectoryNotEmptyException(target.getPathForExceptionMessage());
                        }
                    }
                } else {
                    throw new FileAlreadyExistsException(target.getPathForExceptionMessage());
                }
            } else {
                return;
            }
        }
        if (sourceAttrs2.isDirectory()) {
            copyDirectory(source, sourceAttrs2, target, flags);
        } else if (sourceAttrs2.isSymbolicLink()) {
            copyLink(source, sourceAttrs2, target, flags);
        } else if (!flags.interruptible) {
            copyFile(source, sourceAttrs2, target, flags, 0);
        } else {
            final UnixFileAttributes attrsToCopy = sourceAttrs2;
            try {
                Cancellable.runInterruptibly(new Cancellable() {
                    public void implRun() throws IOException {
                        UnixCopyFile.copyFile(UnixPath.this, attrsToCopy, target, flags, addressToPollForCancel());
                    }
                });
            } catch (ExecutionException e2) {
                Throwable t = e2.getCause();
                if (t instanceof IOException) {
                    throw ((IOException) t);
                }
                throw new IOException(t);
            }
        }
    }
}
