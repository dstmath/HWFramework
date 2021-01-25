package com.android.server.devicepolicy;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ActivityManager;
import android.app.ActivityManagerInternal;
import android.app.ActivityTaskManager;
import android.app.ActivityThread;
import android.app.AlarmManager;
import android.app.AppGlobals;
import android.app.BroadcastOptions;
import android.app.IActivityManager;
import android.app.IActivityTaskManager;
import android.app.IApplicationThread;
import android.app.IServiceConnection;
import android.app.IStopUserCallback;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.admin.DeviceAdminInfo;
import android.app.admin.DevicePolicyCache;
import android.app.admin.DevicePolicyEventLogger;
import android.app.admin.DevicePolicyManagerInternal;
import android.app.admin.NetworkEvent;
import android.app.admin.PasswordMetrics;
import android.app.admin.SecurityLog;
import android.app.admin.StartInstallingUpdateCallback;
import android.app.admin.SystemUpdateInfo;
import android.app.admin.SystemUpdatePolicy;
import android.app.backup.IBackupManager;
import android.app.trust.TrustManager;
import android.app.usage.UsageStatsManagerInternal;
import android.common.HwFrameworkFactory;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.PermissionChecker;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageDataObserver;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManagerInternal;
import android.content.pm.ParceledListSlice;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.StringParceledListSlice;
import android.content.pm.SuspendDialogInfo;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.IAudioService;
import android.net.ConnectivityManager;
import android.net.IIpConnectivityMetrics;
import android.net.NetworkUtils;
import android.net.ProxyInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.PersistableBundle;
import android.os.PowerManager;
import android.os.PowerManagerInternal;
import android.os.Process;
import android.os.RecoverySystem;
import android.os.RemoteCallback;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.ServiceSpecificException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.UserManagerInternal;
import android.os.storage.StorageManager;
import android.permission.PermissionControllerManager;
import android.provider.ContactsContract;
import android.provider.ContactsInternal;
import android.provider.Settings;
import android.provider.Telephony;
import android.security.IKeyChainAliasCallback;
import android.security.IKeyChainService;
import android.security.KeyChain;
import android.security.keymaster.KeymasterCertificateChain;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.ParcelableKeyGenParameterSpec;
import android.service.persistentdata.PersistentDataBlockManager;
import android.telephony.TelephonyManager;
import android.telephony.data.ApnSetting;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.AtomicFile;
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
import android.view.inputmethod.InputMethodSystemProperty;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.notification.SystemNotificationChannels;
import com.android.internal.os.BackgroundThread;
import com.android.internal.policy.IKeyguardDismissCallback;
import com.android.internal.statusbar.IStatusBarService;
import com.android.internal.telephony.SmsApplication;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.FunctionalUtils;
import com.android.internal.util.JournaledFile;
import com.android.internal.util.Preconditions;
import com.android.internal.util.StatLogger;
import com.android.internal.util.XmlUtils;
import com.android.internal.widget.LockPatternUtils;
import com.android.server.BatteryService;
import com.android.server.LocalServices;
import com.android.server.LockGuard;
import com.android.server.SystemServerInitThreadPool;
import com.android.server.SystemService;
import com.android.server.UiModeManagerService;
import com.android.server.connectivity.NetworkAgentInfo;
import com.android.server.devicepolicy.TransferOwnershipMetadataManager;
import com.android.server.hdmi.HdmiCecKeycode;
import com.android.server.inputmethod.InputMethodManagerInternal;
import com.android.server.job.controllers.JobStatus;
import com.android.server.location.IHwLbsLogger;
import com.android.server.net.NetworkPolicyManagerInternal;
import com.android.server.pm.AbsPackageManagerService;
import com.android.server.pm.DumpState;
import com.android.server.pm.PackageManagerService;
import com.android.server.pm.UserRestrictionsUtils;
import com.android.server.power.IHwShutdownThread;
import com.android.server.storage.DeviceStorageMonitorInternal;
import com.android.server.uri.UriGrantsManagerInternal;
import com.android.server.wm.ActivityTaskManagerInternal;
import com.google.android.collect.Sets;
import huawei.android.security.IHwBehaviorCollectManager;
import huawei.cust.HwCustUtils;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class DevicePolicyManagerService extends BaseIDevicePolicyManager {
    private static final String AB_DEVICE_KEY = "ro.build.ab_update";
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
    private static final Set<Integer> DA_DISALLOWED_POLICIES = new ArraySet();
    private static final String[] DELEGATIONS = {"delegation-cert-install", "delegation-app-restrictions", "delegation-block-uninstall", "delegation-enable-system-app", "delegation-keep-uninstalled-packages", "delegation-package-access", "delegation-permission-grant", "delegation-install-existing-package", "delegation-keep-uninstalled-packages", "delegation-network-logging", "delegation-cert-selection"};
    private static final int DEVICE_ADMIN_DEACTIVATE_TIMEOUT = 10000;
    private static final List<String> DEVICE_OWNER_DELEGATIONS = Arrays.asList("delegation-network-logging");
    private static final String DEVICE_POLICIES_XML = "device_policies.xml";
    private static final String DO_NOT_ASK_CREDENTIALS_ON_BOOT_XML = "do-not-ask-credentials-on-boot";
    private static final boolean ENABLE_LOCK_GUARD = true;
    private static final List<String> EXCLUSIVE_DELEGATIONS = Arrays.asList("delegation-network-logging", "delegation-cert-selection");
    private static final long EXPIRATION_GRACE_PERIOD_MS = (MS_PER_DAY * 5);
    private static final Set<String> GLOBAL_SETTINGS_DEPRECATED = new ArraySet();
    private static final Set<String> GLOBAL_SETTINGS_WHITELIST = new ArraySet();
    protected static final String LOG_TAG = "DevicePolicyManager";
    private static final String LOG_TAG_DEVICE_OWNER = "device-owner";
    private static final String LOG_TAG_PROFILE_OWNER = "profile-owner";
    private static final String MDPP_TAG = "MDPPWriteEvent";
    private static final long MINIMUM_STRONG_AUTH_TIMEOUT_MS = TimeUnit.HOURS.toMillis(1);
    private static final long MS_PER_DAY = TimeUnit.DAYS.toMillis(1);
    private static final int PROFILE_KEYGUARD_FEATURES = 440;
    private static final int PROFILE_KEYGUARD_FEATURES_PROFILE_ONLY = 8;
    private static final String PROPERTY_DEVICE_OWNER_PRESENT = "ro.device_owner";
    private static final int REQUEST_EXPIRE_PASSWORD = 5571;
    private static final Set<String> SECURE_SETTINGS_DEVICEOWNER_WHITELIST = new ArraySet();
    private static final Set<String> SECURE_SETTINGS_WHITELIST = new ArraySet();
    private static final int STATUS_BAR_DISABLE2_MASK = 1;
    private static final int STATUS_BAR_DISABLE_MASK = 34013184;
    private static final Set<String> SYSTEM_SETTINGS_WHITELIST = new ArraySet();
    private static final String TAG_ACCEPTED_CA_CERTIFICATES = "accepted-ca-certificate";
    private static final String TAG_ADMIN_BROADCAST_PENDING = "admin-broadcast-pending";
    private static final String TAG_AFFILIATION_ID = "affiliation-id";
    private static final String TAG_CURRENT_INPUT_METHOD_SET = "current-ime-set";
    private static final String TAG_INITIALIZATION_BUNDLE = "initialization-bundle";
    private static final String TAG_LAST_BUG_REPORT_REQUEST = "last-bug-report-request";
    private static final String TAG_LAST_NETWORK_LOG_RETRIEVAL = "last-network-log-retrieval";
    private static final String TAG_LAST_SECURITY_LOG_RETRIEVAL = "last-security-log-retrieval";
    private static final String TAG_LOCK_TASK_COMPONENTS = "lock-task-component";
    private static final String TAG_LOCK_TASK_FEATURES = "lock-task-features";
    private static final String TAG_OWNER_INSTALLED_CA_CERT = "owner-installed-ca-cert";
    private static final String TAG_PASSWORD_TOKEN_HANDLE = "password-token";
    private static final String TAG_PASSWORD_VALIDITY = "password-validity";
    private static final String TAG_STATUS_BAR = "statusbar";
    private static final String TAG_TRANSFER_OWNERSHIP_BUNDLE = "transfer-ownership-bundle";
    private static final String TRANSFER_OWNERSHIP_PARAMETERS_XML = "transfer-ownership-parameters.xml";
    private static final int UNATTENDED_MANAGED_KIOSK_MS = 30000;
    private static final boolean VERBOSE_LOG = false;
    private static HwCustDevicePolicyManagerService mHwCustDevicePolicyManagerService = ((HwCustDevicePolicyManagerService) HwCustUtils.createObj(HwCustDevicePolicyManagerService.class, new Object[0]));
    final Handler mBackgroundHandler;
    private final CertificateMonitor mCertificateMonitor;
    private DevicePolicyConstants mConstants;
    private final DevicePolicyConstantsObserver mConstantsObserver;
    final Context mContext;
    private final DeviceAdminServiceController mDeviceAdminServiceController;
    final Handler mHandler;
    final boolean mHasFeature;
    final boolean mHasTelephonyFeature;
    IHwDevicePolicyManagerService mHwDevicePolicyManagerService;
    final IPackageManager mIPackageManager;
    final Injector mInjector;
    final boolean mIsWatch;
    final LocalService mLocalService;
    private final Object mLockDoNoUseDirectly;
    private final LockPatternUtils mLockPatternUtils;
    @GuardedBy({"getLockObject()"})
    private NetworkLogger mNetworkLogger;
    private final OverlayPackagesProvider mOverlayPackagesProvider;
    @VisibleForTesting
    final Owners mOwners;
    private final Set<Pair<String, Integer>> mPackagesToRemove;
    private final DevicePolicyCacheImpl mPolicyCache;
    final BroadcastReceiver mReceiver;
    private final BroadcastReceiver mRemoteBugreportConsentReceiver;
    private final BroadcastReceiver mRemoteBugreportFinishedReceiver;
    private final AtomicBoolean mRemoteBugreportServiceIsActive;
    private final AtomicBoolean mRemoteBugreportSharingAccepted;
    private final Runnable mRemoteBugreportTimeoutRunnable;
    private final SecurityLogMonitor mSecurityLogMonitor;
    private final SetupContentObserver mSetupContentObserver;
    private final StatLogger mStatLogger;
    final TelephonyManager mTelephonyManager;
    private final Binder mToken;
    @VisibleForTesting
    final TransferOwnershipMetadataManager mTransferOwnershipMetadataManager;
    final UsageStatsManagerInternal mUsageStatsManagerInternal;
    @GuardedBy({"getLockObject()"})
    final SparseArray<DevicePolicyData> mUserData;
    final UserManager mUserManager;
    final UserManagerInternal mUserManagerInternal;
    @GuardedBy({"getLockObject()"})
    final SparseArray<PasswordMetrics> mUserPasswordMetrics;

    interface Stats {
        public static final int COUNT = 1;
        public static final int LOCK_GUARD_GUARD = 0;
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
        GLOBAL_SETTINGS_WHITELIST.add("private_dns_mode");
        GLOBAL_SETTINGS_WHITELIST.add("private_dns_specifier");
        GLOBAL_SETTINGS_DEPRECATED.add("bluetooth_on");
        GLOBAL_SETTINGS_DEPRECATED.add("development_settings_enabled");
        GLOBAL_SETTINGS_DEPRECATED.add("mode_ringer");
        GLOBAL_SETTINGS_DEPRECATED.add("network_preference");
        GLOBAL_SETTINGS_DEPRECATED.add("wifi_on");
        SYSTEM_SETTINGS_WHITELIST.add("screen_brightness");
        SYSTEM_SETTINGS_WHITELIST.add("screen_brightness_mode");
        SYSTEM_SETTINGS_WHITELIST.add("screen_off_timeout");
        DA_DISALLOWED_POLICIES.add(8);
        DA_DISALLOWED_POLICIES.add(9);
        DA_DISALLOWED_POLICIES.add(6);
        DA_DISALLOWED_POLICIES.add(0);
    }

    /* access modifiers changed from: package-private */
    public final Object getLockObject() {
        long start = this.mStatLogger.getTime();
        LockGuard.guard(7);
        this.mStatLogger.logDurationStat(0, start);
        return this.mLockDoNoUseDirectly;
    }

    /* access modifiers changed from: package-private */
    public final void ensureLocked() {
        if (!Thread.holdsLock(this.mLockDoNoUseDirectly)) {
            Slog.wtfStack(LOG_TAG, "Not holding DPMS lock.");
        }
    }

    public static final class Lifecycle extends SystemService {
        private BaseIDevicePolicyManager mService;

        public Lifecycle(Context context) {
            super(context);
            this.mService = HwDevicePolicyFactory.getHwPolicyManagerInnerEx(context);
            if (this.mService == null) {
                String dpmsClassName = context.getResources().getString(17039838);
                dpmsClassName = TextUtils.isEmpty(dpmsClassName) ? DevicePolicyManagerService.class.getName() : dpmsClassName;
                try {
                    this.mService = (BaseIDevicePolicyManager) Class.forName(dpmsClassName).getConstructor(Context.class).newInstance(context);
                } catch (Exception e) {
                    throw new IllegalStateException("Failed to instantiate DevicePolicyManagerService with class name: " + dpmsClassName, e);
                }
            }
        }

        /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: com.android.server.devicepolicy.DevicePolicyManagerService$Lifecycle */
        /* JADX WARN: Multi-variable type inference failed */
        /* JADX WARN: Type inference failed for: r0v0, types: [com.android.server.devicepolicy.BaseIDevicePolicyManager, android.os.IBinder] */
        /* JADX WARNING: Unknown variable types count: 1 */
        @Override // com.android.server.SystemService
        public void onStart() {
            publishBinderService("device_policy", this.mService);
        }

        @Override // com.android.server.SystemService
        public void onBootPhase(int phase) {
            this.mService.systemReady(phase);
        }

        @Override // com.android.server.SystemService
        public void onStartUser(int userHandle) {
            this.mService.handleStartUser(userHandle);
        }

        @Override // com.android.server.SystemService
        public void onUnlockUser(int userHandle) {
            this.mService.handleUnlockUser(userHandle);
        }

        @Override // com.android.server.SystemService
        public void onStopUser(int userHandle) {
            this.mService.handleStopUser(userHandle);
        }
    }

    public static class DevicePolicyData {
        boolean doNotAskCredentialsOnBoot = false;
        final ArraySet<String> mAcceptedCaCertificates = new ArraySet<>();
        boolean mAdminBroadcastPending = false;
        final ArrayList<ActiveAdmin> mAdminList = new ArrayList<>();
        final ArrayMap<ComponentName, ActiveAdmin> mAdminMap = new ArrayMap<>();
        Set<String> mAffiliationIds = new ArraySet();
        boolean mCurrentInputMethodSet = false;
        final ArrayMap<String, List<String>> mDelegationMap = new ArrayMap<>();
        boolean mDeviceProvisioningConfigApplied = false;
        int mFailedPasswordAttempts = 0;
        PersistableBundle mInitBundle = null;
        long mLastBugReportRequestTime = -1;
        long mLastMaximumTimeToLock = -1;
        long mLastNetworkLogsRetrievalTime = -1;
        long mLastSecurityLogRetrievalTime = -1;
        int mLockTaskFeatures = 16;
        List<String> mLockTaskPackages = new ArrayList();
        Set<String> mOwnerInstalledCaCerts = new ArraySet();
        boolean mPaired = false;
        int mPasswordOwner = -1;
        long mPasswordTokenHandle = 0;
        boolean mPasswordValidAtLastCheckpoint = true;
        int mPermissionPolicy;
        final ArrayList<ComponentName> mRemovingAdmins = new ArrayList<>();
        ComponentName mRestrictionsProvider;
        boolean mStatusBarDisabled = false;
        int mUserHandle;
        int mUserProvisioningState;
        boolean mUserSetupComplete = false;

        public DevicePolicyData(int userHandle) {
            this.mUserHandle = userHandle;
        }
    }

    protected static class RestrictionsListener implements UserManagerInternal.UserRestrictionsListener {
        private Context mContext;

        public RestrictionsListener(Context context) {
            this.mContext = context;
        }

        public void onUserRestrictionsChanged(int userId, Bundle newRestrictions, Bundle prevRestrictions) {
            if (newRestrictions.getBoolean("no_sharing_into_profile") != prevRestrictions.getBoolean("no_sharing_into_profile")) {
                Intent intent = new Intent("android.app.action.DATA_SHARING_RESTRICTION_CHANGED");
                intent.setPackage(DevicePolicyManagerService.getManagedProvisioningPackage(this.mContext));
                intent.putExtra("android.intent.extra.USER_ID", userId);
                intent.addFlags(268435456);
                this.mContext.sendBroadcastAsUser(intent, UserHandle.SYSTEM);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public static class ActiveAdmin {
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
        private static final String TAG_CROSS_PROFILE_CALENDAR_PACKAGES = "cross-profile-calendar-packages";
        private static final String TAG_CROSS_PROFILE_CALENDAR_PACKAGES_NULL = "cross-profile-calendar-packages-null";
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
        private static final String TAG_END_USER_SESSION_MESSAGE = "end_user_session_message";
        private static final String TAG_FORCE_EPHEMERAL_USERS = "force_ephemeral_users";
        private static final String TAG_GLOBAL_PROXY_EXCLUSION_LIST = "global-proxy-exclusion-list";
        private static final String TAG_GLOBAL_PROXY_SPEC = "global-proxy-spec";
        private static final String TAG_IS_LOGOUT_ENABLED = "is_logout_enabled";
        private static final String TAG_IS_NETWORK_LOGGING_ENABLED = "is_network_logging_enabled";
        private static final String TAG_KEEP_UNINSTALLED_PACKAGES = "keep-uninstalled-packages";
        private static final String TAG_LONG_SUPPORT_MESSAGE = "long-support-message";
        private static final String TAG_MANAGE_TRUST_AGENT_FEATURES = "manage-trust-agent-features";
        private static final String TAG_MAX_FAILED_PASSWORD_WIPE = "max-failed-password-wipe";
        private static final String TAG_MAX_TIME_TO_UNLOCK = "max-time-to-unlock";
        private static final String TAG_METERED_DATA_DISABLED_PACKAGES = "metered_data_disabled_packages";
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
        private static final String TAG_START_USER_SESSION_MESSAGE = "start_user_session_message";
        private static final String TAG_STRONG_AUTH_UNLOCK_TIMEOUT = "strong-auth-unlock-timeout";
        private static final String TAG_TEST_ONLY_ADMIN = "test-only-admin";
        private static final String TAG_TRUST_AGENT_COMPONENT = "component";
        private static final String TAG_TRUST_AGENT_COMPONENT_OPTIONS = "trust-agent-component-options";
        private static final String TAG_USER_RESTRICTIONS = "user-restrictions";
        final Set<String> accountTypesWithManagementDisabled = new ArraySet();
        List<String> crossProfileWidgetProviders;
        final Set<String> defaultEnabledRestrictionsAlreadySet = new ArraySet();
        boolean disableBluetoothContactSharing = true;
        boolean disableCallerId = false;
        boolean disableCamera = false;
        boolean disableContactsSearch = false;
        boolean disableScreenCapture = false;
        int disabledKeyguardFeatures = 0;
        boolean encryptionRequested = false;
        String endUserSessionMessage = null;
        boolean forceEphemeralUsers = false;
        String globalProxyExclusionList = null;
        String globalProxySpec = null;
        DeviceAdminInfo info;
        boolean isLogoutEnabled = false;
        boolean isNetworkLoggingEnabled = false;
        final boolean isParent;
        List<String> keepUninstalledPackages;
        long lastNetworkLoggingNotificationTimeMs = 0;
        CharSequence longSupportMessage = null;
        List<String> mCrossProfileCalendarPackages = Collections.emptyList();
        public HwActiveAdmin mHwActiveAdmin;
        int maximumFailedPasswordsForWipe = 0;
        long maximumTimeToUnlock = 0;
        List<String> meteredDisabledPackages;
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
        String startUserSessionMessage = null;
        long strongAuthUnlockTimeout = 0;
        boolean testOnlyAdmin = false;
        ArrayMap<String, TrustAgentInfo> trustAgentInfos = new ArrayMap<>();
        Bundle userRestrictions;

        /* access modifiers changed from: package-private */
        public static class TrustAgentInfo {
            public PersistableBundle options;

            TrustAgentInfo(PersistableBundle bundle) {
                this.options = bundle;
            }
        }

        ActiveAdmin(DeviceAdminInfo _info, boolean parent) {
            this.info = _info;
            this.isParent = parent;
        }

        /* access modifiers changed from: package-private */
        public ActiveAdmin getParentActiveAdmin() {
            Preconditions.checkState(!this.isParent);
            if (this.parentAdmin == null) {
                this.parentAdmin = new ActiveAdmin(this.info, true);
            }
            return this.parentAdmin;
        }

        /* access modifiers changed from: package-private */
        public boolean hasParentActiveAdmin() {
            return this.parentAdmin != null;
        }

        /* access modifiers changed from: package-private */
        public int getUid() {
            return this.info.getActivityInfo().applicationInfo.uid;
        }

        public UserHandle getUserHandle() {
            return UserHandle.of(UserHandle.getUserId(this.info.getActivityInfo().applicationInfo.uid));
        }

        /* access modifiers changed from: package-private */
        public void writeToXml(XmlSerializer out) throws IllegalArgumentException, IllegalStateException, IOException {
            out.startTag(null, TAG_POLICIES);
            this.info.writePoliciesToXml(out);
            out.endTag(null, TAG_POLICIES);
            HwActiveAdmin hwActiveAdmin = this.mHwActiveAdmin;
            if (hwActiveAdmin != null) {
                hwActiveAdmin.writePoliciesToXml(out);
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
                Set<Map.Entry<String, TrustAgentInfo>> set = this.trustAgentInfos.entrySet();
                out.startTag(null, TAG_MANAGE_TRUST_AGENT_FEATURES);
                for (Map.Entry<String, TrustAgentInfo> entry : set) {
                    TrustAgentInfo trustAgentInfo = entry.getValue();
                    out.startTag(null, TAG_TRUST_AGENT_COMPONENT);
                    out.attribute(null, ATTR_VALUE, entry.getKey());
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
            List<String> list = this.crossProfileWidgetProviders;
            if (list != null && !list.isEmpty()) {
                out.startTag(null, TAG_CROSS_PROFILE_WIDGET_PROVIDERS);
                writeAttributeValuesToXml(out, TAG_PROVIDER, this.crossProfileWidgetProviders);
                out.endTag(null, TAG_CROSS_PROFILE_WIDGET_PROVIDERS);
            }
            writePackageListToXml(out, TAG_PERMITTED_ACCESSIBILITY_SERVICES, this.permittedAccessiblityServices);
            writePackageListToXml(out, TAG_PERMITTED_IMES, this.permittedInputMethods);
            writePackageListToXml(out, TAG_PERMITTED_NOTIFICATION_LISTENERS, this.permittedNotificationListeners);
            writePackageListToXml(out, TAG_KEEP_UNINSTALLED_PACKAGES, this.keepUninstalledPackages);
            writePackageListToXml(out, TAG_METERED_DATA_DISABLED_PACKAGES, this.meteredDisabledPackages);
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
            if (this.isLogoutEnabled) {
                out.startTag(null, TAG_IS_LOGOUT_ENABLED);
                out.attribute(null, ATTR_VALUE, Boolean.toString(this.isLogoutEnabled));
                out.endTag(null, TAG_IS_LOGOUT_ENABLED);
            }
            if (this.startUserSessionMessage != null) {
                out.startTag(null, TAG_START_USER_SESSION_MESSAGE);
                out.text(this.startUserSessionMessage);
                out.endTag(null, TAG_START_USER_SESSION_MESSAGE);
            }
            if (this.endUserSessionMessage != null) {
                out.startTag(null, TAG_END_USER_SESSION_MESSAGE);
                out.text(this.endUserSessionMessage);
                out.endTag(null, TAG_END_USER_SESSION_MESSAGE);
            }
            List<String> list2 = this.mCrossProfileCalendarPackages;
            if (list2 == null) {
                out.startTag(null, TAG_CROSS_PROFILE_CALENDAR_PACKAGES_NULL);
                out.endTag(null, TAG_CROSS_PROFILE_CALENDAR_PACKAGES_NULL);
                return;
            }
            writePackageListToXml(out, TAG_CROSS_PROFILE_CALENDAR_PACKAGES, list2);
        }

        /* access modifiers changed from: package-private */
        public void writePackageListToXml(XmlSerializer out, String outerTag, List<String> packageList) throws IllegalArgumentException, IllegalStateException, IOException {
            if (packageList != null) {
                out.startTag(null, outerTag);
                writeAttributeValuesToXml(out, "item", packageList);
                out.endTag(null, outerTag);
            }
        }

        /* access modifiers changed from: package-private */
        public void writeAttributeValuesToXml(XmlSerializer out, String tag, Collection<String> values) throws IOException {
            for (String value : values) {
                out.startTag(null, tag);
                out.attribute(null, ATTR_VALUE, value);
                out.endTag(null, tag);
            }
        }

        /* access modifiers changed from: package-private */
        public void readFromXml(XmlPullParser parser, boolean shouldOverridePolicies) throws XmlPullParserException, IOException {
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
                    try {
                        String tag = parser.getName();
                        if (HwActiveAdmin.TAG_POLICIES.equals(tag)) {
                            this.mHwActiveAdmin = HwActiveAdminFactory.loadFactory().getHwActiveAdmin();
                            this.mHwActiveAdmin.readPoliciesFromXml(parser);
                        } else if (TAG_POLICIES.equals(tag)) {
                            if (shouldOverridePolicies) {
                                Log.d(DevicePolicyManagerService.LOG_TAG, "Overriding device admin policies from XML.");
                                this.info.readPoliciesFromXml(parser);
                            }
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
                        } else if (TAG_METERED_DATA_DISABLED_PACKAGES.equals(tag)) {
                            this.meteredDisabledPackages = readPackageList(parser, tag);
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
                            Preconditions.checkState(!this.isParent);
                            this.parentAdmin = new ActiveAdmin(this.info, true);
                            this.parentAdmin.readFromXml(parser, shouldOverridePolicies);
                        } else if (TAG_ORGANIZATION_COLOR.equals(tag)) {
                            this.organizationColor = Integer.parseInt(parser.getAttributeValue(null, ATTR_VALUE));
                        } else if (TAG_ORGANIZATION_NAME.equals(tag)) {
                            if (parser.next() == 4) {
                                this.organizationName = parser.getText();
                            } else {
                                Log.w(DevicePolicyManagerService.LOG_TAG, "Missing text when loading organization name");
                            }
                        } else if (TAG_IS_LOGOUT_ENABLED.equals(tag)) {
                            this.isLogoutEnabled = Boolean.parseBoolean(parser.getAttributeValue(null, ATTR_VALUE));
                        } else if (TAG_START_USER_SESSION_MESSAGE.equals(tag)) {
                            if (parser.next() == 4) {
                                this.startUserSessionMessage = parser.getText();
                            } else {
                                Log.w(DevicePolicyManagerService.LOG_TAG, "Missing text when loading start session message");
                            }
                        } else if (TAG_END_USER_SESSION_MESSAGE.equals(tag)) {
                            if (parser.next() == 4) {
                                this.endUserSessionMessage = parser.getText();
                            } else {
                                Log.w(DevicePolicyManagerService.LOG_TAG, "Missing text when loading end session message");
                            }
                        } else if (TAG_CROSS_PROFILE_CALENDAR_PACKAGES.equals(tag)) {
                            this.mCrossProfileCalendarPackages = readPackageList(parser, tag);
                        } else if (TAG_CROSS_PROFILE_CALENDAR_PACKAGES_NULL.equals(tag)) {
                            this.mCrossProfileCalendarPackages = null;
                        } else {
                            Slog.w(DevicePolicyManagerService.LOG_TAG, "Unknown admin tag: " + tag);
                            XmlUtils.skipCurrentTag(parser);
                        }
                    } catch (NumberFormatException e) {
                        Log.e(DevicePolicyManagerService.LOG_TAG, "getUsrSetExtendTime : NumberFormatException");
                    }
                }
            }
        }

        private List<String> readPackageList(XmlPullParser parser, String tag) throws XmlPullParserException, IOException {
            List<String> result = new ArrayList<>();
            int outerDepth = parser.getDepth();
            while (true) {
                int outerType = parser.next();
                if (outerType == 1 || (outerType == 3 && parser.getDepth() <= outerDepth)) {
                    break;
                } else if (!(outerType == 3 || outerType == 4)) {
                    String outerTag = parser.getName();
                    if ("item".equals(outerTag)) {
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
            ArrayMap<String, TrustAgentInfo> result = new ArrayMap<>();
            while (true) {
                int typeDAM = parser.next();
                if (typeDAM == 1 || (typeDAM == 3 && parser.getDepth() <= outerDepthDAM)) {
                    break;
                } else if (!(typeDAM == 3 || typeDAM == 4)) {
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
                    break;
                } else if (!(typeDAM == 3 || typeDAM == 4)) {
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

        /* access modifiers changed from: package-private */
        public boolean hasUserRestrictions() {
            Bundle bundle = this.userRestrictions;
            return bundle != null && bundle.size() > 0;
        }

        /* access modifiers changed from: package-private */
        public Bundle ensureUserRestrictions() {
            if (this.userRestrictions == null) {
                this.userRestrictions = new Bundle();
            }
            return this.userRestrictions;
        }

        public void transfer(DeviceAdminInfo deviceAdminInfo) {
            if (hasParentActiveAdmin()) {
                this.parentAdmin.info = deviceAdminInfo;
            }
            this.info = deviceAdminInfo;
        }

        /* access modifiers changed from: package-private */
        public void dump(String prefix, PrintWriter pw) {
            pw.print(prefix);
            pw.print("uid=");
            pw.println(getUid());
            pw.print(prefix);
            pw.print("testOnlyAdmin=");
            pw.println(this.testOnlyAdmin);
            pw.print(prefix);
            pw.println("policies:");
            ArrayList<DeviceAdminInfo.PolicyInfo> pols = this.info.getUsedPolicies();
            if (pols != null) {
                for (int i = 0; i < pols.size(); i++) {
                    pw.print(prefix);
                    pw.print("  ");
                    pw.println(pols.get(i).tag);
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
                ActiveAdmin activeAdmin = this.parentAdmin;
                activeAdmin.dump(prefix + "  ", pw);
            }
            if (this.mCrossProfileCalendarPackages != null) {
                pw.print(prefix);
                pw.print("mCrossProfileCalendarPackages=");
                pw.println(this.mCrossProfileCalendarPackages);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handlePackagesChanged(String packageName, int userHandle) {
        boolean removedAdmin = false;
        DevicePolicyData policy = getUserData(userHandle);
        synchronized (getLockObject()) {
            for (int i = policy.mAdminList.size() - 1; i >= 0; i--) {
                ActiveAdmin aa = policy.mAdminList.get(i);
                try {
                    String adminPackage = aa.info.getPackageName();
                    if ((packageName == null || packageName.equals(adminPackage)) && (this.mIPackageManager.getPackageInfo(adminPackage, 0, userHandle) == null || this.mIPackageManager.getReceiverInfo(aa.info.getComponent(), 786432, userHandle) == null)) {
                        removedAdmin = true;
                        policy.mAdminList.remove(i);
                        policy.mAdminMap.remove(aa.info.getComponent());
                        pushActiveAdminPackagesLocked(userHandle);
                        pushMeteredDisabledPackagesLocked(userHandle);
                    }
                } catch (RemoteException e) {
                    Slog.e(LOG_TAG, "cannot handle packages changed.");
                }
            }
            if (removedAdmin) {
                validatePasswordOwnerLocked(policy);
            }
            boolean removedDelegate = false;
            for (int i2 = policy.mDelegationMap.size() - 1; i2 >= 0; i2--) {
                if (isRemovedPackage(packageName, policy.mDelegationMap.keyAt(i2), userHandle)) {
                    policy.mDelegationMap.removeAt(i2);
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

    private boolean isRemovedPackage(String changedPackage, String targetPackage, int userHandle) {
        if (targetPackage == null) {
            return false;
        }
        if (changedPackage != null) {
            try {
                if (!changedPackage.equals(targetPackage)) {
                    return false;
                }
            } catch (RemoteException e) {
                Slog.e(LOG_TAG, "cannot check whether remove packages.");
                return false;
            }
        }
        if (this.mIPackageManager.getPackageInfo(targetPackage, 0, userHandle) == null) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public static class Injector {
        public final Context mContext;

        Injector(Context context) {
            this.mContext = context;
        }

        public boolean hasFeature() {
            return getPackageManager().hasSystemFeature("android.software.device_admin");
        }

        /* access modifiers changed from: package-private */
        public Context createContextAsUser(UserHandle user) throws PackageManager.NameNotFoundException {
            return this.mContext.createPackageContextAsUser(this.mContext.getPackageName(), 0, user);
        }

        /* access modifiers changed from: package-private */
        public Resources getResources() {
            return this.mContext.getResources();
        }

        /* access modifiers changed from: package-private */
        public Owners newOwners() {
            return new Owners(getUserManager(), getUserManagerInternal(), getPackageManagerInternal(), getActivityTaskManagerInternal());
        }

        /* access modifiers changed from: package-private */
        public UserManager getUserManager() {
            return UserManager.get(this.mContext);
        }

        /* access modifiers changed from: package-private */
        public UserManagerInternal getUserManagerInternal() {
            return (UserManagerInternal) LocalServices.getService(UserManagerInternal.class);
        }

        /* access modifiers changed from: package-private */
        public PackageManagerInternal getPackageManagerInternal() {
            return (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class);
        }

        /* access modifiers changed from: package-private */
        public ActivityTaskManagerInternal getActivityTaskManagerInternal() {
            return (ActivityTaskManagerInternal) LocalServices.getService(ActivityTaskManagerInternal.class);
        }

        /* access modifiers changed from: package-private */
        public PermissionControllerManager getPermissionControllerManager(UserHandle user) {
            if (user.equals(this.mContext.getUser())) {
                return (PermissionControllerManager) this.mContext.getSystemService(PermissionControllerManager.class);
            }
            try {
                return (PermissionControllerManager) this.mContext.createPackageContextAsUser(this.mContext.getPackageName(), 0, user).getSystemService(PermissionControllerManager.class);
            } catch (PackageManager.NameNotFoundException notPossible) {
                throw new IllegalStateException(notPossible);
            }
        }

        /* access modifiers changed from: package-private */
        public UsageStatsManagerInternal getUsageStatsManagerInternal() {
            return (UsageStatsManagerInternal) LocalServices.getService(UsageStatsManagerInternal.class);
        }

        /* access modifiers changed from: package-private */
        public NetworkPolicyManagerInternal getNetworkPolicyManagerInternal() {
            return (NetworkPolicyManagerInternal) LocalServices.getService(NetworkPolicyManagerInternal.class);
        }

        /* access modifiers changed from: package-private */
        public NotificationManager getNotificationManager() {
            return (NotificationManager) this.mContext.getSystemService(NotificationManager.class);
        }

        /* access modifiers changed from: package-private */
        public IIpConnectivityMetrics getIIpConnectivityMetrics() {
            return IIpConnectivityMetrics.Stub.asInterface(ServiceManager.getService("connmetrics"));
        }

        /* access modifiers changed from: package-private */
        public PackageManager getPackageManager() {
            return this.mContext.getPackageManager();
        }

        /* access modifiers changed from: package-private */
        public PowerManagerInternal getPowerManagerInternal() {
            return (PowerManagerInternal) LocalServices.getService(PowerManagerInternal.class);
        }

        /* access modifiers changed from: package-private */
        public TelephonyManager getTelephonyManager() {
            return TelephonyManager.from(this.mContext);
        }

        /* access modifiers changed from: package-private */
        public TrustManager getTrustManager() {
            return (TrustManager) this.mContext.getSystemService("trust");
        }

        /* access modifiers changed from: package-private */
        public AlarmManager getAlarmManager() {
            return (AlarmManager) this.mContext.getSystemService(AlarmManager.class);
        }

        /* access modifiers changed from: package-private */
        public ConnectivityManager getConnectivityManager() {
            return (ConnectivityManager) this.mContext.getSystemService(ConnectivityManager.class);
        }

        /* access modifiers changed from: package-private */
        public IWindowManager getIWindowManager() {
            return IWindowManager.Stub.asInterface(ServiceManager.getService("window"));
        }

        /* access modifiers changed from: package-private */
        public IActivityManager getIActivityManager() {
            return ActivityManager.getService();
        }

        /* access modifiers changed from: package-private */
        public IActivityTaskManager getIActivityTaskManager() {
            return ActivityTaskManager.getService();
        }

        /* access modifiers changed from: package-private */
        public ActivityManagerInternal getActivityManagerInternal() {
            return (ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class);
        }

        /* access modifiers changed from: package-private */
        public IPackageManager getIPackageManager() {
            return AppGlobals.getPackageManager();
        }

        /* access modifiers changed from: package-private */
        public IBackupManager getIBackupManager() {
            return IBackupManager.Stub.asInterface(ServiceManager.getService(BatteryService.HealthServiceWrapper.INSTANCE_HEALTHD));
        }

        /* access modifiers changed from: package-private */
        public IAudioService getIAudioService() {
            return IAudioService.Stub.asInterface(ServiceManager.getService("audio"));
        }

        /* access modifiers changed from: package-private */
        public boolean isBuildDebuggable() {
            return Build.IS_DEBUGGABLE;
        }

        /* access modifiers changed from: package-private */
        public LockPatternUtils newLockPatternUtils() {
            return new LockPatternUtils(this.mContext);
        }

        /* access modifiers changed from: package-private */
        public boolean storageManagerIsFileBasedEncryptionEnabled() {
            return StorageManager.isFileEncryptedNativeOnly();
        }

        /* access modifiers changed from: package-private */
        public boolean storageManagerIsNonDefaultBlockEncrypted() {
            long identity = Binder.clearCallingIdentity();
            try {
                return StorageManager.isNonDefaultBlockEncrypted();
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        /* access modifiers changed from: package-private */
        public boolean storageManagerIsEncrypted() {
            return StorageManager.isEncrypted();
        }

        /* access modifiers changed from: package-private */
        public boolean storageManagerIsEncryptable() {
            return StorageManager.isEncryptable();
        }

        /* access modifiers changed from: package-private */
        public Looper getMyLooper() {
            return Looper.myLooper();
        }

        /* access modifiers changed from: package-private */
        public WifiManager getWifiManager() {
            return (WifiManager) this.mContext.getSystemService(WifiManager.class);
        }

        /* access modifiers changed from: package-private */
        public long binderClearCallingIdentity() {
            return Binder.clearCallingIdentity();
        }

        /* access modifiers changed from: package-private */
        public void binderRestoreCallingIdentity(long token) {
            Binder.restoreCallingIdentity(token);
        }

        /* access modifiers changed from: package-private */
        public int binderGetCallingUid() {
            return Binder.getCallingUid();
        }

        /* access modifiers changed from: package-private */
        public int binderGetCallingPid() {
            return Binder.getCallingPid();
        }

        /* access modifiers changed from: package-private */
        public UserHandle binderGetCallingUserHandle() {
            return Binder.getCallingUserHandle();
        }

        /* access modifiers changed from: package-private */
        public boolean binderIsCallingUidMyUid() {
            return Binder.getCallingUid() == Process.myUid();
        }

        /* access modifiers changed from: package-private */
        public void binderWithCleanCallingIdentity(FunctionalUtils.ThrowingRunnable action) {
            Binder.withCleanCallingIdentity(action);
        }

        /* access modifiers changed from: package-private */
        public final int userHandleGetCallingUserId() {
            return UserHandle.getUserId(binderGetCallingUid());
        }

        /* access modifiers changed from: package-private */
        public File environmentGetUserSystemDirectory(int userId) {
            return Environment.getUserSystemDirectory(userId);
        }

        /* access modifiers changed from: package-private */
        public void powerManagerGoToSleep(long time, int reason, int flags) {
            ((PowerManager) this.mContext.getSystemService(PowerManager.class)).goToSleep(time, reason, flags);
        }

        /* access modifiers changed from: package-private */
        public void powerManagerReboot(String reason) {
            ((PowerManager) this.mContext.getSystemService(PowerManager.class)).reboot(reason);
        }

        /* access modifiers changed from: package-private */
        public void recoverySystemRebootWipeUserData(boolean shutdown, String reason, boolean force, boolean wipeEuicc) throws IOException {
            RecoverySystem.rebootWipeUserData(this.mContext, shutdown, reason, force, wipeEuicc);
        }

        /* access modifiers changed from: package-private */
        public boolean systemPropertiesGetBoolean(String key, boolean def) {
            return SystemProperties.getBoolean(key, def);
        }

        /* access modifiers changed from: package-private */
        public long systemPropertiesGetLong(String key, long def) {
            return SystemProperties.getLong(key, def);
        }

        /* access modifiers changed from: package-private */
        public String systemPropertiesGet(String key, String def) {
            return SystemProperties.get(key, def);
        }

        /* access modifiers changed from: package-private */
        public String systemPropertiesGet(String key) {
            return SystemProperties.get(key);
        }

        /* access modifiers changed from: package-private */
        public void systemPropertiesSet(String key, String value) {
            SystemProperties.set(key, value);
        }

        /* access modifiers changed from: package-private */
        public boolean userManagerIsSplitSystemUser() {
            return UserManager.isSplitSystemUser();
        }

        /* access modifiers changed from: package-private */
        public String getDevicePolicyFilePathForSystemUser() {
            return AbsPackageManagerService.UNINSTALLED_DELAPP_DIR;
        }

        /* access modifiers changed from: package-private */
        public PendingIntent pendingIntentGetActivityAsUser(Context context, int requestCode, Intent intent, int flags, Bundle options, UserHandle user) {
            return PendingIntent.getActivityAsUser(context, requestCode, intent, flags, options, user);
        }

        /* access modifiers changed from: package-private */
        public void registerContentObserver(Uri uri, boolean notifyForDescendents, ContentObserver observer, int userHandle) {
            this.mContext.getContentResolver().registerContentObserver(uri, notifyForDescendents, observer, userHandle);
        }

        /* access modifiers changed from: package-private */
        public int settingsSecureGetIntForUser(String name, int def, int userHandle) {
            return Settings.Secure.getIntForUser(this.mContext.getContentResolver(), name, def, userHandle);
        }

        /* access modifiers changed from: package-private */
        public String settingsSecureGetStringForUser(String name, int userHandle) {
            return Settings.Secure.getStringForUser(this.mContext.getContentResolver(), name, userHandle);
        }

        /* access modifiers changed from: package-private */
        public void settingsSecurePutIntForUser(String name, int value, int userHandle) {
            Settings.Secure.putIntForUser(this.mContext.getContentResolver(), name, value, userHandle);
        }

        /* access modifiers changed from: package-private */
        public void settingsSecurePutStringForUser(String name, String value, int userHandle) {
            Settings.Secure.putStringForUser(this.mContext.getContentResolver(), name, value, userHandle);
        }

        /* access modifiers changed from: package-private */
        public void settingsGlobalPutStringForUser(String name, String value, int userHandle) {
            Settings.Global.putStringForUser(this.mContext.getContentResolver(), name, value, userHandle);
        }

        /* access modifiers changed from: package-private */
        public void settingsSecurePutInt(String name, int value) {
            Settings.Secure.putInt(this.mContext.getContentResolver(), name, value);
        }

        /* access modifiers changed from: package-private */
        public int settingsGlobalGetInt(String name, int def) {
            return Settings.Global.getInt(this.mContext.getContentResolver(), name, def);
        }

        /* access modifiers changed from: package-private */
        public String settingsGlobalGetString(String name) {
            return Settings.Global.getString(this.mContext.getContentResolver(), name);
        }

        /* access modifiers changed from: package-private */
        public void settingsGlobalPutInt(String name, int value) {
            Settings.Global.putInt(this.mContext.getContentResolver(), name, value);
        }

        /* access modifiers changed from: package-private */
        public void settingsSecurePutString(String name, String value) {
            Settings.Secure.putString(this.mContext.getContentResolver(), name, value);
        }

        /* access modifiers changed from: package-private */
        public void settingsGlobalPutString(String name, String value) {
            Settings.Global.putString(this.mContext.getContentResolver(), name, value);
        }

        /* access modifiers changed from: package-private */
        public void settingsSystemPutStringForUser(String name, String value, int userId) {
            Settings.System.putStringForUser(this.mContext.getContentResolver(), name, value, userId);
        }

        /* access modifiers changed from: package-private */
        public void securityLogSetLoggingEnabledProperty(boolean enabled) {
            SecurityLog.setLoggingEnabledProperty(enabled);
        }

        /* access modifiers changed from: package-private */
        public boolean securityLogGetLoggingEnabledProperty() {
            return SecurityLog.getLoggingEnabledProperty();
        }

        /* access modifiers changed from: package-private */
        public boolean securityLogIsLoggingEnabled() {
            return SecurityLog.isLoggingEnabled();
        }

        /* access modifiers changed from: package-private */
        public KeyChain.KeyChainConnection keyChainBindAsUser(UserHandle user) throws InterruptedException {
            return KeyChain.bindAsUser(this.mContext, user);
        }

        /* access modifiers changed from: package-private */
        public void postOnSystemServerInitThreadPool(Runnable runnable) {
            SystemServerInitThreadPool.get().submit(runnable, DevicePolicyManagerService.LOG_TAG);
        }

        public TransferOwnershipMetadataManager newTransferOwnershipMetadataManager() {
            return new TransferOwnershipMetadataManager();
        }

        public void runCryptoSelfTest() {
            CryptoTestHelper.runAndLogSelfTest();
        }
    }

    public DevicePolicyManagerService(Context context) {
        this(new Injector(context));
    }

    @VisibleForTesting
    DevicePolicyManagerService(Injector injector) {
        this.mPolicyCache = new DevicePolicyCacheImpl();
        this.mPackagesToRemove = new ArraySet();
        this.mToken = new Binder();
        this.mRemoteBugreportServiceIsActive = new AtomicBoolean();
        this.mRemoteBugreportSharingAccepted = new AtomicBoolean();
        this.mStatLogger = new StatLogger(new String[]{"LockGuard.guard()"});
        this.mLockDoNoUseDirectly = LockGuard.installNewLock(7, true);
        this.mRemoteBugreportTimeoutRunnable = new Runnable() {
            /* class com.android.server.devicepolicy.DevicePolicyManagerService.AnonymousClass1 */

            @Override // java.lang.Runnable
            public void run() {
                if (DevicePolicyManagerService.this.mRemoteBugreportServiceIsActive.get()) {
                    DevicePolicyManagerService.this.onBugreportFailed();
                }
            }
        };
        this.mRemoteBugreportFinishedReceiver = new BroadcastReceiver() {
            /* class com.android.server.devicepolicy.DevicePolicyManagerService.AnonymousClass2 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if ("android.intent.action.REMOTE_BUGREPORT_DISPATCH".equals(intent.getAction()) && DevicePolicyManagerService.this.mRemoteBugreportServiceIsActive.get()) {
                    DevicePolicyManagerService.this.onBugreportFinished(intent);
                }
            }
        };
        this.mRemoteBugreportConsentReceiver = new BroadcastReceiver() {
            /* class com.android.server.devicepolicy.DevicePolicyManagerService.AnonymousClass3 */

            @Override // android.content.BroadcastReceiver
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
        this.mUserData = new SparseArray<>();
        this.mUserPasswordMetrics = new SparseArray<>();
        this.mReceiver = new BroadcastReceiver() {
            /* class com.android.server.devicepolicy.DevicePolicyManagerService.AnonymousClass4 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                final int userHandle = intent.getIntExtra("android.intent.extra.user_handle", getSendingUserId());
                if ("android.intent.action.USER_STARTED".equals(action) && userHandle == DevicePolicyManagerService.this.mOwners.getDeviceOwnerUserId()) {
                    synchronized (DevicePolicyManagerService.this.getLockObject()) {
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
                        /* class com.android.server.devicepolicy.DevicePolicyManagerService.AnonymousClass4.AnonymousClass1 */

                        @Override // java.lang.Runnable
                        public void run() {
                            DevicePolicyManagerService.this.handlePasswordExpirationNotification(userHandle);
                        }
                    });
                }
                if ("android.intent.action.USER_ADDED".equals(action)) {
                    sendDeviceOwnerUserCommand("android.app.action.USER_ADDED", userHandle);
                    synchronized (DevicePolicyManagerService.this.getLockObject()) {
                        DevicePolicyManagerService.this.maybePauseDeviceWideLoggingLocked();
                    }
                } else if ("android.intent.action.USER_REMOVED".equals(action)) {
                    sendDeviceOwnerUserCommand("android.app.action.USER_REMOVED", userHandle);
                    synchronized (DevicePolicyManagerService.this.getLockObject()) {
                        boolean isRemovedUserAffiliated = DevicePolicyManagerService.this.isUserAffiliatedWithDeviceLocked(userHandle);
                        DevicePolicyManagerService.this.removeUserData(userHandle);
                        if (!isRemovedUserAffiliated) {
                            DevicePolicyManagerService.this.discardDeviceWideLogsLocked();
                            DevicePolicyManagerService.this.maybeResumeDeviceWideLoggingLocked();
                        }
                    }
                } else if ("android.intent.action.USER_STARTED".equals(action)) {
                    sendDeviceOwnerUserCommand("android.app.action.USER_STARTED", userHandle);
                    synchronized (DevicePolicyManagerService.this.getLockObject()) {
                        DevicePolicyManagerService.this.maybeSendAdminEnabledBroadcastLocked(userHandle);
                        DevicePolicyManagerService.this.mUserData.remove(userHandle);
                    }
                    DevicePolicyManagerService.this.handlePackagesChanged(null, userHandle);
                } else if ("android.intent.action.USER_STOPPED".equals(action)) {
                    sendDeviceOwnerUserCommand("android.app.action.USER_STOPPED", userHandle);
                } else if ("android.intent.action.USER_SWITCHED".equals(action)) {
                    sendDeviceOwnerUserCommand("android.app.action.USER_SWITCHED", userHandle);
                } else if ("android.intent.action.USER_UNLOCKED".equals(action)) {
                    synchronized (DevicePolicyManagerService.this.getLockObject()) {
                        DevicePolicyManagerService.this.maybeSendAdminEnabledBroadcastLocked(userHandle);
                    }
                } else if ("android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE".equals(action)) {
                    DevicePolicyManagerService.this.handlePackagesChanged(null, userHandle);
                } else if ("android.intent.action.PACKAGE_CHANGED".equals(action) || ("android.intent.action.PACKAGE_ADDED".equals(action) && intent.getBooleanExtra("android.intent.extra.REPLACING", false))) {
                    DevicePolicyManagerService.this.handlePackagesChanged(intent.getData().getSchemeSpecificPart(), userHandle);
                } else if ("android.intent.action.PACKAGE_REMOVED".equals(action) && !intent.getBooleanExtra("android.intent.extra.REPLACING", false)) {
                    DevicePolicyManagerService.this.handlePackagesChanged(intent.getData().getSchemeSpecificPart(), userHandle);
                } else if ("android.intent.action.MANAGED_PROFILE_ADDED".equals(action)) {
                    DevicePolicyManagerService.this.clearWipeProfileNotification();
                } else if ("android.intent.action.DATE_CHANGED".equals(action) || "android.intent.action.TIME_SET".equals(action)) {
                    DevicePolicyManagerService.this.updateSystemUpdateFreezePeriodsRecord(true);
                } else if ("android.intent.action.ACTION_SHUTDOWN".equals(action)) {
                    Slog.i(DevicePolicyManagerService.LOG_TAG, "System shutdown:");
                    DevicePolicyManagerService.this.discardDeviceWideLogsLocked();
                } else {
                    Slog.i(DevicePolicyManagerService.LOG_TAG, "Unhandled intent");
                }
            }

            private void sendDeviceOwnerUserCommand(String action, int userHandle) {
                synchronized (DevicePolicyManagerService.this.getLockObject()) {
                    ActiveAdmin deviceOwner = DevicePolicyManagerService.this.getDeviceOwnerAdminLocked();
                    if (deviceOwner != null) {
                        Bundle extras = new Bundle();
                        extras.putParcelable("android.intent.extra.USER", UserHandle.of(userHandle));
                        DevicePolicyManagerService.this.sendAdminCommandLocked(deviceOwner, action, extras, null, true);
                    }
                }
            }
        };
        this.mInjector = injector;
        this.mContext = (Context) Preconditions.checkNotNull(injector.mContext);
        this.mHandler = new Handler((Looper) Preconditions.checkNotNull(injector.getMyLooper()));
        this.mConstantsObserver = new DevicePolicyConstantsObserver(this.mHandler);
        this.mConstantsObserver.register();
        this.mConstants = loadConstants();
        this.mOwners = (Owners) Preconditions.checkNotNull(injector.newOwners());
        this.mUserManager = (UserManager) Preconditions.checkNotNull(injector.getUserManager());
        this.mUserManagerInternal = (UserManagerInternal) Preconditions.checkNotNull(injector.getUserManagerInternal());
        this.mUsageStatsManagerInternal = (UsageStatsManagerInternal) Preconditions.checkNotNull(injector.getUsageStatsManagerInternal());
        this.mIPackageManager = (IPackageManager) Preconditions.checkNotNull(injector.getIPackageManager());
        this.mTelephonyManager = (TelephonyManager) Preconditions.checkNotNull(injector.getTelephonyManager());
        this.mLocalService = new LocalService();
        this.mLockPatternUtils = injector.newLockPatternUtils();
        this.mSecurityLogMonitor = new SecurityLogMonitor(this);
        this.mHasFeature = this.mInjector.hasFeature();
        this.mIsWatch = this.mInjector.getPackageManager().hasSystemFeature("android.hardware.type.watch");
        this.mHasTelephonyFeature = this.mInjector.getPackageManager().hasSystemFeature("android.hardware.telephony");
        this.mBackgroundHandler = BackgroundThread.getHandler();
        this.mCertificateMonitor = new CertificateMonitor(this, this.mInjector, this.mBackgroundHandler);
        this.mDeviceAdminServiceController = new DeviceAdminServiceController(this, this.mConstants);
        this.mOverlayPackagesProvider = new OverlayPackagesProvider(this.mContext);
        this.mTransferOwnershipMetadataManager = this.mInjector.newTransferOwnershipMetadataManager();
        if (!this.mHasFeature) {
            this.mSetupContentObserver = null;
            return;
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.BOOT_COMPLETED");
        filter.addAction(ACTION_EXPIRED_PASSWORD_NOTIFICATION);
        filter.addAction("android.intent.action.USER_ADDED");
        filter.addAction("android.intent.action.USER_REMOVED");
        filter.addAction("android.intent.action.USER_STARTED");
        filter.addAction("android.intent.action.USER_STOPPED");
        filter.addAction("android.intent.action.USER_SWITCHED");
        filter.addAction("android.intent.action.USER_UNLOCKED");
        filter.setPriority(1000);
        this.mContext.registerReceiverAsUser(this.mReceiver, UserHandle.ALL, filter, null, this.mHandler);
        IntentFilter filter2 = new IntentFilter();
        filter2.addAction("android.intent.action.PACKAGE_CHANGED");
        filter2.addAction("android.intent.action.PACKAGE_REMOVED");
        filter2.addAction("android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE");
        filter2.addAction("android.intent.action.PACKAGE_ADDED");
        filter2.addDataScheme("package");
        this.mContext.registerReceiverAsUser(this.mReceiver, UserHandle.ALL, filter2, null, this.mHandler);
        IntentFilter filter3 = new IntentFilter();
        filter3.addAction("android.intent.action.MANAGED_PROFILE_ADDED");
        filter3.addAction("android.intent.action.TIME_SET");
        filter3.addAction("android.intent.action.DATE_CHANGED");
        filter3.addAction("android.intent.action.ACTION_SHUTDOWN");
        this.mContext.registerReceiverAsUser(this.mReceiver, UserHandle.ALL, filter3, null, this.mHandler);
        LocalServices.addService(DevicePolicyManagerInternal.class, this.mLocalService);
        this.mSetupContentObserver = new SetupContentObserver(this.mHandler);
        this.mUserManagerInternal.addUserRestrictionsListener(new RestrictionsListener(this.mContext));
    }

    /* access modifiers changed from: package-private */
    public DevicePolicyData getUserData(int userHandle) {
        DevicePolicyData policy;
        synchronized (getLockObject()) {
            policy = this.mUserData.get(userHandle);
            if (policy == null) {
                policy = new DevicePolicyData(userHandle);
                this.mUserData.append(userHandle, policy);
                loadSettingsLocked(policy, userHandle);
            }
            if (this.mHwDevicePolicyManagerService != null) {
                this.mHwDevicePolicyManagerService.init();
            }
        }
        return policy;
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"getLockObject()"})
    public PasswordMetrics getUserPasswordMetricsLocked(int userHandle) {
        return this.mUserPasswordMetrics.get(userHandle);
    }

    /* access modifiers changed from: package-private */
    public DevicePolicyData getUserDataUnchecked(int userHandle) {
        long ident = this.mInjector.binderClearCallingIdentity();
        try {
            return getUserData(userHandle);
        } finally {
            this.mInjector.binderRestoreCallingIdentity(ident);
        }
    }

    /* access modifiers changed from: package-private */
    public void removeUserData(int userHandle) {
        synchronized (getLockObject()) {
            if (userHandle == 0) {
                Slog.w(LOG_TAG, "Tried to remove device policy file for user 0! Ignoring.");
                return;
            }
            updatePasswordQualityCacheForUserGroup(userHandle);
            this.mPolicyCache.onUserRemoved(userHandle);
            this.mOwners.removeProfileOwner(userHandle);
            this.mOwners.writeProfileOwner(userHandle);
            if (this.mUserData.get(userHandle) != null) {
                this.mUserData.remove(userHandle);
            }
            if (this.mUserPasswordMetrics.get(userHandle) != null) {
                this.mUserPasswordMetrics.remove(userHandle);
            }
            File policyFile = new File(this.mInjector.environmentGetUserSystemDirectory(userHandle), DEVICE_POLICIES_XML);
            policyFile.delete();
            Slog.i(LOG_TAG, "Removed device policy file " + policyFile.getAbsolutePath());
        }
    }

    /* access modifiers changed from: package-private */
    public void loadOwners() {
        synchronized (getLockObject()) {
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
        synchronized (getLockObject()) {
            for (Integer num : this.mOwners.getProfileOwnerKeys()) {
                int userId = num.intValue();
                ActiveAdmin profileOwner = getProfileOwnerAdminLocked(userId);
                if (profileOwner != null) {
                    if (this.mUserManager.isManagedProfile(userId)) {
                        maybeSetDefaultRestrictionsForAdminLocked(userId, profileOwner, UserRestrictionsUtils.getDefaultEnabledForManagedProfiles());
                        ensureUnknownSourcesRestrictionForProfileOwnerLocked(userId, profileOwner, false);
                    }
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
            Set<String> restrictionsToSet = new ArraySet<>(defaultRestrictions);
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setDeviceOwnerSystemPropertyLocked() {
        boolean deviceProvisioned = false;
        if (this.mInjector.settingsGlobalGetInt("device_provisioned", 0) != 0) {
            deviceProvisioned = true;
        }
        boolean hasDeviceOwner = this.mOwners.hasDeviceOwner();
        if ((!hasDeviceOwner && !deviceProvisioned) || StorageManager.inCryptKeeperBounce()) {
            return;
        }
        if (!this.mInjector.systemPropertiesGet(PROPERTY_DEVICE_OWNER_PRESENT, "").isEmpty()) {
            Slog.w(LOG_TAG, "Trying to set ro.device_owner, but it has already been set?");
            return;
        }
        String value = Boolean.toString(hasDeviceOwner);
        this.mInjector.systemPropertiesSet(PROPERTY_DEVICE_OWNER_PRESENT, value);
        Slog.i(LOG_TAG, "Set ro.device_owner property to " + value);
    }

    private void maybeStartSecurityLogMonitorOnActivityManagerReady() {
        synchronized (getLockObject()) {
            if (this.mInjector.securityLogIsLoggingEnabled()) {
                this.mSecurityLogMonitor.start();
                this.mInjector.runCryptoSelfTest();
                maybePauseDeviceWideLoggingLocked();
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
                    return;
                }
                Owners owners = this.mOwners;
                owners.setDeviceOwnerWithRestrictionsMigrated(doComponent, owners.getDeviceOwnerName(), this.mOwners.getDeviceOwnerUserId(), !this.mOwners.getDeviceOwnerUserRestrictionsNeedsMigration());
                this.mOwners.writeDeviceOwner();
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
                migrateUserRestrictionsForUser(ui.getUserHandle(), getProfileOwnerAdminLocked(userId), userId == 0 ? null : secondaryUserExceptionList, false);
                pushUserRestrictions(userId);
                this.mOwners.setProfileOwnerUserRestrictionsMigrated(userId);
            }
        }
    }

    private void migrateUserRestrictionsForUser(UserHandle user, ActiveAdmin admin, Set<String> exceptionList, boolean isDeviceOwner) {
        boolean canOwnerChange;
        Bundle origRestrictions = this.mUserManagerInternal.getBaseUserRestrictions(user.getIdentifier());
        Bundle newBaseRestrictions = new Bundle();
        Bundle newOwnerRestrictions = new Bundle();
        for (String key : origRestrictions.keySet()) {
            if (origRestrictions.getBoolean(key)) {
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
            ActiveAdmin admin = policy.mAdminList.get(i);
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
        long alarmInterval;
        Throwable th;
        int affectedUserHandle;
        AlarmManager am;
        long expiration = getPasswordExpirationLocked(null, userHandle, parent);
        long now = System.currentTimeMillis();
        long timeToExpire = expiration - now;
        if (expiration == 0) {
            alarmInterval = 0;
        } else if (timeToExpire <= 0) {
            alarmInterval = MS_PER_DAY + now;
        } else {
            long alarmInterval2 = timeToExpire % MS_PER_DAY;
            if (alarmInterval2 == 0) {
                alarmInterval2 = MS_PER_DAY;
            }
            alarmInterval = alarmInterval2 + now;
        }
        long token = this.mInjector.binderClearCallingIdentity();
        if (parent) {
            try {
                affectedUserHandle = getProfileParentId(userHandle);
            } catch (Throwable th2) {
                th = th2;
            }
        } else {
            affectedUserHandle = userHandle;
        }
        try {
            am = this.mInjector.getAlarmManager();
        } catch (Throwable th3) {
            th = th3;
            this.mInjector.binderRestoreCallingIdentity(token);
            throw th;
        }
        try {
            PendingIntent pi = PendingIntent.getBroadcastAsUser(context, REQUEST_EXPIRE_PASSWORD, new Intent(ACTION_EXPIRED_PASSWORD_NOTIFICATION), 1207959552, UserHandle.of(affectedUserHandle));
            am.cancel(pi);
            if (alarmInterval != 0) {
                am.set(1, alarmInterval, pi);
            }
            this.mInjector.binderRestoreCallingIdentity(token);
        } catch (Throwable th4) {
            th = th4;
            this.mInjector.binderRestoreCallingIdentity(token);
            throw th;
        }
    }

    /* access modifiers changed from: package-private */
    public ActiveAdmin getActiveAdminUncheckedLocked(ComponentName who, int userHandle) {
        ensureLocked();
        ActiveAdmin admin = getUserData(userHandle).mAdminMap.get(who);
        if (admin == null || !who.getPackageName().equals(admin.info.getActivityInfo().packageName) || !who.getClassName().equals(admin.info.getActivityInfo().name)) {
            return null;
        }
        return admin;
    }

    /* access modifiers changed from: package-private */
    public ActiveAdmin getActiveAdminUncheckedLocked(ComponentName who, int userHandle, boolean parent) {
        ensureLocked();
        if (parent) {
            enforceManagedProfile(userHandle, "call APIs on the parent profile");
        }
        ActiveAdmin admin = getActiveAdminUncheckedLocked(who, userHandle);
        if (admin == null || !parent) {
            return admin;
        }
        return admin.getParentActiveAdmin();
    }

    /* access modifiers changed from: package-private */
    public ActiveAdmin getActiveAdminForCallerLocked(ComponentName who, int reqPolicy) throws SecurityException {
        return getActiveAdminOrCheckPermissionForCallerLocked(who, reqPolicy, null);
    }

    /* access modifiers changed from: package-private */
    public ActiveAdmin getActiveAdminOrCheckPermissionForCallerLocked(ComponentName who, int reqPolicy, String permission) throws SecurityException {
        ensureLocked();
        int callingUid = this.mInjector.binderGetCallingUid();
        ActiveAdmin result = getActiveAdminWithPolicyForUidLocked(who, reqPolicy, callingUid);
        if (result != null) {
            return result;
        }
        if (permission != null && this.mContext.checkCallingPermission(permission) == 0) {
            return null;
        }
        if (who != null) {
            int userId = UserHandle.getUserId(callingUid);
            ActiveAdmin admin = getUserData(userId).mAdminMap.get(who);
            boolean isDeviceOwner = isDeviceOwner(admin.info.getComponent(), userId);
            boolean isProfileOwner = isProfileOwner(admin.info.getComponent(), userId);
            if (reqPolicy == -2) {
                throw new SecurityException("Admin " + admin.info.getComponent() + " does not own the device");
            } else if (reqPolicy == -1) {
                throw new SecurityException("Admin " + admin.info.getComponent() + " does not own the profile");
            } else if (!DA_DISALLOWED_POLICIES.contains(Integer.valueOf(reqPolicy)) || isDeviceOwner || isProfileOwner) {
                throw new SecurityException("Admin " + admin.info.getComponent() + " did not specify uses-policy for: " + admin.info.getTagForPolicy(reqPolicy));
            } else {
                throw new SecurityException("Admin " + admin.info.getComponent() + " is not a device owner or profile owner, so may not use policy: " + admin.info.getTagForPolicy(reqPolicy));
            }
        } else {
            throw new SecurityException("No active admin owned by uid " + callingUid + " for policy #" + reqPolicy);
        }
    }

    /* access modifiers changed from: package-private */
    public ActiveAdmin getActiveAdminForCallerLocked(ComponentName who, int reqPolicy, boolean parent) throws SecurityException {
        return getActiveAdminOrCheckPermissionForCallerLocked(who, reqPolicy, parent, null);
    }

    /* access modifiers changed from: package-private */
    public ActiveAdmin getActiveAdminOrCheckPermissionForCallerLocked(ComponentName who, int reqPolicy, boolean parent, String permission) throws SecurityException {
        ensureLocked();
        if (parent) {
            enforceManagedProfile(this.mInjector.userHandleGetCallingUserId(), "call APIs on the parent profile");
        }
        ActiveAdmin admin = getActiveAdminOrCheckPermissionForCallerLocked(who, reqPolicy, permission);
        return parent ? admin.getParentActiveAdmin() : admin;
    }

    private ActiveAdmin getActiveAdminForUidLocked(ComponentName who, int uid) {
        ensureLocked();
        ActiveAdmin admin = getUserData(UserHandle.getUserId(uid)).mAdminMap.get(who);
        if (admin == null) {
            throw new SecurityException("No active admin " + who + " for UID " + uid);
        } else if (admin.getUid() == uid) {
            return admin;
        } else {
            throw new SecurityException("Admin " + who + " is not owned by uid " + uid);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private ActiveAdmin getActiveAdminWithPolicyForUidLocked(ComponentName who, int reqPolicy, int uid) {
        ensureLocked();
        int userId = UserHandle.getUserId(uid);
        DevicePolicyData policy = getUserData(userId);
        if (who != null) {
            ActiveAdmin admin = policy.mAdminMap.get(who);
            if (admin == null) {
                throw new SecurityException("No active admin " + who);
            } else if (admin.getUid() != uid) {
                throw new SecurityException("Admin " + who + " is not owned by uid " + uid);
            } else if (isActiveAdminWithPolicyForUserLocked(admin, reqPolicy, userId)) {
                return admin;
            } else {
                return null;
            }
        } else {
            Iterator<ActiveAdmin> it = policy.mAdminList.iterator();
            while (it.hasNext()) {
                ActiveAdmin admin2 = it.next();
                if (admin2.getUid() == uid && isActiveAdminWithPolicyForUserLocked(admin2, reqPolicy, userId)) {
                    return admin2;
                }
            }
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public boolean isActiveAdminWithPolicyForUserLocked(ActiveAdmin admin, int reqPolicy, int userId) {
        IHwDevicePolicyManagerService iHwDevicePolicyManagerService;
        ensureLocked();
        boolean ownsDevice = isDeviceOwner(admin.info.getComponent(), userId);
        boolean ownsProfile = isProfileOwner(admin.info.getComponent(), userId);
        if (reqPolicy == -2) {
            return ownsDevice;
        }
        if (reqPolicy == -1) {
            return ownsDevice || ownsProfile;
        }
        return (ownsDevice || ownsProfile || !DA_DISALLOWED_POLICIES.contains(Integer.valueOf(reqPolicy)) || getTargetSdk(admin.info.getPackageName(), userId) < 29 || ((iHwDevicePolicyManagerService = this.mHwDevicePolicyManagerService) != null && iHwDevicePolicyManagerService.isDeprecatedPolicyEnabled(reqPolicy, userId))) && admin.info.usesPolicy(reqPolicy);
    }

    /* access modifiers changed from: package-private */
    public void sendAdminCommandLocked(ActiveAdmin admin, String action) {
        sendAdminCommandLocked(admin, action, null);
    }

    /* access modifiers changed from: package-private */
    public void sendAdminCommandLocked(ActiveAdmin admin, String action, BroadcastReceiver result) {
        sendAdminCommandLocked(admin, action, (Bundle) null, result);
    }

    /* access modifiers changed from: package-private */
    public void sendAdminCommandLocked(ActiveAdmin admin, String action, Bundle adminExtras, BroadcastReceiver result) {
        sendAdminCommandLocked(admin, action, adminExtras, result, false);
    }

    /* access modifiers changed from: package-private */
    public boolean sendAdminCommandLocked(ActiveAdmin admin, String action, Bundle adminExtras, BroadcastReceiver result, boolean inForeground) {
        Intent intent = new Intent(action);
        intent.setComponent(admin.info.getComponent());
        if (UserManager.isDeviceInDemoMode(this.mContext)) {
            intent.addFlags(268435456);
        }
        if (action.equals("android.app.action.ACTION_PASSWORD_EXPIRING")) {
            intent.putExtra("expiration", admin.passwordExpirationDate);
        }
        if (inForeground) {
            intent.addFlags(268435456);
        }
        if (adminExtras != null) {
            intent.putExtras(adminExtras);
        }
        if (this.mInjector.getPackageManager().queryBroadcastReceiversAsUser(intent, 268435456, admin.getUserHandle()).isEmpty()) {
            return false;
        }
        BroadcastOptions options = BroadcastOptions.makeBasic();
        options.setBackgroundActivityStartsAllowed(true);
        if (result != null) {
            this.mContext.sendOrderedBroadcastAsUser(intent, admin.getUserHandle(), null, -1, options.toBundle(), result, this.mHandler, -1, null, null);
            return true;
        }
        this.mContext.sendBroadcastAsUser(intent, admin.getUserHandle(), null, options.toBundle());
        return true;
    }

    /* access modifiers changed from: package-private */
    public void sendAdminCommandLocked(String action, int reqPolicy, int userHandle, Bundle adminExtras) {
        DevicePolicyData policy = getUserData(userHandle);
        int count = policy.mAdminList.size();
        for (int i = 0; i < count; i++) {
            ActiveAdmin admin = policy.mAdminList.get(i);
            if (admin.info.usesPolicy(reqPolicy)) {
                sendAdminCommandLocked(admin, action, adminExtras, (BroadcastReceiver) null);
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

    /* access modifiers changed from: package-private */
    public void removeActiveAdminLocked(final ComponentName adminReceiver, final int userHandle) {
        ActiveAdmin admin = getActiveAdminUncheckedLocked(adminReceiver, userHandle);
        DevicePolicyData policy = getUserData(userHandle);
        if (admin != null && !policy.mRemovingAdmins.contains(adminReceiver)) {
            policy.mRemovingAdmins.add(adminReceiver);
            sendAdminCommandLocked(admin, "android.app.action.DEVICE_ADMIN_DISABLED", new BroadcastReceiver() {
                /* class com.android.server.devicepolicy.DevicePolicyManagerService.AnonymousClass5 */

                @Override // android.content.BroadcastReceiver
                public void onReceive(Context context, Intent intent) {
                    DevicePolicyManagerService.this.removeAdminArtifacts(adminReceiver, userHandle);
                    DevicePolicyManagerService.this.removePackageIfRequired(adminReceiver.getPackageName(), userHandle);
                }
            });
            Flog.i(305, "removeActiveAdminLocked(" + adminReceiver + "), by user " + userHandle);
        }
    }

    public DeviceAdminInfo findAdmin(ComponentName adminName, int userHandle, boolean throwForMissingPermission) {
        if (!this.mHasFeature) {
            return null;
        }
        enforceFullCrossUsersPermission(userHandle);
        ActivityInfo ai = null;
        try {
            ai = this.mIPackageManager.getReceiverInfo(adminName, 819328, userHandle);
        } catch (RemoteException e) {
            Slog.e(LOG_TAG, "cannot find admin.");
        }
        if (ai != null) {
            if (!"android.permission.BIND_DEVICE_ADMIN".equals(ai.permission)) {
                String message = "DeviceAdminReceiver " + adminName + " must be protected with android.permission.BIND_DEVICE_ADMIN";
                Slog.w(LOG_TAG, message);
                if (throwForMissingPermission && ai.applicationInfo.targetSdkVersion > 23) {
                    throw new IllegalArgumentException(message);
                }
            }
            try {
                return new DeviceAdminInfo(this.mContext, ai);
            } catch (IOException | XmlPullParserException e2) {
                Slog.w(LOG_TAG, "Bad device admin requested for user=" + userHandle + ": " + adminName, e2);
                return null;
            }
        } else {
            throw new IllegalArgumentException("Unknown admin: " + adminName);
        }
    }

    private File getPolicyFileDirectory(int userId) {
        if (userId == 0) {
            return new File(this.mInjector.getDevicePolicyFilePathForSystemUser());
        }
        return this.mInjector.environmentGetUserSystemDirectory(userId);
    }

    private JournaledFile makeJournaledFile(int userId) {
        String base = new File(getPolicyFileDirectory(userId), DEVICE_POLICIES_XML).getAbsolutePath();
        HwCustDevicePolicyManagerService mHwCustDPMS = (HwCustDevicePolicyManagerService) HwCustUtils.createObj(HwCustDevicePolicyManagerService.class, new Object[0]);
        if (mHwCustDPMS != null && mHwCustDPMS.shouldActiveDeviceAdmins(base)) {
            mHwCustDPMS.activeDeviceAdmins(base);
        }
        File file = new File(base);
        return new JournaledFile(file, new File(base + ".tmp"));
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:105:0x032f A[SYNTHETIC, Splitter:B:105:0x032f] */
    public void saveSettingsLocked(int userHandle) {
        JournaledFile journal;
        Exception e;
        int N;
        DevicePolicyData policy = getUserData(userHandle);
        JournaledFile journal2 = makeJournaledFile(userHandle);
        FileOutputStream stream = new FileOutputStream(journal2.chooseForWrite(), false);
        XmlSerializer out = new FastXmlSerializer();
        out.setOutput(stream, StandardCharsets.UTF_8.name());
        out.startDocument(null, true);
        out.startTag(null, "policies");
        if (policy.mRestrictionsProvider != null) {
            out.attribute(null, ATTR_PERMISSION_PROVIDER, policy.mRestrictionsProvider.flattenToString());
        }
        try {
            if (policy.mUserSetupComplete) {
                try {
                    out.attribute(null, ATTR_SETUP_COMPLETE, Boolean.toString(true));
                } catch (IOException | XmlPullParserException e2) {
                    e = e2;
                    journal = journal2;
                    Slog.w(LOG_TAG, "failed writing file", e);
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (IOException e3) {
                            Slog.e(LOG_TAG, "cannot close the stream.");
                        }
                    }
                    journal.rollback();
                }
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
            for (int i = 0; i < policy.mDelegationMap.size(); i++) {
                String scope = policy.mDelegationMap.keyAt(i);
                List<String> scopes = policy.mDelegationMap.valueAt(i);
                for (String scope2 : scopes) {
                    out.startTag(null, "delegation");
                    out.attribute(null, "delegatePackage", scope);
                    out.attribute(null, "scope", scope2);
                    out.endTag(null, "delegation");
                    scopes = scopes;
                    journal2 = journal2;
                    scope = scope;
                }
            }
            journal = journal2;
            int N2 = policy.mAdminList.size();
            int i2 = 0;
            while (i2 < N2) {
                try {
                    ActiveAdmin ap = policy.mAdminList.get(i2);
                    if (ap != null) {
                        out.startTag(null, "admin");
                        N = N2;
                        out.attribute(null, "name", ap.info.getComponent().flattenToString());
                        ap.writeToXml(out);
                        out.endTag(null, "admin");
                    } else {
                        N = N2;
                    }
                    i2++;
                    N2 = N;
                } catch (IOException | XmlPullParserException e4) {
                    e = e4;
                    Slog.w(LOG_TAG, "failed writing file", e);
                    if (stream != null) {
                    }
                    journal.rollback();
                }
            }
            if (this.mHwDevicePolicyManagerService != null) {
                this.mHwDevicePolicyManagerService.setHwSpecialPolicyToXml(out);
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
            if (!this.mInjector.storageManagerIsFileBasedEncryptionEnabled()) {
                out.startTag(null, TAG_PASSWORD_VALIDITY);
                out.attribute(null, ATTR_VALUE, Boolean.toString(policy.mPasswordValidAtLastCheckpoint));
                out.endTag(null, TAG_PASSWORD_VALIDITY);
            }
            for (int i3 = 0; i3 < policy.mAcceptedCaCertificates.size(); i3++) {
                out.startTag(null, TAG_ACCEPTED_CA_CERTIFICATES);
                out.attribute(null, "name", policy.mAcceptedCaCertificates.valueAt(i3));
                out.endTag(null, TAG_ACCEPTED_CA_CERTIFICATES);
            }
            for (int i4 = 0; i4 < policy.mLockTaskPackages.size(); i4++) {
                out.startTag(null, TAG_LOCK_TASK_COMPONENTS);
                out.attribute(null, "name", policy.mLockTaskPackages.get(i4));
                out.endTag(null, TAG_LOCK_TASK_COMPONENTS);
            }
            if (policy.mLockTaskFeatures != 0) {
                out.startTag(null, TAG_LOCK_TASK_FEATURES);
                out.attribute(null, ATTR_VALUE, Integer.toString(policy.mLockTaskFeatures));
                out.endTag(null, TAG_LOCK_TASK_FEATURES);
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
        } catch (IOException | XmlPullParserException e5) {
            e = e5;
            journal = journal2;
            Slog.w(LOG_TAG, "failed writing file", e);
            if (stream != null) {
            }
            journal.rollback();
        }
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

    /* JADX WARNING: Code restructure failed: missing block: B:185:0x0428, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:186:0x0429, code lost:
        r4 = r0;
        r8 = r17;
     */
    /* JADX WARNING: Removed duplicated region for block: B:185:0x0428 A[ExcHandler: IOException | IndexOutOfBoundsException | NullPointerException | NumberFormatException | XmlPullParserException (r0v6 'e' java.lang.Exception A[CUSTOM_DECLARE]), Splitter:B:86:0x0195] */
    /* JADX WARNING: Removed duplicated region for block: B:198:0x0466 A[SYNTHETIC, Splitter:B:198:0x0466] */
    /* JADX WARNING: Removed duplicated region for block: B:204:0x0480  */
    /* JADX WARNING: Removed duplicated region for block: B:207:0x048d  */
    /* JADX WARNING: Removed duplicated region for block: B:210:0x04a0  */
    /* JADX WARNING: Removed duplicated region for block: B:219:? A[RETURN, SYNTHETIC] */
    private void loadSettingsLocked(DevicePolicyData policy, int userHandle) {
        IHwDevicePolicyManagerService iHwDevicePolicyManagerService;
        Exception e;
        XmlPullParser parser;
        FileInputStream stream;
        int type;
        int outerDepth;
        RuntimeException e2;
        FileInputStream stream2 = null;
        File file = makeJournaledFile(userHandle).chooseForRead();
        boolean needsRewrite = false;
        try {
            stream2 = new FileInputStream(file);
            try {
                parser = Xml.newPullParser();
                parser.setInput(stream2, StandardCharsets.UTF_8.name());
                String tag = parser.getName();
                if ("policies".equals(tag)) {
                    String permissionProvider = parser.getAttributeValue(null, ATTR_PERMISSION_PROVIDER);
                    if (permissionProvider != null) {
                        try {
                            policy.mRestrictionsProvider = ComponentName.unflattenFromString(permissionProvider);
                        } catch (FileNotFoundException e3) {
                        } catch (IOException | IndexOutOfBoundsException | NullPointerException | NumberFormatException | XmlPullParserException e4) {
                            e = e4;
                            Slog.w(LOG_TAG, "failed parsing " + file, e);
                            if (stream2 != null) {
                            }
                            policy.mAdminList.addAll(policy.mAdminMap.values());
                            if (needsRewrite) {
                            }
                            validatePasswordOwnerLocked(policy);
                            updateMaximumTimeToLockLocked(userHandle);
                            iHwDevicePolicyManagerService = this.mHwDevicePolicyManagerService;
                            if (iHwDevicePolicyManagerService != null) {
                            }
                            updateLockTaskPackagesLocked(policy.mLockTaskPackages, userHandle);
                            updateLockTaskFeaturesLocked(policy.mLockTaskFeatures, userHandle);
                            if (policy.mStatusBarDisabled) {
                            }
                        }
                    }
                    try {
                        String userSetupComplete = parser.getAttributeValue(null, ATTR_SETUP_COMPLETE);
                        if (userSetupComplete != null) {
                            stream = stream2;
                            try {
                                if (Boolean.toString(true).equals(userSetupComplete)) {
                                    policy.mUserSetupComplete = true;
                                }
                            } catch (FileNotFoundException e5) {
                                stream2 = stream;
                                Slog.e(LOG_TAG, "invalid path.");
                                if (stream2 != null) {
                                }
                                policy.mAdminList.addAll(policy.mAdminMap.values());
                                if (needsRewrite) {
                                }
                                validatePasswordOwnerLocked(policy);
                                updateMaximumTimeToLockLocked(userHandle);
                                iHwDevicePolicyManagerService = this.mHwDevicePolicyManagerService;
                                if (iHwDevicePolicyManagerService != null) {
                                }
                                updateLockTaskPackagesLocked(policy.mLockTaskPackages, userHandle);
                                updateLockTaskFeaturesLocked(policy.mLockTaskFeatures, userHandle);
                                if (policy.mStatusBarDisabled) {
                                }
                            }
                        } else {
                            stream = stream2;
                        }
                        String paired = parser.getAttributeValue(null, ATTR_DEVICE_PAIRED);
                        if (paired != null) {
                            if (Boolean.toString(true).equals(paired)) {
                                policy.mPaired = true;
                            }
                        }
                        String deviceProvisioningConfigApplied = parser.getAttributeValue(null, ATTR_DEVICE_PROVISIONING_CONFIG_APPLIED);
                        if (deviceProvisioningConfigApplied != null) {
                            if (Boolean.toString(true).equals(deviceProvisioningConfigApplied)) {
                                policy.mDeviceProvisioningConfigApplied = true;
                            }
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
                            List<String> scopes = policy.mDelegationMap.get(certDelegate);
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
                            List<String> scopes2 = policy.mDelegationMap.get(appRestrictionsDelegate);
                            if (scopes2 == null) {
                                scopes2 = new ArrayList();
                                policy.mDelegationMap.put(appRestrictionsDelegate, scopes2);
                            }
                            if (!scopes2.contains("delegation-app-restrictions")) {
                                scopes2.add("delegation-app-restrictions");
                                needsRewrite = true;
                            }
                        }
                        parser.next();
                        int outerDepth2 = parser.getDepth();
                        policy.mLockTaskPackages.clear();
                        policy.mAdminList.clear();
                        policy.mAdminMap.clear();
                        policy.mAffiliationIds.clear();
                        policy.mOwnerInstalledCaCerts.clear();
                        while (true) {
                            int type2 = parser.next();
                            if (type2 != 1) {
                                if (type2 == 3 && parser.getDepth() <= outerDepth2) {
                                    break;
                                }
                                if (type2 == 3) {
                                    type = type2;
                                    outerDepth = outerDepth2;
                                } else if (type2 == 4) {
                                    type = type2;
                                    outerDepth = outerDepth2;
                                } else {
                                    String tag2 = parser.getName();
                                    if (this.mHwDevicePolicyManagerService != null) {
                                        this.mHwDevicePolicyManagerService.loadHwSpecialPolicyFromXml(parser);
                                    }
                                    if ("admin".equals(tag2)) {
                                        try {
                                            String name = parser.getAttributeValue(null, "name");
                                            try {
                                                DeviceAdminInfo dai = findAdmin(ComponentName.unflattenFromString(name), userHandle, false);
                                                if (dai != null) {
                                                    boolean shouldOverwritePolicies = shouldOverwritePoliciesFromXml(dai.getComponent(), userHandle);
                                                    type = type2;
                                                    try {
                                                        ActiveAdmin ap = new ActiveAdmin(dai, false);
                                                        ap.readFromXml(parser, shouldOverwritePolicies);
                                                        outerDepth = outerDepth2;
                                                        try {
                                                            policy.mAdminMap.put(ap.info.getComponent(), ap);
                                                        } catch (RuntimeException e6) {
                                                            e2 = e6;
                                                        }
                                                    } catch (RuntimeException e7) {
                                                        outerDepth = outerDepth2;
                                                        e2 = e7;
                                                        Slog.w(LOG_TAG, "Failed loading admin " + name, e2);
                                                        outerDepth2 = outerDepth;
                                                    }
                                                } else {
                                                    type = type2;
                                                    outerDepth = outerDepth2;
                                                }
                                            } catch (RuntimeException e8) {
                                                type = type2;
                                                outerDepth = outerDepth2;
                                                e2 = e8;
                                                Slog.w(LOG_TAG, "Failed loading admin " + name, e2);
                                                outerDepth2 = outerDepth;
                                            }
                                        } catch (IOException | IndexOutOfBoundsException | NullPointerException | NumberFormatException | XmlPullParserException e9) {
                                        }
                                    } else {
                                        type = type2;
                                        outerDepth = outerDepth2;
                                        if ("delegation".equals(tag2)) {
                                            String delegatePackage = parser.getAttributeValue(null, "delegatePackage");
                                            String scope = parser.getAttributeValue(null, "scope");
                                            List<String> scopes3 = policy.mDelegationMap.get(delegatePackage);
                                            if (scopes3 == null) {
                                                scopes3 = new ArrayList();
                                                policy.mDelegationMap.put(delegatePackage, scopes3);
                                            }
                                            if (!scopes3.contains(scope)) {
                                                scopes3.add(scope);
                                            }
                                        } else if ("failed-password-attempts".equals(tag2)) {
                                            policy.mFailedPasswordAttempts = Integer.parseInt(parser.getAttributeValue(null, ATTR_VALUE));
                                        } else if ("password-owner".equals(tag2)) {
                                            policy.mPasswordOwner = Integer.parseInt(parser.getAttributeValue(null, ATTR_VALUE));
                                        } else if (TAG_ACCEPTED_CA_CERTIFICATES.equals(tag2)) {
                                            policy.mAcceptedCaCertificates.add(parser.getAttributeValue(null, "name"));
                                        } else if (TAG_LOCK_TASK_COMPONENTS.equals(tag2)) {
                                            policy.mLockTaskPackages.add(parser.getAttributeValue(null, "name"));
                                        } else if (TAG_LOCK_TASK_FEATURES.equals(tag2)) {
                                            policy.mLockTaskFeatures = Integer.parseInt(parser.getAttributeValue(null, ATTR_VALUE));
                                        } else if (TAG_STATUS_BAR.equals(tag2)) {
                                            policy.mStatusBarDisabled = Boolean.parseBoolean(parser.getAttributeValue(null, ATTR_DISABLED));
                                        } else if (DO_NOT_ASK_CREDENTIALS_ON_BOOT_XML.equals(tag2)) {
                                            policy.doNotAskCredentialsOnBoot = true;
                                        } else if (TAG_AFFILIATION_ID.equals(tag2)) {
                                            policy.mAffiliationIds.add(parser.getAttributeValue(null, ATTR_ID));
                                        } else if (TAG_LAST_SECURITY_LOG_RETRIEVAL.equals(tag2)) {
                                            policy.mLastSecurityLogRetrievalTime = Long.parseLong(parser.getAttributeValue(null, ATTR_VALUE));
                                        } else if (TAG_LAST_BUG_REPORT_REQUEST.equals(tag2)) {
                                            policy.mLastBugReportRequestTime = Long.parseLong(parser.getAttributeValue(null, ATTR_VALUE));
                                        } else if (TAG_LAST_NETWORK_LOG_RETRIEVAL.equals(tag2)) {
                                            policy.mLastNetworkLogsRetrievalTime = Long.parseLong(parser.getAttributeValue(null, ATTR_VALUE));
                                        } else if (TAG_ADMIN_BROADCAST_PENDING.equals(tag2)) {
                                            policy.mAdminBroadcastPending = Boolean.toString(true).equals(parser.getAttributeValue(null, ATTR_VALUE));
                                        } else if (TAG_INITIALIZATION_BUNDLE.equals(tag2)) {
                                            policy.mInitBundle = PersistableBundle.restoreFromXml(parser);
                                        } else if ("active-password".equals(tag2)) {
                                            needsRewrite = true;
                                            outerDepth2 = outerDepth;
                                        } else if (TAG_PASSWORD_VALIDITY.equals(tag2)) {
                                            if (!this.mInjector.storageManagerIsFileBasedEncryptionEnabled()) {
                                                policy.mPasswordValidAtLastCheckpoint = Boolean.parseBoolean(parser.getAttributeValue(null, ATTR_VALUE));
                                            }
                                        } else if (TAG_PASSWORD_TOKEN_HANDLE.equals(tag2)) {
                                            policy.mPasswordTokenHandle = Long.parseLong(parser.getAttributeValue(null, ATTR_VALUE));
                                        } else if (TAG_CURRENT_INPUT_METHOD_SET.equals(tag2)) {
                                            policy.mCurrentInputMethodSet = true;
                                        } else if (TAG_OWNER_INSTALLED_CA_CERT.equals(tag2)) {
                                            policy.mOwnerInstalledCaCerts.add(parser.getAttributeValue(null, ATTR_ALIAS));
                                        } else {
                                            Slog.w(LOG_TAG, "Unknown tag: " + tag2);
                                            XmlUtils.skipCurrentTag(parser);
                                        }
                                    }
                                }
                                outerDepth2 = outerDepth;
                            } else {
                                break;
                            }
                        }
                        stream2 = stream;
                    } catch (FileNotFoundException e10) {
                        Slog.e(LOG_TAG, "invalid path.");
                        if (stream2 != null) {
                        }
                        policy.mAdminList.addAll(policy.mAdminMap.values());
                        if (needsRewrite) {
                        }
                        validatePasswordOwnerLocked(policy);
                        updateMaximumTimeToLockLocked(userHandle);
                        iHwDevicePolicyManagerService = this.mHwDevicePolicyManagerService;
                        if (iHwDevicePolicyManagerService != null) {
                        }
                        updateLockTaskPackagesLocked(policy.mLockTaskPackages, userHandle);
                        updateLockTaskFeaturesLocked(policy.mLockTaskFeatures, userHandle);
                        if (policy.mStatusBarDisabled) {
                        }
                    } catch (IOException | IndexOutOfBoundsException | NullPointerException | NumberFormatException | XmlPullParserException e11) {
                        e = e11;
                        Slog.w(LOG_TAG, "failed parsing " + file, e);
                        if (stream2 != null) {
                        }
                        policy.mAdminList.addAll(policy.mAdminMap.values());
                        if (needsRewrite) {
                        }
                        validatePasswordOwnerLocked(policy);
                        updateMaximumTimeToLockLocked(userHandle);
                        iHwDevicePolicyManagerService = this.mHwDevicePolicyManagerService;
                        if (iHwDevicePolicyManagerService != null) {
                        }
                        updateLockTaskPackagesLocked(policy.mLockTaskPackages, userHandle);
                        updateLockTaskFeaturesLocked(policy.mLockTaskFeatures, userHandle);
                        if (policy.mStatusBarDisabled) {
                        }
                    }
                    if (stream2 != null) {
                        try {
                            stream2.close();
                        } catch (IOException e12) {
                            Slog.e(LOG_TAG, "cannot close the stream.");
                        }
                    }
                    policy.mAdminList.addAll(policy.mAdminMap.values());
                    if (needsRewrite) {
                        saveSettingsLocked(userHandle);
                    }
                    validatePasswordOwnerLocked(policy);
                    updateMaximumTimeToLockLocked(userHandle);
                    iHwDevicePolicyManagerService = this.mHwDevicePolicyManagerService;
                    if (iHwDevicePolicyManagerService != null) {
                        iHwDevicePolicyManagerService.syncHwDeviceSettingsLocked(policy.mUserHandle);
                    }
                    updateLockTaskPackagesLocked(policy.mLockTaskPackages, userHandle);
                    updateLockTaskFeaturesLocked(policy.mLockTaskFeatures, userHandle);
                    if (policy.mStatusBarDisabled) {
                        setStatusBarDisabledInternal(policy.mStatusBarDisabled, userHandle);
                        return;
                    }
                    return;
                }
                throw new XmlPullParserException("Settings do not start with policies tag: found " + tag);
            } catch (FileNotFoundException e13) {
                Slog.e(LOG_TAG, "invalid path.");
                if (stream2 != null) {
                }
                policy.mAdminList.addAll(policy.mAdminMap.values());
                if (needsRewrite) {
                }
                validatePasswordOwnerLocked(policy);
                updateMaximumTimeToLockLocked(userHandle);
                iHwDevicePolicyManagerService = this.mHwDevicePolicyManagerService;
                if (iHwDevicePolicyManagerService != null) {
                }
                updateLockTaskPackagesLocked(policy.mLockTaskPackages, userHandle);
                updateLockTaskFeaturesLocked(policy.mLockTaskFeatures, userHandle);
                if (policy.mStatusBarDisabled) {
                }
            }
        } catch (FileNotFoundException e14) {
            Slog.e(LOG_TAG, "invalid path.");
            if (stream2 != null) {
            }
            policy.mAdminList.addAll(policy.mAdminMap.values());
            if (needsRewrite) {
            }
            validatePasswordOwnerLocked(policy);
            updateMaximumTimeToLockLocked(userHandle);
            iHwDevicePolicyManagerService = this.mHwDevicePolicyManagerService;
            if (iHwDevicePolicyManagerService != null) {
            }
            updateLockTaskPackagesLocked(policy.mLockTaskPackages, userHandle);
            updateLockTaskFeaturesLocked(policy.mLockTaskFeatures, userHandle);
            if (policy.mStatusBarDisabled) {
            }
        } catch (IOException | IndexOutOfBoundsException | NullPointerException | NumberFormatException | XmlPullParserException e15) {
            e = e15;
            Slog.w(LOG_TAG, "failed parsing " + file, e);
            if (stream2 != null) {
            }
            policy.mAdminList.addAll(policy.mAdminMap.values());
            if (needsRewrite) {
            }
            validatePasswordOwnerLocked(policy);
            updateMaximumTimeToLockLocked(userHandle);
            iHwDevicePolicyManagerService = this.mHwDevicePolicyManagerService;
            if (iHwDevicePolicyManagerService != null) {
            }
            updateLockTaskPackagesLocked(policy.mLockTaskPackages, userHandle);
            updateLockTaskFeaturesLocked(policy.mLockTaskFeatures, userHandle);
            if (policy.mStatusBarDisabled) {
            }
        }
        while (true) {
            int type3 = parser.next();
            if (type3 == 1 || type3 == 2) {
                break;
            }
        }
    }

    private boolean shouldOverwritePoliciesFromXml(ComponentName deviceAdminComponent, int userHandle) {
        return !isProfileOwner(deviceAdminComponent, userHandle) && !isDeviceOwner(deviceAdminComponent, userHandle);
    }

    private void updateLockTaskPackagesLocked(List<String> packages, int userId) {
        long ident = this.mInjector.binderClearCallingIdentity();
        try {
            this.mInjector.getIActivityManager().updateLockTaskPackages(userId, (String[]) packages.toArray(new String[packages.size()]));
        } catch (RemoteException e) {
            Slog.e(LOG_TAG, "cannot update lock task packages locked.");
        } catch (Throwable th) {
            this.mInjector.binderRestoreCallingIdentity(ident);
            throw th;
        }
        this.mInjector.binderRestoreCallingIdentity(ident);
    }

    private void updateLockTaskFeaturesLocked(int flags, int userId) {
        long ident = this.mInjector.binderClearCallingIdentity();
        try {
            this.mInjector.getIActivityTaskManager().updateLockTaskFeatures(userId, flags);
        } catch (RemoteException e) {
            Slog.e(LOG_TAG, "cannot update LockTask features locked.");
        } catch (Throwable th) {
            this.mInjector.binderRestoreCallingIdentity(ident);
            throw th;
        }
        this.mInjector.binderRestoreCallingIdentity(ident);
    }

    private void updateDeviceOwnerLocked() {
        long ident = this.mInjector.binderClearCallingIdentity();
        try {
            ComponentName deviceOwnerComponent = this.mOwners.getDeviceOwnerComponent();
            if (deviceOwnerComponent != null) {
                this.mInjector.getIActivityManager().updateDeviceOwner(deviceOwnerComponent.getPackageName());
            }
        } catch (RemoteException e) {
            Slog.e(LOG_TAG, "cannot update deviceOwner locked.");
        } catch (Throwable th) {
            this.mInjector.binderRestoreCallingIdentity(ident);
            throw th;
        }
        this.mInjector.binderRestoreCallingIdentity(ident);
    }

    static void validateQualityConstant(int quality) {
        if (quality != 0 && quality != 32768 && quality != 65536 && quality != 131072 && quality != 196608 && quality != 262144 && quality != 327680 && quality != 393216 && quality != 524288) {
            throw new IllegalArgumentException("Invalid quality constant: 0x" + Integer.toHexString(quality));
        }
    }

    /* access modifiers changed from: package-private */
    public void validatePasswordOwnerLocked(DevicePolicyData policy) {
        if (policy.mPasswordOwner >= 0) {
            boolean haveOwner = false;
            int i = policy.mAdminList.size() - 1;
            while (true) {
                if (i < 0) {
                    break;
                } else if (policy.mAdminList.get(i).getUid() == policy.mPasswordOwner) {
                    haveOwner = true;
                    break;
                } else {
                    i--;
                }
            }
            if (!haveOwner) {
                Slog.w(LOG_TAG, "Previous password owner " + policy.mPasswordOwner + " no longer active; disabling");
                policy.mPasswordOwner = -1;
            }
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.devicepolicy.BaseIDevicePolicyManager
    @VisibleForTesting
    public void systemReady(int phase) {
        if (this.mHasFeature) {
            if (phase == 480) {
                onLockSettingsReady();
                loadAdminDataAsync();
                this.mOwners.systemReady();
            } else if (phase == 550) {
                maybeStartSecurityLogMonitorOnActivityManagerReady();
            } else if (phase == 1000) {
                ensureDeviceOwnerUserStarted();
            }
            IHwDevicePolicyManagerService iHwDevicePolicyManagerService = this.mHwDevicePolicyManagerService;
            if (iHwDevicePolicyManagerService != null) {
                iHwDevicePolicyManagerService.systemReady(phase);
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
        maybeLogStart();
        this.mSetupContentObserver.register();
        updateUserSetupCompleteAndPaired();
        synchronized (getLockObject()) {
            packageList = getKeepUninstalledPackagesLocked();
        }
        if (packageList != null) {
            this.mInjector.getPackageManagerInternal().setKeepUninstalledPackages(packageList);
        }
        synchronized (getLockObject()) {
            ActiveAdmin deviceOwner = getDeviceOwnerAdminLocked();
            if (deviceOwner != null) {
                this.mUserManagerInternal.setForceEphemeralUsers(deviceOwner.forceEphemeralUsers);
                ActivityManagerInternal activityManagerInternal = this.mInjector.getActivityManagerInternal();
                activityManagerInternal.setSwitchingFromSystemUserMessage(deviceOwner.startUserSessionMessage);
                activityManagerInternal.setSwitchingToSystemUserMessage(deviceOwner.endUserSessionMessage);
            }
            revertTransferOwnershipIfNecessaryLocked();
        }
    }

    private void revertTransferOwnershipIfNecessaryLocked() {
        if (this.mTransferOwnershipMetadataManager.metadataFileExists()) {
            Slog.e(LOG_TAG, "Owner transfer metadata file exists! Reverting transfer.");
            TransferOwnershipMetadataManager.Metadata metadata = this.mTransferOwnershipMetadataManager.loadMetadataFile();
            if (metadata.adminType.equals(LOG_TAG_PROFILE_OWNER)) {
                transferProfileOwnershipLocked(metadata.targetComponent, metadata.sourceComponent, metadata.userId);
                deleteTransferOwnershipMetadataFileLocked();
                deleteTransferOwnershipBundleLocked(metadata.userId);
            } else if (metadata.adminType.equals(LOG_TAG_DEVICE_OWNER)) {
                transferDeviceOwnershipLocked(metadata.targetComponent, metadata.sourceComponent, metadata.userId);
                deleteTransferOwnershipMetadataFileLocked();
                deleteTransferOwnershipBundleLocked(metadata.userId);
            }
            updateSystemUpdateFreezePeriodsRecord(true);
        }
    }

    private void maybeLogStart() {
        if (SecurityLog.isLoggingEnabled()) {
            String verifiedBootState = this.mInjector.systemPropertiesGet("ro.boot.verifiedbootstate");
            String verityMode = this.mInjector.systemPropertiesGet("ro.boot.veritymode");
            SecurityLog.writeEvent(210009, new Object[]{verifiedBootState, verityMode});
            Log.i(MDPP_TAG, "TAG 210009 verifiedBootState " + verifiedBootState + " verityMode " + verityMode);
        }
    }

    private void ensureDeviceOwnerUserStarted() {
        int userId;
        synchronized (getLockObject()) {
            if (this.mOwners.hasDeviceOwner()) {
                userId = this.mOwners.getDeviceOwnerUserId();
            } else {
                return;
            }
        }
        if (userId != 0) {
            try {
                this.mInjector.getIActivityManager().startUserInBackground(userId);
            } catch (RemoteException e) {
                Slog.w(LOG_TAG, "Exception starting user", e);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.devicepolicy.BaseIDevicePolicyManager
    public void handleStartUser(int userId) {
        updateScreenCaptureDisabled(userId, getScreenCaptureDisabled(null, userId));
        pushUserRestrictions(userId);
        updatePasswordQualityCacheForUserGroup(userId == 0 ? -1 : userId);
        startOwnerService(userId, "start-user");
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.devicepolicy.BaseIDevicePolicyManager
    public void handleUnlockUser(int userId) {
        startOwnerService(userId, "unlock-user");
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.devicepolicy.BaseIDevicePolicyManager
    public void handleStopUser(int userId) {
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
        Collection<? extends Integer> usersWithProfileOwners;
        ArraySet arraySet;
        synchronized (getLockObject()) {
            usersWithProfileOwners = this.mOwners.getProfileOwnerKeys();
            arraySet = new ArraySet();
            for (int i = 0; i < this.mUserData.size(); i++) {
                arraySet.add(Integer.valueOf(this.mUserData.keyAt(i)));
            }
        }
        List<UserInfo> allUsers = this.mUserManager.getUsers();
        Set<Integer> deletedUsers = new ArraySet<>();
        deletedUsers.addAll(usersWithProfileOwners);
        deletedUsers.addAll(arraySet);
        for (UserInfo userInfo : allUsers) {
            deletedUsers.remove(Integer.valueOf(userInfo.id));
        }
        for (Integer userId : deletedUsers) {
            removeUserData(userId.intValue());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handlePasswordExpirationNotification(int userHandle) {
        Bundle adminExtras = new Bundle();
        adminExtras.putParcelable("android.intent.extra.USER", UserHandle.of(userHandle));
        synchronized (getLockObject()) {
            long now = System.currentTimeMillis();
            List<ActiveAdmin> admins = getActiveAdminsForLockscreenPoliciesLocked(userHandle, false);
            int N = admins.size();
            for (int i = 0; i < N; i++) {
                ActiveAdmin admin = admins.get(i);
                if (admin.info.usesPolicy(6) && admin.passwordExpirationTimeout > 0 && now >= admin.passwordExpirationDate - getUsrSetExtendTime() && admin.passwordExpirationDate > 0) {
                    sendAdminCommandLocked(admin, "android.app.action.ACTION_PASSWORD_EXPIRING", adminExtras, (BroadcastReceiver) null);
                }
            }
            setExpirationAlarmCheckLocked(this.mContext, userHandle, false);
        }
    }

    /* access modifiers changed from: protected */
    public long getUsrSetExtendTime() {
        long usrSetExtendTime = -1;
        IHwDevicePolicyManagerService iHwDevicePolicyManagerService = this.mHwDevicePolicyManagerService;
        if (iHwDevicePolicyManagerService != null) {
            usrSetExtendTime = iHwDevicePolicyManagerService.getUsrSetExtendTime();
        }
        if (usrSetExtendTime < 0) {
            return EXPIRATION_GRACE_PERIOD_MS;
        }
        return usrSetExtendTime;
    }

    /* access modifiers changed from: protected */
    public void onInstalledCertificatesChanged(UserHandle userHandle, Collection<String> installedCertificates) {
        if (this.mHasFeature) {
            enforceManageUsers();
            synchronized (getLockObject()) {
                DevicePolicyData policy = getUserData(userHandle.getIdentifier());
                if ((false | policy.mAcceptedCaCertificates.retainAll(installedCertificates)) || policy.mOwnerInstalledCaCerts.retainAll(installedCertificates)) {
                    saveSettingsLocked(userHandle.getIdentifier());
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public Set<String> getAcceptedCaCertificates(UserHandle userHandle) {
        ArraySet<String> arraySet;
        if (!this.mHasFeature) {
            return Collections.emptySet();
        }
        synchronized (getLockObject()) {
            arraySet = getUserData(userHandle.getIdentifier()).mAcceptedCaCertificates;
        }
        return arraySet;
    }

    public void setActiveAdmin(ComponentName adminReceiver, boolean refreshing, int userHandle) {
        if (this.mHasFeature) {
            setActiveAdmin(adminReceiver, refreshing, userHandle, null);
        }
    }

    private void setActiveAdmin(ComponentName adminReceiver, boolean refreshing, int userHandle, Bundle onEnableData) {
        Throwable th;
        ActiveAdmin newAdmin;
        boolean z;
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_DEVICE_ADMINS", null);
        enforceFullCrossUsersPermission(userHandle);
        DevicePolicyData policy = getUserData(userHandle);
        DeviceAdminInfo info = findAdmin(adminReceiver, userHandle, true);
        synchronized (getLockObject()) {
            try {
                checkActiveAdminPrecondition(adminReceiver, info, policy);
                long ident = this.mInjector.binderClearCallingIdentity();
                try {
                    ActiveAdmin existingAdmin = getActiveAdminUncheckedLocked(adminReceiver, userHandle);
                    if (!refreshing) {
                        if (existingAdmin != null) {
                            throw new IllegalArgumentException("Admin is already added");
                        }
                    }
                    newAdmin = new ActiveAdmin(info, false);
                    if (existingAdmin != null) {
                        z = existingAdmin.testOnlyAdmin;
                    } else {
                        z = isPackageTestOnly(adminReceiver.getPackageName(), userHandle);
                    }
                    newAdmin.testOnlyAdmin = z;
                    policy.mAdminMap.put(adminReceiver, newAdmin);
                    int replaceIndex = -1;
                    int N = policy.mAdminList.size();
                    int i = 0;
                    while (true) {
                        if (i >= N) {
                            break;
                        } else if (policy.mAdminList.get(i).info.getComponent().equals(adminReceiver)) {
                            replaceIndex = i;
                            break;
                        } else {
                            i++;
                        }
                    }
                    if (replaceIndex == -1) {
                        policy.mAdminList.add(newAdmin);
                        enableIfNecessary(info.getPackageName(), userHandle);
                        this.mUsageStatsManagerInternal.onActiveAdminAdded(adminReceiver.getPackageName(), userHandle);
                    } else {
                        policy.mAdminList.set(replaceIndex, newAdmin);
                    }
                    saveSettingsLocked(userHandle);
                } catch (Throwable th2) {
                    th = th2;
                    this.mInjector.binderRestoreCallingIdentity(ident);
                    throw th;
                }
                try {
                    sendAdminCommandLocked(newAdmin, "android.app.action.DEVICE_ADMIN_ENABLED", onEnableData, (BroadcastReceiver) null);
                    Flog.i(305, "setActiveAdmin(" + adminReceiver + "), by user " + userHandle);
                    if (this.mHwDevicePolicyManagerService != null) {
                        this.mHwDevicePolicyManagerService.notifyActiveAdmin(adminReceiver, userHandle);
                    }
                    this.mInjector.binderRestoreCallingIdentity(ident);
                } catch (Throwable th3) {
                    th = th3;
                    throw th;
                }
            } catch (Throwable th4) {
                th = th4;
            }
        }
    }

    private void loadAdminDataAsync() {
        this.mInjector.postOnSystemServerInitThreadPool(new Runnable() {
            /* class com.android.server.devicepolicy.$$Lambda$DevicePolicyManagerService$_NwYGl5ncBgLJs8W81WNW6xoU */

            @Override // java.lang.Runnable
            public final void run() {
                DevicePolicyManagerService.this.lambda$loadAdminDataAsync$0$DevicePolicyManagerService();
            }
        });
    }

    public /* synthetic */ void lambda$loadAdminDataAsync$0$DevicePolicyManagerService() {
        pushActiveAdminPackages();
        this.mUsageStatsManagerInternal.onAdminDataAvailable();
        pushAllMeteredRestrictedPackages();
        this.mInjector.getNetworkPolicyManagerInternal().onAdminDataAvailable();
    }

    private void pushActiveAdminPackages() {
        synchronized (getLockObject()) {
            List<UserInfo> users = this.mUserManager.getUsers();
            for (int i = users.size() - 1; i >= 0; i--) {
                int userId = users.get(i).id;
                this.mUsageStatsManagerInternal.setActiveAdminApps(getActiveAdminPackagesLocked(userId), userId);
            }
        }
    }

    private void pushAllMeteredRestrictedPackages() {
        synchronized (getLockObject()) {
            List<UserInfo> users = this.mUserManager.getUsers();
            for (int i = users.size() - 1; i >= 0; i--) {
                int userId = users.get(i).id;
                this.mInjector.getNetworkPolicyManagerInternal().setMeteredRestrictedPackagesAsync(getMeteredDisabledPackagesLocked(userId), userId);
            }
        }
    }

    private void pushActiveAdminPackagesLocked(int userId) {
        this.mUsageStatsManagerInternal.setActiveAdminApps(getActiveAdminPackagesLocked(userId), userId);
    }

    private Set<String> getActiveAdminPackagesLocked(int userId) {
        DevicePolicyData policy = getUserData(userId);
        Set<String> adminPkgs = null;
        for (int i = policy.mAdminList.size() - 1; i >= 0; i--) {
            String pkgName = policy.mAdminList.get(i).info.getPackageName();
            if (adminPkgs == null) {
                adminPkgs = new ArraySet<>();
            }
            adminPkgs.add(pkgName);
        }
        return adminPkgs;
    }

    private void transferActiveAdminUncheckedLocked(ComponentName incomingReceiver, ComponentName outgoingReceiver, int userHandle) {
        DevicePolicyData policy = getUserData(userHandle);
        if (policy.mAdminMap.containsKey(outgoingReceiver) || !policy.mAdminMap.containsKey(incomingReceiver)) {
            DeviceAdminInfo incomingDeviceInfo = findAdmin(incomingReceiver, userHandle, true);
            ActiveAdmin adminToTransfer = policy.mAdminMap.get(outgoingReceiver);
            int oldAdminUid = adminToTransfer.getUid();
            adminToTransfer.transfer(incomingDeviceInfo);
            policy.mAdminMap.remove(outgoingReceiver);
            policy.mAdminMap.put(incomingReceiver, adminToTransfer);
            if (policy.mPasswordOwner == oldAdminUid) {
                policy.mPasswordOwner = adminToTransfer.getUid();
            }
            saveSettingsLocked(userHandle);
            sendAdminCommandLocked(adminToTransfer, "android.app.action.DEVICE_ADMIN_ENABLED", (Bundle) null, (BroadcastReceiver) null);
        }
    }

    private void checkActiveAdminPrecondition(ComponentName adminReceiver, DeviceAdminInfo info, DevicePolicyData policy) {
        if (info == null) {
            throw new IllegalArgumentException("Bad admin: " + adminReceiver);
        } else if (!info.getActivityInfo().applicationInfo.isInternal()) {
            throw new IllegalArgumentException("Only apps in internal storage can be active admin: " + adminReceiver);
        } else if (info.getActivityInfo().applicationInfo.isInstantApp()) {
            throw new IllegalArgumentException("Instant apps cannot be device admins: " + adminReceiver);
        } else if (policy.mRemovingAdmins.contains(adminReceiver)) {
            throw new IllegalArgumentException("Trying to set an admin which is being removed");
        }
    }

    public boolean isAdminActive(ComponentName adminReceiver, int userHandle) {
        boolean isAdminActive = false;
        if (!this.mHasFeature) {
            Flog.i(305, "isAdminActive(" + adminReceiver + "), by user " + userHandle + ", return false, cause no feature");
            return false;
        }
        enforceFullCrossUsersPermission(userHandle);
        synchronized (getLockObject()) {
            if (getActiveAdminUncheckedLocked(adminReceiver, userHandle) != null) {
                isAdminActive = true;
            }
            Flog.i(305, "isAdminActive(" + adminReceiver + "), by user " + userHandle + ", return " + isAdminActive);
        }
        return isAdminActive;
    }

    public boolean isRemovingAdmin(ComponentName adminReceiver, int userHandle) {
        boolean contains;
        if (!this.mHasFeature) {
            return false;
        }
        enforceFullCrossUsersPermission(userHandle);
        synchronized (getLockObject()) {
            contains = getUserData(userHandle).mRemovingAdmins.contains(adminReceiver);
        }
        return contains;
    }

    public boolean hasGrantedPolicy(ComponentName adminReceiver, int policyId, int userHandle) {
        boolean usesPolicy;
        if (!this.mHasFeature) {
            return false;
        }
        enforceFullCrossUsersPermission(userHandle);
        synchronized (getLockObject()) {
            ActiveAdmin administrator = getActiveAdminUncheckedLocked(adminReceiver, userHandle);
            if (administrator != null) {
                usesPolicy = administrator.info.usesPolicy(policyId);
            } else {
                throw new SecurityException("No active admin " + adminReceiver);
            }
        }
        return usesPolicy;
    }

    public List<ComponentName> getActiveAdmins(int userHandle) {
        if (!this.mHasFeature) {
            return Collections.EMPTY_LIST;
        }
        enforceFullCrossUsersPermission(userHandle);
        synchronized (getLockObject()) {
            DevicePolicyData policy = getUserData(userHandle);
            int N = policy.mAdminList.size();
            if (N <= 0) {
                return null;
            }
            ArrayList<ComponentName> res = new ArrayList<>(N);
            for (int i = 0; i < N; i++) {
                res.add(policy.mAdminList.get(i).info.getComponent());
            }
            return res;
        }
    }

    public boolean packageHasActiveAdmins(String packageName, int userHandle) {
        if (!this.mHasFeature) {
            return false;
        }
        enforceFullCrossUsersPermission(userHandle);
        synchronized (getLockObject()) {
            DevicePolicyData policy = getUserData(userHandle);
            int N = policy.mAdminList.size();
            for (int i = 0; i < N; i++) {
                if (policy.mAdminList.get(i).info.getPackageName().equals(packageName)) {
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
                synchronized (getLockObject()) {
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
            ApplicationInfo ai = this.mInjector.getIPackageManager().getApplicationInfo(packageName, 786432, userHandle);
            if (ai != null) {
                return (ai.flags & 256) != 0;
            }
            throw new IllegalStateException("Couldn't find package: " + packageName + " on user " + userHandle);
        } catch (RemoteException e) {
            throw new IllegalStateException(e);
        }
    }

    private boolean isAdminTestOnlyLocked(ComponentName who, int userHandle) {
        ActiveAdmin admin = getActiveAdminUncheckedLocked(who, userHandle);
        return admin != null && admin.testOnlyAdmin;
    }

    private void enforceShell(String method) {
        int callingUid = this.mInjector.binderGetCallingUid();
        if (callingUid != 2000 && callingUid != 0) {
            throw new SecurityException("Non-shell user attempted to call " + method);
        }
    }

    public void removeActiveAdmin(ComponentName adminReceiver, int userHandle) {
        HwFrameworkFactory.getHwBehaviorCollectManager().sendBehavior(IHwBehaviorCollectManager.BehaviorId.DEVICE_POLICY_REMOVEACTIVEADMIN);
        if (this.mHasFeature) {
            enforceFullCrossUsersPermission(userHandle);
            enforceUserUnlocked(userHandle);
            synchronized (getLockObject()) {
                ActiveAdmin admin = getActiveAdminUncheckedLocked(adminReceiver, userHandle);
                if (admin != null) {
                    if (!isDeviceOwner(adminReceiver, userHandle)) {
                        if (!isProfileOwner(adminReceiver, userHandle)) {
                            if (admin.getUid() != this.mInjector.binderGetCallingUid()) {
                                this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_DEVICE_ADMINS", null);
                            }
                            long ident = this.mInjector.binderClearCallingIdentity();
                            try {
                                if (this.mHwDevicePolicyManagerService != null) {
                                    this.mHwDevicePolicyManagerService.notifyPlugins(adminReceiver, userHandle);
                                }
                                removeActiveAdminLocked(adminReceiver, userHandle);
                                return;
                            } finally {
                                this.mInjector.binderRestoreCallingIdentity(ident);
                            }
                        }
                    }
                    Slog.e(LOG_TAG, "Device/profile owner cannot be removed: component=" + adminReceiver);
                }
            }
        }
    }

    public boolean isSeparateProfileChallengeAllowed(int userHandle) {
        if (isCallerWithSystemUid()) {
            ComponentName profileOwner = getProfileOwner(userHandle);
            return profileOwner != null && getTargetSdk(profileOwner.getPackageName(), userHandle) > 23;
        }
        throw new SecurityException("Caller must be system");
    }

    public void setPasswordQuality(ComponentName who, int quality, boolean parent) {
        if (this.mHasFeature) {
            Preconditions.checkNotNull(who, "ComponentName is null");
            validateQualityConstant(quality);
            int userId = this.mInjector.userHandleGetCallingUserId();
            synchronized (getLockObject()) {
                ActiveAdmin ap = getActiveAdminForCallerLocked(who, 0, parent);
                long ident = this.mInjector.binderClearCallingIdentity();
                try {
                    PasswordMetrics metrics = ap.minimumPasswordMetrics;
                    if (metrics.quality != quality) {
                        metrics.quality = quality;
                        updatePasswordValidityCheckpointLocked(userId, parent);
                        updatePasswordQualityCacheForUserGroup(userId);
                        saveSettingsLocked(userId);
                    }
                    maybeLogPasswordComplexitySet(who, userId, parent, metrics);
                } finally {
                    this.mInjector.binderRestoreCallingIdentity(ident);
                }
            }
            DevicePolicyEventLogger.createEvent(1).setAdmin(who).setInt(quality).setBoolean(parent).write();
        }
    }

    @GuardedBy({"getLockObject()"})
    private void updatePasswordValidityCheckpointLocked(int userHandle, boolean parent) {
        int credentialOwner = getCredentialOwner(userHandle, parent);
        DevicePolicyData policy = getUserData(credentialOwner);
        PasswordMetrics metrics = getUserPasswordMetricsLocked(credentialOwner);
        if (metrics == null) {
            metrics = new PasswordMetrics();
        }
        policy.mPasswordValidAtLastCheckpoint = isPasswordSufficientForUserWithoutCheckpointLocked(metrics, userHandle, parent);
        saveSettingsLocked(credentialOwner);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updatePasswordQualityCacheForUserGroup(int userId) {
        List<UserInfo> users;
        if (userId == -1) {
            users = this.mUserManager.getUsers();
        } else {
            users = this.mUserManager.getProfiles(userId);
        }
        for (UserInfo userInfo : users) {
            int currentUserId = userInfo.id;
            this.mPolicyCache.setPasswordQuality(currentUserId, getPasswordQuality(null, currentUserId, false));
        }
    }

    public int getPasswordQuality(ComponentName who, int userHandle, boolean parent) {
        if (!this.mHasFeature) {
            return 0;
        }
        enforceFullCrossUsersPermission(userHandle);
        synchronized (getLockObject()) {
            int mode = 0;
            if (who != null) {
                ActiveAdmin admin = getActiveAdminUncheckedLocked(who, userHandle, parent);
                return admin != null ? admin.minimumPasswordMetrics.quality : 0;
            }
            List<ActiveAdmin> admins = getActiveAdminsForLockscreenPoliciesLocked(userHandle, parent);
            int N = admins.size();
            for (int i = 0; i < N; i++) {
                ActiveAdmin admin2 = admins.get(i);
                if (mode < admin2.minimumPasswordMetrics.quality) {
                    mode = admin2.minimumPasswordMetrics.quality;
                }
            }
            return mode;
        }
    }

    private List<ActiveAdmin> getActiveAdminsForLockscreenPoliciesLocked(int userHandle, boolean parent) {
        if (!parent && isSeparateProfileChallengeEnabled(userHandle)) {
            return getUserDataUnchecked(userHandle).mAdminList;
        }
        ArrayList<ActiveAdmin> admins = new ArrayList<>();
        for (UserInfo userInfo : this.mUserManager.getProfiles(userHandle)) {
            DevicePolicyData policy = getUserData(userInfo.id);
            if (!userInfo.isManagedProfile()) {
                admins.addAll(policy.mAdminList);
            } else {
                boolean hasSeparateChallenge = isSeparateProfileChallengeEnabled(userInfo.id);
                int N = policy.mAdminList.size();
                for (int i = 0; i < N; i++) {
                    ActiveAdmin admin = policy.mAdminList.get(i);
                    if (admin.hasParentActiveAdmin()) {
                        admins.add(admin.getParentActiveAdmin());
                    }
                    if (!hasSeparateChallenge) {
                        admins.add(admin);
                    }
                }
            }
        }
        return admins;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isSeparateProfileChallengeEnabled(int userHandle) {
        long ident = this.mInjector.binderClearCallingIdentity();
        try {
            return this.mLockPatternUtils.isSeparateProfileChallengeEnabled(userHandle);
        } finally {
            this.mInjector.binderRestoreCallingIdentity(ident);
        }
    }

    public void setPasswordMinimumLength(ComponentName who, int length, boolean parent) {
        if (this.mHasFeature) {
            Preconditions.checkNotNull(who, "ComponentName is null");
            int userId = this.mInjector.userHandleGetCallingUserId();
            synchronized (getLockObject()) {
                PasswordMetrics metrics = getActiveAdminForCallerLocked(who, 0, parent).minimumPasswordMetrics;
                if (metrics.length != length) {
                    metrics.length = length;
                    updatePasswordValidityCheckpointLocked(userId, parent);
                    saveSettingsLocked(userId);
                }
                maybeLogPasswordComplexitySet(who, userId, parent, metrics);
            }
            DevicePolicyEventLogger.createEvent(2).setAdmin(who).setInt(length).write();
        }
    }

    public int getPasswordMinimumLength(ComponentName who, int userHandle, boolean parent) {
        return getStrictestPasswordRequirement(who, userHandle, parent, $$Lambda$DevicePolicyManagerService$NzTaj70nEECGXhr52RbDyXK_fPU.INSTANCE, 0);
    }

    public void setPasswordHistoryLength(ComponentName who, int length, boolean parent) {
        if (this.mHasFeature && this.mLockPatternUtils.hasSecureLockScreen()) {
            Preconditions.checkNotNull(who, "ComponentName is null");
            int userId = this.mInjector.userHandleGetCallingUserId();
            synchronized (getLockObject()) {
                ActiveAdmin ap = getActiveAdminForCallerLocked(who, 0, parent);
                if (ap.passwordHistoryLength != length) {
                    ap.passwordHistoryLength = length;
                    updatePasswordValidityCheckpointLocked(userId, parent);
                    saveSettingsLocked(userId);
                }
            }
            if (SecurityLog.isLoggingEnabled()) {
                SecurityLog.writeEvent(210018, new Object[]{who.getPackageName(), Integer.valueOf(userId), Integer.valueOf(parent ? getProfileParentId(userId) : userId), Integer.valueOf(length)});
                Log.i(MDPP_TAG, "TAG 210018");
            }
        }
    }

    public int getPasswordHistoryLength(ComponentName who, int userHandle, boolean parent) {
        if (!this.mLockPatternUtils.hasSecureLockScreen()) {
            return 0;
        }
        return getStrictestPasswordRequirement(who, userHandle, parent, $$Lambda$DevicePolicyManagerService$kf4uUzLBApkNlieB7zr8MNfAxbg.INSTANCE, 0);
    }

    public void setPasswordExpirationTimeout(ComponentName who, long timeout, boolean parent) {
        if (this.mHasFeature && this.mLockPatternUtils.hasSecureLockScreen()) {
            Preconditions.checkNotNull(who, "ComponentName is null");
            Preconditions.checkArgumentNonnegative(timeout, "Timeout must be >= 0 ms");
            int userHandle = this.mInjector.userHandleGetCallingUserId();
            synchronized (getLockObject()) {
                ActiveAdmin ap = getActiveAdminForCallerLocked(who, 6, parent);
                long expiration = timeout > 0 ? System.currentTimeMillis() + timeout : 0;
                ap.passwordExpirationDate = expiration;
                ap.passwordExpirationTimeout = timeout;
                if (timeout > 0) {
                    Slog.w(LOG_TAG, "setPasswordExpiration(): password will expire on " + DateFormat.getDateTimeInstance(2, 2).format(new Date(expiration)));
                }
                saveSettingsLocked(userHandle);
                setExpirationAlarmCheckLocked(this.mContext, userHandle, parent);
            }
            if (SecurityLog.isLoggingEnabled()) {
                SecurityLog.writeEvent(210016, new Object[]{who.getPackageName(), Integer.valueOf(userHandle), Integer.valueOf(parent ? getProfileParentId(userHandle) : userHandle), Long.valueOf(timeout)});
                Log.i(MDPP_TAG, "TAG 210016");
            }
        }
    }

    public long getPasswordExpirationTimeout(ComponentName who, int userHandle, boolean parent) {
        if (!this.mHasFeature || !this.mLockPatternUtils.hasSecureLockScreen()) {
            return 0;
        }
        enforceFullCrossUsersPermission(userHandle);
        synchronized (getLockObject()) {
            long timeout = 0;
            if (who != null) {
                ActiveAdmin admin = getActiveAdminUncheckedLocked(who, userHandle, parent);
                return admin != null ? admin.passwordExpirationTimeout : 0;
            }
            List<ActiveAdmin> admins = getActiveAdminsForLockscreenPoliciesLocked(userHandle, parent);
            int N = admins.size();
            for (int i = 0; i < N; i++) {
                ActiveAdmin admin2 = admins.get(i);
                if (timeout == 0 || (admin2.passwordExpirationTimeout != 0 && timeout > admin2.passwordExpirationTimeout)) {
                    timeout = admin2.passwordExpirationTimeout;
                }
            }
            return timeout;
        }
    }

    public boolean addCrossProfileWidgetProvider(ComponentName admin, String packageName) {
        int userId = UserHandle.getCallingUserId();
        List<String> changedProviders = null;
        synchronized (getLockObject()) {
            ActiveAdmin activeAdmin = getActiveAdminForCallerLocked(admin, -1);
            if (activeAdmin.crossProfileWidgetProviders == null) {
                activeAdmin.crossProfileWidgetProviders = new ArrayList();
            }
            List<String> providers = activeAdmin.crossProfileWidgetProviders;
            if (!providers.contains(packageName)) {
                providers.add(packageName);
                changedProviders = new ArrayList<>(providers);
                saveSettingsLocked(userId);
            }
        }
        DevicePolicyEventLogger.createEvent(49).setAdmin(admin).write();
        if (changedProviders == null) {
            return false;
        }
        this.mLocalService.notifyCrossProfileProvidersChanged(userId, changedProviders);
        return true;
    }

    public boolean removeCrossProfileWidgetProvider(ComponentName admin, String packageName) {
        int userId = UserHandle.getCallingUserId();
        List<String> changedProviders = null;
        synchronized (getLockObject()) {
            ActiveAdmin activeAdmin = getActiveAdminForCallerLocked(admin, -1);
            if (activeAdmin.crossProfileWidgetProviders != null) {
                if (!activeAdmin.crossProfileWidgetProviders.isEmpty()) {
                    List<String> providers = activeAdmin.crossProfileWidgetProviders;
                    if (providers.remove(packageName)) {
                        changedProviders = new ArrayList<>(providers);
                        saveSettingsLocked(userId);
                    }
                }
            }
            return false;
        }
        DevicePolicyEventLogger.createEvent((int) HdmiCecKeycode.CEC_KEYCODE_F5).setAdmin(admin).write();
        if (changedProviders == null) {
            return false;
        }
        this.mLocalService.notifyCrossProfileProvidersChanged(userId, changedProviders);
        return true;
    }

    public List<String> getCrossProfileWidgetProviders(ComponentName admin) {
        synchronized (getLockObject()) {
            ActiveAdmin activeAdmin = getActiveAdminForCallerLocked(admin, -1);
            if (activeAdmin.crossProfileWidgetProviders != null) {
                if (!activeAdmin.crossProfileWidgetProviders.isEmpty()) {
                    if (this.mInjector.binderIsCallingUidMyUid()) {
                        return new ArrayList(activeAdmin.crossProfileWidgetProviders);
                    }
                    return activeAdmin.crossProfileWidgetProviders;
                }
            }
            return null;
        }
    }

    private long getPasswordExpirationLocked(ComponentName who, int userHandle, boolean parent) {
        long timeout = 0;
        if (who != null) {
            ActiveAdmin admin = getActiveAdminUncheckedLocked(who, userHandle, parent);
            if (admin != null) {
                return admin.passwordExpirationDate;
            }
            return 0;
        }
        List<ActiveAdmin> admins = getActiveAdminsForLockscreenPoliciesLocked(userHandle, parent);
        int N = admins.size();
        for (int i = 0; i < N; i++) {
            ActiveAdmin admin2 = admins.get(i);
            if (timeout == 0 || (admin2.passwordExpirationDate != 0 && timeout > admin2.passwordExpirationDate)) {
                timeout = admin2.passwordExpirationDate;
            }
        }
        return timeout;
    }

    public long getPasswordExpiration(ComponentName who, int userHandle, boolean parent) {
        long passwordExpirationLocked;
        if (!this.mHasFeature || !this.mLockPatternUtils.hasSecureLockScreen()) {
            return 0;
        }
        enforceFullCrossUsersPermission(userHandle);
        synchronized (getLockObject()) {
            passwordExpirationLocked = getPasswordExpirationLocked(who, userHandle, parent);
        }
        return passwordExpirationLocked;
    }

    public void setPasswordMinimumUpperCase(ComponentName who, int length, boolean parent) {
        if (this.mHasFeature) {
            Preconditions.checkNotNull(who, "ComponentName is null");
            int userId = this.mInjector.userHandleGetCallingUserId();
            synchronized (getLockObject()) {
                PasswordMetrics metrics = getActiveAdminForCallerLocked(who, 0, parent).minimumPasswordMetrics;
                if (metrics.upperCase != length) {
                    metrics.upperCase = length;
                    updatePasswordValidityCheckpointLocked(userId, parent);
                    saveSettingsLocked(userId);
                }
                maybeLogPasswordComplexitySet(who, userId, parent, metrics);
            }
            DevicePolicyEventLogger.createEvent(7).setAdmin(who).setInt(length).write();
        }
    }

    public int getPasswordMinimumUpperCase(ComponentName who, int userHandle, boolean parent) {
        return getStrictestPasswordRequirement(who, userHandle, parent, $$Lambda$DevicePolicyManagerService$GdvC4eub6BtkkX5BnHuPR5Ob0ag.INSTANCE, 393216);
    }

    public void setPasswordMinimumLowerCase(ComponentName who, int length, boolean parent) {
        Preconditions.checkNotNull(who, "ComponentName is null");
        int userId = this.mInjector.userHandleGetCallingUserId();
        synchronized (getLockObject()) {
            PasswordMetrics metrics = getActiveAdminForCallerLocked(who, 0, parent).minimumPasswordMetrics;
            if (metrics.lowerCase != length) {
                metrics.lowerCase = length;
                updatePasswordValidityCheckpointLocked(userId, parent);
                saveSettingsLocked(userId);
            }
            maybeLogPasswordComplexitySet(who, userId, parent, metrics);
        }
        DevicePolicyEventLogger.createEvent(6).setAdmin(who).setInt(length).write();
    }

    public int getPasswordMinimumLowerCase(ComponentName who, int userHandle, boolean parent) {
        return getStrictestPasswordRequirement(who, userHandle, parent, $$Lambda$DevicePolicyManagerService$O6O5T5aoG6MmH8aAAGYNwYhbtw8.INSTANCE, 393216);
    }

    public void setPasswordMinimumLetters(ComponentName who, int length, boolean parent) {
        if (this.mHasFeature) {
            Preconditions.checkNotNull(who, "ComponentName is null");
            int userId = this.mInjector.userHandleGetCallingUserId();
            synchronized (getLockObject()) {
                PasswordMetrics metrics = getActiveAdminForCallerLocked(who, 0, parent).minimumPasswordMetrics;
                if (metrics.letters != length) {
                    metrics.letters = length;
                    updatePasswordValidityCheckpointLocked(userId, parent);
                    saveSettingsLocked(userId);
                }
                maybeLogPasswordComplexitySet(who, userId, parent, metrics);
            }
            DevicePolicyEventLogger.createEvent(5).setAdmin(who).setInt(length).write();
        }
    }

    public int getPasswordMinimumLetters(ComponentName who, int userHandle, boolean parent) {
        return getStrictestPasswordRequirement(who, userHandle, parent, $$Lambda$DevicePolicyManagerService$tN28Me5AH2pjgYHvPnMAsCjK_NU.INSTANCE, 393216);
    }

    public void setPasswordMinimumNumeric(ComponentName who, int length, boolean parent) {
        if (this.mHasFeature) {
            Preconditions.checkNotNull(who, "ComponentName is null");
            int userId = this.mInjector.userHandleGetCallingUserId();
            synchronized (getLockObject()) {
                PasswordMetrics metrics = getActiveAdminForCallerLocked(who, 0, parent).minimumPasswordMetrics;
                if (metrics.numeric != length) {
                    metrics.numeric = length;
                    updatePasswordValidityCheckpointLocked(userId, parent);
                    saveSettingsLocked(userId);
                }
                maybeLogPasswordComplexitySet(who, userId, parent, metrics);
            }
            DevicePolicyEventLogger.createEvent(3).setAdmin(who).setInt(length).write();
        }
    }

    public int getPasswordMinimumNumeric(ComponentName who, int userHandle, boolean parent) {
        return getStrictestPasswordRequirement(who, userHandle, parent, $$Lambda$DevicePolicyManagerService$BYd2ftVebU2Ktj6trDFfrGE5TE.INSTANCE, 393216);
    }

    public void setPasswordMinimumSymbols(ComponentName who, int length, boolean parent) {
        if (this.mHasFeature) {
            Preconditions.checkNotNull(who, "ComponentName is null");
            int userId = this.mInjector.userHandleGetCallingUserId();
            synchronized (getLockObject()) {
                ActiveAdmin ap = getActiveAdminForCallerLocked(who, 0, parent);
                PasswordMetrics metrics = ap.minimumPasswordMetrics;
                if (metrics.symbols != length) {
                    ap.minimumPasswordMetrics.symbols = length;
                    updatePasswordValidityCheckpointLocked(userId, parent);
                    saveSettingsLocked(userId);
                }
                maybeLogPasswordComplexitySet(who, userId, parent, metrics);
            }
            DevicePolicyEventLogger.createEvent(8).setAdmin(who).setInt(length).write();
        }
    }

    public int getPasswordMinimumSymbols(ComponentName who, int userHandle, boolean parent) {
        return getStrictestPasswordRequirement(who, userHandle, parent, $$Lambda$DevicePolicyManagerService$CClEWCtZQRadOocoqGh0wiKhG4.INSTANCE, 393216);
    }

    public void setPasswordMinimumNonLetter(ComponentName who, int length, boolean parent) {
        if (this.mHasFeature) {
            Preconditions.checkNotNull(who, "ComponentName is null");
            int userId = this.mInjector.userHandleGetCallingUserId();
            synchronized (getLockObject()) {
                ActiveAdmin ap = getActiveAdminForCallerLocked(who, 0, parent);
                PasswordMetrics metrics = ap.minimumPasswordMetrics;
                if (metrics.nonLetter != length) {
                    ap.minimumPasswordMetrics.nonLetter = length;
                    updatePasswordValidityCheckpointLocked(userId, parent);
                    saveSettingsLocked(userId);
                }
                maybeLogPasswordComplexitySet(who, userId, parent, metrics);
            }
            DevicePolicyEventLogger.createEvent(4).setAdmin(who).setInt(length).write();
        }
    }

    public int getPasswordMinimumNonLetter(ComponentName who, int userHandle, boolean parent) {
        return getStrictestPasswordRequirement(who, userHandle, parent, $$Lambda$DevicePolicyManagerService$8nvbMteplUbtaSMuw4DWJMQa4g.INSTANCE, 393216);
    }

    private int getStrictestPasswordRequirement(ComponentName who, int userHandle, boolean parent, Function<ActiveAdmin, Integer> getter, int minimumPasswordQuality) {
        int i = 0;
        if (!this.mHasFeature) {
            return 0;
        }
        enforceFullCrossUsersPermission(userHandle);
        synchronized (getLockObject()) {
            if (who != null) {
                ActiveAdmin admin = getActiveAdminUncheckedLocked(who, userHandle, parent);
                if (admin != null) {
                    i = getter.apply(admin).intValue();
                }
                return i;
            }
            int maxValue = 0;
            List<ActiveAdmin> admins = getActiveAdminsForLockscreenPoliciesLocked(userHandle, parent);
            int N = admins.size();
            for (int i2 = 0; i2 < N; i2++) {
                ActiveAdmin admin2 = admins.get(i2);
                if (isLimitPasswordAllowed(admin2, minimumPasswordQuality)) {
                    Integer adminValue = getter.apply(admin2);
                    if (adminValue.intValue() > maxValue) {
                        maxValue = adminValue.intValue();
                    }
                }
            }
            return maxValue;
        }
    }

    public boolean isActivePasswordSufficient(int userHandle, boolean parent) {
        boolean isActivePasswordSufficientForUserLocked;
        if (!this.mHasFeature) {
            return true;
        }
        enforceFullCrossUsersPermission(userHandle);
        enforceUserUnlocked(userHandle, parent);
        synchronized (getLockObject()) {
            getActiveAdminForCallerLocked(null, 0, parent);
            int credentialOwner = getCredentialOwner(userHandle, parent);
            DevicePolicyData policy = getUserDataUnchecked(credentialOwner);
            isActivePasswordSufficientForUserLocked = isActivePasswordSufficientForUserLocked(policy.mPasswordValidAtLastCheckpoint, getUserPasswordMetricsLocked(credentialOwner), userHandle, parent);
        }
        return isActivePasswordSufficientForUserLocked;
    }

    public boolean isUsingUnifiedPassword(ComponentName admin) {
        if (!this.mHasFeature) {
            return true;
        }
        int userId = this.mInjector.userHandleGetCallingUserId();
        enforceProfileOrDeviceOwner(admin);
        enforceManagedProfile(userId, "query unified challenge status");
        return true ^ isSeparateProfileChallengeEnabled(userId);
    }

    public boolean isProfileActivePasswordSufficientForParent(int userHandle) {
        boolean isActivePasswordSufficientForUserLocked;
        if (!this.mHasFeature) {
            return true;
        }
        enforceFullCrossUsersPermission(userHandle);
        enforceManagedProfile(userHandle, "call APIs refering to the parent profile");
        synchronized (getLockObject()) {
            int targetUser = getProfileParentId(userHandle);
            enforceUserUnlocked(targetUser, false);
            int credentialOwner = getCredentialOwner(userHandle, false);
            DevicePolicyData policy = getUserDataUnchecked(credentialOwner);
            isActivePasswordSufficientForUserLocked = isActivePasswordSufficientForUserLocked(policy.mPasswordValidAtLastCheckpoint, getUserPasswordMetricsLocked(credentialOwner), targetUser, false);
        }
        return isActivePasswordSufficientForUserLocked;
    }

    private boolean isActivePasswordSufficientForUserLocked(boolean passwordValidAtLastCheckpoint, PasswordMetrics metrics, int userHandle, boolean parent) {
        if (!this.mInjector.storageManagerIsFileBasedEncryptionEnabled() && metrics == null) {
            return passwordValidAtLastCheckpoint;
        }
        if (metrics == null) {
            metrics = new PasswordMetrics();
        }
        return isPasswordSufficientForUserWithoutCheckpointLocked(metrics, userHandle, parent);
    }

    private boolean isPasswordSufficientForUserWithoutCheckpointLocked(PasswordMetrics metrics, int userId, boolean parent) {
        int requiredQuality = getPasswordQuality(null, userId, parent);
        if (requiredQuality >= 131072 && metrics.length < getPasswordMinimumLength(null, userId, parent)) {
            return false;
        }
        if (requiredQuality == 393216) {
            if (metrics.upperCase < getPasswordMinimumUpperCase(null, userId, parent) || metrics.lowerCase < getPasswordMinimumLowerCase(null, userId, parent) || metrics.letters < getPasswordMinimumLetters(null, userId, parent) || metrics.numeric < getPasswordMinimumNumeric(null, userId, parent) || metrics.symbols < getPasswordMinimumSymbols(null, userId, parent) || metrics.nonLetter < getPasswordMinimumNonLetter(null, userId, parent)) {
                return false;
            }
            return true;
        } else if (metrics.quality >= requiredQuality) {
            return true;
        } else {
            return false;
        }
    }

    public int getPasswordComplexity() {
        int i;
        DevicePolicyEventLogger.createEvent(72).setStrings(this.mInjector.getPackageManager().getPackagesForUid(this.mInjector.binderGetCallingUid())).write();
        int callingUserId = this.mInjector.userHandleGetCallingUserId();
        enforceUserUnlocked(callingUserId);
        this.mContext.enforceCallingOrSelfPermission("android.permission.REQUEST_PASSWORD_COMPLEXITY", "Must have android.permission.REQUEST_PASSWORD_COMPLEXITY permission.");
        synchronized (getLockObject()) {
            i = 0;
            PasswordMetrics metrics = getUserPasswordMetricsLocked(getCredentialOwner(callingUserId, false));
            if (metrics != null) {
                i = metrics.determineComplexity();
            }
        }
        return i;
    }

    public int getCurrentFailedPasswordAttempts(int userHandle, boolean parent) {
        int i;
        if (!this.mLockPatternUtils.hasSecureLockScreen()) {
            return 0;
        }
        enforceFullCrossUsersPermission(userHandle);
        synchronized (getLockObject()) {
            if (!isCallerWithSystemUid() && this.mContext.checkCallingPermission("android.permission.ACCESS_KEYGUARD_SECURE_STORAGE") != 0) {
                getActiveAdminForCallerLocked(null, 1, parent);
            }
            i = getUserDataUnchecked(getCredentialOwner(userHandle, parent)).mFailedPasswordAttempts;
        }
        return i;
    }

    public void setMaximumFailedPasswordsForWipe(ComponentName who, int num, boolean parent) {
        if (this.mHasFeature && this.mLockPatternUtils.hasSecureLockScreen()) {
            Preconditions.checkNotNull(who, "ComponentName is null");
            int userId = this.mInjector.userHandleGetCallingUserId();
            synchronized (getLockObject()) {
                getActiveAdminForCallerLocked(who, 4, parent);
                ActiveAdmin ap = getActiveAdminForCallerLocked(who, 1, parent);
                if (ap.maximumFailedPasswordsForWipe != num) {
                    ap.maximumFailedPasswordsForWipe = num;
                    saveSettingsLocked(userId);
                }
            }
            if (SecurityLog.isLoggingEnabled()) {
                SecurityLog.writeEvent(210020, new Object[]{who.getPackageName(), Integer.valueOf(userId), Integer.valueOf(parent ? getProfileParentId(userId) : userId), Integer.valueOf(num)});
            }
        }
    }

    public int getMaximumFailedPasswordsForWipe(ComponentName who, int userHandle, boolean parent) {
        ActiveAdmin admin;
        int i = 0;
        if (!this.mHasFeature || !this.mLockPatternUtils.hasSecureLockScreen()) {
            return 0;
        }
        enforceFullCrossUsersPermission(userHandle);
        synchronized (getLockObject()) {
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
        if (!this.mHasFeature || !this.mLockPatternUtils.hasSecureLockScreen()) {
            return -10000;
        }
        enforceFullCrossUsersPermission(userHandle);
        synchronized (getLockObject()) {
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
            ActiveAdmin admin = admins.get(i);
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
            return this.mUserManager.getUserInfo(userId);
        } finally {
            this.mInjector.binderRestoreCallingIdentity(token);
        }
    }

    private boolean canPOorDOCallResetPassword(ActiveAdmin admin, int userId) {
        return getTargetSdk(admin.info.getPackageName(), userId) < 26;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean canUserHaveUntrustedCredentialReset(int userId) {
        synchronized (getLockObject()) {
            Iterator<ActiveAdmin> it = getUserData(userId).mAdminList.iterator();
            while (it.hasNext()) {
                ActiveAdmin admin = it.next();
                if (isActiveAdminWithPolicyForUserLocked(admin, -1, userId)) {
                    if (canPOorDOCallResetPassword(admin, userId)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    public boolean resetPassword(String passwordOrNull, int flags) throws RemoteException {
        boolean preN;
        HwFrameworkFactory.getHwBehaviorCollectManager().sendBehavior(IHwBehaviorCollectManager.BehaviorId.DEVICE_POLICY_RESETPASSWORD);
        if (!this.mLockPatternUtils.hasSecureLockScreen()) {
            Slog.w(LOG_TAG, "Cannot reset password when the device has no lock screen");
            return false;
        }
        int callingUid = this.mInjector.binderGetCallingUid();
        int userHandle = this.mInjector.userHandleGetCallingUserId();
        String password = passwordOrNull != null ? passwordOrNull : "";
        if (TextUtils.isEmpty(password)) {
            enforceNotManagedProfile(userHandle, "clear the active password");
        }
        synchronized (getLockObject()) {
            ActiveAdmin admin = getActiveAdminWithPolicyForUidLocked(null, -1, callingUid);
            boolean z = true;
            if (admin == null) {
                ActiveAdmin admin2 = getActiveAdminOrCheckPermissionForCallerLocked(null, 2, "android.permission.RESET_PASSWORD");
                if (admin2 == null || getTargetSdk(admin2.info.getPackageName(), userHandle) > 23) {
                    z = false;
                }
                preN = z;
                if (TextUtils.isEmpty(password)) {
                    if (preN) {
                        Slog.e(LOG_TAG, "Cannot call with null password");
                        return false;
                    }
                    throw new SecurityException("Cannot call with null password");
                } else if (isLockScreenSecureUnchecked(userHandle)) {
                    if (preN) {
                        Slog.e(LOG_TAG, "Cannot change current password");
                        return false;
                    }
                    throw new SecurityException("Cannot change current password");
                }
            } else if (canPOorDOCallResetPassword(admin, userHandle)) {
                if (getTargetSdk(admin.info.getPackageName(), userHandle) > 23) {
                    z = false;
                }
                preN = z;
            } else {
                throw new SecurityException("resetPassword() is deprecated for DPC targeting O or later");
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
            }
            if (preN) {
                Slog.e(LOG_TAG, "Cannot reset password when user is locked");
                return false;
            }
            throw new IllegalStateException("Cannot reset password when user is locked");
        }
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:68:0x01c7 */
    /* JADX WARN: Type inference failed for: r14v0 */
    /* JADX WARN: Type inference failed for: r14v4 */
    /* JADX WARN: Type inference failed for: r14v8 */
    /* JADX WARNING: Unknown variable types count: 1 */
    private boolean resetPasswordInternal(String password, long tokenHandle, byte[] token, int flags, int callingUid, int userHandle) {
        int quality;
        long ident;
        boolean result;
        boolean requireEntry;
        long ident2;
        Throwable th;
        synchronized (getLockObject()) {
            int quality2 = getPasswordQuality(null, userHandle, false);
            if (quality2 == 524288) {
                quality2 = 0;
            }
            PasswordMetrics metrics = PasswordMetrics.computeForPassword(password.getBytes());
            int realQuality = metrics.quality;
            if (realQuality >= quality2 || quality2 == 393216) {
                quality = Math.max(realQuality, quality2);
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
            } else {
                Slog.w(LOG_TAG, "resetPassword: password quality 0x" + Integer.toHexString(realQuality) + " does not meet required quality 0x" + Integer.toHexString(quality2));
                return false;
            }
        }
        DevicePolicyData policy = getUserData(userHandle);
        if (policy.mPasswordOwner < 0 || policy.mPasswordOwner == callingUid) {
            ?? r14 = 0;
            boolean callerIsDeviceOwnerAdmin = isCallerDeviceOwner(callingUid);
            boolean doNotAskCredentialsOnBoot = (flags & 2) != 0;
            if (callerIsDeviceOwnerAdmin && doNotAskCredentialsOnBoot) {
                setDoNotAskCredentialsOnBoot();
            }
            long ident3 = this.mInjector.binderClearCallingIdentity();
            if (token == null) {
                try {
                    if (!TextUtils.isEmpty(password)) {
                        r14 = ident3;
                        this.mLockPatternUtils.saveLockPassword(password.getBytes(), (byte[]) null, quality, userHandle, true);
                        ident = r14;
                    } else {
                        ident = ident3;
                        this.mLockPatternUtils.clearLock((byte[]) null, userHandle, true);
                    }
                    result = true;
                    requireEntry = true;
                } catch (Throwable th2) {
                    th = th2;
                    ident2 = r14;
                    this.mInjector.binderRestoreCallingIdentity(ident2 == 1 ? 1 : 0);
                    throw th;
                }
            } else {
                ident = ident3;
                if (!TextUtils.isEmpty(password)) {
                    requireEntry = true;
                    result = this.mLockPatternUtils.setLockCredentialWithToken(password.getBytes(), 2, quality, tokenHandle, token, userHandle);
                } else {
                    requireEntry = true;
                    result = this.mLockPatternUtils.setLockCredentialWithToken((byte[]) null, -1, quality, tokenHandle, token, userHandle);
                }
            }
            if ((flags & 1) == 0) {
                requireEntry = false;
            }
            int newOwner = -1;
            if (requireEntry) {
                this.mLockPatternUtils.requireStrongAuth(2, -1);
            }
            synchronized (getLockObject()) {
                if (requireEntry) {
                    newOwner = callingUid;
                }
                if (policy.mPasswordOwner != newOwner) {
                    policy.mPasswordOwner = newOwner;
                    saveSettingsLocked(userHandle);
                }
            }
            this.mInjector.binderRestoreCallingIdentity(ident);
            return result;
        }
        Slog.w(LOG_TAG, "resetPassword: already set by another uid and not entered by user");
        return false;
    }

    private boolean isLockScreenSecureUnchecked(int userId) {
        long ident = this.mInjector.binderClearCallingIdentity();
        try {
            return this.mLockPatternUtils.isSecure(userId);
        } finally {
            this.mInjector.binderRestoreCallingIdentity(ident);
        }
    }

    private void setDoNotAskCredentialsOnBoot() {
        synchronized (getLockObject()) {
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
        synchronized (getLockObject()) {
            z = getUserData(0).doNotAskCredentialsOnBoot;
        }
        return z;
    }

    public void setMaximumTimeToLock(ComponentName who, long timeMs, boolean parent) {
        if (this.mHasFeature) {
            Preconditions.checkNotNull(who, "ComponentName is null");
            int userHandle = this.mInjector.userHandleGetCallingUserId();
            synchronized (getLockObject()) {
                ActiveAdmin ap = getActiveAdminForCallerLocked(who, 3, parent);
                if (ap.maximumTimeToUnlock != timeMs) {
                    ap.maximumTimeToUnlock = timeMs;
                    saveSettingsLocked(userHandle);
                    updateMaximumTimeToLockLocked(userHandle);
                }
            }
            if (SecurityLog.isLoggingEnabled()) {
                SecurityLog.writeEvent(210019, new Object[]{who.getPackageName(), Integer.valueOf(userHandle), Integer.valueOf(parent ? getProfileParentId(userHandle) : userHandle), Long.valueOf(timeMs)});
                Log.i(MDPP_TAG, "TAG 210019");
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateMaximumTimeToLockLocked(int userId) {
        if (isManagedProfile(userId)) {
            updateProfileLockTimeoutLocked(userId);
        }
        long ident = this.mInjector.binderClearCallingIdentity();
        try {
            int parentId = getProfileParentId(userId);
            long timeMs = getMaximumTimeToLockPolicyFromAdmins(getActiveAdminsForLockscreenPoliciesLocked(parentId, false));
            DevicePolicyData policy = getUserDataUnchecked(parentId);
            if (policy.mLastMaximumTimeToLock != timeMs) {
                policy.mLastMaximumTimeToLock = timeMs;
                if (policy.mLastMaximumTimeToLock != JobStatus.NO_LATEST_RUNTIME) {
                    this.mInjector.settingsGlobalPutInt("stay_on_while_plugged_in", 0);
                }
                getPowerManagerInternal().setMaximumScreenOffTimeoutFromDeviceAdmin(0, timeMs);
                this.mInjector.binderRestoreCallingIdentity(ident);
            }
        } finally {
            this.mInjector.binderRestoreCallingIdentity(ident);
        }
    }

    private void updateProfileLockTimeoutLocked(int userId) {
        long timeMs;
        if (isSeparateProfileChallengeEnabled(userId)) {
            timeMs = getMaximumTimeToLockPolicyFromAdmins(getActiveAdminsForLockscreenPoliciesLocked(userId, false));
        } else {
            timeMs = JobStatus.NO_LATEST_RUNTIME;
        }
        DevicePolicyData policy = getUserDataUnchecked(userId);
        if (policy.mLastMaximumTimeToLock != timeMs) {
            policy.mLastMaximumTimeToLock = timeMs;
            long ident = this.mInjector.binderClearCallingIdentity();
            try {
                getPowerManagerInternal().setMaximumScreenOffTimeoutFromDeviceAdmin(userId, policy.mLastMaximumTimeToLock);
            } finally {
                this.mInjector.binderRestoreCallingIdentity(ident);
            }
        }
    }

    public long getMaximumTimeToLock(ComponentName who, int userHandle, boolean parent) {
        long j = 0;
        if (!this.mHasFeature) {
            return 0;
        }
        enforceFullCrossUsersPermission(userHandle);
        synchronized (getLockObject()) {
            if (who != null) {
                ActiveAdmin admin = getActiveAdminUncheckedLocked(who, userHandle, parent);
                if (admin != null) {
                    j = admin.maximumTimeToUnlock;
                }
                return j;
            }
            long timeMs = getMaximumTimeToLockPolicyFromAdmins(getActiveAdminsForLockscreenPoliciesLocked(userHandle, parent));
            if (timeMs != JobStatus.NO_LATEST_RUNTIME) {
                j = timeMs;
            }
            return j;
        }
    }

    private long getMaximumTimeToLockPolicyFromAdmins(List<ActiveAdmin> admins) {
        long time = JobStatus.NO_LATEST_RUNTIME;
        for (ActiveAdmin admin : admins) {
            if (admin.maximumTimeToUnlock > 0 && admin.maximumTimeToUnlock < time) {
                time = admin.maximumTimeToUnlock;
            }
        }
        return time;
    }

    public void setRequiredStrongAuthTimeout(ComponentName who, long timeoutMs, boolean parent) {
        long timeoutMs2;
        if (this.mHasFeature && this.mLockPatternUtils.hasSecureLockScreen()) {
            Preconditions.checkNotNull(who, "ComponentName is null");
            Preconditions.checkArgument(timeoutMs >= 0, "Timeout must not be a negative number.");
            long minimumStrongAuthTimeout = getMinimumStrongAuthTimeoutMs();
            if (timeoutMs != 0 && timeoutMs < minimumStrongAuthTimeout) {
                timeoutMs = minimumStrongAuthTimeout;
            }
            if (timeoutMs > 259200000) {
                timeoutMs2 = 259200000;
            } else {
                timeoutMs2 = timeoutMs;
            }
            int userHandle = this.mInjector.userHandleGetCallingUserId();
            synchronized (getLockObject()) {
                ActiveAdmin ap = getActiveAdminForCallerLocked(who, -1, parent);
                if (ap.strongAuthUnlockTimeout != timeoutMs2) {
                    ap.strongAuthUnlockTimeout = timeoutMs2;
                    saveSettingsLocked(userHandle);
                }
            }
        }
    }

    public long getRequiredStrongAuthTimeout(ComponentName who, int userId, boolean parent) {
        if (!this.mHasFeature) {
            return 259200000;
        }
        long j = 0;
        if (!this.mLockPatternUtils.hasSecureLockScreen()) {
            return 0;
        }
        enforceFullCrossUsersPermission(userId);
        synchronized (getLockObject()) {
            if (who != null) {
                ActiveAdmin admin = getActiveAdminUncheckedLocked(who, userId, parent);
                if (admin != null) {
                    j = admin.strongAuthUnlockTimeout;
                }
                return j;
            }
            List<ActiveAdmin> admins = getActiveAdminsForLockscreenPoliciesLocked(userId, parent);
            long strongAuthUnlockTimeout = 259200000;
            for (int i = 0; i < admins.size(); i++) {
                long timeout = admins.get(i).strongAuthUnlockTimeout;
                if (timeout != 0) {
                    strongAuthUnlockTimeout = Math.min(timeout, strongAuthUnlockTimeout);
                }
            }
            return Math.max(strongAuthUnlockTimeout, getMinimumStrongAuthTimeoutMs());
        }
    }

    private long getMinimumStrongAuthTimeoutMs() {
        if (!this.mInjector.isBuildDebuggable()) {
            return MINIMUM_STRONG_AUTH_TIMEOUT_MS;
        }
        return Math.min(this.mInjector.systemPropertiesGetLong("persist.sys.min_str_auth_timeo", MINIMUM_STRONG_AUTH_TIMEOUT_MS), MINIMUM_STRONG_AUTH_TIMEOUT_MS);
    }

    /* JADX WARNING: Removed duplicated region for block: B:37:0x0082 A[Catch:{ RemoteException -> 0x00d6, all -> 0x00d4 }] */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x0096 A[Catch:{ RemoteException -> 0x00d6, all -> 0x00d4 }] */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x00a9 A[Catch:{ RemoteException -> 0x00d6, all -> 0x00d4 }] */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x00ae A[Catch:{ RemoteException -> 0x00d6, all -> 0x00d4 }] */
    public void lockNow(int flags, boolean parent) {
        Injector injector;
        ComponentName componentName;
        int userToLock;
        if (this.mHasFeature) {
            int callingUserId = this.mInjector.userHandleGetCallingUserId();
            ComponentName adminComponent = null;
            synchronized (getLockObject()) {
                ActiveAdmin admin = getActiveAdminOrCheckPermissionForCallerLocked(null, 3, parent, "android.permission.LOCK_DEVICE");
                long ident = this.mInjector.binderClearCallingIdentity();
                if (admin == null) {
                    componentName = null;
                } else {
                    try {
                        componentName = admin.info.getComponent();
                    } catch (RemoteException e) {
                        Slog.e(LOG_TAG, "cannot lock Now.");
                        injector = this.mInjector;
                    } catch (Throwable th) {
                        this.mInjector.binderRestoreCallingIdentity(ident);
                        throw th;
                    }
                }
                adminComponent = componentName;
                if (!(adminComponent == null || (flags & 1) == 0)) {
                    enforceManagedProfile(callingUserId, "set FLAG_EVICT_CREDENTIAL_ENCRYPTION_KEY");
                    if (!isProfileOwner(adminComponent, callingUserId)) {
                        throw new SecurityException("Only profile owner admins can set FLAG_EVICT_CREDENTIAL_ENCRYPTION_KEY");
                    } else if (parent) {
                        throw new IllegalArgumentException("Cannot set FLAG_EVICT_CREDENTIAL_ENCRYPTION_KEY for the parent");
                    } else if (this.mInjector.storageManagerIsFileBasedEncryptionEnabled()) {
                        this.mUserManager.evictCredentialEncryptionKey(callingUserId);
                    } else {
                        throw new UnsupportedOperationException("FLAG_EVICT_CREDENTIAL_ENCRYPTION_KEY only applies to FBE devices");
                    }
                }
                if (!parent) {
                    if (isSeparateProfileChallengeEnabled(callingUserId)) {
                        userToLock = callingUserId;
                        this.mLockPatternUtils.requireStrongAuth(2, userToLock);
                        if (userToLock != -1) {
                            this.mInjector.powerManagerGoToSleep(SystemClock.uptimeMillis(), 1, 0);
                            this.mInjector.getIWindowManager().lockNow((Bundle) null);
                        } else {
                            this.mInjector.getTrustManager().setDeviceLockedForUser(userToLock, true);
                        }
                        if (SecurityLog.isLoggingEnabled() && adminComponent != null) {
                            SecurityLog.writeEvent(210022, new Object[]{adminComponent.getPackageName(), Integer.valueOf(callingUserId), Integer.valueOf(!parent ? getProfileParentId(callingUserId) : callingUserId)});
                            Log.i(MDPP_TAG, "TAG 210022");
                        }
                        injector = this.mInjector;
                        injector.binderRestoreCallingIdentity(ident);
                    }
                }
                userToLock = -1;
                this.mLockPatternUtils.requireStrongAuth(2, userToLock);
                if (userToLock != -1) {
                }
                SecurityLog.writeEvent(210022, new Object[]{adminComponent.getPackageName(), Integer.valueOf(callingUserId), Integer.valueOf(!parent ? getProfileParentId(callingUserId) : callingUserId)});
                Log.i(MDPP_TAG, "TAG 210022");
                injector = this.mInjector;
                injector.binderRestoreCallingIdentity(ident);
            }
            DevicePolicyEventLogger.createEvent(10).setAdmin(adminComponent).setInt(flags).write();
        }
    }

    public void enforceCanManageCaCerts(ComponentName who, String callerPackage) {
        if (who != null) {
            enforceProfileOrDeviceOwner(who);
        } else if (!isCallerDelegate(callerPackage, this.mInjector.binderGetCallingUid(), "delegation-cert-install")) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_CA_CERTIFICATES", null);
        }
    }

    private void enforceDeviceOwner(ComponentName who) {
        synchronized (getLockObject()) {
            getActiveAdminForCallerLocked(who, -2);
        }
    }

    private void enforceProfileOrDeviceOwner(ComponentName who) {
        synchronized (getLockObject()) {
            getActiveAdminForCallerLocked(who, -1);
        }
    }

    public boolean approveCaCert(String alias, int userId, boolean approval) {
        enforceManageUsers();
        synchronized (getLockObject()) {
            Set<String> certs = getUserData(userId).mAcceptedCaCertificates;
            if (!(approval ? certs.add(alias) : certs.remove(alias))) {
                return false;
            }
            saveSettingsLocked(userId);
            this.mCertificateMonitor.onCertificateApprovalsChanged(userId);
            return true;
        }
    }

    public boolean isCaCertApproved(String alias, int userId) {
        boolean contains;
        enforceManageUsers();
        synchronized (getLockObject()) {
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
                synchronized (getLockObject()) {
                    getUserData(userInfo.id).mAcceptedCaCertificates.clear();
                    saveSettingsLocked(userInfo.id);
                }
                this.mCertificateMonitor.onCertificateApprovalsChanged(userId);
            }
        }
    }

    public boolean installCaCert(ComponentName admin, String callerPackage, byte[] certBuffer) throws RemoteException {
        HwFrameworkFactory.getHwBehaviorCollectManager().sendBehavior(IHwBehaviorCollectManager.BehaviorId.DEVICE_POLICY_INSTALLCACERT);
        if (!this.mHasFeature) {
            return false;
        }
        enforceCanManageCaCerts(admin, callerPackage);
        UserHandle userHandle = this.mInjector.binderGetCallingUserHandle();
        long id = this.mInjector.binderClearCallingIdentity();
        try {
            String alias = this.mCertificateMonitor.installCaCert(userHandle, certBuffer);
            DevicePolicyEventLogger.createEvent(21).setAdmin(callerPackage).setBoolean(admin == null).write();
            if (alias == null) {
                Log.w(LOG_TAG, "Problem installing cert");
                return false;
            }
            this.mInjector.binderRestoreCallingIdentity(id);
            synchronized (getLockObject()) {
                getUserData(userHandle.getIdentifier()).mOwnerInstalledCaCerts.add(alias);
                saveSettingsLocked(userHandle.getIdentifier());
            }
            return true;
        } finally {
            this.mInjector.binderRestoreCallingIdentity(id);
        }
    }

    /* JADX INFO: finally extract failed */
    public void uninstallCaCerts(ComponentName admin, String callerPackage, String[] aliases) {
        HwFrameworkFactory.getHwBehaviorCollectManager().sendBehavior(IHwBehaviorCollectManager.BehaviorId.DEVICE_POLICY_UNINSTALLCACERTS);
        if (this.mHasFeature) {
            enforceCanManageCaCerts(admin, callerPackage);
            int userId = this.mInjector.userHandleGetCallingUserId();
            long id = this.mInjector.binderClearCallingIdentity();
            try {
                this.mCertificateMonitor.uninstallCaCerts(UserHandle.of(userId), aliases);
                DevicePolicyEventLogger.createEvent(24).setAdmin(callerPackage).setBoolean(admin == null).write();
                this.mInjector.binderRestoreCallingIdentity(id);
                synchronized (getLockObject()) {
                    if (getUserData(userId).mOwnerInstalledCaCerts.removeAll(Arrays.asList(aliases))) {
                        saveSettingsLocked(userId);
                    }
                }
            } catch (Throwable th) {
                this.mInjector.binderRestoreCallingIdentity(id);
                throw th;
            }
        }
    }

    public boolean installKeyPair(ComponentName who, String callerPackage, byte[] privKey, byte[] cert, byte[] chain, String alias, boolean requestAccess, boolean isUserSelectable) {
        IKeyChainService keyChain;
        InterruptedException e;
        Throwable th;
        IKeyChainService keyChain2;
        IKeyChainService keyChain3;
        enforceCanManageScope(who, callerPackage, -1, "delegation-cert-install");
        int callingUid = this.mInjector.binderGetCallingUid();
        long id = this.mInjector.binderClearCallingIdentity();
        try {
            KeyChain.KeyChainConnection keyChainConnection = KeyChain.bindAsUser(this.mContext, UserHandle.getUserHandleForUid(callingUid));
            try {
                keyChain3 = keyChainConnection.getService();
            } catch (RemoteException e2) {
                keyChain2 = e2;
                try {
                    Log.e(LOG_TAG, "Installing certificate", keyChain2);
                    keyChainConnection.close();
                    this.mInjector.binderRestoreCallingIdentity(id);
                    return false;
                } catch (Throwable th2) {
                    th = th2;
                    keyChainConnection.close();
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                keyChainConnection.close();
                throw th;
            }
            try {
                if (!keyChain3.installKeyPair(privKey, cert, chain, alias)) {
                    try {
                        keyChainConnection.close();
                        this.mInjector.binderRestoreCallingIdentity(id);
                        return false;
                    } catch (InterruptedException e3) {
                        e = e3;
                        try {
                            Log.w(LOG_TAG, "Interrupted while installing certificate", e);
                            Thread.currentThread().interrupt();
                            this.mInjector.binderRestoreCallingIdentity(id);
                            return false;
                        } catch (Throwable th4) {
                            keyChain = th4;
                            this.mInjector.binderRestoreCallingIdentity(id);
                            throw keyChain;
                        }
                    } catch (Throwable th5) {
                        keyChain = th5;
                        this.mInjector.binderRestoreCallingIdentity(id);
                        throw keyChain;
                    }
                } else {
                    if (requestAccess) {
                        keyChain3.setGrant(callingUid, alias, true);
                    }
                    try {
                        keyChain3.setUserSelectable(alias, isUserSelectable);
                        DevicePolicyEventLogger.createEvent(20).setAdmin(callerPackage).setBoolean(who == null).write();
                    } catch (RemoteException e4) {
                        keyChain2 = e4;
                        Log.e(LOG_TAG, "Installing certificate", keyChain2);
                        keyChainConnection.close();
                        this.mInjector.binderRestoreCallingIdentity(id);
                        return false;
                    }
                    try {
                        keyChainConnection.close();
                        this.mInjector.binderRestoreCallingIdentity(id);
                        return true;
                    } catch (InterruptedException e5) {
                        e = e5;
                        Log.w(LOG_TAG, "Interrupted while installing certificate", e);
                        Thread.currentThread().interrupt();
                        this.mInjector.binderRestoreCallingIdentity(id);
                        return false;
                    }
                }
            } catch (RemoteException e6) {
                keyChain2 = e6;
                Log.e(LOG_TAG, "Installing certificate", keyChain2);
                keyChainConnection.close();
                this.mInjector.binderRestoreCallingIdentity(id);
                return false;
            } catch (Throwable th6) {
                th = th6;
                keyChainConnection.close();
                throw th;
            }
        } catch (InterruptedException e7) {
            e = e7;
            Log.w(LOG_TAG, "Interrupted while installing certificate", e);
            Thread.currentThread().interrupt();
            this.mInjector.binderRestoreCallingIdentity(id);
            return false;
        } catch (Throwable th7) {
            keyChain = th7;
            this.mInjector.binderRestoreCallingIdentity(id);
            throw keyChain;
        }
    }

    public boolean removeKeyPair(ComponentName who, String callerPackage, String alias) {
        enforceCanManageScope(who, callerPackage, -1, "delegation-cert-install");
        UserHandle userHandle = new UserHandle(UserHandle.getCallingUserId());
        long id = Binder.clearCallingIdentity();
        try {
            KeyChain.KeyChainConnection keyChainConnection = KeyChain.bindAsUser(this.mContext, userHandle);
            try {
                boolean result = keyChainConnection.getService().removeKeyPair(alias);
                DevicePolicyEventLogger.createEvent(23).setAdmin(callerPackage).setBoolean(who == null).write();
                keyChainConnection.close();
                Binder.restoreCallingIdentity(id);
                return result;
            } catch (RemoteException e) {
                Log.e(LOG_TAG, "Removing keypair", e);
                keyChainConnection.close();
            } catch (Throwable keyChain) {
                keyChainConnection.close();
                throw keyChain;
            }
        } catch (InterruptedException e2) {
            Log.w(LOG_TAG, "Interrupted while removing keypair", e2);
            Thread.currentThread().interrupt();
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(id);
            throw th;
        }
        Binder.restoreCallingIdentity(id);
        return false;
    }

    @VisibleForTesting
    public void enforceCallerCanRequestDeviceIdAttestation(ComponentName who, String callerPackage, int callerUid) throws SecurityException {
        int userId = UserHandle.getUserId(callerUid);
        if (hasProfileOwner(userId)) {
            enforceCanManageScope(who, callerPackage, -1, "delegation-cert-install");
            if (!canProfileOwnerAccessDeviceIds(userId)) {
                throw new SecurityException("Profile Owner is not allowed to access Device IDs.");
            }
            return;
        }
        enforceCanManageScope(who, callerPackage, -2, "delegation-cert-install");
    }

    @VisibleForTesting
    public static int[] translateIdAttestationFlags(int idAttestationFlags) {
        Map<Integer, Integer> idTypeToAttestationFlag = new HashMap<>();
        idTypeToAttestationFlag.put(2, 1);
        idTypeToAttestationFlag.put(4, 2);
        idTypeToAttestationFlag.put(8, 3);
        int numFlagsSet = Integer.bitCount(idAttestationFlags);
        if (numFlagsSet == 0) {
            return null;
        }
        if ((idAttestationFlags & 1) != 0) {
            numFlagsSet--;
            idAttestationFlags &= -2;
        }
        int[] attestationUtilsFlags = new int[numFlagsSet];
        int i = 0;
        for (Integer idType : idTypeToAttestationFlag.keySet()) {
            if ((idType.intValue() & idAttestationFlags) != 0) {
                attestationUtilsFlags[i] = idTypeToAttestationFlag.get(idType).intValue();
                i++;
            }
        }
        return attestationUtilsFlags;
    }

    public boolean generateKeyPair(ComponentName who, String callerPackage, String algorithm, ParcelableKeyGenParameterSpec parcelableKeySpec, int idAttestationFlags, KeymasterCertificateChain attestationChain) {
        Throwable th;
        RemoteException e;
        InterruptedException e2;
        Throwable th2;
        Throwable th3;
        int[] attestationUtilsFlags = translateIdAttestationFlags(idAttestationFlags);
        boolean deviceIdAttestationRequired = attestationUtilsFlags != null;
        int callingUid = this.mInjector.binderGetCallingUid();
        if (!deviceIdAttestationRequired || attestationUtilsFlags.length <= 0) {
            enforceCanManageScope(who, callerPackage, -1, "delegation-cert-install");
        } else {
            enforceCallerCanRequestDeviceIdAttestation(who, callerPackage, callingUid);
        }
        KeyGenParameterSpec keySpec = parcelableKeySpec.getSpec();
        String alias = keySpec.getKeystoreAlias();
        if (TextUtils.isEmpty(alias)) {
            throw new IllegalArgumentException("Empty alias provided.");
        } else if (keySpec.getUid() != -1) {
            Log.e(LOG_TAG, "Only the caller can be granted access to the generated keypair.");
            return false;
        } else if (!deviceIdAttestationRequired || keySpec.getAttestationChallenge() != null) {
            UserHandle userHandle = this.mInjector.binderGetCallingUserHandle();
            long id = this.mInjector.binderClearCallingIdentity();
            try {
                KeyChain.KeyChainConnection keyChainConnection = KeyChain.bindAsUser(this.mContext, userHandle);
                try {
                    IKeyChainService keyChain = keyChainConnection.getService();
                    int generationResult = keyChain.generateKeyPair(algorithm, new ParcelableKeyGenParameterSpec(new KeyGenParameterSpec.Builder(keySpec).setAttestationChallenge(null).build()));
                    if (generationResult != 0) {
                        try {
                            Log.e(LOG_TAG, String.format("KeyChain failed to generate a keypair, error %d.", Integer.valueOf(generationResult)));
                            if (generationResult != 6) {
                                try {
                                    $closeResource(null, keyChainConnection);
                                    this.mInjector.binderRestoreCallingIdentity(id);
                                    return false;
                                } catch (RemoteException e3) {
                                    e = e3;
                                    Log.e(LOG_TAG, "KeyChain error while generating a keypair", e);
                                    this.mInjector.binderRestoreCallingIdentity(id);
                                    return false;
                                } catch (InterruptedException e4) {
                                    e2 = e4;
                                    Log.w(LOG_TAG, "Interrupted while generating keypair", e2);
                                    Thread.currentThread().interrupt();
                                    this.mInjector.binderRestoreCallingIdentity(id);
                                    return false;
                                } catch (Throwable th4) {
                                    th = th4;
                                    this.mInjector.binderRestoreCallingIdentity(id);
                                    throw th;
                                }
                            } else {
                                throw new ServiceSpecificException(1, String.format("KeyChain error: %d", Integer.valueOf(generationResult)));
                            }
                        } catch (Throwable th5) {
                            th3 = th5;
                            th2 = th3;
                            try {
                                throw th2;
                            } catch (Throwable th6) {
                                if (keyChainConnection != null) {
                                    $closeResource(th2, keyChainConnection);
                                }
                                throw th6;
                            }
                        }
                    } else {
                        keyChain.setGrant(callingUid, alias, true);
                        byte[] attestationChallenge = keySpec.getAttestationChallenge();
                        if (attestationChallenge != null) {
                            try {
                                int attestationResult = keyChain.attestKey(alias, attestationChallenge, attestationUtilsFlags, attestationChain);
                                if (attestationResult != 0) {
                                    Log.e(LOG_TAG, String.format("Attestation for %s failed (rc=%d), deleting key.", alias, Integer.valueOf(attestationResult)));
                                    keyChain.removeKeyPair(alias);
                                    if (attestationResult != 3) {
                                        try {
                                            $closeResource(null, keyChainConnection);
                                            this.mInjector.binderRestoreCallingIdentity(id);
                                            return false;
                                        } catch (RemoteException e5) {
                                            e = e5;
                                            Log.e(LOG_TAG, "KeyChain error while generating a keypair", e);
                                            this.mInjector.binderRestoreCallingIdentity(id);
                                            return false;
                                        } catch (InterruptedException e6) {
                                            e2 = e6;
                                            Log.w(LOG_TAG, "Interrupted while generating keypair", e2);
                                            Thread.currentThread().interrupt();
                                            this.mInjector.binderRestoreCallingIdentity(id);
                                            return false;
                                        } catch (Throwable th7) {
                                            th = th7;
                                            this.mInjector.binderRestoreCallingIdentity(id);
                                            throw th;
                                        }
                                    } else {
                                        throw new UnsupportedOperationException("Device does not support Device ID attestation.");
                                    }
                                }
                            } catch (Throwable th8) {
                                th3 = th8;
                                th2 = th3;
                                throw th2;
                            }
                        }
                        try {
                            DevicePolicyEventLogger.createEvent(59).setAdmin(callerPackage).setBoolean(who == null).setInt(idAttestationFlags).setStrings(new String[]{algorithm}).write();
                            try {
                                $closeResource(null, keyChainConnection);
                                this.mInjector.binderRestoreCallingIdentity(id);
                                return true;
                            } catch (RemoteException e7) {
                                e = e7;
                                Log.e(LOG_TAG, "KeyChain error while generating a keypair", e);
                                this.mInjector.binderRestoreCallingIdentity(id);
                                return false;
                            } catch (InterruptedException e8) {
                                e2 = e8;
                                Log.w(LOG_TAG, "Interrupted while generating keypair", e2);
                                Thread.currentThread().interrupt();
                                this.mInjector.binderRestoreCallingIdentity(id);
                                return false;
                            }
                        } catch (Throwable th9) {
                            th3 = th9;
                            th2 = th3;
                            throw th2;
                        }
                    }
                } catch (Throwable th10) {
                    th2 = th10;
                    throw th2;
                }
            } catch (RemoteException e9) {
                e = e9;
            } catch (InterruptedException e10) {
                e2 = e10;
                Log.w(LOG_TAG, "Interrupted while generating keypair", e2);
                Thread.currentThread().interrupt();
                this.mInjector.binderRestoreCallingIdentity(id);
                return false;
            } catch (Throwable th11) {
                th = th11;
                this.mInjector.binderRestoreCallingIdentity(id);
                throw th;
            }
        } else {
            throw new IllegalArgumentException("Requested Device ID attestation but challenge is empty.");
        }
    }

    private static /* synthetic */ void $closeResource(Throwable x0, AutoCloseable x1) {
        if (x0 != null) {
            try {
                x1.close();
            } catch (Throwable th) {
                x0.addSuppressed(th);
            }
        } else {
            x1.close();
        }
    }

    public boolean setKeyPairCertificate(ComponentName who, String callerPackage, String alias, byte[] cert, byte[] chain, boolean isUserSelectable) {
        Throwable th;
        InterruptedException e;
        RemoteException e2;
        Throwable th2;
        enforceCanManageScope(who, callerPackage, -1, "delegation-cert-install");
        int callingUid = this.mInjector.binderGetCallingUid();
        long id = this.mInjector.binderClearCallingIdentity();
        try {
            KeyChain.KeyChainConnection keyChainConnection = KeyChain.bindAsUser(this.mContext, UserHandle.getUserHandleForUid(callingUid));
            try {
                IKeyChainService keyChain = keyChainConnection.getService();
                try {
                    if (!keyChain.setKeyPairCertificate(alias, cert, chain)) {
                        try {
                            $closeResource(null, keyChainConnection);
                            this.mInjector.binderRestoreCallingIdentity(id);
                            return false;
                        } catch (InterruptedException e3) {
                            e = e3;
                            Log.w(LOG_TAG, "Interrupted while setting keypair certificate", e);
                            Thread.currentThread().interrupt();
                            this.mInjector.binderRestoreCallingIdentity(id);
                            return false;
                        } catch (RemoteException e4) {
                            e2 = e4;
                            try {
                                Log.e(LOG_TAG, "Failed setting keypair certificate", e2);
                                this.mInjector.binderRestoreCallingIdentity(id);
                                return false;
                            } catch (Throwable th3) {
                                th = th3;
                            }
                        } catch (Throwable th4) {
                            th = th4;
                            this.mInjector.binderRestoreCallingIdentity(id);
                            throw th;
                        }
                    } else {
                        try {
                            keyChain.setUserSelectable(alias, isUserSelectable);
                            DevicePolicyEventLogger.createEvent(60).setAdmin(callerPackage).setBoolean(who == null).write();
                        } catch (Throwable th5) {
                            th2 = th5;
                            try {
                                throw th2;
                            } catch (Throwable th6) {
                                if (keyChainConnection != null) {
                                    $closeResource(th2, keyChainConnection);
                                }
                                throw th6;
                            }
                        }
                        try {
                            $closeResource(null, keyChainConnection);
                            this.mInjector.binderRestoreCallingIdentity(id);
                            return true;
                        } catch (InterruptedException e5) {
                            e = e5;
                            Log.w(LOG_TAG, "Interrupted while setting keypair certificate", e);
                            Thread.currentThread().interrupt();
                            this.mInjector.binderRestoreCallingIdentity(id);
                            return false;
                        } catch (RemoteException e6) {
                            e2 = e6;
                            Log.e(LOG_TAG, "Failed setting keypair certificate", e2);
                            this.mInjector.binderRestoreCallingIdentity(id);
                            return false;
                        }
                    }
                } catch (Throwable th7) {
                    th2 = th7;
                    throw th2;
                }
            } catch (Throwable th8) {
                th2 = th8;
                throw th2;
            }
        } catch (InterruptedException e7) {
            e = e7;
            Log.w(LOG_TAG, "Interrupted while setting keypair certificate", e);
            Thread.currentThread().interrupt();
            this.mInjector.binderRestoreCallingIdentity(id);
            return false;
        } catch (RemoteException e8) {
            e2 = e8;
            Log.e(LOG_TAG, "Failed setting keypair certificate", e2);
            this.mInjector.binderRestoreCallingIdentity(id);
            return false;
        } catch (Throwable th9) {
            th = th9;
            this.mInjector.binderRestoreCallingIdentity(id);
            throw th;
        }
    }

    public void choosePrivateKeyAlias(int uid, Uri uri, String alias, final IBinder response) {
        ComponentName aliasChooser;
        boolean isDelegate;
        long id;
        Throwable th;
        if (isCallerWithSystemUid()) {
            UserHandle caller = this.mInjector.binderGetCallingUserHandle();
            ComponentName aliasChooser2 = getProfileOwner(caller.getIdentifier());
            if (aliasChooser2 != null || !caller.isSystem()) {
                aliasChooser = aliasChooser2;
            } else {
                synchronized (getLockObject()) {
                    ActiveAdmin deviceOwnerAdmin = getDeviceOwnerAdminLocked();
                    if (deviceOwnerAdmin != null) {
                        aliasChooser2 = deviceOwnerAdmin.info.getComponent();
                    }
                }
                aliasChooser = aliasChooser2;
            }
            if (aliasChooser == null) {
                sendPrivateKeyAliasResponse(null, response);
                return;
            }
            Intent intent = new Intent("android.app.action.CHOOSE_PRIVATE_KEY_ALIAS");
            intent.putExtra("android.app.extra.CHOOSE_PRIVATE_KEY_SENDER_UID", uid);
            intent.putExtra("android.app.extra.CHOOSE_PRIVATE_KEY_URI", uri);
            intent.putExtra("android.app.extra.CHOOSE_PRIVATE_KEY_ALIAS", alias);
            intent.putExtra("android.app.extra.CHOOSE_PRIVATE_KEY_RESPONSE", response);
            intent.addFlags(268435456);
            ComponentName delegateReceiver = resolveDelegateReceiver("delegation-cert-selection", "android.app.action.CHOOSE_PRIVATE_KEY_ALIAS", caller.getIdentifier());
            if (delegateReceiver != null) {
                intent.setComponent(delegateReceiver);
                isDelegate = true;
            } else {
                intent.setComponent(aliasChooser);
                isDelegate = false;
            }
            long id2 = this.mInjector.binderClearCallingIdentity();
            try {
                try {
                    this.mContext.sendOrderedBroadcastAsUser(intent, caller, null, new BroadcastReceiver() {
                        /* class com.android.server.devicepolicy.DevicePolicyManagerService.AnonymousClass6 */

                        @Override // android.content.BroadcastReceiver
                        public void onReceive(Context context, Intent intent) {
                            DevicePolicyManagerService.this.sendPrivateKeyAliasResponse(getResultData(), response);
                        }
                    }, null, -1, null, null);
                } catch (Throwable th2) {
                    th = th2;
                    id = id2;
                    this.mInjector.binderRestoreCallingIdentity(id);
                    throw th;
                }
                try {
                    DevicePolicyEventLogger.createEvent(22).setAdmin(intent.getComponent()).setBoolean(isDelegate).write();
                    this.mInjector.binderRestoreCallingIdentity(id2);
                } catch (Throwable th3) {
                    th = th3;
                    id = id2;
                    this.mInjector.binderRestoreCallingIdentity(id);
                    throw th;
                }
            } catch (Throwable th4) {
                th = th4;
                id = id2;
                this.mInjector.binderRestoreCallingIdentity(id);
                throw th;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
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
        if ((scopes.size() != 1 || !scopes.get(0).equals("delegation-cert-install")) && !scopes.isEmpty()) {
            return true;
        }
        return false;
    }

    public void setDelegatedScopes(ComponentName who, String delegatePackage, List<String> scopeList) throws SecurityException {
        Preconditions.checkNotNull(who, "ComponentName is null");
        Preconditions.checkStringNotEmpty(delegatePackage, "Delegate package is null or empty");
        Preconditions.checkCollectionElementsNotNull(scopeList, "Scopes");
        ArrayList<String> scopes = new ArrayList<>(new ArraySet(scopeList));
        if (!scopes.retainAll(Arrays.asList(DELEGATIONS))) {
            boolean hasDoDelegation = !Collections.disjoint(scopes, DEVICE_OWNER_DELEGATIONS);
            int userId = this.mInjector.userHandleGetCallingUserId();
            synchronized (getLockObject()) {
                if (hasDoDelegation) {
                    getActiveAdminForCallerLocked(who, -2);
                } else {
                    getActiveAdminForCallerLocked(who, -1);
                }
                if (shouldCheckIfDelegatePackageIsInstalled(delegatePackage, getTargetSdk(who.getPackageName(), userId), scopes)) {
                    if (!isPackageInstalledForUser(delegatePackage, userId)) {
                        throw new IllegalArgumentException("Package " + delegatePackage + " is not installed on the current user");
                    }
                }
                DevicePolicyData policy = getUserData(userId);
                List<String> exclusiveScopes = null;
                if (!scopes.isEmpty()) {
                    policy.mDelegationMap.put(delegatePackage, new ArrayList(scopes));
                    exclusiveScopes = new ArrayList<>(scopes);
                    exclusiveScopes.retainAll(EXCLUSIVE_DELEGATIONS);
                } else {
                    policy.mDelegationMap.remove(delegatePackage);
                }
                sendDelegationChangedBroadcast(delegatePackage, scopes, userId);
                if (exclusiveScopes != null && !exclusiveScopes.isEmpty()) {
                    for (int i = policy.mDelegationMap.size() - 1; i >= 0; i--) {
                        String currentPackage = policy.mDelegationMap.keyAt(i);
                        List<String> currentScopes = policy.mDelegationMap.valueAt(i);
                        if (!currentPackage.equals(delegatePackage) && currentScopes.removeAll(exclusiveScopes)) {
                            if (currentScopes.isEmpty()) {
                                policy.mDelegationMap.removeAt(i);
                            }
                            sendDelegationChangedBroadcast(currentPackage, new ArrayList<>(currentScopes), userId);
                        }
                    }
                }
                saveSettingsLocked(userId);
            }
            return;
        }
        throw new IllegalArgumentException("Unexpected delegation scopes");
    }

    private void sendDelegationChangedBroadcast(String delegatePackage, ArrayList<String> scopes, int userId) {
        Intent intent = new Intent("android.app.action.APPLICATION_DELEGATION_SCOPES_CHANGED");
        intent.addFlags(1073741824);
        intent.setPackage(delegatePackage);
        intent.putStringArrayListExtra("android.app.extra.DELEGATION_SCOPES", scopes);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.of(userId));
    }

    public List<String> getDelegatedScopes(ComponentName who, String delegatePackage) throws SecurityException {
        List<String> list;
        Preconditions.checkNotNull(delegatePackage, "Delegate package is null");
        int callingUid = this.mInjector.binderGetCallingUid();
        int userId = UserHandle.getUserId(callingUid);
        synchronized (getLockObject()) {
            if (who != null) {
                getActiveAdminForCallerLocked(who, -1);
            } else {
                int uid = 0;
                try {
                    uid = this.mInjector.getPackageManager().getPackageUidAsUser(delegatePackage, userId);
                } catch (PackageManager.NameNotFoundException e) {
                    Slog.e(LOG_TAG, "cannot find the Name.");
                }
                if (uid != callingUid) {
                    throw new SecurityException("Caller with uid " + callingUid + " is not " + delegatePackage);
                }
            }
            List<String> scopes = getUserData(userId).mDelegationMap.get(delegatePackage);
            list = scopes == null ? Collections.EMPTY_LIST : scopes;
        }
        return list;
    }

    public List<String> getDelegatePackages(ComponentName who, String scope) throws SecurityException {
        List<String> delegatePackagesInternalLocked;
        Preconditions.checkNotNull(who, "ComponentName is null");
        Preconditions.checkNotNull(scope, "Scope is null");
        if (Arrays.asList(DELEGATIONS).contains(scope)) {
            int userId = this.mInjector.userHandleGetCallingUserId();
            synchronized (getLockObject()) {
                getActiveAdminForCallerLocked(who, -1);
                delegatePackagesInternalLocked = getDelegatePackagesInternalLocked(scope, userId);
            }
            return delegatePackagesInternalLocked;
        }
        throw new IllegalArgumentException("Unexpected delegation scope: " + scope);
    }

    private List<String> getDelegatePackagesInternalLocked(String scope, int userId) {
        DevicePolicyData policy = getUserData(userId);
        List<String> delegatePackagesWithScope = new ArrayList<>();
        for (int i = 0; i < policy.mDelegationMap.size(); i++) {
            if (policy.mDelegationMap.valueAt(i).contains(scope)) {
                delegatePackagesWithScope.add(policy.mDelegationMap.keyAt(i));
            }
        }
        return delegatePackagesWithScope;
    }

    private ComponentName resolveDelegateReceiver(String scope, String action, int userId) {
        List<String> delegates;
        synchronized (getLockObject()) {
            delegates = getDelegatePackagesInternalLocked(scope, userId);
        }
        if (delegates.size() == 0) {
            return null;
        }
        if (delegates.size() > 1) {
            Slog.wtf(LOG_TAG, "More than one delegate holds " + scope);
            return null;
        }
        Intent intent = new Intent(action);
        intent.setPackage(delegates.get(0));
        try {
            List<ResolveInfo> receivers = this.mIPackageManager.queryIntentReceivers(intent, (String) null, 0, userId).getList();
            int count = receivers.size();
            if (count < 1) {
                return null;
            }
            if (count > 1) {
                Slog.w(LOG_TAG, "the package defines more than one delegate receiver for " + action);
            }
            return receivers.get(0).activityInfo.getComponentName();
        } catch (RemoteException e) {
            return null;
        }
    }

    private boolean isCallerDelegate(String callerPackage, int callerUid, String scope) {
        Preconditions.checkNotNull(callerPackage, "callerPackage is null");
        if (Arrays.asList(DELEGATIONS).contains(scope)) {
            int userId = UserHandle.getUserId(callerUid);
            synchronized (getLockObject()) {
                List<String> scopes = getUserData(userId).mDelegationMap.get(callerPackage);
                boolean z = false;
                if (scopes != null && scopes.contains(scope)) {
                    try {
                        if (this.mInjector.getPackageManager().getPackageUidAsUser(callerPackage, userId) == callerUid) {
                            z = true;
                        }
                        return z;
                    } catch (PackageManager.NameNotFoundException e) {
                        Slog.e(LOG_TAG, "cannot find the name.");
                    }
                }
                return false;
            }
        }
        throw new IllegalArgumentException("Unexpected delegation scope: " + scope);
    }

    private void enforceCanManageScope(ComponentName who, String callerPackage, int reqPolicy, String scope) {
        enforceCanManageScopeOrCheckPermission(who, callerPackage, reqPolicy, scope, null);
    }

    private void enforceCanManageScopeOrCheckPermission(ComponentName who, String callerPackage, int reqPolicy, String scope, String permission) {
        if (who != null) {
            synchronized (getLockObject()) {
                getActiveAdminForCallerLocked(who, reqPolicy);
            }
        } else if (!isCallerDelegate(callerPackage, this.mInjector.binderGetCallingUid(), scope)) {
            if (permission != null) {
                this.mContext.enforceCallingOrSelfPermission(permission, null);
                return;
            }
            throw new SecurityException("Caller with uid " + this.mInjector.binderGetCallingUid() + " is not a delegate of scope " + scope + ".");
        }
    }

    private void setDelegatedScopePreO(ComponentName who, String delegatePackage, String scope) {
        Preconditions.checkNotNull(who, "ComponentName is null");
        int userId = this.mInjector.userHandleGetCallingUserId();
        synchronized (getLockObject()) {
            getActiveAdminForCallerLocked(who, -1);
            DevicePolicyData policy = getUserData(userId);
            if (delegatePackage != null) {
                List<String> scopes = policy.mDelegationMap.get(delegatePackage);
                if (scopes == null) {
                    scopes = new ArrayList();
                }
                if (!scopes.contains(scope)) {
                    scopes.add(scope);
                    setDelegatedScopes(who, delegatePackage, scopes);
                }
            }
            for (int i = 0; i < policy.mDelegationMap.size(); i++) {
                String currentPackage = policy.mDelegationMap.keyAt(i);
                List<String> currentScopes = policy.mDelegationMap.valueAt(i);
                if (!currentPackage.equals(delegatePackage) && currentScopes.contains(scope)) {
                    List<String> newScopes = new ArrayList<>(currentScopes);
                    newScopes.remove(scope);
                    setDelegatedScopes(who, currentPackage, newScopes);
                }
            }
        }
    }

    public void setCertInstallerPackage(ComponentName who, String installerPackage) throws SecurityException {
        setDelegatedScopePreO(who, installerPackage, "delegation-cert-install");
        DevicePolicyEventLogger.createEvent(25).setAdmin(who).setStrings(new String[]{installerPackage}).write();
    }

    public String getCertInstallerPackage(ComponentName who) throws SecurityException {
        List<String> delegatePackages = getDelegatePackages(who, "delegation-cert-install");
        if (delegatePackages.size() > 0) {
            return delegatePackages.get(0);
        }
        return null;
    }

    public boolean setAlwaysOnVpnPackage(ComponentName admin, String vpnPackage, boolean lockdown, List<String> lockdownWhitelist) throws SecurityException {
        enforceProfileOrDeviceOwner(admin);
        int userId = this.mInjector.userHandleGetCallingUserId();
        long token = this.mInjector.binderClearCallingIdentity();
        if (vpnPackage != null) {
            try {
                if (!isPackageInstalledForUser(vpnPackage, userId)) {
                    Slog.w(LOG_TAG, "Non-existent VPN package specified: " + vpnPackage);
                    throw new ServiceSpecificException(1, vpnPackage);
                }
            } catch (Throwable th) {
                this.mInjector.binderRestoreCallingIdentity(token);
                throw th;
            }
        }
        if (!(vpnPackage == null || !lockdown || lockdownWhitelist == null)) {
            for (String packageName : lockdownWhitelist) {
                if (!isPackageInstalledForUser(packageName, userId)) {
                    Slog.w(LOG_TAG, "Non-existent package in VPN whitelist: " + packageName);
                    throw new ServiceSpecificException(1, packageName);
                }
            }
        }
        if (this.mInjector.getConnectivityManager().setAlwaysOnVpnPackageForUser(userId, vpnPackage, lockdown, lockdownWhitelist)) {
            int i = 0;
            DevicePolicyEventLogger devicePolicyEventLogger = DevicePolicyEventLogger.createEvent(26).setAdmin(admin).setStrings(new String[]{vpnPackage}).setBoolean(lockdown);
            if (lockdownWhitelist != null) {
                i = lockdownWhitelist.size();
            }
            devicePolicyEventLogger.setInt(i).write();
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
            return this.mInjector.getConnectivityManager().getAlwaysOnVpnPackageForUser(userId);
        } finally {
            this.mInjector.binderRestoreCallingIdentity(token);
        }
    }

    public boolean isAlwaysOnVpnLockdownEnabled(ComponentName admin) throws SecurityException {
        enforceProfileOrDeviceOwner(admin);
        int userId = this.mInjector.userHandleGetCallingUserId();
        long token = this.mInjector.binderClearCallingIdentity();
        try {
            return this.mInjector.getConnectivityManager().isVpnLockdownEnabled(userId);
        } finally {
            this.mInjector.binderRestoreCallingIdentity(token);
        }
    }

    public List<String> getAlwaysOnVpnLockdownWhitelist(ComponentName admin) throws SecurityException {
        enforceProfileOrDeviceOwner(admin);
        int userId = this.mInjector.userHandleGetCallingUserId();
        long token = this.mInjector.binderClearCallingIdentity();
        try {
            return this.mInjector.getConnectivityManager().getVpnLockdownWhitelist(userId);
        } finally {
            this.mInjector.binderRestoreCallingIdentity(token);
        }
    }

    private void forceWipeDeviceNoLock(boolean wipeExtRequested, String reason, boolean wipeEuicc) {
        wtfIfInLock();
        if (wipeExtRequested) {
            ((StorageManager) this.mContext.getSystemService("storage")).wipeAdoptableDisks();
        }
        HwCustDevicePolicyManagerService hwCustDevicePolicyManagerService = mHwCustDevicePolicyManagerService;
        if (hwCustDevicePolicyManagerService != null) {
            hwCustDevicePolicyManagerService.clearWipeDataFactoryLowlevel(this.mContext, reason, wipeEuicc);
        }
    }

    private void forceWipeUser(int userId, String wipeReasonForUser, boolean wipeSilently) {
        try {
            IActivityManager am = this.mInjector.getIActivityManager();
            if (am.getCurrentUser().id == userId) {
                am.switchUser(0);
            }
            boolean success = this.mUserManagerInternal.removeUserEvenWhenDisallowed(userId);
            if (!success) {
                Slog.w(LOG_TAG, "Couldn't remove user " + userId);
            } else if (isManagedProfile(userId) && !wipeSilently) {
                sendWipeProfileNotification(wipeReasonForUser);
            }
            if (success) {
                return;
            }
        } catch (RemoteException e) {
            Slog.e(LOG_TAG, "cannot force wipe user.");
            if (0 != 0) {
                return;
            }
        } catch (Throwable th) {
            if (0 == 0) {
                SecurityLog.writeEvent(210023, new Object[0]);
            }
            throw th;
        }
        SecurityLog.writeEvent(210023, new Object[0]);
    }

    public void wipeDataWithReason(int flags, String wipeReasonForUser) {
        ActiveAdmin admin;
        if (this.mHasFeature) {
            Preconditions.checkStringNotEmpty(wipeReasonForUser, "wipeReasonForUser is null or empty");
            enforceFullCrossUsersPermission(this.mInjector.userHandleGetCallingUserId());
            synchronized (getLockObject()) {
                admin = getActiveAdminForCallerLocked(null, 4);
            }
            DevicePolicyEventLogger.createEvent(11).setAdmin(admin.info.getComponent()).setInt(flags).write();
            wipeDataNoLock(admin.info.getComponent(), flags, "DevicePolicyManager.wipeDataWithReason() from " + admin.info.getComponent().flattenToShortString(), wipeReasonForUser, admin.getUserHandle().getIdentifier());
        }
    }

    private void wipeDataNoLock(ComponentName admin, int flags, String internalReason, String wipeReasonForUser, int userId) {
        String restriction;
        wtfIfInLock();
        long ident = this.mInjector.binderClearCallingIdentity();
        if (userId == 0) {
            restriction = "no_factory_reset";
        } else {
            try {
                if (isManagedProfile(userId)) {
                    restriction = "no_remove_managed_profile";
                } else {
                    restriction = "no_remove_user";
                }
            } catch (Throwable th) {
                this.mInjector.binderRestoreCallingIdentity(ident);
                throw th;
            }
        }
        if (!isAdminAffectedByRestriction(admin, restriction, userId)) {
            if (mHwCustDevicePolicyManagerService != null) {
                mHwCustDevicePolicyManagerService.monitorFactoryReset(admin.flattenToShortString(), internalReason);
            }
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
            boolean z = false;
            if (userId == 0) {
                boolean z2 = (flags & 1) != 0;
                if ((flags & 4) != 0) {
                    z = true;
                }
                forceWipeDeviceNoLock(z2, internalReason, z);
            } else {
                if ((flags & 8) != 0) {
                    z = true;
                }
                forceWipeUser(userId, wipeReasonForUser, z);
            }
            this.mInjector.binderRestoreCallingIdentity(ident);
            return;
        }
        throw new SecurityException("Cannot wipe data. " + restriction + " restriction is set for user " + userId);
    }

    private void sendWipeProfileNotification(String wipeReasonForUser) {
        this.mInjector.getNotificationManager().notify(NetworkAgentInfo.EVENT_NETWORK_LINGER_COMPLETE, new Notification.Builder(this.mContext, SystemNotificationChannels.DEVICE_ADMIN).setSmallIcon(17301642).setContentTitle(this.mContext.getString(17041584)).setContentText(wipeReasonForUser).setColor(this.mContext.getColor(17170460)).setStyle(new Notification.BigTextStyle().bigText(wipeReasonForUser)).build());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void clearWipeProfileNotification() {
        this.mInjector.getNotificationManager().cancel(NetworkAgentInfo.EVENT_NETWORK_LINGER_COMPLETE);
    }

    public void getRemoveWarning(ComponentName comp, final RemoteCallback result, int userHandle) {
        if (this.mHasFeature) {
            enforceFullCrossUsersPermission(userHandle);
            this.mContext.enforceCallingOrSelfPermission("android.permission.BIND_DEVICE_ADMIN", null);
            synchronized (getLockObject()) {
                ActiveAdmin admin = getActiveAdminUncheckedLocked(comp, userHandle);
                if (admin == null) {
                    result.sendResult((Bundle) null);
                    return;
                }
                Intent intent = new Intent("android.app.action.DEVICE_ADMIN_DISABLE_REQUESTED");
                intent.setFlags(268435456);
                intent.setComponent(admin.info.getComponent());
                this.mContext.sendOrderedBroadcastAsUser(intent, new UserHandle(userHandle), null, new BroadcastReceiver() {
                    /* class com.android.server.devicepolicy.DevicePolicyManagerService.AnonymousClass7 */

                    @Override // android.content.BroadcastReceiver
                    public void onReceive(Context context, Intent intent) {
                        result.sendResult(getResultExtras(false));
                    }
                }, null, -1, null, null);
            }
        }
    }

    public void setActivePasswordState(PasswordMetrics metrics, int userHandle) {
        if (this.mLockPatternUtils.hasSecureLockScreen()) {
            enforceFullCrossUsersPermission(userHandle);
            this.mContext.enforceCallingOrSelfPermission("android.permission.BIND_DEVICE_ADMIN", null);
            if (isManagedProfile(userHandle) && !isSeparateProfileChallengeEnabled(userHandle)) {
                metrics = new PasswordMetrics();
            }
            validateQualityConstant(metrics.quality);
            synchronized (getLockObject()) {
                this.mUserPasswordMetrics.put(userHandle, metrics);
            }
        }
    }

    public void reportPasswordChanged(int userId) {
        if (this.mHasFeature && this.mLockPatternUtils.hasSecureLockScreen()) {
            enforceFullCrossUsersPermission(userId);
            if (!isSeparateProfileChallengeEnabled(userId)) {
                enforceNotManagedProfile(userId, "set the active password");
            }
            this.mContext.enforceCallingOrSelfPermission("android.permission.BIND_DEVICE_ADMIN", null);
            DevicePolicyData policy = getUserData(userId);
            long ident = this.mInjector.binderClearCallingIdentity();
            try {
                synchronized (getLockObject()) {
                    policy.mFailedPasswordAttempts = 0;
                    updatePasswordValidityCheckpointLocked(userId, false);
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
        ArraySet<Integer> affectedUserIds = new ArraySet<>();
        List<ActiveAdmin> admins = getActiveAdminsForLockscreenPoliciesLocked(userHandle, false);
        int N = admins.size();
        for (int i = 0; i < N; i++) {
            ActiveAdmin admin = admins.get(i);
            if (admin.info.usesPolicy(6)) {
                affectedUserIds.add(Integer.valueOf(admin.getUserHandle().getIdentifier()));
                long timeout = admin.passwordExpirationTimeout;
                long expiration = 0;
                if (timeout > 0) {
                    expiration = System.currentTimeMillis() + timeout;
                }
                admin.passwordExpirationDate = expiration;
            }
        }
        Iterator<Integer> it = affectedUserIds.iterator();
        while (it.hasNext()) {
            saveSettingsLocked(it.next().intValue());
        }
    }

    /* JADX INFO: finally extract failed */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x0156  */
    /* JADX WARNING: Removed duplicated region for block: B:73:? A[RETURN, SYNTHETIC] */
    public void reportFailedPasswordAttempt(int userHandle) {
        Throwable th;
        int failedAttempts;
        boolean wipeData;
        ActiveAdmin strictestAdmin;
        int userId;
        SecurityException e;
        enforceFullCrossUsersPermission(userHandle);
        if (!isSeparateProfileChallengeEnabled(userHandle)) {
            enforceNotManagedProfile(userHandle, "report failed password attempt if separate profile challenge is not in place");
        }
        this.mContext.enforceCallingOrSelfPermission("android.permission.BIND_DEVICE_ADMIN", null);
        boolean wipeData2 = false;
        long ident = this.mInjector.binderClearCallingIdentity();
        try {
            synchronized (getLockObject()) {
                try {
                    DevicePolicyData policy = getUserData(userHandle);
                    policy.mFailedPasswordAttempts++;
                    saveSettingsLocked(userHandle);
                    failedAttempts = policy.mFailedPasswordAttempts;
                    try {
                        if (this.mHasFeature) {
                            ActiveAdmin strictestAdmin2 = getAdminWithMinimumFailedPasswordsForWipeLocked(userHandle, false);
                            int max = strictestAdmin2 != null ? strictestAdmin2.maximumFailedPasswordsForWipe : 0;
                            if (max > 0 && policy.mFailedPasswordAttempts >= max) {
                                wipeData2 = true;
                            }
                            sendAdminCommandForLockscreenPoliciesLocked("android.app.action.ACTION_PASSWORD_FAILED", 1, userHandle);
                            wipeData = wipeData2;
                            strictestAdmin = strictestAdmin2;
                        } else {
                            wipeData = false;
                            strictestAdmin = null;
                        }
                        try {
                        } catch (Throwable th2) {
                            th = th2;
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        throw th;
                    }
                } catch (Throwable th4) {
                    th = th4;
                    throw th;
                }
            }
            this.mInjector.binderRestoreCallingIdentity(ident);
            HwCustDevicePolicyManagerService mHwCustRecoverySystem = (HwCustDevicePolicyManagerService) HwCustUtils.createObj(HwCustDevicePolicyManagerService.class, new Object[0]);
            if (mHwCustRecoverySystem != null && mHwCustRecoverySystem.isAttEraseDataOn(this.mContext)) {
                mHwCustRecoverySystem.isStartEraseAllDataForAtt(this.mContext, failedAttempts);
            }
            if (wipeData && strictestAdmin != null) {
                int userId2 = strictestAdmin.getUserHandle().getIdentifier();
                Slog.i(LOG_TAG, "Max failed password attempts policy reached for admin: " + strictestAdmin.info.getComponent().flattenToShortString() + ". Calling wipeData for user " + userId2);
                StringBuilder sb = new StringBuilder();
                sb.append("WipeData for max failed PWD reached:");
                sb.append(strictestAdmin.maximumFailedPasswordsForWipe);
                String reason = sb.toString();
                HwCustDevicePolicyManagerService hwCustDevicePolicyManagerService = mHwCustDevicePolicyManagerService;
                if (hwCustDevicePolicyManagerService != null) {
                    hwCustDevicePolicyManagerService.monitorFactoryReset(strictestAdmin.info.getComponent().flattenToShortString(), reason);
                }
                try {
                    String wipeReasonForUser = this.mContext.getString(17041587);
                    boolean isCustWipeData = false;
                    if (userHandle == 0 && mHwCustRecoverySystem != null) {
                        try {
                            if (mHwCustRecoverySystem.eraseStorageForEAS(this.mContext) || mHwCustRecoverySystem.wipeDataAndReset(this.mContext)) {
                                isCustWipeData = true;
                                Slog.d(LOG_TAG, "Successed wipe storage data.");
                            }
                        } catch (SecurityException e2) {
                            e = e2;
                            userId = userId2;
                            Slog.w(LOG_TAG, "Failed to wipe user " + userId + " after max failed password attempts reached.", e);
                            if (this.mInjector.securityLogIsLoggingEnabled()) {
                            }
                        }
                    }
                    if (!isCustWipeData) {
                        userId = userId2;
                        try {
                            wipeDataNoLock(strictestAdmin.info.getComponent(), 0, reason, wipeReasonForUser, userId);
                        } catch (SecurityException e3) {
                            e = e3;
                        }
                    }
                } catch (SecurityException e4) {
                    e = e4;
                    userId = userId2;
                    Slog.w(LOG_TAG, "Failed to wipe user " + userId + " after max failed password attempts reached.", e);
                    if (this.mInjector.securityLogIsLoggingEnabled()) {
                    }
                }
            }
            if (this.mInjector.securityLogIsLoggingEnabled()) {
                SecurityLog.writeEvent(210007, new Object[]{0, 1});
            }
        } catch (Throwable th5) {
            this.mInjector.binderRestoreCallingIdentity(ident);
            throw th5;
        }
    }

    public void reportSuccessfulPasswordAttempt(int userHandle) {
        enforceFullCrossUsersPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("android.permission.BIND_DEVICE_ADMIN", null);
        synchronized (getLockObject()) {
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
                } finally {
                    this.mInjector.binderRestoreCallingIdentity(ident);
                }
            }
        }
        if (this.mInjector.securityLogIsLoggingEnabled()) {
            SecurityLog.writeEvent(210007, new Object[]{1, 1});
        }
    }

    public void reportFailedBiometricAttempt(int userHandle) {
        enforceFullCrossUsersPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("android.permission.BIND_DEVICE_ADMIN", null);
        if (this.mInjector.securityLogIsLoggingEnabled()) {
            SecurityLog.writeEvent(210007, new Object[]{0, 0});
        }
    }

    public void reportSuccessfulBiometricAttempt(int userHandle) {
        enforceFullCrossUsersPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("android.permission.BIND_DEVICE_ADMIN", null);
        if (this.mInjector.securityLogIsLoggingEnabled()) {
            SecurityLog.writeEvent(210007, new Object[]{1, 0});
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

    public ComponentName setGlobalProxy(ComponentName who, String proxySpec, String exclusionList) {
        if (!this.mHasFeature) {
            return null;
        }
        synchronized (getLockObject()) {
            Preconditions.checkNotNull(who, "ComponentName is null");
            DevicePolicyData policy = getUserData(0);
            ActiveAdmin admin = getActiveAdminForCallerLocked(who, 5);
            for (ComponentName component : policy.mAdminMap.keySet()) {
                if (policy.mAdminMap.get(component).specifiesGlobalProxy && !component.equals(who)) {
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
                return null;
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
        synchronized (getLockObject()) {
            DevicePolicyData policy = getUserData(0);
            int N = policy.mAdminList.size();
            for (int i = 0; i < N; i++) {
                ActiveAdmin ap = policy.mAdminList.get(i);
                if (ap.specifiesGlobalProxy) {
                    return ap.info.getComponent();
                }
            }
            return null;
        }
    }

    public void setRecommendedGlobalProxy(ComponentName who, ProxyInfo proxyInfo) {
        enforceDeviceOwner(who);
        long token = this.mInjector.binderClearCallingIdentity();
        try {
            this.mInjector.getConnectivityManager().setGlobalProxy(proxyInfo);
        } finally {
            this.mInjector.binderRestoreCallingIdentity(token);
        }
    }

    private void resetGlobalProxyLocked(DevicePolicyData policy) {
        int N = policy.mAdminList.size();
        for (int i = 0; i < N; i++) {
            ActiveAdmin ap = policy.mAdminList.get(i);
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
                Slog.e(LOG_TAG, "cannot save global proxy locked.");
            }
        }
        String exclusionList2 = exclusionList.trim();
        ProxyInfo proxyProperties = new ProxyInfo(data[0], proxyPort, exclusionList2);
        if (!proxyProperties.isValid()) {
            Slog.e(LOG_TAG, "Invalid proxy properties, ignoring: " + proxyProperties.toString());
            return;
        }
        this.mInjector.settingsGlobalPutString("global_http_proxy_host", data[0]);
        this.mInjector.settingsGlobalPutInt("global_http_proxy_port", proxyPort);
        this.mInjector.settingsGlobalPutString("global_http_proxy_exclusion_list", exclusionList2);
    }

    public int setStorageEncryption(ComponentName who, boolean encrypt) {
        int i;
        if (!this.mHasFeature) {
            return 0;
        }
        Preconditions.checkNotNull(who, "ComponentName is null");
        int userHandle = UserHandle.getCallingUserId();
        synchronized (getLockObject()) {
            if (userHandle != 0) {
                Slog.w(LOG_TAG, "Only owner/system user is allowed to set storage encryption. User " + UserHandle.getCallingUserId() + " is not permitted.");
                return 0;
            }
            ActiveAdmin ap = getActiveAdminForCallerLocked(who, 7);
            if (!isEncryptionSupported()) {
                return 0;
            }
            if (ap.encryptionRequested != encrypt) {
                ap.encryptionRequested = encrypt;
                saveSettingsLocked(userHandle);
            }
            DevicePolicyData policy = getUserData(0);
            boolean newRequested = false;
            int N = policy.mAdminList.size();
            for (int i2 = 0; i2 < N; i2++) {
                newRequested |= policy.mAdminList.get(i2).encryptionRequested;
            }
            setEncryptionRequested(newRequested);
            if (newRequested) {
                i = 3;
            } else {
                i = 1;
            }
            return i;
        }
    }

    public boolean getStorageEncryption(ComponentName who, int userHandle) {
        boolean z = false;
        if (!this.mHasFeature) {
            return false;
        }
        enforceFullCrossUsersPermission(userHandle);
        synchronized (getLockObject()) {
            if (who != null) {
                ActiveAdmin ap = getActiveAdminUncheckedLocked(who, userHandle);
                if (ap != null) {
                    z = ap.encryptionRequested;
                }
                return z;
            }
            DevicePolicyData policy = getUserData(userHandle);
            int N = policy.mAdminList.size();
            for (int i = 0; i < N; i++) {
                if (policy.mAdminList.get(i).encryptionRequested) {
                    return true;
                }
            }
            return false;
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
            if (rawStatus != 5 || !legacyApp) {
                return rawStatus;
            }
            return 3;
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
        if (this.mInjector.storageManagerIsEncrypted()) {
            return 4;
        }
        if (this.mInjector.storageManagerIsEncryptable()) {
            return 1;
        }
        return 0;
    }

    private void setEncryptionRequested(boolean encrypt) {
    }

    public void setScreenCaptureDisabled(ComponentName who, boolean disabled) {
        if (this.mHasFeature) {
            Preconditions.checkNotNull(who, "ComponentName is null");
            int userHandle = UserHandle.getCallingUserId();
            synchronized (getLockObject()) {
                ActiveAdmin ap = getActiveAdminForCallerLocked(who, -1);
                if (ap.disableScreenCapture != disabled) {
                    ap.disableScreenCapture = disabled;
                    saveSettingsLocked(userHandle);
                    updateScreenCaptureDisabled(userHandle, disabled);
                }
            }
            DevicePolicyEventLogger.createEvent(29).setAdmin(who).setBoolean(disabled).write();
        }
    }

    public boolean getScreenCaptureDisabled(ComponentName who, int userHandle) {
        boolean z = false;
        if (!this.mHasFeature) {
            return false;
        }
        synchronized (getLockObject()) {
            if (who != null) {
                ActiveAdmin admin = getActiveAdminUncheckedLocked(who, userHandle);
                if (admin != null) {
                    z = admin.disableScreenCapture;
                }
                return z;
            }
            DevicePolicyData policy = getUserData(userHandle);
            int N = policy.mAdminList.size();
            for (int i = 0; i < N; i++) {
                if (policy.mAdminList.get(i).disableScreenCapture) {
                    return true;
                }
            }
            return false;
        }
    }

    private void updateScreenCaptureDisabled(final int userHandle, boolean disabled) {
        this.mPolicyCache.setScreenCaptureDisabled(userHandle, disabled);
        this.mHandler.post(new Runnable() {
            /* class com.android.server.devicepolicy.DevicePolicyManagerService.AnonymousClass8 */

            @Override // java.lang.Runnable
            public void run() {
                try {
                    DevicePolicyManagerService.this.mInjector.getIWindowManager().refreshScreenCaptureDisabled(userHandle);
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
            synchronized (getLockObject()) {
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
            DevicePolicyEventLogger.createEvent(36).setAdmin(who).setBoolean(required).write();
        }
    }

    public boolean getAutoTimeRequired() {
        if (!this.mHasFeature) {
            return false;
        }
        synchronized (getLockObject()) {
            ActiveAdmin deviceOwner = getDeviceOwnerAdminLocked();
            if (deviceOwner != null && deviceOwner.requireAutoTime) {
                return true;
            }
            for (Integer userId : this.mOwners.getProfileOwnerKeys()) {
                ActiveAdmin profileOwner = getProfileOwnerAdminLocked(userId.intValue());
                if (profileOwner != null && profileOwner.requireAutoTime) {
                    return true;
                }
            }
            return false;
        }
    }

    public void setForceEphemeralUsers(ComponentName who, boolean forceEphemeralUsers) {
        if (this.mHasFeature) {
            Preconditions.checkNotNull(who, "ComponentName is null");
            if (!forceEphemeralUsers || this.mInjector.userManagerIsSplitSystemUser()) {
                boolean removeAllUsers = false;
                synchronized (getLockObject()) {
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
            } else {
                throw new UnsupportedOperationException("Cannot force ephemeral users on systems without split system user.");
            }
        }
    }

    public boolean getForceEphemeralUsers(ComponentName who) {
        boolean z;
        if (!this.mHasFeature) {
            return false;
        }
        Preconditions.checkNotNull(who, "ComponentName is null");
        synchronized (getLockObject()) {
            z = getActiveAdminForCallerLocked(who, -2).forceEphemeralUsers;
        }
        return z;
    }

    private void ensureDeviceOwnerAndAllUsersAffiliated(ComponentName who) throws SecurityException {
        synchronized (getLockObject()) {
            getActiveAdminForCallerLocked(who, -2);
        }
        ensureAllUsersAffiliated();
    }

    private void ensureAllUsersAffiliated() throws SecurityException {
        synchronized (getLockObject()) {
            if (!areAllUsersAffiliatedWithDeviceLocked()) {
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
        synchronized (getLockObject()) {
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
            this.mHandler.postDelayed(this.mRemoteBugreportTimeoutRunnable, 600000);
            DevicePolicyEventLogger.createEvent(53).setAdmin(who).write();
            return true;
        } catch (RemoteException re) {
            Slog.e(LOG_TAG, "Failed to make remote calls to start bugreportremote service", re);
            return false;
        } finally {
            this.mInjector.binderRestoreCallingIdentity(callingIdentity);
        }
    }

    /* access modifiers changed from: package-private */
    public void sendDeviceOwnerCommand(String action, Bundle extras) {
        int deviceOwnerUserId;
        synchronized (getLockObject()) {
            deviceOwnerUserId = this.mOwners.getDeviceOwnerUserId();
        }
        ComponentName receiverComponent = null;
        if (action.equals("android.app.action.NETWORK_LOGS_AVAILABLE")) {
            receiverComponent = resolveDelegateReceiver("delegation-network-logging", action, deviceOwnerUserId);
        }
        if (receiverComponent == null) {
            synchronized (getLockObject()) {
                receiverComponent = this.mOwners.getDeviceOwnerComponent();
            }
        }
        sendActiveAdminCommand(action, extras, deviceOwnerUserId, receiverComponent);
    }

    private void sendProfileOwnerCommand(String action, Bundle extras, int userHandle) {
        sendActiveAdminCommand(action, extras, userHandle, this.mOwners.getProfileOwnerComponent(userHandle));
    }

    private void sendActiveAdminCommand(String action, Bundle extras, int userHandle, ComponentName receiverComponent) {
        Intent intent = new Intent(action);
        intent.setComponent(receiverComponent);
        if (extras != null) {
            intent.putExtras(extras);
        }
        this.mContext.sendBroadcastAsUser(intent, UserHandle.of(userHandle));
    }

    private void sendOwnerChangedBroadcast(String broadcast, int userId) {
        this.mContext.sendBroadcastAsUser(new Intent(broadcast).addFlags(DumpState.DUMP_SERVICE_PERMISSIONS), UserHandle.of(userId));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String getDeviceOwnerRemoteBugreportUri() {
        String deviceOwnerRemoteBugreportUri;
        synchronized (getLockObject()) {
            deviceOwnerRemoteBugreportUri = this.mOwners.getDeviceOwnerRemoteBugreportUri();
        }
        return deviceOwnerRemoteBugreportUri;
    }

    private void setDeviceOwnerRemoteBugreportUriAndHash(String bugreportUri, String bugreportHash) {
        synchronized (getLockObject()) {
            this.mOwners.setDeviceOwnerRemoteBugreportUriAndHash(bugreportUri, bugreportHash);
        }
    }

    private void registerRemoteBugreportReceivers() {
        try {
            this.mContext.registerReceiver(this.mRemoteBugreportFinishedReceiver, new IntentFilter("android.intent.action.REMOTE_BUGREPORT_DISPATCH", "application/vnd.android.bugreport"));
        } catch (IntentFilter.MalformedMimeTypeException e) {
            Slog.w(LOG_TAG, "Failed to set type application/vnd.android.bugreport", e);
        }
        IntentFilter filterConsent = new IntentFilter();
        filterConsent.addAction("com.android.server.action.REMOTE_BUGREPORT_SHARING_DECLINED");
        filterConsent.addAction("com.android.server.action.REMOTE_BUGREPORT_SHARING_ACCEPTED");
        this.mContext.registerReceiver(this.mRemoteBugreportConsentReceiver, filterConsent);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onBugreportSharingAccepted() {
        String bugreportUriString;
        String bugreportHash;
        this.mRemoteBugreportSharingAccepted.set(true);
        synchronized (getLockObject()) {
            bugreportUriString = getDeviceOwnerRemoteBugreportUri();
            bugreportHash = this.mOwners.getDeviceOwnerRemoteBugreportHash();
        }
        if (bugreportUriString != null) {
            shareBugreportWithDeviceOwnerIfExists(bugreportUriString, bugreportHash);
        } else if (this.mRemoteBugreportServiceIsActive.get()) {
            this.mInjector.getNotificationManager().notifyAsUser(LOG_TAG, 678432343, RemoteBugreportUtils.buildNotification(this.mContext, 2), UserHandle.ALL);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
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
        if (bugreportUriString != null) {
            try {
                Uri bugreportUri = Uri.parse(bugreportUriString);
                ParcelFileDescriptor pfd2 = this.mContext.getContentResolver().openFileDescriptor(bugreportUri, "r");
                synchronized (getLockObject()) {
                    Intent intent = new Intent("android.app.action.BUGREPORT_SHARE");
                    intent.setComponent(this.mOwners.getDeviceOwnerComponent());
                    intent.setDataAndType(bugreportUri, "application/vnd.android.bugreport");
                    intent.putExtra("android.app.extra.BUGREPORT_HASH", bugreportHash);
                    intent.setFlags(1);
                    ((UriGrantsManagerInternal) LocalServices.getService(UriGrantsManagerInternal.class)).grantUriPermissionFromIntent(IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME, this.mOwners.getDeviceOwnerComponent().getPackageName(), intent, this.mOwners.getDeviceOwnerUserId());
                    this.mContext.sendBroadcastAsUser(intent, UserHandle.of(this.mOwners.getDeviceOwnerUserId()));
                }
                if (pfd2 != null) {
                    try {
                        pfd2.close();
                    } catch (IOException e) {
                    }
                }
            } catch (FileNotFoundException e2) {
                Bundle extras = new Bundle();
                extras.putInt("android.app.extra.BUGREPORT_FAILURE_REASON", 1);
                sendDeviceOwnerCommand("android.app.action.BUGREPORT_FAILED", extras);
                if (0 != 0) {
                    try {
                        pfd.close();
                    } catch (IOException e3) {
                    }
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    try {
                        pfd.close();
                    } catch (IOException e4) {
                        Slog.e(LOG_TAG, "cannot close ParcelFileDescriptor.");
                    }
                }
                this.mRemoteBugreportSharingAccepted.set(false);
                setDeviceOwnerRemoteBugreportUriAndHash(null, null);
                throw th;
            }
            this.mRemoteBugreportSharingAccepted.set(false);
            setDeviceOwnerRemoteBugreportUriAndHash(null, null);
        }
        throw new FileNotFoundException();
        Slog.e(LOG_TAG, "cannot close ParcelFileDescriptor.");
        this.mRemoteBugreportSharingAccepted.set(false);
        setDeviceOwnerRemoteBugreportUriAndHash(null, null);
    }

    public void setCameraDisabled(ComponentName who, boolean disabled) {
        if (this.mHasFeature) {
            Preconditions.checkNotNull(who, "ComponentName is null");
            int userHandle = this.mInjector.userHandleGetCallingUserId();
            synchronized (getLockObject()) {
                ActiveAdmin ap = getActiveAdminForCallerLocked(who, 8);
                if (ap.disableCamera != disabled) {
                    ap.disableCamera = disabled;
                    saveSettingsLocked(userHandle);
                }
            }
            pushUserRestrictions(userHandle);
            DevicePolicyEventLogger.createEvent(30).setAdmin(who).setBoolean(disabled).write();
        }
    }

    public boolean getCameraDisabled(ComponentName who, int userHandle) {
        return getCameraDisabled(who, userHandle, true);
    }

    private boolean getCameraDisabled(ComponentName who, int userHandle, boolean mergeDeviceOwnerRestriction) {
        ActiveAdmin deviceOwner;
        boolean z = false;
        if (!this.mHasFeature) {
            return false;
        }
        synchronized (getLockObject()) {
            if (who != null) {
                try {
                    ActiveAdmin admin = getActiveAdminUncheckedLocked(who, userHandle);
                    if (admin != null) {
                        z = admin.disableCamera;
                    }
                    return z;
                } catch (Throwable th) {
                    throw th;
                }
            } else if (mergeDeviceOwnerRestriction && (deviceOwner = getDeviceOwnerAdminLocked()) != null && deviceOwner.disableCamera) {
                return true;
            } else {
                DevicePolicyData policy = getUserData(userHandle);
                int N = policy.mAdminList.size();
                for (int i = 0; i < N; i++) {
                    if (policy.mAdminList.get(i).disableCamera) {
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
                    which &= 432;
                } else {
                    which &= PROFILE_KEYGUARD_FEATURES;
                }
            }
            synchronized (getLockObject()) {
                ActiveAdmin ap = getActiveAdminForCallerLocked(who, 9, parent);
                if (ap.disabledKeyguardFeatures != which) {
                    ap.disabledKeyguardFeatures = which;
                    saveSettingsLocked(userHandle);
                }
            }
            if (SecurityLog.isLoggingEnabled()) {
                SecurityLog.writeEvent(210021, new Object[]{who.getPackageName(), Integer.valueOf(userHandle), Integer.valueOf(parent ? getProfileParentId(userHandle) : userHandle), Integer.valueOf(which)});
            }
            DevicePolicyEventLogger.createEvent(9).setAdmin(who).setInt(which).setBoolean(parent).write();
        }
    }

    public int getKeyguardDisabledFeatures(ComponentName who, int userHandle, boolean parent) {
        List<ActiveAdmin> admins;
        int i;
        int i2 = 0;
        if (!this.mHasFeature) {
            return 0;
        }
        enforceFullCrossUsersPermission(userHandle);
        long ident = this.mInjector.binderClearCallingIdentity();
        try {
            synchronized (getLockObject()) {
                if (who != null) {
                    ActiveAdmin admin = getActiveAdminUncheckedLocked(who, userHandle, parent);
                    if (admin != null) {
                        i2 = admin.disabledKeyguardFeatures;
                    }
                    return i2;
                }
                if (parent || !isManagedProfile(userHandle)) {
                    admins = getActiveAdminsForLockscreenPoliciesLocked(userHandle, parent);
                } else {
                    admins = getUserDataUnchecked(userHandle).mAdminList;
                }
                int which = 0;
                int N = admins.size();
                for (int i3 = 0; i3 < N; i3++) {
                    ActiveAdmin admin2 = admins.get(i3);
                    int userId = admin2.getUserHandle().getIdentifier();
                    if ((!parent && userId == userHandle) || !isManagedProfile(userId)) {
                        i = admin2.disabledKeyguardFeatures;
                    } else {
                        i = admin2.disabledKeyguardFeatures & 432;
                    }
                    which |= i;
                }
                this.mInjector.binderRestoreCallingIdentity(ident);
                return which;
            }
        } finally {
            this.mInjector.binderRestoreCallingIdentity(ident);
        }
    }

    public void setKeepUninstalledPackages(ComponentName who, String callerPackage, List<String> packageList) {
        if (this.mHasFeature) {
            Preconditions.checkNotNull(packageList, "packageList is null");
            int userHandle = UserHandle.getCallingUserId();
            synchronized (getLockObject()) {
                enforceCanManageScope(who, callerPackage, -2, "delegation-keep-uninstalled-packages");
                getDeviceOwnerAdminLocked().keepUninstalledPackages = packageList;
                saveSettingsLocked(userHandle);
                this.mInjector.getPackageManagerInternal().setKeepUninstalledPackages(packageList);
            }
            DevicePolicyEventLogger.createEvent(61).setAdmin(callerPackage).setBoolean(who == null).setStrings((String[]) packageList.toArray(new String[0])).write();
        }
    }

    public List<String> getKeepUninstalledPackages(ComponentName who, String callerPackage) {
        List<String> keepUninstalledPackagesLocked;
        if (!this.mHasFeature) {
            return null;
        }
        synchronized (getLockObject()) {
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

    /* JADX INFO: finally extract failed */
    public boolean setDeviceOwner(ComponentName admin, String ownerName, int userId) {
        if (!this.mHasFeature) {
            return false;
        }
        if (admin == null || !isPackageInstalledForUser(admin.getPackageName(), userId)) {
            throw new IllegalArgumentException("Invalid component " + admin + " for device owner");
        }
        boolean hasIncompatibleAccountsOrNonAdb = hasIncompatibleAccountsOrNonAdbNoLock(userId, admin);
        synchronized (getLockObject()) {
            enforceCanSetDeviceOwnerLocked(admin, userId, hasIncompatibleAccountsOrNonAdb);
            ActiveAdmin activeAdmin = getActiveAdminUncheckedLocked(admin, userId);
            if (activeAdmin == null || getUserData(userId).mRemovingAdmins.contains(admin)) {
                throw new IllegalArgumentException("Not active admin: " + admin);
            }
            toggleBackupServiceActive(0, false);
            if (isAdb()) {
                MetricsLogger.action(this.mContext, 617, LOG_TAG_DEVICE_OWNER);
                DevicePolicyEventLogger.createEvent(82).setAdmin(admin).setStrings(new String[]{LOG_TAG_DEVICE_OWNER}).write();
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
            long ident = this.mInjector.binderClearCallingIdentity();
            try {
                sendOwnerChangedBroadcast("android.app.action.DEVICE_OWNER_CHANGED", userId);
                this.mInjector.binderRestoreCallingIdentity(ident);
                this.mDeviceAdminServiceController.startServiceForOwner(admin.getPackageName(), userId, "set-device-owner");
                if (mHwCustDevicePolicyManagerService != null) {
                    mHwCustDevicePolicyManagerService.setDeviceOwnerEx(this.mContext, admin);
                }
                Slog.i(LOG_TAG, "Device owner set: " + admin + " on user " + userId);
            } catch (Throwable th) {
                this.mInjector.binderRestoreCallingIdentity(ident);
                throw th;
            }
        }
        return true;
    }

    public boolean hasDeviceOwner() {
        enforceDeviceOwnerOrManageUsers();
        return this.mOwners.hasDeviceOwner();
    }

    /* access modifiers changed from: package-private */
    public boolean isDeviceOwner(ActiveAdmin admin) {
        return isDeviceOwner(admin.info.getComponent(), admin.getUserHandle().getIdentifier());
    }

    public boolean isDeviceOwner(ComponentName who, int userId) {
        boolean z;
        synchronized (getLockObject()) {
            z = this.mOwners.hasDeviceOwner() && this.mOwners.getDeviceOwnerUserId() == userId && this.mOwners.getDeviceOwnerComponent().equals(who);
        }
        return z;
    }

    private boolean isDeviceOwnerPackage(String packageName, int userId) {
        boolean z;
        synchronized (getLockObject()) {
            z = this.mOwners.hasDeviceOwner() && this.mOwners.getDeviceOwnerUserId() == userId && this.mOwners.getDeviceOwnerPackageName().equals(packageName);
        }
        return z;
    }

    private boolean isProfileOwnerPackage(String packageName, int userId) {
        boolean z;
        synchronized (getLockObject()) {
            z = this.mOwners.hasProfileOwner(userId) && this.mOwners.getProfileOwnerPackage(userId).equals(packageName);
        }
        return z;
    }

    public boolean isProfileOwner(ComponentName who, int userId) {
        return who != null && who.equals(getProfileOwner(userId));
    }

    private boolean hasProfileOwner(int userId) {
        boolean hasProfileOwner;
        synchronized (getLockObject()) {
            hasProfileOwner = this.mOwners.hasProfileOwner(userId);
        }
        return hasProfileOwner;
    }

    private boolean canProfileOwnerAccessDeviceIds(int userId) {
        boolean canProfileOwnerAccessDeviceIds;
        synchronized (getLockObject()) {
            canProfileOwnerAccessDeviceIds = this.mOwners.canProfileOwnerAccessDeviceIds(userId);
        }
        return canProfileOwnerAccessDeviceIds;
    }

    public ComponentName getDeviceOwnerComponent(boolean callingUserOnly) {
        if (!this.mHasFeature) {
            return null;
        }
        if (!callingUserOnly) {
            enforceManageUsers();
        }
        synchronized (getLockObject()) {
            if (!this.mOwners.hasDeviceOwner()) {
                return null;
            }
            if (callingUserOnly && this.mInjector.userHandleGetCallingUserId() != this.mOwners.getDeviceOwnerUserId()) {
                return null;
            }
            return this.mOwners.getDeviceOwnerComponent();
        }
    }

    public int getDeviceOwnerUserId() {
        int i = -10000;
        if (!this.mHasFeature) {
            return -10000;
        }
        enforceManageUsers();
        synchronized (getLockObject()) {
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
        synchronized (getLockObject()) {
            if (!this.mOwners.hasDeviceOwner()) {
                return null;
            }
            return getApplicationLabel(this.mOwners.getDeviceOwnerPackageName(), 0);
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public ActiveAdmin getDeviceOwnerAdminLocked() {
        ensureLocked();
        ComponentName component = this.mOwners.getDeviceOwnerComponent();
        if (component == null) {
            return null;
        }
        DevicePolicyData policy = getUserData(this.mOwners.getDeviceOwnerUserId());
        int n = policy.mAdminList.size();
        for (int i = 0; i < n; i++) {
            ActiveAdmin admin = policy.mAdminList.get(i);
            if (component.equals(admin.info.getComponent())) {
                return admin;
            }
        }
        Slog.wtf(LOG_TAG, "Active admin for device owner not found. component=" + component);
        return null;
    }

    /* JADX INFO: finally extract failed */
    public void clearDeviceOwner(String packageName) {
        Preconditions.checkNotNull(packageName, "packageName is null");
        int callingUid = this.mInjector.binderGetCallingUid();
        try {
            int uid = this.mInjector.getPackageManager().getPackageUidAsUser(packageName, UserHandle.getUserId(callingUid));
            synchronized (this) {
                Slog.w(LOG_TAG, "clearDeviceOwner packageName=" + packageName + ",callingUid=" + callingUid);
                if (!(uid == callingUid || this.mHwDevicePolicyManagerService == null)) {
                    if (!this.mHwDevicePolicyManagerService.isMdmApiDeviceOwner()) {
                        throw new SecurityException("Invalid packageName");
                    }
                }
            }
            synchronized (getLockObject()) {
                ComponentName deviceOwnerComponent = this.mOwners.getDeviceOwnerComponent();
                int deviceOwnerUserId = this.mOwners.getDeviceOwnerUserId();
                if (!this.mOwners.hasDeviceOwner() || !deviceOwnerComponent.getPackageName().equals(packageName) || deviceOwnerUserId != UserHandle.getUserId(callingUid)) {
                    throw new SecurityException("clearDeviceOwner can only be called by the device owner");
                }
                enforceUserUnlocked(deviceOwnerUserId);
                ActiveAdmin admin = getDeviceOwnerAdminLocked();
                long ident = this.mInjector.binderClearCallingIdentity();
                try {
                    clearDeviceOwnerLocked(admin, deviceOwnerUserId);
                    removeActiveAdminLocked(deviceOwnerComponent, deviceOwnerUserId);
                    sendOwnerChangedBroadcast("android.app.action.DEVICE_OWNER_CHANGED", deviceOwnerUserId);
                    this.mInjector.binderRestoreCallingIdentity(ident);
                    Slog.i(LOG_TAG, "Device owner removed: " + deviceOwnerComponent);
                } catch (Throwable th) {
                    this.mInjector.binderRestoreCallingIdentity(ident);
                    throw th;
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            throw new SecurityException(e);
        }
    }

    private void clearOverrideApnUnchecked() {
        setOverrideApnsEnabledUnchecked(false);
        List<ApnSetting> apns = getOverrideApnsUnchecked();
        for (int i = 0; i < apns.size(); i++) {
            removeOverrideApnUnchecked(apns.get(i).getId());
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
        clearOverrideApnUnchecked();
        this.mOwners.clearDeviceOwner();
        this.mOwners.writeDeviceOwner();
        updateDeviceOwnerLocked();
        clearDeviceOwnerUserRestrictionLocked(UserHandle.of(userId));
        this.mInjector.securityLogSetLoggingEnabledProperty(false);
        this.mSecurityLogMonitor.stop();
        setNetworkLoggingActiveInternal(false);
        deleteTransferOwnershipBundleLocked(userId);
        try {
            if (this.mInjector.getIBackupManager() != null) {
                this.mInjector.getIBackupManager().setBackupServiceActive(0, true);
            }
        } catch (RemoteException e) {
            throw new IllegalStateException("Failed reactivating backup service.", e);
        }
    }

    /* JADX INFO: finally extract failed */
    public boolean setProfileOwner(ComponentName who, String ownerName, int userHandle) {
        if (!this.mHasFeature) {
            return false;
        }
        if (who == null || !isPackageInstalledForUser(who.getPackageName(), userHandle)) {
            throw new IllegalArgumentException("Component " + who + " not installed for userId:" + userHandle);
        }
        boolean hasIncompatibleAccountsOrNonAdb = hasIncompatibleAccountsOrNonAdbNoLock(userHandle, who);
        synchronized (getLockObject()) {
            enforceCanSetProfileOwnerLocked(who, userHandle, hasIncompatibleAccountsOrNonAdb);
            ActiveAdmin admin = getActiveAdminUncheckedLocked(who, userHandle);
            if (admin == null || getUserData(userHandle).mRemovingAdmins.contains(who)) {
                throw new IllegalArgumentException("Not active admin: " + who);
            }
            if (isAdb()) {
                MetricsLogger.action(this.mContext, 617, LOG_TAG_PROFILE_OWNER);
                DevicePolicyEventLogger.createEvent(82).setAdmin(who).setStrings(new String[]{LOG_TAG_PROFILE_OWNER}).write();
            }
            toggleBackupServiceActive(userHandle, false);
            this.mOwners.setProfileOwner(who, ownerName, userHandle);
            this.mOwners.writeProfileOwner(userHandle);
            Slog.i(LOG_TAG, "Profile owner set: " + who + " on user " + userHandle);
            long id = this.mInjector.binderClearCallingIdentity();
            try {
                if (this.mUserManager.isManagedProfile(userHandle)) {
                    maybeSetDefaultRestrictionsForAdminLocked(userHandle, admin, UserRestrictionsUtils.getDefaultEnabledForManagedProfiles());
                    ensureUnknownSourcesRestrictionForProfileOwnerLocked(userHandle, admin, true);
                }
                sendOwnerChangedBroadcast("android.app.action.PROFILE_OWNER_CHANGED", userHandle);
                this.mInjector.binderRestoreCallingIdentity(id);
                this.mDeviceAdminServiceController.startServiceForOwner(who.getPackageName(), userHandle, "set-profile-owner");
                if (mHwCustDevicePolicyManagerService != null) {
                    mHwCustDevicePolicyManagerService.setProfileOwnerEx(this.mContext, who);
                }
            } catch (Throwable th) {
                this.mInjector.binderRestoreCallingIdentity(id);
                throw th;
            }
        }
        return true;
    }

    private void toggleBackupServiceActive(int userId, boolean makeActive) {
        long ident = this.mInjector.binderClearCallingIdentity();
        try {
            if (this.mInjector.getIBackupManager() != null) {
                this.mInjector.getIBackupManager().setBackupServiceActive(userId, makeActive);
            }
            this.mInjector.binderRestoreCallingIdentity(ident);
        } catch (RemoteException e) {
            throw new IllegalStateException("Failed deactivating backup service.", e);
        } catch (Throwable th) {
            this.mInjector.binderRestoreCallingIdentity(ident);
            throw th;
        }
    }

    /* JADX INFO: finally extract failed */
    public void clearProfileOwner(ComponentName who) {
        if (this.mHasFeature) {
            Preconditions.checkNotNull(who, "ComponentName is null");
            int userId = this.mInjector.userHandleGetCallingUserId();
            enforceNotManagedProfile(userId, "clear profile owner");
            enforceUserUnlocked(userId);
            synchronized (getLockObject()) {
                ActiveAdmin admin = getActiveAdminForCallerLocked(who, -1);
                long ident = this.mInjector.binderClearCallingIdentity();
                try {
                    clearProfileOwnerLocked(admin, userId);
                    removeActiveAdminLocked(who, userId);
                    sendOwnerChangedBroadcast("android.app.action.PROFILE_OWNER_CHANGED", userId);
                    this.mInjector.binderRestoreCallingIdentity(ident);
                    Slog.i(LOG_TAG, "Profile owner " + who + " removed from user " + userId);
                } catch (Throwable th) {
                    this.mInjector.binderRestoreCallingIdentity(ident);
                    throw th;
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
        deleteTransferOwnershipBundleLocked(userId);
    }

    public void setDeviceOwnerLockScreenInfo(ComponentName who, CharSequence info) {
        Preconditions.checkNotNull(who, "ComponentName is null");
        if (this.mHasFeature) {
            synchronized (getLockObject()) {
                getActiveAdminForCallerLocked(who, -2);
                long token = this.mInjector.binderClearCallingIdentity();
                try {
                    this.mLockPatternUtils.setDeviceOwnerInfo(info != null ? info.toString() : null);
                } finally {
                    this.mInjector.binderRestoreCallingIdentity(token);
                }
            }
            DevicePolicyEventLogger.createEvent(42).setAdmin(who).write();
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
        updateLockTaskPackagesLocked(policy.mLockTaskPackages, userId);
        policy.mLockTaskFeatures = 0;
        saveSettingsLocked(userId);
        try {
            this.mIPackageManager.updatePermissionFlagsForAllApps(4, 0, userId);
            pushUserRestrictions(userId);
        } catch (RemoteException e) {
            Slog.e(LOG_TAG, "cannot push user restrictions.");
        }
    }

    public boolean hasUserSetupCompleted() {
        return hasUserSetupCompleted(UserHandle.getCallingUserId());
    }

    private boolean hasUserSetupCompleted(int userHandle) {
        if (!this.mHasFeature) {
            return true;
        }
        return getUserData(userHandle).mUserSetupComplete;
    }

    private boolean hasPaired(int userHandle) {
        if (!this.mHasFeature) {
            return true;
        }
        return getUserData(userHandle).mPaired;
    }

    public int getUserProvisioningState() {
        if (!this.mHasFeature) {
            return 0;
        }
        enforceManageUsers();
        return getUserProvisioningState(this.mInjector.userHandleGetCallingUserId());
    }

    private int getUserProvisioningState(int userHandle) {
        return getUserData(userHandle).mUserProvisioningState;
    }

    public void setUserProvisioningState(int newState, int userHandle) {
        if (this.mHasFeature) {
            if (userHandle == this.mOwners.getDeviceOwnerUserId() || this.mOwners.hasProfileOwner(userHandle) || getManagedUserId(userHandle) != -1) {
                synchronized (getLockObject()) {
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
    }

    private void checkUserProvisioningStateTransition(int currentState, int newState) {
        if (currentState != 0) {
            if (currentState == 1 || currentState == 2) {
                if (newState == 3) {
                    return;
                }
            } else if (currentState == 4 && newState == 0) {
                return;
            }
        } else if (newState != 0) {
            return;
        }
        throw new IllegalStateException("Cannot move to user provisioning state [" + newState + "] from state [" + currentState + "]");
    }

    public void setProfileEnabled(ComponentName who) {
        if (this.mHasFeature) {
            Preconditions.checkNotNull(who, "ComponentName is null");
            synchronized (getLockObject()) {
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
                } finally {
                    this.mInjector.binderRestoreCallingIdentity(id);
                }
            }
        }
    }

    public void setProfileName(ComponentName who, String profileName) {
        Preconditions.checkNotNull(who, "ComponentName is null");
        enforceProfileOrDeviceOwner(who);
        int userId = UserHandle.getCallingUserId();
        long id = this.mInjector.binderClearCallingIdentity();
        try {
            this.mUserManager.setUserName(userId, profileName);
            DevicePolicyEventLogger.createEvent(40).setAdmin(who).write();
        } finally {
            this.mInjector.binderRestoreCallingIdentity(id);
        }
    }

    public ComponentName getProfileOwnerAsUser(int userHandle) {
        enforceCrossUsersPermission(userHandle);
        return getProfileOwner(userHandle);
    }

    public ComponentName getProfileOwner(int userHandle) {
        ComponentName profileOwnerComponent;
        if (!this.mHasFeature) {
            return null;
        }
        synchronized (getLockObject()) {
            profileOwnerComponent = this.mOwners.getProfileOwnerComponent(userHandle);
        }
        return profileOwnerComponent;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public ActiveAdmin getProfileOwnerAdminLocked(int userHandle) {
        ComponentName profileOwner = this.mOwners.getProfileOwnerComponent(userHandle);
        if (profileOwner == null) {
            return null;
        }
        DevicePolicyData policy = getUserData(userHandle);
        int n = policy.mAdminList.size();
        for (int i = 0; i < n; i++) {
            ActiveAdmin admin = policy.mAdminList.get(i);
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

    public boolean checkDeviceIdentifierAccess(String packageName, int pid, int uid) {
        int callingUid = this.mInjector.binderGetCallingUid();
        int callingPid = this.mInjector.binderGetCallingPid();
        if (UserHandle.getAppId(callingUid) < 10000 || (callingUid == uid && callingPid == pid)) {
            int userId = UserHandle.getUserId(uid);
            try {
                ApplicationInfo appInfo = this.mIPackageManager.getApplicationInfo(packageName, 0, userId);
                if (appInfo == null) {
                    Log.w(LOG_TAG, String.format("appInfo could not be found for package %s", packageName));
                    return false;
                } else if (uid != appInfo.uid) {
                    String message = String.format("Package %s (uid=%d) does not match provided uid %d", packageName, Integer.valueOf(appInfo.uid), Integer.valueOf(uid));
                    Log.w(LOG_TAG, message);
                    throw new SecurityException(message);
                } else if (this.mContext.checkPermission("android.permission.READ_PHONE_STATE", pid, uid) != 0) {
                    return false;
                } else {
                    ComponentName deviceOwner = getDeviceOwnerComponent(true);
                    if (deviceOwner != null && (deviceOwner.getPackageName().equals(packageName) || isCallerDelegate(packageName, uid, "delegation-cert-install"))) {
                        return true;
                    }
                    ComponentName profileOwner = getProfileOwnerAsUser(userId);
                    if (profileOwner != null && (profileOwner.getPackageName().equals(packageName) || isCallerDelegate(packageName, uid, "delegation-cert-install"))) {
                        return true;
                    }
                    Log.w(LOG_TAG, String.format("Package %s (uid=%d, pid=%d) cannot access Device IDs", packageName, Integer.valueOf(uid), Integer.valueOf(pid)));
                    return false;
                }
            } catch (RemoteException e) {
                Log.e(LOG_TAG, "Exception caught obtaining appInfo for package " + packageName, e);
                return false;
            }
        } else {
            String message2 = String.format("Calling uid %d, pid %d cannot check device identifier access for package %s (uid=%d, pid=%d)", Integer.valueOf(callingUid), Integer.valueOf(callingPid), packageName, Integer.valueOf(uid), Integer.valueOf(pid));
            Log.w(LOG_TAG, message2);
            throw new SecurityException(message2);
        }
    }

    private String getApplicationLabel(String packageName, int userHandle) {
        long token = this.mInjector.binderClearCallingIdentity();
        String str = null;
        try {
            Context userContext = this.mContext.createPackageContextAsUser(packageName, 0, new UserHandle(userHandle));
            try {
                ApplicationInfo appInfo = userContext.getApplicationInfo();
                CharSequence result = null;
                if (appInfo != null) {
                    result = appInfo.loadUnsafeLabel(userContext.getPackageManager());
                }
                if (result != null) {
                    str = result.toString();
                }
                return str;
            } finally {
                this.mInjector.binderRestoreCallingIdentity(token);
            }
        } catch (PackageManager.NameNotFoundException nnfe) {
            Log.w(LOG_TAG, packageName + " is not installed for user " + userHandle, nnfe);
            this.mInjector.binderRestoreCallingIdentity(token);
            return null;
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
            if (!this.mIsWatch && !hasUserSetupCompleted(userHandle)) {
                return;
            }
            if (!isCallerWithSystemUid()) {
                throw new IllegalStateException("Cannot set the profile owner on a user which is already set-up");
            } else if (!this.mIsWatch) {
                String supervisor = this.mContext.getResources().getString(17039828);
                if (supervisor == null) {
                    throw new IllegalStateException("Unable to set profile owner post-setup, nodefault supervisor profile owner defined");
                } else if (!owner.equals(ComponentName.unflattenFromString(supervisor))) {
                    throw new IllegalStateException("Unable to set non-default profile owner post-setup " + owner);
                }
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
        int callingUid = this.mInjector.binderGetCallingUid();
        if (!isCallerWithSystemUid() && callingUid != 0) {
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
        if (!isCallerWithSystemUid() && this.mInjector.binderGetCallingUid() != 0) {
            Context context = this.mContext;
            context.enforceCallingOrSelfPermission(permission, "Must be system or have " + permission + " permission");
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
        synchronized (getLockObject()) {
            if (getActiveAdminWithPolicyForUidLocked(null, -2, this.mInjector.binderGetCallingUid()) == null) {
                enforceManageUsers();
            }
        }
    }

    private void enforceProfileOwnerOrSystemUser() {
        synchronized (getLockObject()) {
            if (getActiveAdminWithPolicyForUidLocked(null, -1, this.mInjector.binderGetCallingUid()) == null) {
                Preconditions.checkState(isCallerWithSystemUid(), "Only profile owner, device owner and system may call this method.");
            }
        }
    }

    private void enforceProfileOwnerOrFullCrossUsersPermission(int userId) {
        if (userId == this.mInjector.userHandleGetCallingUserId()) {
            synchronized (getLockObject()) {
                if (getActiveAdminWithPolicyForUidLocked(null, -1, this.mInjector.binderGetCallingUid()) != null) {
                    return;
                }
            }
        }
        enforceSystemUserOrPermission("android.permission.INTERACT_ACROSS_USERS_FULL");
    }

    private boolean canUserUseLockTaskLocked(int userId) {
        if (isUserAffiliatedWithDeviceLocked(userId)) {
            return true;
        }
        if (!this.mOwners.hasDeviceOwner() && getProfileOwner(userId) != null && !isManagedProfile(userId)) {
            return true;
        }
        return false;
    }

    private void enforceCanCallLockTaskLocked(ComponentName who) {
        getActiveAdminForCallerLocked(who, -1);
        int userId = this.mInjector.userHandleGetCallingUserId();
        if (!canUserUseLockTaskLocked(userId)) {
            throw new SecurityException("User " + userId + " is not allowed to use lock task");
        }
    }

    private void ensureCallerPackage(String packageName) {
        if (packageName == null) {
            Preconditions.checkState(isCallerWithSystemUid(), "Only caller can omit package name");
            return;
        }
        try {
            boolean z = false;
            if (this.mIPackageManager.getApplicationInfo(packageName, 0, this.mInjector.userHandleGetCallingUserId()).uid == this.mInjector.binderGetCallingUid()) {
                z = true;
            }
            Preconditions.checkState(z, "Unmatching package name");
        } catch (RemoteException e) {
            Slog.e(LOG_TAG, "cannot ensure caller package.");
        }
    }

    private boolean isCallerWithSystemUid() {
        return UserHandle.isSameApp(this.mInjector.binderGetCallingUid(), 1000);
    }

    /* access modifiers changed from: protected */
    public int getProfileParentId(int userHandle) {
        long ident = this.mInjector.binderClearCallingIdentity();
        try {
            UserInfo parentUser = this.mUserManager.getProfileParent(userHandle);
            return parentUser != null ? parentUser.id : userHandle;
        } finally {
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
                throw th;
            }
        }
        int credentialOwnerProfile = this.mUserManager.getCredentialOwnerProfile(userHandle);
        this.mInjector.binderRestoreCallingIdentity(ident);
        return credentialOwnerProfile;
    }

    private boolean isManagedProfile(int userHandle) {
        UserInfo user = getUserInfo(userHandle);
        return user != null && user.isManagedProfile();
    }

    private void enableIfNecessary(String packageName, int userId) {
        try {
            if (this.mIPackageManager.getApplicationInfo(packageName, 32768, userId).enabledSetting == 4) {
                this.mIPackageManager.setApplicationEnabledSetting(packageName, 0, 1, userId, LOG_TAG);
            }
        } catch (RemoteException e) {
            Slog.e(LOG_TAG, "cannot enable if necessary.");
        }
    }

    /* access modifiers changed from: protected */
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (DumpUtils.checkDumpPermission(this.mContext, LOG_TAG, pw)) {
            synchronized (getLockObject()) {
                pw.println("Current Device Policy Manager state:");
                this.mOwners.dump("  ", pw);
                this.mDeviceAdminServiceController.dump("  ", pw);
                int userCount = this.mUserData.size();
                for (int u = 0; u < userCount; u++) {
                    DevicePolicyData policy = getUserData(this.mUserData.keyAt(u));
                    pw.println();
                    pw.println("  Enabled Device Admins (User " + policy.mUserHandle + ", provisioningState: " + policy.mUserProvisioningState + "):");
                    pw.println(" ");
                    pw.print("    mPasswordOwner=");
                    pw.println(policy.mPasswordOwner);
                }
                pw.println();
                this.mConstants.dump("  ", pw);
                pw.println();
                this.mStatLogger.dump(pw, "  ");
                pw.println();
                pw.println("  Encryption Status: " + getEncryptionStatusName(getEncryptionStatus()));
                pw.println();
                this.mPolicyCache.dump("  ", pw);
            }
        }
    }

    private String getEncryptionStatusName(int encryptionStatus) {
        if (encryptionStatus == 0) {
            return "unsupported";
        }
        if (encryptionStatus == 1) {
            return "inactive";
        }
        if (encryptionStatus == 2) {
            return "activating";
        }
        if (encryptionStatus == 3) {
            return "block";
        }
        if (encryptionStatus == 4) {
            return "block default key";
        }
        if (encryptionStatus != 5) {
            return UiModeManagerService.Shell.NIGHT_MODE_STR_UNKNOWN;
        }
        return "per-user";
    }

    public void addPersistentPreferredActivity(ComponentName who, IntentFilter filter, ComponentName activity) {
        Injector injector;
        Preconditions.checkNotNull(who, "ComponentName is null");
        int userHandle = UserHandle.getCallingUserId();
        synchronized (getLockObject()) {
            getActiveAdminForCallerLocked(who, -1);
            long id = this.mInjector.binderClearCallingIdentity();
            try {
                this.mIPackageManager.addPersistentPreferredActivity(filter, activity, userHandle);
                this.mIPackageManager.flushPackageRestrictionsAsUser(userHandle);
                injector = this.mInjector;
            } catch (RemoteException e) {
                Slog.e(LOG_TAG, "cannot add persistent preferred activity.");
                injector = this.mInjector;
            } catch (Throwable th) {
                this.mInjector.binderRestoreCallingIdentity(id);
                throw th;
            }
            injector.binderRestoreCallingIdentity(id);
        }
        DevicePolicyEventLogger.createEvent(52).setAdmin(who).setStrings(activity != null ? activity.getPackageName() : null, getIntentFilterActions(filter)).write();
    }

    public void clearPackagePersistentPreferredActivities(ComponentName who, String packageName) {
        Injector injector;
        Preconditions.checkNotNull(who, "ComponentName is null");
        int userHandle = UserHandle.getCallingUserId();
        synchronized (getLockObject()) {
            getActiveAdminForCallerLocked(who, -1);
            long id = this.mInjector.binderClearCallingIdentity();
            try {
                this.mIPackageManager.clearPackagePersistentPreferredActivities(packageName, userHandle);
                this.mIPackageManager.flushPackageRestrictionsAsUser(userHandle);
                injector = this.mInjector;
            } catch (RemoteException e) {
                Slog.e(LOG_TAG, "cannot clear package persistent preferred activities.");
                injector = this.mInjector;
            } catch (Throwable th) {
                this.mInjector.binderRestoreCallingIdentity(id);
                throw th;
            }
            injector.binderRestoreCallingIdentity(id);
        }
    }

    public void setDefaultSmsApplication(ComponentName admin, String packageName) {
        Preconditions.checkNotNull(admin, "ComponentName is null");
        enforceDeviceOwner(admin);
        this.mInjector.binderWithCleanCallingIdentity(new FunctionalUtils.ThrowingRunnable(packageName) {
            /* class com.android.server.devicepolicy.$$Lambda$DevicePolicyManagerService$dDeS1FUetDCbtT673Qp0Hcsm5Vw */
            private final /* synthetic */ String f$1;

            {
                this.f$1 = r2;
            }

            public final void runOrThrow() {
                DevicePolicyManagerService.this.lambda$setDefaultSmsApplication$9$DevicePolicyManagerService(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$setDefaultSmsApplication$9$DevicePolicyManagerService(String packageName) throws Exception {
        SmsApplication.setDefaultApplication(packageName, this.mContext);
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
        if (delegatePackages.size() > 0) {
            return delegatePackages.get(0);
        }
        return null;
    }

    public boolean isCallerApplicationRestrictionsManagingPackage(String callerPackage) {
        return isCallerDelegate(callerPackage, this.mInjector.binderGetCallingUid(), "delegation-app-restrictions");
    }

    public void setApplicationRestrictions(ComponentName who, String callerPackage, String packageName, Bundle settings) {
        enforceCanManageScope(who, callerPackage, -1, "delegation-app-restrictions");
        UserHandle userHandle = this.mInjector.binderGetCallingUserHandle();
        long id = this.mInjector.binderClearCallingIdentity();
        try {
            this.mUserManager.setApplicationRestrictions(packageName, settings, userHandle);
            DevicePolicyEventLogger.createEvent(62).setAdmin(callerPackage).setBoolean(who == null).setStrings(new String[]{packageName}).write();
        } finally {
            this.mInjector.binderRestoreCallingIdentity(id);
        }
    }

    public void setTrustAgentConfiguration(ComponentName admin, ComponentName agent, PersistableBundle args, boolean parent) {
        if (this.mHasFeature && this.mLockPatternUtils.hasSecureLockScreen()) {
            Preconditions.checkNotNull(admin, "admin is null");
            Preconditions.checkNotNull(agent, "agent is null");
            int userHandle = UserHandle.getCallingUserId();
            synchronized (getLockObject()) {
                getActiveAdminForCallerLocked(admin, 9, parent).trustAgentInfos.put(agent.flattenToString(), new ActiveAdmin.TrustAgentInfo(args));
                saveSettingsLocked(userHandle);
            }
        }
    }

    public List<PersistableBundle> getTrustAgentConfiguration(ComponentName admin, ComponentName agent, int userHandle, boolean parent) {
        String componentName;
        if (!this.mHasFeature) {
            return null;
        }
        if (!this.mLockPatternUtils.hasSecureLockScreen()) {
            return null;
        }
        Preconditions.checkNotNull(agent, "agent null");
        enforceFullCrossUsersPermission(userHandle);
        synchronized (getLockObject()) {
            String componentName2 = agent.flattenToString();
            if (admin != null) {
                ActiveAdmin ap = getActiveAdminUncheckedLocked(admin, userHandle, parent);
                if (ap == null) {
                    return null;
                }
                ActiveAdmin.TrustAgentInfo trustAgentInfo = ap.trustAgentInfos.get(componentName2);
                if (trustAgentInfo != null) {
                    if (trustAgentInfo.options != null) {
                        List<PersistableBundle> result = new ArrayList<>();
                        result.add(trustAgentInfo.options);
                        return result;
                    }
                }
                return null;
            }
            List<PersistableBundle> result2 = null;
            List<ActiveAdmin> admins = getActiveAdminsForLockscreenPoliciesLocked(userHandle, parent);
            boolean allAdminsHaveOptions = true;
            int N = admins.size();
            int i = 0;
            while (true) {
                if (i >= N) {
                    break;
                }
                ActiveAdmin active = admins.get(i);
                boolean disablesTrust = (active.disabledKeyguardFeatures & 16) != 0;
                ActiveAdmin.TrustAgentInfo info = active.trustAgentInfos.get(componentName2);
                if (info == null || info.options == null || info.options.isEmpty()) {
                    componentName = componentName2;
                    if (disablesTrust) {
                        allAdminsHaveOptions = false;
                        break;
                    }
                } else if (disablesTrust) {
                    if (result2 == null) {
                        result2 = new ArrayList<>();
                    }
                    result2.add(info.options);
                    componentName = componentName2;
                } else {
                    componentName = componentName2;
                    Log.w(LOG_TAG, "Ignoring admin " + active.info + " because it has trust options but doesn't declare KEYGUARD_DISABLE_TRUST_AGENTS");
                }
                i++;
                componentName2 = componentName;
            }
            return allAdminsHaveOptions ? result2 : null;
        }
    }

    public void setRestrictionsProvider(ComponentName who, ComponentName permissionProvider) {
        Preconditions.checkNotNull(who, "ComponentName is null");
        synchronized (getLockObject()) {
            getActiveAdminForCallerLocked(who, -1);
            int userHandle = UserHandle.getCallingUserId();
            getUserData(userHandle).mRestrictionsProvider = permissionProvider;
            saveSettingsLocked(userHandle);
        }
    }

    public ComponentName getRestrictionsProvider(int userHandle) {
        ComponentName componentName;
        synchronized (getLockObject()) {
            if (isCallerWithSystemUid()) {
                DevicePolicyData userData = getUserData(userHandle);
                componentName = userData != null ? userData.mRestrictionsProvider : null;
            } else {
                throw new SecurityException("Only the system can query the permission provider");
            }
        }
        return componentName;
    }

    public void addCrossProfileIntentFilter(ComponentName who, IntentFilter filter, int flags) {
        Injector injector;
        Preconditions.checkNotNull(who, "ComponentName is null");
        int callingUserId = UserHandle.getCallingUserId();
        synchronized (getLockObject()) {
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
                injector = this.mInjector;
                injector.binderRestoreCallingIdentity(id);
                DevicePolicyEventLogger.createEvent(48).setAdmin(who).setStrings(getIntentFilterActions(filter)).setInt(flags).write();
            } catch (RemoteException e) {
                Slog.e(LOG_TAG, "cannot add crossProfile intent filter.");
                injector = this.mInjector;
            } catch (Throwable th) {
                this.mInjector.binderRestoreCallingIdentity(id);
                throw th;
            }
        }
    }

    private static String[] getIntentFilterActions(IntentFilter filter) {
        if (filter == null) {
            return null;
        }
        int actionsCount = filter.countActions();
        String[] actions = new String[actionsCount];
        for (int i = 0; i < actionsCount; i++) {
            actions[i] = filter.getAction(i);
        }
        return actions;
    }

    public void clearCrossProfileIntentFilters(ComponentName who) {
        UserInfo parent;
        Preconditions.checkNotNull(who, "ComponentName is null");
        int callingUserId = UserHandle.getCallingUserId();
        synchronized (getLockObject()) {
            getActiveAdminForCallerLocked(who, -1);
            long id = this.mInjector.binderClearCallingIdentity();
            try {
                UserInfo parent2 = this.mUserManager.getProfileParent(callingUserId);
                if (parent2 == null) {
                    Slog.e(LOG_TAG, "Cannot call clearCrossProfileIntentFilter if there is no parent");
                    this.mInjector.binderRestoreCallingIdentity(id);
                    return;
                }
                this.mIPackageManager.clearCrossProfileIntentFilters(callingUserId, who.getPackageName());
                this.mIPackageManager.clearCrossProfileIntentFilters(parent2.id, who.getPackageName());
                parent = this.mInjector;
                parent.binderRestoreCallingIdentity(id);
            } catch (RemoteException e) {
                Slog.e(LOG_TAG, "cannot clear crossProfile intent filters.");
                parent = this.mInjector;
            } catch (Throwable th) {
                this.mInjector.binderRestoreCallingIdentity(id);
                throw th;
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
            Iterator<String> it = enabledPackages.iterator();
            while (true) {
                boolean z = true;
                if (it.hasNext()) {
                    String enabledPackage = it.next();
                    boolean systemService = false;
                    try {
                        if ((this.mIPackageManager.getApplicationInfo(enabledPackage, 8192, userIdToCheck).flags & 1) == 0) {
                            z = false;
                        }
                        systemService = z;
                    } catch (RemoteException e) {
                        Log.i(LOG_TAG, "Can't talk to package managed", e);
                    }
                    if (!systemService && !permittedList.contains(enabledPackage)) {
                        return false;
                    }
                } else {
                    this.mInjector.binderRestoreCallingIdentity(id);
                    return true;
                }
            }
        } finally {
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
            long id = this.mInjector.binderClearCallingIdentity();
            try {
                UserInfo user = getUserInfo(userId);
                if (user.isManagedProfile()) {
                    userId = user.profileGroupId;
                }
                List<AccessibilityServiceInfo> enabledServices = getAccessibilityManagerForUser(userId).getEnabledAccessibilityServiceList(-1);
                if (enabledServices != null) {
                    List<String> enabledPackages = new ArrayList<>();
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
        synchronized (getLockObject()) {
            getActiveAdminForCallerLocked(who, -1).permittedAccessiblityServices = packageList;
            saveSettingsLocked(UserHandle.getCallingUserId());
        }
        DevicePolicyEventLogger.createEvent(28).setAdmin(who).setStrings(packageList != null ? (String[]) packageList.toArray(new String[0]) : null).write();
        return true;
    }

    public List getPermittedAccessibilityServices(ComponentName who) {
        List<String> list;
        if (!this.mHasFeature) {
            return null;
        }
        Preconditions.checkNotNull(who, "ComponentName is null");
        synchronized (getLockObject()) {
            list = getActiveAdminForCallerLocked(who, -1).permittedAccessiblityServices;
        }
        return list;
    }

    public List getPermittedAccessibilityServicesForUser(int userId) {
        List<String> result;
        if (!this.mHasFeature) {
            return null;
        }
        enforceManageUsers();
        synchronized (getLockObject()) {
            result = null;
            for (int profileId : this.mUserManager.getProfileIdsWithDisabled(userId)) {
                DevicePolicyData policy = getUserDataUnchecked(profileId);
                int N = policy.mAdminList.size();
                for (int j = 0; j < N; j++) {
                    List<String> fromAdmin = policy.mAdminList.get(j).permittedAccessiblityServices;
                    if (fromAdmin != null) {
                        if (result == null) {
                            result = new ArrayList<>(fromAdmin);
                        } else {
                            result.retainAll(fromAdmin);
                        }
                    }
                }
            }
            if (result != null) {
                long id = this.mInjector.binderClearCallingIdentity();
                try {
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
                } finally {
                    this.mInjector.binderRestoreCallingIdentity(id);
                }
            }
        }
        return result;
    }

    public boolean isAccessibilityServicePermittedByAdmin(ComponentName who, String packageName, int userHandle) {
        if (!this.mHasFeature) {
            return true;
        }
        Preconditions.checkNotNull(who, "ComponentName is null");
        Preconditions.checkStringNotEmpty(packageName, "packageName is null");
        if (isCallerWithSystemUid()) {
            synchronized (getLockObject()) {
                ActiveAdmin admin = getActiveAdminUncheckedLocked(who, userHandle);
                if (admin == null) {
                    return false;
                }
                if (admin.permittedAccessiblityServices == null) {
                    return true;
                }
                return checkPackagesInPermittedListOrSystem(Collections.singletonList(packageName), admin.permittedAccessiblityServices, userHandle);
            }
        }
        throw new SecurityException("Only the system can query if an accessibility service is disabled by admin");
    }

    private boolean checkCallerIsCurrentUserOrProfile() {
        int callingUserId = UserHandle.getCallingUserId();
        long token = this.mInjector.binderClearCallingIdentity();
        try {
            UserInfo callingUser = getUserInfo(callingUserId);
            try {
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
                this.mInjector.binderRestoreCallingIdentity(token);
                return false;
            }
        } finally {
            this.mInjector.binderRestoreCallingIdentity(token);
        }
    }

    public boolean setPermittedInputMethods(ComponentName who, List packageList) {
        List<InputMethodInfo> enabledImes;
        if (!this.mHasFeature) {
            return false;
        }
        Preconditions.checkNotNull(who, "ComponentName is null");
        if (!(InputMethodSystemProperty.PER_PROFILE_IME_ENABLED || checkCallerIsCurrentUserOrProfile())) {
            return false;
        }
        int callingUserId = this.mInjector.userHandleGetCallingUserId();
        if (!(packageList == null || (enabledImes = InputMethodManagerInternal.get().getEnabledInputMethodListAsUser(callingUserId)) == null)) {
            List<String> enabledPackages = new ArrayList<>();
            for (InputMethodInfo ime : enabledImes) {
                enabledPackages.add(ime.getPackageName());
            }
            if (!checkPackagesInPermittedListOrSystem(enabledPackages, packageList, callingUserId)) {
                Slog.e(LOG_TAG, "Cannot set permitted input methods, because it contains already enabled input method.");
                return false;
            }
        }
        synchronized (getLockObject()) {
            getActiveAdminForCallerLocked(who, -1).permittedInputMethods = packageList;
            saveSettingsLocked(callingUserId);
        }
        DevicePolicyEventLogger.createEvent(27).setAdmin(who).setStrings(packageList != null ? (String[]) packageList.toArray(new String[0]) : null).write();
        return true;
    }

    public List getPermittedInputMethods(ComponentName who) {
        List<String> list;
        if (!this.mHasFeature) {
            return null;
        }
        Preconditions.checkNotNull(who, "ComponentName is null");
        synchronized (getLockObject()) {
            list = getActiveAdminForCallerLocked(who, -1).permittedInputMethods;
        }
        return list;
    }

    public List getPermittedInputMethodsForCurrentUser() {
        List<String> result;
        List<InputMethodInfo> imes;
        enforceManageUsers();
        int callingUserId = this.mInjector.userHandleGetCallingUserId();
        synchronized (getLockObject()) {
            result = null;
            for (int profileId : InputMethodSystemProperty.PER_PROFILE_IME_ENABLED ? new int[]{callingUserId} : this.mUserManager.getProfileIdsWithDisabled(callingUserId)) {
                DevicePolicyData policy = getUserDataUnchecked(profileId);
                int N = policy.mAdminList.size();
                for (int j = 0; j < N; j++) {
                    List<String> fromAdmin = policy.mAdminList.get(j).permittedInputMethods;
                    if (fromAdmin != null) {
                        if (result == null) {
                            result = new ArrayList<>(fromAdmin);
                        } else {
                            result.retainAll(fromAdmin);
                        }
                    }
                }
            }
            if (!(result == null || (imes = InputMethodManagerInternal.get().getInputMethodListAsUser(callingUserId)) == null)) {
                for (InputMethodInfo ime : imes) {
                    ServiceInfo serviceInfo = ime.getServiceInfo();
                    if ((serviceInfo.applicationInfo.flags & 1) != 0) {
                        result.add(serviceInfo.packageName);
                    }
                }
            }
        }
        return result;
    }

    public boolean isInputMethodPermittedByAdmin(ComponentName who, String packageName, int userHandle) {
        if (!this.mHasFeature) {
            return true;
        }
        Preconditions.checkNotNull(who, "ComponentName is null");
        Preconditions.checkStringNotEmpty(packageName, "packageName is null");
        if (isCallerWithSystemUid()) {
            synchronized (getLockObject()) {
                ActiveAdmin admin = getActiveAdminUncheckedLocked(who, userHandle);
                if (admin == null) {
                    return false;
                }
                if (admin.permittedInputMethods == null) {
                    return true;
                }
                return checkPackagesInPermittedListOrSystem(Collections.singletonList(packageName), admin.permittedInputMethods, userHandle);
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
        synchronized (getLockObject()) {
            getActiveAdminForCallerLocked(who, -1).permittedNotificationListeners = packageList;
            saveSettingsLocked(callingUserId);
        }
        return true;
    }

    public List<String> getPermittedCrossProfileNotificationListeners(ComponentName who) {
        List<String> list;
        if (!this.mHasFeature) {
            return null;
        }
        Preconditions.checkNotNull(who, "ComponentName is null");
        synchronized (getLockObject()) {
            list = getActiveAdminForCallerLocked(who, -1).permittedNotificationListeners;
        }
        return list;
    }

    public boolean isNotificationListenerServicePermitted(String packageName, int userId) {
        if (!this.mHasFeature) {
            return true;
        }
        Preconditions.checkStringNotEmpty(packageName, "packageName is null or empty");
        if (isCallerWithSystemUid()) {
            synchronized (getLockObject()) {
                ActiveAdmin profileOwner = getProfileOwnerAdminLocked(userId);
                if (profileOwner != null) {
                    if (profileOwner.permittedNotificationListeners != null) {
                        return checkPackagesInPermittedListOrSystem(Collections.singletonList(packageName), profileOwner.permittedNotificationListeners, userId);
                    }
                }
                return true;
            }
        }
        throw new SecurityException("Only the system can query if a notification listener service is permitted");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void maybeSendAdminEnabledBroadcastLocked(int userHandle) {
        DevicePolicyData policyData = getUserData(userHandle);
        if (policyData.mAdminBroadcastPending) {
            ActiveAdmin admin = getProfileOwnerAdminLocked(userHandle);
            boolean clearInitBundle = true;
            if (admin != null) {
                PersistableBundle initBundle = policyData.mInitBundle;
                clearInitBundle = sendAdminCommandLocked(admin, "android.app.action.DEVICE_ADMIN_ENABLED", initBundle == null ? null : new Bundle(initBundle), null, true);
            }
            if (clearInitBundle) {
                policyData.mInitBundle = null;
                policyData.mAdminBroadcastPending = false;
                saveSettingsLocked(userHandle);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:98:0x01c1, code lost:
        r0 = th;
     */
    /* JADX WARNING: Removed duplicated region for block: B:105:0x01d0 A[DONT_GENERATE] */
    /* JADX WARNING: Removed duplicated region for block: B:107:0x01d6  */
    public UserHandle createAndManageUser(ComponentName admin, String name, ComponentName profileOwner, PersistableBundle adminExtras, int flags) {
        Throwable th;
        int targetSdkVersion;
        String[] disallowedPackages;
        Throwable re;
        HwFrameworkFactory.getHwBehaviorCollectManager().sendBehavior(IHwBehaviorCollectManager.BehaviorId.DEVICE_POLICY_CREATEANDMANAGEUSER);
        Preconditions.checkNotNull(admin, "admin is null");
        Preconditions.checkNotNull(profileOwner, "profileOwner is null");
        if (!admin.getPackageName().equals(profileOwner.getPackageName())) {
            throw new IllegalArgumentException("profileOwner " + profileOwner + " and admin " + admin + " are not in the same package");
        } else if (this.mInjector.binderGetCallingUserHandle().isSystem()) {
            boolean ephemeral = (flags & 2) != 0;
            boolean demo = (flags & 4) != 0 && UserManager.isDeviceInDemoMode(this.mContext);
            boolean leaveAllSystemAppsEnabled = (flags & 16) != 0;
            UserHandle user = null;
            synchronized (getLockObject()) {
                try {
                    getActiveAdminForCallerLocked(admin, -2);
                    int callingUid = this.mInjector.binderGetCallingUid();
                    long id = this.mInjector.binderClearCallingIdentity();
                    try {
                        targetSdkVersion = this.mInjector.getPackageManagerInternal().getUidTargetSdkVersion(callingUid);
                        if (((DeviceStorageMonitorInternal) LocalServices.getService(DeviceStorageMonitorInternal.class)).isMemoryLow()) {
                            if (targetSdkVersion < 28) {
                                try {
                                    this.mInjector.binderRestoreCallingIdentity(id);
                                    return null;
                                } catch (Throwable th2) {
                                    th = th2;
                                    throw th;
                                }
                            } else {
                                try {
                                    throw new ServiceSpecificException(5, "low device storage");
                                } catch (Throwable th3) {
                                    th = th3;
                                    this.mInjector.binderRestoreCallingIdentity(id);
                                    throw th;
                                }
                            }
                        } else if (this.mUserManager.canAddMoreUsers()) {
                            int userInfoFlags = 0;
                            if (ephemeral) {
                                userInfoFlags = 0 | 256;
                            }
                            if (demo) {
                                userInfoFlags |= 512;
                            }
                            if (!leaveAllSystemAppsEnabled) {
                                disallowedPackages = (String[]) this.mOverlayPackagesProvider.getNonRequiredApps(admin, UserHandle.myUserId(), "android.app.action.PROVISION_MANAGED_USER").toArray(new String[0]);
                            } else {
                                disallowedPackages = null;
                            }
                            UserInfo userInfo = this.mUserManagerInternal.createUserEvenWhenDisallowed(name, userInfoFlags, disallowedPackages);
                            if (userInfo != null) {
                                user = userInfo.getUserHandle();
                            }
                            this.mInjector.binderRestoreCallingIdentity(id);
                        } else if (targetSdkVersion < 28) {
                            this.mInjector.binderRestoreCallingIdentity(id);
                            return null;
                        } else {
                            throw new ServiceSpecificException(6, "user limit reached");
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        this.mInjector.binderRestoreCallingIdentity(id);
                        throw th;
                    }
                } catch (Throwable th5) {
                    th = th5;
                    throw th;
                }
            }
            if (user != null) {
                int userHandle = user.getIdentifier();
                this.mContext.sendBroadcastAsUser(new Intent("android.app.action.MANAGED_USER_CREATED").putExtra("android.intent.extra.user_handle", userHandle).putExtra("android.app.extra.PROVISIONING_LEAVE_ALL_SYSTEM_APPS_ENABLED", leaveAllSystemAppsEnabled).setPackage(getManagedProvisioningPackage(this.mContext)).addFlags(268435456), UserHandle.SYSTEM);
                long id2 = this.mInjector.binderClearCallingIdentity();
                try {
                    String adminPkg = admin.getPackageName();
                    try {
                        if (!this.mIPackageManager.isPackageAvailable(adminPkg, userHandle)) {
                            this.mIPackageManager.installExistingPackageAsUser(adminPkg, userHandle, (int) DumpState.DUMP_CHANGES, 1, (List) null);
                        }
                    } catch (RemoteException e) {
                        Slog.e(LOG_TAG, "cannot install the profile owner if not present.");
                    } catch (Throwable th6) {
                        re = th6;
                        try {
                            this.mUserManager.removeUser(userHandle);
                            if (targetSdkVersion >= 28) {
                            }
                        } finally {
                            this.mInjector.binderRestoreCallingIdentity(id2);
                        }
                    }
                    setActiveAdmin(profileOwner, true, userHandle);
                    setProfileOwner(profileOwner, getProfileOwnerName(Process.myUserHandle().getIdentifier()), userHandle);
                    synchronized (getLockObject()) {
                        DevicePolicyData policyData = getUserData(userHandle);
                        policyData.mInitBundle = adminExtras;
                        policyData.mAdminBroadcastPending = true;
                        saveSettingsLocked(userHandle);
                    }
                    if ((flags & 1) != 0) {
                        try {
                            Settings.Secure.putIntForUser(this.mContext.getContentResolver(), "user_setup_complete", 1, userHandle);
                        } catch (Throwable th7) {
                            re = th7;
                        }
                    }
                    this.mInjector.binderRestoreCallingIdentity(id2);
                    return user;
                } catch (Throwable th8) {
                    re = th8;
                    this.mUserManager.removeUser(userHandle);
                    if (targetSdkVersion >= 28) {
                        return null;
                    }
                    throw new ServiceSpecificException(1, re.getMessage());
                }
            } else if (targetSdkVersion < 28) {
                return null;
            } else {
                throw new ServiceSpecificException(1, "failed to create user");
            }
        } else {
            throw new SecurityException("createAndManageUser was called from non-system user");
        }
        while (true) {
        }
    }

    public boolean removeUser(ComponentName who, UserHandle userHandle) {
        String restriction;
        Preconditions.checkNotNull(who, "ComponentName is null");
        Preconditions.checkNotNull(userHandle, "UserHandle is null");
        enforceDeviceOwner(who);
        int callingUserId = this.mInjector.userHandleGetCallingUserId();
        long id = this.mInjector.binderClearCallingIdentity();
        try {
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
        int userRestrictionSource = this.mUserManager.getUserRestrictionSource(userRestriction, UserHandle.of(userId));
        if (userRestrictionSource == 0) {
            return false;
        }
        if (userRestrictionSource == 2) {
            return !isDeviceOwner(admin, userId);
        }
        if (userRestrictionSource != 4) {
            return true;
        }
        return !isProfileOwner(admin, userId);
    }

    public boolean switchUser(ComponentName who, UserHandle userHandle) {
        boolean switchUser;
        Preconditions.checkNotNull(who, "ComponentName is null");
        synchronized (getLockObject()) {
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
                    throw th;
                }
            }
            switchUser = this.mInjector.getIActivityManager().switchUser(userId);
            this.mInjector.binderRestoreCallingIdentity(id);
        }
        return switchUser;
    }

    public int startUserInBackground(ComponentName who, UserHandle userHandle) {
        Preconditions.checkNotNull(who, "ComponentName is null");
        Preconditions.checkNotNull(userHandle, "UserHandle is null");
        enforceDeviceOwner(who);
        int userId = userHandle.getIdentifier();
        if (isManagedProfile(userId)) {
            Log.w(LOG_TAG, "Managed profile cannot be started in background");
            return 2;
        }
        long id = this.mInjector.binderClearCallingIdentity();
        try {
            if (!this.mInjector.getActivityManagerInternal().canStartMoreUsers()) {
                Log.w(LOG_TAG, "Cannot start more users in background");
                return 3;
            } else if (this.mInjector.getIActivityManager().startUserInBackground(userId)) {
                this.mInjector.binderRestoreCallingIdentity(id);
                return 0;
            } else {
                this.mInjector.binderRestoreCallingIdentity(id);
                return 1;
            }
        } catch (RemoteException e) {
            return 1;
        } finally {
            this.mInjector.binderRestoreCallingIdentity(id);
        }
    }

    public int stopUser(ComponentName who, UserHandle userHandle) {
        Preconditions.checkNotNull(who, "ComponentName is null");
        Preconditions.checkNotNull(userHandle, "UserHandle is null");
        enforceDeviceOwner(who);
        int userId = userHandle.getIdentifier();
        if (!isManagedProfile(userId)) {
            return stopUserUnchecked(userId);
        }
        Log.w(LOG_TAG, "Managed profile cannot be stopped");
        return 2;
    }

    public int logoutUser(ComponentName who) {
        Preconditions.checkNotNull(who, "ComponentName is null");
        int callingUserId = this.mInjector.userHandleGetCallingUserId();
        synchronized (getLockObject()) {
            getActiveAdminForCallerLocked(who, -1);
            if (!isUserAffiliatedWithDeviceLocked(callingUserId)) {
                throw new SecurityException("Admin " + who + " is neither the device owner or affiliated user's profile owner.");
            }
        }
        if (isManagedProfile(callingUserId)) {
            Log.w(LOG_TAG, "Managed profile cannot be logout");
            return 2;
        }
        long id = this.mInjector.binderClearCallingIdentity();
        try {
            if (!this.mInjector.getIActivityManager().switchUser(0)) {
                Log.w(LOG_TAG, "Failed to switch to primary user");
                return 1;
            }
            this.mInjector.binderRestoreCallingIdentity(id);
            return stopUserUnchecked(callingUserId);
        } catch (RemoteException e) {
            return 1;
        } finally {
            this.mInjector.binderRestoreCallingIdentity(id);
        }
    }

    private int stopUserUnchecked(int userId) {
        long id = this.mInjector.binderClearCallingIdentity();
        try {
            int stopUser = this.mInjector.getIActivityManager().stopUser(userId, true, (IStopUserCallback) null);
            if (stopUser == -2) {
                this.mInjector.binderRestoreCallingIdentity(id);
                return 4;
            } else if (stopUser != 0) {
                return 1;
            } else {
                this.mInjector.binderRestoreCallingIdentity(id);
                return 0;
            }
        } catch (RemoteException e) {
            return 1;
        } finally {
            this.mInjector.binderRestoreCallingIdentity(id);
        }
    }

    public List<UserHandle> getSecondaryUsers(ComponentName who) {
        Preconditions.checkNotNull(who, "ComponentName is null");
        enforceDeviceOwner(who);
        long id = this.mInjector.binderClearCallingIdentity();
        try {
            List<UserInfo> userInfos = this.mInjector.getUserManager().getUsers(true);
            List<UserHandle> userHandles = new ArrayList<>();
            for (UserInfo userInfo : userInfos) {
                UserHandle userHandle = userInfo.getUserHandle();
                if (!userHandle.isSystem() && !isManagedProfile(userHandle.getIdentifier())) {
                    userHandles.add(userInfo.getUserHandle());
                }
            }
            return userHandles;
        } finally {
            this.mInjector.binderRestoreCallingIdentity(id);
        }
    }

    public boolean isEphemeralUser(ComponentName who) {
        Preconditions.checkNotNull(who, "ComponentName is null");
        enforceProfileOrDeviceOwner(who);
        int callingUserId = this.mInjector.userHandleGetCallingUserId();
        long id = this.mInjector.binderClearCallingIdentity();
        try {
            return this.mInjector.getUserManager().isUserEphemeral(callingUserId);
        } finally {
            this.mInjector.binderRestoreCallingIdentity(id);
        }
    }

    public Bundle getApplicationRestrictions(ComponentName who, String callerPackage, String packageName) {
        enforceCanManageScope(who, callerPackage, -1, "delegation-app-restrictions");
        UserHandle userHandle = this.mInjector.binderGetCallingUserHandle();
        long id = this.mInjector.binderClearCallingIdentity();
        try {
            Bundle bundle = this.mUserManager.getApplicationRestrictions(packageName, userHandle);
            return bundle != null ? bundle : Bundle.EMPTY;
        } finally {
            this.mInjector.binderRestoreCallingIdentity(id);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:26:0x0059  */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x005b  */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x0073 A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x0074 A[RETURN] */
    public String[] setPackagesSuspended(ComponentName who, String callerPackage, String[] packageNames, boolean suspended) {
        long id;
        Throwable th;
        RemoteException re;
        int callingUserId = UserHandle.getCallingUserId();
        String[] result = null;
        synchronized (getLockObject()) {
            enforceCanManageScope(who, callerPackage, -1, "delegation-package-access");
            long id2 = this.mInjector.binderClearCallingIdentity();
            try {
                try {
                    result = this.mIPackageManager.setPackagesSuspendedAsUser(packageNames, suspended, (PersistableBundle) null, (PersistableBundle) null, (SuspendDialogInfo) null, PackageManagerService.PLATFORM_PACKAGE_NAME, callingUserId);
                    this.mInjector.binderRestoreCallingIdentity(id2);
                } catch (RemoteException e) {
                    re = e;
                    id = id2;
                    try {
                        Slog.e(LOG_TAG, "Failed talking to the package manager", re);
                        this.mInjector.binderRestoreCallingIdentity(id);
                        DevicePolicyEventLogger.createEvent(68).setAdmin(callerPackage).setBoolean(who == null).setStrings(packageNames).write();
                        if (result != null) {
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        this.mInjector.binderRestoreCallingIdentity(id);
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    id = id2;
                    this.mInjector.binderRestoreCallingIdentity(id);
                    throw th;
                }
            } catch (RemoteException e2) {
                re = e2;
                id = id2;
                Slog.e(LOG_TAG, "Failed talking to the package manager", re);
                this.mInjector.binderRestoreCallingIdentity(id);
                DevicePolicyEventLogger.createEvent(68).setAdmin(callerPackage).setBoolean(who == null).setStrings(packageNames).write();
                if (result != null) {
                }
            } catch (Throwable th4) {
                th = th4;
                id = id2;
                this.mInjector.binderRestoreCallingIdentity(id);
                throw th;
            }
        }
        DevicePolicyEventLogger.createEvent(68).setAdmin(callerPackage).setBoolean(who == null).setStrings(packageNames).write();
        if (result != null) {
            return result;
        }
        return packageNames;
    }

    /* JADX INFO: finally extract failed */
    public boolean isPackageSuspended(ComponentName who, String callerPackage, String packageName) {
        boolean isPackageSuspendedForUser;
        int callingUserId = UserHandle.getCallingUserId();
        synchronized (getLockObject()) {
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
                throw th;
            }
        }
        return isPackageSuspendedForUser;
    }

    public void setUserRestriction(ComponentName who, String key, boolean enabledFromThisOwner) {
        int eventId;
        int eventTag;
        Preconditions.checkNotNull(who, "ComponentName is null");
        if (UserRestrictionsUtils.isValidRestriction(key)) {
            int userHandle = this.mInjector.userHandleGetCallingUserId();
            synchronized (getLockObject()) {
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
            if (enabledFromThisOwner) {
                eventId = 12;
            } else {
                eventId = 13;
            }
            DevicePolicyEventLogger.createEvent(eventId).setAdmin(who).setStrings(new String[]{key}).write();
            if (SecurityLog.isLoggingEnabled()) {
                if (enabledFromThisOwner) {
                    eventTag = 210027;
                } else {
                    eventTag = 210028;
                }
                SecurityLog.writeEvent(eventTag, new Object[]{who.getPackageName(), Integer.valueOf(userHandle), key});
                Log.i(MDPP_TAG, "TAG " + eventTag);
            }
        }
    }

    private void saveUserRestrictionsLocked(int userId) {
        saveSettingsLocked(userId);
        pushUserRestrictions(userId);
        sendChangedNotification(userId);
    }

    private void pushUserRestrictions(int userId) {
        Bundle userRestrictions;
        synchronized (getLockObject()) {
            boolean isDeviceOwner = this.mOwners.isDeviceOwnerUserId(userId);
            boolean disallowCameraGlobally = false;
            if (isDeviceOwner) {
                ActiveAdmin deviceOwner = getDeviceOwnerAdminLocked();
                if (deviceOwner != null) {
                    userRestrictions = deviceOwner.userRestrictions;
                    disallowCameraGlobally = deviceOwner.disableCamera;
                } else {
                    return;
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
        Bundle bundle;
        if (!this.mHasFeature) {
            return null;
        }
        Preconditions.checkNotNull(who, "ComponentName is null");
        synchronized (getLockObject()) {
            bundle = getActiveAdminForCallerLocked(who, -1).userRestrictions;
        }
        return bundle;
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0055: APUT  (r6v3 java.lang.String[]), (1 ??[boolean, int, float, short, byte, char]), (r3v5 java.lang.String) */
    public boolean setApplicationHidden(ComponentName who, String callerPackage, String packageName, boolean hidden) {
        Injector injector;
        int callingUserId = UserHandle.getCallingUserId();
        boolean result = false;
        synchronized (getLockObject()) {
            enforceCanManageScope(who, callerPackage, -1, "delegation-package-access");
            long id = this.mInjector.binderClearCallingIdentity();
            try {
                result = this.mIPackageManager.setApplicationHiddenSettingAsUser(packageName, hidden, callingUserId);
                injector = this.mInjector;
            } catch (RemoteException re) {
                Slog.e(LOG_TAG, "Failed to setApplicationHiddenSetting", re);
                injector = this.mInjector;
            } catch (Throwable th) {
                this.mInjector.binderRestoreCallingIdentity(id);
                throw th;
            }
            injector.binderRestoreCallingIdentity(id);
        }
        DevicePolicyEventLogger devicePolicyEventLogger = DevicePolicyEventLogger.createEvent(63).setAdmin(callerPackage).setBoolean(who == null);
        String[] strArr = new String[2];
        strArr[0] = packageName;
        strArr[1] = hidden ? "hidden" : "not_hidden";
        devicePolicyEventLogger.setStrings(strArr).write();
        return result;
    }

    /* JADX INFO: finally extract failed */
    public boolean isApplicationHidden(ComponentName who, String callerPackage, String packageName) {
        boolean applicationHiddenSettingAsUser;
        int callingUserId = UserHandle.getCallingUserId();
        synchronized (getLockObject()) {
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
                throw th;
            }
        }
        return applicationHiddenSettingAsUser;
    }

    public void enableSystemApp(ComponentName who, String callerPackage, String packageName) {
        Injector injector;
        synchronized (getLockObject()) {
            enforceCanManageScope(who, callerPackage, -1, "delegation-enable-system-app");
            boolean isDemo = isCurrentUserDemo();
            int userId = UserHandle.getCallingUserId();
            long id = this.mInjector.binderClearCallingIdentity();
            try {
                int parentUserId = getProfileParentId(userId);
                if (!isDemo) {
                    if (!isSystemApp(this.mIPackageManager, packageName, parentUserId)) {
                        throw new IllegalArgumentException("Only system apps can be enabled this way.");
                    }
                }
                this.mIPackageManager.installExistingPackageAsUser(packageName, userId, (int) DumpState.DUMP_CHANGES, 1, (List) null);
                if (isDemo) {
                    this.mIPackageManager.setApplicationEnabledSetting(packageName, 1, 1, userId, LOG_TAG);
                }
                injector = this.mInjector;
            } catch (RemoteException re) {
                Slog.wtf(LOG_TAG, "Failed to install " + packageName, re);
                injector = this.mInjector;
            } catch (Throwable th) {
                this.mInjector.binderRestoreCallingIdentity(id);
                throw th;
            }
            injector.binderRestoreCallingIdentity(id);
        }
        DevicePolicyEventLogger.createEvent(64).setAdmin(callerPackage).setBoolean(who == null).setStrings(new String[]{packageName}).write();
    }

    public int enableSystemAppWithIntent(ComponentName who, String callerPackage, Intent intent) {
        int numberOfAppsInstalled = 0;
        synchronized (getLockObject()) {
            enforceCanManageScope(who, callerPackage, -1, "delegation-enable-system-app");
            int userId = UserHandle.getCallingUserId();
            long id = this.mInjector.binderClearCallingIdentity();
            try {
                int parentUserId = getProfileParentId(userId);
                List<ResolveInfo> activitiesToEnable = this.mIPackageManager.queryIntentActivities(intent, intent.resolveTypeIfNeeded(this.mContext.getContentResolver()), 786432, parentUserId).getList();
                if (activitiesToEnable != null) {
                    for (ResolveInfo info : activitiesToEnable) {
                        if (info.activityInfo != null) {
                            String packageName = info.activityInfo.packageName;
                            if (isSystemApp(this.mIPackageManager, packageName, parentUserId)) {
                                numberOfAppsInstalled++;
                                this.mIPackageManager.installExistingPackageAsUser(packageName, userId, (int) DumpState.DUMP_CHANGES, 1, (List) null);
                            } else {
                                Slog.d(LOG_TAG, "Not enabling " + packageName + " since is not a system app");
                            }
                        }
                    }
                }
            } catch (RemoteException e) {
                Slog.wtf(LOG_TAG, "Failed to resolve intent for: " + intent);
                return 0;
            } finally {
                this.mInjector.binderRestoreCallingIdentity(id);
            }
        }
        DevicePolicyEventLogger.createEvent(65).setAdmin(callerPackage).setBoolean(who == null).setStrings(new String[]{intent.getAction()}).write();
        return numberOfAppsInstalled;
    }

    private boolean isSystemApp(IPackageManager pm, String packageName, int userId) throws RemoteException {
        ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 8192, userId);
        if (appInfo != null) {
            return (appInfo.flags & 1) != 0;
        }
        throw new IllegalArgumentException("The application " + packageName + " is not present on this device");
    }

    public boolean installExistingPackage(ComponentName who, String callerPackage, String packageName) {
        boolean result;
        synchronized (getLockObject()) {
            enforceCanManageScope(who, callerPackage, -1, "delegation-install-existing-package");
            int callingUserId = this.mInjector.userHandleGetCallingUserId();
            if (isUserAffiliatedWithDeviceLocked(callingUserId)) {
                long id = this.mInjector.binderClearCallingIdentity();
                try {
                    result = this.mIPackageManager.installExistingPackageAsUser(packageName, callingUserId, DumpState.DUMP_CHANGES, 1, null) == 1;
                } catch (RemoteException e) {
                    return false;
                } finally {
                    this.mInjector.binderRestoreCallingIdentity(id);
                }
            } else {
                throw new SecurityException("Admin " + who + " is neither the device owner or affiliated user's profile owner.");
            }
        }
        if (result) {
            DevicePolicyEventLogger.createEvent(66).setAdmin(callerPackage).setBoolean(who == null).setStrings(new String[]{packageName}).write();
        }
        return result;
    }

    public void setAccountManagementDisabled(ComponentName who, String accountType, boolean disabled) {
        if (this.mHasFeature) {
            Preconditions.checkNotNull(who, "ComponentName is null");
            synchronized (getLockObject()) {
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
        String[] strArr;
        enforceFullCrossUsersPermission(userId);
        if (!this.mHasFeature) {
            return null;
        }
        synchronized (getLockObject()) {
            DevicePolicyData policy = getUserData(userId);
            int N = policy.mAdminList.size();
            ArraySet<String> resultSet = new ArraySet<>();
            for (int i = 0; i < N; i++) {
                resultSet.addAll(policy.mAdminList.get(i).accountTypesWithManagementDisabled);
            }
            strArr = (String[]) resultSet.toArray(new String[resultSet.size()]);
        }
        return strArr;
    }

    public void setUninstallBlocked(ComponentName who, String callerPackage, String packageName, boolean uninstallBlocked) {
        Injector injector;
        int i;
        int userId = UserHandle.getCallingUserId();
        int pid = Process.myPid();
        Integer eventTag = null;
        synchronized (getLockObject()) {
            enforceCanManageScope(who, callerPackage, -1, "delegation-block-uninstall");
            long id = this.mInjector.binderClearCallingIdentity();
            try {
                this.mIPackageManager.setBlockUninstallForUser(packageName, uninstallBlocked, userId);
                if (SecurityLog.isLoggingEnabled()) {
                    if (uninstallBlocked) {
                        i = 210037;
                    } else {
                        i = 210036;
                    }
                    eventTag = Integer.valueOf(i);
                }
                injector = this.mInjector;
            } catch (RemoteException re) {
                if (0 != 0) {
                    SecurityLog.writeEvent(eventTag.intValue(), new Object[]{Integer.valueOf(pid), 0});
                    Log.i(MDPP_TAG, "TAG " + ((Object) null) + " pid " + pid + " result 0");
                }
                Slog.e(LOG_TAG, "Failed to setBlockUninstallForUser", re);
                injector = this.mInjector;
            } catch (Throwable th) {
                this.mInjector.binderRestoreCallingIdentity(id);
                throw th;
            }
            injector.binderRestoreCallingIdentity(id);
        }
        DevicePolicyEventLogger.createEvent(67).setAdmin(callerPackage).setBoolean(who == null).setStrings(new String[]{packageName}).write();
        if (eventTag != null) {
            SecurityLog.writeEvent(eventTag.intValue(), new Object[]{Integer.valueOf(pid), 1});
            Log.i(MDPP_TAG, "TAG " + eventTag + " pid " + pid + " result 1");
        }
    }

    public boolean isUninstallBlocked(ComponentName who, String packageName) {
        boolean blockUninstallForUser;
        int userId = UserHandle.getCallingUserId();
        synchronized (getLockObject()) {
            if (who != null) {
                getActiveAdminForCallerLocked(who, -1);
            }
            long id = this.mInjector.binderClearCallingIdentity();
            try {
                blockUninstallForUser = this.mIPackageManager.getBlockUninstallForUser(packageName, userId);
            } catch (RemoteException re) {
                Slog.e(LOG_TAG, "Failed to getBlockUninstallForUser", re);
                return false;
            } finally {
                this.mInjector.binderRestoreCallingIdentity(id);
            }
        }
        return blockUninstallForUser;
    }

    public void setCrossProfileCallerIdDisabled(ComponentName who, boolean disabled) {
        if (this.mHasFeature) {
            Preconditions.checkNotNull(who, "ComponentName is null");
            synchronized (getLockObject()) {
                ActiveAdmin admin = getActiveAdminForCallerLocked(who, -1);
                if (admin.disableCallerId != disabled) {
                    admin.disableCallerId = disabled;
                    saveSettingsLocked(this.mInjector.userHandleGetCallingUserId());
                }
            }
            DevicePolicyEventLogger.createEvent(46).setAdmin(who).setBoolean(disabled).write();
        }
    }

    public boolean getCrossProfileCallerIdDisabled(ComponentName who) {
        boolean z;
        if (!this.mHasFeature) {
            return false;
        }
        Preconditions.checkNotNull(who, "ComponentName is null");
        synchronized (getLockObject()) {
            z = getActiveAdminForCallerLocked(who, -1).disableCallerId;
        }
        return z;
    }

    public boolean getCrossProfileCallerIdDisabledForUser(int userId) {
        boolean z;
        enforceCrossUsersPermission(userId);
        synchronized (getLockObject()) {
            ActiveAdmin admin = getProfileOwnerAdminLocked(userId);
            z = admin != null ? admin.disableCallerId : false;
        }
        return z;
    }

    public void setCrossProfileContactsSearchDisabled(ComponentName who, boolean disabled) {
        if (this.mHasFeature) {
            Preconditions.checkNotNull(who, "ComponentName is null");
            synchronized (getLockObject()) {
                ActiveAdmin admin = getActiveAdminForCallerLocked(who, -1);
                if (admin.disableContactsSearch != disabled) {
                    admin.disableContactsSearch = disabled;
                    saveSettingsLocked(this.mInjector.userHandleGetCallingUserId());
                }
            }
            DevicePolicyEventLogger.createEvent(45).setAdmin(who).setBoolean(disabled).write();
        }
    }

    public boolean getCrossProfileContactsSearchDisabled(ComponentName who) {
        boolean z;
        if (!this.mHasFeature) {
            return false;
        }
        Preconditions.checkNotNull(who, "ComponentName is null");
        synchronized (getLockObject()) {
            z = getActiveAdminForCallerLocked(who, -1).disableContactsSearch;
        }
        return z;
    }

    public boolean getCrossProfileContactsSearchDisabledForUser(int userId) {
        boolean z;
        enforceCrossUsersPermission(userId);
        synchronized (getLockObject()) {
            ActiveAdmin admin = getProfileOwnerAdminLocked(userId);
            z = admin != null ? admin.disableContactsSearch : false;
        }
        return z;
    }

    public void startManagedQuickContact(String actualLookupKey, long actualContactId, boolean isContactIdIgnored, long actualDirectoryId, Intent originalIntent) {
        Intent intent = ContactsContract.QuickContact.rebuildManagedQuickContactsIntent(actualLookupKey, actualContactId, isContactIdIgnored, actualDirectoryId, originalIntent);
        int callingUserId = UserHandle.getCallingUserId();
        long ident = this.mInjector.binderClearCallingIdentity();
        try {
            synchronized (getLockObject()) {
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
        return getCrossProfileCallerIdDisabledForUser(userId) && getCrossProfileContactsSearchDisabledForUser(userId);
    }

    public int getManagedUserId(int callingUserId) {
        for (UserInfo ui : this.mUserManager.getProfiles(callingUserId)) {
            if (ui.id != callingUserId && ui.isManagedProfile()) {
                return ui.id;
            }
        }
        return -1;
    }

    public void setBluetoothContactSharingDisabled(ComponentName who, boolean disabled) {
        if (this.mHasFeature) {
            Preconditions.checkNotNull(who, "ComponentName is null");
            synchronized (getLockObject()) {
                ActiveAdmin admin = getActiveAdminForCallerLocked(who, -1);
                if (admin.disableBluetoothContactSharing != disabled) {
                    admin.disableBluetoothContactSharing = disabled;
                    saveSettingsLocked(UserHandle.getCallingUserId());
                }
            }
            DevicePolicyEventLogger.createEvent(47).setAdmin(who).setBoolean(disabled).write();
        }
    }

    public boolean getBluetoothContactSharingDisabled(ComponentName who) {
        boolean z;
        if (!this.mHasFeature) {
            return false;
        }
        Preconditions.checkNotNull(who, "ComponentName is null");
        synchronized (getLockObject()) {
            z = getActiveAdminForCallerLocked(who, -1).disableBluetoothContactSharing;
        }
        return z;
    }

    public boolean getBluetoothContactSharingDisabledForUser(int userId) {
        boolean z;
        synchronized (getLockObject()) {
            ActiveAdmin admin = getProfileOwnerAdminLocked(userId);
            z = admin != null ? admin.disableBluetoothContactSharing : false;
        }
        return z;
    }

    public void setLockTaskPackages(ComponentName who, String[] packages) throws SecurityException {
        Preconditions.checkNotNull(who, "ComponentName is null");
        Preconditions.checkNotNull(packages, "packages is null");
        synchronized (getLockObject()) {
            enforceCanCallLockTaskLocked(who);
            setLockTaskPackagesLocked(this.mInjector.userHandleGetCallingUserId(), new ArrayList(Arrays.asList(packages)));
        }
    }

    private void setLockTaskPackagesLocked(int userHandle, List<String> packages) {
        getUserData(userHandle).mLockTaskPackages = packages;
        saveSettingsLocked(userHandle);
        updateLockTaskPackagesLocked(packages, userHandle);
    }

    public String[] getLockTaskPackages(ComponentName who) {
        String[] strArr;
        Preconditions.checkNotNull(who, "ComponentName is null");
        int userHandle = this.mInjector.binderGetCallingUserHandle().getIdentifier();
        synchronized (getLockObject()) {
            enforceCanCallLockTaskLocked(who);
            List<String> packages = getUserData(userHandle).mLockTaskPackages;
            strArr = (String[]) packages.toArray(new String[packages.size()]);
        }
        return strArr;
    }

    public boolean isLockTaskPermitted(String pkg) {
        boolean contains;
        int userHandle = this.mInjector.userHandleGetCallingUserId();
        synchronized (getLockObject()) {
            contains = getUserData(userHandle).mLockTaskPackages.contains(pkg);
        }
        return contains;
    }

    public void setLockTaskFeatures(ComponentName who, int flags) {
        Preconditions.checkNotNull(who, "ComponentName is null");
        boolean z = true;
        boolean hasHome = (flags & 4) != 0;
        Preconditions.checkArgument(hasHome || !((flags & 8) != 0), "Cannot use LOCK_TASK_FEATURE_OVERVIEW without LOCK_TASK_FEATURE_HOME");
        boolean hasNotification = (flags & 2) != 0;
        if (!hasHome && hasNotification) {
            z = false;
        }
        Preconditions.checkArgument(z, "Cannot use LOCK_TASK_FEATURE_NOTIFICATIONS without LOCK_TASK_FEATURE_HOME");
        int userHandle = this.mInjector.userHandleGetCallingUserId();
        synchronized (getLockObject()) {
            enforceCanCallLockTaskLocked(who);
            setLockTaskFeaturesLocked(userHandle, flags);
        }
    }

    private void setLockTaskFeaturesLocked(int userHandle, int flags) {
        getUserData(userHandle).mLockTaskFeatures = flags;
        saveSettingsLocked(userHandle);
        updateLockTaskFeaturesLocked(flags, userHandle);
    }

    public int getLockTaskFeatures(ComponentName who) {
        int i;
        Preconditions.checkNotNull(who, "ComponentName is null");
        int userHandle = this.mInjector.userHandleGetCallingUserId();
        synchronized (getLockObject()) {
            enforceCanCallLockTaskLocked(who);
            i = getUserData(userHandle).mLockTaskFeatures;
        }
        return i;
    }

    private void maybeClearLockTaskPolicyLocked() {
        long ident = this.mInjector.binderClearCallingIdentity();
        try {
            List<UserInfo> userInfos = this.mUserManager.getUsers(true);
            for (int i = userInfos.size() - 1; i >= 0; i--) {
                int userId = userInfos.get(i).id;
                if (!canUserUseLockTaskLocked(userId)) {
                    if (!getUserData(userId).mLockTaskPackages.isEmpty()) {
                        Slog.d(LOG_TAG, "User id " + userId + " not affiliated. Clearing lock task packages");
                        setLockTaskPackagesLocked(userId, Collections.emptyList());
                    }
                    if (getUserData(userId).mLockTaskFeatures != 0) {
                        Slog.d(LOG_TAG, "User id " + userId + " not affiliated. Clearing lock task features");
                        setLockTaskFeaturesLocked(userId, 0);
                    }
                }
            }
        } finally {
            this.mInjector.binderRestoreCallingIdentity(ident);
        }
    }

    public void notifyLockTaskModeChanged(boolean isEnabled, String pkg, int userHandle) {
        if (isCallerWithSystemUid()) {
            synchronized (getLockObject()) {
                DevicePolicyData policy = getUserData(userHandle);
                if (policy.mStatusBarDisabled) {
                    setStatusBarDisabledInternal(!isEnabled, userHandle);
                }
                Bundle adminExtras = new Bundle();
                adminExtras.putString("android.app.extra.LOCK_TASK_PACKAGE", pkg);
                Iterator<ActiveAdmin> it = policy.mAdminList.iterator();
                while (it.hasNext()) {
                    ActiveAdmin admin = it.next();
                    boolean ownsDevice = isDeviceOwner(admin.info.getComponent(), userHandle);
                    boolean ownsProfile = isProfileOwner(admin.info.getComponent(), userHandle);
                    if (ownsDevice || ownsProfile) {
                        if (isEnabled) {
                            sendAdminCommandLocked(admin, "android.app.action.LOCK_TASK_ENTERING", adminExtras, (BroadcastReceiver) null);
                        } else {
                            sendAdminCommandLocked(admin, "android.app.action.LOCK_TASK_EXITING");
                        }
                        DevicePolicyEventLogger.createEvent(51).setAdmin(admin.info.getPackageName()).setBoolean(isEnabled).setStrings(new String[]{pkg}).write();
                    }
                }
            }
            return;
        }
        throw new SecurityException("notifyLockTaskModeChanged can only be called by system");
    }

    public void setGlobalSetting(ComponentName who, String setting, String value) {
        Preconditions.checkNotNull(who, "ComponentName is null");
        synchronized (getLockObject()) {
            getActiveAdminForCallerLocked(who, -2);
            if (GLOBAL_SETTINGS_DEPRECATED.contains(setting)) {
                Log.i(LOG_TAG, "Global setting no longer supported: " + setting);
                return;
            }
            if (!GLOBAL_SETTINGS_WHITELIST.contains(setting)) {
                if (!UserManager.isDeviceInDemoMode(this.mContext)) {
                    throw new SecurityException(String.format("Permission denial: device owners cannot update %1$s", setting));
                }
            }
            if ("stay_on_while_plugged_in".equals(setting)) {
                long timeMs = getMaximumTimeToLock(who, this.mInjector.userHandleGetCallingUserId(), false);
                if (timeMs > 0 && timeMs < JobStatus.NO_LATEST_RUNTIME) {
                    return;
                }
            }
            long id = this.mInjector.binderClearCallingIdentity();
            try {
                this.mInjector.settingsGlobalPutString(setting, value);
            } finally {
                this.mInjector.binderRestoreCallingIdentity(id);
            }
        }
    }

    public void setSystemSetting(ComponentName who, String setting, String value) {
        Preconditions.checkNotNull(who, "ComponentName is null");
        Preconditions.checkStringNotEmpty(setting, "String setting is null or empty");
        synchronized (getLockObject()) {
            getActiveAdminForCallerLocked(who, -1);
            if (SYSTEM_SETTINGS_WHITELIST.contains(setting)) {
                this.mInjector.binderWithCleanCallingIdentity(new FunctionalUtils.ThrowingRunnable(setting, value, this.mInjector.userHandleGetCallingUserId()) {
                    /* class com.android.server.devicepolicy.$$Lambda$DevicePolicyManagerService$mignzFcOqIvnBFOYi8O3tmqXI68 */
                    private final /* synthetic */ String f$1;
                    private final /* synthetic */ String f$2;
                    private final /* synthetic */ int f$3;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                        this.f$3 = r4;
                    }

                    public final void runOrThrow() {
                        DevicePolicyManagerService.this.lambda$setSystemSetting$10$DevicePolicyManagerService(this.f$1, this.f$2, this.f$3);
                    }
                });
            } else {
                throw new SecurityException(String.format("Permission denial: device owners cannot update %1$s", setting));
            }
        }
    }

    public /* synthetic */ void lambda$setSystemSetting$10$DevicePolicyManagerService(String setting, String value, int callingUserId) throws Exception {
        this.mInjector.settingsSystemPutStringForUser(setting, value, callingUserId);
    }

    public boolean setTime(ComponentName who, long millis) {
        Preconditions.checkNotNull(who, "ComponentName is null in setTime");
        enforceDeviceOwner(who);
        if (this.mInjector.settingsGlobalGetInt("auto_time", 0) == 1) {
            return false;
        }
        this.mInjector.binderWithCleanCallingIdentity(new FunctionalUtils.ThrowingRunnable(millis) {
            /* class com.android.server.devicepolicy.$$Lambda$DevicePolicyManagerService$zqf4q67wkQreppEUOBfp0NE94M */
            private final /* synthetic */ long f$1;

            {
                this.f$1 = r2;
            }

            public final void runOrThrow() {
                DevicePolicyManagerService.this.lambda$setTime$11$DevicePolicyManagerService(this.f$1);
            }
        });
        return true;
    }

    public /* synthetic */ void lambda$setTime$11$DevicePolicyManagerService(long millis) throws Exception {
        this.mInjector.getAlarmManager().setTime(millis);
    }

    public boolean setTimeZone(ComponentName who, String timeZone) {
        Preconditions.checkNotNull(who, "ComponentName is null in setTimeZone");
        enforceDeviceOwner(who);
        if (this.mInjector.settingsGlobalGetInt("auto_time_zone", 0) == 1) {
            return false;
        }
        this.mInjector.binderWithCleanCallingIdentity(new FunctionalUtils.ThrowingRunnable(timeZone) {
            /* class com.android.server.devicepolicy.$$Lambda$DevicePolicyManagerService$1qc4cD7h8K2CVmZeyPCWra8TVtQ */
            private final /* synthetic */ String f$1;

            {
                this.f$1 = r2;
            }

            public final void runOrThrow() {
                DevicePolicyManagerService.this.lambda$setTimeZone$12$DevicePolicyManagerService(this.f$1);
            }
        });
        return true;
    }

    public /* synthetic */ void lambda$setTimeZone$12$DevicePolicyManagerService(String timeZone) throws Exception {
        this.mInjector.getAlarmManager().setTimeZone(timeZone);
    }

    public void setSecureSetting(ComponentName who, String setting, String value) {
        Preconditions.checkNotNull(who, "ComponentName is null");
        int callingUserId = this.mInjector.userHandleGetCallingUserId();
        synchronized (getLockObject()) {
            getActiveAdminForCallerLocked(who, -1);
            if (isDeviceOwner(who, callingUserId)) {
                if (!SECURE_SETTINGS_DEVICEOWNER_WHITELIST.contains(setting)) {
                    if (!isCurrentUserDemo()) {
                        throw new SecurityException(String.format("Permission denial: Device owners cannot update %1$s", setting));
                    }
                }
            } else if (!SECURE_SETTINGS_WHITELIST.contains(setting)) {
                if (!isCurrentUserDemo()) {
                    throw new SecurityException(String.format("Permission denial: Profile owners cannot update %1$s", setting));
                }
            }
            if (!setting.equals("install_non_market_apps")) {
                long id = this.mInjector.binderClearCallingIdentity();
                try {
                    if ("default_input_method".equals(setting)) {
                        if (!TextUtils.equals(this.mInjector.settingsSecureGetStringForUser("default_input_method", callingUserId), value)) {
                            this.mSetupContentObserver.addPendingChangeByOwnerLocked(callingUserId);
                        }
                        getUserData(callingUserId).mCurrentInputMethodSet = true;
                        saveSettingsLocked(callingUserId);
                    }
                    this.mInjector.settingsSecurePutStringForUser(setting, value, callingUserId);
                    DevicePolicyEventLogger.createEvent(14).setAdmin(who).setStrings(new String[]{setting, value}).write();
                } finally {
                    this.mInjector.binderRestoreCallingIdentity(id);
                }
            } else if (getTargetSdk(who.getPackageName(), callingUserId) < 26) {
                if (!this.mUserManager.isManagedProfile(callingUserId)) {
                    Slog.e(LOG_TAG, "Ignoring setSecureSetting request for " + setting + ". User restriction no_install_unknown_sources or no_install_unknown_sources_globally should be used instead.");
                } else {
                    try {
                        setUserRestriction(who, "no_install_unknown_sources", Integer.parseInt(value) == 0);
                        DevicePolicyEventLogger.createEvent(14).setAdmin(who).setStrings(new String[]{setting, value}).write();
                    } catch (NumberFormatException e) {
                        Slog.e(LOG_TAG, "Invalid value: " + value + " for setting " + setting);
                    }
                }
            } else {
                throw new UnsupportedOperationException("install_non_market_apps is deprecated. Please use one of the user restrictions no_install_unknown_sources or no_install_unknown_sources_globally instead.");
            }
        }
    }

    public void setMasterVolumeMuted(ComponentName who, boolean on) {
        Preconditions.checkNotNull(who, "ComponentName is null");
        synchronized (getLockObject()) {
            getActiveAdminForCallerLocked(who, -1);
            setUserRestriction(who, "disallow_unmute_device", on);
            DevicePolicyEventLogger.createEvent(35).setAdmin(who).setBoolean(on).write();
        }
    }

    public boolean isMasterVolumeMuted(ComponentName who) {
        boolean isMasterMute;
        Preconditions.checkNotNull(who, "ComponentName is null");
        synchronized (getLockObject()) {
            getActiveAdminForCallerLocked(who, -1);
            isMasterMute = ((AudioManager) this.mContext.getSystemService("audio")).isMasterMute();
        }
        return isMasterMute;
    }

    public void setUserIcon(ComponentName who, Bitmap icon) {
        synchronized (getLockObject()) {
            Preconditions.checkNotNull(who, "ComponentName is null");
            getActiveAdminForCallerLocked(who, -1);
            int userId = UserHandle.getCallingUserId();
            long id = this.mInjector.binderClearCallingIdentity();
            try {
                this.mUserManagerInternal.setUserIcon(userId, icon);
            } finally {
                this.mInjector.binderRestoreCallingIdentity(id);
            }
        }
        DevicePolicyEventLogger.createEvent(41).setAdmin(who).write();
    }

    public boolean setKeyguardDisabled(ComponentName who, boolean disabled) {
        Preconditions.checkNotNull(who, "ComponentName is null");
        int userId = this.mInjector.userHandleGetCallingUserId();
        synchronized (getLockObject()) {
            getActiveAdminForCallerLocked(who, -1);
            if (!isUserAffiliatedWithDeviceLocked(userId)) {
                throw new SecurityException("Admin " + who + " is neither the device owner or affiliated user's profile owner.");
            }
        }
        if (!isManagedProfile(userId)) {
            long ident = this.mInjector.binderClearCallingIdentity();
            if (disabled) {
                try {
                    if (this.mLockPatternUtils.isSecure(userId)) {
                        this.mInjector.binderRestoreCallingIdentity(ident);
                        return false;
                    }
                } catch (RemoteException e) {
                    Slog.e(LOG_TAG, "cannot set keyguard disabled.");
                } catch (Throwable th) {
                    this.mInjector.binderRestoreCallingIdentity(ident);
                    throw th;
                }
            }
            this.mLockPatternUtils.setLockScreenDisabled(disabled, userId);
            if (disabled) {
                this.mInjector.getIWindowManager().dismissKeyguard((IKeyguardDismissCallback) null, (CharSequence) null);
            }
            DevicePolicyEventLogger.createEvent(37).setAdmin(who).setBoolean(disabled).write();
            this.mInjector.binderRestoreCallingIdentity(ident);
            return true;
        }
        throw new SecurityException("Managed profile cannot disable keyguard");
    }

    public boolean setStatusBarDisabled(ComponentName who, boolean disabled) {
        int userId = UserHandle.getCallingUserId();
        synchronized (getLockObject()) {
            getActiveAdminForCallerLocked(who, -1);
            if (!isUserAffiliatedWithDeviceLocked(userId)) {
                throw new SecurityException("Admin " + who + " is neither the device owner or affiliated user's profile owner.");
            } else if (!isManagedProfile(userId)) {
                DevicePolicyData policy = getUserData(userId);
                if (policy.mStatusBarDisabled != disabled) {
                    boolean isLockTaskMode = false;
                    try {
                        isLockTaskMode = this.mInjector.getIActivityTaskManager().getLockTaskModeState() != 0;
                    } catch (RemoteException e) {
                        Slog.e(LOG_TAG, "Failed to get LockTask mode");
                    }
                    if (!isLockTaskMode && !setStatusBarDisabledInternal(disabled, userId)) {
                        return false;
                    }
                    policy.mStatusBarDisabled = disabled;
                    saveSettingsLocked(userId);
                }
                DevicePolicyEventLogger.createEvent(38).setAdmin(who).setBoolean(disabled).write();
                return true;
            } else {
                throw new SecurityException("Managed profile cannot disable status bar");
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
                this.mInjector.binderRestoreCallingIdentity(ident);
                return true;
            }
        } catch (RemoteException e) {
            Slog.e(LOG_TAG, "Failed to disable the status bar", e);
        } catch (Throwable th) {
            this.mInjector.binderRestoreCallingIdentity(ident);
            throw th;
        }
        this.mInjector.binderRestoreCallingIdentity(ident);
        return false;
    }

    /* access modifiers changed from: package-private */
    public void updateUserSetupCompleteAndPaired() {
        List<UserInfo> users = this.mUserManager.getUsers(true);
        int N = users.size();
        for (int i = 0; i < N; i++) {
            int userHandle = users.get(i).id;
            if (this.mInjector.settingsSecureGetIntForUser("user_setup_complete", 0, userHandle) != 0) {
                DevicePolicyData policy = getUserData(userHandle);
                if (!policy.mUserSetupComplete) {
                    policy.mUserSetupComplete = true;
                    synchronized (getLockObject()) {
                        saveSettingsLocked(userHandle);
                    }
                }
            }
            if (this.mIsWatch && this.mInjector.settingsSecureGetIntForUser("device_paired", 0, userHandle) != 0) {
                DevicePolicyData policy2 = getUserData(userHandle);
                if (!policy2.mPaired) {
                    policy2.mPaired = true;
                    synchronized (getLockObject()) {
                        saveSettingsLocked(userHandle);
                    }
                } else {
                    continue;
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public class SetupContentObserver extends ContentObserver {
        private final Uri mDefaultImeChanged = Settings.Secure.getUriFor("default_input_method");
        private final Uri mDeviceProvisioned = Settings.Global.getUriFor("device_provisioned");
        private final Uri mPaired = Settings.Secure.getUriFor("device_paired");
        @GuardedBy({"getLockObject()"})
        private Set<Integer> mUserIdsWithPendingChangesByOwner = new ArraySet();
        private final Uri mUserSetupComplete = Settings.Secure.getUriFor("user_setup_complete");

        public SetupContentObserver(Handler handler) {
            super(handler);
        }

        /* access modifiers changed from: package-private */
        public void register() {
            DevicePolicyManagerService.this.mInjector.registerContentObserver(this.mUserSetupComplete, false, this, -1);
            DevicePolicyManagerService.this.mInjector.registerContentObserver(this.mDeviceProvisioned, false, this, -1);
            if (DevicePolicyManagerService.this.mIsWatch) {
                DevicePolicyManagerService.this.mInjector.registerContentObserver(this.mPaired, false, this, -1);
            }
            DevicePolicyManagerService.this.mInjector.registerContentObserver(this.mDefaultImeChanged, false, this, -1);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        @GuardedBy({"getLockObject()"})
        private void addPendingChangeByOwnerLocked(int userId) {
            this.mUserIdsWithPendingChangesByOwner.add(Integer.valueOf(userId));
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange, Uri uri, int userId) {
            if (this.mUserSetupComplete.equals(uri) || (DevicePolicyManagerService.this.mIsWatch && this.mPaired.equals(uri))) {
                DevicePolicyManagerService.this.updateUserSetupCompleteAndPaired();
            } else if (this.mDeviceProvisioned.equals(uri)) {
                synchronized (DevicePolicyManagerService.this.getLockObject()) {
                    DevicePolicyManagerService.this.setDeviceOwnerSystemPropertyLocked();
                }
            } else if (this.mDefaultImeChanged.equals(uri)) {
                synchronized (DevicePolicyManagerService.this.getLockObject()) {
                    if (this.mUserIdsWithPendingChangesByOwner.contains(Integer.valueOf(userId))) {
                        this.mUserIdsWithPendingChangesByOwner.remove(Integer.valueOf(userId));
                    } else {
                        DevicePolicyManagerService.this.getUserData(userId).mCurrentInputMethodSet = false;
                        DevicePolicyManagerService.this.saveSettingsLocked(userId);
                    }
                }
            }
        }
    }

    private class DevicePolicyConstantsObserver extends ContentObserver {
        final Uri mConstantsUri = Settings.Global.getUriFor("device_policy_constants");

        DevicePolicyConstantsObserver(Handler handler) {
            super(handler);
        }

        /* access modifiers changed from: package-private */
        public void register() {
            DevicePolicyManagerService.this.mInjector.registerContentObserver(this.mConstantsUri, false, this, -1);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange, Uri uri, int userId) {
            DevicePolicyManagerService devicePolicyManagerService = DevicePolicyManagerService.this;
            devicePolicyManagerService.mConstants = devicePolicyManagerService.loadConstants();
        }
    }

    @VisibleForTesting
    final class LocalService extends DevicePolicyManagerInternal {
        private List<DevicePolicyManagerInternal.OnCrossProfileWidgetProvidersChangeListener> mWidgetProviderListeners;

        LocalService() {
        }

        public List<String> getCrossProfileWidgetProviders(int profileId) {
            synchronized (DevicePolicyManagerService.this.getLockObject()) {
                if (DevicePolicyManagerService.this.mOwners == null) {
                    return Collections.emptyList();
                }
                ComponentName ownerComponent = DevicePolicyManagerService.this.mOwners.getProfileOwnerComponent(profileId);
                if (ownerComponent == null) {
                    return Collections.emptyList();
                }
                ActiveAdmin admin = DevicePolicyManagerService.this.getUserDataUnchecked(profileId).mAdminMap.get(ownerComponent);
                if (!(admin == null || admin.crossProfileWidgetProviders == null)) {
                    if (!admin.crossProfileWidgetProviders.isEmpty()) {
                        return admin.crossProfileWidgetProviders;
                    }
                }
                return Collections.emptyList();
            }
        }

        public void addOnCrossProfileWidgetProvidersChangeListener(DevicePolicyManagerInternal.OnCrossProfileWidgetProvidersChangeListener listener) {
            synchronized (DevicePolicyManagerService.this.getLockObject()) {
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
            synchronized (DevicePolicyManagerService.this.getLockObject()) {
                z = DevicePolicyManagerService.this.getActiveAdminWithPolicyForUidLocked(null, reqPolicy, uid) != null;
            }
            return z;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void notifyCrossProfileProvidersChanged(int userId, List<String> packages) {
            List<DevicePolicyManagerInternal.OnCrossProfileWidgetProvidersChangeListener> listeners;
            synchronized (DevicePolicyManagerService.this.getLockObject()) {
                listeners = new ArrayList<>(this.mWidgetProviderListeners);
            }
            int listenerCount = listeners.size();
            for (int i = 0; i < listenerCount; i++) {
                listeners.get(i).onCrossProfileWidgetProvidersChanged(userId, packages);
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
                List<UserManager.EnforcingUser> sources = DevicePolicyManagerService.this.mUserManager.getUserRestrictionSources(userRestriction, UserHandle.of(userId));
                if (sources != null) {
                    if (!sources.isEmpty()) {
                        if (sources.size() > 1) {
                            Intent createShowAdminSupportIntent = DevicePolicyManagerService.this.createShowAdminSupportIntent(null, userId);
                            DevicePolicyManagerService.this.mInjector.binderRestoreCallingIdentity(ident);
                            return createShowAdminSupportIntent;
                        }
                        UserManager.EnforcingUser enforcingUser = sources.get(0);
                        int sourceType = enforcingUser.getUserRestrictionSource();
                        int enforcingUserId = enforcingUser.getUserHandle().getIdentifier();
                        if (sourceType == 4) {
                            ComponentName profileOwner = DevicePolicyManagerService.this.mOwners.getProfileOwnerComponent(enforcingUserId);
                            if (profileOwner != null) {
                                Intent createShowAdminSupportIntent2 = DevicePolicyManagerService.this.createShowAdminSupportIntent(profileOwner, enforcingUserId);
                                DevicePolicyManagerService.this.mInjector.binderRestoreCallingIdentity(ident);
                                return createShowAdminSupportIntent2;
                            }
                        } else if (sourceType == 2) {
                            Pair<Integer, ComponentName> deviceOwner = DevicePolicyManagerService.this.mOwners.getDeviceOwnerUserIdAndComponent();
                            if (deviceOwner != null) {
                                Intent createShowAdminSupportIntent3 = DevicePolicyManagerService.this.createShowAdminSupportIntent((ComponentName) deviceOwner.second, ((Integer) deviceOwner.first).intValue());
                                DevicePolicyManagerService.this.mInjector.binderRestoreCallingIdentity(ident);
                                return createShowAdminSupportIntent3;
                            }
                        } else if (sourceType == 1) {
                            DevicePolicyManagerService.this.mInjector.binderRestoreCallingIdentity(ident);
                            return null;
                        }
                        DevicePolicyManagerService.this.mInjector.binderRestoreCallingIdentity(ident);
                        return null;
                    }
                }
                return null;
            } finally {
                DevicePolicyManagerService.this.mInjector.binderRestoreCallingIdentity(ident);
            }
        }

        public boolean isUserAffiliatedWithDevice(int userId) {
            return DevicePolicyManagerService.this.isUserAffiliatedWithDeviceLocked(userId);
        }

        public boolean canSilentlyInstallPackage(String callerPackage, int callerUid) {
            if (callerPackage != null && isUserAffiliatedWithDevice(UserHandle.getUserId(callerUid)) && isActiveAdminWithPolicy(callerUid, -1)) {
                return true;
            }
            return false;
        }

        /* JADX INFO: finally extract failed */
        public void reportSeparateProfileChallengeChanged(int userId) {
            long ident = DevicePolicyManagerService.this.mInjector.binderClearCallingIdentity();
            try {
                synchronized (DevicePolicyManagerService.this.getLockObject()) {
                    DevicePolicyManagerService.this.updateMaximumTimeToLockLocked(userId);
                    DevicePolicyManagerService.this.updatePasswordQualityCacheForUserGroup(userId);
                }
                DevicePolicyManagerService.this.mInjector.binderRestoreCallingIdentity(ident);
                DevicePolicyEventLogger.createEvent((int) IHwLbsLogger.LOCATION_WIFI_CONNECTION).setBoolean(DevicePolicyManagerService.this.isSeparateProfileChallengeEnabled(userId)).write();
            } catch (Throwable th) {
                DevicePolicyManagerService.this.mInjector.binderRestoreCallingIdentity(ident);
                throw th;
            }
        }

        public boolean canUserHaveUntrustedCredentialReset(int userId) {
            return DevicePolicyManagerService.this.canUserHaveUntrustedCredentialReset(userId);
        }

        public CharSequence getPrintingDisabledReasonForUser(int userId) {
            synchronized (DevicePolicyManagerService.this.getLockObject()) {
                if (!DevicePolicyManagerService.this.mUserManager.hasUserRestriction("no_printing", UserHandle.of(userId))) {
                    Log.e(DevicePolicyManagerService.LOG_TAG, "printing is enabled");
                    return null;
                }
                String ownerPackage = DevicePolicyManagerService.this.mOwners.getProfileOwnerPackage(userId);
                if (ownerPackage == null) {
                    ownerPackage = DevicePolicyManagerService.this.mOwners.getDeviceOwnerPackageName();
                }
                PackageManager pm = DevicePolicyManagerService.this.mInjector.getPackageManager();
                try {
                    PackageInfo packageInfo = pm.getPackageInfo(ownerPackage, 0);
                    if (packageInfo == null) {
                        Log.e(DevicePolicyManagerService.LOG_TAG, "packageInfo is inexplicably null");
                        return null;
                    }
                    ApplicationInfo appInfo = packageInfo.applicationInfo;
                    if (appInfo == null) {
                        Log.e(DevicePolicyManagerService.LOG_TAG, "appInfo is inexplicably null");
                        return null;
                    }
                    CharSequence appLabel = pm.getApplicationLabel(appInfo);
                    if (appLabel == null) {
                        Log.e(DevicePolicyManagerService.LOG_TAG, "appLabel is inexplicably null");
                        return null;
                    }
                    return ActivityThread.currentActivityThread().getSystemUiContext().getResources().getString(17041080, appLabel);
                } catch (PackageManager.NameNotFoundException e) {
                    Log.e(DevicePolicyManagerService.LOG_TAG, "getPackageInfo error", e);
                    return null;
                }
            }
        }

        /* access modifiers changed from: protected */
        public DevicePolicyCache getDevicePolicyCache() {
            return DevicePolicyManagerService.this.mPolicyCache;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private Intent createShowAdminSupportIntent(ComponentName admin, int userId) {
        Intent intent = new Intent("android.settings.SHOW_ADMIN_SUPPORT_DETAILS");
        intent.putExtra("android.intent.extra.USER_ID", userId);
        intent.putExtra("android.app.extra.DEVICE_ADMIN", admin);
        intent.setFlags(268435456);
        return intent;
    }

    public Intent createAdminSupportIntent(String restriction) {
        ActiveAdmin admin;
        ActiveAdmin admin2;
        Preconditions.checkNotNull(restriction);
        int userId = UserHandle.getUserId(this.mInjector.binderGetCallingUid());
        Intent intent = null;
        if ("policy_disable_camera".equals(restriction) || "policy_disable_screen_capture".equals(restriction)) {
            synchronized (getLockObject()) {
                DevicePolicyData policy = getUserData(userId);
                int N = policy.mAdminList.size();
                int i = 0;
                while (true) {
                    if (i >= N) {
                        break;
                    }
                    admin2 = policy.mAdminList.get(i);
                    if ((!admin2.disableCamera || !"policy_disable_camera".equals(restriction)) && (!admin2.disableScreenCapture || !"policy_disable_screen_capture".equals(restriction))) {
                        i++;
                    }
                }
                intent = createShowAdminSupportIntent(admin2.info.getComponent(), userId);
                if (intent == null && "policy_disable_camera".equals(restriction) && (admin = getDeviceOwnerAdminLocked()) != null && admin.disableCamera) {
                    intent = createShowAdminSupportIntent(admin.info.getComponent(), this.mOwners.getDeviceOwnerUserId());
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
        int i;
        if (policy != null) {
            policy.validateType();
            policy.validateFreezePeriods();
            Pair<LocalDate, LocalDate> record = this.mOwners.getSystemUpdateFreezePeriodRecord();
            policy.validateAgainstPreviousFreezePeriod((LocalDate) record.first, (LocalDate) record.second, LocalDate.now());
        }
        synchronized (getLockObject()) {
            getActiveAdminForCallerLocked(who, -2);
            i = 0;
            if (policy == null) {
                this.mOwners.clearSystemUpdatePolicy();
            } else {
                this.mOwners.setSystemUpdatePolicy(policy);
                updateSystemUpdateFreezePeriodsRecord(false);
            }
            this.mOwners.writeDeviceOwner();
        }
        this.mContext.sendBroadcastAsUser(new Intent("android.app.action.SYSTEM_UPDATE_POLICY_CHANGED"), UserHandle.SYSTEM);
        DevicePolicyEventLogger admin = DevicePolicyEventLogger.createEvent(50).setAdmin(who);
        if (policy != null) {
            i = policy.getPolicyType();
        }
        admin.setInt(i).write();
    }

    public SystemUpdatePolicy getSystemUpdatePolicy() {
        synchronized (getLockObject()) {
            SystemUpdatePolicy policy = this.mOwners.getSystemUpdatePolicy();
            if (policy == null || policy.isValid()) {
                return policy;
            }
            Slog.w(LOG_TAG, "Stored system update policy is invalid, return null instead.");
            return null;
        }
    }

    private static boolean withinRange(Pair<LocalDate, LocalDate> range, LocalDate date) {
        return !date.isBefore((ChronoLocalDate) range.first) && !date.isAfter((ChronoLocalDate) range.second);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateSystemUpdateFreezePeriodsRecord(boolean saveIfChanged) {
        boolean changed;
        Slog.d(LOG_TAG, "updateSystemUpdateFreezePeriodsRecord");
        synchronized (getLockObject()) {
            SystemUpdatePolicy policy = this.mOwners.getSystemUpdatePolicy();
            if (policy != null) {
                LocalDate now = LocalDate.now();
                Pair<LocalDate, LocalDate> currentPeriod = policy.getCurrentFreezePeriod(now);
                if (currentPeriod != null) {
                    Pair<LocalDate, LocalDate> record = this.mOwners.getSystemUpdateFreezePeriodRecord();
                    LocalDate start = (LocalDate) record.first;
                    LocalDate end = (LocalDate) record.second;
                    if (end != null) {
                        if (start != null) {
                            if (now.equals(end.plusDays(1))) {
                                changed = this.mOwners.setSystemUpdateFreezePeriodRecord(start, now);
                            } else if (now.isAfter(end.plusDays(1))) {
                                if (!withinRange(currentPeriod, start) || !withinRange(currentPeriod, end)) {
                                    changed = this.mOwners.setSystemUpdateFreezePeriodRecord(now, now);
                                } else {
                                    changed = this.mOwners.setSystemUpdateFreezePeriodRecord(start, now);
                                }
                            } else if (now.isBefore(start)) {
                                changed = this.mOwners.setSystemUpdateFreezePeriodRecord(now, now);
                            } else {
                                changed = false;
                            }
                            if (changed && saveIfChanged) {
                                this.mOwners.writeDeviceOwner();
                            }
                        }
                    }
                    changed = this.mOwners.setSystemUpdateFreezePeriodRecord(now, now);
                    this.mOwners.writeDeviceOwner();
                }
            }
        }
    }

    @Override // com.android.server.devicepolicy.BaseIDevicePolicyManager
    public void clearSystemUpdatePolicyFreezePeriodRecord() {
        enforceShell("clearSystemUpdatePolicyFreezePeriodRecord");
        synchronized (getLockObject()) {
            Slog.i(LOG_TAG, "Clear freeze period record: " + this.mOwners.getSystemUpdateFreezePeriodRecordAsString());
            if (this.mOwners.setSystemUpdateFreezePeriodRecord(null, null)) {
                this.mOwners.writeDeviceOwner();
            }
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public boolean isCallerDeviceOwner(int callerUid) {
        synchronized (getLockObject()) {
            if (!this.mOwners.hasDeviceOwner()) {
                return false;
            }
            if (UserHandle.getUserId(callerUid) != this.mOwners.getDeviceOwnerUserId()) {
                return false;
            }
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

    public void notifyPendingSystemUpdate(SystemUpdateInfo info) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.NOTIFY_PENDING_SYSTEM_UPDATE", "Only the system update service can broadcast update information");
        if (UserHandle.getCallingUserId() != 0) {
            Slog.w(LOG_TAG, "Only the system update service in the system user can broadcast update information.");
        } else if (this.mOwners.saveSystemUpdateInfo(info)) {
            Intent intent = new Intent("android.app.action.NOTIFY_PENDING_SYSTEM_UPDATE").putExtra("android.app.extra.SYSTEM_UPDATE_RECEIVED_TIME", info == null ? -1 : info.getReceivedTime());
            long ident = this.mInjector.binderClearCallingIdentity();
            try {
                synchronized (getLockObject()) {
                    if (this.mOwners.hasDeviceOwner()) {
                        UserHandle deviceOwnerUser = UserHandle.of(this.mOwners.getDeviceOwnerUserId());
                        intent.setComponent(this.mOwners.getDeviceOwnerComponent());
                        this.mContext.sendBroadcastAsUser(intent, deviceOwnerUser);
                    }
                }
                try {
                    int[] runningUserIds = this.mInjector.getIActivityManager().getRunningUserIds();
                    for (int userId : runningUserIds) {
                        synchronized (getLockObject()) {
                            ComponentName profileOwnerPackage = this.mOwners.getProfileOwnerComponent(userId);
                            if (profileOwnerPackage != null) {
                                intent.setComponent(profileOwnerPackage);
                                this.mContext.sendBroadcastAsUser(intent, UserHandle.of(userId));
                            }
                        }
                    }
                } catch (RemoteException e) {
                    Log.e(LOG_TAG, "Could not retrieve the list of running users", e);
                    this.mInjector.binderRestoreCallingIdentity(ident);
                }
            } finally {
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
        synchronized (getLockObject()) {
            enforceCanManageScope(admin, callerPackage, -1, "delegation-permission-grant");
            DevicePolicyData userPolicy = getUserData(userId);
            if (userPolicy.mPermissionPolicy != policy) {
                userPolicy.mPermissionPolicy = policy;
                saveSettingsLocked(userId);
            }
        }
        DevicePolicyEventLogger.createEvent(18).setAdmin(callerPackage).setInt(policy).setBoolean(admin == null).write();
    }

    public int getPermissionPolicy(ComponentName admin) throws RemoteException {
        int i;
        int userId = UserHandle.getCallingUserId();
        synchronized (getLockObject()) {
            i = getUserData(userId).mPermissionPolicy;
        }
        return i;
    }

    public void setPermissionGrantState(ComponentName admin, String callerPackage, String packageName, String permission, int grantState, RemoteCallback callback) throws RemoteException {
        Throwable th;
        SecurityException e;
        long ident;
        PackageManager.NameNotFoundException e2;
        long ident2;
        Preconditions.checkNotNull(callback);
        UserHandle user = this.mInjector.binderGetCallingUserHandle();
        synchronized (getLockObject()) {
            try {
                enforceCanManageScope(admin, callerPackage, -1, "delegation-permission-grant");
                long ident3 = this.mInjector.binderClearCallingIdentity();
                try {
                    boolean isPostQAdmin = getTargetSdk(callerPackage, user.getIdentifier()) >= 29;
                    if (!isPostQAdmin) {
                        try {
                            if (getTargetSdk(packageName, user.getIdentifier()) < 23) {
                                callback.sendResult((Bundle) null);
                                this.mInjector.binderRestoreCallingIdentity(ident3);
                                return;
                            }
                        } catch (SecurityException e3) {
                            e = e3;
                            try {
                                Slog.e(LOG_TAG, "Could not set permission grant state", e);
                                callback.sendResult((Bundle) null);
                                this.mInjector.binderRestoreCallingIdentity(ident3);
                            } catch (Throwable th2) {
                                th = th2;
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            this.mInjector.binderRestoreCallingIdentity(ident3);
                            throw th;
                        }
                    }
                    try {
                        if (!isRuntimePermission(permission)) {
                            try {
                                callback.sendResult((Bundle) null);
                                this.mInjector.binderRestoreCallingIdentity(ident3);
                            } catch (PackageManager.NameNotFoundException e4) {
                                e2 = e4;
                                ident = ident3;
                                try {
                                    StringBuilder sb = new StringBuilder();
                                    sb.append("Cannot check if ");
                                    ident3 = ident;
                                } catch (SecurityException e5) {
                                    e = e5;
                                    ident3 = ident;
                                    Slog.e(LOG_TAG, "Could not set permission grant state", e);
                                    callback.sendResult((Bundle) null);
                                    this.mInjector.binderRestoreCallingIdentity(ident3);
                                } catch (Throwable th4) {
                                    th = th4;
                                    ident3 = ident;
                                    this.mInjector.binderRestoreCallingIdentity(ident3);
                                    throw th;
                                }
                                try {
                                    sb.append(permission);
                                    sb.append("is a runtime permission");
                                    throw new RemoteException(sb.toString(), e2, false, true);
                                } catch (SecurityException e6) {
                                    e = e6;
                                    Slog.e(LOG_TAG, "Could not set permission grant state", e);
                                    callback.sendResult((Bundle) null);
                                    this.mInjector.binderRestoreCallingIdentity(ident3);
                                }
                            }
                        } else {
                            if (grantState == 1 || grantState == 2 || grantState == 0) {
                                try {
                                    try {
                                        ident2 = ident3;
                                        try {
                                            this.mInjector.getPermissionControllerManager(user).setRuntimePermissionGrantStateByDeviceAdmin(callerPackage, packageName, permission, grantState, this.mContext.getMainExecutor(), new Consumer(isPostQAdmin, callback, admin, callerPackage, permission, grantState) {
                                                /* class com.android.server.devicepolicy.$$Lambda$DevicePolicyManagerService$kFKlG1V9Atta0tqqg2BxiKi4I */
                                                private final /* synthetic */ boolean f$0;
                                                private final /* synthetic */ RemoteCallback f$1;
                                                private final /* synthetic */ ComponentName f$2;
                                                private final /* synthetic */ String f$3;
                                                private final /* synthetic */ String f$4;
                                                private final /* synthetic */ int f$5;

                                                {
                                                    this.f$0 = r1;
                                                    this.f$1 = r2;
                                                    this.f$2 = r3;
                                                    this.f$3 = r4;
                                                    this.f$4 = r5;
                                                    this.f$5 = r6;
                                                }

                                                @Override // java.util.function.Consumer
                                                public final void accept(Object obj) {
                                                    DevicePolicyManagerService.lambda$setPermissionGrantState$13(this.f$0, this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, (Boolean) obj);
                                                }
                                            });
                                        } catch (SecurityException e7) {
                                            e = e7;
                                            ident3 = ident2;
                                            Slog.e(LOG_TAG, "Could not set permission grant state", e);
                                            callback.sendResult((Bundle) null);
                                            this.mInjector.binderRestoreCallingIdentity(ident3);
                                        } catch (Throwable th5) {
                                            th = th5;
                                            ident3 = ident2;
                                            this.mInjector.binderRestoreCallingIdentity(ident3);
                                            throw th;
                                        }
                                    } catch (SecurityException e8) {
                                        e = e8;
                                        ident2 = ident3;
                                        ident3 = ident2;
                                        Slog.e(LOG_TAG, "Could not set permission grant state", e);
                                        callback.sendResult((Bundle) null);
                                        this.mInjector.binderRestoreCallingIdentity(ident3);
                                    } catch (Throwable th6) {
                                        th = th6;
                                        ident2 = ident3;
                                        ident3 = ident2;
                                        this.mInjector.binderRestoreCallingIdentity(ident3);
                                        throw th;
                                    }
                                } catch (SecurityException e9) {
                                    e = e9;
                                    Slog.e(LOG_TAG, "Could not set permission grant state", e);
                                    callback.sendResult((Bundle) null);
                                    this.mInjector.binderRestoreCallingIdentity(ident3);
                                } catch (Throwable th7) {
                                    th = th7;
                                    this.mInjector.binderRestoreCallingIdentity(ident3);
                                    throw th;
                                }
                            } else {
                                ident2 = ident3;
                            }
                            try {
                                this.mInjector.binderRestoreCallingIdentity(ident2);
                            } catch (Throwable th8) {
                                th = th8;
                                throw th;
                            }
                        }
                    } catch (PackageManager.NameNotFoundException e10) {
                        e2 = e10;
                        ident = ident3;
                        StringBuilder sb2 = new StringBuilder();
                        sb2.append("Cannot check if ");
                        ident3 = ident;
                        sb2.append(permission);
                        sb2.append("is a runtime permission");
                        throw new RemoteException(sb2.toString(), e2, false, true);
                    }
                } catch (SecurityException e11) {
                    e = e11;
                    Slog.e(LOG_TAG, "Could not set permission grant state", e);
                    callback.sendResult((Bundle) null);
                    this.mInjector.binderRestoreCallingIdentity(ident3);
                } catch (Throwable th9) {
                    th = th9;
                    this.mInjector.binderRestoreCallingIdentity(ident3);
                    throw th;
                }
            } catch (Throwable th10) {
                th = th10;
                throw th;
            }
        }
    }

    static /* synthetic */ void lambda$setPermissionGrantState$13(boolean isPostQAdmin, RemoteCallback callback, ComponentName admin, String callerPackage, String permission, int grantState, Boolean permissionWasSet) {
        if (!isPostQAdmin || permissionWasSet.booleanValue()) {
            DevicePolicyEventLogger.createEvent(19).setAdmin(callerPackage).setStrings(new String[]{permission}).setInt(grantState).setBoolean(admin == null).write();
            callback.sendResult(Bundle.EMPTY);
            return;
        }
        callback.sendResult((Bundle) null);
    }

    public int getPermissionGrantState(ComponentName admin, String callerPackage, String packageName, String permission) throws RemoteException {
        int granted;
        PackageManager packageManager = this.mInjector.getPackageManager();
        UserHandle user = this.mInjector.binderGetCallingUserHandle();
        if (!isCallerWithSystemUid()) {
            enforceCanManageScope(admin, callerPackage, -1, "delegation-permission-grant");
        }
        synchronized (getLockObject()) {
            long ident = this.mInjector.binderClearCallingIdentity();
            try {
                int i = 1;
                if (getTargetSdk(callerPackage, user.getIdentifier()) < 29) {
                    granted = this.mIPackageManager.checkPermission(permission, packageName, user.getIdentifier());
                } else {
                    try {
                        if (PermissionChecker.checkPermissionForPreflight(this.mContext, permission, -1, packageManager.getPackageUidAsUser(packageName, user.getIdentifier()), packageName) != 0) {
                            granted = -1;
                        } else {
                            granted = 0;
                        }
                    } catch (PackageManager.NameNotFoundException e) {
                        throw new RemoteException("Cannot check if " + permission + "is a runtime permission", e, false, true);
                    }
                }
                if ((packageManager.getPermissionFlags(permission, packageName, user) & 4) != 4) {
                    return 0;
                }
                if (granted != 0) {
                    i = 2;
                }
                this.mInjector.binderRestoreCallingIdentity(ident);
                return i;
            } finally {
                this.mInjector.binderRestoreCallingIdentity(ident);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isPackageInstalledForUser(String packageName, int userHandle) {
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

    public boolean isRuntimePermission(String permissionName) throws PackageManager.NameNotFoundException {
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
            return checkProvisioningPreConditionSkipPermission(action, packageName) == 0;
        } catch (PackageManager.NameNotFoundException e) {
            throw new IllegalArgumentException("Invalid package provided " + packageName, e);
        } catch (Throwable th) {
            this.mInjector.binderRestoreCallingIdentity(ident);
            throw th;
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
            char c = 65535;
            switch (action.hashCode()) {
                case -920528692:
                    if (action.equals("android.app.action.PROVISION_MANAGED_DEVICE")) {
                        c = 1;
                        break;
                    }
                    break;
                case -514404415:
                    if (action.equals("android.app.action.PROVISION_MANAGED_USER")) {
                        c = 2;
                        break;
                    }
                    break;
                case -340845101:
                    if (action.equals("android.app.action.PROVISION_MANAGED_PROFILE")) {
                        c = 0;
                        break;
                    }
                    break;
                case 631897778:
                    if (action.equals("android.app.action.PROVISION_MANAGED_SHAREABLE_DEVICE")) {
                        c = 3;
                        break;
                    }
                    break;
            }
            if (c == 0) {
                return checkManagedProfileProvisioningPreCondition(packageName, callingUserId);
            }
            if (c == 1) {
                return checkDeviceOwnerProvisioningPreCondition(callingUserId);
            }
            if (c == 2) {
                return checkManagedUserProvisioningPreCondition(callingUserId);
            }
            if (c == 3) {
                return checkManagedShareableDeviceProvisioningPreCondition(callingUserId);
            }
        }
        throw new IllegalArgumentException("Unknown provisioning action " + action);
    }

    private int checkDeviceOwnerProvisioningPreConditionLocked(ComponentName owner, int deviceOwnerUserId, boolean isAdb, boolean hasIncompatibleAccountsOrNonAdb) {
        IHwDevicePolicyManagerService iHwDevicePolicyManagerService;
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
        if (isAdb || ((iHwDevicePolicyManagerService = this.mHwDevicePolicyManagerService) != null && iHwDevicePolicyManagerService.isMdmApiDeviceOwner())) {
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
        synchronized (getLockObject()) {
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
            if (!this.mUserManager.hasUserRestriction("no_add_managed_profile", callingUserHandle) || (ownerAdmin != null && !isAdminAffectedByRestriction(ownerAdmin, "no_add_managed_profile", callingUserId))) {
                boolean canRemoveProfile = true;
                if (this.mUserManager.hasUserRestriction("no_remove_managed_profile", callingUserHandle) && (ownerAdmin == null || isAdminAffectedByRestriction(ownerAdmin, "no_remove_managed_profile", callingUserId))) {
                    canRemoveProfile = false;
                }
                if (!this.mUserManager.canAddMoreManagedProfiles(callingUserId, canRemoveProfile)) {
                    this.mInjector.binderRestoreCallingIdentity(ident);
                    return 11;
                }
                this.mInjector.binderRestoreCallingIdentity(ident);
                return 0;
            }
            return 15;
        } finally {
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
        synchronized (getLockObject()) {
            if (this.mOwners.getDeviceOwnerUserId() == userId) {
                return this.mOwners.getDeviceOwnerComponent();
            } else if (!this.mOwners.hasProfileOwner(userId)) {
                return null;
            } else {
                return this.mOwners.getProfileOwnerComponent(userId);
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
        if (!this.mIsWatch || !hasPaired(0)) {
            return 0;
        }
        return 8;
    }

    private int checkManagedShareableDeviceProvisioningPreCondition(int callingUserId) {
        if (!this.mInjector.userManagerIsSplitSystemUser()) {
            return 12;
        }
        return checkDeviceOwnerProvisioningPreCondition(callingUserId);
    }

    private boolean hasFeatureManagedUsers() {
        try {
            return this.mIPackageManager.hasSystemFeature("android.software.managed_users", 0);
        } catch (RemoteException e) {
            return false;
        }
    }

    public String getWifiMacAddress(ComponentName admin) {
        enforceDeviceOwner(admin);
        long ident = this.mInjector.binderClearCallingIdentity();
        try {
            String[] macAddresses = this.mInjector.getWifiManager().getFactoryMacAddresses();
            String str = null;
            if (macAddresses == null) {
                return null;
            }
            DevicePolicyEventLogger.createEvent(54).setAdmin(admin).write();
            if (macAddresses.length > 0) {
                str = macAddresses[0];
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
            if (ai == null) {
                return 0;
            }
            return ai.targetSdkVersion;
        } catch (RemoteException e) {
            return 0;
        }
    }

    public boolean isManagedProfile(ComponentName admin) {
        enforceProfileOrDeviceOwner(admin);
        return isManagedProfile(this.mInjector.userHandleGetCallingUserId());
    }

    public boolean isSystemOnlyUser(ComponentName admin) {
        enforceDeviceOwner(admin);
        return UserManager.isSplitSystemUser() && this.mInjector.userHandleGetCallingUserId() == 0;
    }

    public void reboot(ComponentName admin) {
        Preconditions.checkNotNull(admin);
        enforceDeviceOwner(admin);
        long ident = this.mInjector.binderClearCallingIdentity();
        try {
            if (this.mTelephonyManager.getCallState() == 0) {
                DevicePolicyEventLogger.createEvent(34).setAdmin(admin).write();
                this.mInjector.powerManagerReboot("deviceowner");
                return;
            }
            throw new IllegalStateException("Cannot be called with ongoing call on the device");
        } finally {
            this.mInjector.binderRestoreCallingIdentity(ident);
        }
    }

    public void setShortSupportMessage(ComponentName who, CharSequence message) {
        if (this.mHasFeature) {
            Preconditions.checkNotNull(who, "ComponentName is null");
            int userHandle = this.mInjector.userHandleGetCallingUserId();
            synchronized (getLockObject()) {
                ActiveAdmin admin = getActiveAdminForUidLocked(who, this.mInjector.binderGetCallingUid());
                if (!TextUtils.equals(admin.shortSupportMessage, message)) {
                    admin.shortSupportMessage = message;
                    saveSettingsLocked(userHandle);
                }
            }
            DevicePolicyEventLogger.createEvent(43).setAdmin(who).write();
        }
    }

    public CharSequence getShortSupportMessage(ComponentName who) {
        CharSequence charSequence;
        if (!this.mHasFeature) {
            return null;
        }
        Preconditions.checkNotNull(who, "ComponentName is null");
        synchronized (getLockObject()) {
            charSequence = getActiveAdminForUidLocked(who, this.mInjector.binderGetCallingUid()).shortSupportMessage;
        }
        return charSequence;
    }

    public void setLongSupportMessage(ComponentName who, CharSequence message) {
        if (this.mHasFeature) {
            Preconditions.checkNotNull(who, "ComponentName is null");
            int userHandle = this.mInjector.userHandleGetCallingUserId();
            synchronized (getLockObject()) {
                ActiveAdmin admin = getActiveAdminForUidLocked(who, this.mInjector.binderGetCallingUid());
                if (!TextUtils.equals(admin.longSupportMessage, message)) {
                    admin.longSupportMessage = message;
                    saveSettingsLocked(userHandle);
                }
            }
            DevicePolicyEventLogger.createEvent(44).setAdmin(who).write();
        }
    }

    public CharSequence getLongSupportMessage(ComponentName who) {
        CharSequence charSequence;
        if (!this.mHasFeature) {
            return null;
        }
        Preconditions.checkNotNull(who, "ComponentName is null");
        synchronized (getLockObject()) {
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
            synchronized (getLockObject()) {
                ActiveAdmin admin = getActiveAdminUncheckedLocked(who, userHandle);
                if (admin == null) {
                    return null;
                }
                return admin.shortSupportMessage;
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
            synchronized (getLockObject()) {
                ActiveAdmin admin = getActiveAdminUncheckedLocked(who, userHandle);
                if (admin == null) {
                    return null;
                }
                return admin.longSupportMessage;
            }
        }
        throw new SecurityException("Only the system can query support message for user");
    }

    public void setOrganizationColor(ComponentName who, int color) {
        if (this.mHasFeature) {
            Preconditions.checkNotNull(who, "ComponentName is null");
            int userHandle = this.mInjector.userHandleGetCallingUserId();
            enforceManagedProfile(userHandle, "set organization color");
            synchronized (getLockObject()) {
                getActiveAdminForCallerLocked(who, -1).organizationColor = color;
                saveSettingsLocked(userHandle);
            }
            DevicePolicyEventLogger.createEvent(39).setAdmin(who).write();
        }
    }

    public void setOrganizationColorForUser(int color, int userId) {
        if (this.mHasFeature) {
            enforceFullCrossUsersPermission(userId);
            enforceManageUsers();
            enforceManagedProfile(userId, "set organization color");
            synchronized (getLockObject()) {
                getProfileOwnerAdminLocked(userId).organizationColor = color;
                saveSettingsLocked(userId);
            }
        }
    }

    public int getOrganizationColor(ComponentName who) {
        int i;
        if (!this.mHasFeature) {
            return ActiveAdmin.DEF_ORGANIZATION_COLOR;
        }
        Preconditions.checkNotNull(who, "ComponentName is null");
        enforceManagedProfile(this.mInjector.userHandleGetCallingUserId(), "get organization color");
        synchronized (getLockObject()) {
            i = getActiveAdminForCallerLocked(who, -1).organizationColor;
        }
        return i;
    }

    public int getOrganizationColorForUser(int userHandle) {
        int i;
        if (!this.mHasFeature) {
            return ActiveAdmin.DEF_ORGANIZATION_COLOR;
        }
        enforceFullCrossUsersPermission(userHandle);
        enforceManagedProfile(userHandle, "get organization color");
        synchronized (getLockObject()) {
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
        String str;
        if (this.mHasFeature) {
            Preconditions.checkNotNull(who, "ComponentName is null");
            int userHandle = this.mInjector.userHandleGetCallingUserId();
            synchronized (getLockObject()) {
                ActiveAdmin admin = getActiveAdminForCallerLocked(who, -1);
                if (!TextUtils.equals(admin.organizationName, text)) {
                    if (text != null) {
                        if (text.length() != 0) {
                            str = text.toString();
                            admin.organizationName = str;
                            saveSettingsLocked(userHandle);
                        }
                    }
                    str = null;
                    admin.organizationName = str;
                    saveSettingsLocked(userHandle);
                }
            }
        }
    }

    public CharSequence getOrganizationName(ComponentName who) {
        String str;
        if (!this.mHasFeature) {
            return null;
        }
        Preconditions.checkNotNull(who, "ComponentName is null");
        enforceManagedProfile(this.mInjector.userHandleGetCallingUserId(), "get organization name");
        synchronized (getLockObject()) {
            str = getActiveAdminForCallerLocked(who, -1).organizationName;
        }
        return str;
    }

    public CharSequence getDeviceOwnerOrganizationName() {
        String str = null;
        if (!this.mHasFeature) {
            return null;
        }
        enforceDeviceOwnerOrManageUsers();
        synchronized (getLockObject()) {
            ActiveAdmin deviceOwnerAdmin = getDeviceOwnerAdminLocked();
            if (deviceOwnerAdmin != null) {
                str = deviceOwnerAdmin.organizationName;
            }
        }
        return str;
    }

    public CharSequence getOrganizationNameForUser(int userHandle) {
        String str = null;
        if (!this.mHasFeature) {
            return null;
        }
        enforceFullCrossUsersPermission(userHandle);
        enforceManagedProfile(userHandle, "get organization name");
        synchronized (getLockObject()) {
            ActiveAdmin profileOwner = getProfileOwnerAdminLocked(userHandle);
            if (profileOwner != null) {
                str = profileOwner.organizationName;
            }
        }
        return str;
    }

    public List<String> setMeteredDataDisabledPackages(ComponentName who, List<String> packageNames) {
        List<String> excludedPkgs;
        Preconditions.checkNotNull(who);
        Preconditions.checkNotNull(packageNames);
        if (!this.mHasFeature) {
            return packageNames;
        }
        synchronized (getLockObject()) {
            ActiveAdmin admin = getActiveAdminForCallerLocked(who, -1);
            int callingUserId = this.mInjector.userHandleGetCallingUserId();
            long identity = this.mInjector.binderClearCallingIdentity();
            try {
                excludedPkgs = removeInvalidPkgsForMeteredDataRestriction(callingUserId, packageNames);
                admin.meteredDisabledPackages = packageNames;
                pushMeteredDisabledPackagesLocked(callingUserId);
                saveSettingsLocked(callingUserId);
            } finally {
                this.mInjector.binderRestoreCallingIdentity(identity);
            }
        }
        return excludedPkgs;
    }

    private List<String> removeInvalidPkgsForMeteredDataRestriction(int userId, List<String> pkgNames) {
        Set<String> activeAdmins = getActiveAdminPackagesLocked(userId);
        List<String> excludedPkgs = new ArrayList<>();
        for (int i = pkgNames.size() - 1; i >= 0; i--) {
            String pkgName = pkgNames.get(i);
            if (activeAdmins.contains(pkgName)) {
                excludedPkgs.add(pkgName);
            } else {
                try {
                    if (!this.mInjector.getIPackageManager().isPackageAvailable(pkgName, userId)) {
                        excludedPkgs.add(pkgName);
                    }
                } catch (RemoteException e) {
                    Slog.e(LOG_TAG, "cannot remove invalid pkgs for metered data restriction.");
                }
            }
        }
        pkgNames.removeAll(excludedPkgs);
        return excludedPkgs;
    }

    public List<String> getMeteredDataDisabledPackages(ComponentName who) {
        List<String> arrayList;
        Preconditions.checkNotNull(who);
        if (!this.mHasFeature) {
            return new ArrayList();
        }
        synchronized (getLockObject()) {
            ActiveAdmin admin = getActiveAdminForCallerLocked(who, -1);
            arrayList = admin.meteredDisabledPackages == null ? new ArrayList<>() : admin.meteredDisabledPackages;
        }
        return arrayList;
    }

    public boolean isMeteredDataDisabledPackageForUser(ComponentName who, String packageName, int userId) {
        Preconditions.checkNotNull(who);
        if (!this.mHasFeature) {
            return false;
        }
        if (isCallerWithSystemUid()) {
            synchronized (getLockObject()) {
                ActiveAdmin admin = getActiveAdminUncheckedLocked(who, userId);
                if (admin == null || admin.meteredDisabledPackages == null) {
                    return false;
                }
                return admin.meteredDisabledPackages.contains(packageName);
            }
        }
        throw new SecurityException("Only the system can query restricted pkgs for a specific user");
    }

    private boolean hasGrantProfileOwnerDevcieIdAccessPermission() {
        return this.mContext.checkCallingPermission("android.permission.GRANT_PROFILE_OWNER_DEVICE_IDS_ACCESS") == 0;
    }

    public void grantDeviceIdsAccessToProfileOwner(ComponentName who, int userId) {
        Preconditions.checkNotNull(who);
        if (this.mHasFeature) {
            if (!isCallerWithSystemUid() && !isAdb() && !hasGrantProfileOwnerDevcieIdAccessPermission()) {
                throw new SecurityException("Only the system can grant Device IDs access for a profile owner.");
            } else if (!isAdb() || !hasIncompatibleAccountsOrNonAdbNoLock(userId, who)) {
                synchronized (getLockObject()) {
                    if (isProfileOwner(who, userId)) {
                        Slog.i(LOG_TAG, String.format("Granting Device ID access to %s, for user %d", who.flattenToString(), Integer.valueOf(userId)));
                        this.mOwners.setProfileOwnerCanAccessDeviceIds(userId);
                    } else {
                        throw new IllegalArgumentException(String.format("Component %s is not a Profile Owner of user %d", who.flattenToString(), Integer.valueOf(userId)));
                    }
                }
            } else {
                throw new SecurityException("Can only be called from ADB if the device has no accounts.");
            }
        }
    }

    private void pushMeteredDisabledPackagesLocked(int userId) {
        this.mInjector.getNetworkPolicyManagerInternal().setMeteredRestrictedPackages(getMeteredDisabledPackagesLocked(userId), userId);
    }

    private Set<String> getMeteredDisabledPackagesLocked(int userId) {
        ActiveAdmin admin;
        ComponentName who = getOwnerComponent(userId);
        Set<String> restrictedPkgs = new ArraySet<>();
        if (!(who == null || (admin = getActiveAdminUncheckedLocked(who, userId)) == null || admin.meteredDisabledPackages == null)) {
            restrictedPkgs.addAll(admin.meteredDisabledPackages);
        }
        return restrictedPkgs;
    }

    public void setAffiliationIds(ComponentName admin, List<String> ids) {
        if (this.mHasFeature) {
            if (ids != null) {
                for (String id : ids) {
                    if (TextUtils.isEmpty(id)) {
                        throw new IllegalArgumentException("ids must not contain empty string");
                    }
                }
                Set<String> affiliationIds = new ArraySet<>(ids);
                int callingUserId = this.mInjector.userHandleGetCallingUserId();
                synchronized (getLockObject()) {
                    getActiveAdminForCallerLocked(admin, -1);
                    getUserData(callingUserId).mAffiliationIds = affiliationIds;
                    saveSettingsLocked(callingUserId);
                    if (callingUserId != 0 && isDeviceOwner(admin, callingUserId)) {
                        getUserData(0).mAffiliationIds = affiliationIds;
                        saveSettingsLocked(0);
                    }
                    maybePauseDeviceWideLoggingLocked();
                    maybeResumeDeviceWideLoggingLocked();
                    maybeClearLockTaskPolicyLocked();
                }
                return;
            }
            throw new IllegalArgumentException("ids must not be null");
        }
    }

    public List<String> getAffiliationIds(ComponentName admin) {
        ArrayList arrayList;
        if (!this.mHasFeature) {
            return Collections.emptyList();
        }
        Preconditions.checkNotNull(admin);
        synchronized (getLockObject()) {
            getActiveAdminForCallerLocked(admin, -1);
            arrayList = new ArrayList(getUserData(this.mInjector.userHandleGetCallingUserId()).mAffiliationIds);
        }
        return arrayList;
    }

    public boolean isAffiliatedUser() {
        boolean isUserAffiliatedWithDeviceLocked;
        if (!this.mHasFeature) {
            return false;
        }
        synchronized (getLockObject()) {
            isUserAffiliatedWithDeviceLocked = isUserAffiliatedWithDeviceLocked(this.mInjector.userHandleGetCallingUserId());
        }
        return isUserAffiliatedWithDeviceLocked;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
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
            for (int i = 0; i < userInfos.size(); i++) {
                int userId = userInfos.get(i).id;
                if (!isUserAffiliatedWithDeviceLocked(userId)) {
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

    public void setSecurityLoggingEnabled(ComponentName admin, boolean enabled) {
        if (this.mHasFeature) {
            Preconditions.checkNotNull(admin);
            synchronized (getLockObject()) {
                getActiveAdminForCallerLocked(admin, -2);
                if (enabled != this.mInjector.securityLogGetLoggingEnabledProperty()) {
                    this.mInjector.securityLogSetLoggingEnabledProperty(enabled);
                    if (enabled) {
                        this.mSecurityLogMonitor.start();
                        maybePauseDeviceWideLoggingLocked();
                    } else {
                        this.mSecurityLogMonitor.stop();
                    }
                    DevicePolicyEventLogger.createEvent(15).setAdmin(admin).setBoolean(enabled).write();
                }
            }
        }
    }

    public boolean isSecurityLoggingEnabled(ComponentName admin) {
        boolean securityLogGetLoggingEnabledProperty;
        if (!this.mHasFeature) {
            return false;
        }
        synchronized (getLockObject()) {
            if (!isCallerWithSystemUid()) {
                Preconditions.checkNotNull(admin);
                getActiveAdminForCallerLocked(admin, -2);
            }
            securityLogGetLoggingEnabledProperty = this.mInjector.securityLogGetLoggingEnabledProperty();
        }
        return securityLogGetLoggingEnabledProperty;
    }

    private void recordSecurityLogRetrievalTime() {
        synchronized (getLockObject()) {
            long currentTime = System.currentTimeMillis();
            DevicePolicyData policyData = getUserData(0);
            if (currentTime > policyData.mLastSecurityLogRetrievalTime) {
                policyData.mLastSecurityLogRetrievalTime = currentTime;
                saveSettingsLocked(0);
            }
        }
    }

    public ParceledListSlice<SecurityLog.SecurityEvent> retrievePreRebootSecurityLogs(ComponentName admin) {
        if (!this.mHasFeature) {
            return null;
        }
        Preconditions.checkNotNull(admin);
        ensureDeviceOwnerAndAllUsersAffiliated(admin);
        DevicePolicyEventLogger.createEvent(17).setAdmin(admin).write();
        if (!this.mContext.getResources().getBoolean(17891537) || !this.mInjector.securityLogGetLoggingEnabledProperty()) {
            return null;
        }
        recordSecurityLogRetrievalTime();
        ArrayList<SecurityLog.SecurityEvent> output = new ArrayList<>();
        try {
            SecurityLog.readPreviousEvents(output);
            return new ParceledListSlice<>(output);
        } catch (IOException e) {
            Slog.w(LOG_TAG, "Fail to read previous events", e);
            return new ParceledListSlice<>(Collections.emptyList());
        }
    }

    public ParceledListSlice<SecurityLog.SecurityEvent> retrieveSecurityLogs(ComponentName admin) {
        if (!this.mHasFeature) {
            return null;
        }
        Preconditions.checkNotNull(admin);
        ensureDeviceOwnerAndAllUsersAffiliated(admin);
        if (!this.mInjector.securityLogGetLoggingEnabledProperty()) {
            return null;
        }
        recordSecurityLogRetrievalTime();
        List<SecurityLog.SecurityEvent> logs = this.mSecurityLogMonitor.retrieveLogs();
        DevicePolicyEventLogger.createEvent(16).setAdmin(admin).write();
        if (logs != null) {
            return new ParceledListSlice<>(logs);
        }
        return null;
    }

    public long forceSecurityLogs() {
        enforceShell("forceSecurityLogs");
        if (this.mInjector.securityLogGetLoggingEnabledProperty()) {
            return this.mSecurityLogMonitor.forceLogs();
        }
        throw new IllegalStateException("logging is not available");
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
        Pair<String, Integer> packageUserPair = new Pair<>(packageName, Integer.valueOf(this.mInjector.userHandleGetCallingUserId()));
        synchronized (getLockObject()) {
            contains = this.mPackagesToRemove.contains(packageUserPair);
        }
        return contains;
    }

    public void uninstallPackageWithActiveAdmins(final String packageName) {
        enforceCanManageDeviceAdmin();
        Preconditions.checkArgument(!TextUtils.isEmpty(packageName));
        final int userId = this.mInjector.userHandleGetCallingUserId();
        enforceUserUnlocked(userId);
        ComponentName profileOwner = getProfileOwner(userId);
        if (profileOwner == null || !packageName.equals(profileOwner.getPackageName())) {
            ComponentName deviceOwner = getDeviceOwnerComponent(false);
            if (getDeviceOwnerUserId() != userId || deviceOwner == null || !packageName.equals(deviceOwner.getPackageName())) {
                Pair<String, Integer> packageUserPair = new Pair<>(packageName, Integer.valueOf(userId));
                synchronized (getLockObject()) {
                    this.mPackagesToRemove.add(packageUserPair);
                }
                List<ComponentName> allActiveAdmins = getActiveAdmins(userId);
                final List<ComponentName> packageActiveAdmins = new ArrayList<>();
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
                } else {
                    this.mHandler.postDelayed(new Runnable() {
                        /* class com.android.server.devicepolicy.DevicePolicyManagerService.AnonymousClass9 */

                        @Override // java.lang.Runnable
                        public void run() {
                            for (ComponentName activeAdmin : packageActiveAdmins) {
                                DevicePolicyManagerService.this.removeAdminArtifacts(activeAdmin, userId);
                            }
                            DevicePolicyManagerService.this.startUninstallIntent(packageName, userId);
                        }
                    }, JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY);
                }
            } else {
                throw new IllegalArgumentException("Cannot uninstall a package with a device owner");
            }
        } else {
            throw new IllegalArgumentException("Cannot uninstall a package with a profile owner");
        }
    }

    public boolean isDeviceProvisioned() {
        boolean z;
        enforceManageUsers();
        synchronized (getLockObject()) {
            z = getUserDataUnchecked(0).mUserSetupComplete;
        }
        return z;
    }

    private boolean isCurrentUserDemo() {
        if (!UserManager.isDeviceInDemoMode(this.mContext)) {
            return false;
        }
        int userId = this.mInjector.userHandleGetCallingUserId();
        long callingIdentity = this.mInjector.binderClearCallingIdentity();
        try {
            return this.mUserManager.getUserInfo(userId).isDemo();
        } finally {
            this.mInjector.binderRestoreCallingIdentity(callingIdentity);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void removePackageIfRequired(String packageName, int userId) {
        if (!packageHasActiveAdmins(packageName, userId)) {
            startUninstallIntent(packageName, userId);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startUninstallIntent(String packageName, int userId) {
        Pair<String, Integer> packageUserPair = new Pair<>(packageName, Integer.valueOf(userId));
        synchronized (getLockObject()) {
            if (this.mPackagesToRemove.contains(packageUserPair)) {
                this.mPackagesToRemove.remove(packageUserPair);
            } else {
                return;
            }
        }
        try {
            if (this.mInjector.getIPackageManager().getPackageInfo(packageName, 0, userId) == null) {
                return;
            }
        } catch (RemoteException e) {
            Log.e(LOG_TAG, "Failure talking to PackageManager while getting package info");
        }
        try {
            this.mInjector.getIActivityManager().forceStopPackage(packageName, userId);
        } catch (RemoteException e2) {
            Log.e(LOG_TAG, "Failure talking to ActivityManager while force stopping package");
        }
        Intent uninstallIntent = new Intent("android.intent.action.UNINSTALL_PACKAGE", Uri.parse("package:" + packageName));
        uninstallIntent.setFlags(268435456);
        this.mContext.startActivityAsUser(uninstallIntent, UserHandle.of(userId));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void removeAdminArtifacts(ComponentName adminReceiver, int userHandle) {
        synchronized (getLockObject()) {
            ActiveAdmin admin = getActiveAdminUncheckedLocked(adminReceiver, userHandle);
            if (admin != null) {
                DevicePolicyData policy = getUserData(userHandle);
                boolean doProxyCleanup = admin.info.usesPolicy(5);
                policy.mAdminList.remove(admin);
                policy.mAdminMap.remove(adminReceiver);
                validatePasswordOwnerLocked(policy);
                if (doProxyCleanup) {
                    resetGlobalProxyLocked(policy);
                }
                pushActiveAdminPackagesLocked(userHandle);
                pushMeteredDisabledPackagesLocked(userHandle);
                saveSettingsLocked(userHandle);
                updateMaximumTimeToLockLocked(userHandle);
                policy.mRemovingAdmins.remove(adminReceiver);
                Slog.i(LOG_TAG, "Device admin " + adminReceiver + " removed from user " + userHandle);
                if (this.mHwDevicePolicyManagerService != null) {
                    this.mHwDevicePolicyManagerService.syncHwDeviceSettingsLocked(policy.mUserHandle);
                    this.mHwDevicePolicyManagerService.removeActiveAdminCompleted(adminReceiver);
                }
                pushUserRestrictions(userHandle);
            }
        }
    }

    public void setDeviceProvisioningConfigApplied() {
        enforceManageUsers();
        synchronized (getLockObject()) {
            getUserData(0).mDeviceProvisioningConfigApplied = true;
            saveSettingsLocked(0);
        }
    }

    public boolean isDeviceProvisioningConfigApplied() {
        boolean z;
        enforceManageUsers();
        synchronized (getLockObject()) {
            z = getUserData(0).mDeviceProvisioningConfigApplied;
        }
        return z;
    }

    public void forceUpdateUserSetupComplete() {
        enforceCanManageProfileAndDeviceOwners();
        enforceCallerSystemUserHandle();
        if (this.mInjector.isBuildDebuggable()) {
            getUserData(0).mUserSetupComplete = this.mInjector.settingsSecureGetIntForUser("user_setup_complete", 0, 0) != 0;
            synchronized (getLockObject()) {
                saveSettingsLocked(0);
            }
        }
    }

    public void setBackupServiceEnabled(ComponentName admin, boolean enabled) {
        if (this.mHasFeature) {
            Preconditions.checkNotNull(admin);
            enforceProfileOrDeviceOwner(admin);
            toggleBackupServiceActive(this.mInjector.userHandleGetCallingUserId(), enabled);
        }
    }

    public boolean isBackupServiceEnabled(ComponentName admin) {
        Preconditions.checkNotNull(admin);
        boolean z = true;
        if (!this.mHasFeature) {
            return true;
        }
        enforceProfileOrDeviceOwner(admin);
        synchronized (getLockObject()) {
            try {
                IBackupManager ibm = this.mInjector.getIBackupManager();
                if (ibm == null || !ibm.isBackupServiceActive(this.mInjector.userHandleGetCallingUserId())) {
                    z = false;
                }
            } catch (RemoteException e) {
                throw new IllegalStateException("Failed requesting backup service state.", e);
            } catch (Throwable th) {
                throw th;
            }
        }
        return z;
    }

    public boolean bindDeviceAdminServiceAsUser(ComponentName admin, IApplicationThread caller, IBinder activtiyToken, Intent serviceIntent, IServiceConnection connection, int flags, int targetUserId) {
        String targetPackage;
        long callingIdentity;
        Throwable th;
        boolean z = false;
        if (!this.mHasFeature) {
            return false;
        }
        Preconditions.checkNotNull(admin);
        Preconditions.checkNotNull(caller);
        Preconditions.checkNotNull(serviceIntent);
        boolean z2 = (serviceIntent.getComponent() == null && serviceIntent.getPackage() == null) ? false : true;
        Preconditions.checkArgument(z2, "Service intent must be explicit (with a package name or component): " + serviceIntent);
        Preconditions.checkNotNull(connection);
        Preconditions.checkArgument(this.mInjector.userHandleGetCallingUserId() != targetUserId, "target user id must be different from the calling user id");
        if (getBindDeviceAdminTargetUsers(admin).contains(UserHandle.of(targetUserId))) {
            synchronized (getLockObject()) {
                targetPackage = getOwnerPackageNameForUserLocked(targetUserId);
            }
            long callingIdentity2 = this.mInjector.binderClearCallingIdentity();
            try {
                if (createCrossUserServiceIntent(serviceIntent, targetPackage, targetUserId) == null) {
                    this.mInjector.binderRestoreCallingIdentity(callingIdentity2);
                    return false;
                }
                callingIdentity = callingIdentity2;
                try {
                    if (this.mInjector.getIActivityManager().bindService(caller, activtiyToken, serviceIntent, serviceIntent.resolveTypeIfNeeded(this.mContext.getContentResolver()), connection, flags, this.mContext.getOpPackageName(), targetUserId) != 0) {
                        z = true;
                    }
                    this.mInjector.binderRestoreCallingIdentity(callingIdentity);
                    return z;
                } catch (RemoteException e) {
                    try {
                        Slog.e(LOG_TAG, "cannot validate and sanitize the incoming service intent.");
                        this.mInjector.binderRestoreCallingIdentity(callingIdentity);
                        return false;
                    } catch (Throwable th2) {
                        th = th2;
                        this.mInjector.binderRestoreCallingIdentity(callingIdentity);
                        throw th;
                    }
                }
            } catch (RemoteException e2) {
                callingIdentity = callingIdentity2;
                Slog.e(LOG_TAG, "cannot validate and sanitize the incoming service intent.");
                this.mInjector.binderRestoreCallingIdentity(callingIdentity);
                return false;
            } catch (Throwable th3) {
                th = th3;
                callingIdentity = callingIdentity2;
                this.mInjector.binderRestoreCallingIdentity(callingIdentity);
                throw th;
            }
        } else {
            throw new SecurityException("Not allowed to bind to target user id");
        }
    }

    public List<UserHandle> getBindDeviceAdminTargetUsers(ComponentName admin) {
        ArrayList<UserHandle> targetUsers;
        if (!this.mHasFeature) {
            return Collections.emptyList();
        }
        Preconditions.checkNotNull(admin);
        synchronized (getLockObject()) {
            getActiveAdminForCallerLocked(admin, -1);
            int callingUserId = this.mInjector.userHandleGetCallingUserId();
            long callingIdentity = this.mInjector.binderClearCallingIdentity();
            try {
                targetUsers = new ArrayList<>();
                if (isDeviceOwner(admin, callingUserId)) {
                    List<UserInfo> userInfos = this.mUserManager.getUsers(true);
                    for (int i = 0; i < userInfos.size(); i++) {
                        int userId = userInfos.get(i).id;
                        if (userId != callingUserId && canUserBindToDeviceOwnerLocked(userId)) {
                            targetUsers.add(UserHandle.of(userId));
                        }
                    }
                } else if (canUserBindToDeviceOwnerLocked(callingUserId)) {
                    targetUsers.add(UserHandle.of(this.mOwners.getDeviceOwnerUserId()));
                }
            } finally {
                this.mInjector.binderRestoreCallingIdentity(callingIdentity);
            }
        }
        return targetUsers;
    }

    private boolean canUserBindToDeviceOwnerLocked(int userId) {
        if (!this.mOwners.hasDeviceOwner() || userId == this.mOwners.getDeviceOwnerUserId() || !this.mOwners.hasProfileOwner(userId) || !TextUtils.equals(this.mOwners.getDeviceOwnerPackageName(), this.mOwners.getProfileOwnerPackage(userId))) {
            return false;
        }
        return isUserAffiliatedWithDeviceLocked(userId);
    }

    private boolean hasIncompatibleAccountsOrNonAdbNoLock(int userId, ComponentName owner) {
        int callingUid = this.mInjector.binderGetCallingUid();
        synchronized (this) {
            Slog.w(LOG_TAG, "hasIncompatibleAccountsOrNonAdbNoLock callingUid =" + callingUid);
            if (!isAdb() && this.mHwDevicePolicyManagerService != null && !this.mHwDevicePolicyManagerService.isMdmApiDeviceOwner()) {
                return true;
            }
        }
        wtfIfInLock();
        long token = this.mInjector.binderClearCallingIdentity();
        try {
            AccountManager am = AccountManager.get(this.mContext);
            Account[] accounts = am.getAccountsAsUser(userId);
            if (accounts.length == 0) {
                return false;
            }
            synchronized (getLockObject()) {
                if (owner != null) {
                    if (!isAdminTestOnlyLocked(owner, userId)) {
                    }
                }
                Log.w(LOG_TAG, "Non test-only owner can't be installed with existing accounts.");
                this.mInjector.binderRestoreCallingIdentity(token);
                return true;
            }
            String[] feature_allow = {"android.account.DEVICE_OR_PROFILE_OWNER_ALLOWED"};
            String[] feature_disallow = {"android.account.DEVICE_OR_PROFILE_OWNER_DISALLOWED"};
            boolean compatible = true;
            int length = accounts.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                }
                Account account = accounts[i];
                if (hasAccountFeatures(am, account, feature_disallow)) {
                    Log.e(LOG_TAG, account + " has " + feature_disallow[0]);
                    compatible = false;
                    break;
                } else if (!hasAccountFeatures(am, account, feature_allow)) {
                    Log.e(LOG_TAG, account + " doesn't have " + feature_allow[0]);
                    compatible = false;
                    break;
                } else {
                    i++;
                }
            }
            if (compatible) {
                Log.w(LOG_TAG, "All accounts are compatible");
            } else {
                Log.e(LOG_TAG, "Found incompatible accounts");
            }
            boolean z = !compatible;
            this.mInjector.binderRestoreCallingIdentity(token);
            return z;
        } finally {
            this.mInjector.binderRestoreCallingIdentity(token);
        }
    }

    private boolean hasAccountFeatures(AccountManager am, Account account, String[] features) {
        try {
            return am.hasFeatures(account, features, null, null).getResult().booleanValue();
        } catch (Exception e) {
            Log.w(LOG_TAG, "Failed to get account feature", e);
            return false;
        }
    }

    private boolean isAdb() {
        int callingUid = this.mInjector.binderGetCallingUid();
        return callingUid == 2000 || callingUid == 0;
    }

    public void setNetworkLoggingEnabled(ComponentName admin, String packageName, boolean enabled) {
        if (this.mHasFeature) {
            synchronized (getLockObject()) {
                enforceCanManageScope(admin, packageName, -2, "delegation-network-logging");
                if (enabled != isNetworkLoggingEnabledInternalLocked()) {
                    ActiveAdmin deviceOwner = getDeviceOwnerAdminLocked();
                    deviceOwner.isNetworkLoggingEnabled = enabled;
                    int i = 0;
                    if (!enabled) {
                        deviceOwner.numNetworkLoggingNotifications = 0;
                        deviceOwner.lastNetworkLoggingNotificationTimeMs = 0;
                    }
                    saveSettingsLocked(this.mInjector.userHandleGetCallingUserId());
                    setNetworkLoggingActiveInternal(enabled);
                    DevicePolicyEventLogger devicePolicyEventLogger = DevicePolicyEventLogger.createEvent(119).setAdmin(packageName).setBoolean(admin == null);
                    if (enabled) {
                        i = 1;
                    }
                    devicePolicyEventLogger.setInt(i).write();
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setNetworkLoggingActiveInternal(boolean active) {
        synchronized (getLockObject()) {
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
                    throw th;
                }
            } else {
                if (this.mNetworkLogger != null && !this.mNetworkLogger.stopNetworkLogging()) {
                    Slog.wtf(LOG_TAG, "Network logging could not be stopped due to the logging service not being available yet.");
                }
                this.mNetworkLogger = null;
                this.mInjector.getNotificationManager().cancel(1002);
            }
            this.mInjector.binderRestoreCallingIdentity(callingIdentity);
        }
    }

    public long forceNetworkLogs() {
        enforceShell("forceNetworkLogs");
        synchronized (getLockObject()) {
            if (!isNetworkLoggingEnabledInternalLocked()) {
                throw new IllegalStateException("logging is not available");
            } else if (this.mNetworkLogger == null) {
                return 0;
            } else {
                long ident = this.mInjector.binderClearCallingIdentity();
                try {
                    return this.mNetworkLogger.forceBatchFinalization();
                } finally {
                    this.mInjector.binderRestoreCallingIdentity(ident);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"getLockObject()"})
    private void maybePauseDeviceWideLoggingLocked() {
        if (!areAllUsersAffiliatedWithDeviceLocked()) {
            Slog.i(LOG_TAG, "There are unaffiliated users, security and network logging will be paused if enabled.");
            this.mSecurityLogMonitor.pause();
            NetworkLogger networkLogger = this.mNetworkLogger;
            if (networkLogger != null) {
                networkLogger.pause();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"getLockObject()"})
    private void maybeResumeDeviceWideLoggingLocked() {
        if (areAllUsersAffiliatedWithDeviceLocked()) {
            long ident = this.mInjector.binderClearCallingIdentity();
            try {
                this.mSecurityLogMonitor.resume();
                if (this.mNetworkLogger != null) {
                    this.mNetworkLogger.resume();
                }
            } finally {
                this.mInjector.binderRestoreCallingIdentity(ident);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"getLockObject()"})
    private void discardDeviceWideLogsLocked() {
        this.mSecurityLogMonitor.discardLogs();
        NetworkLogger networkLogger = this.mNetworkLogger;
        if (networkLogger != null) {
            networkLogger.discardLogs();
        }
    }

    public boolean isNetworkLoggingEnabled(ComponentName admin, String packageName) {
        boolean isNetworkLoggingEnabledInternalLocked;
        if (!this.mHasFeature) {
            return false;
        }
        synchronized (getLockObject()) {
            enforceCanManageScopeOrCheckPermission(admin, packageName, -2, "delegation-network-logging", "android.permission.MANAGE_USERS");
            isNetworkLoggingEnabledInternalLocked = isNetworkLoggingEnabledInternalLocked();
        }
        return isNetworkLoggingEnabledInternalLocked;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isNetworkLoggingEnabledInternalLocked() {
        ActiveAdmin deviceOwner = getDeviceOwnerAdminLocked();
        return deviceOwner != null && deviceOwner.isNetworkLoggingEnabled;
    }

    public List<NetworkEvent> retrieveNetworkLogs(ComponentName admin, String packageName, long batchToken) {
        if (!this.mHasFeature) {
            return null;
        }
        enforceCanManageScope(admin, packageName, -2, "delegation-network-logging");
        ensureAllUsersAffiliated();
        synchronized (getLockObject()) {
            if (this.mNetworkLogger != null) {
                if (isNetworkLoggingEnabledInternalLocked()) {
                    DevicePolicyEventLogger.createEvent(120).setAdmin(packageName).setBoolean(admin == null).write();
                    long currentTime = System.currentTimeMillis();
                    DevicePolicyData policyData = getUserData(0);
                    if (currentTime > policyData.mLastNetworkLogsRetrievalTime) {
                        policyData.mLastNetworkLogsRetrievalTime = currentTime;
                        saveSettingsLocked(0);
                    }
                    return this.mNetworkLogger.retrieveLogs(batchToken);
                }
            }
            return null;
        }
    }

    private void sendNetworkLoggingNotificationLocked() {
        ActiveAdmin deviceOwner = getDeviceOwnerAdminLocked();
        if (deviceOwner != null && deviceOwner.isNetworkLoggingEnabled && deviceOwner.numNetworkLoggingNotifications < 2) {
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
                this.mInjector.getNotificationManager().notify(1002, new Notification.Builder(this.mContext, SystemNotificationChannels.DEVICE_ADMIN).setSmallIcon(17302439).setContentTitle(this.mContext.getString(17040636)).setContentText(this.mContext.getString(17040635)).setTicker(this.mContext.getString(17040636)).setShowWhen(true).setContentIntent(PendingIntent.getBroadcastAsUser(this.mContext, 0, intent, 0, UserHandle.CURRENT)).setStyle(new Notification.BigTextStyle().bigText(this.mContext.getString(17040635))).build());
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
        } else if (!info.serviceInfo.exported || "android.permission.BIND_DEVICE_ADMIN".equals(info.serviceInfo.permission)) {
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
        if (!this.mHasFeature || !this.mLockPatternUtils.hasSecureLockScreen()) {
            return false;
        }
        if (token == null || token.length < 32) {
            throw new IllegalArgumentException("token must be at least 32-byte long");
        }
        synchronized (getLockObject()) {
            int userHandle = this.mInjector.userHandleGetCallingUserId();
            getActiveAdminForCallerLocked(admin, -1);
            DevicePolicyData policy = getUserData(userHandle);
            long ident = this.mInjector.binderClearCallingIdentity();
            try {
                if (policy.mPasswordTokenHandle != 0) {
                    this.mLockPatternUtils.removeEscrowToken(policy.mPasswordTokenHandle, userHandle);
                }
                policy.mPasswordTokenHandle = this.mLockPatternUtils.addEscrowToken(token, userHandle, (LockPatternUtils.EscrowTokenStateChangeCallback) null);
                saveSettingsLocked(userHandle);
                if (policy.mPasswordTokenHandle != 0) {
                    z = true;
                }
            } finally {
                this.mInjector.binderRestoreCallingIdentity(ident);
            }
        }
        return z;
    }

    public boolean clearResetPasswordToken(ComponentName admin) {
        if (!this.mHasFeature || !this.mLockPatternUtils.hasSecureLockScreen()) {
            return false;
        }
        synchronized (getLockObject()) {
            int userHandle = this.mInjector.userHandleGetCallingUserId();
            getActiveAdminForCallerLocked(admin, -1);
            DevicePolicyData policy = getUserData(userHandle);
            if (policy.mPasswordTokenHandle == 0) {
                return false;
            }
            long ident = this.mInjector.binderClearCallingIdentity();
            try {
                boolean result = this.mLockPatternUtils.removeEscrowToken(policy.mPasswordTokenHandle, userHandle);
                policy.mPasswordTokenHandle = 0;
                saveSettingsLocked(userHandle);
                return result;
            } finally {
                this.mInjector.binderRestoreCallingIdentity(ident);
            }
        }
    }

    public boolean isResetPasswordTokenActive(ComponentName admin) {
        if (!this.mHasFeature || !this.mLockPatternUtils.hasSecureLockScreen()) {
            return false;
        }
        synchronized (getLockObject()) {
            int userHandle = this.mInjector.userHandleGetCallingUserId();
            getActiveAdminForCallerLocked(admin, -1);
            DevicePolicyData policy = getUserData(userHandle);
            if (policy.mPasswordTokenHandle == 0) {
                return false;
            }
            long ident = this.mInjector.binderClearCallingIdentity();
            try {
                return this.mLockPatternUtils.isEscrowTokenActive(policy.mPasswordTokenHandle, userHandle);
            } finally {
                this.mInjector.binderRestoreCallingIdentity(ident);
            }
        }
    }

    public boolean resetPasswordWithToken(ComponentName admin, String passwordOrNull, byte[] token, int flags) {
        if (this.mHasFeature) {
            if (this.mLockPatternUtils.hasSecureLockScreen()) {
                Preconditions.checkNotNull(token);
                synchronized (getLockObject()) {
                    try {
                        int userHandle = this.mInjector.userHandleGetCallingUserId();
                        getActiveAdminForCallerLocked(admin, -1);
                        DevicePolicyData policy = getUserData(userHandle);
                        if (policy.mPasswordTokenHandle != 0) {
                            return resetPasswordInternal(passwordOrNull != null ? passwordOrNull : "", policy.mPasswordTokenHandle, token, flags, this.mInjector.binderGetCallingUid(), userHandle);
                        }
                        Slog.w(LOG_TAG, "No saved token handle");
                        return false;
                    } catch (Throwable th) {
                        th = th;
                        throw th;
                    }
                }
            }
        }
        return false;
    }

    public boolean isCurrentInputMethodSetByOwner() {
        enforceProfileOwnerOrSystemUser();
        return getUserData(this.mInjector.userHandleGetCallingUserId()).mCurrentInputMethodSet;
    }

    public StringParceledListSlice getOwnerInstalledCaCerts(UserHandle user) {
        StringParceledListSlice stringParceledListSlice;
        int userId = user.getIdentifier();
        enforceProfileOwnerOrFullCrossUsersPermission(userId);
        synchronized (getLockObject()) {
            stringParceledListSlice = new StringParceledListSlice(new ArrayList(getUserData(userId).mOwnerInstalledCaCerts));
        }
        return stringParceledListSlice;
    }

    public void clearApplicationUserData(ComponentName admin, String packageName, IPackageDataObserver callback) {
        Preconditions.checkNotNull(admin, "ComponentName is null");
        Preconditions.checkNotNull(packageName, "packageName is null");
        Preconditions.checkNotNull(callback, "callback is null");
        enforceProfileOrDeviceOwner(admin);
        int userId = UserHandle.getCallingUserId();
        long ident = this.mInjector.binderClearCallingIdentity();
        try {
            ActivityManager.getService().clearApplicationUserData(packageName, false, callback, userId);
        } catch (RemoteException e) {
            Slog.e(LOG_TAG, "cannot clear application user data.");
        } catch (SecurityException se) {
            Slog.w(LOG_TAG, "Not allowed to clear application user data for package " + packageName, se);
            try {
                callback.onRemoveCompleted(packageName, false);
            } catch (RemoteException e2) {
                Slog.e(LOG_TAG, "cannot on remove completed.");
            }
        } catch (Throwable th) {
            this.mInjector.binderRestoreCallingIdentity(ident);
            throw th;
        }
        this.mInjector.binderRestoreCallingIdentity(ident);
    }

    public void setLogoutEnabled(ComponentName admin, boolean enabled) {
        if (this.mHasFeature) {
            Preconditions.checkNotNull(admin);
            synchronized (getLockObject()) {
                ActiveAdmin deviceOwner = getActiveAdminForCallerLocked(admin, -2);
                if (deviceOwner.isLogoutEnabled != enabled) {
                    deviceOwner.isLogoutEnabled = enabled;
                    saveSettingsLocked(this.mInjector.userHandleGetCallingUserId());
                }
            }
        }
    }

    public boolean isLogoutEnabled() {
        boolean z = false;
        if (!this.mHasFeature) {
            return false;
        }
        synchronized (getLockObject()) {
            ActiveAdmin deviceOwner = getDeviceOwnerAdminLocked();
            if (deviceOwner != null && deviceOwner.isLogoutEnabled) {
                z = true;
            }
        }
        return z;
    }

    public List<String> getDisallowedSystemApps(ComponentName admin, int userId, String provisioningAction) throws RemoteException {
        enforceCanManageProfileAndDeviceOwners();
        return new ArrayList(this.mOverlayPackagesProvider.getNonRequiredApps(admin, userId, provisioningAction));
    }

    public void transferOwnership(ComponentName admin, ComponentName target, PersistableBundle bundle) {
        Throwable th;
        Throwable th2;
        PersistableBundle bundle2;
        String ownerType;
        if (this.mHasFeature) {
            Preconditions.checkNotNull(admin, "Admin cannot be null.");
            Preconditions.checkNotNull(target, "Target cannot be null.");
            enforceProfileOrDeviceOwner(admin);
            if (admin.equals(target)) {
                throw new IllegalArgumentException("Provided administrator and target are the same object.");
            } else if (!admin.getPackageName().equals(target.getPackageName())) {
                int callingUserId = this.mInjector.userHandleGetCallingUserId();
                DevicePolicyData policy = getUserData(callingUserId);
                DeviceAdminInfo incomingDeviceInfo = findAdmin(target, callingUserId, true);
                checkActiveAdminPrecondition(target, incomingDeviceInfo, policy);
                if (incomingDeviceInfo.supportsTransferOwnership()) {
                    long id = this.mInjector.binderClearCallingIdentity();
                    try {
                        synchronized (getLockObject()) {
                            if (bundle == null) {
                                try {
                                    bundle2 = new PersistableBundle();
                                } catch (Throwable th3) {
                                    th2 = th3;
                                }
                            } else {
                                bundle2 = bundle;
                            }
                            try {
                                if (isProfileOwner(admin, callingUserId)) {
                                    ownerType = LOG_TAG_PROFILE_OWNER;
                                    try {
                                        prepareTransfer(admin, target, bundle2, callingUserId, LOG_TAG_PROFILE_OWNER);
                                        transferProfileOwnershipLocked(admin, target, callingUserId);
                                        sendProfileOwnerCommand("android.app.action.TRANSFER_OWNERSHIP_COMPLETE", getTransferOwnershipAdminExtras(bundle2), callingUserId);
                                        postTransfer("android.app.action.PROFILE_OWNER_CHANGED", callingUserId);
                                        if (isUserAffiliatedWithDeviceLocked(callingUserId)) {
                                            notifyAffiliatedProfileTransferOwnershipComplete(callingUserId);
                                        }
                                    } catch (Throwable th4) {
                                        th2 = th4;
                                    }
                                } else {
                                    try {
                                        if (isDeviceOwner(admin, callingUserId)) {
                                            ownerType = LOG_TAG_DEVICE_OWNER;
                                            prepareTransfer(admin, target, bundle2, callingUserId, LOG_TAG_DEVICE_OWNER);
                                            transferDeviceOwnershipLocked(admin, target, callingUserId);
                                            sendDeviceOwnerCommand("android.app.action.TRANSFER_OWNERSHIP_COMPLETE", getTransferOwnershipAdminExtras(bundle2));
                                            postTransfer("android.app.action.DEVICE_OWNER_CHANGED", callingUserId);
                                        } else {
                                            ownerType = null;
                                        }
                                    } catch (Throwable th5) {
                                        th2 = th5;
                                    }
                                }
                                this.mInjector.binderRestoreCallingIdentity(id);
                                DevicePolicyEventLogger.createEvent(58).setAdmin(admin).setStrings(new String[]{target.getPackageName(), ownerType}).write();
                                return;
                            } catch (Throwable th6) {
                                th2 = th6;
                            }
                        }
                        try {
                            throw th2;
                        } catch (Throwable th7) {
                            th = th7;
                        }
                    } catch (Throwable th8) {
                        th = th8;
                        this.mInjector.binderRestoreCallingIdentity(id);
                        throw th;
                    }
                } else {
                    throw new IllegalArgumentException("Provided target does not support ownership transfer.");
                }
            } else {
                throw new IllegalArgumentException("Provided administrator and target have the same package name.");
            }
        }
    }

    private void prepareTransfer(ComponentName admin, ComponentName target, PersistableBundle bundle, int callingUserId, String adminType) {
        saveTransferOwnershipBundleLocked(bundle, callingUserId);
        this.mTransferOwnershipMetadataManager.saveMetadataFile(new TransferOwnershipMetadataManager.Metadata(admin, target, callingUserId, adminType));
    }

    private void postTransfer(String broadcast, int callingUserId) {
        deleteTransferOwnershipMetadataFileLocked();
        sendOwnerChangedBroadcast(broadcast, callingUserId);
    }

    private void notifyAffiliatedProfileTransferOwnershipComplete(int callingUserId) {
        Bundle extras = new Bundle();
        extras.putParcelable("android.intent.extra.USER", UserHandle.of(callingUserId));
        sendDeviceOwnerCommand("android.app.action.AFFILIATED_PROFILE_TRANSFER_OWNERSHIP_COMPLETE", extras);
    }

    private void transferProfileOwnershipLocked(ComponentName admin, ComponentName target, int profileOwnerUserId) {
        transferActiveAdminUncheckedLocked(target, admin, profileOwnerUserId);
        this.mOwners.transferProfileOwner(target, profileOwnerUserId);
        Slog.i(LOG_TAG, "Profile owner set: " + target + " on user " + profileOwnerUserId);
        this.mOwners.writeProfileOwner(profileOwnerUserId);
        this.mDeviceAdminServiceController.startServiceForOwner(target.getPackageName(), profileOwnerUserId, "transfer-profile-owner");
    }

    private void transferDeviceOwnershipLocked(ComponentName admin, ComponentName target, int userId) {
        transferActiveAdminUncheckedLocked(target, admin, userId);
        this.mOwners.transferDeviceOwnership(target);
        Slog.i(LOG_TAG, "Device owner set: " + target + " on user " + userId);
        this.mOwners.writeDeviceOwner();
        this.mDeviceAdminServiceController.startServiceForOwner(target.getPackageName(), userId, "transfer-device-owner");
    }

    private Bundle getTransferOwnershipAdminExtras(PersistableBundle bundle) {
        Bundle extras = new Bundle();
        if (bundle != null) {
            extras.putParcelable("android.app.extra.TRANSFER_OWNERSHIP_ADMIN_EXTRAS_BUNDLE", bundle);
        }
        return extras;
    }

    public void setStartUserSessionMessage(ComponentName admin, CharSequence startUserSessionMessage) {
        if (this.mHasFeature) {
            Preconditions.checkNotNull(admin);
            String startUserSessionMessageString = startUserSessionMessage != null ? startUserSessionMessage.toString() : null;
            synchronized (getLockObject()) {
                ActiveAdmin deviceOwner = getActiveAdminForCallerLocked(admin, -2);
                if (!TextUtils.equals(deviceOwner.startUserSessionMessage, startUserSessionMessage)) {
                    deviceOwner.startUserSessionMessage = startUserSessionMessageString;
                    saveSettingsLocked(this.mInjector.userHandleGetCallingUserId());
                    this.mInjector.getActivityManagerInternal().setSwitchingFromSystemUserMessage(startUserSessionMessageString);
                }
            }
        }
    }

    public void setEndUserSessionMessage(ComponentName admin, CharSequence endUserSessionMessage) {
        if (this.mHasFeature) {
            Preconditions.checkNotNull(admin);
            String endUserSessionMessageString = endUserSessionMessage != null ? endUserSessionMessage.toString() : null;
            synchronized (getLockObject()) {
                ActiveAdmin deviceOwner = getActiveAdminForCallerLocked(admin, -2);
                if (!TextUtils.equals(deviceOwner.endUserSessionMessage, endUserSessionMessage)) {
                    deviceOwner.endUserSessionMessage = endUserSessionMessageString;
                    saveSettingsLocked(this.mInjector.userHandleGetCallingUserId());
                    this.mInjector.getActivityManagerInternal().setSwitchingToSystemUserMessage(endUserSessionMessageString);
                }
            }
        }
    }

    public String getStartUserSessionMessage(ComponentName admin) {
        String str;
        if (!this.mHasFeature) {
            return null;
        }
        Preconditions.checkNotNull(admin);
        synchronized (getLockObject()) {
            str = getActiveAdminForCallerLocked(admin, -2).startUserSessionMessage;
        }
        return str;
    }

    public String getEndUserSessionMessage(ComponentName admin) {
        String str;
        if (!this.mHasFeature) {
            return null;
        }
        Preconditions.checkNotNull(admin);
        synchronized (getLockObject()) {
            str = getActiveAdminForCallerLocked(admin, -2).endUserSessionMessage;
        }
        return str;
    }

    private void deleteTransferOwnershipMetadataFileLocked() {
        this.mTransferOwnershipMetadataManager.deleteMetadataFile();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0040, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0041, code lost:
        $closeResource(r5, r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0044, code lost:
        throw r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0045, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0046, code lost:
        android.util.Slog.e(com.android.server.devicepolicy.DevicePolicyManagerService.LOG_TAG, "Caught exception while trying to load the owner transfer parameters from file " + r2, r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x005d, code lost:
        return null;
     */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0045 A[ExcHandler: IOException | IllegalArgumentException | XmlPullParserException (r4v4 'e' java.lang.Exception A[CUSTOM_DECLARE]), Splitter:B:10:0x0039] */
    public PersistableBundle getTransferOwnershipBundle() {
        synchronized (getLockObject()) {
            int callingUserId = this.mInjector.userHandleGetCallingUserId();
            getActiveAdminForCallerLocked(null, -1);
            File bundleFile = new File(this.mInjector.environmentGetUserSystemDirectory(callingUserId), TRANSFER_OWNERSHIP_PARAMETERS_XML);
            if (!bundleFile.exists()) {
                return null;
            }
            FileInputStream stream = new FileInputStream(bundleFile);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(stream, null);
            parser.next();
            PersistableBundle restoreFromXml = PersistableBundle.restoreFromXml(parser);
            try {
                $closeResource(null, stream);
                return restoreFromXml;
            } catch (IOException | IllegalArgumentException | XmlPullParserException e) {
            }
        }
    }

    public int addOverrideApn(ComponentName who, ApnSetting apnSetting) {
        if (!this.mHasFeature || !this.mHasTelephonyFeature) {
            return -1;
        }
        Preconditions.checkNotNull(who, "ComponentName is null in addOverrideApn");
        Preconditions.checkNotNull(apnSetting, "ApnSetting is null in addOverrideApn");
        enforceDeviceOwner(who);
        long id = this.mInjector.binderClearCallingIdentity();
        try {
            Uri resultUri = this.mContext.getContentResolver().insert(Telephony.Carriers.DPC_URI, apnSetting.toContentValues());
            if (resultUri == null) {
                return -1;
            }
            try {
                return Integer.parseInt(resultUri.getLastPathSegment());
            } catch (NumberFormatException e) {
                Slog.e(LOG_TAG, "Failed to parse inserted override APN id.", e);
                return -1;
            }
        } finally {
            this.mInjector.binderRestoreCallingIdentity(id);
        }
    }

    public boolean updateOverrideApn(ComponentName who, int apnId, ApnSetting apnSetting) {
        boolean z = false;
        if (!this.mHasFeature || !this.mHasTelephonyFeature) {
            return false;
        }
        Preconditions.checkNotNull(who, "ComponentName is null in updateOverrideApn");
        Preconditions.checkNotNull(apnSetting, "ApnSetting is null in updateOverrideApn");
        enforceDeviceOwner(who);
        if (apnId < 0) {
            return false;
        }
        long id = this.mInjector.binderClearCallingIdentity();
        try {
            if (this.mContext.getContentResolver().update(Uri.withAppendedPath(Telephony.Carriers.DPC_URI, Integer.toString(apnId)), apnSetting.toContentValues(), null, null) > 0) {
                z = true;
            }
            return z;
        } finally {
            this.mInjector.binderRestoreCallingIdentity(id);
        }
    }

    public boolean removeOverrideApn(ComponentName who, int apnId) {
        if (!this.mHasFeature || !this.mHasTelephonyFeature) {
            return false;
        }
        Preconditions.checkNotNull(who, "ComponentName is null in removeOverrideApn");
        enforceDeviceOwner(who);
        return removeOverrideApnUnchecked(apnId);
    }

    private boolean removeOverrideApnUnchecked(int apnId) {
        if (apnId < 0) {
            return false;
        }
        long id = this.mInjector.binderClearCallingIdentity();
        try {
            if (this.mContext.getContentResolver().delete(Uri.withAppendedPath(Telephony.Carriers.DPC_URI, Integer.toString(apnId)), null, null) > 0) {
                return true;
            }
            return false;
        } finally {
            this.mInjector.binderRestoreCallingIdentity(id);
        }
    }

    public List<ApnSetting> getOverrideApns(ComponentName who) {
        if (!this.mHasFeature || !this.mHasTelephonyFeature) {
            return Collections.emptyList();
        }
        Preconditions.checkNotNull(who, "ComponentName is null in getOverrideApns");
        enforceDeviceOwner(who);
        return getOverrideApnsUnchecked();
    }

    private List<ApnSetting> getOverrideApnsUnchecked() {
        long id = this.mInjector.binderClearCallingIdentity();
        try {
            Cursor cursor = this.mContext.getContentResolver().query(Telephony.Carriers.DPC_URI, null, null, null, null);
            if (cursor == null) {
                return Collections.emptyList();
            }
            try {
                List<ApnSetting> apnList = new ArrayList<>();
                cursor.moveToPosition(-1);
                while (cursor.moveToNext()) {
                    apnList.add(ApnSetting.makeApnSetting(cursor));
                }
                return apnList;
            } finally {
                cursor.close();
            }
        } finally {
            this.mInjector.binderRestoreCallingIdentity(id);
        }
    }

    public void setOverrideApnsEnabled(ComponentName who, boolean enabled) {
        if (this.mHasFeature && this.mHasTelephonyFeature) {
            Preconditions.checkNotNull(who, "ComponentName is null in setOverrideApnEnabled");
            enforceDeviceOwner(who);
            setOverrideApnsEnabledUnchecked(enabled);
        }
    }

    private void setOverrideApnsEnabledUnchecked(boolean enabled) {
        ContentValues value = new ContentValues();
        value.put("enforced", Boolean.valueOf(enabled));
        long id = this.mInjector.binderClearCallingIdentity();
        try {
            this.mContext.getContentResolver().update(Telephony.Carriers.ENFORCE_MANAGED_URI, value, null, null);
        } finally {
            this.mInjector.binderRestoreCallingIdentity(id);
        }
    }

    public boolean isOverrideApnEnabled(ComponentName who) {
        boolean z = false;
        if (!this.mHasFeature || !this.mHasTelephonyFeature) {
            return false;
        }
        Preconditions.checkNotNull(who, "ComponentName is null in isOverrideApnEnabled");
        enforceDeviceOwner(who);
        long id = this.mInjector.binderClearCallingIdentity();
        try {
            Cursor enforceCursor = this.mContext.getContentResolver().query(Telephony.Carriers.ENFORCE_MANAGED_URI, null, null, null, null);
            if (enforceCursor == null) {
                return false;
            }
            try {
                if (enforceCursor.moveToFirst()) {
                    if (enforceCursor.getInt(enforceCursor.getColumnIndex("enforced")) == 1) {
                        z = true;
                    }
                    enforceCursor.close();
                    return z;
                }
            } catch (IllegalArgumentException e) {
                Slog.e(LOG_TAG, "Cursor returned from ENFORCE_MANAGED_URI doesn't contain correct info.", e);
            } catch (Throwable th) {
                enforceCursor.close();
                throw th;
            }
            enforceCursor.close();
            return false;
        } finally {
            this.mInjector.binderRestoreCallingIdentity(id);
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void saveTransferOwnershipBundleLocked(PersistableBundle bundle, int userId) {
        File parametersFile = new File(this.mInjector.environmentGetUserSystemDirectory(userId), TRANSFER_OWNERSHIP_PARAMETERS_XML);
        AtomicFile atomicFile = new AtomicFile(parametersFile);
        FileOutputStream stream = null;
        try {
            stream = atomicFile.startWrite();
            XmlSerializer serializer = new FastXmlSerializer();
            serializer.setOutput(stream, StandardCharsets.UTF_8.name());
            serializer.startDocument(null, true);
            serializer.startTag(null, TAG_TRANSFER_OWNERSHIP_BUNDLE);
            bundle.saveToXml(serializer);
            serializer.endTag(null, TAG_TRANSFER_OWNERSHIP_BUNDLE);
            serializer.endDocument();
            atomicFile.finishWrite(stream);
        } catch (IOException | XmlPullParserException e) {
            Slog.e(LOG_TAG, "Caught exception while trying to save the owner transfer parameters to file " + parametersFile, e);
            parametersFile.delete();
            atomicFile.failWrite(stream);
        }
    }

    /* access modifiers changed from: package-private */
    public void deleteTransferOwnershipBundleLocked(int userId) {
        new File(this.mInjector.environmentGetUserSystemDirectory(userId), TRANSFER_OWNERSHIP_PARAMETERS_XML).delete();
    }

    /* access modifiers changed from: protected */
    public void clearWipeDataFactoryLowlevel(String reason, boolean wipeEuicc) {
        try {
            this.mInjector.recoverySystemRebootWipeUserData(false, reason, true, wipeEuicc);
            if (1 != 0) {
                return;
            }
        } catch (IOException | SecurityException e) {
            Slog.w(LOG_TAG, "Failed requesting data wipe", e);
            if (0 != 0) {
                return;
            }
        } catch (Throwable th) {
            if (0 == 0) {
                SecurityLog.writeEvent(210023, new Object[0]);
            }
            throw th;
        }
        SecurityLog.writeEvent(210023, new Object[0]);
    }

    private void maybeLogPasswordComplexitySet(ComponentName who, int userId, boolean parent, PasswordMetrics metrics) {
        if (SecurityLog.isLoggingEnabled()) {
            SecurityLog.writeEvent(210017, new Object[]{who.getPackageName(), Integer.valueOf(userId), Integer.valueOf(parent ? getProfileParentId(userId) : userId), Integer.valueOf(metrics.length), Integer.valueOf(metrics.quality), Integer.valueOf(metrics.letters), Integer.valueOf(metrics.nonLetter), Integer.valueOf(metrics.numeric), Integer.valueOf(metrics.upperCase), Integer.valueOf(metrics.lowerCase), Integer.valueOf(metrics.symbols)});
            Log.i(MDPP_TAG, "TAG 210017");
        }
    }

    /* access modifiers changed from: private */
    public static String getManagedProvisioningPackage(Context context) {
        return context.getResources().getString(17039870);
    }

    private void putPrivateDnsSettings(String mode, String host) {
        long origId = this.mInjector.binderClearCallingIdentity();
        try {
            this.mInjector.settingsGlobalPutString("private_dns_mode", mode);
            this.mInjector.settingsGlobalPutString("private_dns_specifier", host);
        } finally {
            this.mInjector.binderRestoreCallingIdentity(origId);
        }
    }

    public int setGlobalPrivateDns(ComponentName who, int mode, String privateDnsHost) {
        if (!this.mHasFeature) {
            return 2;
        }
        Preconditions.checkNotNull(who, "ComponentName is null");
        enforceDeviceOwner(who);
        if (mode != 2) {
            if (mode != 3) {
                throw new IllegalArgumentException(String.format("Provided mode, %d, is not a valid mode.", Integer.valueOf(mode)));
            } else if (TextUtils.isEmpty(privateDnsHost) || !NetworkUtils.isWeaklyValidatedHostname(privateDnsHost)) {
                throw new IllegalArgumentException(String.format("Provided hostname %s is not valid", privateDnsHost));
            } else {
                putPrivateDnsSettings("hostname", privateDnsHost);
                return 0;
            }
        } else if (TextUtils.isEmpty(privateDnsHost)) {
            putPrivateDnsSettings("opportunistic", null);
            return 0;
        } else {
            throw new IllegalArgumentException("Host provided for opportunistic mode, but is not needed.");
        }
    }

    public int getGlobalPrivateDnsMode(ComponentName who) {
        if (!this.mHasFeature) {
            return 0;
        }
        Preconditions.checkNotNull(who, "ComponentName is null");
        enforceDeviceOwner(who);
        String currentMode = this.mInjector.settingsGlobalGetString("private_dns_mode");
        if (currentMode == null) {
            currentMode = "opportunistic";
        }
        char c = 65535;
        int hashCode = currentMode.hashCode();
        if (hashCode != -539229175) {
            if (hashCode != -299803597) {
                if (hashCode == 109935 && currentMode.equals("off")) {
                    c = 0;
                }
            } else if (currentMode.equals("hostname")) {
                c = 2;
            }
        } else if (currentMode.equals("opportunistic")) {
            c = 1;
        }
        if (c == 0) {
            return 1;
        }
        if (c == 1) {
            return 2;
        }
        if (c != 2) {
            return 0;
        }
        return 3;
    }

    public String getGlobalPrivateDnsHost(ComponentName who) {
        if (!this.mHasFeature) {
            return null;
        }
        Preconditions.checkNotNull(who, "ComponentName is null");
        enforceDeviceOwner(who);
        return this.mInjector.settingsGlobalGetString("private_dns_specifier");
    }

    public void installUpdateFromFile(ComponentName admin, ParcelFileDescriptor updateFileDescriptor, StartInstallingUpdateCallback callback) {
        UpdateInstaller updateInstaller;
        DevicePolicyEventLogger.createEvent(73).setAdmin(admin).setBoolean(isDeviceAB()).write();
        enforceDeviceOwner(admin);
        long id = this.mInjector.binderClearCallingIdentity();
        try {
            if (isDeviceAB()) {
                updateInstaller = new AbUpdateInstaller(this.mContext, updateFileDescriptor, callback, this.mInjector, this.mConstants);
            } else {
                updateInstaller = new NonAbUpdateInstaller(this.mContext, updateFileDescriptor, callback, this.mInjector, this.mConstants);
            }
            updateInstaller.startInstallUpdate();
        } finally {
            this.mInjector.binderRestoreCallingIdentity(id);
        }
    }

    private boolean isDeviceAB() {
        return "true".equalsIgnoreCase(SystemProperties.get(AB_DEVICE_KEY, ""));
    }

    public void setCrossProfileCalendarPackages(ComponentName who, List<String> packageNames) {
        String[] strArr;
        if (this.mHasFeature) {
            Preconditions.checkNotNull(who, "ComponentName is null");
            synchronized (getLockObject()) {
                getActiveAdminForCallerLocked(who, -1).mCrossProfileCalendarPackages = packageNames;
                saveSettingsLocked(this.mInjector.userHandleGetCallingUserId());
            }
            DevicePolicyEventLogger admin = DevicePolicyEventLogger.createEvent(70).setAdmin(who);
            if (packageNames == null) {
                strArr = null;
            } else {
                strArr = (String[]) packageNames.toArray(new String[packageNames.size()]);
            }
            admin.setStrings(strArr).write();
        }
    }

    public List<String> getCrossProfileCalendarPackages(ComponentName who) {
        List<String> list;
        if (!this.mHasFeature) {
            return Collections.emptyList();
        }
        Preconditions.checkNotNull(who, "ComponentName is null");
        synchronized (getLockObject()) {
            list = getActiveAdminForCallerLocked(who, -1).mCrossProfileCalendarPackages;
        }
        return list;
    }

    public boolean isPackageAllowedToAccessCalendarForUser(String packageName, int userHandle) {
        if (!this.mHasFeature) {
            return false;
        }
        Preconditions.checkStringNotEmpty(packageName, "Package name is null or empty");
        enforceCrossUsersPermission(userHandle);
        synchronized (getLockObject()) {
            if (this.mInjector.settingsSecureGetIntForUser("cross_profile_calendar_enabled", 0, userHandle) == 0) {
                return false;
            }
            ActiveAdmin admin = getProfileOwnerAdminLocked(userHandle);
            if (admin == null) {
                return false;
            }
            if (admin.mCrossProfileCalendarPackages == null) {
                return true;
            }
            return admin.mCrossProfileCalendarPackages.contains(packageName);
        }
    }

    public List<String> getCrossProfileCalendarPackagesForUser(int userHandle) {
        if (!this.mHasFeature) {
            return Collections.emptyList();
        }
        enforceCrossUsersPermission(userHandle);
        synchronized (getLockObject()) {
            ActiveAdmin admin = getProfileOwnerAdminLocked(userHandle);
            if (admin == null) {
                return Collections.emptyList();
            }
            return admin.mCrossProfileCalendarPackages;
        }
    }

    public boolean isManagedKiosk() {
        if (!this.mHasFeature) {
            return false;
        }
        enforceManageUsers();
        long id = this.mInjector.binderClearCallingIdentity();
        try {
            boolean isManagedKioskInternal = isManagedKioskInternal();
            this.mInjector.binderRestoreCallingIdentity(id);
            return isManagedKioskInternal;
        } catch (RemoteException e) {
            throw new IllegalStateException(e);
        } catch (Throwable th) {
            this.mInjector.binderRestoreCallingIdentity(id);
            throw th;
        }
    }

    public boolean isUnattendedManagedKiosk() {
        boolean z = false;
        if (!this.mHasFeature) {
            return false;
        }
        enforceManageUsers();
        long id = this.mInjector.binderClearCallingIdentity();
        try {
            if (isManagedKioskInternal() && getPowerManagerInternal().wasDeviceIdleFor(30000)) {
                z = true;
            }
            this.mInjector.binderRestoreCallingIdentity(id);
            return z;
        } catch (RemoteException e) {
            throw new IllegalStateException(e);
        } catch (Throwable th) {
            this.mInjector.binderRestoreCallingIdentity(id);
            throw th;
        }
    }

    private boolean isManagedKioskInternal() throws RemoteException {
        return this.mOwners.hasDeviceOwner() && this.mInjector.getIActivityManager().getLockTaskModeState() == 1 && !isLockTaskFeatureEnabled(1) && !deviceHasKeyguard() && !inEphemeralUserSession();
    }

    private boolean isLockTaskFeatureEnabled(int lockTaskFeature) throws RemoteException {
        return (getUserData(this.mInjector.getIActivityManager().getCurrentUser().id).mLockTaskFeatures & lockTaskFeature) == lockTaskFeature;
    }

    private boolean deviceHasKeyguard() {
        for (UserInfo userInfo : this.mUserManager.getUsers()) {
            if (this.mLockPatternUtils.isSecure(userInfo.id)) {
                return true;
            }
        }
        return false;
    }

    private boolean inEphemeralUserSession() {
        for (UserInfo userInfo : this.mUserManager.getUsers()) {
            if (this.mInjector.getUserManager().isUserEphemeral(userInfo.id)) {
                return true;
            }
        }
        return false;
    }

    private PowerManagerInternal getPowerManagerInternal() {
        return this.mInjector.getPowerManagerInternal();
    }

    public boolean startViewCalendarEventInManagedProfile(String packageName, long eventId, long start, long end, boolean allDay, int flags) {
        ActivityNotFoundException e;
        if (!this.mHasFeature) {
            return false;
        }
        Preconditions.checkStringNotEmpty(packageName, "Package name is empty");
        int callingUid = this.mInjector.binderGetCallingUid();
        int callingUserId = this.mInjector.userHandleGetCallingUserId();
        if (isCallingFromPackage(packageName, callingUid)) {
            long identity = this.mInjector.binderClearCallingIdentity();
            try {
                int workProfileUserId = getManagedUserId(callingUserId);
                if (workProfileUserId < 0) {
                    this.mInjector.binderRestoreCallingIdentity(identity);
                    return false;
                } else if (!isPackageAllowedToAccessCalendarForUser(packageName, workProfileUserId)) {
                    try {
                        Log.d(LOG_TAG, String.format("Package %s is not allowed to access cross-profilecalendar APIs", packageName));
                        this.mInjector.binderRestoreCallingIdentity(identity);
                        return false;
                    } catch (Throwable th) {
                        e = th;
                        this.mInjector.binderRestoreCallingIdentity(identity);
                        throw e;
                    }
                } else {
                    Intent intent = new Intent("android.provider.calendar.action.VIEW_MANAGED_PROFILE_CALENDAR_EVENT");
                    intent.setPackage(packageName);
                    try {
                        intent.putExtra(ATTR_ID, eventId);
                    } catch (Throwable th2) {
                        e = th2;
                        this.mInjector.binderRestoreCallingIdentity(identity);
                        throw e;
                    }
                    try {
                        intent.putExtra("beginTime", start);
                        try {
                            intent.putExtra("endTime", end);
                        } catch (Throwable th3) {
                            e = th3;
                            this.mInjector.binderRestoreCallingIdentity(identity);
                            throw e;
                        }
                        try {
                            intent.putExtra("allDay", allDay);
                            intent.setFlags(flags);
                            try {
                                this.mContext.startActivityAsUser(intent, UserHandle.of(workProfileUserId));
                                this.mInjector.binderRestoreCallingIdentity(identity);
                                return true;
                            } catch (ActivityNotFoundException e2) {
                                Log.e(LOG_TAG, "View event activity not found", e2);
                                this.mInjector.binderRestoreCallingIdentity(identity);
                                return false;
                            }
                        } catch (Throwable th4) {
                            e = th4;
                            this.mInjector.binderRestoreCallingIdentity(identity);
                            throw e;
                        }
                    } catch (Throwable th5) {
                        e = th5;
                        this.mInjector.binderRestoreCallingIdentity(identity);
                        throw e;
                    }
                }
            } catch (Throwable th6) {
                e = th6;
                this.mInjector.binderRestoreCallingIdentity(identity);
                throw e;
            }
        } else {
            throw new SecurityException("Input package name doesn't align with actual calling package.");
        }
    }

    private boolean isCallingFromPackage(String packageName, int callingUid) {
        try {
            if (this.mInjector.getPackageManager().getPackageUidAsUser(packageName, UserHandle.getUserId(callingUid)) == callingUid) {
                return true;
            }
            return false;
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(LOG_TAG, "Calling package not found", e);
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private DevicePolicyConstants loadConstants() {
        return DevicePolicyConstants.loadFromString(this.mInjector.settingsGlobalGetString("device_policy_constants"));
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        IHwDevicePolicyManagerService iHwDevicePolicyManagerService = this.mHwDevicePolicyManagerService;
        if (iHwDevicePolicyManagerService == null || !iHwDevicePolicyManagerService.processTransaction(code, data, reply, flags)) {
            return super.onTransact(code, data, reply, flags);
        }
        return true;
    }
}
