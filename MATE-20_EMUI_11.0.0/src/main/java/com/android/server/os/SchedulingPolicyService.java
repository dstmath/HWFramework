package com.android.server.os;

import android.os.Binder;
import android.os.IBinder;
import android.os.ISchedulingPolicyService;
import android.os.Process;
import android.util.Log;
import com.android.server.SystemServerInitThreadPool;
import com.android.server.policy.WindowManagerPolicy;

public class SchedulingPolicyService extends ISchedulingPolicyService.Stub {
    private static final String[] MEDIA_PROCESS_NAMES = {"media.swcodec"};
    private static final int PRIORITY_MAX = 3;
    private static final int PRIORITY_MIN = 1;
    private static final String TAG = "SchedulingPolicyService";
    private int mBoostedPid = -1;
    private IBinder mClient;
    private final IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        /* class com.android.server.os.SchedulingPolicyService.AnonymousClass1 */

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            SchedulingPolicyService.this.requestCpusetBoost(false, null);
        }
    };

    public SchedulingPolicyService() {
        SystemServerInitThreadPool.get().submit(new Runnable() {
            /* class com.android.server.os.$$Lambda$SchedulingPolicyService$ao2OiSvvlyzmJ0li0c0nhHyIDk */

            @Override // java.lang.Runnable
            public final void run() {
                SchedulingPolicyService.this.lambda$new$0$SchedulingPolicyService();
            }
        }, "SchedulingPolicyService.<init>");
    }

    public /* synthetic */ void lambda$new$0$SchedulingPolicyService() {
        int[] nativePids;
        synchronized (this.mDeathRecipient) {
            if (this.mBoostedPid == -1 && (nativePids = Process.getPidsForCommands(MEDIA_PROCESS_NAMES)) != null && nativePids.length == 1) {
                this.mBoostedPid = nativePids[0];
                disableCpusetBoost(nativePids[0]);
            }
        }
    }

    public int requestPriority(int pid, int tid, int prio, boolean isForApp) {
        if (!isPermitted() || prio < 1 || prio > 3 || Process.getThreadGroupLeader(tid) != pid) {
            return -1;
        }
        if (Binder.getCallingUid() != 1002) {
            try {
                Process.setThreadGroup(tid, !isForApp ? 4 : 6);
            } catch (RuntimeException e) {
                Log.e(TAG, "Failed setThreadGroup: " + e);
                return -1;
            }
        }
        try {
            Process.setThreadScheduler(tid, WindowManagerPolicy.COLOR_FADE_LAYER, prio);
            return 0;
        } catch (RuntimeException e2) {
            Log.e(TAG, "Failed setThreadScheduler: " + e2);
            return -1;
        }
    }

    public int requestCpusetBoost(boolean enable, IBinder client) {
        if (Binder.getCallingPid() != Process.myPid() && Binder.getCallingUid() != 1013) {
            return -1;
        }
        int[] nativePids = Process.getPidsForCommands(MEDIA_PROCESS_NAMES);
        if (nativePids == null || nativePids.length != 1) {
            Log.e(TAG, "requestCpusetBoost: can't find media.codec process");
            return -1;
        }
        synchronized (this.mDeathRecipient) {
            if (enable) {
                return enableCpusetBoost(nativePids[0], client);
            }
            return disableCpusetBoost(nativePids[0]);
        }
    }

    private int enableCpusetBoost(int pid, IBinder client) {
        if (this.mBoostedPid == pid) {
            return 0;
        }
        this.mBoostedPid = -1;
        IBinder iBinder = this.mClient;
        if (iBinder != null) {
            try {
                iBinder.unlinkToDeath(this.mDeathRecipient, 0);
            } catch (Exception e) {
            } catch (Throwable th) {
                this.mClient = null;
                throw th;
            }
            this.mClient = null;
        }
        try {
            client.linkToDeath(this.mDeathRecipient, 0);
            Log.i(TAG, "Moving " + pid + " to group 5");
            Process.setProcessGroup(pid, 5);
            this.mBoostedPid = pid;
            this.mClient = client;
            return 0;
        } catch (Exception e2) {
            Log.e(TAG, "Failed enableCpusetBoost: " + e2);
            try {
                client.unlinkToDeath(this.mDeathRecipient, 0);
            } catch (Exception e3) {
            }
            return -1;
        }
    }

    private int disableCpusetBoost(int pid) {
        int boostedPid = this.mBoostedPid;
        this.mBoostedPid = -1;
        IBinder iBinder = this.mClient;
        if (iBinder != null) {
            try {
                iBinder.unlinkToDeath(this.mDeathRecipient, 0);
            } catch (Exception e) {
            } catch (Throwable th) {
                this.mClient = null;
                throw th;
            }
            this.mClient = null;
        }
        if (boostedPid == pid) {
            try {
                Log.i(TAG, "Moving " + pid + " back to group default");
                Process.setProcessGroup(pid, -1);
            } catch (Exception e2) {
                Log.w(TAG, "Couldn't move pid " + pid + " back to group default");
            }
        }
        return 0;
    }

    private boolean isPermitted() {
        int callingUid;
        if (Binder.getCallingPid() == Process.myPid() || (callingUid = Binder.getCallingUid()) == 1002 || callingUid == 1041 || callingUid == 1047) {
            return true;
        }
        return false;
    }
}
