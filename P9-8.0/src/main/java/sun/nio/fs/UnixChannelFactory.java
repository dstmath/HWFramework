package sun.nio.fs;

import java.io.FileDescriptor;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.FileChannel;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.util.Set;
import sun.misc.JavaIOFileDescriptorAccess;
import sun.misc.SharedSecrets;
import sun.nio.ch.FileChannelImpl;
import sun.nio.ch.SimpleAsynchronousFileChannelImpl;
import sun.nio.ch.ThreadPool;

class UnixChannelFactory {
    private static final JavaIOFileDescriptorAccess fdAccess = SharedSecrets.getJavaIOFileDescriptorAccess();

    protected static class Flags {
        private static final /* synthetic */ int[] -java-nio-file-StandardOpenOptionSwitchesValues = null;
        boolean append;
        boolean create;
        boolean createNew;
        boolean deleteOnClose;
        boolean dsync;
        boolean noFollowLinks;
        boolean read;
        boolean sync;
        boolean truncateExisting;
        boolean write;

        private static /* synthetic */ int[] -getjava-nio-file-StandardOpenOptionSwitchesValues() {
            if (-java-nio-file-StandardOpenOptionSwitchesValues != null) {
                return -java-nio-file-StandardOpenOptionSwitchesValues;
            }
            int[] iArr = new int[StandardOpenOption.values().length];
            try {
                iArr[StandardOpenOption.APPEND.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[StandardOpenOption.CREATE.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[StandardOpenOption.CREATE_NEW.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[StandardOpenOption.DELETE_ON_CLOSE.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                iArr[StandardOpenOption.DSYNC.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                iArr[StandardOpenOption.READ.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                iArr[StandardOpenOption.SPARSE.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                iArr[StandardOpenOption.SYNC.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                iArr[StandardOpenOption.TRUNCATE_EXISTING.ordinal()] = 9;
            } catch (NoSuchFieldError e9) {
            }
            try {
                iArr[StandardOpenOption.WRITE.ordinal()] = 10;
            } catch (NoSuchFieldError e10) {
            }
            -java-nio-file-StandardOpenOptionSwitchesValues = iArr;
            return iArr;
        }

        protected Flags() {
        }

        static Flags toFlags(Set<? extends OpenOption> options) {
            Flags flags = new Flags();
            for (Object option : options) {
                if (option instanceof StandardOpenOption) {
                    switch (-getjava-nio-file-StandardOpenOptionSwitchesValues()[((StandardOpenOption) option).ordinal()]) {
                        case 1:
                            flags.append = true;
                            break;
                        case 2:
                            flags.create = true;
                            break;
                        case 3:
                            flags.createNew = true;
                            break;
                        case 4:
                            flags.deleteOnClose = true;
                            break;
                        case 5:
                            flags.dsync = true;
                            break;
                        case 6:
                            flags.read = true;
                            break;
                        case 7:
                            break;
                        case 8:
                            flags.sync = true;
                            break;
                        case 9:
                            flags.truncateExisting = true;
                            break;
                        case 10:
                            flags.write = true;
                            break;
                        default:
                            throw new UnsupportedOperationException();
                    }
                } else if (option == LinkOption.NOFOLLOW_LINKS && UnixConstants.O_NOFOLLOW != 0) {
                    flags.noFollowLinks = true;
                } else if (option == null) {
                    throw new NullPointerException();
                } else {
                    throw new UnsupportedOperationException(option + " not supported");
                }
            }
            return flags;
        }
    }

    protected UnixChannelFactory() {
    }

    static FileChannel newFileChannel(int fd, String path, boolean reading, boolean writing) {
        FileDescriptor fdObj = new FileDescriptor();
        fdAccess.set(fdObj, fd);
        return FileChannelImpl.open(fdObj, path, reading, writing, null);
    }

    static FileChannel newFileChannel(int dfd, UnixPath path, String pathForPermissionCheck, Set<? extends OpenOption> options, int mode) throws UnixException {
        Flags flags = Flags.toFlags(options);
        if (!(flags.read || (flags.write ^ 1) == 0)) {
            if (flags.append) {
                flags.write = true;
            } else {
                flags.read = true;
            }
        }
        if (flags.read && flags.append) {
            throw new IllegalArgumentException("READ + APPEND not allowed");
        } else if (!flags.append || !flags.truncateExisting) {
            return FileChannelImpl.open(open(dfd, path, pathForPermissionCheck, flags, mode), path.toString(), flags.read, flags.write, flags.append, null);
        } else {
            throw new IllegalArgumentException("APPEND + TRUNCATE_EXISTING not allowed");
        }
    }

    static FileChannel newFileChannel(UnixPath path, Set<? extends OpenOption> options, int mode) throws UnixException {
        return newFileChannel(-1, path, null, options, mode);
    }

    static AsynchronousFileChannel newAsynchronousFileChannel(UnixPath path, Set<? extends OpenOption> options, int mode, ThreadPool pool) throws UnixException {
        Flags flags = Flags.toFlags(options);
        if (!(flags.read || (flags.write ^ 1) == 0)) {
            flags.read = true;
        }
        if (!flags.append) {
            return SimpleAsynchronousFileChannelImpl.open(open(-1, path, null, flags, mode), flags.read, flags.write, pool);
        }
        throw new UnsupportedOperationException("APPEND not allowed");
    }

    protected static FileDescriptor open(int dfd, UnixPath path, String pathForPermissionCheck, Flags flags, int mode) throws UnixException {
        UnixException x;
        int fd;
        int oflags = (flags.read && flags.write) ? UnixConstants.O_RDWR : flags.write ? UnixConstants.O_WRONLY : UnixConstants.O_RDONLY;
        if (flags.write) {
            if (flags.truncateExisting) {
                oflags |= UnixConstants.O_TRUNC;
            }
            if (flags.append) {
                oflags |= UnixConstants.O_APPEND;
            }
            if (flags.createNew) {
                byte[] pathForSysCall = path.asByteArray();
                if (pathForSysCall[pathForSysCall.length - 1] == (byte) 46 && (pathForSysCall.length == 1 || pathForSysCall[pathForSysCall.length - 2] == (byte) 47)) {
                    throw new UnixException(UnixConstants.EEXIST);
                }
                oflags |= UnixConstants.O_CREAT | UnixConstants.O_EXCL;
            } else if (flags.create) {
                oflags |= UnixConstants.O_CREAT;
            }
        }
        boolean followLinks = true;
        if (!flags.createNew && (flags.noFollowLinks || flags.deleteOnClose)) {
            if (flags.deleteOnClose && UnixConstants.O_NOFOLLOW == 0) {
                try {
                    if (UnixFileAttributes.get(path, false).isSymbolicLink()) {
                        throw new UnixException("DELETE_ON_CLOSE specified and file is a symbolic link");
                    }
                } catch (UnixException x2) {
                    if (!(flags.create && x2.errno() == UnixConstants.ENOENT)) {
                        throw x2;
                    }
                }
            }
            followLinks = false;
            oflags |= UnixConstants.O_NOFOLLOW;
        }
        if (flags.dsync) {
            oflags |= UnixConstants.O_DSYNC;
        }
        if (flags.sync) {
            oflags |= UnixConstants.O_SYNC;
        }
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            if (pathForPermissionCheck == null) {
                pathForPermissionCheck = path.getPathForPermissionCheck();
            }
            if (flags.read) {
                sm.checkRead(pathForPermissionCheck);
            }
            if (flags.write) {
                sm.checkWrite(pathForPermissionCheck);
            }
            if (flags.deleteOnClose) {
                sm.checkDelete(pathForPermissionCheck);
            }
        }
        if (dfd >= 0) {
            try {
                fd = UnixNativeDispatcher.openat(dfd, path.asByteArray(), oflags, mode);
            } catch (UnixException e) {
                x2 = e;
                if (flags.createNew && x2.errno() == UnixConstants.EISDIR) {
                    x2.setError(UnixConstants.EEXIST);
                }
                if (!followLinks && x2.errno() == UnixConstants.ELOOP) {
                    x2 = new UnixException(x2.getMessage() + " (NOFOLLOW_LINKS specified)");
                }
                throw x2;
            }
        }
        fd = UnixNativeDispatcher.open(path, oflags, mode);
        if (flags.deleteOnClose) {
            if (dfd >= 0) {
                try {
                    UnixNativeDispatcher.unlinkat(dfd, path.asByteArray(), 0);
                } catch (UnixException e2) {
                }
            } else {
                UnixNativeDispatcher.unlink(path);
            }
        }
        FileDescriptor fdObj = new FileDescriptor();
        fdAccess.set(fdObj, fd);
        return fdObj;
    }
}
