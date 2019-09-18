package android.app.admin;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.SystemApi;
import android.app.IServiceConnection;
import android.app.admin.DevicePolicyManager;
import android.app.admin.SecurityLog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.IPackageDataObserver;
import android.content.pm.PackageManager;
import android.content.pm.ParceledListSlice;
import android.content.pm.UserInfo;
import android.graphics.Bitmap;
import android.net.ProxyInfo;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.os.RemoteCallback;
import android.os.RemoteException;
import android.os.ServiceSpecificException;
import android.os.UserHandle;
import android.os.UserManager;
import android.security.AttestedKeyPair;
import android.security.Credentials;
import android.security.KeyChain;
import android.security.KeyChainException;
import android.security.keymaster.KeymasterCertificateChain;
import android.security.keystore.AttestationUtils;
import android.security.keystore.KeyAttestationException;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.ParcelableKeyGenParameterSpec;
import android.telephony.data.ApnSetting;
import android.util.ArraySet;
import android.util.Log;
import com.android.internal.R;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.Preconditions;
import com.android.org.conscrypt.TrustedCertificateStore;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

public class DevicePolicyManager {
    @SystemApi
    public static final String ACCOUNT_FEATURE_DEVICE_OR_PROFILE_OWNER_ALLOWED = "android.account.DEVICE_OR_PROFILE_OWNER_ALLOWED";
    @SystemApi
    public static final String ACCOUNT_FEATURE_DEVICE_OR_PROFILE_OWNER_DISALLOWED = "android.account.DEVICE_OR_PROFILE_OWNER_DISALLOWED";
    public static final String ACTION_ADD_DEVICE_ADMIN = "android.app.action.ADD_DEVICE_ADMIN";
    public static final String ACTION_APPLICATION_DELEGATION_SCOPES_CHANGED = "android.app.action.APPLICATION_DELEGATION_SCOPES_CHANGED";
    public static final String ACTION_BUGREPORT_SHARING_ACCEPTED = "com.android.server.action.REMOTE_BUGREPORT_SHARING_ACCEPTED";
    public static final String ACTION_BUGREPORT_SHARING_DECLINED = "com.android.server.action.REMOTE_BUGREPORT_SHARING_DECLINED";
    public static final String ACTION_DATA_SHARING_RESTRICTION_APPLIED = "android.app.action.DATA_SHARING_RESTRICTION_APPLIED";
    public static final String ACTION_DATA_SHARING_RESTRICTION_CHANGED = "android.app.action.DATA_SHARING_RESTRICTION_CHANGED";
    public static final String ACTION_DEVICE_ADMIN_SERVICE = "android.app.action.DEVICE_ADMIN_SERVICE";
    public static final String ACTION_DEVICE_OWNER_CHANGED = "android.app.action.DEVICE_OWNER_CHANGED";
    public static final String ACTION_DEVICE_POLICY_MANAGER_STATE_CHANGED = "android.app.action.DEVICE_POLICY_MANAGER_STATE_CHANGED";
    public static final String ACTION_MANAGED_PROFILE_PROVISIONED = "android.app.action.MANAGED_PROFILE_PROVISIONED";
    public static final String ACTION_MANAGED_USER_CREATED = "android.app.action.MANAGED_USER_CREATED";
    public static final String ACTION_PROFILE_OWNER_CHANGED = "android.app.action.PROFILE_OWNER_CHANGED";
    public static final String ACTION_PROVISIONING_SUCCESSFUL = "android.app.action.PROVISIONING_SUCCESSFUL";
    @SystemApi
    public static final String ACTION_PROVISION_FINALIZATION = "android.app.action.PROVISION_FINALIZATION";
    public static final String ACTION_PROVISION_MANAGED_DEVICE = "android.app.action.PROVISION_MANAGED_DEVICE";
    @SystemApi
    public static final String ACTION_PROVISION_MANAGED_DEVICE_FROM_TRUSTED_SOURCE = "android.app.action.PROVISION_MANAGED_DEVICE_FROM_TRUSTED_SOURCE";
    public static final String ACTION_PROVISION_MANAGED_PROFILE = "android.app.action.PROVISION_MANAGED_PROFILE";
    public static final String ACTION_PROVISION_MANAGED_SHAREABLE_DEVICE = "android.app.action.PROVISION_MANAGED_SHAREABLE_DEVICE";
    public static final String ACTION_PROVISION_MANAGED_USER = "android.app.action.PROVISION_MANAGED_USER";
    public static final String ACTION_REMOTE_BUGREPORT_DISPATCH = "android.intent.action.REMOTE_BUGREPORT_DISPATCH";
    public static final String ACTION_SET_NEW_PARENT_PROFILE_PASSWORD = "android.app.action.SET_NEW_PARENT_PROFILE_PASSWORD";
    public static final String ACTION_SET_NEW_PASSWORD = "android.app.action.SET_NEW_PASSWORD";
    @SystemApi
    public static final String ACTION_SET_PROFILE_OWNER = "android.app.action.SET_PROFILE_OWNER";
    public static final String ACTION_SHOW_DEVICE_MONITORING_DIALOG = "android.app.action.SHOW_DEVICE_MONITORING_DIALOG";
    public static final String ACTION_START_ENCRYPTION = "android.app.action.START_ENCRYPTION";
    @SystemApi
    public static final String ACTION_STATE_USER_SETUP_COMPLETE = "android.app.action.STATE_USER_SETUP_COMPLETE";
    public static final String ACTION_SYSTEM_UPDATE_POLICY_CHANGED = "android.app.action.SYSTEM_UPDATE_POLICY_CHANGED";
    public static final int CODE_ACCOUNTS_NOT_EMPTY = 6;
    public static final int CODE_ADD_MANAGED_PROFILE_DISALLOWED = 15;
    public static final int CODE_CANNOT_ADD_MANAGED_PROFILE = 11;
    public static final int CODE_DEVICE_ADMIN_NOT_SUPPORTED = 13;
    public static final int CODE_HAS_DEVICE_OWNER = 1;
    public static final int CODE_HAS_PAIRED = 8;
    public static final int CODE_MANAGED_USERS_NOT_SUPPORTED = 9;
    public static final int CODE_NONSYSTEM_USER_EXISTS = 5;
    public static final int CODE_NOT_SYSTEM_USER = 7;
    public static final int CODE_NOT_SYSTEM_USER_SPLIT = 12;
    public static final int CODE_OK = 0;
    public static final int CODE_SPLIT_SYSTEM_USER_DEVICE_SYSTEM_USER = 14;
    public static final int CODE_SYSTEM_USER = 10;
    public static final int CODE_USER_HAS_PROFILE_OWNER = 2;
    public static final int CODE_USER_NOT_RUNNING = 3;
    public static final int CODE_USER_SETUP_COMPLETED = 4;
    public static final long DEFAULT_STRONG_AUTH_TIMEOUT_MS = 259200000;
    public static final String DELEGATION_APP_RESTRICTIONS = "delegation-app-restrictions";
    public static final String DELEGATION_BLOCK_UNINSTALL = "delegation-block-uninstall";
    public static final String DELEGATION_CERT_INSTALL = "delegation-cert-install";
    public static final String DELEGATION_ENABLE_SYSTEM_APP = "delegation-enable-system-app";
    public static final String DELEGATION_INSTALL_EXISTING_PACKAGE = "delegation-install-existing-package";
    public static final String DELEGATION_KEEP_UNINSTALLED_PACKAGES = "delegation-keep-uninstalled-packages";
    public static final String DELEGATION_PACKAGE_ACCESS = "delegation-package-access";
    public static final String DELEGATION_PERMISSION_GRANT = "delegation-permission-grant";
    public static final int ENCRYPTION_STATUS_ACTIVATING = 2;
    public static final int ENCRYPTION_STATUS_ACTIVE = 3;
    public static final int ENCRYPTION_STATUS_ACTIVE_DEFAULT_KEY = 4;
    public static final int ENCRYPTION_STATUS_ACTIVE_PER_USER = 5;
    public static final int ENCRYPTION_STATUS_INACTIVE = 1;
    public static final int ENCRYPTION_STATUS_UNSUPPORTED = 0;
    public static final String EXTRA_ADD_EXPLANATION = "android.app.extra.ADD_EXPLANATION";
    public static final String EXTRA_BUGREPORT_NOTIFICATION_TYPE = "android.app.extra.bugreport_notification_type";
    public static final String EXTRA_DELEGATION_SCOPES = "android.app.extra.DELEGATION_SCOPES";
    public static final String EXTRA_DEVICE_ADMIN = "android.app.extra.DEVICE_ADMIN";
    @SystemApi
    public static final String EXTRA_PROFILE_OWNER_NAME = "android.app.extra.PROFILE_OWNER_NAME";
    public static final String EXTRA_PROVISIONING_ACCOUNT_TO_MIGRATE = "android.app.extra.PROVISIONING_ACCOUNT_TO_MIGRATE";
    public static final String EXTRA_PROVISIONING_ADMIN_EXTRAS_BUNDLE = "android.app.extra.PROVISIONING_ADMIN_EXTRAS_BUNDLE";
    public static final String EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME = "android.app.extra.PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME";
    public static final String EXTRA_PROVISIONING_DEVICE_ADMIN_MINIMUM_VERSION_CODE = "android.app.extra.PROVISIONING_DEVICE_ADMIN_MINIMUM_VERSION_CODE";
    public static final String EXTRA_PROVISIONING_DEVICE_ADMIN_PACKAGE_CHECKSUM = "android.app.extra.PROVISIONING_DEVICE_ADMIN_PACKAGE_CHECKSUM";
    public static final String EXTRA_PROVISIONING_DEVICE_ADMIN_PACKAGE_DOWNLOAD_COOKIE_HEADER = "android.app.extra.PROVISIONING_DEVICE_ADMIN_PACKAGE_DOWNLOAD_COOKIE_HEADER";
    public static final String EXTRA_PROVISIONING_DEVICE_ADMIN_PACKAGE_DOWNLOAD_LOCATION = "android.app.extra.PROVISIONING_DEVICE_ADMIN_PACKAGE_DOWNLOAD_LOCATION";
    @SystemApi
    public static final String EXTRA_PROVISIONING_DEVICE_ADMIN_PACKAGE_ICON_URI = "android.app.extra.PROVISIONING_DEVICE_ADMIN_PACKAGE_ICON_URI";
    @SystemApi
    public static final String EXTRA_PROVISIONING_DEVICE_ADMIN_PACKAGE_LABEL = "android.app.extra.PROVISIONING_DEVICE_ADMIN_PACKAGE_LABEL";
    @Deprecated
    public static final String EXTRA_PROVISIONING_DEVICE_ADMIN_PACKAGE_NAME = "android.app.extra.PROVISIONING_DEVICE_ADMIN_PACKAGE_NAME";
    public static final String EXTRA_PROVISIONING_DEVICE_ADMIN_SIGNATURE_CHECKSUM = "android.app.extra.PROVISIONING_DEVICE_ADMIN_SIGNATURE_CHECKSUM";
    public static final String EXTRA_PROVISIONING_DISCLAIMERS = "android.app.extra.PROVISIONING_DISCLAIMERS";
    public static final String EXTRA_PROVISIONING_DISCLAIMER_CONTENT = "android.app.extra.PROVISIONING_DISCLAIMER_CONTENT";
    public static final String EXTRA_PROVISIONING_DISCLAIMER_HEADER = "android.app.extra.PROVISIONING_DISCLAIMER_HEADER";
    @Deprecated
    public static final String EXTRA_PROVISIONING_EMAIL_ADDRESS = "android.app.extra.PROVISIONING_EMAIL_ADDRESS";
    public static final String EXTRA_PROVISIONING_KEEP_ACCOUNT_ON_MIGRATION = "android.app.extra.PROVISIONING_KEEP_ACCOUNT_ON_MIGRATION";
    public static final String EXTRA_PROVISIONING_LEAVE_ALL_SYSTEM_APPS_ENABLED = "android.app.extra.PROVISIONING_LEAVE_ALL_SYSTEM_APPS_ENABLED";
    public static final String EXTRA_PROVISIONING_LOCALE = "android.app.extra.PROVISIONING_LOCALE";
    public static final String EXTRA_PROVISIONING_LOCAL_TIME = "android.app.extra.PROVISIONING_LOCAL_TIME";
    public static final String EXTRA_PROVISIONING_LOGO_URI = "android.app.extra.PROVISIONING_LOGO_URI";
    public static final String EXTRA_PROVISIONING_MAIN_COLOR = "android.app.extra.PROVISIONING_MAIN_COLOR";
    @SystemApi
    public static final String EXTRA_PROVISIONING_ORGANIZATION_NAME = "android.app.extra.PROVISIONING_ORGANIZATION_NAME";
    public static final String EXTRA_PROVISIONING_SKIP_ENCRYPTION = "android.app.extra.PROVISIONING_SKIP_ENCRYPTION";
    public static final String EXTRA_PROVISIONING_SKIP_USER_CONSENT = "android.app.extra.PROVISIONING_SKIP_USER_CONSENT";
    public static final String EXTRA_PROVISIONING_SKIP_USER_SETUP = "android.app.extra.PROVISIONING_SKIP_USER_SETUP";
    @SystemApi
    public static final String EXTRA_PROVISIONING_SUPPORT_URL = "android.app.extra.PROVISIONING_SUPPORT_URL";
    public static final String EXTRA_PROVISIONING_TIME_ZONE = "android.app.extra.PROVISIONING_TIME_ZONE";
    public static final String EXTRA_PROVISIONING_USE_MOBILE_DATA = "android.app.extra.PROVISIONING_USE_MOBILE_DATA";
    public static final String EXTRA_PROVISIONING_WIFI_HIDDEN = "android.app.extra.PROVISIONING_WIFI_HIDDEN";
    public static final String EXTRA_PROVISIONING_WIFI_PAC_URL = "android.app.extra.PROVISIONING_WIFI_PAC_URL";
    public static final String EXTRA_PROVISIONING_WIFI_PASSWORD = "android.app.extra.PROVISIONING_WIFI_PASSWORD";
    public static final String EXTRA_PROVISIONING_WIFI_PROXY_BYPASS = "android.app.extra.PROVISIONING_WIFI_PROXY_BYPASS";
    public static final String EXTRA_PROVISIONING_WIFI_PROXY_HOST = "android.app.extra.PROVISIONING_WIFI_PROXY_HOST";
    public static final String EXTRA_PROVISIONING_WIFI_PROXY_PORT = "android.app.extra.PROVISIONING_WIFI_PROXY_PORT";
    public static final String EXTRA_PROVISIONING_WIFI_SECURITY_TYPE = "android.app.extra.PROVISIONING_WIFI_SECURITY_TYPE";
    public static final String EXTRA_PROVISIONING_WIFI_SSID = "android.app.extra.PROVISIONING_WIFI_SSID";
    public static final String EXTRA_REMOTE_BUGREPORT_HASH = "android.intent.extra.REMOTE_BUGREPORT_HASH";
    public static final String EXTRA_RESTRICTION = "android.app.extra.RESTRICTION";
    public static final int FLAG_EVICT_CREDENTIAL_ENCRYPTION_KEY = 1;
    public static final int FLAG_MANAGED_CAN_ACCESS_PARENT = 2;
    public static final int FLAG_PARENT_CAN_ACCESS_MANAGED = 1;
    public static final int ID_TYPE_BASE_INFO = 1;
    public static final int ID_TYPE_IMEI = 4;
    public static final int ID_TYPE_MEID = 8;
    public static final int ID_TYPE_SERIAL = 2;
    public static final int INSTALLKEY_REQUEST_CREDENTIALS_ACCESS = 1;
    public static final int INSTALLKEY_SET_USER_SELECTABLE = 2;
    public static final int KEYGUARD_DISABLE_BIOMETRICS = 416;
    public static final int KEYGUARD_DISABLE_FACE = 128;
    public static final int KEYGUARD_DISABLE_FEATURES_ALL = Integer.MAX_VALUE;
    public static final int KEYGUARD_DISABLE_FEATURES_NONE = 0;
    public static final int KEYGUARD_DISABLE_FINGERPRINT = 32;
    public static final int KEYGUARD_DISABLE_IRIS = 256;
    public static final int KEYGUARD_DISABLE_REMOTE_INPUT = 64;
    public static final int KEYGUARD_DISABLE_SECURE_CAMERA = 2;
    public static final int KEYGUARD_DISABLE_SECURE_NOTIFICATIONS = 4;
    public static final int KEYGUARD_DISABLE_TRUST_AGENTS = 16;
    public static final int KEYGUARD_DISABLE_UNREDACTED_NOTIFICATIONS = 8;
    public static final int KEYGUARD_DISABLE_WIDGETS_ALL = 1;
    public static final int LEAVE_ALL_SYSTEM_APPS_ENABLED = 16;
    public static final int LOCK_TASK_FEATURE_GLOBAL_ACTIONS = 16;
    public static final int LOCK_TASK_FEATURE_HOME = 4;
    public static final int LOCK_TASK_FEATURE_KEYGUARD = 32;
    public static final int LOCK_TASK_FEATURE_NONE = 0;
    public static final int LOCK_TASK_FEATURE_NOTIFICATIONS = 2;
    public static final int LOCK_TASK_FEATURE_OVERVIEW = 8;
    public static final int LOCK_TASK_FEATURE_SYSTEM_INFO = 1;
    public static final int MAKE_USER_DEMO = 4;
    public static final int MAKE_USER_EPHEMERAL = 2;
    public static final String MIME_TYPE_PROVISIONING_NFC = "application/com.android.managedprovisioning";
    public static final int NOTIFICATION_BUGREPORT_ACCEPTED_NOT_FINISHED = 2;
    public static final int NOTIFICATION_BUGREPORT_FINISHED_NOT_ACCEPTED = 3;
    public static final int NOTIFICATION_BUGREPORT_STARTED = 1;
    public static final int PASSWORD_QUALITY_ALPHABETIC = 262144;
    public static final int PASSWORD_QUALITY_ALPHANUMERIC = 327680;
    public static final int PASSWORD_QUALITY_BIOMETRIC_WEAK = 32768;
    public static final int PASSWORD_QUALITY_COMPLEX = 393216;
    public static final int PASSWORD_QUALITY_MANAGED = 524288;
    public static final int PASSWORD_QUALITY_NUMERIC = 131072;
    public static final int PASSWORD_QUALITY_NUMERIC_COMPLEX = 196608;
    public static final int PASSWORD_QUALITY_SOMETHING = 65536;
    public static final int PASSWORD_QUALITY_UNSPECIFIED = 0;
    public static final int PERMISSION_GRANT_STATE_DEFAULT = 0;
    public static final int PERMISSION_GRANT_STATE_DENIED = 2;
    public static final int PERMISSION_GRANT_STATE_GRANTED = 1;
    public static final int PERMISSION_POLICY_AUTO_DENY = 2;
    public static final int PERMISSION_POLICY_AUTO_GRANT = 1;
    public static final int PERMISSION_POLICY_PROMPT = 0;
    public static final String POLICY_DISABLE_CAMERA = "policy_disable_camera";
    public static final String POLICY_DISABLE_SCREEN_CAPTURE = "policy_disable_screen_capture";
    public static final String POLICY_MANDATORY_BACKUPS = "policy_mandatory_backups";
    public static final String POLICY_SUSPEND_PACKAGES = "policy_suspend_packages";
    public static final int PROFILE_KEYGUARD_FEATURES_AFFECT_OWNER = 432;
    public static final int RESET_PASSWORD_DO_NOT_ASK_CREDENTIALS_ON_BOOT = 2;
    public static final int RESET_PASSWORD_REQUIRE_ENTRY = 1;
    public static final int SKIP_SETUP_WIZARD = 1;
    @SystemApi
    public static final int STATE_USER_PROFILE_COMPLETE = 4;
    @SystemApi
    public static final int STATE_USER_SETUP_COMPLETE = 2;
    @SystemApi
    public static final int STATE_USER_SETUP_FINALIZED = 3;
    @SystemApi
    public static final int STATE_USER_SETUP_INCOMPLETE = 1;
    @SystemApi
    public static final int STATE_USER_UNMANAGED = 0;
    private static String TAG = "DevicePolicyManager";
    public static final int WIPE_EUICC = 4;
    public static final int WIPE_EXTERNAL_STORAGE = 1;
    public static final int WIPE_RESET_PROTECTION_DATA = 2;
    private final Context mContext;
    private final boolean mParentInstance;
    private final IDevicePolicyManager mService;

    @Retention(RetentionPolicy.SOURCE)
    public @interface AttestationIdType {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface CreateAndManageUserFlags {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface LockNowFlag {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface LockTaskFeature {
    }

    public interface OnClearApplicationUserDataListener {
        void onApplicationUserDataCleared(String str, boolean z);
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface ProvisioningPreCondition {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface SystemSettingsWhitelist {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface UserProvisioningState {
    }

    public DevicePolicyManager(Context context, IDevicePolicyManager service) {
        this(context, service, false);
    }

    @VisibleForTesting
    protected DevicePolicyManager(Context context, IDevicePolicyManager service, boolean parentInstance) {
        this.mContext = context;
        this.mService = service;
        this.mParentInstance = parentInstance;
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public int myUserId() {
        return this.mContext.getUserId();
    }

    public boolean isAdminActive(ComponentName admin) {
        throwIfParentInstance("isAdminActive");
        return isAdminActiveAsUser(admin, myUserId());
    }

    public boolean isAdminActiveAsUser(ComponentName admin, int userId) {
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.isAdminActive(admin, userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isRemovingAdmin(ComponentName admin, int userId) {
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.isRemovingAdmin(admin, userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<ComponentName> getActiveAdmins() {
        throwIfParentInstance("getActiveAdmins");
        return getActiveAdminsAsUser(myUserId());
    }

    public List<ComponentName> getActiveAdminsAsUser(int userId) {
        if (this.mService == null) {
            return null;
        }
        try {
            return this.mService.getActiveAdmins(userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public boolean packageHasActiveAdmins(String packageName) {
        return packageHasActiveAdmins(packageName, myUserId());
    }

    public boolean packageHasActiveAdmins(String packageName, int userId) {
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.packageHasActiveAdmins(packageName, userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void removeActiveAdmin(ComponentName admin) {
        throwIfParentInstance("removeActiveAdmin");
        if (this.mService != null) {
            try {
                this.mService.removeActiveAdmin(admin, myUserId());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public boolean hasGrantedPolicy(ComponentName admin, int usesPolicy) {
        throwIfParentInstance("hasGrantedPolicy");
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.hasGrantedPolicy(admin, usesPolicy, myUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isSeparateProfileChallengeAllowed(int userHandle) {
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.isSeparateProfileChallengeAllowed(userHandle);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setPasswordQuality(ComponentName admin, int quality) {
        if (this.mService != null) {
            try {
                this.mService.setPasswordQuality(admin, quality, this.mParentInstance);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public int getPasswordQuality(ComponentName admin) {
        return getPasswordQuality(admin, myUserId());
    }

    public int getPasswordQuality(ComponentName admin, int userHandle) {
        if (this.mService == null) {
            return 0;
        }
        try {
            return this.mService.getPasswordQuality(admin, userHandle, this.mParentInstance);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setPasswordMinimumLength(ComponentName admin, int length) {
        if (this.mService != null) {
            try {
                this.mService.setPasswordMinimumLength(admin, length, this.mParentInstance);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public int getPasswordMinimumLength(ComponentName admin) {
        return getPasswordMinimumLength(admin, myUserId());
    }

    public int getPasswordMinimumLength(ComponentName admin, int userHandle) {
        if (this.mService == null) {
            return 0;
        }
        try {
            return this.mService.getPasswordMinimumLength(admin, userHandle, this.mParentInstance);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setPasswordMinimumUpperCase(ComponentName admin, int length) {
        if (this.mService != null) {
            try {
                this.mService.setPasswordMinimumUpperCase(admin, length, this.mParentInstance);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public int getPasswordMinimumUpperCase(ComponentName admin) {
        return getPasswordMinimumUpperCase(admin, myUserId());
    }

    public int getPasswordMinimumUpperCase(ComponentName admin, int userHandle) {
        if (this.mService == null) {
            return 0;
        }
        try {
            return this.mService.getPasswordMinimumUpperCase(admin, userHandle, this.mParentInstance);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setPasswordMinimumLowerCase(ComponentName admin, int length) {
        if (this.mService != null) {
            try {
                this.mService.setPasswordMinimumLowerCase(admin, length, this.mParentInstance);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public int getPasswordMinimumLowerCase(ComponentName admin) {
        return getPasswordMinimumLowerCase(admin, myUserId());
    }

    public int getPasswordMinimumLowerCase(ComponentName admin, int userHandle) {
        if (this.mService == null) {
            return 0;
        }
        try {
            return this.mService.getPasswordMinimumLowerCase(admin, userHandle, this.mParentInstance);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setPasswordMinimumLetters(ComponentName admin, int length) {
        if (this.mService != null) {
            try {
                this.mService.setPasswordMinimumLetters(admin, length, this.mParentInstance);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public int getPasswordMinimumLetters(ComponentName admin) {
        return getPasswordMinimumLetters(admin, myUserId());
    }

    public int getPasswordMinimumLetters(ComponentName admin, int userHandle) {
        if (this.mService == null) {
            return 0;
        }
        try {
            return this.mService.getPasswordMinimumLetters(admin, userHandle, this.mParentInstance);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setPasswordMinimumNumeric(ComponentName admin, int length) {
        if (this.mService != null) {
            try {
                this.mService.setPasswordMinimumNumeric(admin, length, this.mParentInstance);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public int getPasswordMinimumNumeric(ComponentName admin) {
        return getPasswordMinimumNumeric(admin, myUserId());
    }

    public int getPasswordMinimumNumeric(ComponentName admin, int userHandle) {
        if (this.mService == null) {
            return 0;
        }
        try {
            return this.mService.getPasswordMinimumNumeric(admin, userHandle, this.mParentInstance);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setPasswordMinimumSymbols(ComponentName admin, int length) {
        if (this.mService != null) {
            try {
                this.mService.setPasswordMinimumSymbols(admin, length, this.mParentInstance);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public int getPasswordMinimumSymbols(ComponentName admin) {
        return getPasswordMinimumSymbols(admin, myUserId());
    }

    public int getPasswordMinimumSymbols(ComponentName admin, int userHandle) {
        if (this.mService == null) {
            return 0;
        }
        try {
            return this.mService.getPasswordMinimumSymbols(admin, userHandle, this.mParentInstance);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setPasswordMinimumNonLetter(ComponentName admin, int length) {
        if (this.mService != null) {
            try {
                this.mService.setPasswordMinimumNonLetter(admin, length, this.mParentInstance);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public int getPasswordMinimumNonLetter(ComponentName admin) {
        return getPasswordMinimumNonLetter(admin, myUserId());
    }

    public int getPasswordMinimumNonLetter(ComponentName admin, int userHandle) {
        if (this.mService == null) {
            return 0;
        }
        try {
            return this.mService.getPasswordMinimumNonLetter(admin, userHandle, this.mParentInstance);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setPasswordHistoryLength(ComponentName admin, int length) {
        if (this.mService != null) {
            try {
                this.mService.setPasswordHistoryLength(admin, length, this.mParentInstance);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void setPasswordExpirationTimeout(ComponentName admin, long timeout) {
        if (this.mService != null) {
            try {
                this.mService.setPasswordExpirationTimeout(admin, timeout, this.mParentInstance);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public long getPasswordExpirationTimeout(ComponentName admin) {
        if (this.mService == null) {
            return 0;
        }
        try {
            return this.mService.getPasswordExpirationTimeout(admin, myUserId(), this.mParentInstance);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public long getPasswordExpiration(ComponentName admin) {
        if (this.mService == null) {
            return 0;
        }
        try {
            return this.mService.getPasswordExpiration(admin, myUserId(), this.mParentInstance);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getPasswordHistoryLength(ComponentName admin) {
        return getPasswordHistoryLength(admin, myUserId());
    }

    public int getPasswordHistoryLength(ComponentName admin, int userHandle) {
        if (this.mService == null) {
            return 0;
        }
        try {
            return this.mService.getPasswordHistoryLength(admin, userHandle, this.mParentInstance);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getPasswordMaximumLength(int quality) {
        return 32;
    }

    public boolean isActivePasswordSufficient() {
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.isActivePasswordSufficient(myUserId(), this.mParentInstance);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isUsingUnifiedPassword(ComponentName admin) {
        throwIfParentInstance("isUsingUnifiedPassword");
        if (this.mService == null) {
            return true;
        }
        try {
            return this.mService.isUsingUnifiedPassword(admin);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isProfileActivePasswordSufficientForParent(int userHandle) {
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.isProfileActivePasswordSufficientForParent(userHandle);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getCurrentFailedPasswordAttempts() {
        return getCurrentFailedPasswordAttempts(myUserId());
    }

    public int getCurrentFailedPasswordAttempts(int userHandle) {
        if (this.mService == null) {
            return -1;
        }
        try {
            return this.mService.getCurrentFailedPasswordAttempts(userHandle, this.mParentInstance);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean getDoNotAskCredentialsOnBoot() {
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.getDoNotAskCredentialsOnBoot();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setMaximumFailedPasswordsForWipe(ComponentName admin, int num) {
        if (this.mService != null) {
            try {
                this.mService.setMaximumFailedPasswordsForWipe(admin, num, this.mParentInstance);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public int getMaximumFailedPasswordsForWipe(ComponentName admin) {
        return getMaximumFailedPasswordsForWipe(admin, myUserId());
    }

    public int getMaximumFailedPasswordsForWipe(ComponentName admin, int userHandle) {
        if (this.mService == null) {
            return 0;
        }
        try {
            return this.mService.getMaximumFailedPasswordsForWipe(admin, userHandle, this.mParentInstance);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getProfileWithMinimumFailedPasswordsForWipe(int userHandle) {
        if (this.mService == null) {
            return UserInfo.NO_PROFILE_GROUP_ID;
        }
        try {
            return this.mService.getProfileWithMinimumFailedPasswordsForWipe(userHandle, this.mParentInstance);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean resetPassword(String password, int flags) {
        throwIfParentInstance("resetPassword");
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.resetPassword(password, flags);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean setResetPasswordToken(ComponentName admin, byte[] token) {
        throwIfParentInstance("setResetPasswordToken");
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.setResetPasswordToken(admin, token);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean clearResetPasswordToken(ComponentName admin) {
        throwIfParentInstance("clearResetPasswordToken");
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.clearResetPasswordToken(admin);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isResetPasswordTokenActive(ComponentName admin) {
        throwIfParentInstance("isResetPasswordTokenActive");
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.isResetPasswordTokenActive(admin);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean resetPasswordWithToken(ComponentName admin, String password, byte[] token, int flags) {
        throwIfParentInstance("resetPassword");
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.resetPasswordWithToken(admin, password, token, flags);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setMaximumTimeToLock(ComponentName admin, long timeMs) {
        if (this.mService != null) {
            try {
                this.mService.setMaximumTimeToLock(admin, timeMs, this.mParentInstance);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public long getMaximumTimeToLock(ComponentName admin) {
        return getMaximumTimeToLock(admin, myUserId());
    }

    public long getMaximumTimeToLock(ComponentName admin, int userHandle) {
        if (this.mService == null) {
            return 0;
        }
        try {
            return this.mService.getMaximumTimeToLock(admin, userHandle, this.mParentInstance);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setRequiredStrongAuthTimeout(ComponentName admin, long timeoutMs) {
        if (this.mService != null) {
            try {
                this.mService.setRequiredStrongAuthTimeout(admin, timeoutMs, this.mParentInstance);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public long getRequiredStrongAuthTimeout(ComponentName admin) {
        return getRequiredStrongAuthTimeout(admin, myUserId());
    }

    public long getRequiredStrongAuthTimeout(ComponentName admin, int userId) {
        if (this.mService == null) {
            return DEFAULT_STRONG_AUTH_TIMEOUT_MS;
        }
        try {
            return this.mService.getRequiredStrongAuthTimeout(admin, userId, this.mParentInstance);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void lockNow() {
        lockNow(0);
    }

    public void lockNow(int flags) {
        if (this.mService != null) {
            try {
                this.mService.lockNow(flags, this.mParentInstance);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void wipeData(int flags) {
        throwIfParentInstance("wipeData");
        wipeDataInternal(flags, this.mContext.getString(R.string.work_profile_deleted_description_dpm_wipe));
    }

    public void wipeData(int flags, CharSequence reason) {
        throwIfParentInstance("wipeData");
        Preconditions.checkNotNull(reason, "CharSequence is null");
        wipeDataInternal(flags, reason.toString());
    }

    private void wipeDataInternal(int flags, String wipeReasonForUser) {
        if (this.mService != null) {
            try {
                this.mService.wipeDataWithReason(flags, wipeReasonForUser);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public ComponentName setGlobalProxy(ComponentName admin, Proxy proxySpec, List<String> exclusionList) {
        String exclSpec;
        String hostSpec;
        String hostName;
        int port;
        String exclSpec2;
        throwIfParentInstance("setGlobalProxy");
        if (proxySpec == null) {
            throw new NullPointerException();
        } else if (this.mService == null) {
            return null;
        } else {
            try {
                if (proxySpec.equals(Proxy.NO_PROXY)) {
                    hostSpec = null;
                    exclSpec = null;
                } else if (proxySpec.type().equals(Proxy.Type.HTTP)) {
                    InetSocketAddress sa = (InetSocketAddress) proxySpec.address();
                    String hostSpec2 = hostName + ":" + Integer.toString(sa.getPort());
                    if (exclusionList == null) {
                        exclSpec2 = "";
                    } else {
                        StringBuilder listBuilder = new StringBuilder();
                        boolean firstDomain = true;
                        for (String exclDomain : exclusionList) {
                            if (!firstDomain) {
                                listBuilder.append(",");
                            } else {
                                firstDomain = false;
                            }
                            listBuilder.append(exclDomain.trim());
                        }
                        exclSpec2 = listBuilder.toString();
                    }
                    if (android.net.Proxy.validate(hostName, Integer.toString(port), exclSpec2) == 0) {
                        hostSpec = hostSpec2;
                        exclSpec = exclSpec2;
                    } else {
                        throw new IllegalArgumentException();
                    }
                } else {
                    throw new IllegalArgumentException();
                }
                return this.mService.setGlobalProxy(admin, hostSpec, exclSpec);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void setRecommendedGlobalProxy(ComponentName admin, ProxyInfo proxyInfo) {
        throwIfParentInstance("setRecommendedGlobalProxy");
        if (this.mService != null) {
            try {
                this.mService.setRecommendedGlobalProxy(admin, proxyInfo);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public ComponentName getGlobalProxyAdmin() {
        if (this.mService == null) {
            return null;
        }
        try {
            return this.mService.getGlobalProxyAdmin(myUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int setStorageEncryption(ComponentName admin, boolean encrypt) {
        throwIfParentInstance("setStorageEncryption");
        if (this.mService == null) {
            return 0;
        }
        try {
            return this.mService.setStorageEncryption(admin, encrypt);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean getStorageEncryption(ComponentName admin) {
        throwIfParentInstance("getStorageEncryption");
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.getStorageEncryption(admin, myUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getStorageEncryptionStatus() {
        throwIfParentInstance("getStorageEncryptionStatus");
        return getStorageEncryptionStatus(myUserId());
    }

    public int getStorageEncryptionStatus(int userHandle) {
        if (this.mService == null) {
            return 0;
        }
        try {
            return this.mService.getStorageEncryptionStatus(this.mContext.getPackageName(), userHandle);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean approveCaCert(String alias, int userHandle, boolean approval) {
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.approveCaCert(alias, userHandle, approval);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isCaCertApproved(String alias, int userHandle) {
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.isCaCertApproved(alias, userHandle);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean installCaCert(ComponentName admin, byte[] certBuffer) {
        throwIfParentInstance("installCaCert");
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.installCaCert(admin, this.mContext.getPackageName(), certBuffer);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void uninstallCaCert(ComponentName admin, byte[] certBuffer) {
        throwIfParentInstance("uninstallCaCert");
        if (this.mService != null) {
            try {
                String alias = getCaCertAlias(certBuffer);
                this.mService.uninstallCaCerts(admin, this.mContext.getPackageName(), new String[]{alias});
            } catch (CertificateException e) {
                Log.w(TAG, "Unable to parse certificate", e);
            } catch (RemoteException e2) {
                throw e2.rethrowFromSystemServer();
            }
        }
    }

    public List<byte[]> getInstalledCaCerts(ComponentName admin) {
        List<byte[]> certs = new ArrayList<>();
        throwIfParentInstance("getInstalledCaCerts");
        if (this.mService != null) {
            try {
                this.mService.enforceCanManageCaCerts(admin, this.mContext.getPackageName());
                TrustedCertificateStore certStore = new TrustedCertificateStore();
                for (String alias : certStore.userAliases()) {
                    try {
                        certs.add(certStore.getCertificate(alias).getEncoded());
                    } catch (CertificateException ce) {
                        String str = TAG;
                        Log.w(str, "Could not encode certificate: " + alias, ce);
                    }
                }
            } catch (RemoteException re) {
                throw re.rethrowFromSystemServer();
            }
        }
        return certs;
    }

    public void uninstallAllUserCaCerts(ComponentName admin) {
        throwIfParentInstance("uninstallAllUserCaCerts");
        if (this.mService != null) {
            try {
                this.mService.uninstallCaCerts(admin, this.mContext.getPackageName(), (String[]) new TrustedCertificateStore().userAliases().toArray(new String[0]));
            } catch (RemoteException re) {
                throw re.rethrowFromSystemServer();
            }
        }
    }

    public boolean hasCaCertInstalled(ComponentName admin, byte[] certBuffer) {
        throwIfParentInstance("hasCaCertInstalled");
        boolean z = false;
        if (this.mService != null) {
            try {
                this.mService.enforceCanManageCaCerts(admin, this.mContext.getPackageName());
                if (getCaCertAlias(certBuffer) != null) {
                    z = true;
                }
                return z;
            } catch (RemoteException re) {
                throw re.rethrowFromSystemServer();
            } catch (CertificateException ce) {
                Log.w(TAG, "Could not parse certificate", ce);
            }
        }
        return false;
    }

    public boolean installKeyPair(ComponentName admin, PrivateKey privKey, Certificate cert, String alias) {
        return installKeyPair(admin, privKey, new Certificate[]{cert}, alias, false);
    }

    public boolean installKeyPair(ComponentName admin, PrivateKey privKey, Certificate[] certs, String alias, boolean requestAccess) {
        int flags = 2;
        if (requestAccess) {
            flags = 2 | 1;
        }
        return installKeyPair(admin, privKey, certs, alias, flags);
    }

    public boolean installKeyPair(ComponentName admin, PrivateKey privKey, Certificate[] certs, String alias, int flags) {
        Certificate[] certificateArr = certs;
        throwIfParentInstance("installKeyPair");
        boolean requestAccess = (flags & 1) == 1;
        boolean isUserSelectable = (flags & 2) == 2;
        try {
            byte[] pemCert = Credentials.convertToPem(new Certificate[]{certificateArr[0]});
            byte[] pemChain = null;
            if (certificateArr.length > 1) {
                pemChain = Credentials.convertToPem((Certificate[]) Arrays.copyOfRange(certificateArr, 1, certificateArr.length));
            }
            try {
                return this.mService.installKeyPair(admin, this.mContext.getPackageName(), ((PKCS8EncodedKeySpec) KeyFactory.getInstance(privKey.getAlgorithm()).getKeySpec(privKey, PKCS8EncodedKeySpec.class)).getEncoded(), pemCert, pemChain, alias, requestAccess, isUserSelectable);
            } catch (RemoteException e) {
                e = e;
                throw e.rethrowFromSystemServer();
            } catch (NoSuchAlgorithmException | InvalidKeySpecException e2) {
                e = e2;
                Log.w(TAG, "Failed to obtain private key material", e);
                return false;
            } catch (IOException | CertificateException e3) {
                e = e3;
                Log.w(TAG, "Could not pem-encode certificate", e);
                return false;
            }
        } catch (RemoteException e4) {
            e = e4;
            PrivateKey privateKey = privKey;
            throw e.rethrowFromSystemServer();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e5) {
            e = e5;
            PrivateKey privateKey2 = privKey;
            Log.w(TAG, "Failed to obtain private key material", e);
            return false;
        } catch (IOException | CertificateException e6) {
            e = e6;
            PrivateKey privateKey3 = privKey;
            Log.w(TAG, "Could not pem-encode certificate", e);
            return false;
        }
    }

    public boolean removeKeyPair(ComponentName admin, String alias) {
        throwIfParentInstance("removeKeyPair");
        try {
            return this.mService.removeKeyPair(admin, this.mContext.getPackageName(), alias);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public AttestedKeyPair generateKeyPair(ComponentName admin, String algorithm, KeyGenParameterSpec keySpec, int idAttestationFlags) {
        throwIfParentInstance("generateKeyPair");
        try {
            ParcelableKeyGenParameterSpec parcelableSpec = new ParcelableKeyGenParameterSpec(keySpec);
            KeymasterCertificateChain attestationChain = new KeymasterCertificateChain();
            if (!this.mService.generateKeyPair(admin, this.mContext.getPackageName(), algorithm, parcelableSpec, idAttestationFlags, attestationChain)) {
                Log.e(TAG, "Error generating key via DevicePolicyManagerService.");
                return null;
            }
            String alias = keySpec.getKeystoreAlias();
            KeyPair keyPair = KeyChain.getKeyPair(this.mContext, alias);
            Certificate[] outputChain = null;
            try {
                if (AttestationUtils.isChainValid(attestationChain)) {
                    outputChain = AttestationUtils.parseCertificateChain(attestationChain);
                }
                return new AttestedKeyPair(keyPair, outputChain);
            } catch (KeyAttestationException e) {
                Log.e(TAG, "Error parsing attestation chain for alias " + alias, e);
                this.mService.removeKeyPair(admin, this.mContext.getPackageName(), alias);
                return null;
            }
        } catch (RemoteException e2) {
            throw e2.rethrowFromSystemServer();
        } catch (KeyChainException e3) {
            Log.w(TAG, "Failed to generate key", e3);
            return null;
        } catch (InterruptedException e4) {
            Log.w(TAG, "Interrupted while generating key", e4);
            Thread.currentThread().interrupt();
            return null;
        }
    }

    public boolean isDeviceIdAttestationSupported() {
        return this.mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_DEVICE_ID_ATTESTATION);
    }

    public boolean setKeyPairCertificate(ComponentName admin, String alias, List<Certificate> certs, boolean isUserSelectable) {
        byte[] pemChain;
        throwIfParentInstance("setKeyPairCertificate");
        try {
            byte[] pemCert = Credentials.convertToPem(new Certificate[]{certs.get(0)});
            if (certs.size() > 1) {
                pemChain = Credentials.convertToPem((Certificate[]) certs.subList(1, certs.size()).toArray(new Certificate[0]));
            } else {
                pemChain = null;
            }
            return this.mService.setKeyPairCertificate(admin, this.mContext.getPackageName(), alias, pemCert, pemChain, isUserSelectable);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        } catch (IOException | CertificateException e2) {
            Log.w(TAG, "Could not pem-encode certificate", e2);
            return false;
        }
    }

    private static String getCaCertAlias(byte[] certBuffer) throws CertificateException {
        return new TrustedCertificateStore().getCertificateAlias((X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(certBuffer)));
    }

    @Deprecated
    public void setCertInstallerPackage(ComponentName admin, String installerPackage) throws SecurityException {
        throwIfParentInstance("setCertInstallerPackage");
        if (this.mService != null) {
            try {
                this.mService.setCertInstallerPackage(admin, installerPackage);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    @Deprecated
    public String getCertInstallerPackage(ComponentName admin) throws SecurityException {
        throwIfParentInstance("getCertInstallerPackage");
        if (this.mService == null) {
            return null;
        }
        try {
            return this.mService.getCertInstallerPackage(admin);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setDelegatedScopes(ComponentName admin, String delegatePackage, List<String> scopes) {
        throwIfParentInstance("setDelegatedScopes");
        if (this.mService != null) {
            try {
                this.mService.setDelegatedScopes(admin, delegatePackage, scopes);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public List<String> getDelegatedScopes(ComponentName admin, String delegatedPackage) {
        throwIfParentInstance("getDelegatedScopes");
        if (this.mService == null) {
            return null;
        }
        try {
            return this.mService.getDelegatedScopes(admin, delegatedPackage);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<String> getDelegatePackages(ComponentName admin, String delegationScope) {
        throwIfParentInstance("getDelegatePackages");
        if (this.mService == null) {
            return null;
        }
        try {
            return this.mService.getDelegatePackages(admin, delegationScope);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setAlwaysOnVpnPackage(ComponentName admin, String vpnPackage, boolean lockdownEnabled) throws PackageManager.NameNotFoundException, UnsupportedOperationException {
        throwIfParentInstance("setAlwaysOnVpnPackage");
        if (this.mService != null) {
            try {
                if (!this.mService.setAlwaysOnVpnPackage(admin, vpnPackage, lockdownEnabled)) {
                    throw new PackageManager.NameNotFoundException(vpnPackage);
                }
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public String getAlwaysOnVpnPackage(ComponentName admin) {
        throwIfParentInstance("getAlwaysOnVpnPackage");
        if (this.mService == null) {
            return null;
        }
        try {
            return this.mService.getAlwaysOnVpnPackage(admin);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setCameraDisabled(ComponentName admin, boolean disabled) {
        throwIfParentInstance("setCameraDisabled");
        if (this.mService != null) {
            try {
                this.mService.setCameraDisabled(admin, disabled);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public boolean getCameraDisabled(ComponentName admin) {
        throwIfParentInstance("getCameraDisabled");
        return getCameraDisabled(admin, myUserId());
    }

    public boolean getCameraDisabled(ComponentName admin, int userHandle) {
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.getCameraDisabled(admin, userHandle);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean requestBugreport(ComponentName admin) {
        throwIfParentInstance("requestBugreport");
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.requestBugreport(admin);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean getGuestUserDisabled(ComponentName admin) {
        return false;
    }

    public void setScreenCaptureDisabled(ComponentName admin, boolean disabled) {
        throwIfParentInstance("setScreenCaptureDisabled");
        if (this.mService != null) {
            try {
                this.mService.setScreenCaptureDisabled(admin, disabled);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public boolean getScreenCaptureDisabled(ComponentName admin) {
        throwIfParentInstance("getScreenCaptureDisabled");
        return getScreenCaptureDisabled(admin, myUserId());
    }

    public boolean getScreenCaptureDisabled(ComponentName admin, int userHandle) {
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.getScreenCaptureDisabled(admin, userHandle);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setAutoTimeRequired(ComponentName admin, boolean required) {
        throwIfParentInstance("setAutoTimeRequired");
        if (this.mService != null) {
            try {
                this.mService.setAutoTimeRequired(admin, required);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public boolean getAutoTimeRequired() {
        throwIfParentInstance("getAutoTimeRequired");
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.getAutoTimeRequired();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setForceEphemeralUsers(ComponentName admin, boolean forceEphemeralUsers) {
        throwIfParentInstance("setForceEphemeralUsers");
        if (this.mService != null) {
            try {
                this.mService.setForceEphemeralUsers(admin, forceEphemeralUsers);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public boolean getForceEphemeralUsers(ComponentName admin) {
        throwIfParentInstance("getForceEphemeralUsers");
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.getForceEphemeralUsers(admin);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setKeyguardDisabledFeatures(ComponentName admin, int which) {
        if (this.mService != null) {
            try {
                this.mService.setKeyguardDisabledFeatures(admin, which, this.mParentInstance);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public int getKeyguardDisabledFeatures(ComponentName admin) {
        return getKeyguardDisabledFeatures(admin, myUserId());
    }

    public int getKeyguardDisabledFeatures(ComponentName admin, int userHandle) {
        if (this.mService == null) {
            return 0;
        }
        try {
            return this.mService.getKeyguardDisabledFeatures(admin, userHandle, this.mParentInstance);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setActiveAdmin(ComponentName policyReceiver, boolean refreshing, int userHandle) {
        if (this.mService != null) {
            try {
                this.mService.setActiveAdmin(policyReceiver, refreshing, userHandle);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void setActiveAdmin(ComponentName policyReceiver, boolean refreshing) {
        setActiveAdmin(policyReceiver, refreshing, myUserId());
    }

    public void getRemoveWarning(ComponentName admin, RemoteCallback result) {
        if (this.mService != null) {
            try {
                this.mService.getRemoveWarning(admin, result, myUserId());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void setActivePasswordState(PasswordMetrics metrics, int userHandle) {
        if (this.mService != null) {
            try {
                this.mService.setActivePasswordState(metrics, userHandle);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void reportPasswordChanged(int userId) {
        if (this.mService != null) {
            try {
                this.mService.reportPasswordChanged(userId);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void reportFailedPasswordAttempt(int userHandle) {
        if (this.mService != null) {
            try {
                this.mService.reportFailedPasswordAttempt(userHandle);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void reportSuccessfulPasswordAttempt(int userHandle) {
        if (this.mService != null) {
            try {
                this.mService.reportSuccessfulPasswordAttempt(userHandle);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void reportFailedFingerprintAttempt(int userHandle) {
        if (this.mService != null) {
            try {
                this.mService.reportFailedFingerprintAttempt(userHandle);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void reportSuccessfulFingerprintAttempt(int userHandle) {
        if (this.mService != null) {
            try {
                this.mService.reportSuccessfulFingerprintAttempt(userHandle);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void reportKeyguardDismissed(int userHandle) {
        if (this.mService != null) {
            try {
                this.mService.reportKeyguardDismissed(userHandle);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void reportKeyguardSecured(int userHandle) {
        if (this.mService != null) {
            try {
                this.mService.reportKeyguardSecured(userHandle);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public boolean setDeviceOwner(ComponentName who) {
        return setDeviceOwner(who, (String) null);
    }

    public boolean setDeviceOwner(ComponentName who, int userId) {
        return setDeviceOwner(who, null, userId);
    }

    public boolean setDeviceOwner(ComponentName who, String ownerName) {
        return setDeviceOwner(who, ownerName, 0);
    }

    public boolean setDeviceOwner(ComponentName who, String ownerName, int userId) throws IllegalArgumentException, IllegalStateException {
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.setDeviceOwner(who, ownerName, userId);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public boolean isDeviceOwnerApp(String packageName) {
        throwIfParentInstance("isDeviceOwnerApp");
        return isDeviceOwnerAppOnCallingUser(packageName);
    }

    public boolean isDeviceOwnerAppOnCallingUser(String packageName) {
        return isDeviceOwnerAppOnAnyUserInner(packageName, true);
    }

    public boolean isDeviceOwnerAppOnAnyUser(String packageName) {
        return isDeviceOwnerAppOnAnyUserInner(packageName, false);
    }

    public ComponentName getDeviceOwnerComponentOnCallingUser() {
        return getDeviceOwnerComponentInner(true);
    }

    @SystemApi
    public ComponentName getDeviceOwnerComponentOnAnyUser() {
        return getDeviceOwnerComponentInner(false);
    }

    private boolean isDeviceOwnerAppOnAnyUserInner(String packageName, boolean callingUserOnly) {
        if (packageName == null) {
            return false;
        }
        ComponentName deviceOwner = getDeviceOwnerComponentInner(callingUserOnly);
        if (deviceOwner == null) {
            return false;
        }
        return packageName.equals(deviceOwner.getPackageName());
    }

    private ComponentName getDeviceOwnerComponentInner(boolean callingUserOnly) {
        if (this.mService == null) {
            return null;
        }
        try {
            return this.mService.getDeviceOwnerComponent(callingUserOnly);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public int getDeviceOwnerUserId() {
        if (this.mService == null) {
            return UserInfo.NO_PROFILE_GROUP_ID;
        }
        try {
            return this.mService.getDeviceOwnerUserId();
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public void clearDeviceOwnerApp(String packageName) {
        throwIfParentInstance("clearDeviceOwnerApp");
        if (this.mService != null) {
            try {
                this.mService.clearDeviceOwner(packageName);
            } catch (RemoteException re) {
                throw re.rethrowFromSystemServer();
            }
        }
    }

    @SystemApi
    public String getDeviceOwner() {
        throwIfParentInstance("getDeviceOwner");
        ComponentName name = getDeviceOwnerComponentOnCallingUser();
        if (name != null) {
            return name.getPackageName();
        }
        return null;
    }

    @SuppressLint({"Doclava125"})
    @SystemApi
    public boolean isDeviceManaged() {
        try {
            return this.mService.hasDeviceOwner();
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public String getDeviceOwnerNameOnAnyUser() {
        throwIfParentInstance("getDeviceOwnerNameOnAnyUser");
        if (this.mService == null) {
            return null;
        }
        try {
            return this.mService.getDeviceOwnerName();
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    @SuppressLint({"Doclava125"})
    @SystemApi
    @Deprecated
    public String getDeviceInitializerApp() {
        return null;
    }

    @SuppressLint({"Doclava125"})
    @SystemApi
    @Deprecated
    public ComponentName getDeviceInitializerComponent() {
        return null;
    }

    @SystemApi
    @Deprecated
    public boolean setActiveProfileOwner(ComponentName admin, @Deprecated String ownerName) throws IllegalArgumentException {
        throwIfParentInstance("setActiveProfileOwner");
        if (this.mService == null) {
            return false;
        }
        try {
            int myUserId = myUserId();
            this.mService.setActiveAdmin(admin, false, myUserId);
            return this.mService.setProfileOwner(admin, ownerName, myUserId);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public void clearProfileOwner(ComponentName admin) {
        throwIfParentInstance("clearProfileOwner");
        if (this.mService != null) {
            try {
                this.mService.clearProfileOwner(admin);
            } catch (RemoteException re) {
                throw re.rethrowFromSystemServer();
            }
        }
    }

    public boolean hasUserSetupCompleted() {
        if (this.mService == null) {
            return true;
        }
        try {
            return this.mService.hasUserSetupCompleted();
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public boolean setProfileOwner(ComponentName admin, @Deprecated String ownerName, int userHandle) throws IllegalArgumentException {
        if (this.mService == null) {
            return false;
        }
        if (ownerName == null) {
            ownerName = "";
        }
        try {
            return this.mService.setProfileOwner(admin, ownerName, userHandle);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public void setDeviceOwnerLockScreenInfo(ComponentName admin, CharSequence info) {
        throwIfParentInstance("setDeviceOwnerLockScreenInfo");
        if (this.mService != null) {
            try {
                this.mService.setDeviceOwnerLockScreenInfo(admin, info);
            } catch (RemoteException re) {
                throw re.rethrowFromSystemServer();
            }
        }
    }

    public CharSequence getDeviceOwnerLockScreenInfo() {
        throwIfParentInstance("getDeviceOwnerLockScreenInfo");
        if (this.mService == null) {
            return null;
        }
        try {
            return this.mService.getDeviceOwnerLockScreenInfo();
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public String[] setPackagesSuspended(ComponentName admin, String[] packageNames, boolean suspended) {
        throwIfParentInstance("setPackagesSuspended");
        if (this.mService == null) {
            return packageNames;
        }
        try {
            return this.mService.setPackagesSuspended(admin, this.mContext.getPackageName(), packageNames, suspended);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public boolean isPackageSuspended(ComponentName admin, String packageName) throws PackageManager.NameNotFoundException {
        throwIfParentInstance("isPackageSuspended");
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.isPackageSuspended(admin, this.mContext.getPackageName(), packageName);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        } catch (IllegalArgumentException e2) {
            throw new PackageManager.NameNotFoundException(packageName);
        }
    }

    public void setProfileEnabled(ComponentName admin) {
        throwIfParentInstance("setProfileEnabled");
        if (this.mService != null) {
            try {
                this.mService.setProfileEnabled(admin);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void setProfileName(ComponentName admin, String profileName) {
        throwIfParentInstance("setProfileName");
        if (this.mService != null) {
            try {
                this.mService.setProfileName(admin, profileName);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public boolean isProfileOwnerApp(String packageName) {
        throwIfParentInstance("isProfileOwnerApp");
        boolean z = false;
        if (this.mService == null) {
            return false;
        }
        try {
            ComponentName profileOwner = this.mService.getProfileOwner(myUserId());
            if (profileOwner != null && profileOwner.getPackageName().equals(packageName)) {
                z = true;
            }
            return z;
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public ComponentName getProfileOwner() throws IllegalArgumentException {
        throwIfParentInstance("getProfileOwner");
        return getProfileOwnerAsUser(this.mContext.getUserId());
    }

    public ComponentName getProfileOwnerAsUser(int userId) throws IllegalArgumentException {
        if (this.mService == null) {
            return null;
        }
        try {
            return this.mService.getProfileOwner(userId);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public String getProfileOwnerName() throws IllegalArgumentException {
        if (this.mService == null) {
            return null;
        }
        try {
            return this.mService.getProfileOwnerName(this.mContext.getUserId());
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public String getProfileOwnerNameAsUser(int userId) throws IllegalArgumentException {
        throwIfParentInstance("getProfileOwnerNameAsUser");
        if (this.mService == null) {
            return null;
        }
        try {
            return this.mService.getProfileOwnerName(userId);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public void addPersistentPreferredActivity(ComponentName admin, IntentFilter filter, ComponentName activity) {
        throwIfParentInstance("addPersistentPreferredActivity");
        if (this.mService != null) {
            try {
                this.mService.addPersistentPreferredActivity(admin, filter, activity);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void clearPackagePersistentPreferredActivities(ComponentName admin, String packageName) {
        throwIfParentInstance("clearPackagePersistentPreferredActivities");
        if (this.mService != null) {
            try {
                this.mService.clearPackagePersistentPreferredActivities(admin, packageName);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void setDefaultSmsApplication(ComponentName admin, String packageName) {
        throwIfParentInstance("setDefaultSmsApplication");
        if (this.mService != null) {
            try {
                this.mService.setDefaultSmsApplication(admin, packageName);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    @Deprecated
    public void setApplicationRestrictionsManagingPackage(ComponentName admin, String packageName) throws PackageManager.NameNotFoundException {
        throwIfParentInstance("setApplicationRestrictionsManagingPackage");
        if (this.mService != null) {
            try {
                if (!this.mService.setApplicationRestrictionsManagingPackage(admin, packageName)) {
                    throw new PackageManager.NameNotFoundException(packageName);
                }
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    @Deprecated
    public String getApplicationRestrictionsManagingPackage(ComponentName admin) {
        throwIfParentInstance("getApplicationRestrictionsManagingPackage");
        if (this.mService == null) {
            return null;
        }
        try {
            return this.mService.getApplicationRestrictionsManagingPackage(admin);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public boolean isCallerApplicationRestrictionsManagingPackage() {
        throwIfParentInstance("isCallerApplicationRestrictionsManagingPackage");
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.isCallerApplicationRestrictionsManagingPackage(this.mContext.getPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setApplicationRestrictions(ComponentName admin, String packageName, Bundle settings) {
        throwIfParentInstance("setApplicationRestrictions");
        if (this.mService != null) {
            try {
                this.mService.setApplicationRestrictions(admin, this.mContext.getPackageName(), packageName, settings);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void setTrustAgentConfiguration(ComponentName admin, ComponentName target, PersistableBundle configuration) {
        if (this.mService != null) {
            try {
                this.mService.setTrustAgentConfiguration(admin, target, configuration, this.mParentInstance);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public List<PersistableBundle> getTrustAgentConfiguration(ComponentName admin, ComponentName agent) {
        return getTrustAgentConfiguration(admin, agent, myUserId());
    }

    public List<PersistableBundle> getTrustAgentConfiguration(ComponentName admin, ComponentName agent, int userHandle) {
        if (this.mService == null) {
            return new ArrayList();
        }
        try {
            return this.mService.getTrustAgentConfiguration(admin, agent, userHandle, this.mParentInstance);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setCrossProfileCallerIdDisabled(ComponentName admin, boolean disabled) {
        throwIfParentInstance("setCrossProfileCallerIdDisabled");
        if (this.mService != null) {
            try {
                this.mService.setCrossProfileCallerIdDisabled(admin, disabled);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public boolean getCrossProfileCallerIdDisabled(ComponentName admin) {
        throwIfParentInstance("getCrossProfileCallerIdDisabled");
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.getCrossProfileCallerIdDisabled(admin);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean getCrossProfileCallerIdDisabled(UserHandle userHandle) {
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.getCrossProfileCallerIdDisabledForUser(userHandle.getIdentifier());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setCrossProfileContactsSearchDisabled(ComponentName admin, boolean disabled) {
        throwIfParentInstance("setCrossProfileContactsSearchDisabled");
        if (this.mService != null) {
            try {
                this.mService.setCrossProfileContactsSearchDisabled(admin, disabled);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public boolean getCrossProfileContactsSearchDisabled(ComponentName admin) {
        throwIfParentInstance("getCrossProfileContactsSearchDisabled");
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.getCrossProfileContactsSearchDisabled(admin);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean getCrossProfileContactsSearchDisabled(UserHandle userHandle) {
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.getCrossProfileContactsSearchDisabledForUser(userHandle.getIdentifier());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void startManagedQuickContact(String actualLookupKey, long actualContactId, boolean isContactIdIgnored, long directoryId, Intent originalIntent) {
        if (this.mService != null) {
            try {
                this.mService.startManagedQuickContact(actualLookupKey, actualContactId, isContactIdIgnored, directoryId, originalIntent);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void startManagedQuickContact(String actualLookupKey, long actualContactId, Intent originalIntent) {
        startManagedQuickContact(actualLookupKey, actualContactId, false, 0, originalIntent);
    }

    public void setBluetoothContactSharingDisabled(ComponentName admin, boolean disabled) {
        throwIfParentInstance("setBluetoothContactSharingDisabled");
        if (this.mService != null) {
            try {
                this.mService.setBluetoothContactSharingDisabled(admin, disabled);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public boolean getBluetoothContactSharingDisabled(ComponentName admin) {
        throwIfParentInstance("getBluetoothContactSharingDisabled");
        if (this.mService == null) {
            return true;
        }
        try {
            return this.mService.getBluetoothContactSharingDisabled(admin);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean getBluetoothContactSharingDisabled(UserHandle userHandle) {
        if (this.mService == null) {
            return true;
        }
        try {
            return this.mService.getBluetoothContactSharingDisabledForUser(userHandle.getIdentifier());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void addCrossProfileIntentFilter(ComponentName admin, IntentFilter filter, int flags) {
        throwIfParentInstance("addCrossProfileIntentFilter");
        if (this.mService != null) {
            try {
                this.mService.addCrossProfileIntentFilter(admin, filter, flags);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void clearCrossProfileIntentFilters(ComponentName admin) {
        throwIfParentInstance("clearCrossProfileIntentFilters");
        if (this.mService != null) {
            try {
                this.mService.clearCrossProfileIntentFilters(admin);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public boolean setPermittedAccessibilityServices(ComponentName admin, List<String> packageNames) {
        throwIfParentInstance("setPermittedAccessibilityServices");
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.setPermittedAccessibilityServices(admin, packageNames);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<String> getPermittedAccessibilityServices(ComponentName admin) {
        throwIfParentInstance("getPermittedAccessibilityServices");
        if (this.mService == null) {
            return null;
        }
        try {
            return this.mService.getPermittedAccessibilityServices(admin);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isAccessibilityServicePermittedByAdmin(ComponentName admin, String packageName, int userHandle) {
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.isAccessibilityServicePermittedByAdmin(admin, packageName, userHandle);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public List<String> getPermittedAccessibilityServices(int userId) {
        throwIfParentInstance("getPermittedAccessibilityServices");
        if (this.mService == null) {
            return null;
        }
        try {
            return this.mService.getPermittedAccessibilityServicesForUser(userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean setPermittedInputMethods(ComponentName admin, List<String> packageNames) {
        throwIfParentInstance("setPermittedInputMethods");
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.setPermittedInputMethods(admin, packageNames);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<String> getPermittedInputMethods(ComponentName admin) {
        throwIfParentInstance("getPermittedInputMethods");
        if (this.mService == null) {
            return null;
        }
        try {
            return this.mService.getPermittedInputMethods(admin);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isInputMethodPermittedByAdmin(ComponentName admin, String packageName, int userHandle) {
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.isInputMethodPermittedByAdmin(admin, packageName, userHandle);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public List<String> getPermittedInputMethodsForCurrentUser() {
        throwIfParentInstance("getPermittedInputMethodsForCurrentUser");
        if (this.mService == null) {
            return null;
        }
        try {
            return this.mService.getPermittedInputMethodsForCurrentUser();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean setPermittedCrossProfileNotificationListeners(ComponentName admin, List<String> packageList) {
        throwIfParentInstance("setPermittedCrossProfileNotificationListeners");
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.setPermittedCrossProfileNotificationListeners(admin, packageList);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<String> getPermittedCrossProfileNotificationListeners(ComponentName admin) {
        throwIfParentInstance("getPermittedCrossProfileNotificationListeners");
        if (this.mService == null) {
            return null;
        }
        try {
            return this.mService.getPermittedCrossProfileNotificationListeners(admin);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isNotificationListenerServicePermitted(String packageName, int userId) {
        if (this.mService == null) {
            return true;
        }
        try {
            return this.mService.isNotificationListenerServicePermitted(packageName, userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<String> getKeepUninstalledPackages(ComponentName admin) {
        throwIfParentInstance("getKeepUninstalledPackages");
        if (this.mService == null) {
            return null;
        }
        try {
            return this.mService.getKeepUninstalledPackages(admin, this.mContext.getPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setKeepUninstalledPackages(ComponentName admin, List<String> packageNames) {
        throwIfParentInstance("setKeepUninstalledPackages");
        if (this.mService != null) {
            try {
                this.mService.setKeepUninstalledPackages(admin, this.mContext.getPackageName(), packageNames);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    @Deprecated
    public UserHandle createUser(ComponentName admin, String name) {
        return null;
    }

    @Deprecated
    public UserHandle createAndInitializeUser(ComponentName admin, String name, String ownerName, ComponentName profileOwnerComponent, Bundle adminExtras) {
        return null;
    }

    public UserHandle createAndManageUser(ComponentName admin, String name, ComponentName profileOwner, PersistableBundle adminExtras, int flags) {
        throwIfParentInstance("createAndManageUser");
        try {
            return this.mService.createAndManageUser(admin, name, profileOwner, adminExtras, flags);
        } catch (ServiceSpecificException e) {
            throw new UserManager.UserOperationException(e.getMessage(), e.errorCode);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public boolean removeUser(ComponentName admin, UserHandle userHandle) {
        throwIfParentInstance("removeUser");
        try {
            return this.mService.removeUser(admin, userHandle);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public boolean switchUser(ComponentName admin, UserHandle userHandle) {
        throwIfParentInstance("switchUser");
        try {
            return this.mService.switchUser(admin, userHandle);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public int startUserInBackground(ComponentName admin, UserHandle userHandle) {
        throwIfParentInstance("startUserInBackground");
        try {
            return this.mService.startUserInBackground(admin, userHandle);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public int stopUser(ComponentName admin, UserHandle userHandle) {
        throwIfParentInstance("stopUser");
        try {
            return this.mService.stopUser(admin, userHandle);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public int logoutUser(ComponentName admin) {
        throwIfParentInstance("logoutUser");
        try {
            return this.mService.logoutUser(admin);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public List<UserHandle> getSecondaryUsers(ComponentName admin) {
        throwIfParentInstance("getSecondaryUsers");
        try {
            return this.mService.getSecondaryUsers(admin);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public boolean isEphemeralUser(ComponentName admin) {
        throwIfParentInstance("isEphemeralUser");
        try {
            return this.mService.isEphemeralUser(admin);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public Bundle getApplicationRestrictions(ComponentName admin, String packageName) {
        throwIfParentInstance("getApplicationRestrictions");
        if (this.mService == null) {
            return null;
        }
        try {
            return this.mService.getApplicationRestrictions(admin, this.mContext.getPackageName(), packageName);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void addUserRestriction(ComponentName admin, String key) {
        throwIfParentInstance("addUserRestriction");
        if (this.mService != null) {
            try {
                this.mService.setUserRestriction(admin, key, true);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void clearUserRestriction(ComponentName admin, String key) {
        throwIfParentInstance("clearUserRestriction");
        if (this.mService != null) {
            try {
                this.mService.setUserRestriction(admin, key, false);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public Bundle getUserRestrictions(ComponentName admin) {
        throwIfParentInstance("getUserRestrictions");
        Bundle ret = null;
        if (this.mService != null) {
            try {
                ret = this.mService.getUserRestrictions(admin);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
        return ret == null ? new Bundle() : ret;
    }

    public Intent createAdminSupportIntent(String restriction) {
        throwIfParentInstance("createAdminSupportIntent");
        if (this.mService == null) {
            return null;
        }
        try {
            return this.mService.createAdminSupportIntent(restriction);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean setApplicationHidden(ComponentName admin, String packageName, boolean hidden) {
        throwIfParentInstance("setApplicationHidden");
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.setApplicationHidden(admin, this.mContext.getPackageName(), packageName, hidden);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isApplicationHidden(ComponentName admin, String packageName) {
        throwIfParentInstance("isApplicationHidden");
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.isApplicationHidden(admin, this.mContext.getPackageName(), packageName);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void enableSystemApp(ComponentName admin, String packageName) {
        throwIfParentInstance("enableSystemApp");
        if (this.mService != null) {
            try {
                this.mService.enableSystemApp(admin, this.mContext.getPackageName(), packageName);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public int enableSystemApp(ComponentName admin, Intent intent) {
        throwIfParentInstance("enableSystemApp");
        if (this.mService == null) {
            return 0;
        }
        try {
            return this.mService.enableSystemAppWithIntent(admin, this.mContext.getPackageName(), intent);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean installExistingPackage(ComponentName admin, String packageName) {
        throwIfParentInstance("installExistingPackage");
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.installExistingPackage(admin, this.mContext.getPackageName(), packageName);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setAccountManagementDisabled(ComponentName admin, String accountType, boolean disabled) {
        throwIfParentInstance("setAccountManagementDisabled");
        if (this.mService != null) {
            try {
                this.mService.setAccountManagementDisabled(admin, accountType, disabled);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public String[] getAccountTypesWithManagementDisabled() {
        throwIfParentInstance("getAccountTypesWithManagementDisabled");
        return getAccountTypesWithManagementDisabledAsUser(myUserId());
    }

    public String[] getAccountTypesWithManagementDisabledAsUser(int userId) {
        if (this.mService == null) {
            return null;
        }
        try {
            return this.mService.getAccountTypesWithManagementDisabledAsUser(userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setLockTaskPackages(ComponentName admin, String[] packages) throws SecurityException {
        throwIfParentInstance("setLockTaskPackages");
        if (this.mService != null) {
            try {
                this.mService.setLockTaskPackages(admin, packages);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public String[] getLockTaskPackages(ComponentName admin) {
        throwIfParentInstance("getLockTaskPackages");
        if (this.mService == null) {
            return new String[0];
        }
        try {
            return this.mService.getLockTaskPackages(admin);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isLockTaskPermitted(String pkg) {
        throwIfParentInstance("isLockTaskPermitted");
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.isLockTaskPermitted(pkg);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setLockTaskFeatures(ComponentName admin, int flags) {
        throwIfParentInstance("setLockTaskFeatures");
        if (this.mService != null) {
            try {
                this.mService.setLockTaskFeatures(admin, flags);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public int getLockTaskFeatures(ComponentName admin) {
        throwIfParentInstance("getLockTaskFeatures");
        if (this.mService == null) {
            return 0;
        }
        try {
            return this.mService.getLockTaskFeatures(admin);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setGlobalSetting(ComponentName admin, String setting, String value) {
        throwIfParentInstance("setGlobalSetting");
        if (this.mService != null) {
            try {
                this.mService.setGlobalSetting(admin, setting, value);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void setSystemSetting(ComponentName admin, String setting, String value) {
        throwIfParentInstance("setSystemSetting");
        if (this.mService != null) {
            try {
                this.mService.setSystemSetting(admin, setting, value);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public boolean setTime(ComponentName admin, long millis) {
        throwIfParentInstance("setTime");
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.setTime(admin, millis);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean setTimeZone(ComponentName admin, String timeZone) {
        throwIfParentInstance("setTimeZone");
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.setTimeZone(admin, timeZone);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setSecureSetting(ComponentName admin, String setting, String value) {
        throwIfParentInstance("setSecureSetting");
        if (this.mService != null) {
            try {
                this.mService.setSecureSetting(admin, setting, value);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void setRestrictionsProvider(ComponentName admin, ComponentName provider) {
        throwIfParentInstance("setRestrictionsProvider");
        if (this.mService != null) {
            try {
                this.mService.setRestrictionsProvider(admin, provider);
            } catch (RemoteException re) {
                throw re.rethrowFromSystemServer();
            }
        }
    }

    public void setMasterVolumeMuted(ComponentName admin, boolean on) {
        throwIfParentInstance("setMasterVolumeMuted");
        if (this.mService != null) {
            try {
                this.mService.setMasterVolumeMuted(admin, on);
            } catch (RemoteException re) {
                throw re.rethrowFromSystemServer();
            }
        }
    }

    public boolean isMasterVolumeMuted(ComponentName admin) {
        throwIfParentInstance("isMasterVolumeMuted");
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.isMasterVolumeMuted(admin);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public void setUninstallBlocked(ComponentName admin, String packageName, boolean uninstallBlocked) {
        throwIfParentInstance("setUninstallBlocked");
        if (this.mService != null) {
            try {
                this.mService.setUninstallBlocked(admin, this.mContext.getPackageName(), packageName, uninstallBlocked);
            } catch (RemoteException re) {
                throw re.rethrowFromSystemServer();
            }
        }
    }

    public boolean isUninstallBlocked(ComponentName admin, String packageName) {
        throwIfParentInstance("isUninstallBlocked");
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.isUninstallBlocked(admin, packageName);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public boolean addCrossProfileWidgetProvider(ComponentName admin, String packageName) {
        throwIfParentInstance("addCrossProfileWidgetProvider");
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.addCrossProfileWidgetProvider(admin, packageName);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public boolean removeCrossProfileWidgetProvider(ComponentName admin, String packageName) {
        throwIfParentInstance("removeCrossProfileWidgetProvider");
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.removeCrossProfileWidgetProvider(admin, packageName);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public List<String> getCrossProfileWidgetProviders(ComponentName admin) {
        throwIfParentInstance("getCrossProfileWidgetProviders");
        if (this.mService != null) {
            try {
                List<String> providers = this.mService.getCrossProfileWidgetProviders(admin);
                if (providers != null) {
                    return providers;
                }
            } catch (RemoteException re) {
                throw re.rethrowFromSystemServer();
            }
        }
        return Collections.emptyList();
    }

    public void setUserIcon(ComponentName admin, Bitmap icon) {
        throwIfParentInstance("setUserIcon");
        try {
            this.mService.setUserIcon(admin, icon);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public void setSystemUpdatePolicy(ComponentName admin, SystemUpdatePolicy policy) {
        throwIfParentInstance("setSystemUpdatePolicy");
        if (this.mService != null) {
            try {
                this.mService.setSystemUpdatePolicy(admin, policy);
            } catch (RemoteException re) {
                throw re.rethrowFromSystemServer();
            }
        }
    }

    public SystemUpdatePolicy getSystemUpdatePolicy() {
        throwIfParentInstance("getSystemUpdatePolicy");
        if (this.mService == null) {
            return null;
        }
        try {
            return this.mService.getSystemUpdatePolicy();
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public void clearSystemUpdatePolicyFreezePeriodRecord() {
        throwIfParentInstance("clearSystemUpdatePolicyFreezePeriodRecord");
        if (this.mService != null) {
            try {
                this.mService.clearSystemUpdatePolicyFreezePeriodRecord();
            } catch (RemoteException re) {
                throw re.rethrowFromSystemServer();
            }
        }
    }

    public boolean setKeyguardDisabled(ComponentName admin, boolean disabled) {
        throwIfParentInstance("setKeyguardDisabled");
        try {
            return this.mService.setKeyguardDisabled(admin, disabled);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public boolean setStatusBarDisabled(ComponentName admin, boolean disabled) {
        throwIfParentInstance("setStatusBarDisabled");
        try {
            return this.mService.setStatusBarDisabled(admin, disabled);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public void notifyPendingSystemUpdate(long updateReceivedTime) {
        throwIfParentInstance("notifyPendingSystemUpdate");
        if (this.mService != null) {
            try {
                this.mService.notifyPendingSystemUpdate(SystemUpdateInfo.of(updateReceivedTime));
            } catch (RemoteException re) {
                throw re.rethrowFromSystemServer();
            }
        }
    }

    @SystemApi
    public void notifyPendingSystemUpdate(long updateReceivedTime, boolean isSecurityPatch) {
        throwIfParentInstance("notifyPendingSystemUpdate");
        if (this.mService != null) {
            try {
                this.mService.notifyPendingSystemUpdate(SystemUpdateInfo.of(updateReceivedTime, isSecurityPatch));
            } catch (RemoteException re) {
                throw re.rethrowFromSystemServer();
            }
        }
    }

    public SystemUpdateInfo getPendingSystemUpdate(ComponentName admin) {
        throwIfParentInstance("getPendingSystemUpdate");
        try {
            return this.mService.getPendingSystemUpdate(admin);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public void setPermissionPolicy(ComponentName admin, int policy) {
        throwIfParentInstance("setPermissionPolicy");
        try {
            this.mService.setPermissionPolicy(admin, this.mContext.getPackageName(), policy);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public int getPermissionPolicy(ComponentName admin) {
        throwIfParentInstance("getPermissionPolicy");
        try {
            return this.mService.getPermissionPolicy(admin);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public boolean setPermissionGrantState(ComponentName admin, String packageName, String permission, int grantState) {
        throwIfParentInstance("setPermissionGrantState");
        try {
            return this.mService.setPermissionGrantState(admin, this.mContext.getPackageName(), packageName, permission, grantState);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public int getPermissionGrantState(ComponentName admin, String packageName, String permission) {
        throwIfParentInstance("getPermissionGrantState");
        try {
            return this.mService.getPermissionGrantState(admin, this.mContext.getPackageName(), packageName, permission);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public boolean isProvisioningAllowed(String action) {
        throwIfParentInstance("isProvisioningAllowed");
        try {
            return this.mService.isProvisioningAllowed(action, this.mContext.getPackageName());
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public int checkProvisioningPreCondition(String action, String packageName) {
        try {
            return this.mService.checkProvisioningPreCondition(action, packageName);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public boolean isManagedProfile(ComponentName admin) {
        throwIfParentInstance("isManagedProfile");
        try {
            return this.mService.isManagedProfile(admin);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public boolean isSystemOnlyUser(ComponentName admin) {
        try {
            return this.mService.isSystemOnlyUser(admin);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public String getWifiMacAddress(ComponentName admin) {
        throwIfParentInstance("getWifiMacAddress");
        try {
            return this.mService.getWifiMacAddress(admin);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public void reboot(ComponentName admin) {
        throwIfParentInstance("reboot");
        try {
            this.mService.reboot(admin);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public void setShortSupportMessage(ComponentName admin, CharSequence message) {
        throwIfParentInstance("setShortSupportMessage");
        if (this.mService != null) {
            try {
                this.mService.setShortSupportMessage(admin, message);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public CharSequence getShortSupportMessage(ComponentName admin) {
        throwIfParentInstance("getShortSupportMessage");
        if (this.mService == null) {
            return null;
        }
        try {
            return this.mService.getShortSupportMessage(admin);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setLongSupportMessage(ComponentName admin, CharSequence message) {
        throwIfParentInstance("setLongSupportMessage");
        if (this.mService != null) {
            try {
                this.mService.setLongSupportMessage(admin, message);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public CharSequence getLongSupportMessage(ComponentName admin) {
        throwIfParentInstance("getLongSupportMessage");
        if (this.mService == null) {
            return null;
        }
        try {
            return this.mService.getLongSupportMessage(admin);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public CharSequence getShortSupportMessageForUser(ComponentName admin, int userHandle) {
        if (this.mService == null) {
            return null;
        }
        try {
            return this.mService.getShortSupportMessageForUser(admin, userHandle);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public CharSequence getLongSupportMessageForUser(ComponentName admin, int userHandle) {
        if (this.mService == null) {
            return null;
        }
        try {
            return this.mService.getLongSupportMessageForUser(admin, userHandle);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public DevicePolicyManager getParentProfileInstance(ComponentName admin) {
        throwIfParentInstance("getParentProfileInstance");
        try {
            if (this.mService.isManagedProfile(admin)) {
                return new DevicePolicyManager(this.mContext, this.mService, true);
            }
            throw new SecurityException("The current user does not have a parent profile.");
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setSecurityLoggingEnabled(ComponentName admin, boolean enabled) {
        throwIfParentInstance("setSecurityLoggingEnabled");
        try {
            this.mService.setSecurityLoggingEnabled(admin, enabled);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public boolean isSecurityLoggingEnabled(ComponentName admin) {
        throwIfParentInstance("isSecurityLoggingEnabled");
        try {
            return this.mService.isSecurityLoggingEnabled(admin);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public List<SecurityLog.SecurityEvent> retrieveSecurityLogs(ComponentName admin) {
        throwIfParentInstance("retrieveSecurityLogs");
        try {
            ParceledListSlice<SecurityLog.SecurityEvent> list = this.mService.retrieveSecurityLogs(admin);
            if (list != null) {
                return list.getList();
            }
            return null;
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public long forceSecurityLogs() {
        if (this.mService == null) {
            return 0;
        }
        try {
            return this.mService.forceSecurityLogs();
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public DevicePolicyManager getParentProfileInstance(UserInfo uInfo) {
        this.mContext.checkSelfPermission(Manifest.permission.MANAGE_PROFILE_AND_DEVICE_OWNERS);
        if (uInfo.isManagedProfile()) {
            return new DevicePolicyManager(this.mContext, this.mService, true);
        }
        throw new SecurityException("The user " + uInfo.id + " does not have a parent profile.");
    }

    public List<String> setMeteredDataDisabledPackages(ComponentName admin, List<String> packageNames) {
        throwIfParentInstance("setMeteredDataDisabled");
        if (this.mService == null) {
            return packageNames;
        }
        try {
            return this.mService.setMeteredDataDisabledPackages(admin, packageNames);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public List<String> getMeteredDataDisabledPackages(ComponentName admin) {
        throwIfParentInstance("getMeteredDataDisabled");
        if (this.mService == null) {
            return new ArrayList();
        }
        try {
            return this.mService.getMeteredDataDisabledPackages(admin);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public boolean isMeteredDataDisabledPackageForUser(ComponentName admin, String packageName, int userId) {
        throwIfParentInstance("getMeteredDataDisabledForUser");
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.isMeteredDataDisabledPackageForUser(admin, packageName, userId);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public List<SecurityLog.SecurityEvent> retrievePreRebootSecurityLogs(ComponentName admin) {
        throwIfParentInstance("retrievePreRebootSecurityLogs");
        try {
            ParceledListSlice<SecurityLog.SecurityEvent> list = this.mService.retrievePreRebootSecurityLogs(admin);
            if (list != null) {
                return list.getList();
            }
            return null;
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public void setOrganizationColor(ComponentName admin, int color) {
        throwIfParentInstance("setOrganizationColor");
        try {
            this.mService.setOrganizationColor(admin, color | -16777216);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public void setOrganizationColorForUser(int color, int userId) {
        try {
            this.mService.setOrganizationColorForUser(color | -16777216, userId);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public int getOrganizationColor(ComponentName admin) {
        throwIfParentInstance("getOrganizationColor");
        try {
            return this.mService.getOrganizationColor(admin);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public int getOrganizationColorForUser(int userHandle) {
        try {
            return this.mService.getOrganizationColorForUser(userHandle);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public void setOrganizationName(ComponentName admin, CharSequence title) {
        throwIfParentInstance("setOrganizationName");
        try {
            this.mService.setOrganizationName(admin, title);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public CharSequence getOrganizationName(ComponentName admin) {
        throwIfParentInstance("getOrganizationName");
        try {
            return this.mService.getOrganizationName(admin);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    @SuppressLint({"Doclava125"})
    @SystemApi
    public CharSequence getDeviceOwnerOrganizationName() {
        try {
            return this.mService.getDeviceOwnerOrganizationName();
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public CharSequence getOrganizationNameForUser(int userHandle) {
        try {
            return this.mService.getOrganizationNameForUser(userHandle);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public int getUserProvisioningState() {
        throwIfParentInstance("getUserProvisioningState");
        if (this.mService == null) {
            return 0;
        }
        try {
            return this.mService.getUserProvisioningState();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setUserProvisioningState(int state, int userHandle) {
        if (this.mService != null) {
            try {
                this.mService.setUserProvisioningState(state, userHandle);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void setAffiliationIds(ComponentName admin, Set<String> ids) {
        throwIfParentInstance("setAffiliationIds");
        if (ids != null) {
            try {
                this.mService.setAffiliationIds(admin, new ArrayList(ids));
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            throw new IllegalArgumentException("ids must not be null");
        }
    }

    public Set<String> getAffiliationIds(ComponentName admin) {
        throwIfParentInstance("getAffiliationIds");
        try {
            return new ArraySet(this.mService.getAffiliationIds(admin));
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isAffiliatedUser() {
        throwIfParentInstance("isAffiliatedUser");
        try {
            return this.mService.isAffiliatedUser();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isUninstallInQueue(String packageName) {
        try {
            return this.mService.isUninstallInQueue(packageName);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public void uninstallPackageWithActiveAdmins(String packageName) {
        try {
            this.mService.uninstallPackageWithActiveAdmins(packageName);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public void forceRemoveActiveAdmin(ComponentName adminReceiver, int userHandle) {
        try {
            this.mService.forceRemoveActiveAdmin(adminReceiver, userHandle);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public boolean isDeviceProvisioned() {
        try {
            return this.mService.isDeviceProvisioned();
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public void setDeviceProvisioningConfigApplied() {
        try {
            this.mService.setDeviceProvisioningConfigApplied();
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public boolean isDeviceProvisioningConfigApplied() {
        try {
            return this.mService.isDeviceProvisioningConfigApplied();
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public void forceUpdateUserSetupComplete() {
        try {
            this.mService.forceUpdateUserSetupComplete();
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    private void throwIfParentInstance(String functionName) {
        if (this.mParentInstance) {
            throw new SecurityException(functionName + " cannot be called on the parent instance");
        }
    }

    public void setBackupServiceEnabled(ComponentName admin, boolean enabled) {
        throwIfParentInstance("setBackupServiceEnabled");
        try {
            this.mService.setBackupServiceEnabled(admin, enabled);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public boolean isBackupServiceEnabled(ComponentName admin) {
        throwIfParentInstance("isBackupServiceEnabled");
        try {
            return this.mService.isBackupServiceEnabled(admin);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public boolean setMandatoryBackupTransport(ComponentName admin, ComponentName backupTransportComponent) {
        throwIfParentInstance("setMandatoryBackupTransport");
        try {
            return this.mService.setMandatoryBackupTransport(admin, backupTransportComponent);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public ComponentName getMandatoryBackupTransport() {
        throwIfParentInstance("getMandatoryBackupTransport");
        try {
            return this.mService.getMandatoryBackupTransport();
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public void setNetworkLoggingEnabled(ComponentName admin, boolean enabled) {
        throwIfParentInstance("setNetworkLoggingEnabled");
        try {
            this.mService.setNetworkLoggingEnabled(admin, enabled);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public boolean isNetworkLoggingEnabled(ComponentName admin) {
        throwIfParentInstance("isNetworkLoggingEnabled");
        try {
            return this.mService.isNetworkLoggingEnabled(admin);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public List<NetworkEvent> retrieveNetworkLogs(ComponentName admin, long batchToken) {
        throwIfParentInstance("retrieveNetworkLogs");
        try {
            return this.mService.retrieveNetworkLogs(admin, batchToken);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public boolean bindDeviceAdminServiceAsUser(ComponentName admin, Intent serviceIntent, ServiceConnection conn, int flags, UserHandle targetUser) {
        throwIfParentInstance("bindDeviceAdminServiceAsUser");
        try {
            IServiceConnection sd = this.mContext.getServiceDispatcher(conn, this.mContext.getMainThreadHandler(), flags);
            serviceIntent.prepareToLeaveProcess(this.mContext);
            return this.mService.bindDeviceAdminServiceAsUser(admin, this.mContext.getIApplicationThread(), this.mContext.getActivityToken(), serviceIntent, sd, flags, targetUser.getIdentifier());
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public List<UserHandle> getBindDeviceAdminTargetUsers(ComponentName admin) {
        throwIfParentInstance("getBindDeviceAdminTargetUsers");
        try {
            return this.mService.getBindDeviceAdminTargetUsers(admin);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public long getLastSecurityLogRetrievalTime() {
        try {
            return this.mService.getLastSecurityLogRetrievalTime();
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public long getLastBugReportRequestTime() {
        try {
            return this.mService.getLastBugReportRequestTime();
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public long getLastNetworkLogRetrievalTime() {
        try {
            return this.mService.getLastNetworkLogRetrievalTime();
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public boolean isCurrentInputMethodSetByOwner() {
        try {
            return this.mService.isCurrentInputMethodSetByOwner();
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public List<String> getOwnerInstalledCaCerts(UserHandle user) {
        try {
            return this.mService.getOwnerInstalledCaCerts(user).getList();
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public void clearApplicationUserData(ComponentName admin, String packageName, final Executor executor, final OnClearApplicationUserDataListener listener) {
        throwIfParentInstance("clearAppData");
        Preconditions.checkNotNull(executor);
        Preconditions.checkNotNull(listener);
        try {
            this.mService.clearApplicationUserData(admin, packageName, new IPackageDataObserver.Stub() {
                public void onRemoveCompleted(String pkg, boolean succeeded) {
                    executor.execute(new Runnable(pkg, succeeded) {
                        private final /* synthetic */ String f$1;
                        private final /* synthetic */ boolean f$2;

                        {
                            this.f$1 = r2;
                            this.f$2 = r3;
                        }

                        public final void run() {
                            DevicePolicyManager.OnClearApplicationUserDataListener.this.onApplicationUserDataCleared(this.f$1, this.f$2);
                        }
                    });
                }
            });
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public void setLogoutEnabled(ComponentName admin, boolean enabled) {
        throwIfParentInstance("setLogoutEnabled");
        try {
            this.mService.setLogoutEnabled(admin, enabled);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public boolean isLogoutEnabled() {
        throwIfParentInstance("isLogoutEnabled");
        try {
            return this.mService.isLogoutEnabled();
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public Set<String> getDisallowedSystemApps(ComponentName admin, int userId, String provisioningAction) {
        try {
            return new ArraySet(this.mService.getDisallowedSystemApps(admin, userId, provisioningAction));
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public void transferOwnership(ComponentName admin, ComponentName target, PersistableBundle bundle) {
        throwIfParentInstance("transferOwnership");
        try {
            this.mService.transferOwnership(admin, target, bundle);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public void setStartUserSessionMessage(ComponentName admin, CharSequence startUserSessionMessage) {
        throwIfParentInstance("setStartUserSessionMessage");
        try {
            this.mService.setStartUserSessionMessage(admin, startUserSessionMessage);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public void setEndUserSessionMessage(ComponentName admin, CharSequence endUserSessionMessage) {
        throwIfParentInstance("setEndUserSessionMessage");
        try {
            this.mService.setEndUserSessionMessage(admin, endUserSessionMessage);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public CharSequence getStartUserSessionMessage(ComponentName admin) {
        throwIfParentInstance("getStartUserSessionMessage");
        try {
            return this.mService.getStartUserSessionMessage(admin);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public CharSequence getEndUserSessionMessage(ComponentName admin) {
        throwIfParentInstance("getEndUserSessionMessage");
        try {
            return this.mService.getEndUserSessionMessage(admin);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public int addOverrideApn(ComponentName admin, ApnSetting apnSetting) {
        throwIfParentInstance("addOverrideApn");
        if (this.mService == null) {
            return -1;
        }
        try {
            return this.mService.addOverrideApn(admin, apnSetting);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean updateOverrideApn(ComponentName admin, int apnId, ApnSetting apnSetting) {
        throwIfParentInstance("updateOverrideApn");
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.updateOverrideApn(admin, apnId, apnSetting);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean removeOverrideApn(ComponentName admin, int apnId) {
        throwIfParentInstance("removeOverrideApn");
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.removeOverrideApn(admin, apnId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<ApnSetting> getOverrideApns(ComponentName admin) {
        throwIfParentInstance("getOverrideApns");
        if (this.mService == null) {
            return Collections.emptyList();
        }
        try {
            return this.mService.getOverrideApns(admin);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setOverrideApnsEnabled(ComponentName admin, boolean enabled) {
        throwIfParentInstance("setOverrideApnEnabled");
        if (this.mService != null) {
            try {
                this.mService.setOverrideApnsEnabled(admin, enabled);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public boolean isOverrideApnEnabled(ComponentName admin) {
        throwIfParentInstance("isOverrideApnEnabled");
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.isOverrideApnEnabled(admin);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public PersistableBundle getTransferOwnershipBundle() {
        throwIfParentInstance("getTransferOwnershipBundle");
        try {
            return this.mService.getTransferOwnershipBundle();
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }
}
