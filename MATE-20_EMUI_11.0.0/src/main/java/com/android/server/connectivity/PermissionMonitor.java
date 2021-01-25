package com.android.server.connectivity;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManagerInternal;
import android.content.pm.UserInfo;
import android.net.INetd;
import android.net.UidRange;
import android.os.Build;
import android.os.RemoteException;
import android.os.ServiceSpecificException;
import android.os.UserHandle;
import android.os.UserManager;
import android.system.OsConstants;
import android.util.ArraySet;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.LocalServices;
import com.android.server.SystemConfig;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class PermissionMonitor {
    private static final boolean DBG = true;
    protected static final Boolean NETWORK = Boolean.FALSE;
    protected static final Boolean SYSTEM = Boolean.TRUE;
    private static final String TAG = "PermissionMonitor";
    private static final int VERSION_Q = 29;
    @GuardedBy({"this"})
    private final Set<Integer> mAllApps = new HashSet();
    @GuardedBy({"this"})
    private final Map<Integer, Boolean> mApps = new HashMap();
    private final INetd mNetd;
    private final PackageManager mPackageManager;
    private final UserManager mUserManager;
    @GuardedBy({"this"})
    private final Set<Integer> mUsers = new HashSet();
    @GuardedBy({"this"})
    private final Map<String, Set<UidRange>> mVpnUidRanges = new HashMap();

    /* access modifiers changed from: private */
    public class PackageListObserver implements PackageManagerInternal.PackageListObserver {
        private PackageListObserver() {
        }

        private int getPermissionForUid(int uid) {
            int permission = 0;
            String[] packages = PermissionMonitor.this.mPackageManager.getPackagesForUid(uid);
            if (packages == null || packages.length <= 0) {
                return -1;
            }
            for (String name : packages) {
                PackageInfo app = PermissionMonitor.this.getPackageInfo(name);
                if (!(app == null || app.requestedPermissions == null)) {
                    permission |= PermissionMonitor.getNetdPermissionMask(app.requestedPermissions, app.requestedPermissionsFlags);
                }
            }
            return permission;
        }

        public void onPackageAdded(String packageName, int uid) {
            PermissionMonitor.this.sendPackagePermissionsForUid(uid, getPermissionForUid(uid));
        }

        public void onPackageRemoved(String packageName, int uid) {
            PermissionMonitor.this.sendPackagePermissionsForUid(uid, getPermissionForUid(uid));
        }

        public void onPackageChanged(String packageName, int uid) {
            PermissionMonitor.this.sendPackagePermissionsForUid(uid, getPermissionForUid(uid));
        }
    }

    public PermissionMonitor(Context context, INetd netd) {
        this.mPackageManager = context.getPackageManager();
        this.mUserManager = (UserManager) context.getSystemService("user");
        this.mNetd = netd;
    }

    public synchronized void startMonitoring() {
        Boolean permission;
        log("Monitoring");
        PackageManagerInternal pmi = (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class);
        if (pmi != null) {
            pmi.getPackageList(new PackageListObserver());
        } else {
            loge("failed to get the PackageManagerInternal service");
        }
        List<PackageInfo> apps = this.mPackageManager.getInstalledPackages(4198400);
        if (apps == null) {
            loge("No apps");
            return;
        }
        SparseIntArray netdPermsUids = new SparseIntArray();
        for (PackageInfo app : apps) {
            int uid = app.applicationInfo != null ? app.applicationInfo.uid : -1;
            if (uid >= 0) {
                this.mAllApps.add(Integer.valueOf(UserHandle.getAppId(uid)));
                boolean isNetwork = hasNetworkPermission(app);
                boolean hasRestrictedPermission = hasRestrictedNetworkPermission(app);
                if ((isNetwork || hasRestrictedPermission) && ((permission = this.mApps.get(Integer.valueOf(uid))) == null || permission == NETWORK)) {
                    this.mApps.put(Integer.valueOf(uid), Boolean.valueOf(hasRestrictedPermission));
                }
                netdPermsUids.put(uid, netdPermsUids.get(uid) | getNetdPermissionMask(app.requestedPermissions, app.requestedPermissionsFlags));
            }
        }
        List<UserInfo> users = this.mUserManager.getUsers(true);
        if (users != null) {
            for (UserInfo user : users) {
                this.mUsers.add(Integer.valueOf(user.id));
            }
        }
        SparseArray<ArraySet<String>> systemPermission = SystemConfig.getInstance().getSystemPermissions();
        for (int i = 0; i < systemPermission.size(); i++) {
            ArraySet<String> perms = systemPermission.valueAt(i);
            int uid2 = systemPermission.keyAt(i);
            int netdPermission = 0;
            if (perms != null) {
                netdPermission = 0 | (perms.contains("android.permission.UPDATE_DEVICE_STATS") ? 8 : 0) | (perms.contains("android.permission.INTERNET") ? 4 : 0);
            }
            netdPermsUids.put(uid2, netdPermsUids.get(uid2) | netdPermission);
        }
        log("Users: " + this.mUsers.size() + ", Apps: " + this.mApps.size());
        update(this.mUsers, this.mApps, true);
        sendPackagePermissionsToNetd(netdPermsUids);
    }

    @VisibleForTesting
    static boolean isVendorApp(ApplicationInfo appInfo) {
        return appInfo.isVendor() || appInfo.isOem() || appInfo.isProduct();
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public int getDeviceFirstSdkInt() {
        return Build.VERSION.FIRST_SDK_INT;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public boolean hasPermission(PackageInfo app, String permission) {
        int index;
        if (app.requestedPermissions == null || app.requestedPermissionsFlags == null || (index = ArrayUtils.indexOf(app.requestedPermissions, permission)) < 0 || index >= app.requestedPermissionsFlags.length || (app.requestedPermissionsFlags[index] & 2) == 0) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public boolean hasNetworkPermission(PackageInfo app) {
        return hasPermission(app, "android.permission.CHANGE_NETWORK_STATE");
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public boolean hasRestrictedNetworkPermission(PackageInfo app) {
        if (app.applicationInfo != null) {
            if (app.applicationInfo.uid == 1000 && getDeviceFirstSdkInt() < 29) {
                return true;
            }
            if (app.applicationInfo.targetSdkVersion < 29 && isVendorApp(app.applicationInfo)) {
                return true;
            }
        }
        if (hasPermission(app, "android.permission.CONNECTIVITY_INTERNAL") || hasPermission(app, "android.permission.NETWORK_STACK") || hasPermission(app, "android.permission.CONNECTIVITY_USE_RESTRICTED_NETWORKS")) {
            return true;
        }
        return false;
    }

    public synchronized boolean hasUseBackgroundNetworksPermission(int uid) {
        return this.mApps.containsKey(Integer.valueOf(uid));
    }

    private int[] toIntArray(Collection<Integer> list) {
        int[] array = new int[list.size()];
        int i = 0;
        for (Integer item : list) {
            array[i] = item.intValue();
            i++;
        }
        return array;
    }

    private void update(Set<Integer> users, Map<Integer, Boolean> apps, boolean add) {
        List<Integer> network = new ArrayList<>();
        List<Integer> system = new ArrayList<>();
        for (Map.Entry<Integer, Boolean> app : apps.entrySet()) {
            List<Integer> list = app.getValue().booleanValue() ? system : network;
            for (Integer num : users) {
                list.add(Integer.valueOf(UserHandle.getUid(num.intValue(), app.getKey().intValue())));
            }
        }
        if (add) {
            try {
                this.mNetd.networkSetPermissionForUser(1, toIntArray(network));
                this.mNetd.networkSetPermissionForUser(2, toIntArray(system));
            } catch (RemoteException | IllegalStateException e) {
                loge("Exception when updating permissions: " + e);
            }
        } else {
            this.mNetd.networkClearPermissionForUser(toIntArray(network));
            this.mNetd.networkClearPermissionForUser(toIntArray(system));
        }
    }

    public synchronized void onUserAdded(int user) {
        if (user < 0) {
            loge("Invalid user in onUserAdded: " + user);
            return;
        }
        this.mUsers.add(Integer.valueOf(user));
        Set<Integer> users = new HashSet<>();
        users.add(Integer.valueOf(user));
        update(users, this.mApps, true);
    }

    public synchronized void onUserRemoved(int user) {
        if (user < 0) {
            loge("Invalid user in onUserRemoved: " + user);
            return;
        }
        this.mUsers.remove(Integer.valueOf(user));
        Set<Integer> users = new HashSet<>();
        users.add(Integer.valueOf(user));
        update(users, this.mApps, false);
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public Boolean highestPermissionForUid(Boolean currentPermission, String name) {
        if (currentPermission == SYSTEM) {
            return currentPermission;
        }
        try {
            PackageInfo app = this.mPackageManager.getPackageInfo(name, 4096);
            boolean isNetwork = hasNetworkPermission(app);
            boolean hasRestrictedPermission = hasRestrictedNetworkPermission(app);
            if (isNetwork || hasRestrictedPermission) {
                return Boolean.valueOf(hasRestrictedPermission);
            }
            return currentPermission;
        } catch (PackageManager.NameNotFoundException e) {
            loge("NameNotFoundException " + name);
            return currentPermission;
        }
    }

    public synchronized void onPackageAdded(String packageName, int uid) {
        Boolean permission = highestPermissionForUid(this.mApps.get(Integer.valueOf(uid)), packageName);
        if (permission != this.mApps.get(Integer.valueOf(uid))) {
            this.mApps.put(Integer.valueOf(uid), permission);
            Map<Integer, Boolean> apps = new HashMap<>();
            apps.put(Integer.valueOf(uid), permission);
            update(this.mUsers, apps, true);
        }
        for (Map.Entry<String, Set<UidRange>> vpn : this.mVpnUidRanges.entrySet()) {
            if (UidRange.containsUid(vpn.getValue(), uid)) {
                Set<Integer> changedUids = new HashSet<>();
                changedUids.add(Integer.valueOf(uid));
                removeBypassingUids(changedUids, -1);
                updateVpnUids(vpn.getKey(), changedUids, true);
            }
        }
        this.mAllApps.add(Integer.valueOf(UserHandle.getAppId(uid)));
    }

    public synchronized void onPackageRemoved(int uid) {
        for (Map.Entry<String, Set<UidRange>> vpn : this.mVpnUidRanges.entrySet()) {
            if (UidRange.containsUid(vpn.getValue(), uid)) {
                Set<Integer> changedUids = new HashSet<>();
                changedUids.add(Integer.valueOf(uid));
                removeBypassingUids(changedUids, -1);
                updateVpnUids(vpn.getKey(), changedUids, false);
            }
        }
        if (this.mPackageManager.getNameForUid(uid) == null) {
            this.mAllApps.remove(Integer.valueOf(UserHandle.getAppId(uid)));
        }
        Map<Integer, Boolean> apps = new HashMap<>();
        Boolean permission = null;
        String[] packages = this.mPackageManager.getPackagesForUid(uid);
        if (packages != null && packages.length > 0) {
            Boolean permission2 = null;
            for (String name : packages) {
                permission2 = highestPermissionForUid(permission2, name);
                if (permission2 == SYSTEM) {
                    return;
                }
            }
            permission = permission2;
        }
        if (permission != this.mApps.get(Integer.valueOf(uid))) {
            if (permission != null) {
                this.mApps.put(Integer.valueOf(uid), permission);
                apps.put(Integer.valueOf(uid), permission);
                update(this.mUsers, apps, true);
            } else {
                this.mApps.remove(Integer.valueOf(uid));
                apps.put(Integer.valueOf(uid), NETWORK);
                update(this.mUsers, apps, false);
            }
        }
    }

    /* access modifiers changed from: private */
    public static int getNetdPermissionMask(String[] requestedPermissions, int[] requestedPermissionsFlags) {
        int permissions = 0;
        if (requestedPermissions == null || requestedPermissionsFlags == null) {
            return 0;
        }
        for (int i = 0; i < requestedPermissions.length; i++) {
            if (requestedPermissions[i].equals("android.permission.INTERNET") && (requestedPermissionsFlags[i] & 2) != 0) {
                permissions |= 4;
            }
            if (requestedPermissions[i].equals("android.permission.UPDATE_DEVICE_STATS") && (requestedPermissionsFlags[i] & 2) != 0) {
                permissions |= 8;
            }
        }
        return permissions;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private PackageInfo getPackageInfo(String packageName) {
        try {
            return this.mPackageManager.getPackageInfo(packageName, 4198400);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    public synchronized void onVpnUidRangesAdded(String iface, Set<UidRange> rangesToAdd, int vpnAppUid) {
        Set<Integer> changedUids = intersectUids(rangesToAdd, this.mAllApps);
        removeBypassingUids(changedUids, vpnAppUid);
        updateVpnUids(iface, changedUids, true);
        if (this.mVpnUidRanges.containsKey(iface)) {
            this.mVpnUidRanges.get(iface).addAll(rangesToAdd);
        } else {
            this.mVpnUidRanges.put(iface, new HashSet(rangesToAdd));
        }
    }

    public synchronized void onVpnUidRangesRemoved(String iface, Set<UidRange> rangesToRemove, int vpnAppUid) {
        Set<Integer> changedUids = intersectUids(rangesToRemove, this.mAllApps);
        removeBypassingUids(changedUids, vpnAppUid);
        updateVpnUids(iface, changedUids, false);
        Set<UidRange> existingRanges = this.mVpnUidRanges.getOrDefault(iface, null);
        if (existingRanges == null) {
            loge("Attempt to remove unknown vpn uid Range iface = " + iface);
            return;
        }
        existingRanges.removeAll(rangesToRemove);
        if (existingRanges.size() == 0) {
            this.mVpnUidRanges.remove(iface);
        }
    }

    private Set<Integer> intersectUids(Set<UidRange> ranges, Set<Integer> appIds) {
        Set<Integer> result = new HashSet<>();
        if (ranges == null) {
            return result;
        }
        for (UidRange range : ranges) {
            for (int userId = range.getStartUser(); userId <= range.getEndUser(); userId++) {
                for (Integer num : appIds) {
                    int uid = UserHandle.getUid(userId, num.intValue());
                    if (range.contains(uid)) {
                        result.add(Integer.valueOf(uid));
                    }
                }
            }
        }
        return result;
    }

    private void removeBypassingUids(Set<Integer> uids, int vpnAppUid) {
        uids.remove(Integer.valueOf(vpnAppUid));
        uids.removeIf(new Predicate() {
            /* class com.android.server.connectivity.$$Lambda$PermissionMonitor$hGPsXXwaQMfu5dqCp_VIYNOM */

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return PermissionMonitor.this.lambda$removeBypassingUids$0$PermissionMonitor((Integer) obj);
            }
        });
    }

    public /* synthetic */ boolean lambda$removeBypassingUids$0$PermissionMonitor(Integer uid) {
        return this.mApps.getOrDefault(uid, NETWORK) == SYSTEM;
    }

    private void updateVpnUids(String iface, Set<Integer> uids, boolean add) {
        if (uids.size() != 0) {
            if (add) {
                try {
                    this.mNetd.firewallAddUidInterfaceRules(iface, toIntArray(uids));
                } catch (ServiceSpecificException e) {
                    if (e.errorCode != OsConstants.EOPNOTSUPP) {
                        loge("Exception when updating permissions: ", e);
                    }
                } catch (RemoteException e2) {
                    loge("Exception when updating permissions: ", e2);
                }
            } else {
                this.mNetd.firewallRemoveUidInterfaceRules(toIntArray(uids));
            }
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void sendPackagePermissionsForUid(int uid, int permissions) {
        SparseIntArray netdPermissionsAppIds = new SparseIntArray();
        netdPermissionsAppIds.put(uid, permissions);
        sendPackagePermissionsToNetd(netdPermissionsAppIds);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void sendPackagePermissionsToNetd(SparseIntArray netdPermissionsAppIds) {
        if (this.mNetd == null) {
            Log.e(TAG, "Failed to get the netd service");
            return;
        }
        ArrayList<Integer> allPermissionAppIds = new ArrayList<>();
        ArrayList<Integer> internetPermissionAppIds = new ArrayList<>();
        ArrayList<Integer> updateStatsPermissionAppIds = new ArrayList<>();
        ArrayList<Integer> noPermissionAppIds = new ArrayList<>();
        ArrayList<Integer> uninstalledAppIds = new ArrayList<>();
        for (int i = 0; i < netdPermissionsAppIds.size(); i++) {
            int permissions = netdPermissionsAppIds.valueAt(i);
            if (permissions != -1) {
                if (permissions == 0) {
                    noPermissionAppIds.add(Integer.valueOf(netdPermissionsAppIds.keyAt(i)));
                } else if (permissions == 4) {
                    internetPermissionAppIds.add(Integer.valueOf(netdPermissionsAppIds.keyAt(i)));
                } else if (permissions == 8) {
                    updateStatsPermissionAppIds.add(Integer.valueOf(netdPermissionsAppIds.keyAt(i)));
                } else if (permissions == 12) {
                    allPermissionAppIds.add(Integer.valueOf(netdPermissionsAppIds.keyAt(i)));
                }
            } else {
                uninstalledAppIds.add(Integer.valueOf(netdPermissionsAppIds.keyAt(i)));
            }
            Log.e(TAG, "unknown permission type: " + permissions + "for uid: " + netdPermissionsAppIds.keyAt(i));
        }
        try {
            if (allPermissionAppIds.size() != 0) {
                this.mNetd.trafficSetNetPermForUids(12, ArrayUtils.convertToIntArray(allPermissionAppIds));
            }
            if (internetPermissionAppIds.size() != 0) {
                this.mNetd.trafficSetNetPermForUids(4, ArrayUtils.convertToIntArray(internetPermissionAppIds));
            }
            if (updateStatsPermissionAppIds.size() != 0) {
                this.mNetd.trafficSetNetPermForUids(8, ArrayUtils.convertToIntArray(updateStatsPermissionAppIds));
            }
            if (noPermissionAppIds.size() != 0) {
                this.mNetd.trafficSetNetPermForUids(0, ArrayUtils.convertToIntArray(noPermissionAppIds));
            }
            if (uninstalledAppIds.size() != 0) {
                this.mNetd.trafficSetNetPermForUids(-1, ArrayUtils.convertToIntArray(uninstalledAppIds));
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Pass appId list of special permission failed." + e);
        }
    }

    @VisibleForTesting
    public Set<UidRange> getVpnUidRanges(String iface) {
        return this.mVpnUidRanges.get(iface);
    }

    public void dump(IndentingPrintWriter pw) {
        pw.println("Interface filtering rules:");
        pw.increaseIndent();
        for (Map.Entry<String, Set<UidRange>> vpn : this.mVpnUidRanges.entrySet()) {
            pw.println("Interface: " + vpn.getKey());
            pw.println("UIDs: " + vpn.getValue().toString());
            pw.println();
        }
        pw.decreaseIndent();
    }

    private static void log(String s) {
        Log.d(TAG, s);
    }

    private static void loge(String s) {
        Log.e(TAG, s);
    }

    private static void loge(String s, Throwable e) {
        Log.e(TAG, s, e);
    }
}
