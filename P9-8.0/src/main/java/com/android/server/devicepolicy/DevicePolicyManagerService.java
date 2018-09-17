package com.android.server.devicepolicy;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ActivityManager;
import android.app.ActivityManagerInternal;
import android.app.AlarmManager;
import android.app.AppGlobals;
import android.app.IActivityManager;
import android.app.IApplicationThread;
import android.app.IServiceConnection;
import android.app.Notification.BigTextStyle;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.admin.DeviceAdminInfo;
import android.app.admin.DeviceAdminInfo.PolicyInfo;
import android.app.admin.DevicePolicyManagerInternal;
import android.app.admin.DevicePolicyManagerInternal.OnCrossProfileWidgetProvidersChangeListener;
import android.app.admin.NetworkEvent;
import android.app.admin.PasswordMetrics;
import android.app.admin.SecurityLog;
import android.app.admin.SecurityLog.SecurityEvent;
import android.app.admin.SystemUpdateInfo;
import android.app.admin.SystemUpdatePolicy;
import android.app.backup.IBackupManager;
import android.app.trust.TrustManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageManagerInternal;
import android.content.pm.ParceledListSlice;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.StringParceledListSlice;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.IAudioService;
import android.net.ConnectivityManager;
import android.net.IIpConnectivityMetrics;
import android.net.IIpConnectivityMetrics.Stub;
import android.net.ProxyInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.os.PersistableBundle;
import android.os.PowerManager;
import android.os.PowerManagerInternal;
import android.os.Process;
import android.os.RecoverySystem;
import android.os.RemoteCallback;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.UserManagerInternal;
import android.os.storage.StorageManager;
import android.provider.ContactsContract.QuickContact;
import android.provider.ContactsInternal;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.security.IKeyChainAliasCallback;
import android.security.IKeyChainService;
import android.security.KeyChain;
import android.security.KeyChain.KeyChainConnection;
import android.service.persistentdata.PersistentDataBlockManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.EventLog;
import android.util.Flog;
import android.util.Log;
import android.util.Pair;
import android.util.Slog;
import android.util.SparseArray;
import android.util.Xml;
import android.view.IWindowManager;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.IAccessibilityManager;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.notification.SystemNotificationChannels;
import com.android.internal.os.BackgroundThread;
import com.android.internal.statusbar.IStatusBarService;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.JournaledFile;
import com.android.internal.util.Preconditions;
import com.android.internal.util.XmlUtils;
import com.android.internal.widget.LockPatternUtils;
import com.android.server.LocalServices;
import com.android.server.SystemService;
import com.android.server.am.HwBroadcastRadarUtil;
import com.android.server.connectivity.NetworkAgentInfo;
import com.android.server.devicepolicy.HwDevicePolicyFactory.IHwDevicePolicyManagerService;
import com.android.server.job.controllers.JobStatus;
import com.android.server.location.LocationFudger;
import com.android.server.pm.UserRestrictionsUtils;
import com.android.server.power.IHwShutdownThread;
import com.google.android.collect.Sets;
import huawei.cust.HwCustUtils;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class DevicePolicyManagerService extends AbsDevicePolicyManagerService {
    private static final String ACTION_EXPIRED_PASSWORD_NOTIFICATION = "com.android.server.ACTION_EXPIRED_PASSWORD_NOTIFICATION";
    private static final String ATTR_ALIAS = "alias";
    private static final String ATTR_APPLICATION_RESTRICTIONS_MANAGER = "application-restrictions-manager";
    private static final String ATTR_DELEGATED_CERT_INSTALLER = "delegated-cert-installer";
    private static final String ATTR_DEVICE_PAIRED = "device-paired";
    private static final String ATTR_DEVICE_PROVISIONING_CONFIG_APPLIED = "device-provisioning-config-applied";
    private static final String ATTR_DISABLED = "disabled";
    private static final String ATTR_ID = "id";
    private static final String ATTR_NAME = "name";
    private static final String ATTR_PERMISSION_POLICY = "permission-policy";
    private static final String ATTR_PERMISSION_PROVIDER = "permission-provider";
    private static final String ATTR_PROVISIONING_STATE = "provisioning-state";
    private static final String ATTR_SETUP_COMPLETE = "setup-complete";
    private static final String ATTR_VALUE = "value";
    private static final String[] DELEGATIONS = new String[]{"delegation-cert-install", "delegation-app-restrictions", "delegation-block-uninstall", "delegation-enable-system-app", "delegation-keep-uninstalled-packages", "delegation-package-access", "delegation-permission-grant"};
    private static final int DEVICE_ADMIN_DEACTIVATE_TIMEOUT = 10000;
    private static final String DEVICE_POLICIES_XML = "device_policies.xml";
    private static final String DO_NOT_ASK_CREDENTIALS_ON_BOOT_XML = "do-not-ask-credentials-on-boot";
    private static final long EXPIRATION_GRACE_PERIOD_MS = (MS_PER_DAY * 5);
    private static final Set<String> GLOBAL_SETTINGS_DEPRECATED = new ArraySet();
    private static final Set<String> GLOBAL_SETTINGS_WHITELIST = new ArraySet();
    protected static final String LOG_TAG = "DevicePolicyManager";
    private static final String LOG_TAG_DEVICE_OWNER = "device-owner";
    private static final String LOG_TAG_PROFILE_OWNER = "profile-owner";
    private static final long MINIMUM_STRONG_AUTH_TIMEOUT_MS = TimeUnit.HOURS.toMillis(1);
    private static final long MS_PER_DAY = TimeUnit.DAYS.toMillis(1);
    private static final int PROFILE_KEYGUARD_FEATURES = 56;
    private static final int PROFILE_KEYGUARD_FEATURES_PROFILE_ONLY = 8;
    private static final String PROPERTY_DEVICE_OWNER_PRESENT = "ro.device_owner";
    private static final int REQUEST_EXPIRE_PASSWORD = 5571;
    private static final Set<String> SECURE_SETTINGS_DEVICEOWNER_WHITELIST = new ArraySet();
    private static final Set<String> SECURE_SETTINGS_WHITELIST = new ArraySet();
    private static final int STATUS_BAR_DISABLE2_MASK = 1;
    private static final int STATUS_BAR_DISABLE_MASK = 34013184;
    private static final String TAG_ACCEPTED_CA_CERTIFICATES = "accepted-ca-certificate";
    private static final String TAG_ADMIN_BROADCAST_PENDING = "admin-broadcast-pending";
    private static final String TAG_AFFILIATION_ID = "affiliation-id";
    private static final String TAG_CURRENT_INPUT_METHOD_SET = "current-ime-set";
    private static final String TAG_INITIALIZATION_BUNDLE = "initialization-bundle";
    private static final String TAG_LAST_BUG_REPORT_REQUEST = "last-bug-report-request";
    private static final String TAG_LAST_NETWORK_LOG_RETRIEVAL = "last-network-log-retrieval";
    private static final String TAG_LAST_SECURITY_LOG_RETRIEVAL = "last-security-log-retrieval";
    private static final String TAG_LOCK_TASK_COMPONENTS = "lock-task-component";
    private static final String TAG_OWNER_INSTALLED_CA_CERT = "owner-installed-ca-cert";
    private static final String TAG_PASSWORD_TOKEN_HANDLE = "password-token";
    private static final String TAG_STATUS_BAR = "statusbar";
    private static final boolean VERBOSE_LOG = false;
    private static HwCustDevicePolicyManagerService mHwCustDevicePolicyManagerService = ((HwCustDevicePolicyManagerService) HwCustUtils.createObj(HwCustDevicePolicyManagerService.class, new Object[0]));
    final Handler mBackgroundHandler;
    private final CertificateMonitor mCertificateMonitor;
    private final DevicePolicyConstants mConstants;
    final Context mContext;
    private final DeviceAdminServiceController mDeviceAdminServiceController;
    final Handler mHandler;
    boolean mHasFeature;
    final IPackageManager mIPackageManager;
    final Injector mInjector;
    boolean mIsMDMDeviceOwnerAPI;
    boolean mIsWatch;
    final LocalService mLocalService;
    private final LockPatternUtils mLockPatternUtils;
    private NetworkLogger mNetworkLogger;
    final Owners mOwners;
    private final Set<Pair<String, Integer>> mPackagesToRemove;
    final BroadcastReceiver mReceiver;
    private final BroadcastReceiver mRemoteBugreportConsentReceiver;
    private final BroadcastReceiver mRemoteBugreportFinishedReceiver;
    private final AtomicBoolean mRemoteBugreportServiceIsActive;
    private final AtomicBoolean mRemoteBugreportSharingAccepted;
    private final Runnable mRemoteBugreportTimeoutRunnable;
    private final SecurityLogMonitor mSecurityLogMonitor;
    private SetupContentObserver mSetupContentObserver;
    final TelephonyManager mTelephonyManager;
    private final Binder mToken;
    final SparseArray<DevicePolicyData> mUserData;
    final UserManager mUserManager;
    final UserManagerInternal mUserManagerInternal;

    static class ActiveAdmin {
        private static final String ATTR_LAST_NETWORK_LOGGING_NOTIFICATION = "last-notification";
        private static final String ATTR_NUM_NETWORK_LOGGING_NOTIFICATIONS = "num-notifications";
        private static final String ATTR_VALUE = "value";
        static final int DEF_KEYGUARD_FEATURES_DISABLED = 0;
        static final int DEF_MAXIMUM_FAILED_PASSWORDS_FOR_WIPE = 0;
        static final int DEF_MAXIMUM_NETWORK_LOGGING_NOTIFICATIONS_SHOWN = 2;
        static final long DEF_MAXIMUM_TIME_TO_UNLOCK = 0;
        static final int DEF_MINIMUM_PASSWORD_LENGTH = 0;
        static final int DEF_MINIMUM_PASSWORD_LETTERS = 1;
        static final int DEF_MINIMUM_PASSWORD_LOWER_CASE = 0;
        static final int DEF_MINIMUM_PASSWORD_NON_LETTER = 0;
        static final int DEF_MINIMUM_PASSWORD_NUMERIC = 1;
        static final int DEF_MINIMUM_PASSWORD_SYMBOLS = 1;
        static final int DEF_MINIMUM_PASSWORD_UPPER_CASE = 0;
        static final int DEF_ORGANIZATION_COLOR = Color.parseColor("#00796B");
        static final long DEF_PASSWORD_EXPIRATION_DATE = 0;
        static final long DEF_PASSWORD_EXPIRATION_TIMEOUT = 0;
        static final int DEF_PASSWORD_HISTORY_LENGTH = 0;
        private static final String TAG_ACCOUNT_TYPE = "account-type";
        private static final String TAG_CROSS_PROFILE_WIDGET_PROVIDERS = "cross-profile-widget-providers";
        private static final String TAG_DEFAULT_ENABLED_USER_RESTRICTIONS = "default-enabled-user-restrictions";
        private static final String TAG_DISABLE_ACCOUNT_MANAGEMENT = "disable-account-management";
        private static final String TAG_DISABLE_BLUETOOTH_CONTACT_SHARING = "disable-bt-contacts-sharing";
        private static final String TAG_DISABLE_CALLER_ID = "disable-caller-id";
        private static final String TAG_DISABLE_CAMERA = "disable-camera";
        private static final String TAG_DISABLE_CONTACTS_SEARCH = "disable-contacts-search";
        private static final String TAG_DISABLE_KEYGUARD_FEATURES = "disable-keyguard-features";
        private static final String TAG_DISABLE_SCREEN_CAPTURE = "disable-screen-capture";
        private static final String TAG_ENCRYPTION_REQUESTED = "encryption-requested";
        private static final String TAG_FORCE_EPHEMERAL_USERS = "force_ephemeral_users";
        private static final String TAG_GLOBAL_PROXY_EXCLUSION_LIST = "global-proxy-exclusion-list";
        private static final String TAG_GLOBAL_PROXY_SPEC = "global-proxy-spec";
        private static final String TAG_IS_NETWORK_LOGGING_ENABLED = "is_network_logging_enabled";
        private static final String TAG_KEEP_UNINSTALLED_PACKAGES = "keep-uninstalled-packages";
        private static final String TAG_LONG_SUPPORT_MESSAGE = "long-support-message";
        private static final String TAG_MANAGE_TRUST_AGENT_FEATURES = "manage-trust-agent-features";
        private static final String TAG_MAX_FAILED_PASSWORD_WIPE = "max-failed-password-wipe";
        private static final String TAG_MAX_TIME_TO_UNLOCK = "max-time-to-unlock";
        private static final String TAG_MIN_PASSWORD_LENGTH = "min-password-length";
        private static final String TAG_MIN_PASSWORD_LETTERS = "min-password-letters";
        private static final String TAG_MIN_PASSWORD_LOWERCASE = "min-password-lowercase";
        private static final String TAG_MIN_PASSWORD_NONLETTER = "min-password-nonletter";
        private static final String TAG_MIN_PASSWORD_NUMERIC = "min-password-numeric";
        private static final String TAG_MIN_PASSWORD_SYMBOLS = "min-password-symbols";
        private static final String TAG_MIN_PASSWORD_UPPERCASE = "min-password-uppercase";
        private static final String TAG_ORGANIZATION_COLOR = "organization-color";
        private static final String TAG_ORGANIZATION_NAME = "organization-name";
        private static final String TAG_PACKAGE_LIST_ITEM = "item";
        private static final String TAG_PARENT_ADMIN = "parent-admin";
        private static final String TAG_PASSWORD_EXPIRATION_DATE = "password-expiration-date";
        private static final String TAG_PASSWORD_EXPIRATION_TIMEOUT = "password-expiration-timeout";
        private static final String TAG_PASSWORD_HISTORY_LENGTH = "password-history-length";
        private static final String TAG_PASSWORD_QUALITY = "password-quality";
        private static final String TAG_PERMITTED_ACCESSIBILITY_SERVICES = "permitted-accessiblity-services";
        private static final String TAG_PERMITTED_IMES = "permitted-imes";
        private static final String TAG_PERMITTED_NOTIFICATION_LISTENERS = "permitted-notification-listeners";
        private static final String TAG_POLICIES = "policies";
        private static final String TAG_PROVIDER = "provider";
        private static final String TAG_REQUIRE_AUTO_TIME = "require_auto_time";
        private static final String TAG_RESTRICTION = "restriction";
        private static final String TAG_SHORT_SUPPORT_MESSAGE = "short-support-message";
        private static final String TAG_SPECIFIES_GLOBAL_PROXY = "specifies-global-proxy";
        private static final String TAG_STRONG_AUTH_UNLOCK_TIMEOUT = "strong-auth-unlock-timeout";
        private static final String TAG_TEST_ONLY_ADMIN = "test-only-admin";
        private static final String TAG_TRUST_AGENT_COMPONENT = "component";
        private static final String TAG_TRUST_AGENT_COMPONENT_OPTIONS = "trust-agent-component-options";
        private static final String TAG_USER_RESTRICTIONS = "user-restrictions";
        final Set<String> accountTypesWithManagementDisabled = new ArraySet();
        boolean allowSimplePassword = true;
        List<String> crossProfileWidgetProviders;
        final Set<String> defaultEnabledRestrictionsAlreadySet = new ArraySet();
        boolean disableBluetoothContactSharing = true;
        boolean disableCallerId = false;
        boolean disableCamera = false;
        boolean disableContactsSearch = false;
        boolean disableScreenCapture = false;
        int disabledKeyguardFeatures = 0;
        boolean encryptionRequested = false;
        boolean forceEphemeralUsers = false;
        String globalProxyExclusionList = null;
        String globalProxySpec = null;
        final DeviceAdminInfo info;
        boolean isNetworkLoggingEnabled = false;
        final boolean isParent;
        List<String> keepUninstalledPackages;
        long lastNetworkLoggingNotificationTimeMs = 0;
        CharSequence longSupportMessage = null;
        public HwActiveAdmin mHwActiveAdmin;
        int maximumFailedPasswordsForWipe = 0;
        long maximumTimeToUnlock = 0;
        PasswordMetrics minimumPasswordMetrics = new PasswordMetrics(0, 0, 1, 0, 0, 1, 1, 0);
        int numNetworkLoggingNotifications = 0;
        int organizationColor = DEF_ORGANIZATION_COLOR;
        String organizationName = null;
        ActiveAdmin parentAdmin;
        long passwordExpirationDate = 0;
        long passwordExpirationTimeout = 0;
        int passwordHistoryLength = 0;
        List<String> permittedAccessiblityServices;
        List<String> permittedInputMethods;
        List<String> permittedNotificationListeners;
        boolean requireAutoTime = false;
        CharSequence shortSupportMessage = null;
        boolean specifiesGlobalProxy = false;
        long strongAuthUnlockTimeout = 0;
        boolean testOnlyAdmin = false;
        ArrayMap<String, TrustAgentInfo> trustAgentInfos = new ArrayMap();
        Bundle userRestrictions;

        static class TrustAgentInfo {
            public PersistableBundle options;

            TrustAgentInfo(PersistableBundle bundle) {
                this.options = bundle;
            }
        }

        ActiveAdmin(DeviceAdminInfo _info, boolean parent) {
            this.info = _info;
            this.isParent = parent;
        }

        ActiveAdmin getParentActiveAdmin() {
            Preconditions.checkState(this.isParent ^ 1);
            if (this.parentAdmin == null) {
                this.parentAdmin = new ActiveAdmin(this.info, true);
            }
            return this.parentAdmin;
        }

        boolean hasParentActiveAdmin() {
            return this.parentAdmin != null;
        }

        int getUid() {
            return this.info.getActivityInfo().applicationInfo.uid;
        }

        public UserHandle getUserHandle() {
            return UserHandle.of(UserHandle.getUserId(this.info.getActivityInfo().applicationInfo.uid));
        }

        void writeToXml(XmlSerializer out) throws IllegalArgumentException, IllegalStateException, IOException {
            out.startTag(null, TAG_POLICIES);
            this.info.writePoliciesToXml(out);
            out.endTag(null, TAG_POLICIES);
            if (this.mHwActiveAdmin != null) {
                this.mHwActiveAdmin.writePoliciesToXml(out);
            }
            if (this.minimumPasswordMetrics.quality != 0) {
                out.startTag(null, TAG_PASSWORD_QUALITY);
                out.attribute(null, ATTR_VALUE, Integer.toString(this.minimumPasswordMetrics.quality));
                out.endTag(null, TAG_PASSWORD_QUALITY);
                if (this.minimumPasswordMetrics.length != 0) {
                    out.startTag(null, TAG_MIN_PASSWORD_LENGTH);
                    out.attribute(null, ATTR_VALUE, Integer.toString(this.minimumPasswordMetrics.length));
                    out.endTag(null, TAG_MIN_PASSWORD_LENGTH);
                }
                if (this.passwordHistoryLength != 0) {
                    out.startTag(null, TAG_PASSWORD_HISTORY_LENGTH);
                    out.attribute(null, ATTR_VALUE, Integer.toString(this.passwordHistoryLength));
                    out.endTag(null, TAG_PASSWORD_HISTORY_LENGTH);
                }
                if (this.minimumPasswordMetrics.upperCase != 0) {
                    out.startTag(null, TAG_MIN_PASSWORD_UPPERCASE);
                    out.attribute(null, ATTR_VALUE, Integer.toString(this.minimumPasswordMetrics.upperCase));
                    out.endTag(null, TAG_MIN_PASSWORD_UPPERCASE);
                }
                if (this.minimumPasswordMetrics.lowerCase != 0) {
                    out.startTag(null, TAG_MIN_PASSWORD_LOWERCASE);
                    out.attribute(null, ATTR_VALUE, Integer.toString(this.minimumPasswordMetrics.lowerCase));
                    out.endTag(null, TAG_MIN_PASSWORD_LOWERCASE);
                }
                if (this.minimumPasswordMetrics.letters != 1) {
                    out.startTag(null, TAG_MIN_PASSWORD_LETTERS);
                    out.attribute(null, ATTR_VALUE, Integer.toString(this.minimumPasswordMetrics.letters));
                    out.endTag(null, TAG_MIN_PASSWORD_LETTERS);
                }
                if (this.minimumPasswordMetrics.numeric != 1) {
                    out.startTag(null, TAG_MIN_PASSWORD_NUMERIC);
                    out.attribute(null, ATTR_VALUE, Integer.toString(this.minimumPasswordMetrics.numeric));
                    out.endTag(null, TAG_MIN_PASSWORD_NUMERIC);
                }
                if (this.minimumPasswordMetrics.symbols != 1) {
                    out.startTag(null, TAG_MIN_PASSWORD_SYMBOLS);
                    out.attribute(null, ATTR_VALUE, Integer.toString(this.minimumPasswordMetrics.symbols));
                    out.endTag(null, TAG_MIN_PASSWORD_SYMBOLS);
                }
                if (this.minimumPasswordMetrics.nonLetter > 0) {
                    out.startTag(null, TAG_MIN_PASSWORD_NONLETTER);
                    out.attribute(null, ATTR_VALUE, Integer.toString(this.minimumPasswordMetrics.nonLetter));
                    out.endTag(null, TAG_MIN_PASSWORD_NONLETTER);
                }
                if (DevicePolicyManagerService.mHwCustDevicePolicyManagerService != null && DevicePolicyManagerService.mHwCustDevicePolicyManagerService.isForbiddenSimplePwdFeatureEnable()) {
                    out.startTag(null, "allow-simple-password");
                    out.attribute(null, ATTR_VALUE, Boolean.toString(this.allowSimplePassword));
                    out.endTag(null, "allow-simple-password");
                }
            }
            if (this.maximumTimeToUnlock != 0) {
                out.startTag(null, TAG_MAX_TIME_TO_UNLOCK);
                out.attribute(null, ATTR_VALUE, Long.toString(this.maximumTimeToUnlock));
                out.endTag(null, TAG_MAX_TIME_TO_UNLOCK);
            }
            if (this.strongAuthUnlockTimeout != 259200000) {
                out.startTag(null, TAG_STRONG_AUTH_UNLOCK_TIMEOUT);
                out.attribute(null, ATTR_VALUE, Long.toString(this.strongAuthUnlockTimeout));
                out.endTag(null, TAG_STRONG_AUTH_UNLOCK_TIMEOUT);
            }
            if (this.maximumFailedPasswordsForWipe != 0) {
                out.startTag(null, TAG_MAX_FAILED_PASSWORD_WIPE);
                out.attribute(null, ATTR_VALUE, Integer.toString(this.maximumFailedPasswordsForWipe));
                out.endTag(null, TAG_MAX_FAILED_PASSWORD_WIPE);
            }
            if (this.specifiesGlobalProxy) {
                out.startTag(null, TAG_SPECIFIES_GLOBAL_PROXY);
                out.attribute(null, ATTR_VALUE, Boolean.toString(this.specifiesGlobalProxy));
                out.endTag(null, TAG_SPECIFIES_GLOBAL_PROXY);
                if (this.globalProxySpec != null) {
                    out.startTag(null, TAG_GLOBAL_PROXY_SPEC);
                    out.attribute(null, ATTR_VALUE, this.globalProxySpec);
                    out.endTag(null, TAG_GLOBAL_PROXY_SPEC);
                }
                if (this.globalProxyExclusionList != null) {
                    out.startTag(null, TAG_GLOBAL_PROXY_EXCLUSION_LIST);
                    out.attribute(null, ATTR_VALUE, this.globalProxyExclusionList);
                    out.endTag(null, TAG_GLOBAL_PROXY_EXCLUSION_LIST);
                }
            }
            if (this.passwordExpirationTimeout != 0) {
                out.startTag(null, TAG_PASSWORD_EXPIRATION_TIMEOUT);
                out.attribute(null, ATTR_VALUE, Long.toString(this.passwordExpirationTimeout));
                out.endTag(null, TAG_PASSWORD_EXPIRATION_TIMEOUT);
            }
            if (this.passwordExpirationDate != 0) {
                out.startTag(null, TAG_PASSWORD_EXPIRATION_DATE);
                out.attribute(null, ATTR_VALUE, Long.toString(this.passwordExpirationDate));
                out.endTag(null, TAG_PASSWORD_EXPIRATION_DATE);
            }
            if (this.encryptionRequested) {
                out.startTag(null, TAG_ENCRYPTION_REQUESTED);
                out.attribute(null, ATTR_VALUE, Boolean.toString(this.encryptionRequested));
                out.endTag(null, TAG_ENCRYPTION_REQUESTED);
            }
            if (this.testOnlyAdmin) {
                out.startTag(null, TAG_TEST_ONLY_ADMIN);
                out.attribute(null, ATTR_VALUE, Boolean.toString(this.testOnlyAdmin));
                out.endTag(null, TAG_TEST_ONLY_ADMIN);
            }
            if (this.disableCamera) {
                out.startTag(null, TAG_DISABLE_CAMERA);
                out.attribute(null, ATTR_VALUE, Boolean.toString(this.disableCamera));
                out.endTag(null, TAG_DISABLE_CAMERA);
            }
            if (this.disableCallerId) {
                out.startTag(null, TAG_DISABLE_CALLER_ID);
                out.attribute(null, ATTR_VALUE, Boolean.toString(this.disableCallerId));
                out.endTag(null, TAG_DISABLE_CALLER_ID);
            }
            if (this.disableContactsSearch) {
                out.startTag(null, TAG_DISABLE_CONTACTS_SEARCH);
                out.attribute(null, ATTR_VALUE, Boolean.toString(this.disableContactsSearch));
                out.endTag(null, TAG_DISABLE_CONTACTS_SEARCH);
            }
            if (!this.disableBluetoothContactSharing) {
                out.startTag(null, TAG_DISABLE_BLUETOOTH_CONTACT_SHARING);
                out.attribute(null, ATTR_VALUE, Boolean.toString(this.disableBluetoothContactSharing));
                out.endTag(null, TAG_DISABLE_BLUETOOTH_CONTACT_SHARING);
            }
            if (this.disableScreenCapture) {
                out.startTag(null, TAG_DISABLE_SCREEN_CAPTURE);
                out.attribute(null, ATTR_VALUE, Boolean.toString(this.disableScreenCapture));
                out.endTag(null, TAG_DISABLE_SCREEN_CAPTURE);
            }
            if (this.requireAutoTime) {
                out.startTag(null, TAG_REQUIRE_AUTO_TIME);
                out.attribute(null, ATTR_VALUE, Boolean.toString(this.requireAutoTime));
                out.endTag(null, TAG_REQUIRE_AUTO_TIME);
            }
            if (this.forceEphemeralUsers) {
                out.startTag(null, TAG_FORCE_EPHEMERAL_USERS);
                out.attribute(null, ATTR_VALUE, Boolean.toString(this.forceEphemeralUsers));
                out.endTag(null, TAG_FORCE_EPHEMERAL_USERS);
            }
            if (this.isNetworkLoggingEnabled) {
                out.startTag(null, TAG_IS_NETWORK_LOGGING_ENABLED);
                out.attribute(null, ATTR_VALUE, Boolean.toString(this.isNetworkLoggingEnabled));
                out.attribute(null, ATTR_NUM_NETWORK_LOGGING_NOTIFICATIONS, Integer.toString(this.numNetworkLoggingNotifications));
                out.attribute(null, ATTR_LAST_NETWORK_LOGGING_NOTIFICATION, Long.toString(this.lastNetworkLoggingNotificationTimeMs));
                out.endTag(null, TAG_IS_NETWORK_LOGGING_ENABLED);
            }
            if (this.disabledKeyguardFeatures != 0) {
                out.startTag(null, TAG_DISABLE_KEYGUARD_FEATURES);
                out.attribute(null, ATTR_VALUE, Integer.toString(this.disabledKeyguardFeatures));
                out.endTag(null, TAG_DISABLE_KEYGUARD_FEATURES);
            }
            if (!this.accountTypesWithManagementDisabled.isEmpty()) {
                out.startTag(null, TAG_DISABLE_ACCOUNT_MANAGEMENT);
                writeAttributeValuesToXml(out, TAG_ACCOUNT_TYPE, this.accountTypesWithManagementDisabled);
                out.endTag(null, TAG_DISABLE_ACCOUNT_MANAGEMENT);
            }
            if (!this.trustAgentInfos.isEmpty()) {
                Set<Entry<String, TrustAgentInfo>> set = this.trustAgentInfos.entrySet();
                out.startTag(null, TAG_MANAGE_TRUST_AGENT_FEATURES);
                for (Entry<String, TrustAgentInfo> entry : set) {
                    TrustAgentInfo trustAgentInfo = (TrustAgentInfo) entry.getValue();
                    out.startTag(null, TAG_TRUST_AGENT_COMPONENT);
                    out.attribute(null, ATTR_VALUE, (String) entry.getKey());
                    if (trustAgentInfo.options != null) {
                        out.startTag(null, TAG_TRUST_AGENT_COMPONENT_OPTIONS);
                        try {
                            trustAgentInfo.options.saveToXml(out);
                        } catch (XmlPullParserException e) {
                            Log.e(DevicePolicyManagerService.LOG_TAG, "Failed to save TrustAgent options", e);
                        }
                        out.endTag(null, TAG_TRUST_AGENT_COMPONENT_OPTIONS);
                    }
                    out.endTag(null, TAG_TRUST_AGENT_COMPONENT);
                }
                out.endTag(null, TAG_MANAGE_TRUST_AGENT_FEATURES);
            }
            if (!(this.crossProfileWidgetProviders == null || (this.crossProfileWidgetProviders.isEmpty() ^ 1) == 0)) {
                out.startTag(null, TAG_CROSS_PROFILE_WIDGET_PROVIDERS);
                writeAttributeValuesToXml(out, TAG_PROVIDER, this.crossProfileWidgetProviders);
                out.endTag(null, TAG_CROSS_PROFILE_WIDGET_PROVIDERS);
            }
            writePackageListToXml(out, TAG_PERMITTED_ACCESSIBILITY_SERVICES, this.permittedAccessiblityServices);
            writePackageListToXml(out, TAG_PERMITTED_IMES, this.permittedInputMethods);
            writePackageListToXml(out, TAG_PERMITTED_NOTIFICATION_LISTENERS, this.permittedNotificationListeners);
            writePackageListToXml(out, TAG_KEEP_UNINSTALLED_PACKAGES, this.keepUninstalledPackages);
            if (hasUserRestrictions()) {
                UserRestrictionsUtils.writeRestrictions(out, this.userRestrictions, TAG_USER_RESTRICTIONS);
            }
            if (!this.defaultEnabledRestrictionsAlreadySet.isEmpty()) {
                out.startTag(null, TAG_DEFAULT_ENABLED_USER_RESTRICTIONS);
                writeAttributeValuesToXml(out, TAG_RESTRICTION, this.defaultEnabledRestrictionsAlreadySet);
                out.endTag(null, TAG_DEFAULT_ENABLED_USER_RESTRICTIONS);
            }
            if (!TextUtils.isEmpty(this.shortSupportMessage)) {
                out.startTag(null, TAG_SHORT_SUPPORT_MESSAGE);
                out.text(this.shortSupportMessage.toString());
                out.endTag(null, TAG_SHORT_SUPPORT_MESSAGE);
            }
            if (!TextUtils.isEmpty(this.longSupportMessage)) {
                out.startTag(null, TAG_LONG_SUPPORT_MESSAGE);
                out.text(this.longSupportMessage.toString());
                out.endTag(null, TAG_LONG_SUPPORT_MESSAGE);
            }
            if (this.parentAdmin != null) {
                out.startTag(null, TAG_PARENT_ADMIN);
                this.parentAdmin.writeToXml(out);
                out.endTag(null, TAG_PARENT_ADMIN);
            }
            if (this.organizationColor != DEF_ORGANIZATION_COLOR) {
                out.startTag(null, TAG_ORGANIZATION_COLOR);
                out.attribute(null, ATTR_VALUE, Integer.toString(this.organizationColor));
                out.endTag(null, TAG_ORGANIZATION_COLOR);
            }
            if (this.organizationName != null) {
                out.startTag(null, TAG_ORGANIZATION_NAME);
                out.text(this.organizationName);
                out.endTag(null, TAG_ORGANIZATION_NAME);
            }
        }

        void writePackageListToXml(XmlSerializer out, String outerTag, List<String> packageList) throws IllegalArgumentException, IllegalStateException, IOException {
            if (packageList != null) {
                out.startTag(null, outerTag);
                writeAttributeValuesToXml(out, TAG_PACKAGE_LIST_ITEM, packageList);
                out.endTag(null, outerTag);
            }
        }

        void writeAttributeValuesToXml(XmlSerializer out, String tag, Collection<String> values) throws IOException {
            for (String value : values) {
                out.startTag(null, tag);
                out.attribute(null, ATTR_VALUE, value);
                out.endTag(null, tag);
            }
        }

        void readFromXml(XmlPullParser parser) throws XmlPullParserException, IOException {
            int outerDepth = parser.getDepth();
            while (true) {
                int type = parser.next();
                if (type == 1) {
                    return;
                }
                if (type == 3 && parser.getDepth() <= outerDepth) {
                    return;
                }
                if (!(type == 3 || type == 4)) {
                    String tag = parser.getName();
                    if (HwActiveAdmin.TAG_POLICIES.equals(tag)) {
                        this.mHwActiveAdmin = new HwActiveAdmin();
                        this.mHwActiveAdmin.readPoliciesFromXml(parser);
                    } else if (TAG_POLICIES.equals(tag)) {
                        this.info.readPoliciesFromXml(parser);
                    } else if (TAG_PASSWORD_QUALITY.equals(tag)) {
                        this.minimumPasswordMetrics.quality = Integer.parseInt(parser.getAttributeValue(null, ATTR_VALUE));
                    } else if (TAG_MIN_PASSWORD_LENGTH.equals(tag)) {
                        this.minimumPasswordMetrics.length = Integer.parseInt(parser.getAttributeValue(null, ATTR_VALUE));
                    } else if (TAG_PASSWORD_HISTORY_LENGTH.equals(tag)) {
                        this.passwordHistoryLength = Integer.parseInt(parser.getAttributeValue(null, ATTR_VALUE));
                    } else if (TAG_MIN_PASSWORD_UPPERCASE.equals(tag)) {
                        this.minimumPasswordMetrics.upperCase = Integer.parseInt(parser.getAttributeValue(null, ATTR_VALUE));
                    } else if (TAG_MIN_PASSWORD_LOWERCASE.equals(tag)) {
                        this.minimumPasswordMetrics.lowerCase = Integer.parseInt(parser.getAttributeValue(null, ATTR_VALUE));
                    } else if (TAG_MIN_PASSWORD_LETTERS.equals(tag)) {
                        this.minimumPasswordMetrics.letters = Integer.parseInt(parser.getAttributeValue(null, ATTR_VALUE));
                    } else if (TAG_MIN_PASSWORD_NUMERIC.equals(tag)) {
                        this.minimumPasswordMetrics.numeric = Integer.parseInt(parser.getAttributeValue(null, ATTR_VALUE));
                    } else if (TAG_MIN_PASSWORD_SYMBOLS.equals(tag)) {
                        this.minimumPasswordMetrics.symbols = Integer.parseInt(parser.getAttributeValue(null, ATTR_VALUE));
                    } else if (TAG_MIN_PASSWORD_NONLETTER.equals(tag)) {
                        this.minimumPasswordMetrics.nonLetter = Integer.parseInt(parser.getAttributeValue(null, ATTR_VALUE));
                    } else if (TAG_MAX_TIME_TO_UNLOCK.equals(tag)) {
                        this.maximumTimeToUnlock = Long.parseLong(parser.getAttributeValue(null, ATTR_VALUE));
                    } else if (TAG_STRONG_AUTH_UNLOCK_TIMEOUT.equals(tag)) {
                        this.strongAuthUnlockTimeout = Long.parseLong(parser.getAttributeValue(null, ATTR_VALUE));
                    } else if (TAG_MAX_FAILED_PASSWORD_WIPE.equals(tag)) {
                        this.maximumFailedPasswordsForWipe = Integer.parseInt(parser.getAttributeValue(null, ATTR_VALUE));
                    } else if (TAG_SPECIFIES_GLOBAL_PROXY.equals(tag)) {
                        this.specifiesGlobalProxy = Boolean.parseBoolean(parser.getAttributeValue(null, ATTR_VALUE));
                    } else if (TAG_GLOBAL_PROXY_SPEC.equals(tag)) {
                        this.globalProxySpec = parser.getAttributeValue(null, ATTR_VALUE);
                    } else if (TAG_GLOBAL_PROXY_EXCLUSION_LIST.equals(tag)) {
                        this.globalProxyExclusionList = parser.getAttributeValue(null, ATTR_VALUE);
                    } else if (TAG_PASSWORD_EXPIRATION_TIMEOUT.equals(tag)) {
                        this.passwordExpirationTimeout = Long.parseLong(parser.getAttributeValue(null, ATTR_VALUE));
                    } else if (TAG_PASSWORD_EXPIRATION_DATE.equals(tag)) {
                        this.passwordExpirationDate = Long.parseLong(parser.getAttributeValue(null, ATTR_VALUE));
                    } else if (TAG_ENCRYPTION_REQUESTED.equals(tag)) {
                        this.encryptionRequested = Boolean.parseBoolean(parser.getAttributeValue(null, ATTR_VALUE));
                    } else if (TAG_TEST_ONLY_ADMIN.equals(tag)) {
                        this.testOnlyAdmin = Boolean.parseBoolean(parser.getAttributeValue(null, ATTR_VALUE));
                    } else if (TAG_DISABLE_CAMERA.equals(tag)) {
                        this.disableCamera = Boolean.parseBoolean(parser.getAttributeValue(null, ATTR_VALUE));
                    } else if (TAG_DISABLE_CALLER_ID.equals(tag)) {
                        this.disableCallerId = Boolean.parseBoolean(parser.getAttributeValue(null, ATTR_VALUE));
                    } else if (TAG_DISABLE_CONTACTS_SEARCH.equals(tag)) {
                        this.disableContactsSearch = Boolean.parseBoolean(parser.getAttributeValue(null, ATTR_VALUE));
                    } else if (TAG_DISABLE_BLUETOOTH_CONTACT_SHARING.equals(tag)) {
                        this.disableBluetoothContactSharing = Boolean.parseBoolean(parser.getAttributeValue(null, ATTR_VALUE));
                    } else if (TAG_DISABLE_SCREEN_CAPTURE.equals(tag)) {
                        this.disableScreenCapture = Boolean.parseBoolean(parser.getAttributeValue(null, ATTR_VALUE));
                    } else if (TAG_REQUIRE_AUTO_TIME.equals(tag)) {
                        this.requireAutoTime = Boolean.parseBoolean(parser.getAttributeValue(null, ATTR_VALUE));
                    } else if (TAG_FORCE_EPHEMERAL_USERS.equals(tag)) {
                        this.forceEphemeralUsers = Boolean.parseBoolean(parser.getAttributeValue(null, ATTR_VALUE));
                    } else if (TAG_IS_NETWORK_LOGGING_ENABLED.equals(tag)) {
                        this.isNetworkLoggingEnabled = Boolean.parseBoolean(parser.getAttributeValue(null, ATTR_VALUE));
                        this.lastNetworkLoggingNotificationTimeMs = Long.parseLong(parser.getAttributeValue(null, ATTR_LAST_NETWORK_LOGGING_NOTIFICATION));
                        this.numNetworkLoggingNotifications = Integer.parseInt(parser.getAttributeValue(null, ATTR_NUM_NETWORK_LOGGING_NOTIFICATIONS));
                    } else if (TAG_DISABLE_KEYGUARD_FEATURES.equals(tag)) {
                        this.disabledKeyguardFeatures = Integer.parseInt(parser.getAttributeValue(null, ATTR_VALUE));
                    } else if (TAG_DISABLE_ACCOUNT_MANAGEMENT.equals(tag)) {
                        readAttributeValues(parser, TAG_ACCOUNT_TYPE, this.accountTypesWithManagementDisabled);
                    } else if (TAG_MANAGE_TRUST_AGENT_FEATURES.equals(tag)) {
                        this.trustAgentInfos = getAllTrustAgentInfos(parser, tag);
                    } else if (TAG_CROSS_PROFILE_WIDGET_PROVIDERS.equals(tag)) {
                        this.crossProfileWidgetProviders = new ArrayList();
                        readAttributeValues(parser, TAG_PROVIDER, this.crossProfileWidgetProviders);
                    } else if (TAG_PERMITTED_ACCESSIBILITY_SERVICES.equals(tag)) {
                        this.permittedAccessiblityServices = readPackageList(parser, tag);
                    } else if (TAG_PERMITTED_IMES.equals(tag)) {
                        this.permittedInputMethods = readPackageList(parser, tag);
                    } else if (TAG_PERMITTED_NOTIFICATION_LISTENERS.equals(tag)) {
                        this.permittedNotificationListeners = readPackageList(parser, tag);
                    } else if (TAG_KEEP_UNINSTALLED_PACKAGES.equals(tag)) {
                        this.keepUninstalledPackages = readPackageList(parser, tag);
                    } else if (TAG_USER_RESTRICTIONS.equals(tag)) {
                        this.userRestrictions = UserRestrictionsUtils.readRestrictions(parser);
                    } else if (TAG_DEFAULT_ENABLED_USER_RESTRICTIONS.equals(tag)) {
                        readAttributeValues(parser, TAG_RESTRICTION, this.defaultEnabledRestrictionsAlreadySet);
                    } else if (TAG_SHORT_SUPPORT_MESSAGE.equals(tag)) {
                        if (parser.next() == 4) {
                            this.shortSupportMessage = parser.getText();
                        } else {
                            Log.w(DevicePolicyManagerService.LOG_TAG, "Missing text when loading short support message");
                        }
                    } else if (TAG_LONG_SUPPORT_MESSAGE.equals(tag)) {
                        if (parser.next() == 4) {
                            this.longSupportMessage = parser.getText();
                        } else {
                            Log.w(DevicePolicyManagerService.LOG_TAG, "Missing text when loading long support message");
                        }
                    } else if (TAG_PARENT_ADMIN.equals(tag)) {
                        Preconditions.checkState(this.isParent ^ 1);
                        this.parentAdmin = new ActiveAdmin(this.info, true);
                        this.parentAdmin.readFromXml(parser);
                    } else if (TAG_ORGANIZATION_COLOR.equals(tag)) {
                        this.organizationColor = Integer.parseInt(parser.getAttributeValue(null, ATTR_VALUE));
                    } else if (TAG_ORGANIZATION_NAME.equals(tag)) {
                        if (parser.next() == 4) {
                            this.organizationName = parser.getText();
                        } else {
                            Log.w(DevicePolicyManagerService.LOG_TAG, "Missing text when loading organization name");
                        }
                    } else if (DevicePolicyManagerService.mHwCustDevicePolicyManagerService != null && DevicePolicyManagerService.mHwCustDevicePolicyManagerService.isForbiddenSimplePwdFeatureEnable() && "allow-simple-password".equals(tag)) {
                        this.allowSimplePassword = Boolean.parseBoolean(parser.getAttributeValue(null, ATTR_VALUE));
                    } else {
                        Slog.w(DevicePolicyManagerService.LOG_TAG, "Unknown admin tag: " + tag);
                        XmlUtils.skipCurrentTag(parser);
                    }
                }
            }
        }

        private List<String> readPackageList(XmlPullParser parser, String tag) throws XmlPullParserException, IOException {
            List<String> result = new ArrayList();
            int outerDepth = parser.getDepth();
            while (true) {
                int outerType = parser.next();
                if (outerType == 1 || (outerType == 3 && parser.getDepth() <= outerDepth)) {
                    return result;
                }
                if (!(outerType == 3 || outerType == 4)) {
                    String outerTag = parser.getName();
                    if (TAG_PACKAGE_LIST_ITEM.equals(outerTag)) {
                        String packageName = parser.getAttributeValue(null, ATTR_VALUE);
                        if (packageName != null) {
                            result.add(packageName);
                        } else {
                            Slog.w(DevicePolicyManagerService.LOG_TAG, "Package name missing under " + outerTag);
                        }
                    } else {
                        Slog.w(DevicePolicyManagerService.LOG_TAG, "Unknown tag under " + tag + ": " + outerTag);
                    }
                }
            }
            return result;
        }

        private void readAttributeValues(XmlPullParser parser, String tag, Collection<String> result) throws XmlPullParserException, IOException {
            result.clear();
            int outerDepthDAM = parser.getDepth();
            while (true) {
                int typeDAM = parser.next();
                if (typeDAM == 1) {
                    return;
                }
                if (typeDAM == 3 && parser.getDepth() <= outerDepthDAM) {
                    return;
                }
                if (!(typeDAM == 3 || typeDAM == 4)) {
                    String tagDAM = parser.getName();
                    if (tag.equals(tagDAM)) {
                        result.add(parser.getAttributeValue(null, ATTR_VALUE));
                    } else {
                        Slog.e(DevicePolicyManagerService.LOG_TAG, "Expected tag " + tag + " but found " + tagDAM);
                    }
                }
            }
        }

        private ArrayMap<String, TrustAgentInfo> getAllTrustAgentInfos(XmlPullParser parser, String tag) throws XmlPullParserException, IOException {
            int outerDepthDAM = parser.getDepth();
            ArrayMap<String, TrustAgentInfo> result = new ArrayMap();
            while (true) {
                int typeDAM = parser.next();
                if (typeDAM == 1 || (typeDAM == 3 && parser.getDepth() <= outerDepthDAM)) {
                    return result;
                }
                if (!(typeDAM == 3 || typeDAM == 4)) {
                    String tagDAM = parser.getName();
                    if (TAG_TRUST_AGENT_COMPONENT.equals(tagDAM)) {
                        result.put(parser.getAttributeValue(null, ATTR_VALUE), getTrustAgentInfo(parser, tag));
                    } else {
                        Slog.w(DevicePolicyManagerService.LOG_TAG, "Unknown tag under " + tag + ": " + tagDAM);
                    }
                }
            }
            return result;
        }

        private TrustAgentInfo getTrustAgentInfo(XmlPullParser parser, String tag) throws XmlPullParserException, IOException {
            int outerDepthDAM = parser.getDepth();
            TrustAgentInfo result = new TrustAgentInfo(null);
            while (true) {
                int typeDAM = parser.next();
                if (typeDAM == 1 || (typeDAM == 3 && parser.getDepth() <= outerDepthDAM)) {
                    return result;
                }
                if (!(typeDAM == 3 || typeDAM == 4)) {
                    String tagDAM = parser.getName();
                    if (TAG_TRUST_AGENT_COMPONENT_OPTIONS.equals(tagDAM)) {
                        result.options = PersistableBundle.restoreFromXml(parser);
                    } else {
                        Slog.w(DevicePolicyManagerService.LOG_TAG, "Unknown tag under " + tag + ": " + tagDAM);
                    }
                }
            }
            return result;
        }

        boolean hasUserRestrictions() {
            return this.userRestrictions != null && this.userRestrictions.size() > 0;
        }

        Bundle ensureUserRestrictions() {
            if (this.userRestrictions == null) {
                this.userRestrictions = new Bundle();
            }
            return this.userRestrictions;
        }

        void dump(String prefix, PrintWriter pw) {
            pw.print(prefix);
            pw.print("uid=");
            pw.println(getUid());
            pw.print(prefix);
            pw.print("testOnlyAdmin=");
            pw.println(this.testOnlyAdmin);
            pw.print(prefix);
            pw.println("policies:");
            ArrayList<PolicyInfo> pols = this.info.getUsedPolicies();
            if (pols != null) {
                for (int i = 0; i < pols.size(); i++) {
                    pw.print(prefix);
                    pw.print("  ");
                    pw.println(((PolicyInfo) pols.get(i)).tag);
                }
            }
            pw.print(prefix);
            pw.print("passwordQuality=0x");
            pw.println(Integer.toHexString(this.minimumPasswordMetrics.quality));
            pw.print(prefix);
            pw.print("minimumPasswordLength=");
            pw.println(this.minimumPasswordMetrics.length);
            pw.print(prefix);
            pw.print("passwordHistoryLength=");
            pw.println(this.passwordHistoryLength);
            pw.print(prefix);
            pw.print("minimumPasswordUpperCase=");
            pw.println(this.minimumPasswordMetrics.upperCase);
            pw.print(prefix);
            pw.print("minimumPasswordLowerCase=");
            pw.println(this.minimumPasswordMetrics.lowerCase);
            pw.print(prefix);
            pw.print("minimumPasswordLetters=");
            pw.println(this.minimumPasswordMetrics.letters);
            pw.print(prefix);
            pw.print("minimumPasswordNumeric=");
            pw.println(this.minimumPasswordMetrics.numeric);
            pw.print(prefix);
            pw.print("minimumPasswordSymbols=");
            pw.println(this.minimumPasswordMetrics.symbols);
            pw.print(prefix);
            pw.print("minimumPasswordNonLetter=");
            pw.println(this.minimumPasswordMetrics.nonLetter);
            pw.print(prefix);
            pw.print("maximumTimeToUnlock=");
            pw.println(this.maximumTimeToUnlock);
            pw.print(prefix);
            pw.print("strongAuthUnlockTimeout=");
            pw.println(this.strongAuthUnlockTimeout);
            pw.print(prefix);
            pw.print("maximumFailedPasswordsForWipe=");
            pw.println(this.maximumFailedPasswordsForWipe);
            pw.print(prefix);
            pw.print("specifiesGlobalProxy=");
            pw.println(this.specifiesGlobalProxy);
            pw.print(prefix);
            pw.print("passwordExpirationTimeout=");
            pw.println(this.passwordExpirationTimeout);
            pw.print(prefix);
            pw.print("passwordExpirationDate=");
            pw.println(this.passwordExpirationDate);
            if (this.globalProxySpec != null) {
                pw.print(prefix);
                pw.print("globalProxySpec=");
                pw.println(this.globalProxySpec);
            }
            if (this.globalProxyExclusionList != null) {
                pw.print(prefix);
                pw.print("globalProxyEclusionList=");
                pw.println(this.globalProxyExclusionList);
            }
            pw.print(prefix);
            pw.print("encryptionRequested=");
            pw.println(this.encryptionRequested);
            pw.print(prefix);
            pw.print("disableCamera=");
            pw.println(this.disableCamera);
            pw.print(prefix);
            pw.print("disableCallerId=");
            pw.println(this.disableCallerId);
            pw.print(prefix);
            pw.print("disableContactsSearch=");
            pw.println(this.disableContactsSearch);
            pw.print(prefix);
            pw.print("disableBluetoothContactSharing=");
            pw.println(this.disableBluetoothContactSharing);
            pw.print(prefix);
            pw.print("disableScreenCapture=");
            pw.println(this.disableScreenCapture);
            pw.print(prefix);
            pw.print("requireAutoTime=");
            pw.println(this.requireAutoTime);
            pw.print(prefix);
            pw.print("forceEphemeralUsers=");
            pw.println(this.forceEphemeralUsers);
            pw.print(prefix);
            pw.print("isNetworkLoggingEnabled=");
            pw.println(this.isNetworkLoggingEnabled);
            pw.print(prefix);
            pw.print("disabledKeyguardFeatures=");
            pw.println(this.disabledKeyguardFeatures);
            pw.print(prefix);
            pw.print("crossProfileWidgetProviders=");
            pw.println(this.crossProfileWidgetProviders);
            if (this.permittedAccessiblityServices != null) {
                pw.print(prefix);
                pw.print("permittedAccessibilityServices=");
                pw.println(this.permittedAccessiblityServices);
            }
            if (this.permittedInputMethods != null) {
                pw.print(prefix);
                pw.print("permittedInputMethods=");
                pw.println(this.permittedInputMethods);
            }
            if (this.permittedNotificationListeners != null) {
                pw.print(prefix);
                pw.print("permittedNotificationListeners=");
                pw.println(this.permittedNotificationListeners);
            }
            if (this.keepUninstalledPackages != null) {
                pw.print(prefix);
                pw.print("keepUninstalledPackages=");
                pw.println(this.keepUninstalledPackages);
            }
            pw.print(prefix);
            pw.print("organizationColor=");
            pw.println(this.organizationColor);
            if (this.organizationName != null) {
                pw.print(prefix);
                pw.print("organizationName=");
                pw.println(this.organizationName);
            }
            pw.print(prefix);
            pw.println("userRestrictions:");
            UserRestrictionsUtils.dumpRestrictions(pw, prefix + "  ", this.userRestrictions);
            pw.print(prefix);
            pw.print("defaultEnabledRestrictionsAlreadySet=");
            pw.println(this.defaultEnabledRestrictionsAlreadySet);
            pw.print(prefix);
            pw.print("isParent=");
            pw.println(this.isParent);
            if (this.parentAdmin != null) {
                pw.print(prefix);
                pw.println("parentAdmin:");
                this.parentAdmin.dump(prefix + "  ", pw);
            }
        }
    }

    public static class DevicePolicyData {
        boolean doNotAskCredentialsOnBoot = false;
        final ArraySet<String> mAcceptedCaCertificates = new ArraySet();
        PasswordMetrics mActivePasswordMetrics = new PasswordMetrics();
        boolean mAdminBroadcastPending = false;
        final ArrayList<ActiveAdmin> mAdminList = new ArrayList();
        final ArrayMap<ComponentName, ActiveAdmin> mAdminMap = new ArrayMap();
        Set<String> mAffiliationIds = new ArraySet();
        boolean mCurrentInputMethodSet = false;
        final ArrayMap<String, List<String>> mDelegationMap = new ArrayMap();
        boolean mDeviceProvisioningConfigApplied = false;
        int mFailedPasswordAttempts = 0;
        PersistableBundle mInitBundle = null;
        boolean mIsCurrentPwdSimple = true;
        long mLastBugReportRequestTime = -1;
        long mLastMaximumTimeToLock = -1;
        long mLastNetworkLogsRetrievalTime = -1;
        long mLastSecurityLogRetrievalTime = -1;
        List<String> mLockTaskPackages = new ArrayList();
        Set<String> mOwnerInstalledCaCerts = new ArraySet();
        boolean mPaired = false;
        int mPasswordOwner = -1;
        long mPasswordTokenHandle = 0;
        int mPermissionPolicy;
        final ArrayList<ComponentName> mRemovingAdmins = new ArrayList();
        ComponentName mRestrictionsProvider;
        boolean mStatusBarDisabled = false;
        int mUserHandle;
        int mUserProvisioningState;
        boolean mUserSetupComplete = false;

        public DevicePolicyData(int userHandle) {
            this.mUserHandle = userHandle;
        }
    }

    static class Injector {
        public final Context mContext;

        Injector(Context context) {
            this.mContext = context;
        }

        Context createContextAsUser(UserHandle user) throws NameNotFoundException {
            return this.mContext.createPackageContextAsUser(this.mContext.getPackageName(), 0, user);
        }

        Resources getResources() {
            return this.mContext.getResources();
        }

        Owners newOwners() {
            return new Owners(getUserManager(), getUserManagerInternal(), getPackageManagerInternal());
        }

        UserManager getUserManager() {
            return UserManager.get(this.mContext);
        }

        UserManagerInternal getUserManagerInternal() {
            return (UserManagerInternal) LocalServices.getService(UserManagerInternal.class);
        }

        PackageManagerInternal getPackageManagerInternal() {
            return (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class);
        }

        NotificationManager getNotificationManager() {
            return (NotificationManager) this.mContext.getSystemService(NotificationManager.class);
        }

        IIpConnectivityMetrics getIIpConnectivityMetrics() {
            return Stub.asInterface(ServiceManager.getService("connmetrics"));
        }

        PackageManager getPackageManager() {
            return this.mContext.getPackageManager();
        }

        PowerManagerInternal getPowerManagerInternal() {
            return (PowerManagerInternal) LocalServices.getService(PowerManagerInternal.class);
        }

        TelephonyManager getTelephonyManager() {
            return TelephonyManager.from(this.mContext);
        }

        TrustManager getTrustManager() {
            return (TrustManager) this.mContext.getSystemService("trust");
        }

        AlarmManager getAlarmManager() {
            return (AlarmManager) this.mContext.getSystemService(AlarmManager.class);
        }

        IWindowManager getIWindowManager() {
            return IWindowManager.Stub.asInterface(ServiceManager.getService("window"));
        }

        IActivityManager getIActivityManager() {
            return ActivityManager.getService();
        }

        IPackageManager getIPackageManager() {
            return AppGlobals.getPackageManager();
        }

        IBackupManager getIBackupManager() {
            return IBackupManager.Stub.asInterface(ServiceManager.getService("backup"));
        }

        IAudioService getIAudioService() {
            return IAudioService.Stub.asInterface(ServiceManager.getService("audio"));
        }

        boolean isBuildDebuggable() {
            return Build.IS_DEBUGGABLE;
        }

        LockPatternUtils newLockPatternUtils() {
            return new LockPatternUtils(this.mContext);
        }

        boolean storageManagerIsFileBasedEncryptionEnabled() {
            return StorageManager.isFileEncryptedNativeOnly();
        }

        boolean storageManagerIsNonDefaultBlockEncrypted() {
            long identity = Binder.clearCallingIdentity();
            try {
                boolean isNonDefaultBlockEncrypted = StorageManager.isNonDefaultBlockEncrypted();
                return isNonDefaultBlockEncrypted;
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        boolean storageManagerIsEncrypted() {
            return StorageManager.isEncrypted();
        }

        boolean storageManagerIsEncryptable() {
            return StorageManager.isEncryptable();
        }

        Looper getMyLooper() {
            return Looper.myLooper();
        }

        WifiManager getWifiManager() {
            return (WifiManager) this.mContext.getSystemService(WifiManager.class);
        }

        long binderClearCallingIdentity() {
            return Binder.clearCallingIdentity();
        }

        void binderRestoreCallingIdentity(long token) {
            Binder.restoreCallingIdentity(token);
        }

        int binderGetCallingUid() {
            return Binder.getCallingUid();
        }

        int binderGetCallingPid() {
            return Binder.getCallingPid();
        }

        UserHandle binderGetCallingUserHandle() {
            return Binder.getCallingUserHandle();
        }

        boolean binderIsCallingUidMyUid() {
            return DevicePolicyManagerService.getCallingUid() == Process.myUid();
        }

        final int userHandleGetCallingUserId() {
            return UserHandle.getUserId(binderGetCallingUid());
        }

        File environmentGetUserSystemDirectory(int userId) {
            return Environment.getUserSystemDirectory(userId);
        }

        void powerManagerGoToSleep(long time, int reason, int flags) {
            ((PowerManager) this.mContext.getSystemService(PowerManager.class)).goToSleep(time, reason, flags);
        }

        void powerManagerReboot(String reason) {
            ((PowerManager) this.mContext.getSystemService(PowerManager.class)).reboot(reason);
        }

        void recoverySystemRebootWipeUserData(boolean shutdown, String reason, boolean force) throws IOException {
            RecoverySystem.rebootWipeUserData(this.mContext, shutdown, reason, force);
        }

        boolean systemPropertiesGetBoolean(String key, boolean def) {
            return SystemProperties.getBoolean(key, def);
        }

        long systemPropertiesGetLong(String key, long def) {
            return SystemProperties.getLong(key, def);
        }

        String systemPropertiesGet(String key, String def) {
            return SystemProperties.get(key, def);
        }

        String systemPropertiesGet(String key) {
            return SystemProperties.get(key);
        }

        void systemPropertiesSet(String key, String value) {
            SystemProperties.set(key, value);
        }

        boolean userManagerIsSplitSystemUser() {
            return UserManager.isSplitSystemUser();
        }

        String getDevicePolicyFilePathForSystemUser() {
            return "/data/system/";
        }

        PendingIntent pendingIntentGetActivityAsUser(Context context, int requestCode, Intent intent, int flags, Bundle options, UserHandle user) {
            return PendingIntent.getActivityAsUser(context, requestCode, intent, flags, options, user);
        }

        void registerContentObserver(Uri uri, boolean notifyForDescendents, ContentObserver observer, int userHandle) {
            this.mContext.getContentResolver().registerContentObserver(uri, notifyForDescendents, observer, userHandle);
        }

        int settingsSecureGetIntForUser(String name, int def, int userHandle) {
            return Secure.getIntForUser(this.mContext.getContentResolver(), name, def, userHandle);
        }

        String settingsSecureGetStringForUser(String name, int userHandle) {
            return Secure.getStringForUser(this.mContext.getContentResolver(), name, userHandle);
        }

        void settingsSecurePutIntForUser(String name, int value, int userHandle) {
            Secure.putIntForUser(this.mContext.getContentResolver(), name, value, userHandle);
        }

        void settingsSecurePutStringForUser(String name, String value, int userHandle) {
            Secure.putStringForUser(this.mContext.getContentResolver(), name, value, userHandle);
        }

        void settingsGlobalPutStringForUser(String name, String value, int userHandle) {
            Global.putStringForUser(this.mContext.getContentResolver(), name, value, userHandle);
        }

        void settingsSecurePutInt(String name, int value) {
            Secure.putInt(this.mContext.getContentResolver(), name, value);
        }

        int settingsGlobalGetInt(String name, int def) {
            return Global.getInt(this.mContext.getContentResolver(), name, def);
        }

        String settingsGlobalGetString(String name) {
            return Global.getString(this.mContext.getContentResolver(), name);
        }

        void settingsGlobalPutInt(String name, int value) {
            Global.putInt(this.mContext.getContentResolver(), name, value);
        }

        void settingsSecurePutString(String name, String value) {
            Secure.putString(this.mContext.getContentResolver(), name, value);
        }

        void settingsGlobalPutString(String name, String value) {
            Global.putString(this.mContext.getContentResolver(), name, value);
        }

        void securityLogSetLoggingEnabledProperty(boolean enabled) {
            SecurityLog.setLoggingEnabledProperty(enabled);
        }

        boolean securityLogGetLoggingEnabledProperty() {
            return SecurityLog.getLoggingEnabledProperty();
        }

        boolean securityLogIsLoggingEnabled() {
            return SecurityLog.isLoggingEnabled();
        }

        KeyChainConnection keyChainBindAsUser(UserHandle user) throws InterruptedException {
            return KeyChain.bindAsUser(this.mContext, user);
        }
    }

    public static final class Lifecycle extends SystemService {
        private DevicePolicyManagerService mService;

        public Lifecycle(Context context) {
            super(context);
            IHwDevicePolicyManagerService iwms = HwDevicePolicyFactory.getHuaweiDevicePolicyManagerService();
            if (iwms != null) {
                this.mService = iwms.getInstance(context);
            } else {
                this.mService = new DevicePolicyManagerService(context);
            }
        }

        public void onStart() {
            publishBinderService("device_policy", this.mService);
        }

        public void onBootPhase(int phase) {
            this.mService.systemReady(phase);
        }

        public void onStartUser(int userHandle) {
            this.mService.handleStartUser(userHandle);
        }

        public void onUnlockUser(int userHandle) {
            this.mService.handleUnlockUser(userHandle);
        }

        public void onStopUser(int userHandle) {
            this.mService.handleStopUser(userHandle);
        }
    }

    final class LocalService extends DevicePolicyManagerInternal {
        private List<OnCrossProfileWidgetProvidersChangeListener> mWidgetProviderListeners;

        LocalService() {
        }

        public List<String> getCrossProfileWidgetProviders(int profileId) {
            synchronized (DevicePolicyManagerService.this) {
                List<String> emptyList;
                if (DevicePolicyManagerService.this.mOwners == null) {
                    emptyList = Collections.emptyList();
                    return emptyList;
                }
                ComponentName ownerComponent = DevicePolicyManagerService.this.mOwners.getProfileOwnerComponent(profileId);
                if (ownerComponent == null) {
                    emptyList = Collections.emptyList();
                    return emptyList;
                }
                ActiveAdmin admin = (ActiveAdmin) DevicePolicyManagerService.this.getUserDataUnchecked(profileId).mAdminMap.get(ownerComponent);
                if (!(admin == null || admin.crossProfileWidgetProviders == null)) {
                    if (!admin.crossProfileWidgetProviders.isEmpty()) {
                        emptyList = admin.crossProfileWidgetProviders;
                        return emptyList;
                    }
                }
                emptyList = Collections.emptyList();
                return emptyList;
            }
        }

        public void addOnCrossProfileWidgetProvidersChangeListener(OnCrossProfileWidgetProvidersChangeListener listener) {
            synchronized (DevicePolicyManagerService.this) {
                if (this.mWidgetProviderListeners == null) {
                    this.mWidgetProviderListeners = new ArrayList();
                }
                if (!this.mWidgetProviderListeners.contains(listener)) {
                    this.mWidgetProviderListeners.add(listener);
                }
            }
        }

        public boolean isActiveAdminWithPolicy(int uid, int reqPolicy) {
            boolean z;
            synchronized (DevicePolicyManagerService.this) {
                z = DevicePolicyManagerService.this.getActiveAdminWithPolicyForUidLocked(null, reqPolicy, uid) != null;
            }
            return z;
        }

        private void notifyCrossProfileProvidersChanged(int userId, List<String> packages) {
            List<OnCrossProfileWidgetProvidersChangeListener> listeners;
            synchronized (DevicePolicyManagerService.this) {
                listeners = new ArrayList(this.mWidgetProviderListeners);
            }
            int listenerCount = listeners.size();
            for (int i = 0; i < listenerCount; i++) {
                ((OnCrossProfileWidgetProvidersChangeListener) listeners.get(i)).onCrossProfileWidgetProvidersChanged(userId, packages);
            }
        }

        public Intent createShowAdminSupportIntent(int userId, boolean useDefaultIfNoAdmin) {
            ComponentName profileOwner = DevicePolicyManagerService.this.mOwners.getProfileOwnerComponent(userId);
            if (profileOwner != null) {
                return DevicePolicyManagerService.this.createShowAdminSupportIntent(profileOwner, userId);
            }
            Pair<Integer, ComponentName> deviceOwner = DevicePolicyManagerService.this.mOwners.getDeviceOwnerUserIdAndComponent();
            if (deviceOwner != null && ((Integer) deviceOwner.first).intValue() == userId) {
                return DevicePolicyManagerService.this.createShowAdminSupportIntent((ComponentName) deviceOwner.second, userId);
            }
            if (useDefaultIfNoAdmin) {
                return DevicePolicyManagerService.this.createShowAdminSupportIntent(null, userId);
            }
            return null;
        }

        public Intent createUserRestrictionSupportIntent(int userId, String userRestriction) {
            long ident = DevicePolicyManagerService.this.mInjector.binderClearCallingIdentity();
            try {
                int source = DevicePolicyManagerService.this.mUserManager.getUserRestrictionSource(userRestriction, UserHandle.of(userId));
                if ((source & 1) != 0) {
                    return null;
                }
                boolean enforcedByDo = (source & 2) != 0;
                boolean enforcedByPo = (source & 4) != 0;
                if (enforcedByDo && enforcedByPo) {
                    return DevicePolicyManagerService.this.createShowAdminSupportIntent(null, userId);
                }
                if (enforcedByPo) {
                    ComponentName profileOwner = DevicePolicyManagerService.this.mOwners.getProfileOwnerComponent(userId);
                    if (profileOwner != null) {
                        return DevicePolicyManagerService.this.createShowAdminSupportIntent(profileOwner, userId);
                    }
                    return null;
                } else if (!enforcedByDo) {
                    return null;
                } else {
                    Pair<Integer, ComponentName> deviceOwner = DevicePolicyManagerService.this.mOwners.getDeviceOwnerUserIdAndComponent();
                    if (deviceOwner != null) {
                        return DevicePolicyManagerService.this.createShowAdminSupportIntent((ComponentName) deviceOwner.second, ((Integer) deviceOwner.first).intValue());
                    }
                    return null;
                }
            } finally {
                DevicePolicyManagerService.this.mInjector.binderRestoreCallingIdentity(ident);
            }
        }
    }

    private class SetupContentObserver extends ContentObserver {
        private final Uri mDefaultImeChanged = Secure.getUriFor("default_input_method");
        private final Uri mDeviceProvisioned = Global.getUriFor("device_provisioned");
        private final Uri mPaired = Secure.getUriFor("device_paired");
        @GuardedBy("DevicePolicyManagerService.this")
        private Set<Integer> mUserIdsWithPendingChangesByOwner = new ArraySet();
        private final Uri mUserSetupComplete = Secure.getUriFor("user_setup_complete");

        public SetupContentObserver(Handler handler) {
            super(handler);
        }

        void register() {
            DevicePolicyManagerService.this.mInjector.registerContentObserver(this.mUserSetupComplete, false, this, -1);
            DevicePolicyManagerService.this.mInjector.registerContentObserver(this.mDeviceProvisioned, false, this, -1);
            if (DevicePolicyManagerService.this.mIsWatch) {
                DevicePolicyManagerService.this.mInjector.registerContentObserver(this.mPaired, false, this, -1);
            }
            DevicePolicyManagerService.this.mInjector.registerContentObserver(this.mDefaultImeChanged, false, this, -1);
        }

        private void addPendingChangeByOwnerLocked(int userId) {
            this.mUserIdsWithPendingChangesByOwner.add(Integer.valueOf(userId));
        }

        public void onChange(boolean selfChange, Uri uri, int userId) {
            if (this.mUserSetupComplete.equals(uri) || (DevicePolicyManagerService.this.mIsWatch && this.mPaired.equals(uri))) {
                DevicePolicyManagerService.this.updateUserSetupCompleteAndPaired();
                return;
            }
            DevicePolicyManagerService devicePolicyManagerService;
            if (this.mDeviceProvisioned.equals(uri)) {
                devicePolicyManagerService = DevicePolicyManagerService.this;
                synchronized (devicePolicyManagerService) {
                    DevicePolicyManagerService.this.setDeviceOwnerSystemPropertyLocked();
                }
            } else if (this.mDefaultImeChanged.equals(uri)) {
                devicePolicyManagerService = DevicePolicyManagerService.this;
                synchronized (devicePolicyManagerService) {
                    if (this.mUserIdsWithPendingChangesByOwner.contains(Integer.valueOf(userId))) {
                        this.mUserIdsWithPendingChangesByOwner.remove(Integer.valueOf(userId));
                    } else {
                        DevicePolicyManagerService.this.getUserData(userId).mCurrentInputMethodSet = false;
                        DevicePolicyManagerService.this.saveSettingsLocked(userId);
                    }
                }
            } else {
                return;
            }
        }
    }

    static {
        SECURE_SETTINGS_WHITELIST.add("default_input_method");
        SECURE_SETTINGS_WHITELIST.add("skip_first_use_hints");
        SECURE_SETTINGS_WHITELIST.add("install_non_market_apps");
        SECURE_SETTINGS_DEVICEOWNER_WHITELIST.addAll(SECURE_SETTINGS_WHITELIST);
        SECURE_SETTINGS_DEVICEOWNER_WHITELIST.add("location_mode");
        GLOBAL_SETTINGS_WHITELIST.add("adb_enabled");
        GLOBAL_SETTINGS_WHITELIST.add("auto_time");
        GLOBAL_SETTINGS_WHITELIST.add("auto_time_zone");
        GLOBAL_SETTINGS_WHITELIST.add("data_roaming");
        GLOBAL_SETTINGS_WHITELIST.add("usb_mass_storage_enabled");
        GLOBAL_SETTINGS_WHITELIST.add("wifi_sleep_policy");
        GLOBAL_SETTINGS_WHITELIST.add("stay_on_while_plugged_in");
        GLOBAL_SETTINGS_WHITELIST.add("wifi_device_owner_configs_lockdown");
        GLOBAL_SETTINGS_DEPRECATED.add("bluetooth_on");
        GLOBAL_SETTINGS_DEPRECATED.add("development_settings_enabled");
        GLOBAL_SETTINGS_DEPRECATED.add("mode_ringer");
        GLOBAL_SETTINGS_DEPRECATED.add("network_preference");
        GLOBAL_SETTINGS_DEPRECATED.add("wifi_on");
    }

    private void handlePackagesChanged(String packageName, int userHandle) {
        boolean removedAdmin = false;
        DevicePolicyData policy = getUserData(userHandle);
        synchronized (this) {
            int i;
            for (i = policy.mAdminList.size() - 1; i >= 0; i--) {
                ActiveAdmin aa = (ActiveAdmin) policy.mAdminList.get(i);
                try {
                    String adminPackage = aa.info.getPackageName();
                    if ((packageName == null || packageName.equals(adminPackage)) && (this.mIPackageManager.getPackageInfo(adminPackage, 0, userHandle) == null || this.mIPackageManager.getReceiverInfo(aa.info.getComponent(), 786432, userHandle) == null)) {
                        removedAdmin = true;
                        policy.mAdminList.remove(i);
                        policy.mAdminMap.remove(aa.info.getComponent());
                    }
                } catch (RemoteException e) {
                }
            }
            if (removedAdmin) {
                validatePasswordOwnerLocked(policy);
            }
            boolean removedDelegate = false;
            for (i = policy.mDelegationMap.size() - 1; i >= 0; i--) {
                if (isRemovedPackage(packageName, (String) policy.mDelegationMap.keyAt(i), userHandle)) {
                    policy.mDelegationMap.removeAt(i);
                    removedDelegate = true;
                }
            }
            ComponentName owner = getOwnerComponent(userHandle);
            if (!(packageName == null || owner == null || !owner.getPackageName().equals(packageName))) {
                startOwnerService(userHandle, "package-broadcast");
            }
            if (removedAdmin || removedDelegate) {
                saveSettingsLocked(policy.mUserHandle);
            }
        }
        if (removedAdmin) {
            pushUserRestrictions(userHandle);
        }
    }

    /* JADX WARNING: Missing block: B:5:0x0009, code:
            if (r5.equals(r6) != false) goto L_0x000b;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean isRemovedPackage(String changedPackage, String targetPackage, int userHandle) {
        boolean z = false;
        if (targetPackage != null) {
            if (changedPackage != null) {
                try {
                } catch (RemoteException e) {
                    return false;
                }
            }
            if (this.mIPackageManager.getPackageInfo(targetPackage, 0, userHandle) == null) {
                z = true;
            }
        }
        return z;
    }

    public DevicePolicyManagerService(Context context) {
        this(new Injector(context));
    }

    DevicePolicyManagerService(Injector injector) {
        this.mPackagesToRemove = new ArraySet();
        this.mToken = new Binder();
        this.mIsMDMDeviceOwnerAPI = false;
        this.mRemoteBugreportServiceIsActive = new AtomicBoolean();
        this.mRemoteBugreportSharingAccepted = new AtomicBoolean();
        this.mRemoteBugreportTimeoutRunnable = new Runnable() {
            public void run() {
                if (DevicePolicyManagerService.this.mRemoteBugreportServiceIsActive.get()) {
                    DevicePolicyManagerService.this.onBugreportFailed();
                }
            }
        };
        this.mRemoteBugreportFinishedReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if ("android.intent.action.REMOTE_BUGREPORT_DISPATCH".equals(intent.getAction()) && DevicePolicyManagerService.this.mRemoteBugreportServiceIsActive.get()) {
                    DevicePolicyManagerService.this.onBugreportFinished(intent);
                }
            }
        };
        this.mRemoteBugreportConsentReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                DevicePolicyManagerService.this.mInjector.getNotificationManager().cancel(DevicePolicyManagerService.LOG_TAG, 678432343);
                if ("com.android.server.action.REMOTE_BUGREPORT_SHARING_ACCEPTED".equals(action)) {
                    DevicePolicyManagerService.this.onBugreportSharingAccepted();
                } else if ("com.android.server.action.REMOTE_BUGREPORT_SHARING_DECLINED".equals(action)) {
                    DevicePolicyManagerService.this.onBugreportSharingDeclined();
                }
                DevicePolicyManagerService.this.mContext.unregisterReceiver(DevicePolicyManagerService.this.mRemoteBugreportConsentReceiver);
            }
        };
        this.mUserData = new SparseArray();
        this.mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                final int userHandle = intent.getIntExtra("android.intent.extra.user_handle", getSendingUserId());
                if ("android.intent.action.USER_STARTED".equals(action) && userHandle == DevicePolicyManagerService.this.mOwners.getDeviceOwnerUserId()) {
                    synchronized (DevicePolicyManagerService.this) {
                        if (DevicePolicyManagerService.this.isNetworkLoggingEnabledInternalLocked()) {
                            DevicePolicyManagerService.this.setNetworkLoggingActiveInternal(true);
                        }
                    }
                }
                if ("android.intent.action.BOOT_COMPLETED".equals(action) && userHandle == DevicePolicyManagerService.this.mOwners.getDeviceOwnerUserId() && DevicePolicyManagerService.this.getDeviceOwnerRemoteBugreportUri() != null) {
                    IntentFilter filterConsent = new IntentFilter();
                    filterConsent.addAction("com.android.server.action.REMOTE_BUGREPORT_SHARING_DECLINED");
                    filterConsent.addAction("com.android.server.action.REMOTE_BUGREPORT_SHARING_ACCEPTED");
                    DevicePolicyManagerService.this.mContext.registerReceiver(DevicePolicyManagerService.this.mRemoteBugreportConsentReceiver, filterConsent);
                    DevicePolicyManagerService.this.mInjector.getNotificationManager().notifyAsUser(DevicePolicyManagerService.LOG_TAG, 678432343, RemoteBugreportUtils.buildNotification(DevicePolicyManagerService.this.mContext, 3), UserHandle.ALL);
                }
                if ("android.intent.action.BOOT_COMPLETED".equals(action) || DevicePolicyManagerService.ACTION_EXPIRED_PASSWORD_NOTIFICATION.equals(action)) {
                    DevicePolicyManagerService.this.mHandler.post(new Runnable() {
                        public void run() {
                            DevicePolicyManagerService.this.handlePasswordExpirationNotification(userHandle);
                        }
                    });
                }
                DevicePolicyManagerService devicePolicyManagerService;
                if ("android.intent.action.USER_ADDED".equals(action)) {
                    sendUserAddedOrRemovedCommand("android.app.action.USER_ADDED", userHandle);
                    devicePolicyManagerService = DevicePolicyManagerService.this;
                    synchronized (devicePolicyManagerService) {
                        DevicePolicyManagerService.this.maybePauseDeviceWideLoggingLocked();
                    }
                } else if ("android.intent.action.USER_REMOVED".equals(action)) {
                    sendUserAddedOrRemovedCommand("android.app.action.USER_REMOVED", userHandle);
                    devicePolicyManagerService = DevicePolicyManagerService.this;
                    synchronized (devicePolicyManagerService) {
                        boolean isRemovedUserAffiliated = DevicePolicyManagerService.this.isUserAffiliatedWithDeviceLocked(userHandle);
                        DevicePolicyManagerService.this.removeUserData(userHandle);
                        if (!isRemovedUserAffiliated) {
                            DevicePolicyManagerService.this.discardDeviceWideLogsLocked();
                            DevicePolicyManagerService.this.maybeResumeDeviceWideLoggingLocked();
                        }
                    }
                } else if ("android.intent.action.USER_STARTED".equals(action)) {
                    synchronized (DevicePolicyManagerService.this) {
                        DevicePolicyManagerService.this.sendAdminEnabledBroadcastLocked(userHandle);
                    }
                    DevicePolicyManagerService.this.handlePackagesChanged(null, userHandle);
                    return;
                } else if ("android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE".equals(action)) {
                    DevicePolicyManagerService.this.handlePackagesChanged(null, userHandle);
                    return;
                } else if ("android.intent.action.PACKAGE_CHANGED".equals(action) || ("android.intent.action.PACKAGE_ADDED".equals(action) && intent.getBooleanExtra("android.intent.extra.REPLACING", false))) {
                    DevicePolicyManagerService.this.handlePackagesChanged(intent.getData().getSchemeSpecificPart(), userHandle);
                    return;
                } else if ("android.intent.action.PACKAGE_REMOVED".equals(action) && (intent.getBooleanExtra("android.intent.extra.REPLACING", false) ^ 1) != 0) {
                    DevicePolicyManagerService.this.handlePackagesChanged(intent.getData().getSchemeSpecificPart(), userHandle);
                    return;
                } else if ("android.intent.action.MANAGED_PROFILE_ADDED".equals(action)) {
                    DevicePolicyManagerService.this.clearWipeProfileNotification();
                    return;
                } else if ("android.intent.action.USER_STOPPED".equals(action)) {
                    devicePolicyManagerService = DevicePolicyManagerService.this;
                    synchronized (devicePolicyManagerService) {
                        Slog.d(DevicePolicyManagerService.LOG_TAG, "ACTION_USER_STOPPED received, remove mUserData for " + userHandle);
                        DevicePolicyManagerService.this.mUserData.remove(userHandle);
                    }
                } else {
                    return;
                }
            }

            private void sendUserAddedOrRemovedCommand(String action, int userHandle) {
                synchronized (DevicePolicyManagerService.this) {
                    ActiveAdmin deviceOwner = DevicePolicyManagerService.this.getDeviceOwnerAdminLocked();
                    if (deviceOwner != null) {
                        Bundle extras = new Bundle();
                        extras.putParcelable("android.intent.extra.USER", UserHandle.of(userHandle));
                        DevicePolicyManagerService.this.sendAdminCommandLocked(deviceOwner, action, extras, null);
                    }
                }
            }
        };
        this.mInjector = injector;
        this.mContext = (Context) Preconditions.checkNotNull(injector.mContext);
        this.mHandler = new Handler((Looper) Preconditions.checkNotNull(injector.getMyLooper()));
        this.mConstants = DevicePolicyConstants.loadFromString(this.mInjector.settingsGlobalGetString("device_policy_constants"));
        this.mOwners = (Owners) Preconditions.checkNotNull(injector.newOwners());
        this.mUserManager = (UserManager) Preconditions.checkNotNull(injector.getUserManager());
        this.mUserManagerInternal = (UserManagerInternal) Preconditions.checkNotNull(injector.getUserManagerInternal());
        this.mIPackageManager = (IPackageManager) Preconditions.checkNotNull(injector.getIPackageManager());
        this.mTelephonyManager = (TelephonyManager) Preconditions.checkNotNull(injector.getTelephonyManager());
        this.mLocalService = new LocalService();
        this.mLockPatternUtils = injector.newLockPatternUtils();
        this.mSecurityLogMonitor = new SecurityLogMonitor(this);
        this.mHasFeature = this.mInjector.getPackageManager().hasSystemFeature("android.software.device_admin");
        this.mIsWatch = this.mInjector.getPackageManager().hasSystemFeature("android.hardware.type.watch");
        this.mBackgroundHandler = BackgroundThread.getHandler();
        this.mCertificateMonitor = new CertificateMonitor(this, this.mInjector, this.mBackgroundHandler);
        this.mDeviceAdminServiceController = new DeviceAdminServiceController(this, this.mConstants);
        if (this.mHasFeature) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.BOOT_COMPLETED");
            filter.addAction(ACTION_EXPIRED_PASSWORD_NOTIFICATION);
            filter.addAction("android.intent.action.USER_ADDED");
            filter.addAction("android.intent.action.USER_REMOVED");
            filter.addAction("android.intent.action.USER_STARTED");
            filter.addAction("android.intent.action.USER_STOPPED");
            filter.setPriority(1000);
            this.mContext.registerReceiverAsUser(this.mReceiver, UserHandle.ALL, filter, null, this.mHandler);
            filter = new IntentFilter();
            filter.addAction("android.intent.action.PACKAGE_CHANGED");
            filter.addAction("android.intent.action.PACKAGE_REMOVED");
            filter.addAction("android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE");
            filter.addAction("android.intent.action.PACKAGE_ADDED");
            filter.addDataScheme(HwBroadcastRadarUtil.KEY_PACKAGE);
            this.mContext.registerReceiverAsUser(this.mReceiver, UserHandle.ALL, filter, null, this.mHandler);
            filter = new IntentFilter();
            filter.addAction("android.intent.action.MANAGED_PROFILE_ADDED");
            this.mContext.registerReceiverAsUser(this.mReceiver, UserHandle.ALL, filter, null, this.mHandler);
            LocalServices.addService(DevicePolicyManagerInternal.class, this.mLocalService);
            this.mSetupContentObserver = new SetupContentObserver(this.mHandler);
        }
    }

    DevicePolicyData getUserData(int userHandle) {
        DevicePolicyData policy;
        synchronized (this) {
            policy = (DevicePolicyData) this.mUserData.get(userHandle);
            if (policy == null) {
                policy = new DevicePolicyData(userHandle);
                this.mUserData.append(userHandle, policy);
                loadSettingsLocked(policy, userHandle);
            }
            init();
        }
        return policy;
    }

    DevicePolicyData getUserDataUnchecked(int userHandle) {
        long ident = this.mInjector.binderClearCallingIdentity();
        try {
            DevicePolicyData userData = getUserData(userHandle);
            return userData;
        } finally {
            this.mInjector.binderRestoreCallingIdentity(ident);
        }
    }

    void removeUserData(int userHandle) {
        synchronized (this) {
            if (userHandle == 0) {
                Slog.w(LOG_TAG, "Tried to remove device policy file for user 0! Ignoring.");
                return;
            }
            this.mOwners.removeProfileOwner(userHandle);
            this.mOwners.writeProfileOwner(userHandle);
            if (((DevicePolicyData) this.mUserData.get(userHandle)) != null) {
                this.mUserData.remove(userHandle);
            }
            File policyFile = new File(this.mInjector.environmentGetUserSystemDirectory(userHandle), DEVICE_POLICIES_XML);
            policyFile.delete();
            Slog.i(LOG_TAG, "Removed device policy file " + policyFile.getAbsolutePath());
            updateScreenCaptureDisabledInWindowManager(userHandle, false);
        }
    }

    void loadOwners() {
        synchronized (this) {
            this.mOwners.load();
            setDeviceOwnerSystemPropertyLocked();
            findOwnerComponentIfNecessaryLocked();
            migrateUserRestrictionsIfNecessaryLocked();
            maybeSetDefaultDeviceOwnerUserRestrictionsLocked();
            updateDeviceOwnerLocked();
        }
    }

    private void maybeSetDefaultDeviceOwnerUserRestrictionsLocked() {
        ActiveAdmin deviceOwner = getDeviceOwnerAdminLocked();
        if (deviceOwner != null) {
            maybeSetDefaultRestrictionsForAdminLocked(this.mOwners.getDeviceOwnerUserId(), deviceOwner, UserRestrictionsUtils.getDefaultEnabledForDeviceOwner());
        }
    }

    private void maybeSetDefaultProfileOwnerUserRestrictions() {
        synchronized (this) {
            for (Integer intValue : this.mOwners.getProfileOwnerKeys()) {
                int userId = intValue.intValue();
                ActiveAdmin profileOwner = getProfileOwnerAdminLocked(userId);
                if (profileOwner != null && (this.mUserManager.isManagedProfile(userId) ^ 1) == 0) {
                    maybeSetDefaultRestrictionsForAdminLocked(userId, profileOwner, UserRestrictionsUtils.getDefaultEnabledForManagedProfiles());
                    ensureUnknownSourcesRestrictionForProfileOwnerLocked(userId, profileOwner, false);
                }
            }
        }
    }

    private void ensureUnknownSourcesRestrictionForProfileOwnerLocked(int userId, ActiveAdmin profileOwner, boolean newOwner) {
        if (newOwner || this.mInjector.settingsSecureGetIntForUser("unknown_sources_default_reversed", 0, userId) != 0) {
            profileOwner.ensureUserRestrictions().putBoolean("no_install_unknown_sources", true);
            saveUserRestrictionsLocked(userId);
            this.mInjector.settingsSecurePutIntForUser("unknown_sources_default_reversed", 0, userId);
        }
    }

    private void maybeSetDefaultRestrictionsForAdminLocked(int userId, ActiveAdmin admin, Set<String> defaultRestrictions) {
        if (!defaultRestrictions.equals(admin.defaultEnabledRestrictionsAlreadySet)) {
            Slog.i(LOG_TAG, "New user restrictions need to be set by default for user " + userId);
            Set<String> restrictionsToSet = new ArraySet(defaultRestrictions);
            restrictionsToSet.removeAll(admin.defaultEnabledRestrictionsAlreadySet);
            if (!restrictionsToSet.isEmpty()) {
                for (String restriction : restrictionsToSet) {
                    admin.ensureUserRestrictions().putBoolean(restriction, true);
                }
                admin.defaultEnabledRestrictionsAlreadySet.addAll(restrictionsToSet);
                Slog.i(LOG_TAG, "Enabled the following restrictions by default: " + restrictionsToSet);
                saveUserRestrictionsLocked(userId);
            }
        }
    }

    private void setDeviceOwnerSystemPropertyLocked() {
        boolean deviceProvisioned = this.mInjector.settingsGlobalGetInt("device_provisioned", 0) != 0;
        if (((this.mIsWatch ? this.mOwners.hasDeviceOwner() : false) || (deviceProvisioned ^ 1) == 0) && !StorageManager.inCryptKeeperBounce()) {
            if (!TextUtils.isEmpty(this.mInjector.systemPropertiesGet(PROPERTY_DEVICE_OWNER_PRESENT))) {
                Slog.w(LOG_TAG, "Trying to set ro.device_owner, but it has already been set?");
            } else if (this.mOwners.hasDeviceOwner()) {
                this.mInjector.systemPropertiesSet(PROPERTY_DEVICE_OWNER_PRESENT, "true");
                Slog.i(LOG_TAG, "Set ro.device_owner property to true");
                if (this.mInjector.securityLogGetLoggingEnabledProperty()) {
                    this.mSecurityLogMonitor.start();
                    maybePauseDeviceWideLoggingLocked();
                }
            } else {
                this.mInjector.systemPropertiesSet(PROPERTY_DEVICE_OWNER_PRESENT, "false");
                Slog.i(LOG_TAG, "Set ro.device_owner property to false");
            }
        }
    }

    private void findOwnerComponentIfNecessaryLocked() {
        if (this.mOwners.hasDeviceOwner()) {
            ComponentName doComponentName = this.mOwners.getDeviceOwnerComponent();
            if (TextUtils.isEmpty(doComponentName.getClassName())) {
                ComponentName doComponent = findAdminComponentWithPackageLocked(doComponentName.getPackageName(), this.mOwners.getDeviceOwnerUserId());
                if (doComponent == null) {
                    Slog.e(LOG_TAG, "Device-owner isn't registered as device-admin");
                } else {
                    this.mOwners.setDeviceOwnerWithRestrictionsMigrated(doComponent, this.mOwners.getDeviceOwnerName(), this.mOwners.getDeviceOwnerUserId(), this.mOwners.getDeviceOwnerUserRestrictionsNeedsMigration() ^ 1);
                    this.mOwners.writeDeviceOwner();
                }
            }
        }
    }

    private void migrateUserRestrictionsIfNecessaryLocked() {
        if (this.mOwners.getDeviceOwnerUserRestrictionsNeedsMigration()) {
            migrateUserRestrictionsForUser(UserHandle.SYSTEM, getDeviceOwnerAdminLocked(), null, true);
            pushUserRestrictions(0);
            this.mOwners.setDeviceOwnerUserRestrictionsMigrated();
        }
        Set<String> secondaryUserExceptionList = Sets.newArraySet(new String[]{"no_outgoing_calls", "no_sms"});
        for (UserInfo ui : this.mUserManager.getUsers()) {
            int userId = ui.id;
            if (this.mOwners.getProfileOwnerUserRestrictionsNeedsMigration(userId)) {
                Set exceptionList;
                ActiveAdmin profileOwnerAdmin = getProfileOwnerAdminLocked(userId);
                if (userId == 0) {
                    exceptionList = null;
                } else {
                    Set<String> exceptionList2 = secondaryUserExceptionList;
                }
                migrateUserRestrictionsForUser(ui.getUserHandle(), profileOwnerAdmin, exceptionList2, false);
                pushUserRestrictions(userId);
                this.mOwners.setProfileOwnerUserRestrictionsMigrated(userId);
            }
        }
    }

    private void migrateUserRestrictionsForUser(UserHandle user, ActiveAdmin admin, Set<String> exceptionList, boolean isDeviceOwner) {
        Bundle origRestrictions = this.mUserManagerInternal.getBaseUserRestrictions(user.getIdentifier());
        Bundle newBaseRestrictions = new Bundle();
        Bundle newOwnerRestrictions = new Bundle();
        for (String key : origRestrictions.keySet()) {
            if (origRestrictions.getBoolean(key)) {
                boolean canOwnerChange;
                if (isDeviceOwner) {
                    canOwnerChange = UserRestrictionsUtils.canDeviceOwnerChange(key);
                } else {
                    canOwnerChange = UserRestrictionsUtils.canProfileOwnerChange(key, user.getIdentifier());
                }
                if (!canOwnerChange || (exceptionList != null && exceptionList.contains(key))) {
                    newBaseRestrictions.putBoolean(key, true);
                } else {
                    newOwnerRestrictions.putBoolean(key, true);
                }
            }
        }
        this.mUserManagerInternal.setBaseUserRestrictionsByDpmsForMigration(user.getIdentifier(), newBaseRestrictions);
        if (admin != null) {
            admin.ensureUserRestrictions().clear();
            admin.ensureUserRestrictions().putAll(newOwnerRestrictions);
        } else {
            Slog.w(LOG_TAG, "ActiveAdmin for DO/PO not found. user=" + user.getIdentifier());
        }
        saveSettingsLocked(user.getIdentifier());
    }

    private ComponentName findAdminComponentWithPackageLocked(String packageName, int userId) {
        DevicePolicyData policy = getUserData(userId);
        int n = policy.mAdminList.size();
        ComponentName found = null;
        int nFound = 0;
        for (int i = 0; i < n; i++) {
            ActiveAdmin admin = (ActiveAdmin) policy.mAdminList.get(i);
            if (packageName.equals(admin.info.getPackageName())) {
                if (nFound == 0) {
                    found = admin.info.getComponent();
                }
                nFound++;
            }
        }
        if (nFound > 1) {
            Slog.w(LOG_TAG, "Multiple DA found; assume the first one is DO.");
        }
        return found;
    }

    private void setExpirationAlarmCheckLocked(Context context, int userHandle, boolean parent) {
        long alarmTime;
        int affectedUserHandle;
        long expiration = getPasswordExpirationLocked(null, userHandle, parent);
        long now = System.currentTimeMillis();
        long timeToExpire = expiration - now;
        if (expiration == 0) {
            alarmTime = 0;
        } else if (timeToExpire <= 0) {
            alarmTime = now + MS_PER_DAY;
        } else {
            long alarmInterval = timeToExpire % MS_PER_DAY;
            if (alarmInterval == 0) {
                alarmInterval = MS_PER_DAY;
            }
            alarmTime = now + alarmInterval;
        }
        long token = this.mInjector.binderClearCallingIdentity();
        if (parent) {
            try {
                affectedUserHandle = getProfileParentId(userHandle);
            } catch (Throwable th) {
                this.mInjector.binderRestoreCallingIdentity(token);
            }
        } else {
            affectedUserHandle = userHandle;
        }
        AlarmManager am = this.mInjector.getAlarmManager();
        PendingIntent pi = PendingIntent.getBroadcastAsUser(context, REQUEST_EXPIRE_PASSWORD, new Intent(ACTION_EXPIRED_PASSWORD_NOTIFICATION), 1207959552, UserHandle.of(affectedUserHandle));
        am.cancel(pi);
        if (alarmTime != 0) {
            am.set(1, alarmTime, pi);
        }
        this.mInjector.binderRestoreCallingIdentity(token);
    }

    ActiveAdmin getActiveAdminUncheckedLocked(ComponentName who, int userHandle) {
        ActiveAdmin admin = (ActiveAdmin) getUserData(userHandle).mAdminMap.get(who);
        if (admin != null && who.getPackageName().equals(admin.info.getActivityInfo().packageName) && who.getClassName().equals(admin.info.getActivityInfo().name)) {
            return admin;
        }
        return null;
    }

    ActiveAdmin getActiveAdminUncheckedLocked(ComponentName who, int userHandle, boolean parent) {
        if (parent) {
            enforceManagedProfile(userHandle, "call APIs on the parent profile");
        }
        ActiveAdmin admin = getActiveAdminUncheckedLocked(who, userHandle);
        if (admin == null || !parent) {
            return admin;
        }
        return admin.getParentActiveAdmin();
    }

    ActiveAdmin getActiveAdminForCallerLocked(ComponentName who, int reqPolicy) throws SecurityException {
        int callingUid = this.mInjector.binderGetCallingUid();
        ActiveAdmin result = getActiveAdminWithPolicyForUidLocked(who, reqPolicy, callingUid);
        if (result != null) {
            return result;
        }
        if (who != null) {
            ActiveAdmin admin = (ActiveAdmin) getUserData(UserHandle.getUserId(callingUid)).mAdminMap.get(who);
            if (reqPolicy == -2) {
                throw new SecurityException("Admin " + admin.info.getComponent() + " does not own the device");
            } else if (reqPolicy == -1) {
                throw new SecurityException("Admin " + admin.info.getComponent() + " does not own the profile");
            } else {
                throw new SecurityException("Admin " + admin.info.getComponent() + " did not specify uses-policy for: " + admin.info.getTagForPolicy(reqPolicy));
            }
        }
        throw new SecurityException("No active admin owned by uid " + this.mInjector.binderGetCallingUid() + " for policy #" + reqPolicy);
    }

    ActiveAdmin getActiveAdminForCallerLocked(ComponentName who, int reqPolicy, boolean parent) throws SecurityException {
        if (parent) {
            enforceManagedProfile(this.mInjector.userHandleGetCallingUserId(), "call APIs on the parent profile");
        }
        ActiveAdmin admin = getActiveAdminForCallerLocked(who, reqPolicy);
        return parent ? admin.getParentActiveAdmin() : admin;
    }

    private ActiveAdmin getActiveAdminForUidLocked(ComponentName who, int uid) {
        ActiveAdmin admin = (ActiveAdmin) getUserData(UserHandle.getUserId(uid)).mAdminMap.get(who);
        if (admin == null) {
            throw new SecurityException("No active admin " + who);
        } else if (admin.getUid() == uid) {
            return admin;
        } else {
            throw new SecurityException("Admin " + who + " is not owned by uid " + uid);
        }
    }

    private ActiveAdmin getActiveAdminWithPolicyForUidLocked(ComponentName who, int reqPolicy, int uid) {
        int userId = UserHandle.getUserId(uid);
        DevicePolicyData policy = getUserData(userId);
        ActiveAdmin admin;
        if (who != null) {
            admin = (ActiveAdmin) policy.mAdminMap.get(who);
            if (admin == null) {
                throw new SecurityException("No active admin " + who);
            } else if (admin.getUid() != uid) {
                throw new SecurityException("Admin " + who + " is not owned by uid " + uid);
            } else if (isActiveAdminWithPolicyForUserLocked(admin, reqPolicy, userId)) {
                return admin;
            }
        }
        for (ActiveAdmin admin2 : policy.mAdminList) {
            if (admin2.getUid() == uid && isActiveAdminWithPolicyForUserLocked(admin2, reqPolicy, userId)) {
                return admin2;
            }
        }
        return null;
    }

    boolean isActiveAdminWithPolicyForUserLocked(ActiveAdmin admin, int reqPolicy, int userId) {
        boolean ownsDevice = isDeviceOwner(admin.info.getComponent(), userId);
        boolean ownsProfile = isProfileOwner(admin.info.getComponent(), userId);
        if (reqPolicy == -2) {
            return ownsDevice;
        }
        if (reqPolicy != -1) {
            return admin.info.usesPolicy(reqPolicy);
        }
        if (ownsDevice) {
            ownsProfile = true;
        }
        return ownsProfile;
    }

    void sendAdminCommandLocked(ActiveAdmin admin, String action) {
        sendAdminCommandLocked(admin, action, null);
    }

    void sendAdminCommandLocked(ActiveAdmin admin, String action, BroadcastReceiver result) {
        sendAdminCommandLocked(admin, action, null, result);
    }

    void sendAdminCommandLocked(ActiveAdmin admin, String action, Bundle adminExtras, BroadcastReceiver result) {
        Intent intent = new Intent(action);
        intent.setComponent(admin.info.getComponent());
        if (action.equals("android.app.action.ACTION_PASSWORD_EXPIRING")) {
            intent.putExtra("expiration", admin.passwordExpirationDate);
        }
        if (adminExtras != null) {
            intent.putExtras(adminExtras);
        }
        if (result != null) {
            this.mContext.sendOrderedBroadcastAsUser(intent, admin.getUserHandle(), null, result, this.mHandler, -1, null, null);
            return;
        }
        this.mContext.sendBroadcastAsUser(intent, admin.getUserHandle());
    }

    void sendAdminCommandLocked(String action, int reqPolicy, int userHandle, Bundle adminExtras) {
        DevicePolicyData policy = getUserData(userHandle);
        int count = policy.mAdminList.size();
        for (int i = 0; i < count; i++) {
            ActiveAdmin admin = (ActiveAdmin) policy.mAdminList.get(i);
            if (admin.info.usesPolicy(reqPolicy)) {
                sendAdminCommandLocked(admin, action, adminExtras, null);
            }
        }
    }

    private void sendAdminCommandToSelfAndProfilesLocked(String action, int reqPolicy, int userHandle, Bundle adminExtras) {
        for (int profileId : this.mUserManager.getProfileIdsWithDisabled(userHandle)) {
            sendAdminCommandLocked(action, reqPolicy, profileId, adminExtras);
        }
    }

    private void sendAdminCommandForLockscreenPoliciesLocked(String action, int reqPolicy, int userHandle) {
        Bundle extras = new Bundle();
        extras.putParcelable("android.intent.extra.USER", UserHandle.of(userHandle));
        if (isSeparateProfileChallengeEnabled(userHandle)) {
            sendAdminCommandLocked(action, reqPolicy, userHandle, extras);
        } else {
            sendAdminCommandToSelfAndProfilesLocked(action, reqPolicy, userHandle, extras);
        }
    }

    void removeActiveAdminLocked(final ComponentName adminReceiver, final int userHandle) {
        ActiveAdmin admin = getActiveAdminUncheckedLocked(adminReceiver, userHandle);
        DevicePolicyData policy = getUserData(userHandle);
        if (admin != null && (policy.mRemovingAdmins.contains(adminReceiver) ^ 1) != 0) {
            policy.mRemovingAdmins.add(adminReceiver);
            sendAdminCommandLocked(admin, "android.app.action.DEVICE_ADMIN_DISABLED", new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    DevicePolicyManagerService.this.removeAdminArtifacts(adminReceiver, userHandle);
                    DevicePolicyManagerService.this.removePackageIfRequired(adminReceiver.getPackageName(), userHandle);
                }
            });
            Flog.i(305, "removeActiveAdminLocked(" + adminReceiver + "), by user " + userHandle);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x007a A:{Splitter: B:17:0x0072, ExcHandler: org.xmlpull.v1.XmlPullParserException (r2_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:20:0x007a, code:
            r2 = move-exception;
     */
    /* JADX WARNING: Missing block: B:21:0x007b, code:
            android.util.Slog.w(LOG_TAG, "Bad device admin requested for user=" + r10 + ": " + r9, r2);
     */
    /* JADX WARNING: Missing block: B:22:0x00a0, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public DeviceAdminInfo findAdmin(ComponentName adminName, int userHandle, boolean throwForMissiongPermission) {
        if (!this.mHasFeature) {
            return null;
        }
        enforceFullCrossUsersPermission(userHandle);
        ActivityInfo ai = null;
        try {
            ai = this.mIPackageManager.getReceiverInfo(adminName, 819328, userHandle);
        } catch (RemoteException e) {
        }
        if (ai == null) {
            throw new IllegalArgumentException("Unknown admin: " + adminName);
        }
        if (!"android.permission.BIND_DEVICE_ADMIN".equals(ai.permission)) {
            String message = "DeviceAdminReceiver " + adminName + " must be protected with " + "android.permission.BIND_DEVICE_ADMIN";
            Slog.w(LOG_TAG, message);
            if (throwForMissiongPermission && ai.applicationInfo.targetSdkVersion > 23) {
                throw new IllegalArgumentException(message);
            }
        }
        try {
            return new DeviceAdminInfo(this.mContext, ai);
        } catch (Exception e2) {
        }
    }

    private JournaledFile makeJournaledFile(int userHandle) {
        String base;
        if (userHandle == 0) {
            base = this.mInjector.getDevicePolicyFilePathForSystemUser() + DEVICE_POLICIES_XML;
        } else {
            base = new File(this.mInjector.environmentGetUserSystemDirectory(userHandle), DEVICE_POLICIES_XML).getAbsolutePath();
        }
        HwCustDevicePolicyManagerService mHwCustDPMS = (HwCustDevicePolicyManagerService) HwCustUtils.createObj(HwCustDevicePolicyManagerService.class, new Object[0]);
        if (mHwCustDPMS != null && mHwCustDPMS.shouldActiveDeviceAdmins(base)) {
            mHwCustDPMS.activeDeviceAdmins(base);
        }
        return new JournaledFile(new File(base), new File(base + ".tmp"));
    }

    /* JADX WARNING: Removed duplicated region for block: B:30:0x017e A:{Splitter: B:3:0x0015, ExcHandler: org.xmlpull.v1.XmlPullParserException (e org.xmlpull.v1.XmlPullParserException)} */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x0190 A:{SYNTHETIC, Splitter: B:34:0x0190} */
    /* JADX WARNING: Removed duplicated region for block: B:107:0x06f8 A:{Splitter: B:1:0x000a, ExcHandler: org.xmlpull.v1.XmlPullParserException (e org.xmlpull.v1.XmlPullParserException)} */
    /* JADX WARNING: Missing block: B:30:0x017e, code:
            r10 = e;
     */
    /* JADX WARNING: Missing block: B:31:0x017f, code:
            r22 = r23;
     */
    /* JADX WARNING: Missing block: B:32:0x0181, code:
            android.util.Slog.w(LOG_TAG, "failed writing file", r10);
     */
    /* JADX WARNING: Missing block: B:33:0x018e, code:
            if (r22 != null) goto L_0x0190;
     */
    /* JADX WARNING: Missing block: B:35:?, code:
            r22.close();
     */
    /* JADX WARNING: Missing block: B:107:0x06f8, code:
            r10 = e;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void saveSettingsLocked(int userHandle) {
        DevicePolicyData policy = getUserData(userHandle);
        JournaledFile journal = makeJournaledFile(userHandle);
        FileOutputStream fileOutputStream = null;
        try {
            OutputStream stream = new FileOutputStream(journal.chooseForWrite(), false);
            try {
                int i;
                XmlSerializer out = new FastXmlSerializer();
                out.setOutput(stream, StandardCharsets.UTF_8.name());
                out.startDocument(null, Boolean.valueOf(true));
                out.startTag(null, "policies");
                if (policy.mRestrictionsProvider != null) {
                    out.attribute(null, ATTR_PERMISSION_PROVIDER, policy.mRestrictionsProvider.flattenToString());
                }
                if (policy.mUserSetupComplete) {
                    out.attribute(null, ATTR_SETUP_COMPLETE, Boolean.toString(true));
                }
                if (policy.mPaired) {
                    out.attribute(null, ATTR_DEVICE_PAIRED, Boolean.toString(true));
                }
                if (policy.mDeviceProvisioningConfigApplied) {
                    out.attribute(null, ATTR_DEVICE_PROVISIONING_CONFIG_APPLIED, Boolean.toString(true));
                }
                if (policy.mUserProvisioningState != 0) {
                    out.attribute(null, ATTR_PROVISIONING_STATE, Integer.toString(policy.mUserProvisioningState));
                }
                if (policy.mPermissionPolicy != 0) {
                    out.attribute(null, ATTR_PERMISSION_POLICY, Integer.toString(policy.mPermissionPolicy));
                }
                for (i = 0; i < policy.mDelegationMap.size(); i++) {
                    String delegatePackage = (String) policy.mDelegationMap.keyAt(i);
                    for (String scope : (List) policy.mDelegationMap.valueAt(i)) {
                        out.startTag(null, "delegation");
                        out.attribute(null, "delegatePackage", delegatePackage);
                        out.attribute(null, "scope", scope);
                        out.endTag(null, "delegation");
                    }
                }
                int N = policy.mAdminList.size();
                for (i = 0; i < N; i++) {
                    ActiveAdmin ap = (ActiveAdmin) policy.mAdminList.get(i);
                    if (ap != null) {
                        out.startTag(null, "admin");
                        out.attribute(null, ATTR_NAME, ap.info.getComponent().flattenToString());
                        ap.writeToXml(out);
                        out.endTag(null, "admin");
                    }
                }
                if (policy.mPasswordOwner >= 0) {
                    out.startTag(null, "password-owner");
                    out.attribute(null, ATTR_VALUE, Integer.toString(policy.mPasswordOwner));
                    out.endTag(null, "password-owner");
                }
                if (policy.mFailedPasswordAttempts != 0) {
                    out.startTag(null, "failed-password-attempts");
                    out.attribute(null, ATTR_VALUE, Integer.toString(policy.mFailedPasswordAttempts));
                    out.endTag(null, "failed-password-attempts");
                }
                PasswordMetrics metrics = policy.mActivePasswordMetrics;
                if (!(this.mInjector.storageManagerIsFileBasedEncryptionEnabled() || (metrics.isDefault() ^ 1) == 0)) {
                    out.startTag(null, "active-password");
                    out.attribute(null, "quality", Integer.toString(metrics.quality));
                    out.attribute(null, "length", Integer.toString(metrics.length));
                    out.attribute(null, "uppercase", Integer.toString(metrics.upperCase));
                    out.attribute(null, "lowercase", Integer.toString(metrics.lowerCase));
                    out.attribute(null, "letters", Integer.toString(metrics.letters));
                    out.attribute(null, "numeric", Integer.toString(metrics.numeric));
                    out.attribute(null, "symbols", Integer.toString(metrics.symbols));
                    out.attribute(null, "nonletter", Integer.toString(metrics.nonLetter));
                    out.endTag(null, "active-password");
                }
                if (mHwCustDevicePolicyManagerService != null && mHwCustDevicePolicyManagerService.isForbiddenSimplePwdFeatureEnable()) {
                    out.startTag(null, "is-currentpwd-simple");
                    out.attribute(null, ATTR_VALUE, Boolean.toString(policy.mIsCurrentPwdSimple));
                    out.endTag(null, "is-currentpwd-simple");
                }
                for (i = 0; i < policy.mAcceptedCaCertificates.size(); i++) {
                    out.startTag(null, TAG_ACCEPTED_CA_CERTIFICATES);
                    out.attribute(null, ATTR_NAME, (String) policy.mAcceptedCaCertificates.valueAt(i));
                    out.endTag(null, TAG_ACCEPTED_CA_CERTIFICATES);
                }
                for (i = 0; i < policy.mLockTaskPackages.size(); i++) {
                    String component = (String) policy.mLockTaskPackages.get(i);
                    out.startTag(null, TAG_LOCK_TASK_COMPONENTS);
                    out.attribute(null, ATTR_NAME, component);
                    out.endTag(null, TAG_LOCK_TASK_COMPONENTS);
                }
                if (policy.mStatusBarDisabled) {
                    out.startTag(null, TAG_STATUS_BAR);
                    out.attribute(null, ATTR_DISABLED, Boolean.toString(policy.mStatusBarDisabled));
                    out.endTag(null, TAG_STATUS_BAR);
                }
                if (policy.doNotAskCredentialsOnBoot) {
                    out.startTag(null, DO_NOT_ASK_CREDENTIALS_ON_BOOT_XML);
                    out.endTag(null, DO_NOT_ASK_CREDENTIALS_ON_BOOT_XML);
                }
                for (String id : policy.mAffiliationIds) {
                    out.startTag(null, TAG_AFFILIATION_ID);
                    out.attribute(null, ATTR_ID, id);
                    out.endTag(null, TAG_AFFILIATION_ID);
                }
                if (policy.mLastSecurityLogRetrievalTime >= 0) {
                    out.startTag(null, TAG_LAST_SECURITY_LOG_RETRIEVAL);
                    out.attribute(null, ATTR_VALUE, Long.toString(policy.mLastSecurityLogRetrievalTime));
                    out.endTag(null, TAG_LAST_SECURITY_LOG_RETRIEVAL);
                }
                if (policy.mLastBugReportRequestTime >= 0) {
                    out.startTag(null, TAG_LAST_BUG_REPORT_REQUEST);
                    out.attribute(null, ATTR_VALUE, Long.toString(policy.mLastBugReportRequestTime));
                    out.endTag(null, TAG_LAST_BUG_REPORT_REQUEST);
                }
                if (policy.mLastNetworkLogsRetrievalTime >= 0) {
                    out.startTag(null, TAG_LAST_NETWORK_LOG_RETRIEVAL);
                    out.attribute(null, ATTR_VALUE, Long.toString(policy.mLastNetworkLogsRetrievalTime));
                    out.endTag(null, TAG_LAST_NETWORK_LOG_RETRIEVAL);
                }
                if (policy.mAdminBroadcastPending) {
                    out.startTag(null, TAG_ADMIN_BROADCAST_PENDING);
                    out.attribute(null, ATTR_VALUE, Boolean.toString(policy.mAdminBroadcastPending));
                    out.endTag(null, TAG_ADMIN_BROADCAST_PENDING);
                }
                if (policy.mInitBundle != null) {
                    out.startTag(null, TAG_INITIALIZATION_BUNDLE);
                    policy.mInitBundle.saveToXml(out);
                    out.endTag(null, TAG_INITIALIZATION_BUNDLE);
                }
                if (policy.mPasswordTokenHandle != 0) {
                    out.startTag(null, TAG_PASSWORD_TOKEN_HANDLE);
                    out.attribute(null, ATTR_VALUE, Long.toString(policy.mPasswordTokenHandle));
                    out.endTag(null, TAG_PASSWORD_TOKEN_HANDLE);
                }
                if (policy.mCurrentInputMethodSet) {
                    out.startTag(null, TAG_CURRENT_INPUT_METHOD_SET);
                    out.endTag(null, TAG_CURRENT_INPUT_METHOD_SET);
                }
                for (String cert : policy.mOwnerInstalledCaCerts) {
                    out.startTag(null, TAG_OWNER_INSTALLED_CA_CERT);
                    out.attribute(null, ATTR_ALIAS, cert);
                    out.endTag(null, TAG_OWNER_INSTALLED_CA_CERT);
                }
                out.endTag(null, "policies");
                out.endDocument();
                stream.flush();
                FileUtils.sync(stream);
                stream.close();
                journal.commit();
                sendChangedNotification(userHandle);
                return;
            } catch (XmlPullParserException e) {
            }
        } catch (XmlPullParserException e2) {
        }
        journal.rollback();
    }

    private void sendChangedNotification(int userHandle) {
        Intent intent = new Intent("android.app.action.DEVICE_POLICY_MANAGER_STATE_CHANGED");
        intent.setFlags(1073741824);
        long ident = this.mInjector.binderClearCallingIdentity();
        try {
            this.mContext.sendBroadcastAsUser(intent, new UserHandle(userHandle));
        } finally {
            this.mInjector.binderRestoreCallingIdentity(ident);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:163:0x071a A:{Splitter: B:1:0x0010, ExcHandler: java.lang.NullPointerException (e java.lang.NullPointerException)} */
    /* JADX WARNING: Removed duplicated region for block: B:163:0x071a A:{Splitter: B:1:0x0010, ExcHandler: java.lang.NullPointerException (e java.lang.NullPointerException)} */
    /* JADX WARNING: Removed duplicated region for block: B:163:0x071a A:{Splitter: B:1:0x0010, ExcHandler: java.lang.NullPointerException (e java.lang.NullPointerException)} */
    /* JADX WARNING: Removed duplicated region for block: B:163:0x071a A:{Splitter: B:1:0x0010, ExcHandler: java.lang.NullPointerException (e java.lang.NullPointerException)} */
    /* JADX WARNING: Removed duplicated region for block: B:88:0x031a A:{Splitter: B:3:0x0017, ExcHandler: java.lang.NullPointerException (e java.lang.NullPointerException), PHI: r18 } */
    /* JADX WARNING: Removed duplicated region for block: B:88:0x031a A:{Splitter: B:3:0x0017, ExcHandler: java.lang.NullPointerException (e java.lang.NullPointerException), PHI: r18 } */
    /* JADX WARNING: Removed duplicated region for block: B:88:0x031a A:{Splitter: B:3:0x0017, ExcHandler: java.lang.NullPointerException (e java.lang.NullPointerException), PHI: r18 } */
    /* JADX WARNING: Removed duplicated region for block: B:88:0x031a A:{Splitter: B:3:0x0017, ExcHandler: java.lang.NullPointerException (e java.lang.NullPointerException), PHI: r18 } */
    /* JADX WARNING: Missing block: B:88:0x031a, code:
            r12 = e;
     */
    /* JADX WARNING: Missing block: B:89:0x031b, code:
            r28 = r0;
     */
    /* JADX WARNING: Missing block: B:90:0x031d, code:
            android.util.Slog.w(LOG_TAG, "failed parsing " + r14, r12);
     */
    /* JADX WARNING: Missing block: B:163:0x071a, code:
            r12 = e;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void loadSettingsLocked(DevicePolicyData policy, int userHandle) {
        FileInputStream stream = null;
        File file = makeJournaledFile(userHandle).chooseForRead();
        boolean needsRewrite = false;
        try {
            InputStream fileInputStream = new FileInputStream(file);
            try {
                int type;
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(fileInputStream, StandardCharsets.UTF_8.name());
                do {
                    type = parser.next();
                    if (type == 1) {
                        break;
                    }
                } while (type != 2);
                String tag = parser.getName();
                if ("policies".equals(tag)) {
                    List<String> scopes;
                    InputStream stream2;
                    String permissionProvider = parser.getAttributeValue(null, ATTR_PERMISSION_PROVIDER);
                    if (permissionProvider != null) {
                        policy.mRestrictionsProvider = ComponentName.unflattenFromString(permissionProvider);
                    }
                    String userSetupComplete = parser.getAttributeValue(null, ATTR_SETUP_COMPLETE);
                    if (userSetupComplete != null && Boolean.toString(true).equals(userSetupComplete)) {
                        policy.mUserSetupComplete = true;
                    }
                    String paired = parser.getAttributeValue(null, ATTR_DEVICE_PAIRED);
                    if (paired != null && Boolean.toString(true).equals(paired)) {
                        policy.mPaired = true;
                    }
                    String deviceProvisioningConfigApplied = parser.getAttributeValue(null, ATTR_DEVICE_PROVISIONING_CONFIG_APPLIED);
                    if (deviceProvisioningConfigApplied != null && Boolean.toString(true).equals(deviceProvisioningConfigApplied)) {
                        policy.mDeviceProvisioningConfigApplied = true;
                    }
                    String provisioningState = parser.getAttributeValue(null, ATTR_PROVISIONING_STATE);
                    if (!TextUtils.isEmpty(provisioningState)) {
                        policy.mUserProvisioningState = Integer.parseInt(provisioningState);
                    }
                    String permissionPolicy = parser.getAttributeValue(null, ATTR_PERMISSION_POLICY);
                    if (!TextUtils.isEmpty(permissionPolicy)) {
                        policy.mPermissionPolicy = Integer.parseInt(permissionPolicy);
                    }
                    String certDelegate = parser.getAttributeValue(null, ATTR_DELEGATED_CERT_INSTALLER);
                    if (certDelegate != null) {
                        scopes = (List) policy.mDelegationMap.get(certDelegate);
                        if (scopes == null) {
                            scopes = new ArrayList();
                            policy.mDelegationMap.put(certDelegate, scopes);
                        }
                        if (!scopes.contains("delegation-cert-install")) {
                            scopes.add("delegation-cert-install");
                            needsRewrite = true;
                        }
                    }
                    String appRestrictionsDelegate = parser.getAttributeValue(null, ATTR_APPLICATION_RESTRICTIONS_MANAGER);
                    if (appRestrictionsDelegate != null) {
                        scopes = (List) policy.mDelegationMap.get(appRestrictionsDelegate);
                        if (scopes == null) {
                            scopes = new ArrayList();
                            policy.mDelegationMap.put(appRestrictionsDelegate, scopes);
                        }
                        if (!scopes.contains("delegation-app-restrictions")) {
                            scopes.add("delegation-app-restrictions");
                            needsRewrite = true;
                        }
                    }
                    type = parser.next();
                    int outerDepth = parser.getDepth();
                    policy.mLockTaskPackages.clear();
                    policy.mAdminList.clear();
                    policy.mAdminMap.clear();
                    policy.mAffiliationIds.clear();
                    policy.mOwnerInstalledCaCerts.clear();
                    while (true) {
                        type = parser.next();
                        if (type == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                            stream2 = fileInputStream;
                        } else if (!(type == 3 || type == 4)) {
                            tag = parser.getName();
                            if ("admin".equals(tag)) {
                                String name = parser.getAttributeValue(null, ATTR_NAME);
                                try {
                                    DeviceAdminInfo dai = findAdmin(ComponentName.unflattenFromString(name), userHandle, false);
                                    if (dai != null) {
                                        ActiveAdmin ap = new ActiveAdmin(dai, false);
                                        ap.readFromXml(parser);
                                        policy.mAdminMap.put(ap.info.getComponent(), ap);
                                    }
                                } catch (RuntimeException e) {
                                    Slog.w(LOG_TAG, "Failed loading admin " + name, e);
                                }
                            } else if ("delegation".equals(tag)) {
                                String delegatePackage = parser.getAttributeValue(null, "delegatePackage");
                                String scope = parser.getAttributeValue(null, "scope");
                                scopes = (List) policy.mDelegationMap.get(delegatePackage);
                                if (scopes == null) {
                                    scopes = new ArrayList();
                                    policy.mDelegationMap.put(delegatePackage, scopes);
                                }
                                if (!scopes.contains(scope)) {
                                    scopes.add(scope);
                                }
                            } else if ("failed-password-attempts".equals(tag)) {
                                policy.mFailedPasswordAttempts = Integer.parseInt(parser.getAttributeValue(null, ATTR_VALUE));
                            } else if ("password-owner".equals(tag)) {
                                policy.mPasswordOwner = Integer.parseInt(parser.getAttributeValue(null, ATTR_VALUE));
                            } else if (TAG_ACCEPTED_CA_CERTIFICATES.equals(tag)) {
                                policy.mAcceptedCaCertificates.add(parser.getAttributeValue(null, ATTR_NAME));
                            } else if (TAG_LOCK_TASK_COMPONENTS.equals(tag)) {
                                policy.mLockTaskPackages.add(parser.getAttributeValue(null, ATTR_NAME));
                            } else if (TAG_STATUS_BAR.equals(tag)) {
                                policy.mStatusBarDisabled = Boolean.parseBoolean(parser.getAttributeValue(null, ATTR_DISABLED));
                            } else if (DO_NOT_ASK_CREDENTIALS_ON_BOOT_XML.equals(tag)) {
                                policy.doNotAskCredentialsOnBoot = true;
                            } else if (TAG_AFFILIATION_ID.equals(tag)) {
                                policy.mAffiliationIds.add(parser.getAttributeValue(null, ATTR_ID));
                            } else if (TAG_LAST_SECURITY_LOG_RETRIEVAL.equals(tag)) {
                                policy.mLastSecurityLogRetrievalTime = Long.parseLong(parser.getAttributeValue(null, ATTR_VALUE));
                            } else if (TAG_LAST_BUG_REPORT_REQUEST.equals(tag)) {
                                policy.mLastBugReportRequestTime = Long.parseLong(parser.getAttributeValue(null, ATTR_VALUE));
                            } else if (TAG_LAST_NETWORK_LOG_RETRIEVAL.equals(tag)) {
                                policy.mLastNetworkLogsRetrievalTime = Long.parseLong(parser.getAttributeValue(null, ATTR_VALUE));
                            } else if (TAG_ADMIN_BROADCAST_PENDING.equals(tag)) {
                                policy.mAdminBroadcastPending = Boolean.toString(true).equals(parser.getAttributeValue(null, ATTR_VALUE));
                            } else if (TAG_INITIALIZATION_BUNDLE.equals(tag)) {
                                policy.mInitBundle = PersistableBundle.restoreFromXml(parser);
                            } else if ("active-password".equals(tag)) {
                                if (this.mInjector.storageManagerIsFileBasedEncryptionEnabled()) {
                                    needsRewrite = true;
                                } else {
                                    PasswordMetrics m = policy.mActivePasswordMetrics;
                                    m.quality = Integer.parseInt(parser.getAttributeValue(null, "quality"));
                                    m.length = Integer.parseInt(parser.getAttributeValue(null, "length"));
                                    m.upperCase = Integer.parseInt(parser.getAttributeValue(null, "uppercase"));
                                    m.lowerCase = Integer.parseInt(parser.getAttributeValue(null, "lowercase"));
                                    m.letters = Integer.parseInt(parser.getAttributeValue(null, "letters"));
                                    m.numeric = Integer.parseInt(parser.getAttributeValue(null, "numeric"));
                                    m.symbols = Integer.parseInt(parser.getAttributeValue(null, "symbols"));
                                    m.nonLetter = Integer.parseInt(parser.getAttributeValue(null, "nonletter"));
                                }
                            } else if (TAG_PASSWORD_TOKEN_HANDLE.equals(tag)) {
                                policy.mPasswordTokenHandle = Long.parseLong(parser.getAttributeValue(null, ATTR_VALUE));
                            } else if (TAG_CURRENT_INPUT_METHOD_SET.equals(tag)) {
                                policy.mCurrentInputMethodSet = true;
                            } else if (TAG_OWNER_INSTALLED_CA_CERT.equals(tag)) {
                                policy.mOwnerInstalledCaCerts.add(parser.getAttributeValue(null, ATTR_ALIAS));
                            } else if (mHwCustDevicePolicyManagerService != null && mHwCustDevicePolicyManagerService.isForbiddenSimplePwdFeatureEnable() && "is-currentpwd-simple".equals(tag)) {
                                policy.mIsCurrentPwdSimple = Boolean.parseBoolean(parser.getAttributeValue(null, ATTR_VALUE));
                            } else {
                                Slog.w(LOG_TAG, "Unknown tag: " + tag);
                                XmlUtils.skipCurrentTag(parser);
                            }
                        }
                    }
                    stream2 = fileInputStream;
                    if (stream2 != null) {
                        try {
                            stream2.close();
                        } catch (IOException e2) {
                        }
                    }
                    policy.mAdminList.addAll(policy.mAdminMap.values());
                    if (needsRewrite) {
                        saveSettingsLocked(userHandle);
                    }
                    validatePasswordOwnerLocked(policy);
                    updateMaximumTimeToLockLocked(userHandle);
                    syncHwDeviceSettingsLocked(policy.mUserHandle);
                    updateLockTaskPackagesLocked(policy.mLockTaskPackages, userHandle);
                    if (policy.mStatusBarDisabled) {
                        setStatusBarDisabledInternal(policy.mStatusBarDisabled, userHandle);
                        return;
                    }
                    return;
                }
                throw new XmlPullParserException("Settings do not start with policies tag: found " + tag);
            } catch (FileNotFoundException e3) {
                stream2 = fileInputStream;
            } catch (NullPointerException e4) {
            }
        } catch (FileNotFoundException e5) {
        } catch (NullPointerException e6) {
        }
    }

    private void updateLockTaskPackagesLocked(List<String> packages, int userId) {
        long ident = this.mInjector.binderClearCallingIdentity();
        try {
            this.mInjector.getIActivityManager().updateLockTaskPackages(userId, (String[]) packages.toArray(new String[packages.size()]));
        } catch (RemoteException e) {
        } finally {
            this.mInjector.binderRestoreCallingIdentity(ident);
        }
    }

    private void updateDeviceOwnerLocked() {
        long ident = this.mInjector.binderClearCallingIdentity();
        try {
            ComponentName deviceOwnerComponent = this.mOwners.getDeviceOwnerComponent();
            if (deviceOwnerComponent != null) {
                this.mInjector.getIActivityManager().updateDeviceOwner(deviceOwnerComponent.getPackageName());
            }
            this.mInjector.binderRestoreCallingIdentity(ident);
        } catch (RemoteException e) {
            this.mInjector.binderRestoreCallingIdentity(ident);
        } catch (Throwable th) {
            this.mInjector.binderRestoreCallingIdentity(ident);
            throw th;
        }
    }

    static void validateQualityConstant(int quality) {
        switch (quality) {
            case 0:
            case 32768:
            case 65536:
            case DumpState.DUMP_INTENT_FILTER_VERIFIERS /*131072*/:
            case 196608:
            case DumpState.DUMP_DOMAIN_PREFERRED /*262144*/:
            case 327680:
            case 393216:
            case DumpState.DUMP_FROZEN /*524288*/:
                return;
            default:
                throw new IllegalArgumentException("Invalid quality constant: 0x" + Integer.toHexString(quality));
        }
    }

    void validatePasswordOwnerLocked(DevicePolicyData policy) {
        if (policy.mPasswordOwner >= 0) {
            boolean haveOwner = false;
            for (int i = policy.mAdminList.size() - 1; i >= 0; i--) {
                if (((ActiveAdmin) policy.mAdminList.get(i)).getUid() == policy.mPasswordOwner) {
                    haveOwner = true;
                    break;
                }
            }
            if (!haveOwner) {
                Slog.w(LOG_TAG, "Previous password owner " + policy.mPasswordOwner + " no longer active; disabling");
                policy.mPasswordOwner = -1;
            }
        }
    }

    void systemReady(int phase) {
        if (this.mHasFeature) {
            switch (phase) {
                case 480:
                    onLockSettingsReady();
                    break;
                case 1000:
                    ensureDeviceOwnerUserStarted();
                    break;
            }
        }
    }

    private void onLockSettingsReady() {
        List<String> packageList;
        getUserData(0);
        loadOwners();
        cleanUpOldUsers();
        maybeSetDefaultProfileOwnerUserRestrictions();
        handleStartUser(0);
        this.mSetupContentObserver.register();
        updateUserSetupCompleteAndPaired();
        synchronized (this) {
            packageList = getKeepUninstalledPackagesLocked();
        }
        if (packageList != null) {
            this.mInjector.getPackageManagerInternal().setKeepUninstalledPackages(packageList);
        }
        synchronized (this) {
            ActiveAdmin deviceOwner = getDeviceOwnerAdminLocked();
            if (deviceOwner != null) {
                this.mUserManagerInternal.setForceEphemeralUsers(deviceOwner.forceEphemeralUsers);
            }
        }
    }

    /* JADX WARNING: Missing block: B:9:0x0012, code:
            if (r1 == 0) goto L_0x001d;
     */
    /* JADX WARNING: Missing block: B:11:?, code:
            r4.mInjector.getIActivityManager().startUserInBackground(r1);
     */
    /* JADX WARNING: Missing block: B:16:0x0021, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:17:0x0022, code:
            android.util.Slog.w(LOG_TAG, "Exception starting user", r0);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void ensureDeviceOwnerUserStarted() {
        synchronized (this) {
            if (this.mOwners.hasDeviceOwner()) {
                int userId = this.mOwners.getDeviceOwnerUserId();
            }
        }
    }

    void handleStartUser(int userId) {
        updateScreenCaptureDisabledInWindowManager(userId, getScreenCaptureDisabled(null, userId));
        pushUserRestrictions(userId);
        startOwnerService(userId, "start-user");
    }

    void handleUnlockUser(int userId) {
        startOwnerService(userId, "unlock-user");
    }

    void handleStopUser(int userId) {
        stopOwnerService(userId, "stop-user");
    }

    private void startOwnerService(int userId, String actionForLog) {
        ComponentName owner = getOwnerComponent(userId);
        if (owner != null) {
            this.mDeviceAdminServiceController.startServiceForOwner(owner.getPackageName(), userId, actionForLog);
        }
    }

    private void stopOwnerService(int userId, String actionForLog) {
        this.mDeviceAdminServiceController.stopServiceForOwner(userId, actionForLog);
    }

    private void cleanUpOldUsers() {
        Set<Integer> usersWithProfileOwners;
        Set<Integer> usersWithData;
        synchronized (this) {
            usersWithProfileOwners = this.mOwners.getProfileOwnerKeys();
            usersWithData = new ArraySet();
            for (int i = 0; i < this.mUserData.size(); i++) {
                usersWithData.add(Integer.valueOf(this.mUserData.keyAt(i)));
            }
        }
        List<UserInfo> allUsers = this.mUserManager.getUsers();
        Set<Integer> deletedUsers = new ArraySet();
        deletedUsers.addAll(usersWithProfileOwners);
        deletedUsers.addAll(usersWithData);
        for (UserInfo userInfo : allUsers) {
            deletedUsers.remove(Integer.valueOf(userInfo.id));
        }
        for (Integer userId : deletedUsers) {
            removeUserData(userId.intValue());
        }
    }

    private void handlePasswordExpirationNotification(int userHandle) {
        Bundle adminExtras = new Bundle();
        adminExtras.putParcelable("android.intent.extra.USER", UserHandle.of(userHandle));
        synchronized (this) {
            long now = System.currentTimeMillis();
            List<ActiveAdmin> admins = getActiveAdminsForLockscreenPoliciesLocked(userHandle, false);
            int N = admins.size();
            for (int i = 0; i < N; i++) {
                ActiveAdmin admin = (ActiveAdmin) admins.get(i);
                if (admin.info.usesPolicy(6) && admin.passwordExpirationTimeout > 0 && now >= admin.passwordExpirationDate - getUsrSetExtendTime() && admin.passwordExpirationDate > 0) {
                    sendAdminCommandLocked(admin, "android.app.action.ACTION_PASSWORD_EXPIRING", adminExtras, null);
                }
            }
            setExpirationAlarmCheckLocked(this.mContext, userHandle, false);
        }
    }

    protected long getUsrSetExtendTime() {
        return EXPIRATION_GRACE_PERIOD_MS;
    }

    protected void onInstalledCertificatesChanged(UserHandle userHandle, Collection<String> installedCertificates) {
        if (this.mHasFeature) {
            enforceManageUsers();
            synchronized (this) {
                DevicePolicyData policy = getUserData(userHandle.getIdentifier());
                if (policy.mAcceptedCaCertificates.retainAll(installedCertificates) | policy.mOwnerInstalledCaCerts.retainAll(installedCertificates)) {
                    saveSettingsLocked(userHandle.getIdentifier());
                }
            }
        }
    }

    protected Set<String> getAcceptedCaCertificates(UserHandle userHandle) {
        if (!this.mHasFeature) {
            return Collections.emptySet();
        }
        Set set;
        synchronized (this) {
            set = getUserData(userHandle.getIdentifier()).mAcceptedCaCertificates;
        }
        return set;
    }

    public void setActiveAdmin(ComponentName adminReceiver, boolean refreshing, int userHandle) {
        if (this.mHasFeature) {
            setActiveAdmin(adminReceiver, refreshing, userHandle, null);
        }
    }

    private void setActiveAdmin(ComponentName adminReceiver, boolean refreshing, int userHandle, Bundle onEnableData) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_DEVICE_ADMINS", null);
        enforceFullCrossUsersPermission(userHandle);
        DevicePolicyData policy = getUserData(userHandle);
        DeviceAdminInfo info = findAdmin(adminReceiver, userHandle, true);
        if (info == null) {
            throw new IllegalArgumentException("Bad admin: " + adminReceiver);
        } else if (!info.getActivityInfo().applicationInfo.isInternal()) {
            throw new IllegalArgumentException("Only apps in internal storage can be active admin: " + adminReceiver);
        } else if (info.getActivityInfo().applicationInfo.isInstantApp()) {
            throw new IllegalArgumentException("Instant apps cannot be device admins: " + adminReceiver);
        } else {
            synchronized (this) {
                long ident = this.mInjector.binderClearCallingIdentity();
                try {
                    ActiveAdmin existingAdmin = getActiveAdminUncheckedLocked(adminReceiver, userHandle);
                    if (!refreshing && existingAdmin != null) {
                        throw new IllegalArgumentException("Admin is already added");
                    } else if (policy.mRemovingAdmins.contains(adminReceiver)) {
                        throw new IllegalArgumentException("Trying to set an admin which is being removed");
                    } else {
                        boolean z;
                        ActiveAdmin newAdmin = new ActiveAdmin(info, false);
                        if (existingAdmin != null) {
                            z = existingAdmin.testOnlyAdmin;
                        } else {
                            z = isPackageTestOnly(adminReceiver.getPackageName(), userHandle);
                        }
                        newAdmin.testOnlyAdmin = z;
                        policy.mAdminMap.put(adminReceiver, newAdmin);
                        int replaceIndex = -1;
                        int N = policy.mAdminList.size();
                        for (int i = 0; i < N; i++) {
                            if (((ActiveAdmin) policy.mAdminList.get(i)).info.getComponent().equals(adminReceiver)) {
                                replaceIndex = i;
                                break;
                            }
                        }
                        if (replaceIndex == -1) {
                            policy.mAdminList.add(newAdmin);
                            enableIfNecessary(info.getPackageName(), userHandle);
                        } else {
                            policy.mAdminList.set(replaceIndex, newAdmin);
                        }
                        saveSettingsLocked(userHandle);
                        sendAdminCommandLocked(newAdmin, "android.app.action.DEVICE_ADMIN_ENABLED", onEnableData, null);
                        Flog.i(305, "setActiveAdmin(" + adminReceiver + "), by user " + userHandle);
                        this.mInjector.binderRestoreCallingIdentity(ident);
                    }
                } catch (Throwable th) {
                    this.mInjector.binderRestoreCallingIdentity(ident);
                }
            }
        }
    }

    public boolean isAdminActive(ComponentName adminReceiver, int userHandle) {
        if (this.mHasFeature) {
            boolean isAdminActive;
            enforceFullCrossUsersPermission(userHandle);
            synchronized (this) {
                isAdminActive = getActiveAdminUncheckedLocked(adminReceiver, userHandle) != null;
                Flog.i(305, "isAdminActive(" + adminReceiver + "), by user " + userHandle + ", return " + isAdminActive);
            }
            return isAdminActive;
        }
        Flog.i(305, "isAdminActive(" + adminReceiver + "), by user " + userHandle + ", return false, cause no feature");
        return false;
    }

    public boolean isRemovingAdmin(ComponentName adminReceiver, int userHandle) {
        if (!this.mHasFeature) {
            return false;
        }
        boolean contains;
        enforceFullCrossUsersPermission(userHandle);
        synchronized (this) {
            contains = getUserData(userHandle).mRemovingAdmins.contains(adminReceiver);
        }
        return contains;
    }

    public boolean hasGrantedPolicy(ComponentName adminReceiver, int policyId, int userHandle) {
        if (!this.mHasFeature) {
            return false;
        }
        boolean usesPolicy;
        enforceFullCrossUsersPermission(userHandle);
        synchronized (this) {
            ActiveAdmin administrator = getActiveAdminUncheckedLocked(adminReceiver, userHandle);
            if (administrator == null) {
                throw new SecurityException("No active admin " + adminReceiver);
            }
            usesPolicy = administrator.info.usesPolicy(policyId);
        }
        return usesPolicy;
    }

    public List<ComponentName> getActiveAdmins(int userHandle) {
        if (!this.mHasFeature) {
            return Collections.EMPTY_LIST;
        }
        enforceFullCrossUsersPermission(userHandle);
        synchronized (this) {
            DevicePolicyData policy = getUserData(userHandle);
            int N = policy.mAdminList.size();
            if (N <= 0) {
                return null;
            }
            ArrayList<ComponentName> res = new ArrayList(N);
            for (int i = 0; i < N; i++) {
                res.add(((ActiveAdmin) policy.mAdminList.get(i)).info.getComponent());
            }
            return res;
        }
    }

    public boolean packageHasActiveAdmins(String packageName, int userHandle) {
        if (!this.mHasFeature) {
            return false;
        }
        enforceFullCrossUsersPermission(userHandle);
        synchronized (this) {
            DevicePolicyData policy = getUserData(userHandle);
            int N = policy.mAdminList.size();
            for (int i = 0; i < N; i++) {
                if (((ActiveAdmin) policy.mAdminList.get(i)).info.getPackageName().equals(packageName)) {
                    return true;
                }
            }
            return false;
        }
    }

    public void forceRemoveActiveAdmin(ComponentName adminReceiver, int userHandle) {
        if (this.mHasFeature) {
            Preconditions.checkNotNull(adminReceiver, "ComponentName is null");
            enforceShell("forceRemoveActiveAdmin");
            long ident = this.mInjector.binderClearCallingIdentity();
            try {
                synchronized (this) {
                    if (isAdminTestOnlyLocked(adminReceiver, userHandle)) {
                        if (isDeviceOwner(adminReceiver, userHandle)) {
                            clearDeviceOwnerLocked(getDeviceOwnerAdminLocked(), userHandle);
                        }
                        if (isProfileOwner(adminReceiver, userHandle)) {
                            clearProfileOwnerLocked(getActiveAdminUncheckedLocked(adminReceiver, userHandle, false), userHandle);
                        }
                    } else {
                        throw new SecurityException("Attempt to remove non-test admin " + adminReceiver + " " + userHandle);
                    }
                }
                removeAdminArtifacts(adminReceiver, userHandle);
                Slog.i(LOG_TAG, "Admin " + adminReceiver + " removed from user " + userHandle);
            } finally {
                this.mInjector.binderRestoreCallingIdentity(ident);
            }
        }
    }

    private void clearDeviceOwnerUserRestrictionLocked(UserHandle userHandle) {
        if (this.mUserManager.hasUserRestriction("no_add_user", userHandle)) {
            this.mUserManager.setUserRestriction("no_add_user", false, userHandle);
        }
    }

    private boolean isPackageTestOnly(String packageName, int userHandle) {
        try {
            ApplicationInfo ai = this.mIPackageManager.getApplicationInfo(packageName, 786432, userHandle);
            if (ai == null) {
                throw new IllegalStateException("Couldn't find package: " + packageName + " on user " + userHandle);
            } else if ((ai.flags & 256) != 0) {
                return true;
            } else {
                return false;
            }
        } catch (RemoteException e) {
            throw new IllegalStateException(e);
        }
    }

    private boolean isAdminTestOnlyLocked(ComponentName who, int userHandle) {
        ActiveAdmin admin = getActiveAdminUncheckedLocked(who, userHandle);
        return admin != null ? admin.testOnlyAdmin : false;
    }

    private void enforceShell(String method) {
        int callingUid = Binder.getCallingUid();
        if (callingUid != IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME && callingUid != 0) {
            throw new SecurityException("Non-shell user attempted to call " + method);
        }
    }

    public void removeActiveAdmin(ComponentName adminReceiver, int userHandle) {
        if (this.mHasFeature) {
            enforceFullCrossUsersPermission(userHandle);
            enforceUserUnlocked(userHandle);
            synchronized (this) {
                ActiveAdmin admin = getActiveAdminUncheckedLocked(adminReceiver, userHandle);
                if (admin == null) {
                } else if (isDeviceOwner(adminReceiver, userHandle) || isProfileOwner(adminReceiver, userHandle)) {
                    Slog.e(LOG_TAG, "Device/profile owner cannot be removed: component=" + adminReceiver);
                } else {
                    if (admin.getUid() != this.mInjector.binderGetCallingUid()) {
                        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_DEVICE_ADMINS", null);
                    }
                    long ident = this.mInjector.binderClearCallingIdentity();
                    try {
                        notifyPlugins(adminReceiver, userHandle);
                        removeActiveAdminLocked(adminReceiver, userHandle);
                        this.mInjector.binderRestoreCallingIdentity(ident);
                    } catch (Throwable th) {
                        this.mInjector.binderRestoreCallingIdentity(ident);
                    }
                }
            }
        }
    }

    public boolean isSeparateProfileChallengeAllowed(int userHandle) {
        ComponentName profileOwner = getProfileOwner(userHandle);
        if (profileOwner == null || getTargetSdk(profileOwner.getPackageName(), userHandle) <= 23) {
            return false;
        }
        return true;
    }

    public void setPasswordQuality(ComponentName who, int quality, boolean parent) {
        if (this.mHasFeature) {
            Preconditions.checkNotNull(who, "ComponentName is null");
            validateQualityConstant(quality);
            synchronized (this) {
                ActiveAdmin ap = getActiveAdminForCallerLocked(who, 0, parent);
                if (ap.minimumPasswordMetrics.quality != quality) {
                    ap.minimumPasswordMetrics.quality = quality;
                    saveSettingsLocked(this.mInjector.userHandleGetCallingUserId());
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:12:0x0018, code:
            return r5;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int getPasswordQuality(ComponentName who, int userHandle, boolean parent) {
        int i = 0;
        if (!this.mHasFeature) {
            return 0;
        }
        enforceFullCrossUsersPermission(userHandle);
        synchronized (this) {
            int mode = 0;
            ActiveAdmin admin;
            if (who != null) {
                admin = getActiveAdminUncheckedLocked(who, userHandle, parent);
                if (admin != null) {
                    i = admin.minimumPasswordMetrics.quality;
                }
            } else {
                List<ActiveAdmin> admins = getActiveAdminsForLockscreenPoliciesLocked(userHandle, parent);
                int N = admins.size();
                for (int i2 = 0; i2 < N; i2++) {
                    admin = (ActiveAdmin) admins.get(i2);
                    if (mode < admin.minimumPasswordMetrics.quality) {
                        mode = admin.minimumPasswordMetrics.quality;
                    }
                }
                return mode;
            }
        }
    }

    private List<ActiveAdmin> getActiveAdminsForLockscreenPoliciesLocked(int userHandle, boolean parent) {
        if (!parent && isSeparateProfileChallengeEnabled(userHandle)) {
            return getUserDataUnchecked(userHandle).mAdminList;
        }
        ArrayList<ActiveAdmin> admins = new ArrayList();
        for (UserInfo userInfo : this.mUserManager.getProfiles(userHandle)) {
            DevicePolicyData policy = getUserData(userInfo.id);
            if (userInfo.isManagedProfile()) {
                boolean hasSeparateChallenge = isSeparateProfileChallengeEnabled(userInfo.id);
                int N = policy.mAdminList.size();
                for (int i = 0; i < N; i++) {
                    ActiveAdmin admin = (ActiveAdmin) policy.mAdminList.get(i);
                    if (admin.hasParentActiveAdmin()) {
                        admins.add(admin.getParentActiveAdmin());
                    }
                    if (!hasSeparateChallenge) {
                        admins.add(admin);
                    }
                }
            } else {
                admins.addAll(policy.mAdminList);
            }
        }
        return admins;
    }

    private boolean isSeparateProfileChallengeEnabled(int userHandle) {
        long ident = this.mInjector.binderClearCallingIdentity();
        try {
            boolean isSeparateProfileChallengeEnabled = this.mLockPatternUtils.isSeparateProfileChallengeEnabled(userHandle);
            return isSeparateProfileChallengeEnabled;
        } finally {
            this.mInjector.binderRestoreCallingIdentity(ident);
        }
    }

    public void setPasswordMinimumLength(ComponentName who, int length, boolean parent) {
        if (this.mHasFeature) {
            Preconditions.checkNotNull(who, "ComponentName is null");
            synchronized (this) {
                ActiveAdmin ap = getActiveAdminForCallerLocked(who, 0, parent);
                if (ap.minimumPasswordMetrics.length != length) {
                    ap.minimumPasswordMetrics.length = length;
                    saveSettingsLocked(this.mInjector.userHandleGetCallingUserId());
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:12:0x0018, code:
            return r5;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int getPasswordMinimumLength(ComponentName who, int userHandle, boolean parent) {
        int i = 0;
        if (!this.mHasFeature) {
            return 0;
        }
        enforceFullCrossUsersPermission(userHandle);
        synchronized (this) {
            int length = 0;
            ActiveAdmin admin;
            if (who != null) {
                admin = getActiveAdminUncheckedLocked(who, userHandle, parent);
                if (admin != null) {
                    i = admin.minimumPasswordMetrics.length;
                }
            } else {
                List<ActiveAdmin> admins = getActiveAdminsForLockscreenPoliciesLocked(userHandle, parent);
                int N = admins.size();
                for (int i2 = 0; i2 < N; i2++) {
                    admin = (ActiveAdmin) admins.get(i2);
                    if (length < admin.minimumPasswordMetrics.length) {
                        length = admin.minimumPasswordMetrics.length;
                    }
                }
                return length;
            }
        }
    }

    public void setPasswordHistoryLength(ComponentName who, int length, boolean parent) {
        if (this.mHasFeature) {
            Preconditions.checkNotNull(who, "ComponentName is null");
            synchronized (this) {
                ActiveAdmin ap = getActiveAdminForCallerLocked(who, 0, parent);
                if (ap.passwordHistoryLength != length) {
                    ap.passwordHistoryLength = length;
                    saveSettingsLocked(this.mInjector.userHandleGetCallingUserId());
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:12:0x0016, code:
            return r5;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int getPasswordHistoryLength(ComponentName who, int userHandle, boolean parent) {
        int i = 0;
        if (!this.mHasFeature) {
            return 0;
        }
        enforceFullCrossUsersPermission(userHandle);
        synchronized (this) {
            int length = 0;
            ActiveAdmin admin;
            if (who != null) {
                admin = getActiveAdminUncheckedLocked(who, userHandle, parent);
                if (admin != null) {
                    i = admin.passwordHistoryLength;
                }
            } else {
                List<ActiveAdmin> admins = getActiveAdminsForLockscreenPoliciesLocked(userHandle, parent);
                int N = admins.size();
                for (int i2 = 0; i2 < N; i2++) {
                    admin = (ActiveAdmin) admins.get(i2);
                    if (length < admin.passwordHistoryLength) {
                        length = admin.passwordHistoryLength;
                    }
                }
                return length;
            }
        }
    }

    public void setPasswordExpirationTimeout(ComponentName who, long timeout, boolean parent) {
        if (this.mHasFeature) {
            Preconditions.checkNotNull(who, "ComponentName is null");
            Preconditions.checkArgumentNonnegative(timeout, "Timeout must be >= 0 ms");
            int userHandle = this.mInjector.userHandleGetCallingUserId();
            synchronized (this) {
                ActiveAdmin ap = getActiveAdminForCallerLocked(who, 6, parent);
                long expiration = timeout > 0 ? timeout + System.currentTimeMillis() : 0;
                ap.passwordExpirationDate = expiration;
                ap.passwordExpirationTimeout = timeout;
                if (timeout > 0) {
                    Slog.w(LOG_TAG, "setPasswordExpiration(): password will expire on " + DateFormat.getDateTimeInstance(2, 2).format(new Date(expiration)));
                }
                saveSettingsLocked(userHandle);
                setExpirationAlarmCheckLocked(this.mContext, userHandle, parent);
            }
        }
    }

    /* JADX WARNING: Missing block: B:12:0x0018, code:
            return r6;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public long getPasswordExpirationTimeout(ComponentName who, int userHandle, boolean parent) {
        long j = 0;
        if (!this.mHasFeature) {
            return 0;
        }
        enforceFullCrossUsersPermission(userHandle);
        synchronized (this) {
            long timeout = 0;
            ActiveAdmin admin;
            if (who != null) {
                admin = getActiveAdminUncheckedLocked(who, userHandle, parent);
                if (admin != null) {
                    j = admin.passwordExpirationTimeout;
                }
            } else {
                List<ActiveAdmin> admins = getActiveAdminsForLockscreenPoliciesLocked(userHandle, parent);
                int N = admins.size();
                for (int i = 0; i < N; i++) {
                    admin = (ActiveAdmin) admins.get(i);
                    if (timeout == 0 || (admin.passwordExpirationTimeout != 0 && timeout > admin.passwordExpirationTimeout)) {
                        timeout = admin.passwordExpirationTimeout;
                    }
                }
                return timeout;
            }
        }
    }

    /* JADX WARNING: Missing block: B:14:0x002b, code:
            if (r1 == null) goto L_0x0037;
     */
    /* JADX WARNING: Missing block: B:15:0x002d, code:
            com.android.server.devicepolicy.DevicePolicyManagerService.LocalService.-wrap0(r6.mLocalService, r4, r1);
     */
    /* JADX WARNING: Missing block: B:16:0x0033, code:
            return true;
     */
    /* JADX WARNING: Missing block: B:21:0x0038, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean addCrossProfileWidgetProvider(ComponentName admin, String packageName) {
        Throwable th;
        int userId = UserHandle.getCallingUserId();
        List list = null;
        synchronized (this) {
            try {
                ActiveAdmin activeAdmin = getActiveAdminForCallerLocked(admin, -1);
                if (activeAdmin.crossProfileWidgetProviders == null) {
                    activeAdmin.crossProfileWidgetProviders = new ArrayList();
                }
                List<String> providers = activeAdmin.crossProfileWidgetProviders;
                if (!providers.contains(packageName)) {
                    providers.add(packageName);
                    List<String> changedProviders = new ArrayList(providers);
                    try {
                        saveSettingsLocked(userId);
                        list = changedProviders;
                    } catch (Throwable th2) {
                        th = th2;
                        throw th;
                    }
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
    }

    /* JADX WARNING: Missing block: B:9:0x0019, code:
            return false;
     */
    /* JADX WARNING: Missing block: B:18:0x002c, code:
            if (r1 == null) goto L_0x0038;
     */
    /* JADX WARNING: Missing block: B:19:0x002e, code:
            com.android.server.devicepolicy.DevicePolicyManagerService.LocalService.-wrap0(r7.mLocalService, r4, r1);
     */
    /* JADX WARNING: Missing block: B:20:0x0034, code:
            return true;
     */
    /* JADX WARNING: Missing block: B:24:0x0038, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean removeCrossProfileWidgetProvider(ComponentName admin, String packageName) {
        Throwable th;
        int userId = UserHandle.getCallingUserId();
        List list = null;
        synchronized (this) {
            try {
                ActiveAdmin activeAdmin = getActiveAdminForCallerLocked(admin, -1);
                if (activeAdmin.crossProfileWidgetProviders == null || activeAdmin.crossProfileWidgetProviders.isEmpty()) {
                } else {
                    List<String> providers = activeAdmin.crossProfileWidgetProviders;
                    if (providers.remove(packageName)) {
                        List<String> changedProviders = new ArrayList(providers);
                        try {
                            saveSettingsLocked(userId);
                            list = changedProviders;
                        } catch (Throwable th2) {
                            th = th2;
                            throw th;
                        }
                    }
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
    }

    /* JADX WARNING: Missing block: B:9:0x0014, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public List<String> getCrossProfileWidgetProviders(ComponentName admin) {
        synchronized (this) {
            ActiveAdmin activeAdmin = getActiveAdminForCallerLocked(admin, -1);
            if (activeAdmin.crossProfileWidgetProviders == null || activeAdmin.crossProfileWidgetProviders.isEmpty()) {
            } else if (this.mInjector.binderIsCallingUidMyUid()) {
                List arrayList = new ArrayList(activeAdmin.crossProfileWidgetProviders);
                return arrayList;
            } else {
                List<String> list = activeAdmin.crossProfileWidgetProviders;
                return list;
            }
        }
    }

    private long getPasswordExpirationLocked(ComponentName who, int userHandle, boolean parent) {
        long j = 0;
        long timeout = 0;
        ActiveAdmin admin;
        if (who != null) {
            admin = getActiveAdminUncheckedLocked(who, userHandle, parent);
            if (admin != null) {
                j = admin.passwordExpirationDate;
            }
            return j;
        }
        List<ActiveAdmin> admins = getActiveAdminsForLockscreenPoliciesLocked(userHandle, parent);
        int N = admins.size();
        for (int i = 0; i < N; i++) {
            admin = (ActiveAdmin) admins.get(i);
            if (timeout == 0 || (admin.passwordExpirationDate != 0 && timeout > admin.passwordExpirationDate)) {
                timeout = admin.passwordExpirationDate;
            }
        }
        return timeout;
    }

    public long getPasswordExpiration(ComponentName who, int userHandle, boolean parent) {
        if (!this.mHasFeature) {
            return 0;
        }
        long passwordExpirationLocked;
        enforceFullCrossUsersPermission(userHandle);
        synchronized (this) {
            passwordExpirationLocked = getPasswordExpirationLocked(who, userHandle, parent);
        }
        return passwordExpirationLocked;
    }

    public void setPasswordMinimumUpperCase(ComponentName who, int length, boolean parent) {
        if (this.mHasFeature) {
            Preconditions.checkNotNull(who, "ComponentName is null");
            synchronized (this) {
                ActiveAdmin ap = getActiveAdminForCallerLocked(who, 0, parent);
                if (ap.minimumPasswordMetrics.upperCase != length) {
                    ap.minimumPasswordMetrics.upperCase = length;
                    saveSettingsLocked(this.mInjector.userHandleGetCallingUserId());
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:12:0x0018, code:
            return r5;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int getPasswordMinimumUpperCase(ComponentName who, int userHandle, boolean parent) {
        int i = 0;
        if (!this.mHasFeature) {
            return 0;
        }
        enforceFullCrossUsersPermission(userHandle);
        synchronized (this) {
            int length = 0;
            ActiveAdmin admin;
            if (who != null) {
                admin = getActiveAdminUncheckedLocked(who, userHandle, parent);
                if (admin != null) {
                    i = admin.minimumPasswordMetrics.upperCase;
                }
            } else {
                List<ActiveAdmin> admins = getActiveAdminsForLockscreenPoliciesLocked(userHandle, parent);
                int N = admins.size();
                for (int i2 = 0; i2 < N; i2++) {
                    admin = (ActiveAdmin) admins.get(i2);
                    if (length < admin.minimumPasswordMetrics.upperCase) {
                        length = admin.minimumPasswordMetrics.upperCase;
                    }
                }
                return length;
            }
        }
    }

    public void setPasswordMinimumLowerCase(ComponentName who, int length, boolean parent) {
        Preconditions.checkNotNull(who, "ComponentName is null");
        synchronized (this) {
            ActiveAdmin ap = getActiveAdminForCallerLocked(who, 0, parent);
            if (ap.minimumPasswordMetrics.lowerCase != length) {
                ap.minimumPasswordMetrics.lowerCase = length;
                saveSettingsLocked(this.mInjector.userHandleGetCallingUserId());
            }
        }
    }

    /* JADX WARNING: Missing block: B:12:0x0018, code:
            return r5;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int getPasswordMinimumLowerCase(ComponentName who, int userHandle, boolean parent) {
        int i = 0;
        if (!this.mHasFeature) {
            return 0;
        }
        enforceFullCrossUsersPermission(userHandle);
        synchronized (this) {
            int length = 0;
            ActiveAdmin admin;
            if (who != null) {
                admin = getActiveAdminUncheckedLocked(who, userHandle, parent);
                if (admin != null) {
                    i = admin.minimumPasswordMetrics.lowerCase;
                }
            } else {
                List<ActiveAdmin> admins = getActiveAdminsForLockscreenPoliciesLocked(userHandle, parent);
                int N = admins.size();
                for (int i2 = 0; i2 < N; i2++) {
                    admin = (ActiveAdmin) admins.get(i2);
                    if (length < admin.minimumPasswordMetrics.lowerCase) {
                        length = admin.minimumPasswordMetrics.lowerCase;
                    }
                }
                return length;
            }
        }
    }

    public void setPasswordMinimumLetters(ComponentName who, int length, boolean parent) {
        if (this.mHasFeature) {
            Preconditions.checkNotNull(who, "ComponentName is null");
            synchronized (this) {
                ActiveAdmin ap = getActiveAdminForCallerLocked(who, 0, parent);
                if (ap.minimumPasswordMetrics.letters != length) {
                    ap.minimumPasswordMetrics.letters = length;
                    saveSettingsLocked(this.mInjector.userHandleGetCallingUserId());
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:12:0x0018, code:
            return r5;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int getPasswordMinimumLetters(ComponentName who, int userHandle, boolean parent) {
        int i = 0;
        if (!this.mHasFeature) {
            return 0;
        }
        enforceFullCrossUsersPermission(userHandle);
        synchronized (this) {
            int length = 0;
            ActiveAdmin admin;
            if (who != null) {
                admin = getActiveAdminUncheckedLocked(who, userHandle, parent);
                if (admin != null) {
                    i = admin.minimumPasswordMetrics.letters;
                }
            } else {
                List<ActiveAdmin> admins = getActiveAdminsForLockscreenPoliciesLocked(userHandle, parent);
                int N = admins.size();
                for (int i2 = 0; i2 < N; i2++) {
                    admin = (ActiveAdmin) admins.get(i2);
                    if (isLimitPasswordAllowed(admin, 393216) && length < admin.minimumPasswordMetrics.letters) {
                        length = admin.minimumPasswordMetrics.letters;
                    }
                }
                return length;
            }
        }
    }

    public void setPasswordMinimumNumeric(ComponentName who, int length, boolean parent) {
        if (this.mHasFeature) {
            Preconditions.checkNotNull(who, "ComponentName is null");
            synchronized (this) {
                ActiveAdmin ap = getActiveAdminForCallerLocked(who, 0, parent);
                if (ap.minimumPasswordMetrics.numeric != length) {
                    ap.minimumPasswordMetrics.numeric = length;
                    saveSettingsLocked(this.mInjector.userHandleGetCallingUserId());
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:12:0x0018, code:
            return r5;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int getPasswordMinimumNumeric(ComponentName who, int userHandle, boolean parent) {
        int i = 0;
        if (!this.mHasFeature) {
            return 0;
        }
        enforceFullCrossUsersPermission(userHandle);
        synchronized (this) {
            int length = 0;
            ActiveAdmin admin;
            if (who != null) {
                admin = getActiveAdminUncheckedLocked(who, userHandle, parent);
                if (admin != null) {
                    i = admin.minimumPasswordMetrics.numeric;
                }
            } else {
                List<ActiveAdmin> admins = getActiveAdminsForLockscreenPoliciesLocked(userHandle, parent);
                int N = admins.size();
                for (int i2 = 0; i2 < N; i2++) {
                    admin = (ActiveAdmin) admins.get(i2);
                    if (isLimitPasswordAllowed(admin, 393216) && length < admin.minimumPasswordMetrics.numeric) {
                        length = admin.minimumPasswordMetrics.numeric;
                    }
                }
                return length;
            }
        }
    }

    public void setPasswordMinimumSymbols(ComponentName who, int length, boolean parent) {
        if (this.mHasFeature) {
            Preconditions.checkNotNull(who, "ComponentName is null");
            synchronized (this) {
                ActiveAdmin ap = getActiveAdminForCallerLocked(who, 0, parent);
                if (ap.minimumPasswordMetrics.symbols != length) {
                    ap.minimumPasswordMetrics.symbols = length;
                    saveSettingsLocked(this.mInjector.userHandleGetCallingUserId());
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:12:0x0018, code:
            return r5;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int getPasswordMinimumSymbols(ComponentName who, int userHandle, boolean parent) {
        int i = 0;
        if (!this.mHasFeature) {
            return 0;
        }
        enforceFullCrossUsersPermission(userHandle);
        synchronized (this) {
            int length = 0;
            ActiveAdmin admin;
            if (who != null) {
                admin = getActiveAdminUncheckedLocked(who, userHandle, parent);
                if (admin != null) {
                    i = admin.minimumPasswordMetrics.symbols;
                }
            } else {
                List<ActiveAdmin> admins = getActiveAdminsForLockscreenPoliciesLocked(userHandle, parent);
                int N = admins.size();
                for (int i2 = 0; i2 < N; i2++) {
                    admin = (ActiveAdmin) admins.get(i2);
                    if (isLimitPasswordAllowed(admin, 393216) && length < admin.minimumPasswordMetrics.symbols) {
                        length = admin.minimumPasswordMetrics.symbols;
                    }
                }
                return length;
            }
        }
    }

    public void setPasswordMinimumNonLetter(ComponentName who, int length, boolean parent) {
        if (this.mHasFeature) {
            Preconditions.checkNotNull(who, "ComponentName is null");
            synchronized (this) {
                ActiveAdmin ap = getActiveAdminForCallerLocked(who, 0, parent);
                if (ap.minimumPasswordMetrics.nonLetter != length) {
                    ap.minimumPasswordMetrics.nonLetter = length;
                    saveSettingsLocked(this.mInjector.userHandleGetCallingUserId());
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:12:0x0018, code:
            return r5;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int getPasswordMinimumNonLetter(ComponentName who, int userHandle, boolean parent) {
        int i = 0;
        if (!this.mHasFeature) {
            return 0;
        }
        enforceFullCrossUsersPermission(userHandle);
        synchronized (this) {
            int length = 0;
            ActiveAdmin admin;
            if (who != null) {
                admin = getActiveAdminUncheckedLocked(who, userHandle, parent);
                if (admin != null) {
                    i = admin.minimumPasswordMetrics.nonLetter;
                }
            } else {
                List<ActiveAdmin> admins = getActiveAdminsForLockscreenPoliciesLocked(userHandle, parent);
                int N = admins.size();
                for (int i2 = 0; i2 < N; i2++) {
                    admin = (ActiveAdmin) admins.get(i2);
                    if (isLimitPasswordAllowed(admin, 393216) && length < admin.minimumPasswordMetrics.nonLetter) {
                        length = admin.minimumPasswordMetrics.nonLetter;
                    }
                }
                return length;
            }
        }
    }

    public boolean isActivePasswordSufficient(int userHandle, boolean parent) {
        if (!this.mHasFeature) {
            return true;
        }
        boolean isActivePasswordSufficientForUserLocked;
        enforceFullCrossUsersPermission(userHandle);
        synchronized (this) {
            getActiveAdminForCallerLocked(null, 0, parent);
            isActivePasswordSufficientForUserLocked = isActivePasswordSufficientForUserLocked(getUserDataUnchecked(getCredentialOwner(userHandle, parent)), userHandle, parent);
        }
        return isActivePasswordSufficientForUserLocked;
    }

    public boolean isProfileActivePasswordSufficientForParent(int userHandle) {
        if (!this.mHasFeature) {
            return true;
        }
        boolean isActivePasswordSufficientForUserLocked;
        enforceFullCrossUsersPermission(userHandle);
        enforceManagedProfile(userHandle, "call APIs refering to the parent profile");
        synchronized (this) {
            isActivePasswordSufficientForUserLocked = isActivePasswordSufficientForUserLocked(getUserDataUnchecked(getCredentialOwner(userHandle, false)), getProfileParentId(userHandle), false);
        }
        return isActivePasswordSufficientForUserLocked;
    }

    private boolean isActivePasswordSufficientForUserLocked(DevicePolicyData policy, int userHandle, boolean parent) {
        boolean z = true;
        enforceUserUnlocked(userHandle, parent);
        int requiredPasswordQuality = getPasswordQuality(null, userHandle, parent);
        if (policy.mActivePasswordMetrics.quality < requiredPasswordQuality) {
            return false;
        }
        if (requiredPasswordQuality >= DumpState.DUMP_INTENT_FILTER_VERIFIERS && policy.mActivePasswordMetrics.length < getPasswordMinimumLength(null, userHandle, parent)) {
            return false;
        }
        if (mHwCustDevicePolicyManagerService != null && mHwCustDevicePolicyManagerService.isForbiddenSimplePwdFeatureEnable() && (getAllowSimplePassword(null, userHandle) ^ 1) != 0 && policy.mIsCurrentPwdSimple) {
            Slog.d(LOG_TAG, "forbiddenSimplePassword and current pwd is simple return false");
            return false;
        } else if (requiredPasswordQuality != 393216) {
            return true;
        } else {
            if (policy.mActivePasswordMetrics.upperCase < getPasswordMinimumUpperCase(null, userHandle, parent) || policy.mActivePasswordMetrics.lowerCase < getPasswordMinimumLowerCase(null, userHandle, parent) || policy.mActivePasswordMetrics.letters < getPasswordMinimumLetters(null, userHandle, parent) || policy.mActivePasswordMetrics.numeric < getPasswordMinimumNumeric(null, userHandle, parent) || policy.mActivePasswordMetrics.symbols < getPasswordMinimumSymbols(null, userHandle, parent)) {
                z = false;
            } else if (policy.mActivePasswordMetrics.nonLetter < getPasswordMinimumNonLetter(null, userHandle, parent)) {
                z = false;
            }
            return z;
        }
    }

    public int getCurrentFailedPasswordAttempts(int userHandle, boolean parent) {
        int i;
        enforceFullCrossUsersPermission(userHandle);
        synchronized (this) {
            if (!isCallerWithSystemUid()) {
                getActiveAdminForCallerLocked(null, 1, parent);
            }
            i = getUserDataUnchecked(getCredentialOwner(userHandle, parent)).mFailedPasswordAttempts;
        }
        return i;
    }

    public void setMaximumFailedPasswordsForWipe(ComponentName who, int num, boolean parent) {
        if (this.mHasFeature) {
            Preconditions.checkNotNull(who, "ComponentName is null");
            synchronized (this) {
                getActiveAdminForCallerLocked(who, 4, parent);
                ActiveAdmin ap = getActiveAdminForCallerLocked(who, 1, parent);
                if (ap.maximumFailedPasswordsForWipe != num) {
                    ap.maximumFailedPasswordsForWipe = num;
                    saveSettingsLocked(this.mInjector.userHandleGetCallingUserId());
                }
            }
        }
    }

    public int getMaximumFailedPasswordsForWipe(ComponentName who, int userHandle, boolean parent) {
        int i = 0;
        if (!this.mHasFeature) {
            return 0;
        }
        enforceFullCrossUsersPermission(userHandle);
        synchronized (this) {
            ActiveAdmin admin;
            if (who != null) {
                admin = getActiveAdminUncheckedLocked(who, userHandle, parent);
            } else {
                admin = getAdminWithMinimumFailedPasswordsForWipeLocked(userHandle, parent);
            }
            if (admin != null) {
                i = admin.maximumFailedPasswordsForWipe;
            }
        }
        return i;
    }

    public int getProfileWithMinimumFailedPasswordsForWipe(int userHandle, boolean parent) {
        int i = -10000;
        if (!this.mHasFeature) {
            return -10000;
        }
        enforceFullCrossUsersPermission(userHandle);
        synchronized (this) {
            ActiveAdmin admin = getAdminWithMinimumFailedPasswordsForWipeLocked(userHandle, parent);
            if (admin != null) {
                i = admin.getUserHandle().getIdentifier();
            }
        }
        return i;
    }

    private ActiveAdmin getAdminWithMinimumFailedPasswordsForWipeLocked(int userHandle, boolean parent) {
        int count = 0;
        ActiveAdmin strictestAdmin = null;
        List<ActiveAdmin> admins = getActiveAdminsForLockscreenPoliciesLocked(userHandle, parent);
        int N = admins.size();
        for (int i = 0; i < N; i++) {
            ActiveAdmin admin = (ActiveAdmin) admins.get(i);
            if (admin.maximumFailedPasswordsForWipe != 0) {
                int userId = admin.getUserHandle().getIdentifier();
                if (count == 0 || count > admin.maximumFailedPasswordsForWipe || (count == admin.maximumFailedPasswordsForWipe && getUserInfo(userId).isPrimary())) {
                    count = admin.maximumFailedPasswordsForWipe;
                    strictestAdmin = admin;
                }
            }
        }
        return strictestAdmin;
    }

    private UserInfo getUserInfo(int userId) {
        long token = this.mInjector.binderClearCallingIdentity();
        try {
            UserInfo userInfo = this.mUserManager.getUserInfo(userId);
            return userInfo;
        } finally {
            this.mInjector.binderRestoreCallingIdentity(token);
        }
    }

    public boolean resetPassword(String passwordOrNull, int flags) throws RemoteException {
        int callingUid = this.mInjector.binderGetCallingUid();
        int userHandle = this.mInjector.userHandleGetCallingUserId();
        String password = passwordOrNull != null ? passwordOrNull : "";
        if (TextUtils.isEmpty(password)) {
            enforceNotManagedProfile(userHandle, "clear the active password");
        }
        synchronized (this) {
            boolean preN;
            ActiveAdmin admin = getActiveAdminWithPolicyForUidLocked(null, -1, callingUid);
            if (admin != null) {
                int targetSdk = getTargetSdk(admin.info.getPackageName(), userHandle);
                if (targetSdk >= 26) {
                    throw new SecurityException("resetPassword() is deprecated for DPC targeting O or later");
                } else if (targetSdk <= 23) {
                    preN = true;
                } else {
                    preN = false;
                }
            } else {
                preN = getTargetSdk(getActiveAdminForCallerLocked(null, 2).info.getPackageName(), userHandle) <= 23;
                if (TextUtils.isEmpty(password)) {
                    if (preN) {
                        Slog.e(LOG_TAG, "Cannot call with null password");
                        return false;
                    }
                    throw new SecurityException("Cannot call with null password");
                } else if (isLockScreenSecureUnchecked(userHandle)) {
                    if (preN) {
                        Slog.e(LOG_TAG, "Admin cannot change current password");
                        return false;
                    }
                    throw new SecurityException("Admin cannot change current password");
                }
            }
            if (!isManagedProfile(userHandle)) {
                for (UserInfo userInfo : this.mUserManager.getProfiles(userHandle)) {
                    if (userInfo.isManagedProfile()) {
                        if (preN) {
                            Slog.e(LOG_TAG, "Cannot reset password on user has managed profile");
                            return false;
                        }
                        throw new IllegalStateException("Cannot reset password on user has managed profile");
                    }
                }
            }
            if (this.mUserManager.isUserUnlocked(userHandle)) {
                return resetPasswordInternal(password, 0, null, flags, callingUid, userHandle);
            } else if (preN) {
                Slog.e(LOG_TAG, "Cannot reset password when user is locked");
                return false;
            } else {
                throw new IllegalStateException("Cannot reset password when user is locked");
            }
        }
    }

    /* JADX WARNING: Missing block: B:87:0x025b, code:
            r26 = getUserData(r39);
     */
    /* JADX WARNING: Missing block: B:88:0x0267, code:
            if (r26.mPasswordOwner < 0) goto L_0x027f;
     */
    /* JADX WARNING: Missing block: B:90:0x026f, code:
            if (r26.mPasswordOwner == r38) goto L_0x027f;
     */
    /* JADX WARNING: Missing block: B:91:0x0271, code:
            android.util.Slog.w(LOG_TAG, "resetPassword: already set by another uid and not entered by user");
     */
    /* JADX WARNING: Missing block: B:92:0x027b, code:
            return false;
     */
    /* JADX WARNING: Missing block: B:96:0x027f, code:
            r12 = isCallerDeviceOwner(r38);
     */
    /* JADX WARNING: Missing block: B:97:0x0289, code:
            if ((r37 & 2) == 0) goto L_0x02e7;
     */
    /* JADX WARNING: Missing block: B:98:0x028b, code:
            r13 = true;
     */
    /* JADX WARNING: Missing block: B:99:0x028c, code:
            if (r12 == false) goto L_0x0293;
     */
    /* JADX WARNING: Missing block: B:100:0x028e, code:
            if (r13 == false) goto L_0x0293;
     */
    /* JADX WARNING: Missing block: B:101:0x0290, code:
            setDoNotAskCredentialsOnBoot();
     */
    /* JADX WARNING: Missing block: B:102:0x0293, code:
            r14 = r32.mInjector.binderClearCallingIdentity();
     */
    /* JADX WARNING: Missing block: B:103:0x029b, code:
            if (r36 != null) goto L_0x02fd;
     */
    /* JADX WARNING: Missing block: B:106:0x02a1, code:
            if (android.text.TextUtils.isEmpty(r33) != false) goto L_0x02e9;
     */
    /* JADX WARNING: Missing block: B:107:0x02a3, code:
            r32.mLockPatternUtils.saveLockPassword(r33, null, r27, r39);
     */
    /* JADX WARNING: Missing block: B:108:0x02b1, code:
            r30 = true;
     */
    /* JADX WARNING: Missing block: B:110:0x02b5, code:
            if ((r37 & 1) == 0) goto L_0x0317;
     */
    /* JADX WARNING: Missing block: B:111:0x02b7, code:
            r29 = true;
     */
    /* JADX WARNING: Missing block: B:112:0x02b9, code:
            if (r29 == false) goto L_0x02c4;
     */
    /* JADX WARNING: Missing block: B:113:0x02bb, code:
            r32.mLockPatternUtils.requireStrongAuth(2, -1);
     */
    /* JADX WARNING: Missing block: B:114:0x02c4, code:
            monitor-enter(r32);
     */
    /* JADX WARNING: Missing block: B:115:0x02c5, code:
            if (r29 == false) goto L_0x031a;
     */
    /* JADX WARNING: Missing block: B:116:0x02c7, code:
            r25 = r38;
     */
    /* JADX WARNING: Missing block: B:119:0x02cf, code:
            if (r26.mPasswordOwner == r25) goto L_0x02de;
     */
    /* JADX WARNING: Missing block: B:120:0x02d1, code:
            r26.mPasswordOwner = r25;
            saveSettingsLocked(r39);
     */
    /* JADX WARNING: Missing block: B:122:?, code:
            monitor-exit(r32);
     */
    /* JADX WARNING: Missing block: B:123:0x02df, code:
            r32.mInjector.binderRestoreCallingIdentity(r14);
     */
    /* JADX WARNING: Missing block: B:124:0x02e6, code:
            return r30;
     */
    /* JADX WARNING: Missing block: B:125:0x02e7, code:
            r13 = false;
     */
    /* JADX WARNING: Missing block: B:127:?, code:
            r32.mLockPatternUtils.clearLock(null, r39);
     */
    /* JADX WARNING: Missing block: B:129:0x02f5, code:
            r32.mInjector.binderRestoreCallingIdentity(r14);
     */
    /* JADX WARNING: Missing block: B:132:?, code:
            r5 = r32.mLockPatternUtils;
     */
    /* JADX WARNING: Missing block: B:133:0x0305, code:
            if (android.text.TextUtils.isEmpty(r33) == false) goto L_0x0315;
     */
    /* JADX WARNING: Missing block: B:134:0x0307, code:
            r7 = -1;
     */
    /* JADX WARNING: Missing block: B:135:0x0308, code:
            r30 = r5.setLockCredentialWithToken(r33, r7, r34, r36, r39);
     */
    /* JADX WARNING: Missing block: B:136:0x0315, code:
            r7 = 2;
     */
    /* JADX WARNING: Missing block: B:137:0x0317, code:
            r29 = false;
     */
    /* JADX WARNING: Missing block: B:138:0x031a, code:
            r25 = -1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean resetPasswordInternal(String password, long tokenHandle, byte[] token, int flags, int callingUid, int userHandle) {
        synchronized (this) {
            if (mHwCustDevicePolicyManagerService != null && mHwCustDevicePolicyManagerService.isForbiddenSimplePwdFeatureEnable()) {
                boolean isNewPwdSimple = mHwCustDevicePolicyManagerService.isNewPwdSimpleCheck(password, this.mContext);
                if (!getAllowSimplePassword(null, userHandle) && isNewPwdSimple) {
                    Slog.e(LOG_TAG, "Cannot reset password when forbidden SimplePassword and current pwd is simple");
                    return false;
                }
            }
            int quality = getPasswordQuality(null, userHandle, false);
            if (quality == 524288) {
                quality = 0;
            }
            PasswordMetrics metrics = PasswordMetrics.computeForPassword(password);
            if (quality != 0) {
                int realQuality = metrics.quality;
                if (realQuality >= quality || quality == 393216) {
                    quality = Math.max(realQuality, quality);
                } else {
                    Slog.w(LOG_TAG, "resetPassword: password quality 0x" + Integer.toHexString(realQuality) + " does not meet required quality 0x" + Integer.toHexString(quality));
                    return false;
                }
            }
            int length = getPasswordMinimumLength(null, userHandle, false);
            if (password.length() < length) {
                Slog.w(LOG_TAG, "resetPassword: password length " + password.length() + " does not meet required length " + length);
                return false;
            } else if (quality == 393216) {
                int neededLetters = getPasswordMinimumLetters(null, userHandle, false);
                if (metrics.letters < neededLetters) {
                    Slog.w(LOG_TAG, "resetPassword: number of letters " + metrics.letters + " does not meet required number of letters " + neededLetters);
                    return false;
                }
                int neededNumeric = getPasswordMinimumNumeric(null, userHandle, false);
                if (metrics.numeric < neededNumeric) {
                    Slog.w(LOG_TAG, "resetPassword: number of numerical digits " + metrics.numeric + " does not meet required number of numerical digits " + neededNumeric);
                    return false;
                }
                int neededLowerCase = getPasswordMinimumLowerCase(null, userHandle, false);
                if (metrics.lowerCase < neededLowerCase) {
                    Slog.w(LOG_TAG, "resetPassword: number of lowercase letters " + metrics.lowerCase + " does not meet required number of lowercase letters " + neededLowerCase);
                    return false;
                }
                int neededUpperCase = getPasswordMinimumUpperCase(null, userHandle, false);
                if (metrics.upperCase < neededUpperCase) {
                    Slog.w(LOG_TAG, "resetPassword: number of uppercase letters " + metrics.upperCase + " does not meet required number of uppercase letters " + neededUpperCase);
                    return false;
                }
                int neededSymbols = getPasswordMinimumSymbols(null, userHandle, false);
                if (metrics.symbols < neededSymbols) {
                    Slog.w(LOG_TAG, "resetPassword: number of special symbols " + metrics.symbols + " does not meet required number of special symbols " + neededSymbols);
                    return false;
                }
                int neededNonLetter = getPasswordMinimumNonLetter(null, userHandle, false);
                if (metrics.nonLetter < neededNonLetter) {
                    Slog.w(LOG_TAG, "resetPassword: number of non-letter characters " + metrics.nonLetter + " does not meet required number of non-letter characters " + neededNonLetter);
                    return false;
                }
            }
        }
    }

    private boolean isLockScreenSecureUnchecked(int userId) {
        long ident = this.mInjector.binderClearCallingIdentity();
        try {
            boolean isSecure = this.mLockPatternUtils.isSecure(userId);
            return isSecure;
        } finally {
            this.mInjector.binderRestoreCallingIdentity(ident);
        }
    }

    private void setDoNotAskCredentialsOnBoot() {
        synchronized (this) {
            DevicePolicyData policyData = getUserData(0);
            if (!policyData.doNotAskCredentialsOnBoot) {
                policyData.doNotAskCredentialsOnBoot = true;
                saveSettingsLocked(0);
            }
        }
    }

    public boolean getDoNotAskCredentialsOnBoot() {
        boolean z;
        this.mContext.enforceCallingOrSelfPermission("android.permission.QUERY_DO_NOT_ASK_CREDENTIALS_ON_BOOT", null);
        synchronized (this) {
            z = getUserData(0).doNotAskCredentialsOnBoot;
        }
        return z;
    }

    public void setMaximumTimeToLock(ComponentName who, long timeMs, boolean parent) {
        if (this.mHasFeature) {
            Preconditions.checkNotNull(who, "ComponentName is null");
            int userHandle = this.mInjector.userHandleGetCallingUserId();
            synchronized (this) {
                ActiveAdmin ap = getActiveAdminForCallerLocked(who, 3, parent);
                if (ap.maximumTimeToUnlock != timeMs) {
                    ap.maximumTimeToUnlock = timeMs;
                    saveSettingsLocked(userHandle);
                    updateMaximumTimeToLockLocked(userHandle);
                }
            }
        }
    }

    void updateMaximumTimeToLockLocked(int userHandle) {
        DevicePolicyData policy;
        long timeMs = JobStatus.NO_LATEST_RUNTIME;
        for (int profileId : this.mUserManager.getProfileIdsWithDisabled(userHandle)) {
            policy = getUserDataUnchecked(profileId);
            int N = policy.mAdminList.size();
            for (int i = 0; i < N; i++) {
                ActiveAdmin admin = (ActiveAdmin) policy.mAdminList.get(i);
                if (admin.maximumTimeToUnlock > 0 && timeMs > admin.maximumTimeToUnlock) {
                    timeMs = admin.maximumTimeToUnlock;
                }
                if (admin.hasParentActiveAdmin()) {
                    ActiveAdmin parentAdmin = admin.getParentActiveAdmin();
                    if (parentAdmin.maximumTimeToUnlock > 0 && timeMs > parentAdmin.maximumTimeToUnlock) {
                        timeMs = parentAdmin.maximumTimeToUnlock;
                    }
                }
            }
        }
        policy = getUserDataUnchecked(getProfileParentId(userHandle));
        if (policy.mLastMaximumTimeToLock != timeMs) {
            policy.mLastMaximumTimeToLock = timeMs;
            long ident = this.mInjector.binderClearCallingIdentity();
            try {
                if (policy.mLastMaximumTimeToLock != JobStatus.NO_LATEST_RUNTIME) {
                    this.mInjector.settingsGlobalPutInt("stay_on_while_plugged_in", 0);
                }
                this.mInjector.getPowerManagerInternal().setMaximumScreenOffTimeoutFromDeviceAdmin((int) Math.min(policy.mLastMaximumTimeToLock, 2147483647L));
            } finally {
                this.mInjector.binderRestoreCallingIdentity(ident);
            }
        }
    }

    /* JADX WARNING: Missing block: B:11:0x0016, code:
            return r2;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public long getMaximumTimeToLock(ComponentName who, int userHandle, boolean parent) {
        long j = 0;
        if (!this.mHasFeature) {
            return 0;
        }
        enforceFullCrossUsersPermission(userHandle);
        synchronized (this) {
            if (who != null) {
                ActiveAdmin admin = getActiveAdminUncheckedLocked(who, userHandle, parent);
                if (admin != null) {
                    j = admin.maximumTimeToUnlock;
                }
            } else {
                j = getMaximumTimeToLockPolicyFromAdmins(getActiveAdminsForLockscreenPoliciesLocked(userHandle, parent));
                return j;
            }
        }
    }

    public long getMaximumTimeToLockForUserAndProfiles(int userHandle) {
        if (!this.mHasFeature) {
            return 0;
        }
        long maximumTimeToLockPolicyFromAdmins;
        enforceFullCrossUsersPermission(userHandle);
        synchronized (this) {
            ArrayList<ActiveAdmin> admins = new ArrayList();
            for (UserInfo userInfo : this.mUserManager.getProfiles(userHandle)) {
                DevicePolicyData policy = getUserData(userInfo.id);
                admins.addAll(policy.mAdminList);
                if (userInfo.isManagedProfile()) {
                    for (ActiveAdmin admin : policy.mAdminList) {
                        if (admin.hasParentActiveAdmin()) {
                            admins.add(admin.getParentActiveAdmin());
                        }
                    }
                }
            }
            maximumTimeToLockPolicyFromAdmins = getMaximumTimeToLockPolicyFromAdmins(admins);
        }
        return maximumTimeToLockPolicyFromAdmins;
    }

    private long getMaximumTimeToLockPolicyFromAdmins(List<ActiveAdmin> admins) {
        long time = 0;
        int N = admins.size();
        for (int i = 0; i < N; i++) {
            ActiveAdmin admin = (ActiveAdmin) admins.get(i);
            if (time == 0) {
                time = admin.maximumTimeToUnlock;
            } else if (admin.maximumTimeToUnlock != 0 && time > admin.maximumTimeToUnlock) {
                time = admin.maximumTimeToUnlock;
            }
        }
        return time;
    }

    public void setRequiredStrongAuthTimeout(ComponentName who, long timeoutMs, boolean parent) {
        if (this.mHasFeature) {
            Preconditions.checkNotNull(who, "ComponentName is null");
            Preconditions.checkArgument(timeoutMs >= 0, "Timeout must not be a negative number.");
            long minimumStrongAuthTimeout = getMinimumStrongAuthTimeoutMs();
            if (timeoutMs != 0 && timeoutMs < minimumStrongAuthTimeout) {
                timeoutMs = minimumStrongAuthTimeout;
            }
            if (timeoutMs > 259200000) {
                timeoutMs = 259200000;
            }
            int userHandle = this.mInjector.userHandleGetCallingUserId();
            synchronized (this) {
                ActiveAdmin ap = getActiveAdminForCallerLocked(who, -1, parent);
                if (ap.strongAuthUnlockTimeout != timeoutMs) {
                    ap.strongAuthUnlockTimeout = timeoutMs;
                    saveSettingsLocked(userHandle);
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:12:0x0019, code:
            return r8;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public long getRequiredStrongAuthTimeout(ComponentName who, int userId, boolean parent) {
        long j = 0;
        if (!this.mHasFeature) {
            return 259200000;
        }
        enforceFullCrossUsersPermission(userId);
        synchronized (this) {
            if (who != null) {
                ActiveAdmin admin = getActiveAdminUncheckedLocked(who, userId, parent);
                if (admin != null) {
                    j = admin.strongAuthUnlockTimeout;
                }
            } else {
                List<ActiveAdmin> admins = getActiveAdminsForLockscreenPoliciesLocked(userId, parent);
                long strongAuthUnlockTimeout = 259200000;
                for (int i = 0; i < admins.size(); i++) {
                    long timeout = ((ActiveAdmin) admins.get(i)).strongAuthUnlockTimeout;
                    if (timeout != 0) {
                        strongAuthUnlockTimeout = Math.min(timeout, strongAuthUnlockTimeout);
                    }
                }
                j = Math.max(strongAuthUnlockTimeout, getMinimumStrongAuthTimeoutMs());
                return j;
            }
        }
    }

    private long getMinimumStrongAuthTimeoutMs() {
        if (this.mInjector.isBuildDebuggable()) {
            return Math.min(this.mInjector.systemPropertiesGetLong("persist.sys.min_str_auth_timeo", MINIMUM_STRONG_AUTH_TIMEOUT_MS), MINIMUM_STRONG_AUTH_TIMEOUT_MS);
        }
        return MINIMUM_STRONG_AUTH_TIMEOUT_MS;
    }

    public void lockNow(int flags, boolean parent) {
        if (this.mHasFeature) {
            int callingUserId = this.mInjector.userHandleGetCallingUserId();
            synchronized (this) {
                ActiveAdmin admin = getActiveAdminForCallerLocked(null, 3, parent);
                long ident = this.mInjector.binderClearCallingIdentity();
                if ((flags & 1) != 0) {
                    try {
                        enforceManagedProfile(callingUserId, "set FLAG_EVICT_CREDENTIAL_ENCRYPTION_KEY");
                        if (!isProfileOwner(admin.info.getComponent(), callingUserId)) {
                            throw new SecurityException("Only profile owner admins can set FLAG_EVICT_CREDENTIAL_ENCRYPTION_KEY");
                        } else if (parent) {
                            throw new IllegalArgumentException("Cannot set FLAG_EVICT_CREDENTIAL_ENCRYPTION_KEY for the parent");
                        } else if (this.mInjector.storageManagerIsFileBasedEncryptionEnabled()) {
                            this.mUserManager.evictCredentialEncryptionKey(callingUserId);
                        } else {
                            throw new UnsupportedOperationException("FLAG_EVICT_CREDENTIAL_ENCRYPTION_KEY only applies to FBE devices");
                        }
                    } catch (RemoteException e) {
                        this.mInjector.binderRestoreCallingIdentity(ident);
                    } catch (Throwable th) {
                        this.mInjector.binderRestoreCallingIdentity(ident);
                    }
                }
                int userToLock = (parent || (isSeparateProfileChallengeEnabled(callingUserId) ^ 1) != 0) ? -1 : callingUserId;
                this.mLockPatternUtils.requireStrongAuth(2, userToLock);
                if (userToLock == -1) {
                    this.mInjector.powerManagerGoToSleep(SystemClock.uptimeMillis(), 1, 0);
                    this.mInjector.getIWindowManager().lockNow(null);
                } else {
                    this.mInjector.getTrustManager().setDeviceLockedForUser(userToLock, true);
                }
                this.mInjector.binderRestoreCallingIdentity(ident);
            }
            return;
        }
        return;
    }

    public void enforceCanManageCaCerts(ComponentName who, String callerPackage) {
        if (who != null) {
            enforceProfileOrDeviceOwner(who);
        } else if (!isCallerDelegate(callerPackage, "delegation-cert-install")) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_CA_CERTIFICATES", null);
        }
    }

    private void enforceProfileOrDeviceOwner(ComponentName who) {
        synchronized (this) {
            getActiveAdminForCallerLocked(who, -1);
        }
    }

    public boolean approveCaCert(String alias, int userId, boolean approval) {
        enforceManageUsers();
        synchronized (this) {
            Set<String> certs = getUserData(userId).mAcceptedCaCertificates;
            if (approval ? certs.add(alias) : certs.remove(alias)) {
                saveSettingsLocked(userId);
                this.mCertificateMonitor.onCertificateApprovalsChanged(userId);
                return true;
            }
            return false;
        }
    }

    public boolean isCaCertApproved(String alias, int userId) {
        boolean contains;
        enforceManageUsers();
        synchronized (this) {
            contains = getUserData(userId).mAcceptedCaCertificates.contains(alias);
        }
        return contains;
    }

    private void removeCaApprovalsIfNeeded(int userId) {
        for (UserInfo userInfo : this.mUserManager.getProfiles(userId)) {
            boolean isSecure = this.mLockPatternUtils.isSecure(userInfo.id);
            if (userInfo.isManagedProfile()) {
                isSecure |= this.mLockPatternUtils.isSecure(getProfileParentId(userInfo.id));
            }
            if (!isSecure) {
                synchronized (this) {
                    getUserData(userInfo.id).mAcceptedCaCertificates.clear();
                    saveSettingsLocked(userInfo.id);
                }
                this.mCertificateMonitor.onCertificateApprovalsChanged(userId);
            }
        }
    }

    public boolean installCaCert(ComponentName admin, String callerPackage, byte[] certBuffer) throws RemoteException {
        if (!this.mHasFeature) {
            return false;
        }
        enforceCanManageCaCerts(admin, callerPackage);
        UserHandle userHandle = this.mInjector.binderGetCallingUserHandle();
        long id = this.mInjector.binderClearCallingIdentity();
        try {
            String alias = this.mCertificateMonitor.installCaCert(userHandle, certBuffer);
            if (alias == null) {
                Log.w(LOG_TAG, "Problem installing cert");
                return false;
            }
            this.mInjector.binderRestoreCallingIdentity(id);
            synchronized (this) {
                getUserData(userHandle.getIdentifier()).mOwnerInstalledCaCerts.add(alias);
                saveSettingsLocked(userHandle.getIdentifier());
            }
            return true;
        } finally {
            this.mInjector.binderRestoreCallingIdentity(id);
        }
    }

    public void uninstallCaCerts(ComponentName admin, String callerPackage, String[] aliases) {
        if (this.mHasFeature) {
            enforceCanManageCaCerts(admin, callerPackage);
            int userId = this.mInjector.userHandleGetCallingUserId();
            long id = this.mInjector.binderClearCallingIdentity();
            try {
                this.mCertificateMonitor.uninstallCaCerts(UserHandle.of(userId), aliases);
                synchronized (this) {
                    if (getUserData(userId).mOwnerInstalledCaCerts.removeAll(Arrays.asList(aliases))) {
                        saveSettingsLocked(userId);
                    }
                }
            } finally {
                this.mInjector.binderRestoreCallingIdentity(id);
            }
        }
    }

    public boolean installKeyPair(ComponentName who, String callerPackage, byte[] privKey, byte[] cert, byte[] chain, String alias, boolean requestAccess) {
        enforceCanManageScope(who, callerPackage, -1, "delegation-cert-install");
        int callingUid = this.mInjector.binderGetCallingUid();
        long id = this.mInjector.binderClearCallingIdentity();
        try {
            KeyChainConnection keyChainConnection = KeyChain.bindAsUser(this.mContext, UserHandle.getUserHandleForUid(callingUid));
            try {
                IKeyChainService keyChain = keyChainConnection.getService();
                if (keyChain.installKeyPair(privKey, cert, chain, alias)) {
                    if (requestAccess) {
                        keyChain.setGrant(callingUid, alias, true);
                    }
                    keyChainConnection.close();
                    this.mInjector.binderRestoreCallingIdentity(id);
                    return true;
                }
                this.mInjector.binderRestoreCallingIdentity(id);
                return false;
            } catch (RemoteException e) {
                Log.e(LOG_TAG, "Installing certificate", e);
                this.mInjector.binderRestoreCallingIdentity(id);
                return false;
            } finally {
                keyChainConnection.close();
            }
        } catch (InterruptedException e2) {
            try {
                Log.w(LOG_TAG, "Interrupted while installing certificate", e2);
                Thread.currentThread().interrupt();
            } finally {
                this.mInjector.binderRestoreCallingIdentity(id);
            }
        }
    }

    public boolean removeKeyPair(ComponentName who, String callerPackage, String alias) {
        enforceCanManageScope(who, callerPackage, -1, "delegation-cert-install");
        UserHandle userHandle = new UserHandle(UserHandle.getCallingUserId());
        long id = Binder.clearCallingIdentity();
        try {
            KeyChainConnection keyChainConnection = KeyChain.bindAsUser(this.mContext, userHandle);
            boolean removeKeyPair;
            try {
                removeKeyPair = keyChainConnection.getService().removeKeyPair(alias);
                Binder.restoreCallingIdentity(id);
                return removeKeyPair;
            } catch (RemoteException e) {
                removeKeyPair = LOG_TAG;
                Log.e(removeKeyPair, "Removing keypair", e);
                Binder.restoreCallingIdentity(id);
                return false;
            } finally {
                keyChainConnection.close();
            }
        } catch (InterruptedException e2) {
            try {
                Log.w(LOG_TAG, "Interrupted while removing keypair", e2);
                Thread.currentThread().interrupt();
            } finally {
                Binder.restoreCallingIdentity(id);
            }
        }
    }

    public void choosePrivateKeyAlias(int uid, Uri uri, String alias, IBinder response) {
        if (isCallerWithSystemUid()) {
            UserHandle caller = this.mInjector.binderGetCallingUserHandle();
            ComponentName aliasChooser = getProfileOwner(caller.getIdentifier());
            if (aliasChooser == null && caller.isSystem()) {
                ActiveAdmin deviceOwnerAdmin = getDeviceOwnerAdminLocked();
                if (deviceOwnerAdmin != null) {
                    aliasChooser = deviceOwnerAdmin.info.getComponent();
                }
            }
            if (aliasChooser == null) {
                sendPrivateKeyAliasResponse(null, response);
                return;
            }
            Intent intent = new Intent("android.app.action.CHOOSE_PRIVATE_KEY_ALIAS");
            intent.setComponent(aliasChooser);
            intent.putExtra("android.app.extra.CHOOSE_PRIVATE_KEY_SENDER_UID", uid);
            intent.putExtra("android.app.extra.CHOOSE_PRIVATE_KEY_URI", uri);
            intent.putExtra("android.app.extra.CHOOSE_PRIVATE_KEY_ALIAS", alias);
            intent.putExtra("android.app.extra.CHOOSE_PRIVATE_KEY_RESPONSE", response);
            intent.addFlags(268435456);
            long id = this.mInjector.binderClearCallingIdentity();
            try {
                final IBinder iBinder = response;
                this.mContext.sendOrderedBroadcastAsUser(intent, caller, null, new BroadcastReceiver() {
                    public void onReceive(Context context, Intent intent) {
                        DevicePolicyManagerService.this.sendPrivateKeyAliasResponse(getResultData(), iBinder);
                    }
                }, null, -1, null, null);
            } finally {
                this.mInjector.binderRestoreCallingIdentity(id);
            }
        }
    }

    private void sendPrivateKeyAliasResponse(String alias, IBinder responseBinder) {
        try {
            IKeyChainAliasCallback.Stub.asInterface(responseBinder).alias(alias);
        } catch (Exception e) {
            Log.e(LOG_TAG, "error while responding to callback", e);
        }
    }

    private static boolean shouldCheckIfDelegatePackageIsInstalled(String delegatePackage, int targetSdk, List<String> scopes) {
        if (targetSdk >= 24) {
            return true;
        }
        return ((scopes.size() == 1 && ((String) scopes.get(0)).equals("delegation-cert-install")) || scopes.isEmpty()) ? false : true;
    }

    public void setDelegatedScopes(ComponentName who, String delegatePackage, List<String> scopes) throws SecurityException {
        Preconditions.checkNotNull(who, "ComponentName is null");
        Preconditions.checkStringNotEmpty(delegatePackage, "Delegate package is null or empty");
        Preconditions.checkCollectionElementsNotNull(scopes, "Scopes");
        List<String> scopes2 = new ArrayList(new ArraySet(scopes));
        if (scopes2.retainAll(Arrays.asList(DELEGATIONS))) {
            throw new IllegalArgumentException("Unexpected delegation scopes");
        }
        int userId = this.mInjector.userHandleGetCallingUserId();
        synchronized (this) {
            getActiveAdminForCallerLocked(who, -1);
            if (!shouldCheckIfDelegatePackageIsInstalled(delegatePackage, getTargetSdk(who.getPackageName(), userId), scopes2) || isPackageInstalledForUser(delegatePackage, userId)) {
                DevicePolicyData policy = getUserData(userId);
                if (scopes2.isEmpty()) {
                    policy.mDelegationMap.remove(delegatePackage);
                } else {
                    policy.mDelegationMap.put(delegatePackage, new ArrayList(scopes2));
                }
                Intent intent = new Intent("android.app.action.APPLICATION_DELEGATION_SCOPES_CHANGED");
                intent.addFlags(1073741824);
                intent.setPackage(delegatePackage);
                intent.putStringArrayListExtra("android.app.extra.DELEGATION_SCOPES", (ArrayList) scopes2);
                this.mContext.sendBroadcastAsUser(intent, UserHandle.of(userId));
                saveSettingsLocked(userId);
            } else {
                throw new IllegalArgumentException("Package " + delegatePackage + " is not installed on the current user");
            }
        }
    }

    public List<String> getDelegatedScopes(ComponentName who, String delegatePackage) throws SecurityException {
        List<String> scopes;
        Preconditions.checkNotNull(delegatePackage, "Delegate package is null");
        int callingUid = this.mInjector.binderGetCallingUid();
        int userId = UserHandle.getUserId(callingUid);
        synchronized (this) {
            if (who != null) {
                getActiveAdminForCallerLocked(who, -1);
            } else {
                int uid = 0;
                try {
                    uid = this.mInjector.getPackageManager().getPackageUidAsUser(delegatePackage, userId);
                } catch (NameNotFoundException e) {
                }
                if (uid != callingUid) {
                    throw new SecurityException("Caller with uid " + callingUid + " is not " + delegatePackage);
                }
            }
            scopes = (List) getUserData(userId).mDelegationMap.get(delegatePackage);
            if (scopes == null) {
                scopes = Collections.EMPTY_LIST;
            }
        }
        return scopes;
    }

    public List<String> getDelegatePackages(ComponentName who, String scope) throws SecurityException {
        Preconditions.checkNotNull(who, "ComponentName is null");
        Preconditions.checkNotNull(scope, "Scope is null");
        if (Arrays.asList(DELEGATIONS).contains(scope)) {
            List<String> delegatePackagesWithScope;
            int userId = this.mInjector.userHandleGetCallingUserId();
            synchronized (this) {
                getActiveAdminForCallerLocked(who, -1);
                DevicePolicyData policy = getUserData(userId);
                delegatePackagesWithScope = new ArrayList();
                for (int i = 0; i < policy.mDelegationMap.size(); i++) {
                    if (((List) policy.mDelegationMap.valueAt(i)).contains(scope)) {
                        delegatePackagesWithScope.add((String) policy.mDelegationMap.keyAt(i));
                    }
                }
            }
            return delegatePackagesWithScope;
        }
        throw new IllegalArgumentException("Unexpected delegation scope: " + scope);
    }

    /* JADX WARNING: Missing block: B:16:0x005a, code:
            return r6;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean isCallerDelegate(String callerPackage, String scope) {
        boolean z = false;
        Preconditions.checkNotNull(callerPackage, "callerPackage is null");
        if (Arrays.asList(DELEGATIONS).contains(scope)) {
            int callingUid = this.mInjector.binderGetCallingUid();
            int userId = UserHandle.getUserId(callingUid);
            synchronized (this) {
                List<String> scopes = (List) getUserData(userId).mDelegationMap.get(callerPackage);
                if (scopes != null && scopes.contains(scope)) {
                    try {
                        if (this.mInjector.getPackageManager().getPackageUidAsUser(callerPackage, userId) == callingUid) {
                            z = true;
                        }
                    } catch (NameNotFoundException e) {
                    }
                }
            }
        } else {
            throw new IllegalArgumentException("Unexpected delegation scope: " + scope);
        }
        return false;
    }

    private void enforceCanManageScope(ComponentName who, String callerPackage, int reqPolicy, String scope) {
        if (who != null) {
            synchronized (this) {
                getActiveAdminForCallerLocked(who, reqPolicy);
            }
        } else if (!isCallerDelegate(callerPackage, scope)) {
            throw new SecurityException("Caller with uid " + this.mInjector.binderGetCallingUid() + " is not a delegate of scope " + scope + ".");
        }
    }

    private void setDelegatedScopePreO(ComponentName who, String delegatePackage, String scope) {
        Preconditions.checkNotNull(who, "ComponentName is null");
        int userId = this.mInjector.userHandleGetCallingUserId();
        synchronized (this) {
            getActiveAdminForCallerLocked(who, -1);
            DevicePolicyData policy = getUserData(userId);
            if (delegatePackage != null) {
                List<String> scopes = (List) policy.mDelegationMap.get(delegatePackage);
                if (scopes == null) {
                    scopes = new ArrayList();
                }
                if (!scopes.contains(scope)) {
                    scopes.add(scope);
                    setDelegatedScopes(who, delegatePackage, scopes);
                }
            }
            for (int i = 0; i < policy.mDelegationMap.size(); i++) {
                String currentPackage = (String) policy.mDelegationMap.keyAt(i);
                List<String> currentScopes = (List) policy.mDelegationMap.valueAt(i);
                if (!currentPackage.equals(delegatePackage) && currentScopes.contains(scope)) {
                    List<String> newScopes = new ArrayList(currentScopes);
                    newScopes.remove(scope);
                    setDelegatedScopes(who, currentPackage, newScopes);
                }
            }
        }
    }

    public void setCertInstallerPackage(ComponentName who, String installerPackage) throws SecurityException {
        setDelegatedScopePreO(who, installerPackage, "delegation-cert-install");
    }

    public String getCertInstallerPackage(ComponentName who) throws SecurityException {
        List<String> delegatePackages = getDelegatePackages(who, "delegation-cert-install");
        return delegatePackages.size() > 0 ? (String) delegatePackages.get(0) : null;
    }

    public boolean setAlwaysOnVpnPackage(ComponentName admin, String vpnPackage, boolean lockdown) throws SecurityException {
        enforceProfileOrDeviceOwner(admin);
        int userId = this.mInjector.userHandleGetCallingUserId();
        long token = this.mInjector.binderClearCallingIdentity();
        if (vpnPackage != null) {
            try {
                if ((isPackageInstalledForUser(vpnPackage, userId) ^ 1) != 0) {
                    return false;
                }
            } finally {
                this.mInjector.binderRestoreCallingIdentity(token);
            }
        }
        if (((ConnectivityManager) this.mContext.getSystemService("connectivity")).setAlwaysOnVpnPackageForUser(userId, vpnPackage, lockdown)) {
            this.mInjector.binderRestoreCallingIdentity(token);
            return true;
        }
        throw new UnsupportedOperationException();
    }

    public String getAlwaysOnVpnPackage(ComponentName admin) throws SecurityException {
        enforceProfileOrDeviceOwner(admin);
        int userId = this.mInjector.userHandleGetCallingUserId();
        long token = this.mInjector.binderClearCallingIdentity();
        try {
            String alwaysOnVpnPackageForUser = ((ConnectivityManager) this.mContext.getSystemService("connectivity")).getAlwaysOnVpnPackageForUser(userId);
            return alwaysOnVpnPackageForUser;
        } finally {
            this.mInjector.binderRestoreCallingIdentity(token);
        }
    }

    private void forceWipeDeviceNoLock(boolean wipeExtRequested, String reason) {
        wtfIfInLock();
        if (wipeExtRequested) {
            ((StorageManager) this.mContext.getSystemService("storage")).wipeAdoptableDisks();
        }
        clearWipeDataFactoryLowlevel(reason);
    }

    private void forceWipeUser(int userId) {
        try {
            IActivityManager am = this.mInjector.getIActivityManager();
            if (am.getCurrentUser().id == userId) {
                am.switchUser(0);
            }
            if (!this.mUserManagerInternal.removeUserEvenWhenDisallowed(userId)) {
                Slog.w(LOG_TAG, "Couldn't remove user " + userId);
            } else if (isManagedProfile(userId)) {
                sendWipeProfileNotification();
            }
        } catch (RemoteException e) {
        }
    }

    public void wipeData(int flags) {
        if (this.mHasFeature) {
            ActiveAdmin admin;
            enforceFullCrossUsersPermission(this.mInjector.userHandleGetCallingUserId());
            synchronized (this) {
                admin = getActiveAdminForCallerLocked(null, 4);
            }
            wipeDataNoLock(admin.info.getComponent(), flags, "DevicePolicyManager.wipeData() from " + admin.info.getComponent().flattenToShortString(), admin.getUserHandle().getIdentifier());
        }
    }

    private void wipeDataNoLock(ComponentName admin, int flags, String reason, int userId) {
        String restriction;
        boolean z = false;
        wtfIfInLock();
        long ident = this.mInjector.binderClearCallingIdentity();
        if (userId == 0) {
            try {
                restriction = "no_factory_reset";
            } catch (Throwable th) {
                this.mInjector.binderRestoreCallingIdentity(ident);
            }
        } else if (isManagedProfile(userId)) {
            restriction = "no_remove_managed_profile";
        } else {
            restriction = "no_remove_user";
        }
        if (isAdminAffectedByRestriction(admin, restriction, userId)) {
            throw new SecurityException("Cannot wipe data. " + restriction + " restriction is set for user " + userId);
        }
        monitorFactoryReset(admin.flattenToShortString(), reason);
        if ((flags & 2) != 0) {
            if (isDeviceOwner(admin, userId)) {
                PersistentDataBlockManager manager = (PersistentDataBlockManager) this.mContext.getSystemService("persistent_data_block");
                if (manager != null) {
                    manager.wipe();
                }
            } else {
                throw new SecurityException("Only device owner admins can set WIPE_RESET_PROTECTION_DATA");
            }
        }
        if (userId == 0) {
            if ((flags & 1) != 0) {
                z = true;
            }
            forceWipeDeviceNoLock(z, reason);
        } else {
            forceWipeUser(userId);
        }
        this.mInjector.binderRestoreCallingIdentity(ident);
    }

    private void sendWipeProfileNotification() {
        String contentText = this.mContext.getString(17041272);
        this.mInjector.getNotificationManager().notify(NetworkAgentInfo.EVENT_NETWORK_LINGER_COMPLETE, new Builder(this.mContext, SystemNotificationChannels.DEVICE_ADMIN).setSmallIcon(17301642).setContentTitle(this.mContext.getString(17041270)).setContentText(contentText).setColor(this.mContext.getColor(17170769)).setStyle(new BigTextStyle().bigText(contentText)).build());
    }

    private void clearWipeProfileNotification() {
        this.mInjector.getNotificationManager().cancel(NetworkAgentInfo.EVENT_NETWORK_LINGER_COMPLETE);
    }

    public void getRemoveWarning(ComponentName comp, final RemoteCallback result, int userHandle) {
        if (this.mHasFeature) {
            enforceFullCrossUsersPermission(userHandle);
            this.mContext.enforceCallingOrSelfPermission("android.permission.BIND_DEVICE_ADMIN", null);
            synchronized (this) {
                ActiveAdmin admin = getActiveAdminUncheckedLocked(comp, userHandle);
                if (admin == null) {
                    result.sendResult(null);
                    return;
                }
                Intent intent = new Intent("android.app.action.DEVICE_ADMIN_DISABLE_REQUESTED");
                intent.setFlags(268435456);
                intent.setComponent(admin.info.getComponent());
                this.mContext.sendOrderedBroadcastAsUser(intent, new UserHandle(userHandle), null, new BroadcastReceiver() {
                    public void onReceive(Context context, Intent intent) {
                        result.sendResult(getResultExtras(false));
                    }
                }, null, -1, null, null);
            }
        }
    }

    public void setActivePasswordState(PasswordMetrics metrics, int userHandle) {
        if (this.mHasFeature) {
            enforceFullCrossUsersPermission(userHandle);
            this.mContext.enforceCallingOrSelfPermission("android.permission.BIND_DEVICE_ADMIN", null);
            if (isManagedProfile(userHandle) && (isSeparateProfileChallengeEnabled(userHandle) ^ 1) != 0) {
                metrics = new PasswordMetrics();
            }
            validateQualityConstant(metrics.quality);
            DevicePolicyData policy = getUserData(userHandle);
            synchronized (this) {
                policy.mActivePasswordMetrics = metrics;
            }
        }
    }

    public void reportPasswordChanged(int userId) {
        if (this.mHasFeature) {
            enforceFullCrossUsersPermission(userId);
            if (!isSeparateProfileChallengeEnabled(userId)) {
                enforceNotManagedProfile(userId, "set the active password");
            }
            this.mContext.enforceCallingOrSelfPermission("android.permission.BIND_DEVICE_ADMIN", null);
            DevicePolicyData policy = getUserData(userId);
            long ident = this.mInjector.binderClearCallingIdentity();
            try {
                synchronized (this) {
                    policy.mFailedPasswordAttempts = 0;
                    saveSettingsLocked(userId);
                    updatePasswordExpirationsLocked(userId);
                    setExpirationAlarmCheckLocked(this.mContext, userId, false);
                    sendAdminCommandForLockscreenPoliciesLocked("android.app.action.ACTION_PASSWORD_CHANGED", 0, userId);
                }
                removeCaApprovalsIfNeeded(userId);
            } finally {
                this.mInjector.binderRestoreCallingIdentity(ident);
            }
        }
    }

    private void updatePasswordExpirationsLocked(int userHandle) {
        ArraySet<Integer> affectedUserIds = new ArraySet();
        List<ActiveAdmin> admins = getActiveAdminsForLockscreenPoliciesLocked(userHandle, false);
        int N = admins.size();
        for (int i = 0; i < N; i++) {
            ActiveAdmin admin = (ActiveAdmin) admins.get(i);
            if (admin.info.usesPolicy(6)) {
                affectedUserIds.add(Integer.valueOf(admin.getUserHandle().getIdentifier()));
                long timeout = admin.passwordExpirationTimeout;
                admin.passwordExpirationDate = timeout > 0 ? timeout + System.currentTimeMillis() : 0;
            }
        }
        for (Integer intValue : affectedUserIds) {
            saveSettingsLocked(intValue.intValue());
        }
    }

    public void reportFailedPasswordAttempt(int userHandle) {
        enforceFullCrossUsersPermission(userHandle);
        if (!isSeparateProfileChallengeEnabled(userHandle)) {
            enforceNotManagedProfile(userHandle, "report failed password attempt if separate profile challenge is not in place");
        }
        this.mContext.enforceCallingOrSelfPermission("android.permission.BIND_DEVICE_ADMIN", null);
        boolean wipeData = false;
        int failedAttempts = 0;
        ActiveAdmin strictestAdmin = null;
        long ident = this.mInjector.binderClearCallingIdentity();
        try {
            synchronized (this) {
                DevicePolicyData policy = getUserData(userHandle);
                policy.mFailedPasswordAttempts++;
                saveSettingsLocked(userHandle);
                failedAttempts = policy.mFailedPasswordAttempts;
                if (this.mHasFeature) {
                    strictestAdmin = getAdminWithMinimumFailedPasswordsForWipeLocked(userHandle, false);
                    int max = strictestAdmin != null ? strictestAdmin.maximumFailedPasswordsForWipe : 0;
                    if (max > 0 && policy.mFailedPasswordAttempts >= max) {
                        wipeData = true;
                    }
                    sendAdminCommandForLockscreenPoliciesLocked("android.app.action.ACTION_PASSWORD_FAILED", 1, userHandle);
                }
            }
            HwCustDevicePolicyManagerService mHwCustRecoverySystem = (HwCustDevicePolicyManagerService) HwCustUtils.createObj(HwCustDevicePolicyManagerService.class, new Object[0]);
            if (mHwCustRecoverySystem != null && mHwCustRecoverySystem.isAttEraseDataOn(this.mContext)) {
                mHwCustRecoverySystem.isStartEraseAllDataForAtt(this.mContext, failedAttempts);
            }
            if (wipeData && strictestAdmin != null) {
                int userId = strictestAdmin.getUserHandle().getIdentifier();
                Slog.i(LOG_TAG, "Max failed password attempts policy reached for admin: " + strictestAdmin.info.getComponent().flattenToShortString() + ". Calling wipeData for user " + userId);
                String reason = "WipeData for max failed PWD reached:" + strictestAdmin.maximumFailedPasswordsForWipe;
                monitorFactoryReset(strictestAdmin.info.getComponent().flattenToShortString(), reason);
                boolean isCustWipeData = false;
                if (userHandle == 0 && mHwCustRecoverySystem != null) {
                    try {
                        if (mHwCustRecoverySystem.eraseStorageForEAS(this.mContext) || mHwCustRecoverySystem.wipeDataAndReset(this.mContext)) {
                            isCustWipeData = true;
                            Slog.d(LOG_TAG, "Successed wipe storage data.");
                        }
                    } catch (SecurityException e) {
                        Slog.w(LOG_TAG, "Failed to wipe user " + userId + " after max failed password attempts reached.", e);
                    }
                }
                if (!isCustWipeData) {
                    wipeDataNoLock(strictestAdmin.info.getComponent(), 0, reason, userId);
                }
            }
            if (this.mInjector.securityLogIsLoggingEnabled()) {
                SecurityLog.writeEvent(210007, new Object[]{Integer.valueOf(0), Integer.valueOf(1)});
            }
        } finally {
            this.mInjector.binderRestoreCallingIdentity(ident);
        }
    }

    public void reportSuccessfulPasswordAttempt(int userHandle) {
        enforceFullCrossUsersPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("android.permission.BIND_DEVICE_ADMIN", null);
        synchronized (this) {
            DevicePolicyData policy = getUserData(userHandle);
            if (policy.mFailedPasswordAttempts != 0 || policy.mPasswordOwner >= 0) {
                long ident = this.mInjector.binderClearCallingIdentity();
                try {
                    policy.mFailedPasswordAttempts = 0;
                    policy.mPasswordOwner = -1;
                    saveSettingsLocked(userHandle);
                    if (this.mHasFeature) {
                        sendAdminCommandForLockscreenPoliciesLocked("android.app.action.ACTION_PASSWORD_SUCCEEDED", 1, userHandle);
                    }
                    this.mInjector.binderRestoreCallingIdentity(ident);
                } catch (Throwable th) {
                    this.mInjector.binderRestoreCallingIdentity(ident);
                }
            }
        }
        if (this.mInjector.securityLogIsLoggingEnabled()) {
            SecurityLog.writeEvent(210007, new Object[]{Integer.valueOf(1), Integer.valueOf(1)});
        }
    }

    public void reportFailedFingerprintAttempt(int userHandle) {
        enforceFullCrossUsersPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("android.permission.BIND_DEVICE_ADMIN", null);
        if (this.mInjector.securityLogIsLoggingEnabled()) {
            SecurityLog.writeEvent(210007, new Object[]{Integer.valueOf(0), Integer.valueOf(0)});
        }
    }

    public void reportSuccessfulFingerprintAttempt(int userHandle) {
        enforceFullCrossUsersPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("android.permission.BIND_DEVICE_ADMIN", null);
        if (this.mInjector.securityLogIsLoggingEnabled()) {
            SecurityLog.writeEvent(210007, new Object[]{Integer.valueOf(1), Integer.valueOf(0)});
        }
    }

    public void reportKeyguardDismissed(int userHandle) {
        enforceFullCrossUsersPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("android.permission.BIND_DEVICE_ADMIN", null);
        if (this.mInjector.securityLogIsLoggingEnabled()) {
            SecurityLog.writeEvent(210006, new Object[0]);
        }
    }

    public void reportKeyguardSecured(int userHandle) {
        enforceFullCrossUsersPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("android.permission.BIND_DEVICE_ADMIN", null);
        if (this.mInjector.securityLogIsLoggingEnabled()) {
            SecurityLog.writeEvent(210008, new Object[0]);
        }
    }

    /* JADX WARNING: Missing block: B:30:0x008a, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public ComponentName setGlobalProxy(ComponentName who, String proxySpec, String exclusionList) {
        if (!this.mHasFeature) {
            return null;
        }
        synchronized (this) {
            Preconditions.checkNotNull(who, "ComponentName is null");
            DevicePolicyData policy = getUserData(0);
            ActiveAdmin admin = getActiveAdminForCallerLocked(who, 5);
            for (ComponentName component : policy.mAdminMap.keySet()) {
                if (((ActiveAdmin) policy.mAdminMap.get(component)).specifiesGlobalProxy && (component.equals(who) ^ 1) != 0) {
                    return component;
                }
            }
            if (UserHandle.getCallingUserId() != 0) {
                Slog.w(LOG_TAG, "Only the owner is allowed to set the global proxy. User " + UserHandle.getCallingUserId() + " is not permitted.");
                return null;
            }
            if (proxySpec == null) {
                admin.specifiesGlobalProxy = false;
                admin.globalProxySpec = null;
                admin.globalProxyExclusionList = null;
            } else {
                admin.specifiesGlobalProxy = true;
                admin.globalProxySpec = proxySpec;
                admin.globalProxyExclusionList = exclusionList;
            }
            long origId = this.mInjector.binderClearCallingIdentity();
            try {
                resetGlobalProxyLocked(policy);
            } finally {
                this.mInjector.binderRestoreCallingIdentity(origId);
            }
        }
    }

    public ComponentName getGlobalProxyAdmin(int userHandle) {
        if (!this.mHasFeature) {
            return null;
        }
        enforceFullCrossUsersPermission(userHandle);
        synchronized (this) {
            DevicePolicyData policy = getUserData(0);
            int N = policy.mAdminList.size();
            for (int i = 0; i < N; i++) {
                ActiveAdmin ap = (ActiveAdmin) policy.mAdminList.get(i);
                if (ap.specifiesGlobalProxy) {
                    ComponentName component = ap.info.getComponent();
                    return component;
                }
            }
            return null;
        }
    }

    public void setRecommendedGlobalProxy(ComponentName who, ProxyInfo proxyInfo) {
        synchronized (this) {
            getActiveAdminForCallerLocked(who, -2);
        }
        long token = this.mInjector.binderClearCallingIdentity();
        try {
            ((ConnectivityManager) this.mContext.getSystemService("connectivity")).setGlobalProxy(proxyInfo);
        } finally {
            this.mInjector.binderRestoreCallingIdentity(token);
        }
    }

    private void resetGlobalProxyLocked(DevicePolicyData policy) {
        int N = policy.mAdminList.size();
        for (int i = 0; i < N; i++) {
            ActiveAdmin ap = (ActiveAdmin) policy.mAdminList.get(i);
            if (ap.specifiesGlobalProxy) {
                saveGlobalProxyLocked(ap.globalProxySpec, ap.globalProxyExclusionList);
                return;
            }
        }
        saveGlobalProxyLocked(null, null);
    }

    private void saveGlobalProxyLocked(String proxySpec, String exclusionList) {
        if (exclusionList == null) {
            exclusionList = "";
        }
        if (proxySpec == null) {
            proxySpec = "";
        }
        String[] data = proxySpec.trim().split(":");
        int proxyPort = 8080;
        if (data.length > 1) {
            try {
                proxyPort = Integer.parseInt(data[1]);
            } catch (NumberFormatException e) {
            }
        }
        exclusionList = exclusionList.trim();
        ProxyInfo proxyProperties = new ProxyInfo(data[0], proxyPort, exclusionList);
        if (proxyProperties.isValid()) {
            this.mInjector.settingsGlobalPutString("global_http_proxy_host", data[0]);
            this.mInjector.settingsGlobalPutInt("global_http_proxy_port", proxyPort);
            this.mInjector.settingsGlobalPutString("global_http_proxy_exclusion_list", exclusionList);
            return;
        }
        Slog.e(LOG_TAG, "Invalid proxy properties, ignoring: " + proxyProperties.toString());
    }

    /* JADX WARNING: Missing block: B:27:0x0074, code:
            return r6;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int setStorageEncryption(ComponentName who, boolean encrypt) {
        if (!this.mHasFeature) {
            return 0;
        }
        Preconditions.checkNotNull(who, "ComponentName is null");
        int userHandle = UserHandle.getCallingUserId();
        synchronized (this) {
            if (userHandle != 0) {
                Slog.w(LOG_TAG, "Only owner/system user is allowed to set storage encryption. User " + UserHandle.getCallingUserId() + " is not permitted.");
                return 0;
            }
            ActiveAdmin ap = getActiveAdminForCallerLocked(who, 7);
            if (isEncryptionSupported()) {
                if (ap.encryptionRequested != encrypt) {
                    ap.encryptionRequested = encrypt;
                    saveSettingsLocked(userHandle);
                }
                DevicePolicyData policy = getUserData(0);
                boolean newRequested = false;
                for (int i = 0; i < policy.mAdminList.size(); i++) {
                    newRequested |= ((ActiveAdmin) policy.mAdminList.get(i)).encryptionRequested;
                }
                setEncryptionRequested(newRequested);
                int i2;
                if (newRequested) {
                    i2 = 3;
                } else {
                    i2 = 1;
                }
            } else {
                return 0;
            }
        }
    }

    /* JADX WARNING: Missing block: B:11:0x0015, code:
            return r4;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean getStorageEncryption(ComponentName who, int userHandle) {
        if (!this.mHasFeature) {
            return false;
        }
        enforceFullCrossUsersPermission(userHandle);
        synchronized (this) {
            if (who != null) {
                ActiveAdmin ap = getActiveAdminUncheckedLocked(who, userHandle);
                boolean z = ap != null ? ap.encryptionRequested : false;
            } else {
                DevicePolicyData policy = getUserData(userHandle);
                int N = policy.mAdminList.size();
                for (int i = 0; i < N; i++) {
                    if (((ActiveAdmin) policy.mAdminList.get(i)).encryptionRequested) {
                        return true;
                    }
                }
                return false;
            }
        }
    }

    public int getStorageEncryptionStatus(String callerPackage, int userHandle) {
        boolean z = this.mHasFeature;
        enforceFullCrossUsersPermission(userHandle);
        ensureCallerPackage(callerPackage);
        try {
            boolean legacyApp = false;
            if (this.mIPackageManager.getApplicationInfo(callerPackage, 0, userHandle).targetSdkVersion <= 23) {
                legacyApp = true;
            }
            int rawStatus = getEncryptionStatus();
            if (rawStatus == 5 && legacyApp) {
                return 3;
            }
            return rawStatus;
        } catch (RemoteException e) {
            throw new SecurityException(e);
        }
    }

    private boolean isEncryptionSupported() {
        return getEncryptionStatus() != 0;
    }

    private int getEncryptionStatus() {
        if (this.mInjector.storageManagerIsFileBasedEncryptionEnabled()) {
            return 5;
        }
        if (this.mInjector.storageManagerIsNonDefaultBlockEncrypted()) {
            return 3;
        }
        if (isSecureBlockEncrypted()) {
            Log.i(LOG_TAG, "return ENCRYPTION_STATUS_ACTIVE for old products upgrade to N with secure block encryption.");
            return 3;
        } else if (this.mInjector.storageManagerIsEncrypted()) {
            return 4;
        } else {
            if (this.mInjector.storageManagerIsEncryptable()) {
                return 1;
            }
            return 0;
        }
    }

    private void setEncryptionRequested(boolean encrypt) {
    }

    public void setScreenCaptureDisabled(ComponentName who, boolean disabled) {
        if (this.mHasFeature) {
            Preconditions.checkNotNull(who, "ComponentName is null");
            int userHandle = UserHandle.getCallingUserId();
            synchronized (this) {
                ActiveAdmin ap = getActiveAdminForCallerLocked(who, -1);
                if (ap.disableScreenCapture != disabled) {
                    ap.disableScreenCapture = disabled;
                    saveSettingsLocked(userHandle);
                    updateScreenCaptureDisabledInWindowManager(userHandle, disabled);
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:10:0x0012, code:
            return r4;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean getScreenCaptureDisabled(ComponentName who, int userHandle) {
        boolean z = false;
        if (!this.mHasFeature) {
            return false;
        }
        synchronized (this) {
            if (who != null) {
                ActiveAdmin admin = getActiveAdminUncheckedLocked(who, userHandle);
                if (admin != null) {
                    z = admin.disableScreenCapture;
                }
            } else {
                DevicePolicyData policy = getUserData(userHandle);
                int N = policy.mAdminList.size();
                for (int i = 0; i < N; i++) {
                    if (((ActiveAdmin) policy.mAdminList.get(i)).disableScreenCapture) {
                        return true;
                    }
                }
                return false;
            }
        }
    }

    private void updateScreenCaptureDisabledInWindowManager(final int userHandle, final boolean disabled) {
        this.mHandler.post(new Runnable() {
            public void run() {
                try {
                    DevicePolicyManagerService.this.mInjector.getIWindowManager().setScreenCaptureDisabled(userHandle, disabled);
                } catch (RemoteException e) {
                    Log.w(DevicePolicyManagerService.LOG_TAG, "Unable to notify WindowManager.", e);
                }
            }
        });
    }

    public void setAutoTimeRequired(ComponentName who, boolean required) {
        if (this.mHasFeature) {
            Preconditions.checkNotNull(who, "ComponentName is null");
            int userHandle = UserHandle.getCallingUserId();
            synchronized (this) {
                ActiveAdmin admin = getActiveAdminForCallerLocked(who, -1);
                if (admin.requireAutoTime != required) {
                    admin.requireAutoTime = required;
                    saveSettingsLocked(userHandle);
                }
            }
            if (required) {
                long ident = this.mInjector.binderClearCallingIdentity();
                try {
                    this.mInjector.settingsGlobalPutInt("auto_time", 1);
                } finally {
                    this.mInjector.binderRestoreCallingIdentity(ident);
                }
            }
        }
    }

    public boolean getAutoTimeRequired() {
        if (!this.mHasFeature) {
            return false;
        }
        synchronized (this) {
            ActiveAdmin deviceOwner = getDeviceOwnerAdminLocked();
            if (deviceOwner == null || !deviceOwner.requireAutoTime) {
                for (Integer userId : this.mOwners.getProfileOwnerKeys()) {
                    ActiveAdmin profileOwner = getProfileOwnerAdminLocked(userId.intValue());
                    if (profileOwner != null && profileOwner.requireAutoTime) {
                        return true;
                    }
                }
                return false;
            }
            return true;
        }
    }

    public void setForceEphemeralUsers(ComponentName who, boolean forceEphemeralUsers) {
        if (this.mHasFeature) {
            Preconditions.checkNotNull(who, "ComponentName is null");
            if (!forceEphemeralUsers || (this.mInjector.userManagerIsSplitSystemUser() ^ 1) == 0) {
                boolean removeAllUsers = false;
                synchronized (this) {
                    ActiveAdmin deviceOwner = getActiveAdminForCallerLocked(who, -2);
                    if (deviceOwner.forceEphemeralUsers != forceEphemeralUsers) {
                        deviceOwner.forceEphemeralUsers = forceEphemeralUsers;
                        saveSettingsLocked(this.mInjector.userHandleGetCallingUserId());
                        this.mUserManagerInternal.setForceEphemeralUsers(forceEphemeralUsers);
                        removeAllUsers = forceEphemeralUsers;
                    }
                }
                if (removeAllUsers) {
                    long identitity = this.mInjector.binderClearCallingIdentity();
                    try {
                        this.mUserManagerInternal.removeAllUsers();
                    } finally {
                        this.mInjector.binderRestoreCallingIdentity(identitity);
                    }
                }
                return;
            }
            throw new UnsupportedOperationException("Cannot force ephemeral users on systems without split system user.");
        }
    }

    public boolean getForceEphemeralUsers(ComponentName who) {
        if (!this.mHasFeature) {
            return false;
        }
        boolean z;
        Preconditions.checkNotNull(who, "ComponentName is null");
        synchronized (this) {
            z = getActiveAdminForCallerLocked(who, -2).forceEphemeralUsers;
        }
        return z;
    }

    private void ensureDeviceOwnerAndAllUsersAffiliated(ComponentName who) throws SecurityException {
        synchronized (this) {
            getActiveAdminForCallerLocked(who, -2);
            if (areAllUsersAffiliatedWithDeviceLocked()) {
            } else {
                throw new SecurityException("Not all users are affiliated.");
            }
        }
    }

    public boolean requestBugreport(ComponentName who) {
        if (!this.mHasFeature) {
            return false;
        }
        Preconditions.checkNotNull(who, "ComponentName is null");
        ensureDeviceOwnerAndAllUsersAffiliated(who);
        if (this.mRemoteBugreportServiceIsActive.get() || getDeviceOwnerRemoteBugreportUri() != null) {
            Slog.d(LOG_TAG, "Remote bugreport wasn't started because there's already one running.");
            return false;
        }
        long currentTime = System.currentTimeMillis();
        synchronized (this) {
            DevicePolicyData policyData = getUserData(0);
            if (currentTime > policyData.mLastBugReportRequestTime) {
                policyData.mLastBugReportRequestTime = currentTime;
                saveSettingsLocked(0);
            }
        }
        long callingIdentity = this.mInjector.binderClearCallingIdentity();
        try {
            this.mInjector.getIActivityManager().requestBugReport(2);
            this.mRemoteBugreportServiceIsActive.set(true);
            this.mRemoteBugreportSharingAccepted.set(false);
            registerRemoteBugreportReceivers();
            this.mInjector.getNotificationManager().notifyAsUser(LOG_TAG, 678432343, RemoteBugreportUtils.buildNotification(this.mContext, 1), UserHandle.ALL);
            this.mHandler.postDelayed(this.mRemoteBugreportTimeoutRunnable, LocationFudger.FASTEST_INTERVAL_MS);
            return true;
        } catch (RemoteException re) {
            Slog.e(LOG_TAG, "Failed to make remote calls to start bugreportremote service", re);
            return false;
        } finally {
            this.mInjector.binderRestoreCallingIdentity(callingIdentity);
        }
    }

    synchronized void sendDeviceOwnerCommand(String action, Bundle extras) {
        Intent intent = new Intent(action);
        intent.setComponent(this.mOwners.getDeviceOwnerComponent());
        if (extras != null) {
            intent.putExtras(extras);
        }
        this.mContext.sendBroadcastAsUser(intent, UserHandle.of(this.mOwners.getDeviceOwnerUserId()));
    }

    private synchronized String getDeviceOwnerRemoteBugreportUri() {
        return this.mOwners.getDeviceOwnerRemoteBugreportUri();
    }

    private synchronized void setDeviceOwnerRemoteBugreportUriAndHash(String bugreportUri, String bugreportHash) {
        this.mOwners.setDeviceOwnerRemoteBugreportUriAndHash(bugreportUri, bugreportHash);
    }

    private void registerRemoteBugreportReceivers() {
        try {
            this.mContext.registerReceiver(this.mRemoteBugreportFinishedReceiver, new IntentFilter("android.intent.action.REMOTE_BUGREPORT_DISPATCH", "application/vnd.android.bugreport"));
        } catch (MalformedMimeTypeException e) {
            Slog.w(LOG_TAG, "Failed to set type application/vnd.android.bugreport", e);
        }
        IntentFilter filterConsent = new IntentFilter();
        filterConsent.addAction("com.android.server.action.REMOTE_BUGREPORT_SHARING_DECLINED");
        filterConsent.addAction("com.android.server.action.REMOTE_BUGREPORT_SHARING_ACCEPTED");
        this.mContext.registerReceiver(this.mRemoteBugreportConsentReceiver, filterConsent);
    }

    private void onBugreportFinished(Intent intent) {
        this.mHandler.removeCallbacks(this.mRemoteBugreportTimeoutRunnable);
        this.mRemoteBugreportServiceIsActive.set(false);
        Uri bugreportUri = intent.getData();
        String bugreportUriString = null;
        if (bugreportUri != null) {
            bugreportUriString = bugreportUri.toString();
        }
        String bugreportHash = intent.getStringExtra("android.intent.extra.REMOTE_BUGREPORT_HASH");
        if (this.mRemoteBugreportSharingAccepted.get()) {
            shareBugreportWithDeviceOwnerIfExists(bugreportUriString, bugreportHash);
            this.mInjector.getNotificationManager().cancel(LOG_TAG, 678432343);
        } else {
            setDeviceOwnerRemoteBugreportUriAndHash(bugreportUriString, bugreportHash);
            this.mInjector.getNotificationManager().notifyAsUser(LOG_TAG, 678432343, RemoteBugreportUtils.buildNotification(this.mContext, 3), UserHandle.ALL);
        }
        this.mContext.unregisterReceiver(this.mRemoteBugreportFinishedReceiver);
    }

    private void onBugreportFailed() {
        this.mRemoteBugreportServiceIsActive.set(false);
        this.mInjector.systemPropertiesSet("ctl.stop", "bugreportremote");
        this.mRemoteBugreportSharingAccepted.set(false);
        setDeviceOwnerRemoteBugreportUriAndHash(null, null);
        this.mInjector.getNotificationManager().cancel(LOG_TAG, 678432343);
        Bundle extras = new Bundle();
        extras.putInt("android.app.extra.BUGREPORT_FAILURE_REASON", 0);
        sendDeviceOwnerCommand("android.app.action.BUGREPORT_FAILED", extras);
        this.mContext.unregisterReceiver(this.mRemoteBugreportConsentReceiver);
        this.mContext.unregisterReceiver(this.mRemoteBugreportFinishedReceiver);
    }

    private void onBugreportSharingAccepted() {
        String bugreportUriString;
        String bugreportHash;
        this.mRemoteBugreportSharingAccepted.set(true);
        synchronized (this) {
            bugreportUriString = getDeviceOwnerRemoteBugreportUri();
            bugreportHash = this.mOwners.getDeviceOwnerRemoteBugreportHash();
        }
        if (bugreportUriString != null) {
            shareBugreportWithDeviceOwnerIfExists(bugreportUriString, bugreportHash);
        } else if (this.mRemoteBugreportServiceIsActive.get()) {
            this.mInjector.getNotificationManager().notifyAsUser(LOG_TAG, 678432343, RemoteBugreportUtils.buildNotification(this.mContext, 2), UserHandle.ALL);
        }
    }

    private void onBugreportSharingDeclined() {
        if (this.mRemoteBugreportServiceIsActive.get()) {
            this.mInjector.systemPropertiesSet("ctl.stop", "bugreportremote");
            this.mRemoteBugreportServiceIsActive.set(false);
            this.mHandler.removeCallbacks(this.mRemoteBugreportTimeoutRunnable);
            this.mContext.unregisterReceiver(this.mRemoteBugreportFinishedReceiver);
        }
        this.mRemoteBugreportSharingAccepted.set(false);
        setDeviceOwnerRemoteBugreportUriAndHash(null, null);
        sendDeviceOwnerCommand("android.app.action.BUGREPORT_SHARING_DECLINED", null);
    }

    private void shareBugreportWithDeviceOwnerIfExists(String bugreportUriString, String bugreportHash) {
        ParcelFileDescriptor pfd = null;
        if (bugreportUriString == null) {
            try {
                throw new FileNotFoundException();
            } catch (FileNotFoundException e) {
                Bundle extras = new Bundle();
                extras.putInt("android.app.extra.BUGREPORT_FAILURE_REASON", 1);
                sendDeviceOwnerCommand("android.app.action.BUGREPORT_FAILED", extras);
                if (pfd != null) {
                    try {
                        pfd.close();
                    } catch (IOException e2) {
                    }
                }
                this.mRemoteBugreportSharingAccepted.set(false);
                setDeviceOwnerRemoteBugreportUriAndHash(null, null);
            } catch (Throwable th) {
                if (pfd != null) {
                    try {
                        pfd.close();
                    } catch (IOException e3) {
                    }
                }
                this.mRemoteBugreportSharingAccepted.set(false);
                setDeviceOwnerRemoteBugreportUriAndHash(null, null);
            }
        } else {
            Uri bugreportUri = Uri.parse(bugreportUriString);
            pfd = this.mContext.getContentResolver().openFileDescriptor(bugreportUri, "r");
            synchronized (this) {
                Intent intent = new Intent("android.app.action.BUGREPORT_SHARE");
                intent.setComponent(this.mOwners.getDeviceOwnerComponent());
                intent.setDataAndType(bugreportUri, "application/vnd.android.bugreport");
                intent.putExtra("android.app.extra.BUGREPORT_HASH", bugreportHash);
                intent.setFlags(1);
                ((ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class)).grantUriPermissionFromIntent(IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME, this.mOwners.getDeviceOwnerComponent().getPackageName(), intent, this.mOwners.getDeviceOwnerUserId());
                this.mContext.sendBroadcastAsUser(intent, UserHandle.of(this.mOwners.getDeviceOwnerUserId()));
            }
            if (pfd != null) {
                try {
                    pfd.close();
                } catch (IOException e4) {
                }
            }
            this.mRemoteBugreportSharingAccepted.set(false);
            setDeviceOwnerRemoteBugreportUriAndHash(null, null);
        }
    }

    public void setCameraDisabled(ComponentName who, boolean disabled) {
        if (this.mHasFeature) {
            Preconditions.checkNotNull(who, "ComponentName is null");
            int userHandle = this.mInjector.userHandleGetCallingUserId();
            synchronized (this) {
                ActiveAdmin ap = getActiveAdminForCallerLocked(who, 8);
                if (ap.disableCamera != disabled) {
                    ap.disableCamera = disabled;
                    saveSettingsLocked(userHandle);
                }
            }
            pushUserRestrictions(userHandle);
        }
    }

    public boolean getCameraDisabled(ComponentName who, int userHandle) {
        return getCameraDisabled(who, userHandle, true);
    }

    /* JADX WARNING: Missing block: B:10:0x0013, code:
            return r5;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean getCameraDisabled(ComponentName who, int userHandle, boolean mergeDeviceOwnerRestriction) {
        boolean z = false;
        if (!this.mHasFeature) {
            return false;
        }
        synchronized (this) {
            if (who != null) {
                ActiveAdmin admin = getActiveAdminUncheckedLocked(who, userHandle);
                if (admin != null) {
                    z = admin.disableCamera;
                }
            } else {
                if (mergeDeviceOwnerRestriction) {
                    ActiveAdmin deviceOwner = getDeviceOwnerAdminLocked();
                    if (deviceOwner != null && deviceOwner.disableCamera) {
                        return true;
                    }
                }
                DevicePolicyData policy = getUserData(userHandle);
                int N = policy.mAdminList.size();
                for (int i = 0; i < N; i++) {
                    if (((ActiveAdmin) policy.mAdminList.get(i)).disableCamera) {
                        return true;
                    }
                }
                return false;
            }
        }
    }

    public void setKeyguardDisabledFeatures(ComponentName who, int which, boolean parent) {
        if (this.mHasFeature) {
            Slog.w(LOG_TAG, "setKeyguardDisabledFeatures to " + which + "; by: " + who);
            Preconditions.checkNotNull(who, "ComponentName is null");
            int userHandle = this.mInjector.userHandleGetCallingUserId();
            if (isManagedProfile(userHandle)) {
                if (parent) {
                    which &= 48;
                } else {
                    which &= 56;
                }
            }
            synchronized (this) {
                ActiveAdmin ap = getActiveAdminForCallerLocked(who, 9, parent);
                if (ap.disabledKeyguardFeatures != which) {
                    ap.disabledKeyguardFeatures = which;
                    saveSettingsLocked(userHandle);
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:21:0x0037 A:{Catch:{ all -> 0x0071 }} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int getKeyguardDisabledFeatures(ComponentName who, int userHandle, boolean parent) {
        int i = 0;
        if (!this.mHasFeature) {
            return 0;
        }
        enforceFullCrossUsersPermission(userHandle);
        long ident = this.mInjector.binderClearCallingIdentity();
        try {
            synchronized (this) {
                ActiveAdmin admin;
                if (who != null) {
                    admin = getActiveAdminUncheckedLocked(who, userHandle, parent);
                    if (admin != null) {
                        i = admin.disabledKeyguardFeatures;
                    }
                } else {
                    List<ActiveAdmin> admins;
                    int which;
                    int N;
                    int i2;
                    if (!parent) {
                        if (isManagedProfile(userHandle)) {
                            admins = getUserDataUnchecked(userHandle).mAdminList;
                            which = 0;
                            N = admins.size();
                            for (i2 = 0; i2 < N; i2++) {
                                admin = (ActiveAdmin) admins.get(i2);
                                int userId = admin.getUserHandle().getIdentifier();
                                boolean isRequestedUser = !parent && userId == userHandle;
                                if (isRequestedUser || (isManagedProfile(userId) ^ 1) != 0) {
                                    i = admin.disabledKeyguardFeatures;
                                } else {
                                    i = admin.disabledKeyguardFeatures & 48;
                                }
                                which |= i;
                            }
                            this.mInjector.binderRestoreCallingIdentity(ident);
                            return which;
                        }
                    }
                    admins = getActiveAdminsForLockscreenPoliciesLocked(userHandle, parent);
                    which = 0;
                    N = admins.size();
                    while (i2 < N) {
                    }
                    this.mInjector.binderRestoreCallingIdentity(ident);
                    return which;
                }
            }
        } finally {
            this.mInjector.binderRestoreCallingIdentity(ident);
        }
        return i;
    }

    public void setKeepUninstalledPackages(ComponentName who, String callerPackage, List<String> packageList) {
        if (this.mHasFeature) {
            Preconditions.checkNotNull(packageList, "packageList is null");
            int userHandle = UserHandle.getCallingUserId();
            synchronized (this) {
                enforceCanManageScope(who, callerPackage, -2, "delegation-keep-uninstalled-packages");
                getDeviceOwnerAdminLocked().keepUninstalledPackages = packageList;
                saveSettingsLocked(userHandle);
                this.mInjector.getPackageManagerInternal().setKeepUninstalledPackages(packageList);
            }
        }
    }

    public List<String> getKeepUninstalledPackages(ComponentName who, String callerPackage) {
        if (!this.mHasFeature) {
            return null;
        }
        List<String> keepUninstalledPackagesLocked;
        synchronized (this) {
            enforceCanManageScope(who, callerPackage, -2, "delegation-keep-uninstalled-packages");
            keepUninstalledPackagesLocked = getKeepUninstalledPackagesLocked();
        }
        return keepUninstalledPackagesLocked;
    }

    private List<String> getKeepUninstalledPackagesLocked() {
        ActiveAdmin deviceOwner = getDeviceOwnerAdminLocked();
        if (deviceOwner != null) {
            return deviceOwner.keepUninstalledPackages;
        }
        return null;
    }

    public boolean setDeviceOwner(ComponentName admin, String ownerName, int userId) {
        if (!this.mHasFeature) {
            return false;
        }
        if (admin == null || (isPackageInstalledForUser(admin.getPackageName(), userId) ^ 1) != 0) {
            throw new IllegalArgumentException("Invalid component " + admin + " for device owner");
        }
        boolean hasIncompatibleAccountsOrNonAdb = hasIncompatibleAccountsOrNonAdbNoLock(userId, admin);
        synchronized (this) {
            enforceCanSetDeviceOwnerLocked(admin, userId, hasIncompatibleAccountsOrNonAdb);
            ActiveAdmin activeAdmin = getActiveAdminUncheckedLocked(admin, userId);
            if (activeAdmin == null || getUserData(userId).mRemovingAdmins.contains(admin)) {
                throw new IllegalArgumentException("Not active admin: " + admin);
            }
            long ident = this.mInjector.binderClearCallingIdentity();
            try {
                if (this.mInjector.getIBackupManager() != null) {
                    this.mInjector.getIBackupManager().setBackupServiceActive(0, false);
                }
                this.mInjector.binderRestoreCallingIdentity(ident);
                if (isAdb()) {
                    MetricsLogger.action(this.mContext, NetdResponseCode.StrictCleartext, LOG_TAG_DEVICE_OWNER);
                }
                this.mOwners.setDeviceOwner(admin, ownerName, userId);
                this.mOwners.writeDeviceOwner();
                updateDeviceOwnerLocked();
                setDeviceOwnerSystemPropertyLocked();
                Set<String> restrictions = UserRestrictionsUtils.getDefaultEnabledForDeviceOwner();
                if (!restrictions.isEmpty()) {
                    for (String restriction : restrictions) {
                        activeAdmin.ensureUserRestrictions().putBoolean(restriction, true);
                    }
                    activeAdmin.defaultEnabledRestrictionsAlreadySet.addAll(restrictions);
                    Slog.i(LOG_TAG, "Enabled the following restrictions by default: " + restrictions);
                    saveUserRestrictionsLocked(userId);
                }
                ident = this.mInjector.binderClearCallingIdentity();
                try {
                    this.mContext.sendBroadcastAsUser(new Intent("android.app.action.DEVICE_OWNER_CHANGED").addFlags(16777216), UserHandle.of(userId));
                    this.mDeviceAdminServiceController.startServiceForOwner(admin.getPackageName(), userId, "set-device-owner");
                    Slog.i(LOG_TAG, "Device owner set: " + admin + " on user " + userId);
                } finally {
                    this.mInjector.binderRestoreCallingIdentity(ident);
                }
            } catch (RemoteException e) {
                throw new IllegalStateException("Failed deactivating backup service.", e);
            } catch (Throwable th) {
                this.mInjector.binderRestoreCallingIdentity(ident);
            }
        }
        return true;
    }

    public boolean hasDeviceOwner() {
        enforceDeviceOwnerOrManageUsers();
        return this.mOwners.hasDeviceOwner();
    }

    boolean isDeviceOwner(ActiveAdmin admin) {
        return isDeviceOwner(admin.info.getComponent(), admin.getUserHandle().getIdentifier());
    }

    public boolean isDeviceOwner(ComponentName who, int userId) {
        boolean equals;
        synchronized (this) {
            if (this.mOwners.hasDeviceOwner() && this.mOwners.getDeviceOwnerUserId() == userId) {
                equals = this.mOwners.getDeviceOwnerComponent().equals(who);
            } else {
                equals = false;
            }
        }
        return equals;
    }

    private boolean isDeviceOwnerPackage(String packageName, int userId) {
        boolean equals;
        synchronized (this) {
            if (this.mOwners.hasDeviceOwner() && this.mOwners.getDeviceOwnerUserId() == userId) {
                equals = this.mOwners.getDeviceOwnerPackageName().equals(packageName);
            } else {
                equals = false;
            }
        }
        return equals;
    }

    private boolean isProfileOwnerPackage(String packageName, int userId) {
        boolean equals;
        synchronized (this) {
            if (this.mOwners.hasProfileOwner(userId)) {
                equals = this.mOwners.getProfileOwnerPackage(userId).equals(packageName);
            } else {
                equals = false;
            }
        }
        return equals;
    }

    public boolean isProfileOwner(ComponentName who, int userId) {
        return who != null ? who.equals(getProfileOwner(userId)) : false;
    }

    public ComponentName getDeviceOwnerComponent(boolean callingUserOnly) {
        if (!this.mHasFeature) {
            return null;
        }
        if (!callingUserOnly) {
            enforceManageUsers();
        }
        synchronized (this) {
            if (this.mOwners.hasDeviceOwner()) {
                if (callingUserOnly) {
                    if (this.mInjector.userHandleGetCallingUserId() != this.mOwners.getDeviceOwnerUserId()) {
                        return null;
                    }
                }
                ComponentName deviceOwnerComponent = this.mOwners.getDeviceOwnerComponent();
                return deviceOwnerComponent;
            }
            return null;
        }
    }

    public int getDeviceOwnerUserId() {
        int i = -10000;
        if (!this.mHasFeature) {
            return -10000;
        }
        enforceManageUsers();
        synchronized (this) {
            if (this.mOwners.hasDeviceOwner()) {
                i = this.mOwners.getDeviceOwnerUserId();
            }
        }
        return i;
    }

    public String getDeviceOwnerName() {
        if (!this.mHasFeature) {
            return null;
        }
        enforceManageUsers();
        synchronized (this) {
            if (this.mOwners.hasDeviceOwner()) {
                String applicationLabel = getApplicationLabel(this.mOwners.getDeviceOwnerPackageName(), 0);
                return applicationLabel;
            }
            return null;
        }
    }

    ActiveAdmin getDeviceOwnerAdminLocked() {
        ComponentName component = this.mOwners.getDeviceOwnerComponent();
        if (component == null) {
            return null;
        }
        DevicePolicyData policy = getUserData(this.mOwners.getDeviceOwnerUserId());
        int n = policy.mAdminList.size();
        for (int i = 0; i < n; i++) {
            ActiveAdmin admin = (ActiveAdmin) policy.mAdminList.get(i);
            if (component.equals(admin.info.getComponent())) {
                return admin;
            }
        }
        Slog.wtf(LOG_TAG, "Active admin for device owner not found. component=" + component);
        return null;
    }

    public void clearDeviceOwner(String packageName) {
        Preconditions.checkNotNull(packageName, "packageName is null");
        int callingUid = this.mInjector.binderGetCallingUid();
        try {
            int uid = this.mInjector.getPackageManager().getPackageUidAsUser(packageName, UserHandle.getUserId(callingUid));
            synchronized (this) {
                Slog.w(LOG_TAG, "clearDeviceOwner packageName=" + packageName + ",callingUid=" + callingUid + ",mIsMDMDeviceOwnerAPI=" + this.mIsMDMDeviceOwnerAPI);
                if (uid == callingUid || (this.mIsMDMDeviceOwnerAPI ^ 1) == 0) {
                } else {
                    throw new SecurityException("Invalid packageName");
                }
            }
            synchronized (this) {
                ComponentName deviceOwnerComponent = this.mOwners.getDeviceOwnerComponent();
                int deviceOwnerUserId = this.mOwners.getDeviceOwnerUserId();
                if (this.mOwners.hasDeviceOwner() && (deviceOwnerComponent.getPackageName().equals(packageName) ^ 1) == 0 && deviceOwnerUserId == UserHandle.getUserId(callingUid)) {
                    enforceUserUnlocked(deviceOwnerUserId);
                    ActiveAdmin admin = getDeviceOwnerAdminLocked();
                    long ident = this.mInjector.binderClearCallingIdentity();
                    try {
                        clearDeviceOwnerLocked(admin, deviceOwnerUserId);
                        removeActiveAdminLocked(deviceOwnerComponent, deviceOwnerUserId);
                        Intent intent = new Intent("android.app.action.DEVICE_OWNER_CHANGED");
                        intent.addFlags(16777216);
                        this.mContext.sendBroadcastAsUser(intent, UserHandle.of(deviceOwnerUserId));
                        Slog.i(LOG_TAG, "Device owner removed: " + deviceOwnerComponent);
                    } finally {
                        this.mInjector.binderRestoreCallingIdentity(ident);
                    }
                } else {
                    throw new SecurityException("clearDeviceOwner can only be called by the device owner");
                }
            }
        } catch (NameNotFoundException e) {
            throw new SecurityException(e);
        }
    }

    private void clearDeviceOwnerLocked(ActiveAdmin admin, int userId) {
        this.mDeviceAdminServiceController.stopServiceForOwner(userId, "clear-device-owner");
        if (admin != null) {
            admin.disableCamera = false;
            admin.userRestrictions = null;
            admin.defaultEnabledRestrictionsAlreadySet.clear();
            admin.forceEphemeralUsers = false;
            admin.isNetworkLoggingEnabled = false;
            this.mUserManagerInternal.setForceEphemeralUsers(admin.forceEphemeralUsers);
        }
        getUserData(userId).mCurrentInputMethodSet = false;
        saveSettingsLocked(userId);
        DevicePolicyData systemPolicyData = getUserData(0);
        systemPolicyData.mLastSecurityLogRetrievalTime = -1;
        systemPolicyData.mLastBugReportRequestTime = -1;
        systemPolicyData.mLastNetworkLogsRetrievalTime = -1;
        saveSettingsLocked(0);
        clearUserPoliciesLocked(userId);
        this.mOwners.clearDeviceOwner();
        this.mOwners.writeDeviceOwner();
        updateDeviceOwnerLocked();
        clearDeviceOwnerUserRestrictionLocked(UserHandle.of(userId));
        this.mInjector.securityLogSetLoggingEnabledProperty(false);
        this.mSecurityLogMonitor.stop();
        setNetworkLoggingActiveInternal(false);
        try {
            if (this.mInjector.getIBackupManager() != null) {
                this.mInjector.getIBackupManager().setBackupServiceActive(0, true);
            }
        } catch (RemoteException e) {
            throw new IllegalStateException("Failed reactivating backup service.", e);
        }
    }

    public boolean setProfileOwner(ComponentName who, String ownerName, int userHandle) {
        if (!this.mHasFeature) {
            return false;
        }
        if (who == null || (isPackageInstalledForUser(who.getPackageName(), userHandle) ^ 1) != 0) {
            throw new IllegalArgumentException("Component " + who + " not installed for userId:" + userHandle);
        }
        boolean hasIncompatibleAccountsOrNonAdb = hasIncompatibleAccountsOrNonAdbNoLock(userHandle, who);
        synchronized (this) {
            enforceCanSetProfileOwnerLocked(who, userHandle, hasIncompatibleAccountsOrNonAdb);
            ActiveAdmin admin = getActiveAdminUncheckedLocked(who, userHandle);
            if (admin == null || getUserData(userHandle).mRemovingAdmins.contains(who)) {
                throw new IllegalArgumentException("Not active admin: " + who);
            }
            if (isAdb()) {
                MetricsLogger.action(this.mContext, NetdResponseCode.StrictCleartext, LOG_TAG_PROFILE_OWNER);
            }
            this.mOwners.setProfileOwner(who, ownerName, userHandle);
            this.mOwners.writeProfileOwner(userHandle);
            Slog.i(LOG_TAG, "Profile owner set: " + who + " on user " + userHandle);
            long id = this.mInjector.binderClearCallingIdentity();
            try {
                if (this.mUserManager.isManagedProfile(userHandle)) {
                    maybeSetDefaultRestrictionsForAdminLocked(userHandle, admin, UserRestrictionsUtils.getDefaultEnabledForManagedProfiles());
                    ensureUnknownSourcesRestrictionForProfileOwnerLocked(userHandle, admin, true);
                }
                this.mDeviceAdminServiceController.startServiceForOwner(who.getPackageName(), userHandle, "set-profile-owner");
            } finally {
                this.mInjector.binderRestoreCallingIdentity(id);
            }
        }
        return true;
    }

    public void clearProfileOwner(ComponentName who) {
        if (this.mHasFeature) {
            Preconditions.checkNotNull(who, "ComponentName is null");
            int userId = this.mInjector.userHandleGetCallingUserId();
            enforceNotManagedProfile(userId, "clear profile owner");
            enforceUserUnlocked(userId);
            synchronized (this) {
                ActiveAdmin admin = getActiveAdminForCallerLocked(who, -1);
                long ident = this.mInjector.binderClearCallingIdentity();
                try {
                    clearProfileOwnerLocked(admin, userId);
                    removeActiveAdminLocked(who, userId);
                    this.mInjector.binderRestoreCallingIdentity(ident);
                    Slog.i(LOG_TAG, "Profile owner " + who + " removed from user " + userId);
                } catch (Throwable th) {
                    this.mInjector.binderRestoreCallingIdentity(ident);
                }
            }
        }
    }

    public void clearProfileOwnerLocked(ActiveAdmin admin, int userId) {
        this.mDeviceAdminServiceController.stopServiceForOwner(userId, "clear-profile-owner");
        if (admin != null) {
            admin.disableCamera = false;
            admin.userRestrictions = null;
            admin.defaultEnabledRestrictionsAlreadySet.clear();
        }
        DevicePolicyData policyData = getUserData(userId);
        policyData.mCurrentInputMethodSet = false;
        policyData.mOwnerInstalledCaCerts.clear();
        saveSettingsLocked(userId);
        clearUserPoliciesLocked(userId);
        this.mOwners.removeProfileOwner(userId);
        this.mOwners.writeProfileOwner(userId);
    }

    public void setDeviceOwnerLockScreenInfo(ComponentName who, CharSequence info) {
        String str = null;
        Preconditions.checkNotNull(who, "ComponentName is null");
        if (this.mHasFeature) {
            synchronized (this) {
                getActiveAdminForCallerLocked(who, -2);
                long token = this.mInjector.binderClearCallingIdentity();
                try {
                    LockPatternUtils lockPatternUtils = this.mLockPatternUtils;
                    if (info != null) {
                        str = info.toString();
                    }
                    lockPatternUtils.setDeviceOwnerInfo(str);
                    this.mInjector.binderRestoreCallingIdentity(token);
                } catch (Throwable th) {
                    this.mInjector.binderRestoreCallingIdentity(token);
                }
            }
        }
    }

    public CharSequence getDeviceOwnerLockScreenInfo() {
        return this.mLockPatternUtils.getDeviceOwnerInfo();
    }

    private void clearUserPoliciesLocked(int userId) {
        DevicePolicyData policy = getUserData(userId);
        policy.mPermissionPolicy = 0;
        policy.mDelegationMap.clear();
        policy.mStatusBarDisabled = false;
        policy.mUserProvisioningState = 0;
        policy.mAffiliationIds.clear();
        policy.mLockTaskPackages.clear();
        saveSettingsLocked(userId);
        try {
            this.mIPackageManager.updatePermissionFlagsForAllApps(4, 0, userId);
            pushUserRestrictions(userId);
        } catch (RemoteException e) {
        }
    }

    public boolean hasUserSetupCompleted() {
        return hasUserSetupCompleted(UserHandle.getCallingUserId());
    }

    private boolean hasUserSetupCompleted(int userHandle) {
        if (this.mHasFeature) {
            return getUserData(userHandle).mUserSetupComplete;
        }
        return true;
    }

    private boolean hasPaired(int userHandle) {
        if (this.mHasFeature) {
            return getUserData(userHandle).mPaired;
        }
        return true;
    }

    public int getUserProvisioningState() {
        if (this.mHasFeature) {
            return getUserProvisioningState(this.mInjector.userHandleGetCallingUserId());
        }
        return 0;
    }

    private int getUserProvisioningState(int userHandle) {
        return getUserData(userHandle).mUserProvisioningState;
    }

    public void setUserProvisioningState(int newState, int userHandle) {
        if (!this.mHasFeature) {
            return;
        }
        if (userHandle == this.mOwners.getDeviceOwnerUserId() || (this.mOwners.hasProfileOwner(userHandle) ^ 1) == 0 || getManagedUserId(userHandle) != -1) {
            synchronized (this) {
                boolean transitionCheckNeeded = true;
                if (!isAdb()) {
                    enforceCanManageProfileAndDeviceOwners();
                } else if (getUserProvisioningState(userHandle) == 0 && newState == 3) {
                    transitionCheckNeeded = false;
                } else {
                    throw new IllegalStateException("Not allowed to change provisioning state unless current provisioning state is unmanaged, and new state is finalized.");
                }
                DevicePolicyData policyData = getUserData(userHandle);
                if (transitionCheckNeeded) {
                    checkUserProvisioningStateTransition(policyData.mUserProvisioningState, newState);
                }
                policyData.mUserProvisioningState = newState;
                saveSettingsLocked(userHandle);
            }
            return;
        }
        throw new IllegalStateException("Not allowed to change provisioning state unless a device or profile owner is set.");
    }

    private void checkUserProvisioningStateTransition(int currentState, int newState) {
        switch (currentState) {
            case 0:
                if (newState != 0) {
                    return;
                }
                break;
            case 1:
            case 2:
                if (newState == 3) {
                    return;
                }
                break;
            case 4:
                if (newState == 0) {
                    return;
                }
                break;
        }
        throw new IllegalStateException("Cannot move to user provisioning state [" + newState + "] " + "from state [" + currentState + "]");
    }

    public void setProfileEnabled(ComponentName who) {
        if (this.mHasFeature) {
            Preconditions.checkNotNull(who, "ComponentName is null");
            synchronized (this) {
                getActiveAdminForCallerLocked(who, -1);
                int userId = UserHandle.getCallingUserId();
                enforceManagedProfile(userId, "enable the profile");
                if (getUserInfo(userId).isEnabled()) {
                    Slog.e(LOG_TAG, "setProfileEnabled is called when the profile is already enabled");
                    return;
                }
                long id = this.mInjector.binderClearCallingIdentity();
                try {
                    this.mUserManager.setUserEnabled(userId);
                    UserInfo parent = this.mUserManager.getProfileParent(userId);
                    Intent intent = new Intent("android.intent.action.MANAGED_PROFILE_ADDED");
                    intent.putExtra("android.intent.extra.USER", new UserHandle(userId));
                    intent.addFlags(1342177280);
                    this.mContext.sendBroadcastAsUser(intent, new UserHandle(parent.id));
                    this.mInjector.binderRestoreCallingIdentity(id);
                } catch (Throwable th) {
                    this.mInjector.binderRestoreCallingIdentity(id);
                }
            }
        }
    }

    public void setProfileName(ComponentName who, String profileName) {
        Preconditions.checkNotNull(who, "ComponentName is null");
        int userId = UserHandle.getCallingUserId();
        getActiveAdminForCallerLocked(who, -1);
        long id = this.mInjector.binderClearCallingIdentity();
        try {
            this.mUserManager.setUserName(userId, profileName);
        } finally {
            this.mInjector.binderRestoreCallingIdentity(id);
        }
    }

    public ComponentName getProfileOwner(int userHandle) {
        if (!this.mHasFeature) {
            return null;
        }
        ComponentName profileOwnerComponent;
        synchronized (this) {
            profileOwnerComponent = this.mOwners.getProfileOwnerComponent(userHandle);
        }
        return profileOwnerComponent;
    }

    ActiveAdmin getProfileOwnerAdminLocked(int userHandle) {
        ComponentName profileOwner = this.mOwners.getProfileOwnerComponent(userHandle);
        if (profileOwner == null) {
            return null;
        }
        DevicePolicyData policy = getUserData(userHandle);
        int n = policy.mAdminList.size();
        for (int i = 0; i < n; i++) {
            ActiveAdmin admin = (ActiveAdmin) policy.mAdminList.get(i);
            if (profileOwner.equals(admin.info.getComponent())) {
                return admin;
            }
        }
        return null;
    }

    public String getProfileOwnerName(int userHandle) {
        if (!this.mHasFeature) {
            return null;
        }
        enforceManageUsers();
        ComponentName profileOwner = getProfileOwner(userHandle);
        if (profileOwner == null) {
            return null;
        }
        return getApplicationLabel(profileOwner.getPackageName(), userHandle);
    }

    private String getApplicationLabel(String packageName, int userHandle) {
        String str = null;
        long token = this.mInjector.binderClearCallingIdentity();
        try {
            Context userContext = this.mContext.createPackageContextAsUser(packageName, 0, new UserHandle(userHandle));
            ApplicationInfo appInfo = userContext.getApplicationInfo();
            CharSequence result = null;
            if (appInfo != null) {
                result = userContext.getPackageManager().getApplicationLabel(appInfo);
            }
            if (result != null) {
                str = result.toString();
            }
            this.mInjector.binderRestoreCallingIdentity(token);
            return str;
        } catch (NameNotFoundException nnfe) {
            Log.w(LOG_TAG, packageName + " is not installed for user " + userHandle, nnfe);
            this.mInjector.binderRestoreCallingIdentity(token);
            return null;
        } catch (Throwable th) {
            this.mInjector.binderRestoreCallingIdentity(token);
            throw th;
        }
    }

    private void wtfIfInLock() {
        if (Thread.holdsLock(this)) {
            Slog.wtfStack(LOG_TAG, "Shouldn't be called with DPMS lock held");
        }
    }

    private void enforceCanSetProfileOwnerLocked(ComponentName owner, int userHandle, boolean hasIncompatibleAccountsOrNonAdb) {
        UserInfo info = getUserInfo(userHandle);
        if (info == null) {
            throw new IllegalArgumentException("Attempted to set profile owner for invalid userId: " + userHandle);
        } else if (info.isGuest()) {
            throw new IllegalStateException("Cannot set a profile owner on a guest");
        } else if (this.mOwners.hasProfileOwner(userHandle)) {
            throw new IllegalStateException("Trying to set the profile owner, but profile owner is already set.");
        } else if (this.mOwners.hasDeviceOwner() && this.mOwners.getDeviceOwnerUserId() == userHandle) {
            throw new IllegalStateException("Trying to set the profile owner, but the user already has a device owner.");
        } else if (!isAdb()) {
            enforceCanManageProfileAndDeviceOwners();
            if ((this.mIsWatch || hasUserSetupCompleted(userHandle)) && (isCallerWithSystemUid() ^ 1) != 0) {
                throw new IllegalStateException("Cannot set the profile owner on a user which is already set-up");
            }
        } else if ((this.mIsWatch || hasUserSetupCompleted(userHandle)) && hasIncompatibleAccountsOrNonAdb) {
            throw new IllegalStateException("Not allowed to set the profile owner because there are already some accounts on the profile");
        }
    }

    private void enforceCanSetDeviceOwnerLocked(ComponentName owner, int userId, boolean hasIncompatibleAccountsOrNonAdb) {
        if (!isAdb()) {
            enforceCanManageProfileAndDeviceOwners();
        }
        int code = checkDeviceOwnerProvisioningPreConditionLocked(owner, userId, isAdb(), hasIncompatibleAccountsOrNonAdb);
        switch (code) {
            case 0:
                return;
            case 1:
                throw new IllegalStateException("Trying to set the device owner, but device owner is already set.");
            case 2:
                throw new IllegalStateException("Trying to set the device owner, but the user already has a profile owner.");
            case 3:
                throw new IllegalStateException("User not running: " + userId);
            case 4:
                throw new IllegalStateException("Cannot set the device owner if the device is already set-up");
            case 5:
                throw new IllegalStateException("Not allowed to set the device owner because there are already several users on the device");
            case 6:
                throw new IllegalStateException("Not allowed to set the device owner because there are already some accounts on the device");
            case 7:
                throw new IllegalStateException("User is not system user");
            case 8:
                throw new IllegalStateException("Not allowed to set the device owner because this device has already paired");
            default:
                throw new IllegalStateException("Unexpected @ProvisioningPreCondition " + code);
        }
    }

    private void enforceUserUnlocked(int userId) {
        Preconditions.checkState(this.mUserManager.isUserUnlocked(userId), "User must be running and unlocked");
    }

    private void enforceUserUnlocked(int userId, boolean parent) {
        if (parent) {
            enforceUserUnlocked(getProfileParentId(userId));
        } else {
            enforceUserUnlocked(userId);
        }
    }

    private void enforceManageUsers() {
        Object obj = 1;
        int callingUid = this.mInjector.binderGetCallingUid();
        if (!(isCallerWithSystemUid() || callingUid == 0)) {
            obj = null;
        }
        if (obj == null) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_USERS", null);
        }
    }

    public void enforceFullCrossUsersPermission(int userHandle) {
        enforceSystemUserOrPermissionIfCrossUser(userHandle, "android.permission.INTERACT_ACROSS_USERS_FULL");
    }

    private void enforceCrossUsersPermission(int userHandle) {
        enforceSystemUserOrPermissionIfCrossUser(userHandle, "android.permission.INTERACT_ACROSS_USERS");
    }

    private void enforceSystemUserOrPermission(String permission) {
        Object obj = 1;
        if (!(isCallerWithSystemUid() || this.mInjector.binderGetCallingUid() == 0)) {
            obj = null;
        }
        if (obj == null) {
            this.mContext.enforceCallingOrSelfPermission(permission, "Must be system or have " + permission + " permission");
        }
    }

    private void enforceSystemUserOrPermissionIfCrossUser(int userHandle, String permission) {
        if (userHandle < 0) {
            throw new IllegalArgumentException("Invalid userId " + userHandle);
        } else if (userHandle != this.mInjector.userHandleGetCallingUserId()) {
            enforceSystemUserOrPermission(permission);
        }
    }

    private void enforceManagedProfile(int userHandle, String message) {
        if (!isManagedProfile(userHandle)) {
            throw new SecurityException("You can not " + message + " outside a managed profile.");
        }
    }

    private void enforceNotManagedProfile(int userHandle, String message) {
        if (isManagedProfile(userHandle)) {
            throw new SecurityException("You can not " + message + " for a managed profile.");
        }
    }

    private void enforceDeviceOwnerOrManageUsers() {
        synchronized (this) {
            if (getActiveAdminWithPolicyForUidLocked(null, -2, this.mInjector.binderGetCallingUid()) != null) {
                return;
            }
            enforceManageUsers();
        }
    }

    private void enforceProfileOwnerOrSystemUser() {
        synchronized (this) {
            if (getActiveAdminWithPolicyForUidLocked(null, -1, this.mInjector.binderGetCallingUid()) != null) {
                return;
            }
            Preconditions.checkState(isCallerWithSystemUid(), "Only profile owner, device owner and system may call this method.");
        }
    }

    private void enforceProfileOwnerOrFullCrossUsersPermission(int userId) {
        if (userId == this.mInjector.userHandleGetCallingUserId()) {
            synchronized (this) {
                if (getActiveAdminWithPolicyForUidLocked(null, -1, this.mInjector.binderGetCallingUid()) != null) {
                    return;
                }
            }
        }
        enforceSystemUserOrPermission("android.permission.INTERACT_ACROSS_USERS_FULL");
    }

    private void ensureCallerPackage(String packageName) {
        boolean z = false;
        if (packageName == null) {
            Preconditions.checkState(isCallerWithSystemUid(), "Only caller can omit package name");
            return;
        }
        try {
            if (this.mIPackageManager.getApplicationInfo(packageName, 0, this.mInjector.userHandleGetCallingUserId()).uid == this.mInjector.binderGetCallingUid()) {
                z = true;
            }
            Preconditions.checkState(z, "Unmatching package name");
        } catch (RemoteException e) {
        }
    }

    private boolean isCallerWithSystemUid() {
        return UserHandle.isSameApp(this.mInjector.binderGetCallingUid(), 1000);
    }

    protected int getProfileParentId(int userHandle) {
        long ident = this.mInjector.binderClearCallingIdentity();
        try {
            UserInfo parentUser = this.mUserManager.getProfileParent(userHandle);
            if (parentUser != null) {
                userHandle = parentUser.id;
            }
            this.mInjector.binderRestoreCallingIdentity(ident);
            return userHandle;
        } catch (Throwable th) {
            this.mInjector.binderRestoreCallingIdentity(ident);
        }
    }

    private int getCredentialOwner(int userHandle, boolean parent) {
        long ident = this.mInjector.binderClearCallingIdentity();
        if (parent) {
            try {
                UserInfo parentProfile = this.mUserManager.getProfileParent(userHandle);
                if (parentProfile != null) {
                    userHandle = parentProfile.id;
                }
            } catch (Throwable th) {
                this.mInjector.binderRestoreCallingIdentity(ident);
            }
        }
        int credentialOwnerProfile = this.mUserManager.getCredentialOwnerProfile(userHandle);
        this.mInjector.binderRestoreCallingIdentity(ident);
        return credentialOwnerProfile;
    }

    private boolean isManagedProfile(int userHandle) {
        UserInfo user = getUserInfo(userHandle);
        return user != null ? user.isManagedProfile() : false;
    }

    private void enableIfNecessary(String packageName, int userId) {
        try {
            if (this.mIPackageManager.getApplicationInfo(packageName, 32768, userId).enabledSetting == 4) {
                this.mIPackageManager.setApplicationEnabledSetting(packageName, 0, 1, userId, LOG_TAG);
            }
        } catch (RemoteException e) {
        }
    }

    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (DumpUtils.checkDumpPermission(this.mContext, LOG_TAG, pw)) {
            synchronized (this) {
                pw.println("Current Device Policy Manager state:");
                this.mOwners.dump("  ", pw);
                this.mDeviceAdminServiceController.dump("  ", pw);
                int userCount = this.mUserData.size();
                for (int u = 0; u < userCount; u++) {
                    DevicePolicyData policy = getUserData(this.mUserData.keyAt(u));
                    pw.println();
                    pw.println("  Enabled Device Admins (User " + policy.mUserHandle + ", provisioningState: " + policy.mUserProvisioningState + "):");
                    pw.println(" ");
                    if (mHwCustDevicePolicyManagerService != null && mHwCustDevicePolicyManagerService.isForbiddenSimplePwdFeatureEnable()) {
                        pw.print("  mIsCurrentPwdSimple=");
                        pw.println(policy.mIsCurrentPwdSimple);
                    }
                    pw.print("    mPasswordOwner=");
                    pw.println(policy.mPasswordOwner);
                }
                pw.println();
                this.mConstants.dump("  ", pw);
                pw.println();
                pw.println("  Encryption Status: " + getEncryptionStatusName(getEncryptionStatus()));
            }
        }
    }

    private String getEncryptionStatusName(int encryptionStatus) {
        switch (encryptionStatus) {
            case 0:
                return "unsupported";
            case 1:
                return "inactive";
            case 2:
                return "activating";
            case 3:
                return "block";
            case 4:
                return "block default key";
            case 5:
                return "per-user";
            default:
                return Shell.NIGHT_MODE_STR_UNKNOWN;
        }
    }

    public void addPersistentPreferredActivity(ComponentName who, IntentFilter filter, ComponentName activity) {
        Preconditions.checkNotNull(who, "ComponentName is null");
        int userHandle = UserHandle.getCallingUserId();
        synchronized (this) {
            getActiveAdminForCallerLocked(who, -1);
            long id = this.mInjector.binderClearCallingIdentity();
            try {
                this.mIPackageManager.addPersistentPreferredActivity(filter, activity, userHandle);
                this.mIPackageManager.flushPackageRestrictionsAsUser(userHandle);
                this.mInjector.binderRestoreCallingIdentity(id);
            } catch (RemoteException e) {
                this.mInjector.binderRestoreCallingIdentity(id);
            } catch (Throwable th) {
                this.mInjector.binderRestoreCallingIdentity(id);
            }
        }
        return;
    }

    public void clearPackagePersistentPreferredActivities(ComponentName who, String packageName) {
        Preconditions.checkNotNull(who, "ComponentName is null");
        int userHandle = UserHandle.getCallingUserId();
        synchronized (this) {
            getActiveAdminForCallerLocked(who, -1);
            long id = this.mInjector.binderClearCallingIdentity();
            try {
                this.mIPackageManager.clearPackagePersistentPreferredActivities(packageName, userHandle);
                this.mIPackageManager.flushPackageRestrictionsAsUser(userHandle);
                this.mInjector.binderRestoreCallingIdentity(id);
            } catch (RemoteException e) {
                this.mInjector.binderRestoreCallingIdentity(id);
            } catch (Throwable th) {
                this.mInjector.binderRestoreCallingIdentity(id);
            }
        }
        return;
    }

    public boolean setApplicationRestrictionsManagingPackage(ComponentName admin, String packageName) {
        try {
            setDelegatedScopePreO(admin, packageName, "delegation-app-restrictions");
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public String getApplicationRestrictionsManagingPackage(ComponentName admin) {
        List<String> delegatePackages = getDelegatePackages(admin, "delegation-app-restrictions");
        return delegatePackages.size() > 0 ? (String) delegatePackages.get(0) : null;
    }

    public boolean isCallerApplicationRestrictionsManagingPackage(String callerPackage) {
        return isCallerDelegate(callerPackage, "delegation-app-restrictions");
    }

    public void setApplicationRestrictions(ComponentName who, String callerPackage, String packageName, Bundle settings) {
        enforceCanManageScope(who, callerPackage, -1, "delegation-app-restrictions");
        UserHandle userHandle = this.mInjector.binderGetCallingUserHandle();
        long id = this.mInjector.binderClearCallingIdentity();
        try {
            this.mUserManager.setApplicationRestrictions(packageName, settings, userHandle);
        } finally {
            this.mInjector.binderRestoreCallingIdentity(id);
        }
    }

    public void setTrustAgentConfiguration(ComponentName admin, ComponentName agent, PersistableBundle args, boolean parent) {
        if (this.mHasFeature) {
            Preconditions.checkNotNull(admin, "admin is null");
            Preconditions.checkNotNull(agent, "agent is null");
            int userHandle = UserHandle.getCallingUserId();
            synchronized (this) {
                getActiveAdminForCallerLocked(admin, 9, parent).trustAgentInfos.put(agent.flattenToString(), new TrustAgentInfo(args));
                saveSettingsLocked(userHandle);
            }
        }
    }

    /* JADX WARNING: Missing block: B:53:0x00cd, code:
            return r13;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public List<PersistableBundle> getTrustAgentConfiguration(ComponentName admin, ComponentName agent, int userHandle, boolean parent) {
        if (!this.mHasFeature) {
            return null;
        }
        Preconditions.checkNotNull(agent, "agent null");
        enforceFullCrossUsersPermission(userHandle);
        synchronized (this) {
            String componentName = agent.flattenToString();
            List<PersistableBundle> result;
            if (admin != null) {
                ActiveAdmin ap = getActiveAdminUncheckedLocked(admin, userHandle, parent);
                if (ap == null) {
                    return null;
                }
                TrustAgentInfo trustAgentInfo = (TrustAgentInfo) ap.trustAgentInfos.get(componentName);
                if (trustAgentInfo == null || trustAgentInfo.options == null) {
                    return null;
                }
                result = new ArrayList();
                result.add(trustAgentInfo.options);
                return result;
            }
            result = null;
            List<ActiveAdmin> admins = getActiveAdminsForLockscreenPoliciesLocked(userHandle, parent);
            boolean allAdminsHaveOptions = true;
            int N = admins.size();
            for (int i = 0; i < N; i++) {
                ActiveAdmin active = (ActiveAdmin) admins.get(i);
                boolean disablesTrust = (active.disabledKeyguardFeatures & 16) != 0;
                TrustAgentInfo info = (TrustAgentInfo) active.trustAgentInfos.get(componentName);
                if (info == null || info.options == null || (info.options.isEmpty() ^ 1) == 0) {
                    if (disablesTrust) {
                        allAdminsHaveOptions = false;
                        break;
                    }
                } else if (disablesTrust) {
                    if (result == null) {
                        result = new ArrayList();
                    }
                    result.add(info.options);
                } else {
                    Log.w(LOG_TAG, "Ignoring admin " + active.info + " because it has trust options but doesn't declare " + "KEYGUARD_DISABLE_TRUST_AGENTS");
                }
            }
            if (!allAdminsHaveOptions) {
                result = null;
            }
        }
    }

    public void setRestrictionsProvider(ComponentName who, ComponentName permissionProvider) {
        Preconditions.checkNotNull(who, "ComponentName is null");
        synchronized (this) {
            getActiveAdminForCallerLocked(who, -1);
            int userHandle = UserHandle.getCallingUserId();
            getUserData(userHandle).mRestrictionsProvider = permissionProvider;
            saveSettingsLocked(userHandle);
        }
    }

    public ComponentName getRestrictionsProvider(int userHandle) {
        ComponentName componentName = null;
        synchronized (this) {
            if (isCallerWithSystemUid()) {
                DevicePolicyData userData = getUserData(userHandle);
                if (userData != null) {
                    componentName = userData.mRestrictionsProvider;
                }
            } else {
                throw new SecurityException("Only the system can query the permission provider");
            }
        }
        return componentName;
    }

    public void addCrossProfileIntentFilter(ComponentName who, IntentFilter filter, int flags) {
        Preconditions.checkNotNull(who, "ComponentName is null");
        int callingUserId = UserHandle.getCallingUserId();
        synchronized (this) {
            getActiveAdminForCallerLocked(who, -1);
            long id = this.mInjector.binderClearCallingIdentity();
            try {
                UserInfo parent = this.mUserManager.getProfileParent(callingUserId);
                if (parent == null) {
                    Slog.e(LOG_TAG, "Cannot call addCrossProfileIntentFilter if there is no parent");
                    this.mInjector.binderRestoreCallingIdentity(id);
                    return;
                }
                if ((flags & 1) != 0) {
                    this.mIPackageManager.addCrossProfileIntentFilter(filter, who.getPackageName(), callingUserId, parent.id, 0);
                }
                if ((flags & 2) != 0) {
                    this.mIPackageManager.addCrossProfileIntentFilter(filter, who.getPackageName(), parent.id, callingUserId, 0);
                }
                this.mInjector.binderRestoreCallingIdentity(id);
            } catch (RemoteException e) {
                this.mInjector.binderRestoreCallingIdentity(id);
            } catch (Throwable th) {
                this.mInjector.binderRestoreCallingIdentity(id);
            }
        }
    }

    public void clearCrossProfileIntentFilters(ComponentName who) {
        Preconditions.checkNotNull(who, "ComponentName is null");
        int callingUserId = UserHandle.getCallingUserId();
        synchronized (this) {
            getActiveAdminForCallerLocked(who, -1);
            long id = this.mInjector.binderClearCallingIdentity();
            try {
                UserInfo parent = this.mUserManager.getProfileParent(callingUserId);
                if (parent == null) {
                    Slog.e(LOG_TAG, "Cannot call clearCrossProfileIntentFilter if there is no parent");
                    this.mInjector.binderRestoreCallingIdentity(id);
                    return;
                }
                this.mIPackageManager.clearCrossProfileIntentFilters(callingUserId, who.getPackageName());
                this.mIPackageManager.clearCrossProfileIntentFilters(parent.id, who.getPackageName());
                this.mInjector.binderRestoreCallingIdentity(id);
            } catch (RemoteException e) {
                this.mInjector.binderRestoreCallingIdentity(id);
            } catch (Throwable th) {
                this.mInjector.binderRestoreCallingIdentity(id);
            }
        }
    }

    private boolean checkPackagesInPermittedListOrSystem(List<String> enabledPackages, List<String> permittedList, int userIdToCheck) {
        long id = this.mInjector.binderClearCallingIdentity();
        try {
            UserInfo user = getUserInfo(userIdToCheck);
            if (user.isManagedProfile()) {
                userIdToCheck = user.profileGroupId;
            }
            for (String enabledPackage : enabledPackages) {
                boolean systemService = false;
                systemService = (this.mIPackageManager.getApplicationInfo(enabledPackage, 8192, userIdToCheck).flags & 1) != 0;
                if (!systemService) {
                    if ((permittedList.contains(enabledPackage) ^ 1) != 0) {
                        this.mInjector.binderRestoreCallingIdentity(id);
                        return false;
                    }
                }
            }
            this.mInjector.binderRestoreCallingIdentity(id);
            return true;
        } catch (RemoteException e) {
            Log.i(LOG_TAG, "Can't talk to package managed", e);
        } catch (Throwable th) {
            this.mInjector.binderRestoreCallingIdentity(id);
        }
    }

    private AccessibilityManager getAccessibilityManagerForUser(int userId) {
        IBinder iBinder = ServiceManager.getService("accessibility");
        return new AccessibilityManager(this.mContext, iBinder == null ? null : IAccessibilityManager.Stub.asInterface(iBinder), userId);
    }

    public boolean setPermittedAccessibilityServices(ComponentName who, List packageList) {
        if (!this.mHasFeature) {
            return false;
        }
        Preconditions.checkNotNull(who, "ComponentName is null");
        if (packageList != null) {
            int userId = UserHandle.getCallingUserId();
            List<AccessibilityServiceInfo> enabledServices = null;
            long id = this.mInjector.binderClearCallingIdentity();
            try {
                UserInfo user = getUserInfo(userId);
                if (user.isManagedProfile()) {
                    userId = user.profileGroupId;
                }
                enabledServices = getAccessibilityManagerForUser(userId).getEnabledAccessibilityServiceList(-1);
                if (enabledServices != null) {
                    List<String> enabledPackages = new ArrayList();
                    for (AccessibilityServiceInfo service : enabledServices) {
                        enabledPackages.add(service.getResolveInfo().serviceInfo.packageName);
                    }
                    if (!checkPackagesInPermittedListOrSystem(enabledPackages, packageList, userId)) {
                        Slog.e(LOG_TAG, "Cannot set permitted accessibility services, because it contains already enabled accesibility services.");
                        return false;
                    }
                }
            } finally {
                this.mInjector.binderRestoreCallingIdentity(id);
            }
        }
        synchronized (this) {
            getActiveAdminForCallerLocked(who, -1).permittedAccessiblityServices = packageList;
            saveSettingsLocked(UserHandle.getCallingUserId());
        }
        return true;
    }

    public List getPermittedAccessibilityServices(ComponentName who) {
        if (!this.mHasFeature) {
            return null;
        }
        List list;
        Preconditions.checkNotNull(who, "ComponentName is null");
        synchronized (this) {
            list = getActiveAdminForCallerLocked(who, -1).permittedAccessiblityServices;
        }
        return list;
    }

    /* JADX WARNING: Missing block: B:48:0x00d2, code:
            return r14;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public List getPermittedAccessibilityServicesForUser(int userId) {
        Throwable th;
        if (!this.mHasFeature) {
            return null;
        }
        synchronized (this) {
            List<String> result = null;
            long id;
            try {
                int[] profileIds = this.mUserManager.getProfileIdsWithDisabled(userId);
                int i = 0;
                int length = profileIds.length;
                while (i < length) {
                    DevicePolicyData policy = getUserDataUnchecked(profileIds[i]);
                    int N = policy.mAdminList.size();
                    int j = 0;
                    List<String> result2 = result;
                    while (j < N) {
                        try {
                            List<String> fromAdmin = ((ActiveAdmin) policy.mAdminList.get(j)).permittedAccessiblityServices;
                            if (fromAdmin == null) {
                                result = result2;
                            } else if (result2 == null) {
                                result = new ArrayList(fromAdmin);
                            } else {
                                result2.retainAll(fromAdmin);
                                result = result2;
                            }
                            j++;
                            result2 = result;
                        } catch (Throwable th2) {
                            th = th2;
                            result = result2;
                            throw th;
                        }
                    }
                    i++;
                    result = result2;
                }
                if (result != null) {
                    id = this.mInjector.binderClearCallingIdentity();
                    UserInfo user = getUserInfo(userId);
                    if (user.isManagedProfile()) {
                        userId = user.profileGroupId;
                    }
                    List<AccessibilityServiceInfo> installedServices = getAccessibilityManagerForUser(userId).getInstalledAccessibilityServiceList();
                    if (installedServices != null) {
                        for (AccessibilityServiceInfo service : installedServices) {
                            ServiceInfo serviceInfo = service.getResolveInfo().serviceInfo;
                            if ((serviceInfo.applicationInfo.flags & 1) != 0) {
                                result.add(serviceInfo.packageName);
                            }
                        }
                    }
                    this.mInjector.binderRestoreCallingIdentity(id);
                }
            } catch (Throwable th3) {
                th = th3;
            }
        }
    }

    public boolean isAccessibilityServicePermittedByAdmin(ComponentName who, String packageName, int userHandle) {
        if (!this.mHasFeature) {
            return true;
        }
        Preconditions.checkNotNull(who, "ComponentName is null");
        Preconditions.checkStringNotEmpty(packageName, "packageName is null");
        if (isCallerWithSystemUid()) {
            synchronized (this) {
                ActiveAdmin admin = getActiveAdminUncheckedLocked(who, userHandle);
                if (admin == null) {
                    return false;
                } else if (admin.permittedAccessiblityServices == null) {
                    return true;
                } else {
                    boolean checkPackagesInPermittedListOrSystem = checkPackagesInPermittedListOrSystem(Collections.singletonList(packageName), admin.permittedAccessiblityServices, userHandle);
                    return checkPackagesInPermittedListOrSystem;
                }
            }
        }
        throw new SecurityException("Only the system can query if an accessibility service is disabled by admin");
    }

    private boolean checkCallerIsCurrentUserOrProfile() {
        int callingUserId = UserHandle.getCallingUserId();
        long token = this.mInjector.binderClearCallingIdentity();
        try {
            UserInfo callingUser = getUserInfo(callingUserId);
            UserInfo currentUser = this.mInjector.getIActivityManager().getCurrentUser();
            if (callingUser.isManagedProfile() && callingUser.profileGroupId != currentUser.id) {
                Slog.e(LOG_TAG, "Cannot set permitted input methods for managed profile of a user that isn't the foreground user.");
                return false;
            } else if (callingUser.isManagedProfile() || callingUserId == currentUser.id) {
                this.mInjector.binderRestoreCallingIdentity(token);
                return true;
            } else {
                Slog.e(LOG_TAG, "Cannot set permitted input methods of a user that isn't the foreground user.");
                this.mInjector.binderRestoreCallingIdentity(token);
                return false;
            }
        } catch (RemoteException e) {
            Slog.e(LOG_TAG, "Failed to talk to activity managed.", e);
            return false;
        } finally {
            this.mInjector.binderRestoreCallingIdentity(token);
        }
    }

    public boolean setPermittedInputMethods(ComponentName who, List packageList) {
        if (!this.mHasFeature) {
            return false;
        }
        Preconditions.checkNotNull(who, "ComponentName is null");
        if (!checkCallerIsCurrentUserOrProfile()) {
            return false;
        }
        int callingUserId = this.mInjector.userHandleGetCallingUserId();
        if (packageList != null) {
            List<InputMethodInfo> enabledImes = ((InputMethodManager) this.mContext.getSystemService(InputMethodManager.class)).getEnabledInputMethodList();
            if (enabledImes != null) {
                List<String> enabledPackages = new ArrayList();
                for (InputMethodInfo ime : enabledImes) {
                    enabledPackages.add(ime.getPackageName());
                }
                if (!checkPackagesInPermittedListOrSystem(enabledPackages, packageList, callingUserId)) {
                    Slog.e(LOG_TAG, "Cannot set permitted input methods, because it contains already enabled input method.");
                    return false;
                }
            }
        }
        synchronized (this) {
            getActiveAdminForCallerLocked(who, -1).permittedInputMethods = packageList;
            saveSettingsLocked(callingUserId);
        }
        return true;
    }

    public List getPermittedInputMethods(ComponentName who) {
        if (!this.mHasFeature) {
            return null;
        }
        List list;
        Preconditions.checkNotNull(who, "ComponentName is null");
        synchronized (this) {
            list = getActiveAdminForCallerLocked(who, -1).permittedInputMethods;
        }
        return list;
    }

    /* JADX WARNING: Missing block: B:47:0x00ed, code:
            return r18;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public List getPermittedInputMethodsForCurrentUser() {
        try {
            Throwable th;
            int userId = this.mInjector.getIActivityManager().getCurrentUser().id;
            synchronized (this) {
                List<String> result = null;
                long id;
                try {
                    int[] profileIds = this.mUserManager.getProfileIdsWithDisabled(userId);
                    int i = 0;
                    int length = profileIds.length;
                    while (i < length) {
                        DevicePolicyData policy = getUserDataUnchecked(profileIds[i]);
                        int N = policy.mAdminList.size();
                        int j = 0;
                        List<String> result2 = result;
                        while (j < N) {
                            try {
                                List<String> fromAdmin = ((ActiveAdmin) policy.mAdminList.get(j)).permittedInputMethods;
                                if (fromAdmin == null) {
                                    result = result2;
                                } else if (result2 == null) {
                                    List<String> arrayList = new ArrayList(fromAdmin);
                                } else {
                                    result2.retainAll(fromAdmin);
                                    result = result2;
                                }
                                j++;
                                result2 = result;
                            } catch (Throwable th2) {
                                th = th2;
                                result = result2;
                            }
                        }
                        i++;
                        result = result2;
                    }
                    if (result != null) {
                        List<InputMethodInfo> imes = ((InputMethodManager) this.mContext.getSystemService(InputMethodManager.class)).getInputMethodList();
                        id = this.mInjector.binderClearCallingIdentity();
                        if (imes != null) {
                            for (InputMethodInfo ime : imes) {
                                ServiceInfo serviceInfo = ime.getServiceInfo();
                                if ((serviceInfo.applicationInfo.flags & 1) != 0) {
                                    result.add(serviceInfo.packageName);
                                }
                            }
                        }
                        this.mInjector.binderRestoreCallingIdentity(id);
                    }
                } catch (Throwable th3) {
                    th = th3;
                }
            }
            throw th;
        } catch (RemoteException e) {
            Slog.e(LOG_TAG, "Failed to make remote calls to get current user", e);
            return null;
        }
    }

    public boolean isInputMethodPermittedByAdmin(ComponentName who, String packageName, int userHandle) {
        if (!this.mHasFeature) {
            return true;
        }
        Preconditions.checkNotNull(who, "ComponentName is null");
        Preconditions.checkStringNotEmpty(packageName, "packageName is null");
        if (isCallerWithSystemUid()) {
            synchronized (this) {
                ActiveAdmin admin = getActiveAdminUncheckedLocked(who, userHandle);
                if (admin == null) {
                    return false;
                } else if (admin.permittedInputMethods == null) {
                    return true;
                } else {
                    boolean checkPackagesInPermittedListOrSystem = checkPackagesInPermittedListOrSystem(Collections.singletonList(packageName), admin.permittedInputMethods, userHandle);
                    return checkPackagesInPermittedListOrSystem;
                }
            }
        }
        throw new SecurityException("Only the system can query if an input method is disabled by admin");
    }

    public boolean setPermittedCrossProfileNotificationListeners(ComponentName who, List<String> packageList) {
        if (!this.mHasFeature) {
            return false;
        }
        Preconditions.checkNotNull(who, "ComponentName is null");
        int callingUserId = this.mInjector.userHandleGetCallingUserId();
        if (!isManagedProfile(callingUserId)) {
            return false;
        }
        synchronized (this) {
            getActiveAdminForCallerLocked(who, -1).permittedNotificationListeners = packageList;
            saveSettingsLocked(callingUserId);
        }
        return true;
    }

    public List<String> getPermittedCrossProfileNotificationListeners(ComponentName who) {
        if (!this.mHasFeature) {
            return null;
        }
        List<String> list;
        Preconditions.checkNotNull(who, "ComponentName is null");
        synchronized (this) {
            list = getActiveAdminForCallerLocked(who, -1).permittedNotificationListeners;
        }
        return list;
    }

    /* JADX WARNING: Missing block: B:14:0x0027, code:
            return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isNotificationListenerServicePermitted(String packageName, int userId) {
        if (!this.mHasFeature) {
            return true;
        }
        Preconditions.checkStringNotEmpty(packageName, "packageName is null or empty");
        if (isCallerWithSystemUid()) {
            synchronized (this) {
                ActiveAdmin profileOwner = getProfileOwnerAdminLocked(userId);
                if (profileOwner == null || profileOwner.permittedNotificationListeners == null) {
                } else {
                    boolean checkPackagesInPermittedListOrSystem = checkPackagesInPermittedListOrSystem(Collections.singletonList(packageName), profileOwner.permittedNotificationListeners, userId);
                    return checkPackagesInPermittedListOrSystem;
                }
            }
        }
        throw new SecurityException("Only the system can query if a notification listener service is permitted");
    }

    private void sendAdminEnabledBroadcastLocked(int userHandle) {
        DevicePolicyData policyData = getUserData(userHandle);
        if (policyData.mAdminBroadcastPending) {
            ActiveAdmin admin = getProfileOwnerAdminLocked(userHandle);
            if (admin != null) {
                PersistableBundle initBundle = policyData.mInitBundle;
                sendAdminCommandLocked(admin, "android.app.action.DEVICE_ADMIN_ENABLED", initBundle == null ? null : new Bundle(initBundle), null);
            }
            policyData.mInitBundle = null;
            policyData.mAdminBroadcastPending = false;
            saveSettingsLocked(userHandle);
        }
    }

    public UserHandle createAndManageUser(ComponentName admin, String name, ComponentName profileOwner, PersistableBundle adminExtras, int flags) {
        Preconditions.checkNotNull(admin, "admin is null");
        Preconditions.checkNotNull(profileOwner, "profileOwner is null");
        if (!admin.getPackageName().equals(profileOwner.getPackageName())) {
            throw new IllegalArgumentException("profileOwner " + profileOwner + " and admin " + admin + " are not in the same package");
        } else if (!this.mInjector.binderGetCallingUserHandle().isSystem()) {
            throw new SecurityException("createAndManageUser was called from non-system user");
        } else if (this.mInjector.userManagerIsSplitSystemUser() || (flags & 2) == 0) {
            long id;
            UserHandle user = null;
            synchronized (this) {
                getActiveAdminForCallerLocked(admin, -2);
                id = this.mInjector.binderClearCallingIdentity();
                int userInfoFlags = 0;
                if ((flags & 2) != 0) {
                    userInfoFlags = 256;
                }
                try {
                    UserInfo userInfo = this.mUserManagerInternal.createUserEvenWhenDisallowed(name, userInfoFlags);
                    if (userInfo != null) {
                        user = userInfo.getUserHandle();
                    }
                    this.mInjector.binderRestoreCallingIdentity(id);
                } catch (Throwable th) {
                    this.mInjector.binderRestoreCallingIdentity(id);
                }
            }
            if (user == null) {
                return null;
            }
            id = this.mInjector.binderClearCallingIdentity();
            try {
                String adminPkg = admin.getPackageName();
                int userHandle = user.getIdentifier();
                if (!this.mIPackageManager.isPackageAvailable(adminPkg, userHandle)) {
                    this.mIPackageManager.installExistingPackageAsUser(adminPkg, userHandle, 0, 1);
                }
                setActiveAdmin(profileOwner, true, userHandle);
                synchronized (this) {
                    DevicePolicyData policyData = getUserData(userHandle);
                    policyData.mInitBundle = adminExtras;
                    policyData.mAdminBroadcastPending = true;
                    saveSettingsLocked(userHandle);
                }
                setProfileOwner(profileOwner, getProfileOwnerName(Process.myUserHandle().getIdentifier()), userHandle);
                if ((flags & 1) != 0) {
                    Secure.putIntForUser(this.mContext.getContentResolver(), "user_setup_complete", 1, userHandle);
                }
                this.mInjector.binderRestoreCallingIdentity(id);
                return user;
            } catch (RemoteException e) {
                Slog.e(LOG_TAG, "Failed to make remote calls for createAndManageUser, removing created user", e);
                this.mUserManager.removeUser(user.getIdentifier());
                this.mInjector.binderRestoreCallingIdentity(id);
                return null;
            } catch (Throwable th2) {
                this.mInjector.binderRestoreCallingIdentity(id);
            }
        } else {
            throw new IllegalArgumentException("Ephemeral users are only supported on systems with a split system user.");
        }
    }

    public boolean removeUser(ComponentName who, UserHandle userHandle) {
        Preconditions.checkNotNull(who, "ComponentName is null");
        synchronized (this) {
            getActiveAdminForCallerLocked(who, -2);
        }
        int callingUserId = this.mInjector.userHandleGetCallingUserId();
        long id = this.mInjector.binderClearCallingIdentity();
        try {
            String restriction;
            if (isManagedProfile(userHandle.getIdentifier())) {
                restriction = "no_remove_managed_profile";
            } else {
                restriction = "no_remove_user";
            }
            if (isAdminAffectedByRestriction(who, restriction, callingUserId)) {
                Log.w(LOG_TAG, "The device owner cannot remove a user because " + restriction + " is enabled, and was not set by the device owner");
                return false;
            }
            boolean removeUserEvenWhenDisallowed = this.mUserManagerInternal.removeUserEvenWhenDisallowed(userHandle.getIdentifier());
            this.mInjector.binderRestoreCallingIdentity(id);
            return removeUserEvenWhenDisallowed;
        } finally {
            this.mInjector.binderRestoreCallingIdentity(id);
        }
    }

    private boolean isAdminAffectedByRestriction(ComponentName admin, String userRestriction, int userId) {
        switch (this.mUserManager.getUserRestrictionSource(userRestriction, UserHandle.of(userId))) {
            case 0:
                return false;
            case 2:
                return isDeviceOwner(admin, userId) ^ 1;
            case 4:
                return isProfileOwner(admin, userId) ^ 1;
            default:
                return true;
        }
    }

    public boolean switchUser(ComponentName who, UserHandle userHandle) {
        boolean switchUser;
        Preconditions.checkNotNull(who, "ComponentName is null");
        synchronized (this) {
            getActiveAdminForCallerLocked(who, -2);
            long id = this.mInjector.binderClearCallingIdentity();
            int userId = 0;
            if (userHandle != null) {
                try {
                    userId = userHandle.getIdentifier();
                } catch (RemoteException e) {
                    Log.e(LOG_TAG, "Couldn't switch user", e);
                    this.mInjector.binderRestoreCallingIdentity(id);
                    return false;
                } catch (Throwable th) {
                    this.mInjector.binderRestoreCallingIdentity(id);
                }
            }
            switchUser = this.mInjector.getIActivityManager().switchUser(userId);
            this.mInjector.binderRestoreCallingIdentity(id);
        }
        return switchUser;
    }

    public Bundle getApplicationRestrictions(ComponentName who, String callerPackage, String packageName) {
        enforceCanManageScope(who, callerPackage, -1, "delegation-app-restrictions");
        UserHandle userHandle = this.mInjector.binderGetCallingUserHandle();
        long id = this.mInjector.binderClearCallingIdentity();
        try {
            Bundle bundle = this.mUserManager.getApplicationRestrictions(packageName, userHandle);
            if (bundle == null) {
                bundle = Bundle.EMPTY;
            }
            this.mInjector.binderRestoreCallingIdentity(id);
            return bundle;
        } catch (Throwable th) {
            this.mInjector.binderRestoreCallingIdentity(id);
        }
    }

    public String[] setPackagesSuspended(ComponentName who, String callerPackage, String[] packageNames, boolean suspended) {
        String[] packagesSuspendedAsUser;
        int callingUserId = UserHandle.getCallingUserId();
        synchronized (this) {
            enforceCanManageScope(who, callerPackage, -1, "delegation-package-access");
            long id = this.mInjector.binderClearCallingIdentity();
            try {
                packagesSuspendedAsUser = this.mIPackageManager.setPackagesSuspendedAsUser(packageNames, suspended, callingUserId);
                this.mInjector.binderRestoreCallingIdentity(id);
            } catch (RemoteException re) {
                Slog.e(LOG_TAG, "Failed talking to the package manager", re);
                this.mInjector.binderRestoreCallingIdentity(id);
                return packageNames;
            } catch (Throwable th) {
                this.mInjector.binderRestoreCallingIdentity(id);
            }
        }
        return packagesSuspendedAsUser;
    }

    public boolean isPackageSuspended(ComponentName who, String callerPackage, String packageName) {
        boolean isPackageSuspendedForUser;
        int callingUserId = UserHandle.getCallingUserId();
        synchronized (this) {
            enforceCanManageScope(who, callerPackage, -1, "delegation-package-access");
            long id = this.mInjector.binderClearCallingIdentity();
            try {
                isPackageSuspendedForUser = this.mIPackageManager.isPackageSuspendedForUser(packageName, callingUserId);
                this.mInjector.binderRestoreCallingIdentity(id);
            } catch (RemoteException re) {
                Slog.e(LOG_TAG, "Failed talking to the package manager", re);
                this.mInjector.binderRestoreCallingIdentity(id);
                return false;
            } catch (Throwable th) {
                this.mInjector.binderRestoreCallingIdentity(id);
            }
        }
        return isPackageSuspendedForUser;
    }

    public void setUserRestriction(ComponentName who, String key, boolean enabledFromThisOwner) {
        Preconditions.checkNotNull(who, "ComponentName is null");
        if (UserRestrictionsUtils.isValidRestriction(key)) {
            int userHandle = this.mInjector.userHandleGetCallingUserId();
            synchronized (this) {
                ActiveAdmin activeAdmin = getActiveAdminForCallerLocked(who, -1);
                if (isDeviceOwner(who, userHandle)) {
                    if (!UserRestrictionsUtils.canDeviceOwnerChange(key)) {
                        throw new SecurityException("Device owner cannot set user restriction " + key);
                    }
                } else if (!UserRestrictionsUtils.canProfileOwnerChange(key, userHandle)) {
                    throw new SecurityException("Profile owner cannot set user restriction " + key);
                }
                Bundle restrictions = activeAdmin.ensureUserRestrictions();
                if (enabledFromThisOwner) {
                    restrictions.putBoolean(key, true);
                } else {
                    restrictions.remove(key);
                }
                saveUserRestrictionsLocked(userHandle);
            }
        }
    }

    private void saveUserRestrictionsLocked(int userId) {
        saveSettingsLocked(userId);
        pushUserRestrictions(userId);
        sendChangedNotification(userId);
    }

    private void pushUserRestrictions(int userId) {
        synchronized (this) {
            Bundle userRestrictions;
            boolean isDeviceOwner = this.mOwners.isDeviceOwnerUserId(userId);
            boolean disallowCameraGlobally = false;
            if (isDeviceOwner) {
                ActiveAdmin deviceOwner = getDeviceOwnerAdminLocked();
                if (deviceOwner == null) {
                    return;
                } else {
                    userRestrictions = deviceOwner.userRestrictions;
                    disallowCameraGlobally = deviceOwner.disableCamera;
                }
            } else {
                ActiveAdmin profileOwner = getProfileOwnerAdminLocked(userId);
                userRestrictions = profileOwner != null ? profileOwner.userRestrictions : null;
            }
            this.mUserManagerInternal.setDevicePolicyUserRestrictions(userId, userRestrictions, isDeviceOwner, getCameraRestrictionScopeLocked(userId, disallowCameraGlobally));
        }
    }

    private int getCameraRestrictionScopeLocked(int userId, boolean disallowCameraGlobally) {
        if (disallowCameraGlobally) {
            return 2;
        }
        if (getCameraDisabled(null, userId, false)) {
            return 1;
        }
        return 0;
    }

    public Bundle getUserRestrictions(ComponentName who) {
        if (!this.mHasFeature) {
            return null;
        }
        Bundle bundle;
        Preconditions.checkNotNull(who, "ComponentName is null");
        synchronized (this) {
            bundle = getActiveAdminForCallerLocked(who, -1).userRestrictions;
        }
        return bundle;
    }

    public boolean setApplicationHidden(ComponentName who, String callerPackage, String packageName, boolean hidden) {
        boolean applicationHiddenSettingAsUser;
        int callingUserId = UserHandle.getCallingUserId();
        synchronized (this) {
            enforceCanManageScope(who, callerPackage, -1, "delegation-package-access");
            long id = this.mInjector.binderClearCallingIdentity();
            try {
                applicationHiddenSettingAsUser = this.mIPackageManager.setApplicationHiddenSettingAsUser(packageName, hidden, callingUserId);
                this.mInjector.binderRestoreCallingIdentity(id);
            } catch (RemoteException re) {
                Slog.e(LOG_TAG, "Failed to setApplicationHiddenSetting", re);
                this.mInjector.binderRestoreCallingIdentity(id);
                return false;
            } catch (Throwable th) {
                this.mInjector.binderRestoreCallingIdentity(id);
            }
        }
        return applicationHiddenSettingAsUser;
    }

    public boolean isApplicationHidden(ComponentName who, String callerPackage, String packageName) {
        boolean applicationHiddenSettingAsUser;
        int callingUserId = UserHandle.getCallingUserId();
        synchronized (this) {
            enforceCanManageScope(who, callerPackage, -1, "delegation-package-access");
            long id = this.mInjector.binderClearCallingIdentity();
            try {
                applicationHiddenSettingAsUser = this.mIPackageManager.getApplicationHiddenSettingAsUser(packageName, callingUserId);
                this.mInjector.binderRestoreCallingIdentity(id);
            } catch (RemoteException re) {
                Slog.e(LOG_TAG, "Failed to getApplicationHiddenSettingAsUser", re);
                this.mInjector.binderRestoreCallingIdentity(id);
                return false;
            } catch (Throwable th) {
                this.mInjector.binderRestoreCallingIdentity(id);
            }
        }
        return applicationHiddenSettingAsUser;
    }

    public void enableSystemApp(ComponentName who, String callerPackage, String packageName) {
        synchronized (this) {
            enforceCanManageScope(who, callerPackage, -1, "delegation-enable-system-app");
            int userId = UserHandle.getCallingUserId();
            long id = this.mInjector.binderClearCallingIdentity();
            try {
                if (isSystemApp(this.mIPackageManager, packageName, getProfileParentId(userId))) {
                    this.mIPackageManager.installExistingPackageAsUser(packageName, userId, 0, 1);
                    this.mInjector.binderRestoreCallingIdentity(id);
                } else {
                    throw new IllegalArgumentException("Only system apps can be enabled this way.");
                }
            } catch (RemoteException re) {
                Slog.wtf(LOG_TAG, "Failed to install " + packageName, re);
                this.mInjector.binderRestoreCallingIdentity(id);
            } catch (Throwable th) {
                this.mInjector.binderRestoreCallingIdentity(id);
            }
        }
        return;
    }

    public int enableSystemAppWithIntent(ComponentName who, String callerPackage, Intent intent) {
        int numberOfAppsInstalled;
        synchronized (this) {
            enforceCanManageScope(who, callerPackage, -1, "delegation-enable-system-app");
            int userId = UserHandle.getCallingUserId();
            long id = this.mInjector.binderClearCallingIdentity();
            try {
                int parentUserId = getProfileParentId(userId);
                List<ResolveInfo> activitiesToEnable = this.mIPackageManager.queryIntentActivities(intent, intent.resolveTypeIfNeeded(this.mContext.getContentResolver()), 786432, parentUserId).getList();
                numberOfAppsInstalled = 0;
                if (activitiesToEnable != null) {
                    for (ResolveInfo info : activitiesToEnable) {
                        if (info.activityInfo != null) {
                            String packageName = info.activityInfo.packageName;
                            if (isSystemApp(this.mIPackageManager, packageName, parentUserId)) {
                                numberOfAppsInstalled++;
                                this.mIPackageManager.installExistingPackageAsUser(packageName, userId, 0, 1);
                            } else {
                                Slog.d(LOG_TAG, "Not enabling " + packageName + " since is not a" + " system app");
                            }
                        }
                    }
                }
                this.mInjector.binderRestoreCallingIdentity(id);
            } catch (RemoteException e) {
                Slog.wtf(LOG_TAG, "Failed to resolve intent for: " + intent);
                this.mInjector.binderRestoreCallingIdentity(id);
                return 0;
            } catch (Throwable th) {
                this.mInjector.binderRestoreCallingIdentity(id);
            }
        }
        return numberOfAppsInstalled;
    }

    private boolean isSystemApp(IPackageManager pm, String packageName, int userId) throws RemoteException {
        ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 8192, userId);
        if (appInfo == null) {
            throw new IllegalArgumentException("The application " + packageName + " is not present on this device");
        } else if ((appInfo.flags & 1) != 0) {
            return true;
        } else {
            return false;
        }
    }

    public void setAccountManagementDisabled(ComponentName who, String accountType, boolean disabled) {
        if (this.mHasFeature) {
            Preconditions.checkNotNull(who, "ComponentName is null");
            synchronized (this) {
                ActiveAdmin ap = getActiveAdminForCallerLocked(who, -1);
                if (disabled) {
                    ap.accountTypesWithManagementDisabled.add(accountType);
                } else {
                    ap.accountTypesWithManagementDisabled.remove(accountType);
                }
                saveSettingsLocked(UserHandle.getCallingUserId());
            }
        }
    }

    public String[] getAccountTypesWithManagementDisabled() {
        return getAccountTypesWithManagementDisabledAsUser(UserHandle.getCallingUserId());
    }

    public String[] getAccountTypesWithManagementDisabledAsUser(int userId) {
        enforceFullCrossUsersPermission(userId);
        if (!this.mHasFeature) {
            return null;
        }
        String[] strArr;
        synchronized (this) {
            DevicePolicyData policy = getUserData(userId);
            int N = policy.mAdminList.size();
            ArraySet<String> resultSet = new ArraySet();
            for (int i = 0; i < N; i++) {
                resultSet.addAll(((ActiveAdmin) policy.mAdminList.get(i)).accountTypesWithManagementDisabled);
            }
            strArr = (String[]) resultSet.toArray(new String[resultSet.size()]);
        }
        return strArr;
    }

    public void setUninstallBlocked(ComponentName who, String callerPackage, String packageName, boolean uninstallBlocked) {
        int userId = UserHandle.getCallingUserId();
        synchronized (this) {
            enforceCanManageScope(who, callerPackage, -1, "delegation-block-uninstall");
            long id = this.mInjector.binderClearCallingIdentity();
            try {
                this.mIPackageManager.setBlockUninstallForUser(packageName, uninstallBlocked, userId);
                this.mInjector.binderRestoreCallingIdentity(id);
            } catch (RemoteException re) {
                Slog.e(LOG_TAG, "Failed to setBlockUninstallForUser", re);
                this.mInjector.binderRestoreCallingIdentity(id);
            } catch (Throwable th) {
                this.mInjector.binderRestoreCallingIdentity(id);
            }
        }
        return;
    }

    public boolean isUninstallBlocked(ComponentName who, String packageName) {
        boolean blockUninstallForUser;
        int userId = UserHandle.getCallingUserId();
        synchronized (this) {
            if (who != null) {
                getActiveAdminForCallerLocked(who, -1);
            }
            long id = this.mInjector.binderClearCallingIdentity();
            try {
                blockUninstallForUser = this.mIPackageManager.getBlockUninstallForUser(packageName, userId);
                this.mInjector.binderRestoreCallingIdentity(id);
            } catch (RemoteException re) {
                Slog.e(LOG_TAG, "Failed to getBlockUninstallForUser", re);
                this.mInjector.binderRestoreCallingIdentity(id);
                return false;
            } catch (Throwable th) {
                this.mInjector.binderRestoreCallingIdentity(id);
            }
        }
        return blockUninstallForUser;
    }

    public void setCrossProfileCallerIdDisabled(ComponentName who, boolean disabled) {
        if (this.mHasFeature) {
            Preconditions.checkNotNull(who, "ComponentName is null");
            synchronized (this) {
                ActiveAdmin admin = getActiveAdminForCallerLocked(who, -1);
                if (admin.disableCallerId != disabled) {
                    admin.disableCallerId = disabled;
                    saveSettingsLocked(this.mInjector.userHandleGetCallingUserId());
                }
            }
        }
    }

    public boolean getCrossProfileCallerIdDisabled(ComponentName who) {
        if (!this.mHasFeature) {
            return false;
        }
        boolean z;
        Preconditions.checkNotNull(who, "ComponentName is null");
        synchronized (this) {
            z = getActiveAdminForCallerLocked(who, -1).disableCallerId;
        }
        return z;
    }

    public boolean getCrossProfileCallerIdDisabledForUser(int userId) {
        boolean z;
        enforceCrossUsersPermission(userId);
        synchronized (this) {
            ActiveAdmin admin = getProfileOwnerAdminLocked(userId);
            z = admin != null ? admin.disableCallerId : false;
        }
        return z;
    }

    public void setCrossProfileContactsSearchDisabled(ComponentName who, boolean disabled) {
        if (this.mHasFeature) {
            Preconditions.checkNotNull(who, "ComponentName is null");
            synchronized (this) {
                ActiveAdmin admin = getActiveAdminForCallerLocked(who, -1);
                if (admin.disableContactsSearch != disabled) {
                    admin.disableContactsSearch = disabled;
                    saveSettingsLocked(this.mInjector.userHandleGetCallingUserId());
                }
            }
        }
    }

    public boolean getCrossProfileContactsSearchDisabled(ComponentName who) {
        if (!this.mHasFeature) {
            return false;
        }
        boolean z;
        Preconditions.checkNotNull(who, "ComponentName is null");
        synchronized (this) {
            z = getActiveAdminForCallerLocked(who, -1).disableContactsSearch;
        }
        return z;
    }

    public boolean getCrossProfileContactsSearchDisabledForUser(int userId) {
        boolean z;
        enforceCrossUsersPermission(userId);
        synchronized (this) {
            ActiveAdmin admin = getProfileOwnerAdminLocked(userId);
            z = admin != null ? admin.disableContactsSearch : false;
        }
        return z;
    }

    public void startManagedQuickContact(String actualLookupKey, long actualContactId, boolean isContactIdIgnored, long actualDirectoryId, Intent originalIntent) {
        Intent intent = QuickContact.rebuildManagedQuickContactsIntent(actualLookupKey, actualContactId, isContactIdIgnored, actualDirectoryId, originalIntent);
        int callingUserId = UserHandle.getCallingUserId();
        long ident = this.mInjector.binderClearCallingIdentity();
        try {
            synchronized (this) {
                int managedUserId = getManagedUserId(callingUserId);
                if (managedUserId >= 0) {
                    if (isCrossProfileQuickContactDisabled(managedUserId)) {
                        this.mInjector.binderRestoreCallingIdentity(ident);
                        return;
                    }
                    ContactsInternal.startQuickContactWithErrorToastForUser(this.mContext, intent, new UserHandle(managedUserId));
                    this.mInjector.binderRestoreCallingIdentity(ident);
                }
            }
        } finally {
            this.mInjector.binderRestoreCallingIdentity(ident);
        }
    }

    private boolean isCrossProfileQuickContactDisabled(int userId) {
        if (getCrossProfileCallerIdDisabledForUser(userId)) {
            return getCrossProfileContactsSearchDisabledForUser(userId);
        }
        return false;
    }

    public int getManagedUserId(int callingUserId) {
        for (UserInfo ui : this.mUserManager.getProfiles(callingUserId)) {
            if (ui.id != callingUserId && (ui.isManagedProfile() ^ 1) == 0) {
                return ui.id;
            }
        }
        return -1;
    }

    public void setBluetoothContactSharingDisabled(ComponentName who, boolean disabled) {
        if (this.mHasFeature) {
            Preconditions.checkNotNull(who, "ComponentName is null");
            synchronized (this) {
                ActiveAdmin admin = getActiveAdminForCallerLocked(who, -1);
                if (admin.disableBluetoothContactSharing != disabled) {
                    admin.disableBluetoothContactSharing = disabled;
                    saveSettingsLocked(UserHandle.getCallingUserId());
                }
            }
        }
    }

    public boolean getBluetoothContactSharingDisabled(ComponentName who) {
        if (!this.mHasFeature) {
            return false;
        }
        boolean z;
        Preconditions.checkNotNull(who, "ComponentName is null");
        synchronized (this) {
            z = getActiveAdminForCallerLocked(who, -1).disableBluetoothContactSharing;
        }
        return z;
    }

    public boolean getBluetoothContactSharingDisabledForUser(int userId) {
        boolean z;
        synchronized (this) {
            ActiveAdmin admin = getProfileOwnerAdminLocked(userId);
            z = admin != null ? admin.disableBluetoothContactSharing : false;
        }
        return z;
    }

    public void setLockTaskPackages(ComponentName who, String[] packages) throws SecurityException {
        Preconditions.checkNotNull(who, "ComponentName is null");
        Preconditions.checkNotNull(packages, "packages is null");
        synchronized (this) {
            getActiveAdminForCallerLocked(who, -1);
            int userHandle = this.mInjector.userHandleGetCallingUserId();
            if (isUserAffiliatedWithDeviceLocked(userHandle)) {
                setLockTaskPackagesLocked(userHandle, new ArrayList(Arrays.asList(packages)));
            } else {
                throw new SecurityException("Admin " + who + " is neither the device owner or affiliated user's profile owner.");
            }
        }
    }

    private void setLockTaskPackagesLocked(int userHandle, List<String> packages) {
        getUserData(userHandle).mLockTaskPackages = packages;
        saveSettingsLocked(userHandle);
        updateLockTaskPackagesLocked(packages, userHandle);
    }

    private void maybeClearLockTaskPackagesLocked() {
        long ident = this.mInjector.binderClearCallingIdentity();
        try {
            List<UserInfo> userInfos = this.mUserManager.getUsers(true);
            for (int i = 0; i < userInfos.size(); i++) {
                int userId = ((UserInfo) userInfos.get(i)).id;
                if (!(getUserData(userId).mLockTaskPackages.isEmpty() || (isUserAffiliatedWithDeviceLocked(userId) ^ 1) == 0)) {
                    Slog.d(LOG_TAG, "User id " + userId + " not affiliated. Clearing lock task packages");
                    setLockTaskPackagesLocked(userId, Collections.emptyList());
                }
            }
        } finally {
            this.mInjector.binderRestoreCallingIdentity(ident);
        }
    }

    public String[] getLockTaskPackages(ComponentName who) {
        String[] strArr;
        Preconditions.checkNotNull(who, "ComponentName is null");
        int userHandle = this.mInjector.binderGetCallingUserHandle().getIdentifier();
        synchronized (this) {
            getActiveAdminForCallerLocked(who, -1);
            if (isUserAffiliatedWithDeviceLocked(userHandle)) {
                List<String> packages = getUserData(userHandle).mLockTaskPackages;
                strArr = (String[]) packages.toArray(new String[packages.size()]);
            } else {
                throw new SecurityException("Admin " + who + " is neither the device owner or affiliated user's profile owner.");
            }
        }
        return strArr;
    }

    public boolean isLockTaskPermitted(String pkg) {
        boolean contains;
        int userHandle = this.mInjector.userHandleGetCallingUserId();
        synchronized (this) {
            contains = getUserData(userHandle).mLockTaskPackages.contains(pkg);
        }
        return contains;
    }

    public void notifyLockTaskModeChanged(boolean isEnabled, String pkg, int userHandle) {
        if (isCallerWithSystemUid()) {
            synchronized (this) {
                DevicePolicyData policy = getUserData(userHandle);
                Bundle adminExtras = new Bundle();
                adminExtras.putString("android.app.extra.LOCK_TASK_PACKAGE", pkg);
                for (ActiveAdmin admin : policy.mAdminList) {
                    boolean ownsDevice = isDeviceOwner(admin.info.getComponent(), userHandle);
                    boolean ownsProfile = isProfileOwner(admin.info.getComponent(), userHandle);
                    if (ownsDevice || ownsProfile) {
                        if (isEnabled) {
                            sendAdminCommandLocked(admin, "android.app.action.LOCK_TASK_ENTERING", adminExtras, null);
                        } else {
                            sendAdminCommandLocked(admin, "android.app.action.LOCK_TASK_EXITING");
                        }
                    }
                }
            }
            return;
        }
        throw new SecurityException("notifyLockTaskModeChanged can only be called by system");
    }

    /* JADX WARNING: Missing block: B:30:0x0081, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setGlobalSetting(ComponentName who, String setting, String value) {
        Preconditions.checkNotNull(who, "ComponentName is null");
        synchronized (this) {
            getActiveAdminForCallerLocked(who, -2);
            if (GLOBAL_SETTINGS_DEPRECATED.contains(setting)) {
                Log.i(LOG_TAG, "Global setting no longer supported: " + setting);
            } else if (GLOBAL_SETTINGS_WHITELIST.contains(setting)) {
                if ("stay_on_while_plugged_in".equals(setting)) {
                    long timeMs = getMaximumTimeToLock(who, this.mInjector.userHandleGetCallingUserId(), false);
                    if (timeMs > 0 && timeMs < 2147483647L) {
                        return;
                    }
                }
                long id = this.mInjector.binderClearCallingIdentity();
                try {
                    this.mInjector.settingsGlobalPutString(setting, value);
                } finally {
                    this.mInjector.binderRestoreCallingIdentity(id);
                }
            } else {
                throw new SecurityException(String.format("Permission denial: device owners cannot update %1$s", new Object[]{setting}));
            }
        }
    }

    /*  JADX ERROR: JadxRuntimeException in pass: RegionMakerVisitor
        jadx.core.utils.exceptions.JadxRuntimeException: Exception block dominator not found, method:com.android.server.devicepolicy.DevicePolicyManagerService.setSecureSetting(android.content.ComponentName, java.lang.String, java.lang.String):void, dom blocks: [B:29:0x00a9, B:38:0x00e5]
        	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.searchTryCatchDominators(ProcessTryCatchRegions.java:89)
        	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.process(ProcessTryCatchRegions.java:45)
        	at jadx.core.dex.visitors.regions.RegionMakerVisitor.postProcessRegions(RegionMakerVisitor.java:63)
        	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:58)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public void setSecureSetting(android.content.ComponentName r10, java.lang.String r11, java.lang.String r12) {
        /*
        r9 = this;
        r3 = 1;
        r6 = 0;
        r7 = "ComponentName is null";
        com.android.internal.util.Preconditions.checkNotNull(r10, r7);
        r7 = r9.mInjector;
        r0 = r7.userHandleGetCallingUserId();
        monitor-enter(r9);
        r7 = -1;
        r9.getActiveAdminForCallerLocked(r10, r7);	 Catch:{ all -> 0x0034 }
        r7 = r9.isDeviceOwner(r10, r0);	 Catch:{ all -> 0x0034 }
        if (r7 == 0) goto L_0x0037;	 Catch:{ all -> 0x0034 }
    L_0x0019:
        r7 = SECURE_SETTINGS_DEVICEOWNER_WHITELIST;	 Catch:{ all -> 0x0034 }
        r7 = r7.contains(r11);	 Catch:{ all -> 0x0034 }
        if (r7 != 0) goto L_0x0052;	 Catch:{ all -> 0x0034 }
    L_0x0021:
        r3 = new java.lang.SecurityException;	 Catch:{ all -> 0x0034 }
        r6 = "Permission denial: Device owners cannot update %1$s";	 Catch:{ all -> 0x0034 }
        r7 = 1;	 Catch:{ all -> 0x0034 }
        r7 = new java.lang.Object[r7];	 Catch:{ all -> 0x0034 }
        r8 = 0;	 Catch:{ all -> 0x0034 }
        r7[r8] = r11;	 Catch:{ all -> 0x0034 }
        r6 = java.lang.String.format(r6, r7);	 Catch:{ all -> 0x0034 }
        r3.<init>(r6);	 Catch:{ all -> 0x0034 }
        throw r3;	 Catch:{ all -> 0x0034 }
    L_0x0034:
        r3 = move-exception;
        monitor-exit(r9);
        throw r3;
    L_0x0037:
        r7 = SECURE_SETTINGS_WHITELIST;	 Catch:{ all -> 0x0034 }
        r7 = r7.contains(r11);	 Catch:{ all -> 0x0034 }
        if (r7 != 0) goto L_0x0052;	 Catch:{ all -> 0x0034 }
    L_0x003f:
        r3 = new java.lang.SecurityException;	 Catch:{ all -> 0x0034 }
        r6 = "Permission denial: Profile owners cannot update %1$s";	 Catch:{ all -> 0x0034 }
        r7 = 1;	 Catch:{ all -> 0x0034 }
        r7 = new java.lang.Object[r7];	 Catch:{ all -> 0x0034 }
        r8 = 0;	 Catch:{ all -> 0x0034 }
        r7[r8] = r11;	 Catch:{ all -> 0x0034 }
        r6 = java.lang.String.format(r6, r7);	 Catch:{ all -> 0x0034 }
        r3.<init>(r6);	 Catch:{ all -> 0x0034 }
        throw r3;	 Catch:{ all -> 0x0034 }
    L_0x0052:
        r7 = "install_non_market_apps";	 Catch:{ all -> 0x0034 }
        r7 = r11.equals(r7);	 Catch:{ all -> 0x0034 }
        if (r7 == 0) goto L_0x00df;	 Catch:{ all -> 0x0034 }
    L_0x005b:
        r7 = r10.getPackageName();	 Catch:{ all -> 0x0034 }
        r7 = r9.getTargetSdk(r7, r0);	 Catch:{ all -> 0x0034 }
        r8 = 26;	 Catch:{ all -> 0x0034 }
        if (r7 < r8) goto L_0x0070;	 Catch:{ all -> 0x0034 }
    L_0x0067:
        r3 = new java.lang.UnsupportedOperationException;	 Catch:{ all -> 0x0034 }
        r6 = "install_non_market_apps is deprecated. Please use the user restriction no_install_unknown_sources instead.";	 Catch:{ all -> 0x0034 }
        r3.<init>(r6);	 Catch:{ all -> 0x0034 }
        throw r3;	 Catch:{ all -> 0x0034 }
    L_0x0070:
        r7 = r9.mUserManager;	 Catch:{ all -> 0x0034 }
        r7 = r7.isManagedProfile(r0);	 Catch:{ all -> 0x0034 }
        if (r7 != 0) goto L_0x00a9;	 Catch:{ all -> 0x0034 }
    L_0x0078:
        r3 = "DevicePolicyManager";	 Catch:{ all -> 0x0034 }
        r6 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0034 }
        r6.<init>();	 Catch:{ all -> 0x0034 }
        r7 = "Ignoring setSecureSetting request for ";	 Catch:{ all -> 0x0034 }
        r6 = r6.append(r7);	 Catch:{ all -> 0x0034 }
        r6 = r6.append(r11);	 Catch:{ all -> 0x0034 }
        r7 = ". User restriction ";	 Catch:{ all -> 0x0034 }
        r6 = r6.append(r7);	 Catch:{ all -> 0x0034 }
        r7 = "no_install_unknown_sources";	 Catch:{ all -> 0x0034 }
        r6 = r6.append(r7);	 Catch:{ all -> 0x0034 }
        r7 = " should be used instead.";	 Catch:{ all -> 0x0034 }
        r6 = r6.append(r7);	 Catch:{ all -> 0x0034 }
        r6 = r6.toString();	 Catch:{ all -> 0x0034 }
        android.util.Slog.e(r3, r6);	 Catch:{ all -> 0x0034 }
    L_0x00a7:
        monitor-exit(r9);
        return;
    L_0x00a9:
        r7 = "no_install_unknown_sources";	 Catch:{ NumberFormatException -> 0x00b6 }
        r8 = java.lang.Integer.parseInt(r12);	 Catch:{ NumberFormatException -> 0x00b6 }
        if (r8 != 0) goto L_0x00dd;	 Catch:{ NumberFormatException -> 0x00b6 }
    L_0x00b2:
        r9.setUserRestriction(r10, r7, r3);	 Catch:{ NumberFormatException -> 0x00b6 }
        goto L_0x00a7;
    L_0x00b6:
        r2 = move-exception;
        r3 = "DevicePolicyManager";	 Catch:{ all -> 0x0034 }
        r6 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0034 }
        r6.<init>();	 Catch:{ all -> 0x0034 }
        r7 = "Invalid value: ";	 Catch:{ all -> 0x0034 }
        r6 = r6.append(r7);	 Catch:{ all -> 0x0034 }
        r6 = r6.append(r12);	 Catch:{ all -> 0x0034 }
        r7 = " for setting ";	 Catch:{ all -> 0x0034 }
        r6 = r6.append(r7);	 Catch:{ all -> 0x0034 }
        r6 = r6.append(r11);	 Catch:{ all -> 0x0034 }
        r6 = r6.toString();	 Catch:{ all -> 0x0034 }
        android.util.Slog.e(r3, r6);	 Catch:{ all -> 0x0034 }
        goto L_0x00a7;	 Catch:{ all -> 0x0034 }
    L_0x00dd:
        r3 = r6;	 Catch:{ all -> 0x0034 }
        goto L_0x00b2;	 Catch:{ all -> 0x0034 }
    L_0x00df:
        r3 = r9.mInjector;	 Catch:{ all -> 0x0034 }
        r4 = r3.binderClearCallingIdentity();	 Catch:{ all -> 0x0034 }
        r3 = "default_input_method";	 Catch:{ all -> 0x0118 }
        r3 = r3.equals(r11);	 Catch:{ all -> 0x0118 }
        if (r3 == 0) goto L_0x010c;	 Catch:{ all -> 0x0118 }
    L_0x00ee:
        r3 = r9.mInjector;	 Catch:{ all -> 0x0118 }
        r6 = "default_input_method";	 Catch:{ all -> 0x0118 }
        r1 = r3.settingsSecureGetStringForUser(r6, r0);	 Catch:{ all -> 0x0118 }
        r3 = android.text.TextUtils.equals(r1, r12);	 Catch:{ all -> 0x0118 }
        if (r3 != 0) goto L_0x0102;	 Catch:{ all -> 0x0118 }
    L_0x00fd:
        r3 = r9.mSetupContentObserver;	 Catch:{ all -> 0x0118 }
        r3.addPendingChangeByOwnerLocked(r0);	 Catch:{ all -> 0x0118 }
    L_0x0102:
        r3 = r9.getUserData(r0);	 Catch:{ all -> 0x0118 }
        r6 = 1;	 Catch:{ all -> 0x0118 }
        r3.mCurrentInputMethodSet = r6;	 Catch:{ all -> 0x0118 }
        r9.saveSettingsLocked(r0);	 Catch:{ all -> 0x0118 }
    L_0x010c:
        r3 = r9.mInjector;	 Catch:{ all -> 0x0118 }
        r3.settingsSecurePutStringForUser(r11, r12, r0);	 Catch:{ all -> 0x0118 }
        r3 = r9.mInjector;	 Catch:{ all -> 0x0034 }
        r3.binderRestoreCallingIdentity(r4);	 Catch:{ all -> 0x0034 }
        monitor-exit(r9);
        return;
    L_0x0118:
        r3 = move-exception;
        r6 = r9.mInjector;	 Catch:{ all -> 0x0034 }
        r6.binderRestoreCallingIdentity(r4);	 Catch:{ all -> 0x0034 }
        throw r3;	 Catch:{ all -> 0x0034 }
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.devicepolicy.DevicePolicyManagerService.setSecureSetting(android.content.ComponentName, java.lang.String, java.lang.String):void");
    }

    public void setMasterVolumeMuted(ComponentName who, boolean on) {
        Preconditions.checkNotNull(who, "ComponentName is null");
        synchronized (this) {
            getActiveAdminForCallerLocked(who, -1);
            setUserRestriction(who, "disallow_unmute_device", on);
        }
    }

    public boolean isMasterVolumeMuted(ComponentName who) {
        boolean isMasterMute;
        Preconditions.checkNotNull(who, "ComponentName is null");
        synchronized (this) {
            getActiveAdminForCallerLocked(who, -1);
            isMasterMute = ((AudioManager) this.mContext.getSystemService("audio")).isMasterMute();
        }
        return isMasterMute;
    }

    public void setUserIcon(ComponentName who, Bitmap icon) {
        synchronized (this) {
            Preconditions.checkNotNull(who, "ComponentName is null");
            getActiveAdminForCallerLocked(who, -1);
            int userId = UserHandle.getCallingUserId();
            long id = this.mInjector.binderClearCallingIdentity();
            try {
                this.mUserManagerInternal.setUserIcon(userId, icon);
                this.mInjector.binderRestoreCallingIdentity(id);
            } catch (Throwable th) {
                this.mInjector.binderRestoreCallingIdentity(id);
            }
        }
    }

    public boolean setKeyguardDisabled(ComponentName who, boolean disabled) {
        Preconditions.checkNotNull(who, "ComponentName is null");
        synchronized (this) {
            getActiveAdminForCallerLocked(who, -2);
        }
        int userId = UserHandle.getCallingUserId();
        long ident = this.mInjector.binderClearCallingIdentity();
        if (disabled) {
            try {
                if (this.mLockPatternUtils.isSecure(userId)) {
                    return false;
                }
            } finally {
                this.mInjector.binderRestoreCallingIdentity(ident);
            }
        }
        this.mLockPatternUtils.setLockScreenDisabled(disabled, userId);
        this.mInjector.binderRestoreCallingIdentity(ident);
        return true;
    }

    /* JADX WARNING: Missing block: B:15:0x0021, code:
            return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean setStatusBarDisabled(ComponentName who, boolean disabled) {
        int userId = UserHandle.getCallingUserId();
        synchronized (this) {
            getActiveAdminForCallerLocked(who, -2);
            DevicePolicyData policy = getUserData(userId);
            if (policy.mStatusBarDisabled != disabled) {
                if (setStatusBarDisabledInternal(disabled, userId)) {
                    policy.mStatusBarDisabled = disabled;
                    saveSettingsLocked(userId);
                } else {
                    return false;
                }
            }
        }
    }

    private boolean setStatusBarDisabledInternal(boolean disabled, int userId) {
        long ident = this.mInjector.binderClearCallingIdentity();
        try {
            IStatusBarService statusBarService = IStatusBarService.Stub.asInterface(ServiceManager.checkService(TAG_STATUS_BAR));
            if (statusBarService != null) {
                int flags1 = disabled ? STATUS_BAR_DISABLE_MASK : 0;
                int flags2 = disabled ? 1 : 0;
                statusBarService.disableForUser(flags1, this.mToken, this.mContext.getPackageName(), userId);
                statusBarService.disable2ForUser(flags2, this.mToken, this.mContext.getPackageName(), userId);
                return true;
            }
            this.mInjector.binderRestoreCallingIdentity(ident);
            return false;
        } catch (RemoteException e) {
            Slog.e(LOG_TAG, "Failed to disable the status bar", e);
        } finally {
            this.mInjector.binderRestoreCallingIdentity(ident);
        }
    }

    void updateUserSetupCompleteAndPaired() {
        List<UserInfo> users = this.mUserManager.getUsers(true);
        int N = users.size();
        for (int i = 0; i < N; i++) {
            DevicePolicyData policy;
            int userHandle = ((UserInfo) users.get(i)).id;
            if (this.mInjector.settingsSecureGetIntForUser("user_setup_complete", 0, userHandle) != 0) {
                policy = getUserData(userHandle);
                if (!policy.mUserSetupComplete) {
                    policy.mUserSetupComplete = true;
                    synchronized (this) {
                        saveSettingsLocked(userHandle);
                    }
                }
            }
            if (this.mIsWatch && this.mInjector.settingsSecureGetIntForUser("device_paired", 0, userHandle) != 0) {
                policy = getUserData(userHandle);
                if (policy.mPaired) {
                    continue;
                } else {
                    policy.mPaired = true;
                    synchronized (this) {
                        saveSettingsLocked(userHandle);
                    }
                }
            }
        }
    }

    private Intent createShowAdminSupportIntent(ComponentName admin, int userId) {
        Intent intent = new Intent("android.settings.SHOW_ADMIN_SUPPORT_DETAILS");
        intent.putExtra("android.intent.extra.USER_ID", userId);
        intent.putExtra("android.app.extra.DEVICE_ADMIN", admin);
        intent.setFlags(268435456);
        return intent;
    }

    public Intent createAdminSupportIntent(String restriction) {
        Preconditions.checkNotNull(restriction);
        int userId = UserHandle.getUserId(this.mInjector.binderGetCallingUid());
        Intent intent = null;
        if ("policy_disable_camera".equals(restriction) || "policy_disable_screen_capture".equals(restriction)) {
            synchronized (this) {
                ActiveAdmin admin;
                DevicePolicyData policy = getUserData(userId);
                int N = policy.mAdminList.size();
                for (int i = 0; i < N; i++) {
                    admin = (ActiveAdmin) policy.mAdminList.get(i);
                    if ((admin.disableCamera && "policy_disable_camera".equals(restriction)) || (admin.disableScreenCapture && "policy_disable_screen_capture".equals(restriction))) {
                        intent = createShowAdminSupportIntent(admin.info.getComponent(), userId);
                        break;
                    }
                }
                if (intent == null && "policy_disable_camera".equals(restriction)) {
                    admin = getDeviceOwnerAdminLocked();
                    if (admin != null && admin.disableCamera) {
                        intent = createShowAdminSupportIntent(admin.info.getComponent(), this.mOwners.getDeviceOwnerUserId());
                    }
                }
            }
        } else {
            intent = this.mLocalService.createUserRestrictionSupportIntent(userId, restriction);
        }
        if (intent != null) {
            intent.putExtra("android.app.extra.RESTRICTION", restriction);
        }
        return intent;
    }

    private static boolean isLimitPasswordAllowed(ActiveAdmin admin, int minPasswordQuality) {
        if (admin.minimumPasswordMetrics.quality < minPasswordQuality) {
            return false;
        }
        return admin.info.usesPolicy(0);
    }

    public void setSystemUpdatePolicy(ComponentName who, SystemUpdatePolicy policy) {
        if (policy == null || (policy.isValid() ^ 1) == 0) {
            synchronized (this) {
                getActiveAdminForCallerLocked(who, -2);
                if (policy == null) {
                    this.mOwners.clearSystemUpdatePolicy();
                } else {
                    this.mOwners.setSystemUpdatePolicy(policy);
                }
                this.mOwners.writeDeviceOwner();
            }
            this.mContext.sendBroadcastAsUser(new Intent("android.app.action.SYSTEM_UPDATE_POLICY_CHANGED"), UserHandle.SYSTEM);
            return;
        }
        throw new IllegalArgumentException("Invalid system update policy.");
    }

    /* JADX WARNING: Missing block: B:14:0x002b, code:
            return r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public SystemUpdatePolicy getSystemUpdatePolicy() {
        if (UserManager.isDeviceInDemoMode(this.mContext)) {
            return SystemUpdatePolicy.createAutomaticInstallPolicy();
        }
        synchronized (this) {
            SystemUpdatePolicy policy = this.mOwners.getSystemUpdatePolicy();
            if (policy == null || (policy.isValid() ^ 1) == 0) {
            } else {
                Slog.w(LOG_TAG, "Stored system update policy is invalid, return null instead.");
                return null;
            }
        }
    }

    boolean isCallerDeviceOwner(int callerUid) {
        synchronized (this) {
            if (!this.mOwners.hasDeviceOwner()) {
                return false;
            } else if (UserHandle.getUserId(callerUid) != this.mOwners.getDeviceOwnerUserId()) {
                return false;
            } else {
                String deviceOwnerPackageName = this.mOwners.getDeviceOwnerComponent().getPackageName();
                try {
                    for (String pkg : this.mInjector.getIPackageManager().getPackagesForUid(callerUid)) {
                        if (deviceOwnerPackageName.equals(pkg)) {
                            return true;
                        }
                    }
                    return false;
                } catch (RemoteException e) {
                    return false;
                }
            }
        }
    }

    public void notifyPendingSystemUpdate(SystemUpdateInfo info) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.NOTIFY_PENDING_SYSTEM_UPDATE", "Only the system update service can broadcast update information");
        if (UserHandle.getCallingUserId() != 0) {
            Slog.w(LOG_TAG, "Only the system update service in the system user can broadcast update information.");
        } else if (this.mOwners.saveSystemUpdateInfo(info)) {
            Intent intent = new Intent("android.app.action.NOTIFY_PENDING_SYSTEM_UPDATE").putExtra("android.app.extra.SYSTEM_UPDATE_RECEIVED_TIME", info == null ? -1 : info.getReceivedTime());
            long ident = this.mInjector.binderClearCallingIdentity();
            synchronized (this) {
                if (this.mOwners.hasDeviceOwner()) {
                    UserHandle deviceOwnerUser = UserHandle.of(this.mOwners.getDeviceOwnerUserId());
                    intent.setComponent(this.mOwners.getDeviceOwnerComponent());
                    this.mContext.sendBroadcastAsUser(intent, deviceOwnerUser);
                }
                try {
                } catch (Throwable th) {
                    this.mInjector.binderRestoreCallingIdentity(ident);
                }
            }
            try {
                for (int userId : this.mInjector.getIActivityManager().getRunningUserIds()) {
                    synchronized (this) {
                        ComponentName profileOwnerPackage = this.mOwners.getProfileOwnerComponent(userId);
                        if (profileOwnerPackage != null) {
                            intent.setComponent(profileOwnerPackage);
                            this.mContext.sendBroadcastAsUser(intent, UserHandle.of(userId));
                        }
                    }
                }
                this.mInjector.binderRestoreCallingIdentity(ident);
            } catch (RemoteException e) {
                Log.e(LOG_TAG, "Could not retrieve the list of running users", e);
                this.mInjector.binderRestoreCallingIdentity(ident);
            }
        }
    }

    public SystemUpdateInfo getPendingSystemUpdate(ComponentName admin) {
        Preconditions.checkNotNull(admin, "ComponentName is null");
        enforceProfileOrDeviceOwner(admin);
        return this.mOwners.getSystemUpdateInfo();
    }

    public void setPermissionPolicy(ComponentName admin, String callerPackage, int policy) throws RemoteException {
        int userId = UserHandle.getCallingUserId();
        synchronized (this) {
            enforceCanManageScope(admin, callerPackage, -1, "delegation-permission-grant");
            DevicePolicyData userPolicy = getUserData(userId);
            if (userPolicy.mPermissionPolicy != policy) {
                userPolicy.mPermissionPolicy = policy;
                saveSettingsLocked(userId);
            }
        }
    }

    public int getPermissionPolicy(ComponentName admin) throws RemoteException {
        int i;
        int userId = UserHandle.getCallingUserId();
        synchronized (this) {
            i = getUserData(userId).mPermissionPolicy;
        }
        return i;
    }

    public boolean setPermissionGrantState(ComponentName admin, String callerPackage, String packageName, String permission, int grantState) throws RemoteException {
        UserHandle user = this.mInjector.binderGetCallingUserHandle();
        synchronized (this) {
            enforceCanManageScope(admin, callerPackage, -1, "delegation-permission-grant");
            long ident = this.mInjector.binderClearCallingIdentity();
            try {
                if (getTargetSdk(packageName, user.getIdentifier()) < 23) {
                    this.mInjector.binderRestoreCallingIdentity(ident);
                    return false;
                } else if (isRuntimePermission(permission)) {
                    PackageManager packageManager = this.mInjector.getPackageManager();
                    switch (grantState) {
                        case 0:
                            packageManager.updatePermissionFlags(permission, packageName, 4, 0, user);
                            break;
                        case 1:
                            this.mInjector.getPackageManagerInternal().grantRuntimePermission(packageName, permission, user.getIdentifier(), true);
                            packageManager.updatePermissionFlags(permission, packageName, 4, 4, user);
                            break;
                        case 2:
                            this.mInjector.getPackageManagerInternal().revokeRuntimePermission(packageName, permission, user.getIdentifier(), true);
                            packageManager.updatePermissionFlags(permission, packageName, 4, 4, user);
                            break;
                    }
                    this.mInjector.binderRestoreCallingIdentity(ident);
                    return true;
                } else {
                    EventLog.writeEvent(1397638484, new Object[]{"62623498", Integer.valueOf(user.getIdentifier()), ""});
                    this.mInjector.binderRestoreCallingIdentity(ident);
                    return false;
                }
            } catch (SecurityException e) {
                this.mInjector.binderRestoreCallingIdentity(ident);
                return false;
            } catch (NameNotFoundException e2) {
                this.mInjector.binderRestoreCallingIdentity(ident);
                return false;
            } catch (Throwable th) {
                this.mInjector.binderRestoreCallingIdentity(ident);
            }
        }
    }

    public int getPermissionGrantState(ComponentName admin, String callerPackage, String packageName, String permission) throws RemoteException {
        PackageManager packageManager = this.mInjector.getPackageManager();
        UserHandle user = this.mInjector.binderGetCallingUserHandle();
        if (!isCallerWithSystemUid()) {
            enforceCanManageScope(admin, callerPackage, -1, "delegation-permission-grant");
        }
        synchronized (this) {
            long ident = this.mInjector.binderClearCallingIdentity();
            try {
                int granted = this.mIPackageManager.checkPermission(permission, packageName, user.getIdentifier());
                if ((packageManager.getPermissionFlags(permission, packageName, user) & 4) != 4) {
                    this.mInjector.binderRestoreCallingIdentity(ident);
                    return 0;
                }
                int i;
                if (granted == 0) {
                    i = 1;
                } else {
                    i = 2;
                }
                this.mInjector.binderRestoreCallingIdentity(ident);
                return i;
            } catch (Throwable th) {
                this.mInjector.binderRestoreCallingIdentity(ident);
            }
        }
    }

    boolean isPackageInstalledForUser(String packageName, int userHandle) {
        try {
            PackageInfo pi = this.mInjector.getIPackageManager().getPackageInfo(packageName, 0, userHandle);
            if (pi == null || pi.applicationInfo.flags == 0) {
                return false;
            }
            return true;
        } catch (RemoteException re) {
            throw new RuntimeException("Package manager has died", re);
        }
    }

    public boolean isRuntimePermission(String permissionName) throws NameNotFoundException {
        if ((this.mInjector.getPackageManager().getPermissionInfo(permissionName, 0).protectionLevel & 15) == 1) {
            return true;
        }
        return false;
    }

    public boolean isProvisioningAllowed(String action, String packageName) {
        Preconditions.checkNotNull(packageName);
        int callingUid = this.mInjector.binderGetCallingUid();
        long ident = this.mInjector.binderClearCallingIdentity();
        try {
            Preconditions.checkArgument(callingUid == this.mInjector.getPackageManager().getPackageUidAsUser(packageName, UserHandle.getUserId(callingUid)), "Caller uid doesn't match the one for the provided package.");
            this.mInjector.binderRestoreCallingIdentity(ident);
            if (checkProvisioningPreConditionSkipPermission(action, packageName) == 0) {
                return true;
            }
            return false;
        } catch (NameNotFoundException e) {
            throw new IllegalArgumentException("Invalid package provided " + packageName, e);
        } catch (Throwable th) {
            this.mInjector.binderRestoreCallingIdentity(ident);
        }
    }

    public int checkProvisioningPreCondition(String action, String packageName) {
        Preconditions.checkNotNull(packageName);
        enforceCanManageProfileAndDeviceOwners();
        return checkProvisioningPreConditionSkipPermission(action, packageName);
    }

    private int checkProvisioningPreConditionSkipPermission(String action, String packageName) {
        if (!this.mHasFeature) {
            return 13;
        }
        int callingUserId = this.mInjector.userHandleGetCallingUserId();
        if (action != null) {
            if (action.equals("android.app.action.PROVISION_MANAGED_PROFILE")) {
                return checkManagedProfileProvisioningPreCondition(packageName, callingUserId);
            }
            if (action.equals("android.app.action.PROVISION_MANAGED_DEVICE")) {
                return checkDeviceOwnerProvisioningPreCondition(callingUserId);
            }
            if (action.equals("android.app.action.PROVISION_MANAGED_USER")) {
                return checkManagedUserProvisioningPreCondition(callingUserId);
            }
            if (action.equals("android.app.action.PROVISION_MANAGED_SHAREABLE_DEVICE")) {
                return checkManagedShareableDeviceProvisioningPreCondition(callingUserId);
            }
        }
        throw new IllegalArgumentException("Unknown provisioning action " + action);
    }

    private int checkDeviceOwnerProvisioningPreConditionLocked(ComponentName owner, int deviceOwnerUserId, boolean isAdb, boolean hasIncompatibleAccountsOrNonAdb) {
        if (this.mOwners.hasDeviceOwner()) {
            return 1;
        }
        if (this.mOwners.hasProfileOwner(deviceOwnerUserId)) {
            return 2;
        }
        if (!this.mUserManager.isUserRunning(new UserHandle(deviceOwnerUserId))) {
            return 3;
        }
        if (this.mIsWatch && hasPaired(0)) {
            return 8;
        }
        if (isAdb || this.mIsMDMDeviceOwnerAPI) {
            if ((this.mIsWatch || hasUserSetupCompleted(0)) && !this.mInjector.userManagerIsSplitSystemUser()) {
                if (this.mUserManager.getUserCount() > 1) {
                    return 5;
                }
                if (hasIncompatibleAccountsOrNonAdb) {
                    return 6;
                }
            }
            return 0;
        }
        if (!this.mInjector.userManagerIsSplitSystemUser()) {
            if (deviceOwnerUserId != 0) {
                return 7;
            }
            if (hasUserSetupCompleted(0)) {
                return 4;
            }
        }
        return 0;
    }

    private int checkDeviceOwnerProvisioningPreCondition(int deviceOwnerUserId) {
        int checkDeviceOwnerProvisioningPreConditionLocked;
        synchronized (this) {
            checkDeviceOwnerProvisioningPreConditionLocked = checkDeviceOwnerProvisioningPreConditionLocked(null, deviceOwnerUserId, false, true);
        }
        return checkDeviceOwnerProvisioningPreConditionLocked;
    }

    private int checkManagedProfileProvisioningPreCondition(String packageName, int callingUserId) {
        if (!hasFeatureManagedUsers()) {
            return 9;
        }
        if (callingUserId == 0 && this.mInjector.userManagerIsSplitSystemUser()) {
            return 14;
        }
        if (getProfileOwner(callingUserId) != null) {
            return 2;
        }
        long ident = this.mInjector.binderClearCallingIdentity();
        try {
            UserHandle callingUserHandle = UserHandle.of(callingUserId);
            ComponentName ownerAdmin = getOwnerComponent(packageName, callingUserId);
            if (this.mUserManager.hasUserRestriction("no_add_managed_profile", callingUserHandle) && (ownerAdmin == null || isAdminAffectedByRestriction(ownerAdmin, "no_add_managed_profile", callingUserId))) {
                this.mInjector.binderRestoreCallingIdentity(ident);
                return 15;
            }
            boolean canRemoveProfile = true;
            if (this.mUserManager.hasUserRestriction("no_remove_managed_profile", callingUserHandle) && (ownerAdmin == null || isAdminAffectedByRestriction(ownerAdmin, "no_remove_managed_profile", callingUserId))) {
                canRemoveProfile = false;
            }
            if (this.mUserManager.canAddMoreManagedProfiles(callingUserId, canRemoveProfile)) {
                this.mInjector.binderRestoreCallingIdentity(ident);
                return 0;
            }
            this.mInjector.binderRestoreCallingIdentity(ident);
            return 11;
        } catch (Throwable th) {
            this.mInjector.binderRestoreCallingIdentity(ident);
        }
    }

    private ComponentName getOwnerComponent(String packageName, int userId) {
        if (isDeviceOwnerPackage(packageName, userId)) {
            return this.mOwners.getDeviceOwnerComponent();
        }
        if (isProfileOwnerPackage(packageName, userId)) {
            return this.mOwners.getProfileOwnerComponent(userId);
        }
        return null;
    }

    private ComponentName getOwnerComponent(int userId) {
        synchronized (this) {
            ComponentName deviceOwnerComponent;
            if (this.mOwners.getDeviceOwnerUserId() == userId) {
                deviceOwnerComponent = this.mOwners.getDeviceOwnerComponent();
                return deviceOwnerComponent;
            } else if (this.mOwners.hasProfileOwner(userId)) {
                deviceOwnerComponent = this.mOwners.getProfileOwnerComponent(userId);
                return deviceOwnerComponent;
            } else {
                return null;
            }
        }
    }

    private int checkManagedUserProvisioningPreCondition(int callingUserId) {
        if (!hasFeatureManagedUsers()) {
            return 9;
        }
        if (!this.mInjector.userManagerIsSplitSystemUser()) {
            return 12;
        }
        if (callingUserId == 0) {
            return 10;
        }
        if (hasUserSetupCompleted(callingUserId)) {
            return 4;
        }
        if (this.mIsWatch && hasPaired(0)) {
            return 8;
        }
        return 0;
    }

    private int checkManagedShareableDeviceProvisioningPreCondition(int callingUserId) {
        if (this.mInjector.userManagerIsSplitSystemUser()) {
            return checkDeviceOwnerProvisioningPreCondition(callingUserId);
        }
        return 12;
    }

    private boolean hasFeatureManagedUsers() {
        try {
            return this.mIPackageManager.hasSystemFeature("android.software.managed_users", 0);
        } catch (RemoteException e) {
            return false;
        }
    }

    public String getWifiMacAddress(ComponentName admin) {
        String str = null;
        synchronized (this) {
            getActiveAdminForCallerLocked(admin, -2);
        }
        long ident = this.mInjector.binderClearCallingIdentity();
        try {
            WifiInfo wifiInfo = this.mInjector.getWifiManager().getConnectionInfo();
            if (wifiInfo == null) {
                return null;
            }
            if (wifiInfo.hasRealMacAddress()) {
                str = wifiInfo.getMacAddress();
            }
            this.mInjector.binderRestoreCallingIdentity(ident);
            return str;
        } finally {
            this.mInjector.binderRestoreCallingIdentity(ident);
        }
    }

    private int getTargetSdk(String packageName, int userId) {
        try {
            ApplicationInfo ai = this.mIPackageManager.getApplicationInfo(packageName, 0, userId);
            return ai == null ? 0 : ai.targetSdkVersion;
        } catch (RemoteException e) {
            return 0;
        }
    }

    public boolean isManagedProfile(ComponentName admin) {
        enforceProfileOrDeviceOwner(admin);
        return isManagedProfile(this.mInjector.userHandleGetCallingUserId());
    }

    public boolean isSystemOnlyUser(ComponentName admin) {
        synchronized (this) {
            getActiveAdminForCallerLocked(admin, -2);
        }
        int callingUserId = this.mInjector.userHandleGetCallingUserId();
        if (UserManager.isSplitSystemUser() && callingUserId == 0) {
            return true;
        }
        return false;
    }

    public void reboot(ComponentName admin) {
        Preconditions.checkNotNull(admin);
        synchronized (this) {
            getActiveAdminForCallerLocked(admin, -2);
        }
        long ident = this.mInjector.binderClearCallingIdentity();
        try {
            if (this.mTelephonyManager.getCallState() != 0) {
                throw new IllegalStateException("Cannot be called with ongoing call on the device");
            }
            this.mInjector.powerManagerReboot("deviceowner");
        } finally {
            this.mInjector.binderRestoreCallingIdentity(ident);
        }
    }

    public void setShortSupportMessage(ComponentName who, CharSequence message) {
        if (this.mHasFeature) {
            Preconditions.checkNotNull(who, "ComponentName is null");
            int userHandle = this.mInjector.userHandleGetCallingUserId();
            synchronized (this) {
                ActiveAdmin admin = getActiveAdminForUidLocked(who, this.mInjector.binderGetCallingUid());
                if (!TextUtils.equals(admin.shortSupportMessage, message)) {
                    admin.shortSupportMessage = message;
                    saveSettingsLocked(userHandle);
                }
            }
        }
    }

    public CharSequence getShortSupportMessage(ComponentName who) {
        if (!this.mHasFeature) {
            return null;
        }
        CharSequence charSequence;
        Preconditions.checkNotNull(who, "ComponentName is null");
        synchronized (this) {
            charSequence = getActiveAdminForUidLocked(who, this.mInjector.binderGetCallingUid()).shortSupportMessage;
        }
        return charSequence;
    }

    public void setLongSupportMessage(ComponentName who, CharSequence message) {
        if (this.mHasFeature) {
            Preconditions.checkNotNull(who, "ComponentName is null");
            int userHandle = this.mInjector.userHandleGetCallingUserId();
            synchronized (this) {
                ActiveAdmin admin = getActiveAdminForUidLocked(who, this.mInjector.binderGetCallingUid());
                if (!TextUtils.equals(admin.longSupportMessage, message)) {
                    admin.longSupportMessage = message;
                    saveSettingsLocked(userHandle);
                }
            }
        }
    }

    public CharSequence getLongSupportMessage(ComponentName who) {
        if (!this.mHasFeature) {
            return null;
        }
        CharSequence charSequence;
        Preconditions.checkNotNull(who, "ComponentName is null");
        synchronized (this) {
            charSequence = getActiveAdminForUidLocked(who, this.mInjector.binderGetCallingUid()).longSupportMessage;
        }
        return charSequence;
    }

    public CharSequence getShortSupportMessageForUser(ComponentName who, int userHandle) {
        if (!this.mHasFeature) {
            return null;
        }
        Preconditions.checkNotNull(who, "ComponentName is null");
        if (isCallerWithSystemUid()) {
            synchronized (this) {
                ActiveAdmin admin = getActiveAdminUncheckedLocked(who, userHandle);
                if (admin != null) {
                    CharSequence charSequence = admin.shortSupportMessage;
                    return charSequence;
                }
                return null;
            }
        }
        throw new SecurityException("Only the system can query support message for user");
    }

    public CharSequence getLongSupportMessageForUser(ComponentName who, int userHandle) {
        if (!this.mHasFeature) {
            return null;
        }
        Preconditions.checkNotNull(who, "ComponentName is null");
        if (isCallerWithSystemUid()) {
            synchronized (this) {
                ActiveAdmin admin = getActiveAdminUncheckedLocked(who, userHandle);
                if (admin != null) {
                    CharSequence charSequence = admin.longSupportMessage;
                    return charSequence;
                }
                return null;
            }
        }
        throw new SecurityException("Only the system can query support message for user");
    }

    public void setOrganizationColor(ComponentName who, int color) {
        if (this.mHasFeature) {
            Preconditions.checkNotNull(who, "ComponentName is null");
            int userHandle = this.mInjector.userHandleGetCallingUserId();
            enforceManagedProfile(userHandle, "set organization color");
            synchronized (this) {
                getActiveAdminForCallerLocked(who, -1).organizationColor = color;
                saveSettingsLocked(userHandle);
            }
        }
    }

    public void setOrganizationColorForUser(int color, int userId) {
        if (this.mHasFeature) {
            enforceFullCrossUsersPermission(userId);
            enforceManageUsers();
            enforceManagedProfile(userId, "set organization color");
            synchronized (this) {
                getProfileOwnerAdminLocked(userId).organizationColor = color;
                saveSettingsLocked(userId);
            }
        }
    }

    public int getOrganizationColor(ComponentName who) {
        if (!this.mHasFeature) {
            return ActiveAdmin.DEF_ORGANIZATION_COLOR;
        }
        int i;
        Preconditions.checkNotNull(who, "ComponentName is null");
        enforceManagedProfile(this.mInjector.userHandleGetCallingUserId(), "get organization color");
        synchronized (this) {
            i = getActiveAdminForCallerLocked(who, -1).organizationColor;
        }
        return i;
    }

    public int getOrganizationColorForUser(int userHandle) {
        if (!this.mHasFeature) {
            return ActiveAdmin.DEF_ORGANIZATION_COLOR;
        }
        int i;
        enforceFullCrossUsersPermission(userHandle);
        enforceManagedProfile(userHandle, "get organization color");
        synchronized (this) {
            ActiveAdmin profileOwner = getProfileOwnerAdminLocked(userHandle);
            if (profileOwner != null) {
                i = profileOwner.organizationColor;
            } else {
                i = ActiveAdmin.DEF_ORGANIZATION_COLOR;
            }
        }
        return i;
    }

    public void setOrganizationName(ComponentName who, CharSequence text) {
        String str = null;
        if (this.mHasFeature) {
            Preconditions.checkNotNull(who, "ComponentName is null");
            int userHandle = this.mInjector.userHandleGetCallingUserId();
            synchronized (this) {
                ActiveAdmin admin = getActiveAdminForCallerLocked(who, -1);
                if (!TextUtils.equals(admin.organizationName, text)) {
                    if (!(text == null || text.length() == 0)) {
                        str = text.toString();
                    }
                    admin.organizationName = str;
                    saveSettingsLocked(userHandle);
                }
            }
        }
    }

    public CharSequence getOrganizationName(ComponentName who) {
        if (!this.mHasFeature) {
            return null;
        }
        CharSequence charSequence;
        Preconditions.checkNotNull(who, "ComponentName is null");
        enforceManagedProfile(this.mInjector.userHandleGetCallingUserId(), "get organization name");
        synchronized (this) {
            charSequence = getActiveAdminForCallerLocked(who, -1).organizationName;
        }
        return charSequence;
    }

    public CharSequence getDeviceOwnerOrganizationName() {
        CharSequence charSequence = null;
        if (!this.mHasFeature) {
            return null;
        }
        enforceDeviceOwnerOrManageUsers();
        synchronized (this) {
            ActiveAdmin deviceOwnerAdmin = getDeviceOwnerAdminLocked();
            if (deviceOwnerAdmin != null) {
                charSequence = deviceOwnerAdmin.organizationName;
            }
        }
        return charSequence;
    }

    public CharSequence getOrganizationNameForUser(int userHandle) {
        CharSequence charSequence = null;
        if (!this.mHasFeature) {
            return null;
        }
        enforceFullCrossUsersPermission(userHandle);
        enforceManagedProfile(userHandle, "get organization name");
        synchronized (this) {
            ActiveAdmin profileOwner = getProfileOwnerAdminLocked(userHandle);
            if (profileOwner != null) {
                charSequence = profileOwner.organizationName;
            }
        }
        return charSequence;
    }

    public void setAffiliationIds(ComponentName admin, List<String> ids) {
        if (!this.mHasFeature) {
            return;
        }
        if (ids == null) {
            throw new IllegalArgumentException("ids must not be null");
        }
        for (String id : ids) {
            if (TextUtils.isEmpty(id)) {
                throw new IllegalArgumentException("ids must not contain empty string");
            }
        }
        Set<String> affiliationIds = new ArraySet(ids);
        int callingUserId = this.mInjector.userHandleGetCallingUserId();
        synchronized (this) {
            getActiveAdminForCallerLocked(admin, -1);
            getUserData(callingUserId).mAffiliationIds = affiliationIds;
            saveSettingsLocked(callingUserId);
            if (callingUserId != 0 && isDeviceOwner(admin, callingUserId)) {
                getUserData(0).mAffiliationIds = affiliationIds;
                saveSettingsLocked(0);
            }
            maybePauseDeviceWideLoggingLocked();
            maybeResumeDeviceWideLoggingLocked();
            maybeClearLockTaskPackagesLocked();
        }
    }

    public List<String> getAffiliationIds(ComponentName admin) {
        if (!this.mHasFeature) {
            return Collections.emptyList();
        }
        List arrayList;
        Preconditions.checkNotNull(admin);
        synchronized (this) {
            getActiveAdminForCallerLocked(admin, -1);
            arrayList = new ArrayList(getUserData(this.mInjector.userHandleGetCallingUserId()).mAffiliationIds);
        }
        return arrayList;
    }

    public boolean isAffiliatedUser() {
        if (!this.mHasFeature) {
            return false;
        }
        boolean isUserAffiliatedWithDeviceLocked;
        synchronized (this) {
            isUserAffiliatedWithDeviceLocked = isUserAffiliatedWithDeviceLocked(this.mInjector.userHandleGetCallingUserId());
        }
        return isUserAffiliatedWithDeviceLocked;
    }

    private boolean isUserAffiliatedWithDeviceLocked(int userId) {
        if (!this.mOwners.hasDeviceOwner()) {
            return false;
        }
        if (userId == this.mOwners.getDeviceOwnerUserId() || userId == 0) {
            return true;
        }
        if (getProfileOwner(userId) == null) {
            return false;
        }
        Set<String> userAffiliationIds = getUserData(userId).mAffiliationIds;
        Set<String> deviceAffiliationIds = getUserData(0).mAffiliationIds;
        for (String id : userAffiliationIds) {
            if (deviceAffiliationIds.contains(id)) {
                return true;
            }
        }
        return false;
    }

    private boolean areAllUsersAffiliatedWithDeviceLocked() {
        long ident = this.mInjector.binderClearCallingIdentity();
        try {
            List<UserInfo> userInfos = this.mUserManager.getUsers(true);
            int i = 0;
            while (i < userInfos.size()) {
                int userId = ((UserInfo) userInfos.get(i)).id;
                if (isUserAffiliatedWithDeviceLocked(userId)) {
                    i++;
                } else {
                    Slog.d(LOG_TAG, "User id " + userId + " not affiliated.");
                    return false;
                }
            }
            this.mInjector.binderRestoreCallingIdentity(ident);
            return true;
        } finally {
            this.mInjector.binderRestoreCallingIdentity(ident);
        }
    }

    /* JADX WARNING: Missing block: B:16:0x0027, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setSecurityLoggingEnabled(ComponentName admin, boolean enabled) {
        if (this.mHasFeature) {
            Preconditions.checkNotNull(admin);
            synchronized (this) {
                getActiveAdminForCallerLocked(admin, -2);
                if (enabled == this.mInjector.securityLogGetLoggingEnabledProperty()) {
                    return;
                }
                this.mInjector.securityLogSetLoggingEnabledProperty(enabled);
                if (enabled) {
                    this.mSecurityLogMonitor.start();
                    maybePauseDeviceWideLoggingLocked();
                } else {
                    this.mSecurityLogMonitor.stop();
                }
            }
        }
    }

    public boolean isSecurityLoggingEnabled(ComponentName admin) {
        if (!this.mHasFeature) {
            return false;
        }
        boolean securityLogGetLoggingEnabledProperty;
        synchronized (this) {
            if (!isCallerWithSystemUid()) {
                Preconditions.checkNotNull(admin);
                getActiveAdminForCallerLocked(admin, -2);
            }
            securityLogGetLoggingEnabledProperty = this.mInjector.securityLogGetLoggingEnabledProperty();
        }
        return securityLogGetLoggingEnabledProperty;
    }

    private synchronized void recordSecurityLogRetrievalTime() {
        long currentTime = System.currentTimeMillis();
        DevicePolicyData policyData = getUserData(0);
        if (currentTime > policyData.mLastSecurityLogRetrievalTime) {
            policyData.mLastSecurityLogRetrievalTime = currentTime;
            saveSettingsLocked(0);
        }
    }

    public ParceledListSlice<SecurityEvent> retrievePreRebootSecurityLogs(ComponentName admin) {
        if (!this.mHasFeature) {
            return null;
        }
        Preconditions.checkNotNull(admin);
        ensureDeviceOwnerAndAllUsersAffiliated(admin);
        if (!this.mContext.getResources().getBoolean(17957021) || (this.mInjector.securityLogGetLoggingEnabledProperty() ^ 1) != 0) {
            return null;
        }
        recordSecurityLogRetrievalTime();
        ArrayList<SecurityEvent> output = new ArrayList();
        try {
            SecurityLog.readPreviousEvents(output);
            return new ParceledListSlice(output);
        } catch (IOException e) {
            Slog.w(LOG_TAG, "Fail to read previous events", e);
            return new ParceledListSlice(Collections.emptyList());
        }
    }

    public ParceledListSlice<SecurityEvent> retrieveSecurityLogs(ComponentName admin) {
        ParceledListSlice<SecurityEvent> parceledListSlice = null;
        if (!this.mHasFeature) {
            return null;
        }
        Preconditions.checkNotNull(admin);
        ensureDeviceOwnerAndAllUsersAffiliated(admin);
        if (!this.mInjector.securityLogGetLoggingEnabledProperty()) {
            return null;
        }
        recordSecurityLogRetrievalTime();
        List<SecurityEvent> logs = this.mSecurityLogMonitor.retrieveLogs();
        if (logs != null) {
            parceledListSlice = new ParceledListSlice(logs);
        }
        return parceledListSlice;
    }

    private void enforceCanManageDeviceAdmin() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_DEVICE_ADMINS", null);
    }

    private void enforceCanManageProfileAndDeviceOwners() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_PROFILE_AND_DEVICE_OWNERS", null);
    }

    private void enforceCallerSystemUserHandle() {
        if (UserHandle.getUserId(this.mInjector.binderGetCallingUid()) != 0) {
            throw new SecurityException("Caller has to be in user 0");
        }
    }

    public boolean isUninstallInQueue(String packageName) {
        boolean contains;
        enforceCanManageDeviceAdmin();
        Pair<String, Integer> packageUserPair = new Pair(packageName, Integer.valueOf(this.mInjector.userHandleGetCallingUserId()));
        synchronized (this) {
            contains = this.mPackagesToRemove.contains(packageUserPair);
        }
        return contains;
    }

    public void uninstallPackageWithActiveAdmins(final String packageName) {
        enforceCanManageDeviceAdmin();
        Preconditions.checkArgument(TextUtils.isEmpty(packageName) ^ 1);
        final int userId = this.mInjector.userHandleGetCallingUserId();
        enforceUserUnlocked(userId);
        ComponentName profileOwner = getProfileOwner(userId);
        if (profileOwner == null || !packageName.equals(profileOwner.getPackageName())) {
            ComponentName deviceOwner = getDeviceOwnerComponent(false);
            if (getDeviceOwnerUserId() == userId && deviceOwner != null && packageName.equals(deviceOwner.getPackageName())) {
                throw new IllegalArgumentException("Cannot uninstall a package with a device owner");
            }
            Pair<String, Integer> packageUserPair = new Pair(packageName, Integer.valueOf(userId));
            synchronized (this) {
                this.mPackagesToRemove.add(packageUserPair);
            }
            List<ComponentName> allActiveAdmins = getActiveAdmins(userId);
            final List<ComponentName> packageActiveAdmins = new ArrayList();
            if (allActiveAdmins != null) {
                for (ComponentName activeAdmin : allActiveAdmins) {
                    if (packageName.equals(activeAdmin.getPackageName())) {
                        packageActiveAdmins.add(activeAdmin);
                        removeActiveAdmin(activeAdmin, userId);
                    }
                }
            }
            if (packageActiveAdmins.size() == 0) {
                startUninstallIntent(packageName, userId);
                return;
            } else {
                this.mHandler.postDelayed(new Runnable() {
                    public void run() {
                        for (ComponentName activeAdmin : packageActiveAdmins) {
                            DevicePolicyManagerService.this.removeAdminArtifacts(activeAdmin, userId);
                        }
                        DevicePolicyManagerService.this.startUninstallIntent(packageName, userId);
                    }
                }, JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY);
                return;
            }
        }
        throw new IllegalArgumentException("Cannot uninstall a package with a profile owner");
    }

    public boolean isDeviceProvisioned() {
        boolean z;
        synchronized (this) {
            z = getUserDataUnchecked(0).mUserSetupComplete;
        }
        return z;
    }

    private void removePackageIfRequired(String packageName, int userId) {
        if (!packageHasActiveAdmins(packageName, userId)) {
            startUninstallIntent(packageName, userId);
        }
    }

    /* JADX WARNING: Missing block: B:12:0x0025, code:
            if (r6.mInjector.getIPackageManager().getPackageInfo(r7, 0, r8) != null) goto L_0x0035;
     */
    /* JADX WARNING: Missing block: B:13:0x0027, code:
            return;
     */
    /* JADX WARNING: Missing block: B:18:0x002c, code:
            android.util.Log.e(LOG_TAG, "Failure talking to PackageManager while getting package info");
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void startUninstallIntent(String packageName, int userId) {
        Pair<String, Integer> packageUserPair = new Pair(packageName, Integer.valueOf(userId));
        synchronized (this) {
            if (this.mPackagesToRemove.contains(packageUserPair)) {
                this.mPackagesToRemove.remove(packageUserPair);
            } else {
                return;
            }
        }
        Intent uninstallIntent;
        try {
            this.mInjector.getIActivityManager().forceStopPackage(packageName, userId);
        } catch (RemoteException e) {
            Log.e(LOG_TAG, "Failure talking to ActivityManager while force stopping package");
        }
        uninstallIntent = new Intent("android.intent.action.UNINSTALL_PACKAGE", Uri.parse("package:" + packageName));
        uninstallIntent.setFlags(268435456);
        this.mContext.startActivityAsUser(uninstallIntent, UserHandle.of(userId));
        uninstallIntent = new Intent("android.intent.action.UNINSTALL_PACKAGE", Uri.parse("package:" + packageName));
        uninstallIntent.setFlags(268435456);
        this.mContext.startActivityAsUser(uninstallIntent, UserHandle.of(userId));
    }

    private void removeAdminArtifacts(ComponentName adminReceiver, int userHandle) {
        synchronized (this) {
            ActiveAdmin admin = getActiveAdminUncheckedLocked(adminReceiver, userHandle);
            if (admin == null) {
                return;
            }
            DevicePolicyData policy = getUserData(userHandle);
            boolean doProxyCleanup = admin.info.usesPolicy(5);
            policy.mAdminList.remove(admin);
            policy.mAdminMap.remove(adminReceiver);
            validatePasswordOwnerLocked(policy);
            if (doProxyCleanup) {
                resetGlobalProxyLocked(policy);
            }
            saveSettingsLocked(userHandle);
            updateMaximumTimeToLockLocked(userHandle);
            policy.mRemovingAdmins.remove(adminReceiver);
            Slog.i(LOG_TAG, "Device admin " + adminReceiver + " removed from user " + userHandle);
            syncHwDeviceSettingsLocked(policy.mUserHandle);
            removeActiveAdminCompleted(adminReceiver);
            pushUserRestrictions(userHandle);
        }
    }

    public void setDeviceProvisioningConfigApplied() {
        enforceManageUsers();
        synchronized (this) {
            getUserData(0).mDeviceProvisioningConfigApplied = true;
            saveSettingsLocked(0);
        }
    }

    public boolean isDeviceProvisioningConfigApplied() {
        boolean z;
        enforceManageUsers();
        synchronized (this) {
            z = getUserData(0).mDeviceProvisioningConfigApplied;
        }
        return z;
    }

    public void forceUpdateUserSetupComplete() {
        enforceCanManageProfileAndDeviceOwners();
        enforceCallerSystemUserHandle();
        if (this.mInjector.isBuildDebuggable()) {
            getUserData(0).mUserSetupComplete = this.mInjector.settingsSecureGetIntForUser("user_setup_complete", 0, 0) != 0;
            synchronized (this) {
                saveSettingsLocked(0);
            }
        }
    }

    public void setBackupServiceEnabled(ComponentName admin, boolean enabled) {
        if (this.mHasFeature) {
            Preconditions.checkNotNull(admin);
            synchronized (this) {
                getActiveAdminForCallerLocked(admin, -2);
            }
            long ident = this.mInjector.binderClearCallingIdentity();
            try {
                IBackupManager ibm = this.mInjector.getIBackupManager();
                if (ibm != null) {
                    ibm.setBackupServiceActive(0, enabled);
                }
                this.mInjector.binderRestoreCallingIdentity(ident);
            } catch (RemoteException e) {
                throw new IllegalStateException("Failed " + (enabled ? "" : "de") + "activating backup service.", e);
            } catch (Throwable th) {
                this.mInjector.binderRestoreCallingIdentity(ident);
            }
        }
    }

    public boolean isBackupServiceEnabled(ComponentName admin) {
        boolean z = false;
        Preconditions.checkNotNull(admin);
        if (!this.mHasFeature) {
            return true;
        }
        synchronized (this) {
            try {
                getActiveAdminForCallerLocked(admin, -2);
                IBackupManager ibm = this.mInjector.getIBackupManager();
                if (ibm != null) {
                    z = ibm.isBackupServiceActive(0);
                }
            } catch (RemoteException e) {
                throw new IllegalStateException("Failed requesting backup service state.", e);
            }
        }
        return z;
    }

    public boolean bindDeviceAdminServiceAsUser(ComponentName admin, IApplicationThread caller, IBinder activtiyToken, Intent serviceIntent, IServiceConnection connection, int flags, int targetUserId) {
        if (!this.mHasFeature) {
            return false;
        }
        Preconditions.checkNotNull(admin);
        Preconditions.checkNotNull(caller);
        Preconditions.checkNotNull(serviceIntent);
        boolean z = (serviceIntent.getComponent() == null && serviceIntent.getPackage() == null) ? false : true;
        Preconditions.checkArgument(z, "Service intent must be explicit (with a package name or component): " + serviceIntent);
        Preconditions.checkNotNull(connection);
        Preconditions.checkArgument(this.mInjector.userHandleGetCallingUserId() != targetUserId, "target user id must be different from the calling user id");
        if (getBindDeviceAdminTargetUsers(admin).contains(UserHandle.of(targetUserId))) {
            String targetPackage;
            synchronized (this) {
                targetPackage = getOwnerPackageNameForUserLocked(targetUserId);
            }
            long callingIdentity = this.mInjector.binderClearCallingIdentity();
            try {
                if (createCrossUserServiceIntent(serviceIntent, targetPackage, targetUserId) == null) {
                    return false;
                }
                z = this.mInjector.getIActivityManager().bindService(caller, activtiyToken, serviceIntent, serviceIntent.resolveTypeIfNeeded(this.mContext.getContentResolver()), connection, flags, this.mContext.getOpPackageName(), targetUserId) != 0;
                this.mInjector.binderRestoreCallingIdentity(callingIdentity);
                return z;
            } catch (RemoteException e) {
                return false;
            } finally {
                this.mInjector.binderRestoreCallingIdentity(callingIdentity);
                return false;
            }
        }
        throw new SecurityException("Not allowed to bind to target user id");
    }

    public List<UserHandle> getBindDeviceAdminTargetUsers(ComponentName admin) {
        if (!this.mHasFeature) {
            return Collections.emptyList();
        }
        ArrayList<UserHandle> targetUsers;
        Preconditions.checkNotNull(admin);
        synchronized (this) {
            getActiveAdminForCallerLocked(admin, -1);
            int callingUserId = this.mInjector.userHandleGetCallingUserId();
            long callingIdentity = this.mInjector.binderClearCallingIdentity();
            try {
                targetUsers = new ArrayList();
                if (isDeviceOwner(admin, callingUserId)) {
                    List<UserInfo> userInfos = this.mUserManager.getUsers(true);
                    for (int i = 0; i < userInfos.size(); i++) {
                        int userId = ((UserInfo) userInfos.get(i)).id;
                        if (userId != callingUserId && canUserBindToDeviceOwnerLocked(userId)) {
                            targetUsers.add(UserHandle.of(userId));
                        }
                    }
                } else if (canUserBindToDeviceOwnerLocked(callingUserId)) {
                    targetUsers.add(UserHandle.of(this.mOwners.getDeviceOwnerUserId()));
                }
                this.mInjector.binderRestoreCallingIdentity(callingIdentity);
            } catch (Throwable th) {
                this.mInjector.binderRestoreCallingIdentity(callingIdentity);
            }
        }
        return targetUsers;
    }

    /* JADX WARNING: Missing block: B:4:0x0011, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean canUserBindToDeviceOwnerLocked(int userId) {
        if (this.mOwners.hasDeviceOwner() && userId != this.mOwners.getDeviceOwnerUserId() && this.mOwners.hasProfileOwner(userId) && (TextUtils.equals(this.mOwners.getDeviceOwnerPackageName(), this.mOwners.getProfileOwnerPackage(userId)) ^ 1) == 0) {
            return isUserAffiliatedWithDeviceLocked(userId);
        }
        return false;
    }

    /* JADX WARNING: Missing block: B:12:0x003e, code:
            wtfIfInLock();
            r8 = r12.mInjector.binderClearCallingIdentity();
     */
    /* JADX WARNING: Missing block: B:14:?, code:
            r2 = android.accounts.AccountManager.get(r12.mContext);
            r1 = r2.getAccountsAsUser(r13);
     */
    /* JADX WARNING: Missing block: B:15:0x0052, code:
            if (r1.length != 0) goto L_0x005e;
     */
    /* JADX WARNING: Missing block: B:22:?, code:
            monitor-enter(r12);
     */
    /* JADX WARNING: Missing block: B:23:0x005f, code:
            if (r14 == null) goto L_0x0069;
     */
    /* JADX WARNING: Missing block: B:26:0x0067, code:
            if ((isAdminTestOnlyLocked(r14, r13) ^ 1) == 0) goto L_0x007a;
     */
    /* JADX WARNING: Missing block: B:27:0x0069, code:
            android.util.Log.w(LOG_TAG, "Non test-only owner can't be installed with existing accounts.");
     */
    /* JADX WARNING: Missing block: B:29:?, code:
            monitor-exit(r12);
     */
    /* JADX WARNING: Missing block: B:30:0x0073, code:
            r12.mInjector.binderRestoreCallingIdentity(r8);
     */
    /* JADX WARNING: Missing block: B:31:0x0079, code:
            return true;
     */
    /* JADX WARNING: Missing block: B:33:?, code:
            monitor-exit(r12);
     */
    /* JADX WARNING: Missing block: B:34:0x007b, code:
            r5 = new java.lang.String[]{"android.account.DEVICE_OR_PROFILE_OWNER_ALLOWED"};
            r6 = new java.lang.String[]{"android.account.DEVICE_OR_PROFILE_OWNER_DISALLOWED"};
            r4 = true;
            r7 = 0;
            r10 = r1.length;
     */
    /* JADX WARNING: Missing block: B:35:0x0090, code:
            if (r7 >= r10) goto L_0x00bc;
     */
    /* JADX WARNING: Missing block: B:36:0x0092, code:
            r0 = r1[r7];
     */
    /* JADX WARNING: Missing block: B:37:0x0098, code:
            if (hasAccountFeatures(r2, r0, r6) == false) goto L_0x00d9;
     */
    /* JADX WARNING: Missing block: B:38:0x009a, code:
            android.util.Log.e(LOG_TAG, r0 + " has " + r6[0]);
            r4 = false;
     */
    /* JADX WARNING: Missing block: B:39:0x00bc, code:
            if (r4 == false) goto L_0x0105;
     */
    /* JADX WARNING: Missing block: B:40:0x00be, code:
            android.util.Log.w(LOG_TAG, "All accounts are compatible");
     */
    /* JADX WARNING: Missing block: B:41:0x00c7, code:
            r7 = r4 ^ 1;
            r12.mInjector.binderRestoreCallingIdentity(r8);
     */
    /* JADX WARNING: Missing block: B:42:0x00ce, code:
            return r7;
     */
    /* JADX WARNING: Missing block: B:48:0x00d3, code:
            r12.mInjector.binderRestoreCallingIdentity(r8);
     */
    /* JADX WARNING: Missing block: B:51:0x00dd, code:
            if (hasAccountFeatures(r2, r0, r5) != false) goto L_0x0102;
     */
    /* JADX WARNING: Missing block: B:52:0x00df, code:
            android.util.Log.e(LOG_TAG, r0 + " doesn't have " + r5[0]);
            r4 = false;
     */
    /* JADX WARNING: Missing block: B:53:0x0102, code:
            r7 = r7 + 1;
     */
    /* JADX WARNING: Missing block: B:54:0x0105, code:
            android.util.Log.e(LOG_TAG, "Found incompatible accounts");
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean hasIncompatibleAccountsOrNonAdbNoLock(int userId, ComponentName owner) {
        int callingUid = this.mInjector.binderGetCallingUid();
        synchronized (this) {
            Slog.w(LOG_TAG, "hasIncompatibleAccountsOrNonAdbNoLock mIsMDMDeviceOwnerAPI=" + this.mIsMDMDeviceOwnerAPI + ",callingUid =" + callingUid);
            if (isAdb() || (this.mIsMDMDeviceOwnerAPI ^ 1) == 0) {
            } else {
                return true;
            }
        }
        return false;
    }

    private boolean hasAccountFeatures(AccountManager am, Account account, String[] features) {
        try {
            return ((Boolean) am.hasFeatures(account, features, null, null).getResult()).booleanValue();
        } catch (Exception e) {
            Log.w(LOG_TAG, "Failed to get account feature", e);
            return false;
        }
    }

    private boolean isAdb() {
        int callingUid = this.mInjector.binderGetCallingUid();
        if (callingUid == IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME || callingUid == 0) {
            return true;
        }
        return false;
    }

    public synchronized void setNetworkLoggingEnabled(ComponentName admin, boolean enabled) {
        if (this.mHasFeature) {
            Preconditions.checkNotNull(admin);
            getActiveAdminForCallerLocked(admin, -2);
            if (enabled != isNetworkLoggingEnabledInternalLocked()) {
                ActiveAdmin deviceOwner = getDeviceOwnerAdminLocked();
                deviceOwner.isNetworkLoggingEnabled = enabled;
                if (!enabled) {
                    deviceOwner.numNetworkLoggingNotifications = 0;
                    deviceOwner.lastNetworkLoggingNotificationTimeMs = 0;
                }
                saveSettingsLocked(this.mInjector.userHandleGetCallingUserId());
                setNetworkLoggingActiveInternal(enabled);
            }
        }
    }

    private synchronized void setNetworkLoggingActiveInternal(boolean active) {
        long callingIdentity = this.mInjector.binderClearCallingIdentity();
        if (active) {
            try {
                this.mNetworkLogger = new NetworkLogger(this, this.mInjector.getPackageManagerInternal());
                if (!this.mNetworkLogger.startNetworkLogging()) {
                    this.mNetworkLogger = null;
                    Slog.wtf(LOG_TAG, "Network logging could not be started due to the logging service not being available yet.");
                }
                maybePauseDeviceWideLoggingLocked();
                sendNetworkLoggingNotificationLocked();
            } catch (Throwable th) {
                this.mInjector.binderRestoreCallingIdentity(callingIdentity);
            }
        } else {
            if (!(this.mNetworkLogger == null || (this.mNetworkLogger.stopNetworkLogging() ^ 1) == 0)) {
                Slog.wtf(LOG_TAG, "Network logging could not be stopped due to the logging service not being available yet.");
            }
            this.mNetworkLogger = null;
            this.mInjector.getNotificationManager().cancel(1002);
        }
        this.mInjector.binderRestoreCallingIdentity(callingIdentity);
    }

    private void maybePauseDeviceWideLoggingLocked() {
        if (!areAllUsersAffiliatedWithDeviceLocked()) {
            Slog.i(LOG_TAG, "There are unaffiliated users, security and network logging will be paused if enabled.");
            this.mSecurityLogMonitor.pause();
            if (this.mNetworkLogger != null) {
                this.mNetworkLogger.pause();
            }
        }
    }

    private void maybeResumeDeviceWideLoggingLocked() {
        if (areAllUsersAffiliatedWithDeviceLocked()) {
            long ident = this.mInjector.binderClearCallingIdentity();
            try {
                this.mSecurityLogMonitor.resume();
                if (this.mNetworkLogger != null) {
                    this.mNetworkLogger.resume();
                }
                this.mInjector.binderRestoreCallingIdentity(ident);
            } catch (Throwable th) {
                this.mInjector.binderRestoreCallingIdentity(ident);
            }
        }
    }

    private void discardDeviceWideLogsLocked() {
        this.mSecurityLogMonitor.discardLogs();
        if (this.mNetworkLogger != null) {
            this.mNetworkLogger.discardLogs();
        }
    }

    public boolean isNetworkLoggingEnabled(ComponentName admin) {
        if (!this.mHasFeature) {
            return false;
        }
        boolean isNetworkLoggingEnabledInternalLocked;
        synchronized (this) {
            enforceDeviceOwnerOrManageUsers();
            isNetworkLoggingEnabledInternalLocked = isNetworkLoggingEnabledInternalLocked();
        }
        return isNetworkLoggingEnabledInternalLocked;
    }

    private boolean isNetworkLoggingEnabledInternalLocked() {
        ActiveAdmin deviceOwner = getDeviceOwnerAdminLocked();
        return deviceOwner != null ? deviceOwner.isNetworkLoggingEnabled : false;
    }

    /* JADX WARNING: Missing block: B:12:0x001a, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public List<NetworkEvent> retrieveNetworkLogs(ComponentName admin, long batchToken) {
        if (!this.mHasFeature) {
            return null;
        }
        Preconditions.checkNotNull(admin);
        ensureDeviceOwnerAndAllUsersAffiliated(admin);
        synchronized (this) {
            if (this.mNetworkLogger == null || (isNetworkLoggingEnabledInternalLocked() ^ 1) != 0) {
            } else {
                long currentTime = System.currentTimeMillis();
                DevicePolicyData policyData = getUserData(0);
                if (currentTime > policyData.mLastNetworkLogsRetrievalTime) {
                    policyData.mLastNetworkLogsRetrievalTime = currentTime;
                    saveSettingsLocked(0);
                }
                List<NetworkEvent> retrieveLogs = this.mNetworkLogger.retrieveLogs(batchToken);
                return retrieveLogs;
            }
        }
    }

    /* JADX WARNING: Missing block: B:4:0x0014, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void sendNetworkLoggingNotificationLocked() {
        ActiveAdmin deviceOwner = getDeviceOwnerAdminLocked();
        if (deviceOwner != null && (deviceOwner.isNetworkLoggingEnabled ^ 1) == 0 && deviceOwner.numNetworkLoggingNotifications < 2) {
            long now = System.currentTimeMillis();
            if (now - deviceOwner.lastNetworkLoggingNotificationTimeMs >= MS_PER_DAY) {
                deviceOwner.numNetworkLoggingNotifications++;
                if (deviceOwner.numNetworkLoggingNotifications >= 2) {
                    deviceOwner.lastNetworkLoggingNotificationTimeMs = 0;
                } else {
                    deviceOwner.lastNetworkLoggingNotificationTimeMs = now;
                }
                Intent intent = new Intent("android.app.action.SHOW_DEVICE_MONITORING_DIALOG");
                intent.setPackage("com.android.systemui");
                this.mInjector.getNotificationManager().notify(1002, new Builder(this.mContext, SystemNotificationChannels.DEVICE_ADMIN).setSmallIcon(17302389).setContentTitle(this.mContext.getString(17040466)).setContentText(this.mContext.getString(17040465)).setTicker(this.mContext.getString(17040466)).setShowWhen(true).setContentIntent(PendingIntent.getBroadcastAsUser(this.mContext, 0, intent, 0, UserHandle.CURRENT)).setStyle(new BigTextStyle().bigText(this.mContext.getString(17040465))).build());
                saveSettingsLocked(this.mOwners.getDeviceOwnerUserId());
            }
        }
    }

    private String getOwnerPackageNameForUserLocked(int userId) {
        if (this.mOwners.getDeviceOwnerUserId() == userId) {
            return this.mOwners.getDeviceOwnerPackageName();
        }
        return this.mOwners.getProfileOwnerPackage(userId);
    }

    private Intent createCrossUserServiceIntent(Intent rawIntent, String expectedPackageName, int targetUserId) throws RemoteException, SecurityException {
        ResolveInfo info = this.mIPackageManager.resolveService(rawIntent, rawIntent.resolveTypeIfNeeded(this.mContext.getContentResolver()), 0, targetUserId);
        if (info == null || info.serviceInfo == null) {
            Log.e(LOG_TAG, "Fail to look up the service: " + rawIntent + " or user " + targetUserId + " is not running");
            return null;
        } else if (!expectedPackageName.equals(info.serviceInfo.packageName)) {
            throw new SecurityException("Only allow to bind service in " + expectedPackageName);
        } else if (!info.serviceInfo.exported || ("android.permission.BIND_DEVICE_ADMIN".equals(info.serviceInfo.permission) ^ 1) == 0) {
            rawIntent.setComponent(info.serviceInfo.getComponentName());
            return rawIntent;
        } else {
            throw new SecurityException("Service must be protected by BIND_DEVICE_ADMIN permission");
        }
    }

    public long getLastSecurityLogRetrievalTime() {
        enforceDeviceOwnerOrManageUsers();
        return getUserData(0).mLastSecurityLogRetrievalTime;
    }

    public long getLastBugReportRequestTime() {
        enforceDeviceOwnerOrManageUsers();
        return getUserData(0).mLastBugReportRequestTime;
    }

    public long getLastNetworkLogRetrievalTime() {
        enforceDeviceOwnerOrManageUsers();
        return getUserData(0).mLastNetworkLogsRetrievalTime;
    }

    public boolean setResetPasswordToken(ComponentName admin, byte[] token) {
        boolean z = false;
        if (!this.mHasFeature) {
            return false;
        }
        if (token == null || token.length < 32) {
            throw new IllegalArgumentException("token must be at least 32-byte long");
        }
        synchronized (this) {
            int userHandle = this.mInjector.userHandleGetCallingUserId();
            getActiveAdminForCallerLocked(admin, -1);
            DevicePolicyData policy = getUserData(userHandle);
            long ident = this.mInjector.binderClearCallingIdentity();
            try {
                if (policy.mPasswordTokenHandle != 0) {
                    this.mLockPatternUtils.removeEscrowToken(policy.mPasswordTokenHandle, userHandle);
                }
                policy.mPasswordTokenHandle = this.mLockPatternUtils.addEscrowToken(token, userHandle);
                saveSettingsLocked(userHandle);
                if (policy.mPasswordTokenHandle != 0) {
                    z = true;
                }
                this.mInjector.binderRestoreCallingIdentity(ident);
            } catch (Throwable th) {
                this.mInjector.binderRestoreCallingIdentity(ident);
            }
        }
        return z;
    }

    public boolean clearResetPasswordToken(ComponentName admin) {
        if (!this.mHasFeature) {
            return false;
        }
        synchronized (this) {
            int userHandle = this.mInjector.userHandleGetCallingUserId();
            getActiveAdminForCallerLocked(admin, -1);
            DevicePolicyData policy = getUserData(userHandle);
            if (policy.mPasswordTokenHandle != 0) {
                long ident = this.mInjector.binderClearCallingIdentity();
                try {
                    boolean result = this.mLockPatternUtils.removeEscrowToken(policy.mPasswordTokenHandle, userHandle);
                    policy.mPasswordTokenHandle = 0;
                    saveSettingsLocked(userHandle);
                    this.mInjector.binderRestoreCallingIdentity(ident);
                    return result;
                } catch (Throwable th) {
                    this.mInjector.binderRestoreCallingIdentity(ident);
                }
            } else {
                return false;
            }
        }
    }

    public boolean isResetPasswordTokenActive(ComponentName admin) {
        synchronized (this) {
            int userHandle = this.mInjector.userHandleGetCallingUserId();
            getActiveAdminForCallerLocked(admin, -1);
            DevicePolicyData policy = getUserData(userHandle);
            if (policy.mPasswordTokenHandle != 0) {
                long ident = this.mInjector.binderClearCallingIdentity();
                try {
                    boolean isEscrowTokenActive = this.mLockPatternUtils.isEscrowTokenActive(policy.mPasswordTokenHandle, userHandle);
                    this.mInjector.binderRestoreCallingIdentity(ident);
                    return isEscrowTokenActive;
                } catch (Throwable th) {
                    this.mInjector.binderRestoreCallingIdentity(ident);
                }
            } else {
                return false;
            }
        }
    }

    public boolean resetPasswordWithToken(ComponentName admin, String passwordOrNull, byte[] token, int flags) {
        Preconditions.checkNotNull(token);
        synchronized (this) {
            int userHandle = this.mInjector.userHandleGetCallingUserId();
            getActiveAdminForCallerLocked(admin, -1);
            DevicePolicyData policy = getUserData(userHandle);
            if (policy.mPasswordTokenHandle != 0) {
                boolean resetPasswordInternal = resetPasswordInternal(passwordOrNull != null ? passwordOrNull : "", policy.mPasswordTokenHandle, token, flags, this.mInjector.binderGetCallingUid(), userHandle);
                return resetPasswordInternal;
            }
            Slog.w(LOG_TAG, "No saved token handle");
            return false;
        }
    }

    public boolean isCurrentInputMethodSetByOwner() {
        enforceProfileOwnerOrSystemUser();
        return getUserData(this.mInjector.userHandleGetCallingUserId()).mCurrentInputMethodSet;
    }

    public StringParceledListSlice getOwnerInstalledCaCerts(UserHandle user) {
        StringParceledListSlice stringParceledListSlice;
        int userId = user.getIdentifier();
        enforceProfileOwnerOrFullCrossUsersPermission(userId);
        synchronized (this) {
            stringParceledListSlice = new StringParceledListSlice(new ArrayList(getUserData(userId).mOwnerInstalledCaCerts));
        }
        return stringParceledListSlice;
    }

    /* JADX WARNING: Removed duplicated region for block: B:2:0x0008 A:{Splitter: B:0:0x0000, ExcHandler: java.io.IOException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:2:0x0008, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:3:0x0009, code:
            android.util.Slog.w(LOG_TAG, "Failed requesting data wipe", r0);
     */
    /* JADX WARNING: Missing block: B:5:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void clearWipeDataFactoryLowlevel(String reason) {
        try {
            this.mInjector.recoverySystemRebootWipeUserData(false, reason, true);
        } catch (Exception e) {
        }
    }
}
