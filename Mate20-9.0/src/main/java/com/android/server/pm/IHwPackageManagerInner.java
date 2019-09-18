package com.android.server.pm;

import android.content.ComponentName;
import android.content.IIntentReceiver;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.FeatureInfo;
import android.content.pm.IPackageDeleteObserver2;
import android.content.pm.IPackageInstallObserver2;
import android.content.pm.PackageInfo;
import android.content.pm.PackageParser;
import android.content.pm.ParceledListSlice;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.content.pm.VersionedPackage;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.os.UserManagerInternal;
import android.util.ArrayMap;
import com.android.server.pm.PackageManagerService;
import com.android.server.pm.permission.PermissionManagerInternal;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public interface IHwPackageManagerInner {
    int checkPermissionInner(String str, String str2, int i);

    int checkSignaturesInner(String str, String str2);

    PackageManagerService.InstallParams createInstallParams(PackageManagerService.OriginInfo originInfo, PackageManagerService.MoveInfo moveInfo, IPackageInstallObserver2 iPackageInstallObserver2, int i, String str, String str2, PackageManagerService.VerificationInfo verificationInfo, UserHandle userHandle, String str3, String[] strArr, PackageParser.SigningDetails signingDetails, int i2);

    int deletePackageInner(String str, long j, int i, int i2);

    void deletePackageVersionedImpl(VersionedPackage versionedPackage, IPackageDeleteObserver2 iPackageDeleteObserver2, int i, int i2);

    ActivityInfo getActivityInfoInternalInner(ComponentName componentName, int i, int i2, int i3);

    ApplicationInfo getApplicationInfo(String str, int i, int i2);

    ArrayMap<String, FeatureInfo> getAvailableFeaturesInner();

    int getComponentEnabledSettingInner(ComponentName componentName, int i);

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

    PackageInfo getPackageInfoInner(String str, int i, int i2);

    ArrayMap<String, PackageParser.Package> getPackagesLock();

    PermissionManagerInternal getPermissionManager();

    Signature[] getRealSignature(PackageParser.Package packageR);

    ActivityInfo getResolveActivityInner();

    Settings getSettings();

    boolean getSystemReadyInner();

    UserManagerInternal getUserManagerInternalInner();

    void grantRuntimePermissionInner(String str, String str2, int i);

    int installExistingPackageAsUserInternalInner(String str, int i, int i2, int i3);

    boolean isFirstBootInner();

    boolean isPackageAvailableImpl(String str, int i);

    boolean isUpgrade();

    void killApplicationInner(String str, int i, String str2);

    boolean performDexOptMode(String str, boolean z, String str2, boolean z2, boolean z3, String str3);

    void prepareAppDataAfterInstallLIFInner(PackageParser.Package packageR);

    ParceledListSlice<ResolveInfo> queryIntentActivitiesInner(Intent intent, String str, int i, int i2);

    List<ResolveInfo> queryIntentActivitiesInternalImpl(Intent intent, String str, int i, int i2, int i3, boolean z, boolean z2);

    List<ResolveInfo> queryIntentActivitiesInternalInner(Intent intent, String str, int i, int i2);

    ParceledListSlice<ResolveInfo> queryIntentReceiversInner(Intent intent, String str, int i, int i2);

    void removePackageLIInner(PackageParser.Package packageR, boolean z);

    void scanDirLIInner(File file, int i, int i2, long j, int i3);

    void scanPackageFilesLIInner(File[] fileArr, int i, int i2, long j, int i3);

    PackageParser.Package scanPackageLIInner(File file, int i, int i2, long j, UserHandle userHandle, int i3) throws PackageManagerException;

    void scheduleWritePackageRestrictionsLockedInner(int i);

    void sendPackageBroadcastInner(String str, String str2, Bundle bundle, int i, String str3, IIntentReceiver iIntentReceiver, int[] iArr, int[] iArr2);

    void setComponentEnabledSettingInner(ComponentName componentName, int i, int i2, int i3);

    void setHwPMSCotaApksInstallStatus(int i);

    void setPackageStoppedStateInner(String str, boolean z, int i);

    void setRealSigningDetails(PackageParser.Package packageR, PackageParser.SigningDetails signingDetails);

    void setUpCustomResolverActivityInner(PackageParser.Package packageR);

    void updateSettingsLIInner(PackageParser.Package packageR, String str, int[] iArr, PackageManagerService.PackageInstalledInfo packageInstalledInfo, UserHandle userHandle, int i);
}
