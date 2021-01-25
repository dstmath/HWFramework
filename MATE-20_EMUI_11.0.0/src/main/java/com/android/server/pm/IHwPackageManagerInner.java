package com.android.server.pm;

import android.content.ComponentName;
import android.content.IIntentReceiver;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.FeatureInfo;
import android.content.pm.IPackageInstallObserver2;
import android.content.pm.PackageParser;
import android.content.pm.ParceledListSlice;
import android.content.pm.Signature;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.os.UserManagerInternal;
import android.util.ArrayMap;
import com.android.server.pm.PackageManagerService;
import com.android.server.pm.permission.PermissionManagerServiceInternal;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public interface IHwPackageManagerInner {
    void WritePackageRestrictions(int i);

    void addPreferredActivity(IntentFilter intentFilter, int i, ComponentName[] componentNameArr, ComponentName componentName, int i2);

    void assertProvidersNotDefined(PackageParser.Package v) throws PackageManagerException;

    int checkSignaturesInner(String str, String str2);

    PackageManagerService.InstallParams createInstallParams(PackageManagerService.OriginInfo originInfo, PackageManagerService.MoveInfo moveInfo, IPackageInstallObserver2 iPackageInstallObserver2, int i, String str, String str2, PackageManagerService.VerificationInfo verificationInfo, UserHandle userHandle, String str3, String[] strArr, PackageParser.SigningDetails signingDetails, int i2);

    int deletePackageInner(String str, long j, int i, int i2);

    ActivityInfo getActivityInfo(ComponentName componentName, int i, int i2);

    ApplicationInfo getApplicationInfo(String str, int i, int i2);

    ArrayMap<String, FeatureInfo> getAvailableFeaturesInner();

    boolean getCotaFlagInner();

    HashMap<String, HashSet<String>> getHwPMSCotaDelInstallMap();

    HashMap<String, HashSet<String>> getHwPMSCotaInstallMap();

    HwCustPackageManagerService getHwPMSCustPackageManagerService();

    ParceledListSlice<ApplicationInfo> getInstalledApplications(int i, int i2);

    Installer getInstallerInner();

    boolean getIsDefaultGoogleCalendarInner();

    boolean getIsDefaultPreferredActivityChangedInner();

    boolean getIsPreNUpgradeInner();

    String getNameForUidInner(int i);

    Handler getPackageHandler();

    PackageSetting getPackageSettingByPackageName(String str);

    String[] getPackagesForUid(int i);

    ArrayMap<String, PackageParser.Package> getPackagesLock();

    PermissionManagerServiceInternal getPermissionManager();

    Signature[] getRealSignature(PackageParser.Package v);

    ActivityInfo getResolveActivityInner();

    Settings getSettings();

    boolean getSystemReadyInner();

    int getUidTargetSdkVersionLockedLPrEx(int i);

    UserManagerInternal getUserManagerInternalInner();

    int installExistingPackageAsUserInternalInner(String str, int i, int i2, int i3);

    boolean isFirstBootInner();

    boolean isUpgrade();

    void killApplicationInner(String str, int i, String str2);

    boolean performDexOptMode(String str, boolean z, String str2, boolean z2, boolean z3, String str3);

    void prepareAppDataAfterInstallLIFInner(PackageParser.Package v);

    void removePackageLIInner(PackageParser.Package v, boolean z);

    void scanDirLIInner(File file, int i, int i2, long j, int i3);

    void scanPackageFilesLIInner(File[] fileArr, int i, int i2, long j, int i3);

    PackageParser.Package scanPackageLIInner(File file, int i, int i2, long j, UserHandle userHandle, int i3) throws PackageManagerException;

    void scheduleWriteSettingsInner();

    void sendPackageBroadcastInner(String str, String str2, Bundle bundle, int i, String str3, IIntentReceiver iIntentReceiver, int[] iArr, int[] iArr2);

    void sendPreferredActivityChangedBroadcast(int i);

    void setHwPMSCotaApksInstallStatus(int i);

    void setRealSigningDetails(PackageParser.Package v, PackageParser.SigningDetails signingDetails);

    void setUpCustomResolverActivityInner(PackageParser.Package v);

    boolean setWhitelistedRestrictedPermissionsInner(String str, List<String> list, int i, int i2);

    void updateSettingsLIInner(PackageParser.Package v, String str, int[] iArr, PackageManagerService.PackageInstalledInfo packageInstalledInfo, UserHandle userHandle, int i);

    void updateSharedLibrariesLPrInner(PackageParser.Package v, PackageParser.Package v2) throws PackageManagerException;
}
