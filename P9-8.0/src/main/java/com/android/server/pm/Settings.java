package com.android.server.pm;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.AuthorityEntry;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.IntentFilterVerificationInfo;
import android.content.pm.PackageCleanItem;
import android.content.pm.PackageManager;
import android.content.pm.PackageParser;
import android.content.pm.PackageParser.ActivityIntentInfo;
import android.content.pm.PackageParser.Package;
import android.content.pm.PackageParser.Permission;
import android.content.pm.PackageUserState;
import android.content.pm.PermissionInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.content.pm.UserInfo;
import android.content.pm.VerifierDeviceIdentity;
import android.net.Uri.Builder;
import android.os.Binder;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Environment;
import android.os.FileUtils;
import android.os.Handler;
import android.os.Message;
import android.os.PatternMatcher;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.storage.StorageManager;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.AtomicFile;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.util.SparseLongArray;
import android.util.Xml;
import android.util.proto.ProtoOutputStream;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.os.BackgroundThread;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.JournaledFile;
import com.android.internal.util.XmlUtils;
import com.android.server.HwServiceFactory;
import com.android.server.NetworkManagementService;
import com.android.server.am.HwBroadcastRadarUtil;
import com.android.server.connectivity.NetworkAgentInfo;
import com.android.server.os.HwBootFail;
import com.android.server.pm.Installer.InstallerException;
import com.android.server.pm.PermissionsState.PermissionState;
import com.android.server.power.IHwShutdownThread;
import com.android.server.voiceinteraction.DatabaseHelper.SoundModelContract;
import huawei.cust.HwCfgFilePolicy;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

final class Settings {
    private static final String ATTR_APP_LINK_GENERATION = "app-link-generation";
    private static final String ATTR_BLOCKED = "blocked";
    @Deprecated
    private static final String ATTR_BLOCK_UNINSTALL = "blockUninstall";
    private static final String ATTR_CE_DATA_INODE = "ceDataInode";
    private static final String ATTR_CODE = "code";
    private static final String ATTR_DATABASE_VERSION = "databaseVersion";
    private static final String ATTR_DOMAIN_VERIFICATON_STATE = "domainVerificationStatus";
    private static final String ATTR_DONE = "done";
    private static final String ATTR_ENABLED = "enabled";
    private static final String ATTR_ENABLED_CALLER = "enabledCaller";
    private static final String ATTR_ENFORCEMENT = "enforcement";
    private static final String ATTR_FINGERPRINT = "fingerprint";
    private static final String ATTR_FLAGS = "flags";
    private static final String ATTR_GRANTED = "granted";
    private static final String ATTR_HIDDEN = "hidden";
    private static final String ATTR_INSTALLED = "inst";
    private static final String ATTR_INSTALL_REASON = "install-reason";
    private static final String ATTR_INSTANT_APP = "instant-app";
    private static final String ATTR_NAME = "name";
    private static final String ATTR_NOT_LAUNCHED = "nl";
    private static final String ATTR_PACKAGE_NAME = "packageName";
    private static final String ATTR_REVOKE_ON_UPGRADE = "rou";
    private static final String ATTR_SDK_VERSION = "sdkVersion";
    private static final String ATTR_STOPPED = "stopped";
    private static final String ATTR_SUSPENDED = "suspended";
    private static final String ATTR_USER = "user";
    private static final String ATTR_USER_FIXED = "fixed";
    private static final String ATTR_USER_SET = "set";
    private static final String ATTR_VERSION = "version";
    private static final String ATTR_VOLUME_UUID = "volumeUuid";
    public static final int CURRENT_DATABASE_VERSION = 3;
    private static final boolean DEBUG_KERNEL = false;
    private static final boolean DEBUG_MU = false;
    private static final boolean DEBUG_PARSER = false;
    private static final boolean DEBUG_STOPPED = false;
    private static final String DIR_CUST_XML = "/data/cust/xml/";
    private static final String DIR_ETC_XML = "/system/etc/xml/";
    private static final String FILE_SUB_USER_DELAPPS_LIST = "hw_subuser_delapps_config.xml";
    static final Object[] FLAG_DUMP_SPEC = new Object[]{Integer.valueOf(1), NetworkManagementService.PERMISSION_SYSTEM, Integer.valueOf(2), "DEBUGGABLE", Integer.valueOf(4), "HAS_CODE", Integer.valueOf(8), "PERSISTENT", Integer.valueOf(16), "FACTORY_TEST", Integer.valueOf(32), "ALLOW_TASK_REPARENTING", Integer.valueOf(64), "ALLOW_CLEAR_USER_DATA", Integer.valueOf(128), "UPDATED_SYSTEM_APP", Integer.valueOf(256), "TEST_ONLY", Integer.valueOf(16384), "VM_SAFE_MODE", Integer.valueOf(32768), "ALLOW_BACKUP", Integer.valueOf(65536), "KILL_AFTER_RESTORE", Integer.valueOf(DumpState.DUMP_INTENT_FILTER_VERIFIERS), "RESTORE_ANY_VERSION", Integer.valueOf(DumpState.DUMP_DOMAIN_PREFERRED), "EXTERNAL_STORAGE", Integer.valueOf(DumpState.DUMP_DEXOPT), "LARGE_HEAP"};
    static final Object[] HW_FLAG_DUMP_SPEC = new Object[]{Integer.valueOf(33554432), "PARSE_IS_REMOVABLE_PREINSTALLED_APK", Integer.valueOf(67108864), "FLAG_UPDATED_REMOVEABLE_APP"};
    private static int PRE_M_APP_INFO_FLAG_CANT_SAVE_STATE = 268435456;
    private static int PRE_M_APP_INFO_FLAG_FORWARD_LOCK = 536870912;
    private static int PRE_M_APP_INFO_FLAG_HIDDEN = 134217728;
    private static int PRE_M_APP_INFO_FLAG_PRIVILEGED = 1073741824;
    private static final Object[] PRIVATE_FLAG_DUMP_SPEC = new Object[]{Integer.valueOf(1), "HIDDEN", Integer.valueOf(2), "CANT_SAVE_STATE", Integer.valueOf(4), "FORWARD_LOCK", Integer.valueOf(8), "PRIVILEGED", Integer.valueOf(16), "HAS_DOMAIN_URLS", Integer.valueOf(32), "DEFAULT_TO_DEVICE_PROTECTED_STORAGE", Integer.valueOf(64), "DIRECT_BOOT_AWARE", Integer.valueOf(256), "PARTIALLY_DIRECT_BOOT_AWARE", Integer.valueOf(128), "EPHEMERAL", Integer.valueOf(512), "REQUIRED_FOR_SYSTEM_USER", Integer.valueOf(1024), "PRIVATE_FLAG_ACTIVITIES_RESIZE_MODE_RESIZEABLE", Integer.valueOf(2048), "PRIVATE_FLAG_ACTIVITIES_RESIZE_MODE_UNRESIZEABLE", Integer.valueOf(4096), "PRIVATE_FLAG_ACTIVITIES_RESIZE_MODE_RESIZEABLE_VIA_SDK_VERSION", Integer.valueOf(8192), "BACKUP_IN_FOREGROUND", Integer.valueOf(16384), "STATIC_SHARED_LIBRARY"};
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
    private static final String TAG_ITEM = "item";
    private static final String TAG_PACKAGE = "pkg";
    private static final String TAG_PACKAGE_RESTRICTIONS = "package-restrictions";
    private static final String TAG_PERMISSIONS = "perms";
    private static final String TAG_PERMISSION_ENTRY = "perm";
    private static final String TAG_PERSISTENT_PREFERRED_ACTIVITIES = "persistent-preferred-activities";
    private static final String TAG_READ_EXTERNAL_STORAGE = "read-external-storage";
    private static final String TAG_RESTORED_RUNTIME_PERMISSIONS = "restored-perms";
    private static final String TAG_RUNTIME_PERMISSIONS = "runtime-permissions";
    private static final String TAG_SHARED_USER = "shared-user";
    private static final String TAG_USES_STATIC_LIB = "uses-static-lib";
    private static final String TAG_VERSION = "version";
    private static final int USER_RUNTIME_GRANT_MASK = 11;
    private static int mFirstAvailableUid = 0;
    private static AtomicBoolean mIsCheckDelAppsFinished = new AtomicBoolean(false);
    private final File mBackupSettingsFilename;
    private final File mBackupStoppedPackagesFilename;
    private final SparseArray<ArraySet<String>> mBlockUninstallPackages;
    final SparseArray<CrossProfileIntentResolver> mCrossProfileIntentResolvers;
    final SparseArray<String> mDefaultBrowserApp;
    final SparseArray<String> mDefaultDialerApp;
    private ArrayList<String> mDelAppLists;
    private final ArrayMap<String, PackageSetting> mDisabledSysPackages;
    final ArraySet<String> mInstallerPackages;
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
    final ArrayMap<String, BasePermission> mPermissionTrees;
    final ArrayMap<String, BasePermission> mPermissions;
    final SparseArray<PersistentPreferredIntentResolver> mPersistentPreferredActivities;
    final SparseArray<PreferredIntentResolver> mPreferredActivities;
    Boolean mReadExternalStorageEnforced;
    final StringBuilder mReadMessages;
    private final ArrayMap<String, String> mRenamedPackages;
    private final ArrayMap<String, IntentFilterVerificationInfo> mRestoredIntentFilterVerifications;
    private final SparseArray<ArrayMap<String, ArraySet<RestoredPermissionGrant>>> mRestoredUserGrants;
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

        /* synthetic */ KernelPackageState(KernelPackageState -this0) {
            this();
        }

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
        private final SparseArray<String> mFingerprints = new SparseArray();
        private final Handler mHandler = new MyHandler();
        @GuardedBy("mLock")
        private final SparseLongArray mLastNotWrittenMutationTimesMillis = new SparseLongArray();
        private final Object mLock;
        @GuardedBy("mLock")
        private final SparseBooleanArray mWriteScheduled = new SparseBooleanArray();

        private final class MyHandler extends Handler {
            public MyHandler() {
                super(BackgroundThread.getHandler().getLooper());
            }

            public void handleMessage(Message message) {
                Runnable callback = message.obj;
                RuntimePermissionPersistence.this.writePermissionsSync(message.what);
                if (callback != null) {
                    callback.run();
                }
            }
        }

        public RuntimePermissionPersistence(Object lock) {
            this.mLock = lock;
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

        private void writePermissionsSync(int userId) {
            int packageCount;
            int i;
            String packageName;
            int sharedUserCount;
            AtomicFile destination = new AtomicFile(Settings.this.getUserRuntimePermissionsFile(userId));
            ArrayMap<String, List<PermissionState>> permissionsForPackage = new ArrayMap();
            ArrayMap<String, List<PermissionState>> permissionsForSharedUser = new ArrayMap();
            synchronized (this.mLock) {
                try {
                    List<PermissionState> permissionsStates;
                    this.mWriteScheduled.delete(userId);
                    packageCount = Settings.this.mPackages.size();
                    for (i = 0; i < packageCount; i++) {
                        packageName = (String) Settings.this.mPackages.keyAt(i);
                        PackageSetting packageSetting = (PackageSetting) Settings.this.mPackages.valueAt(i);
                        if (packageSetting.sharedUser == null) {
                            permissionsStates = packageSetting.getPermissionsState().getRuntimePermissionStates(userId);
                            if (!permissionsStates.isEmpty()) {
                                permissionsForPackage.put(packageName, permissionsStates);
                            }
                        }
                    }
                    sharedUserCount = Settings.this.mSharedUsers.size();
                    for (i = 0; i < sharedUserCount; i++) {
                        String sharedUserName = (String) Settings.this.mSharedUsers.keyAt(i);
                        permissionsStates = ((SharedUserSetting) Settings.this.mSharedUsers.valueAt(i)).getPermissionsState().getRuntimePermissionStates(userId);
                        if (!permissionsStates.isEmpty()) {
                            permissionsForSharedUser.put(sharedUserName, permissionsStates);
                        }
                    }
                } catch (Throwable th) {
                    throw th;
                }
            }
            AutoCloseable autoCloseable = null;
            try {
                List<PermissionState> permissionStates;
                autoCloseable = destination.startWrite();
                XmlSerializer serializer = Xml.newSerializer();
                serializer.setOutput(autoCloseable, StandardCharsets.UTF_8.name());
                serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
                serializer.startDocument(null, Boolean.valueOf(true));
                serializer.startTag(null, Settings.TAG_RUNTIME_PERMISSIONS);
                String fingerprint = (String) this.mFingerprints.get(userId);
                if (fingerprint != null) {
                    serializer.attribute(null, Settings.ATTR_FINGERPRINT, fingerprint);
                }
                packageCount = permissionsForPackage.size();
                for (i = 0; i < packageCount; i++) {
                    packageName = (String) permissionsForPackage.keyAt(i);
                    permissionStates = (List) permissionsForPackage.valueAt(i);
                    serializer.startTag(null, "pkg");
                    serializer.attribute(null, Settings.ATTR_NAME, packageName);
                    writePermissions(serializer, permissionStates);
                    serializer.endTag(null, "pkg");
                }
                sharedUserCount = permissionsForSharedUser.size();
                for (i = 0; i < sharedUserCount; i++) {
                    packageName = (String) permissionsForSharedUser.keyAt(i);
                    permissionStates = (List) permissionsForSharedUser.valueAt(i);
                    serializer.startTag(null, Settings.TAG_SHARED_USER);
                    serializer.attribute(null, Settings.ATTR_NAME, packageName);
                    writePermissions(serializer, permissionStates);
                    serializer.endTag(null, Settings.TAG_SHARED_USER);
                }
                serializer.endTag(null, Settings.TAG_RUNTIME_PERMISSIONS);
                if (Settings.this.mRestoredUserGrants.get(userId) != null) {
                    ArrayMap<String, ArraySet<RestoredPermissionGrant>> restoredGrants = (ArrayMap) Settings.this.mRestoredUserGrants.get(userId);
                    if (restoredGrants != null) {
                        int pkgCount = restoredGrants.size();
                        for (i = 0; i < pkgCount; i++) {
                            ArraySet<RestoredPermissionGrant> pkgGrants = (ArraySet) restoredGrants.valueAt(i);
                            if (pkgGrants != null && pkgGrants.size() > 0) {
                                String pkgName = (String) restoredGrants.keyAt(i);
                                serializer.startTag(null, Settings.TAG_RESTORED_RUNTIME_PERMISSIONS);
                                serializer.attribute(null, Settings.ATTR_PACKAGE_NAME, pkgName);
                                int N = pkgGrants.size();
                                for (int z = 0; z < N; z++) {
                                    RestoredPermissionGrant g = (RestoredPermissionGrant) pkgGrants.valueAt(z);
                                    serializer.startTag(null, Settings.TAG_PERMISSION_ENTRY);
                                    serializer.attribute(null, Settings.ATTR_NAME, g.permissionName);
                                    if (g.granted) {
                                        serializer.attribute(null, Settings.ATTR_GRANTED, "true");
                                    }
                                    if ((g.grantBits & 1) != 0) {
                                        serializer.attribute(null, Settings.ATTR_USER_SET, "true");
                                    }
                                    if ((g.grantBits & 2) != 0) {
                                        serializer.attribute(null, Settings.ATTR_USER_FIXED, "true");
                                    }
                                    if ((g.grantBits & 8) != 0) {
                                        serializer.attribute(null, Settings.ATTR_REVOKE_ON_UPGRADE, "true");
                                    }
                                    serializer.endTag(null, Settings.TAG_PERMISSION_ENTRY);
                                }
                                serializer.endTag(null, Settings.TAG_RESTORED_RUNTIME_PERMISSIONS);
                            }
                        }
                    }
                }
                serializer.endDocument();
                destination.finishWrite(autoCloseable);
                if (Build.FINGERPRINT.equals(fingerprint)) {
                    this.mDefaultPermissionsGranted.put(userId, true);
                }
                IoUtils.closeQuietly(autoCloseable);
            } catch (Throwable th2) {
                IoUtils.closeQuietly(autoCloseable);
                throw th2;
            }
        }

        private void onUserRemovedLPw(int userId) {
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
            for (PermissionState permissionState : permissionsState.getRuntimePermissionStates(userId)) {
                BasePermission bp = (BasePermission) Settings.this.mPermissions.get(permissionState.getName());
                if (bp != null) {
                    permissionsState.revokeRuntimePermission(bp, userId);
                    permissionsState.updatePermissionFlags(bp, userId, 255, 0);
                }
            }
        }

        public void deleteUserRuntimePermissionsFile(int userId) {
            Settings.this.getUserRuntimePermissionsFile(userId).delete();
        }

        /* JADX WARNING: Removed duplicated region for block: B:12:0x0030 A:{Splitter: B:5:0x0016, ExcHandler: org.xmlpull.v1.XmlPullParserException (r0_0 'e' java.lang.Exception)} */
        /* JADX WARNING: Missing block: B:12:0x0030, code:
            r0 = move-exception;
     */
        /* JADX WARNING: Missing block: B:15:0x004a, code:
            throw new java.lang.IllegalStateException("Failed parsing permissions file: " + r4, r0);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
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
                    } catch (Exception e) {
                    } catch (Throwable th) {
                        IoUtils.closeQuietly(in);
                    }
                } catch (FileNotFoundException e2) {
                    Slog.i("PackageManager", "No permissions state");
                }
            }
        }

        public void rememberRestoredUserGrantLPr(String pkgName, String permission, boolean isGranted, int restoredFlagSet, int userId) {
            ArrayMap<String, ArraySet<RestoredPermissionGrant>> grantsByPackage = (ArrayMap) Settings.this.mRestoredUserGrants.get(userId);
            if (grantsByPackage == null) {
                grantsByPackage = new ArrayMap();
                Settings.this.mRestoredUserGrants.put(userId, grantsByPackage);
            }
            ArraySet<RestoredPermissionGrant> grants = (ArraySet) grantsByPackage.get(pkgName);
            if (grants == null) {
                grants = new ArraySet();
                grantsByPackage.put(pkgName, grants);
            }
            grants.add(new RestoredPermissionGrant(permission, isGranted, restoredFlagSet));
        }

        private void parseRuntimePermissionsLPr(XmlPullParser parser, int userId) throws IOException, XmlPullParserException {
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
                    String name = parser.getName();
                    String name2;
                    if (name.equals(Settings.TAG_RUNTIME_PERMISSIONS)) {
                        String fingerprint = parser.getAttributeValue(null, Settings.ATTR_FINGERPRINT);
                        this.mFingerprints.put(userId, fingerprint);
                        this.mDefaultPermissionsGranted.put(userId, Build.FINGERPRINT.equals(fingerprint));
                    } else if (name.equals("pkg")) {
                        name2 = parser.getAttributeValue(null, Settings.ATTR_NAME);
                        PackageSetting ps = (PackageSetting) Settings.this.mPackages.get(name2);
                        if (ps == null) {
                            Slog.w("PackageManager", "Unknown package:" + name2);
                            XmlUtils.skipCurrentTag(parser);
                        } else {
                            parsePermissionsLPr(parser, ps.getPermissionsState(), userId);
                        }
                    } else if (name.equals(Settings.TAG_SHARED_USER)) {
                        name2 = parser.getAttributeValue(null, Settings.ATTR_NAME);
                        SharedUserSetting sus = (SharedUserSetting) Settings.this.mSharedUsers.get(name2);
                        if (sus == null) {
                            Slog.w("PackageManager", "Unknown shared user:" + name2);
                            XmlUtils.skipCurrentTag(parser);
                        } else {
                            parsePermissionsLPr(parser, sus.getPermissionsState(), userId);
                        }
                    } else if (name.equals(Settings.TAG_RESTORED_RUNTIME_PERMISSIONS)) {
                        parseRestoredRuntimePermissionsLPr(parser, parser.getAttributeValue(null, Settings.ATTR_PACKAGE_NAME), userId);
                    }
                }
            }
        }

        private void parseRestoredRuntimePermissionsLPr(XmlPullParser parser, String pkgName, int userId) throws IOException, XmlPullParserException {
            int outerDepth = parser.getDepth();
            while (true) {
                int type = parser.next();
                if (type == 1) {
                    return;
                }
                if (type == 3 && parser.getDepth() <= outerDepth) {
                    return;
                }
                if (!(type == 3 || type == 4 || !parser.getName().equals(Settings.TAG_PERMISSION_ENTRY))) {
                    String permName = parser.getAttributeValue(null, Settings.ATTR_NAME);
                    boolean isGranted = "true".equals(parser.getAttributeValue(null, Settings.ATTR_GRANTED));
                    int permBits = 0;
                    if ("true".equals(parser.getAttributeValue(null, Settings.ATTR_USER_SET))) {
                        permBits = 1;
                    }
                    if ("true".equals(parser.getAttributeValue(null, Settings.ATTR_USER_FIXED))) {
                        permBits |= 2;
                    }
                    if ("true".equals(parser.getAttributeValue(null, Settings.ATTR_REVOKE_ON_UPGRADE))) {
                        permBits |= 8;
                    }
                    if (isGranted || permBits != 0) {
                        rememberRestoredUserGrantLPr(pkgName, permName, isGranted, permBits, userId);
                    }
                }
            }
        }

        private void parsePermissionsLPr(XmlPullParser parser, PermissionsState permissionsState, int userId) throws IOException, XmlPullParserException {
            int outerDepth = parser.getDepth();
            while (true) {
                int type = parser.next();
                if (type == 1) {
                    return;
                }
                if (type == 3 && parser.getDepth() <= outerDepth) {
                    return;
                }
                if (!(type == 3 || type == 4 || !parser.getName().equals(Settings.TAG_ITEM))) {
                    String name = parser.getAttributeValue(null, Settings.ATTR_NAME);
                    BasePermission bp = (BasePermission) Settings.this.mPermissions.get(name);
                    if (bp == null) {
                        Slog.w("PackageManager", "Unknown permission:" + name);
                        XmlUtils.skipCurrentTag(parser);
                    } else {
                        boolean granted;
                        String grantedStr = parser.getAttributeValue(null, Settings.ATTR_GRANTED);
                        if (grantedStr != null) {
                            granted = Boolean.parseBoolean(grantedStr);
                        } else {
                            granted = true;
                        }
                        String flagsStr = parser.getAttributeValue(null, Settings.ATTR_FLAGS);
                        int flags = flagsStr != null ? Integer.parseInt(flagsStr, 16) : 0;
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

        private void writePermissions(XmlSerializer serializer, List<PermissionState> permissionStates) throws IOException {
            for (PermissionState permissionState : permissionStates) {
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
        int sdkVersion;

        public void forceCurrent() {
            this.sdkVersion = VERSION.SDK_INT;
            this.databaseVersion = 3;
            this.fingerprint = Build.FINGERPRINT;
        }
    }

    Settings(Object lock) {
        this(Environment.getDataDirectory(), lock);
    }

    Settings(File dataDir, Object lock) {
        this.mPackages = new ArrayMap();
        this.mInstallerPackages = new ArraySet();
        this.mKernelMapping = new ArrayMap();
        this.mDisabledSysPackages = new ArrayMap();
        this.mBlockUninstallPackages = new SparseArray();
        this.mRestoredIntentFilterVerifications = new ArrayMap();
        this.mRestoredUserGrants = new SparseArray();
        this.mVersion = new ArrayMap();
        this.mPreferredActivities = new SparseArray();
        this.mPersistentPreferredActivities = new SparseArray();
        this.mCrossProfileIntentResolvers = new SparseArray();
        this.mSharedUsers = new ArrayMap();
        this.mUserIds = new ArrayList();
        this.mOtherUserIds = new SparseArray();
        this.mPastSignatures = new ArrayList();
        this.mKeySetRefs = new ArrayMap();
        this.mPermissions = new ArrayMap();
        this.mPermissionTrees = new ArrayMap();
        this.mPackagesToBeCleaned = new ArrayList();
        this.mRenamedPackages = new ArrayMap();
        this.mDefaultBrowserApp = new SparseArray();
        this.mDefaultDialerApp = new SparseArray();
        this.mNextAppLinkGeneration = new SparseIntArray();
        this.mReadMessages = new StringBuilder();
        this.mPendingPackages = new ArrayList();
        this.mKeySetManagerService = new KeySetManagerService(this.mPackages);
        this.mDelAppLists = new ArrayList();
        this.mLock = lock;
        this.mRuntimePermissionsPersistence = new RuntimePermissionPersistence(this.mLock);
        this.mSystemDir = new File(dataDir, "system");
        this.mSystemDir.mkdirs();
        FileUtils.setPermissions(this.mSystemDir.toString(), 509, -1, -1);
        this.mSettingsFilename = new File(this.mSystemDir, "packages.xml");
        this.mBackupSettingsFilename = new File(this.mSystemDir, "packages-backup.xml");
        this.mPackageListFilename = new File(this.mSystemDir, "packages.list");
        FileUtils.setPermissions(this.mPackageListFilename, 416, 1000, 1032);
        File kernelDir = new File("/config/sdcardfs");
        if (!kernelDir.exists()) {
            kernelDir = null;
        }
        this.mKernelMappingFilename = kernelDir;
        this.mStoppedPackagesFilename = new File(this.mSystemDir, "packages-stopped.xml");
        this.mBackupStoppedPackagesFilename = new File(this.mSystemDir, "packages-stopped-backup.xml");
    }

    PackageSetting getPackageLPr(String pkgName) {
        return (PackageSetting) this.mPackages.get(pkgName);
    }

    String getRenamedPackageLPr(String pkgName) {
        return (String) this.mRenamedPackages.get(pkgName);
    }

    String addRenamedPackageLPw(String pkgName, String origPkgName) {
        return (String) this.mRenamedPackages.put(pkgName, origPkgName);
    }

    void setInstallStatus(String pkgName, int status) {
        PackageSetting p = (PackageSetting) this.mPackages.get(pkgName);
        if (p != null && p.getInstallStatus() != status) {
            p.setInstallStatus(status);
        }
    }

    void applyPendingPermissionGrantsLPw(String packageName, int userId) {
        ArrayMap<String, ArraySet<RestoredPermissionGrant>> grantsByPackage = (ArrayMap) this.mRestoredUserGrants.get(userId);
        if (grantsByPackage != null && grantsByPackage.size() != 0) {
            ArraySet<RestoredPermissionGrant> grants = (ArraySet) grantsByPackage.get(packageName);
            if (grants != null && grants.size() != 0) {
                PackageSetting ps = (PackageSetting) this.mPackages.get(packageName);
                if (ps == null) {
                    Slog.e(TAG, "Can't find supposedly installed package " + packageName);
                    return;
                }
                PermissionsState perms = ps.getPermissionsState();
                for (RestoredPermissionGrant grant : grants) {
                    BasePermission bp = (BasePermission) this.mPermissions.get(grant.permissionName);
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

    void setInstallerPackageName(String pkgName, String installerPkgName) {
        PackageSetting p = (PackageSetting) this.mPackages.get(pkgName);
        if (p != null) {
            p.setInstallerPackageName(installerPkgName);
            if (installerPkgName != null) {
                this.mInstallerPackages.add(installerPkgName);
            }
        }
    }

    SharedUserSetting getSharedUserLPw(String name, int pkgFlags, int pkgPrivateFlags, boolean create) throws PackageManagerException {
        SharedUserSetting s = (SharedUserSetting) this.mSharedUsers.get(name);
        if (s == null && create) {
            s = new SharedUserSetting(name, pkgFlags, pkgPrivateFlags);
            s.userId = newUserIdLPw(s);
            if (s.userId < 0) {
                throw new PackageManagerException(-4, "Creating shared user " + name + " failed");
            }
            Log.i("PackageManager", "New shared user " + name + ": id=" + s.userId);
            this.mSharedUsers.put(name, s);
        }
        return s;
    }

    Collection<SharedUserSetting> getAllSharedUsersLPw() {
        return this.mSharedUsers.values();
    }

    String getDisabledSysPackagesPath(String name) {
        PackageSetting dp = (PackageSetting) this.mDisabledSysPackages.get(name);
        if (dp == null) {
            return null;
        }
        return dp.codePathString;
    }

    boolean disableSystemPackageLPw(String name) {
        return disableSystemPackageLPw(name, true);
    }

    boolean disableSystemPackageLPw(String name, boolean replaced) {
        PackageSetting p = (PackageSetting) this.mPackages.get(name);
        if (p == null) {
            Log.w("PackageManager", "Package " + name + " is not an installed package");
            return false;
        } else if (((PackageSetting) this.mDisabledSysPackages.get(name)) != null || p.pkg == null || !p.pkg.isSystemApp() || (p.pkg.isUpdatedSystemApp() ^ 1) == 0) {
            return false;
        } else {
            if (!(p.pkg == null || p.pkg.applicationInfo == null)) {
                ApplicationInfo applicationInfo = p.pkg.applicationInfo;
                applicationInfo.flags |= 128;
            }
            this.mDisabledSysPackages.put(name, p);
            if (replaced) {
                replacePackageLPw(name, new PackageSetting(p));
            }
            return true;
        }
    }

    PackageSetting enableSystemPackageLPw(String name) {
        PackageSetting p = (PackageSetting) this.mDisabledSysPackages.get(name);
        if (p == null) {
            Log.w("PackageManager", "Package " + name + " is not disabled");
            return null;
        }
        if (!(p.pkg == null || p.pkg.applicationInfo == null)) {
            ApplicationInfo applicationInfo = p.pkg.applicationInfo;
            applicationInfo.flags &= -129;
        }
        PackageSetting ret = addPackageLPw(name, p.realName, p.codePath, p.resourcePath, p.legacyNativeLibraryPathString, p.primaryCpuAbiString, p.secondaryCpuAbiString, p.cpuAbiOverrideString, p.appId, p.versionCode, p.pkgFlags, p.pkgPrivateFlags, p.parentPackageName, p.childPackageNames, p.usesStaticLibraries, p.usesStaticLibrariesVersions);
        this.mDisabledSysPackages.remove(name);
        return ret;
    }

    String getDisabledSystemPackageName(String filePath) {
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

    boolean isDisabledSystemPackageLPr(String name) {
        return this.mDisabledSysPackages.containsKey(name);
    }

    void removeDisabledSystemPackageLPw(String name) {
        this.mDisabledSysPackages.remove(name);
    }

    PackageSetting addPackageLPw(String name, String realName, File codePath, File resourcePath, String legacyNativeLibraryPathString, String primaryCpuAbiString, String secondaryCpuAbiString, String cpuAbiOverrideString, int uid, int vc, int pkgFlags, int pkgPrivateFlags, String parentPackageName, List<String> childPackageNames, String[] usesStaticLibraries, int[] usesStaticLibraryNames) {
        PackageSetting p = (PackageSetting) this.mPackages.get(name);
        if (p == null) {
            p = new PackageSetting(name, realName, codePath, resourcePath, legacyNativeLibraryPathString, primaryCpuAbiString, secondaryCpuAbiString, cpuAbiOverrideString, vc, pkgFlags, pkgPrivateFlags, parentPackageName, childPackageNames, 0, usesStaticLibraries, usesStaticLibraryNames);
            p.appId = uid;
            if (!addUserIdLPw(uid, p, name)) {
                return null;
            }
            this.mPackages.put(name, p);
            return p;
        } else if (p.appId == uid) {
            return p;
        } else {
            PackageManagerService.reportSettingsProblem(6, "Adding duplicate package, keeping first: " + name);
            return null;
        }
    }

    SharedUserSetting addSharedUserLPw(String name, int uid, int pkgFlags, int pkgPrivateFlags) {
        SharedUserSetting s = (SharedUserSetting) this.mSharedUsers.get(name);
        if (s == null) {
            s = new SharedUserSetting(name, pkgFlags, pkgPrivateFlags);
            s.userId = uid;
            if (!addUserIdLPw(uid, s, name)) {
                return null;
            }
            this.mSharedUsers.put(name, s);
            return s;
        } else if (s.userId == uid) {
            return s;
        } else {
            PackageManagerService.reportSettingsProblem(6, "Adding duplicate shared user, keeping first: " + name);
            return null;
        }
    }

    void pruneSharedUsersLPw() {
        ArrayList<String> removeStage = new ArrayList();
        for (Entry<String, SharedUserSetting> entry : this.mSharedUsers.entrySet()) {
            SharedUserSetting sus = (SharedUserSetting) entry.getValue();
            if (sus == null) {
                removeStage.add((String) entry.getKey());
            } else {
                Iterator<PackageSetting> iter = sus.packages.iterator();
                while (iter.hasNext()) {
                    if (this.mPackages.get(((PackageSetting) iter.next()).name) == null) {
                        iter.remove();
                    }
                }
                if (sus.packages.size() == 0) {
                    removeStage.add((String) entry.getKey());
                }
            }
        }
        for (int i = 0; i < removeStage.size(); i++) {
            this.mSharedUsers.remove(removeStage.get(i));
        }
    }

    void transferPermissionsLPw(String origPkg, String newPkg) {
        int i = 0;
        while (i < 2) {
            for (BasePermission bp : (i == 0 ? this.mPermissionTrees : this.mPermissions).values()) {
                if (origPkg.equals(bp.sourcePackage)) {
                    bp.sourcePackage = newPkg;
                    bp.packageSetting = null;
                    bp.perm = null;
                    if (bp.pendingInfo != null) {
                        bp.pendingInfo.packageName = newPkg;
                    }
                    bp.uid = 0;
                    bp.setGids(null, false);
                }
            }
            i++;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:46:0x0125  */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x0107  */
    /* JADX WARNING: Missing block: B:35:0x00ef, code:
            if ((isAdbInstallDisallowed(r45, r21.id) ^ 1) == 0) goto L_0x00f1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static PackageSetting createNewSetting(String pkgName, PackageSetting originalPkg, PackageSetting disabledPkg, String realPkgName, SharedUserSetting sharedUser, File codePath, File resourcePath, String legacyNativeLibraryPath, String primaryCpuAbi, String secondaryCpuAbi, int versionCode, int pkgFlags, int pkgPrivateFlags, UserHandle installUser, boolean allowInstall, boolean instantApp, String parentPkgName, List<String> childPkgNames, UserManagerService userManager, String[] usesStaticLibraries, int[] usesStaticLibrariesVersions) {
        PackageSetting pkgSetting;
        List<UserInfo> users;
        if (originalPkg != null) {
            pkgSetting = new PackageSetting(originalPkg, pkgName);
            pkgSetting.childPackageNames = childPkgNames != null ? new ArrayList(childPkgNames) : null;
            pkgSetting.codePath = codePath;
            pkgSetting.legacyNativeLibraryPathString = legacyNativeLibraryPath;
            pkgSetting.origPackage = originalPkg;
            pkgSetting.parentPackageName = parentPkgName;
            pkgSetting.pkgFlags = pkgFlags;
            pkgSetting.pkgPrivateFlags = pkgPrivateFlags;
            pkgSetting.primaryCpuAbiString = primaryCpuAbi;
            pkgSetting.resourcePath = resourcePath;
            pkgSetting.secondaryCpuAbiString = secondaryCpuAbi;
            pkgSetting.signatures = new PackageSignatures();
            pkgSetting.versionCode = versionCode;
            pkgSetting.usesStaticLibraries = usesStaticLibraries;
            pkgSetting.usesStaticLibrariesVersions = usesStaticLibrariesVersions;
            pkgSetting.setTimeStamp(codePath.lastModified());
        } else {
            pkgSetting = new PackageSetting(pkgName, realPkgName, codePath, resourcePath, legacyNativeLibraryPath, primaryCpuAbi, secondaryCpuAbi, null, versionCode, pkgFlags, pkgPrivateFlags, parentPkgName, childPkgNames, 0, usesStaticLibraries, usesStaticLibrariesVersions);
            pkgSetting.setTimeStamp(codePath.lastModified());
            pkgSetting.sharedUser = sharedUser;
            if ((pkgFlags & 1) == 0 || HwServiceFactory.isCustedCouldStopped(pkgName, false, false)) {
                users = getAllUsers(userManager);
                int installUserId = installUser != null ? installUser.getIdentifier() : 0;
                if (users != null && allowInstall) {
                    for (UserInfo user : users) {
                        boolean installed;
                        int i;
                        boolean z;
                        if (installUser != null) {
                            if (installUserId == -1) {
                            }
                            installed = installUserId == user.id;
                            i = user.id;
                            if (user.isClonedProfile()) {
                                z = installed;
                            } else {
                                z = false;
                            }
                            pkgSetting.setUserState(i, 0, 0, z, true, true, false, false, instantApp, null, null, null, 0, 0, 0);
                        }
                        installed = true;
                        i = user.id;
                        if (user.isClonedProfile()) {
                        }
                        pkgSetting.setUserState(i, 0, 0, z, true, true, false, false, instantApp, null, null, null, 0, 0, 0);
                    }
                }
            }
            if (sharedUser != null) {
                pkgSetting.appId = sharedUser.userId;
            } else if (disabledPkg != null) {
                pkgSetting.signatures = new PackageSignatures(disabledPkg.signatures);
                pkgSetting.appId = disabledPkg.appId;
                pkgSetting.getPermissionsState().copyFrom(disabledPkg.getPermissionsState());
                users = getAllUsers(userManager);
                if (users != null) {
                    for (UserInfo user2 : users) {
                        int userId = user2.id;
                        pkgSetting.setDisabledComponentsCopy(disabledPkg.getDisabledComponents(userId), userId);
                        pkgSetting.setEnabledComponentsCopy(disabledPkg.getEnabledComponents(userId), userId);
                    }
                }
            }
        }
        if ((pkgFlags & 1) != 0 && disabledPkg == null) {
            users = getAllUsers(userManager);
            if (users != null) {
                for (UserInfo userInfo : users) {
                    if (userInfo.isClonedProfile()) {
                        pkgSetting.setInstalled(false, userInfo.id);
                        break;
                    }
                }
            }
        }
        return pkgSetting;
    }

    static void updatePackageSetting(PackageSetting pkgSetting, PackageSetting disabledPkg, SharedUserSetting sharedUser, File codePath, String legacyNativeLibraryPath, String primaryCpuAbi, String secondaryCpuAbi, int pkgFlags, int pkgPrivateFlags, List<String> childPkgNames, UserManagerService userManager, String[] usesStaticLibraries, int[] usesStaticLibrariesVersions) throws PackageManagerException {
        String pkgName = pkgSetting.name;
        if (pkgSetting.sharedUser != sharedUser) {
            PackageManagerService.reportSettingsProblem(5, "Package " + pkgName + " shared user changed from " + (pkgSetting.sharedUser != null ? pkgSetting.sharedUser.name : "<nothing>") + " to " + (sharedUser != null ? sharedUser.name : "<nothing>"));
            throw new PackageManagerException(-8, "Updating application package " + pkgName + " failed");
        }
        if (!pkgSetting.codePath.equals(codePath)) {
            if ((pkgSetting.pkgFlags & 1) != 0) {
                Slog.w("PackageManager", "Trying to update system app code path from " + pkgSetting.codePathString + " to " + codePath.toString());
            } else {
                Slog.i("PackageManager", "Package " + pkgName + " codePath changed from " + pkgSetting.codePath + " to " + codePath + "; Retaining data and using new");
                if ((pkgFlags & 1) != 0 && disabledPkg == null) {
                    List<UserInfo> allUserInfos = getAllUsers(userManager);
                    if (allUserInfos != null) {
                        for (UserInfo userInfo : allUserInfos) {
                            if (!userInfo.isClonedProfile() || (pkgSetting.getInstalled(userInfo.id) ^ 1) == 0) {
                                pkgSetting.setInstalled(true, userInfo.id);
                            }
                        }
                    }
                }
                pkgSetting.legacyNativeLibraryPathString = legacyNativeLibraryPath;
            }
        }
        pkgSetting.pkgFlags |= pkgFlags & 1;
        pkgSetting.pkgPrivateFlags |= pkgPrivateFlags & 8;
        pkgSetting.primaryCpuAbiString = primaryCpuAbi;
        pkgSetting.secondaryCpuAbiString = secondaryCpuAbi;
        if (childPkgNames != null) {
            pkgSetting.childPackageNames = new ArrayList(childPkgNames);
        }
        if (usesStaticLibraries != null) {
            pkgSetting.usesStaticLibraries = (String[]) Arrays.copyOf(usesStaticLibraries, usesStaticLibraries.length);
        }
        if (usesStaticLibrariesVersions != null) {
            pkgSetting.usesStaticLibrariesVersions = Arrays.copyOf(usesStaticLibrariesVersions, usesStaticLibrariesVersions.length);
        }
    }

    void addUserToSettingLPw(PackageSetting p) throws PackageManagerException {
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

    void writeUserRestrictionsLPw(PackageSetting newPackage, PackageSetting oldPackage) {
        if (getPackageLPr(newPackage.name) != null) {
            List<UserInfo> allUsers = getAllUsers(UserManagerService.getInstance());
            if (allUsers != null) {
                for (UserInfo user : allUsers) {
                    PackageUserState oldUserState;
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

    void insertPackageSettingLPw(PackageSetting p, Package pkg) {
        p.pkg = pkg;
        String volumeUuid = pkg.applicationInfo.volumeUuid;
        String codePath = pkg.applicationInfo.getCodePath();
        String resourcePath = pkg.applicationInfo.getResourcePath();
        String legacyNativeLibraryPath = pkg.applicationInfo.nativeLibraryRootDir;
        if (!Objects.equals(volumeUuid, p.volumeUuid)) {
            Slog.w("PackageManager", "Volume for " + p.pkg.packageName + " changing from " + p.volumeUuid + " to " + volumeUuid);
            p.volumeUuid = volumeUuid;
        }
        if (!Objects.equals(codePath, p.codePathString)) {
            Slog.w("PackageManager", "Code path for " + p.pkg.packageName + " changing from " + p.codePathString + " to " + codePath);
            p.codePath = new File(codePath);
            p.codePathString = codePath;
        }
        if (!Objects.equals(resourcePath, p.resourcePathString)) {
            Slog.w("PackageManager", "Resource path for " + p.pkg.packageName + " changing from " + p.resourcePathString + " to " + resourcePath);
            p.resourcePath = new File(resourcePath);
            p.resourcePathString = resourcePath;
        }
        if (!Objects.equals(legacyNativeLibraryPath, p.legacyNativeLibraryPathString)) {
            p.legacyNativeLibraryPathString = legacyNativeLibraryPath;
        }
        p.primaryCpuAbiString = pkg.applicationInfo.primaryCpuAbi;
        p.secondaryCpuAbiString = pkg.applicationInfo.secondaryCpuAbi;
        p.cpuAbiOverrideString = pkg.cpuAbiOverride;
        if (pkg.mVersionCode != p.versionCode) {
            p.versionCode = pkg.mVersionCode;
        }
        if (p.signatures.mSignatures == null) {
            p.signatures.assignSignatures(pkg.mSignatures);
        }
        if (pkg.applicationInfo.flags != p.pkgFlags) {
            p.pkgFlags = pkg.applicationInfo.flags;
        }
        if (p.sharedUser != null && p.sharedUser.signatures.mSignatures == null) {
            p.sharedUser.signatures.assignSignatures(pkg.mSignatures);
        }
        if (pkg.usesStaticLibraries == null || pkg.usesStaticLibrariesVersions == null || pkg.usesStaticLibraries.size() != pkg.usesStaticLibrariesVersions.length) {
            p.usesStaticLibraries = null;
            p.usesStaticLibrariesVersions = null;
        } else {
            p.usesStaticLibraries = new String[pkg.usesStaticLibraries.size()];
            pkg.usesStaticLibraries.toArray(p.usesStaticLibraries);
            p.usesStaticLibrariesVersions = pkg.usesStaticLibrariesVersions;
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
        SettingBase userIdPs = getUserIdLPr(p.appId);
        if (sharedUser == null) {
            if (!(userIdPs == null || userIdPs == p)) {
                replaceUserIdLPw(p.appId, p);
            }
        } else if (!(userIdPs == null || userIdPs == sharedUser)) {
            replaceUserIdLPw(p.appId, sharedUser);
        }
        IntentFilterVerificationInfo ivi = (IntentFilterVerificationInfo) this.mRestoredIntentFilterVerifications.get(p.name);
        if (ivi != null) {
            this.mRestoredIntentFilterVerifications.remove(p.name);
            p.setIntentFilterVerificationInfo(ivi);
        }
    }

    int updateSharedUserPermsLPw(PackageSetting deletedPs, int userId) {
        if (deletedPs == null || deletedPs.pkg == null) {
            Slog.i("PackageManager", "Trying to update info for null package. Just ignoring");
            return -10000;
        } else if (deletedPs.sharedUser == null) {
            return -10000;
        } else {
            SharedUserSetting sus = deletedPs.sharedUser;
            for (String eachPerm : deletedPs.pkg.requestedPermissions) {
                BasePermission bp = (BasePermission) this.mPermissions.get(eachPerm);
                if (bp != null) {
                    boolean used = false;
                    for (PackageSetting pkg : sus.packages) {
                        if (pkg.pkg != null && (pkg.pkg.packageName.equals(deletedPs.pkg.packageName) ^ 1) != 0 && pkg.pkg.requestedPermissions.contains(eachPerm)) {
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
                            for (String permission : disabledPs.pkg.requestedPermissions) {
                                if (permission.equals(eachPerm)) {
                                    reqByDisabledSysPkg = true;
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

    int removePackageLPw(String name) {
        PackageSetting p = (PackageSetting) this.mPackages.get(name);
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
            }
            removeUserIdLPw(p.appId);
            return p.appId;
        }
        return -1;
    }

    private void removeInstallerPackageStatus(String packageName) {
        if (this.mInstallerPackages.contains(packageName)) {
            for (int i = 0; i < this.mPackages.size(); i++) {
                PackageSetting ps = (PackageSetting) this.mPackages.valueAt(i);
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
        PackageSetting p = (PackageSetting) this.mPackages.get(name);
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
        if (uid < 10000) {
            return this.mOtherUserIds.get(uid);
        }
        int index = uid - 10000;
        return index < this.mUserIds.size() ? this.mUserIds.get(index) : null;
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

    PreferredIntentResolver editPreferredActivitiesLPw(int userId) {
        PreferredIntentResolver pir = (PreferredIntentResolver) this.mPreferredActivities.get(userId);
        if (pir != null) {
            return pir;
        }
        pir = new PreferredIntentResolver();
        this.mPreferredActivities.put(userId, pir);
        return pir;
    }

    PersistentPreferredIntentResolver editPersistentPreferredActivitiesLPw(int userId) {
        PersistentPreferredIntentResolver ppir = (PersistentPreferredIntentResolver) this.mPersistentPreferredActivities.get(userId);
        if (ppir != null) {
            return ppir;
        }
        ppir = new PersistentPreferredIntentResolver();
        this.mPersistentPreferredActivities.put(userId, ppir);
        return ppir;
    }

    CrossProfileIntentResolver editCrossProfileIntentResolverLPw(int userId) {
        CrossProfileIntentResolver cpir = (CrossProfileIntentResolver) this.mCrossProfileIntentResolvers.get(userId);
        if (cpir != null) {
            return cpir;
        }
        cpir = new CrossProfileIntentResolver();
        this.mCrossProfileIntentResolvers.put(userId, cpir);
        return cpir;
    }

    IntentFilterVerificationInfo getIntentFilterVerificationLPr(String packageName) {
        PackageSetting ps = (PackageSetting) this.mPackages.get(packageName);
        if (ps == null) {
            return null;
        }
        return ps.getIntentFilterVerificationInfo();
    }

    IntentFilterVerificationInfo createIntentFilterVerificationIfNeededLPw(String packageName, ArraySet<String> domains) {
        PackageSetting ps = (PackageSetting) this.mPackages.get(packageName);
        if (ps == null) {
            return null;
        }
        IntentFilterVerificationInfo ivi = ps.getIntentFilterVerificationInfo();
        if (ivi == null) {
            ivi = new IntentFilterVerificationInfo(packageName, domains);
            ps.setIntentFilterVerificationInfo(ivi);
        } else {
            ivi.setDomains(domains);
        }
        return ivi;
    }

    int getIntentFilterVerificationStatusLPr(String packageName, int userId) {
        PackageSetting ps = (PackageSetting) this.mPackages.get(packageName);
        if (ps == null) {
            return 0;
        }
        return (int) (ps.getDomainVerificationStatusForUser(userId) >> 32);
    }

    boolean updateIntentFilterVerificationStatusLPw(String packageName, int status, int userId) {
        PackageSetting current = (PackageSetting) this.mPackages.get(packageName);
        if (current == null) {
            return false;
        }
        int alwaysGeneration;
        if (status == 2) {
            alwaysGeneration = this.mNextAppLinkGeneration.get(userId) + 1;
            this.mNextAppLinkGeneration.put(userId, alwaysGeneration);
        } else {
            alwaysGeneration = 0;
        }
        current.setDomainVerificationStatusForUser(status, alwaysGeneration, userId);
        return true;
    }

    List<IntentFilterVerificationInfo> getIntentFilterVerificationsLPr(String packageName) {
        if (packageName == null) {
            return Collections.emptyList();
        }
        ArrayList<IntentFilterVerificationInfo> result = new ArrayList();
        for (PackageSetting ps : this.mPackages.values()) {
            IntentFilterVerificationInfo ivi = ps.getIntentFilterVerificationInfo();
            if (!(ivi == null || TextUtils.isEmpty(ivi.getPackageName()) || (ivi.getPackageName().equalsIgnoreCase(packageName) ^ 1) != 0)) {
                result.add(ivi);
            }
        }
        return result;
    }

    boolean removeIntentFilterVerificationLPw(String packageName, int userId) {
        PackageSetting ps = (PackageSetting) this.mPackages.get(packageName);
        if (ps == null) {
            return false;
        }
        ps.clearDomainVerificationStatusForUser(userId);
        return true;
    }

    boolean removeIntentFilterVerificationLPw(String packageName, int[] userIds) {
        boolean result = false;
        for (int userId : userIds) {
            result |= removeIntentFilterVerificationLPw(packageName, userId);
        }
        return result;
    }

    boolean setDefaultBrowserPackageNameLPw(String packageName, int userId) {
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

    String getDefaultBrowserPackageNameLPw(int userId) {
        return userId == -1 ? null : (String) this.mDefaultBrowserApp.get(userId);
    }

    boolean setDefaultDialerPackageNameLPw(String packageName, int userId) {
        if (userId == -1) {
            return false;
        }
        this.mDefaultDialerApp.put(userId, packageName);
        writePackageRestrictionsLPr(userId);
        return true;
    }

    String getDefaultDialerPackageNameLPw(int userId) {
        return userId == -1 ? null : (String) this.mDefaultDialerApp.get(userId);
    }

    private File getUserPackagesStateFile(int userId) {
        return new File(new File(new File(this.mSystemDir, SoundModelContract.KEY_USERS), Integer.toString(userId)), "package-restrictions.xml");
    }

    private File getUserRuntimePermissionsFile(int userId) {
        return new File(new File(new File(this.mSystemDir, SoundModelContract.KEY_USERS), Integer.toString(userId)), RUNTIME_PERMISSIONS_FILE_NAME);
    }

    private File getUserPackagesStateBackupFile(int userId) {
        return new File(Environment.getUserSystemDirectory(userId), "package-restrictions-backup.xml");
    }

    void writeAllUsersPackageRestrictionsLPr() {
        List<UserInfo> users = getAllUsers(UserManagerService.getInstance());
        if (users != null) {
            for (UserInfo user : users) {
                writePackageRestrictionsLPr(user.id);
            }
        }
    }

    void writeAllRuntimePermissionsLPr() {
        for (int userId : UserManagerService.getInstance().getUserIds()) {
            this.mRuntimePermissionsPersistence.writePermissionsForUserAsyncLPr(userId);
        }
    }

    boolean areDefaultRuntimePermissionsGrantedLPr(int userId) {
        return this.mRuntimePermissionsPersistence.areDefaultRuntimPermissionsGrantedLPr(userId);
    }

    void onDefaultRuntimePermissionsGrantedLPr(int userId) {
        this.mRuntimePermissionsPersistence.onDefaultRuntimePermissionsGrantedLPr(userId);
    }

    public VersionInfo findOrCreateVersion(String volumeUuid) {
        VersionInfo ver = (VersionInfo) this.mVersion.get(volumeUuid);
        if (ver != null) {
            return ver;
        }
        ver = new VersionInfo();
        this.mVersion.put(volumeUuid, ver);
        return ver;
    }

    public VersionInfo getInternalVersion() {
        return (VersionInfo) this.mVersion.get(StorageManager.UUID_PRIVATE_INTERNAL);
    }

    public VersionInfo getExternalVersion() {
        return (VersionInfo) this.mVersion.get("primary_physical");
    }

    public void onVolumeForgotten(String fsUuid) {
        this.mVersion.remove(fsUuid);
    }

    void readPreferredActivitiesLPw(XmlPullParser parser, int userId) throws XmlPullParserException, IOException {
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
            int type = parser.next();
            if (type == 1) {
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
            int type = parser.next();
            if (type == 1) {
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
            int type = parser.next();
            if (type == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == 3 || type == 4)) {
                String tagName = parser.getName();
                if (tagName.equals(TAG_DOMAIN_VERIFICATION)) {
                    IntentFilterVerificationInfo ivi = new IntentFilterVerificationInfo(parser);
                    this.mRestoredIntentFilterVerifications.put(ivi.getPackageName(), ivi);
                } else {
                    Slog.w(TAG, "Unknown element: " + tagName);
                    XmlUtils.skipCurrentTag(parser);
                }
            }
        }
    }

    void readDefaultAppsLPw(XmlPullParser parser, int userId) throws XmlPullParserException, IOException {
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
                String tagName = parser.getName();
                if (tagName.equals(TAG_DEFAULT_BROWSER)) {
                    this.mDefaultBrowserApp.put(userId, parser.getAttributeValue(null, ATTR_PACKAGE_NAME));
                } else if (tagName.equals(TAG_DEFAULT_DIALER)) {
                    this.mDefaultDialerApp.put(userId, parser.getAttributeValue(null, ATTR_PACKAGE_NAME));
                } else {
                    PackageManagerService.reportSettingsProblem(5, "Unknown element under default-apps: " + parser.getName());
                    XmlUtils.skipCurrentTag(parser);
                }
            }
        }
    }

    void readBlockUninstallPackagesLPw(XmlPullParser parser, int userId) throws XmlPullParserException, IOException {
        int outerDepth = parser.getDepth();
        ArraySet<String> packages = new ArraySet();
        while (true) {
            int type = parser.next();
            if (type != 1 && (type != 3 || parser.getDepth() > outerDepth)) {
                if (!(type == 3 || type == 4)) {
                    if (parser.getName().equals(TAG_BLOCK_UNINSTALL)) {
                        packages.add(parser.getAttributeValue(null, ATTR_PACKAGE_NAME));
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

    /* JADX WARNING: Removed duplicated region for block: B:81:0x028d A:{Catch:{ XmlPullParserException -> 0x018c, IOException -> 0x029e }} */
    /* JADX WARNING: Removed duplicated region for block: B:112:0x0105 A:{SYNTHETIC, EDGE_INSN: B:112:0x0105->B:32:0x0105 ?: BREAK  , EDGE_INSN: B:112:0x0105->B:32:0x0105 ?: BREAK  } */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x0100 A:{Catch:{ XmlPullParserException -> 0x018c, IOException -> 0x029e }} */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x011c A:{Catch:{ XmlPullParserException -> 0x018c, IOException -> 0x029e }} */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x010a A:{Catch:{ XmlPullParserException -> 0x018c, IOException -> 0x029e }} */
    /* JADX WARNING: Removed duplicated region for block: B:108:0x0387  */
    /* JADX WARNING: Removed duplicated region for block: B:9:0x0053 A:{SYNTHETIC, Splitter: B:9:0x0053} */
    /* JADX WARNING: Removed duplicated region for block: B:9:0x0053 A:{SYNTHETIC, Splitter: B:9:0x0053} */
    /* JADX WARNING: Removed duplicated region for block: B:108:0x0387  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void readPackageRestrictionsLPr(int userId) {
        InputStream fileInputStream;
        InputStream str;
        Throwable e;
        Throwable e2;
        InputStream str2 = null;
        File userPackagesStateFile = getUserPackagesStateFile(userId);
        File backupFile = getUserPackagesStateBackupFile(userId);
        if (backupFile.exists()) {
            try {
                fileInputStream = new FileInputStream(backupFile);
                try {
                    this.mReadMessages.append("Reading from backup stopped packages file\n");
                    PackageManagerService.reportSettingsProblem(4, "Need to read from backup stopped packages file");
                    if (userPackagesStateFile.exists()) {
                        Slog.w("PackageManager", "Cleaning up stopped packages file " + userPackagesStateFile);
                        userPackagesStateFile.delete();
                    }
                } catch (IOException e3) {
                    str2 = fileInputStream;
                    str = str2;
                    if (str == null) {
                    }
                }
            } catch (IOException e4) {
                str = str2;
                if (str == null) {
                }
            }
        }
        str = null;
        XmlPullParser parser;
        int type;
        if (str == null) {
            try {
                if (userPackagesStateFile.exists()) {
                    fileInputStream = new FileInputStream(userPackagesStateFile);
                    try {
                        parser = Xml.newPullParser();
                        parser.setInput(str2, StandardCharsets.UTF_8.name());
                        do {
                            type = parser.next();
                            if (type != 2) {
                                break;
                            }
                        } while (type != 1);
                        if (type == 2) {
                            this.mReadMessages.append("No start tag found in package restrictions file\n");
                            PackageManagerService.reportSettingsProblem(5, "No start tag found in package manager stopped packages");
                            return;
                        }
                        int maxAppLinkGeneration = 0;
                        int outerDepth = parser.getDepth();
                        while (true) {
                            type = parser.next();
                            if (type == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                                str2.close();
                                this.mNextAppLinkGeneration.put(userId, maxAppLinkGeneration + 1);
                            } else if (!(type == 3 || type == 4)) {
                                String tagName = parser.getName();
                                if (tagName.equals("pkg")) {
                                    String name = parser.getAttributeValue(null, ATTR_NAME);
                                    PackageSetting ps = (PackageSetting) this.mPackages.get(name);
                                    if (ps == null) {
                                        Slog.w("PackageManager", "No package known for stopped package " + name);
                                        XmlUtils.skipCurrentTag(parser);
                                    } else {
                                        long ceDataInode = XmlUtils.readLongAttribute(parser, ATTR_CE_DATA_INODE, 0);
                                        boolean installed = XmlUtils.readBooleanAttribute(parser, ATTR_INSTALLED, true);
                                        boolean stopped = XmlUtils.readBooleanAttribute(parser, ATTR_STOPPED, false);
                                        boolean notLaunched = XmlUtils.readBooleanAttribute(parser, ATTR_NOT_LAUNCHED, false);
                                        String blockedStr = parser.getAttributeValue(null, ATTR_BLOCKED);
                                        boolean hidden = blockedStr == null ? false : Boolean.parseBoolean(blockedStr);
                                        String hiddenStr = parser.getAttributeValue(null, ATTR_HIDDEN);
                                        if (hiddenStr != null) {
                                            hidden = Boolean.parseBoolean(hiddenStr);
                                        }
                                        boolean suspended = XmlUtils.readBooleanAttribute(parser, ATTR_SUSPENDED, false);
                                        boolean blockUninstall = XmlUtils.readBooleanAttribute(parser, ATTR_BLOCK_UNINSTALL, false);
                                        boolean instantApp = XmlUtils.readBooleanAttribute(parser, ATTR_INSTANT_APP, false);
                                        int enabled = XmlUtils.readIntAttribute(parser, ATTR_ENABLED, 0);
                                        String enabledCaller = parser.getAttributeValue(null, ATTR_ENABLED_CALLER);
                                        int verifState = XmlUtils.readIntAttribute(parser, ATTR_DOMAIN_VERIFICATON_STATE, 0);
                                        int linkGeneration = XmlUtils.readIntAttribute(parser, ATTR_APP_LINK_GENERATION, 0);
                                        if (linkGeneration > maxAppLinkGeneration) {
                                            maxAppLinkGeneration = linkGeneration;
                                        }
                                        int installReason = XmlUtils.readIntAttribute(parser, ATTR_INSTALL_REASON, 0);
                                        ArraySet enabledComponents = null;
                                        ArraySet disabledComponents = null;
                                        int packageDepth = parser.getDepth();
                                        while (true) {
                                            type = parser.next();
                                            if (type == 1 || (type == 3 && parser.getDepth() <= packageDepth)) {
                                                if (blockUninstall) {
                                                    setBlockUninstallLPw(userId, name, true);
                                                }
                                            } else if (!(type == 3 || type == 4)) {
                                                tagName = parser.getName();
                                                if (tagName.equals(TAG_ENABLED_COMPONENTS)) {
                                                    enabledComponents = readComponentsLPr(parser);
                                                } else {
                                                    if (tagName.equals(TAG_DISABLED_COMPONENTS)) {
                                                        disabledComponents = readComponentsLPr(parser);
                                                    }
                                                }
                                            }
                                        }
                                        if (blockUninstall) {
                                        }
                                        ps.setUserState(userId, ceDataInode, enabled, installed, stopped, notLaunched, hidden, suspended, instantApp, enabledCaller, enabledComponents, disabledComponents, verifState, linkGeneration, installReason);
                                    }
                                } else {
                                    if (tagName.equals("preferred-activities")) {
                                        readPreferredActivitiesLPw(parser, userId);
                                    } else {
                                        if (tagName.equals(TAG_PERSISTENT_PREFERRED_ACTIVITIES)) {
                                            readPersistentPreferredActivitiesLPw(parser, userId);
                                        } else {
                                            if (tagName.equals(TAG_CROSS_PROFILE_INTENT_FILTERS)) {
                                                readCrossProfileIntentFiltersLPw(parser, userId);
                                            } else {
                                                if (tagName.equals(TAG_DEFAULT_APPS)) {
                                                    readDefaultAppsLPw(parser, userId);
                                                } else {
                                                    if (tagName.equals(TAG_BLOCK_UNINSTALL_PACKAGES)) {
                                                        readBlockUninstallPackagesLPw(parser, userId);
                                                    } else {
                                                        Slog.w("PackageManager", "Unknown element under <stopped-packages>: " + parser.getName());
                                                        XmlUtils.skipCurrentTag(parser);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        str2.close();
                        this.mNextAppLinkGeneration.put(userId, maxAppLinkGeneration + 1);
                    } catch (XmlPullParserException e5) {
                        e = e5;
                        this.mReadMessages.append("Error reading: ").append(e.toString());
                        PackageManagerService.reportSettingsProblem(6, "Error reading stopped packages: " + e);
                        Slog.wtf("PackageManager", "Error reading package manager stopped packages", e);
                    } catch (IOException e6) {
                        e2 = e6;
                        this.mReadMessages.append("Error reading: ").append(e2.toString());
                        PackageManagerService.reportSettingsProblem(6, "Error reading settings: " + e2);
                        Slog.wtf("PackageManager", "Error reading package manager stopped packages", e2);
                    }
                }
                this.mReadMessages.append("No stopped packages file found\n");
                PackageManagerService.reportSettingsProblem(4, "No stopped packages file; assuming all started");
                for (PackageSetting pkg : this.mPackages.values()) {
                    pkg.setUserState(userId, 0, 0, true, false, false, false, false, false, null, null, null, 0, 0, 0);
                }
                return;
            } catch (XmlPullParserException e7) {
                e = e7;
                this.mReadMessages.append("Error reading: ").append(e.toString());
                PackageManagerService.reportSettingsProblem(6, "Error reading stopped packages: " + e);
                Slog.wtf("PackageManager", "Error reading package manager stopped packages", e);
            } catch (IOException e8) {
                e2 = e8;
                this.mReadMessages.append("Error reading: ").append(e2.toString());
                PackageManagerService.reportSettingsProblem(6, "Error reading settings: " + e2);
                Slog.wtf("PackageManager", "Error reading package manager stopped packages", e2);
            }
        }
        str2 = str;
        parser = Xml.newPullParser();
        parser.setInput(str2, StandardCharsets.UTF_8.name());
        do {
            type = parser.next();
            if (type != 2) {
            }
        } while (type != 1);
        if (type == 2) {
        }
    }

    void setBlockUninstallLPw(int userId, String packageName, boolean blockUninstall) {
        ArraySet<String> packages = (ArraySet) this.mBlockUninstallPackages.get(userId);
        if (blockUninstall) {
            if (packages == null) {
                packages = new ArraySet();
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

    boolean getBlockUninstallLPr(int userId, String packageName) {
        ArraySet<String> packages = (ArraySet) this.mBlockUninstallPackages.get(userId);
        if (packages == null) {
            return false;
        }
        return packages.contains(packageName);
    }

    private ArraySet<String> readComponentsLPr(XmlPullParser parser) throws IOException, XmlPullParserException {
        ArraySet<String> components = null;
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                return components;
            }
            if (!(type == 3 || type == 4 || !parser.getName().equals(TAG_ITEM))) {
                String componentName = parser.getAttributeValue(null, ATTR_NAME);
                if (componentName != null) {
                    if (components == null) {
                        components = new ArraySet();
                    }
                    components.add(componentName);
                }
            }
        }
        return components;
    }

    void writePreferredActivitiesLPr(XmlSerializer serializer, int userId, boolean full) throws IllegalArgumentException, IllegalStateException, IOException {
        serializer.startTag(null, "preferred-activities");
        PreferredIntentResolver pir = (PreferredIntentResolver) this.mPreferredActivities.get(userId);
        if (pir != null) {
            for (PreferredActivity pa : pir.filterSet()) {
                serializer.startTag(null, TAG_ITEM);
                pa.writeToXml(serializer, full);
                serializer.endTag(null, TAG_ITEM);
            }
        }
        serializer.endTag(null, "preferred-activities");
    }

    void writePersistentPreferredActivitiesLPr(XmlSerializer serializer, int userId) throws IllegalArgumentException, IllegalStateException, IOException {
        serializer.startTag(null, TAG_PERSISTENT_PREFERRED_ACTIVITIES);
        PersistentPreferredIntentResolver ppir = (PersistentPreferredIntentResolver) this.mPersistentPreferredActivities.get(userId);
        if (ppir != null) {
            for (PersistentPreferredActivity ppa : ppir.filterSet()) {
                serializer.startTag(null, TAG_ITEM);
                ppa.writeToXml(serializer);
                serializer.endTag(null, TAG_ITEM);
            }
        }
        serializer.endTag(null, TAG_PERSISTENT_PREFERRED_ACTIVITIES);
    }

    void writeCrossProfileIntentFiltersLPr(XmlSerializer serializer, int userId) throws IllegalArgumentException, IllegalStateException, IOException {
        serializer.startTag(null, TAG_CROSS_PROFILE_INTENT_FILTERS);
        CrossProfileIntentResolver cpir = (CrossProfileIntentResolver) this.mCrossProfileIntentResolvers.get(userId);
        if (cpir != null) {
            for (CrossProfileIntentFilter cpif : cpir.filterSet()) {
                serializer.startTag(null, TAG_ITEM);
                cpif.writeToXml(serializer);
                serializer.endTag(null, TAG_ITEM);
            }
        }
        serializer.endTag(null, TAG_CROSS_PROFILE_INTENT_FILTERS);
    }

    void writeDomainVerificationsLPr(XmlSerializer serializer, IntentFilterVerificationInfo verificationInfo) throws IllegalArgumentException, IllegalStateException, IOException {
        if (verificationInfo != null && verificationInfo.getPackageName() != null) {
            serializer.startTag(null, TAG_DOMAIN_VERIFICATION);
            verificationInfo.writeToXml(serializer);
            serializer.endTag(null, TAG_DOMAIN_VERIFICATION);
        }
    }

    void writeAllDomainVerificationsLPr(XmlSerializer serializer, int userId) throws IllegalArgumentException, IllegalStateException, IOException {
        serializer.startTag(null, TAG_ALL_INTENT_FILTER_VERIFICATION);
        int N = this.mPackages.size();
        for (int i = 0; i < N; i++) {
            IntentFilterVerificationInfo ivi = ((PackageSetting) this.mPackages.valueAt(i)).getIntentFilterVerificationInfo();
            if (ivi != null) {
                writeDomainVerificationsLPr(serializer, ivi);
            }
        }
        serializer.endTag(null, TAG_ALL_INTENT_FILTER_VERIFICATION);
    }

    void readAllDomainVerificationsLPr(XmlPullParser parser, int userId) throws XmlPullParserException, IOException {
        this.mRestoredIntentFilterVerifications.clear();
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
                if (parser.getName().equals(TAG_DOMAIN_VERIFICATION)) {
                    IntentFilterVerificationInfo ivi = new IntentFilterVerificationInfo(parser);
                    String pkgName = ivi.getPackageName();
                    PackageSetting ps = (PackageSetting) this.mPackages.get(pkgName);
                    if (ps != null) {
                        ps.setIntentFilterVerificationInfo(ivi);
                    } else {
                        this.mRestoredIntentFilterVerifications.put(pkgName, ivi);
                    }
                } else {
                    PackageManagerService.reportSettingsProblem(5, "Unknown element under <all-intent-filter-verification>: " + parser.getName());
                    XmlUtils.skipCurrentTag(parser);
                }
            }
        }
    }

    public void processRestoredPermissionGrantLPr(String pkgName, String permission, boolean isGranted, int restoredFlagSet, int userId) throws IOException, XmlPullParserException {
        this.mRuntimePermissionsPersistence.rememberRestoredUserGrantLPr(pkgName, permission, isGranted, restoredFlagSet, userId);
    }

    void writeDefaultAppsLPr(XmlSerializer serializer, int userId) throws IllegalArgumentException, IllegalStateException, IOException {
        serializer.startTag(null, TAG_DEFAULT_APPS);
        String defaultBrowser = (String) this.mDefaultBrowserApp.get(userId);
        if (!TextUtils.isEmpty(defaultBrowser)) {
            serializer.startTag(null, TAG_DEFAULT_BROWSER);
            serializer.attribute(null, ATTR_PACKAGE_NAME, defaultBrowser);
            serializer.endTag(null, TAG_DEFAULT_BROWSER);
        }
        String defaultDialer = (String) this.mDefaultDialerApp.get(userId);
        if (!TextUtils.isEmpty(defaultDialer)) {
            serializer.startTag(null, TAG_DEFAULT_DIALER);
            serializer.attribute(null, ATTR_PACKAGE_NAME, defaultDialer);
            serializer.endTag(null, TAG_DEFAULT_DIALER);
        }
        serializer.endTag(null, TAG_DEFAULT_APPS);
    }

    void writeBlockUninstallPackagesLPr(XmlSerializer serializer, int userId) throws IOException {
        ArraySet<String> packages = (ArraySet) this.mBlockUninstallPackages.get(userId);
        if (packages != null) {
            serializer.startTag(null, TAG_BLOCK_UNINSTALL_PACKAGES);
            for (int i = 0; i < packages.size(); i++) {
                serializer.startTag(null, TAG_BLOCK_UNINSTALL);
                serializer.attribute(null, ATTR_PACKAGE_NAME, (String) packages.valueAt(i));
                serializer.endTag(null, TAG_BLOCK_UNINSTALL);
            }
            serializer.endTag(null, TAG_BLOCK_UNINSTALL_PACKAGES);
        }
    }

    void writePackageRestrictionsLPr(int userId) {
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
            XmlSerializer serializer = new FastXmlSerializer();
            serializer.setOutput(str, StandardCharsets.UTF_8.name());
            serializer.startDocument(null, Boolean.valueOf(true));
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            serializer.startTag(null, TAG_PACKAGE_RESTRICTIONS);
            for (PackageSetting pkg : this.mPackages.values()) {
                PackageUserState ustate = pkg.readUserState(userId);
                serializer.startTag(null, "pkg");
                serializer.attribute(null, ATTR_NAME, pkg.name);
                if (ustate.ceDataInode != 0) {
                    XmlUtils.writeLongAttribute(serializer, ATTR_CE_DATA_INODE, ustate.ceDataInode);
                }
                if (!ustate.installed) {
                    serializer.attribute(null, ATTR_INSTALLED, "false");
                }
                if (ustate.stopped) {
                    serializer.attribute(null, ATTR_STOPPED, "true");
                }
                if (ustate.notLaunched) {
                    serializer.attribute(null, ATTR_NOT_LAUNCHED, "true");
                }
                if (ustate.hidden) {
                    serializer.attribute(null, ATTR_HIDDEN, "true");
                }
                if (ustate.suspended) {
                    serializer.attribute(null, ATTR_SUSPENDED, "true");
                }
                if (ustate.instantApp) {
                    serializer.attribute(null, ATTR_INSTANT_APP, "true");
                }
                if (ustate.enabled != 0) {
                    serializer.attribute(null, ATTR_ENABLED, Integer.toString(ustate.enabled));
                    if (ustate.lastDisableAppCaller != null) {
                        serializer.attribute(null, ATTR_ENABLED_CALLER, ustate.lastDisableAppCaller);
                    }
                }
                if (ustate.domainVerificationStatus != 0) {
                    XmlUtils.writeIntAttribute(serializer, ATTR_DOMAIN_VERIFICATON_STATE, ustate.domainVerificationStatus);
                }
                if (ustate.appLinkGeneration != 0) {
                    XmlUtils.writeIntAttribute(serializer, ATTR_APP_LINK_GENERATION, ustate.appLinkGeneration);
                }
                if (ustate.installReason != 0) {
                    serializer.attribute(null, ATTR_INSTALL_REASON, Integer.toString(ustate.installReason));
                }
                if (!ArrayUtils.isEmpty(ustate.enabledComponents)) {
                    serializer.startTag(null, TAG_ENABLED_COMPONENTS);
                    for (String name : ustate.enabledComponents) {
                        serializer.startTag(null, TAG_ITEM);
                        serializer.attribute(null, ATTR_NAME, name);
                        serializer.endTag(null, TAG_ITEM);
                    }
                    serializer.endTag(null, TAG_ENABLED_COMPONENTS);
                }
                if (!ArrayUtils.isEmpty(ustate.disabledComponents)) {
                    serializer.startTag(null, TAG_DISABLED_COMPONENTS);
                    for (String name2 : ustate.disabledComponents) {
                        serializer.startTag(null, TAG_ITEM);
                        serializer.attribute(null, ATTR_NAME, name2);
                        serializer.endTag(null, TAG_ITEM);
                    }
                    serializer.endTag(null, TAG_DISABLED_COMPONENTS);
                }
                serializer.endTag(null, "pkg");
            }
            writePreferredActivitiesLPr(serializer, userId, true);
            writePersistentPreferredActivitiesLPr(serializer, userId);
            writeCrossProfileIntentFiltersLPr(serializer, userId);
            writeDefaultAppsLPr(serializer, userId);
            writeBlockUninstallPackagesLPr(serializer, userId);
            serializer.endTag(null, TAG_PACKAGE_RESTRICTIONS);
            serializer.endDocument();
            str.flush();
            FileUtils.sync(fstr);
            str.close();
            backupFile.delete();
            FileUtils.setPermissions(userPackagesStateFile.toString(), 432, -1, -1);
        } catch (IOException e) {
            Slog.wtf("PackageManager", "Unable to write package manager user packages state,  current changes will be lost at reboot", e);
            if (userPackagesStateFile.exists() && !userPackagesStateFile.delete()) {
                Log.i("PackageManager", "Failed to clean up mangled file: " + this.mStoppedPackagesFilename);
            }
        }
    }

    void readInstallPermissionsLPr(XmlPullParser parser, PermissionsState permissionsState) throws IOException, XmlPullParserException {
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
                if (parser.getName().equals(TAG_ITEM)) {
                    String name = parser.getAttributeValue(null, ATTR_NAME);
                    BasePermission bp = (BasePermission) this.mPermissions.get(name);
                    if (bp == null) {
                        Slog.w("PackageManager", "Unknown permission: " + name);
                        XmlUtils.skipCurrentTag(parser);
                    } else {
                        boolean granted;
                        String grantedStr = parser.getAttributeValue(null, ATTR_GRANTED);
                        if (grantedStr != null) {
                            granted = Boolean.parseBoolean(grantedStr);
                        } else {
                            granted = true;
                        }
                        String flagsStr = parser.getAttributeValue(null, ATTR_FLAGS);
                        int flags = flagsStr != null ? Integer.parseInt(flagsStr, 16) : 0;
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

    void writePermissionsLPr(XmlSerializer serializer, List<PermissionState> permissionStates) throws IOException {
        if (!permissionStates.isEmpty()) {
            serializer.startTag(null, TAG_PERMISSIONS);
            for (PermissionState permissionState : permissionStates) {
                serializer.startTag(null, TAG_ITEM);
                serializer.attribute(null, ATTR_NAME, permissionState.getName());
                serializer.attribute(null, ATTR_GRANTED, String.valueOf(permissionState.isGranted()));
                serializer.attribute(null, ATTR_FLAGS, Integer.toHexString(permissionState.getFlags()));
                serializer.endTag(null, TAG_ITEM);
            }
            serializer.endTag(null, TAG_PERMISSIONS);
        }
    }

    void writeChildPackagesLPw(XmlSerializer serializer, List<String> childPackageNames) throws IOException {
        if (childPackageNames != null) {
            int childCount = childPackageNames.size();
            for (int i = 0; i < childCount; i++) {
                String childPackageName = (String) childPackageNames.get(i);
                serializer.startTag(null, TAG_CHILD_PACKAGE);
                serializer.attribute(null, ATTR_NAME, childPackageName);
                serializer.endTag(null, TAG_CHILD_PACKAGE);
            }
        }
    }

    void readUsesStaticLibLPw(XmlPullParser parser, PackageSetting outPs) throws IOException, XmlPullParserException {
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
                String libName = parser.getAttributeValue(null, ATTR_NAME);
                int libVersion = -1;
                try {
                    libVersion = Integer.parseInt(parser.getAttributeValue(null, "version"));
                } catch (NumberFormatException e) {
                }
                if (libName != null && libVersion >= 0) {
                    outPs.usesStaticLibraries = (String[]) ArrayUtils.appendElement(String.class, outPs.usesStaticLibraries, libName);
                    outPs.usesStaticLibrariesVersions = ArrayUtils.appendInt(outPs.usesStaticLibrariesVersions, libVersion);
                }
                XmlUtils.skipCurrentTag(parser);
            }
        }
    }

    void writeUsesStaticLibLPw(XmlSerializer serializer, String[] usesStaticLibraries, int[] usesStaticLibraryVersions) throws IOException {
        if (!ArrayUtils.isEmpty(usesStaticLibraries) && !ArrayUtils.isEmpty(usesStaticLibraryVersions) && usesStaticLibraries.length == usesStaticLibraryVersions.length) {
            int libCount = usesStaticLibraries.length;
            for (int i = 0; i < libCount; i++) {
                String libName = usesStaticLibraries[i];
                int libVersion = usesStaticLibraryVersions[i];
                serializer.startTag(null, TAG_USES_STATIC_LIB);
                serializer.attribute(null, ATTR_NAME, libName);
                serializer.attribute(null, "version", Integer.toString(libVersion));
                serializer.endTag(null, TAG_USES_STATIC_LIB);
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:70:0x00d3 A:{SYNTHETIC, EDGE_INSN: B:70:0x00d3->B:32:0x00d3 ?: BREAK  , EDGE_INSN: B:70:0x00d3->B:32:0x00d3 ?: BREAK  } */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x00d0 A:{Catch:{ XmlPullParserException -> 0x013e, IOException -> 0x015c }} */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x00e6 A:{Catch:{ XmlPullParserException -> 0x013e, IOException -> 0x015c }} */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x00d6 A:{Catch:{ XmlPullParserException -> 0x013e, IOException -> 0x015c }} */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x01bf  */
    /* JADX WARNING: Removed duplicated region for block: B:9:0x004a A:{SYNTHETIC, Splitter: B:9:0x004a} */
    /* JADX WARNING: Removed duplicated region for block: B:9:0x004a A:{SYNTHETIC, Splitter: B:9:0x004a} */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x01bf  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void readStoppedLPw() {
        FileInputStream str;
        XmlPullParserException e;
        IOException e2;
        FileInputStream str2 = null;
        if (this.mBackupStoppedPackagesFilename.exists()) {
            try {
                str = new FileInputStream(this.mBackupStoppedPackagesFilename);
                try {
                    this.mReadMessages.append("Reading from backup stopped packages file\n");
                    PackageManagerService.reportSettingsProblem(4, "Need to read from backup stopped packages file");
                    if (this.mSettingsFilename.exists()) {
                        Slog.w("PackageManager", "Cleaning up stopped packages file " + this.mStoppedPackagesFilename);
                        this.mStoppedPackagesFilename.delete();
                    }
                } catch (IOException e3) {
                    str2 = str;
                    str = str2;
                    if (str == null) {
                    }
                }
            } catch (IOException e4) {
                str = str2;
                if (str == null) {
                }
            }
        }
        str = null;
        XmlPullParser parser;
        int type;
        if (str == null) {
            try {
                if (this.mStoppedPackagesFilename.exists()) {
                    str2 = new FileInputStream(this.mStoppedPackagesFilename);
                    try {
                        parser = Xml.newPullParser();
                        parser.setInput(str2, null);
                        do {
                            type = parser.next();
                            if (type != 2) {
                                break;
                            }
                        } while (type != 1);
                        if (type == 2) {
                            this.mReadMessages.append("No start tag found in stopped packages file\n");
                            PackageManagerService.reportSettingsProblem(5, "No start tag found in package manager stopped packages");
                            return;
                        }
                        int outerDepth = parser.getDepth();
                        while (true) {
                            type = parser.next();
                            if (type == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                                str2.close();
                            } else if (!(type == 3 || type == 4)) {
                                if (parser.getName().equals("pkg")) {
                                    String name = parser.getAttributeValue(null, ATTR_NAME);
                                    PackageSetting ps = (PackageSetting) this.mPackages.get(name);
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
                        str2.close();
                    } catch (XmlPullParserException e5) {
                        e = e5;
                        this.mReadMessages.append("Error reading: ").append(e.toString());
                        PackageManagerService.reportSettingsProblem(6, "Error reading stopped packages: " + e);
                        Slog.wtf("PackageManager", "Error reading package manager stopped packages", e);
                    } catch (IOException e6) {
                        e2 = e6;
                        this.mReadMessages.append("Error reading: ").append(e2.toString());
                        PackageManagerService.reportSettingsProblem(6, "Error reading settings: " + e2);
                        Slog.wtf("PackageManager", "Error reading package manager stopped packages", e2);
                    }
                }
                this.mReadMessages.append("No stopped packages file found\n");
                PackageManagerService.reportSettingsProblem(4, "No stopped packages file file; assuming all started");
                for (PackageSetting pkg : this.mPackages.values()) {
                    pkg.setStopped(false, 0);
                    pkg.setNotLaunched(false, 0);
                }
                return;
            } catch (XmlPullParserException e7) {
                e = e7;
                this.mReadMessages.append("Error reading: ").append(e.toString());
                PackageManagerService.reportSettingsProblem(6, "Error reading stopped packages: " + e);
                Slog.wtf("PackageManager", "Error reading package manager stopped packages", e);
            } catch (IOException e8) {
                e2 = e8;
                this.mReadMessages.append("Error reading: ").append(e2.toString());
                PackageManagerService.reportSettingsProblem(6, "Error reading settings: " + e2);
                Slog.wtf("PackageManager", "Error reading package manager stopped packages", e2);
            }
        }
        str2 = str;
        parser = Xml.newPullParser();
        parser.setInput(str2, null);
        do {
            type = parser.next();
            if (type != 2) {
            }
        } while (type != 1);
        if (type == 2) {
        }
    }

    void writeLPr() {
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
            int i;
            FileOutputStream fstr = new FileOutputStream(this.mSettingsFilename);
            OutputStream bufferedOutputStream = new BufferedOutputStream(fstr);
            XmlSerializer serializer = new FastXmlSerializer();
            serializer.setOutput(bufferedOutputStream, StandardCharsets.UTF_8.name());
            serializer.startDocument(null, Boolean.valueOf(true));
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            serializer.startTag(null, "packages");
            for (i = 0; i < this.mVersion.size(); i++) {
                String volumeUuid = (String) this.mVersion.keyAt(i);
                VersionInfo ver = (VersionInfo) this.mVersion.valueAt(i);
                serializer.startTag(null, "version");
                XmlUtils.writeStringAttribute(serializer, ATTR_VOLUME_UUID, volumeUuid);
                XmlUtils.writeIntAttribute(serializer, ATTR_SDK_VERSION, ver.sdkVersion);
                XmlUtils.writeIntAttribute(serializer, ATTR_DATABASE_VERSION, ver.databaseVersion);
                XmlUtils.writeStringAttribute(serializer, ATTR_FINGERPRINT, ver.fingerprint);
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
            for (BasePermission bp : this.mPermissionTrees.values()) {
                writePermissionLPr(serializer, bp);
            }
            serializer.endTag(null, "permission-trees");
            serializer.startTag(null, "permissions");
            for (BasePermission bp2 : this.mPermissions.values()) {
                writePermissionLPr(serializer, bp2);
            }
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
                for (PackageCleanItem item : this.mPackagesToBeCleaned) {
                    String userStr = Integer.toString(item.userId);
                    serializer.startTag(null, "cleaning-package");
                    serializer.attribute(null, ATTR_NAME, item.packageName);
                    serializer.attribute(null, ATTR_CODE, item.andCode ? "true" : "false");
                    serializer.attribute(null, ATTR_USER, userStr);
                    serializer.endTag(null, "cleaning-package");
                }
            }
            if (this.mRenamedPackages.size() > 0) {
                for (Entry<String, String> e : this.mRenamedPackages.entrySet()) {
                    serializer.startTag(null, "renamed-package");
                    serializer.attribute(null, "new", (String) e.getKey());
                    serializer.attribute(null, "old", (String) e.getValue());
                    serializer.endTag(null, "renamed-package");
                }
            }
            int numIVIs = this.mRestoredIntentFilterVerifications.size();
            if (numIVIs > 0) {
                serializer.startTag(null, "restored-ivi");
                for (i = 0; i < numIVIs; i++) {
                    writeDomainVerificationsLPr(serializer, (IntentFilterVerificationInfo) this.mRestoredIntentFilterVerifications.valueAt(i));
                }
                serializer.endTag(null, "restored-ivi");
            }
            this.mKeySetManagerService.writeKeySetManagerServiceLPr(serializer);
            serializer.endTag(null, "packages");
            serializer.endDocument();
            bufferedOutputStream.flush();
            FileUtils.sync(fstr);
            bufferedOutputStream.close();
            this.mBackupSettingsFilename.delete();
            FileUtils.setPermissions(this.mSettingsFilename.toString(), 432, -1, -1);
            writeKernelMappingLPr();
            writePackageListLPr();
            writeAllUsersPackageRestrictionsLPr();
            writeAllRuntimePermissionsLPr();
        } catch (XmlPullParserException e2) {
            Slog.wtf("PackageManager", "Unable to write package manager settings, current changes will be lost at reboot", e2);
            if (this.mSettingsFilename.exists() && !this.mSettingsFilename.delete()) {
                Slog.wtf("PackageManager", "Failed to clean up mangled file: " + this.mSettingsFilename);
            }
        } catch (IOException e3) {
            Slog.wtf("PackageManager", "Unable to write package manager settings, current changes will be lost at reboot", e3);
            Slog.wtf("PackageManager", "Failed to clean up mangled file: " + this.mSettingsFilename);
        }
    }

    private void writeKernelRemoveUserLPr(int userId) {
        if (this.mKernelMappingFilename != null) {
            writeIntToFile(new File(this.mKernelMappingFilename, "remove_userid"), userId);
        }
    }

    void writeKernelMappingLPr() {
        if (this.mKernelMappingFilename != null) {
            String name;
            String[] known = this.mKernelMappingFilename.list();
            ArraySet<String> knownSet = new ArraySet(known.length);
            for (String name2 : known) {
                knownSet.add(name2);
            }
            for (PackageSetting ps : this.mPackages.values()) {
                knownSet.remove(ps.name);
                writeKernelMappingLPr(ps);
            }
            for (int i = 0; i < knownSet.size(); i++) {
                name2 = (String) knownSet.valueAt(i);
                this.mKernelMapping.remove(name2);
                new File(this.mKernelMappingFilename, name2).delete();
            }
        }
    }

    void writeKernelMappingLPr(PackageSetting ps) {
        if (this.mKernelMappingFilename != null && ps != null && ps.name != null) {
            int userIdsChanged;
            KernelPackageState cur = (KernelPackageState) this.mKernelMapping.get(ps.name);
            boolean firstTime = cur == null;
            int[] excludedUserIds = ps.getNotInstalledUserIds();
            if (firstTime) {
                userIdsChanged = 1;
            } else {
                userIdsChanged = Arrays.equals(excludedUserIds, cur.excludedUserIds) ^ 1;
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
            if (userIdsChanged != 0) {
                int i = 0;
                while (i < excludedUserIds.length) {
                    if (cur.excludedUserIds == null || (ArrayUtils.contains(cur.excludedUserIds, excludedUserIds[i]) ^ 1) != 0) {
                        writeIntToFile(new File(dir, "excluded_userids"), excludedUserIds[i]);
                    }
                    i++;
                }
                if (cur.excludedUserIds != null) {
                    for (i = 0; i < cur.excludedUserIds.length; i++) {
                        if (!ArrayUtils.contains(excludedUserIds, cur.excludedUserIds[i])) {
                            writeIntToFile(new File(dir, "clear_userid"), cur.excludedUserIds[i]);
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

    void writePackageListLPr() {
        writePackageListLPr(-1);
    }

    void writePackageListLPr(int creatingUserId) {
        int i;
        Exception e;
        List<UserInfo> users = UserManagerService.getInstance().getUsers(true);
        int[] userIds = new int[users.size()];
        for (i = 0; i < userIds.length; i++) {
            userIds[i] = ((UserInfo) users.get(i)).id;
        }
        if (creatingUserId != -1) {
            userIds = ArrayUtils.appendInt(userIds, creatingUserId);
        }
        JournaledFile journal = new JournaledFile(this.mPackageListFilename, new File(this.mPackageListFilename.getAbsolutePath() + ".tmp"));
        AutoCloseable autoCloseable = null;
        try {
            FileOutputStream fstr = new FileOutputStream(journal.chooseForWrite());
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fstr, Charset.defaultCharset()));
            try {
                FileUtils.setPermissions(fstr.getFD(), 416, 1000, 1032);
                StringBuilder sb = new StringBuilder();
                for (PackageSetting pkg : this.mPackages.values()) {
                    if (!(pkg.pkg == null || pkg.pkg.applicationInfo == null)) {
                        if (pkg.pkg.applicationInfo.dataDir != null) {
                            ApplicationInfo ai = pkg.pkg.applicationInfo;
                            String dataPath = ai.dataDir;
                            boolean isDebug = (ai.flags & 2) != 0;
                            int[] gids = pkg.getPermissionsState().computeGids(userIds);
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
                                    for (i = 1; i < gids.length; i++) {
                                        sb.append(",");
                                        sb.append(gids[i]);
                                    }
                                }
                                sb.append("\n");
                                writer.append(sb);
                            }
                        }
                    }
                    if (!"android".equals(pkg.name)) {
                        Slog.w(TAG, "Skipping " + pkg + " due to missing metadata");
                    }
                }
                writer.flush();
                FileUtils.sync(fstr);
                writer.close();
                journal.commit();
            } catch (Exception e2) {
                e = e2;
                autoCloseable = writer;
                Slog.wtf(TAG, "Failed to write packages.list", e);
                IoUtils.closeQuietly(autoCloseable);
                journal.rollback();
            }
        } catch (Exception e3) {
            e = e3;
            Slog.wtf(TAG, "Failed to write packages.list", e);
            IoUtils.closeQuietly(autoCloseable);
            journal.rollback();
        }
    }

    void writeDisabledSysPackageLPr(XmlSerializer serializer, PackageSetting pkg) throws IOException {
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

    void writePackageLPr(XmlSerializer serializer, PackageSetting pkg) throws IOException {
        serializer.startTag(null, HwBroadcastRadarUtil.KEY_PACKAGE);
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
        if (pkg.installStatus == 0) {
            serializer.attribute(null, "installStatus", "false");
        }
        if (pkg.installerPackageName != null) {
            serializer.attribute(null, "installer", pkg.installerPackageName);
        }
        if (pkg.maxAspectRatio > 0.0f) {
            serializer.attribute(null, "maxAspectRatio", Float.toString(pkg.maxAspectRatio));
        }
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
        if (pkg.pkg == null || pkg.pkg.mRealSignatures == null || pkg.pkg.mRealSignatures.length == 0) {
            pkg.signatures.writeXml(serializer, "sigs", this.mPastSignatures);
        } else {
            new PackageSignatures(pkg.pkg.mRealSignatures).writeXml(serializer, "sigs", this.mPastSignatures);
        }
        writePermissionsLPr(serializer, pkg.getPermissionsState().getInstallPermissionStates());
        writeSigningKeySetLPr(serializer, pkg.keySetData);
        writeUpgradeKeySetsLPr(serializer, pkg.keySetData);
        writeKeySetAliasesLPr(serializer, pkg.keySetData);
        writeDomainVerificationsLPr(serializer, pkg.verificationInfo);
        serializer.endTag(null, HwBroadcastRadarUtil.KEY_PACKAGE);
    }

    void writeSigningKeySetLPr(XmlSerializer serializer, PackageKeySetData data) throws IOException {
        serializer.startTag(null, "proper-signing-keyset");
        serializer.attribute(null, "identifier", Long.toString(data.getProperSigningKeySet()));
        serializer.endTag(null, "proper-signing-keyset");
    }

    void writeUpgradeKeySetsLPr(XmlSerializer serializer, PackageKeySetData data) throws IOException {
        long properSigning = data.getProperSigningKeySet();
        if (data.isUsingUpgradeKeySets()) {
            for (long id : data.getUpgradeKeySets()) {
                serializer.startTag(null, "upgrade-keyset");
                serializer.attribute(null, "identifier", Long.toString(id));
                serializer.endTag(null, "upgrade-keyset");
            }
        }
    }

    void writeKeySetAliasesLPr(XmlSerializer serializer, PackageKeySetData data) throws IOException {
        for (Entry<String, Long> e : data.getAliases().entrySet()) {
            serializer.startTag(null, "defined-keyset");
            serializer.attribute(null, "alias", (String) e.getKey());
            serializer.attribute(null, "identifier", Long.toString(((Long) e.getValue()).longValue()));
            serializer.endTag(null, "defined-keyset");
        }
    }

    void writePermissionLPr(XmlSerializer serializer, BasePermission bp) throws XmlPullParserException, IOException {
        if (bp.sourcePackage != null) {
            serializer.startTag(null, TAG_ITEM);
            serializer.attribute(null, ATTR_NAME, bp.name);
            serializer.attribute(null, HwBroadcastRadarUtil.KEY_PACKAGE, bp.sourcePackage);
            if (bp.protectionLevel != 0) {
                serializer.attribute(null, "protection", Integer.toString(bp.protectionLevel));
            }
            if (bp.type == 2) {
                PermissionInfo pi = bp.perm != null ? bp.perm.info : bp.pendingInfo;
                if (pi != null) {
                    serializer.attribute(null, SoundModelContract.KEY_TYPE, "dynamic");
                    if (pi.icon != 0) {
                        serializer.attribute(null, "icon", Integer.toString(pi.icon));
                    }
                    if (pi.nonLocalizedLabel != null) {
                        serializer.attribute(null, "label", pi.nonLocalizedLabel.toString());
                    }
                }
            }
            serializer.endTag(null, TAG_ITEM);
        }
    }

    ArrayList<PackageSetting> getListOfIncompleteInstallPackagesLPr() {
        Iterator<String> its = new ArraySet(this.mPackages.keySet()).iterator();
        ArrayList<PackageSetting> ret = new ArrayList();
        while (its.hasNext()) {
            PackageSetting ps = (PackageSetting) this.mPackages.get((String) its.next());
            if (ps.getInstallStatus() == 0) {
                ret.add(ps);
            }
        }
        return ret;
    }

    void addPackageToCleanLPw(PackageCleanItem pkg) {
        if (!this.mPackagesToBeCleaned.contains(pkg)) {
            this.mPackagesToBeCleaned.add(pkg);
        }
    }

    boolean readLPw(List<UserInfo> users) {
        int type;
        InputStream str = null;
        if (this.mBackupSettingsFilename.exists()) {
            try {
                InputStream fileInputStream = new FileInputStream(this.mBackupSettingsFilename);
                try {
                    this.mReadMessages.append("Reading from backup settings file\n");
                    PackageManagerService.reportSettingsProblem(4, "Need to read from backup settings file");
                    if (this.mSettingsFilename.exists()) {
                        Slog.w("PackageManager", "Cleaning up settings file " + this.mSettingsFilename);
                        this.mSettingsFilename.delete();
                    }
                    str = fileInputStream;
                } catch (IOException e) {
                    str = fileInputStream;
                }
            } catch (IOException e2) {
            }
        }
        this.mPendingPackages.clear();
        this.mPastSignatures.clear();
        this.mKeySetRefs.clear();
        this.mInstallerPackages.clear();
        if (str == null) {
            try {
                if (this.mSettingsFilename.exists()) {
                    str = new FileInputStream(this.mSettingsFilename);
                } else {
                    this.mReadMessages.append("No settings file found\n");
                    PackageManagerService.reportSettingsProblem(4, "No settings file; creating initial state");
                    findOrCreateVersion(StorageManager.UUID_PRIVATE_INTERNAL).forceCurrent();
                    findOrCreateVersion("primary_physical").forceCurrent();
                    return false;
                }
            } catch (Throwable e3) {
                this.mReadMessages.append("Error reading: ").append(e3.toString());
                PackageManagerService.reportSettingsProblem(6, "Error reading settings: " + e3);
                Slog.e("PackageManager", "Error reading package manager settings", e3);
                HwBootFail.brokenFileBootFail(83886084, "/data/system/packages.xml", new Throwable());
                this.mPendingPackages.clear();
                this.mPastSignatures.clear();
                this.mKeySetRefs.clear();
                this.mUserIds.clear();
                this.mSharedUsers.clear();
                addSharedUserLPw("android.uid.system", 1000, 1, 8);
                addSharedUserLPw("android.uid.phone", NetworkAgentInfo.EVENT_NETWORK_LINGER_COMPLETE, 1, 8);
                addSharedUserLPw("android.uid.log", 1007, 1, 8);
                addSharedUserLPw("android.uid.nfc", 1027, 1, 8);
                addSharedUserLPw("com.nxp.uid.nfceeapi", 1054, 1, 8);
                addSharedUserLPw("android.uid.bluetooth", 1002, 1, 8);
                addSharedUserLPw("android.uid.shell", IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME, 1, 8);
                findOrCreateVersion(StorageManager.UUID_PRIVATE_INTERNAL);
                findOrCreateVersion("primary_physical");
                return false;
            } catch (IOException e4) {
                this.mReadMessages.append("Error reading: ").append(e4.toString());
                PackageManagerService.reportSettingsProblem(6, "Error reading settings: " + e4);
                Slog.wtf("PackageManager", "Error reading package manager settings", e4);
            } catch (Exception e5) {
                this.mReadMessages.append("Error reading: ").append(e5.toString());
                PackageManagerService.reportSettingsProblem(6, "Error reading settings: " + e5);
                Log.wtf("PackageManager", "Error reading package manager settings", e5);
            }
        }
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(str, StandardCharsets.UTF_8.name());
        do {
            type = parser.next();
            if (type == 2) {
                break;
            }
        } while (type != 1);
        if (type != 2) {
            this.mReadMessages.append("No start tag found in settings file\n");
            PackageManagerService.reportSettingsProblem(5, "No start tag found in package manager settings");
            Slog.wtf("PackageManager", "No start tag found in package manager settings");
            return false;
        }
        int outerDepth = parser.getDepth();
        while (true) {
            type = parser.next();
            if (type == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                str.close();
            } else if (!(type == 3 || type == 4)) {
                String tagName = parser.getName();
                if (tagName.equals(HwBroadcastRadarUtil.KEY_PACKAGE)) {
                    readPackageLPw(parser);
                } else if (tagName.equals("permissions")) {
                    readPermissionsLPw(this.mPermissions, parser);
                } else if (tagName.equals("permission-trees")) {
                    readPermissionsLPw(this.mPermissionTrees, parser);
                } else if (tagName.equals(TAG_SHARED_USER)) {
                    readSharedUserLPw(parser);
                } else if (!tagName.equals("preferred-packages")) {
                    VersionInfo internal;
                    VersionInfo external;
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
                                } catch (NumberFormatException e6) {
                                }
                            }
                            if (codeStr != null) {
                                andCode = Boolean.parseBoolean(codeStr);
                            }
                            addPackageToCleanLPw(new PackageCleanItem(userId, name, andCode));
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
                        internal = findOrCreateVersion(StorageManager.UUID_PRIVATE_INTERNAL);
                        external = findOrCreateVersion("primary_physical");
                        internal.sdkVersion = XmlUtils.readIntAttribute(parser, "internal", 0);
                        external.sdkVersion = XmlUtils.readIntAttribute(parser, "external", 0);
                        String readStringAttribute = XmlUtils.readStringAttribute(parser, ATTR_FINGERPRINT);
                        external.fingerprint = readStringAttribute;
                        internal.fingerprint = readStringAttribute;
                    } else if (tagName.equals("database-version")) {
                        internal = findOrCreateVersion(StorageManager.UUID_PRIVATE_INTERNAL);
                        external = findOrCreateVersion("primary_physical");
                        internal.databaseVersion = XmlUtils.readIntAttribute(parser, "internal", 0);
                        external.databaseVersion = XmlUtils.readIntAttribute(parser, "external", 0);
                    } else if (tagName.equals("verifier")) {
                        try {
                            this.mVerifierDeviceIdentity = VerifierDeviceIdentity.parse(parser.getAttributeValue(null, "device"));
                        } catch (IllegalArgumentException e7) {
                            Slog.w("PackageManager", "Discard invalid verifier device id: " + e7.getMessage());
                        }
                    } else if (TAG_READ_EXTERNAL_STORAGE.equals(tagName)) {
                        this.mReadExternalStorageEnforced = Boolean.valueOf("1".equals(parser.getAttributeValue(null, ATTR_ENFORCEMENT)));
                    } else if (tagName.equals("keyset-settings")) {
                        this.mKeySetManagerService.readKeySetsLPw(parser, this.mKeySetRefs);
                    } else if ("version".equals(tagName)) {
                        VersionInfo ver = findOrCreateVersion(XmlUtils.readStringAttribute(parser, ATTR_VOLUME_UUID));
                        ver.sdkVersion = XmlUtils.readIntAttribute(parser, ATTR_SDK_VERSION);
                        ver.databaseVersion = XmlUtils.readIntAttribute(parser, ATTR_SDK_VERSION);
                        ver.fingerprint = XmlUtils.readStringAttribute(parser, ATTR_FINGERPRINT);
                    } else {
                        Slog.w("PackageManager", "Unknown element under <packages>: " + parser.getName());
                        XmlUtils.skipCurrentTag(parser);
                    }
                }
            }
        }
        str.close();
        int N = this.mPendingPackages.size();
        for (int i = 0; i < N; i++) {
            PackageSetting p = (PackageSetting) this.mPendingPackages.get(i);
            int sharedUserId = p.getSharedUserId();
            SharedUserSetting idObj = getUserIdLPr(sharedUserId);
            String msg;
            if (idObj instanceof SharedUserSetting) {
                SharedUserSetting sharedUser = idObj;
                p.sharedUser = sharedUser;
                p.appId = sharedUser.userId;
                addPackageSettingLPw(p, sharedUser);
            } else if (idObj != null) {
                msg = "Bad package setting: package " + p.name + " has shared uid " + sharedUserId + " that is not a shared uid\n";
                this.mReadMessages.append(msg);
                PackageManagerService.reportSettingsProblem(6, msg);
            } else {
                msg = "Bad package setting: package " + p.name + " has shared uid " + sharedUserId + " that is not defined\n";
                this.mReadMessages.append(msg);
                PackageManagerService.reportSettingsProblem(6, msg);
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
        } catch (IllegalStateException e8) {
            HwBootFail.brokenFileBootFail(83886085, "/data/system/users/0/runtime-permissions.xml", new Throwable());
            Log.wtf("PackageManager", "Error reading state for user", e8);
        }
        for (PackageSetting disabledPs : this.mDisabledSysPackages.values()) {
            Object id = getUserIdLPr(disabledPs.appId);
            if (id != null && (id instanceof SharedUserSetting)) {
                disabledPs.sharedUser = (SharedUserSetting) id;
            }
        }
        this.mReadMessages.append("Read completed successfully: ").append(this.mPackages.size()).append(" packages, ").append(this.mSharedUsers.size()).append(" shared uids\n");
        writeKernelMappingLPr();
        return true;
    }

    /* JADX WARNING: Removed duplicated region for block: B:105:0x0105 A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:73:0x0230 A:{SYNTHETIC, Splitter: B:73:0x0230} */
    /* JADX WARNING: Removed duplicated region for block: B:104:0x0105 A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x0205 A:{SYNTHETIC, Splitter: B:66:0x0205} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void applyDefaultPreferredAppsLPw(PackageManagerService service, int userId) {
        InputStream str;
        XmlPullParserException e;
        IOException e2;
        Throwable th;
        for (PackageSetting ps : this.mPackages.values()) {
            if (!((ps.pkgFlags & 1) == 0 || ps.pkg == null || ps.pkg.preferredActivityFilters == null)) {
                ArrayList<ActivityIntentInfo> intents = ps.pkg.preferredActivityFilters;
                for (int i = 0; i < intents.size(); i++) {
                    ActivityIntentInfo aii = (ActivityIntentInfo) intents.get(i);
                    applyDefaultPreferredActivityLPw(service, aii, new ComponentName(ps.name, aii.activity.className), userId);
                }
            }
        }
        File preferredDir = new File(Environment.getRootDirectory(), "etc/preferred-apps");
        if (!preferredDir.exists() || (preferredDir.isDirectory() ^ 1) != 0) {
            return;
        }
        if (preferredDir.canRead()) {
            for (File f : preferredDir.listFiles()) {
                if (!f.getPath().endsWith(".xml")) {
                    Slog.i(TAG, "Non-xml file " + f + " in " + preferredDir + " directory, ignoring");
                } else if (f.canRead()) {
                    str = null;
                    try {
                        InputStream str2 = new BufferedInputStream(new FileInputStream(f));
                        try {
                            int type;
                            XmlPullParser parser = Xml.newPullParser();
                            parser.setInput(str2, null);
                            do {
                                type = parser.next();
                                if (type == 2) {
                                    break;
                                }
                            } while (type != 1);
                            if (type != 2) {
                                Slog.w(TAG, "Preferred apps file " + f + " does not have start tag");
                                if (str2 != null) {
                                    try {
                                        str2.close();
                                    } catch (IOException e3) {
                                    }
                                }
                            } else if ("preferred-activities".equals(parser.getName())) {
                                readDefaultPreferredActivitiesLPw(service, parser, userId);
                                if (str2 != null) {
                                    try {
                                        str2.close();
                                    } catch (IOException e4) {
                                    }
                                }
                            } else {
                                Slog.w(TAG, "Preferred apps file " + f + " does not start with 'preferred-activities'");
                                if (str2 != null) {
                                    try {
                                        str2.close();
                                    } catch (IOException e5) {
                                    }
                                }
                            }
                        } catch (XmlPullParserException e6) {
                            e = e6;
                            str = str2;
                            Slog.w(TAG, "Error reading apps file " + f, e);
                            if (str == null) {
                            }
                        } catch (IOException e7) {
                            e2 = e7;
                            str = str2;
                            try {
                                Slog.w(TAG, "Error reading apps file " + f, e2);
                                if (str == null) {
                                }
                            } catch (Throwable th2) {
                                th = th2;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            str = str2;
                        }
                    } catch (XmlPullParserException e8) {
                        e = e8;
                        Slog.w(TAG, "Error reading apps file " + f, e);
                        if (str == null) {
                            try {
                                str.close();
                            } catch (IOException e9) {
                            }
                        }
                    } catch (IOException e10) {
                        e2 = e10;
                        Slog.w(TAG, "Error reading apps file " + f, e2);
                        if (str == null) {
                            try {
                                str.close();
                            } catch (IOException e11) {
                            }
                        }
                    }
                } else {
                    Slog.w(TAG, "Preferred apps file " + f + " cannot be read");
                }
            }
            return;
        }
        Slog.w(TAG, "Directory " + preferredDir + " cannot be read");
        return;
        if (str != null) {
            try {
                str.close();
            } catch (IOException e12) {
            }
        }
        throw th;
        throw th;
    }

    private void applyDefaultPreferredActivityLPw(PackageManagerService service, IntentFilter tmpPa, ComponentName cn, int userId) {
        int ischeme;
        String scheme;
        Builder builder;
        Intent finalIntent;
        Intent intent = new Intent();
        int flags = 786432;
        intent.setAction(tmpPa.getAction(0));
        for (int i = 0; i < tmpPa.countCategories(); i++) {
            String cat = tmpPa.getCategory(i);
            if (cat.equals("android.intent.category.DEFAULT")) {
                flags |= 65536;
            } else {
                intent.addCategory(cat);
            }
        }
        boolean doNonData = true;
        boolean hasSchemes = false;
        for (ischeme = 0; ischeme < tmpPa.countDataSchemes(); ischeme++) {
            boolean doScheme = true;
            scheme = tmpPa.getDataScheme(ischeme);
            if (!(scheme == null || (scheme.isEmpty() ^ 1) == 0)) {
                hasSchemes = true;
            }
            for (int issp = 0; issp < tmpPa.countDataSchemeSpecificParts(); issp++) {
                builder = new Builder();
                builder.scheme(scheme);
                PatternMatcher ssp = tmpPa.getDataSchemeSpecificPart(issp);
                builder.opaquePart(ssp.getPath());
                finalIntent = new Intent(intent);
                finalIntent.setData(builder.build());
                applyDefaultPreferredActivityLPw(service, finalIntent, flags, cn, scheme, ssp, null, null, userId);
                doScheme = false;
            }
            for (int iauth = 0; iauth < tmpPa.countDataAuthorities(); iauth++) {
                boolean doAuth = true;
                AuthorityEntry auth = tmpPa.getDataAuthority(iauth);
                for (int ipath = 0; ipath < tmpPa.countDataPaths(); ipath++) {
                    builder = new Builder();
                    builder.scheme(scheme);
                    if (auth.getHost() != null) {
                        builder.authority(auth.getHost());
                    }
                    PatternMatcher path = tmpPa.getDataPath(ipath);
                    builder.path(path.getPath());
                    finalIntent = new Intent(intent);
                    finalIntent.setData(builder.build());
                    applyDefaultPreferredActivityLPw(service, finalIntent, flags, cn, scheme, null, auth, path, userId);
                    doScheme = false;
                    doAuth = false;
                }
                if (doAuth) {
                    builder = new Builder();
                    builder.scheme(scheme);
                    if (auth.getHost() != null) {
                        builder.authority(auth.getHost());
                    }
                    finalIntent = new Intent(intent);
                    finalIntent.setData(builder.build());
                    applyDefaultPreferredActivityLPw(service, finalIntent, flags, cn, scheme, null, auth, null, userId);
                    doScheme = false;
                }
            }
            if (doScheme) {
                builder = new Builder();
                builder.scheme(scheme);
                finalIntent = new Intent(intent);
                finalIntent.setData(builder.build());
                applyDefaultPreferredActivityLPw(service, finalIntent, flags, cn, scheme, null, null, null, userId);
            }
            doNonData = false;
        }
        for (int idata = 0; idata < tmpPa.countDataTypes(); idata++) {
            String mimeType = tmpPa.getDataType(idata);
            if (hasSchemes) {
                builder = new Builder();
                for (ischeme = 0; ischeme < tmpPa.countDataSchemes(); ischeme++) {
                    scheme = tmpPa.getDataScheme(ischeme);
                    if (!(scheme == null || (scheme.isEmpty() ^ 1) == 0)) {
                        finalIntent = new Intent(intent);
                        builder.scheme(scheme);
                        finalIntent.setDataAndType(builder.build(), mimeType);
                        applyDefaultPreferredActivityLPw(service, finalIntent, flags, cn, scheme, null, null, null, userId);
                    }
                }
            } else {
                finalIntent = new Intent(intent);
                finalIntent.setType(mimeType);
                applyDefaultPreferredActivityLPw(service, finalIntent, flags, cn, null, null, null, null, userId);
            }
            doNonData = false;
        }
        if (doNonData) {
            applyDefaultPreferredActivityLPw(service, intent, flags, cn, null, null, null, null, userId);
        }
    }

    private void applyDefaultPreferredActivityLPw(PackageManagerService service, Intent intent, int flags, ComponentName cn, String scheme, PatternMatcher ssp, AuthorityEntry auth, PatternMatcher path, int userId) {
        flags = service.updateFlagsForResolve(flags, userId, intent, Binder.getCallingUid(), false);
        List<ResolveInfo> ri = service.mActivities.queryIntent(intent, intent.getType(), flags, 0);
        int systemMatch = 0;
        if (ri == null || ri.size() <= 1) {
            Slog.w(TAG, "No potential matches found for " + intent + " while setting preferred " + cn.flattenToShortString());
            return;
        }
        int i;
        boolean haveAct = false;
        ComponentName haveNonSys = null;
        ComponentName[] set = new ComponentName[ri.size()];
        for (i = 0; i < ri.size(); i++) {
            ActivityInfo ai = ((ResolveInfo) ri.get(i)).activityInfo;
            set[i] = new ComponentName(ai.packageName, ai.name);
            if ((ai.applicationInfo.flags & 1) == 0) {
                if (((ResolveInfo) ri.get(i)).match >= 0) {
                    haveNonSys = set[i];
                    break;
                }
            } else if (cn.getPackageName().equals(ai.packageName) && cn.getClassName().equals(ai.name)) {
                haveAct = true;
                systemMatch = ((ResolveInfo) ri.get(i)).match;
            }
        }
        if (haveNonSys != null && systemMatch > 0) {
            haveNonSys = null;
        }
        if (haveAct && haveNonSys == null) {
            IntentFilter filter = new IntentFilter();
            if (intent.getAction() != null) {
                filter.addAction(intent.getAction());
            }
            if (intent.getCategories() != null) {
                for (String cat : intent.getCategories()) {
                    filter.addCategory(cat);
                }
            }
            if ((65536 & flags) != 0) {
                filter.addCategory("android.intent.category.DEFAULT");
            }
            if (scheme != null) {
                filter.addDataScheme(scheme);
            }
            if (ssp != null) {
                filter.addDataSchemeSpecificPart(ssp.getPath(), ssp.getType());
            }
            if (auth != null) {
                filter.addDataAuthority(auth);
            }
            if (path != null) {
                filter.addDataPath(path);
            }
            if (intent.getType() != null) {
                try {
                    filter.addDataType(intent.getType());
                } catch (MalformedMimeTypeException e) {
                    Slog.w(TAG, "Malformed mimetype " + intent.getType() + " for " + cn);
                }
            }
            editPreferredActivitiesLPw(userId).addFilter(new PreferredActivity(filter, systemMatch, set, cn, true));
        } else if (haveNonSys == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("No component ");
            sb.append(cn.flattenToShortString());
            sb.append(" found setting preferred ");
            sb.append(intent);
            sb.append("; possible matches are ");
            for (i = 0; i < set.length; i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(set[i].flattenToShortString());
            }
            Slog.w(TAG, sb.toString());
        } else {
            Slog.i(TAG, "Not setting preferred " + intent + "; found third party match " + haveNonSys.flattenToShortString());
        }
    }

    private void readDefaultPreferredActivitiesLPw(PackageManagerService service, XmlPullParser parser, int userId) throws XmlPullParserException, IOException {
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

    private int readInt(XmlPullParser parser, String ns, String name, int defValue) {
        String v = parser.getAttributeValue(ns, name);
        if (v == null) {
            return defValue;
        }
        try {
            return Integer.parseInt(v);
        } catch (NumberFormatException e) {
            PackageManagerService.reportSettingsProblem(5, "Error in package manager settings: attribute " + name + " has bad integer value " + v + " at " + parser.getPositionDescription());
            return defValue;
        }
    }

    private void readPermissionsLPw(ArrayMap<String, BasePermission> out, XmlPullParser parser) throws IOException, XmlPullParserException {
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
                if (parser.getName().equals(TAG_ITEM)) {
                    String name = parser.getAttributeValue(null, ATTR_NAME);
                    String sourcePackage = parser.getAttributeValue(null, HwBroadcastRadarUtil.KEY_PACKAGE);
                    String ptype = parser.getAttributeValue(null, SoundModelContract.KEY_TYPE);
                    if (name == null || sourcePackage == null) {
                        PackageManagerService.reportSettingsProblem(5, "Error in package manager settings: permissions has no name at " + parser.getPositionDescription());
                    } else {
                        boolean dynamic = "dynamic".equals(ptype);
                        BasePermission bp = (BasePermission) out.get(name);
                        if (bp == null || bp.type != 1) {
                            bp = new BasePermission(name.intern(), sourcePackage, dynamic ? 2 : 0);
                        }
                        bp.protectionLevel = readInt(parser, null, "protection", 0);
                        bp.protectionLevel = PermissionInfo.fixProtectionLevel(bp.protectionLevel);
                        if (dynamic) {
                            PermissionInfo pi = new PermissionInfo();
                            pi.packageName = sourcePackage.intern();
                            pi.name = name.intern();
                            pi.icon = readInt(parser, null, "icon", 0);
                            pi.nonLocalizedLabel = parser.getAttributeValue(null, "label");
                            pi.protectionLevel = bp.protectionLevel;
                            bp.pendingInfo = pi;
                        }
                        out.put(bp.name, bp);
                    }
                } else {
                    PackageManagerService.reportSettingsProblem(5, "Unknown element reading permissions: " + parser.getName() + " at " + parser.getPositionDescription());
                }
                XmlUtils.skipCurrentTag(parser);
            }
        }
    }

    private void readDisabledSysPackageLPw(XmlPullParser parser) throws XmlPullParserException, IOException {
        String name = parser.getAttributeValue(null, ATTR_NAME);
        String realName = parser.getAttributeValue(null, "realName");
        String codePathStr = parser.getAttributeValue(null, "codePath");
        String resourcePathStr = parser.getAttributeValue(null, "resourcePath");
        String legacyCpuAbiStr = parser.getAttributeValue(null, "requiredCpuAbi");
        String legacyNativeLibraryPathStr = parser.getAttributeValue(null, "nativeLibraryPath");
        String parentPackageName = parser.getAttributeValue(null, "parentPackageName");
        String primaryCpuAbiStr = parser.getAttributeValue(null, "primaryCpuAbi");
        String secondaryCpuAbiStr = parser.getAttributeValue(null, "secondaryCpuAbi");
        String cpuAbiOverrideStr = parser.getAttributeValue(null, "cpuAbiOverride");
        if (primaryCpuAbiStr == null && legacyCpuAbiStr != null) {
            primaryCpuAbiStr = legacyCpuAbiStr;
        }
        if (resourcePathStr == null) {
            resourcePathStr = codePathStr;
        }
        String version = parser.getAttributeValue(null, "version");
        int versionCode = 0;
        if (version != null) {
            try {
                versionCode = Integer.parseInt(version);
            } catch (NumberFormatException e) {
            }
        }
        int pkgPrivateFlags = 0;
        File codePathFile = new File(codePathStr);
        if (PackageManagerService.locationIsPrivileged(codePathFile)) {
            pkgPrivateFlags = 8;
        }
        PackageSetting ps = new PackageSetting(name, realName, codePathFile, new File(resourcePathStr), legacyNativeLibraryPathStr, primaryCpuAbiStr, secondaryCpuAbiStr, cpuAbiOverrideStr, versionCode, 1, pkgPrivateFlags, parentPackageName, null, 0, null, null);
        String timeStampStr = parser.getAttributeValue(null, "ft");
        if (timeStampStr != null) {
            try {
                ps.setTimeStamp(Long.parseLong(timeStampStr, 16));
            } catch (NumberFormatException e2) {
            }
        } else {
            timeStampStr = parser.getAttributeValue(null, "ts");
            if (timeStampStr != null) {
                try {
                    ps.setTimeStamp(Long.parseLong(timeStampStr));
                } catch (NumberFormatException e3) {
                }
            }
        }
        timeStampStr = parser.getAttributeValue(null, "it");
        if (timeStampStr != null) {
            try {
                ps.firstInstallTime = Long.parseLong(timeStampStr, 16);
            } catch (NumberFormatException e4) {
            }
        }
        timeStampStr = parser.getAttributeValue(null, "ut");
        if (timeStampStr != null) {
            try {
                ps.lastUpdateTime = Long.parseLong(timeStampStr, 16);
            } catch (NumberFormatException e5) {
            }
        }
        String idStr = parser.getAttributeValue(null, "userId");
        ps.appId = idStr != null ? Integer.parseInt(idStr) : 0;
        if (ps.appId <= 0) {
            String sharedIdStr = parser.getAttributeValue(null, "sharedUserId");
            ps.appId = sharedIdStr != null ? Integer.parseInt(sharedIdStr) : 0;
        }
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                this.mDisabledSysPackages.put(name, ps);
            } else if (!(type == 3 || type == 4)) {
                if (parser.getName().equals(TAG_PERMISSIONS)) {
                    readInstallPermissionsLPr(parser, ps.getPermissionsState());
                } else if (parser.getName().equals(TAG_CHILD_PACKAGE)) {
                    String childPackageName = parser.getAttributeValue(null, ATTR_NAME);
                    if (ps.childPackageNames == null) {
                        ps.childPackageNames = new ArrayList();
                    }
                    ps.childPackageNames.add(childPackageName);
                } else if (parser.getName().equals(TAG_USES_STATIC_LIB)) {
                    readUsesStaticLibLPw(parser, ps);
                } else {
                    PackageManagerService.reportSettingsProblem(5, "Unknown element under <updated-package>: " + parser.getName());
                    XmlUtils.skipCurrentTag(parser);
                }
            }
        }
        this.mDisabledSysPackages.put(name, ps);
    }

    /* JADX WARNING: Removed duplicated region for block: B:201:0x06ae  */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x0193  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void readPackageLPw(XmlPullParser parser) throws XmlPullParserException, IOException {
        PackageSettingBase packageSetting;
        String str = null;
        String str2 = null;
        String str3 = null;
        String primaryCpuAbiString = null;
        String str4 = null;
        String installerPackageName = null;
        String isOrphaned = null;
        String volumeUuid = null;
        String str5 = null;
        int categoryHint = -1;
        String str6 = null;
        int pkgFlags = 0;
        int pkgPrivateFlags = 0;
        long timeStamp = 0;
        long firstInstallTime = 0;
        long lastUpdateTime = 0;
        int versionCode = 0;
        str = parser.getAttributeValue(null, ATTR_NAME);
        String realName = parser.getAttributeValue(null, "realName");
        str2 = parser.getAttributeValue(null, "userId");
        str6 = parser.getAttributeValue(null, "uidError");
        String sharedIdStr = parser.getAttributeValue(null, "sharedUserId");
        String codePathStr = parser.getAttributeValue(null, "codePath");
        String resourcePathStr = parser.getAttributeValue(null, "resourcePath");
        String legacyCpuAbiString = parser.getAttributeValue(null, "requiredCpuAbi");
        String parentPackageName = parser.getAttributeValue(null, "parentPackageName");
        str3 = parser.getAttributeValue(null, "nativeLibraryPath");
        primaryCpuAbiString = parser.getAttributeValue(null, "primaryCpuAbi");
        str4 = parser.getAttributeValue(null, "secondaryCpuAbi");
        String cpuAbiOverrideString = parser.getAttributeValue(null, "cpuAbiOverride");
        str5 = parser.getAttributeValue(null, "updateAvailable");
        if (primaryCpuAbiString == null && legacyCpuAbiString != null) {
            primaryCpuAbiString = legacyCpuAbiString;
        }
        String version = parser.getAttributeValue(null, "version");
        if (version != null) {
            try {
                versionCode = Integer.parseInt(version);
            } catch (NumberFormatException e) {
            }
        }
        try {
            installerPackageName = parser.getAttributeValue(null, "installer");
            isOrphaned = parser.getAttributeValue(null, "isOrphaned");
            volumeUuid = parser.getAttributeValue(null, ATTR_VOLUME_UUID);
            String categoryHintString = parser.getAttributeValue(null, "categoryHint");
            if (categoryHintString != null) {
                try {
                    categoryHint = Integer.parseInt(categoryHintString);
                } catch (NumberFormatException e2) {
                }
            }
            String systemStr = parser.getAttributeValue(null, "publicFlags");
            if (systemStr != null) {
                try {
                    pkgFlags = Integer.parseInt(systemStr);
                } catch (NumberFormatException e3) {
                }
                systemStr = parser.getAttributeValue(null, "privateFlags");
                if (systemStr != null) {
                    try {
                        pkgPrivateFlags = Integer.parseInt(systemStr);
                    } catch (NumberFormatException e4) {
                    }
                }
            } else {
                systemStr = parser.getAttributeValue(null, ATTR_FLAGS);
                if (systemStr != null) {
                    try {
                        pkgFlags = Integer.parseInt(systemStr);
                    } catch (NumberFormatException e5) {
                    }
                    if ((PRE_M_APP_INFO_FLAG_HIDDEN & pkgFlags) != 0) {
                        pkgPrivateFlags = 1;
                    }
                    if ((PRE_M_APP_INFO_FLAG_CANT_SAVE_STATE & pkgFlags) != 0) {
                        pkgPrivateFlags |= 2;
                    }
                    if ((PRE_M_APP_INFO_FLAG_FORWARD_LOCK & pkgFlags) != 0) {
                        pkgPrivateFlags |= 4;
                    }
                    if ((PRE_M_APP_INFO_FLAG_PRIVILEGED & pkgFlags) != 0) {
                        pkgPrivateFlags |= 8;
                    }
                    pkgFlags &= ~(((PRE_M_APP_INFO_FLAG_HIDDEN | PRE_M_APP_INFO_FLAG_CANT_SAVE_STATE) | PRE_M_APP_INFO_FLAG_FORWARD_LOCK) | PRE_M_APP_INFO_FLAG_PRIVILEGED);
                } else {
                    systemStr = parser.getAttributeValue(null, "system");
                    if (systemStr != null) {
                        int i;
                        if ("true".equalsIgnoreCase(systemStr)) {
                            i = 1;
                        } else {
                            i = 0;
                        }
                        pkgFlags = i | 0;
                    } else {
                        pkgFlags = 1;
                    }
                }
            }
            String timeStampStr = parser.getAttributeValue(null, "ft");
            if (timeStampStr != null) {
                try {
                    timeStamp = Long.parseLong(timeStampStr, 16);
                } catch (NumberFormatException e6) {
                }
            } else {
                timeStampStr = parser.getAttributeValue(null, "ts");
                if (timeStampStr != null) {
                    try {
                        timeStamp = Long.parseLong(timeStampStr);
                    } catch (NumberFormatException e7) {
                    }
                }
            }
            timeStampStr = parser.getAttributeValue(null, "it");
            if (timeStampStr != null) {
                try {
                    firstInstallTime = Long.parseLong(timeStampStr, 16);
                } catch (NumberFormatException e8) {
                }
            }
            timeStampStr = parser.getAttributeValue(null, "ut");
            if (timeStampStr != null) {
                try {
                    lastUpdateTime = Long.parseLong(timeStampStr, 16);
                } catch (NumberFormatException e9) {
                }
            }
            int userId = str2 != null ? Integer.parseInt(str2) : 0;
            int sharedUserId = sharedIdStr != null ? Integer.parseInt(sharedIdStr) : 0;
            if (resourcePathStr == null) {
                resourcePathStr = codePathStr;
            }
            if (realName != null) {
                realName = realName.intern();
            }
            if (str == null) {
                PackageManagerService.reportSettingsProblem(5, "Error in package manager settings: <package> has no name at " + parser.getPositionDescription());
                packageSetting = null;
            } else if (codePathStr == null) {
                PackageManagerService.reportSettingsProblem(5, "Error in package manager settings: <package> has no codePath at " + parser.getPositionDescription());
                packageSetting = null;
            } else if (userId > 0) {
                packageSetting = addPackageLPw(str.intern(), realName, new File(codePathStr), new File(resourcePathStr), str3, primaryCpuAbiString, str4, cpuAbiOverrideString, userId, versionCode, pkgFlags, pkgPrivateFlags, parentPackageName, null, null, null);
                if (packageSetting == null) {
                    try {
                        PackageManagerService.reportSettingsProblem(6, "Failure adding uid " + userId + " while parsing settings at " + parser.getPositionDescription());
                    } catch (NumberFormatException e10) {
                    }
                } else {
                    packageSetting.setTimeStamp(timeStamp);
                    packageSetting.firstInstallTime = firstInstallTime;
                    packageSetting.lastUpdateTime = lastUpdateTime;
                }
            } else if (sharedIdStr == null) {
                PackageManagerService.reportSettingsProblem(5, "Error in package manager settings: package " + str + " has bad userId " + str2 + " at " + parser.getPositionDescription());
                packageSetting = null;
            } else if (sharedUserId > 0) {
                packageSetting = new PackageSetting(str.intern(), realName, new File(codePathStr), new File(resourcePathStr), str3, primaryCpuAbiString, str4, cpuAbiOverrideString, versionCode, pkgFlags, pkgPrivateFlags, parentPackageName, null, sharedUserId, null, null);
                packageSetting.setTimeStamp(timeStamp);
                packageSetting.firstInstallTime = firstInstallTime;
                packageSetting.lastUpdateTime = lastUpdateTime;
                this.mPendingPackages.add(packageSetting);
            } else {
                PackageManagerService.reportSettingsProblem(5, "Error in package manager settings: package " + str + " has bad sharedId " + sharedIdStr + " at " + parser.getPositionDescription());
                packageSetting = null;
            }
        } catch (NumberFormatException e11) {
            packageSetting = null;
        }
        if (packageSetting == null) {
            packageSetting.uidError = "true".equals(str6);
            packageSetting.installerPackageName = installerPackageName;
            packageSetting.isOrphaned = "true".equals(isOrphaned);
            packageSetting.volumeUuid = volumeUuid;
            packageSetting.categoryHint = categoryHint;
            packageSetting.legacyNativeLibraryPathString = str3;
            packageSetting.primaryCpuAbiString = primaryCpuAbiString;
            packageSetting.secondaryCpuAbiString = str4;
            packageSetting.updateAvailable = "true".equals(str5);
            String enabledStr = parser.getAttributeValue(null, ATTR_ENABLED);
            if (enabledStr != null) {
                try {
                    packageSetting.setEnabled(Integer.parseInt(enabledStr), 0, null);
                } catch (NumberFormatException e12) {
                    if (enabledStr.equalsIgnoreCase("true")) {
                        packageSetting.setEnabled(1, 0, null);
                    } else {
                        if (enabledStr.equalsIgnoreCase("false")) {
                            packageSetting.setEnabled(2, 0, null);
                        } else {
                            if (enabledStr.equalsIgnoreCase("default")) {
                                packageSetting.setEnabled(0, 0, null);
                            } else {
                                PackageManagerService.reportSettingsProblem(5, "Error in package manager settings: package " + str + " has bad enabled value: " + str2 + " at " + parser.getPositionDescription());
                            }
                        }
                    }
                }
            } else {
                packageSetting.setEnabled(0, 0, null);
            }
            if (installerPackageName != null) {
                this.mInstallerPackages.add(installerPackageName);
            }
            String maxAspectRatioStr = parser.getAttributeValue(null, "maxAspectRatio");
            if (maxAspectRatioStr != null) {
                float maxAspectRatio = Float.parseFloat(maxAspectRatioStr);
                if (maxAspectRatio > 0.0f) {
                    packageSetting.maxAspectRatio = maxAspectRatio;
                }
            }
            String installStatusStr = parser.getAttributeValue(null, "installStatus");
            if (installStatusStr != null) {
                if (installStatusStr.equalsIgnoreCase("false")) {
                    packageSetting.installStatus = 0;
                } else {
                    packageSetting.installStatus = 1;
                }
            }
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
                    String tagName = parser.getName();
                    if (tagName.equals(TAG_DISABLED_COMPONENTS)) {
                        readDisabledComponentsLPw(packageSetting, parser, 0);
                    } else {
                        if (tagName.equals(TAG_ENABLED_COMPONENTS)) {
                            readEnabledComponentsLPw(packageSetting, parser, 0);
                        } else {
                            if (tagName.equals("sigs")) {
                                packageSetting.signatures.readXml(parser, this.mPastSignatures);
                            } else {
                                if (tagName.equals(TAG_PERMISSIONS)) {
                                    readInstallPermissionsLPr(parser, packageSetting.getPermissionsState());
                                    packageSetting.installPermissionsFixed = true;
                                } else {
                                    long id;
                                    Integer refCt;
                                    if (tagName.equals("proper-signing-keyset")) {
                                        id = Long.parseLong(parser.getAttributeValue(null, "identifier"));
                                        refCt = (Integer) this.mKeySetRefs.get(Long.valueOf(id));
                                        if (refCt != null) {
                                            this.mKeySetRefs.put(Long.valueOf(id), Integer.valueOf(refCt.intValue() + 1));
                                        } else {
                                            this.mKeySetRefs.put(Long.valueOf(id), Integer.valueOf(1));
                                        }
                                        packageSetting.keySetData.setProperSigningKeySet(id);
                                    } else {
                                        if (!tagName.equals("signing-keyset")) {
                                            if (tagName.equals("upgrade-keyset")) {
                                                packageSetting.keySetData.addUpgradeKeySetById(Long.parseLong(parser.getAttributeValue(null, "identifier")));
                                            } else {
                                                if (tagName.equals("defined-keyset")) {
                                                    id = Long.parseLong(parser.getAttributeValue(null, "identifier"));
                                                    String alias = parser.getAttributeValue(null, "alias");
                                                    refCt = (Integer) this.mKeySetRefs.get(Long.valueOf(id));
                                                    if (refCt != null) {
                                                        this.mKeySetRefs.put(Long.valueOf(id), Integer.valueOf(refCt.intValue() + 1));
                                                    } else {
                                                        this.mKeySetRefs.put(Long.valueOf(id), Integer.valueOf(1));
                                                    }
                                                    packageSetting.keySetData.addDefinedKeySet(id, alias);
                                                } else {
                                                    if (tagName.equals(TAG_DOMAIN_VERIFICATION)) {
                                                        readDomainVerificationLPw(parser, packageSetting);
                                                    } else {
                                                        if (tagName.equals(TAG_CHILD_PACKAGE)) {
                                                            String childPackageName = parser.getAttributeValue(null, ATTR_NAME);
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
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            XmlUtils.skipCurrentTag(parser);
            return;
        }
        PackageManagerService.reportSettingsProblem(5, "Error in package manager settings: package " + str + " has bad userId " + str2 + " at " + parser.getPositionDescription());
        if (packageSetting == null) {
        }
    }

    private void readDisabledComponentsLPw(PackageSettingBase packageSetting, XmlPullParser parser, int userId) throws IOException, XmlPullParserException {
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
            int type = parser.next();
            if (type == 1) {
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
        String name = null;
        String idStr = null;
        int pkgFlags = 0;
        SharedUserSetting su = null;
        try {
            name = parser.getAttributeValue(null, ATTR_NAME);
            idStr = parser.getAttributeValue(null, "userId");
            int userId = idStr != null ? Integer.parseInt(idStr) : 0;
            if ("true".equals(parser.getAttributeValue(null, "system"))) {
                pkgFlags = 1;
            }
            if (name == null) {
                PackageManagerService.reportSettingsProblem(5, "Error in package manager settings: <shared-user> has no name at " + parser.getPositionDescription());
            } else if (userId == 0) {
                PackageManagerService.reportSettingsProblem(5, "Error in package manager settings: shared-user " + name + " has bad userId " + idStr + " at " + parser.getPositionDescription());
            } else {
                su = addSharedUserLPw(name.intern(), userId, pkgFlags, 0);
                if (su == null) {
                    PackageManagerService.reportSettingsProblem(6, "Occurred while parsing settings at " + parser.getPositionDescription());
                }
            }
        } catch (NumberFormatException e) {
            PackageManagerService.reportSettingsProblem(5, "Error in package manager settings: package " + name + " has bad userId " + idStr + " at " + parser.getPositionDescription());
        }
        if (su != null) {
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
                    String tagName = parser.getName();
                    if (tagName.equals("sigs")) {
                        su.signatures.readXml(parser, this.mPastSignatures);
                        if (su.signatures.mSignatures.length <= 0) {
                            throw new XmlPullParserException("No cert in shareduid:" + su.name);
                        }
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

    /* JADX WARNING: Removed duplicated region for block: B:20:0x0079  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void createNewUserLI(PackageManagerService service, Installer installer, int userHandle, String[] disallowedPackages) {
        int packagesCount;
        String[] volumeUuids;
        String[] names;
        int[] appIds;
        String[] seinfos;
        int[] targetSdkVersions;
        int i;
        synchronized (this.mPackages) {
            Collection<PackageSetting> packages = this.mPackages.values();
            packagesCount = packages.size();
            volumeUuids = new String[packagesCount];
            names = new String[packagesCount];
            appIds = new int[packagesCount];
            seinfos = new String[packagesCount];
            targetSdkVersions = new int[packagesCount];
            Iterator<PackageSetting> packagesIterator = packages.iterator();
            for (i = 0; i < packagesCount; i++) {
                PackageSetting ps = (PackageSetting) packagesIterator.next();
                if (!(ps.pkg == null || ps.pkg.applicationInfo == null)) {
                    boolean shouldInstall;
                    if (ps.isSystem()) {
                        shouldInstall = ArrayUtils.contains(disallowedPackages, ps.name) ^ 1;
                    } else {
                        shouldInstall = false;
                    }
                    if (ps.isSystem()) {
                        if (isInDelAppList(ps.name)) {
                            ps.setInstalled(false, userHandle);
                            if (!shouldInstall) {
                                writeKernelMappingLPr(ps);
                            }
                            volumeUuids[i] = ps.volumeUuid;
                            names[i] = ps.name;
                            appIds[i] = ps.appId;
                            seinfos[i] = ps.pkg.applicationInfo.seInfo;
                            targetSdkVersions[i] = ps.pkg.applicationInfo.targetSdkVersion;
                        }
                    }
                    ps.setInstalled(shouldInstall, userHandle);
                    if (shouldInstall) {
                    }
                    volumeUuids[i] = ps.volumeUuid;
                    names[i] = ps.name;
                    appIds[i] = ps.appId;
                    seinfos[i] = ps.pkg.applicationInfo.seInfo;
                    targetSdkVersions[i] = ps.pkg.applicationInfo.targetSdkVersion;
                }
            }
        }
        for (i = 0; i < packagesCount; i++) {
            if (names[i] != null) {
                try {
                    installer.createAppData(volumeUuids[i], names[i], userHandle, 3, appIds[i], seinfos[i], targetSdkVersions[i]);
                } catch (InstallerException e) {
                    Slog.w(TAG, "Failed to prepare app data", e);
                }
            }
        }
        synchronized (this.mPackages) {
            applyDefaultPreferredAppsLPw(service, userHandle);
        }
    }

    void removeUserLPw(int userId) {
        for (Entry<String, PackageSetting> entry : this.mPackages.entrySet()) {
            ((PackageSetting) entry.getValue()).removeUser(userId);
        }
        this.mPreferredActivities.remove(userId);
        getUserPackagesStateFile(userId).delete();
        getUserPackagesStateBackupFile(userId).delete();
        removeCrossProfileIntentFiltersLPw(userId);
        this.mRuntimePermissionsPersistence.onUserRemovedLPw(userId);
        writePackageListLPr();
        writeKernelRemoveUserLPr(userId);
    }

    void removeCrossProfileIntentFiltersLPw(int userId) {
        synchronized (this.mCrossProfileIntentResolvers) {
            if (this.mCrossProfileIntentResolvers.get(userId) != null) {
                this.mCrossProfileIntentResolvers.remove(userId);
                writePackageRestrictionsLPr(userId);
            }
            int count = this.mCrossProfileIntentResolvers.size();
            for (int i = 0; i < count; i++) {
                int sourceUserId = this.mCrossProfileIntentResolvers.keyAt(i);
                CrossProfileIntentResolver cpir = (CrossProfileIntentResolver) this.mCrossProfileIntentResolvers.get(sourceUserId);
                boolean needsWriting = false;
                for (CrossProfileIntentFilter cpif : new ArraySet(cpir.filterSet())) {
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
                return i + 10000;
            }
        }
        if (N > 9999) {
            return -1;
        }
        this.mUserIds.add(obj);
        return N + 10000;
    }

    public VerifierDeviceIdentity getVerifierDeviceIdentityLPw() {
        if (this.mVerifierDeviceIdentity == null) {
            this.mVerifierDeviceIdentity = VerifierDeviceIdentity.generate();
            writeLPr();
        }
        return this.mVerifierDeviceIdentity;
    }

    boolean hasOtherDisabledSystemPkgWithChildLPr(String parentPackageName, String childPackageName) {
        int packageCount = this.mDisabledSysPackages.size();
        for (int i = 0; i < packageCount; i++) {
            PackageSetting disabledPs = (PackageSetting) this.mDisabledSysPackages.valueAt(i);
            if (!(disabledPs.childPackageNames == null || disabledPs.childPackageNames.isEmpty() || disabledPs.name.equals(parentPackageName))) {
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
        return (PackageSetting) this.mDisabledSysPackages.get(name);
    }

    private String compToString(ArraySet<String> cmp) {
        return cmp != null ? Arrays.toString(cmp.toArray()) : "[]";
    }

    boolean isEnabledAndMatchLPr(ComponentInfo componentInfo, int flags, int userId) {
        PackageSetting ps = (PackageSetting) this.mPackages.get(componentInfo.packageName);
        if (ps == null) {
            return false;
        }
        return ps.readUserState(userId).isMatch(componentInfo, flags);
    }

    String getInstallerPackageNameLPr(String packageName) {
        PackageSetting pkg = (PackageSetting) this.mPackages.get(packageName);
        if (pkg != null) {
            return pkg.installerPackageName;
        }
        throw new IllegalArgumentException("Unknown package: " + packageName);
    }

    boolean isOrphaned(String packageName) {
        PackageSetting pkg = (PackageSetting) this.mPackages.get(packageName);
        if (pkg != null) {
            return pkg.isOrphaned;
        }
        throw new IllegalArgumentException("Unknown package: " + packageName);
    }

    int getApplicationEnabledSettingLPr(String packageName, int userId) {
        PackageSetting pkg = (PackageSetting) this.mPackages.get(packageName);
        if (pkg != null) {
            return pkg.getEnabled(userId);
        }
        throw new IllegalArgumentException("Unknown package: " + packageName);
    }

    int getComponentEnabledSettingLPr(ComponentName componentName, int userId) {
        PackageSetting pkg = (PackageSetting) this.mPackages.get(componentName.getPackageName());
        if (pkg != null) {
            return pkg.getCurrentEnabledStateLPr(componentName.getClassName(), userId);
        }
        throw new IllegalArgumentException("Unknown component: " + componentName);
    }

    boolean wasPackageEverLaunchedLPr(String packageName, int userId) {
        PackageSetting pkgSetting = (PackageSetting) this.mPackages.get(packageName);
        if (pkgSetting != null) {
            return pkgSetting.getNotLaunched(userId) ^ 1;
        }
        throw new IllegalArgumentException("Unknown package: " + packageName);
    }

    boolean setPackageStoppedStateLPw(PackageManagerService pm, String packageName, boolean stopped, boolean allowedByPermission, int uid, int userId) {
        int appId = UserHandle.getAppId(uid);
        PackageSetting pkgSetting = (PackageSetting) this.mPackages.get(packageName);
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

    private static List<UserInfo> getAllUsers(UserManagerService userManager) {
        long id = Binder.clearCallingIdentity();
        List<UserInfo> list = null;
        try {
            list = userManager.getUsers(list);
            return list;
        } catch (NullPointerException e) {
            return null;
        } finally {
            Binder.restoreCallingIdentity(id);
        }
    }

    List<PackageSetting> getVolumePackagesLPr(String volumeUuid) {
        ArrayList<PackageSetting> res = new ArrayList();
        for (int i = 0; i < this.mPackages.size(); i++) {
            PackageSetting setting = (PackageSetting) this.mPackages.valueAt(i);
            if (Objects.equals(volumeUuid, setting.volumeUuid)) {
                res.add(setting);
            }
        }
        return res;
    }

    static void printFlags(PrintWriter pw, int val, Object[] spec) {
        pw.print("[ ");
        for (int i = 0; i < spec.length; i += 2) {
            if ((val & ((Integer) spec[i]).intValue()) != 0) {
                pw.print(spec[i + 1]);
                pw.print(" ");
            }
        }
        pw.print("]");
    }

    void dumpVersionLPr(IndentingPrintWriter pw) {
        pw.increaseIndent();
        for (int i = 0; i < this.mVersion.size(); i++) {
            String volumeUuid = (String) this.mVersion.keyAt(i);
            VersionInfo ver = (VersionInfo) this.mVersion.valueAt(i);
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
            pw.decreaseIndent();
        }
        pw.decreaseIndent();
    }

    void dumpPackageLPr(PrintWriter pw, String prefix, String checkinTag, ArraySet<String> permissionNames, PackageSetting ps, SimpleDateFormat sdf, Date date, List<UserInfo> users, boolean dumpAll) {
        int i;
        String lastDisabledAppCaller;
        if (checkinTag != null) {
            pw.print(checkinTag);
            pw.print(",");
            pw.print(ps.realName != null ? ps.realName : ps.name);
            pw.print(",");
            pw.print(ps.appId);
            pw.print(",");
            pw.print(ps.versionCode);
            pw.print(",");
            pw.print(ps.firstInstallTime);
            pw.print(",");
            pw.print(ps.lastUpdateTime);
            pw.print(",");
            pw.print(ps.installerPackageName != null ? ps.installerPackageName : "?");
            pw.println();
            if (ps.pkg != null) {
                pw.print(checkinTag);
                pw.print("-");
                pw.print("splt,");
                pw.print("base,");
                pw.println(ps.pkg.baseRevisionCode);
                if (ps.pkg.splitNames != null) {
                    for (i = 0; i < ps.pkg.splitNames.length; i++) {
                        pw.print(checkinTag);
                        pw.print("-");
                        pw.print("splt,");
                        pw.print(ps.pkg.splitNames[i]);
                        pw.print(",");
                        pw.println(ps.pkg.splitRevisionCodes[i]);
                    }
                }
            }
            for (UserInfo user : users) {
                pw.print(checkinTag);
                pw.print("-");
                pw.print("usr");
                pw.print(",");
                pw.print(user.id);
                pw.print(",");
                pw.print(ps.getInstalled(user.id) ? "I" : "i");
                pw.print(ps.getHidden(user.id) ? "B" : "b");
                pw.print(ps.getSuspended(user.id) ? "SU" : "su");
                pw.print(ps.getStopped(user.id) ? "S" : "s");
                pw.print(ps.getNotLaunched(user.id) ? "l" : "L");
                pw.print(ps.getInstantApp(user.id) ? "IA" : "ia");
                pw.print(",");
                pw.print(ps.getEnabled(user.id));
                lastDisabledAppCaller = ps.getLastDisabledAppCaller(user.id);
                pw.print(",");
                if (lastDisabledAppCaller == null) {
                    lastDisabledAppCaller = "?";
                }
                pw.print(lastDisabledAppCaller);
                pw.println();
            }
            return;
        }
        String str;
        pw.print(prefix);
        pw.print("Package [");
        if (ps.realName != null) {
            str = ps.realName;
        } else {
            str = ps.name;
        }
        pw.print(str);
        pw.print("] (");
        pw.print(Integer.toHexString(System.identityHashCode(ps)));
        pw.println("):");
        if (ps.realName != null) {
            pw.print(prefix);
            pw.print("  compat name=");
            pw.println(ps.name);
        }
        pw.print(prefix);
        pw.print("  userId=");
        pw.println(ps.appId);
        if (ps.sharedUser != null) {
            pw.print(prefix);
            pw.print("  sharedUser=");
            pw.println(ps.sharedUser);
        }
        pw.print(prefix);
        pw.print("  pkg=");
        pw.println(ps.pkg);
        pw.print(prefix);
        pw.print("  codePath=");
        pw.println(ps.codePathString);
        if (permissionNames == null) {
            pw.print(prefix);
            pw.print("  resourcePath=");
            pw.println(ps.resourcePathString);
            pw.print(prefix);
            pw.print("  legacyNativeLibraryDir=");
            pw.println(ps.legacyNativeLibraryPathString);
            pw.print(prefix);
            pw.print("  primaryCpuAbi=");
            pw.println(ps.primaryCpuAbiString);
            pw.print(prefix);
            pw.print("  secondaryCpuAbi=");
            pw.println(ps.secondaryCpuAbiString);
        }
        pw.print(prefix);
        pw.print("  versionCode=");
        pw.print(ps.versionCode);
        if (ps.pkg != null) {
            pw.print(" minSdk=");
            pw.print(ps.pkg.applicationInfo.minSdkVersion);
            pw.print(" targetSdk=");
            pw.print(ps.pkg.applicationInfo.targetSdkVersion);
        }
        pw.println();
        if (ps.pkg != null) {
            if (ps.pkg.parentPackage != null) {
                Package parentPkg = ps.pkg.parentPackage;
                PackageSetting pps = (PackageSetting) this.mPackages.get(parentPkg.packageName);
                if (pps == null || (pps.codePathString.equals(parentPkg.codePath) ^ 1) != 0) {
                    pps = (PackageSetting) this.mDisabledSysPackages.get(parentPkg.packageName);
                }
                if (pps != null) {
                    pw.print(prefix);
                    pw.print("  parentPackage=");
                    if (pps.realName != null) {
                        str = pps.realName;
                    } else {
                        str = pps.name;
                    }
                    pw.println(str);
                }
            } else if (ps.pkg.childPackages != null) {
                pw.print(prefix);
                pw.print("  childPackages=[");
                int childCount = ps.pkg.childPackages.size();
                for (i = 0; i < childCount; i++) {
                    Package childPkg = (Package) ps.pkg.childPackages.get(i);
                    PackageSetting cps = (PackageSetting) this.mPackages.get(childPkg.packageName);
                    if (cps == null || (cps.codePathString.equals(childPkg.codePath) ^ 1) != 0) {
                        cps = (PackageSetting) this.mDisabledSysPackages.get(childPkg.packageName);
                    }
                    if (cps != null) {
                        if (i > 0) {
                            pw.print(", ");
                        }
                        if (cps.realName != null) {
                            str = cps.realName;
                        } else {
                            str = cps.name;
                        }
                        pw.print(str);
                    }
                }
                pw.println("]");
            }
            pw.print(prefix);
            pw.print("  versionName=");
            pw.println(ps.pkg.mVersionName);
            pw.print(prefix);
            pw.print("  splits=");
            dumpSplitNames(pw, ps.pkg);
            pw.println();
            int apkSigningVersion = PackageParser.getApkSigningVersion(ps.pkg);
            if (apkSigningVersion != 0) {
                pw.print(prefix);
                pw.print("  apkSigningVersion=");
                pw.println(apkSigningVersion);
            }
            pw.print(prefix);
            pw.print("  applicationInfo=");
            pw.println(ps.pkg.applicationInfo.toString());
            pw.print(prefix);
            pw.print("  flags=");
            printFlags(pw, ps.pkg.applicationInfo.flags, FLAG_DUMP_SPEC);
            pw.println();
            pw.print(prefix);
            pw.print("  hwflags=");
            printFlags(pw, ps.pkg.applicationInfo.hwFlags, HW_FLAG_DUMP_SPEC);
            pw.println();
            if (ps.pkg.applicationInfo.privateFlags != 0) {
                pw.print(prefix);
                pw.print("  privateFlags=");
                printFlags(pw, ps.pkg.applicationInfo.privateFlags, PRIVATE_FLAG_DUMP_SPEC);
                pw.println();
            }
            pw.print(prefix);
            pw.print("  dataDir=");
            pw.println(ps.pkg.applicationInfo.dataDir);
            pw.print(prefix);
            pw.print("  supportsScreens=[");
            boolean first = true;
            if ((ps.pkg.applicationInfo.flags & 512) != 0) {
                if (1 == null) {
                    pw.print(", ");
                }
                first = false;
                pw.print("small");
            }
            if ((ps.pkg.applicationInfo.flags & 1024) != 0) {
                if (!first) {
                    pw.print(", ");
                }
                first = false;
                pw.print("medium");
            }
            if ((ps.pkg.applicationInfo.flags & 2048) != 0) {
                if (!first) {
                    pw.print(", ");
                }
                first = false;
                pw.print("large");
            }
            if ((ps.pkg.applicationInfo.flags & DumpState.DUMP_FROZEN) != 0) {
                if (!first) {
                    pw.print(", ");
                }
                first = false;
                pw.print("xlarge");
            }
            if ((ps.pkg.applicationInfo.flags & 4096) != 0) {
                if (!first) {
                    pw.print(", ");
                }
                first = false;
                pw.print("resizeable");
            }
            if ((ps.pkg.applicationInfo.flags & 8192) != 0) {
                if (!first) {
                    pw.print(", ");
                }
                pw.print("anyDensity");
            }
            pw.println("]");
            if (ps.pkg.libraryNames != null && ps.pkg.libraryNames.size() > 0) {
                pw.print(prefix);
                pw.println("  dynamic libraries:");
                for (i = 0; i < ps.pkg.libraryNames.size(); i++) {
                    pw.print(prefix);
                    pw.print("    ");
                    pw.println((String) ps.pkg.libraryNames.get(i));
                }
            }
            if (ps.pkg.staticSharedLibName != null) {
                pw.print(prefix);
                pw.println("  static library:");
                pw.print(prefix);
                pw.print("    ");
                pw.print("name:");
                pw.print(ps.pkg.staticSharedLibName);
                pw.print(" version:");
                pw.println(ps.pkg.staticSharedLibVersion);
            }
            if (ps.pkg.usesLibraries != null && ps.pkg.usesLibraries.size() > 0) {
                pw.print(prefix);
                pw.println("  usesLibraries:");
                for (i = 0; i < ps.pkg.usesLibraries.size(); i++) {
                    pw.print(prefix);
                    pw.print("    ");
                    pw.println((String) ps.pkg.usesLibraries.get(i));
                }
            }
            if (ps.pkg.usesStaticLibraries != null && ps.pkg.usesStaticLibraries.size() > 0) {
                pw.print(prefix);
                pw.println("  usesStaticLibraries:");
                for (i = 0; i < ps.pkg.usesStaticLibraries.size(); i++) {
                    pw.print(prefix);
                    pw.print("    ");
                    pw.print((String) ps.pkg.usesStaticLibraries.get(i));
                    pw.print(" version:");
                    pw.println(ps.pkg.usesStaticLibrariesVersions[i]);
                }
            }
            if (ps.pkg.usesOptionalLibraries != null && ps.pkg.usesOptionalLibraries.size() > 0) {
                pw.print(prefix);
                pw.println("  usesOptionalLibraries:");
                for (i = 0; i < ps.pkg.usesOptionalLibraries.size(); i++) {
                    pw.print(prefix);
                    pw.print("    ");
                    pw.println((String) ps.pkg.usesOptionalLibraries.get(i));
                }
            }
            if (ps.pkg.usesLibraryFiles != null && ps.pkg.usesLibraryFiles.length > 0) {
                pw.print(prefix);
                pw.println("  usesLibraryFiles:");
                for (String str2 : ps.pkg.usesLibraryFiles) {
                    pw.print(prefix);
                    pw.print("    ");
                    pw.println(str2);
                }
            }
        }
        pw.print(prefix);
        pw.print("  timeStamp=");
        date.setTime(ps.timeStamp);
        pw.println(sdf.format(date));
        pw.print(prefix);
        pw.print("  firstInstallTime=");
        date.setTime(ps.firstInstallTime);
        pw.println(sdf.format(date));
        pw.print(prefix);
        pw.print("  lastUpdateTime=");
        date.setTime(ps.lastUpdateTime);
        pw.println(sdf.format(date));
        if (ps.installerPackageName != null) {
            pw.print(prefix);
            pw.print("  installerPackageName=");
            pw.println(ps.installerPackageName);
        }
        if (ps.volumeUuid != null) {
            pw.print(prefix);
            pw.print("  volumeUuid=");
            pw.println(ps.volumeUuid);
        }
        pw.print(prefix);
        pw.print("  signatures=");
        pw.println(ps.signatures);
        pw.print(prefix);
        pw.print("  installPermissionsFixed=");
        pw.print(ps.installPermissionsFixed);
        pw.print(" installStatus=");
        pw.println(ps.installStatus);
        pw.print(prefix);
        pw.print("  pkgFlags=");
        printFlags(pw, ps.pkgFlags, FLAG_DUMP_SPEC);
        pw.println();
        if (!(ps.pkg == null || ps.pkg.permissions == null || ps.pkg.permissions.size() <= 0)) {
            ArrayList<Permission> perms = ps.pkg.permissions;
            pw.print(prefix);
            pw.println("  declared permissions:");
            for (i = 0; i < perms.size(); i++) {
                Permission perm = (Permission) perms.get(i);
                if (permissionNames != null) {
                    if ((permissionNames.contains(perm.info.name) ^ 1) != 0) {
                    }
                }
                pw.print(prefix);
                pw.print("    ");
                pw.print(perm.info.name);
                pw.print(": prot=");
                pw.print(PermissionInfo.protectionToString(perm.info.protectionLevel));
                if ((perm.info.flags & 1) != 0) {
                    pw.print(", COSTS_MONEY");
                }
                if ((perm.info.flags & 2) != 0) {
                    pw.print(", HIDDEN");
                }
                if ((perm.info.flags & 1073741824) != 0) {
                    pw.print(", INSTALLED");
                }
                pw.println();
            }
        }
        if ((permissionNames != null || dumpAll) && ps.pkg != null && ps.pkg.requestedPermissions != null && ps.pkg.requestedPermissions.size() > 0) {
            ArrayList<String> perms2 = ps.pkg.requestedPermissions;
            pw.print(prefix);
            pw.println("  requested permissions:");
            for (i = 0; i < perms2.size(); i++) {
                String perm2 = (String) perms2.get(i);
                if (permissionNames == null || (permissionNames.contains(perm2) ^ 1) == 0) {
                    pw.print(prefix);
                    pw.print("    ");
                    pw.println(perm2);
                }
            }
        }
        if (ps.sharedUser == null || permissionNames != null || dumpAll) {
            dumpInstallPermissionsLPr(pw, prefix + "  ", permissionNames, ps.getPermissionsState());
        }
        for (UserInfo user2 : users) {
            pw.print(prefix);
            pw.print("  User ");
            pw.print(user2.id);
            pw.print(": ");
            pw.print("ceDataInode=");
            pw.print(ps.getCeDataInode(user2.id));
            pw.print(" installed=");
            pw.print(ps.getInstalled(user2.id));
            pw.print(" hidden=");
            pw.print(ps.getHidden(user2.id));
            pw.print(" suspended=");
            pw.print(ps.getSuspended(user2.id));
            pw.print(" stopped=");
            pw.print(ps.getStopped(user2.id));
            pw.print(" notLaunched=");
            pw.print(ps.getNotLaunched(user2.id));
            pw.print(" enabled=");
            pw.print(ps.getEnabled(user2.id));
            pw.print(" instant=");
            pw.println(ps.getInstantApp(user2.id));
            String[] overlayPaths = ps.getOverlayPaths(user2.id);
            if (overlayPaths != null && overlayPaths.length > 0) {
                pw.print(prefix);
                pw.println("  overlay paths:");
                for (String path : overlayPaths) {
                    pw.print(prefix);
                    pw.print("    ");
                    pw.println(path);
                }
            }
            lastDisabledAppCaller = ps.getLastDisabledAppCaller(user2.id);
            if (lastDisabledAppCaller != null) {
                pw.print(prefix);
                pw.print("    lastDisabledCaller: ");
                pw.println(lastDisabledAppCaller);
            }
            if (ps.sharedUser == null) {
                PermissionsState permissionsState = ps.getPermissionsState();
                dumpGidsLPr(pw, prefix + "    ", permissionsState.computeGids(user2.id));
                dumpRuntimePermissionsLPr(pw, prefix + "    ", permissionNames, permissionsState.getRuntimePermissionStates(user2.id), dumpAll);
            }
            if (permissionNames == null) {
                ArraySet<String> cmp = ps.getDisabledComponents(user2.id);
                if (cmp != null && cmp.size() > 0) {
                    pw.print(prefix);
                    pw.println("    disabledComponents:");
                    for (String s : cmp) {
                        pw.print(prefix);
                        pw.print("      ");
                        pw.println(s);
                    }
                }
                cmp = ps.getEnabledComponents(user2.id);
                if (cmp != null && cmp.size() > 0) {
                    pw.print(prefix);
                    pw.println("    enabledComponents:");
                    for (String s2 : cmp) {
                        pw.print(prefix);
                        pw.print("      ");
                        pw.println(s2);
                    }
                }
            }
        }
    }

    void dumpPackagesLPr(PrintWriter pw, String packageName, ArraySet<String> permissionNames, DumpState dumpState, boolean checkin) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        boolean printedSomething = false;
        List<UserInfo> users = getAllUsers(UserManagerService.getInstance());
        for (PackageSetting ps : this.mPackages.values()) {
            if (packageName != null) {
                if ((packageName.equals(ps.realName) ^ 1) != 0) {
                    if ((packageName.equals(ps.name) ^ 1) != 0) {
                    }
                }
            }
            if (permissionNames == null || (ps.getPermissionsState().hasRequestedPermission(permissionNames) ^ 1) == 0) {
                if (!(checkin || packageName == null)) {
                    dumpState.setSharedUser(ps.sharedUser);
                }
                if (!(checkin || (printedSomething ^ 1) == 0)) {
                    if (dumpState.onTitlePrinted()) {
                        pw.println();
                    }
                    pw.println("Packages:");
                    printedSomething = true;
                }
                dumpPackageLPr(pw, "  ", checkin ? "pkg" : null, permissionNames, ps, sdf, date, users, packageName != null);
            }
        }
        printedSomething = false;
        if (this.mRenamedPackages.size() > 0 && permissionNames == null) {
            for (Entry<String, String> e : this.mRenamedPackages.entrySet()) {
                if (packageName != null) {
                    if ((packageName.equals(e.getKey()) ^ 1) != 0) {
                        if ((packageName.equals(e.getValue()) ^ 1) != 0) {
                        }
                    }
                }
                if (checkin) {
                    pw.print("ren,");
                } else {
                    if (!printedSomething) {
                        if (dumpState.onTitlePrinted()) {
                            pw.println();
                        }
                        pw.println("Renamed packages:");
                        printedSomething = true;
                    }
                    pw.print("  ");
                }
                pw.print((String) e.getKey());
                pw.print(checkin ? " -> " : ",");
                pw.println((String) e.getValue());
            }
        }
        printedSomething = false;
        if (this.mDisabledSysPackages.size() > 0 && permissionNames == null) {
            for (PackageSetting ps2 : this.mDisabledSysPackages.values()) {
                if (packageName != null) {
                    if ((packageName.equals(ps2.realName) ^ 1) != 0) {
                        if ((packageName.equals(ps2.name) ^ 1) != 0) {
                        }
                    }
                }
                if (!(checkin || (printedSomething ^ 1) == 0)) {
                    if (dumpState.onTitlePrinted()) {
                        pw.println();
                    }
                    pw.println("Hidden system packages:");
                    printedSomething = true;
                }
                dumpPackageLPr(pw, "  ", checkin ? "dis" : null, permissionNames, ps2, sdf, date, users, packageName != null);
            }
        }
    }

    void dumpPackagesProto(ProtoOutputStream proto) {
        List<UserInfo> users = getAllUsers(UserManagerService.getInstance());
        int count = this.mPackages.size();
        for (int i = 0; i < count; i++) {
            ((PackageSetting) this.mPackages.valueAt(i)).writeToProto(proto, 2272037699589L, users);
        }
    }

    void dumpPermissionsLPr(PrintWriter pw, String packageName, ArraySet<String> permissionNames, DumpState dumpState) {
        boolean printedSomething = false;
        for (BasePermission p : this.mPermissions.values()) {
            if ((packageName == null || (packageName.equals(p.sourcePackage) ^ 1) == 0) && (permissionNames == null || (permissionNames.contains(p.name) ^ 1) == 0)) {
                if (!printedSomething) {
                    if (dumpState.onTitlePrinted()) {
                        pw.println();
                    }
                    pw.println("Permissions:");
                    printedSomething = true;
                }
                pw.print("  Permission [");
                pw.print(p.name);
                pw.print("] (");
                pw.print(Integer.toHexString(System.identityHashCode(p)));
                pw.println("):");
                pw.print("    sourcePackage=");
                pw.println(p.sourcePackage);
                pw.print("    uid=");
                pw.print(p.uid);
                pw.print(" gids=");
                pw.print(Arrays.toString(p.computeGids(0)));
                pw.print(" type=");
                pw.print(p.type);
                pw.print(" prot=");
                pw.println(PermissionInfo.protectionToString(p.protectionLevel));
                if (p.perm != null) {
                    pw.print("    perm=");
                    pw.println(p.perm);
                    if ((p.perm.info.flags & 1073741824) == 0 || (p.perm.info.flags & 2) != 0) {
                        pw.print("    flags=0x");
                        pw.println(Integer.toHexString(p.perm.info.flags));
                    }
                }
                if (p.packageSetting != null) {
                    pw.print("    packageSetting=");
                    pw.println(p.packageSetting);
                }
                if ("android.permission.READ_EXTERNAL_STORAGE".equals(p.name)) {
                    pw.print("    enforced=");
                    pw.println(this.mReadExternalStorageEnforced);
                }
            }
        }
    }

    void dumpSharedUsersLPr(PrintWriter pw, String packageName, ArraySet<String> permissionNames, DumpState dumpState, boolean checkin) {
        boolean printedSomething = false;
        for (SharedUserSetting su : this.mSharedUsers.values()) {
            if ((packageName == null || su == dumpState.getSharedUser()) && (permissionNames == null || (su.getPermissionsState().hasRequestedPermission(permissionNames) ^ 1) == 0)) {
                if (!checkin) {
                    if (!printedSomething) {
                        if (dumpState.onTitlePrinted()) {
                            pw.println();
                        }
                        pw.println("Shared users:");
                        printedSomething = true;
                    }
                    pw.print("  SharedUser [");
                    pw.print(su.name);
                    pw.print("] (");
                    pw.print(Integer.toHexString(System.identityHashCode(su)));
                    pw.println("):");
                    String prefix = "    ";
                    pw.print(prefix);
                    pw.print("userId=");
                    pw.println(su.userId);
                    PermissionsState permissionsState = su.getPermissionsState();
                    dumpInstallPermissionsLPr(pw, prefix, permissionNames, permissionsState);
                    int[] userIds = UserManagerService.getInstance().getUserIds();
                    int i = 0;
                    int length = userIds.length;
                    while (true) {
                        int i2 = i;
                        if (i2 >= length) {
                            break;
                        }
                        int userId = userIds[i2];
                        int[] gids = permissionsState.computeGids(userId);
                        List<PermissionState> permissions = permissionsState.getRuntimePermissionStates(userId);
                        if (!ArrayUtils.isEmpty(gids) || (permissions.isEmpty() ^ 1) != 0) {
                            pw.print(prefix);
                            pw.print("User ");
                            pw.print(userId);
                            pw.println(": ");
                            dumpGidsLPr(pw, prefix + "  ", gids);
                            dumpRuntimePermissionsLPr(pw, prefix + "  ", permissionNames, permissions, packageName != null);
                        }
                        i = i2 + 1;
                    }
                } else {
                    pw.print("suid,");
                    pw.print(su.userId);
                    pw.print(",");
                    pw.println(su.name);
                }
            }
        }
    }

    void dumpSharedUsersProto(ProtoOutputStream proto) {
        int count = this.mSharedUsers.size();
        for (int i = 0; i < count; i++) {
            SharedUserSetting su = (SharedUserSetting) this.mSharedUsers.valueAt(i);
            long sharedUserToken = proto.start(2272037699590L);
            proto.write(1112396529665L, su.userId);
            proto.write(1159641169922L, su.name);
            proto.end(sharedUserToken);
        }
    }

    void dumpReadMessagesLPr(PrintWriter pw, DumpState dumpState) {
        pw.println("Settings parse messages:");
        pw.print(this.mReadMessages.toString());
    }

    void dumpRestoredPermissionGrantsLPr(PrintWriter pw, DumpState dumpState) {
        if (this.mRestoredUserGrants.size() > 0) {
            pw.println();
            pw.println("Restored (pending) permission grants:");
            for (int userIndex = 0; userIndex < this.mRestoredUserGrants.size(); userIndex++) {
                ArrayMap<String, ArraySet<RestoredPermissionGrant>> grantsByPackage = (ArrayMap) this.mRestoredUserGrants.valueAt(userIndex);
                if (grantsByPackage != null && grantsByPackage.size() > 0) {
                    int userId = this.mRestoredUserGrants.keyAt(userIndex);
                    pw.print("  User ");
                    pw.println(userId);
                    for (int pkgIndex = 0; pkgIndex < grantsByPackage.size(); pkgIndex++) {
                        ArraySet<RestoredPermissionGrant> grants = (ArraySet) grantsByPackage.valueAt(pkgIndex);
                        if (grants != null && grants.size() > 0) {
                            String pkgName = (String) grantsByPackage.keyAt(pkgIndex);
                            pw.print("    ");
                            pw.print(pkgName);
                            pw.println(" :");
                            for (RestoredPermissionGrant g : grants) {
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

    private static void dumpSplitNames(PrintWriter pw, Package pkg) {
        if (pkg == null) {
            pw.print(Shell.NIGHT_MODE_STR_UNKNOWN);
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
                if (pkg.splitRevisionCodes[i] != 0) {
                    pw.print(":");
                    pw.print(pkg.splitRevisionCodes[i]);
                }
            }
        }
        pw.print("]");
    }

    void dumpGidsLPr(PrintWriter pw, String prefix, int[] gids) {
        if (!ArrayUtils.isEmpty(gids)) {
            pw.print(prefix);
            pw.print("gids=");
            pw.println(PackageManagerService.arrayToString(gids));
        }
    }

    void dumpRuntimePermissionsLPr(PrintWriter pw, String prefix, ArraySet<String> permissionNames, List<PermissionState> permissionStates, boolean dumpAll) {
        if (!permissionStates.isEmpty() || dumpAll) {
            pw.print(prefix);
            pw.println("runtime permissions:");
            for (PermissionState permissionState : permissionStates) {
                if (permissionNames == null || (permissionNames.contains(permissionState.getName()) ^ 1) == 0) {
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
            return "";
        }
        flagsString.append(']');
        return flagsString.toString();
    }

    void dumpInstallPermissionsLPr(PrintWriter pw, String prefix, ArraySet<String> permissionNames, PermissionsState permissionsState) {
        List<PermissionState> permissionStates = permissionsState.getInstallPermissionStates();
        if (!permissionStates.isEmpty()) {
            pw.print(prefix);
            pw.println("install permissions:");
            for (PermissionState permissionState : permissionStates) {
                if (permissionNames == null || (permissionNames.contains(permissionState.getName()) ^ 1) == 0) {
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
        ArrayList<File> delAppsFileList = new ArrayList();
        try {
            delAppsFileList = HwCfgFilePolicy.getCfgFileList("xml/hw_subuser_delapps_config.xml", 0);
        } catch (NoClassDefFoundError e) {
            Log.d(TAG, "HwCfgFilePolicy NoClassDefFoundError");
        } finally {
            delAppsFileList.add(new File(getCustomizedFileName(FILE_SUB_USER_DELAPPS_LIST)));
        }
        for (File delAppsFile : delAppsFileList) {
            loadDelAppsFromXml(delAppsFile);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:25:0x0062 A:{SYNTHETIC, Splitter: B:25:0x0062} */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x00f9 A:{SYNTHETIC, Splitter: B:46:0x00f9} */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x00b1 A:{SYNTHETIC, Splitter: B:38:0x00b1} */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x013b A:{SYNTHETIC, Splitter: B:54:0x013b} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void loadDelAppsFromXml(File configFile) {
        IOException e;
        FileNotFoundException e2;
        Throwable th;
        XmlPullParserException e3;
        if (configFile.exists()) {
            FileInputStream stream = null;
            try {
                FileInputStream stream2 = new FileInputStream(configFile);
                try {
                    XmlPullParser parser = Xml.newPullParser();
                    parser.setInput(stream2, null);
                    int depth = parser.getDepth();
                    while (true) {
                        int type = parser.next();
                        if ((type != 3 || parser.getDepth() > depth) && type != 1) {
                            if (type == 2 && parser.getName().equals("del_app")) {
                                this.mDelAppLists.add(parser.getAttributeValue(0));
                            }
                        }
                    }
                    if (stream2 != null) {
                        try {
                            stream2.close();
                        } catch (IOException e4) {
                            Slog.e(TAG, "failed close stream " + e4);
                        }
                    }
                } catch (FileNotFoundException e5) {
                    e2 = e5;
                    stream = stream2;
                    try {
                        Slog.e(TAG, "file is not exist " + e2);
                        if (stream != null) {
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (stream != null) {
                        }
                        throw th;
                    }
                } catch (XmlPullParserException e6) {
                    e3 = e6;
                    stream = stream2;
                    Slog.e(TAG, "failed parsing " + configFile + " " + e3);
                    if (stream != null) {
                    }
                } catch (IOException e7) {
                    e4 = e7;
                    stream = stream2;
                    Slog.e(TAG, "failed parsing " + configFile + " " + e4);
                    if (stream != null) {
                    }
                } catch (Throwable th3) {
                    th = th3;
                    stream = stream2;
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (IOException e42) {
                            Slog.e(TAG, "failed close stream " + e42);
                        }
                    }
                    throw th;
                }
            } catch (FileNotFoundException e8) {
                e2 = e8;
                Slog.e(TAG, "file is not exist " + e2);
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e422) {
                        Slog.e(TAG, "failed close stream " + e422);
                    }
                }
            } catch (XmlPullParserException e9) {
                e3 = e9;
                Slog.e(TAG, "failed parsing " + configFile + " " + e3);
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e4222) {
                        Slog.e(TAG, "failed close stream " + e4222);
                    }
                }
            } catch (IOException e10) {
                e4222 = e10;
                Slog.e(TAG, "failed parsing " + configFile + " " + e4222);
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e42222) {
                        Slog.e(TAG, "failed close stream " + e42222);
                    }
                }
            }
        }
    }

    private String getCustomizedFileName(String xmlName) {
        String path = "/data/cust/xml/" + xmlName;
        if (new File(path).exists()) {
            return path;
        }
        return DIR_ETC_XML + xmlName;
    }
}
