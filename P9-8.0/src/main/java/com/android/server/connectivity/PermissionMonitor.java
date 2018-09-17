package com.android.server.connectivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.UserInfo;
import android.net.Uri;
import android.os.INetworkManagementService;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.server.NetworkManagementService;
import com.android.server.am.HwBroadcastRadarUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class PermissionMonitor {
    private static final boolean DBG = true;
    private static final Boolean NETWORK = Boolean.FALSE;
    private static final Boolean SYSTEM = Boolean.TRUE;
    private static final String TAG = "PermissionMonitor";
    private final Map<Integer, Boolean> mApps = new HashMap();
    private final Context mContext;
    private final BroadcastReceiver mIntentReceiver;
    private final INetworkManagementService mNetd;
    private final PackageManager mPackageManager;
    private final UserManager mUserManager;
    private final Set<Integer> mUsers = new HashSet();

    public PermissionMonitor(Context context, INetworkManagementService netd) {
        this.mContext = context;
        this.mPackageManager = context.getPackageManager();
        this.mUserManager = UserManager.get(context);
        this.mNetd = netd;
        this.mIntentReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                int user = intent.getIntExtra("android.intent.extra.user_handle", -10000);
                int appUid = intent.getIntExtra("android.intent.extra.UID", -1);
                Uri appData = intent.getData();
                String appName = appData != null ? appData.getSchemeSpecificPart() : null;
                if ("android.intent.action.USER_ADDED".equals(action)) {
                    PermissionMonitor.this.onUserAdded(user);
                } else if ("android.intent.action.USER_REMOVED".equals(action)) {
                    PermissionMonitor.this.onUserRemoved(user);
                } else if ("android.intent.action.PACKAGE_ADDED".equals(action)) {
                    PermissionMonitor.this.onAppAdded(appName, appUid);
                } else if ("android.intent.action.PACKAGE_REMOVED".equals(action)) {
                    PermissionMonitor.this.onAppRemoved(appUid);
                }
            }
        };
    }

    public synchronized void startMonitoring() {
        log("Monitoring");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.USER_ADDED");
        intentFilter.addAction("android.intent.action.USER_REMOVED");
        this.mContext.registerReceiverAsUser(this.mIntentReceiver, UserHandle.ALL, intentFilter, null, null);
        intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.PACKAGE_ADDED");
        intentFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        intentFilter.addDataScheme(HwBroadcastRadarUtil.KEY_PACKAGE);
        this.mContext.registerReceiverAsUser(this.mIntentReceiver, UserHandle.ALL, intentFilter, null, null);
        List<PackageInfo> apps = this.mPackageManager.getInstalledPackages(4096);
        if (apps == null) {
            loge("No apps");
            return;
        }
        for (PackageInfo app : apps) {
            int uid = app.applicationInfo != null ? app.applicationInfo.uid : -1;
            if (uid >= 0) {
                boolean isNetwork = hasNetworkPermission(app);
                boolean hasRestrictedPermission = hasRestrictedNetworkPermission(app);
                if (isNetwork || hasRestrictedPermission) {
                    Boolean permission = (Boolean) this.mApps.get(Integer.valueOf(uid));
                    if (permission == null || permission == NETWORK) {
                        this.mApps.put(Integer.valueOf(uid), Boolean.valueOf(hasRestrictedPermission));
                    }
                }
            }
        }
        List<UserInfo> users = this.mUserManager.getUsers(true);
        if (users != null) {
            for (UserInfo user : users) {
                this.mUsers.add(Integer.valueOf(user.id));
            }
        }
        log("Users: " + this.mUsers.size() + ", Apps: " + this.mApps.size());
        update(this.mUsers, this.mApps, true);
    }

    private boolean hasPermission(PackageInfo app, String permission) {
        if (app.requestedPermissions != null) {
            for (String p : app.requestedPermissions) {
                if (permission.equals(p)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasNetworkPermission(PackageInfo app) {
        return hasPermission(app, "android.permission.CHANGE_NETWORK_STATE");
    }

    private boolean hasRestrictedNetworkPermission(PackageInfo app) {
        boolean z = true;
        int flags = app.applicationInfo != null ? app.applicationInfo.flags : 0;
        if ((flags & 1) != 0 || (flags & 128) != 0) {
            return true;
        }
        if (!hasPermission(app, "android.permission.CONNECTIVITY_INTERNAL")) {
            z = hasPermission(app, "android.permission.CONNECTIVITY_USE_RESTRICTED_NETWORKS");
        }
        return z;
    }

    private int[] toIntArray(List<Integer> list) {
        int[] array = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = ((Integer) list.get(i)).intValue();
        }
        return array;
    }

    private void update(Set<Integer> users, Map<Integer, Boolean> apps, boolean add) {
        List<Integer> network = new ArrayList();
        List<Integer> system = new ArrayList();
        for (Entry<Integer, Boolean> app : apps.entrySet()) {
            List<Integer> list = ((Boolean) app.getValue()).booleanValue() ? system : network;
            for (Integer intValue : users) {
                list.add(Integer.valueOf(UserHandle.getUid(intValue.intValue(), ((Integer) app.getKey()).intValue())));
            }
        }
        if (add) {
            try {
                this.mNetd.setPermission(NetworkManagementService.PERMISSION_NETWORK, toIntArray(network));
                this.mNetd.setPermission(NetworkManagementService.PERMISSION_SYSTEM, toIntArray(system));
                return;
            } catch (RemoteException e) {
                loge("Exception when updating permissions: " + e);
                return;
            }
        }
        this.mNetd.clearPermission(toIntArray(network));
        this.mNetd.clearPermission(toIntArray(system));
    }

    private synchronized void onUserAdded(int user) {
        if (user < 0) {
            loge("Invalid user in onUserAdded: " + user);
            return;
        }
        this.mUsers.add(Integer.valueOf(user));
        Set<Integer> users = new HashSet();
        users.add(Integer.valueOf(user));
        update(users, this.mApps, true);
    }

    private synchronized void onUserRemoved(int user) {
        if (user < 0) {
            loge("Invalid user in onUserRemoved: " + user);
            return;
        }
        this.mUsers.remove(Integer.valueOf(user));
        Set<Integer> users = new HashSet();
        users.add(Integer.valueOf(user));
        update(users, this.mApps, false);
    }

    private Boolean highestPermissionForUid(Boolean currentPermission, String name) {
        if (currentPermission == SYSTEM) {
            return currentPermission;
        }
        try {
            PackageInfo app = this.mPackageManager.getPackageInfo(name, 4096);
            boolean isNetwork = hasNetworkPermission(app);
            boolean hasRestrictedPermission = hasRestrictedNetworkPermission(app);
            if (isNetwork || hasRestrictedPermission) {
                currentPermission = Boolean.valueOf(hasRestrictedPermission);
            }
        } catch (NameNotFoundException e) {
            loge("NameNotFoundException " + name);
        }
        return currentPermission;
    }

    /* JADX WARNING: Missing block: B:13:0x0065, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized void onAppAdded(String appName, int appUid) {
        if (TextUtils.isEmpty(appName) || appUid < 0) {
            loge("Invalid app in onAppAdded: " + appName + " | " + appUid);
            return;
        }
        Boolean permission = highestPermissionForUid((Boolean) this.mApps.get(Integer.valueOf(appUid)), appName);
        if (permission != this.mApps.get(Integer.valueOf(appUid))) {
            this.mApps.put(Integer.valueOf(appUid), permission);
            Map<Integer, Boolean> apps = new HashMap();
            apps.put(Integer.valueOf(appUid), permission);
            update(this.mUsers, apps, true);
        }
    }

    /* JADX WARNING: Missing block: B:28:0x0067, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized void onAppRemoved(int appUid) {
        synchronized (this) {
            if (appUid < 0) {
                loge("Invalid app in onAppRemoved: " + appUid);
                return;
            }
            Map<Integer, Boolean> apps = new HashMap();
            Object permission = null;
            String[] packages = this.mPackageManager.getPackagesForUid(appUid);
            if (packages != null && packages.length > 0) {
                for (String name : packages) {
                    Boolean permission2 = highestPermissionForUid(permission, name);
                    if (permission2 == SYSTEM) {
                        return;
                    }
                }
            }
            if (permission == this.mApps.get(Integer.valueOf(appUid))) {
            } else if (permission != null) {
                this.mApps.put(Integer.valueOf(appUid), permission);
                apps.put(Integer.valueOf(appUid), permission);
                update(this.mUsers, apps, true);
            } else {
                this.mApps.remove(Integer.valueOf(appUid));
                apps.put(Integer.valueOf(appUid), NETWORK);
                update(this.mUsers, apps, false);
            }
        }
    }

    private static void log(String s) {
        Log.d(TAG, s);
    }

    private static void loge(String s) {
        Log.e(TAG, s);
    }
}
