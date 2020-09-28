package android.database;

import android.common.HwFrameworkFactory;
import android.content.Context;
import android.os.Process;
import android.rms.HwSysResource;
import android.util.Log;
import com.android.internal.content.NativeLibraryHelper;

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
        StringBuilder token = new StringBuilder();
        String str = this.mProcessName;
        if (str == null) {
            str = "null";
        }
        token.append(str);
        token.append(NativeLibraryHelper.CLEAR_ABI_OVERRIDE);
        token.append(this.mPid);
        token.append(";");
        token.append(pid);
        if (1 == this.mCursorResouce.acquire(uid, token.toString(), -1, usage)) {
            return true;
        }
        return false;
    }

    public static boolean isNeedResProtect(Context context) {
        if (context == null) {
            return false;
        }
        try {
            boolean ret = true;
            if ((context.getApplicationInfo().flags & 1) == 0) {
                ret = false;
            }
            return ret;
        } catch (Exception e) {
            Log.e(TAG, "context is wrong!\n");
        } catch (Throwable th) {
        }
        return false;
    }
}
