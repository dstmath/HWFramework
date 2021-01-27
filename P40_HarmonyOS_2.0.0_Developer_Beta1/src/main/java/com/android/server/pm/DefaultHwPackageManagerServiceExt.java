package com.android.server.pm;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.FeatureInfo;
import android.content.pm.IPackageInstallObserver2;
import android.content.pm.PackageInfo;
import android.content.pm.PackageParser;
import android.content.pm.PackageParserEx;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.os.Bundle;
import android.os.IBackupSessionCallback;
import android.os.ParcelFileDescriptor;
import android.os.UserHandle;
import android.util.ArrayMap;
import com.android.internal.annotations.GuardedBy;
import com.android.server.pm.CompilerStats;
import com.android.server.pm.CompilerStatsEx;
import com.android.server.pm.PackageManagerService;
import com.android.server.pm.PackageManagerServiceEx;
import com.android.server.pm.dex.DexoptOptions;
import com.android.server.pm.dex.DexoptOptionsEx;
import com.android.server.pm.dex.PackageDexUsage;
import com.android.server.pm.dex.PackageDexUsageEx;
import com.huawei.android.content.pm.HwHepPackageInfo;
import com.huawei.android.content.pm.HwPresetPackage;
import com.huawei.android.content.pm.IExtServiceProvider;
import com.huawei.android.content.pm.IExtServiceProviderEx;
import com.huawei.android.content.pm.IPackageInstallObserver2Ex;
import com.huawei.android.content.pm.ResolveInfoEx;
import com.huawei.android.content.pm.SignatureEx;
import com.huawei.android.os.UserHandleEx;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DefaultHwPackageManagerServiceExt implements IHwPackageManagerServiceEx, IHwPackageManagerServiceExInner {
    public boolean isPerfOptEnable(String packageName, int optType) {
        return false;
    }

    public void checkHwCertification(PackageParser.Package pkg, boolean isUpdate) {
        checkHwCertification(new PackageParserEx.PackageEx(pkg), isUpdate);
    }

    public void checkHwCertification(PackageParserEx.PackageEx pkg, boolean isUpdate) {
    }

    public void cleanUpHwCert() {
    }

    public boolean getHwCertPermission(boolean isAllowed, PackageParser.Package pkg, String perm) {
        return getHwCertPermission(isAllowed, new PackageParserEx.PackageEx(pkg), perm);
    }

    public boolean getHwCertPermission(boolean isAllowed, PackageParserEx.PackageEx pkg, String perm) {
        return false;
    }

    public void initHwCertificationManager() {
    }

    public boolean isAllowedSetHomeActivityForAntiMal(PackageInfo pi, int userId) {
        return false;
    }

    public void updateNotchScreenWhite(String packageName, String flag, int versionCode) {
    }

    public int getAppUseNotchMode(String packageName) {
        return -1;
    }

    public void setAppUseNotchMode(String packageName, int mode) {
    }

    public boolean isApkDexOpt(String targetCompilerFilter) {
        return false;
    }

    public boolean hwPerformDexOptMode(String packageName, boolean isCheckProfiles, boolean isForce, boolean isBootCompleted, String splitName) {
        return false;
    }

    public void setAppCanUninstall(String packageName, boolean isCanUninstall) {
    }

    public boolean isAllowUninstallApp(String packageName) {
        return false;
    }

    public void installPackageAsUser(String originPath, IPackageInstallObserver2 observer, int installFlags, String installerPackageName, int userId) {
        IPackageInstallObserver2Ex observer2 = new IPackageInstallObserver2Ex();
        observer2.setPackageInstallObserver(observer);
        installPackageAsUser(originPath, observer2, installFlags, installerPackageName, userId);
    }

    public void installPackageAsUser(String originPath, IPackageInstallObserver2Ex observer, int installFlags, String installerPackageName, int userId) {
    }

    public boolean isSystemPreApp(File scanFile) {
        return false;
    }

    public boolean isPrivilegedPreApp(File scanFile) {
        return false;
    }

    public void readPersistentConfig() {
    }

    public void resolvePersistentFlagForPackage(int oldFlags, PackageParser.Package pkg) {
        resolvePersistentFlagForPackage(oldFlags, new PackageParserEx.PackageEx(pkg));
    }

    public void resolvePersistentFlagForPackage(int oldFlags, PackageParserEx.PackageEx pkg) {
    }

    public boolean isPersistentUpdatable(PackageParser.Package pkg) {
        return isPersistentUpdatable(new PackageParserEx.PackageEx(pkg));
    }

    public boolean isPersistentUpdatable(PackageParserEx.PackageEx pkg) {
        return false;
    }

    public void systemReady() {
    }

    public boolean isMDMDisallowedInstallPackage(PackageParser.Package pkg, PackageManagerService.PackageInstalledInfo res) {
        return isMDMDisallowedInstallPackage(new PackageParserEx.PackageEx(pkg), new PackageManagerServiceEx.PackageInstalledInfoEx());
    }

    public boolean isMDMDisallowedInstallPackage(PackageParserEx.PackageEx pkg, PackageManagerServiceEx.PackageInstalledInfoEx res) {
        return false;
    }

    public ResolveInfo hwFindPreferredActivity(Intent intent, List<ResolveInfo> query) {
        List<ResolveInfoEx> resolveInfoExList = new ArrayList<>();
        for (ResolveInfo info : query) {
            ResolveInfoEx infoEx = new ResolveInfoEx();
            infoEx.setResolveInfo(info);
            resolveInfoExList.add(infoEx);
        }
        ResolveInfoEx resolveInfoEx = hwFindPreferredActivityEx(intent, resolveInfoExList);
        if (resolveInfoEx == null) {
            return null;
        }
        return resolveInfoEx.getResolveInfo();
    }

    public ResolveInfoEx hwFindPreferredActivityEx(Intent intent, List<ResolveInfoEx> list) {
        return new ResolveInfoEx();
    }

    public int getStartBackupSession(IBackupSessionCallback callback) {
        return -1;
    }

    public int getExecuteBackupTask(int sessionId, String taskCmd) {
        return -1;
    }

    public int getFinishBackupSession(int sessionId) {
        return -1;
    }

    public void getAPKInstallListForHwPMS(List<File> list, HashMap<String, HashSet<String>> hashMap) {
    }

    public void installAPKforInstallListForHwPMS(HashSet<String> hashSet, int flags, int scanMode, long currentTime) {
    }

    public void installAPKforInstallListForHwPMS(HashSet<String> hashSet, int parseFlags, int scanFlags, long currentTime, int hwFlags) {
    }

    public boolean isDelappInData(PackageSetting ps) {
        PackageSettingEx settingEx = new PackageSettingEx();
        settingEx.setPackageSetting(ps);
        return isDelappInData(settingEx);
    }

    public boolean isDelappInData(PackageSettingEx ps) {
        return false;
    }

    public boolean isUninstallApk(String filePath) {
        return false;
    }

    public void getUninstallApk() {
    }

    public boolean isHwCustHiddenInfoPackage(PackageParser.Package pkgInfo) {
        if (pkgInfo == null) {
            return false;
        }
        return isHwCustHiddenInfoPackage(new PackageParserEx.PackageEx(pkgInfo));
    }

    public boolean isHwCustHiddenInfoPackage(PackageParserEx.PackageEx pkgInfo) {
        return false;
    }

    public void setUpCustomResolverActivity(PackageParser.Package pkg) {
        setUpCustomResolverActivity(new PackageParserEx.PackageEx(pkg));
    }

    public void setUpCustomResolverActivity(PackageParserEx.PackageEx pkg) {
    }

    public void replaceSignatureIfNeeded(PackageSetting ps, PackageParser.Package pkg, boolean isBootScan, boolean isUpdate) {
        PackageSettingEx settingEx = new PackageSettingEx();
        settingEx.setPackageSetting(ps);
        replaceSignatureIfNeeded(settingEx, new PackageParserEx.PackageEx(pkg), isBootScan, isUpdate);
    }

    public void replaceSignatureIfNeeded(PackageSettingEx ps, PackageParserEx.PackageEx pkg, boolean isBootScan, boolean isUpdate) {
    }

    public void initCertCompatSettings() {
    }

    public void writeCertCompatPackages(boolean isUpdate) {
    }

    public void updateCertCompatPackage(PackageParser.Package pkg, PackageSetting ps) {
        PackageSettingEx settingEx = new PackageSettingEx();
        settingEx.setPackageSetting(ps);
        updateCertCompatPackage(new PackageParserEx.PackageEx(pkg), settingEx);
    }

    public void updateCertCompatPackage(PackageParserEx.PackageEx pkg, PackageSettingEx ps) {
    }

    public boolean isSystemSignatureUpdated(Signature[] previous, Signature[] current) {
        return isSystemSignatureUpdatedEx(toSignatureEx(previous), toSignatureEx(current));
    }

    private SignatureEx[] toSignatureEx(Signature[] signatures) {
        SignatureEx[] signatureExes = new SignatureEx[signatures.length];
        for (int i = 0; i < signatures.length; i++) {
            signatureExes[i] = new SignatureEx();
            signatureExes[i].setSignature(signatures[i]);
        }
        return signatureExes;
    }

    public boolean isSystemSignatureUpdatedEx(SignatureEx[] previous, SignatureEx[] current) {
        return false;
    }

    public void sendIncompatibleNotificationIfNeeded(String packageName) {
    }

    public void recordInstallAppInfo(String pkgName, long beginTime, int installFlags) {
    }

    public void onNewUserCreated(int userId) {
    }

    public void updatePackageBlackListInfo(String packageName) {
    }

    public boolean setApplicationAspectRatio(String packageName, String aspectName, float ar) {
        return false;
    }

    public float getApplicationAspectRatio(String packageName, String aspectName) {
        return 0.0f;
    }

    public void addPreinstalledPkgToList(PackageParser.Package scannedPkg) {
        addPreinstalledPkgToList(new PackageParserEx.PackageEx(scannedPkg));
    }

    public void addPreinstalledPkgToList(PackageParserEx.PackageEx scannedPkg) {
    }

    public List<String> getPreinstalledApkList() {
        return new ArrayList();
    }

    public void writePreinstalledApkListToFile() {
    }

    public void createPublicityFile() {
    }

    public List<String> getHwPublicityAppList() {
        return new ArrayList();
    }

    public ParcelFileDescriptor getHwPublicityAppParcelFileDescriptor() {
        return null;
    }

    public void scanRemovableAppDir(int scanMode) {
    }

    public boolean needInstallRemovablePreApk(PackageParser.Package pkg, int hwFlags) {
        return needInstallRemovablePreApk(new PackageParserEx.PackageEx(pkg), hwFlags);
    }

    public boolean needInstallRemovablePreApk(PackageParserEx.PackageEx pkg, int hwFlags) {
        return false;
    }

    public boolean isDelapp(PackageSetting ps) {
        PackageSettingEx settingEx = new PackageSettingEx();
        settingEx.setPackageSetting(ps);
        return isDelapp(settingEx);
    }

    public boolean isDelapp(PackageSettingEx ps) {
        return false;
    }

    public void addFlagsForRemovablePreApk(PackageParser.Package pkg, int hwFlags) {
        addFlagsForRemovablePreApk(new PackageParserEx.PackageEx(pkg), hwFlags);
    }

    public void addFlagsForRemovablePreApk(PackageParserEx.PackageEx pkg, int hwFlags) {
    }

    public boolean isDisallowUninstallApk(String packageName) {
        return false;
    }

    public boolean isInMultiWinWhiteList(String packageName) {
        return false;
    }

    public boolean isInMWPortraitWhiteList(String packageName) {
        return false;
    }

    public String getResourcePackageNameByIcon(String pkgName, int icon, int userId) {
        return "";
    }

    public void doPostScanInstall(PackageParser.Package pkg, UserHandle user, boolean isNewInstall, int hwFlags, PackageParser.Package scannedPkg) {
        UserHandleEx user1 = new UserHandleEx();
        user1.setUserHandle(user);
        doPostScanInstall(new PackageParserEx.PackageEx(pkg), user1, isNewInstall, hwFlags, new PackageParserEx.PackageEx(scannedPkg));
    }

    public void doPostScanInstall(PackageParserEx.PackageEx pkg, UserHandleEx user, boolean isNewInstall, int hwFlags, PackageParserEx.PackageEx scannedPkg) {
    }

    public void recordUninstalledDelapp(String packageName, String path) {
    }

    public List<String> getScanInstallList() {
        return new ArrayList();
    }

    public boolean scanInstallApk(String apkFile) {
        return false;
    }

    @Override // com.android.server.pm.IHwPackageManagerServiceExInner
    public boolean scanInstallApk(String packageName, String apkFile, int userId) {
        return false;
    }

    @Override // com.android.server.pm.IHwPackageManagerServiceExInner
    public void installAPKforInstallList(HashSet<String> hashSet, int parseFlags, int scanFlags, long currentTime, int hwFlags) {
    }

    public void readPreInstallApkList() {
    }

    public List<String> getOldDataBackup() {
        return new ArrayList();
    }

    public boolean isPreRemovableApp(String codePath) {
        return false;
    }

    public void parseInstalledPkgInfo(String pkgUri, String pkgName, String pkgVerName, int pkgVerCode, int resultCode, boolean isPkgUpdate) {
    }

    public void addUpdatedRemoveableAppFlag(String scanFileString, String packageName) {
    }

    @Override // com.android.server.pm.IHwPackageManagerServiceExInner
    public boolean containDelPath(String sensePath) {
        return false;
    }

    public boolean needAddUpdatedRemoveableAppFlag(String packageName) {
        return false;
    }

    public boolean isUnAppInstallAllowed(String originPath) {
        return false;
    }

    public void scanNonSystemPartitionDir(int scanMode) {
    }

    public void scanNoSysAppInNonSystemPartitionDir(int scanMode) {
    }

    public void setHdbKey(String key) {
    }

    public void loadCorrectUninstallDelapp() {
    }

    public void addUninstallDataToCache(String packageName, String codePath) {
    }

    public boolean checkUninstalledSystemApp(PackageParser.Package pkg, PackageManagerService.InstallArgs args, PackageManagerService.PackageInstalledInfo res) throws PackageManagerException {
        PackageManagerServiceEx.InstallArgsEx argsEx = new PackageManagerServiceEx.InstallArgsEx();
        argsEx.setInstallArgs(args);
        PackageManagerServiceEx.PackageInstalledInfoEx infoEx = new PackageManagerServiceEx.PackageInstalledInfoEx();
        infoEx.setPackageInstalledInfo(res);
        try {
            return checkUninstalledSystemApp(new PackageParserEx.PackageEx(pkg), argsEx, infoEx);
        } catch (PackageManagerExceptionEx exceptionEx) {
            throw exceptionEx.getManagerException();
        }
    }

    public boolean checkUninstalledSystemApp(PackageParserEx.PackageEx pkg, PackageManagerServiceEx.InstallArgsEx args, PackageManagerServiceEx.PackageInstalledInfoEx res) throws PackageManagerExceptionEx {
        return false;
    }

    public boolean isPrivAppNonSystemPartitionDir(File path) {
        return false;
    }

    public boolean verifyPackageSecurityPolicy(String packageName, File path) {
        return false;
    }

    public boolean pmInstallHwTheme(String themePath, boolean isSetWallpaper, int userId) {
        return false;
    }

    public void onUserRemoved(int userId) {
    }

    public void reportEventStream(int eventId, String message) {
    }

    public ArrayList<String> getDataApkShouldNotUpdateByCota() {
        return new ArrayList<>();
    }

    public void deleteExistsIfNeeded() {
    }

    public boolean isAppInstallAllowed(String appName) {
        return false;
    }

    public String readMspesFile(String fileName) {
        return "";
    }

    public boolean writeMspesFile(String fileName, String content) {
        return false;
    }

    public String getMspesOEMConfig() {
        return "";
    }

    public int updateMspesOEMConfig(String src) {
        return -1;
    }

    public boolean isInMspesForbidInstallPackageList(String pkg) {
        return false;
    }

    public void preSendPackageBroadcast(String action, String pkg, String targetPkg) {
    }

    public boolean setAllAppsUseSideMode(boolean isUse) {
        return false;
    }

    public boolean setAllAppsUseSideModeAndStopApps(List<String> list, boolean isUse) {
        return false;
    }

    public boolean restoreAllAppsUseSideMode() {
        return false;
    }

    public boolean isAllAppsUseSideMode(List<String> list) {
        return false;
    }

    public int getAppUseSideMode(String packageName) {
        return -1;
    }

    public void setAppUseSideMode(String packageName, int mode) {
    }

    public void updateAppsUseSideWhitelist(ArrayMap<String, String> arrayMap, ArrayMap<String, String> arrayMap2) {
    }

    public List<String> getAppsUseSideList() {
        return new ArrayList();
    }

    public void updateUseSideMode(String pkgName, PackageSetting ps) {
        PackageSettingEx settingEx = new PackageSettingEx();
        settingEx.setPackageSetting(ps);
        updateUseSideMode(pkgName, settingEx);
    }

    public void updateUseSideMode(String pkgName, PackageSettingEx ps) {
    }

    public List<String> getSystemWhiteList(String type) {
        return new ArrayList();
    }

    public boolean shouldSkipTriggerFreeform(String pkgName, int userId) {
        return false;
    }

    public int getPrivilegeAppType(String pkgName) {
        return -1;
    }

    public void addGrantedInstalledPkg(PackageParser.Package pkg, boolean isGrant) {
        addGrantedInstalledPkg(new PackageParserEx.PackageEx(pkg), isGrant);
    }

    public void addGrantedInstalledPkg(PackageParserEx.PackageEx pkg, boolean isGrant) {
    }

    public void clearPreferredActivityAsUser(IntentFilter filter, int match, ComponentName[] set, ComponentName activity, int userId) {
    }

    public void registerExtServiceProvider(IExtServiceProvider extServiceProvider, Intent filter) {
        IExtServiceProviderEx serviceProviderEx = new IExtServiceProviderEx();
        serviceProviderEx.setExtServiceProvider(extServiceProvider);
        registerExtServiceProvider(serviceProviderEx, filter);
    }

    public void registerExtServiceProvider(IExtServiceProviderEx extServiceProvider, Intent filter) {
    }

    public void unregisterExtServiceProvider(IExtServiceProvider extServiceProvider) {
        IExtServiceProviderEx serviceProviderEx = new IExtServiceProviderEx();
        serviceProviderEx.setExtServiceProvider(extServiceProvider);
        unregisterExtServiceProvider(serviceProviderEx);
    }

    public void unregisterExtServiceProvider(IExtServiceProviderEx extServiceProvider) {
    }

    public ResolveInfo[] queryExtService(String action, String packageName) {
        ResolveInfoEx[] infoExes = queryExtServiceEx(action, packageName);
        if (infoExes == null) {
            return null;
        }
        ResolveInfo[] infos = new ResolveInfo[infoExes.length];
        for (int i = 0; i < infoExes.length; i++) {
            infos[i] = infoExes[i].getResolveInfo();
        }
        return infos;
    }

    public ResolveInfoEx[] queryExtServiceEx(String action, String packageName) {
        return null;
    }

    public int adjustScanFlagForApk(PackageParser.Package pkg, int scanFlags) {
        return adjustScanFlagForApk(new PackageParserEx.PackageEx(pkg), scanFlags);
    }

    public int adjustScanFlagForApk(PackageParserEx.PackageEx pkg, int scanFlags) {
        return -1;
    }

    public boolean isSystemAppGrantByMdm(PackageParser.Package pkg) {
        return isSystemAppGrantByMdm(new PackageParserEx.PackageEx(pkg));
    }

    public boolean isSystemAppGrantByMdm(PackageParserEx.PackageEx pkg) {
        return false;
    }

    public boolean isSystemAppGrantByMdm(String pkgName) {
        return false;
    }

    public boolean isInValidApkPatchFile(File file, int parseFlag) {
        return false;
    }

    public boolean isInValidApkPatchPkg(PackageParser.Package pkg) {
        return isInValidApkPatchPkg(new PackageParserEx.PackageEx(pkg));
    }

    public boolean isInValidApkPatchPkg(PackageParserEx.PackageEx pkg) {
        return false;
    }

    public boolean isReservePersistentApp(PackageSetting ps) {
        PackageSettingEx settingEx = new PackageSettingEx();
        settingEx.setPackageSetting(ps);
        return isReservePersistentApp(settingEx);
    }

    public boolean isReservePersistentApp(PackageSettingEx ps) {
        return false;
    }

    public boolean setForceDarkSetting(List<String> list, int forceDarkMode) {
        return false;
    }

    public int getForceDarkSetting(String packageName) {
        return -1;
    }

    public void updateForceDarkMode(String pkgName, PackageSetting ps) {
        PackageSettingEx settingEx = new PackageSettingEx();
        settingEx.setPackageSetting(ps);
        updateForceDarkMode(pkgName, settingEx);
    }

    public void updateForceDarkMode(String pkgName, PackageSettingEx ps) {
    }

    public void revokePermissionsFromApp(String pkgName, List<String> list) {
    }

    public void updateWhitelistByHot() {
    }

    public void restoreHwLauncherMode(int[] allUserHandles) {
    }

    public ComponentName getMdmDefaultLauncher(List<ResolveInfo> resolveInfos) {
        List<ResolveInfoEx> infoExList = new ArrayList<>();
        for (ResolveInfo info : resolveInfos) {
            ResolveInfoEx infoEx = new ResolveInfoEx();
            infoEx.setResolveInfo(info);
            infoExList.add(infoEx);
        }
        return getMdmDefaultLauncherEx(infoExList);
    }

    public ComponentName getMdmDefaultLauncherEx(List<ResolveInfoEx> list) {
        return null;
    }

    public ArrayList<String> getDelPackageList() {
        return new ArrayList<>();
    }

    public void putPreferredActivityInPcMode(int userId, IntentFilter filter, PreferredActivity preferredActivity) {
        PreferredActivityEx activityEx = new PreferredActivityEx();
        activityEx.setPreferredActivity(preferredActivity);
        putPreferredActivityInPcMode(userId, filter, activityEx);
    }

    public void putPreferredActivityInPcMode(int userId, IntentFilter filter, PreferredActivityEx preferredActivity) {
    }

    public boolean isFindPreferredActivityInCache(Intent intent, String resolvedType, int userId) {
        return false;
    }

    public ResolveInfo findPreferredActivityInCache(Intent intent, String resolvedType, int flags, List<ResolveInfo> query, int userId) {
        List<ResolveInfoEx> infoExList = new ArrayList<>();
        for (ResolveInfo info : query) {
            ResolveInfoEx infoEx = new ResolveInfoEx();
            infoEx.setResolveInfo(info);
            infoExList.add(infoEx);
        }
        return findPreferredActivityInCacheEx(intent, resolvedType, flags, infoExList, userId).getResolveInfo();
    }

    public ResolveInfoEx findPreferredActivityInCacheEx(Intent intent, String resolvedType, int flags, List<ResolveInfoEx> list, int userId) {
        return new ResolveInfoEx();
    }

    public boolean isMultiScreenCollaborationEnabled(Intent intent) {
        return false;
    }

    public void filterResolveInfo(Intent intent, String resolvedType, List<ResolveInfo> resolveInfoList) {
        List<ResolveInfoEx> infoExList = new ArrayList<>();
        for (ResolveInfo info : resolveInfoList) {
            ResolveInfoEx infoEx = new ResolveInfoEx();
            infoEx.setResolveInfo(info);
            infoExList.add(infoEx);
        }
        filterResolveInfoEx(intent, resolvedType, infoExList);
        resolveInfoList.clear();
        for (ResolveInfoEx infoEx2 : infoExList) {
            resolveInfoList.add(infoEx2.getResolveInfo());
        }
    }

    public void filterResolveInfoEx(Intent intent, String resolvedType, List<ResolveInfoEx> list) {
    }

    public Bundle[] canGrantDPermissions(Bundle[] bundles) {
        return null;
    }

    @GuardedBy({"PackagesLock"})
    public Optional<HwRenamedPackagePolicy> generateRenamedPackagePolicyLocked(PackageParser.Package pkg) {
        Optional<HwRenamedPackagePolicyEx> policyExOptional = generateRenamedPackagePolicyLocked(new PackageParserEx.PackageEx(pkg));
        if (!policyExOptional.isPresent()) {
            return Optional.empty();
        }
        return Optional.of(policyExOptional.get().getmPackagePolicy());
    }

    @GuardedBy({"PackagesLock"})
    public Optional<HwRenamedPackagePolicyEx> generateRenamedPackagePolicyLocked(PackageParserEx.PackageEx pkg) {
        return Optional.empty();
    }

    public Map<String, String> getHwRenamedPackages(int flags) {
        return null;
    }

    public Optional<HwRenamedPackagePolicy> getRenamedPackagePolicyByOriginalName(String originalPackageName) {
        Optional<HwRenamedPackagePolicyEx> policyExOptional = getRenamedPackagePolicyByOriginalNameEx(originalPackageName);
        if (policyExOptional.isPresent()) {
            return Optional.of(policyExOptional.get().getmPackagePolicy());
        }
        return Optional.empty();
    }

    public Optional<HwRenamedPackagePolicyEx> getRenamedPackagePolicyByOriginalNameEx(String originalPackageName) {
        return Optional.empty();
    }

    @GuardedBy({"PackagesLock"})
    public boolean migrateDataForRenamedPackageLocked(PackageParser.Package pkg, int userId, int flags) {
        return migrateDataForRenamedPackageLocked(new PackageParserEx.PackageEx(pkg), userId, flags);
    }

    @GuardedBy({"PackagesLock"})
    public boolean migrateDataForRenamedPackageLocked(PackageParserEx.PackageEx pkg, int userId, int flags) {
        return false;
    }

    public boolean isOldPackageNameCanNotInstall(String packageName) {
        return false;
    }

    public boolean migrateAppUninstalledState(String packageName) {
        return false;
    }

    public List<ApplicationInfo> getClusterApplications(int flags, int clusterMask, boolean isOnlyDisabled, int userId) {
        return new ArrayList();
    }

    public int installHepApp(File stageDir) {
        return -1;
    }

    public List<HwHepPackageInfo> getInstalledHep(int flags) {
        return null;
    }

    public HwPresetPackage getPresetPackage(String packageName) {
        return null;
    }

    public int uninstallHep(String packageName, int flags) {
        return -1;
    }

    public void setVersionMatchFlag(int deviceType, int version, boolean isMatchSuccess) {
    }

    public boolean getVersionMatchFlag(int deviceType, int version) {
        return false;
    }

    public void setOpenFileResult(Intent intent, int retCode) {
    }

    public int getOpenFileResult(Intent intent) {
        return -1;
    }

    public int getDisplayChangeAppRestartConfig(int type, String pkgName) {
        return 0;
    }

    public boolean isMygoteEnabled() {
        return false;
    }

    public int getCacheLevelForMapleApp(PackageParser.Package pkg) {
        return getCacheLevelForMapleApp(new PackageParserEx.PackageEx(pkg));
    }

    public int getCacheLevelForMapleApp(PackageParserEx.PackageEx pkg) {
        return 0;
    }

    public String obtainMapleClassPathByPkg(PackageParser.Package pkg) {
        return obtainMapleClassPathByPkg(new PackageParserEx.PackageEx(pkg));
    }

    public String obtainMapleClassPathByPkg(PackageParserEx.PackageEx pkg) {
        return "";
    }

    public void callGenMplCacheAtPmsInstaller(String baseCodePath, int sharedGid, int cacheLevel, String mapleClassPath) {
    }

    public List<PackageParser.Package> obtainMaplePkgsToGenCache() {
        List<PackageParserEx.PackageEx> packageExList = obtainMaplePkgsToGenCacheEx();
        if (packageExList == null) {
            return null;
        }
        List<PackageParser.Package> packageList = new ArrayList<>();
        for (PackageParserEx.PackageEx pkg : packageExList) {
            packageList.add((PackageParser.Package) pkg.getPackage());
        }
        return packageList;
    }

    public List<PackageParserEx.PackageEx> obtainMaplePkgsToGenCacheEx() {
        return null;
    }

    public void clearMplCacheLIF(PackageParser.Package pkg, int userId, int flags) {
        clearMplCacheLIF(new PackageParserEx.PackageEx(pkg), userId, flags);
    }

    public void clearMplCacheLIF(PackageParserEx.PackageEx pkg, int userId, int flags) {
    }

    public boolean isMplPackage(PackageParser.Package pkg) {
        return isMplPackage(new PackageParserEx.PackageEx(pkg));
    }

    public boolean isMplPackage(PackageParserEx.PackageEx pkg) {
        return false;
    }

    public boolean isNeedForbidShellFunc(String packageName) {
        return false;
    }

    public void updateDozeList(String packageName, boolean isProtect) {
    }

    public boolean resolvePreferredActivity(IntentFilter filter, int match, ComponentName[] sets, ComponentName activity, int userId) {
        return false;
    }

    public void rebuildPreferredActivity(int userId) {
    }

    public void rebuildApkBindFile() {
    }

    public void initParallelPackageDexOptimizer(Context context, PackageDexOptimizer packageDexOptimizer) {
        PackageDexOptimizerEx optimizerEx = new PackageDexOptimizerEx();
        optimizerEx.setmOptimizer(packageDexOptimizer);
        initParallelPackageDexOptimizer(context, optimizerEx);
    }

    public void initParallelPackageDexOptimizer(Context context, PackageDexOptimizerEx packageDexOptimizer) {
    }

    public void parallelPerformDexOpt(PackageParser.Package pkg, String[] instructionSets, CompilerStats.PackageStats packageStats, PackageDexUsage.PackageUseInfo packageUseInfo, DexoptOptions options) {
        PackageParserEx.PackageEx packageEx = new PackageParserEx.PackageEx(pkg);
        CompilerStatsEx.PackageStatsEx statsEx = new CompilerStatsEx.PackageStatsEx();
        statsEx.setPackageStats(packageStats);
        PackageDexUsageEx.PackageUseInfoEx infoEx = new PackageDexUsageEx.PackageUseInfoEx();
        infoEx.setPackageUseInfo(packageUseInfo);
        DexoptOptionsEx optionsEx = new DexoptOptionsEx();
        optionsEx.setDexoptOptions(options);
        parallelPerformDexOpt(packageEx, instructionSets, statsEx, infoEx, optionsEx);
    }

    public void parallelPerformDexOpt(PackageParserEx.PackageEx pkg, String[] instructionSets, CompilerStatsEx.PackageStatsEx packageStats, PackageDexUsageEx.PackageUseInfoEx packageUseInfo, DexoptOptionsEx options) {
    }

    public static boolean isSupportCloneAppInCust(String packageName) {
        return HwPackageManagerService.isSupportCloneAppInCust(packageName);
    }

    public boolean removeMatchedPreferredActivity(Intent intent, PreferredIntentResolver preferredIntentResolver, PreferredActivity preferredActivity) {
        PreferredIntentResolverEx resolverEx = new PreferredIntentResolverEx();
        resolverEx.setPreferredIntentResolver(preferredIntentResolver);
        PreferredActivityEx activityEx = new PreferredActivityEx();
        activityEx.setPreferredActivity(preferredActivity);
        return removeMatchedPreferredActivity(intent, resolverEx, activityEx);
    }

    public boolean removeMatchedPreferredActivity(Intent intent, PreferredIntentResolverEx preferredIntentResolver, PreferredActivityEx preferredActivity) {
        return false;
    }

    public boolean isScanInstallApk(String codePath) {
        return false;
    }

    public void loadRemoveUnstallApks() {
    }

    public boolean isCustedCouldStoppedEx(String pkgName, boolean isBlock, boolean isStopped) {
        return false;
    }

    public void initCustStoppedAppsEx() {
    }

    public boolean forbidGMSUpgrade(PackageParser.Package pkg, PackageParser.Package oldPackage, int callingSessionUid, HwGunstallSwitchState hwGSwitchState) {
        return forbidGMSUpgrade(new PackageParserEx.PackageEx(pkg), new PackageParserEx.PackageEx(oldPackage), callingSessionUid, hwGSwitchState);
    }

    public boolean forbidGMSUpgrade(PackageParserEx.PackageEx pkg, PackageParserEx.PackageEx oldPackage, int callingSessionUid, HwGunstallSwitchState hwGSwitchState) {
        return false;
    }

    public HwGunstallSwitchState updateGunstallState() {
        return null;
    }

    public FeatureInfo[] getHwSystemAvailableFeatures() {
        return null;
    }

    public boolean hasHwSystemFeature(String featureName, int version) {
        return false;
    }

    public void fixMdmRuntimePermission(String packageName, String permName, int flag) {
    }

    public boolean isMdmFix(String packageName) {
        return false;
    }

    public boolean isNeedForbidAppAct(String scenes, String pkgName, String className, HashMap<String, String> hashMap) {
        return false;
    }

    public void recordPreasApp(String pkgName, String appPath) {
    }

    public boolean isInDelAppList(String packageName) {
        return false;
    }

    public void addWaitDexOptPackage(String packageName) {
    }

    public boolean isRemoveUnstallApk(File file) {
        return false;
    }

    @Override // com.android.server.pm.IHwPackageManagerServiceExInner
    public HwCustPackageManagerServiceEx getCust() {
        return null;
    }

    @Override // com.android.server.pm.IHwPackageManagerServiceExInner
    public PackageManagerServiceEx getIPmsInner() {
        return null;
    }

    @Override // com.android.server.pm.IHwPackageManagerServiceExInner
    public Map<String, String> getUninstalledMap() {
        return null;
    }

    @Override // com.android.server.pm.IHwPackageManagerServiceExInner
    public boolean isPlatformSignatureApp(String pkgName) {
        return false;
    }

    public boolean isNeedForbidHarmfulAppDisableApp(String callingPackageName, String targetPackageName) {
        return false;
    }

    public boolean isNeedForbidHarmfulAppUpdateApp(String packageName, String updateSource) {
        return false;
    }

    public boolean isNeedForbidHarmfulAppSlientDeleteApp(String deletePackageName) {
        return false;
    }
}
