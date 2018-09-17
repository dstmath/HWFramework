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
            Set<String> result = new HashSet();
            result.addAll(UnixFileSystem.standardFileAttributeViews());
            result.-java_util_stream_Collectors-mthref-4("dos");
            result.-java_util_stream_Collectors-mthref-4("user");
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

    void copyNonPosixAttributes(int ofd, int nfd) {
        LinuxUserDefinedFileAttributeView.copyExtendedAttributes(ofd, nfd);
    }

    /* JADX WARNING: Missing block: B:7:?, code:
            sun.nio.fs.LinuxNativeDispatcher.endmntent(r2);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    Iterable<UnixMountEntry> getMountEntries(String fstab) {
        ArrayList<UnixMountEntry> entries = new ArrayList();
        long fp;
        try {
            fp = LinuxNativeDispatcher.setmntent(Util.toBytes(fstab), Util.toBytes("r"));
            while (true) {
                UnixMountEntry entry = new UnixMountEntry();
                if (LinuxNativeDispatcher.getmntent(fp, entry) < 0) {
                    break;
                }
                entries.add(entry);
            }
        } catch (UnixException e) {
        } catch (Throwable th) {
            LinuxNativeDispatcher.endmntent(fp);
        }
        return entries;
    }

    Iterable<UnixMountEntry> getMountEntries() {
        return getMountEntries("/proc/mounts");
    }

    FileStore getFileStore(UnixMountEntry entry) throws IOException {
        return new LinuxFileStore(this, entry);
    }
}
