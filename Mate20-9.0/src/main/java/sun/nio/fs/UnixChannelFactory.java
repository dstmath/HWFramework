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

        protected Flags() {
        }

        static Flags toFlags(Set<? extends OpenOption> options) {
            Flags flags = new Flags();
            for (OpenOption option : options) {
                if (option instanceof StandardOpenOption) {
                    switch ((StandardOpenOption) option) {
                        case READ:
                            flags.read = true;
                            break;
                        case WRITE:
                            flags.write = true;
                            break;
                        case APPEND:
                            flags.append = true;
                            break;
                        case TRUNCATE_EXISTING:
                            flags.truncateExisting = true;
                            break;
                        case CREATE:
                            flags.create = true;
                            break;
                        case CREATE_NEW:
                            flags.createNew = true;
                            break;
                        case DELETE_ON_CLOSE:
                            flags.deleteOnClose = true;
                            break;
                        case SPARSE:
                            break;
                        case SYNC:
                            flags.sync = true;
                            break;
                        case DSYNC:
                            flags.dsync = true;
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
        if (!flags.read && !flags.write) {
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
        if (!flags.read && !flags.write) {
            flags.read = true;
        }
        if (!flags.append) {
            return SimpleAsynchronousFileChannelImpl.open(open(-1, path, null, flags, mode), flags.read, flags.write, pool);
        }
        throw new UnsupportedOperationException("APPEND not allowed");
    }

    protected static FileDescriptor open(int dfd, UnixPath path, String pathForPermissionCheck, Flags flags, int mode) throws UnixException {
        int oflags;
        int fd;
        if (!flags.read || !flags.write) {
            oflags = flags.write != 0 ? UnixConstants.O_WRONLY : UnixConstants.O_RDONLY;
        } else {
            oflags = UnixConstants.O_RDWR;
        }
        if (flags.write) {
            if (flags.truncateExisting) {
                oflags |= UnixConstants.O_TRUNC;
            }
            if (flags.append) {
                oflags |= UnixConstants.O_APPEND;
            }
            if (flags.createNew) {
                byte[] pathForSysCall = path.asByteArray();
                if (pathForSysCall[pathForSysCall.length - 1] == 46 && (pathForSysCall.length == 1 || pathForSysCall[pathForSysCall.length - 2] == 47)) {
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
                } catch (UnixException x) {
                    if (!flags.create || x.errno() != UnixConstants.ENOENT) {
                        throw x;
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
                x = e;
                if (flags.createNew && x.errno() == UnixConstants.EISDIR) {
                    x.setError(UnixConstants.EEXIST);
                }
                if (!followLinks && x.errno() == UnixConstants.ELOOP) {
                    x = new UnixException(x.getMessage() + " (NOFOLLOW_LINKS specified)");
                }
                throw x;
            }
        } else {
            fd = UnixNativeDispatcher.open(path, oflags, mode);
        }
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
