package android.content.pm;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.IDexModuleRegisterCallback;
import android.content.pm.IOnPermissionsChangeListener;
import android.content.pm.IPackageDataObserver;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.IPackageDeleteObserver2;
import android.content.pm.IPackageInstaller;
import android.content.pm.IPackageMoveObserver;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.dex.IArtManager;
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.text.TextUtils;
import java.util.ArrayList;
import java.util.List;

public interface IPackageManager extends IInterface {

    public static abstract class Stub extends Binder implements IPackageManager {
        private static final String DESCRIPTOR = "android.content.pm.IPackageManager";
        static final int TRANSACTION_activitySupportsIntent = 15;
        static final int TRANSACTION_addCrossProfileIntentFilter = 75;
        static final int TRANSACTION_addOnPermissionsChangeListener = 157;
        static final int TRANSACTION_addPermission = 21;
        static final int TRANSACTION_addPermissionAsync = 128;
        static final int TRANSACTION_addPersistentPreferredActivity = 73;
        static final int TRANSACTION_addPreferredActivity = 69;
        static final int TRANSACTION_canForwardTo = 44;
        static final int TRANSACTION_canRequestPackageInstalls = 180;
        static final int TRANSACTION_canonicalToCurrentPackageNames = 8;
        static final int TRANSACTION_checkPackageStartable = 1;
        static final int TRANSACTION_checkPermission = 19;
        static final int TRANSACTION_checkSignatures = 31;
        static final int TRANSACTION_checkUidPermission = 20;
        static final int TRANSACTION_checkUidSignatures = 32;
        static final int TRANSACTION_clearApplicationProfileData = 102;
        static final int TRANSACTION_clearApplicationUserData = 101;
        static final int TRANSACTION_clearCrossProfileIntentFilters = 76;
        static final int TRANSACTION_clearPackagePersistentPreferredActivities = 74;
        static final int TRANSACTION_clearPackagePreferredActivities = 71;
        static final int TRANSACTION_currentToCanonicalPackageNames = 7;
        static final int TRANSACTION_deleteApplicationCacheFiles = 99;
        static final int TRANSACTION_deleteApplicationCacheFilesAsUser = 100;
        static final int TRANSACTION_deletePackageAsUser = 63;
        static final int TRANSACTION_deletePackageVersioned = 64;
        static final int TRANSACTION_deletePreloadsFileCache = 181;
        static final int TRANSACTION_dumpProfiles = 118;
        static final int TRANSACTION_enterSafeMode = 107;
        static final int TRANSACTION_extendVerificationTimeout = 133;
        static final int TRANSACTION_findPersistentPreferredActivity = 43;
        static final int TRANSACTION_finishPackageInstall = 60;
        static final int TRANSACTION_flushPackageRestrictionsAsUser = 95;
        static final int TRANSACTION_forceDexOpt = 119;
        static final int TRANSACTION_freeStorage = 98;
        static final int TRANSACTION_freeStorageAndNotify = 97;
        static final int TRANSACTION_getActivityInfo = 14;
        static final int TRANSACTION_getAllIntentFilters = 138;
        static final int TRANSACTION_getAllPackages = 33;
        static final int TRANSACTION_getAllPermissionGroups = 12;
        static final int TRANSACTION_getAppOpPermissionPackages = 41;
        static final int TRANSACTION_getApplicationEnabledSetting = 93;
        static final int TRANSACTION_getApplicationHiddenSettingAsUser = 149;
        static final int TRANSACTION_getApplicationInfo = 13;
        static final int TRANSACTION_getArtManager = 186;
        static final int TRANSACTION_getBlockUninstallForUser = 152;
        static final int TRANSACTION_getChangedPackages = 176;
        static final int TRANSACTION_getComponentEnabledSetting = 91;
        static final int TRANSACTION_getDefaultAppsBackup = 82;
        static final int TRANSACTION_getDefaultBrowserPackageName = 140;
        static final int TRANSACTION_getFlagsForUid = 38;
        static final int TRANSACTION_getHarmfulAppWarning = 188;
        static final int TRANSACTION_getHomeActivities = 88;
        static final int TRANSACTION_getHwInnerService = 193;
        static final int TRANSACTION_getInstallLocation = 130;
        static final int TRANSACTION_getInstallReason = 178;
        static final int TRANSACTION_getInstalledApplications = 53;
        static final int TRANSACTION_getInstalledPackages = 51;
        static final int TRANSACTION_getInstallerPackageName = 65;
        static final int TRANSACTION_getInstantAppAndroidId = 185;
        static final int TRANSACTION_getInstantAppCookie = 168;
        static final int TRANSACTION_getInstantAppIcon = 170;
        static final int TRANSACTION_getInstantAppInstallerComponent = 184;
        static final int TRANSACTION_getInstantAppResolverComponent = 182;
        static final int TRANSACTION_getInstantAppResolverSettingsComponent = 183;
        static final int TRANSACTION_getInstantApps = 167;
        static final int TRANSACTION_getInstrumentationInfo = 58;
        static final int TRANSACTION_getIntentFilterVerificationBackup = 84;
        static final int TRANSACTION_getIntentFilterVerifications = 137;
        static final int TRANSACTION_getIntentVerificationStatus = 135;
        static final int TRANSACTION_getKeySetByAlias = 153;
        static final int TRANSACTION_getLastChosenActivity = 67;
        static final int TRANSACTION_getMoveStatus = 123;
        static final int TRANSACTION_getNameForUid = 35;
        static final int TRANSACTION_getNamesForUids = 36;
        static final int TRANSACTION_getPackageGids = 6;
        static final int TRANSACTION_getPackageInfo = 3;
        static final int TRANSACTION_getPackageInfoVersioned = 4;
        static final int TRANSACTION_getPackageInstaller = 150;
        static final int TRANSACTION_getPackageSizeInfo = 103;
        static final int TRANSACTION_getPackageUid = 5;
        static final int TRANSACTION_getPackagesForUid = 34;
        static final int TRANSACTION_getPackagesHoldingPermissions = 52;
        static final int TRANSACTION_getPermissionControllerPackageName = 166;
        static final int TRANSACTION_getPermissionFlags = 26;
        static final int TRANSACTION_getPermissionGrantBackup = 86;
        static final int TRANSACTION_getPermissionGroupInfo = 11;
        static final int TRANSACTION_getPermissionInfo = 9;
        static final int TRANSACTION_getPersistentApplications = 54;
        static final int TRANSACTION_getPreferredActivities = 72;
        static final int TRANSACTION_getPreferredActivityBackup = 80;
        static final int TRANSACTION_getPrivateFlagsForUid = 39;
        static final int TRANSACTION_getProviderInfo = 18;
        static final int TRANSACTION_getReceiverInfo = 16;
        static final int TRANSACTION_getServiceInfo = 17;
        static final int TRANSACTION_getServicesSystemSharedLibraryPackageName = 174;
        static final int TRANSACTION_getSharedLibraries = 179;
        static final int TRANSACTION_getSharedSystemSharedLibraryPackageName = 175;
        static final int TRANSACTION_getSigningKeySet = 154;
        static final int TRANSACTION_getSuspendedPackageAppExtras = 79;
        static final int TRANSACTION_getSystemAvailableFeatures = 105;
        static final int TRANSACTION_getSystemSharedLibraryNames = 104;
        static final int TRANSACTION_getSystemTextClassifierPackageName = 191;
        static final int TRANSACTION_getUidForSharedUser = 37;
        static final int TRANSACTION_getVerifierDeviceIdentity = 141;
        static final int TRANSACTION_grantDefaultPermissionsToActiveLuiApp = 163;
        static final int TRANSACTION_grantDefaultPermissionsToEnabledCarrierApps = 159;
        static final int TRANSACTION_grantDefaultPermissionsToEnabledImsServices = 160;
        static final int TRANSACTION_grantDefaultPermissionsToEnabledTelephonyDataServices = 161;
        static final int TRANSACTION_grantRuntimePermission = 23;
        static final int TRANSACTION_hasSigningCertificate = 189;
        static final int TRANSACTION_hasSystemFeature = 106;
        static final int TRANSACTION_hasSystemUidErrors = 110;
        static final int TRANSACTION_hasUidSigningCertificate = 190;
        static final int TRANSACTION_installExistingPackageAsUser = 131;
        static final int TRANSACTION_isFirstBoot = 142;
        static final int TRANSACTION_isInstantApp = 171;
        static final int TRANSACTION_isOnlyCoreApps = 143;
        static final int TRANSACTION_isPackageAvailable = 2;
        static final int TRANSACTION_isPackageDeviceAdminOnAnyUser = 177;
        static final int TRANSACTION_isPackageSignedByKeySet = 155;
        static final int TRANSACTION_isPackageSignedByKeySetExactly = 156;
        static final int TRANSACTION_isPackageStateProtected = 192;
        static final int TRANSACTION_isPackageSuspendedForUser = 78;
        static final int TRANSACTION_isPermissionEnforced = 146;
        static final int TRANSACTION_isPermissionRevokedByPolicy = 165;
        static final int TRANSACTION_isProtectedBroadcast = 30;
        static final int TRANSACTION_isSafeMode = 108;
        static final int TRANSACTION_isStorageLow = 147;
        static final int TRANSACTION_isUidPrivileged = 40;
        static final int TRANSACTION_isUpgrade = 144;
        static final int TRANSACTION_logAppProcessStartIfNeeded = 94;
        static final int TRANSACTION_movePackage = 126;
        static final int TRANSACTION_movePrimaryStorage = 127;
        static final int TRANSACTION_nextPackageToClean = 122;
        static final int TRANSACTION_notifyDexLoad = 114;
        static final int TRANSACTION_notifyPackageUse = 113;
        static final int TRANSACTION_performDexOptMode = 116;
        static final int TRANSACTION_performDexOptSecondary = 117;
        static final int TRANSACTION_performFstrimIfNeeded = 111;
        static final int TRANSACTION_queryContentProviders = 57;
        static final int TRANSACTION_queryInstrumentation = 59;
        static final int TRANSACTION_queryIntentActivities = 45;
        static final int TRANSACTION_queryIntentActivityOptions = 46;
        static final int TRANSACTION_queryIntentContentProviders = 50;
        static final int TRANSACTION_queryIntentReceivers = 47;
        static final int TRANSACTION_queryIntentServices = 49;
        static final int TRANSACTION_queryPermissionsByGroup = 10;
        static final int TRANSACTION_querySyncProviders = 56;
        static final int TRANSACTION_reconcileSecondaryDexFiles = 121;
        static final int TRANSACTION_registerDexModule = 115;
        static final int TRANSACTION_registerMoveCallback = 124;
        static final int TRANSACTION_removeOnPermissionsChangeListener = 158;
        static final int TRANSACTION_removePermission = 22;
        static final int TRANSACTION_replacePreferredActivity = 70;
        static final int TRANSACTION_resetApplicationPreferences = 66;
        static final int TRANSACTION_resetRuntimePermissions = 25;
        static final int TRANSACTION_resolveContentProvider = 55;
        static final int TRANSACTION_resolveIntent = 42;
        static final int TRANSACTION_resolveService = 48;
        static final int TRANSACTION_restoreDefaultApps = 83;
        static final int TRANSACTION_restoreIntentFilterVerification = 85;
        static final int TRANSACTION_restorePermissionGrants = 87;
        static final int TRANSACTION_restorePreferredActivities = 81;
        static final int TRANSACTION_revokeDefaultPermissionsFromDisabledTelephonyDataServices = 162;
        static final int TRANSACTION_revokeDefaultPermissionsFromLuiApps = 164;
        static final int TRANSACTION_revokeRuntimePermission = 24;
        static final int TRANSACTION_runBackgroundDexoptJob = 120;
        static final int TRANSACTION_setApplicationCategoryHint = 62;
        static final int TRANSACTION_setApplicationEnabledSetting = 92;
        static final int TRANSACTION_setApplicationHiddenSettingAsUser = 148;
        static final int TRANSACTION_setBlockUninstallForUser = 151;
        static final int TRANSACTION_setComponentEnabledSetting = 90;
        static final int TRANSACTION_setDefaultBrowserPackageName = 139;
        static final int TRANSACTION_setHarmfulAppWarning = 187;
        static final int TRANSACTION_setHomeActivity = 89;
        static final int TRANSACTION_setInstallLocation = 129;
        static final int TRANSACTION_setInstallerPackageName = 61;
        static final int TRANSACTION_setInstantAppCookie = 169;
        static final int TRANSACTION_setLastChosenActivity = 68;
        static final int TRANSACTION_setPackageStoppedState = 96;
        static final int TRANSACTION_setPackagesSuspendedAsUser = 77;
        static final int TRANSACTION_setPermissionEnforced = 145;
        static final int TRANSACTION_setRequiredForSystemUser = 172;
        static final int TRANSACTION_setUpdateAvailable = 173;
        static final int TRANSACTION_shouldShowRequestPermissionRationale = 29;
        static final int TRANSACTION_systemReady = 109;
        static final int TRANSACTION_unregisterMoveCallback = 125;
        static final int TRANSACTION_updateIntentVerificationStatus = 136;
        static final int TRANSACTION_updatePackagesIfNeeded = 112;
        static final int TRANSACTION_updatePermissionFlags = 27;
        static final int TRANSACTION_updatePermissionFlagsForAllApps = 28;
        static final int TRANSACTION_verifyIntentFilter = 134;
        static final int TRANSACTION_verifyPendingInstall = 132;

        private static class Proxy implements IPackageManager {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            public void checkPackageStartable(String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isPackageAvailable(String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    boolean _result = false;
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public PackageInfo getPackageInfo(String packageName, int flags, int userId) throws RemoteException {
                PackageInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(flags);
                    _data.writeInt(userId);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = PackageInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public PackageInfo getPackageInfoVersioned(VersionedPackage versionedPackage, int flags, int userId) throws RemoteException {
                PackageInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (versionedPackage != null) {
                        _data.writeInt(1);
                        versionedPackage.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(flags);
                    _data.writeInt(userId);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = PackageInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getPackageUid(String packageName, int flags, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(flags);
                    _data.writeInt(userId);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int[] getPackageGids(String packageName, int flags, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(flags);
                    _data.writeInt(userId);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createIntArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String[] currentToCanonicalPackageNames(String[] names) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringArray(names);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String[] canonicalToCurrentPackageNames(String[] names) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringArray(names);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public PermissionInfo getPermissionInfo(String name, String packageName, int flags) throws RemoteException {
                PermissionInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    _data.writeString(packageName);
                    _data.writeInt(flags);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = PermissionInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParceledListSlice queryPermissionsByGroup(String group, int flags) throws RemoteException {
                ParceledListSlice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(group);
                    _data.writeInt(flags);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public PermissionGroupInfo getPermissionGroupInfo(String name, int flags) throws RemoteException {
                PermissionGroupInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    _data.writeInt(flags);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = PermissionGroupInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParceledListSlice getAllPermissionGroups(int flags) throws RemoteException {
                ParceledListSlice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(flags);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ApplicationInfo getApplicationInfo(String packageName, int flags, int userId) throws RemoteException {
                ApplicationInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(flags);
                    _data.writeInt(userId);
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ApplicationInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ActivityInfo getActivityInfo(ComponentName className, int flags, int userId) throws RemoteException {
                ActivityInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (className != null) {
                        _data.writeInt(1);
                        className.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(flags);
                    _data.writeInt(userId);
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ActivityInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean activitySupportsIntent(ComponentName className, Intent intent, String resolvedType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (className != null) {
                        _data.writeInt(1);
                        className.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (intent != null) {
                        _data.writeInt(1);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(resolvedType);
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ActivityInfo getReceiverInfo(ComponentName className, int flags, int userId) throws RemoteException {
                ActivityInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (className != null) {
                        _data.writeInt(1);
                        className.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(flags);
                    _data.writeInt(userId);
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ActivityInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ServiceInfo getServiceInfo(ComponentName className, int flags, int userId) throws RemoteException {
                ServiceInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (className != null) {
                        _data.writeInt(1);
                        className.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(flags);
                    _data.writeInt(userId);
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ServiceInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ProviderInfo getProviderInfo(ComponentName className, int flags, int userId) throws RemoteException {
                ProviderInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (className != null) {
                        _data.writeInt(1);
                        className.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(flags);
                    _data.writeInt(userId);
                    this.mRemote.transact(18, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ProviderInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int checkPermission(String permName, String pkgName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(permName);
                    _data.writeString(pkgName);
                    _data.writeInt(userId);
                    this.mRemote.transact(19, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int checkUidPermission(String permName, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(permName);
                    _data.writeInt(uid);
                    this.mRemote.transact(20, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean addPermission(PermissionInfo info) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (info != null) {
                        _data.writeInt(1);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(21, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removePermission(String name) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    this.mRemote.transact(22, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void grantRuntimePermission(String packageName, String permissionName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(permissionName);
                    _data.writeInt(userId);
                    this.mRemote.transact(23, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void revokeRuntimePermission(String packageName, String permissionName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(permissionName);
                    _data.writeInt(userId);
                    this.mRemote.transact(24, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void resetRuntimePermissions() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(25, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getPermissionFlags(String permissionName, String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(permissionName);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    this.mRemote.transact(26, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void updatePermissionFlags(String permissionName, String packageName, int flagMask, int flagValues, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(permissionName);
                    _data.writeString(packageName);
                    _data.writeInt(flagMask);
                    _data.writeInt(flagValues);
                    _data.writeInt(userId);
                    this.mRemote.transact(27, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void updatePermissionFlagsForAllApps(int flagMask, int flagValues, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(flagMask);
                    _data.writeInt(flagValues);
                    _data.writeInt(userId);
                    this.mRemote.transact(28, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean shouldShowRequestPermissionRationale(String permissionName, String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(permissionName);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    boolean _result = false;
                    this.mRemote.transact(29, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isProtectedBroadcast(String actionName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(actionName);
                    boolean _result = false;
                    this.mRemote.transact(30, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int checkSignatures(String pkg1, String pkg2) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg1);
                    _data.writeString(pkg2);
                    this.mRemote.transact(31, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int checkUidSignatures(int uid1, int uid2) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid1);
                    _data.writeInt(uid2);
                    this.mRemote.transact(32, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> getAllPackages() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(33, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArrayList();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String[] getPackagesForUid(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    this.mRemote.transact(34, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getNameForUid(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    this.mRemote.transact(35, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String[] getNamesForUids(int[] uids) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeIntArray(uids);
                    this.mRemote.transact(36, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getUidForSharedUser(String sharedUserName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(sharedUserName);
                    this.mRemote.transact(37, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getFlagsForUid(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    this.mRemote.transact(38, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getPrivateFlagsForUid(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    this.mRemote.transact(39, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isUidPrivileged(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    boolean _result = false;
                    this.mRemote.transact(40, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String[] getAppOpPermissionPackages(String permissionName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(permissionName);
                    this.mRemote.transact(41, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ResolveInfo resolveIntent(Intent intent, String resolvedType, int flags, int userId) throws RemoteException {
                ResolveInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (intent != null) {
                        _data.writeInt(1);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(resolvedType);
                    _data.writeInt(flags);
                    _data.writeInt(userId);
                    this.mRemote.transact(42, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ResolveInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ResolveInfo findPersistentPreferredActivity(Intent intent, int userId) throws RemoteException {
                ResolveInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (intent != null) {
                        _data.writeInt(1);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    this.mRemote.transact(43, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ResolveInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean canForwardTo(Intent intent, String resolvedType, int sourceUserId, int targetUserId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (intent != null) {
                        _data.writeInt(1);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(resolvedType);
                    _data.writeInt(sourceUserId);
                    _data.writeInt(targetUserId);
                    this.mRemote.transact(44, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParceledListSlice queryIntentActivities(Intent intent, String resolvedType, int flags, int userId) throws RemoteException {
                ParceledListSlice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (intent != null) {
                        _data.writeInt(1);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(resolvedType);
                    _data.writeInt(flags);
                    _data.writeInt(userId);
                    this.mRemote.transact(45, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParceledListSlice queryIntentActivityOptions(ComponentName caller, Intent[] specifics, String[] specificTypes, Intent intent, String resolvedType, int flags, int userId) throws RemoteException {
                ParceledListSlice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (caller != null) {
                        _data.writeInt(1);
                        caller.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeTypedArray(specifics, 0);
                    _data.writeStringArray(specificTypes);
                    if (intent != null) {
                        _data.writeInt(1);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(resolvedType);
                    _data.writeInt(flags);
                    _data.writeInt(userId);
                    this.mRemote.transact(46, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParceledListSlice queryIntentReceivers(Intent intent, String resolvedType, int flags, int userId) throws RemoteException {
                ParceledListSlice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (intent != null) {
                        _data.writeInt(1);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(resolvedType);
                    _data.writeInt(flags);
                    _data.writeInt(userId);
                    this.mRemote.transact(47, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ResolveInfo resolveService(Intent intent, String resolvedType, int flags, int userId) throws RemoteException {
                ResolveInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (intent != null) {
                        _data.writeInt(1);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(resolvedType);
                    _data.writeInt(flags);
                    _data.writeInt(userId);
                    this.mRemote.transact(48, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ResolveInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParceledListSlice queryIntentServices(Intent intent, String resolvedType, int flags, int userId) throws RemoteException {
                ParceledListSlice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (intent != null) {
                        _data.writeInt(1);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(resolvedType);
                    _data.writeInt(flags);
                    _data.writeInt(userId);
                    this.mRemote.transact(49, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParceledListSlice queryIntentContentProviders(Intent intent, String resolvedType, int flags, int userId) throws RemoteException {
                ParceledListSlice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (intent != null) {
                        _data.writeInt(1);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(resolvedType);
                    _data.writeInt(flags);
                    _data.writeInt(userId);
                    this.mRemote.transact(50, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParceledListSlice getInstalledPackages(int flags, int userId) throws RemoteException {
                ParceledListSlice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(flags);
                    _data.writeInt(userId);
                    this.mRemote.transact(51, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParceledListSlice getPackagesHoldingPermissions(String[] permissions, int flags, int userId) throws RemoteException {
                ParceledListSlice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringArray(permissions);
                    _data.writeInt(flags);
                    _data.writeInt(userId);
                    this.mRemote.transact(52, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParceledListSlice getInstalledApplications(int flags, int userId) throws RemoteException {
                ParceledListSlice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(flags);
                    _data.writeInt(userId);
                    this.mRemote.transact(53, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParceledListSlice getPersistentApplications(int flags) throws RemoteException {
                ParceledListSlice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(flags);
                    this.mRemote.transact(54, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ProviderInfo resolveContentProvider(String name, int flags, int userId) throws RemoteException {
                ProviderInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    _data.writeInt(flags);
                    _data.writeInt(userId);
                    this.mRemote.transact(55, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ProviderInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void querySyncProviders(List<String> outNames, List<ProviderInfo> outInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(outNames);
                    _data.writeTypedList(outInfo);
                    this.mRemote.transact(56, _data, _reply, 0);
                    _reply.readException();
                    _reply.readStringList(outNames);
                    _reply.readTypedList(outInfo, ProviderInfo.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParceledListSlice queryContentProviders(String processName, int uid, int flags, String metaDataKey) throws RemoteException {
                ParceledListSlice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(processName);
                    _data.writeInt(uid);
                    _data.writeInt(flags);
                    _data.writeString(metaDataKey);
                    this.mRemote.transact(57, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public InstrumentationInfo getInstrumentationInfo(ComponentName className, int flags) throws RemoteException {
                InstrumentationInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (className != null) {
                        _data.writeInt(1);
                        className.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(flags);
                    this.mRemote.transact(58, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = InstrumentationInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParceledListSlice queryInstrumentation(String targetPackage, int flags) throws RemoteException {
                ParceledListSlice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(targetPackage);
                    _data.writeInt(flags);
                    this.mRemote.transact(59, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void finishPackageInstall(int token, boolean didLaunch) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(token);
                    _data.writeInt(didLaunch);
                    this.mRemote.transact(60, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setInstallerPackageName(String targetPackage, String installerPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(targetPackage);
                    _data.writeString(installerPackageName);
                    this.mRemote.transact(61, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setApplicationCategoryHint(String packageName, int categoryHint, String callerPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(categoryHint);
                    _data.writeString(callerPackageName);
                    this.mRemote.transact(62, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void deletePackageAsUser(String packageName, int versionCode, IPackageDeleteObserver observer, int userId, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(versionCode);
                    _data.writeStrongBinder(observer != null ? observer.asBinder() : null);
                    _data.writeInt(userId);
                    _data.writeInt(flags);
                    this.mRemote.transact(63, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void deletePackageVersioned(VersionedPackage versionedPackage, IPackageDeleteObserver2 observer, int userId, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (versionedPackage != null) {
                        _data.writeInt(1);
                        versionedPackage.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(observer != null ? observer.asBinder() : null);
                    _data.writeInt(userId);
                    _data.writeInt(flags);
                    this.mRemote.transact(64, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getInstallerPackageName(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    this.mRemote.transact(65, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void resetApplicationPreferences(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(66, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ResolveInfo getLastChosenActivity(Intent intent, String resolvedType, int flags) throws RemoteException {
                ResolveInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (intent != null) {
                        _data.writeInt(1);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(resolvedType);
                    _data.writeInt(flags);
                    this.mRemote.transact(67, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ResolveInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setLastChosenActivity(Intent intent, String resolvedType, int flags, IntentFilter filter, int match, ComponentName activity) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (intent != null) {
                        _data.writeInt(1);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(resolvedType);
                    _data.writeInt(flags);
                    if (filter != null) {
                        _data.writeInt(1);
                        filter.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(match);
                    if (activity != null) {
                        _data.writeInt(1);
                        activity.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(68, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addPreferredActivity(IntentFilter filter, int match, ComponentName[] set, ComponentName activity, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (filter != null) {
                        _data.writeInt(1);
                        filter.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(match);
                    _data.writeTypedArray(set, 0);
                    if (activity != null) {
                        _data.writeInt(1);
                        activity.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    this.mRemote.transact(69, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void replacePreferredActivity(IntentFilter filter, int match, ComponentName[] set, ComponentName activity, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (filter != null) {
                        _data.writeInt(1);
                        filter.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(match);
                    _data.writeTypedArray(set, 0);
                    if (activity != null) {
                        _data.writeInt(1);
                        activity.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    this.mRemote.transact(70, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void clearPackagePreferredActivities(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    this.mRemote.transact(71, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getPreferredActivities(List<IntentFilter> outFilters, List<ComponentName> outActivities, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    this.mRemote.transact(72, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.readTypedList(outFilters, IntentFilter.CREATOR);
                    _reply.readTypedList(outActivities, ComponentName.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addPersistentPreferredActivity(IntentFilter filter, ComponentName activity, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (filter != null) {
                        _data.writeInt(1);
                        filter.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (activity != null) {
                        _data.writeInt(1);
                        activity.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    this.mRemote.transact(73, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void clearPackagePersistentPreferredActivities(String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    this.mRemote.transact(74, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addCrossProfileIntentFilter(IntentFilter intentFilter, String ownerPackage, int sourceUserId, int targetUserId, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (intentFilter != null) {
                        _data.writeInt(1);
                        intentFilter.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(ownerPackage);
                    _data.writeInt(sourceUserId);
                    _data.writeInt(targetUserId);
                    _data.writeInt(flags);
                    this.mRemote.transact(75, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void clearCrossProfileIntentFilters(int sourceUserId, String ownerPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sourceUserId);
                    _data.writeString(ownerPackage);
                    this.mRemote.transact(76, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String[] setPackagesSuspendedAsUser(String[] packageNames, boolean suspended, PersistableBundle appExtras, PersistableBundle launcherExtras, String dialogMessage, String callingPackage, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringArray(packageNames);
                    _data.writeInt(suspended);
                    if (appExtras != null) {
                        _data.writeInt(1);
                        appExtras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (launcherExtras != null) {
                        _data.writeInt(1);
                        launcherExtras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(dialogMessage);
                    _data.writeString(callingPackage);
                    _data.writeInt(userId);
                    this.mRemote.transact(77, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isPackageSuspendedForUser(String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    boolean _result = false;
                    this.mRemote.transact(78, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public PersistableBundle getSuspendedPackageAppExtras(String packageName, int userId) throws RemoteException {
                PersistableBundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    this.mRemote.transact(79, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (PersistableBundle) PersistableBundle.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public byte[] getPreferredActivityBackup(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(80, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createByteArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void restorePreferredActivities(byte[] backup, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(backup);
                    _data.writeInt(userId);
                    this.mRemote.transact(81, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public byte[] getDefaultAppsBackup(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(82, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createByteArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void restoreDefaultApps(byte[] backup, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(backup);
                    _data.writeInt(userId);
                    this.mRemote.transact(83, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public byte[] getIntentFilterVerificationBackup(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(84, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createByteArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void restoreIntentFilterVerification(byte[] backup, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(backup);
                    _data.writeInt(userId);
                    this.mRemote.transact(85, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public byte[] getPermissionGrantBackup(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(86, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createByteArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void restorePermissionGrants(byte[] backup, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(backup);
                    _data.writeInt(userId);
                    this.mRemote.transact(87, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ComponentName getHomeActivities(List<ResolveInfo> outHomeCandidates) throws RemoteException {
                ComponentName _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(88, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ComponentName.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.readTypedList(outHomeCandidates, ResolveInfo.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setHomeActivity(ComponentName className, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (className != null) {
                        _data.writeInt(1);
                        className.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    this.mRemote.transact(89, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setComponentEnabledSetting(ComponentName componentName, int newState, int flags, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (componentName != null) {
                        _data.writeInt(1);
                        componentName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(newState);
                    _data.writeInt(flags);
                    _data.writeInt(userId);
                    this.mRemote.transact(90, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getComponentEnabledSetting(ComponentName componentName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (componentName != null) {
                        _data.writeInt(1);
                        componentName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    this.mRemote.transact(91, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setApplicationEnabledSetting(String packageName, int newState, int flags, int userId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(newState);
                    _data.writeInt(flags);
                    _data.writeInt(userId);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(92, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getApplicationEnabledSetting(String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    this.mRemote.transact(93, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void logAppProcessStartIfNeeded(String processName, int uid, String seinfo, String apkFile, int pid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(processName);
                    _data.writeInt(uid);
                    _data.writeString(seinfo);
                    _data.writeString(apkFile);
                    _data.writeInt(pid);
                    this.mRemote.transact(94, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void flushPackageRestrictionsAsUser(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(95, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setPackageStoppedState(String packageName, boolean stopped, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(stopped);
                    _data.writeInt(userId);
                    this.mRemote.transact(96, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void freeStorageAndNotify(String volumeUuid, long freeStorageSize, int storageFlags, IPackageDataObserver observer) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(volumeUuid);
                    _data.writeLong(freeStorageSize);
                    _data.writeInt(storageFlags);
                    _data.writeStrongBinder(observer != null ? observer.asBinder() : null);
                    this.mRemote.transact(97, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void freeStorage(String volumeUuid, long freeStorageSize, int storageFlags, IntentSender pi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(volumeUuid);
                    _data.writeLong(freeStorageSize);
                    _data.writeInt(storageFlags);
                    if (pi != null) {
                        _data.writeInt(1);
                        pi.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(98, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void deleteApplicationCacheFiles(String packageName, IPackageDataObserver observer) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeStrongBinder(observer != null ? observer.asBinder() : null);
                    this.mRemote.transact(99, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void deleteApplicationCacheFilesAsUser(String packageName, int userId, IPackageDataObserver observer) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    _data.writeStrongBinder(observer != null ? observer.asBinder() : null);
                    this.mRemote.transact(100, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void clearApplicationUserData(String packageName, IPackageDataObserver observer, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeStrongBinder(observer != null ? observer.asBinder() : null);
                    _data.writeInt(userId);
                    this.mRemote.transact(101, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void clearApplicationProfileData(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    this.mRemote.transact(102, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void getPackageSizeInfo(String packageName, int userHandle, IPackageStatsObserver observer) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userHandle);
                    _data.writeStrongBinder(observer != null ? observer.asBinder() : null);
                    this.mRemote.transact(103, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String[] getSystemSharedLibraryNames() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(104, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParceledListSlice getSystemAvailableFeatures() throws RemoteException {
                ParceledListSlice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(105, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean hasSystemFeature(String name, int version) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    _data.writeInt(version);
                    boolean _result = false;
                    this.mRemote.transact(106, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void enterSafeMode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(107, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isSafeMode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(108, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void systemReady() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(109, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean hasSystemUidErrors() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(110, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void performFstrimIfNeeded() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(111, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void updatePackagesIfNeeded() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(112, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void notifyPackageUse(String packageName, int reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(reason);
                    this.mRemote.transact(113, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void notifyDexLoad(String loadingPackageName, List<String> classLoadersNames, List<String> classPaths, String loaderIsa) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(loadingPackageName);
                    _data.writeStringList(classLoadersNames);
                    _data.writeStringList(classPaths);
                    _data.writeString(loaderIsa);
                    this.mRemote.transact(114, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void registerDexModule(String packageName, String dexModulePath, boolean isSharedModule, IDexModuleRegisterCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(dexModulePath);
                    _data.writeInt(isSharedModule);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(115, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public boolean performDexOptMode(String packageName, boolean checkProfiles, String targetCompilerFilter, boolean force, boolean bootComplete, String splitName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(checkProfiles);
                    _data.writeString(targetCompilerFilter);
                    _data.writeInt(force);
                    _data.writeInt(bootComplete);
                    _data.writeString(splitName);
                    boolean _result = false;
                    this.mRemote.transact(116, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean performDexOptSecondary(String packageName, String targetCompilerFilter, boolean force) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(targetCompilerFilter);
                    _data.writeInt(force);
                    boolean _result = false;
                    this.mRemote.transact(117, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void dumpProfiles(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    this.mRemote.transact(118, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void forceDexOpt(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    this.mRemote.transact(119, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean runBackgroundDexoptJob(List<String> packageNames) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(packageNames);
                    boolean _result = false;
                    this.mRemote.transact(120, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void reconcileSecondaryDexFiles(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    this.mRemote.transact(121, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public PackageCleanItem nextPackageToClean(PackageCleanItem lastPackage) throws RemoteException {
                PackageCleanItem _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (lastPackage != null) {
                        _data.writeInt(1);
                        lastPackage.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(122, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = PackageCleanItem.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getMoveStatus(int moveId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(moveId);
                    this.mRemote.transact(123, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registerMoveCallback(IPackageMoveObserver callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(124, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unregisterMoveCallback(IPackageMoveObserver callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(125, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int movePackage(String packageName, String volumeUuid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(volumeUuid);
                    this.mRemote.transact(126, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int movePrimaryStorage(String volumeUuid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(volumeUuid);
                    this.mRemote.transact(127, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean addPermissionAsync(PermissionInfo info) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (info != null) {
                        _data.writeInt(1);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(128, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setInstallLocation(int loc) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(loc);
                    boolean _result = false;
                    this.mRemote.transact(129, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getInstallLocation() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(130, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int installExistingPackageAsUser(String packageName, int userId, int installFlags, int installReason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    _data.writeInt(installFlags);
                    _data.writeInt(installReason);
                    this.mRemote.transact(131, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void verifyPendingInstall(int id, int verificationCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(id);
                    _data.writeInt(verificationCode);
                    this.mRemote.transact(132, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void extendVerificationTimeout(int id, int verificationCodeAtTimeout, long millisecondsToDelay) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(id);
                    _data.writeInt(verificationCodeAtTimeout);
                    _data.writeLong(millisecondsToDelay);
                    this.mRemote.transact(133, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void verifyIntentFilter(int id, int verificationCode, List<String> failedDomains) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(id);
                    _data.writeInt(verificationCode);
                    _data.writeStringList(failedDomains);
                    this.mRemote.transact(134, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getIntentVerificationStatus(String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    this.mRemote.transact(135, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean updateIntentVerificationStatus(String packageName, int status, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(status);
                    _data.writeInt(userId);
                    boolean _result = false;
                    this.mRemote.transact(136, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParceledListSlice getIntentFilterVerifications(String packageName) throws RemoteException {
                ParceledListSlice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    this.mRemote.transact(137, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParceledListSlice getAllIntentFilters(String packageName) throws RemoteException {
                ParceledListSlice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    this.mRemote.transact(138, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setDefaultBrowserPackageName(String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    boolean _result = false;
                    this.mRemote.transact(139, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getDefaultBrowserPackageName(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(140, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public VerifierDeviceIdentity getVerifierDeviceIdentity() throws RemoteException {
                VerifierDeviceIdentity _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(141, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = VerifierDeviceIdentity.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isFirstBoot() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(142, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isOnlyCoreApps() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(143, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isUpgrade() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(144, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setPermissionEnforced(String permission, boolean enforced) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(permission);
                    _data.writeInt(enforced);
                    this.mRemote.transact(145, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isPermissionEnforced(String permission) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(permission);
                    boolean _result = false;
                    this.mRemote.transact(146, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isStorageLow() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(147, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setApplicationHiddenSettingAsUser(String packageName, boolean hidden, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(hidden);
                    _data.writeInt(userId);
                    boolean _result = false;
                    this.mRemote.transact(148, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean getApplicationHiddenSettingAsUser(String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    boolean _result = false;
                    this.mRemote.transact(149, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public IPackageInstaller getPackageInstaller() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(150, _data, _reply, 0);
                    _reply.readException();
                    return IPackageInstaller.Stub.asInterface(_reply.readStrongBinder());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setBlockUninstallForUser(String packageName, boolean blockUninstall, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(blockUninstall);
                    _data.writeInt(userId);
                    boolean _result = false;
                    this.mRemote.transact(151, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean getBlockUninstallForUser(String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    boolean _result = false;
                    this.mRemote.transact(152, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public KeySet getKeySetByAlias(String packageName, String alias) throws RemoteException {
                KeySet _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(alias);
                    this.mRemote.transact(153, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = KeySet.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public KeySet getSigningKeySet(String packageName) throws RemoteException {
                KeySet _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    this.mRemote.transact(154, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = KeySet.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isPackageSignedByKeySet(String packageName, KeySet ks) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    boolean _result = true;
                    if (ks != null) {
                        _data.writeInt(1);
                        ks.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(155, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isPackageSignedByKeySetExactly(String packageName, KeySet ks) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    boolean _result = true;
                    if (ks != null) {
                        _data.writeInt(1);
                        ks.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(156, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addOnPermissionsChangeListener(IOnPermissionsChangeListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    this.mRemote.transact(157, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeOnPermissionsChangeListener(IOnPermissionsChangeListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    this.mRemote.transact(158, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void grantDefaultPermissionsToEnabledCarrierApps(String[] packageNames, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringArray(packageNames);
                    _data.writeInt(userId);
                    this.mRemote.transact(159, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void grantDefaultPermissionsToEnabledImsServices(String[] packageNames, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringArray(packageNames);
                    _data.writeInt(userId);
                    this.mRemote.transact(160, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void grantDefaultPermissionsToEnabledTelephonyDataServices(String[] packageNames, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringArray(packageNames);
                    _data.writeInt(userId);
                    this.mRemote.transact(161, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void revokeDefaultPermissionsFromDisabledTelephonyDataServices(String[] packageNames, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringArray(packageNames);
                    _data.writeInt(userId);
                    this.mRemote.transact(162, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void grantDefaultPermissionsToActiveLuiApp(String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    this.mRemote.transact(163, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void revokeDefaultPermissionsFromLuiApps(String[] packageNames, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringArray(packageNames);
                    _data.writeInt(userId);
                    this.mRemote.transact(164, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isPermissionRevokedByPolicy(String permission, String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(permission);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    boolean _result = false;
                    this.mRemote.transact(165, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getPermissionControllerPackageName() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(166, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParceledListSlice getInstantApps(int userId) throws RemoteException {
                ParceledListSlice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(167, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public byte[] getInstantAppCookie(String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    this.mRemote.transact(168, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createByteArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setInstantAppCookie(String packageName, byte[] cookie, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeByteArray(cookie);
                    _data.writeInt(userId);
                    boolean _result = false;
                    this.mRemote.transact(169, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Bitmap getInstantAppIcon(String packageName, int userId) throws RemoteException {
                Bitmap _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    this.mRemote.transact(170, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Bitmap.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isInstantApp(String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    boolean _result = false;
                    this.mRemote.transact(171, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setRequiredForSystemUser(String packageName, boolean systemUserApp) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(systemUserApp);
                    boolean _result = false;
                    this.mRemote.transact(172, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setUpdateAvailable(String packageName, boolean updateAvaialble) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(updateAvaialble);
                    this.mRemote.transact(173, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getServicesSystemSharedLibraryPackageName() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(174, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getSharedSystemSharedLibraryPackageName() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(175, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ChangedPackages getChangedPackages(int sequenceNumber, int userId) throws RemoteException {
                ChangedPackages _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sequenceNumber);
                    _data.writeInt(userId);
                    this.mRemote.transact(176, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ChangedPackages.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isPackageDeviceAdminOnAnyUser(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    boolean _result = false;
                    this.mRemote.transact(177, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getInstallReason(String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    this.mRemote.transact(178, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParceledListSlice getSharedLibraries(String packageName, int flags, int userId) throws RemoteException {
                ParceledListSlice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(flags);
                    _data.writeInt(userId);
                    this.mRemote.transact(179, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean canRequestPackageInstalls(String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    boolean _result = false;
                    this.mRemote.transact(180, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void deletePreloadsFileCache() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(181, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ComponentName getInstantAppResolverComponent() throws RemoteException {
                ComponentName _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(182, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ComponentName.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ComponentName getInstantAppResolverSettingsComponent() throws RemoteException {
                ComponentName _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(183, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ComponentName.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ComponentName getInstantAppInstallerComponent() throws RemoteException {
                ComponentName _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(184, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ComponentName.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getInstantAppAndroidId(String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    this.mRemote.transact(185, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public IArtManager getArtManager() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(186, _data, _reply, 0);
                    _reply.readException();
                    return IArtManager.Stub.asInterface(_reply.readStrongBinder());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setHarmfulAppWarning(String packageName, CharSequence warning, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (warning != null) {
                        _data.writeInt(1);
                        TextUtils.writeToParcel(warning, _data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    this.mRemote.transact(187, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public CharSequence getHarmfulAppWarning(String packageName, int userId) throws RemoteException {
                CharSequence _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    this.mRemote.transact(188, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean hasSigningCertificate(String packageName, byte[] signingCertificate, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeByteArray(signingCertificate);
                    _data.writeInt(flags);
                    boolean _result = false;
                    this.mRemote.transact(189, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean hasUidSigningCertificate(int uid, byte[] signingCertificate, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeByteArray(signingCertificate);
                    _data.writeInt(flags);
                    boolean _result = false;
                    this.mRemote.transact(190, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getSystemTextClassifierPackageName() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(191, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isPackageStateProtected(String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    boolean _result = false;
                    this.mRemote.transact(192, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public IBinder getHwInnerService() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(193, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readStrongBinder();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IPackageManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IPackageManager)) {
                return new Proxy(obj);
            }
            return (IPackageManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v5, resolved type: android.content.pm.VersionedPackage} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v18, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v26, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v30, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v34, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v40, resolved type: android.content.pm.PermissionInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v108, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v118, resolved type: android.content.pm.VersionedPackage} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v145, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v173, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v177, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v181, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v220, resolved type: android.content.pm.PackageCleanItem} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v231, resolved type: android.content.pm.PermissionInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v262, resolved type: android.content.pm.KeySet} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v266, resolved type: android.content.pm.KeySet} */
        /* JADX WARNING: type inference failed for: r0v1 */
        /* JADX WARNING: type inference failed for: r0v22, types: [android.content.Intent] */
        /* JADX WARNING: type inference failed for: r0v63, types: [android.content.Intent] */
        /* JADX WARNING: type inference failed for: r0v67, types: [android.content.Intent] */
        /* JADX WARNING: type inference failed for: r0v71, types: [android.content.Intent] */
        /* JADX WARNING: type inference failed for: r0v75, types: [android.content.Intent] */
        /* JADX WARNING: type inference failed for: r0v79 */
        /* JADX WARNING: type inference failed for: r0v85, types: [android.content.Intent] */
        /* JADX WARNING: type inference failed for: r0v89, types: [android.content.Intent] */
        /* JADX WARNING: type inference failed for: r0v93, types: [android.content.Intent] */
        /* JADX WARNING: type inference failed for: r0v97, types: [android.content.Intent] */
        /* JADX WARNING: type inference failed for: r0v124, types: [android.content.Intent] */
        /* JADX WARNING: type inference failed for: r0v128 */
        /* JADX WARNING: type inference failed for: r0v133 */
        /* JADX WARNING: type inference failed for: r0v138 */
        /* JADX WARNING: type inference failed for: r0v150 */
        /* JADX WARNING: type inference failed for: r0v156 */
        /* JADX WARNING: type inference failed for: r0v192 */
        /* JADX WARNING: type inference failed for: r0v256, types: [android.os.IBinder] */
        /* JADX WARNING: type inference failed for: r0v300, types: [android.os.IBinder] */
        /* JADX WARNING: type inference failed for: r0v302, types: [java.lang.CharSequence] */
        /* JADX WARNING: type inference failed for: r0v313 */
        /* JADX WARNING: type inference failed for: r0v314 */
        /* JADX WARNING: type inference failed for: r0v315 */
        /* JADX WARNING: type inference failed for: r0v316 */
        /* JADX WARNING: type inference failed for: r0v317 */
        /* JADX WARNING: type inference failed for: r0v318 */
        /* JADX WARNING: type inference failed for: r0v319 */
        /* JADX WARNING: type inference failed for: r0v320 */
        /* JADX WARNING: type inference failed for: r0v321 */
        /* JADX WARNING: type inference failed for: r0v322 */
        /* JADX WARNING: type inference failed for: r0v323 */
        /* JADX WARNING: type inference failed for: r0v324 */
        /* JADX WARNING: type inference failed for: r0v325 */
        /* JADX WARNING: type inference failed for: r0v326 */
        /* JADX WARNING: type inference failed for: r0v327 */
        /* JADX WARNING: type inference failed for: r0v328 */
        /* JADX WARNING: type inference failed for: r0v329 */
        /* JADX WARNING: type inference failed for: r0v330 */
        /* JADX WARNING: type inference failed for: r0v331 */
        /* JADX WARNING: type inference failed for: r0v332 */
        /* JADX WARNING: type inference failed for: r0v333 */
        /* JADX WARNING: type inference failed for: r0v334 */
        /* JADX WARNING: type inference failed for: r0v335 */
        /* JADX WARNING: type inference failed for: r0v336 */
        /* JADX WARNING: type inference failed for: r0v337 */
        /* JADX WARNING: type inference failed for: r0v338 */
        /* JADX WARNING: type inference failed for: r0v339 */
        /* JADX WARNING: type inference failed for: r0v340 */
        /* JADX WARNING: type inference failed for: r0v341 */
        /* JADX WARNING: type inference failed for: r0v342 */
        /* JADX WARNING: type inference failed for: r0v343 */
        /* JADX WARNING: type inference failed for: r0v344 */
        /* JADX WARNING: type inference failed for: r0v345 */
        /* JADX WARNING: type inference failed for: r0v346 */
        /* JADX WARNING: type inference failed for: r0v347 */
        /* JADX WARNING: type inference failed for: r0v348 */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ComponentName _arg0;
            ComponentName _arg02;
            Intent _arg03;
            IntentFilter _arg3;
            IntentFilter _arg04;
            IntentFilter _arg05;
            IntentFilter _arg06;
            PersistableBundle _arg2;
            int i = code;
            Parcel parcel = data;
            Parcel parcel2 = reply;
            if (i != 1598968902) {
                ? _arg1 = 0;
                boolean _arg12 = false;
                switch (i) {
                    case 1:
                        parcel.enforceInterface(DESCRIPTOR);
                        checkPackageStartable(data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 2:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result = isPackageAvailable(data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result);
                        return true;
                    case 3:
                        parcel.enforceInterface(DESCRIPTOR);
                        PackageInfo _result2 = getPackageInfo(data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        if (_result2 != null) {
                            parcel2.writeInt(1);
                            _result2.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 4:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg1 = VersionedPackage.CREATOR.createFromParcel(parcel);
                        }
                        PackageInfo _result3 = getPackageInfoVersioned(_arg1, data.readInt(), data.readInt());
                        reply.writeNoException();
                        if (_result3 != null) {
                            parcel2.writeInt(1);
                            _result3.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 5:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result4 = getPackageUid(data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result4);
                        return true;
                    case 6:
                        parcel.enforceInterface(DESCRIPTOR);
                        int[] _result5 = getPackageGids(data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeIntArray(_result5);
                        return true;
                    case 7:
                        parcel.enforceInterface(DESCRIPTOR);
                        String[] _result6 = currentToCanonicalPackageNames(data.createStringArray());
                        reply.writeNoException();
                        parcel2.writeStringArray(_result6);
                        return true;
                    case 8:
                        parcel.enforceInterface(DESCRIPTOR);
                        String[] _result7 = canonicalToCurrentPackageNames(data.createStringArray());
                        reply.writeNoException();
                        parcel2.writeStringArray(_result7);
                        return true;
                    case 9:
                        parcel.enforceInterface(DESCRIPTOR);
                        PermissionInfo _result8 = getPermissionInfo(data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        if (_result8 != null) {
                            parcel2.writeInt(1);
                            _result8.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 10:
                        parcel.enforceInterface(DESCRIPTOR);
                        ParceledListSlice _result9 = queryPermissionsByGroup(data.readString(), data.readInt());
                        reply.writeNoException();
                        if (_result9 != null) {
                            parcel2.writeInt(1);
                            _result9.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 11:
                        parcel.enforceInterface(DESCRIPTOR);
                        PermissionGroupInfo _result10 = getPermissionGroupInfo(data.readString(), data.readInt());
                        reply.writeNoException();
                        if (_result10 != null) {
                            parcel2.writeInt(1);
                            _result10.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 12:
                        parcel.enforceInterface(DESCRIPTOR);
                        ParceledListSlice _result11 = getAllPermissionGroups(data.readInt());
                        reply.writeNoException();
                        if (_result11 != null) {
                            parcel2.writeInt(1);
                            _result11.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 13:
                        parcel.enforceInterface(DESCRIPTOR);
                        ApplicationInfo _result12 = getApplicationInfo(data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        if (_result12 != null) {
                            parcel2.writeInt(1);
                            _result12.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 14:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg1 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        ActivityInfo _result13 = getActivityInfo(_arg1, data.readInt(), data.readInt());
                        reply.writeNoException();
                        if (_result13 != null) {
                            parcel2.writeInt(1);
                            _result13.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 15:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = ComponentName.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg0 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg1 = Intent.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result14 = activitySupportsIntent(_arg0, _arg1, data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result14);
                        return true;
                    case 16:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg1 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        ActivityInfo _result15 = getReceiverInfo(_arg1, data.readInt(), data.readInt());
                        reply.writeNoException();
                        if (_result15 != null) {
                            parcel2.writeInt(1);
                            _result15.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 17:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg1 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        ServiceInfo _result16 = getServiceInfo(_arg1, data.readInt(), data.readInt());
                        reply.writeNoException();
                        if (_result16 != null) {
                            parcel2.writeInt(1);
                            _result16.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 18:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg1 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        ProviderInfo _result17 = getProviderInfo(_arg1, data.readInt(), data.readInt());
                        reply.writeNoException();
                        if (_result17 != null) {
                            parcel2.writeInt(1);
                            _result17.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 19:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result18 = checkPermission(data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result18);
                        return true;
                    case 20:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result19 = checkUidPermission(data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result19);
                        return true;
                    case 21:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg1 = PermissionInfo.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result20 = addPermission(_arg1);
                        reply.writeNoException();
                        parcel2.writeInt(_result20);
                        return true;
                    case 22:
                        parcel.enforceInterface(DESCRIPTOR);
                        removePermission(data.readString());
                        reply.writeNoException();
                        return true;
                    case 23:
                        parcel.enforceInterface(DESCRIPTOR);
                        grantRuntimePermission(data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 24:
                        parcel.enforceInterface(DESCRIPTOR);
                        revokeRuntimePermission(data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 25:
                        parcel.enforceInterface(DESCRIPTOR);
                        resetRuntimePermissions();
                        reply.writeNoException();
                        return true;
                    case 26:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result21 = getPermissionFlags(data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result21);
                        return true;
                    case 27:
                        parcel.enforceInterface(DESCRIPTOR);
                        updatePermissionFlags(data.readString(), data.readString(), data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 28:
                        parcel.enforceInterface(DESCRIPTOR);
                        updatePermissionFlagsForAllApps(data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 29:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result22 = shouldShowRequestPermissionRationale(data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result22);
                        return true;
                    case 30:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result23 = isProtectedBroadcast(data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result23);
                        return true;
                    case 31:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result24 = checkSignatures(data.readString(), data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result24);
                        return true;
                    case 32:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result25 = checkUidSignatures(data.readInt(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result25);
                        return true;
                    case 33:
                        parcel.enforceInterface(DESCRIPTOR);
                        List<String> _result26 = getAllPackages();
                        reply.writeNoException();
                        parcel2.writeStringList(_result26);
                        return true;
                    case 34:
                        parcel.enforceInterface(DESCRIPTOR);
                        String[] _result27 = getPackagesForUid(data.readInt());
                        reply.writeNoException();
                        parcel2.writeStringArray(_result27);
                        return true;
                    case 35:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result28 = getNameForUid(data.readInt());
                        reply.writeNoException();
                        parcel2.writeString(_result28);
                        return true;
                    case 36:
                        parcel.enforceInterface(DESCRIPTOR);
                        String[] _result29 = getNamesForUids(data.createIntArray());
                        reply.writeNoException();
                        parcel2.writeStringArray(_result29);
                        return true;
                    case 37:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result30 = getUidForSharedUser(data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result30);
                        return true;
                    case 38:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result31 = getFlagsForUid(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result31);
                        return true;
                    case 39:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result32 = getPrivateFlagsForUid(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result32);
                        return true;
                    case 40:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result33 = isUidPrivileged(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result33);
                        return true;
                    case 41:
                        parcel.enforceInterface(DESCRIPTOR);
                        String[] _result34 = getAppOpPermissionPackages(data.readString());
                        reply.writeNoException();
                        parcel2.writeStringArray(_result34);
                        return true;
                    case 42:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg1 = Intent.CREATOR.createFromParcel(parcel);
                        }
                        ResolveInfo _result35 = resolveIntent(_arg1, data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        if (_result35 != null) {
                            parcel2.writeInt(1);
                            _result35.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 43:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg1 = Intent.CREATOR.createFromParcel(parcel);
                        }
                        ResolveInfo _result36 = findPersistentPreferredActivity(_arg1, data.readInt());
                        reply.writeNoException();
                        if (_result36 != null) {
                            parcel2.writeInt(1);
                            _result36.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 44:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg1 = Intent.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result37 = canForwardTo(_arg1, data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result37);
                        return true;
                    case 45:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg1 = Intent.CREATOR.createFromParcel(parcel);
                        }
                        ParceledListSlice _result38 = queryIntentActivities(_arg1, data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        if (_result38 != null) {
                            parcel2.writeInt(1);
                            _result38.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 46:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = ComponentName.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg02 = null;
                        }
                        Intent[] _arg13 = (Intent[]) parcel.createTypedArray(Intent.CREATOR);
                        String[] _arg22 = data.createStringArray();
                        if (data.readInt() != 0) {
                            _arg1 = Intent.CREATOR.createFromParcel(parcel);
                        }
                        ParceledListSlice _result39 = queryIntentActivityOptions(_arg02, _arg13, _arg22, _arg1, data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        if (_result39 != null) {
                            parcel2.writeInt(1);
                            _result39.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 47:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg1 = Intent.CREATOR.createFromParcel(parcel);
                        }
                        ParceledListSlice _result40 = queryIntentReceivers(_arg1, data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        if (_result40 != null) {
                            parcel2.writeInt(1);
                            _result40.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 48:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg1 = Intent.CREATOR.createFromParcel(parcel);
                        }
                        ResolveInfo _result41 = resolveService(_arg1, data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        if (_result41 != null) {
                            parcel2.writeInt(1);
                            _result41.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 49:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg1 = Intent.CREATOR.createFromParcel(parcel);
                        }
                        ParceledListSlice _result42 = queryIntentServices(_arg1, data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        if (_result42 != null) {
                            parcel2.writeInt(1);
                            _result42.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 50:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg1 = Intent.CREATOR.createFromParcel(parcel);
                        }
                        ParceledListSlice _result43 = queryIntentContentProviders(_arg1, data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        if (_result43 != null) {
                            parcel2.writeInt(1);
                            _result43.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 51:
                        parcel.enforceInterface(DESCRIPTOR);
                        ParceledListSlice _result44 = getInstalledPackages(data.readInt(), data.readInt());
                        reply.writeNoException();
                        if (_result44 != null) {
                            parcel2.writeInt(1);
                            _result44.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 52:
                        parcel.enforceInterface(DESCRIPTOR);
                        ParceledListSlice _result45 = getPackagesHoldingPermissions(data.createStringArray(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        if (_result45 != null) {
                            parcel2.writeInt(1);
                            _result45.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 53:
                        parcel.enforceInterface(DESCRIPTOR);
                        ParceledListSlice _result46 = getInstalledApplications(data.readInt(), data.readInt());
                        reply.writeNoException();
                        if (_result46 != null) {
                            parcel2.writeInt(1);
                            _result46.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 54:
                        parcel.enforceInterface(DESCRIPTOR);
                        ParceledListSlice _result47 = getPersistentApplications(data.readInt());
                        reply.writeNoException();
                        if (_result47 != null) {
                            parcel2.writeInt(1);
                            _result47.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 55:
                        parcel.enforceInterface(DESCRIPTOR);
                        ProviderInfo _result48 = resolveContentProvider(data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        if (_result48 != null) {
                            parcel2.writeInt(1);
                            _result48.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 56:
                        parcel.enforceInterface(DESCRIPTOR);
                        List<String> _arg07 = data.createStringArrayList();
                        List<ProviderInfo> _arg14 = parcel.createTypedArrayList(ProviderInfo.CREATOR);
                        querySyncProviders(_arg07, _arg14);
                        reply.writeNoException();
                        parcel2.writeStringList(_arg07);
                        parcel2.writeTypedList(_arg14);
                        return true;
                    case 57:
                        parcel.enforceInterface(DESCRIPTOR);
                        ParceledListSlice _result49 = queryContentProviders(data.readString(), data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        if (_result49 != null) {
                            parcel2.writeInt(1);
                            _result49.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 58:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg1 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        InstrumentationInfo _result50 = getInstrumentationInfo(_arg1, data.readInt());
                        reply.writeNoException();
                        if (_result50 != null) {
                            parcel2.writeInt(1);
                            _result50.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 59:
                        parcel.enforceInterface(DESCRIPTOR);
                        ParceledListSlice _result51 = queryInstrumentation(data.readString(), data.readInt());
                        reply.writeNoException();
                        if (_result51 != null) {
                            parcel2.writeInt(1);
                            _result51.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 60:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg08 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg12 = true;
                        }
                        finishPackageInstall(_arg08, _arg12);
                        reply.writeNoException();
                        return true;
                    case 61:
                        parcel.enforceInterface(DESCRIPTOR);
                        setInstallerPackageName(data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 62:
                        parcel.enforceInterface(DESCRIPTOR);
                        setApplicationCategoryHint(data.readString(), data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 63:
                        parcel.enforceInterface(DESCRIPTOR);
                        deletePackageAsUser(data.readString(), data.readInt(), IPackageDeleteObserver.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 64:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg1 = VersionedPackage.CREATOR.createFromParcel(parcel);
                        }
                        deletePackageVersioned(_arg1, IPackageDeleteObserver2.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 65:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result52 = getInstallerPackageName(data.readString());
                        reply.writeNoException();
                        parcel2.writeString(_result52);
                        return true;
                    case 66:
                        parcel.enforceInterface(DESCRIPTOR);
                        resetApplicationPreferences(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 67:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg1 = Intent.CREATOR.createFromParcel(parcel);
                        }
                        ResolveInfo _result53 = getLastChosenActivity(_arg1, data.readString(), data.readInt());
                        reply.writeNoException();
                        if (_result53 != null) {
                            parcel2.writeInt(1);
                            _result53.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 68:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = Intent.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg03 = null;
                        }
                        String _arg15 = data.readString();
                        int _arg23 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg3 = IntentFilter.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg3 = null;
                        }
                        int _arg4 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        setLastChosenActivity(_arg03, _arg15, _arg23, _arg3, _arg4, _arg1);
                        reply.writeNoException();
                        return true;
                    case 69:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = IntentFilter.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg04 = null;
                        }
                        int _arg16 = data.readInt();
                        ComponentName[] _arg24 = (ComponentName[]) parcel.createTypedArray(ComponentName.CREATOR);
                        if (data.readInt() != 0) {
                            _arg1 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        addPreferredActivity(_arg04, _arg16, _arg24, _arg1, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 70:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg05 = IntentFilter.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg05 = null;
                        }
                        int _arg17 = data.readInt();
                        ComponentName[] _arg25 = (ComponentName[]) parcel.createTypedArray(ComponentName.CREATOR);
                        if (data.readInt() != 0) {
                            _arg1 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        replacePreferredActivity(_arg05, _arg17, _arg25, _arg1, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 71:
                        parcel.enforceInterface(DESCRIPTOR);
                        clearPackagePreferredActivities(data.readString());
                        reply.writeNoException();
                        return true;
                    case 72:
                        parcel.enforceInterface(DESCRIPTOR);
                        List<IntentFilter> _arg09 = new ArrayList<>();
                        List<ComponentName> _arg18 = new ArrayList<>();
                        int _result54 = getPreferredActivities(_arg09, _arg18, data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result54);
                        parcel2.writeTypedList(_arg09);
                        parcel2.writeTypedList(_arg18);
                        return true;
                    case 73:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg06 = IntentFilter.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg06 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg1 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        addPersistentPreferredActivity(_arg06, _arg1, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 74:
                        parcel.enforceInterface(DESCRIPTOR);
                        clearPackagePersistentPreferredActivities(data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 75:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg1 = IntentFilter.CREATOR.createFromParcel(parcel);
                        }
                        addCrossProfileIntentFilter(_arg1, data.readString(), data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 76:
                        parcel.enforceInterface(DESCRIPTOR);
                        clearCrossProfileIntentFilters(data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 77:
                        parcel.enforceInterface(DESCRIPTOR);
                        String[] _arg010 = data.createStringArray();
                        boolean _arg19 = data.readInt() != 0;
                        if (data.readInt() != 0) {
                            _arg2 = (PersistableBundle) PersistableBundle.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg2 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg1 = (PersistableBundle) PersistableBundle.CREATOR.createFromParcel(parcel);
                        }
                        String[] _result55 = setPackagesSuspendedAsUser(_arg010, _arg19, _arg2, _arg1, data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeStringArray(_result55);
                        return true;
                    case 78:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result56 = isPackageSuspendedForUser(data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result56);
                        return true;
                    case 79:
                        parcel.enforceInterface(DESCRIPTOR);
                        PersistableBundle _result57 = getSuspendedPackageAppExtras(data.readString(), data.readInt());
                        reply.writeNoException();
                        if (_result57 != null) {
                            parcel2.writeInt(1);
                            _result57.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 80:
                        parcel.enforceInterface(DESCRIPTOR);
                        byte[] _result58 = getPreferredActivityBackup(data.readInt());
                        reply.writeNoException();
                        parcel2.writeByteArray(_result58);
                        return true;
                    case 81:
                        parcel.enforceInterface(DESCRIPTOR);
                        restorePreferredActivities(data.createByteArray(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 82:
                        parcel.enforceInterface(DESCRIPTOR);
                        byte[] _result59 = getDefaultAppsBackup(data.readInt());
                        reply.writeNoException();
                        parcel2.writeByteArray(_result59);
                        return true;
                    case 83:
                        parcel.enforceInterface(DESCRIPTOR);
                        restoreDefaultApps(data.createByteArray(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 84:
                        parcel.enforceInterface(DESCRIPTOR);
                        byte[] _result60 = getIntentFilterVerificationBackup(data.readInt());
                        reply.writeNoException();
                        parcel2.writeByteArray(_result60);
                        return true;
                    case 85:
                        parcel.enforceInterface(DESCRIPTOR);
                        restoreIntentFilterVerification(data.createByteArray(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 86:
                        parcel.enforceInterface(DESCRIPTOR);
                        byte[] _result61 = getPermissionGrantBackup(data.readInt());
                        reply.writeNoException();
                        parcel2.writeByteArray(_result61);
                        return true;
                    case 87:
                        parcel.enforceInterface(DESCRIPTOR);
                        restorePermissionGrants(data.createByteArray(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 88:
                        parcel.enforceInterface(DESCRIPTOR);
                        List<ResolveInfo> _arg011 = new ArrayList<>();
                        ComponentName _result62 = getHomeActivities(_arg011);
                        reply.writeNoException();
                        if (_result62 != null) {
                            parcel2.writeInt(1);
                            _result62.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        parcel2.writeTypedList(_arg011);
                        return true;
                    case 89:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg1 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        setHomeActivity(_arg1, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 90:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg1 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        setComponentEnabledSetting(_arg1, data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 91:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg1 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        int _result63 = getComponentEnabledSetting(_arg1, data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result63);
                        return true;
                    case 92:
                        parcel.enforceInterface(DESCRIPTOR);
                        setApplicationEnabledSetting(data.readString(), data.readInt(), data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 93:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result64 = getApplicationEnabledSetting(data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result64);
                        return true;
                    case 94:
                        parcel.enforceInterface(DESCRIPTOR);
                        logAppProcessStartIfNeeded(data.readString(), data.readInt(), data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 95:
                        parcel.enforceInterface(DESCRIPTOR);
                        flushPackageRestrictionsAsUser(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 96:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg012 = data.readString();
                        if (data.readInt() != 0) {
                            _arg12 = true;
                        }
                        setPackageStoppedState(_arg012, _arg12, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 97:
                        parcel.enforceInterface(DESCRIPTOR);
                        freeStorageAndNotify(data.readString(), data.readLong(), data.readInt(), IPackageDataObserver.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 98:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg013 = data.readString();
                        long _arg110 = data.readLong();
                        int _arg26 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = IntentSender.CREATOR.createFromParcel(parcel);
                        }
                        freeStorage(_arg013, _arg110, _arg26, _arg1);
                        reply.writeNoException();
                        return true;
                    case 99:
                        parcel.enforceInterface(DESCRIPTOR);
                        deleteApplicationCacheFiles(data.readString(), IPackageDataObserver.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 100:
                        parcel.enforceInterface(DESCRIPTOR);
                        deleteApplicationCacheFilesAsUser(data.readString(), data.readInt(), IPackageDataObserver.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 101:
                        parcel.enforceInterface(DESCRIPTOR);
                        clearApplicationUserData(data.readString(), IPackageDataObserver.Stub.asInterface(data.readStrongBinder()), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 102:
                        parcel.enforceInterface(DESCRIPTOR);
                        clearApplicationProfileData(data.readString());
                        reply.writeNoException();
                        return true;
                    case 103:
                        parcel.enforceInterface(DESCRIPTOR);
                        getPackageSizeInfo(data.readString(), data.readInt(), IPackageStatsObserver.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 104:
                        parcel.enforceInterface(DESCRIPTOR);
                        String[] _result65 = getSystemSharedLibraryNames();
                        reply.writeNoException();
                        parcel2.writeStringArray(_result65);
                        return true;
                    case 105:
                        parcel.enforceInterface(DESCRIPTOR);
                        ParceledListSlice _result66 = getSystemAvailableFeatures();
                        reply.writeNoException();
                        if (_result66 != null) {
                            parcel2.writeInt(1);
                            _result66.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 106:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result67 = hasSystemFeature(data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result67);
                        return true;
                    case 107:
                        parcel.enforceInterface(DESCRIPTOR);
                        enterSafeMode();
                        reply.writeNoException();
                        return true;
                    case 108:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result68 = isSafeMode();
                        reply.writeNoException();
                        parcel2.writeInt(_result68);
                        return true;
                    case 109:
                        parcel.enforceInterface(DESCRIPTOR);
                        systemReady();
                        reply.writeNoException();
                        return true;
                    case 110:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result69 = hasSystemUidErrors();
                        reply.writeNoException();
                        parcel2.writeInt(_result69);
                        return true;
                    case 111:
                        parcel.enforceInterface(DESCRIPTOR);
                        performFstrimIfNeeded();
                        reply.writeNoException();
                        return true;
                    case 112:
                        parcel.enforceInterface(DESCRIPTOR);
                        updatePackagesIfNeeded();
                        reply.writeNoException();
                        return true;
                    case 113:
                        parcel.enforceInterface(DESCRIPTOR);
                        notifyPackageUse(data.readString(), data.readInt());
                        return true;
                    case 114:
                        parcel.enforceInterface(DESCRIPTOR);
                        notifyDexLoad(data.readString(), data.createStringArrayList(), data.createStringArrayList(), data.readString());
                        return true;
                    case 115:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg014 = data.readString();
                        String _arg111 = data.readString();
                        if (data.readInt() != 0) {
                            _arg12 = true;
                        }
                        registerDexModule(_arg014, _arg111, _arg12, IDexModuleRegisterCallback.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 116:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result70 = performDexOptMode(data.readString(), data.readInt() != 0, data.readString(), data.readInt() != 0, data.readInt() != 0, data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result70);
                        return true;
                    case 117:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg015 = data.readString();
                        String _arg112 = data.readString();
                        if (data.readInt() != 0) {
                            _arg12 = true;
                        }
                        boolean _result71 = performDexOptSecondary(_arg015, _arg112, _arg12);
                        reply.writeNoException();
                        parcel2.writeInt(_result71);
                        return true;
                    case 118:
                        parcel.enforceInterface(DESCRIPTOR);
                        dumpProfiles(data.readString());
                        reply.writeNoException();
                        return true;
                    case 119:
                        parcel.enforceInterface(DESCRIPTOR);
                        forceDexOpt(data.readString());
                        reply.writeNoException();
                        return true;
                    case 120:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result72 = runBackgroundDexoptJob(data.createStringArrayList());
                        reply.writeNoException();
                        parcel2.writeInt(_result72);
                        return true;
                    case 121:
                        parcel.enforceInterface(DESCRIPTOR);
                        reconcileSecondaryDexFiles(data.readString());
                        reply.writeNoException();
                        return true;
                    case 122:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg1 = PackageCleanItem.CREATOR.createFromParcel(parcel);
                        }
                        PackageCleanItem _result73 = nextPackageToClean(_arg1);
                        reply.writeNoException();
                        if (_result73 != null) {
                            parcel2.writeInt(1);
                            _result73.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 123:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result74 = getMoveStatus(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result74);
                        return true;
                    case 124:
                        parcel.enforceInterface(DESCRIPTOR);
                        registerMoveCallback(IPackageMoveObserver.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 125:
                        parcel.enforceInterface(DESCRIPTOR);
                        unregisterMoveCallback(IPackageMoveObserver.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 126:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result75 = movePackage(data.readString(), data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result75);
                        return true;
                    case 127:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result76 = movePrimaryStorage(data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result76);
                        return true;
                    case 128:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg1 = PermissionInfo.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result77 = addPermissionAsync(_arg1);
                        reply.writeNoException();
                        parcel2.writeInt(_result77);
                        return true;
                    case 129:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result78 = setInstallLocation(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result78);
                        return true;
                    case 130:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result79 = getInstallLocation();
                        reply.writeNoException();
                        parcel2.writeInt(_result79);
                        return true;
                    case 131:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result80 = installExistingPackageAsUser(data.readString(), data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result80);
                        return true;
                    case 132:
                        parcel.enforceInterface(DESCRIPTOR);
                        verifyPendingInstall(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 133:
                        parcel.enforceInterface(DESCRIPTOR);
                        extendVerificationTimeout(data.readInt(), data.readInt(), data.readLong());
                        reply.writeNoException();
                        return true;
                    case 134:
                        parcel.enforceInterface(DESCRIPTOR);
                        verifyIntentFilter(data.readInt(), data.readInt(), data.createStringArrayList());
                        reply.writeNoException();
                        return true;
                    case 135:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result81 = getIntentVerificationStatus(data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result81);
                        return true;
                    case 136:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result82 = updateIntentVerificationStatus(data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result82);
                        return true;
                    case 137:
                        parcel.enforceInterface(DESCRIPTOR);
                        ParceledListSlice _result83 = getIntentFilterVerifications(data.readString());
                        reply.writeNoException();
                        if (_result83 != null) {
                            parcel2.writeInt(1);
                            _result83.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 138:
                        parcel.enforceInterface(DESCRIPTOR);
                        ParceledListSlice _result84 = getAllIntentFilters(data.readString());
                        reply.writeNoException();
                        if (_result84 != null) {
                            parcel2.writeInt(1);
                            _result84.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 139:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result85 = setDefaultBrowserPackageName(data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result85);
                        return true;
                    case 140:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result86 = getDefaultBrowserPackageName(data.readInt());
                        reply.writeNoException();
                        parcel2.writeString(_result86);
                        return true;
                    case 141:
                        parcel.enforceInterface(DESCRIPTOR);
                        VerifierDeviceIdentity _result87 = getVerifierDeviceIdentity();
                        reply.writeNoException();
                        if (_result87 != null) {
                            parcel2.writeInt(1);
                            _result87.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 142:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result88 = isFirstBoot();
                        reply.writeNoException();
                        parcel2.writeInt(_result88);
                        return true;
                    case 143:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result89 = isOnlyCoreApps();
                        reply.writeNoException();
                        parcel2.writeInt(_result89);
                        return true;
                    case 144:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result90 = isUpgrade();
                        reply.writeNoException();
                        parcel2.writeInt(_result90);
                        return true;
                    case 145:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg016 = data.readString();
                        if (data.readInt() != 0) {
                            _arg12 = true;
                        }
                        setPermissionEnforced(_arg016, _arg12);
                        reply.writeNoException();
                        return true;
                    case 146:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result91 = isPermissionEnforced(data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result91);
                        return true;
                    case 147:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result92 = isStorageLow();
                        reply.writeNoException();
                        parcel2.writeInt(_result92);
                        return true;
                    case 148:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg017 = data.readString();
                        if (data.readInt() != 0) {
                            _arg12 = true;
                        }
                        boolean _result93 = setApplicationHiddenSettingAsUser(_arg017, _arg12, data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result93);
                        return true;
                    case 149:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result94 = getApplicationHiddenSettingAsUser(data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result94);
                        return true;
                    case 150:
                        parcel.enforceInterface(DESCRIPTOR);
                        IPackageInstaller _result95 = getPackageInstaller();
                        reply.writeNoException();
                        if (_result95 != null) {
                            _arg1 = _result95.asBinder();
                        }
                        parcel2.writeStrongBinder(_arg1);
                        return true;
                    case 151:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg018 = data.readString();
                        if (data.readInt() != 0) {
                            _arg12 = true;
                        }
                        boolean _result96 = setBlockUninstallForUser(_arg018, _arg12, data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result96);
                        return true;
                    case 152:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result97 = getBlockUninstallForUser(data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result97);
                        return true;
                    case 153:
                        parcel.enforceInterface(DESCRIPTOR);
                        KeySet _result98 = getKeySetByAlias(data.readString(), data.readString());
                        reply.writeNoException();
                        if (_result98 != null) {
                            parcel2.writeInt(1);
                            _result98.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 154:
                        parcel.enforceInterface(DESCRIPTOR);
                        KeySet _result99 = getSigningKeySet(data.readString());
                        reply.writeNoException();
                        if (_result99 != null) {
                            parcel2.writeInt(1);
                            _result99.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 155:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg019 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = KeySet.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result100 = isPackageSignedByKeySet(_arg019, _arg1);
                        reply.writeNoException();
                        parcel2.writeInt(_result100);
                        return true;
                    case 156:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg020 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = KeySet.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result101 = isPackageSignedByKeySetExactly(_arg020, _arg1);
                        reply.writeNoException();
                        parcel2.writeInt(_result101);
                        return true;
                    case 157:
                        parcel.enforceInterface(DESCRIPTOR);
                        addOnPermissionsChangeListener(IOnPermissionsChangeListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 158:
                        parcel.enforceInterface(DESCRIPTOR);
                        removeOnPermissionsChangeListener(IOnPermissionsChangeListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 159:
                        parcel.enforceInterface(DESCRIPTOR);
                        grantDefaultPermissionsToEnabledCarrierApps(data.createStringArray(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 160:
                        parcel.enforceInterface(DESCRIPTOR);
                        grantDefaultPermissionsToEnabledImsServices(data.createStringArray(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 161:
                        parcel.enforceInterface(DESCRIPTOR);
                        grantDefaultPermissionsToEnabledTelephonyDataServices(data.createStringArray(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 162:
                        parcel.enforceInterface(DESCRIPTOR);
                        revokeDefaultPermissionsFromDisabledTelephonyDataServices(data.createStringArray(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 163:
                        parcel.enforceInterface(DESCRIPTOR);
                        grantDefaultPermissionsToActiveLuiApp(data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 164:
                        parcel.enforceInterface(DESCRIPTOR);
                        revokeDefaultPermissionsFromLuiApps(data.createStringArray(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 165:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result102 = isPermissionRevokedByPolicy(data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result102);
                        return true;
                    case 166:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result103 = getPermissionControllerPackageName();
                        reply.writeNoException();
                        parcel2.writeString(_result103);
                        return true;
                    case 167:
                        parcel.enforceInterface(DESCRIPTOR);
                        ParceledListSlice _result104 = getInstantApps(data.readInt());
                        reply.writeNoException();
                        if (_result104 != null) {
                            parcel2.writeInt(1);
                            _result104.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 168:
                        parcel.enforceInterface(DESCRIPTOR);
                        byte[] _result105 = getInstantAppCookie(data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeByteArray(_result105);
                        return true;
                    case 169:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result106 = setInstantAppCookie(data.readString(), data.createByteArray(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result106);
                        return true;
                    case 170:
                        parcel.enforceInterface(DESCRIPTOR);
                        Bitmap _result107 = getInstantAppIcon(data.readString(), data.readInt());
                        reply.writeNoException();
                        if (_result107 != null) {
                            parcel2.writeInt(1);
                            _result107.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 171:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result108 = isInstantApp(data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result108);
                        return true;
                    case 172:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg021 = data.readString();
                        if (data.readInt() != 0) {
                            _arg12 = true;
                        }
                        boolean _result109 = setRequiredForSystemUser(_arg021, _arg12);
                        reply.writeNoException();
                        parcel2.writeInt(_result109);
                        return true;
                    case 173:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg022 = data.readString();
                        if (data.readInt() != 0) {
                            _arg12 = true;
                        }
                        setUpdateAvailable(_arg022, _arg12);
                        reply.writeNoException();
                        return true;
                    case 174:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result110 = getServicesSystemSharedLibraryPackageName();
                        reply.writeNoException();
                        parcel2.writeString(_result110);
                        return true;
                    case 175:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result111 = getSharedSystemSharedLibraryPackageName();
                        reply.writeNoException();
                        parcel2.writeString(_result111);
                        return true;
                    case 176:
                        parcel.enforceInterface(DESCRIPTOR);
                        ChangedPackages _result112 = getChangedPackages(data.readInt(), data.readInt());
                        reply.writeNoException();
                        if (_result112 != null) {
                            parcel2.writeInt(1);
                            _result112.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 177:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result113 = isPackageDeviceAdminOnAnyUser(data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result113);
                        return true;
                    case 178:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result114 = getInstallReason(data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result114);
                        return true;
                    case 179:
                        parcel.enforceInterface(DESCRIPTOR);
                        ParceledListSlice _result115 = getSharedLibraries(data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        if (_result115 != null) {
                            parcel2.writeInt(1);
                            _result115.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 180:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result116 = canRequestPackageInstalls(data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result116);
                        return true;
                    case 181:
                        parcel.enforceInterface(DESCRIPTOR);
                        deletePreloadsFileCache();
                        reply.writeNoException();
                        return true;
                    case 182:
                        parcel.enforceInterface(DESCRIPTOR);
                        ComponentName _result117 = getInstantAppResolverComponent();
                        reply.writeNoException();
                        if (_result117 != null) {
                            parcel2.writeInt(1);
                            _result117.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 183:
                        parcel.enforceInterface(DESCRIPTOR);
                        ComponentName _result118 = getInstantAppResolverSettingsComponent();
                        reply.writeNoException();
                        if (_result118 != null) {
                            parcel2.writeInt(1);
                            _result118.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 184:
                        parcel.enforceInterface(DESCRIPTOR);
                        ComponentName _result119 = getInstantAppInstallerComponent();
                        reply.writeNoException();
                        if (_result119 != null) {
                            parcel2.writeInt(1);
                            _result119.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 185:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result120 = getInstantAppAndroidId(data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeString(_result120);
                        return true;
                    case 186:
                        parcel.enforceInterface(DESCRIPTOR);
                        IArtManager _result121 = getArtManager();
                        reply.writeNoException();
                        if (_result121 != null) {
                            _arg1 = _result121.asBinder();
                        }
                        parcel2.writeStrongBinder(_arg1);
                        return true;
                    case 187:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg023 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel);
                        }
                        setHarmfulAppWarning(_arg023, _arg1, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 188:
                        parcel.enforceInterface(DESCRIPTOR);
                        CharSequence _result122 = getHarmfulAppWarning(data.readString(), data.readInt());
                        reply.writeNoException();
                        if (_result122 != null) {
                            parcel2.writeInt(1);
                            TextUtils.writeToParcel(_result122, parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 189:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result123 = hasSigningCertificate(data.readString(), data.createByteArray(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result123);
                        return true;
                    case 190:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result124 = hasUidSigningCertificate(data.readInt(), data.createByteArray(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result124);
                        return true;
                    case 191:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result125 = getSystemTextClassifierPackageName();
                        reply.writeNoException();
                        parcel2.writeString(_result125);
                        return true;
                    case 192:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result126 = isPackageStateProtected(data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result126);
                        return true;
                    case 193:
                        parcel.enforceInterface(DESCRIPTOR);
                        IBinder _result127 = getHwInnerService();
                        reply.writeNoException();
                        parcel2.writeStrongBinder(_result127);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                parcel2.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    boolean activitySupportsIntent(ComponentName componentName, Intent intent, String str) throws RemoteException;

    void addCrossProfileIntentFilter(IntentFilter intentFilter, String str, int i, int i2, int i3) throws RemoteException;

    void addOnPermissionsChangeListener(IOnPermissionsChangeListener iOnPermissionsChangeListener) throws RemoteException;

    boolean addPermission(PermissionInfo permissionInfo) throws RemoteException;

    boolean addPermissionAsync(PermissionInfo permissionInfo) throws RemoteException;

    void addPersistentPreferredActivity(IntentFilter intentFilter, ComponentName componentName, int i) throws RemoteException;

    void addPreferredActivity(IntentFilter intentFilter, int i, ComponentName[] componentNameArr, ComponentName componentName, int i2) throws RemoteException;

    boolean canForwardTo(Intent intent, String str, int i, int i2) throws RemoteException;

    boolean canRequestPackageInstalls(String str, int i) throws RemoteException;

    String[] canonicalToCurrentPackageNames(String[] strArr) throws RemoteException;

    void checkPackageStartable(String str, int i) throws RemoteException;

    int checkPermission(String str, String str2, int i) throws RemoteException;

    int checkSignatures(String str, String str2) throws RemoteException;

    int checkUidPermission(String str, int i) throws RemoteException;

    int checkUidSignatures(int i, int i2) throws RemoteException;

    void clearApplicationProfileData(String str) throws RemoteException;

    void clearApplicationUserData(String str, IPackageDataObserver iPackageDataObserver, int i) throws RemoteException;

    void clearCrossProfileIntentFilters(int i, String str) throws RemoteException;

    void clearPackagePersistentPreferredActivities(String str, int i) throws RemoteException;

    void clearPackagePreferredActivities(String str) throws RemoteException;

    String[] currentToCanonicalPackageNames(String[] strArr) throws RemoteException;

    void deleteApplicationCacheFiles(String str, IPackageDataObserver iPackageDataObserver) throws RemoteException;

    void deleteApplicationCacheFilesAsUser(String str, int i, IPackageDataObserver iPackageDataObserver) throws RemoteException;

    void deletePackageAsUser(String str, int i, IPackageDeleteObserver iPackageDeleteObserver, int i2, int i3) throws RemoteException;

    void deletePackageVersioned(VersionedPackage versionedPackage, IPackageDeleteObserver2 iPackageDeleteObserver2, int i, int i2) throws RemoteException;

    void deletePreloadsFileCache() throws RemoteException;

    void dumpProfiles(String str) throws RemoteException;

    void enterSafeMode() throws RemoteException;

    void extendVerificationTimeout(int i, int i2, long j) throws RemoteException;

    ResolveInfo findPersistentPreferredActivity(Intent intent, int i) throws RemoteException;

    void finishPackageInstall(int i, boolean z) throws RemoteException;

    void flushPackageRestrictionsAsUser(int i) throws RemoteException;

    void forceDexOpt(String str) throws RemoteException;

    void freeStorage(String str, long j, int i, IntentSender intentSender) throws RemoteException;

    void freeStorageAndNotify(String str, long j, int i, IPackageDataObserver iPackageDataObserver) throws RemoteException;

    ActivityInfo getActivityInfo(ComponentName componentName, int i, int i2) throws RemoteException;

    ParceledListSlice getAllIntentFilters(String str) throws RemoteException;

    List<String> getAllPackages() throws RemoteException;

    ParceledListSlice getAllPermissionGroups(int i) throws RemoteException;

    String[] getAppOpPermissionPackages(String str) throws RemoteException;

    int getApplicationEnabledSetting(String str, int i) throws RemoteException;

    boolean getApplicationHiddenSettingAsUser(String str, int i) throws RemoteException;

    ApplicationInfo getApplicationInfo(String str, int i, int i2) throws RemoteException;

    IArtManager getArtManager() throws RemoteException;

    boolean getBlockUninstallForUser(String str, int i) throws RemoteException;

    ChangedPackages getChangedPackages(int i, int i2) throws RemoteException;

    int getComponentEnabledSetting(ComponentName componentName, int i) throws RemoteException;

    byte[] getDefaultAppsBackup(int i) throws RemoteException;

    String getDefaultBrowserPackageName(int i) throws RemoteException;

    int getFlagsForUid(int i) throws RemoteException;

    CharSequence getHarmfulAppWarning(String str, int i) throws RemoteException;

    ComponentName getHomeActivities(List<ResolveInfo> list) throws RemoteException;

    IBinder getHwInnerService() throws RemoteException;

    int getInstallLocation() throws RemoteException;

    int getInstallReason(String str, int i) throws RemoteException;

    ParceledListSlice getInstalledApplications(int i, int i2) throws RemoteException;

    ParceledListSlice getInstalledPackages(int i, int i2) throws RemoteException;

    String getInstallerPackageName(String str) throws RemoteException;

    String getInstantAppAndroidId(String str, int i) throws RemoteException;

    byte[] getInstantAppCookie(String str, int i) throws RemoteException;

    Bitmap getInstantAppIcon(String str, int i) throws RemoteException;

    ComponentName getInstantAppInstallerComponent() throws RemoteException;

    ComponentName getInstantAppResolverComponent() throws RemoteException;

    ComponentName getInstantAppResolverSettingsComponent() throws RemoteException;

    ParceledListSlice getInstantApps(int i) throws RemoteException;

    InstrumentationInfo getInstrumentationInfo(ComponentName componentName, int i) throws RemoteException;

    byte[] getIntentFilterVerificationBackup(int i) throws RemoteException;

    ParceledListSlice getIntentFilterVerifications(String str) throws RemoteException;

    int getIntentVerificationStatus(String str, int i) throws RemoteException;

    KeySet getKeySetByAlias(String str, String str2) throws RemoteException;

    ResolveInfo getLastChosenActivity(Intent intent, String str, int i) throws RemoteException;

    int getMoveStatus(int i) throws RemoteException;

    String getNameForUid(int i) throws RemoteException;

    String[] getNamesForUids(int[] iArr) throws RemoteException;

    int[] getPackageGids(String str, int i, int i2) throws RemoteException;

    PackageInfo getPackageInfo(String str, int i, int i2) throws RemoteException;

    PackageInfo getPackageInfoVersioned(VersionedPackage versionedPackage, int i, int i2) throws RemoteException;

    IPackageInstaller getPackageInstaller() throws RemoteException;

    void getPackageSizeInfo(String str, int i, IPackageStatsObserver iPackageStatsObserver) throws RemoteException;

    int getPackageUid(String str, int i, int i2) throws RemoteException;

    String[] getPackagesForUid(int i) throws RemoteException;

    ParceledListSlice getPackagesHoldingPermissions(String[] strArr, int i, int i2) throws RemoteException;

    String getPermissionControllerPackageName() throws RemoteException;

    int getPermissionFlags(String str, String str2, int i) throws RemoteException;

    byte[] getPermissionGrantBackup(int i) throws RemoteException;

    PermissionGroupInfo getPermissionGroupInfo(String str, int i) throws RemoteException;

    PermissionInfo getPermissionInfo(String str, String str2, int i) throws RemoteException;

    ParceledListSlice getPersistentApplications(int i) throws RemoteException;

    int getPreferredActivities(List<IntentFilter> list, List<ComponentName> list2, String str) throws RemoteException;

    byte[] getPreferredActivityBackup(int i) throws RemoteException;

    int getPrivateFlagsForUid(int i) throws RemoteException;

    ProviderInfo getProviderInfo(ComponentName componentName, int i, int i2) throws RemoteException;

    ActivityInfo getReceiverInfo(ComponentName componentName, int i, int i2) throws RemoteException;

    ServiceInfo getServiceInfo(ComponentName componentName, int i, int i2) throws RemoteException;

    String getServicesSystemSharedLibraryPackageName() throws RemoteException;

    ParceledListSlice getSharedLibraries(String str, int i, int i2) throws RemoteException;

    String getSharedSystemSharedLibraryPackageName() throws RemoteException;

    KeySet getSigningKeySet(String str) throws RemoteException;

    PersistableBundle getSuspendedPackageAppExtras(String str, int i) throws RemoteException;

    ParceledListSlice getSystemAvailableFeatures() throws RemoteException;

    String[] getSystemSharedLibraryNames() throws RemoteException;

    String getSystemTextClassifierPackageName() throws RemoteException;

    int getUidForSharedUser(String str) throws RemoteException;

    VerifierDeviceIdentity getVerifierDeviceIdentity() throws RemoteException;

    void grantDefaultPermissionsToActiveLuiApp(String str, int i) throws RemoteException;

    void grantDefaultPermissionsToEnabledCarrierApps(String[] strArr, int i) throws RemoteException;

    void grantDefaultPermissionsToEnabledImsServices(String[] strArr, int i) throws RemoteException;

    void grantDefaultPermissionsToEnabledTelephonyDataServices(String[] strArr, int i) throws RemoteException;

    void grantRuntimePermission(String str, String str2, int i) throws RemoteException;

    boolean hasSigningCertificate(String str, byte[] bArr, int i) throws RemoteException;

    boolean hasSystemFeature(String str, int i) throws RemoteException;

    boolean hasSystemUidErrors() throws RemoteException;

    boolean hasUidSigningCertificate(int i, byte[] bArr, int i2) throws RemoteException;

    int installExistingPackageAsUser(String str, int i, int i2, int i3) throws RemoteException;

    boolean isFirstBoot() throws RemoteException;

    boolean isInstantApp(String str, int i) throws RemoteException;

    boolean isOnlyCoreApps() throws RemoteException;

    boolean isPackageAvailable(String str, int i) throws RemoteException;

    boolean isPackageDeviceAdminOnAnyUser(String str) throws RemoteException;

    boolean isPackageSignedByKeySet(String str, KeySet keySet) throws RemoteException;

    boolean isPackageSignedByKeySetExactly(String str, KeySet keySet) throws RemoteException;

    boolean isPackageStateProtected(String str, int i) throws RemoteException;

    boolean isPackageSuspendedForUser(String str, int i) throws RemoteException;

    boolean isPermissionEnforced(String str) throws RemoteException;

    boolean isPermissionRevokedByPolicy(String str, String str2, int i) throws RemoteException;

    boolean isProtectedBroadcast(String str) throws RemoteException;

    boolean isSafeMode() throws RemoteException;

    boolean isStorageLow() throws RemoteException;

    boolean isUidPrivileged(int i) throws RemoteException;

    boolean isUpgrade() throws RemoteException;

    void logAppProcessStartIfNeeded(String str, int i, String str2, String str3, int i2) throws RemoteException;

    int movePackage(String str, String str2) throws RemoteException;

    int movePrimaryStorage(String str) throws RemoteException;

    PackageCleanItem nextPackageToClean(PackageCleanItem packageCleanItem) throws RemoteException;

    void notifyDexLoad(String str, List<String> list, List<String> list2, String str2) throws RemoteException;

    void notifyPackageUse(String str, int i) throws RemoteException;

    boolean performDexOptMode(String str, boolean z, String str2, boolean z2, boolean z3, String str3) throws RemoteException;

    boolean performDexOptSecondary(String str, String str2, boolean z) throws RemoteException;

    void performFstrimIfNeeded() throws RemoteException;

    ParceledListSlice queryContentProviders(String str, int i, int i2, String str2) throws RemoteException;

    ParceledListSlice queryInstrumentation(String str, int i) throws RemoteException;

    ParceledListSlice queryIntentActivities(Intent intent, String str, int i, int i2) throws RemoteException;

    ParceledListSlice queryIntentActivityOptions(ComponentName componentName, Intent[] intentArr, String[] strArr, Intent intent, String str, int i, int i2) throws RemoteException;

    ParceledListSlice queryIntentContentProviders(Intent intent, String str, int i, int i2) throws RemoteException;

    ParceledListSlice queryIntentReceivers(Intent intent, String str, int i, int i2) throws RemoteException;

    ParceledListSlice queryIntentServices(Intent intent, String str, int i, int i2) throws RemoteException;

    ParceledListSlice queryPermissionsByGroup(String str, int i) throws RemoteException;

    void querySyncProviders(List<String> list, List<ProviderInfo> list2) throws RemoteException;

    void reconcileSecondaryDexFiles(String str) throws RemoteException;

    void registerDexModule(String str, String str2, boolean z, IDexModuleRegisterCallback iDexModuleRegisterCallback) throws RemoteException;

    void registerMoveCallback(IPackageMoveObserver iPackageMoveObserver) throws RemoteException;

    void removeOnPermissionsChangeListener(IOnPermissionsChangeListener iOnPermissionsChangeListener) throws RemoteException;

    void removePermission(String str) throws RemoteException;

    void replacePreferredActivity(IntentFilter intentFilter, int i, ComponentName[] componentNameArr, ComponentName componentName, int i2) throws RemoteException;

    void resetApplicationPreferences(int i) throws RemoteException;

    void resetRuntimePermissions() throws RemoteException;

    ProviderInfo resolveContentProvider(String str, int i, int i2) throws RemoteException;

    ResolveInfo resolveIntent(Intent intent, String str, int i, int i2) throws RemoteException;

    ResolveInfo resolveService(Intent intent, String str, int i, int i2) throws RemoteException;

    void restoreDefaultApps(byte[] bArr, int i) throws RemoteException;

    void restoreIntentFilterVerification(byte[] bArr, int i) throws RemoteException;

    void restorePermissionGrants(byte[] bArr, int i) throws RemoteException;

    void restorePreferredActivities(byte[] bArr, int i) throws RemoteException;

    void revokeDefaultPermissionsFromDisabledTelephonyDataServices(String[] strArr, int i) throws RemoteException;

    void revokeDefaultPermissionsFromLuiApps(String[] strArr, int i) throws RemoteException;

    void revokeRuntimePermission(String str, String str2, int i) throws RemoteException;

    boolean runBackgroundDexoptJob(List<String> list) throws RemoteException;

    void setApplicationCategoryHint(String str, int i, String str2) throws RemoteException;

    void setApplicationEnabledSetting(String str, int i, int i2, int i3, String str2) throws RemoteException;

    boolean setApplicationHiddenSettingAsUser(String str, boolean z, int i) throws RemoteException;

    boolean setBlockUninstallForUser(String str, boolean z, int i) throws RemoteException;

    void setComponentEnabledSetting(ComponentName componentName, int i, int i2, int i3) throws RemoteException;

    boolean setDefaultBrowserPackageName(String str, int i) throws RemoteException;

    void setHarmfulAppWarning(String str, CharSequence charSequence, int i) throws RemoteException;

    void setHomeActivity(ComponentName componentName, int i) throws RemoteException;

    boolean setInstallLocation(int i) throws RemoteException;

    void setInstallerPackageName(String str, String str2) throws RemoteException;

    boolean setInstantAppCookie(String str, byte[] bArr, int i) throws RemoteException;

    void setLastChosenActivity(Intent intent, String str, int i, IntentFilter intentFilter, int i2, ComponentName componentName) throws RemoteException;

    void setPackageStoppedState(String str, boolean z, int i) throws RemoteException;

    String[] setPackagesSuspendedAsUser(String[] strArr, boolean z, PersistableBundle persistableBundle, PersistableBundle persistableBundle2, String str, String str2, int i) throws RemoteException;

    void setPermissionEnforced(String str, boolean z) throws RemoteException;

    boolean setRequiredForSystemUser(String str, boolean z) throws RemoteException;

    void setUpdateAvailable(String str, boolean z) throws RemoteException;

    boolean shouldShowRequestPermissionRationale(String str, String str2, int i) throws RemoteException;

    void systemReady() throws RemoteException;

    void unregisterMoveCallback(IPackageMoveObserver iPackageMoveObserver) throws RemoteException;

    boolean updateIntentVerificationStatus(String str, int i, int i2) throws RemoteException;

    void updatePackagesIfNeeded() throws RemoteException;

    void updatePermissionFlags(String str, String str2, int i, int i2, int i3) throws RemoteException;

    void updatePermissionFlagsForAllApps(int i, int i2, int i3) throws RemoteException;

    void verifyIntentFilter(int i, int i2, List<String> list) throws RemoteException;

    void verifyPendingInstall(int i, int i2) throws RemoteException;
}
