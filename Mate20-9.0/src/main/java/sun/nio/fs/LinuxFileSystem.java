package sun.nio.fs;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

class LinuxFileSystem extends UnixFileSystem {

    private static class SupportedFileFileAttributeViewsHolder {
        static final Set<String> supportedFileAttributeViews = supportedFileAttributeViews();

        private SupportedFileFileAttributeViewsHolder() {
        }

        private static Set<String> supportedFileAttributeViews() {
            Set<String> result = new HashSet<>();
            result.addAll(UnixFileSystem.standardFileAttributeViews());
            result.add("dos");
            result.add("user");
            return Collections.unmodifiableSet(result);
        }
    }

    LinuxFileSystem(UnixFileSystemProvider provider, String dir) {
        super(provider, dir);
    }

    public WatchService newWatchService() throws IOException {
        return new LinuxWatchService(this);
    }

    public Set<String> supportedFileAttributeViews() {
        return SupportedFileFileAttributeViewsHolder.supportedFileAttributeViews;
    }

    /* access modifiers changed from: package-private */
    public void copyNonPosixAttributes(int ofd, int nfd) {
        LinuxUserDefinedFileAttributeView.copyExtendedAttributes(ofd, nfd);
    }

    /* access modifiers changed from: package-private */
    public Iterable<UnixMountEntry> getMountEntries(String fstab) {
        long fp;
        ArrayList<UnixMountEntry> entries = new ArrayList<>();
        try {
            fp = LinuxNativeDispatcher.setmntent(Util.toBytes(fstab), Util.toBytes("r"));
            while (true) {
                UnixMountEntry entry = new UnixMountEntry();
                if (LinuxNativeDispatcher.getmntent(fp, entry) < 0) {
                    break;
                }
                entries.add(entry);
            }
            LinuxNativeDispatcher.endmntent(fp);
        } catch (UnixException e) {
        } catch (Throwable th) {
            LinuxNativeDispatcher.endmntent(fp);
            throw th;
        }
        return entries;
    }

    /* access modifiers changed from: package-private */
    public Iterable<UnixMountEntry> getMountEntries() {
        return getMountEntries("/proc/mounts");
    }

    /* access modifiers changed from: package-private */
    public FileStore getFileStore(UnixMountEntry entry) throws IOException {
        return new LinuxFileStore(this, entry);
    }
}
