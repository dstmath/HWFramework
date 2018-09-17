package com.android.server.rms.handler;

import android.os.Process;
import android.util.Slog;
import com.android.server.HwBinderMonitor;
import com.android.server.rms.IDaemonRecoverHandler;

public class DaemonRecoverHandler implements IDaemonRecoverHandler {
    static final String TAG = "DaemonRecoverHandler";
    private static HwBinderMonitor mIBinderM = new HwBinderMonitor();

    public DaemonRecoverHandler() {
        Slog.i(TAG, "SFSentinel init..");
    }

    public boolean isProcessBlocked(String[] cmdlines) {
        int[] pids = Process.getPidsForCommands(cmdlines);
        for (int i = 0; i < pids.length; i++) {
            if (mIBinderM.catchBadproc(pids[i], 2) == pids[i]) {
                Slog.w(TAG, "Found process blocked: PID:" + pids[i] + " cmd: " + cmdlines[i]);
                return true;
            }
        }
        return false;
    }
}
