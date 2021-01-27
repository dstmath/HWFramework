package com.android.server.pm;

import android.content.pm.PackageParser;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import com.android.internal.annotations.GuardedBy;
import com.android.server.pm.Installer;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class HwRenamedPackagePolicyManager {
    private static final boolean IS_DEBUG = SystemProperties.get("ro.dbg.pms_log", "0").equals("on");
    private static final String TAG = "HwRenamedPackagePolicyManager";
    @GuardedBy({"this"})
    private Map<String, HwRenamedPackagePolicy> mRenamedPackagePolicyMap = new HashMap();

    private static class HwRenamedPackagePolicyManagerHolder {
        static HwRenamedPackagePolicyManager renamedPackagePolicyManager = new HwRenamedPackagePolicyManager();

        private HwRenamedPackagePolicyManagerHolder() {
        }
    }

    public static synchronized HwRenamedPackagePolicyManager getInstance() {
        HwRenamedPackagePolicyManager hwRenamedPackagePolicyManager;
        synchronized (HwRenamedPackagePolicyManager.class) {
            hwRenamedPackagePolicyManager = HwRenamedPackagePolicyManagerHolder.renamedPackagePolicyManager;
        }
        return hwRenamedPackagePolicyManager;
    }

    public static Optional<HwRenamedPackagePolicy> generateRenamedPackagePolicyLocked(PackageParser.Package pkg, IHwPackageManagerInner pms) {
        if (pkg == null || pkg.mAppMetaData == null || pms == null) {
            return Optional.empty();
        }
        if (pkg.isSystem() || pkg.isUpdatedSystemApp()) {
            String renamedPolicy = pkg.mAppMetaData.getString("renamed-package-policy");
            if (TextUtils.isEmpty(renamedPolicy)) {
                return Optional.empty();
            }
            HwRenamedPackagePolicy policy = createPolicy(pkg.packageName, renamedPolicy, pms);
            if (policy == null) {
                return Optional.empty();
            }
            PackageSetting originalPs = pms.getSettings().getPackageLPr(policy.getOriginalPackageName());
            if (originalPs == null) {
                Slog.i(TAG, "PACKAGE_NAME_CHANGE no old package name info named for " + pkg.packageName);
                return Optional.of(policy);
            } else if (originalPs.isSystem() || originalPs.isUpdatedSystem()) {
                Slog.i(TAG, "PACKAGE_NAME_CHANGE old package name:" + originalPs.name + " may migrate to " + pkg.packageName);
                if (!(isNeedMigrateDeDataInPolicy(policy) || isNeedMigrateCeDataInPolicy(policy))) {
                    policy.setAppId(0);
                } else if (originalPs.sharedUser == null || originalPs.sharedUser.name.equals(pkg.mSharedUserId)) {
                    policy.setAppId(originalPs.appId);
                } else {
                    Slog.w(TAG, "PACKAGE_NAME_CHANGE unable to migrate data from " + originalPs.name + " to " + pkg.packageName + ": old uid " + originalPs.sharedUser.name + " differs from " + pkg.mSharedUserId);
                    return Optional.empty();
                }
                return Optional.of(policy);
            } else {
                Slog.w(TAG, originalPs.name + " is not a system app,can not generate renamed package policy!");
                return Optional.empty();
            }
        } else {
            Slog.w(TAG, pkg.packageName + " is not a system app,can not generate renamed package policy!");
            return Optional.empty();
        }
    }

    private static HwRenamedPackagePolicy createPolicy(String newPackageName, String strPolicy, IHwPackageManagerInner pms) {
        if (TextUtils.isEmpty(strPolicy)) {
            Log.i(TAG, newPackageName + " declared invalid renamed-package-policy with empty string");
            return null;
        }
        HwRenamedPackagePolicy policy = new HwRenamedPackagePolicy(newPackageName, strPolicy);
        if (TextUtils.isEmpty(policy.getOriginalPackageName())) {
            Log.i(TAG, newPackageName + " declared invalid original-package-name");
            return null;
        } else if (policy.getPolicyFlags() == 0) {
            Log.i(TAG, newPackageName + " declared invalid renamed-package-policy");
            return null;
        } else {
            Log.i(TAG, newPackageName + ", old package name:" + policy.getOriginalPackageName() + ", policyFlags=" + Integer.toHexString(policy.getPolicyFlags()));
            return policy;
        }
    }

    public boolean addRenamedPackagePolicy(HwRenamedPackagePolicy renamedPackagePolicy) {
        if (renamedPackagePolicy == null || TextUtils.isEmpty(renamedPackagePolicy.getNewPackageName()) || TextUtils.isEmpty(renamedPackagePolicy.getOriginalPackageName())) {
            return false;
        }
        synchronized (this) {
            this.mRenamedPackagePolicyMap.put(renamedPackagePolicy.getOriginalPackageName(), renamedPackagePolicy);
        }
        return true;
    }

    public List<HwRenamedPackagePolicy> getRenamedPackagePolicy(int flags) {
        List<HwRenamedPackagePolicy> renamedPackagePolicyList = new ArrayList<>();
        synchronized (this) {
            for (String packageName : this.mRenamedPackagePolicyMap.keySet()) {
                HwRenamedPackagePolicy renamedPackagePolicy = this.mRenamedPackagePolicyMap.get(packageName);
                if (flags == 0 || renamedPackagePolicy.checkFlags(flags)) {
                    renamedPackagePolicyList.add(renamedPackagePolicy);
                }
            }
        }
        return renamedPackagePolicyList;
    }

    public Optional<HwRenamedPackagePolicy> getRenamedPackagePolicyByOriginalName(String originalPackageName) {
        Optional<HwRenamedPackagePolicy> ofNullable;
        if (TextUtils.isEmpty(originalPackageName)) {
            return Optional.empty();
        }
        synchronized (this) {
            HwRenamedPackagePolicy renamedPackagePolicy = null;
            if (this.mRenamedPackagePolicyMap.containsKey(originalPackageName)) {
                renamedPackagePolicy = this.mRenamedPackagePolicyMap.get(originalPackageName);
            }
            ofNullable = Optional.ofNullable(renamedPackagePolicy);
        }
        return ofNullable;
    }

    public Optional<HwRenamedPackagePolicy> getRenamedPackagePolicyByNewPackageName(String newPackageName) {
        Optional<HwRenamedPackagePolicy> ofNullable;
        if (TextUtils.isEmpty(newPackageName)) {
            return Optional.empty();
        }
        synchronized (this) {
            HwRenamedPackagePolicy renamedPackagePolicy = null;
            Iterator<HwRenamedPackagePolicy> it = this.mRenamedPackagePolicyMap.values().iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                HwRenamedPackagePolicy policy = it.next();
                if (policy.getNewPackageName().equals(newPackageName)) {
                    renamedPackagePolicy = policy;
                    break;
                }
            }
            ofNullable = Optional.ofNullable(renamedPackagePolicy);
        }
        return ofNullable;
    }

    @GuardedBy({"PackagesLock"})
    public boolean migrateDataForRenamedPackageLocked(PackageParser.Package pkg, int userId, int flags, IHwPackageManagerInner pms) {
        boolean isNeedMigrateData;
        if (pkg == null || pms == null) {
            return true;
        }
        if (TextUtils.isEmpty(pkg.packageName)) {
            return true;
        }
        Optional<HwRenamedPackagePolicy> renamedPackagePolicyOptional = getRenamedPackagePolicyByOriginalName(pkg.packageName);
        boolean isNeedMigrateData2 = false;
        if (renamedPackagePolicyOptional.isPresent()) {
            Slog.w(TAG, "renamed migrate data check, package name: " + pkg.packageName + " is an old package and rename to " + renamedPackagePolicyOptional.get().getNewPackageName());
            return false;
        }
        Optional<HwRenamedPackagePolicy> policyOptional = getRenamedPackagePolicyByNewPackageName(pkg.packageName);
        if (!policyOptional.isPresent()) {
            return true;
        }
        HwRenamedPackagePolicy policy = policyOptional.get();
        String oldPkgName = policy.getOriginalPackageName();
        PackageSetting originalPkgSetting = pms.getSettings().getPackageLPr(oldPkgName);
        if (originalPkgSetting == null) {
            Slog.i(TAG, pkg.packageName + "originalPkgSetting is null,do not need to transfer data.");
            return true;
        }
        Slog.w(TAG, "migrateData from:" + oldPkgName + " to: " + pkg.packageName + ",flags:" + flags);
        boolean isNeedMigrateDe = isNeedMigrateDeDataInPolicy(policy) && isCanMigrateDeDataInFlags(flags);
        boolean isNeedMigrateCe = isNeedMigrateCeDataInPolicy(policy) && isCanMigrateCeDataInFlags(flags);
        if (isNeedMigrateDe || isNeedMigrateCe) {
            isNeedMigrateData2 = true;
        }
        if (isNeedMigrateData2) {
            isNeedMigrateData = isNeedMigrateData2;
            transferDataLocked(pkg, userId, flags, originalPkgSetting, pms);
        } else {
            isNeedMigrateData = isNeedMigrateData2;
        }
        if ((flags & 2) == 0) {
            return true;
        }
        clearOldPkgData(originalPkgSetting, policy, isNeedMigrateData, pms);
        return true;
    }

    private static boolean isNeedMigrateDeDataInPolicy(HwRenamedPackagePolicy policy) {
        return policy.checkFlags(1);
    }

    private static boolean isNeedMigrateCeDataInPolicy(HwRenamedPackagePolicy policy) {
        return policy.checkFlags(2);
    }

    private static boolean isCanMigrateDeDataInFlags(int flags) {
        return (flags & 1) != 0;
    }

    private static boolean isCanMigrateCeDataInFlags(int flags) {
        return (flags & 2) != 0;
    }

    private void clearOldPkgData(PackageSetting originalPkgSetting, HwRenamedPackagePolicy policy, boolean isTransferData, IHwPackageManagerInner pms) {
        boolean isExclusiveInstall = policy.checkFlags(4);
        if (isTransferData || isExclusiveInstall) {
            synchronized (pms.getPackagesLock()) {
                removeOldPackageDataCodePath(originalPkgSetting, pms);
                clearOldPkgSetting(originalPkgSetting.name, pms);
            }
        }
    }

    @GuardedBy({"PackagesLock"})
    private void clearOldPkgSetting(String originalPkgName, IHwPackageManagerInner pms) {
        Slog.i(TAG, "PACKAGE_NAME_CHANGE Ce data migrate complete, remove PackageSetting for " + originalPkgName);
        pms.getPackagesLock().remove(originalPkgName);
        pms.getSettings().removePackageLPw(originalPkgName);
        pms.scheduleWriteSettingsInner();
    }

    @GuardedBy({"PackagesLock"})
    private void removeOldPackageDataCodePath(PackageSetting originalPkgSetting, IHwPackageManagerInner pms) {
        if (originalPkgSetting.getPackage() != null) {
            String codePath = originalPkgSetting.getPackage().codePath;
            if (!TextUtils.isEmpty(codePath) && codePath.startsWith("/data/")) {
                Slog.w(TAG, "delete old pkg:" + originalPkgSetting.name + " in data code path.");
                try {
                    File codePathFile = new File(codePath.trim()).getCanonicalFile();
                    if (codePathFile.exists()) {
                        if (!codePathFile.isDirectory() || pms.getInstallerInner() == null) {
                            codePathFile.delete();
                            return;
                        }
                        try {
                            pms.getInstallerInner().rmPackageDir(codePathFile.getCanonicalPath());
                        } catch (Installer.InstallerException e) {
                            Slog.w(TAG, "Failed to remove code path");
                        }
                    }
                } catch (IOException e2) {
                    Slog.i(TAG, "delete old pkg code path error!");
                }
            }
        }
    }

    @GuardedBy({"PackagesLock"})
    private void transferDataLocked(PackageParser.Package pkg, int userId, int flags, PackageSetting originalPkgSetting, IHwPackageManagerInner pms) {
        if (!pkg.isSystem() && !pkg.isUpdatedSystemApp()) {
            Slog.w(TAG, pkg.packageName + " is not a system app,can not migrate data!");
        } else if (originalPkgSetting.getPackage() == null || originalPkgSetting.getPackage().isSystem() || originalPkgSetting.getPackage().isUpdatedSystemApp()) {
            synchronized (pms.getPackagesLock()) {
                PackageManagerServiceUtils.logCriticalInfo(5, "PACKAGE_NAME_CHANGE transfering data from old package " + originalPkgSetting.name + " to new package " + pkg.packageName);
                if (pms.getInstallerInner() != null) {
                    try {
                        Installer.RenamePackageParam renamePackageParam = new Installer.RenamePackageParam();
                        renamePackageParam.setUuid(pkg.volumeUuid);
                        renamePackageParam.setOrgiPackageName(originalPkgSetting.name);
                        renamePackageParam.setNewPackageName(pkg.packageName);
                        renamePackageParam.setUserId(userId);
                        renamePackageParam.setFlags(flags);
                        renamePackageParam.setAppId(originalPkgSetting.appId);
                        renamePackageParam.setSeInfo(pkg.applicationInfo.seInfo);
                        renamePackageParam.setTargetSdkVersion(pkg.applicationInfo.targetSdkVersion);
                        pms.getInstallerInner().renamePackageData(renamePackageParam);
                    } catch (Installer.InstallerException e) {
                        PackageManagerServiceUtils.logCriticalInfo(5, "Error transfering data from old package " + originalPkgSetting.name + " to new package " + pkg.packageName);
                    }
                }
            }
        } else {
            Slog.w(TAG, originalPkgSetting.name + " is not a system app,can not migrate data!");
        }
    }
}
