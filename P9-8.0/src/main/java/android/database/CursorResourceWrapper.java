package android.database;

import android.common.HwFrameworkFactory;
import android.content.Context;
import android.os.Process;
import android.rms.HwSysResource;
import android.util.Log;

public class CursorResourceWrapper {
    private static final boolean DEBUG = false;
    private static final String TAG = "CursorResourceWrapper";
    private HwSysResource mCursorResouce;
    private int mPid;
    private String mProcessName;

    public CursorResourceWrapper(Context context) {
        if (!(context == null || context.getApplicationInfo() == null)) {
            this.mProcessName = context.getApplicationInfo().processName;
        }
        this.mPid = Process.myPid();
    }

    public boolean acquireLocked(int pid, int uid, int usage) {
        if (this.mCursorResouce == null) {
            this.mCursorResouce = HwFrameworkFactory.getHwResource(16);
        }
        if (this.mCursorResouce == null) {
            return true;
        }
        boolean z;
        StringBuilder token = new StringBuilder();
        token.append(this.mProcessName == null ? "null" : this.mProcessName);
        token.append("-").append(this.mPid);
        token.append(";").append(pid);
        if (1 == this.mCursorResouce.acquire(uid, token.toString(), -1, usage)) {
            z = true;
        } else {
            z = false;
        }
        return z;
    }

    public static boolean isNeedResProtect(Context context) {
        if (context == null) {
            return false;
        }
        try {
            return (context.getApplicationInfo().flags & 1) != 0;
        } catch (Exception e) {
            Log.e(TAG, "context is wrong!\n");
            return false;
        } catch (Throwable th) {
            return false;
        }
    }
}
