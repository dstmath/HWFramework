package android.app.admin;

import android.app.IApplicationThread;
import android.app.IServiceConnection;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.IPackageDataObserver;
import android.content.pm.ParceledListSlice;
import android.content.pm.StringParceledListSlice;
import android.graphics.Bitmap;
import android.net.ProxyInfo;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.PersistableBundle;
import android.os.RemoteCallback;
import android.os.RemoteException;
import android.os.UserHandle;
import android.security.keymaster.KeymasterCertificateChain;
import android.security.keystore.ParcelableKeyGenParameterSpec;
import android.telephony.data.ApnSetting;
import android.text.TextUtils;
import java.util.List;

public interface IDevicePolicyManager extends IInterface {

    public static abstract class Stub extends Binder implements IDevicePolicyManager {
        private static final String DESCRIPTOR = "android.app.admin.IDevicePolicyManager";
        static final int TRANSACTION_addCrossProfileIntentFilter = 111;
        static final int TRANSACTION_addCrossProfileWidgetProvider = 167;
        static final int TRANSACTION_addOverrideApn = 258;
        static final int TRANSACTION_addPersistentPreferredActivity = 99;
        static final int TRANSACTION_approveCaCert = 85;
        static final int TRANSACTION_bindDeviceAdminServiceAsUser = 234;
        static final int TRANSACTION_checkProvisioningPreCondition = 189;
        static final int TRANSACTION_choosePrivateKeyAlias = 91;
        static final int TRANSACTION_clearApplicationUserData = 246;
        static final int TRANSACTION_clearCrossProfileIntentFilters = 112;
        static final int TRANSACTION_clearDeviceOwner = 69;
        static final int TRANSACTION_clearPackagePersistentPreferredActivities = 100;
        static final int TRANSACTION_clearProfileOwner = 76;
        static final int TRANSACTION_clearResetPasswordToken = 241;
        static final int TRANSACTION_clearSystemUpdatePolicyFreezePeriodRecord = 178;
        static final int TRANSACTION_createAdminSupportIntent = 124;
        static final int TRANSACTION_createAndManageUser = 127;
        static final int TRANSACTION_enableSystemApp = 134;
        static final int TRANSACTION_enableSystemAppWithIntent = 135;
        static final int TRANSACTION_enforceCanManageCaCerts = 84;
        static final int TRANSACTION_forceRemoveActiveAdmin = 55;
        static final int TRANSACTION_forceSecurityLogs = 220;
        static final int TRANSACTION_forceUpdateUserSetupComplete = 226;
        static final int TRANSACTION_generateKeyPair = 89;
        static final int TRANSACTION_getAccountTypesWithManagementDisabled = 138;
        static final int TRANSACTION_getAccountTypesWithManagementDisabledAsUser = 139;
        static final int TRANSACTION_getActiveAdmins = 51;
        static final int TRANSACTION_getAffiliationIds = 214;
        static final int TRANSACTION_getAlwaysOnVpnPackage = 98;
        static final int TRANSACTION_getApplicationRestrictions = 103;
        static final int TRANSACTION_getApplicationRestrictionsManagingPackage = 105;
        static final int TRANSACTION_getAutoTimeRequired = 171;
        static final int TRANSACTION_getBindDeviceAdminTargetUsers = 235;
        static final int TRANSACTION_getBluetoothContactSharingDisabled = 163;
        static final int TRANSACTION_getBluetoothContactSharingDisabledForUser = 164;
        static final int TRANSACTION_getCameraDisabled = 44;
        static final int TRANSACTION_getCertInstallerPackage = 96;
        static final int TRANSACTION_getCrossProfileCallerIdDisabled = 156;
        static final int TRANSACTION_getCrossProfileCallerIdDisabledForUser = 157;
        static final int TRANSACTION_getCrossProfileContactsSearchDisabled = 159;
        static final int TRANSACTION_getCrossProfileContactsSearchDisabledForUser = 160;
        static final int TRANSACTION_getCrossProfileWidgetProviders = 169;
        static final int TRANSACTION_getCurrentFailedPasswordAttempts = 25;
        static final int TRANSACTION_getDelegatePackages = 94;
        static final int TRANSACTION_getDelegatedScopes = 93;
        static final int TRANSACTION_getDeviceOwnerComponent = 66;
        static final int TRANSACTION_getDeviceOwnerLockScreenInfo = 79;
        static final int TRANSACTION_getDeviceOwnerName = 68;
        static final int TRANSACTION_getDeviceOwnerOrganizationName = 209;
        static final int TRANSACTION_getDeviceOwnerUserId = 70;
        static final int TRANSACTION_getDisallowedSystemApps = 249;
        static final int TRANSACTION_getDoNotAskCredentialsOnBoot = 181;
        static final int TRANSACTION_getEndUserSessionMessage = 255;
        static final int TRANSACTION_getForceEphemeralUsers = 173;
        static final int TRANSACTION_getGlobalProxyAdmin = 37;
        static final int TRANSACTION_getKeepUninstalledPackages = 191;
        static final int TRANSACTION_getKeyguardDisabledFeatures = 48;
        static final int TRANSACTION_getLastBugReportRequestTime = 238;
        static final int TRANSACTION_getLastNetworkLogRetrievalTime = 239;
        static final int TRANSACTION_getLastSecurityLogRetrievalTime = 237;
        static final int TRANSACTION_getLockTaskFeatures = 144;
        static final int TRANSACTION_getLockTaskPackages = 141;
        static final int TRANSACTION_getLongSupportMessage = 199;
        static final int TRANSACTION_getLongSupportMessageForUser = 201;
        static final int TRANSACTION_getMandatoryBackupTransport = 230;
        static final int TRANSACTION_getMaximumFailedPasswordsForWipe = 28;
        static final int TRANSACTION_getMaximumTimeToLock = 31;
        static final int TRANSACTION_getMeteredDataDisabledPackages = 257;
        static final int TRANSACTION_getOrganizationColor = 205;
        static final int TRANSACTION_getOrganizationColorForUser = 206;
        static final int TRANSACTION_getOrganizationName = 208;
        static final int TRANSACTION_getOrganizationNameForUser = 210;
        static final int TRANSACTION_getOverrideApns = 261;
        static final int TRANSACTION_getOwnerInstalledCaCerts = 245;
        static final int TRANSACTION_getPasswordExpiration = 21;
        static final int TRANSACTION_getPasswordExpirationTimeout = 20;
        static final int TRANSACTION_getPasswordHistoryLength = 18;
        static final int TRANSACTION_getPasswordMinimumLength = 4;
        static final int TRANSACTION_getPasswordMinimumLetters = 10;
        static final int TRANSACTION_getPasswordMinimumLowerCase = 8;
        static final int TRANSACTION_getPasswordMinimumNonLetter = 16;
        static final int TRANSACTION_getPasswordMinimumNumeric = 12;
        static final int TRANSACTION_getPasswordMinimumSymbols = 14;
        static final int TRANSACTION_getPasswordMinimumUpperCase = 6;
        static final int TRANSACTION_getPasswordQuality = 2;
        static final int TRANSACTION_getPendingSystemUpdate = 183;
        static final int TRANSACTION_getPermissionGrantState = 187;
        static final int TRANSACTION_getPermissionPolicy = 185;
        static final int TRANSACTION_getPermittedAccessibilityServices = 114;
        static final int TRANSACTION_getPermittedAccessibilityServicesForUser = 115;
        static final int TRANSACTION_getPermittedCrossProfileNotificationListeners = 122;
        static final int TRANSACTION_getPermittedInputMethods = 118;
        static final int TRANSACTION_getPermittedInputMethodsForCurrentUser = 119;
        static final int TRANSACTION_getProfileOwner = 72;
        static final int TRANSACTION_getProfileOwnerName = 73;
        static final int TRANSACTION_getProfileWithMinimumFailedPasswordsForWipe = 26;
        static final int TRANSACTION_getRemoveWarning = 53;
        static final int TRANSACTION_getRequiredStrongAuthTimeout = 33;
        static final int TRANSACTION_getRestrictionsProvider = 108;
        static final int TRANSACTION_getScreenCaptureDisabled = 46;
        static final int TRANSACTION_getSecondaryUsers = 133;
        static final int TRANSACTION_getShortSupportMessage = 197;
        static final int TRANSACTION_getShortSupportMessageForUser = 200;
        static final int TRANSACTION_getStartUserSessionMessage = 254;
        static final int TRANSACTION_getStorageEncryption = 40;
        static final int TRANSACTION_getStorageEncryptionStatus = 41;
        static final int TRANSACTION_getSystemUpdatePolicy = 177;
        static final int TRANSACTION_getTransferOwnershipBundle = 251;
        static final int TRANSACTION_getTrustAgentConfiguration = 166;
        static final int TRANSACTION_getUserProvisioningState = 211;
        static final int TRANSACTION_getUserRestrictions = 110;
        static final int TRANSACTION_getWifiMacAddress = 194;
        static final int TRANSACTION_hasDeviceOwner = 67;
        static final int TRANSACTION_hasGrantedPolicy = 56;
        static final int TRANSACTION_hasUserSetupCompleted = 77;
        static final int TRANSACTION_installCaCert = 82;
        static final int TRANSACTION_installExistingPackage = 136;
        static final int TRANSACTION_installKeyPair = 87;
        static final int TRANSACTION_isAccessibilityServicePermittedByAdmin = 116;
        static final int TRANSACTION_isActivePasswordSufficient = 22;
        static final int TRANSACTION_isAdminActive = 50;
        static final int TRANSACTION_isAffiliatedUser = 215;
        static final int TRANSACTION_isApplicationHidden = 126;
        static final int TRANSACTION_isBackupServiceEnabled = 228;
        static final int TRANSACTION_isCaCertApproved = 86;
        static final int TRANSACTION_isCallerApplicationRestrictionsManagingPackage = 106;
        static final int TRANSACTION_isCurrentInputMethodSetByOwner = 244;
        static final int TRANSACTION_isDeviceProvisioned = 223;
        static final int TRANSACTION_isDeviceProvisioningConfigApplied = 224;
        static final int TRANSACTION_isEphemeralUser = 236;
        static final int TRANSACTION_isInputMethodPermittedByAdmin = 120;
        static final int TRANSACTION_isLockTaskPermitted = 142;
        static final int TRANSACTION_isLogoutEnabled = 248;
        static final int TRANSACTION_isManagedProfile = 192;
        static final int TRANSACTION_isMasterVolumeMuted = 151;
        static final int TRANSACTION_isMeteredDataDisabledPackageForUser = 264;
        static final int TRANSACTION_isNetworkLoggingEnabled = 232;
        static final int TRANSACTION_isNotificationListenerServicePermitted = 123;
        static final int TRANSACTION_isOverrideApnEnabled = 263;
        static final int TRANSACTION_isPackageSuspended = 81;
        static final int TRANSACTION_isProfileActivePasswordSufficientForParent = 23;
        static final int TRANSACTION_isProvisioningAllowed = 188;
        static final int TRANSACTION_isRemovingAdmin = 174;
        static final int TRANSACTION_isResetPasswordTokenActive = 242;
        static final int TRANSACTION_isSecurityLoggingEnabled = 217;
        static final int TRANSACTION_isSeparateProfileChallengeAllowed = 202;
        static final int TRANSACTION_isSystemOnlyUser = 193;
        static final int TRANSACTION_isUninstallBlocked = 154;
        static final int TRANSACTION_isUninstallInQueue = 221;
        static final int TRANSACTION_isUsingUnifiedPassword = 24;
        static final int TRANSACTION_lockNow = 34;
        static final int TRANSACTION_logoutUser = 132;
        static final int TRANSACTION_notifyLockTaskModeChanged = 152;
        static final int TRANSACTION_notifyPendingSystemUpdate = 182;
        static final int TRANSACTION_packageHasActiveAdmins = 52;
        static final int TRANSACTION_reboot = 195;
        static final int TRANSACTION_removeActiveAdmin = 54;
        static final int TRANSACTION_removeCrossProfileWidgetProvider = 168;
        static final int TRANSACTION_removeKeyPair = 88;
        static final int TRANSACTION_removeOverrideApn = 260;
        static final int TRANSACTION_removeUser = 128;
        static final int TRANSACTION_reportFailedFingerprintAttempt = 61;
        static final int TRANSACTION_reportFailedPasswordAttempt = 59;
        static final int TRANSACTION_reportKeyguardDismissed = 63;
        static final int TRANSACTION_reportKeyguardSecured = 64;
        static final int TRANSACTION_reportPasswordChanged = 58;
        static final int TRANSACTION_reportSuccessfulFingerprintAttempt = 62;
        static final int TRANSACTION_reportSuccessfulPasswordAttempt = 60;
        static final int TRANSACTION_requestBugreport = 42;
        static final int TRANSACTION_resetPassword = 29;
        static final int TRANSACTION_resetPasswordWithToken = 243;
        static final int TRANSACTION_retrieveNetworkLogs = 233;
        static final int TRANSACTION_retrievePreRebootSecurityLogs = 219;
        static final int TRANSACTION_retrieveSecurityLogs = 218;
        static final int TRANSACTION_setAccountManagementDisabled = 137;
        static final int TRANSACTION_setActiveAdmin = 49;
        static final int TRANSACTION_setActivePasswordState = 57;
        static final int TRANSACTION_setAffiliationIds = 213;
        static final int TRANSACTION_setAlwaysOnVpnPackage = 97;
        static final int TRANSACTION_setApplicationHidden = 125;
        static final int TRANSACTION_setApplicationRestrictions = 102;
        static final int TRANSACTION_setApplicationRestrictionsManagingPackage = 104;
        static final int TRANSACTION_setAutoTimeRequired = 170;
        static final int TRANSACTION_setBackupServiceEnabled = 227;
        static final int TRANSACTION_setBluetoothContactSharingDisabled = 162;
        static final int TRANSACTION_setCameraDisabled = 43;
        static final int TRANSACTION_setCertInstallerPackage = 95;
        static final int TRANSACTION_setCrossProfileCallerIdDisabled = 155;
        static final int TRANSACTION_setCrossProfileContactsSearchDisabled = 158;
        static final int TRANSACTION_setDefaultSmsApplication = 101;
        static final int TRANSACTION_setDelegatedScopes = 92;
        static final int TRANSACTION_setDeviceOwner = 65;
        static final int TRANSACTION_setDeviceOwnerLockScreenInfo = 78;
        static final int TRANSACTION_setDeviceProvisioningConfigApplied = 225;
        static final int TRANSACTION_setEndUserSessionMessage = 253;
        static final int TRANSACTION_setForceEphemeralUsers = 172;
        static final int TRANSACTION_setGlobalProxy = 36;
        static final int TRANSACTION_setGlobalSetting = 145;
        static final int TRANSACTION_setKeepUninstalledPackages = 190;
        static final int TRANSACTION_setKeyPairCertificate = 90;
        static final int TRANSACTION_setKeyguardDisabled = 179;
        static final int TRANSACTION_setKeyguardDisabledFeatures = 47;
        static final int TRANSACTION_setLockTaskFeatures = 143;
        static final int TRANSACTION_setLockTaskPackages = 140;
        static final int TRANSACTION_setLogoutEnabled = 247;
        static final int TRANSACTION_setLongSupportMessage = 198;
        static final int TRANSACTION_setMandatoryBackupTransport = 229;
        static final int TRANSACTION_setMasterVolumeMuted = 150;
        static final int TRANSACTION_setMaximumFailedPasswordsForWipe = 27;
        static final int TRANSACTION_setMaximumTimeToLock = 30;
        static final int TRANSACTION_setMeteredDataDisabledPackages = 256;
        static final int TRANSACTION_setNetworkLoggingEnabled = 231;
        static final int TRANSACTION_setOrganizationColor = 203;
        static final int TRANSACTION_setOrganizationColorForUser = 204;
        static final int TRANSACTION_setOrganizationName = 207;
        static final int TRANSACTION_setOverrideApnsEnabled = 262;
        static final int TRANSACTION_setPackagesSuspended = 80;
        static final int TRANSACTION_setPasswordExpirationTimeout = 19;
        static final int TRANSACTION_setPasswordHistoryLength = 17;
        static final int TRANSACTION_setPasswordMinimumLength = 3;
        static final int TRANSACTION_setPasswordMinimumLetters = 9;
        static final int TRANSACTION_setPasswordMinimumLowerCase = 7;
        static final int TRANSACTION_setPasswordMinimumNonLetter = 15;
        static final int TRANSACTION_setPasswordMinimumNumeric = 11;
        static final int TRANSACTION_setPasswordMinimumSymbols = 13;
        static final int TRANSACTION_setPasswordMinimumUpperCase = 5;
        static final int TRANSACTION_setPasswordQuality = 1;
        static final int TRANSACTION_setPermissionGrantState = 186;
        static final int TRANSACTION_setPermissionPolicy = 184;
        static final int TRANSACTION_setPermittedAccessibilityServices = 113;
        static final int TRANSACTION_setPermittedCrossProfileNotificationListeners = 121;
        static final int TRANSACTION_setPermittedInputMethods = 117;
        static final int TRANSACTION_setProfileEnabled = 74;
        static final int TRANSACTION_setProfileName = 75;
        static final int TRANSACTION_setProfileOwner = 71;
        static final int TRANSACTION_setRecommendedGlobalProxy = 38;
        static final int TRANSACTION_setRequiredStrongAuthTimeout = 32;
        static final int TRANSACTION_setResetPasswordToken = 240;
        static final int TRANSACTION_setRestrictionsProvider = 107;
        static final int TRANSACTION_setScreenCaptureDisabled = 45;
        static final int TRANSACTION_setSecureSetting = 147;
        static final int TRANSACTION_setSecurityLoggingEnabled = 216;
        static final int TRANSACTION_setShortSupportMessage = 196;
        static final int TRANSACTION_setStartUserSessionMessage = 252;
        static final int TRANSACTION_setStatusBarDisabled = 180;
        static final int TRANSACTION_setStorageEncryption = 39;
        static final int TRANSACTION_setSystemSetting = 146;
        static final int TRANSACTION_setSystemUpdatePolicy = 176;
        static final int TRANSACTION_setTime = 148;
        static final int TRANSACTION_setTimeZone = 149;
        static final int TRANSACTION_setTrustAgentConfiguration = 165;
        static final int TRANSACTION_setUninstallBlocked = 153;
        static final int TRANSACTION_setUserIcon = 175;
        static final int TRANSACTION_setUserProvisioningState = 212;
        static final int TRANSACTION_setUserRestriction = 109;
        static final int TRANSACTION_startManagedQuickContact = 161;
        static final int TRANSACTION_startUserInBackground = 130;
        static final int TRANSACTION_stopUser = 131;
        static final int TRANSACTION_switchUser = 129;
        static final int TRANSACTION_transferOwnership = 250;
        static final int TRANSACTION_uninstallCaCerts = 83;
        static final int TRANSACTION_uninstallPackageWithActiveAdmins = 222;
        static final int TRANSACTION_updateOverrideApn = 259;
        static final int TRANSACTION_wipeDataWithReason = 35;

        private static class Proxy implements IDevicePolicyManager {
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

            public void setPasswordQuality(ComponentName who, int quality, boolean parent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(quality);
                    _data.writeInt(parent);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getPasswordQuality(ComponentName who, int userHandle, boolean parent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userHandle);
                    _data.writeInt(parent);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setPasswordMinimumLength(ComponentName who, int length, boolean parent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(length);
                    _data.writeInt(parent);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getPasswordMinimumLength(ComponentName who, int userHandle, boolean parent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userHandle);
                    _data.writeInt(parent);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setPasswordMinimumUpperCase(ComponentName who, int length, boolean parent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(length);
                    _data.writeInt(parent);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getPasswordMinimumUpperCase(ComponentName who, int userHandle, boolean parent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userHandle);
                    _data.writeInt(parent);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setPasswordMinimumLowerCase(ComponentName who, int length, boolean parent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(length);
                    _data.writeInt(parent);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getPasswordMinimumLowerCase(ComponentName who, int userHandle, boolean parent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userHandle);
                    _data.writeInt(parent);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setPasswordMinimumLetters(ComponentName who, int length, boolean parent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(length);
                    _data.writeInt(parent);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getPasswordMinimumLetters(ComponentName who, int userHandle, boolean parent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userHandle);
                    _data.writeInt(parent);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setPasswordMinimumNumeric(ComponentName who, int length, boolean parent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(length);
                    _data.writeInt(parent);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getPasswordMinimumNumeric(ComponentName who, int userHandle, boolean parent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userHandle);
                    _data.writeInt(parent);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setPasswordMinimumSymbols(ComponentName who, int length, boolean parent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(length);
                    _data.writeInt(parent);
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getPasswordMinimumSymbols(ComponentName who, int userHandle, boolean parent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userHandle);
                    _data.writeInt(parent);
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setPasswordMinimumNonLetter(ComponentName who, int length, boolean parent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(length);
                    _data.writeInt(parent);
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getPasswordMinimumNonLetter(ComponentName who, int userHandle, boolean parent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userHandle);
                    _data.writeInt(parent);
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setPasswordHistoryLength(ComponentName who, int length, boolean parent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(length);
                    _data.writeInt(parent);
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getPasswordHistoryLength(ComponentName who, int userHandle, boolean parent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userHandle);
                    _data.writeInt(parent);
                    this.mRemote.transact(18, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setPasswordExpirationTimeout(ComponentName who, long expiration, boolean parent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeLong(expiration);
                    _data.writeInt(parent);
                    this.mRemote.transact(19, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long getPasswordExpirationTimeout(ComponentName who, int userHandle, boolean parent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userHandle);
                    _data.writeInt(parent);
                    this.mRemote.transact(20, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readLong();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long getPasswordExpiration(ComponentName who, int userHandle, boolean parent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userHandle);
                    _data.writeInt(parent);
                    this.mRemote.transact(21, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readLong();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isActivePasswordSufficient(int userHandle, boolean parent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    _data.writeInt(parent);
                    boolean _result = false;
                    this.mRemote.transact(22, _data, _reply, 0);
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

            public boolean isProfileActivePasswordSufficientForParent(int userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    boolean _result = false;
                    this.mRemote.transact(23, _data, _reply, 0);
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

            public boolean isUsingUnifiedPassword(ComponentName admin) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(24, _data, _reply, 0);
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

            public int getCurrentFailedPasswordAttempts(int userHandle, boolean parent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    _data.writeInt(parent);
                    this.mRemote.transact(25, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getProfileWithMinimumFailedPasswordsForWipe(int userHandle, boolean parent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    _data.writeInt(parent);
                    this.mRemote.transact(26, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setMaximumFailedPasswordsForWipe(ComponentName admin, int num, boolean parent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(num);
                    _data.writeInt(parent);
                    this.mRemote.transact(27, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getMaximumFailedPasswordsForWipe(ComponentName admin, int userHandle, boolean parent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userHandle);
                    _data.writeInt(parent);
                    this.mRemote.transact(28, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean resetPassword(String password, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(password);
                    _data.writeInt(flags);
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

            public void setMaximumTimeToLock(ComponentName who, long timeMs, boolean parent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeLong(timeMs);
                    _data.writeInt(parent);
                    this.mRemote.transact(30, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long getMaximumTimeToLock(ComponentName who, int userHandle, boolean parent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userHandle);
                    _data.writeInt(parent);
                    this.mRemote.transact(31, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readLong();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setRequiredStrongAuthTimeout(ComponentName who, long timeMs, boolean parent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeLong(timeMs);
                    _data.writeInt(parent);
                    this.mRemote.transact(32, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long getRequiredStrongAuthTimeout(ComponentName who, int userId, boolean parent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    _data.writeInt(parent);
                    this.mRemote.transact(33, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readLong();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void lockNow(int flags, boolean parent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(flags);
                    _data.writeInt(parent);
                    this.mRemote.transact(34, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void wipeDataWithReason(int flags, String wipeReasonForUser) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(flags);
                    _data.writeString(wipeReasonForUser);
                    this.mRemote.transact(35, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ComponentName setGlobalProxy(ComponentName admin, String proxySpec, String exclusionList) throws RemoteException {
                ComponentName _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(proxySpec);
                    _data.writeString(exclusionList);
                    this.mRemote.transact(36, _data, _reply, 0);
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

            public ComponentName getGlobalProxyAdmin(int userHandle) throws RemoteException {
                ComponentName _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    this.mRemote.transact(37, _data, _reply, 0);
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

            public void setRecommendedGlobalProxy(ComponentName admin, ProxyInfo proxyInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (proxyInfo != null) {
                        _data.writeInt(1);
                        proxyInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(38, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setStorageEncryption(ComponentName who, boolean encrypt) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(encrypt);
                    this.mRemote.transact(39, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean getStorageEncryption(ComponentName who, int userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userHandle);
                    this.mRemote.transact(40, _data, _reply, 0);
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

            public int getStorageEncryptionStatus(String callerPackage, int userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callerPackage);
                    _data.writeInt(userHandle);
                    this.mRemote.transact(41, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean requestBugreport(ComponentName who) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(42, _data, _reply, 0);
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

            public void setCameraDisabled(ComponentName who, boolean disabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(disabled);
                    this.mRemote.transact(43, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean getCameraDisabled(ComponentName who, int userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userHandle);
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

            public void setScreenCaptureDisabled(ComponentName who, boolean disabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(disabled);
                    this.mRemote.transact(45, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean getScreenCaptureDisabled(ComponentName who, int userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userHandle);
                    this.mRemote.transact(46, _data, _reply, 0);
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

            public void setKeyguardDisabledFeatures(ComponentName who, int which, boolean parent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(which);
                    _data.writeInt(parent);
                    this.mRemote.transact(47, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getKeyguardDisabledFeatures(ComponentName who, int userHandle, boolean parent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userHandle);
                    _data.writeInt(parent);
                    this.mRemote.transact(48, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setActiveAdmin(ComponentName policyReceiver, boolean refreshing, int userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (policyReceiver != null) {
                        _data.writeInt(1);
                        policyReceiver.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(refreshing);
                    _data.writeInt(userHandle);
                    this.mRemote.transact(49, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isAdminActive(ComponentName policyReceiver, int userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (policyReceiver != null) {
                        _data.writeInt(1);
                        policyReceiver.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userHandle);
                    this.mRemote.transact(50, _data, _reply, 0);
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

            public List<ComponentName> getActiveAdmins(int userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    this.mRemote.transact(51, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createTypedArrayList(ComponentName.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean packageHasActiveAdmins(String packageName, int userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userHandle);
                    boolean _result = false;
                    this.mRemote.transact(52, _data, _reply, 0);
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

            public void getRemoveWarning(ComponentName policyReceiver, RemoteCallback result, int userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (policyReceiver != null) {
                        _data.writeInt(1);
                        policyReceiver.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (result != null) {
                        _data.writeInt(1);
                        result.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userHandle);
                    this.mRemote.transact(53, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeActiveAdmin(ComponentName policyReceiver, int userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (policyReceiver != null) {
                        _data.writeInt(1);
                        policyReceiver.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userHandle);
                    this.mRemote.transact(54, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void forceRemoveActiveAdmin(ComponentName policyReceiver, int userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (policyReceiver != null) {
                        _data.writeInt(1);
                        policyReceiver.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userHandle);
                    this.mRemote.transact(55, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean hasGrantedPolicy(ComponentName policyReceiver, int usesPolicy, int userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (policyReceiver != null) {
                        _data.writeInt(1);
                        policyReceiver.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(usesPolicy);
                    _data.writeInt(userHandle);
                    this.mRemote.transact(56, _data, _reply, 0);
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

            public void setActivePasswordState(PasswordMetrics metrics, int userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (metrics != null) {
                        _data.writeInt(1);
                        metrics.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userHandle);
                    this.mRemote.transact(57, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void reportPasswordChanged(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(58, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void reportFailedPasswordAttempt(int userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    this.mRemote.transact(59, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void reportSuccessfulPasswordAttempt(int userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    this.mRemote.transact(60, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void reportFailedFingerprintAttempt(int userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    this.mRemote.transact(61, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void reportSuccessfulFingerprintAttempt(int userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    this.mRemote.transact(62, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void reportKeyguardDismissed(int userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    this.mRemote.transact(63, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void reportKeyguardSecured(int userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    this.mRemote.transact(64, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setDeviceOwner(ComponentName who, String ownerName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(ownerName);
                    _data.writeInt(userId);
                    this.mRemote.transact(65, _data, _reply, 0);
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

            public ComponentName getDeviceOwnerComponent(boolean callingUserOnly) throws RemoteException {
                ComponentName _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(callingUserOnly);
                    this.mRemote.transact(66, _data, _reply, 0);
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

            public boolean hasDeviceOwner() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(67, _data, _reply, 0);
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

            public String getDeviceOwnerName() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(68, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void clearDeviceOwner(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    this.mRemote.transact(69, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getDeviceOwnerUserId() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(70, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setProfileOwner(ComponentName who, String ownerName, int userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(ownerName);
                    _data.writeInt(userHandle);
                    this.mRemote.transact(71, _data, _reply, 0);
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

            public ComponentName getProfileOwner(int userHandle) throws RemoteException {
                ComponentName _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    this.mRemote.transact(72, _data, _reply, 0);
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

            public String getProfileOwnerName(int userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    this.mRemote.transact(73, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setProfileEnabled(ComponentName who) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(74, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setProfileName(ComponentName who, String profileName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(profileName);
                    this.mRemote.transact(75, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void clearProfileOwner(ComponentName who) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(76, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean hasUserSetupCompleted() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(77, _data, _reply, 0);
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

            public void setDeviceOwnerLockScreenInfo(ComponentName who, CharSequence deviceOwnerInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (deviceOwnerInfo != null) {
                        _data.writeInt(1);
                        TextUtils.writeToParcel(deviceOwnerInfo, _data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(78, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public CharSequence getDeviceOwnerLockScreenInfo() throws RemoteException {
                CharSequence _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(79, _data, _reply, 0);
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

            public String[] setPackagesSuspended(ComponentName admin, String callerPackage, String[] packageNames, boolean suspended) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(callerPackage);
                    _data.writeStringArray(packageNames);
                    _data.writeInt(suspended);
                    this.mRemote.transact(80, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isPackageSuspended(ComponentName admin, String callerPackage, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(callerPackage);
                    _data.writeString(packageName);
                    this.mRemote.transact(81, _data, _reply, 0);
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

            public boolean installCaCert(ComponentName admin, String callerPackage, byte[] certBuffer) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(callerPackage);
                    _data.writeByteArray(certBuffer);
                    this.mRemote.transact(82, _data, _reply, 0);
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

            public void uninstallCaCerts(ComponentName admin, String callerPackage, String[] aliases) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(callerPackage);
                    _data.writeStringArray(aliases);
                    this.mRemote.transact(83, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void enforceCanManageCaCerts(ComponentName admin, String callerPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(callerPackage);
                    this.mRemote.transact(84, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean approveCaCert(String alias, int userHandle, boolean approval) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(alias);
                    _data.writeInt(userHandle);
                    _data.writeInt(approval);
                    boolean _result = false;
                    this.mRemote.transact(85, _data, _reply, 0);
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

            public boolean isCaCertApproved(String alias, int userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(alias);
                    _data.writeInt(userHandle);
                    boolean _result = false;
                    this.mRemote.transact(86, _data, _reply, 0);
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

            public boolean installKeyPair(ComponentName who, String callerPackage, byte[] privKeyBuffer, byte[] certBuffer, byte[] certChainBuffer, String alias, boolean requestAccess, boolean isUserSelectable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(callerPackage);
                    _data.writeByteArray(privKeyBuffer);
                    _data.writeByteArray(certBuffer);
                    _data.writeByteArray(certChainBuffer);
                    _data.writeString(alias);
                    _data.writeInt(requestAccess);
                    _data.writeInt(isUserSelectable);
                    this.mRemote.transact(87, _data, _reply, 0);
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

            public boolean removeKeyPair(ComponentName who, String callerPackage, String alias) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(callerPackage);
                    _data.writeString(alias);
                    this.mRemote.transact(88, _data, _reply, 0);
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

            public boolean generateKeyPair(ComponentName who, String callerPackage, String algorithm, ParcelableKeyGenParameterSpec keySpec, int idAttestationFlags, KeymasterCertificateChain attestationChain) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(callerPackage);
                    _data.writeString(algorithm);
                    if (keySpec != null) {
                        _data.writeInt(1);
                        keySpec.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(idAttestationFlags);
                    this.mRemote.transact(89, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    if (_reply.readInt() != 0) {
                        attestationChain.readFromParcel(_reply);
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setKeyPairCertificate(ComponentName who, String callerPackage, String alias, byte[] certBuffer, byte[] certChainBuffer, boolean isUserSelectable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(callerPackage);
                    _data.writeString(alias);
                    _data.writeByteArray(certBuffer);
                    _data.writeByteArray(certChainBuffer);
                    _data.writeInt(isUserSelectable);
                    this.mRemote.transact(90, _data, _reply, 0);
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

            public void choosePrivateKeyAlias(int uid, Uri uri, String alias, IBinder aliasCallback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    if (uri != null) {
                        _data.writeInt(1);
                        uri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(alias);
                    _data.writeStrongBinder(aliasCallback);
                    this.mRemote.transact(91, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setDelegatedScopes(ComponentName who, String delegatePackage, List<String> scopes) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(delegatePackage);
                    _data.writeStringList(scopes);
                    this.mRemote.transact(92, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> getDelegatedScopes(ComponentName who, String delegatePackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(delegatePackage);
                    this.mRemote.transact(93, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArrayList();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> getDelegatePackages(ComponentName who, String scope) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(scope);
                    this.mRemote.transact(94, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArrayList();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setCertInstallerPackage(ComponentName who, String installerPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(installerPackage);
                    this.mRemote.transact(95, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getCertInstallerPackage(ComponentName who) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(96, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setAlwaysOnVpnPackage(ComponentName who, String vpnPackage, boolean lockdown) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(vpnPackage);
                    _data.writeInt(lockdown);
                    this.mRemote.transact(97, _data, _reply, 0);
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

            public String getAlwaysOnVpnPackage(ComponentName who) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(98, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addPersistentPreferredActivity(ComponentName admin, IntentFilter filter, ComponentName activity) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
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
                    this.mRemote.transact(99, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void clearPackagePersistentPreferredActivities(ComponentName admin, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(packageName);
                    this.mRemote.transact(100, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setDefaultSmsApplication(ComponentName admin, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(packageName);
                    this.mRemote.transact(101, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setApplicationRestrictions(ComponentName who, String callerPackage, String packageName, Bundle settings) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(callerPackage);
                    _data.writeString(packageName);
                    if (settings != null) {
                        _data.writeInt(1);
                        settings.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(102, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Bundle getApplicationRestrictions(ComponentName who, String callerPackage, String packageName) throws RemoteException {
                Bundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(callerPackage);
                    _data.writeString(packageName);
                    this.mRemote.transact(103, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (Bundle) Bundle.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setApplicationRestrictionsManagingPackage(ComponentName admin, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(packageName);
                    this.mRemote.transact(104, _data, _reply, 0);
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

            public String getApplicationRestrictionsManagingPackage(ComponentName admin) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(105, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isCallerApplicationRestrictionsManagingPackage(String callerPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callerPackage);
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

            public void setRestrictionsProvider(ComponentName who, ComponentName provider) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (provider != null) {
                        _data.writeInt(1);
                        provider.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(107, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ComponentName getRestrictionsProvider(int userHandle) throws RemoteException {
                ComponentName _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    this.mRemote.transact(108, _data, _reply, 0);
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

            public void setUserRestriction(ComponentName who, String key, boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(key);
                    _data.writeInt(enable);
                    this.mRemote.transact(109, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Bundle getUserRestrictions(ComponentName who) throws RemoteException {
                Bundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(110, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (Bundle) Bundle.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addCrossProfileIntentFilter(ComponentName admin, IntentFilter filter, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (filter != null) {
                        _data.writeInt(1);
                        filter.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(flags);
                    this.mRemote.transact(111, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void clearCrossProfileIntentFilters(ComponentName admin) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(112, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setPermittedAccessibilityServices(ComponentName admin, List packageList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeList(packageList);
                    this.mRemote.transact(113, _data, _reply, 0);
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

            public List getPermittedAccessibilityServices(ComponentName admin) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(114, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readArrayList(getClass().getClassLoader());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List getPermittedAccessibilityServicesForUser(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(115, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readArrayList(getClass().getClassLoader());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isAccessibilityServicePermittedByAdmin(ComponentName admin, String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    this.mRemote.transact(116, _data, _reply, 0);
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

            public boolean setPermittedInputMethods(ComponentName admin, List packageList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeList(packageList);
                    this.mRemote.transact(117, _data, _reply, 0);
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

            public List getPermittedInputMethods(ComponentName admin) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(118, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readArrayList(getClass().getClassLoader());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List getPermittedInputMethodsForCurrentUser() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(119, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readArrayList(getClass().getClassLoader());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isInputMethodPermittedByAdmin(ComponentName admin, String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    this.mRemote.transact(120, _data, _reply, 0);
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

            public boolean setPermittedCrossProfileNotificationListeners(ComponentName admin, List<String> packageList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStringList(packageList);
                    this.mRemote.transact(121, _data, _reply, 0);
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

            public List<String> getPermittedCrossProfileNotificationListeners(ComponentName admin) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(122, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArrayList();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isNotificationListenerServicePermitted(String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    boolean _result = false;
                    this.mRemote.transact(123, _data, _reply, 0);
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

            public Intent createAdminSupportIntent(String restriction) throws RemoteException {
                Intent _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(restriction);
                    this.mRemote.transact(124, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Intent.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setApplicationHidden(ComponentName admin, String callerPackage, String packageName, boolean hidden) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(callerPackage);
                    _data.writeString(packageName);
                    _data.writeInt(hidden);
                    this.mRemote.transact(125, _data, _reply, 0);
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

            public boolean isApplicationHidden(ComponentName admin, String callerPackage, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(callerPackage);
                    _data.writeString(packageName);
                    this.mRemote.transact(126, _data, _reply, 0);
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

            public UserHandle createAndManageUser(ComponentName who, String name, ComponentName profileOwner, PersistableBundle adminExtras, int flags) throws RemoteException {
                UserHandle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(name);
                    if (profileOwner != null) {
                        _data.writeInt(1);
                        profileOwner.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (adminExtras != null) {
                        _data.writeInt(1);
                        adminExtras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(flags);
                    this.mRemote.transact(127, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (UserHandle) UserHandle.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean removeUser(ComponentName who, UserHandle userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (userHandle != null) {
                        _data.writeInt(1);
                        userHandle.writeToParcel(_data, 0);
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

            public boolean switchUser(ComponentName who, UserHandle userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (userHandle != null) {
                        _data.writeInt(1);
                        userHandle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(129, _data, _reply, 0);
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

            public int startUserInBackground(ComponentName who, UserHandle userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (userHandle != null) {
                        _data.writeInt(1);
                        userHandle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(130, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int stopUser(ComponentName who, UserHandle userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (userHandle != null) {
                        _data.writeInt(1);
                        userHandle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(131, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int logoutUser(ComponentName who) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(132, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<UserHandle> getSecondaryUsers(ComponentName who) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(133, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createTypedArrayList(UserHandle.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void enableSystemApp(ComponentName admin, String callerPackage, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(callerPackage);
                    _data.writeString(packageName);
                    this.mRemote.transact(134, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int enableSystemAppWithIntent(ComponentName admin, String callerPackage, Intent intent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(callerPackage);
                    if (intent != null) {
                        _data.writeInt(1);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(135, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean installExistingPackage(ComponentName admin, String callerPackage, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(callerPackage);
                    _data.writeString(packageName);
                    this.mRemote.transact(136, _data, _reply, 0);
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

            public void setAccountManagementDisabled(ComponentName who, String accountType, boolean disabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(accountType);
                    _data.writeInt(disabled);
                    this.mRemote.transact(137, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String[] getAccountTypesWithManagementDisabled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(138, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String[] getAccountTypesWithManagementDisabledAsUser(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(139, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setLockTaskPackages(ComponentName who, String[] packages) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStringArray(packages);
                    this.mRemote.transact(140, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String[] getLockTaskPackages(ComponentName who) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(141, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isLockTaskPermitted(String pkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
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

            public void setLockTaskFeatures(ComponentName who, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(flags);
                    this.mRemote.transact(143, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getLockTaskFeatures(ComponentName who) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(144, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setGlobalSetting(ComponentName who, String setting, String value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(setting);
                    _data.writeString(value);
                    this.mRemote.transact(145, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setSystemSetting(ComponentName who, String setting, String value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(setting);
                    _data.writeString(value);
                    this.mRemote.transact(146, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setSecureSetting(ComponentName who, String setting, String value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(setting);
                    _data.writeString(value);
                    this.mRemote.transact(147, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setTime(ComponentName who, long millis) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeLong(millis);
                    this.mRemote.transact(148, _data, _reply, 0);
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

            public boolean setTimeZone(ComponentName who, String timeZone) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(timeZone);
                    this.mRemote.transact(149, _data, _reply, 0);
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

            public void setMasterVolumeMuted(ComponentName admin, boolean on) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(on);
                    this.mRemote.transact(150, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isMasterVolumeMuted(ComponentName admin) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(151, _data, _reply, 0);
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

            public void notifyLockTaskModeChanged(boolean isEnabled, String pkg, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(isEnabled);
                    _data.writeString(pkg);
                    _data.writeInt(userId);
                    this.mRemote.transact(152, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setUninstallBlocked(ComponentName admin, String callerPackage, String packageName, boolean uninstallBlocked) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(callerPackage);
                    _data.writeString(packageName);
                    _data.writeInt(uninstallBlocked);
                    this.mRemote.transact(153, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isUninstallBlocked(ComponentName admin, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(packageName);
                    this.mRemote.transact(154, _data, _reply, 0);
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

            public void setCrossProfileCallerIdDisabled(ComponentName who, boolean disabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(disabled);
                    this.mRemote.transact(155, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean getCrossProfileCallerIdDisabled(ComponentName who) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
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

            public boolean getCrossProfileCallerIdDisabledForUser(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    boolean _result = false;
                    this.mRemote.transact(157, _data, _reply, 0);
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

            public void setCrossProfileContactsSearchDisabled(ComponentName who, boolean disabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(disabled);
                    this.mRemote.transact(158, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean getCrossProfileContactsSearchDisabled(ComponentName who) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(159, _data, _reply, 0);
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

            public boolean getCrossProfileContactsSearchDisabledForUser(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    boolean _result = false;
                    this.mRemote.transact(160, _data, _reply, 0);
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

            public void startManagedQuickContact(String lookupKey, long contactId, boolean isContactIdIgnored, long directoryId, Intent originalIntent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(lookupKey);
                    _data.writeLong(contactId);
                    _data.writeInt(isContactIdIgnored);
                    _data.writeLong(directoryId);
                    if (originalIntent != null) {
                        _data.writeInt(1);
                        originalIntent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(161, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setBluetoothContactSharingDisabled(ComponentName who, boolean disabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(disabled);
                    this.mRemote.transact(162, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean getBluetoothContactSharingDisabled(ComponentName who) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(163, _data, _reply, 0);
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

            public boolean getBluetoothContactSharingDisabledForUser(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    boolean _result = false;
                    this.mRemote.transact(164, _data, _reply, 0);
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

            public void setTrustAgentConfiguration(ComponentName admin, ComponentName agent, PersistableBundle args, boolean parent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (agent != null) {
                        _data.writeInt(1);
                        agent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (args != null) {
                        _data.writeInt(1);
                        args.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(parent);
                    this.mRemote.transact(165, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<PersistableBundle> getTrustAgentConfiguration(ComponentName admin, ComponentName agent, int userId, boolean parent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (agent != null) {
                        _data.writeInt(1);
                        agent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    _data.writeInt(parent);
                    this.mRemote.transact(166, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createTypedArrayList(PersistableBundle.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean addCrossProfileWidgetProvider(ComponentName admin, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(packageName);
                    this.mRemote.transact(167, _data, _reply, 0);
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

            public boolean removeCrossProfileWidgetProvider(ComponentName admin, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(packageName);
                    this.mRemote.transact(168, _data, _reply, 0);
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

            public List<String> getCrossProfileWidgetProviders(ComponentName admin) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(169, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArrayList();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setAutoTimeRequired(ComponentName who, boolean required) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(required);
                    this.mRemote.transact(170, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean getAutoTimeRequired() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
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

            public void setForceEphemeralUsers(ComponentName who, boolean forceEpehemeralUsers) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(forceEpehemeralUsers);
                    this.mRemote.transact(172, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean getForceEphemeralUsers(ComponentName who) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(173, _data, _reply, 0);
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

            public boolean isRemovingAdmin(ComponentName adminReceiver, int userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (adminReceiver != null) {
                        _data.writeInt(1);
                        adminReceiver.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userHandle);
                    this.mRemote.transact(174, _data, _reply, 0);
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

            public void setUserIcon(ComponentName admin, Bitmap icon) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (icon != null) {
                        _data.writeInt(1);
                        icon.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(175, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setSystemUpdatePolicy(ComponentName who, SystemUpdatePolicy policy) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (policy != null) {
                        _data.writeInt(1);
                        policy.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(176, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public SystemUpdatePolicy getSystemUpdatePolicy() throws RemoteException {
                SystemUpdatePolicy _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(177, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = SystemUpdatePolicy.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void clearSystemUpdatePolicyFreezePeriodRecord() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(178, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setKeyguardDisabled(ComponentName admin, boolean disabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(disabled);
                    this.mRemote.transact(179, _data, _reply, 0);
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

            public boolean setStatusBarDisabled(ComponentName who, boolean disabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (who != null) {
                        _data.writeInt(1);
                        who.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(disabled);
                    this.mRemote.transact(180, _data, _reply, 0);
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

            public boolean getDoNotAskCredentialsOnBoot() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(181, _data, _reply, 0);
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

            public void notifyPendingSystemUpdate(SystemUpdateInfo info) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (info != null) {
                        _data.writeInt(1);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(182, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public SystemUpdateInfo getPendingSystemUpdate(ComponentName admin) throws RemoteException {
                SystemUpdateInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(183, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = SystemUpdateInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setPermissionPolicy(ComponentName admin, String callerPackage, int policy) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(callerPackage);
                    _data.writeInt(policy);
                    this.mRemote.transact(184, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getPermissionPolicy(ComponentName admin) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(185, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setPermissionGrantState(ComponentName admin, String callerPackage, String packageName, String permission, int grantState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(callerPackage);
                    _data.writeString(packageName);
                    _data.writeString(permission);
                    _data.writeInt(grantState);
                    this.mRemote.transact(186, _data, _reply, 0);
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

            public int getPermissionGrantState(ComponentName admin, String callerPackage, String packageName, String permission) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(callerPackage);
                    _data.writeString(packageName);
                    _data.writeString(permission);
                    this.mRemote.transact(187, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isProvisioningAllowed(String action, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(action);
                    _data.writeString(packageName);
                    boolean _result = false;
                    this.mRemote.transact(188, _data, _reply, 0);
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

            public int checkProvisioningPreCondition(String action, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(action);
                    _data.writeString(packageName);
                    this.mRemote.transact(189, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setKeepUninstalledPackages(ComponentName admin, String callerPackage, List<String> packageList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(callerPackage);
                    _data.writeStringList(packageList);
                    this.mRemote.transact(190, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> getKeepUninstalledPackages(ComponentName admin, String callerPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(callerPackage);
                    this.mRemote.transact(191, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArrayList();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isManagedProfile(ComponentName admin) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(192, _data, _reply, 0);
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

            public boolean isSystemOnlyUser(ComponentName admin) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(193, _data, _reply, 0);
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

            public String getWifiMacAddress(ComponentName admin) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(194, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void reboot(ComponentName admin) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(195, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setShortSupportMessage(ComponentName admin, CharSequence message) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (message != null) {
                        _data.writeInt(1);
                        TextUtils.writeToParcel(message, _data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(196, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public CharSequence getShortSupportMessage(ComponentName admin) throws RemoteException {
                CharSequence _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(197, _data, _reply, 0);
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

            public void setLongSupportMessage(ComponentName admin, CharSequence message) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (message != null) {
                        _data.writeInt(1);
                        TextUtils.writeToParcel(message, _data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(198, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public CharSequence getLongSupportMessage(ComponentName admin) throws RemoteException {
                CharSequence _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(199, _data, _reply, 0);
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

            public CharSequence getShortSupportMessageForUser(ComponentName admin, int userHandle) throws RemoteException {
                CharSequence _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userHandle);
                    this.mRemote.transact(200, _data, _reply, 0);
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

            public CharSequence getLongSupportMessageForUser(ComponentName admin, int userHandle) throws RemoteException {
                CharSequence _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userHandle);
                    this.mRemote.transact(201, _data, _reply, 0);
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

            public boolean isSeparateProfileChallengeAllowed(int userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    boolean _result = false;
                    this.mRemote.transact(202, _data, _reply, 0);
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

            public void setOrganizationColor(ComponentName admin, int color) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(color);
                    this.mRemote.transact(203, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setOrganizationColorForUser(int color, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(color);
                    _data.writeInt(userId);
                    this.mRemote.transact(204, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getOrganizationColor(ComponentName admin) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(205, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getOrganizationColorForUser(int userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    this.mRemote.transact(206, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setOrganizationName(ComponentName admin, CharSequence title) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (title != null) {
                        _data.writeInt(1);
                        TextUtils.writeToParcel(title, _data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(207, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public CharSequence getOrganizationName(ComponentName admin) throws RemoteException {
                CharSequence _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(208, _data, _reply, 0);
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

            public CharSequence getDeviceOwnerOrganizationName() throws RemoteException {
                CharSequence _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(209, _data, _reply, 0);
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

            public CharSequence getOrganizationNameForUser(int userHandle) throws RemoteException {
                CharSequence _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    this.mRemote.transact(210, _data, _reply, 0);
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

            public int getUserProvisioningState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(211, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setUserProvisioningState(int state, int userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    _data.writeInt(userHandle);
                    this.mRemote.transact(212, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setAffiliationIds(ComponentName admin, List<String> ids) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStringList(ids);
                    this.mRemote.transact(213, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> getAffiliationIds(ComponentName admin) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(214, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArrayList();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isAffiliatedUser() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(215, _data, _reply, 0);
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

            public void setSecurityLoggingEnabled(ComponentName admin, boolean enabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(enabled);
                    this.mRemote.transact(216, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isSecurityLoggingEnabled(ComponentName admin) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(217, _data, _reply, 0);
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

            public ParceledListSlice retrieveSecurityLogs(ComponentName admin) throws RemoteException {
                ParceledListSlice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(218, _data, _reply, 0);
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

            public ParceledListSlice retrievePreRebootSecurityLogs(ComponentName admin) throws RemoteException {
                ParceledListSlice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(219, _data, _reply, 0);
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

            public long forceSecurityLogs() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(220, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readLong();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isUninstallInQueue(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    boolean _result = false;
                    this.mRemote.transact(221, _data, _reply, 0);
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

            public void uninstallPackageWithActiveAdmins(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    this.mRemote.transact(222, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isDeviceProvisioned() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(223, _data, _reply, 0);
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

            public boolean isDeviceProvisioningConfigApplied() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(224, _data, _reply, 0);
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

            public void setDeviceProvisioningConfigApplied() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(225, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void forceUpdateUserSetupComplete() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(226, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setBackupServiceEnabled(ComponentName admin, boolean enabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(enabled);
                    this.mRemote.transact(227, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isBackupServiceEnabled(ComponentName admin) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(228, _data, _reply, 0);
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

            public boolean setMandatoryBackupTransport(ComponentName admin, ComponentName backupTransportComponent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (backupTransportComponent != null) {
                        _data.writeInt(1);
                        backupTransportComponent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(229, _data, _reply, 0);
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

            public ComponentName getMandatoryBackupTransport() throws RemoteException {
                ComponentName _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(230, _data, _reply, 0);
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

            public void setNetworkLoggingEnabled(ComponentName admin, boolean enabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(enabled);
                    this.mRemote.transact(231, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isNetworkLoggingEnabled(ComponentName admin) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(232, _data, _reply, 0);
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

            public List<NetworkEvent> retrieveNetworkLogs(ComponentName admin, long batchToken) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeLong(batchToken);
                    this.mRemote.transact(233, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createTypedArrayList(NetworkEvent.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean bindDeviceAdminServiceAsUser(ComponentName admin, IApplicationThread caller, IBinder token, Intent service, IServiceConnection connection, int flags, int targetUserId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    IBinder iBinder = null;
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeStrongBinder(token);
                    if (service != null) {
                        _data.writeInt(1);
                        service.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (connection != null) {
                        iBinder = connection.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(flags);
                    _data.writeInt(targetUserId);
                    this.mRemote.transact(234, _data, _reply, 0);
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

            public List<UserHandle> getBindDeviceAdminTargetUsers(ComponentName admin) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(235, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createTypedArrayList(UserHandle.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isEphemeralUser(ComponentName admin) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(236, _data, _reply, 0);
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

            public long getLastSecurityLogRetrievalTime() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(237, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readLong();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long getLastBugReportRequestTime() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(238, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readLong();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long getLastNetworkLogRetrievalTime() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(239, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readLong();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setResetPasswordToken(ComponentName admin, byte[] token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeByteArray(token);
                    this.mRemote.transact(240, _data, _reply, 0);
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

            public boolean clearResetPasswordToken(ComponentName admin) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(241, _data, _reply, 0);
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

            public boolean isResetPasswordTokenActive(ComponentName admin) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(242, _data, _reply, 0);
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

            public boolean resetPasswordWithToken(ComponentName admin, String password, byte[] token, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(password);
                    _data.writeByteArray(token);
                    _data.writeInt(flags);
                    this.mRemote.transact(243, _data, _reply, 0);
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

            public boolean isCurrentInputMethodSetByOwner() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(244, _data, _reply, 0);
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

            public StringParceledListSlice getOwnerInstalledCaCerts(UserHandle user) throws RemoteException {
                StringParceledListSlice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (user != null) {
                        _data.writeInt(1);
                        user.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(245, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (StringParceledListSlice) StringParceledListSlice.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void clearApplicationUserData(ComponentName admin, String packageName, IPackageDataObserver callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(packageName);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(246, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setLogoutEnabled(ComponentName admin, boolean enabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(enabled);
                    this.mRemote.transact(247, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isLogoutEnabled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(248, _data, _reply, 0);
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

            public List<String> getDisallowedSystemApps(ComponentName admin, int userId, String provisioningAction) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    _data.writeString(provisioningAction);
                    this.mRemote.transact(249, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArrayList();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void transferOwnership(ComponentName admin, ComponentName target, PersistableBundle bundle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (target != null) {
                        _data.writeInt(1);
                        target.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(250, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public PersistableBundle getTransferOwnershipBundle() throws RemoteException {
                PersistableBundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(251, _data, _reply, 0);
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

            public void setStartUserSessionMessage(ComponentName admin, CharSequence startUserSessionMessage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (startUserSessionMessage != null) {
                        _data.writeInt(1);
                        TextUtils.writeToParcel(startUserSessionMessage, _data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(252, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setEndUserSessionMessage(ComponentName admin, CharSequence endUserSessionMessage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (endUserSessionMessage != null) {
                        _data.writeInt(1);
                        TextUtils.writeToParcel(endUserSessionMessage, _data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(253, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public CharSequence getStartUserSessionMessage(ComponentName admin) throws RemoteException {
                CharSequence _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(254, _data, _reply, 0);
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

            public CharSequence getEndUserSessionMessage(ComponentName admin) throws RemoteException {
                CharSequence _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(255, _data, _reply, 0);
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

            public List<String> setMeteredDataDisabledPackages(ComponentName admin, List<String> packageNames) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStringList(packageNames);
                    this.mRemote.transact(256, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArrayList();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> getMeteredDataDisabledPackages(ComponentName admin) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(257, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArrayList();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int addOverrideApn(ComponentName admin, ApnSetting apnSetting) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (apnSetting != null) {
                        _data.writeInt(1);
                        apnSetting.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(258, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean updateOverrideApn(ComponentName admin, int apnId, ApnSetting apnSetting) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(apnId);
                    if (apnSetting != null) {
                        _data.writeInt(1);
                        apnSetting.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(259, _data, _reply, 0);
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

            public boolean removeOverrideApn(ComponentName admin, int apnId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(apnId);
                    this.mRemote.transact(260, _data, _reply, 0);
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

            public List<ApnSetting> getOverrideApns(ComponentName admin) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(261, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createTypedArrayList(ApnSetting.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setOverrideApnsEnabled(ComponentName admin, boolean enabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(enabled);
                    this.mRemote.transact(262, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isOverrideApnEnabled(ComponentName admin) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(263, _data, _reply, 0);
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

            public boolean isMeteredDataDisabledPackageForUser(ComponentName admin, String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    this.mRemote.transact(264, _data, _reply, 0);
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
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IDevicePolicyManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IDevicePolicyManager)) {
                return new Proxy(obj);
            }
            return (IDevicePolicyManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v1, resolved type: android.security.keystore.ParcelableKeyGenParameterSpec} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v2, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v6, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v10, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v14, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v18, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v22, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v26, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v30, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v34, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v38, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v42, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v46, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v50, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v54, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v58, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v62, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v66, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v70, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v74, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v78, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v82, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v88, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v94, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v98, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v103, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v107, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v111, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v115, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v121, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v126, resolved type: android.net.ProxyInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v130, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v134, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v139, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v143, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v147, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v151, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v155, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v159, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v163, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v167, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v171, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v177, resolved type: android.os.RemoteCallback} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v181, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v185, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v189, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v193, resolved type: android.app.admin.PasswordMetrics} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v204, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v216, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v222, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v226, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v230, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v240, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v244, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v248, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v252, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v256, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v270, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v274, resolved type: android.security.keystore.ParcelableKeyGenParameterSpec} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v280, resolved type: android.security.keystore.ParcelableKeyGenParameterSpec} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v292, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v296, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v300, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v304, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v308, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v312, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v316, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v320, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v324, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v328, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v332, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v336, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v340, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v344, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v349, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v354, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v358, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v366, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v370, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v374, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v379, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v383, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v387, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v392, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v396, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v400, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v406, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v410, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v420, resolved type: android.os.UserHandle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v424, resolved type: android.os.UserHandle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v428, resolved type: android.os.UserHandle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v432, resolved type: android.os.UserHandle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v436, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v440, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v444, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v452, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v456, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v462, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v466, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v471, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v475, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v479, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v483, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v487, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v491, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v495, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v499, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v503, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v509, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v513, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v517, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v521, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v526, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v530, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v540, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v544, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v549, resolved type: android.os.PersistableBundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v553, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v557, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v561, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v565, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v569, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v574, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v578, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v582, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v586, resolved type: android.graphics.Bitmap} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v590, resolved type: android.app.admin.SystemUpdatePolicy} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v595, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v599, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v604, resolved type: android.app.admin.SystemUpdateInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v608, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v612, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v616, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v626, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v632, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v636, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v640, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v644, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v648, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v652, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v660, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v668, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v672, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v676, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v681, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v686, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v695, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v703, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v707, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v712, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v715, resolved type: android.os.RemoteCallback} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v716, resolved type: android.os.RemoteCallback} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v719, resolved type: android.os.RemoteCallback} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v720, resolved type: android.os.RemoteCallback} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v723, resolved type: android.os.RemoteCallback} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v724, resolved type: android.os.RemoteCallback} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v727, resolved type: android.os.RemoteCallback} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v733, resolved type: android.os.RemoteCallback} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v736, resolved type: android.os.RemoteCallback} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v737, resolved type: android.os.RemoteCallback} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v740, resolved type: android.os.RemoteCallback} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v741, resolved type: android.os.RemoteCallback} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v744, resolved type: android.os.RemoteCallback} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v746, resolved type: android.os.RemoteCallback} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v749, resolved type: android.os.RemoteCallback} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v750, resolved type: android.os.RemoteCallback} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v753, resolved type: android.os.RemoteCallback} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v754, resolved type: android.os.RemoteCallback} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v757, resolved type: android.os.RemoteCallback} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v758, resolved type: android.os.RemoteCallback} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v13, resolved type: android.security.keystore.ParcelableKeyGenParameterSpec} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v764, resolved type: android.security.keystore.ParcelableKeyGenParameterSpec} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v765, resolved type: android.security.keystore.ParcelableKeyGenParameterSpec} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v768, resolved type: android.security.keystore.ParcelableKeyGenParameterSpec} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v769, resolved type: android.security.keystore.ParcelableKeyGenParameterSpec} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v772, resolved type: android.security.keystore.ParcelableKeyGenParameterSpec} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v776, resolved type: android.security.keystore.ParcelableKeyGenParameterSpec} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v779, resolved type: android.security.keystore.ParcelableKeyGenParameterSpec} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v780, resolved type: android.security.keystore.ParcelableKeyGenParameterSpec} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v783, resolved type: android.security.keystore.ParcelableKeyGenParameterSpec} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v784, resolved type: android.security.keystore.ParcelableKeyGenParameterSpec} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v787, resolved type: android.security.keystore.ParcelableKeyGenParameterSpec} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v788, resolved type: android.security.keystore.ParcelableKeyGenParameterSpec} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v791, resolved type: android.security.keystore.ParcelableKeyGenParameterSpec} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v793, resolved type: android.security.keystore.ParcelableKeyGenParameterSpec} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v796, resolved type: android.security.keystore.ParcelableKeyGenParameterSpec} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v797, resolved type: android.security.keystore.ParcelableKeyGenParameterSpec} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v800, resolved type: android.security.keystore.ParcelableKeyGenParameterSpec} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v801, resolved type: android.security.keystore.ParcelableKeyGenParameterSpec} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v804, resolved type: android.security.keystore.ParcelableKeyGenParameterSpec} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v806, resolved type: android.security.keystore.ParcelableKeyGenParameterSpec} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v809, resolved type: android.security.keystore.ParcelableKeyGenParameterSpec} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v810, resolved type: android.security.keystore.ParcelableKeyGenParameterSpec} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v813, resolved type: android.security.keystore.ParcelableKeyGenParameterSpec} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v815, resolved type: android.security.keystore.ParcelableKeyGenParameterSpec} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v818, resolved type: android.security.keystore.ParcelableKeyGenParameterSpec} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v819, resolved type: android.security.keystore.ParcelableKeyGenParameterSpec} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v822, resolved type: android.security.keystore.ParcelableKeyGenParameterSpec} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v823, resolved type: android.security.keystore.ParcelableKeyGenParameterSpec} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v826, resolved type: android.security.keystore.ParcelableKeyGenParameterSpec} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v827, resolved type: android.security.keystore.ParcelableKeyGenParameterSpec} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v830, resolved type: android.security.keystore.ParcelableKeyGenParameterSpec} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v831, resolved type: android.security.keystore.ParcelableKeyGenParameterSpec} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v834, resolved type: android.security.keystore.ParcelableKeyGenParameterSpec} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v835, resolved type: android.security.keystore.ParcelableKeyGenParameterSpec} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v838, resolved type: android.security.keystore.ParcelableKeyGenParameterSpec} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v839, resolved type: android.security.keystore.ParcelableKeyGenParameterSpec} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v842, resolved type: android.security.keystore.ParcelableKeyGenParameterSpec} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v843, resolved type: android.security.keystore.ParcelableKeyGenParameterSpec} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v846, resolved type: android.security.keystore.ParcelableKeyGenParameterSpec} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v847, resolved type: android.security.keystore.ParcelableKeyGenParameterSpec} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v850, resolved type: android.security.keystore.ParcelableKeyGenParameterSpec} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v851, resolved type: android.security.keystore.ParcelableKeyGenParameterSpec} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v854, resolved type: android.security.keystore.ParcelableKeyGenParameterSpec} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v855, resolved type: android.security.keystore.ParcelableKeyGenParameterSpec} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v858, resolved type: android.security.keystore.ParcelableKeyGenParameterSpec} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v859, resolved type: android.security.keystore.ParcelableKeyGenParameterSpec} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v862, resolved type: android.security.keystore.ParcelableKeyGenParameterSpec} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v863, resolved type: android.security.keystore.ParcelableKeyGenParameterSpec} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v866, resolved type: android.security.keystore.ParcelableKeyGenParameterSpec} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v909, resolved type: android.os.RemoteCallback} */
        /* JADX WARNING: type inference failed for: r0v235, types: [java.lang.CharSequence] */
        /* JADX WARNING: type inference failed for: r0v262 */
        /* JADX WARNING: type inference failed for: r0v281 */
        /* JADX WARNING: type inference failed for: r0v288, types: [android.net.Uri] */
        /* JADX WARNING: type inference failed for: r0v362, types: [android.content.IntentFilter] */
        /* JADX WARNING: type inference failed for: r0v414 */
        /* JADX WARNING: type inference failed for: r0v448, types: [android.content.Intent] */
        /* JADX WARNING: type inference failed for: r0v535 */
        /* JADX WARNING: type inference failed for: r0v620 */
        /* JADX WARNING: type inference failed for: r0v656, types: [java.lang.CharSequence] */
        /* JADX WARNING: type inference failed for: r0v664, types: [java.lang.CharSequence] */
        /* JADX WARNING: type inference failed for: r0v691, types: [java.lang.CharSequence] */
        /* JADX WARNING: type inference failed for: r0v710, types: [android.content.ComponentName] */
        /* JADX WARNING: type inference failed for: r0v868 */
        /* JADX WARNING: type inference failed for: r0v869 */
        /* JADX WARNING: type inference failed for: r0v870 */
        /* JADX WARNING: type inference failed for: r0v871 */
        /* JADX WARNING: type inference failed for: r0v872 */
        /* JADX WARNING: type inference failed for: r0v873 */
        /* JADX WARNING: type inference failed for: r0v874 */
        /* JADX WARNING: type inference failed for: r0v875 */
        /* JADX WARNING: type inference failed for: r0v876 */
        /* JADX WARNING: type inference failed for: r0v877 */
        /* JADX WARNING: type inference failed for: r0v878 */
        /* JADX WARNING: type inference failed for: r0v879 */
        /* JADX WARNING: type inference failed for: r0v880 */
        /* JADX WARNING: type inference failed for: r0v881 */
        /* JADX WARNING: type inference failed for: r0v882 */
        /* JADX WARNING: type inference failed for: r0v883 */
        /* JADX WARNING: type inference failed for: r0v884 */
        /* JADX WARNING: type inference failed for: r0v885 */
        /* JADX WARNING: type inference failed for: r0v886 */
        /* JADX WARNING: type inference failed for: r0v887 */
        /* JADX WARNING: type inference failed for: r0v888 */
        /* JADX WARNING: type inference failed for: r0v889 */
        /* JADX WARNING: type inference failed for: r0v890 */
        /* JADX WARNING: type inference failed for: r0v891 */
        /* JADX WARNING: type inference failed for: r0v892 */
        /* JADX WARNING: type inference failed for: r0v893 */
        /* JADX WARNING: type inference failed for: r0v894 */
        /* JADX WARNING: type inference failed for: r0v895 */
        /* JADX WARNING: type inference failed for: r0v896 */
        /* JADX WARNING: type inference failed for: r0v897 */
        /* JADX WARNING: type inference failed for: r0v898 */
        /* JADX WARNING: type inference failed for: r0v899 */
        /* JADX WARNING: type inference failed for: r0v900 */
        /* JADX WARNING: type inference failed for: r0v901 */
        /* JADX WARNING: type inference failed for: r0v902 */
        /* JADX WARNING: type inference failed for: r0v903 */
        /* JADX WARNING: type inference failed for: r0v904 */
        /* JADX WARNING: type inference failed for: r0v905 */
        /* JADX WARNING: type inference failed for: r0v906 */
        /* JADX WARNING: type inference failed for: r0v907 */
        /* JADX WARNING: type inference failed for: r0v908 */
        /* JADX WARNING: type inference failed for: r0v910 */
        /* JADX WARNING: type inference failed for: r0v911 */
        /* JADX WARNING: type inference failed for: r0v912 */
        /* JADX WARNING: type inference failed for: r0v913 */
        /* JADX WARNING: type inference failed for: r0v914 */
        /* JADX WARNING: type inference failed for: r0v915 */
        /* JADX WARNING: type inference failed for: r0v916 */
        /* JADX WARNING: type inference failed for: r0v917 */
        /* JADX WARNING: type inference failed for: r0v918 */
        /* JADX WARNING: type inference failed for: r0v919 */
        /* JADX WARNING: type inference failed for: r0v920 */
        /* JADX WARNING: type inference failed for: r0v921 */
        /* JADX WARNING: type inference failed for: r0v922 */
        /* JADX WARNING: type inference failed for: r0v923 */
        /* JADX WARNING: type inference failed for: r0v924 */
        /* JADX WARNING: type inference failed for: r0v925 */
        /* JADX WARNING: type inference failed for: r0v926 */
        /* JADX WARNING: type inference failed for: r0v927 */
        /* JADX WARNING: type inference failed for: r0v928 */
        /* JADX WARNING: type inference failed for: r0v929 */
        /* JADX WARNING: type inference failed for: r0v930 */
        /* JADX WARNING: type inference failed for: r0v931 */
        /* JADX WARNING: type inference failed for: r0v932 */
        /* JADX WARNING: type inference failed for: r0v933 */
        /* JADX WARNING: type inference failed for: r0v934 */
        /* JADX WARNING: type inference failed for: r0v935 */
        /* JADX WARNING: type inference failed for: r0v936 */
        /* JADX WARNING: type inference failed for: r0v937 */
        /* JADX WARNING: type inference failed for: r0v938 */
        /* JADX WARNING: type inference failed for: r0v939 */
        /* JADX WARNING: type inference failed for: r0v940 */
        /* JADX WARNING: type inference failed for: r0v941 */
        /* JADX WARNING: type inference failed for: r0v942 */
        /* JADX WARNING: type inference failed for: r0v943 */
        /* JADX WARNING: type inference failed for: r0v944 */
        /* JADX WARNING: type inference failed for: r0v945 */
        /* JADX WARNING: type inference failed for: r0v946 */
        /* JADX WARNING: type inference failed for: r0v947 */
        /* JADX WARNING: type inference failed for: r0v948 */
        /* JADX WARNING: type inference failed for: r0v949 */
        /* JADX WARNING: type inference failed for: r0v950 */
        /* JADX WARNING: type inference failed for: r0v951 */
        /* JADX WARNING: type inference failed for: r0v952 */
        /* JADX WARNING: type inference failed for: r0v953 */
        /* JADX WARNING: type inference failed for: r0v954 */
        /* JADX WARNING: type inference failed for: r0v955 */
        /* JADX WARNING: type inference failed for: r0v956 */
        /* JADX WARNING: type inference failed for: r0v957 */
        /* JADX WARNING: type inference failed for: r0v958 */
        /* JADX WARNING: type inference failed for: r0v959 */
        /* JADX WARNING: type inference failed for: r0v960 */
        /* JADX WARNING: type inference failed for: r0v961 */
        /* JADX WARNING: type inference failed for: r0v962 */
        /* JADX WARNING: type inference failed for: r0v963 */
        /* JADX WARNING: type inference failed for: r0v964 */
        /* JADX WARNING: type inference failed for: r0v965 */
        /* JADX WARNING: type inference failed for: r0v966 */
        /* JADX WARNING: type inference failed for: r0v967 */
        /* JADX WARNING: type inference failed for: r0v968 */
        /* JADX WARNING: type inference failed for: r0v969 */
        /* JADX WARNING: type inference failed for: r0v970 */
        /* JADX WARNING: type inference failed for: r0v971 */
        /* JADX WARNING: type inference failed for: r0v972 */
        /* JADX WARNING: type inference failed for: r0v973 */
        /* JADX WARNING: type inference failed for: r0v974 */
        /* JADX WARNING: type inference failed for: r0v975 */
        /* JADX WARNING: type inference failed for: r0v976 */
        /* JADX WARNING: type inference failed for: r0v977 */
        /* JADX WARNING: type inference failed for: r0v978 */
        /* JADX WARNING: type inference failed for: r0v979 */
        /* JADX WARNING: type inference failed for: r0v980 */
        /* JADX WARNING: type inference failed for: r0v981 */
        /* JADX WARNING: type inference failed for: r0v982 */
        /* JADX WARNING: type inference failed for: r0v983 */
        /* JADX WARNING: type inference failed for: r0v984 */
        /* JADX WARNING: type inference failed for: r0v985 */
        /* JADX WARNING: type inference failed for: r0v986 */
        /* JADX WARNING: type inference failed for: r0v987 */
        /* JADX WARNING: type inference failed for: r0v988 */
        /* JADX WARNING: type inference failed for: r0v989 */
        /* JADX WARNING: type inference failed for: r0v990 */
        /* JADX WARNING: type inference failed for: r0v991 */
        /* JADX WARNING: type inference failed for: r0v992 */
        /* JADX WARNING: type inference failed for: r0v993 */
        /* JADX WARNING: type inference failed for: r0v994 */
        /* JADX WARNING: type inference failed for: r0v995 */
        /* JADX WARNING: type inference failed for: r0v996 */
        /* JADX WARNING: type inference failed for: r0v997 */
        /* JADX WARNING: type inference failed for: r0v998 */
        /* JADX WARNING: type inference failed for: r0v999 */
        /* JADX WARNING: type inference failed for: r0v1000 */
        /* JADX WARNING: type inference failed for: r0v1001 */
        /* JADX WARNING: type inference failed for: r0v1002 */
        /* JADX WARNING: type inference failed for: r0v1003 */
        /* JADX WARNING: type inference failed for: r0v1004 */
        /* JADX WARNING: type inference failed for: r0v1005 */
        /* JADX WARNING: type inference failed for: r0v1006 */
        /* JADX WARNING: type inference failed for: r0v1007 */
        /* JADX WARNING: type inference failed for: r0v1008 */
        /* JADX WARNING: type inference failed for: r0v1009 */
        /* JADX WARNING: type inference failed for: r0v1010 */
        /* JADX WARNING: type inference failed for: r0v1011 */
        /* JADX WARNING: type inference failed for: r0v1012 */
        /* JADX WARNING: type inference failed for: r0v1013 */
        /* JADX WARNING: type inference failed for: r0v1014 */
        /* JADX WARNING: type inference failed for: r0v1015 */
        /* JADX WARNING: type inference failed for: r0v1016 */
        /* JADX WARNING: type inference failed for: r0v1017 */
        /* JADX WARNING: type inference failed for: r0v1018 */
        /* JADX WARNING: type inference failed for: r0v1019 */
        /* JADX WARNING: type inference failed for: r0v1020 */
        /* JADX WARNING: type inference failed for: r0v1021 */
        /* JADX WARNING: type inference failed for: r0v1022 */
        /* JADX WARNING: type inference failed for: r0v1023 */
        /* JADX WARNING: type inference failed for: r0v1024 */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ComponentName _arg0;
            ComponentName _arg02;
            ComponentName _arg03;
            ComponentName _arg04;
            ComponentName _arg05;
            IntentFilter _arg1;
            ComponentName _arg06;
            ComponentName _arg07;
            ComponentName _arg08;
            ComponentName _arg09;
            ComponentName _arg2;
            ComponentName _arg010;
            ComponentName _arg011;
            ComponentName _arg012;
            ComponentName _arg013;
            ComponentName _arg014;
            ComponentName _arg015;
            ComponentName _arg12;
            ComponentName _arg016;
            ComponentName _arg017;
            ComponentName _arg018;
            ComponentName _arg019;
            ComponentName _arg020;
            ComponentName _arg021;
            ComponentName _arg022;
            ComponentName _arg023;
            ComponentName _arg024;
            ComponentName _arg13;
            ComponentName _arg025;
            ComponentName _arg026;
            ComponentName _arg027;
            ComponentName _arg028;
            int i = code;
            Parcel parcel = data;
            Parcel parcel2 = reply;
            if (i != 1598968902) {
                boolean _arg5 = false;
                ParcelableKeyGenParameterSpec _arg14 = 0;
                switch (i) {
                    case 1:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        int _arg15 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg5 = true;
                        }
                        setPasswordQuality(_arg14, _arg15, _arg5);
                        reply.writeNoException();
                        return true;
                    case 2:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        int _arg16 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg5 = true;
                        }
                        int _result = getPasswordQuality(_arg14, _arg16, _arg5);
                        reply.writeNoException();
                        parcel2.writeInt(_result);
                        return true;
                    case 3:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        int _arg17 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg5 = true;
                        }
                        setPasswordMinimumLength(_arg14, _arg17, _arg5);
                        reply.writeNoException();
                        return true;
                    case 4:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        int _arg18 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg5 = true;
                        }
                        int _result2 = getPasswordMinimumLength(_arg14, _arg18, _arg5);
                        reply.writeNoException();
                        parcel2.writeInt(_result2);
                        return true;
                    case 5:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        int _arg19 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg5 = true;
                        }
                        setPasswordMinimumUpperCase(_arg14, _arg19, _arg5);
                        reply.writeNoException();
                        return true;
                    case 6:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        int _arg110 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg5 = true;
                        }
                        int _result3 = getPasswordMinimumUpperCase(_arg14, _arg110, _arg5);
                        reply.writeNoException();
                        parcel2.writeInt(_result3);
                        return true;
                    case 7:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        int _arg111 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg5 = true;
                        }
                        setPasswordMinimumLowerCase(_arg14, _arg111, _arg5);
                        reply.writeNoException();
                        return true;
                    case 8:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        int _arg112 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg5 = true;
                        }
                        int _result4 = getPasswordMinimumLowerCase(_arg14, _arg112, _arg5);
                        reply.writeNoException();
                        parcel2.writeInt(_result4);
                        return true;
                    case 9:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        int _arg113 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg5 = true;
                        }
                        setPasswordMinimumLetters(_arg14, _arg113, _arg5);
                        reply.writeNoException();
                        return true;
                    case 10:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        int _arg114 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg5 = true;
                        }
                        int _result5 = getPasswordMinimumLetters(_arg14, _arg114, _arg5);
                        reply.writeNoException();
                        parcel2.writeInt(_result5);
                        return true;
                    case 11:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        int _arg115 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg5 = true;
                        }
                        setPasswordMinimumNumeric(_arg14, _arg115, _arg5);
                        reply.writeNoException();
                        return true;
                    case 12:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        int _arg116 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg5 = true;
                        }
                        int _result6 = getPasswordMinimumNumeric(_arg14, _arg116, _arg5);
                        reply.writeNoException();
                        parcel2.writeInt(_result6);
                        return true;
                    case 13:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        int _arg117 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg5 = true;
                        }
                        setPasswordMinimumSymbols(_arg14, _arg117, _arg5);
                        reply.writeNoException();
                        return true;
                    case 14:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        int _arg118 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg5 = true;
                        }
                        int _result7 = getPasswordMinimumSymbols(_arg14, _arg118, _arg5);
                        reply.writeNoException();
                        parcel2.writeInt(_result7);
                        return true;
                    case 15:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        int _arg119 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg5 = true;
                        }
                        setPasswordMinimumNonLetter(_arg14, _arg119, _arg5);
                        reply.writeNoException();
                        return true;
                    case 16:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        int _arg120 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg5 = true;
                        }
                        int _result8 = getPasswordMinimumNonLetter(_arg14, _arg120, _arg5);
                        reply.writeNoException();
                        parcel2.writeInt(_result8);
                        return true;
                    case 17:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        int _arg121 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg5 = true;
                        }
                        setPasswordHistoryLength(_arg14, _arg121, _arg5);
                        reply.writeNoException();
                        return true;
                    case 18:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        int _arg122 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg5 = true;
                        }
                        int _result9 = getPasswordHistoryLength(_arg14, _arg122, _arg5);
                        reply.writeNoException();
                        parcel2.writeInt(_result9);
                        return true;
                    case 19:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        long _arg123 = data.readLong();
                        if (data.readInt() != 0) {
                            _arg5 = true;
                        }
                        setPasswordExpirationTimeout(_arg14, _arg123, _arg5);
                        reply.writeNoException();
                        return true;
                    case 20:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        int _arg124 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg5 = true;
                        }
                        long _result10 = getPasswordExpirationTimeout(_arg14, _arg124, _arg5);
                        reply.writeNoException();
                        parcel2.writeLong(_result10);
                        return true;
                    case 21:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        int _arg125 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg5 = true;
                        }
                        long _result11 = getPasswordExpiration(_arg14, _arg125, _arg5);
                        reply.writeNoException();
                        parcel2.writeLong(_result11);
                        return true;
                    case 22:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg029 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg5 = true;
                        }
                        boolean _result12 = isActivePasswordSufficient(_arg029, _arg5);
                        reply.writeNoException();
                        parcel2.writeInt(_result12);
                        return true;
                    case 23:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result13 = isProfileActivePasswordSufficientForParent(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result13);
                        return true;
                    case 24:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result14 = isUsingUnifiedPassword(_arg14);
                        reply.writeNoException();
                        parcel2.writeInt(_result14);
                        return true;
                    case 25:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg030 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg5 = true;
                        }
                        int _result15 = getCurrentFailedPasswordAttempts(_arg030, _arg5);
                        reply.writeNoException();
                        parcel2.writeInt(_result15);
                        return true;
                    case 26:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg031 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg5 = true;
                        }
                        int _result16 = getProfileWithMinimumFailedPasswordsForWipe(_arg031, _arg5);
                        reply.writeNoException();
                        parcel2.writeInt(_result16);
                        return true;
                    case 27:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        int _arg126 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg5 = true;
                        }
                        setMaximumFailedPasswordsForWipe(_arg14, _arg126, _arg5);
                        reply.writeNoException();
                        return true;
                    case 28:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        int _arg127 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg5 = true;
                        }
                        int _result17 = getMaximumFailedPasswordsForWipe(_arg14, _arg127, _arg5);
                        reply.writeNoException();
                        parcel2.writeInt(_result17);
                        return true;
                    case 29:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result18 = resetPassword(data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result18);
                        return true;
                    case 30:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        long _arg128 = data.readLong();
                        if (data.readInt() != 0) {
                            _arg5 = true;
                        }
                        setMaximumTimeToLock(_arg14, _arg128, _arg5);
                        reply.writeNoException();
                        return true;
                    case 31:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        int _arg129 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg5 = true;
                        }
                        long _result19 = getMaximumTimeToLock(_arg14, _arg129, _arg5);
                        reply.writeNoException();
                        parcel2.writeLong(_result19);
                        return true;
                    case 32:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        long _arg130 = data.readLong();
                        if (data.readInt() != 0) {
                            _arg5 = true;
                        }
                        setRequiredStrongAuthTimeout(_arg14, _arg130, _arg5);
                        reply.writeNoException();
                        return true;
                    case 33:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        int _arg131 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg5 = true;
                        }
                        long _result20 = getRequiredStrongAuthTimeout(_arg14, _arg131, _arg5);
                        reply.writeNoException();
                        parcel2.writeLong(_result20);
                        return true;
                    case 34:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg032 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg5 = true;
                        }
                        lockNow(_arg032, _arg5);
                        reply.writeNoException();
                        return true;
                    case 35:
                        parcel.enforceInterface(DESCRIPTOR);
                        wipeDataWithReason(data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 36:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        ComponentName _result21 = setGlobalProxy(_arg14, data.readString(), data.readString());
                        reply.writeNoException();
                        if (_result21 != null) {
                            parcel2.writeInt(1);
                            _result21.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 37:
                        parcel.enforceInterface(DESCRIPTOR);
                        ComponentName _result22 = getGlobalProxyAdmin(data.readInt());
                        reply.writeNoException();
                        if (_result22 != null) {
                            parcel2.writeInt(1);
                            _result22.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 38:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = ComponentName.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg0 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg14 = ProxyInfo.CREATOR.createFromParcel(parcel);
                        }
                        setRecommendedGlobalProxy(_arg0, _arg14);
                        reply.writeNoException();
                        return true;
                    case 39:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        if (data.readInt() != 0) {
                            _arg5 = true;
                        }
                        int _result23 = setStorageEncryption(_arg14, _arg5);
                        reply.writeNoException();
                        parcel2.writeInt(_result23);
                        return true;
                    case 40:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result24 = getStorageEncryption(_arg14, data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result24);
                        return true;
                    case 41:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result25 = getStorageEncryptionStatus(data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result25);
                        return true;
                    case 42:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result26 = requestBugreport(_arg14);
                        reply.writeNoException();
                        parcel2.writeInt(_result26);
                        return true;
                    case 43:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        if (data.readInt() != 0) {
                            _arg5 = true;
                        }
                        setCameraDisabled(_arg14, _arg5);
                        reply.writeNoException();
                        return true;
                    case 44:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result27 = getCameraDisabled(_arg14, data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result27);
                        return true;
                    case 45:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        if (data.readInt() != 0) {
                            _arg5 = true;
                        }
                        setScreenCaptureDisabled(_arg14, _arg5);
                        reply.writeNoException();
                        return true;
                    case 46:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result28 = getScreenCaptureDisabled(_arg14, data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result28);
                        return true;
                    case 47:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        int _arg132 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg5 = true;
                        }
                        setKeyguardDisabledFeatures(_arg14, _arg132, _arg5);
                        reply.writeNoException();
                        return true;
                    case 48:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        int _arg133 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg5 = true;
                        }
                        int _result29 = getKeyguardDisabledFeatures(_arg14, _arg133, _arg5);
                        reply.writeNoException();
                        parcel2.writeInt(_result29);
                        return true;
                    case 49:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        if (data.readInt() != 0) {
                            _arg5 = true;
                        }
                        setActiveAdmin(_arg14, _arg5, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 50:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result30 = isAdminActive(_arg14, data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result30);
                        return true;
                    case 51:
                        parcel.enforceInterface(DESCRIPTOR);
                        List<ComponentName> _result31 = getActiveAdmins(data.readInt());
                        reply.writeNoException();
                        parcel2.writeTypedList(_result31);
                        return true;
                    case 52:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result32 = packageHasActiveAdmins(data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result32);
                        return true;
                    case 53:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = ComponentName.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg02 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg14 = (RemoteCallback) RemoteCallback.CREATOR.createFromParcel(parcel);
                        }
                        getRemoveWarning(_arg02, _arg14, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 54:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        removeActiveAdmin(_arg14, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 55:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        forceRemoveActiveAdmin(_arg14, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 56:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result33 = hasGrantedPolicy(_arg14, data.readInt(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result33);
                        return true;
                    case 57:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = PasswordMetrics.CREATOR.createFromParcel(parcel);
                        }
                        setActivePasswordState(_arg14, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 58:
                        parcel.enforceInterface(DESCRIPTOR);
                        reportPasswordChanged(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 59:
                        parcel.enforceInterface(DESCRIPTOR);
                        reportFailedPasswordAttempt(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 60:
                        parcel.enforceInterface(DESCRIPTOR);
                        reportSuccessfulPasswordAttempt(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 61:
                        parcel.enforceInterface(DESCRIPTOR);
                        reportFailedFingerprintAttempt(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 62:
                        parcel.enforceInterface(DESCRIPTOR);
                        reportSuccessfulFingerprintAttempt(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 63:
                        parcel.enforceInterface(DESCRIPTOR);
                        reportKeyguardDismissed(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 64:
                        parcel.enforceInterface(DESCRIPTOR);
                        reportKeyguardSecured(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 65:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result34 = setDeviceOwner(_arg14, data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result34);
                        return true;
                    case 66:
                        parcel.enforceInterface(DESCRIPTOR);
                        ComponentName _result35 = getDeviceOwnerComponent(data.readInt() != 0);
                        reply.writeNoException();
                        if (_result35 != null) {
                            parcel2.writeInt(1);
                            _result35.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 67:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result36 = hasDeviceOwner();
                        reply.writeNoException();
                        parcel2.writeInt(_result36);
                        return true;
                    case 68:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result37 = getDeviceOwnerName();
                        reply.writeNoException();
                        parcel2.writeString(_result37);
                        return true;
                    case 69:
                        parcel.enforceInterface(DESCRIPTOR);
                        clearDeviceOwner(data.readString());
                        reply.writeNoException();
                        return true;
                    case 70:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result38 = getDeviceOwnerUserId();
                        reply.writeNoException();
                        parcel2.writeInt(_result38);
                        return true;
                    case 71:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result39 = setProfileOwner(_arg14, data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result39);
                        return true;
                    case 72:
                        parcel.enforceInterface(DESCRIPTOR);
                        ComponentName _result40 = getProfileOwner(data.readInt());
                        reply.writeNoException();
                        if (_result40 != null) {
                            parcel2.writeInt(1);
                            _result40.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 73:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result41 = getProfileOwnerName(data.readInt());
                        reply.writeNoException();
                        parcel2.writeString(_result41);
                        return true;
                    case 74:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        setProfileEnabled(_arg14);
                        reply.writeNoException();
                        return true;
                    case 75:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        setProfileName(_arg14, data.readString());
                        reply.writeNoException();
                        return true;
                    case 76:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        clearProfileOwner(_arg14);
                        reply.writeNoException();
                        return true;
                    case 77:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result42 = hasUserSetupCompleted();
                        reply.writeNoException();
                        parcel2.writeInt(_result42);
                        return true;
                    case 78:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = ComponentName.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg03 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg14 = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel);
                        }
                        setDeviceOwnerLockScreenInfo(_arg03, _arg14);
                        reply.writeNoException();
                        return true;
                    case 79:
                        parcel.enforceInterface(DESCRIPTOR);
                        CharSequence _result43 = getDeviceOwnerLockScreenInfo();
                        reply.writeNoException();
                        if (_result43 != null) {
                            parcel2.writeInt(1);
                            TextUtils.writeToParcel(_result43, parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 80:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        String _arg134 = data.readString();
                        String[] _arg22 = data.createStringArray();
                        if (data.readInt() != 0) {
                            _arg5 = true;
                        }
                        String[] _result44 = setPackagesSuspended(_arg14, _arg134, _arg22, _arg5);
                        reply.writeNoException();
                        parcel2.writeStringArray(_result44);
                        return true;
                    case 81:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result45 = isPackageSuspended(_arg14, data.readString(), data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result45);
                        return true;
                    case 82:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result46 = installCaCert(_arg14, data.readString(), data.createByteArray());
                        reply.writeNoException();
                        parcel2.writeInt(_result46);
                        return true;
                    case 83:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        uninstallCaCerts(_arg14, data.readString(), data.createStringArray());
                        reply.writeNoException();
                        return true;
                    case 84:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        enforceCanManageCaCerts(_arg14, data.readString());
                        reply.writeNoException();
                        return true;
                    case 85:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg033 = data.readString();
                        int _arg135 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg5 = true;
                        }
                        boolean _result47 = approveCaCert(_arg033, _arg135, _arg5);
                        reply.writeNoException();
                        parcel2.writeInt(_result47);
                        return true;
                    case 86:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result48 = isCaCertApproved(data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result48);
                        return true;
                    case 87:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result49 = installKeyPair(_arg14, data.readString(), data.createByteArray(), data.createByteArray(), data.createByteArray(), data.readString(), data.readInt() != 0, data.readInt() != 0);
                        reply.writeNoException();
                        parcel2.writeInt(_result49);
                        return true;
                    case 88:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result50 = removeKeyPair(_arg14, data.readString(), data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result50);
                        return true;
                    case 89:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = ComponentName.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg04 = null;
                        }
                        String _arg136 = data.readString();
                        String _arg23 = data.readString();
                        if (data.readInt() != 0) {
                            _arg14 = (ParcelableKeyGenParameterSpec) ParcelableKeyGenParameterSpec.CREATOR.createFromParcel(parcel);
                        }
                        ParcelableKeyGenParameterSpec _arg3 = _arg14;
                        int _arg4 = data.readInt();
                        KeymasterCertificateChain _arg52 = new KeymasterCertificateChain();
                        boolean _result51 = generateKeyPair(_arg04, _arg136, _arg23, _arg3, _arg4, _arg52);
                        reply.writeNoException();
                        parcel2.writeInt(_result51);
                        parcel2.writeInt(1);
                        _arg52.writeToParcel(parcel2, 1);
                        return true;
                    case 90:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        ComponentName _arg034 = _arg14;
                        String _arg137 = data.readString();
                        String _arg24 = data.readString();
                        byte[] _arg32 = data.createByteArray();
                        byte[] _arg42 = data.createByteArray();
                        if (data.readInt() != 0) {
                            _arg5 = true;
                        }
                        boolean _result52 = setKeyPairCertificate(_arg034, _arg137, _arg24, _arg32, _arg42, _arg5);
                        reply.writeNoException();
                        parcel2.writeInt(_result52);
                        return true;
                    case 91:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg035 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg14 = Uri.CREATOR.createFromParcel(parcel);
                        }
                        choosePrivateKeyAlias(_arg035, _arg14, data.readString(), data.readStrongBinder());
                        reply.writeNoException();
                        return true;
                    case 92:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        setDelegatedScopes(_arg14, data.readString(), data.createStringArrayList());
                        reply.writeNoException();
                        return true;
                    case 93:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        List<String> _result53 = getDelegatedScopes(_arg14, data.readString());
                        reply.writeNoException();
                        parcel2.writeStringList(_result53);
                        return true;
                    case 94:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        List<String> _result54 = getDelegatePackages(_arg14, data.readString());
                        reply.writeNoException();
                        parcel2.writeStringList(_result54);
                        return true;
                    case 95:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        setCertInstallerPackage(_arg14, data.readString());
                        reply.writeNoException();
                        return true;
                    case 96:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        String _result55 = getCertInstallerPackage(_arg14);
                        reply.writeNoException();
                        parcel2.writeString(_result55);
                        return true;
                    case 97:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        String _arg138 = data.readString();
                        if (data.readInt() != 0) {
                            _arg5 = true;
                        }
                        boolean _result56 = setAlwaysOnVpnPackage(_arg14, _arg138, _arg5);
                        reply.writeNoException();
                        parcel2.writeInt(_result56);
                        return true;
                    case 98:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        String _result57 = getAlwaysOnVpnPackage(_arg14);
                        reply.writeNoException();
                        parcel2.writeString(_result57);
                        return true;
                    case 99:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg05 = ComponentName.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg05 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg1 = IntentFilter.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg1 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        addPersistentPreferredActivity(_arg05, _arg1, _arg14);
                        reply.writeNoException();
                        return true;
                    case 100:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        clearPackagePersistentPreferredActivities(_arg14, data.readString());
                        reply.writeNoException();
                        return true;
                    case 101:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        setDefaultSmsApplication(_arg14, data.readString());
                        reply.writeNoException();
                        return true;
                    case 102:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg06 = ComponentName.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg06 = null;
                        }
                        String _arg139 = data.readString();
                        String _arg25 = data.readString();
                        if (data.readInt() != 0) {
                            _arg14 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        setApplicationRestrictions(_arg06, _arg139, _arg25, _arg14);
                        reply.writeNoException();
                        return true;
                    case 103:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        Bundle _result58 = getApplicationRestrictions(_arg14, data.readString(), data.readString());
                        reply.writeNoException();
                        if (_result58 != null) {
                            parcel2.writeInt(1);
                            _result58.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 104:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result59 = setApplicationRestrictionsManagingPackage(_arg14, data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result59);
                        return true;
                    case 105:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        String _result60 = getApplicationRestrictionsManagingPackage(_arg14);
                        reply.writeNoException();
                        parcel2.writeString(_result60);
                        return true;
                    case 106:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result61 = isCallerApplicationRestrictionsManagingPackage(data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result61);
                        return true;
                    case 107:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg07 = ComponentName.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg07 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        setRestrictionsProvider(_arg07, _arg14);
                        reply.writeNoException();
                        return true;
                    case 108:
                        parcel.enforceInterface(DESCRIPTOR);
                        ComponentName _result62 = getRestrictionsProvider(data.readInt());
                        reply.writeNoException();
                        if (_result62 != null) {
                            parcel2.writeInt(1);
                            _result62.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 109:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        String _arg140 = data.readString();
                        if (data.readInt() != 0) {
                            _arg5 = true;
                        }
                        setUserRestriction(_arg14, _arg140, _arg5);
                        reply.writeNoException();
                        return true;
                    case 110:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        Bundle _result63 = getUserRestrictions(_arg14);
                        reply.writeNoException();
                        if (_result63 != null) {
                            parcel2.writeInt(1);
                            _result63.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 111:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg08 = ComponentName.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg08 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg14 = IntentFilter.CREATOR.createFromParcel(parcel);
                        }
                        addCrossProfileIntentFilter(_arg08, _arg14, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 112:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        clearCrossProfileIntentFilters(_arg14);
                        reply.writeNoException();
                        return true;
                    case 113:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result64 = setPermittedAccessibilityServices(_arg14, parcel.readArrayList(getClass().getClassLoader()));
                        reply.writeNoException();
                        parcel2.writeInt(_result64);
                        return true;
                    case 114:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        List _result65 = getPermittedAccessibilityServices(_arg14);
                        reply.writeNoException();
                        parcel2.writeList(_result65);
                        return true;
                    case 115:
                        parcel.enforceInterface(DESCRIPTOR);
                        List _result66 = getPermittedAccessibilityServicesForUser(data.readInt());
                        reply.writeNoException();
                        parcel2.writeList(_result66);
                        return true;
                    case 116:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result67 = isAccessibilityServicePermittedByAdmin(_arg14, data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result67);
                        return true;
                    case 117:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result68 = setPermittedInputMethods(_arg14, parcel.readArrayList(getClass().getClassLoader()));
                        reply.writeNoException();
                        parcel2.writeInt(_result68);
                        return true;
                    case 118:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        List _result69 = getPermittedInputMethods(_arg14);
                        reply.writeNoException();
                        parcel2.writeList(_result69);
                        return true;
                    case 119:
                        parcel.enforceInterface(DESCRIPTOR);
                        List _result70 = getPermittedInputMethodsForCurrentUser();
                        reply.writeNoException();
                        parcel2.writeList(_result70);
                        return true;
                    case 120:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result71 = isInputMethodPermittedByAdmin(_arg14, data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result71);
                        return true;
                    case 121:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result72 = setPermittedCrossProfileNotificationListeners(_arg14, data.createStringArrayList());
                        reply.writeNoException();
                        parcel2.writeInt(_result72);
                        return true;
                    case 122:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        List<String> _result73 = getPermittedCrossProfileNotificationListeners(_arg14);
                        reply.writeNoException();
                        parcel2.writeStringList(_result73);
                        return true;
                    case 123:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result74 = isNotificationListenerServicePermitted(data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result74);
                        return true;
                    case 124:
                        parcel.enforceInterface(DESCRIPTOR);
                        Intent _result75 = createAdminSupportIntent(data.readString());
                        reply.writeNoException();
                        if (_result75 != null) {
                            parcel2.writeInt(1);
                            _result75.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 125:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        String _arg141 = data.readString();
                        String _arg26 = data.readString();
                        if (data.readInt() != 0) {
                            _arg5 = true;
                        }
                        boolean _result76 = setApplicationHidden(_arg14, _arg141, _arg26, _arg5);
                        reply.writeNoException();
                        parcel2.writeInt(_result76);
                        return true;
                    case 126:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result77 = isApplicationHidden(_arg14, data.readString(), data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result77);
                        return true;
                    case 127:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg09 = ComponentName.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg09 = null;
                        }
                        String _arg142 = data.readString();
                        if (data.readInt() != 0) {
                            _arg2 = ComponentName.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg2 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg14 = (PersistableBundle) PersistableBundle.CREATOR.createFromParcel(parcel);
                        }
                        UserHandle _result78 = createAndManageUser(_arg09, _arg142, _arg2, _arg14, data.readInt());
                        reply.writeNoException();
                        if (_result78 != null) {
                            parcel2.writeInt(1);
                            _result78.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 128:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg010 = ComponentName.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg010 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg14 = (UserHandle) UserHandle.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result79 = removeUser(_arg010, _arg14);
                        reply.writeNoException();
                        parcel2.writeInt(_result79);
                        return true;
                    case 129:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg011 = ComponentName.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg011 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg14 = (UserHandle) UserHandle.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result80 = switchUser(_arg011, _arg14);
                        reply.writeNoException();
                        parcel2.writeInt(_result80);
                        return true;
                    case 130:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg012 = ComponentName.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg012 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg14 = (UserHandle) UserHandle.CREATOR.createFromParcel(parcel);
                        }
                        int _result81 = startUserInBackground(_arg012, _arg14);
                        reply.writeNoException();
                        parcel2.writeInt(_result81);
                        return true;
                    case 131:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg013 = ComponentName.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg013 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg14 = (UserHandle) UserHandle.CREATOR.createFromParcel(parcel);
                        }
                        int _result82 = stopUser(_arg013, _arg14);
                        reply.writeNoException();
                        parcel2.writeInt(_result82);
                        return true;
                    case 132:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        int _result83 = logoutUser(_arg14);
                        reply.writeNoException();
                        parcel2.writeInt(_result83);
                        return true;
                    case 133:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        List<UserHandle> _result84 = getSecondaryUsers(_arg14);
                        reply.writeNoException();
                        parcel2.writeTypedList(_result84);
                        return true;
                    case 134:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        enableSystemApp(_arg14, data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 135:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg014 = ComponentName.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg014 = null;
                        }
                        String _arg143 = data.readString();
                        if (data.readInt() != 0) {
                            _arg14 = Intent.CREATOR.createFromParcel(parcel);
                        }
                        int _result85 = enableSystemAppWithIntent(_arg014, _arg143, _arg14);
                        reply.writeNoException();
                        parcel2.writeInt(_result85);
                        return true;
                    case 136:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result86 = installExistingPackage(_arg14, data.readString(), data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result86);
                        return true;
                    case 137:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        String _arg144 = data.readString();
                        if (data.readInt() != 0) {
                            _arg5 = true;
                        }
                        setAccountManagementDisabled(_arg14, _arg144, _arg5);
                        reply.writeNoException();
                        return true;
                    case 138:
                        parcel.enforceInterface(DESCRIPTOR);
                        String[] _result87 = getAccountTypesWithManagementDisabled();
                        reply.writeNoException();
                        parcel2.writeStringArray(_result87);
                        return true;
                    case 139:
                        parcel.enforceInterface(DESCRIPTOR);
                        String[] _result88 = getAccountTypesWithManagementDisabledAsUser(data.readInt());
                        reply.writeNoException();
                        parcel2.writeStringArray(_result88);
                        return true;
                    case 140:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        setLockTaskPackages(_arg14, data.createStringArray());
                        reply.writeNoException();
                        return true;
                    case 141:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        String[] _result89 = getLockTaskPackages(_arg14);
                        reply.writeNoException();
                        parcel2.writeStringArray(_result89);
                        return true;
                    case 142:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result90 = isLockTaskPermitted(data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result90);
                        return true;
                    case 143:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        setLockTaskFeatures(_arg14, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 144:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        int _result91 = getLockTaskFeatures(_arg14);
                        reply.writeNoException();
                        parcel2.writeInt(_result91);
                        return true;
                    case 145:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        setGlobalSetting(_arg14, data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 146:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        setSystemSetting(_arg14, data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 147:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        setSecureSetting(_arg14, data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 148:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result92 = setTime(_arg14, data.readLong());
                        reply.writeNoException();
                        parcel2.writeInt(_result92);
                        return true;
                    case 149:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result93 = setTimeZone(_arg14, data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result93);
                        return true;
                    case 150:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        if (data.readInt() != 0) {
                            _arg5 = true;
                        }
                        setMasterVolumeMuted(_arg14, _arg5);
                        reply.writeNoException();
                        return true;
                    case 151:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result94 = isMasterVolumeMuted(_arg14);
                        reply.writeNoException();
                        parcel2.writeInt(_result94);
                        return true;
                    case 152:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg5 = true;
                        }
                        notifyLockTaskModeChanged(_arg5, data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 153:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        String _arg145 = data.readString();
                        String _arg27 = data.readString();
                        if (data.readInt() != 0) {
                            _arg5 = true;
                        }
                        setUninstallBlocked(_arg14, _arg145, _arg27, _arg5);
                        reply.writeNoException();
                        return true;
                    case 154:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result95 = isUninstallBlocked(_arg14, data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result95);
                        return true;
                    case 155:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        if (data.readInt() != 0) {
                            _arg5 = true;
                        }
                        setCrossProfileCallerIdDisabled(_arg14, _arg5);
                        reply.writeNoException();
                        return true;
                    case 156:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result96 = getCrossProfileCallerIdDisabled(_arg14);
                        reply.writeNoException();
                        parcel2.writeInt(_result96);
                        return true;
                    case 157:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result97 = getCrossProfileCallerIdDisabledForUser(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result97);
                        return true;
                    case 158:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        if (data.readInt() != 0) {
                            _arg5 = true;
                        }
                        setCrossProfileContactsSearchDisabled(_arg14, _arg5);
                        reply.writeNoException();
                        return true;
                    case 159:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result98 = getCrossProfileContactsSearchDisabled(_arg14);
                        reply.writeNoException();
                        parcel2.writeInt(_result98);
                        return true;
                    case 160:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result99 = getCrossProfileContactsSearchDisabledForUser(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result99);
                        return true;
                    case 161:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg036 = data.readString();
                        long _arg146 = data.readLong();
                        boolean _arg28 = data.readInt() != 0;
                        long _arg33 = data.readLong();
                        if (data.readInt() != 0) {
                            _arg14 = Intent.CREATOR.createFromParcel(parcel);
                        }
                        startManagedQuickContact(_arg036, _arg146, _arg28, _arg33, _arg14);
                        reply.writeNoException();
                        return true;
                    case 162:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        if (data.readInt() != 0) {
                            _arg5 = true;
                        }
                        setBluetoothContactSharingDisabled(_arg14, _arg5);
                        reply.writeNoException();
                        return true;
                    case 163:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result100 = getBluetoothContactSharingDisabled(_arg14);
                        reply.writeNoException();
                        parcel2.writeInt(_result100);
                        return true;
                    case 164:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result101 = getBluetoothContactSharingDisabledForUser(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result101);
                        return true;
                    case 165:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg015 = ComponentName.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg015 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg12 = ComponentName.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg12 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg14 = (PersistableBundle) PersistableBundle.CREATOR.createFromParcel(parcel);
                        }
                        if (data.readInt() != 0) {
                            _arg5 = true;
                        }
                        setTrustAgentConfiguration(_arg015, _arg12, _arg14, _arg5);
                        reply.writeNoException();
                        return true;
                    case 166:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg016 = ComponentName.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg016 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        int _arg29 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg5 = true;
                        }
                        List<PersistableBundle> _result102 = getTrustAgentConfiguration(_arg016, _arg14, _arg29, _arg5);
                        reply.writeNoException();
                        parcel2.writeTypedList(_result102);
                        return true;
                    case 167:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result103 = addCrossProfileWidgetProvider(_arg14, data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result103);
                        return true;
                    case 168:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result104 = removeCrossProfileWidgetProvider(_arg14, data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result104);
                        return true;
                    case 169:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        List<String> _result105 = getCrossProfileWidgetProviders(_arg14);
                        reply.writeNoException();
                        parcel2.writeStringList(_result105);
                        return true;
                    case 170:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        if (data.readInt() != 0) {
                            _arg5 = true;
                        }
                        setAutoTimeRequired(_arg14, _arg5);
                        reply.writeNoException();
                        return true;
                    case 171:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result106 = getAutoTimeRequired();
                        reply.writeNoException();
                        parcel2.writeInt(_result106);
                        return true;
                    case 172:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        if (data.readInt() != 0) {
                            _arg5 = true;
                        }
                        setForceEphemeralUsers(_arg14, _arg5);
                        reply.writeNoException();
                        return true;
                    case 173:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result107 = getForceEphemeralUsers(_arg14);
                        reply.writeNoException();
                        parcel2.writeInt(_result107);
                        return true;
                    case 174:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result108 = isRemovingAdmin(_arg14, data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result108);
                        return true;
                    case 175:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg017 = ComponentName.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg017 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg14 = Bitmap.CREATOR.createFromParcel(parcel);
                        }
                        setUserIcon(_arg017, _arg14);
                        reply.writeNoException();
                        return true;
                    case 176:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg018 = ComponentName.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg018 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg14 = SystemUpdatePolicy.CREATOR.createFromParcel(parcel);
                        }
                        setSystemUpdatePolicy(_arg018, _arg14);
                        reply.writeNoException();
                        return true;
                    case 177:
                        parcel.enforceInterface(DESCRIPTOR);
                        SystemUpdatePolicy _result109 = getSystemUpdatePolicy();
                        reply.writeNoException();
                        if (_result109 != null) {
                            parcel2.writeInt(1);
                            _result109.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 178:
                        parcel.enforceInterface(DESCRIPTOR);
                        clearSystemUpdatePolicyFreezePeriodRecord();
                        reply.writeNoException();
                        return true;
                    case 179:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        if (data.readInt() != 0) {
                            _arg5 = true;
                        }
                        boolean _result110 = setKeyguardDisabled(_arg14, _arg5);
                        reply.writeNoException();
                        parcel2.writeInt(_result110);
                        return true;
                    case 180:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        if (data.readInt() != 0) {
                            _arg5 = true;
                        }
                        boolean _result111 = setStatusBarDisabled(_arg14, _arg5);
                        reply.writeNoException();
                        parcel2.writeInt(_result111);
                        return true;
                    case 181:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result112 = getDoNotAskCredentialsOnBoot();
                        reply.writeNoException();
                        parcel2.writeInt(_result112);
                        return true;
                    case 182:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = SystemUpdateInfo.CREATOR.createFromParcel(parcel);
                        }
                        notifyPendingSystemUpdate(_arg14);
                        reply.writeNoException();
                        return true;
                    case 183:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        SystemUpdateInfo _result113 = getPendingSystemUpdate(_arg14);
                        reply.writeNoException();
                        if (_result113 != null) {
                            parcel2.writeInt(1);
                            _result113.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 184:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        setPermissionPolicy(_arg14, data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 185:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        int _result114 = getPermissionPolicy(_arg14);
                        reply.writeNoException();
                        parcel2.writeInt(_result114);
                        return true;
                    case 186:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result115 = setPermissionGrantState(_arg14, data.readString(), data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result115);
                        return true;
                    case 187:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        int _result116 = getPermissionGrantState(_arg14, data.readString(), data.readString(), data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result116);
                        return true;
                    case 188:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result117 = isProvisioningAllowed(data.readString(), data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result117);
                        return true;
                    case 189:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result118 = checkProvisioningPreCondition(data.readString(), data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result118);
                        return true;
                    case 190:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        setKeepUninstalledPackages(_arg14, data.readString(), data.createStringArrayList());
                        reply.writeNoException();
                        return true;
                    case 191:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        List<String> _result119 = getKeepUninstalledPackages(_arg14, data.readString());
                        reply.writeNoException();
                        parcel2.writeStringList(_result119);
                        return true;
                    case 192:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result120 = isManagedProfile(_arg14);
                        reply.writeNoException();
                        parcel2.writeInt(_result120);
                        return true;
                    case 193:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result121 = isSystemOnlyUser(_arg14);
                        reply.writeNoException();
                        parcel2.writeInt(_result121);
                        return true;
                    case 194:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        String _result122 = getWifiMacAddress(_arg14);
                        reply.writeNoException();
                        parcel2.writeString(_result122);
                        return true;
                    case 195:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        reboot(_arg14);
                        reply.writeNoException();
                        return true;
                    case 196:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg019 = ComponentName.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg019 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg14 = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel);
                        }
                        setShortSupportMessage(_arg019, _arg14);
                        reply.writeNoException();
                        return true;
                    case 197:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        CharSequence _result123 = getShortSupportMessage(_arg14);
                        reply.writeNoException();
                        if (_result123 != null) {
                            parcel2.writeInt(1);
                            TextUtils.writeToParcel(_result123, parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 198:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg020 = ComponentName.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg020 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg14 = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel);
                        }
                        setLongSupportMessage(_arg020, _arg14);
                        reply.writeNoException();
                        return true;
                    case 199:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        CharSequence _result124 = getLongSupportMessage(_arg14);
                        reply.writeNoException();
                        if (_result124 != null) {
                            parcel2.writeInt(1);
                            TextUtils.writeToParcel(_result124, parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 200:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        CharSequence _result125 = getShortSupportMessageForUser(_arg14, data.readInt());
                        reply.writeNoException();
                        if (_result125 != null) {
                            parcel2.writeInt(1);
                            TextUtils.writeToParcel(_result125, parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 201:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        CharSequence _result126 = getLongSupportMessageForUser(_arg14, data.readInt());
                        reply.writeNoException();
                        if (_result126 != null) {
                            parcel2.writeInt(1);
                            TextUtils.writeToParcel(_result126, parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 202:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result127 = isSeparateProfileChallengeAllowed(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result127);
                        return true;
                    case 203:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        setOrganizationColor(_arg14, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 204:
                        parcel.enforceInterface(DESCRIPTOR);
                        setOrganizationColorForUser(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 205:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        int _result128 = getOrganizationColor(_arg14);
                        reply.writeNoException();
                        parcel2.writeInt(_result128);
                        return true;
                    case 206:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result129 = getOrganizationColorForUser(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result129);
                        return true;
                    case 207:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg021 = ComponentName.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg021 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg14 = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel);
                        }
                        setOrganizationName(_arg021, _arg14);
                        reply.writeNoException();
                        return true;
                    case 208:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        CharSequence _result130 = getOrganizationName(_arg14);
                        reply.writeNoException();
                        if (_result130 != null) {
                            parcel2.writeInt(1);
                            TextUtils.writeToParcel(_result130, parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 209:
                        parcel.enforceInterface(DESCRIPTOR);
                        CharSequence _result131 = getDeviceOwnerOrganizationName();
                        reply.writeNoException();
                        if (_result131 != null) {
                            parcel2.writeInt(1);
                            TextUtils.writeToParcel(_result131, parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 210:
                        parcel.enforceInterface(DESCRIPTOR);
                        CharSequence _result132 = getOrganizationNameForUser(data.readInt());
                        reply.writeNoException();
                        if (_result132 != null) {
                            parcel2.writeInt(1);
                            TextUtils.writeToParcel(_result132, parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 211:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result133 = getUserProvisioningState();
                        reply.writeNoException();
                        parcel2.writeInt(_result133);
                        return true;
                    case 212:
                        parcel.enforceInterface(DESCRIPTOR);
                        setUserProvisioningState(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 213:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        setAffiliationIds(_arg14, data.createStringArrayList());
                        reply.writeNoException();
                        return true;
                    case 214:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        List<String> _result134 = getAffiliationIds(_arg14);
                        reply.writeNoException();
                        parcel2.writeStringList(_result134);
                        return true;
                    case 215:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result135 = isAffiliatedUser();
                        reply.writeNoException();
                        parcel2.writeInt(_result135);
                        return true;
                    case 216:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        if (data.readInt() != 0) {
                            _arg5 = true;
                        }
                        setSecurityLoggingEnabled(_arg14, _arg5);
                        reply.writeNoException();
                        return true;
                    case 217:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result136 = isSecurityLoggingEnabled(_arg14);
                        reply.writeNoException();
                        parcel2.writeInt(_result136);
                        return true;
                    case 218:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        ParceledListSlice _result137 = retrieveSecurityLogs(_arg14);
                        reply.writeNoException();
                        if (_result137 != null) {
                            parcel2.writeInt(1);
                            _result137.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 219:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        ParceledListSlice _result138 = retrievePreRebootSecurityLogs(_arg14);
                        reply.writeNoException();
                        if (_result138 != null) {
                            parcel2.writeInt(1);
                            _result138.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 220:
                        parcel.enforceInterface(DESCRIPTOR);
                        long _result139 = forceSecurityLogs();
                        reply.writeNoException();
                        parcel2.writeLong(_result139);
                        return true;
                    case 221:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result140 = isUninstallInQueue(data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result140);
                        return true;
                    case 222:
                        parcel.enforceInterface(DESCRIPTOR);
                        uninstallPackageWithActiveAdmins(data.readString());
                        reply.writeNoException();
                        return true;
                    case 223:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result141 = isDeviceProvisioned();
                        reply.writeNoException();
                        parcel2.writeInt(_result141);
                        return true;
                    case 224:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result142 = isDeviceProvisioningConfigApplied();
                        reply.writeNoException();
                        parcel2.writeInt(_result142);
                        return true;
                    case 225:
                        parcel.enforceInterface(DESCRIPTOR);
                        setDeviceProvisioningConfigApplied();
                        reply.writeNoException();
                        return true;
                    case 226:
                        parcel.enforceInterface(DESCRIPTOR);
                        forceUpdateUserSetupComplete();
                        reply.writeNoException();
                        return true;
                    case 227:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        if (data.readInt() != 0) {
                            _arg5 = true;
                        }
                        setBackupServiceEnabled(_arg14, _arg5);
                        reply.writeNoException();
                        return true;
                    case 228:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result143 = isBackupServiceEnabled(_arg14);
                        reply.writeNoException();
                        parcel2.writeInt(_result143);
                        return true;
                    case 229:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg022 = ComponentName.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg022 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result144 = setMandatoryBackupTransport(_arg022, _arg14);
                        reply.writeNoException();
                        parcel2.writeInt(_result144);
                        return true;
                    case 230:
                        parcel.enforceInterface(DESCRIPTOR);
                        ComponentName _result145 = getMandatoryBackupTransport();
                        reply.writeNoException();
                        if (_result145 != null) {
                            parcel2.writeInt(1);
                            _result145.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 231:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        if (data.readInt() != 0) {
                            _arg5 = true;
                        }
                        setNetworkLoggingEnabled(_arg14, _arg5);
                        reply.writeNoException();
                        return true;
                    case 232:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result146 = isNetworkLoggingEnabled(_arg14);
                        reply.writeNoException();
                        parcel2.writeInt(_result146);
                        return true;
                    case 233:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        List<NetworkEvent> _result147 = retrieveNetworkLogs(_arg14, data.readLong());
                        reply.writeNoException();
                        parcel2.writeTypedList(_result147);
                        return true;
                    case 234:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg023 = ComponentName.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg023 = null;
                        }
                        IApplicationThread _arg147 = IApplicationThread.Stub.asInterface(data.readStrongBinder());
                        IBinder _arg210 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg14 = Intent.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result148 = bindDeviceAdminServiceAsUser(_arg023, _arg147, _arg210, _arg14, IServiceConnection.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result148);
                        return true;
                    case 235:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        List<UserHandle> _result149 = getBindDeviceAdminTargetUsers(_arg14);
                        reply.writeNoException();
                        parcel2.writeTypedList(_result149);
                        return true;
                    case 236:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result150 = isEphemeralUser(_arg14);
                        reply.writeNoException();
                        parcel2.writeInt(_result150);
                        return true;
                    case 237:
                        parcel.enforceInterface(DESCRIPTOR);
                        long _result151 = getLastSecurityLogRetrievalTime();
                        reply.writeNoException();
                        parcel2.writeLong(_result151);
                        return true;
                    case 238:
                        parcel.enforceInterface(DESCRIPTOR);
                        long _result152 = getLastBugReportRequestTime();
                        reply.writeNoException();
                        parcel2.writeLong(_result152);
                        return true;
                    case 239:
                        parcel.enforceInterface(DESCRIPTOR);
                        long _result153 = getLastNetworkLogRetrievalTime();
                        reply.writeNoException();
                        parcel2.writeLong(_result153);
                        return true;
                    case 240:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result154 = setResetPasswordToken(_arg14, data.createByteArray());
                        reply.writeNoException();
                        parcel2.writeInt(_result154);
                        return true;
                    case 241:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result155 = clearResetPasswordToken(_arg14);
                        reply.writeNoException();
                        parcel2.writeInt(_result155);
                        return true;
                    case 242:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result156 = isResetPasswordTokenActive(_arg14);
                        reply.writeNoException();
                        parcel2.writeInt(_result156);
                        return true;
                    case 243:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result157 = resetPasswordWithToken(_arg14, data.readString(), data.createByteArray(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result157);
                        return true;
                    case 244:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result158 = isCurrentInputMethodSetByOwner();
                        reply.writeNoException();
                        parcel2.writeInt(_result158);
                        return true;
                    case 245:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = (UserHandle) UserHandle.CREATOR.createFromParcel(parcel);
                        }
                        StringParceledListSlice _result159 = getOwnerInstalledCaCerts(_arg14);
                        reply.writeNoException();
                        if (_result159 != null) {
                            parcel2.writeInt(1);
                            _result159.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 246:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        clearApplicationUserData(_arg14, data.readString(), IPackageDataObserver.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 247:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        if (data.readInt() != 0) {
                            _arg5 = true;
                        }
                        setLogoutEnabled(_arg14, _arg5);
                        reply.writeNoException();
                        return true;
                    case 248:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result160 = isLogoutEnabled();
                        reply.writeNoException();
                        parcel2.writeInt(_result160);
                        return true;
                    case 249:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        List<String> _result161 = getDisallowedSystemApps(_arg14, data.readInt(), data.readString());
                        reply.writeNoException();
                        parcel2.writeStringList(_result161);
                        return true;
                    case 250:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg024 = ComponentName.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg024 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg13 = ComponentName.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg13 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg14 = (PersistableBundle) PersistableBundle.CREATOR.createFromParcel(parcel);
                        }
                        transferOwnership(_arg024, _arg13, _arg14);
                        reply.writeNoException();
                        return true;
                    case 251:
                        parcel.enforceInterface(DESCRIPTOR);
                        PersistableBundle _result162 = getTransferOwnershipBundle();
                        reply.writeNoException();
                        if (_result162 != null) {
                            parcel2.writeInt(1);
                            _result162.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 252:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg025 = ComponentName.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg025 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg14 = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel);
                        }
                        setStartUserSessionMessage(_arg025, _arg14);
                        reply.writeNoException();
                        return true;
                    case 253:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg026 = ComponentName.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg026 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg14 = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel);
                        }
                        setEndUserSessionMessage(_arg026, _arg14);
                        reply.writeNoException();
                        return true;
                    case 254:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        CharSequence _result163 = getStartUserSessionMessage(_arg14);
                        reply.writeNoException();
                        if (_result163 != null) {
                            parcel2.writeInt(1);
                            TextUtils.writeToParcel(_result163, parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 255:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        CharSequence _result164 = getEndUserSessionMessage(_arg14);
                        reply.writeNoException();
                        if (_result164 != null) {
                            parcel2.writeInt(1);
                            TextUtils.writeToParcel(_result164, parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 256:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        List<String> _result165 = setMeteredDataDisabledPackages(_arg14, data.createStringArrayList());
                        reply.writeNoException();
                        parcel2.writeStringList(_result165);
                        return true;
                    case 257:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        List<String> _result166 = getMeteredDataDisabledPackages(_arg14);
                        reply.writeNoException();
                        parcel2.writeStringList(_result166);
                        return true;
                    case 258:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg027 = ComponentName.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg027 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg14 = (ApnSetting) ApnSetting.CREATOR.createFromParcel(parcel);
                        }
                        int _result167 = addOverrideApn(_arg027, _arg14);
                        reply.writeNoException();
                        parcel2.writeInt(_result167);
                        return true;
                    case 259:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg028 = ComponentName.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg028 = null;
                        }
                        int _arg148 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg14 = (ApnSetting) ApnSetting.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result168 = updateOverrideApn(_arg028, _arg148, _arg14);
                        reply.writeNoException();
                        parcel2.writeInt(_result168);
                        return true;
                    case 260:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result169 = removeOverrideApn(_arg14, data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result169);
                        return true;
                    case 261:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        List<ApnSetting> _result170 = getOverrideApns(_arg14);
                        reply.writeNoException();
                        parcel2.writeTypedList(_result170);
                        return true;
                    case 262:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        if (data.readInt() != 0) {
                            _arg5 = true;
                        }
                        setOverrideApnsEnabled(_arg14, _arg5);
                        reply.writeNoException();
                        return true;
                    case 263:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result171 = isOverrideApnEnabled(_arg14);
                        reply.writeNoException();
                        parcel2.writeInt(_result171);
                        return true;
                    case 264:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result172 = isMeteredDataDisabledPackageForUser(_arg14, data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result172);
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

    void addCrossProfileIntentFilter(ComponentName componentName, IntentFilter intentFilter, int i) throws RemoteException;

    boolean addCrossProfileWidgetProvider(ComponentName componentName, String str) throws RemoteException;

    int addOverrideApn(ComponentName componentName, ApnSetting apnSetting) throws RemoteException;

    void addPersistentPreferredActivity(ComponentName componentName, IntentFilter intentFilter, ComponentName componentName2) throws RemoteException;

    boolean approveCaCert(String str, int i, boolean z) throws RemoteException;

    boolean bindDeviceAdminServiceAsUser(ComponentName componentName, IApplicationThread iApplicationThread, IBinder iBinder, Intent intent, IServiceConnection iServiceConnection, int i, int i2) throws RemoteException;

    int checkProvisioningPreCondition(String str, String str2) throws RemoteException;

    void choosePrivateKeyAlias(int i, Uri uri, String str, IBinder iBinder) throws RemoteException;

    void clearApplicationUserData(ComponentName componentName, String str, IPackageDataObserver iPackageDataObserver) throws RemoteException;

    void clearCrossProfileIntentFilters(ComponentName componentName) throws RemoteException;

    void clearDeviceOwner(String str) throws RemoteException;

    void clearPackagePersistentPreferredActivities(ComponentName componentName, String str) throws RemoteException;

    void clearProfileOwner(ComponentName componentName) throws RemoteException;

    boolean clearResetPasswordToken(ComponentName componentName) throws RemoteException;

    void clearSystemUpdatePolicyFreezePeriodRecord() throws RemoteException;

    Intent createAdminSupportIntent(String str) throws RemoteException;

    UserHandle createAndManageUser(ComponentName componentName, String str, ComponentName componentName2, PersistableBundle persistableBundle, int i) throws RemoteException;

    void enableSystemApp(ComponentName componentName, String str, String str2) throws RemoteException;

    int enableSystemAppWithIntent(ComponentName componentName, String str, Intent intent) throws RemoteException;

    void enforceCanManageCaCerts(ComponentName componentName, String str) throws RemoteException;

    void forceRemoveActiveAdmin(ComponentName componentName, int i) throws RemoteException;

    long forceSecurityLogs() throws RemoteException;

    void forceUpdateUserSetupComplete() throws RemoteException;

    boolean generateKeyPair(ComponentName componentName, String str, String str2, ParcelableKeyGenParameterSpec parcelableKeyGenParameterSpec, int i, KeymasterCertificateChain keymasterCertificateChain) throws RemoteException;

    String[] getAccountTypesWithManagementDisabled() throws RemoteException;

    String[] getAccountTypesWithManagementDisabledAsUser(int i) throws RemoteException;

    List<ComponentName> getActiveAdmins(int i) throws RemoteException;

    List<String> getAffiliationIds(ComponentName componentName) throws RemoteException;

    String getAlwaysOnVpnPackage(ComponentName componentName) throws RemoteException;

    Bundle getApplicationRestrictions(ComponentName componentName, String str, String str2) throws RemoteException;

    String getApplicationRestrictionsManagingPackage(ComponentName componentName) throws RemoteException;

    boolean getAutoTimeRequired() throws RemoteException;

    List<UserHandle> getBindDeviceAdminTargetUsers(ComponentName componentName) throws RemoteException;

    boolean getBluetoothContactSharingDisabled(ComponentName componentName) throws RemoteException;

    boolean getBluetoothContactSharingDisabledForUser(int i) throws RemoteException;

    boolean getCameraDisabled(ComponentName componentName, int i) throws RemoteException;

    String getCertInstallerPackage(ComponentName componentName) throws RemoteException;

    boolean getCrossProfileCallerIdDisabled(ComponentName componentName) throws RemoteException;

    boolean getCrossProfileCallerIdDisabledForUser(int i) throws RemoteException;

    boolean getCrossProfileContactsSearchDisabled(ComponentName componentName) throws RemoteException;

    boolean getCrossProfileContactsSearchDisabledForUser(int i) throws RemoteException;

    List<String> getCrossProfileWidgetProviders(ComponentName componentName) throws RemoteException;

    int getCurrentFailedPasswordAttempts(int i, boolean z) throws RemoteException;

    List<String> getDelegatePackages(ComponentName componentName, String str) throws RemoteException;

    List<String> getDelegatedScopes(ComponentName componentName, String str) throws RemoteException;

    ComponentName getDeviceOwnerComponent(boolean z) throws RemoteException;

    CharSequence getDeviceOwnerLockScreenInfo() throws RemoteException;

    String getDeviceOwnerName() throws RemoteException;

    CharSequence getDeviceOwnerOrganizationName() throws RemoteException;

    int getDeviceOwnerUserId() throws RemoteException;

    List<String> getDisallowedSystemApps(ComponentName componentName, int i, String str) throws RemoteException;

    boolean getDoNotAskCredentialsOnBoot() throws RemoteException;

    CharSequence getEndUserSessionMessage(ComponentName componentName) throws RemoteException;

    boolean getForceEphemeralUsers(ComponentName componentName) throws RemoteException;

    ComponentName getGlobalProxyAdmin(int i) throws RemoteException;

    List<String> getKeepUninstalledPackages(ComponentName componentName, String str) throws RemoteException;

    int getKeyguardDisabledFeatures(ComponentName componentName, int i, boolean z) throws RemoteException;

    long getLastBugReportRequestTime() throws RemoteException;

    long getLastNetworkLogRetrievalTime() throws RemoteException;

    long getLastSecurityLogRetrievalTime() throws RemoteException;

    int getLockTaskFeatures(ComponentName componentName) throws RemoteException;

    String[] getLockTaskPackages(ComponentName componentName) throws RemoteException;

    CharSequence getLongSupportMessage(ComponentName componentName) throws RemoteException;

    CharSequence getLongSupportMessageForUser(ComponentName componentName, int i) throws RemoteException;

    ComponentName getMandatoryBackupTransport() throws RemoteException;

    int getMaximumFailedPasswordsForWipe(ComponentName componentName, int i, boolean z) throws RemoteException;

    long getMaximumTimeToLock(ComponentName componentName, int i, boolean z) throws RemoteException;

    List<String> getMeteredDataDisabledPackages(ComponentName componentName) throws RemoteException;

    int getOrganizationColor(ComponentName componentName) throws RemoteException;

    int getOrganizationColorForUser(int i) throws RemoteException;

    CharSequence getOrganizationName(ComponentName componentName) throws RemoteException;

    CharSequence getOrganizationNameForUser(int i) throws RemoteException;

    List<ApnSetting> getOverrideApns(ComponentName componentName) throws RemoteException;

    StringParceledListSlice getOwnerInstalledCaCerts(UserHandle userHandle) throws RemoteException;

    long getPasswordExpiration(ComponentName componentName, int i, boolean z) throws RemoteException;

    long getPasswordExpirationTimeout(ComponentName componentName, int i, boolean z) throws RemoteException;

    int getPasswordHistoryLength(ComponentName componentName, int i, boolean z) throws RemoteException;

    int getPasswordMinimumLength(ComponentName componentName, int i, boolean z) throws RemoteException;

    int getPasswordMinimumLetters(ComponentName componentName, int i, boolean z) throws RemoteException;

    int getPasswordMinimumLowerCase(ComponentName componentName, int i, boolean z) throws RemoteException;

    int getPasswordMinimumNonLetter(ComponentName componentName, int i, boolean z) throws RemoteException;

    int getPasswordMinimumNumeric(ComponentName componentName, int i, boolean z) throws RemoteException;

    int getPasswordMinimumSymbols(ComponentName componentName, int i, boolean z) throws RemoteException;

    int getPasswordMinimumUpperCase(ComponentName componentName, int i, boolean z) throws RemoteException;

    int getPasswordQuality(ComponentName componentName, int i, boolean z) throws RemoteException;

    SystemUpdateInfo getPendingSystemUpdate(ComponentName componentName) throws RemoteException;

    int getPermissionGrantState(ComponentName componentName, String str, String str2, String str3) throws RemoteException;

    int getPermissionPolicy(ComponentName componentName) throws RemoteException;

    List getPermittedAccessibilityServices(ComponentName componentName) throws RemoteException;

    List getPermittedAccessibilityServicesForUser(int i) throws RemoteException;

    List<String> getPermittedCrossProfileNotificationListeners(ComponentName componentName) throws RemoteException;

    List getPermittedInputMethods(ComponentName componentName) throws RemoteException;

    List getPermittedInputMethodsForCurrentUser() throws RemoteException;

    ComponentName getProfileOwner(int i) throws RemoteException;

    String getProfileOwnerName(int i) throws RemoteException;

    int getProfileWithMinimumFailedPasswordsForWipe(int i, boolean z) throws RemoteException;

    void getRemoveWarning(ComponentName componentName, RemoteCallback remoteCallback, int i) throws RemoteException;

    long getRequiredStrongAuthTimeout(ComponentName componentName, int i, boolean z) throws RemoteException;

    ComponentName getRestrictionsProvider(int i) throws RemoteException;

    boolean getScreenCaptureDisabled(ComponentName componentName, int i) throws RemoteException;

    List<UserHandle> getSecondaryUsers(ComponentName componentName) throws RemoteException;

    CharSequence getShortSupportMessage(ComponentName componentName) throws RemoteException;

    CharSequence getShortSupportMessageForUser(ComponentName componentName, int i) throws RemoteException;

    CharSequence getStartUserSessionMessage(ComponentName componentName) throws RemoteException;

    boolean getStorageEncryption(ComponentName componentName, int i) throws RemoteException;

    int getStorageEncryptionStatus(String str, int i) throws RemoteException;

    SystemUpdatePolicy getSystemUpdatePolicy() throws RemoteException;

    PersistableBundle getTransferOwnershipBundle() throws RemoteException;

    List<PersistableBundle> getTrustAgentConfiguration(ComponentName componentName, ComponentName componentName2, int i, boolean z) throws RemoteException;

    int getUserProvisioningState() throws RemoteException;

    Bundle getUserRestrictions(ComponentName componentName) throws RemoteException;

    String getWifiMacAddress(ComponentName componentName) throws RemoteException;

    boolean hasDeviceOwner() throws RemoteException;

    boolean hasGrantedPolicy(ComponentName componentName, int i, int i2) throws RemoteException;

    boolean hasUserSetupCompleted() throws RemoteException;

    boolean installCaCert(ComponentName componentName, String str, byte[] bArr) throws RemoteException;

    boolean installExistingPackage(ComponentName componentName, String str, String str2) throws RemoteException;

    boolean installKeyPair(ComponentName componentName, String str, byte[] bArr, byte[] bArr2, byte[] bArr3, String str2, boolean z, boolean z2) throws RemoteException;

    boolean isAccessibilityServicePermittedByAdmin(ComponentName componentName, String str, int i) throws RemoteException;

    boolean isActivePasswordSufficient(int i, boolean z) throws RemoteException;

    boolean isAdminActive(ComponentName componentName, int i) throws RemoteException;

    boolean isAffiliatedUser() throws RemoteException;

    boolean isApplicationHidden(ComponentName componentName, String str, String str2) throws RemoteException;

    boolean isBackupServiceEnabled(ComponentName componentName) throws RemoteException;

    boolean isCaCertApproved(String str, int i) throws RemoteException;

    boolean isCallerApplicationRestrictionsManagingPackage(String str) throws RemoteException;

    boolean isCurrentInputMethodSetByOwner() throws RemoteException;

    boolean isDeviceProvisioned() throws RemoteException;

    boolean isDeviceProvisioningConfigApplied() throws RemoteException;

    boolean isEphemeralUser(ComponentName componentName) throws RemoteException;

    boolean isInputMethodPermittedByAdmin(ComponentName componentName, String str, int i) throws RemoteException;

    boolean isLockTaskPermitted(String str) throws RemoteException;

    boolean isLogoutEnabled() throws RemoteException;

    boolean isManagedProfile(ComponentName componentName) throws RemoteException;

    boolean isMasterVolumeMuted(ComponentName componentName) throws RemoteException;

    boolean isMeteredDataDisabledPackageForUser(ComponentName componentName, String str, int i) throws RemoteException;

    boolean isNetworkLoggingEnabled(ComponentName componentName) throws RemoteException;

    boolean isNotificationListenerServicePermitted(String str, int i) throws RemoteException;

    boolean isOverrideApnEnabled(ComponentName componentName) throws RemoteException;

    boolean isPackageSuspended(ComponentName componentName, String str, String str2) throws RemoteException;

    boolean isProfileActivePasswordSufficientForParent(int i) throws RemoteException;

    boolean isProvisioningAllowed(String str, String str2) throws RemoteException;

    boolean isRemovingAdmin(ComponentName componentName, int i) throws RemoteException;

    boolean isResetPasswordTokenActive(ComponentName componentName) throws RemoteException;

    boolean isSecurityLoggingEnabled(ComponentName componentName) throws RemoteException;

    boolean isSeparateProfileChallengeAllowed(int i) throws RemoteException;

    boolean isSystemOnlyUser(ComponentName componentName) throws RemoteException;

    boolean isUninstallBlocked(ComponentName componentName, String str) throws RemoteException;

    boolean isUninstallInQueue(String str) throws RemoteException;

    boolean isUsingUnifiedPassword(ComponentName componentName) throws RemoteException;

    void lockNow(int i, boolean z) throws RemoteException;

    int logoutUser(ComponentName componentName) throws RemoteException;

    void notifyLockTaskModeChanged(boolean z, String str, int i) throws RemoteException;

    void notifyPendingSystemUpdate(SystemUpdateInfo systemUpdateInfo) throws RemoteException;

    boolean packageHasActiveAdmins(String str, int i) throws RemoteException;

    void reboot(ComponentName componentName) throws RemoteException;

    void removeActiveAdmin(ComponentName componentName, int i) throws RemoteException;

    boolean removeCrossProfileWidgetProvider(ComponentName componentName, String str) throws RemoteException;

    boolean removeKeyPair(ComponentName componentName, String str, String str2) throws RemoteException;

    boolean removeOverrideApn(ComponentName componentName, int i) throws RemoteException;

    boolean removeUser(ComponentName componentName, UserHandle userHandle) throws RemoteException;

    void reportFailedFingerprintAttempt(int i) throws RemoteException;

    void reportFailedPasswordAttempt(int i) throws RemoteException;

    void reportKeyguardDismissed(int i) throws RemoteException;

    void reportKeyguardSecured(int i) throws RemoteException;

    void reportPasswordChanged(int i) throws RemoteException;

    void reportSuccessfulFingerprintAttempt(int i) throws RemoteException;

    void reportSuccessfulPasswordAttempt(int i) throws RemoteException;

    boolean requestBugreport(ComponentName componentName) throws RemoteException;

    boolean resetPassword(String str, int i) throws RemoteException;

    boolean resetPasswordWithToken(ComponentName componentName, String str, byte[] bArr, int i) throws RemoteException;

    List<NetworkEvent> retrieveNetworkLogs(ComponentName componentName, long j) throws RemoteException;

    ParceledListSlice retrievePreRebootSecurityLogs(ComponentName componentName) throws RemoteException;

    ParceledListSlice retrieveSecurityLogs(ComponentName componentName) throws RemoteException;

    void setAccountManagementDisabled(ComponentName componentName, String str, boolean z) throws RemoteException;

    void setActiveAdmin(ComponentName componentName, boolean z, int i) throws RemoteException;

    void setActivePasswordState(PasswordMetrics passwordMetrics, int i) throws RemoteException;

    void setAffiliationIds(ComponentName componentName, List<String> list) throws RemoteException;

    boolean setAlwaysOnVpnPackage(ComponentName componentName, String str, boolean z) throws RemoteException;

    boolean setApplicationHidden(ComponentName componentName, String str, String str2, boolean z) throws RemoteException;

    void setApplicationRestrictions(ComponentName componentName, String str, String str2, Bundle bundle) throws RemoteException;

    boolean setApplicationRestrictionsManagingPackage(ComponentName componentName, String str) throws RemoteException;

    void setAutoTimeRequired(ComponentName componentName, boolean z) throws RemoteException;

    void setBackupServiceEnabled(ComponentName componentName, boolean z) throws RemoteException;

    void setBluetoothContactSharingDisabled(ComponentName componentName, boolean z) throws RemoteException;

    void setCameraDisabled(ComponentName componentName, boolean z) throws RemoteException;

    void setCertInstallerPackage(ComponentName componentName, String str) throws RemoteException;

    void setCrossProfileCallerIdDisabled(ComponentName componentName, boolean z) throws RemoteException;

    void setCrossProfileContactsSearchDisabled(ComponentName componentName, boolean z) throws RemoteException;

    void setDefaultSmsApplication(ComponentName componentName, String str) throws RemoteException;

    void setDelegatedScopes(ComponentName componentName, String str, List<String> list) throws RemoteException;

    boolean setDeviceOwner(ComponentName componentName, String str, int i) throws RemoteException;

    void setDeviceOwnerLockScreenInfo(ComponentName componentName, CharSequence charSequence) throws RemoteException;

    void setDeviceProvisioningConfigApplied() throws RemoteException;

    void setEndUserSessionMessage(ComponentName componentName, CharSequence charSequence) throws RemoteException;

    void setForceEphemeralUsers(ComponentName componentName, boolean z) throws RemoteException;

    ComponentName setGlobalProxy(ComponentName componentName, String str, String str2) throws RemoteException;

    void setGlobalSetting(ComponentName componentName, String str, String str2) throws RemoteException;

    void setKeepUninstalledPackages(ComponentName componentName, String str, List<String> list) throws RemoteException;

    boolean setKeyPairCertificate(ComponentName componentName, String str, String str2, byte[] bArr, byte[] bArr2, boolean z) throws RemoteException;

    boolean setKeyguardDisabled(ComponentName componentName, boolean z) throws RemoteException;

    void setKeyguardDisabledFeatures(ComponentName componentName, int i, boolean z) throws RemoteException;

    void setLockTaskFeatures(ComponentName componentName, int i) throws RemoteException;

    void setLockTaskPackages(ComponentName componentName, String[] strArr) throws RemoteException;

    void setLogoutEnabled(ComponentName componentName, boolean z) throws RemoteException;

    void setLongSupportMessage(ComponentName componentName, CharSequence charSequence) throws RemoteException;

    boolean setMandatoryBackupTransport(ComponentName componentName, ComponentName componentName2) throws RemoteException;

    void setMasterVolumeMuted(ComponentName componentName, boolean z) throws RemoteException;

    void setMaximumFailedPasswordsForWipe(ComponentName componentName, int i, boolean z) throws RemoteException;

    void setMaximumTimeToLock(ComponentName componentName, long j, boolean z) throws RemoteException;

    List<String> setMeteredDataDisabledPackages(ComponentName componentName, List<String> list) throws RemoteException;

    void setNetworkLoggingEnabled(ComponentName componentName, boolean z) throws RemoteException;

    void setOrganizationColor(ComponentName componentName, int i) throws RemoteException;

    void setOrganizationColorForUser(int i, int i2) throws RemoteException;

    void setOrganizationName(ComponentName componentName, CharSequence charSequence) throws RemoteException;

    void setOverrideApnsEnabled(ComponentName componentName, boolean z) throws RemoteException;

    String[] setPackagesSuspended(ComponentName componentName, String str, String[] strArr, boolean z) throws RemoteException;

    void setPasswordExpirationTimeout(ComponentName componentName, long j, boolean z) throws RemoteException;

    void setPasswordHistoryLength(ComponentName componentName, int i, boolean z) throws RemoteException;

    void setPasswordMinimumLength(ComponentName componentName, int i, boolean z) throws RemoteException;

    void setPasswordMinimumLetters(ComponentName componentName, int i, boolean z) throws RemoteException;

    void setPasswordMinimumLowerCase(ComponentName componentName, int i, boolean z) throws RemoteException;

    void setPasswordMinimumNonLetter(ComponentName componentName, int i, boolean z) throws RemoteException;

    void setPasswordMinimumNumeric(ComponentName componentName, int i, boolean z) throws RemoteException;

    void setPasswordMinimumSymbols(ComponentName componentName, int i, boolean z) throws RemoteException;

    void setPasswordMinimumUpperCase(ComponentName componentName, int i, boolean z) throws RemoteException;

    void setPasswordQuality(ComponentName componentName, int i, boolean z) throws RemoteException;

    boolean setPermissionGrantState(ComponentName componentName, String str, String str2, String str3, int i) throws RemoteException;

    void setPermissionPolicy(ComponentName componentName, String str, int i) throws RemoteException;

    boolean setPermittedAccessibilityServices(ComponentName componentName, List list) throws RemoteException;

    boolean setPermittedCrossProfileNotificationListeners(ComponentName componentName, List<String> list) throws RemoteException;

    boolean setPermittedInputMethods(ComponentName componentName, List list) throws RemoteException;

    void setProfileEnabled(ComponentName componentName) throws RemoteException;

    void setProfileName(ComponentName componentName, String str) throws RemoteException;

    boolean setProfileOwner(ComponentName componentName, String str, int i) throws RemoteException;

    void setRecommendedGlobalProxy(ComponentName componentName, ProxyInfo proxyInfo) throws RemoteException;

    void setRequiredStrongAuthTimeout(ComponentName componentName, long j, boolean z) throws RemoteException;

    boolean setResetPasswordToken(ComponentName componentName, byte[] bArr) throws RemoteException;

    void setRestrictionsProvider(ComponentName componentName, ComponentName componentName2) throws RemoteException;

    void setScreenCaptureDisabled(ComponentName componentName, boolean z) throws RemoteException;

    void setSecureSetting(ComponentName componentName, String str, String str2) throws RemoteException;

    void setSecurityLoggingEnabled(ComponentName componentName, boolean z) throws RemoteException;

    void setShortSupportMessage(ComponentName componentName, CharSequence charSequence) throws RemoteException;

    void setStartUserSessionMessage(ComponentName componentName, CharSequence charSequence) throws RemoteException;

    boolean setStatusBarDisabled(ComponentName componentName, boolean z) throws RemoteException;

    int setStorageEncryption(ComponentName componentName, boolean z) throws RemoteException;

    void setSystemSetting(ComponentName componentName, String str, String str2) throws RemoteException;

    void setSystemUpdatePolicy(ComponentName componentName, SystemUpdatePolicy systemUpdatePolicy) throws RemoteException;

    boolean setTime(ComponentName componentName, long j) throws RemoteException;

    boolean setTimeZone(ComponentName componentName, String str) throws RemoteException;

    void setTrustAgentConfiguration(ComponentName componentName, ComponentName componentName2, PersistableBundle persistableBundle, boolean z) throws RemoteException;

    void setUninstallBlocked(ComponentName componentName, String str, String str2, boolean z) throws RemoteException;

    void setUserIcon(ComponentName componentName, Bitmap bitmap) throws RemoteException;

    void setUserProvisioningState(int i, int i2) throws RemoteException;

    void setUserRestriction(ComponentName componentName, String str, boolean z) throws RemoteException;

    void startManagedQuickContact(String str, long j, boolean z, long j2, Intent intent) throws RemoteException;

    int startUserInBackground(ComponentName componentName, UserHandle userHandle) throws RemoteException;

    int stopUser(ComponentName componentName, UserHandle userHandle) throws RemoteException;

    boolean switchUser(ComponentName componentName, UserHandle userHandle) throws RemoteException;

    void transferOwnership(ComponentName componentName, ComponentName componentName2, PersistableBundle persistableBundle) throws RemoteException;

    void uninstallCaCerts(ComponentName componentName, String str, String[] strArr) throws RemoteException;

    void uninstallPackageWithActiveAdmins(String str) throws RemoteException;

    boolean updateOverrideApn(ComponentName componentName, int i, ApnSetting apnSetting) throws RemoteException;

    void wipeDataWithReason(int i, String str) throws RemoteException;
}
