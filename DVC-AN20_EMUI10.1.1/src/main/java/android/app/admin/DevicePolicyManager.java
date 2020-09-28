package android.app.admin;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.SystemApi;
import android.annotation.UnsupportedAppUsage;
import android.app.IServiceConnection;
import android.app.admin.DevicePolicyManager;
import android.app.admin.SecurityLog;
import android.app.admin.StartInstallingUpdateCallback;
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
import android.net.NetworkUtils;
import android.net.PrivateDnsConnectivityChecker;
import android.net.ProxyInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.os.PersistableBundle;
import android.os.RemoteCallback;
import android.os.RemoteException;
import android.os.ServiceSpecificException;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.SettingsStringUtil;
import android.security.AttestedKeyPair;
import android.security.Credentials;
import android.security.KeyChain;
import android.security.KeyChainException;
import android.security.keymaster.KeymasterCertificateChain;
import android.security.keystore.AttestationUtils;
import android.security.keystore.KeyAttestationException;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.ParcelableKeyGenParameterSpec;
import android.security.keystore.StrongBoxUnavailableException;
import android.telephony.SmsManager;
import android.telephony.data.ApnSetting;
import android.util.ArraySet;
import android.util.Log;
import com.android.internal.R;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.os.BackgroundThread;
import com.android.internal.util.Preconditions;
import com.android.internal.util.function.pooled.PooledLambda;
import com.android.org.conscrypt.TrustedCertificateStore;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

public class DevicePolicyManager {
    @SystemApi
    public static final String ACCOUNT_FEATURE_DEVICE_OR_PROFILE_OWNER_ALLOWED = "android.account.DEVICE_OR_PROFILE_OWNER_ALLOWED";
    @SystemApi
    public static final String ACCOUNT_FEATURE_DEVICE_OR_PROFILE_OWNER_DISALLOWED = "android.account.DEVICE_OR_PROFILE_OWNER_DISALLOWED";
    public static final String ACTION_ADD_DEVICE_ADMIN = "android.app.action.ADD_DEVICE_ADMIN";
    public static final String ACTION_ADMIN_POLICY_COMPLIANCE = "android.app.action.ADMIN_POLICY_COMPLIANCE";
    public static final String ACTION_APPLICATION_DELEGATION_SCOPES_CHANGED = "android.app.action.APPLICATION_DELEGATION_SCOPES_CHANGED";
    public static final String ACTION_BUGREPORT_SHARING_ACCEPTED = "com.android.server.action.REMOTE_BUGREPORT_SHARING_ACCEPTED";
    public static final String ACTION_BUGREPORT_SHARING_DECLINED = "com.android.server.action.REMOTE_BUGREPORT_SHARING_DECLINED";
    public static final String ACTION_DATA_SHARING_RESTRICTION_APPLIED = "android.app.action.DATA_SHARING_RESTRICTION_APPLIED";
    public static final String ACTION_DATA_SHARING_RESTRICTION_CHANGED = "android.app.action.DATA_SHARING_RESTRICTION_CHANGED";
    public static final String ACTION_DEVICE_ADMIN_SERVICE = "android.app.action.DEVICE_ADMIN_SERVICE";
    public static final String ACTION_DEVICE_OWNER_CHANGED = "android.app.action.DEVICE_OWNER_CHANGED";
    @UnsupportedAppUsage
    public static final String ACTION_DEVICE_POLICY_MANAGER_STATE_CHANGED = "android.app.action.DEVICE_POLICY_MANAGER_STATE_CHANGED";
    public static final String ACTION_GET_PROVISIONING_MODE = "android.app.action.GET_PROVISIONING_MODE";
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
    public static final String DELEGATION_CERT_SELECTION = "delegation-cert-selection";
    public static final String DELEGATION_ENABLE_SYSTEM_APP = "delegation-enable-system-app";
    public static final String DELEGATION_INSTALL_EXISTING_PACKAGE = "delegation-install-existing-package";
    public static final String DELEGATION_KEEP_UNINSTALLED_PACKAGES = "delegation-keep-uninstalled-packages";
    public static final String DELEGATION_NETWORK_LOGGING = "delegation-network-logging";
    public static final String DELEGATION_PACKAGE_ACCESS = "delegation-package-access";
    public static final String DELEGATION_PERMISSION_GRANT = "delegation-permission-grant";
    public static final int ENCRYPTION_STATUS_ACTIVATING = 2;
    public static final int ENCRYPTION_STATUS_ACTIVE = 3;
    public static final int ENCRYPTION_STATUS_ACTIVE_DEFAULT_KEY = 4;
    public static final int ENCRYPTION_STATUS_ACTIVE_PER_USER = 5;
    public static final int ENCRYPTION_STATUS_INACTIVE = 1;
    public static final int ENCRYPTION_STATUS_UNSUPPORTED = 0;
    public static final int ERROR_VPN_PACKAGE_NOT_FOUND = 1;
    public static final String EXTRA_ADD_EXPLANATION = "android.app.extra.ADD_EXPLANATION";
    public static final String EXTRA_BUGREPORT_NOTIFICATION_TYPE = "android.app.extra.bugreport_notification_type";
    public static final String EXTRA_DELEGATION_SCOPES = "android.app.extra.DELEGATION_SCOPES";
    public static final String EXTRA_DEVICE_ADMIN = "android.app.extra.DEVICE_ADMIN";
    public static final String EXTRA_PASSWORD_COMPLEXITY = "android.app.extra.PASSWORD_COMPLEXITY";
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
    public static final String EXTRA_PROVISIONING_IMEI = "android.app.extra.PROVISIONING_IMEI";
    public static final String EXTRA_PROVISIONING_KEEP_ACCOUNT_ON_MIGRATION = "android.app.extra.PROVISIONING_KEEP_ACCOUNT_ON_MIGRATION";
    public static final String EXTRA_PROVISIONING_LEAVE_ALL_SYSTEM_APPS_ENABLED = "android.app.extra.PROVISIONING_LEAVE_ALL_SYSTEM_APPS_ENABLED";
    public static final String EXTRA_PROVISIONING_LOCALE = "android.app.extra.PROVISIONING_LOCALE";
    public static final String EXTRA_PROVISIONING_LOCAL_TIME = "android.app.extra.PROVISIONING_LOCAL_TIME";
    public static final String EXTRA_PROVISIONING_LOGO_URI = "android.app.extra.PROVISIONING_LOGO_URI";
    public static final String EXTRA_PROVISIONING_MAIN_COLOR = "android.app.extra.PROVISIONING_MAIN_COLOR";
    public static final String EXTRA_PROVISIONING_MODE = "android.app.extra.PROVISIONING_MODE";
    @SystemApi
    public static final String EXTRA_PROVISIONING_ORGANIZATION_NAME = "android.app.extra.PROVISIONING_ORGANIZATION_NAME";
    public static final String EXTRA_PROVISIONING_SERIAL_NUMBER = "android.app.extra.PROVISIONING_SERIAL_NUMBER";
    public static final String EXTRA_PROVISIONING_SKIP_EDUCATION_SCREENS = "android.app.extra.PROVISIONING_SKIP_EDUCATION_SCREENS";
    public static final String EXTRA_PROVISIONING_SKIP_ENCRYPTION = "android.app.extra.PROVISIONING_SKIP_ENCRYPTION";
    public static final String EXTRA_PROVISIONING_SKIP_USER_CONSENT = "android.app.extra.PROVISIONING_SKIP_USER_CONSENT";
    public static final String EXTRA_PROVISIONING_SKIP_USER_SETUP = "android.app.extra.PROVISIONING_SKIP_USER_SETUP";
    @SystemApi
    public static final String EXTRA_PROVISIONING_SUPPORT_URL = "android.app.extra.PROVISIONING_SUPPORT_URL";
    public static final String EXTRA_PROVISIONING_TIME_ZONE = "android.app.extra.PROVISIONING_TIME_ZONE";
    @SystemApi
    public static final String EXTRA_PROVISIONING_TRIGGER = "android.app.extra.PROVISIONING_TRIGGER";
    public static final String EXTRA_PROVISIONING_USE_MOBILE_DATA = "android.app.extra.PROVISIONING_USE_MOBILE_DATA";
    public static final String EXTRA_PROVISIONING_WIFI_ANONYMOUS_IDENTITY = "android.app.extra.PROVISIONING_WIFI_ANONYMOUS_IDENTITY";
    public static final String EXTRA_PROVISIONING_WIFI_CA_CERTIFICATE = "android.app.extra.PROVISIONING_WIFI_CA_CERTIFICATE";
    public static final String EXTRA_PROVISIONING_WIFI_DOMAIN = "android.app.extra.PROVISIONING_WIFI_DOMAIN";
    public static final String EXTRA_PROVISIONING_WIFI_EAP_METHOD = "android.app.extra.PROVISIONING_WIFI_EAP_METHOD";
    public static final String EXTRA_PROVISIONING_WIFI_HIDDEN = "android.app.extra.PROVISIONING_WIFI_HIDDEN";
    public static final String EXTRA_PROVISIONING_WIFI_IDENTITY = "android.app.extra.PROVISIONING_WIFI_IDENTITY";
    public static final String EXTRA_PROVISIONING_WIFI_PAC_URL = "android.app.extra.PROVISIONING_WIFI_PAC_URL";
    public static final String EXTRA_PROVISIONING_WIFI_PASSWORD = "android.app.extra.PROVISIONING_WIFI_PASSWORD";
    public static final String EXTRA_PROVISIONING_WIFI_PHASE2_AUTH = "android.app.extra.PROVISIONING_WIFI_PHASE2_AUTH";
    public static final String EXTRA_PROVISIONING_WIFI_PROXY_BYPASS = "android.app.extra.PROVISIONING_WIFI_PROXY_BYPASS";
    public static final String EXTRA_PROVISIONING_WIFI_PROXY_HOST = "android.app.extra.PROVISIONING_WIFI_PROXY_HOST";
    public static final String EXTRA_PROVISIONING_WIFI_PROXY_PORT = "android.app.extra.PROVISIONING_WIFI_PROXY_PORT";
    public static final String EXTRA_PROVISIONING_WIFI_SECURITY_TYPE = "android.app.extra.PROVISIONING_WIFI_SECURITY_TYPE";
    public static final String EXTRA_PROVISIONING_WIFI_SSID = "android.app.extra.PROVISIONING_WIFI_SSID";
    public static final String EXTRA_PROVISIONING_WIFI_USER_CERTIFICATE = "android.app.extra.PROVISIONING_WIFI_USER_CERTIFICATE";
    public static final String EXTRA_REMOTE_BUGREPORT_HASH = "android.intent.extra.REMOTE_BUGREPORT_HASH";
    @SystemApi
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
    public static final int KEY_GEN_STRONGBOX_UNAVAILABLE = 1;
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
    private static final int MAXIMUM_PASSWORD_LENGTH = 32;
    public static final String MIME_TYPE_PROVISIONING_NFC = "application/com.android.managedprovisioning";
    public static final int NOTIFICATION_BUGREPORT_ACCEPTED_NOT_FINISHED = 2;
    public static final int NOTIFICATION_BUGREPORT_FINISHED_NOT_ACCEPTED = 3;
    public static final int NOTIFICATION_BUGREPORT_STARTED = 1;
    public static final int PASSWORD_COMPLEXITY_HIGH = 327680;
    public static final int PASSWORD_COMPLEXITY_LOW = 65536;
    public static final int PASSWORD_COMPLEXITY_MEDIUM = 196608;
    public static final int PASSWORD_COMPLEXITY_NONE = 0;
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
    public static final String POLICY_SUSPEND_PACKAGES = "policy_suspend_packages";
    public static final int PRIVATE_DNS_MODE_OFF = 1;
    public static final int PRIVATE_DNS_MODE_OPPORTUNISTIC = 2;
    public static final int PRIVATE_DNS_MODE_PROVIDER_HOSTNAME = 3;
    public static final int PRIVATE_DNS_MODE_UNKNOWN = 0;
    public static final int PRIVATE_DNS_SET_ERROR_FAILURE_SETTING = 2;
    public static final int PRIVATE_DNS_SET_ERROR_HOST_NOT_SERVING = 1;
    public static final int PRIVATE_DNS_SET_NO_ERROR = 0;
    public static final int PROFILE_KEYGUARD_FEATURES_AFFECT_OWNER = 432;
    public static final int PROVISIONING_MODE_FULLY_MANAGED_DEVICE = 1;
    public static final int PROVISIONING_MODE_MANAGED_PROFILE = 2;
    @SystemApi
    public static final int PROVISIONING_TRIGGER_CLOUD_ENROLLMENT = 1;
    @SystemApi
    public static final int PROVISIONING_TRIGGER_PERSISTENT_DEVICE_OWNER = 3;
    @SystemApi
    public static final int PROVISIONING_TRIGGER_QR_CODE = 2;
    @SystemApi
    public static final int PROVISIONING_TRIGGER_UNSPECIFIED = 0;
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
    public static final int WIPE_SILENTLY = 8;
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
    public @interface InstallUpdateCallbackErrorConstants {
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
    public @interface PasswordComplexity {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface PermissionGrantState {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface PrivateDnsMode {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface PrivateDnsModeErrorCodes {
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

    public static abstract class InstallSystemUpdateCallback {
        public static final int UPDATE_ERROR_BATTERY_LOW = 5;
        public static final int UPDATE_ERROR_FILE_NOT_FOUND = 4;
        public static final int UPDATE_ERROR_INCORRECT_OS_VERSION = 2;
        public static final int UPDATE_ERROR_UNKNOWN = 1;
        public static final int UPDATE_ERROR_UPDATE_FILE_INVALID = 3;

        public void onInstallUpdateError(int errorCode, String errorMessage) {
        }
    }

    public boolean isAdminActive(ComponentName admin) {
        throwIfParentInstance("isAdminActive");
        return isAdminActiveAsUser(admin, myUserId());
    }

    public boolean isAdminActiveAsUser(ComponentName admin, int userId) {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return false;
        }
        try {
            return iDevicePolicyManager.isAdminActive(admin, userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isRemovingAdmin(ComponentName admin, int userId) {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return false;
        }
        try {
            return iDevicePolicyManager.isRemovingAdmin(admin, userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<ComponentName> getActiveAdmins() {
        throwIfParentInstance("getActiveAdmins");
        return getActiveAdminsAsUser(myUserId());
    }

    @UnsupportedAppUsage
    public List<ComponentName> getActiveAdminsAsUser(int userId) {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return null;
        }
        try {
            return iDevicePolicyManager.getActiveAdmins(userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public boolean packageHasActiveAdmins(String packageName) {
        return packageHasActiveAdmins(packageName, myUserId());
    }

    @UnsupportedAppUsage
    public boolean packageHasActiveAdmins(String packageName, int userId) {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return false;
        }
        try {
            return iDevicePolicyManager.packageHasActiveAdmins(packageName, userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void removeActiveAdmin(ComponentName admin) {
        throwIfParentInstance("removeActiveAdmin");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                iDevicePolicyManager.removeActiveAdmin(admin, myUserId());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public boolean hasGrantedPolicy(ComponentName admin, int usesPolicy) {
        throwIfParentInstance("hasGrantedPolicy");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return false;
        }
        try {
            return iDevicePolicyManager.hasGrantedPolicy(admin, usesPolicy, myUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isSeparateProfileChallengeAllowed(int userHandle) {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return false;
        }
        try {
            return iDevicePolicyManager.isSeparateProfileChallengeAllowed(userHandle);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setPasswordQuality(ComponentName admin, int quality) {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                iDevicePolicyManager.setPasswordQuality(admin, quality, this.mParentInstance);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public int getPasswordQuality(ComponentName admin) {
        return getPasswordQuality(admin, myUserId());
    }

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public int getPasswordQuality(ComponentName admin, int userHandle) {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return 0;
        }
        try {
            return iDevicePolicyManager.getPasswordQuality(admin, userHandle, this.mParentInstance);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setPasswordMinimumLength(ComponentName admin, int length) {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                iDevicePolicyManager.setPasswordMinimumLength(admin, length, this.mParentInstance);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public int getPasswordMinimumLength(ComponentName admin) {
        return getPasswordMinimumLength(admin, myUserId());
    }

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public int getPasswordMinimumLength(ComponentName admin, int userHandle) {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return 0;
        }
        try {
            return iDevicePolicyManager.getPasswordMinimumLength(admin, userHandle, this.mParentInstance);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setPasswordMinimumUpperCase(ComponentName admin, int length) {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                iDevicePolicyManager.setPasswordMinimumUpperCase(admin, length, this.mParentInstance);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public int getPasswordMinimumUpperCase(ComponentName admin) {
        return getPasswordMinimumUpperCase(admin, myUserId());
    }

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public int getPasswordMinimumUpperCase(ComponentName admin, int userHandle) {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return 0;
        }
        try {
            return iDevicePolicyManager.getPasswordMinimumUpperCase(admin, userHandle, this.mParentInstance);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setPasswordMinimumLowerCase(ComponentName admin, int length) {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                iDevicePolicyManager.setPasswordMinimumLowerCase(admin, length, this.mParentInstance);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public int getPasswordMinimumLowerCase(ComponentName admin) {
        return getPasswordMinimumLowerCase(admin, myUserId());
    }

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public int getPasswordMinimumLowerCase(ComponentName admin, int userHandle) {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return 0;
        }
        try {
            return iDevicePolicyManager.getPasswordMinimumLowerCase(admin, userHandle, this.mParentInstance);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setPasswordMinimumLetters(ComponentName admin, int length) {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                iDevicePolicyManager.setPasswordMinimumLetters(admin, length, this.mParentInstance);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public int getPasswordMinimumLetters(ComponentName admin) {
        return getPasswordMinimumLetters(admin, myUserId());
    }

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public int getPasswordMinimumLetters(ComponentName admin, int userHandle) {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return 0;
        }
        try {
            return iDevicePolicyManager.getPasswordMinimumLetters(admin, userHandle, this.mParentInstance);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setPasswordMinimumNumeric(ComponentName admin, int length) {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                iDevicePolicyManager.setPasswordMinimumNumeric(admin, length, this.mParentInstance);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public int getPasswordMinimumNumeric(ComponentName admin) {
        return getPasswordMinimumNumeric(admin, myUserId());
    }

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public int getPasswordMinimumNumeric(ComponentName admin, int userHandle) {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return 0;
        }
        try {
            return iDevicePolicyManager.getPasswordMinimumNumeric(admin, userHandle, this.mParentInstance);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setPasswordMinimumSymbols(ComponentName admin, int length) {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                iDevicePolicyManager.setPasswordMinimumSymbols(admin, length, this.mParentInstance);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public int getPasswordMinimumSymbols(ComponentName admin) {
        return getPasswordMinimumSymbols(admin, myUserId());
    }

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public int getPasswordMinimumSymbols(ComponentName admin, int userHandle) {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return 0;
        }
        try {
            return iDevicePolicyManager.getPasswordMinimumSymbols(admin, userHandle, this.mParentInstance);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setPasswordMinimumNonLetter(ComponentName admin, int length) {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                iDevicePolicyManager.setPasswordMinimumNonLetter(admin, length, this.mParentInstance);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public int getPasswordMinimumNonLetter(ComponentName admin) {
        return getPasswordMinimumNonLetter(admin, myUserId());
    }

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public int getPasswordMinimumNonLetter(ComponentName admin, int userHandle) {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return 0;
        }
        try {
            return iDevicePolicyManager.getPasswordMinimumNonLetter(admin, userHandle, this.mParentInstance);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setPasswordHistoryLength(ComponentName admin, int length) {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                iDevicePolicyManager.setPasswordHistoryLength(admin, length, this.mParentInstance);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void setPasswordExpirationTimeout(ComponentName admin, long timeout) {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                iDevicePolicyManager.setPasswordExpirationTimeout(admin, timeout, this.mParentInstance);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public long getPasswordExpirationTimeout(ComponentName admin) {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return 0;
        }
        try {
            return iDevicePolicyManager.getPasswordExpirationTimeout(admin, myUserId(), this.mParentInstance);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public long getPasswordExpiration(ComponentName admin) {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return 0;
        }
        try {
            return iDevicePolicyManager.getPasswordExpiration(admin, myUserId(), this.mParentInstance);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getPasswordHistoryLength(ComponentName admin) {
        return getPasswordHistoryLength(admin, myUserId());
    }

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public int getPasswordHistoryLength(ComponentName admin, int userHandle) {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return 0;
        }
        try {
            return iDevicePolicyManager.getPasswordHistoryLength(admin, userHandle, this.mParentInstance);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getPasswordMaximumLength(int quality) {
        if (!this.mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_SECURE_LOCK_SCREEN)) {
            return 0;
        }
        return 32;
    }

    public boolean isActivePasswordSufficient() {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return false;
        }
        try {
            return iDevicePolicyManager.isActivePasswordSufficient(myUserId(), this.mParentInstance);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getPasswordComplexity() {
        throwIfParentInstance("getPasswordComplexity");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return 0;
        }
        try {
            return iDevicePolicyManager.getPasswordComplexity();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isUsingUnifiedPassword(ComponentName admin) {
        throwIfParentInstance("isUsingUnifiedPassword");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return true;
        }
        try {
            return iDevicePolicyManager.isUsingUnifiedPassword(admin);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isProfileActivePasswordSufficientForParent(int userHandle) {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return false;
        }
        try {
            return iDevicePolicyManager.isProfileActivePasswordSufficientForParent(userHandle);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getCurrentFailedPasswordAttempts() {
        return getCurrentFailedPasswordAttempts(myUserId());
    }

    @UnsupportedAppUsage
    public int getCurrentFailedPasswordAttempts(int userHandle) {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return -1;
        }
        try {
            return iDevicePolicyManager.getCurrentFailedPasswordAttempts(userHandle, this.mParentInstance);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean getDoNotAskCredentialsOnBoot() {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return false;
        }
        try {
            return iDevicePolicyManager.getDoNotAskCredentialsOnBoot();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setMaximumFailedPasswordsForWipe(ComponentName admin, int num) {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                iDevicePolicyManager.setMaximumFailedPasswordsForWipe(admin, num, this.mParentInstance);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public int getMaximumFailedPasswordsForWipe(ComponentName admin) {
        return getMaximumFailedPasswordsForWipe(admin, myUserId());
    }

    @UnsupportedAppUsage
    public int getMaximumFailedPasswordsForWipe(ComponentName admin, int userHandle) {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return 0;
        }
        try {
            return iDevicePolicyManager.getMaximumFailedPasswordsForWipe(admin, userHandle, this.mParentInstance);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getProfileWithMinimumFailedPasswordsForWipe(int userHandle) {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return -10000;
        }
        try {
            return iDevicePolicyManager.getProfileWithMinimumFailedPasswordsForWipe(userHandle, this.mParentInstance);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean resetPassword(String password, int flags) {
        throwIfParentInstance("resetPassword");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return false;
        }
        try {
            return iDevicePolicyManager.resetPassword(password, flags);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean setResetPasswordToken(ComponentName admin, byte[] token) {
        throwIfParentInstance("setResetPasswordToken");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return false;
        }
        try {
            return iDevicePolicyManager.setResetPasswordToken(admin, token);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean clearResetPasswordToken(ComponentName admin) {
        throwIfParentInstance("clearResetPasswordToken");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return false;
        }
        try {
            return iDevicePolicyManager.clearResetPasswordToken(admin);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isResetPasswordTokenActive(ComponentName admin) {
        throwIfParentInstance("isResetPasswordTokenActive");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return false;
        }
        try {
            return iDevicePolicyManager.isResetPasswordTokenActive(admin);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean resetPasswordWithToken(ComponentName admin, String password, byte[] token, int flags) {
        throwIfParentInstance("resetPassword");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return false;
        }
        try {
            return iDevicePolicyManager.resetPasswordWithToken(admin, password, token, flags);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setMaximumTimeToLock(ComponentName admin, long timeMs) {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                iDevicePolicyManager.setMaximumTimeToLock(admin, timeMs, this.mParentInstance);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public long getMaximumTimeToLock(ComponentName admin) {
        return getMaximumTimeToLock(admin, myUserId());
    }

    @UnsupportedAppUsage
    public long getMaximumTimeToLock(ComponentName admin, int userHandle) {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return 0;
        }
        try {
            return iDevicePolicyManager.getMaximumTimeToLock(admin, userHandle, this.mParentInstance);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setRequiredStrongAuthTimeout(ComponentName admin, long timeoutMs) {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                iDevicePolicyManager.setRequiredStrongAuthTimeout(admin, timeoutMs, this.mParentInstance);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public long getRequiredStrongAuthTimeout(ComponentName admin) {
        return getRequiredStrongAuthTimeout(admin, myUserId());
    }

    @UnsupportedAppUsage
    public long getRequiredStrongAuthTimeout(ComponentName admin, int userId) {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return DEFAULT_STRONG_AUTH_TIMEOUT_MS;
        }
        try {
            return iDevicePolicyManager.getRequiredStrongAuthTimeout(admin, userId, this.mParentInstance);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void lockNow() {
        lockNow(0);
    }

    public void lockNow(int flags) {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                iDevicePolicyManager.lockNow(flags, this.mParentInstance);
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
        Preconditions.checkNotNull(reason, "reason string is null");
        Preconditions.checkStringNotEmpty(reason, "reason string is empty");
        Preconditions.checkArgument((flags & 8) == 0, "WIPE_SILENTLY cannot be set");
        wipeDataInternal(flags, reason.toString());
    }

    private void wipeDataInternal(int flags, String wipeReasonForUser) {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                iDevicePolicyManager.wipeDataWithReason(flags, wipeReasonForUser);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    @UnsupportedAppUsage
    public ComponentName setGlobalProxy(ComponentName admin, Proxy proxySpec, List<String> exclusionList) {
        String hostName;
        String hostSpec;
        String exclSpec;
        throwIfParentInstance("setGlobalProxy");
        if (proxySpec == null) {
            throw new NullPointerException();
        } else if (this.mService == null) {
            return null;
        } else {
            try {
                if (proxySpec.equals(Proxy.NO_PROXY)) {
                    hostSpec = null;
                    hostName = null;
                } else if (proxySpec.type().equals(Proxy.Type.HTTP)) {
                    InetSocketAddress sa = (InetSocketAddress) proxySpec.address();
                    String hostName2 = sa.getHostName();
                    int port = sa.getPort();
                    String hostSpec2 = hostName2 + SettingsStringUtil.DELIMITER + Integer.toString(port);
                    if (exclusionList == null) {
                        exclSpec = "";
                    } else {
                        StringBuilder listBuilder = new StringBuilder();
                        boolean firstDomain = true;
                        for (String exclDomain : exclusionList) {
                            if (!firstDomain) {
                                listBuilder.append(SmsManager.REGEX_PREFIX_DELIMITER);
                            } else {
                                firstDomain = false;
                            }
                            listBuilder.append(exclDomain.trim());
                        }
                        exclSpec = listBuilder.toString();
                    }
                    if (android.net.Proxy.validate(hostName2, Integer.toString(port), exclSpec) == 0) {
                        hostSpec = hostSpec2;
                        hostName = exclSpec;
                    } else {
                        throw new IllegalArgumentException();
                    }
                } else {
                    throw new IllegalArgumentException();
                }
                return this.mService.setGlobalProxy(admin, hostSpec, hostName);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void setRecommendedGlobalProxy(ComponentName admin, ProxyInfo proxyInfo) {
        throwIfParentInstance("setRecommendedGlobalProxy");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                iDevicePolicyManager.setRecommendedGlobalProxy(admin, proxyInfo);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public ComponentName getGlobalProxyAdmin() {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return null;
        }
        try {
            return iDevicePolicyManager.getGlobalProxyAdmin(myUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int setStorageEncryption(ComponentName admin, boolean encrypt) {
        throwIfParentInstance("setStorageEncryption");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return 0;
        }
        try {
            return iDevicePolicyManager.setStorageEncryption(admin, encrypt);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean getStorageEncryption(ComponentName admin) {
        throwIfParentInstance("getStorageEncryption");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return false;
        }
        try {
            return iDevicePolicyManager.getStorageEncryption(admin, myUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getStorageEncryptionStatus() {
        throwIfParentInstance("getStorageEncryptionStatus");
        return getStorageEncryptionStatus(myUserId());
    }

    @UnsupportedAppUsage
    public int getStorageEncryptionStatus(int userHandle) {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return 0;
        }
        try {
            return iDevicePolicyManager.getStorageEncryptionStatus(this.mContext.getPackageName(), userHandle);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean approveCaCert(String alias, int userHandle, boolean approval) {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return false;
        }
        try {
            return iDevicePolicyManager.approveCaCert(alias, userHandle, approval);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isCaCertApproved(String alias, int userHandle) {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return false;
        }
        try {
            return iDevicePolicyManager.isCaCertApproved(alias, userHandle);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean installCaCert(ComponentName admin, byte[] certBuffer) {
        throwIfParentInstance("installCaCert");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return false;
        }
        try {
            return iDevicePolicyManager.installCaCert(admin, this.mContext.getPackageName(), certBuffer);
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
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                iDevicePolicyManager.enforceCanManageCaCerts(admin, this.mContext.getPackageName());
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
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                iDevicePolicyManager.uninstallCaCerts(admin, this.mContext.getPackageName(), (String[]) new TrustedCertificateStore().userAliases().toArray(new String[0]));
            } catch (RemoteException re) {
                throw re.rethrowFromSystemServer();
            }
        }
    }

    public boolean hasCaCertInstalled(ComponentName admin, byte[] certBuffer) {
        throwIfParentInstance("hasCaCertInstalled");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                iDevicePolicyManager.enforceCanManageCaCerts(admin, this.mContext.getPackageName());
                if (getCaCertAlias(certBuffer) != null) {
                    return true;
                }
                return false;
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
        throwIfParentInstance("installKeyPair");
        boolean requestAccess = (flags & 1) == 1;
        boolean isUserSelectable = (flags & 2) == 2;
        try {
            byte[] pemCert = Credentials.convertToPem(certs[0]);
            byte[] pemChain = null;
            if (certs.length > 1) {
                pemChain = Credentials.convertToPem((Certificate[]) Arrays.copyOfRange(certs, 1, certs.length));
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
            throw e.rethrowFromSystemServer();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e5) {
            e = e5;
            Log.w(TAG, "Failed to obtain private key material", e);
            return false;
        } catch (IOException | CertificateException e6) {
            e = e6;
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
                String str = TAG;
                Log.e(str, "Error parsing attestation chain for alias " + alias, e);
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
        } catch (ServiceSpecificException e5) {
            Log.w(TAG, String.format("Key Generation failure: %d", Integer.valueOf(e5.errorCode)));
            if (e5.errorCode != 1) {
                throw new RuntimeException(String.format("Unknown error while generating key: %d", Integer.valueOf(e5.errorCode)));
            }
            throw new StrongBoxUnavailableException("No StrongBox for key generation.");
        }
    }

    public boolean isDeviceIdAttestationSupported() {
        return this.mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_DEVICE_ID_ATTESTATION);
    }

    public boolean setKeyPairCertificate(ComponentName admin, String alias, List<Certificate> certs, boolean isUserSelectable) {
        byte[] pemChain;
        throwIfParentInstance("setKeyPairCertificate");
        try {
            byte[] pemCert = Credentials.convertToPem(certs.get(0));
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
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                iDevicePolicyManager.setCertInstallerPackage(admin, installerPackage);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    @Deprecated
    public String getCertInstallerPackage(ComponentName admin) throws SecurityException {
        throwIfParentInstance("getCertInstallerPackage");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return null;
        }
        try {
            return iDevicePolicyManager.getCertInstallerPackage(admin);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setDelegatedScopes(ComponentName admin, String delegatePackage, List<String> scopes) {
        throwIfParentInstance("setDelegatedScopes");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                iDevicePolicyManager.setDelegatedScopes(admin, delegatePackage, scopes);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public List<String> getDelegatedScopes(ComponentName admin, String delegatedPackage) {
        throwIfParentInstance("getDelegatedScopes");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return null;
        }
        try {
            return iDevicePolicyManager.getDelegatedScopes(admin, delegatedPackage);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<String> getDelegatePackages(ComponentName admin, String delegationScope) {
        throwIfParentInstance("getDelegatePackages");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return null;
        }
        try {
            return iDevicePolicyManager.getDelegatePackages(admin, delegationScope);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setAlwaysOnVpnPackage(ComponentName admin, String vpnPackage, boolean lockdownEnabled) throws PackageManager.NameNotFoundException {
        setAlwaysOnVpnPackage(admin, vpnPackage, lockdownEnabled, Collections.emptySet());
    }

    public void setAlwaysOnVpnPackage(ComponentName admin, String vpnPackage, boolean lockdownEnabled, Set<String> lockdownWhitelist) throws PackageManager.NameNotFoundException {
        ArrayList arrayList;
        throwIfParentInstance("setAlwaysOnVpnPackage");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            if (lockdownWhitelist == null) {
                arrayList = null;
            } else {
                try {
                    arrayList = new ArrayList(lockdownWhitelist);
                } catch (ServiceSpecificException e) {
                    if (e.errorCode != 1) {
                        throw new RuntimeException("Unknown error setting always-on VPN: " + e.errorCode, e);
                    }
                    throw new PackageManager.NameNotFoundException(e.getMessage());
                } catch (RemoteException e2) {
                    throw e2.rethrowFromSystemServer();
                }
            }
            iDevicePolicyManager.setAlwaysOnVpnPackage(admin, vpnPackage, lockdownEnabled, arrayList);
        }
    }

    public boolean isAlwaysOnVpnLockdownEnabled(ComponentName admin) {
        throwIfParentInstance("isAlwaysOnVpnLockdownEnabled");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return false;
        }
        try {
            return iDevicePolicyManager.isAlwaysOnVpnLockdownEnabled(admin);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public Set<String> getAlwaysOnVpnLockdownWhitelist(ComponentName admin) {
        throwIfParentInstance("getAlwaysOnVpnLockdownWhitelist");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        HashSet hashSet = null;
        if (iDevicePolicyManager == null) {
            return null;
        }
        try {
            List<String> whitelist = iDevicePolicyManager.getAlwaysOnVpnLockdownWhitelist(admin);
            if (whitelist != null) {
                hashSet = new HashSet(whitelist);
            }
            return hashSet;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public String getAlwaysOnVpnPackage(ComponentName admin) {
        throwIfParentInstance("getAlwaysOnVpnPackage");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return null;
        }
        try {
            return iDevicePolicyManager.getAlwaysOnVpnPackage(admin);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setCameraDisabled(ComponentName admin, boolean disabled) {
        throwIfParentInstance("setCameraDisabled");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                iDevicePolicyManager.setCameraDisabled(admin, disabled);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public boolean getCameraDisabled(ComponentName admin) {
        throwIfParentInstance("getCameraDisabled");
        return getCameraDisabled(admin, myUserId());
    }

    @UnsupportedAppUsage
    public boolean getCameraDisabled(ComponentName admin, int userHandle) {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return false;
        }
        try {
            return iDevicePolicyManager.getCameraDisabled(admin, userHandle);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean requestBugreport(ComponentName admin) {
        throwIfParentInstance("requestBugreport");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return false;
        }
        try {
            return iDevicePolicyManager.requestBugreport(admin);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean getGuestUserDisabled(ComponentName admin) {
        return false;
    }

    public void setScreenCaptureDisabled(ComponentName admin, boolean disabled) {
        throwIfParentInstance("setScreenCaptureDisabled");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                iDevicePolicyManager.setScreenCaptureDisabled(admin, disabled);
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
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return false;
        }
        try {
            return iDevicePolicyManager.getScreenCaptureDisabled(admin, userHandle);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setAutoTimeRequired(ComponentName admin, boolean required) {
        throwIfParentInstance("setAutoTimeRequired");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                iDevicePolicyManager.setAutoTimeRequired(admin, required);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public boolean getAutoTimeRequired() {
        throwIfParentInstance("getAutoTimeRequired");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return false;
        }
        try {
            return iDevicePolicyManager.getAutoTimeRequired();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setForceEphemeralUsers(ComponentName admin, boolean forceEphemeralUsers) {
        throwIfParentInstance("setForceEphemeralUsers");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                iDevicePolicyManager.setForceEphemeralUsers(admin, forceEphemeralUsers);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public boolean getForceEphemeralUsers(ComponentName admin) {
        throwIfParentInstance("getForceEphemeralUsers");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return false;
        }
        try {
            return iDevicePolicyManager.getForceEphemeralUsers(admin);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setKeyguardDisabledFeatures(ComponentName admin, int which) {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                iDevicePolicyManager.setKeyguardDisabledFeatures(admin, which, this.mParentInstance);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public int getKeyguardDisabledFeatures(ComponentName admin) {
        return getKeyguardDisabledFeatures(admin, myUserId());
    }

    @UnsupportedAppUsage
    public int getKeyguardDisabledFeatures(ComponentName admin, int userHandle) {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return 0;
        }
        try {
            return iDevicePolicyManager.getKeyguardDisabledFeatures(admin, userHandle, this.mParentInstance);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @UnsupportedAppUsage
    public void setActiveAdmin(ComponentName policyReceiver, boolean refreshing, int userHandle) {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                iDevicePolicyManager.setActiveAdmin(policyReceiver, refreshing, userHandle);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    @UnsupportedAppUsage
    public void setActiveAdmin(ComponentName policyReceiver, boolean refreshing) {
        setActiveAdmin(policyReceiver, refreshing, myUserId());
    }

    public void getRemoveWarning(ComponentName admin, RemoteCallback result) {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                iDevicePolicyManager.getRemoveWarning(admin, result, myUserId());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    @UnsupportedAppUsage
    public void setActivePasswordState(PasswordMetrics metrics, int userHandle) {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                iDevicePolicyManager.setActivePasswordState(metrics, userHandle);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void reportPasswordChanged(int userId) {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                iDevicePolicyManager.reportPasswordChanged(userId);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    @UnsupportedAppUsage
    public void reportFailedPasswordAttempt(int userHandle) {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                iDevicePolicyManager.reportFailedPasswordAttempt(userHandle);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    @UnsupportedAppUsage
    public void reportSuccessfulPasswordAttempt(int userHandle) {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                iDevicePolicyManager.reportSuccessfulPasswordAttempt(userHandle);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void reportFailedBiometricAttempt(int userHandle) {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                iDevicePolicyManager.reportFailedBiometricAttempt(userHandle);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void reportSuccessfulBiometricAttempt(int userHandle) {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                iDevicePolicyManager.reportSuccessfulBiometricAttempt(userHandle);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void reportKeyguardDismissed(int userHandle) {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                iDevicePolicyManager.reportKeyguardDismissed(userHandle);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void reportKeyguardSecured(int userHandle) {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                iDevicePolicyManager.reportKeyguardSecured(userHandle);
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
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return false;
        }
        try {
            return iDevicePolicyManager.setDeviceOwner(who, ownerName, userId);
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
        ComponentName deviceOwner;
        if (packageName == null || (deviceOwner = getDeviceOwnerComponentInner(callingUserOnly)) == null) {
            return false;
        }
        return packageName.equals(deviceOwner.getPackageName());
    }

    private ComponentName getDeviceOwnerComponentInner(boolean callingUserOnly) {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return null;
        }
        try {
            return iDevicePolicyManager.getDeviceOwnerComponent(callingUserOnly);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public UserHandle getDeviceOwnerUser() {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return null;
        }
        try {
            int userId = iDevicePolicyManager.getDeviceOwnerUserId();
            if (userId != -10000) {
                return UserHandle.of(userId);
            }
            return null;
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public int getDeviceOwnerUserId() {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return -10000;
        }
        try {
            return iDevicePolicyManager.getDeviceOwnerUserId();
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public void clearDeviceOwnerApp(String packageName) {
        throwIfParentInstance("clearDeviceOwnerApp");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                iDevicePolicyManager.clearDeviceOwner(packageName);
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

    @SystemApi
    @SuppressLint({"Doclava125"})
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
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return null;
        }
        try {
            return iDevicePolicyManager.getDeviceOwnerName();
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    @SystemApi
    @SuppressLint({"Doclava125"})
    @Deprecated
    public String getDeviceInitializerApp() {
        return null;
    }

    @SystemApi
    @SuppressLint({"Doclava125"})
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
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                iDevicePolicyManager.clearProfileOwner(admin);
            } catch (RemoteException re) {
                throw re.rethrowFromSystemServer();
            }
        }
    }

    public boolean hasUserSetupCompleted() {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return true;
        }
        try {
            return iDevicePolicyManager.hasUserSetupCompleted();
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
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                iDevicePolicyManager.setDeviceOwnerLockScreenInfo(admin, info);
            } catch (RemoteException re) {
                throw re.rethrowFromSystemServer();
            }
        }
    }

    public CharSequence getDeviceOwnerLockScreenInfo() {
        throwIfParentInstance("getDeviceOwnerLockScreenInfo");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return null;
        }
        try {
            return iDevicePolicyManager.getDeviceOwnerLockScreenInfo();
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public String[] setPackagesSuspended(ComponentName admin, String[] packageNames, boolean suspended) {
        throwIfParentInstance("setPackagesSuspended");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return packageNames;
        }
        try {
            return iDevicePolicyManager.setPackagesSuspended(admin, this.mContext.getPackageName(), packageNames, suspended);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public boolean isPackageSuspended(ComponentName admin, String packageName) throws PackageManager.NameNotFoundException {
        throwIfParentInstance("isPackageSuspended");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return false;
        }
        try {
            return iDevicePolicyManager.isPackageSuspended(admin, this.mContext.getPackageName(), packageName);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        } catch (IllegalArgumentException e2) {
            throw new PackageManager.NameNotFoundException(packageName);
        }
    }

    public void setProfileEnabled(ComponentName admin) {
        throwIfParentInstance("setProfileEnabled");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                iDevicePolicyManager.setProfileEnabled(admin);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void setProfileName(ComponentName admin, String profileName) {
        throwIfParentInstance("setProfileName");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                iDevicePolicyManager.setProfileName(admin, profileName);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public boolean isProfileOwnerApp(String packageName) {
        throwIfParentInstance("isProfileOwnerApp");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return false;
        }
        try {
            ComponentName profileOwner = iDevicePolicyManager.getProfileOwner(myUserId());
            if (profileOwner == null || !profileOwner.getPackageName().equals(packageName)) {
                return false;
            }
            return true;
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public ComponentName getProfileOwner() throws IllegalArgumentException {
        throwIfParentInstance("getProfileOwner");
        return getProfileOwnerAsUser(this.mContext.getUserId());
    }

    public ComponentName getProfileOwnerAsUser(UserHandle user) {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return null;
        }
        try {
            return iDevicePolicyManager.getProfileOwnerAsUser(user.getIdentifier());
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    @UnsupportedAppUsage
    public ComponentName getProfileOwnerAsUser(int userId) {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return null;
        }
        try {
            return iDevicePolicyManager.getProfileOwnerAsUser(userId);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public String getProfileOwnerName() throws IllegalArgumentException {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return null;
        }
        try {
            return iDevicePolicyManager.getProfileOwnerName(this.mContext.getUserId());
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public String getProfileOwnerNameAsUser(int userId) throws IllegalArgumentException {
        throwIfParentInstance("getProfileOwnerNameAsUser");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return null;
        }
        try {
            return iDevicePolicyManager.getProfileOwnerName(userId);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public boolean checkDeviceIdentifierAccess(String packageName, int pid, int uid) {
        IDevicePolicyManager iDevicePolicyManager;
        throwIfParentInstance("checkDeviceIdentifierAccess");
        if (packageName == null || (iDevicePolicyManager = this.mService) == null) {
            return false;
        }
        try {
            return iDevicePolicyManager.checkDeviceIdentifierAccess(packageName, pid, uid);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public void addPersistentPreferredActivity(ComponentName admin, IntentFilter filter, ComponentName activity) {
        throwIfParentInstance("addPersistentPreferredActivity");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                iDevicePolicyManager.addPersistentPreferredActivity(admin, filter, activity);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void clearPackagePersistentPreferredActivities(ComponentName admin, String packageName) {
        throwIfParentInstance("clearPackagePersistentPreferredActivities");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                iDevicePolicyManager.clearPackagePersistentPreferredActivities(admin, packageName);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void setDefaultSmsApplication(ComponentName admin, String packageName) {
        throwIfParentInstance("setDefaultSmsApplication");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                iDevicePolicyManager.setDefaultSmsApplication(admin, packageName);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    @Deprecated
    public void setApplicationRestrictionsManagingPackage(ComponentName admin, String packageName) throws PackageManager.NameNotFoundException {
        throwIfParentInstance("setApplicationRestrictionsManagingPackage");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                if (!iDevicePolicyManager.setApplicationRestrictionsManagingPackage(admin, packageName)) {
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
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return null;
        }
        try {
            return iDevicePolicyManager.getApplicationRestrictionsManagingPackage(admin);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public boolean isCallerApplicationRestrictionsManagingPackage() {
        throwIfParentInstance("isCallerApplicationRestrictionsManagingPackage");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return false;
        }
        try {
            return iDevicePolicyManager.isCallerApplicationRestrictionsManagingPackage(this.mContext.getPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setApplicationRestrictions(ComponentName admin, String packageName, Bundle settings) {
        throwIfParentInstance("setApplicationRestrictions");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                iDevicePolicyManager.setApplicationRestrictions(admin, this.mContext.getPackageName(), packageName, settings);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void setTrustAgentConfiguration(ComponentName admin, ComponentName target, PersistableBundle configuration) {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                iDevicePolicyManager.setTrustAgentConfiguration(admin, target, configuration, this.mParentInstance);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public List<PersistableBundle> getTrustAgentConfiguration(ComponentName admin, ComponentName agent) {
        return getTrustAgentConfiguration(admin, agent, myUserId());
    }

    @UnsupportedAppUsage
    public List<PersistableBundle> getTrustAgentConfiguration(ComponentName admin, ComponentName agent, int userHandle) {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return new ArrayList();
        }
        try {
            return iDevicePolicyManager.getTrustAgentConfiguration(admin, agent, userHandle, this.mParentInstance);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setCrossProfileCallerIdDisabled(ComponentName admin, boolean disabled) {
        throwIfParentInstance("setCrossProfileCallerIdDisabled");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                iDevicePolicyManager.setCrossProfileCallerIdDisabled(admin, disabled);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public boolean getCrossProfileCallerIdDisabled(ComponentName admin) {
        throwIfParentInstance("getCrossProfileCallerIdDisabled");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return false;
        }
        try {
            return iDevicePolicyManager.getCrossProfileCallerIdDisabled(admin);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean getCrossProfileCallerIdDisabled(UserHandle userHandle) {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return false;
        }
        try {
            return iDevicePolicyManager.getCrossProfileCallerIdDisabledForUser(userHandle.getIdentifier());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setCrossProfileContactsSearchDisabled(ComponentName admin, boolean disabled) {
        throwIfParentInstance("setCrossProfileContactsSearchDisabled");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                iDevicePolicyManager.setCrossProfileContactsSearchDisabled(admin, disabled);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public boolean getCrossProfileContactsSearchDisabled(ComponentName admin) {
        throwIfParentInstance("getCrossProfileContactsSearchDisabled");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return false;
        }
        try {
            return iDevicePolicyManager.getCrossProfileContactsSearchDisabled(admin);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean getCrossProfileContactsSearchDisabled(UserHandle userHandle) {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return false;
        }
        try {
            return iDevicePolicyManager.getCrossProfileContactsSearchDisabledForUser(userHandle.getIdentifier());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void startManagedQuickContact(String actualLookupKey, long actualContactId, boolean isContactIdIgnored, long directoryId, Intent originalIntent) {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                iDevicePolicyManager.startManagedQuickContact(actualLookupKey, actualContactId, isContactIdIgnored, directoryId, originalIntent);
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
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                iDevicePolicyManager.setBluetoothContactSharingDisabled(admin, disabled);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public boolean getBluetoothContactSharingDisabled(ComponentName admin) {
        throwIfParentInstance("getBluetoothContactSharingDisabled");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return true;
        }
        try {
            return iDevicePolicyManager.getBluetoothContactSharingDisabled(admin);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean getBluetoothContactSharingDisabled(UserHandle userHandle) {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return true;
        }
        try {
            return iDevicePolicyManager.getBluetoothContactSharingDisabledForUser(userHandle.getIdentifier());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void addCrossProfileIntentFilter(ComponentName admin, IntentFilter filter, int flags) {
        throwIfParentInstance("addCrossProfileIntentFilter");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                iDevicePolicyManager.addCrossProfileIntentFilter(admin, filter, flags);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void clearCrossProfileIntentFilters(ComponentName admin) {
        throwIfParentInstance("clearCrossProfileIntentFilters");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                iDevicePolicyManager.clearCrossProfileIntentFilters(admin);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public boolean setPermittedAccessibilityServices(ComponentName admin, List<String> packageNames) {
        throwIfParentInstance("setPermittedAccessibilityServices");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return false;
        }
        try {
            return iDevicePolicyManager.setPermittedAccessibilityServices(admin, packageNames);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<String> getPermittedAccessibilityServices(ComponentName admin) {
        throwIfParentInstance("getPermittedAccessibilityServices");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return null;
        }
        try {
            return iDevicePolicyManager.getPermittedAccessibilityServices(admin);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isAccessibilityServicePermittedByAdmin(ComponentName admin, String packageName, int userHandle) {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return false;
        }
        try {
            return iDevicePolicyManager.isAccessibilityServicePermittedByAdmin(admin, packageName, userHandle);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public List<String> getPermittedAccessibilityServices(int userId) {
        throwIfParentInstance("getPermittedAccessibilityServices");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return null;
        }
        try {
            return iDevicePolicyManager.getPermittedAccessibilityServicesForUser(userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean setPermittedInputMethods(ComponentName admin, List<String> packageNames) {
        throwIfParentInstance("setPermittedInputMethods");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return false;
        }
        try {
            return iDevicePolicyManager.setPermittedInputMethods(admin, packageNames);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<String> getPermittedInputMethods(ComponentName admin) {
        throwIfParentInstance("getPermittedInputMethods");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return null;
        }
        try {
            return iDevicePolicyManager.getPermittedInputMethods(admin);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isInputMethodPermittedByAdmin(ComponentName admin, String packageName, int userHandle) {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return false;
        }
        try {
            return iDevicePolicyManager.isInputMethodPermittedByAdmin(admin, packageName, userHandle);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public List<String> getPermittedInputMethodsForCurrentUser() {
        throwIfParentInstance("getPermittedInputMethodsForCurrentUser");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return null;
        }
        try {
            return iDevicePolicyManager.getPermittedInputMethodsForCurrentUser();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean setPermittedCrossProfileNotificationListeners(ComponentName admin, List<String> packageList) {
        throwIfParentInstance("setPermittedCrossProfileNotificationListeners");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return false;
        }
        try {
            return iDevicePolicyManager.setPermittedCrossProfileNotificationListeners(admin, packageList);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<String> getPermittedCrossProfileNotificationListeners(ComponentName admin) {
        throwIfParentInstance("getPermittedCrossProfileNotificationListeners");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return null;
        }
        try {
            return iDevicePolicyManager.getPermittedCrossProfileNotificationListeners(admin);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isNotificationListenerServicePermitted(String packageName, int userId) {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return true;
        }
        try {
            return iDevicePolicyManager.isNotificationListenerServicePermitted(packageName, userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<String> getKeepUninstalledPackages(ComponentName admin) {
        throwIfParentInstance("getKeepUninstalledPackages");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return null;
        }
        try {
            return iDevicePolicyManager.getKeepUninstalledPackages(admin, this.mContext.getPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setKeepUninstalledPackages(ComponentName admin, List<String> packageNames) {
        throwIfParentInstance("setKeepUninstalledPackages");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                iDevicePolicyManager.setKeepUninstalledPackages(admin, this.mContext.getPackageName(), packageNames);
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
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return null;
        }
        try {
            return iDevicePolicyManager.getApplicationRestrictions(admin, this.mContext.getPackageName(), packageName);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void addUserRestriction(ComponentName admin, String key) {
        throwIfParentInstance("addUserRestriction");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                iDevicePolicyManager.setUserRestriction(admin, key, true);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void clearUserRestriction(ComponentName admin, String key) {
        throwIfParentInstance("clearUserRestriction");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                iDevicePolicyManager.setUserRestriction(admin, key, false);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public Bundle getUserRestrictions(ComponentName admin) {
        throwIfParentInstance("getUserRestrictions");
        Bundle ret = null;
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                ret = iDevicePolicyManager.getUserRestrictions(admin);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
        return ret == null ? new Bundle() : ret;
    }

    public Intent createAdminSupportIntent(String restriction) {
        throwIfParentInstance("createAdminSupportIntent");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return null;
        }
        try {
            return iDevicePolicyManager.createAdminSupportIntent(restriction);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean setApplicationHidden(ComponentName admin, String packageName, boolean hidden) {
        throwIfParentInstance("setApplicationHidden");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return false;
        }
        try {
            return iDevicePolicyManager.setApplicationHidden(admin, this.mContext.getPackageName(), packageName, hidden);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isApplicationHidden(ComponentName admin, String packageName) {
        throwIfParentInstance("isApplicationHidden");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return false;
        }
        try {
            return iDevicePolicyManager.isApplicationHidden(admin, this.mContext.getPackageName(), packageName);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void enableSystemApp(ComponentName admin, String packageName) {
        throwIfParentInstance("enableSystemApp");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                iDevicePolicyManager.enableSystemApp(admin, this.mContext.getPackageName(), packageName);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public int enableSystemApp(ComponentName admin, Intent intent) {
        throwIfParentInstance("enableSystemApp");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return 0;
        }
        try {
            return iDevicePolicyManager.enableSystemAppWithIntent(admin, this.mContext.getPackageName(), intent);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean installExistingPackage(ComponentName admin, String packageName) {
        throwIfParentInstance("installExistingPackage");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return false;
        }
        try {
            return iDevicePolicyManager.installExistingPackage(admin, this.mContext.getPackageName(), packageName);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setAccountManagementDisabled(ComponentName admin, String accountType, boolean disabled) {
        throwIfParentInstance("setAccountManagementDisabled");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                iDevicePolicyManager.setAccountManagementDisabled(admin, accountType, disabled);
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
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return null;
        }
        try {
            return iDevicePolicyManager.getAccountTypesWithManagementDisabledAsUser(userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setLockTaskPackages(ComponentName admin, String[] packages) throws SecurityException {
        throwIfParentInstance("setLockTaskPackages");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                iDevicePolicyManager.setLockTaskPackages(admin, packages);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public String[] getLockTaskPackages(ComponentName admin) {
        throwIfParentInstance("getLockTaskPackages");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return new String[0];
        }
        try {
            return iDevicePolicyManager.getLockTaskPackages(admin);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isLockTaskPermitted(String pkg) {
        throwIfParentInstance("isLockTaskPermitted");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return false;
        }
        try {
            return iDevicePolicyManager.isLockTaskPermitted(pkg);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setLockTaskFeatures(ComponentName admin, int flags) {
        throwIfParentInstance("setLockTaskFeatures");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                iDevicePolicyManager.setLockTaskFeatures(admin, flags);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public int getLockTaskFeatures(ComponentName admin) {
        throwIfParentInstance("getLockTaskFeatures");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return 0;
        }
        try {
            return iDevicePolicyManager.getLockTaskFeatures(admin);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setGlobalSetting(ComponentName admin, String setting, String value) {
        throwIfParentInstance("setGlobalSetting");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                iDevicePolicyManager.setGlobalSetting(admin, setting, value);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void setSystemSetting(ComponentName admin, String setting, String value) {
        throwIfParentInstance("setSystemSetting");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                iDevicePolicyManager.setSystemSetting(admin, setting, value);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public boolean setTime(ComponentName admin, long millis) {
        throwIfParentInstance("setTime");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return false;
        }
        try {
            return iDevicePolicyManager.setTime(admin, millis);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean setTimeZone(ComponentName admin, String timeZone) {
        throwIfParentInstance("setTimeZone");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return false;
        }
        try {
            return iDevicePolicyManager.setTimeZone(admin, timeZone);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setSecureSetting(ComponentName admin, String setting, String value) {
        throwIfParentInstance("setSecureSetting");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                iDevicePolicyManager.setSecureSetting(admin, setting, value);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void setRestrictionsProvider(ComponentName admin, ComponentName provider) {
        throwIfParentInstance("setRestrictionsProvider");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                iDevicePolicyManager.setRestrictionsProvider(admin, provider);
            } catch (RemoteException re) {
                throw re.rethrowFromSystemServer();
            }
        }
    }

    public void setMasterVolumeMuted(ComponentName admin, boolean on) {
        throwIfParentInstance("setMasterVolumeMuted");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                iDevicePolicyManager.setMasterVolumeMuted(admin, on);
            } catch (RemoteException re) {
                throw re.rethrowFromSystemServer();
            }
        }
    }

    public boolean isMasterVolumeMuted(ComponentName admin) {
        throwIfParentInstance("isMasterVolumeMuted");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return false;
        }
        try {
            return iDevicePolicyManager.isMasterVolumeMuted(admin);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public void setUninstallBlocked(ComponentName admin, String packageName, boolean uninstallBlocked) {
        throwIfParentInstance("setUninstallBlocked");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                iDevicePolicyManager.setUninstallBlocked(admin, this.mContext.getPackageName(), packageName, uninstallBlocked);
            } catch (RemoteException re) {
                throw re.rethrowFromSystemServer();
            }
        }
    }

    public boolean isUninstallBlocked(ComponentName admin, String packageName) {
        throwIfParentInstance("isUninstallBlocked");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return false;
        }
        try {
            return iDevicePolicyManager.isUninstallBlocked(admin, packageName);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public boolean addCrossProfileWidgetProvider(ComponentName admin, String packageName) {
        throwIfParentInstance("addCrossProfileWidgetProvider");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return false;
        }
        try {
            return iDevicePolicyManager.addCrossProfileWidgetProvider(admin, packageName);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public boolean removeCrossProfileWidgetProvider(ComponentName admin, String packageName) {
        throwIfParentInstance("removeCrossProfileWidgetProvider");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return false;
        }
        try {
            return iDevicePolicyManager.removeCrossProfileWidgetProvider(admin, packageName);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public List<String> getCrossProfileWidgetProviders(ComponentName admin) {
        throwIfParentInstance("getCrossProfileWidgetProviders");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                List<String> providers = iDevicePolicyManager.getCrossProfileWidgetProviders(admin);
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
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                iDevicePolicyManager.setSystemUpdatePolicy(admin, policy);
            } catch (RemoteException re) {
                throw re.rethrowFromSystemServer();
            }
        }
    }

    public SystemUpdatePolicy getSystemUpdatePolicy() {
        throwIfParentInstance("getSystemUpdatePolicy");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return null;
        }
        try {
            return iDevicePolicyManager.getSystemUpdatePolicy();
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public void clearSystemUpdatePolicyFreezePeriodRecord() {
        throwIfParentInstance("clearSystemUpdatePolicyFreezePeriodRecord");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                iDevicePolicyManager.clearSystemUpdatePolicyFreezePeriodRecord();
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
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                iDevicePolicyManager.notifyPendingSystemUpdate(SystemUpdateInfo.of(updateReceivedTime));
            } catch (RemoteException re) {
                throw re.rethrowFromSystemServer();
            }
        }
    }

    @SystemApi
    public void notifyPendingSystemUpdate(long updateReceivedTime, boolean isSecurityPatch) {
        throwIfParentInstance("notifyPendingSystemUpdate");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                iDevicePolicyManager.notifyPendingSystemUpdate(SystemUpdateInfo.of(updateReceivedTime, isSecurityPatch));
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
            CompletableFuture<Boolean> result = new CompletableFuture<>();
            this.mService.setPermissionGrantState(admin, this.mContext.getPackageName(), packageName, permission, grantState, new RemoteCallback(new RemoteCallback.OnResultListener(result) {
                /* class android.app.admin.$$Lambda$DevicePolicyManager$w2TynM9H41ejac4JVpNbnemNVWk */
                private final /* synthetic */ CompletableFuture f$0;

                {
                    this.f$0 = r1;
                }

                @Override // android.os.RemoteCallback.OnResultListener
                public final void onResult(Bundle bundle) {
                    DevicePolicyManager.lambda$setPermissionGrantState$0(this.f$0, bundle);
                }
            }));
            BackgroundThread.getHandler().sendMessageDelayed(PooledLambda.obtainMessage($$Lambda$pWaRScwKTZTgGW4Wa_v5R_pKBDU.INSTANCE, result, false), 20000);
            return result.get().booleanValue();
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    static /* synthetic */ void lambda$setPermissionGrantState$0(CompletableFuture result, Bundle b) {
        result.complete(Boolean.valueOf(b != null));
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
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                iDevicePolicyManager.setShortSupportMessage(admin, message);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public CharSequence getShortSupportMessage(ComponentName admin) {
        throwIfParentInstance("getShortSupportMessage");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return null;
        }
        try {
            return iDevicePolicyManager.getShortSupportMessage(admin);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setLongSupportMessage(ComponentName admin, CharSequence message) {
        throwIfParentInstance("setLongSupportMessage");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                iDevicePolicyManager.setLongSupportMessage(admin, message);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public CharSequence getLongSupportMessage(ComponentName admin) {
        throwIfParentInstance("getLongSupportMessage");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return null;
        }
        try {
            return iDevicePolicyManager.getLongSupportMessage(admin);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public CharSequence getShortSupportMessageForUser(ComponentName admin, int userHandle) {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return null;
        }
        try {
            return iDevicePolicyManager.getShortSupportMessageForUser(admin, userHandle);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public CharSequence getLongSupportMessageForUser(ComponentName admin, int userHandle) {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return null;
        }
        try {
            return iDevicePolicyManager.getLongSupportMessageForUser(admin, userHandle);
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

    public long forceNetworkLogs() {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return -1;
        }
        try {
            return iDevicePolicyManager.forceNetworkLogs();
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public long forceSecurityLogs() {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return 0;
        }
        try {
            return iDevicePolicyManager.forceSecurityLogs();
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
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return packageNames;
        }
        try {
            return iDevicePolicyManager.setMeteredDataDisabledPackages(admin, packageNames);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public List<String> getMeteredDataDisabledPackages(ComponentName admin) {
        throwIfParentInstance("getMeteredDataDisabled");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return new ArrayList();
        }
        try {
            return iDevicePolicyManager.getMeteredDataDisabledPackages(admin);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public boolean isMeteredDataDisabledPackageForUser(ComponentName admin, String packageName, int userId) {
        throwIfParentInstance("getMeteredDataDisabledForUser");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return false;
        }
        try {
            return iDevicePolicyManager.isMeteredDataDisabledPackageForUser(admin, packageName, userId);
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

    @SystemApi
    @SuppressLint({"Doclava125"})
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
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return 0;
        }
        try {
            return iDevicePolicyManager.getUserProvisioningState();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setUserProvisioningState(int state, int userHandle) {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                iDevicePolicyManager.setUserProvisioningState(state, userHandle);
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

    @UnsupportedAppUsage
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

    public void setNetworkLoggingEnabled(ComponentName admin, boolean enabled) {
        throwIfParentInstance("setNetworkLoggingEnabled");
        try {
            this.mService.setNetworkLoggingEnabled(admin, this.mContext.getPackageName(), enabled);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public boolean isNetworkLoggingEnabled(ComponentName admin) {
        throwIfParentInstance("isNetworkLoggingEnabled");
        try {
            return this.mService.isNetworkLoggingEnabled(admin, this.mContext.getPackageName());
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public List<NetworkEvent> retrieveNetworkLogs(ComponentName admin, long batchToken) {
        throwIfParentInstance("retrieveNetworkLogs");
        try {
            return this.mService.retrieveNetworkLogs(admin, this.mContext.getPackageName(), batchToken);
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
                /* class android.app.admin.DevicePolicyManager.AnonymousClass1 */

                @Override // android.content.pm.IPackageDataObserver
                public void onRemoveCompleted(String pkg, boolean succeeded) {
                    executor.execute(new Runnable(pkg, succeeded) {
                        /* class android.app.admin.$$Lambda$DevicePolicyManager$1$k6Rmp3Fg9FFATYRU5Z7rHDXGemA */
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
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return -1;
        }
        try {
            return iDevicePolicyManager.addOverrideApn(admin, apnSetting);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean updateOverrideApn(ComponentName admin, int apnId, ApnSetting apnSetting) {
        throwIfParentInstance("updateOverrideApn");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return false;
        }
        try {
            return iDevicePolicyManager.updateOverrideApn(admin, apnId, apnSetting);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean removeOverrideApn(ComponentName admin, int apnId) {
        throwIfParentInstance("removeOverrideApn");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return false;
        }
        try {
            return iDevicePolicyManager.removeOverrideApn(admin, apnId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<ApnSetting> getOverrideApns(ComponentName admin) {
        throwIfParentInstance("getOverrideApns");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return Collections.emptyList();
        }
        try {
            return iDevicePolicyManager.getOverrideApns(admin);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setOverrideApnsEnabled(ComponentName admin, boolean enabled) {
        throwIfParentInstance("setOverrideApnEnabled");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                iDevicePolicyManager.setOverrideApnsEnabled(admin, enabled);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public boolean isOverrideApnEnabled(ComponentName admin) {
        throwIfParentInstance("isOverrideApnEnabled");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return false;
        }
        try {
            return iDevicePolicyManager.isOverrideApnEnabled(admin);
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

    public int setGlobalPrivateDnsModeOpportunistic(ComponentName admin) {
        throwIfParentInstance("setGlobalPrivateDnsModeOpportunistic");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return 2;
        }
        try {
            return iDevicePolicyManager.setGlobalPrivateDns(admin, 2, null);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public int setGlobalPrivateDnsModeSpecifiedHost(ComponentName admin, String privateDnsHost) {
        throwIfParentInstance("setGlobalPrivateDnsModeSpecifiedHost");
        Preconditions.checkNotNull(privateDnsHost, "dns resolver is null");
        if (this.mService == null) {
            return 2;
        }
        if (NetworkUtils.isWeaklyValidatedHostname(privateDnsHost) && !PrivateDnsConnectivityChecker.canConnectToPrivateDnsServer(privateDnsHost)) {
            return 1;
        }
        try {
            return this.mService.setGlobalPrivateDns(admin, 3, privateDnsHost);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x002a, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x002b, code lost:
        if (r0 != null) goto L_0x002d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:?, code lost:
        r0.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0031, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0032, code lost:
        r1.addSuppressed(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0035, code lost:
        throw r2;
     */
    public void installSystemUpdate(ComponentName admin, Uri updateFilePath, final Executor executor, final InstallSystemUpdateCallback callback) {
        throwIfParentInstance("installUpdate");
        if (this.mService != null) {
            try {
                ParcelFileDescriptor fileDescriptor = this.mContext.getContentResolver().openFileDescriptor(updateFilePath, "r");
                this.mService.installUpdateFromFile(admin, fileDescriptor, new StartInstallingUpdateCallback.Stub() {
                    /* class android.app.admin.DevicePolicyManager.AnonymousClass2 */

                    @Override // android.app.admin.StartInstallingUpdateCallback
                    public void onStartInstallingUpdateError(int errorCode, String errorMessage) {
                        DevicePolicyManager.this.executeCallback(errorCode, errorMessage, executor, callback);
                    }
                });
                if (fileDescriptor != null) {
                    fileDescriptor.close();
                }
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            } catch (FileNotFoundException e2) {
                Log.w(TAG, e2);
                executeCallback(4, Log.getStackTraceString(e2), executor, callback);
            } catch (IOException e3) {
                Log.w(TAG, e3);
                executeCallback(1, Log.getStackTraceString(e3), executor, callback);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void executeCallback(int errorCode, String errorMessage, Executor executor, InstallSystemUpdateCallback callback) {
        executor.execute(new Runnable(errorCode, errorMessage) {
            /* class android.app.admin.$$Lambda$DevicePolicyManager$aBAov4sAc4DWENs1hCXh31NAg0 */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ String f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                DevicePolicyManager.InstallSystemUpdateCallback.this.onInstallUpdateError(this.f$1, this.f$2);
            }
        });
    }

    public int getGlobalPrivateDnsMode(ComponentName admin) {
        throwIfParentInstance("setGlobalPrivateDns");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return 0;
        }
        try {
            return iDevicePolicyManager.getGlobalPrivateDnsMode(admin);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public String getGlobalPrivateDnsHost(ComponentName admin) {
        throwIfParentInstance("setGlobalPrivateDns");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return null;
        }
        try {
            return iDevicePolicyManager.getGlobalPrivateDnsHost(admin);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public void setProfileOwnerCanAccessDeviceIds(ComponentName who) {
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            try {
                iDevicePolicyManager.grantDeviceIdsAccessToProfileOwner(who, myUserId());
            } catch (RemoteException re) {
                throw re.rethrowFromSystemServer();
            }
        }
    }

    public void setCrossProfileCalendarPackages(ComponentName admin, Set<String> packageNames) {
        ArrayList arrayList;
        throwIfParentInstance("setCrossProfileCalendarPackages");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager != null) {
            if (packageNames == null) {
                arrayList = null;
            } else {
                try {
                    arrayList = new ArrayList(packageNames);
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            }
            iDevicePolicyManager.setCrossProfileCalendarPackages(admin, arrayList);
        }
    }

    public Set<String> getCrossProfileCalendarPackages(ComponentName admin) {
        throwIfParentInstance("getCrossProfileCalendarPackages");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return Collections.emptySet();
        }
        try {
            List<String> packageNames = iDevicePolicyManager.getCrossProfileCalendarPackages(admin);
            if (packageNames == null) {
                return null;
            }
            return new ArraySet(packageNames);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isPackageAllowedToAccessCalendar(String packageName) {
        throwIfParentInstance("isPackageAllowedToAccessCalendar");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return false;
        }
        try {
            return iDevicePolicyManager.isPackageAllowedToAccessCalendarForUser(packageName, myUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public Set<String> getCrossProfileCalendarPackages() {
        throwIfParentInstance("getCrossProfileCalendarPackages");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return Collections.emptySet();
        }
        try {
            List<String> packageNames = iDevicePolicyManager.getCrossProfileCalendarPackagesForUser(myUserId());
            if (packageNames == null) {
                return null;
            }
            return new ArraySet(packageNames);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public boolean isManagedKiosk() {
        throwIfParentInstance("isManagedKiosk");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return false;
        }
        try {
            return iDevicePolicyManager.isManagedKiosk();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public boolean isUnattendedManagedKiosk() {
        throwIfParentInstance("isUnattendedManagedKiosk");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return false;
        }
        try {
            return iDevicePolicyManager.isUnattendedManagedKiosk();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean startViewCalendarEventInManagedProfile(long eventId, long start, long end, boolean allDay, int flags) {
        throwIfParentInstance("startViewCalendarEventInManagedProfile");
        IDevicePolicyManager iDevicePolicyManager = this.mService;
        if (iDevicePolicyManager == null) {
            return false;
        }
        try {
            return iDevicePolicyManager.startViewCalendarEventInManagedProfile(this.mContext.getPackageName(), eventId, start, end, allDay, flags);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }
}
