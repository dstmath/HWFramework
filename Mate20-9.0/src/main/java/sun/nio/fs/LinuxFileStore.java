package sun.nio.fs;

import java.io.IOException;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.util.Arrays;
import sun.nio.fs.UnixFileStore;

class LinuxFileStore extends UnixFileStore {
    private volatile boolean xattrChecked;
    private volatile boolean xattrEnabled;

    LinuxFileStore(UnixPath file) throws IOException {
        super(file);
    }

    LinuxFileStore(UnixFileSystem fs, UnixMountEntry entry) throws IOException {
        super(fs, entry);
    }

    /* access modifiers changed from: package-private */
    public UnixMountEntry findMountEntry() throws IOException {
        LinuxFileSystem fs = (LinuxFileSystem) file().getFileSystem();
        UnixPath path = null;
        try {
            path = new UnixPath((UnixFileSystem) fs, UnixNativeDispatcher.realpath(file()));
        } catch (UnixException x) {
            x.rethrowAsIOException(file());
        }
        for (UnixPath parent = path.getParent(); parent != null; parent = parent.getParent()) {
            UnixFileAttributes attrs = null;
            try {
                attrs = UnixFileAttributes.get(parent, true);
            } catch (UnixException x2) {
                x2.rethrowAsIOException(parent);
            }
            if (attrs.dev() != dev()) {
                break;
            }
            path = parent;
        }
        byte[] dir = path.asByteArray();
        for (UnixMountEntry entry : fs.getMountEntries("/proc/mounts")) {
            if (Arrays.equals(dir, entry.dir())) {
                return entry;
            }
        }
        throw new IOException("Mount point not found");
    }

    private boolean isExtendedAttributesEnabled(UnixPath path) {
        int fd;
        try {
            fd = path.openForAttributeAccess(false);
            try {
                LinuxNativeDispatcher.fgetxattr(fd, Util.toBytes("user.java"), 0, 0);
                UnixNativeDispatcher.close(fd);
                return true;
            } catch (UnixException e) {
                if (e.errno() == UnixConstants.ENODATA) {
                    UnixNativeDispatcher.close(fd);
                    return true;
                }
                UnixNativeDispatcher.close(fd);
                return false;
            }
        } catch (IOException e2) {
        } catch (Throwable th) {
            UnixNativeDispatcher.close(fd);
            throw th;
        }
    }

    public boolean supportsFileAttributeView(Class<? extends FileAttributeView> type) {
        if (type == DosFileAttributeView.class || type == UserDefinedFileAttributeView.class) {
            UnixFileStore.FeatureStatus status = checkIfFeaturePresent("user_xattr");
            if (status == UnixFileStore.FeatureStatus.PRESENT) {
                return true;
            }
            if (status == UnixFileStore.FeatureStatus.NOT_PRESENT) {
                return false;
            }
            if (entry().hasOption("user_xattr")) {
                return true;
            }
            if (entry().fstype().equals("ext3") || entry().fstype().equals("ext4")) {
                return false;
            }
            if (!this.xattrChecked) {
                this.xattrEnabled = isExtendedAttributesEnabled(new UnixPath(file().getFileSystem(), entry().dir()));
                this.xattrChecked = true;
            }
            return this.xattrEnabled;
        } else if (type != PosixFileAttributeView.class || !entry().fstype().equals("vfat")) {
            return super.supportsFileAttributeView(type);
        } else {
            return false;
        }
    }

    public boolean supportsFileAttributeView(String name) {
        if (name.equals("dos")) {
            return supportsFileAttributeView((Class<? extends FileAttributeView>) DosFileAttributeView.class);
        }
        if (name.equals("user")) {
            return supportsFileAttributeView((Class<? extends FileAttributeView>) UserDefinedFileAttributeView.class);
        }
        return super.supportsFileAttributeView(name);
    }
}
