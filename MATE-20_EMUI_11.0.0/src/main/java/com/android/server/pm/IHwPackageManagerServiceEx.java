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
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.os.Bundle;
import android.os.IBackupSessionCallback;
import android.os.ParcelFileDescriptor;
import android.os.UserHandle;
import android.util.ArrayMap;
import com.android.internal.annotations.GuardedBy;
import com.android.server.pm.CompilerStats;
import com.android.server.pm.PackageManagerService;
import com.android.server.pm.dex.DexoptOptions;
import com.android.server.pm.dex.PackageDexUsage;
import com.huawei.android.content.pm.HwHepPackageInfo;
import com.huawei.android.content.pm.IExtServiceProvider;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IHwPackageManagerServiceEx {
    void addFlagsForRemovablePreApk(PackageParser.Package v, int i);

    void addGrantedInstalledPkg(PackageParser.Package v, boolean z);

    void addPreinstalledPkgToList(PackageParser.Package v);

    void addUninstallDataToCache(String str, String str2);

    void addUpdatedRemoveableAppFlag(String str, String str2);

    void addWaitDexOptPackage(String str);

    int adjustScanFlagForApk(PackageParser.Package v, int i);

    void callGenMplCacheAtPmsInstaller(String str, int i, int i2, String str2);

    Bundle[] canGrantDPermissions(Bundle[] bundleArr);

    void checkHwCertification(PackageParser.Package v, boolean z);

    boolean checkUninstalledSystemApp(PackageParser.Package v, PackageManagerService.InstallArgs installArgs, PackageManagerService.PackageInstalledInfo packageInstalledInfo) throws PackageManagerException;

    void cleanUpHwCert();

    void clearMplCacheLIF(PackageParser.Package v, int i, int i2);

    void clearPreferredActivityAsUser(IntentFilter intentFilter, int i, ComponentName[] componentNameArr, ComponentName componentName, int i2);

    boolean containDelPath(String str);

    void createPublicityFile();

    void deleteExistsIfNeeded();

    void doPostScanInstall(PackageParser.Package v, UserHandle userHandle, boolean z, int i, PackageParser.Package v2);

    void filterResolveInfo(Intent intent, String str, List<ResolveInfo> list);

    ResolveInfo findPreferredActivityInCache(Intent intent, String str, int i, List<ResolveInfo> list, int i2);

    void fixMdmRuntimePermission(String str, String str2, int i);

    boolean forbidGMSUpgrade(PackageParser.Package v, PackageParser.Package v2, int i, HwGunstallSwitchState hwGunstallSwitchState);

    @GuardedBy({"PackagesLock"})
    Optional<HwRenamedPackagePolicy> generateRenamedPackagePolicyLocked(PackageParser.Package v);

    void getAPKInstallListForHwPMS(List<File> list, HashMap<String, HashSet<String>> hashMap);

    int getAppUseNotchMode(String str);

    int getAppUseSideMode(String str);

    float getApplicationAspectRatio(String str, String str2);

    List<String> getAppsUseSideList();

    int getCacheLevelForMapleApp(PackageParser.Package v);

    List<ApplicationInfo> getClusterApplications(int i, int i2, boolean z, int i3);

    ArrayList<String> getDataApkShouldNotUpdateByCota();

    ArrayList<String> getDelPackageList();

    int getDisplayChangeAppRestartConfig(int i, String str);

    int getExecuteBackupTask(int i, String str);

    int getFinishBackupSession(int i);

    int getForceDarkSetting(String str);

    boolean getHwCertPermission(boolean z, PackageParser.Package v, String str);

    List<String> getHwPublicityAppList();

    ParcelFileDescriptor getHwPublicityAppParcelFileDescriptor();

    Map<String, String> getHwRenamedPackages(int i);

    FeatureInfo[] getHwSystemAvailableFeatures();

    List<HwHepPackageInfo> getInstalledHep(int i);

    ComponentName getMdmDefaultLauncher(List<ResolveInfo> list);

    String getMspesOEMConfig();

    List<String> getOldDataBackup();

    int getOpenFileResult(Intent intent);

    List<String> getPreinstalledApkList();

    int getPrivilegeAppType(String str);

    Optional<HwRenamedPackagePolicy> getRenamedPackagePolicyByOriginalName(String str);

    String getResourcePackageNameByIcon(String str, int i, int i2);

    List<String> getScanInstallList();

    int getStartBackupSession(IBackupSessionCallback iBackupSessionCallback);

    List<String> getSystemWhiteList(String str);

    void getUninstallApk();

    boolean getVersionMatchFlag(int i, int i2);

    boolean hasHwSystemFeature(String str, int i);

    ResolveInfo hwFindPreferredActivity(Intent intent, List<ResolveInfo> list);

    boolean hwPerformDexOptMode(String str, boolean z, boolean z2, boolean z3, String str2);

    void initCertCompatSettings();

    void initHwCertificationManager();

    void initParallelPackageDexOptimizer(Context context, PackageDexOptimizer packageDexOptimizer);

    void installAPKforInstallListForHwPMS(HashSet<String> hashSet, int i, int i2, long j);

    void installAPKforInstallListForHwPMS(HashSet<String> hashSet, int i, int i2, long j, int i3);

    int installHepApp(File file);

    void installPackageAsUser(String str, IPackageInstallObserver2 iPackageInstallObserver2, int i, String str2, int i2);

    boolean isAllAppsUseSideMode(List<String> list);

    boolean isAllowUninstallApp(String str);

    boolean isAllowedSetHomeActivityForAntiMal(PackageInfo packageInfo, int i);

    boolean isApkDexOpt(String str);

    boolean isAppInstallAllowed(String str);

    boolean isDelapp(PackageSetting packageSetting);

    boolean isDelappInData(PackageSetting packageSetting);

    boolean isDisallowUninstallApk(String str);

    boolean isFindPreferredActivityInCache(Intent intent, String str, int i);

    boolean isHwCustHiddenInfoPackage(PackageParser.Package v);

    boolean isInDelAppList(String str);

    boolean isInMWPortraitWhiteList(String str);

    boolean isInMspesForbidInstallPackageList(String str);

    boolean isInMultiWinWhiteList(String str);

    boolean isInValidApkPatchFile(File file, int i);

    boolean isInValidApkPatchPkg(PackageParser.Package v);

    boolean isMDMDisallowedInstallPackage(PackageParser.Package v, PackageManagerService.PackageInstalledInfo packageInstalledInfo);

    boolean isMplPackage(PackageParser.Package v);

    boolean isMultiScreenCollaborationEnabled(Intent intent);

    boolean isMygoteEnabled();

    boolean isNeedForbidAppAct(String str, String str2, String str3, HashMap<String, String> hashMap);

    boolean isNeedForbidHarmfulAppDisableApp(String str, String str2);

    boolean isNeedForbidHarmfulAppSlientDeleteApp(String str);

    boolean isNeedForbidHarmfulAppUpdateApp(String str, String str2);

    boolean isNeedForbidShellFunc(String str);

    boolean isOldPackageNameCanNotInstall(String str);

    boolean isPerfOptEnable(String str, int i);

    boolean isPersistentUpdatable(PackageParser.Package v);

    boolean isPreRemovableApp(String str);

    boolean isPrivAppNonSystemPartitionDir(File file);

    boolean isPrivilegedPreApp(File file);

    boolean isRemoveUnstallApk(File file);

    boolean isReservePersistentApp(PackageSetting packageSetting);

    boolean isScanInstallApk(String str);

    boolean isSystemAppGrantByMdm(PackageParser.Package v);

    boolean isSystemAppGrantByMdm(String str);

    boolean isSystemPreApp(File file);

    boolean isSystemSignatureUpdated(Signature[] signatureArr, Signature[] signatureArr2);

    boolean isUnAppInstallAllowed(String str);

    boolean isUninstallApk(String str);

    void loadCorrectUninstallDelapp();

    void loadRemoveUnstallApks();

    boolean migrateAppUninstalledState(String str);

    @GuardedBy({"PackagesLock"})
    boolean migrateDataForRenamedPackageLocked(PackageParser.Package v, int i, int i2);

    boolean needAddUpdatedRemoveableAppFlag(String str);

    boolean needInstallRemovablePreApk(PackageParser.Package v, int i);

    String obtainMapleClassPathByPkg(PackageParser.Package v);

    List<PackageParser.Package> obtainMaplePkgsToGenCache();

    void onNewUserCreated(int i);

    void onUserRemoved(int i);

    void parallelPerformDexOpt(PackageParser.Package v, String[] strArr, CompilerStats.PackageStats packageStats, PackageDexUsage.PackageUseInfo packageUseInfo, DexoptOptions dexoptOptions);

    void parseInstalledPkgInfo(String str, String str2, String str3, int i, int i2, boolean z);

    boolean pmInstallHwTheme(String str, boolean z, int i);

    void preSendPackageBroadcast(String str, String str2, String str3);

    void putPreferredActivityInPcMode(int i, IntentFilter intentFilter, PreferredActivity preferredActivity);

    ResolveInfo[] queryExtService(String str, String str2);

    String readMspesFile(String str);

    void readPersistentConfig();

    void readPreInstallApkList();

    void rebuildApkBindFile();

    void rebuildPreferredActivity(int i);

    void recordInstallAppInfo(String str, long j, int i);

    void recordPreasApp(String str, String str2);

    void recordUninstalledDelapp(String str, String str2);

    void registerExtServiceProvider(IExtServiceProvider iExtServiceProvider, Intent intent);

    boolean removeMatchedPreferredActivity(Intent intent, PreferredIntentResolver preferredIntentResolver, PreferredActivity preferredActivity);

    void replaceSignatureIfNeeded(PackageSetting packageSetting, PackageParser.Package v, boolean z, boolean z2);

    void reportEventStream(int i, String str);

    void resolvePersistentFlagForPackage(int i, PackageParser.Package v);

    boolean resolvePreferredActivity(IntentFilter intentFilter, int i, ComponentName[] componentNameArr, ComponentName componentName, int i2);

    boolean restoreAllAppsUseSideMode();

    void restoreHwLauncherMode(int[] iArr);

    void revokePermissionsFromApp(String str, List<String> list);

    boolean scanInstallApk(String str);

    void scanNoSysAppInNonSystemPartitionDir(int i);

    void scanNonSystemPartitionDir(int i);

    void scanRemovableAppDir(int i);

    void sendIncompatibleNotificationIfNeeded(String str);

    boolean setAllAppsUseSideMode(boolean z);

    boolean setAllAppsUseSideModeAndStopApps(List<String> list, boolean z);

    void setAppCanUninstall(String str, boolean z);

    void setAppUseNotchMode(String str, int i);

    void setAppUseSideMode(String str, int i);

    boolean setApplicationAspectRatio(String str, String str2, float f);

    boolean setForceDarkSetting(List<String> list, int i);

    void setHdbKey(String str);

    void setOpenFileResult(Intent intent, int i);

    void setUpCustomResolverActivity(PackageParser.Package v);

    void setVersionMatchFlag(int i, int i2, boolean z);

    boolean shouldSkipTriggerFreeform(String str, int i);

    void systemReady();

    int uninstallHep(String str, int i);

    void unregisterExtServiceProvider(IExtServiceProvider iExtServiceProvider);

    void updateAppsUseSideWhitelist(ArrayMap<String, String> arrayMap, ArrayMap<String, String> arrayMap2);

    void updateCertCompatPackage(PackageParser.Package v, PackageSetting packageSetting);

    void updateDozeList(String str, boolean z);

    void updateForceDarkMode(String str, PackageSetting packageSetting);

    HwGunstallSwitchState updateGunstallState();

    int updateMspesOEMConfig(String str);

    void updateNotchScreenWhite(String str, String str2, int i);

    void updatePackageBlackListInfo(String str);

    void updateUseSideMode(String str, PackageSetting packageSetting);

    void updateWhitelistByHot();

    boolean verifyPackageSecurityPolicy(String str, File file);

    void writeCertCompatPackages(boolean z);

    boolean writeMspesFile(String str, String str2);

    void writePreinstalledApkListToFile();
}
