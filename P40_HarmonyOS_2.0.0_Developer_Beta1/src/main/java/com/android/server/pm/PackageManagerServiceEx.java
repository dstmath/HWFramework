package com.android.server.pm;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfoEx;
import android.content.pm.ApplicationInfo;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageParser;
import android.content.pm.PackageParserEx;
import android.content.pm.ParceledListSlice;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ServiceManager;
import android.util.ArrayMap;
import com.android.server.pm.PackageManagerService;
import com.android.server.pm.permission.PermissionManagerServiceInternalEx;
import com.huawei.android.content.IIntentReceiverEx;
import com.huawei.android.content.pm.IPackageDeleteObserver2Ex;
import com.huawei.android.content.pm.IPackageInstallObserver2Ex;
import com.huawei.android.content.pm.PackageInfoEx;
import com.huawei.android.content.pm.ParceledListSliceEx;
import com.huawei.android.content.pm.ResolveInfoEx;
import com.huawei.android.content.pm.SignatureEx;
import com.huawei.android.content.pm.VersionedPackageEx;
import com.huawei.android.os.ServiceManagerEx;
import com.huawei.android.os.UserHandleEx;
import com.huawei.android.os.UserManagerInternalEx;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class PackageManagerServiceEx {
    public static final boolean DEBUG_INSTALL = PackageManagerService.DEBUG_INSTALL;
    public static final int INIT_COPY = 5;
    public static final String PLATFORM_PACKAGE_NAME = "android";
    private static final String PMS_PACKAGE = "package";
    public static final int SCAN_AS_PRIVILEGED = 262144;
    public static final int SCAN_AS_SYSTEM = 131072;
    private PackageManagerService mPackageManagerService = ServiceManager.getService(PMS_PACKAGE);
    private PermissionManagerServiceInternalEx.PermissionCallbackEx mPermissionCallbackEx = new PermissionManagerServiceInternalEx.PermissionCallbackEx();

    public class InstallParamsEx {
        private PackageManagerService.InstallParams mInstallParams;

        public InstallParamsEx() {
        }

        public PackageManagerService.InstallParams getInstallParams() {
            return this.mInstallParams;
        }

        public void setInstallParams(PackageManagerService.InstallParams installParams) {
            this.mInstallParams = installParams;
        }
    }

    public static class OriginInfoEx {
        private PackageManagerService.OriginInfo mOriginInfo;

        public PackageManagerService.OriginInfo getOriginInfo() {
            return this.mOriginInfo;
        }

        public void setOriginInfo(PackageManagerService.OriginInfo originInfo) {
            this.mOriginInfo = originInfo;
        }

        public static OriginInfoEx fromUntrustedFile(File file) {
            PackageManagerService.OriginInfo origin = PackageManagerService.OriginInfo.fromUntrustedFile(file);
            OriginInfoEx infoEx = new OriginInfoEx();
            infoEx.setOriginInfo(origin);
            return infoEx;
        }
    }

    public static class MoveInfoEx {
        private PackageManagerService.MoveInfo mMoveInfo;

        public PackageManagerService.MoveInfo getMoveInfo() {
            return this.mMoveInfo;
        }

        public void setMoveInfo(PackageManagerService.MoveInfo moveInfo) {
            this.mMoveInfo = moveInfo;
        }
    }

    public static class VerificationInfoEx {
        private PackageManagerService.VerificationInfo mVerificationInfo;

        public VerificationInfoEx() {
        }

        public VerificationInfoEx(Uri originatingUri, Uri referrer, int originatingUid, int installerUid) {
            this.mVerificationInfo = new PackageManagerService.VerificationInfo(originatingUri, referrer, originatingUid, installerUid);
        }

        public void setVerificationInfo(PackageManagerService.VerificationInfo verificationInfo) {
            this.mVerificationInfo = verificationInfo;
        }

        public PackageManagerService.VerificationInfo getVerificationInfo() {
            return this.mVerificationInfo;
        }
    }

    public static class PackageInstalledInfoEx {
        private PackageManagerService.PackageInstalledInfo mPackageInstalledInfo = new PackageManagerService.PackageInstalledInfo();

        public void setPackageInstalledInfo(PackageManagerService.PackageInstalledInfo packageInstalledInfo) {
            this.mPackageInstalledInfo = packageInstalledInfo;
        }

        public PackageManagerService.PackageInstalledInfo getPackageInstalledInfo() {
            return this.mPackageInstalledInfo;
        }

        public void setError(int code, String msg) {
            this.mPackageInstalledInfo.setError(code, msg);
        }

        public void setNewUsers(int[] users) {
            this.mPackageInstalledInfo.newUsers = users;
        }

        public void setReturnCode(int returnCode) {
            this.mPackageInstalledInfo.setReturnCode(returnCode);
        }

        public void setUid(int uid) {
            this.mPackageInstalledInfo.uid = uid;
        }

        public void setOrigUsers(int[] origUsers) {
            this.mPackageInstalledInfo.origUsers = origUsers;
        }

        public void setPkg(PackageParserEx.PackageEx pkg) {
            if (pkg != null) {
                this.mPackageInstalledInfo.pkg = (PackageParser.Package) pkg.getPackage();
                return;
            }
            this.mPackageInstalledInfo.pkg = null;
        }

        public void setRemovedInfo(PackageRemovedInfoEx removedInfo) {
            if (removedInfo != null) {
                this.mPackageInstalledInfo.removedInfo = removedInfo.getRemovedInfo();
                return;
            }
            this.mPackageInstalledInfo.removedInfo = null;
        }
    }

    public static class PackageRemovedInfoEx {
        private PackageManagerService.PackageRemovedInfo mRemovedInfo;

        public PackageManagerService.PackageRemovedInfo getRemovedInfo() {
            return this.mRemovedInfo;
        }

        public void setRemovedInfo(PackageManagerService.PackageRemovedInfo removedInfo) {
            this.mRemovedInfo = removedInfo;
        }
    }

    public static class InstallArgsEx {
        private PackageManagerService.InstallArgs mArgs;

        public PackageManagerService.InstallArgs getInstallArgs() {
            return this.mArgs;
        }

        public void setInstallArgs(PackageManagerService.InstallArgs args) {
            this.mArgs = args;
        }

        public final UserHandleEx getUser() {
            UserHandleEx userHandleEx = new UserHandleEx();
            userHandleEx.setUserHandle(this.mArgs.user);
            return userHandleEx;
        }

        public final String getInstallerPackageName() {
            return this.mArgs.installerPackageName;
        }

        public int getInstallReason() {
            return this.mArgs.installReason;
        }
    }

    public PackageManagerService getPackageManagerSerivce() {
        return this.mPackageManagerService;
    }

    public void setPackageManagerService() {
        this.mPackageManagerService = ServiceManagerEx.getService(PMS_PACKAGE);
    }

    public void setPackageManagerService(PackageManagerService packageManagerService) {
        this.mPackageManagerService = packageManagerService;
    }

    public boolean isUpgrade() {
        PackageManagerService packageManagerService = this.mPackageManagerService;
        if (packageManagerService != null) {
            return packageManagerService.isUpgrade();
        }
        return false;
    }

    public ArrayMap<String, PackageParserEx.PackageEx> getPackagesLock() {
        ArrayMap<String, PackageParserEx.PackageEx> packageExMap;
        PackageManagerService packageManagerService = this.mPackageManagerService;
        if (packageManagerService == null) {
            return new ArrayMap<>();
        }
        ArrayMap<String, PackageParser.Package> packageMap = packageManagerService.getPackagesLock();
        synchronized (packageMap) {
            packageExMap = new ArrayMap<>(packageMap.size());
            for (int i = 0; i < packageMap.size(); i++) {
                packageExMap.append(packageMap.keyAt(i), new PackageParserEx.PackageEx(packageMap.valueAt(i)));
            }
        }
        return packageExMap;
    }

    public Object getPackagesLockObject() {
        return this.mPackageManagerService.getPackagesLock();
    }

    public void putPackagesLock(String packageName, PackageParserEx.PackageEx packageEx) {
        ArrayMap<String, PackageParser.Package> packageMap = this.mPackageManagerService.getPackagesLock();
        synchronized (packageMap) {
            packageMap.put(packageName, (PackageParser.Package) packageEx.getPackage());
        }
    }

    public SettingsEx getSettings() {
        if (this.mPackageManagerService == null) {
            return new SettingsEx();
        }
        SettingsEx settingsEx = new SettingsEx();
        settingsEx.setSettings(this.mPackageManagerService.getSettings());
        return settingsEx;
    }

    public boolean performDexOptMode(String packageName, boolean isCheckProfiles, String targetCompilerFilter, boolean isForce, boolean isBootCompleted, String splitName) {
        PackageManagerService packageManagerService = this.mPackageManagerService;
        if (packageManagerService != null) {
            return packageManagerService.performDexOptMode(packageName, isCheckProfiles, targetCompilerFilter, isForce, isBootCompleted, splitName);
        }
        return false;
    }

    public Handler getPackageHandler() {
        PackageManagerService packageManagerService = this.mPackageManagerService;
        if (packageManagerService != null) {
            return packageManagerService.getPackageHandler();
        }
        return null;
    }

    public PermissionManagerServiceInternalEx getPermissionManager() {
        if (this.mPackageManagerService == null) {
            return new PermissionManagerServiceInternalEx();
        }
        PermissionManagerServiceInternalEx pmsie = new PermissionManagerServiceInternalEx();
        pmsie.setPermissionManagerServiceInternal(this.mPackageManagerService.getPermissionManager());
        return pmsie;
    }

    public InstallParamsEx createInstallParams(OriginInfoEx origin, MoveInfoEx move, IPackageInstallObserver2Ex observer, int installFlags, String installerPackageName, String volumeUuid, VerificationInfoEx verificationInfo, UserHandleEx user, String packageAbiOverride, String[] grantedPermissions, PackageParserEx.SigningDetailsEx signingDetails, int installReason) {
        if (this.mPackageManagerService == null) {
            return new InstallParamsEx();
        }
        InstallParamsEx installParamsEx = new InstallParamsEx();
        installParamsEx.setInstallParams(this.mPackageManagerService.createInstallParams(origin.getOriginInfo(), move.getMoveInfo(), observer.getPackageInstallObserver(), installFlags, installerPackageName, volumeUuid, verificationInfo.getVerificationInfo(), user.getUserHandle(), packageAbiOverride, grantedPermissions, signingDetails.getSigningDetails(), installReason));
        return installParamsEx;
    }

    public boolean getIsDefaultPreferredActivityChangedInner() {
        PackageManagerService packageManagerService = this.mPackageManagerService;
        if (packageManagerService != null) {
            return packageManagerService.getIsDefaultPreferredActivityChangedInner();
        }
        return false;
    }

    public boolean getIsDefaultGoogleCalendarInner() {
        PackageManagerService packageManagerService = this.mPackageManagerService;
        if (packageManagerService != null) {
            return packageManagerService.getIsDefaultGoogleCalendarInner();
        }
        return false;
    }

    public HwCustPackageManagerServiceEx getHwPMSCustPackageManagerService() {
        if (this.mPackageManagerService == null) {
            return null;
        }
        HwCustPackageManagerServiceEx serviceEx = new HwCustPackageManagerServiceEx();
        serviceEx.setHwCustPackageManagerService(this.mPackageManagerService.getHwPMSCustPackageManagerService());
        return serviceEx;
    }

    public String getNameForUidInner(int uid) {
        PackageManagerService packageManagerService = this.mPackageManagerService;
        if (packageManagerService != null) {
            return packageManagerService.getNameForUidInner(uid);
        }
        return null;
    }

    public int checkSignaturesInner(String pkg1, String pkg2) {
        PackageManagerService packageManagerService = this.mPackageManagerService;
        if (packageManagerService != null) {
            return packageManagerService.checkSignaturesInner(pkg1, pkg2);
        }
        return 0;
    }

    public InstallerEx getInstallerInner() {
        if (this.mPackageManagerService == null) {
            return null;
        }
        InstallerEx installerEx = new InstallerEx();
        installerEx.setInstaller(this.mPackageManagerService.getInstallerInner());
        return installerEx;
    }

    public boolean getCotaFlagInner() {
        PackageManagerService packageManagerService = this.mPackageManagerService;
        if (packageManagerService != null) {
            return packageManagerService.getCotaFlagInner();
        }
        return false;
    }

    public void setHwPMSCotaApksInstallStatus(int value) {
        PackageManagerService packageManagerService = this.mPackageManagerService;
        if (packageManagerService != null) {
            packageManagerService.setHwPMSCotaApksInstallStatus(value);
        }
    }

    public void scanPackageFilesLIInner(File[] files, int parseFlags, int scanFlags, long currentTime, int hwFlags) {
        PackageManagerService packageManagerService = this.mPackageManagerService;
        if (packageManagerService != null) {
            packageManagerService.scanPackageFilesLIInner(files, parseFlags, scanFlags, currentTime, hwFlags);
        }
    }

    public ActivityInfoEx getResolveActivityInner() {
        if (this.mPackageManagerService == null) {
            return new ActivityInfoEx();
        }
        ActivityInfoEx activityInfoEx = new ActivityInfoEx();
        activityInfoEx.setActivityInfo(this.mPackageManagerService.getResolveActivityInner());
        return activityInfoEx;
    }

    public void setUpCustomResolverActivityInner(PackageParserEx.PackageEx pkg) {
        PackageManagerService packageManagerService = this.mPackageManagerService;
        if (packageManagerService != null) {
            packageManagerService.setUpCustomResolverActivityInner((PackageParser.Package) pkg.getPackage());
        }
    }

    public void setRealSigningDetails(PackageParserEx.PackageEx pkg, PackageParserEx.SigningDetailsEx real) {
        PackageManagerService packageManagerService = this.mPackageManagerService;
        if (packageManagerService != null) {
            packageManagerService.setRealSigningDetails((PackageParser.Package) pkg.getPackage(), real.getSigningDetails());
        }
    }

    public SignatureEx[] getRealSignature(PackageParserEx.PackageEx pkg) {
        PackageManagerService packageManagerService = this.mPackageManagerService;
        if (packageManagerService == null) {
            return new SignatureEx[0];
        }
        Signature[] signatures = packageManagerService.getRealSignature((PackageParser.Package) pkg.getPackage());
        SignatureEx[] signatureExs = new SignatureEx[signatures.length];
        for (int i = 0; i < signatures.length; i++) {
            signatureExs[i] = new SignatureEx();
            signatureExs[i].setSignature(signatures[i]);
        }
        return signatureExs;
    }

    public int deletePackageInner(String packageName, long versionCode, int userId, int deleteFlags) {
        PackageManagerService packageManagerService = this.mPackageManagerService;
        if (packageManagerService != null) {
            return packageManagerService.deletePackageInner(packageName, versionCode, userId, deleteFlags);
        }
        return -1;
    }

    public ParceledListSliceEx getInstalledApplications(int flags, int userId) {
        PackageManagerService packageManagerService = this.mPackageManagerService;
        if (packageManagerService == null) {
            return null;
        }
        ParceledListSlice<ApplicationInfo> pls = packageManagerService.getInstalledApplications(flags, userId);
        ParceledListSliceEx plse = new ParceledListSliceEx();
        plse.setParceledListSlice(pls);
        return plse;
    }

    public void assertProvidersNotDefined(PackageParserEx.PackageEx pkg) throws PackageManagerExceptionEx {
        try {
            if (this.mPackageManagerService != null) {
                this.mPackageManagerService.assertProvidersNotDefined((PackageParser.Package) pkg.getPackage());
            }
        } catch (PackageManagerException e) {
            throw new PackageManagerExceptionEx((Throwable) e);
        }
    }

    public UserManagerInternalEx getUserManagerInternalInner() {
        return new UserManagerInternalEx();
    }

    public int installExistingPackageAsUserInternalInner(String packageName, int userId, int installFlags, int installReason) {
        PackageManagerService packageManagerService = this.mPackageManagerService;
        if (packageManagerService != null) {
            return packageManagerService.installExistingPackageAsUserInternalInner(packageName, userId, installFlags, installReason);
        }
        return -1;
    }

    public PackageParserEx.PackageEx scanPackageLIInner(File scanFile, int parseFlags, int scanFlags, long currentTime, UserHandleEx user, int hwFlags) throws PackageManagerExceptionEx {
        try {
            if (this.mPackageManagerService != null) {
                return new PackageParserEx.PackageEx(this.mPackageManagerService.scanPackageLIInner(scanFile, parseFlags, scanFlags, currentTime, user.getUserHandle(), hwFlags));
            }
            return null;
        } catch (PackageManagerException e) {
            throw new PackageManagerExceptionEx((Throwable) e);
        }
    }

    public boolean setWhitelistedRestrictedPermissionsInner(String packageName, List<String> permissions, int whitelistFlag, int userId) {
        PackageManagerService packageManagerService = this.mPackageManagerService;
        if (packageManagerService != null) {
            return packageManagerService.setWhitelistedRestrictedPermissionsInner(packageName, permissions, whitelistFlag, userId);
        }
        return false;
    }

    public void updateSharedLibrariesLPrInner(PackageParserEx.PackageEx pkg, PackageParserEx.PackageEx changingLib) throws PackageManagerExceptionEx {
        try {
            if (this.mPackageManagerService != null) {
                PackageParser.Package targetPkg = null;
                if (pkg != null) {
                    targetPkg = (PackageParser.Package) pkg.getPackage();
                }
                if (targetPkg != null) {
                    PackageParser.Package targetChangingLib = null;
                    if (changingLib != null) {
                        targetChangingLib = (PackageParser.Package) changingLib.getPackage();
                    }
                    this.mPackageManagerService.updateSharedLibrariesLPrInner(targetPkg, targetChangingLib);
                }
            }
        } catch (PackageManagerException e) {
            throw new PackageManagerExceptionEx((Throwable) e);
        }
    }

    public void updateSettingsLIInner(PackageParserEx.PackageEx newPackage, String installerPackageName, int[] allUsers, PackageInstalledInfoEx res, UserHandleEx user, int installReason) {
        PackageManagerService packageManagerService = this.mPackageManagerService;
        if (packageManagerService != null) {
            packageManagerService.updateSettingsLIInner((PackageParser.Package) newPackage.getPackage(), installerPackageName, allUsers, res.getPackageInstalledInfo(), user.getUserHandle(), installReason);
        }
    }

    public void prepareAppDataAfterInstallLIFInner(PackageParserEx.PackageEx pkg) {
        if (this.mPackageManagerService != null) {
            PackageParser.Package targetPkg = null;
            if (pkg != null) {
                targetPkg = (PackageParser.Package) pkg.getPackage();
            }
            if (targetPkg != null) {
                this.mPackageManagerService.prepareAppDataAfterInstallLIFInner(targetPkg);
            }
        }
    }

    public void sendPackageBroadcastInner(String action, String pkg, Bundle extras, int flags, String targetPkg, IIntentReceiverEx finishedReceiver, int[] userIds, int[] instantUserIds) {
        PackageManagerService packageManagerService = this.mPackageManagerService;
        if (packageManagerService != null) {
            packageManagerService.sendPackageBroadcastInner(action, pkg, extras, flags, targetPkg, finishedReceiver != null ? finishedReceiver.getIntentReceiver() : null, userIds, instantUserIds);
        }
    }

    public boolean getSystemReadyInner() {
        PackageManagerService packageManagerService = this.mPackageManagerService;
        if (packageManagerService != null) {
            return packageManagerService.getSystemReadyInner();
        }
        return false;
    }

    public int getUidTargetSdkVersionLockedLPrEx(int userId) {
        PackageManagerService packageManagerService = this.mPackageManagerService;
        if (packageManagerService != null) {
            return packageManagerService.getUidTargetSdkVersionLockedLPrEx(userId);
        }
        return -1;
    }

    public void writePackageRestrictions(int userId) {
        PackageManagerService packageManagerService = this.mPackageManagerService;
        if (packageManagerService != null) {
            packageManagerService.WritePackageRestrictions(userId);
        }
    }

    public void sendPreferredActivityChangedBroadcast(int userId) {
        PackageManagerService packageManagerService = this.mPackageManagerService;
        if (packageManagerService != null) {
            packageManagerService.sendPreferredActivityChangedBroadcast(userId);
        }
    }

    public PackageSettingEx getPackageSettingByPackageName(String packageName) {
        if (this.mPackageManagerService == null) {
            return new PackageSettingEx();
        }
        PackageSettingEx packageSettingEx = new PackageSettingEx();
        packageSettingEx.setPackageSetting(this.mPackageManagerService.getPackageSettingByPackageName(packageName));
        return packageSettingEx;
    }

    public PackageInfoEx getPackageInfo(String packageName, int flags, int userId) {
        PackageInfo packageInfo;
        PackageManagerService packageManagerService = this.mPackageManagerService;
        if (packageManagerService == null || (packageInfo = packageManagerService.getPackageInfo(packageName, flags, userId)) == null) {
            return null;
        }
        PackageInfoEx packageInfoEx = new PackageInfoEx();
        packageInfoEx.setmPackageInfo(packageInfo);
        return packageInfoEx;
    }

    public int getApplicationEnabledSetting(String packageName, int userId) {
        PackageManagerService packageManagerService = this.mPackageManagerService;
        if (packageManagerService != null) {
            return packageManagerService.getApplicationEnabledSetting(packageName, userId);
        }
        return -1;
    }

    public void setApplicationEnabledSetting(String appPackageName, int newState, int flags, int userId, String callingPackage) {
        PackageManagerService packageManagerService = this.mPackageManagerService;
        if (packageManagerService != null) {
            packageManagerService.setApplicationEnabledSetting(appPackageName, newState, flags, userId, callingPackage);
        }
    }

    public void setComponentEnabledSetting(ComponentName componentName, int newState, int flags, int userId) {
        PackageManagerService packageManagerService = this.mPackageManagerService;
        if (packageManagerService != null) {
            packageManagerService.setComponentEnabledSetting(componentName, newState, flags, userId);
        }
    }

    public PermissionManagerServiceInternalEx.PermissionCallbackEx getPermissionCallbackEx() {
        return this.mPermissionCallbackEx;
    }

    public List<ResolveInfoEx> queryIntentActivitiesInternal(Intent intent, String resolvedType, int flags, int userId) {
        PackageManagerService packageManagerService = this.mPackageManagerService;
        if (packageManagerService == null) {
            return null;
        }
        List<ResolveInfo> resolveInfos = packageManagerService.queryIntentActivitiesInternal(intent, resolvedType, flags, userId);
        List<ResolveInfoEx> resolveInfoExList = new ArrayList<>();
        for (ResolveInfo info : resolveInfos) {
            ResolveInfoEx infoEx = new ResolveInfoEx();
            infoEx.setResolveInfo(info);
            resolveInfoExList.add(infoEx);
        }
        return resolveInfoExList;
    }

    public void deletePackageVersioned(VersionedPackageEx versionedPackage, IPackageDeleteObserver2Ex observer, int userId, int deleteFlags) {
        this.mPackageManagerService.deletePackageVersioned(versionedPackage.getVersionedPackage(), observer.getPackageDeleteObserver(), userId, deleteFlags);
    }

    public boolean setSystemAppInstallState(String packageName, boolean installed, int userId) {
        return this.mPackageManagerService.setSystemAppInstallState(packageName, installed, userId);
    }

    public void scanDirLIInner(File scanDir, int parseFlags, int scanFlags, long currentTime, int hwFlags) {
        this.mPackageManagerService.scanDirLIInner(scanDir, parseFlags, scanFlags, currentTime, hwFlags);
    }

    public ArrayMap<String, FeatureInfo> getAvailableFeaturesInner() {
        return this.mPackageManagerService.getAvailableFeaturesInner();
    }

    public static UserManagerServiceEx getUserManager() {
        UserManagerServiceEx userManagerServiceEx = new UserManagerServiceEx();
        userManagerServiceEx.setUserManagerService(PackageManagerService.sUserManager);
        return userManagerServiceEx;
    }

    public boolean isStorageLow() {
        return this.mPackageManagerService.isStorageLow();
    }

    public void deleteNonRequiredAppsForClone(int clonedProfileUserId, boolean isFirstCreate) {
        this.mPackageManagerService.deleteNonRequiredAppsForClone(clonedProfileUserId, isFirstCreate);
    }

    public void flushPackageRestrictionsAsUser(int userId) {
        this.mPackageManagerService.flushPackageRestrictionsAsUser(userId);
    }

    public void setPackageStoppedState(String packageName, boolean stopped, int userId) {
        this.mPackageManagerService.setPackageStoppedState(packageName, stopped, userId);
    }

    public int installExistingPackageAsUser(String packageName, int userId, int installFlags, int installReason, List<String> whiteListedPermissions) {
        return this.mPackageManagerService.installExistingPackageAsUser(packageName, userId, installFlags, installReason, whiteListedPermissions);
    }

    public HashMap<String, HashSet<String>> getHwPMSCotaDelInstallMap() {
        return this.mPackageManagerService.getHwPMSCotaDelInstallMap();
    }

    public HashMap<String, HashSet<String>> getHwPMSCotaInstallMap() {
        return this.mPackageManagerService.getHwPMSCotaInstallMap();
    }

    public boolean getIsPreNUpgradeInner() {
        return this.mPackageManagerService.getIsPreNUpgradeInner();
    }

    public ApplicationInfo getApplicationInfo(String packageName, int flags, int userId) {
        return this.mPackageManagerService.getApplicationInfo(packageName, flags, userId);
    }
}
