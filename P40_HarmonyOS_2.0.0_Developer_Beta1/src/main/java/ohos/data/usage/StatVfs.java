package ohos.data.usage;

import java.io.File;
import java.io.IOException;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class StatVfs {
    private static final long INVALID = -1;
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218109441, "StatVfs");
    private static final String LIB_NAME = "datausage.z";
    private StatVfsInfo info;

    private native StatVfsInfo getStatVfsInfo(String str);

    public static boolean isSupported() {
        return true;
    }

    static {
        System.loadLibrary(LIB_NAME);
    }

    public StatVfs(String str) {
        init(str);
    }

    public void reStatVfs(String str) {
        init(str);
    }

    public long getSpace() {
        StatVfsInfo statVfsInfo = this.info;
        if (statVfsInfo == null) {
            return -1;
        }
        return statVfsInfo.getTotalBytes();
    }

    public long getFreeSpace() {
        StatVfsInfo statVfsInfo = this.info;
        if (statVfsInfo == null) {
            return -1;
        }
        return statVfsInfo.getFreeBytes();
    }

    public long getAvailableSpace() {
        StatVfsInfo statVfsInfo = this.info;
        if (statVfsInfo == null) {
            return -1;
        }
        return statVfsInfo.getAvailableBytes();
    }

    private void init(String str) {
        if (str == null) {
            HiLog.info(LABEL, "path is null", new Object[0]);
            return;
        }
        try {
            this.info = getStatVfsInfo(new File(str).getCanonicalPath());
            if (this.info == null) {
                HiLog.info(LABEL, "native return null info", new Object[0]);
            }
        } catch (IOException unused) {
            HiLog.info(LABEL, "getCanonicalPath failed", new Object[0]);
        }
    }
}
