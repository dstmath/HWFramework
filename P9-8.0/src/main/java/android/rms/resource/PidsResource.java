package android.rms.resource;

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
    private static String[] mPidsCgroupLimitList = new String[]{PIDS_CGROUP_LIMIT, DAEMON_PIDS_CGROUP_LIMIT};
    private static PidsResource mPidsResource;
    private static final int[] mWhiteListTypes = new int[]{0};
    private volatile boolean mValid = false;

    private PidsResource() {
        super(15, TAG, mWhiteListTypes);
    }

    public static synchronized PidsResource getInstance() {
        PidsResource pidsResource;
        synchronized (PidsResource.class) {
            if (mPidsResource == null) {
                mPidsResource = new PidsResource();
                mPidsResource.getConfig();
                if (Utils.DEBUG) {
                    Log.d(TAG, "PidsResource create new resource");
                }
            }
            pidsResource = mPidsResource;
        }
        return pidsResource;
    }

    public int acquire(int tgid, String pkg, int processTpye) {
        if (Utils.DEBUG) {
            Log.v(TAG, "acquire valid=" + this.mValid + "tgid=" + tgid + "type=" + processTpye);
        }
        if (this.mValid && processTpye == 0 && (isInWhiteList(pkg, 0) ^ 1) != 0) {
            Utils.writeFile(PIDS_CGROUP, String.valueOf(tgid));
        } else if (this.mValid && 2 == processTpye) {
            Utils.writeFile(DAEMON_PIDS_CGROUP, String.valueOf(tgid));
        }
        return 1;
    }

    public final void init(String[] args) {
        int count = args.length;
        if (count > 0) {
            Utils.generateDirectory(PIDS_HISTORY);
            for (int i = 0; i < count; i++) {
                this.mValid = new File(mPidsCgroupLimitList[i]).exists();
                if (this.mValid) {
                    Utils.writeFile(mPidsCgroupLimitList[i], args[i]);
                }
            }
        }
    }
}
