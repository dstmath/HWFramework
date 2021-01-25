package android.net;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.INetworkStackConnector;
import android.net.dhcp.DhcpServingParamsParcel;
import android.net.dhcp.IDhcpServerCallbacks;
import android.net.ip.IIpClientCallbacks;
import android.net.util.SharedLog;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.provider.DeviceConfig;
import android.util.ArraySet;
import android.util.Slog;
import com.android.internal.annotations.GuardedBy;
import com.android.server.connectivity.NetworkAgentInfo;
import com.android.server.job.controllers.JobStatus;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;

public class NetworkStackClient {
    private static final String CONFIG_ALWAYS_RATELIMIT_NETWORKSTACK_CRASH = "always_ratelimit_networkstack_crash";
    private static final String CONFIG_MIN_CRASH_INTERVAL_MS = "min_crash_interval";
    private static final String CONFIG_MIN_UPTIME_BEFORE_CRASH_MS = "min_uptime_before_crash";
    private static final long DEFAULT_MIN_CRASH_INTERVAL_MS = 21600000;
    private static final long DEFAULT_MIN_UPTIME_BEFORE_CRASH_MS = 1800000;
    private static final String IN_PROCESS_SUFFIX = ".InProcess";
    private static final int NETWORKSTACK_TIMEOUT_MS = 10000;
    private static final String PREFS_FILE = "NetworkStackClientPrefs.xml";
    private static final String PREF_KEY_LAST_CRASH_TIME = "lastcrash_time";
    private static final String TAG = NetworkStackClient.class.getSimpleName();
    private static NetworkStackClient sInstance;
    @GuardedBy({"mPendingNetStackRequests"})
    private INetworkStackConnector mConnector;
    @GuardedBy({"mHealthListeners"})
    private final ArraySet<NetworkStackHealthListener> mHealthListeners = new ArraySet<>();
    @GuardedBy({"mLog"})
    private final SharedLog mLog = new SharedLog(TAG);
    @GuardedBy({"mPendingNetStackRequests"})
    private final ArrayList<NetworkStackCallback> mPendingNetStackRequests = new ArrayList<>();
    private volatile boolean mWasSystemServerInitialized = false;

    /* access modifiers changed from: private */
    public interface NetworkStackCallback {
        void onNetworkStackConnected(INetworkStackConnector iNetworkStackConnector);
    }

    public interface NetworkStackHealthListener {
        void onNetworkStackFailure(String str);
    }

    private NetworkStackClient() {
    }

    public static synchronized NetworkStackClient getInstance() {
        NetworkStackClient networkStackClient;
        synchronized (NetworkStackClient.class) {
            if (sInstance == null) {
                sInstance = new NetworkStackClient();
            }
            networkStackClient = sInstance;
        }
        return networkStackClient;
    }

    public void registerHealthListener(NetworkStackHealthListener listener) {
        synchronized (this.mHealthListeners) {
            this.mHealthListeners.add(listener);
        }
    }

    public void makeDhcpServer(String ifName, DhcpServingParamsParcel params, IDhcpServerCallbacks cb) {
        requestConnector(new NetworkStackCallback(ifName, params, cb) {
            /* class android.net.$$Lambda$NetworkStackClient$tuv4lz5fwSxR2XuU69pB4cKkltA */
            private final /* synthetic */ String f$0;
            private final /* synthetic */ DhcpServingParamsParcel f$1;
            private final /* synthetic */ IDhcpServerCallbacks f$2;

            {
                this.f$0 = r1;
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // android.net.NetworkStackClient.NetworkStackCallback
            public final void onNetworkStackConnected(INetworkStackConnector iNetworkStackConnector) {
                NetworkStackClient.lambda$makeDhcpServer$0(this.f$0, this.f$1, this.f$2, iNetworkStackConnector);
            }
        });
    }

    static /* synthetic */ void lambda$makeDhcpServer$0(String ifName, DhcpServingParamsParcel params, IDhcpServerCallbacks cb, INetworkStackConnector connector) {
        try {
            connector.makeDhcpServer(ifName, params, cb);
        } catch (RemoteException e) {
            e.rethrowFromSystemServer();
        }
    }

    public void makeIpClient(String ifName, IIpClientCallbacks cb) {
        requestConnector(new NetworkStackCallback(ifName, cb) {
            /* class android.net.$$Lambda$NetworkStackClient$EsrnifYD8EHxTwVQsf45HJKvtM */
            private final /* synthetic */ String f$0;
            private final /* synthetic */ IIpClientCallbacks f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            @Override // android.net.NetworkStackClient.NetworkStackCallback
            public final void onNetworkStackConnected(INetworkStackConnector iNetworkStackConnector) {
                NetworkStackClient.lambda$makeIpClient$1(this.f$0, this.f$1, iNetworkStackConnector);
            }
        });
    }

    static /* synthetic */ void lambda$makeIpClient$1(String ifName, IIpClientCallbacks cb, INetworkStackConnector connector) {
        try {
            connector.makeIpClient(ifName, cb);
        } catch (RemoteException e) {
            e.rethrowFromSystemServer();
        }
    }

    public void makeNetworkMonitor(Network network, String name, INetworkMonitorCallbacks cb) {
        requestConnector(new NetworkStackCallback(network, name, cb) {
            /* class android.net.$$Lambda$NetworkStackClient$8Y7GJyozK7_xixdmgfHS4QSifA */
            private final /* synthetic */ Network f$0;
            private final /* synthetic */ String f$1;
            private final /* synthetic */ INetworkMonitorCallbacks f$2;

            {
                this.f$0 = r1;
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // android.net.NetworkStackClient.NetworkStackCallback
            public final void onNetworkStackConnected(INetworkStackConnector iNetworkStackConnector) {
                NetworkStackClient.lambda$makeNetworkMonitor$2(this.f$0, this.f$1, this.f$2, iNetworkStackConnector);
            }
        });
    }

    static /* synthetic */ void lambda$makeNetworkMonitor$2(Network network, String name, INetworkMonitorCallbacks cb, INetworkStackConnector connector) {
        try {
            connector.makeNetworkMonitor(network, name, cb);
        } catch (RemoteException e) {
            e.rethrowFromSystemServer();
        }
    }

    public void fetchIpMemoryStore(IIpMemoryStoreCallbacks cb) {
        requestConnector(new NetworkStackCallback() {
            /* class android.net.$$Lambda$NetworkStackClient$qInwLPrclXOFvKSYRjcCaCSeEhw */

            @Override // android.net.NetworkStackClient.NetworkStackCallback
            public final void onNetworkStackConnected(INetworkStackConnector iNetworkStackConnector) {
                NetworkStackClient.lambda$fetchIpMemoryStore$3(IIpMemoryStoreCallbacks.this, iNetworkStackConnector);
            }
        });
    }

    static /* synthetic */ void lambda$fetchIpMemoryStore$3(IIpMemoryStoreCallbacks cb, INetworkStackConnector connector) {
        try {
            connector.fetchIpMemoryStore(cb);
        } catch (RemoteException e) {
            e.rethrowFromSystemServer();
        }
    }

    private class NetworkStackConnection implements ServiceConnection {
        private final Context mContext;
        private final String mPackageName;

        private NetworkStackConnection(Context context, String packageName) {
            this.mContext = context;
            this.mPackageName = packageName;
        }

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName name, IBinder service) {
            NetworkStackClient.this.logi("Network stack service connected");
            NetworkStackClient.this.registerNetworkStackService(service);
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName name) {
            NetworkStackClient.this.maybeCrashWithTerribleFailure("Lost network stack", this.mContext, this.mPackageName);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void registerNetworkStackService(IBinder service) {
        ArrayList<NetworkStackCallback> requests;
        INetworkStackConnector connector = INetworkStackConnector.Stub.asInterface(service);
        ServiceManager.addService("network_stack", service, false, 6);
        log("Network stack service registered");
        synchronized (this.mPendingNetStackRequests) {
            requests = new ArrayList<>(this.mPendingNetStackRequests);
            this.mPendingNetStackRequests.clear();
            this.mConnector = connector;
        }
        Iterator<NetworkStackCallback> it = requests.iterator();
        while (it.hasNext()) {
            it.next().onNetworkStackConnected(connector);
        }
    }

    public void init() {
        log("Network stack init");
        this.mWasSystemServerInitialized = true;
    }

    public void start(Context context) {
        log("Starting network stack");
        PackageManager pm = context.getPackageManager();
        Intent intent = getNetworkStackIntent(pm, true);
        if (intent == null) {
            intent = getNetworkStackIntent(pm, false);
            log("Starting network stack process");
        } else {
            log("Starting network stack in-process");
        }
        if (intent == null) {
            maybeCrashWithTerribleFailure("Could not resolve the network stack", context, null);
            return;
        }
        String packageName = intent.getComponent().getPackageName();
        if (!context.bindServiceAsUser(intent, new NetworkStackConnection(context, packageName), 65, UserHandle.SYSTEM)) {
            maybeCrashWithTerribleFailure("Could not bind to network stack in-process, or in app with " + intent, context, packageName);
            return;
        }
        log("Network stack service start requested");
    }

    private Intent getNetworkStackIntent(PackageManager pm, boolean inSystemProcess) {
        String str;
        String baseAction = INetworkStackConnector.class.getName();
        if (inSystemProcess) {
            str = baseAction + IN_PROCESS_SUFFIX;
        } else {
            str = baseAction;
        }
        Intent intent = new Intent(str);
        ComponentName comp = intent.resolveSystemService(pm, 0);
        if (comp == null) {
            return null;
        }
        intent.setComponent(comp);
        int uid = -1;
        try {
            uid = pm.getPackageUidAsUser(comp.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            logWtf("Network stack package not found", e);
        }
        if (uid == (inSystemProcess ? 1000 : 1073)) {
            if (!inSystemProcess) {
                checkNetworkStackPermission(pm, comp);
            }
            return intent;
        }
        throw new SecurityException("Invalid network stack UID: " + uid);
    }

    private void checkNetworkStackPermission(PackageManager pm, ComponentName comp) {
        if (pm.checkPermission("android.permission.MAINLINE_NETWORK_STACK", comp.getPackageName()) != 0) {
            throw new SecurityException("Network stack does not have permission android.permission.MAINLINE_NETWORK_STACK");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void maybeCrashWithTerribleFailure(String message, Context context, String packageName) {
        ArraySet<NetworkStackHealthListener> listeners;
        logWtf(message, null);
        long uptime = SystemClock.elapsedRealtime();
        long now = System.currentTimeMillis();
        long minCrashIntervalMs = DeviceConfig.getLong("connectivity", CONFIG_MIN_CRASH_INTERVAL_MS, (long) DEFAULT_MIN_CRASH_INTERVAL_MS);
        long minUptimeBeforeCrash = DeviceConfig.getLong("connectivity", CONFIG_MIN_UPTIME_BEFORE_CRASH_MS, 1800000);
        boolean haveKnownRecentCrash = false;
        boolean alwaysRatelimit = DeviceConfig.getBoolean("connectivity", CONFIG_ALWAYS_RATELIMIT_NETWORKSTACK_CRASH, false);
        SharedPreferences prefs = getSharedPreferences(context);
        long lastCrashTime = tryGetLastCrashTime(prefs);
        boolean alwaysCrash = Build.IS_DEBUGGABLE && !alwaysRatelimit;
        boolean justBooted = uptime < minUptimeBeforeCrash;
        if ((lastCrashTime != 0 && lastCrashTime < now) && now < lastCrashTime + minCrashIntervalMs) {
            haveKnownRecentCrash = true;
        }
        if (!alwaysCrash) {
            if (justBooted || haveKnownRecentCrash) {
                if (packageName != null) {
                    synchronized (this.mHealthListeners) {
                        listeners = new ArraySet<>(this.mHealthListeners);
                    }
                    Iterator<NetworkStackHealthListener> it = listeners.iterator();
                    while (it.hasNext()) {
                        it.next().onNetworkStackFailure(packageName);
                    }
                    return;
                }
                return;
            }
        }
        tryWriteLastCrashTime(prefs, now);
        throw new IllegalStateException(message);
    }

    private SharedPreferences getSharedPreferences(Context context) {
        try {
            return context.createDeviceProtectedStorageContext().getSharedPreferences(new File(Environment.getDataSystemDeDirectory(0), PREFS_FILE), 0);
        } catch (Throwable e) {
            logWtf("Error loading shared preferences", e);
            return null;
        }
    }

    private long tryGetLastCrashTime(SharedPreferences prefs) {
        if (prefs == null) {
            return 0;
        }
        try {
            return prefs.getLong(PREF_KEY_LAST_CRASH_TIME, 0);
        } catch (Throwable e) {
            logWtf("Error getting last crash time", e);
            return 0;
        }
    }

    private void tryWriteLastCrashTime(SharedPreferences prefs, long value) {
        if (prefs != null) {
            try {
                prefs.edit().putLong(PREF_KEY_LAST_CRASH_TIME, value).commit();
            } catch (Throwable e) {
                logWtf("Error writing last crash time", e);
            }
        }
    }

    private void log(String message) {
        synchronized (this.mLog) {
            this.mLog.log(message);
        }
    }

    private void logWtf(String message, Throwable e) {
        Slog.wtf(TAG, message);
        synchronized (this.mLog) {
            this.mLog.e(message, e);
        }
    }

    private void loge(String message, Throwable e) {
        synchronized (this.mLog) {
            this.mLog.e(message, e);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void logi(String message) {
        synchronized (this.mLog) {
            this.mLog.i(message);
        }
    }

    private INetworkStackConnector getRemoteConnector() {
        try {
            long before = System.currentTimeMillis();
            do {
                IBinder connector = ServiceManager.getService("network_stack");
                if (connector != null) {
                    return INetworkStackConnector.Stub.asInterface(connector);
                }
                Thread.sleep(20);
            } while (System.currentTimeMillis() - before <= JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY);
            loge("Timeout waiting for NetworkStack connector", null);
            return null;
        } catch (InterruptedException e) {
            loge("Error waiting for NetworkStack connector", e);
            return null;
        }
    }

    private void requestConnector(NetworkStackCallback request) {
        int caller = Binder.getCallingUid();
        if (caller != 1000 && !UserHandle.isSameApp(caller, 1002) && !UserHandle.isSameApp(caller, NetworkAgentInfo.EVENT_NETWORK_LINGER_COMPLETE)) {
            throw new SecurityException("Only the system server should try to bind to the network stack.");
        } else if (!this.mWasSystemServerInitialized) {
            INetworkStackConnector connector = getRemoteConnector();
            synchronized (this.mPendingNetStackRequests) {
                this.mConnector = connector;
            }
            request.onNetworkStackConnected(connector);
        } else {
            synchronized (this.mPendingNetStackRequests) {
                INetworkStackConnector connector2 = this.mConnector;
                if (connector2 == null) {
                    this.mPendingNetStackRequests.add(request);
                } else {
                    request.onNetworkStackConnected(connector2);
                }
            }
        }
    }

    public void dump(PrintWriter pw) {
        int requestsQueueLength;
        this.mLog.dump(null, pw, null);
        synchronized (this.mPendingNetStackRequests) {
            requestsQueueLength = this.mPendingNetStackRequests.size();
        }
        pw.println();
        pw.println("pendingNetStackRequests length: " + requestsQueueLength);
    }
}
