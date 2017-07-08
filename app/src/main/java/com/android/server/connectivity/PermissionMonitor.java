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
    private static final boolean DBG = false;
    private static final boolean NETWORK = false;
    private static final boolean SYSTEM = true;
    private static final String TAG = "PermissionMonitor";
    private final Map<Integer, Boolean> mApps;
    private final Context mContext;
    private final BroadcastReceiver mIntentReceiver;
    private final INetworkManagementService mNetd;
    private final PackageManager mPackageManager;
    private final UserManager mUserManager;
    private final Set<Integer> mUsers;

    public PermissionMonitor(Context context, INetworkManagementService netd) {
        this.mUsers = new HashSet();
        this.mApps = new HashMap();
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
                String schemeSpecificPart = appData != null ? appData.getSchemeSpecificPart() : null;
                if ("android.intent.action.USER_ADDED".equals(action)) {
                    PermissionMonitor.this.onUserAdded(user);
                } else if ("android.intent.action.USER_REMOVED".equals(action)) {
                    PermissionMonitor.this.onUserRemoved(user);
                } else if ("android.intent.action.PACKAGE_ADDED".equals(action)) {
                    PermissionMonitor.this.onAppAdded(schemeSpecificPart, appUid);
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
        List<PackageInfo> apps = this.mPackageManager.getInstalledPackages(DumpState.DUMP_PREFERRED);
        if (apps == null) {
            loge("No apps");
            return;
        }
        for (PackageInfo app : apps) {
            int uid = app.applicationInfo != null ? app.applicationInfo.uid : -1;
            if (uid >= 0) {
                boolean isNetwork = hasNetworkPermission(app);
                boolean isSystem = hasSystemPermission(app);
                if (isNetwork || isSystem) {
                    Boolean permission = (Boolean) this.mApps.get(Integer.valueOf(uid));
                    if (permission == null || !permission.booleanValue()) {
                        this.mApps.put(Integer.valueOf(uid), Boolean.valueOf(isSystem));
                    }
                }
            }
        }
        List<UserInfo> users = this.mUserManager.getUsers(SYSTEM);
        if (users != null) {
            for (UserInfo user : users) {
                this.mUsers.add(Integer.valueOf(user.id));
            }
        }
        log("Users: " + this.mUsers.size() + ", Apps: " + this.mApps.size());
        update(this.mUsers, this.mApps, SYSTEM);
    }

    private boolean hasPermission(PackageInfo app, String permission) {
        if (app.requestedPermissions != null) {
            for (String p : app.requestedPermissions) {
                if (permission.equals(p)) {
                    return SYSTEM;
                }
            }
        }
        return NETWORK;
    }

    private boolean hasNetworkPermission(PackageInfo app) {
        return hasPermission(app, "android.permission.CHANGE_NETWORK_STATE");
    }

    private boolean hasSystemPermission(PackageInfo app) {
        int flags = 0;
        if (app.applicationInfo != null) {
            flags = app.applicationInfo.flags;
        }
        if ((flags & 1) == 0 && (flags & DumpState.DUMP_PACKAGES) == 0) {
            return hasPermission(app, "android.permission.CONNECTIVITY_INTERNAL");
        }
        return SYSTEM;
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
        update(users, this.mApps, SYSTEM);
        return;
    }

    private synchronized void onUserRemoved(int user) {
        if (user < 0) {
            loge("Invalid user in onUserRemoved: " + user);
            return;
        }
        this.mUsers.remove(Integer.valueOf(user));
        Set<Integer> users = new HashSet();
        users.add(Integer.valueOf(user));
        update(users, this.mApps, NETWORK);
        return;
    }

    private synchronized void onAppAdded(String appName, int appUid) {
        if (TextUtils.isEmpty(appName) || appUid < 0) {
            loge("Invalid app in onAppAdded: " + appName + " | " + appUid);
            return;
        }
        try {
            PackageInfo app = this.mPackageManager.getPackageInfo(appName, DumpState.DUMP_PREFERRED);
            boolean isNetwork = hasNetworkPermission(app);
            boolean isSystem = hasSystemPermission(app);
            if (isNetwork || isSystem) {
                Boolean permission = (Boolean) this.mApps.get(Integer.valueOf(appUid));
                if (permission == null || !permission.booleanValue()) {
                    this.mApps.put(Integer.valueOf(appUid), Boolean.valueOf(isSystem));
                    Map<Integer, Boolean> apps = new HashMap();
                    apps.put(Integer.valueOf(appUid), Boolean.valueOf(isSystem));
                    update(this.mUsers, apps, SYSTEM);
                }
            }
        } catch (NameNotFoundException e) {
            loge("NameNotFoundException in onAppAdded: " + e);
        }
    }

    private synchronized void onAppRemoved(int appUid) {
        if (appUid < 0) {
            loge("Invalid app in onAppRemoved: " + appUid);
            return;
        }
        this.mApps.remove(Integer.valueOf(appUid));
        Map<Integer, Boolean> apps = new HashMap();
        apps.put(Integer.valueOf(appUid), Boolean.valueOf(NETWORK));
        update(this.mUsers, apps, NETWORK);
        return;
    }

    private static void log(String s) {
    }

    private static void loge(String s) {
        Log.e(TAG, s);
    }
}
