package com.android.server.pm;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.IntentFilterVerificationInfo;
import android.content.pm.PackageCleanItem;
import android.content.pm.PackageManager;
import android.content.pm.PackageParser;
import android.content.pm.PackageUserState;
import android.content.pm.PermissionInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.content.pm.UserInfo;
import android.content.pm.VerifierDeviceIdentity;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.FileUtils;
import android.os.Handler;
import android.os.Message;
import android.os.PatternMatcher;
import android.os.PersistableBundle;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.storage.StorageManager;
import android.system.ErrnoException;
import android.system.Os;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.AtomicFile;
import android.util.Log;
import android.util.LogPrinter;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.util.SparseLongArray;
import android.util.Xml;
import android.util.proto.ProtoOutputStream;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.logging.EventLogTags;
import com.android.internal.os.BackgroundThread;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.JournaledFile;
import com.android.internal.util.XmlUtils;
import com.android.server.BatteryService;
import com.android.server.UiModeManagerService;
import com.android.server.connectivity.NetworkAgentInfo;
import com.android.server.os.HwBootFail;
import com.android.server.pm.Installer;
import com.android.server.pm.permission.BasePermission;
import com.android.server.pm.permission.PermissionSettings;
import com.android.server.pm.permission.PermissionsState;
import com.android.server.power.IHwShutdownThread;
import com.android.server.slice.SliceClientPermissions;
import com.android.server.usb.descriptors.UsbTerminalTypes;
import com.android.server.voiceinteraction.DatabaseHelper;
import huawei.cust.HwCfgFilePolicy;
import huawei.cust.HwCustUtils;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public final class Settings {
    private static final String ATTR_APP_LINK_GENERATION = "app-link-generation";
    private static final String ATTR_BLOCKED = "blocked";
    @Deprecated
    private static final String ATTR_BLOCK_UNINSTALL = "blockUninstall";
    private static final String ATTR_CE_DATA_INODE = "ceDataInode";
    private static final String ATTR_CODE = "code";
    private static final String ATTR_DATABASE_VERSION = "databaseVersion";
    private static final String ATTR_DOMAIN_VERIFICATON_STATE = "domainVerificationStatus";
    private static final String ATTR_ENABLED = "enabled";
    private static final String ATTR_ENABLED_CALLER = "enabledCaller";
    private static final String ATTR_ENFORCEMENT = "enforcement";
    private static final String ATTR_FINGERPRINT = "fingerprint";
    private static final String ATTR_FLAGS = "flags";
    private static final String ATTR_GRANTED = "granted";
    private static final String ATTR_HARMFUL_APP_WARNING = "harmful-app-warning";
    private static final String ATTR_HIDDEN = "hidden";
    private static final String ATTR_HWFINGERPRINT = "hwFingerprint";
    private static final String ATTR_INSTALLED = "inst";
    private static final String ATTR_INSTALL_REASON = "install-reason";
    private static final String ATTR_INSTANT_APP = "instant-app";
    public static final String ATTR_NAME = "name";
    private static final String ATTR_NOT_LAUNCHED = "nl";
    public static final String ATTR_PACKAGE = "package";
    private static final String ATTR_PACKAGE_NAME = "packageName";
    private static final String ATTR_REVOKE_ON_UPGRADE = "rou";
    private static final String ATTR_SDK_VERSION = "sdkVersion";
    private static final String ATTR_STOPPED = "stopped";
    private static final String ATTR_SUSPENDED = "suspended";
    private static final String ATTR_SUSPENDING_PACKAGE = "suspending-package";
    private static final String ATTR_SUSPEND_DIALOG_MESSAGE = "suspend_dialog_message";
    private static final String ATTR_USER = "user";
    private static final String ATTR_USER_FIXED = "fixed";
    private static final String ATTR_USER_SET = "set";
    private static final String ATTR_VERSION = "version";
    private static final String ATTR_VIRTUAL_PRELOAD = "virtual-preload";
    private static final String ATTR_VOLUME_UUID = "volumeUuid";
    public static final int CURRENT_DATABASE_VERSION = 3;
    private static final boolean DEBUG_KERNEL = false;
    private static final boolean DEBUG_MU = false;
    private static final boolean DEBUG_PARSER = false;
    private static final boolean DEBUG_STOPPED = false;
    private static final String DIR_CUST_XML = "/data/cust/xml/";
    private static final String DIR_ETC_XML = "/system/etc/xml/";
    private static final String FILE_SUB_USER_DELAPPS_LIST = "hw_subuser_delapps_config.xml";
    static final Object[] FLAG_DUMP_SPEC = {1, "SYSTEM", 2, "DEBUGGABLE", 4, "HAS_CODE", 8, "PERSISTENT", 16, "FACTORY_TEST", 32, "ALLOW_TASK_REPARENTING", 64, "ALLOW_CLEAR_USER_DATA", 128, "UPDATED_SYSTEM_APP", 256, "TEST_ONLY", 16384, "VM_SAFE_MODE", 32768, "ALLOW_BACKUP", 65536, "KILL_AFTER_RESTORE", 131072, "RESTORE_ANY_VERSION", 262144, "EXTERNAL_STORAGE", Integer.valueOf(DumpState.DUMP_DEXOPT), "LARGE_HEAP"};
    static final Object[] HW_FLAG_DUMP_SPEC = {Integer.valueOf(DumpState.DUMP_VOLUMES), "PARSE_IS_BOTH_APK", Integer.valueOf(DumpState.DUMP_SERVICE_PERMISSIONS), "PARSE_IS_MAPLE_APK", Integer.valueOf(DumpState.DUMP_HANDLE), "PARSE_IS_REMOVABLE_PREINSTALLED_APK", 67108864, "FLAG_UPDATED_REMOVEABLE_APP"};
    private static final String KEY_PACKAGE_SETTINS_ERROR = "persist.sys.package_settings_error";
    private static int PRE_M_APP_INFO_FLAG_CANT_SAVE_STATE = 268435456;
    private static int PRE_M_APP_INFO_FLAG_FORWARD_LOCK = 536870912;
    private static int PRE_M_APP_INFO_FLAG_HIDDEN = 134217728;
    private static int PRE_M_APP_INFO_FLAG_PRIVILEGED = 1073741824;
    private static final Object[] PRIVATE_FLAG_DUMP_SPEC = {1024, "PRIVATE_FLAG_ACTIVITIES_RESIZE_MODE_RESIZEABLE", 4096, "PRIVATE_FLAG_ACTIVITIES_RESIZE_MODE_RESIZEABLE_VIA_SDK_VERSION", 2048, "PRIVATE_FLAG_ACTIVITIES_RESIZE_MODE_UNRESIZEABLE", 8192, "BACKUP_IN_FOREGROUND", 2, "CANT_SAVE_STATE", 32, "DEFAULT_TO_DEVICE_PROTECTED_STORAGE", 64, "DIRECT_BOOT_AWARE", 4, "FORWARD_LOCK", 16, "HAS_DOMAIN_URLS", 1, "HIDDEN", 128, "EPHEMERAL", 32768, "ISOLATED_SPLIT_LOADING", 131072, "OEM", 256, "PARTIALLY_DIRECT_BOOT_AWARE", 8, "PRIVILEGED", 512, "REQUIRED_FOR_SYSTEM_USER", 16384, "STATIC_SHARED_LIBRARY", 262144, "VENDOR", Integer.valueOf(DumpState.DUMP_FROZEN), "PRODUCT", 65536, "VIRTUAL_PRELOAD"};
    private static final String RUNTIME_PERMISSIONS_FILE_NAME = "runtime-permissions.xml";
    private static final String TAG = "PackageSettings";
    private static final String TAG_ALL_INTENT_FILTER_VERIFICATION = "all-intent-filter-verifications";
    private static final String TAG_BLOCK_UNINSTALL = "block-uninstall";
    private static final String TAG_BLOCK_UNINSTALL_PACKAGES = "block-uninstall-packages";
    private static final String TAG_CHILD_PACKAGE = "child-package";
    static final String TAG_CROSS_PROFILE_INTENT_FILTERS = "crossProfile-intent-filters";
    private static final String TAG_DEFAULT_APPS = "default-apps";
    private static final String TAG_DEFAULT_BROWSER = "default-browser";
    private static final String TAG_DEFAULT_DIALER = "default-dialer";
    private static final String TAG_DISABLED_COMPONENTS = "disabled-components";
    private static final String TAG_DOMAIN_VERIFICATION = "domain-verification";
    private static final String TAG_ENABLED_COMPONENTS = "enabled-components";
    public static final String TAG_ITEM = "item";
    private static final String TAG_PACKAGE = "pkg";
    private static final String TAG_PACKAGE_RESTRICTIONS = "package-restrictions";
    private static final String TAG_PERMISSIONS = "perms";
    private static final String TAG_PERMISSION_ENTRY = "perm";
    private static final String TAG_PERSISTENT_PREFERRED_ACTIVITIES = "persistent-preferred-activities";
    private static final String TAG_READ_EXTERNAL_STORAGE = "read-external-storage";
    private static final String TAG_RESTORED_RUNTIME_PERMISSIONS = "restored-perms";
    private static final String TAG_RUNTIME_PERMISSIONS = "runtime-permissions";
    private static final String TAG_SHARED_USER = "shared-user";
    private static final String TAG_SUSPENDED_APP_EXTRAS = "suspended-app-extras";
    private static final String TAG_SUSPENDED_LAUNCHER_EXTRAS = "suspended-launcher-extras";
    private static final String TAG_USES_STATIC_LIB = "uses-static-lib";
    private static final String TAG_VERSION = "version";
    private static final int USER_RUNTIME_GRANT_MASK = 11;
    private static int mFirstAvailableUid = 0;
    private static AtomicBoolean mIsCheckDelAppsFinished = new AtomicBoolean(false);
    private boolean isNeedRetryNewUserId;
    private final File mBackupSettingsFilename;
    private final File mBackupStoppedPackagesFilename;
    private final SparseArray<ArraySet<String>> mBlockUninstallPackages;
    final SparseArray<CrossProfileIntentResolver> mCrossProfileIntentResolvers;
    private HwCustSettings mCustSettings;
    final SparseArray<String> mDefaultBrowserApp;
    final SparseArray<String> mDefaultDialerApp;
    private ArrayList<String> mDelAppLists;
    protected final ArrayMap<String, PackageSetting> mDisabledSysPackages;
    final ArraySet<String> mInstallerPackages;
    private boolean mIsPackageSettingsError;
    private final ArrayMap<String, KernelPackageState> mKernelMapping;
    private final File mKernelMappingFilename;
    public final KeySetManagerService mKeySetManagerService;
    private final ArrayMap<Long, Integer> mKeySetRefs;
    private final Object mLock;
    final SparseIntArray mNextAppLinkGeneration;
    private final SparseArray<Object> mOtherUserIds;
    private final File mPackageListFilename;
    final ArrayMap<String, PackageSetting> mPackages;
    final ArrayList<PackageCleanItem> mPackagesToBeCleaned;
    private final ArrayList<Signature> mPastSignatures;
    private final ArrayList<PackageSetting> mPendingPackages;
    final PermissionSettings mPermissions;
    final SparseArray<PersistentPreferredIntentResolver> mPersistentPreferredActivities;
    final SparseArray<PreferredIntentResolver> mPreferredActivities;
    Boolean mReadExternalStorageEnforced;
    final StringBuilder mReadMessages;
    private final ArrayMap<String, String> mRenamedPackages;
    private final ArrayMap<String, IntentFilterVerificationInfo> mRestoredIntentFilterVerifications;
    /* access modifiers changed from: private */
    public final SparseArray<ArrayMap<String, ArraySet<RestoredPermissionGrant>>> mRestoredUserGrants;
    private final RuntimePermissionPersistence mRuntimePermissionsPersistence;
    private final File mSettingsFilename;
    final ArrayMap<String, SharedUserSetting> mSharedUsers;
    private final File mStoppedPackagesFilename;
    private final File mSystemDir;
    private final ArrayList<Object> mUserIds;
    private VerifierDeviceIdentity mVerifierDeviceIdentity;
    private ArrayMap<String, VersionInfo> mVersion;

    public static class DatabaseVersion {
        public static final int FIRST_VERSION = 1;
        public static final int SIGNATURE_END_ENTITY = 2;
        public static final int SIGNATURE_MALFORMED_RECOVER = 3;
    }

    private static final class KernelPackageState {
        int appId;
        int[] excludedUserIds;

        private KernelPackageState() {
        }
    }

    final class RestoredPermissionGrant {
        int grantBits;
        boolean granted;
        String permissionName;

        RestoredPermissionGrant(String name, boolean isGranted, int theGrantBits) {
            this.permissionName = name;
            this.granted = isGranted;
            this.grantBits = theGrantBits;
        }
    }

    private final class RuntimePermissionPersistence {
        private static final long MAX_WRITE_PERMISSIONS_DELAY_MILLIS = 2000;
        private static final long WRITE_PERMISSIONS_DELAY_MILLIS = 200;
        @GuardedBy("mLock")
        private final SparseBooleanArray mDefaultPermissionsGranted = new SparseBooleanArray();
        @GuardedBy("mLock")
        private final SparseArray<String> mFingerprints = new SparseArray<>();
        private final Handler mHandler = new MyHandler();
        @GuardedBy("mLock")
        private final SparseLongArray mLastNotWrittenMutationTimesMillis = new SparseLongArray();
        private final Object mPersistenceLock;
        @GuardedBy("mLock")
        private final SparseBooleanArray mWriteScheduled = new SparseBooleanArray();

        private final class MyHandler extends Handler {
            public MyHandler() {
                super(BackgroundThread.getHandler().getLooper());
            }

            public void handleMessage(Message message) {
                int userId = message.what;
                Runnable callback = (Runnable) message.obj;
                RuntimePermissionPersistence.this.writePermissionsSync(userId);
                if (callback != null) {
                    callback.run();
                }
            }
        }

        public RuntimePermissionPersistence(Object persistenceLock) {
            this.mPersistenceLock = persistenceLock;
        }

        public boolean areDefaultRuntimPermissionsGrantedLPr(int userId) {
            return this.mDefaultPermissionsGranted.get(userId);
        }

        public void onDefaultRuntimePermissionsGrantedLPr(int userId) {
            this.mFingerprints.put(userId, Build.FINGERPRINT);
            writePermissionsForUserAsyncLPr(userId);
        }

        public void writePermissionsForUserSyncLPr(int userId) {
            this.mHandler.removeMessages(userId);
            writePermissionsSync(userId);
        }

        public void writePermissionsForUserAsyncLPr(int userId) {
            long currentTimeMillis = SystemClock.uptimeMillis();
            if (this.mWriteScheduled.get(userId)) {
                this.mHandler.removeMessages(userId);
                long lastNotWrittenMutationTimeMillis = this.mLastNotWrittenMutationTimesMillis.get(userId);
                if (currentTimeMillis - lastNotWrittenMutationTimeMillis >= MAX_WRITE_PERMISSIONS_DELAY_MILLIS) {
                    this.mHandler.obtainMessage(userId).sendToTarget();
                    return;
                }
                long writeDelayMillis = Math.min(WRITE_PERMISSIONS_DELAY_MILLIS, Math.max((MAX_WRITE_PERMISSIONS_DELAY_MILLIS + lastNotWrittenMutationTimeMillis) - currentTimeMillis, 0));
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(userId), writeDelayMillis);
            } else {
                this.mLastNotWrittenMutationTimesMillis.put(userId, currentTimeMillis);
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(userId), WRITE_PERMISSIONS_DELAY_MILLIS);
                this.mWriteScheduled.put(userId, true);
            }
        }

        /* access modifiers changed from: private */
        /* JADX WARNING: Code restructure failed: missing block: B:102:?, code lost:
            android.util.Slog.wtf("PackageManager", "Failed to write settings, restoring backup", r0);
            r3.failWrite(r6);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:103:0x0295, code lost:
            libcore.io.IoUtils.closeQuietly(r6);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:104:0x0299, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:105:0x029a, code lost:
            r0 = th;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:106:0x029b, code lost:
            libcore.io.IoUtils.closeQuietly(r6);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:107:0x029e, code lost:
            throw r0;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:26:0x00aa, code lost:
            r0 = null;
            r6 = null;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:28:?, code lost:
            r6 = r3.startWrite();
            r8 = android.util.Xml.newSerializer();
            r8.setOutput(r6, java.nio.charset.StandardCharsets.UTF_8.name());
            r8.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            r8.startDocument(null, true);
            r8.startTag(null, com.android.server.pm.Settings.TAG_RUNTIME_PERMISSIONS);
            r9 = r1.mFingerprints.get(r2);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:29:0x00d9, code lost:
            if (r9 == null) goto L_0x00ef;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:32:?, code lost:
            r8.attribute(null, com.android.server.pm.Settings.ATTR_FINGERPRINT, r9);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:33:0x00e1, code lost:
            r0 = th;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:34:0x00e2, code lost:
            r18 = r4;
            r22 = r5;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:35:0x00e8, code lost:
            r0 = th;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:36:0x00e9, code lost:
            r18 = r4;
            r22 = r5;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:38:?, code lost:
            r11 = r4.size();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:39:0x00f3, code lost:
            r12 = 0;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:40:0x00f4, code lost:
            if (r12 >= r11) goto L_0x011a;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:42:?, code lost:
            r8.startTag(null, "pkg");
            r8.attribute(null, com.android.server.pm.Settings.ATTR_NAME, r4.keyAt(r12));
            writePermissions(r8, r4.valueAt(r12));
            r8.endTag(null, "pkg");
         */
        /* JADX WARNING: Code restructure failed: missing block: B:43:0x0117, code lost:
            r12 = r12 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:45:?, code lost:
            r12 = r5.size();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:46:0x011e, code lost:
            r13 = 0;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:47:0x011f, code lost:
            if (r13 >= r12) goto L_0x0145;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:49:?, code lost:
            r8.startTag(null, com.android.server.pm.Settings.TAG_SHARED_USER);
            r8.attribute(null, com.android.server.pm.Settings.ATTR_NAME, r5.keyAt(r13));
            writePermissions(r8, r5.valueAt(r13));
            r8.endTag(null, com.android.server.pm.Settings.TAG_SHARED_USER);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:50:0x0142, code lost:
            r13 = r13 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:53:?, code lost:
            r8.endTag(null, com.android.server.pm.Settings.TAG_RUNTIME_PERMISSIONS);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:54:0x0155, code lost:
            if (com.android.server.pm.Settings.access$300(r1.this$0).get(r2) == null) goto L_0x023c;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:55:0x0157, code lost:
            r7 = (android.util.ArrayMap) com.android.server.pm.Settings.access$300(r1.this$0).get(r2);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:56:0x0163, code lost:
            if (r7 == null) goto L_0x023c;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:57:0x0165, code lost:
            r13 = r7.size();
            r14 = 0;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:58:0x016a, code lost:
            if (r14 >= r13) goto L_0x023c;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:59:0x016c, code lost:
            r15 = r7.valueAt(r14);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:60:0x0173, code lost:
            if (r15 == null) goto L_0x0229;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:62:0x0179, code lost:
            if (r15.size() <= 0) goto L_0x0229;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:63:0x017b, code lost:
            r17 = r7.keyAt(r14);
            r8.startTag(r0, com.android.server.pm.Settings.TAG_RESTORED_RUNTIME_PERMISSIONS);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:64:0x018c, code lost:
            r18 = r4;
            r4 = r17;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:66:?, code lost:
            r8.attribute(r0, "packageName", r4);
            r10 = r15.size();
            r16 = 0;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:67:0x0199, code lost:
            r0 = r16;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:68:0x019d, code lost:
            if (r0 >= r10) goto L_0x0210;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:69:0x019f, code lost:
            r20 = r15.valueAt(r0);
            r21 = r4;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:70:0x01ac, code lost:
            r22 = r5;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:72:?, code lost:
            r8.startTag(null, com.android.server.pm.Settings.TAG_PERMISSION_ENTRY);
            r23 = r7;
            r5 = r20;
            r24 = r10;
            r8.attribute(null, com.android.server.pm.Settings.ATTR_NAME, r5.permissionName);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:73:0x01c3, code lost:
            if (r5.granted == false) goto L_0x01ce;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:74:0x01c5, code lost:
            r8.attribute(null, com.android.server.pm.Settings.ATTR_GRANTED, "true");
         */
        /* JADX WARNING: Code restructure failed: missing block: B:76:0x01d2, code lost:
            if ((r5.grantBits & 1) == 0) goto L_0x01de;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:77:0x01d4, code lost:
            r8.attribute(null, com.android.server.pm.Settings.ATTR_USER_SET, "true");
         */
        /* JADX WARNING: Code restructure failed: missing block: B:79:0x01e2, code lost:
            if ((r5.grantBits & 2) == 0) goto L_0x01ed;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:80:0x01e4, code lost:
            r8.attribute(null, com.android.server.pm.Settings.ATTR_USER_FIXED, "true");
         */
        /* JADX WARNING: Code restructure failed: missing block: B:82:0x01f1, code lost:
            if ((r5.grantBits & 8) == 0) goto L_0x01fd;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:83:0x01f3, code lost:
            r8.attribute(null, com.android.server.pm.Settings.ATTR_REVOKE_ON_UPGRADE, "true");
         */
        /* JADX WARNING: Code restructure failed: missing block: B:84:0x01fd, code lost:
            r8.endTag(null, com.android.server.pm.Settings.TAG_PERMISSION_ENTRY);
            r16 = r0 + 1;
            r4 = r21;
            r5 = r22;
            r7 = r23;
            r10 = r24;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:85:0x0210, code lost:
            r21 = r4;
            r22 = r5;
            r23 = r7;
            r24 = r10;
            r4 = null;
            r8.endTag(null, com.android.server.pm.Settings.TAG_RESTORED_RUNTIME_PERMISSIONS);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:86:0x0220, code lost:
            r0 = th;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:87:0x0221, code lost:
            r22 = r5;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:88:0x0225, code lost:
            r0 = th;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:89:0x0226, code lost:
            r22 = r5;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:90:0x0229, code lost:
            r18 = r4;
            r22 = r5;
            r23 = r7;
            r4 = r0;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:91:0x0230, code lost:
            r14 = r14 + 1;
            r0 = r4;
            r4 = r18;
            r5 = r22;
            r7 = r23;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:92:0x023c, code lost:
            r18 = r4;
            r22 = r5;
            r8.endDocument();
            r3.finishWrite(r6);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:93:0x024c, code lost:
            if (android.os.Build.FINGERPRINT.equals(r9) == false) goto L_0x0295;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:94:0x024e, code lost:
            android.util.Slog.i("PackageManager", "writePermissionsSync -> user:" + r2 + ", put true:, Build.FINGERPRINT:" + android.os.Build.FINGERPRINT + ", fingerprint:" + r9);
            r1.mDefaultPermissionsGranted.put(r2, true);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:95:0x027e, code lost:
            r0 = th;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:96:0x0280, code lost:
            r0 = th;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:97:0x0281, code lost:
            r18 = r4;
            r22 = r5;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:98:0x0286, code lost:
            r0 = th;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:99:0x0287, code lost:
            r18 = r4;
            r22 = r5;
         */
        public void writePermissionsSync(int userId) {
            int i = userId;
            AtomicFile destination = new AtomicFile(Settings.this.getUserRuntimePermissionsFile(i), "package-perms-" + i);
            ArrayMap<String, List<PermissionsState.PermissionState>> permissionsForPackage = new ArrayMap<>();
            ArrayMap<String, List<PermissionsState.PermissionState>> permissionsForSharedUser = new ArrayMap<>();
            synchronized (this.mPersistenceLock) {
                try {
                    this.mWriteScheduled.delete(i);
                    int packageCount = Settings.this.mPackages.size();
                    int i2 = 0;
                    while (i2 < packageCount) {
                        try {
                            String packageName = Settings.this.mPackages.keyAt(i2);
                            PackageSetting packageSetting = Settings.this.mPackages.valueAt(i2);
                            if (packageSetting.sharedUser == null) {
                                List<PermissionsState.PermissionState> permissionsStates = packageSetting.getPermissionsState().getRuntimePermissionStates(i);
                                if (!permissionsStates.isEmpty()) {
                                    permissionsForPackage.put(packageName, permissionsStates);
                                }
                            }
                            i2++;
                        } catch (Throwable th) {
                            th = th;
                            ArrayMap<String, List<PermissionsState.PermissionState>> arrayMap = permissionsForPackage;
                            ArrayMap<String, List<PermissionsState.PermissionState>> arrayMap2 = permissionsForSharedUser;
                            while (true) {
                                try {
                                    break;
                                } catch (Throwable th2) {
                                    th = th2;
                                }
                            }
                            throw th;
                        }
                    }
                    int sharedUserCount = Settings.this.mSharedUsers.size();
                    for (int i3 = 0; i3 < sharedUserCount; i3++) {
                        String sharedUserName = Settings.this.mSharedUsers.keyAt(i3);
                        List<PermissionsState.PermissionState> permissionsStates2 = Settings.this.mSharedUsers.valueAt(i3).getPermissionsState().getRuntimePermissionStates(i);
                        if (!permissionsStates2.isEmpty()) {
                            permissionsForSharedUser.put(sharedUserName, permissionsStates2);
                        }
                    }
                } catch (Throwable th3) {
                    th = th3;
                    ArrayMap<String, List<PermissionsState.PermissionState>> arrayMap3 = permissionsForPackage;
                    ArrayMap<String, List<PermissionsState.PermissionState>> arrayMap4 = permissionsForSharedUser;
                    while (true) {
                        break;
                    }
                    throw th;
                }
            }
        }

        /* access modifiers changed from: private */
        public void onUserRemovedLPw(int userId) {
            this.mHandler.removeMessages(userId);
            for (PackageSetting sb : Settings.this.mPackages.values()) {
                revokeRuntimePermissionsAndClearFlags(sb, userId);
            }
            for (SharedUserSetting sb2 : Settings.this.mSharedUsers.values()) {
                revokeRuntimePermissionsAndClearFlags(sb2, userId);
            }
            this.mDefaultPermissionsGranted.delete(userId);
            this.mFingerprints.remove(userId);
        }

        private void revokeRuntimePermissionsAndClearFlags(SettingBase sb, int userId) {
            PermissionsState permissionsState = sb.getPermissionsState();
            for (PermissionsState.PermissionState permissionState : permissionsState.getRuntimePermissionStates(userId)) {
                BasePermission bp = Settings.this.mPermissions.getPermission(permissionState.getName());
                if (bp != null) {
                    permissionsState.revokeRuntimePermission(bp, userId);
                    permissionsState.updatePermissionFlags(bp, userId, 255, 0);
                }
            }
        }

        public void deleteUserRuntimePermissionsFile(int userId) {
            Settings.this.getUserRuntimePermissionsFile(userId).delete();
        }

        public void readStateForUserSyncLPr(int userId) {
            File permissionsFile = Settings.this.getUserRuntimePermissionsFile(userId);
            if (permissionsFile.exists()) {
                try {
                    FileInputStream in = new AtomicFile(permissionsFile).openRead();
                    try {
                        XmlPullParser parser = Xml.newPullParser();
                        parser.setInput(in, null);
                        parseRuntimePermissionsLPr(parser, userId);
                        IoUtils.closeQuietly(in);
                    } catch (IOException | XmlPullParserException e) {
                        throw new IllegalStateException("Failed parsing permissions file: " + permissionsFile, e);
                    } catch (Throwable th) {
                        IoUtils.closeQuietly(in);
                        throw th;
                    }
                } catch (FileNotFoundException e2) {
                    Slog.i("PackageManager", "No permissions state");
                }
            }
        }

        public void rememberRestoredUserGrantLPr(String pkgName, String permission, boolean isGranted, int restoredFlagSet, int userId) {
            ArrayMap<String, ArraySet<RestoredPermissionGrant>> grantsByPackage = (ArrayMap) Settings.this.mRestoredUserGrants.get(userId);
            if (grantsByPackage == null) {
                grantsByPackage = new ArrayMap<>();
                Settings.this.mRestoredUserGrants.put(userId, grantsByPackage);
            }
            ArraySet<RestoredPermissionGrant> grants = grantsByPackage.get(pkgName);
            if (grants == null) {
                grants = new ArraySet<>();
                grantsByPackage.put(pkgName, grants);
            }
            grants.add(new RestoredPermissionGrant(permission, isGranted, restoredFlagSet));
        }

        /* JADX WARNING: Code restructure failed: missing block: B:25:0x0056, code lost:
            if (r4.equals("pkg") != false) goto L_0x0065;
         */
        /* JADX WARNING: Removed duplicated region for block: B:32:0x006b  */
        /* JADX WARNING: Removed duplicated region for block: B:33:0x0077  */
        /* JADX WARNING: Removed duplicated region for block: B:37:0x00ae  */
        /* JADX WARNING: Removed duplicated region for block: B:41:0x00e4  */
        /* JADX WARNING: Removed duplicated region for block: B:47:0x0004 A[SYNTHETIC] */
        private void parseRuntimePermissionsLPr(XmlPullParser parser, int userId) throws IOException, XmlPullParserException {
            boolean defaultsGranted;
            int outerDepth = parser.getDepth();
            while (true) {
                int next = parser.next();
                int type = next;
                char c = 1;
                if (next == 1) {
                    return;
                }
                if (type != 3 || parser.getDepth() > outerDepth) {
                    if (!(type == 3 || type == 4)) {
                        String name = parser.getName();
                        int hashCode = name.hashCode();
                        if (hashCode == -2044791156) {
                            if (name.equals(Settings.TAG_RESTORED_RUNTIME_PERMISSIONS)) {
                                c = 3;
                                switch (c) {
                                    case 0:
                                        break;
                                    case 1:
                                        break;
                                    case 2:
                                        break;
                                    case 3:
                                        break;
                                }
                            }
                        } else if (hashCode != 111052) {
                            if (hashCode == 160289295) {
                                if (name.equals(Settings.TAG_RUNTIME_PERMISSIONS)) {
                                    c = 0;
                                    switch (c) {
                                        case 0:
                                            break;
                                        case 1:
                                            break;
                                        case 2:
                                            break;
                                        case 3:
                                            break;
                                    }
                                }
                            } else if (hashCode == 485578803 && name.equals(Settings.TAG_SHARED_USER)) {
                                c = 2;
                                switch (c) {
                                    case 0:
                                        String fingerprint = parser.getAttributeValue(null, Settings.ATTR_FINGERPRINT);
                                        this.mFingerprints.put(userId, fingerprint);
                                        Slog.i("PackageManager", "parseRuntimePermissionsLPr-> user:" + userId + ", put " + defaultsGranted + " ,Build.FINGERPRINT:" + Build.FINGERPRINT + ",fingerprint:" + fingerprint);
                                        this.mDefaultPermissionsGranted.put(userId, defaultsGranted);
                                        break;
                                    case 1:
                                        PackageSetting ps = Settings.this.mPackages.get(parser.getAttributeValue(null, Settings.ATTR_NAME));
                                        if (ps != null) {
                                            parsePermissionsLPr(parser, ps.getPermissionsState(), userId);
                                            break;
                                        } else {
                                            Slog.w("PackageManager", "Unknown package:" + name);
                                            XmlUtils.skipCurrentTag(parser);
                                            break;
                                        }
                                    case 2:
                                        SharedUserSetting sus = Settings.this.mSharedUsers.get(parser.getAttributeValue(null, Settings.ATTR_NAME));
                                        if (sus != null) {
                                            parsePermissionsLPr(parser, sus.getPermissionsState(), userId);
                                            break;
                                        } else {
                                            Slog.w("PackageManager", "Unknown shared user:" + name);
                                            XmlUtils.skipCurrentTag(parser);
                                            break;
                                        }
                                    case 3:
                                        parseRestoredRuntimePermissionsLPr(parser, parser.getAttributeValue(null, "packageName"), userId);
                                        break;
                                }
                            }
                        }
                        c = 65535;
                        switch (c) {
                            case 0:
                                break;
                            case 1:
                                break;
                            case 2:
                                break;
                            case 3:
                                break;
                        }
                    }
                } else {
                    return;
                }
            }
        }

        private void parseRestoredRuntimePermissionsLPr(XmlPullParser parser, String pkgName, int userId) throws IOException, XmlPullParserException {
            int permBits;
            int outerDepth = parser.getDepth();
            while (true) {
                int next = parser.next();
                int type = next;
                if (next == 1) {
                    return;
                }
                if (type == 3 && parser.getDepth() <= outerDepth) {
                    return;
                }
                if (!(type == 3 || type == 4)) {
                    String name = parser.getName();
                    char c = 65535;
                    if (name.hashCode() == 3437296 && name.equals(Settings.TAG_PERMISSION_ENTRY)) {
                        c = 0;
                    }
                    if (c == 0) {
                        String permName = parser.getAttributeValue(null, Settings.ATTR_NAME);
                        boolean isGranted = "true".equals(parser.getAttributeValue(null, Settings.ATTR_GRANTED));
                        int permBits2 = 0;
                        if ("true".equals(parser.getAttributeValue(null, Settings.ATTR_USER_SET))) {
                            permBits2 = 0 | 1;
                        }
                        if ("true".equals(parser.getAttributeValue(null, Settings.ATTR_USER_FIXED))) {
                            permBits2 |= 2;
                        }
                        if ("true".equals(parser.getAttributeValue(null, Settings.ATTR_REVOKE_ON_UPGRADE))) {
                            permBits = permBits2 | 8;
                        } else {
                            permBits = permBits2;
                        }
                        if (isGranted || permBits != 0) {
                            rememberRestoredUserGrantLPr(pkgName, permName, isGranted, permBits, userId);
                        }
                    }
                }
            }
        }

        private void parsePermissionsLPr(XmlPullParser parser, PermissionsState permissionsState, int userId) throws IOException, XmlPullParserException {
            int outerDepth = parser.getDepth();
            while (true) {
                int next = parser.next();
                int type = next;
                boolean granted = true;
                if (next == 1) {
                    return;
                }
                if (type == 3 && parser.getDepth() <= outerDepth) {
                    return;
                }
                if (!(type == 3 || type == 4)) {
                    String name = parser.getName();
                    char c = 65535;
                    int flags = 0;
                    if (name.hashCode() == 3242771 && name.equals(Settings.TAG_ITEM)) {
                        c = 0;
                    }
                    if (c == 0) {
                        BasePermission bp = Settings.this.mPermissions.getPermission(parser.getAttributeValue(null, Settings.ATTR_NAME));
                        if (bp == null) {
                            Slog.w("PackageManager", "Unknown permission:" + name);
                            XmlUtils.skipCurrentTag(parser);
                        } else {
                            String grantedStr = parser.getAttributeValue(null, Settings.ATTR_GRANTED);
                            if (grantedStr != null && !Boolean.parseBoolean(grantedStr)) {
                                granted = false;
                            }
                            String flagsStr = parser.getAttributeValue(null, Settings.ATTR_FLAGS);
                            if (flagsStr != null) {
                                flags = Integer.parseInt(flagsStr, 16);
                            }
                            if (granted) {
                                permissionsState.grantRuntimePermission(bp, userId);
                                permissionsState.updatePermissionFlags(bp, userId, 255, flags);
                            } else {
                                permissionsState.updatePermissionFlags(bp, userId, 255, flags);
                            }
                        }
                    }
                }
            }
        }

        private void writePermissions(XmlSerializer serializer, List<PermissionsState.PermissionState> permissionStates) throws IOException {
            for (PermissionsState.PermissionState permissionState : permissionStates) {
                serializer.startTag(null, Settings.TAG_ITEM);
                serializer.attribute(null, Settings.ATTR_NAME, permissionState.getName());
                serializer.attribute(null, Settings.ATTR_GRANTED, String.valueOf(permissionState.isGranted()));
                serializer.attribute(null, Settings.ATTR_FLAGS, Integer.toHexString(permissionState.getFlags()));
                serializer.endTag(null, Settings.TAG_ITEM);
            }
        }
    }

    public static class VersionInfo {
        int databaseVersion;
        String fingerprint;
        String hwFingerprint;
        int sdkVersion;

        public void forceCurrent() {
            this.sdkVersion = Build.VERSION.SDK_INT;
            this.databaseVersion = 3;
            this.fingerprint = Build.FINGERPRINT;
            this.hwFingerprint = Build.HWFINGERPRINT;
        }
    }

    Settings(PermissionSettings permissions, Object lock) {
        this(Environment.getDataDirectory(), permissions, lock);
    }

    Settings(File dataDir, PermissionSettings permission, Object lock) {
        this.mPackages = new ArrayMap<>();
        this.mInstallerPackages = new ArraySet<>();
        this.mKernelMapping = new ArrayMap<>();
        this.mDisabledSysPackages = new ArrayMap<>();
        this.mBlockUninstallPackages = new SparseArray<>();
        this.mRestoredIntentFilterVerifications = new ArrayMap<>();
        this.mCustSettings = (HwCustSettings) HwCustUtils.createObj(HwCustSettings.class, new Object[0]);
        this.mRestoredUserGrants = new SparseArray<>();
        this.mVersion = new ArrayMap<>();
        this.mPreferredActivities = new SparseArray<>();
        this.mPersistentPreferredActivities = new SparseArray<>();
        this.mCrossProfileIntentResolvers = new SparseArray<>();
        this.mSharedUsers = new ArrayMap<>();
        this.mUserIds = new ArrayList<>();
        this.mOtherUserIds = new SparseArray<>();
        this.mPastSignatures = new ArrayList<>();
        this.mKeySetRefs = new ArrayMap<>();
        this.mPackagesToBeCleaned = new ArrayList<>();
        this.mRenamedPackages = new ArrayMap<>();
        this.mDefaultBrowserApp = new SparseArray<>();
        this.mDefaultDialerApp = new SparseArray<>();
        this.mNextAppLinkGeneration = new SparseIntArray();
        this.mReadMessages = new StringBuilder();
        this.mPendingPackages = new ArrayList<>();
        this.mKeySetManagerService = new KeySetManagerService(this.mPackages);
        this.mIsPackageSettingsError = false;
        this.isNeedRetryNewUserId = true;
        this.mDelAppLists = new ArrayList<>();
        this.mLock = lock;
        this.mPermissions = permission;
        this.mRuntimePermissionsPersistence = new RuntimePermissionPersistence(this.mLock);
        this.mSystemDir = new File(dataDir, "system");
        this.mSystemDir.mkdirs();
        FileUtils.setPermissions(this.mSystemDir.toString(), 509, -1, -1);
        this.mSettingsFilename = new File(this.mSystemDir, "packages.xml");
        this.mBackupSettingsFilename = new File(this.mSystemDir, "packages-backup.xml");
        this.mPackageListFilename = new File(this.mSystemDir, "packages.list");
        FileUtils.setPermissions(this.mPackageListFilename, 416, 1000, 1032);
        File kernelDir = new File("/config/sdcardfs");
        this.mKernelMappingFilename = kernelDir.exists() ? kernelDir : null;
        this.mStoppedPackagesFilename = new File(this.mSystemDir, "packages-stopped.xml");
        this.mBackupStoppedPackagesFilename = new File(this.mSystemDir, "packages-stopped-backup.xml");
    }

    /* access modifiers changed from: package-private */
    public PackageSetting getPackageLPr(String pkgName) {
        return this.mPackages.get(pkgName);
    }

    /* access modifiers changed from: package-private */
    public String getRenamedPackageLPr(String pkgName) {
        return this.mRenamedPackages.get(pkgName);
    }

    /* access modifiers changed from: package-private */
    public String addRenamedPackageLPw(String pkgName, String origPkgName) {
        return this.mRenamedPackages.put(pkgName, origPkgName);
    }

    /* access modifiers changed from: package-private */
    public void applyPendingPermissionGrantsLPw(String packageName, int userId) {
        ArrayMap<String, ArraySet<RestoredPermissionGrant>> grantsByPackage = this.mRestoredUserGrants.get(userId);
        if (grantsByPackage != null && grantsByPackage.size() != 0) {
            ArraySet<RestoredPermissionGrant> grants = grantsByPackage.get(packageName);
            if (grants != null && grants.size() != 0) {
                PackageSetting ps = this.mPackages.get(packageName);
                if (ps == null) {
                    Slog.e(TAG, "Can't find supposedly installed package " + packageName);
                    return;
                }
                PermissionsState perms = ps.getPermissionsState();
                Iterator<RestoredPermissionGrant> it = grants.iterator();
                while (it.hasNext()) {
                    RestoredPermissionGrant grant = it.next();
                    BasePermission bp = this.mPermissions.getPermission(grant.permissionName);
                    if (bp != null) {
                        if (grant.granted) {
                            perms.grantRuntimePermission(bp, userId);
                        }
                        perms.updatePermissionFlags(bp, userId, 11, grant.grantBits);
                    }
                }
                grantsByPackage.remove(packageName);
                if (grantsByPackage.size() < 1) {
                    this.mRestoredUserGrants.remove(userId);
                }
                writeRuntimePermissionsForUserLPr(userId, false);
            }
        }
    }

    public boolean canPropagatePermissionToInstantApp(String permName) {
        return this.mPermissions.canPropagatePermissionToInstantApp(permName);
    }

    /* access modifiers changed from: package-private */
    public void setInstallerPackageName(String pkgName, String installerPkgName) {
        PackageSetting p = this.mPackages.get(pkgName);
        if (p != null) {
            p.setInstallerPackageName(installerPkgName);
            if (installerPkgName != null) {
                this.mInstallerPackages.add(installerPkgName);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public SharedUserSetting getSharedUserLPw(String name, int pkgFlags, int pkgPrivateFlags, boolean create) throws PackageManagerException {
        SharedUserSetting s = this.mSharedUsers.get(name);
        if (s == null && create) {
            s = new SharedUserSetting(name, pkgFlags, pkgPrivateFlags);
            s.userId = newUserIdLPw(s);
            if (s.userId >= 0) {
                Log.i("PackageManager", "New shared user " + name + ": id=" + s.userId);
                this.mSharedUsers.put(name, s);
            } else {
                throw new PackageManagerException(-4, "Creating shared user " + name + " failed");
            }
        }
        return s;
    }

    /* access modifiers changed from: package-private */
    public Collection<SharedUserSetting> getAllSharedUsersLPw() {
        return this.mSharedUsers.values();
    }

    /* access modifiers changed from: package-private */
    public String getDisabledSysPackagesPath(String name) {
        PackageSetting dp = this.mDisabledSysPackages.get(name);
        if (dp == null) {
            return null;
        }
        return dp.codePathString;
    }

    /* access modifiers changed from: package-private */
    public boolean disableSystemPackageLPw(String name) {
        return disableSystemPackageLPw(name, true);
    }

    /* access modifiers changed from: package-private */
    public boolean disableSystemPackageLPw(String name, boolean replaced) {
        PackageSetting p = this.mPackages.get(name);
        if (p == null) {
            Log.w("PackageManager", "Package " + name + " is not an installed package");
            return false;
        } else if (this.mDisabledSysPackages.get(name) != null || p.pkg == null || !p.pkg.isSystem() || p.pkg.isUpdatedSystemApp()) {
            return false;
        } else {
            if (!(p.pkg == null || p.pkg.applicationInfo == null)) {
                p.pkg.applicationInfo.flags |= 128;
            }
            this.mDisabledSysPackages.put(name, p);
            if (replaced) {
                replacePackageLPw(name, new PackageSetting(p));
            }
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public PackageSetting enableSystemPackageLPw(String name) {
        String str = name;
        PackageSetting p = this.mDisabledSysPackages.get(str);
        if (p == null) {
            Log.w("PackageManager", "Package " + str + " is not disabled");
            return null;
        }
        if (!(p.pkg == null || p.pkg.applicationInfo == null)) {
            p.pkg.applicationInfo.flags &= -129;
        }
        String str2 = p.realName;
        File file = p.codePath;
        File file2 = p.resourcePath;
        String str3 = p.legacyNativeLibraryPathString;
        String str4 = p.primaryCpuAbiString;
        String str5 = p.secondaryCpuAbiString;
        String str6 = p.cpuAbiOverrideString;
        int i = p.appId;
        long j = p.versionCode;
        int i2 = p.pkgFlags;
        int i3 = p.pkgPrivateFlags;
        int i4 = i3;
        int i5 = i2;
        PackageSetting packageSetting = p;
        PackageSetting ret = addPackageLPw(str, str2, file, file2, str3, str4, str5, str6, i, j, i5, i4, p.parentPackageName, p.childPackageNames, p.usesStaticLibraries, p.usesStaticLibrariesVersions);
        this.mDisabledSysPackages.remove(name);
        return ret;
    }

    /* access modifiers changed from: package-private */
    public String getDisabledSystemPackageName(String filePath) {
        if (filePath == null) {
            Log.e(TAG, "getDisabledSystemPackageName, error");
            return null;
        }
        for (PackageSetting pkg : this.mDisabledSysPackages.values()) {
            if (filePath.equals(pkg.codePathString)) {
                Log.i(TAG, "getDisabledSystemPackageName " + filePath + ", pkg.name " + pkg.name);
                return pkg.name;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public boolean isDisabledSystemPackageLPr(String name) {
        return this.mDisabledSysPackages.containsKey(name);
    }

    /* access modifiers changed from: package-private */
    public void removeDisabledSystemPackageLPw(String name) {
        this.mDisabledSysPackages.remove(name);
    }

    /* access modifiers changed from: package-private */
    public PackageSetting addPackageLPw(String name, String realName, File codePath, File resourcePath, String legacyNativeLibraryPathString, String primaryCpuAbiString, String secondaryCpuAbiString, String cpuAbiOverrideString, int uid, long vc, int pkgFlags, int pkgPrivateFlags, String parentPackageName, List<String> childPackageNames, String[] usesStaticLibraries, long[] usesStaticLibraryNames) {
        String str = name;
        int i = uid;
        PackageSetting p = this.mPackages.get(str);
        if (p == null) {
            PackageSetting packageSetting = p;
            int i2 = i;
            PackageSetting p2 = new PackageSetting(str, realName, codePath, resourcePath, legacyNativeLibraryPathString, primaryCpuAbiString, secondaryCpuAbiString, cpuAbiOverrideString, vc, pkgFlags, pkgPrivateFlags, parentPackageName, childPackageNames, 0, usesStaticLibraries, usesStaticLibraryNames);
            p2.appId = i2;
            int i3 = i2;
            String str2 = name;
            if (!addUserIdLPw(i3, p2, str2)) {
                return null;
            }
            this.mPackages.put(str2, p2);
            return p2;
        } else if (p.appId == i) {
            return p;
        } else {
            PackageManagerService.reportSettingsProblem(6, "Adding duplicate package, keeping first: " + str);
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public void addAppOpPackage(String permName, String packageName) {
        this.mPermissions.addAppOpPackage(permName, packageName);
    }

    /* access modifiers changed from: package-private */
    public SharedUserSetting addSharedUserLPw(String name, int uid, int pkgFlags, int pkgPrivateFlags) {
        SharedUserSetting s = this.mSharedUsers.get(name);
        if (s == null) {
            SharedUserSetting s2 = new SharedUserSetting(name, pkgFlags, pkgPrivateFlags);
            s2.userId = uid;
            if (!addUserIdLPw(uid, s2, name)) {
                return null;
            }
            this.mSharedUsers.put(name, s2);
            return s2;
        } else if (s.userId == uid) {
            return s;
        } else {
            PackageManagerService.reportSettingsProblem(6, "Adding duplicate shared user, keeping first: " + name);
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public void pruneSharedUsersLPw() {
        ArrayList<String> removeStage = new ArrayList<>();
        for (Map.Entry<String, SharedUserSetting> entry : this.mSharedUsers.entrySet()) {
            SharedUserSetting sus = entry.getValue();
            if (sus == null) {
                removeStage.add(entry.getKey());
            } else {
                Iterator<PackageSetting> iter = sus.packages.iterator();
                while (iter.hasNext()) {
                    if (this.mPackages.get(iter.next().name) == null) {
                        iter.remove();
                    }
                }
                if (sus.packages.size() == 0) {
                    removeStage.add(entry.getKey());
                }
            }
        }
        for (int i = 0; i < removeStage.size(); i++) {
            this.mSharedUsers.remove(removeStage.get(i));
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x00db, code lost:
        if (com.android.server.HwServiceFactory.isCustedCouldStopped(r43, false, false) != false) goto L_0x00e5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0113, code lost:
        if (isAdbInstallDisallowed(r63, r8.id) != false) goto L_0x0118;
     */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x012e  */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x0131  */
    static PackageSetting createNewSetting(String pkgName, PackageSetting originalPkg, PackageSetting disabledPkg, String realPkgName, SharedUserSetting sharedUser, File codePath, File resourcePath, String legacyNativeLibraryPath, String primaryCpuAbi, String secondaryCpuAbi, long versionCode, int pkgFlags, int pkgPrivateFlags, UserHandle installUser, boolean allowInstall, boolean instantApp, boolean virtualPreload, String parentPkgName, List<String> childPkgNames, UserManagerService userManager, String[] usesStaticLibraries, long[] usesStaticLibrariesVersions) {
        int i;
        PackageSetting packageSetting;
        boolean z;
        PackageSetting pkgSetting;
        boolean installed;
        String str = pkgName;
        PackageSetting packageSetting2 = originalPkg;
        PackageSetting packageSetting3 = disabledPkg;
        SharedUserSetting sharedUserSetting = sharedUser;
        int i2 = pkgFlags;
        List<String> list = childPkgNames;
        if (packageSetting2 != null) {
            if (PackageManagerService.DEBUG_UPGRADE) {
                Log.v("PackageManager", "Package " + str + " is adopting original package " + packageSetting2.name);
            }
            pkgSetting = new PackageSetting(packageSetting2, str);
            pkgSetting.childPackageNames = list != null ? new ArrayList(list) : null;
            pkgSetting.codePath = codePath;
            pkgSetting.legacyNativeLibraryPathString = legacyNativeLibraryPath;
            pkgSetting.parentPackageName = parentPkgName;
            pkgSetting.pkgFlags = i2;
            pkgSetting.pkgPrivateFlags = pkgPrivateFlags;
            pkgSetting.primaryCpuAbiString = primaryCpuAbi;
            pkgSetting.resourcePath = resourcePath;
            pkgSetting.secondaryCpuAbiString = secondaryCpuAbi;
            pkgSetting.signatures = new PackageSignatures();
            pkgSetting.versionCode = versionCode;
            pkgSetting.usesStaticLibraries = usesStaticLibraries;
            pkgSetting.usesStaticLibrariesVersions = usesStaticLibrariesVersions;
            pkgSetting.setTimeStamp(codePath.lastModified());
            UserManagerService userManagerService = userManager;
            String str2 = str;
            SharedUserSetting sharedUserSetting2 = sharedUser;
            z = false;
            packageSetting = disabledPkg;
            i = pkgFlags;
        } else {
            File file = resourcePath;
            String str3 = primaryCpuAbi;
            String str4 = secondaryCpuAbi;
            int i3 = pkgPrivateFlags;
            String str5 = parentPkgName;
            String[] strArr = usesStaticLibraries;
            long[] jArr = usesStaticLibrariesVersions;
            i = pkgFlags;
            PackageSetting packageSetting4 = disabledPkg;
            pkgSetting = new PackageSetting(str, realPkgName, codePath, resourcePath, legacyNativeLibraryPath, primaryCpuAbi, secondaryCpuAbi, null, versionCode, i, pkgPrivateFlags, parentPkgName, childPkgNames, 0, usesStaticLibraries, usesStaticLibrariesVersions);
            pkgSetting.setTimeStamp(codePath.lastModified());
            SharedUserSetting sharedUserSetting3 = sharedUser;
            pkgSetting.sharedUser = sharedUserSetting3;
            if ((i & 1) != 0) {
                z = false;
            } else {
                String str6 = pkgName;
                z = false;
                List<UserInfo> users = getAllUsers(userManager);
                int installUserId = installUser != null ? installUser.getIdentifier() : z;
                if (users != null && allowInstall) {
                    Iterator<UserInfo> it = users.iterator();
                    while (it.hasNext()) {
                        UserInfo user = it.next();
                        if (installUser != null) {
                            if (installUserId != -1) {
                                UserManagerService userManagerService2 = userManager;
                            }
                            if (installUserId != user.id) {
                                installed = z;
                                pkgSetting.setUserState(user.id, 0, 0, !user.isClonedProfile() ? z : installed, true, true, false, false, null, null, null, null, instantApp, virtualPreload, null, null, null, 0, 0, 0, null);
                            }
                        } else {
                            UserManagerService userManagerService3 = userManager;
                        }
                        installed = true;
                        pkgSetting.setUserState(user.id, 0, 0, !user.isClonedProfile() ? z : installed, true, true, false, false, null, null, null, null, instantApp, virtualPreload, null, null, null, 0, 0, 0, null);
                    }
                }
            }
            UserManagerService userManagerService4 = userManager;
            if (sharedUserSetting3 != null) {
                pkgSetting.appId = sharedUserSetting3.userId;
                packageSetting = disabledPkg;
            } else {
                packageSetting = disabledPkg;
                if (packageSetting != null) {
                    pkgSetting.signatures = new PackageSignatures(packageSetting.signatures);
                    pkgSetting.appId = packageSetting.appId;
                    pkgSetting.getPermissionsState().copyFrom(disabledPkg.getPermissionsState());
                    List<UserInfo> users2 = getAllUsers(userManager);
                    if (users2 != null) {
                        for (UserInfo user2 : users2) {
                            int userId = user2.id;
                            pkgSetting.setDisabledComponentsCopy(packageSetting.getDisabledComponents(userId), userId);
                            pkgSetting.setEnabledComponentsCopy(packageSetting.getEnabledComponents(userId), userId);
                        }
                    }
                }
            }
        }
        if ((i & 1) != 0 && packageSetting == null) {
            List<UserInfo> users3 = getAllUsers(userManager);
            if (users3 != null) {
                Iterator<UserInfo> it2 = users3.iterator();
                while (true) {
                    if (!it2.hasNext()) {
                        break;
                    }
                    UserInfo userInfo = it2.next();
                    if (userInfo.isClonedProfile()) {
                        pkgSetting.setInstalled(z, userInfo.id);
                        break;
                    }
                }
            }
        }
        return pkgSetting;
    }

    static void updatePackageSetting(PackageSetting pkgSetting, PackageSetting disabledPkg, SharedUserSetting sharedUser, File codePath, File resourcePath, String legacyNativeLibraryPath, String primaryCpuAbi, String secondaryCpuAbi, int pkgFlags, int pkgPrivateFlags, List<String> childPkgNames, UserManagerService userManager, String[] usesStaticLibraries, long[] usesStaticLibrariesVersions) throws PackageManagerException {
        PackageSetting packageSetting = pkgSetting;
        SharedUserSetting sharedUserSetting = sharedUser;
        File file = codePath;
        File file2 = resourcePath;
        List<String> list = childPkgNames;
        String[] strArr = usesStaticLibraries;
        long[] jArr = usesStaticLibrariesVersions;
        String pkgName = packageSetting.name;
        if (packageSetting.sharedUser != sharedUserSetting) {
            StringBuilder sb = new StringBuilder();
            sb.append("Package ");
            sb.append(pkgName);
            sb.append(" shared user changed from ");
            sb.append(packageSetting.sharedUser != null ? packageSetting.sharedUser.name : "<nothing>");
            sb.append(" to ");
            sb.append(sharedUserSetting != null ? sharedUserSetting.name : "<nothing>");
            PackageManagerService.reportSettingsProblem(5, sb.toString());
            throw new PackageManagerException(-8, "Updating application package " + pkgName + " failed");
        }
        if (!packageSetting.codePath.equals(file)) {
            boolean isSystem = packageSetting.isSystem();
            StringBuilder sb2 = new StringBuilder();
            sb2.append("Update");
            sb2.append(isSystem ? " system" : BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
            sb2.append(" package ");
            sb2.append(pkgName);
            sb2.append(" code path from ");
            sb2.append(packageSetting.codePathString);
            sb2.append(" to ");
            sb2.append(codePath.toString());
            sb2.append("; Retain data and using new");
            Slog.i("PackageManager", sb2.toString());
            if (!isSystem) {
                if ((pkgFlags & 1) != 0 && disabledPkg == null) {
                    List<UserInfo> allUserInfos = getAllUsers(userManager);
                    if (allUserInfos != null) {
                        for (UserInfo userInfo : allUserInfos) {
                            if (!userInfo.isClonedProfile() || packageSetting.getInstalled(userInfo.id)) {
                                packageSetting.setInstalled(true, userInfo.id);
                                SharedUserSetting sharedUserSetting2 = sharedUser;
                            }
                        }
                    }
                }
                packageSetting.legacyNativeLibraryPathString = legacyNativeLibraryPath;
            } else {
                String str = legacyNativeLibraryPath;
            }
            packageSetting.codePath = file;
            packageSetting.codePathString = codePath.toString();
        } else {
            String str2 = legacyNativeLibraryPath;
        }
        if (!packageSetting.resourcePath.equals(file2)) {
            boolean isSystem2 = packageSetting.isSystem();
            StringBuilder sb3 = new StringBuilder();
            sb3.append("Update");
            sb3.append(isSystem2 ? " system" : BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
            sb3.append(" package ");
            sb3.append(pkgName);
            sb3.append(" resource path from ");
            sb3.append(packageSetting.resourcePathString);
            sb3.append(" to ");
            sb3.append(resourcePath.toString());
            sb3.append("; Retain data and using new");
            Slog.i("PackageManager", sb3.toString());
            packageSetting.resourcePath = file2;
            packageSetting.resourcePathString = resourcePath.toString();
        }
        packageSetting.pkgFlags &= -2;
        packageSetting.pkgPrivateFlags &= -917513;
        packageSetting.pkgFlags |= pkgFlags & 1;
        packageSetting.pkgPrivateFlags |= pkgPrivateFlags & 8;
        packageSetting.pkgPrivateFlags |= pkgPrivateFlags & 131072;
        packageSetting.pkgPrivateFlags |= pkgPrivateFlags & 262144;
        packageSetting.pkgPrivateFlags |= pkgPrivateFlags & DumpState.DUMP_FROZEN;
        packageSetting.primaryCpuAbiString = primaryCpuAbi;
        packageSetting.secondaryCpuAbiString = secondaryCpuAbi;
        if (list != null) {
            packageSetting.childPackageNames = new ArrayList(list);
        }
        if (strArr == null || jArr == null || strArr.length != jArr.length) {
            packageSetting.usesStaticLibraries = null;
            packageSetting.usesStaticLibrariesVersions = null;
            return;
        }
        packageSetting.usesStaticLibraries = strArr;
        packageSetting.usesStaticLibrariesVersions = jArr;
    }

    /* access modifiers changed from: package-private */
    public void addUserToSettingLPw(PackageSetting p) throws PackageManagerException {
        if (p.appId == 0) {
            p.appId = newUserIdLPw(p);
        } else {
            addUserIdLPw(p.appId, p, p.name);
        }
        if (p.appId < 0) {
            PackageManagerService.reportSettingsProblem(5, "Package " + p.name + " could not be assigned a valid UID");
            throw new PackageManagerException(-4, "Package " + p.name + " could not be assigned a valid UID");
        }
    }

    /* access modifiers changed from: package-private */
    public void writeUserRestrictionsLPw(PackageSetting newPackage, PackageSetting oldPackage) {
        PackageUserState oldUserState;
        if (getPackageLPr(newPackage.name) != null) {
            List<UserInfo> allUsers = getAllUsers(UserManagerService.getInstance());
            if (allUsers != null) {
                for (UserInfo user : allUsers) {
                    if (oldPackage == null) {
                        oldUserState = PackageSettingBase.DEFAULT_USER_STATE;
                    } else {
                        oldUserState = oldPackage.readUserState(user.id);
                    }
                    if (!oldUserState.equals(newPackage.readUserState(user.id))) {
                        writePackageRestrictionsLPr(user.id);
                    }
                }
            }
        }
    }

    static boolean isAdbInstallDisallowed(UserManagerService userManager, int userId) {
        return userManager.hasUserRestriction("no_debugging_features", userId);
    }

    /* access modifiers changed from: package-private */
    public void insertPackageSettingLPw(PackageSetting p, PackageParser.Package pkg) {
        if (p.signatures.mSigningDetails.signatures == null) {
            p.signatures.mSigningDetails = pkg.mSigningDetails;
        }
        if (p.sharedUser != null && p.sharedUser.signatures.mSigningDetails.signatures == null) {
            p.sharedUser.signatures.mSigningDetails = pkg.mSigningDetails;
        }
        addPackageSettingLPw(p, p.sharedUser);
    }

    private void addPackageSettingLPw(PackageSetting p, SharedUserSetting sharedUser) {
        this.mPackages.put(p.name, p);
        if (sharedUser != null) {
            if (p.sharedUser != null && p.sharedUser != sharedUser) {
                PackageManagerService.reportSettingsProblem(6, "Package " + p.name + " was user " + p.sharedUser + " but is now " + sharedUser + "; I am not changing its files so it will probably fail!");
                p.sharedUser.removePackage(p);
            } else if (p.appId != sharedUser.userId) {
                PackageManagerService.reportSettingsProblem(6, "Package " + p.name + " was user id " + p.appId + " but is now user " + sharedUser + " with id " + sharedUser.userId + "; I am not changing its files so it will probably fail!");
            }
            sharedUser.addPackage(p);
            p.sharedUser = sharedUser;
            p.appId = sharedUser.userId;
        }
        Object userIdPs = getUserIdLPr(p.appId);
        if (sharedUser == null) {
            if (!(userIdPs == null || userIdPs == p)) {
                replaceUserIdLPw(p.appId, p);
            }
        } else if (!(userIdPs == null || userIdPs == sharedUser)) {
            replaceUserIdLPw(p.appId, sharedUser);
        }
        IntentFilterVerificationInfo ivi = this.mRestoredIntentFilterVerifications.get(p.name);
        if (ivi != null) {
            if (PackageManagerService.DEBUG_DOMAIN_VERIFICATION) {
                Slog.i(TAG, "Applying restored IVI for " + p.name + " : " + ivi.getStatusString());
            }
            this.mRestoredIntentFilterVerifications.remove(p.name);
            p.setIntentFilterVerificationInfo(ivi);
        }
    }

    /* access modifiers changed from: package-private */
    public int updateSharedUserPermsLPw(PackageSetting deletedPs, int userId) {
        if (deletedPs == null || deletedPs.pkg == null) {
            Slog.i("PackageManager", "Trying to update info for null package. Just ignoring");
            return -10000;
        } else if (deletedPs.sharedUser == null) {
            return -10000;
        } else {
            SharedUserSetting sus = deletedPs.sharedUser;
            Iterator it = deletedPs.pkg.requestedPermissions.iterator();
            while (it.hasNext()) {
                String eachPerm = (String) it.next();
                BasePermission bp = this.mPermissions.getPermission(eachPerm);
                if (bp != null) {
                    boolean used = false;
                    Iterator<PackageSetting> it2 = sus.packages.iterator();
                    while (true) {
                        if (!it2.hasNext()) {
                            break;
                        }
                        PackageSetting pkg = it2.next();
                        if (pkg.pkg != null && !pkg.pkg.packageName.equals(deletedPs.pkg.packageName) && pkg.pkg.requestedPermissions.contains(eachPerm)) {
                            used = true;
                            break;
                        }
                    }
                    if (used) {
                        continue;
                    } else {
                        PermissionsState permissionsState = sus.getPermissionsState();
                        PackageSetting disabledPs = getDisabledSystemPkgLPr(deletedPs.pkg.packageName);
                        if (!(disabledPs == null || disabledPs.pkg == null)) {
                            boolean reqByDisabledSysPkg = false;
                            Iterator it3 = disabledPs.pkg.requestedPermissions.iterator();
                            while (true) {
                                if (it3.hasNext()) {
                                    if (((String) it3.next()).equals(eachPerm)) {
                                        reqByDisabledSysPkg = true;
                                        break;
                                    }
                                } else {
                                    break;
                                }
                            }
                            if (reqByDisabledSysPkg) {
                                continue;
                            }
                        }
                        permissionsState.updatePermissionFlags(bp, userId, 255, 0);
                        if (permissionsState.revokeInstallPermission(bp) == 1) {
                            return -1;
                        }
                        if (permissionsState.revokeRuntimePermission(bp, userId) == 1) {
                            return userId;
                        }
                    }
                }
            }
            return -10000;
        }
    }

    /* access modifiers changed from: package-private */
    public int removePackageLPw(String name) {
        PackageSetting p = this.mPackages.get(name);
        if (p != null) {
            this.mPackages.remove(name);
            removeInstallerPackageStatus(name);
            if (p.sharedUser != null) {
                p.sharedUser.removePackage(p);
                if (p.sharedUser.packages.size() == 0) {
                    this.mSharedUsers.remove(p.sharedUser.name);
                    removeUserIdLPw(p.sharedUser.userId);
                    return p.sharedUser.userId;
                }
            } else {
                removeUserIdLPw(p.appId);
                return p.appId;
            }
        }
        return -1;
    }

    private void removeInstallerPackageStatus(String packageName) {
        if (this.mInstallerPackages.contains(packageName)) {
            for (int i = 0; i < this.mPackages.size(); i++) {
                PackageSetting ps = this.mPackages.valueAt(i);
                String installerPackageName = ps.getInstallerPackageName();
                if (installerPackageName != null && installerPackageName.equals(packageName)) {
                    ps.setInstallerPackageName(null);
                    ps.isOrphaned = true;
                }
            }
            this.mInstallerPackages.remove(packageName);
        }
    }

    private void replacePackageLPw(String name, PackageSetting newp) {
        PackageSetting p = this.mPackages.get(name);
        if (p != null) {
            if (p.sharedUser != null) {
                p.sharedUser.removePackage(p);
                p.sharedUser.addPackage(newp);
            } else {
                replaceUserIdLPw(p.appId, newp);
            }
        }
        this.mPackages.put(name, newp);
    }

    private boolean addUserIdLPw(int uid, Object obj, Object name) {
        if (uid > 19999) {
            return false;
        }
        if (uid >= 10000) {
            int index = uid - 10000;
            for (int N = this.mUserIds.size(); index >= N; N++) {
                this.mUserIds.add(null);
            }
            if (this.mUserIds.get(index) != null) {
                PackageManagerService.reportSettingsProblem(6, "Adding duplicate user id: " + uid + " name=" + name);
                return false;
            }
            this.mUserIds.set(index, obj);
        } else if (this.mOtherUserIds.get(uid) != null) {
            PackageManagerService.reportSettingsProblem(6, "Adding duplicate shared id: " + uid + " name=" + name);
            return false;
        } else {
            this.mOtherUserIds.put(uid, obj);
        }
        return true;
    }

    public Object getUserIdLPr(int uid) {
        if (uid >= 19959 && uid <= 19999) {
            int hbsUid = getHbsUid();
            uid = hbsUid != -1 ? hbsUid : uid;
        }
        if (uid < 10000) {
            return this.mOtherUserIds.get(uid);
        }
        int index = uid - 10000;
        return index < this.mUserIds.size() ? this.mUserIds.get(index) : null;
    }

    private int getHbsUid() {
        try {
            return Os.lstat("/data/data/com.huawei.hbs.framework").st_uid;
        } catch (ErrnoException e) {
            return -1;
        }
    }

    private void removeUserIdLPw(int uid) {
        if (uid >= 10000) {
            int index = uid - 10000;
            if (index < this.mUserIds.size()) {
                this.mUserIds.set(index, null);
            }
        } else {
            this.mOtherUserIds.remove(uid);
        }
        setFirstAvailableUid(uid + 1);
    }

    private void replaceUserIdLPw(int uid, Object obj) {
        if (uid >= 10000) {
            int index = uid - 10000;
            if (index < this.mUserIds.size()) {
                this.mUserIds.set(index, obj);
                return;
            }
            return;
        }
        this.mOtherUserIds.put(uid, obj);
    }

    /* access modifiers changed from: package-private */
    public PreferredIntentResolver editPreferredActivitiesLPw(int userId) {
        PreferredIntentResolver pir = this.mPreferredActivities.get(userId);
        if (pir != null) {
            return pir;
        }
        PreferredIntentResolver pir2 = new PreferredIntentResolver();
        this.mPreferredActivities.put(userId, pir2);
        return pir2;
    }

    /* access modifiers changed from: package-private */
    public PersistentPreferredIntentResolver editPersistentPreferredActivitiesLPw(int userId) {
        PersistentPreferredIntentResolver ppir = this.mPersistentPreferredActivities.get(userId);
        if (ppir != null) {
            return ppir;
        }
        PersistentPreferredIntentResolver ppir2 = new PersistentPreferredIntentResolver();
        this.mPersistentPreferredActivities.put(userId, ppir2);
        return ppir2;
    }

    /* access modifiers changed from: package-private */
    public CrossProfileIntentResolver editCrossProfileIntentResolverLPw(int userId) {
        CrossProfileIntentResolver cpir = this.mCrossProfileIntentResolvers.get(userId);
        if (cpir != null) {
            return cpir;
        }
        CrossProfileIntentResolver cpir2 = new CrossProfileIntentResolver();
        this.mCrossProfileIntentResolvers.put(userId, cpir2);
        return cpir2;
    }

    /* access modifiers changed from: package-private */
    public IntentFilterVerificationInfo getIntentFilterVerificationLPr(String packageName) {
        PackageSetting ps = this.mPackages.get(packageName);
        if (ps != null) {
            return ps.getIntentFilterVerificationInfo();
        }
        if (PackageManagerService.DEBUG_DOMAIN_VERIFICATION) {
            Slog.w("PackageManager", "No package known: " + packageName);
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public IntentFilterVerificationInfo createIntentFilterVerificationIfNeededLPw(String packageName, ArraySet<String> domains) {
        PackageSetting ps = this.mPackages.get(packageName);
        if (ps == null) {
            if (PackageManagerService.DEBUG_DOMAIN_VERIFICATION) {
                Slog.w("PackageManager", "No package known: " + packageName);
            }
            return null;
        }
        IntentFilterVerificationInfo ivi = ps.getIntentFilterVerificationInfo();
        if (ivi == null) {
            ivi = new IntentFilterVerificationInfo(packageName, domains);
            ps.setIntentFilterVerificationInfo(ivi);
            if (PackageManagerService.DEBUG_DOMAIN_VERIFICATION) {
                Slog.d("PackageManager", "Creating new IntentFilterVerificationInfo for pkg: " + packageName);
            }
        } else {
            ivi.setDomains(domains);
            if (PackageManagerService.DEBUG_DOMAIN_VERIFICATION) {
                Slog.d("PackageManager", "Setting domains to existing IntentFilterVerificationInfo for pkg: " + packageName + " and with domains: " + ivi.getDomainsString());
            }
        }
        return ivi;
    }

    /* access modifiers changed from: package-private */
    public int getIntentFilterVerificationStatusLPr(String packageName, int userId) {
        PackageSetting ps = this.mPackages.get(packageName);
        if (ps != null) {
            return (int) (ps.getDomainVerificationStatusForUser(userId) >> 32);
        }
        if (PackageManagerService.DEBUG_DOMAIN_VERIFICATION) {
            Slog.w("PackageManager", "No package known: " + packageName);
        }
        return 0;
    }

    /* access modifiers changed from: package-private */
    public boolean updateIntentFilterVerificationStatusLPw(String packageName, int status, int userId) {
        PackageSetting current = this.mPackages.get(packageName);
        int alwaysGeneration = 0;
        if (current == null) {
            if (PackageManagerService.DEBUG_DOMAIN_VERIFICATION) {
                Slog.w("PackageManager", "No package known: " + packageName);
            }
            return false;
        }
        if (status == 2) {
            alwaysGeneration = this.mNextAppLinkGeneration.get(userId) + 1;
            this.mNextAppLinkGeneration.put(userId, alwaysGeneration);
        }
        current.setDomainVerificationStatusForUser(status, alwaysGeneration, userId);
        return true;
    }

    /* access modifiers changed from: package-private */
    public List<IntentFilterVerificationInfo> getIntentFilterVerificationsLPr(String packageName) {
        if (packageName == null) {
            return Collections.emptyList();
        }
        ArrayList<IntentFilterVerificationInfo> result = new ArrayList<>();
        for (PackageSetting ps : this.mPackages.values()) {
            IntentFilterVerificationInfo ivi = ps.getIntentFilterVerificationInfo();
            if (ivi != null && !TextUtils.isEmpty(ivi.getPackageName()) && ivi.getPackageName().equalsIgnoreCase(packageName)) {
                result.add(ivi);
            }
        }
        return result;
    }

    /* access modifiers changed from: package-private */
    public boolean removeIntentFilterVerificationLPw(String packageName, int userId) {
        PackageSetting ps = this.mPackages.get(packageName);
        if (ps == null) {
            if (PackageManagerService.DEBUG_DOMAIN_VERIFICATION) {
                Slog.w("PackageManager", "No package known: " + packageName);
            }
            return false;
        }
        ps.clearDomainVerificationStatusForUser(userId);
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean removeIntentFilterVerificationLPw(String packageName, int[] userIds) {
        boolean result = false;
        for (int userId : userIds) {
            result |= removeIntentFilterVerificationLPw(packageName, userId);
        }
        return result;
    }

    /* access modifiers changed from: package-private */
    public boolean setDefaultBrowserPackageNameLPw(String packageName, int userId) {
        if (userId == -1) {
            return false;
        }
        if (packageName != null) {
            this.mDefaultBrowserApp.put(userId, packageName);
        } else {
            this.mDefaultBrowserApp.remove(userId);
        }
        writePackageRestrictionsLPr(userId);
        return true;
    }

    /* access modifiers changed from: package-private */
    public String getDefaultBrowserPackageNameLPw(int userId) {
        if (userId == -1) {
            return null;
        }
        return this.mDefaultBrowserApp.get(userId);
    }

    /* access modifiers changed from: package-private */
    public boolean setDefaultDialerPackageNameLPw(String packageName, int userId) {
        if (userId == -1) {
            return false;
        }
        this.mDefaultDialerApp.put(userId, packageName);
        writePackageRestrictionsLPr(userId);
        return true;
    }

    /* access modifiers changed from: package-private */
    public String getDefaultDialerPackageNameLPw(int userId) {
        if (userId == -1) {
            return null;
        }
        return this.mDefaultDialerApp.get(userId);
    }

    private File getUserPackagesStateFile(int userId) {
        return new File(new File(new File(this.mSystemDir, DatabaseHelper.SoundModelContract.KEY_USERS), Integer.toString(userId)), "package-restrictions.xml");
    }

    /* access modifiers changed from: private */
    public File getUserRuntimePermissionsFile(int userId) {
        return new File(new File(new File(this.mSystemDir, DatabaseHelper.SoundModelContract.KEY_USERS), Integer.toString(userId)), RUNTIME_PERMISSIONS_FILE_NAME);
    }

    private File getUserPackagesStateBackupFile(int userId) {
        return new File(Environment.getUserSystemDirectory(userId), "package-restrictions-backup.xml");
    }

    /* access modifiers changed from: package-private */
    public void writeAllUsersPackageRestrictionsLPr() {
        List<UserInfo> users = getAllUsers(UserManagerService.getInstance());
        if (users != null) {
            for (UserInfo user : users) {
                writePackageRestrictionsLPr(user.id);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void writeAllRuntimePermissionsLPr() {
        for (int userId : UserManagerService.getInstance().getUserIds()) {
            this.mRuntimePermissionsPersistence.writePermissionsForUserAsyncLPr(userId);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean areDefaultRuntimePermissionsGrantedLPr(int userId) {
        return this.mRuntimePermissionsPersistence.areDefaultRuntimPermissionsGrantedLPr(userId);
    }

    /* access modifiers changed from: package-private */
    public void onDefaultRuntimePermissionsGrantedLPr(int userId) {
        this.mRuntimePermissionsPersistence.onDefaultRuntimePermissionsGrantedLPr(userId);
    }

    public VersionInfo findOrCreateVersion(String volumeUuid) {
        VersionInfo ver = this.mVersion.get(volumeUuid);
        if (ver != null) {
            return ver;
        }
        VersionInfo ver2 = new VersionInfo();
        this.mVersion.put(volumeUuid, ver2);
        return ver2;
    }

    public VersionInfo getInternalVersion() {
        return this.mVersion.get(StorageManager.UUID_PRIVATE_INTERNAL);
    }

    public VersionInfo getExternalVersion() {
        return this.mVersion.get("primary_physical");
    }

    public void onVolumeForgotten(String fsUuid) {
        this.mVersion.remove(fsUuid);
    }

    /* access modifiers changed from: package-private */
    public void readPreferredActivitiesLPw(XmlPullParser parser, int userId) throws XmlPullParserException, IOException {
        int outerDepth = parser.getDepth();
        while (true) {
            int next = parser.next();
            int type = next;
            if (next == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == 3 || type == 4)) {
                if (parser.getName().equals(TAG_ITEM)) {
                    PreferredActivity pa = new PreferredActivity(parser);
                    if (pa.mPref.getParseError() == null) {
                        editPreferredActivitiesLPw(userId).addFilter(pa);
                    } else {
                        PackageManagerService.reportSettingsProblem(5, "Error in package manager settings: <preferred-activity> " + pa.mPref.getParseError() + " at " + parser.getPositionDescription());
                    }
                } else {
                    PackageManagerService.reportSettingsProblem(5, "Unknown element under <preferred-activities>: " + parser.getName());
                    XmlUtils.skipCurrentTag(parser);
                }
            }
        }
    }

    private void readPersistentPreferredActivitiesLPw(XmlPullParser parser, int userId) throws XmlPullParserException, IOException {
        int outerDepth = parser.getDepth();
        while (true) {
            int next = parser.next();
            int type = next;
            if (next == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == 3 || type == 4)) {
                if (parser.getName().equals(TAG_ITEM)) {
                    editPersistentPreferredActivitiesLPw(userId).addFilter(new PersistentPreferredActivity(parser));
                } else {
                    PackageManagerService.reportSettingsProblem(5, "Unknown element under <persistent-preferred-activities>: " + parser.getName());
                    XmlUtils.skipCurrentTag(parser);
                }
            }
        }
    }

    private void readCrossProfileIntentFiltersLPw(XmlPullParser parser, int userId) throws XmlPullParserException, IOException {
        int outerDepth = parser.getDepth();
        while (true) {
            int next = parser.next();
            int type = next;
            if (next == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == 3 || type == 4)) {
                String tagName = parser.getName();
                if (tagName.equals(TAG_ITEM)) {
                    editCrossProfileIntentResolverLPw(userId).addFilter(new CrossProfileIntentFilter(parser));
                } else {
                    PackageManagerService.reportSettingsProblem(5, "Unknown element under crossProfile-intent-filters: " + tagName);
                    XmlUtils.skipCurrentTag(parser);
                }
            }
        }
    }

    private void readDomainVerificationLPw(XmlPullParser parser, PackageSettingBase packageSetting) throws XmlPullParserException, IOException {
        packageSetting.setIntentFilterVerificationInfo(new IntentFilterVerificationInfo(parser));
    }

    private void readRestoredIntentFilterVerifications(XmlPullParser parser) throws XmlPullParserException, IOException {
        int outerDepth = parser.getDepth();
        while (true) {
            int next = parser.next();
            int type = next;
            if (next == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == 3 || type == 4)) {
                String tagName = parser.getName();
                if (tagName.equals(TAG_DOMAIN_VERIFICATION)) {
                    IntentFilterVerificationInfo ivi = new IntentFilterVerificationInfo(parser);
                    if (PackageManagerService.DEBUG_DOMAIN_VERIFICATION) {
                        Slog.i(TAG, "Restored IVI for " + ivi.getPackageName() + " status=" + ivi.getStatusString());
                    }
                    this.mRestoredIntentFilterVerifications.put(ivi.getPackageName(), ivi);
                } else {
                    Slog.w(TAG, "Unknown element: " + tagName);
                    XmlUtils.skipCurrentTag(parser);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void readDefaultAppsLPw(XmlPullParser parser, int userId) throws XmlPullParserException, IOException {
        int outerDepth = parser.getDepth();
        while (true) {
            int next = parser.next();
            int type = next;
            if (next == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == 3 || type == 4)) {
                String tagName = parser.getName();
                if (tagName.equals(TAG_DEFAULT_BROWSER)) {
                    this.mDefaultBrowserApp.put(userId, parser.getAttributeValue(null, "packageName"));
                } else if (tagName.equals(TAG_DEFAULT_DIALER)) {
                    this.mDefaultDialerApp.put(userId, parser.getAttributeValue(null, "packageName"));
                } else {
                    PackageManagerService.reportSettingsProblem(5, "Unknown element under default-apps: " + parser.getName());
                    XmlUtils.skipCurrentTag(parser);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void readBlockUninstallPackagesLPw(XmlPullParser parser, int userId) throws XmlPullParserException, IOException {
        int outerDepth = parser.getDepth();
        ArraySet<String> packages = new ArraySet<>();
        while (true) {
            int next = parser.next();
            int type = next;
            if (next != 1 && (type != 3 || parser.getDepth() > outerDepth)) {
                if (!(type == 3 || type == 4)) {
                    if (parser.getName().equals(TAG_BLOCK_UNINSTALL)) {
                        packages.add(parser.getAttributeValue(null, "packageName"));
                    } else {
                        PackageManagerService.reportSettingsProblem(5, "Unknown element under block-uninstall-packages: " + parser.getName());
                        XmlUtils.skipCurrentTag(parser);
                    }
                }
            }
        }
        if (packages.isEmpty()) {
            this.mBlockUninstallPackages.remove(userId);
        } else {
            this.mBlockUninstallPackages.put(userId, packages);
        }
    }

    /* access modifiers changed from: package-private */
    public void readPackageRestrictionsLPr(int userId) {
        int i;
        File userPackagesStateFile;
        FileInputStream str;
        int type;
        char c;
        boolean z;
        int maxAppLinkGeneration;
        FileInputStream str2;
        int type2;
        FileInputStream str3;
        File userPackagesStateFile2;
        int i2;
        int type3;
        boolean z2;
        String str4;
        char c2;
        int outerDepth;
        int maxAppLinkGeneration2;
        int packageDepth;
        String hiddenStr;
        int type4;
        boolean z3;
        int i3;
        String hiddenStr2;
        int packageDepth2;
        Settings settings = this;
        int i4 = userId;
        FileInputStream str5 = null;
        File userPackagesStateFile3 = getUserPackagesStateFile(userId);
        File backupFile = getUserPackagesStateBackupFile(userId);
        int i5 = 4;
        if (backupFile.exists()) {
            try {
                str5 = new FileInputStream(backupFile);
                settings.mReadMessages.append("Reading from backup stopped packages file\n");
                PackageManagerService.reportSettingsProblem(4, "Need to read from backup stopped packages file");
                if (userPackagesStateFile3.exists()) {
                    Slog.w("PackageManager", "Cleaning up stopped packages file " + userPackagesStateFile3);
                    userPackagesStateFile3.delete();
                }
            } catch (IOException e) {
            }
        }
        FileInputStream str6 = str5;
        if (str6 == null) {
            try {
                if (!userPackagesStateFile3.exists()) {
                    try {
                        settings.mReadMessages.append("No stopped packages file found\n");
                        PackageManagerService.reportSettingsProblem(4, "No stopped packages file; assuming all started");
                        for (PackageSetting pkg : settings.mPackages.values()) {
                            File backupFile2 = backupFile;
                            File userPackagesStateFile4 = userPackagesStateFile3;
                            try {
                                pkg.setUserState(i4, 0, 0, true, false, false, false, false, null, null, null, null, false, false, null, null, null, 0, 0, 0, null);
                                backupFile = backupFile2;
                                userPackagesStateFile3 = userPackagesStateFile4;
                                i4 = userId;
                            } catch (XmlPullParserException e2) {
                                e = e2;
                                FileInputStream fileInputStream = str6;
                                File file = userPackagesStateFile4;
                                int i6 = userId;
                                i = 6;
                                settings.mReadMessages.append("Error reading: " + e.toString());
                                PackageManagerService.reportSettingsProblem(i, "Error reading stopped packages: " + e);
                                Slog.wtf("PackageManager", "Error reading package manager stopped packages", e);
                            } catch (IOException e3) {
                                e = e3;
                                FileInputStream fileInputStream2 = str6;
                                File file2 = userPackagesStateFile4;
                                int i7 = userId;
                                settings.mReadMessages.append("Error reading: " + e.toString());
                                PackageManagerService.reportSettingsProblem(6, "Error reading settings: " + e);
                                Slog.wtf("PackageManager", "Error reading package manager stopped packages", e);
                            }
                        }
                        File file3 = userPackagesStateFile3;
                        return;
                    } catch (XmlPullParserException e4) {
                        e = e4;
                        File file4 = backupFile;
                        i = 6;
                        File file5 = userPackagesStateFile3;
                        int i8 = i4;
                        FileInputStream fileInputStream3 = str6;
                        settings.mReadMessages.append("Error reading: " + e.toString());
                        PackageManagerService.reportSettingsProblem(i, "Error reading stopped packages: " + e);
                        Slog.wtf("PackageManager", "Error reading package manager stopped packages", e);
                    } catch (IOException e5) {
                        e = e5;
                        File file6 = backupFile;
                        File file7 = userPackagesStateFile3;
                        int i9 = i4;
                        FileInputStream fileInputStream4 = str6;
                        settings.mReadMessages.append("Error reading: " + e.toString());
                        PackageManagerService.reportSettingsProblem(6, "Error reading settings: " + e);
                        Slog.wtf("PackageManager", "Error reading package manager stopped packages", e);
                    }
                } else {
                    File userPackagesStateFile5 = userPackagesStateFile3;
                    try {
                        userPackagesStateFile = userPackagesStateFile5;
                        try {
                            str = new FileInputStream(userPackagesStateFile);
                        } catch (XmlPullParserException e6) {
                            e = e6;
                            File file8 = userPackagesStateFile;
                            FileInputStream fileInputStream5 = str6;
                            int i62 = userId;
                            i = 6;
                            settings.mReadMessages.append("Error reading: " + e.toString());
                            PackageManagerService.reportSettingsProblem(i, "Error reading stopped packages: " + e);
                            Slog.wtf("PackageManager", "Error reading package manager stopped packages", e);
                        } catch (IOException e7) {
                            e = e7;
                            File file9 = userPackagesStateFile;
                            FileInputStream fileInputStream6 = str6;
                            int i72 = userId;
                            settings.mReadMessages.append("Error reading: " + e.toString());
                            PackageManagerService.reportSettingsProblem(6, "Error reading settings: " + e);
                            Slog.wtf("PackageManager", "Error reading package manager stopped packages", e);
                        }
                    } catch (XmlPullParserException e8) {
                        e = e8;
                        FileInputStream fileInputStream7 = str6;
                        File file10 = userPackagesStateFile5;
                        int i10 = userId;
                        i = 6;
                        settings.mReadMessages.append("Error reading: " + e.toString());
                        PackageManagerService.reportSettingsProblem(i, "Error reading stopped packages: " + e);
                        Slog.wtf("PackageManager", "Error reading package manager stopped packages", e);
                    } catch (IOException e9) {
                        e = e9;
                        FileInputStream fileInputStream8 = str6;
                        File file11 = userPackagesStateFile5;
                        int i11 = userId;
                        settings.mReadMessages.append("Error reading: " + e.toString());
                        PackageManagerService.reportSettingsProblem(6, "Error reading settings: " + e);
                        Slog.wtf("PackageManager", "Error reading package manager stopped packages", e);
                    }
                }
            } catch (XmlPullParserException e10) {
                e = e10;
                File file12 = backupFile;
                i = 6;
                File file13 = userPackagesStateFile3;
                FileInputStream fileInputStream9 = str6;
                int i12 = userId;
                settings.mReadMessages.append("Error reading: " + e.toString());
                PackageManagerService.reportSettingsProblem(i, "Error reading stopped packages: " + e);
                Slog.wtf("PackageManager", "Error reading package manager stopped packages", e);
            } catch (IOException e11) {
                e = e11;
                File file14 = backupFile;
                File file15 = userPackagesStateFile3;
                FileInputStream fileInputStream10 = str6;
                int i13 = userId;
                settings.mReadMessages.append("Error reading: " + e.toString());
                PackageManagerService.reportSettingsProblem(6, "Error reading settings: " + e);
                Slog.wtf("PackageManager", "Error reading package manager stopped packages", e);
            }
        } else {
            userPackagesStateFile = userPackagesStateFile3;
            str = str6;
        }
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(str, StandardCharsets.UTF_8.name());
            do {
                int next = parser.next();
                type = next;
                c = 2;
                z = true;
                if (next == 2) {
                    break;
                }
            } while (type != 1);
            if (type != 2) {
                try {
                    settings.mReadMessages.append("No start tag found in package restrictions file\n");
                    PackageManagerService.reportSettingsProblem(5, "No start tag found in package manager stopped packages");
                } catch (XmlPullParserException e12) {
                    e = e12;
                    File file16 = userPackagesStateFile;
                    int i622 = userId;
                    i = 6;
                    settings.mReadMessages.append("Error reading: " + e.toString());
                    PackageManagerService.reportSettingsProblem(i, "Error reading stopped packages: " + e);
                    Slog.wtf("PackageManager", "Error reading package manager stopped packages", e);
                } catch (IOException e13) {
                    e = e13;
                    File file17 = userPackagesStateFile;
                    int i722 = userId;
                    settings.mReadMessages.append("Error reading: " + e.toString());
                    PackageManagerService.reportSettingsProblem(6, "Error reading settings: " + e);
                    Slog.wtf("PackageManager", "Error reading package manager stopped packages", e);
                }
            } else {
                int outerDepth2 = parser.getDepth();
                String str7 = null;
                int maxAppLinkGeneration3 = 0;
                while (true) {
                    int next2 = parser.next();
                    int type5 = next2;
                    if (next2 != z) {
                        if (type5 == 3) {
                            if (parser.getDepth() <= outerDepth2) {
                                int i14 = type5;
                                maxAppLinkGeneration = maxAppLinkGeneration3;
                                int i15 = outerDepth2;
                                str2 = str;
                                File file18 = userPackagesStateFile;
                                type2 = userId;
                            }
                        }
                        if (type5 == 3) {
                            type3 = type5;
                            z2 = z;
                            str4 = str7;
                            c2 = c;
                            i2 = i5;
                            outerDepth = outerDepth2;
                            str3 = str;
                            userPackagesStateFile2 = userPackagesStateFile;
                            int type6 = userId;
                            maxAppLinkGeneration2 = maxAppLinkGeneration3;
                        } else if (type5 == i5) {
                            type3 = type5;
                            z2 = z;
                            str4 = str7;
                            c2 = c;
                            i2 = i5;
                            outerDepth = outerDepth2;
                            str3 = str;
                            userPackagesStateFile2 = userPackagesStateFile;
                            int type7 = userId;
                            maxAppLinkGeneration2 = maxAppLinkGeneration3;
                        } else {
                            try {
                                String tagName = parser.getName();
                                if (tagName.equals("pkg")) {
                                    try {
                                        String name = parser.getAttributeValue(str7, ATTR_NAME);
                                        PackageSetting ps = settings.mPackages.get(name);
                                        if (ps == null) {
                                            Slog.w("PackageManager", "No package known for stopped package " + name);
                                            XmlUtils.skipCurrentTag(parser);
                                            PackageSetting packageSetting = ps;
                                        } else {
                                            int maxAppLinkGeneration4 = maxAppLinkGeneration3;
                                            String name2 = name;
                                            long ceDataInode = XmlUtils.readLongAttribute(parser, ATTR_CE_DATA_INODE, 0);
                                            String tagName2 = tagName;
                                            boolean installed = XmlUtils.readBooleanAttribute(parser, ATTR_INSTALLED, z);
                                            boolean stopped = XmlUtils.readBooleanAttribute(parser, ATTR_STOPPED, false);
                                            String str8 = tagName2;
                                            int maxAppLinkGeneration5 = maxAppLinkGeneration4;
                                            boolean notLaunched = XmlUtils.readBooleanAttribute(parser, ATTR_NOT_LAUNCHED, false);
                                            String blockedStr = parser.getAttributeValue(str7, ATTR_BLOCKED);
                                            boolean hidden = blockedStr == null ? false : Boolean.parseBoolean(blockedStr);
                                            String hiddenStr3 = parser.getAttributeValue(str7, ATTR_HIDDEN);
                                            long ceDataInode2 = ceDataInode;
                                            String str9 = str7;
                                            boolean hidden2 = hiddenStr3 == null ? hidden : Boolean.parseBoolean(hiddenStr3);
                                            boolean suspended = XmlUtils.readBooleanAttribute(parser, ATTR_SUSPENDED, false);
                                            String suspendingPackage = parser.getAttributeValue(null, ATTR_SUSPENDING_PACKAGE);
                                            int i16 = type5;
                                            outerDepth = outerDepth2;
                                            String dialogMessage = parser.getAttributeValue(null, ATTR_SUSPEND_DIALOG_MESSAGE);
                                            String suspendingPackage2 = (!suspended || suspendingPackage != null) ? suspendingPackage : PackageManagerService.PLATFORM_PACKAGE_NAME;
                                            boolean blockUninstall = XmlUtils.readBooleanAttribute(parser, ATTR_BLOCK_UNINSTALL, false);
                                            boolean instantApp = XmlUtils.readBooleanAttribute(parser, ATTR_INSTANT_APP, false);
                                            boolean virtualPreload = XmlUtils.readBooleanAttribute(parser, ATTR_VIRTUAL_PRELOAD, false);
                                            String str10 = blockedStr;
                                            int i17 = 1;
                                            int enabled = XmlUtils.readIntAttribute(parser, ATTR_ENABLED, 0);
                                            String enabledCaller = parser.getAttributeValue(null, ATTR_ENABLED_CALLER);
                                            String harmfulAppWarning = parser.getAttributeValue(null, ATTR_HARMFUL_APP_WARNING);
                                            int verifState = XmlUtils.readIntAttribute(parser, ATTR_DOMAIN_VERIFICATON_STATE, 0);
                                            int linkGeneration = XmlUtils.readIntAttribute(parser, ATTR_APP_LINK_GENERATION, 0);
                                            int maxAppLinkGeneration6 = linkGeneration > maxAppLinkGeneration5 ? linkGeneration : maxAppLinkGeneration5;
                                            int installReason = XmlUtils.readIntAttribute(parser, ATTR_INSTALL_REASON, 0);
                                            int packageDepth3 = parser.getDepth();
                                            ArraySet<String> enabledComponents = null;
                                            ArraySet<String> disabledComponents = null;
                                            PersistableBundle suspendedAppExtras = null;
                                            PersistableBundle suspendedLauncherExtras = null;
                                            while (true) {
                                                int packageDepth4 = packageDepth3;
                                                int next3 = parser.next();
                                                int type8 = next3;
                                                if (next3 != i17) {
                                                    type4 = type8;
                                                    if (type4 == 3) {
                                                        if (parser.getDepth() <= packageDepth4) {
                                                            packageDepth = packageDepth4;
                                                            hiddenStr = hiddenStr3;
                                                        }
                                                    }
                                                    if (type4 != 3) {
                                                        if (type4 != 4) {
                                                            String name3 = parser.getName();
                                                            char c3 = 65535;
                                                            packageDepth2 = packageDepth4;
                                                            int packageDepth5 = name3.hashCode();
                                                            hiddenStr2 = hiddenStr3;
                                                            if (packageDepth5 != -2027581689) {
                                                                if (packageDepth5 != -1963032286) {
                                                                    if (packageDepth5 != -1592287551) {
                                                                        if (packageDepth5 == -1422791362) {
                                                                            if (name3.equals(TAG_SUSPENDED_LAUNCHER_EXTRAS)) {
                                                                                c3 = 3;
                                                                            }
                                                                        }
                                                                    } else if (name3.equals(TAG_SUSPENDED_APP_EXTRAS)) {
                                                                        c3 = 2;
                                                                    }
                                                                } else if (name3.equals(TAG_ENABLED_COMPONENTS)) {
                                                                    c3 = 0;
                                                                }
                                                            } else if (name3.equals(TAG_DISABLED_COMPONENTS)) {
                                                                c3 = 1;
                                                            }
                                                            switch (c3) {
                                                                case 0:
                                                                    enabledComponents = settings.readComponentsLPr(parser);
                                                                    break;
                                                                case 1:
                                                                    disabledComponents = settings.readComponentsLPr(parser);
                                                                    break;
                                                                case 2:
                                                                    suspendedAppExtras = PersistableBundle.restoreFromXml(parser);
                                                                    break;
                                                                case 3:
                                                                    suspendedLauncherExtras = PersistableBundle.restoreFromXml(parser);
                                                                    break;
                                                                default:
                                                                    Slog.wtf(TAG, "Unknown tag " + parser.getName() + " under tag " + "pkg");
                                                                    break;
                                                            }
                                                        } else {
                                                            packageDepth2 = packageDepth4;
                                                            hiddenStr2 = hiddenStr3;
                                                        }
                                                    } else {
                                                        packageDepth2 = packageDepth4;
                                                        hiddenStr2 = hiddenStr3;
                                                    }
                                                    int i18 = type4;
                                                    packageDepth3 = packageDepth2;
                                                    hiddenStr3 = hiddenStr2;
                                                    i17 = 1;
                                                } else {
                                                    packageDepth = packageDepth4;
                                                    hiddenStr = hiddenStr3;
                                                    type4 = type8;
                                                }
                                            }
                                            if (blockUninstall) {
                                                i3 = userId;
                                                z3 = true;
                                                try {
                                                    settings.setBlockUninstallLPw(i3, name2, true);
                                                } catch (XmlPullParserException e14) {
                                                    e = e14;
                                                    int i19 = i3;
                                                    File file19 = userPackagesStateFile;
                                                } catch (IOException e15) {
                                                    e = e15;
                                                    int i20 = i3;
                                                    File file20 = userPackagesStateFile;
                                                    settings.mReadMessages.append("Error reading: " + e.toString());
                                                    PackageManagerService.reportSettingsProblem(6, "Error reading settings: " + e);
                                                    Slog.wtf("PackageManager", "Error reading package manager stopped packages", e);
                                                }
                                            } else {
                                                i3 = userId;
                                                z3 = true;
                                            }
                                            String str11 = name2;
                                            int i21 = packageDepth;
                                            int i22 = i3;
                                            String str12 = hiddenStr;
                                            c2 = 2;
                                            type3 = type4;
                                            i2 = 4;
                                            str3 = str;
                                            userPackagesStateFile2 = userPackagesStateFile;
                                            int i23 = i3;
                                            int i24 = linkGeneration;
                                            long j = ceDataInode2;
                                            str4 = null;
                                            z2 = z3;
                                            try {
                                                ps.setUserState(i22, j, enabled, installed, stopped, notLaunched, hidden2, suspended, suspendingPackage2, dialogMessage, suspendedAppExtras, suspendedLauncherExtras, instantApp, virtualPreload, enabledCaller, enabledComponents, disabledComponents, verifState, linkGeneration, installReason, harmfulAppWarning);
                                                int i25 = i23;
                                                PackageSetting packageSetting2 = ps;
                                                maxAppLinkGeneration3 = maxAppLinkGeneration6;
                                                settings = this;
                                                outerDepth2 = outerDepth;
                                                c = c2;
                                                str7 = str4;
                                                z = z2;
                                                i5 = i2;
                                                userPackagesStateFile = userPackagesStateFile2;
                                                str = str3;
                                            } catch (XmlPullParserException e16) {
                                                e = e16;
                                                int i26 = i23;
                                                FileInputStream fileInputStream11 = str3;
                                                settings = this;
                                                i = 6;
                                                settings.mReadMessages.append("Error reading: " + e.toString());
                                                PackageManagerService.reportSettingsProblem(i, "Error reading stopped packages: " + e);
                                                Slog.wtf("PackageManager", "Error reading package manager stopped packages", e);
                                            } catch (IOException e17) {
                                                e = e17;
                                                int i27 = i23;
                                                FileInputStream fileInputStream12 = str3;
                                                settings = this;
                                                settings.mReadMessages.append("Error reading: " + e.toString());
                                                PackageManagerService.reportSettingsProblem(6, "Error reading settings: " + e);
                                                Slog.wtf("PackageManager", "Error reading package manager stopped packages", e);
                                            }
                                        }
                                    } catch (XmlPullParserException e18) {
                                        e = e18;
                                        FileInputStream fileInputStream13 = str;
                                        File file21 = userPackagesStateFile;
                                        settings = this;
                                        int i28 = userId;
                                        i = 6;
                                        settings.mReadMessages.append("Error reading: " + e.toString());
                                        PackageManagerService.reportSettingsProblem(i, "Error reading stopped packages: " + e);
                                        Slog.wtf("PackageManager", "Error reading package manager stopped packages", e);
                                    } catch (IOException e19) {
                                        e = e19;
                                        FileInputStream fileInputStream14 = str;
                                        File file22 = userPackagesStateFile;
                                        settings = this;
                                        int i29 = userId;
                                        settings.mReadMessages.append("Error reading: " + e.toString());
                                        PackageManagerService.reportSettingsProblem(6, "Error reading settings: " + e);
                                        Slog.wtf("PackageManager", "Error reading package manager stopped packages", e);
                                    }
                                } else {
                                    type3 = type5;
                                    z2 = z;
                                    str4 = str7;
                                    c2 = c;
                                    i2 = i5;
                                    outerDepth = outerDepth2;
                                    str3 = str;
                                    userPackagesStateFile2 = userPackagesStateFile;
                                    int i30 = userId;
                                    int maxAppLinkGeneration7 = maxAppLinkGeneration3;
                                    String tagName3 = tagName;
                                    try {
                                        if (tagName3.equals("preferred-activities")) {
                                            int i31 = i30;
                                            settings = this;
                                            try {
                                                settings.readPreferredActivitiesLPw(parser, i31);
                                            } catch (XmlPullParserException e20) {
                                                e = e20;
                                                i = 6;
                                                settings.mReadMessages.append("Error reading: " + e.toString());
                                                PackageManagerService.reportSettingsProblem(i, "Error reading stopped packages: " + e);
                                                Slog.wtf("PackageManager", "Error reading package manager stopped packages", e);
                                            } catch (IOException e21) {
                                                e = e21;
                                                settings.mReadMessages.append("Error reading: " + e.toString());
                                                PackageManagerService.reportSettingsProblem(6, "Error reading settings: " + e);
                                                Slog.wtf("PackageManager", "Error reading package manager stopped packages", e);
                                            }
                                        } else {
                                            int i32 = i30;
                                            settings = this;
                                            if (tagName3.equals(TAG_PERSISTENT_PREFERRED_ACTIVITIES)) {
                                                settings.readPersistentPreferredActivitiesLPw(parser, i32);
                                            } else if (tagName3.equals(TAG_CROSS_PROFILE_INTENT_FILTERS)) {
                                                settings.readCrossProfileIntentFiltersLPw(parser, i32);
                                            } else if (tagName3.equals(TAG_DEFAULT_APPS)) {
                                                settings.readDefaultAppsLPw(parser, i32);
                                            } else if (tagName3.equals(TAG_BLOCK_UNINSTALL_PACKAGES)) {
                                                settings.readBlockUninstallPackagesLPw(parser, i32);
                                            } else {
                                                Slog.w("PackageManager", "Unknown element under <stopped-packages>: " + parser.getName());
                                                XmlUtils.skipCurrentTag(parser);
                                            }
                                        }
                                        maxAppLinkGeneration3 = maxAppLinkGeneration7;
                                        outerDepth2 = outerDepth;
                                        c = c2;
                                        str7 = str4;
                                        z = z2;
                                        i5 = i2;
                                        userPackagesStateFile = userPackagesStateFile2;
                                        str = str3;
                                    } catch (XmlPullParserException e22) {
                                        e = e22;
                                        int i33 = i30;
                                        settings = this;
                                        i = 6;
                                        settings.mReadMessages.append("Error reading: " + e.toString());
                                        PackageManagerService.reportSettingsProblem(i, "Error reading stopped packages: " + e);
                                        Slog.wtf("PackageManager", "Error reading package manager stopped packages", e);
                                    } catch (IOException e23) {
                                        e = e23;
                                        int i34 = i30;
                                        settings = this;
                                        settings.mReadMessages.append("Error reading: " + e.toString());
                                        PackageManagerService.reportSettingsProblem(6, "Error reading settings: " + e);
                                        Slog.wtf("PackageManager", "Error reading package manager stopped packages", e);
                                    }
                                }
                            } catch (XmlPullParserException e24) {
                                e = e24;
                                FileInputStream fileInputStream15 = str;
                                File file23 = userPackagesStateFile;
                                int i282 = userId;
                                i = 6;
                                settings.mReadMessages.append("Error reading: " + e.toString());
                                PackageManagerService.reportSettingsProblem(i, "Error reading stopped packages: " + e);
                                Slog.wtf("PackageManager", "Error reading package manager stopped packages", e);
                            } catch (IOException e25) {
                                e = e25;
                                FileInputStream fileInputStream16 = str;
                                File file24 = userPackagesStateFile;
                                int i292 = userId;
                                settings.mReadMessages.append("Error reading: " + e.toString());
                                PackageManagerService.reportSettingsProblem(6, "Error reading settings: " + e);
                                Slog.wtf("PackageManager", "Error reading package manager stopped packages", e);
                            }
                        }
                        maxAppLinkGeneration3 = maxAppLinkGeneration2;
                        outerDepth2 = outerDepth;
                        c = c2;
                        str7 = str4;
                        z = z2;
                        i5 = i2;
                        userPackagesStateFile = userPackagesStateFile2;
                        str = str3;
                    } else {
                        int i35 = type5;
                        maxAppLinkGeneration = maxAppLinkGeneration3;
                        int i36 = outerDepth2;
                        str2 = str;
                        File file25 = userPackagesStateFile;
                        type2 = userId;
                    }
                }
                FileInputStream str13 = str2;
                try {
                    str13.close();
                    settings.mNextAppLinkGeneration.put(type2, maxAppLinkGeneration + 1);
                    FileInputStream fileInputStream17 = str13;
                } catch (XmlPullParserException e26) {
                    e = e26;
                    FileInputStream fileInputStream18 = str13;
                    i = 6;
                    settings.mReadMessages.append("Error reading: " + e.toString());
                    PackageManagerService.reportSettingsProblem(i, "Error reading stopped packages: " + e);
                    Slog.wtf("PackageManager", "Error reading package manager stopped packages", e);
                } catch (IOException e27) {
                    e = e27;
                    FileInputStream fileInputStream19 = str13;
                    settings.mReadMessages.append("Error reading: " + e.toString());
                    PackageManagerService.reportSettingsProblem(6, "Error reading settings: " + e);
                    Slog.wtf("PackageManager", "Error reading package manager stopped packages", e);
                }
            }
        } catch (XmlPullParserException e28) {
            e = e28;
            FileInputStream fileInputStream20 = str;
            File file26 = userPackagesStateFile;
            int i37 = userId;
            i = 6;
            settings.mReadMessages.append("Error reading: " + e.toString());
            PackageManagerService.reportSettingsProblem(i, "Error reading stopped packages: " + e);
            Slog.wtf("PackageManager", "Error reading package manager stopped packages", e);
        } catch (IOException e29) {
            e = e29;
            FileInputStream fileInputStream21 = str;
            File file27 = userPackagesStateFile;
            int i38 = userId;
            settings.mReadMessages.append("Error reading: " + e.toString());
            PackageManagerService.reportSettingsProblem(6, "Error reading settings: " + e);
            Slog.wtf("PackageManager", "Error reading package manager stopped packages", e);
        }
    }

    /* access modifiers changed from: package-private */
    public void setBlockUninstallLPw(int userId, String packageName, boolean blockUninstall) {
        ArraySet<String> packages = this.mBlockUninstallPackages.get(userId);
        if (blockUninstall) {
            if (packages == null) {
                packages = new ArraySet<>();
                this.mBlockUninstallPackages.put(userId, packages);
            }
            packages.add(packageName);
        } else if (packages != null) {
            packages.remove(packageName);
            if (packages.isEmpty()) {
                this.mBlockUninstallPackages.remove(userId);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean getBlockUninstallLPr(int userId, String packageName) {
        ArraySet<String> packages = this.mBlockUninstallPackages.get(userId);
        if (packages == null) {
            return false;
        }
        return packages.contains(packageName);
    }

    private ArraySet<String> readComponentsLPr(XmlPullParser parser) throws IOException, XmlPullParserException {
        ArraySet<String> components = null;
        int outerDepth = parser.getDepth();
        while (true) {
            int next = parser.next();
            int type = next;
            if (next == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                return components;
            }
            if (!(type == 3 || type == 4 || !parser.getName().equals(TAG_ITEM))) {
                String componentName = parser.getAttributeValue(null, ATTR_NAME);
                if (componentName != null) {
                    if (components == null) {
                        components = new ArraySet<>();
                    }
                    components.add(componentName);
                }
            }
        }
        return components;
    }

    /* access modifiers changed from: package-private */
    public void writePreferredActivitiesLPr(XmlSerializer serializer, int userId, boolean full) throws IllegalArgumentException, IllegalStateException, IOException {
        serializer.startTag(null, "preferred-activities");
        PreferredIntentResolver pir = this.mPreferredActivities.get(userId);
        if (pir != null) {
            for (PreferredActivity pa : pir.filterSet()) {
                serializer.startTag(null, TAG_ITEM);
                pa.writeToXml(serializer, full);
                serializer.endTag(null, TAG_ITEM);
            }
        }
        serializer.endTag(null, "preferred-activities");
    }

    /* access modifiers changed from: package-private */
    public void writePersistentPreferredActivitiesLPr(XmlSerializer serializer, int userId) throws IllegalArgumentException, IllegalStateException, IOException {
        serializer.startTag(null, TAG_PERSISTENT_PREFERRED_ACTIVITIES);
        PersistentPreferredIntentResolver ppir = this.mPersistentPreferredActivities.get(userId);
        if (ppir != null) {
            for (PersistentPreferredActivity ppa : ppir.filterSet()) {
                serializer.startTag(null, TAG_ITEM);
                ppa.writeToXml(serializer);
                serializer.endTag(null, TAG_ITEM);
            }
        }
        serializer.endTag(null, TAG_PERSISTENT_PREFERRED_ACTIVITIES);
    }

    /* access modifiers changed from: package-private */
    public void writeCrossProfileIntentFiltersLPr(XmlSerializer serializer, int userId) throws IllegalArgumentException, IllegalStateException, IOException {
        serializer.startTag(null, TAG_CROSS_PROFILE_INTENT_FILTERS);
        CrossProfileIntentResolver cpir = this.mCrossProfileIntentResolvers.get(userId);
        if (cpir != null) {
            for (CrossProfileIntentFilter cpif : cpir.filterSet()) {
                serializer.startTag(null, TAG_ITEM);
                cpif.writeToXml(serializer);
                serializer.endTag(null, TAG_ITEM);
            }
        }
        serializer.endTag(null, TAG_CROSS_PROFILE_INTENT_FILTERS);
    }

    /* access modifiers changed from: package-private */
    public void writeDomainVerificationsLPr(XmlSerializer serializer, IntentFilterVerificationInfo verificationInfo) throws IllegalArgumentException, IllegalStateException, IOException {
        if (verificationInfo != null && verificationInfo.getPackageName() != null) {
            serializer.startTag(null, TAG_DOMAIN_VERIFICATION);
            verificationInfo.writeToXml(serializer);
            if (PackageManagerService.DEBUG_DOMAIN_VERIFICATION) {
                Slog.d(TAG, "Wrote domain verification for package: " + verificationInfo.getPackageName());
            }
            serializer.endTag(null, TAG_DOMAIN_VERIFICATION);
        }
    }

    /* access modifiers changed from: package-private */
    public void writeAllDomainVerificationsLPr(XmlSerializer serializer, int userId) throws IllegalArgumentException, IllegalStateException, IOException {
        serializer.startTag(null, TAG_ALL_INTENT_FILTER_VERIFICATION);
        int N = this.mPackages.size();
        for (int i = 0; i < N; i++) {
            IntentFilterVerificationInfo ivi = this.mPackages.valueAt(i).getIntentFilterVerificationInfo();
            if (ivi != null) {
                writeDomainVerificationsLPr(serializer, ivi);
            }
        }
        serializer.endTag(null, TAG_ALL_INTENT_FILTER_VERIFICATION);
    }

    /* access modifiers changed from: package-private */
    public void readAllDomainVerificationsLPr(XmlPullParser parser, int userId) throws XmlPullParserException, IOException {
        this.mRestoredIntentFilterVerifications.clear();
        int outerDepth = parser.getDepth();
        while (true) {
            int next = parser.next();
            int type = next;
            if (next == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == 3 || type == 4)) {
                if (parser.getName().equals(TAG_DOMAIN_VERIFICATION)) {
                    IntentFilterVerificationInfo ivi = new IntentFilterVerificationInfo(parser);
                    String pkgName = ivi.getPackageName();
                    PackageSetting ps = this.mPackages.get(pkgName);
                    if (ps != null) {
                        ps.setIntentFilterVerificationInfo(ivi);
                        if (PackageManagerService.DEBUG_DOMAIN_VERIFICATION) {
                            Slog.d(TAG, "Restored IVI for existing app " + pkgName + " status=" + ivi.getStatusString());
                        }
                    } else {
                        this.mRestoredIntentFilterVerifications.put(pkgName, ivi);
                        if (PackageManagerService.DEBUG_DOMAIN_VERIFICATION) {
                            Slog.d(TAG, "Restored IVI for pending app " + pkgName + " status=" + ivi.getStatusString());
                        }
                    }
                } else {
                    PackageManagerService.reportSettingsProblem(5, "Unknown element under <all-intent-filter-verification>: " + parser.getName());
                    XmlUtils.skipCurrentTag(parser);
                }
            }
        }
    }

    public void processRestoredPermissionGrantLPr(String pkgName, String permission, boolean isGranted, int restoredFlagSet, int userId) {
        this.mRuntimePermissionsPersistence.rememberRestoredUserGrantLPr(pkgName, permission, isGranted, restoredFlagSet, userId);
    }

    /* access modifiers changed from: package-private */
    public void writeDefaultAppsLPr(XmlSerializer serializer, int userId) throws IllegalArgumentException, IllegalStateException, IOException {
        serializer.startTag(null, TAG_DEFAULT_APPS);
        String defaultBrowser = this.mDefaultBrowserApp.get(userId);
        if (!TextUtils.isEmpty(defaultBrowser)) {
            serializer.startTag(null, TAG_DEFAULT_BROWSER);
            serializer.attribute(null, "packageName", defaultBrowser);
            serializer.endTag(null, TAG_DEFAULT_BROWSER);
        }
        String defaultDialer = this.mDefaultDialerApp.get(userId);
        if (!TextUtils.isEmpty(defaultDialer)) {
            serializer.startTag(null, TAG_DEFAULT_DIALER);
            serializer.attribute(null, "packageName", defaultDialer);
            serializer.endTag(null, TAG_DEFAULT_DIALER);
        }
        serializer.endTag(null, TAG_DEFAULT_APPS);
    }

    /* access modifiers changed from: package-private */
    public void writeBlockUninstallPackagesLPr(XmlSerializer serializer, int userId) throws IOException {
        ArraySet<String> packages = this.mBlockUninstallPackages.get(userId);
        if (packages != null) {
            serializer.startTag(null, TAG_BLOCK_UNINSTALL_PACKAGES);
            for (int i = 0; i < packages.size(); i++) {
                serializer.startTag(null, TAG_BLOCK_UNINSTALL);
                serializer.attribute(null, "packageName", packages.valueAt(i));
                serializer.endTag(null, TAG_BLOCK_UNINSTALL);
            }
            serializer.endTag(null, TAG_BLOCK_UNINSTALL_PACKAGES);
        }
    }

    /* access modifiers changed from: package-private */
    public void writePackageRestrictionsLPr(int userId) {
        int i = userId;
        long startTime = SystemClock.uptimeMillis();
        File userPackagesStateFile = getUserPackagesStateFile(userId);
        File backupFile = getUserPackagesStateBackupFile(userId);
        new File(userPackagesStateFile.getParent()).mkdirs();
        if (userPackagesStateFile.exists()) {
            if (backupFile.exists()) {
                userPackagesStateFile.delete();
                Slog.w("PackageManager", "Preserving older stopped packages backup");
            } else if (!userPackagesStateFile.renameTo(backupFile)) {
                Slog.wtf("PackageManager", "Unable to backup user packages state file, current changes will be lost at reboot");
                return;
            }
        }
        try {
            FileOutputStream fstr = new FileOutputStream(userPackagesStateFile);
            BufferedOutputStream str = new BufferedOutputStream(fstr);
            FastXmlSerializer fastXmlSerializer = new FastXmlSerializer();
            fastXmlSerializer.setOutput(str, StandardCharsets.UTF_8.name());
            String str2 = null;
            fastXmlSerializer.startDocument(null, true);
            fastXmlSerializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            fastXmlSerializer.startTag(null, TAG_PACKAGE_RESTRICTIONS);
            for (PackageSetting pkg : this.mPackages.values()) {
                PackageUserState ustate = pkg.readUserState(i);
                fastXmlSerializer.startTag(str2, "pkg");
                fastXmlSerializer.attribute(str2, ATTR_NAME, pkg.name);
                if (ustate.ceDataInode != 0) {
                    XmlUtils.writeLongAttribute(fastXmlSerializer, ATTR_CE_DATA_INODE, ustate.ceDataInode);
                }
                if (!ustate.installed) {
                    fastXmlSerializer.attribute(null, ATTR_INSTALLED, "false");
                }
                if (ustate.stopped) {
                    fastXmlSerializer.attribute(null, ATTR_STOPPED, "true");
                }
                if (ustate.notLaunched) {
                    fastXmlSerializer.attribute(null, ATTR_NOT_LAUNCHED, "true");
                }
                if (ustate.hidden) {
                    fastXmlSerializer.attribute(null, ATTR_HIDDEN, "true");
                }
                if (ustate.suspended) {
                    fastXmlSerializer.attribute(null, ATTR_SUSPENDED, "true");
                    if (ustate.suspendingPackage != null) {
                        fastXmlSerializer.attribute(null, ATTR_SUSPENDING_PACKAGE, ustate.suspendingPackage);
                    }
                    if (ustate.dialogMessage != null) {
                        fastXmlSerializer.attribute(null, ATTR_SUSPEND_DIALOG_MESSAGE, ustate.dialogMessage);
                    }
                    if (ustate.suspendedAppExtras != null) {
                        fastXmlSerializer.startTag(null, TAG_SUSPENDED_APP_EXTRAS);
                        try {
                            ustate.suspendedAppExtras.saveToXml(fastXmlSerializer);
                        } catch (XmlPullParserException xmle) {
                            Slog.wtf(TAG, "Exception while trying to write suspendedAppExtras for " + pkg + ". Will be lost on reboot", xmle);
                        }
                        fastXmlSerializer.endTag(null, TAG_SUSPENDED_APP_EXTRAS);
                    }
                    if (ustate.suspendedLauncherExtras != null) {
                        fastXmlSerializer.startTag(null, TAG_SUSPENDED_LAUNCHER_EXTRAS);
                        try {
                            ustate.suspendedLauncherExtras.saveToXml(fastXmlSerializer);
                        } catch (XmlPullParserException xmle2) {
                            Slog.wtf(TAG, "Exception while trying to write suspendedLauncherExtras for " + pkg + ". Will be lost on reboot", xmle2);
                        }
                        fastXmlSerializer.endTag(null, TAG_SUSPENDED_LAUNCHER_EXTRAS);
                    }
                }
                if (ustate.instantApp) {
                    fastXmlSerializer.attribute(null, ATTR_INSTANT_APP, "true");
                }
                if (ustate.virtualPreload) {
                    fastXmlSerializer.attribute(null, ATTR_VIRTUAL_PRELOAD, "true");
                }
                if (ustate.enabled != 0) {
                    fastXmlSerializer.attribute(null, ATTR_ENABLED, Integer.toString(ustate.enabled));
                    if (ustate.lastDisableAppCaller != null) {
                        fastXmlSerializer.attribute(null, ATTR_ENABLED_CALLER, ustate.lastDisableAppCaller);
                    }
                }
                if (ustate.domainVerificationStatus != 0) {
                    XmlUtils.writeIntAttribute(fastXmlSerializer, ATTR_DOMAIN_VERIFICATON_STATE, ustate.domainVerificationStatus);
                }
                if (ustate.appLinkGeneration != 0) {
                    XmlUtils.writeIntAttribute(fastXmlSerializer, ATTR_APP_LINK_GENERATION, ustate.appLinkGeneration);
                }
                if (ustate.installReason != 0) {
                    fastXmlSerializer.attribute(null, ATTR_INSTALL_REASON, Integer.toString(ustate.installReason));
                }
                if (ustate.harmfulAppWarning != null) {
                    fastXmlSerializer.attribute(null, ATTR_HARMFUL_APP_WARNING, ustate.harmfulAppWarning);
                }
                if (!ArrayUtils.isEmpty(ustate.enabledComponents)) {
                    fastXmlSerializer.startTag(null, TAG_ENABLED_COMPONENTS);
                    Iterator it = ustate.enabledComponents.iterator();
                    while (it.hasNext()) {
                        fastXmlSerializer.startTag(null, TAG_ITEM);
                        fastXmlSerializer.attribute(null, ATTR_NAME, (String) it.next());
                        fastXmlSerializer.endTag(null, TAG_ITEM);
                    }
                    fastXmlSerializer.endTag(null, TAG_ENABLED_COMPONENTS);
                }
                if (!ArrayUtils.isEmpty(ustate.disabledComponents)) {
                    fastXmlSerializer.startTag(null, TAG_DISABLED_COMPONENTS);
                    Iterator it2 = ustate.disabledComponents.iterator();
                    while (it2.hasNext()) {
                        fastXmlSerializer.startTag(null, TAG_ITEM);
                        fastXmlSerializer.attribute(null, ATTR_NAME, (String) it2.next());
                        fastXmlSerializer.endTag(null, TAG_ITEM);
                    }
                    fastXmlSerializer.endTag(null, TAG_DISABLED_COMPONENTS);
                }
                fastXmlSerializer.endTag(null, "pkg");
                str2 = null;
            }
            writePreferredActivitiesLPr(fastXmlSerializer, i, true);
            writePersistentPreferredActivitiesLPr(fastXmlSerializer, i);
            writeCrossProfileIntentFiltersLPr(fastXmlSerializer, i);
            writeDefaultAppsLPr(fastXmlSerializer, i);
            writeBlockUninstallPackagesLPr(fastXmlSerializer, i);
            fastXmlSerializer.endTag(null, TAG_PACKAGE_RESTRICTIONS);
            fastXmlSerializer.endDocument();
            str.flush();
            FileUtils.sync(fstr);
            str.close();
            backupFile.delete();
            FileUtils.setPermissions(userPackagesStateFile.toString(), 432, -1, -1);
            EventLogTags.writeCommitSysConfigFile("package-user-" + i, SystemClock.uptimeMillis() - startTime);
        } catch (IOException e) {
            Slog.wtf("PackageManager", "Unable to write package manager user packages state,  current changes will be lost at reboot", e);
            if (userPackagesStateFile.exists() && !userPackagesStateFile.delete()) {
                Log.i("PackageManager", "Failed to clean up mangled file: " + this.mStoppedPackagesFilename);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void readInstallPermissionsLPr(XmlPullParser parser, PermissionsState permissionsState) throws IOException, XmlPullParserException {
        int outerDepth = parser.getDepth();
        while (true) {
            int next = parser.next();
            int type = next;
            boolean granted = true;
            if (next == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == 3 || type == 4)) {
                if (parser.getName().equals(TAG_ITEM)) {
                    BasePermission bp = this.mPermissions.getPermission(parser.getAttributeValue(null, ATTR_NAME));
                    if (bp == null) {
                        Slog.w("PackageManager", "Unknown permission: " + name);
                        XmlUtils.skipCurrentTag(parser);
                    } else {
                        String grantedStr = parser.getAttributeValue(null, ATTR_GRANTED);
                        int flags = 0;
                        if (grantedStr != null && !Boolean.parseBoolean(grantedStr)) {
                            granted = false;
                        }
                        String flagsStr = parser.getAttributeValue(null, ATTR_FLAGS);
                        if (flagsStr != null) {
                            flags = Integer.parseInt(flagsStr, 16);
                        }
                        if (granted) {
                            if (permissionsState.grantInstallPermission(bp) == -1) {
                                Slog.w("PackageManager", "Permission already added: " + name);
                                XmlUtils.skipCurrentTag(parser);
                            } else {
                                permissionsState.updatePermissionFlags(bp, -1, 255, flags);
                            }
                        } else if (permissionsState.revokeInstallPermission(bp) == -1) {
                            Slog.w("PackageManager", "Permission already added: " + name);
                            XmlUtils.skipCurrentTag(parser);
                        } else {
                            permissionsState.updatePermissionFlags(bp, -1, 255, flags);
                        }
                    }
                } else {
                    Slog.w("PackageManager", "Unknown element under <permissions>: " + parser.getName());
                    XmlUtils.skipCurrentTag(parser);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void writePermissionsLPr(XmlSerializer serializer, List<PermissionsState.PermissionState> permissionStates) throws IOException {
        if (!permissionStates.isEmpty()) {
            serializer.startTag(null, TAG_PERMISSIONS);
            for (PermissionsState.PermissionState permissionState : permissionStates) {
                serializer.startTag(null, TAG_ITEM);
                serializer.attribute(null, ATTR_NAME, permissionState.getName());
                serializer.attribute(null, ATTR_GRANTED, String.valueOf(permissionState.isGranted()));
                serializer.attribute(null, ATTR_FLAGS, Integer.toHexString(permissionState.getFlags()));
                serializer.endTag(null, TAG_ITEM);
            }
            serializer.endTag(null, TAG_PERMISSIONS);
        }
    }

    /* access modifiers changed from: package-private */
    public void writeChildPackagesLPw(XmlSerializer serializer, List<String> childPackageNames) throws IOException {
        if (childPackageNames != null) {
            int childCount = childPackageNames.size();
            for (int i = 0; i < childCount; i++) {
                serializer.startTag(null, TAG_CHILD_PACKAGE);
                serializer.attribute(null, ATTR_NAME, childPackageNames.get(i));
                serializer.endTag(null, TAG_CHILD_PACKAGE);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void readUsesStaticLibLPw(XmlPullParser parser, PackageSetting outPs) throws IOException, XmlPullParserException {
        int outerDepth = parser.getDepth();
        while (true) {
            int next = parser.next();
            int type = next;
            if (next == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == 3 || type == 4)) {
                String libName = parser.getAttributeValue(null, ATTR_NAME);
                long libVersion = -1;
                try {
                    libVersion = Long.parseLong(parser.getAttributeValue(null, "version"));
                } catch (NumberFormatException e) {
                }
                if (libName != null && libVersion >= 0) {
                    outPs.usesStaticLibraries = (String[]) ArrayUtils.appendElement(String.class, outPs.usesStaticLibraries, libName);
                    outPs.usesStaticLibrariesVersions = ArrayUtils.appendLong(outPs.usesStaticLibrariesVersions, libVersion);
                }
                XmlUtils.skipCurrentTag(parser);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void writeUsesStaticLibLPw(XmlSerializer serializer, String[] usesStaticLibraries, long[] usesStaticLibraryVersions) throws IOException {
        if (!ArrayUtils.isEmpty(usesStaticLibraries) && !ArrayUtils.isEmpty(usesStaticLibraryVersions) && usesStaticLibraries.length == usesStaticLibraryVersions.length) {
            int libCount = usesStaticLibraries.length;
            for (int i = 0; i < libCount; i++) {
                String libName = usesStaticLibraries[i];
                long libVersion = usesStaticLibraryVersions[i];
                serializer.startTag(null, TAG_USES_STATIC_LIB);
                serializer.attribute(null, ATTR_NAME, libName);
                serializer.attribute(null, "version", Long.toString(libVersion));
                serializer.endTag(null, TAG_USES_STATIC_LIB);
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x00a0 A[Catch:{ XmlPullParserException -> 0x0087, IOException -> 0x0084 }] */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x00ae A[Catch:{ XmlPullParserException -> 0x0087, IOException -> 0x0084 }] */
    public void readStoppedLPw() {
        int type;
        FileInputStream str = null;
        if (this.mBackupStoppedPackagesFilename.exists()) {
            try {
                str = new FileInputStream(this.mBackupStoppedPackagesFilename);
                this.mReadMessages.append("Reading from backup stopped packages file\n");
                PackageManagerService.reportSettingsProblem(4, "Need to read from backup stopped packages file");
                if (this.mSettingsFilename.exists()) {
                    Slog.w("PackageManager", "Cleaning up stopped packages file " + this.mStoppedPackagesFilename);
                    this.mStoppedPackagesFilename.delete();
                }
            } catch (IOException e) {
            }
        }
        if (str == null) {
            try {
                if (!this.mStoppedPackagesFilename.exists()) {
                    this.mReadMessages.append("No stopped packages file found\n");
                    PackageManagerService.reportSettingsProblem(4, "No stopped packages file file; assuming all started");
                    for (PackageSetting pkg : this.mPackages.values()) {
                        pkg.setStopped(false, 0);
                        pkg.setNotLaunched(false, 0);
                    }
                    return;
                }
                str = new FileInputStream(this.mStoppedPackagesFilename);
            } catch (XmlPullParserException e2) {
                StringBuilder sb = this.mReadMessages;
                sb.append("Error reading: " + e2.toString());
                PackageManagerService.reportSettingsProblem(6, "Error reading stopped packages: " + e2);
                Slog.wtf("PackageManager", "Error reading package manager stopped packages", e2);
            } catch (IOException e3) {
                StringBuilder sb2 = this.mReadMessages;
                sb2.append("Error reading: " + e3.toString());
                PackageManagerService.reportSettingsProblem(6, "Error reading settings: " + e3);
                Slog.wtf("PackageManager", "Error reading package manager stopped packages", e3);
            }
        }
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(str, null);
        while (true) {
            int next = parser.next();
            type = next;
            if (next == 2 || type == 1) {
                if (type == 2) {
                    this.mReadMessages.append("No start tag found in stopped packages file\n");
                    PackageManagerService.reportSettingsProblem(5, "No start tag found in package manager stopped packages");
                    return;
                }
                int outerDepth = parser.getDepth();
                while (true) {
                    int next2 = parser.next();
                    int type2 = next2;
                    if (next2 == 1 || (type2 == 3 && parser.getDepth() <= outerDepth)) {
                        str.close();
                    } else if (type2 != 3) {
                        if (type2 != 4) {
                            if (parser.getName().equals("pkg")) {
                                String name = parser.getAttributeValue(null, ATTR_NAME);
                                PackageSetting ps = this.mPackages.get(name);
                                if (ps != null) {
                                    ps.setStopped(true, 0);
                                    if ("1".equals(parser.getAttributeValue(null, ATTR_NOT_LAUNCHED))) {
                                        ps.setNotLaunched(true, 0);
                                    }
                                } else {
                                    Slog.w("PackageManager", "No package known for stopped package " + name);
                                }
                                XmlUtils.skipCurrentTag(parser);
                            } else {
                                Slog.w("PackageManager", "Unknown element under <stopped-packages>: " + parser.getName());
                                XmlUtils.skipCurrentTag(parser);
                            }
                        }
                    }
                }
                str.close();
                return;
            }
        }
        if (type == 2) {
        }
    }

    /* access modifiers changed from: package-private */
    public void writeLPr() {
        long startTime = SystemClock.uptimeMillis();
        if (this.mSettingsFilename.exists()) {
            if (this.mBackupSettingsFilename.exists()) {
                this.mSettingsFilename.delete();
                Slog.w("PackageManager", "Preserving older settings backup");
            } else if (!this.mSettingsFilename.renameTo(this.mBackupSettingsFilename)) {
                Slog.wtf("PackageManager", "Unable to backup package manager settings,  current changes will be lost at reboot");
                return;
            }
        }
        this.mPastSignatures.clear();
        try {
            FileOutputStream fstr = new FileOutputStream(this.mSettingsFilename);
            BufferedOutputStream str = new BufferedOutputStream(fstr);
            XmlSerializer serializer = new FastXmlSerializer();
            serializer.setOutput(str, StandardCharsets.UTF_8.name());
            serializer.startDocument(null, true);
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            serializer.startTag(null, "packages");
            for (int i = 0; i < this.mVersion.size(); i++) {
                VersionInfo ver = this.mVersion.valueAt(i);
                serializer.startTag(null, "version");
                XmlUtils.writeStringAttribute(serializer, ATTR_VOLUME_UUID, this.mVersion.keyAt(i));
                XmlUtils.writeIntAttribute(serializer, ATTR_SDK_VERSION, ver.sdkVersion);
                XmlUtils.writeIntAttribute(serializer, ATTR_DATABASE_VERSION, ver.databaseVersion);
                XmlUtils.writeStringAttribute(serializer, ATTR_FINGERPRINT, ver.fingerprint);
                XmlUtils.writeStringAttribute(serializer, ATTR_HWFINGERPRINT, ver.hwFingerprint);
                serializer.endTag(null, "version");
            }
            if (this.mVerifierDeviceIdentity != null) {
                serializer.startTag(null, "verifier");
                serializer.attribute(null, "device", this.mVerifierDeviceIdentity.toString());
                serializer.endTag(null, "verifier");
            }
            if (this.mReadExternalStorageEnforced != null) {
                serializer.startTag(null, TAG_READ_EXTERNAL_STORAGE);
                serializer.attribute(null, ATTR_ENFORCEMENT, this.mReadExternalStorageEnforced.booleanValue() ? "1" : "0");
                serializer.endTag(null, TAG_READ_EXTERNAL_STORAGE);
            }
            serializer.startTag(null, "permission-trees");
            this.mPermissions.writePermissionTrees(serializer);
            serializer.endTag(null, "permission-trees");
            serializer.startTag(null, "permissions");
            this.mPermissions.writePermissions(serializer);
            serializer.endTag(null, "permissions");
            for (PackageSetting pkg : this.mPackages.values()) {
                writePackageLPr(serializer, pkg);
            }
            for (PackageSetting pkg2 : this.mDisabledSysPackages.values()) {
                writeDisabledSysPackageLPr(serializer, pkg2);
            }
            for (SharedUserSetting usr : this.mSharedUsers.values()) {
                serializer.startTag(null, TAG_SHARED_USER);
                serializer.attribute(null, ATTR_NAME, usr.name);
                serializer.attribute(null, "userId", Integer.toString(usr.userId));
                usr.signatures.writeXml(serializer, "sigs", this.mPastSignatures);
                writePermissionsLPr(serializer, usr.getPermissionsState().getInstallPermissionStates());
                serializer.endTag(null, TAG_SHARED_USER);
            }
            if (this.mPackagesToBeCleaned.size() > 0) {
                Iterator<PackageCleanItem> it = this.mPackagesToBeCleaned.iterator();
                while (it.hasNext()) {
                    PackageCleanItem item = it.next();
                    String userStr = Integer.toString(item.userId);
                    serializer.startTag(null, "cleaning-package");
                    serializer.attribute(null, ATTR_NAME, item.packageName);
                    serializer.attribute(null, ATTR_CODE, item.andCode ? "true" : "false");
                    serializer.attribute(null, ATTR_USER, userStr);
                    serializer.endTag(null, "cleaning-package");
                }
            }
            if (this.mRenamedPackages.size() > 0) {
                for (Map.Entry<String, String> e : this.mRenamedPackages.entrySet()) {
                    serializer.startTag(null, "renamed-package");
                    serializer.attribute(null, "new", e.getKey());
                    serializer.attribute(null, "old", e.getValue());
                    serializer.endTag(null, "renamed-package");
                }
            }
            int numIVIs = this.mRestoredIntentFilterVerifications.size();
            if (numIVIs > 0) {
                if (PackageManagerService.DEBUG_DOMAIN_VERIFICATION) {
                    Slog.i(TAG, "Writing restored-ivi entries to packages.xml");
                }
                serializer.startTag(null, "restored-ivi");
                for (int i2 = 0; i2 < numIVIs; i2++) {
                    writeDomainVerificationsLPr(serializer, this.mRestoredIntentFilterVerifications.valueAt(i2));
                }
                serializer.endTag(null, "restored-ivi");
            } else if (PackageManagerService.DEBUG_DOMAIN_VERIFICATION) {
                Slog.i(TAG, "  no restored IVI entries to write");
            }
            this.mKeySetManagerService.writeKeySetManagerServiceLPr(serializer);
            serializer.endTag(null, "packages");
            serializer.endDocument();
            str.flush();
            FileUtils.sync(fstr);
            str.close();
            this.mBackupSettingsFilename.delete();
            FileUtils.setPermissions(this.mSettingsFilename.toString(), 432, -1, -1);
            writeKernelMappingLPr();
            writePackageListLPr();
            writeAllUsersPackageRestrictionsLPr();
            writeAllRuntimePermissionsLPr();
            EventLogTags.writeCommitSysConfigFile("package", SystemClock.uptimeMillis() - startTime);
        } catch (IOException e2) {
            Slog.wtf("PackageManager", "Unable to write package manager settings, current changes will be lost at reboot", e2);
            if (this.mSettingsFilename.exists() && !this.mSettingsFilename.delete()) {
                Slog.wtf("PackageManager", "Failed to clean up mangled file: " + this.mSettingsFilename);
            }
        }
    }

    private void writeKernelRemoveUserLPr(int userId) {
        if (this.mKernelMappingFilename != null) {
            writeIntToFile(new File(this.mKernelMappingFilename, "remove_userid"), userId);
        }
    }

    /* access modifiers changed from: package-private */
    public void writeKernelMappingLPr() {
        if (this.mKernelMappingFilename != null) {
            String[] known = this.mKernelMappingFilename.list();
            ArraySet<String> knownSet = new ArraySet<>(known.length);
            int i = 0;
            for (String name : known) {
                knownSet.add(name);
            }
            for (PackageSetting ps : this.mPackages.values()) {
                knownSet.remove(ps.name);
                writeKernelMappingLPr(ps);
            }
            while (true) {
                int i2 = i;
                if (i2 < knownSet.size()) {
                    String name2 = knownSet.valueAt(i2);
                    this.mKernelMapping.remove(name2);
                    new File(this.mKernelMappingFilename, name2).delete();
                    i = i2 + 1;
                } else {
                    return;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void writeKernelMappingLPr(PackageSetting ps) {
        if (this.mKernelMappingFilename != null && ps != null && ps.name != null) {
            KernelPackageState cur = this.mKernelMapping.get(ps.name);
            boolean userIdsChanged = true;
            boolean firstTime = cur == null;
            int[] excludedUserIds = ps.getNotInstalledUserIds();
            if (!firstTime && Arrays.equals(excludedUserIds, cur.excludedUserIds)) {
                userIdsChanged = false;
            }
            File dir = new File(this.mKernelMappingFilename, ps.name);
            if (firstTime) {
                dir.mkdir();
                cur = new KernelPackageState();
                this.mKernelMapping.put(ps.name, cur);
            }
            if (cur.appId != ps.appId) {
                writeIntToFile(new File(dir, "appid"), ps.appId);
            }
            if (userIdsChanged) {
                for (int i = 0; i < excludedUserIds.length; i++) {
                    if (cur.excludedUserIds == null || !ArrayUtils.contains(cur.excludedUserIds, excludedUserIds[i])) {
                        writeIntToFile(new File(dir, "excluded_userids"), excludedUserIds[i]);
                    }
                }
                if (cur.excludedUserIds != null) {
                    for (int i2 = 0; i2 < cur.excludedUserIds.length; i2++) {
                        if (!ArrayUtils.contains(excludedUserIds, cur.excludedUserIds[i2])) {
                            writeIntToFile(new File(dir, "clear_userid"), cur.excludedUserIds[i2]);
                        }
                    }
                }
                cur.excludedUserIds = excludedUserIds;
            }
        }
    }

    private void writeIntToFile(File file, int value) {
        try {
            FileUtils.bytesToFile(file.getAbsolutePath(), Integer.toString(value).getBytes(StandardCharsets.US_ASCII));
        } catch (IOException e) {
            Slog.w(TAG, "Couldn't write " + value + " to " + file.getAbsolutePath());
        }
    }

    /* access modifiers changed from: package-private */
    public void writePackageListLPr() {
        writePackageListLPr(-1);
    }

    /* access modifiers changed from: package-private */
    public void writePackageListLPr(int creatingUserId) {
        int i = creatingUserId;
        List<UserInfo> users = UserManagerService.getInstance().getUsers(true);
        int[] userIds = new int[users.size()];
        for (int i2 = 0; i2 < userIds.length; i2++) {
            userIds[i2] = users.get(i2).id;
        }
        if (i != -1) {
            userIds = ArrayUtils.appendInt(userIds, i);
        }
        int[] userIds2 = userIds;
        JournaledFile journal = new JournaledFile(this.mPackageListFilename, new File(this.mPackageListFilename.getAbsolutePath() + ".tmp"));
        try {
            FileOutputStream fstr = new FileOutputStream(journal.chooseForWrite());
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fstr, Charset.defaultCharset()));
            FileUtils.setPermissions(fstr.getFD(), 416, 1000, 1032);
            StringBuilder sb = new StringBuilder();
            for (PackageSetting pkg : this.mPackages.values()) {
                if (!(pkg.pkg == null || pkg.pkg.applicationInfo == null)) {
                    if (pkg.pkg.applicationInfo.dataDir != null) {
                        ApplicationInfo ai = pkg.pkg.applicationInfo;
                        String dataPath = ai.dataDir;
                        boolean isDebug = (ai.flags & 2) != 0;
                        int[] gids = pkg.getPermissionsState().computeGids(userIds2);
                        if (dataPath.indexOf(32) < 0) {
                            sb.setLength(0);
                            sb.append(ai.packageName);
                            sb.append(" ");
                            sb.append(ai.uid);
                            sb.append(isDebug ? " 1 " : " 0 ");
                            sb.append(dataPath);
                            sb.append(" ");
                            sb.append(ai.seInfo);
                            sb.append(" ");
                            if (gids == null || gids.length <= 0) {
                                sb.append("none");
                            } else {
                                sb.append(gids[0]);
                                for (int i3 = 1; i3 < gids.length; i3++) {
                                    sb.append(",");
                                    sb.append(gids[i3]);
                                }
                            }
                            sb.append("\n");
                            writer.append(sb);
                            int i4 = creatingUserId;
                        }
                    }
                }
                if (!PackageManagerService.PLATFORM_PACKAGE_NAME.equals(pkg.name)) {
                    Slog.w(TAG, "Skipping " + pkg + " due to missing metadata");
                }
                int i42 = creatingUserId;
            }
            writer.flush();
            FileUtils.sync(fstr);
            writer.close();
            journal.commit();
        } catch (Exception e) {
            Slog.wtf(TAG, "Failed to write packages.list", e);
            IoUtils.closeQuietly(null);
            journal.rollback();
        }
    }

    /* access modifiers changed from: package-private */
    public void writeDisabledSysPackageLPr(XmlSerializer serializer, PackageSetting pkg) throws IOException {
        serializer.startTag(null, "updated-package");
        serializer.attribute(null, ATTR_NAME, pkg.name);
        if (pkg.realName != null) {
            serializer.attribute(null, "realName", pkg.realName);
        }
        serializer.attribute(null, "codePath", pkg.codePathString);
        serializer.attribute(null, "ft", Long.toHexString(pkg.timeStamp));
        serializer.attribute(null, "it", Long.toHexString(pkg.firstInstallTime));
        serializer.attribute(null, "ut", Long.toHexString(pkg.lastUpdateTime));
        serializer.attribute(null, "version", String.valueOf(pkg.versionCode));
        if (!pkg.resourcePathString.equals(pkg.codePathString)) {
            serializer.attribute(null, "resourcePath", pkg.resourcePathString);
        }
        if (pkg.legacyNativeLibraryPathString != null) {
            serializer.attribute(null, "nativeLibraryPath", pkg.legacyNativeLibraryPathString);
        }
        if (pkg.primaryCpuAbiString != null) {
            serializer.attribute(null, "primaryCpuAbi", pkg.primaryCpuAbiString);
        }
        if (pkg.secondaryCpuAbiString != null) {
            serializer.attribute(null, "secondaryCpuAbi", pkg.secondaryCpuAbiString);
        }
        if (pkg.cpuAbiOverrideString != null) {
            serializer.attribute(null, "cpuAbiOverride", pkg.cpuAbiOverrideString);
        }
        if (pkg.sharedUser == null) {
            serializer.attribute(null, "userId", Integer.toString(pkg.appId));
        } else {
            serializer.attribute(null, "sharedUserId", Integer.toString(pkg.appId));
        }
        if (pkg.parentPackageName != null) {
            serializer.attribute(null, "parentPackageName", pkg.parentPackageName);
        }
        writeChildPackagesLPw(serializer, pkg.childPackageNames);
        writeUsesStaticLibLPw(serializer, pkg.usesStaticLibraries, pkg.usesStaticLibrariesVersions);
        if (pkg.sharedUser == null) {
            writePermissionsLPr(serializer, pkg.getPermissionsState().getInstallPermissionStates());
        }
        serializer.endTag(null, "updated-package");
    }

    /* access modifiers changed from: package-private */
    public void writePackageLPr(XmlSerializer serializer, PackageSetting pkg) throws IOException {
        serializer.startTag(null, "package");
        serializer.attribute(null, ATTR_NAME, pkg.name);
        if (pkg.realName != null) {
            serializer.attribute(null, "realName", pkg.realName);
        }
        serializer.attribute(null, "codePath", pkg.codePathString);
        if (!pkg.resourcePathString.equals(pkg.codePathString)) {
            serializer.attribute(null, "resourcePath", pkg.resourcePathString);
        }
        if (pkg.legacyNativeLibraryPathString != null) {
            serializer.attribute(null, "nativeLibraryPath", pkg.legacyNativeLibraryPathString);
        }
        if (pkg.primaryCpuAbiString != null) {
            serializer.attribute(null, "primaryCpuAbi", pkg.primaryCpuAbiString);
        }
        if (pkg.secondaryCpuAbiString != null) {
            serializer.attribute(null, "secondaryCpuAbi", pkg.secondaryCpuAbiString);
        }
        if (pkg.cpuAbiOverrideString != null) {
            serializer.attribute(null, "cpuAbiOverride", pkg.cpuAbiOverrideString);
        }
        serializer.attribute(null, "publicFlags", Integer.toString(pkg.pkgFlags));
        serializer.attribute(null, "privateFlags", Integer.toString(pkg.pkgPrivateFlags));
        serializer.attribute(null, "ft", Long.toHexString(pkg.timeStamp));
        serializer.attribute(null, "it", Long.toHexString(pkg.firstInstallTime));
        serializer.attribute(null, "ut", Long.toHexString(pkg.lastUpdateTime));
        serializer.attribute(null, "version", String.valueOf(pkg.versionCode));
        if (pkg.sharedUser == null) {
            serializer.attribute(null, "userId", Integer.toString(pkg.appId));
        } else {
            serializer.attribute(null, "sharedUserId", Integer.toString(pkg.appId));
        }
        if (pkg.uidError) {
            serializer.attribute(null, "uidError", "true");
        }
        if (pkg.installerPackageName != null) {
            serializer.attribute(null, "installer", pkg.installerPackageName);
        }
        if (pkg.maxAspectRatio > 0.0f) {
            serializer.attribute(null, "maxAspectRatio", Float.toString(pkg.maxAspectRatio));
        }
        serializer.attribute(null, "minAspectRatio", Float.toString(pkg.minAspectRatio));
        serializer.attribute(null, "appUseNotchMode", Integer.toString(pkg.appUseNotchMode));
        if (pkg.isOrphaned) {
            serializer.attribute(null, "isOrphaned", "true");
        }
        if (pkg.volumeUuid != null) {
            serializer.attribute(null, ATTR_VOLUME_UUID, pkg.volumeUuid);
        }
        if (pkg.categoryHint != -1) {
            serializer.attribute(null, "categoryHint", Integer.toString(pkg.categoryHint));
        }
        if (pkg.parentPackageName != null) {
            serializer.attribute(null, "parentPackageName", pkg.parentPackageName);
        }
        if (pkg.updateAvailable) {
            serializer.attribute(null, "updateAvailable", "true");
        }
        writeChildPackagesLPw(serializer, pkg.childPackageNames);
        writeUsesStaticLibLPw(serializer, pkg.usesStaticLibraries, pkg.usesStaticLibrariesVersions);
        if (pkg.pkg == null || !pkg.pkg.mRealSigningDetails.hasSignatures()) {
            pkg.signatures.writeXml(serializer, "sigs", this.mPastSignatures);
        } else {
            new PackageSignatures(pkg.pkg.mRealSigningDetails).writeXml(serializer, "sigs", this.mPastSignatures);
        }
        writePermissionsLPr(serializer, pkg.getPermissionsState().getInstallPermissionStates());
        writeSigningKeySetLPr(serializer, pkg.keySetData);
        writeUpgradeKeySetsLPr(serializer, pkg.keySetData);
        writeKeySetAliasesLPr(serializer, pkg.keySetData);
        writeDomainVerificationsLPr(serializer, pkg.verificationInfo);
        serializer.endTag(null, "package");
    }

    /* access modifiers changed from: package-private */
    public void writeSigningKeySetLPr(XmlSerializer serializer, PackageKeySetData data) throws IOException {
        serializer.startTag(null, "proper-signing-keyset");
        serializer.attribute(null, "identifier", Long.toString(data.getProperSigningKeySet()));
        serializer.endTag(null, "proper-signing-keyset");
    }

    /* access modifiers changed from: package-private */
    public void writeUpgradeKeySetsLPr(XmlSerializer serializer, PackageKeySetData data) throws IOException {
        if (data.isUsingUpgradeKeySets()) {
            for (long id : data.getUpgradeKeySets()) {
                serializer.startTag(null, "upgrade-keyset");
                serializer.attribute(null, "identifier", Long.toString(id));
                serializer.endTag(null, "upgrade-keyset");
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void writeKeySetAliasesLPr(XmlSerializer serializer, PackageKeySetData data) throws IOException {
        for (Map.Entry<String, Long> e : data.getAliases().entrySet()) {
            serializer.startTag(null, "defined-keyset");
            serializer.attribute(null, "alias", e.getKey());
            serializer.attribute(null, "identifier", Long.toString(e.getValue().longValue()));
            serializer.endTag(null, "defined-keyset");
        }
    }

    /* access modifiers changed from: package-private */
    public void writePermissionLPr(XmlSerializer serializer, BasePermission bp) throws IOException {
        bp.writeLPr(serializer);
    }

    /* access modifiers changed from: package-private */
    public void addPackageToCleanLPw(PackageCleanItem pkg) {
        if (!this.mPackagesToBeCleaned.contains(pkg)) {
            this.mPackagesToBeCleaned.add(pkg);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isPackageSettingsError() {
        return this.mIsPackageSettingsError || "1".equals(SystemProperties.get(KEY_PACKAGE_SETTINS_ERROR, "0"));
    }

    static void setPackageSettingsError() {
        SystemProperties.set(KEY_PACKAGE_SETTINS_ERROR, "1");
    }

    static void resetPackageSettingsError() {
        SystemProperties.set(KEY_PACKAGE_SETTINS_ERROR, "0");
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x00c3 A[Catch:{ XmlPullParserException -> 0x00a5, IOException -> 0x00a2, Exception -> 0x009f }] */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x00d8 A[Catch:{ XmlPullParserException -> 0x00a5, IOException -> 0x00a2, Exception -> 0x009f }] */
    public boolean readLPw(List<UserInfo> users) {
        int type;
        BufferedInputStream str = null;
        int i = 4;
        if (this.mBackupSettingsFilename.exists()) {
            try {
                str = new BufferedInputStream(new FileInputStream(this.mBackupSettingsFilename), DumpState.DUMP_COMPILER_STATS);
                this.mReadMessages.append("Reading from backup settings file\n");
                PackageManagerService.reportSettingsProblem(4, "Need to read from backup settings file");
                if (this.mSettingsFilename.exists()) {
                    Slog.w("PackageManager", "Cleaning up settings file " + this.mSettingsFilename);
                    this.mSettingsFilename.delete();
                }
            } catch (IOException e) {
            }
        }
        this.mPendingPackages.clear();
        this.mPastSignatures.clear();
        this.mKeySetRefs.clear();
        this.mInstallerPackages.clear();
        if (str == null) {
            try {
                if (!this.mSettingsFilename.exists()) {
                    this.mReadMessages.append("No settings file found\n");
                    PackageManagerService.reportSettingsProblem(4, "No settings file; creating initial state");
                    findOrCreateVersion(StorageManager.UUID_PRIVATE_INTERNAL).forceCurrent();
                    findOrCreateVersion("primary_physical").forceCurrent();
                    return false;
                }
                str = new BufferedInputStream(new FileInputStream(this.mSettingsFilename), DumpState.DUMP_COMPILER_STATS);
            } catch (XmlPullParserException e2) {
                HwPackageManagerServiceUtils.reportException(HwPackageManagerServiceUtils.EVENT_SETTINGS_EXCEPTION, e2.getMessage());
                this.mIsPackageSettingsError = true;
                this.mReadMessages.append("Error reading: " + e2.toString());
                PackageManagerService.reportSettingsProblem(6, "Error reading settings: " + e2);
                Slog.e("PackageManager", "Error reading package manager settings", e2);
                HwBootFail.brokenFileBootFail(83886084, "/data/system/packages.xml", new Throwable());
                this.mPendingPackages.clear();
                this.mPastSignatures.clear();
                this.mKeySetRefs.clear();
                this.mUserIds.clear();
                this.mSharedUsers.clear();
                this.mOtherUserIds.clear();
                this.mPackages.clear();
                addSharedUserLPw("android.uid.system", 1000, 1, 8);
                addSharedUserLPw("android.uid.phone", NetworkAgentInfo.EVENT_NETWORK_LINGER_COMPLETE, 1, 8);
                addSharedUserLPw("android.uid.log", 1007, 1, 8);
                addSharedUserLPw("android.uid.nfc", UsbTerminalTypes.TERMINAL_BIDIR_SKRPHONE, 1, 8);
                addSharedUserLPw("com.nxp.uid.nfceeapi", 1054, 1, 8);
                addSharedUserLPw("android.uid.bluetooth", 1002, 1, 8);
                addSharedUserLPw("android.uid.shell", IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME, 1, 8);
                addSharedUserLPw("android.uid.se", 1068, 1, 8);
                addSharedUserLPw("android.uid.hbs", 5508, 1, 8);
                findOrCreateVersion(StorageManager.UUID_PRIVATE_INTERNAL);
                findOrCreateVersion("primary_physical");
                return false;
            } catch (IOException e3) {
                HwPackageManagerServiceUtils.reportException(HwPackageManagerServiceUtils.EVENT_SETTINGS_EXCEPTION, e3.getMessage());
                this.mIsPackageSettingsError = true;
                this.mReadMessages.append("Error reading: " + e3.toString());
                PackageManagerService.reportSettingsProblem(6, "Error reading settings: " + e3);
                Slog.wtf("PackageManager", "Error reading package manager settings", e3);
            } catch (Exception e4) {
                HwPackageManagerServiceUtils.reportException(HwPackageManagerServiceUtils.EVENT_SETTINGS_EXCEPTION, e4.getMessage());
                this.mIsPackageSettingsError = true;
                this.mReadMessages.append("Error reading: " + e4.toString());
                PackageManagerService.reportSettingsProblem(6, "Error reading settings: " + e4);
                Log.wtf("PackageManager", "Error reading package manager settings", e4);
            }
        }
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(str, StandardCharsets.UTF_8.name());
        while (true) {
            int next = parser.next();
            type = next;
            if (next == 2 || type == 1) {
                if (type == 2) {
                    this.mReadMessages.append("No start tag found in settings file\n");
                    PackageManagerService.reportSettingsProblem(5, "No start tag found in package manager settings");
                    Slog.wtf("PackageManager", "No start tag found in package manager settings");
                    return false;
                }
                int outerDepth = parser.getDepth();
                while (true) {
                    int outerDepth2 = outerDepth;
                    int outerDepth3 = parser.next();
                    int type2 = outerDepth3;
                    if (outerDepth3 == 1 || (type2 == 3 && parser.getDepth() <= outerDepth2)) {
                        str.close();
                    } else {
                        if (type2 != 3) {
                            if (type2 != i) {
                                String tagName = parser.getName();
                                if (tagName.equals("package")) {
                                    readPackageLPw(parser);
                                } else if (tagName.equals("permissions")) {
                                    this.mPermissions.readPermissions(parser);
                                } else if (tagName.equals("permission-trees")) {
                                    this.mPermissions.readPermissionTrees(parser);
                                } else if (tagName.equals(TAG_SHARED_USER)) {
                                    readSharedUserLPw(parser);
                                } else if (!tagName.equals("preferred-packages")) {
                                    if (tagName.equals("preferred-activities")) {
                                        readPreferredActivitiesLPw(parser, 0);
                                    } else if (tagName.equals(TAG_PERSISTENT_PREFERRED_ACTIVITIES)) {
                                        readPersistentPreferredActivitiesLPw(parser, 0);
                                    } else if (tagName.equals(TAG_CROSS_PROFILE_INTENT_FILTERS)) {
                                        readCrossProfileIntentFiltersLPw(parser, 0);
                                    } else if (tagName.equals(TAG_DEFAULT_BROWSER)) {
                                        readDefaultAppsLPw(parser, 0);
                                    } else if (tagName.equals("updated-package")) {
                                        readDisabledSysPackageLPw(parser);
                                    } else if (tagName.equals("cleaning-package")) {
                                        String name = parser.getAttributeValue(null, ATTR_NAME);
                                        String userStr = parser.getAttributeValue(null, ATTR_USER);
                                        String codeStr = parser.getAttributeValue(null, ATTR_CODE);
                                        if (name != null) {
                                            int userId = 0;
                                            boolean andCode = true;
                                            if (userStr != null) {
                                                try {
                                                    userId = Integer.parseInt(userStr);
                                                } catch (NumberFormatException e5) {
                                                }
                                            }
                                            int userId2 = userId;
                                            if (codeStr != null) {
                                                andCode = Boolean.parseBoolean(codeStr);
                                            }
                                            addPackageToCleanLPw(new PackageCleanItem(userId2, name, andCode));
                                        }
                                    } else if (tagName.equals("renamed-package")) {
                                        String nname = parser.getAttributeValue(null, "new");
                                        String oname = parser.getAttributeValue(null, "old");
                                        if (!(nname == null || oname == null)) {
                                            this.mRenamedPackages.put(nname, oname);
                                        }
                                    } else if (tagName.equals("restored-ivi")) {
                                        readRestoredIntentFilterVerifications(parser);
                                    } else if (tagName.equals("last-platform-version")) {
                                        VersionInfo internal = findOrCreateVersion(StorageManager.UUID_PRIVATE_INTERNAL);
                                        VersionInfo external = findOrCreateVersion("primary_physical");
                                        internal.sdkVersion = XmlUtils.readIntAttribute(parser, "internal", 0);
                                        external.sdkVersion = XmlUtils.readIntAttribute(parser, "external", 0);
                                        String readStringAttribute = XmlUtils.readStringAttribute(parser, ATTR_FINGERPRINT);
                                        external.fingerprint = readStringAttribute;
                                        internal.fingerprint = readStringAttribute;
                                        String readStringAttribute2 = XmlUtils.readStringAttribute(parser, ATTR_HWFINGERPRINT);
                                        external.hwFingerprint = readStringAttribute2;
                                        internal.hwFingerprint = readStringAttribute2;
                                    } else if (tagName.equals("database-version")) {
                                        VersionInfo internal2 = findOrCreateVersion(StorageManager.UUID_PRIVATE_INTERNAL);
                                        VersionInfo external2 = findOrCreateVersion("primary_physical");
                                        internal2.databaseVersion = XmlUtils.readIntAttribute(parser, "internal", 0);
                                        external2.databaseVersion = XmlUtils.readIntAttribute(parser, "external", 0);
                                    } else if (tagName.equals("verifier")) {
                                        try {
                                            this.mVerifierDeviceIdentity = VerifierDeviceIdentity.parse(parser.getAttributeValue(null, "device"));
                                        } catch (IllegalArgumentException e6) {
                                            Slog.w("PackageManager", "Discard invalid verifier device id: " + e6.getMessage());
                                        }
                                    } else if (TAG_READ_EXTERNAL_STORAGE.equals(tagName)) {
                                        this.mReadExternalStorageEnforced = "1".equals(parser.getAttributeValue(null, ATTR_ENFORCEMENT)) ? Boolean.TRUE : Boolean.FALSE;
                                    } else if (tagName.equals("keyset-settings")) {
                                        this.mKeySetManagerService.readKeySetsLPw(parser, this.mKeySetRefs);
                                    } else if ("version".equals(tagName)) {
                                        VersionInfo ver = findOrCreateVersion(XmlUtils.readStringAttribute(parser, ATTR_VOLUME_UUID));
                                        ver.sdkVersion = XmlUtils.readIntAttribute(parser, ATTR_SDK_VERSION);
                                        ver.databaseVersion = XmlUtils.readIntAttribute(parser, ATTR_DATABASE_VERSION);
                                        ver.fingerprint = XmlUtils.readStringAttribute(parser, ATTR_FINGERPRINT);
                                        ver.hwFingerprint = XmlUtils.readStringAttribute(parser, ATTR_HWFINGERPRINT);
                                    } else {
                                        Slog.w("PackageManager", "Unknown element under <packages>: " + parser.getName());
                                        XmlUtils.skipCurrentTag(parser);
                                    }
                                }
                            }
                        }
                        outerDepth = outerDepth2;
                        i = 4;
                    }
                }
                str.close();
                int N = this.mPendingPackages.size();
                for (int i2 = 0; i2 < N; i2++) {
                    PackageSetting p = this.mPendingPackages.get(i2);
                    Object idObj = getUserIdLPr(p.getSharedUserId());
                    if (idObj instanceof SharedUserSetting) {
                        SharedUserSetting sharedUser = (SharedUserSetting) idObj;
                        p.sharedUser = sharedUser;
                        p.appId = sharedUser.userId;
                        addPackageSettingLPw(p, sharedUser);
                    } else if (idObj != null) {
                        String msg = "Bad package setting: package " + p.name + " has shared uid " + sharedUserId + " that is not a shared uid\n";
                        this.mReadMessages.append(msg);
                        PackageManagerService.reportSettingsProblem(6, msg);
                    } else {
                        String msg2 = "Bad package setting: package " + p.name + " has shared uid " + sharedUserId + " that is not defined\n";
                        this.mReadMessages.append(msg2);
                        PackageManagerService.reportSettingsProblem(6, msg2);
                    }
                }
                this.mPendingPackages.clear();
                if (this.mBackupStoppedPackagesFilename.exists() || this.mStoppedPackagesFilename.exists()) {
                    readStoppedLPw();
                    this.mBackupStoppedPackagesFilename.delete();
                    this.mStoppedPackagesFilename.delete();
                    writePackageRestrictionsLPr(0);
                } else {
                    for (UserInfo user : users) {
                        readPackageRestrictionsLPr(user.id);
                    }
                }
                try {
                    for (UserInfo user2 : users) {
                        this.mRuntimePermissionsPersistence.readStateForUserSyncLPr(user2.id);
                    }
                } catch (IllegalStateException e7) {
                    HwBootFail.brokenFileBootFail(83886085, "/data/system/users/0/runtime-permissions.xml", new Throwable());
                    Log.wtf("PackageManager", "Error reading state for user", e7);
                }
                for (PackageSetting disabledPs : this.mDisabledSysPackages.values()) {
                    Object id = getUserIdLPr(disabledPs.appId);
                    if (id != null && (id instanceof SharedUserSetting)) {
                        disabledPs.sharedUser = (SharedUserSetting) id;
                    }
                }
                this.mReadMessages.append("Read completed successfully: " + this.mPackages.size() + " packages, " + this.mSharedUsers.size() + " shared uids\n");
                writeKernelMappingLPr();
                return true;
            }
        }
        if (type == 2) {
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x011d A[Catch:{ XmlPullParserException -> 0x0194, IOException -> 0x0177, all -> 0x0175 }] */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x0140  */
    public void applyDefaultPreferredAppsLPw(PackageManagerService service, int userId) {
        int type;
        Iterator<PackageSetting> it = this.mPackages.values().iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            PackageSetting ps = it.next();
            if (!((1 & ps.pkgFlags) == 0 || ps.pkg == null || ps.pkg.preferredActivityFilters == null)) {
                ArrayList<PackageParser.ActivityIntentInfo> intents = ps.pkg.preferredActivityFilters;
                for (int i = 0; i < intents.size(); i++) {
                    PackageParser.ActivityIntentInfo aii = intents.get(i);
                    applyDefaultPreferredActivityLPw(service, aii, new ComponentName(ps.name, aii.activity.className), userId);
                }
            }
        }
        ArrayList<File> allFiles = new ArrayList<>();
        ArrayList<File> custDirs = HwCfgFilePolicy.getCfgFileList("preferred-apps", 0);
        custDirs.add(new File(Environment.getRootDirectory(), "etc/preferred-apps"));
        Iterator<File> it2 = custDirs.iterator();
        while (it2.hasNext()) {
            File dir = it2.next();
            if (dir.isDirectory() && dir.canRead()) {
                Collections.addAll(allFiles, dir.listFiles());
            }
        }
        Iterator<File> it3 = allFiles.iterator();
        while (it3.hasNext()) {
            File f = it3.next();
            if (!f.getPath().endsWith(".xml")) {
                Slog.i(TAG, "Non-xml file " + f + ", ignoring");
            } else if (!f.canRead()) {
                Slog.w(TAG, "Preferred apps file " + f + " cannot be read");
            } else {
                if (PackageManagerService.DEBUG_PREFERRED) {
                    Log.d(TAG, "Reading default preferred " + f);
                }
                InputStream str = null;
                try {
                    str = new BufferedInputStream(new FileInputStream(f));
                    XmlPullParser parser = Xml.newPullParser();
                    parser.setInput(str, null);
                    while (true) {
                        int next = parser.next();
                        type = next;
                        if (next == 2 || type == 1) {
                            if (type == 2) {
                                Slog.w(TAG, "Preferred apps file " + f + " does not have start tag");
                                try {
                                    str.close();
                                } catch (IOException e) {
                                }
                            } else if (!"preferred-activities".equals(parser.getName())) {
                                Slog.w(TAG, "Preferred apps file " + f + " does not start with 'preferred-activities'");
                                str.close();
                            } else {
                                readDefaultPreferredActivitiesLPw(service, parser, userId);
                                try {
                                    str.close();
                                } catch (IOException e2) {
                                }
                            }
                        }
                    }
                    if (type == 2) {
                    }
                } catch (XmlPullParserException e3) {
                    Slog.w(TAG, "Error reading apps file " + f, e3);
                    if (str != null) {
                        str.close();
                    }
                } catch (IOException e4) {
                    Slog.w(TAG, "Error reading apps file " + f, e4);
                    if (str != null) {
                        str.close();
                    }
                } catch (Throwable th) {
                    if (str != null) {
                        try {
                            str.close();
                        } catch (IOException e5) {
                        }
                    }
                    throw th;
                }
            }
        }
    }

    private void applyDefaultPreferredActivityLPw(PackageManagerService service, IntentFilter tmpPa, ComponentName cn, int userId) {
        Uri.Builder builder;
        int ischeme;
        boolean doAuth;
        IntentFilter intentFilter = tmpPa;
        if (PackageManagerService.DEBUG_PREFERRED) {
            Log.d(TAG, "Processing preferred:");
            intentFilter.dump(new LogPrinter(3, TAG), "  ");
        }
        Intent intent = new Intent();
        int i = 0;
        intent.setAction(intentFilter.getAction(0));
        int flags = 786432;
        for (int i2 = 0; i2 < tmpPa.countCategories(); i2++) {
            String cat = intentFilter.getCategory(i2);
            if (cat.equals("android.intent.category.DEFAULT")) {
                flags |= 65536;
            } else {
                intent.addCategory(cat);
            }
        }
        boolean doNonData = true;
        int ischeme2 = 0;
        boolean hasSchemes = false;
        while (ischeme2 < tmpPa.countDataSchemes()) {
            String scheme = intentFilter.getDataScheme(ischeme2);
            if (scheme != null && !scheme.isEmpty()) {
                hasSchemes = true;
            }
            boolean doAuth2 = true;
            int issp = i;
            while (true) {
                int issp2 = issp;
                if (issp2 >= tmpPa.countDataSchemeSpecificParts()) {
                    break;
                }
                Uri.Builder builder2 = new Uri.Builder();
                builder2.scheme(scheme);
                PatternMatcher ssp = intentFilter.getDataSchemeSpecificPart(issp2);
                builder2.opaquePart(ssp.getPath());
                Intent finalIntent = new Intent(intent);
                finalIntent.setData(builder2.build());
                Intent intent2 = finalIntent;
                PatternMatcher patternMatcher = ssp;
                Uri.Builder builder3 = builder2;
                applyDefaultPreferredActivityLPw(service, finalIntent, flags, cn, scheme, ssp, null, null, userId);
                doAuth2 = false;
                issp = issp2 + 1;
                scheme = scheme;
            }
            String scheme2 = scheme;
            int iauth = 0;
            while (true) {
                int iauth2 = iauth;
                if (iauth2 >= tmpPa.countDataAuthorities()) {
                    break;
                }
                IntentFilter.AuthorityEntry auth = intentFilter.getDataAuthority(iauth2);
                boolean doScheme = doAuth;
                boolean doAuth3 = true;
                int ipath = 0;
                while (true) {
                    int ipath2 = ipath;
                    if (ipath2 >= tmpPa.countDataPaths()) {
                        break;
                    }
                    Uri.Builder builder4 = new Uri.Builder();
                    builder4.scheme(scheme2);
                    if (auth.getHost() != null) {
                        builder4.authority(auth.getHost());
                    }
                    PatternMatcher path = intentFilter.getDataPath(ipath2);
                    builder4.path(path.getPath());
                    Intent finalIntent2 = new Intent(intent);
                    finalIntent2.setData(builder4.build());
                    Intent intent3 = finalIntent2;
                    Uri.Builder builder5 = builder4;
                    applyDefaultPreferredActivityLPw(service, finalIntent2, flags, cn, scheme2, null, auth, path, userId);
                    doScheme = false;
                    doAuth3 = false;
                    ipath = ipath2 + 1;
                    auth = auth;
                    iauth2 = iauth2;
                }
                IntentFilter.AuthorityEntry auth2 = auth;
                int iauth3 = iauth2;
                if (doAuth3) {
                    Uri.Builder builder6 = new Uri.Builder();
                    builder6.scheme(scheme2);
                    IntentFilter.AuthorityEntry auth3 = auth2;
                    if (auth3.getHost() != null) {
                        builder6.authority(auth3.getHost());
                    }
                    Intent finalIntent3 = new Intent(intent);
                    finalIntent3.setData(builder6.build());
                    Intent intent4 = finalIntent3;
                    IntentFilter.AuthorityEntry authorityEntry = auth3;
                    Uri.Builder builder7 = builder6;
                    applyDefaultPreferredActivityLPw(service, finalIntent3, flags, cn, scheme2, null, auth3, null, userId);
                    doAuth = false;
                } else {
                    doAuth = doScheme;
                }
                iauth = iauth3 + 1;
            }
            if (doAuth) {
                Uri.Builder builder8 = new Uri.Builder();
                builder8.scheme(scheme2);
                Intent finalIntent4 = new Intent(intent);
                finalIntent4.setData(builder8.build());
                Intent intent5 = finalIntent4;
                Uri.Builder builder9 = builder8;
                applyDefaultPreferredActivityLPw(service, finalIntent4, flags, cn, scheme2, null, null, null, userId);
            }
            doNonData = false;
            ischeme2++;
            i = 0;
        }
        int i3 = i;
        for (int idata = i3; idata < tmpPa.countDataTypes(); idata++) {
            String mimeType = intentFilter.getDataType(idata);
            if (hasSchemes) {
                Uri.Builder builder10 = new Uri.Builder();
                int ischeme3 = i3;
                while (true) {
                    int ischeme4 = ischeme3;
                    if (ischeme4 >= tmpPa.countDataSchemes()) {
                        break;
                    }
                    String scheme3 = intentFilter.getDataScheme(ischeme4);
                    if (scheme3 == null || scheme3.isEmpty()) {
                        ischeme = ischeme4;
                        builder = builder10;
                    } else {
                        Intent finalIntent5 = new Intent(intent);
                        builder10.scheme(scheme3);
                        finalIntent5.setDataAndType(builder10.build(), mimeType);
                        Intent intent6 = finalIntent5;
                        String str = scheme3;
                        ischeme = ischeme4;
                        builder = builder10;
                        applyDefaultPreferredActivityLPw(service, finalIntent5, flags, cn, scheme3, null, null, null, userId);
                    }
                    ischeme3 = ischeme + 1;
                    builder10 = builder;
                }
            } else {
                Intent finalIntent6 = new Intent(intent);
                finalIntent6.setType(mimeType);
                Intent intent7 = finalIntent6;
                applyDefaultPreferredActivityLPw(service, finalIntent6, flags, cn, null, null, null, null, userId);
            }
            doNonData = false;
        }
        if (doNonData) {
            applyDefaultPreferredActivityLPw(service, intent, flags, cn, null, null, null, null, userId);
        }
    }

    private void applyDefaultPreferredActivityLPw(PackageManagerService service, Intent intent, int flags, ComponentName cn, String scheme, PatternMatcher ssp, IntentFilter.AuthorityEntry auth, PatternMatcher path, int userId) {
        ComponentName componentName;
        Intent intent2 = intent;
        String str = scheme;
        IntentFilter.AuthorityEntry authorityEntry = auth;
        PatternMatcher patternMatcher = path;
        int flags2 = service.updateFlagsForResolve(flags, userId, intent2, Binder.getCallingUid(), false);
        List<ResolveInfo> ri = service.mActivities.queryIntent(intent2, intent.getType(), flags2, 0);
        if (PackageManagerService.DEBUG_PREFERRED) {
            Log.d(TAG, "Queried " + intent2 + " results: " + ri);
        }
        if (ri == null || ri.size() <= 1) {
            int i = userId;
            int i2 = flags2;
            List<ResolveInfo> list = ri;
            ComponentName componentName2 = cn;
            Slog.w(TAG, "No potential matches found for " + intent2 + " while setting preferred " + cn.flattenToShortString());
            return;
        }
        boolean haveAct = false;
        ComponentName haveNonSys = null;
        ComponentName[] set = new ComponentName[ri.size()];
        int systemMatch = 0;
        int i3 = 0;
        while (true) {
            if (i3 >= ri.size()) {
                break;
            }
            ActivityInfo ai = ri.get(i3).activityInfo;
            set[i3] = new ComponentName(ai.packageName, ai.name);
            if ((ai.applicationInfo.flags & 1) == 0) {
                if (ri.get(i3).match >= 0) {
                    if (PackageManagerService.DEBUG_PREFERRED) {
                        Log.d(TAG, "Result " + ai.packageName + SliceClientPermissions.SliceAuthority.DELIMITER + ai.name + ": non-system!");
                    }
                    haveNonSys = set[i3];
                }
            } else if (cn.getPackageName().equals(ai.packageName) && cn.getClassName().equals(ai.name)) {
                if (PackageManagerService.DEBUG_PREFERRED) {
                    Log.d(TAG, "Result " + ai.packageName + SliceClientPermissions.SliceAuthority.DELIMITER + ai.name + ": default!");
                }
                haveAct = true;
                systemMatch = ri.get(i3).match;
            } else if (PackageManagerService.DEBUG_PREFERRED) {
                Log.d(TAG, "Result " + ai.packageName + SliceClientPermissions.SliceAuthority.DELIMITER + ai.name + ": skipped");
            }
            i3++;
            PackageManagerService packageManagerService = service;
        }
        if (haveNonSys != null && 0 < systemMatch) {
            haveNonSys = null;
        }
        if (!haveAct || haveNonSys != null) {
            int i4 = flags2;
            int i5 = systemMatch;
            ComponentName componentName3 = cn;
            int systemMatch2 = userId;
            if (haveNonSys == null) {
                StringBuilder sb = new StringBuilder();
                sb.append("No component ");
                sb.append(cn.flattenToShortString());
                sb.append(" found setting preferred ");
                sb.append(intent2);
                sb.append("; possible matches are ");
                int i6 = 0;
                while (true) {
                    int i7 = i6;
                    List<ResolveInfo> ri2 = ri;
                    if (i7 < set.length) {
                        if (i7 > 0) {
                            sb.append(", ");
                        }
                        sb.append(set[i7].flattenToShortString());
                        i6 = i7 + 1;
                        ri = ri2;
                    } else {
                        Slog.w(TAG, sb.toString());
                        return;
                    }
                }
            } else {
                Slog.i(TAG, "Not setting preferred " + intent2 + "; found third party match " + haveNonSys.flattenToShortString());
            }
        } else {
            IntentFilter filter = new IntentFilter();
            if (intent.getAction() != null) {
                filter.addAction(intent.getAction());
            }
            if (intent.getCategories() != null) {
                for (String cat : intent.getCategories()) {
                    filter.addCategory(cat);
                }
            }
            if ((65536 & flags2) != 0) {
                filter.addCategory("android.intent.category.DEFAULT");
            }
            if (str != null) {
                filter.addDataScheme(str);
            }
            if (ssp != null) {
                filter.addDataSchemeSpecificPart(ssp.getPath(), ssp.getType());
            }
            if (authorityEntry != null) {
                filter.addDataAuthority(authorityEntry);
            }
            if (patternMatcher != null) {
                filter.addDataPath(patternMatcher);
            }
            if (intent.getType() != null) {
                try {
                    filter.addDataType(intent.getType());
                    int i8 = flags2;
                    componentName = cn;
                } catch (IntentFilter.MalformedMimeTypeException ex) {
                    StringBuilder sb2 = new StringBuilder();
                    IntentFilter.MalformedMimeTypeException malformedMimeTypeException = ex;
                    sb2.append("Malformed mimetype ");
                    sb2.append(intent.getType());
                    sb2.append(" for ");
                    int i9 = flags2;
                    componentName = cn;
                    sb2.append(componentName);
                    Slog.w(TAG, sb2.toString());
                }
            } else {
                componentName = cn;
            }
            PreferredActivity preferredActivity = new PreferredActivity(filter, systemMatch, set, componentName, true);
            IntentFilter intentFilter = filter;
            editPreferredActivitiesLPw(userId).addFilter(preferredActivity);
            List<ResolveInfo> list2 = ri;
        }
    }

    private void readDefaultPreferredActivitiesLPw(PackageManagerService service, XmlPullParser parser, int userId) throws XmlPullParserException, IOException {
        int outerDepth = parser.getDepth();
        while (true) {
            int next = parser.next();
            int type = next;
            if (next == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == 3 || type == 4)) {
                if (parser.getName().equals(TAG_ITEM)) {
                    PreferredActivity tmpPa = new PreferredActivity(parser);
                    if (tmpPa.mPref.getParseError() == null) {
                        applyDefaultPreferredActivityLPw(service, tmpPa, tmpPa.mPref.mComponent, userId);
                    } else {
                        PackageManagerService.reportSettingsProblem(5, "Error in package manager settings: <preferred-activity> " + tmpPa.mPref.getParseError() + " at " + parser.getPositionDescription());
                    }
                } else {
                    PackageManagerService.reportSettingsProblem(5, "Unknown element under <preferred-activities>: " + parser.getName());
                    XmlUtils.skipCurrentTag(parser);
                }
            }
        }
    }

    private void readDisabledSysPackageLPw(XmlPullParser parser) throws XmlPullParserException, IOException {
        int pkgPrivateFlags;
        XmlPullParser xmlPullParser = parser;
        String name = xmlPullParser.getAttributeValue(null, ATTR_NAME);
        String realName = xmlPullParser.getAttributeValue(null, "realName");
        String codePathStr = xmlPullParser.getAttributeValue(null, "codePath");
        String resourcePathStr = xmlPullParser.getAttributeValue(null, "resourcePath");
        String legacyCpuAbiStr = xmlPullParser.getAttributeValue(null, "requiredCpuAbi");
        String legacyNativeLibraryPathStr = xmlPullParser.getAttributeValue(null, "nativeLibraryPath");
        String parentPackageName = xmlPullParser.getAttributeValue(null, "parentPackageName");
        String primaryCpuAbiStr = xmlPullParser.getAttributeValue(null, "primaryCpuAbi");
        String secondaryCpuAbiStr = xmlPullParser.getAttributeValue(null, "secondaryCpuAbi");
        String cpuAbiOverrideStr = xmlPullParser.getAttributeValue(null, "cpuAbiOverride");
        if (primaryCpuAbiStr == null && legacyCpuAbiStr != null) {
            primaryCpuAbiStr = legacyCpuAbiStr;
        }
        String primaryCpuAbiStr2 = primaryCpuAbiStr;
        if (resourcePathStr == null) {
            resourcePathStr = codePathStr;
        }
        String resourcePathStr2 = resourcePathStr;
        String version = xmlPullParser.getAttributeValue(null, "version");
        long versionCode = 0;
        if (version != null) {
            try {
                versionCode = Long.parseLong(version);
            } catch (NumberFormatException e) {
            }
        }
        long versionCode2 = versionCode;
        int pkgFlags = 0 | 1;
        if (PackageManagerService.locationIsPrivileged(codePathStr) != 0) {
            pkgPrivateFlags = 0 | 8;
        } else {
            pkgPrivateFlags = 0;
        }
        String str = version;
        String str2 = codePathStr;
        String str3 = resourcePathStr2;
        String name2 = name;
        PackageSetting ps = new PackageSetting(name, realName, new File(codePathStr), new File(resourcePathStr2), legacyNativeLibraryPathStr, primaryCpuAbiStr2, secondaryCpuAbiStr, cpuAbiOverrideStr, versionCode2, pkgFlags, pkgPrivateFlags, parentPackageName, null, 0, null, null);
        String timeStampStr = xmlPullParser.getAttributeValue(null, "ft");
        if (timeStampStr != null) {
            try {
                ps.setTimeStamp(Long.parseLong(timeStampStr, 16));
            } catch (NumberFormatException e2) {
            }
        } else {
            String timeStampStr2 = xmlPullParser.getAttributeValue(null, "ts");
            if (timeStampStr2 != null) {
                try {
                    ps.setTimeStamp(Long.parseLong(timeStampStr2));
                } catch (NumberFormatException e3) {
                }
            }
        }
        String timeStampStr3 = xmlPullParser.getAttributeValue(null, "it");
        if (timeStampStr3 != null) {
            try {
                ps.firstInstallTime = Long.parseLong(timeStampStr3, 16);
            } catch (NumberFormatException e4) {
            }
        }
        String timeStampStr4 = xmlPullParser.getAttributeValue(null, "ut");
        if (timeStampStr4 != null) {
            try {
                ps.lastUpdateTime = Long.parseLong(timeStampStr4, 16);
            } catch (NumberFormatException e5) {
            }
        }
        String idStr = xmlPullParser.getAttributeValue(null, "userId");
        int i = 0;
        ps.appId = idStr != null ? Integer.parseInt(idStr) : 0;
        if (ps.appId <= 0) {
            String sharedIdStr = xmlPullParser.getAttributeValue(null, "sharedUserId");
            if (sharedIdStr != null) {
                i = Integer.parseInt(sharedIdStr);
            }
            ps.appId = i;
        }
        int outerDepth = parser.getDepth();
        while (true) {
            int next = parser.next();
            int type = next;
            if (next == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                this.mDisabledSysPackages.put(name2, ps);
            } else if (!(type == 3 || type == 4)) {
                if (parser.getName().equals(TAG_PERMISSIONS)) {
                    readInstallPermissionsLPr(xmlPullParser, ps.getPermissionsState());
                } else if (parser.getName().equals(TAG_CHILD_PACKAGE)) {
                    String childPackageName = xmlPullParser.getAttributeValue(null, ATTR_NAME);
                    if (ps.childPackageNames == null) {
                        ps.childPackageNames = new ArrayList();
                    }
                    ps.childPackageNames.add(childPackageName);
                } else if (parser.getName().equals(TAG_USES_STATIC_LIB)) {
                    readUsesStaticLibLPw(xmlPullParser, ps);
                } else {
                    PackageManagerService.reportSettingsProblem(5, "Unknown element under <updated-package>: " + parser.getName());
                    XmlUtils.skipCurrentTag(parser);
                }
            }
        }
        this.mDisabledSysPackages.put(name2, ps);
    }

    /* JADX WARNING: Removed duplicated region for block: B:133:0x0198 A[SYNTHETIC, Splitter:B:133:0x0198] */
    /* JADX WARNING: Removed duplicated region for block: B:138:0x01a4  */
    /* JADX WARNING: Removed duplicated region for block: B:152:0x01c3  */
    /* JADX WARNING: Removed duplicated region for block: B:162:0x01da  */
    /* JADX WARNING: Removed duplicated region for block: B:171:0x01ea  */
    /* JADX WARNING: Removed duplicated region for block: B:179:0x0220 A[Catch:{ NumberFormatException -> 0x0229 }] */
    /* JADX WARNING: Removed duplicated region for block: B:181:0x0224 A[Catch:{ NumberFormatException -> 0x0229 }] */
    /* JADX WARNING: Removed duplicated region for block: B:186:0x0234 A[Catch:{ NumberFormatException -> 0x0229 }] */
    /* JADX WARNING: Removed duplicated region for block: B:188:0x0237 A[Catch:{ NumberFormatException -> 0x0229 }] */
    /* JADX WARNING: Removed duplicated region for block: B:189:0x023c  */
    /* JADX WARNING: Removed duplicated region for block: B:192:0x0240  */
    /* JADX WARNING: Removed duplicated region for block: B:193:0x0242  */
    /* JADX WARNING: Removed duplicated region for block: B:195:0x0246 A[SYNTHETIC, Splitter:B:195:0x0246] */
    /* JADX WARNING: Removed duplicated region for block: B:202:0x0256 A[SYNTHETIC, Splitter:B:202:0x0256] */
    /* JADX WARNING: Removed duplicated region for block: B:216:0x0298  */
    /* JADX WARNING: Removed duplicated region for block: B:405:0x08fe  */
    /* JADX WARNING: Removed duplicated region for block: B:418:0x0951  */
    /* JADX WARNING: Removed duplicated region for block: B:419:0x0959  */
    /* JADX WARNING: Removed duplicated region for block: B:429:0x09ad  */
    /* JADX WARNING: Removed duplicated region for block: B:432:0x09bb  */
    /* JADX WARNING: Removed duplicated region for block: B:437:0x09d0  */
    /* JADX WARNING: Removed duplicated region for block: B:440:0x09de  */
    /* JADX WARNING: Removed duplicated region for block: B:444:0x09f5  */
    /* JADX WARNING: Removed duplicated region for block: B:503:0x0be3  */
    /* JADX WARNING: Removed duplicated region for block: B:504:0x0bdc A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:65:0x00e0 A[SYNTHETIC, Splitter:B:65:0x00e0] */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x00f3 A[SYNTHETIC, Splitter:B:74:0x00f3] */
    /* JADX WARNING: Removed duplicated region for block: B:90:0x0126  */
    private void readPackageLPw(XmlPullParser parser) throws XmlPullParserException, IOException {
        Settings settings;
        String idStr;
        String timeStampStr;
        String version;
        String cpuAbiOverrideString;
        String secondaryCpuAbiString;
        String resourcePathStr;
        int categoryHint;
        String codePathStr;
        String idStr2;
        String idStr3;
        String uidError;
        PackageSetting packageSetting;
        String maxAspectRatioStr;
        String minAspectRatioStr;
        String appUseNotchModeStr;
        int outerDepth;
        int next;
        String primaryCpuAbiString;
        String legacyNativeLibraryPathStr;
        int outerDepth2;
        String appUseNotchModeStr2;
        String enabledStr;
        String installerPackageName;
        int i;
        String volumeUuid;
        String isOrphaned;
        String uidError2;
        String primaryCpuAbiString2;
        String installerPackageName2;
        int i2;
        String version2;
        String cpuAbiOverrideString2;
        String secondaryCpuAbiString2;
        String resourcePathStr2;
        String legacyNativeLibraryPathStr2;
        String realName;
        Settings settings2;
        String name;
        Settings settings3;
        String sharedIdStr;
        String resourcePathStr3;
        String name2;
        String version3;
        String resourcePathStr4;
        String str;
        String name3;
        String legacyCpuAbiString;
        String parentPackageName;
        String str2;
        String name4;
        Settings settings4;
        String str3;
        String name5;
        long versionCode;
        String str4;
        String name6;
        Settings settings5;
        String categoryHintString;
        String str5;
        String sharedIdStr2;
        int i3;
        String name7;
        int pkgFlags;
        String timeStampStr2;
        long timeStamp;
        String timeStampStr3;
        String timeStampStr4;
        String categoryHintString2;
        int userId;
        long firstInstallTime;
        long timeStamp2;
        String resourcePathStr5;
        long firstInstallTime2;
        Settings settings6;
        String sharedIdStr3;
        int i4;
        String name8;
        String idStr4;
        String codePathStr2;
        String sharedIdStr4;
        long j;
        long j2;
        String sharedIdStr5;
        String name9;
        String idStr5;
        String sharedIdStr6;
        String codePathStr3;
        String resourcePathStr6;
        String version4;
        long j3;
        String str6;
        long firstInstallTime3;
        long timeStamp3;
        long firstInstallTime4;
        String resourcePathStr7;
        String timeStampStr5;
        XmlPullParser xmlPullParser = parser;
        String name10 = null;
        String idStr6 = null;
        String sharedIdStr7 = null;
        String codePathStr4 = null;
        String systemStr = null;
        String updateAvailable = null;
        int categoryHint2 = -1;
        int pkgFlags2 = 0;
        int pkgPrivateFlags = 0;
        long timeStamp4 = 0;
        long firstInstallTime5 = 0;
        long lastUpdateTime = 0;
        PackageSetting packageSetting2 = null;
        try {
            name10 = xmlPullParser.getAttributeValue(null, ATTR_NAME);
            try {
                realName = xmlPullParser.getAttributeValue(null, "realName");
                idStr6 = xmlPullParser.getAttributeValue(null, "userId");
                try {
                    uidError2 = xmlPullParser.getAttributeValue(null, "uidError");
                    try {
                        sharedIdStr7 = xmlPullParser.getAttributeValue(null, "sharedUserId");
                        try {
                            codePathStr4 = xmlPullParser.getAttributeValue(null, "codePath");
                            try {
                                resourcePathStr4 = xmlPullParser.getAttributeValue(null, "resourcePath");
                                try {
                                    legacyCpuAbiString = xmlPullParser.getAttributeValue(null, "requiredCpuAbi");
                                    try {
                                        parentPackageName = xmlPullParser.getAttributeValue(null, "parentPackageName");
                                        legacyNativeLibraryPathStr2 = xmlPullParser.getAttributeValue(null, "nativeLibraryPath");
                                    } catch (NumberFormatException e) {
                                        str = name10;
                                        name3 = sharedIdStr7;
                                        String str7 = codePathStr4;
                                        settings3 = this;
                                        String sharedIdStr8 = idStr6;
                                        legacyNativeLibraryPathStr2 = null;
                                        secondaryCpuAbiString2 = null;
                                        isOrphaned = null;
                                        volumeUuid = null;
                                        version3 = null;
                                        resourcePathStr3 = resourcePathStr4;
                                        i2 = 5;
                                        sharedIdStr = name3;
                                        name2 = str;
                                        installerPackageName2 = null;
                                        primaryCpuAbiString2 = null;
                                        cpuAbiOverrideString2 = null;
                                        PackageManagerService.reportSettingsProblem(i2, "Error in package manager settings: package " + name10 + " has bad userId " + idStr6 + " at " + parser.getPositionDescription());
                                        timeStampStr = name10;
                                        idStr = idStr6;
                                        String str8 = sharedIdStr7;
                                        String str9 = codePathStr4;
                                        String str10 = resourcePathStr2;
                                        String str11 = cpuAbiOverrideString2;
                                        String str12 = version2;
                                        version = updateAvailable;
                                        packageSetting = packageSetting2;
                                        idStr3 = installerPackageName2;
                                        uidError = uidError2;
                                        idStr2 = isOrphaned;
                                        codePathStr = volumeUuid;
                                        resourcePathStr = legacyNativeLibraryPathStr2;
                                        cpuAbiOverrideString = secondaryCpuAbiString2;
                                        categoryHint = categoryHint2;
                                        secondaryCpuAbiString = primaryCpuAbiString2;
                                        if (packageSetting != null) {
                                        }
                                    }
                                } catch (NumberFormatException e2) {
                                    str = name10;
                                    name3 = sharedIdStr7;
                                    String str13 = codePathStr4;
                                    settings3 = this;
                                    String sharedIdStr9 = idStr6;
                                    legacyNativeLibraryPathStr2 = null;
                                    secondaryCpuAbiString2 = null;
                                    isOrphaned = null;
                                    volumeUuid = null;
                                    version3 = null;
                                    resourcePathStr3 = resourcePathStr4;
                                    i2 = 5;
                                    sharedIdStr = name3;
                                    name2 = str;
                                    installerPackageName2 = null;
                                    primaryCpuAbiString2 = null;
                                    cpuAbiOverrideString2 = null;
                                    PackageManagerService.reportSettingsProblem(i2, "Error in package manager settings: package " + name10 + " has bad userId " + idStr6 + " at " + parser.getPositionDescription());
                                    timeStampStr = name10;
                                    idStr = idStr6;
                                    String str82 = sharedIdStr7;
                                    String str92 = codePathStr4;
                                    String str102 = resourcePathStr2;
                                    String str112 = cpuAbiOverrideString2;
                                    String str122 = version2;
                                    version = updateAvailable;
                                    packageSetting = packageSetting2;
                                    idStr3 = installerPackageName2;
                                    uidError = uidError2;
                                    idStr2 = isOrphaned;
                                    codePathStr = volumeUuid;
                                    resourcePathStr = legacyNativeLibraryPathStr2;
                                    cpuAbiOverrideString = secondaryCpuAbiString2;
                                    categoryHint = categoryHint2;
                                    secondaryCpuAbiString = primaryCpuAbiString2;
                                    if (packageSetting != null) {
                                    }
                                }
                            } catch (NumberFormatException e3) {
                                String str14 = name10;
                                String name11 = sharedIdStr7;
                                String str15 = codePathStr4;
                                settings3 = this;
                                String sharedIdStr10 = idStr6;
                                isOrphaned = null;
                                volumeUuid = null;
                                i2 = 5;
                                sharedIdStr = name11;
                                String str16 = realName;
                                resourcePathStr3 = null;
                                legacyNativeLibraryPathStr2 = null;
                                secondaryCpuAbiString2 = null;
                                name2 = str14;
                                version3 = null;
                                installerPackageName2 = null;
                                primaryCpuAbiString2 = null;
                                cpuAbiOverrideString2 = null;
                                PackageManagerService.reportSettingsProblem(i2, "Error in package manager settings: package " + name10 + " has bad userId " + idStr6 + " at " + parser.getPositionDescription());
                                timeStampStr = name10;
                                idStr = idStr6;
                                String str822 = sharedIdStr7;
                                String str922 = codePathStr4;
                                String str1022 = resourcePathStr2;
                                String str1122 = cpuAbiOverrideString2;
                                String str1222 = version2;
                                version = updateAvailable;
                                packageSetting = packageSetting2;
                                idStr3 = installerPackageName2;
                                uidError = uidError2;
                                idStr2 = isOrphaned;
                                codePathStr = volumeUuid;
                                resourcePathStr = legacyNativeLibraryPathStr2;
                                cpuAbiOverrideString = secondaryCpuAbiString2;
                                categoryHint = categoryHint2;
                                secondaryCpuAbiString = primaryCpuAbiString2;
                                if (packageSetting != null) {
                                }
                            }
                        } catch (NumberFormatException e4) {
                            String str17 = name10;
                            String name12 = sharedIdStr7;
                            settings = this;
                            String sharedIdStr11 = idStr6;
                            isOrphaned = null;
                            volumeUuid = null;
                            i2 = 5;
                            sharedIdStr7 = name12;
                            String str18 = realName;
                            resourcePathStr2 = null;
                            legacyNativeLibraryPathStr2 = null;
                            secondaryCpuAbiString2 = null;
                            name10 = str17;
                            version2 = null;
                            installerPackageName2 = null;
                            primaryCpuAbiString2 = null;
                            cpuAbiOverrideString2 = null;
                            PackageManagerService.reportSettingsProblem(i2, "Error in package manager settings: package " + name10 + " has bad userId " + idStr6 + " at " + parser.getPositionDescription());
                            timeStampStr = name10;
                            idStr = idStr6;
                            String str8222 = sharedIdStr7;
                            String str9222 = codePathStr4;
                            String str10222 = resourcePathStr2;
                            String str11222 = cpuAbiOverrideString2;
                            String str12222 = version2;
                            version = updateAvailable;
                            packageSetting = packageSetting2;
                            idStr3 = installerPackageName2;
                            uidError = uidError2;
                            idStr2 = isOrphaned;
                            codePathStr = volumeUuid;
                            resourcePathStr = legacyNativeLibraryPathStr2;
                            cpuAbiOverrideString = secondaryCpuAbiString2;
                            categoryHint = categoryHint2;
                            secondaryCpuAbiString = primaryCpuAbiString2;
                            if (packageSetting != null) {
                            }
                        }
                    } catch (NumberFormatException e5) {
                        String str19 = name10;
                        String name13 = idStr6;
                        settings2 = this;
                        name = str19;
                        isOrphaned = null;
                        volumeUuid = null;
                        i2 = 5;
                        resourcePathStr2 = null;
                        legacyNativeLibraryPathStr2 = null;
                        secondaryCpuAbiString2 = null;
                        version2 = null;
                        installerPackageName2 = null;
                        primaryCpuAbiString2 = null;
                        cpuAbiOverrideString2 = null;
                        PackageManagerService.reportSettingsProblem(i2, "Error in package manager settings: package " + name10 + " has bad userId " + idStr6 + " at " + parser.getPositionDescription());
                        timeStampStr = name10;
                        idStr = idStr6;
                        String str82222 = sharedIdStr7;
                        String str92222 = codePathStr4;
                        String str102222 = resourcePathStr2;
                        String str112222 = cpuAbiOverrideString2;
                        String str122222 = version2;
                        version = updateAvailable;
                        packageSetting = packageSetting2;
                        idStr3 = installerPackageName2;
                        uidError = uidError2;
                        idStr2 = isOrphaned;
                        codePathStr = volumeUuid;
                        resourcePathStr = legacyNativeLibraryPathStr2;
                        cpuAbiOverrideString = secondaryCpuAbiString2;
                        categoryHint = categoryHint2;
                        secondaryCpuAbiString = primaryCpuAbiString2;
                        if (packageSetting != null) {
                        }
                    }
                } catch (NumberFormatException e6) {
                    String str20 = name10;
                    String name14 = idStr6;
                    settings2 = this;
                    name = str20;
                    uidError2 = null;
                    isOrphaned = null;
                    volumeUuid = null;
                    i2 = 5;
                    resourcePathStr2 = null;
                    legacyNativeLibraryPathStr2 = null;
                    secondaryCpuAbiString2 = null;
                    version2 = null;
                    installerPackageName2 = null;
                    primaryCpuAbiString2 = null;
                    cpuAbiOverrideString2 = null;
                    PackageManagerService.reportSettingsProblem(i2, "Error in package manager settings: package " + name10 + " has bad userId " + idStr6 + " at " + parser.getPositionDescription());
                    timeStampStr = name10;
                    idStr = idStr6;
                    String str822222 = sharedIdStr7;
                    String str922222 = codePathStr4;
                    String str1022222 = resourcePathStr2;
                    String str1122222 = cpuAbiOverrideString2;
                    String str1222222 = version2;
                    version = updateAvailable;
                    packageSetting = packageSetting2;
                    idStr3 = installerPackageName2;
                    uidError = uidError2;
                    idStr2 = isOrphaned;
                    codePathStr = volumeUuid;
                    resourcePathStr = legacyNativeLibraryPathStr2;
                    cpuAbiOverrideString = secondaryCpuAbiString2;
                    categoryHint = categoryHint2;
                    secondaryCpuAbiString = primaryCpuAbiString2;
                    if (packageSetting != null) {
                    }
                }
            } catch (NumberFormatException e7) {
                String str21 = name10;
                settings = this;
                i2 = 5;
                uidError2 = null;
                isOrphaned = null;
                volumeUuid = null;
                resourcePathStr2 = null;
                legacyNativeLibraryPathStr2 = null;
                secondaryCpuAbiString2 = null;
                version2 = null;
                installerPackageName2 = null;
                primaryCpuAbiString2 = null;
                cpuAbiOverrideString2 = null;
                PackageManagerService.reportSettingsProblem(i2, "Error in package manager settings: package " + name10 + " has bad userId " + idStr6 + " at " + parser.getPositionDescription());
                timeStampStr = name10;
                idStr = idStr6;
                String str8222222 = sharedIdStr7;
                String str9222222 = codePathStr4;
                String str10222222 = resourcePathStr2;
                String str11222222 = cpuAbiOverrideString2;
                String str12222222 = version2;
                version = updateAvailable;
                packageSetting = packageSetting2;
                idStr3 = installerPackageName2;
                uidError = uidError2;
                idStr2 = isOrphaned;
                codePathStr = volumeUuid;
                resourcePathStr = legacyNativeLibraryPathStr2;
                cpuAbiOverrideString = secondaryCpuAbiString2;
                categoryHint = categoryHint2;
                secondaryCpuAbiString = primaryCpuAbiString2;
                if (packageSetting != null) {
                }
            }
            try {
                String resourcePathStr8 = xmlPullParser.getAttributeValue(null, "primaryCpuAbi");
                try {
                    secondaryCpuAbiString2 = xmlPullParser.getAttributeValue(null, "secondaryCpuAbi");
                    try {
                        cpuAbiOverrideString2 = xmlPullParser.getAttributeValue(null, "cpuAbiOverride");
                        try {
                            updateAvailable = xmlPullParser.getAttributeValue(null, "updateAvailable");
                            primaryCpuAbiString2 = (resourcePathStr8 != null || legacyCpuAbiString == null) ? resourcePathStr8 : legacyCpuAbiString;
                            try {
                                version2 = xmlPullParser.getAttributeValue(null, "version");
                                if (version2 != null) {
                                    try {
                                        versionCode = Long.parseLong(version2);
                                    } catch (NumberFormatException e8) {
                                    }
                                    installerPackageName2 = xmlPullParser.getAttributeValue(null, "installer");
                                    try {
                                        isOrphaned = xmlPullParser.getAttributeValue(null, "isOrphaned");
                                        try {
                                            volumeUuid = xmlPullParser.getAttributeValue(null, ATTR_VOLUME_UUID);
                                            try {
                                                categoryHintString = xmlPullParser.getAttributeValue(null, "categoryHint");
                                                if (categoryHintString != null) {
                                                    try {
                                                        categoryHint2 = Integer.parseInt(categoryHintString);
                                                    } catch (NumberFormatException e9) {
                                                    }
                                                }
                                                try {
                                                    systemStr = xmlPullParser.getAttributeValue(null, "publicFlags");
                                                    if (systemStr == null) {
                                                        try {
                                                            pkgFlags2 = Integer.parseInt(systemStr);
                                                        } catch (NumberFormatException e10) {
                                                        }
                                                        try {
                                                            systemStr = xmlPullParser.getAttributeValue(null, "privateFlags");
                                                            if (systemStr != null) {
                                                                try {
                                                                    pkgPrivateFlags = Integer.parseInt(systemStr);
                                                                } catch (NumberFormatException e11) {
                                                                }
                                                            }
                                                        } catch (NumberFormatException e12) {
                                                            String str22 = categoryHintString;
                                                            String str23 = systemStr;
                                                            settings = this;
                                                            resourcePathStr2 = resourcePathStr4;
                                                            i2 = 5;
                                                            String str24 = realName;
                                                            PackageManagerService.reportSettingsProblem(i2, "Error in package manager settings: package " + name10 + " has bad userId " + idStr6 + " at " + parser.getPositionDescription());
                                                            timeStampStr = name10;
                                                            idStr = idStr6;
                                                            String str82222222 = sharedIdStr7;
                                                            String str92222222 = codePathStr4;
                                                            String str102222222 = resourcePathStr2;
                                                            String str112222222 = cpuAbiOverrideString2;
                                                            String str122222222 = version2;
                                                            version = updateAvailable;
                                                            packageSetting = packageSetting2;
                                                            idStr3 = installerPackageName2;
                                                            uidError = uidError2;
                                                            idStr2 = isOrphaned;
                                                            codePathStr = volumeUuid;
                                                            resourcePathStr = legacyNativeLibraryPathStr2;
                                                            cpuAbiOverrideString = secondaryCpuAbiString2;
                                                            categoryHint = categoryHint2;
                                                            secondaryCpuAbiString = primaryCpuAbiString2;
                                                            if (packageSetting != null) {
                                                            }
                                                        }
                                                    } else {
                                                        systemStr = xmlPullParser.getAttributeValue(null, ATTR_FLAGS);
                                                        if (systemStr != null) {
                                                            try {
                                                                pkgFlags2 = Integer.parseInt(systemStr);
                                                            } catch (NumberFormatException e13) {
                                                            }
                                                            if ((pkgFlags2 & PRE_M_APP_INFO_FLAG_HIDDEN) != 0) {
                                                                pkgPrivateFlags = 0 | 1;
                                                            }
                                                            if ((pkgFlags2 & PRE_M_APP_INFO_FLAG_CANT_SAVE_STATE) != 0) {
                                                                pkgPrivateFlags |= 2;
                                                            }
                                                            if ((pkgFlags2 & PRE_M_APP_INFO_FLAG_FORWARD_LOCK) != 0) {
                                                                pkgPrivateFlags |= 4;
                                                            }
                                                            if ((pkgFlags2 & PRE_M_APP_INFO_FLAG_PRIVILEGED) != 0) {
                                                                pkgPrivateFlags |= 8;
                                                            }
                                                            pkgFlags = pkgFlags2 & (~(PRE_M_APP_INFO_FLAG_HIDDEN | PRE_M_APP_INFO_FLAG_CANT_SAVE_STATE | PRE_M_APP_INFO_FLAG_FORWARD_LOCK | PRE_M_APP_INFO_FLAG_PRIVILEGED));
                                                        } else {
                                                            systemStr = xmlPullParser.getAttributeValue(null, "system");
                                                            if (systemStr != null) {
                                                                pkgFlags = 0 | ("true".equalsIgnoreCase(systemStr) ? 1 : 0);
                                                            } else {
                                                                pkgFlags = 0 | 1;
                                                            }
                                                        }
                                                        pkgFlags2 = pkgFlags;
                                                    }
                                                    timeStampStr2 = xmlPullParser.getAttributeValue(null, "ft");
                                                    if (timeStampStr2 == null) {
                                                        try {
                                                            timeStamp4 = Long.parseLong(timeStampStr2, 16);
                                                        } catch (NumberFormatException e14) {
                                                        }
                                                    } else {
                                                        String timeStampStr6 = xmlPullParser.getAttributeValue(null, "ts");
                                                        if (timeStampStr6 != null) {
                                                            try {
                                                                timeStamp4 = Long.parseLong(timeStampStr6);
                                                            } catch (NumberFormatException e15) {
                                                            }
                                                        }
                                                    }
                                                    timeStamp = timeStamp4;
                                                } catch (NumberFormatException e16) {
                                                    str5 = name10;
                                                    name7 = sharedIdStr7;
                                                    String str25 = codePathStr4;
                                                    String str26 = categoryHintString;
                                                    String str27 = version2;
                                                    sharedIdStr2 = idStr6;
                                                    i3 = 5;
                                                    String str28 = systemStr;
                                                    settings = this;
                                                    resourcePathStr2 = resourcePathStr4;
                                                    i2 = i3;
                                                    idStr6 = sharedIdStr2;
                                                    sharedIdStr7 = name7;
                                                    name10 = str5;
                                                    PackageManagerService.reportSettingsProblem(i2, "Error in package manager settings: package " + name10 + " has bad userId " + idStr6 + " at " + parser.getPositionDescription());
                                                    timeStampStr = name10;
                                                    idStr = idStr6;
                                                    String str822222222 = sharedIdStr7;
                                                    String str922222222 = codePathStr4;
                                                    String str1022222222 = resourcePathStr2;
                                                    String str1122222222 = cpuAbiOverrideString2;
                                                    String str1222222222 = version2;
                                                    version = updateAvailable;
                                                    packageSetting = packageSetting2;
                                                    idStr3 = installerPackageName2;
                                                    uidError = uidError2;
                                                    idStr2 = isOrphaned;
                                                    codePathStr = volumeUuid;
                                                    resourcePathStr = legacyNativeLibraryPathStr2;
                                                    cpuAbiOverrideString = secondaryCpuAbiString2;
                                                    categoryHint = categoryHint2;
                                                    secondaryCpuAbiString = primaryCpuAbiString2;
                                                    if (packageSetting != null) {
                                                    }
                                                }
                                            } catch (NumberFormatException e17) {
                                                str4 = name10;
                                                name6 = sharedIdStr7;
                                                String str29 = codePathStr4;
                                                String str30 = version2;
                                                String sharedIdStr12 = idStr6;
                                                settings5 = this;
                                                resourcePathStr2 = resourcePathStr4;
                                                i2 = 5;
                                                sharedIdStr7 = name6;
                                                name10 = str4;
                                                PackageManagerService.reportSettingsProblem(i2, "Error in package manager settings: package " + name10 + " has bad userId " + idStr6 + " at " + parser.getPositionDescription());
                                                timeStampStr = name10;
                                                idStr = idStr6;
                                                String str8222222222 = sharedIdStr7;
                                                String str9222222222 = codePathStr4;
                                                String str10222222222 = resourcePathStr2;
                                                String str11222222222 = cpuAbiOverrideString2;
                                                String str12222222222 = version2;
                                                version = updateAvailable;
                                                packageSetting = packageSetting2;
                                                idStr3 = installerPackageName2;
                                                uidError = uidError2;
                                                idStr2 = isOrphaned;
                                                codePathStr = volumeUuid;
                                                resourcePathStr = legacyNativeLibraryPathStr2;
                                                cpuAbiOverrideString = secondaryCpuAbiString2;
                                                categoryHint = categoryHint2;
                                                secondaryCpuAbiString = primaryCpuAbiString2;
                                                if (packageSetting != null) {
                                                }
                                            }
                                        } catch (NumberFormatException e18) {
                                            str4 = name10;
                                            name6 = sharedIdStr7;
                                            String str31 = codePathStr4;
                                            String str32 = version2;
                                            String sharedIdStr13 = idStr6;
                                            settings5 = this;
                                            volumeUuid = null;
                                            resourcePathStr2 = resourcePathStr4;
                                            i2 = 5;
                                            sharedIdStr7 = name6;
                                            name10 = str4;
                                            PackageManagerService.reportSettingsProblem(i2, "Error in package manager settings: package " + name10 + " has bad userId " + idStr6 + " at " + parser.getPositionDescription());
                                            timeStampStr = name10;
                                            idStr = idStr6;
                                            String str82222222222 = sharedIdStr7;
                                            String str92222222222 = codePathStr4;
                                            String str102222222222 = resourcePathStr2;
                                            String str112222222222 = cpuAbiOverrideString2;
                                            String str122222222222 = version2;
                                            version = updateAvailable;
                                            packageSetting = packageSetting2;
                                            idStr3 = installerPackageName2;
                                            uidError = uidError2;
                                            idStr2 = isOrphaned;
                                            codePathStr = volumeUuid;
                                            resourcePathStr = legacyNativeLibraryPathStr2;
                                            cpuAbiOverrideString = secondaryCpuAbiString2;
                                            categoryHint = categoryHint2;
                                            secondaryCpuAbiString = primaryCpuAbiString2;
                                            if (packageSetting != null) {
                                            }
                                        }
                                    } catch (NumberFormatException e19) {
                                        str4 = name10;
                                        name6 = sharedIdStr7;
                                        String str33 = codePathStr4;
                                        String str34 = version2;
                                        String sharedIdStr14 = idStr6;
                                        settings5 = this;
                                        isOrphaned = null;
                                        volumeUuid = null;
                                        resourcePathStr2 = resourcePathStr4;
                                        i2 = 5;
                                        sharedIdStr7 = name6;
                                        name10 = str4;
                                        PackageManagerService.reportSettingsProblem(i2, "Error in package manager settings: package " + name10 + " has bad userId " + idStr6 + " at " + parser.getPositionDescription());
                                        timeStampStr = name10;
                                        idStr = idStr6;
                                        String str822222222222 = sharedIdStr7;
                                        String str922222222222 = codePathStr4;
                                        String str1022222222222 = resourcePathStr2;
                                        String str1122222222222 = cpuAbiOverrideString2;
                                        String str1222222222222 = version2;
                                        version = updateAvailable;
                                        packageSetting = packageSetting2;
                                        idStr3 = installerPackageName2;
                                        uidError = uidError2;
                                        idStr2 = isOrphaned;
                                        codePathStr = volumeUuid;
                                        resourcePathStr = legacyNativeLibraryPathStr2;
                                        cpuAbiOverrideString = secondaryCpuAbiString2;
                                        categoryHint = categoryHint2;
                                        secondaryCpuAbiString = primaryCpuAbiString2;
                                        if (packageSetting != null) {
                                        }
                                    }
                                    try {
                                        timeStampStr3 = xmlPullParser.getAttributeValue(null, "it");
                                        if (timeStampStr3 != null) {
                                            try {
                                                firstInstallTime5 = Long.parseLong(timeStampStr3, 16);
                                            } catch (NumberFormatException e20) {
                                            }
                                        }
                                        long firstInstallTime6 = firstInstallTime5;
                                        try {
                                            timeStampStr4 = xmlPullParser.getAttributeValue(null, "ut");
                                            if (timeStampStr4 != null) {
                                                try {
                                                    lastUpdateTime = Long.parseLong(timeStampStr4, 16);
                                                } catch (NumberFormatException e21) {
                                                }
                                            }
                                            long lastUpdateTime2 = lastUpdateTime;
                                            try {
                                                if (!PackageManagerService.DEBUG_SETTINGS) {
                                                    try {
                                                        StringBuilder sb = new StringBuilder();
                                                        categoryHintString2 = categoryHintString;
                                                        try {
                                                            sb.append("Reading package: ");
                                                            sb.append(name10);
                                                            sb.append(" userId=");
                                                            sb.append(idStr6);
                                                            sb.append(" sharedUserId=");
                                                            sb.append(sharedIdStr7);
                                                            Log.v("PackageManager", sb.toString());
                                                        } catch (NumberFormatException e22) {
                                                            String str35 = realName;
                                                            settings = this;
                                                            resourcePathStr2 = resourcePathStr4;
                                                            i2 = 5;
                                                            PackageManagerService.reportSettingsProblem(i2, "Error in package manager settings: package " + name10 + " has bad userId " + idStr6 + " at " + parser.getPositionDescription());
                                                            timeStampStr = name10;
                                                            idStr = idStr6;
                                                            String str8222222222222 = sharedIdStr7;
                                                            String str9222222222222 = codePathStr4;
                                                            String str10222222222222 = resourcePathStr2;
                                                            String str11222222222222 = cpuAbiOverrideString2;
                                                            String str12222222222222 = version2;
                                                            version = updateAvailable;
                                                            packageSetting = packageSetting2;
                                                            idStr3 = installerPackageName2;
                                                            uidError = uidError2;
                                                            idStr2 = isOrphaned;
                                                            codePathStr = volumeUuid;
                                                            resourcePathStr = legacyNativeLibraryPathStr2;
                                                            cpuAbiOverrideString = secondaryCpuAbiString2;
                                                            categoryHint = categoryHint2;
                                                            secondaryCpuAbiString = primaryCpuAbiString2;
                                                            if (packageSetting != null) {
                                                            }
                                                        }
                                                    } catch (NumberFormatException e23) {
                                                        String str36 = categoryHintString;
                                                        String str37 = realName;
                                                        settings = this;
                                                        resourcePathStr2 = resourcePathStr4;
                                                        String str38 = str36;
                                                        i2 = 5;
                                                        PackageManagerService.reportSettingsProblem(i2, "Error in package manager settings: package " + name10 + " has bad userId " + idStr6 + " at " + parser.getPositionDescription());
                                                        timeStampStr = name10;
                                                        idStr = idStr6;
                                                        String str82222222222222 = sharedIdStr7;
                                                        String str92222222222222 = codePathStr4;
                                                        String str102222222222222 = resourcePathStr2;
                                                        String str112222222222222 = cpuAbiOverrideString2;
                                                        String str122222222222222 = version2;
                                                        version = updateAvailable;
                                                        packageSetting = packageSetting2;
                                                        idStr3 = installerPackageName2;
                                                        uidError = uidError2;
                                                        idStr2 = isOrphaned;
                                                        codePathStr = volumeUuid;
                                                        resourcePathStr = legacyNativeLibraryPathStr2;
                                                        cpuAbiOverrideString = secondaryCpuAbiString2;
                                                        categoryHint = categoryHint2;
                                                        secondaryCpuAbiString = primaryCpuAbiString2;
                                                        if (packageSetting != null) {
                                                        }
                                                    }
                                                } else {
                                                    categoryHintString2 = categoryHintString;
                                                }
                                                if (idStr6 == null) {
                                                    userId = Integer.parseInt(idStr6);
                                                } else {
                                                    userId = 0;
                                                }
                                                int sharedUserId = sharedIdStr7 == null ? Integer.parseInt(sharedIdStr7) : 0;
                                                if (resourcePathStr4 != null) {
                                                    resourcePathStr2 = codePathStr4;
                                                } else {
                                                    resourcePathStr2 = resourcePathStr4;
                                                }
                                                if (realName != null) {
                                                    try {
                                                        realName = realName.intern();
                                                    } catch (NumberFormatException e24) {
                                                        String str39 = realName;
                                                        settings = this;
                                                        i2 = 5;
                                                        PackageManagerService.reportSettingsProblem(i2, "Error in package manager settings: package " + name10 + " has bad userId " + idStr6 + " at " + parser.getPositionDescription());
                                                        timeStampStr = name10;
                                                        idStr = idStr6;
                                                        String str822222222222222 = sharedIdStr7;
                                                        String str922222222222222 = codePathStr4;
                                                        String str1022222222222222 = resourcePathStr2;
                                                        String str1122222222222222 = cpuAbiOverrideString2;
                                                        String str1222222222222222 = version2;
                                                        version = updateAvailable;
                                                        packageSetting = packageSetting2;
                                                        idStr3 = installerPackageName2;
                                                        uidError = uidError2;
                                                        idStr2 = isOrphaned;
                                                        codePathStr = volumeUuid;
                                                        resourcePathStr = legacyNativeLibraryPathStr2;
                                                        cpuAbiOverrideString = secondaryCpuAbiString2;
                                                        categoryHint = categoryHint2;
                                                        secondaryCpuAbiString = primaryCpuAbiString2;
                                                        if (packageSetting != null) {
                                                        }
                                                    }
                                                }
                                                String realName2 = realName;
                                                if (name10 != null) {
                                                    try {
                                                        StringBuilder sb2 = new StringBuilder();
                                                        idStr4 = idStr6;
                                                        try {
                                                            sb2.append("Error in package manager settings: <package> has no name at ");
                                                            sb2.append(parser.getPositionDescription());
                                                            i4 = 5;
                                                            try {
                                                                PackageManagerService.reportSettingsProblem(5, sb2.toString());
                                                                timeStampStr = name10;
                                                                name8 = sharedIdStr7;
                                                                String str40 = codePathStr4;
                                                                String str41 = version2;
                                                                settings6 = this;
                                                                timeStamp2 = timeStamp;
                                                                firstInstallTime = firstInstallTime6;
                                                                firstInstallTime2 = lastUpdateTime2;
                                                                String str42 = categoryHintString2;
                                                                sharedIdStr3 = idStr4;
                                                                i4 = 5;
                                                            } catch (NumberFormatException e25) {
                                                                settings = this;
                                                                String str43 = categoryHintString2;
                                                                i2 = i4;
                                                                idStr6 = idStr4;
                                                                PackageManagerService.reportSettingsProblem(i2, "Error in package manager settings: package " + name10 + " has bad userId " + idStr6 + " at " + parser.getPositionDescription());
                                                                timeStampStr = name10;
                                                                idStr = idStr6;
                                                                String str8222222222222222 = sharedIdStr7;
                                                                String str9222222222222222 = codePathStr4;
                                                                String str10222222222222222 = resourcePathStr2;
                                                                String str11222222222222222 = cpuAbiOverrideString2;
                                                                String str12222222222222222 = version2;
                                                                version = updateAvailable;
                                                                packageSetting = packageSetting2;
                                                                idStr3 = installerPackageName2;
                                                                uidError = uidError2;
                                                                idStr2 = isOrphaned;
                                                                codePathStr = volumeUuid;
                                                                resourcePathStr = legacyNativeLibraryPathStr2;
                                                                cpuAbiOverrideString = secondaryCpuAbiString2;
                                                                categoryHint = categoryHint2;
                                                                secondaryCpuAbiString = primaryCpuAbiString2;
                                                                if (packageSetting != null) {
                                                                }
                                                            }
                                                        } catch (NumberFormatException e26) {
                                                            settings = this;
                                                            String str44 = categoryHintString2;
                                                            idStr6 = idStr4;
                                                            i2 = 5;
                                                            PackageManagerService.reportSettingsProblem(i2, "Error in package manager settings: package " + name10 + " has bad userId " + idStr6 + " at " + parser.getPositionDescription());
                                                            timeStampStr = name10;
                                                            idStr = idStr6;
                                                            String str82222222222222222 = sharedIdStr7;
                                                            String str92222222222222222 = codePathStr4;
                                                            String str102222222222222222 = resourcePathStr2;
                                                            String str112222222222222222 = cpuAbiOverrideString2;
                                                            String str122222222222222222 = version2;
                                                            version = updateAvailable;
                                                            packageSetting = packageSetting2;
                                                            idStr3 = installerPackageName2;
                                                            uidError = uidError2;
                                                            idStr2 = isOrphaned;
                                                            codePathStr = volumeUuid;
                                                            resourcePathStr = legacyNativeLibraryPathStr2;
                                                            cpuAbiOverrideString = secondaryCpuAbiString2;
                                                            categoryHint = categoryHint2;
                                                            secondaryCpuAbiString = primaryCpuAbiString2;
                                                            if (packageSetting != null) {
                                                            }
                                                        }
                                                    } catch (NumberFormatException e27) {
                                                        String str45 = idStr6;
                                                        settings = this;
                                                        String str46 = categoryHintString2;
                                                        i2 = 5;
                                                        PackageManagerService.reportSettingsProblem(i2, "Error in package manager settings: package " + name10 + " has bad userId " + idStr6 + " at " + parser.getPositionDescription());
                                                        timeStampStr = name10;
                                                        idStr = idStr6;
                                                        String str822222222222222222 = sharedIdStr7;
                                                        String str922222222222222222 = codePathStr4;
                                                        String str1022222222222222222 = resourcePathStr2;
                                                        String str1122222222222222222 = cpuAbiOverrideString2;
                                                        String str1222222222222222222 = version2;
                                                        version = updateAvailable;
                                                        packageSetting = packageSetting2;
                                                        idStr3 = installerPackageName2;
                                                        uidError = uidError2;
                                                        idStr2 = isOrphaned;
                                                        codePathStr = volumeUuid;
                                                        resourcePathStr = legacyNativeLibraryPathStr2;
                                                        cpuAbiOverrideString = secondaryCpuAbiString2;
                                                        categoryHint = categoryHint2;
                                                        secondaryCpuAbiString = primaryCpuAbiString2;
                                                        if (packageSetting != null) {
                                                        }
                                                    }
                                                } else {
                                                    idStr4 = idStr6;
                                                    if (codePathStr4 == null) {
                                                        i4 = 5;
                                                        PackageManagerService.reportSettingsProblem(5, "Error in package manager settings: <package> has no codePath at " + parser.getPositionDescription());
                                                        timeStampStr = name10;
                                                        name8 = sharedIdStr7;
                                                        String str47 = codePathStr4;
                                                        String str48 = version2;
                                                        settings6 = this;
                                                        timeStamp2 = timeStamp;
                                                        firstInstallTime = firstInstallTime6;
                                                        firstInstallTime2 = lastUpdateTime2;
                                                        String str49 = categoryHintString2;
                                                        sharedIdStr3 = idStr4;
                                                    } else if (userId > 0) {
                                                        try {
                                                            String sharedIdStr15 = sharedIdStr7;
                                                            try {
                                                                String codePathStr5 = codePathStr4;
                                                                try {
                                                                    name9 = name10;
                                                                    idStr5 = idStr4;
                                                                    sharedIdStr6 = sharedIdStr15;
                                                                    codePathStr3 = codePathStr5;
                                                                    resourcePathStr6 = resourcePathStr2;
                                                                    String str50 = categoryHintString2;
                                                                    version4 = version2;
                                                                    String str51 = timeStampStr4;
                                                                    int i5 = sharedUserId;
                                                                    j3 = versionCode;
                                                                } catch (NumberFormatException e28) {
                                                                    String str52 = name10;
                                                                    String str53 = resourcePathStr2;
                                                                    String str54 = version2;
                                                                    long j4 = timeStamp;
                                                                    long j5 = firstInstallTime6;
                                                                    long j6 = lastUpdateTime2;
                                                                    String str55 = categoryHintString2;
                                                                    settings = this;
                                                                    idStr6 = idStr4;
                                                                    sharedIdStr7 = sharedIdStr15;
                                                                    codePathStr4 = codePathStr5;
                                                                    i2 = 5;
                                                                    PackageManagerService.reportSettingsProblem(i2, "Error in package manager settings: package " + name10 + " has bad userId " + idStr6 + " at " + parser.getPositionDescription());
                                                                    timeStampStr = name10;
                                                                    idStr = idStr6;
                                                                    String str8222222222222222222 = sharedIdStr7;
                                                                    String str9222222222222222222 = codePathStr4;
                                                                    String str10222222222222222222 = resourcePathStr2;
                                                                    String str11222222222222222222 = cpuAbiOverrideString2;
                                                                    String str12222222222222222222 = version2;
                                                                    version = updateAvailable;
                                                                    packageSetting = packageSetting2;
                                                                    idStr3 = installerPackageName2;
                                                                    uidError = uidError2;
                                                                    idStr2 = isOrphaned;
                                                                    codePathStr = volumeUuid;
                                                                    resourcePathStr = legacyNativeLibraryPathStr2;
                                                                    cpuAbiOverrideString = secondaryCpuAbiString2;
                                                                    categoryHint = categoryHint2;
                                                                    secondaryCpuAbiString = primaryCpuAbiString2;
                                                                    if (packageSetting != null) {
                                                                    }
                                                                }
                                                            } catch (NumberFormatException e29) {
                                                                String str56 = name10;
                                                                String str57 = codePathStr4;
                                                                String str58 = resourcePathStr2;
                                                                String str59 = version2;
                                                                long j7 = timeStamp;
                                                                long j8 = firstInstallTime6;
                                                                long j9 = lastUpdateTime2;
                                                                String str60 = categoryHintString2;
                                                                settings = this;
                                                                idStr6 = idStr4;
                                                                sharedIdStr7 = sharedIdStr15;
                                                                i2 = 5;
                                                                PackageManagerService.reportSettingsProblem(i2, "Error in package manager settings: package " + name10 + " has bad userId " + idStr6 + " at " + parser.getPositionDescription());
                                                                timeStampStr = name10;
                                                                idStr = idStr6;
                                                                String str82222222222222222222 = sharedIdStr7;
                                                                String str92222222222222222222 = codePathStr4;
                                                                String str102222222222222222222 = resourcePathStr2;
                                                                String str112222222222222222222 = cpuAbiOverrideString2;
                                                                String str122222222222222222222 = version2;
                                                                version = updateAvailable;
                                                                packageSetting = packageSetting2;
                                                                idStr3 = installerPackageName2;
                                                                uidError = uidError2;
                                                                idStr2 = isOrphaned;
                                                                codePathStr = volumeUuid;
                                                                resourcePathStr = legacyNativeLibraryPathStr2;
                                                                cpuAbiOverrideString = secondaryCpuAbiString2;
                                                                categoryHint = categoryHint2;
                                                                secondaryCpuAbiString = primaryCpuAbiString2;
                                                                if (packageSetting != null) {
                                                                }
                                                            }
                                                        } catch (NumberFormatException e30) {
                                                            String str61 = name10;
                                                            String str62 = codePathStr4;
                                                            String str63 = resourcePathStr2;
                                                            String str64 = version2;
                                                            long j10 = timeStamp;
                                                            long j11 = firstInstallTime6;
                                                            long j12 = lastUpdateTime2;
                                                            String str65 = categoryHintString2;
                                                            settings = this;
                                                            idStr6 = idStr4;
                                                            sharedIdStr7 = sharedIdStr7;
                                                            i2 = 5;
                                                            PackageManagerService.reportSettingsProblem(i2, "Error in package manager settings: package " + name10 + " has bad userId " + idStr6 + " at " + parser.getPositionDescription());
                                                            timeStampStr = name10;
                                                            idStr = idStr6;
                                                            String str822222222222222222222 = sharedIdStr7;
                                                            String str922222222222222222222 = codePathStr4;
                                                            String str1022222222222222222222 = resourcePathStr2;
                                                            String str1122222222222222222222 = cpuAbiOverrideString2;
                                                            String str1222222222222222222222 = version2;
                                                            version = updateAvailable;
                                                            packageSetting = packageSetting2;
                                                            idStr3 = installerPackageName2;
                                                            uidError = uidError2;
                                                            idStr2 = isOrphaned;
                                                            codePathStr = volumeUuid;
                                                            resourcePathStr = legacyNativeLibraryPathStr2;
                                                            cpuAbiOverrideString = secondaryCpuAbiString2;
                                                            categoryHint = categoryHint2;
                                                            secondaryCpuAbiString = primaryCpuAbiString2;
                                                            if (packageSetting != null) {
                                                            }
                                                        }
                                                        try {
                                                            PackageSetting packageSetting3 = addPackageLPw(name10.intern(), realName2, new File(codePathStr4), new File(resourcePathStr2), legacyNativeLibraryPathStr2, primaryCpuAbiString2, secondaryCpuAbiString2, cpuAbiOverrideString2, userId, j3, pkgFlags2, pkgPrivateFlags, parentPackageName, null, null, null);
                                                            try {
                                                                if (PackageManagerService.DEBUG_SETTINGS) {
                                                                    try {
                                                                        String name15 = j3;
                                                                        StringBuilder sb3 = new StringBuilder();
                                                                        sb3.append("Reading package ");
                                                                        String str66 = name9;
                                                                        try {
                                                                            name15 = str66;
                                                                            sb3.append(str66);
                                                                            sb3.append(": userId=");
                                                                            sb3.append(userId);
                                                                            sb3.append(" pkg=");
                                                                            sb3.append(packageSetting3);
                                                                            Log.i("PackageManager", sb3.toString());
                                                                            timeStampStr5 = str66;
                                                                        } catch (NumberFormatException e31) {
                                                                            packageSetting2 = packageSetting3;
                                                                            name10 = name15;
                                                                            version2 = version4;
                                                                            idStr6 = idStr5;
                                                                            sharedIdStr7 = sharedIdStr6;
                                                                            codePathStr4 = codePathStr3;
                                                                            resourcePathStr7 = resourcePathStr6;
                                                                            settings = this;
                                                                            i2 = 5;
                                                                            PackageManagerService.reportSettingsProblem(i2, "Error in package manager settings: package " + name10 + " has bad userId " + idStr6 + " at " + parser.getPositionDescription());
                                                                            timeStampStr = name10;
                                                                            idStr = idStr6;
                                                                            String str8222222222222222222222 = sharedIdStr7;
                                                                            String str9222222222222222222222 = codePathStr4;
                                                                            String str10222222222222222222222 = resourcePathStr2;
                                                                            String str11222222222222222222222 = cpuAbiOverrideString2;
                                                                            String str12222222222222222222222 = version2;
                                                                            version = updateAvailable;
                                                                            packageSetting = packageSetting2;
                                                                            idStr3 = installerPackageName2;
                                                                            uidError = uidError2;
                                                                            idStr2 = isOrphaned;
                                                                            codePathStr = volumeUuid;
                                                                            resourcePathStr = legacyNativeLibraryPathStr2;
                                                                            cpuAbiOverrideString = secondaryCpuAbiString2;
                                                                            categoryHint = categoryHint2;
                                                                            secondaryCpuAbiString = primaryCpuAbiString2;
                                                                            if (packageSetting != null) {
                                                                            }
                                                                        }
                                                                    } catch (NumberFormatException e32) {
                                                                        packageSetting2 = packageSetting3;
                                                                        name10 = name9;
                                                                        version2 = version4;
                                                                        idStr6 = idStr5;
                                                                        sharedIdStr7 = sharedIdStr6;
                                                                        codePathStr4 = codePathStr3;
                                                                        resourcePathStr2 = resourcePathStr6;
                                                                        settings = this;
                                                                        i2 = 5;
                                                                        PackageManagerService.reportSettingsProblem(i2, "Error in package manager settings: package " + name10 + " has bad userId " + idStr6 + " at " + parser.getPositionDescription());
                                                                        timeStampStr = name10;
                                                                        idStr = idStr6;
                                                                        String str82222222222222222222222 = sharedIdStr7;
                                                                        String str92222222222222222222222 = codePathStr4;
                                                                        String str102222222222222222222222 = resourcePathStr2;
                                                                        String str112222222222222222222222 = cpuAbiOverrideString2;
                                                                        String str122222222222222222222222 = version2;
                                                                        version = updateAvailable;
                                                                        packageSetting = packageSetting2;
                                                                        idStr3 = installerPackageName2;
                                                                        uidError = uidError2;
                                                                        idStr2 = isOrphaned;
                                                                        codePathStr = volumeUuid;
                                                                        resourcePathStr = legacyNativeLibraryPathStr2;
                                                                        cpuAbiOverrideString = secondaryCpuAbiString2;
                                                                        categoryHint = categoryHint2;
                                                                        secondaryCpuAbiString = primaryCpuAbiString2;
                                                                        if (packageSetting != null) {
                                                                        }
                                                                    }
                                                                } else {
                                                                    timeStampStr5 = name9;
                                                                }
                                                                if (packageSetting3 == null) {
                                                                    PackageManagerService.reportSettingsProblem(6, "Failure adding uid " + userId + " while parsing settings at " + parser.getPositionDescription());
                                                                    timeStamp3 = timeStamp;
                                                                    firstInstallTime3 = firstInstallTime6;
                                                                    firstInstallTime4 = lastUpdateTime2;
                                                                } else {
                                                                    timeStamp3 = timeStamp;
                                                                    try {
                                                                        packageSetting3.setTimeStamp(timeStamp3);
                                                                        long firstInstallTime7 = firstInstallTime6;
                                                                        try {
                                                                            packageSetting3.firstInstallTime = firstInstallTime7;
                                                                            firstInstallTime3 = firstInstallTime7;
                                                                            firstInstallTime4 = lastUpdateTime2;
                                                                            try {
                                                                                packageSetting3.lastUpdateTime = firstInstallTime4;
                                                                            } catch (NumberFormatException e33) {
                                                                                packageSetting2 = packageSetting3;
                                                                                long j13 = timeStamp3;
                                                                                name10 = timeStampStr;
                                                                                long j14 = firstInstallTime4;
                                                                                version2 = version4;
                                                                                idStr6 = idStr5;
                                                                                sharedIdStr7 = sharedIdStr6;
                                                                                codePathStr4 = codePathStr3;
                                                                                resourcePathStr7 = resourcePathStr6;
                                                                                long j15 = firstInstallTime3;
                                                                                settings = this;
                                                                                i2 = 5;
                                                                                PackageManagerService.reportSettingsProblem(i2, "Error in package manager settings: package " + name10 + " has bad userId " + idStr6 + " at " + parser.getPositionDescription());
                                                                                timeStampStr = name10;
                                                                                idStr = idStr6;
                                                                                String str822222222222222222222222 = sharedIdStr7;
                                                                                String str922222222222222222222222 = codePathStr4;
                                                                                String str1022222222222222222222222 = resourcePathStr2;
                                                                                String str1122222222222222222222222 = cpuAbiOverrideString2;
                                                                                String str1222222222222222222222222 = version2;
                                                                                version = updateAvailable;
                                                                                packageSetting = packageSetting2;
                                                                                idStr3 = installerPackageName2;
                                                                                uidError = uidError2;
                                                                                idStr2 = isOrphaned;
                                                                                codePathStr = volumeUuid;
                                                                                resourcePathStr = legacyNativeLibraryPathStr2;
                                                                                cpuAbiOverrideString = secondaryCpuAbiString2;
                                                                                categoryHint = categoryHint2;
                                                                                secondaryCpuAbiString = primaryCpuAbiString2;
                                                                                if (packageSetting != null) {
                                                                                }
                                                                            }
                                                                        } catch (NumberFormatException e34) {
                                                                            long j16 = firstInstallTime7;
                                                                            long firstInstallTime8 = lastUpdateTime2;
                                                                            packageSetting2 = packageSetting3;
                                                                            long j17 = timeStamp3;
                                                                            name10 = timeStampStr;
                                                                            version2 = version4;
                                                                            idStr6 = idStr5;
                                                                            sharedIdStr7 = sharedIdStr6;
                                                                            codePathStr4 = codePathStr3;
                                                                            resourcePathStr2 = resourcePathStr6;
                                                                            long j18 = j16;
                                                                            settings = this;
                                                                            i2 = 5;
                                                                            PackageManagerService.reportSettingsProblem(i2, "Error in package manager settings: package " + name10 + " has bad userId " + idStr6 + " at " + parser.getPositionDescription());
                                                                            timeStampStr = name10;
                                                                            idStr = idStr6;
                                                                            String str8222222222222222222222222 = sharedIdStr7;
                                                                            String str9222222222222222222222222 = codePathStr4;
                                                                            String str10222222222222222222222222 = resourcePathStr2;
                                                                            String str11222222222222222222222222 = cpuAbiOverrideString2;
                                                                            String str12222222222222222222222222 = version2;
                                                                            version = updateAvailable;
                                                                            packageSetting = packageSetting2;
                                                                            idStr3 = installerPackageName2;
                                                                            uidError = uidError2;
                                                                            idStr2 = isOrphaned;
                                                                            codePathStr = volumeUuid;
                                                                            resourcePathStr = legacyNativeLibraryPathStr2;
                                                                            cpuAbiOverrideString = secondaryCpuAbiString2;
                                                                            categoryHint = categoryHint2;
                                                                            secondaryCpuAbiString = primaryCpuAbiString2;
                                                                            if (packageSetting != null) {
                                                                            }
                                                                        }
                                                                    } catch (NumberFormatException e35) {
                                                                        long j19 = firstInstallTime6;
                                                                        long j20 = lastUpdateTime2;
                                                                        packageSetting2 = packageSetting3;
                                                                        long j21 = timeStamp3;
                                                                        name10 = timeStampStr;
                                                                        version2 = version4;
                                                                        idStr6 = idStr5;
                                                                        sharedIdStr7 = sharedIdStr6;
                                                                        codePathStr4 = codePathStr3;
                                                                        resourcePathStr2 = resourcePathStr6;
                                                                        settings = this;
                                                                        i2 = 5;
                                                                        PackageManagerService.reportSettingsProblem(i2, "Error in package manager settings: package " + name10 + " has bad userId " + idStr6 + " at " + parser.getPositionDescription());
                                                                        timeStampStr = name10;
                                                                        idStr = idStr6;
                                                                        String str82222222222222222222222222 = sharedIdStr7;
                                                                        String str92222222222222222222222222 = codePathStr4;
                                                                        String str102222222222222222222222222 = resourcePathStr2;
                                                                        String str112222222222222222222222222 = cpuAbiOverrideString2;
                                                                        String str122222222222222222222222222 = version2;
                                                                        version = updateAvailable;
                                                                        packageSetting = packageSetting2;
                                                                        idStr3 = installerPackageName2;
                                                                        uidError = uidError2;
                                                                        idStr2 = isOrphaned;
                                                                        codePathStr = volumeUuid;
                                                                        resourcePathStr = legacyNativeLibraryPathStr2;
                                                                        cpuAbiOverrideString = secondaryCpuAbiString2;
                                                                        categoryHint = categoryHint2;
                                                                        secondaryCpuAbiString = primaryCpuAbiString2;
                                                                        if (packageSetting != null) {
                                                                        }
                                                                    }
                                                                }
                                                                packageSetting2 = packageSetting3;
                                                                timeStamp2 = timeStamp3;
                                                                sharedIdStr3 = idStr5;
                                                                name8 = sharedIdStr6;
                                                                String str67 = codePathStr3;
                                                                resourcePathStr5 = resourcePathStr6;
                                                                firstInstallTime = firstInstallTime3;
                                                                i4 = 5;
                                                                settings6 = this;
                                                                String parentPackageName2 = name8;
                                                                resourcePathStr = legacyNativeLibraryPathStr2;
                                                                String str68 = cpuAbiOverrideString2;
                                                                long j22 = firstInstallTime2;
                                                                String str69 = resourcePathStr5;
                                                                categoryHint = categoryHint2;
                                                                packageSetting = packageSetting2;
                                                                uidError = uidError2;
                                                                codePathStr = volumeUuid;
                                                                long j23 = timeStamp2;
                                                                long j24 = firstInstallTime;
                                                                int i6 = i4;
                                                                idStr = sharedIdStr3;
                                                                cpuAbiOverrideString = secondaryCpuAbiString2;
                                                                settings = settings6;
                                                                version = updateAvailable;
                                                                idStr3 = installerPackageName2;
                                                                secondaryCpuAbiString = primaryCpuAbiString2;
                                                                idStr2 = isOrphaned;
                                                            } catch (NumberFormatException e36) {
                                                                long j25 = timeStamp;
                                                                long j26 = firstInstallTime6;
                                                                long j27 = lastUpdateTime2;
                                                                str6 = name9;
                                                                packageSetting2 = packageSetting3;
                                                                name10 = str6;
                                                                version2 = version4;
                                                                idStr6 = idStr5;
                                                                sharedIdStr7 = sharedIdStr6;
                                                                codePathStr4 = codePathStr3;
                                                                resourcePathStr2 = resourcePathStr6;
                                                                settings = this;
                                                                i2 = 5;
                                                                PackageManagerService.reportSettingsProblem(i2, "Error in package manager settings: package " + name10 + " has bad userId " + idStr6 + " at " + parser.getPositionDescription());
                                                                timeStampStr = name10;
                                                                idStr = idStr6;
                                                                String str822222222222222222222222222 = sharedIdStr7;
                                                                String str922222222222222222222222222 = codePathStr4;
                                                                String str1022222222222222222222222222 = resourcePathStr2;
                                                                String str1122222222222222222222222222 = cpuAbiOverrideString2;
                                                                String str1222222222222222222222222222 = version2;
                                                                version = updateAvailable;
                                                                packageSetting = packageSetting2;
                                                                idStr3 = installerPackageName2;
                                                                uidError = uidError2;
                                                                idStr2 = isOrphaned;
                                                                codePathStr = volumeUuid;
                                                                resourcePathStr = legacyNativeLibraryPathStr2;
                                                                cpuAbiOverrideString = secondaryCpuAbiString2;
                                                                categoryHint = categoryHint2;
                                                                secondaryCpuAbiString = primaryCpuAbiString2;
                                                                if (packageSetting != null) {
                                                                }
                                                            }
                                                        } catch (NumberFormatException e37) {
                                                            long j28 = timeStamp;
                                                            long j29 = firstInstallTime6;
                                                            long j30 = lastUpdateTime2;
                                                            str6 = name9;
                                                            name10 = str6;
                                                            version2 = version4;
                                                            idStr6 = idStr5;
                                                            sharedIdStr7 = sharedIdStr6;
                                                            codePathStr4 = codePathStr3;
                                                            resourcePathStr2 = resourcePathStr6;
                                                            settings = this;
                                                            i2 = 5;
                                                            PackageManagerService.reportSettingsProblem(i2, "Error in package manager settings: package " + name10 + " has bad userId " + idStr6 + " at " + parser.getPositionDescription());
                                                            timeStampStr = name10;
                                                            idStr = idStr6;
                                                            String str8222222222222222222222222222 = sharedIdStr7;
                                                            String str9222222222222222222222222222 = codePathStr4;
                                                            String str10222222222222222222222222222 = resourcePathStr2;
                                                            String str11222222222222222222222222222 = cpuAbiOverrideString2;
                                                            String str12222222222222222222222222222 = version2;
                                                            version = updateAvailable;
                                                            packageSetting = packageSetting2;
                                                            idStr3 = installerPackageName2;
                                                            uidError = uidError2;
                                                            idStr2 = isOrphaned;
                                                            codePathStr = volumeUuid;
                                                            resourcePathStr = legacyNativeLibraryPathStr2;
                                                            cpuAbiOverrideString = secondaryCpuAbiString2;
                                                            categoryHint = categoryHint2;
                                                            secondaryCpuAbiString = primaryCpuAbiString2;
                                                            if (packageSetting != null) {
                                                            }
                                                        }
                                                        if (packageSetting != null) {
                                                            packageSetting.uidError = "true".equals(uidError);
                                                            packageSetting.installerPackageName = idStr3;
                                                            packageSetting.isOrphaned = "true".equals(idStr2);
                                                            packageSetting.volumeUuid = codePathStr;
                                                            packageSetting.categoryHint = categoryHint;
                                                            packageSetting.legacyNativeLibraryPathString = resourcePathStr;
                                                            packageSetting.primaryCpuAbiString = secondaryCpuAbiString;
                                                            packageSetting.secondaryCpuAbiString = cpuAbiOverrideString;
                                                            packageSetting.updateAvailable = "true".equals(version);
                                                            XmlPullParser xmlPullParser2 = parser;
                                                            String str70 = uidError;
                                                            String str71 = idStr2;
                                                            String uidError3 = null;
                                                            String enabledStr2 = xmlPullParser2.getAttributeValue(null, ATTR_ENABLED);
                                                            if (enabledStr2 != null) {
                                                                try {
                                                                    String str72 = codePathStr;
                                                                    i = 0;
                                                                    try {
                                                                        packageSetting.setEnabled(Integer.parseInt(enabledStr2), 0, null);
                                                                        int i7 = categoryHint;
                                                                    } catch (NumberFormatException e38) {
                                                                        if (!enabledStr2.equalsIgnoreCase("true")) {
                                                                        }
                                                                        uidError3 = null;
                                                                        if (idStr3 != null) {
                                                                        }
                                                                        maxAspectRatioStr = xmlPullParser2.getAttributeValue(uidError3, "maxAspectRatio");
                                                                        if (maxAspectRatioStr != null) {
                                                                        }
                                                                        minAspectRatioStr = xmlPullParser2.getAttributeValue(null, "minAspectRatio");
                                                                        if (minAspectRatioStr != null) {
                                                                        }
                                                                        appUseNotchModeStr = xmlPullParser2.getAttributeValue(null, "appUseNotchMode");
                                                                        if (appUseNotchModeStr != null) {
                                                                        }
                                                                        outerDepth = parser.getDepth();
                                                                        while (true) {
                                                                            String maxAspectRatioStr2 = maxAspectRatioStr;
                                                                            next = parser.next();
                                                                            int type = next;
                                                                            String minAspectRatioStr2 = minAspectRatioStr;
                                                                            if (next != 1) {
                                                                            }
                                                                            maxAspectRatioStr = maxAspectRatioStr2;
                                                                            minAspectRatioStr = minAspectRatioStr2;
                                                                            idStr3 = installerPackageName;
                                                                            enabledStr2 = enabledStr;
                                                                            appUseNotchModeStr = appUseNotchModeStr2;
                                                                            outerDepth = outerDepth2;
                                                                            resourcePathStr = legacyNativeLibraryPathStr;
                                                                            secondaryCpuAbiString = primaryCpuAbiString;
                                                                        }
                                                                    }
                                                                } catch (NumberFormatException e39) {
                                                                    String str73 = codePathStr;
                                                                    i = 0;
                                                                    if (!enabledStr2.equalsIgnoreCase("true")) {
                                                                        int i8 = categoryHint;
                                                                        packageSetting.setEnabled(1, i, null);
                                                                    } else {
                                                                        if (enabledStr2.equalsIgnoreCase("false")) {
                                                                            packageSetting.setEnabled(2, i, null);
                                                                        } else if (enabledStr2.equalsIgnoreCase(BatteryService.HealthServiceWrapper.INSTANCE_VENDOR)) {
                                                                            packageSetting.setEnabled(i, i, null);
                                                                        } else {
                                                                            PackageManagerService.reportSettingsProblem(5, "Error in package manager settings: package " + timeStampStr + " has bad enabled value: " + idStr + " at " + parser.getPositionDescription());
                                                                        }
                                                                    }
                                                                    uidError3 = null;
                                                                    if (idStr3 != null) {
                                                                    }
                                                                    maxAspectRatioStr = xmlPullParser2.getAttributeValue(uidError3, "maxAspectRatio");
                                                                    if (maxAspectRatioStr != null) {
                                                                    }
                                                                    minAspectRatioStr = xmlPullParser2.getAttributeValue(null, "minAspectRatio");
                                                                    if (minAspectRatioStr != null) {
                                                                    }
                                                                    appUseNotchModeStr = xmlPullParser2.getAttributeValue(null, "appUseNotchMode");
                                                                    if (appUseNotchModeStr != null) {
                                                                    }
                                                                    outerDepth = parser.getDepth();
                                                                    while (true) {
                                                                        String maxAspectRatioStr22 = maxAspectRatioStr;
                                                                        next = parser.next();
                                                                        int type2 = next;
                                                                        String minAspectRatioStr22 = minAspectRatioStr;
                                                                        if (next != 1) {
                                                                        }
                                                                        maxAspectRatioStr = maxAspectRatioStr22;
                                                                        minAspectRatioStr = minAspectRatioStr22;
                                                                        idStr3 = installerPackageName;
                                                                        enabledStr2 = enabledStr;
                                                                        appUseNotchModeStr = appUseNotchModeStr2;
                                                                        outerDepth = outerDepth2;
                                                                        resourcePathStr = legacyNativeLibraryPathStr;
                                                                        secondaryCpuAbiString = primaryCpuAbiString;
                                                                    }
                                                                }
                                                            } else {
                                                                String volumeUuid2 = codePathStr;
                                                                int i9 = categoryHint;
                                                                uidError3 = null;
                                                                packageSetting.setEnabled(0, 0, null);
                                                            }
                                                            if (idStr3 != null) {
                                                                settings.mInstallerPackages.add(idStr3);
                                                            }
                                                            maxAspectRatioStr = xmlPullParser2.getAttributeValue(uidError3, "maxAspectRatio");
                                                            if (maxAspectRatioStr != null) {
                                                                float maxAspectRatio = Float.parseFloat(maxAspectRatioStr);
                                                                if (maxAspectRatio > 0.0f) {
                                                                    packageSetting.maxAspectRatio = maxAspectRatio;
                                                                }
                                                            }
                                                            minAspectRatioStr = xmlPullParser2.getAttributeValue(null, "minAspectRatio");
                                                            if (minAspectRatioStr != null) {
                                                                packageSetting.minAspectRatio = Float.parseFloat(minAspectRatioStr);
                                                            }
                                                            appUseNotchModeStr = xmlPullParser2.getAttributeValue(null, "appUseNotchMode");
                                                            if (appUseNotchModeStr != null) {
                                                                packageSetting.appUseNotchMode = Integer.parseInt(appUseNotchModeStr);
                                                            }
                                                            outerDepth = parser.getDepth();
                                                            while (true) {
                                                                String maxAspectRatioStr222 = maxAspectRatioStr;
                                                                next = parser.next();
                                                                int type22 = next;
                                                                String minAspectRatioStr222 = minAspectRatioStr;
                                                                if (next != 1) {
                                                                    int type3 = type22;
                                                                    if (type3 != 3 || parser.getDepth() > outerDepth) {
                                                                        if (type3 == 3) {
                                                                            installerPackageName = idStr3;
                                                                            enabledStr = enabledStr2;
                                                                            appUseNotchModeStr2 = appUseNotchModeStr;
                                                                            outerDepth2 = outerDepth;
                                                                            legacyNativeLibraryPathStr = resourcePathStr;
                                                                            primaryCpuAbiString = secondaryCpuAbiString;
                                                                        } else if (type3 == 4) {
                                                                            installerPackageName = idStr3;
                                                                            enabledStr = enabledStr2;
                                                                            appUseNotchModeStr2 = appUseNotchModeStr;
                                                                            outerDepth2 = outerDepth;
                                                                            legacyNativeLibraryPathStr = resourcePathStr;
                                                                            primaryCpuAbiString = secondaryCpuAbiString;
                                                                        } else {
                                                                            String tagName = parser.getName();
                                                                            int i10 = type3;
                                                                            if (tagName.equals(TAG_DISABLED_COMPONENTS) != 0) {
                                                                                settings.readDisabledComponentsLPw(packageSetting, xmlPullParser2, 0);
                                                                            } else if (tagName.equals(TAG_ENABLED_COMPONENTS)) {
                                                                                settings.readEnabledComponentsLPw(packageSetting, xmlPullParser2, 0);
                                                                            } else if (tagName.equals("sigs")) {
                                                                                installerPackageName = idStr3;
                                                                                packageSetting.signatures.readXml(xmlPullParser2, settings.mPastSignatures);
                                                                                enabledStr = enabledStr2;
                                                                                appUseNotchModeStr2 = appUseNotchModeStr;
                                                                                outerDepth2 = outerDepth;
                                                                                legacyNativeLibraryPathStr = resourcePathStr;
                                                                                primaryCpuAbiString = secondaryCpuAbiString;
                                                                            } else {
                                                                                installerPackageName = idStr3;
                                                                                if (tagName.equals(TAG_PERMISSIONS)) {
                                                                                    settings.readInstallPermissionsLPr(xmlPullParser2, packageSetting.getPermissionsState());
                                                                                    packageSetting.installPermissionsFixed = true;
                                                                                    enabledStr = enabledStr2;
                                                                                    appUseNotchModeStr2 = appUseNotchModeStr;
                                                                                    outerDepth2 = outerDepth;
                                                                                    legacyNativeLibraryPathStr = resourcePathStr;
                                                                                    primaryCpuAbiString = secondaryCpuAbiString;
                                                                                } else {
                                                                                    if (tagName.equals("proper-signing-keyset")) {
                                                                                        long id = Long.parseLong(xmlPullParser2.getAttributeValue(null, "identifier"));
                                                                                        enabledStr = enabledStr2;
                                                                                        appUseNotchModeStr2 = appUseNotchModeStr;
                                                                                        Integer refCt = settings.mKeySetRefs.get(Long.valueOf(id));
                                                                                        if (refCt != null) {
                                                                                            outerDepth2 = outerDepth;
                                                                                            Integer num = refCt;
                                                                                            legacyNativeLibraryPathStr = resourcePathStr;
                                                                                            settings.mKeySetRefs.put(Long.valueOf(id), Integer.valueOf(refCt.intValue() + 1));
                                                                                        } else {
                                                                                            outerDepth2 = outerDepth;
                                                                                            legacyNativeLibraryPathStr = resourcePathStr;
                                                                                            settings.mKeySetRefs.put(Long.valueOf(id), 1);
                                                                                        }
                                                                                        packageSetting.keySetData.setProperSigningKeySet(id);
                                                                                    } else {
                                                                                        enabledStr = enabledStr2;
                                                                                        appUseNotchModeStr2 = appUseNotchModeStr;
                                                                                        outerDepth2 = outerDepth;
                                                                                        legacyNativeLibraryPathStr = resourcePathStr;
                                                                                        if (!tagName.equals("signing-keyset")) {
                                                                                            if (tagName.equals("upgrade-keyset")) {
                                                                                                packageSetting.keySetData.addUpgradeKeySetById(Long.parseLong(xmlPullParser2.getAttributeValue(null, "identifier")));
                                                                                            } else if (tagName.equals("defined-keyset")) {
                                                                                                long id2 = Long.parseLong(xmlPullParser2.getAttributeValue(null, "identifier"));
                                                                                                String alias = xmlPullParser2.getAttributeValue(null, "alias");
                                                                                                Integer refCt2 = settings.mKeySetRefs.get(Long.valueOf(id2));
                                                                                                if (refCt2 != null) {
                                                                                                    Integer num2 = refCt2;
                                                                                                    primaryCpuAbiString = secondaryCpuAbiString;
                                                                                                    settings.mKeySetRefs.put(Long.valueOf(id2), Integer.valueOf(refCt2.intValue() + 1));
                                                                                                } else {
                                                                                                    primaryCpuAbiString = secondaryCpuAbiString;
                                                                                                    settings.mKeySetRefs.put(Long.valueOf(id2), 1);
                                                                                                }
                                                                                                packageSetting.keySetData.addDefinedKeySet(id2, alias);
                                                                                            } else {
                                                                                                primaryCpuAbiString = secondaryCpuAbiString;
                                                                                                if (tagName.equals(TAG_DOMAIN_VERIFICATION)) {
                                                                                                    settings.readDomainVerificationLPw(xmlPullParser2, packageSetting);
                                                                                                } else if (tagName.equals(TAG_CHILD_PACKAGE)) {
                                                                                                    String childPackageName = xmlPullParser2.getAttributeValue(null, ATTR_NAME);
                                                                                                    if (packageSetting.childPackageNames == null) {
                                                                                                        packageSetting.childPackageNames = new ArrayList();
                                                                                                    }
                                                                                                    packageSetting.childPackageNames.add(childPackageName);
                                                                                                } else {
                                                                                                    PackageManagerService.reportSettingsProblem(5, "Unknown element under <package>: " + parser.getName());
                                                                                                    XmlUtils.skipCurrentTag(parser);
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                    }
                                                                                    primaryCpuAbiString = secondaryCpuAbiString;
                                                                                }
                                                                            }
                                                                            installerPackageName = idStr3;
                                                                            enabledStr = enabledStr2;
                                                                            appUseNotchModeStr2 = appUseNotchModeStr;
                                                                            outerDepth2 = outerDepth;
                                                                            legacyNativeLibraryPathStr = resourcePathStr;
                                                                            primaryCpuAbiString = secondaryCpuAbiString;
                                                                        }
                                                                        maxAspectRatioStr = maxAspectRatioStr222;
                                                                        minAspectRatioStr = minAspectRatioStr222;
                                                                        idStr3 = installerPackageName;
                                                                        enabledStr2 = enabledStr;
                                                                        appUseNotchModeStr = appUseNotchModeStr2;
                                                                        outerDepth = outerDepth2;
                                                                        resourcePathStr = legacyNativeLibraryPathStr;
                                                                        secondaryCpuAbiString = primaryCpuAbiString;
                                                                    } else {
                                                                        String str74 = idStr3;
                                                                        String str75 = resourcePathStr;
                                                                        String str76 = secondaryCpuAbiString;
                                                                        return;
                                                                    }
                                                                } else {
                                                                    String installerPackageName3 = idStr3;
                                                                    String str77 = resourcePathStr;
                                                                    String str78 = secondaryCpuAbiString;
                                                                    return;
                                                                }
                                                            }
                                                        } else {
                                                            XmlPullParser xmlPullParser3 = parser;
                                                            String str79 = uidError;
                                                            String str80 = idStr3;
                                                            String str81 = idStr2;
                                                            String str83 = codePathStr;
                                                            int i11 = categoryHint;
                                                            String str84 = resourcePathStr;
                                                            String str85 = secondaryCpuAbiString;
                                                            XmlUtils.skipCurrentTag(parser);
                                                            return;
                                                        }
                                                    } else {
                                                        String codePathStr6 = codePathStr4;
                                                        String resourcePathStr9 = resourcePathStr2;
                                                        String version5 = version2;
                                                        String str86 = timeStampStr4;
                                                        int sharedUserId2 = sharedUserId;
                                                        long timeStamp5 = timeStamp;
                                                        long firstInstallTime9 = firstInstallTime6;
                                                        firstInstallTime2 = lastUpdateTime2;
                                                        String str87 = categoryHintString2;
                                                        String idStr7 = idStr4;
                                                        timeStampStr = name10;
                                                        name8 = sharedIdStr7;
                                                        if (name8 != null) {
                                                            int sharedUserId3 = sharedUserId2;
                                                            if (sharedUserId3 > 0) {
                                                                try {
                                                                    String intern = timeStampStr.intern();
                                                                    String codePathStr7 = codePathStr6;
                                                                    try {
                                                                        File file = new File(codePathStr7);
                                                                        codePathStr2 = codePathStr7;
                                                                        resourcePathStr5 = resourcePathStr9;
                                                                        try {
                                                                            PackageSetting packageSetting4 = new PackageSetting(intern, realName2, file, new File(resourcePathStr5), legacyNativeLibraryPathStr2, primaryCpuAbiString2, secondaryCpuAbiString2, cpuAbiOverrideString2, versionCode, pkgFlags2, pkgPrivateFlags, parentPackageName, null, sharedUserId3, null, null);
                                                                            try {
                                                                                packageSetting4.setTimeStamp(timeStamp5);
                                                                                timeStamp2 = timeStamp5;
                                                                                long firstInstallTime10 = firstInstallTime9;
                                                                                try {
                                                                                    packageSetting4.firstInstallTime = firstInstallTime10;
                                                                                    packageSetting4.lastUpdateTime = firstInstallTime2;
                                                                                    settings6 = this;
                                                                                    try {
                                                                                        settings6.mPendingPackages.add(packageSetting4);
                                                                                        if (PackageManagerService.DEBUG_SETTINGS) {
                                                                                            int i12 = userId;
                                                                                            StringBuilder sb4 = new StringBuilder();
                                                                                            firstInstallTime = firstInstallTime10;
                                                                                            try {
                                                                                                sb4.append("Reading package ");
                                                                                                sb4.append(timeStampStr);
                                                                                                sb4.append(": sharedUserId=");
                                                                                                sb4.append(sharedUserId3);
                                                                                                sb4.append(" pkg=");
                                                                                                sb4.append(packageSetting4);
                                                                                                Log.i("PackageManager", sb4.toString());
                                                                                            } catch (NumberFormatException e40) {
                                                                                                sharedIdStr4 = name8;
                                                                                                packageSetting2 = packageSetting4;
                                                                                            }
                                                                                        } else {
                                                                                            firstInstallTime = firstInstallTime10;
                                                                                        }
                                                                                        packageSetting2 = packageSetting4;
                                                                                        sharedIdStr3 = idStr7;
                                                                                        i4 = 5;
                                                                                        String parentPackageName22 = name8;
                                                                                        resourcePathStr = legacyNativeLibraryPathStr2;
                                                                                        String str682 = cpuAbiOverrideString2;
                                                                                        long j222 = firstInstallTime2;
                                                                                        String str692 = resourcePathStr5;
                                                                                        categoryHint = categoryHint2;
                                                                                        packageSetting = packageSetting2;
                                                                                        uidError = uidError2;
                                                                                        codePathStr = volumeUuid;
                                                                                        long j232 = timeStamp2;
                                                                                        long j242 = firstInstallTime;
                                                                                        int i62 = i4;
                                                                                        idStr = sharedIdStr3;
                                                                                        cpuAbiOverrideString = secondaryCpuAbiString2;
                                                                                        settings = settings6;
                                                                                        version = updateAvailable;
                                                                                        idStr3 = installerPackageName2;
                                                                                        secondaryCpuAbiString = primaryCpuAbiString2;
                                                                                        idStr2 = isOrphaned;
                                                                                    } catch (NumberFormatException e41) {
                                                                                        long j31 = firstInstallTime10;
                                                                                        sharedIdStr7 = name8;
                                                                                        packageSetting2 = packageSetting4;
                                                                                        name10 = timeStampStr;
                                                                                        long j32 = firstInstallTime2;
                                                                                        resourcePathStr2 = resourcePathStr5;
                                                                                        idStr6 = idStr7;
                                                                                        codePathStr4 = codePathStr2;
                                                                                        long j33 = timeStamp2;
                                                                                        long j34 = j31;
                                                                                        i2 = 5;
                                                                                        settings = settings6;
                                                                                        version2 = version5;
                                                                                        PackageManagerService.reportSettingsProblem(i2, "Error in package manager settings: package " + name10 + " has bad userId " + idStr6 + " at " + parser.getPositionDescription());
                                                                                        timeStampStr = name10;
                                                                                        idStr = idStr6;
                                                                                        String str82222222222222222222222222222 = sharedIdStr7;
                                                                                        String str92222222222222222222222222222 = codePathStr4;
                                                                                        String str102222222222222222222222222222 = resourcePathStr2;
                                                                                        String str112222222222222222222222222222 = cpuAbiOverrideString2;
                                                                                        String str122222222222222222222222222222 = version2;
                                                                                        version = updateAvailable;
                                                                                        packageSetting = packageSetting2;
                                                                                        idStr3 = installerPackageName2;
                                                                                        uidError = uidError2;
                                                                                        idStr2 = isOrphaned;
                                                                                        codePathStr = volumeUuid;
                                                                                        resourcePathStr = legacyNativeLibraryPathStr2;
                                                                                        cpuAbiOverrideString = secondaryCpuAbiString2;
                                                                                        categoryHint = categoryHint2;
                                                                                        secondaryCpuAbiString = primaryCpuAbiString2;
                                                                                        if (packageSetting != null) {
                                                                                        }
                                                                                    }
                                                                                } catch (NumberFormatException e42) {
                                                                                    long j35 = firstInstallTime10;
                                                                                    sharedIdStr7 = name8;
                                                                                    packageSetting2 = packageSetting4;
                                                                                    name10 = timeStampStr;
                                                                                    long j36 = firstInstallTime2;
                                                                                    resourcePathStr2 = resourcePathStr5;
                                                                                    version2 = version5;
                                                                                    idStr6 = idStr7;
                                                                                    codePathStr4 = codePathStr2;
                                                                                    long j37 = timeStamp2;
                                                                                    long j38 = j35;
                                                                                    settings = this;
                                                                                    i2 = 5;
                                                                                    PackageManagerService.reportSettingsProblem(i2, "Error in package manager settings: package " + name10 + " has bad userId " + idStr6 + " at " + parser.getPositionDescription());
                                                                                    timeStampStr = name10;
                                                                                    idStr = idStr6;
                                                                                    String str822222222222222222222222222222 = sharedIdStr7;
                                                                                    String str922222222222222222222222222222 = codePathStr4;
                                                                                    String str1022222222222222222222222222222 = resourcePathStr2;
                                                                                    String str1122222222222222222222222222222 = cpuAbiOverrideString2;
                                                                                    String str1222222222222222222222222222222 = version2;
                                                                                    version = updateAvailable;
                                                                                    packageSetting = packageSetting2;
                                                                                    idStr3 = installerPackageName2;
                                                                                    uidError = uidError2;
                                                                                    idStr2 = isOrphaned;
                                                                                    codePathStr = volumeUuid;
                                                                                    resourcePathStr = legacyNativeLibraryPathStr2;
                                                                                    cpuAbiOverrideString = secondaryCpuAbiString2;
                                                                                    categoryHint = categoryHint2;
                                                                                    secondaryCpuAbiString = primaryCpuAbiString2;
                                                                                    if (packageSetting != null) {
                                                                                    }
                                                                                }
                                                                            } catch (NumberFormatException e43) {
                                                                                j2 = timeStamp5;
                                                                                j = firstInstallTime9;
                                                                                sharedIdStr5 = name8;
                                                                                packageSetting2 = packageSetting4;
                                                                                name10 = timeStampStr;
                                                                                long lastUpdateTime3 = firstInstallTime2;
                                                                                resourcePathStr2 = resourcePathStr5;
                                                                                version2 = version5;
                                                                                idStr6 = idStr7;
                                                                                codePathStr4 = codePathStr2;
                                                                                long timeStamp6 = j2;
                                                                                long firstInstallTime11 = j;
                                                                                settings = this;
                                                                                i2 = 5;
                                                                                PackageManagerService.reportSettingsProblem(i2, "Error in package manager settings: package " + name10 + " has bad userId " + idStr6 + " at " + parser.getPositionDescription());
                                                                                timeStampStr = name10;
                                                                                idStr = idStr6;
                                                                                String str8222222222222222222222222222222 = sharedIdStr7;
                                                                                String str9222222222222222222222222222222 = codePathStr4;
                                                                                String str10222222222222222222222222222222 = resourcePathStr2;
                                                                                String str11222222222222222222222222222222 = cpuAbiOverrideString2;
                                                                                String str12222222222222222222222222222222 = version2;
                                                                                version = updateAvailable;
                                                                                packageSetting = packageSetting2;
                                                                                idStr3 = installerPackageName2;
                                                                                uidError = uidError2;
                                                                                idStr2 = isOrphaned;
                                                                                codePathStr = volumeUuid;
                                                                                resourcePathStr = legacyNativeLibraryPathStr2;
                                                                                cpuAbiOverrideString = secondaryCpuAbiString2;
                                                                                categoryHint = categoryHint2;
                                                                                secondaryCpuAbiString = primaryCpuAbiString2;
                                                                                if (packageSetting != null) {
                                                                                }
                                                                            }
                                                                        } catch (NumberFormatException e44) {
                                                                            j2 = timeStamp5;
                                                                            j = firstInstallTime9;
                                                                            sharedIdStr5 = name8;
                                                                            name10 = timeStampStr;
                                                                            long lastUpdateTime32 = firstInstallTime2;
                                                                            resourcePathStr2 = resourcePathStr5;
                                                                            version2 = version5;
                                                                            idStr6 = idStr7;
                                                                            codePathStr4 = codePathStr2;
                                                                            long timeStamp62 = j2;
                                                                            long firstInstallTime112 = j;
                                                                            settings = this;
                                                                            i2 = 5;
                                                                            PackageManagerService.reportSettingsProblem(i2, "Error in package manager settings: package " + name10 + " has bad userId " + idStr6 + " at " + parser.getPositionDescription());
                                                                            timeStampStr = name10;
                                                                            idStr = idStr6;
                                                                            String str82222222222222222222222222222222 = sharedIdStr7;
                                                                            String str92222222222222222222222222222222 = codePathStr4;
                                                                            String str102222222222222222222222222222222 = resourcePathStr2;
                                                                            String str112222222222222222222222222222222 = cpuAbiOverrideString2;
                                                                            String str122222222222222222222222222222222 = version2;
                                                                            version = updateAvailable;
                                                                            packageSetting = packageSetting2;
                                                                            idStr3 = installerPackageName2;
                                                                            uidError = uidError2;
                                                                            idStr2 = isOrphaned;
                                                                            codePathStr = volumeUuid;
                                                                            resourcePathStr = legacyNativeLibraryPathStr2;
                                                                            cpuAbiOverrideString = secondaryCpuAbiString2;
                                                                            categoryHint = categoryHint2;
                                                                            secondaryCpuAbiString = primaryCpuAbiString2;
                                                                            if (packageSetting != null) {
                                                                            }
                                                                        }
                                                                    } catch (NumberFormatException e45) {
                                                                        long j39 = timeStamp5;
                                                                        sharedIdStr7 = name8;
                                                                        name10 = timeStampStr;
                                                                        long j40 = firstInstallTime2;
                                                                        resourcePathStr2 = resourcePathStr9;
                                                                        version2 = version5;
                                                                        idStr6 = idStr7;
                                                                        codePathStr4 = codePathStr7;
                                                                        long j41 = j39;
                                                                        long j42 = firstInstallTime9;
                                                                        settings = this;
                                                                        i2 = 5;
                                                                        PackageManagerService.reportSettingsProblem(i2, "Error in package manager settings: package " + name10 + " has bad userId " + idStr6 + " at " + parser.getPositionDescription());
                                                                        timeStampStr = name10;
                                                                        idStr = idStr6;
                                                                        String str822222222222222222222222222222222 = sharedIdStr7;
                                                                        String str922222222222222222222222222222222 = codePathStr4;
                                                                        String str1022222222222222222222222222222222 = resourcePathStr2;
                                                                        String str1122222222222222222222222222222222 = cpuAbiOverrideString2;
                                                                        String str1222222222222222222222222222222222 = version2;
                                                                        version = updateAvailable;
                                                                        packageSetting = packageSetting2;
                                                                        idStr3 = installerPackageName2;
                                                                        uidError = uidError2;
                                                                        idStr2 = isOrphaned;
                                                                        codePathStr = volumeUuid;
                                                                        resourcePathStr = legacyNativeLibraryPathStr2;
                                                                        cpuAbiOverrideString = secondaryCpuAbiString2;
                                                                        categoryHint = categoryHint2;
                                                                        secondaryCpuAbiString = primaryCpuAbiString2;
                                                                        if (packageSetting != null) {
                                                                        }
                                                                    }
                                                                } catch (NumberFormatException e46) {
                                                                    long j43 = timeStamp5;
                                                                    sharedIdStr7 = name8;
                                                                    name10 = timeStampStr;
                                                                    long j44 = firstInstallTime2;
                                                                    resourcePathStr2 = resourcePathStr9;
                                                                    version2 = version5;
                                                                    idStr6 = idStr7;
                                                                    codePathStr4 = codePathStr6;
                                                                    long j45 = j43;
                                                                    long j46 = firstInstallTime9;
                                                                    settings = this;
                                                                    i2 = 5;
                                                                    PackageManagerService.reportSettingsProblem(i2, "Error in package manager settings: package " + name10 + " has bad userId " + idStr6 + " at " + parser.getPositionDescription());
                                                                    timeStampStr = name10;
                                                                    idStr = idStr6;
                                                                    String str8222222222222222222222222222222222 = sharedIdStr7;
                                                                    String str9222222222222222222222222222222222 = codePathStr4;
                                                                    String str10222222222222222222222222222222222 = resourcePathStr2;
                                                                    String str11222222222222222222222222222222222 = cpuAbiOverrideString2;
                                                                    String str12222222222222222222222222222222222 = version2;
                                                                    version = updateAvailable;
                                                                    packageSetting = packageSetting2;
                                                                    idStr3 = installerPackageName2;
                                                                    uidError = uidError2;
                                                                    idStr2 = isOrphaned;
                                                                    codePathStr = volumeUuid;
                                                                    resourcePathStr = legacyNativeLibraryPathStr2;
                                                                    cpuAbiOverrideString = secondaryCpuAbiString2;
                                                                    categoryHint = categoryHint2;
                                                                    secondaryCpuAbiString = primaryCpuAbiString2;
                                                                    if (packageSetting != null) {
                                                                    }
                                                                }
                                                                if (packageSetting != null) {
                                                                }
                                                            } else {
                                                                int i13 = userId;
                                                                timeStamp2 = timeStamp5;
                                                                codePathStr2 = codePathStr6;
                                                                resourcePathStr5 = resourcePathStr9;
                                                                firstInstallTime = firstInstallTime9;
                                                                settings6 = this;
                                                                try {
                                                                    i4 = 5;
                                                                    try {
                                                                        PackageManagerService.reportSettingsProblem(5, "Error in package manager settings: package " + timeStampStr + " has bad sharedId " + name8 + " at " + parser.getPositionDescription());
                                                                        sharedIdStr3 = idStr7;
                                                                        String parentPackageName222 = name8;
                                                                        resourcePathStr = legacyNativeLibraryPathStr2;
                                                                        String str6822 = cpuAbiOverrideString2;
                                                                        long j2222 = firstInstallTime2;
                                                                        String str6922 = resourcePathStr5;
                                                                        categoryHint = categoryHint2;
                                                                        packageSetting = packageSetting2;
                                                                        uidError = uidError2;
                                                                        codePathStr = volumeUuid;
                                                                        long j2322 = timeStamp2;
                                                                        long j2422 = firstInstallTime;
                                                                        int i622 = i4;
                                                                        idStr = sharedIdStr3;
                                                                        cpuAbiOverrideString = secondaryCpuAbiString2;
                                                                        settings = settings6;
                                                                        version = updateAvailable;
                                                                        idStr3 = installerPackageName2;
                                                                        secondaryCpuAbiString = primaryCpuAbiString2;
                                                                        idStr2 = isOrphaned;
                                                                    } catch (NumberFormatException e47) {
                                                                        sharedIdStr7 = name8;
                                                                        name10 = timeStampStr;
                                                                        long j47 = firstInstallTime2;
                                                                        resourcePathStr2 = resourcePathStr5;
                                                                        codePathStr4 = codePathStr2;
                                                                        long j48 = timeStamp2;
                                                                        long j49 = firstInstallTime;
                                                                        i2 = 5;
                                                                        settings = settings6;
                                                                        version2 = version5;
                                                                        idStr6 = idStr7;
                                                                        PackageManagerService.reportSettingsProblem(i2, "Error in package manager settings: package " + name10 + " has bad userId " + idStr6 + " at " + parser.getPositionDescription());
                                                                        timeStampStr = name10;
                                                                        idStr = idStr6;
                                                                        String str82222222222222222222222222222222222 = sharedIdStr7;
                                                                        String str92222222222222222222222222222222222 = codePathStr4;
                                                                        String str102222222222222222222222222222222222 = resourcePathStr2;
                                                                        String str112222222222222222222222222222222222 = cpuAbiOverrideString2;
                                                                        String str122222222222222222222222222222222222 = version2;
                                                                        version = updateAvailable;
                                                                        packageSetting = packageSetting2;
                                                                        idStr3 = installerPackageName2;
                                                                        uidError = uidError2;
                                                                        idStr2 = isOrphaned;
                                                                        codePathStr = volumeUuid;
                                                                        resourcePathStr = legacyNativeLibraryPathStr2;
                                                                        cpuAbiOverrideString = secondaryCpuAbiString2;
                                                                        categoryHint = categoryHint2;
                                                                        secondaryCpuAbiString = primaryCpuAbiString2;
                                                                        if (packageSetting != null) {
                                                                        }
                                                                    }
                                                                } catch (NumberFormatException e48) {
                                                                    sharedIdStr4 = name8;
                                                                    name10 = timeStampStr;
                                                                    long lastUpdateTime4 = firstInstallTime2;
                                                                    resourcePathStr2 = resourcePathStr5;
                                                                    idStr6 = idStr7;
                                                                    codePathStr4 = codePathStr2;
                                                                    i2 = 5;
                                                                    settings = settings6;
                                                                    version2 = version5;
                                                                    PackageManagerService.reportSettingsProblem(i2, "Error in package manager settings: package " + name10 + " has bad userId " + idStr6 + " at " + parser.getPositionDescription());
                                                                    timeStampStr = name10;
                                                                    idStr = idStr6;
                                                                    String str822222222222222222222222222222222222 = sharedIdStr7;
                                                                    String str922222222222222222222222222222222222 = codePathStr4;
                                                                    String str1022222222222222222222222222222222222 = resourcePathStr2;
                                                                    String str1122222222222222222222222222222222222 = cpuAbiOverrideString2;
                                                                    String str1222222222222222222222222222222222222 = version2;
                                                                    version = updateAvailable;
                                                                    packageSetting = packageSetting2;
                                                                    idStr3 = installerPackageName2;
                                                                    uidError = uidError2;
                                                                    idStr2 = isOrphaned;
                                                                    codePathStr = volumeUuid;
                                                                    resourcePathStr = legacyNativeLibraryPathStr2;
                                                                    cpuAbiOverrideString = secondaryCpuAbiString2;
                                                                    categoryHint = categoryHint2;
                                                                    secondaryCpuAbiString = primaryCpuAbiString2;
                                                                    if (packageSetting != null) {
                                                                    }
                                                                }
                                                                if (packageSetting != null) {
                                                                }
                                                            }
                                                        } else {
                                                            int i14 = userId;
                                                            timeStamp2 = timeStamp5;
                                                            String codePathStr8 = codePathStr6;
                                                            resourcePathStr5 = resourcePathStr9;
                                                            int i15 = sharedUserId2;
                                                            firstInstallTime = firstInstallTime9;
                                                            i4 = 5;
                                                            settings6 = this;
                                                            try {
                                                                StringBuilder sb5 = new StringBuilder();
                                                                sb5.append("Error in package manager settings: package ");
                                                                sb5.append(timeStampStr);
                                                                sb5.append(" has bad userId ");
                                                                sharedIdStr3 = idStr7;
                                                                try {
                                                                    sb5.append(sharedIdStr3);
                                                                    sb5.append(" at ");
                                                                    sb5.append(parser.getPositionDescription());
                                                                    PackageManagerService.reportSettingsProblem(5, sb5.toString());
                                                                    String parentPackageName2222 = name8;
                                                                    resourcePathStr = legacyNativeLibraryPathStr2;
                                                                    String str68222 = cpuAbiOverrideString2;
                                                                    long j22222 = firstInstallTime2;
                                                                    String str69222 = resourcePathStr5;
                                                                    categoryHint = categoryHint2;
                                                                    packageSetting = packageSetting2;
                                                                    uidError = uidError2;
                                                                    codePathStr = volumeUuid;
                                                                    long j23222 = timeStamp2;
                                                                    long j24222 = firstInstallTime;
                                                                    int i6222 = i4;
                                                                    idStr = sharedIdStr3;
                                                                    cpuAbiOverrideString = secondaryCpuAbiString2;
                                                                    settings = settings6;
                                                                    version = updateAvailable;
                                                                    idStr3 = installerPackageName2;
                                                                    secondaryCpuAbiString = primaryCpuAbiString2;
                                                                    idStr2 = isOrphaned;
                                                                } catch (NumberFormatException e49) {
                                                                    long j50 = firstInstallTime2;
                                                                    resourcePathStr2 = resourcePathStr5;
                                                                    codePathStr4 = codePathStr8;
                                                                    long j51 = timeStamp2;
                                                                    long j52 = firstInstallTime;
                                                                    i2 = 5;
                                                                    idStr6 = sharedIdStr3;
                                                                    settings = settings6;
                                                                    version2 = version5;
                                                                    sharedIdStr7 = name8;
                                                                    name10 = timeStampStr;
                                                                    PackageManagerService.reportSettingsProblem(i2, "Error in package manager settings: package " + name10 + " has bad userId " + idStr6 + " at " + parser.getPositionDescription());
                                                                    timeStampStr = name10;
                                                                    idStr = idStr6;
                                                                    String str8222222222222222222222222222222222222 = sharedIdStr7;
                                                                    String str9222222222222222222222222222222222222 = codePathStr4;
                                                                    String str10222222222222222222222222222222222222 = resourcePathStr2;
                                                                    String str11222222222222222222222222222222222222 = cpuAbiOverrideString2;
                                                                    String str12222222222222222222222222222222222222 = version2;
                                                                    version = updateAvailable;
                                                                    packageSetting = packageSetting2;
                                                                    idStr3 = installerPackageName2;
                                                                    uidError = uidError2;
                                                                    idStr2 = isOrphaned;
                                                                    codePathStr = volumeUuid;
                                                                    resourcePathStr = legacyNativeLibraryPathStr2;
                                                                    cpuAbiOverrideString = secondaryCpuAbiString2;
                                                                    categoryHint = categoryHint2;
                                                                    secondaryCpuAbiString = primaryCpuAbiString2;
                                                                    if (packageSetting != null) {
                                                                    }
                                                                }
                                                            } catch (NumberFormatException e50) {
                                                                long j53 = firstInstallTime2;
                                                                resourcePathStr2 = resourcePathStr5;
                                                                codePathStr4 = codePathStr8;
                                                                long j54 = timeStamp2;
                                                                long j55 = firstInstallTime;
                                                                i2 = 5;
                                                                idStr6 = idStr7;
                                                                settings = settings6;
                                                                version2 = version5;
                                                                sharedIdStr7 = name8;
                                                                name10 = timeStampStr;
                                                                PackageManagerService.reportSettingsProblem(i2, "Error in package manager settings: package " + name10 + " has bad userId " + idStr6 + " at " + parser.getPositionDescription());
                                                                timeStampStr = name10;
                                                                idStr = idStr6;
                                                                String str82222222222222222222222222222222222222 = sharedIdStr7;
                                                                String str92222222222222222222222222222222222222 = codePathStr4;
                                                                String str102222222222222222222222222222222222222 = resourcePathStr2;
                                                                String str112222222222222222222222222222222222222 = cpuAbiOverrideString2;
                                                                String str122222222222222222222222222222222222222 = version2;
                                                                version = updateAvailable;
                                                                packageSetting = packageSetting2;
                                                                idStr3 = installerPackageName2;
                                                                uidError = uidError2;
                                                                idStr2 = isOrphaned;
                                                                codePathStr = volumeUuid;
                                                                resourcePathStr = legacyNativeLibraryPathStr2;
                                                                cpuAbiOverrideString = secondaryCpuAbiString2;
                                                                categoryHint = categoryHint2;
                                                                secondaryCpuAbiString = primaryCpuAbiString2;
                                                                if (packageSetting != null) {
                                                                }
                                                            }
                                                            if (packageSetting != null) {
                                                            }
                                                        }
                                                    }
                                                }
                                                resourcePathStr5 = resourcePathStr2;
                                                String parentPackageName22222 = name8;
                                                resourcePathStr = legacyNativeLibraryPathStr2;
                                                String str682222 = cpuAbiOverrideString2;
                                                long j222222 = firstInstallTime2;
                                                String str692222 = resourcePathStr5;
                                                categoryHint = categoryHint2;
                                                packageSetting = packageSetting2;
                                                uidError = uidError2;
                                                codePathStr = volumeUuid;
                                                long j232222 = timeStamp2;
                                                long j242222 = firstInstallTime;
                                                int i62222 = i4;
                                                idStr = sharedIdStr3;
                                                cpuAbiOverrideString = secondaryCpuAbiString2;
                                                settings = settings6;
                                                version = updateAvailable;
                                                idStr3 = installerPackageName2;
                                                secondaryCpuAbiString = primaryCpuAbiString2;
                                                idStr2 = isOrphaned;
                                            } catch (NumberFormatException e51) {
                                                String str88 = codePathStr4;
                                                String str89 = categoryHintString;
                                                String str90 = version2;
                                                long j56 = timeStamp;
                                                long j57 = firstInstallTime6;
                                                long j58 = lastUpdateTime2;
                                                String str91 = realName;
                                                settings = this;
                                                resourcePathStr2 = resourcePathStr4;
                                                i2 = 5;
                                                idStr6 = idStr6;
                                                sharedIdStr7 = sharedIdStr7;
                                                name10 = name10;
                                                PackageManagerService.reportSettingsProblem(i2, "Error in package manager settings: package " + name10 + " has bad userId " + idStr6 + " at " + parser.getPositionDescription());
                                                timeStampStr = name10;
                                                idStr = idStr6;
                                                String str822222222222222222222222222222222222222 = sharedIdStr7;
                                                String str922222222222222222222222222222222222222 = codePathStr4;
                                                String str1022222222222222222222222222222222222222 = resourcePathStr2;
                                                String str1122222222222222222222222222222222222222 = cpuAbiOverrideString2;
                                                String str1222222222222222222222222222222222222222 = version2;
                                                version = updateAvailable;
                                                packageSetting = packageSetting2;
                                                idStr3 = installerPackageName2;
                                                uidError = uidError2;
                                                idStr2 = isOrphaned;
                                                codePathStr = volumeUuid;
                                                resourcePathStr = legacyNativeLibraryPathStr2;
                                                cpuAbiOverrideString = secondaryCpuAbiString2;
                                                categoryHint = categoryHint2;
                                                secondaryCpuAbiString = primaryCpuAbiString2;
                                                if (packageSetting != null) {
                                                }
                                            }
                                        } catch (NumberFormatException e52) {
                                            String str93 = codePathStr4;
                                            String str94 = categoryHintString;
                                            String str95 = version2;
                                            long j59 = timeStamp;
                                            long j60 = firstInstallTime6;
                                            String str96 = realName;
                                            settings = this;
                                            resourcePathStr2 = resourcePathStr4;
                                            i2 = 5;
                                            idStr6 = idStr6;
                                            sharedIdStr7 = sharedIdStr7;
                                            name10 = name10;
                                            PackageManagerService.reportSettingsProblem(i2, "Error in package manager settings: package " + name10 + " has bad userId " + idStr6 + " at " + parser.getPositionDescription());
                                            timeStampStr = name10;
                                            idStr = idStr6;
                                            String str8222222222222222222222222222222222222222 = sharedIdStr7;
                                            String str9222222222222222222222222222222222222222 = codePathStr4;
                                            String str10222222222222222222222222222222222222222 = resourcePathStr2;
                                            String str11222222222222222222222222222222222222222 = cpuAbiOverrideString2;
                                            String str12222222222222222222222222222222222222222 = version2;
                                            version = updateAvailable;
                                            packageSetting = packageSetting2;
                                            idStr3 = installerPackageName2;
                                            uidError = uidError2;
                                            idStr2 = isOrphaned;
                                            codePathStr = volumeUuid;
                                            resourcePathStr = legacyNativeLibraryPathStr2;
                                            cpuAbiOverrideString = secondaryCpuAbiString2;
                                            categoryHint = categoryHint2;
                                            secondaryCpuAbiString = primaryCpuAbiString2;
                                            if (packageSetting != null) {
                                            }
                                        }
                                    } catch (NumberFormatException e53) {
                                        String str97 = codePathStr4;
                                        String str98 = categoryHintString;
                                        String str99 = version2;
                                        long j61 = timeStamp;
                                        String str100 = realName;
                                        settings = this;
                                        resourcePathStr2 = resourcePathStr4;
                                        i2 = 5;
                                        idStr6 = idStr6;
                                        sharedIdStr7 = sharedIdStr7;
                                        name10 = name10;
                                        PackageManagerService.reportSettingsProblem(i2, "Error in package manager settings: package " + name10 + " has bad userId " + idStr6 + " at " + parser.getPositionDescription());
                                        timeStampStr = name10;
                                        idStr = idStr6;
                                        String str82222222222222222222222222222222222222222 = sharedIdStr7;
                                        String str92222222222222222222222222222222222222222 = codePathStr4;
                                        String str102222222222222222222222222222222222222222 = resourcePathStr2;
                                        String str112222222222222222222222222222222222222222 = cpuAbiOverrideString2;
                                        String str122222222222222222222222222222222222222222 = version2;
                                        version = updateAvailable;
                                        packageSetting = packageSetting2;
                                        idStr3 = installerPackageName2;
                                        uidError = uidError2;
                                        idStr2 = isOrphaned;
                                        codePathStr = volumeUuid;
                                        resourcePathStr = legacyNativeLibraryPathStr2;
                                        cpuAbiOverrideString = secondaryCpuAbiString2;
                                        categoryHint = categoryHint2;
                                        secondaryCpuAbiString = primaryCpuAbiString2;
                                        if (packageSetting != null) {
                                        }
                                    }
                                    if (packageSetting != null) {
                                    }
                                }
                                versionCode = 0;
                                try {
                                    installerPackageName2 = xmlPullParser.getAttributeValue(null, "installer");
                                    isOrphaned = xmlPullParser.getAttributeValue(null, "isOrphaned");
                                    volumeUuid = xmlPullParser.getAttributeValue(null, ATTR_VOLUME_UUID);
                                    categoryHintString = xmlPullParser.getAttributeValue(null, "categoryHint");
                                    if (categoryHintString != null) {
                                    }
                                    systemStr = xmlPullParser.getAttributeValue(null, "publicFlags");
                                    if (systemStr == null) {
                                    }
                                } catch (NumberFormatException e54) {
                                    str4 = name10;
                                    name6 = sharedIdStr7;
                                    String str101 = codePathStr4;
                                    String str103 = version2;
                                    String sharedIdStr16 = idStr6;
                                    settings5 = this;
                                    installerPackageName2 = null;
                                    isOrphaned = null;
                                    volumeUuid = null;
                                    resourcePathStr2 = resourcePathStr4;
                                    i2 = 5;
                                    sharedIdStr7 = name6;
                                    name10 = str4;
                                    PackageManagerService.reportSettingsProblem(i2, "Error in package manager settings: package " + name10 + " has bad userId " + idStr6 + " at " + parser.getPositionDescription());
                                    timeStampStr = name10;
                                    idStr = idStr6;
                                    String str822222222222222222222222222222222222222222 = sharedIdStr7;
                                    String str922222222222222222222222222222222222222222 = codePathStr4;
                                    String str1022222222222222222222222222222222222222222 = resourcePathStr2;
                                    String str1122222222222222222222222222222222222222222 = cpuAbiOverrideString2;
                                    String str1222222222222222222222222222222222222222222 = version2;
                                    version = updateAvailable;
                                    packageSetting = packageSetting2;
                                    idStr3 = installerPackageName2;
                                    uidError = uidError2;
                                    idStr2 = isOrphaned;
                                    codePathStr = volumeUuid;
                                    resourcePathStr = legacyNativeLibraryPathStr2;
                                    cpuAbiOverrideString = secondaryCpuAbiString2;
                                    categoryHint = categoryHint2;
                                    secondaryCpuAbiString = primaryCpuAbiString2;
                                    if (packageSetting != null) {
                                    }
                                }
                            } catch (NumberFormatException e55) {
                                str3 = name10;
                                name5 = sharedIdStr7;
                                String str104 = codePathStr4;
                                String sharedIdStr17 = idStr6;
                                settings = this;
                                isOrphaned = null;
                                volumeUuid = null;
                                version2 = null;
                                resourcePathStr2 = resourcePathStr4;
                                i2 = 5;
                                sharedIdStr7 = name5;
                                name10 = str3;
                                installerPackageName2 = null;
                                PackageManagerService.reportSettingsProblem(i2, "Error in package manager settings: package " + name10 + " has bad userId " + idStr6 + " at " + parser.getPositionDescription());
                                timeStampStr = name10;
                                idStr = idStr6;
                                String str8222222222222222222222222222222222222222222 = sharedIdStr7;
                                String str9222222222222222222222222222222222222222222 = codePathStr4;
                                String str10222222222222222222222222222222222222222222 = resourcePathStr2;
                                String str11222222222222222222222222222222222222222222 = cpuAbiOverrideString2;
                                String str12222222222222222222222222222222222222222222 = version2;
                                version = updateAvailable;
                                packageSetting = packageSetting2;
                                idStr3 = installerPackageName2;
                                uidError = uidError2;
                                idStr2 = isOrphaned;
                                codePathStr = volumeUuid;
                                resourcePathStr = legacyNativeLibraryPathStr2;
                                cpuAbiOverrideString = secondaryCpuAbiString2;
                                categoryHint = categoryHint2;
                                secondaryCpuAbiString = primaryCpuAbiString2;
                                if (packageSetting != null) {
                                }
                            }
                        } catch (NumberFormatException e56) {
                            str3 = name10;
                            name5 = sharedIdStr7;
                            String str105 = codePathStr4;
                            String sharedIdStr18 = idStr6;
                            primaryCpuAbiString2 = resourcePathStr8;
                            settings = this;
                            isOrphaned = null;
                            volumeUuid = null;
                            version2 = null;
                            resourcePathStr2 = resourcePathStr4;
                            i2 = 5;
                            sharedIdStr7 = name5;
                            name10 = str3;
                            installerPackageName2 = null;
                            PackageManagerService.reportSettingsProblem(i2, "Error in package manager settings: package " + name10 + " has bad userId " + idStr6 + " at " + parser.getPositionDescription());
                            timeStampStr = name10;
                            idStr = idStr6;
                            String str82222222222222222222222222222222222222222222 = sharedIdStr7;
                            String str92222222222222222222222222222222222222222222 = codePathStr4;
                            String str102222222222222222222222222222222222222222222 = resourcePathStr2;
                            String str112222222222222222222222222222222222222222222 = cpuAbiOverrideString2;
                            String str122222222222222222222222222222222222222222222 = version2;
                            version = updateAvailable;
                            packageSetting = packageSetting2;
                            idStr3 = installerPackageName2;
                            uidError = uidError2;
                            idStr2 = isOrphaned;
                            codePathStr = volumeUuid;
                            resourcePathStr = legacyNativeLibraryPathStr2;
                            cpuAbiOverrideString = secondaryCpuAbiString2;
                            categoryHint = categoryHint2;
                            secondaryCpuAbiString = primaryCpuAbiString2;
                            if (packageSetting != null) {
                            }
                        }
                    } catch (NumberFormatException e57) {
                        str2 = name10;
                        name4 = sharedIdStr7;
                        String str106 = codePathStr4;
                        String sharedIdStr19 = idStr6;
                        settings4 = this;
                        isOrphaned = null;
                        volumeUuid = null;
                        version2 = null;
                        cpuAbiOverrideString2 = null;
                        i2 = 5;
                        sharedIdStr7 = name4;
                        primaryCpuAbiString2 = resourcePathStr8;
                        name10 = str2;
                        installerPackageName2 = null;
                        resourcePathStr2 = resourcePathStr4;
                        PackageManagerService.reportSettingsProblem(i2, "Error in package manager settings: package " + name10 + " has bad userId " + idStr6 + " at " + parser.getPositionDescription());
                        timeStampStr = name10;
                        idStr = idStr6;
                        String str822222222222222222222222222222222222222222222 = sharedIdStr7;
                        String str922222222222222222222222222222222222222222222 = codePathStr4;
                        String str1022222222222222222222222222222222222222222222 = resourcePathStr2;
                        String str1122222222222222222222222222222222222222222222 = cpuAbiOverrideString2;
                        String str1222222222222222222222222222222222222222222222 = version2;
                        version = updateAvailable;
                        packageSetting = packageSetting2;
                        idStr3 = installerPackageName2;
                        uidError = uidError2;
                        idStr2 = isOrphaned;
                        codePathStr = volumeUuid;
                        resourcePathStr = legacyNativeLibraryPathStr2;
                        cpuAbiOverrideString = secondaryCpuAbiString2;
                        categoryHint = categoryHint2;
                        secondaryCpuAbiString = primaryCpuAbiString2;
                        if (packageSetting != null) {
                        }
                    }
                    try {
                        timeStampStr2 = xmlPullParser.getAttributeValue(null, "ft");
                        if (timeStampStr2 == null) {
                        }
                        timeStamp = timeStamp4;
                        timeStampStr3 = xmlPullParser.getAttributeValue(null, "it");
                        if (timeStampStr3 != null) {
                        }
                        long firstInstallTime62 = firstInstallTime5;
                        timeStampStr4 = xmlPullParser.getAttributeValue(null, "ut");
                        if (timeStampStr4 != null) {
                        }
                        long lastUpdateTime22 = lastUpdateTime;
                        if (!PackageManagerService.DEBUG_SETTINGS) {
                        }
                        if (idStr6 == null) {
                        }
                        int sharedUserId4 = sharedIdStr7 == null ? Integer.parseInt(sharedIdStr7) : 0;
                        if (resourcePathStr4 != null) {
                        }
                        if (realName != null) {
                        }
                        String realName22 = realName;
                        if (name10 != null) {
                        }
                        resourcePathStr5 = resourcePathStr2;
                        String parentPackageName222222 = name8;
                        resourcePathStr = legacyNativeLibraryPathStr2;
                        String str6822222 = cpuAbiOverrideString2;
                        long j2222222 = firstInstallTime2;
                        String str6922222 = resourcePathStr5;
                        categoryHint = categoryHint2;
                        packageSetting = packageSetting2;
                        uidError = uidError2;
                        codePathStr = volumeUuid;
                        long j2322222 = timeStamp2;
                        long j2422222 = firstInstallTime;
                        int i622222 = i4;
                        idStr = sharedIdStr3;
                        cpuAbiOverrideString = secondaryCpuAbiString2;
                        settings = settings6;
                        version = updateAvailable;
                        idStr3 = installerPackageName2;
                        secondaryCpuAbiString = primaryCpuAbiString2;
                        idStr2 = isOrphaned;
                    } catch (NumberFormatException e58) {
                        str5 = name10;
                        name7 = sharedIdStr7;
                        String str107 = codePathStr4;
                        String str108 = categoryHintString;
                        String str109 = version2;
                        sharedIdStr2 = idStr6;
                        i3 = 5;
                        settings = this;
                        resourcePathStr2 = resourcePathStr4;
                        i2 = i3;
                        idStr6 = sharedIdStr2;
                        sharedIdStr7 = name7;
                        name10 = str5;
                        PackageManagerService.reportSettingsProblem(i2, "Error in package manager settings: package " + name10 + " has bad userId " + idStr6 + " at " + parser.getPositionDescription());
                        timeStampStr = name10;
                        idStr = idStr6;
                        String str8222222222222222222222222222222222222222222222 = sharedIdStr7;
                        String str9222222222222222222222222222222222222222222222 = codePathStr4;
                        String str10222222222222222222222222222222222222222222222 = resourcePathStr2;
                        String str11222222222222222222222222222222222222222222222 = cpuAbiOverrideString2;
                        String str12222222222222222222222222222222222222222222222 = version2;
                        version = updateAvailable;
                        packageSetting = packageSetting2;
                        idStr3 = installerPackageName2;
                        uidError = uidError2;
                        idStr2 = isOrphaned;
                        codePathStr = volumeUuid;
                        resourcePathStr = legacyNativeLibraryPathStr2;
                        cpuAbiOverrideString = secondaryCpuAbiString2;
                        categoryHint = categoryHint2;
                        secondaryCpuAbiString = primaryCpuAbiString2;
                        if (packageSetting != null) {
                        }
                    }
                } catch (NumberFormatException e59) {
                    str2 = name10;
                    name4 = sharedIdStr7;
                    String str110 = codePathStr4;
                    settings4 = this;
                    String sharedIdStr20 = idStr6;
                    secondaryCpuAbiString2 = null;
                    isOrphaned = null;
                    volumeUuid = null;
                    version2 = null;
                    cpuAbiOverrideString2 = null;
                    i2 = 5;
                    sharedIdStr7 = name4;
                    primaryCpuAbiString2 = resourcePathStr8;
                    name10 = str2;
                    installerPackageName2 = null;
                    resourcePathStr2 = resourcePathStr4;
                    PackageManagerService.reportSettingsProblem(i2, "Error in package manager settings: package " + name10 + " has bad userId " + idStr6 + " at " + parser.getPositionDescription());
                    timeStampStr = name10;
                    idStr = idStr6;
                    String str82222222222222222222222222222222222222222222222 = sharedIdStr7;
                    String str92222222222222222222222222222222222222222222222 = codePathStr4;
                    String str102222222222222222222222222222222222222222222222 = resourcePathStr2;
                    String str112222222222222222222222222222222222222222222222 = cpuAbiOverrideString2;
                    String str122222222222222222222222222222222222222222222222 = version2;
                    version = updateAvailable;
                    packageSetting = packageSetting2;
                    idStr3 = installerPackageName2;
                    uidError = uidError2;
                    idStr2 = isOrphaned;
                    codePathStr = volumeUuid;
                    resourcePathStr = legacyNativeLibraryPathStr2;
                    cpuAbiOverrideString = secondaryCpuAbiString2;
                    categoryHint = categoryHint2;
                    secondaryCpuAbiString = primaryCpuAbiString2;
                    if (packageSetting != null) {
                    }
                }
            } catch (NumberFormatException e60) {
                str = name10;
                name3 = sharedIdStr7;
                String str111 = codePathStr4;
                settings3 = this;
                String sharedIdStr21 = idStr6;
                secondaryCpuAbiString2 = null;
                isOrphaned = null;
                volumeUuid = null;
                version3 = null;
                resourcePathStr3 = resourcePathStr4;
                i2 = 5;
                sharedIdStr = name3;
                name2 = str;
                installerPackageName2 = null;
                primaryCpuAbiString2 = null;
                cpuAbiOverrideString2 = null;
                PackageManagerService.reportSettingsProblem(i2, "Error in package manager settings: package " + name10 + " has bad userId " + idStr6 + " at " + parser.getPositionDescription());
                timeStampStr = name10;
                idStr = idStr6;
                String str822222222222222222222222222222222222222222222222 = sharedIdStr7;
                String str922222222222222222222222222222222222222222222222 = codePathStr4;
                String str1022222222222222222222222222222222222222222222222 = resourcePathStr2;
                String str1122222222222222222222222222222222222222222222222 = cpuAbiOverrideString2;
                String str1222222222222222222222222222222222222222222222222 = version2;
                version = updateAvailable;
                packageSetting = packageSetting2;
                idStr3 = installerPackageName2;
                uidError = uidError2;
                idStr2 = isOrphaned;
                codePathStr = volumeUuid;
                resourcePathStr = legacyNativeLibraryPathStr2;
                cpuAbiOverrideString = secondaryCpuAbiString2;
                categoryHint = categoryHint2;
                secondaryCpuAbiString = primaryCpuAbiString2;
                if (packageSetting != null) {
                }
            }
        } catch (NumberFormatException e61) {
            settings = this;
            i2 = 5;
            uidError2 = null;
            isOrphaned = null;
            volumeUuid = null;
            resourcePathStr2 = null;
            legacyNativeLibraryPathStr2 = null;
            secondaryCpuAbiString2 = null;
            version2 = null;
            installerPackageName2 = null;
            primaryCpuAbiString2 = null;
            cpuAbiOverrideString2 = null;
            PackageManagerService.reportSettingsProblem(i2, "Error in package manager settings: package " + name10 + " has bad userId " + idStr6 + " at " + parser.getPositionDescription());
            timeStampStr = name10;
            idStr = idStr6;
            String str8222222222222222222222222222222222222222222222222 = sharedIdStr7;
            String str9222222222222222222222222222222222222222222222222 = codePathStr4;
            String str10222222222222222222222222222222222222222222222222 = resourcePathStr2;
            String str11222222222222222222222222222222222222222222222222 = cpuAbiOverrideString2;
            String str12222222222222222222222222222222222222222222222222 = version2;
            version = updateAvailable;
            packageSetting = packageSetting2;
            idStr3 = installerPackageName2;
            uidError = uidError2;
            idStr2 = isOrphaned;
            codePathStr = volumeUuid;
            resourcePathStr = legacyNativeLibraryPathStr2;
            cpuAbiOverrideString = secondaryCpuAbiString2;
            categoryHint = categoryHint2;
            secondaryCpuAbiString = primaryCpuAbiString2;
            if (packageSetting != null) {
            }
        }
        if (packageSetting != null) {
        }
    }

    private void readDisabledComponentsLPw(PackageSettingBase packageSetting, XmlPullParser parser, int userId) throws IOException, XmlPullParserException {
        int outerDepth = parser.getDepth();
        while (true) {
            int next = parser.next();
            int type = next;
            if (next == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == 3 || type == 4)) {
                if (parser.getName().equals(TAG_ITEM)) {
                    String name = parser.getAttributeValue(null, ATTR_NAME);
                    if (name != null) {
                        packageSetting.addDisabledComponent(name.intern(), userId);
                    } else {
                        PackageManagerService.reportSettingsProblem(5, "Error in package manager settings: <disabled-components> has no name at " + parser.getPositionDescription());
                    }
                } else {
                    PackageManagerService.reportSettingsProblem(5, "Unknown element under <disabled-components>: " + parser.getName());
                }
                XmlUtils.skipCurrentTag(parser);
            }
        }
    }

    private void readEnabledComponentsLPw(PackageSettingBase packageSetting, XmlPullParser parser, int userId) throws IOException, XmlPullParserException {
        int outerDepth = parser.getDepth();
        while (true) {
            int next = parser.next();
            int type = next;
            if (next == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == 3 || type == 4)) {
                if (parser.getName().equals(TAG_ITEM)) {
                    String name = parser.getAttributeValue(null, ATTR_NAME);
                    if (name != null) {
                        packageSetting.addEnabledComponent(name.intern(), userId);
                    } else {
                        PackageManagerService.reportSettingsProblem(5, "Error in package manager settings: <enabled-components> has no name at " + parser.getPositionDescription());
                    }
                } else {
                    PackageManagerService.reportSettingsProblem(5, "Unknown element under <enabled-components>: " + parser.getName());
                }
                XmlUtils.skipCurrentTag(parser);
            }
        }
    }

    private void readSharedUserLPw(XmlPullParser parser) throws XmlPullParserException, IOException {
        int pkgFlags = 0;
        SharedUserSetting su = null;
        try {
            String name = parser.getAttributeValue(null, ATTR_NAME);
            String idStr = parser.getAttributeValue(null, "userId");
            int userId = idStr != null ? Integer.parseInt(idStr) : 0;
            if ("true".equals(parser.getAttributeValue(null, "system"))) {
                pkgFlags = 0 | 1;
            }
            if (name == null) {
                PackageManagerService.reportSettingsProblem(5, "Error in package manager settings: <shared-user> has no name at " + parser.getPositionDescription());
            } else if (userId == 0) {
                PackageManagerService.reportSettingsProblem(5, "Error in package manager settings: shared-user " + name + " has bad userId " + idStr + " at " + parser.getPositionDescription());
            } else {
                SharedUserSetting addSharedUserLPw = addSharedUserLPw(name.intern(), userId, pkgFlags, 0);
                su = addSharedUserLPw;
                if (addSharedUserLPw == null) {
                    PackageManagerService.reportSettingsProblem(6, "Occurred while parsing settings at " + parser.getPositionDescription());
                }
            }
        } catch (NumberFormatException e) {
            PackageManagerService.reportSettingsProblem(5, "Error in package manager settings: package " + null + " has bad userId " + null + " at " + parser.getPositionDescription());
        }
        if (su != null) {
            int outerDepth = parser.getDepth();
            while (true) {
                int next = parser.next();
                int type = next;
                if (next == 1) {
                    return;
                }
                if (type == 3 && parser.getDepth() <= outerDepth) {
                    return;
                }
                if (!(type == 3 || type == 4)) {
                    String tagName = parser.getName();
                    if (tagName.equals("sigs")) {
                        su.signatures.readXml(parser, this.mPastSignatures);
                    } else if (tagName.equals(TAG_PERMISSIONS)) {
                        readInstallPermissionsLPr(parser, su.getPermissionsState());
                    } else {
                        PackageManagerService.reportSettingsProblem(5, "Unknown element under <shared-user>: " + parser.getName());
                        XmlUtils.skipCurrentTag(parser);
                    }
                }
            }
        } else {
            XmlUtils.skipCurrentTag(parser);
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x00c8 A[Catch:{ all -> 0x006a, all -> 0x0144 }] */
    public void createNewUserLI(PackageManagerService service, Installer installer, int userHandle, String[] disallowedPackages) {
        int packagesCount;
        String[] volumeUuids;
        String[] names;
        int[] appIds;
        String[] seinfos;
        int[] targetSdkVersions;
        int flags;
        int packagesCount2;
        int i;
        Iterator<PackageSetting> packagesIterator;
        int i2;
        Collection<PackageSetting> packages;
        boolean z;
        boolean shouldInstall;
        boolean shouldInstall2;
        PackageSetting ps;
        int i3 = userHandle;
        synchronized (this.mPackages) {
            try {
                Collection<PackageSetting> packages2 = this.mPackages.values();
                packagesCount = packages2.size();
                volumeUuids = new String[packagesCount];
                names = new String[packagesCount];
                appIds = new int[packagesCount];
                seinfos = new String[packagesCount];
                targetSdkVersions = new int[packagesCount];
                Iterator<PackageSetting> packagesIterator2 = packages2.iterator();
                flags = 0;
                int i4 = 0;
                while (true) {
                    int i5 = i4;
                    if (i5 >= packagesCount) {
                        break;
                    }
                    PackageSetting ps2 = packagesIterator2.next();
                    if (ps2.pkg == null) {
                        packages = packages2;
                        i2 = i5;
                        packagesIterator = packagesIterator2;
                    } else if (ps2.pkg.applicationInfo == null) {
                        packages = packages2;
                        i2 = i5;
                        packagesIterator = packagesIterator2;
                    } else {
                        boolean isSystemApp = ps2.isSystem();
                        if (this.mCustSettings != null && this.mCustSettings.isInNosysAppList(ps2.name)) {
                            isSystemApp = true;
                        }
                        if (!isSystemApp) {
                            String[] strArr = disallowedPackages;
                        } else if (!ArrayUtils.contains(disallowedPackages, ps2.name)) {
                            z = true;
                            shouldInstall = z;
                            ps2.setInstalled(shouldInstall, i3);
                            if (shouldInstall || !isInDelAppList(ps2.name)) {
                                packages = packages2;
                                shouldInstall2 = shouldInstall;
                                ps = ps2;
                                i2 = i5;
                                packagesIterator = packagesIterator2;
                            } else {
                                packages = packages2;
                                StringBuilder sb = new StringBuilder();
                                shouldInstall2 = shouldInstall;
                                sb.append("disable application: ");
                                sb.append(ps2.name);
                                sb.append(" for user ");
                                sb.append(i3);
                                Slog.w(TAG, sb.toString());
                                ps = ps2;
                                i2 = i5;
                                packagesIterator = packagesIterator2;
                                service.setApplicationEnabledSetting(ps2.name, 2, 0, i3, null);
                            }
                            if (!shouldInstall2) {
                                writeKernelMappingLPr(ps);
                            }
                            volumeUuids[i2] = ps.volumeUuid;
                            names[i2] = ps.name;
                            appIds[i2] = ps.appId;
                            seinfos[i2] = ps.pkg.applicationInfo.seInfo;
                            targetSdkVersions[i2] = ps.pkg.applicationInfo.targetSdkVersion;
                        }
                        z = false;
                        shouldInstall = z;
                        ps2.setInstalled(shouldInstall, i3);
                        if (shouldInstall) {
                        }
                        packages = packages2;
                        shouldInstall2 = shouldInstall;
                        ps = ps2;
                        i2 = i5;
                        packagesIterator = packagesIterator2;
                        if (!shouldInstall2) {
                        }
                        volumeUuids[i2] = ps.volumeUuid;
                        names[i2] = ps.name;
                        appIds[i2] = ps.appId;
                        seinfos[i2] = ps.pkg.applicationInfo.seInfo;
                        targetSdkVersions[i2] = ps.pkg.applicationInfo.targetSdkVersion;
                    }
                    i4 = i2 + 1;
                    packages2 = packages;
                    packagesIterator2 = packagesIterator;
                }
            } catch (Throwable th) {
                th = th;
                PackageManagerService packageManagerService = service;
                while (true) {
                    break;
                }
                throw th;
            }
        }
        while (true) {
            int i6 = flags;
            if (i6 < packagesCount) {
                if (names[i6] == null) {
                    i = i6;
                    packagesCount2 = packagesCount;
                } else {
                    try {
                        i = i6;
                        packagesCount2 = packagesCount;
                        try {
                            installer.createAppData(volumeUuids[i6], names[i6], i3, 3, appIds[i6], seinfos[i6], targetSdkVersions[i6]);
                        } catch (Installer.InstallerException e) {
                            e = e;
                        }
                    } catch (Installer.InstallerException e2) {
                        e = e2;
                        i = i6;
                        packagesCount2 = packagesCount;
                        Slog.w(TAG, "Failed to prepare app data", e);
                        flags = i + 1;
                        packagesCount = packagesCount2;
                    }
                }
                flags = i + 1;
                packagesCount = packagesCount2;
            } else {
                synchronized (this.mPackages) {
                    applyDefaultPreferredAppsLPw(service, i3);
                }
                return;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void removeUserLPw(int userId) {
        for (Map.Entry<String, PackageSetting> entry : this.mPackages.entrySet()) {
            entry.getValue().removeUser(userId);
        }
        this.mPreferredActivities.remove(userId);
        getUserPackagesStateFile(userId).delete();
        getUserPackagesStateBackupFile(userId).delete();
        removeCrossProfileIntentFiltersLPw(userId);
        this.mRuntimePermissionsPersistence.onUserRemovedLPw(userId);
        writePackageListLPr();
        writeKernelRemoveUserLPr(userId);
    }

    /* access modifiers changed from: package-private */
    public void removeCrossProfileIntentFiltersLPw(int userId) {
        synchronized (this.mCrossProfileIntentResolvers) {
            if (this.mCrossProfileIntentResolvers.get(userId) != null) {
                this.mCrossProfileIntentResolvers.remove(userId);
                writePackageRestrictionsLPr(userId);
            }
            int count = this.mCrossProfileIntentResolvers.size();
            for (int i = 0; i < count; i++) {
                int sourceUserId = this.mCrossProfileIntentResolvers.keyAt(i);
                CrossProfileIntentResolver cpir = this.mCrossProfileIntentResolvers.get(sourceUserId);
                boolean needsWriting = false;
                Iterator<CrossProfileIntentFilter> it = new ArraySet<>(cpir.filterSet()).iterator();
                while (it.hasNext()) {
                    CrossProfileIntentFilter cpif = it.next();
                    if (cpif.getTargetUserId() == userId) {
                        needsWriting = true;
                        cpir.removeFilter(cpif);
                    }
                }
                if (needsWriting) {
                    writePackageRestrictionsLPr(sourceUserId);
                }
            }
        }
    }

    private void setFirstAvailableUid(int uid) {
        if (uid > mFirstAvailableUid) {
            mFirstAvailableUid = uid;
        }
    }

    private int newUserIdLPw(Object obj) {
        int N = this.mUserIds.size();
        for (int i = mFirstAvailableUid; i < N; i++) {
            if (this.mUserIds.get(i) == null) {
                this.mUserIds.set(i, obj);
                return 10000 + i;
            }
        }
        if (N > 9958) {
            return retryNewUserIdLPw(obj);
        }
        this.mUserIds.add(obj);
        return 10000 + N;
    }

    private int retryNewUserIdLPw(Object obj) {
        int N;
        Slog.i(TAG, "retryNewUserIdLPw N:" + N + ",first available uid:" + mFirstAvailableUid);
        if (!this.isNeedRetryNewUserId) {
            Slog.i(TAG, "No need to retry to find an available UserId to assign!");
            return -1;
        }
        int appId = -1;
        int i = 0;
        while (true) {
            if (i >= N) {
                break;
            } else if (this.mUserIds.get(i) == null) {
                this.mUserIds.set(i, obj);
                appId = 10000 + i;
                mFirstAvailableUid = i + 1;
                Slog.i(TAG, "we find an available UserId " + appId + " to assign after retry, change first available uid to " + mFirstAvailableUid);
                break;
            } else {
                i++;
            }
        }
        if (appId < 0) {
            this.isNeedRetryNewUserId = false;
            Slog.e(TAG, "we could not find an available UserId to assign after retry!");
        }
        return appId;
    }

    public VerifierDeviceIdentity getVerifierDeviceIdentityLPw() {
        if (this.mVerifierDeviceIdentity == null) {
            this.mVerifierDeviceIdentity = VerifierDeviceIdentity.generate();
            writeLPr();
        }
        return this.mVerifierDeviceIdentity;
    }

    /* access modifiers changed from: package-private */
    public boolean hasOtherDisabledSystemPkgWithChildLPr(String parentPackageName, String childPackageName) {
        int packageCount = this.mDisabledSysPackages.size();
        for (int i = 0; i < packageCount; i++) {
            PackageSetting disabledPs = this.mDisabledSysPackages.valueAt(i);
            if (disabledPs.childPackageNames != null && !disabledPs.childPackageNames.isEmpty() && !disabledPs.name.equals(parentPackageName)) {
                int childCount = disabledPs.childPackageNames.size();
                for (int j = 0; j < childCount; j++) {
                    if (((String) disabledPs.childPackageNames.get(j)).equals(childPackageName)) {
                        return true;
                    }
                }
                continue;
            }
        }
        return false;
    }

    public PackageSetting getDisabledSystemPkgLPr(String name) {
        return this.mDisabledSysPackages.get(name);
    }

    /* access modifiers changed from: package-private */
    public boolean isEnabledAndMatchLPr(ComponentInfo componentInfo, int flags, int userId) {
        PackageSetting ps = this.mPackages.get(componentInfo.packageName);
        if (ps == null) {
            return false;
        }
        return ps.readUserState(userId).isMatch(componentInfo, flags);
    }

    /* access modifiers changed from: package-private */
    public String getInstallerPackageNameLPr(String packageName) {
        PackageSetting pkg = this.mPackages.get(packageName);
        if (pkg != null) {
            return pkg.installerPackageName;
        }
        throw new IllegalArgumentException("Unknown package: " + packageName);
    }

    /* access modifiers changed from: package-private */
    public boolean isOrphaned(String packageName) {
        PackageSetting pkg = this.mPackages.get(packageName);
        if (pkg != null) {
            return pkg.isOrphaned;
        }
        throw new IllegalArgumentException("Unknown package: " + packageName);
    }

    /* access modifiers changed from: package-private */
    public int getApplicationEnabledSettingLPr(String packageName, int userId) {
        PackageSetting pkg = this.mPackages.get(packageName);
        if (pkg != null) {
            return pkg.getEnabled(userId);
        }
        throw new IllegalArgumentException("Unknown package: " + packageName);
    }

    /* access modifiers changed from: package-private */
    public int getComponentEnabledSettingLPr(ComponentName componentName, int userId) {
        PackageSetting pkg = this.mPackages.get(componentName.getPackageName());
        if (pkg != null) {
            return pkg.getCurrentEnabledStateLPr(componentName.getClassName(), userId);
        }
        throw new IllegalArgumentException("Unknown component: " + componentName);
    }

    /* access modifiers changed from: package-private */
    public boolean wasPackageEverLaunchedLPr(String packageName, int userId) {
        PackageSetting pkgSetting = this.mPackages.get(packageName);
        if (pkgSetting != null) {
            return !pkgSetting.getNotLaunched(userId);
        }
        throw new IllegalArgumentException("Unknown package: " + packageName);
    }

    /* access modifiers changed from: package-private */
    public boolean setPackageStoppedStateLPw(PackageManagerService pm, String packageName, boolean stopped, boolean allowedByPermission, int uid, int userId) {
        int appId = UserHandle.getAppId(uid);
        PackageSetting pkgSetting = this.mPackages.get(packageName);
        if (pkgSetting == null) {
            throw new IllegalArgumentException("Unknown package: " + packageName);
        } else if (!allowedByPermission && appId != pkgSetting.appId) {
            throw new SecurityException("Permission Denial: attempt to change stopped state from pid=" + Binder.getCallingPid() + ", uid=" + uid + ", package uid=" + pkgSetting.appId);
        } else if (pkgSetting.getStopped(userId) == stopped) {
            return false;
        } else {
            pkgSetting.setStopped(stopped, userId);
            if (pkgSetting.getNotLaunched(userId)) {
                if (pkgSetting.installerPackageName != null) {
                    pm.notifyFirstLaunch(pkgSetting.name, pkgSetting.installerPackageName, userId);
                }
                pkgSetting.setNotLaunched(false, userId);
            }
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public void setHarmfulAppWarningLPw(String packageName, CharSequence warning, int userId) {
        PackageSetting pkgSetting = this.mPackages.get(packageName);
        if (pkgSetting != null) {
            pkgSetting.setHarmfulAppWarning(userId, warning == null ? null : warning.toString());
            return;
        }
        throw new IllegalArgumentException("Unknown package: " + packageName);
    }

    /* access modifiers changed from: package-private */
    public String getHarmfulAppWarningLPr(String packageName, int userId) {
        PackageSetting pkgSetting = this.mPackages.get(packageName);
        if (pkgSetting != null) {
            return pkgSetting.getHarmfulAppWarning(userId);
        }
        throw new IllegalArgumentException("Unknown package: " + packageName);
    }

    /* JADX INFO: finally extract failed */
    private static List<UserInfo> getAllUsers(UserManagerService userManager) {
        long id = Binder.clearCallingIdentity();
        try {
            List<UserInfo> users = userManager.getUsers(false);
            Binder.restoreCallingIdentity(id);
            return users;
        } catch (NullPointerException e) {
            Binder.restoreCallingIdentity(id);
            return null;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(id);
            throw th;
        }
    }

    /* access modifiers changed from: package-private */
    public List<PackageSetting> getVolumePackagesLPr(String volumeUuid) {
        ArrayList<PackageSetting> res = new ArrayList<>();
        for (int i = 0; i < this.mPackages.size(); i++) {
            PackageSetting setting = this.mPackages.valueAt(i);
            if (Objects.equals(volumeUuid, setting.volumeUuid)) {
                res.add(setting);
            }
        }
        return res;
    }

    static void printFlags(PrintWriter pw, int val, Object[] spec) {
        pw.print("[ ");
        for (int i = 0; i < spec.length; i += 2) {
            if ((val & spec[i].intValue()) != 0) {
                pw.print(spec[i + 1]);
                pw.print(" ");
            }
        }
        pw.print("]");
    }

    /* access modifiers changed from: package-private */
    public void dumpVersionLPr(IndentingPrintWriter pw) {
        pw.increaseIndent();
        for (int i = 0; i < this.mVersion.size(); i++) {
            String volumeUuid = this.mVersion.keyAt(i);
            VersionInfo ver = this.mVersion.valueAt(i);
            if (Objects.equals(StorageManager.UUID_PRIVATE_INTERNAL, volumeUuid)) {
                pw.println("Internal:");
            } else if (Objects.equals("primary_physical", volumeUuid)) {
                pw.println("External:");
            } else {
                pw.println("UUID " + volumeUuid + ":");
            }
            pw.increaseIndent();
            pw.printPair(ATTR_SDK_VERSION, Integer.valueOf(ver.sdkVersion));
            pw.printPair(ATTR_DATABASE_VERSION, Integer.valueOf(ver.databaseVersion));
            pw.println();
            pw.printPair(ATTR_FINGERPRINT, ver.fingerprint);
            pw.println();
            pw.printPair(ATTR_HWFINGERPRINT, ver.hwFingerprint);
            pw.println();
            pw.decreaseIndent();
        }
        pw.decreaseIndent();
    }

    /* access modifiers changed from: package-private */
    public void dumpPackageLPr(PrintWriter pw, String prefix, String checkinTag, ArraySet<String> permissionNames, PackageSetting ps, SimpleDateFormat sdf, Date date, List<UserInfo> users, boolean dumpAll) {
        UserInfo user;
        PrintWriter printWriter = pw;
        String str = prefix;
        String str2 = checkinTag;
        ArraySet<String> arraySet = permissionNames;
        PackageSetting packageSetting = ps;
        Date date2 = date;
        if (str2 != null) {
            printWriter.print(str2);
            printWriter.print(",");
            printWriter.print(packageSetting.realName != null ? packageSetting.realName : packageSetting.name);
            printWriter.print(",");
            printWriter.print(packageSetting.appId);
            printWriter.print(",");
            printWriter.print(packageSetting.versionCode);
            printWriter.print(",");
            printWriter.print(packageSetting.firstInstallTime);
            printWriter.print(",");
            printWriter.print(packageSetting.lastUpdateTime);
            printWriter.print(",");
            printWriter.print(packageSetting.installerPackageName != null ? packageSetting.installerPackageName : "?");
            pw.println();
            if (packageSetting.pkg != null) {
                printWriter.print(str2);
                printWriter.print("-");
                printWriter.print("splt,");
                printWriter.print("base,");
                printWriter.println(packageSetting.pkg.baseRevisionCode);
                if (packageSetting.pkg.splitNames != null) {
                    int i = 0;
                    while (true) {
                        int i2 = i;
                        if (i2 >= packageSetting.pkg.splitNames.length) {
                            break;
                        }
                        printWriter.print(str2);
                        printWriter.print("-");
                        printWriter.print("splt,");
                        printWriter.print(packageSetting.pkg.splitNames[i2]);
                        printWriter.print(",");
                        if (packageSetting.pkg.isPlugin) {
                            printWriter.print(packageSetting.pkg.splitVersionCodes[i2]);
                            printWriter.print(",");
                        }
                        printWriter.println(packageSetting.pkg.splitRevisionCodes[i2]);
                        i = i2 + 1;
                    }
                }
            }
            for (UserInfo user2 : users) {
                printWriter.print(str2);
                printWriter.print("-");
                printWriter.print("usr");
                printWriter.print(",");
                printWriter.print(user2.id);
                printWriter.print(",");
                printWriter.print(packageSetting.getInstalled(user2.id) ? "I" : "i");
                printWriter.print(packageSetting.getHidden(user2.id) ? "B" : "b");
                printWriter.print(packageSetting.getSuspended(user2.id) ? "SU" : "su");
                printWriter.print(packageSetting.getStopped(user2.id) ? "S" : "s");
                printWriter.print(packageSetting.getNotLaunched(user2.id) ? "l" : "L");
                printWriter.print(packageSetting.getInstantApp(user2.id) ? "IA" : "ia");
                printWriter.print(packageSetting.getVirtulalPreload(user2.id) ? "VPI" : "vpi");
                printWriter.print(packageSetting.getHarmfulAppWarning(user2.id) != null ? "HA" : "ha");
                printWriter.print(",");
                printWriter.print(packageSetting.getEnabled(user2.id));
                String lastDisabledAppCaller = packageSetting.getLastDisabledAppCaller(user2.id);
                printWriter.print(",");
                printWriter.print(lastDisabledAppCaller != null ? lastDisabledAppCaller : "?");
                printWriter.print(",");
                pw.println();
            }
            return;
        }
        pw.print(prefix);
        printWriter.print("Package [");
        printWriter.print(packageSetting.realName != null ? packageSetting.realName : packageSetting.name);
        printWriter.print("] (");
        printWriter.print(Integer.toHexString(System.identityHashCode(ps)));
        printWriter.println("):");
        if (packageSetting.realName != null) {
            pw.print(prefix);
            printWriter.print("  compat name=");
            printWriter.println(packageSetting.name);
        }
        pw.print(prefix);
        printWriter.print("  userId=");
        printWriter.println(packageSetting.appId);
        if (packageSetting.sharedUser != null) {
            pw.print(prefix);
            printWriter.print("  sharedUser=");
            printWriter.println(packageSetting.sharedUser);
        }
        pw.print(prefix);
        printWriter.print("  pkg=");
        printWriter.println(packageSetting.pkg);
        pw.print(prefix);
        printWriter.print("  codePath=");
        printWriter.println(packageSetting.codePathString);
        if (arraySet == null) {
            pw.print(prefix);
            printWriter.print("  resourcePath=");
            printWriter.println(packageSetting.resourcePathString);
            pw.print(prefix);
            printWriter.print("  legacyNativeLibraryDir=");
            printWriter.println(packageSetting.legacyNativeLibraryPathString);
            pw.print(prefix);
            printWriter.print("  primaryCpuAbi=");
            printWriter.println(packageSetting.primaryCpuAbiString);
            pw.print(prefix);
            printWriter.print("  secondaryCpuAbi=");
            printWriter.println(packageSetting.secondaryCpuAbiString);
        }
        pw.print(prefix);
        printWriter.print("  versionCode=");
        printWriter.print(packageSetting.versionCode);
        if (packageSetting.pkg != null) {
            printWriter.print(" minSdk=");
            printWriter.print(packageSetting.pkg.applicationInfo.minSdkVersion);
            printWriter.print(" targetSdk=");
            printWriter.print(packageSetting.pkg.applicationInfo.targetSdkVersion);
        }
        pw.println();
        if (packageSetting.pkg != null) {
            if (packageSetting.pkg.parentPackage != null) {
                PackageParser.Package parentPkg = packageSetting.pkg.parentPackage;
                PackageSetting pps = this.mPackages.get(parentPkg.packageName);
                if (pps == null || !pps.codePathString.equals(parentPkg.codePath)) {
                    pps = this.mDisabledSysPackages.get(parentPkg.packageName);
                }
                if (pps != null) {
                    pw.print(prefix);
                    printWriter.print("  parentPackage=");
                    printWriter.println(pps.realName != null ? pps.realName : pps.name);
                }
            } else if (packageSetting.pkg.childPackages != null) {
                pw.print(prefix);
                printWriter.print("  childPackages=[");
                int childCount = packageSetting.pkg.childPackages.size();
                for (int i3 = 0; i3 < childCount; i3++) {
                    PackageParser.Package childPkg = (PackageParser.Package) packageSetting.pkg.childPackages.get(i3);
                    PackageSetting cps = this.mPackages.get(childPkg.packageName);
                    if (cps == null || !cps.codePathString.equals(childPkg.codePath)) {
                        cps = this.mDisabledSysPackages.get(childPkg.packageName);
                    }
                    if (cps != null) {
                        if (i3 > 0) {
                            printWriter.print(", ");
                        }
                        printWriter.print(cps.realName != null ? cps.realName : cps.name);
                    }
                }
                printWriter.println("]");
            }
            pw.print(prefix);
            printWriter.print("  versionName=");
            printWriter.println(packageSetting.pkg.mVersionName);
            pw.print(prefix);
            printWriter.print("  splits=");
            dumpSplitNames(printWriter, packageSetting.pkg);
            pw.println();
            int apkSigningVersion = packageSetting.pkg.mSigningDetails.signatureSchemeVersion;
            pw.print(prefix);
            printWriter.print("  apkSigningVersion=");
            printWriter.println(apkSigningVersion);
            pw.print(prefix);
            printWriter.print("  applicationInfo=");
            printWriter.println(packageSetting.pkg.applicationInfo.toString());
            pw.print(prefix);
            printWriter.print("  flags=");
            printFlags(printWriter, packageSetting.pkg.applicationInfo.flags, FLAG_DUMP_SPEC);
            pw.println();
            pw.print(prefix);
            printWriter.print("  hwflags=");
            printFlags(printWriter, packageSetting.pkg.applicationInfo.hwFlags, HW_FLAG_DUMP_SPEC);
            pw.println();
            if (packageSetting.pkg.applicationInfo.privateFlags != 0) {
                pw.print(prefix);
                printWriter.print("  privateFlags=");
                printFlags(printWriter, packageSetting.pkg.applicationInfo.privateFlags, PRIVATE_FLAG_DUMP_SPEC);
                pw.println();
            }
            pw.print(prefix);
            printWriter.print("  dataDir=");
            printWriter.println(packageSetting.pkg.applicationInfo.dataDir);
            pw.print(prefix);
            printWriter.print("  supportsScreens=[");
            boolean first = true;
            if ((packageSetting.pkg.applicationInfo.flags & 512) != 0) {
                if (1 == 0) {
                    printWriter.print(", ");
                }
                first = false;
                printWriter.print("small");
            }
            if ((packageSetting.pkg.applicationInfo.flags & 1024) != 0) {
                if (!first) {
                    printWriter.print(", ");
                }
                first = false;
                printWriter.print("medium");
            }
            if ((packageSetting.pkg.applicationInfo.flags & 2048) != 0) {
                if (!first) {
                    printWriter.print(", ");
                }
                first = false;
                printWriter.print("large");
            }
            if ((packageSetting.pkg.applicationInfo.flags & DumpState.DUMP_FROZEN) != 0) {
                if (!first) {
                    printWriter.print(", ");
                }
                first = false;
                printWriter.print("xlarge");
            }
            if ((packageSetting.pkg.applicationInfo.flags & 4096) != 0) {
                if (!first) {
                    printWriter.print(", ");
                }
                first = false;
                printWriter.print("resizeable");
            }
            if ((packageSetting.pkg.applicationInfo.flags & 8192) != 0) {
                if (!first) {
                    printWriter.print(", ");
                }
                printWriter.print("anyDensity");
            }
            printWriter.println("]");
            if (packageSetting.pkg.libraryNames != null && packageSetting.pkg.libraryNames.size() > 0) {
                pw.print(prefix);
                printWriter.println("  dynamic libraries:");
                for (int i4 = 0; i4 < packageSetting.pkg.libraryNames.size(); i4++) {
                    pw.print(prefix);
                    printWriter.print("    ");
                    printWriter.println((String) packageSetting.pkg.libraryNames.get(i4));
                }
            }
            if (packageSetting.pkg.staticSharedLibName != null) {
                pw.print(prefix);
                printWriter.println("  static library:");
                pw.print(prefix);
                printWriter.print("    ");
                printWriter.print("name:");
                printWriter.print(packageSetting.pkg.staticSharedLibName);
                printWriter.print(" version:");
                printWriter.println(packageSetting.pkg.staticSharedLibVersion);
            }
            if (packageSetting.pkg.usesLibraries != null && packageSetting.pkg.usesLibraries.size() > 0) {
                pw.print(prefix);
                printWriter.println("  usesLibraries:");
                for (int i5 = 0; i5 < packageSetting.pkg.usesLibraries.size(); i5++) {
                    pw.print(prefix);
                    printWriter.print("    ");
                    printWriter.println((String) packageSetting.pkg.usesLibraries.get(i5));
                }
            }
            if (packageSetting.pkg.usesStaticLibraries != null && packageSetting.pkg.usesStaticLibraries.size() > 0) {
                pw.print(prefix);
                printWriter.println("  usesStaticLibraries:");
                for (int i6 = 0; i6 < packageSetting.pkg.usesStaticLibraries.size(); i6++) {
                    pw.print(prefix);
                    printWriter.print("    ");
                    printWriter.print((String) packageSetting.pkg.usesStaticLibraries.get(i6));
                    printWriter.print(" version:");
                    printWriter.println(packageSetting.pkg.usesStaticLibrariesVersions[i6]);
                }
            }
            if (packageSetting.pkg.usesOptionalLibraries != null && packageSetting.pkg.usesOptionalLibraries.size() > 0) {
                pw.print(prefix);
                printWriter.println("  usesOptionalLibraries:");
                for (int i7 = 0; i7 < packageSetting.pkg.usesOptionalLibraries.size(); i7++) {
                    pw.print(prefix);
                    printWriter.print("    ");
                    printWriter.println((String) packageSetting.pkg.usesOptionalLibraries.get(i7));
                }
            }
            if (packageSetting.pkg.usesLibraryFiles != null && packageSetting.pkg.usesLibraryFiles.length > 0) {
                pw.print(prefix);
                printWriter.println("  usesLibraryFiles:");
                for (String println : packageSetting.pkg.usesLibraryFiles) {
                    pw.print(prefix);
                    printWriter.print("    ");
                    printWriter.println(println);
                }
            }
        }
        pw.print(prefix);
        printWriter.print("  timeStamp=");
        date2.setTime(packageSetting.timeStamp);
        printWriter.println(sdf.format(date));
        pw.print(prefix);
        printWriter.print("  firstInstallTime=");
        date2.setTime(packageSetting.firstInstallTime);
        printWriter.println(sdf.format(date));
        pw.print(prefix);
        printWriter.print("  lastUpdateTime=");
        date2.setTime(packageSetting.lastUpdateTime);
        printWriter.println(sdf.format(date));
        if (packageSetting.installerPackageName != null) {
            pw.print(prefix);
            printWriter.print("  installerPackageName=");
            printWriter.println(packageSetting.installerPackageName);
        }
        if (packageSetting.volumeUuid != null) {
            pw.print(prefix);
            printWriter.print("  volumeUuid=");
            printWriter.println(packageSetting.volumeUuid);
        }
        pw.print(prefix);
        printWriter.print("  signatures=");
        printWriter.println(packageSetting.signatures);
        pw.print(prefix);
        printWriter.print("  installPermissionsFixed=");
        printWriter.print(packageSetting.installPermissionsFixed);
        pw.println();
        pw.print(prefix);
        printWriter.print("  pkgFlags=");
        printFlags(printWriter, packageSetting.pkgFlags, FLAG_DUMP_SPEC);
        pw.println();
        if (!(packageSetting.pkg == null || packageSetting.pkg.mOverlayTarget == null)) {
            pw.print(prefix);
            printWriter.print("  overlayTarget=");
            printWriter.println(packageSetting.pkg.mOverlayTarget);
            pw.print(prefix);
            printWriter.print("  overlayCategory=");
            printWriter.println(packageSetting.pkg.mOverlayCategory);
        }
        if (!(packageSetting.pkg == null || packageSetting.pkg.permissions == null || packageSetting.pkg.permissions.size() <= 0)) {
            ArrayList<PackageParser.Permission> perms = packageSetting.pkg.permissions;
            pw.print(prefix);
            printWriter.println("  declared permissions:");
            for (int i8 = 0; i8 < perms.size(); i8++) {
                PackageParser.Permission perm = perms.get(i8);
                if (arraySet == null || arraySet.contains(perm.info.name)) {
                    pw.print(prefix);
                    printWriter.print("    ");
                    printWriter.print(perm.info.name);
                    printWriter.print(": prot=");
                    printWriter.print(PermissionInfo.protectionToString(perm.info.protectionLevel));
                    if ((perm.info.flags & 1) != 0) {
                        printWriter.print(", COSTS_MONEY");
                    }
                    if ((perm.info.flags & 2) != 0) {
                        printWriter.print(", HIDDEN");
                    }
                    if ((perm.info.flags & 1073741824) != 0) {
                        printWriter.print(", INSTALLED");
                    }
                    pw.println();
                }
            }
        }
        if ((arraySet != null || dumpAll) && packageSetting.pkg != null && packageSetting.pkg.requestedPermissions != null && packageSetting.pkg.requestedPermissions.size() > 0) {
            ArrayList<String> perms2 = packageSetting.pkg.requestedPermissions;
            pw.print(prefix);
            printWriter.println("  requested permissions:");
            for (int i9 = 0; i9 < perms2.size(); i9++) {
                String perm2 = perms2.get(i9);
                if (arraySet == null || arraySet.contains(perm2)) {
                    pw.print(prefix);
                    printWriter.print("    ");
                    printWriter.println(perm2);
                }
            }
        }
        if (packageSetting.sharedUser == null || arraySet != null || dumpAll) {
            dumpInstallPermissionsLPr(printWriter, str + "  ", arraySet, ps.getPermissionsState());
        }
        for (UserInfo user3 : users) {
            pw.print(prefix);
            printWriter.print("  User ");
            printWriter.print(user3.id);
            printWriter.print(": ");
            printWriter.print("ceDataInode=");
            printWriter.print(packageSetting.getCeDataInode(user3.id));
            printWriter.print(" installed=");
            printWriter.print(packageSetting.getInstalled(user3.id));
            printWriter.print(" hidden=");
            printWriter.print(packageSetting.getHidden(user3.id));
            printWriter.print(" suspended=");
            printWriter.print(packageSetting.getSuspended(user3.id));
            if (packageSetting.getSuspended(user3.id)) {
                PackageUserState pus = packageSetting.readUserState(user3.id);
                printWriter.print(" suspendingPackage=");
                printWriter.print(pus.suspendingPackage);
                printWriter.print(" dialogMessage=");
                printWriter.print(pus.dialogMessage);
            }
            printWriter.print(" stopped=");
            printWriter.print(packageSetting.getStopped(user3.id));
            printWriter.print(" notLaunched=");
            printWriter.print(packageSetting.getNotLaunched(user3.id));
            printWriter.print(" enabled=");
            printWriter.print(packageSetting.getEnabled(user3.id));
            printWriter.print(" instant=");
            printWriter.print(packageSetting.getInstantApp(user3.id));
            printWriter.print(" virtual=");
            printWriter.println(packageSetting.getVirtulalPreload(user3.id));
            String[] overlayPaths = packageSetting.getOverlayPaths(user3.id);
            if (overlayPaths != null && overlayPaths.length > 0) {
                pw.print(prefix);
                printWriter.println("  overlay paths:");
                for (String path : overlayPaths) {
                    pw.print(prefix);
                    printWriter.print("    ");
                    printWriter.println(path);
                }
            }
            String lastDisabledAppCaller2 = packageSetting.getLastDisabledAppCaller(user3.id);
            if (lastDisabledAppCaller2 != null) {
                pw.print(prefix);
                printWriter.print("    lastDisabledCaller: ");
                printWriter.println(lastDisabledAppCaller2);
            }
            if (packageSetting.sharedUser == null) {
                PermissionsState permissionsState = ps.getPermissionsState();
                dumpGidsLPr(printWriter, str + "    ", permissionsState.computeGids(user3.id));
                PermissionsState permissionsState2 = permissionsState;
                String str3 = lastDisabledAppCaller2;
                String[] strArr = overlayPaths;
                user = user3;
                dumpRuntimePermissionsLPr(printWriter, str + "    ", arraySet, permissionsState.getRuntimePermissionStates(user3.id), dumpAll);
            } else {
                String[] strArr2 = overlayPaths;
                user = user3;
            }
            String harmfulAppWarning = packageSetting.getHarmfulAppWarning(user.id);
            if (harmfulAppWarning != null) {
                pw.print(prefix);
                printWriter.print("      harmfulAppWarning: ");
                printWriter.println(harmfulAppWarning);
            }
            if (arraySet == null) {
                ArraySet<String> cmp = packageSetting.getDisabledComponents(user.id);
                if (cmp != null && cmp.size() > 0) {
                    pw.print(prefix);
                    printWriter.println("    disabledComponents:");
                    Iterator<String> it = cmp.iterator();
                    while (it.hasNext()) {
                        pw.print(prefix);
                        printWriter.print("      ");
                        printWriter.println(it.next());
                    }
                }
                ArraySet<String> cmp2 = packageSetting.getEnabledComponents(user.id);
                if (cmp2 != null && cmp2.size() > 0) {
                    pw.print(prefix);
                    printWriter.println("    enabledComponents:");
                    Iterator<String> it2 = cmp2.iterator();
                    while (it2.hasNext()) {
                        pw.print(prefix);
                        printWriter.print("      ");
                        printWriter.println(it2.next());
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void dumpPackagesLPr(PrintWriter pw, String packageName, ArraySet<String> permissionNames, DumpState dumpState, boolean checkin) {
        Settings settings = this;
        PrintWriter printWriter = pw;
        String str = packageName;
        ArraySet<String> arraySet = permissionNames;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        boolean printedSomething = false;
        List<UserInfo> users = getAllUsers(UserManagerService.getInstance());
        Iterator<PackageSetting> it = settings.mPackages.values().iterator();
        while (true) {
            String str2 = null;
            if (!it.hasNext()) {
                break;
            }
            PackageSetting ps = it.next();
            if ((str == null || str.equals(ps.realName) || str.equals(ps.name)) && (arraySet == null || ps.getPermissionsState().hasRequestedPermission(arraySet))) {
                if (checkin || str == null) {
                    DumpState dumpState2 = dumpState;
                } else {
                    dumpState.setSharedUser(ps.sharedUser);
                }
                if (!checkin && !printedSomething) {
                    if (dumpState.onTitlePrinted()) {
                        pw.println();
                    }
                    printWriter.println("Packages:");
                    printedSomething = true;
                }
                boolean printedSomething2 = printedSomething;
                if (checkin) {
                    str2 = "pkg";
                }
                settings.dumpPackageLPr(printWriter, "  ", str2, arraySet, ps, sdf, date, users, str != null);
                printedSomething = printedSomething2;
            }
        }
        boolean printedSomething3 = false;
        if (settings.mRenamedPackages.size() > 0 && arraySet == null) {
            for (Map.Entry<String, String> e : settings.mRenamedPackages.entrySet()) {
                if (str == null || str.equals(e.getKey()) || str.equals(e.getValue())) {
                    if (!checkin) {
                        if (!printedSomething3) {
                            if (dumpState.onTitlePrinted()) {
                                pw.println();
                            }
                            printWriter.println("Renamed packages:");
                            printedSomething3 = true;
                        }
                        printWriter.print("  ");
                    } else {
                        printWriter.print("ren,");
                    }
                    printWriter.print(e.getKey());
                    printWriter.print(checkin ? " -> " : ",");
                    printWriter.println(e.getValue());
                }
            }
        }
        boolean printedSomething4 = false;
        if (settings.mDisabledSysPackages.size() > 0 && arraySet == null) {
            for (PackageSetting ps2 : settings.mDisabledSysPackages.values()) {
                if (str == null || str.equals(ps2.realName) || str.equals(ps2.name)) {
                    if (!checkin && !printedSomething4) {
                        if (dumpState.onTitlePrinted()) {
                            pw.println();
                        }
                        printWriter.println("Hidden system packages:");
                        printedSomething4 = true;
                    }
                    settings.dumpPackageLPr(printWriter, "  ", checkin ? "dis" : null, permissionNames, ps2, sdf, date, users, str != null);
                    settings = this;
                    printWriter = pw;
                    str = packageName;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void dumpPackagesProto(ProtoOutputStream proto) {
        List<UserInfo> users = getAllUsers(UserManagerService.getInstance());
        int count = this.mPackages.size();
        for (int i = 0; i < count; i++) {
            this.mPackages.valueAt(i).writeToProto(proto, 2246267895813L, users);
        }
    }

    /* access modifiers changed from: package-private */
    public void dumpPermissionsLPr(PrintWriter pw, String packageName, ArraySet<String> permissionNames, DumpState dumpState) {
        this.mPermissions.dumpPermissions(pw, packageName, permissionNames, this.mReadExternalStorageEnforced == Boolean.TRUE, dumpState);
    }

    /* access modifiers changed from: package-private */
    public void dumpSharedUsersLPr(PrintWriter pw, String packageName, ArraySet<String> permissionNames, DumpState dumpState, boolean checkin) {
        int[] iArr;
        int i;
        int i2;
        PrintWriter printWriter = pw;
        ArraySet<String> arraySet = permissionNames;
        boolean printedSomething = false;
        for (SharedUserSetting su : this.mSharedUsers.values()) {
            if ((packageName == null || su == dumpState.getSharedUser()) && (arraySet == null || su.getPermissionsState().hasRequestedPermission(arraySet))) {
                if (!checkin) {
                    if (!printedSomething) {
                        if (dumpState.onTitlePrinted()) {
                            pw.println();
                        }
                        printWriter.println("Shared users:");
                        printedSomething = true;
                    }
                    boolean printedSomething2 = printedSomething;
                    printWriter.print("  SharedUser [");
                    printWriter.print(su.name);
                    printWriter.print("] (");
                    printWriter.print(Integer.toHexString(System.identityHashCode(su)));
                    printWriter.println("):");
                    printWriter.print("    ");
                    printWriter.print("userId=");
                    printWriter.println(su.userId);
                    PermissionsState permissionsState = su.getPermissionsState();
                    dumpInstallPermissionsLPr(printWriter, "    ", arraySet, permissionsState);
                    int[] userIds = UserManagerService.getInstance().getUserIds();
                    int length = userIds.length;
                    int i3 = 0;
                    while (i3 < length) {
                        int userId = userIds[i3];
                        int[] gids = permissionsState.computeGids(userId);
                        List<PermissionsState.PermissionState> permissions = permissionsState.getRuntimePermissionStates(userId);
                        if (!ArrayUtils.isEmpty(gids) || !permissions.isEmpty()) {
                            printWriter.print("    ");
                            List<PermissionsState.PermissionState> permissions2 = permissions;
                            printWriter.print("User ");
                            printWriter.print(userId);
                            printWriter.println(": ");
                            StringBuilder sb = new StringBuilder();
                            sb.append("    ");
                            int i4 = userId;
                            sb.append("  ");
                            dumpGidsLPr(printWriter, sb.toString(), gids);
                            int[] iArr2 = gids;
                            i2 = i3;
                            i = length;
                            iArr = userIds;
                            dumpRuntimePermissionsLPr(printWriter, "    " + "  ", arraySet, permissions2, packageName != null);
                        } else {
                            i2 = i3;
                            i = length;
                            iArr = userIds;
                        }
                        i3 = i2 + 1;
                        length = i;
                        userIds = iArr;
                    }
                    printedSomething = printedSomething2;
                } else {
                    printWriter.print("suid,");
                    printWriter.print(su.userId);
                    printWriter.print(",");
                    printWriter.println(su.name);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void dumpSharedUsersProto(ProtoOutputStream proto) {
        int count = this.mSharedUsers.size();
        for (int i = 0; i < count; i++) {
            this.mSharedUsers.valueAt(i).writeToProto(proto, 2246267895814L);
        }
    }

    /* access modifiers changed from: package-private */
    public void dumpReadMessagesLPr(PrintWriter pw, DumpState dumpState) {
        pw.println("Settings parse messages:");
        pw.print(this.mReadMessages.toString());
    }

    /* access modifiers changed from: package-private */
    public void dumpRestoredPermissionGrantsLPr(PrintWriter pw, DumpState dumpState) {
        if (this.mRestoredUserGrants.size() > 0) {
            pw.println();
            pw.println("Restored (pending) permission grants:");
            for (int userIndex = 0; userIndex < this.mRestoredUserGrants.size(); userIndex++) {
                ArrayMap<String, ArraySet<RestoredPermissionGrant>> grantsByPackage = this.mRestoredUserGrants.valueAt(userIndex);
                if (grantsByPackage != null && grantsByPackage.size() > 0) {
                    int userId = this.mRestoredUserGrants.keyAt(userIndex);
                    pw.print("  User ");
                    pw.println(userId);
                    for (int pkgIndex = 0; pkgIndex < grantsByPackage.size(); pkgIndex++) {
                        ArraySet<RestoredPermissionGrant> grants = grantsByPackage.valueAt(pkgIndex);
                        if (grants != null && grants.size() > 0) {
                            pw.print("    ");
                            pw.print(grantsByPackage.keyAt(pkgIndex));
                            pw.println(" :");
                            Iterator<RestoredPermissionGrant> it = grants.iterator();
                            while (it.hasNext()) {
                                RestoredPermissionGrant g = it.next();
                                pw.print("      ");
                                pw.print(g.permissionName);
                                if (g.granted) {
                                    pw.print(" GRANTED");
                                }
                                if ((g.grantBits & 1) != 0) {
                                    pw.print(" user_set");
                                }
                                if ((g.grantBits & 2) != 0) {
                                    pw.print(" user_fixed");
                                }
                                if ((g.grantBits & 8) != 0) {
                                    pw.print(" revoke_on_upgrade");
                                }
                                pw.println();
                            }
                        }
                    }
                }
            }
            pw.println();
        }
    }

    private static void dumpSplitNames(PrintWriter pw, PackageParser.Package pkg) {
        if (pkg == null) {
            pw.print(UiModeManagerService.Shell.NIGHT_MODE_STR_UNKNOWN);
            return;
        }
        pw.print("[");
        pw.print("base");
        if (pkg.baseRevisionCode != 0) {
            pw.print(":");
            pw.print(pkg.baseRevisionCode);
        }
        if (pkg.splitNames != null) {
            for (int i = 0; i < pkg.splitNames.length; i++) {
                pw.print(", ");
                pw.print(pkg.splitNames[i]);
                if (pkg.isPlugin) {
                    pw.print(":");
                    pw.print(pkg.splitVersionCodes[i]);
                }
                if (pkg.splitRevisionCodes[i] != 0) {
                    pw.print(":");
                    pw.print(pkg.splitRevisionCodes[i]);
                }
            }
        }
        pw.print("]");
    }

    /* access modifiers changed from: package-private */
    public void dumpGidsLPr(PrintWriter pw, String prefix, int[] gids) {
        if (!ArrayUtils.isEmpty(gids)) {
            pw.print(prefix);
            pw.print("gids=");
            pw.println(PackageManagerService.arrayToString(gids));
        }
    }

    /* access modifiers changed from: package-private */
    public void dumpRuntimePermissionsLPr(PrintWriter pw, String prefix, ArraySet<String> permissionNames, List<PermissionsState.PermissionState> permissionStates, boolean dumpAll) {
        if (!permissionStates.isEmpty() || dumpAll) {
            pw.print(prefix);
            pw.println("runtime permissions:");
            for (PermissionsState.PermissionState permissionState : permissionStates) {
                if (permissionNames == null || permissionNames.contains(permissionState.getName())) {
                    pw.print(prefix);
                    pw.print("  ");
                    pw.print(permissionState.getName());
                    pw.print(": granted=");
                    pw.print(permissionState.isGranted());
                    pw.println(permissionFlagsToString(", flags=", permissionState.getFlags()));
                }
            }
        }
    }

    private static String permissionFlagsToString(String prefix, int flags) {
        StringBuilder flagsString = null;
        while (flags != 0) {
            if (flagsString == null) {
                flagsString = new StringBuilder();
                flagsString.append(prefix);
                flagsString.append("[ ");
            }
            int flag = 1 << Integer.numberOfTrailingZeros(flags);
            flags &= ~flag;
            flagsString.append(PackageManager.permissionFlagToString(flag));
            flagsString.append(' ');
        }
        if (flagsString == null) {
            return BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
        }
        flagsString.append(']');
        return flagsString.toString();
    }

    /* access modifiers changed from: package-private */
    public void dumpInstallPermissionsLPr(PrintWriter pw, String prefix, ArraySet<String> permissionNames, PermissionsState permissionsState) {
        List<PermissionsState.PermissionState> permissionStates = permissionsState.getInstallPermissionStates();
        if (!permissionStates.isEmpty()) {
            pw.print(prefix);
            pw.println("install permissions:");
            for (PermissionsState.PermissionState permissionState : permissionStates) {
                if (permissionNames == null || permissionNames.contains(permissionState.getName())) {
                    pw.print(prefix);
                    pw.print("  ");
                    pw.print(permissionState.getName());
                    pw.print(": granted=");
                    pw.print(permissionState.isGranted());
                    pw.println(permissionFlagsToString(", flags=", permissionState.getFlags()));
                }
            }
        }
    }

    public void writeRuntimePermissionsForUserLPr(int userId, boolean sync) {
        if (sync) {
            this.mRuntimePermissionsPersistence.writePermissionsForUserSyncLPr(userId);
        } else {
            this.mRuntimePermissionsPersistence.writePermissionsForUserAsyncLPr(userId);
        }
    }

    private boolean isInDelAppList(String packageName) {
        if (mIsCheckDelAppsFinished.compareAndSet(false, true)) {
            readDelAppsFiles();
        }
        return this.mDelAppLists.contains(packageName);
    }

    private void readDelAppsFiles() {
        File file;
        ArrayList<File> delAppsFileList = new ArrayList<>();
        try {
            delAppsFileList = HwCfgFilePolicy.getCfgFileList("xml/hw_subuser_delapps_config.xml", 0);
            file = new File(getCustomizedFileName(FILE_SUB_USER_DELAPPS_LIST));
        } catch (NoClassDefFoundError e) {
            Log.d(TAG, "HwCfgFilePolicy NoClassDefFoundError");
            file = new File(getCustomizedFileName(FILE_SUB_USER_DELAPPS_LIST));
        } catch (Throwable th) {
            delAppsFileList.add(new File(getCustomizedFileName(FILE_SUB_USER_DELAPPS_LIST)));
            throw th;
        }
        delAppsFileList.add(file);
        Iterator<File> it = delAppsFileList.iterator();
        while (it.hasNext()) {
            loadDelAppsFromXml(it.next());
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:19:?, code lost:
        r1.close();
     */
    private void loadDelAppsFromXml(File configFile) {
        String str;
        StringBuilder sb;
        if (configFile.exists()) {
            FileInputStream stream = null;
            try {
                FileInputStream stream2 = new FileInputStream(configFile);
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(stream2, null);
                int depth = parser.getDepth();
                while (true) {
                    int next = parser.next();
                    int type = next;
                    if ((next == 3 && parser.getDepth() <= depth) || type == 1) {
                        try {
                            break;
                        } catch (IOException e) {
                            e = e;
                            str = TAG;
                            sb = new StringBuilder();
                        }
                    } else if (type == 2) {
                        if (parser.getName().equals("del_app")) {
                            this.mDelAppLists.add(parser.getAttributeValue(0));
                        }
                    }
                }
            } catch (FileNotFoundException e2) {
                Slog.e(TAG, "file is not exist " + e2);
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e3) {
                        e = e3;
                        str = TAG;
                        sb = new StringBuilder();
                    }
                }
            } catch (XmlPullParserException e4) {
                Slog.e(TAG, "failed parsing " + configFile + " " + e4);
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e5) {
                        e = e5;
                        str = TAG;
                        sb = new StringBuilder();
                    }
                }
            } catch (IOException e6) {
                Slog.e(TAG, "failed parsing " + configFile + " " + e6);
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e7) {
                        e = e7;
                        str = TAG;
                        sb = new StringBuilder();
                    }
                }
            } catch (Throwable th) {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e8) {
                        Slog.e(TAG, "failed close stream " + e8);
                    }
                }
                throw th;
            }
        }
        return;
        sb.append("failed close stream ");
        sb.append(e);
        Slog.e(str, sb.toString());
    }

    private String getCustomizedFileName(String xmlName) {
        String path = "/data/cust/xml/" + xmlName;
        if (new File(path).exists()) {
            return path;
        }
        return DIR_ETC_XML + xmlName;
    }
}
