package android.content.pm;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.ArrayList;
import java.util.List;

public interface IPackageManager extends IInterface {

    public static abstract class Stub extends Binder implements IPackageManager {
        private static final String DESCRIPTOR = "android.content.pm.IPackageManager";
        static final int TRANSACTION_activitySupportsIntent = 14;
        static final int TRANSACTION_addCrossProfileIntentFilter = 72;
        static final int TRANSACTION_addOnPermissionsChangeListener = 151;
        static final int TRANSACTION_addPermission = 20;
        static final int TRANSACTION_addPermissionAsync = 122;
        static final int TRANSACTION_addPersistentPreferredActivity = 70;
        static final int TRANSACTION_addPreferredActivity = 66;
        static final int TRANSACTION_canForwardTo = 41;
        static final int TRANSACTION_canonicalToCurrentPackageNames = 7;
        static final int TRANSACTION_checkPackageStartable = 1;
        static final int TRANSACTION_checkPermission = 18;
        static final int TRANSACTION_checkSignatures = 30;
        static final int TRANSACTION_checkUidPermission = 19;
        static final int TRANSACTION_checkUidSignatures = 31;
        static final int TRANSACTION_clearApplicationProfileData = 98;
        static final int TRANSACTION_clearApplicationUserData = 97;
        static final int TRANSACTION_clearCrossProfileIntentFilters = 73;
        static final int TRANSACTION_clearPackagePersistentPreferredActivities = 71;
        static final int TRANSACTION_clearPackagePreferredActivities = 68;
        static final int TRANSACTION_currentToCanonicalPackageNames = 6;
        static final int TRANSACTION_deleteApplicationCacheFiles = 95;
        static final int TRANSACTION_deleteApplicationCacheFilesAsUser = 96;
        static final int TRANSACTION_deletePackage = 61;
        static final int TRANSACTION_deletePackageAsUser = 60;
        static final int TRANSACTION_dumpProfiles = 113;
        static final int TRANSACTION_enterSafeMode = 103;
        static final int TRANSACTION_extendVerificationTimeout = 127;
        static final int TRANSACTION_finishPackageInstall = 58;
        static final int TRANSACTION_flushPackageRestrictionsAsUser = 91;
        static final int TRANSACTION_forceDexOpt = 114;
        static final int TRANSACTION_freeStorage = 94;
        static final int TRANSACTION_freeStorageAndNotify = 93;
        static final int TRANSACTION_getActivityInfo = 13;
        static final int TRANSACTION_getAllIntentFilters = 132;
        static final int TRANSACTION_getAllPackages = 32;
        static final int TRANSACTION_getAllPermissionGroups = 11;
        static final int TRANSACTION_getAppOpPermissionPackages = 39;
        static final int TRANSACTION_getApplicationEnabledSetting = 89;
        static final int TRANSACTION_getApplicationHiddenSettingAsUser = 143;
        static final int TRANSACTION_getApplicationInfo = 12;
        static final int TRANSACTION_getBlockUninstallForUser = 146;
        static final int TRANSACTION_getComponentEnabledSetting = 87;
        static final int TRANSACTION_getDefaultAppsBackup = 78;
        static final int TRANSACTION_getDefaultBrowserPackageName = 134;
        static final int TRANSACTION_getEphemeralApplicationCookie = 157;
        static final int TRANSACTION_getEphemeralApplicationIcon = 159;
        static final int TRANSACTION_getEphemeralApplications = 156;
        static final int TRANSACTION_getFlagsForUid = 36;
        static final int TRANSACTION_getHomeActivities = 84;
        static final int TRANSACTION_getInstallLocation = 124;
        static final int TRANSACTION_getInstalledApplications = 50;
        static final int TRANSACTION_getInstalledPackages = 48;
        static final int TRANSACTION_getInstallerPackageName = 62;
        static final int TRANSACTION_getInstrumentationInfo = 55;
        static final int TRANSACTION_getIntentFilterVerificationBackup = 80;
        static final int TRANSACTION_getIntentFilterVerifications = 131;
        static final int TRANSACTION_getIntentVerificationStatus = 129;
        static final int TRANSACTION_getKeySetByAlias = 147;
        static final int TRANSACTION_getLastChosenActivity = 64;
        static final int TRANSACTION_getMoveStatus = 117;
        static final int TRANSACTION_getNameForUid = 34;
        static final int TRANSACTION_getPackageGids = 5;
        static final int TRANSACTION_getPackageInfo = 3;
        static final int TRANSACTION_getPackageInstaller = 144;
        static final int TRANSACTION_getPackageSizeInfo = 99;
        static final int TRANSACTION_getPackageUid = 4;
        static final int TRANSACTION_getPackagesForUid = 33;
        static final int TRANSACTION_getPackagesHoldingPermissions = 49;
        static final int TRANSACTION_getPermissionControllerPackageName = 155;
        static final int TRANSACTION_getPermissionFlags = 25;
        static final int TRANSACTION_getPermissionGrantBackup = 82;
        static final int TRANSACTION_getPermissionGroupInfo = 10;
        static final int TRANSACTION_getPermissionInfo = 8;
        static final int TRANSACTION_getPersistentApplications = 51;
        static final int TRANSACTION_getPreferredActivities = 69;
        static final int TRANSACTION_getPreferredActivityBackup = 76;
        static final int TRANSACTION_getPreviousCodePaths = 165;
        static final int TRANSACTION_getPrivateFlagsForUid = 37;
        static final int TRANSACTION_getProviderInfo = 17;
        static final int TRANSACTION_getReceiverInfo = 15;
        static final int TRANSACTION_getServiceInfo = 16;
        static final int TRANSACTION_getServicesSystemSharedLibraryPackageName = 162;
        static final int TRANSACTION_getSharedSystemSharedLibraryPackageName = 163;
        static final int TRANSACTION_getSigningKeySet = 148;
        static final int TRANSACTION_getSystemAvailableFeatures = 101;
        static final int TRANSACTION_getSystemSharedLibraryNames = 100;
        static final int TRANSACTION_getUidForSharedUser = 35;
        static final int TRANSACTION_getVerifierDeviceIdentity = 135;
        static final int TRANSACTION_grantDefaultPermissionsToEnabledCarrierApps = 153;
        static final int TRANSACTION_grantRuntimePermission = 22;
        static final int TRANSACTION_hasSystemFeature = 102;
        static final int TRANSACTION_hasSystemUidErrors = 106;
        static final int TRANSACTION_installExistingPackageAsUser = 125;
        static final int TRANSACTION_installPackageAsUser = 57;
        static final int TRANSACTION_isEphemeralApplication = 160;
        static final int TRANSACTION_isFirstBoot = 136;
        static final int TRANSACTION_isOnlyCoreApps = 137;
        static final int TRANSACTION_isPackageAvailable = 2;
        static final int TRANSACTION_isPackageDeviceAdminOnAnyUser = 164;
        static final int TRANSACTION_isPackageSignedByKeySet = 149;
        static final int TRANSACTION_isPackageSignedByKeySetExactly = 150;
        static final int TRANSACTION_isPackageSuspendedForUser = 75;
        static final int TRANSACTION_isPermissionEnforced = 140;
        static final int TRANSACTION_isPermissionRevokedByPolicy = 154;
        static final int TRANSACTION_isProtectedBroadcast = 29;
        static final int TRANSACTION_isSafeMode = 104;
        static final int TRANSACTION_isStorageLow = 141;
        static final int TRANSACTION_isUidPrivileged = 38;
        static final int TRANSACTION_isUpgrade = 138;
        static final int TRANSACTION_logAppProcessStartIfNeeded = 90;
        static final int TRANSACTION_movePackage = 120;
        static final int TRANSACTION_movePrimaryStorage = 121;
        static final int TRANSACTION_nextPackageToClean = 116;
        static final int TRANSACTION_notifyPackageUse = 109;
        static final int TRANSACTION_performDexOpt = 111;
        static final int TRANSACTION_performDexOptIfNeeded = 110;
        static final int TRANSACTION_performDexOptMode = 112;
        static final int TRANSACTION_performFstrimIfNeeded = 107;
        static final int TRANSACTION_queryContentProviders = 54;
        static final int TRANSACTION_queryInstrumentation = 56;
        static final int TRANSACTION_queryIntentActivities = 42;
        static final int TRANSACTION_queryIntentActivityOptions = 43;
        static final int TRANSACTION_queryIntentContentProviders = 47;
        static final int TRANSACTION_queryIntentReceivers = 44;
        static final int TRANSACTION_queryIntentServices = 46;
        static final int TRANSACTION_queryPermissionsByGroup = 9;
        static final int TRANSACTION_querySyncProviders = 53;
        static final int TRANSACTION_registerMoveCallback = 118;
        static final int TRANSACTION_removeOnPermissionsChangeListener = 152;
        static final int TRANSACTION_removePermission = 21;
        static final int TRANSACTION_replacePreferredActivity = 67;
        static final int TRANSACTION_resetApplicationPreferences = 63;
        static final int TRANSACTION_resetRuntimePermissions = 24;
        static final int TRANSACTION_resolveContentProvider = 52;
        static final int TRANSACTION_resolveIntent = 40;
        static final int TRANSACTION_resolveService = 45;
        static final int TRANSACTION_restoreDefaultApps = 79;
        static final int TRANSACTION_restoreIntentFilterVerification = 81;
        static final int TRANSACTION_restorePermissionGrants = 83;
        static final int TRANSACTION_restorePreferredActivities = 77;
        static final int TRANSACTION_revokeRuntimePermission = 23;
        static final int TRANSACTION_setApplicationEnabledSetting = 88;
        static final int TRANSACTION_setApplicationHiddenSettingAsUser = 142;
        static final int TRANSACTION_setBlockUninstallForUser = 145;
        static final int TRANSACTION_setComponentEnabledSetting = 86;
        static final int TRANSACTION_setDefaultBrowserPackageName = 133;
        static final int TRANSACTION_setEphemeralApplicationCookie = 158;
        static final int TRANSACTION_setHomeActivity = 85;
        static final int TRANSACTION_setInstallLocation = 123;
        static final int TRANSACTION_setInstallerPackageName = 59;
        static final int TRANSACTION_setLastChosenActivity = 65;
        static final int TRANSACTION_setPackageStoppedState = 92;
        static final int TRANSACTION_setPackagesSuspendedAsUser = 74;
        static final int TRANSACTION_setPermissionEnforced = 139;
        static final int TRANSACTION_setRequiredForSystemUser = 161;
        static final int TRANSACTION_shouldShowRequestPermissionRationale = 28;
        static final int TRANSACTION_systemReady = 105;
        static final int TRANSACTION_unregisterMoveCallback = 119;
        static final int TRANSACTION_updateExternalMediaStatus = 115;
        static final int TRANSACTION_updateIntentVerificationStatus = 130;
        static final int TRANSACTION_updatePackagesIfNeeded = 108;
        static final int TRANSACTION_updatePermissionFlags = 26;
        static final int TRANSACTION_updatePermissionFlagsForAllApps = 27;
        static final int TRANSACTION_verifyIntentFilter = 128;
        static final int TRANSACTION_verifyPendingInstall = 126;

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
                    this.mRemote.transact(Stub.TRANSACTION_checkPackageStartable, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_isPackageAvailable, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public PackageInfo getPackageInfo(String packageName, int flags, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    PackageInfo packageInfo;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(flags);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_getPackageInfo, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        packageInfo = (PackageInfo) PackageInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        packageInfo = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return packageInfo;
                } catch (Throwable th) {
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
                    this.mRemote.transact(Stub.TRANSACTION_getPackageUid, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_getPackageGids, _data, _reply, 0);
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_currentToCanonicalPackageNames, _data, _reply, 0);
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_canonicalToCurrentPackageNames, _data, _reply, 0);
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public PermissionInfo getPermissionInfo(String name, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    PermissionInfo permissionInfo;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    _data.writeInt(flags);
                    this.mRemote.transact(Stub.TRANSACTION_getPermissionInfo, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        permissionInfo = (PermissionInfo) PermissionInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        permissionInfo = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return permissionInfo;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParceledListSlice queryPermissionsByGroup(String group, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ParceledListSlice parceledListSlice;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(group);
                    _data.writeInt(flags);
                    this.mRemote.transact(Stub.TRANSACTION_queryPermissionsByGroup, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        parceledListSlice = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(_reply);
                    } else {
                        parceledListSlice = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return parceledListSlice;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public PermissionGroupInfo getPermissionGroupInfo(String name, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    PermissionGroupInfo permissionGroupInfo;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    _data.writeInt(flags);
                    this.mRemote.transact(Stub.TRANSACTION_getPermissionGroupInfo, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        permissionGroupInfo = (PermissionGroupInfo) PermissionGroupInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        permissionGroupInfo = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return permissionGroupInfo;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParceledListSlice getAllPermissionGroups(int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ParceledListSlice parceledListSlice;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(flags);
                    this.mRemote.transact(Stub.TRANSACTION_getAllPermissionGroups, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        parceledListSlice = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(_reply);
                    } else {
                        parceledListSlice = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return parceledListSlice;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ApplicationInfo getApplicationInfo(String packageName, int flags, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ApplicationInfo applicationInfo;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(flags);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_getApplicationInfo, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        applicationInfo = (ApplicationInfo) ApplicationInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        applicationInfo = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return applicationInfo;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ActivityInfo getActivityInfo(ComponentName className, int flags, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ActivityInfo activityInfo;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (className != null) {
                        _data.writeInt(Stub.TRANSACTION_checkPackageStartable);
                        className.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(flags);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_getActivityInfo, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        activityInfo = (ActivityInfo) ActivityInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        activityInfo = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return activityInfo;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean activitySupportsIntent(ComponentName className, Intent intent, String resolvedType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (className != null) {
                        _data.writeInt(Stub.TRANSACTION_checkPackageStartable);
                        className.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (intent != null) {
                        _data.writeInt(Stub.TRANSACTION_checkPackageStartable);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(resolvedType);
                    this.mRemote.transact(Stub.TRANSACTION_activitySupportsIntent, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ActivityInfo getReceiverInfo(ComponentName className, int flags, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ActivityInfo activityInfo;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (className != null) {
                        _data.writeInt(Stub.TRANSACTION_checkPackageStartable);
                        className.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(flags);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_getReceiverInfo, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        activityInfo = (ActivityInfo) ActivityInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        activityInfo = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return activityInfo;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ServiceInfo getServiceInfo(ComponentName className, int flags, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ServiceInfo serviceInfo;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (className != null) {
                        _data.writeInt(Stub.TRANSACTION_checkPackageStartable);
                        className.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(flags);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_getServiceInfo, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        serviceInfo = (ServiceInfo) ServiceInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        serviceInfo = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return serviceInfo;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ProviderInfo getProviderInfo(ComponentName className, int flags, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ProviderInfo providerInfo;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (className != null) {
                        _data.writeInt(Stub.TRANSACTION_checkPackageStartable);
                        className.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(flags);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_getProviderInfo, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        providerInfo = (ProviderInfo) ProviderInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        providerInfo = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return providerInfo;
                } catch (Throwable th) {
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
                    this.mRemote.transact(Stub.TRANSACTION_checkPermission, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_checkUidPermission, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    if (info != null) {
                        _data.writeInt(Stub.TRANSACTION_checkPackageStartable);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_addPermission, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
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
                    this.mRemote.transact(Stub.TRANSACTION_removePermission, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_grantRuntimePermission, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_revokeRuntimePermission, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_resetRuntimePermissions, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_getPermissionFlags, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_updatePermissionFlags, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_updatePermissionFlagsForAllApps, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_shouldShowRequestPermissionRationale, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
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
                    this.mRemote.transact(Stub.TRANSACTION_isProtectedBroadcast, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
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
                    this.mRemote.transact(Stub.TRANSACTION_checkSignatures, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_checkUidSignatures, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_getAllPackages, _data, _reply, 0);
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_getPackagesForUid, _data, _reply, 0);
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_getNameForUid, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_getUidForSharedUser, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_getFlagsForUid, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_getPrivateFlagsForUid, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_isUidPrivileged, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
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
                    this.mRemote.transact(Stub.TRANSACTION_getAppOpPermissionPackages, _data, _reply, 0);
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ResolveInfo resolveIntent(Intent intent, String resolvedType, int flags, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ResolveInfo resolveInfo;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (intent != null) {
                        _data.writeInt(Stub.TRANSACTION_checkPackageStartable);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(resolvedType);
                    _data.writeInt(flags);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_resolveIntent, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        resolveInfo = (ResolveInfo) ResolveInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        resolveInfo = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return resolveInfo;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean canForwardTo(Intent intent, String resolvedType, int sourceUserId, int targetUserId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (intent != null) {
                        _data.writeInt(Stub.TRANSACTION_checkPackageStartable);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(resolvedType);
                    _data.writeInt(sourceUserId);
                    _data.writeInt(targetUserId);
                    this.mRemote.transact(Stub.TRANSACTION_canForwardTo, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParceledListSlice queryIntentActivities(Intent intent, String resolvedType, int flags, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ParceledListSlice parceledListSlice;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (intent != null) {
                        _data.writeInt(Stub.TRANSACTION_checkPackageStartable);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(resolvedType);
                    _data.writeInt(flags);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_queryIntentActivities, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        parceledListSlice = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(_reply);
                    } else {
                        parceledListSlice = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return parceledListSlice;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParceledListSlice queryIntentActivityOptions(ComponentName caller, Intent[] specifics, String[] specificTypes, Intent intent, String resolvedType, int flags, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ParceledListSlice parceledListSlice;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (caller != null) {
                        _data.writeInt(Stub.TRANSACTION_checkPackageStartable);
                        caller.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeTypedArray(specifics, 0);
                    _data.writeStringArray(specificTypes);
                    if (intent != null) {
                        _data.writeInt(Stub.TRANSACTION_checkPackageStartable);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(resolvedType);
                    _data.writeInt(flags);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_queryIntentActivityOptions, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        parceledListSlice = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(_reply);
                    } else {
                        parceledListSlice = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return parceledListSlice;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParceledListSlice queryIntentReceivers(Intent intent, String resolvedType, int flags, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ParceledListSlice parceledListSlice;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (intent != null) {
                        _data.writeInt(Stub.TRANSACTION_checkPackageStartable);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(resolvedType);
                    _data.writeInt(flags);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_queryIntentReceivers, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        parceledListSlice = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(_reply);
                    } else {
                        parceledListSlice = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return parceledListSlice;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ResolveInfo resolveService(Intent intent, String resolvedType, int flags, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ResolveInfo resolveInfo;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (intent != null) {
                        _data.writeInt(Stub.TRANSACTION_checkPackageStartable);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(resolvedType);
                    _data.writeInt(flags);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_resolveService, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        resolveInfo = (ResolveInfo) ResolveInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        resolveInfo = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return resolveInfo;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParceledListSlice queryIntentServices(Intent intent, String resolvedType, int flags, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ParceledListSlice parceledListSlice;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (intent != null) {
                        _data.writeInt(Stub.TRANSACTION_checkPackageStartable);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(resolvedType);
                    _data.writeInt(flags);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_queryIntentServices, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        parceledListSlice = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(_reply);
                    } else {
                        parceledListSlice = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return parceledListSlice;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParceledListSlice queryIntentContentProviders(Intent intent, String resolvedType, int flags, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ParceledListSlice parceledListSlice;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (intent != null) {
                        _data.writeInt(Stub.TRANSACTION_checkPackageStartable);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(resolvedType);
                    _data.writeInt(flags);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_queryIntentContentProviders, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        parceledListSlice = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(_reply);
                    } else {
                        parceledListSlice = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return parceledListSlice;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParceledListSlice getInstalledPackages(int flags, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ParceledListSlice parceledListSlice;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(flags);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_getInstalledPackages, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        parceledListSlice = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(_reply);
                    } else {
                        parceledListSlice = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return parceledListSlice;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParceledListSlice getPackagesHoldingPermissions(String[] permissions, int flags, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ParceledListSlice parceledListSlice;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringArray(permissions);
                    _data.writeInt(flags);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_getPackagesHoldingPermissions, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        parceledListSlice = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(_reply);
                    } else {
                        parceledListSlice = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return parceledListSlice;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParceledListSlice getInstalledApplications(int flags, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ParceledListSlice parceledListSlice;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(flags);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_getInstalledApplications, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        parceledListSlice = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(_reply);
                    } else {
                        parceledListSlice = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return parceledListSlice;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParceledListSlice getPersistentApplications(int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ParceledListSlice parceledListSlice;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(flags);
                    this.mRemote.transact(Stub.TRANSACTION_getPersistentApplications, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        parceledListSlice = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(_reply);
                    } else {
                        parceledListSlice = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return parceledListSlice;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ProviderInfo resolveContentProvider(String name, int flags, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ProviderInfo providerInfo;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    _data.writeInt(flags);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_resolveContentProvider, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        providerInfo = (ProviderInfo) ProviderInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        providerInfo = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return providerInfo;
                } catch (Throwable th) {
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
                    this.mRemote.transact(Stub.TRANSACTION_querySyncProviders, _data, _reply, 0);
                    _reply.readException();
                    _reply.readStringList(outNames);
                    _reply.readTypedList(outInfo, ProviderInfo.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParceledListSlice queryContentProviders(String processName, int uid, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ParceledListSlice parceledListSlice;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(processName);
                    _data.writeInt(uid);
                    _data.writeInt(flags);
                    this.mRemote.transact(Stub.TRANSACTION_queryContentProviders, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        parceledListSlice = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(_reply);
                    } else {
                        parceledListSlice = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return parceledListSlice;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public InstrumentationInfo getInstrumentationInfo(ComponentName className, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    InstrumentationInfo instrumentationInfo;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (className != null) {
                        _data.writeInt(Stub.TRANSACTION_checkPackageStartable);
                        className.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(flags);
                    this.mRemote.transact(Stub.TRANSACTION_getInstrumentationInfo, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        instrumentationInfo = (InstrumentationInfo) InstrumentationInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        instrumentationInfo = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return instrumentationInfo;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParceledListSlice queryInstrumentation(String targetPackage, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ParceledListSlice parceledListSlice;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(targetPackage);
                    _data.writeInt(flags);
                    this.mRemote.transact(Stub.TRANSACTION_queryInstrumentation, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        parceledListSlice = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(_reply);
                    } else {
                        parceledListSlice = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return parceledListSlice;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void installPackageAsUser(String originPath, IPackageInstallObserver2 observer, int flags, String installerPackageName, int userId) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(originPath);
                    if (observer != null) {
                        iBinder = observer.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(flags);
                    _data.writeString(installerPackageName);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_installPackageAsUser, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void finishPackageInstall(int token, boolean didLaunch) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(token);
                    if (didLaunch) {
                        i = Stub.TRANSACTION_checkPackageStartable;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_finishPackageInstall, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_setInstallerPackageName, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void deletePackageAsUser(String packageName, IPackageDeleteObserver observer, int userId, int flags) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (observer != null) {
                        iBinder = observer.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(userId);
                    _data.writeInt(flags);
                    this.mRemote.transact(Stub.TRANSACTION_deletePackageAsUser, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void deletePackage(String packageName, IPackageDeleteObserver2 observer, int userId, int flags) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (observer != null) {
                        iBinder = observer.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(userId);
                    _data.writeInt(flags);
                    this.mRemote.transact(Stub.TRANSACTION_deletePackage, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_getInstallerPackageName, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_resetApplicationPreferences, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ResolveInfo getLastChosenActivity(Intent intent, String resolvedType, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ResolveInfo resolveInfo;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (intent != null) {
                        _data.writeInt(Stub.TRANSACTION_checkPackageStartable);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(resolvedType);
                    _data.writeInt(flags);
                    this.mRemote.transact(Stub.TRANSACTION_getLastChosenActivity, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        resolveInfo = (ResolveInfo) ResolveInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        resolveInfo = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return resolveInfo;
                } catch (Throwable th) {
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
                        _data.writeInt(Stub.TRANSACTION_checkPackageStartable);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(resolvedType);
                    _data.writeInt(flags);
                    if (filter != null) {
                        _data.writeInt(Stub.TRANSACTION_checkPackageStartable);
                        filter.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(match);
                    if (activity != null) {
                        _data.writeInt(Stub.TRANSACTION_checkPackageStartable);
                        activity.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_setLastChosenActivity, _data, _reply, 0);
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
                        _data.writeInt(Stub.TRANSACTION_checkPackageStartable);
                        filter.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(match);
                    _data.writeTypedArray(set, 0);
                    if (activity != null) {
                        _data.writeInt(Stub.TRANSACTION_checkPackageStartable);
                        activity.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_addPreferredActivity, _data, _reply, 0);
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
                        _data.writeInt(Stub.TRANSACTION_checkPackageStartable);
                        filter.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(match);
                    _data.writeTypedArray(set, 0);
                    if (activity != null) {
                        _data.writeInt(Stub.TRANSACTION_checkPackageStartable);
                        activity.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_replacePreferredActivity, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_clearPackagePreferredActivities, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_getPreferredActivities, _data, _reply, 0);
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
                        _data.writeInt(Stub.TRANSACTION_checkPackageStartable);
                        filter.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (activity != null) {
                        _data.writeInt(Stub.TRANSACTION_checkPackageStartable);
                        activity.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_addPersistentPreferredActivity, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_clearPackagePersistentPreferredActivities, _data, _reply, 0);
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
                        _data.writeInt(Stub.TRANSACTION_checkPackageStartable);
                        intentFilter.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(ownerPackage);
                    _data.writeInt(sourceUserId);
                    _data.writeInt(targetUserId);
                    _data.writeInt(flags);
                    this.mRemote.transact(Stub.TRANSACTION_addCrossProfileIntentFilter, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_clearCrossProfileIntentFilters, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String[] setPackagesSuspendedAsUser(String[] packageNames, boolean suspended, int userId) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringArray(packageNames);
                    if (suspended) {
                        i = Stub.TRANSACTION_checkPackageStartable;
                    }
                    _data.writeInt(i);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_setPackagesSuspendedAsUser, _data, _reply, 0);
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_isPackageSuspendedForUser, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
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
                    this.mRemote.transact(Stub.TRANSACTION_getPreferredActivityBackup, _data, _reply, 0);
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_restorePreferredActivities, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_getDefaultAppsBackup, _data, _reply, 0);
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_restoreDefaultApps, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_getIntentFilterVerificationBackup, _data, _reply, 0);
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_restoreIntentFilterVerification, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_getPermissionGrantBackup, _data, _reply, 0);
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_restorePermissionGrants, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ComponentName getHomeActivities(List<ResolveInfo> outHomeCandidates) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ComponentName componentName;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getHomeActivities, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        componentName = (ComponentName) ComponentName.CREATOR.createFromParcel(_reply);
                    } else {
                        componentName = null;
                    }
                    _reply.readTypedList(outHomeCandidates, ResolveInfo.CREATOR);
                    return componentName;
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
                        _data.writeInt(Stub.TRANSACTION_checkPackageStartable);
                        className.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_setHomeActivity, _data, _reply, 0);
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
                        _data.writeInt(Stub.TRANSACTION_checkPackageStartable);
                        componentName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(newState);
                    _data.writeInt(flags);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_setComponentEnabledSetting, _data, _reply, 0);
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
                        _data.writeInt(Stub.TRANSACTION_checkPackageStartable);
                        componentName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_getComponentEnabledSetting, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_setApplicationEnabledSetting, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_getApplicationEnabledSetting, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_logAppProcessStartIfNeeded, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_flushPackageRestrictionsAsUser, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setPackageStoppedState(String packageName, boolean stopped, int userId) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (stopped) {
                        i = Stub.TRANSACTION_checkPackageStartable;
                    }
                    _data.writeInt(i);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_setPackageStoppedState, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void freeStorageAndNotify(String volumeUuid, long freeStorageSize, IPackageDataObserver observer) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(volumeUuid);
                    _data.writeLong(freeStorageSize);
                    if (observer != null) {
                        iBinder = observer.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_freeStorageAndNotify, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void freeStorage(String volumeUuid, long freeStorageSize, IntentSender pi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(volumeUuid);
                    _data.writeLong(freeStorageSize);
                    if (pi != null) {
                        _data.writeInt(Stub.TRANSACTION_checkPackageStartable);
                        pi.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_freeStorage, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void deleteApplicationCacheFiles(String packageName, IPackageDataObserver observer) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (observer != null) {
                        iBinder = observer.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_deleteApplicationCacheFiles, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void deleteApplicationCacheFilesAsUser(String packageName, int userId, IPackageDataObserver observer) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    if (observer != null) {
                        iBinder = observer.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_deleteApplicationCacheFilesAsUser, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void clearApplicationUserData(String packageName, IPackageDataObserver observer, int userId) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (observer != null) {
                        iBinder = observer.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_clearApplicationUserData, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_clearApplicationProfileData, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void getPackageSizeInfo(String packageName, int userHandle, IPackageStatsObserver observer) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userHandle);
                    if (observer != null) {
                        iBinder = observer.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_getPackageSizeInfo, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_getSystemSharedLibraryNames, _data, _reply, 0);
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParceledListSlice getSystemAvailableFeatures() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ParceledListSlice parceledListSlice;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getSystemAvailableFeatures, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        parceledListSlice = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(_reply);
                    } else {
                        parceledListSlice = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return parceledListSlice;
                } catch (Throwable th) {
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
                    this.mRemote.transact(Stub.TRANSACTION_hasSystemFeature, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void enterSafeMode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_enterSafeMode, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_isSafeMode, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void systemReady() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_systemReady, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_hasSystemUidErrors, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void performFstrimIfNeeded() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_performFstrimIfNeeded, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_updatePackagesIfNeeded, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void notifyPackageUse(String packageName, int reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(reason);
                    this.mRemote.transact(Stub.TRANSACTION_notifyPackageUse, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean performDexOptIfNeeded(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    this.mRemote.transact(Stub.TRANSACTION_performDexOptIfNeeded, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean performDexOpt(String packageName, boolean checkProfiles, int compileReason, boolean force) throws RemoteException {
                int i = Stub.TRANSACTION_checkPackageStartable;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    int i2;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (checkProfiles) {
                        i2 = Stub.TRANSACTION_checkPackageStartable;
                    } else {
                        i2 = 0;
                    }
                    _data.writeInt(i2);
                    _data.writeInt(compileReason);
                    if (!force) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_performDexOpt, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean performDexOptMode(String packageName, boolean checkProfiles, String targetCompilerFilter, boolean force) throws RemoteException {
                int i = Stub.TRANSACTION_checkPackageStartable;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    int i2;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (checkProfiles) {
                        i2 = Stub.TRANSACTION_checkPackageStartable;
                    } else {
                        i2 = 0;
                    }
                    _data.writeInt(i2);
                    _data.writeString(targetCompilerFilter);
                    if (!force) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_performDexOptMode, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
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
                    this.mRemote.transact(Stub.TRANSACTION_dumpProfiles, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_forceDexOpt, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void updateExternalMediaStatus(boolean mounted, boolean reportStatus) throws RemoteException {
                int i = Stub.TRANSACTION_checkPackageStartable;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    int i2;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (mounted) {
                        i2 = Stub.TRANSACTION_checkPackageStartable;
                    } else {
                        i2 = 0;
                    }
                    _data.writeInt(i2);
                    if (!reportStatus) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_updateExternalMediaStatus, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public PackageCleanItem nextPackageToClean(PackageCleanItem lastPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    PackageCleanItem packageCleanItem;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (lastPackage != null) {
                        _data.writeInt(Stub.TRANSACTION_checkPackageStartable);
                        lastPackage.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_nextPackageToClean, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        packageCleanItem = (PackageCleanItem) PackageCleanItem.CREATOR.createFromParcel(_reply);
                    } else {
                        packageCleanItem = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return packageCleanItem;
                } catch (Throwable th) {
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
                    this.mRemote.transact(Stub.TRANSACTION_getMoveStatus, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registerMoveCallback(IPackageMoveObserver callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_registerMoveCallback, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unregisterMoveCallback(IPackageMoveObserver callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_unregisterMoveCallback, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_movePackage, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_movePrimaryStorage, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    if (info != null) {
                        _data.writeInt(Stub.TRANSACTION_checkPackageStartable);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_addPermissionAsync, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
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
                    this.mRemote.transact(Stub.TRANSACTION_setInstallLocation, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getInstallLocation() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getInstallLocation, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int installExistingPackageAsUser(String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_installExistingPackageAsUser, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_verifyPendingInstall, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_extendVerificationTimeout, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_verifyIntentFilter, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_getIntentVerificationStatus, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_updateIntentVerificationStatus, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParceledListSlice getIntentFilterVerifications(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ParceledListSlice parceledListSlice;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    this.mRemote.transact(Stub.TRANSACTION_getIntentFilterVerifications, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        parceledListSlice = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(_reply);
                    } else {
                        parceledListSlice = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return parceledListSlice;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParceledListSlice getAllIntentFilters(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ParceledListSlice parceledListSlice;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    this.mRemote.transact(Stub.TRANSACTION_getAllIntentFilters, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        parceledListSlice = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(_reply);
                    } else {
                        parceledListSlice = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return parceledListSlice;
                } catch (Throwable th) {
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
                    this.mRemote.transact(Stub.TRANSACTION_setDefaultBrowserPackageName, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
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
                    this.mRemote.transact(Stub.TRANSACTION_getDefaultBrowserPackageName, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public VerifierDeviceIdentity getVerifierDeviceIdentity() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    VerifierDeviceIdentity verifierDeviceIdentity;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getVerifierDeviceIdentity, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        verifierDeviceIdentity = (VerifierDeviceIdentity) VerifierDeviceIdentity.CREATOR.createFromParcel(_reply);
                    } else {
                        verifierDeviceIdentity = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return verifierDeviceIdentity;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isFirstBoot() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isFirstBoot, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isOnlyCoreApps() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isOnlyCoreApps, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isUpgrade() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isUpgrade, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setPermissionEnforced(String permission, boolean enforced) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(permission);
                    if (enforced) {
                        i = Stub.TRANSACTION_checkPackageStartable;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setPermissionEnforced, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_isPermissionEnforced, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isStorageLow() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isStorageLow, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setApplicationHiddenSettingAsUser(String packageName, boolean hidden, int userId) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (hidden) {
                        i = Stub.TRANSACTION_checkPackageStartable;
                    }
                    _data.writeInt(i);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_setApplicationHiddenSettingAsUser, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
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
                    this.mRemote.transact(Stub.TRANSACTION_getApplicationHiddenSettingAsUser, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public IPackageInstaller getPackageInstaller() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getPackageInstaller, _data, _reply, 0);
                    _reply.readException();
                    IPackageInstaller _result = android.content.pm.IPackageInstaller.Stub.asInterface(_reply.readStrongBinder());
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setBlockUninstallForUser(String packageName, boolean blockUninstall, int userId) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (blockUninstall) {
                        i = Stub.TRANSACTION_checkPackageStartable;
                    }
                    _data.writeInt(i);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_setBlockUninstallForUser, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
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
                    this.mRemote.transact(Stub.TRANSACTION_getBlockUninstallForUser, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public KeySet getKeySetByAlias(String packageName, String alias) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    KeySet keySet;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(alias);
                    this.mRemote.transact(Stub.TRANSACTION_getKeySetByAlias, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        keySet = (KeySet) KeySet.CREATOR.createFromParcel(_reply);
                    } else {
                        keySet = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return keySet;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public KeySet getSigningKeySet(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    KeySet keySet;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    this.mRemote.transact(Stub.TRANSACTION_getSigningKeySet, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        keySet = (KeySet) KeySet.CREATOR.createFromParcel(_reply);
                    } else {
                        keySet = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return keySet;
                } catch (Throwable th) {
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
                    if (ks != null) {
                        _data.writeInt(Stub.TRANSACTION_checkPackageStartable);
                        ks.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_isPackageSignedByKeySet, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
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
                    if (ks != null) {
                        _data.writeInt(Stub.TRANSACTION_checkPackageStartable);
                        ks.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_isPackageSignedByKeySetExactly, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addOnPermissionsChangeListener(IOnPermissionsChangeListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_addOnPermissionsChangeListener, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeOnPermissionsChangeListener(IOnPermissionsChangeListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_removeOnPermissionsChangeListener, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_grantDefaultPermissionsToEnabledCarrierApps, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_isPermissionRevokedByPolicy, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getPermissionControllerPackageName() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getPermissionControllerPackageName, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParceledListSlice getEphemeralApplications(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ParceledListSlice parceledListSlice;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_getEphemeralApplications, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        parceledListSlice = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(_reply);
                    } else {
                        parceledListSlice = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return parceledListSlice;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public byte[] getEphemeralApplicationCookie(String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_getEphemeralApplicationCookie, _data, _reply, 0);
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setEphemeralApplicationCookie(String packageName, byte[] cookie, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeByteArray(cookie);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_setEphemeralApplicationCookie, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Bitmap getEphemeralApplicationIcon(String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    Bitmap bitmap;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_getEphemeralApplicationIcon, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        bitmap = (Bitmap) Bitmap.CREATOR.createFromParcel(_reply);
                    } else {
                        bitmap = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return bitmap;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isEphemeralApplication(String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_isEphemeralApplication, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setRequiredForSystemUser(String packageName, boolean systemUserApp) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (systemUserApp) {
                        i = Stub.TRANSACTION_checkPackageStartable;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setRequiredForSystemUser, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getServicesSystemSharedLibraryPackageName() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getServicesSystemSharedLibraryPackageName, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_getSharedSystemSharedLibraryPackageName, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
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
                    this.mRemote.transact(Stub.TRANSACTION_isPackageDeviceAdminOnAnyUser, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> getPreviousCodePaths(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    this.mRemote.transact(Stub.TRANSACTION_getPreviousCodePaths, _data, _reply, 0);
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    return _result;
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

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            boolean _result;
            int _result2;
            String[] _result3;
            ParceledListSlice _result4;
            ComponentName componentName;
            ActivityInfo _result5;
            ProviderInfo _result6;
            PermissionInfo permissionInfo;
            List<String> _result7;
            String _result8;
            Intent intent;
            ResolveInfo _result9;
            IntentFilter intentFilter;
            int _arg1;
            ComponentName[] _arg2;
            ComponentName componentName2;
            byte[] _result10;
            String _arg0;
            KeySet _result11;
            KeySet keySet;
            switch (code) {
                case TRANSACTION_checkPackageStartable /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    checkPackageStartable(data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_isPackageAvailable /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isPackageAvailable(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_checkPackageStartable : 0);
                    return true;
                case TRANSACTION_getPackageInfo /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    PackageInfo _result12 = getPackageInfo(data.readString(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    if (_result12 != null) {
                        reply.writeInt(TRANSACTION_checkPackageStartable);
                        _result12.writeToParcel(reply, TRANSACTION_checkPackageStartable);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getPackageUid /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getPackageUid(data.readString(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_getPackageGids /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    int[] _result13 = getPackageGids(data.readString(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeIntArray(_result13);
                    return true;
                case TRANSACTION_currentToCanonicalPackageNames /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = currentToCanonicalPackageNames(data.createStringArray());
                    reply.writeNoException();
                    reply.writeStringArray(_result3);
                    return true;
                case TRANSACTION_canonicalToCurrentPackageNames /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = canonicalToCurrentPackageNames(data.createStringArray());
                    reply.writeNoException();
                    reply.writeStringArray(_result3);
                    return true;
                case TRANSACTION_getPermissionInfo /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    PermissionInfo _result14 = getPermissionInfo(data.readString(), data.readInt());
                    reply.writeNoException();
                    if (_result14 != null) {
                        reply.writeInt(TRANSACTION_checkPackageStartable);
                        _result14.writeToParcel(reply, TRANSACTION_checkPackageStartable);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_queryPermissionsByGroup /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = queryPermissionsByGroup(data.readString(), data.readInt());
                    reply.writeNoException();
                    if (_result4 != null) {
                        reply.writeInt(TRANSACTION_checkPackageStartable);
                        _result4.writeToParcel(reply, TRANSACTION_checkPackageStartable);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getPermissionGroupInfo /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    PermissionGroupInfo _result15 = getPermissionGroupInfo(data.readString(), data.readInt());
                    reply.writeNoException();
                    if (_result15 != null) {
                        reply.writeInt(TRANSACTION_checkPackageStartable);
                        _result15.writeToParcel(reply, TRANSACTION_checkPackageStartable);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getAllPermissionGroups /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getAllPermissionGroups(data.readInt());
                    reply.writeNoException();
                    if (_result4 != null) {
                        reply.writeInt(TRANSACTION_checkPackageStartable);
                        _result4.writeToParcel(reply, TRANSACTION_checkPackageStartable);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getApplicationInfo /*12*/:
                    data.enforceInterface(DESCRIPTOR);
                    ApplicationInfo _result16 = getApplicationInfo(data.readString(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    if (_result16 != null) {
                        reply.writeInt(TRANSACTION_checkPackageStartable);
                        _result16.writeToParcel(reply, TRANSACTION_checkPackageStartable);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getActivityInfo /*13*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        componentName = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                    } else {
                        componentName = null;
                    }
                    _result5 = getActivityInfo(componentName, data.readInt(), data.readInt());
                    reply.writeNoException();
                    if (_result5 != null) {
                        reply.writeInt(TRANSACTION_checkPackageStartable);
                        _result5.writeToParcel(reply, TRANSACTION_checkPackageStartable);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_activitySupportsIntent /*14*/:
                    Intent intent2;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        componentName = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                    } else {
                        componentName = null;
                    }
                    if (data.readInt() != 0) {
                        intent2 = (Intent) Intent.CREATOR.createFromParcel(data);
                    } else {
                        intent2 = null;
                    }
                    _result = activitySupportsIntent(componentName, intent2, data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_checkPackageStartable : 0);
                    return true;
                case TRANSACTION_getReceiverInfo /*15*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        componentName = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                    } else {
                        componentName = null;
                    }
                    _result5 = getReceiverInfo(componentName, data.readInt(), data.readInt());
                    reply.writeNoException();
                    if (_result5 != null) {
                        reply.writeInt(TRANSACTION_checkPackageStartable);
                        _result5.writeToParcel(reply, TRANSACTION_checkPackageStartable);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getServiceInfo /*16*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        componentName = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                    } else {
                        componentName = null;
                    }
                    ServiceInfo _result17 = getServiceInfo(componentName, data.readInt(), data.readInt());
                    reply.writeNoException();
                    if (_result17 != null) {
                        reply.writeInt(TRANSACTION_checkPackageStartable);
                        _result17.writeToParcel(reply, TRANSACTION_checkPackageStartable);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getProviderInfo /*17*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        componentName = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                    } else {
                        componentName = null;
                    }
                    _result6 = getProviderInfo(componentName, data.readInt(), data.readInt());
                    reply.writeNoException();
                    if (_result6 != null) {
                        reply.writeInt(TRANSACTION_checkPackageStartable);
                        _result6.writeToParcel(reply, TRANSACTION_checkPackageStartable);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_checkPermission /*18*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = checkPermission(data.readString(), data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_checkUidPermission /*19*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = checkUidPermission(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_addPermission /*20*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        permissionInfo = (PermissionInfo) PermissionInfo.CREATOR.createFromParcel(data);
                    } else {
                        permissionInfo = null;
                    }
                    _result = addPermission(permissionInfo);
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_checkPackageStartable : 0);
                    return true;
                case TRANSACTION_removePermission /*21*/:
                    data.enforceInterface(DESCRIPTOR);
                    removePermission(data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_grantRuntimePermission /*22*/:
                    data.enforceInterface(DESCRIPTOR);
                    grantRuntimePermission(data.readString(), data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_revokeRuntimePermission /*23*/:
                    data.enforceInterface(DESCRIPTOR);
                    revokeRuntimePermission(data.readString(), data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_resetRuntimePermissions /*24*/:
                    data.enforceInterface(DESCRIPTOR);
                    resetRuntimePermissions();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getPermissionFlags /*25*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getPermissionFlags(data.readString(), data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_updatePermissionFlags /*26*/:
                    data.enforceInterface(DESCRIPTOR);
                    updatePermissionFlags(data.readString(), data.readString(), data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_updatePermissionFlagsForAllApps /*27*/:
                    data.enforceInterface(DESCRIPTOR);
                    updatePermissionFlagsForAllApps(data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_shouldShowRequestPermissionRationale /*28*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = shouldShowRequestPermissionRationale(data.readString(), data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_checkPackageStartable : 0);
                    return true;
                case TRANSACTION_isProtectedBroadcast /*29*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isProtectedBroadcast(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_checkPackageStartable : 0);
                    return true;
                case TRANSACTION_checkSignatures /*30*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = checkSignatures(data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_checkUidSignatures /*31*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = checkUidSignatures(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_getAllPackages /*32*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result7 = getAllPackages();
                    reply.writeNoException();
                    reply.writeStringList(_result7);
                    return true;
                case TRANSACTION_getPackagesForUid /*33*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getPackagesForUid(data.readInt());
                    reply.writeNoException();
                    reply.writeStringArray(_result3);
                    return true;
                case TRANSACTION_getNameForUid /*34*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result8 = getNameForUid(data.readInt());
                    reply.writeNoException();
                    reply.writeString(_result8);
                    return true;
                case TRANSACTION_getUidForSharedUser /*35*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getUidForSharedUser(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_getFlagsForUid /*36*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getFlagsForUid(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_getPrivateFlagsForUid /*37*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getPrivateFlagsForUid(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_isUidPrivileged /*38*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isUidPrivileged(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_checkPackageStartable : 0);
                    return true;
                case TRANSACTION_getAppOpPermissionPackages /*39*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getAppOpPermissionPackages(data.readString());
                    reply.writeNoException();
                    reply.writeStringArray(_result3);
                    return true;
                case TRANSACTION_resolveIntent /*40*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        intent = (Intent) Intent.CREATOR.createFromParcel(data);
                    } else {
                        intent = null;
                    }
                    _result9 = resolveIntent(intent, data.readString(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    if (_result9 != null) {
                        reply.writeInt(TRANSACTION_checkPackageStartable);
                        _result9.writeToParcel(reply, TRANSACTION_checkPackageStartable);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_canForwardTo /*41*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        intent = (Intent) Intent.CREATOR.createFromParcel(data);
                    } else {
                        intent = null;
                    }
                    _result = canForwardTo(intent, data.readString(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_checkPackageStartable : 0);
                    return true;
                case TRANSACTION_queryIntentActivities /*42*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        intent = (Intent) Intent.CREATOR.createFromParcel(data);
                    } else {
                        intent = null;
                    }
                    _result4 = queryIntentActivities(intent, data.readString(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    if (_result4 != null) {
                        reply.writeInt(TRANSACTION_checkPackageStartable);
                        _result4.writeToParcel(reply, TRANSACTION_checkPackageStartable);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_queryIntentActivityOptions /*43*/:
                    Intent intent3;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        componentName = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                    } else {
                        componentName = null;
                    }
                    Intent[] _arg12 = (Intent[]) data.createTypedArray(Intent.CREATOR);
                    String[] _arg22 = data.createStringArray();
                    if (data.readInt() != 0) {
                        intent3 = (Intent) Intent.CREATOR.createFromParcel(data);
                    } else {
                        intent3 = null;
                    }
                    _result4 = queryIntentActivityOptions(componentName, _arg12, _arg22, intent3, data.readString(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    if (_result4 != null) {
                        reply.writeInt(TRANSACTION_checkPackageStartable);
                        _result4.writeToParcel(reply, TRANSACTION_checkPackageStartable);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_queryIntentReceivers /*44*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        intent = (Intent) Intent.CREATOR.createFromParcel(data);
                    } else {
                        intent = null;
                    }
                    _result4 = queryIntentReceivers(intent, data.readString(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    if (_result4 != null) {
                        reply.writeInt(TRANSACTION_checkPackageStartable);
                        _result4.writeToParcel(reply, TRANSACTION_checkPackageStartable);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_resolveService /*45*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        intent = (Intent) Intent.CREATOR.createFromParcel(data);
                    } else {
                        intent = null;
                    }
                    _result9 = resolveService(intent, data.readString(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    if (_result9 != null) {
                        reply.writeInt(TRANSACTION_checkPackageStartable);
                        _result9.writeToParcel(reply, TRANSACTION_checkPackageStartable);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_queryIntentServices /*46*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        intent = (Intent) Intent.CREATOR.createFromParcel(data);
                    } else {
                        intent = null;
                    }
                    _result4 = queryIntentServices(intent, data.readString(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    if (_result4 != null) {
                        reply.writeInt(TRANSACTION_checkPackageStartable);
                        _result4.writeToParcel(reply, TRANSACTION_checkPackageStartable);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_queryIntentContentProviders /*47*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        intent = (Intent) Intent.CREATOR.createFromParcel(data);
                    } else {
                        intent = null;
                    }
                    _result4 = queryIntentContentProviders(intent, data.readString(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    if (_result4 != null) {
                        reply.writeInt(TRANSACTION_checkPackageStartable);
                        _result4.writeToParcel(reply, TRANSACTION_checkPackageStartable);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getInstalledPackages /*48*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getInstalledPackages(data.readInt(), data.readInt());
                    reply.writeNoException();
                    if (_result4 != null) {
                        reply.writeInt(TRANSACTION_checkPackageStartable);
                        _result4.writeToParcel(reply, TRANSACTION_checkPackageStartable);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getPackagesHoldingPermissions /*49*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getPackagesHoldingPermissions(data.createStringArray(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    if (_result4 != null) {
                        reply.writeInt(TRANSACTION_checkPackageStartable);
                        _result4.writeToParcel(reply, TRANSACTION_checkPackageStartable);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getInstalledApplications /*50*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getInstalledApplications(data.readInt(), data.readInt());
                    reply.writeNoException();
                    if (_result4 != null) {
                        reply.writeInt(TRANSACTION_checkPackageStartable);
                        _result4.writeToParcel(reply, TRANSACTION_checkPackageStartable);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getPersistentApplications /*51*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getPersistentApplications(data.readInt());
                    reply.writeNoException();
                    if (_result4 != null) {
                        reply.writeInt(TRANSACTION_checkPackageStartable);
                        _result4.writeToParcel(reply, TRANSACTION_checkPackageStartable);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_resolveContentProvider /*52*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result6 = resolveContentProvider(data.readString(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    if (_result6 != null) {
                        reply.writeInt(TRANSACTION_checkPackageStartable);
                        _result6.writeToParcel(reply, TRANSACTION_checkPackageStartable);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_querySyncProviders /*53*/:
                    data.enforceInterface(DESCRIPTOR);
                    List<String> _arg02 = data.createStringArrayList();
                    List<ProviderInfo> _arg13 = data.createTypedArrayList(ProviderInfo.CREATOR);
                    querySyncProviders(_arg02, _arg13);
                    reply.writeNoException();
                    reply.writeStringList(_arg02);
                    reply.writeTypedList(_arg13);
                    return true;
                case TRANSACTION_queryContentProviders /*54*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = queryContentProviders(data.readString(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    if (_result4 != null) {
                        reply.writeInt(TRANSACTION_checkPackageStartable);
                        _result4.writeToParcel(reply, TRANSACTION_checkPackageStartable);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getInstrumentationInfo /*55*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        componentName = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                    } else {
                        componentName = null;
                    }
                    InstrumentationInfo _result18 = getInstrumentationInfo(componentName, data.readInt());
                    reply.writeNoException();
                    if (_result18 != null) {
                        reply.writeInt(TRANSACTION_checkPackageStartable);
                        _result18.writeToParcel(reply, TRANSACTION_checkPackageStartable);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_queryInstrumentation /*56*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = queryInstrumentation(data.readString(), data.readInt());
                    reply.writeNoException();
                    if (_result4 != null) {
                        reply.writeInt(TRANSACTION_checkPackageStartable);
                        _result4.writeToParcel(reply, TRANSACTION_checkPackageStartable);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_installPackageAsUser /*57*/:
                    data.enforceInterface(DESCRIPTOR);
                    installPackageAsUser(data.readString(), android.content.pm.IPackageInstallObserver2.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_finishPackageInstall /*58*/:
                    data.enforceInterface(DESCRIPTOR);
                    finishPackageInstall(data.readInt(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setInstallerPackageName /*59*/:
                    data.enforceInterface(DESCRIPTOR);
                    setInstallerPackageName(data.readString(), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_deletePackageAsUser /*60*/:
                    data.enforceInterface(DESCRIPTOR);
                    deletePackageAsUser(data.readString(), android.content.pm.IPackageDeleteObserver.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_deletePackage /*61*/:
                    data.enforceInterface(DESCRIPTOR);
                    deletePackage(data.readString(), android.content.pm.IPackageDeleteObserver2.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getInstallerPackageName /*62*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result8 = getInstallerPackageName(data.readString());
                    reply.writeNoException();
                    reply.writeString(_result8);
                    return true;
                case TRANSACTION_resetApplicationPreferences /*63*/:
                    data.enforceInterface(DESCRIPTOR);
                    resetApplicationPreferences(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getLastChosenActivity /*64*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        intent = (Intent) Intent.CREATOR.createFromParcel(data);
                    } else {
                        intent = null;
                    }
                    _result9 = getLastChosenActivity(intent, data.readString(), data.readInt());
                    reply.writeNoException();
                    if (_result9 != null) {
                        reply.writeInt(TRANSACTION_checkPackageStartable);
                        _result9.writeToParcel(reply, TRANSACTION_checkPackageStartable);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_setLastChosenActivity /*65*/:
                    IntentFilter intentFilter2;
                    ComponentName componentName3;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        intent = (Intent) Intent.CREATOR.createFromParcel(data);
                    } else {
                        intent = null;
                    }
                    String _arg14 = data.readString();
                    int _arg23 = data.readInt();
                    if (data.readInt() != 0) {
                        intentFilter2 = (IntentFilter) IntentFilter.CREATOR.createFromParcel(data);
                    } else {
                        intentFilter2 = null;
                    }
                    int _arg4 = data.readInt();
                    if (data.readInt() != 0) {
                        componentName3 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                    } else {
                        componentName3 = null;
                    }
                    setLastChosenActivity(intent, _arg14, _arg23, intentFilter2, _arg4, componentName3);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_addPreferredActivity /*66*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        intentFilter = (IntentFilter) IntentFilter.CREATOR.createFromParcel(data);
                    } else {
                        intentFilter = null;
                    }
                    _arg1 = data.readInt();
                    _arg2 = (ComponentName[]) data.createTypedArray(ComponentName.CREATOR);
                    if (data.readInt() != 0) {
                        componentName2 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                    } else {
                        componentName2 = null;
                    }
                    addPreferredActivity(intentFilter, _arg1, _arg2, componentName2, data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_replacePreferredActivity /*67*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        intentFilter = (IntentFilter) IntentFilter.CREATOR.createFromParcel(data);
                    } else {
                        intentFilter = null;
                    }
                    _arg1 = data.readInt();
                    _arg2 = (ComponentName[]) data.createTypedArray(ComponentName.CREATOR);
                    if (data.readInt() != 0) {
                        componentName2 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                    } else {
                        componentName2 = null;
                    }
                    replacePreferredActivity(intentFilter, _arg1, _arg2, componentName2, data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_clearPackagePreferredActivities /*68*/:
                    data.enforceInterface(DESCRIPTOR);
                    clearPackagePreferredActivities(data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getPreferredActivities /*69*/:
                    data.enforceInterface(DESCRIPTOR);
                    List<IntentFilter> _arg03 = new ArrayList();
                    List<ComponentName> _arg15 = new ArrayList();
                    _result2 = getPreferredActivities(_arg03, _arg15, data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    reply.writeTypedList(_arg03);
                    reply.writeTypedList(_arg15);
                    return true;
                case TRANSACTION_addPersistentPreferredActivity /*70*/:
                    ComponentName componentName4;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        intentFilter = (IntentFilter) IntentFilter.CREATOR.createFromParcel(data);
                    } else {
                        intentFilter = null;
                    }
                    if (data.readInt() != 0) {
                        componentName4 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                    } else {
                        componentName4 = null;
                    }
                    addPersistentPreferredActivity(intentFilter, componentName4, data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_clearPackagePersistentPreferredActivities /*71*/:
                    data.enforceInterface(DESCRIPTOR);
                    clearPackagePersistentPreferredActivities(data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_addCrossProfileIntentFilter /*72*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        intentFilter = (IntentFilter) IntentFilter.CREATOR.createFromParcel(data);
                    } else {
                        intentFilter = null;
                    }
                    addCrossProfileIntentFilter(intentFilter, data.readString(), data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_clearCrossProfileIntentFilters /*73*/:
                    data.enforceInterface(DESCRIPTOR);
                    clearCrossProfileIntentFilters(data.readInt(), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setPackagesSuspendedAsUser /*74*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = setPackagesSuspendedAsUser(data.createStringArray(), data.readInt() != 0, data.readInt());
                    reply.writeNoException();
                    reply.writeStringArray(_result3);
                    return true;
                case TRANSACTION_isPackageSuspendedForUser /*75*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isPackageSuspendedForUser(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_checkPackageStartable : 0);
                    return true;
                case TRANSACTION_getPreferredActivityBackup /*76*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result10 = getPreferredActivityBackup(data.readInt());
                    reply.writeNoException();
                    reply.writeByteArray(_result10);
                    return true;
                case TRANSACTION_restorePreferredActivities /*77*/:
                    data.enforceInterface(DESCRIPTOR);
                    restorePreferredActivities(data.createByteArray(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getDefaultAppsBackup /*78*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result10 = getDefaultAppsBackup(data.readInt());
                    reply.writeNoException();
                    reply.writeByteArray(_result10);
                    return true;
                case TRANSACTION_restoreDefaultApps /*79*/:
                    data.enforceInterface(DESCRIPTOR);
                    restoreDefaultApps(data.createByteArray(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getIntentFilterVerificationBackup /*80*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result10 = getIntentFilterVerificationBackup(data.readInt());
                    reply.writeNoException();
                    reply.writeByteArray(_result10);
                    return true;
                case TRANSACTION_restoreIntentFilterVerification /*81*/:
                    data.enforceInterface(DESCRIPTOR);
                    restoreIntentFilterVerification(data.createByteArray(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getPermissionGrantBackup /*82*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result10 = getPermissionGrantBackup(data.readInt());
                    reply.writeNoException();
                    reply.writeByteArray(_result10);
                    return true;
                case TRANSACTION_restorePermissionGrants /*83*/:
                    data.enforceInterface(DESCRIPTOR);
                    restorePermissionGrants(data.createByteArray(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getHomeActivities /*84*/:
                    data.enforceInterface(DESCRIPTOR);
                    List<ResolveInfo> _arg04 = new ArrayList();
                    ComponentName _result19 = getHomeActivities(_arg04);
                    reply.writeNoException();
                    if (_result19 != null) {
                        reply.writeInt(TRANSACTION_checkPackageStartable);
                        _result19.writeToParcel(reply, (int) TRANSACTION_checkPackageStartable);
                    } else {
                        reply.writeInt(0);
                    }
                    reply.writeTypedList(_arg04);
                    return true;
                case TRANSACTION_setHomeActivity /*85*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        componentName = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                    } else {
                        componentName = null;
                    }
                    setHomeActivity(componentName, data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setComponentEnabledSetting /*86*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        componentName = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                    } else {
                        componentName = null;
                    }
                    setComponentEnabledSetting(componentName, data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getComponentEnabledSetting /*87*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        componentName = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                    } else {
                        componentName = null;
                    }
                    _result2 = getComponentEnabledSetting(componentName, data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_setApplicationEnabledSetting /*88*/:
                    data.enforceInterface(DESCRIPTOR);
                    setApplicationEnabledSetting(data.readString(), data.readInt(), data.readInt(), data.readInt(), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getApplicationEnabledSetting /*89*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getApplicationEnabledSetting(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_logAppProcessStartIfNeeded /*90*/:
                    data.enforceInterface(DESCRIPTOR);
                    logAppProcessStartIfNeeded(data.readString(), data.readInt(), data.readString(), data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_flushPackageRestrictionsAsUser /*91*/:
                    data.enforceInterface(DESCRIPTOR);
                    flushPackageRestrictionsAsUser(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setPackageStoppedState /*92*/:
                    data.enforceInterface(DESCRIPTOR);
                    setPackageStoppedState(data.readString(), data.readInt() != 0, data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_freeStorageAndNotify /*93*/:
                    data.enforceInterface(DESCRIPTOR);
                    freeStorageAndNotify(data.readString(), data.readLong(), android.content.pm.IPackageDataObserver.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_freeStorage /*94*/:
                    IntentSender intentSender;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    long _arg16 = data.readLong();
                    if (data.readInt() != 0) {
                        intentSender = (IntentSender) IntentSender.CREATOR.createFromParcel(data);
                    } else {
                        intentSender = null;
                    }
                    freeStorage(_arg0, _arg16, intentSender);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_deleteApplicationCacheFiles /*95*/:
                    data.enforceInterface(DESCRIPTOR);
                    deleteApplicationCacheFiles(data.readString(), android.content.pm.IPackageDataObserver.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_deleteApplicationCacheFilesAsUser /*96*/:
                    data.enforceInterface(DESCRIPTOR);
                    deleteApplicationCacheFilesAsUser(data.readString(), data.readInt(), android.content.pm.IPackageDataObserver.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_clearApplicationUserData /*97*/:
                    data.enforceInterface(DESCRIPTOR);
                    clearApplicationUserData(data.readString(), android.content.pm.IPackageDataObserver.Stub.asInterface(data.readStrongBinder()), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_clearApplicationProfileData /*98*/:
                    data.enforceInterface(DESCRIPTOR);
                    clearApplicationProfileData(data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getPackageSizeInfo /*99*/:
                    data.enforceInterface(DESCRIPTOR);
                    getPackageSizeInfo(data.readString(), data.readInt(), android.content.pm.IPackageStatsObserver.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getSystemSharedLibraryNames /*100*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getSystemSharedLibraryNames();
                    reply.writeNoException();
                    reply.writeStringArray(_result3);
                    return true;
                case TRANSACTION_getSystemAvailableFeatures /*101*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getSystemAvailableFeatures();
                    reply.writeNoException();
                    if (_result4 != null) {
                        reply.writeInt(TRANSACTION_checkPackageStartable);
                        _result4.writeToParcel(reply, TRANSACTION_checkPackageStartable);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_hasSystemFeature /*102*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = hasSystemFeature(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_checkPackageStartable : 0);
                    return true;
                case TRANSACTION_enterSafeMode /*103*/:
                    data.enforceInterface(DESCRIPTOR);
                    enterSafeMode();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_isSafeMode /*104*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isSafeMode();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_checkPackageStartable : 0);
                    return true;
                case TRANSACTION_systemReady /*105*/:
                    data.enforceInterface(DESCRIPTOR);
                    systemReady();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_hasSystemUidErrors /*106*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = hasSystemUidErrors();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_checkPackageStartable : 0);
                    return true;
                case TRANSACTION_performFstrimIfNeeded /*107*/:
                    data.enforceInterface(DESCRIPTOR);
                    performFstrimIfNeeded();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_updatePackagesIfNeeded /*108*/:
                    data.enforceInterface(DESCRIPTOR);
                    updatePackagesIfNeeded();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_notifyPackageUse /*109*/:
                    data.enforceInterface(DESCRIPTOR);
                    notifyPackageUse(data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_performDexOptIfNeeded /*110*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = performDexOptIfNeeded(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_checkPackageStartable : 0);
                    return true;
                case TRANSACTION_performDexOpt /*111*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = performDexOpt(data.readString(), data.readInt() != 0, data.readInt(), data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_checkPackageStartable : 0);
                    return true;
                case TRANSACTION_performDexOptMode /*112*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = performDexOptMode(data.readString(), data.readInt() != 0, data.readString(), data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_checkPackageStartable : 0);
                    return true;
                case TRANSACTION_dumpProfiles /*113*/:
                    data.enforceInterface(DESCRIPTOR);
                    dumpProfiles(data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_forceDexOpt /*114*/:
                    data.enforceInterface(DESCRIPTOR);
                    forceDexOpt(data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_updateExternalMediaStatus /*115*/:
                    data.enforceInterface(DESCRIPTOR);
                    updateExternalMediaStatus(data.readInt() != 0, data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_nextPackageToClean /*116*/:
                    PackageCleanItem packageCleanItem;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        packageCleanItem = (PackageCleanItem) PackageCleanItem.CREATOR.createFromParcel(data);
                    } else {
                        packageCleanItem = null;
                    }
                    PackageCleanItem _result20 = nextPackageToClean(packageCleanItem);
                    reply.writeNoException();
                    if (_result20 != null) {
                        reply.writeInt(TRANSACTION_checkPackageStartable);
                        _result20.writeToParcel(reply, TRANSACTION_checkPackageStartable);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getMoveStatus /*117*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getMoveStatus(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_registerMoveCallback /*118*/:
                    data.enforceInterface(DESCRIPTOR);
                    registerMoveCallback(android.content.pm.IPackageMoveObserver.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_unregisterMoveCallback /*119*/:
                    data.enforceInterface(DESCRIPTOR);
                    unregisterMoveCallback(android.content.pm.IPackageMoveObserver.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_movePackage /*120*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = movePackage(data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_movePrimaryStorage /*121*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = movePrimaryStorage(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_addPermissionAsync /*122*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        permissionInfo = (PermissionInfo) PermissionInfo.CREATOR.createFromParcel(data);
                    } else {
                        permissionInfo = null;
                    }
                    _result = addPermissionAsync(permissionInfo);
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_checkPackageStartable : 0);
                    return true;
                case TRANSACTION_setInstallLocation /*123*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = setInstallLocation(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_checkPackageStartable : 0);
                    return true;
                case TRANSACTION_getInstallLocation /*124*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getInstallLocation();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_installExistingPackageAsUser /*125*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = installExistingPackageAsUser(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_verifyPendingInstall /*126*/:
                    data.enforceInterface(DESCRIPTOR);
                    verifyPendingInstall(data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_extendVerificationTimeout /*127*/:
                    data.enforceInterface(DESCRIPTOR);
                    extendVerificationTimeout(data.readInt(), data.readInt(), data.readLong());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_verifyIntentFilter /*128*/:
                    data.enforceInterface(DESCRIPTOR);
                    verifyIntentFilter(data.readInt(), data.readInt(), data.createStringArrayList());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getIntentVerificationStatus /*129*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getIntentVerificationStatus(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_updateIntentVerificationStatus /*130*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = updateIntentVerificationStatus(data.readString(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_checkPackageStartable : 0);
                    return true;
                case TRANSACTION_getIntentFilterVerifications /*131*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getIntentFilterVerifications(data.readString());
                    reply.writeNoException();
                    if (_result4 != null) {
                        reply.writeInt(TRANSACTION_checkPackageStartable);
                        _result4.writeToParcel(reply, TRANSACTION_checkPackageStartable);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getAllIntentFilters /*132*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getAllIntentFilters(data.readString());
                    reply.writeNoException();
                    if (_result4 != null) {
                        reply.writeInt(TRANSACTION_checkPackageStartable);
                        _result4.writeToParcel(reply, TRANSACTION_checkPackageStartable);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_setDefaultBrowserPackageName /*133*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = setDefaultBrowserPackageName(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_checkPackageStartable : 0);
                    return true;
                case TRANSACTION_getDefaultBrowserPackageName /*134*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result8 = getDefaultBrowserPackageName(data.readInt());
                    reply.writeNoException();
                    reply.writeString(_result8);
                    return true;
                case TRANSACTION_getVerifierDeviceIdentity /*135*/:
                    data.enforceInterface(DESCRIPTOR);
                    VerifierDeviceIdentity _result21 = getVerifierDeviceIdentity();
                    reply.writeNoException();
                    if (_result21 != null) {
                        reply.writeInt(TRANSACTION_checkPackageStartable);
                        _result21.writeToParcel(reply, TRANSACTION_checkPackageStartable);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_isFirstBoot /*136*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isFirstBoot();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_checkPackageStartable : 0);
                    return true;
                case TRANSACTION_isOnlyCoreApps /*137*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isOnlyCoreApps();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_checkPackageStartable : 0);
                    return true;
                case TRANSACTION_isUpgrade /*138*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isUpgrade();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_checkPackageStartable : 0);
                    return true;
                case TRANSACTION_setPermissionEnforced /*139*/:
                    data.enforceInterface(DESCRIPTOR);
                    setPermissionEnforced(data.readString(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_isPermissionEnforced /*140*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isPermissionEnforced(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_checkPackageStartable : 0);
                    return true;
                case TRANSACTION_isStorageLow /*141*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isStorageLow();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_checkPackageStartable : 0);
                    return true;
                case TRANSACTION_setApplicationHiddenSettingAsUser /*142*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = setApplicationHiddenSettingAsUser(data.readString(), data.readInt() != 0, data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_checkPackageStartable : 0);
                    return true;
                case TRANSACTION_getApplicationHiddenSettingAsUser /*143*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getApplicationHiddenSettingAsUser(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_checkPackageStartable : 0);
                    return true;
                case TRANSACTION_getPackageInstaller /*144*/:
                    data.enforceInterface(DESCRIPTOR);
                    IPackageInstaller _result22 = getPackageInstaller();
                    reply.writeNoException();
                    reply.writeStrongBinder(_result22 != null ? _result22.asBinder() : null);
                    return true;
                case TRANSACTION_setBlockUninstallForUser /*145*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = setBlockUninstallForUser(data.readString(), data.readInt() != 0, data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_checkPackageStartable : 0);
                    return true;
                case TRANSACTION_getBlockUninstallForUser /*146*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getBlockUninstallForUser(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_checkPackageStartable : 0);
                    return true;
                case TRANSACTION_getKeySetByAlias /*147*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result11 = getKeySetByAlias(data.readString(), data.readString());
                    reply.writeNoException();
                    if (_result11 != null) {
                        reply.writeInt(TRANSACTION_checkPackageStartable);
                        _result11.writeToParcel(reply, TRANSACTION_checkPackageStartable);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getSigningKeySet /*148*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result11 = getSigningKeySet(data.readString());
                    reply.writeNoException();
                    if (_result11 != null) {
                        reply.writeInt(TRANSACTION_checkPackageStartable);
                        _result11.writeToParcel(reply, TRANSACTION_checkPackageStartable);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_isPackageSignedByKeySet /*149*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        keySet = (KeySet) KeySet.CREATOR.createFromParcel(data);
                    } else {
                        keySet = null;
                    }
                    _result = isPackageSignedByKeySet(_arg0, keySet);
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_checkPackageStartable : 0);
                    return true;
                case TRANSACTION_isPackageSignedByKeySetExactly /*150*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        keySet = (KeySet) KeySet.CREATOR.createFromParcel(data);
                    } else {
                        keySet = null;
                    }
                    _result = isPackageSignedByKeySetExactly(_arg0, keySet);
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_checkPackageStartable : 0);
                    return true;
                case TRANSACTION_addOnPermissionsChangeListener /*151*/:
                    data.enforceInterface(DESCRIPTOR);
                    addOnPermissionsChangeListener(android.content.pm.IOnPermissionsChangeListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_removeOnPermissionsChangeListener /*152*/:
                    data.enforceInterface(DESCRIPTOR);
                    removeOnPermissionsChangeListener(android.content.pm.IOnPermissionsChangeListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_grantDefaultPermissionsToEnabledCarrierApps /*153*/:
                    data.enforceInterface(DESCRIPTOR);
                    grantDefaultPermissionsToEnabledCarrierApps(data.createStringArray(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_isPermissionRevokedByPolicy /*154*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isPermissionRevokedByPolicy(data.readString(), data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_checkPackageStartable : 0);
                    return true;
                case TRANSACTION_getPermissionControllerPackageName /*155*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result8 = getPermissionControllerPackageName();
                    reply.writeNoException();
                    reply.writeString(_result8);
                    return true;
                case TRANSACTION_getEphemeralApplications /*156*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getEphemeralApplications(data.readInt());
                    reply.writeNoException();
                    if (_result4 != null) {
                        reply.writeInt(TRANSACTION_checkPackageStartable);
                        _result4.writeToParcel(reply, TRANSACTION_checkPackageStartable);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getEphemeralApplicationCookie /*157*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result10 = getEphemeralApplicationCookie(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeByteArray(_result10);
                    return true;
                case TRANSACTION_setEphemeralApplicationCookie /*158*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = setEphemeralApplicationCookie(data.readString(), data.createByteArray(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_checkPackageStartable : 0);
                    return true;
                case TRANSACTION_getEphemeralApplicationIcon /*159*/:
                    data.enforceInterface(DESCRIPTOR);
                    Bitmap _result23 = getEphemeralApplicationIcon(data.readString(), data.readInt());
                    reply.writeNoException();
                    if (_result23 != null) {
                        reply.writeInt(TRANSACTION_checkPackageStartable);
                        _result23.writeToParcel(reply, TRANSACTION_checkPackageStartable);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_isEphemeralApplication /*160*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isEphemeralApplication(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_checkPackageStartable : 0);
                    return true;
                case TRANSACTION_setRequiredForSystemUser /*161*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = setRequiredForSystemUser(data.readString(), data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_checkPackageStartable : 0);
                    return true;
                case TRANSACTION_getServicesSystemSharedLibraryPackageName /*162*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result8 = getServicesSystemSharedLibraryPackageName();
                    reply.writeNoException();
                    reply.writeString(_result8);
                    return true;
                case TRANSACTION_getSharedSystemSharedLibraryPackageName /*163*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result8 = getSharedSystemSharedLibraryPackageName();
                    reply.writeNoException();
                    reply.writeString(_result8);
                    return true;
                case TRANSACTION_isPackageDeviceAdminOnAnyUser /*164*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isPackageDeviceAdminOnAnyUser(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_checkPackageStartable : 0);
                    return true;
                case TRANSACTION_getPreviousCodePaths /*165*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result7 = getPreviousCodePaths(data.readString());
                    reply.writeNoException();
                    reply.writeStringList(_result7);
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
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

    void deletePackage(String str, IPackageDeleteObserver2 iPackageDeleteObserver2, int i, int i2) throws RemoteException;

    void deletePackageAsUser(String str, IPackageDeleteObserver iPackageDeleteObserver, int i, int i2) throws RemoteException;

    void dumpProfiles(String str) throws RemoteException;

    void enterSafeMode() throws RemoteException;

    void extendVerificationTimeout(int i, int i2, long j) throws RemoteException;

    void finishPackageInstall(int i, boolean z) throws RemoteException;

    void flushPackageRestrictionsAsUser(int i) throws RemoteException;

    void forceDexOpt(String str) throws RemoteException;

    void freeStorage(String str, long j, IntentSender intentSender) throws RemoteException;

    void freeStorageAndNotify(String str, long j, IPackageDataObserver iPackageDataObserver) throws RemoteException;

    ActivityInfo getActivityInfo(ComponentName componentName, int i, int i2) throws RemoteException;

    ParceledListSlice getAllIntentFilters(String str) throws RemoteException;

    List<String> getAllPackages() throws RemoteException;

    ParceledListSlice getAllPermissionGroups(int i) throws RemoteException;

    String[] getAppOpPermissionPackages(String str) throws RemoteException;

    int getApplicationEnabledSetting(String str, int i) throws RemoteException;

    boolean getApplicationHiddenSettingAsUser(String str, int i) throws RemoteException;

    ApplicationInfo getApplicationInfo(String str, int i, int i2) throws RemoteException;

    boolean getBlockUninstallForUser(String str, int i) throws RemoteException;

    int getComponentEnabledSetting(ComponentName componentName, int i) throws RemoteException;

    byte[] getDefaultAppsBackup(int i) throws RemoteException;

    String getDefaultBrowserPackageName(int i) throws RemoteException;

    byte[] getEphemeralApplicationCookie(String str, int i) throws RemoteException;

    Bitmap getEphemeralApplicationIcon(String str, int i) throws RemoteException;

    ParceledListSlice getEphemeralApplications(int i) throws RemoteException;

    int getFlagsForUid(int i) throws RemoteException;

    ComponentName getHomeActivities(List<ResolveInfo> list) throws RemoteException;

    int getInstallLocation() throws RemoteException;

    ParceledListSlice getInstalledApplications(int i, int i2) throws RemoteException;

    ParceledListSlice getInstalledPackages(int i, int i2) throws RemoteException;

    String getInstallerPackageName(String str) throws RemoteException;

    InstrumentationInfo getInstrumentationInfo(ComponentName componentName, int i) throws RemoteException;

    byte[] getIntentFilterVerificationBackup(int i) throws RemoteException;

    ParceledListSlice getIntentFilterVerifications(String str) throws RemoteException;

    int getIntentVerificationStatus(String str, int i) throws RemoteException;

    KeySet getKeySetByAlias(String str, String str2) throws RemoteException;

    ResolveInfo getLastChosenActivity(Intent intent, String str, int i) throws RemoteException;

    int getMoveStatus(int i) throws RemoteException;

    String getNameForUid(int i) throws RemoteException;

    int[] getPackageGids(String str, int i, int i2) throws RemoteException;

    PackageInfo getPackageInfo(String str, int i, int i2) throws RemoteException;

    IPackageInstaller getPackageInstaller() throws RemoteException;

    void getPackageSizeInfo(String str, int i, IPackageStatsObserver iPackageStatsObserver) throws RemoteException;

    int getPackageUid(String str, int i, int i2) throws RemoteException;

    String[] getPackagesForUid(int i) throws RemoteException;

    ParceledListSlice getPackagesHoldingPermissions(String[] strArr, int i, int i2) throws RemoteException;

    String getPermissionControllerPackageName() throws RemoteException;

    int getPermissionFlags(String str, String str2, int i) throws RemoteException;

    byte[] getPermissionGrantBackup(int i) throws RemoteException;

    PermissionGroupInfo getPermissionGroupInfo(String str, int i) throws RemoteException;

    PermissionInfo getPermissionInfo(String str, int i) throws RemoteException;

    ParceledListSlice getPersistentApplications(int i) throws RemoteException;

    int getPreferredActivities(List<IntentFilter> list, List<ComponentName> list2, String str) throws RemoteException;

    byte[] getPreferredActivityBackup(int i) throws RemoteException;

    List<String> getPreviousCodePaths(String str) throws RemoteException;

    int getPrivateFlagsForUid(int i) throws RemoteException;

    ProviderInfo getProviderInfo(ComponentName componentName, int i, int i2) throws RemoteException;

    ActivityInfo getReceiverInfo(ComponentName componentName, int i, int i2) throws RemoteException;

    ServiceInfo getServiceInfo(ComponentName componentName, int i, int i2) throws RemoteException;

    String getServicesSystemSharedLibraryPackageName() throws RemoteException;

    String getSharedSystemSharedLibraryPackageName() throws RemoteException;

    KeySet getSigningKeySet(String str) throws RemoteException;

    ParceledListSlice getSystemAvailableFeatures() throws RemoteException;

    String[] getSystemSharedLibraryNames() throws RemoteException;

    int getUidForSharedUser(String str) throws RemoteException;

    VerifierDeviceIdentity getVerifierDeviceIdentity() throws RemoteException;

    void grantDefaultPermissionsToEnabledCarrierApps(String[] strArr, int i) throws RemoteException;

    void grantRuntimePermission(String str, String str2, int i) throws RemoteException;

    boolean hasSystemFeature(String str, int i) throws RemoteException;

    boolean hasSystemUidErrors() throws RemoteException;

    int installExistingPackageAsUser(String str, int i) throws RemoteException;

    void installPackageAsUser(String str, IPackageInstallObserver2 iPackageInstallObserver2, int i, String str2, int i2) throws RemoteException;

    boolean isEphemeralApplication(String str, int i) throws RemoteException;

    boolean isFirstBoot() throws RemoteException;

    boolean isOnlyCoreApps() throws RemoteException;

    boolean isPackageAvailable(String str, int i) throws RemoteException;

    boolean isPackageDeviceAdminOnAnyUser(String str) throws RemoteException;

    boolean isPackageSignedByKeySet(String str, KeySet keySet) throws RemoteException;

    boolean isPackageSignedByKeySetExactly(String str, KeySet keySet) throws RemoteException;

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

    void notifyPackageUse(String str, int i) throws RemoteException;

    boolean performDexOpt(String str, boolean z, int i, boolean z2) throws RemoteException;

    boolean performDexOptIfNeeded(String str) throws RemoteException;

    boolean performDexOptMode(String str, boolean z, String str2, boolean z2) throws RemoteException;

    void performFstrimIfNeeded() throws RemoteException;

    ParceledListSlice queryContentProviders(String str, int i, int i2) throws RemoteException;

    ParceledListSlice queryInstrumentation(String str, int i) throws RemoteException;

    ParceledListSlice queryIntentActivities(Intent intent, String str, int i, int i2) throws RemoteException;

    ParceledListSlice queryIntentActivityOptions(ComponentName componentName, Intent[] intentArr, String[] strArr, Intent intent, String str, int i, int i2) throws RemoteException;

    ParceledListSlice queryIntentContentProviders(Intent intent, String str, int i, int i2) throws RemoteException;

    ParceledListSlice queryIntentReceivers(Intent intent, String str, int i, int i2) throws RemoteException;

    ParceledListSlice queryIntentServices(Intent intent, String str, int i, int i2) throws RemoteException;

    ParceledListSlice queryPermissionsByGroup(String str, int i) throws RemoteException;

    void querySyncProviders(List<String> list, List<ProviderInfo> list2) throws RemoteException;

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

    void revokeRuntimePermission(String str, String str2, int i) throws RemoteException;

    void setApplicationEnabledSetting(String str, int i, int i2, int i3, String str2) throws RemoteException;

    boolean setApplicationHiddenSettingAsUser(String str, boolean z, int i) throws RemoteException;

    boolean setBlockUninstallForUser(String str, boolean z, int i) throws RemoteException;

    void setComponentEnabledSetting(ComponentName componentName, int i, int i2, int i3) throws RemoteException;

    boolean setDefaultBrowserPackageName(String str, int i) throws RemoteException;

    boolean setEphemeralApplicationCookie(String str, byte[] bArr, int i) throws RemoteException;

    void setHomeActivity(ComponentName componentName, int i) throws RemoteException;

    boolean setInstallLocation(int i) throws RemoteException;

    void setInstallerPackageName(String str, String str2) throws RemoteException;

    void setLastChosenActivity(Intent intent, String str, int i, IntentFilter intentFilter, int i2, ComponentName componentName) throws RemoteException;

    void setPackageStoppedState(String str, boolean z, int i) throws RemoteException;

    String[] setPackagesSuspendedAsUser(String[] strArr, boolean z, int i) throws RemoteException;

    void setPermissionEnforced(String str, boolean z) throws RemoteException;

    boolean setRequiredForSystemUser(String str, boolean z) throws RemoteException;

    boolean shouldShowRequestPermissionRationale(String str, String str2, int i) throws RemoteException;

    void systemReady() throws RemoteException;

    void unregisterMoveCallback(IPackageMoveObserver iPackageMoveObserver) throws RemoteException;

    void updateExternalMediaStatus(boolean z, boolean z2) throws RemoteException;

    boolean updateIntentVerificationStatus(String str, int i, int i2) throws RemoteException;

    void updatePackagesIfNeeded() throws RemoteException;

    void updatePermissionFlags(String str, String str2, int i, int i2, int i3) throws RemoteException;

    void updatePermissionFlagsForAllApps(int i, int i2, int i3) throws RemoteException;

    void verifyIntentFilter(int i, int i2, List<String> list) throws RemoteException;

    void verifyPendingInstall(int i, int i2) throws RemoteException;
}
