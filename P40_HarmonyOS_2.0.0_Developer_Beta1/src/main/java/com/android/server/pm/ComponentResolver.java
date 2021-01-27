package com.android.server.pm;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.AuxiliaryResolveInfo;
import android.content.pm.InstantAppResolveInfo;
import android.content.pm.PackageManagerInternal;
import android.content.pm.PackageParser;
import android.content.pm.PackageUserState;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.BadParcelableException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.DebugUtils;
import android.util.Log;
import android.util.Pair;
import android.util.Slog;
import com.android.internal.annotations.GuardedBy;
import com.android.server.HwServiceFactory;
import com.android.server.IntentResolver;
import com.android.server.am.HwBroadcastRadarUtil;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ComponentResolver {
    private static final boolean DEBUG = false;
    private static final boolean DEBUG_FILTERS = false;
    private static final boolean DEBUG_SHOW_INFO = false;
    private static final Set<String> PROTECTED_ACTIONS = new ArraySet();
    static final Comparator<ResolveInfo> RESOLVE_PRIORITY_SORTER = $$Lambda$ComponentResolver$PuHbZd5KEOMGjkH8xDOhOwfLtC0.INSTANCE;
    private static final String TAG = "PackageManager";
    private static PackageManagerInternal sPackageManagerInternal;
    private static UserManagerService sUserManager;
    @GuardedBy({"mLock"})
    private final ActivityIntentResolver mActivities = new ActivityIntentResolver();
    private boolean mDeferProtectedFilters = true;
    private final Object mLock;
    private List<PackageParser.ActivityIntentInfo> mProtectedFilters;
    @GuardedBy({"mLock"})
    private final ProviderIntentResolver mProviders = new ProviderIntentResolver();
    @GuardedBy({"mLock"})
    private final ArrayMap<String, PackageParser.Provider> mProvidersByAuthority = new ArrayMap<>();
    @GuardedBy({"mLock"})
    private final ActivityIntentResolver mReceivers = new ActivityIntentResolver();
    @GuardedBy({"mLock"})
    private final ServiceIntentResolver mServices = new ServiceIntentResolver();

    static {
        PROTECTED_ACTIONS.add("android.intent.action.SEND");
        PROTECTED_ACTIONS.add("android.intent.action.SENDTO");
        PROTECTED_ACTIONS.add("android.intent.action.SEND_MULTIPLE");
        PROTECTED_ACTIONS.add("android.intent.action.VIEW");
    }

    static /* synthetic */ int lambda$static$0(ResolveInfo r1, ResolveInfo r2) {
        int v1 = r1.priority;
        int v2 = r2.priority;
        if (v1 != v2) {
            return v1 > v2 ? -1 : 1;
        }
        int v12 = r1.preferredOrder;
        int v22 = r2.preferredOrder;
        if (v12 != v22) {
            return v12 > v22 ? -1 : 1;
        }
        if (r1.isDefault != r2.isDefault) {
            return r1.isDefault ? -1 : 1;
        }
        int v13 = r1.match;
        int v23 = r2.match;
        if (v13 != v23) {
            return v13 > v23 ? -1 : 1;
        }
        if (r1.system != r2.system) {
            return r1.system ? -1 : 1;
        }
        if (r1.activityInfo != null) {
            return r1.activityInfo.packageName.compareTo(r2.activityInfo.packageName);
        }
        if (r1.serviceInfo != null) {
            return r1.serviceInfo.packageName.compareTo(r2.serviceInfo.packageName);
        }
        if (r1.providerInfo != null) {
            return r1.providerInfo.packageName.compareTo(r2.providerInfo.packageName);
        }
        return 0;
    }

    ComponentResolver(UserManagerService userManager, PackageManagerInternal packageManagerInternal, Object lock) {
        sPackageManagerInternal = packageManagerInternal;
        sUserManager = userManager;
        this.mLock = lock;
    }

    /* access modifiers changed from: package-private */
    public PackageParser.Activity getActivity(ComponentName component) {
        PackageParser.Activity activity;
        synchronized (this.mLock) {
            activity = (PackageParser.Activity) this.mActivities.mActivities.get(component);
        }
        return activity;
    }

    /* access modifiers changed from: package-private */
    public PackageParser.Provider getProvider(ComponentName component) {
        PackageParser.Provider provider;
        synchronized (this.mLock) {
            provider = (PackageParser.Provider) this.mProviders.mProviders.get(component);
        }
        return provider;
    }

    /* access modifiers changed from: package-private */
    public PackageParser.Activity getReceiver(ComponentName component) {
        PackageParser.Activity activity;
        synchronized (this.mLock) {
            activity = (PackageParser.Activity) this.mReceivers.mActivities.get(component);
        }
        return activity;
    }

    /* access modifiers changed from: package-private */
    public PackageParser.Service getService(ComponentName component) {
        PackageParser.Service service;
        synchronized (this.mLock) {
            service = (PackageParser.Service) this.mServices.mServices.get(component);
        }
        return service;
    }

    /* access modifiers changed from: package-private */
    public List<ResolveInfo> queryActivities(Intent intent, String resolvedType, int flags, int userId) {
        List<ResolveInfo> queryIntent;
        synchronized (this.mLock) {
            queryIntent = this.mActivities.queryIntent(intent, resolvedType, flags, userId);
        }
        return queryIntent;
    }

    /* access modifiers changed from: package-private */
    public List<ResolveInfo> queryActivities(Intent intent, String resolvedType, int flags, List<PackageParser.Activity> activities, int userId) {
        List<ResolveInfo> queryIntentForPackage;
        synchronized (this.mLock) {
            queryIntentForPackage = this.mActivities.queryIntentForPackage(intent, resolvedType, flags, activities, userId);
        }
        return queryIntentForPackage;
    }

    /* access modifiers changed from: package-private */
    public List<ResolveInfo> queryProviders(Intent intent, String resolvedType, int flags, int userId) {
        List<ResolveInfo> queryIntent;
        synchronized (this.mLock) {
            queryIntent = this.mProviders.queryIntent(intent, resolvedType, flags, userId);
        }
        return queryIntent;
    }

    /* access modifiers changed from: package-private */
    public List<ResolveInfo> queryProviders(Intent intent, String resolvedType, int flags, List<PackageParser.Provider> providers, int userId) {
        List<ResolveInfo> queryIntentForPackage;
        synchronized (this.mLock) {
            queryIntentForPackage = this.mProviders.queryIntentForPackage(intent, resolvedType, flags, providers, userId);
        }
        return queryIntentForPackage;
    }

    /* access modifiers changed from: package-private */
    public List<ProviderInfo> queryProviders(String processName, String metaDataKey, int uid, int flags, int userId) {
        if (!sUserManager.exists(userId)) {
            return null;
        }
        List<ProviderInfo> providerList = null;
        synchronized (this.mLock) {
            for (int i = this.mProviders.mProviders.size() - 1; i >= 0; i--) {
                PackageParser.Provider p = (PackageParser.Provider) this.mProviders.mProviders.valueAt(i);
                PackageSetting ps = (PackageSetting) p.owner.mExtras;
                if (ps != null) {
                    if (p.info.authority != null) {
                        if (processName != null) {
                            if (p.info.processName.equals(processName)) {
                                if (!UserHandle.isSameApp(p.info.applicationInfo.uid, uid)) {
                                }
                            }
                        }
                        if (metaDataKey != null) {
                            if (p.metaData != null) {
                                if (!p.metaData.containsKey(metaDataKey)) {
                                }
                            }
                        }
                        ProviderInfo info = PackageParser.generateProviderInfo(p, flags, ps.readUserState(userId), userId);
                        if (info != null) {
                            if (providerList == null) {
                                providerList = new ArrayList<>(i + 1);
                            }
                            if (ps.pkg == null || !sPackageManagerInternal.isNeedForbidAppAct(ps.pkg.packageName)) {
                                providerList.add(info);
                            } else {
                                Slog.i(TAG, "queryProviders forbid query contentProvider");
                            }
                        }
                    }
                }
            }
        }
        return providerList;
    }

    /* access modifiers changed from: package-private */
    public ProviderInfo queryProvider(String authority, int flags, int userId) {
        synchronized (this.mLock) {
            PackageParser.Provider p = this.mProvidersByAuthority.get(authority);
            if (p == null) {
                return null;
            }
            PackageSetting ps = (PackageSetting) p.owner.mExtras;
            if (ps == null) {
                return null;
            }
            if (ps.pkg == null || !sPackageManagerInternal.isNeedForbidAppAct(ps.pkg.packageName)) {
                return PackageParser.generateProviderInfo(p, flags, ps.readUserState(userId), userId);
            }
            Slog.i(TAG, "queryProvider forbid contentProvider");
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public void querySyncProviders(List<String> outNames, List<ProviderInfo> outInfo, boolean safeMode, int userId) {
        synchronized (this.mLock) {
            for (int i = this.mProvidersByAuthority.size() - 1; i >= 0; i--) {
                PackageParser.Provider p = this.mProvidersByAuthority.valueAt(i);
                PackageSetting ps = (PackageSetting) p.owner.mExtras;
                if (ps != null) {
                    if (p.syncable) {
                        if (!safeMode || (p.info.applicationInfo.flags & 1) != 0) {
                            ProviderInfo info = PackageParser.generateProviderInfo(p, 0, ps.readUserState(userId), userId);
                            if (info != null) {
                                outNames.add(this.mProvidersByAuthority.keyAt(i));
                                outInfo.add(info);
                            }
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public List<ResolveInfo> queryReceivers(Intent intent, String resolvedType, int flags, int userId) {
        List<ResolveInfo> queryIntent;
        synchronized (this.mLock) {
            queryIntent = this.mReceivers.queryIntent(intent, resolvedType, flags, userId);
        }
        return queryIntent;
    }

    /* access modifiers changed from: package-private */
    public List<ResolveInfo> queryReceivers(Intent intent, String resolvedType, int flags, List<PackageParser.Activity> receivers, int userId) {
        List<ResolveInfo> queryIntentForPackage;
        synchronized (this.mLock) {
            queryIntentForPackage = this.mReceivers.queryIntentForPackage(intent, resolvedType, flags, receivers, userId);
        }
        return queryIntentForPackage;
    }

    /* access modifiers changed from: package-private */
    public List<ResolveInfo> queryServices(Intent intent, String resolvedType, int flags, int userId) {
        List<ResolveInfo> queryIntent;
        synchronized (this.mLock) {
            queryIntent = this.mServices.queryIntent(intent, resolvedType, flags, userId);
        }
        return queryIntent;
    }

    /* access modifiers changed from: package-private */
    public List<ResolveInfo> queryServices(Intent intent, String resolvedType, int flags, List<PackageParser.Service> services, int userId) {
        List<ResolveInfo> queryIntentForPackage;
        synchronized (this.mLock) {
            queryIntentForPackage = this.mServices.queryIntentForPackage(intent, resolvedType, flags, services, userId);
        }
        return queryIntentForPackage;
    }

    /* access modifiers changed from: package-private */
    public boolean isActivityDefined(ComponentName component) {
        boolean z;
        synchronized (this.mLock) {
            z = this.mActivities.mActivities.get(component) != null;
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public void assertProvidersNotDefined(PackageParser.Package pkg) throws PackageManagerException {
        synchronized (this.mLock) {
            assertProvidersNotDefinedLocked(pkg);
        }
    }

    /* access modifiers changed from: package-private */
    public void addAllComponents(PackageParser.Package pkg, boolean chatty) {
        ArrayList<PackageParser.ActivityIntentInfo> newIntents = new ArrayList<>();
        synchronized (this.mLock) {
            addActivitiesLocked(pkg, newIntents, chatty);
            addReceiversLocked(pkg, chatty);
            addProvidersLocked(pkg, chatty);
            addServicesLocked(pkg, chatty);
        }
        String setupWizardPackage = sPackageManagerInternal.getKnownPackageName(1, 0);
        for (int i = newIntents.size() - 1; i >= 0; i--) {
            PackageParser.ActivityIntentInfo intentInfo = newIntents.get(i);
            PackageParser.Package disabledPkg = sPackageManagerInternal.getDisabledSystemPackage(intentInfo.activity.info.packageName);
            adjustPriority(disabledPkg != null ? disabledPkg.activities : null, intentInfo, setupWizardPackage);
        }
    }

    /* access modifiers changed from: package-private */
    public void removeAllComponents(PackageParser.Package pkg, boolean chatty) {
        synchronized (this.mLock) {
            removeAllComponentsLocked(pkg, chatty);
        }
    }

    /* access modifiers changed from: package-private */
    public void fixProtectedFilterPriorities() {
        if (this.mDeferProtectedFilters) {
            this.mDeferProtectedFilters = false;
            List<PackageParser.ActivityIntentInfo> list = this.mProtectedFilters;
            if (!(list == null || list.size() == 0)) {
                List<PackageParser.ActivityIntentInfo> protectedFilters = this.mProtectedFilters;
                this.mProtectedFilters = null;
                String setupWizardPackage = sPackageManagerInternal.getKnownPackageName(1, 0);
                for (int i = protectedFilters.size() - 1; i >= 0; i--) {
                    PackageParser.ActivityIntentInfo filter = protectedFilters.get(i);
                    if (!filter.activity.info.packageName.equals(setupWizardPackage)) {
                        filter.setPriority(0);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void dumpActivityResolvers(PrintWriter pw, DumpState dumpState, String packageName) {
        String str;
        ActivityIntentResolver activityIntentResolver = this.mActivities;
        if (dumpState.getTitlePrinted()) {
            str = "\nActivity Resolver Table:";
        } else {
            str = "Activity Resolver Table:";
        }
        if (activityIntentResolver.dump(pw, str, "  ", packageName, dumpState.isOptionEnabled(1), true)) {
            dumpState.setTitlePrinted(true);
        }
    }

    /* access modifiers changed from: package-private */
    public void dumpProviderResolvers(PrintWriter pw, DumpState dumpState, String packageName) {
        String str;
        ProviderIntentResolver providerIntentResolver = this.mProviders;
        if (dumpState.getTitlePrinted()) {
            str = "\nProvider Resolver Table:";
        } else {
            str = "Provider Resolver Table:";
        }
        if (providerIntentResolver.dump(pw, str, "  ", packageName, dumpState.isOptionEnabled(1), true)) {
            dumpState.setTitlePrinted(true);
        }
    }

    /* access modifiers changed from: package-private */
    public void dumpReceiverResolvers(PrintWriter pw, DumpState dumpState, String packageName) {
        String str;
        ActivityIntentResolver activityIntentResolver = this.mReceivers;
        if (dumpState.getTitlePrinted()) {
            str = "\nReceiver Resolver Table:";
        } else {
            str = "Receiver Resolver Table:";
        }
        if (activityIntentResolver.dump(pw, str, "  ", packageName, dumpState.isOptionEnabled(1), true)) {
            dumpState.setTitlePrinted(true);
        }
    }

    /* access modifiers changed from: package-private */
    public void dumpServiceResolvers(PrintWriter pw, DumpState dumpState, String packageName) {
        String str;
        ServiceIntentResolver serviceIntentResolver = this.mServices;
        if (dumpState.getTitlePrinted()) {
            str = "\nService Resolver Table:";
        } else {
            str = "Service Resolver Table:";
        }
        if (serviceIntentResolver.dump(pw, str, "  ", packageName, dumpState.isOptionEnabled(1), true)) {
            dumpState.setTitlePrinted(true);
        }
    }

    /* access modifiers changed from: package-private */
    public void dumpContentProviders(PrintWriter pw, DumpState dumpState, String packageName) {
        boolean printedSomething = false;
        for (PackageParser.Provider p : this.mProviders.mProviders.values()) {
            if (packageName == null || packageName.equals(p.info.packageName)) {
                if (!printedSomething) {
                    if (dumpState.onTitlePrinted()) {
                        pw.println();
                    }
                    pw.println("Registered ContentProviders:");
                    printedSomething = true;
                }
                pw.print("  ");
                p.printComponentShortName(pw);
                pw.println(":");
                pw.print("    ");
                pw.println(p.toString());
            }
        }
        boolean printedSomething2 = false;
        for (Map.Entry<String, PackageParser.Provider> entry : this.mProvidersByAuthority.entrySet()) {
            PackageParser.Provider p2 = entry.getValue();
            if (packageName == null || packageName.equals(p2.info.packageName)) {
                if (!printedSomething2) {
                    if (dumpState.onTitlePrinted()) {
                        pw.println();
                    }
                    pw.println("ContentProvider Authorities:");
                    printedSomething2 = true;
                }
                pw.print("  [");
                pw.print(entry.getKey());
                pw.println("]:");
                pw.print("    ");
                pw.println(p2.toString());
                if (!(p2.info == null || p2.info.applicationInfo == null)) {
                    String appInfo = p2.info.applicationInfo.toString();
                    pw.print("      applicationInfo=");
                    pw.println(appInfo);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void dumpServicePermissions(PrintWriter pw, DumpState dumpState, String packageName) {
        if (dumpState.onTitlePrinted()) {
            pw.println();
        }
        pw.println("Service permissions:");
        Iterator<PackageParser.ServiceIntentInfo> filterIterator = this.mServices.filterIterator();
        while (filterIterator.hasNext()) {
            ServiceInfo serviceInfo = filterIterator.next().service.info;
            String permission = serviceInfo.permission;
            if (permission != null) {
                pw.print("    ");
                pw.print(serviceInfo.getComponentName().flattenToShortString());
                pw.print(": ");
                pw.println(permission);
            }
        }
    }

    @GuardedBy({"mLock"})
    private void addActivitiesLocked(PackageParser.Package pkg, List<PackageParser.ActivityIntentInfo> newIntents, boolean chatty) {
        int activitiesSize = pkg.activities.size();
        StringBuilder r = null;
        for (int i = 0; i < activitiesSize; i++) {
            PackageParser.Activity a = (PackageParser.Activity) pkg.activities.get(i);
            a.info.processName = PackageManagerService.fixProcessName(pkg.applicationInfo.processName, a.info.processName);
            this.mActivities.addActivity(a, "activity", newIntents);
            if (PackageManagerService.DEBUG_PACKAGE_SCANNING && chatty) {
                if (r == null) {
                    r = new StringBuilder(256);
                } else {
                    r.append(' ');
                }
                r.append(a.info.name);
            }
        }
        if (PackageManagerService.DEBUG_PACKAGE_SCANNING && chatty) {
            StringBuilder sb = new StringBuilder();
            sb.append("  Activities: ");
            sb.append(r == null ? "<NONE>" : r);
            Log.d(TAG, sb.toString());
        }
    }

    @GuardedBy({"mLock"})
    private void addProvidersLocked(PackageParser.Package pkg, boolean chatty) {
        int providersSize = pkg.providers.size();
        StringBuilder r = null;
        for (int i = 0; i < providersSize; i++) {
            PackageParser.Provider p = (PackageParser.Provider) pkg.providers.get(i);
            p.info.processName = PackageManagerService.fixProcessName(pkg.applicationInfo.processName, p.info.processName);
            this.mProviders.addProvider(p);
            p.syncable = p.info.isSyncable;
            if (p.info.authority != null) {
                String[] names = p.info.authority.split(";");
                p.info.authority = null;
                for (int j = 0; j < names.length; j++) {
                    if (j == 1 && p.syncable) {
                        p = new PackageParser.Provider(p);
                        p.syncable = false;
                    }
                    if (!this.mProvidersByAuthority.containsKey(names[j])) {
                        this.mProvidersByAuthority.put(names[j], p);
                        if (p.info.authority == null) {
                            p.info.authority = names[j];
                        } else {
                            ProviderInfo providerInfo = p.info;
                            providerInfo.authority = p.info.authority + ";" + names[j];
                        }
                        if (PackageManagerService.DEBUG_PACKAGE_SCANNING && chatty) {
                            Log.d(TAG, "Registered content provider: " + names[j] + ", className = " + p.info.name + ", isSyncable = " + p.info.isSyncable);
                        }
                    } else {
                        PackageParser.Provider other = this.mProvidersByAuthority.get(names[j]);
                        ComponentName component = (other == null || other.getComponentName() == null) ? null : other.getComponentName();
                        String packageName = component != null ? component.getPackageName() : "?";
                        Slog.w(TAG, "Skipping provider name " + names[j] + " (in package " + pkg.applicationInfo.packageName + "): name already used by " + packageName);
                    }
                }
            }
            if (PackageManagerService.DEBUG_PACKAGE_SCANNING && chatty) {
                if (r == null) {
                    r = new StringBuilder(256);
                } else {
                    r.append(' ');
                }
                r.append(p.info.name);
            }
        }
        if (PackageManagerService.DEBUG_PACKAGE_SCANNING && chatty) {
            StringBuilder sb = new StringBuilder();
            sb.append("  Providers: ");
            sb.append(r == null ? "<NONE>" : r);
            Log.d(TAG, sb.toString());
        }
    }

    @GuardedBy({"mLock"})
    private void addReceiversLocked(PackageParser.Package pkg, boolean chatty) {
        int receiversSize = pkg.receivers.size();
        StringBuilder r = null;
        for (int i = 0; i < receiversSize; i++) {
            PackageParser.Activity a = (PackageParser.Activity) pkg.receivers.get(i);
            a.info.processName = PackageManagerService.fixProcessName(pkg.applicationInfo.processName, a.info.processName);
            this.mReceivers.addActivity(a, HwBroadcastRadarUtil.KEY_RECEIVER, null);
            if (PackageManagerService.DEBUG_PACKAGE_SCANNING && chatty) {
                if (r == null) {
                    r = new StringBuilder(256);
                } else {
                    r.append(' ');
                }
                r.append(a.info.name);
            }
        }
        if (PackageManagerService.DEBUG_PACKAGE_SCANNING && chatty) {
            StringBuilder sb = new StringBuilder();
            sb.append("  Receivers: ");
            sb.append(r == null ? "<NONE>" : r);
            Log.d(TAG, sb.toString());
        }
    }

    @GuardedBy({"mLock"})
    private void addServicesLocked(PackageParser.Package pkg, boolean chatty) {
        int servicesSize = pkg.services.size();
        StringBuilder r = null;
        for (int i = 0; i < servicesSize; i++) {
            PackageParser.Service s = (PackageParser.Service) pkg.services.get(i);
            s.info.processName = PackageManagerService.fixProcessName(pkg.applicationInfo.processName, s.info.processName);
            this.mServices.addService(s);
            if (PackageManagerService.DEBUG_PACKAGE_SCANNING && chatty) {
                if (r == null) {
                    r = new StringBuilder(256);
                } else {
                    r.append(' ');
                }
                r.append(s.info.name);
            }
        }
        if (PackageManagerService.DEBUG_PACKAGE_SCANNING && chatty) {
            StringBuilder sb = new StringBuilder();
            sb.append("  Services: ");
            sb.append(r == null ? "<NONE>" : r);
            Log.d(TAG, sb.toString());
        }
    }

    private static <T> void getIntentListSubset(List<PackageParser.ActivityIntentInfo> intentList, IterGenerator<T> generator, Iterator<T> searchIterator) {
        while (searchIterator.hasNext() && intentList.size() != 0) {
            T searchAction = searchIterator.next();
            Iterator<PackageParser.ActivityIntentInfo> intentIter = intentList.iterator();
            while (intentIter.hasNext()) {
                boolean selectionFound = false;
                Iterator<T> intentSelectionIter = generator.generate(intentIter.next());
                while (true) {
                    if (intentSelectionIter == null || !intentSelectionIter.hasNext()) {
                        break;
                    }
                    T intentSelection = intentSelectionIter.next();
                    if (intentSelection != null && intentSelection.equals(searchAction)) {
                        selectionFound = true;
                        break;
                    }
                }
                if (!selectionFound) {
                    intentIter.remove();
                }
            }
        }
    }

    private static boolean isProtectedAction(PackageParser.ActivityIntentInfo filter) {
        Iterator<String> actionsIter = filter.actionsIterator();
        while (actionsIter != null && actionsIter.hasNext()) {
            if (PROTECTED_ACTIONS.contains(actionsIter.next())) {
                return true;
            }
        }
        return false;
    }

    private static PackageParser.Activity findMatchingActivity(List<PackageParser.Activity> activityList, ActivityInfo activityInfo) {
        Iterator<PackageParser.Activity> it = activityList.iterator();
        while (it.hasNext()) {
            PackageParser.Activity sysActivity = it.next();
            if (sysActivity.info.name.equals(activityInfo.name) || sysActivity.info.name.equals(activityInfo.targetActivity)) {
                return sysActivity;
            }
            if (sysActivity.info.targetActivity != null && (sysActivity.info.targetActivity.equals(activityInfo.name) || sysActivity.info.targetActivity.equals(activityInfo.targetActivity))) {
                return sysActivity;
            }
        }
        return null;
    }

    private void adjustPriority(List<PackageParser.Activity> systemActivities, PackageParser.ActivityIntentInfo intent, String setupWizardPackage) {
        if (intent.getPriority() > 0) {
            ActivityInfo activityInfo = intent.activity.info;
            if (!((activityInfo.applicationInfo.privateFlags & 8) != 0)) {
                intent.setPriority(0);
            } else if (systemActivities != null) {
                PackageParser.Activity foundActivity = findMatchingActivity(systemActivities, activityInfo);
                if (foundActivity == null) {
                    intent.setPriority(0);
                    return;
                }
                List<PackageParser.ActivityIntentInfo> intentListCopy = new ArrayList<>(foundActivity.intents);
                this.mActivities.findFilters(intent);
                Iterator<String> actionsIterator = intent.actionsIterator();
                if (actionsIterator != null) {
                    getIntentListSubset(intentListCopy, new ActionIterGenerator(), actionsIterator);
                    if (intentListCopy.size() == 0) {
                        intent.setPriority(0);
                        return;
                    }
                }
                Iterator<String> categoriesIterator = intent.categoriesIterator();
                if (categoriesIterator != null) {
                    getIntentListSubset(intentListCopy, new CategoriesIterGenerator(), categoriesIterator);
                    if (intentListCopy.size() == 0) {
                        intent.setPriority(0);
                        return;
                    }
                }
                Iterator<String> schemesIterator = intent.schemesIterator();
                if (schemesIterator != null) {
                    getIntentListSubset(intentListCopy, new SchemesIterGenerator(), schemesIterator);
                    if (intentListCopy.size() == 0) {
                        intent.setPriority(0);
                        return;
                    }
                }
                Iterator<IntentFilter.AuthorityEntry> authoritiesIterator = intent.authoritiesIterator();
                if (authoritiesIterator != null) {
                    getIntentListSubset(intentListCopy, new AuthoritiesIterGenerator(), authoritiesIterator);
                    if (intentListCopy.size() == 0) {
                        intent.setPriority(0);
                        return;
                    }
                }
                int cappedPriority = 0;
                for (int i = intentListCopy.size() - 1; i >= 0; i--) {
                    cappedPriority = Math.max(cappedPriority, intentListCopy.get(i).getPriority());
                }
                if (intent.getPriority() > cappedPriority) {
                    intent.setPriority(cappedPriority);
                }
            } else if (isProtectedAction(intent)) {
                if (this.mDeferProtectedFilters) {
                    if (this.mProtectedFilters == null) {
                        this.mProtectedFilters = new ArrayList();
                    }
                    this.mProtectedFilters.add(intent);
                } else if (!intent.activity.info.packageName.equals(setupWizardPackage)) {
                    intent.setPriority(0);
                }
            }
        }
    }

    @GuardedBy({"mLock"})
    private void removeAllComponentsLocked(PackageParser.Package pkg, boolean chatty) {
        int componentSize = pkg.activities.size();
        StringBuilder r = null;
        for (int i = 0; i < componentSize; i++) {
            PackageParser.Activity a = (PackageParser.Activity) pkg.activities.get(i);
            this.mActivities.removeActivity(a, "activity");
            if (PackageManagerService.DEBUG_REMOVE && chatty) {
                if (r == null) {
                    r = new StringBuilder(256);
                } else {
                    r.append(' ');
                }
                r.append(a.info.name);
            }
        }
        CharSequence charSequence = "<NONE>";
        if (PackageManagerService.DEBUG_REMOVE && chatty) {
            StringBuilder sb = new StringBuilder();
            sb.append("  Activities: ");
            sb.append((Object) (r == null ? charSequence : r));
            Log.d(TAG, sb.toString());
        }
        int componentSize2 = pkg.providers.size();
        StringBuilder r2 = null;
        for (int i2 = 0; i2 < componentSize2; i2++) {
            PackageParser.Provider p = (PackageParser.Provider) pkg.providers.get(i2);
            this.mProviders.removeProvider(p);
            if (p.info.authority != null) {
                String[] names = p.info.authority.split(";");
                for (int j = 0; j < names.length; j++) {
                    if (this.mProvidersByAuthority.get(names[j]) == p) {
                        this.mProvidersByAuthority.remove(names[j]);
                        if (PackageManagerService.DEBUG_REMOVE && chatty) {
                            Log.d(TAG, "Unregistered content provider: " + names[j] + ", className = " + p.info.name + ", isSyncable = " + p.info.isSyncable);
                        }
                    }
                }
                if (PackageManagerService.DEBUG_REMOVE && chatty) {
                    if (r2 == null) {
                        r2 = new StringBuilder(256);
                    } else {
                        r2.append(' ');
                    }
                    r2.append(p.info.name);
                }
            }
        }
        if (PackageManagerService.DEBUG_REMOVE && chatty) {
            StringBuilder sb2 = new StringBuilder();
            sb2.append("  Providers: ");
            sb2.append((Object) (r2 == null ? charSequence : r2));
            Log.d(TAG, sb2.toString());
        }
        int componentSize3 = pkg.receivers.size();
        StringBuilder r3 = null;
        for (int i3 = 0; i3 < componentSize3; i3++) {
            PackageParser.Activity a2 = (PackageParser.Activity) pkg.receivers.get(i3);
            this.mReceivers.removeActivity(a2, HwBroadcastRadarUtil.KEY_RECEIVER);
            if (PackageManagerService.DEBUG_REMOVE && chatty) {
                if (r3 == null) {
                    r3 = new StringBuilder(256);
                } else {
                    r3.append(' ');
                }
                r3.append(a2.info.name);
            }
        }
        if (PackageManagerService.DEBUG_REMOVE && chatty) {
            StringBuilder sb3 = new StringBuilder();
            sb3.append("  Receivers: ");
            sb3.append((Object) (r3 == null ? charSequence : r3));
            Log.d(TAG, sb3.toString());
        }
        int componentSize4 = pkg.services.size();
        StringBuilder r4 = null;
        for (int i4 = 0; i4 < componentSize4; i4++) {
            PackageParser.Service s = (PackageParser.Service) pkg.services.get(i4);
            this.mServices.removeService(s);
            if (PackageManagerService.DEBUG_REMOVE && chatty) {
                if (r4 == null) {
                    r4 = new StringBuilder(256);
                } else {
                    r4.append(' ');
                }
                r4.append(s.info.name);
            }
        }
        if (PackageManagerService.DEBUG_REMOVE && chatty) {
            StringBuilder sb4 = new StringBuilder();
            sb4.append("  Services: ");
            if (r4 != null) {
                charSequence = r4;
            }
            sb4.append((Object) charSequence);
            Log.d(TAG, sb4.toString());
        }
    }

    @GuardedBy({"mLock"})
    private void assertProvidersNotDefinedLocked(PackageParser.Package pkg) throws PackageManagerException {
        int providersSize = pkg.providers.size();
        for (int i = 0; i < providersSize; i++) {
            PackageParser.Provider p = (PackageParser.Provider) pkg.providers.get(i);
            if (p.info.authority != null) {
                String[] names = p.info.authority.split(";");
                for (int j = 0; j < names.length; j++) {
                    if (this.mProvidersByAuthority.containsKey(names[j])) {
                        PackageParser.Provider other = this.mProvidersByAuthority.get(names[j]);
                        String otherPackageName = (other == null || other.getComponentName() == null) ? "?" : other.getComponentName().getPackageName();
                        if (!otherPackageName.equals(pkg.packageName)) {
                            throw new PackageManagerException(-13, "Can't install because provider name " + names[j] + " (in package " + pkg.applicationInfo.packageName + ") is already used by " + otherPackageName);
                        }
                    }
                }
                continue;
            }
        }
    }

    /* access modifiers changed from: private */
    public static final class ActivityIntentResolver extends IntentResolver<PackageParser.ActivityIntentInfo, ResolveInfo> {
        private static final String HW_ACTION_QUERY_ALL_BY_PREFIX = "com.huawei.action.QUERY_BY_PREFIX";
        private static final String HW_EXTRA_ACTION_PREFIX = "com.huawei.extra.ACTION_PREFIX";
        private final ArrayMap<ComponentName, PackageParser.Activity> mActivities;
        private int mFlags;

        private ActivityIntentResolver() {
            this.mActivities = new ArrayMap<>();
        }

        @Override // com.android.server.IntentResolver
        public List<ResolveInfo> queryIntent(Intent intent, String resolvedType, boolean defaultOnly, int userId) {
            if (!ComponentResolver.sUserManager.exists(userId)) {
                return null;
            }
            this.mFlags = defaultOnly ? 65536 : 0;
            return super.queryIntent(intent, resolvedType, defaultOnly, userId);
        }

        /* access modifiers changed from: package-private */
        public List<ResolveInfo> queryIntent(Intent intent, String resolvedType, int flags, int userId) {
            if (!ComponentResolver.sUserManager.exists(userId)) {
                return null;
            }
            this.mFlags = flags;
            if (HW_ACTION_QUERY_ALL_BY_PREFIX.equals(intent.getAction())) {
                try {
                    return super.queryAllByActionPrefix(userId, intent.getStringExtra(HW_EXTRA_ACTION_PREFIX));
                } catch (BadParcelableException e) {
                    Log.e(ComponentResolver.TAG, "intent lack of necessary extra info.");
                    return Collections.emptyList();
                }
            } else {
                return super.queryIntent(intent, resolvedType, (65536 & flags) != 0, userId);
            }
        }

        /* access modifiers changed from: package-private */
        public List<ResolveInfo> queryIntentForPackage(Intent intent, String resolvedType, int flags, List<PackageParser.Activity> packageActivities, int userId) {
            if (!ComponentResolver.sUserManager.exists(userId) || packageActivities == null) {
                return null;
            }
            this.mFlags = flags;
            boolean defaultOnly = (65536 & flags) != 0;
            int activitiesSize = packageActivities.size();
            ArrayList<PackageParser.ActivityIntentInfo[]> listCut = new ArrayList<>(activitiesSize);
            for (int i = 0; i < activitiesSize; i++) {
                ArrayList<PackageParser.ActivityIntentInfo> intentFilters = packageActivities.get(i).intents;
                if (intentFilters != null && intentFilters.size() > 0) {
                    PackageParser.ActivityIntentInfo[] array = new PackageParser.ActivityIntentInfo[intentFilters.size()];
                    intentFilters.toArray(array);
                    listCut.add(array);
                }
            }
            return super.queryIntentFromList(intent, resolvedType, defaultOnly, listCut, userId);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void addActivity(PackageParser.Activity a, String type, List<PackageParser.ActivityIntentInfo> newIntents) {
            this.mActivities.put(a.getComponentName(), a);
            int intentsSize = a.intents.size();
            for (int j = 0; j < intentsSize; j++) {
                PackageParser.ActivityIntentInfo intent = (PackageParser.ActivityIntentInfo) a.intents.get(j);
                if (newIntents != null && "activity".equals(type)) {
                    newIntents.add(intent);
                }
                if (!intent.debugCheck()) {
                    Log.w(ComponentResolver.TAG, "==> For Activity " + a.info.name);
                }
                addFilter(intent);
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void removeActivity(PackageParser.Activity a, String type) {
            this.mActivities.remove(a.getComponentName());
            int intentsSize = a.intents.size();
            for (int j = 0; j < intentsSize; j++) {
                removeFilter((PackageParser.ActivityIntentInfo) a.intents.get(j));
            }
        }

        /* access modifiers changed from: protected */
        public boolean allowFilterResult(PackageParser.ActivityIntentInfo filter, List<ResolveInfo> dest) {
            ActivityInfo filterAi = filter.activity.info;
            for (int i = dest.size() - 1; i >= 0; i--) {
                ActivityInfo destAi = dest.get(i).activityInfo;
                if (destAi.name == filterAi.name && destAi.packageName == filterAi.packageName) {
                    return false;
                }
            }
            return true;
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.IntentResolver
        public PackageParser.ActivityIntentInfo[] newArray(int size) {
            return new PackageParser.ActivityIntentInfo[size];
        }

        /* access modifiers changed from: protected */
        public boolean isFilterStopped(PackageParser.ActivityIntentInfo filter, int userId) {
            PackageSetting ps;
            if (!ComponentResolver.sUserManager.exists(userId)) {
                return true;
            }
            PackageParser.Package p = filter.activity.owner;
            if (p == null || (ps = (PackageSetting) p.mExtras) == null) {
                return false;
            }
            if (((ps.pkgFlags & 1) == 0 || HwServiceFactory.isCustedCouldStopped(p.packageName, true, ps.getStopped(userId))) && ps.getStopped(userId)) {
                return true;
            }
            return false;
        }

        /* access modifiers changed from: protected */
        public boolean isPackageForFilter(String packageName, PackageParser.ActivityIntentInfo info) {
            return packageName.equals(info.activity.owner.packageName);
        }

        private void log(String reason, PackageParser.ActivityIntentInfo info, int match, int userId) {
            Slog.w(ComponentResolver.TAG, reason + "; match: " + DebugUtils.flagsToString(IntentFilter.class, "MATCH_", match) + "; userId: " + userId + "; intent info: " + info);
        }

        /* access modifiers changed from: protected */
        public ResolveInfo newResult(PackageParser.ActivityIntentInfo info, int match, int userId) {
            PackageUserState userState;
            ActivityInfo ai;
            if (!ComponentResolver.sUserManager.exists(userId) || !ComponentResolver.sPackageManagerInternal.isEnabledAndMatches(info.activity.info, this.mFlags, userId)) {
                return null;
            }
            PackageParser.Activity activity = info.activity;
            PackageSetting ps = (PackageSetting) activity.owner.mExtras;
            if (ps == null || (ai = PackageParser.generateActivityInfo(activity, this.mFlags, (userState = ps.readUserState(userId)), userId)) == null) {
                return null;
            }
            boolean matchInstantApp = false;
            boolean matchExplicitlyVisibleOnly = (this.mFlags & DumpState.DUMP_APEX) != 0;
            boolean matchVisibleToInstantApp = (this.mFlags & DumpState.DUMP_SERVICE_PERMISSIONS) != 0;
            boolean componentVisible = matchVisibleToInstantApp && info.isVisibleToInstantApp() && (!matchExplicitlyVisibleOnly || info.isExplicitlyVisibleToInstantApp());
            if ((this.mFlags & DumpState.DUMP_VOLUMES) != 0) {
                matchInstantApp = true;
            }
            if (matchVisibleToInstantApp && !componentVisible && !userState.instantApp) {
                return null;
            }
            if (!matchInstantApp && userState.instantApp) {
                return null;
            }
            if (userState.instantApp && ps.isUpdateAvailable()) {
                return null;
            }
            ResolveInfo res = new ResolveInfo();
            res.activityInfo = ai;
            if ((this.mFlags & 64) != 0) {
                res.filter = info;
            }
            if (info.countActionFilters() > 0) {
                res.filter = info;
            }
            res.handleAllWebDataURI = info.handleAllWebDataURI();
            res.priority = info.getPriority();
            res.preferredOrder = activity.owner.mPreferredOrder;
            res.match = match;
            res.isDefault = info.hasDefault;
            res.labelRes = info.labelRes;
            res.nonLocalizedLabel = info.nonLocalizedLabel;
            if (ComponentResolver.sPackageManagerInternal.userNeedsBadging(userId)) {
                res.noResourceId = true;
            } else {
                res.icon = info.icon;
            }
            res.iconResourceId = info.icon;
            res.system = res.activityInfo.applicationInfo.isSystemApp();
            res.isInstantAppAvailable = userState.instantApp;
            if (!ComponentResolver.sPackageManagerInternal.isNeedForbidAppAct(res.activityInfo.packageName)) {
                return res;
            }
            Slog.i(ComponentResolver.TAG, "forbid query activity packageName");
            return null;
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.IntentResolver
        public void sortResults(List<ResolveInfo> results) {
            results.sort(ComponentResolver.RESOLVE_PRIORITY_SORTER);
        }

        /* access modifiers changed from: protected */
        public void dumpFilter(PrintWriter out, String prefix, PackageParser.ActivityIntentInfo filter) {
            out.print(prefix);
            out.print(Integer.toHexString(System.identityHashCode(filter.activity)));
            out.print(' ');
            filter.activity.printComponentShortName(out);
            out.print(" filter ");
            out.println(Integer.toHexString(System.identityHashCode(filter)));
        }

        /* access modifiers changed from: protected */
        public Object filterToLabel(PackageParser.ActivityIntentInfo filter) {
            return filter.activity;
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.IntentResolver
        public void dumpFilterLabel(PrintWriter out, String prefix, Object label, int count) {
            PackageParser.Activity activity = (PackageParser.Activity) label;
            out.print(prefix);
            out.print(Integer.toHexString(System.identityHashCode(activity)));
            out.print(' ');
            activity.printComponentShortName(out);
            if (count > 1) {
                out.print(" (");
                out.print(count);
                out.print(" filters)");
            }
            out.println();
        }
    }

    /* access modifiers changed from: private */
    public static final class ProviderIntentResolver extends IntentResolver<PackageParser.ProviderIntentInfo, ResolveInfo> {
        private int mFlags;
        private final ArrayMap<ComponentName, PackageParser.Provider> mProviders;

        private ProviderIntentResolver() {
            this.mProviders = new ArrayMap<>();
        }

        @Override // com.android.server.IntentResolver
        public List<ResolveInfo> queryIntent(Intent intent, String resolvedType, boolean defaultOnly, int userId) {
            this.mFlags = defaultOnly ? 65536 : 0;
            return super.queryIntent(intent, resolvedType, defaultOnly, userId);
        }

        /* access modifiers changed from: package-private */
        public List<ResolveInfo> queryIntent(Intent intent, String resolvedType, int flags, int userId) {
            if (!ComponentResolver.sUserManager.exists(userId)) {
                return null;
            }
            this.mFlags = flags;
            return super.queryIntent(intent, resolvedType, (65536 & flags) != 0, userId);
        }

        /* access modifiers changed from: package-private */
        public List<ResolveInfo> queryIntentForPackage(Intent intent, String resolvedType, int flags, List<PackageParser.Provider> packageProviders, int userId) {
            if (!ComponentResolver.sUserManager.exists(userId) || packageProviders == null) {
                return null;
            }
            this.mFlags = flags;
            boolean defaultOnly = (65536 & flags) != 0;
            int providersSize = packageProviders.size();
            ArrayList<PackageParser.ProviderIntentInfo[]> listCut = new ArrayList<>(providersSize);
            for (int i = 0; i < providersSize; i++) {
                ArrayList<PackageParser.ProviderIntentInfo> intentFilters = packageProviders.get(i).intents;
                if (intentFilters != null && intentFilters.size() > 0) {
                    PackageParser.ProviderIntentInfo[] array = new PackageParser.ProviderIntentInfo[intentFilters.size()];
                    intentFilters.toArray(array);
                    listCut.add(array);
                }
            }
            return super.queryIntentFromList(intent, resolvedType, defaultOnly, listCut, userId);
        }

        /* access modifiers changed from: package-private */
        public void addProvider(PackageParser.Provider p) {
            if (this.mProviders.containsKey(p.getComponentName())) {
                Slog.w(ComponentResolver.TAG, "Provider " + p.getComponentName() + " already defined; ignoring");
                return;
            }
            this.mProviders.put(p.getComponentName(), p);
            int intentsSize = p.intents.size();
            for (int j = 0; j < intentsSize; j++) {
                PackageParser.ProviderIntentInfo intent = (PackageParser.ProviderIntentInfo) p.intents.get(j);
                if (!intent.debugCheck()) {
                    Log.w(ComponentResolver.TAG, "==> For Provider " + p.info.name);
                }
                addFilter(intent);
            }
        }

        /* access modifiers changed from: package-private */
        public void removeProvider(PackageParser.Provider p) {
            this.mProviders.remove(p.getComponentName());
            int intentsSize = p.intents.size();
            for (int j = 0; j < intentsSize; j++) {
                removeFilter((PackageParser.ProviderIntentInfo) p.intents.get(j));
            }
        }

        /* access modifiers changed from: protected */
        public boolean allowFilterResult(PackageParser.ProviderIntentInfo filter, List<ResolveInfo> dest) {
            ProviderInfo filterPi = filter.provider.info;
            for (int i = dest.size() - 1; i >= 0; i--) {
                ProviderInfo destPi = dest.get(i).providerInfo;
                if (destPi.name == filterPi.name && destPi.packageName == filterPi.packageName) {
                    return false;
                }
            }
            return true;
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.IntentResolver
        public PackageParser.ProviderIntentInfo[] newArray(int size) {
            return new PackageParser.ProviderIntentInfo[size];
        }

        /* access modifiers changed from: protected */
        public boolean isFilterStopped(PackageParser.ProviderIntentInfo filter, int userId) {
            PackageSetting ps;
            if (!ComponentResolver.sUserManager.exists(userId)) {
                return true;
            }
            PackageParser.Package p = filter.provider.owner;
            if (p == null || (ps = (PackageSetting) p.mExtras) == null) {
                return false;
            }
            if ((ps.pkgFlags & 1) != 0 || !ps.getStopped(userId)) {
                return false;
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean isPackageForFilter(String packageName, PackageParser.ProviderIntentInfo info) {
            return packageName.equals(info.provider.owner.packageName);
        }

        /* access modifiers changed from: protected */
        public ResolveInfo newResult(PackageParser.ProviderIntentInfo filter, int match, int userId) {
            ProviderInfo pi;
            if (!ComponentResolver.sUserManager.exists(userId) || !ComponentResolver.sPackageManagerInternal.isEnabledAndMatches(filter.provider.info, this.mFlags, userId)) {
                return null;
            }
            PackageParser.Provider provider = filter.provider;
            PackageSetting ps = (PackageSetting) provider.owner.mExtras;
            if (ps == null) {
                return null;
            }
            PackageUserState userState = ps.readUserState(userId);
            boolean isInstantApp = true;
            boolean matchVisibleToInstantApp = (this.mFlags & DumpState.DUMP_SERVICE_PERMISSIONS) != 0;
            if ((this.mFlags & DumpState.DUMP_VOLUMES) == 0) {
                isInstantApp = false;
            }
            if (matchVisibleToInstantApp && !filter.isVisibleToInstantApp() && !userState.instantApp) {
                return null;
            }
            if (!isInstantApp && userState.instantApp) {
                return null;
            }
            if ((userState.instantApp && ps.isUpdateAvailable()) || (pi = PackageParser.generateProviderInfo(provider, this.mFlags, userState, userId)) == null) {
                return null;
            }
            ResolveInfo res = new ResolveInfo();
            res.providerInfo = pi;
            if ((this.mFlags & 64) != 0) {
                res.filter = filter;
            }
            res.priority = filter.getPriority();
            res.preferredOrder = provider.owner.mPreferredOrder;
            res.match = match;
            res.isDefault = filter.hasDefault;
            res.labelRes = filter.labelRes;
            res.nonLocalizedLabel = filter.nonLocalizedLabel;
            res.icon = filter.icon;
            res.system = res.providerInfo.applicationInfo.isSystemApp();
            if (!ComponentResolver.sPackageManagerInternal.isNeedForbidAppAct(res.providerInfo.packageName)) {
                return res;
            }
            Slog.i(ComponentResolver.TAG, "forbid query provider packageName");
            return null;
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.IntentResolver
        public void sortResults(List<ResolveInfo> results) {
            results.sort(ComponentResolver.RESOLVE_PRIORITY_SORTER);
        }

        /* access modifiers changed from: protected */
        public void dumpFilter(PrintWriter out, String prefix, PackageParser.ProviderIntentInfo filter) {
            out.print(prefix);
            out.print(Integer.toHexString(System.identityHashCode(filter.provider)));
            out.print(' ');
            filter.provider.printComponentShortName(out);
            out.print(" filter ");
            out.println(Integer.toHexString(System.identityHashCode(filter)));
        }

        /* access modifiers changed from: protected */
        public Object filterToLabel(PackageParser.ProviderIntentInfo filter) {
            return filter.provider;
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.IntentResolver
        public void dumpFilterLabel(PrintWriter out, String prefix, Object label, int count) {
            PackageParser.Provider provider = (PackageParser.Provider) label;
            out.print(prefix);
            out.print(Integer.toHexString(System.identityHashCode(provider)));
            out.print(' ');
            provider.printComponentShortName(out);
            if (count > 1) {
                out.print(" (");
                out.print(count);
                out.print(" filters)");
            }
            out.println();
        }
    }

    /* access modifiers changed from: private */
    public static final class ServiceIntentResolver extends IntentResolver<PackageParser.ServiceIntentInfo, ResolveInfo> {
        private static final String HW_AA_ACTION_PREFIX = "com.huawei.aa.action.";
        private static final String HW_ACTION_QUERY_ALL_BY_PREFIX = "com.huawei.action.QUERY_BY_PREFIX";
        private static final String HW_EXTRA_ACTION_PREFIX = "com.huawei.extra.ACTION_PREFIX";
        private int mFlags;
        private final ArrayMap<ComponentName, PackageParser.Service> mServices;

        private ServiceIntentResolver() {
            this.mServices = new ArrayMap<>();
        }

        @Override // com.android.server.IntentResolver
        public List<ResolveInfo> queryIntent(Intent intent, String resolvedType, boolean defaultOnly, int userId) {
            this.mFlags = defaultOnly ? 65536 : 0;
            return super.queryIntent(intent, resolvedType, defaultOnly, userId);
        }

        /* access modifiers changed from: package-private */
        public List<ResolveInfo> queryIntent(Intent intent, String resolvedType, int flags, int userId) {
            if (!ComponentResolver.sUserManager.exists(userId)) {
                return null;
            }
            this.mFlags = flags;
            String action = intent.getAction();
            if (HW_ACTION_QUERY_ALL_BY_PREFIX.equals(action)) {
                try {
                    return super.queryAllByActionPrefix(userId, intent.getStringExtra(HW_EXTRA_ACTION_PREFIX));
                } catch (BadParcelableException e) {
                    Log.e(ComponentResolver.TAG, "queryIntent failed, catch" + e.getClass());
                    return Collections.emptyList();
                }
            } else {
                boolean z = true;
                if (action == null || !action.startsWith(HW_AA_ACTION_PREFIX)) {
                    if ((65536 & flags) == 0) {
                        z = false;
                    }
                    return super.queryIntent(intent, resolvedType, z, userId);
                }
                ResolveInfo[] extInfo = ComponentResolver.getHwPmsEx().queryExtService(action, intent.getPackage());
                if ((65536 & flags) == 0) {
                    z = false;
                }
                List<ResolveInfo> ret = super.queryIntent(intent, resolvedType, z, userId);
                if (extInfo != null) {
                    ret.addAll(Arrays.asList(extInfo));
                }
                return ret;
            }
        }

        /* access modifiers changed from: package-private */
        public List<ResolveInfo> queryIntentForPackage(Intent intent, String resolvedType, int flags, List<PackageParser.Service> packageServices, int userId) {
            if (!ComponentResolver.sUserManager.exists(userId) || packageServices == null) {
                return null;
            }
            this.mFlags = flags;
            boolean defaultOnly = (65536 & flags) != 0;
            int servicesSize = packageServices.size();
            ArrayList<PackageParser.ServiceIntentInfo[]> listCut = new ArrayList<>(servicesSize);
            for (int i = 0; i < servicesSize; i++) {
                ArrayList<PackageParser.ServiceIntentInfo> intentFilters = packageServices.get(i).intents;
                if (intentFilters != null && intentFilters.size() > 0) {
                    PackageParser.ServiceIntentInfo[] array = new PackageParser.ServiceIntentInfo[intentFilters.size()];
                    intentFilters.toArray(array);
                    listCut.add(array);
                }
            }
            return super.queryIntentFromList(intent, resolvedType, defaultOnly, listCut, userId);
        }

        /* access modifiers changed from: package-private */
        public void addService(PackageParser.Service s) {
            this.mServices.put(s.getComponentName(), s);
            int intentsSize = s.intents.size();
            for (int j = 0; j < intentsSize; j++) {
                PackageParser.ServiceIntentInfo intent = (PackageParser.ServiceIntentInfo) s.intents.get(j);
                if (!intent.debugCheck()) {
                    Log.w(ComponentResolver.TAG, "==> For Service " + s.info.name);
                }
                addFilter(intent);
            }
        }

        /* access modifiers changed from: package-private */
        public void removeService(PackageParser.Service s) {
            this.mServices.remove(s.getComponentName());
            int intentsSize = s.intents.size();
            for (int j = 0; j < intentsSize; j++) {
                removeFilter((PackageParser.ServiceIntentInfo) s.intents.get(j));
            }
        }

        /* access modifiers changed from: protected */
        public boolean allowFilterResult(PackageParser.ServiceIntentInfo filter, List<ResolveInfo> dest) {
            ServiceInfo filterSi = filter.service.info;
            for (int i = dest.size() - 1; i >= 0; i--) {
                ServiceInfo destAi = dest.get(i).serviceInfo;
                if (destAi.name == filterSi.name && destAi.packageName == filterSi.packageName) {
                    return false;
                }
            }
            return true;
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.IntentResolver
        public PackageParser.ServiceIntentInfo[] newArray(int size) {
            return new PackageParser.ServiceIntentInfo[size];
        }

        /* access modifiers changed from: protected */
        public boolean isFilterStopped(PackageParser.ServiceIntentInfo filter, int userId) {
            PackageSetting ps;
            if (!ComponentResolver.sUserManager.exists(userId)) {
                return true;
            }
            PackageParser.Package p = filter.service.owner;
            if (p == null || (ps = (PackageSetting) p.mExtras) == null) {
                return false;
            }
            if ((ps.pkgFlags & 1) != 0 || !ps.getStopped(userId)) {
                return false;
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean isPackageForFilter(String packageName, PackageParser.ServiceIntentInfo info) {
            return packageName.equals(info.service.owner.packageName);
        }

        /* access modifiers changed from: protected */
        public ResolveInfo newResult(PackageParser.ServiceIntentInfo filter, int match, int userId) {
            PackageUserState userState;
            ServiceInfo si;
            if (!ComponentResolver.sUserManager.exists(userId) || !ComponentResolver.sPackageManagerInternal.isEnabledAndMatches(filter.service.info, this.mFlags, userId)) {
                return null;
            }
            PackageParser.Service service = filter.service;
            PackageSetting ps = (PackageSetting) service.owner.mExtras;
            if (ps == null || (si = PackageParser.generateServiceInfo(service, this.mFlags, (userState = ps.readUserState(userId)), userId)) == null) {
                return null;
            }
            boolean isInstantApp = true;
            boolean matchVisibleToInstantApp = (this.mFlags & DumpState.DUMP_SERVICE_PERMISSIONS) != 0;
            if ((this.mFlags & DumpState.DUMP_VOLUMES) == 0) {
                isInstantApp = false;
            }
            if (matchVisibleToInstantApp && !filter.isVisibleToInstantApp() && !userState.instantApp) {
                return null;
            }
            if (!isInstantApp && userState.instantApp) {
                return null;
            }
            if (userState.instantApp && ps.isUpdateAvailable()) {
                return null;
            }
            ResolveInfo res = new ResolveInfo();
            res.serviceInfo = si;
            if ((this.mFlags & 64) != 0) {
                res.filter = filter;
            }
            res.priority = filter.getPriority();
            res.preferredOrder = service.owner.mPreferredOrder;
            res.match = match;
            res.isDefault = filter.hasDefault;
            res.labelRes = filter.labelRes;
            res.nonLocalizedLabel = filter.nonLocalizedLabel;
            res.icon = filter.icon;
            res.system = res.serviceInfo.applicationInfo.isSystemApp();
            if (!ComponentResolver.sPackageManagerInternal.isNeedForbidAppAct(res.serviceInfo.packageName)) {
                return res;
            }
            Slog.i(ComponentResolver.TAG, "forbid query service packageName");
            return null;
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.IntentResolver
        public void sortResults(List<ResolveInfo> results) {
            results.sort(ComponentResolver.RESOLVE_PRIORITY_SORTER);
        }

        /* access modifiers changed from: protected */
        public void dumpFilter(PrintWriter out, String prefix, PackageParser.ServiceIntentInfo filter) {
            out.print(prefix);
            out.print(Integer.toHexString(System.identityHashCode(filter.service)));
            out.print(' ');
            filter.service.printComponentShortName(out);
            out.print(" filter ");
            out.print(Integer.toHexString(System.identityHashCode(filter)));
            if (filter.service.info.permission != null) {
                out.print(" permission ");
                out.println(filter.service.info.permission);
                return;
            }
            out.println();
        }

        /* access modifiers changed from: protected */
        public Object filterToLabel(PackageParser.ServiceIntentInfo filter) {
            return filter.service;
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.IntentResolver
        public void dumpFilterLabel(PrintWriter out, String prefix, Object label, int count) {
            PackageParser.Service service = (PackageParser.Service) label;
            out.print(prefix);
            out.print(Integer.toHexString(System.identityHashCode(service)));
            out.print(' ');
            service.printComponentShortName(out);
            if (count > 1) {
                out.print(" (");
                out.print(count);
                out.print(" filters)");
            }
            out.println();
        }
    }

    static final class InstantAppIntentResolver extends IntentResolver<AuxiliaryResolveInfo.AuxiliaryFilter, AuxiliaryResolveInfo.AuxiliaryFilter> {
        final ArrayMap<String, Pair<Integer, InstantAppResolveInfo>> mOrderResult = new ArrayMap<>();

        InstantAppIntentResolver() {
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.IntentResolver
        public AuxiliaryResolveInfo.AuxiliaryFilter[] newArray(int size) {
            return new AuxiliaryResolveInfo.AuxiliaryFilter[size];
        }

        /* access modifiers changed from: protected */
        public boolean isPackageForFilter(String packageName, AuxiliaryResolveInfo.AuxiliaryFilter responseObj) {
            return true;
        }

        /* access modifiers changed from: protected */
        public AuxiliaryResolveInfo.AuxiliaryFilter newResult(AuxiliaryResolveInfo.AuxiliaryFilter responseObj, int match, int userId) {
            if (!ComponentResolver.sUserManager.exists(userId)) {
                return null;
            }
            String packageName = responseObj.resolveInfo.getPackageName();
            Integer order = Integer.valueOf(responseObj.getOrder());
            Pair<Integer, InstantAppResolveInfo> lastOrderResult = this.mOrderResult.get(packageName);
            if (lastOrderResult != null && ((Integer) lastOrderResult.first).intValue() >= order.intValue()) {
                return null;
            }
            InstantAppResolveInfo res = responseObj.resolveInfo;
            if (order.intValue() > 0) {
                this.mOrderResult.put(packageName, new Pair<>(order, res));
            }
            return responseObj;
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.IntentResolver
        public void filterResults(List<AuxiliaryResolveInfo.AuxiliaryFilter> results) {
            if (this.mOrderResult.size() != 0) {
                int resultSize = results.size();
                int i = 0;
                while (i < resultSize) {
                    InstantAppResolveInfo info = results.get(i).resolveInfo;
                    String packageName = info.getPackageName();
                    Pair<Integer, InstantAppResolveInfo> savedInfo = this.mOrderResult.get(packageName);
                    if (savedInfo != null) {
                        if (savedInfo.second == info) {
                            this.mOrderResult.remove(packageName);
                            if (this.mOrderResult.size() == 0) {
                                return;
                            }
                        } else {
                            results.remove(i);
                            resultSize--;
                            i--;
                        }
                    }
                    i++;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public static class IterGenerator<E> {
        IterGenerator() {
        }

        public Iterator<E> generate(PackageParser.ActivityIntentInfo info) {
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public static class ActionIterGenerator extends IterGenerator<String> {
        ActionIterGenerator() {
        }

        @Override // com.android.server.pm.ComponentResolver.IterGenerator
        public Iterator<String> generate(PackageParser.ActivityIntentInfo info) {
            return info.actionsIterator();
        }
    }

    /* access modifiers changed from: package-private */
    public static class CategoriesIterGenerator extends IterGenerator<String> {
        CategoriesIterGenerator() {
        }

        @Override // com.android.server.pm.ComponentResolver.IterGenerator
        public Iterator<String> generate(PackageParser.ActivityIntentInfo info) {
            return info.categoriesIterator();
        }
    }

    /* access modifiers changed from: package-private */
    public static class SchemesIterGenerator extends IterGenerator<String> {
        SchemesIterGenerator() {
        }

        @Override // com.android.server.pm.ComponentResolver.IterGenerator
        public Iterator<String> generate(PackageParser.ActivityIntentInfo info) {
            return info.schemesIterator();
        }
    }

    /* access modifiers changed from: package-private */
    public static class AuthoritiesIterGenerator extends IterGenerator<IntentFilter.AuthorityEntry> {
        AuthoritiesIterGenerator() {
        }

        @Override // com.android.server.pm.ComponentResolver.IterGenerator
        public Iterator<IntentFilter.AuthorityEntry> generate(PackageParser.ActivityIntentInfo info) {
            return info.authoritiesIterator();
        }
    }

    /* access modifiers changed from: private */
    public static IHwPackageManagerServiceEx getHwPmsEx() {
        return ((PackageManagerService) ServiceManager.getService("package")).getHwPMSEx();
    }
}
