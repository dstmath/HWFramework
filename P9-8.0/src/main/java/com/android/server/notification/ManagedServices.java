package com.android.server.notification;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.IPackageManager.Stub;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.UserInfo;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.IInterface;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import com.android.internal.util.ArrayUtils;
import com.android.server.am.HwBroadcastRadarUtil;
import com.android.server.notification.NotificationManagerService.DumpFilter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public abstract class ManagedServices {
    protected static final String ENABLED_SERVICES_SEPARATOR = ":";
    protected final boolean DEBUG = Log.isLoggable(this.TAG, 3);
    protected final String TAG = getClass().getSimpleName();
    private final Config mConfig;
    protected final Context mContext;
    private ArraySet<ComponentName> mEnabledServicesForCurrentProfiles = new ArraySet();
    private ArraySet<String> mEnabledServicesPackageNames = new ArraySet();
    private int[] mLastSeenProfileIds;
    protected final Object mMutex;
    private final IPackageManager mPm;
    private final BroadcastReceiver mRestoreReceiver;
    private ArraySet<String> mRestored;
    private ArraySet<String> mRestoredPackages = new ArraySet();
    private final ArrayList<ManagedServiceInfo> mServices = new ArrayList();
    private final ArrayList<String> mServicesBinding = new ArrayList();
    private final SettingsObserver mSettingsObserver;
    private ArraySet<ComponentName> mSnoozingForCurrentProfiles = new ArraySet();
    private final UserProfiles mUserProfiles;

    public static class Config {
        public String bindPermission;
        public String caption;
        public int clientLabel;
        public String secondarySettingName;
        public String secureSettingName;
        public String serviceInterface;
        public String settingsAction;
    }

    public class ManagedServiceInfo implements DeathRecipient {
        public ComponentName component;
        public ServiceConnection connection;
        public boolean isSystem;
        public IInterface service;
        public int targetSdkVersion;
        public int userid;

        public ManagedServiceInfo(IInterface service, ComponentName component, int userid, boolean isSystem, ServiceConnection connection, int targetSdkVersion) {
            this.service = service;
            this.component = component;
            this.userid = userid;
            this.isSystem = isSystem;
            this.connection = connection;
            this.targetSdkVersion = targetSdkVersion;
        }

        public boolean isGuest(ManagedServices host) {
            return ManagedServices.this != host;
        }

        public ManagedServices getOwner() {
            return ManagedServices.this;
        }

        public String toString() {
            String str = null;
            StringBuilder append = new StringBuilder("ManagedServiceInfo[").append("component=").append(this.component).append(",userid=").append(this.userid).append(",isSystem=").append(this.isSystem).append(",targetSdkVersion=").append(this.targetSdkVersion).append(",connection=");
            if (this.connection != null) {
                str = "<connection>";
            }
            return append.append(str).append(",service=").append(this.service).append(']').toString();
        }

        public boolean enabledAndUserMatches(int nid) {
            boolean z = false;
            if (!isEnabledForCurrentProfiles()) {
                return false;
            }
            if (this.userid == -1 || this.isSystem || nid == -1 || nid == this.userid) {
                return true;
            }
            if (supportsProfiles() && ManagedServices.this.mUserProfiles.isCurrentProfile(nid)) {
                z = isPermittedForProfile(nid);
            }
            return z;
        }

        public boolean supportsProfiles() {
            return this.targetSdkVersion >= 21;
        }

        public void binderDied() {
            if (ManagedServices.this.DEBUG) {
                Slog.d(ManagedServices.this.TAG, "binderDied");
            }
            ManagedServices.this.removeServiceImpl(this.service, this.userid);
        }

        public boolean isEnabledForCurrentProfiles() {
            if (this.isSystem) {
                return true;
            }
            if (this.connection == null) {
                return false;
            }
            boolean contains;
            synchronized (ManagedServices.this.mMutex) {
                contains = ManagedServices.this.mEnabledServicesForCurrentProfiles.contains(this.component);
            }
            return contains;
        }

        public boolean isPermittedForProfile(int userId) {
            if (!ManagedServices.this.mUserProfiles.isManagedProfile(userId)) {
                return true;
            }
            DevicePolicyManager dpm = (DevicePolicyManager) ManagedServices.this.mContext.getSystemService("device_policy");
            long identity = Binder.clearCallingIdentity();
            try {
                boolean isNotificationListenerServicePermitted = dpm.isNotificationListenerServicePermitted(this.component.getPackageName(), userId);
                return isNotificationListenerServicePermitted;
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    class SettingRestoredReceiver extends BroadcastReceiver {
        SettingRestoredReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if ("android.os.action.SETTING_RESTORED".equals(intent.getAction())) {
                String element = intent.getStringExtra("setting_name");
                if (Objects.equals(element, ManagedServices.this.mConfig.secureSettingName) || Objects.equals(element, ManagedServices.this.mConfig.secondarySettingName)) {
                    ManagedServices.this.settingRestored(element, intent.getStringExtra("previous_value"), intent.getStringExtra("new_value"), getSendingUserId());
                }
            }
        }
    }

    private class SettingsObserver extends ContentObserver {
        private final Uri mSecondarySettingsUri;
        private final Uri mSecureSettingsUri;

        /* synthetic */ SettingsObserver(ManagedServices this$0, Handler handler, SettingsObserver -this2) {
            this(handler);
        }

        private SettingsObserver(Handler handler) {
            super(handler);
            this.mSecureSettingsUri = Secure.getUriFor(ManagedServices.this.mConfig.secureSettingName);
            if (ManagedServices.this.mConfig.secondarySettingName != null) {
                this.mSecondarySettingsUri = Secure.getUriFor(ManagedServices.this.mConfig.secondarySettingName);
            } else {
                this.mSecondarySettingsUri = null;
            }
        }

        private void observe() {
            ContentResolver resolver = ManagedServices.this.mContext.getContentResolver();
            resolver.registerContentObserver(this.mSecureSettingsUri, false, this, -1);
            if (this.mSecondarySettingsUri != null) {
                resolver.registerContentObserver(this.mSecondarySettingsUri, false, this, -1);
            }
            update(null);
        }

        public void onChange(boolean selfChange, Uri uri) {
            update(uri);
        }

        private void update(Uri uri) {
            if (uri == null || this.mSecureSettingsUri.equals(uri) || uri.equals(this.mSecondarySettingsUri)) {
                if (ManagedServices.this.DEBUG) {
                    Slog.d(ManagedServices.this.TAG, "Setting changed: uri=" + uri);
                }
                ManagedServices.this.rebindServices(false);
                ManagedServices.this.rebuildRestoredPackages();
            }
        }
    }

    public static class UserProfiles {
        private final SparseArray<UserInfo> mCurrentProfiles = new SparseArray();

        public void updateCache(Context context) {
            UserManager userManager = (UserManager) context.getSystemService("user");
            if (userManager != null) {
                List<UserInfo> profiles = userManager.getProfiles(ActivityManager.getCurrentUser());
                synchronized (this.mCurrentProfiles) {
                    this.mCurrentProfiles.clear();
                    for (UserInfo user : profiles) {
                        this.mCurrentProfiles.put(user.id, user);
                    }
                }
            }
        }

        public int[] getCurrentProfileIds() {
            int[] users;
            synchronized (this.mCurrentProfiles) {
                users = new int[this.mCurrentProfiles.size()];
                int N = this.mCurrentProfiles.size();
                for (int i = 0; i < N; i++) {
                    users[i] = this.mCurrentProfiles.keyAt(i);
                }
            }
            return users;
        }

        public boolean isCurrentProfile(int userId) {
            boolean z;
            synchronized (this.mCurrentProfiles) {
                z = this.mCurrentProfiles.get(userId) != null;
            }
            return z;
        }

        public boolean isManagedProfile(int userId) {
            boolean isManagedProfile;
            synchronized (this.mCurrentProfiles) {
                UserInfo user = (UserInfo) this.mCurrentProfiles.get(userId);
                isManagedProfile = user != null ? user.isManagedProfile() : false;
            }
            return isManagedProfile;
        }
    }

    protected abstract IInterface asInterface(IBinder iBinder);

    protected abstract boolean checkType(IInterface iInterface);

    protected abstract Config getConfig();

    protected abstract void onServiceAdded(ManagedServiceInfo managedServiceInfo);

    public ManagedServices(Context context, Handler handler, Object mutex, UserProfiles userProfiles) {
        this.mContext = context;
        this.mMutex = mutex;
        this.mUserProfiles = userProfiles;
        this.mPm = Stub.asInterface(ServiceManager.getService(HwBroadcastRadarUtil.KEY_PACKAGE));
        this.mConfig = getConfig();
        this.mSettingsObserver = new SettingsObserver(this, handler, null);
        this.mRestoreReceiver = new SettingRestoredReceiver();
        context.registerReceiver(this.mRestoreReceiver, new IntentFilter("android.os.action.SETTING_RESTORED"));
        rebuildRestoredPackages();
    }

    private String getCaption() {
        return this.mConfig.caption;
    }

    protected List<ManagedServiceInfo> getServices() {
        List<ManagedServiceInfo> services;
        synchronized (this.mMutex) {
            services = new ArrayList(this.mServices);
        }
        return services;
    }

    protected void onServiceRemovedLocked(ManagedServiceInfo removed) {
    }

    private ManagedServiceInfo newServiceInfo(IInterface service, ComponentName component, int userid, boolean isSystem, ServiceConnection connection, int targetSdkVersion) {
        return new ManagedServiceInfo(service, component, userid, isSystem, connection, targetSdkVersion);
    }

    public void onBootPhaseAppsCanStart() {
        this.mSettingsObserver.observe();
    }

    public void dump(PrintWriter pw, DumpFilter filter) {
        pw.println("    All " + getCaption() + "s (" + this.mEnabledServicesForCurrentProfiles.size() + ") enabled for current profiles:");
        for (ComponentName cmpt : this.mEnabledServicesForCurrentProfiles) {
            if (filter == null || (filter.matches(cmpt) ^ 1) == 0) {
                pw.println("      " + cmpt);
            }
        }
        pw.println("    Live " + getCaption() + "s (" + this.mServices.size() + "):");
        for (ManagedServiceInfo info : this.mServices) {
            if (filter == null || (filter.matches(info.component) ^ 1) == 0) {
                pw.println("      " + info.component + " (user " + info.userid + "): " + info.service + (info.isSystem ? " SYSTEM" : "") + (info.isGuest(this) ? " GUEST" : ""));
            }
        }
        pw.println("    Snoozed " + getCaption() + "s (" + this.mSnoozingForCurrentProfiles.size() + "):");
        for (ComponentName name : this.mSnoozingForCurrentProfiles) {
            pw.println("      " + name.flattenToShortString());
        }
    }

    public static String restoredSettingName(String setting) {
        return setting + ":restored";
    }

    public void settingRestored(String element, String oldValue, String newValue, int userid) {
        if (this.DEBUG) {
            Slog.d(this.TAG, "Restored managed service setting: " + element + " ovalue=" + oldValue + " nvalue=" + newValue);
        }
        if ((this.mConfig.secureSettingName.equals(element) || this.mConfig.secondarySettingName.equals(element)) && element != null) {
            Secure.putStringForUser(this.mContext.getContentResolver(), restoredSettingName(element), newValue, userid);
            if (this.mConfig.secureSettingName.equals(element)) {
                updateSettingsAccordingToInstalledServices(element, userid);
            }
            rebuildRestoredPackages();
        }
    }

    public boolean isComponentEnabledForPackage(String pkg) {
        return this.mEnabledServicesPackageNames.contains(pkg);
    }

    public void onPackagesChanged(boolean removingPackage, String[] pkgList) {
        Object obj = null;
        if (this.DEBUG) {
            String str = this.TAG;
            StringBuilder append = new StringBuilder().append("onPackagesChanged removingPackage=").append(removingPackage).append(" pkgList=");
            if (pkgList != null) {
                obj = Arrays.asList(pkgList);
            }
            Slog.d(str, append.append(obj).append(" mEnabledServicesPackageNames=").append(this.mEnabledServicesPackageNames).toString());
        }
        boolean anyServicesInvolved = false;
        if (pkgList != null && pkgList.length > 0) {
            for (String pkgName : pkgList) {
                if (this.mEnabledServicesPackageNames.contains(pkgName) || this.mRestoredPackages.contains(pkgName)) {
                    anyServicesInvolved = true;
                }
            }
        }
        if (anyServicesInvolved) {
            if (removingPackage) {
                updateSettingsAccordingToInstalledServices();
                rebuildRestoredPackages();
            }
            if (pkgList == null || pkgList.length <= 0 || !ArrayUtils.contains(pkgList, "com.huawei.bone")) {
                rebindServices(false);
            } else {
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        Slog.d(ManagedServices.this.TAG, "rebind com.huawei.bone service 500ms later");
                        ManagedServices.this.rebindServices(false);
                    }
                }, 500);
            }
        }
    }

    public void onUserSwitched(int user) {
        if (this.DEBUG) {
            Slog.d(this.TAG, "onUserSwitched u=" + user);
        }
        rebuildRestoredPackages();
        if (Arrays.equals(this.mLastSeenProfileIds, this.mUserProfiles.getCurrentProfileIds())) {
            if (this.DEBUG) {
                Slog.d(this.TAG, "Current profile IDs didn't change, skipping rebindServices().");
            }
            return;
        }
        rebindServices(true);
    }

    public void onUserUnlocked(int user) {
        if (this.DEBUG) {
            Slog.d(this.TAG, "onUserUnlocked u=" + user);
        }
        rebuildRestoredPackages();
        rebindServices(false);
    }

    public ManagedServiceInfo getServiceFromTokenLocked(IInterface service) {
        if (service == null) {
            return null;
        }
        IBinder token = service.asBinder();
        int N = this.mServices.size();
        for (int i = 0; i < N; i++) {
            ManagedServiceInfo info = (ManagedServiceInfo) this.mServices.get(i);
            if (info.service.asBinder() == token) {
                return info;
            }
        }
        return null;
    }

    public ManagedServiceInfo checkServiceTokenLocked(IInterface service) {
        checkNotNull(service);
        ManagedServiceInfo info = getServiceFromTokenLocked(service);
        if (info != null) {
            return info;
        }
        throw new SecurityException("Disallowed call from unknown " + getCaption() + ": " + service);
    }

    public void unregisterService(IInterface service, int userid) {
        checkNotNull(service);
        unregisterServiceImpl(service, userid);
    }

    public void registerService(IInterface service, ComponentName component, int userid) {
        checkNotNull(service);
        ManagedServiceInfo info = registerServiceImpl(service, component, userid);
        if (info != null) {
            onServiceAdded(info);
        }
    }

    public void registerGuestService(ManagedServiceInfo guest) {
        checkNotNull(guest.service);
        if (!checkType(guest.service)) {
            throw new IllegalArgumentException();
        } else if (registerServiceImpl(guest) != null) {
            onServiceAdded(guest);
        }
    }

    public void setComponentState(ComponentName component, boolean enabled) {
        if ((this.mSnoozingForCurrentProfiles.contains(component) ^ 1) != enabled) {
            if (enabled) {
                this.mSnoozingForCurrentProfiles.remove(component);
            } else {
                this.mSnoozingForCurrentProfiles.add(component);
            }
            if (this.DEBUG) {
                Slog.d(this.TAG, (enabled ? "Enabling " : "Disabling ") + "component " + component.flattenToShortString());
            }
            synchronized (this.mMutex) {
                for (int userId : this.mUserProfiles.getCurrentProfileIds()) {
                    if (enabled) {
                        registerServiceLocked(component, userId);
                    } else {
                        unregisterServiceLocked(component, userId);
                    }
                }
            }
        }
    }

    private void rebuildRestoredPackages() {
        this.mRestoredPackages.clear();
        String secureSettingName = restoredSettingName(this.mConfig.secureSettingName);
        String secondarySettingName = this.mConfig.secondarySettingName == null ? null : restoredSettingName(this.mConfig.secondarySettingName);
        int[] userIds = this.mUserProfiles.getCurrentProfileIds();
        int N = userIds.length;
        for (int i = 0; i < N; i++) {
            ArraySet<ComponentName> names = loadComponentNamesFromSetting(secureSettingName, userIds[i]);
            if (secondarySettingName != null) {
                names.addAll(loadComponentNamesFromSetting(secondarySettingName, userIds[i]));
            }
            for (ComponentName name : names) {
                this.mRestoredPackages.add(name.getPackageName());
            }
        }
    }

    protected ArraySet<ComponentName> loadComponentNamesFromSetting(String settingName, int userId) {
        String settingValue = Secure.getStringForUser(this.mContext.getContentResolver(), settingName, userId);
        if (TextUtils.isEmpty(settingValue)) {
            return new ArraySet();
        }
        String[] restored = settingValue.split(ENABLED_SERVICES_SEPARATOR);
        ArraySet<ComponentName> result = new ArraySet(restored.length);
        for (String unflattenFromString : restored) {
            ComponentName value = ComponentName.unflattenFromString(unflattenFromString);
            if (value != null) {
                result.add(value);
            }
        }
        return result;
    }

    private void storeComponentsToSetting(Set<ComponentName> components, String settingName, int userId) {
        String value;
        Object[] componentNames = null;
        if (components != null) {
            componentNames = new String[components.size()];
            int index = 0;
            for (ComponentName c : components) {
                int index2 = index + 1;
                componentNames[index] = c.flattenToString();
                index = index2;
            }
        }
        if (componentNames == null) {
            value = "";
        } else {
            value = TextUtils.join(ENABLED_SERVICES_SEPARATOR, componentNames);
        }
        Secure.putStringForUser(this.mContext.getContentResolver(), settingName, value, userId);
    }

    private void updateSettingsAccordingToInstalledServices() {
        int[] userIds = this.mUserProfiles.getCurrentProfileIds();
        int N = userIds.length;
        for (int i = 0; i < N; i++) {
            updateSettingsAccordingToInstalledServices(this.mConfig.secureSettingName, userIds[i]);
            if (this.mConfig.secondarySettingName != null) {
                updateSettingsAccordingToInstalledServices(this.mConfig.secondarySettingName, userIds[i]);
            }
        }
        rebuildRestoredPackages();
    }

    protected Set<ComponentName> queryPackageForServices(String packageName, int userId) {
        Set<ComponentName> installed = new ArraySet();
        PackageManager pm = this.mContext.getPackageManager();
        Intent queryIntent = new Intent(this.mConfig.serviceInterface);
        if (!TextUtils.isEmpty(packageName)) {
            queryIntent.setPackage(packageName);
        }
        List<ResolveInfo> installedServices = pm.queryIntentServicesAsUser(queryIntent, 132, userId);
        if (this.DEBUG) {
            Slog.v(this.TAG, this.mConfig.serviceInterface + " services: " + installedServices);
        }
        if (installedServices != null) {
            int count = installedServices.size();
            for (int i = 0; i < count; i++) {
                ServiceInfo info = ((ResolveInfo) installedServices.get(i)).serviceInfo;
                ComponentName component = new ComponentName(info.packageName, info.name);
                if (this.mConfig.bindPermission.equals(info.permission)) {
                    installed.add(component);
                } else {
                    Slog.w(this.TAG, "Skipping " + getCaption() + " service " + info.packageName + "/" + info.name + ": it does not require the permission " + this.mConfig.bindPermission);
                }
            }
        }
        return installed;
    }

    private void updateSettingsAccordingToInstalledServices(String setting, int userId) {
        int i = 0;
        boolean restoredChanged = false;
        boolean currentChanged = false;
        Set<ComponentName> restored = loadComponentNamesFromSetting(restoredSettingName(setting), userId);
        Set<ComponentName> current = loadComponentNamesFromSetting(setting, userId);
        Set<ComponentName> installed = queryPackageForServices(null, userId);
        ArraySet<ComponentName> retained = new ArraySet();
        for (ComponentName component : installed) {
            if (restored != null && restored.remove(component)) {
                if (this.DEBUG) {
                    Slog.v(this.TAG, "Restoring " + component + " for user " + userId);
                }
                restoredChanged = true;
                currentChanged = true;
                retained.add(component);
            } else if (current != null && current.contains(component)) {
                retained.add(component);
            }
        }
        if ((current == null ? 0 : current.size()) != retained.size()) {
            i = 1;
        }
        if (currentChanged | i) {
            if (this.DEBUG) {
                Slog.v(this.TAG, "List of  " + getCaption() + " services was updated " + current);
            }
            storeComponentsToSetting(retained, setting, userId);
        }
        if (restoredChanged) {
            if (this.DEBUG) {
                Slog.v(this.TAG, "List of  " + getCaption() + " restored services was updated " + restored);
            }
            storeComponentsToSetting(restored, restoredSettingName(setting), userId);
        }
    }

    private void rebindServices(boolean forceRebind) {
        int i;
        ComponentName component;
        if (this.DEBUG) {
            Slog.d(this.TAG, "rebindServices");
        }
        int[] userIds = this.mUserProfiles.getCurrentProfileIds();
        int nUserIds = userIds.length;
        SparseArray<ArraySet<ComponentName>> componentsByUser = new SparseArray();
        for (i = 0; i < nUserIds; i++) {
            componentsByUser.put(userIds[i], loadComponentNamesFromSetting(this.mConfig.secureSettingName, userIds[i]));
            if (this.mConfig.secondarySettingName != null) {
                ((ArraySet) componentsByUser.get(userIds[i])).addAll(loadComponentNamesFromSetting(this.mConfig.secondarySettingName, userIds[i]));
            }
        }
        ArrayList<ManagedServiceInfo> removableBoundServices = new ArrayList();
        SparseArray<Set<ComponentName>> toAdd = new SparseArray();
        synchronized (this.mMutex) {
            for (ManagedServiceInfo service : this.mServices) {
                if (!(service.isSystem || (service.isGuest(this) ^ 1) == 0)) {
                    removableBoundServices.add(service);
                }
            }
            this.mEnabledServicesForCurrentProfiles.clear();
            this.mEnabledServicesPackageNames.clear();
            for (i = 0; i < nUserIds; i++) {
                ArraySet<ComponentName> userComponents = (ArraySet) componentsByUser.get(userIds[i]);
                if (userComponents == null) {
                    toAdd.put(userIds[i], new ArraySet());
                } else {
                    Set<ComponentName> add = new HashSet(userComponents);
                    add.removeAll(this.mSnoozingForCurrentProfiles);
                    toAdd.put(userIds[i], add);
                    this.mEnabledServicesForCurrentProfiles.addAll(userComponents);
                    for (int j = 0; j < userComponents.size(); j++) {
                        component = (ComponentName) userComponents.valueAt(j);
                        this.mEnabledServicesPackageNames.add(component.getPackageName());
                    }
                }
            }
        }
        for (ManagedServiceInfo info : removableBoundServices) {
            component = info.component;
            int oldUser = info.userid;
            Set<ComponentName> allowedComponents = (Set) toAdd.get(info.userid);
            if (allowedComponents != null) {
                if (!allowedComponents.contains(component) || (forceRebind ^ 1) == 0) {
                    Slog.v(this.TAG, "disabling " + getCaption() + " for user " + oldUser + ": " + component);
                    unregisterService(component, oldUser);
                } else {
                    allowedComponents.remove(component);
                }
            }
        }
        for (i = 0; i < nUserIds; i++) {
            for (ComponentName component2 : (Set) toAdd.get(userIds[i])) {
                try {
                    ServiceInfo info2 = this.mPm.getServiceInfo(component2, 786432, userIds[i]);
                    if (info2 == null || (this.mConfig.bindPermission.equals(info2.permission) ^ 1) != 0) {
                        Slog.w(this.TAG, "Skipping " + getCaption() + " service " + component2 + ": it does not require the permission " + this.mConfig.bindPermission);
                    } else {
                        Slog.v(this.TAG, "enabling " + getCaption() + " for " + userIds[i] + ": " + component2);
                        registerService(component2, userIds[i]);
                    }
                } catch (RemoteException e) {
                    e.rethrowFromSystemServer();
                }
            }
        }
        this.mLastSeenProfileIds = userIds;
    }

    private void registerService(ComponentName name, int userid) {
        synchronized (this.mMutex) {
            registerServiceLocked(name, userid);
        }
    }

    public void registerSystemService(ComponentName name, int userid) {
        synchronized (this.mMutex) {
            registerServiceLocked(name, userid, true);
        }
    }

    private void registerServiceLocked(ComponentName name, int userid) {
        registerServiceLocked(name, userid, false);
    }

    private void registerServiceLocked(ComponentName name, int userid, boolean isSystem) {
        if (this.DEBUG) {
            Slog.v(this.TAG, "registerService: " + name + " u=" + userid);
        }
        final String servicesBindingTag = name.toString() + "/" + userid;
        if (!this.mServicesBinding.contains(servicesBindingTag)) {
            this.mServicesBinding.add(servicesBindingTag);
        }
        for (int i = this.mServices.size() - 1; i >= 0; i--) {
            ManagedServiceInfo info = (ManagedServiceInfo) this.mServices.get(i);
            if (name.equals(info.component) && info.userid == userid) {
                if (this.DEBUG) {
                    Slog.v(this.TAG, "    disconnecting old " + getCaption() + ": " + info.service);
                }
                removeServiceLocked(i);
                if (info.connection != null) {
                    this.mContext.unbindService(info.connection);
                }
            }
        }
        Intent intent = new Intent(this.mConfig.serviceInterface);
        intent.setComponent(name);
        intent.putExtra("android.intent.extra.client_label", this.mConfig.clientLabel);
        intent.putExtra("android.intent.extra.client_intent", PendingIntent.getActivity(this.mContext, 0, new Intent(this.mConfig.settingsAction), 0));
        ApplicationInfo appInfo = null;
        try {
            appInfo = this.mContext.getPackageManager().getApplicationInfo(name.getPackageName(), 0);
        } catch (NameNotFoundException e) {
        }
        final int targetSdkVersion = appInfo != null ? appInfo.targetSdkVersion : 1;
        try {
            if (this.DEBUG) {
                Slog.v(this.TAG, "binding: " + intent);
            }
            final int i2 = userid;
            final boolean z = isSystem;
            if (!this.mContext.bindServiceAsUser(intent, new ServiceConnection() {
                IInterface mService;

                public void onServiceConnected(ComponentName name, IBinder binder) {
                    boolean added = false;
                    ManagedServiceInfo info = null;
                    synchronized (ManagedServices.this.mMutex) {
                        if (ManagedServices.this.mServicesBinding.contains(servicesBindingTag)) {
                            Slog.d(ManagedServices.this.TAG, "onServiceConnected, just remove the servicesBindingTag and add service.");
                            ManagedServices.this.mServicesBinding.remove(servicesBindingTag);
                            try {
                                this.mService = ManagedServices.this.asInterface(binder);
                                info = ManagedServices.this.newServiceInfo(this.mService, name, i2, z, this, targetSdkVersion);
                                binder.linkToDeath(info, 0);
                                added = ManagedServices.this.mServices.add(info);
                            } catch (RemoteException e) {
                            }
                        } else {
                            Slog.d(ManagedServices.this.TAG, "service has been connected, just return.");
                            ManagedServices.this.mContext.unbindService(this);
                            return;
                        }
                    }
                    if (added) {
                        ManagedServices.this.onServiceAdded(info);
                    }
                }

                public void onServiceDisconnected(ComponentName name) {
                    Slog.v(ManagedServices.this.TAG, ManagedServices.this.getCaption() + " connection lost: " + name);
                }
            }, 83886081, new UserHandle(userid))) {
                this.mServicesBinding.remove(servicesBindingTag);
                Slog.w(this.TAG, "Unable to bind " + getCaption() + " service: " + intent);
            }
        } catch (SecurityException ex) {
            Slog.e(this.TAG, "Unable to bind " + getCaption() + " service: " + intent, ex);
        }
    }

    private void unregisterService(ComponentName name, int userid) {
        synchronized (this.mMutex) {
            unregisterServiceLocked(name, userid);
        }
    }

    private void unregisterServiceLocked(ComponentName name, int userid) {
        for (int i = this.mServices.size() - 1; i >= 0; i--) {
            ManagedServiceInfo info = (ManagedServiceInfo) this.mServices.get(i);
            if (name.equals(info.component) && info.userid == userid) {
                removeServiceLocked(i);
                if (info.connection != null) {
                    try {
                        this.mContext.unbindService(info.connection);
                    } catch (IllegalArgumentException ex) {
                        Slog.e(this.TAG, getCaption() + " " + name + " could not be unbound: " + ex);
                    }
                }
            }
        }
    }

    private ManagedServiceInfo removeServiceImpl(IInterface service, int userid) {
        if (this.DEBUG) {
            Slog.d(this.TAG, "removeServiceImpl service=" + service + " u=" + userid);
        }
        ManagedServiceInfo serviceInfo = null;
        synchronized (this.mMutex) {
            for (int i = this.mServices.size() - 1; i >= 0; i--) {
                ManagedServiceInfo info = (ManagedServiceInfo) this.mServices.get(i);
                if (info.service.asBinder() == service.asBinder() && info.userid == userid) {
                    if (this.DEBUG) {
                        Slog.d(this.TAG, "Removing active service " + info.component);
                    }
                    serviceInfo = removeServiceLocked(i);
                }
            }
        }
        return serviceInfo;
    }

    private ManagedServiceInfo removeServiceLocked(int i) {
        ManagedServiceInfo info = (ManagedServiceInfo) this.mServices.remove(i);
        onServiceRemovedLocked(info);
        if (info != null) {
            try {
                info.service.asBinder().unlinkToDeath(info, 0);
            } catch (Exception e) {
                if (this.DEBUG) {
                    Slog.d(this.TAG, "Death link does not exist , error msg: " + e.getMessage());
                }
            }
        }
        return info;
    }

    private void checkNotNull(IInterface service) {
        if (service == null) {
            throw new IllegalArgumentException(getCaption() + " must not be null");
        }
    }

    private ManagedServiceInfo registerServiceImpl(IInterface service, ComponentName component, int userid) {
        return registerServiceImpl(newServiceInfo(service, component, userid, true, null, 21));
    }

    private ManagedServiceInfo registerServiceImpl(ManagedServiceInfo info) {
        synchronized (this.mMutex) {
            try {
                info.service.asBinder().linkToDeath(info, 0);
                this.mServices.add(info);
            } catch (RemoteException e) {
                return null;
            }
        }
        return info;
    }

    private void unregisterServiceImpl(IInterface service, int userid) {
        ManagedServiceInfo info = removeServiceImpl(service, userid);
        if (info != null && info.connection != null && (info.isGuest(this) ^ 1) != 0) {
            this.mContext.unbindService(info.connection);
        }
    }

    public boolean isComponentEnabledForCurrentProfiles(ComponentName component) {
        boolean contains;
        synchronized (this.mMutex) {
            contains = this.mEnabledServicesForCurrentProfiles.contains(component);
        }
        return contains;
    }
}
