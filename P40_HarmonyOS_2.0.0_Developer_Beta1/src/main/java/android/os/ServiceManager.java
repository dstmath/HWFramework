package android.os;

import android.annotation.UnsupportedAppUsage;
import android.util.ArrayMap;
import android.util.Log;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.os.BinderInternal;
import com.android.internal.util.StatLogger;
import com.huawei.android.hwdfu.ServiceManagerUtil;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public final class ServiceManager {
    private static final int GET_SERVICE_LOG_EVERY_CALLS_CORE = SystemProperties.getInt("debug.servicemanager.log_calls_core", 100);
    private static final int GET_SERVICE_LOG_EVERY_CALLS_NON_CORE = SystemProperties.getInt("debug.servicemanager.log_calls", 200);
    private static final long GET_SERVICE_SLOW_THRESHOLD_US_CORE = ((long) (SystemProperties.getInt("debug.servicemanager.slow_call_core_ms", 10) * 1000));
    private static final long GET_SERVICE_SLOW_THRESHOLD_US_NON_CORE = ((long) (SystemProperties.getInt("debug.servicemanager.slow_call_ms", 50) * 1000));
    private static final int SLOW_LOG_INTERVAL_MS = 5000;
    private static final int STATS_LOG_INTERVAL_MS = 5000;
    private static final int STAT_DUMP_MAX_RANK = 10;
    private static final int STAT_MAP_INIT_SIZE = 100;
    private static final int STAT_STACK_MAX_DEPTH = 10;
    private static final String TAG = "ServiceManager";
    @UnsupportedAppUsage
    private static Map<String, IBinder> sCache = new ArrayMap();
    private static long sDumpStatsInterval = -1;
    @GuardedBy({"sLock"})
    private static int sGetServiceAccumulatedCallCount;
    @GuardedBy({"sLock"})
    private static int sGetServiceAccumulatedUs;
    private static BiFunction<String, Integer, Integer> sIncrementBf = $$Lambda$ServiceManager$ZZaA1aiBltezxl_gb5GnxlXvcxQ.INSTANCE;
    private static long sLastDumpStatsTime;
    @GuardedBy({"sLock"})
    private static long sLastSlowLogActualTime;
    @GuardedBy({"sLock"})
    private static long sLastSlowLogUptime;
    @GuardedBy({"sLock"})
    private static long sLastStatsLogUptime;
    private static final Object sLock = new Object();
    @UnsupportedAppUsage
    private static IServiceManager sServiceManager;
    private static Map<String, Integer> sServiceStatsMap;
    private static Map<String, Integer> sStackStatsMap;
    public static final StatLogger sStatLogger = new StatLogger(new String[]{"getService()"});

    interface Stats {
        public static final int COUNT = 1;
        public static final int GET_SERVICE = 0;
    }

    static /* synthetic */ Integer lambda$static$0(String key, Integer value) {
        if (value == null) {
            return 1;
        }
        return Integer.valueOf(value.intValue() + 1);
    }

    @UnsupportedAppUsage
    private static IServiceManager getIServiceManager() {
        IServiceManager iServiceManager = sServiceManager;
        if (iServiceManager != null) {
            return iServiceManager;
        }
        initGetServiceStats(null);
        sServiceManager = ServiceManagerNative.asInterface(Binder.allowBlocking(BinderInternal.getContextObject()));
        return sServiceManager;
    }

    @UnsupportedAppUsage
    public static IBinder getService(String name) {
        try {
            IBinder service = sCache.get(name);
            ServiceManagerUtil.registerService(name);
            if (service != null) {
                return service;
            }
            IBinder ibinder = Binder.allowBlocking(rawGetService(name));
            if (!(sServiceStatsMap == null || sStackStatsMap == null)) {
                statGetServiceAndDump(name);
            }
            return ibinder;
        } catch (RemoteException e) {
            Log.e(TAG, "error in getService", e);
            return null;
        }
    }

    public static IBinder getServiceOrThrow(String name) throws ServiceNotFoundException {
        IBinder binder = getService(name);
        ServiceManagerUtil.registerService(name);
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
            ServiceManagerUtil.addService(name);
        } catch (RemoteException e) {
            Log.e(TAG, "error in addService", e);
        }
    }

    @UnsupportedAppUsage
    public static IBinder checkService(String name) {
        try {
            IBinder service = sCache.get(name);
            ServiceManagerUtil.registerService(name);
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

    private static void initGetServiceStats(String processName) {
        String statConfig = SystemProperties.get("ro.config.hw_getservice_stat", "");
        if (!statConfig.isEmpty()) {
            String process = processName;
            if (!(process == null && (process = Process.getCmdlineForPid(Process.myPid())) == null)) {
                String[] configs = statConfig.split("\\|");
                for (int i = 1; i < configs.length; i++) {
                    if (process.equals(configs[i])) {
                        try {
                            sDumpStatsInterval = Long.valueOf(configs[0]).longValue();
                            sLastDumpStatsTime = SystemClock.uptimeMillis();
                            sServiceStatsMap = new ConcurrentHashMap(100);
                            sStackStatsMap = new ConcurrentHashMap(100);
                            Log.i(TAG, "initGetServiceStats for " + process);
                            return;
                        } catch (NumberFormatException e) {
                            Log.w(TAG, "initGetServiceStats invalid config " + configs[0]);
                            return;
                        }
                    }
                }
            }
        }
    }

    private static void statGetServiceAndDump(String serviceName) {
        sServiceStatsMap.compute(serviceName, sIncrementBf);
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        int dumpSize = 10;
        if (stackTrace.length > 4) {
            int limit = stackTrace.length > 10 ? 10 : stackTrace.length;
            StringBuilder sb = new StringBuilder();
            for (int i = 4; i < limit; i++) {
                sb.append(stackTrace[i].toString());
                sb.append(System.lineSeparator());
            }
            sStackStatsMap.compute(sb.toString(), sIncrementBf);
        }
        long nowUptime = SystemClock.uptimeMillis();
        if (nowUptime - sLastDumpStatsTime > sDumpStatsInterval) {
            List<Map.Entry<String, Integer>> stats = (List) sServiceStatsMap.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).collect(Collectors.toList());
            int dumpSize2 = stats.size() > 10 ? 10 : stats.size();
            for (int i2 = 0; i2 < dumpSize2; i2++) {
                Log.i(TAG, "getService service sum " + stats.get(i2).getValue() + " for " + stats.get(i2).getKey());
            }
            Log.i(TAG, "getService service total count " + stats.size());
            List<Map.Entry<String, Integer>> stats2 = (List) sStackStatsMap.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).collect(Collectors.toList());
            if (stats2.size() <= 10) {
                dumpSize = stats2.size();
            }
            for (int i3 = 0; i3 < dumpSize; i3++) {
                Log.i(TAG, "getService stack sum " + stats2.get(i3).getValue() + " for " + stats2.get(i3).getKey());
            }
            Log.i(TAG, "getService stack total count " + stats2.size());
            sServiceStatsMap.clear();
            sStackStatsMap.clear();
            sLastDumpStatsTime = nowUptime;
        }
    }

    public static void handleApplicationBinded(String processName) {
        initGetServiceStats(processName);
    }
}
