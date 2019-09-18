package com.android.server.pm;

import android.content.ComponentName;
import android.content.IIntentReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.IPackageDeleteObserver2;
import android.content.pm.IPackageInstallObserver2;
import android.content.pm.PackageInfo;
import android.content.pm.PackageParser;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.content.pm.VersionedPackage;
import android.os.Bundle;
import android.os.IBackupSessionCallback;
import android.os.ParcelFileDescriptor;
import android.os.UserHandle;
import com.android.server.pm.PackageManagerService;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public interface IHwPackageManagerServiceEx {
    void addFlagsForRemovablePreApk(PackageParser.Package packageR, int i);

    void addPreinstalledPkgToList(PackageParser.Package packageR);

    void addUnisntallDataToCache(String str, String str2);

    void addUpdatedRemoveableAppFlag(String str, String str2);

    int adjustScanFlagForApk(PackageParser.Package packageR, int i);

    void checkHwCertification(PackageParser.Package packageR, boolean z);

    void checkIllegalSysApk(PackageParser.Package packageR, int i) throws PackageManagerException;

    boolean checkPermissionGranted(String str, int i);

    boolean checkUidPermissionGranted(String str, int i);

    boolean checkUninstalledSystemApp(PackageParser.Package packageR, PackageManagerService.InstallArgs installArgs, PackageManagerService.PackageInstalledInfo packageInstalledInfo) throws PackageManagerException;

    void cleanUpHwCert();

    boolean containDelPath(String str);

    void createPublicityFile();

    void deleteClonedProfileIfNeed(int[] iArr);

    void deleteExistsIfNeedForHwPMS();

    void deleteNonRequiredAppsForClone(int i, boolean z);

    void deleteNonSupportedAppsForClone();

    void deletePackageVersioned(VersionedPackage versionedPackage, IPackageDeleteObserver2 iPackageDeleteObserver2, int i, int i2);

    void doPostScanInstall(PackageParser.Package packageR, UserHandle userHandle, boolean z, int i);

    void filterShellApps(ArrayList<PackageParser.Package> arrayList, LinkedList<PackageParser.Package> linkedList);

    void getAPKInstallListForHwPMS(List<File> list, HashMap<String, HashSet<String>> hashMap);

    ActivityInfo getActivityInfo(ComponentName componentName, int i, int i2);

    int getAppUseNotchMode(String str);

    float getApplicationAspectRatio(String str, String str2);

    float getApplicationMaxAspectRatio(String str);

    ArrayList<String> getDataApkShouldNotUpdateByCota();

    int getExecuteBackupTask(int i, String str);

    int getFinishBackupSession(int i);

    boolean getHwCertPermission(boolean z, PackageParser.Package packageR, String str);

    List<String> getHwPublicityAppList();

    ParcelFileDescriptor getHwPublicityAppParcelFileDescriptor();

    UserHandle getHwUserHandle(UserHandle userHandle);

    String getMspesOEMConfig();

    List<String> getOldDataBackup();

    List<String> getPreinstalledApkList();

    String getResourcePackageNameByIcon(String str, int i, int i2);

    List<String> getScanInstallList();

    int getStartBackupSession(IBackupSessionCallback iBackupSessionCallback);

    void getUninstallApk();

    void handleActivityInfoNotFound(int i, Intent intent, int i2, List<ResolveInfo> list);

    PackageInfo handlePackageNotFound(String str, int i, int i2);

    boolean hasSystemFeatureDelegate(String str, int i);

    void hwAddRequirementForDefaultHome(IntentFilter intentFilter, ComponentName componentName, int i);

    ResolveInfo hwFindPreferredActivity(Intent intent, String str, int i, List<ResolveInfo> list, int i2, boolean z, boolean z2, boolean z3, int i3);

    boolean hwPerformDexOptMode(String str, boolean z, String str2, boolean z2, boolean z3, String str3);

    void initCertCompatSettings();

    void initHwCertificationManager();

    void installAPKforInstallListForHwPMS(HashSet<String> hashSet, int i, int i2, long j);

    void installAPKforInstallListForHwPMS(HashSet<String> hashSet, int i, int i2, long j, int i3);

    void installPackageAsUser(String str, IPackageInstallObserver2 iPackageInstallObserver2, int i, String str2, int i2);

    boolean isAllowUninstallApp(String str);

    boolean isAllowedSetHomeActivityForAntiMal(PackageInfo packageInfo, int i);

    boolean isAllowedToBeDisabled(String str);

    boolean isApkDexOpt(String str);

    boolean isAppInstallAllowed(String str, String str2);

    boolean isDelapp(PackageSetting packageSetting);

    boolean isDelappInData(PackageSetting packageSetting);

    boolean isDisallowUninstallApk(String str);

    boolean isDisallowedInstallApk(PackageParser.Package packageR);

    boolean isHwCustHiddenInfoPackage(PackageParser.Package packageR);

    boolean isInMWPortraitWhiteList(String str);

    boolean isInMspesForbidInstallPackageList(String str);

    boolean isInMultiWinWhiteList(String str);

    boolean isInValidApkPatchFile(File file, int i);

    boolean isInValidApkPatchPkg(PackageParser.Package packageR);

    boolean isMDMDisallowedInstallPackage(PackageParser.Package packageR, PackageManagerService.PackageInstalledInfo packageInstalledInfo);

    boolean isPackageAvailable(String str, int i);

    boolean isPerfOptEnable(String str, int i);

    boolean isPersistentUpdatable(PackageParser.Package packageR);

    boolean isPreRemovableApp(String str);

    boolean isPrivAppNonSystemPartitionDir(File file);

    boolean isPrivilegedPreApp(File file);

    boolean isSetupDisabled();

    boolean isSystemAppGrantByMdm(PackageParser.Package packageR);

    boolean isSystemAppGrantByMdm(String str);

    boolean isSystemPathApp(PackageSetting packageSetting);

    boolean isSystemPreApp(File file);

    boolean isSystemSignatureUpdated(Signature[] signatureArr, Signature[] signatureArr2);

    boolean isUnAppInstallAllowed(String str);

    boolean isUninstallApk(String str);

    void loadCorrectUninstallDelapp();

    void loadSysWhitelist();

    boolean makeSetupDisabled(String str);

    boolean needAddUpdatedRemoveableAppFlag(String str);

    boolean needInstallRemovablePreApk(PackageParser.Package packageR, int i);

    void onNewUserCreated(int i);

    void onUserRemoved(int i);

    void parseInstalledPkgInfo(String str, String str2, String str3, int i, int i2, boolean z);

    boolean pmInstallHwTheme(String str, boolean z, int i);

    void preInstallExistingPackageAsUser(String str, int i, int i2, int i3);

    void preSendPackageBroadcast(String str, String str2, Bundle bundle, int i, String str3, IIntentReceiver iIntentReceiver, int[] iArr, int[] iArr2);

    List<ResolveInfo> queryIntentActivitiesInternal(Intent intent, String str, int i, int i2, int i3, boolean z, boolean z2);

    String readMspesFile(String str);

    void readPersistentConfig();

    void readPreInstallApkList();

    void recordInstallAppInfo(String str, long j, int i);

    void recordUninstalledDelapp(String str, String str2);

    void replaceSignatureIfNeeded(PackageSetting packageSetting, PackageParser.Package packageR, boolean z, boolean z2);

    void reportEventStream(int i, String str);

    void resetSharedUserSignaturesIfNeeded();

    void resolvePersistentFlagForPackage(int i, PackageParser.Package packageR);

    void restoreAppDataForClone(String str, int i, int i2);

    boolean scanInstallApk(String str);

    void scanNonSystemPartitionDir(int i);

    void scanRemovableAppDir(int i);

    void sendIncompatibleNotificationIfNeeded(String str);

    void setAppCanUninstall(String str, boolean z);

    void setAppUseNotchMode(String str, int i);

    boolean setApplicationAspectRatio(String str, String str2, float f);

    boolean setApplicationMaxAspectRatio(String str, float f);

    void setHdbKey(String str);

    void setUpCustomResolverActivity(PackageParser.Package packageR);

    boolean skipSetupEnable(String str);

    void systemReady();

    void updateCertCompatPackage(PackageParser.Package packageR, PackageSetting packageSetting);

    int updateFlags(int i, int i2);

    int updateMspesOEMConfig(String str);

    void updateNochScreenWhite(String str, String str2, int i);

    void updatePackageBlackListInfo(String str);

    boolean verifyPackageSecurityPolicy(String str, File file);

    void writeCertCompatPackages(boolean z);

    boolean writeMspesFile(String str, String str2);

    void writePreinstalledApkListToFile();
}
