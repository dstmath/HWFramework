package android.app;

import android.annotation.UnsupportedAppUsage;
import android.app.LoadedApk;
import android.common.HwFrameworkFactory;
import android.content.AutofillOptions;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentCaptureOptions;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.IContentProvider;
import android.content.IIntentReceiver;
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
import android.hsm.HwSystemManager;
import android.net.Uri;
import android.net.wifi.WifiEnterpriseConfig;
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
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.StorageManager;
import android.provider.MediaStore;
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
import com.android.internal.annotations.GuardedBy;
import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.util.Preconditions;
import com.huawei.android.app.HwActivityTaskManager;
import com.huawei.hwaps.IHwApsImpl;
import dalvik.system.BlockGuard;
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
    private static final String XATTR_INODE_CACHE = "user.inode_cache";
    private static final String XATTR_INODE_CODE_CACHE = "user.inode_code_cache";
    @UnsupportedAppUsage
    @GuardedBy({"ContextImpl.class"})
    private static ArrayMap<String, ArrayMap<File, SharedPreferencesImpl>> sSharedPrefsCache;
    private final IBinder mActivityToken;
    private AutofillManager.AutofillClient mAutofillClient = null;
    private AutofillOptions mAutofillOptions;
    @UnsupportedAppUsage
    private final String mBasePackageName;
    @GuardedBy({"mSync"})
    private File mCacheDir;
    @UnsupportedAppUsage
    private ClassLoader mClassLoader;
    @GuardedBy({"mSync"})
    private File mCodeCacheDir;
    private ContentCaptureOptions mContentCaptureOptions = null;
    @UnsupportedAppUsage
    private final ApplicationContentResolver mContentResolver;
    @GuardedBy({"mSync"})
    private File mDatabasesDir;
    private Display mDisplay;
    @GuardedBy({"mSync"})
    private File mFilesDir;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    private final int mFlags;
    @UnsupportedAppUsage
    final ActivityThread mMainThread;
    @GuardedBy({"mSync"})
    private File mNoBackupFilesDir;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    private final String mOpPackageName;
    @UnsupportedAppUsage
    private Context mOuterContext = this;
    @UnsupportedAppUsage
    final LoadedApk mPackageInfo;
    @UnsupportedAppUsage
    private PackageManager mPackageManager;
    @UnsupportedAppUsage
    @GuardedBy({"mSync"})
    private File mPreferencesDir;
    private Context mReceiverRestrictedContext = null;
    @UnsupportedAppUsage
    private Resources mResources;
    private final ResourcesManager mResourcesManager;
    @UnsupportedAppUsage
    final Object[] mServiceCache = SystemServiceRegistry.createServiceCache();
    final int[] mServiceInitializationStateArray = new int[this.mServiceCache.length];
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    @GuardedBy({"ContextImpl.class"})
    private ArrayMap<String, File> mSharedPrefsPaths;
    private String mSplitName = null;
    private final Object mSync = new Object();
    @UnsupportedAppUsage
    private Resources.Theme mTheme = null;
    @UnsupportedAppUsage
    private int mThemeResource = 0;
    private final UserHandle mUser;

    @Retention(RetentionPolicy.SOURCE)
    @interface ServiceInitializationState {
    }

    @UnsupportedAppUsage
    static ContextImpl getImpl(Context context) {
        Context nextContext;
        while ((context instanceof ContextWrapper) && (nextContext = ((ContextWrapper) context).getBaseContext()) != null) {
            context = nextContext;
        }
        return (ContextImpl) context;
    }

    @Override // android.content.Context
    public AssetManager getAssets() {
        return getResources().getAssets();
    }

    @Override // android.content.Context
    public Resources getResources() {
        return this.mResources;
    }

    @Override // android.content.Context
    public PackageManager getPackageManager() {
        PackageManager packageManager = this.mPackageManager;
        if (packageManager != null) {
            return packageManager;
        }
        IPackageManager pm = ActivityThread.getPackageManager();
        if (pm == null) {
            return null;
        }
        ApplicationPackageManager applicationPackageManager = new ApplicationPackageManager(this, pm);
        this.mPackageManager = applicationPackageManager;
        return applicationPackageManager;
    }

    @Override // android.content.Context
    public ContentResolver getContentResolver() {
        return this.mContentResolver;
    }

    @Override // android.content.Context
    public Looper getMainLooper() {
        return this.mMainThread.getLooper();
    }

    @Override // android.content.Context
    public Executor getMainExecutor() {
        return this.mMainThread.getExecutor();
    }

    @Override // android.content.Context
    public Context getApplicationContext() {
        LoadedApk loadedApk = this.mPackageInfo;
        return loadedApk != null ? loadedApk.getApplication() : this.mMainThread.getApplication();
    }

    @Override // android.content.Context
    public void setTheme(int resId) {
        synchronized (this.mSync) {
            if (this.mThemeResource != resId) {
                this.mThemeResource = resId;
                initializeTheme();
            }
        }
    }

    @Override // android.content.Context
    public int getThemeResId() {
        int i;
        synchronized (this.mSync) {
            i = this.mThemeResource;
        }
        return i;
    }

    @Override // android.content.Context
    public Resources.Theme getTheme() {
        synchronized (this.mSync) {
            if (this.mTheme != null) {
                return this.mTheme;
            }
            this.mThemeResource = Resources.selectDefaultTheme(this.mThemeResource, getOuterContext().getApplicationInfo().targetSdkVersion);
            initializeTheme();
            return this.mTheme;
        }
    }

    private void initializeTheme() {
        if (this.mTheme == null) {
            this.mTheme = this.mResources.newTheme();
        }
        this.mTheme.applyStyle(this.mThemeResource, true);
    }

    @Override // android.content.Context
    public ClassLoader getClassLoader() {
        ClassLoader classLoader = this.mClassLoader;
        if (classLoader != null) {
            return classLoader;
        }
        LoadedApk loadedApk = this.mPackageInfo;
        return loadedApk != null ? loadedApk.getClassLoader() : ClassLoader.getSystemClassLoader();
    }

    @Override // android.content.Context
    public String getPackageName() {
        LoadedApk loadedApk = this.mPackageInfo;
        if (loadedApk != null) {
            return loadedApk.getPackageName();
        }
        return "android";
    }

    @Override // android.content.Context
    public String getBasePackageName() {
        String str = this.mBasePackageName;
        return str != null ? str : getPackageName();
    }

    @Override // android.content.Context
    public String getOpPackageName() {
        String str = this.mOpPackageName;
        return str != null ? str : getBasePackageName();
    }

    @Override // android.content.Context
    public ApplicationInfo getApplicationInfo() {
        LoadedApk loadedApk = this.mPackageInfo;
        if (loadedApk != null) {
            return loadedApk.getApplicationInfo();
        }
        throw new RuntimeException("Not supported in system context");
    }

    @Override // android.content.Context
    public String getPackageResourcePath() {
        LoadedApk loadedApk = this.mPackageInfo;
        if (loadedApk != null) {
            return loadedApk.getResDir();
        }
        throw new RuntimeException("Not supported in system context");
    }

    @Override // android.content.Context
    public String getPackageCodePath() {
        LoadedApk loadedApk = this.mPackageInfo;
        if (loadedApk != null) {
            return loadedApk.getAppDir();
        }
        throw new RuntimeException("Not supported in system context");
    }

    @Override // android.content.Context
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

    @Override // android.content.Context
    public SharedPreferences getSharedPreferences(File file, int mode) {
        SharedPreferencesImpl sp;
        synchronized (ContextImpl.class) {
            ArrayMap<File, SharedPreferencesImpl> cache = getSharedPreferencesCacheLocked();
            sp = cache.get(file);
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
        if ((mode & 4) != 0 || getApplicationInfo().targetSdkVersion < 11) {
            sp.startReloadIfChangedUnexpectedly();
        }
        return sp;
    }

    @GuardedBy({"ContextImpl.class"})
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

    @Override // android.content.Context
    public void reloadSharedPreferences() {
        ArrayList<SharedPreferencesImpl> spImpls = new ArrayList<>();
        synchronized (ContextImpl.class) {
            ArrayMap<File, SharedPreferencesImpl> cache = getSharedPreferencesCacheLocked();
            for (int i = 0; i < cache.size(); i++) {
                SharedPreferencesImpl sp = cache.valueAt(i);
                if (sp != null) {
                    spImpls.add(sp);
                }
            }
        }
        for (int i2 = 0; i2 < spImpls.size(); i2++) {
            spImpls.get(i2).startReloadIfChangedUnexpectedly();
        }
    }

    private static int moveFiles(File sourceDir, File targetDir, final String prefix) {
        File[] sourceFiles = FileUtils.listFilesOrEmpty(sourceDir, new FilenameFilter() {
            /* class android.app.ContextImpl.AnonymousClass1 */

            @Override // java.io.FilenameFilter
            public boolean accept(File dir, String name) {
                return name.startsWith(prefix);
            }
        });
        int res = 0;
        for (File sourceFile : sourceFiles) {
            File targetFile = new File(targetDir, sourceFile.getName());
            Log.d(TAG, "Migrating " + sourceFile + " to " + targetFile);
            try {
                FileUtils.copyFileOrThrow(sourceFile, targetFile);
                FileUtils.copyPermissions(sourceFile, targetFile);
                if (sourceFile.delete()) {
                    if (res != -1) {
                        res++;
                    }
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

    @Override // android.content.Context
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

    @Override // android.content.Context
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

    @UnsupportedAppUsage
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

    @Override // android.content.Context
    public FileInputStream openFileInput(String name) throws FileNotFoundException {
        return new FileInputStream(makeFilename(getFilesDir(), name));
    }

    @Override // android.content.Context
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
            FileUtils.setPermissions(parent.getPath(), 505, -1, -1);
            FileOutputStream fos2 = new FileOutputStream(f, append);
            setFilePermissionsFromMode(f.getPath(), mode, 0);
            return fos2;
        }
    }

    @Override // android.content.Context
    public boolean deleteFile(String name) {
        return makeFilename(getFilesDir(), name).delete();
    }

    private static File ensurePrivateDirExists(File file) {
        return ensurePrivateDirExists(file, 505, -1, null);
    }

    private static File ensurePrivateCacheDirExists(File file, String xattr) {
        return ensurePrivateDirExists(file, MetricsProto.MetricsEvent.FIELD_PROCESS_RECORD_PROCESS_NAME, UserHandle.getCacheAppGid(Process.myUid()), xattr);
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

    @Override // android.content.Context
    public File getFilesDir() {
        File ensurePrivateDirExists;
        synchronized (this.mSync) {
            if (this.mFilesDir == null) {
                this.mFilesDir = new File(getDataDir(), MediaStore.Files.TABLE);
            }
            ensurePrivateDirExists = ensurePrivateDirExists(this.mFilesDir);
        }
        return ensurePrivateDirExists;
    }

    @Override // android.content.Context
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

    @Override // android.content.Context
    public File getExternalFilesDir(String type) {
        File[] dirs = getExternalFilesDirs(type);
        if (!IS_SWITCH_SD_ENABLED || !checkPrimaryVolumeIsSD()) {
            if (dirs == null || dirs.length <= 0) {
                return null;
            }
            return dirs[0];
        } else if (dirs == null || dirs.length <= 0) {
            return null;
        } else {
            if (dirs.length == 1) {
                return dirs[0];
            }
            return dirs[1];
        }
    }

    @Override // android.content.Context
    public File[] getExternalFilesDirs(String type) {
        File[] ensureExternalDirsExistOrFilter;
        synchronized (this.mSync) {
            File[] dirs = Environment.buildExternalStorageAppFilesDirs(getPackageName());
            if (type != null) {
                dirs = Environment.buildPaths(dirs, type);
            }
            ensureExternalDirsExistOrFilter = ensureExternalDirsExistOrFilter(dirs);
        }
        return ensureExternalDirsExistOrFilter;
    }

    @Override // android.content.Context
    public File getObbDir() {
        File[] dirs = getObbDirs();
        if (!IS_SWITCH_SD_ENABLED || !checkPrimaryVolumeIsSD()) {
            if (dirs == null || dirs.length <= 0) {
                return null;
            }
            return dirs[0];
        } else if (dirs == null || dirs.length <= 0) {
            return null;
        } else {
            if (dirs.length == 1) {
                return dirs[0];
            }
            return dirs[1];
        }
    }

    @Override // android.content.Context
    public File[] getObbDirs() {
        File[] ensureExternalDirsExistOrFilter;
        synchronized (this.mSync) {
            ensureExternalDirsExistOrFilter = ensureExternalDirsExistOrFilter(Environment.buildExternalStorageAppObbDirs(getPackageName()));
        }
        return ensureExternalDirsExistOrFilter;
    }

    @Override // android.content.Context
    public File getCacheDir() {
        File ensurePrivateCacheDirExists;
        synchronized (this.mSync) {
            if (this.mCacheDir == null) {
                this.mCacheDir = new File(getDataDir(), "cache");
            }
            ensurePrivateCacheDirExists = ensurePrivateCacheDirExists(this.mCacheDir, XATTR_INODE_CACHE);
        }
        return ensurePrivateCacheDirExists;
    }

    @Override // android.content.Context
    public File getCodeCacheDir() {
        File ensurePrivateCacheDirExists;
        synchronized (this.mSync) {
            if (this.mCodeCacheDir == null) {
                this.mCodeCacheDir = new File(getDataDir(), "code_cache");
            }
            ensurePrivateCacheDirExists = ensurePrivateCacheDirExists(this.mCodeCacheDir, XATTR_INODE_CODE_CACHE);
        }
        return ensurePrivateCacheDirExists;
    }

    @Override // android.content.Context
    public File getExternalCacheDir() {
        File[] dirs = getExternalCacheDirs();
        if (!IS_SWITCH_SD_ENABLED || !checkPrimaryVolumeIsSD()) {
            if (dirs == null || dirs.length <= 0) {
                return null;
            }
            return dirs[0];
        } else if (dirs == null || dirs.length <= 0) {
            return null;
        } else {
            if (dirs.length == 1) {
                return dirs[0];
            }
            return dirs[1];
        }
    }

    @Override // android.content.Context
    public File[] getExternalCacheDirs() {
        File[] ensureExternalDirsExistOrFilter;
        synchronized (this.mSync) {
            ensureExternalDirsExistOrFilter = ensureExternalDirsExistOrFilter(Environment.buildExternalStorageAppCacheDirs(getPackageName()));
        }
        return ensureExternalDirsExistOrFilter;
    }

    @Override // android.content.Context
    public File[] getExternalMediaDirs() {
        File[] ensureExternalDirsExistOrFilter;
        synchronized (this.mSync) {
            ensureExternalDirsExistOrFilter = ensureExternalDirsExistOrFilter(Environment.buildExternalStorageAppMediaDirs(getPackageName()));
        }
        return ensureExternalDirsExistOrFilter;
    }

    @Override // android.content.Context
    public File getPreloadsFileCache() {
        return Environment.getDataPreloadsFileCacheDirectory(getPackageName());
    }

    @Override // android.content.Context
    public File getFileStreamPath(String name) {
        return makeFilename(getFilesDir(), name);
    }

    @Override // android.content.Context
    public File getSharedPreferencesPath(String name) {
        File preferencesDir = getPreferencesDir();
        return makeFilename(preferencesDir, name + ".xml");
    }

    @Override // android.content.Context
    public String[] fileList() {
        return FileUtils.listOrEmpty(getFilesDir());
    }

    @Override // android.content.Context
    public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory) {
        return openOrCreateDatabase(name, mode, factory, null);
    }

    @Override // android.content.Context
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

    @Override // android.content.Context
    public boolean moveDatabaseFrom(Context sourceContext, String name) {
        boolean z;
        synchronized (ContextImpl.class) {
            File source = sourceContext.getDatabasePath(name);
            z = moveFiles(source.getParentFile(), getDatabasePath(name).getParentFile(), source.getName()) != -1;
        }
        return z;
    }

    @Override // android.content.Context
    public boolean deleteDatabase(String name) {
        try {
            return SQLiteDatabase.deleteDatabase(getDatabasePath(name));
        } catch (Exception e) {
            return false;
        }
    }

    @Override // android.content.Context
    public File getDatabasePath(String name) {
        if (name.charAt(0) != File.separatorChar) {
            return makeFilename(getDatabasesDir(), name);
        }
        File dir = new File(name.substring(0, name.lastIndexOf(File.separatorChar)));
        File f = new File(dir, name.substring(name.lastIndexOf(File.separatorChar)));
        if (dir.isDirectory() || !dir.mkdir()) {
            return f;
        }
        FileUtils.setPermissions(dir.getPath(), 505, -1, -1);
        return f;
    }

    @Override // android.content.Context
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
                }
            }
            ensurePrivateDirExists = ensurePrivateDirExists(this.mDatabasesDir);
        }
        return ensurePrivateDirExists;
    }

    @Override // android.content.Context
    @Deprecated
    public Drawable getWallpaper() {
        return getWallpaperManager().getDrawable();
    }

    @Override // android.content.Context
    @Deprecated
    public Drawable peekWallpaper() {
        return getWallpaperManager().peekDrawable();
    }

    @Override // android.content.Context
    @Deprecated
    public int getWallpaperDesiredMinimumWidth() {
        return getWallpaperManager().getDesiredMinimumWidth();
    }

    @Override // android.content.Context
    @Deprecated
    public int getWallpaperDesiredMinimumHeight() {
        return getWallpaperManager().getDesiredMinimumHeight();
    }

    @Override // android.content.Context
    @Deprecated
    public void setWallpaper(Bitmap bitmap) throws IOException {
        getWallpaperManager().setBitmap(bitmap);
    }

    @Override // android.content.Context
    @Deprecated
    public void setWallpaper(InputStream data) throws IOException {
        getWallpaperManager().setStream(data);
    }

    @Override // android.content.Context
    @Deprecated
    public void clearWallpaper() throws IOException {
        getWallpaperManager().clear();
    }

    private WallpaperManager getWallpaperManager() {
        return (WallpaperManager) getSystemService(WallpaperManager.class);
    }

    @Override // android.content.Context
    public void startActivity(Intent intent) {
        warnIfCallingFromSystemProcess();
        startActivity(intent, null);
    }

    @Override // android.content.Context
    public void startActivityAsUser(Intent intent, UserHandle user) {
        startActivityAsUser(intent, null, user);
    }

    @Override // android.content.Context
    public void startActivity(Intent intent, Bundle options) {
        if (!HwSystemManager.canStartActivity(getApplicationContext(), intent)) {
            Log.i(TAG, "this app not allowed to start activity:" + intent);
            return;
        }
        warnIfCallingFromSystemProcess();
        int targetSdkVersion = getApplicationInfo().targetSdkVersion;
        if ((intent.getFlags() & 268435456) != 0 || ((targetSdkVersion >= 24 && targetSdkVersion < 28) || !(options == null || ActivityOptions.fromBundle(options).getLaunchTaskId() == -1))) {
            this.mMainThread.getInstrumentation().execStartActivity(getOuterContext(), this.mMainThread.getApplicationThread(), (IBinder) null, (Activity) null, intent, -1, HwActivityTaskManager.hookStartActivityOptions(this, HwPCUtils.hookStartActivityOptions(this, options)));
            return;
        }
        throw new AndroidRuntimeException("Calling startActivity() from outside of an Activity  context requires the FLAG_ACTIVITY_NEW_TASK flag. Is this really what you want?");
    }

    @Override // android.content.Context
    public void startActivityAsUser(Intent intent, Bundle options, UserHandle user) {
        RemoteException e;
        try {
            try {
                try {
                    Jlog.printStartActivityInfo(intent, SystemClock.uptimeMillis(), ActivityTaskManager.getService().startActivityAsUser(this.mMainThread.getApplicationThread(), getBasePackageName(), intent, intent.resolveTypeIfNeeded(getContentResolver()), null, null, 0, 268435456, null, HwActivityTaskManager.hookStartActivityOptions(this, HwPCUtils.hookStartActivityOptions(this, options)), user.getIdentifier()));
                } catch (RemoteException e2) {
                    e = e2;
                }
            } catch (RemoteException e3) {
                e = e3;
                throw e.rethrowFromSystemServer();
            }
        } catch (RemoteException e4) {
            e = e4;
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.Context
    public void startActivities(Intent[] intents) {
        warnIfCallingFromSystemProcess();
        startActivities(intents, null);
    }

    @Override // android.content.Context
    public int startActivitiesAsUser(Intent[] intents, Bundle options, UserHandle userHandle) {
        if ((intents[0].getFlags() & 268435456) != 0) {
            return this.mMainThread.getInstrumentation().execStartActivitiesAsUser(getOuterContext(), this.mMainThread.getApplicationThread(), null, null, intents, HwActivityTaskManager.hookStartActivityOptions(this, HwPCUtils.hookStartActivityOptions(this, options)), userHandle.getIdentifier());
        }
        throw new AndroidRuntimeException("Calling startActivities() from outside of an Activity  context requires the FLAG_ACTIVITY_NEW_TASK flag on first Intent. Is this really what you want?");
    }

    @Override // android.content.Context
    public void startActivities(Intent[] intents, Bundle options) {
        warnIfCallingFromSystemProcess();
        if ((intents[0].getFlags() & 268435456) != 0) {
            this.mMainThread.getInstrumentation().execStartActivities(getOuterContext(), this.mMainThread.getApplicationThread(), null, null, intents, HwActivityTaskManager.hookStartActivityOptions(this, HwPCUtils.hookStartActivityOptions(this, options)));
            return;
        }
        throw new AndroidRuntimeException("Calling startActivities() from outside of an Activity  context requires the FLAG_ACTIVITY_NEW_TASK flag on first Intent. Is this really what you want?");
    }

    @Override // android.content.Context
    public void startIntentSender(IntentSender intent, Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags) throws IntentSender.SendIntentException {
        startIntentSender(intent, fillInIntent, flagsMask, flagsValues, extraFlags, null);
    }

    @Override // android.content.Context
    public void startIntentSender(IntentSender intent, Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags, Bundle options) throws IntentSender.SendIntentException {
        String resolvedType = null;
        if (fillInIntent != null) {
            try {
                fillInIntent.migrateExtraStreamToClipData();
                fillInIntent.prepareToLeaveProcess(this);
                resolvedType = fillInIntent.resolveTypeIfNeeded(getContentResolver());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
        int result = ActivityTaskManager.getService().startActivityIntentSender(this.mMainThread.getApplicationThread(), intent != null ? intent.getTarget() : null, intent != null ? intent.getWhitelistToken() : null, fillInIntent, resolvedType, null, null, 0, flagsMask, flagsValues, options);
        if (result != -96) {
            Instrumentation.checkStartActivityResult(result, null);
            return;
        }
        throw new IntentSender.SendIntentException();
    }

    @Override // android.content.Context
    public void sendBroadcast(Intent intent) {
        warnIfCallingFromSystemProcess();
        String resolvedType = intent.resolveTypeIfNeeded(getContentResolver());
        try {
            intent.prepareToLeaveProcess(this);
            ActivityManager.getService().broadcastIntent(this.mMainThread.getApplicationThread(), intent, resolvedType, null, -1, null, null, null, -1, null, false, false, getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.Context
    public void sendBroadcast(Intent intent, String receiverPermission) {
        warnIfCallingFromSystemProcess();
        String resolvedType = intent.resolveTypeIfNeeded(getContentResolver());
        String[] receiverPermissions = receiverPermission == null ? null : new String[]{receiverPermission};
        try {
            intent.prepareToLeaveProcess(this);
            ActivityManager.getService().broadcastIntent(this.mMainThread.getApplicationThread(), intent, resolvedType, null, -1, null, null, receiverPermissions, -1, null, false, false, getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.Context
    public void sendBroadcastMultiplePermissions(Intent intent, String[] receiverPermissions) {
        warnIfCallingFromSystemProcess();
        String resolvedType = intent.resolveTypeIfNeeded(getContentResolver());
        try {
            intent.prepareToLeaveProcess(this);
            ActivityManager.getService().broadcastIntent(this.mMainThread.getApplicationThread(), intent, resolvedType, null, -1, null, null, receiverPermissions, -1, null, false, false, getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.Context
    public void sendBroadcastAsUserMultiplePermissions(Intent intent, UserHandle user, String[] receiverPermissions) {
        String resolvedType = intent.resolveTypeIfNeeded(getContentResolver());
        try {
            intent.prepareToLeaveProcess(this);
            ActivityManager.getService().broadcastIntent(this.mMainThread.getApplicationThread(), intent, resolvedType, null, -1, null, null, receiverPermissions, -1, null, false, false, user.getIdentifier());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.Context
    public void sendBroadcast(Intent intent, String receiverPermission, Bundle options) {
        warnIfCallingFromSystemProcess();
        String resolvedType = intent.resolveTypeIfNeeded(getContentResolver());
        String[] receiverPermissions = receiverPermission == null ? null : new String[]{receiverPermission};
        try {
            intent.prepareToLeaveProcess(this);
            ActivityManager.getService().broadcastIntent(this.mMainThread.getApplicationThread(), intent, resolvedType, null, -1, null, null, receiverPermissions, -1, options, false, false, getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.Context
    public void sendBroadcast(Intent intent, String receiverPermission, int appOp) {
        warnIfCallingFromSystemProcess();
        String resolvedType = intent.resolveTypeIfNeeded(getContentResolver());
        String[] receiverPermissions = receiverPermission == null ? null : new String[]{receiverPermission};
        try {
            intent.prepareToLeaveProcess(this);
            ActivityManager.getService().broadcastIntent(this.mMainThread.getApplicationThread(), intent, resolvedType, null, -1, null, null, receiverPermissions, appOp, null, false, false, getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.Context
    public void sendOrderedBroadcast(Intent intent, String receiverPermission) {
        warnIfCallingFromSystemProcess();
        String resolvedType = intent.resolveTypeIfNeeded(getContentResolver());
        String[] receiverPermissions = receiverPermission == null ? null : new String[]{receiverPermission};
        try {
            intent.prepareToLeaveProcess(this);
            ActivityManager.getService().broadcastIntent(this.mMainThread.getApplicationThread(), intent, resolvedType, null, -1, null, null, receiverPermissions, -1, null, true, false, getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.Context
    public void sendOrderedBroadcast(Intent intent, String receiverPermission, BroadcastReceiver resultReceiver, Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {
        sendOrderedBroadcast(intent, receiverPermission, -1, resultReceiver, scheduler, initialCode, initialData, initialExtras, null);
    }

    @Override // android.content.Context
    public void sendOrderedBroadcast(Intent intent, String receiverPermission, Bundle options, BroadcastReceiver resultReceiver, Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {
        sendOrderedBroadcast(intent, receiverPermission, -1, resultReceiver, scheduler, initialCode, initialData, initialExtras, options);
    }

    @Override // android.content.Context
    public void sendOrderedBroadcast(Intent intent, String receiverPermission, int appOp, BroadcastReceiver resultReceiver, Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {
        sendOrderedBroadcast(intent, receiverPermission, appOp, resultReceiver, scheduler, initialCode, initialData, initialExtras, null);
    }

    /* access modifiers changed from: package-private */
    public void sendOrderedBroadcast(Intent intent, String receiverPermission, int appOp, BroadcastReceiver resultReceiver, Handler scheduler, int initialCode, String initialData, Bundle initialExtras, Bundle options) {
        IIntentReceiver rd;
        Handler scheduler2;
        Handler scheduler3;
        warnIfCallingFromSystemProcess();
        if (resultReceiver == null) {
            rd = null;
        } else if (this.mPackageInfo != null) {
            if (scheduler == null) {
                scheduler3 = this.mMainThread.getHandler();
            } else {
                scheduler3 = scheduler;
            }
            rd = this.mPackageInfo.getReceiverDispatcher(resultReceiver, getOuterContext(), scheduler3, this.mMainThread.getInstrumentation(), false);
        } else {
            if (scheduler == null) {
                scheduler2 = this.mMainThread.getHandler();
            } else {
                scheduler2 = scheduler;
            }
            rd = new LoadedApk.ReceiverDispatcher(resultReceiver, getOuterContext(), scheduler2, null, false).getIIntentReceiver();
        }
        String resolvedType = intent.resolveTypeIfNeeded(getContentResolver());
        String[] receiverPermissions = receiverPermission == null ? null : new String[]{receiverPermission};
        try {
            intent.prepareToLeaveProcess(this);
            ActivityManager.getService().broadcastIntent(this.mMainThread.getApplicationThread(), intent, resolvedType, rd, initialCode, initialData, initialExtras, receiverPermissions, appOp, options, true, false, getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.Context
    public void sendBroadcastAsUser(Intent intent, UserHandle user) {
        String resolvedType = intent.resolveTypeIfNeeded(getContentResolver());
        try {
            intent.prepareToLeaveProcess(this);
            ActivityManager.getService().broadcastIntent(this.mMainThread.getApplicationThread(), intent, resolvedType, null, -1, null, null, null, -1, null, false, false, user.getIdentifier());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.Context
    public void sendBroadcastAsUser(Intent intent, UserHandle user, String receiverPermission) {
        sendBroadcastAsUser(intent, user, receiverPermission, -1);
    }

    @Override // android.content.Context
    public void sendBroadcastAsUser(Intent intent, UserHandle user, String receiverPermission, Bundle options) {
        String resolvedType = intent.resolveTypeIfNeeded(getContentResolver());
        String[] receiverPermissions = receiverPermission == null ? null : new String[]{receiverPermission};
        try {
            intent.prepareToLeaveProcess(this);
            ActivityManager.getService().broadcastIntent(this.mMainThread.getApplicationThread(), intent, resolvedType, null, -1, null, null, receiverPermissions, -1, options, false, false, user.getIdentifier());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.Context
    public void sendBroadcastAsUser(Intent intent, UserHandle user, String receiverPermission, int appOp) {
        String resolvedType = intent.resolveTypeIfNeeded(getContentResolver());
        String[] receiverPermissions = receiverPermission == null ? null : new String[]{receiverPermission};
        try {
            intent.prepareToLeaveProcess(this);
            ActivityManager.getService().broadcastIntent(this.mMainThread.getApplicationThread(), intent, resolvedType, null, -1, null, null, receiverPermissions, appOp, null, false, false, user.getIdentifier());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.Context
    public void sendOrderedBroadcastAsUser(Intent intent, UserHandle user, String receiverPermission, BroadcastReceiver resultReceiver, Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {
        sendOrderedBroadcastAsUser(intent, user, receiverPermission, -1, null, resultReceiver, scheduler, initialCode, initialData, initialExtras);
    }

    @Override // android.content.Context
    public void sendOrderedBroadcastAsUser(Intent intent, UserHandle user, String receiverPermission, int appOp, BroadcastReceiver resultReceiver, Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {
        sendOrderedBroadcastAsUser(intent, user, receiverPermission, appOp, null, resultReceiver, scheduler, initialCode, initialData, initialExtras);
    }

    @Override // android.content.Context
    public void sendOrderedBroadcastAsUser(Intent intent, UserHandle user, String receiverPermission, int appOp, Bundle options, BroadcastReceiver resultReceiver, Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {
        IIntentReceiver rd;
        Handler scheduler2;
        Handler scheduler3;
        if (resultReceiver == null) {
            rd = null;
        } else if (this.mPackageInfo != null) {
            if (scheduler == null) {
                scheduler3 = this.mMainThread.getHandler();
            } else {
                scheduler3 = scheduler;
            }
            rd = this.mPackageInfo.getReceiverDispatcher(resultReceiver, getOuterContext(), scheduler3, this.mMainThread.getInstrumentation(), false);
        } else {
            if (scheduler == null) {
                scheduler2 = this.mMainThread.getHandler();
            } else {
                scheduler2 = scheduler;
            }
            rd = new LoadedApk.ReceiverDispatcher(resultReceiver, getOuterContext(), scheduler2, null, false).getIIntentReceiver();
        }
        String resolvedType = intent.resolveTypeIfNeeded(getContentResolver());
        String[] receiverPermissions = receiverPermission == null ? null : new String[]{receiverPermission};
        try {
            intent.prepareToLeaveProcess(this);
            ActivityManager.getService().broadcastIntent(this.mMainThread.getApplicationThread(), intent, resolvedType, rd, initialCode, initialData, initialExtras, receiverPermissions, appOp, options, true, false, user.getIdentifier());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.Context
    @Deprecated
    public void sendStickyBroadcast(Intent intent) {
        warnIfCallingFromSystemProcess();
        String resolvedType = intent.resolveTypeIfNeeded(getContentResolver());
        try {
            intent.prepareToLeaveProcess(this);
            ActivityManager.getService().broadcastIntent(this.mMainThread.getApplicationThread(), intent, resolvedType, null, -1, null, null, null, -1, null, false, true, getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.Context
    @Deprecated
    public void sendStickyOrderedBroadcast(Intent intent, BroadcastReceiver resultReceiver, Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {
        IIntentReceiver rd;
        Handler scheduler2;
        Handler scheduler3;
        warnIfCallingFromSystemProcess();
        if (resultReceiver == null) {
            rd = null;
        } else if (this.mPackageInfo != null) {
            if (scheduler == null) {
                scheduler3 = this.mMainThread.getHandler();
            } else {
                scheduler3 = scheduler;
            }
            rd = this.mPackageInfo.getReceiverDispatcher(resultReceiver, getOuterContext(), scheduler3, this.mMainThread.getInstrumentation(), false);
        } else {
            if (scheduler == null) {
                scheduler2 = this.mMainThread.getHandler();
            } else {
                scheduler2 = scheduler;
            }
            rd = new LoadedApk.ReceiverDispatcher(resultReceiver, getOuterContext(), scheduler2, null, false).getIIntentReceiver();
        }
        String resolvedType = intent.resolveTypeIfNeeded(getContentResolver());
        try {
            intent.prepareToLeaveProcess(this);
            ActivityManager.getService().broadcastIntent(this.mMainThread.getApplicationThread(), intent, resolvedType, rd, initialCode, initialData, initialExtras, null, -1, null, true, true, getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.Context
    @Deprecated
    public void removeStickyBroadcast(Intent intent) {
        String resolvedType = intent.resolveTypeIfNeeded(getContentResolver());
        if (resolvedType != null) {
            intent = new Intent(intent);
            intent.setDataAndType(intent.getData(), resolvedType);
        }
        try {
            intent.prepareToLeaveProcess(this);
            ActivityManager.getService().unbroadcastIntent(this.mMainThread.getApplicationThread(), intent, getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.Context
    @Deprecated
    public void sendStickyBroadcastAsUser(Intent intent, UserHandle user) {
        String resolvedType = intent.resolveTypeIfNeeded(getContentResolver());
        try {
            intent.prepareToLeaveProcess(this);
            ActivityManager.getService().broadcastIntent(this.mMainThread.getApplicationThread(), intent, resolvedType, null, -1, null, null, null, -1, null, false, true, user.getIdentifier());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.Context
    @Deprecated
    public void sendStickyBroadcastAsUser(Intent intent, UserHandle user, Bundle options) {
        String resolvedType = intent.resolveTypeIfNeeded(getContentResolver());
        try {
            intent.prepareToLeaveProcess(this);
            ActivityManager.getService().broadcastIntent(this.mMainThread.getApplicationThread(), intent, resolvedType, null, -1, null, null, null, -1, options, false, true, user.getIdentifier());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.Context
    @Deprecated
    public void sendStickyOrderedBroadcastAsUser(Intent intent, UserHandle user, BroadcastReceiver resultReceiver, Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {
        IIntentReceiver rd;
        Handler scheduler2;
        Handler scheduler3;
        if (resultReceiver == null) {
            rd = null;
        } else if (this.mPackageInfo != null) {
            if (scheduler == null) {
                scheduler3 = this.mMainThread.getHandler();
            } else {
                scheduler3 = scheduler;
            }
            rd = this.mPackageInfo.getReceiverDispatcher(resultReceiver, getOuterContext(), scheduler3, this.mMainThread.getInstrumentation(), false);
        } else {
            if (scheduler == null) {
                scheduler2 = this.mMainThread.getHandler();
            } else {
                scheduler2 = scheduler;
            }
            rd = new LoadedApk.ReceiverDispatcher(resultReceiver, getOuterContext(), scheduler2, null, false).getIIntentReceiver();
        }
        String resolvedType = intent.resolveTypeIfNeeded(getContentResolver());
        try {
            intent.prepareToLeaveProcess(this);
            ActivityManager.getService().broadcastIntent(this.mMainThread.getApplicationThread(), intent, resolvedType, rd, initialCode, initialData, initialExtras, null, -1, null, true, true, user.getIdentifier());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.Context
    @Deprecated
    public void removeStickyBroadcastAsUser(Intent intent, UserHandle user) {
        String resolvedType = intent.resolveTypeIfNeeded(getContentResolver());
        if (resolvedType != null) {
            intent = new Intent(intent);
            intent.setDataAndType(intent.getData(), resolvedType);
        }
        try {
            intent.prepareToLeaveProcess(this);
            ActivityManager.getService().unbroadcastIntent(this.mMainThread.getApplicationThread(), intent, user.getIdentifier());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.Context
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        return registerReceiver(receiver, filter, null, null);
    }

    @Override // android.content.Context
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter, int flags) {
        return registerReceiver(receiver, filter, null, null, flags);
    }

    @Override // android.content.Context
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter, String broadcastPermission, Handler scheduler) {
        return registerReceiverInternal(receiver, getUserId(), filter, broadcastPermission, scheduler, getOuterContext(), 0);
    }

    @Override // android.content.Context
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter, String broadcastPermission, Handler scheduler, int flags) {
        return registerReceiverInternal(receiver, getUserId(), filter, broadcastPermission, scheduler, getOuterContext(), flags);
    }

    @Override // android.content.Context
    public Intent registerReceiverAsUser(BroadcastReceiver receiver, UserHandle user, IntentFilter filter, String broadcastPermission, Handler scheduler) {
        return registerReceiverInternal(receiver, user.getIdentifier(), filter, broadcastPermission, scheduler, getOuterContext(), 0);
    }

    private Intent registerReceiverInternal(BroadcastReceiver receiver, int userId, IntentFilter filter, String broadcastPermission, Handler scheduler, Context context, int flags) {
        IIntentReceiver rd;
        Handler scheduler2;
        Handler scheduler3;
        if (receiver == null) {
            rd = null;
        } else if (this.mPackageInfo == null || context == null) {
            if (scheduler == null) {
                scheduler2 = this.mMainThread.getHandler();
            } else {
                scheduler2 = scheduler;
            }
            rd = new LoadedApk.ReceiverDispatcher(receiver, context, scheduler2, null, true).getIIntentReceiver();
        } else {
            if (scheduler == null) {
                scheduler3 = this.mMainThread.getHandler();
            } else {
                scheduler3 = scheduler;
            }
            rd = this.mPackageInfo.getReceiverDispatcher(receiver, context, scheduler3, this.mMainThread.getInstrumentation(), true);
        }
        HwFrameworkFactory.getHwActivityThread().setFilterIdentifier(filter, receiver, context, this.mBasePackageName);
        try {
            Intent intent = ActivityManager.getService().registerReceiver(this.mMainThread.getApplicationThread(), this.mBasePackageName, rd, filter, broadcastPermission, userId, flags);
            if (intent != null) {
                intent.setExtrasClassLoader(getClassLoader());
                intent.prepareToEnterProcess();
            }
            return intent;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.Context
    public void unregisterReceiver(BroadcastReceiver receiver) {
        LoadedApk loadedApk = this.mPackageInfo;
        if (loadedApk != null) {
            try {
                ActivityManager.getService().unregisterReceiver(loadedApk.forgetReceiverDispatcher(getOuterContext(), receiver));
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
            Log.w(TAG, "Implicit intents with startService are not safe: " + service + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + Debug.getCallers(2, 3));
            return;
        }
        throw new IllegalArgumentException("Service Intent must be explicit: " + service);
    }

    @Override // android.content.Context
    public ComponentName startService(Intent service) {
        warnIfCallingFromSystemProcess();
        return startServiceCommon(service, false, this.mUser);
    }

    @Override // android.content.Context
    public ComponentName startForegroundService(Intent service) {
        warnIfCallingFromSystemProcess();
        return startServiceCommon(service, true, this.mUser);
    }

    @Override // android.content.Context
    public boolean stopService(Intent service) {
        warnIfCallingFromSystemProcess();
        return stopServiceCommon(service, this.mUser);
    }

    @Override // android.content.Context
    public ComponentName startServiceAsUser(Intent service, UserHandle user) {
        return startServiceCommon(service, false, user);
    }

    @Override // android.content.Context
    public ComponentName startForegroundServiceAsUser(Intent service, UserHandle user) {
        return startServiceCommon(service, true, user);
    }

    private ComponentName startServiceCommon(Intent service, boolean requireForeground, UserHandle user) {
        try {
            validateServiceIntent(service);
            service.prepareToLeaveProcess(this);
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

    @Override // android.content.Context
    public boolean stopServiceAsUser(Intent service, UserHandle user) {
        return stopServiceCommon(service, user);
    }

    private boolean stopServiceCommon(Intent service, UserHandle user) {
        try {
            validateServiceIntent(service);
            service.prepareToLeaveProcess(this);
            int res = ActivityManager.getService().stopService(this.mMainThread.getApplicationThread(), service, service.resolveTypeIfNeeded(getContentResolver()), user.getIdentifier());
            if (res >= 0) {
                return res != 0;
            }
            throw new SecurityException("Not allowed to stop service " + service);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.Context
    public boolean bindService(Intent service, ServiceConnection conn, int flags) {
        warnIfCallingFromSystemProcess();
        return bindServiceCommon(service, conn, flags, null, this.mMainThread.getHandler(), null, getUser());
    }

    @Override // android.content.Context
    public boolean bindService(Intent service, int flags, Executor executor, ServiceConnection conn) {
        warnIfCallingFromSystemProcess();
        return bindServiceCommon(service, conn, flags, null, null, executor, getUser());
    }

    @Override // android.content.Context
    public boolean bindIsolatedService(Intent service, int flags, String instanceName, Executor executor, ServiceConnection conn) {
        warnIfCallingFromSystemProcess();
        if (instanceName != null) {
            return bindServiceCommon(service, conn, flags, instanceName, null, executor, getUser());
        }
        throw new NullPointerException("null instanceName");
    }

    @Override // android.content.Context
    public boolean bindServiceAsUser(Intent service, ServiceConnection conn, int flags, UserHandle user) {
        return bindServiceCommon(service, conn, flags, null, this.mMainThread.getHandler(), null, user);
    }

    @Override // android.content.Context
    public boolean bindServiceAsUser(Intent service, ServiceConnection conn, int flags, Handler handler, UserHandle user) {
        if (handler != null) {
            return bindServiceCommon(service, conn, flags, null, handler, null, user);
        }
        throw new IllegalArgumentException("handler must not be null.");
    }

    @Override // android.content.Context
    public IServiceConnection getServiceDispatcher(ServiceConnection conn, Handler handler, int flags) {
        return this.mPackageInfo.getServiceDispatcher(conn, getOuterContext(), handler, flags);
    }

    @Override // android.content.Context
    public IApplicationThread getIApplicationThread() {
        return this.mMainThread.getApplicationThread();
    }

    @Override // android.content.Context
    public Handler getMainThreadHandler() {
        return this.mMainThread.getHandler();
    }

    private boolean bindServiceCommon(Intent service, ServiceConnection conn, int flags, String instanceName, Handler handler, Executor executor, UserHandle user) {
        IServiceConnection sd;
        RemoteException e;
        int flags2;
        if (conn == null) {
            throw new IllegalArgumentException("connection is null");
        } else if (handler == null || executor == null) {
            LoadedApk loadedApk = this.mPackageInfo;
            if (loadedApk != null) {
                if (executor != null) {
                    sd = loadedApk.getServiceDispatcher(conn, getOuterContext(), executor, flags);
                } else {
                    sd = loadedApk.getServiceDispatcher(conn, getOuterContext(), handler, flags);
                }
                validateServiceIntent(service);
                try {
                    if (getActivityToken() != null || (flags & 1) != 0 || this.mPackageInfo == null || this.mPackageInfo.getApplicationInfo().targetSdkVersion >= 14) {
                        flags2 = flags;
                    } else {
                        flags2 = flags | 32;
                    }
                    try {
                        service.prepareToLeaveProcess(this);
                        int res = ActivityManager.getService().bindIsolatedService(this.mMainThread.getApplicationThread(), getActivityToken(), service, service.resolveTypeIfNeeded(getContentResolver()), sd, flags2, instanceName, getOpPackageName(), user.getIdentifier());
                        if (res >= 0) {
                            return res != 0;
                        }
                        throw new SecurityException("Not allowed to bind to service " + service);
                    } catch (RemoteException e2) {
                        e = e2;
                        throw e.rethrowFromSystemServer();
                    }
                } catch (RemoteException e3) {
                    e = e3;
                    throw e.rethrowFromSystemServer();
                }
            } else {
                throw new RuntimeException("Not supported in system context");
            }
        } else {
            throw new IllegalArgumentException("Handler and Executor both supplied");
        }
    }

    @Override // android.content.Context
    public void updateServiceGroup(ServiceConnection conn, int group, int importance) {
        if (conn != null) {
            LoadedApk loadedApk = this.mPackageInfo;
            if (loadedApk != null) {
                IServiceConnection sd = loadedApk.lookupServiceDispatcher(conn, getOuterContext());
                if (sd != null) {
                    try {
                        ActivityManager.getService().updateServiceGroup(sd, group, importance);
                    } catch (RemoteException e) {
                        throw e.rethrowFromSystemServer();
                    }
                } else {
                    throw new IllegalArgumentException("ServiceConnection not currently bound: " + conn);
                }
            } else {
                throw new RuntimeException("Not supported in system context");
            }
        } else {
            throw new IllegalArgumentException("connection is null");
        }
    }

    @Override // android.content.Context
    public void unbindService(ServiceConnection conn) {
        if (conn != null) {
            LoadedApk loadedApk = this.mPackageInfo;
            if (loadedApk != null) {
                try {
                    ActivityManager.getService().unbindService(loadedApk.forgetServiceDispatcher(getOuterContext(), conn));
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            } else {
                throw new RuntimeException("Not supported in system context");
            }
        } else {
            throw new IllegalArgumentException("connection is null");
        }
    }

    @Override // android.content.Context
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

    @Override // android.content.Context
    public Object getSystemService(String name) {
        return SystemServiceRegistry.getSystemService(this, name);
    }

    @Override // android.content.Context
    public String getSystemServiceName(Class<?> serviceClass) {
        return SystemServiceRegistry.getSystemServiceName(serviceClass);
    }

    @Override // android.content.Context
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

    @Override // android.content.Context
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

    @Override // android.content.Context
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

    @Override // android.content.Context
    public int checkCallingOrSelfPermission(String permission) {
        if (permission != null) {
            return checkPermission(permission, Binder.getCallingPid(), Binder.getCallingUid());
        }
        throw new IllegalArgumentException("permission is null");
    }

    @Override // android.content.Context
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

    @Override // android.content.Context
    public void enforcePermission(String permission, int pid, int uid, String message) {
        enforce(permission, checkPermission(permission, pid, uid), false, uid, message);
    }

    @Override // android.content.Context
    public void enforceCallingPermission(String permission, String message) {
        enforce(permission, checkCallingPermission(permission), false, Binder.getCallingUid(), message);
    }

    @Override // android.content.Context
    public void enforceCallingOrSelfPermission(String permission, String message) {
        enforce(permission, checkCallingOrSelfPermission(permission), true, Binder.getCallingUid(), message);
    }

    @Override // android.content.Context
    public void grantUriPermission(String toPackage, Uri uri, int modeFlags) {
        try {
            ActivityManager.getService().grantUriPermission(this.mMainThread.getApplicationThread(), toPackage, ContentProvider.getUriWithoutUserId(uri), modeFlags, resolveUserId(uri));
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.Context
    public void revokeUriPermission(Uri uri, int modeFlags) {
        try {
            ActivityManager.getService().revokeUriPermission(this.mMainThread.getApplicationThread(), null, ContentProvider.getUriWithoutUserId(uri), modeFlags, resolveUserId(uri));
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.Context
    public void revokeUriPermission(String targetPackage, Uri uri, int modeFlags) {
        try {
            ActivityManager.getService().revokeUriPermission(this.mMainThread.getApplicationThread(), targetPackage, ContentProvider.getUriWithoutUserId(uri), modeFlags, resolveUserId(uri));
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.Context
    public int checkUriPermission(Uri uri, int pid, int uid, int modeFlags) {
        try {
            return ActivityManager.getService().checkUriPermission(ContentProvider.getUriWithoutUserId(uri), pid, uid, modeFlags, resolveUserId(uri), null);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.Context
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

    @Override // android.content.Context
    public int checkCallingUriPermission(Uri uri, int modeFlags) {
        int pid = Binder.getCallingPid();
        if (pid != Process.myPid()) {
            return checkUriPermission(uri, pid, Binder.getCallingUid(), modeFlags);
        }
        return -1;
    }

    @Override // android.content.Context
    public int checkCallingOrSelfUriPermission(Uri uri, int modeFlags) {
        return checkUriPermission(uri, Binder.getCallingPid(), Binder.getCallingUid(), modeFlags);
    }

    @Override // android.content.Context
    public int checkUriPermission(Uri uri, String readPermission, String writePermission, int pid, int uid, int modeFlags) {
        if ((modeFlags & 1) != 0 && (readPermission == null || checkPermission(readPermission, pid, uid) == 0)) {
            return 0;
        }
        if ((modeFlags & 2) != 0 && (writePermission == null || checkPermission(writePermission, pid, uid) == 0)) {
            return 0;
        }
        if (uri != null) {
            return checkUriPermission(uri, pid, uid, modeFlags);
        }
        return -1;
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

    @Override // android.content.Context
    public void enforceUriPermission(Uri uri, int pid, int uid, int modeFlags, String message) {
        enforceForUri(modeFlags, checkUriPermission(uri, pid, uid, modeFlags), false, uid, uri, message);
    }

    @Override // android.content.Context
    public void enforceCallingUriPermission(Uri uri, int modeFlags, String message) {
        enforceForUri(modeFlags, checkCallingUriPermission(uri, modeFlags), false, Binder.getCallingUid(), uri, message);
    }

    @Override // android.content.Context
    public void enforceCallingOrSelfUriPermission(Uri uri, int modeFlags, String message) {
        enforceForUri(modeFlags, checkCallingOrSelfUriPermission(uri, modeFlags), true, Binder.getCallingUid(), uri, message);
    }

    @Override // android.content.Context
    public void enforceUriPermission(Uri uri, String readPermission, String writePermission, int pid, int uid, int modeFlags, String message) {
        enforceForUri(modeFlags, checkUriPermission(uri, readPermission, writePermission, pid, uid, modeFlags), false, uid, uri, message);
    }

    private void warnIfCallingFromSystemProcess() {
        if (Process.myUid() == 1000) {
            Slog.w(TAG, "Calling a method in the system process without a qualified user: " + Debug.getCallers(5));
        }
    }

    private static Resources createResources(IBinder activityToken, LoadedApk pi, String splitName, int displayId, Configuration overrideConfig, CompatibilityInfo compatInfo) {
        CompatibilityInfo compatInfo2;
        try {
            String[] splitResDirs = pi.getSplitPaths(splitName);
            ClassLoader classLoader = pi.getSplitClassLoader(splitName);
            IHwApsImpl hwApsImpl = HwFrameworkFactory.getHwApsImpl();
            if (hwApsImpl != null && compatInfo != null) {
                float ratio = hwApsImpl.getResolutionRatioByPkgName(pi.mPackageName);
                if (hwApsImpl.isValidSdrRatio(ratio) && compatInfo.supportsScreen()) {
                    compatInfo2 = CompatibilityInfo.makeCompatibilityInfo(ratio);
                    ResourcesManager.getInstance().setHwThemeType(pi.getResDir(), pi.getApplicationInfo().hwThemeType);
                    return ResourcesManager.getInstance().getResources(activityToken, pi.getResDir(), splitResDirs, pi.getOverlayDirs(), pi.getApplicationInfo().sharedLibraryFiles, displayId, overrideConfig, compatInfo2, classLoader);
                }
            }
            compatInfo2 = compatInfo;
            ResourcesManager.getInstance().setHwThemeType(pi.getResDir(), pi.getApplicationInfo().hwThemeType);
            return ResourcesManager.getInstance().getResources(activityToken, pi.getResDir(), splitResDirs, pi.getOverlayDirs(), pi.getApplicationInfo().sharedLibraryFiles, displayId, overrideConfig, compatInfo2, classLoader);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override // android.content.Context
    public Context createApplicationContext(ApplicationInfo application, int flags) throws PackageManager.NameNotFoundException {
        LoadedApk pi = this.mMainThread.getPackageInfo(application, this.mResources.getCompatibilityInfo(), 1073741824 | flags);
        if (pi != null) {
            ContextImpl c = new ContextImpl(this, this.mMainThread, pi, null, this.mActivityToken, new UserHandle(UserHandle.getUserId(application.uid)), flags, null, null);
            int displayId = getDisplayId();
            c.setResources(createResources(this.mActivityToken, pi, null, displayId, null, getDisplayAdjustments(displayId).getCompatibilityInfo()));
            if (c.mResources != null) {
                return c;
            }
        }
        throw new PackageManager.NameNotFoundException("Application package " + application.packageName + " not found");
    }

    @Override // android.content.Context
    public Context createPackageContext(String packageName, int flags) throws PackageManager.NameNotFoundException {
        return createPackageContextAsUser(packageName, flags, this.mUser);
    }

    @Override // android.content.Context
    public Context createPackageContextAsUser(String packageName, int flags, UserHandle user) throws PackageManager.NameNotFoundException {
        if (packageName.equals("system") || packageName.equals("android")) {
            return new ContextImpl(this, this.mMainThread, this.mPackageInfo, null, this.mActivityToken, user, flags, null, null);
        }
        LoadedApk pi = this.mMainThread.getPackageInfo(packageName, this.mResources.getCompatibilityInfo(), flags | 1073741824, user.getIdentifier());
        if (pi != null) {
            ContextImpl c = new ContextImpl(this, this.mMainThread, pi, null, this.mActivityToken, user, flags, null, null);
            int displayId = getDisplayId();
            c.setResources(createResources(this.mActivityToken, pi, null, displayId, null, getDisplayAdjustments(displayId).getCompatibilityInfo()));
            if (c.mResources != null) {
                return c;
            }
        }
        throw new PackageManager.NameNotFoundException("Application package " + packageName + " not found");
    }

    @Override // android.content.Context
    public Context createContextForSplit(String splitName) throws PackageManager.NameNotFoundException {
        if (!this.mPackageInfo.getApplicationInfo().requestsIsolatedSplitLoading()) {
            return this;
        }
        ClassLoader classLoader = this.mPackageInfo.getSplitClassLoader(splitName);
        String[] paths = this.mPackageInfo.getSplitPaths(splitName);
        ContextImpl context = new ContextImpl(this, this.mMainThread, this.mPackageInfo, splitName, this.mActivityToken, this.mUser, this.mFlags, classLoader, null);
        int displayId = getDisplayId();
        ResourcesManager.getInstance().setHwThemeType(this.mPackageInfo.getResDir(), this.mPackageInfo.getApplicationInfo().hwThemeType);
        context.setResources(ResourcesManager.getInstance().getResources(this.mActivityToken, this.mPackageInfo.getResDir(), paths, this.mPackageInfo.getOverlayDirs(), this.mPackageInfo.getApplicationInfo().sharedLibraryFiles, displayId, null, this.mPackageInfo.getCompatibilityInfo(), classLoader));
        return context;
    }

    @Override // android.content.Context
    public void releaseContextForSplit(String splitName) throws PackageManager.NameNotFoundException {
        this.mPackageInfo.releaseContextForSplit(splitName);
    }

    @Override // android.content.Context
    public Context createConfigurationContext(Configuration overrideConfiguration) {
        if (overrideConfiguration != null) {
            ContextImpl context = new ContextImpl(this, this.mMainThread, this.mPackageInfo, this.mSplitName, this.mActivityToken, this.mUser, this.mFlags, this.mClassLoader, null);
            int displayId = getDisplayId();
            context.setResources(createResources(this.mActivityToken, this.mPackageInfo, this.mSplitName, displayId, overrideConfiguration, getDisplayAdjustments(displayId).getCompatibilityInfo()));
            return context;
        }
        throw new IllegalArgumentException("overrideConfiguration must not be null");
    }

    @Override // android.content.Context
    public Context createDisplayContext(Display display) {
        if (display != null) {
            ContextImpl context = new ContextImpl(this, this.mMainThread, this.mPackageInfo, this.mSplitName, this.mActivityToken, this.mUser, this.mFlags, this.mClassLoader, null);
            int displayId = display.getDisplayId();
            context.setResources(createResources(this.mActivityToken, this.mPackageInfo, this.mSplitName, displayId, null, getDisplayAdjustments(displayId).getCompatibilityInfo()));
            context.mDisplay = display;
            return context;
        }
        throw new IllegalArgumentException("display must not be null");
    }

    @Override // android.content.Context
    public Context createDeviceProtectedStorageContext() {
        return new ContextImpl(this, this.mMainThread, this.mPackageInfo, this.mSplitName, this.mActivityToken, this.mUser, (this.mFlags & -17) | 8, this.mClassLoader, null);
    }

    @Override // android.content.Context
    public Context createCredentialProtectedStorageContext() {
        return new ContextImpl(this, this.mMainThread, this.mPackageInfo, this.mSplitName, this.mActivityToken, this.mUser, (this.mFlags & -9) | 16, this.mClassLoader, null);
    }

    @Override // android.content.Context
    public boolean isRestricted() {
        return (this.mFlags & 4) != 0;
    }

    @Override // android.content.Context
    public boolean isDeviceProtectedStorage() {
        return (this.mFlags & 8) != 0;
    }

    @Override // android.content.Context
    public boolean isCredentialProtectedStorage() {
        return (this.mFlags & 16) != 0;
    }

    @Override // android.content.Context
    public boolean canLoadUnsafeResources() {
        if (!getPackageName().equals(getOpPackageName()) && (this.mFlags & 2) == 0) {
            return false;
        }
        return true;
    }

    @Override // android.content.Context
    public Display getDisplay() {
        Display display = this.mDisplay;
        if (display == null) {
            return this.mResourcesManager.getAdjustedDisplay(0, this.mResources);
        }
        return display;
    }

    public int peekHwPCDisplayId() {
        Display display = this.mDisplay;
        if (display == null) {
            return -1;
        }
        return display.getDisplayId();
    }

    @Override // android.content.Context
    public int getDisplayId() {
        Display display = this.mDisplay;
        if (display != null) {
            return display.getDisplayId();
        }
        return 0;
    }

    @Override // android.content.Context
    public void updateDisplay(int displayId) {
        this.mDisplay = this.mResourcesManager.getAdjustedDisplay(displayId, this.mResources);
    }

    @Override // android.content.Context
    public DisplayAdjustments getDisplayAdjustments(int displayId) {
        return this.mResources.getDisplayAdjustments();
    }

    @Override // android.content.Context
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

    @Override // android.content.Context
    public File getDir(String name, int mode) {
        checkMode(mode);
        File file = makeFilename(getDataDir(), "app_" + name);
        if (!file.exists()) {
            file.mkdir();
            setFilePermissionsFromMode(file.getPath(), mode, 505);
        }
        return file;
    }

    @Override // android.content.Context
    public UserHandle getUser() {
        return this.mUser;
    }

    @Override // android.content.Context
    public int getUserId() {
        return this.mUser.getIdentifier();
    }

    @Override // android.content.Context
    public AutofillManager.AutofillClient getAutofillClient() {
        return this.mAutofillClient;
    }

    @Override // android.content.Context
    public void setAutofillClient(AutofillManager.AutofillClient client) {
        this.mAutofillClient = client;
    }

    @Override // android.content.Context
    public AutofillOptions getAutofillOptions() {
        return this.mAutofillOptions;
    }

    @Override // android.content.Context
    public void setAutofillOptions(AutofillOptions options) {
        this.mAutofillOptions = options;
    }

    @Override // android.content.Context
    public ContentCaptureOptions getContentCaptureOptions() {
        return this.mContentCaptureOptions;
    }

    @Override // android.content.Context
    public void setContentCaptureOptions(ContentCaptureOptions options) {
        this.mContentCaptureOptions = options;
    }

    @UnsupportedAppUsage
    static ContextImpl createSystemContext(ActivityThread mainThread) {
        DisplayMetrics metrics;
        LoadedApk packageInfo = new LoadedApk(mainThread);
        ContextImpl context = new ContextImpl(null, mainThread, packageInfo, null, null, null, 0, null, null);
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

    static ContextImpl createSystemUiContext(ContextImpl systemContext, int displayId) {
        LoadedApk packageInfo = systemContext.mPackageInfo;
        ContextImpl context = new ContextImpl(null, systemContext.mMainThread, packageInfo, null, null, null, 0, null, null);
        context.setResources(createResources(null, packageInfo, null, displayId, null, packageInfo.getCompatibilityInfo()));
        context.updateDisplay(displayId);
        return context;
    }

    static ContextImpl createSystemUiContext(ContextImpl systemContext) {
        return createSystemUiContext(systemContext, 0);
    }

    @UnsupportedAppUsage
    static ContextImpl createAppContext(ActivityThread mainThread, LoadedApk packageInfo) {
        return createAppContext(mainThread, packageInfo, null);
    }

    static ContextImpl createAppContext(ActivityThread mainThread, LoadedApk packageInfo, String opPackageName) {
        if (packageInfo == null) {
            throw new IllegalArgumentException("packageInfo");
        } else if (HwPCUtils.isValidExtDisplayId(mainThread.mDisplayId) && HwPCUtils.isPcCastMode()) {
            return createHwPcAppContext(mainThread, packageInfo);
        } else {
            ContextImpl context = new ContextImpl(null, mainThread, packageInfo, null, null, null, 0, null, opPackageName);
            context.setResources(packageInfo.getResources());
            return context;
        }
    }

    static ContextImpl createHwPcAppContext(ActivityThread mainThread, LoadedApk packageInfo) {
        CompatibilityInfo compatInfo;
        ContextImpl context = new ContextImpl(null, mainThread, packageInfo, null, null, null, 0, null, null);
        int displayId = mainThread.mDisplayId;
        if (displayId == 0) {
            compatInfo = packageInfo.getCompatibilityInfo();
        } else {
            compatInfo = CompatibilityInfo.DEFAULT_COMPATIBILITY_INFO;
        }
        ResourcesManager resourcesManager = ResourcesManager.getInstance();
        if (HwPCUtils.PKG_ARTICLE_NEWS.equals(packageInfo.getPackageName())) {
            context.setResources(packageInfo.getResources());
            context.mDisplay = resourcesManager.getAdjustedDisplay(displayId, packageInfo.getResources());
        } else {
            Resources resources = resourcesManager.getResources(null, packageInfo.getResDir(), packageInfo.getSplitResDirs(), packageInfo.getOverlayDirs(), packageInfo.getApplicationInfo().sharedLibraryFiles, displayId, mainThread.getOverrideConfig(), compatInfo, packageInfo.getClassLoader());
            context.setResources(resources);
            context.mDisplay = resourcesManager.getAdjustedDisplay(displayId, resources);
        }
        return context;
    }

    @UnsupportedAppUsage
    static ContextImpl createActivityContext(ActivityThread mainThread, LoadedApk packageInfo, ActivityInfo activityInfo, IBinder activityToken, int displayId, Configuration overrideConfiguration) {
        String[] splitDirs;
        ClassLoader classLoader;
        CompatibilityInfo compatInfo;
        if (packageInfo != null) {
            String[] splitDirs2 = packageInfo.getSplitResDirs();
            ClassLoader classLoader2 = packageInfo.getClassLoader();
            if (packageInfo.getApplicationInfo().requestsIsolatedSplitLoading()) {
                Trace.traceBegin(8192, "SplitDependencies");
                try {
                    ClassLoader classLoader3 = packageInfo.getSplitClassLoader(activityInfo.splitName);
                    String[] splitDirs3 = packageInfo.getSplitPaths(activityInfo.splitName);
                    Trace.traceEnd(8192);
                    splitDirs = splitDirs3;
                    classLoader = classLoader3;
                } catch (PackageManager.NameNotFoundException e) {
                    throw new RuntimeException(e);
                } catch (Throwable th) {
                    Trace.traceEnd(8192);
                    throw th;
                }
            } else {
                splitDirs = splitDirs2;
                classLoader = classLoader2;
            }
            ContextImpl context = new ContextImpl(null, mainThread, packageInfo, activityInfo.splitName, activityToken, null, 0, classLoader, null);
            int displayId2 = displayId != -1 ? displayId : 0;
            if (displayId2 == 0) {
                compatInfo = packageInfo.getCompatibilityInfo();
            } else {
                compatInfo = CompatibilityInfo.DEFAULT_COMPATIBILITY_INFO;
            }
            ResourcesManager resourcesManager = ResourcesManager.getInstance();
            resourcesManager.setHwThemeType(packageInfo.getResDir(), packageInfo.getApplicationInfo().hwThemeType);
            context.setResources(resourcesManager.createBaseActivityResources(activityToken, packageInfo.getResDir(), splitDirs, packageInfo.getOverlayDirs(), packageInfo.getApplicationInfo().sharedLibraryFiles, displayId2, overrideConfiguration, compatInfo, classLoader));
            context.mDisplay = resourcesManager.getAdjustedDisplay(displayId2, context.getResources());
            return context;
        }
        throw new IllegalArgumentException("packageInfo");
    }

    private ContextImpl(ContextImpl container, ActivityThread mainThread, LoadedApk packageInfo, String splitName, IBinder activityToken, UserHandle user, int flags, ClassLoader classLoader, String overrideOpPackageName) {
        String opPackageName;
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
            opPackageName = container.mOpPackageName;
            setResources(container.mResources);
            this.mDisplay = container.mDisplay;
        } else {
            this.mBasePackageName = packageInfo.mPackageName;
            ApplicationInfo ainfo = packageInfo.getApplicationInfo();
            if (ainfo.uid != 1000 || ainfo.uid == Process.myUid()) {
                opPackageName = this.mBasePackageName;
            } else {
                opPackageName = ActivityThread.currentPackageName();
            }
        }
        this.mOpPackageName = overrideOpPackageName != null ? overrideOpPackageName : opPackageName;
        this.mContentResolver = new ApplicationContentResolver(this, mainThread);
    }

    /* access modifiers changed from: package-private */
    public void setResources(Resources r) {
        if (r instanceof CompatResources) {
            ((CompatResources) r).setContext(this);
        }
        this.mResources = r;
    }

    /* access modifiers changed from: package-private */
    public void installSystemApplicationInfo(ApplicationInfo info, ClassLoader classLoader) {
        this.mPackageInfo.installSystemApplicationInfo(info, classLoader);
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public final void scheduleFinalCleanup(String who, String what) {
        this.mMainThread.scheduleContextCleanup(this, who, what);
    }

    /* access modifiers changed from: package-private */
    public final void performFinalCleanup(String who, String what) {
        this.mPackageInfo.removeContextRegistrations(getOuterContext(), who, what);
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public final Context getReceiverRestrictedContext() {
        Context context = this.mReceiverRestrictedContext;
        if (context != null) {
            return context;
        }
        ReceiverRestrictedContext receiverRestrictedContext = new ReceiverRestrictedContext(getOuterContext());
        this.mReceiverRestrictedContext = receiverRestrictedContext;
        return receiverRestrictedContext;
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public final void setOuterContext(Context context) {
        this.mOuterContext = context;
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public final Context getOuterContext() {
        return this.mOuterContext;
    }

    @Override // android.content.Context
    @UnsupportedAppUsage
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
        int perms = extraPermissions | 432;
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
            File res = new File(base, name);
            BlockGuard.getVmPolicy().onPathAccess(res.getPath());
            return res;
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

    /* access modifiers changed from: private */
    public static final class ApplicationContentResolver extends ContentResolver {
        @UnsupportedAppUsage
        private final ActivityThread mMainThread;

        public ApplicationContentResolver(Context context, ActivityThread mainThread) {
            super(context);
            this.mMainThread = (ActivityThread) Preconditions.checkNotNull(mainThread);
        }

        /* access modifiers changed from: protected */
        @Override // android.content.ContentResolver
        @UnsupportedAppUsage
        public IContentProvider acquireProvider(Context context, String auth) {
            return this.mMainThread.acquireProvider(context, ContentProvider.getAuthorityWithoutUserId(auth), resolveUserIdFromAuthority(auth), true);
        }

        /* access modifiers changed from: protected */
        @Override // android.content.ContentResolver
        public IContentProvider acquireExistingProvider(Context context, String auth) {
            return this.mMainThread.acquireExistingProvider(context, ContentProvider.getAuthorityWithoutUserId(auth), resolveUserIdFromAuthority(auth), true);
        }

        @Override // android.content.ContentResolver
        public boolean releaseProvider(IContentProvider provider) {
            return this.mMainThread.releaseProvider(provider, true);
        }

        /* access modifiers changed from: protected */
        @Override // android.content.ContentResolver
        public IContentProvider acquireUnstableProvider(Context c, String auth) {
            return this.mMainThread.acquireProvider(c, ContentProvider.getAuthorityWithoutUserId(auth), resolveUserIdFromAuthority(auth), false);
        }

        @Override // android.content.ContentResolver
        public boolean releaseUnstableProvider(IContentProvider icp) {
            return this.mMainThread.releaseProvider(icp, false);
        }

        @Override // android.content.ContentResolver
        public void unstableProviderDied(IContentProvider icp) {
            this.mMainThread.handleUnstableProviderDied(icp.asBinder(), true);
        }

        @Override // android.content.ContentResolver
        public void appNotRespondingViaProvider(IContentProvider icp) {
            this.mMainThread.appNotRespondingViaProvider(icp.asBinder());
        }

        /* access modifiers changed from: protected */
        public int resolveUserIdFromAuthority(String auth) {
            return ContentProvider.getUserIdFromAuthority(auth, getUserId());
        }
    }

    private static boolean checkPrimaryVolumeIsSD() {
        return 1 == SystemProperties.getInt("persist.sys.primarysd", 0);
    }
}
