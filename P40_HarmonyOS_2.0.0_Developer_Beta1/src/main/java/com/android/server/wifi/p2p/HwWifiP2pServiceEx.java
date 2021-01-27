package com.android.server.wifi.p2p;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Process;
import android.text.TextUtils;
import android.util.wifi.HwHiLog;
import java.util.HashMap;

public class HwWifiP2pServiceEx implements IHwWifiP2pServiceEx {
    private static final int FRAMEWORK_UID = -1;
    private static final String[] REMOVE_GROUP_WHITE_PACKAGE_LIST = new String[0];
    private static final String TAG = "HwWifiP2pServiceEx";
    private static volatile HwWifiP2pServiceEx sHwWifiP2pServiceEx = null;
    private Context mContext;
    private HashMap<String, CreateGroupPackageInfo> mCreateGroupPackageMap = new HashMap<>();

    private HwWifiP2pServiceEx(Context context) {
        this.mContext = context;
    }

    public static HwWifiP2pServiceEx createHwWifiP2pServiceEx(Context context) {
        if (sHwWifiP2pServiceEx == null) {
            synchronized (HwWifiP2pServiceEx.class) {
                if (sHwWifiP2pServiceEx == null) {
                    sHwWifiP2pServiceEx = new HwWifiP2pServiceEx(context);
                }
            }
        }
        return sHwWifiP2pServiceEx;
    }

    public static HwWifiP2pServiceEx getInstance() {
        return sHwWifiP2pServiceEx;
    }

    /* access modifiers changed from: private */
    public enum CreateGroupPriority {
        PRI_HIGHEST(5),
        PRI_HIGH(4),
        PRI_NORMAL(3),
        PRI_LOW(2),
        PRI_LOWEST(1);
        
        private int mPriority;

        private CreateGroupPriority(int priority) {
            this.mPriority = priority;
        }

        public int getValue() {
            return this.mPriority;
        }
    }

    private class CreateGroupPackageInfo {
        private int mPriority;
        private int mUid;

        CreateGroupPackageInfo() {
        }

        public void setUid(int uid) {
            this.mUid = uid;
        }

        public int getUid() {
            return this.mUid;
        }

        public void setPkgPriority(int priority) {
            this.mPriority = priority;
        }

        public int getPkgPriority() {
            return this.mPriority;
        }
    }

    private boolean isInRemoveGroupWhiteList(String packageName) {
        for (String whitePackageName : REMOVE_GROUP_WHITE_PACKAGE_LIST) {
            if (whitePackageName.equals(packageName)) {
                HwHiLog.i(TAG, false, "package=%{public}s is in white list", new Object[]{packageName});
                return true;
            }
        }
        return false;
    }

    private boolean isSignMatchApp(int uid) {
        PackageManager pm = this.mContext.getPackageManager();
        if (pm == null) {
            return false;
        }
        int matchResult = pm.checkSignatures(uid, Process.myUid());
        if (matchResult == 0) {
            return true;
        }
        HwHiLog.d(TAG, false, "isSignMatchApp uid=%{public}d atchRe=%{public}d", new Object[]{Integer.valueOf(uid), Integer.valueOf(matchResult)});
        return false;
    }

    private boolean isSystemApp(int uid, String pkgName) {
        if (uid == 1000 || uid == -1) {
            return true;
        }
        PackageManager pm = this.mContext.getPackageManager();
        if (pm == null || TextUtils.isEmpty(pkgName)) {
            HwHiLog.d(TAG, false, "isSystemApp uid=%{public}d pkgName=%{public}s", new Object[]{Integer.valueOf(uid), pkgName});
            return false;
        }
        try {
            ApplicationInfo info = pm.getApplicationInfo(pkgName, 0);
            if (info != null && (info.flags & 1) != 0) {
                return true;
            }
            HwHiLog.d(TAG, false, "isSystemApp uid=%{public}d pkgName=%{public}s", new Object[]{Integer.valueOf(uid), pkgName});
            return false;
        } catch (PackageManager.NameNotFoundException e) {
            HwHiLog.e(TAG, false, "isSystemApp not found", new Object[0]);
            return false;
        }
    }

    private int getPriorityOfRemoveGroup(int uid, String pkgName) {
        if (isSystemApp(uid, pkgName) || isSignMatchApp(uid) || isInRemoveGroupWhiteList(pkgName)) {
            return CreateGroupPriority.PRI_HIGHEST.getValue();
        }
        return CreateGroupPriority.PRI_LOW.getValue();
    }

    public void updateGroupCreatedPkgList(int uid, String pkgName, boolean isAdded, boolean isExistedKept) {
        if (uid == Integer.MIN_VALUE) {
            this.mCreateGroupPackageMap.clear();
        } else if (TextUtils.isEmpty(pkgName)) {
            HwHiLog.e(TAG, false, "updateGroupCreatedPkgList pkgName is null or empty", new Object[0]);
        } else {
            if (!isExistedKept) {
                this.mCreateGroupPackageMap.clear();
            }
            if (isAdded) {
                HwHiLog.i(TAG, false, "updateGroupCreatedPkgList add uid=%{public}d pkg=%{public}s", new Object[]{Integer.valueOf(uid), pkgName});
                CreateGroupPackageInfo packageInfo = new CreateGroupPackageInfo();
                packageInfo.setUid(uid);
                packageInfo.setPkgPriority(getPriorityOfRemoveGroup(uid, pkgName));
                this.mCreateGroupPackageMap.put(pkgName, packageInfo);
                return;
            }
            this.mCreateGroupPackageMap.remove(pkgName);
        }
    }

    public boolean isRemoveGroupAllowed(int uid, String pkgName) {
        int removeGroupPkgPriority = getPriorityOfRemoveGroup(uid, pkgName);
        for (CreateGroupPackageInfo packageInfo : this.mCreateGroupPackageMap.values()) {
            int priority = packageInfo.getPkgPriority();
            if (priority > removeGroupPkgPriority) {
                HwHiLog.i(TAG, false, "do not allow to remove group because removePriority=%{public}d is low, createPriority=%{public}d", new Object[]{Integer.valueOf(removeGroupPkgPriority), Integer.valueOf(priority)});
                return false;
            }
        }
        HwHiLog.i(TAG, false, "allow to remove group because removePriority=%{public}d is higher", new Object[]{Integer.valueOf(removeGroupPkgPriority)});
        return true;
    }
}
