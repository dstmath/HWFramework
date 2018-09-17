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
import com.android.internal.annotations.GuardedBy;
import com.android.internal.os.BackgroundThread;
import com.android.internal.os.HwBootFail;
import com.android.internal.os.InstallerConnection.InstallerException;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.JournaledFile;
import com.android.internal.util.XmlUtils;
import com.android.server.HwServiceFactory;
import com.android.server.am.HwBroadcastRadarUtil;
import com.android.server.am.ProcessList;
import com.android.server.display.RampAnimator;
import com.android.server.pm.PermissionsState.PermissionState;
import com.android.server.power.AbsPowerManagerService;
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
    private static final String ATTR_VOLUME_UUID = "volumeUuid";
    public static final int CURRENT_DATABASE_VERSION = 3;
    private static final boolean DEBUG_KERNEL = false;
    private static final boolean DEBUG_MU = false;
    private static final boolean DEBUG_STOPPED = false;
    private static final String DIR_CUST_XML = "/data/cust/xml/";
    private static final String DIR_ETC_XML = "/system/etc/xml/";
    private static final String FILE_SUB_USER_DELAPPS_LIST = "hw_subuser_delapps_config.xml";
    static final Object[] FLAG_DUMP_SPEC = null;
    static final Object[] HW_FLAG_DUMP_SPEC = null;
    private static int PRE_M_APP_INFO_FLAG_CANT_SAVE_STATE = 0;
    private static int PRE_M_APP_INFO_FLAG_FORWARD_LOCK = 0;
    private static int PRE_M_APP_INFO_FLAG_HIDDEN = 0;
    private static int PRE_M_APP_INFO_FLAG_PRIVILEGED = 0;
    static final Object[] PRIVATE_FLAG_DUMP_SPEC = null;
    private static final String RUNTIME_PERMISSIONS_FILE_NAME = "runtime-permissions.xml";
    private static final String TAG = "PackageSettings";
    private static final String TAG_ALL_INTENT_FILTER_VERIFICATION = "all-intent-filter-verifications";
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
    private static final String TAG_VERSION = "version";
    private static final int USER_RUNTIME_GRANT_MASK = 11;
    private static int mFirstAvailableUid;
    private static AtomicBoolean mIsCheckDelAppsFinished;
    private final File mBackupSettingsFilename;
    private final File mBackupStoppedPackagesFilename;
    final SparseArray<CrossProfileIntentResolver> mCrossProfileIntentResolvers;
    final SparseArray<String> mDefaultBrowserApp;
    final SparseArray<String> mDefaultDialerApp;
    private ArrayList<String> mDelAppLists;
    private final ArrayMap<String, PackageSetting> mDisabledSysPackages;
    final ArraySet<String> mInstallerPackages;
    private final ArrayMap<String, Integer> mKernelMapping;
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
    private final ArrayList<PendingPackage> mPendingPackages;
    final ArrayMap<String, BasePermission> mPermissionTrees;
    final ArrayMap<String, BasePermission> mPermissions;
    final SparseArray<PersistentPreferredIntentResolver> mPersistentPreferredActivities;
    final SparseArray<PreferredIntentResolver> mPreferredActivities;
    Boolean mReadExternalStorageEnforced;
    final StringBuilder mReadMessages;
    final ArrayMap<String, String> mRenamedPackages;
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
        private final SparseBooleanArray mDefaultPermissionsGranted;
        @GuardedBy("mLock")
        private final SparseArray<String> mFingerprints;
        private final Handler mHandler;
        @GuardedBy("mLock")
        private final SparseLongArray mLastNotWrittenMutationTimesMillis;
        private final Object mLock;
        @GuardedBy("mLock")
        private final SparseBooleanArray mWriteScheduled;

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
            this.mHandler = new MyHandler();
            this.mWriteScheduled = new SparseBooleanArray();
            this.mLastNotWrittenMutationTimesMillis = new SparseLongArray();
            this.mFingerprints = new SparseArray();
            this.mDefaultPermissionsGranted = new SparseBooleanArray();
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

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private void writePermissionsSync(int userId) {
            int i;
            AtomicFile destination = new AtomicFile(Settings.this.getUserRuntimePermissionsFile(userId));
            ArrayMap<String, List<PermissionState>> permissionsForPackage = new ArrayMap();
            ArrayMap<String, List<PermissionState>> permissionsForSharedUser = new ArrayMap();
            synchronized (this.mLock) {
                this.mWriteScheduled.delete(userId);
                int packageCount = Settings.this.mPackages.size();
                for (i = 0; i < packageCount; i++) {
                    List<PermissionState> permissionsStates;
                    String packageName = (String) Settings.this.mPackages.keyAt(i);
                    PackageSetting packageSetting = (PackageSetting) Settings.this.mPackages.valueAt(i);
                    if (packageSetting.sharedUser == null) {
                        permissionsStates = packageSetting.getPermissionsState().getRuntimePermissionStates(userId);
                        if (!permissionsStates.isEmpty()) {
                            permissionsForPackage.put(packageName, permissionsStates);
                        }
                    }
                }
                int sharedUserCount = Settings.this.mSharedUsers.size();
                for (i = 0; i < sharedUserCount; i++) {
                    String sharedUserName = (String) Settings.this.mSharedUsers.keyAt(i);
                    permissionsStates = ((SharedUserSetting) Settings.this.mSharedUsers.valueAt(i)).getPermissionsState().getRuntimePermissionStates(userId);
                    if (!permissionsStates.isEmpty()) {
                        permissionsForSharedUser.put(sharedUserName, permissionsStates);
                    }
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
                    serializer.startTag(null, Settings.TAG_PACKAGE);
                    serializer.attribute(null, Settings.ATTR_NAME, packageName);
                    writePermissions(serializer, permissionStates);
                    serializer.endTag(null, Settings.TAG_PACKAGE);
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
            } catch (Throwable th) {
                IoUtils.closeQuietly(autoCloseable);
            }
        }

        private void onUserRemoved(int userId) {
            this.mHandler.removeMessages(userId);
            for (PackageSetting sb : Settings.this.mPackages.values()) {
                revokeRuntimePermissionsAndClearFlags(sb, userId);
            }
            for (SharedUserSetting sb2 : Settings.this.mSharedUsers.values()) {
                revokeRuntimePermissionsAndClearFlags(sb2, userId);
            }
        }

        private void revokeRuntimePermissionsAndClearFlags(SettingBase sb, int userId) {
            PermissionsState permissionsState = sb.getPermissionsState();
            for (PermissionState permissionState : permissionsState.getRuntimePermissionStates(userId)) {
                BasePermission bp = (BasePermission) Settings.this.mPermissions.get(permissionState.getName());
                if (bp != null) {
                    permissionsState.revokeRuntimePermission(bp, userId);
                    permissionsState.updatePermissionFlags(bp, userId, RampAnimator.DEFAULT_MAX_BRIGHTNESS, 0);
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
                    } catch (Exception e) {
                        throw new IllegalStateException("Failed parsing permissions file: " + permissionsFile, e);
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
                if (type == Settings.CURRENT_DATABASE_VERSION && parser.getDepth() <= outerDepth) {
                    return;
                }
                if (!(type == Settings.CURRENT_DATABASE_VERSION || type == 4)) {
                    String name = parser.getName();
                    if (name.equals(Settings.TAG_RUNTIME_PERMISSIONS)) {
                        String fingerprint = parser.getAttributeValue(null, Settings.ATTR_FINGERPRINT);
                        this.mFingerprints.put(userId, fingerprint);
                        this.mDefaultPermissionsGranted.put(userId, Build.FINGERPRINT.equals(fingerprint));
                    } else if (name.equals(Settings.TAG_PACKAGE)) {
                        name = parser.getAttributeValue(null, Settings.ATTR_NAME);
                        PackageSetting ps = (PackageSetting) Settings.this.mPackages.get(name);
                        if (ps == null) {
                            Slog.w("PackageManager", "Unknown package:" + name);
                            XmlUtils.skipCurrentTag(parser);
                        } else {
                            parsePermissionsLPr(parser, ps.getPermissionsState(), userId);
                        }
                    } else if (name.equals(Settings.TAG_SHARED_USER)) {
                        name = parser.getAttributeValue(null, Settings.ATTR_NAME);
                        SharedUserSetting sus = (SharedUserSetting) Settings.this.mSharedUsers.get(name);
                        if (sus == null) {
                            Slog.w("PackageManager", "Unknown shared user:" + name);
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
                if (type == Settings.CURRENT_DATABASE_VERSION && parser.getDepth() <= outerDepth) {
                    return;
                }
                if (!(type == Settings.CURRENT_DATABASE_VERSION || type == 4 || !parser.getName().equals(Settings.TAG_PERMISSION_ENTRY))) {
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
                if (type == Settings.CURRENT_DATABASE_VERSION && parser.getDepth() <= outerDepth) {
                    return;
                }
                if (!(type == Settings.CURRENT_DATABASE_VERSION || type == 4 || !parser.getName().equals(Settings.TAG_ITEM))) {
                    String name = parser.getAttributeValue(null, Settings.ATTR_NAME);
                    BasePermission bp = (BasePermission) Settings.this.mPermissions.get(name);
                    if (bp == null) {
                        Slog.w("PackageManager", "Unknown permission:" + name);
                        XmlUtils.skipCurrentTag(parser);
                    } else {
                        boolean parseBoolean;
                        String grantedStr = parser.getAttributeValue(null, Settings.ATTR_GRANTED);
                        if (grantedStr != null) {
                            parseBoolean = Boolean.parseBoolean(grantedStr);
                        } else {
                            parseBoolean = true;
                        }
                        String flagsStr = parser.getAttributeValue(null, Settings.ATTR_FLAGS);
                        int flags = flagsStr != null ? Integer.parseInt(flagsStr, 16) : 0;
                        if (parseBoolean) {
                            permissionsState.grantRuntimePermission(bp, userId);
                            permissionsState.updatePermissionFlags(bp, userId, RampAnimator.DEFAULT_MAX_BRIGHTNESS, flags);
                        } else {
                            permissionsState.updatePermissionFlags(bp, userId, RampAnimator.DEFAULT_MAX_BRIGHTNESS, flags);
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
            this.databaseVersion = Settings.CURRENT_DATABASE_VERSION;
            this.fingerprint = Build.FINGERPRINT;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.pm.Settings.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.pm.Settings.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.pm.Settings.<clinit>():void");
    }

    private static java.lang.String permissionFlagsToString(java.lang.String r1, int r2) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.pm.Settings.permissionFlagsToString(java.lang.String, int):java.lang.String
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.pm.Settings.permissionFlagsToString(java.lang.String, int):java.lang.String");
    }

    private void readPackageLPw(org.xmlpull.v1.XmlPullParser r1) throws org.xmlpull.v1.XmlPullParserException, java.io.IOException {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.pm.Settings.readPackageLPw(org.xmlpull.v1.XmlPullParser):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.pm.Settings.readPackageLPw(org.xmlpull.v1.XmlPullParser):void");
    }

    Settings(Object lock) {
        this(Environment.getDataDirectory(), lock);
    }

    Settings(File dataDir, Object lock) {
        this.mPackages = new ArrayMap();
        this.mInstallerPackages = new ArraySet();
        this.mKernelMapping = new ArrayMap();
        this.mDisabledSysPackages = new ArrayMap();
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
        FileUtils.setPermissions(this.mPackageListFilename, 416, ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE, 1032);
        File kernelDir = new File("/config/sdcardfs");
        if (!kernelDir.exists()) {
            kernelDir = null;
        }
        this.mKernelMappingFilename = kernelDir;
        this.mStoppedPackagesFilename = new File(this.mSystemDir, "packages-stopped.xml");
        this.mBackupStoppedPackagesFilename = new File(this.mSystemDir, "packages-stopped-backup.xml");
    }

    PackageSetting getPackageLPw(Package pkg, PackageSetting origPackage, String realName, SharedUserSetting sharedUser, File codePath, File resourcePath, String legacyNativeLibraryPathString, String primaryCpuAbi, String secondaryCpuAbi, int pkgFlags, int pkgPrivateFlags, UserHandle user, boolean add) {
        String name = pkg.packageName;
        String str = pkg.parentPackage != null ? pkg.parentPackage.packageName : null;
        List list = null;
        if (pkg.childPackages != null) {
            int childCount = pkg.childPackages.size();
            list = new ArrayList(childCount);
            for (int i = 0; i < childCount; i++) {
                list.add(((Package) pkg.childPackages.get(i)).packageName);
            }
        }
        return getPackageLPw(name, origPackage, realName, sharedUser, codePath, resourcePath, legacyNativeLibraryPathString, primaryCpuAbi, secondaryCpuAbi, pkg.mVersionCode, pkgFlags, pkgPrivateFlags, user, add, true, str, list);
    }

    PackageSetting peekPackageLPr(String name) {
        return (PackageSetting) this.mPackages.get(name);
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
                        perms.updatePermissionFlags(bp, userId, USER_RUNTIME_GRANT_MASK, grant.grantBits);
                    }
                }
                grantsByPackage.remove(packageName);
                if (grantsByPackage.size() < 1) {
                    this.mRestoredUserGrants.remove(userId);
                }
                writeRuntimePermissionsForUserLPr(userId, DEBUG_STOPPED);
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

    SharedUserSetting getSharedUserLPw(String name, int pkgFlags, int pkgPrivateFlags, boolean create) {
        SharedUserSetting s = (SharedUserSetting) this.mSharedUsers.get(name);
        if (s == null) {
            if (!create) {
                return null;
            }
            s = new SharedUserSetting(name, pkgFlags, pkgPrivateFlags);
            s.userId = newUserIdLPw(s);
            Log.i("PackageManager", "New shared user " + name + ": id=" + s.userId);
            if (s.userId >= 0) {
                this.mSharedUsers.put(name, s);
            }
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
            return DEBUG_STOPPED;
        } else if (((PackageSetting) this.mDisabledSysPackages.get(name)) != null || p.pkg == null || !p.pkg.isSystemApp() || p.pkg.isUpdatedSystemApp()) {
            return DEBUG_STOPPED;
        } else {
            if (!(p.pkg == null || p.pkg.applicationInfo == null)) {
                ApplicationInfo applicationInfo = p.pkg.applicationInfo;
                applicationInfo.flags |= DumpState.DUMP_PACKAGES;
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
        PackageSetting ret = addPackageLPw(name, p.realName, p.codePath, p.resourcePath, p.legacyNativeLibraryPathString, p.primaryCpuAbiString, p.secondaryCpuAbiString, p.cpuAbiOverrideString, p.appId, p.versionCode, p.pkgFlags, p.pkgPrivateFlags, p.parentPackageName, p.childPackageNames);
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

    PackageSetting addPackageLPw(String name, String realName, File codePath, File resourcePath, String legacyNativeLibraryPathString, String primaryCpuAbiString, String secondaryCpuAbiString, String cpuAbiOverrideString, int uid, int vc, int pkgFlags, int pkgPrivateFlags, String parentPackageName, List<String> childPackageNames) {
        PackageSetting p = (PackageSetting) this.mPackages.get(name);
        if (p == null) {
            p = new PackageSetting(name, realName, codePath, resourcePath, legacyNativeLibraryPathString, primaryCpuAbiString, secondaryCpuAbiString, cpuAbiOverrideString, vc, pkgFlags, pkgPrivateFlags, parentPackageName, childPackageNames);
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
                    bp.setGids(null, DEBUG_STOPPED);
                }
            }
            i++;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private PackageSetting getPackageLPw(String name, PackageSetting origPackage, String realName, SharedUserSetting sharedUser, File codePath, File resourcePath, String legacyNativeLibraryPathString, String primaryCpuAbiString, String secondaryCpuAbiString, int vc, int pkgFlags, int pkgPrivateFlags, UserHandle installUser, boolean add, boolean allowInstall, String parentPackage, List<String> childPackageNames) {
        PackageSetting p = (PackageSetting) this.mPackages.get(name);
        UserManagerService userManager = UserManagerService.getInstance();
        if (p != null) {
            p.primaryCpuAbiString = primaryCpuAbiString;
            p.secondaryCpuAbiString = secondaryCpuAbiString;
            if (childPackageNames != null) {
                p.childPackageNames = new ArrayList(childPackageNames);
            }
            if (!p.codePath.equals(codePath)) {
                if ((p.pkgFlags & 1) != 0) {
                    Slog.w("PackageManager", "Trying to update system app code path from " + p.codePathString + " to " + codePath.toString());
                } else {
                    Slog.i("PackageManager", "Package " + name + " codePath changed from " + p.codePath + " to " + codePath + "; Retaining data and using new");
                    if ((pkgFlags & 1) != 0 && getDisabledSystemPkgLPr(name) == null) {
                        List<UserInfo> allUserInfos = getAllUsers();
                        if (allUserInfos != null) {
                            for (UserInfo userInfo : allUserInfos) {
                                p.setInstalled(true, userInfo.id);
                            }
                        }
                    }
                    p.legacyNativeLibraryPathString = legacyNativeLibraryPathString;
                }
            }
            if (p.sharedUser != sharedUser) {
                PackageManagerService.reportSettingsProblem(5, "Package " + name + " shared user changed from " + (p.sharedUser != null ? p.sharedUser.name : "<nothing>") + " to " + (sharedUser != null ? sharedUser.name : "<nothing>") + "; replacing with new");
                p = null;
            } else {
                p.pkgFlags |= pkgFlags & 1;
                p.pkgPrivateFlags |= pkgPrivateFlags & 8;
            }
        }
        List<UserInfo> users;
        if (p == null) {
            if (origPackage != null) {
                p = new PackageSetting(origPackage.name, name, codePath, resourcePath, legacyNativeLibraryPathString, primaryCpuAbiString, secondaryCpuAbiString, null, vc, pkgFlags, pkgPrivateFlags, parentPackage, childPackageNames);
                PackageSignatures s = p.signatures;
                p.copyFrom(origPackage);
                p.signatures = s;
                p.sharedUser = origPackage.sharedUser;
                p.appId = origPackage.appId;
                p.origPackage = origPackage;
                p.getPermissionsState().copyFrom(origPackage.getPermissionsState());
                this.mRenamedPackages.put(name, origPackage.name);
                name = origPackage.name;
                p.setTimeStamp(codePath.lastModified());
            } else {
                p = new PackageSetting(name, realName, codePath, resourcePath, legacyNativeLibraryPathString, primaryCpuAbiString, secondaryCpuAbiString, null, vc, pkgFlags, pkgPrivateFlags, parentPackage, childPackageNames);
                p.setTimeStamp(codePath.lastModified());
                p.sharedUser = sharedUser;
                if ((pkgFlags & 1) == 0 || HwServiceFactory.isCustedCouldStopped(name, DEBUG_STOPPED, DEBUG_STOPPED)) {
                    users = getAllUsers();
                    int installUserId = installUser != null ? installUser.getIdentifier() : 0;
                    if (users != null && allowInstall) {
                        for (UserInfo user : users) {
                            boolean installed;
                            if (installUser != null) {
                                if (installUserId == -1) {
                                }
                                installed = installUserId == user.id ? true : DEBUG_STOPPED;
                                p.setUserState(user.id, 0, 0, installed, true, true, DEBUG_STOPPED, DEBUG_STOPPED, null, null, null, DEBUG_STOPPED, 0, 0);
                                writePackageRestrictionsLPr(user.id);
                            }
                            installed = true;
                            p.setUserState(user.id, 0, 0, installed, true, true, DEBUG_STOPPED, DEBUG_STOPPED, null, null, null, DEBUG_STOPPED, 0, 0);
                            writePackageRestrictionsLPr(user.id);
                        }
                    }
                }
                if (sharedUser != null) {
                    p.appId = sharedUser.userId;
                } else {
                    PackageSetting dis = (PackageSetting) this.mDisabledSysPackages.get(name);
                    if (dis != null) {
                        if (dis.signatures.mSignatures != null) {
                            p.signatures.mSignatures = (Signature[]) dis.signatures.mSignatures.clone();
                        }
                        p.appId = dis.appId;
                        p.getPermissionsState().copyFrom(dis.getPermissionsState());
                        users = getAllUsers();
                        if (users != null) {
                            for (UserInfo userInfo2 : users) {
                                int userId = userInfo2.id;
                                p.setDisabledComponentsCopy(dis.getDisabledComponents(userId), userId);
                                p.setEnabledComponentsCopy(dis.getEnabledComponents(userId), userId);
                            }
                        }
                        addUserIdLPw(p.appId, p, name);
                    } else {
                        p.appId = newUserIdLPw(p);
                    }
                }
            }
            if (p.appId < 0) {
                PackageManagerService.reportSettingsProblem(5, "Package " + name + " could not be assigned a valid uid");
                return null;
            } else if (add) {
                addPackageSettingLPw(p, name, sharedUser);
            }
        } else if (installUser != null && allowInstall) {
            users = getAllUsers();
            if (users != null) {
                for (UserInfo user2 : users) {
                    if (installUser.getIdentifier() == -1) {
                    }
                    if (installUser.getIdentifier() != user2.id) {
                    }
                    if (!p.getInstalled(user2.id)) {
                        p.setInstalled(true, user2.id);
                        writePackageRestrictionsLPr(user2.id);
                    }
                }
            }
        }
        return p;
    }

    boolean isAdbInstallDisallowed(UserManagerService userManager, int userId) {
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
        addPackageSettingLPw(p, pkg.packageName, p.sharedUser);
    }

    private void addPackageSettingLPw(PackageSetting p, String name, SharedUserSetting sharedUser) {
        this.mPackages.put(name, p);
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
        IntentFilterVerificationInfo ivi = (IntentFilterVerificationInfo) this.mRestoredIntentFilterVerifications.get(name);
        if (ivi != null) {
            this.mRestoredIntentFilterVerifications.remove(name);
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
                    boolean used = DEBUG_STOPPED;
                    for (PackageSetting pkg : sus.packages) {
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
                            boolean reqByDisabledSysPkg = DEBUG_STOPPED;
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
                        permissionsState.updatePermissionFlags(bp, userId, RampAnimator.DEFAULT_MAX_BRIGHTNESS, 0);
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
            return DEBUG_STOPPED;
        }
        if (uid >= AbsPowerManagerService.MIN_COVER_SCREEN_OFF_TIMEOUT) {
            int index = uid - 10000;
            for (int N = this.mUserIds.size(); index >= N; N++) {
                this.mUserIds.add(null);
            }
            if (this.mUserIds.get(index) != null) {
                PackageManagerService.reportSettingsProblem(6, "Adding duplicate user id: " + uid + " name=" + name);
                return DEBUG_STOPPED;
            }
            this.mUserIds.set(index, obj);
        } else if (this.mOtherUserIds.get(uid) != null) {
            PackageManagerService.reportSettingsProblem(6, "Adding duplicate shared id: " + uid + " name=" + name);
            return DEBUG_STOPPED;
        } else {
            this.mOtherUserIds.put(uid, obj);
        }
        return true;
    }

    public Object getUserIdLPr(int uid) {
        if (uid < AbsPowerManagerService.MIN_COVER_SCREEN_OFF_TIMEOUT) {
            return this.mOtherUserIds.get(uid);
        }
        int index = uid - 10000;
        return index < this.mUserIds.size() ? this.mUserIds.get(index) : null;
    }

    private void removeUserIdLPw(int uid) {
        if (uid >= AbsPowerManagerService.MIN_COVER_SCREEN_OFF_TIMEOUT) {
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
        if (uid >= AbsPowerManagerService.MIN_COVER_SCREEN_OFF_TIMEOUT) {
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

    IntentFilterVerificationInfo createIntentFilterVerificationIfNeededLPw(String packageName, ArrayList<String> domains) {
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
            return DEBUG_STOPPED;
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
            if (!(ivi == null || TextUtils.isEmpty(ivi.getPackageName()) || !ivi.getPackageName().equalsIgnoreCase(packageName))) {
                result.add(ivi);
            }
        }
        return result;
    }

    boolean removeIntentFilterVerificationLPw(String packageName, int userId) {
        PackageSetting ps = (PackageSetting) this.mPackages.get(packageName);
        if (ps == null) {
            return DEBUG_STOPPED;
        }
        ps.clearDomainVerificationStatusForUser(userId);
        return true;
    }

    boolean removeIntentFilterVerificationLPw(String packageName, int[] userIds) {
        boolean result = DEBUG_STOPPED;
        for (int userId : userIds) {
            result |= removeIntentFilterVerificationLPw(packageName, userId);
        }
        return result;
    }

    boolean setDefaultBrowserPackageNameLPw(String packageName, int userId) {
        if (userId == -1) {
            return DEBUG_STOPPED;
        }
        this.mDefaultBrowserApp.put(userId, packageName);
        writePackageRestrictionsLPr(userId);
        return true;
    }

    String getDefaultBrowserPackageNameLPw(int userId) {
        return userId == -1 ? null : (String) this.mDefaultBrowserApp.get(userId);
    }

    boolean setDefaultDialerPackageNameLPw(String packageName, int userId) {
        if (userId == -1) {
            return DEBUG_STOPPED;
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
        List<UserInfo> users = getAllUsers();
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
        ver.forceCurrent();
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
            if (type == CURRENT_DATABASE_VERSION && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == CURRENT_DATABASE_VERSION || type == 4)) {
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
            if (type == CURRENT_DATABASE_VERSION && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == CURRENT_DATABASE_VERSION || type == 4)) {
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
            if (type == CURRENT_DATABASE_VERSION && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == CURRENT_DATABASE_VERSION || type == 4)) {
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
        IntentFilterVerificationInfo ivi = new IntentFilterVerificationInfo(parser);
        packageSetting.setIntentFilterVerificationInfo(ivi);
        Log.d(TAG, "Read domain verification for package: " + ivi.getPackageName());
    }

    private void readRestoredIntentFilterVerifications(XmlPullParser parser) throws XmlPullParserException, IOException {
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1) {
                return;
            }
            if (type == CURRENT_DATABASE_VERSION && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == CURRENT_DATABASE_VERSION || type == 4)) {
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
            if (type == CURRENT_DATABASE_VERSION && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == CURRENT_DATABASE_VERSION || type == 4)) {
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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void readPackageRestrictionsLPr(int userId) {
        InputStream fileInputStream;
        InputStream str;
        XmlPullParser parser;
        int type;
        int maxAppLinkGeneration;
        int outerDepth;
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
                        str2 = str;
                        try {
                            parser = Xml.newPullParser();
                            parser.setInput(str2, StandardCharsets.UTF_8.name());
                            do {
                                type = parser.next();
                                if (type != 2) {
                                    break;
                                }
                                if (type != 2) {
                                    maxAppLinkGeneration = 0;
                                    outerDepth = parser.getDepth();
                                    while (true) {
                                        type = parser.next();
                                        if (type == 1) {
                                            break;
                                        }
                                        break;
                                        str2.close();
                                        this.mNextAppLinkGeneration.put(userId, maxAppLinkGeneration + 1);
                                    }
                                } else {
                                    this.mReadMessages.append("No start tag found in package restrictions file\n");
                                    PackageManagerService.reportSettingsProblem(5, "No start tag found in package manager stopped packages");
                                    return;
                                }
                            } while (type != 1);
                            if (type != 2) {
                                this.mReadMessages.append("No start tag found in package restrictions file\n");
                                PackageManagerService.reportSettingsProblem(5, "No start tag found in package manager stopped packages");
                                return;
                            }
                            maxAppLinkGeneration = 0;
                            outerDepth = parser.getDepth();
                            while (true) {
                                type = parser.next();
                                if (type == 1) {
                                    break;
                                }
                                break;
                                str2.close();
                                this.mNextAppLinkGeneration.put(userId, maxAppLinkGeneration + 1);
                            }
                        } catch (XmlPullParserException e4) {
                            e = e4;
                        } catch (IOException e5) {
                            e2 = e5;
                        }
                    } else {
                        try {
                            if (userPackagesStateFile.exists()) {
                                this.mReadMessages.append("No stopped packages file found\n");
                                PackageManagerService.reportSettingsProblem(4, "No stopped packages file; assuming all started");
                                for (PackageSetting pkg : this.mPackages.values()) {
                                    pkg.setUserState(userId, 0, 0, true, DEBUG_STOPPED, DEBUG_STOPPED, DEBUG_STOPPED, DEBUG_STOPPED, null, null, null, DEBUG_STOPPED, 0, 0);
                                }
                            }
                            fileInputStream = new FileInputStream(userPackagesStateFile);
                            parser = Xml.newPullParser();
                            parser.setInput(str2, StandardCharsets.UTF_8.name());
                            do {
                                type = parser.next();
                                if (type != 2) {
                                    break;
                                }
                                if (type != 2) {
                                    maxAppLinkGeneration = 0;
                                    outerDepth = parser.getDepth();
                                    while (true) {
                                        type = parser.next();
                                        if (type == 1) {
                                            break;
                                        }
                                        break;
                                        str2.close();
                                        this.mNextAppLinkGeneration.put(userId, maxAppLinkGeneration + 1);
                                    }
                                } else {
                                    this.mReadMessages.append("No start tag found in package restrictions file\n");
                                    PackageManagerService.reportSettingsProblem(5, "No start tag found in package manager stopped packages");
                                    return;
                                }
                            } while (type != 1);
                            if (type != 2) {
                                this.mReadMessages.append("No start tag found in package restrictions file\n");
                                PackageManagerService.reportSettingsProblem(5, "No start tag found in package manager stopped packages");
                                return;
                            }
                            maxAppLinkGeneration = 0;
                            outerDepth = parser.getDepth();
                            while (true) {
                                type = parser.next();
                                if (type == 1) {
                                    break;
                                }
                                break;
                                str2.close();
                                this.mNextAppLinkGeneration.put(userId, maxAppLinkGeneration + 1);
                            }
                        } catch (XmlPullParserException e6) {
                            e = e6;
                            this.mReadMessages.append("Error reading: ").append(e.toString());
                            PackageManagerService.reportSettingsProblem(6, "Error reading stopped packages: " + e);
                            Slog.wtf("PackageManager", "Error reading package manager stopped packages", e);
                        } catch (IOException e7) {
                            e2 = e7;
                            this.mReadMessages.append("Error reading: ").append(e2.toString());
                            PackageManagerService.reportSettingsProblem(6, "Error reading settings: " + e2);
                            Slog.wtf("PackageManager", "Error reading package manager stopped packages", e2);
                        }
                    }
                }
            } catch (IOException e8) {
                str = str2;
                if (str == null) {
                    str2 = str;
                    parser = Xml.newPullParser();
                    parser.setInput(str2, StandardCharsets.UTF_8.name());
                    do {
                        type = parser.next();
                        if (type != 2) {
                            break;
                        }
                        if (type != 2) {
                            maxAppLinkGeneration = 0;
                            outerDepth = parser.getDepth();
                            while (true) {
                                type = parser.next();
                                if (type == 1) {
                                    break;
                                }
                                break;
                                str2.close();
                                this.mNextAppLinkGeneration.put(userId, maxAppLinkGeneration + 1);
                            }
                        } else {
                            this.mReadMessages.append("No start tag found in package restrictions file\n");
                            PackageManagerService.reportSettingsProblem(5, "No start tag found in package manager stopped packages");
                            return;
                        }
                    } while (type != 1);
                    if (type != 2) {
                        this.mReadMessages.append("No start tag found in package restrictions file\n");
                        PackageManagerService.reportSettingsProblem(5, "No start tag found in package manager stopped packages");
                        return;
                    }
                    maxAppLinkGeneration = 0;
                    outerDepth = parser.getDepth();
                    while (true) {
                        type = parser.next();
                        if (type == 1) {
                            break;
                        }
                        break;
                        str2.close();
                        this.mNextAppLinkGeneration.put(userId, maxAppLinkGeneration + 1);
                    }
                } else if (userPackagesStateFile.exists()) {
                    this.mReadMessages.append("No stopped packages file found\n");
                    PackageManagerService.reportSettingsProblem(4, "No stopped packages file; assuming all started");
                    while (pkg$iterator.hasNext()) {
                        pkg.setUserState(userId, 0, 0, true, DEBUG_STOPPED, DEBUG_STOPPED, DEBUG_STOPPED, DEBUG_STOPPED, null, null, null, DEBUG_STOPPED, 0, 0);
                    }
                } else {
                    fileInputStream = new FileInputStream(userPackagesStateFile);
                    parser = Xml.newPullParser();
                    parser.setInput(str2, StandardCharsets.UTF_8.name());
                    do {
                        type = parser.next();
                        if (type != 2) {
                            break;
                        }
                        if (type != 2) {
                            maxAppLinkGeneration = 0;
                            outerDepth = parser.getDepth();
                            while (true) {
                                type = parser.next();
                                if (type == 1) {
                                    break;
                                }
                                break;
                                str2.close();
                                this.mNextAppLinkGeneration.put(userId, maxAppLinkGeneration + 1);
                            }
                        } else {
                            this.mReadMessages.append("No start tag found in package restrictions file\n");
                            PackageManagerService.reportSettingsProblem(5, "No start tag found in package manager stopped packages");
                            return;
                        }
                    } while (type != 1);
                    if (type != 2) {
                        this.mReadMessages.append("No start tag found in package restrictions file\n");
                        PackageManagerService.reportSettingsProblem(5, "No start tag found in package manager stopped packages");
                        return;
                    }
                    maxAppLinkGeneration = 0;
                    outerDepth = parser.getDepth();
                    while (true) {
                        type = parser.next();
                        if (type == 1) {
                            break;
                        }
                        break;
                        str2.close();
                        this.mNextAppLinkGeneration.put(userId, maxAppLinkGeneration + 1);
                    }
                }
            }
        }
        str = null;
        if (str == null) {
            str2 = str;
            parser = Xml.newPullParser();
            parser.setInput(str2, StandardCharsets.UTF_8.name());
            do {
                type = parser.next();
                if (type != 2) {
                    break;
                }
                if (type != 2) {
                    maxAppLinkGeneration = 0;
                    outerDepth = parser.getDepth();
                    while (true) {
                        type = parser.next();
                        if (type == 1) {
                            break;
                        }
                        break;
                        str2.close();
                        this.mNextAppLinkGeneration.put(userId, maxAppLinkGeneration + 1);
                    }
                } else {
                    this.mReadMessages.append("No start tag found in package restrictions file\n");
                    PackageManagerService.reportSettingsProblem(5, "No start tag found in package manager stopped packages");
                    return;
                }
            } while (type != 1);
            if (type != 2) {
                this.mReadMessages.append("No start tag found in package restrictions file\n");
                PackageManagerService.reportSettingsProblem(5, "No start tag found in package manager stopped packages");
                return;
            }
            maxAppLinkGeneration = 0;
            outerDepth = parser.getDepth();
            while (true) {
                type = parser.next();
                if (type == 1) {
                    break;
                }
                break;
                str2.close();
                this.mNextAppLinkGeneration.put(userId, maxAppLinkGeneration + 1);
            }
        } else if (userPackagesStateFile.exists()) {
            this.mReadMessages.append("No stopped packages file found\n");
            PackageManagerService.reportSettingsProblem(4, "No stopped packages file; assuming all started");
            while (pkg$iterator.hasNext()) {
                pkg.setUserState(userId, 0, 0, true, DEBUG_STOPPED, DEBUG_STOPPED, DEBUG_STOPPED, DEBUG_STOPPED, null, null, null, DEBUG_STOPPED, 0, 0);
            }
        } else {
            fileInputStream = new FileInputStream(userPackagesStateFile);
            parser = Xml.newPullParser();
            parser.setInput(str2, StandardCharsets.UTF_8.name());
            do {
                type = parser.next();
                if (type != 2) {
                    break;
                }
            } while (type != 1);
            if (type != 2) {
                this.mReadMessages.append("No start tag found in package restrictions file\n");
                PackageManagerService.reportSettingsProblem(5, "No start tag found in package manager stopped packages");
                return;
            }
            maxAppLinkGeneration = 0;
            outerDepth = parser.getDepth();
            while (true) {
                type = parser.next();
                if (type == 1 || (type == CURRENT_DATABASE_VERSION && parser.getDepth() <= outerDepth)) {
                    str2.close();
                    this.mNextAppLinkGeneration.put(userId, maxAppLinkGeneration + 1);
                } else if (!(type == CURRENT_DATABASE_VERSION || type == 4)) {
                    String tagName = parser.getName();
                    if (tagName.equals(TAG_PACKAGE)) {
                        String name = parser.getAttributeValue(null, ATTR_NAME);
                        PackageSetting ps = (PackageSetting) this.mPackages.get(name);
                        if (ps == null) {
                            Slog.w("PackageManager", "No package known for stopped package " + name);
                            XmlUtils.skipCurrentTag(parser);
                        } else {
                            long ceDataInode = XmlUtils.readLongAttribute(parser, ATTR_CE_DATA_INODE, 0);
                            boolean installed = XmlUtils.readBooleanAttribute(parser, ATTR_INSTALLED, true);
                            boolean stopped = XmlUtils.readBooleanAttribute(parser, ATTR_STOPPED, DEBUG_STOPPED);
                            boolean notLaunched = XmlUtils.readBooleanAttribute(parser, ATTR_NOT_LAUNCHED, DEBUG_STOPPED);
                            String blockedStr = parser.getAttributeValue(null, ATTR_BLOCKED);
                            boolean parseBoolean = blockedStr == null ? DEBUG_STOPPED : Boolean.parseBoolean(blockedStr);
                            String hiddenStr = parser.getAttributeValue(null, ATTR_HIDDEN);
                            if (hiddenStr != null) {
                                parseBoolean = Boolean.parseBoolean(hiddenStr);
                            }
                            boolean suspended = XmlUtils.readBooleanAttribute(parser, ATTR_SUSPENDED, DEBUG_STOPPED);
                            boolean blockUninstall = XmlUtils.readBooleanAttribute(parser, ATTR_BLOCK_UNINSTALL, DEBUG_STOPPED);
                            int enabled = XmlUtils.readIntAttribute(parser, ATTR_ENABLED, 0);
                            String enabledCaller = parser.getAttributeValue(null, ATTR_ENABLED_CALLER);
                            int verifState = XmlUtils.readIntAttribute(parser, ATTR_DOMAIN_VERIFICATON_STATE, 0);
                            int linkGeneration = XmlUtils.readIntAttribute(parser, ATTR_APP_LINK_GENERATION, 0);
                            if (linkGeneration > maxAppLinkGeneration) {
                                maxAppLinkGeneration = linkGeneration;
                            }
                            ArraySet enabledComponents = null;
                            ArraySet disabledComponents = null;
                            int packageDepth = parser.getDepth();
                            while (true) {
                                type = parser.next();
                                if (type == 1 || (type == CURRENT_DATABASE_VERSION && parser.getDepth() <= packageDepth)) {
                                    ps.setUserState(userId, ceDataInode, enabled, installed, stopped, notLaunched, parseBoolean, suspended, enabledCaller, enabledComponents, disabledComponents, blockUninstall, verifState, linkGeneration);
                                } else if (!(type == CURRENT_DATABASE_VERSION || type == 4)) {
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
                            ps.setUserState(userId, ceDataInode, enabled, installed, stopped, notLaunched, parseBoolean, suspended, enabledCaller, enabledComponents, disabledComponents, blockUninstall, verifState, linkGeneration);
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
                                        Slog.w("PackageManager", "Unknown element under <stopped-packages>: " + parser.getName());
                                        XmlUtils.skipCurrentTag(parser);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            str2.close();
            this.mNextAppLinkGeneration.put(userId, maxAppLinkGeneration + 1);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private ArraySet<String> readComponentsLPr(XmlPullParser parser) throws IOException, XmlPullParserException {
        ArraySet<String> components = null;
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1 || (type == CURRENT_DATABASE_VERSION && parser.getDepth() <= outerDepth)) {
                return components;
            }
            if (!(type == CURRENT_DATABASE_VERSION || type == 4 || !parser.getName().equals(TAG_ITEM))) {
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
            if (type == CURRENT_DATABASE_VERSION && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == CURRENT_DATABASE_VERSION || type == 4)) {
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
                serializer.startTag(null, TAG_PACKAGE);
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
                if (ustate.blockUninstall) {
                    serializer.attribute(null, ATTR_BLOCK_UNINSTALL, "true");
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
                serializer.endTag(null, TAG_PACKAGE);
            }
            writePreferredActivitiesLPr(serializer, userId, true);
            writePersistentPreferredActivitiesLPr(serializer, userId);
            writeCrossProfileIntentFiltersLPr(serializer, userId);
            writeDefaultAppsLPr(serializer, userId);
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
            if (type == CURRENT_DATABASE_VERSION && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == CURRENT_DATABASE_VERSION || type == 4)) {
                if (parser.getName().equals(TAG_ITEM)) {
                    String name = parser.getAttributeValue(null, ATTR_NAME);
                    BasePermission bp = (BasePermission) this.mPermissions.get(name);
                    if (bp == null) {
                        Slog.w("PackageManager", "Unknown permission: " + name);
                        XmlUtils.skipCurrentTag(parser);
                    } else {
                        boolean parseBoolean;
                        String grantedStr = parser.getAttributeValue(null, ATTR_GRANTED);
                        if (grantedStr != null) {
                            parseBoolean = Boolean.parseBoolean(grantedStr);
                        } else {
                            parseBoolean = true;
                        }
                        String flagsStr = parser.getAttributeValue(null, ATTR_FLAGS);
                        int flags = flagsStr != null ? Integer.parseInt(flagsStr, 16) : 0;
                        if (parseBoolean) {
                            if (permissionsState.grantInstallPermission(bp) == -1) {
                                Slog.w("PackageManager", "Permission already added: " + name);
                                XmlUtils.skipCurrentTag(parser);
                            } else {
                                permissionsState.updatePermissionFlags(bp, -1, RampAnimator.DEFAULT_MAX_BRIGHTNESS, flags);
                            }
                        } else if (permissionsState.revokeInstallPermission(bp) == -1) {
                            Slog.w("PackageManager", "Permission already added: " + name);
                            XmlUtils.skipCurrentTag(parser);
                        } else {
                            permissionsState.updatePermissionFlags(bp, -1, RampAnimator.DEFAULT_MAX_BRIGHTNESS, flags);
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

    void readStoppedLPw() {
        FileInputStream str;
        XmlPullParser parser;
        int type;
        int outerDepth;
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
                        str2 = str;
                        try {
                            parser = Xml.newPullParser();
                            parser.setInput(str2, null);
                            do {
                                type = parser.next();
                                if (type != 2) {
                                    break;
                                }
                                if (type != 2) {
                                    outerDepth = parser.getDepth();
                                    while (true) {
                                        type = parser.next();
                                        if (type == 1) {
                                            break;
                                        }
                                        break;
                                        str2.close();
                                    }
                                } else {
                                    this.mReadMessages.append("No start tag found in stopped packages file\n");
                                    PackageManagerService.reportSettingsProblem(5, "No start tag found in package manager stopped packages");
                                    return;
                                }
                            } while (type != 1);
                            if (type != 2) {
                                this.mReadMessages.append("No start tag found in stopped packages file\n");
                                PackageManagerService.reportSettingsProblem(5, "No start tag found in package manager stopped packages");
                                return;
                            }
                            outerDepth = parser.getDepth();
                            while (true) {
                                type = parser.next();
                                if (type == 1) {
                                    break;
                                }
                                break;
                                str2.close();
                            }
                        } catch (XmlPullParserException e4) {
                            e = e4;
                        } catch (IOException e5) {
                            e2 = e5;
                        }
                    } else {
                        try {
                            if (this.mStoppedPackagesFilename.exists()) {
                                this.mReadMessages.append("No stopped packages file found\n");
                                PackageManagerService.reportSettingsProblem(4, "No stopped packages file file; assuming all started");
                                for (PackageSetting pkg : this.mPackages.values()) {
                                    pkg.setStopped(DEBUG_STOPPED, 0);
                                    pkg.setNotLaunched(DEBUG_STOPPED, 0);
                                }
                            }
                            str2 = new FileInputStream(this.mStoppedPackagesFilename);
                            parser = Xml.newPullParser();
                            parser.setInput(str2, null);
                            do {
                                type = parser.next();
                                if (type != 2) {
                                    break;
                                }
                                if (type != 2) {
                                    outerDepth = parser.getDepth();
                                    while (true) {
                                        type = parser.next();
                                        if (type == 1) {
                                            break;
                                        }
                                        break;
                                        str2.close();
                                    }
                                } else {
                                    this.mReadMessages.append("No start tag found in stopped packages file\n");
                                    PackageManagerService.reportSettingsProblem(5, "No start tag found in package manager stopped packages");
                                    return;
                                }
                            } while (type != 1);
                            if (type != 2) {
                                this.mReadMessages.append("No start tag found in stopped packages file\n");
                                PackageManagerService.reportSettingsProblem(5, "No start tag found in package manager stopped packages");
                                return;
                            }
                            outerDepth = parser.getDepth();
                            while (true) {
                                type = parser.next();
                                if (type == 1) {
                                    break;
                                }
                                break;
                                str2.close();
                            }
                        } catch (XmlPullParserException e6) {
                            e = e6;
                            this.mReadMessages.append("Error reading: ").append(e.toString());
                            PackageManagerService.reportSettingsProblem(6, "Error reading stopped packages: " + e);
                            Slog.wtf("PackageManager", "Error reading package manager stopped packages", e);
                        } catch (IOException e7) {
                            e2 = e7;
                            this.mReadMessages.append("Error reading: ").append(e2.toString());
                            PackageManagerService.reportSettingsProblem(6, "Error reading settings: " + e2);
                            Slog.wtf("PackageManager", "Error reading package manager stopped packages", e2);
                        }
                    }
                }
            } catch (IOException e8) {
                str = str2;
                if (str == null) {
                    str2 = str;
                    parser = Xml.newPullParser();
                    parser.setInput(str2, null);
                    do {
                        type = parser.next();
                        if (type != 2) {
                            break;
                        }
                        if (type != 2) {
                            outerDepth = parser.getDepth();
                            while (true) {
                                type = parser.next();
                                if (type == 1) {
                                    break;
                                }
                                break;
                                str2.close();
                            }
                        } else {
                            this.mReadMessages.append("No start tag found in stopped packages file\n");
                            PackageManagerService.reportSettingsProblem(5, "No start tag found in package manager stopped packages");
                            return;
                        }
                    } while (type != 1);
                    if (type != 2) {
                        this.mReadMessages.append("No start tag found in stopped packages file\n");
                        PackageManagerService.reportSettingsProblem(5, "No start tag found in package manager stopped packages");
                        return;
                    }
                    outerDepth = parser.getDepth();
                    while (true) {
                        type = parser.next();
                        if (type == 1) {
                            break;
                        }
                        break;
                        str2.close();
                    }
                } else if (this.mStoppedPackagesFilename.exists()) {
                    this.mReadMessages.append("No stopped packages file found\n");
                    PackageManagerService.reportSettingsProblem(4, "No stopped packages file file; assuming all started");
                    for (PackageSetting pkg2 : this.mPackages.values()) {
                        pkg2.setStopped(DEBUG_STOPPED, 0);
                        pkg2.setNotLaunched(DEBUG_STOPPED, 0);
                    }
                } else {
                    str2 = new FileInputStream(this.mStoppedPackagesFilename);
                    parser = Xml.newPullParser();
                    parser.setInput(str2, null);
                    do {
                        type = parser.next();
                        if (type != 2) {
                            break;
                        }
                        if (type != 2) {
                            outerDepth = parser.getDepth();
                            while (true) {
                                type = parser.next();
                                if (type == 1) {
                                    break;
                                }
                                break;
                                str2.close();
                            }
                        } else {
                            this.mReadMessages.append("No start tag found in stopped packages file\n");
                            PackageManagerService.reportSettingsProblem(5, "No start tag found in package manager stopped packages");
                            return;
                        }
                    } while (type != 1);
                    if (type != 2) {
                        this.mReadMessages.append("No start tag found in stopped packages file\n");
                        PackageManagerService.reportSettingsProblem(5, "No start tag found in package manager stopped packages");
                        return;
                    }
                    outerDepth = parser.getDepth();
                    while (true) {
                        type = parser.next();
                        if (type == 1) {
                            break;
                        }
                        break;
                        str2.close();
                    }
                }
            }
        }
        str = null;
        if (str == null) {
            str2 = str;
            parser = Xml.newPullParser();
            parser.setInput(str2, null);
            do {
                type = parser.next();
                if (type != 2) {
                    break;
                }
                if (type != 2) {
                    outerDepth = parser.getDepth();
                    while (true) {
                        type = parser.next();
                        if (type == 1) {
                            break;
                        }
                        break;
                        str2.close();
                    }
                } else {
                    this.mReadMessages.append("No start tag found in stopped packages file\n");
                    PackageManagerService.reportSettingsProblem(5, "No start tag found in package manager stopped packages");
                    return;
                }
            } while (type != 1);
            if (type != 2) {
                this.mReadMessages.append("No start tag found in stopped packages file\n");
                PackageManagerService.reportSettingsProblem(5, "No start tag found in package manager stopped packages");
                return;
            }
            outerDepth = parser.getDepth();
            while (true) {
                type = parser.next();
                if (type == 1) {
                    break;
                }
                break;
                str2.close();
            }
        } else if (this.mStoppedPackagesFilename.exists()) {
            this.mReadMessages.append("No stopped packages file found\n");
            PackageManagerService.reportSettingsProblem(4, "No stopped packages file file; assuming all started");
            for (PackageSetting pkg22 : this.mPackages.values()) {
                pkg22.setStopped(DEBUG_STOPPED, 0);
                pkg22.setNotLaunched(DEBUG_STOPPED, 0);
            }
        } else {
            str2 = new FileInputStream(this.mStoppedPackagesFilename);
            parser = Xml.newPullParser();
            parser.setInput(str2, null);
            do {
                type = parser.next();
                if (type != 2) {
                    break;
                }
            } while (type != 1);
            if (type != 2) {
                this.mReadMessages.append("No start tag found in stopped packages file\n");
                PackageManagerService.reportSettingsProblem(5, "No start tag found in package manager stopped packages");
                return;
            }
            outerDepth = parser.getDepth();
            while (true) {
                type = parser.next();
                if (type == 1 || (type == CURRENT_DATABASE_VERSION && parser.getDepth() <= outerDepth)) {
                    str2.close();
                } else if (!(type == CURRENT_DATABASE_VERSION || type == 4)) {
                    if (parser.getName().equals(TAG_PACKAGE)) {
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
        }
    }

    void writeLPr() {
        if (this.mSettingsFilename.exists()) {
            if (this.mBackupSettingsFilename.exists()) {
                this.mSettingsFilename.delete();
                Slog.w("PackageManager", "Preserving older settings backup");
            } else {
                if (!this.mSettingsFilename.renameTo(this.mBackupSettingsFilename)) {
                    Slog.wtf("PackageManager", "Unable to backup package manager settings,  current changes will be lost at reboot");
                    return;
                }
            }
        }
        this.mPastSignatures.clear();
        try {
            FileOutputStream fstr = new FileOutputStream(this.mSettingsFilename);
            OutputStream bufferedOutputStream = new BufferedOutputStream(fstr);
            XmlSerializer serializer = new FastXmlSerializer();
            serializer.setOutput(bufferedOutputStream, StandardCharsets.UTF_8.name());
            serializer.startDocument(null, Boolean.valueOf(true));
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            serializer.startTag(null, "packages");
            int i = 0;
            while (true) {
                if (i >= this.mVersion.size()) {
                    break;
                }
                String volumeUuid = (String) this.mVersion.keyAt(i);
                VersionInfo ver = (VersionInfo) this.mVersion.valueAt(i);
                serializer.startTag(null, TAG_VERSION);
                XmlUtils.writeStringAttribute(serializer, ATTR_VOLUME_UUID, volumeUuid);
                XmlUtils.writeIntAttribute(serializer, ATTR_SDK_VERSION, ver.sdkVersion);
                XmlUtils.writeIntAttribute(serializer, ATTR_DATABASE_VERSION, ver.databaseVersion);
                XmlUtils.writeStringAttribute(serializer, ATTR_FINGERPRINT, ver.fingerprint);
                serializer.endTag(null, TAG_VERSION);
                i++;
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
            for (PackageSetting writePackageLPr : this.mPackages.values()) {
                writePackageLPr(serializer, writePackageLPr);
            }
            for (PackageSetting writePackageLPr2 : this.mDisabledSysPackages.values()) {
                writeDisabledSysPackageLPr(serializer, writePackageLPr2);
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
            if (this.mSettingsFilename.exists()) {
                if (!this.mSettingsFilename.delete()) {
                    Slog.wtf("PackageManager", "Failed to clean up mangled file: " + this.mSettingsFilename);
                }
            }
        } catch (IOException e3) {
            Slog.wtf("PackageManager", "Unable to write package manager settings, current changes will be lost at reboot", e3);
            if (this.mSettingsFilename.exists()) {
                if (this.mSettingsFilename.delete()) {
                    Slog.wtf("PackageManager", "Failed to clean up mangled file: " + this.mSettingsFilename);
                }
            }
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
        if (this.mKernelMappingFilename != null) {
            Integer cur = (Integer) this.mKernelMapping.get(ps.name);
            if (cur == null || cur.intValue() != ps.appId) {
                File dir = new File(this.mKernelMappingFilename, ps.name);
                dir.mkdir();
                try {
                    FileUtils.stringToFile(new File(dir, "appid"), Integer.toString(ps.appId));
                    this.mKernelMapping.put(ps.name, Integer.valueOf(ps.appId));
                } catch (IOException e) {
                }
            }
        }
    }

    void writePackageListLPr() {
        writePackageListLPr(-1);
    }

    void writePackageListLPr(int creatingUserId) {
        Exception e;
        List<UserInfo> users = UserManagerService.getInstance().getUsers(true);
        int[] userIds = new int[users.size()];
        int i = 0;
        while (true) {
            int length = userIds.length;
            if (i >= r0) {
                break;
            }
            userIds[i] = ((UserInfo) users.get(i)).id;
            i++;
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
                FileUtils.setPermissions(fstr.getFD(), 416, ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE, 1032);
                StringBuilder sb = new StringBuilder();
                for (PackageSetting pkg : this.mPackages.values()) {
                    if (pkg.pkg != null) {
                        if (pkg.pkg.applicationInfo != null) {
                            if (pkg.pkg.applicationInfo.dataDir != null) {
                                ApplicationInfo ai = pkg.pkg.applicationInfo;
                                String dataPath = ai.dataDir;
                                boolean isDebug = (ai.flags & 2) != 0 ? true : DEBUG_STOPPED;
                                int[] gids = pkg.getPermissionsState().computeGids(userIds);
                                if (dataPath.indexOf(32) < 0) {
                                    sb.setLength(0);
                                    sb.append(ai.packageName);
                                    sb.append(" ");
                                    sb.append(ai.uid);
                                    sb.append(isDebug ? " 1 " : " 0 ");
                                    sb.append(dataPath);
                                    sb.append(" ");
                                    sb.append(ai.seinfo);
                                    sb.append(" ");
                                    if (gids != null && gids.length > 0) {
                                        sb.append(gids[0]);
                                        i = 1;
                                        while (true) {
                                            length = gids.length;
                                            if (i >= r0) {
                                                break;
                                            }
                                            sb.append(",");
                                            sb.append(gids[i]);
                                            i++;
                                        }
                                    } else {
                                        sb.append("none");
                                    }
                                    sb.append("\n");
                                    writer.append(sb);
                                }
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
        serializer.attribute(null, TAG_VERSION, String.valueOf(pkg.versionCode));
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
        serializer.attribute(null, TAG_VERSION, String.valueOf(pkg.versionCode));
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
        if (pkg.isOrphaned) {
            serializer.attribute(null, "isOrphaned", "true");
        }
        if (pkg.volumeUuid != null) {
            serializer.attribute(null, ATTR_VOLUME_UUID, pkg.volumeUuid);
        }
        if (pkg.parentPackageName != null) {
            serializer.attribute(null, "parentPackageName", pkg.parentPackageName);
        }
        writeChildPackagesLPw(serializer, pkg.childPackageNames);
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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    boolean readLPw(List<UserInfo> users) {
        int type;
        InputStream inputStream = null;
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
                    inputStream = fileInputStream;
                } catch (IOException e) {
                    inputStream = fileInputStream;
                }
            } catch (IOException e2) {
            }
        }
        this.mPendingPackages.clear();
        this.mPastSignatures.clear();
        this.mKeySetRefs.clear();
        this.mInstallerPackages.clear();
        if (inputStream == null) {
            try {
                if (this.mSettingsFilename.exists()) {
                    inputStream = new FileInputStream(this.mSettingsFilename);
                } else {
                    this.mReadMessages.append("No settings file found\n");
                    PackageManagerService.reportSettingsProblem(4, "No settings file; creating initial state");
                    findOrCreateVersion(StorageManager.UUID_PRIVATE_INTERNAL);
                    findOrCreateVersion("primary_physical");
                    return DEBUG_STOPPED;
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
                addSharedUserLPw("android.uid.system", ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE, 1, 8);
                addSharedUserLPw("android.uid.phone", 1001, 1, 8);
                addSharedUserLPw("android.uid.log", 1007, 1, 8);
                addSharedUserLPw("android.uid.nfc", 1027, 1, 8);
                addSharedUserLPw("com.nxp.uid.nfceeapi", 1054, 1, 8);
                addSharedUserLPw("android.uid.bluetooth", 1002, 1, 8);
                addSharedUserLPw("android.uid.shell", IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME, 1, 8);
                findOrCreateVersion(StorageManager.UUID_PRIVATE_INTERNAL);
                findOrCreateVersion("primary_physical");
                return DEBUG_STOPPED;
            } catch (Throwable e4) {
                this.mReadMessages.append("Error reading: ").append(e4.toString());
                PackageManagerService.reportSettingsProblem(6, "Error reading settings: " + e4);
                Slog.wtf("PackageManager", "Error reading package manager settings", e4);
            } catch (Throwable e5) {
                this.mReadMessages.append("Error reading: ").append(e5.toString());
                PackageManagerService.reportSettingsProblem(6, "Error reading settings: " + e5);
                Log.wtf("PackageManager", "Error reading package manager settings", e5);
            }
        }
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(inputStream, StandardCharsets.UTF_8.name());
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
            return DEBUG_STOPPED;
        }
        int outerDepth = parser.getDepth();
        while (true) {
            type = parser.next();
            if (type == 1 || (type == CURRENT_DATABASE_VERSION && parser.getDepth() <= outerDepth)) {
                inputStream.close();
            } else if (!(type == CURRENT_DATABASE_VERSION || type == 4)) {
                String tagName = parser.getName();
                if (tagName.equals(HwBroadcastRadarUtil.KEY_PACKAGE)) {
                    readPackageLPw(parser);
                } else {
                    if (tagName.equals("permissions")) {
                        readPermissionsLPw(this.mPermissions, parser);
                    } else {
                        if (tagName.equals("permission-trees")) {
                            readPermissionsLPw(this.mPermissionTrees, parser);
                        } else {
                            if (tagName.equals(TAG_SHARED_USER)) {
                                readSharedUserLPw(parser);
                            } else {
                                if (tagName.equals("preferred-packages")) {
                                    continue;
                                } else {
                                    if (tagName.equals("preferred-activities")) {
                                        readPreferredActivitiesLPw(parser, 0);
                                    } else {
                                        if (tagName.equals(TAG_PERSISTENT_PREFERRED_ACTIVITIES)) {
                                            readPersistentPreferredActivitiesLPw(parser, 0);
                                        } else {
                                            if (tagName.equals(TAG_CROSS_PROFILE_INTENT_FILTERS)) {
                                                readCrossProfileIntentFiltersLPw(parser, 0);
                                            } else {
                                                if (tagName.equals(TAG_DEFAULT_BROWSER)) {
                                                    readDefaultAppsLPw(parser, 0);
                                                } else {
                                                    if (tagName.equals("updated-package")) {
                                                        readDisabledSysPackageLPw(parser);
                                                    } else {
                                                        if (tagName.equals("cleaning-package")) {
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
                                                            } else {
                                                                continue;
                                                            }
                                                        } else {
                                                            if (tagName.equals("renamed-package")) {
                                                                String nname = parser.getAttributeValue(null, "new");
                                                                String oname = parser.getAttributeValue(null, "old");
                                                                if (!(nname == null || oname == null)) {
                                                                    this.mRenamedPackages.put(nname, oname);
                                                                }
                                                            } else {
                                                                if (tagName.equals("restored-ivi")) {
                                                                    readRestoredIntentFilterVerifications(parser);
                                                                } else {
                                                                    VersionInfo internal;
                                                                    VersionInfo external;
                                                                    if (tagName.equals("last-platform-version")) {
                                                                        internal = findOrCreateVersion(StorageManager.UUID_PRIVATE_INTERNAL);
                                                                        external = findOrCreateVersion("primary_physical");
                                                                        internal.sdkVersion = XmlUtils.readIntAttribute(parser, "internal", 0);
                                                                        external.sdkVersion = XmlUtils.readIntAttribute(parser, "external", 0);
                                                                        String readStringAttribute = XmlUtils.readStringAttribute(parser, ATTR_FINGERPRINT);
                                                                        external.fingerprint = readStringAttribute;
                                                                        internal.fingerprint = readStringAttribute;
                                                                    } else {
                                                                        if (tagName.equals("database-version")) {
                                                                            internal = findOrCreateVersion(StorageManager.UUID_PRIVATE_INTERNAL);
                                                                            external = findOrCreateVersion("primary_physical");
                                                                            internal.databaseVersion = XmlUtils.readIntAttribute(parser, "internal", 0);
                                                                            external.databaseVersion = XmlUtils.readIntAttribute(parser, "external", 0);
                                                                        } else {
                                                                            if (tagName.equals("verifier")) {
                                                                                try {
                                                                                    this.mVerifierDeviceIdentity = VerifierDeviceIdentity.parse(parser.getAttributeValue(null, "device"));
                                                                                } catch (IllegalArgumentException e7) {
                                                                                    Slog.w("PackageManager", "Discard invalid verifier device id: " + e7.getMessage());
                                                                                }
                                                                            } else if (TAG_READ_EXTERNAL_STORAGE.equals(tagName)) {
                                                                                this.mReadExternalStorageEnforced = Boolean.valueOf("1".equals(parser.getAttributeValue(null, ATTR_ENFORCEMENT)));
                                                                            } else {
                                                                                if (tagName.equals("keyset-settings")) {
                                                                                    this.mKeySetManagerService.readKeySetsLPw(parser, this.mKeySetRefs);
                                                                                } else if (TAG_VERSION.equals(tagName)) {
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
            }
        }
        inputStream.close();
        int N = this.mPendingPackages.size();
        for (int i = 0; i < N; i++) {
            PackageSettingBase pp = (PendingPackage) this.mPendingPackages.get(i);
            Object idObj = getUserIdLPr(pp.sharedId);
            if (idObj != null && (idObj instanceof SharedUserSetting)) {
                PackageSetting p = getPackageLPw(pp.name, null, pp.realName, (SharedUserSetting) idObj, pp.codePath, pp.resourcePath, pp.legacyNativeLibraryPathString, pp.primaryCpuAbiString, pp.secondaryCpuAbiString, pp.versionCode, pp.pkgFlags, pp.pkgPrivateFlags, null, true, DEBUG_STOPPED, pp.parentPackageName, pp.childPackageNames);
                if (p == null) {
                    PackageManagerService.reportSettingsProblem(5, "Unable to create application package for " + pp.name);
                } else {
                    p.copyFrom(pp);
                }
            } else if (idObj != null) {
                msg = "Bad package setting: package " + pp.name + " has shared uid " + pp.sharedId + " that is not a shared uid\n";
                this.mReadMessages.append(msg);
                PackageManagerService.reportSettingsProblem(6, msg);
            } else {
                msg = "Bad package setting: package " + pp.name + " has shared uid " + pp.sharedId + " that is not defined\n";
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
            for (UserInfo userInfo : users) {
                readPackageRestrictionsLPr(userInfo.id);
            }
        }
        try {
            for (UserInfo user : users) {
                this.mRuntimePermissionsPersistence.readStateForUserSyncLPr(user.id);
            }
        } catch (Throwable e8) {
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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void applyDefaultPreferredAppsLPw(PackageManagerService service, int userId) {
        XmlPullParserException e;
        IOException e2;
        Throwable th;
        for (PackageSetting ps : this.mPackages.values()) {
            if (!((ps.pkgFlags & 1) == 0 || ps.pkg == null)) {
                if (ps.pkg.preferredActivityFilters != null) {
                    ArrayList<ActivityIntentInfo> intents = ps.pkg.preferredActivityFilters;
                    for (int i = 0; i < intents.size(); i++) {
                        ActivityIntentInfo aii = (ActivityIntentInfo) intents.get(i);
                        applyDefaultPreferredActivityLPw(service, aii, new ComponentName(ps.name, aii.activity.className), userId);
                    }
                }
            }
        }
        File preferredDir = new File(Environment.getRootDirectory(), "etc/preferred-apps");
        if (!preferredDir.exists() || !preferredDir.isDirectory()) {
            return;
        }
        if (preferredDir.canRead()) {
            for (File f : preferredDir.listFiles()) {
                if (!f.getPath().endsWith(".xml")) {
                    Slog.i(TAG, "Non-xml file " + f + " in " + preferredDir + " directory, ignoring");
                } else if (f.canRead()) {
                    InputStream inputStream = null;
                    try {
                        InputStream str = new BufferedInputStream(new FileInputStream(f));
                        try {
                            int type;
                            XmlPullParser parser = Xml.newPullParser();
                            parser.setInput(str, null);
                            do {
                                type = parser.next();
                                if (type == 2) {
                                    break;
                                }
                            } while (type != 1);
                            if (type != 2) {
                                Slog.w(TAG, "Preferred apps file " + f + " does not have start tag");
                                if (str != null) {
                                    try {
                                        str.close();
                                    } catch (IOException e3) {
                                    }
                                }
                            } else if ("preferred-activities".equals(parser.getName())) {
                                readDefaultPreferredActivitiesLPw(service, parser, userId);
                                if (str != null) {
                                    try {
                                        str.close();
                                    } catch (IOException e4) {
                                    }
                                }
                            } else {
                                Slog.w(TAG, "Preferred apps file " + f + " does not start with 'preferred-activities'");
                                if (str != null) {
                                    try {
                                        str.close();
                                    } catch (IOException e5) {
                                    }
                                }
                            }
                        } catch (XmlPullParserException e6) {
                            e = e6;
                            inputStream = str;
                            Slog.w(TAG, "Error reading apps file " + f, e);
                            if (inputStream != null) {
                                try {
                                    inputStream.close();
                                } catch (IOException e7) {
                                }
                            }
                        } catch (IOException e8) {
                            e2 = e8;
                            inputStream = str;
                            try {
                                Slog.w(TAG, "Error reading apps file " + f, e2);
                                if (inputStream != null) {
                                    try {
                                        inputStream.close();
                                    } catch (IOException e9) {
                                    }
                                }
                            } catch (Throwable th2) {
                                th = th2;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            inputStream = str;
                        }
                    } catch (XmlPullParserException e10) {
                        e = e10;
                        Slog.w(TAG, "Error reading apps file " + f, e);
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    } catch (IOException e11) {
                        e2 = e11;
                        Slog.w(TAG, "Error reading apps file " + f, e2);
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    }
                } else {
                    Slog.w(TAG, "Preferred apps file " + f + " cannot be read");
                }
            }
            return;
        }
        Slog.w(TAG, "Directory " + preferredDir + " cannot be read");
    }

    private void applyDefaultPreferredActivityLPw(PackageManagerService service, IntentFilter tmpPa, ComponentName cn, int userId) {
        int ischeme;
        Intent intent = new Intent();
        int flags = 786432;
        intent.setAction(tmpPa.getAction(0));
        for (int i = 0; i < tmpPa.countCategories(); i++) {
            String cat = tmpPa.getCategory(i);
            if (cat.equals("android.intent.category.DEFAULT")) {
                flags |= DumpState.DUMP_INSTALLS;
            } else {
                intent.addCategory(cat);
            }
        }
        boolean doNonData = true;
        boolean hasSchemes = DEBUG_STOPPED;
        for (ischeme = 0; ischeme < tmpPa.countDataSchemes(); ischeme++) {
            boolean doScheme = true;
            String scheme = tmpPa.getDataScheme(ischeme);
            if (!(scheme == null || scheme.isEmpty())) {
                hasSchemes = true;
            }
            for (int issp = 0; issp < tmpPa.countDataSchemeSpecificParts(); issp++) {
                Builder builder = new Builder();
                builder.scheme(scheme);
                PatternMatcher ssp = tmpPa.getDataSchemeSpecificPart(issp);
                builder.opaquePart(ssp.getPath());
                Intent finalIntent = new Intent(intent);
                finalIntent.setData(builder.build());
                applyDefaultPreferredActivityLPw(service, finalIntent, flags, cn, scheme, ssp, null, null, userId);
                doScheme = DEBUG_STOPPED;
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
                    doScheme = DEBUG_STOPPED;
                    doAuth = DEBUG_STOPPED;
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
                    doScheme = DEBUG_STOPPED;
                }
            }
            if (doScheme) {
                builder = new Builder();
                builder.scheme(scheme);
                finalIntent = new Intent(intent);
                finalIntent.setData(builder.build());
                applyDefaultPreferredActivityLPw(service, finalIntent, flags, cn, scheme, null, null, null, userId);
            }
            doNonData = DEBUG_STOPPED;
        }
        for (int idata = 0; idata < tmpPa.countDataTypes(); idata++) {
            String mimeType = tmpPa.getDataType(idata);
            if (hasSchemes) {
                builder = new Builder();
                for (ischeme = 0; ischeme < tmpPa.countDataSchemes(); ischeme++) {
                    scheme = tmpPa.getDataScheme(ischeme);
                    if (!(scheme == null || scheme.isEmpty())) {
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
            doNonData = DEBUG_STOPPED;
        }
        if (doNonData) {
            applyDefaultPreferredActivityLPw(service, intent, flags, cn, null, null, null, null, userId);
        }
    }

    private void applyDefaultPreferredActivityLPw(PackageManagerService service, Intent intent, int flags, ComponentName cn, String scheme, PatternMatcher ssp, AuthorityEntry auth, PatternMatcher path, int userId) {
        flags = service.updateFlagsForResolve(flags, userId, intent);
        List<ResolveInfo> ri = service.mActivities.queryIntent(intent, intent.getType(), flags, 0);
        int systemMatch = 0;
        if (ri == null || ri.size() <= 1) {
            String str = " while setting preferred ";
            str = cn.flattenToShortString();
            Slog.w(TAG, "No potential matches found for " + intent + r19 + r19);
            return;
        }
        int i;
        boolean haveAct = DEBUG_STOPPED;
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
            if ((DumpState.DUMP_INSTALLS & flags) != 0) {
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
                    str = intent.getType();
                    str = " for ";
                    Slog.w(TAG, "Malformed mimetype " + r19 + r19 + cn);
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
            str = "; found third party match ";
            str = haveNonSys.flattenToShortString();
            Slog.i(TAG, "Not setting preferred " + intent + r19 + r19);
        }
    }

    private void readDefaultPreferredActivitiesLPw(PackageManagerService service, XmlPullParser parser, int userId) throws XmlPullParserException, IOException {
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1) {
                return;
            }
            if (type == CURRENT_DATABASE_VERSION && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == CURRENT_DATABASE_VERSION || type == 4)) {
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
            if (type == CURRENT_DATABASE_VERSION && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == CURRENT_DATABASE_VERSION || type == 4)) {
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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
        String version = parser.getAttributeValue(null, TAG_VERSION);
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
        PackageSetting ps = new PackageSetting(name, realName, codePathFile, new File(resourcePathStr), legacyNativeLibraryPathStr, primaryCpuAbiStr, secondaryCpuAbiStr, cpuAbiOverrideStr, versionCode, 1, pkgPrivateFlags, parentPackageName, null);
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
            if (type == 1 || (type == CURRENT_DATABASE_VERSION && parser.getDepth() <= outerDepth)) {
                this.mDisabledSysPackages.put(name, ps);
            } else if (!(type == CURRENT_DATABASE_VERSION || type == 4)) {
                if (parser.getName().equals(TAG_PERMISSIONS)) {
                    readInstallPermissionsLPr(parser, ps.getPermissionsState());
                } else if (parser.getName().equals(TAG_CHILD_PACKAGE)) {
                    String childPackageName = parser.getAttributeValue(null, ATTR_NAME);
                    if (ps.childPackageNames == null) {
                        ps.childPackageNames = new ArrayList();
                    }
                    ps.childPackageNames.add(childPackageName);
                } else {
                    PackageManagerService.reportSettingsProblem(5, "Unknown element under <updated-package>: " + parser.getName());
                    XmlUtils.skipCurrentTag(parser);
                }
            }
        }
        this.mDisabledSysPackages.put(name, ps);
    }

    private void readDisabledComponentsLPw(PackageSettingBase packageSetting, XmlPullParser parser, int userId) throws IOException, XmlPullParserException {
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1) {
                return;
            }
            if (type == CURRENT_DATABASE_VERSION && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == CURRENT_DATABASE_VERSION || type == 4)) {
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
            if (type == CURRENT_DATABASE_VERSION && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == CURRENT_DATABASE_VERSION || type == 4)) {
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
        String str = null;
        String str2 = null;
        int pkgFlags = 0;
        SharedUserSetting su = null;
        try {
            str = parser.getAttributeValue(null, ATTR_NAME);
            str2 = parser.getAttributeValue(null, "userId");
            int userId = str2 != null ? Integer.parseInt(str2) : 0;
            if ("true".equals(parser.getAttributeValue(null, "system"))) {
                pkgFlags = 1;
            }
            if (str == null) {
                PackageManagerService.reportSettingsProblem(5, "Error in package manager settings: <shared-user> has no name at " + parser.getPositionDescription());
            } else if (userId == 0) {
                PackageManagerService.reportSettingsProblem(5, "Error in package manager settings: shared-user " + str + " has bad userId " + str2 + " at " + parser.getPositionDescription());
            } else {
                su = addSharedUserLPw(str.intern(), userId, pkgFlags, 0);
                if (su == null) {
                    PackageManagerService.reportSettingsProblem(6, "Occurred while parsing settings at " + parser.getPositionDescription());
                }
            }
        } catch (NumberFormatException e) {
            PackageManagerService.reportSettingsProblem(5, "Error in package manager settings: package " + str + " has bad userId " + str2 + " at " + parser.getPositionDescription());
        }
        if (su != null) {
            int outerDepth = parser.getDepth();
            while (true) {
                int type = parser.next();
                if (type == 1) {
                    return;
                }
                if (type == CURRENT_DATABASE_VERSION && parser.getDepth() <= outerDepth) {
                    return;
                }
                if (!(type == CURRENT_DATABASE_VERSION || type == 4)) {
                    String tagName = parser.getName();
                    if (tagName.equals("sigs")) {
                        su.signatures.readXml(parser, this.mPastSignatures);
                        if (su.signatures.mSignatures.length <= 0) {
                            break;
                        }
                    } else if (tagName.equals(TAG_PERMISSIONS)) {
                        readInstallPermissionsLPr(parser, su.getPermissionsState());
                    } else {
                        PackageManagerService.reportSettingsProblem(5, "Unknown element under <shared-user>: " + parser.getName());
                        XmlUtils.skipCurrentTag(parser);
                    }
                }
            }
            throw new XmlPullParserException("No cert in shareduid:" + su.name);
        }
        XmlUtils.skipCurrentTag(parser);
    }

    void createNewUserLI(PackageManagerService service, Installer installer, int userHandle) {
        synchronized (this.mPackages) {
            int i;
            Collection<PackageSetting> packages = this.mPackages.values();
            int packagesCount = packages.size();
            String[] volumeUuids = new String[packagesCount];
            String[] names = new String[packagesCount];
            int[] appIds = new int[packagesCount];
            String[] seinfos = new String[packagesCount];
            int[] targetSdkVersions = new int[packagesCount];
            Iterator<PackageSetting> packagesIterator = packages.iterator();
            for (i = 0; i < packagesCount; i++) {
                PackageSetting ps = (PackageSetting) packagesIterator.next();
                if (!(ps.pkg == null || ps.pkg.applicationInfo == null)) {
                    boolean isSystemApp = (ps.pkgFlags & 1) != 0 ? true : DEBUG_STOPPED;
                    if (isSystemApp) {
                        if (isInDelAppList(ps.name)) {
                            ps.setInstalled(DEBUG_STOPPED, userHandle);
                            volumeUuids[i] = ps.volumeUuid;
                            names[i] = ps.name;
                            appIds[i] = ps.appId;
                            seinfos[i] = ps.pkg.applicationInfo.seinfo;
                            targetSdkVersions[i] = ps.pkg.applicationInfo.targetSdkVersion;
                        }
                    }
                    ps.setInstalled(isSystemApp, userHandle);
                    volumeUuids[i] = ps.volumeUuid;
                    names[i] = ps.name;
                    appIds[i] = ps.appId;
                    seinfos[i] = ps.pkg.applicationInfo.seinfo;
                    targetSdkVersions[i] = ps.pkg.applicationInfo.targetSdkVersion;
                }
            }
        }
        for (i = 0; i < packagesCount; i++) {
            if (names[i] != null) {
                try {
                    installer.createAppData(volumeUuids[i], names[i], userHandle, CURRENT_DATABASE_VERSION, appIds[i], seinfos[i], targetSdkVersions[i]);
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
        this.mRuntimePermissionsPersistence.onUserRemoved(userId);
        writePackageListLPr();
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
                boolean needsWriting = DEBUG_STOPPED;
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
                return i + AbsPowerManagerService.MIN_COVER_SCREEN_OFF_TIMEOUT;
            }
        }
        if (N > 9999) {
            return -1;
        }
        this.mUserIds.add(obj);
        return N + AbsPowerManagerService.MIN_COVER_SCREEN_OFF_TIMEOUT;
    }

    public VerifierDeviceIdentity getVerifierDeviceIdentityLPw() {
        if (this.mVerifierDeviceIdentity == null) {
            this.mVerifierDeviceIdentity = VerifierDeviceIdentity.generate();
            writeLPr();
        }
        return this.mVerifierDeviceIdentity;
    }

    public boolean hasOtherDisabledSystemPkgWithChildLPr(String parentPackageName, String childPackageName) {
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
        return DEBUG_STOPPED;
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
            return DEBUG_STOPPED;
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

    boolean setPackageStoppedStateLPw(PackageManagerService pm, String packageName, boolean stopped, boolean allowedByPermission, int uid, int userId) {
        int appId = UserHandle.getAppId(uid);
        PackageSetting pkgSetting = (PackageSetting) this.mPackages.get(packageName);
        if (pkgSetting == null) {
            throw new IllegalArgumentException("Unknown package: " + packageName);
        } else if (!allowedByPermission && appId != pkgSetting.appId) {
            throw new SecurityException("Permission Denial: attempt to change stopped state from pid=" + Binder.getCallingPid() + ", uid=" + uid + ", package uid=" + pkgSetting.appId);
        } else if (pkgSetting.getStopped(userId) == stopped) {
            return DEBUG_STOPPED;
        } else {
            pkgSetting.setStopped(stopped, userId);
            if (pkgSetting.getNotLaunched(userId)) {
                if (pkgSetting.installerPackageName != null) {
                    pm.notifyFirstLaunch(pkgSetting.name, pkgSetting.installerPackageName, userId);
                }
                pkgSetting.setNotLaunched(DEBUG_STOPPED, userId);
            }
            return true;
        }
    }

    List<UserInfo> getAllUsers() {
        long id = Binder.clearCallingIdentity();
        try {
            List<UserInfo> users = UserManagerService.getInstance().getUsers(DEBUG_STOPPED);
            return users;
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
                if (pps == null || !pps.codePathString.equals(parentPkg.codePath)) {
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
                    if (cps == null || !cps.codePathString.equals(childPkg.codePath)) {
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
            if ((ps.pkg.applicationInfo.flags & DumpState.DUMP_MESSAGES) != 0) {
                if (1 == null) {
                    pw.print(", ");
                }
                first = DEBUG_STOPPED;
                pw.print("small");
            }
            if ((ps.pkg.applicationInfo.flags & DumpState.DUMP_PROVIDERS) != 0) {
                if (!first) {
                    pw.print(", ");
                }
                first = DEBUG_STOPPED;
                pw.print("medium");
            }
            if ((ps.pkg.applicationInfo.flags & DumpState.DUMP_VERIFIERS) != 0) {
                if (!first) {
                    pw.print(", ");
                }
                first = DEBUG_STOPPED;
                pw.print("large");
            }
            if ((ps.pkg.applicationInfo.flags & DumpState.DUMP_FROZEN) != 0) {
                if (!first) {
                    pw.print(", ");
                }
                first = DEBUG_STOPPED;
                pw.print("xlarge");
            }
            if ((ps.pkg.applicationInfo.flags & DumpState.DUMP_PREFERRED) != 0) {
                if (!first) {
                    pw.print(", ");
                }
                first = DEBUG_STOPPED;
                pw.print("resizeable");
            }
            if ((ps.pkg.applicationInfo.flags & DumpState.DUMP_PREFERRED_XML) != 0) {
                if (!first) {
                    pw.print(", ");
                }
                pw.print("anyDensity");
            }
            pw.println("]");
            if (ps.pkg.libraryNames != null && ps.pkg.libraryNames.size() > 0) {
                pw.print(prefix);
                pw.println("  libraries:");
                for (i = 0; i < ps.pkg.libraryNames.size(); i++) {
                    pw.print(prefix);
                    pw.print("    ");
                    pw.println((String) ps.pkg.libraryNames.get(i));
                }
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
                    if (!permissionNames.contains(perm.info.name)) {
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
                if (permissionNames == null || permissionNames.contains(perm2)) {
                    pw.print(prefix);
                    pw.print("    ");
                    pw.println(perm2);
                }
            }
        }
        if (ps.sharedUser != null && permissionNames == null) {
            if (dumpAll) {
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
                pw.println(ps.getEnabled(user2.id));
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
        dumpInstallPermissionsLPr(pw, prefix + "  ", permissionNames, ps.getPermissionsState());
        for (UserInfo user22 : users) {
            pw.print(prefix);
            pw.print("  User ");
            pw.print(user22.id);
            pw.print(": ");
            pw.print("ceDataInode=");
            pw.print(ps.getCeDataInode(user22.id));
            pw.print(" installed=");
            pw.print(ps.getInstalled(user22.id));
            pw.print(" hidden=");
            pw.print(ps.getHidden(user22.id));
            pw.print(" suspended=");
            pw.print(ps.getSuspended(user22.id));
            pw.print(" stopped=");
            pw.print(ps.getStopped(user22.id));
            pw.print(" notLaunched=");
            pw.print(ps.getNotLaunched(user22.id));
            pw.print(" enabled=");
            pw.println(ps.getEnabled(user22.id));
            lastDisabledAppCaller = ps.getLastDisabledAppCaller(user22.id);
            if (lastDisabledAppCaller != null) {
                pw.print(prefix);
                pw.print("    lastDisabledCaller: ");
                pw.println(lastDisabledAppCaller);
            }
            if (ps.sharedUser == null) {
                PermissionsState permissionsState2 = ps.getPermissionsState();
                dumpGidsLPr(pw, prefix + "    ", permissionsState2.computeGids(user22.id));
                dumpRuntimePermissionsLPr(pw, prefix + "    ", permissionNames, permissionsState2.getRuntimePermissionStates(user22.id), dumpAll);
            }
            if (permissionNames == null) {
                ArraySet<String> cmp2 = ps.getDisabledComponents(user22.id);
                pw.print(prefix);
                pw.println("    disabledComponents:");
                for (String s22 : cmp2) {
                    pw.print(prefix);
                    pw.print("      ");
                    pw.println(s22);
                }
                cmp2 = ps.getEnabledComponents(user22.id);
                pw.print(prefix);
                pw.println("    enabledComponents:");
                for (String s222 : cmp2) {
                    pw.print(prefix);
                    pw.print("      ");
                    pw.println(s222);
                }
            }
        }
    }

    void dumpPackagesLPr(PrintWriter pw, String packageName, ArraySet<String> permissionNames, DumpState dumpState, boolean checkin) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        boolean printedSomething = DEBUG_STOPPED;
        List<UserInfo> users = getAllUsers();
        for (PackageSetting ps : this.mPackages.values()) {
            if (packageName != null) {
                if (!packageName.equals(ps.realName)) {
                    if (packageName.equals(ps.name)) {
                    }
                }
            }
            if (permissionNames == null || ps.getPermissionsState().hasRequestedPermission(permissionNames)) {
                if (!(checkin || packageName == null)) {
                    dumpState.setSharedUser(ps.sharedUser);
                }
                if (!(checkin || printedSomething)) {
                    if (dumpState.onTitlePrinted()) {
                        pw.println();
                    }
                    pw.println("Packages:");
                    printedSomething = true;
                }
                dumpPackageLPr(pw, "  ", checkin ? TAG_PACKAGE : null, permissionNames, ps, sdf, date, users, packageName != null ? true : DEBUG_STOPPED);
            }
        }
        printedSomething = DEBUG_STOPPED;
        if (this.mRenamedPackages.size() > 0 && permissionNames == null) {
            for (Entry<String, String> e : this.mRenamedPackages.entrySet()) {
                if (packageName != null) {
                    if (!packageName.equals(e.getKey())) {
                        if (packageName.equals(e.getValue())) {
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
        printedSomething = DEBUG_STOPPED;
        if (this.mDisabledSysPackages.size() > 0 && permissionNames == null) {
            for (PackageSetting ps2 : this.mDisabledSysPackages.values()) {
                if (packageName != null) {
                    if (!packageName.equals(ps2.realName)) {
                        if (packageName.equals(ps2.name)) {
                        }
                    }
                }
                if (!(checkin || printedSomething)) {
                    if (dumpState.onTitlePrinted()) {
                        pw.println();
                    }
                    pw.println("Hidden system packages:");
                    printedSomething = true;
                }
                dumpPackageLPr(pw, "  ", checkin ? "dis" : null, permissionNames, ps2, sdf, date, users, packageName != null ? true : DEBUG_STOPPED);
            }
        }
    }

    void dumpPermissionsLPr(PrintWriter pw, String packageName, ArraySet<String> permissionNames, DumpState dumpState) {
        boolean printedSomething = DEBUG_STOPPED;
        for (BasePermission p : this.mPermissions.values()) {
            if ((packageName == null || packageName.equals(p.sourcePackage)) && (permissionNames == null || permissionNames.contains(p.name))) {
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
        boolean printedSomething = DEBUG_STOPPED;
        for (SharedUserSetting su : this.mSharedUsers.values()) {
            if ((packageName == null || su == dumpState.getSharedUser()) && (permissionNames == null || su.getPermissionsState().hasRequestedPermission(permissionNames))) {
                if (checkin) {
                    pw.print("suid,");
                    pw.print(su.userId);
                    pw.print(",");
                    pw.println(su.name);
                } else {
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
                    for (int userId : UserManagerService.getInstance().getUserIds()) {
                        int[] gids = permissionsState.computeGids(userId);
                        List<PermissionState> permissions = permissionsState.getRuntimePermissionStates(userId);
                        if (!ArrayUtils.isEmpty(gids) || !permissions.isEmpty()) {
                            pw.print(prefix);
                            pw.print("User ");
                            pw.print(userId);
                            pw.println(": ");
                            dumpGidsLPr(pw, prefix + "  ", gids);
                            dumpRuntimePermissionsLPr(pw, prefix + "  ", permissionNames, permissions, packageName != null ? true : DEBUG_STOPPED);
                        }
                    }
                }
            }
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
            pw.print("unknown");
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

    void dumpInstallPermissionsLPr(PrintWriter pw, String prefix, ArraySet<String> permissionNames, PermissionsState permissionsState) {
        List<PermissionState> permissionStates = permissionsState.getInstallPermissionStates();
        if (!permissionStates.isEmpty()) {
            pw.print(prefix);
            pw.println("install permissions:");
            for (PermissionState permissionState : permissionStates) {
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
        if (mIsCheckDelAppsFinished.compareAndSet(DEBUG_STOPPED, true)) {
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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void loadDelAppsFromXml(File configFile) {
        IOException e;
        FileNotFoundException e2;
        Throwable th;
        XmlPullParserException e3;
        if (configFile.exists()) {
            FileInputStream fileInputStream = null;
            try {
                FileInputStream stream = new FileInputStream(configFile);
                try {
                    XmlPullParser parser = Xml.newPullParser();
                    parser.setInput(stream, null);
                    int depth = parser.getDepth();
                    while (true) {
                        int type = parser.next();
                        if ((type != CURRENT_DATABASE_VERSION || parser.getDepth() > depth) && type != 1) {
                            if (type == 2 && parser.getName().equals("del_app")) {
                                this.mDelAppLists.add(parser.getAttributeValue(0));
                            }
                        }
                    }
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (IOException e4) {
                            Slog.e(TAG, "failed close stream " + e4);
                        }
                    }
                } catch (FileNotFoundException e5) {
                    e2 = e5;
                    fileInputStream = stream;
                    try {
                        Slog.e(TAG, "file is not exist " + e2);
                        if (fileInputStream != null) {
                            try {
                                fileInputStream.close();
                            } catch (IOException e42) {
                                Slog.e(TAG, "failed close stream " + e42);
                            }
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (fileInputStream != null) {
                            try {
                                fileInputStream.close();
                            } catch (IOException e422) {
                                Slog.e(TAG, "failed close stream " + e422);
                            }
                        }
                        throw th;
                    }
                } catch (XmlPullParserException e6) {
                    e3 = e6;
                    fileInputStream = stream;
                    Slog.e(TAG, "failed parsing " + configFile + " " + e3);
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e4222) {
                            Slog.e(TAG, "failed close stream " + e4222);
                        }
                    }
                } catch (IOException e7) {
                    e4222 = e7;
                    fileInputStream = stream;
                    Slog.e(TAG, "failed parsing " + configFile + " " + e4222);
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e42222) {
                            Slog.e(TAG, "failed close stream " + e42222);
                        }
                    }
                } catch (Throwable th3) {
                    th = th3;
                    fileInputStream = stream;
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    throw th;
                }
            } catch (FileNotFoundException e8) {
                e2 = e8;
                Slog.e(TAG, "file is not exist " + e2);
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
            } catch (XmlPullParserException e9) {
                e3 = e9;
                Slog.e(TAG, "failed parsing " + configFile + " " + e3);
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
            } catch (IOException e10) {
                e42222 = e10;
                Slog.e(TAG, "failed parsing " + configFile + " " + e42222);
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
            }
        }
    }

    private String getCustomizedFileName(String xmlName) {
        String path = DIR_CUST_XML + xmlName;
        if (new File(path).exists()) {
            return path;
        }
        return DIR_ETC_XML + xmlName;
    }
}
