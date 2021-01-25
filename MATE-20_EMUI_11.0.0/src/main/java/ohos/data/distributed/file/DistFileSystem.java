package ohos.data.distributed.file;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

class DistFileSystem {
    static final HiLogLabel LABEL = new HiLogLabel(3, 218109440, "DistFileSystem");
    static String ROOT_DIST_PATH;

    static native int createAgentFile(String str, String str2, String str3);

    static native int createLinkFile(String str, String str2);

    static native String getRootDir();

    static native String getXattr(String str, String str2);

    DistFileSystem() {
    }

    static {
        try {
            System.loadLibrary("distributedfile.z");
            ROOT_DIST_PATH = getRootDir();
        } catch (UnsatisfiedLinkError unused) {
            HiLog.error(LABEL, "Failed to load libdistributedfile.z.so", new Object[0]);
        }
    }
}
