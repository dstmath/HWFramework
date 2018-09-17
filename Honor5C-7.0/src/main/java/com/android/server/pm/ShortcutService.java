package com.android.server.pm;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.AppGlobals;
import android.app.IUidObserver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.IShortcutService.Stub;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManagerInternal;
import android.content.pm.ParceledListSlice;
import android.content.pm.ResolveInfo;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutServiceInternal;
import android.content.pm.ShortcutServiceInternal.ShortcutChangeListener;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.graphics.drawable.Icon;
import android.os.Binder;
import android.os.Environment;
import android.os.FileUtils;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.SELinux;
import android.os.ShellCommand;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.Global;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.ArraySet;
import android.util.AtomicFile;
import android.util.KeyValueListParser;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.util.SparseLongArray;
import android.util.TypedValue;
import android.util.Xml;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.content.PackageMonitor;
import com.android.internal.os.BackgroundThread;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.Preconditions;
import com.android.server.LocalServices;
import com.android.server.SystemService;
import com.android.server.am.ProcessList;
import com.android.server.power.IHwShutdownThread;
import com.android.server.usb.UsbAudioDevice;
import com.android.server.wm.WindowState;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Predicate;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class ShortcutService extends Stub {
    private static final String ATTR_VALUE = "value";
    static final boolean DEBUG = false;
    static final boolean DEBUG_LOAD = false;
    static final boolean DEBUG_PROCSTATE = false;
    static final String DEFAULT_ICON_PERSIST_FORMAT = null;
    static final int DEFAULT_ICON_PERSIST_QUALITY = 100;
    static final int DEFAULT_MAX_ICON_DIMENSION_DP = 96;
    static final int DEFAULT_MAX_ICON_DIMENSION_LOWRAM_DP = 48;
    static final int DEFAULT_MAX_SHORTCUTS_PER_APP = 5;
    static final int DEFAULT_MAX_UPDATES_PER_INTERVAL = 10;
    static final long DEFAULT_RESET_INTERVAL_SEC = 86400;
    static final int DEFAULT_SAVE_DELAY_MS = 3000;
    static final String DIRECTORY_BITMAPS = "bitmaps";
    static final String DIRECTORY_PER_USER = "shortcut_service";
    public static final boolean FEATURE_ENABLED = false;
    static final String FILENAME_BASE_STATE = "shortcut_service.xml";
    static final String FILENAME_USER_PACKAGES = "shortcuts.xml";
    private static final int PACKAGE_MATCH_FLAGS = 794624;
    private static final int PROCESS_STATE_FOREGROUND_THRESHOLD = 4;
    static final String TAG = "ShortcutService";
    private static final String TAG_LAST_RESET_TIME = "last_reset_time";
    private static final String TAG_LOCALE_CHANGE_SEQUENCE_NUMBER = "locale_seq_no";
    private static final String TAG_ROOT = "root";
    final Context mContext;
    @GuardedBy("mStatLock")
    private final int[] mCountStats;
    @GuardedBy("mLock")
    private List<Integer> mDirtyUserIds;
    @GuardedBy("mStatLock")
    private final long[] mDurationStats;
    private final Handler mHandler;
    private final IPackageManager mIPackageManager;
    private CompressFormat mIconPersistFormat;
    private int mIconPersistQuality;
    @GuardedBy("mLock")
    private final ArrayList<ShortcutChangeListener> mListeners;
    private final AtomicLong mLocaleChangeSequenceNumber;
    private final Object mLock;
    private int mMaxDynamicShortcuts;
    private int mMaxIconDimension;
    int mMaxUpdatesPerInterval;
    private final PackageManagerInternal mPackageManagerInternal;
    final PackageMonitor mPackageMonitor;
    @GuardedBy("mLock")
    private long mRawLastResetTime;
    private long mResetInterval;
    private int mSaveDelayMillis;
    private final Runnable mSaveDirtyInfoRunner;
    final Object mStatLock;
    @GuardedBy("mLock")
    final SparseLongArray mUidLastForegroundElapsedTime;
    private final IUidObserver mUidObserver;
    @GuardedBy("mLock")
    final SparseIntArray mUidState;
    private final UserManager mUserManager;
    @GuardedBy("mLock")
    private final SparseArray<ShortcutUser> mUsers;

    final /* synthetic */ class -android_content_pm_ParceledListSlice_getDynamicShortcuts_java_lang_String_packageName_int_userId_LambdaImpl0 implements Predicate {
        public boolean test(Object arg0) {
            return ((ShortcutInfo) arg0).isDynamic();
        }
    }

    final /* synthetic */ class -android_content_pm_ParceledListSlice_getPinnedShortcuts_java_lang_String_packageName_int_userId_LambdaImpl0 implements Predicate {
        public boolean test(Object arg0) {
            return ((ShortcutInfo) arg0).isPinned();
        }
    }

    final /* synthetic */ class -byte__getBackupPayload_int_userId_LambdaImpl0 implements Consumer {
        private /* synthetic */ ShortcutService val$this;

        public /* synthetic */ -byte__getBackupPayload_int_userId_LambdaImpl0(ShortcutService shortcutService) {
            this.val$this = shortcutService;
        }

        public void accept(Object arg0) {
            this.val$this.-com_android_server_pm_ShortcutService_lambda$15((ShortcutPackageItem) arg0);
        }
    }

    final /* synthetic */ class -void__init__android_content_Context_context_android_os_Looper_looper_LambdaImpl0 implements Runnable {
        private /* synthetic */ ShortcutService val$this;

        public /* synthetic */ -void__init__android_content_Context_context_android_os_Looper_looper_LambdaImpl0(ShortcutService shortcutService) {
            this.val$this = shortcutService;
        }

        public void run() {
            this.val$this.-com_android_server_pm_ShortcutService-mthref-0();
        }
    }

    final /* synthetic */ class -void_checkPackageChanges_int_ownerUserId_LambdaImpl0 implements Consumer {
        private /* synthetic */ ArrayList val$gonePackages;
        private /* synthetic */ int val$ownerUserId;
        private /* synthetic */ ShortcutService val$this;

        public /* synthetic */ -void_checkPackageChanges_int_ownerUserId_LambdaImpl0(ShortcutService shortcutService, int i, ArrayList arrayList) {
            this.val$this = shortcutService;
            this.val$ownerUserId = i;
            this.val$gonePackages = arrayList;
        }

        public void accept(Object arg0) {
            this.val$this.-com_android_server_pm_ShortcutService_lambda$12(this.val$ownerUserId, this.val$gonePackages, (ShortcutPackageItem) arg0);
        }
    }

    final /* synthetic */ class -void_cleanUpPackageForAllLoadedUsers_java_lang_String_packageName_int_packageUserId_LambdaImpl0 implements Consumer {
        private /* synthetic */ String val$packageName;
        private /* synthetic */ int val$packageUserId;
        private /* synthetic */ ShortcutService val$this;

        public /* synthetic */ -void_cleanUpPackageForAllLoadedUsers_java_lang_String_packageName_int_packageUserId_LambdaImpl0(ShortcutService shortcutService, String str, int i) {
            this.val$this = shortcutService;
            this.val$packageName = str;
            this.val$packageUserId = i;
        }

        public void accept(Object arg0) {
            this.val$this.-com_android_server_pm_ShortcutService_lambda$9(this.val$packageName, this.val$packageUserId, (ShortcutUser) arg0);
        }
    }

    final /* synthetic */ class -void_cleanUpPackageLocked_java_lang_String_packageName_int_owningUserId_int_packageUserId_LambdaImpl0 implements Consumer {
        private /* synthetic */ String val$packageName;
        private /* synthetic */ int val$packageUserId;

        public /* synthetic */ -void_cleanUpPackageLocked_java_lang_String_packageName_int_owningUserId_int_packageUserId_LambdaImpl0(String str, int i) {
            this.val$packageName = str;
            this.val$packageUserId = i;
        }

        public void accept(Object arg0) {
            ((ShortcutLauncher) arg0).cleanUpPackage(this.val$packageName, this.val$packageUserId);
        }
    }

    final /* synthetic */ class -void_cleanUpPackageLocked_java_lang_String_packageName_int_owningUserId_int_packageUserId_LambdaImpl1 implements Consumer {
        private /* synthetic */ ShortcutService val$this;

        public /* synthetic */ -void_cleanUpPackageLocked_java_lang_String_packageName_int_owningUserId_int_packageUserId_LambdaImpl1(ShortcutService shortcutService) {
            this.val$this = shortcutService;
        }

        public void accept(Object arg0) {
            this.val$this.-com_android_server_pm_ShortcutService_lambda$11((ShortcutPackage) arg0);
        }
    }

    final /* synthetic */ class -void_handlePackageAdded_java_lang_String_packageName_int_userId_LambdaImpl0 implements Consumer {
        private /* synthetic */ String val$packageName;
        private /* synthetic */ ShortcutService val$this;
        private /* synthetic */ int val$userId;

        public /* synthetic */ -void_handlePackageAdded_java_lang_String_packageName_int_userId_LambdaImpl0(ShortcutService shortcutService, String str, int i) {
            this.val$this = shortcutService;
            this.val$packageName = str;
            this.val$userId = i;
        }

        public void accept(Object arg0) {
            this.val$this.-com_android_server_pm_ShortcutService_lambda$13(this.val$packageName, this.val$userId, (ShortcutUser) arg0);
        }
    }

    final /* synthetic */ class -void_handlePackageUpdateFinished_java_lang_String_packageName_int_userId_LambdaImpl0 implements Consumer {
        private /* synthetic */ String val$packageName;
        private /* synthetic */ ShortcutService val$this;
        private /* synthetic */ int val$userId;

        public /* synthetic */ -void_handlePackageUpdateFinished_java_lang_String_packageName_int_userId_LambdaImpl0(ShortcutService shortcutService, String str, int i) {
            this.val$this = shortcutService;
            this.val$packageName = str;
            this.val$userId = i;
        }

        public void accept(Object arg0) {
            this.val$this.-com_android_server_pm_ShortcutService_lambda$14(this.val$packageName, this.val$userId, (ShortcutUser) arg0);
        }
    }

    final /* synthetic */ class -void_notifyListeners_java_lang_String_packageName_int_userId_LambdaImpl0 implements Runnable {
        private /* synthetic */ String val$packageName;
        private /* synthetic */ ShortcutService val$this;
        private /* synthetic */ int val$userId;

        public /* synthetic */ -void_notifyListeners_java_lang_String_packageName_int_userId_LambdaImpl0(ShortcutService shortcutService, String str, int i) {
            this.val$this = shortcutService;
            this.val$packageName = str;
            this.val$userId = i;
        }

        public void run() {
            this.val$this.-com_android_server_pm_ShortcutService_lambda$6(this.val$packageName, this.val$userId);
        }
    }

    static class CommandException extends Exception {
        public CommandException(String message) {
            super(message);
        }
    }

    interface ConfigConstants {
        public static final String KEY_ICON_FORMAT = "icon_format";
        public static final String KEY_ICON_QUALITY = "icon_quality";
        public static final String KEY_MAX_ICON_DIMENSION_DP = "max_icon_dimension_dp";
        public static final String KEY_MAX_ICON_DIMENSION_DP_LOWRAM = "max_icon_dimension_dp_lowram";
        public static final String KEY_MAX_SHORTCUTS = "max_shortcuts";
        public static final String KEY_MAX_UPDATES_PER_INTERVAL = "max_updates_per_interval";
        public static final String KEY_RESET_INTERVAL_SEC = "reset_interval_sec";
        public static final String KEY_SAVE_DELAY_MILLIS = "save_delay_ms";
    }

    static class FileOutputStreamWithPath extends FileOutputStream {
        private final File mFile;

        public FileOutputStreamWithPath(File file) throws FileNotFoundException {
            super(file);
            this.mFile = file;
        }

        public File getFile() {
            return this.mFile;
        }
    }

    public static final class Lifecycle extends SystemService {
        final ShortcutService mService;

        public Lifecycle(Context context) {
            super(context);
            this.mService = new ShortcutService(context);
        }

        public void onStart() {
            publishBinderService("shortcut", this.mService);
        }

        public void onBootPhase(int phase) {
            this.mService.onBootPhase(phase);
        }

        public void onCleanupUser(int userHandle) {
            this.mService.handleCleanupUser(userHandle);
        }

        public void onUnlockUser(int userId) {
            this.mService.handleUnlockUser(userId);
        }
    }

    private class LocalService extends ShortcutServiceInternal {

        final /* synthetic */ class -android_content_pm_ShortcutInfo_getShortcutInfoLocked_int_launcherUserId_java_lang_String_callingPackage_java_lang_String_packageName_java_lang_String_shortcutId_int_userId_LambdaImpl0 implements Predicate {
            private /* synthetic */ String val$shortcutId;

            public /* synthetic */ -android_content_pm_ShortcutInfo_getShortcutInfoLocked_int_launcherUserId_java_lang_String_callingPackage_java_lang_String_packageName_java_lang_String_shortcutId_int_userId_LambdaImpl0(String str) {
                this.val$shortcutId = str;
            }

            public boolean test(Object arg0) {
                return this.val$shortcutId.equals(((ShortcutInfo) arg0).getId());
            }
        }

        final /* synthetic */ class -java_util_List_getShortcuts_int_launcherUserId_java_lang_String_callingPackage_long_changedSince_java_lang_String_packageName_java_util_List_shortcutIds_android_content_ComponentName_componentName_int_queryFlags_int_userId_LambdaImpl0 implements Consumer {
            private /* synthetic */ String val$callingPackage;
            private /* synthetic */ long val$changedSince;
            private /* synthetic */ int val$cloneFlag;
            private /* synthetic */ ComponentName val$componentName;
            private /* synthetic */ int val$launcherUserId;
            private /* synthetic */ int val$queryFlags;
            private /* synthetic */ ArrayList val$ret;
            private /* synthetic */ List val$shortcutIdsF;
            private /* synthetic */ LocalService val$this;
            private /* synthetic */ int val$userId;

            public /* synthetic */ -java_util_List_getShortcuts_int_launcherUserId_java_lang_String_callingPackage_long_changedSince_java_lang_String_packageName_java_util_List_shortcutIds_android_content_ComponentName_componentName_int_queryFlags_int_userId_LambdaImpl0(LocalService localService, int i, String str, List list, long j, ComponentName componentName, int i2, int i3, ArrayList arrayList, int i4) {
                this.val$this = localService;
                this.val$launcherUserId = i;
                this.val$callingPackage = str;
                this.val$shortcutIdsF = list;
                this.val$changedSince = j;
                this.val$componentName = componentName;
                this.val$queryFlags = i2;
                this.val$userId = i3;
                this.val$ret = arrayList;
                this.val$cloneFlag = i4;
            }

            public void accept(Object arg0) {
                this.val$this.-com_android_server_pm_ShortcutService$LocalService_lambda$1(this.val$launcherUserId, this.val$callingPackage, this.val$shortcutIdsF, this.val$changedSince, this.val$componentName, this.val$queryFlags, this.val$userId, this.val$ret, this.val$cloneFlag, (ShortcutPackage) arg0);
            }
        }

        final /* synthetic */ class -void_getShortcutsInnerLocked_int_launcherUserId_java_lang_String_callingPackage_java_lang_String_packageName_java_util_List_shortcutIds_long_changedSince_android_content_ComponentName_componentName_int_queryFlags_int_userId_java_util_ArrayList_ret_int_cloneFlag_LambdaImpl0 implements Predicate {
            private /* synthetic */ long val$changedSince;
            private /* synthetic */ ComponentName val$componentName;
            private /* synthetic */ ArraySet val$ids;
            private /* synthetic */ int val$queryFlags;

            public /* synthetic */ -void_getShortcutsInnerLocked_int_launcherUserId_java_lang_String_callingPackage_java_lang_String_packageName_java_util_List_shortcutIds_long_changedSince_android_content_ComponentName_componentName_int_queryFlags_int_userId_java_util_ArrayList_ret_int_cloneFlag_LambdaImpl0(long j, ArraySet arraySet, ComponentName componentName, int i) {
                this.val$changedSince = j;
                this.val$ids = arraySet;
                this.val$componentName = componentName;
                this.val$queryFlags = i;
            }

            public boolean test(Object arg0) {
                return LocalService.-com_android_server_pm_ShortcutService$LocalService_lambda$2(this.val$changedSince, this.val$ids, this.val$componentName, this.val$queryFlags, (ShortcutInfo) arg0);
            }
        }

        final /* synthetic */ class -void_onSystemLocaleChangedNoLock__LambdaImpl0 implements Runnable {
            private /* synthetic */ LocalService val$this;

            public /* synthetic */ -void_onSystemLocaleChangedNoLock__LambdaImpl0(LocalService localService) {
                this.val$this = localService;
            }

            public void run() {
                this.val$this.-com_android_server_pm_ShortcutService$LocalService_lambda$4();
            }
        }

        private LocalService() {
        }

        public List<ShortcutInfo> getShortcuts(int launcherUserId, String callingPackage, long changedSince, String packageName, List<String> shortcutIds, ComponentName componentName, int queryFlags, int userId) {
            int cloneFlag;
            ArrayList<ShortcutInfo> ret = new ArrayList();
            if ((queryFlags & ShortcutService.PROCESS_STATE_FOREGROUND_THRESHOLD) == 0) {
                cloneFlag = 3;
            } else {
                cloneFlag = ShortcutService.PROCESS_STATE_FOREGROUND_THRESHOLD;
            }
            if (packageName == null) {
                shortcutIds = null;
            }
            synchronized (ShortcutService.this.mLock) {
                ShortcutService.this.getLauncherShortcutsLocked(callingPackage, userId, launcherUserId).attemptToRestoreIfNeededAndSave(ShortcutService.this);
                if (packageName != null) {
                    getShortcutsInnerLocked(launcherUserId, callingPackage, packageName, shortcutIds, changedSince, componentName, queryFlags, userId, ret, cloneFlag);
                } else {
                    ShortcutService.this.getUserShortcutsLocked(userId).forAllPackages(new -java_util_List_getShortcuts_int_launcherUserId_java_lang_String_callingPackage_long_changedSince_java_lang_String_packageName_java_util_List_shortcutIds_android_content_ComponentName_componentName_int_queryFlags_int_userId_LambdaImpl0(this, launcherUserId, callingPackage, shortcutIds, changedSince, componentName, queryFlags, userId, ret, cloneFlag));
                }
            }
            return ret;
        }

        /* synthetic */ void -com_android_server_pm_ShortcutService$LocalService_lambda$1(int launcherUserId, String callingPackage, List shortcutIdsF, long changedSince, ComponentName componentName, int queryFlags, int userId, ArrayList ret, int cloneFlag, ShortcutPackage p) {
            getShortcutsInnerLocked(launcherUserId, callingPackage, p.getPackageName(), shortcutIdsF, changedSince, componentName, queryFlags, userId, ret, cloneFlag);
        }

        private void getShortcutsInnerLocked(int launcherUserId, String callingPackage, String packageName, List<String> shortcutIds, long changedSince, ComponentName componentName, int queryFlags, int userId, ArrayList<ShortcutInfo> ret, int cloneFlag) {
            ArraySet arraySet;
            if (shortcutIds == null) {
                arraySet = null;
            } else {
                arraySet = new ArraySet(shortcutIds);
            }
            ShortcutService.this.getPackageShortcutsLocked(packageName, userId).findAll(ShortcutService.this, ret, new -void_getShortcutsInnerLocked_int_launcherUserId_java_lang_String_callingPackage_java_lang_String_packageName_java_util_List_shortcutIds_long_changedSince_android_content_ComponentName_componentName_int_queryFlags_int_userId_java_util_ArrayList_ret_int_cloneFlag_LambdaImpl0(changedSince, arraySet, componentName, queryFlags), cloneFlag, callingPackage, launcherUserId);
        }

        static /* synthetic */ boolean -com_android_server_pm_ShortcutService$LocalService_lambda$2(long changedSince, ArraySet ids, ComponentName componentName, int queryFlags, ShortcutInfo si) {
            if (si.getLastChangedTimestamp() < changedSince) {
                return ShortcutService.FEATURE_ENABLED;
            }
            if (ids != null && !ids.contains(si.getId())) {
                return ShortcutService.FEATURE_ENABLED;
            }
            if (componentName != null && !componentName.equals(si.getActivityComponent())) {
                return ShortcutService.FEATURE_ENABLED;
            }
            boolean isDynamic;
            boolean isPinned;
            if ((queryFlags & 1) != 0) {
                isDynamic = si.isDynamic();
            } else {
                isDynamic = ShortcutService.FEATURE_ENABLED;
            }
            if ((queryFlags & 2) != 0) {
                isPinned = si.isPinned();
            } else {
                isPinned = ShortcutService.FEATURE_ENABLED;
            }
            if (isDynamic) {
                isPinned = true;
            }
            return isPinned;
        }

        public boolean isPinnedByCaller(int launcherUserId, String callingPackage, String packageName, String shortcutId, int userId) {
            boolean isPinned;
            Preconditions.checkStringNotEmpty(packageName, "packageName");
            Preconditions.checkStringNotEmpty(shortcutId, "shortcutId");
            synchronized (ShortcutService.this.mLock) {
                ShortcutService.this.getLauncherShortcutsLocked(callingPackage, userId, launcherUserId).attemptToRestoreIfNeededAndSave(ShortcutService.this);
                ShortcutInfo si = getShortcutInfoLocked(launcherUserId, callingPackage, packageName, shortcutId, userId);
                isPinned = si != null ? si.isPinned() : ShortcutService.FEATURE_ENABLED;
            }
            return isPinned;
        }

        private ShortcutInfo getShortcutInfoLocked(int launcherUserId, String callingPackage, String packageName, String shortcutId, int userId) {
            Preconditions.checkStringNotEmpty(packageName, "packageName");
            Preconditions.checkStringNotEmpty(shortcutId, "shortcutId");
            ArrayList<ShortcutInfo> list = new ArrayList(1);
            ShortcutService.this.getPackageShortcutsLocked(packageName, userId).findAll(ShortcutService.this, list, new -android_content_pm_ShortcutInfo_getShortcutInfoLocked_int_launcherUserId_java_lang_String_callingPackage_java_lang_String_packageName_java_lang_String_shortcutId_int_userId_LambdaImpl0(shortcutId), 0, callingPackage, launcherUserId);
            return list.size() == 0 ? null : (ShortcutInfo) list.get(0);
        }

        public void pinShortcuts(int launcherUserId, String callingPackage, String packageName, List<String> shortcutIds, int userId) {
            Preconditions.checkStringNotEmpty(packageName, "packageName");
            Preconditions.checkNotNull(shortcutIds, "shortcutIds");
            synchronized (ShortcutService.this.mLock) {
                ShortcutLauncher launcher = ShortcutService.this.getLauncherShortcutsLocked(callingPackage, userId, launcherUserId);
                launcher.attemptToRestoreIfNeededAndSave(ShortcutService.this);
                launcher.pinShortcuts(ShortcutService.this, userId, packageName, shortcutIds);
            }
            ShortcutService.this.packageShortcutsChanged(packageName, userId);
        }

        public Intent createShortcutIntent(int launcherUserId, String callingPackage, String packageName, String shortcutId, int userId) {
            Preconditions.checkStringNotEmpty(packageName, "packageName can't be empty");
            Preconditions.checkStringNotEmpty(shortcutId, "shortcutId can't be empty");
            synchronized (ShortcutService.this.mLock) {
                ShortcutService.this.getLauncherShortcutsLocked(callingPackage, userId, launcherUserId).attemptToRestoreIfNeededAndSave(ShortcutService.this);
                ShortcutInfo si = getShortcutInfoLocked(launcherUserId, callingPackage, packageName, shortcutId, userId);
                if (si == null || !(si.isDynamic() || si.isPinned())) {
                    return null;
                }
                Intent intent = si.getIntent();
                return intent;
            }
        }

        public void addListener(ShortcutChangeListener listener) {
            synchronized (ShortcutService.this.mLock) {
                ShortcutService.this.mListeners.add((ShortcutChangeListener) Preconditions.checkNotNull(listener));
            }
        }

        public int getShortcutIconResId(int launcherUserId, String callingPackage, String packageName, String shortcutId, int userId) {
            int iconResourceId;
            Preconditions.checkNotNull(callingPackage, "callingPackage");
            Preconditions.checkNotNull(packageName, "packageName");
            Preconditions.checkNotNull(shortcutId, "shortcutId");
            synchronized (ShortcutService.this.mLock) {
                ShortcutService.this.getLauncherShortcutsLocked(callingPackage, userId, launcherUserId).attemptToRestoreIfNeededAndSave(ShortcutService.this);
                ShortcutInfo shortcutInfo = ShortcutService.this.getPackageShortcutsLocked(packageName, userId).findShortcutById(shortcutId);
                iconResourceId = (shortcutInfo == null || !shortcutInfo.hasIconResource()) ? 0 : shortcutInfo.getIconResourceId();
            }
            return iconResourceId;
        }

        public ParcelFileDescriptor getShortcutIconFd(int launcherUserId, String callingPackage, String packageName, String shortcutId, int userId) {
            Preconditions.checkNotNull(callingPackage, "callingPackage");
            Preconditions.checkNotNull(packageName, "packageName");
            Preconditions.checkNotNull(shortcutId, "shortcutId");
            synchronized (ShortcutService.this.mLock) {
                ShortcutService.this.getLauncherShortcutsLocked(callingPackage, userId, launcherUserId).attemptToRestoreIfNeededAndSave(ShortcutService.this);
                ShortcutInfo shortcutInfo = ShortcutService.this.getPackageShortcutsLocked(packageName, userId).findShortcutById(shortcutId);
                if (shortcutInfo == null || !shortcutInfo.hasIconFile()) {
                    return null;
                }
                try {
                    if (shortcutInfo.getBitmapPath() == null) {
                        Slog.w(ShortcutService.TAG, "null bitmap detected in getShortcutIconFd()");
                        return null;
                    }
                    ParcelFileDescriptor open = ParcelFileDescriptor.open(new File(shortcutInfo.getBitmapPath()), 268435456);
                    return open;
                } catch (FileNotFoundException e) {
                    Slog.e(ShortcutService.TAG, "Icon file not found: " + shortcutInfo.getBitmapPath());
                    return null;
                }
            }
        }

        public boolean hasShortcutHostPermission(int launcherUserId, String callingPackage) {
            return ShortcutService.this.hasShortcutHostPermission(callingPackage, launcherUserId);
        }

        public void onSystemLocaleChangedNoLock() {
        }

        /* synthetic */ void -com_android_server_pm_ShortcutService$LocalService_lambda$4() {
            ShortcutService.this.scheduleSaveBaseState();
        }
    }

    private class MyShellCommand extends ShellCommand {
        private int mUserId;

        private MyShellCommand() {
            this.mUserId = 0;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private void parseOptions(boolean takeUser) throws CommandException {
            String opt;
            while (true) {
                opt = getNextOption();
                if (opt == null) {
                    return;
                }
                if (opt.equals("--user") && takeUser) {
                    this.mUserId = UserHandle.parseUserArg(getNextArgRequired());
                }
            }
            throw new CommandException("Unknown option: " + opt);
        }

        public int onCommand(String cmd) {
            if (cmd == null) {
                return handleDefaultCommands(cmd);
            }
            PrintWriter pw = getOutPrintWriter();
            try {
                if (cmd.equals("reset-package-throttling")) {
                    handleResetPackageThrottling();
                } else if (cmd.equals("reset-throttling")) {
                    handleResetThrottling();
                } else if (cmd.equals("reset-all-throttling")) {
                    handleResetAllThrottling();
                } else if (cmd.equals("override-config")) {
                    handleOverrideConfig();
                } else if (cmd.equals("reset-config")) {
                    handleResetConfig();
                } else if (cmd.equals("clear-default-launcher")) {
                    handleClearDefaultLauncher();
                } else if (cmd.equals("get-default-launcher")) {
                    handleGetDefaultLauncher();
                } else if (cmd.equals("refresh-default-launcher")) {
                    handleRefreshDefaultLauncher();
                } else if (cmd.equals("unload-user")) {
                    handleUnloadUser();
                } else if (!cmd.equals("clear-shortcuts")) {
                    return handleDefaultCommands(cmd);
                } else {
                    handleClearShortcuts();
                }
                pw.println("Success");
                return 0;
            } catch (CommandException e) {
                pw.println("Error: " + e.getMessage());
                return 1;
            }
        }

        public void onHelp() {
            PrintWriter pw = getOutPrintWriter();
            pw.println("Usage: cmd shortcut COMMAND [options ...]");
            pw.println();
            pw.println("cmd shortcut reset-package-throttling [--user USER_ID] PACKAGE");
            pw.println("    Reset throttling for a package");
            pw.println();
            pw.println("cmd shortcut reset-throttling [--user USER_ID]");
            pw.println("    Reset throttling for all packages and users");
            pw.println();
            pw.println("cmd shortcut reset-all-throttling");
            pw.println("    Reset the throttling state for all users");
            pw.println();
            pw.println("cmd shortcut override-config CONFIG");
            pw.println("    Override the configuration for testing (will last until reboot)");
            pw.println();
            pw.println("cmd shortcut reset-config");
            pw.println("    Reset the configuration set with \"update-config\"");
            pw.println();
            pw.println("cmd shortcut clear-default-launcher [--user USER_ID]");
            pw.println("    Clear the cached default launcher");
            pw.println();
            pw.println("cmd shortcut get-default-launcher [--user USER_ID]");
            pw.println("    Show the cached default launcher");
            pw.println();
            pw.println("cmd shortcut refresh-default-launcher [--user USER_ID]");
            pw.println("    Refresh the cached default launcher");
            pw.println();
            pw.println("cmd shortcut unload-user [--user USER_ID]");
            pw.println("    Unload a user from the memory");
            pw.println("    (This should not affect any observable behavior)");
            pw.println();
            pw.println("cmd shortcut clear-shortcuts [--user USER_ID] PACKAGE");
            pw.println("    Remove all shortcuts from a package, including pinned shortcuts");
            pw.println();
        }

        private void handleResetThrottling() throws CommandException {
            parseOptions(true);
            Slog.i(ShortcutService.TAG, "cmd: handleResetThrottling");
            ShortcutService.this.resetThrottlingInner(this.mUserId);
        }

        private void handleResetAllThrottling() {
            Slog.i(ShortcutService.TAG, "cmd: handleResetAllThrottling");
            ShortcutService.this.resetAllThrottlingInner();
        }

        private void handleResetPackageThrottling() throws CommandException {
            parseOptions(true);
            String packageName = getNextArgRequired();
            Slog.i(ShortcutService.TAG, "cmd: handleResetPackageThrottling: " + packageName);
            ShortcutService.this.resetPackageThrottling(packageName, this.mUserId);
        }

        private void handleOverrideConfig() throws CommandException {
            String config = getNextArgRequired();
            Slog.i(ShortcutService.TAG, "cmd: handleOverrideConfig: " + config);
            synchronized (ShortcutService.this.mLock) {
                if (ShortcutService.this.updateConfigurationLocked(config)) {
                } else {
                    throw new CommandException("override-config failed.  See logcat for details.");
                }
            }
        }

        private void handleResetConfig() {
            Slog.i(ShortcutService.TAG, "cmd: handleResetConfig");
            synchronized (ShortcutService.this.mLock) {
                ShortcutService.this.loadConfigurationLocked();
            }
        }

        private void clearLauncher() {
            synchronized (ShortcutService.this.mLock) {
                ShortcutService.this.getUserShortcutsLocked(this.mUserId).setLauncherComponent(ShortcutService.this, null);
            }
        }

        private void showLauncher() {
            synchronized (ShortcutService.this.mLock) {
                ShortcutService.this.hasShortcutHostPermissionInner("-", this.mUserId);
                getOutPrintWriter().println("Launcher: " + ShortcutService.this.getUserShortcutsLocked(this.mUserId).getLauncherComponent());
            }
        }

        private void handleClearDefaultLauncher() throws CommandException {
            parseOptions(true);
            clearLauncher();
        }

        private void handleGetDefaultLauncher() throws CommandException {
            parseOptions(true);
            showLauncher();
        }

        private void handleRefreshDefaultLauncher() throws CommandException {
            parseOptions(true);
            clearLauncher();
            showLauncher();
        }

        private void handleUnloadUser() throws CommandException {
            parseOptions(true);
            Slog.i(ShortcutService.TAG, "cmd: handleUnloadUser: " + this.mUserId);
            ShortcutService.this.handleCleanupUser(this.mUserId);
        }

        private void handleClearShortcuts() throws CommandException {
            parseOptions(true);
            String packageName = getNextArgRequired();
            Slog.i(ShortcutService.TAG, "cmd: handleClearShortcuts: " + this.mUserId + ", " + packageName);
            ShortcutService.this.cleanUpPackageForAllLoadedUsers(packageName, this.mUserId);
        }
    }

    interface Stats {
        public static final int COUNT = 5;
        public static final int GET_APPLICATION_INFO = 3;
        public static final int GET_DEFAULT_HOME = 0;
        public static final int GET_PACKAGE_INFO = 1;
        public static final int GET_PACKAGE_INFO_WITH_SIG = 2;
        public static final int LAUNCHER_PERMISSION_CHECK = 4;
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.pm.ShortcutService.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.pm.ShortcutService.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.pm.ShortcutService.<clinit>():void");
    }

    android.content.pm.ApplicationInfo injectApplicationInfo(java.lang.String r9, int r10) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find immediate dominator for block B:12:? in {3, 8, 9, 11, 13, 14} preds:[]
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.computeDominators(BlockProcessor.java:129)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.processBlocksTree(BlockProcessor.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.rerun(BlockProcessor.java:44)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:57)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
        /*
        r8 = this;
        r7 = 3;
        r2 = java.lang.System.currentTimeMillis();
        r4 = r8.injectClearCallingIdentity();
        r1 = r8.mIPackageManager;	 Catch:{ RemoteException -> 0x0019, all -> 0x002b }
        r6 = 794624; // 0xc2000 float:1.113505E-39 double:3.925964E-318;	 Catch:{ RemoteException -> 0x0019, all -> 0x002b }
        r1 = r1.getApplicationInfo(r9, r6, r10);	 Catch:{ RemoteException -> 0x0019, all -> 0x002b }
        r8.injectRestoreCallingIdentity(r4);
        r8.logDurationStat(r7, r2);
        return r1;
    L_0x0019:
        r0 = move-exception;
        r1 = "ShortcutService";	 Catch:{ RemoteException -> 0x0019, all -> 0x002b }
        r6 = "RemoteException";	 Catch:{ RemoteException -> 0x0019, all -> 0x002b }
        android.util.Slog.wtf(r1, r6, r0);	 Catch:{ RemoteException -> 0x0019, all -> 0x002b }
        r1 = 0;
        r8.injectRestoreCallingIdentity(r4);
        r8.logDurationStat(r7, r2);
        return r1;
    L_0x002b:
        r1 = move-exception;
        r8.injectRestoreCallingIdentity(r4);
        r8.logDurationStat(r7, r2);
        throw r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.pm.ShortcutService.injectApplicationInfo(java.lang.String, int):android.content.pm.ApplicationInfo");
    }

    int injectGetPackageUid(java.lang.String r6, int r7) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find immediate dominator for block B:12:? in {3, 8, 9, 11, 13, 14} preds:[]
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.computeDominators(BlockProcessor.java:129)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.processBlocksTree(BlockProcessor.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.rerun(BlockProcessor.java:44)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:57)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
        /*
        r5 = this;
        r2 = r5.injectClearCallingIdentity();
        r1 = r5.mIPackageManager;	 Catch:{ RemoteException -> 0x0011, all -> 0x0020 }
        r4 = 794624; // 0xc2000 float:1.113505E-39 double:3.925964E-318;	 Catch:{ RemoteException -> 0x0011, all -> 0x0020 }
        r1 = r1.getPackageUid(r6, r4, r7);	 Catch:{ RemoteException -> 0x0011, all -> 0x0020 }
        r5.injectRestoreCallingIdentity(r2);
        return r1;
    L_0x0011:
        r0 = move-exception;
        r1 = "ShortcutService";	 Catch:{ RemoteException -> 0x0011, all -> 0x0020 }
        r4 = "RemoteException";	 Catch:{ RemoteException -> 0x0011, all -> 0x0020 }
        android.util.Slog.wtf(r1, r4, r0);	 Catch:{ RemoteException -> 0x0011, all -> 0x0020 }
        r1 = -1;
        r5.injectRestoreCallingIdentity(r2);
        return r1;
    L_0x0020:
        r1 = move-exception;
        r5.injectRestoreCallingIdentity(r2);
        throw r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.pm.ShortcutService.injectGetPackageUid(java.lang.String, int):int");
    }

    android.content.pm.PackageInfo injectPackageInfo(java.lang.String r11, int r12, boolean r13) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find immediate dominator for block B:24:? in {4, 8, 10, 11, 12, 17, 18, 22, 23, 25} preds:[]
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.computeDominators(BlockProcessor.java:129)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.processBlocksTree(BlockProcessor.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.rerun(BlockProcessor.java:44)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:57)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
        /*
        r10 = this;
        r6 = 2;
        r7 = 1;
        r2 = java.lang.System.currentTimeMillis();
        r4 = r10.injectClearCallingIdentity();
        r8 = r10.mIPackageManager;	 Catch:{ RemoteException -> 0x0026, all -> 0x003c }
        if (r13 == 0) goto L_0x0022;	 Catch:{ RemoteException -> 0x0026, all -> 0x003c }
    L_0x000e:
        r1 = 64;	 Catch:{ RemoteException -> 0x0026, all -> 0x003c }
    L_0x0010:
        r9 = 794624; // 0xc2000 float:1.113505E-39 double:3.925964E-318;	 Catch:{ RemoteException -> 0x0026, all -> 0x003c }
        r1 = r1 | r9;	 Catch:{ RemoteException -> 0x0026, all -> 0x003c }
        r8 = r8.getPackageInfo(r11, r1, r12);	 Catch:{ RemoteException -> 0x0026, all -> 0x003c }
        r10.injectRestoreCallingIdentity(r4);
        if (r13 == 0) goto L_0x0024;
    L_0x001d:
        r1 = r6;
    L_0x001e:
        r10.logDurationStat(r1, r2);
        return r8;
    L_0x0022:
        r1 = 0;
        goto L_0x0010;
    L_0x0024:
        r1 = r7;
        goto L_0x001e;
    L_0x0026:
        r0 = move-exception;
        r1 = "ShortcutService";	 Catch:{ RemoteException -> 0x0026, all -> 0x003c }
        r8 = "RemoteException";	 Catch:{ RemoteException -> 0x0026, all -> 0x003c }
        android.util.Slog.wtf(r1, r8, r0);	 Catch:{ RemoteException -> 0x0026, all -> 0x003c }
        r1 = 0;
        r10.injectRestoreCallingIdentity(r4);
        if (r13 == 0) goto L_0x003a;
    L_0x0036:
        r10.logDurationStat(r6, r2);
        return r1;
    L_0x003a:
        r6 = r7;
        goto L_0x0036;
    L_0x003c:
        r1 = move-exception;
        r10.injectRestoreCallingIdentity(r4);
        if (r13 == 0) goto L_0x0046;
    L_0x0042:
        r10.logDurationStat(r6, r2);
        throw r1;
    L_0x0046:
        r6 = r7;
        goto L_0x0042;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.pm.ShortcutService.injectPackageInfo(java.lang.String, int, boolean):android.content.pm.PackageInfo");
    }

    public ShortcutService(Context context) {
        this(context, BackgroundThread.get().getLooper());
    }

    ShortcutService(Context context, Looper looper) {
        this.mLock = new Object();
        this.mListeners = new ArrayList(1);
        this.mUsers = new SparseArray();
        this.mUidState = new SparseIntArray();
        this.mUidLastForegroundElapsedTime = new SparseLongArray();
        this.mDirtyUserIds = new ArrayList();
        this.mLocaleChangeSequenceNumber = new AtomicLong();
        this.mStatLock = new Object();
        this.mCountStats = new int[DEFAULT_MAX_SHORTCUTS_PER_APP];
        this.mDurationStats = new long[DEFAULT_MAX_SHORTCUTS_PER_APP];
        this.mUidObserver = new IUidObserver.Stub() {
            public void onUidStateChanged(int uid, int procState) throws RemoteException {
                ShortcutService.this.handleOnUidStateChanged(uid, procState);
            }

            public void onUidGone(int uid) throws RemoteException {
                ShortcutService.this.handleOnUidStateChanged(uid, 16);
            }

            public void onUidActive(int uid) throws RemoteException {
            }

            public void onUidIdle(int uid) throws RemoteException {
            }
        };
        this.mSaveDirtyInfoRunner = new -void__init__android_content_Context_context_android_os_Looper_looper_LambdaImpl0();
        this.mPackageMonitor = new PackageMonitor() {
            public void onPackageAdded(String packageName, int uid) {
                ShortcutService.this.handlePackageAdded(packageName, getChangingUserId());
            }

            public void onPackageUpdateFinished(String packageName, int uid) {
                ShortcutService.this.handlePackageUpdateFinished(packageName, getChangingUserId());
            }

            public void onPackageRemoved(String packageName, int uid) {
                ShortcutService.this.handlePackageRemoved(packageName, getChangingUserId());
            }

            public void onPackageDataCleared(String packageName, int uid) {
                ShortcutService.this.handlePackageDataCleared(packageName, getChangingUserId());
            }
        };
        this.mContext = (Context) Preconditions.checkNotNull(context);
        LocalServices.addService(ShortcutServiceInternal.class, new LocalService());
        this.mHandler = new Handler(looper);
        this.mIPackageManager = AppGlobals.getPackageManager();
        this.mPackageManagerInternal = (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class);
        this.mUserManager = (UserManager) context.getSystemService(UserManager.class);
    }

    void logDurationStat(int statId, long start) {
        synchronized (this.mStatLock) {
            int[] iArr = this.mCountStats;
            iArr[statId] = iArr[statId] + 1;
            long[] jArr = this.mDurationStats;
            jArr[statId] = jArr[statId] + (System.currentTimeMillis() - start);
        }
    }

    public long getLocaleChangeSequenceNumber() {
        return this.mLocaleChangeSequenceNumber.get();
    }

    void handleOnUidStateChanged(int uid, int procState) {
        synchronized (this.mLock) {
            this.mUidState.put(uid, procState);
            if (isProcessStateForeground(procState)) {
                this.mUidLastForegroundElapsedTime.put(uid, injectElapsedRealtime());
            }
        }
    }

    private boolean isProcessStateForeground(int processState) {
        return processState <= PROCESS_STATE_FOREGROUND_THRESHOLD ? true : FEATURE_ENABLED;
    }

    boolean isUidForegroundLocked(int uid) {
        if (uid == ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE) {
            return true;
        }
        return isProcessStateForeground(this.mUidState.get(uid, 16));
    }

    long getUidLastForegroundElapsedTimeLocked(int uid) {
        return this.mUidLastForegroundElapsedTime.get(uid);
    }

    void onBootPhase(int phase) {
        switch (phase) {
            case SystemService.PHASE_LOCK_SETTINGS_READY /*480*/:
                initialize();
            default:
        }
    }

    void handleUnlockUser(int userId) {
    }

    void handleCleanupUser(int userId) {
    }

    private void unloadUserLocked(int userId) {
        saveDirtyInfo();
        this.mUsers.delete(userId);
    }

    private AtomicFile getBaseStateFile() {
        File path = new File(injectSystemDataPath(), FILENAME_BASE_STATE);
        path.mkdirs();
        return new AtomicFile(path);
    }

    private void initialize() {
        synchronized (this.mLock) {
            loadConfigurationLocked();
            loadBaseStateLocked();
        }
    }

    private void loadConfigurationLocked() {
        updateConfigurationLocked(injectShortcutManagerConstants());
    }

    boolean updateConfigurationLocked(String config) {
        int i;
        boolean result = true;
        KeyValueListParser parser = new KeyValueListParser(',');
        try {
            parser.setString(config);
        } catch (IllegalArgumentException e) {
            Slog.e(TAG, "Bad shortcut manager settings", e);
            result = FEATURE_ENABLED;
        }
        this.mSaveDelayMillis = Math.max(0, (int) parser.getLong(ConfigConstants.KEY_SAVE_DELAY_MILLIS, 3000));
        this.mResetInterval = Math.max(1, parser.getLong(ConfigConstants.KEY_RESET_INTERVAL_SEC, DEFAULT_RESET_INTERVAL_SEC) * 1000);
        this.mMaxUpdatesPerInterval = Math.max(0, (int) parser.getLong(ConfigConstants.KEY_MAX_UPDATES_PER_INTERVAL, 10));
        this.mMaxDynamicShortcuts = Math.max(0, (int) parser.getLong(ConfigConstants.KEY_MAX_SHORTCUTS, 5));
        if (injectIsLowRamDevice()) {
            i = (int) parser.getLong(ConfigConstants.KEY_MAX_ICON_DIMENSION_DP_LOWRAM, 48);
        } else {
            i = (int) parser.getLong(ConfigConstants.KEY_MAX_ICON_DIMENSION_DP, 96);
        }
        this.mMaxIconDimension = injectDipToPixel(Math.max(1, i));
        this.mIconPersistFormat = CompressFormat.valueOf(parser.getString(ConfigConstants.KEY_ICON_FORMAT, DEFAULT_ICON_PERSIST_FORMAT));
        this.mIconPersistQuality = (int) parser.getLong(ConfigConstants.KEY_ICON_QUALITY, 100);
        return result;
    }

    String injectShortcutManagerConstants() {
        return Global.getString(this.mContext.getContentResolver(), "shortcut_manager_constants");
    }

    int injectDipToPixel(int dip) {
        return (int) TypedValue.applyDimension(1, (float) dip, this.mContext.getResources().getDisplayMetrics());
    }

    static String parseStringAttribute(XmlPullParser parser, String attribute) {
        return parser.getAttributeValue(null, attribute);
    }

    static boolean parseBooleanAttribute(XmlPullParser parser, String attribute) {
        return parseLongAttribute(parser, attribute) == 1 ? true : FEATURE_ENABLED;
    }

    static int parseIntAttribute(XmlPullParser parser, String attribute) {
        return (int) parseLongAttribute(parser, attribute);
    }

    static int parseIntAttribute(XmlPullParser parser, String attribute, int def) {
        return (int) parseLongAttribute(parser, attribute, (long) def);
    }

    static long parseLongAttribute(XmlPullParser parser, String attribute) {
        return parseLongAttribute(parser, attribute, 0);
    }

    static long parseLongAttribute(XmlPullParser parser, String attribute, long def) {
        String value = parseStringAttribute(parser, attribute);
        if (TextUtils.isEmpty(value)) {
            return def;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            Slog.e(TAG, "Error parsing long " + value);
            return def;
        }
    }

    static ComponentName parseComponentNameAttribute(XmlPullParser parser, String attribute) {
        String value = parseStringAttribute(parser, attribute);
        if (TextUtils.isEmpty(value)) {
            return null;
        }
        return ComponentName.unflattenFromString(value);
    }

    static Intent parseIntentAttribute(XmlPullParser parser, String attribute) {
        String value = parseStringAttribute(parser, attribute);
        if (TextUtils.isEmpty(value)) {
            return null;
        }
        try {
            return Intent.parseUri(value, 0);
        } catch (URISyntaxException e) {
            Slog.e(TAG, "Error parsing intent", e);
            return null;
        }
    }

    static void writeTagValue(XmlSerializer out, String tag, String value) throws IOException {
        if (!TextUtils.isEmpty(value)) {
            out.startTag(null, tag);
            out.attribute(null, ATTR_VALUE, value);
            out.endTag(null, tag);
        }
    }

    static void writeTagValue(XmlSerializer out, String tag, long value) throws IOException {
        writeTagValue(out, tag, Long.toString(value));
    }

    static void writeTagValue(XmlSerializer out, String tag, ComponentName name) throws IOException {
        if (name != null) {
            writeTagValue(out, tag, name.flattenToString());
        }
    }

    static void writeTagExtra(XmlSerializer out, String tag, PersistableBundle bundle) throws IOException, XmlPullParserException {
        if (bundle != null) {
            out.startTag(null, tag);
            bundle.saveToXml(out);
            out.endTag(null, tag);
        }
    }

    static void writeAttr(XmlSerializer out, String name, String value) throws IOException {
        if (!TextUtils.isEmpty(value)) {
            out.attribute(null, name, value);
        }
    }

    static void writeAttr(XmlSerializer out, String name, long value) throws IOException {
        writeAttr(out, name, String.valueOf(value));
    }

    static void writeAttr(XmlSerializer out, String name, boolean value) throws IOException {
        if (value) {
            writeAttr(out, name, "1");
        }
    }

    static void writeAttr(XmlSerializer out, String name, ComponentName comp) throws IOException {
        if (comp != null) {
            writeAttr(out, name, comp.flattenToString());
        }
    }

    static void writeAttr(XmlSerializer out, String name, Intent intent) throws IOException {
        if (intent != null) {
            writeAttr(out, name, intent.toUri(0));
        }
    }

    void saveBaseStateLocked() {
        AtomicFile file = getBaseStateFile();
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = file.startWrite();
            XmlSerializer out = new FastXmlSerializer();
            out.setOutput(fileOutputStream, StandardCharsets.UTF_8.name());
            out.startDocument(null, Boolean.valueOf(true));
            out.startTag(null, TAG_ROOT);
            writeTagValue(out, TAG_LAST_RESET_TIME, this.mRawLastResetTime);
            writeTagValue(out, TAG_LOCALE_CHANGE_SEQUENCE_NUMBER, this.mLocaleChangeSequenceNumber.get());
            out.endTag(null, TAG_ROOT);
            out.endDocument();
            file.finishWrite(fileOutputStream);
        } catch (IOException e) {
            Slog.e(TAG, "Failed to write to file " + file.getBaseFile(), e);
            file.failWrite(fileOutputStream);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void loadBaseStateLocked() {
        String tag;
        this.mRawLastResetTime = 0;
        AtomicFile file = getBaseStateFile();
        Throwable th = null;
        FileInputStream fileInputStream = null;
        fileInputStream = file.openRead();
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(fileInputStream, StandardCharsets.UTF_8.name());
        while (true) {
            int type = parser.next();
            if (type == 1) {
                break;
            } else if (type == 2) {
                int depth = parser.getDepth();
                tag = parser.getName();
                if (depth != 1) {
                    try {
                        if (tag.equals(TAG_LAST_RESET_TIME)) {
                            this.mRawLastResetTime = parseLongAttribute(parser, ATTR_VALUE);
                        } else if (tag.equals(TAG_LOCALE_CHANGE_SEQUENCE_NUMBER)) {
                            this.mLocaleChangeSequenceNumber.set(parseLongAttribute(parser, ATTR_VALUE));
                        } else {
                            Slog.e(TAG, "Invalid tag: " + tag);
                        }
                    } catch (Throwable th2) {
                        Throwable th3 = th2;
                        th2 = th;
                        th = th3;
                    }
                } else if (!TAG_ROOT.equals(tag)) {
                    break;
                }
            }
        }
        Slog.e(TAG, "Invalid root tag: " + tag);
        if (fileInputStream != null) {
            try {
                fileInputStream.close();
            } catch (Throwable th4) {
                th2 = th4;
            }
        }
        if (th2 != null) {
            try {
                throw th2;
            } catch (FileNotFoundException e) {
            } catch (Exception e2) {
                Slog.e(TAG, "Failed to read file " + file.getBaseFile(), e2);
                this.mRawLastResetTime = 0;
            }
        }
    }

    private void saveUserLocked(int userId) {
        File path = new File(injectUserDataPath(userId), FILENAME_USER_PACKAGES);
        path.mkdirs();
        AtomicFile file = new AtomicFile(path);
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = file.startWrite();
            saveUserInternalLocked(userId, fileOutputStream, FEATURE_ENABLED);
            file.finishWrite(fileOutputStream);
        } catch (Exception e) {
            Slog.e(TAG, "Failed to write to file " + file.getBaseFile(), e);
            file.failWrite(fileOutputStream);
        }
    }

    private void saveUserInternalLocked(int userId, OutputStream os, boolean forBackup) throws IOException, XmlPullParserException {
        BufferedOutputStream bos = new BufferedOutputStream(os);
        XmlSerializer out = new FastXmlSerializer();
        out.setOutput(bos, StandardCharsets.UTF_8.name());
        out.startDocument(null, Boolean.valueOf(true));
        getUserShortcutsLocked(userId).saveToXml(this, out, forBackup);
        out.endDocument();
        bos.flush();
        os.flush();
    }

    static IOException throwForInvalidTag(int depth, String tag) throws IOException {
        throw new IOException(String.format("Invalid tag '%s' found at depth %d", new Object[]{tag, Integer.valueOf(depth)}));
    }

    static void warnForInvalidTag(int depth, String tag) throws IOException {
        Slog.w(TAG, String.format("Invalid tag '%s' found at depth %d", new Object[]{tag, Integer.valueOf(depth)}));
    }

    private ShortcutUser loadUserLocked(int userId) {
        AtomicFile file = new AtomicFile(new File(injectUserDataPath(userId), FILENAME_USER_PACKAGES));
        try {
            FileInputStream in = file.openRead();
            ShortcutUser loadUserInternal;
            try {
                loadUserInternal = loadUserInternal(userId, in, FEATURE_ENABLED);
                return loadUserInternal;
            } catch (Exception e) {
                loadUserInternal = TAG;
                Slog.e(loadUserInternal, "Failed to read file " + file.getBaseFile(), e);
                return null;
            } finally {
                IoUtils.closeQuietly(in);
            }
        } catch (FileNotFoundException e2) {
            return null;
        }
    }

    private ShortcutUser loadUserInternal(int userId, InputStream is, boolean fromBackup) throws XmlPullParserException, IOException {
        BufferedInputStream bis = new BufferedInputStream(is);
        ShortcutUser ret = null;
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(bis, StandardCharsets.UTF_8.name());
        while (true) {
            int type = parser.next();
            if (type == 1) {
                return ret;
            }
            if (type == 2) {
                int depth = parser.getDepth();
                String tag = parser.getName();
                if (depth == 1 && "user".equals(tag)) {
                    ret = ShortcutUser.loadFromXml(this, parser, userId, fromBackup);
                } else {
                    throwForInvalidTag(depth, tag);
                }
            }
        }
    }

    private void scheduleSaveBaseState() {
        scheduleSaveInner(-10000);
    }

    void scheduleSaveUser(int userId) {
        scheduleSaveInner(userId);
    }

    /* synthetic */ void -com_android_server_pm_ShortcutService-mthref-0() {
        saveDirtyInfo();
    }

    private void scheduleSaveInner(int userId) {
        synchronized (this.mLock) {
            if (!this.mDirtyUserIds.contains(Integer.valueOf(userId))) {
                this.mDirtyUserIds.add(Integer.valueOf(userId));
            }
        }
        this.mHandler.removeCallbacks(this.mSaveDirtyInfoRunner);
        this.mHandler.postDelayed(this.mSaveDirtyInfoRunner, (long) this.mSaveDelayMillis);
    }

    void saveDirtyInfo() {
        synchronized (this.mLock) {
            for (int i = this.mDirtyUserIds.size() - 1; i >= 0; i--) {
                int userId = ((Integer) this.mDirtyUserIds.get(i)).intValue();
                if (userId == -10000) {
                    saveBaseStateLocked();
                } else {
                    saveUserLocked(userId);
                }
            }
            this.mDirtyUserIds.clear();
        }
    }

    long getLastResetTimeLocked() {
        updateTimesLocked();
        return this.mRawLastResetTime;
    }

    long getNextResetTimeLocked() {
        updateTimesLocked();
        return this.mRawLastResetTime + this.mResetInterval;
    }

    static boolean isClockValid(long time) {
        return time >= 1420070400 ? true : FEATURE_ENABLED;
    }

    private void updateTimesLocked() {
        long now = injectCurrentTimeMillis();
        long prevLastResetTime = this.mRawLastResetTime;
        if (this.mRawLastResetTime == 0) {
            this.mRawLastResetTime = now;
        } else if (now < this.mRawLastResetTime) {
            if (isClockValid(now)) {
                Slog.w(TAG, "Clock rewound");
                this.mRawLastResetTime = now;
            }
        } else if (this.mRawLastResetTime + this.mResetInterval <= now) {
            this.mRawLastResetTime = ((now / this.mResetInterval) * this.mResetInterval) + (this.mRawLastResetTime % this.mResetInterval);
        }
        if (prevLastResetTime != this.mRawLastResetTime) {
            scheduleSaveBaseState();
        }
    }

    @GuardedBy("mLock")
    private boolean isUserLoadedLocked(int userId) {
        return this.mUsers.get(userId) != null ? true : FEATURE_ENABLED;
    }

    @GuardedBy("mLock")
    ShortcutUser getUserShortcutsLocked(int userId) {
        ShortcutUser userPackages = (ShortcutUser) this.mUsers.get(userId);
        if (userPackages == null) {
            userPackages = loadUserLocked(userId);
            if (userPackages == null) {
                userPackages = new ShortcutUser(userId);
            }
            this.mUsers.put(userId, userPackages);
        }
        return userPackages;
    }

    void forEachLoadedUserLocked(Consumer<ShortcutUser> c) {
        for (int i = this.mUsers.size() - 1; i >= 0; i--) {
            c.accept((ShortcutUser) this.mUsers.valueAt(i));
        }
    }

    @GuardedBy("mLock")
    ShortcutPackage getPackageShortcutsLocked(String packageName, int userId) {
        return getUserShortcutsLocked(userId).getPackageShortcuts(this, packageName);
    }

    @GuardedBy("mLock")
    ShortcutLauncher getLauncherShortcutsLocked(String packageName, int ownerUserId, int launcherUserId) {
        return getUserShortcutsLocked(ownerUserId).getLauncherShortcuts(this, packageName, launcherUserId);
    }

    void removeIcon(int userId, ShortcutInfo shortcut) {
        if (shortcut.getBitmapPath() != null) {
            new File(shortcut.getBitmapPath()).delete();
            shortcut.setBitmapPath(null);
            shortcut.setIconResourceId(0);
            shortcut.clearFlags(12);
        }
    }

    public void cleanupBitmapsForPackage(int userId, String packageName) {
        File packagePath = new File(getUserBitmapFilePath(userId), packageName);
        if (packagePath.isDirectory()) {
            if (!(FileUtils.deleteContents(packagePath) ? packagePath.delete() : FEATURE_ENABLED)) {
                Slog.w(TAG, "Unable to remove directory " + packagePath);
            }
        }
    }

    FileOutputStreamWithPath openIconFileForWrite(int userId, ShortcutInfo shortcut) throws IOException {
        File packagePath = new File(getUserBitmapFilePath(userId), shortcut.getPackageName());
        if (!packagePath.isDirectory()) {
            packagePath.mkdirs();
            if (packagePath.isDirectory()) {
                SELinux.restorecon(packagePath);
            } else {
                throw new IOException("Unable to create directory " + packagePath);
            }
        }
        String baseName = String.valueOf(injectCurrentTimeMillis());
        int suffix = 0;
        while (true) {
            File file = new File(packagePath, (suffix == 0 ? baseName : baseName + "_" + suffix) + ".png");
            if (!file.exists()) {
                return new FileOutputStreamWithPath(file);
            }
            suffix++;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void saveIconAndFixUpShortcut(int userId, ShortcutInfo shortcut) {
        if (!shortcut.hasIconFile() && !shortcut.hasIconResource()) {
            long token = injectClearCallingIdentity();
            try {
                shortcut.setIconResourceId(0);
                shortcut.setBitmapPath(null);
                Icon icon = shortcut.getIcon();
                if (icon == null) {
                    injectRestoreCallingIdentity(token);
                    return;
                }
                switch (icon.getType()) {
                    case WindowState.LOW_RESOLUTION_COMPOSITION_OFF /*1*/:
                        Bitmap bitmap = icon.getBitmap();
                        if (bitmap == null) {
                            Slog.e(TAG, "Null bitmap detected");
                            shortcut.clearIcon();
                            injectRestoreCallingIdentity(token);
                            return;
                        }
                        File file = null;
                        try {
                            FileOutputStreamWithPath out = openIconFileForWrite(userId, shortcut);
                            Bitmap shrunk;
                            try {
                                file = out.getFile();
                                shrunk = shrinkBitmap(bitmap, this.mMaxIconDimension);
                                shrunk.compress(this.mIconPersistFormat, this.mIconPersistQuality, out);
                                if (bitmap != shrunk) {
                                    shrunk.recycle();
                                }
                                shortcut.setBitmapPath(out.getFile().getAbsolutePath());
                                shortcut.addFlags(8);
                                IoUtils.closeQuietly(out);
                            } catch (Throwable th) {
                                IoUtils.closeQuietly(out);
                            }
                        } catch (Exception e) {
                            Slog.wtf(TAG, "Unable to write bitmap to file", e);
                            file.delete();
                            break;
                        }
                        shortcut.clearIcon();
                        injectRestoreCallingIdentity(token);
                    case WindowState.LOW_RESOLUTION_COMPOSITION_ON /*2*/:
                        injectValidateIconResPackage(shortcut, icon);
                        shortcut.setIconResourceId(icon.getResId());
                        shortcut.addFlags(PROCESS_STATE_FOREGROUND_THRESHOLD);
                        shortcut.clearIcon();
                        injectRestoreCallingIdentity(token);
                    default:
                        throw ShortcutInfo.getInvalidIconException();
                }
            } catch (Throwable th2) {
                injectRestoreCallingIdentity(token);
            }
        }
    }

    void injectValidateIconResPackage(ShortcutInfo shortcut, Icon icon) {
        if (!shortcut.getPackageName().equals(icon.getResPackage())) {
            throw new IllegalArgumentException("Icon resource must reside in shortcut owner package");
        }
    }

    static Bitmap shrinkBitmap(Bitmap in, int maxSize) {
        int ow = in.getWidth();
        int oh = in.getHeight();
        if (ow <= maxSize && oh <= maxSize) {
            return in;
        }
        int longerDimension = Math.max(ow, oh);
        int nw = (ow * maxSize) / longerDimension;
        int nh = (oh * maxSize) / longerDimension;
        Bitmap scaledBitmap = Bitmap.createBitmap(nw, nh, Config.ARGB_8888);
        new Canvas(scaledBitmap).drawBitmap(in, null, new RectF(0.0f, 0.0f, (float) nw, (float) nh), null);
        return scaledBitmap;
    }

    private boolean isCallerSystem() {
        return UserHandle.isSameApp(injectBinderCallingUid(), ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE);
    }

    private boolean isCallerShell() {
        int callingUid = injectBinderCallingUid();
        if (callingUid == IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME || callingUid == 0) {
            return true;
        }
        return FEATURE_ENABLED;
    }

    private void enforceSystemOrShell() {
        Preconditions.checkState(!isCallerSystem() ? isCallerShell() : true, "Caller must be system or shell");
    }

    private void enforceShell() {
        Preconditions.checkState(isCallerShell(), "Caller must be shell");
    }

    private void enforceSystem() {
        Preconditions.checkState(isCallerSystem(), "Caller must be system");
    }

    private void enforceResetThrottlingPermission() {
        if (!isCallerSystem()) {
            injectEnforceCallingPermission("android.permission.RESET_SHORTCUT_MANAGER_THROTTLING", null);
        }
    }

    void injectEnforceCallingPermission(String permission, String message) {
        this.mContext.enforceCallingPermission(permission, message);
    }

    private void verifyCaller(String packageName, int userId) {
        Preconditions.checkStringNotEmpty(packageName, "packageName");
        if (!isCallerSystem()) {
            if (UserHandle.getUserId(injectBinderCallingUid()) != userId) {
                throw new SecurityException("Invalid user-ID");
            } else if (injectGetPackageUid(packageName, userId) != injectBinderCallingUid()) {
                throw new SecurityException("Calling package name mismatch");
            }
        }
    }

    void postToHandler(Runnable r) {
        this.mHandler.post(r);
    }

    void enforceMaxDynamicShortcuts(int numShortcuts) {
        if (numShortcuts > this.mMaxDynamicShortcuts) {
            throw new IllegalArgumentException("Max number of dynamic shortcuts exceeded");
        }
    }

    void packageShortcutsChanged(String packageName, int userId) {
        notifyListeners(packageName, userId);
        scheduleSaveUser(userId);
    }

    private void notifyListeners(String packageName, int userId) {
        if (this.mUserManager.isUserRunning(userId)) {
            postToHandler(new -void_notifyListeners_java_lang_String_packageName_int_userId_LambdaImpl0(this, packageName, userId));
        }
    }

    /* synthetic */ void -com_android_server_pm_ShortcutService_lambda$6(String packageName, int userId) {
        synchronized (this.mLock) {
            ArrayList<ShortcutChangeListener> copy = new ArrayList(this.mListeners);
        }
        for (int i = copy.size() - 1; i >= 0; i--) {
            ((ShortcutChangeListener) copy.get(i)).onShortcutChanged(packageName, userId);
        }
    }

    private void fixUpIncomingShortcutInfo(ShortcutInfo shortcut, boolean forUpdate) {
        Preconditions.checkNotNull(shortcut, "Null shortcut detected");
        if (shortcut.getActivityComponent() != null) {
            Preconditions.checkState(shortcut.getPackageName().equals(shortcut.getActivityComponent().getPackageName()), "Activity package name mismatch");
        }
        if (!forUpdate) {
            shortcut.enforceMandatoryFields();
        }
        if (shortcut.getIcon() != null) {
            ShortcutInfo.validateIcon(shortcut.getIcon());
        }
        validateForXml(shortcut.getId());
        validateForXml(shortcut.getTitle());
        validatePersistableBundleForXml(shortcut.getIntentPersistableExtras());
        validatePersistableBundleForXml(shortcut.getExtras());
        shortcut.replaceFlags(0);
    }

    private static void validatePersistableBundleForXml(PersistableBundle b) {
        if (b != null && b.size() != 0) {
            for (String key : b.keySet()) {
                validateForXml(key);
                Object value = b.get(key);
                if (value != null) {
                    if (value instanceof String) {
                        validateForXml((String) value);
                    } else if (value instanceof String[]) {
                        for (String v : (String[]) value) {
                            validateForXml(v);
                        }
                    } else if (value instanceof PersistableBundle) {
                        validatePersistableBundleForXml((PersistableBundle) value);
                    }
                }
            }
        }
    }

    private static void validateForXml(String s) {
        if (!TextUtils.isEmpty(s)) {
            int i = s.length() - 1;
            while (i >= 0) {
                if (isAllowedInXml(s.charAt(i))) {
                    i--;
                } else {
                    throw new IllegalArgumentException("Unsupported character detected in: " + s);
                }
            }
        }
    }

    private static boolean isAllowedInXml(char c) {
        if (c < ' ' || c > '\ud7ff') {
            return (c < '\ue000' || c > '\ufffd') ? FEATURE_ENABLED : true;
        } else {
            return true;
        }
    }

    public boolean setDynamicShortcuts(String packageName, ParceledListSlice shortcutInfoList, int userId) {
        verifyCaller(packageName, userId);
        List<ShortcutInfo> newShortcuts = shortcutInfoList.getList();
        int size = newShortcuts.size();
        synchronized (this.mLock) {
            ShortcutPackage ps = getPackageShortcutsLocked(packageName, userId);
            if (ps.tryApiCall(this)) {
                int i;
                enforceMaxDynamicShortcuts(size);
                for (i = 0; i < size; i++) {
                    fixUpIncomingShortcutInfo((ShortcutInfo) newShortcuts.get(i), FEATURE_ENABLED);
                }
                ps.deleteAllDynamicShortcuts(this);
                for (i = 0; i < size; i++) {
                    ps.addDynamicShortcut(this, (ShortcutInfo) newShortcuts.get(i));
                }
                packageShortcutsChanged(packageName, userId);
                return true;
            }
            return FEATURE_ENABLED;
        }
    }

    public boolean updateShortcuts(String packageName, ParceledListSlice shortcutInfoList, int userId) {
        verifyCaller(packageName, userId);
        List<ShortcutInfo> newShortcuts = shortcutInfoList.getList();
        int size = newShortcuts.size();
        synchronized (this.mLock) {
            ShortcutPackage ps = getPackageShortcutsLocked(packageName, userId);
            if (ps.tryApiCall(this)) {
                for (int i = 0; i < size; i++) {
                    ShortcutInfo source = (ShortcutInfo) newShortcuts.get(i);
                    fixUpIncomingShortcutInfo(source, true);
                    ShortcutInfo target = ps.findShortcutById(source.getId());
                    if (target != null) {
                        boolean replacingIcon = source.getIcon() != null ? true : FEATURE_ENABLED;
                        if (replacingIcon) {
                            removeIcon(userId, target);
                        }
                        target.copyNonNullFieldsFrom(source);
                        if (replacingIcon) {
                            saveIconAndFixUpShortcut(userId, target);
                        }
                    }
                }
                packageShortcutsChanged(packageName, userId);
                return true;
            }
            return FEATURE_ENABLED;
        }
    }

    public boolean addDynamicShortcuts(String packageName, ParceledListSlice shortcutInfoList, int userId) {
        verifyCaller(packageName, userId);
        List<ShortcutInfo> newShortcuts = shortcutInfoList.getList();
        int size = newShortcuts.size();
        synchronized (this.mLock) {
            ShortcutPackage ps = getPackageShortcutsLocked(packageName, userId);
            if (ps.tryApiCall(this)) {
                for (int i = 0; i < size; i++) {
                    ShortcutInfo newShortcut = (ShortcutInfo) newShortcuts.get(i);
                    fixUpIncomingShortcutInfo(newShortcut, FEATURE_ENABLED);
                    ps.addDynamicShortcut(this, newShortcut);
                }
                packageShortcutsChanged(packageName, userId);
                return true;
            }
            return FEATURE_ENABLED;
        }
    }

    public void removeDynamicShortcuts(String packageName, List shortcutIds, int userId) {
        verifyCaller(packageName, userId);
        Preconditions.checkNotNull(shortcutIds, "shortcutIds must be provided");
        synchronized (this.mLock) {
            for (int i = shortcutIds.size() - 1; i >= 0; i--) {
                getPackageShortcutsLocked(packageName, userId).deleteDynamicWithId(this, (String) Preconditions.checkStringNotEmpty((String) shortcutIds.get(i)));
            }
        }
        packageShortcutsChanged(packageName, userId);
    }

    public void removeAllDynamicShortcuts(String packageName, int userId) {
        verifyCaller(packageName, userId);
        synchronized (this.mLock) {
            getPackageShortcutsLocked(packageName, userId).deleteAllDynamicShortcuts(this);
        }
        packageShortcutsChanged(packageName, userId);
    }

    public ParceledListSlice<ShortcutInfo> getDynamicShortcuts(String packageName, int userId) {
        ParceledListSlice<ShortcutInfo> shortcutsWithQueryLocked;
        verifyCaller(packageName, userId);
        synchronized (this.mLock) {
            shortcutsWithQueryLocked = getShortcutsWithQueryLocked(packageName, userId, 1, new -android_content_pm_ParceledListSlice_getDynamicShortcuts_java_lang_String_packageName_int_userId_LambdaImpl0());
        }
        return shortcutsWithQueryLocked;
    }

    public ParceledListSlice<ShortcutInfo> getPinnedShortcuts(String packageName, int userId) {
        ParceledListSlice<ShortcutInfo> shortcutsWithQueryLocked;
        verifyCaller(packageName, userId);
        synchronized (this.mLock) {
            shortcutsWithQueryLocked = getShortcutsWithQueryLocked(packageName, userId, 1, new -android_content_pm_ParceledListSlice_getPinnedShortcuts_java_lang_String_packageName_int_userId_LambdaImpl0());
        }
        return shortcutsWithQueryLocked;
    }

    private ParceledListSlice<ShortcutInfo> getShortcutsWithQueryLocked(String packageName, int userId, int cloneFlags, Predicate<ShortcutInfo> query) {
        ArrayList<ShortcutInfo> ret = new ArrayList();
        getPackageShortcutsLocked(packageName, userId).findAll(this, ret, query, cloneFlags);
        return new ParceledListSlice(ret);
    }

    public int getMaxDynamicShortcutCount(String packageName, int userId) throws RemoteException {
        verifyCaller(packageName, userId);
        return this.mMaxDynamicShortcuts;
    }

    public int getRemainingCallCount(String packageName, int userId) {
        int apiCallCount;
        verifyCaller(packageName, userId);
        synchronized (this.mLock) {
            apiCallCount = this.mMaxUpdatesPerInterval - getPackageShortcutsLocked(packageName, userId).getApiCallCount(this);
        }
        return apiCallCount;
    }

    public long getRateLimitResetTime(String packageName, int userId) {
        long nextResetTimeLocked;
        verifyCaller(packageName, userId);
        synchronized (this.mLock) {
            nextResetTimeLocked = getNextResetTimeLocked();
        }
        return nextResetTimeLocked;
    }

    public int getIconMaxDimensions(String packageName, int userId) throws RemoteException {
        int i;
        verifyCaller(packageName, userId);
        synchronized (this.mLock) {
            i = this.mMaxIconDimension;
        }
        return i;
    }

    public void resetThrottling() {
        enforceSystemOrShell();
        resetThrottlingInner(getCallingUserId());
    }

    void resetThrottlingInner(int userId) {
        synchronized (this.mLock) {
            getUserShortcutsLocked(userId).resetThrottling();
        }
        scheduleSaveUser(userId);
        Slog.i(TAG, "ShortcutManager: throttling counter reset for user " + userId);
    }

    void resetAllThrottlingInner() {
        synchronized (this.mLock) {
            this.mRawLastResetTime = injectCurrentTimeMillis();
        }
        scheduleSaveBaseState();
        Slog.i(TAG, "ShortcutManager: throttling counter reset for all users");
    }

    void resetPackageThrottling(String packageName, int userId) {
        synchronized (this.mLock) {
            getPackageShortcutsLocked(packageName, userId).resetRateLimitingForCommandLineNoSaving();
            saveUserLocked(userId);
        }
    }

    public void onApplicationActive(String packageName, int userId) {
        enforceResetThrottlingPermission();
        resetPackageThrottling(packageName, userId);
    }

    boolean hasShortcutHostPermission(String callingPackage, int userId) {
        return hasShortcutHostPermissionInner(callingPackage, userId);
    }

    boolean hasShortcutHostPermissionInner(String callingPackage, int userId) {
        synchronized (this.mLock) {
            ComponentName detected;
            long start = System.currentTimeMillis();
            ShortcutUser user = getUserShortcutsLocked(userId);
            List<ResolveInfo> allHomeCandidates = new ArrayList();
            long startGetHomeActivitiesAsUser = System.currentTimeMillis();
            ComponentName defaultLauncher = injectPackageManagerInternal().getHomeActivitiesAsUser(allHomeCandidates, userId);
            logDurationStat(0, startGetHomeActivitiesAsUser);
            if (defaultLauncher != null) {
                detected = defaultLauncher;
            } else {
                detected = user.getLauncherComponent();
            }
            if (detected == null) {
                int size = allHomeCandidates.size();
                int lastPriority = UsbAudioDevice.kAudioDeviceMeta_Alsa;
                for (int i = 0; i < size; i++) {
                    ResolveInfo ri = (ResolveInfo) allHomeCandidates.get(i);
                    if (ri.activityInfo.applicationInfo.isSystemApp() && ri.priority >= lastPriority) {
                        detected = ri.activityInfo.getComponentName();
                        lastPriority = ri.priority;
                    }
                }
            }
            logDurationStat(PROCESS_STATE_FOREGROUND_THRESHOLD, start);
            if (detected != null) {
                user.setLauncherComponent(this, detected);
                boolean equals = detected.getPackageName().equals(callingPackage);
                return equals;
            }
            return FEATURE_ENABLED;
        }
    }

    private void cleanUpPackageForAllLoadedUsers(String packageName, int packageUserId) {
        synchronized (this.mLock) {
            forEachLoadedUserLocked(new -void_cleanUpPackageForAllLoadedUsers_java_lang_String_packageName_int_packageUserId_LambdaImpl0(this, packageName, packageUserId));
        }
    }

    /* synthetic */ void -com_android_server_pm_ShortcutService_lambda$9(String packageName, int packageUserId, ShortcutUser user) {
        cleanUpPackageLocked(packageName, user.getUserId(), packageUserId);
    }

    void cleanUpPackageLocked(String packageName, int owningUserId, int packageUserId) {
        boolean wasUserLoaded = isUserLoadedLocked(owningUserId);
        ShortcutUser user = getUserShortcutsLocked(owningUserId);
        boolean doNotify = FEATURE_ENABLED;
        if (packageUserId == owningUserId && user.removePackage(this, packageName) != null) {
            doNotify = true;
        }
        user.removeLauncher(packageUserId, packageName);
        user.forAllLaunchers(new -void_cleanUpPackageLocked_java_lang_String_packageName_int_owningUserId_int_packageUserId_LambdaImpl0(packageName, packageUserId));
        user.forAllPackages(new -void_cleanUpPackageLocked_java_lang_String_packageName_int_owningUserId_int_packageUserId_LambdaImpl1());
        scheduleSaveUser(owningUserId);
        if (doNotify) {
            notifyListeners(packageName, owningUserId);
        }
        if (!wasUserLoaded) {
            unloadUserLocked(owningUserId);
        }
    }

    /* synthetic */ void -com_android_server_pm_ShortcutService_lambda$11(ShortcutPackage p) {
        p.refreshPinnedFlags(this);
    }

    void checkPackageChanges(int ownerUserId) {
        ArrayList<PackageWithUser> gonePackages = new ArrayList();
        synchronized (this.mLock) {
            getUserShortcutsLocked(ownerUserId).forAllPackageItems(new -void_checkPackageChanges_int_ownerUserId_LambdaImpl0(this, ownerUserId, gonePackages));
            if (gonePackages.size() > 0) {
                for (int i = gonePackages.size() - 1; i >= 0; i--) {
                    PackageWithUser pu = (PackageWithUser) gonePackages.get(i);
                    cleanUpPackageLocked(pu.packageName, ownerUserId, pu.userId);
                }
            }
        }
    }

    /* synthetic */ void -com_android_server_pm_ShortcutService_lambda$12(int ownerUserId, ArrayList gonePackages, ShortcutPackageItem spi) {
        if (!spi.getPackageInfo().isShadow()) {
            int versionCode = getApplicationVersionCode(spi.getPackageName(), spi.getPackageUserId());
            if (versionCode >= 0) {
                getUserShortcutsLocked(ownerUserId).handlePackageUpdated(this, spi.getPackageName(), versionCode);
            } else {
                gonePackages.add(PackageWithUser.of(spi));
            }
        }
    }

    private void handlePackageAdded(String packageName, int userId) {
        synchronized (this.mLock) {
            forEachLoadedUserLocked(new -void_handlePackageAdded_java_lang_String_packageName_int_userId_LambdaImpl0(this, packageName, userId));
        }
    }

    /* synthetic */ void -com_android_server_pm_ShortcutService_lambda$13(String packageName, int userId, ShortcutUser user) {
        user.attemptToRestoreIfNeededAndSave(this, packageName, userId);
    }

    private void handlePackageUpdateFinished(String packageName, int userId) {
        synchronized (this.mLock) {
            forEachLoadedUserLocked(new -void_handlePackageUpdateFinished_java_lang_String_packageName_int_userId_LambdaImpl0(this, packageName, userId));
            int versionCode = getApplicationVersionCode(packageName, userId);
            if (versionCode < 0) {
                return;
            }
            getUserShortcutsLocked(userId).handlePackageUpdated(this, packageName, versionCode);
        }
    }

    /* synthetic */ void -com_android_server_pm_ShortcutService_lambda$14(String packageName, int userId, ShortcutUser user) {
        user.attemptToRestoreIfNeededAndSave(this, packageName, userId);
    }

    private void handlePackageRemoved(String packageName, int packageUserId) {
        cleanUpPackageForAllLoadedUsers(packageName, packageUserId);
    }

    private void handlePackageDataCleared(String packageName, int packageUserId) {
        cleanUpPackageForAllLoadedUsers(packageName, packageUserId);
    }

    PackageInfo getPackageInfoWithSignatures(String packageName, int userId) {
        return injectPackageInfo(packageName, userId, true);
    }

    private boolean isApplicationFlagSet(String packageName, int userId, int flags) {
        ApplicationInfo ai = injectApplicationInfo(packageName, userId);
        if (ai == null || (ai.flags & flags) != flags) {
            return FEATURE_ENABLED;
        }
        return true;
    }

    boolean isPackageInstalled(String packageName, int userId) {
        return isApplicationFlagSet(packageName, userId, 8388608);
    }

    int getApplicationVersionCode(String packageName, int userId) {
        ApplicationInfo ai = injectApplicationInfo(packageName, userId);
        if (ai == null || (ai.flags & 8388608) == 0) {
            return -1;
        }
        return ai.versionCode;
    }

    boolean shouldBackupApp(String packageName, int userId) {
        return isApplicationFlagSet(packageName, userId, DumpState.DUMP_VERSION);
    }

    boolean shouldBackupApp(PackageInfo pi) {
        return (pi.applicationInfo.flags & DumpState.DUMP_VERSION) != 0 ? true : FEATURE_ENABLED;
    }

    public byte[] getBackupPayload(int userId) {
        enforceSystem();
        synchronized (this.mLock) {
            ShortcutUser user = getUserShortcutsLocked(userId);
            if (user == null) {
                Slog.w(TAG, "Can't backup: user not found: id=" + userId);
                return null;
            }
            user.forAllPackageItems(new -byte__getBackupPayload_int_userId_LambdaImpl0());
            ByteArrayOutputStream os = new ByteArrayOutputStream(DumpState.DUMP_VERSION);
            try {
                saveUserInternalLocked(userId, os, true);
                byte[] toByteArray = os.toByteArray();
                return toByteArray;
            } catch (Exception e) {
                Slog.w(TAG, "Backup failed.", e);
                return null;
            }
        }
    }

    /* synthetic */ void -com_android_server_pm_ShortcutService_lambda$15(ShortcutPackageItem spi) {
        spi.refreshPackageInfoAndSave(this);
    }

    public void applyRestore(byte[] payload, int userId) {
        enforceSystem();
        try {
            ShortcutUser user = loadUserInternal(userId, new ByteArrayInputStream(payload), true);
            synchronized (this.mLock) {
                this.mUsers.put(userId, user);
                File bitmapPath = getUserBitmapFilePath(userId);
                if (!FileUtils.deleteContents(bitmapPath)) {
                    Slog.w(TAG, "Failed to delete " + bitmapPath);
                }
                saveUserLocked(userId);
            }
        } catch (Exception e) {
            Slog.w(TAG, "Restoration failed.", e);
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
            pw.println("Permission Denial: can't dump UserManager from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " without permission " + "android.permission.DUMP");
        } else {
            dumpInner(pw, args);
        }
    }

    void dumpInner(PrintWriter pw, String[] args) {
        synchronized (this.mLock) {
            int i;
            long now = injectCurrentTimeMillis();
            pw.print("Now: [");
            pw.print(now);
            pw.print("] ");
            pw.print(formatTime(now));
            pw.print("  Raw last reset: [");
            pw.print(this.mRawLastResetTime);
            pw.print("] ");
            PrintWriter printWriter = pw;
            printWriter.print(formatTime(this.mRawLastResetTime));
            long last = getLastResetTimeLocked();
            pw.print("  Last reset: [");
            pw.print(last);
            pw.print("] ");
            pw.print(formatTime(last));
            long next = getNextResetTimeLocked();
            pw.print("  Next reset: [");
            pw.print(next);
            pw.print("] ");
            pw.print(formatTime(next));
            pw.print("  Locale change seq#: ");
            pw.print(this.mLocaleChangeSequenceNumber.get());
            pw.println();
            pw.print("  Config:");
            pw.print("    Max icon dim: ");
            pw.println(this.mMaxIconDimension);
            pw.print("    Icon format: ");
            pw.println(this.mIconPersistFormat);
            pw.print("    Icon quality: ");
            pw.println(this.mIconPersistQuality);
            pw.print("    saveDelayMillis: ");
            pw.println(this.mSaveDelayMillis);
            pw.print("    resetInterval: ");
            pw.println(this.mResetInterval);
            pw.print("    maxUpdatesPerInterval: ");
            pw.println(this.mMaxUpdatesPerInterval);
            pw.print("    maxDynamicShortcuts: ");
            pw.println(this.mMaxDynamicShortcuts);
            pw.println();
            pw.println("  Stats:");
            synchronized (this.mStatLock) {
                String p = "    ";
                dumpStatLS(pw, "    ", 0, "getHomeActivities()");
                dumpStatLS(pw, "    ", PROCESS_STATE_FOREGROUND_THRESHOLD, "Launcher permission check");
                dumpStatLS(pw, "    ", 1, "getPackageInfo()");
                dumpStatLS(pw, "    ", 2, "getPackageInfo(SIG)");
                dumpStatLS(pw, "    ", 3, "getApplicationInfo");
            }
            for (i = 0; i < this.mUsers.size(); i++) {
                pw.println();
                ((ShortcutUser) this.mUsers.valueAt(i)).dump(this, pw, "  ");
            }
            pw.println();
            pw.println("  UID state:");
            for (i = 0; i < this.mUidState.size(); i++) {
                int uid = this.mUidState.keyAt(i);
                int state = this.mUidState.valueAt(i);
                pw.print("    UID=");
                pw.print(uid);
                pw.print(" state=");
                pw.print(state);
                if (isProcessStateForeground(state)) {
                    pw.print("  [FG]");
                }
                pw.print("  last FG=");
                pw.print(this.mUidLastForegroundElapsedTime.get(uid));
                pw.println();
            }
        }
    }

    static String formatTime(long time) {
        Time tobj = new Time();
        tobj.set(time);
        return tobj.format("%Y-%m-%d %H:%M:%S");
    }

    private void dumpStatLS(PrintWriter pw, String prefix, int statId, String label) {
        pw.print(prefix);
        int count = this.mCountStats[statId];
        long dur = this.mDurationStats[statId];
        String str = "%s: count=%d, total=%dms, avg=%.1fms";
        Object[] objArr = new Object[PROCESS_STATE_FOREGROUND_THRESHOLD];
        objArr[0] = label;
        objArr[1] = Integer.valueOf(count);
        objArr[2] = Long.valueOf(dur);
        objArr[3] = Double.valueOf(count == 0 ? 0.0d : ((double) dur) / ((double) count));
        pw.println(String.format(str, objArr));
    }

    public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ResultReceiver resultReceiver) throws RemoteException {
        enforceShell();
        new MyShellCommand().exec(this, in, out, err, args, resultReceiver);
    }

    long injectCurrentTimeMillis() {
        return System.currentTimeMillis();
    }

    long injectElapsedRealtime() {
        return SystemClock.elapsedRealtime();
    }

    int injectBinderCallingUid() {
        return getCallingUid();
    }

    private int getCallingUserId() {
        return UserHandle.getUserId(injectBinderCallingUid());
    }

    long injectClearCallingIdentity() {
        return Binder.clearCallingIdentity();
    }

    void injectRestoreCallingIdentity(long token) {
        Binder.restoreCallingIdentity(token);
    }

    final void wtf(String message) {
        wtf(message, null);
    }

    void wtf(String message, Exception e) {
        Slog.wtf(TAG, message, e);
    }

    File injectSystemDataPath() {
        return Environment.getDataSystemDirectory();
    }

    File injectUserDataPath(int userId) {
        return new File(Environment.getDataSystemCeDirectory(userId), DIRECTORY_PER_USER);
    }

    boolean injectIsLowRamDevice() {
        return ActivityManager.isLowRamDeviceStatic();
    }

    void injectRegisterUidObserver(IUidObserver observer, int which) {
        try {
            ActivityManagerNative.getDefault().registerUidObserver(observer, which);
        } catch (RemoteException e) {
        }
    }

    PackageManagerInternal injectPackageManagerInternal() {
        return this.mPackageManagerInternal;
    }

    File getUserBitmapFilePath(int userId) {
        return new File(injectUserDataPath(userId), DIRECTORY_BITMAPS);
    }

    SparseArray<ShortcutUser> getShortcutsForTest() {
        return this.mUsers;
    }

    int getMaxDynamicShortcutsForTest() {
        return this.mMaxDynamicShortcuts;
    }

    int getMaxUpdatesPerIntervalForTest() {
        return this.mMaxUpdatesPerInterval;
    }

    long getResetIntervalForTest() {
        return this.mResetInterval;
    }

    int getMaxIconDimensionForTest() {
        return this.mMaxIconDimension;
    }

    CompressFormat getIconPersistFormatForTest() {
        return this.mIconPersistFormat;
    }

    int getIconPersistQualityForTest() {
        return this.mIconPersistQuality;
    }

    ShortcutInfo getPackageShortcutForTest(String packageName, String shortcutId, int userId) {
        synchronized (this.mLock) {
            ShortcutUser user = (ShortcutUser) this.mUsers.get(userId);
            if (user == null) {
                return null;
            }
            ShortcutPackage pkg = (ShortcutPackage) user.getAllPackagesForTest().get(packageName);
            if (pkg == null) {
                return null;
            }
            ShortcutInfo findShortcutById = pkg.findShortcutById(shortcutId);
            return findShortcutById;
        }
    }
}
