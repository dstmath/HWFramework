package android.os;

import android.annotation.UnsupportedAppUsage;
import android.util.ArrayMap;
import android.util.Log;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.os.BinderInternal;
import com.android.internal.util.StatLogger;
import java.util.Map;

public final class ServiceManager {
    private static final int GET_SERVICE_LOG_EVERY_CALLS_CORE = SystemProperties.getInt("debug.servicemanager.log_calls_core", 100);
    private static final int GET_SERVICE_LOG_EVERY_CALLS_NON_CORE = SystemProperties.getInt("debug.servicemanager.log_calls", 200);
    private static final long GET_SERVICE_SLOW_THRESHOLD_US_CORE = ((long) (SystemProperties.getInt("debug.servicemanager.slow_call_core_ms", 10) * 1000));
    private static final long GET_SERVICE_SLOW_THRESHOLD_US_NON_CORE = ((long) (SystemProperties.getInt("debug.servicemanager.slow_call_ms", 50) * 1000));
    private static final int SLOW_LOG_INTERVAL_MS = 5000;
    private static final int STATS_LOG_INTERVAL_MS = 5000;
    private static final String TAG = "ServiceManager";
    @UnsupportedAppUsage
    private static Map<String, IBinder> sCache = new ArrayMap();
    @GuardedBy({"sLock"})
    private static int sGetServiceAccumulatedCallCount;
    @GuardedBy({"sLock"})
    private static int sGetServiceAccumulatedUs;
    @GuardedBy({"sLock"})
    private static long sLastSlowLogActualTime;
    @GuardedBy({"sLock"})
    private static long sLastSlowLogUptime;
    @GuardedBy({"sLock"})
    private static long sLastStatsLogUptime;
    private static final Object sLock = new Object();
    @UnsupportedAppUsage
    private static IServiceManager sServiceManager;
    public static final StatLogger sStatLogger = new StatLogger(new String[]{"getService()"});

    interface Stats {
        public static final int COUNT = 1;
        public static final int GET_SERVICE = 0;
    }

    @UnsupportedAppUsage
    private static IServiceManager getIServiceManager() {
        IServiceManager iServiceManager = sServiceManager;
        if (iServiceManager != null) {
            return iServiceManager;
        }
        sServiceManager = ServiceManagerNative.asInterface(Binder.allowBlocking(BinderInternal.getContextObject()));
        return sServiceManager;
    }

    @UnsupportedAppUsage
    public static IBinder getService(String name) {
        try {
            IBinder service = sCache.get(name);
            if (service != null) {
                return service;
            }
            return Binder.allowBlocking(rawGetService(name));
        } catch (RemoteException e) {
            Log.e(TAG, "error in getService", e);
            return null;
        }
    }

    public static IBinder getServiceOrThrow(String name) throws ServiceNotFoundException {
        IBinder binder = getService(name);
        if (binder != null) {
            return binder;
        }
        throw new ServiceNotFoundException(name);
    }

    @UnsupportedAppUsage
    public static void addService(String name, IBinder service) {
        addService(name, service, false, 8);
    }

    @UnsupportedAppUsage
    public static void addService(String name, IBinder service, boolean allowIsolated) {
        addService(name, service, allowIsolated, 8);
    }

    @UnsupportedAppUsage
    public static void addService(String name, IBinder service, boolean allowIsolated, int dumpPriority) {
        try {
            getIServiceManager().addService(name, service, allowIsolated, dumpPriority);
        } catch (RemoteException e) {
            Log.e(TAG, "error in addService", e);
        }
    }

    @UnsupportedAppUsage
    public static IBinder checkService(String name) {
        try {
            IBinder service = sCache.get(name);
            if (service != null) {
                return service;
            }
            return Binder.allowBlocking(getIServiceManager().checkService(name));
        } catch (RemoteException e) {
            Log.e(TAG, "error in checkService", e);
            return null;
        }
    }

    @UnsupportedAppUsage
    public static String[] listServices() {
        try {
            return getIServiceManager().listServices(15);
        } catch (RemoteException e) {
            Log.e(TAG, "error in listServices", e);
            return null;
        }
    }

    public static void initServiceCache(Map<String, IBinder> cache) {
        if (sCache.size() == 0) {
            sCache.putAll(cache);
            return;
        }
        throw new IllegalStateException("setServiceCache may only be called once");
    }

    public static class ServiceNotFoundException extends Exception {
        public ServiceNotFoundException(String name) {
            super("No service published for: " + name);
        }
    }

    private static IBinder rawGetService(String name) throws RemoteException {
        long slowThreshold;
        int logInterval;
        long start = sStatLogger.getTime();
        IBinder binder = getIServiceManager().getService(name);
        int time = (int) sStatLogger.logDurationStat(0, start);
        boolean isCore = UserHandle.isCore(Process.myUid());
        if (isCore) {
            slowThreshold = GET_SERVICE_SLOW_THRESHOLD_US_CORE;
        } else {
            slowThreshold = GET_SERVICE_SLOW_THRESHOLD_US_NON_CORE;
        }
        synchronized (sLock) {
            try {
                sGetServiceAccumulatedUs += time;
                sGetServiceAccumulatedCallCount++;
                long nowUptime = SystemClock.uptimeMillis();
                if (((long) time) >= slowThreshold) {
                    try {
                        if (nowUptime > sLastSlowLogUptime + 5000 || sLastSlowLogActualTime < ((long) time)) {
                            EventLogTags.writeServiceManagerSlow(time / 1000, name);
                            sLastSlowLogUptime = nowUptime;
                            sLastSlowLogActualTime = (long) time;
                        }
                    } catch (Throwable th) {
                        th = th;
                        throw th;
                    }
                }
                if (isCore) {
                    logInterval = GET_SERVICE_LOG_EVERY_CALLS_CORE;
                } else {
                    logInterval = GET_SERVICE_LOG_EVERY_CALLS_NON_CORE;
                }
                if (sGetServiceAccumulatedCallCount >= logInterval && nowUptime >= sLastStatsLogUptime + 5000) {
                    EventLogTags.writeServiceManagerStats(sGetServiceAccumulatedCallCount, sGetServiceAccumulatedUs / 1000, (int) (nowUptime - sLastStatsLogUptime));
                    sGetServiceAccumulatedCallCount = 0;
                    sGetServiceAccumulatedUs = 0;
                    sLastStatsLogUptime = nowUptime;
                }
                return binder;
            } catch (Throwable th2) {
                th = th2;
                throw th;
            }
        }
    }
}
