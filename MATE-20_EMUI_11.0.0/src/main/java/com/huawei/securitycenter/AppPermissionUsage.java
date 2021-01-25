package com.huawei.securitycenter;

import android.app.AppOpsManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.ArraySet;
import java.util.ArrayList;
import java.util.List;

public class AppPermissionUsage {
    private static final int DEFAULT_PERM_SIZE = 16;
    private static final String KEY_ALLOW_BACKGROUND_LIST = "allow_background_access_count_list";
    private static final String KEY_ALLOW_FOREGROUND_LIST = "allow_foreground_access_count_list";
    private static final String KEY_LAST_ACCESS_TIME_ARRAY = "last_access_time_array";
    private static final String KEY_PACKAGE_NAME = "packageName";
    private static final String KEY_PERMISSION_NAME_LIST = "permission_name_list";
    private static final String KEY_REJECT_BACKGROUND_LIST = "reject_background_access_count_list";
    private static final String KEY_REJECT_FOREGROUND_LIST = "reject_foreground_access_count_list";
    private static final String TAG = "AppPermissionUsage";
    private final AppOpsManager.HistoricalPackageOps mHistoricalUsage;
    private final AppOpsManager.PackageOps mLastUsage;
    private final String mPackageName;
    private List<PermissionUsage> mPermUsageList = new ArrayList(16);
    private final ArraySet<String> mPermissonNameSet;
    private int mUid;

    public AppPermissionUsage(String packageName, int appUid, ArraySet<String> permSet, AppOpsManager.PackageOps packageOps, AppOpsManager.HistoricalPackageOps historicalPackageOps) {
        this.mPackageName = packageName;
        this.mUid = appUid;
        this.mLastUsage = packageOps;
        this.mHistoricalUsage = historicalPackageOps;
        this.mPermissonNameSet = permSet;
        createPermissionUsages();
    }

    public Bundle getPermissionUsageForApp() {
        if (this.mPermUsageList.isEmpty()) {
            return null;
        }
        int size = this.mPermUsageList.size();
        ArrayList<String> permNameList = new ArrayList<>(size);
        ArrayList<Integer> allowForegroundAccessCountList = new ArrayList<>(size);
        ArrayList<Integer> rejectForegroundAccessCountList = new ArrayList<>(size);
        ArrayList<Integer> allowBackgroundAccessCountList = new ArrayList<>(size);
        ArrayList<Integer> rejectBackgroundAccessCountList = new ArrayList<>(size);
        long[] lastAccessTimeArray = new long[size];
        for (int i = 0; i < size; i++) {
            PermissionUsage permissionUsage = this.mPermUsageList.get(i);
            if (permissionUsage != null) {
                permNameList.add(permissionUsage.getPermissionName());
                allowBackgroundAccessCountList.add(new Integer((int) permissionUsage.getBackgroundAccessAllowedCount()));
                allowForegroundAccessCountList.add(new Integer((int) permissionUsage.getForegroundAccessAllowedCount()));
                rejectBackgroundAccessCountList.add(new Integer((int) permissionUsage.getBackgroundRejectCount()));
                rejectForegroundAccessCountList.add(new Integer((int) permissionUsage.getForegroundRejectCount()));
                lastAccessTimeArray[i] = permissionUsage.getLastAccessTime();
            }
        }
        Bundle bundle = new Bundle();
        bundle.putString(KEY_PACKAGE_NAME, this.mPackageName);
        bundle.putStringArrayList(KEY_PERMISSION_NAME_LIST, permNameList);
        bundle.putIntegerArrayList(KEY_ALLOW_BACKGROUND_LIST, allowBackgroundAccessCountList);
        bundle.putIntegerArrayList(KEY_ALLOW_FOREGROUND_LIST, allowForegroundAccessCountList);
        bundle.putIntegerArrayList(KEY_REJECT_BACKGROUND_LIST, rejectBackgroundAccessCountList);
        bundle.putIntegerArrayList(KEY_REJECT_FOREGROUND_LIST, rejectForegroundAccessCountList);
        bundle.putLongArray(KEY_LAST_ACCESS_TIME_ARRAY, lastAccessTimeArray);
        return bundle;
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public int getUid() {
        return this.mUid;
    }

    private void createPermissionUsages() {
        ArraySet<String> arraySet;
        if (!(this.mHistoricalUsage == null || (arraySet = this.mPermissonNameSet) == null || arraySet.isEmpty())) {
            int permSize = this.mPermissonNameSet.size();
            for (int i = 0; i < permSize; i++) {
                String permName = this.mPermissonNameSet.valueAt(i);
                if (!TextUtils.isEmpty(permName)) {
                    this.mPermUsageList.add(new PermissionUsage(permName, this.mLastUsage, this.mHistoricalUsage));
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public static class PermissionUsage {
        private final AppOpsManager.HistoricalPackageOps mHistoricalUsage;
        private final AppOpsManager.PackageOps mLastUsage;
        private final String mPermissionName;

        PermissionUsage(String permissionName, AppOpsManager.PackageOps packageOps, AppOpsManager.HistoricalPackageOps historicalPackageOps) {
            this.mPermissionName = permissionName;
            this.mLastUsage = packageOps;
            this.mHistoricalUsage = historicalPackageOps;
        }

        public long getLastAccessTime() {
            if (this.mLastUsage == null) {
                return 0;
            }
            String opName = AppOpsManager.permissionToOp(this.mPermissionName);
            if (TextUtils.isEmpty(opName)) {
                return 0;
            }
            List<AppOpsManager.OpEntry> ops = this.mLastUsage.getOps();
            int opCount = ops.size();
            long lastAccess = 0;
            for (int opNum = 0; opNum < opCount; opNum++) {
                AppOpsManager.OpEntry op = ops.get(opNum);
                if (op.getOpStr().equals(opName)) {
                    long lastAccessTime = op.getLastAccessTime(13);
                    lastAccess = lastAccessTime >= 0 ? lastAccessTime : 0;
                }
            }
            return lastAccess;
        }

        public long getForegroundAccessCount() {
            AppOpsManager.HistoricalOp historicalOp;
            if (this.mHistoricalUsage == null) {
                return 0;
            }
            String opName = AppOpsManager.permissionToOp(this.mPermissionName);
            if (!TextUtils.isEmpty(opName) && (historicalOp = this.mHistoricalUsage.getOp(opName)) != null) {
                return historicalOp.getForegroundAccessCount(13);
            }
            return 0;
        }

        public long getForegroundAccessAllowedCount() {
            if (this.mHistoricalUsage == null) {
                return 0;
            }
            long totalForegroundAccess = getForegroundAccessCount();
            if (totalForegroundAccess <= 0) {
                return 0;
            }
            long foregroundRejectAccess = getForegroundRejectCount();
            if (foregroundRejectAccess <= 0) {
                return totalForegroundAccess;
            }
            return totalForegroundAccess - foregroundRejectAccess;
        }

        public long getForegroundRejectCount() {
            AppOpsManager.HistoricalOp historicalOp;
            if (this.mHistoricalUsage == null) {
                return 0;
            }
            String opName = AppOpsManager.permissionToOp(this.mPermissionName);
            if (!TextUtils.isEmpty(opName) && (historicalOp = this.mHistoricalUsage.getOp(opName)) != null) {
                return historicalOp.getForegroundRejectCount(13);
            }
            return 0;
        }

        public long getBackgroundAccessAllowedCount() {
            if (this.mHistoricalUsage == null) {
                return 0;
            }
            long totalBackgroundAccess = getBackgroundAccessCount();
            if (totalBackgroundAccess <= 0) {
                return 0;
            }
            long backgroundRejectAccess = getBackgroundRejectCount();
            if (backgroundRejectAccess <= 0) {
                return totalBackgroundAccess;
            }
            return totalBackgroundAccess - backgroundRejectAccess;
        }

        public long getBackgroundRejectCount() {
            AppOpsManager.HistoricalOp historicalOp;
            if (this.mHistoricalUsage == null) {
                return 0;
            }
            String opName = AppOpsManager.permissionToOp(this.mPermissionName);
            if (!TextUtils.isEmpty(opName) && (historicalOp = this.mHistoricalUsage.getOp(opName)) != null) {
                return historicalOp.getBackgroundRejectCount(13);
            }
            return 0;
        }

        public long getBackgroundAccessCount() {
            AppOpsManager.HistoricalOp historicalOp;
            if (this.mHistoricalUsage == null) {
                return 0;
            }
            String opName = AppOpsManager.permissionToOp(this.mPermissionName);
            if (!TextUtils.isEmpty(opName) && (historicalOp = this.mHistoricalUsage.getOp(opName)) != null) {
                return historicalOp.getBackgroundAccessCount(13);
            }
            return 0;
        }

        public long getAccessCount() {
            AppOpsManager.HistoricalOp historicalOp;
            if (this.mHistoricalUsage == null) {
                return 0;
            }
            String opName = AppOpsManager.permissionToOp(this.mPermissionName);
            if (!TextUtils.isEmpty(opName) && (historicalOp = this.mHistoricalUsage.getOp(opName)) != null) {
                return historicalOp.getForegroundAccessCount(13) + historicalOp.getBackgroundAccessCount(13);
            }
            return 0;
        }

        public String getPermissionName() {
            return this.mPermissionName;
        }
    }

    public static class Builder {
        private AppOpsManager.HistoricalPackageOps mHistoricalUsage;
        private AppOpsManager.PackageOps mLastUsage;
        private final String mPackageName;
        private ArraySet<String> mPermNameSet = new ArraySet<>(16);
        private int mUid;

        public Builder(String packageName, int uid) {
            this.mPackageName = packageName;
            this.mUid = uid;
        }

        public void addPermissionName(String permissionName) {
            if (!TextUtils.isEmpty(permissionName)) {
                this.mPermNameSet.add(permissionName);
            }
        }

        public void addPermissionNames(ArraySet<String> permNameSet) {
            if (permNameSet != null && !permNameSet.isEmpty()) {
                this.mPermNameSet.addAll((ArraySet<? extends String>) permNameSet);
            }
        }

        public String getPackageName() {
            return this.mPackageName;
        }

        public int getUid() {
            return this.mUid;
        }

        public void setHistoricalUsage(AppOpsManager.HistoricalPackageOps historicalUsage) {
            this.mHistoricalUsage = historicalUsage;
        }

        public void setLastUsage(AppOpsManager.PackageOps lastUsage) {
            this.mLastUsage = lastUsage;
        }

        public AppPermissionUsage build() {
            if (this.mPermNameSet.isEmpty()) {
                return null;
            }
            return new AppPermissionUsage(this.mPackageName, this.mUid, this.mPermNameSet, this.mLastUsage, this.mHistoricalUsage);
        }
    }
}
