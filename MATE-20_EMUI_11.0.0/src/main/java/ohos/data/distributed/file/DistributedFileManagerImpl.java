package ohos.data.distributed.file;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class DistributedFileManagerImpl implements DistributedFileManager {
    static final HiLogLabel LABEL = new HiLogLabel(3, 218109440, "DistributedFileManagerImpl");

    private native String nativeGetBundleDistributedDir(String str);

    private native String nativeGetSystemDistributedDir(String str);

    static {
        try {
            System.loadLibrary("distributedfile.z");
        } catch (UnsatisfiedLinkError e) {
            HiLog.error(LABEL, "load libdistributedfile.z.so UnsatisfiedLinkError: %{public}s", new Object[]{e});
        }
    }

    @Override // ohos.data.distributed.file.DistributedFileManager
    public String getBundleDistributedDir(String str) {
        if (str == null || str.isEmpty()) {
            HiLog.error(LABEL, "bundle name cannot be empty", new Object[0]);
            return "";
        }
        try {
            String nativeGetBundleDistributedDir = nativeGetBundleDistributedDir(str);
            if (nativeGetBundleDistributedDir == null) {
                HiLog.error(LABEL, "bundle %{private}s, cannot get distributed dir", new Object[]{str});
                return "";
            }
            HiLog.info(LABEL, "bundle %{private}s, distributed dir %{private}s", new Object[]{str, nativeGetBundleDistributedDir});
            return nativeGetBundleDistributedDir;
        } catch (UnsatisfiedLinkError e) {
            HiLog.error(LABEL, "nativeGetBundleDistributedDir UnsatisfiedLinkError: %{public}s", new Object[]{e});
            return "";
        }
    }

    @Override // ohos.data.distributed.file.DistributedFileManager
    public String getSystemDistributedDir(String str) {
        if (str == null || str.isEmpty()) {
            HiLog.error(LABEL, "file name cannot be empty", new Object[0]);
            return "";
        }
        try {
            String nativeGetSystemDistributedDir = nativeGetSystemDistributedDir(str);
            if (nativeGetSystemDistributedDir == null) {
                HiLog.error(LABEL, "cannot create distributed dir %{public}s", new Object[]{str});
                return "";
            }
            HiLog.info(LABEL, "create distributed dir %{public}s", new Object[]{nativeGetSystemDistributedDir});
            return nativeGetSystemDistributedDir;
        } catch (UnsatisfiedLinkError e) {
            HiLog.error(LABEL, "nativeGetSystemDistributedDir UnsatisfiedLinkError: %{public}s", new Object[]{e});
            return "";
        }
    }
}
