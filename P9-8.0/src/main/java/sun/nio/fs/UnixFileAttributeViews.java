package sun.nio.fs;

import java.io.IOException;
import java.nio.file.ProviderMismatchException;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipal;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

class UnixFileAttributeViews {

    static class Basic extends AbstractBasicFileAttributeView {
        protected final UnixPath file;
        protected final boolean followLinks;

        Basic(UnixPath file, boolean followLinks) {
            this.file = file;
            this.followLinks = followLinks;
        }

        public BasicFileAttributes readAttributes() throws IOException {
            this.file.checkRead();
            try {
                return UnixFileAttributes.get(this.file, this.followLinks).asBasicFileAttributes();
            } catch (UnixException x) {
                x.rethrowAsIOException(this.file);
                return null;
            }
        }

        public void setTimes(FileTime lastModifiedTime, FileTime lastAccessTime, FileTime createTime) throws IOException {
            if (lastModifiedTime != null || lastAccessTime != null) {
                this.file.checkWrite();
                int fd = this.file.openForAttributeAccess(this.followLinks);
                if (lastModifiedTime == null || lastAccessTime == null) {
                    try {
                        UnixFileAttributes attrs = UnixFileAttributes.get(fd);
                        if (lastModifiedTime == null) {
                            lastModifiedTime = attrs.lastModifiedTime();
                        }
                        if (lastAccessTime == null) {
                            lastAccessTime = attrs.lastAccessTime();
                        }
                    } catch (UnixException x) {
                        x.rethrowAsIOException(this.file);
                    } catch (Throwable th) {
                        UnixNativeDispatcher.close(fd);
                    }
                }
                long modValue = lastModifiedTime.to(TimeUnit.MICROSECONDS);
                long accessValue = lastAccessTime.to(TimeUnit.MICROSECONDS);
                boolean retry = false;
                try {
                    if (UnixNativeDispatcher.futimesSupported()) {
                        UnixNativeDispatcher.futimes(fd, accessValue, modValue);
                    } else {
                        UnixNativeDispatcher.utimes(this.file, accessValue, modValue);
                    }
                } catch (UnixException x2) {
                    if (x2.errno() != UnixConstants.EINVAL || (modValue >= 0 && accessValue >= 0)) {
                        x2.rethrowAsIOException(this.file);
                    } else {
                        retry = true;
                    }
                }
                if (retry) {
                    if (modValue < 0) {
                        modValue = 0;
                    }
                    if (accessValue < 0) {
                        accessValue = 0;
                    }
                    try {
                        if (UnixNativeDispatcher.futimesSupported()) {
                            UnixNativeDispatcher.futimes(fd, accessValue, modValue);
                        } else {
                            UnixNativeDispatcher.utimes(this.file, accessValue, modValue);
                        }
                    } catch (UnixException x22) {
                        x22.rethrowAsIOException(this.file);
                    }
                }
                UnixNativeDispatcher.close(fd);
            }
        }
    }

    private static class Posix extends Basic implements PosixFileAttributeView {
        private static final String GROUP_NAME = "group";
        private static final String OWNER_NAME = "owner";
        private static final String PERMISSIONS_NAME = "permissions";
        static final Set<String> posixAttributeNames = Util.newSet(basicAttributeNames, PERMISSIONS_NAME, OWNER_NAME, GROUP_NAME);

        Posix(UnixPath file, boolean followLinks) {
            super(file, followLinks);
        }

        final void checkReadExtended() {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                this.file.checkRead();
                sm.checkPermission(new RuntimePermission("accessUserInformation"));
            }
        }

        final void checkWriteExtended() {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                this.file.checkWrite();
                sm.checkPermission(new RuntimePermission("accessUserInformation"));
            }
        }

        public String name() {
            return "posix";
        }

        public void setAttribute(String attribute, Object value) throws IOException {
            if (attribute.equals(PERMISSIONS_NAME)) {
                setPermissions((Set) value);
            } else if (attribute.equals(OWNER_NAME)) {
                setOwner((UserPrincipal) value);
            } else if (attribute.equals(GROUP_NAME)) {
                setGroup((GroupPrincipal) value);
            } else {
                super.setAttribute(attribute, value);
            }
        }

        final void addRequestedPosixAttributes(PosixFileAttributes attrs, AttributesBuilder builder) {
            addRequestedBasicAttributes(attrs, builder);
            if (builder.match(PERMISSIONS_NAME)) {
                builder.add(PERMISSIONS_NAME, attrs.permissions());
            }
            if (builder.match(OWNER_NAME)) {
                builder.add(OWNER_NAME, attrs.owner());
            }
            if (builder.match(GROUP_NAME)) {
                builder.add(GROUP_NAME, attrs.group());
            }
        }

        public Map<String, Object> readAttributes(String[] requested) throws IOException {
            AttributesBuilder builder = AttributesBuilder.create(posixAttributeNames, requested);
            addRequestedPosixAttributes(readAttributes(), builder);
            return builder.unmodifiableMap();
        }

        public UnixFileAttributes readAttributes() throws IOException {
            checkReadExtended();
            try {
                return UnixFileAttributes.get(this.file, this.followLinks);
            } catch (UnixException x) {
                x.rethrowAsIOException(this.file);
                return null;
            }
        }

        final void setMode(int mode) throws IOException {
            checkWriteExtended();
            int fd;
            try {
                if (this.followLinks) {
                    UnixNativeDispatcher.chmod(this.file, mode);
                    return;
                }
                fd = this.file.openForAttributeAccess(false);
                UnixNativeDispatcher.fchmod(fd, mode);
                UnixNativeDispatcher.close(fd);
            } catch (UnixException x) {
                x.rethrowAsIOException(this.file);
            } catch (Throwable th) {
                UnixNativeDispatcher.close(fd);
            }
        }

        final void setOwners(int uid, int gid) throws IOException {
            checkWriteExtended();
            try {
                if (this.followLinks) {
                    UnixNativeDispatcher.chown(this.file, uid, gid);
                } else {
                    UnixNativeDispatcher.lchown(this.file, uid, gid);
                }
            } catch (UnixException x) {
                x.rethrowAsIOException(this.file);
            }
        }

        public void setPermissions(Set<PosixFilePermission> perms) throws IOException {
            setMode(UnixFileModeAttribute.toUnixMode(perms));
        }

        public void setOwner(UserPrincipal owner) throws IOException {
            if (owner == null) {
                throw new NullPointerException("'owner' is null");
            } else if (!(owner instanceof User)) {
                throw new ProviderMismatchException();
            } else if (owner instanceof Group) {
                throw new IOException("'owner' parameter can't be a group");
            } else {
                setOwners(((User) owner).uid(), -1);
            }
        }

        public UserPrincipal getOwner() throws IOException {
            return readAttributes().owner();
        }

        public void setGroup(GroupPrincipal group) throws IOException {
            if (group == null) {
                throw new NullPointerException("'owner' is null");
            } else if (group instanceof Group) {
                setOwners(-1, ((Group) group).gid());
            } else {
                throw new ProviderMismatchException();
            }
        }
    }

    private static class Unix extends Posix {
        private static final String CTIME_NAME = "ctime";
        private static final String DEV_NAME = "dev";
        private static final String GID_NAME = "gid";
        private static final String INO_NAME = "ino";
        private static final String MODE_NAME = "mode";
        private static final String NLINK_NAME = "nlink";
        private static final String RDEV_NAME = "rdev";
        private static final String UID_NAME = "uid";
        static final Set<String> unixAttributeNames = Util.newSet(posixAttributeNames, MODE_NAME, INO_NAME, DEV_NAME, RDEV_NAME, NLINK_NAME, UID_NAME, GID_NAME, CTIME_NAME);

        Unix(UnixPath file, boolean followLinks) {
            super(file, followLinks);
        }

        public String name() {
            return "unix";
        }

        public void setAttribute(String attribute, Object value) throws IOException {
            if (attribute.equals(MODE_NAME)) {
                setMode(((Integer) value).lambda$-java_util_stream_IntPipeline_14709());
            } else if (attribute.equals(UID_NAME)) {
                setOwners(((Integer) value).lambda$-java_util_stream_IntPipeline_14709(), -1);
            } else if (attribute.equals(GID_NAME)) {
                setOwners(-1, ((Integer) value).lambda$-java_util_stream_IntPipeline_14709());
            } else {
                super.setAttribute(attribute, value);
            }
        }

        public Map<String, Object> readAttributes(String[] requested) throws IOException {
            AttributesBuilder builder = AttributesBuilder.create(unixAttributeNames, requested);
            UnixFileAttributes attrs = readAttributes();
            addRequestedPosixAttributes(attrs, builder);
            if (builder.match(MODE_NAME)) {
                builder.add(MODE_NAME, Integer.valueOf(attrs.mode()));
            }
            if (builder.match(INO_NAME)) {
                builder.add(INO_NAME, Long.valueOf(attrs.ino()));
            }
            if (builder.match(DEV_NAME)) {
                builder.add(DEV_NAME, Long.valueOf(attrs.dev()));
            }
            if (builder.match(RDEV_NAME)) {
                builder.add(RDEV_NAME, Long.valueOf(attrs.rdev()));
            }
            if (builder.match(NLINK_NAME)) {
                builder.add(NLINK_NAME, Integer.valueOf(attrs.nlink()));
            }
            if (builder.match(UID_NAME)) {
                builder.add(UID_NAME, Integer.valueOf(attrs.uid()));
            }
            if (builder.match(GID_NAME)) {
                builder.add(GID_NAME, Integer.valueOf(attrs.gid()));
            }
            if (builder.match(CTIME_NAME)) {
                builder.add(CTIME_NAME, attrs.ctime());
            }
            return builder.unmodifiableMap();
        }
    }

    UnixFileAttributeViews() {
    }

    static Basic createBasicView(UnixPath file, boolean followLinks) {
        return new Basic(file, followLinks);
    }

    static Posix createPosixView(UnixPath file, boolean followLinks) {
        return new Posix(file, followLinks);
    }

    static Unix createUnixView(UnixPath file, boolean followLinks) {
        return new Unix(file, followLinks);
    }

    static FileOwnerAttributeViewImpl createOwnerView(UnixPath file, boolean followLinks) {
        return new FileOwnerAttributeViewImpl(createPosixView(file, followLinks));
    }
}
