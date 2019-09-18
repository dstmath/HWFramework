package android.app;

import android.app.ActivityThread;
import android.app.LoadedApk;
import android.aps.IApsManager;
import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.IContentProvider;
import android.content.IIntentReceiver;
import android.content.IIntentSender;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.CompatResources;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.hardware.radio.V1_0.RadioError;
import android.hsm.HwSystemManager;
import android.hwtheme.HwThemeManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.os.FileUtils;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.StorageManager;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.util.AndroidRuntimeException;
import android.util.ArrayMap;
import android.util.DisplayMetrics;
import android.util.Flog;
import android.util.HwPCUtils;
import android.util.Jlog;
import android.util.Log;
import android.util.Slog;
import android.view.Display;
import android.view.DisplayAdjustments;
import android.view.autofill.AutofillManager;
import android.vrsystem.IVRSystemServiceManager;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.Preconditions;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Executor;
import libcore.io.Memory;

public class ContextImpl extends Context {
    private static final boolean DEBUG = false;
    private static final boolean IS_SWITCH_SD_ENABLED = "true".equals(SystemProperties.get("ro.config.switchPrimaryVolume", "false"));
    static final int STATE_INITIALIZING = 1;
    static final int STATE_NOT_FOUND = 3;
    static final int STATE_READY = 2;
    static final int STATE_UNINITIALIZED = 0;
    private static final String TAG = "ContextImpl";
    private static final String UNMOUNT_PATH = "/dev/null";
    private static final String XATTR_INODE_CACHE = "user.inode_cache";
    private static final String XATTR_INODE_CODE_CACHE = "user.inode_code_cache";
    @GuardedBy("ContextImpl.class")
    private static ArrayMap<String, ArrayMap<File, SharedPreferencesImpl>> sSharedPrefsCache;
    private final IBinder mActivityToken;
    private AutofillManager.AutofillClient mAutofillClient = null;
    private final String mBasePackageName;
    @GuardedBy("mSync")
    private File mCacheDir;
    private ClassLoader mClassLoader;
    @GuardedBy("mSync")
    private File mCodeCacheDir;
    private final ApplicationContentResolver mContentResolver;
    @GuardedBy("mSync")
    private File mDatabasesDir;
    private Display mDisplay;
    @GuardedBy("mSync")
    private File mFilesDir;
    private final int mFlags;
    private boolean mIsAutofillCompatEnabled;
    final ActivityThread mMainThread;
    @GuardedBy("mSync")
    private File mNoBackupFilesDir;
    private final String mOpPackageName;
    private Context mOuterContext = this;
    final LoadedApk mPackageInfo;
    private PackageManager mPackageManager;
    @GuardedBy("mSync")
    private File mPreferencesDir;
    private Context mReceiverRestrictedContext = null;
    private Resources mResources;
    private final ResourcesManager mResourcesManager;
    final Object[] mServiceCache = SystemServiceRegistry.createServiceCache();
    final int[] mServiceInitializationStateArray = new int[this.mServiceCache.length];
    @GuardedBy("ContextImpl.class")
    private ArrayMap<String, File> mSharedPrefsPaths;
    private String mSplitName = null;
    private final Object mSync = new Object();
    private Resources.Theme mTheme = null;
    private int mThemeResource = 0;
    private final UserHandle mUser;
    private IVRSystemServiceManager mVrMananger = HwFrameworkFactory.getVRSystemServiceManager();

    private static final class ApplicationContentResolver extends ContentResolver {
        private final ActivityThread mMainThread;

        public ApplicationContentResolver(Context context, ActivityThread mainThread) {
            super(context);
            this.mMainThread = (ActivityThread) Preconditions.checkNotNull(mainThread);
        }

        /* access modifiers changed from: protected */
        public IContentProvider acquireProvider(Context context, String auth) {
            return this.mMainThread.acquireProvider(context, ContentProvider.getAuthorityWithoutUserId(auth), resolveUserIdFromAuthority(auth), true);
        }

        /* access modifiers changed from: protected */
        public IContentProvider acquireExistingProvider(Context context, String auth) {
            return this.mMainThread.acquireExistingProvider(context, ContentProvider.getAuthorityWithoutUserId(auth), resolveUserIdFromAuthority(auth), true);
        }

        public boolean releaseProvider(IContentProvider provider) {
            return this.mMainThread.releaseProvider(provider, true);
        }

        /* access modifiers changed from: protected */
        public IContentProvider acquireUnstableProvider(Context c, String auth) {
            return this.mMainThread.acquireProvider(c, ContentProvider.getAuthorityWithoutUserId(auth), resolveUserIdFromAuthority(auth), false);
        }

        public boolean releaseUnstableProvider(IContentProvider icp) {
            return this.mMainThread.releaseProvider(icp, false);
        }

        public void unstableProviderDied(IContentProvider icp) {
            this.mMainThread.handleUnstableProviderDied(icp.asBinder(), true);
        }

        public void appNotRespondingViaProvider(IContentProvider icp) {
            this.mMainThread.appNotRespondingViaProvider(icp.asBinder());
        }

        /* access modifiers changed from: protected */
        public int resolveUserIdFromAuthority(String auth) {
            return ContentProvider.getUserIdFromAuthority(auth, getUserId());
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    @interface ServiceInitializationState {
    }

    static ContextImpl getImpl(Context context) {
        while (context instanceof ContextWrapper) {
            Context baseContext = ((ContextWrapper) context).getBaseContext();
            Context nextContext = baseContext;
            if (baseContext == null) {
                break;
            }
            context = nextContext;
        }
        return (ContextImpl) context;
    }

    public AssetManager getAssets() {
        return getResources().getAssets();
    }

    public Resources getResources() {
        return this.mResources;
    }

    public PackageManager getPackageManager() {
        if (this.mPackageManager != null) {
            return this.mPackageManager;
        }
        IPackageManager pm = ActivityThread.getPackageManager();
        if (pm == null) {
            return null;
        }
        ApplicationPackageManager applicationPackageManager = new ApplicationPackageManager(this, pm);
        this.mPackageManager = applicationPackageManager;
        return applicationPackageManager;
    }

    public ContentResolver getContentResolver() {
        return this.mContentResolver;
    }

    public Looper getMainLooper() {
        return this.mMainThread.getLooper();
    }

    public Executor getMainExecutor() {
        return this.mMainThread.getExecutor();
    }

    public Context getApplicationContext() {
        return this.mPackageInfo != null ? this.mPackageInfo.getApplication() : this.mMainThread.getApplication();
    }

    public void setTheme(int resId) {
        synchronized (this.mSync) {
            if (this.mThemeResource != resId) {
                this.mThemeResource = resId;
                initializeTheme();
            }
        }
    }

    public int getThemeResId() {
        int i;
        synchronized (this.mSync) {
            i = this.mThemeResource;
        }
        return i;
    }

    public Resources.Theme getTheme() {
        synchronized (this.mSync) {
            if (this.mTheme != null) {
                Resources.Theme theme = this.mTheme;
                return theme;
            }
            this.mThemeResource = Resources.selectDefaultTheme(this.mThemeResource, getOuterContext().getApplicationInfo().targetSdkVersion);
            initializeTheme();
            Resources.Theme theme2 = this.mTheme;
            return theme2;
        }
    }

    private void initializeTheme() {
        if (this.mTheme == null) {
            this.mTheme = this.mResources.newTheme();
        }
        this.mTheme.applyStyle(this.mThemeResource, true);
    }

    public ClassLoader getClassLoader() {
        if (this.mClassLoader != null) {
            return this.mClassLoader;
        }
        return this.mPackageInfo != null ? this.mPackageInfo.getClassLoader() : ClassLoader.getSystemClassLoader();
    }

    public String getPackageName() {
        if (this.mPackageInfo != null) {
            return this.mPackageInfo.getPackageName();
        }
        return "android";
    }

    public String getBasePackageName() {
        return this.mBasePackageName != null ? this.mBasePackageName : getPackageName();
    }

    public String getOpPackageName() {
        return this.mOpPackageName != null ? this.mOpPackageName : getBasePackageName();
    }

    public ApplicationInfo getApplicationInfo() {
        if (this.mPackageInfo != null) {
            return this.mPackageInfo.getApplicationInfo();
        }
        throw new RuntimeException("Not supported in system context");
    }

    public String getPackageResourcePath() {
        if (this.mPackageInfo != null) {
            return this.mPackageInfo.getResDir();
        }
        throw new RuntimeException("Not supported in system context");
    }

    public String getPackageCodePath() {
        if (this.mPackageInfo != null) {
            return this.mPackageInfo.getAppDir();
        }
        throw new RuntimeException("Not supported in system context");
    }

    public SharedPreferences getSharedPreferences(String name, int mode) {
        File file;
        if (this.mPackageInfo.getApplicationInfo().targetSdkVersion < 19 && name == null) {
            name = "null";
        }
        synchronized (ContextImpl.class) {
            if (this.mSharedPrefsPaths == null) {
                this.mSharedPrefsPaths = new ArrayMap<>();
            }
            file = this.mSharedPrefsPaths.get(name);
            if (file == null) {
                file = getSharedPreferencesPath(name);
                this.mSharedPrefsPaths.put(name, file);
            }
        }
        return getSharedPreferences(file, mode);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:19:0x004b, code lost:
        if ((r7 & 4) != 0) goto L_0x0057;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0055, code lost:
        if (getApplicationInfo().targetSdkVersion >= 11) goto L_0x005a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0057, code lost:
        r2.startReloadIfChangedUnexpectedly();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x005a, code lost:
        return r2;
     */
    public SharedPreferences getSharedPreferences(File file, int mode) {
        synchronized (ContextImpl.class) {
            ArrayMap<File, SharedPreferencesImpl> cache = getSharedPreferencesCacheLocked();
            SharedPreferencesImpl sp = cache.get(file);
            if (sp == null) {
                checkMode(mode);
                if (getApplicationInfo().targetSdkVersion >= 26 && isCredentialProtectedStorage()) {
                    if (!((UserManager) getSystemService(UserManager.class)).isUserUnlockingOrUnlocked(UserHandle.myUserId())) {
                        throw new IllegalStateException("SharedPreferences in credential encrypted storage are not available until after user is unlocked");
                    }
                }
                SharedPreferencesImpl sp2 = new SharedPreferencesImpl(file, mode);
                cache.put(file, sp2);
                return sp2;
            }
        }
    }

    @GuardedBy("ContextImpl.class")
    private ArrayMap<File, SharedPreferencesImpl> getSharedPreferencesCacheLocked() {
        if (sSharedPrefsCache == null) {
            sSharedPrefsCache = new ArrayMap<>();
        }
        String packageName = getPackageName();
        ArrayMap<File, SharedPreferencesImpl> packagePrefs = sSharedPrefsCache.get(packageName);
        if (packagePrefs != null) {
            return packagePrefs;
        }
        ArrayMap<File, SharedPreferencesImpl> packagePrefs2 = new ArrayMap<>();
        sSharedPrefsCache.put(packageName, packagePrefs2);
        return packagePrefs2;
    }

    public void reloadSharedPreferences() {
        int i;
        ArrayList<SharedPreferencesImpl> spImpls = new ArrayList<>();
        synchronized (ContextImpl.class) {
            ArrayMap<File, SharedPreferencesImpl> cache = getSharedPreferencesCacheLocked();
            i = 0;
            for (int i2 = 0; i2 < cache.size(); i2++) {
                SharedPreferencesImpl sp = cache.valueAt(i2);
                if (sp != null) {
                    spImpls.add(sp);
                }
            }
        }
        while (true) {
            int i3 = i;
            if (i3 < spImpls.size()) {
                spImpls.get(i3).startReloadIfChangedUnexpectedly();
                i = i3 + 1;
            } else {
                return;
            }
        }
    }

    private static int moveFiles(File sourceDir, File targetDir, final String prefix) {
        File[] sourceFiles = FileUtils.listFilesOrEmpty(sourceDir, new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.startsWith(prefix);
            }
        });
        int res = 0;
        int length = sourceFiles.length;
        int i = 0;
        while (i < length) {
            File sourceFile = sourceFiles[i];
            File targetFile = new File(targetDir, sourceFile.getName());
            Log.d(TAG, "Migrating " + sourceFile + " to " + targetFile);
            try {
                FileUtils.copyFileOrThrow(sourceFile, targetFile);
                FileUtils.copyPermissions(sourceFile, targetFile);
                if (sourceFile.delete()) {
                    if (res != -1) {
                        res++;
                    }
                    i++;
                } else {
                    throw new IOException("Failed to clean up " + sourceFile);
                }
            } catch (IOException e) {
                Log.w(TAG, "Failed to migrate " + sourceFile + ": " + e);
                res = -1;
            }
        }
        return res;
    }

    public boolean moveSharedPreferencesFrom(Context sourceContext, String name) {
        boolean z;
        synchronized (ContextImpl.class) {
            File source = sourceContext.getSharedPreferencesPath(name);
            File target = getSharedPreferencesPath(name);
            int res = moveFiles(source.getParentFile(), target.getParentFile(), source.getName());
            if (res > 0) {
                ArrayMap<File, SharedPreferencesImpl> cache = getSharedPreferencesCacheLocked();
                cache.remove(source);
                cache.remove(target);
            }
            z = res != -1;
        }
        return z;
    }

    public boolean deleteSharedPreferences(String name) {
        boolean z;
        synchronized (ContextImpl.class) {
            File prefs = getSharedPreferencesPath(name);
            File prefsBackup = SharedPreferencesImpl.makeBackupFile(prefs);
            getSharedPreferencesCacheLocked().remove(prefs);
            prefs.delete();
            prefsBackup.delete();
            z = !prefs.exists() && !prefsBackup.exists();
        }
        return z;
    }

    private File getPreferencesDir() {
        File ensurePrivateDirExists;
        synchronized (this.mSync) {
            if (this.mPreferencesDir == null) {
                this.mPreferencesDir = new File(getDataDir(), "shared_prefs");
            }
            ensurePrivateDirExists = ensurePrivateDirExists(this.mPreferencesDir);
        }
        return ensurePrivateDirExists;
    }

    public FileInputStream openFileInput(String name) throws FileNotFoundException {
        return new FileInputStream(makeFilename(getFilesDir(), name));
    }

    public FileOutputStream openFileOutput(String name, int mode) throws FileNotFoundException {
        checkMode(mode);
        boolean append = (32768 & mode) != 0;
        File f = makeFilename(getFilesDir(), name);
        try {
            FileOutputStream fos = new FileOutputStream(f, append);
            setFilePermissionsFromMode(f.getPath(), mode, 0);
            return fos;
        } catch (FileNotFoundException e) {
            File parent = f.getParentFile();
            parent.mkdir();
            FileUtils.setPermissions(parent.getPath(), RadioError.OEM_ERROR_5, -1, -1);
            FileOutputStream fos2 = new FileOutputStream(f, append);
            setFilePermissionsFromMode(f.getPath(), mode, 0);
            return fos2;
        }
    }

    public boolean deleteFile(String name) {
        return makeFilename(getFilesDir(), name).delete();
    }

    private static File ensurePrivateDirExists(File file) {
        return ensurePrivateDirExists(file, RadioError.OEM_ERROR_5, -1, null);
    }

    private static File ensurePrivateCacheDirExists(File file, String xattr) {
        return ensurePrivateDirExists(file, 1529, UserHandle.getCacheAppGid(Process.myUid()), xattr);
    }

    private static File ensurePrivateDirExists(File file, int mode, int gid, String xattr) {
        if (!file.exists()) {
            String path = file.getAbsolutePath();
            try {
                Os.mkdir(path, mode);
                Os.chmod(path, mode);
                if (gid != -1) {
                    Os.chown(path, -1, gid);
                }
            } catch (ErrnoException e) {
                if (e.errno != OsConstants.EEXIST) {
                    Log.w(TAG, "Failed to ensure " + file + ": " + e.getMessage());
                }
            }
            if (xattr != null) {
                try {
                    byte[] value = new byte[8];
                    Memory.pokeLong(value, 0, Os.stat(file.getAbsolutePath()).st_ino, ByteOrder.nativeOrder());
                    Os.setxattr(file.getParentFile().getAbsolutePath(), xattr, value, 0);
                } catch (ErrnoException e2) {
                    Log.w(TAG, "Failed to update " + xattr + ": " + e2.getMessage());
                }
            }
        }
        return file;
    }

    public File getFilesDir() {
        synchronized (this.mSync) {
            if (isHbsMiniApp()) {
                if (this.mFilesDir == null) {
                    this.mFilesDir = getHbsMiniAppDir("files");
                }
                File ensurePrivateDirExists = ensurePrivateDirExists(this.mFilesDir);
                return ensurePrivateDirExists;
            }
            if (this.mFilesDir == null) {
                this.mFilesDir = new File(getDataDir(), "files");
            }
            File ensurePrivateDirExists2 = ensurePrivateDirExists(this.mFilesDir);
            return ensurePrivateDirExists2;
        }
    }

    public File getNoBackupFilesDir() {
        File ensurePrivateDirExists;
        synchronized (this.mSync) {
            if (this.mNoBackupFilesDir == null) {
                this.mNoBackupFilesDir = new File(getDataDir(), "no_backup");
            }
            ensurePrivateDirExists = ensurePrivateDirExists(this.mNoBackupFilesDir);
        }
        return ensurePrivateDirExists;
    }

    public File getExternalFilesDir(String type) {
        File[] dirs = getExternalFilesDirs(type);
        File file = null;
        if (!IS_SWITCH_SD_ENABLED || !checkPrimaryVolumeIsSD()) {
            if (dirs != null && dirs.length > 0) {
                file = dirs[0];
            }
            return file;
        } else if (dirs == null || dirs.length <= 0) {
            return null;
        } else {
            if (dirs.length == 1) {
                return dirs[0];
            }
            return dirs[1];
        }
    }

    public File[] getExternalFilesDirs(String type) {
        File[] ensureExternalDirsExistOrFilter;
        synchronized (this.mSync) {
            File[] dirs = Environment.buildExternalStorageAppFilesDirs(getPackageName());
            if (type != null) {
                dirs = Environment.buildPaths(dirs, new String[]{type});
            }
            ensureExternalDirsExistOrFilter = ensureExternalDirsExistOrFilter(dirs);
        }
        return ensureExternalDirsExistOrFilter;
    }

    public File getObbDir() {
        File[] dirs = getObbDirs();
        File file = null;
        if (!IS_SWITCH_SD_ENABLED || !checkPrimaryVolumeIsSD()) {
            if (dirs != null && dirs.length > 0) {
                file = dirs[0];
            }
            return file;
        } else if (dirs == null || dirs.length <= 0) {
            return null;
        } else {
            if (dirs.length == 1) {
                return dirs[0];
            }
            return dirs[1];
        }
    }

    public File[] getObbDirs() {
        File[] ensureExternalDirsExistOrFilter;
        synchronized (this.mSync) {
            ensureExternalDirsExistOrFilter = ensureExternalDirsExistOrFilter(Environment.buildExternalStorageAppObbDirs(getPackageName()));
        }
        return ensureExternalDirsExistOrFilter;
    }

    public File getCacheDir() {
        synchronized (this.mSync) {
            if (isHbsMiniApp()) {
                if (this.mCacheDir == null) {
                    this.mCacheDir = getHbsMiniAppDir("security/cache");
                }
                File ensurePrivateDirExists = ensurePrivateDirExists(this.mCacheDir);
                return ensurePrivateDirExists;
            }
            if (this.mCacheDir == null) {
                this.mCacheDir = new File(getDataDir(), "cache");
            }
            File ensurePrivateCacheDirExists = ensurePrivateCacheDirExists(this.mCacheDir, XATTR_INODE_CACHE);
            return ensurePrivateCacheDirExists;
        }
    }

    public File getCodeCacheDir() {
        synchronized (this.mSync) {
            if (isHbsMiniApp()) {
                if (this.mCodeCacheDir == null) {
                    this.mCodeCacheDir = getHbsMiniAppDir("security/code_cache");
                }
                File ensurePrivateDirExists = ensurePrivateDirExists(this.mCodeCacheDir);
                return ensurePrivateDirExists;
            }
            if (this.mCodeCacheDir == null) {
                this.mCodeCacheDir = new File(getDataDir(), "code_cache");
            }
            File ensurePrivateCacheDirExists = ensurePrivateCacheDirExists(this.mCodeCacheDir, XATTR_INODE_CODE_CACHE);
            return ensurePrivateCacheDirExists;
        }
    }

    public File getExternalCacheDir() {
        File[] dirs = getExternalCacheDirs();
        File file = null;
        if (!IS_SWITCH_SD_ENABLED || !checkPrimaryVolumeIsSD()) {
            if (dirs != null && dirs.length > 0) {
                file = dirs[0];
            }
            return file;
        } else if (dirs == null || dirs.length <= 0) {
            return null;
        } else {
            if (dirs.length == 1) {
                return dirs[0];
            }
            return dirs[1];
        }
    }

    public File[] getExternalCacheDirs() {
        File[] ensureExternalDirsExistOrFilter;
        synchronized (this.mSync) {
            ensureExternalDirsExistOrFilter = ensureExternalDirsExistOrFilter(Environment.buildExternalStorageAppCacheDirs(getPackageName()));
        }
        return ensureExternalDirsExistOrFilter;
    }

    public File[] getExternalMediaDirs() {
        File[] ensureExternalDirsExistOrFilter;
        synchronized (this.mSync) {
            ensureExternalDirsExistOrFilter = ensureExternalDirsExistOrFilter(Environment.buildExternalStorageAppMediaDirs(getPackageName()));
        }
        return ensureExternalDirsExistOrFilter;
    }

    public File getPreloadsFileCache() {
        return Environment.getDataPreloadsFileCacheDirectory(getPackageName());
    }

    public File getFileStreamPath(String name) {
        return makeFilename(getFilesDir(), name);
    }

    public File getSharedPreferencesPath(String name) {
        File preferencesDir = getPreferencesDir();
        return makeFilename(preferencesDir, name + ".xml");
    }

    public String[] fileList() {
        return FileUtils.listOrEmpty(getFilesDir());
    }

    public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory) {
        return openOrCreateDatabase(name, mode, factory, null);
    }

    public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory, DatabaseErrorHandler errorHandler) {
        checkMode(mode);
        File f = getDatabasePath(name);
        int flags = 268435456;
        if ((mode & 8) != 0) {
            flags = 268435456 | 536870912;
        }
        if ((mode & 16) != 0) {
            flags |= 16;
        }
        SQLiteDatabase db = SQLiteDatabase.openDatabase(f.getPath(), factory, flags, errorHandler);
        setFilePermissionsFromMode(f.getPath(), mode, 0);
        return db;
    }

    public boolean moveDatabaseFrom(Context sourceContext, String name) {
        boolean z;
        synchronized (ContextImpl.class) {
            File source = sourceContext.getDatabasePath(name);
            z = moveFiles(source.getParentFile(), getDatabasePath(name).getParentFile(), source.getName()) != -1;
        }
        return z;
    }

    public boolean deleteDatabase(String name) {
        try {
            return SQLiteDatabase.deleteDatabase(getDatabasePath(name));
        } catch (Exception e) {
            return false;
        }
    }

    public File getDatabasePath(String name) {
        File f;
        if (name.charAt(0) == File.separatorChar) {
            File dir = new File(name.substring(0, name.lastIndexOf(File.separatorChar)));
            f = new File(dir, name.substring(name.lastIndexOf(File.separatorChar)));
            if (!dir.isDirectory() && dir.mkdir()) {
                FileUtils.setPermissions(dir.getPath(), RadioError.OEM_ERROR_5, -1, -1);
            }
        } else {
            f = makeFilename(getDatabasesDir(), name);
        }
        return f;
    }

    public String[] databaseList() {
        return FileUtils.listOrEmpty(getDatabasesDir());
    }

    private File getDatabasesDir() {
        File ensurePrivateDirExists;
        synchronized (this.mSync) {
            if (this.mDatabasesDir == null) {
                if ("android".equals(getPackageName())) {
                    this.mDatabasesDir = new File("/data/system");
                } else {
                    this.mDatabasesDir = new File(getDataDir(), "databases");
                    if (isHbsMiniApp()) {
                        this.mDatabasesDir = getHbsMiniAppDir("databases");
                    }
                }
            }
            ensurePrivateDirExists = ensurePrivateDirExists(this.mDatabasesDir);
        }
        return ensurePrivateDirExists;
    }

    private boolean isHbsMiniApp() {
        int uid = Os.getuid();
        return 19959 <= uid && uid <= 19999;
    }

    private File getHbsMiniAppDir(String name) {
        return new File(new File(getDataDir(), name), Integer.toString(Os.getuid()));
    }

    @Deprecated
    public Drawable getWallpaper() {
        return getWallpaperManager().getDrawable();
    }

    @Deprecated
    public Drawable peekWallpaper() {
        return getWallpaperManager().peekDrawable();
    }

    @Deprecated
    public int getWallpaperDesiredMinimumWidth() {
        return getWallpaperManager().getDesiredMinimumWidth();
    }

    @Deprecated
    public int getWallpaperDesiredMinimumHeight() {
        return getWallpaperManager().getDesiredMinimumHeight();
    }

    @Deprecated
    public void setWallpaper(Bitmap bitmap) throws IOException {
        getWallpaperManager().setBitmap(bitmap);
    }

    @Deprecated
    public void setWallpaper(InputStream data) throws IOException {
        getWallpaperManager().setStream(data);
    }

    @Deprecated
    public void clearWallpaper() throws IOException {
        getWallpaperManager().clear();
    }

    private WallpaperManager getWallpaperManager() {
        return (WallpaperManager) getSystemService(WallpaperManager.class);
    }

    public void startActivity(Intent intent) {
        warnIfCallingFromSystemProcess();
        startActivity(intent, null);
    }

    public void startActivityAsUser(Intent intent, UserHandle user) {
        startActivityAsUser(intent, null, user);
    }

    public void startActivity(Intent intent, Bundle options) {
        if (!HwSystemManager.canStartActivity(getApplicationContext(), intent)) {
            Log.i(TAG, "this app not allowed to start activity:" + intent);
            return;
        }
        warnIfCallingFromSystemProcess();
        int targetSdkVersion = getApplicationInfo().targetSdkVersion;
        if ((intent.getFlags() & 268435456) != 0 || ((targetSdkVersion >= 24 && targetSdkVersion < 28) || !(options == null || ActivityOptions.fromBundle(options).getLaunchTaskId() == -1))) {
            this.mVrMananger.setTargetComponentName(intent.getComponent());
            this.mMainThread.getInstrumentation().execStartActivity(getOuterContext(), (IBinder) this.mMainThread.getApplicationThread(), (IBinder) null, (Activity) null, intent, -1, HwPCUtils.hookStartActivityOptions(this, options));
            return;
        }
        throw new AndroidRuntimeException("Calling startActivity() from outside of an Activity  context requires the FLAG_ACTIVITY_NEW_TASK flag. Is this really what you want?");
    }

    public void startActivityAsUser(Intent intent, Bundle options, UserHandle user) {
        Bundle bundle;
        try {
            if (Jlog.isPerfTest()) {
                try {
                    Jlog.i(3022, Jlog.getMessage(TAG, "startActivityAsUser", "whopkg=" + getPackageName() + "&" + Intent.toPkgClsString(intent)));
                } catch (RemoteException e) {
                    e = e;
                    Intent intent2 = intent;
                    Bundle bundle2 = options;
                }
            }
            bundle = options;
            try {
                Bundle options2 = HwPCUtils.hookStartActivityOptions(this, bundle);
                try {
                    Intent intent3 = intent;
                    try {
                        ActivityManager.getService().startActivityAsUser(this.mMainThread.getApplicationThread(), getBasePackageName(), intent3, intent3.resolveTypeIfNeeded(getContentResolver()), null, null, 0, 268435456, null, options2, user.getIdentifier());
                    } catch (RemoteException e2) {
                        e = e2;
                    }
                } catch (RemoteException e3) {
                    e = e3;
                    Intent intent4 = intent;
                    throw e.rethrowFromSystemServer();
                }
            } catch (RemoteException e4) {
                e = e4;
                Intent intent5 = intent;
                Bundle options3 = bundle;
                throw e.rethrowFromSystemServer();
            }
        } catch (RemoteException e5) {
            e = e5;
            Intent intent6 = intent;
            bundle = options;
            Bundle options32 = bundle;
            throw e.rethrowFromSystemServer();
        }
    }

    public void startActivities(Intent[] intents) {
        warnIfCallingFromSystemProcess();
        startActivities(intents, null);
    }

    public int startActivitiesAsUser(Intent[] intents, Bundle options, UserHandle userHandle) {
        if ((intents[0].getFlags() & 268435456) != 0) {
            return this.mMainThread.getInstrumentation().execStartActivitiesAsUser(getOuterContext(), this.mMainThread.getApplicationThread(), null, null, intents, HwPCUtils.hookStartActivityOptions(this, options), userHandle.getIdentifier());
        }
        throw new AndroidRuntimeException("Calling startActivities() from outside of an Activity  context requires the FLAG_ACTIVITY_NEW_TASK flag on first Intent. Is this really what you want?");
    }

    public void startActivities(Intent[] intents, Bundle options) {
        warnIfCallingFromSystemProcess();
        if ((intents[0].getFlags() & 268435456) != 0) {
            this.mMainThread.getInstrumentation().execStartActivities(getOuterContext(), this.mMainThread.getApplicationThread(), null, null, intents, HwPCUtils.hookStartActivityOptions(this, options));
            return;
        }
        throw new AndroidRuntimeException("Calling startActivities() from outside of an Activity  context requires the FLAG_ACTIVITY_NEW_TASK flag on first Intent. Is this really what you want?");
    }

    public void startIntentSender(IntentSender intent, Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags) throws IntentSender.SendIntentException {
        startIntentSender(intent, fillInIntent, flagsMask, flagsValues, extraFlags, null);
    }

    public void startIntentSender(IntentSender intent, Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags, Bundle options) throws IntentSender.SendIntentException {
        IIntentSender iIntentSender;
        Intent intent2 = fillInIntent;
        String resolvedType = null;
        if (intent2 != null) {
            try {
                fillInIntent.migrateExtraStreamToClipData();
                intent2.prepareToLeaveProcess((Context) this);
                resolvedType = intent2.resolveTypeIfNeeded(getContentResolver());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
        IActivityManager service = ActivityManager.getService();
        ActivityThread.ApplicationThread applicationThread = this.mMainThread.getApplicationThread();
        if (intent != null) {
            iIntentSender = intent.getTarget();
        } else {
            iIntentSender = null;
        }
        String str = resolvedType;
        int result = service.startActivityIntentSender(applicationThread, iIntentSender, intent != null ? intent.getWhitelistToken() : null, intent2, resolvedType, null, null, 0, flagsMask, flagsValues, options);
        if (result != -96) {
            Instrumentation.checkStartActivityResult(result, null);
            return;
        }
        throw new IntentSender.SendIntentException();
    }

    public void sendBroadcast(Intent intent) {
        Intent intent2 = intent;
        warnIfCallingFromSystemProcess();
        if (HwSystemManager.canSendBroadcast(this, intent)) {
            String resolvedType = intent2.resolveTypeIfNeeded(getContentResolver());
            try {
                intent2.prepareToLeaveProcess((Context) this);
                ActivityManager.getService().broadcastIntent(this.mMainThread.getApplicationThread(), intent2, resolvedType, null, -1, null, null, null, -1, null, false, false, getUserId());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void sendBroadcast(Intent intent, String receiverPermission) {
        Intent intent2 = intent;
        warnIfCallingFromSystemProcess();
        if (HwSystemManager.canSendBroadcast(this, intent)) {
            String resolvedType = intent2.resolveTypeIfNeeded(getContentResolver());
            String[] receiverPermissions = receiverPermission == null ? null : new String[]{receiverPermission};
            try {
                intent2.prepareToLeaveProcess((Context) this);
                ActivityManager.getService().broadcastIntent(this.mMainThread.getApplicationThread(), intent2, resolvedType, null, -1, null, null, receiverPermissions, -1, null, false, false, getUserId());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void sendBroadcastMultiplePermissions(Intent intent, String[] receiverPermissions) {
        Intent intent2 = intent;
        warnIfCallingFromSystemProcess();
        String resolvedType = intent2.resolveTypeIfNeeded(getContentResolver());
        try {
            intent2.prepareToLeaveProcess((Context) this);
            ActivityManager.getService().broadcastIntent(this.mMainThread.getApplicationThread(), intent2, resolvedType, null, -1, null, null, receiverPermissions, -1, null, false, false, getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void sendBroadcastAsUserMultiplePermissions(Intent intent, UserHandle user, String[] receiverPermissions) {
        Intent intent2 = intent;
        warnIfCallingFromSystemProcess();
        String resolvedType = intent2.resolveTypeIfNeeded(getContentResolver());
        try {
            intent2.prepareToLeaveProcess((Context) this);
            ActivityManager.getService().broadcastIntent(this.mMainThread.getApplicationThread(), intent2, resolvedType, null, -1, null, null, receiverPermissions, -1, null, false, false, user.getIdentifier());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void sendBroadcast(Intent intent, String receiverPermission, Bundle options) {
        Intent intent2 = intent;
        warnIfCallingFromSystemProcess();
        String resolvedType = intent2.resolveTypeIfNeeded(getContentResolver());
        String[] receiverPermissions = receiverPermission == null ? null : new String[]{receiverPermission};
        try {
            intent2.prepareToLeaveProcess((Context) this);
            ActivityManager.getService().broadcastIntent(this.mMainThread.getApplicationThread(), intent2, resolvedType, null, -1, null, null, receiverPermissions, -1, options, false, false, getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void sendBroadcast(Intent intent, String receiverPermission, int appOp) {
        Intent intent2 = intent;
        warnIfCallingFromSystemProcess();
        if (HwSystemManager.canSendBroadcast(this, intent)) {
            String resolvedType = intent2.resolveTypeIfNeeded(getContentResolver());
            String[] receiverPermissions = receiverPermission == null ? null : new String[]{receiverPermission};
            try {
                intent2.prepareToLeaveProcess((Context) this);
                ActivityManager.getService().broadcastIntent(this.mMainThread.getApplicationThread(), intent2, resolvedType, null, -1, null, null, receiverPermissions, appOp, null, false, false, getUserId());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void sendOrderedBroadcast(Intent intent, String receiverPermission) {
        Intent intent2 = intent;
        warnIfCallingFromSystemProcess();
        String resolvedType = intent2.resolveTypeIfNeeded(getContentResolver());
        String[] receiverPermissions = receiverPermission == null ? null : new String[]{receiverPermission};
        try {
            intent2.prepareToLeaveProcess((Context) this);
            ActivityManager.getService().broadcastIntent(this.mMainThread.getApplicationThread(), intent2, resolvedType, null, -1, null, null, receiverPermissions, -1, null, true, false, getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void sendOrderedBroadcast(Intent intent, String receiverPermission, BroadcastReceiver resultReceiver, Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {
        if (HwSystemManager.canSendBroadcast(this, intent)) {
            sendOrderedBroadcast(intent, receiverPermission, -1, resultReceiver, scheduler, initialCode, initialData, initialExtras, null);
        }
    }

    public void sendOrderedBroadcast(Intent intent, String receiverPermission, Bundle options, BroadcastReceiver resultReceiver, Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {
        sendOrderedBroadcast(intent, receiverPermission, -1, resultReceiver, scheduler, initialCode, initialData, initialExtras, options);
    }

    public void sendOrderedBroadcast(Intent intent, String receiverPermission, int appOp, BroadcastReceiver resultReceiver, Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {
        sendOrderedBroadcast(intent, receiverPermission, appOp, resultReceiver, scheduler, initialCode, initialData, initialExtras, null);
    }

    /* access modifiers changed from: package-private */
    public void sendOrderedBroadcast(Intent intent, String receiverPermission, int appOp, BroadcastReceiver resultReceiver, Handler scheduler, int initialCode, String initialData, Bundle initialExtras, Bundle options) {
        Handler scheduler2;
        Handler scheduler3;
        Intent intent2 = intent;
        warnIfCallingFromSystemProcess();
        if (HwSystemManager.canSendBroadcast(this, intent)) {
            IIntentReceiver rd = null;
            if (resultReceiver != null) {
                if (this.mPackageInfo != null) {
                    if (scheduler == null) {
                        scheduler2 = this.mMainThread.getHandler();
                    } else {
                        scheduler2 = scheduler;
                    }
                    rd = this.mPackageInfo.getReceiverDispatcher(resultReceiver, getOuterContext(), scheduler2, this.mMainThread.getInstrumentation(), false);
                } else {
                    if (scheduler == null) {
                        scheduler3 = this.mMainThread.getHandler();
                    } else {
                        scheduler3 = scheduler;
                    }
                    LoadedApk.ReceiverDispatcher receiverDispatcher = new LoadedApk.ReceiverDispatcher(resultReceiver, getOuterContext(), scheduler2, null, false);
                    rd = receiverDispatcher.getIIntentReceiver();
                }
            } else {
                Handler handler = scheduler;
            }
            IIntentReceiver rd2 = rd;
            String resolvedType = intent2.resolveTypeIfNeeded(getContentResolver());
            String[] receiverPermissions = receiverPermission == null ? null : new String[]{receiverPermission};
            try {
                intent2.prepareToLeaveProcess((Context) this);
                ActivityManager.getService().broadcastIntent(this.mMainThread.getApplicationThread(), intent2, resolvedType, rd2, initialCode, initialData, initialExtras, receiverPermissions, appOp, options, true, false, getUserId());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void sendBroadcastAsUser(Intent intent, UserHandle user) {
        Intent intent2 = intent;
        if (HwSystemManager.canSendBroadcast(this, intent)) {
            String resolvedType = intent2.resolveTypeIfNeeded(getContentResolver());
            try {
                intent2.prepareToLeaveProcess((Context) this);
                ActivityManager.getService().broadcastIntent(this.mMainThread.getApplicationThread(), intent2, resolvedType, null, -1, null, null, null, -1, null, false, false, user.getIdentifier());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void sendBroadcastAsUser(Intent intent, UserHandle user, String receiverPermission) {
        sendBroadcastAsUser(intent, user, receiverPermission, -1);
    }

    public void sendBroadcastAsUser(Intent intent, UserHandle user, String receiverPermission, Bundle options) {
        Intent intent2 = intent;
        String resolvedType = intent2.resolveTypeIfNeeded(getContentResolver());
        String[] receiverPermissions = receiverPermission == null ? null : new String[]{receiverPermission};
        try {
            intent2.prepareToLeaveProcess((Context) this);
            ActivityManager.getService().broadcastIntent(this.mMainThread.getApplicationThread(), intent2, resolvedType, null, -1, null, null, receiverPermissions, -1, options, false, false, user.getIdentifier());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void sendBroadcastAsUser(Intent intent, UserHandle user, String receiverPermission, int appOp) {
        Intent intent2 = intent;
        if (HwSystemManager.canSendBroadcast(this, intent)) {
            String resolvedType = intent2.resolveTypeIfNeeded(getContentResolver());
            String[] receiverPermissions = receiverPermission == null ? null : new String[]{receiverPermission};
            try {
                intent2.prepareToLeaveProcess((Context) this);
                ActivityManager.getService().broadcastIntent(this.mMainThread.getApplicationThread(), intent2, resolvedType, null, -1, null, null, receiverPermissions, appOp, null, false, false, user.getIdentifier());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void sendOrderedBroadcastAsUser(Intent intent, UserHandle user, String receiverPermission, BroadcastReceiver resultReceiver, Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {
        sendOrderedBroadcastAsUser(intent, user, receiverPermission, -1, null, resultReceiver, scheduler, initialCode, initialData, initialExtras);
    }

    public void sendOrderedBroadcastAsUser(Intent intent, UserHandle user, String receiverPermission, int appOp, BroadcastReceiver resultReceiver, Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {
        sendOrderedBroadcastAsUser(intent, user, receiverPermission, appOp, null, resultReceiver, scheduler, initialCode, initialData, initialExtras);
    }

    public void sendOrderedBroadcastAsUser(Intent intent, UserHandle user, String receiverPermission, int appOp, Bundle options, BroadcastReceiver resultReceiver, Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {
        Handler scheduler2;
        Handler scheduler3;
        Intent intent2 = intent;
        if (HwSystemManager.canSendBroadcast(this, intent)) {
            IIntentReceiver rd = null;
            if (resultReceiver != null) {
                if (this.mPackageInfo != null) {
                    if (scheduler == null) {
                        scheduler2 = this.mMainThread.getHandler();
                    } else {
                        scheduler2 = scheduler;
                    }
                    rd = this.mPackageInfo.getReceiverDispatcher(resultReceiver, getOuterContext(), scheduler2, this.mMainThread.getInstrumentation(), false);
                } else {
                    if (scheduler == null) {
                        scheduler3 = this.mMainThread.getHandler();
                    } else {
                        scheduler3 = scheduler;
                    }
                    LoadedApk.ReceiverDispatcher receiverDispatcher = new LoadedApk.ReceiverDispatcher(resultReceiver, getOuterContext(), scheduler2, null, false);
                    rd = receiverDispatcher.getIIntentReceiver();
                }
            } else {
                Handler handler = scheduler;
            }
            IIntentReceiver rd2 = rd;
            String resolvedType = intent2.resolveTypeIfNeeded(getContentResolver());
            String[] receiverPermissions = receiverPermission == null ? null : new String[]{receiverPermission};
            try {
                intent2.prepareToLeaveProcess((Context) this);
                ActivityManager.getService().broadcastIntent(this.mMainThread.getApplicationThread(), intent2, resolvedType, rd2, initialCode, initialData, initialExtras, receiverPermissions, appOp, options, true, false, user.getIdentifier());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    @Deprecated
    public void sendStickyBroadcast(Intent intent) {
        Intent intent2 = intent;
        warnIfCallingFromSystemProcess();
        if (HwSystemManager.canSendBroadcast(this, intent)) {
            String resolvedType = intent2.resolveTypeIfNeeded(getContentResolver());
            try {
                intent2.prepareToLeaveProcess((Context) this);
                ActivityManager.getService().broadcastIntent(this.mMainThread.getApplicationThread(), intent2, resolvedType, null, -1, null, null, null, -1, null, false, true, getUserId());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    @Deprecated
    public void sendStickyOrderedBroadcast(Intent intent, BroadcastReceiver resultReceiver, Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {
        Handler scheduler2;
        Handler scheduler3;
        Intent intent2 = intent;
        warnIfCallingFromSystemProcess();
        if (HwSystemManager.canSendBroadcast(this, intent)) {
            IIntentReceiver rd = null;
            if (resultReceiver != null) {
                if (this.mPackageInfo != null) {
                    if (scheduler == null) {
                        scheduler2 = this.mMainThread.getHandler();
                    } else {
                        scheduler2 = scheduler;
                    }
                    rd = this.mPackageInfo.getReceiverDispatcher(resultReceiver, getOuterContext(), scheduler2, this.mMainThread.getInstrumentation(), false);
                } else {
                    if (scheduler == null) {
                        scheduler3 = this.mMainThread.getHandler();
                    } else {
                        scheduler3 = scheduler;
                    }
                    LoadedApk.ReceiverDispatcher receiverDispatcher = new LoadedApk.ReceiverDispatcher(resultReceiver, getOuterContext(), scheduler2, null, false);
                    rd = receiverDispatcher.getIIntentReceiver();
                }
            } else {
                Handler handler = scheduler;
            }
            IIntentReceiver rd2 = rd;
            String resolvedType = intent2.resolveTypeIfNeeded(getContentResolver());
            try {
                intent2.prepareToLeaveProcess((Context) this);
                ActivityManager.getService().broadcastIntent(this.mMainThread.getApplicationThread(), intent2, resolvedType, rd2, initialCode, initialData, initialExtras, null, -1, null, true, true, getUserId());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    @Deprecated
    public void removeStickyBroadcast(Intent intent) {
        String resolvedType = intent.resolveTypeIfNeeded(getContentResolver());
        if (resolvedType != null) {
            intent = new Intent(intent);
            intent.setDataAndType(intent.getData(), resolvedType);
        }
        try {
            intent.prepareToLeaveProcess((Context) this);
            ActivityManager.getService().unbroadcastIntent(this.mMainThread.getApplicationThread(), intent, getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public void sendStickyBroadcastAsUser(Intent intent, UserHandle user) {
        Intent intent2 = intent;
        if (HwSystemManager.canSendBroadcast(this, intent)) {
            String resolvedType = intent2.resolveTypeIfNeeded(getContentResolver());
            try {
                intent2.prepareToLeaveProcess((Context) this);
                ActivityManager.getService().broadcastIntent(this.mMainThread.getApplicationThread(), intent2, resolvedType, null, -1, null, null, null, -1, null, false, true, user.getIdentifier());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    @Deprecated
    public void sendStickyBroadcastAsUser(Intent intent, UserHandle user, Bundle options) {
        Intent intent2 = intent;
        String resolvedType = intent2.resolveTypeIfNeeded(getContentResolver());
        try {
            intent2.prepareToLeaveProcess((Context) this);
            ActivityManager.getService().broadcastIntent(this.mMainThread.getApplicationThread(), intent2, resolvedType, null, -1, null, null, null, -1, options, false, true, user.getIdentifier());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public void sendStickyOrderedBroadcastAsUser(Intent intent, UserHandle user, BroadcastReceiver resultReceiver, Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {
        Handler scheduler2;
        Handler scheduler3;
        Intent intent2 = intent;
        if (HwSystemManager.canSendBroadcast(this, intent)) {
            IIntentReceiver rd = null;
            if (resultReceiver != null) {
                if (this.mPackageInfo != null) {
                    if (scheduler == null) {
                        scheduler2 = this.mMainThread.getHandler();
                    } else {
                        scheduler2 = scheduler;
                    }
                    rd = this.mPackageInfo.getReceiverDispatcher(resultReceiver, getOuterContext(), scheduler2, this.mMainThread.getInstrumentation(), false);
                } else {
                    if (scheduler == null) {
                        scheduler3 = this.mMainThread.getHandler();
                    } else {
                        scheduler3 = scheduler;
                    }
                    LoadedApk.ReceiverDispatcher receiverDispatcher = new LoadedApk.ReceiverDispatcher(resultReceiver, getOuterContext(), scheduler2, null, false);
                    rd = receiverDispatcher.getIIntentReceiver();
                }
            } else {
                Handler handler = scheduler;
            }
            IIntentReceiver rd2 = rd;
            String resolvedType = intent2.resolveTypeIfNeeded(getContentResolver());
            try {
                intent2.prepareToLeaveProcess((Context) this);
                ActivityManager.getService().broadcastIntent(this.mMainThread.getApplicationThread(), intent2, resolvedType, rd2, initialCode, initialData, initialExtras, null, -1, null, true, true, user.getIdentifier());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    @Deprecated
    public void removeStickyBroadcastAsUser(Intent intent, UserHandle user) {
        String resolvedType = intent.resolveTypeIfNeeded(getContentResolver());
        if (resolvedType != null) {
            intent = new Intent(intent);
            intent.setDataAndType(intent.getData(), resolvedType);
        }
        try {
            intent.prepareToLeaveProcess((Context) this);
            ActivityManager.getService().unbroadcastIntent(this.mMainThread.getApplicationThread(), intent, user.getIdentifier());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        return registerReceiver(receiver, filter, null, null);
    }

    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter, int flags) {
        return registerReceiver(receiver, filter, null, null, flags);
    }

    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter, String broadcastPermission, Handler scheduler) {
        return registerReceiverInternal(receiver, getUserId(), filter, broadcastPermission, scheduler, getOuterContext(), 0);
    }

    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter, String broadcastPermission, Handler scheduler, int flags) {
        return registerReceiverInternal(receiver, getUserId(), filter, broadcastPermission, scheduler, getOuterContext(), flags);
    }

    public Intent registerReceiverAsUser(BroadcastReceiver receiver, UserHandle user, IntentFilter filter, String broadcastPermission, Handler scheduler) {
        return registerReceiverInternal(receiver, user.getIdentifier(), filter, broadcastPermission, scheduler, getOuterContext(), 0);
    }

    private Intent registerReceiverInternal(BroadcastReceiver receiver, int userId, IntentFilter filter, String broadcastPermission, Handler scheduler, Context context, int flags) {
        IIntentReceiver rd;
        Handler scheduler2;
        Handler scheduler3;
        IntentFilter intentFilter = filter;
        if (receiver != null) {
            if (this.mPackageInfo == null || context == null) {
                if (scheduler == null) {
                    scheduler3 = this.mMainThread.getHandler();
                } else {
                    scheduler3 = scheduler;
                }
                LoadedApk.ReceiverDispatcher receiverDispatcher = new LoadedApk.ReceiverDispatcher(receiver, context, scheduler2, null, true);
                rd = receiverDispatcher.getIIntentReceiver();
            } else {
                if (scheduler == null) {
                    scheduler2 = this.mMainThread.getHandler();
                } else {
                    scheduler2 = scheduler;
                }
                rd = this.mPackageInfo.getReceiverDispatcher(receiver, context, scheduler2, this.mMainThread.getInstrumentation(), true);
            }
        } else {
            rd = null;
        }
        if (intentFilter != null) {
            if (receiver == null || context == null || this.mBasePackageName == null) {
                intentFilter.setIdentifier("");
            } else {
                intentFilter.setIdentifier(this.mBasePackageName + "+" + Process.myPid() + "+" + getObjectSimplifiedString(context) + "+" + getClassSimplifiedString(this.mBasePackageName, receiver.getClass()) + "+" + filter.getActionsHashcode());
            }
        }
        try {
            Intent intent = ActivityManager.getService().registerReceiver(this.mMainThread.getApplicationThread(), this.mBasePackageName, rd, intentFilter, broadcastPermission, userId, flags);
            if (intent != null) {
                intent.setExtrasClassLoader(getClassLoader());
                intent.prepareToEnterProcess();
            }
            return intent;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void unregisterReceiver(BroadcastReceiver receiver) {
        if (this.mPackageInfo != null) {
            try {
                ActivityManager.getService().unregisterReceiver(this.mPackageInfo.forgetReceiverDispatcher(getOuterContext(), receiver));
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            throw new RuntimeException("Not supported in system context");
        }
    }

    private void validateServiceIntent(Intent service) {
        if (service.getComponent() != null || service.getPackage() != null) {
            return;
        }
        if (getApplicationInfo().targetSdkVersion < 21) {
            Log.w(TAG, "Implicit intents with startService are not safe: " + service + " " + Debug.getCallers(2, 3));
            return;
        }
        throw new IllegalArgumentException("Service Intent must be explicit: " + service);
    }

    public ComponentName startService(Intent service) {
        warnIfCallingFromSystemProcess();
        return startServiceCommon(service, false, this.mUser);
    }

    public ComponentName startForegroundService(Intent service) {
        warnIfCallingFromSystemProcess();
        return startServiceCommon(service, true, this.mUser);
    }

    public boolean stopService(Intent service) {
        warnIfCallingFromSystemProcess();
        return stopServiceCommon(service, this.mUser);
    }

    public ComponentName startServiceAsUser(Intent service, UserHandle user) {
        return startServiceCommon(service, false, user);
    }

    public ComponentName startForegroundServiceAsUser(Intent service, UserHandle user) {
        return startServiceCommon(service, true, user);
    }

    private ComponentName startServiceCommon(Intent service, boolean requireForeground, UserHandle user) {
        try {
            validateServiceIntent(service);
            service.prepareToLeaveProcess((Context) this);
            ComponentName cn = ActivityManager.getService().startService(this.mMainThread.getApplicationThread(), service, service.resolveTypeIfNeeded(getContentResolver()), requireForeground, getOpPackageName(), user.getIdentifier());
            if (cn != null) {
                if (cn.getPackageName().equals("!")) {
                    throw new SecurityException("Not allowed to start service " + service + " without permission " + cn.getClassName());
                } else if (cn.getPackageName().equals("!!")) {
                    throw new SecurityException("Unable to start service " + service + ": " + cn.getClassName());
                } else if (cn.getPackageName().equals("?")) {
                    throw new IllegalStateException("Not allowed to start service " + service + ": " + cn.getClassName());
                }
            }
            return cn;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean stopServiceAsUser(Intent service, UserHandle user) {
        return stopServiceCommon(service, user);
    }

    private boolean stopServiceCommon(Intent service, UserHandle user) {
        try {
            validateServiceIntent(service);
            service.prepareToLeaveProcess((Context) this);
            int res = ActivityManager.getService().stopService(this.mMainThread.getApplicationThread(), service, service.resolveTypeIfNeeded(getContentResolver()), user.getIdentifier());
            if (res >= 0) {
                return res != 0;
            }
            throw new SecurityException("Not allowed to stop service " + service);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean bindService(Intent service, ServiceConnection conn, int flags) {
        warnIfCallingFromSystemProcess();
        return bindServiceCommon(service, conn, flags, this.mMainThread.getHandler(), getUser());
    }

    public boolean bindServiceAsUser(Intent service, ServiceConnection conn, int flags, UserHandle user) {
        return bindServiceCommon(service, conn, flags, this.mMainThread.getHandler(), user);
    }

    public boolean bindServiceAsUser(Intent service, ServiceConnection conn, int flags, Handler handler, UserHandle user) {
        if (handler != null) {
            return bindServiceCommon(service, conn, flags, handler, user);
        }
        throw new IllegalArgumentException("handler must not be null.");
    }

    public IServiceConnection getServiceDispatcher(ServiceConnection conn, Handler handler, int flags) {
        return this.mPackageInfo.getServiceDispatcher(conn, getOuterContext(), handler, flags);
    }

    public IApplicationThread getIApplicationThread() {
        return this.mMainThread.getApplicationThread();
    }

    public Handler getMainThreadHandler() {
        return this.mMainThread.getHandler();
    }

    private boolean bindServiceCommon(Intent service, ServiceConnection conn, int flags, Handler handler, UserHandle user) {
        Intent intent = service;
        ServiceConnection serviceConnection = conn;
        int i = flags;
        if (serviceConnection == null) {
            Handler handler2 = handler;
            throw new IllegalArgumentException("connection is null");
        } else if (this.mPackageInfo != null) {
            IServiceConnection sd = this.mPackageInfo.getServiceDispatcher(serviceConnection, getOuterContext(), handler, i);
            validateServiceIntent(service);
            try {
                if (getActivityToken() == null && (i & 1) == 0 && this.mPackageInfo != null && this.mPackageInfo.getApplicationInfo().targetSdkVersion < 14) {
                    i |= 32;
                }
                int flags2 = i;
                try {
                    intent.prepareToLeaveProcess((Context) this);
                    int res = ActivityManager.getService().bindService(this.mMainThread.getApplicationThread(), getActivityToken(), intent, intent.resolveTypeIfNeeded(getContentResolver()), sd, flags2, getOpPackageName(), user.getIdentifier());
                    if (res >= 0) {
                        return res != 0;
                    }
                    throw new SecurityException("Not allowed to bind to service " + intent);
                } catch (RemoteException e) {
                    e = e;
                    throw e.rethrowFromSystemServer();
                }
            } catch (RemoteException e2) {
                e = e2;
                int i2 = i;
                throw e.rethrowFromSystemServer();
            }
        } else {
            Handler handler3 = handler;
            throw new RuntimeException("Not supported in system context");
        }
    }

    public void unbindService(ServiceConnection conn) {
        if (conn == null) {
            throw new IllegalArgumentException("connection is null");
        } else if (this.mPackageInfo != null) {
            try {
                ActivityManager.getService().unbindService(this.mPackageInfo.forgetServiceDispatcher(getOuterContext(), conn));
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            throw new RuntimeException("Not supported in system context");
        }
    }

    public boolean startInstrumentation(ComponentName className, String profileFile, Bundle arguments) {
        if (arguments != null) {
            try {
                arguments.setAllowFds(false);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
        return ActivityManager.getService().startInstrumentation(className, profileFile, 0, arguments, null, null, getUserId(), null);
    }

    public Object getSystemService(String name) {
        return SystemServiceRegistry.getSystemService(this, name);
    }

    public String getSystemServiceName(Class<?> serviceClass) {
        return SystemServiceRegistry.getSystemServiceName(serviceClass);
    }

    public int checkPermission(String permission, int pid, int uid) {
        if (permission != null) {
            IActivityManager am = ActivityManager.getService();
            if (am == null) {
                int appId = UserHandle.getAppId(uid);
                if (appId == 0 || appId == 1000) {
                    Slog.w(TAG, "Missing ActivityManager; assuming " + uid + " holds " + permission);
                    return 0;
                }
                Slog.w(TAG, "Missing ActivityManager; assuming " + uid + " does not hold " + permission);
                return -1;
            }
            try {
                return am.checkPermission(permission, pid, uid);
            } catch (RemoteException e) {
                Flog.e(201, "checkPermission " + permission + ", for pid " + pid + ", uid " + pid);
                throw e.rethrowFromSystemServer();
            }
        } else {
            throw new IllegalArgumentException("permission is null");
        }
    }

    public int checkPermission(String permission, int pid, int uid, IBinder callerToken) {
        if (permission != null) {
            try {
                return ActivityManager.getService().checkPermissionWithToken(permission, pid, uid, callerToken);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            throw new IllegalArgumentException("permission is null");
        }
    }

    public int checkCallingPermission(String permission) {
        if (permission != null) {
            int pid = Binder.getCallingPid();
            if (pid != Process.myPid()) {
                return checkPermission(permission, pid, Binder.getCallingUid());
            }
            return -1;
        }
        throw new IllegalArgumentException("permission is null");
    }

    public int checkCallingOrSelfPermission(String permission) {
        if (permission != null) {
            return checkPermission(permission, Binder.getCallingPid(), Binder.getCallingUid());
        }
        throw new IllegalArgumentException("permission is null");
    }

    public int checkSelfPermission(String permission) {
        if (permission != null) {
            return checkPermission(permission, Process.myPid(), Process.myUid());
        }
        throw new IllegalArgumentException("permission is null");
    }

    private void enforce(String permission, int resultOfCheck, boolean selfToo, int uid, String message) {
        String str;
        String str2;
        if (resultOfCheck != 0) {
            StringBuilder sb = new StringBuilder();
            if (message != null) {
                str = message + ": ";
            } else {
                str = "";
            }
            sb.append(str);
            if (selfToo) {
                str2 = "Neither user " + uid + " nor current process has ";
            } else {
                str2 = "uid " + uid + " does not have ";
            }
            sb.append(str2);
            sb.append(permission);
            sb.append(".");
            throw new SecurityException(sb.toString());
        }
    }

    public void enforcePermission(String permission, int pid, int uid, String message) {
        enforce(permission, checkPermission(permission, pid, uid), false, uid, message);
    }

    public void enforceCallingPermission(String permission, String message) {
        enforce(permission, checkCallingPermission(permission), false, Binder.getCallingUid(), message);
    }

    public void enforceCallingOrSelfPermission(String permission, String message) {
        enforce(permission, checkCallingOrSelfPermission(permission), true, Binder.getCallingUid(), message);
    }

    public void grantUriPermission(String toPackage, Uri uri, int modeFlags) {
        try {
            ActivityManager.getService().grantUriPermission(this.mMainThread.getApplicationThread(), toPackage, ContentProvider.getUriWithoutUserId(uri), modeFlags, resolveUserId(uri));
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void revokeUriPermission(Uri uri, int modeFlags) {
        try {
            ActivityManager.getService().revokeUriPermission(this.mMainThread.getApplicationThread(), null, ContentProvider.getUriWithoutUserId(uri), modeFlags, resolveUserId(uri));
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void revokeUriPermission(String targetPackage, Uri uri, int modeFlags) {
        try {
            ActivityManager.getService().revokeUriPermission(this.mMainThread.getApplicationThread(), targetPackage, ContentProvider.getUriWithoutUserId(uri), modeFlags, resolveUserId(uri));
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int checkUriPermission(Uri uri, int pid, int uid, int modeFlags) {
        try {
            return ActivityManager.getService().checkUriPermission(ContentProvider.getUriWithoutUserId(uri), pid, uid, modeFlags, resolveUserId(uri), null);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int checkUriPermission(Uri uri, int pid, int uid, int modeFlags, IBinder callerToken) {
        try {
            return ActivityManager.getService().checkUriPermission(ContentProvider.getUriWithoutUserId(uri), pid, uid, modeFlags, resolveUserId(uri), callerToken);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    private int resolveUserId(Uri uri) {
        return ContentProvider.getUserIdFromUri(uri, getUserId());
    }

    public int checkCallingUriPermission(Uri uri, int modeFlags) {
        int pid = Binder.getCallingPid();
        if (pid != Process.myPid()) {
            return checkUriPermission(uri, pid, Binder.getCallingUid(), modeFlags);
        }
        return -1;
    }

    public int checkCallingOrSelfUriPermission(Uri uri, int modeFlags) {
        return checkUriPermission(uri, Binder.getCallingPid(), Binder.getCallingUid(), modeFlags);
    }

    public int checkUriPermission(Uri uri, String readPermission, String writePermission, int pid, int uid, int modeFlags) {
        int i;
        if ((modeFlags & 1) != 0 && (readPermission == null || checkPermission(readPermission, pid, uid) == 0)) {
            return 0;
        }
        if ((modeFlags & 2) != 0 && (writePermission == null || checkPermission(writePermission, pid, uid) == 0)) {
            return 0;
        }
        if (uri != null) {
            i = checkUriPermission(uri, pid, uid, modeFlags);
        } else {
            i = -1;
        }
        return i;
    }

    private String uriModeFlagToString(int uriModeFlags) {
        StringBuilder builder = new StringBuilder();
        if ((uriModeFlags & 1) != 0) {
            builder.append("read and ");
        }
        if ((uriModeFlags & 2) != 0) {
            builder.append("write and ");
        }
        if ((uriModeFlags & 64) != 0) {
            builder.append("persistable and ");
        }
        if ((uriModeFlags & 128) != 0) {
            builder.append("prefix and ");
        }
        if (builder.length() > 5) {
            builder.setLength(builder.length() - 5);
            return builder.toString();
        }
        throw new IllegalArgumentException("Unknown permission mode flags: " + uriModeFlags);
    }

    private void enforceForUri(int modeFlags, int resultOfCheck, boolean selfToo, int uid, Uri uri, String message) {
        String str;
        String str2;
        if (resultOfCheck != 0) {
            StringBuilder sb = new StringBuilder();
            if (message != null) {
                str = message + ": ";
            } else {
                str = "";
            }
            sb.append(str);
            if (selfToo) {
                str2 = "Neither user " + uid + " nor current process has ";
            } else {
                str2 = "User " + uid + " does not have ";
            }
            sb.append(str2);
            sb.append(uriModeFlagToString(modeFlags));
            sb.append(" permission on ");
            sb.append(uri);
            sb.append(".");
            throw new SecurityException(sb.toString());
        }
    }

    public void enforceUriPermission(Uri uri, int pid, int uid, int modeFlags, String message) {
        enforceForUri(modeFlags, checkUriPermission(uri, pid, uid, modeFlags), false, uid, uri, message);
    }

    public void enforceCallingUriPermission(Uri uri, int modeFlags, String message) {
        enforceForUri(modeFlags, checkCallingUriPermission(uri, modeFlags), false, Binder.getCallingUid(), uri, message);
    }

    public void enforceCallingOrSelfUriPermission(Uri uri, int modeFlags, String message) {
        enforceForUri(modeFlags, checkCallingOrSelfUriPermission(uri, modeFlags), true, Binder.getCallingUid(), uri, message);
    }

    public void enforceUriPermission(Uri uri, String readPermission, String writePermission, int pid, int uid, int modeFlags, String message) {
        enforceForUri(modeFlags, checkUriPermission(uri, readPermission, writePermission, pid, uid, modeFlags), false, uid, uri, message);
    }

    private void warnIfCallingFromSystemProcess() {
        if (Process.myUid() == 1000) {
            Slog.w(TAG, "Calling a method in the system process without a qualified user: " + Debug.getCallers(5));
        }
    }

    private static Resources createResources(IBinder activityToken, LoadedApk pi, String splitName, int displayId, Configuration overrideConfig, CompatibilityInfo compatInfo) {
        try {
            String[] splitResDirs = pi.getSplitPaths(splitName);
            ClassLoader classLoader = pi.getSplitClassLoader(splitName);
            if (!UserHandle.isIsolated(Process.myUid())) {
                float resolutionRatio = -1.0f;
                IApsManager apsmanager = HwFrameworkFactory.getApsManager();
                if (apsmanager != null) {
                    resolutionRatio = apsmanager.getResolution(pi.mPackageName);
                }
                if (0.0f < resolutionRatio && resolutionRatio < 1.0f) {
                    compatInfo = CompatibilityInfo.makeCompatibilityInfo(resolutionRatio);
                }
            }
            ResourcesManager.getInstance().setHwThemeType(pi.getResDir(), pi.getApplicationInfo().hwThemeType);
            return ResourcesManager.getInstance().getResources(activityToken, pi.getResDir(), splitResDirs, pi.getOverlayDirs(), pi.getApplicationInfo().sharedLibraryFiles, displayId, overrideConfig, compatInfo, classLoader);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public Context createApplicationContext(ApplicationInfo application, int flags) throws PackageManager.NameNotFoundException {
        LoadedApk pi = this.mMainThread.getPackageInfo(application, this.mResources.getCompatibilityInfo(), 1073741824 | flags);
        if (pi != null) {
            ContextImpl contextImpl = new ContextImpl(this, this.mMainThread, pi, null, this.mActivityToken, new UserHandle(UserHandle.getUserId(application.uid)), flags, null);
            int displayId = this.mDisplay != null ? this.mDisplay.getDisplayId() : 0;
            contextImpl.setResources(createResources(this.mActivityToken, pi, null, displayId, null, getDisplayAdjustments(displayId).getCompatibilityInfo()));
            if (contextImpl.mResources != null) {
                return contextImpl;
            }
        }
        throw new PackageManager.NameNotFoundException("Application package " + application.packageName + " not found");
    }

    public Context createPackageContext(String packageName, int flags) throws PackageManager.NameNotFoundException {
        return createPackageContextAsUser(packageName, flags, this.mUser);
    }

    public Context createPackageContextAsUser(String packageName, int flags, UserHandle user) throws PackageManager.NameNotFoundException {
        String str = packageName;
        if (str.equals(HwThemeManager.HWT_USER_SYSTEM) || str.equals("android")) {
            ContextImpl contextImpl = new ContextImpl(this, this.mMainThread, this.mPackageInfo, null, this.mActivityToken, user, flags, null);
            return contextImpl;
        }
        LoadedApk pi = this.mMainThread.getPackageInfo(str, this.mResources.getCompatibilityInfo(), flags | 1073741824, user.getIdentifier());
        if (pi != null) {
            ContextImpl c = new ContextImpl(this, this.mMainThread, pi, null, this.mActivityToken, user, flags, null);
            int displayId = this.mDisplay != null ? this.mDisplay.getDisplayId() : 0;
            c.setResources(createResources(this.mActivityToken, pi, null, displayId, null, getDisplayAdjustments(displayId).getCompatibilityInfo()));
            if (c.mResources != null) {
                return c;
            }
        }
        throw new PackageManager.NameNotFoundException("Application package " + str + " not found");
    }

    public Context createContextForSplit(String splitName) throws PackageManager.NameNotFoundException {
        if (!this.mPackageInfo.getApplicationInfo().requestsIsolatedSplitLoading()) {
            return this;
        }
        ClassLoader classLoader = this.mPackageInfo.getSplitClassLoader(splitName);
        String[] paths = this.mPackageInfo.getSplitPaths(splitName);
        ContextImpl contextImpl = new ContextImpl(this, this.mMainThread, this.mPackageInfo, splitName, this.mActivityToken, this.mUser, this.mFlags, classLoader);
        ContextImpl context = contextImpl;
        int displayId = this.mDisplay != null ? this.mDisplay.getDisplayId() : 0;
        ResourcesManager.getInstance().setHwThemeType(this.mPackageInfo.getResDir(), this.mPackageInfo.getApplicationInfo().hwThemeType);
        context.setResources(ResourcesManager.getInstance().getResources(this.mActivityToken, this.mPackageInfo.getResDir(), paths, this.mPackageInfo.getOverlayDirs(), this.mPackageInfo.getApplicationInfo().sharedLibraryFiles, displayId, null, this.mPackageInfo.getCompatibilityInfo(), classLoader));
        return context;
    }

    public Context createConfigurationContext(Configuration overrideConfiguration) {
        if (overrideConfiguration != null) {
            ContextImpl context = new ContextImpl(this, this.mMainThread, this.mPackageInfo, this.mSplitName, this.mActivityToken, this.mUser, this.mFlags, this.mClassLoader);
            int displayId = this.mDisplay != null ? this.mDisplay.getDisplayId() : 0;
            context.setResources(createResources(this.mActivityToken, this.mPackageInfo, this.mSplitName, displayId, overrideConfiguration, getDisplayAdjustments(displayId).getCompatibilityInfo()));
            return context;
        }
        throw new IllegalArgumentException("overrideConfiguration must not be null");
    }

    public Context createDisplayContext(Display display) {
        if (display != null) {
            ContextImpl context = new ContextImpl(this, this.mMainThread, this.mPackageInfo, this.mSplitName, this.mActivityToken, this.mUser, this.mFlags, this.mClassLoader);
            int displayId = display.getDisplayId();
            context.setResources(createResources(this.mActivityToken, this.mPackageInfo, this.mSplitName, displayId, null, getDisplayAdjustments(displayId).getCompatibilityInfo()));
            context.mDisplay = display;
            return context;
        }
        throw new IllegalArgumentException("display must not be null");
    }

    public Context createDeviceProtectedStorageContext() {
        ContextImpl contextImpl = new ContextImpl(this, this.mMainThread, this.mPackageInfo, this.mSplitName, this.mActivityToken, this.mUser, (this.mFlags & -17) | 8, this.mClassLoader);
        return contextImpl;
    }

    public Context createCredentialProtectedStorageContext() {
        ContextImpl contextImpl = new ContextImpl(this, this.mMainThread, this.mPackageInfo, this.mSplitName, this.mActivityToken, this.mUser, (this.mFlags & -9) | 16, this.mClassLoader);
        return contextImpl;
    }

    public boolean isRestricted() {
        return (this.mFlags & 4) != 0;
    }

    public boolean isDeviceProtectedStorage() {
        return (this.mFlags & 8) != 0;
    }

    public boolean isCredentialProtectedStorage() {
        return (this.mFlags & 16) != 0;
    }

    public boolean canLoadUnsafeResources() {
        boolean z = true;
        if (getPackageName().equals(getOpPackageName())) {
            return true;
        }
        if ((this.mFlags & 2) == 0) {
            z = false;
        }
        return z;
    }

    public Display getDisplay() {
        if (this.mDisplay == null) {
            return this.mResourcesManager.getAdjustedDisplay(0, this.mResources);
        }
        return this.mDisplay;
    }

    public int peekHwPCDisplayId() {
        if (this.mDisplay == null) {
            return -1;
        }
        return this.mDisplay.getDisplayId();
    }

    public void updateDisplay(int displayId) {
        this.mDisplay = this.mResourcesManager.getAdjustedDisplay(displayId, this.mResources);
    }

    public DisplayAdjustments getDisplayAdjustments(int displayId) {
        return this.mResources.getDisplayAdjustments();
    }

    public File getDataDir() {
        File res;
        if (this.mPackageInfo != null) {
            if (isCredentialProtectedStorage()) {
                res = this.mPackageInfo.getCredentialProtectedDataDirFile();
            } else if (isDeviceProtectedStorage()) {
                res = this.mPackageInfo.getDeviceProtectedDataDirFile();
            } else {
                res = this.mPackageInfo.getDataDirFile();
            }
            if (res != null) {
                if (!res.exists() && Process.myUid() == 1000) {
                    Log.e(TAG, "Data directory doesn't exist for package " + getPackageName(), new Throwable());
                }
                return res;
            }
            throw new RuntimeException("No data directory found for package " + getPackageName());
        }
        throw new RuntimeException("No package details found for package " + getPackageName());
    }

    public File getDir(String name, int mode) {
        checkMode(mode);
        File file = makeFilename(getDataDir(), "app_" + name);
        if (!file.exists()) {
            file.mkdir();
            setFilePermissionsFromMode(file.getPath(), mode, RadioError.OEM_ERROR_5);
        }
        return file;
    }

    public UserHandle getUser() {
        return this.mUser;
    }

    public int getUserId() {
        return this.mUser.getIdentifier();
    }

    public AutofillManager.AutofillClient getAutofillClient() {
        return this.mAutofillClient;
    }

    public void setAutofillClient(AutofillManager.AutofillClient client) {
        this.mAutofillClient = client;
    }

    public boolean isAutofillCompatibilityEnabled() {
        return this.mIsAutofillCompatEnabled;
    }

    public void setAutofillCompatibilityEnabled(boolean autofillCompatEnabled) {
        this.mIsAutofillCompatEnabled = autofillCompatEnabled;
    }

    static ContextImpl createSystemContext(ActivityThread mainThread) {
        DisplayMetrics metrics;
        LoadedApk packageInfo = new LoadedApk(mainThread);
        ContextImpl context = new ContextImpl(null, mainThread, packageInfo, null, null, null, 0, null);
        context.setResources(packageInfo.getResources());
        if (HwPCUtils.isValidExtDisplayId(mainThread.mDisplayId)) {
            if (mainThread.mOverrideConfig == null || mainThread.mOverrideConfig.equals(Configuration.EMPTY)) {
                metrics = context.mResourcesManager.getDisplayMetrics(mainThread.mDisplayId, DisplayAdjustments.DEFAULT_DISPLAY_ADJUSTMENTS);
            } else {
                metrics = context.mResourcesManager.getDisplayMetrics(mainThread.mDisplayId, new DisplayAdjustments(mainThread.mOverrideConfig));
            }
            context.mResources.updateConfiguration(context.mResourcesManager.getConfiguration(), metrics);
        } else {
            context.mResources.updateConfiguration(context.mResourcesManager.getConfiguration(), context.mResourcesManager.getDisplayMetrics());
        }
        return context;
    }

    static ContextImpl createSystemUiContext(ContextImpl systemContext) {
        LoadedApk packageInfo = systemContext.mPackageInfo;
        ContextImpl contextImpl = new ContextImpl(null, systemContext.mMainThread, packageInfo, null, null, null, 0, null);
        ContextImpl context = contextImpl;
        context.setResources(createResources(null, packageInfo, null, 0, null, packageInfo.getCompatibilityInfo()));
        return context;
    }

    static ContextImpl createAppContext(ActivityThread mainThread, LoadedApk packageInfo) {
        if (packageInfo == null) {
            throw new IllegalArgumentException("packageInfo");
        } else if (HwPCUtils.isValidExtDisplayId(mainThread.mDisplayId) && HwPCUtils.isPcCastMode()) {
            return createHwPcAppContext(mainThread, packageInfo);
        } else {
            ContextImpl contextImpl = new ContextImpl(null, mainThread, packageInfo, null, null, null, 0, null);
            contextImpl.setResources(packageInfo.getResources());
            return contextImpl;
        }
    }

    static ContextImpl createHwPcAppContext(ActivityThread mainThread, LoadedApk packageInfo) {
        CompatibilityInfo compatibilityInfo;
        ContextImpl context = new ContextImpl(null, mainThread, packageInfo, null, null, null, 0, null);
        int displayId = mainThread.mDisplayId;
        if (displayId == 0) {
            compatibilityInfo = packageInfo.getCompatibilityInfo();
        } else {
            compatibilityInfo = CompatibilityInfo.DEFAULT_COMPATIBILITY_INFO;
        }
        CompatibilityInfo compatInfo = compatibilityInfo;
        ResourcesManager resourcesManager = ResourcesManager.getInstance();
        if ("com.ss.android.article.news".equals(packageInfo.getPackageName())) {
            context.setResources(packageInfo.getResources());
            context.mDisplay = resourcesManager.getAdjustedDisplay(displayId, packageInfo.getResources());
        } else {
            Resources resources = resourcesManager.getResources(null, packageInfo.getResDir(), packageInfo.getSplitResDirs(), packageInfo.getOverlayDirs(), packageInfo.getApplicationInfo().sharedLibraryFiles, displayId, mainThread.getOverrideConfig(), compatInfo, packageInfo.getClassLoader());
            context.setResources(resources);
            context.mDisplay = resourcesManager.getAdjustedDisplay(displayId, resources);
        }
        return context;
    }

    static ContextImpl createActivityContext(ActivityThread mainThread, LoadedApk packageInfo, ActivityInfo activityInfo, IBinder activityToken, int displayId, Configuration overrideConfiguration) {
        CompatibilityInfo compatibilityInfo;
        LoadedApk loadedApk = packageInfo;
        ActivityInfo activityInfo2 = activityInfo;
        if (loadedApk != null) {
            String[] splitDirs = packageInfo.getSplitResDirs();
            ClassLoader classLoader = packageInfo.getClassLoader();
            if (packageInfo.getApplicationInfo().requestsIsolatedSplitLoading()) {
                Trace.traceBegin(8192, "SplitDependencies");
                try {
                    classLoader = loadedApk.getSplitClassLoader(activityInfo2.splitName);
                    splitDirs = loadedApk.getSplitPaths(activityInfo2.splitName);
                    Trace.traceEnd(8192);
                } catch (PackageManager.NameNotFoundException e) {
                    throw new RuntimeException(e);
                } catch (Throwable th) {
                    Trace.traceEnd(8192);
                    throw th;
                }
            }
            String[] splitDirs2 = splitDirs;
            ClassLoader classLoader2 = classLoader;
            ContextImpl context = new ContextImpl(null, mainThread, loadedApk, activityInfo2.splitName, activityToken, null, 0, classLoader2);
            int i = displayId;
            int displayId2 = i != -1 ? i : 0;
            boolean mIsValidVRDisplayId = HwFrameworkFactory.getVRSystemServiceManager().isValidVRDisplayId(displayId2);
            if (displayId2 == 0 || mIsValidVRDisplayId) {
                compatibilityInfo = packageInfo.getCompatibilityInfo();
            } else {
                compatibilityInfo = CompatibilityInfo.DEFAULT_COMPATIBILITY_INFO;
            }
            CompatibilityInfo compatInfo = compatibilityInfo;
            ResourcesManager resourcesManager = ResourcesManager.getInstance();
            resourcesManager.setHwThemeType(packageInfo.getResDir(), packageInfo.getApplicationInfo().hwThemeType);
            context.setResources(resourcesManager.createBaseActivityResources(activityToken, packageInfo.getResDir(), splitDirs2, packageInfo.getOverlayDirs(), packageInfo.getApplicationInfo().sharedLibraryFiles, displayId2, overrideConfiguration, compatInfo, classLoader2));
            context.mDisplay = resourcesManager.getAdjustedDisplay(displayId2, context.getResources());
            return context;
        }
        int i2 = displayId;
        throw new IllegalArgumentException("packageInfo");
    }

    private ContextImpl(ContextImpl container, ActivityThread mainThread, LoadedApk packageInfo, String splitName, IBinder activityToken, UserHandle user, int flags, ClassLoader classLoader) {
        if ((flags & 24) == 0) {
            File dataDir = packageInfo.getDataDirFile();
            if (Objects.equals(dataDir, packageInfo.getCredentialProtectedDataDirFile())) {
                flags |= 16;
            } else if (Objects.equals(dataDir, packageInfo.getDeviceProtectedDataDirFile())) {
                flags |= 8;
            }
        }
        this.mMainThread = mainThread;
        this.mActivityToken = activityToken;
        this.mFlags = flags;
        this.mUser = user == null ? Process.myUserHandle() : user;
        this.mPackageInfo = packageInfo;
        this.mSplitName = splitName;
        this.mClassLoader = classLoader;
        this.mResourcesManager = ResourcesManager.getInstance();
        if (container != null) {
            this.mBasePackageName = container.mBasePackageName;
            this.mOpPackageName = container.mOpPackageName;
            setResources(container.mResources);
            this.mDisplay = container.mDisplay;
        } else {
            this.mBasePackageName = packageInfo.mPackageName;
            ApplicationInfo ainfo = packageInfo.getApplicationInfo();
            if (ainfo.uid != 1000 || ainfo.uid == Process.myUid()) {
                this.mOpPackageName = this.mBasePackageName;
            } else {
                this.mOpPackageName = ActivityThread.currentPackageName();
            }
        }
        this.mContentResolver = new ApplicationContentResolver(this, mainThread);
    }

    /* access modifiers changed from: package-private */
    public void setResources(Resources r) {
        if (r instanceof CompatResources) {
            ((CompatResources) r).setContext(this);
        }
        this.mResources = r;
        initResourcePackageName();
    }

    private void initResourcePackageName() {
        if (this.mResources != null) {
            this.mResources.setPackageName(this.mPackageInfo.getPackageName());
            this.mResources.getImpl().getHwResourcesImpl().setPackageName(this.mPackageInfo.getPackageName());
        }
    }

    /* access modifiers changed from: package-private */
    public void installSystemApplicationInfo(ApplicationInfo info, ClassLoader classLoader) {
        this.mPackageInfo.installSystemApplicationInfo(info, classLoader);
    }

    /* access modifiers changed from: package-private */
    public final void scheduleFinalCleanup(String who, String what) {
        this.mMainThread.scheduleContextCleanup(this, who, what);
    }

    /* access modifiers changed from: package-private */
    public final void performFinalCleanup(String who, String what) {
        this.mPackageInfo.removeContextRegistrations(getOuterContext(), who, what);
    }

    /* access modifiers changed from: package-private */
    public final Context getReceiverRestrictedContext() {
        if (this.mReceiverRestrictedContext != null) {
            return this.mReceiverRestrictedContext;
        }
        ReceiverRestrictedContext receiverRestrictedContext = new ReceiverRestrictedContext(getOuterContext());
        this.mReceiverRestrictedContext = receiverRestrictedContext;
        return receiverRestrictedContext;
    }

    /* access modifiers changed from: package-private */
    public final void setOuterContext(Context context) {
        this.mOuterContext = context;
    }

    /* access modifiers changed from: package-private */
    public final Context getOuterContext() {
        return this.mOuterContext;
    }

    public IBinder getActivityToken() {
        return this.mActivityToken;
    }

    private void checkMode(int mode) {
        if (getApplicationInfo().targetSdkVersion < 24) {
            return;
        }
        if ((mode & 1) != 0) {
            throw new SecurityException("MODE_WORLD_READABLE no longer supported");
        } else if ((mode & 2) != 0) {
            throw new SecurityException("MODE_WORLD_WRITEABLE no longer supported");
        }
    }

    static void setFilePermissionsFromMode(String name, int mode, int extraPermissions) {
        int perms = 432 | extraPermissions;
        if ((mode & 1) != 0) {
            perms |= 4;
        }
        if ((mode & 2) != 0) {
            perms |= 2;
        }
        FileUtils.setPermissions(name, perms, -1, -1);
    }

    private File makeFilename(File base, String name) {
        if (name.indexOf(File.separatorChar) < 0) {
            return new File(base, name);
        }
        throw new IllegalArgumentException("File " + name + " contains a path separator");
    }

    private File[] ensureExternalDirsExistOrFilter(File[] dirs) {
        StorageManager sm = (StorageManager) getSystemService(StorageManager.class);
        File[] result = new File[dirs.length];
        for (int i = 0; i < dirs.length; i++) {
            File dir = dirs[i];
            if (!dir.exists() && !dir.mkdirs() && !dir.exists()) {
                try {
                    sm.mkdirs(dir);
                } catch (Exception e) {
                    Log.w(TAG, "Failed to ensure " + dir + ": " + e);
                    dir = null;
                }
            }
            result[i] = dir;
        }
        return result;
    }

    private static boolean checkPrimaryVolumeIsSD() {
        return 1 == SystemProperties.getInt("persist.sys.primarysd", 0);
    }

    private static String getObjectSimplifiedString(Object obj) {
        if (obj == null) {
            return null;
        }
        return obj.getClass().getSimpleName() + "@" + Integer.toHexString(obj.hashCode());
    }

    private static String getClassSimplifiedString(String basePkgName, Class clazz) {
        if (clazz == null) {
            return null;
        }
        String className = clazz.getName();
        if (className.startsWith("com.android.server")) {
            className = className.replaceFirst("com.android.server", "");
        } else if (className.startsWith(basePkgName)) {
            className = className.replaceFirst(basePkgName, "");
        }
        return className;
    }
}
