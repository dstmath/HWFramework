package com.android.server.notification;

import android.app.INotificationManager;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.IPackageManager;
import android.net.Uri;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;
import android.service.notification.Condition;
import android.service.notification.IConditionProvider;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Slog;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.notification.ManagedServices;
import com.android.server.notification.NotificationManagerService;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;

public class ConditionProviders extends ManagedServices {
    @VisibleForTesting
    static final String TAG_ENABLED_DND_APPS = "dnd_apps";
    private Callback mCallback;
    private final ArrayList<ConditionRecord> mRecords = new ArrayList<>();
    private final ArraySet<String> mSystemConditionProviderNames = safeSet(PropConfig.getStringArray(this.mContext, "system.condition.providers", 17236038));
    private final ArraySet<SystemConditionProviderService> mSystemConditionProviders = new ArraySet<>();

    public interface Callback {
        void onBootComplete();

        void onConditionChanged(Uri uri, Condition condition);

        void onServiceAdded(ComponentName componentName);

        void onUserSwitched();
    }

    private static class ConditionRecord {
        public final ComponentName component;
        public Condition condition;
        public final Uri id;
        public ManagedServices.ManagedServiceInfo info;
        public boolean subscribed;

        private ConditionRecord(Uri id2, ComponentName component2) {
            this.id = id2;
            this.component = component2;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder("ConditionRecord[id=");
            sb.append(this.id);
            sb.append(",component=");
            sb.append(this.component);
            sb.append(",subscribed=");
            StringBuilder sb2 = sb.append(this.subscribed);
            sb2.append(']');
            return sb2.toString();
        }
    }

    public ConditionProviders(Context context, ManagedServices.UserProfiles userProfiles, IPackageManager pm) {
        super(context, new Object(), userProfiles, pm);
        this.mApprovalLevel = 0;
    }

    public void setCallback(Callback callback) {
        this.mCallback = callback;
    }

    public boolean isSystemProviderEnabled(String path) {
        return this.mSystemConditionProviderNames.contains(path);
    }

    public void addSystemProvider(SystemConditionProviderService service) {
        this.mSystemConditionProviders.add(service);
        service.attachBase(this.mContext);
        registerService(service.asInterface(), service.getComponent(), 0);
    }

    public Iterable<SystemConditionProviderService> getSystemProviders() {
        return this.mSystemConditionProviders;
    }

    /* access modifiers changed from: protected */
    public ManagedServices.Config getConfig() {
        ManagedServices.Config c = new ManagedServices.Config();
        c.caption = "condition provider";
        c.serviceInterface = "android.service.notification.ConditionProviderService";
        c.secureSettingName = "enabled_notification_policy_access_packages";
        c.xmlTag = TAG_ENABLED_DND_APPS;
        c.secondarySettingName = "enabled_notification_listeners";
        c.bindPermission = "android.permission.BIND_CONDITION_PROVIDER_SERVICE";
        c.settingsAction = "android.settings.ACTION_CONDITION_PROVIDER_SETTINGS";
        c.clientLabel = 17039762;
        return c;
    }

    public void dump(PrintWriter pw, NotificationManagerService.DumpFilter filter) {
        int i;
        super.dump(pw, filter);
        synchronized (this.mMutex) {
            pw.print("    mRecords(");
            pw.print(this.mRecords.size());
            pw.println("):");
            i = 0;
            for (int i2 = 0; i2 < this.mRecords.size(); i2++) {
                ConditionRecord r = this.mRecords.get(i2);
                if (filter == null || filter.matches(r.component)) {
                    pw.print("      ");
                    pw.println(r);
                    String countdownDesc = CountdownConditionProvider.tryParseDescription(r.id);
                    if (countdownDesc != null) {
                        pw.print("        (");
                        pw.print(countdownDesc);
                        pw.println(")");
                    }
                }
            }
        }
        pw.print("    mSystemConditionProviders: ");
        pw.println(this.mSystemConditionProviderNames);
        while (true) {
            int i3 = i;
            if (i3 < this.mSystemConditionProviders.size()) {
                this.mSystemConditionProviders.valueAt(i3).dump(pw, filter);
                i = i3 + 1;
            } else {
                return;
            }
        }
    }

    /* access modifiers changed from: protected */
    public IInterface asInterface(IBinder binder) {
        return IConditionProvider.Stub.asInterface(binder);
    }

    /* access modifiers changed from: protected */
    public boolean checkType(IInterface service) {
        return service instanceof IConditionProvider;
    }

    public void onBootPhaseAppsCanStart() {
        super.onBootPhaseAppsCanStart();
        for (int i = 0; i < this.mSystemConditionProviders.size(); i++) {
            this.mSystemConditionProviders.valueAt(i).onBootComplete();
        }
        if (this.mCallback != null) {
            this.mCallback.onBootComplete();
        }
    }

    public void onUserSwitched(int user) {
        super.onUserSwitched(user);
        if (this.mCallback != null) {
            this.mCallback.onUserSwitched();
        }
    }

    /* access modifiers changed from: protected */
    public void onServiceAdded(ManagedServices.ManagedServiceInfo info) {
        try {
            provider(info).onConnected();
        } catch (RemoteException e) {
        }
        if (this.mCallback != null) {
            this.mCallback.onServiceAdded(info.component);
        }
    }

    /* access modifiers changed from: protected */
    public void onServiceRemovedLocked(ManagedServices.ManagedServiceInfo removed) {
        if (removed != null) {
            for (int i = this.mRecords.size() - 1; i >= 0; i--) {
                if (this.mRecords.get(i).component.equals(removed.component)) {
                    this.mRecords.remove(i);
                }
            }
        }
    }

    public void onPackagesChanged(boolean removingPackage, String[] pkgList, int[] uid) {
        if (removingPackage) {
            INotificationManager inm = NotificationManager.getService();
            if (pkgList != null && pkgList.length > 0) {
                for (String pkgName : pkgList) {
                    try {
                        inm.removeAutomaticZenRules(pkgName);
                        inm.setNotificationPolicyAccessGranted(pkgName, false);
                    } catch (Exception e) {
                        Slog.e(this.TAG, "Failed to clean up rules for " + pkgName, e);
                    }
                }
            }
        }
        super.onPackagesChanged(removingPackage, pkgList, uid);
    }

    /* access modifiers changed from: protected */
    public boolean isValidEntry(String packageOrComponent, int userId) {
        return true;
    }

    public ManagedServices.ManagedServiceInfo checkServiceToken(IConditionProvider provider) {
        ManagedServices.ManagedServiceInfo checkServiceTokenLocked;
        synchronized (this.mMutex) {
            checkServiceTokenLocked = checkServiceTokenLocked(provider);
        }
        return checkServiceTokenLocked;
    }

    private Condition[] removeDuplicateConditions(String pkg, Condition[] conditions) {
        if (conditions == null || conditions.length == 0) {
            return null;
        }
        int N = conditions.length;
        ArrayMap<Uri, Condition> valid = new ArrayMap<>(N);
        for (int i = 0; i < N; i++) {
            Uri id = conditions[i].id;
            if (valid.containsKey(id)) {
                Slog.w(this.TAG, "Ignoring condition from " + pkg + " for duplicate id: " + id);
            } else {
                valid.put(id, conditions[i]);
            }
        }
        if (valid.size() == 0) {
            return null;
        }
        if (valid.size() == N) {
            return conditions;
        }
        Condition[] rt = new Condition[valid.size()];
        for (int i2 = 0; i2 < rt.length; i2++) {
            rt[i2] = valid.valueAt(i2);
        }
        return rt;
    }

    private ConditionRecord getRecordLocked(Uri id, ComponentName component, boolean create) {
        if (id == null || component == null) {
            return null;
        }
        int N = this.mRecords.size();
        for (int i = 0; i < N; i++) {
            ConditionRecord r = this.mRecords.get(i);
            if (r.id.equals(id) && r.component.equals(component)) {
                return r;
            }
        }
        if (!create) {
            return null;
        }
        ConditionRecord r2 = new ConditionRecord(id, component);
        this.mRecords.add(r2);
        return r2;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0059, code lost:
        r0 = r11.length;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x005c, code lost:
        if (r8.mCallback == null) goto L_0x006d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0062, code lost:
        if ((r8.mCallback instanceof com.android.server.notification.ZenModeConditions) == false) goto L_0x006d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0064, code lost:
        ((com.android.server.notification.ZenModeConditions) r8.mCallback).onConditionChanged(r11);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x006d, code lost:
        r1 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x006e, code lost:
        if (r1 >= r0) goto L_0x0080;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0070, code lost:
        r2 = r11[r1];
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0074, code lost:
        if (r8.mCallback == null) goto L_0x007d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0076, code lost:
        r8.mCallback.onConditionChanged(r2.id, r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x007d, code lost:
        r2 = r1 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0080, code lost:
        return;
     */
    public void notifyConditions(String pkg, ManagedServices.ManagedServiceInfo info, Condition[] conditions) {
        synchronized (this.mMutex) {
            if (this.DEBUG) {
                String str = this.TAG;
                StringBuilder sb = new StringBuilder();
                sb.append("notifyConditions pkg=");
                sb.append(pkg);
                sb.append(" info=");
                sb.append(info);
                sb.append(" conditions=");
                sb.append(conditions == null ? null : Arrays.asList(conditions));
                Slog.d(str, sb.toString());
            }
            Condition[] conditions2 = removeDuplicateConditions(pkg, conditions);
            if (conditions2 != null) {
                if (conditions2.length != 0) {
                    int i = 0;
                    for (Condition c : conditions2) {
                        ConditionRecord r = getRecordLocked(c.id, info.component, true);
                        r.info = info;
                        r.condition = c;
                    }
                }
            }
        }
    }

    public IConditionProvider findConditionProvider(ComponentName component) {
        if (component == null) {
            return null;
        }
        for (ManagedServices.ManagedServiceInfo service : getServices()) {
            if (component.equals(service.component)) {
                return provider(service);
            }
        }
        return null;
    }

    public Condition findCondition(ComponentName component, Uri conditionId) {
        Condition condition = null;
        if (component == null || conditionId == null) {
            return null;
        }
        synchronized (this.mMutex) {
            ConditionRecord r = getRecordLocked(conditionId, component, false);
            if (r != null) {
                condition = r.condition;
            }
        }
        return condition;
    }

    public void ensureRecordExists(ComponentName component, Uri conditionId, IConditionProvider provider) {
        ConditionRecord r = getRecordLocked(conditionId, component, true);
        if (r.info == null) {
            r.info = checkServiceTokenLocked(provider);
        }
    }

    public boolean subscribeIfNecessary(ComponentName component, Uri conditionId) {
        synchronized (this.mMutex) {
            ConditionRecord r = getRecordLocked(conditionId, component, false);
            if (r == null) {
                String str = this.TAG;
                Slog.w(str, "Unable to subscribe to " + component + " " + conditionId);
                return false;
            } else if (r.subscribed) {
                return true;
            } else {
                subscribeLocked(r);
                boolean z = r.subscribed;
                return z;
            }
        }
    }

    public void unsubscribeIfNecessary(ComponentName component, Uri conditionId) {
        synchronized (this.mMutex) {
            ConditionRecord r = getRecordLocked(conditionId, component, false);
            if (r == null) {
                String str = this.TAG;
                Slog.w(str, "Unable to unsubscribe to " + component + " " + conditionId);
            } else if (r.subscribed) {
                unsubscribeLocked(r);
            }
        }
    }

    private void subscribeLocked(ConditionRecord r) {
        if (this.DEBUG) {
            String str = this.TAG;
            Slog.d(str, "subscribeLocked " + r);
        }
        IConditionProvider provider = provider(r);
        RemoteException re = null;
        if (provider != null) {
            try {
                String str2 = this.TAG;
                Slog.d(str2, "Subscribing to " + r.id + " with " + r.component);
                provider.onSubscribe(r.id);
                r.subscribed = true;
            } catch (RemoteException e) {
                String str3 = this.TAG;
                Slog.w(str3, "Error subscribing to " + r, e);
                re = e;
            }
        }
        ZenLog.traceSubscribe(r != null ? r.id : null, provider, re);
    }

    @SafeVarargs
    private static <T> ArraySet<T> safeSet(T... items) {
        ArraySet<T> rt = new ArraySet<>();
        if (items == null || items.length == 0) {
            return rt;
        }
        for (T item : items) {
            if (item != null) {
                rt.add(item);
            }
        }
        return rt;
    }

    private void unsubscribeLocked(ConditionRecord r) {
        if (this.DEBUG) {
            String str = this.TAG;
            Slog.d(str, "unsubscribeLocked " + r);
        }
        IConditionProvider provider = provider(r);
        RemoteException re = null;
        if (provider != null) {
            try {
                provider.onUnsubscribe(r.id);
            } catch (RemoteException e) {
                String str2 = this.TAG;
                Slog.w(str2, "Error unsubscribing to " + r, e);
                re = e;
            }
            r.subscribed = false;
        }
        ZenLog.traceUnsubscribe(r != null ? r.id : null, provider, re);
    }

    private static IConditionProvider provider(ConditionRecord r) {
        if (r == null) {
            return null;
        }
        return provider(r.info);
    }

    private static IConditionProvider provider(ManagedServices.ManagedServiceInfo info) {
        if (info == null) {
            return null;
        }
        return info.service;
    }
}
