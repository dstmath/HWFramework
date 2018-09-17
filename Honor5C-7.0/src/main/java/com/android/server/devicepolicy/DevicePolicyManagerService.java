package com.android.server.devicepolicy;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.accounts.AccountManager;
import android.annotation.IntDef;
import android.app.ActivityManagerNative;
import android.app.AlarmManager;
import android.app.AppGlobals;
import android.app.IActivityManager;
import android.app.Notification.BigTextStyle;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.admin.DeviceAdminInfo;
import android.app.admin.DeviceAdminInfo.PolicyInfo;
import android.app.admin.DevicePolicyManagerInternal;
import android.app.admin.DevicePolicyManagerInternal.OnCrossProfileWidgetProvidersChangeListener;
import android.app.admin.SecurityLog;
import android.app.admin.SecurityLog.SecurityEvent;
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
import android.content.pm.UserInfo;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.IAudioService;
import android.net.ConnectivityManager;
import android.net.ProxyInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
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
import android.security.Credentials;
import android.security.IKeyChainAliasCallback;
import android.security.IKeyChainService;
import android.security.KeyChain;
import android.security.KeyChain.KeyChainConnection;
import android.service.persistentdata.PersistentDataBlockManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Flog;
import android.util.Log;
import android.util.Pair;
import android.util.Slog;
import android.util.SparseArray;
import android.util.Xml;
import android.view.IWindowManager;
import android.view.IWindowManager.Stub;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.IAccessibilityManager;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import com.android.internal.statusbar.IStatusBarService;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.JournaledFile;
import com.android.internal.util.ParcelableString;
import com.android.internal.util.Preconditions;
import com.android.internal.util.XmlUtils;
import com.android.internal.widget.LockPatternUtils;
import com.android.server.LocalServices;
import com.android.server.SystemService;
import com.android.server.am.HwBroadcastRadarUtil;
import com.android.server.am.ProcessList;
import com.android.server.devicepolicy.HwDevicePolicyFactory.IHwDevicePolicyManagerService;
import com.android.server.job.controllers.JobStatus;
import com.android.server.location.LocationFudger;
import com.android.server.pm.AbsPackageManagerService;
import com.android.server.pm.UserRestrictionsUtils;
import com.android.server.power.IHwShutdownThread;
import com.google.android.collect.Sets;
import huawei.cust.HwCustUtils;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.charset.StandardCharsets;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class DevicePolicyManagerService extends AbsDevicePolicyManagerService {
    private static final String ACTION_EXPIRED_PASSWORD_NOTIFICATION = "com.android.server.ACTION_EXPIRED_PASSWORD_NOTIFICATION";
    private static final String ATTR_APPLICATION_RESTRICTIONS_MANAGER = "application-restrictions-manager";
    private static final String ATTR_DELEGATED_CERT_INSTALLER = "delegated-cert-installer";
    private static final String ATTR_DISABLED = "disabled";
    private static final String ATTR_NAME = "name";
    private static final String ATTR_PERMISSION_POLICY = "permission-policy";
    private static final String ATTR_PERMISSION_PROVIDER = "permission-provider";
    private static final String ATTR_PROVISIONING_STATE = "provisioning-state";
    private static final String ATTR_SETUP_COMPLETE = "setup-complete";
    private static final String ATTR_VALUE = "value";
    private static final int CODE_ACCOUNTS_NOT_EMPTY = 6;
    private static final int CODE_HAS_DEVICE_OWNER = 1;
    private static final int CODE_NONSYSTEM_USER_EXISTS = 5;
    private static final int CODE_NOT_SYSTEM_USER = 7;
    private static final int CODE_OK = 0;
    private static final int CODE_USER_HAS_PROFILE_OWNER = 2;
    private static final int CODE_USER_NOT_RUNNING = 3;
    private static final int CODE_USER_SETUP_COMPLETED = 4;
    private static final int DEVICE_ADMIN_DEACTIVATE_TIMEOUT = 10000;
    private static final String DEVICE_POLICIES_XML = "device_policies.xml";
    private static final String DO_NOT_ASK_CREDENTIALS_ON_BOOT_XML = "do-not-ask-credentials-on-boot";
    private static final long EXPIRATION_GRACE_PERIOD_MS = 432000000;
    private static final Set<String> GLOBAL_SETTINGS_DEPRECATED = null;
    private static final Set<String> GLOBAL_SETTINGS_WHITELIST = null;
    private static final String LOG_TAG = "DevicePolicyManagerService";
    private static final int MONITORING_CERT_NOTIFICATION_ID = 18087955;
    private static final long MS_PER_DAY = 86400000;
    private static final int PROFILE_KEYGUARD_FEATURES = 56;
    private static final int PROFILE_KEYGUARD_FEATURES_AFFECT_OWNER = 48;
    private static final int PROFILE_KEYGUARD_FEATURES_PROFILE_ONLY = 8;
    private static final int PROFILE_WIPED_NOTIFICATION_ID = 1001;
    private static final String PROPERTY_DEVICE_OWNER_PRESENT = "ro.device_owner";
    private static final int REQUEST_EXPIRE_PASSWORD = 5571;
    private static final Set<String> SECURE_SETTINGS_DEVICEOWNER_WHITELIST = null;
    private static final Set<String> SECURE_SETTINGS_WHITELIST = null;
    private static final int STATUS_BAR_DISABLE2_MASK = 1;
    private static final int STATUS_BAR_DISABLE_MASK = 34013184;
    private static final String TAG_ACCEPTED_CA_CERTIFICATES = "accepted-ca-certificate";
    private static final String TAG_ADMIN_BROADCAST_PENDING = "admin-broadcast-pending";
    private static final String TAG_AFFILIATION_ID = "affiliation-id";
    private static final String TAG_INITIALIZATION_BUNDLE = "initialization-bundle";
    private static final String TAG_LOCK_TASK_COMPONENTS = "lock-task-component";
    private static final String TAG_STATUS_BAR = "statusbar";
    private static final boolean VERBOSE_LOG = false;
    final Context mContext;
    final Handler mHandler;
    boolean mHasFeature;
    final IPackageManager mIPackageManager;
    final Injector mInjector;
    final LocalService mLocalService;
    private final LockPatternUtils mLockPatternUtils;
    final Owners mOwners;
    private final Set<Pair<String, Integer>> mPackagesToRemove;
    BroadcastReceiver mReceiver;
    private final BroadcastReceiver mRemoteBugreportConsentReceiver;
    private final BroadcastReceiver mRemoteBugreportFinishedReceiver;
    private final AtomicBoolean mRemoteBugreportServiceIsActive;
    private final AtomicBoolean mRemoteBugreportSharingAccepted;
    private final Runnable mRemoteBugreportTimeoutRunnable;
    private final SecurityLogMonitor mSecurityLogMonitor;
    final TelephonyManager mTelephonyManager;
    private final Binder mToken;
    final SparseArray<DevicePolicyData> mUserData;
    final UserManager mUserManager;
    final UserManagerInternal mUserManagerInternal;

    /* renamed from: com.android.server.devicepolicy.DevicePolicyManagerService.10 */
    class AnonymousClass10 implements Runnable {
        final /* synthetic */ boolean val$disabled;
        final /* synthetic */ int val$userHandle;

        AnonymousClass10(int val$userHandle, boolean val$disabled) {
            this.val$userHandle = val$userHandle;
            this.val$disabled = val$disabled;
        }

        public void run() {
            try {
                DevicePolicyManagerService.this.mInjector.getIWindowManager().setScreenCaptureDisabled(this.val$userHandle, this.val$disabled);
            } catch (RemoteException e) {
                Log.w(DevicePolicyManagerService.LOG_TAG, "Unable to notify WindowManager.", e);
            }
        }
    }

    /* renamed from: com.android.server.devicepolicy.DevicePolicyManagerService.11 */
    class AnonymousClass11 implements Runnable {
        final /* synthetic */ List val$packageActiveAdmins;
        final /* synthetic */ String val$packageName;
        final /* synthetic */ int val$userId;

        AnonymousClass11(List val$packageActiveAdmins, int val$userId, String val$packageName) {
            this.val$packageActiveAdmins = val$packageActiveAdmins;
            this.val$userId = val$userId;
            this.val$packageName = val$packageName;
        }

        public void run() {
            for (ComponentName activeAdmin : this.val$packageActiveAdmins) {
                DevicePolicyManagerService.this.removeAdminArtifacts(activeAdmin, this.val$userId);
            }
            DevicePolicyManagerService.this.startUninstallIntent(this.val$packageName, this.val$userId);
        }
    }

    /* renamed from: com.android.server.devicepolicy.DevicePolicyManagerService.5 */
    class AnonymousClass5 extends BroadcastReceiver {
        final /* synthetic */ ComponentName val$adminReceiver;
        final /* synthetic */ int val$userHandle;

        AnonymousClass5(ComponentName val$adminReceiver, int val$userHandle) {
            this.val$adminReceiver = val$adminReceiver;
            this.val$userHandle = val$userHandle;
        }

        public void onReceive(Context context, Intent intent) {
            DevicePolicyManagerService.this.removeAdminArtifacts(this.val$adminReceiver, this.val$userHandle);
            DevicePolicyManagerService.this.removePackageIfRequired(this.val$adminReceiver.getPackageName(), this.val$userHandle);
        }
    }

    /* renamed from: com.android.server.devicepolicy.DevicePolicyManagerService.6 */
    class AnonymousClass6 extends BroadcastReceiver {
        final /* synthetic */ IBinder val$response;

        AnonymousClass6(IBinder val$response) {
            this.val$response = val$response;
        }

        public void onReceive(Context context, Intent intent) {
            DevicePolicyManagerService.this.sendPrivateKeyAliasResponse(getResultData(), this.val$response);
        }
    }

    /* renamed from: com.android.server.devicepolicy.DevicePolicyManagerService.7 */
    class AnonymousClass7 extends AsyncTask<Void, Void, Void> {
        final /* synthetic */ String val$alias;
        final /* synthetic */ IKeyChainAliasCallback val$keyChainAliasResponse;

        AnonymousClass7(IKeyChainAliasCallback val$keyChainAliasResponse, String val$alias) {
            this.val$keyChainAliasResponse = val$keyChainAliasResponse;
            this.val$alias = val$alias;
        }

        protected Void doInBackground(Void... unused) {
            try {
                this.val$keyChainAliasResponse.alias(this.val$alias);
            } catch (Exception e) {
                Log.e(DevicePolicyManagerService.LOG_TAG, "error while responding to callback", e);
            }
            return null;
        }
    }

    /* renamed from: com.android.server.devicepolicy.DevicePolicyManagerService.8 */
    class AnonymousClass8 implements Runnable {
        final /* synthetic */ int val$userHandle;

        AnonymousClass8(int val$userHandle) {
            this.val$userHandle = val$userHandle;
        }

        public void run() {
            try {
                IActivityManager am = DevicePolicyManagerService.this.mInjector.getIActivityManager();
                if (am.getCurrentUser().id == this.val$userHandle) {
                    am.switchUser(DevicePolicyManagerService.CODE_OK);
                }
                boolean isManagedProfile = DevicePolicyManagerService.this.isManagedProfile(this.val$userHandle);
                if (!DevicePolicyManagerService.this.mUserManager.removeUser(this.val$userHandle)) {
                    Slog.w(DevicePolicyManagerService.LOG_TAG, "Couldn't remove user " + this.val$userHandle);
                } else if (isManagedProfile) {
                    DevicePolicyManagerService.this.sendWipeProfileNotification();
                }
            } catch (RemoteException e) {
            }
        }
    }

    /* renamed from: com.android.server.devicepolicy.DevicePolicyManagerService.9 */
    class AnonymousClass9 extends BroadcastReceiver {
        final /* synthetic */ RemoteCallback val$result;

        AnonymousClass9(RemoteCallback val$result) {
            this.val$result = val$result;
        }

        public void onReceive(Context context, Intent intent) {
            this.val$result.sendResult(getResultExtras(false));
        }
    }

    static class ActiveAdmin {
        private static final String ATTR_VALUE = "value";
        static final int DEF_KEYGUARD_FEATURES_DISABLED = 0;
        static final int DEF_MAXIMUM_FAILED_PASSWORDS_FOR_WIPE = 0;
        static final long DEF_MAXIMUM_TIME_TO_UNLOCK = 0;
        static final int DEF_MINIMUM_PASSWORD_LENGTH = 0;
        static final int DEF_MINIMUM_PASSWORD_LETTERS = 1;
        static final int DEF_MINIMUM_PASSWORD_LOWER_CASE = 0;
        static final int DEF_MINIMUM_PASSWORD_NON_LETTER = 0;
        static final int DEF_MINIMUM_PASSWORD_NUMERIC = 1;
        static final int DEF_MINIMUM_PASSWORD_SYMBOLS = 1;
        static final int DEF_MINIMUM_PASSWORD_UPPER_CASE = 0;
        static final int DEF_ORGANIZATION_COLOR = 0;
        static final long DEF_PASSWORD_EXPIRATION_DATE = 0;
        static final long DEF_PASSWORD_EXPIRATION_TIMEOUT = 0;
        static final int DEF_PASSWORD_HISTORY_LENGTH = 0;
        private static final String TAG_ACCOUNT_TYPE = "account-type";
        private static final String TAG_CROSS_PROFILE_WIDGET_PROVIDERS = "cross-profile-widget-providers";
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
        private static final String TAG_POLICIES = "policies";
        private static final String TAG_PROVIDER = "provider";
        private static final String TAG_REQUIRE_AUTO_TIME = "require_auto_time";
        private static final String TAG_SHORT_SUPPORT_MESSAGE = "short-support-message";
        private static final String TAG_SPECIFIES_GLOBAL_PROXY = "specifies-global-proxy";
        private static final String TAG_TRUST_AGENT_COMPONENT = "component";
        private static final String TAG_TRUST_AGENT_COMPONENT_OPTIONS = "trust-agent-component-options";
        private static final String TAG_USER_RESTRICTIONS = "user-restrictions";
        Set<String> accountTypesWithManagementDisabled;
        List<String> crossProfileWidgetProviders;
        boolean disableBluetoothContactSharing;
        boolean disableCallerId;
        boolean disableCamera;
        boolean disableContactsSearch;
        boolean disableScreenCapture;
        int disabledKeyguardFeatures;
        boolean encryptionRequested;
        boolean forceEphemeralUsers;
        String globalProxyExclusionList;
        String globalProxySpec;
        final DeviceAdminInfo info;
        final boolean isParent;
        List<String> keepUninstalledPackages;
        CharSequence longSupportMessage;
        public HwActiveAdmin mHwActiveAdmin;
        int maximumFailedPasswordsForWipe;
        long maximumTimeToUnlock;
        int minimumPasswordLength;
        int minimumPasswordLetters;
        int minimumPasswordLowerCase;
        int minimumPasswordNonLetter;
        int minimumPasswordNumeric;
        int minimumPasswordSymbols;
        int minimumPasswordUpperCase;
        int organizationColor;
        String organizationName;
        ActiveAdmin parentAdmin;
        long passwordExpirationDate;
        long passwordExpirationTimeout;
        int passwordHistoryLength;
        int passwordQuality;
        List<String> permittedAccessiblityServices;
        List<String> permittedInputMethods;
        boolean requireAutoTime;
        CharSequence shortSupportMessage;
        boolean specifiesGlobalProxy;
        ArrayMap<String, TrustAgentInfo> trustAgentInfos;
        Bundle userRestrictions;

        static class TrustAgentInfo {
            public PersistableBundle options;

            TrustAgentInfo(PersistableBundle bundle) {
                this.options = bundle;
            }
        }

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.devicepolicy.DevicePolicyManagerService.ActiveAdmin.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.devicepolicy.DevicePolicyManagerService.ActiveAdmin.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.devicepolicy.DevicePolicyManagerService.ActiveAdmin.<clinit>():void");
        }

        ActiveAdmin(DeviceAdminInfo _info, boolean parent) {
            this.passwordQuality = DEF_PASSWORD_HISTORY_LENGTH;
            this.minimumPasswordLength = DEF_PASSWORD_HISTORY_LENGTH;
            this.passwordHistoryLength = DEF_PASSWORD_HISTORY_LENGTH;
            this.minimumPasswordUpperCase = DEF_PASSWORD_HISTORY_LENGTH;
            this.minimumPasswordLowerCase = DEF_PASSWORD_HISTORY_LENGTH;
            this.minimumPasswordLetters = DEF_MINIMUM_PASSWORD_SYMBOLS;
            this.minimumPasswordNumeric = DEF_MINIMUM_PASSWORD_SYMBOLS;
            this.minimumPasswordSymbols = DEF_MINIMUM_PASSWORD_SYMBOLS;
            this.minimumPasswordNonLetter = DEF_PASSWORD_HISTORY_LENGTH;
            this.maximumTimeToUnlock = DEF_PASSWORD_EXPIRATION_TIMEOUT;
            this.maximumFailedPasswordsForWipe = DEF_PASSWORD_HISTORY_LENGTH;
            this.passwordExpirationTimeout = DEF_PASSWORD_EXPIRATION_TIMEOUT;
            this.passwordExpirationDate = DEF_PASSWORD_EXPIRATION_TIMEOUT;
            this.disabledKeyguardFeatures = DEF_PASSWORD_HISTORY_LENGTH;
            this.encryptionRequested = false;
            this.disableCamera = false;
            this.disableCallerId = false;
            this.disableContactsSearch = false;
            this.disableBluetoothContactSharing = true;
            this.disableScreenCapture = false;
            this.requireAutoTime = false;
            this.forceEphemeralUsers = false;
            this.accountTypesWithManagementDisabled = new ArraySet();
            this.specifiesGlobalProxy = false;
            this.globalProxySpec = null;
            this.globalProxyExclusionList = null;
            this.trustAgentInfos = new ArrayMap();
            this.shortSupportMessage = null;
            this.longSupportMessage = null;
            this.organizationColor = DEF_ORGANIZATION_COLOR;
            this.organizationName = null;
            this.info = _info;
            this.isParent = parent;
        }

        ActiveAdmin getParentActiveAdmin() {
            Preconditions.checkState(!this.isParent);
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
            if (this.passwordQuality != 0) {
                out.startTag(null, TAG_PASSWORD_QUALITY);
                out.attribute(null, ATTR_VALUE, Integer.toString(this.passwordQuality));
                out.endTag(null, TAG_PASSWORD_QUALITY);
                if (this.minimumPasswordLength != 0) {
                    out.startTag(null, TAG_MIN_PASSWORD_LENGTH);
                    out.attribute(null, ATTR_VALUE, Integer.toString(this.minimumPasswordLength));
                    out.endTag(null, TAG_MIN_PASSWORD_LENGTH);
                }
                if (this.passwordHistoryLength != 0) {
                    out.startTag(null, TAG_PASSWORD_HISTORY_LENGTH);
                    out.attribute(null, ATTR_VALUE, Integer.toString(this.passwordHistoryLength));
                    out.endTag(null, TAG_PASSWORD_HISTORY_LENGTH);
                }
                if (this.minimumPasswordUpperCase != 0) {
                    out.startTag(null, TAG_MIN_PASSWORD_UPPERCASE);
                    out.attribute(null, ATTR_VALUE, Integer.toString(this.minimumPasswordUpperCase));
                    out.endTag(null, TAG_MIN_PASSWORD_UPPERCASE);
                }
                if (this.minimumPasswordLowerCase != 0) {
                    out.startTag(null, TAG_MIN_PASSWORD_LOWERCASE);
                    out.attribute(null, ATTR_VALUE, Integer.toString(this.minimumPasswordLowerCase));
                    out.endTag(null, TAG_MIN_PASSWORD_LOWERCASE);
                }
                if (this.minimumPasswordLetters != DEF_MINIMUM_PASSWORD_SYMBOLS) {
                    out.startTag(null, TAG_MIN_PASSWORD_LETTERS);
                    out.attribute(null, ATTR_VALUE, Integer.toString(this.minimumPasswordLetters));
                    out.endTag(null, TAG_MIN_PASSWORD_LETTERS);
                }
                if (this.minimumPasswordNumeric != DEF_MINIMUM_PASSWORD_SYMBOLS) {
                    out.startTag(null, TAG_MIN_PASSWORD_NUMERIC);
                    out.attribute(null, ATTR_VALUE, Integer.toString(this.minimumPasswordNumeric));
                    out.endTag(null, TAG_MIN_PASSWORD_NUMERIC);
                }
                if (this.minimumPasswordSymbols != DEF_MINIMUM_PASSWORD_SYMBOLS) {
                    out.startTag(null, TAG_MIN_PASSWORD_SYMBOLS);
                    out.attribute(null, ATTR_VALUE, Integer.toString(this.minimumPasswordSymbols));
                    out.endTag(null, TAG_MIN_PASSWORD_SYMBOLS);
                }
                if (this.minimumPasswordNonLetter > 0) {
                    out.startTag(null, TAG_MIN_PASSWORD_NONLETTER);
                    out.attribute(null, ATTR_VALUE, Integer.toString(this.minimumPasswordNonLetter));
                    out.endTag(null, TAG_MIN_PASSWORD_NONLETTER);
                }
            }
            if (this.maximumTimeToUnlock != DEF_PASSWORD_EXPIRATION_TIMEOUT) {
                out.startTag(null, TAG_MAX_TIME_TO_UNLOCK);
                out.attribute(null, ATTR_VALUE, Long.toString(this.maximumTimeToUnlock));
                out.endTag(null, TAG_MAX_TIME_TO_UNLOCK);
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
            if (this.passwordExpirationTimeout != DEF_PASSWORD_EXPIRATION_TIMEOUT) {
                out.startTag(null, TAG_PASSWORD_EXPIRATION_TIMEOUT);
                out.attribute(null, ATTR_VALUE, Long.toString(this.passwordExpirationTimeout));
                out.endTag(null, TAG_PASSWORD_EXPIRATION_TIMEOUT);
            }
            if (this.passwordExpirationDate != DEF_PASSWORD_EXPIRATION_TIMEOUT) {
                out.startTag(null, TAG_PASSWORD_EXPIRATION_DATE);
                out.attribute(null, ATTR_VALUE, Long.toString(this.passwordExpirationDate));
                out.endTag(null, TAG_PASSWORD_EXPIRATION_DATE);
            }
            if (this.encryptionRequested) {
                out.startTag(null, TAG_ENCRYPTION_REQUESTED);
                out.attribute(null, ATTR_VALUE, Boolean.toString(this.encryptionRequested));
                out.endTag(null, TAG_ENCRYPTION_REQUESTED);
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
            if (this.disabledKeyguardFeatures != 0) {
                out.startTag(null, TAG_DISABLE_KEYGUARD_FEATURES);
                out.attribute(null, ATTR_VALUE, Integer.toString(this.disabledKeyguardFeatures));
                out.endTag(null, TAG_DISABLE_KEYGUARD_FEATURES);
            }
            if (!this.accountTypesWithManagementDisabled.isEmpty()) {
                out.startTag(null, TAG_DISABLE_ACCOUNT_MANAGEMENT);
                for (String ac : this.accountTypesWithManagementDisabled) {
                    out.startTag(null, TAG_ACCOUNT_TYPE);
                    out.attribute(null, ATTR_VALUE, ac);
                    out.endTag(null, TAG_ACCOUNT_TYPE);
                }
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
            if (!(this.crossProfileWidgetProviders == null || this.crossProfileWidgetProviders.isEmpty())) {
                out.startTag(null, TAG_CROSS_PROFILE_WIDGET_PROVIDERS);
                int providerCount = this.crossProfileWidgetProviders.size();
                for (int i = DEF_PASSWORD_HISTORY_LENGTH; i < providerCount; i += DEF_MINIMUM_PASSWORD_SYMBOLS) {
                    String provider = (String) this.crossProfileWidgetProviders.get(i);
                    out.startTag(null, TAG_PROVIDER);
                    out.attribute(null, ATTR_VALUE, provider);
                    out.endTag(null, TAG_PROVIDER);
                }
                out.endTag(null, TAG_CROSS_PROFILE_WIDGET_PROVIDERS);
            }
            writePackageListToXml(out, TAG_PERMITTED_ACCESSIBILITY_SERVICES, this.permittedAccessiblityServices);
            writePackageListToXml(out, TAG_PERMITTED_IMES, this.permittedInputMethods);
            writePackageListToXml(out, TAG_KEEP_UNINSTALLED_PACKAGES, this.keepUninstalledPackages);
            if (hasUserRestrictions()) {
                UserRestrictionsUtils.writeRestrictions(out, this.userRestrictions, TAG_USER_RESTRICTIONS);
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
                for (String packageName : packageList) {
                    out.startTag(null, TAG_PACKAGE_LIST_ITEM);
                    out.attribute(null, ATTR_VALUE, packageName);
                    out.endTag(null, TAG_PACKAGE_LIST_ITEM);
                }
                out.endTag(null, outerTag);
            }
        }

        void readFromXml(XmlPullParser parser) throws XmlPullParserException, IOException {
            int outerDepth = parser.getDepth();
            while (true) {
                int type = parser.next();
                if (type == DEF_MINIMUM_PASSWORD_SYMBOLS) {
                    return;
                }
                if (type == DevicePolicyManagerService.CODE_USER_NOT_RUNNING && parser.getDepth() <= outerDepth) {
                    return;
                }
                if (!(type == DevicePolicyManagerService.CODE_USER_NOT_RUNNING || type == DevicePolicyManagerService.CODE_USER_SETUP_COMPLETED)) {
                    String tag = parser.getName();
                    if (HwActiveAdmin.TAG_POLICIES.equals(tag)) {
                        this.mHwActiveAdmin = new HwActiveAdmin();
                        this.mHwActiveAdmin.readPoliciesFromXml(parser);
                    } else if (TAG_POLICIES.equals(tag)) {
                        this.info.readPoliciesFromXml(parser);
                    } else if (TAG_PASSWORD_QUALITY.equals(tag)) {
                        this.passwordQuality = Integer.parseInt(parser.getAttributeValue(null, ATTR_VALUE));
                    } else if (TAG_MIN_PASSWORD_LENGTH.equals(tag)) {
                        this.minimumPasswordLength = Integer.parseInt(parser.getAttributeValue(null, ATTR_VALUE));
                    } else if (TAG_PASSWORD_HISTORY_LENGTH.equals(tag)) {
                        this.passwordHistoryLength = Integer.parseInt(parser.getAttributeValue(null, ATTR_VALUE));
                    } else if (TAG_MIN_PASSWORD_UPPERCASE.equals(tag)) {
                        this.minimumPasswordUpperCase = Integer.parseInt(parser.getAttributeValue(null, ATTR_VALUE));
                    } else if (TAG_MIN_PASSWORD_LOWERCASE.equals(tag)) {
                        this.minimumPasswordLowerCase = Integer.parseInt(parser.getAttributeValue(null, ATTR_VALUE));
                    } else if (TAG_MIN_PASSWORD_LETTERS.equals(tag)) {
                        this.minimumPasswordLetters = Integer.parseInt(parser.getAttributeValue(null, ATTR_VALUE));
                    } else if (TAG_MIN_PASSWORD_NUMERIC.equals(tag)) {
                        this.minimumPasswordNumeric = Integer.parseInt(parser.getAttributeValue(null, ATTR_VALUE));
                    } else if (TAG_MIN_PASSWORD_SYMBOLS.equals(tag)) {
                        this.minimumPasswordSymbols = Integer.parseInt(parser.getAttributeValue(null, ATTR_VALUE));
                    } else if (TAG_MIN_PASSWORD_NONLETTER.equals(tag)) {
                        this.minimumPasswordNonLetter = Integer.parseInt(parser.getAttributeValue(null, ATTR_VALUE));
                    } else if (TAG_MAX_TIME_TO_UNLOCK.equals(tag)) {
                        this.maximumTimeToUnlock = Long.parseLong(parser.getAttributeValue(null, ATTR_VALUE));
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
                    } else if (TAG_DISABLE_KEYGUARD_FEATURES.equals(tag)) {
                        this.disabledKeyguardFeatures = Integer.parseInt(parser.getAttributeValue(null, ATTR_VALUE));
                    } else if (TAG_DISABLE_ACCOUNT_MANAGEMENT.equals(tag)) {
                        this.accountTypesWithManagementDisabled = readDisableAccountInfo(parser, tag);
                    } else if (TAG_MANAGE_TRUST_AGENT_FEATURES.equals(tag)) {
                        this.trustAgentInfos = getAllTrustAgentInfos(parser, tag);
                    } else if (TAG_CROSS_PROFILE_WIDGET_PROVIDERS.equals(tag)) {
                        this.crossProfileWidgetProviders = getCrossProfileWidgetProviders(parser, tag);
                    } else if (TAG_PERMITTED_ACCESSIBILITY_SERVICES.equals(tag)) {
                        this.permittedAccessiblityServices = readPackageList(parser, tag);
                    } else if (TAG_PERMITTED_IMES.equals(tag)) {
                        this.permittedInputMethods = readPackageList(parser, tag);
                    } else if (TAG_KEEP_UNINSTALLED_PACKAGES.equals(tag)) {
                        this.keepUninstalledPackages = readPackageList(parser, tag);
                    } else if (TAG_USER_RESTRICTIONS.equals(tag)) {
                        UserRestrictionsUtils.readRestrictions(parser, ensureUserRestrictions());
                    } else if (TAG_SHORT_SUPPORT_MESSAGE.equals(tag)) {
                        if (parser.next() == DevicePolicyManagerService.CODE_USER_SETUP_COMPLETED) {
                            this.shortSupportMessage = parser.getText();
                        } else {
                            Log.w(DevicePolicyManagerService.LOG_TAG, "Missing text when loading short support message");
                        }
                    } else if (TAG_LONG_SUPPORT_MESSAGE.equals(tag)) {
                        if (parser.next() == DevicePolicyManagerService.CODE_USER_SETUP_COMPLETED) {
                            this.longSupportMessage = parser.getText();
                        } else {
                            Log.w(DevicePolicyManagerService.LOG_TAG, "Missing text when loading long support message");
                        }
                    } else if (TAG_PARENT_ADMIN.equals(tag)) {
                        boolean z;
                        if (this.isParent) {
                            z = false;
                        } else {
                            z = true;
                        }
                        Preconditions.checkState(z);
                        this.parentAdmin = new ActiveAdmin(this.info, true);
                        this.parentAdmin.readFromXml(parser);
                    } else if (TAG_ORGANIZATION_COLOR.equals(tag)) {
                        this.organizationColor = Integer.parseInt(parser.getAttributeValue(null, ATTR_VALUE));
                    } else if (!TAG_ORGANIZATION_NAME.equals(tag)) {
                        Slog.w(DevicePolicyManagerService.LOG_TAG, "Unknown admin tag: " + tag);
                        XmlUtils.skipCurrentTag(parser);
                    } else if (parser.next() == DevicePolicyManagerService.CODE_USER_SETUP_COMPLETED) {
                        this.organizationName = parser.getText();
                    } else {
                        Log.w(DevicePolicyManagerService.LOG_TAG, "Missing text when loading organization name");
                    }
                }
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private List<String> readPackageList(XmlPullParser parser, String tag) throws XmlPullParserException, IOException {
            List<String> result = new ArrayList();
            int outerDepth = parser.getDepth();
            while (true) {
                int outerType = parser.next();
                if (outerType == DEF_MINIMUM_PASSWORD_SYMBOLS || (outerType == DevicePolicyManagerService.CODE_USER_NOT_RUNNING && parser.getDepth() <= outerDepth)) {
                    return result;
                }
                if (!(outerType == DevicePolicyManagerService.CODE_USER_NOT_RUNNING || outerType == DevicePolicyManagerService.CODE_USER_SETUP_COMPLETED)) {
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

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private Set<String> readDisableAccountInfo(XmlPullParser parser, String tag) throws XmlPullParserException, IOException {
            int outerDepthDAM = parser.getDepth();
            Set<String> result = new ArraySet();
            while (true) {
                int typeDAM = parser.next();
                if (typeDAM == DEF_MINIMUM_PASSWORD_SYMBOLS || (typeDAM == DevicePolicyManagerService.CODE_USER_NOT_RUNNING && parser.getDepth() <= outerDepthDAM)) {
                    return result;
                }
                if (!(typeDAM == DevicePolicyManagerService.CODE_USER_NOT_RUNNING || typeDAM == DevicePolicyManagerService.CODE_USER_SETUP_COMPLETED)) {
                    String tagDAM = parser.getName();
                    if (TAG_ACCOUNT_TYPE.equals(tagDAM)) {
                        result.add(parser.getAttributeValue(null, ATTR_VALUE));
                    } else {
                        Slog.w(DevicePolicyManagerService.LOG_TAG, "Unknown tag under " + tag + ": " + tagDAM);
                    }
                }
            }
            return result;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private ArrayMap<String, TrustAgentInfo> getAllTrustAgentInfos(XmlPullParser parser, String tag) throws XmlPullParserException, IOException {
            int outerDepthDAM = parser.getDepth();
            ArrayMap<String, TrustAgentInfo> result = new ArrayMap();
            while (true) {
                int typeDAM = parser.next();
                if (typeDAM == DEF_MINIMUM_PASSWORD_SYMBOLS || (typeDAM == DevicePolicyManagerService.CODE_USER_NOT_RUNNING && parser.getDepth() <= outerDepthDAM)) {
                    return result;
                }
                if (!(typeDAM == DevicePolicyManagerService.CODE_USER_NOT_RUNNING || typeDAM == DevicePolicyManagerService.CODE_USER_SETUP_COMPLETED)) {
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

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private TrustAgentInfo getTrustAgentInfo(XmlPullParser parser, String tag) throws XmlPullParserException, IOException {
            int outerDepthDAM = parser.getDepth();
            TrustAgentInfo result = new TrustAgentInfo(null);
            while (true) {
                int typeDAM = parser.next();
                if (typeDAM == DEF_MINIMUM_PASSWORD_SYMBOLS || (typeDAM == DevicePolicyManagerService.CODE_USER_NOT_RUNNING && parser.getDepth() <= outerDepthDAM)) {
                    return result;
                }
                if (!(typeDAM == DevicePolicyManagerService.CODE_USER_NOT_RUNNING || typeDAM == DevicePolicyManagerService.CODE_USER_SETUP_COMPLETED)) {
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

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private List<String> getCrossProfileWidgetProviders(XmlPullParser parser, String tag) throws XmlPullParserException, IOException {
            int outerDepthDAM = parser.getDepth();
            List<String> list = null;
            while (true) {
                int typeDAM = parser.next();
                if (typeDAM == DEF_MINIMUM_PASSWORD_SYMBOLS || (typeDAM == DevicePolicyManagerService.CODE_USER_NOT_RUNNING && parser.getDepth() <= outerDepthDAM)) {
                    return list;
                }
                if (!(typeDAM == DevicePolicyManagerService.CODE_USER_NOT_RUNNING || typeDAM == DevicePolicyManagerService.CODE_USER_SETUP_COMPLETED)) {
                    String tagDAM = parser.getName();
                    if (TAG_PROVIDER.equals(tagDAM)) {
                        String provider = parser.getAttributeValue(null, ATTR_VALUE);
                        if (list == null) {
                            list = new ArrayList();
                        }
                        list.add(provider);
                    } else {
                        Slog.w(DevicePolicyManagerService.LOG_TAG, "Unknown tag under " + tag + ": " + tagDAM);
                    }
                }
            }
            return list;
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
            pw.println("policies:");
            ArrayList<PolicyInfo> pols = this.info.getUsedPolicies();
            if (pols != null) {
                for (int i = DEF_PASSWORD_HISTORY_LENGTH; i < pols.size(); i += DEF_MINIMUM_PASSWORD_SYMBOLS) {
                    pw.print(prefix);
                    pw.print("  ");
                    pw.println(((PolicyInfo) pols.get(i)).tag);
                }
            }
            pw.print(prefix);
            pw.print("passwordQuality=0x");
            pw.println(Integer.toHexString(this.passwordQuality));
            pw.print(prefix);
            pw.print("minimumPasswordLength=");
            pw.println(this.minimumPasswordLength);
            pw.print(prefix);
            pw.print("passwordHistoryLength=");
            pw.println(this.passwordHistoryLength);
            pw.print(prefix);
            pw.print("minimumPasswordUpperCase=");
            pw.println(this.minimumPasswordUpperCase);
            pw.print(prefix);
            pw.print("minimumPasswordLowerCase=");
            pw.println(this.minimumPasswordLowerCase);
            pw.print(prefix);
            pw.print("minimumPasswordLetters=");
            pw.println(this.minimumPasswordLetters);
            pw.print(prefix);
            pw.print("minimumPasswordNumeric=");
            pw.println(this.minimumPasswordNumeric);
            pw.print(prefix);
            pw.print("minimumPasswordSymbols=");
            pw.println(this.minimumPasswordSymbols);
            pw.print(prefix);
            pw.print("minimumPasswordNonLetter=");
            pw.println(this.minimumPasswordNonLetter);
            pw.print(prefix);
            pw.print("maximumTimeToUnlock=");
            pw.println(this.maximumTimeToUnlock);
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
            pw.print("isParent=");
            pw.println(this.isParent);
            if (this.parentAdmin != null) {
                pw.print(prefix);
                pw.println("parentAdmin:");
                this.parentAdmin.dump(prefix + "  ", pw);
            }
        }
    }

    @IntDef({0, 1, 2, 3, 4, 7})
    @Retention(RetentionPolicy.SOURCE)
    private @interface DeviceOwnerPreConditionCode {
    }

    public static class DevicePolicyData {
        boolean doNotAskCredentialsOnBoot;
        final ArraySet<String> mAcceptedCaCertificates;
        int mActivePasswordLength;
        int mActivePasswordLetters;
        int mActivePasswordLowerCase;
        int mActivePasswordNonLetter;
        int mActivePasswordNumeric;
        int mActivePasswordQuality;
        int mActivePasswordSymbols;
        int mActivePasswordUpperCase;
        boolean mAdminBroadcastPending;
        final ArrayList<ActiveAdmin> mAdminList;
        final ArrayMap<ComponentName, ActiveAdmin> mAdminMap;
        Set<String> mAffiliationIds;
        String mApplicationRestrictionsManagingPackage;
        String mDelegatedCertInstallerPackage;
        int mFailedPasswordAttempts;
        PersistableBundle mInitBundle;
        long mLastMaximumTimeToLock;
        List<String> mLockTaskPackages;
        int mPasswordOwner;
        int mPermissionPolicy;
        final ArrayList<ComponentName> mRemovingAdmins;
        ComponentName mRestrictionsProvider;
        boolean mStatusBarDisabled;
        int mUserHandle;
        int mUserProvisioningState;
        boolean mUserSetupComplete;

        public DevicePolicyData(int userHandle) {
            this.mActivePasswordQuality = DevicePolicyManagerService.CODE_OK;
            this.mActivePasswordLength = DevicePolicyManagerService.CODE_OK;
            this.mActivePasswordUpperCase = DevicePolicyManagerService.CODE_OK;
            this.mActivePasswordLowerCase = DevicePolicyManagerService.CODE_OK;
            this.mActivePasswordLetters = DevicePolicyManagerService.CODE_OK;
            this.mActivePasswordNumeric = DevicePolicyManagerService.CODE_OK;
            this.mActivePasswordSymbols = DevicePolicyManagerService.CODE_OK;
            this.mActivePasswordNonLetter = DevicePolicyManagerService.CODE_OK;
            this.mFailedPasswordAttempts = DevicePolicyManagerService.CODE_OK;
            this.mPasswordOwner = -1;
            this.mLastMaximumTimeToLock = -1;
            this.mUserSetupComplete = false;
            this.mAdminMap = new ArrayMap();
            this.mAdminList = new ArrayList();
            this.mRemovingAdmins = new ArrayList();
            this.mAcceptedCaCertificates = new ArraySet();
            this.mLockTaskPackages = new ArrayList();
            this.mStatusBarDisabled = false;
            this.doNotAskCredentialsOnBoot = false;
            this.mAffiliationIds = new ArraySet();
            this.mAdminBroadcastPending = false;
            this.mInitBundle = null;
            this.mUserHandle = userHandle;
        }
    }

    static class Injector {
        private final Context mContext;

        Injector(Context context) {
            this.mContext = context;
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

        PowerManagerInternal getPowerManagerInternal() {
            return (PowerManagerInternal) LocalServices.getService(PowerManagerInternal.class);
        }

        TelephonyManager getTelephonyManager() {
            return TelephonyManager.from(this.mContext);
        }

        TrustManager getTrustManager() {
            return (TrustManager) this.mContext.getSystemService("trust");
        }

        IWindowManager getIWindowManager() {
            return Stub.asInterface(ServiceManager.getService("window"));
        }

        IActivityManager getIActivityManager() {
            return ActivityManagerNative.getDefault();
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
            return AbsPackageManagerService.UNINSTALLED_DELAPP_DIR;
        }

        void registerContentObserver(Uri uri, boolean notifyForDescendents, ContentObserver observer, int userHandle) {
            this.mContext.getContentResolver().registerContentObserver(uri, notifyForDescendents, observer, userHandle);
        }

        int settingsSecureGetIntForUser(String name, int def, int userHandle) {
            return Secure.getIntForUser(this.mContext.getContentResolver(), name, def, userHandle);
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
            this.mService.onStartUser(userHandle);
        }
    }

    final class LocalService extends DevicePolicyManagerInternal {
        private List<OnCrossProfileWidgetProvidersChangeListener> mWidgetProviderListeners;
        final /* synthetic */ DevicePolicyManagerService this$0;

        LocalService(DevicePolicyManagerService this$0) {
            this.this$0 = this$0;
        }

        public List<String> getCrossProfileWidgetProviders(int profileId) {
            synchronized (this.this$0) {
                if (this.this$0.mOwners == null) {
                    List<String> emptyList = Collections.emptyList();
                    return emptyList;
                }
                ComponentName ownerComponent = this.this$0.mOwners.getProfileOwnerComponent(profileId);
                if (ownerComponent == null) {
                    emptyList = Collections.emptyList();
                    return emptyList;
                }
                ActiveAdmin admin = (ActiveAdmin) this.this$0.getUserDataUnchecked(profileId).mAdminMap.get(ownerComponent);
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
            synchronized (this.this$0) {
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
            synchronized (this.this$0) {
                z = this.this$0.getActiveAdminWithPolicyForUidLocked(null, reqPolicy, uid) != null;
            }
            return z;
        }

        private void notifyCrossProfileProvidersChanged(int userId, List<String> packages) {
            List<OnCrossProfileWidgetProvidersChangeListener> listeners;
            synchronized (this.this$0) {
                listeners = new ArrayList(this.mWidgetProviderListeners);
            }
            int listenerCount = listeners.size();
            for (int i = DevicePolicyManagerService.CODE_OK; i < listenerCount; i += DevicePolicyManagerService.STATUS_BAR_DISABLE2_MASK) {
                ((OnCrossProfileWidgetProvidersChangeListener) listeners.get(i)).onCrossProfileWidgetProvidersChanged(userId, packages);
            }
        }

        public Intent createPackageSuspendedDialogIntent(String packageName, int userId) {
            Intent intent = new Intent("android.settings.SHOW_ADMIN_SUPPORT_DETAILS");
            intent.putExtra("android.intent.extra.USER_ID", userId);
            intent.setFlags(268435456);
            ComponentName profileOwner = this.this$0.mOwners.getProfileOwnerComponent(userId);
            if (profileOwner != null) {
                intent.putExtra("android.app.extra.DEVICE_ADMIN", profileOwner);
                return intent;
            }
            Pair<Integer, ComponentName> deviceOwner = this.this$0.mOwners.getDeviceOwnerUserIdAndComponent();
            if (deviceOwner == null || ((Integer) deviceOwner.first).intValue() != userId) {
                return intent;
            }
            intent.putExtra("android.app.extra.DEVICE_ADMIN", (Parcelable) deviceOwner.second);
            return intent;
        }
    }

    private class MonitoringCertNotificationTask extends AsyncTask<Integer, Void, Void> {
        final /* synthetic */ DevicePolicyManagerService this$0;

        /* synthetic */ MonitoringCertNotificationTask(DevicePolicyManagerService this$0, MonitoringCertNotificationTask monitoringCertNotificationTask) {
            this(this$0);
        }

        private MonitoringCertNotificationTask(DevicePolicyManagerService this$0) {
            this.this$0 = this$0;
        }

        protected /* bridge */ /* synthetic */ Object doInBackground(Object[] params) {
            return doInBackground((Integer[]) params);
        }

        protected Void doInBackground(Integer... params) {
            int userHandle = params[DevicePolicyManagerService.CODE_OK].intValue();
            if (userHandle == -1) {
                for (UserInfo userInfo : this.this$0.mUserManager.getUsers(true)) {
                    manageNotification(userInfo.getUserHandle());
                }
            } else {
                manageNotification(UserHandle.of(userHandle));
            }
            return null;
        }

        private void manageNotification(UserHandle userHandle) {
            if (this.this$0.mUserManager.isUserUnlocked(userHandle)) {
                try {
                    List<String> pendingCertificates = getInstalledCaCertificates(userHandle);
                    synchronized (this.this$0) {
                        DevicePolicyData policy = this.this$0.getUserData(userHandle.getIdentifier());
                        if (policy.mAcceptedCaCertificates.retainAll(pendingCertificates)) {
                            this.this$0.saveSettingsLocked(userHandle.getIdentifier());
                        }
                        pendingCertificates.removeAll(policy.mAcceptedCaCertificates);
                    }
                    if (pendingCertificates.isEmpty()) {
                        this.this$0.mInjector.getNotificationManager().cancelAsUser(null, DevicePolicyManagerService.MONITORING_CERT_NOTIFICATION_ID, userHandle);
                        return;
                    }
                    String contentText;
                    int smallIconId;
                    int parentUserId = userHandle.getIdentifier();
                    Context context;
                    Object[] objArr;
                    if (this.this$0.getProfileOwner(userHandle.getIdentifier()) != null) {
                        context = this.this$0.mContext;
                        objArr = new Object[DevicePolicyManagerService.STATUS_BAR_DISABLE2_MASK];
                        objArr[DevicePolicyManagerService.CODE_OK] = this.this$0.getProfileOwnerName(userHandle.getIdentifier());
                        contentText = context.getString(17039626, objArr);
                        smallIconId = 17303249;
                        parentUserId = this.this$0.getProfileParentId(userHandle.getIdentifier());
                    } else if (this.this$0.getDeviceOwnerUserId() == userHandle.getIdentifier()) {
                        context = this.this$0.mContext;
                        objArr = new Object[DevicePolicyManagerService.STATUS_BAR_DISABLE2_MASK];
                        objArr[DevicePolicyManagerService.CODE_OK] = this.this$0.getDeviceOwnerName();
                        contentText = context.getString(17039626, objArr);
                        smallIconId = 17303249;
                    } else {
                        contentText = this.this$0.mContext.getString(17039624);
                        smallIconId = 17301642;
                    }
                    int numberOfCertificates = pendingCertificates.size();
                    Intent dialogIntent = new Intent("com.android.settings.MONITORING_CERT_INFO");
                    dialogIntent.setFlags(268468224);
                    dialogIntent.setPackage("com.android.settings");
                    dialogIntent.putExtra("android.settings.extra.number_of_certificates", numberOfCertificates);
                    dialogIntent.putExtra("android.intent.extra.USER_ID", userHandle.getIdentifier());
                    try {
                        this.this$0.mInjector.getNotificationManager().notifyAsUser(null, DevicePolicyManagerService.MONITORING_CERT_NOTIFICATION_ID, new Builder(this.this$0.mContext.createPackageContextAsUser(this.this$0.mContext.getPackageName(), DevicePolicyManagerService.CODE_OK, userHandle)).setSmallIcon(smallIconId).setContentTitle(this.this$0.mContext.getResources().getQuantityText(DevicePolicyManagerService.MONITORING_CERT_NOTIFICATION_ID, numberOfCertificates)).setContentText(contentText).setContentIntent(PendingIntent.getActivityAsUser(this.this$0.mContext, DevicePolicyManagerService.CODE_OK, dialogIntent, 134217728, null, new UserHandle(parentUserId))).setPriority(DevicePolicyManagerService.STATUS_BAR_DISABLE2_MASK).setShowWhen(false).setColor(this.this$0.mContext.getColor(17170519)).build(), userHandle);
                    } catch (NameNotFoundException e) {
                        Log.e(DevicePolicyManagerService.LOG_TAG, "Create context as " + userHandle + " failed", e);
                    }
                } catch (Exception e2) {
                    Log.e(DevicePolicyManagerService.LOG_TAG, "Could not retrieve certificates from KeyChain service", e2);
                }
            }
        }

        private List<String> getInstalledCaCertificates(UserHandle userHandle) throws RemoteException, RuntimeException {
            KeyChainConnection keyChainConnection = null;
            try {
                keyChainConnection = KeyChain.bindAsUser(this.this$0.mContext, userHandle);
                List<ParcelableString> aliases = keyChainConnection.getService().getUserCaAliases().getList();
                List<String> result = new ArrayList(aliases.size());
                for (int i = DevicePolicyManagerService.CODE_OK; i < aliases.size(); i += DevicePolicyManagerService.STATUS_BAR_DISABLE2_MASK) {
                    result.add(((ParcelableString) aliases.get(i)).string);
                }
                if (keyChainConnection != null) {
                    keyChainConnection.close();
                }
                return result;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                if (keyChainConnection != null) {
                    keyChainConnection.close();
                }
                return null;
            } catch (AssertionError e2) {
                throw new RuntimeException(e2);
            } catch (Throwable th) {
                if (keyChainConnection != null) {
                    keyChainConnection.close();
                }
            }
        }
    }

    private class SetupContentObserver extends ContentObserver {
        private final Uri mDeviceProvisioned;
        private final Uri mUserSetupComplete;
        final /* synthetic */ DevicePolicyManagerService this$0;

        public SetupContentObserver(DevicePolicyManagerService this$0, Handler handler) {
            this.this$0 = this$0;
            super(handler);
            this.mUserSetupComplete = Secure.getUriFor("user_setup_complete");
            this.mDeviceProvisioned = Global.getUriFor("device_provisioned");
        }

        void register() {
            this.this$0.mInjector.registerContentObserver(this.mUserSetupComplete, false, this, -1);
            this.this$0.mInjector.registerContentObserver(this.mDeviceProvisioned, false, this, -1);
        }

        public void onChange(boolean selfChange, Uri uri) {
            if (this.mUserSetupComplete.equals(uri)) {
                this.this$0.updateUserSetupComplete();
            } else if (this.mDeviceProvisioned.equals(uri)) {
                synchronized (this.this$0) {
                    this.this$0.setDeviceOwnerSystemPropertyLocked();
                }
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.devicepolicy.DevicePolicyManagerService.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.devicepolicy.DevicePolicyManagerService.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.devicepolicy.DevicePolicyManagerService.<clinit>():void");
    }

    private void handlePackagesChanged(String packageName, int userHandle) {
        boolean removed = false;
        DevicePolicyData policy = getUserData(userHandle);
        synchronized (this) {
            for (int i = policy.mAdminList.size() - 1; i >= 0; i--) {
                ActiveAdmin aa = (ActiveAdmin) policy.mAdminList.get(i);
                try {
                    String adminPackage = aa.info.getPackageName();
                    if ((packageName == null || packageName.equals(adminPackage)) && (this.mIPackageManager.getPackageInfo(adminPackage, CODE_OK, userHandle) == null || this.mIPackageManager.getReceiverInfo(aa.info.getComponent(), 786432, userHandle) == null)) {
                        removed = true;
                        policy.mAdminList.remove(i);
                        policy.mAdminMap.remove(aa.info.getComponent());
                    }
                } catch (RemoteException e) {
                }
            }
            if (removed) {
                validatePasswordOwnerLocked(policy);
                saveSettingsLocked(policy.mUserHandle);
            }
            if (isRemovedPackage(packageName, policy.mDelegatedCertInstallerPackage, userHandle)) {
                policy.mDelegatedCertInstallerPackage = null;
                saveSettingsLocked(policy.mUserHandle);
            }
            if (isRemovedPackage(packageName, policy.mApplicationRestrictionsManagingPackage, userHandle)) {
                policy.mApplicationRestrictionsManagingPackage = null;
                saveSettingsLocked(policy.mUserHandle);
            }
        }
        if (removed) {
            pushUserRestrictions(userHandle);
        }
    }

    /* JADX WARNING: inconsistent code. */
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
            if (this.mIPackageManager.getPackageInfo(targetPackage, CODE_OK, userHandle) == null) {
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
                if ("com.android.server.action.BUGREPORT_SHARING_ACCEPTED".equals(action)) {
                    DevicePolicyManagerService.this.onBugreportSharingAccepted();
                } else if ("com.android.server.action.BUGREPORT_SHARING_DECLINED".equals(action)) {
                    DevicePolicyManagerService.this.onBugreportSharingDeclined();
                }
                DevicePolicyManagerService.this.mContext.unregisterReceiver(DevicePolicyManagerService.this.mRemoteBugreportConsentReceiver);
            }
        };
        this.mUserData = new SparseArray();
        this.mReceiver = new BroadcastReceiver() {

            /* renamed from: com.android.server.devicepolicy.DevicePolicyManagerService.4.1 */
            class AnonymousClass1 implements Runnable {
                final /* synthetic */ int val$userHandle;

                AnonymousClass1(int val$userHandle) {
                    this.val$userHandle = val$userHandle;
                }

                public void run() {
                    DevicePolicyManagerService.this.handlePasswordExpirationNotification(this.val$userHandle);
                }
            }

            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                int userHandle = intent.getIntExtra("android.intent.extra.user_handle", getSendingUserId());
                if ("android.intent.action.BOOT_COMPLETED".equals(action) && userHandle == DevicePolicyManagerService.this.mOwners.getDeviceOwnerUserId() && DevicePolicyManagerService.this.getDeviceOwnerRemoteBugreportUri() != null) {
                    IntentFilter filterConsent = new IntentFilter();
                    filterConsent.addAction("com.android.server.action.BUGREPORT_SHARING_DECLINED");
                    filterConsent.addAction("com.android.server.action.BUGREPORT_SHARING_ACCEPTED");
                    DevicePolicyManagerService.this.mContext.registerReceiver(DevicePolicyManagerService.this.mRemoteBugreportConsentReceiver, filterConsent);
                    DevicePolicyManagerService.this.mInjector.getNotificationManager().notifyAsUser(DevicePolicyManagerService.LOG_TAG, 678432343, RemoteBugreportUtils.buildNotification(DevicePolicyManagerService.this.mContext, DevicePolicyManagerService.CODE_USER_NOT_RUNNING), UserHandle.ALL);
                }
                if ("android.intent.action.BOOT_COMPLETED".equals(action) || DevicePolicyManagerService.ACTION_EXPIRED_PASSWORD_NOTIFICATION.equals(action)) {
                    DevicePolicyManagerService.this.mHandler.post(new AnonymousClass1(userHandle));
                }
                if ("android.intent.action.USER_UNLOCKED".equals(action) || "android.intent.action.USER_STARTED".equals(action) || "android.security.STORAGE_CHANGED".equals(action)) {
                    int userId = intent.getIntExtra("android.intent.extra.user_handle", -1);
                    MonitoringCertNotificationTask monitoringCertNotificationTask = new MonitoringCertNotificationTask(DevicePolicyManagerService.this, null);
                    Integer[] numArr = new Integer[DevicePolicyManagerService.STATUS_BAR_DISABLE2_MASK];
                    numArr[DevicePolicyManagerService.CODE_OK] = Integer.valueOf(userId);
                    monitoringCertNotificationTask.execute(numArr);
                }
                if ("android.intent.action.USER_ADDED".equals(action)) {
                    DevicePolicyManagerService.this.disableSecurityLoggingIfNotCompliant();
                } else if ("android.intent.action.USER_REMOVED".equals(action)) {
                    DevicePolicyManagerService.this.disableSecurityLoggingIfNotCompliant();
                    DevicePolicyManagerService.this.removeUserData(userHandle);
                } else if ("android.intent.action.USER_STARTED".equals(action)) {
                    synchronized (DevicePolicyManagerService.this) {
                        DevicePolicyManagerService.this.mUserData.remove(userHandle);
                        DevicePolicyManagerService.this.sendAdminEnabledBroadcastLocked(userHandle);
                    }
                    DevicePolicyManagerService.this.handlePackagesChanged(null, userHandle);
                } else if ("android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE".equals(action)) {
                    DevicePolicyManagerService.this.handlePackagesChanged(null, userHandle);
                } else if ("android.intent.action.PACKAGE_CHANGED".equals(action) || ("android.intent.action.PACKAGE_ADDED".equals(action) && intent.getBooleanExtra("android.intent.extra.REPLACING", false))) {
                    DevicePolicyManagerService.this.handlePackagesChanged(intent.getData().getSchemeSpecificPart(), userHandle);
                } else if ("android.intent.action.PACKAGE_REMOVED".equals(action) && !intent.getBooleanExtra("android.intent.extra.REPLACING", false)) {
                    DevicePolicyManagerService.this.handlePackagesChanged(intent.getData().getSchemeSpecificPart(), userHandle);
                } else if ("android.intent.action.MANAGED_PROFILE_ADDED".equals(action)) {
                    DevicePolicyManagerService.this.clearWipeProfileNotification();
                }
            }
        };
        this.mInjector = injector;
        this.mContext = (Context) Preconditions.checkNotNull(injector.mContext);
        this.mHandler = new Handler((Looper) Preconditions.checkNotNull(injector.getMyLooper()));
        this.mOwners = (Owners) Preconditions.checkNotNull(injector.newOwners());
        this.mUserManager = (UserManager) Preconditions.checkNotNull(injector.getUserManager());
        this.mUserManagerInternal = (UserManagerInternal) Preconditions.checkNotNull(injector.getUserManagerInternal());
        this.mIPackageManager = (IPackageManager) Preconditions.checkNotNull(injector.getIPackageManager());
        this.mTelephonyManager = (TelephonyManager) Preconditions.checkNotNull(injector.getTelephonyManager());
        this.mLocalService = new LocalService(this);
        this.mLockPatternUtils = injector.newLockPatternUtils();
        this.mSecurityLogMonitor = new SecurityLogMonitor(this);
        this.mHasFeature = this.mContext.getPackageManager().hasSystemFeature("android.software.device_admin");
        if (this.mHasFeature) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.BOOT_COMPLETED");
            filter.addAction(ACTION_EXPIRED_PASSWORD_NOTIFICATION);
            filter.addAction("android.intent.action.USER_ADDED");
            filter.addAction("android.intent.action.USER_REMOVED");
            filter.addAction("android.intent.action.USER_STARTED");
            filter.addAction("android.intent.action.USER_UNLOCKED");
            filter.addAction("android.security.STORAGE_CHANGED");
            filter.setPriority(ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE);
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
            return;
        }
    }

    void loadOwners() {
        synchronized (this) {
            this.mOwners.load();
            setDeviceOwnerSystemPropertyLocked();
            findOwnerComponentIfNecessaryLocked();
            migrateUserRestrictionsIfNecessaryLocked();
            updateDeviceOwnerLocked();
        }
    }

    private void setDeviceOwnerSystemPropertyLocked() {
        if (this.mInjector.settingsGlobalGetInt("device_provisioned", CODE_OK) != 0 && !StorageManager.inCryptKeeperBounce()) {
            if (!TextUtils.isEmpty(this.mInjector.systemPropertiesGet(PROPERTY_DEVICE_OWNER_PRESENT))) {
                Slog.w(LOG_TAG, "Trying to set ro.device_owner, but it has already been set?");
            } else if (this.mOwners.hasDeviceOwner()) {
                this.mInjector.systemPropertiesSet(PROPERTY_DEVICE_OWNER_PRESENT, "true");
                Slog.i(LOG_TAG, "Set ro.device_owner property to true");
                disableSecurityLoggingIfNotCompliant();
                if (this.mInjector.securityLogGetLoggingEnabledProperty()) {
                    this.mSecurityLogMonitor.start();
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
                    this.mOwners.setDeviceOwnerWithRestrictionsMigrated(doComponent, this.mOwners.getDeviceOwnerName(), this.mOwners.getDeviceOwnerUserId(), !this.mOwners.getDeviceOwnerUserRestrictionsNeedsMigration());
                    this.mOwners.writeDeviceOwner();
                }
            }
        }
    }

    private void migrateUserRestrictionsIfNecessaryLocked() {
        if (this.mOwners.getDeviceOwnerUserRestrictionsNeedsMigration()) {
            migrateUserRestrictionsForUser(UserHandle.SYSTEM, getDeviceOwnerAdminLocked(), null, true);
            pushUserRestrictions(CODE_OK);
            this.mOwners.setDeviceOwnerUserRestrictionsMigrated();
        }
        String[] strArr = new String[CODE_USER_HAS_PROFILE_OWNER];
        strArr[CODE_OK] = "no_outgoing_calls";
        strArr[STATUS_BAR_DISABLE2_MASK] = "no_sms";
        Set<String> secondaryUserExceptionList = Sets.newArraySet(strArr);
        for (UserInfo ui : this.mUserManager.getUsers()) {
            int userId = ui.id;
            if (this.mOwners.getProfileOwnerUserRestrictionsNeedsMigration(userId)) {
                Set set;
                ActiveAdmin profileOwnerAdmin = getProfileOwnerAdminLocked(userId);
                if (userId == 0) {
                    set = null;
                } else {
                    Set<String> exceptionList = secondaryUserExceptionList;
                }
                migrateUserRestrictionsForUser(ui.getUserHandle(), profileOwnerAdmin, set, false);
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
        int nFound = CODE_OK;
        for (int i = CODE_OK; i < n; i += STATUS_BAR_DISABLE2_MASK) {
            ActiveAdmin admin = (ActiveAdmin) policy.mAdminList.get(i);
            if (packageName.equals(admin.info.getPackageName())) {
                if (nFound == 0) {
                    found = admin.info.getComponent();
                }
                nFound += STATUS_BAR_DISABLE2_MASK;
            }
        }
        if (nFound > STATUS_BAR_DISABLE2_MASK) {
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
        AlarmManager am = (AlarmManager) context.getSystemService("alarm");
        PendingIntent pi = PendingIntent.getBroadcastAsUser(context, REQUEST_EXPIRE_PASSWORD, new Intent(ACTION_EXPIRED_PASSWORD_NOTIFICATION), 1207959552, UserHandle.of(affectedUserHandle));
        am.cancel(pi);
        if (alarmTime != 0) {
            am.set(STATUS_BAR_DISABLE2_MASK, alarmTime, pi);
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

    void sendAdminCommandLocked(String action, int reqPolicy, int userHandle) {
        DevicePolicyData policy = getUserData(userHandle);
        int count = policy.mAdminList.size();
        if (count > 0) {
            for (int i = CODE_OK; i < count; i += STATUS_BAR_DISABLE2_MASK) {
                ActiveAdmin admin = (ActiveAdmin) policy.mAdminList.get(i);
                if (admin.info.usesPolicy(reqPolicy)) {
                    sendAdminCommandLocked(admin, action);
                }
            }
        }
    }

    private void sendAdminCommandToSelfAndProfilesLocked(String action, int reqPolicy, int userHandle) {
        int[] profileIds = this.mUserManager.getProfileIdsWithDisabled(userHandle);
        int length = profileIds.length;
        for (int i = CODE_OK; i < length; i += STATUS_BAR_DISABLE2_MASK) {
            sendAdminCommandLocked(action, reqPolicy, profileIds[i]);
        }
    }

    private void sendAdminCommandForLockscreenPoliciesLocked(String action, int reqPolicy, int userHandle) {
        if (isSeparateProfileChallengeEnabled(userHandle)) {
            sendAdminCommandLocked(action, reqPolicy, userHandle);
        } else {
            sendAdminCommandToSelfAndProfilesLocked(action, reqPolicy, userHandle);
        }
    }

    void removeActiveAdminLocked(ComponentName adminReceiver, int userHandle) {
        ActiveAdmin admin = getActiveAdminUncheckedLocked(adminReceiver, userHandle);
        if (admin != null) {
            getUserData(userHandle).mRemovingAdmins.add(adminReceiver);
            sendAdminCommandLocked(admin, "android.app.action.DEVICE_ADMIN_DISABLED", new AnonymousClass5(adminReceiver, userHandle));
            Flog.i(305, "removeActiveAdminLocked(" + adminReceiver + "), by user " + userHandle);
        }
    }

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
            Slog.w(LOG_TAG, "Bad device admin requested for user=" + userHandle + ": " + adminName, e2);
            return null;
        }
    }

    private JournaledFile makeJournaledFile(int userHandle) {
        String base;
        if (userHandle == 0) {
            base = this.mInjector.getDevicePolicyFilePathForSystemUser() + DEVICE_POLICIES_XML;
        } else {
            base = new File(this.mInjector.environmentGetUserSystemDirectory(userHandle), DEVICE_POLICIES_XML).getAbsolutePath();
        }
        return new JournaledFile(new File(base), new File(base + ".tmp"));
    }

    void saveSettingsLocked(int userHandle) {
        Exception e;
        DevicePolicyData policy = getUserData(userHandle);
        JournaledFile journal = makeJournaledFile(userHandle);
        FileOutputStream fileOutputStream = null;
        try {
            FileOutputStream stream = new FileOutputStream(journal.chooseForWrite(), false);
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
                if (policy.mUserProvisioningState != 0) {
                    out.attribute(null, ATTR_PROVISIONING_STATE, Integer.toString(policy.mUserProvisioningState));
                }
                if (policy.mPermissionPolicy != 0) {
                    out.attribute(null, ATTR_PERMISSION_POLICY, Integer.toString(policy.mPermissionPolicy));
                }
                if (policy.mDelegatedCertInstallerPackage != null) {
                    out.attribute(null, ATTR_DELEGATED_CERT_INSTALLER, policy.mDelegatedCertInstallerPackage);
                }
                if (policy.mApplicationRestrictionsManagingPackage != null) {
                    out.attribute(null, ATTR_APPLICATION_RESTRICTIONS_MANAGER, policy.mApplicationRestrictionsManagingPackage);
                }
                int N = policy.mAdminList.size();
                for (i = CODE_OK; i < N; i += STATUS_BAR_DISABLE2_MASK) {
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
                if (!this.mInjector.storageManagerIsFileBasedEncryptionEnabled()) {
                    if (policy.mActivePasswordQuality == 0 && policy.mActivePasswordLength == 0 && policy.mActivePasswordUpperCase == 0 && policy.mActivePasswordLowerCase == 0 && policy.mActivePasswordLetters == 0 && policy.mActivePasswordNumeric == 0 && policy.mActivePasswordSymbols == 0) {
                        if (policy.mActivePasswordNonLetter != 0) {
                        }
                    }
                    out.startTag(null, "active-password");
                    out.attribute(null, "quality", Integer.toString(policy.mActivePasswordQuality));
                    out.attribute(null, "length", Integer.toString(policy.mActivePasswordLength));
                    out.attribute(null, "uppercase", Integer.toString(policy.mActivePasswordUpperCase));
                    out.attribute(null, "lowercase", Integer.toString(policy.mActivePasswordLowerCase));
                    out.attribute(null, "letters", Integer.toString(policy.mActivePasswordLetters));
                    out.attribute(null, "numeric", Integer.toString(policy.mActivePasswordNumeric));
                    out.attribute(null, "symbols", Integer.toString(policy.mActivePasswordSymbols));
                    out.attribute(null, "nonletter", Integer.toString(policy.mActivePasswordNonLetter));
                    out.endTag(null, "active-password");
                }
                for (i = CODE_OK; i < policy.mAcceptedCaCertificates.size(); i += STATUS_BAR_DISABLE2_MASK) {
                    out.startTag(null, TAG_ACCEPTED_CA_CERTIFICATES);
                    out.attribute(null, ATTR_NAME, (String) policy.mAcceptedCaCertificates.valueAt(i));
                    out.endTag(null, TAG_ACCEPTED_CA_CERTIFICATES);
                }
                for (i = CODE_OK; i < policy.mLockTaskPackages.size(); i += STATUS_BAR_DISABLE2_MASK) {
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
                    out.attribute(null, "id", id);
                    out.endTag(null, TAG_AFFILIATION_ID);
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
                out.endTag(null, "policies");
                out.endDocument();
                stream.flush();
                FileUtils.sync(stream);
                stream.close();
                journal.commit();
                sendChangedNotification(userHandle);
            } catch (XmlPullParserException e2) {
                e = e2;
                fileOutputStream = stream;
                Slog.w(LOG_TAG, "failed writing file", e);
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e3) {
                    }
                }
                journal.rollback();
            }
        } catch (XmlPullParserException e4) {
            e = e4;
            Slog.w(LOG_TAG, "failed writing file", e);
            if (fileOutputStream != null) {
                fileOutputStream.close();
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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void loadSettingsLocked(DevicePolicyData policy, int userHandle) {
        Exception e;
        FileInputStream fileInputStream = null;
        File file = makeJournaledFile(userHandle).chooseForRead();
        try {
            InputStream fileInputStream2 = new FileInputStream(file);
            try {
                int type;
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(fileInputStream2, StandardCharsets.UTF_8.name());
                do {
                    type = parser.next();
                    if (type == STATUS_BAR_DISABLE2_MASK) {
                        break;
                    }
                } while (type != CODE_USER_HAS_PROFILE_OWNER);
                String tag = parser.getName();
                if ("policies".equals(tag)) {
                    InputStream stream;
                    String permissionProvider = parser.getAttributeValue(null, ATTR_PERMISSION_PROVIDER);
                    if (permissionProvider != null) {
                        policy.mRestrictionsProvider = ComponentName.unflattenFromString(permissionProvider);
                    }
                    String userSetupComplete = parser.getAttributeValue(null, ATTR_SETUP_COMPLETE);
                    if (userSetupComplete != null) {
                        if (Boolean.toString(true).equals(userSetupComplete)) {
                            policy.mUserSetupComplete = true;
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
                    policy.mDelegatedCertInstallerPackage = parser.getAttributeValue(null, ATTR_DELEGATED_CERT_INSTALLER);
                    policy.mApplicationRestrictionsManagingPackage = parser.getAttributeValue(null, ATTR_APPLICATION_RESTRICTIONS_MANAGER);
                    type = parser.next();
                    int outerDepth = parser.getDepth();
                    policy.mLockTaskPackages.clear();
                    policy.mAdminList.clear();
                    policy.mAdminMap.clear();
                    policy.mAffiliationIds.clear();
                    while (true) {
                        type = parser.next();
                        if (type == STATUS_BAR_DISABLE2_MASK || (type == CODE_USER_NOT_RUNNING && parser.getDepth() <= outerDepth)) {
                            stream = fileInputStream2;
                        } else if (!(type == CODE_USER_NOT_RUNNING || type == CODE_USER_SETUP_COMPLETED)) {
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
                                } catch (RuntimeException e2) {
                                    Slog.w(LOG_TAG, "Failed loading admin " + name, e2);
                                }
                            } else {
                                if ("failed-password-attempts".equals(tag)) {
                                    policy.mFailedPasswordAttempts = Integer.parseInt(parser.getAttributeValue(null, ATTR_VALUE));
                                } else {
                                    if ("password-owner".equals(tag)) {
                                        policy.mPasswordOwner = Integer.parseInt(parser.getAttributeValue(null, ATTR_VALUE));
                                    } else {
                                        if (TAG_ACCEPTED_CA_CERTIFICATES.equals(tag)) {
                                            policy.mAcceptedCaCertificates.add(parser.getAttributeValue(null, ATTR_NAME));
                                        } else {
                                            if (TAG_LOCK_TASK_COMPONENTS.equals(tag)) {
                                                policy.mLockTaskPackages.add(parser.getAttributeValue(null, ATTR_NAME));
                                            } else {
                                                if (TAG_STATUS_BAR.equals(tag)) {
                                                    policy.mStatusBarDisabled = Boolean.parseBoolean(parser.getAttributeValue(null, ATTR_DISABLED));
                                                } else {
                                                    if (DO_NOT_ASK_CREDENTIALS_ON_BOOT_XML.equals(tag)) {
                                                        policy.doNotAskCredentialsOnBoot = true;
                                                    } else {
                                                        if (TAG_AFFILIATION_ID.equals(tag)) {
                                                            policy.mAffiliationIds.add(parser.getAttributeValue(null, "id"));
                                                        } else {
                                                            if (TAG_ADMIN_BROADCAST_PENDING.equals(tag)) {
                                                                String pending = parser.getAttributeValue(null, ATTR_VALUE);
                                                                policy.mAdminBroadcastPending = Boolean.toString(true).equals(pending);
                                                            } else {
                                                                if (TAG_INITIALIZATION_BUNDLE.equals(tag)) {
                                                                    policy.mInitBundle = PersistableBundle.restoreFromXml(parser);
                                                                } else {
                                                                    if ("active-password".equals(tag)) {
                                                                        policy.mActivePasswordQuality = Integer.parseInt(parser.getAttributeValue(null, "quality"));
                                                                        policy.mActivePasswordLength = Integer.parseInt(parser.getAttributeValue(null, "length"));
                                                                        policy.mActivePasswordUpperCase = Integer.parseInt(parser.getAttributeValue(null, "uppercase"));
                                                                        policy.mActivePasswordLowerCase = Integer.parseInt(parser.getAttributeValue(null, "lowercase"));
                                                                        policy.mActivePasswordLetters = Integer.parseInt(parser.getAttributeValue(null, "letters"));
                                                                        policy.mActivePasswordNumeric = Integer.parseInt(parser.getAttributeValue(null, "numeric"));
                                                                        policy.mActivePasswordSymbols = Integer.parseInt(parser.getAttributeValue(null, "symbols"));
                                                                        policy.mActivePasswordNonLetter = Integer.parseInt(parser.getAttributeValue(null, "nonletter"));
                                                                    } else {
                                                                        Slog.w(LOG_TAG, "Unknown tag: " + tag);
                                                                        XmlUtils.skipCurrentTag(parser);
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    stream = fileInputStream2;
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e3) {
                        }
                    }
                    policy.mAdminList.addAll(policy.mAdminMap.values());
                    if (false) {
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
            } catch (FileNotFoundException e4) {
                fileInputStream = fileInputStream2;
            } catch (NullPointerException e5) {
                e = e5;
                fileInputStream = fileInputStream2;
            }
        } catch (FileNotFoundException e6) {
        } catch (NullPointerException e7) {
            e = e7;
            Slog.w(LOG_TAG, "failed parsing " + file, e);
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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void updateDeviceOwnerLocked() {
        long ident = this.mInjector.binderClearCallingIdentity();
        try {
            ComponentName deviceOwnerComponent = this.mOwners.getDeviceOwnerComponent();
            if (deviceOwnerComponent != null) {
                this.mInjector.getIActivityManager().updateDeviceOwner(deviceOwnerComponent.getPackageName());
            }
            this.mInjector.binderRestoreCallingIdentity(ident);
        } catch (RemoteException e) {
        } catch (Throwable th) {
            this.mInjector.binderRestoreCallingIdentity(ident);
        }
    }

    static void validateQualityConstant(int quality) {
        switch (quality) {
            case CODE_OK /*0*/:
            case DumpState.DUMP_VERSION /*32768*/:
            case DumpState.DUMP_INSTALLS /*65536*/:
            case DumpState.DUMP_INTENT_FILTER_VERIFIERS /*131072*/:
            case 196608:
            case DumpState.DUMP_DOMAIN_PREFERRED /*262144*/:
            case 327680:
            case 393216:
            case DumpState.DUMP_FROZEN /*524288*/:
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
                case SystemService.PHASE_LOCK_SETTINGS_READY /*480*/:
                    onLockSettingsReady();
                    break;
                case ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE /*1000*/:
                    ensureDeviceOwnerUserStarted();
                    break;
            }
        }
    }

    private void onLockSettingsReady() {
        getUserData(CODE_OK);
        loadOwners();
        cleanUpOldUsers();
        onStartUser(CODE_OK);
        new SetupContentObserver(this, this.mHandler).register();
        updateUserSetupComplete();
        synchronized (this) {
            List<String> packageList = getKeepUninstalledPackagesLocked();
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

    private void ensureDeviceOwnerUserStarted() {
        synchronized (this) {
            if (this.mOwners.hasDeviceOwner()) {
                int userId = this.mOwners.getDeviceOwnerUserId();
                if (userId != 0) {
                    try {
                        this.mInjector.getIActivityManager().startUserInBackground(userId);
                    } catch (RemoteException e) {
                        Slog.w(LOG_TAG, "Exception starting user", e);
                    }
                }
                return;
            }
        }
    }

    private void onStartUser(int userId) {
        updateScreenCaptureDisabledInWindowManager(userId, getScreenCaptureDisabled(null, userId));
        pushUserRestrictions(userId);
    }

    private void cleanUpOldUsers() {
        Set<Integer> usersWithProfileOwners;
        Set<Integer> usersWithData;
        synchronized (this) {
            usersWithProfileOwners = this.mOwners.getProfileOwnerKeys();
            usersWithData = new ArraySet();
            for (int i = CODE_OK; i < this.mUserData.size(); i += STATUS_BAR_DISABLE2_MASK) {
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
        synchronized (this) {
            long now = System.currentTimeMillis();
            List<ActiveAdmin> admins = getActiveAdminsForLockscreenPoliciesLocked(userHandle, false);
            int N = admins.size();
            for (int i = CODE_OK; i < N; i += STATUS_BAR_DISABLE2_MASK) {
                ActiveAdmin admin = (ActiveAdmin) admins.get(i);
                if (admin.info.usesPolicy(CODE_ACCOUNTS_NOT_EMPTY) && admin.passwordExpirationTimeout > 0 && now >= admin.passwordExpirationDate - EXPIRATION_GRACE_PERIOD_MS && admin.passwordExpirationDate > 0) {
                    sendAdminCommandLocked(admin, "android.app.action.ACTION_PASSWORD_EXPIRING");
                }
            }
            setExpirationAlarmCheckLocked(this.mContext, userHandle, false);
        }
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
        } else if (info.getActivityInfo().applicationInfo.isInternal()) {
            synchronized (this) {
                long ident = this.mInjector.binderClearCallingIdentity();
                if (!refreshing) {
                    try {
                        if (getActiveAdminUncheckedLocked(adminReceiver, userHandle) != null) {
                            throw new IllegalArgumentException("Admin is already added");
                        }
                    } catch (Throwable th) {
                        this.mInjector.binderRestoreCallingIdentity(ident);
                    }
                }
                if (policy.mRemovingAdmins.contains(adminReceiver)) {
                    throw new IllegalArgumentException("Trying to set an admin which is being removed");
                }
                ActiveAdmin newAdmin = new ActiveAdmin(info, false);
                policy.mAdminMap.put(adminReceiver, newAdmin);
                int replaceIndex = -1;
                int N = policy.mAdminList.size();
                for (int i = CODE_OK; i < N; i += STATUS_BAR_DISABLE2_MASK) {
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
        } else {
            throw new IllegalArgumentException("Only apps in internal storage can be active admin: " + adminReceiver);
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
            for (int i = CODE_OK; i < N; i += STATUS_BAR_DISABLE2_MASK) {
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
            for (int i = CODE_OK; i < N; i += STATUS_BAR_DISABLE2_MASK) {
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
                ApplicationInfo ai = this.mIPackageManager.getApplicationInfo(adminReceiver.getPackageName(), CODE_OK, userHandle);
                if (ai == null) {
                    throw new IllegalStateException("Couldn't find package to remove admin " + adminReceiver.getPackageName() + " " + userHandle);
                } else if ((ai.flags & DumpState.DUMP_SHARED_USERS) == 0) {
                    throw new SecurityException("Attempt to remove non-test admin " + adminReceiver + adminReceiver + " " + userHandle);
                } else {
                    synchronized (this) {
                        if (isDeviceOwner(adminReceiver, userHandle)) {
                            clearDeviceOwnerLocked(getDeviceOwnerAdminLocked(), userHandle);
                        }
                        if (isProfileOwner(adminReceiver, userHandle)) {
                            clearProfileOwnerLocked(getActiveAdminUncheckedLocked(adminReceiver, userHandle, false), userHandle);
                        }
                    }
                    removeAdminArtifacts(adminReceiver, userHandle);
                    this.mInjector.binderRestoreCallingIdentity(ident);
                }
            } catch (RemoteException e) {
                throw new IllegalStateException(e);
            } catch (Throwable th) {
                this.mInjector.binderRestoreCallingIdentity(ident);
            }
        }
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
                ActiveAdmin ap = getActiveAdminForCallerLocked(who, CODE_OK, parent);
                if (ap.passwordQuality != quality) {
                    ap.passwordQuality = quality;
                    saveSettingsLocked(this.mInjector.userHandleGetCallingUserId());
                }
            }
        }
    }

    public int getPasswordQuality(ComponentName who, int userHandle, boolean parent) {
        int i = CODE_OK;
        if (!this.mHasFeature) {
            return CODE_OK;
        }
        enforceFullCrossUsersPermission(userHandle);
        synchronized (this) {
            int mode = CODE_OK;
            ActiveAdmin admin;
            if (who != null) {
                admin = getActiveAdminUncheckedLocked(who, userHandle, parent);
                if (admin != null) {
                    i = admin.passwordQuality;
                }
                return i;
            }
            List<ActiveAdmin> admins = getActiveAdminsForLockscreenPoliciesLocked(userHandle, parent);
            int N = admins.size();
            for (int i2 = CODE_OK; i2 < N; i2 += STATUS_BAR_DISABLE2_MASK) {
                admin = (ActiveAdmin) admins.get(i2);
                if (mode < admin.passwordQuality) {
                    mode = admin.passwordQuality;
                }
            }
            return mode;
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
                for (int i = CODE_OK; i < N; i += STATUS_BAR_DISABLE2_MASK) {
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
                ActiveAdmin ap = getActiveAdminForCallerLocked(who, CODE_OK, parent);
                if (ap.minimumPasswordLength != length) {
                    ap.minimumPasswordLength = length;
                    saveSettingsLocked(this.mInjector.userHandleGetCallingUserId());
                }
            }
        }
    }

    public int getPasswordMinimumLength(ComponentName who, int userHandle, boolean parent) {
        int i = CODE_OK;
        if (!this.mHasFeature) {
            return CODE_OK;
        }
        enforceFullCrossUsersPermission(userHandle);
        synchronized (this) {
            int length = CODE_OK;
            ActiveAdmin admin;
            if (who != null) {
                admin = getActiveAdminUncheckedLocked(who, userHandle, parent);
                if (admin != null) {
                    i = admin.minimumPasswordLength;
                }
                return i;
            }
            List<ActiveAdmin> admins = getActiveAdminsForLockscreenPoliciesLocked(userHandle, parent);
            int N = admins.size();
            for (int i2 = CODE_OK; i2 < N; i2 += STATUS_BAR_DISABLE2_MASK) {
                admin = (ActiveAdmin) admins.get(i2);
                if (length < admin.minimumPasswordLength) {
                    length = admin.minimumPasswordLength;
                }
            }
            return length;
        }
    }

    public void setPasswordHistoryLength(ComponentName who, int length, boolean parent) {
        if (this.mHasFeature) {
            Preconditions.checkNotNull(who, "ComponentName is null");
            synchronized (this) {
                ActiveAdmin ap = getActiveAdminForCallerLocked(who, CODE_OK, parent);
                if (ap.passwordHistoryLength != length) {
                    ap.passwordHistoryLength = length;
                    saveSettingsLocked(this.mInjector.userHandleGetCallingUserId());
                }
            }
        }
    }

    public int getPasswordHistoryLength(ComponentName who, int userHandle, boolean parent) {
        int i = CODE_OK;
        if (!this.mHasFeature) {
            return CODE_OK;
        }
        enforceFullCrossUsersPermission(userHandle);
        synchronized (this) {
            int length = CODE_OK;
            ActiveAdmin admin;
            if (who != null) {
                admin = getActiveAdminUncheckedLocked(who, userHandle, parent);
                if (admin != null) {
                    i = admin.passwordHistoryLength;
                }
                return i;
            }
            List<ActiveAdmin> admins = getActiveAdminsForLockscreenPoliciesLocked(userHandle, parent);
            int N = admins.size();
            for (int i2 = CODE_OK; i2 < N; i2 += STATUS_BAR_DISABLE2_MASK) {
                admin = (ActiveAdmin) admins.get(i2);
                if (length < admin.passwordHistoryLength) {
                    length = admin.passwordHistoryLength;
                }
            }
            return length;
        }
    }

    public void setPasswordExpirationTimeout(ComponentName who, long timeout, boolean parent) {
        if (this.mHasFeature) {
            Preconditions.checkNotNull(who, "ComponentName is null");
            Preconditions.checkArgumentNonnegative(timeout, "Timeout must be >= 0 ms");
            int userHandle = this.mInjector.userHandleGetCallingUserId();
            synchronized (this) {
                ActiveAdmin ap = getActiveAdminForCallerLocked(who, CODE_ACCOUNTS_NOT_EMPTY, parent);
                long expiration = timeout > 0 ? timeout + System.currentTimeMillis() : 0;
                ap.passwordExpirationDate = expiration;
                ap.passwordExpirationTimeout = timeout;
                if (timeout > 0) {
                    Slog.w(LOG_TAG, "setPasswordExpiration(): password will expire on " + DateFormat.getDateTimeInstance(CODE_USER_HAS_PROFILE_OWNER, CODE_USER_HAS_PROFILE_OWNER).format(new Date(expiration)));
                }
                saveSettingsLocked(userHandle);
                setExpirationAlarmCheckLocked(this.mContext, userHandle, parent);
            }
        }
    }

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
                return j;
            }
            List<ActiveAdmin> admins = getActiveAdminsForLockscreenPoliciesLocked(userHandle, parent);
            int N = admins.size();
            for (int i = CODE_OK; i < N; i += STATUS_BAR_DISABLE2_MASK) {
                admin = (ActiveAdmin) admins.get(i);
                if (timeout == 0 || (admin.passwordExpirationTimeout != 0 && timeout > admin.passwordExpirationTimeout)) {
                    timeout = admin.passwordExpirationTimeout;
                }
            }
            return timeout;
        }
    }

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
                if (list == null) {
                    return false;
                }
                this.mLocalService.notifyCrossProfileProvidersChanged(userId, list);
                return true;
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
    }

    public boolean removeCrossProfileWidgetProvider(ComponentName admin, String packageName) {
        Throwable th;
        int userId = UserHandle.getCallingUserId();
        List list = null;
        synchronized (this) {
            try {
                ActiveAdmin activeAdmin = getActiveAdminForCallerLocked(admin, -1);
                if (activeAdmin.crossProfileWidgetProviders == null) {
                    return false;
                }
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
                if (list == null) {
                    return false;
                }
                this.mLocalService.notifyCrossProfileProvidersChanged(userId, list);
                return true;
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
    }

    public List<String> getCrossProfileWidgetProviders(ComponentName admin) {
        synchronized (this) {
            ActiveAdmin activeAdmin = getActiveAdminForCallerLocked(admin, -1);
            if (activeAdmin.crossProfileWidgetProviders == null || activeAdmin.crossProfileWidgetProviders.isEmpty()) {
                return null;
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
        for (int i = CODE_OK; i < N; i += STATUS_BAR_DISABLE2_MASK) {
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
                ActiveAdmin ap = getActiveAdminForCallerLocked(who, CODE_OK, parent);
                if (ap.minimumPasswordUpperCase != length) {
                    ap.minimumPasswordUpperCase = length;
                    saveSettingsLocked(this.mInjector.userHandleGetCallingUserId());
                }
            }
        }
    }

    public int getPasswordMinimumUpperCase(ComponentName who, int userHandle, boolean parent) {
        int i = CODE_OK;
        if (!this.mHasFeature) {
            return CODE_OK;
        }
        enforceFullCrossUsersPermission(userHandle);
        synchronized (this) {
            int length = CODE_OK;
            ActiveAdmin admin;
            if (who != null) {
                admin = getActiveAdminUncheckedLocked(who, userHandle, parent);
                if (admin != null) {
                    i = admin.minimumPasswordUpperCase;
                }
                return i;
            }
            List<ActiveAdmin> admins = getActiveAdminsForLockscreenPoliciesLocked(userHandle, parent);
            int N = admins.size();
            for (int i2 = CODE_OK; i2 < N; i2 += STATUS_BAR_DISABLE2_MASK) {
                admin = (ActiveAdmin) admins.get(i2);
                if (length < admin.minimumPasswordUpperCase) {
                    length = admin.minimumPasswordUpperCase;
                }
            }
            return length;
        }
    }

    public void setPasswordMinimumLowerCase(ComponentName who, int length, boolean parent) {
        Preconditions.checkNotNull(who, "ComponentName is null");
        synchronized (this) {
            ActiveAdmin ap = getActiveAdminForCallerLocked(who, CODE_OK, parent);
            if (ap.minimumPasswordLowerCase != length) {
                ap.minimumPasswordLowerCase = length;
                saveSettingsLocked(this.mInjector.userHandleGetCallingUserId());
            }
        }
    }

    public int getPasswordMinimumLowerCase(ComponentName who, int userHandle, boolean parent) {
        int i = CODE_OK;
        if (!this.mHasFeature) {
            return CODE_OK;
        }
        enforceFullCrossUsersPermission(userHandle);
        synchronized (this) {
            int length = CODE_OK;
            ActiveAdmin admin;
            if (who != null) {
                admin = getActiveAdminUncheckedLocked(who, userHandle, parent);
                if (admin != null) {
                    i = admin.minimumPasswordLowerCase;
                }
                return i;
            }
            List<ActiveAdmin> admins = getActiveAdminsForLockscreenPoliciesLocked(userHandle, parent);
            int N = admins.size();
            for (int i2 = CODE_OK; i2 < N; i2 += STATUS_BAR_DISABLE2_MASK) {
                admin = (ActiveAdmin) admins.get(i2);
                if (length < admin.minimumPasswordLowerCase) {
                    length = admin.minimumPasswordLowerCase;
                }
            }
            return length;
        }
    }

    public void setPasswordMinimumLetters(ComponentName who, int length, boolean parent) {
        if (this.mHasFeature) {
            Preconditions.checkNotNull(who, "ComponentName is null");
            synchronized (this) {
                ActiveAdmin ap = getActiveAdminForCallerLocked(who, CODE_OK, parent);
                if (ap.minimumPasswordLetters != length) {
                    ap.minimumPasswordLetters = length;
                    saveSettingsLocked(this.mInjector.userHandleGetCallingUserId());
                }
            }
        }
    }

    public int getPasswordMinimumLetters(ComponentName who, int userHandle, boolean parent) {
        int i = CODE_OK;
        if (!this.mHasFeature) {
            return CODE_OK;
        }
        enforceFullCrossUsersPermission(userHandle);
        synchronized (this) {
            int length = CODE_OK;
            ActiveAdmin admin;
            if (who != null) {
                admin = getActiveAdminUncheckedLocked(who, userHandle, parent);
                if (admin != null) {
                    i = admin.minimumPasswordLetters;
                }
                return i;
            }
            List<ActiveAdmin> admins = getActiveAdminsForLockscreenPoliciesLocked(userHandle, parent);
            int N = admins.size();
            for (int i2 = CODE_OK; i2 < N; i2 += STATUS_BAR_DISABLE2_MASK) {
                admin = (ActiveAdmin) admins.get(i2);
                if (isLimitPasswordAllowed(admin, 393216) && length < admin.minimumPasswordLetters) {
                    length = admin.minimumPasswordLetters;
                }
            }
            return length;
        }
    }

    public void setPasswordMinimumNumeric(ComponentName who, int length, boolean parent) {
        if (this.mHasFeature) {
            Preconditions.checkNotNull(who, "ComponentName is null");
            synchronized (this) {
                ActiveAdmin ap = getActiveAdminForCallerLocked(who, CODE_OK, parent);
                if (ap.minimumPasswordNumeric != length) {
                    ap.minimumPasswordNumeric = length;
                    saveSettingsLocked(this.mInjector.userHandleGetCallingUserId());
                }
            }
        }
    }

    public int getPasswordMinimumNumeric(ComponentName who, int userHandle, boolean parent) {
        int i = CODE_OK;
        if (!this.mHasFeature) {
            return CODE_OK;
        }
        enforceFullCrossUsersPermission(userHandle);
        synchronized (this) {
            int length = CODE_OK;
            ActiveAdmin admin;
            if (who != null) {
                admin = getActiveAdminUncheckedLocked(who, userHandle, parent);
                if (admin != null) {
                    i = admin.minimumPasswordNumeric;
                }
                return i;
            }
            List<ActiveAdmin> admins = getActiveAdminsForLockscreenPoliciesLocked(userHandle, parent);
            int N = admins.size();
            for (int i2 = CODE_OK; i2 < N; i2 += STATUS_BAR_DISABLE2_MASK) {
                admin = (ActiveAdmin) admins.get(i2);
                if (isLimitPasswordAllowed(admin, 393216) && length < admin.minimumPasswordNumeric) {
                    length = admin.minimumPasswordNumeric;
                }
            }
            return length;
        }
    }

    public void setPasswordMinimumSymbols(ComponentName who, int length, boolean parent) {
        if (this.mHasFeature) {
            Preconditions.checkNotNull(who, "ComponentName is null");
            synchronized (this) {
                ActiveAdmin ap = getActiveAdminForCallerLocked(who, CODE_OK, parent);
                if (ap.minimumPasswordSymbols != length) {
                    ap.minimumPasswordSymbols = length;
                    saveSettingsLocked(this.mInjector.userHandleGetCallingUserId());
                }
            }
        }
    }

    public int getPasswordMinimumSymbols(ComponentName who, int userHandle, boolean parent) {
        int i = CODE_OK;
        if (!this.mHasFeature) {
            return CODE_OK;
        }
        enforceFullCrossUsersPermission(userHandle);
        synchronized (this) {
            int length = CODE_OK;
            ActiveAdmin admin;
            if (who != null) {
                admin = getActiveAdminUncheckedLocked(who, userHandle, parent);
                if (admin != null) {
                    i = admin.minimumPasswordSymbols;
                }
                return i;
            }
            List<ActiveAdmin> admins = getActiveAdminsForLockscreenPoliciesLocked(userHandle, parent);
            int N = admins.size();
            for (int i2 = CODE_OK; i2 < N; i2 += STATUS_BAR_DISABLE2_MASK) {
                admin = (ActiveAdmin) admins.get(i2);
                if (isLimitPasswordAllowed(admin, 393216) && length < admin.minimumPasswordSymbols) {
                    length = admin.minimumPasswordSymbols;
                }
            }
            return length;
        }
    }

    public void setPasswordMinimumNonLetter(ComponentName who, int length, boolean parent) {
        if (this.mHasFeature) {
            Preconditions.checkNotNull(who, "ComponentName is null");
            synchronized (this) {
                ActiveAdmin ap = getActiveAdminForCallerLocked(who, CODE_OK, parent);
                if (ap.minimumPasswordNonLetter != length) {
                    ap.minimumPasswordNonLetter = length;
                    saveSettingsLocked(this.mInjector.userHandleGetCallingUserId());
                }
            }
        }
    }

    public int getPasswordMinimumNonLetter(ComponentName who, int userHandle, boolean parent) {
        int i = CODE_OK;
        if (!this.mHasFeature) {
            return CODE_OK;
        }
        enforceFullCrossUsersPermission(userHandle);
        synchronized (this) {
            int length = CODE_OK;
            ActiveAdmin admin;
            if (who != null) {
                admin = getActiveAdminUncheckedLocked(who, userHandle, parent);
                if (admin != null) {
                    i = admin.minimumPasswordNonLetter;
                }
                return i;
            }
            List<ActiveAdmin> admins = getActiveAdminsForLockscreenPoliciesLocked(userHandle, parent);
            int N = admins.size();
            for (int i2 = CODE_OK; i2 < N; i2 += STATUS_BAR_DISABLE2_MASK) {
                admin = (ActiveAdmin) admins.get(i2);
                if (isLimitPasswordAllowed(admin, 393216) && length < admin.minimumPasswordNonLetter) {
                    length = admin.minimumPasswordNonLetter;
                }
            }
            return length;
        }
    }

    public boolean isActivePasswordSufficient(int userHandle, boolean parent) {
        if (!this.mHasFeature) {
            return true;
        }
        boolean isActivePasswordSufficientForUserLocked;
        enforceFullCrossUsersPermission(userHandle);
        synchronized (this) {
            getActiveAdminForCallerLocked(null, CODE_OK, parent);
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
        if (policy.mActivePasswordQuality < getPasswordQuality(null, userHandle, parent) || policy.mActivePasswordLength < getPasswordMinimumLength(null, userHandle, parent)) {
            return false;
        }
        if (policy.mActivePasswordQuality != 393216) {
            return true;
        }
        if (policy.mActivePasswordUpperCase < getPasswordMinimumUpperCase(null, userHandle, parent) || policy.mActivePasswordLowerCase < getPasswordMinimumLowerCase(null, userHandle, parent) || policy.mActivePasswordLetters < getPasswordMinimumLetters(null, userHandle, parent) || policy.mActivePasswordNumeric < getPasswordMinimumNumeric(null, userHandle, parent) || policy.mActivePasswordSymbols < getPasswordMinimumSymbols(null, userHandle, parent)) {
            z = false;
        } else if (policy.mActivePasswordNonLetter < getPasswordMinimumNonLetter(null, userHandle, parent)) {
            z = false;
        }
        return z;
    }

    public int getCurrentFailedPasswordAttempts(int userHandle, boolean parent) {
        int i;
        enforceFullCrossUsersPermission(userHandle);
        synchronized (this) {
            if (!isCallerWithSystemUid()) {
                getActiveAdminForCallerLocked(null, STATUS_BAR_DISABLE2_MASK, parent);
            }
            i = getUserDataUnchecked(getCredentialOwner(userHandle, parent)).mFailedPasswordAttempts;
        }
        return i;
    }

    public void setMaximumFailedPasswordsForWipe(ComponentName who, int num, boolean parent) {
        if (this.mHasFeature) {
            Preconditions.checkNotNull(who, "ComponentName is null");
            synchronized (this) {
                getActiveAdminForCallerLocked(who, CODE_USER_SETUP_COMPLETED, parent);
                ActiveAdmin ap = getActiveAdminForCallerLocked(who, STATUS_BAR_DISABLE2_MASK, parent);
                if (ap.maximumFailedPasswordsForWipe != num) {
                    ap.maximumFailedPasswordsForWipe = num;
                    saveSettingsLocked(this.mInjector.userHandleGetCallingUserId());
                }
            }
        }
    }

    public int getMaximumFailedPasswordsForWipe(ComponentName who, int userHandle, boolean parent) {
        int i = CODE_OK;
        if (!this.mHasFeature) {
            return CODE_OK;
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
        int count = CODE_OK;
        ActiveAdmin strictestAdmin = null;
        List<ActiveAdmin> admins = getActiveAdminsForLockscreenPoliciesLocked(userHandle, parent);
        int N = admins.size();
        for (int i = CODE_OK; i < N; i += STATUS_BAR_DISABLE2_MASK) {
            ActiveAdmin admin = (ActiveAdmin) admins.get(i);
            if (admin.maximumFailedPasswordsForWipe != 0) {
                int userId = admin.getUserHandle().getIdentifier();
                if (count != 0 && count <= admin.maximumFailedPasswordsForWipe) {
                    if (count == admin.maximumFailedPasswordsForWipe && getUserInfo(userId).isPrimary()) {
                    }
                }
                count = admin.maximumFailedPasswordsForWipe;
                strictestAdmin = admin;
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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean resetPassword(String passwordOrNull, int flags) throws RemoteException {
        if (!this.mHasFeature) {
            return false;
        }
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
                if (getTargetSdk(admin.info.getPackageName(), userHandle) <= 23) {
                    preN = true;
                } else {
                    preN = false;
                }
            } else {
                preN = getTargetSdk(getActiveAdminForCallerLocked(null, CODE_USER_HAS_PROFILE_OWNER).info.getPackageName(), userHandle) <= 23;
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
                int quality = getPasswordQuality(null, userHandle, false);
                if (quality == 524288) {
                    quality = CODE_OK;
                }
                if (quality != 0) {
                    int realQuality = LockPatternUtils.computePasswordQuality(password);
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
                }
                int i;
                if (quality == 393216) {
                    int letters = CODE_OK;
                    int uppercase = CODE_OK;
                    int lowercase = CODE_OK;
                    int numbers = CODE_OK;
                    int symbols = CODE_OK;
                    int nonletter = CODE_OK;
                    int i2 = CODE_OK;
                    while (true) {
                        if (i2 >= password.length()) {
                            break;
                        }
                        char c = password.charAt(i2);
                        if (c >= 'A' && c <= 'Z') {
                            letters += STATUS_BAR_DISABLE2_MASK;
                            uppercase += STATUS_BAR_DISABLE2_MASK;
                        } else if (c >= 'a' && c <= 'z') {
                            letters += STATUS_BAR_DISABLE2_MASK;
                            lowercase += STATUS_BAR_DISABLE2_MASK;
                        } else if (c < PROFILE_KEYGUARD_FEATURES_AFFECT_OWNER || c > '9') {
                            symbols += STATUS_BAR_DISABLE2_MASK;
                            nonletter += STATUS_BAR_DISABLE2_MASK;
                        } else {
                            numbers += STATUS_BAR_DISABLE2_MASK;
                            nonletter += STATUS_BAR_DISABLE2_MASK;
                        }
                        i2 += STATUS_BAR_DISABLE2_MASK;
                    }
                    int neededLetters = getPasswordMinimumLetters(null, userHandle, false);
                    if (letters < neededLetters) {
                        Slog.w(LOG_TAG, "resetPassword: number of letters " + letters + " does not meet required number of letters " + neededLetters);
                        return false;
                    }
                    int neededNumbers = getPasswordMinimumNumeric(null, userHandle, false);
                    if (numbers < neededNumbers) {
                        Slog.w(LOG_TAG, "resetPassword: number of numerical digits " + numbers + " does not meet required number of numerical digits " + neededNumbers);
                        return false;
                    }
                    int neededLowerCase = getPasswordMinimumLowerCase(null, userHandle, false);
                    if (lowercase < neededLowerCase) {
                        Slog.w(LOG_TAG, "resetPassword: number of lowercase letters " + lowercase + " does not meet required number of lowercase letters " + neededLowerCase);
                        return false;
                    }
                    int neededUpperCase = getPasswordMinimumUpperCase(null, userHandle, false);
                    if (uppercase < neededUpperCase) {
                        Slog.w(LOG_TAG, "resetPassword: number of uppercase letters " + uppercase + " does not meet required number of uppercase letters " + neededUpperCase);
                        return false;
                    }
                    int neededSymbols = getPasswordMinimumSymbols(null, userHandle, false);
                    if (symbols < neededSymbols) {
                        Slog.w(LOG_TAG, "resetPassword: number of special symbols " + symbols + " does not meet required number of special symbols " + neededSymbols);
                        return false;
                    }
                    int neededNonLetter = getPasswordMinimumNonLetter(null, userHandle, false);
                    if (nonletter < neededNonLetter) {
                        Slog.w(LOG_TAG, "resetPassword: number of non-letter characters " + nonletter + " does not meet required number of non-letter characters " + neededNonLetter);
                        return false;
                    }
                }
                DevicePolicyData policy = getUserData(userHandle);
                if (policy.mPasswordOwner >= 0) {
                    i = policy.mPasswordOwner;
                    if (r0 != callingUid) {
                        Slog.w(LOG_TAG, "resetPassword: already set by another uid and not entered by user");
                        return false;
                    }
                }
                boolean callerIsDeviceOwnerAdmin = isCallerDeviceOwner(callingUid);
                boolean doNotAskCredentialsOnBoot = (flags & CODE_USER_HAS_PROFILE_OWNER) != 0;
                if (callerIsDeviceOwnerAdmin && doNotAskCredentialsOnBoot) {
                    setDoNotAskCredentialsOnBoot();
                }
                long ident = this.mInjector.binderClearCallingIdentity();
                try {
                    if (TextUtils.isEmpty(password)) {
                        this.mLockPatternUtils.clearLock(userHandle);
                    } else {
                        this.mLockPatternUtils.saveLockPassword(password, null, quality, userHandle);
                    }
                    boolean requireEntry = (flags & STATUS_BAR_DISABLE2_MASK) != 0;
                    if (requireEntry) {
                        this.mLockPatternUtils.requireStrongAuth(CODE_USER_HAS_PROFILE_OWNER, -1);
                    }
                    synchronized (this) {
                        int newOwner = requireEntry ? callingUid : -1;
                        i = policy.mPasswordOwner;
                        if (r0 != newOwner) {
                            policy.mPasswordOwner = newOwner;
                            saveSettingsLocked(userHandle);
                        }
                    }
                    this.mInjector.binderRestoreCallingIdentity(ident);
                    return true;
                } catch (Throwable th) {
                    this.mInjector.binderRestoreCallingIdentity(ident);
                }
            } else if (preN) {
                Slog.e(LOG_TAG, "Cannot reset password when user is locked");
                return false;
            } else {
                throw new IllegalStateException("Cannot reset password when user is locked");
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
            DevicePolicyData policyData = getUserData(CODE_OK);
            if (!policyData.doNotAskCredentialsOnBoot) {
                policyData.doNotAskCredentialsOnBoot = true;
                saveSettingsLocked(CODE_OK);
            }
        }
    }

    public boolean getDoNotAskCredentialsOnBoot() {
        boolean z;
        this.mContext.enforceCallingOrSelfPermission("android.permission.QUERY_DO_NOT_ASK_CREDENTIALS_ON_BOOT", null);
        synchronized (this) {
            z = getUserData(CODE_OK).doNotAskCredentialsOnBoot;
        }
        return z;
    }

    public void setMaximumTimeToLock(ComponentName who, long timeMs, boolean parent) {
        if (this.mHasFeature) {
            Preconditions.checkNotNull(who, "ComponentName is null");
            int userHandle = this.mInjector.userHandleGetCallingUserId();
            synchronized (this) {
                ActiveAdmin ap = getActiveAdminForCallerLocked(who, CODE_USER_NOT_RUNNING, parent);
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
        int[] profileIds = this.mUserManager.getProfileIdsWithDisabled(userHandle);
        int length = profileIds.length;
        for (int i = CODE_OK; i < length; i += STATUS_BAR_DISABLE2_MASK) {
            policy = getUserDataUnchecked(profileIds[i]);
            int N = policy.mAdminList.size();
            for (int i2 = CODE_OK; i2 < N; i2 += STATUS_BAR_DISABLE2_MASK) {
                ActiveAdmin admin = (ActiveAdmin) policy.mAdminList.get(i2);
                if (admin.maximumTimeToUnlock > 0) {
                    if (timeMs > admin.maximumTimeToUnlock) {
                        timeMs = admin.maximumTimeToUnlock;
                    }
                }
                if (admin.hasParentActiveAdmin()) {
                    ActiveAdmin parentAdmin = admin.getParentActiveAdmin();
                    if (parentAdmin.maximumTimeToUnlock > 0) {
                        if (timeMs > parentAdmin.maximumTimeToUnlock) {
                            timeMs = parentAdmin.maximumTimeToUnlock;
                        }
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
                    this.mInjector.settingsGlobalPutInt("stay_on_while_plugged_in", CODE_OK);
                }
                this.mInjector.getPowerManagerInternal().setMaximumScreenOffTimeoutFromDeviceAdmin((int) Math.min(policy.mLastMaximumTimeToLock, 2147483647L));
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
        synchronized (this) {
            if (who != null) {
                ActiveAdmin admin = getActiveAdminUncheckedLocked(who, userHandle, parent);
                if (admin != null) {
                    j = admin.maximumTimeToUnlock;
                }
                return j;
            }
            j = getMaximumTimeToLockPolicyFromAdmins(getActiveAdminsForLockscreenPoliciesLocked(userHandle, parent));
            return j;
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
        for (int i = CODE_OK; i < N; i += STATUS_BAR_DISABLE2_MASK) {
            ActiveAdmin admin = (ActiveAdmin) admins.get(i);
            if (time == 0) {
                time = admin.maximumTimeToUnlock;
            } else if (admin.maximumTimeToUnlock != 0 && time > admin.maximumTimeToUnlock) {
                time = admin.maximumTimeToUnlock;
            }
        }
        return time;
    }

    public void lockNow(boolean parent) {
        if (this.mHasFeature) {
            synchronized (this) {
                getActiveAdminForCallerLocked(null, CODE_USER_NOT_RUNNING, parent);
                int userToLock = this.mInjector.userHandleGetCallingUserId();
                if (parent || !isSeparateProfileChallengeEnabled(userToLock)) {
                    userToLock = -1;
                }
                long ident = this.mInjector.binderClearCallingIdentity();
                try {
                    this.mLockPatternUtils.requireStrongAuth(CODE_USER_HAS_PROFILE_OWNER, userToLock);
                    if (userToLock == -1) {
                        this.mInjector.powerManagerGoToSleep(SystemClock.uptimeMillis(), STATUS_BAR_DISABLE2_MASK, CODE_OK);
                        this.mInjector.getIWindowManager().lockNow(null);
                    } else {
                        this.mInjector.getTrustManager().setDeviceLockedForUser(userToLock, true);
                    }
                    this.mInjector.binderRestoreCallingIdentity(ident);
                } catch (RemoteException e) {
                    this.mInjector.binderRestoreCallingIdentity(ident);
                } catch (Throwable th) {
                    this.mInjector.binderRestoreCallingIdentity(ident);
                }
            }
        }
    }

    public void enforceCanManageCaCerts(ComponentName who) {
        if (who != null) {
            synchronized (this) {
                getActiveAdminForCallerLocked(who, -1);
            }
        } else if (!isCallerDelegatedCertInstaller()) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_CA_CERTIFICATES", null);
        }
    }

    private void enforceCanManageInstalledKeys(ComponentName who) {
        if (who != null) {
            synchronized (this) {
                getActiveAdminForCallerLocked(who, -1);
            }
        } else if (!isCallerDelegatedCertInstaller()) {
            throw new SecurityException("who == null, but caller is not cert installer");
        }
    }

    private boolean isCallerDelegatedCertInstaller() {
        boolean z = false;
        int callingUid = this.mInjector.binderGetCallingUid();
        int userHandle = UserHandle.getUserId(callingUid);
        synchronized (this) {
            DevicePolicyData policy = getUserData(userHandle);
            if (policy.mDelegatedCertInstallerPackage == null) {
                return false;
            }
            try {
                if (this.mContext.getPackageManager().getPackageUidAsUser(policy.mDelegatedCertInstallerPackage, userHandle) == callingUid) {
                    z = true;
                }
                return z;
            } catch (NameNotFoundException e) {
                return false;
            }
        }
    }

    public boolean approveCaCert(String alias, int userId, boolean approval) {
        enforceManageUsers();
        synchronized (this) {
            Set<String> certs = getUserData(userId).mAcceptedCaCertificates;
            if (approval ? certs.add(alias) : certs.remove(alias)) {
                saveSettingsLocked(userId);
                MonitoringCertNotificationTask monitoringCertNotificationTask = new MonitoringCertNotificationTask();
                Integer[] numArr = new Integer[STATUS_BAR_DISABLE2_MASK];
                numArr[CODE_OK] = Integer.valueOf(userId);
                monitoringCertNotificationTask.execute(numArr);
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
                MonitoringCertNotificationTask monitoringCertNotificationTask = new MonitoringCertNotificationTask();
                Integer[] numArr = new Integer[STATUS_BAR_DISABLE2_MASK];
                numArr[CODE_OK] = Integer.valueOf(userInfo.id);
                monitoringCertNotificationTask.execute(numArr);
            }
        }
    }

    public boolean installCaCert(ComponentName admin, byte[] certBuffer) throws RemoteException {
        enforceCanManageCaCerts(admin);
        try {
            Certificate[] certificateArr = new Certificate[STATUS_BAR_DISABLE2_MASK];
            certificateArr[CODE_OK] = parseCert(certBuffer);
            byte[] pemCert = Credentials.convertToPem(certificateArr);
            UserHandle userHandle = new UserHandle(UserHandle.getCallingUserId());
            long id = this.mInjector.binderClearCallingIdentity();
            try {
                KeyChainConnection keyChainConnection = KeyChain.bindAsUser(this.mContext, userHandle);
                try {
                    keyChainConnection.getService().installCaCertificate(pemCert);
                    this.mInjector.binderRestoreCallingIdentity(id);
                    return true;
                } catch (RemoteException e) {
                    Log.e(LOG_TAG, "installCaCertsToKeyChain(): ", e);
                    this.mInjector.binderRestoreCallingIdentity(id);
                    return false;
                } finally {
                    keyChainConnection.close();
                }
            } catch (InterruptedException e1) {
                try {
                    Log.w(LOG_TAG, "installCaCertsToKeyChain(): ", e1);
                    Thread.currentThread().interrupt();
                } finally {
                    this.mInjector.binderRestoreCallingIdentity(id);
                }
            }
        } catch (CertificateException ce) {
            Log.e(LOG_TAG, "Problem converting cert", ce);
            return false;
        } catch (IOException ioe) {
            Log.e(LOG_TAG, "Problem reading cert", ioe);
            return false;
        }
    }

    private static X509Certificate parseCert(byte[] certBuffer) throws CertificateException {
        return (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(certBuffer));
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void uninstallCaCerts(ComponentName admin, String[] aliases) {
        enforceCanManageCaCerts(admin);
        UserHandle userHandle = new UserHandle(UserHandle.getCallingUserId());
        long id = this.mInjector.binderClearCallingIdentity();
        try {
            KeyChainConnection keyChainConnection = KeyChain.bindAsUser(this.mContext, userHandle);
            int i = CODE_OK;
            while (i < aliases.length) {
                try {
                    keyChainConnection.getService().deleteCaCertificate(aliases[i]);
                    i += STATUS_BAR_DISABLE2_MASK;
                } catch (RemoteException e) {
                    Log.e(LOG_TAG, "from CaCertUninstaller: ", e);
                    this.mInjector.binderRestoreCallingIdentity(id);
                } finally {
                    keyChainConnection.close();
                }
            }
            this.mInjector.binderRestoreCallingIdentity(id);
        } catch (InterruptedException ie) {
            Log.w(LOG_TAG, "CaCertUninstaller: ", ie);
            Thread.currentThread().interrupt();
        } catch (Throwable th) {
        }
    }

    public boolean installKeyPair(ComponentName who, byte[] privKey, byte[] cert, byte[] chain, String alias, boolean requestAccess) {
        enforceCanManageInstalledKeys(who);
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

    public boolean removeKeyPair(ComponentName who, String alias) {
        boolean removeKeyPair;
        enforceCanManageInstalledKeys(who);
        UserHandle userHandle = new UserHandle(UserHandle.getCallingUserId());
        long id = Binder.clearCallingIdentity();
        try {
            KeyChainConnection keyChainConnection = KeyChain.bindAsUser(this.mContext, userHandle);
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
            long id = this.mInjector.binderClearCallingIdentity();
            try {
                this.mContext.sendOrderedBroadcastAsUser(intent, caller, null, new AnonymousClass6(response), null, -1, null, null);
            } finally {
                this.mInjector.binderRestoreCallingIdentity(id);
            }
        }
    }

    private void sendPrivateKeyAliasResponse(String alias, IBinder responseBinder) {
        new AnonymousClass7(IKeyChainAliasCallback.Stub.asInterface(responseBinder), alias).execute(new Void[CODE_OK]);
    }

    public void setCertInstallerPackage(ComponentName who, String installerPackage) throws SecurityException {
        int userHandle = UserHandle.getCallingUserId();
        synchronized (this) {
            getActiveAdminForCallerLocked(who, -1);
            if (getTargetSdk(who.getPackageName(), userHandle) < 24 || installerPackage == null || isPackageInstalledForUser(installerPackage, userHandle)) {
                getUserData(userHandle).mDelegatedCertInstallerPackage = installerPackage;
                saveSettingsLocked(userHandle);
            } else {
                throw new IllegalArgumentException("Package " + installerPackage + " is not installed on the current user");
            }
        }
    }

    public String getCertInstallerPackage(ComponentName who) throws SecurityException {
        String str;
        int userHandle = UserHandle.getCallingUserId();
        synchronized (this) {
            getActiveAdminForCallerLocked(who, -1);
            str = getUserData(userHandle).mDelegatedCertInstallerPackage;
        }
        return str;
    }

    public boolean setAlwaysOnVpnPackage(ComponentName admin, String vpnPackage, boolean lockdown) throws SecurityException {
        synchronized (this) {
            getActiveAdminForCallerLocked(admin, -1);
        }
        int userId = this.mInjector.userHandleGetCallingUserId();
        long token = this.mInjector.binderClearCallingIdentity();
        if (vpnPackage != null) {
            try {
                if (!isPackageInstalledForUser(vpnPackage, userId)) {
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
        synchronized (this) {
            getActiveAdminForCallerLocked(admin, -1);
        }
        int userId = this.mInjector.userHandleGetCallingUserId();
        long token = this.mInjector.binderClearCallingIdentity();
        try {
            String alwaysOnVpnPackageForUser = ((ConnectivityManager) this.mContext.getSystemService("connectivity")).getAlwaysOnVpnPackageForUser(userId);
            return alwaysOnVpnPackageForUser;
        } finally {
            this.mInjector.binderRestoreCallingIdentity(token);
        }
    }

    private void wipeDataNoLock(boolean wipeExtRequested, String reason) {
        if (wipeExtRequested) {
            ((StorageManager) this.mContext.getSystemService("storage")).wipeAdoptableDisks();
            HwCustDevicePolicyManagerService mHwCustRecoverySystem = (HwCustDevicePolicyManagerService) HwCustUtils.createObj(HwCustDevicePolicyManagerService.class, new Object[CODE_OK]);
            if (mHwCustRecoverySystem != null && mHwCustRecoverySystem.wipeDataAndReset(this.mContext)) {
                Slog.d(LOG_TAG, "Successed wipe storage data.");
                return;
            }
        }
        try {
            RecoverySystem.rebootWipeUserData(this.mContext, reason);
        } catch (Exception e) {
            Slog.w(LOG_TAG, "Failed requesting data wipe", e);
        }
    }

    public void wipeData(int flags) {
        if (this.mHasFeature) {
            int userHandle = this.mInjector.userHandleGetCallingUserId();
            enforceFullCrossUsersPermission(userHandle);
            synchronized (this) {
                ActiveAdmin admin = getActiveAdminForCallerLocked(null, CODE_USER_SETUP_COMPLETED);
                String source = admin.info.getComponent().flattenToShortString();
                long ident = this.mInjector.binderClearCallingIdentity();
                if ((flags & CODE_USER_HAS_PROFILE_OWNER) != 0) {
                    try {
                        if (isDeviceOwner(admin.info.getComponent(), userHandle)) {
                            PersistentDataBlockManager manager = (PersistentDataBlockManager) this.mContext.getSystemService("persistent_data_block");
                            if (manager != null) {
                                manager.wipe();
                            }
                            HwCustDevicePolicyManagerService mHwCustRecoverySystem = (HwCustDevicePolicyManagerService) HwCustUtils.createObj(HwCustDevicePolicyManagerService.class, new Object[CODE_OK]);
                            if (mHwCustRecoverySystem != null && mHwCustRecoverySystem.wipeDataAndReset(this.mContext)) {
                                Slog.d(LOG_TAG, "DO successed wipe storage data and reset factory lowlevel.");
                                this.mInjector.binderRestoreCallingIdentity(ident);
                                return;
                            }
                        }
                        throw new SecurityException("Only device owner admins can set WIPE_RESET_PROTECTION_DATA");
                    } catch (Throwable th) {
                        this.mInjector.binderRestoreCallingIdentity(ident);
                    }
                }
                this.mInjector.binderRestoreCallingIdentity(ident);
                wipeDeviceNoLock((flags & STATUS_BAR_DISABLE2_MASK) != 0, userHandle, "DevicePolicyManager.wipeData() from " + source);
            }
        }
    }

    private void wipeDeviceNoLock(boolean wipeExtRequested, int userHandle, String reason) {
        long ident = this.mInjector.binderClearCallingIdentity();
        if (userHandle == 0) {
            try {
                wipeDataNoLock(wipeExtRequested, reason);
            } catch (Throwable th) {
                this.mInjector.binderRestoreCallingIdentity(ident);
            }
        } else {
            this.mHandler.post(new AnonymousClass8(userHandle));
        }
        this.mInjector.binderRestoreCallingIdentity(ident);
    }

    private void sendWipeProfileNotification() {
        String contentText = this.mContext.getString(17039630);
        this.mInjector.getNotificationManager().notify(PROFILE_WIPED_NOTIFICATION_ID, new Builder(this.mContext).setSmallIcon(17301642).setContentTitle(this.mContext.getString(17039627)).setContentText(contentText).setColor(this.mContext.getColor(17170519)).setStyle(new BigTextStyle().bigText(contentText)).build());
    }

    private void clearWipeProfileNotification() {
        this.mInjector.getNotificationManager().cancel(PROFILE_WIPED_NOTIFICATION_ID);
    }

    public void getRemoveWarning(ComponentName comp, RemoteCallback result, int userHandle) {
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
                this.mContext.sendOrderedBroadcastAsUser(intent, new UserHandle(userHandle), null, new AnonymousClass9(result), null, -1, null, null);
            }
        }
    }

    public void setActivePasswordState(int quality, int length, int letters, int uppercase, int lowercase, int numbers, int symbols, int nonletter, int userHandle) {
        if (this.mHasFeature) {
            enforceFullCrossUsersPermission(userHandle);
            this.mContext.enforceCallingOrSelfPermission("android.permission.BIND_DEVICE_ADMIN", null);
            if (isManagedProfile(userHandle) && !isSeparateProfileChallengeEnabled(userHandle)) {
                quality = CODE_OK;
                length = CODE_OK;
                letters = CODE_OK;
                uppercase = CODE_OK;
                lowercase = CODE_OK;
                numbers = CODE_OK;
                symbols = CODE_OK;
                nonletter = CODE_OK;
            }
            validateQualityConstant(quality);
            DevicePolicyData policy = getUserData(userHandle);
            synchronized (this) {
                policy.mActivePasswordQuality = quality;
                policy.mActivePasswordLength = length;
                policy.mActivePasswordLetters = letters;
                policy.mActivePasswordLowerCase = lowercase;
                policy.mActivePasswordUpperCase = uppercase;
                policy.mActivePasswordNumeric = numbers;
                policy.mActivePasswordSymbols = symbols;
                policy.mActivePasswordNonLetter = nonletter;
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
                    policy.mFailedPasswordAttempts = CODE_OK;
                    saveSettingsLocked(userId);
                    updatePasswordExpirationsLocked(userId);
                    setExpirationAlarmCheckLocked(this.mContext, userId, false);
                    sendAdminCommandForLockscreenPoliciesLocked("android.app.action.ACTION_PASSWORD_CHANGED", CODE_OK, userId);
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
        for (int i = CODE_OK; i < N; i += STATUS_BAR_DISABLE2_MASK) {
            ActiveAdmin admin = (ActiveAdmin) admins.get(i);
            if (admin.info.usesPolicy(CODE_ACCOUNTS_NOT_EMPTY)) {
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
        long ident = this.mInjector.binderClearCallingIdentity();
        boolean wipeData = false;
        int identifier = CODE_OK;
        try {
            synchronized (this) {
                DevicePolicyData policy = getUserData(userHandle);
                policy.mFailedPasswordAttempts += STATUS_BAR_DISABLE2_MASK;
                saveSettingsLocked(userHandle);
                if (this.mHasFeature) {
                    int max;
                    ActiveAdmin strictestAdmin = getAdminWithMinimumFailedPasswordsForWipeLocked(userHandle, false);
                    if (strictestAdmin != null) {
                        max = strictestAdmin.maximumFailedPasswordsForWipe;
                    } else {
                        max = CODE_OK;
                    }
                    if (max > 0 && policy.mFailedPasswordAttempts >= max) {
                        wipeData = true;
                        identifier = strictestAdmin.getUserHandle().getIdentifier();
                    }
                    sendAdminCommandForLockscreenPoliciesLocked("android.app.action.ACTION_PASSWORD_FAILED", STATUS_BAR_DISABLE2_MASK, userHandle);
                }
            }
            if (wipeData) {
                boolean isCustWipeData = false;
                if (userHandle == 0) {
                    HwCustDevicePolicyManagerService mHwCustRecoverySystem = (HwCustDevicePolicyManagerService) HwCustUtils.createObj(HwCustDevicePolicyManagerService.class, new Object[CODE_OK]);
                    if (mHwCustRecoverySystem != null && mHwCustRecoverySystem.wipeDataAndReset(this.mContext)) {
                        isCustWipeData = true;
                        Slog.d(LOG_TAG, "Successed wipe storage data.");
                    }
                }
                if (!isCustWipeData) {
                    wipeDeviceNoLock(false, identifier, "reportFailedPasswordAttempt()");
                }
            }
            this.mInjector.binderRestoreCallingIdentity(ident);
            if (this.mInjector.securityLogIsLoggingEnabled()) {
                Object[] objArr = new Object[CODE_USER_HAS_PROFILE_OWNER];
                objArr[CODE_OK] = Integer.valueOf(CODE_OK);
                objArr[STATUS_BAR_DISABLE2_MASK] = Integer.valueOf(STATUS_BAR_DISABLE2_MASK);
                SecurityLog.writeEvent(210007, objArr);
            }
        } catch (Throwable th) {
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
                    policy.mFailedPasswordAttempts = CODE_OK;
                    policy.mPasswordOwner = -1;
                    saveSettingsLocked(userHandle);
                    if (this.mHasFeature) {
                        sendAdminCommandForLockscreenPoliciesLocked("android.app.action.ACTION_PASSWORD_SUCCEEDED", STATUS_BAR_DISABLE2_MASK, userHandle);
                    }
                    this.mInjector.binderRestoreCallingIdentity(ident);
                } catch (Throwable th) {
                    this.mInjector.binderRestoreCallingIdentity(ident);
                }
            }
        }
        if (this.mInjector.securityLogIsLoggingEnabled()) {
            Object[] objArr = new Object[CODE_USER_HAS_PROFILE_OWNER];
            objArr[CODE_OK] = Integer.valueOf(STATUS_BAR_DISABLE2_MASK);
            objArr[STATUS_BAR_DISABLE2_MASK] = Integer.valueOf(STATUS_BAR_DISABLE2_MASK);
            SecurityLog.writeEvent(210007, objArr);
        }
    }

    public void reportFailedFingerprintAttempt(int userHandle) {
        enforceFullCrossUsersPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("android.permission.BIND_DEVICE_ADMIN", null);
        if (this.mInjector.securityLogIsLoggingEnabled()) {
            Object[] objArr = new Object[CODE_USER_HAS_PROFILE_OWNER];
            objArr[CODE_OK] = Integer.valueOf(CODE_OK);
            objArr[STATUS_BAR_DISABLE2_MASK] = Integer.valueOf(CODE_OK);
            SecurityLog.writeEvent(210007, objArr);
        }
    }

    public void reportSuccessfulFingerprintAttempt(int userHandle) {
        enforceFullCrossUsersPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("android.permission.BIND_DEVICE_ADMIN", null);
        if (this.mInjector.securityLogIsLoggingEnabled()) {
            Object[] objArr = new Object[CODE_USER_HAS_PROFILE_OWNER];
            objArr[CODE_OK] = Integer.valueOf(STATUS_BAR_DISABLE2_MASK);
            objArr[STATUS_BAR_DISABLE2_MASK] = Integer.valueOf(CODE_OK);
            SecurityLog.writeEvent(210007, objArr);
        }
    }

    public void reportKeyguardDismissed(int userHandle) {
        enforceFullCrossUsersPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("android.permission.BIND_DEVICE_ADMIN", null);
        if (this.mInjector.securityLogIsLoggingEnabled()) {
            SecurityLog.writeEvent(210006, new Object[CODE_OK]);
        }
    }

    public void reportKeyguardSecured(int userHandle) {
        enforceFullCrossUsersPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("android.permission.BIND_DEVICE_ADMIN", null);
        if (this.mInjector.securityLogIsLoggingEnabled()) {
            SecurityLog.writeEvent(210008, new Object[CODE_OK]);
        }
    }

    public ComponentName setGlobalProxy(ComponentName who, String proxySpec, String exclusionList) {
        if (!this.mHasFeature) {
            return null;
        }
        synchronized (this) {
            Preconditions.checkNotNull(who, "ComponentName is null");
            DevicePolicyData policy = getUserData(CODE_OK);
            ActiveAdmin admin = getActiveAdminForCallerLocked(who, CODE_NONSYSTEM_USER_EXISTS);
            for (ComponentName component : policy.mAdminMap.keySet()) {
                if (((ActiveAdmin) policy.mAdminMap.get(component)).specifiesGlobalProxy && !component.equals(who)) {
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
        synchronized (this) {
            DevicePolicyData policy = getUserData(CODE_OK);
            int N = policy.mAdminList.size();
            for (int i = CODE_OK; i < N; i += STATUS_BAR_DISABLE2_MASK) {
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
        for (int i = CODE_OK; i < N; i += STATUS_BAR_DISABLE2_MASK) {
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
        if (data.length > STATUS_BAR_DISABLE2_MASK) {
            try {
                proxyPort = Integer.parseInt(data[STATUS_BAR_DISABLE2_MASK]);
            } catch (NumberFormatException e) {
            }
        }
        exclusionList = exclusionList.trim();
        ProxyInfo proxyProperties = new ProxyInfo(data[CODE_OK], proxyPort, exclusionList);
        if (proxyProperties.isValid()) {
            this.mInjector.settingsGlobalPutString("global_http_proxy_host", data[CODE_OK]);
            this.mInjector.settingsGlobalPutInt("global_http_proxy_port", proxyPort);
            this.mInjector.settingsGlobalPutString("global_http_proxy_exclusion_list", exclusionList);
            return;
        }
        Slog.e(LOG_TAG, "Invalid proxy properties, ignoring: " + proxyProperties.toString());
    }

    public int setStorageEncryption(ComponentName who, boolean encrypt) {
        if (!this.mHasFeature) {
            return CODE_OK;
        }
        Preconditions.checkNotNull(who, "ComponentName is null");
        int userHandle = UserHandle.getCallingUserId();
        synchronized (this) {
            if (userHandle != 0) {
                Slog.w(LOG_TAG, "Only owner/system user is allowed to set storage encryption. User " + UserHandle.getCallingUserId() + " is not permitted.");
                return CODE_OK;
            }
            ActiveAdmin ap = getActiveAdminForCallerLocked(who, CODE_NOT_SYSTEM_USER);
            if (isEncryptionSupported()) {
                int i;
                if (ap.encryptionRequested != encrypt) {
                    ap.encryptionRequested = encrypt;
                    saveSettingsLocked(userHandle);
                }
                DevicePolicyData policy = getUserData(CODE_OK);
                boolean newRequested = false;
                int N = policy.mAdminList.size();
                for (int i2 = CODE_OK; i2 < N; i2 += STATUS_BAR_DISABLE2_MASK) {
                    newRequested |= ((ActiveAdmin) policy.mAdminList.get(i2)).encryptionRequested;
                }
                setEncryptionRequested(newRequested);
                if (newRequested) {
                    i = CODE_USER_NOT_RUNNING;
                } else {
                    i = STATUS_BAR_DISABLE2_MASK;
                }
                return i;
            }
            return CODE_OK;
        }
    }

    public boolean getStorageEncryption(ComponentName who, int userHandle) {
        if (!this.mHasFeature) {
            return false;
        }
        enforceFullCrossUsersPermission(userHandle);
        synchronized (this) {
            if (who != null) {
                ActiveAdmin ap = getActiveAdminUncheckedLocked(who, userHandle);
                boolean z = ap != null ? ap.encryptionRequested : false;
                return z;
            }
            DevicePolicyData policy = getUserData(userHandle);
            int N = policy.mAdminList.size();
            for (int i = CODE_OK; i < N; i += STATUS_BAR_DISABLE2_MASK) {
                if (((ActiveAdmin) policy.mAdminList.get(i)).encryptionRequested) {
                    return true;
                }
            }
            return false;
        }
    }

    public int getStorageEncryptionStatus(String callerPackage, int userHandle) {
        if (this.mHasFeature) {
            enforceFullCrossUsersPermission(userHandle);
            ensureCallerPackage(callerPackage);
        } else {
            enforceFullCrossUsersPermission(userHandle);
            ensureCallerPackage(callerPackage);
        }
        try {
            ApplicationInfo ai = this.mIPackageManager.getApplicationInfo(callerPackage, CODE_OK, userHandle);
            boolean legacyApp = false;
            if (ai.targetSdkVersion <= 23) {
                legacyApp = true;
            } else if ("com.google.android.apps.enterprise.dmagent".equals(ai.packageName) && ai.versionCode == 697) {
                legacyApp = true;
            }
            int rawStatus = getEncryptionStatus();
            if (rawStatus == CODE_NONSYSTEM_USER_EXISTS && legacyApp) {
                return CODE_USER_NOT_RUNNING;
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
            return CODE_NONSYSTEM_USER_EXISTS;
        }
        if (this.mInjector.storageManagerIsNonDefaultBlockEncrypted()) {
            return CODE_USER_NOT_RUNNING;
        }
        if (this.mInjector.storageManagerIsEncrypted()) {
            return CODE_USER_SETUP_COMPLETED;
        }
        if (this.mInjector.storageManagerIsEncryptable()) {
            return STATUS_BAR_DISABLE2_MASK;
        }
        return CODE_OK;
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
                return z;
            }
            DevicePolicyData policy = getUserData(userHandle);
            int N = policy.mAdminList.size();
            for (int i = CODE_OK; i < N; i += STATUS_BAR_DISABLE2_MASK) {
                if (((ActiveAdmin) policy.mAdminList.get(i)).disableScreenCapture) {
                    return true;
                }
            }
            return false;
        }
    }

    private void updateScreenCaptureDisabledInWindowManager(int userHandle, boolean disabled) {
        this.mHandler.post(new AnonymousClass10(userHandle, disabled));
    }

    public void setAutoTimeRequired(ComponentName who, boolean required) {
        if (this.mHasFeature) {
            Preconditions.checkNotNull(who, "ComponentName is null");
            int userHandle = UserHandle.getCallingUserId();
            synchronized (this) {
                ActiveAdmin admin = getActiveAdminForCallerLocked(who, -2);
                if (admin.requireAutoTime != required) {
                    admin.requireAutoTime = required;
                    saveSettingsLocked(userHandle);
                }
            }
            if (required) {
                long ident = this.mInjector.binderClearCallingIdentity();
                try {
                    this.mInjector.settingsGlobalPutInt("auto_time", STATUS_BAR_DISABLE2_MASK);
                } finally {
                    this.mInjector.binderRestoreCallingIdentity(ident);
                }
            }
        }
    }

    public boolean getAutoTimeRequired() {
        boolean z = false;
        if (!this.mHasFeature) {
            return false;
        }
        synchronized (this) {
            ActiveAdmin deviceOwner = getDeviceOwnerAdminLocked();
            if (deviceOwner != null) {
                z = deviceOwner.requireAutoTime;
            }
        }
        return z;
    }

    public void setForceEphemeralUsers(ComponentName who, boolean forceEphemeralUsers) {
        if (this.mHasFeature) {
            Preconditions.checkNotNull(who, "ComponentName is null");
            if (!forceEphemeralUsers || this.mInjector.userManagerIsSplitSystemUser()) {
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

    private boolean isDeviceOwnerManagedSingleUserDevice() {
        boolean z = true;
        synchronized (this) {
            if (this.mOwners.hasDeviceOwner()) {
                long callingIdentity = this.mInjector.binderClearCallingIdentity();
                try {
                    if (this.mInjector.userManagerIsSplitSystemUser()) {
                        if (this.mUserManager.getUserCount() != CODE_USER_HAS_PROFILE_OWNER) {
                            z = false;
                        } else if (this.mOwners.getDeviceOwnerUserId() == 0) {
                            z = false;
                        }
                        this.mInjector.binderRestoreCallingIdentity(callingIdentity);
                        return z;
                    }
                    if (this.mUserManager.getUserCount() != STATUS_BAR_DISABLE2_MASK) {
                        z = false;
                    }
                    this.mInjector.binderRestoreCallingIdentity(callingIdentity);
                    return z;
                } catch (Throwable th) {
                    this.mInjector.binderRestoreCallingIdentity(callingIdentity);
                }
            } else {
                return false;
            }
        }
    }

    private void ensureDeviceOwnerManagingSingleUser(ComponentName who) throws SecurityException {
        synchronized (this) {
            getActiveAdminForCallerLocked(who, -2);
        }
        if (!isDeviceOwnerManagedSingleUserDevice()) {
            throw new SecurityException("There should only be one user, managed by Device Owner");
        }
    }

    public boolean requestBugreport(ComponentName who) {
        if (!this.mHasFeature) {
            return false;
        }
        Preconditions.checkNotNull(who, "ComponentName is null");
        ensureDeviceOwnerManagingSingleUser(who);
        if (this.mRemoteBugreportServiceIsActive.get() || getDeviceOwnerRemoteBugreportUri() != null) {
            Slog.d(LOG_TAG, "Remote bugreport wasn't started because there's already one running.");
            return false;
        }
        long callingIdentity = this.mInjector.binderClearCallingIdentity();
        try {
            ActivityManagerNative.getDefault().requestBugReport(CODE_USER_HAS_PROFILE_OWNER);
            this.mRemoteBugreportServiceIsActive.set(true);
            this.mRemoteBugreportSharingAccepted.set(false);
            registerRemoteBugreportReceivers();
            this.mInjector.getNotificationManager().notifyAsUser(LOG_TAG, 678432343, RemoteBugreportUtils.buildNotification(this.mContext, STATUS_BAR_DISABLE2_MASK), UserHandle.ALL);
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
        filterConsent.addAction("com.android.server.action.BUGREPORT_SHARING_DECLINED");
        filterConsent.addAction("com.android.server.action.BUGREPORT_SHARING_ACCEPTED");
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
            this.mInjector.getNotificationManager().notifyAsUser(LOG_TAG, 678432343, RemoteBugreportUtils.buildNotification(this.mContext, CODE_USER_NOT_RUNNING), UserHandle.ALL);
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
        extras.putInt("android.app.extra.BUGREPORT_FAILURE_REASON", CODE_OK);
        sendDeviceOwnerCommand("android.app.action.BUGREPORT_FAILED", extras);
        this.mContext.unregisterReceiver(this.mRemoteBugreportConsentReceiver);
        this.mContext.unregisterReceiver(this.mRemoteBugreportFinishedReceiver);
    }

    private void onBugreportSharingAccepted() {
        this.mRemoteBugreportSharingAccepted.set(true);
        synchronized (this) {
            String bugreportUriString = getDeviceOwnerRemoteBugreportUri();
            String bugreportHash = this.mOwners.getDeviceOwnerRemoteBugreportHash();
        }
        if (bugreportUriString != null) {
            shareBugreportWithDeviceOwnerIfExists(bugreportUriString, bugreportHash);
        } else if (this.mRemoteBugreportServiceIsActive.get()) {
            this.mInjector.getNotificationManager().notifyAsUser(LOG_TAG, 678432343, RemoteBugreportUtils.buildNotification(this.mContext, CODE_USER_HAS_PROFILE_OWNER), UserHandle.ALL);
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
                extras.putInt("android.app.extra.BUGREPORT_FAILURE_REASON", STATUS_BAR_DISABLE2_MASK);
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
                this.mContext.grantUriPermission(this.mOwners.getDeviceOwnerComponent().getPackageName(), bugreportUri, STATUS_BAR_DISABLE2_MASK);
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
                ActiveAdmin ap = getActiveAdminForCallerLocked(who, PROFILE_KEYGUARD_FEATURES_PROFILE_ONLY);
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
                return z;
            }
            if (mergeDeviceOwnerRestriction) {
                ActiveAdmin deviceOwner = getDeviceOwnerAdminLocked();
                if (deviceOwner != null && deviceOwner.disableCamera) {
                    return true;
                }
            }
            DevicePolicyData policy = getUserData(userHandle);
            int N = policy.mAdminList.size();
            for (int i = CODE_OK; i < N; i += STATUS_BAR_DISABLE2_MASK) {
                if (((ActiveAdmin) policy.mAdminList.get(i)).disableCamera) {
                    return true;
                }
            }
            return false;
        }
    }

    public void setKeyguardDisabledFeatures(ComponentName who, int which, boolean parent) {
        if (this.mHasFeature) {
            Preconditions.checkNotNull(who, "ComponentName is null");
            int userHandle = this.mInjector.userHandleGetCallingUserId();
            if (isManagedProfile(userHandle)) {
                if (parent) {
                    which &= PROFILE_KEYGUARD_FEATURES_AFFECT_OWNER;
                } else {
                    which &= PROFILE_KEYGUARD_FEATURES;
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

    public int getKeyguardDisabledFeatures(ComponentName who, int userHandle, boolean parent) {
        int i = CODE_OK;
        if (!this.mHasFeature) {
            return CODE_OK;
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
                    return i;
                }
                List<ActiveAdmin> admins;
                int which;
                int N;
                int i2;
                int userId;
                boolean isRequestedUser;
                if (!parent) {
                    if (isManagedProfile(userHandle)) {
                        admins = getUserDataUnchecked(userHandle).mAdminList;
                        which = CODE_OK;
                        N = admins.size();
                        for (i2 = CODE_OK; i2 < N; i2 += STATUS_BAR_DISABLE2_MASK) {
                            admin = (ActiveAdmin) admins.get(i2);
                            userId = admin.getUserHandle().getIdentifier();
                            if (parent && userId == userHandle) {
                                isRequestedUser = true;
                            } else {
                                isRequestedUser = false;
                            }
                            if (isRequestedUser && isManagedProfile(userId)) {
                                which |= admin.disabledKeyguardFeatures & PROFILE_KEYGUARD_FEATURES_AFFECT_OWNER;
                            } else {
                                which |= admin.disabledKeyguardFeatures;
                            }
                        }
                        this.mInjector.binderRestoreCallingIdentity(ident);
                        return which;
                    }
                }
                admins = getActiveAdminsForLockscreenPoliciesLocked(userHandle, parent);
                which = CODE_OK;
                N = admins.size();
                for (i2 = CODE_OK; i2 < N; i2 += STATUS_BAR_DISABLE2_MASK) {
                    admin = (ActiveAdmin) admins.get(i2);
                    userId = admin.getUserHandle().getIdentifier();
                    if (parent) {
                    }
                    isRequestedUser = false;
                    if (isRequestedUser) {
                    }
                    which |= admin.disabledKeyguardFeatures;
                }
                this.mInjector.binderRestoreCallingIdentity(ident);
                return which;
            }
        } finally {
            this.mInjector.binderRestoreCallingIdentity(ident);
        }
    }

    public void setKeepUninstalledPackages(ComponentName who, List<String> packageList) {
        if (this.mHasFeature) {
            Preconditions.checkNotNull(who, "ComponentName is null");
            Preconditions.checkNotNull(packageList, "packageList is null");
            int userHandle = UserHandle.getCallingUserId();
            synchronized (this) {
                getActiveAdminForCallerLocked(who, -2).keepUninstalledPackages = packageList;
                saveSettingsLocked(userHandle);
                this.mInjector.getPackageManagerInternal().setKeepUninstalledPackages(packageList);
            }
        }
    }

    public List<String> getKeepUninstalledPackages(ComponentName who) {
        Preconditions.checkNotNull(who, "ComponentName is null");
        if (!this.mHasFeature) {
            return null;
        }
        List<String> keepUninstalledPackagesLocked;
        synchronized (this) {
            getActiveAdminForCallerLocked(who, -2);
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
        if (admin == null || !isPackageInstalledForUser(admin.getPackageName(), userId)) {
            throw new IllegalArgumentException("Invalid component " + admin + " for device owner");
        }
        synchronized (this) {
            enforceCanSetDeviceOwnerLocked(userId);
            if (getActiveAdminUncheckedLocked(admin, userId) == null) {
                throw new IllegalArgumentException("Not active admin: " + admin);
            }
            long ident = this.mInjector.binderClearCallingIdentity();
            try {
                if (this.mInjector.getIBackupManager() != null) {
                    this.mInjector.getIBackupManager().setBackupServiceActive(CODE_OK, false);
                }
                this.mInjector.binderRestoreCallingIdentity(ident);
                this.mOwners.setDeviceOwner(admin, ownerName, userId);
                this.mOwners.writeDeviceOwner();
                updateDeviceOwnerLocked();
                setDeviceOwnerSystemPropertyLocked();
                Intent intent = new Intent("android.app.action.DEVICE_OWNER_CHANGED");
                ident = this.mInjector.binderClearCallingIdentity();
                try {
                    this.mContext.sendBroadcastAsUser(intent, new UserHandle(userId));
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
                String applicationLabel = getApplicationLabel(this.mOwners.getDeviceOwnerPackageName(), CODE_OK);
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
        for (int i = CODE_OK; i < n; i += STATUS_BAR_DISABLE2_MASK) {
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
            if (this.mContext.getPackageManager().getPackageUidAsUser(packageName, UserHandle.getUserId(callingUid)) != callingUid) {
                throw new SecurityException("Invalid packageName");
            }
            synchronized (this) {
                ComponentName deviceOwnerComponent = this.mOwners.getDeviceOwnerComponent();
                int deviceOwnerUserId = this.mOwners.getDeviceOwnerUserId();
                if (this.mOwners.hasDeviceOwner() && deviceOwnerComponent.getPackageName().equals(packageName) && deviceOwnerUserId == UserHandle.getUserId(callingUid)) {
                    enforceUserUnlocked(deviceOwnerUserId);
                    ActiveAdmin admin = getDeviceOwnerAdminLocked();
                    long ident = this.mInjector.binderClearCallingIdentity();
                    try {
                        clearDeviceOwnerLocked(admin, deviceOwnerUserId);
                        removeActiveAdminLocked(deviceOwnerComponent, deviceOwnerUserId);
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
        if (admin != null) {
            admin.disableCamera = false;
            admin.userRestrictions = null;
            admin.forceEphemeralUsers = false;
            this.mUserManagerInternal.setForceEphemeralUsers(admin.forceEphemeralUsers);
        }
        clearUserPoliciesLocked(userId);
        this.mOwners.clearDeviceOwner();
        this.mOwners.writeDeviceOwner();
        updateDeviceOwnerLocked();
        disableSecurityLoggingIfNotCompliant();
        try {
            this.mInjector.getIBackupManager().setBackupServiceActive(CODE_OK, true);
        } catch (RemoteException e) {
            throw new IllegalStateException("Failed reactivating backup service.", e);
        }
    }

    public boolean setProfileOwner(ComponentName who, String ownerName, int userHandle) {
        if (!this.mHasFeature) {
            return false;
        }
        if (who == null || !isPackageInstalledForUser(who.getPackageName(), userHandle)) {
            throw new IllegalArgumentException("Component " + who + " not installed for userId:" + userHandle);
        }
        synchronized (this) {
            enforceCanSetProfileOwnerLocked(userHandle);
            if (getActiveAdminUncheckedLocked(who, userHandle) == null) {
                throw new IllegalArgumentException("Not active admin: " + who);
            }
            this.mOwners.setProfileOwner(who, ownerName, userHandle);
            this.mOwners.writeProfileOwner(userHandle);
        }
        return true;
    }

    public void clearProfileOwner(ComponentName who) {
        if (this.mHasFeature) {
            int userId = this.mInjector.binderGetCallingUserHandle().getIdentifier();
            enforceNotManagedProfile(userId, "clear profile owner");
            enforceUserUnlocked(userId);
            ActiveAdmin admin = getActiveAdminForCallerLocked(who, -1);
            synchronized (this) {
                long ident = this.mInjector.binderClearCallingIdentity();
                try {
                    clearProfileOwnerLocked(admin, userId);
                    removeActiveAdminLocked(who, userId);
                    this.mInjector.binderRestoreCallingIdentity(ident);
                } catch (Throwable th) {
                    this.mInjector.binderRestoreCallingIdentity(ident);
                }
            }
        }
    }

    public void clearProfileOwnerLocked(ActiveAdmin admin, int userId) {
        if (admin != null) {
            admin.disableCamera = false;
            admin.userRestrictions = null;
        }
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
        policy.mPermissionPolicy = CODE_OK;
        policy.mDelegatedCertInstallerPackage = null;
        policy.mApplicationRestrictionsManagingPackage = null;
        policy.mStatusBarDisabled = false;
        policy.mUserProvisioningState = CODE_OK;
        saveSettingsLocked(userId);
        try {
            this.mIPackageManager.updatePermissionFlagsForAllApps(CODE_USER_SETUP_COMPLETED, CODE_OK, userId);
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

    public int getUserProvisioningState() {
        if (this.mHasFeature) {
            return getUserProvisioningState(this.mInjector.userHandleGetCallingUserId());
        }
        return CODE_OK;
    }

    private int getUserProvisioningState(int userHandle) {
        return getUserData(userHandle).mUserProvisioningState;
    }

    public void setUserProvisioningState(int newState, int userHandle) {
        if (!this.mHasFeature) {
            return;
        }
        if (userHandle == this.mOwners.getDeviceOwnerUserId() || this.mOwners.hasProfileOwner(userHandle) || getManagedUserId(userHandle) != -1) {
            synchronized (this) {
                boolean transitionCheckNeeded = true;
                int callingUid = this.mInjector.binderGetCallingUid();
                if (callingUid != IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME && callingUid != 0) {
                    enforceCanManageProfileAndDeviceOwners();
                } else if (getUserProvisioningState(userHandle) == 0 && newState == CODE_USER_NOT_RUNNING) {
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
            case CODE_OK /*0*/:
                if (newState != 0) {
                    return;
                }
                break;
            case STATUS_BAR_DISABLE2_MASK /*1*/:
            case CODE_USER_HAS_PROFILE_OWNER /*2*/:
                if (newState == CODE_USER_NOT_RUNNING) {
                    return;
                }
                break;
            case CODE_USER_SETUP_COMPLETED /*4*/:
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
        for (int i = CODE_OK; i < n; i += STATUS_BAR_DISABLE2_MASK) {
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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private String getApplicationLabel(String packageName, int userHandle) {
        String str = null;
        long token = this.mInjector.binderClearCallingIdentity();
        try {
            Context userContext = this.mContext.createPackageContextAsUser(packageName, CODE_OK, new UserHandle(userHandle));
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
            return null;
        } catch (Throwable th) {
            this.mInjector.binderRestoreCallingIdentity(token);
        }
    }

    private void enforceCanSetProfileOwnerLocked(int userHandle) {
        UserInfo info = getUserInfo(userHandle);
        if (info == null) {
            throw new IllegalArgumentException("Attempted to set profile owner for invalid userId: " + userHandle);
        } else if (info.isGuest()) {
            throw new IllegalStateException("Cannot set a profile owner on a guest");
        } else if (this.mOwners.hasProfileOwner(userHandle)) {
            throw new IllegalStateException("Trying to set the profile owner, but profile owner is already set.");
        } else if (this.mOwners.hasDeviceOwner() && this.mOwners.getDeviceOwnerUserId() == userHandle) {
            throw new IllegalStateException("Trying to set the profile owner, but the user already has a device owner.");
        } else {
            int callingUid = this.mInjector.binderGetCallingUid();
            if (callingUid != IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME && callingUid != 0) {
                enforceCanManageProfileAndDeviceOwners();
                if (hasUserSetupCompleted(userHandle) && !isCallerWithSystemUid()) {
                    throw new IllegalStateException("Cannot set the profile owner on a user which is already set-up");
                }
            } else if (hasUserSetupCompleted(userHandle) && AccountManager.get(this.mContext).getAccountsAsUser(userHandle).length > 0) {
                throw new IllegalStateException("Not allowed to set the profile owner because there are already some accounts on the profile");
            }
        }
    }

    private void enforceCanSetDeviceOwnerLocked(int userId) {
        boolean isAdb = true;
        int callingUid = this.mInjector.binderGetCallingUid();
        if (!(callingUid == IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME || callingUid == 0)) {
            isAdb = false;
        }
        if (!isAdb) {
            enforceCanManageProfileAndDeviceOwners();
        }
        int code = checkSetDeviceOwnerPreCondition(userId, isAdb);
        switch (code) {
            case CODE_OK /*0*/:
            case STATUS_BAR_DISABLE2_MASK /*1*/:
                throw new IllegalStateException("Trying to set the device owner, but device owner is already set.");
            case CODE_USER_HAS_PROFILE_OWNER /*2*/:
                throw new IllegalStateException("Trying to set the device owner, but the user already has a profile owner.");
            case CODE_USER_NOT_RUNNING /*3*/:
                throw new IllegalStateException("User not running: " + userId);
            case CODE_USER_SETUP_COMPLETED /*4*/:
                throw new IllegalStateException("Cannot set the device owner if the device is already set-up");
            case CODE_NONSYSTEM_USER_EXISTS /*5*/:
                throw new IllegalStateException("Not allowed to set the device owner because there are already several users on the device");
            case CODE_ACCOUNTS_NOT_EMPTY /*6*/:
                throw new IllegalStateException("Not allowed to set the device owner because there are already some accounts on the device");
            case CODE_NOT_SYSTEM_USER /*7*/:
                throw new IllegalStateException("User is not system user");
            default:
                throw new IllegalStateException("Unknown @DeviceOwnerPreConditionCode " + code);
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
        Object obj = STATUS_BAR_DISABLE2_MASK;
        int callingUid = this.mInjector.binderGetCallingUid();
        if (!(isCallerWithSystemUid() || callingUid == 0)) {
            obj = CODE_OK;
        }
        if (obj == null) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_USERS", null);
        }
    }

    protected void enforceFullCrossUsersPermission(int userHandle) {
        enforceSystemUserOrPermission(userHandle, "android.permission.INTERACT_ACROSS_USERS_FULL");
    }

    private void enforceCrossUsersPermission(int userHandle) {
        enforceSystemUserOrPermission(userHandle, "android.permission.INTERACT_ACROSS_USERS");
    }

    private void enforceSystemUserOrPermission(int userHandle, String permission) {
        Object obj = STATUS_BAR_DISABLE2_MASK;
        if (userHandle < 0) {
            throw new IllegalArgumentException("Invalid userId " + userHandle);
        }
        int callingUid = this.mInjector.binderGetCallingUid();
        if (userHandle != UserHandle.getUserId(callingUid)) {
            if (!(isCallerWithSystemUid() || callingUid == 0)) {
                obj = CODE_OK;
            }
            if (obj == null) {
                this.mContext.enforceCallingOrSelfPermission(permission, "Must be system or have " + permission + " permission");
            }
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

    private void ensureCallerPackage(String packageName) {
        boolean z = false;
        if (packageName == null) {
            Preconditions.checkState(isCallerWithSystemUid(), "Only caller can omit package name");
            return;
        }
        try {
            if (this.mIPackageManager.getApplicationInfo(packageName, CODE_OK, this.mInjector.userHandleGetCallingUserId()).uid == this.mInjector.binderGetCallingUid()) {
                z = true;
            }
            Preconditions.checkState(z, "Unmatching package name");
        } catch (RemoteException e) {
        }
    }

    private boolean isCallerWithSystemUid() {
        return UserHandle.isSameApp(this.mInjector.binderGetCallingUid(), ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE);
    }

    private int getProfileParentId(int userHandle) {
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
        if (getUserInfo(userHandle) != null) {
            return getUserInfo(userHandle).isManagedProfile();
        }
        Log.w(LOG_TAG, "UserInfo null");
        return false;
    }

    private void enableIfNecessary(String packageName, int userId) {
        try {
            if (this.mIPackageManager.getApplicationInfo(packageName, DumpState.DUMP_VERSION, userId).enabledSetting == CODE_USER_SETUP_COMPLETED) {
                this.mIPackageManager.setApplicationEnabledSetting(packageName, CODE_OK, STATUS_BAR_DISABLE2_MASK, userId, "DevicePolicyManager");
            }
        } catch (RemoteException e) {
        }
    }

    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
            pw.println("Permission Denial: can't dump DevicePolicyManagerService from from pid=" + this.mInjector.binderGetCallingPid() + ", uid=" + this.mInjector.binderGetCallingUid());
            return;
        }
        synchronized (this) {
            pw.println("Current Device Policy Manager state:");
            this.mOwners.dump("  ", pw);
            int userCount = this.mUserData.size();
            for (int u = CODE_OK; u < userCount; u += STATUS_BAR_DISABLE2_MASK) {
                DevicePolicyData policy = getUserData(this.mUserData.keyAt(u));
                pw.println();
                pw.println("  Enabled Device Admins (User " + policy.mUserHandle + ", provisioningState: " + policy.mUserProvisioningState + "):");
                pw.println(" ");
                pw.print("    mPasswordOwner=");
                pw.println(policy.mPasswordOwner);
            }
            pw.println();
            pw.println("Encryption Status: " + getEncryptionStatusName(getEncryptionStatus()));
        }
    }

    private String getEncryptionStatusName(int encryptionStatus) {
        switch (encryptionStatus) {
            case CODE_OK /*0*/:
                return "unsupported";
            case STATUS_BAR_DISABLE2_MASK /*1*/:
                return "inactive";
            case CODE_USER_HAS_PROFILE_OWNER /*2*/:
                return "activating";
            case CODE_USER_NOT_RUNNING /*3*/:
                return "block";
            case CODE_USER_SETUP_COMPLETED /*4*/:
                return "block default key";
            case CODE_NONSYSTEM_USER_EXISTS /*5*/:
                return "per-user";
            default:
                return "unknown";
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
                this.mInjector.binderRestoreCallingIdentity(id);
            } catch (RemoteException e) {
                this.mInjector.binderRestoreCallingIdentity(id);
            } catch (Throwable th) {
                this.mInjector.binderRestoreCallingIdentity(id);
            }
        }
    }

    public void clearPackagePersistentPreferredActivities(ComponentName who, String packageName) {
        Preconditions.checkNotNull(who, "ComponentName is null");
        int userHandle = UserHandle.getCallingUserId();
        synchronized (this) {
            getActiveAdminForCallerLocked(who, -1);
            long id = this.mInjector.binderClearCallingIdentity();
            try {
                this.mIPackageManager.clearPackagePersistentPreferredActivities(packageName, userHandle);
                this.mInjector.binderRestoreCallingIdentity(id);
            } catch (RemoteException e) {
                this.mInjector.binderRestoreCallingIdentity(id);
            } catch (Throwable th) {
                this.mInjector.binderRestoreCallingIdentity(id);
            }
        }
    }

    public boolean setApplicationRestrictionsManagingPackage(ComponentName admin, String packageName) {
        Preconditions.checkNotNull(admin, "ComponentName is null");
        int userHandle = this.mInjector.userHandleGetCallingUserId();
        synchronized (this) {
            getActiveAdminForCallerLocked(admin, -1);
            if (packageName == null || isPackageInstalledForUser(packageName, userHandle)) {
                getUserData(userHandle).mApplicationRestrictionsManagingPackage = packageName;
                saveSettingsLocked(userHandle);
                return true;
            }
            return false;
        }
    }

    public String getApplicationRestrictionsManagingPackage(ComponentName admin) {
        String str;
        Preconditions.checkNotNull(admin, "ComponentName is null");
        int userHandle = this.mInjector.userHandleGetCallingUserId();
        synchronized (this) {
            getActiveAdminForCallerLocked(admin, -1);
            str = getUserData(userHandle).mApplicationRestrictionsManagingPackage;
        }
        return str;
    }

    public boolean isCallerApplicationRestrictionsManagingPackage() {
        boolean z = false;
        int callingUid = this.mInjector.binderGetCallingUid();
        int userHandle = UserHandle.getUserId(callingUid);
        synchronized (this) {
            DevicePolicyData policy = getUserData(userHandle);
            if (policy.mApplicationRestrictionsManagingPackage == null) {
                return false;
            }
            try {
                if (this.mContext.getPackageManager().getPackageUidAsUser(policy.mApplicationRestrictionsManagingPackage, userHandle) == callingUid) {
                    z = true;
                }
                return z;
            } catch (NameNotFoundException e) {
                return false;
            }
        }
    }

    private void enforceCanManageApplicationRestrictions(ComponentName who) {
        if (who != null) {
            synchronized (this) {
                getActiveAdminForCallerLocked(who, -1);
            }
        } else if (!isCallerApplicationRestrictionsManagingPackage()) {
            throw new SecurityException("No admin component given, and caller cannot manage application restrictions for other apps.");
        }
    }

    public void setApplicationRestrictions(ComponentName who, String packageName, Bundle settings) {
        enforceCanManageApplicationRestrictions(who);
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

    public List<PersistableBundle> getTrustAgentConfiguration(ComponentName admin, ComponentName agent, int userHandle, boolean parent) {
        if (!this.mHasFeature) {
            return null;
        }
        Preconditions.checkNotNull(agent, "agent null");
        enforceFullCrossUsersPermission(userHandle);
        synchronized (this) {
            String componentName = agent.flattenToString();
            if (admin != null) {
                ActiveAdmin ap = getActiveAdminUncheckedLocked(admin, userHandle, parent);
                if (ap == null) {
                    return null;
                }
                TrustAgentInfo trustAgentInfo = (TrustAgentInfo) ap.trustAgentInfos.get(componentName);
                if (trustAgentInfo == null || trustAgentInfo.options == null) {
                    return null;
                }
                List<PersistableBundle> result = new ArrayList();
                result.add(trustAgentInfo.options);
                return result;
            }
            result = null;
            List<ActiveAdmin> admins = getActiveAdminsForLockscreenPoliciesLocked(userHandle, parent);
            boolean allAdminsHaveOptions = true;
            int N = admins.size();
            for (int i = CODE_OK; i < N; i += STATUS_BAR_DISABLE2_MASK) {
                ActiveAdmin active = (ActiveAdmin) admins.get(i);
                boolean disablesTrust = (active.disabledKeyguardFeatures & 16) != 0;
                TrustAgentInfo info = (TrustAgentInfo) active.trustAgentInfos.get(componentName);
                if (info == null || info.options == null || info.options.isEmpty()) {
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
            return result;
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
                if ((flags & STATUS_BAR_DISABLE2_MASK) != 0) {
                    this.mIPackageManager.addCrossProfileIntentFilter(filter, who.getPackageName(), callingUserId, parent.id, CODE_OK);
                }
                if ((flags & CODE_USER_HAS_PROFILE_OWNER) != 0) {
                    this.mIPackageManager.addCrossProfileIntentFilter(filter, who.getPackageName(), parent.id, callingUserId, CODE_OK);
                }
                this.mInjector.binderRestoreCallingIdentity(id);
                return;
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
                return;
            } catch (RemoteException e) {
                this.mInjector.binderRestoreCallingIdentity(id);
            } catch (Throwable th) {
                this.mInjector.binderRestoreCallingIdentity(id);
            }
        }
    }

    private boolean checkPackagesInPermittedListOrSystem(List<String> enabledPackages, List<String> permittedList, int userIdToCheck) {
        long id = this.mInjector.binderClearCallingIdentity();
        UserInfo user = getUserInfo(userIdToCheck);
        if (user.isManagedProfile()) {
            userIdToCheck = user.profileGroupId;
        }
        for (String enabledPackage : enabledPackages) {
            boolean systemService = false;
            try {
                systemService = (this.mIPackageManager.getApplicationInfo(enabledPackage, DumpState.DUMP_PREFERRED_XML, userIdToCheck).flags & STATUS_BAR_DISABLE2_MASK) != 0;
            } catch (RemoteException e) {
                Log.i(LOG_TAG, "Can't talk to package managed", e);
            } catch (Throwable th) {
                this.mInjector.binderRestoreCallingIdentity(id);
            }
            if (!systemService) {
                if (!permittedList.contains(enabledPackage)) {
                    this.mInjector.binderRestoreCallingIdentity(id);
                    return false;
                }
            }
        }
        this.mInjector.binderRestoreCallingIdentity(id);
        return true;
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
            List<AccessibilityServiceInfo> list = null;
            long id = this.mInjector.binderClearCallingIdentity();
            try {
                UserInfo user = getUserInfo(userId);
                if (user.isManagedProfile()) {
                    userId = user.profileGroupId;
                }
                list = getAccessibilityManagerForUser(userId).getEnabledAccessibilityServiceList(-1);
                if (list != null) {
                    List<String> enabledPackages = new ArrayList();
                    for (AccessibilityServiceInfo service : list) {
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

    public List getPermittedAccessibilityServicesForUser(int userId) {
        Throwable th;
        long id;
        if (!this.mHasFeature) {
            return null;
        }
        synchronized (this) {
            List<String> result = null;
            try {
                int[] profileIds = this.mUserManager.getProfileIdsWithDisabled(userId);
                int i = CODE_OK;
                int length = profileIds.length;
                while (i < length) {
                    DevicePolicyData policy = getUserDataUnchecked(profileIds[i]);
                    int N = policy.mAdminList.size();
                    int j = CODE_OK;
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
                            j += STATUS_BAR_DISABLE2_MASK;
                            result2 = result;
                        } catch (Throwable th2) {
                            th = th2;
                            result = result2;
                        }
                    }
                    i += STATUS_BAR_DISABLE2_MASK;
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
                            if ((serviceInfo.applicationInfo.flags & STATUS_BAR_DISABLE2_MASK) != 0) {
                                result.add(serviceInfo.packageName);
                            }
                        }
                    }
                    this.mInjector.binderRestoreCallingIdentity(id);
                }
                return result;
            } catch (Throwable th3) {
                th = th3;
                throw th;
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
                    String[] strArr = new String[STATUS_BAR_DISABLE2_MASK];
                    strArr[CODE_OK] = packageName;
                    boolean checkPackagesInPermittedListOrSystem = checkPackagesInPermittedListOrSystem(Arrays.asList(strArr), admin.permittedAccessiblityServices, userHandle);
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
        if (packageList != null) {
            List<InputMethodInfo> enabledImes = ((InputMethodManager) this.mContext.getSystemService(InputMethodManager.class)).getEnabledInputMethodList();
            if (enabledImes != null) {
                List<String> enabledPackages = new ArrayList();
                for (InputMethodInfo ime : enabledImes) {
                    enabledPackages.add(ime.getPackageName());
                }
                if (!checkPackagesInPermittedListOrSystem(enabledPackages, packageList, this.mInjector.binderGetCallingUserHandle().getIdentifier())) {
                    Slog.e(LOG_TAG, "Cannot set permitted input methods, because it contains already enabled input method.");
                    return false;
                }
            }
        }
        synchronized (this) {
            getActiveAdminForCallerLocked(who, -1).permittedInputMethods = packageList;
            saveSettingsLocked(UserHandle.getCallingUserId());
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

    public List getPermittedInputMethodsForCurrentUser() {
        Throwable th;
        long id;
        try {
            int userId = this.mInjector.getIActivityManager().getCurrentUser().id;
            synchronized (this) {
                List<String> result = null;
                try {
                    int[] profileIds = this.mUserManager.getProfileIdsWithDisabled(userId);
                    int i = CODE_OK;
                    int length = profileIds.length;
                    while (i < length) {
                        DevicePolicyData policy = getUserDataUnchecked(profileIds[i]);
                        int N = policy.mAdminList.size();
                        int j = CODE_OK;
                        List<String> result2 = result;
                        while (j < N) {
                            List<String> fromAdmin = ((ActiveAdmin) policy.mAdminList.get(j)).permittedInputMethods;
                            if (fromAdmin == null) {
                                result = result2;
                            } else if (result2 == null) {
                                List<String> arrayList = new ArrayList(fromAdmin);
                            } else {
                                try {
                                    result2.retainAll(fromAdmin);
                                    result = result2;
                                } catch (Throwable th2) {
                                    th = th2;
                                    result = result2;
                                }
                            }
                            j += STATUS_BAR_DISABLE2_MASK;
                            result2 = result;
                        }
                        i += STATUS_BAR_DISABLE2_MASK;
                        result = result2;
                    }
                    if (result != null) {
                        List<InputMethodInfo> imes = ((InputMethodManager) this.mContext.getSystemService(InputMethodManager.class)).getInputMethodList();
                        id = this.mInjector.binderClearCallingIdentity();
                        if (imes != null) {
                            for (InputMethodInfo ime : imes) {
                                ServiceInfo serviceInfo = ime.getServiceInfo();
                                if ((serviceInfo.applicationInfo.flags & STATUS_BAR_DISABLE2_MASK) != 0) {
                                    result.add(serviceInfo.packageName);
                                }
                            }
                        }
                        this.mInjector.binderRestoreCallingIdentity(id);
                    }
                    return result;
                } catch (Throwable th3) {
                    th = th3;
                    throw th;
                }
            }
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
                    String[] strArr = new String[STATUS_BAR_DISABLE2_MASK];
                    strArr[CODE_OK] = packageName;
                    boolean checkPackagesInPermittedListOrSystem = checkPackagesInPermittedListOrSystem(Arrays.asList(strArr), admin.permittedInputMethods, userHandle);
                    return checkPackagesInPermittedListOrSystem;
                }
            }
        }
        throw new SecurityException("Only the system can query if an input method is disabled by admin");
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
        } else if (this.mInjector.userManagerIsSplitSystemUser() || (flags & CODE_USER_HAS_PROFILE_OWNER) == 0) {
            long id;
            UserHandle user = null;
            synchronized (this) {
                getActiveAdminForCallerLocked(admin, -2);
                id = this.mInjector.binderClearCallingIdentity();
                int userInfoFlags = CODE_OK;
                if ((flags & CODE_USER_HAS_PROFILE_OWNER) != 0) {
                    userInfoFlags = DumpState.DUMP_SHARED_USERS;
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
                    this.mIPackageManager.installExistingPackageAsUser(adminPkg, userHandle);
                }
                setActiveAdmin(profileOwner, true, userHandle);
                synchronized (this) {
                    DevicePolicyData policyData = getUserData(userHandle);
                    policyData.mInitBundle = adminExtras;
                    policyData.mAdminBroadcastPending = true;
                    saveSettingsLocked(userHandle);
                }
                setProfileOwner(profileOwner, getProfileOwnerName(Process.myUserHandle().getIdentifier()), userHandle);
                if ((flags & STATUS_BAR_DISABLE2_MASK) != 0) {
                    Secure.putIntForUser(this.mContext.getContentResolver(), "user_setup_complete", STATUS_BAR_DISABLE2_MASK, userHandle);
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
        boolean removeUser;
        Preconditions.checkNotNull(who, "ComponentName is null");
        synchronized (this) {
            getActiveAdminForCallerLocked(who, -2);
            long id = this.mInjector.binderClearCallingIdentity();
            try {
                removeUser = this.mUserManager.removeUser(userHandle.getIdentifier());
                this.mInjector.binderRestoreCallingIdentity(id);
            } catch (Throwable th) {
                this.mInjector.binderRestoreCallingIdentity(id);
            }
        }
        return removeUser;
    }

    public boolean switchUser(ComponentName who, UserHandle userHandle) {
        boolean switchUser;
        Preconditions.checkNotNull(who, "ComponentName is null");
        synchronized (this) {
            getActiveAdminForCallerLocked(who, -2);
            long id = this.mInjector.binderClearCallingIdentity();
            int userId = CODE_OK;
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

    public Bundle getApplicationRestrictions(ComponentName who, String packageName) {
        enforceCanManageApplicationRestrictions(who);
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

    public String[] setPackagesSuspended(ComponentName who, String[] packageNames, boolean suspended) {
        String[] packagesSuspendedAsUser;
        Preconditions.checkNotNull(who, "ComponentName is null");
        int callingUserId = UserHandle.getCallingUserId();
        synchronized (this) {
            getActiveAdminForCallerLocked(who, -1);
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

    public boolean isPackageSuspended(ComponentName who, String packageName) {
        boolean isPackageSuspendedForUser;
        Preconditions.checkNotNull(who, "ComponentName is null");
        int callingUserId = UserHandle.getCallingUserId();
        synchronized (this) {
            getActiveAdminForCallerLocked(who, -1);
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
                activeAdmin.ensureUserRestrictions().putBoolean(key, enabledFromThisOwner);
                saveSettingsLocked(userHandle);
                pushUserRestrictions(userHandle);
                sendChangedNotification(userHandle);
            }
        }
    }

    private void pushUserRestrictions(int userId) {
        synchronized (this) {
            Bundle global;
            Bundle local = new Bundle();
            if (this.mOwners.isDeviceOwnerUserId(userId)) {
                global = new Bundle();
                ActiveAdmin deviceOwner = getDeviceOwnerAdminLocked();
                if (deviceOwner == null) {
                    return;
                }
                UserRestrictionsUtils.sortToGlobalAndLocal(deviceOwner.userRestrictions, global, local);
                if (deviceOwner.disableCamera) {
                    global.putBoolean("no_camera", true);
                }
            } else {
                global = null;
                ActiveAdmin profileOwner = getProfileOwnerAdminLocked(userId);
                if (profileOwner != null) {
                    UserRestrictionsUtils.merge(local, profileOwner.userRestrictions);
                }
            }
            if (getCameraDisabled(null, userId, false)) {
                local.putBoolean("no_camera", true);
            }
            this.mUserManagerInternal.setDevicePolicyUserRestrictions(userId, local, global);
        }
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

    public boolean setApplicationHidden(ComponentName who, String packageName, boolean hidden) {
        boolean applicationHiddenSettingAsUser;
        Preconditions.checkNotNull(who, "ComponentName is null");
        int callingUserId = UserHandle.getCallingUserId();
        synchronized (this) {
            getActiveAdminForCallerLocked(who, -1);
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

    public boolean isApplicationHidden(ComponentName who, String packageName) {
        boolean applicationHiddenSettingAsUser;
        Preconditions.checkNotNull(who, "ComponentName is null");
        int callingUserId = UserHandle.getCallingUserId();
        synchronized (this) {
            getActiveAdminForCallerLocked(who, -1);
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

    public void enableSystemApp(ComponentName who, String packageName) {
        Preconditions.checkNotNull(who, "ComponentName is null");
        synchronized (this) {
            getActiveAdminForCallerLocked(who, -1);
            int userId = UserHandle.getCallingUserId();
            long id = this.mInjector.binderClearCallingIdentity();
            try {
                if (isSystemApp(this.mIPackageManager, packageName, getProfileParentId(userId))) {
                    this.mIPackageManager.installExistingPackageAsUser(packageName, userId);
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
    }

    public int enableSystemAppWithIntent(ComponentName who, Intent intent) {
        int i;
        Preconditions.checkNotNull(who, "ComponentName is null");
        synchronized (this) {
            getActiveAdminForCallerLocked(who, -1);
            int userId = UserHandle.getCallingUserId();
            long id = this.mInjector.binderClearCallingIdentity();
            try {
                int parentUserId = getProfileParentId(userId);
                List<ResolveInfo> activitiesToEnable = this.mIPackageManager.queryIntentActivities(intent, intent.resolveTypeIfNeeded(this.mContext.getContentResolver()), 786432, parentUserId).getList();
                i = CODE_OK;
                if (activitiesToEnable != null) {
                    for (ResolveInfo info : activitiesToEnable) {
                        if (info.activityInfo != null) {
                            String packageName = info.activityInfo.packageName;
                            if (isSystemApp(this.mIPackageManager, packageName, parentUserId)) {
                                i += STATUS_BAR_DISABLE2_MASK;
                                this.mIPackageManager.installExistingPackageAsUser(packageName, userId);
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
                return CODE_OK;
            } catch (Throwable th) {
                this.mInjector.binderRestoreCallingIdentity(id);
            }
        }
        return i;
    }

    private boolean isSystemApp(IPackageManager pm, String packageName, int userId) throws RemoteException {
        ApplicationInfo appInfo = pm.getApplicationInfo(packageName, DumpState.DUMP_PREFERRED_XML, userId);
        if (appInfo == null) {
            throw new IllegalArgumentException("The application " + packageName + " is not present on this device");
        } else if ((appInfo.flags & STATUS_BAR_DISABLE2_MASK) != 0) {
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
            for (int i = CODE_OK; i < N; i += STATUS_BAR_DISABLE2_MASK) {
                resultSet.addAll(((ActiveAdmin) policy.mAdminList.get(i)).accountTypesWithManagementDisabled);
            }
            strArr = (String[]) resultSet.toArray(new String[resultSet.size()]);
        }
        return strArr;
    }

    public void setUninstallBlocked(ComponentName who, String packageName, boolean uninstallBlocked) {
        Preconditions.checkNotNull(who, "ComponentName is null");
        int userId = UserHandle.getCallingUserId();
        synchronized (this) {
            getActiveAdminForCallerLocked(who, -1);
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
                if (managedUserId < 0) {
                } else if (isCrossProfileQuickContactDisabled(managedUserId)) {
                    this.mInjector.binderRestoreCallingIdentity(ident);
                } else {
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
            if (ui.id != callingUserId && ui.isManagedProfile()) {
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
        synchronized (this) {
            ActiveAdmin deviceOwner = getActiveAdminWithPolicyForUidLocked(who, -2, this.mInjector.binderGetCallingUid());
            ActiveAdmin profileOwner = getActiveAdminWithPolicyForUidLocked(who, -1, this.mInjector.binderGetCallingUid());
            if (deviceOwner != null || (profileOwner != null && isAffiliatedUser())) {
                setLockTaskPackagesLocked(this.mInjector.userHandleGetCallingUserId(), new ArrayList(Arrays.asList(packages)));
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

    public String[] getLockTaskPackages(ComponentName who) {
        String[] strArr;
        Preconditions.checkNotNull(who, "ComponentName is null");
        synchronized (this) {
            getActiveAdminForCallerLocked(who, -2);
            List<String> packages = getLockTaskPackagesLocked(this.mInjector.binderGetCallingUserHandle().getIdentifier());
            strArr = (String[]) packages.toArray(new String[packages.size()]);
        }
        return strArr;
    }

    private List<String> getLockTaskPackagesLocked(int userHandle) {
        return getUserData(userHandle).mLockTaskPackages;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isLockTaskPermitted(String pkg) {
        DevicePolicyData policy = getUserData(UserHandle.getUserId(this.mInjector.binderGetCallingUid()));
        synchronized (this) {
            int i = CODE_OK;
            while (true) {
                if (i >= policy.mLockTaskPackages.size()) {
                    return false;
                } else if (((String) policy.mLockTaskPackages.get(i)).equals(pkg)) {
                    return true;
                } else {
                    i += STATUS_BAR_DISABLE2_MASK;
                }
            }
        }
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
                Object[] objArr = new Object[STATUS_BAR_DISABLE2_MASK];
                objArr[CODE_OK] = setting;
                throw new SecurityException(String.format("Permission denial: device owners cannot update %1$s", objArr));
            }
        }
    }

    public void setSecureSetting(ComponentName who, String setting, String value) {
        Preconditions.checkNotNull(who, "ComponentName is null");
        int callingUserId = this.mInjector.userHandleGetCallingUserId();
        synchronized (this) {
            getActiveAdminForCallerLocked(who, -1);
            Object[] objArr;
            if (isDeviceOwner(who, callingUserId)) {
                if (!SECURE_SETTINGS_DEVICEOWNER_WHITELIST.contains(setting)) {
                    objArr = new Object[STATUS_BAR_DISABLE2_MASK];
                    objArr[CODE_OK] = setting;
                    throw new SecurityException(String.format("Permission denial: Device owners cannot update %1$s", objArr));
                }
            } else if (!SECURE_SETTINGS_WHITELIST.contains(setting)) {
                objArr = new Object[STATUS_BAR_DISABLE2_MASK];
                objArr[CODE_OK] = setting;
                throw new SecurityException(String.format("Permission denial: Profile owners cannot update %1$s", objArr));
            }
            long id = this.mInjector.binderClearCallingIdentity();
            try {
                this.mInjector.settingsSecurePutStringForUser(setting, value, callingUserId);
            } finally {
                this.mInjector.binderRestoreCallingIdentity(id);
            }
        }
    }

    public void setMasterVolumeMuted(ComponentName who, boolean on) {
        Preconditions.checkNotNull(who, "ComponentName is null");
        synchronized (this) {
            getActiveAdminForCallerLocked(who, -1);
            int userId = UserHandle.getCallingUserId();
            long identity = this.mInjector.binderClearCallingIdentity();
            try {
                IAudioService.Stub.asInterface(ServiceManager.getService("audio")).setMasterMute(on, CODE_OK, this.mContext.getPackageName(), userId);
                this.mInjector.binderRestoreCallingIdentity(identity);
            } catch (RemoteException re) {
                Slog.e(LOG_TAG, "Failed to setMasterMute", re);
                this.mInjector.binderRestoreCallingIdentity(identity);
            } catch (Throwable th) {
                this.mInjector.binderRestoreCallingIdentity(identity);
            }
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
            return true;
        }
    }

    private boolean setStatusBarDisabledInternal(boolean disabled, int userId) {
        long ident = this.mInjector.binderClearCallingIdentity();
        try {
            IStatusBarService statusBarService = IStatusBarService.Stub.asInterface(ServiceManager.checkService(TAG_STATUS_BAR));
            if (statusBarService != null) {
                int flags1 = disabled ? STATUS_BAR_DISABLE_MASK : CODE_OK;
                int flags2 = disabled ? STATUS_BAR_DISABLE2_MASK : CODE_OK;
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

    void updateUserSetupComplete() {
        List<UserInfo> users = this.mUserManager.getUsers(true);
        int N = users.size();
        for (int i = CODE_OK; i < N; i += STATUS_BAR_DISABLE2_MASK) {
            int userHandle = ((UserInfo) users.get(i)).id;
            if (this.mInjector.settingsSecureGetIntForUser("user_setup_complete", CODE_OK, userHandle) != 0) {
                DevicePolicyData policy = getUserData(userHandle);
                if (policy.mUserSetupComplete) {
                    continue;
                } else {
                    policy.mUserSetupComplete = true;
                    synchronized (this) {
                        saveSettingsLocked(userHandle);
                    }
                }
            }
        }
    }

    private static boolean isLimitPasswordAllowed(ActiveAdmin admin, int minPasswordQuality) {
        if (admin.passwordQuality < minPasswordQuality) {
            return false;
        }
        return admin.info.usesPolicy(CODE_OK);
    }

    public void setSystemUpdatePolicy(ComponentName who, SystemUpdatePolicy policy) {
        if (policy == null || policy.isValid()) {
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

    public SystemUpdatePolicy getSystemUpdatePolicy() {
        synchronized (this) {
            SystemUpdatePolicy policy = this.mOwners.getSystemUpdatePolicy();
            if (policy == null || policy.isValid()) {
                return policy;
            }
            Slog.w(LOG_TAG, "Stored system update policy is invalid, return null instead.");
            return null;
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
                String[] pkgs = this.mContext.getPackageManager().getPackagesForUid(callerUid);
                int length = pkgs.length;
                for (int i = CODE_OK; i < length; i += STATUS_BAR_DISABLE2_MASK) {
                    if (deviceOwnerPackageName.equals(pkgs[i])) {
                        return true;
                    }
                }
                return false;
            }
        }
    }

    public void notifyPendingSystemUpdate(long updateReceivedTime) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.NOTIFY_PENDING_SYSTEM_UPDATE", "Only the system update service can broadcast update information");
        if (UserHandle.getCallingUserId() != 0) {
            Slog.w(LOG_TAG, "Only the system update service in the system user can broadcast update information.");
            return;
        }
        Intent intent = new Intent("android.app.action.NOTIFY_PENDING_SYSTEM_UPDATE");
        intent.putExtra("android.app.extra.SYSTEM_UPDATE_RECEIVED_TIME", updateReceivedTime);
        synchronized (this) {
            String packageName;
            if (this.mOwners.hasDeviceOwner()) {
                packageName = this.mOwners.getDeviceOwnerComponent().getPackageName();
            } else {
                packageName = null;
            }
            if (packageName == null) {
                return;
            }
            long ident;
            UserHandle deviceOwnerUser = new UserHandle(this.mOwners.getDeviceOwnerUserId());
            ActivityInfo[] receivers = null;
            try {
                receivers = this.mContext.getPackageManager().getPackageInfo(packageName, CODE_USER_HAS_PROFILE_OWNER).receivers;
            } catch (NameNotFoundException e) {
                Log.e(LOG_TAG, "Cannot find device owner package", e);
            } catch (Throwable th) {
                this.mInjector.binderRestoreCallingIdentity(ident);
            }
            if (receivers != null) {
                ident = this.mInjector.binderClearCallingIdentity();
                for (int i = CODE_OK; i < receivers.length; i += STATUS_BAR_DISABLE2_MASK) {
                    if ("android.permission.BIND_DEVICE_ADMIN".equals(receivers[i].permission)) {
                        intent.setComponent(new ComponentName(packageName, receivers[i].name));
                        this.mContext.sendBroadcastAsUser(intent, deviceOwnerUser);
                    }
                }
                this.mInjector.binderRestoreCallingIdentity(ident);
            }
        }
    }

    public void setPermissionPolicy(ComponentName admin, int policy) throws RemoteException {
        int userId = UserHandle.getCallingUserId();
        synchronized (this) {
            getActiveAdminForCallerLocked(admin, -1);
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

    public boolean setPermissionGrantState(ComponentName admin, String packageName, String permission, int grantState) throws RemoteException {
        UserHandle user = this.mInjector.binderGetCallingUserHandle();
        synchronized (this) {
            getActiveAdminForCallerLocked(admin, -1);
            long ident = this.mInjector.binderClearCallingIdentity();
            try {
                if (getTargetSdk(packageName, user.getIdentifier()) < 23) {
                    this.mInjector.binderRestoreCallingIdentity(ident);
                    return false;
                }
                PackageManager packageManager = this.mContext.getPackageManager();
                switch (grantState) {
                    case CODE_OK /*0*/:
                        packageManager.updatePermissionFlags(permission, packageName, CODE_USER_SETUP_COMPLETED, CODE_OK, user);
                        break;
                    case STATUS_BAR_DISABLE2_MASK /*1*/:
                        packageManager.grantRuntimePermission(packageName, permission, user);
                        packageManager.updatePermissionFlags(permission, packageName, CODE_USER_SETUP_COMPLETED, CODE_USER_SETUP_COMPLETED, user);
                        break;
                    case CODE_USER_HAS_PROFILE_OWNER /*2*/:
                        packageManager.revokeRuntimePermission(packageName, permission, user);
                        packageManager.updatePermissionFlags(permission, packageName, CODE_USER_SETUP_COMPLETED, CODE_USER_SETUP_COMPLETED, user);
                        break;
                }
                this.mInjector.binderRestoreCallingIdentity(ident);
                return true;
            } catch (SecurityException e) {
                this.mInjector.binderRestoreCallingIdentity(ident);
                return false;
            } catch (Throwable th) {
                this.mInjector.binderRestoreCallingIdentity(ident);
            }
        }
    }

    public int getPermissionGrantState(ComponentName admin, String packageName, String permission) throws RemoteException {
        PackageManager packageManager = this.mContext.getPackageManager();
        UserHandle user = this.mInjector.binderGetCallingUserHandle();
        synchronized (this) {
            getActiveAdminForCallerLocked(admin, -1);
            long ident = this.mInjector.binderClearCallingIdentity();
            try {
                int granted = this.mIPackageManager.checkPermission(permission, packageName, user.getIdentifier());
                if ((packageManager.getPermissionFlags(permission, packageName, user) & CODE_USER_SETUP_COMPLETED) != CODE_USER_SETUP_COMPLETED) {
                    this.mInjector.binderRestoreCallingIdentity(ident);
                    return CODE_OK;
                }
                int i;
                if (granted == 0) {
                    i = STATUS_BAR_DISABLE2_MASK;
                } else {
                    i = CODE_USER_HAS_PROFILE_OWNER;
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
            PackageInfo pi = this.mInjector.getIPackageManager().getPackageInfo(packageName, CODE_OK, userHandle);
            if (pi == null || pi.applicationInfo.flags == 0) {
                return false;
            }
            return true;
        } catch (RemoteException re) {
            throw new RuntimeException("Package manager has died", re);
        }
    }

    public boolean isProvisioningAllowed(String action) {
        if (!this.mHasFeature) {
            return false;
        }
        int callingUserId = this.mInjector.userHandleGetCallingUserId();
        if ("android.app.action.PROVISION_MANAGED_PROFILE".equals(action)) {
            if (!hasFeatureManagedUsers()) {
                return false;
            }
            synchronized (this) {
                if (this.mOwners.hasDeviceOwner()) {
                    if (!this.mInjector.userManagerIsSplitSystemUser()) {
                        return false;
                    } else if (this.mOwners.getDeviceOwnerUserId() != 0) {
                        return false;
                    } else if (callingUserId == 0) {
                        return false;
                    }
                }
                if (getProfileOwner(callingUserId) != null) {
                    return false;
                }
                long ident = this.mInjector.binderClearCallingIdentity();
                try {
                    if (!this.mUserManager.canAddMoreManagedProfiles(callingUserId, true)) {
                        return false;
                    }
                    this.mInjector.binderRestoreCallingIdentity(ident);
                    return true;
                } finally {
                    this.mInjector.binderRestoreCallingIdentity(ident);
                }
            }
        } else if ("android.app.action.PROVISION_MANAGED_DEVICE".equals(action)) {
            return isDeviceOwnerProvisioningAllowed(callingUserId);
        } else {
            if ("android.app.action.PROVISION_MANAGED_USER".equals(action)) {
                return hasFeatureManagedUsers() && this.mInjector.userManagerIsSplitSystemUser() && callingUserId != 0 && !hasUserSetupCompleted(callingUserId);
            } else {
                if (!"android.app.action.PROVISION_MANAGED_SHAREABLE_DEVICE".equals(action)) {
                    throw new IllegalArgumentException("Unknown provisioning action " + action);
                } else if (this.mInjector.userManagerIsSplitSystemUser()) {
                    return isDeviceOwnerProvisioningAllowed(callingUserId);
                } else {
                    return false;
                }
            }
        }
    }

    private synchronized int checkSetDeviceOwnerPreCondition(int deviceOwnerUserId, boolean isAdb) {
        if (this.mOwners.hasDeviceOwner()) {
            return STATUS_BAR_DISABLE2_MASK;
        }
        if (this.mOwners.hasProfileOwner(deviceOwnerUserId)) {
            return CODE_USER_HAS_PROFILE_OWNER;
        }
        if (!this.mUserManager.isUserRunning(new UserHandle(deviceOwnerUserId))) {
            return CODE_USER_NOT_RUNNING;
        }
        if (isAdb) {
            if (hasUserSetupCompleted(CODE_OK) && !this.mInjector.userManagerIsSplitSystemUser()) {
                if (this.mUserManager.getUserCount() > STATUS_BAR_DISABLE2_MASK) {
                    return CODE_NONSYSTEM_USER_EXISTS;
                }
                if (AccountManager.get(this.mContext).getAccounts().length > 0) {
                    return CODE_ACCOUNTS_NOT_EMPTY;
                }
            }
            return CODE_OK;
        }
        if (!this.mInjector.userManagerIsSplitSystemUser()) {
            if (deviceOwnerUserId != 0) {
                return CODE_NOT_SYSTEM_USER;
            }
            if (hasUserSetupCompleted(CODE_OK)) {
                return CODE_USER_SETUP_COMPLETED;
            }
        }
        return CODE_OK;
    }

    private boolean isDeviceOwnerProvisioningAllowed(int deviceOwnerUserId) {
        return checkSetDeviceOwnerPreCondition(deviceOwnerUserId, false) == 0;
    }

    private boolean hasFeatureManagedUsers() {
        try {
            return this.mIPackageManager.hasSystemFeature("android.software.managed_users", CODE_OK);
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
            ApplicationInfo ai = this.mIPackageManager.getApplicationInfo(packageName, CODE_OK, userId);
            return ai == null ? CODE_OK : ai.targetSdkVersion;
        } catch (RemoteException e) {
            return CODE_OK;
        }
    }

    public boolean isManagedProfile(ComponentName admin) {
        synchronized (this) {
            getActiveAdminForCallerLocked(admin, -1);
        }
        UserInfo user = getUserInfo(this.mInjector.userHandleGetCallingUserId());
        if (user != null) {
            return user.isManagedProfile();
        }
        return false;
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
            enforceManagedProfile(userHandle, "set organization name");
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
        Set<String> affiliationIds = new ArraySet(ids);
        int callingUserId = this.mInjector.userHandleGetCallingUserId();
        synchronized (this) {
            getActiveAdminForCallerLocked(admin, -1);
            getUserData(callingUserId).mAffiliationIds = affiliationIds;
            saveSettingsLocked(callingUserId);
            if (callingUserId != 0 && isDeviceOwner(admin, callingUserId)) {
                getUserData(CODE_OK).mAffiliationIds = affiliationIds;
                saveSettingsLocked(CODE_OK);
            }
        }
    }

    public boolean isAffiliatedUser() {
        int callingUserId = this.mInjector.userHandleGetCallingUserId();
        synchronized (this) {
            if (this.mOwners.getDeviceOwnerUserId() == callingUserId) {
                return true;
            }
            ComponentName profileOwner = getProfileOwner(callingUserId);
            if (profileOwner == null || !profileOwner.getPackageName().equals(this.mOwners.getDeviceOwnerPackageName())) {
                return false;
            }
            Set<String> userAffiliationIds = getUserData(callingUserId).mAffiliationIds;
            Set<String> deviceAffiliationIds = getUserData(CODE_OK).mAffiliationIds;
            for (String id : userAffiliationIds) {
                if (deviceAffiliationIds.contains(id)) {
                    return true;
                }
            }
            return false;
        }
    }

    private synchronized void disableSecurityLoggingIfNotCompliant() {
        if (!isDeviceOwnerManagedSingleUserDevice()) {
            this.mInjector.securityLogSetLoggingEnabledProperty(false);
            Slog.w(LOG_TAG, "Security logging turned off as it's no longer a single user device.");
        }
    }

    public void setSecurityLoggingEnabled(ComponentName admin, boolean enabled) {
        Preconditions.checkNotNull(admin);
        ensureDeviceOwnerManagingSingleUser(admin);
        synchronized (this) {
            if (enabled == this.mInjector.securityLogGetLoggingEnabledProperty()) {
                return;
            }
            this.mInjector.securityLogSetLoggingEnabledProperty(enabled);
            if (enabled) {
                this.mSecurityLogMonitor.start();
            } else {
                this.mSecurityLogMonitor.stop();
            }
        }
    }

    public boolean isSecurityLoggingEnabled(ComponentName admin) {
        boolean securityLogGetLoggingEnabledProperty;
        Preconditions.checkNotNull(admin);
        synchronized (this) {
            getActiveAdminForCallerLocked(admin, -2);
            securityLogGetLoggingEnabledProperty = this.mInjector.securityLogGetLoggingEnabledProperty();
        }
        return securityLogGetLoggingEnabledProperty;
    }

    public ParceledListSlice<SecurityEvent> retrievePreRebootSecurityLogs(ComponentName admin) {
        Preconditions.checkNotNull(admin);
        ensureDeviceOwnerManagingSingleUser(admin);
        if (!this.mContext.getResources().getBoolean(17957053)) {
            return null;
        }
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
        Preconditions.checkNotNull(admin);
        ensureDeviceOwnerManagingSingleUser(admin);
        List<SecurityEvent> logs = this.mSecurityLogMonitor.retrieveLogs();
        if (logs != null) {
            return new ParceledListSlice(logs);
        }
        return null;
    }

    private void enforceCanManageDeviceAdmin() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_DEVICE_ADMINS", null);
    }

    private void enforceCanManageProfileAndDeviceOwners() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_PROFILE_AND_DEVICE_OWNERS", null);
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

    public void uninstallPackageWithActiveAdmins(String packageName) {
        enforceCanManageDeviceAdmin();
        Preconditions.checkArgument(!TextUtils.isEmpty(packageName));
        int userId = this.mInjector.userHandleGetCallingUserId();
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
            List<ComponentName> packageActiveAdmins = new ArrayList();
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
                this.mHandler.postDelayed(new AnonymousClass11(packageActiveAdmins, userId, packageName), JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY);
                return;
            }
        }
        throw new IllegalArgumentException("Cannot uninstall a package with a profile owner");
    }

    private void removePackageIfRequired(String packageName, int userId) {
        if (!packageHasActiveAdmins(packageName, userId)) {
            startUninstallIntent(packageName, userId);
        }
    }

    private void startUninstallIntent(String packageName, int userId) {
        Pair<String, Integer> packageUserPair = new Pair(packageName, Integer.valueOf(userId));
        synchronized (this) {
            if (this.mPackagesToRemove.contains(packageUserPair)) {
                this.mPackagesToRemove.remove(packageUserPair);
                try {
                    if (this.mInjector.getIPackageManager().getPackageInfo(packageName, CODE_OK, userId) == null) {
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
                return;
            }
        }
    }

    private void removeAdminArtifacts(ComponentName adminReceiver, int userHandle) {
        synchronized (this) {
            ActiveAdmin admin = getActiveAdminUncheckedLocked(adminReceiver, userHandle);
            if (admin == null) {
                return;
            }
            DevicePolicyData policy = getUserData(userHandle);
            boolean doProxyCleanup = admin.info.usesPolicy(CODE_NONSYSTEM_USER_EXISTS);
            policy.mAdminList.remove(admin);
            policy.mAdminMap.remove(adminReceiver);
            validatePasswordOwnerLocked(policy);
            if (doProxyCleanup) {
                resetGlobalProxyLocked(policy);
            }
            saveSettingsLocked(userHandle);
            updateMaximumTimeToLockLocked(userHandle);
            policy.mRemovingAdmins.remove(adminReceiver);
            syncHwDeviceSettingsLocked(policy.mUserHandle);
            pushUserRestrictions(userHandle);
        }
    }
}
