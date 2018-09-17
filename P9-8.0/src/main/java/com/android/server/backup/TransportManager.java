package com.android.server.backup;

import android.app.backup.SelectBackupTransportCallback;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.EventLog;
import android.util.Log;
import android.util.Slog;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.backup.IBackupTransport;
import com.android.internal.backup.IBackupTransport.Stub;
import com.android.server.EventLogTags;
import com.android.server.HwServiceFactory;
import com.android.server.Watchdog;
import com.android.server.rms.IHwIpcMonitor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

class TransportManager {
    private static final int REBINDING_TIMEOUT_MSG = 1;
    private static final long REBINDING_TIMEOUT_PROVISIONED_MS = 300000;
    private static final long REBINDING_TIMEOUT_UNPROVISIONED_MS = 30000;
    private static final String SERVICE_ACTION_TRANSPORT_HOST = "android.backup.TRANSPORT_HOST";
    private static final String TAG = "BackupTransportManager";
    private IHwIpcMonitor mBackupIpcMonitor;
    @GuardedBy("mTransportLock")
    private final Map<String, ComponentName> mBoundTransports = new ArrayMap();
    private final Context mContext;
    private String mCurrentTransportName;
    private final Handler mHandler;
    private final PackageManager mPackageManager;
    private final TransportBoundListener mTransportBoundListener;
    private final Object mTransportLock = new Object();
    private final Intent mTransportServiceIntent = new Intent(SERVICE_ACTION_TRANSPORT_HOST);
    private final Set<ComponentName> mTransportWhitelist;
    @GuardedBy("mTransportLock")
    private final Map<ComponentName, TransportConnection> mValidTransports = new ArrayMap();

    interface TransportBoundListener {
        boolean onTransportBound(IBackupTransport iBackupTransport);
    }

    private class RebindOnTimeoutHandler extends Handler {
        RebindOnTimeoutHandler(Looper looper) {
            super(looper);
        }

        /* JADX WARNING: Missing block: B:15:0x008f, code:
            com.android.server.backup.TransportManager.-wrap1(r7.this$0, r2);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                String componentShortString = msg.obj;
                ComponentName transportComponent = ComponentName.unflattenFromString(componentShortString);
                synchronized (TransportManager.this.mTransportLock) {
                    if (TransportManager.this.mBoundTransports.containsValue(transportComponent)) {
                        Slog.d(TransportManager.TAG, "Explicit rebinding timeout passed, but already bound to " + componentShortString + " so not attempting to rebind");
                        return;
                    }
                    Slog.d(TransportManager.TAG, "Explicit rebinding timeout passed, attempting rebinding to: " + componentShortString);
                    TransportConnection conn = (TransportConnection) TransportManager.this.mValidTransports.get(transportComponent);
                    if (conn != null) {
                        TransportManager.this.mContext.unbindService(conn);
                        Slog.d(TransportManager.TAG, "Unbinding the existing (broken) connection to transport: " + componentShortString);
                    }
                }
            } else {
                Slog.e(TransportManager.TAG, "Unknown message sent to RebindOnTimeoutHandler, msg.what: " + msg.what);
            }
        }
    }

    private class TransportConnection implements ServiceConnection {
        private volatile IBackupTransport mBinder;
        private final List<SelectBackupTransportCallback> mListeners;
        private final ComponentName mTransportComponent;
        private volatile String mTransportName;

        /* synthetic */ TransportConnection(TransportManager this$0, ComponentName transportComponent, TransportConnection -this2) {
            this(transportComponent);
        }

        private TransportConnection(ComponentName transportComponent) {
            this.mListeners = new ArrayList();
            this.mTransportComponent = transportComponent;
        }

        public void onServiceConnected(ComponentName component, IBinder binder) {
            synchronized (TransportManager.this.mTransportLock) {
                this.mBinder = Stub.asInterface(binder);
                EventLog.writeEvent(EventLogTags.BACKUP_TRANSPORT_LIFECYCLE, new Object[]{component.flattenToShortString(), Integer.valueOf(1)});
                String componentShortString;
                try {
                    this.mTransportName = this.mBinder.name();
                    boolean success = TransportManager.this.mTransportBoundListener.onTransportBound(this.mBinder);
                    componentShortString = component.flattenToShortString().intern();
                    if (success) {
                        Slog.d(TransportManager.TAG, "Bound to transport: " + componentShortString);
                        TransportManager.this.mBoundTransports.put(this.mTransportName, component);
                        for (SelectBackupTransportCallback listener : this.mListeners) {
                            listener.onSuccess(this.mTransportName);
                        }
                        TransportManager.this.mHandler.removeMessages(1, componentShortString);
                    } else {
                        Slog.w(TransportManager.TAG, "Bound to transport " + componentShortString + " but it is invalid");
                        EventLog.writeEvent(EventLogTags.BACKUP_TRANSPORT_LIFECYCLE, new Object[]{componentShortString, Integer.valueOf(0)});
                        TransportManager.this.mContext.unbindService(this);
                        TransportManager.this.mValidTransports.remove(component);
                        this.mBinder = null;
                        for (SelectBackupTransportCallback listener2 : this.mListeners) {
                            listener2.onFailure(-2);
                        }
                    }
                    this.mListeners.clear();
                } catch (RemoteException e) {
                    Slog.e(TransportManager.TAG, "Couldn't get transport name.", e);
                    componentShortString = component.flattenToShortString().intern();
                    if (null != null) {
                        Slog.d(TransportManager.TAG, "Bound to transport: " + componentShortString);
                        TransportManager.this.mBoundTransports.put(this.mTransportName, component);
                        for (SelectBackupTransportCallback listener22 : this.mListeners) {
                            listener22.onSuccess(this.mTransportName);
                        }
                        TransportManager.this.mHandler.removeMessages(1, componentShortString);
                    } else {
                        Slog.w(TransportManager.TAG, "Bound to transport " + componentShortString + " but it is invalid");
                        EventLog.writeEvent(EventLogTags.BACKUP_TRANSPORT_LIFECYCLE, new Object[]{componentShortString, Integer.valueOf(0)});
                        TransportManager.this.mContext.unbindService(this);
                        TransportManager.this.mValidTransports.remove(component);
                        this.mBinder = null;
                        for (SelectBackupTransportCallback listener222 : this.mListeners) {
                            listener222.onFailure(-2);
                        }
                    }
                    this.mListeners.clear();
                } catch (Throwable th) {
                    componentShortString = component.flattenToShortString().intern();
                    if (null != null) {
                        Slog.d(TransportManager.TAG, "Bound to transport: " + componentShortString);
                        TransportManager.this.mBoundTransports.put(this.mTransportName, component);
                        for (SelectBackupTransportCallback listener2222 : this.mListeners) {
                            listener2222.onSuccess(this.mTransportName);
                        }
                        TransportManager.this.mHandler.removeMessages(1, componentShortString);
                    } else {
                        Slog.w(TransportManager.TAG, "Bound to transport " + componentShortString + " but it is invalid");
                        EventLog.writeEvent(EventLogTags.BACKUP_TRANSPORT_LIFECYCLE, new Object[]{componentShortString, Integer.valueOf(0)});
                        TransportManager.this.mContext.unbindService(this);
                        TransportManager.this.mValidTransports.remove(component);
                        this.mBinder = null;
                        for (SelectBackupTransportCallback listener22222 : this.mListeners) {
                            listener22222.onFailure(-2);
                        }
                    }
                    this.mListeners.clear();
                }
            }
        }

        public void onServiceDisconnected(ComponentName component) {
            synchronized (TransportManager.this.mTransportLock) {
                this.mBinder = null;
                TransportManager.this.mBoundTransports.remove(this.mTransportName);
            }
            EventLog.writeEvent(EventLogTags.BACKUP_TRANSPORT_LIFECYCLE, new Object[]{component.flattenToShortString(), Integer.valueOf(0)});
            Slog.w(TransportManager.TAG, "Disconnected from transport " + componentShortString);
            scheduleRebindTimeout(component);
        }

        private void scheduleRebindTimeout(ComponentName component) {
            String componentShortString = component.flattenToShortString().intern();
            long rebindTimeout = getRebindTimeout();
            TransportManager.this.mHandler.removeMessages(1, componentShortString);
            Message msg = TransportManager.this.mHandler.obtainMessage(1);
            msg.obj = componentShortString;
            TransportManager.this.mHandler.sendMessageDelayed(msg, rebindTimeout);
            Slog.d(TransportManager.TAG, "Scheduled explicit rebinding for " + componentShortString + " in " + rebindTimeout + "ms");
        }

        private IBackupTransport getBinder() {
            return this.mBinder;
        }

        private String getName() {
            return this.mTransportName;
        }

        private void bindIfUnbound() {
            if (this.mBinder == null) {
                Slog.d(TransportManager.TAG, "Rebinding to transport " + this.mTransportComponent.flattenToShortString());
                TransportManager.this.bindToTransport(this.mTransportComponent, this);
            }
        }

        private void addListener(SelectBackupTransportCallback listener) {
            synchronized (TransportManager.this.mTransportLock) {
                if (this.mBinder == null) {
                    this.mListeners.add(listener);
                } else {
                    listener.onSuccess(this.mTransportName);
                }
            }
        }

        private long getRebindTimeout() {
            if (Global.getInt(TransportManager.this.mContext.getContentResolver(), "device_provisioned", 0) != 0) {
                return TransportManager.REBINDING_TIMEOUT_PROVISIONED_MS;
            }
            return TransportManager.REBINDING_TIMEOUT_UNPROVISIONED_MS;
        }
    }

    TransportManager(Context context, Set<ComponentName> whitelist, String defaultTransport, TransportBoundListener listener, Looper looper) {
        this.mContext = context;
        this.mPackageManager = context.getPackageManager();
        if (whitelist == null) {
            whitelist = new ArraySet();
        }
        this.mTransportWhitelist = whitelist;
        this.mCurrentTransportName = defaultTransport;
        this.mTransportBoundListener = listener;
        this.mHandler = new RebindOnTimeoutHandler(looper);
        if (this.mBackupIpcMonitor == null) {
            this.mBackupIpcMonitor = HwServiceFactory.getIHwIpcMonitor(this.mTransportLock, "backup", "backupTransport");
            if (this.mBackupIpcMonitor != null) {
                Watchdog.getInstance().addIpcMonitor(this.mBackupIpcMonitor);
            }
        }
    }

    void onPackageAdded(String packageName) {
        synchronized (this.mTransportLock) {
            log_verbose("Package added. Binding to all transports. " + packageName);
            bindToAllInternal(packageName, null);
        }
    }

    void onPackageRemoved(String packageName) {
        synchronized (this.mTransportLock) {
            Iterator<Entry<ComponentName, TransportConnection>> iter = this.mValidTransports.entrySet().iterator();
            while (iter.hasNext()) {
                Entry<ComponentName, TransportConnection> validTransport = (Entry) iter.next();
                ComponentName componentName = (ComponentName) validTransport.getKey();
                if (componentName.getPackageName().equals(packageName)) {
                    TransportConnection transportConnection = (TransportConnection) validTransport.getValue();
                    iter.remove();
                    if (transportConnection != null) {
                        try {
                            this.mContext.unbindService(transportConnection);
                        } catch (IllegalArgumentException e) {
                            Slog.w(TAG, "Service not registered");
                        }
                        log_verbose("Package removed, removing transport: " + componentName.flattenToShortString());
                    } else {
                        continue;
                    }
                }
            }
        }
    }

    void onPackageChanged(String packageName, String[] components) {
        synchronized (this.mTransportLock) {
            for (String component : components) {
                ComponentName componentName = new ComponentName(packageName, component);
                TransportConnection removed = (TransportConnection) this.mValidTransports.remove(componentName);
                if (removed != null) {
                    try {
                        this.mContext.unbindService(removed);
                    } catch (IllegalArgumentException e) {
                        Slog.w(TAG, "Service not registered");
                    }
                    log_verbose("Package changed. Removing transport: " + componentName.flattenToShortString());
                }
            }
            bindToAllInternal(packageName, components);
        }
    }

    IBackupTransport getTransportBinder(String transportName) {
        synchronized (this.mTransportLock) {
            ComponentName component = (ComponentName) this.mBoundTransports.get(transportName);
            if (component == null) {
                Slog.w(TAG, "Transport " + transportName + " not bound.");
                return null;
            }
            TransportConnection conn = (TransportConnection) this.mValidTransports.get(component);
            if (conn == null) {
                Slog.w(TAG, "Transport " + transportName + " not valid.");
                return null;
            }
            IBackupTransport -wrap0 = conn.getBinder();
            return -wrap0;
        }
    }

    IBackupTransport getCurrentTransportBinder() {
        return getTransportBinder(this.mCurrentTransportName);
    }

    String getTransportName(IBackupTransport binder) {
        synchronized (this.mTransportLock) {
            for (TransportConnection conn : this.mValidTransports.values()) {
                if (conn.getBinder() == binder) {
                    String -wrap1 = conn.getName();
                    return -wrap1;
                }
            }
            return null;
        }
    }

    String[] getBoundTransportNames() {
        String[] strArr;
        synchronized (this.mTransportLock) {
            strArr = (String[]) this.mBoundTransports.keySet().toArray(new String[this.mBoundTransports.size()]);
        }
        return strArr;
    }

    ComponentName[] getAllTransportCompenents() {
        ComponentName[] componentNameArr;
        synchronized (this.mTransportLock) {
            componentNameArr = (ComponentName[]) this.mValidTransports.keySet().toArray(new ComponentName[this.mValidTransports.size()]);
        }
        return componentNameArr;
    }

    String getCurrentTransportName() {
        return this.mCurrentTransportName;
    }

    Set<ComponentName> getTransportWhitelist() {
        return this.mTransportWhitelist;
    }

    String selectTransport(String transport) {
        String prevTransport;
        synchronized (this.mTransportLock) {
            prevTransport = this.mCurrentTransportName;
            this.mCurrentTransportName = transport;
        }
        return prevTransport;
    }

    void ensureTransportReady(ComponentName transportComponent, SelectBackupTransportCallback listener) {
        synchronized (this.mTransportLock) {
            TransportConnection conn = (TransportConnection) this.mValidTransports.get(transportComponent);
            if (conn == null) {
                listener.onFailure(-1);
                return;
            }
            conn.bindIfUnbound();
            conn.addListener(listener);
        }
    }

    void registerAllTransports() {
        bindToAllInternal(null, null);
    }

    private void bindToAllInternal(String packageName, String[] components) {
        PackageInfo pkgInfo = null;
        if (packageName != null) {
            try {
                pkgInfo = this.mPackageManager.getPackageInfo(packageName, 0);
            } catch (NameNotFoundException e) {
                Slog.w(TAG, "Package not found: " + packageName);
                return;
            }
        }
        Intent intent = new Intent(this.mTransportServiceIntent);
        if (packageName != null) {
            intent.setPackage(packageName);
        }
        List<ResolveInfo> hosts = this.mPackageManager.queryIntentServicesAsUser(intent, 0, 0);
        if (hosts != null) {
            for (ResolveInfo host : hosts) {
                ComponentName infoComponentName = host.serviceInfo.getComponentName();
                boolean shouldBind = false;
                if (components == null || packageName == null) {
                    shouldBind = true;
                } else {
                    for (String component : components) {
                        if (infoComponentName.equals(new ComponentName(pkgInfo.packageName, component))) {
                            shouldBind = true;
                            break;
                        }
                    }
                }
                if (shouldBind && isTransportTrusted(infoComponentName)) {
                    tryBindTransport(infoComponentName);
                }
            }
        }
    }

    private boolean isTransportTrusted(ComponentName transport) {
        if (this.mTransportWhitelist.contains(transport)) {
            try {
                if ((this.mPackageManager.getPackageInfo(transport.getPackageName(), 0).applicationInfo.privateFlags & 8) != 0) {
                    return true;
                }
                Slog.w(TAG, "Transport package " + transport.getPackageName() + " not privileged");
                return false;
            } catch (NameNotFoundException e) {
                Slog.w(TAG, "Package not found.", e);
                return false;
            }
        }
        Slog.w(TAG, "BackupTransport " + transport.flattenToShortString() + " not whitelisted.");
        return false;
    }

    private void tryBindTransport(ComponentName transportComponentName) {
        Slog.d(TAG, "Binding to transport: " + transportComponentName.flattenToShortString());
        TransportConnection connection = new TransportConnection(this, transportComponentName, null);
        if (bindToTransport(transportComponentName, connection)) {
            synchronized (this.mTransportLock) {
                this.mValidTransports.put(transportComponentName, connection);
            }
            return;
        }
        Slog.w(TAG, "Couldn't bind to transport " + transportComponentName);
    }

    private boolean bindToTransport(ComponentName componentName, ServiceConnection connection) {
        return this.mContext.bindServiceAsUser(new Intent(this.mTransportServiceIntent).setComponent(componentName), connection, 1, UserHandle.SYSTEM);
    }

    private static void log_verbose(String message) {
        if (Log.isLoggable(TAG, 2)) {
            Slog.v(TAG, message);
        }
    }
}
