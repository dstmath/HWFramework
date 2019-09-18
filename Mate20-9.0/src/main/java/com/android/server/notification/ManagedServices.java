package com.android.server.notification;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.UserInfo;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.util.proto.ProtoOutputStream;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.XmlUtils;
import com.android.server.job.controllers.JobStatus;
import com.android.server.notification.NotificationManagerService;
import com.android.server.slice.SliceClientPermissions;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public abstract class ManagedServices {
    static final int APPROVAL_BY_COMPONENT = 1;
    static final int APPROVAL_BY_PACKAGE = 0;
    static final String ATT_APPROVED_LIST = "approved";
    static final String ATT_IS_PRIMARY = "primary";
    static final String ATT_USER_ID = "user";
    static final String ATT_VERSION = "version";
    static final int DB_VERSION = 1;
    protected static final String ENABLED_SERVICES_SEPARATOR = ":";
    private static final int ON_BINDING_DIED_REBIND_DELAY_MS = 10000;
    static final String TAG_MANAGED_SERVICES = "service_listing";
    protected final boolean DEBUG = Log.isLoggable(this.TAG, 3);
    protected final String TAG = getClass().getSimpleName();
    protected int mApprovalLevel;
    private ArrayMap<Integer, ArrayMap<Boolean, ArraySet<String>>> mApproved = new ArrayMap<>();
    private final Config mConfig;
    protected final Context mContext;
    /* access modifiers changed from: private */
    public ArraySet<ComponentName> mEnabledServicesForCurrentProfiles = new ArraySet<>();
    private ArraySet<String> mEnabledServicesPackageNames = new ArraySet<>();
    /* access modifiers changed from: private */
    public final Handler mHandler = new Handler(Looper.getMainLooper());
    private int[] mLastSeenProfileIds;
    protected final Object mMutex;
    private final IPackageManager mPm;
    /* access modifiers changed from: private */
    public final ArrayList<ManagedServiceInfo> mServices = new ArrayList<>();
    /* access modifiers changed from: private */
    public final ArrayList<String> mServicesBinding = new ArrayList<>();
    /* access modifiers changed from: private */
    public final ArraySet<String> mServicesRebinding = new ArraySet<>();
    private ArraySet<ComponentName> mSnoozingForCurrentProfiles = new ArraySet<>();
    protected final UserManager mUm;
    private boolean mUseXml;
    /* access modifiers changed from: private */
    public final UserProfiles mUserProfiles;

    public static class Config {
        public String bindPermission;
        public String caption;
        public int clientLabel;
        public String secondarySettingName;
        public String secureSettingName;
        public String serviceInterface;
        public String settingsAction;
        public String xmlTag;
    }

    public class ManagedServiceInfo implements IBinder.DeathRecipient {
        public ComponentName component;
        public ServiceConnection connection;
        public boolean isSystem;
        public IInterface service;
        public int targetSdkVersion;
        public int userid;

        public ManagedServiceInfo(IInterface service2, ComponentName component2, int userid2, boolean isSystem2, ServiceConnection connection2, int targetSdkVersion2) {
            this.service = service2;
            this.component = component2;
            this.userid = userid2;
            this.isSystem = isSystem2;
            this.connection = connection2;
            this.targetSdkVersion = targetSdkVersion2;
        }

        public boolean isGuest(ManagedServices host) {
            return ManagedServices.this != host;
        }

        public ManagedServices getOwner() {
            return ManagedServices.this;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder("ManagedServiceInfo[");
            sb.append("component=");
            sb.append(this.component);
            sb.append(",userid=");
            sb.append(this.userid);
            sb.append(",isSystem=");
            sb.append(this.isSystem);
            sb.append(",targetSdkVersion=");
            sb.append(this.targetSdkVersion);
            sb.append(",connection=");
            sb.append(this.connection == null ? null : "<connection>");
            sb.append(",service=");
            sb.append(this.service);
            sb.append(']');
            return sb.toString();
        }

        public void writeToProto(ProtoOutputStream proto, long fieldId, ManagedServices host) {
            long token = proto.start(fieldId);
            this.component.writeToProto(proto, 1146756268033L);
            proto.write(1120986464258L, this.userid);
            proto.write(1138166333443L, this.service.getClass().getName());
            proto.write(1133871366148L, this.isSystem);
            proto.write(1133871366149L, isGuest(host));
            proto.end(token);
        }

        public boolean enabledAndUserMatches(int nid) {
            boolean z = false;
            if (!isEnabledForCurrentProfiles()) {
                return false;
            }
            if (this.userid == -1 || this.isSystem || nid == -1 || nid == this.userid) {
                return true;
            }
            if (supportsProfiles() && ManagedServices.this.mUserProfiles.isCurrentProfile(nid) && isPermittedForProfile(nid)) {
                z = true;
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
            ManagedServiceInfo unused = ManagedServices.this.removeServiceImpl(this.service, this.userid);
        }

        public boolean isEnabledForCurrentProfiles() {
            boolean contains;
            if (this.isSystem) {
                return true;
            }
            if (this.connection == null) {
                return false;
            }
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
                return dpm.isNotificationListenerServicePermitted(this.component.getPackageName(), userId);
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    public static class UserProfiles {
        private final SparseArray<UserInfo> mCurrentProfiles = new SparseArray<>();

        public void updateCache(Context context) {
            UserManager userManager = (UserManager) context.getSystemService(ManagedServices.ATT_USER_ID);
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
            boolean z;
            synchronized (this.mCurrentProfiles) {
                UserInfo user = this.mCurrentProfiles.get(userId);
                z = user != null && user.isManagedProfile();
            }
            return z;
        }
    }

    /* access modifiers changed from: protected */
    public abstract IInterface asInterface(IBinder iBinder);

    /* access modifiers changed from: protected */
    public abstract boolean checkType(IInterface iInterface);

    /* access modifiers changed from: protected */
    public abstract Config getConfig();

    /* access modifiers changed from: protected */
    public abstract void onServiceAdded(ManagedServiceInfo managedServiceInfo);

    public ManagedServices(Context context, Object mutex, UserProfiles userProfiles, IPackageManager pm) {
        this.mContext = context;
        this.mMutex = mutex;
        this.mUserProfiles = userProfiles;
        this.mPm = pm;
        this.mConfig = getConfig();
        this.mApprovalLevel = 1;
        this.mUm = (UserManager) this.mContext.getSystemService(ATT_USER_ID);
    }

    /* access modifiers changed from: private */
    public String getCaption() {
        return this.mConfig.caption;
    }

    /* access modifiers changed from: protected */
    public List<ManagedServiceInfo> getServices() {
        List<ManagedServiceInfo> services;
        synchronized (this.mMutex) {
            services = new ArrayList<>(this.mServices);
        }
        return services;
    }

    /* access modifiers changed from: protected */
    public void onServiceRemovedLocked(ManagedServiceInfo removed) {
    }

    /* access modifiers changed from: private */
    public ManagedServiceInfo newServiceInfo(IInterface service, ComponentName component, int userId, boolean isSystem, ServiceConnection connection, int targetSdkVersion) {
        ManagedServiceInfo managedServiceInfo = new ManagedServiceInfo(service, component, userId, isSystem, connection, targetSdkVersion);
        return managedServiceInfo;
    }

    public void onBootPhaseAppsCanStart() {
    }

    public void dump(PrintWriter pw, NotificationManagerService.DumpFilter filter) {
        pw.println("    Allowed " + getCaption() + "s:");
        int N = this.mApproved.size();
        for (int i = 0; i < N; i++) {
            int userId = this.mApproved.keyAt(i).intValue();
            ArrayMap<Boolean, ArraySet<String>> approvedByType = this.mApproved.valueAt(i);
            if (approvedByType != null) {
                int M = approvedByType.size();
                for (int j = 0; j < M; j++) {
                    boolean isPrimary = approvedByType.keyAt(j).booleanValue();
                    ArraySet<String> approved = approvedByType.valueAt(j);
                    if (approvedByType != null && approvedByType.size() > 0) {
                        pw.println("      " + String.join(ENABLED_SERVICES_SEPARATOR, approved) + " (user: " + userId + " isPrimary: " + isPrimary + ")");
                    }
                }
            }
        }
        pw.println("    All " + getCaption() + "s (" + this.mEnabledServicesForCurrentProfiles.size() + ") enabled for current profiles:");
        Iterator<ComponentName> it = this.mEnabledServicesForCurrentProfiles.iterator();
        while (it.hasNext()) {
            ComponentName cmpt = it.next();
            if (filter == null || filter.matches(cmpt)) {
                pw.println("      " + cmpt);
            }
        }
        pw.println("    Live " + getCaption() + "s (" + this.mServices.size() + "):");
        Iterator<ManagedServiceInfo> it2 = this.mServices.iterator();
        while (it2.hasNext()) {
            ManagedServiceInfo info = it2.next();
            if (filter == null || filter.matches(info.component)) {
                StringBuilder sb = new StringBuilder();
                sb.append("      ");
                sb.append(info.component);
                sb.append(" (user ");
                sb.append(info.userid);
                sb.append("): ");
                sb.append(info.service);
                sb.append(info.isSystem ? " SYSTEM" : BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
                sb.append(info.isGuest(this) ? " GUEST" : BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
                pw.println(sb.toString());
            }
        }
        pw.println("    Snoozed " + getCaption() + "s (" + this.mSnoozingForCurrentProfiles.size() + "):");
        Iterator<ComponentName> it3 = this.mSnoozingForCurrentProfiles.iterator();
        while (it3.hasNext()) {
            pw.println("      " + it3.next().flattenToShortString());
        }
    }

    public void dump(ProtoOutputStream proto, NotificationManagerService.DumpFilter filter) {
        int i;
        ProtoOutputStream protoOutputStream = proto;
        NotificationManagerService.DumpFilter dumpFilter = filter;
        protoOutputStream.write(1138166333441L, getCaption());
        int N = this.mApproved.size();
        int i2 = 0;
        while (i2 < N) {
            int userId = this.mApproved.keyAt(i2).intValue();
            ArrayMap<Boolean, ArraySet<String>> approvedByType = this.mApproved.valueAt(i2);
            if (approvedByType != null) {
                int M = approvedByType.size();
                int j = 0;
                while (j < M) {
                    boolean isPrimary = approvedByType.keyAt(j).booleanValue();
                    ArraySet<String> approved = approvedByType.valueAt(j);
                    if (approvedByType == null || approvedByType.size() <= 0) {
                        i = i2;
                    } else {
                        long sToken = protoOutputStream.start(2246267895810L);
                        Iterator<String> it = approved.iterator();
                        while (it.hasNext()) {
                            protoOutputStream.write(2237677961217L, it.next());
                            i2 = i2;
                        }
                        i = i2;
                        protoOutputStream.write(1120986464258L, userId);
                        protoOutputStream.write(1133871366147L, isPrimary);
                        protoOutputStream.end(sToken);
                    }
                    j++;
                    i2 = i;
                }
            }
            i2++;
        }
        Iterator<ComponentName> it2 = this.mEnabledServicesForCurrentProfiles.iterator();
        while (it2.hasNext()) {
            ComponentName cmpt = it2.next();
            if (dumpFilter == null || dumpFilter.matches(cmpt)) {
                cmpt.writeToProto(protoOutputStream, 2246267895811L);
            }
        }
        Iterator<ManagedServiceInfo> it3 = this.mServices.iterator();
        while (it3.hasNext()) {
            ManagedServiceInfo info = it3.next();
            if (dumpFilter == null || dumpFilter.matches(info.component)) {
                info.writeToProto(protoOutputStream, 2246267895812L, this);
            }
        }
        Iterator<ComponentName> it4 = this.mSnoozingForCurrentProfiles.iterator();
        while (it4.hasNext()) {
            it4.next().writeToProto(protoOutputStream, 2246267895813L);
        }
    }

    /* access modifiers changed from: protected */
    public void onSettingRestored(String element, String value, int backupSdkInt, int userId) {
        if (!this.mUseXml) {
            Slog.d(this.TAG, "Restored managed service setting: " + element);
            if (this.mConfig.secureSettingName.equals(element) || (this.mConfig.secondarySettingName != null && this.mConfig.secondarySettingName.equals(element))) {
                if (backupSdkInt < 26) {
                    String currentSetting = getApproved(userId, this.mConfig.secureSettingName.equals(element));
                    if (!TextUtils.isEmpty(currentSetting)) {
                        if (!TextUtils.isEmpty(value)) {
                            value = value + ENABLED_SERVICES_SEPARATOR + currentSetting;
                        } else {
                            value = currentSetting;
                        }
                    }
                }
                Settings.Secure.putStringForUser(this.mContext.getContentResolver(), element, value, userId);
                loadAllowedComponentsFromSettings();
                rebindServices(false);
            }
        }
    }

    public void writeXml(XmlSerializer out, boolean forBackup) throws IOException {
        out.startTag(null, getConfig().xmlTag);
        out.attribute(null, ATT_VERSION, String.valueOf(1));
        if (forBackup) {
            trimApprovedListsAccordingToInstalledServices();
        }
        int N = this.mApproved.size();
        for (int i = 0; i < N; i++) {
            int userId = this.mApproved.keyAt(i).intValue();
            ArrayMap<Boolean, ArraySet<String>> approvedByType = this.mApproved.valueAt(i);
            if (approvedByType != null) {
                int M = approvedByType.size();
                for (int j = 0; j < M; j++) {
                    boolean isPrimary = approvedByType.keyAt(j).booleanValue();
                    Set<String> approved = approvedByType.valueAt(j);
                    if (approved != null) {
                        String allowedItems = String.join(ENABLED_SERVICES_SEPARATOR, approved);
                        out.startTag(null, TAG_MANAGED_SERVICES);
                        out.attribute(null, ATT_APPROVED_LIST, allowedItems);
                        out.attribute(null, ATT_USER_ID, Integer.toString(userId));
                        out.attribute(null, ATT_IS_PRIMARY, Boolean.toString(isPrimary));
                        out.endTag(null, TAG_MANAGED_SERVICES);
                        if (!forBackup && isPrimary) {
                            Settings.Secure.putStringForUser(this.mContext.getContentResolver(), getConfig().secureSettingName, allowedItems, userId);
                        }
                    }
                }
            }
        }
        out.endTag(null, getConfig().xmlTag);
    }

    /* access modifiers changed from: protected */
    public void migrateToXml() {
        loadAllowedComponentsFromSettings();
    }

    public void readXml(XmlPullParser parser, Predicate<String> allowedManagedServicePackages) throws XmlPullParserException, IOException {
        int xmlVersion = XmlUtils.readIntAttribute(parser, ATT_VERSION, 0);
        for (UserInfo userInfo : this.mUm.getUsers(true)) {
            upgradeXml(xmlVersion, userInfo.getUserHandle().getIdentifier());
        }
        while (true) {
            int next = parser.next();
            int type = next;
            if (next == 1) {
                break;
            }
            String tag = parser.getName();
            if (type == 3 && getConfig().xmlTag.equals(tag)) {
                break;
            } else if (type == 2 && TAG_MANAGED_SERVICES.equals(tag)) {
                String str = this.TAG;
                Slog.i(str, "Read " + this.mConfig.caption + " permissions from xml");
                String approved = XmlUtils.readStringAttribute(parser, ATT_APPROVED_LIST);
                int userId = XmlUtils.readIntAttribute(parser, ATT_USER_ID, 0);
                boolean isPrimary = XmlUtils.readBooleanAttribute(parser, ATT_IS_PRIMARY, true);
                if (allowedManagedServicePackages == null || allowedManagedServicePackages.test(getPackageName(approved))) {
                    if (this.mUm.getUserInfo(userId) != null) {
                        addApprovedList(approved, userId, isPrimary);
                    }
                    this.mUseXml = true;
                }
            }
        }
        rebindServices(false);
    }

    /* access modifiers changed from: protected */
    public void upgradeXml(int xmlVersion, int userId) {
    }

    private void loadAllowedComponentsFromSettings() {
        for (UserInfo user : this.mUm.getUsers()) {
            ContentResolver cr = this.mContext.getContentResolver();
            addApprovedList(Settings.Secure.getStringForUser(cr, getConfig().secureSettingName, user.id), user.id, true);
            if (!TextUtils.isEmpty(getConfig().secondarySettingName)) {
                addApprovedList(Settings.Secure.getStringForUser(cr, getConfig().secondarySettingName, user.id), user.id, false);
            }
        }
        Slog.d(this.TAG, "Done loading approved values from settings");
    }

    /* access modifiers changed from: protected */
    public void addApprovedList(String approved, int userId, boolean isPrimary) {
        if (TextUtils.isEmpty(approved)) {
            approved = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
        }
        ArrayMap<Boolean, ArraySet<String>> approvedByType = this.mApproved.get(Integer.valueOf(userId));
        if (approvedByType == null) {
            approvedByType = new ArrayMap<>();
            this.mApproved.put(Integer.valueOf(userId), approvedByType);
        }
        ArraySet<String> approvedList = approvedByType.get(Boolean.valueOf(isPrimary));
        if (approvedList == null) {
            approvedList = new ArraySet<>();
            approvedByType.put(Boolean.valueOf(isPrimary), approvedList);
        }
        for (String pkgOrComponent : approved.split(ENABLED_SERVICES_SEPARATOR)) {
            String approvedItem = getApprovedValue(pkgOrComponent);
            if (approvedItem != null) {
                approvedList.add(approvedItem);
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean isComponentEnabledForPackage(String pkg) {
        return this.mEnabledServicesPackageNames.contains(pkg);
    }

    /* access modifiers changed from: protected */
    public void setPackageOrComponentEnabled(String pkgOrComponent, int userId, boolean isPrimary, boolean enabled) {
        String str = this.TAG;
        StringBuilder sb = new StringBuilder();
        sb.append(enabled ? " Allowing " : "Disallowing ");
        sb.append(this.mConfig.caption);
        sb.append(" ");
        sb.append(pkgOrComponent);
        Slog.i(str, sb.toString());
        ArrayMap<Boolean, ArraySet<String>> allowedByType = this.mApproved.get(Integer.valueOf(userId));
        if (allowedByType == null) {
            allowedByType = new ArrayMap<>();
            this.mApproved.put(Integer.valueOf(userId), allowedByType);
        }
        ArraySet<String> approved = allowedByType.get(Boolean.valueOf(isPrimary));
        if (approved == null) {
            approved = new ArraySet<>();
            allowedByType.put(Boolean.valueOf(isPrimary), approved);
        }
        String approvedItem = getApprovedValue(pkgOrComponent);
        if (approvedItem != null) {
            if (enabled) {
                approved.add(approvedItem);
            } else {
                approved.remove(approvedItem);
            }
        }
        rebindServices(false);
    }

    private String getApprovedValue(String pkgOrComponent) {
        if (this.mApprovalLevel != 1) {
            return getPackageName(pkgOrComponent);
        }
        if (ComponentName.unflattenFromString(pkgOrComponent) != null) {
            return pkgOrComponent;
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public String getApproved(int userId, boolean primary) {
        return String.join(ENABLED_SERVICES_SEPARATOR, (ArraySet) ((ArrayMap) this.mApproved.getOrDefault(Integer.valueOf(userId), new ArrayMap())).getOrDefault(Boolean.valueOf(primary), new ArraySet()));
    }

    /* access modifiers changed from: protected */
    public List<ComponentName> getAllowedComponents(int userId) {
        List<ComponentName> allowedComponents = new ArrayList<>();
        ArrayMap<Boolean, ArraySet<String>> allowedByType = (ArrayMap) this.mApproved.getOrDefault(Integer.valueOf(userId), new ArrayMap());
        for (int i = 0; i < allowedByType.size(); i++) {
            ArraySet<String> allowed = allowedByType.valueAt(i);
            for (int j = 0; j < allowed.size(); j++) {
                ComponentName cn = ComponentName.unflattenFromString(allowed.valueAt(j));
                if (cn != null) {
                    allowedComponents.add(cn);
                }
            }
        }
        return allowedComponents;
    }

    /* access modifiers changed from: protected */
    public List<String> getAllowedPackages(int userId) {
        List<String> allowedPackages = new ArrayList<>();
        ArrayMap<Boolean, ArraySet<String>> allowedByType = (ArrayMap) this.mApproved.getOrDefault(Integer.valueOf(userId), new ArrayMap());
        for (int i = 0; i < allowedByType.size(); i++) {
            ArraySet<String> allowed = allowedByType.valueAt(i);
            for (int j = 0; j < allowed.size(); j++) {
                String pkgName = getPackageName(allowed.valueAt(j));
                if (!TextUtils.isEmpty(pkgName)) {
                    allowedPackages.add(pkgName);
                }
            }
        }
        return allowedPackages;
    }

    /* access modifiers changed from: protected */
    public boolean isPackageOrComponentAllowed(String pkgOrComponent, int userId) {
        ArrayMap<Boolean, ArraySet<String>> allowedByType = (ArrayMap) this.mApproved.getOrDefault(Integer.valueOf(userId), new ArrayMap());
        for (int i = 0; i < allowedByType.size(); i++) {
            if (allowedByType.valueAt(i).contains(pkgOrComponent)) {
                return true;
            }
        }
        return false;
    }

    public void onPackagesChanged(boolean removingPackage, String[] pkgList, int[] uidList) {
        if (this.DEBUG) {
            String str = this.TAG;
            StringBuilder sb = new StringBuilder();
            sb.append("onPackagesChanged removingPackage=");
            sb.append(removingPackage);
            sb.append(" pkgList=");
            sb.append(pkgList == null ? null : Arrays.asList(pkgList));
            sb.append(" mEnabledServicesPackageNames=");
            sb.append(this.mEnabledServicesPackageNames);
            Slog.d(str, sb.toString());
        }
        if (pkgList != null && pkgList.length > 0) {
            boolean i = false;
            if (removingPackage) {
                int size = Math.min(pkgList.length, uidList.length);
                boolean anyServicesInvolved = false;
                for (int i2 = 0; i2 < size; i2++) {
                    anyServicesInvolved = removeUninstalledItemsFromApprovedLists(UserHandle.getUserId(uidList[i2]), pkgList[i2]);
                }
                i = anyServicesInvolved;
            }
            boolean anyServicesInvolved2 = i;
            for (String pkgName : pkgList) {
                if (this.mEnabledServicesPackageNames.contains(pkgName)) {
                    anyServicesInvolved2 = true;
                }
            }
            if (anyServicesInvolved2) {
                rebindServices(false);
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

    public void onUserRemoved(int user) {
        String str = this.TAG;
        Slog.i(str, "Removing approved services for removed user " + user);
        this.mApproved.remove(Integer.valueOf(user));
        rebindServices(true);
    }

    public void onUserSwitched(int user) {
        if (this.DEBUG) {
            String str = this.TAG;
            Slog.d(str, "onUserSwitched u=" + user);
        }
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
            String str = this.TAG;
            Slog.d(str, "onUserUnlocked u=" + user);
        }
        rebindServices(false);
    }

    private ManagedServiceInfo getServiceFromTokenLocked(IInterface service) {
        if (service == null) {
            return null;
        }
        IBinder token = service.asBinder();
        int N = this.mServices.size();
        for (int i = 0; i < N; i++) {
            ManagedServiceInfo info = this.mServices.get(i);
            if (info.service.asBinder() == token) {
                return info;
            }
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public boolean isServiceTokenValidLocked(IInterface service) {
        if (service == null || getServiceFromTokenLocked(service) == null) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public ManagedServiceInfo checkServiceTokenLocked(IInterface service) {
        checkNotNull(service);
        ManagedServiceInfo info = getServiceFromTokenLocked(service);
        if (info != null) {
            return info;
        }
        throw new SecurityException("Disallowed call from unknown " + getCaption() + ": " + service + " " + service.getClass());
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

    /* access modifiers changed from: protected */
    public void registerGuestService(ManagedServiceInfo guest) {
        checkNotNull(guest.service);
        if (!checkType(guest.service)) {
            throw new IllegalArgumentException();
        } else if (registerServiceImpl(guest) != null) {
            onServiceAdded(guest);
        }
    }

    /* access modifiers changed from: protected */
    public void setComponentState(ComponentName component, boolean enabled) {
        if ((!this.mSnoozingForCurrentProfiles.contains(component)) != enabled) {
            if (enabled) {
                this.mSnoozingForCurrentProfiles.remove(component);
            } else {
                this.mSnoozingForCurrentProfiles.add(component);
            }
            String str = this.TAG;
            StringBuilder sb = new StringBuilder();
            sb.append(enabled ? "Enabling " : "Disabling ");
            sb.append("component ");
            sb.append(component.flattenToShortString());
            Slog.d(str, sb.toString());
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

    private ArraySet<ComponentName> loadComponentNamesFromValues(ArraySet<String> approved, int userId) {
        if (approved == null || approved.size() == 0) {
            return new ArraySet<>();
        }
        ArraySet<ComponentName> result = new ArraySet<>(approved.size());
        for (int i = 0; i < approved.size(); i++) {
            String packageOrComponent = approved.valueAt(i);
            if (!TextUtils.isEmpty(packageOrComponent)) {
                ComponentName component = ComponentName.unflattenFromString(packageOrComponent);
                if (component != null) {
                    result.add(component);
                } else {
                    result.addAll(queryPackageForServices(packageOrComponent, userId));
                }
            }
        }
        return result;
    }

    /* access modifiers changed from: protected */
    public Set<ComponentName> queryPackageForServices(String packageName, int userId) {
        return queryPackageForServices(packageName, 0, userId);
    }

    /* access modifiers changed from: protected */
    public Set<ComponentName> queryPackageForServices(String packageName, int extraFlags, int userId) {
        Set<ComponentName> installed = new ArraySet<>();
        PackageManager pm = this.mContext.getPackageManager();
        Intent queryIntent = new Intent(this.mConfig.serviceInterface);
        if (!TextUtils.isEmpty(packageName)) {
            queryIntent.setPackage(packageName);
        }
        List<ResolveInfo> installedServices = pm.queryIntentServicesAsUser(queryIntent, 132 | extraFlags, userId);
        if (this.DEBUG) {
            String str = this.TAG;
            Slog.v(str, this.mConfig.serviceInterface + " services: " + installedServices);
        }
        if (installedServices != null) {
            int count = installedServices.size();
            for (int i = 0; i < count; i++) {
                ServiceInfo info = installedServices.get(i).serviceInfo;
                ComponentName component = new ComponentName(info.packageName, info.name);
                if (!this.mConfig.bindPermission.equals(info.permission)) {
                    String str2 = this.TAG;
                    Slog.w(str2, "Skipping " + getCaption() + " service " + info.packageName + SliceClientPermissions.SliceAuthority.DELIMITER + info.name + ": it does not require the permission " + this.mConfig.bindPermission);
                } else {
                    installed.add(component);
                }
            }
        }
        return installed;
    }

    private void trimApprovedListsAccordingToInstalledServices() {
        int N = this.mApproved.size();
        for (int i = 0; i < N; i++) {
            int userId = this.mApproved.keyAt(i).intValue();
            ArrayMap<Boolean, ArraySet<String>> approvedByType = this.mApproved.valueAt(i);
            int M = approvedByType.size();
            for (int j = 0; j < M; j++) {
                ArraySet<String> approved = approvedByType.valueAt(j);
                for (int k = approved.size() - 1; k >= 0; k--) {
                    if (!isValidEntry(approved.valueAt(k), userId)) {
                        approved.removeAt(k);
                        Slog.v(this.TAG, "Removing " + approvedPackageOrComponent + " from approved list; no matching services found");
                    } else if (this.DEBUG) {
                        Slog.v(this.TAG, "Keeping " + approvedPackageOrComponent + " on approved list; matching services found");
                    }
                }
            }
        }
    }

    private boolean removeUninstalledItemsFromApprovedLists(int uninstalledUserId, String pkg) {
        ArrayMap<Boolean, ArraySet<String>> approvedByType = this.mApproved.get(Integer.valueOf(uninstalledUserId));
        if (approvedByType != null) {
            int M = approvedByType.size();
            for (int j = 0; j < M; j++) {
                ArraySet<String> approved = approvedByType.valueAt(j);
                for (int k = approved.size() - 1; k >= 0; k--) {
                    if (TextUtils.equals(pkg, getPackageName(approved.valueAt(k)))) {
                        approved.removeAt(k);
                        if (this.DEBUG) {
                            Slog.v(this.TAG, "Removing " + packageOrComponent + " from approved list; uninstalled");
                        }
                    }
                }
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public String getPackageName(String packageOrComponent) {
        ComponentName component = ComponentName.unflattenFromString(packageOrComponent);
        if (component != null) {
            return component.getPackageName();
        }
        return packageOrComponent;
    }

    /* access modifiers changed from: protected */
    public boolean isValidEntry(String packageOrComponent, int userId) {
        return hasMatchingServices(packageOrComponent, userId);
    }

    private boolean hasMatchingServices(String packageOrComponent, int userId) {
        boolean z = false;
        if (TextUtils.isEmpty(packageOrComponent)) {
            return false;
        }
        if (queryPackageForServices(getPackageName(packageOrComponent), userId).size() > 0) {
            z = true;
        }
        return z;
    }

    /* access modifiers changed from: protected */
    public void rebindServices(boolean forceRebind) {
        if (this.DEBUG) {
            Slog.d(this.TAG, "rebindServices");
        }
        if (this.mConfig.serviceInterface.equals("android.service.notification.NotificationAssistantService")) {
            Slog.w(this.TAG, "rebindServices() ignore for NotificationAssistantService");
            return;
        }
        int[] userIds = this.mUserProfiles.getCurrentProfileIds();
        int nUserIds = userIds.length;
        SparseArray<ArraySet<ComponentName>> componentsByUser = new SparseArray<>();
        for (int i = 0; i < nUserIds; i++) {
            int userId = userIds[i];
            ArrayMap<Boolean, ArraySet<String>> approvedLists = this.mApproved.get(Integer.valueOf(userIds[i]));
            if (approvedLists != null) {
                int N = approvedLists.size();
                for (int j = 0; j < N; j++) {
                    ArraySet<ComponentName> approvedByUser = componentsByUser.get(userId);
                    if (approvedByUser == null) {
                        approvedByUser = new ArraySet<>();
                        componentsByUser.put(userId, approvedByUser);
                    }
                    approvedByUser.addAll(loadComponentNamesFromValues(approvedLists.valueAt(j), userId));
                }
            }
        }
        ArrayList<ManagedServiceInfo> removableBoundServices = new ArrayList<>();
        SparseArray<Set<ComponentName>> toAdd = new SparseArray<>();
        synchronized (this.mMutex) {
            Iterator<ManagedServiceInfo> it = this.mServices.iterator();
            while (it.hasNext()) {
                ManagedServiceInfo service = it.next();
                if (!service.isSystem && !service.isGuest(this)) {
                    removableBoundServices.add(service);
                }
            }
            this.mEnabledServicesForCurrentProfiles.clear();
            this.mEnabledServicesPackageNames.clear();
            for (int i2 = 0; i2 < nUserIds; i2++) {
                ArraySet<ComponentName> userComponents = componentsByUser.get(userIds[i2]);
                if (userComponents == null) {
                    toAdd.put(userIds[i2], new ArraySet());
                } else {
                    Set<ComponentName> add = new HashSet<>(userComponents);
                    add.removeAll(this.mSnoozingForCurrentProfiles);
                    toAdd.put(userIds[i2], add);
                    this.mEnabledServicesForCurrentProfiles.addAll(userComponents);
                    for (int j2 = 0; j2 < userComponents.size(); j2++) {
                        this.mEnabledServicesPackageNames.add(userComponents.valueAt(j2).getPackageName());
                    }
                }
            }
        }
        Iterator<ManagedServiceInfo> it2 = removableBoundServices.iterator();
        while (it2.hasNext()) {
            ManagedServiceInfo info = it2.next();
            ComponentName component = info.component;
            int oldUser = info.userid;
            Set<ComponentName> allowedComponents = toAdd.get(info.userid);
            if (allowedComponents != null) {
                if (!allowedComponents.contains(component) || forceRebind) {
                    Slog.v(this.TAG, "disabling " + getCaption() + " for user " + oldUser + ": " + component);
                    unregisterService(component, oldUser);
                } else {
                    allowedComponents.remove(component);
                }
            }
        }
        for (int i3 = 0; i3 < nUserIds; i3++) {
            for (ComponentName component2 : toAdd.get(userIds[i3])) {
                try {
                    ServiceInfo info2 = this.mPm.getServiceInfo(component2, 786432, userIds[i3]);
                    if (info2 == null) {
                        Slog.w(this.TAG, "Not binding " + getCaption() + " service " + component2 + ": service not found");
                    } else if (!this.mConfig.bindPermission.equals(info2.permission)) {
                        Slog.w(this.TAG, "Not binding " + getCaption() + " service " + component2 + ": it does not require the permission " + this.mConfig.bindPermission);
                    } else {
                        Slog.v(this.TAG, "enabling " + getCaption() + " for " + userIds[i3] + ": " + component2);
                        registerService(component2, userIds[i3]);
                    }
                } catch (RemoteException e) {
                    e.rethrowFromSystemServer();
                }
            }
        }
        this.mLastSeenProfileIds = userIds;
    }

    /* access modifiers changed from: private */
    public void registerService(ComponentName name, int userid) {
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
        String servicesBindingTag = name.toString() + SliceClientPermissions.SliceAuthority.DELIMITER + userid;
        if (!this.mServicesBinding.contains(servicesBindingTag)) {
            this.mServicesBinding.add(servicesBindingTag);
        }
        for (int i = this.mServices.size() - 1; i >= 0; i--) {
            ManagedServiceInfo info = this.mServices.get(i);
            if (name.equals(info.component) && info.userid == userid) {
                Slog.v(this.TAG, "    disconnecting old " + getCaption() + ": " + info.service);
                removeServiceLocked(i);
                if (info.connection != null) {
                    try {
                        this.mContext.unbindService(info.connection);
                    } catch (IllegalArgumentException e) {
                        Slog.e(this.TAG, "failed to unbind " + name, e);
                    }
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
        } catch (PackageManager.NameNotFoundException e2) {
        }
        ApplicationInfo appInfo2 = appInfo;
        final int targetSdkVersion = appInfo2 != null ? appInfo2.targetSdkVersion : 1;
        try {
            Slog.v(this.TAG, "binding: " + intent);
            final String str = servicesBindingTag;
            final int i2 = userid;
            final boolean z = isSystem;
            AnonymousClass2 serviceConnection = new ServiceConnection() {
                IInterface mService;

                /* JADX WARNING: Code restructure failed: missing block: B:12:0x0061, code lost:
                    if (r0 == false) goto L_0x0068;
                 */
                /* JADX WARNING: Code restructure failed: missing block: B:13:0x0063, code lost:
                    r11.this$0.onServiceAdded(r1);
                 */
                /* JADX WARNING: Code restructure failed: missing block: B:14:0x0068, code lost:
                    return;
                 */
                public void onServiceConnected(ComponentName name, IBinder binder) {
                    boolean added = false;
                    ManagedServiceInfo info = null;
                    synchronized (ManagedServices.this.mMutex) {
                        ManagedServices.this.mServicesRebinding.remove(str);
                        if (ManagedServices.this.mServicesBinding.contains(str)) {
                            Slog.d(ManagedServices.this.TAG, "onServiceConnected, just remove the servicesBindingTag and add service.");
                            ManagedServices.this.mServicesBinding.remove(str);
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
                        }
                    }
                }

                public void onServiceDisconnected(ComponentName name) {
                    ManagedServices.this.mServicesBinding.remove(str);
                    String str = ManagedServices.this.TAG;
                    Slog.v(str, ManagedServices.this.getCaption() + " connection lost: " + name);
                }

                public void onBindingDied(final ComponentName name) {
                    String str = ManagedServices.this.TAG;
                    Slog.w(str, ManagedServices.this.getCaption() + " binding died: " + name);
                    synchronized (ManagedServices.this.mMutex) {
                        ManagedServices.this.mServicesBinding.remove(str);
                        try {
                            ManagedServices.this.mContext.unbindService(this);
                        } catch (IllegalArgumentException e) {
                            String str2 = ManagedServices.this.TAG;
                            Slog.e(str2, "failed to unbind " + name, e);
                        }
                        if (!ManagedServices.this.mServicesRebinding.contains(str)) {
                            ManagedServices.this.mServicesRebinding.add(str);
                            ManagedServices.this.mHandler.postDelayed(new Runnable() {
                                public void run() {
                                    ManagedServices.this.registerService(name, i2);
                                }
                            }, JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY);
                        } else {
                            String str3 = ManagedServices.this.TAG;
                            Slog.v(str3, ManagedServices.this.getCaption() + " not rebinding as a previous rebind attempt was made: " + name);
                        }
                    }
                }
            };
            if (!this.mContext.bindServiceAsUser(intent, serviceConnection, 83886081, new UserHandle(userid))) {
                this.mServicesBinding.remove(servicesBindingTag);
                Slog.w(this.TAG, "Unable to bind " + getCaption() + " service: " + intent);
            }
        } catch (SecurityException ex) {
            this.mServicesBinding.remove(servicesBindingTag);
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
            ManagedServiceInfo info = this.mServices.get(i);
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

    /* access modifiers changed from: private */
    public ManagedServiceInfo removeServiceImpl(IInterface service, int userid) {
        if (this.DEBUG) {
            Slog.d(this.TAG, "removeServiceImpl service=" + service + " u=" + userid);
        }
        ManagedServiceInfo serviceInfo = null;
        synchronized (this.mMutex) {
            for (int i = this.mServices.size() - 1; i >= 0; i--) {
                ManagedServiceInfo info = this.mServices.get(i);
                if (info.service.asBinder() == service.asBinder() && info.userid == userid) {
                    Slog.d(this.TAG, "Removing active service " + info.component);
                    serviceInfo = removeServiceLocked(i);
                }
            }
        }
        return serviceInfo;
    }

    private ManagedServiceInfo removeServiceLocked(int i) {
        ManagedServiceInfo info = this.mServices.remove(i);
        onServiceRemovedLocked(info);
        if (info != null) {
            try {
                info.service.asBinder().unlinkToDeath(info, 0);
            } catch (Exception e) {
                if (this.DEBUG) {
                    String str = this.TAG;
                    Slog.d(str, "Death link does not exist , error msg: " + e.getMessage());
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
            } catch (Throwable th) {
                throw th;
            }
        }
        return info;
    }

    private void unregisterServiceImpl(IInterface service, int userid) {
        ManagedServiceInfo info = removeServiceImpl(service, userid);
        if (info != null && info.connection != null && !info.isGuest(this)) {
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
