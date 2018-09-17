package android.app;

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
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.content.res.CompatResources;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.hardware.camera2.params.TonemapCurve;
import android.hsm.HwSystemManager;
import android.net.ProxyInfo;
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
import android.os.ParcelFileDescriptor;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.UserHandle;
import android.os.storage.IStorageManager.Stub;
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
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.Preconditions;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.util.Objects;
import libcore.io.Memory;

public class ContextImpl extends Context {
    private static final boolean DBG_PRELOAD = Log.isLoggable(TAG_PRELOAD, 3);
    private static final boolean DEBUG = false;
    private static final boolean IS_SWITCH_SD_ENABLED = "true".equals(SystemProperties.get("ro.config.switchPrimaryVolume", "false"));
    private static final String TAG = "ContextImpl";
    private static final String TAG_PRELOAD = "PreloadSharedPrefs";
    private static final String UNMOUNT_PATH = "/dev/null";
    private static final String XATTR_INODE_CACHE = "user.inode_cache";
    private static final String XATTR_INODE_CODE_CACHE = "user.inode_code_cache";
    @GuardedBy("ContextImpl.class")
    private static ArrayMap<String, ArrayMap<File, SharedPreferencesImpl>> sSharedPrefsCache;
    private final IBinder mActivityToken;
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
    @GuardedBy("ContextImpl.class")
    private ArrayMap<String, File> mSharedPrefsPaths;
    private String mSplitName = null;
    private final Object mSync = new Object();
    private Theme mTheme = null;
    private int mThemeResource = 0;
    private final UserHandle mUser;

    private static final class ApplicationContentResolver extends ContentResolver {
        private final ActivityThread mMainThread;
        private final UserHandle mUser;

        public ApplicationContentResolver(Context context, ActivityThread mainThread, UserHandle user) {
            super(context);
            this.mMainThread = (ActivityThread) Preconditions.checkNotNull(mainThread);
            this.mUser = (UserHandle) Preconditions.checkNotNull(user);
        }

        protected IContentProvider acquireProvider(Context context, String auth) {
            return this.mMainThread.acquireProvider(context, ContentProvider.getAuthorityWithoutUserId(auth), resolveUserIdFromAuthority(auth), true);
        }

        protected IContentProvider acquireExistingProvider(Context context, String auth) {
            return this.mMainThread.acquireExistingProvider(context, ContentProvider.getAuthorityWithoutUserId(auth), resolveUserIdFromAuthority(auth), true);
        }

        public boolean releaseProvider(IContentProvider provider) {
            return this.mMainThread.releaseProvider(provider, true);
        }

        protected IContentProvider acquireUnstableProvider(Context c, String auth) {
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

        protected int resolveUserIdFromAuthority(String auth) {
            return ContentProvider.getUserIdFromAuthority(auth, this.mUser.getIdentifier());
        }
    }

    static ContextImpl getImpl(Context context) {
        while (context instanceof ContextWrapper) {
            Context nextContext = ((ContextWrapper) context).getBaseContext();
            if (nextContext == null) {
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
        PackageManager applicationPackageManager = new ApplicationPackageManager(this, pm);
        this.mPackageManager = applicationPackageManager;
        return applicationPackageManager;
    }

    public ContentResolver getContentResolver() {
        return this.mContentResolver;
    }

    public Looper getMainLooper() {
        return this.mMainThread.getLooper();
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

    public Theme getTheme() {
        synchronized (this.mSync) {
            Theme theme;
            if (this.mTheme != null) {
                theme = this.mTheme;
                return theme;
            }
            this.mThemeResource = Resources.selectDefaultTheme(this.mThemeResource, getOuterContext().getApplicationInfo().targetSdkVersion);
            initializeTheme();
            theme = this.mTheme;
            return theme;
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
                this.mSharedPrefsPaths = new ArrayMap();
            }
            file = (File) this.mSharedPrefsPaths.get(name);
            if (file == null) {
                file = getSharedPreferencesPath(name);
                this.mSharedPrefsPaths.put(name, file);
            }
        }
        return getSharedPreferences(file, mode);
    }

    private boolean isBuggy() {
        if (SystemProperties.getBoolean("fw.ignore_buggy", false)) {
            return false;
        }
        return "com.google.android.tts".equals(getApplicationInfo().packageName) || "com.breel.geswallpapers".equals(getApplicationInfo().packageName);
    }

    /* JADX WARNING: Missing block: B:22:0x0054, code:
            if ((r6 & 4) != 0) goto L_0x0060;
     */
    /* JADX WARNING: Missing block: B:24:0x005e, code:
            if (getApplicationInfo().targetSdkVersion >= 11) goto L_0x0063;
     */
    /* JADX WARNING: Missing block: B:25:0x0060, code:
            r1.startReloadIfChangedUnexpectedly();
     */
    /* JADX WARNING: Missing block: B:26:0x0063, code:
            return r1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public SharedPreferences getSharedPreferences(File file, int mode) {
        checkMode(mode);
        if (getApplicationInfo().targetSdkVersion >= 26 && isCredentialProtectedStorage()) {
            getSystemService(StorageManager.class);
            if (!((StorageManager.isUserKeyUnlocked(UserHandle.myUserId()) ^ 1) == 0 || (isBuggy() ^ 1) == 0)) {
                throw new IllegalStateException("SharedPreferences in credential encrypted storage are not available until after user is unlocked");
            }
        }
        synchronized (ContextImpl.class) {
            ArrayMap<File, SharedPreferencesImpl> cache = getSharedPreferencesCacheLocked();
            SharedPreferencesImpl sp = (SharedPreferencesImpl) cache.get(file);
            if (sp == null) {
                sp = new SharedPreferencesImpl(file, mode);
                cache.put(file, sp);
                return sp;
            }
            sp.setMode(mode);
        }
    }

    private ArrayMap<File, SharedPreferencesImpl> getSharedPreferencesCacheLocked() {
        if (sSharedPrefsCache == null) {
            sSharedPrefsCache = new ArrayMap();
        }
        String packageName = getPackageName();
        ArrayMap<File, SharedPreferencesImpl> packagePrefs = (ArrayMap) sSharedPrefsCache.get(packageName);
        if (packagePrefs != null) {
            return packagePrefs;
        }
        packagePrefs = new ArrayMap();
        sSharedPrefsCache.put(packageName, packagePrefs);
        return packagePrefs;
    }

    private static int moveFiles(File sourceDir, File targetDir, final String prefix) {
        File[] sourceFiles = FileUtils.listFilesOrEmpty(sourceDir, new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.startsWith(prefix);
            }
        });
        int res = 0;
        int i = 0;
        int length = sourceFiles.length;
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
        boolean z = false;
        synchronized (ContextImpl.class) {
            File source = sourceContext.getSharedPreferencesPath(name);
            File target = getSharedPreferencesPath(name);
            int res = moveFiles(source.getParentFile(), target.getParentFile(), source.getName());
            if (res > 0) {
                ArrayMap<File, SharedPreferencesImpl> cache = getSharedPreferencesCacheLocked();
                cache.remove(source);
                cache.remove(target);
            }
            if (res != -1) {
                z = true;
            }
        }
        return z;
    }

    public boolean deleteSharedPreferences(String name) {
        boolean exists;
        synchronized (ContextImpl.class) {
            File prefs = getSharedPreferencesPath(name);
            File prefsBackup = SharedPreferencesImpl.makeBackupFile(prefs);
            getSharedPreferencesCacheLocked().remove(prefs);
            prefs.delete();
            prefsBackup.delete();
            exists = (!prefs.exists() ? prefsBackup.exists() : 1) ^ 1;
        }
        return exists;
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

    public void preloadSharedPrefs() {
        new Thread("ContextImpl-preloadSharedPrefs") {
            public void run() {
                String[] names = ContextImpl.this.getPreferencesDir().list(new FilenameFilter() {
                    public boolean accept(File dir, String filename) {
                        boolean z = false;
                        if (ContextImpl.DBG_PRELOAD) {
                            Log.d(ContextImpl.TAG_PRELOAD, "Get " + filename);
                        }
                        if (!filename.endsWith(".xml")) {
                            return false;
                        }
                        if (new File(dir, filename).length() < 10240) {
                            z = true;
                        }
                        return z;
                    }
                });
                if (names == null) {
                    if (ContextImpl.DBG_PRELOAD) {
                        Log.d(ContextImpl.TAG_PRELOAD, "No prefs to load");
                    }
                    return;
                }
                for (String name : names) {
                    ((SharedPreferencesImpl) ContextImpl.this.getSharedPreferences(name.substring(0, name.length() - 4), 0)).awaitLoaded();
                }
            }
        }.start();
    }

    public FileInputStream openFileInput(String name) throws FileNotFoundException {
        return new FileInputStream(makeFilename(getFilesDir(), name));
    }

    public FileOutputStream openFileOutput(String name, int mode) throws FileNotFoundException {
        checkMode(mode);
        boolean append = (32768 & mode) != 0;
        File f = makeFilename(getFilesDir(), name);
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(f, append);
            setFilePermissionsFromMode(f.getPath(), mode, 0);
            return fos;
        } catch (FileNotFoundException e) {
            File parent = f.getParentFile();
            parent.mkdir();
            FileUtils.setPermissions(parent.getPath(), 505, -1, -1);
            fos = new FileOutputStream(f, append);
            setFilePermissionsFromMode(f.getPath(), mode, 0);
            return fos;
        }
    }

    public boolean deleteFile(String name) {
        return makeFilename(getFilesDir(), name).delete();
    }

    private static File ensurePrivateDirExists(File file) {
        return ensurePrivateDirExists(file, 505, -1, null);
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
        File ensurePrivateDirExists;
        synchronized (this.mSync) {
            if (this.mFilesDir == null) {
                this.mFilesDir = new File(getDataDir(), "files");
            }
            ensurePrivateDirExists = ensurePrivateDirExists(this.mFilesDir);
        }
        return ensurePrivateDirExists;
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
        if (!IS_SWITCH_SD_ENABLED || !checkPrimaryVolumeIsSD()) {
            return getExternalFilesDirs(type)[0];
        }
        File[] externalFilesDirs = getExternalFilesDirs(type);
        if (externalFilesDirs.length == 1) {
            return externalFilesDirs[0];
        }
        return externalFilesDirs[1];
    }

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

    public File getObbDir() {
        if (!IS_SWITCH_SD_ENABLED || !checkPrimaryVolumeIsSD()) {
            return getObbDirs()[0];
        }
        if (getObbDirs().length == 1) {
            return getObbDirs()[0];
        }
        return getObbDirs()[1];
    }

    public File[] getObbDirs() {
        File[] ensureExternalDirsExistOrFilter;
        synchronized (this.mSync) {
            ensureExternalDirsExistOrFilter = ensureExternalDirsExistOrFilter(Environment.buildExternalStorageAppObbDirs(getPackageName()));
        }
        return ensureExternalDirsExistOrFilter;
    }

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

    public File getExternalCacheDir() {
        if (!IS_SWITCH_SD_ENABLED || !checkPrimaryVolumeIsSD()) {
            return getExternalCacheDirs()[0];
        }
        if (getExternalCacheDirs().length == 1) {
            return getExternalCacheDirs()[0];
        }
        return getExternalCacheDirs()[1];
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
        return makeFilename(getPreferencesDir(), name + ".xml");
    }

    public String[] fileList() {
        return FileUtils.listOrEmpty(getFilesDir());
    }

    public SQLiteDatabase openOrCreateDatabase(String name, int mode, CursorFactory factory) {
        return openOrCreateDatabase(name, mode, factory, null);
    }

    public SQLiteDatabase openOrCreateDatabase(String name, int mode, CursorFactory factory, DatabaseErrorHandler errorHandler) {
        checkMode(mode);
        File f = getDatabasePath(name);
        int flags = 268435456;
        if ((mode & 8) != 0) {
            flags = ParcelFileDescriptor.MODE_READ_WRITE;
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
        if (HwSystemManager.canStartActivity(getApplicationContext(), intent)) {
            warnIfCallingFromSystemProcess();
            if ((intent.getFlags() & 268435456) == 0 && options != null && ActivityOptions.fromBundle(options).getLaunchTaskId() == -1) {
                throw new AndroidRuntimeException("Calling startActivity() from outside of an Activity  context requires the FLAG_ACTIVITY_NEW_TASK flag. Is this really what you want?");
            }
            this.mMainThread.getInstrumentation().execStartActivity(getOuterContext(), this.mMainThread.getApplicationThread(), null, (Activity) null, intent, -1, HwPCUtils.hookStartActivityOptions(this, options));
            return;
        }
        Log.i(TAG, "this app not allowed to start activity:" + intent);
    }

    public void startActivityAsUser(Intent intent, Bundle options, UserHandle user) {
        try {
            if (Jlog.isPerfTest()) {
                Jlog.i(2022, "whopid=" + Process.myPid() + "&whopkg=" + getPackageName() + "&" + Intent.toPkgClsString(intent));
            }
            ActivityManager.getService().startActivityAsUser(this.mMainThread.getApplicationThread(), getBasePackageName(), intent, intent.resolveTypeIfNeeded(getContentResolver()), null, null, 0, 268435456, null, HwPCUtils.hookStartActivityOptions(this, options), user.getIdentifier());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void startActivities(Intent[] intents) {
        warnIfCallingFromSystemProcess();
        startActivities(intents, null);
    }

    public void startActivitiesAsUser(Intent[] intents, Bundle options, UserHandle userHandle) {
        if ((intents[0].getFlags() & 268435456) == 0) {
            throw new AndroidRuntimeException("Calling startActivities() from outside of an Activity  context requires the FLAG_ACTIVITY_NEW_TASK flag on first Intent. Is this really what you want?");
        }
        this.mMainThread.getInstrumentation().execStartActivitiesAsUser(getOuterContext(), this.mMainThread.getApplicationThread(), null, (Activity) null, intents, HwPCUtils.hookStartActivityOptions(this, options), userHandle.getIdentifier());
    }

    public void startActivities(Intent[] intents, Bundle options) {
        warnIfCallingFromSystemProcess();
        if ((intents[0].getFlags() & 268435456) == 0) {
            throw new AndroidRuntimeException("Calling startActivities() from outside of an Activity  context requires the FLAG_ACTIVITY_NEW_TASK flag on first Intent. Is this really what you want?");
        }
        this.mMainThread.getInstrumentation().execStartActivities(getOuterContext(), this.mMainThread.getApplicationThread(), null, (Activity) null, intents, HwPCUtils.hookStartActivityOptions(this, options));
    }

    public void startIntentSender(IntentSender intent, Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags) throws SendIntentException {
        startIntentSender(intent, fillInIntent, flagsMask, flagsValues, extraFlags, null);
    }

    public void startIntentSender(IntentSender intent, Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags, Bundle options) throws SendIntentException {
        String resolvedType = null;
        if (fillInIntent != null) {
            try {
                fillInIntent.migrateExtraStreamToClipData();
                fillInIntent.prepareToLeaveProcess((Context) this);
                resolvedType = fillInIntent.resolveTypeIfNeeded(getContentResolver());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
        int result = ActivityManager.getService().startActivityIntentSender(this.mMainThread.getApplicationThread(), intent != null ? intent.getTarget() : null, intent != null ? intent.getWhitelistToken() : null, fillInIntent, resolvedType, null, null, 0, flagsMask, flagsValues, options);
        if (result == -96) {
            throw new SendIntentException();
        }
        Instrumentation.checkStartActivityResult(result, null);
    }

    public void sendBroadcast(Intent intent) {
        warnIfCallingFromSystemProcess();
        if (HwSystemManager.canSendBroadcast(this, intent)) {
            String resolvedType = intent.resolveTypeIfNeeded(getContentResolver());
            try {
                intent.prepareToLeaveProcess((Context) this);
                ActivityManager.getService().broadcastIntent(this.mMainThread.getApplicationThread(), intent, resolvedType, null, -1, null, null, null, -1, null, false, false, getUserId());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void sendBroadcast(Intent intent, String receiverPermission) {
        warnIfCallingFromSystemProcess();
        if (HwSystemManager.canSendBroadcast(this, intent)) {
            String resolvedType = intent.resolveTypeIfNeeded(getContentResolver());
            String[] receiverPermissions = receiverPermission == null ? null : new String[]{receiverPermission};
            try {
                intent.prepareToLeaveProcess((Context) this);
                ActivityManager.getService().broadcastIntent(this.mMainThread.getApplicationThread(), intent, resolvedType, null, -1, null, null, receiverPermissions, -1, null, false, false, getUserId());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void sendBroadcastMultiplePermissions(Intent intent, String[] receiverPermissions) {
        warnIfCallingFromSystemProcess();
        String resolvedType = intent.resolveTypeIfNeeded(getContentResolver());
        try {
            intent.prepareToLeaveProcess((Context) this);
            ActivityManager.getService().broadcastIntent(this.mMainThread.getApplicationThread(), intent, resolvedType, null, -1, null, null, receiverPermissions, -1, null, false, false, getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void sendBroadcast(Intent intent, String receiverPermission, Bundle options) {
        warnIfCallingFromSystemProcess();
        String resolvedType = intent.resolveTypeIfNeeded(getContentResolver());
        String[] receiverPermissions = receiverPermission == null ? null : new String[]{receiverPermission};
        try {
            intent.prepareToLeaveProcess((Context) this);
            ActivityManager.getService().broadcastIntent(this.mMainThread.getApplicationThread(), intent, resolvedType, null, -1, null, null, receiverPermissions, -1, options, false, false, getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void sendBroadcast(Intent intent, String receiverPermission, int appOp) {
        warnIfCallingFromSystemProcess();
        if (HwSystemManager.canSendBroadcast(this, intent)) {
            String resolvedType = intent.resolveTypeIfNeeded(getContentResolver());
            String[] receiverPermissions = receiverPermission == null ? null : new String[]{receiverPermission};
            try {
                intent.prepareToLeaveProcess((Context) this);
                ActivityManager.getService().broadcastIntent(this.mMainThread.getApplicationThread(), intent, resolvedType, null, -1, null, null, receiverPermissions, appOp, null, false, false, getUserId());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void sendOrderedBroadcast(Intent intent, String receiverPermission) {
        warnIfCallingFromSystemProcess();
        String resolvedType = intent.resolveTypeIfNeeded(getContentResolver());
        String[] receiverPermissions = receiverPermission == null ? null : new String[]{receiverPermission};
        try {
            intent.prepareToLeaveProcess((Context) this);
            ActivityManager.getService().broadcastIntent(this.mMainThread.getApplicationThread(), intent, resolvedType, null, -1, null, null, receiverPermissions, -1, null, true, false, getUserId());
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

    void sendOrderedBroadcast(Intent intent, String receiverPermission, int appOp, BroadcastReceiver resultReceiver, Handler scheduler, int initialCode, String initialData, Bundle initialExtras, Bundle options) {
        warnIfCallingFromSystemProcess();
        if (HwSystemManager.canSendBroadcast(this, intent)) {
            IIntentReceiver rd = null;
            if (resultReceiver != null) {
                if (this.mPackageInfo != null) {
                    if (scheduler == null) {
                        scheduler = this.mMainThread.getHandler();
                    }
                    rd = this.mPackageInfo.getReceiverDispatcher(resultReceiver, getOuterContext(), scheduler, this.mMainThread.getInstrumentation(), false);
                } else {
                    if (scheduler == null) {
                        scheduler = this.mMainThread.getHandler();
                    }
                    rd = new ReceiverDispatcher(resultReceiver, getOuterContext(), scheduler, null, false).getIIntentReceiver();
                }
            }
            String resolvedType = intent.resolveTypeIfNeeded(getContentResolver());
            String[] receiverPermissions = receiverPermission == null ? null : new String[]{receiverPermission};
            try {
                intent.prepareToLeaveProcess((Context) this);
                ActivityManager.getService().broadcastIntent(this.mMainThread.getApplicationThread(), intent, resolvedType, rd, initialCode, initialData, initialExtras, receiverPermissions, appOp, options, true, false, getUserId());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void sendBroadcastAsUser(Intent intent, UserHandle user) {
        if (HwSystemManager.canSendBroadcast(this, intent)) {
            String resolvedType = intent.resolveTypeIfNeeded(getContentResolver());
            try {
                intent.prepareToLeaveProcess((Context) this);
                ActivityManager.getService().broadcastIntent(this.mMainThread.getApplicationThread(), intent, resolvedType, null, -1, null, null, null, -1, null, false, false, user.getIdentifier());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void sendBroadcastAsUser(Intent intent, UserHandle user, String receiverPermission) {
        sendBroadcastAsUser(intent, user, receiverPermission, -1);
    }

    public void sendBroadcastAsUser(Intent intent, UserHandle user, String receiverPermission, Bundle options) {
        String resolvedType = intent.resolveTypeIfNeeded(getContentResolver());
        String[] receiverPermissions = receiverPermission == null ? null : new String[]{receiverPermission};
        try {
            intent.prepareToLeaveProcess((Context) this);
            ActivityManager.getService().broadcastIntent(this.mMainThread.getApplicationThread(), intent, resolvedType, null, -1, null, null, receiverPermissions, -1, options, false, false, user.getIdentifier());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void sendBroadcastAsUser(Intent intent, UserHandle user, String receiverPermission, int appOp) {
        if (HwSystemManager.canSendBroadcast(this, intent)) {
            String resolvedType = intent.resolveTypeIfNeeded(getContentResolver());
            String[] receiverPermissions = receiverPermission == null ? null : new String[]{receiverPermission};
            try {
                intent.prepareToLeaveProcess((Context) this);
                ActivityManager.getService().broadcastIntent(this.mMainThread.getApplicationThread(), intent, resolvedType, null, -1, null, null, receiverPermissions, appOp, null, false, false, user.getIdentifier());
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
        if (HwSystemManager.canSendBroadcast(this, intent)) {
            IIntentReceiver rd = null;
            if (resultReceiver != null) {
                if (this.mPackageInfo != null) {
                    if (scheduler == null) {
                        scheduler = this.mMainThread.getHandler();
                    }
                    rd = this.mPackageInfo.getReceiverDispatcher(resultReceiver, getOuterContext(), scheduler, this.mMainThread.getInstrumentation(), false);
                } else {
                    if (scheduler == null) {
                        scheduler = this.mMainThread.getHandler();
                    }
                    rd = new ReceiverDispatcher(resultReceiver, getOuterContext(), scheduler, null, false).getIIntentReceiver();
                }
            }
            String resolvedType = intent.resolveTypeIfNeeded(getContentResolver());
            String[] receiverPermissions = receiverPermission == null ? null : new String[]{receiverPermission};
            try {
                intent.prepareToLeaveProcess((Context) this);
                ActivityManager.getService().broadcastIntent(this.mMainThread.getApplicationThread(), intent, resolvedType, rd, initialCode, initialData, initialExtras, receiverPermissions, appOp, options, true, false, user.getIdentifier());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    @Deprecated
    public void sendStickyBroadcast(Intent intent) {
        warnIfCallingFromSystemProcess();
        if (HwSystemManager.canSendBroadcast(this, intent)) {
            String resolvedType = intent.resolveTypeIfNeeded(getContentResolver());
            try {
                intent.prepareToLeaveProcess((Context) this);
                ActivityManager.getService().broadcastIntent(this.mMainThread.getApplicationThread(), intent, resolvedType, null, -1, null, null, null, -1, null, false, true, getUserId());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    @Deprecated
    public void sendStickyOrderedBroadcast(Intent intent, BroadcastReceiver resultReceiver, Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {
        warnIfCallingFromSystemProcess();
        if (HwSystemManager.canSendBroadcast(this, intent)) {
            IIntentReceiver rd = null;
            if (resultReceiver != null) {
                if (this.mPackageInfo != null) {
                    if (scheduler == null) {
                        scheduler = this.mMainThread.getHandler();
                    }
                    rd = this.mPackageInfo.getReceiverDispatcher(resultReceiver, getOuterContext(), scheduler, this.mMainThread.getInstrumentation(), false);
                } else {
                    if (scheduler == null) {
                        scheduler = this.mMainThread.getHandler();
                    }
                    rd = new ReceiverDispatcher(resultReceiver, getOuterContext(), scheduler, null, false).getIIntentReceiver();
                }
            }
            String resolvedType = intent.resolveTypeIfNeeded(getContentResolver());
            try {
                intent.prepareToLeaveProcess((Context) this);
                ActivityManager.getService().broadcastIntent(this.mMainThread.getApplicationThread(), intent, resolvedType, rd, initialCode, initialData, initialExtras, null, -1, null, true, true, getUserId());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    @Deprecated
    public void removeStickyBroadcast(Intent intent) {
        String resolvedType = intent.resolveTypeIfNeeded(getContentResolver());
        if (resolvedType != null) {
            Intent intent2 = new Intent(intent);
            intent2.setDataAndType(intent2.getData(), resolvedType);
            intent = intent2;
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
        if (HwSystemManager.canSendBroadcast(this, intent)) {
            String resolvedType = intent.resolveTypeIfNeeded(getContentResolver());
            try {
                intent.prepareToLeaveProcess((Context) this);
                ActivityManager.getService().broadcastIntent(this.mMainThread.getApplicationThread(), intent, resolvedType, null, -1, null, null, null, -1, null, false, true, user.getIdentifier());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    @Deprecated
    public void sendStickyBroadcastAsUser(Intent intent, UserHandle user, Bundle options) {
        String resolvedType = intent.resolveTypeIfNeeded(getContentResolver());
        try {
            intent.prepareToLeaveProcess((Context) this);
            ActivityManager.getService().broadcastIntent(this.mMainThread.getApplicationThread(), intent, resolvedType, null, -1, null, null, null, -1, options, false, true, user.getIdentifier());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public void sendStickyOrderedBroadcastAsUser(Intent intent, UserHandle user, BroadcastReceiver resultReceiver, Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {
        if (HwSystemManager.canSendBroadcast(this, intent)) {
            IIntentReceiver rd = null;
            if (resultReceiver != null) {
                if (this.mPackageInfo != null) {
                    if (scheduler == null) {
                        scheduler = this.mMainThread.getHandler();
                    }
                    rd = this.mPackageInfo.getReceiverDispatcher(resultReceiver, getOuterContext(), scheduler, this.mMainThread.getInstrumentation(), false);
                } else {
                    if (scheduler == null) {
                        scheduler = this.mMainThread.getHandler();
                    }
                    rd = new ReceiverDispatcher(resultReceiver, getOuterContext(), scheduler, null, false).getIIntentReceiver();
                }
            }
            String resolvedType = intent.resolveTypeIfNeeded(getContentResolver());
            try {
                intent.prepareToLeaveProcess((Context) this);
                ActivityManager.getService().broadcastIntent(this.mMainThread.getApplicationThread(), intent, resolvedType, rd, initialCode, initialData, initialExtras, null, -1, null, true, true, user.getIdentifier());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    @Deprecated
    public void removeStickyBroadcastAsUser(Intent intent, UserHandle user) {
        String resolvedType = intent.resolveTypeIfNeeded(getContentResolver());
        if (resolvedType != null) {
            Intent intent2 = new Intent(intent);
            intent2.setDataAndType(intent2.getData(), resolvedType);
            intent = intent2;
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
        IIntentReceiver rd = null;
        if (receiver != null) {
            if (this.mPackageInfo == null || context == null) {
                if (scheduler == null) {
                    scheduler = this.mMainThread.getHandler();
                }
                rd = new ReceiverDispatcher(receiver, context, scheduler, null, true).getIIntentReceiver();
            } else {
                if (scheduler == null) {
                    scheduler = this.mMainThread.getHandler();
                }
                rd = this.mPackageInfo.getReceiverDispatcher(receiver, context, scheduler, this.mMainThread.getInstrumentation(), true);
            }
        }
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

    public void unregisterReceiver(BroadcastReceiver receiver) {
        if (this.mPackageInfo != null) {
            try {
                ActivityManager.getService().unregisterReceiver(this.mPackageInfo.forgetReceiverDispatcher(getOuterContext(), receiver));
                return;
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
        throw new RuntimeException("Not supported in system context");
    }

    private void validateServiceIntent(Intent service) {
        if (service.getComponent() != null || service.getPackage() != null) {
            return;
        }
        if (getApplicationInfo().targetSdkVersion >= 21) {
            throw new IllegalArgumentException("Service Intent must be explicit: " + service);
        }
        Log.w(TAG, "Implicit intents with startService are not safe: " + service + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + Debug.getCallers(2, 3));
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
            if (res < 0) {
                throw new SecurityException("Not allowed to stop service " + service);
            } else if (res != 0) {
                return true;
            } else {
                return false;
            }
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean bindService(Intent service, ServiceConnection conn, int flags) {
        warnIfCallingFromSystemProcess();
        return bindServiceCommon(service, conn, flags, this.mMainThread.getHandler(), Process.myUserHandle());
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
        if (conn == null) {
            throw new IllegalArgumentException("connection is null");
        } else if (this.mPackageInfo != null) {
            IServiceConnection sd = this.mPackageInfo.getServiceDispatcher(conn, getOuterContext(), handler, flags);
            validateServiceIntent(service);
            try {
                if (getActivityToken() == null && (flags & 1) == 0 && this.mPackageInfo != null && this.mPackageInfo.getApplicationInfo().targetSdkVersion < 14) {
                    flags |= 32;
                }
                service.prepareToLeaveProcess((Context) this);
                int res = ActivityManager.getService().bindService(this.mMainThread.getApplicationThread(), getActivityToken(), service, service.resolveTypeIfNeeded(getContentResolver()), sd, flags, getOpPackageName(), user.getIdentifier());
                if (res >= 0) {
                    return res != 0;
                } else {
                    throw new SecurityException("Not allowed to bind to service " + service);
                }
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
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
        if (permission == null) {
            throw new IllegalArgumentException("permission is null");
        }
        IActivityManager am = ActivityManager.getService();
        if (am == null) {
            int appId = UserHandle.getAppId(uid);
            if (appId == 0 || appId == 1000) {
                Slog.w(TAG, "Missing ActivityManager; assuming " + uid + " holds " + permission);
                return 0;
            }
        }
        try {
            return am.checkPermission(permission, pid, uid);
        } catch (RemoteException e) {
            Flog.e(201, "checkPermission " + permission + ", for pid " + pid + ", uid " + pid);
            throw e.rethrowFromSystemServer();
        }
    }

    public int checkPermission(String permission, int pid, int uid, IBinder callerToken) {
        if (permission == null) {
            throw new IllegalArgumentException("permission is null");
        }
        try {
            return ActivityManager.getService().checkPermissionWithToken(permission, pid, uid, callerToken);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int checkCallingPermission(String permission) {
        if (permission == null) {
            throw new IllegalArgumentException("permission is null");
        }
        int pid = Binder.getCallingPid();
        if (pid != Process.myPid()) {
            return checkPermission(permission, pid, Binder.getCallingUid());
        }
        return -1;
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
        if (resultOfCheck != 0) {
            String str;
            StringBuilder append = new StringBuilder().append(message != null ? message + ": " : ProxyInfo.LOCAL_EXCL_LIST);
            if (selfToo) {
                str = "Neither user " + uid + " nor current process has ";
            } else {
                str = "uid " + uid + " does not have ";
            }
            throw new SecurityException(append.append(str).append(permission).append(".").toString());
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
        if ((modeFlags & 1) != 0 && (readPermission == null || checkPermission(readPermission, pid, uid) == 0)) {
            return 0;
        }
        if ((modeFlags & 2) != 0 && (writePermission == null || checkPermission(writePermission, pid, uid) == 0)) {
            return 0;
        }
        int checkUriPermission;
        if (uri != null) {
            checkUriPermission = checkUriPermission(uri, pid, uid, modeFlags);
        } else {
            checkUriPermission = -1;
        }
        return checkUriPermission;
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
        if (resultOfCheck != 0) {
            String str;
            StringBuilder append = new StringBuilder().append(message != null ? message + ": " : ProxyInfo.LOCAL_EXCL_LIST);
            if (selfToo) {
                str = "Neither user " + uid + " nor current process has ";
            } else {
                str = "User " + uid + " does not have ";
            }
            throw new SecurityException(append.append(str).append(uriModeFlagToString(modeFlags)).append(" permission on ").append(uri).append(".").toString());
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
                    resolutionRatio = apsmanager.getResolution(pi.getPackageName());
                }
                if (TonemapCurve.LEVEL_BLACK < resolutionRatio && resolutionRatio < 1.0f) {
                    compatInfo = CompatibilityInfo.makeCompatibilityInfo(resolutionRatio);
                }
            }
            return ResourcesManager.getInstance().getResources(activityToken, pi.getResDir(), splitResDirs, pi.getOverlayDirs(), pi.getApplicationInfo().sharedLibraryFiles, displayId, overrideConfig, compatInfo, classLoader);
        } catch (NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public Context createApplicationContext(ApplicationInfo application, int flags) throws NameNotFoundException {
        LoadedApk pi = this.mMainThread.getPackageInfo(application, this.mResources.getCompatibilityInfo(), 1073741824 | flags);
        if (pi != null) {
            if ("com.huawei.health".contains(pi.getPackageName())) {
                Slog.i(TAG, "ContextImpl#createApplicationContext pi.resDir is" + pi.getResDir());
            }
            ContextImpl c = new ContextImpl(this, this.mMainThread, pi, null, this.mActivityToken, new UserHandle(UserHandle.getUserId(application.uid)), flags, null);
            int displayId = this.mDisplay != null ? this.mDisplay.getDisplayId() : 0;
            c.setResources(createResources(this.mActivityToken, pi, null, displayId, null, getDisplayAdjustments(displayId).getCompatibilityInfo()));
            if (c.mResources != null) {
                return c;
            }
        }
        throw new NameNotFoundException("Application package " + application.packageName + " not found");
    }

    public Context createPackageContext(String packageName, int flags) throws NameNotFoundException {
        return createPackageContextAsUser(packageName, flags, this.mUser != null ? this.mUser : Process.myUserHandle());
    }

    public Context createPackageContextAsUser(String packageName, int flags, UserHandle user) throws NameNotFoundException {
        if (packageName.equals("system") || packageName.equals("android")) {
            return new ContextImpl(this, this.mMainThread, this.mPackageInfo, null, this.mActivityToken, user, flags, null);
        } else if (this.mResources == null) {
            throw new NameNotFoundException("Application package " + packageName + " resources not found");
        } else {
            LoadedApk pi = this.mMainThread.getPackageInfo(packageName, this.mResources.getCompatibilityInfo(), 1073741824 | flags, user.getIdentifier());
            if (pi != null) {
                ContextImpl c = new ContextImpl(this, this.mMainThread, pi, null, this.mActivityToken, user, flags, null);
                int displayId = this.mDisplay != null ? this.mDisplay.getDisplayId() : 0;
                c.setResources(createResources(this.mActivityToken, pi, null, displayId, null, getDisplayAdjustments(displayId).getCompatibilityInfo()));
                if (c.mResources != null) {
                    return c;
                }
            }
            throw new NameNotFoundException("Application package " + packageName + " not found");
        }
    }

    public Context createContextForSplit(String splitName) throws NameNotFoundException {
        if (!this.mPackageInfo.getApplicationInfo().requestsIsolatedSplitLoading()) {
            return this;
        }
        ClassLoader classLoader = this.mPackageInfo.getSplitClassLoader(splitName);
        String[] paths = this.mPackageInfo.getSplitPaths(splitName);
        ContextImpl context = new ContextImpl(this, this.mMainThread, this.mPackageInfo, splitName, this.mActivityToken, this.mUser, this.mFlags, classLoader);
        context.setResources(ResourcesManager.getInstance().getResources(this.mActivityToken, this.mPackageInfo.getResDir(), paths, this.mPackageInfo.getOverlayDirs(), this.mPackageInfo.getApplicationInfo().sharedLibraryFiles, this.mDisplay != null ? this.mDisplay.getDisplayId() : 0, null, this.mPackageInfo.getCompatibilityInfo(), classLoader));
        return context;
    }

    public Context createConfigurationContext(Configuration overrideConfiguration) {
        if (overrideConfiguration == null) {
            throw new IllegalArgumentException("overrideConfiguration must not be null");
        }
        ContextImpl context = new ContextImpl(this, this.mMainThread, this.mPackageInfo, this.mSplitName, this.mActivityToken, this.mUser, this.mFlags, this.mClassLoader);
        int displayId = this.mDisplay != null ? this.mDisplay.getDisplayId() : 0;
        context.setResources(createResources(this.mActivityToken, this.mPackageInfo, this.mSplitName, displayId, overrideConfiguration, getDisplayAdjustments(displayId).getCompatibilityInfo()));
        return context;
    }

    public Context createDisplayContext(Display display) {
        if (display == null) {
            throw new IllegalArgumentException("display must not be null");
        }
        ContextImpl context = new ContextImpl(this, this.mMainThread, this.mPackageInfo, this.mSplitName, this.mActivityToken, this.mUser, this.mFlags, this.mClassLoader);
        int displayId = display.getDisplayId();
        context.setResources(createResources(this.mActivityToken, this.mPackageInfo, this.mSplitName, displayId, null, getDisplayAdjustments(displayId).getCompatibilityInfo()));
        context.mDisplay = display;
        return context;
    }

    public Context createDeviceProtectedStorageContext() {
        return new ContextImpl(this, this.mMainThread, this.mPackageInfo, this.mSplitName, this.mActivityToken, this.mUser, (this.mFlags & -17) | 8, this.mClassLoader);
    }

    public Context createCredentialProtectedStorageContext() {
        return new ContextImpl(this, this.mMainThread, this.mPackageInfo, this.mSplitName, this.mActivityToken, this.mUser, (this.mFlags & -9) | 16, this.mClassLoader);
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
        return this.mDisplay == null ? -1 : this.mDisplay.getDisplayId();
    }

    public void updateDisplay(int displayId) {
        this.mDisplay = this.mResourcesManager.getAdjustedDisplay(displayId, this.mResources);
    }

    public DisplayAdjustments getDisplayAdjustments(int displayId) {
        return this.mResources.getDisplayAdjustments();
    }

    public File getDataDir() {
        if (this.mPackageInfo != null) {
            File res;
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
            setFilePermissionsFromMode(file.getPath(), mode, 505);
        }
        return file;
    }

    public int getUserId() {
        return this.mUser.getIdentifier();
    }

    static ContextImpl createSystemContext(ActivityThread mainThread) {
        LoadedApk packageInfo = new LoadedApk(mainThread);
        ContextImpl context = new ContextImpl(null, mainThread, packageInfo, null, null, null, 0, null);
        context.setResources(packageInfo.getResources());
        if (HwPCUtils.isValidExtDisplayId(mainThread.mDisplayId)) {
            DisplayMetrics metrics;
            if (mainThread.mOverrideConfig == null || (mainThread.mOverrideConfig.equals(Configuration.EMPTY) ^ 1) == 0) {
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
        ContextImpl context = new ContextImpl(null, systemContext.mMainThread, packageInfo, null, null, null, 0, null);
        context.setResources(createResources(null, packageInfo, null, 0, null, packageInfo.getCompatibilityInfo()));
        return context;
    }

    static ContextImpl createAppContext(ActivityThread mainThread, LoadedApk packageInfo) {
        if (packageInfo == null) {
            throw new IllegalArgumentException("packageInfo");
        } else if (HwPCUtils.isPcCastMode() && HwPCUtils.isValidExtDisplayId(mainThread.mDisplayId)) {
            return createHwPcAppContext(mainThread, packageInfo);
        } else {
            ContextImpl context = new ContextImpl(null, mainThread, packageInfo, null, null, null, 0, null);
            context.setResources(packageInfo.getResources());
            return context;
        }
    }

    static ContextImpl createHwPcAppContext(ActivityThread mainThread, LoadedApk packageInfo) {
        CompatibilityInfo compatInfo;
        ContextImpl context = new ContextImpl(null, mainThread, packageInfo, null, null, null, 0, null);
        int displayId = mainThread.mDisplayId;
        if (displayId == 0) {
            compatInfo = packageInfo.getCompatibilityInfo();
        } else {
            compatInfo = CompatibilityInfo.DEFAULT_COMPATIBILITY_INFO;
        }
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
        if (packageInfo == null) {
            throw new IllegalArgumentException("packageInfo");
        }
        CompatibilityInfo compatInfo;
        String[] splitDirs = packageInfo.getSplitResDirs();
        ClassLoader classLoader = packageInfo.getClassLoader();
        if (packageInfo.getApplicationInfo().requestsIsolatedSplitLoading()) {
            Trace.traceBegin(8192, "SplitDependencies");
            try {
                classLoader = packageInfo.getSplitClassLoader(activityInfo.splitName);
                splitDirs = packageInfo.getSplitPaths(activityInfo.splitName);
                Trace.traceEnd(8192);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            } catch (Throwable th) {
                Trace.traceEnd(8192);
            }
        }
        ContextImpl context = new ContextImpl(null, mainThread, packageInfo, activityInfo.splitName, activityToken, null, 0, classLoader);
        if (displayId == -1) {
            displayId = 0;
        }
        if (displayId == 0) {
            compatInfo = packageInfo.getCompatibilityInfo();
        } else {
            compatInfo = CompatibilityInfo.DEFAULT_COMPATIBILITY_INFO;
        }
        ResourcesManager resourcesManager = ResourcesManager.getInstance();
        context.setResources(resourcesManager.createBaseActivityResources(activityToken, packageInfo.getResDir(), splitDirs, packageInfo.getOverlayDirs(), packageInfo.getApplicationInfo().sharedLibraryFiles, displayId, overrideConfiguration, compatInfo, classLoader));
        context.mDisplay = resourcesManager.getAdjustedDisplay(displayId, context.getResources());
        return context;
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
        if (user == null) {
            user = Process.myUserHandle();
        }
        this.mUser = user;
        this.mPackageInfo = packageInfo;
        this.mSplitName = splitName;
        this.mClassLoader = classLoader;
        this.mResourcesManager = ResourcesManager.getInstance();
        if (container != null) {
            this.mBasePackageName = container.mBasePackageName;
            this.mOpPackageName = container.mOpPackageName;
            setResources(container.mResources);
            this.mDisplay = container.mDisplay;
            if (this.mResources != null) {
                this.mResources.getImpl().getHwResourcesImpl().setPackageName(this.mPackageInfo.getPackageName());
                this.mResources.setPackageName(this.mPackageInfo.getPackageName());
            }
        } else {
            this.mBasePackageName = packageInfo.mPackageName;
            ApplicationInfo ainfo = packageInfo.getApplicationInfo();
            if (ainfo.uid != 1000 || ainfo.uid == Process.myUid()) {
                this.mOpPackageName = this.mBasePackageName;
            } else {
                this.mOpPackageName = ActivityThread.currentPackageName();
            }
        }
        this.mContentResolver = new ApplicationContentResolver(this, mainThread, user);
    }

    void setResources(Resources r) {
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

    void installSystemApplicationInfo(ApplicationInfo info, ClassLoader classLoader) {
        this.mPackageInfo.installSystemApplicationInfo(info, classLoader);
    }

    final void scheduleFinalCleanup(String who, String what) {
        this.mMainThread.scheduleContextCleanup(this, who, what);
    }

    final void performFinalCleanup(String who, String what) {
        this.mPackageInfo.removeContextRegistrations(getOuterContext(), who, what);
    }

    final Context getReceiverRestrictedContext() {
        if (this.mReceiverRestrictedContext != null) {
            return this.mReceiverRestrictedContext;
        }
        Context receiverRestrictedContext = new ReceiverRestrictedContext(getOuterContext());
        this.mReceiverRestrictedContext = receiverRestrictedContext;
        return receiverRestrictedContext;
    }

    final void setOuterContext(Context context) {
        this.mOuterContext = context;
    }

    final Context getOuterContext() {
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
            return new File(base, name);
        }
        throw new IllegalArgumentException("File " + name + " contains a path separator");
    }

    private File[] ensureExternalDirsExistOrFilter(File[] dirs) {
        File[] result = new File[dirs.length];
        for (int i = 0; i < dirs.length; i++) {
            File dir = dirs[i];
            if (!(dir.exists() || dir.mkdirs() || dir.exists())) {
                String path = dir.getAbsolutePath();
                if (path == null || !path.startsWith(UNMOUNT_PATH)) {
                    try {
                        int res = Stub.asInterface(ServiceManager.getService("mount")).mkdirs(getPackageName(), path);
                        if (res != 0) {
                            Log.w(TAG, "Failed to ensure " + dir + ": " + res);
                            dir = null;
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "Failed to ensure " + dir + ": " + e);
                        dir = null;
                    }
                } else {
                    Log.w(TAG, "disk  is not mounted");
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
}
