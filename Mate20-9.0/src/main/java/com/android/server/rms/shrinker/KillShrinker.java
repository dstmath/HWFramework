package com.android.server.rms.shrinker;

import android.os.Bundle;
import android.os.Process;
import android.util.Log;
import com.android.server.rms.IShrinker;

public class KillShrinker implements IShrinker {
    public static final String PID_KEY = "pid";
    static final String TAG = "RMS.KillShrinker";

    public int reclaim(String reason, Bundle extras) {
        if (extras == null) {
            return 0;
        }
        int pid = extras.getInt("pid");
        if (pid > 0) {
            Log.w(TAG, reason + " kill pid=" + pid);
            Process.killProcess(pid);
        }
        return 1;
    }

    public void interrupt() {
    }
}
