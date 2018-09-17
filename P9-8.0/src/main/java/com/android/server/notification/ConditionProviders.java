package com.android.server.notification;

import android.app.INotificationManager;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;
import android.provider.Settings.Secure;
import android.service.notification.Condition;
import android.service.notification.IConditionProvider;
import android.service.notification.IConditionProvider.Stub;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Slog;
import com.android.server.notification.ManagedServices.Config;
import com.android.server.notification.ManagedServices.ManagedServiceInfo;
import com.android.server.notification.ManagedServices.UserProfiles;
import com.android.server.notification.NotificationManagerService.DumpFilter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;

public class ConditionProviders extends ManagedServices {
    private Callback mCallback;
    private final ArrayList<ConditionRecord> mRecords = new ArrayList();
    private final ArraySet<String> mSystemConditionProviderNames = safeSet(PropConfig.getStringArray(this.mContext, "system.condition.providers", 17236034));
    private final ArraySet<SystemConditionProviderService> mSystemConditionProviders = new ArraySet();

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
        public ManagedServiceInfo info;
        public boolean subscribed;

        /* synthetic */ ConditionRecord(Uri id, ComponentName component, ConditionRecord -this2) {
            this(id, component);
        }

        private ConditionRecord(Uri id, ComponentName component) {
            this.id = id;
            this.component = component;
        }

        public String toString() {
            return "ConditionRecord[id=" + this.id + ",component=" + this.component + ",subscribed=" + this.subscribed + ']';
        }
    }

    public ConditionProviders(Context context, Handler handler, UserProfiles userProfiles) {
        super(context, handler, new Object(), userProfiles);
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

    protected Config getConfig() {
        Config c = new Config();
        c.caption = "condition provider";
        c.serviceInterface = "android.service.notification.ConditionProviderService";
        c.secureSettingName = "enabled_notification_policy_access_packages";
        c.secondarySettingName = "enabled_notification_listeners";
        c.bindPermission = "android.permission.BIND_CONDITION_PROVIDER_SERVICE";
        c.settingsAction = "android.settings.ACTION_CONDITION_PROVIDER_SETTINGS";
        c.clientLabel = 17039747;
        return c;
    }

    public void dump(PrintWriter pw, DumpFilter filter) {
        int i;
        super.dump(pw, filter);
        synchronized (this.mMutex) {
            pw.print("    mRecords(");
            pw.print(this.mRecords.size());
            pw.println("):");
            for (i = 0; i < this.mRecords.size(); i++) {
                ConditionRecord r = (ConditionRecord) this.mRecords.get(i);
                if (filter == null || (filter.matches(r.component) ^ 1) == 0) {
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
        for (i = 0; i < this.mSystemConditionProviders.size(); i++) {
            ((SystemConditionProviderService) this.mSystemConditionProviders.valueAt(i)).dump(pw, filter);
        }
    }

    protected IInterface asInterface(IBinder binder) {
        return Stub.asInterface(binder);
    }

    protected boolean checkType(IInterface service) {
        return service instanceof IConditionProvider;
    }

    public void onBootPhaseAppsCanStart() {
        super.onBootPhaseAppsCanStart();
        for (int i = 0; i < this.mSystemConditionProviders.size(); i++) {
            ((SystemConditionProviderService) this.mSystemConditionProviders.valueAt(i)).onBootComplete();
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

    protected void onServiceAdded(ManagedServiceInfo info) {
        try {
            provider(info).onConnected();
        } catch (RemoteException e) {
        }
        if (this.mCallback != null) {
            this.mCallback.onServiceAdded(info.component);
        }
    }

    protected void onServiceRemovedLocked(ManagedServiceInfo removed) {
        if (removed != null) {
            for (int i = this.mRecords.size() - 1; i >= 0; i--) {
                if (((ConditionRecord) this.mRecords.get(i)).component.equals(removed.component)) {
                    this.mRecords.remove(i);
                }
            }
        }
    }

    public void onPackagesChanged(boolean removingPackage, String[] pkgList) {
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
        super.onPackagesChanged(removingPackage, pkgList);
    }

    public ManagedServiceInfo checkServiceToken(IConditionProvider provider) {
        ManagedServiceInfo checkServiceTokenLocked;
        synchronized (this.mMutex) {
            checkServiceTokenLocked = checkServiceTokenLocked(provider);
        }
        return checkServiceTokenLocked;
    }

    private Condition[] removeDuplicateConditions(String pkg, Condition[] conditions) {
        if (conditions == null || conditions.length == 0) {
            return null;
        }
        int i;
        int N = conditions.length;
        ArrayMap<Uri, Condition> valid = new ArrayMap(N);
        for (i = 0; i < N; i++) {
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
        for (i = 0; i < rt.length; i++) {
            rt[i] = (Condition) valid.valueAt(i);
        }
        return rt;
    }

    private ConditionRecord getRecordLocked(Uri id, ComponentName component, boolean create) {
        if (id == null || component == null) {
            return null;
        }
        ConditionRecord r;
        int N = this.mRecords.size();
        for (int i = 0; i < N; i++) {
            r = (ConditionRecord) this.mRecords.get(i);
            if (r.id.equals(id) && r.component.equals(component)) {
                return r;
            }
        }
        if (!create) {
            return null;
        }
        r = new ConditionRecord(id, component, null);
        this.mRecords.add(r);
        return r;
    }

    /* JADX WARNING: Missing block: B:13:0x0043, code:
            return;
     */
    /* JADX WARNING: Missing block: B:21:0x0060, code:
            r0 = r12.length;
     */
    /* JADX WARNING: Missing block: B:22:0x0063, code:
            if (r9.mCallback == null) goto L_0x0076;
     */
    /* JADX WARNING: Missing block: B:24:0x0069, code:
            if ((r9.mCallback instanceof com.android.server.notification.ZenModeConditions) == false) goto L_0x0076;
     */
    /* JADX WARNING: Missing block: B:25:0x006b, code:
            ((com.android.server.notification.ZenModeConditions) r9.mCallback).onConditionChanged(r12);
     */
    /* JADX WARNING: Missing block: B:26:0x0072, code:
            return;
     */
    /* JADX WARNING: Missing block: B:30:0x0076, code:
            r2 = 0;
     */
    /* JADX WARNING: Missing block: B:31:0x0077, code:
            if (r2 >= r0) goto L_0x0072;
     */
    /* JADX WARNING: Missing block: B:32:0x0079, code:
            r1 = r12[r2];
     */
    /* JADX WARNING: Missing block: B:33:0x007d, code:
            if (r9.mCallback == null) goto L_0x0086;
     */
    /* JADX WARNING: Missing block: B:34:0x007f, code:
            r9.mCallback.onConditionChanged(r1.id, r1);
     */
    /* JADX WARNING: Missing block: B:35:0x0086, code:
            r2 = r2 + 1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void notifyConditions(String pkg, ManagedServiceInfo info, Condition[] conditions) {
        Object obj = null;
        synchronized (this.mMutex) {
            if (this.DEBUG) {
                String str = this.TAG;
                StringBuilder append = new StringBuilder().append("notifyConditions pkg=").append(pkg).append(" info=").append(info).append(" conditions=");
                if (conditions != null) {
                    obj = Arrays.asList(conditions);
                }
                Slog.d(str, append.append(obj).toString());
            }
            conditions = removeDuplicateConditions(pkg, conditions);
            if (conditions == null || conditions.length == 0) {
            } else {
                for (Condition c : conditions) {
                    ConditionRecord r = getRecordLocked(c.id, info.component, true);
                    r.info = info;
                    r.condition = c;
                }
            }
        }
    }

    public IConditionProvider findConditionProvider(ComponentName component) {
        if (component == null) {
            return null;
        }
        for (ManagedServiceInfo service : getServices()) {
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

    protected ArraySet<ComponentName> loadComponentNamesFromSetting(String settingName, int userId) {
        String settingValue = Secure.getStringForUser(this.mContext.getContentResolver(), settingName, userId);
        if (TextUtils.isEmpty(settingValue)) {
            return new ArraySet();
        }
        String[] packages = settingValue.split(":");
        ArraySet<ComponentName> result = new ArraySet(packages.length);
        for (int i = 0; i < packages.length; i++) {
            if (!TextUtils.isEmpty(packages[i])) {
                ComponentName component = ComponentName.unflattenFromString(packages[i]);
                if (component != null) {
                    result.addAll(queryPackageForServices(component.getPackageName(), userId));
                } else {
                    result.addAll(queryPackageForServices(packages[i], userId));
                }
            }
        }
        return result;
    }

    public boolean subscribeIfNecessary(ComponentName component, Uri conditionId) {
        synchronized (this.mMutex) {
            ConditionRecord r = getRecordLocked(conditionId, component, false);
            if (r == null) {
                Slog.w(this.TAG, "Unable to subscribe to " + component + " " + conditionId);
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
                Slog.w(this.TAG, "Unable to unsubscribe to " + component + " " + conditionId);
            } else if (r.subscribed) {
                unsubscribeLocked(r);
            }
        }
    }

    private void subscribeLocked(ConditionRecord r) {
        Uri uri = null;
        if (this.DEBUG) {
            Slog.d(this.TAG, "subscribeLocked " + r);
        }
        IConditionProvider provider = provider(r);
        RemoteException re = null;
        if (provider != null) {
            try {
                Slog.d(this.TAG, "Subscribing to " + r.id + " with " + r.component);
                provider.onSubscribe(r.id);
                r.subscribed = true;
            } catch (RemoteException e) {
                Slog.w(this.TAG, "Error subscribing to " + r, e);
                re = e;
            }
        }
        if (r != null) {
            uri = r.id;
        }
        ZenLog.traceSubscribe(uri, provider, re);
    }

    @SafeVarargs
    private static <T> ArraySet<T> safeSet(T... items) {
        ArraySet<T> rt = new ArraySet();
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
        Uri uri = null;
        if (this.DEBUG) {
            Slog.d(this.TAG, "unsubscribeLocked " + r);
        }
        IConditionProvider provider = provider(r);
        RemoteException re = null;
        if (provider != null) {
            try {
                provider.onUnsubscribe(r.id);
            } catch (RemoteException e) {
                Slog.w(this.TAG, "Error unsubscribing to " + r, e);
                re = e;
            }
            r.subscribed = false;
        }
        if (r != null) {
            uri = r.id;
        }
        ZenLog.traceUnsubscribe(uri, provider, re);
    }

    private static IConditionProvider provider(ConditionRecord r) {
        return r == null ? null : provider(r.info);
    }

    private static IConditionProvider provider(ManagedServiceInfo info) {
        return info == null ? null : (IConditionProvider) info.service;
    }
}
