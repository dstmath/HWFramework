package com.android.server.pm;

import android.content.pm.ActivityInfoEx;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageParser;
import android.content.pm.PackageParserEx;
import android.os.Bundle;
import android.os.Handler;
import android.util.ArrayMap;
import com.android.server.pm.PackageManagerServiceEx;
import com.android.server.pm.permission.PermissionManagerServiceInternal;
import com.android.server.pm.permission.PermissionManagerServiceInternalEx;
import com.huawei.android.content.IIntentReceiverEx;
import com.huawei.android.content.pm.IPackageInstallObserver2Ex;
import com.huawei.android.content.pm.ParceledListSliceEx;
import com.huawei.android.content.pm.SignatureEx;
import com.huawei.android.os.UserHandleEx;
import com.huawei.android.os.UserManagerInternalEx;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class IHwPackageManagerInnerEx {
    private IHwPackageManagerInner mHwPackageManagerInner;

    public IHwPackageManagerInner getHwPackageManagerInner() {
        return this.mHwPackageManagerInner;
    }

    public void setHwPackageManagerInner(IHwPackageManagerInner hwPackageManagerInner) {
        this.mHwPackageManagerInner = hwPackageManagerInner;
    }

    public InstallerEx getInstallerInner() {
        Installer installer = this.mHwPackageManagerInner.getInstallerInner();
        InstallerEx installerEx = new InstallerEx();
        installerEx.setInstaller(installer);
        return installerEx;
    }

    public boolean isUpgrade() {
        return this.mHwPackageManagerInner.isUpgrade();
    }

    public ArrayMap<String, PackageParserEx.PackageEx> getPackagesLock() {
        ArrayMap<String, PackageParser.Package> packageMap = this.mHwPackageManagerInner.getPackagesLock();
        ArrayMap<String, PackageParserEx.PackageEx> packageExMap = new ArrayMap<>();
        for (int i = 0; i < packageMap.size(); i++) {
            packageExMap.put(packageMap.keyAt(i), new PackageParserEx.PackageEx(packageMap.valueAt(i)));
        }
        return packageExMap;
    }

    public SettingsEx getSettings() {
        Settings settings = this.mHwPackageManagerInner.getSettings();
        SettingsEx settingsEx = new SettingsEx();
        settingsEx.setSettings(settings);
        return settingsEx;
    }

    public boolean performDexOptMode(String packageName, boolean isCheckProfiles, String targetCompilerFilter, boolean isForce, boolean isBootCompleted, String splitName) {
        return this.mHwPackageManagerInner.performDexOptMode(packageName, isCheckProfiles, targetCompilerFilter, isForce, isBootCompleted, splitName);
    }

    public PermissionManagerServiceInternalEx getPermissionManager() {
        PermissionManagerServiceInternal pmsi = this.mHwPackageManagerInner.getPermissionManager();
        PermissionManagerServiceInternalEx managerEx = new PermissionManagerServiceInternalEx();
        managerEx.setPermissionManagerServiceInternal(pmsi);
        return managerEx;
    }

    public Handler getPackageHandler() {
        return this.mHwPackageManagerInner.getPackageHandler();
    }

    public PackageManagerServiceEx.InstallParamsEx createInstallParams(PackageManagerServiceEx.OriginInfoEx origin, PackageManagerServiceEx.MoveInfoEx move, IPackageInstallObserver2Ex observer, int installFlags, String installerPackageName, String volumeUuid, PackageManagerServiceEx.VerificationInfoEx verificationInfo, UserHandleEx user, String packageAbiOverride, String[] grantedPermissions, PackageParserEx.SigningDetailsEx signingDetails, int installReason) {
        return null;
    }

    public boolean getIsDefaultPreferredActivityChangedInner() {
        return this.mHwPackageManagerInner.getIsDefaultPreferredActivityChangedInner();
    }

    public boolean getIsDefaultGoogleCalendarInner() {
        return this.mHwPackageManagerInner.getIsDefaultGoogleCalendarInner();
    }

    public HwCustPackageManagerServiceEx getHwPMSCustPackageManagerService() {
        HwCustPackageManagerServiceEx serviceEx = new HwCustPackageManagerServiceEx();
        serviceEx.setHwCustPackageManagerService(this.mHwPackageManagerInner.getHwPMSCustPackageManagerService());
        return serviceEx;
    }

    public String getNameForUidInner(int uid) {
        return this.mHwPackageManagerInner.getNameForUidInner(uid);
    }

    public int checkSignaturesInner(String pkg1, String pkg2) {
        return this.mHwPackageManagerInner.checkSignaturesInner(pkg1, pkg2);
    }

    public boolean getCotaFlagInner() {
        return this.mHwPackageManagerInner.getCotaFlagInner();
    }

    public void setHwPMSCotaApksInstallStatus(int value) {
        this.mHwPackageManagerInner.setHwPMSCotaApksInstallStatus(value);
    }

    public void scanPackageFilesLIInner(File[] files, int parseFlags, int scanFlags, long currentTime, int hwFlags) {
        this.mHwPackageManagerInner.scanPackageFilesLIInner(files, parseFlags, scanFlags, currentTime, hwFlags);
    }

    /* access modifiers changed from: package-private */
    public ActivityInfoEx getResolveActivityInner() {
        return null;
    }

    public void setUpCustomResolverActivityInner(PackageParserEx.PackageEx pkg) {
    }

    public void setRealSigningDetails(PackageParserEx.PackageEx pkg, PackageParserEx.SigningDetailsEx real) {
    }

    public SignatureEx[] getRealSignature(PackageParserEx.PackageEx pkg) {
        return null;
    }

    public int deletePackageInner(String packageName, long versionCode, int userId, int deleteFlags) {
        return -1;
    }

    public ParceledListSliceEx getInstalledApplications(int flags, int userId) {
        return null;
    }

    public void assertProvidersNotDefined(PackageParserEx.PackageEx pkg) throws PackageManagerException {
    }

    public UserManagerInternalEx getUserManagerInternalInnerEx() {
        return null;
    }

    public int installExistingPackageAsUserInternalInner(String packageName, int userId, int installFlags, int installReason) {
        return -1;
    }

    public PackageParserEx.PackageEx scanPackageLIInner(File scanFile, int parseFlags, int scanFlags, long currentTime, UserHandleEx user, int hwFlags) throws PackageManagerExceptionEx {
        return null;
    }

    public boolean setWhitelistedRestrictedPermissionsInner(String packageName, List<String> list, int whitelistFlag, int userId) {
        return false;
    }

    public void updateSharedLibrariesLPrInner(PackageParserEx.PackageEx pkg, PackageParserEx.PackageEx changingLib) throws PackageManagerExceptionEx {
    }

    public void updateSettingsLIInner(PackageParserEx.PackageEx newPackage, String installerPackageName, int[] allUsers, PackageManagerServiceEx.PackageInstalledInfoEx res, UserHandleEx user, int installReason) {
    }

    public void prepareAppDataAfterInstallLIFInner(PackageParserEx.PackageEx pkg) {
    }

    /* access modifiers changed from: package-private */
    public void sendPackageBroadcastInner(String action, String pkg, Bundle extras, int flags, String targetPkg, IIntentReceiverEx finishedReceiver, int[] userIds, int[] instantUserIds) {
    }

    public boolean getSystemReadyInner() {
        return false;
    }

    public int getUidTargetSdkVersionLockedLPrEx(int userId) {
        return -1;
    }

    public void writePackageRestrictions(int userId) {
    }

    public void sendPreferredActivityChangedBroadcast(int userId) {
    }

    public PackageSettingEx getPackageSettingByPackageName(String packageName) {
        return null;
    }

    public ActivityInfoEx getResolveActivityInnerEx() {
        return null;
    }

    public ArrayMap<String, FeatureInfo> getAvailableFeaturesInner() {
        return null;
    }

    public void scanDirLIInner(File scanDir, int parseFlags, int scanFlags, long currentTime, int hwFlags) {
    }

    public HashMap<String, HashSet<String>> getHwPMSCotaInstallMap() {
        return null;
    }

    public HashMap<String, HashSet<String>> getHwPMSCotaDelInstallMap() {
        return null;
    }
}
