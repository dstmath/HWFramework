package android.util;

import android.Manifest;
import android.annotation.SystemApi;
import android.content.Context;
import android.os.IStatsManager;
import android.os.RemoteException;
import android.os.ServiceManager;

public final class StatsLog extends StatsLogInternal {
    private static final boolean DEBUG = false;
    private static final String TAG = "StatsLog";
    private static Object sLogLock = new Object();
    private static IStatsManager sService;

    @SystemApi
    public static native void writeRaw(byte[] bArr, int i);

    private StatsLog() {
    }

    public static boolean logStart(int label) {
        synchronized (sLogLock) {
            try {
                IStatsManager service = getIStatsManagerLocked();
                if (service == null) {
                    return false;
                }
                service.sendAppBreadcrumbAtom(label, 3);
                return true;
            } catch (RemoteException e) {
                sService = null;
                return false;
            }
        }
    }

    public static boolean logStop(int label) {
        synchronized (sLogLock) {
            try {
                IStatsManager service = getIStatsManagerLocked();
                if (service == null) {
                    return false;
                }
                service.sendAppBreadcrumbAtom(label, 2);
                return true;
            } catch (RemoteException e) {
                sService = null;
                return false;
            }
        }
    }

    public static boolean logEvent(int label) {
        synchronized (sLogLock) {
            try {
                IStatsManager service = getIStatsManagerLocked();
                if (service == null) {
                    return false;
                }
                service.sendAppBreadcrumbAtom(label, 1);
                return true;
            } catch (RemoteException e) {
                sService = null;
                return false;
            }
        }
    }

    public static boolean logBinaryPushStateChanged(String trainName, long trainVersionCode, int options, int state, long[] experimentIds) {
        synchronized (sLogLock) {
            try {
                IStatsManager service = getIStatsManagerLocked();
                if (service == null) {
                    return false;
                }
                service.sendBinaryPushStateChangedAtom(trainName, trainVersionCode, options, state, experimentIds);
                return true;
            } catch (RemoteException e) {
                sService = null;
                return false;
            }
        }
    }

    public static boolean logWatchdogRollbackOccurred(int rollbackType, String packageName, long packageVersionCode) {
        synchronized (sLogLock) {
            try {
                IStatsManager service = getIStatsManagerLocked();
                if (service == null) {
                    return false;
                }
                service.sendWatchdogRollbackOccurredAtom(rollbackType, packageName, packageVersionCode);
                return true;
            } catch (RemoteException e) {
                sService = null;
                return false;
            }
        }
    }

    private static IStatsManager getIStatsManagerLocked() throws RemoteException {
        IStatsManager iStatsManager = sService;
        if (iStatsManager != null) {
            return iStatsManager;
        }
        sService = IStatsManager.Stub.asInterface(ServiceManager.getService(Context.STATS_MANAGER));
        return sService;
    }

    private static void enforceDumpCallingPermission(Context context) {
        context.enforceCallingPermission(Manifest.permission.DUMP, "Need DUMP permission.");
    }

    private static void enforcesageStatsCallingPermission(Context context) {
        context.enforceCallingPermission(Manifest.permission.PACKAGE_USAGE_STATS, "Need PACKAGE_USAGE_STATS permission.");
    }
}
