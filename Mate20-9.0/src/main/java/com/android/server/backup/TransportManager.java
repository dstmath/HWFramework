package com.android.server.backup;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Slog;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.backup.IBackupTransport;
import com.android.internal.util.Preconditions;
import com.android.server.BatteryService;
import com.android.server.HwServiceFactory;
import com.android.server.Watchdog;
import com.android.server.backup.transport.OnTransportRegisteredListener;
import com.android.server.backup.transport.TransportClient;
import com.android.server.backup.transport.TransportClientManager;
import com.android.server.backup.transport.TransportNotAvailableException;
import com.android.server.backup.transport.TransportNotRegisteredException;
import com.android.server.backup.transport.TransportStats;
import com.android.server.rms.IHwIpcMonitor;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class TransportManager {
    @VisibleForTesting
    public static final String SERVICE_ACTION_TRANSPORT_HOST = "android.backup.TRANSPORT_HOST";
    private static final String TAG = "BackupTransportManager";
    private IHwIpcMonitor mBackupIpcMonitor;
    private final Context mContext;
    @GuardedBy("mTransportLock")
    private volatile String mCurrentTransportName;
    private OnTransportRegisteredListener mOnTransportRegisteredListener = $$Lambda$TransportManager$Z9ckpFUW2V4jkdHnyXIEiLuAoBc.INSTANCE;
    private final PackageManager mPackageManager;
    @GuardedBy("mTransportLock")
    private final Map<ComponentName, TransportDescription> mRegisteredTransportsDescriptionMap = new ArrayMap();
    private final TransportClientManager mTransportClientManager;
    private final Object mTransportLock = new Object();
    private final Intent mTransportServiceIntent = new Intent(SERVICE_ACTION_TRANSPORT_HOST);
    private final TransportStats mTransportStats;
    private final Set<ComponentName> mTransportWhitelist;

    private static class TransportDescription {
        /* access modifiers changed from: private */
        public Intent configurationIntent;
        /* access modifiers changed from: private */
        public String currentDestinationString;
        /* access modifiers changed from: private */
        public Intent dataManagementIntent;
        /* access modifiers changed from: private */
        public String dataManagementLabel;
        /* access modifiers changed from: private */
        public String name;
        /* access modifiers changed from: private */
        public final String transportDirName;

        private TransportDescription(String name2, String transportDirName2, Intent configurationIntent2, String currentDestinationString2, Intent dataManagementIntent2, String dataManagementLabel2) {
            this.name = name2;
            this.transportDirName = transportDirName2;
            this.configurationIntent = configurationIntent2;
            this.currentDestinationString = currentDestinationString2;
            this.dataManagementIntent = dataManagementIntent2;
            this.dataManagementLabel = dataManagementLabel2;
        }
    }

    static /* synthetic */ void lambda$new$0(String c, String n) {
    }

    TransportManager(Context context, Set<ComponentName> whitelist, String selectedTransport) {
        this.mContext = context;
        this.mPackageManager = context.getPackageManager();
        this.mTransportWhitelist = (Set) Preconditions.checkNotNull(whitelist);
        this.mCurrentTransportName = selectedTransport;
        this.mTransportStats = new TransportStats();
        this.mTransportClientManager = new TransportClientManager(context, this.mTransportStats);
    }

    @VisibleForTesting
    TransportManager(Context context, Set<ComponentName> whitelist, String selectedTransport, TransportClientManager transportClientManager) {
        this.mContext = context;
        this.mPackageManager = context.getPackageManager();
        this.mTransportWhitelist = (Set) Preconditions.checkNotNull(whitelist);
        this.mCurrentTransportName = selectedTransport;
        this.mTransportStats = new TransportStats();
        this.mTransportClientManager = transportClientManager;
        if (this.mBackupIpcMonitor == null) {
            this.mBackupIpcMonitor = HwServiceFactory.getIHwIpcMonitor(this.mTransportLock, BatteryService.HealthServiceWrapper.INSTANCE_HEALTHD, "backupTransport");
            if (this.mBackupIpcMonitor != null) {
                Watchdog.getInstance().addIpcMonitor(this.mBackupIpcMonitor);
            }
        }
    }

    public void setOnTransportRegisteredListener(OnTransportRegisteredListener listener) {
        this.mOnTransportRegisteredListener = listener;
    }

    static /* synthetic */ boolean lambda$onPackageAdded$1(ComponentName transportComponent) {
        return true;
    }

    /* access modifiers changed from: package-private */
    public void onPackageAdded(String packageName) {
        registerTransportsFromPackage(packageName, $$Lambda$TransportManager$4ND1hZMerK5gHU67okq6DZjKDQw.INSTANCE);
    }

    /* access modifiers changed from: package-private */
    public void onPackageRemoved(String packageName) {
        synchronized (this.mTransportLock) {
            this.mRegisteredTransportsDescriptionMap.keySet().removeIf(fromPackageFilter(packageName));
        }
    }

    /* access modifiers changed from: package-private */
    public void onPackageChanged(String packageName, String... components) {
        Set<ComponentName> transportComponents = new ArraySet<>(components.length);
        for (String componentName : components) {
            transportComponents.add(new ComponentName(packageName, componentName));
        }
        synchronized (this.mTransportLock) {
            Set<ComponentName> keySet = this.mRegisteredTransportsDescriptionMap.keySet();
            Objects.requireNonNull(transportComponents);
            keySet.removeIf(new Predicate(transportComponents) {
                private final /* synthetic */ Set f$0;

                {
                    this.f$0 = r1;
                }

                public final boolean test(Object obj) {
                    return this.f$0.contains((ComponentName) obj);
                }
            });
        }
        Objects.requireNonNull(transportComponents);
        registerTransportsFromPackage(packageName, new Predicate(transportComponents) {
            private final /* synthetic */ Set f$0;

            {
                this.f$0 = r1;
            }

            public final boolean test(Object obj) {
                return this.f$0.contains((ComponentName) obj);
            }
        });
    }

    /* access modifiers changed from: package-private */
    public ComponentName[] getRegisteredTransportComponents() {
        ComponentName[] componentNameArr;
        synchronized (this.mTransportLock) {
            componentNameArr = (ComponentName[]) this.mRegisteredTransportsDescriptionMap.keySet().toArray(new ComponentName[this.mRegisteredTransportsDescriptionMap.size()]);
        }
        return componentNameArr;
    }

    /* access modifiers changed from: package-private */
    public String[] getRegisteredTransportNames() {
        String[] transportNames;
        synchronized (this.mTransportLock) {
            transportNames = new String[this.mRegisteredTransportsDescriptionMap.size()];
            int i = 0;
            for (TransportDescription description : this.mRegisteredTransportsDescriptionMap.values()) {
                transportNames[i] = description.name;
                i++;
            }
        }
        return transportNames;
    }

    /* access modifiers changed from: package-private */
    public Set<ComponentName> getTransportWhitelist() {
        return this.mTransportWhitelist;
    }

    /* access modifiers changed from: package-private */
    public String getCurrentTransportName() {
        return this.mCurrentTransportName;
    }

    public String getTransportName(ComponentName transportComponent) throws TransportNotRegisteredException {
        String access$000;
        synchronized (this.mTransportLock) {
            access$000 = getRegisteredTransportDescriptionOrThrowLocked(transportComponent).name;
        }
        return access$000;
    }

    public String getTransportDirName(ComponentName transportComponent) throws TransportNotRegisteredException {
        String access$100;
        synchronized (this.mTransportLock) {
            access$100 = getRegisteredTransportDescriptionOrThrowLocked(transportComponent).transportDirName;
        }
        return access$100;
    }

    public String getTransportDirName(String transportName) throws TransportNotRegisteredException {
        String access$100;
        synchronized (this.mTransportLock) {
            access$100 = getRegisteredTransportDescriptionOrThrowLocked(transportName).transportDirName;
        }
        return access$100;
    }

    public Intent getTransportConfigurationIntent(String transportName) throws TransportNotRegisteredException {
        Intent access$200;
        synchronized (this.mTransportLock) {
            access$200 = getRegisteredTransportDescriptionOrThrowLocked(transportName).configurationIntent;
        }
        return access$200;
    }

    public String getTransportCurrentDestinationString(String transportName) throws TransportNotRegisteredException {
        String access$300;
        synchronized (this.mTransportLock) {
            access$300 = getRegisteredTransportDescriptionOrThrowLocked(transportName).currentDestinationString;
        }
        return access$300;
    }

    public Intent getTransportDataManagementIntent(String transportName) throws TransportNotRegisteredException {
        Intent access$400;
        synchronized (this.mTransportLock) {
            access$400 = getRegisteredTransportDescriptionOrThrowLocked(transportName).dataManagementIntent;
        }
        return access$400;
    }

    public String getTransportDataManagementLabel(String transportName) throws TransportNotRegisteredException {
        String access$500;
        synchronized (this.mTransportLock) {
            access$500 = getRegisteredTransportDescriptionOrThrowLocked(transportName).dataManagementLabel;
        }
        return access$500;
    }

    public boolean isTransportRegistered(String transportName) {
        boolean z;
        synchronized (this.mTransportLock) {
            z = getRegisteredTransportEntryLocked(transportName) != null;
        }
        return z;
    }

    public void forEachRegisteredTransport(Consumer<String> transportConsumer) {
        synchronized (this.mTransportLock) {
            for (TransportDescription transportDescription : this.mRegisteredTransportsDescriptionMap.values()) {
                transportConsumer.accept(transportDescription.name);
            }
        }
    }

    public void updateTransportAttributes(ComponentName transportComponent, String name, Intent configurationIntent, String currentDestinationString, Intent dataManagementIntent, String dataManagementLabel) {
        synchronized (this.mTransportLock) {
            TransportDescription description = this.mRegisteredTransportsDescriptionMap.get(transportComponent);
            if (description == null) {
                Slog.e(TAG, "Transport " + name + " not registered tried to change description");
                return;
            }
            String unused = description.name = name;
            Intent unused2 = description.configurationIntent = configurationIntent;
            String unused3 = description.currentDestinationString = currentDestinationString;
            Intent unused4 = description.dataManagementIntent = dataManagementIntent;
            String unused5 = description.dataManagementLabel = dataManagementLabel;
            Slog.d(TAG, "Transport " + name + " updated its attributes");
        }
    }

    @GuardedBy("mTransportLock")
    private TransportDescription getRegisteredTransportDescriptionOrThrowLocked(ComponentName transportComponent) throws TransportNotRegisteredException {
        TransportDescription description = this.mRegisteredTransportsDescriptionMap.get(transportComponent);
        if (description != null) {
            return description;
        }
        throw new TransportNotRegisteredException(transportComponent);
    }

    @GuardedBy("mTransportLock")
    private TransportDescription getRegisteredTransportDescriptionOrThrowLocked(String transportName) throws TransportNotRegisteredException {
        TransportDescription description = getRegisteredTransportDescriptionLocked(transportName);
        if (description != null) {
            return description;
        }
        throw new TransportNotRegisteredException(transportName);
    }

    @GuardedBy("mTransportLock")
    private ComponentName getRegisteredTransportComponentLocked(String transportName) {
        Map.Entry<ComponentName, TransportDescription> entry = getRegisteredTransportEntryLocked(transportName);
        if (entry == null) {
            return null;
        }
        return entry.getKey();
    }

    @GuardedBy("mTransportLock")
    private TransportDescription getRegisteredTransportDescriptionLocked(String transportName) {
        Map.Entry<ComponentName, TransportDescription> entry = getRegisteredTransportEntryLocked(transportName);
        if (entry == null) {
            return null;
        }
        return entry.getValue();
    }

    @GuardedBy("mTransportLock")
    private Map.Entry<ComponentName, TransportDescription> getRegisteredTransportEntryLocked(String transportName) {
        for (Map.Entry<ComponentName, TransportDescription> entry : this.mRegisteredTransportsDescriptionMap.entrySet()) {
            if (transportName.equals(entry.getValue().name)) {
                return entry;
            }
        }
        return null;
    }

    public TransportClient getTransportClient(String transportName, String caller) {
        try {
            return getTransportClientOrThrow(transportName, caller);
        } catch (TransportNotRegisteredException e) {
            Slog.w(TAG, "Transport " + transportName + " not registered");
            return null;
        }
    }

    public TransportClient getTransportClientOrThrow(String transportName, String caller) throws TransportNotRegisteredException {
        TransportClient transportClient;
        synchronized (this.mTransportLock) {
            ComponentName component = getRegisteredTransportComponentLocked(transportName);
            if (component != null) {
                transportClient = this.mTransportClientManager.getTransportClient(component, caller);
            } else {
                throw new TransportNotRegisteredException(transportName);
            }
        }
        return transportClient;
    }

    public TransportClient getCurrentTransportClient(String caller) {
        TransportClient transportClient;
        synchronized (this.mTransportLock) {
            transportClient = getTransportClient(this.mCurrentTransportName, caller);
        }
        return transportClient;
    }

    public TransportClient getCurrentTransportClientOrThrow(String caller) throws TransportNotRegisteredException {
        TransportClient transportClientOrThrow;
        synchronized (this.mTransportLock) {
            transportClientOrThrow = getTransportClientOrThrow(this.mCurrentTransportName, caller);
        }
        return transportClientOrThrow;
    }

    public void disposeOfTransportClient(TransportClient transportClient, String caller) {
        this.mTransportClientManager.disposeOfTransportClient(transportClient, caller);
    }

    /* access modifiers changed from: package-private */
    @Deprecated
    public String selectTransport(String transportName) {
        String prevTransport;
        synchronized (this.mTransportLock) {
            prevTransport = this.mCurrentTransportName;
            this.mCurrentTransportName = transportName;
        }
        return prevTransport;
    }

    public int registerAndSelectTransport(ComponentName transportComponent) {
        synchronized (this.mTransportLock) {
            try {
                selectTransport(getTransportName(transportComponent));
            } catch (TransportNotRegisteredException e) {
                int result = registerTransport(transportComponent);
                if (result != 0) {
                    return result;
                }
                synchronized (this.mTransportLock) {
                    try {
                        selectTransport(getTransportName(transportComponent));
                        return 0;
                    } catch (TransportNotRegisteredException e2) {
                        Slog.wtf(TAG, "Transport got unregistered");
                        return -1;
                    }
                }
            } catch (Throwable th) {
                throw th;
            }
        }
        return 0;
    }

    static /* synthetic */ boolean lambda$registerTransports$2(ComponentName transportComponent) {
        return true;
    }

    public void registerTransports() {
        registerTransportsForIntent(this.mTransportServiceIntent, $$Lambda$TransportManager$Qbutmzd17ICwZdy0UzRrO3_VK0.INSTANCE);
    }

    private void registerTransportsFromPackage(String packageName, Predicate<ComponentName> transportComponentFilter) {
        try {
            this.mPackageManager.getPackageInfo(packageName, 0);
            registerTransportsForIntent(new Intent(this.mTransportServiceIntent).setPackage(packageName), transportComponentFilter.and(fromPackageFilter(packageName)));
        } catch (PackageManager.NameNotFoundException e) {
            Slog.e(TAG, "Trying to register transports from package not found " + packageName);
        }
    }

    private void registerTransportsForIntent(Intent intent, Predicate<ComponentName> transportComponentFilter) {
        List<ResolveInfo> hosts = this.mPackageManager.queryIntentServicesAsUser(intent, 0, 0);
        if (hosts != null) {
            for (ResolveInfo host : hosts) {
                ComponentName transportComponent = host.serviceInfo.getComponentName();
                if (transportComponentFilter.test(transportComponent) && isTransportTrusted(transportComponent)) {
                    registerTransport(transportComponent);
                }
            }
        }
    }

    private boolean isTransportTrusted(ComponentName transport) {
        if (!this.mTransportWhitelist.contains(transport)) {
            Slog.w(TAG, "BackupTransport " + transport.flattenToShortString() + " not whitelisted.");
            return false;
        }
        try {
            if ((this.mPackageManager.getPackageInfo(transport.getPackageName(), 0).applicationInfo.privateFlags & 8) != 0) {
                return true;
            }
            Slog.w(TAG, "Transport package " + transport.getPackageName() + " not privileged");
            return false;
        } catch (PackageManager.NameNotFoundException e) {
            Slog.w(TAG, "Package not found.", e);
            return false;
        }
    }

    private int registerTransport(ComponentName transportComponent) {
        checkCanUseTransport();
        if (!isTransportTrusted(transportComponent)) {
            return -2;
        }
        String transportString = transportComponent.flattenToShortString();
        Bundle extras = new Bundle();
        extras.putBoolean("android.app.backup.extra.TRANSPORT_REGISTRATION", true);
        TransportClient transportClient = this.mTransportClientManager.getTransportClient(transportComponent, extras, "TransportManager.registerTransport()");
        int result = -1;
        try {
            IBackupTransport transport = transportClient.connectOrThrow("TransportManager.registerTransport()");
            try {
                String transportName = transport.name();
                String transportDirName = transport.transportDirName();
                registerTransport(transportComponent, transport);
                Slog.d(TAG, "Transport " + transportString + " registered");
                this.mOnTransportRegisteredListener.onTransportRegistered(transportName, transportDirName);
                result = 0;
            } catch (RemoteException e) {
                Slog.e(TAG, "Transport " + transportString + " died while registering");
            }
            this.mTransportClientManager.disposeOfTransportClient(transportClient, "TransportManager.registerTransport()");
            return result;
        } catch (TransportNotAvailableException e2) {
            Slog.e(TAG, "Couldn't connect to transport " + transportString + " for registration");
            this.mTransportClientManager.disposeOfTransportClient(transportClient, "TransportManager.registerTransport()");
            return -1;
        }
    }

    private void registerTransport(ComponentName transportComponent, IBackupTransport transport) throws RemoteException {
        checkCanUseTransport();
        TransportDescription description = new TransportDescription(transport.name(), transport.transportDirName(), transport.configurationIntent(), transport.currentDestinationString(), transport.dataManagementIntent(), transport.dataManagementLabel());
        synchronized (this.mTransportLock) {
            this.mRegisteredTransportsDescriptionMap.put(transportComponent, description);
        }
    }

    private void checkCanUseTransport() {
        Preconditions.checkState(!Thread.holdsLock(this.mTransportLock), "Can't call transport with transport lock held");
    }

    public void dumpTransportClients(PrintWriter pw) {
        this.mTransportClientManager.dump(pw);
    }

    public void dumpTransportStats(PrintWriter pw) {
        this.mTransportStats.dump(pw);
    }

    private static Predicate<ComponentName> fromPackageFilter(String packageName) {
        return new Predicate(packageName) {
            private final /* synthetic */ String f$0;

            {
                this.f$0 = r1;
            }

            public final boolean test(Object obj) {
                return this.f$0.equals(((ComponentName) obj).getPackageName());
            }
        };
    }
}
