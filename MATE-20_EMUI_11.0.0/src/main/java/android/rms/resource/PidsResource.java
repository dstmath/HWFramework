package android.rms.resource;

import android.os.StrictMode;
import android.rms.HwSysResImpl;
import android.rms.utils.Utils;
import android.util.Log;
import java.io.File;

public final class PidsResource extends HwSysResImpl {
    private static final String DAEMON_PIDS_CGROUP = "/sys/fs/cgroup/pids/daemon_proc/cgroup.procs";
    private static final String DAEMON_PIDS_CGROUP_LIMIT = "/sys/fs/cgroup/pids/daemon_proc/pids.group_limit";
    private static final String PIDS_CGROUP = "/sys/fs/cgroup/pids/unconfirm_app/cgroup.procs";
    private static final String PIDS_CGROUP_LIMIT = "/sys/fs/cgroup/pids/unconfirm_app/pids.group_limit";
    private static final String PIDS_HISTORY = "/data/log/reliability/rms_log/";
    private static final String TAG = "RMS.PidsResource";
    private static final int[] WHITE_LIST_TYPES = {0};
    private static String[] pidsCgroupLimitList = {PIDS_CGROUP_LIMIT, DAEMON_PIDS_CGROUP_LIMIT};
    private static PidsResource pidsResource;
    private volatile boolean mIsValid = false;

    private PidsResource() {
        super(15, TAG, WHITE_LIST_TYPES);
    }

    public static synchronized PidsResource getInstance() {
        PidsResource pidsResource2;
        synchronized (PidsResource.class) {
            if (pidsResource == null) {
                pidsResource = new PidsResource();
                pidsResource.getConfig();
                if (Utils.DEBUG) {
                    Log.d(TAG, "PidsResource create new resource");
                }
            }
            pidsResource2 = pidsResource;
        }
        return pidsResource2;
    }

    /* JADX INFO: finally extract failed */
    @Override // android.rms.HwSysResImpl
    public int acquire(int tgid, String pkg, int processTpye) {
        if (Utils.DEBUG) {
            Log.v(TAG, "acquire valid=" + this.mIsValid + "tgid=" + tgid + "type=" + processTpye);
        }
        StrictMode.ThreadPolicy savedPolicy = StrictMode.allowThreadDiskReads();
        try {
            if (this.mIsValid && processTpye == 0 && !isInWhiteList(pkg, 0)) {
                Utils.writeFile(PIDS_CGROUP, String.valueOf(tgid));
            } else if (this.mIsValid && processTpye == 2) {
                Utils.writeFile(DAEMON_PIDS_CGROUP, String.valueOf(tgid));
            }
            StrictMode.setThreadPolicy(savedPolicy);
            return 1;
        } catch (Throwable th) {
            StrictMode.setThreadPolicy(savedPolicy);
            throw th;
        }
    }

    public void init(String[] args) {
        int count;
        if (args != null && (count = args.length) > 0) {
            Utils.generateDirectory(PIDS_HISTORY);
            for (int i = 0; i < count; i++) {
                this.mIsValid = new File(pidsCgroupLimitList[i]).exists();
                if (this.mIsValid) {
                    Utils.writeFile(pidsCgroupLimitList[i], args[i]);
                }
            }
        }
    }
}
