package android.rms.resource;

import android.os.RemoteException;
import android.rms.HwSysResImpl;
import android.rms.IUpdateWhiteListCallback;
import android.rms.IUpdateWhiteListCallback.Stub;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public final class PidsResource extends HwSysResImpl {
    private static final boolean DEBUG = false;
    private static final String PIDS_CGROUP = "/sys/fs/cgroup/pids/unconfirm_app/cgroup.procs";
    private static final String PIDS_CGROUP_LIMIT = "/sys/fs/cgroup/pids/unconfirm_app/pids.group_limit";
    private static final String TAG = "PidsResource";
    private static PidsResource mPidsResource;
    private IUpdateWhiteListCallback mUpdateWhiteListCallback;
    private volatile boolean mValid;
    private ArrayList<String> mWhiteList;

    private PidsResource() {
        this.mValid = DEBUG;
        this.mWhiteList = null;
        this.mUpdateWhiteListCallback = new Stub() {
            public void update() throws RemoteException {
                PidsResource Resource = PidsResource.getInstance();
                if (Resource == null) {
                    Log.e(PidsResource.TAG, "Pids Resource update get the instance is null");
                    return;
                }
                ArrayList<String> tempWhiteList = Resource.getResWhiteList(16, 0);
                if (tempWhiteList.size() != 0) {
                    PidsResource.this.mWhiteList.clear();
                    PidsResource.this.mWhiteList = tempWhiteList;
                } else {
                    Log.e(PidsResource.TAG, "Pids Resource update nameList failed!!!");
                }
            }
        };
        if (!registerResourceCallback(this.mUpdateWhiteListCallback)) {
            Log.e(TAG, "Pids Resource register callback failed");
        }
        this.mWhiteList = super.getResWhiteList(16, 0);
    }

    public static synchronized PidsResource getInstance() {
        PidsResource pidsResource;
        synchronized (PidsResource.class) {
            if (mPidsResource == null) {
                mPidsResource = new PidsResource();
            }
            pidsResource = mPidsResource;
        }
        return pidsResource;
    }

    public int acquire(int tgid, String pkg, int processTpye) {
        if (this.mValid && processTpye == 0 && !isWhiteList(pkg)) {
            writeFile(PIDS_CGROUP, String.valueOf(tgid));
        }
        return 1;
    }

    public final void init(String args) {
        String unconfrim_app_limit = args;
        this.mValid = new File(PIDS_CGROUP_LIMIT).exists();
        if (this.mValid) {
            writeFile(PIDS_CGROUP_LIMIT, args);
        }
    }

    private final boolean writeFile(String path, String data) {
        Throwable th;
        FileOutputStream fileOutputStream = null;
        boolean success = true;
        try {
            FileOutputStream fos = new FileOutputStream(path);
            try {
                fos.write(data.getBytes());
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        Log.w(TAG, "writeFile : IOException when close");
                    }
                }
                fileOutputStream = fos;
            } catch (IOException e2) {
                fileOutputStream = fos;
                try {
                    Log.w(TAG, "Unable to write " + path);
                    success = DEBUG;
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (IOException e3) {
                            Log.w(TAG, "writeFile : IOException when close");
                        }
                    }
                    return success;
                } catch (Throwable th2) {
                    th = th2;
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (IOException e4) {
                            Log.w(TAG, "writeFile : IOException when close");
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                fileOutputStream = fos;
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                throw th;
            }
        } catch (IOException e5) {
            Log.w(TAG, "Unable to write " + path);
            success = DEBUG;
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
            return success;
        }
        return success;
    }

    private boolean isWhiteList(String pkg) {
        if (pkg == null) {
            return DEBUG;
        }
        for (String proc : this.mWhiteList) {
            if (pkg.contains(proc)) {
                return true;
            }
        }
        return DEBUG;
    }
}
