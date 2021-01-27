package android.net.util;

import android.net.INetd;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.util.Log;
import com.android.server.job.controllers.JobStatus;

public class NetdService {
    private static final long BASE_TIMEOUT_MS = 100;
    private static final long MAX_TIMEOUT_MS = 1000;
    private static final String TAG = NetdService.class.getSimpleName();

    public interface NetdCommand {
        void run(INetd iNetd) throws RemoteException;
    }

    public static INetd getInstance() {
        INetd netdInstance = INetd.Stub.asInterface(ServiceManager.getService("netd"));
        if (netdInstance == null) {
            Log.w(TAG, "WARNING: returning null INetd instance.");
        }
        return netdInstance;
    }

    public static INetd get(long maxTimeoutMs) {
        long stop;
        if (maxTimeoutMs == 0) {
            return getInstance();
        }
        if (maxTimeoutMs > 0) {
            stop = SystemClock.elapsedRealtime() + maxTimeoutMs;
        } else {
            stop = JobStatus.NO_LATEST_RUNTIME;
        }
        long timeoutMs = 0;
        while (true) {
            INetd netdInstance = getInstance();
            if (netdInstance != null) {
                return netdInstance;
            }
            long remaining = stop - SystemClock.elapsedRealtime();
            if (remaining <= 0) {
                return null;
            }
            timeoutMs = Math.min(Math.min(BASE_TIMEOUT_MS + timeoutMs, 1000L), remaining);
            try {
                Thread.sleep(timeoutMs);
            } catch (InterruptedException e) {
            }
        }
    }

    public static INetd get() {
        return get(-1);
    }

    public static void run(NetdCommand cmd) {
        while (true) {
            try {
                cmd.run(get());
                break;
            } catch (RemoteException re) {
                String str = TAG;
                Log.e(str, "error communicating with netd: " + re);
            }
        }
    }
}
