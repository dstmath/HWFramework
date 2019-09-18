package com.android.server.rms.shrinker;

import android.os.Bundle;
import android.util.Log;
import com.android.server.am.ProcessList;
import com.android.server.rms.IShrinker;

public class ProcessShrinker implements IShrinker {
    public static final String PID_KEY = "pid";
    public static final int RECLAIM_ALL_MODE = 2;
    public static final int RECLAIM_ANON_MODE = 1;
    static final String TAG = "RMS.ProcessShrinker";
    private int mMode = 1;

    public ProcessShrinker(int mode) {
        this.mMode = mode;
    }

    public int reclaim(String reason, Bundle extras) {
        if (extras == null) {
            return 0;
        }
        int pid = extras.getInt("pid");
        if (pid > 0) {
            Log.w(TAG, reason + " pid=" + pid + " mMode=" + this.mMode);
            ProcessList.callProcReclaim(pid, this.mMode);
        }
        return 1;
    }

    public void interrupt() {
    }
}
