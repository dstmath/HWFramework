package android.app;

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
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.hsm.HwSystemManager;
import android.hwtheme.HwThemeManager;
import android.location.LocationRequest;
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
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.storage.IMountService.Stub;
import android.provider.DocumentsContract.Document;
import android.security.keymaster.KeymasterDefs;
import android.service.notification.ZenModeConfig;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.util.AndroidRuntimeException;
import android.util.ArrayMap;
import android.util.Flog;
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
import java.util.Objects;

public class ContextImpl extends Context {
    private static final boolean DBG_PRELOAD = false;
    private static final boolean DEBUG = false;
    private static final boolean IS_SWITCH_SD_ENABLED = false;
    private static final String TAG = "ContextImpl";
    private static final String TAG_PRELOAD = "PreloadSharedPrefs";
    private static final String UNMOUNT_PATH = "/dev/null";
    @GuardedBy("ContextImpl.class")
    private static ArrayMap<String, ArrayMap<File, SharedPreferencesImpl>> sSharedPrefsCache;
    private final IBinder mActivityToken;
    private final String mBasePackageName;
    @GuardedBy("mSync")
    private File mCacheDir;
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
    private Context mOuterContext;
    final LoadedApk mPackageInfo;
    private PackageManager mPackageManager;
    @GuardedBy("mSync")
    private File mPreferencesDir;
    private Context mReceiverRestrictedContext;
    private final Resources mResources;
    private final ResourcesManager mResourcesManager;
    final Object[] mServiceCache;
    @GuardedBy("ContextImpl.class")
    private ArrayMap<String, File> mSharedPrefsPaths;
    private final Object mSync;
    private Theme mTheme;
    private int mThemeResource;
    private final UserHandle mUser;

    /* renamed from: android.app.ContextImpl.1 */
    static class AnonymousClass1 implements FilenameFilter {
        final /* synthetic */ String val$prefix;

        AnonymousClass1(String val$prefix) {
            this.val$prefix = val$prefix;
        }

        public boolean accept(File dir, String name) {
            return name.startsWith(this.val$prefix);
        }
    }

    /* renamed from: android.app.ContextImpl.2 */
    class AnonymousClass2 extends Thread {
        AnonymousClass2(String $anonymous0) {
            super($anonymous0);
        }

        public void run() {
            String[] names = ContextImpl.this.getPreferencesDir().list(new FilenameFilter() {
                public boolean accept(File dir, String filename) {
                    boolean z = ContextImpl.IS_SWITCH_SD_ENABLED;
                    if (ContextImpl.DBG_PRELOAD) {
                        Log.d(ContextImpl.TAG_PRELOAD, "Get " + filename);
                    }
                    if (!filename.endsWith(".xml")) {
                        return ContextImpl.IS_SWITCH_SD_ENABLED;
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
    }

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
            return this.mMainThread.acquireProvider(c, ContentProvider.getAuthorityWithoutUserId(auth), resolveUserIdFromAuthority(auth), ContextImpl.IS_SWITCH_SD_ENABLED);
        }

        public boolean releaseUnstableProvider(IContentProvider icp) {
            return this.mMainThread.releaseProvider(icp, ContextImpl.IS_SWITCH_SD_ENABLED);
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.app.ContextImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.app.ContextImpl.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.app.ContextImpl.<clinit>():void");
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
        if (this.mThemeResource != resId) {
            this.mThemeResource = resId;
            initializeTheme();
        }
    }

    public int getThemeResId() {
        return this.mThemeResource;
    }

    public Theme getTheme() {
        if (this.mTheme != null) {
            return this.mTheme;
        }
        this.mThemeResource = Resources.selectDefaultTheme(this.mThemeResource, getOuterContext().getApplicationInfo().targetSdkVersion);
        initializeTheme();
        return this.mTheme;
    }

    private void initializeTheme() {
        if (this.mTheme == null) {
            this.mTheme = this.mResources.newTheme();
        }
        this.mTheme.applyStyle(this.mThemeResource, true);
    }

    public ClassLoader getClassLoader() {
        return this.mPackageInfo != null ? this.mPackageInfo.getClassLoader() : ClassLoader.getSystemClassLoader();
    }

    public String getPackageName() {
        if (this.mPackageInfo != null) {
            return this.mPackageInfo.getPackageName();
        }
        return ZenModeConfig.SYSTEM_AUTHORITY;
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

    public SharedPreferences getSharedPreferences(File file, int mode) {
        checkMode(mode);
        synchronized (ContextImpl.class) {
            ArrayMap<File, SharedPreferencesImpl> cache = getSharedPreferencesCacheLocked();
            SharedPreferencesImpl sp = (SharedPreferencesImpl) cache.get(file);
            if (sp == null) {
                sp = new SharedPreferencesImpl(file, mode);
                cache.put(file, sp);
                return sp;
            }
            sp.setMode(mode);
            if ((mode & 4) != 0 || getApplicationInfo().targetSdkVersion < 11) {
                sp.startReloadIfChangedUnexpectedly();
            }
            return sp;
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

    private static int moveFiles(File sourceDir, File targetDir, String prefix) {
        File[] sourceFiles = FileUtils.listFilesOrEmpty(sourceDir, new AnonymousClass1(prefix));
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
        boolean z = IS_SWITCH_SD_ENABLED;
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
        boolean z;
        synchronized (ContextImpl.class) {
            File prefs = getSharedPreferencesPath(name);
            File prefsBackup = SharedPreferencesImpl.makeBackupFile(prefs);
            getSharedPreferencesCacheLocked().remove(prefs);
            prefs.delete();
            prefsBackup.delete();
            z = (prefs.exists() || prefsBackup.exists()) ? IS_SWITCH_SD_ENABLED : true;
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

    public void preloadSharedPrefs() {
        new AnonymousClass2("ContextImpl-preloadSharedPrefs").start();
    }

    public FileInputStream openFileInput(String name) throws FileNotFoundException {
        return new FileInputStream(makeFilename(getFilesDir(), name));
    }

    public FileOutputStream openFileOutput(String name, int mode) throws FileNotFoundException {
        FileOutputStream fos;
        checkMode(mode);
        boolean append = (Document.FLAG_ARCHIVE & mode) != 0 ? true : IS_SWITCH_SD_ENABLED;
        File f = makeFilename(getFilesDir(), name);
        try {
            fos = new FileOutputStream(f, append);
            setFilePermissionsFromMode(f.getPath(), mode, 0);
            return fos;
        } catch (FileNotFoundException e) {
            File parent = f.getParentFile();
            parent.mkdir();
            FileUtils.setPermissions(parent.getPath(), (int) IActivityManager.IS_PACKAGE_CLONED_TRANSACTION, -1, -1);
            fos = new FileOutputStream(f, append);
            setFilePermissionsFromMode(f.getPath(), mode, 0);
            return fos;
        }
    }

    public boolean deleteFile(String name) {
        return makeFilename(getFilesDir(), name).delete();
    }

    private static File ensurePrivateDirExists(File file) {
        if (!file.exists()) {
            try {
                Os.mkdir(file.getAbsolutePath(), IActivityManager.IS_PACKAGE_CLONED_TRANSACTION);
                Os.chmod(file.getAbsolutePath(), IActivityManager.IS_PACKAGE_CLONED_TRANSACTION);
            } catch (ErrnoException e) {
                if (e.errno != OsConstants.EEXIST) {
                    Log.w(TAG, "Failed to ensure " + file + ": " + e.getMessage());
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
        File ensurePrivateDirExists;
        synchronized (this.mSync) {
            if (this.mCacheDir == null) {
                this.mCacheDir = new File(getDataDir(), "cache");
            }
            ensurePrivateDirExists = ensurePrivateDirExists(this.mCacheDir);
        }
        return ensurePrivateDirExists;
    }

    public File getCodeCacheDir() {
        File ensurePrivateDirExists;
        synchronized (this.mSync) {
            if (this.mCodeCacheDir == null) {
                this.mCodeCacheDir = new File(getDataDir(), "code_cache");
            }
            ensurePrivateDirExists = ensurePrivateDirExists(this.mCodeCacheDir);
        }
        return ensurePrivateDirExists;
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
        int flags = KeymasterDefs.KM_ENUM;
        if ((mode & 8) != 0) {
            flags = KeymasterDefs.KM_UINT;
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
            z = moveFiles(source.getParentFile(), getDatabasePath(name).getParentFile(), source.getName()) != -1 ? true : IS_SWITCH_SD_ENABLED;
        }
        return z;
    }

    public boolean deleteDatabase(String name) {
        try {
            return SQLiteDatabase.deleteDatabase(getDatabasePath(name));
        } catch (Exception e) {
            return IS_SWITCH_SD_ENABLED;
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
        FileUtils.setPermissions(dir.getPath(), (int) IActivityManager.IS_PACKAGE_CLONED_TRANSACTION, -1, -1);
        return f;
    }

    public String[] databaseList() {
        return FileUtils.listOrEmpty(getDatabasesDir());
    }

    private File getDatabasesDir() {
        File ensurePrivateDirExists;
        synchronized (this.mSync) {
            if (this.mDatabasesDir == null) {
                if (ZenModeConfig.SYSTEM_AUTHORITY.equals(getPackageName())) {
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
            if ((intent.getFlags() & KeymasterDefs.KM_ENUM) == 0 && options != null && ActivityOptions.fromBundle(options).getLaunchTaskId() == -1) {
                throw new AndroidRuntimeException("Calling startActivity() from outside of an Activity  context requires the FLAG_ACTIVITY_NEW_TASK flag. Is this really what you want?");
            }
            this.mMainThread.getInstrumentation().execStartActivity(getOuterContext(), this.mMainThread.getApplicationThread(), null, (Activity) null, intent, -1, options);
            return;
        }
        Log.i(TAG, "this app not allowed to start activity:" + intent);
    }

    public void startActivityAsUser(Intent intent, Bundle options, UserHandle user) {
        try {
            if (Jlog.isPerfTest()) {
                Jlog.i(2022, "whopid=" + Process.myPid() + "&whopkg=" + getPackageName() + "&" + Intent.toPkgClsString(intent));
            }
            ActivityManagerNative.getDefault().startActivityAsUser(this.mMainThread.getApplicationThread(), getBasePackageName(), intent, intent.resolveTypeIfNeeded(getContentResolver()), null, null, 0, KeymasterDefs.KM_ENUM, null, options, user.getIdentifier());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void startActivities(Intent[] intents) {
        warnIfCallingFromSystemProcess();
        startActivities(intents, null);
    }

    public void startActivitiesAsUser(Intent[] intents, Bundle options, UserHandle userHandle) {
        if ((intents[0].getFlags() & KeymasterDefs.KM_ENUM) == 0) {
            throw new AndroidRuntimeException("Calling startActivities() from outside of an Activity  context requires the FLAG_ACTIVITY_NEW_TASK flag on first Intent. Is this really what you want?");
        }
        this.mMainThread.getInstrumentation().execStartActivitiesAsUser(getOuterContext(), this.mMainThread.getApplicationThread(), null, (Activity) null, intents, options, userHandle.getIdentifier());
    }

    public void startActivities(Intent[] intents, Bundle options) {
        warnIfCallingFromSystemProcess();
        if ((intents[0].getFlags() & KeymasterDefs.KM_ENUM) == 0) {
            throw new AndroidRuntimeException("Calling startActivities() from outside of an Activity  context requires the FLAG_ACTIVITY_NEW_TASK flag on first Intent. Is this really what you want?");
        }
        this.mMainThread.getInstrumentation().execStartActivities(getOuterContext(), this.mMainThread.getApplicationThread(), null, (Activity) null, intents, options);
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
        int result = ActivityManagerNative.getDefault().startActivityIntentSender(this.mMainThread.getApplicationThread(), intent, fillInIntent, resolvedType, null, null, 0, flagsMask, flagsValues, options);
        if (result == -6) {
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
                ActivityManagerNative.getDefault().broadcastIntent(this.mMainThread.getApplicationThread(), intent, resolvedType, null, -1, null, null, null, -1, null, IS_SWITCH_SD_ENABLED, IS_SWITCH_SD_ENABLED, getUserId());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void sendBroadcast(Intent intent, String receiverPermission) {
        warnIfCallingFromSystemProcess();
        if (HwSystemManager.canSendBroadcast(this, intent)) {
            String resolvedType = intent.resolveTypeIfNeeded(getContentResolver());
            String[] strArr = receiverPermission == null ? null : new String[]{receiverPermission};
            try {
                intent.prepareToLeaveProcess((Context) this);
                ActivityManagerNative.getDefault().broadcastIntent(this.mMainThread.getApplicationThread(), intent, resolvedType, null, -1, null, null, strArr, -1, null, IS_SWITCH_SD_ENABLED, IS_SWITCH_SD_ENABLED, getUserId());
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
            ActivityManagerNative.getDefault().broadcastIntent(this.mMainThread.getApplicationThread(), intent, resolvedType, null, -1, null, null, receiverPermissions, -1, null, IS_SWITCH_SD_ENABLED, IS_SWITCH_SD_ENABLED, getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void sendBroadcast(Intent intent, String receiverPermission, Bundle options) {
        warnIfCallingFromSystemProcess();
        String resolvedType = intent.resolveTypeIfNeeded(getContentResolver());
        String[] strArr = receiverPermission == null ? null : new String[]{receiverPermission};
        try {
            intent.prepareToLeaveProcess((Context) this);
            ActivityManagerNative.getDefault().broadcastIntent(this.mMainThread.getApplicationThread(), intent, resolvedType, null, -1, null, null, strArr, -1, options, IS_SWITCH_SD_ENABLED, IS_SWITCH_SD_ENABLED, getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void sendBroadcast(Intent intent, String receiverPermission, int appOp) {
        warnIfCallingFromSystemProcess();
        if (HwSystemManager.canSendBroadcast(this, intent)) {
            String resolvedType = intent.resolveTypeIfNeeded(getContentResolver());
            String[] strArr = receiverPermission == null ? null : new String[]{receiverPermission};
            try {
                intent.prepareToLeaveProcess((Context) this);
                ActivityManagerNative.getDefault().broadcastIntent(this.mMainThread.getApplicationThread(), intent, resolvedType, null, -1, null, null, strArr, appOp, null, IS_SWITCH_SD_ENABLED, IS_SWITCH_SD_ENABLED, getUserId());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void sendOrderedBroadcast(Intent intent, String receiverPermission) {
        warnIfCallingFromSystemProcess();
        String resolvedType = intent.resolveTypeIfNeeded(getContentResolver());
        String[] strArr = receiverPermission == null ? null : new String[]{receiverPermission};
        try {
            intent.prepareToLeaveProcess((Context) this);
            ActivityManagerNative.getDefault().broadcastIntent(this.mMainThread.getApplicationThread(), intent, resolvedType, null, -1, null, null, strArr, -1, null, true, IS_SWITCH_SD_ENABLED, getUserId());
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
            IIntentReceiver iIntentReceiver = null;
            if (resultReceiver != null) {
                if (this.mPackageInfo != null) {
                    if (scheduler == null) {
                        scheduler = this.mMainThread.getHandler();
                    }
                    iIntentReceiver = this.mPackageInfo.getReceiverDispatcher(resultReceiver, getOuterContext(), scheduler, this.mMainThread.getInstrumentation(), IS_SWITCH_SD_ENABLED);
                } else {
                    if (scheduler == null) {
                        scheduler = this.mMainThread.getHandler();
                    }
                    iIntentReceiver = new ReceiverDispatcher(resultReceiver, getOuterContext(), scheduler, null, IS_SWITCH_SD_ENABLED).getIIntentReceiver();
                }
            }
            String resolvedType = intent.resolveTypeIfNeeded(getContentResolver());
            String[] strArr = receiverPermission == null ? null : new String[]{receiverPermission};
            try {
                intent.prepareToLeaveProcess((Context) this);
                ActivityManagerNative.getDefault().broadcastIntent(this.mMainThread.getApplicationThread(), intent, resolvedType, iIntentReceiver, initialCode, initialData, initialExtras, strArr, appOp, options, true, IS_SWITCH_SD_ENABLED, getUserId());
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
                ActivityManagerNative.getDefault().broadcastIntent(this.mMainThread.getApplicationThread(), intent, resolvedType, null, -1, null, null, null, -1, null, IS_SWITCH_SD_ENABLED, IS_SWITCH_SD_ENABLED, user.getIdentifier());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void sendBroadcastAsUser(Intent intent, UserHandle user, String receiverPermission) {
        sendBroadcastAsUser(intent, user, receiverPermission, -1);
    }

    public void sendBroadcastAsUser(Intent intent, UserHandle user, String receiverPermission, int appOp) {
        if (HwSystemManager.canSendBroadcast(this, intent)) {
            String resolvedType = intent.resolveTypeIfNeeded(getContentResolver());
            String[] strArr = receiverPermission == null ? null : new String[]{receiverPermission};
            try {
                intent.prepareToLeaveProcess((Context) this);
                ActivityManagerNative.getDefault().broadcastIntent(this.mMainThread.getApplicationThread(), intent, resolvedType, null, -1, null, null, strArr, appOp, null, IS_SWITCH_SD_ENABLED, IS_SWITCH_SD_ENABLED, user.getIdentifier());
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
            IIntentReceiver iIntentReceiver = null;
            if (resultReceiver != null) {
                if (this.mPackageInfo != null) {
                    if (scheduler == null) {
                        scheduler = this.mMainThread.getHandler();
                    }
                    iIntentReceiver = this.mPackageInfo.getReceiverDispatcher(resultReceiver, getOuterContext(), scheduler, this.mMainThread.getInstrumentation(), IS_SWITCH_SD_ENABLED);
                } else {
                    if (scheduler == null) {
                        scheduler = this.mMainThread.getHandler();
                    }
                    iIntentReceiver = new ReceiverDispatcher(resultReceiver, getOuterContext(), scheduler, null, IS_SWITCH_SD_ENABLED).getIIntentReceiver();
                }
            }
            String resolvedType = intent.resolveTypeIfNeeded(getContentResolver());
            String[] strArr = receiverPermission == null ? null : new String[]{receiverPermission};
            try {
                intent.prepareToLeaveProcess((Context) this);
                ActivityManagerNative.getDefault().broadcastIntent(this.mMainThread.getApplicationThread(), intent, resolvedType, iIntentReceiver, initialCode, initialData, initialExtras, strArr, appOp, options, true, IS_SWITCH_SD_ENABLED, user.getIdentifier());
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
                ActivityManagerNative.getDefault().broadcastIntent(this.mMainThread.getApplicationThread(), intent, resolvedType, null, -1, null, null, null, -1, null, IS_SWITCH_SD_ENABLED, true, getUserId());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    @Deprecated
    public void sendStickyOrderedBroadcast(Intent intent, BroadcastReceiver resultReceiver, Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {
        warnIfCallingFromSystemProcess();
        if (HwSystemManager.canSendBroadcast(this, intent)) {
            IIntentReceiver iIntentReceiver = null;
            if (resultReceiver != null) {
                if (this.mPackageInfo != null) {
                    if (scheduler == null) {
                        scheduler = this.mMainThread.getHandler();
                    }
                    iIntentReceiver = this.mPackageInfo.getReceiverDispatcher(resultReceiver, getOuterContext(), scheduler, this.mMainThread.getInstrumentation(), IS_SWITCH_SD_ENABLED);
                } else {
                    if (scheduler == null) {
                        scheduler = this.mMainThread.getHandler();
                    }
                    iIntentReceiver = new ReceiverDispatcher(resultReceiver, getOuterContext(), scheduler, null, IS_SWITCH_SD_ENABLED).getIIntentReceiver();
                }
            }
            String resolvedType = intent.resolveTypeIfNeeded(getContentResolver());
            try {
                intent.prepareToLeaveProcess((Context) this);
                ActivityManagerNative.getDefault().broadcastIntent(this.mMainThread.getApplicationThread(), intent, resolvedType, iIntentReceiver, initialCode, initialData, initialExtras, null, -1, null, true, true, getUserId());
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
            ActivityManagerNative.getDefault().unbroadcastIntent(this.mMainThread.getApplicationThread(), intent, getUserId());
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
                ActivityManagerNative.getDefault().broadcastIntent(this.mMainThread.getApplicationThread(), intent, resolvedType, null, -1, null, null, null, -1, null, IS_SWITCH_SD_ENABLED, true, user.getIdentifier());
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
            ActivityManagerNative.getDefault().broadcastIntent(this.mMainThread.getApplicationThread(), intent, resolvedType, null, -1, null, null, null, -1, options, IS_SWITCH_SD_ENABLED, true, user.getIdentifier());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public void sendStickyOrderedBroadcastAsUser(Intent intent, UserHandle user, BroadcastReceiver resultReceiver, Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {
        if (HwSystemManager.canSendBroadcast(this, intent)) {
            IIntentReceiver iIntentReceiver = null;
            if (resultReceiver != null) {
                if (this.mPackageInfo != null) {
                    if (scheduler == null) {
                        scheduler = this.mMainThread.getHandler();
                    }
                    iIntentReceiver = this.mPackageInfo.getReceiverDispatcher(resultReceiver, getOuterContext(), scheduler, this.mMainThread.getInstrumentation(), IS_SWITCH_SD_ENABLED);
                } else {
                    if (scheduler == null) {
                        scheduler = this.mMainThread.getHandler();
                    }
                    iIntentReceiver = new ReceiverDispatcher(resultReceiver, getOuterContext(), scheduler, null, IS_SWITCH_SD_ENABLED).getIIntentReceiver();
                }
            }
            String resolvedType = intent.resolveTypeIfNeeded(getContentResolver());
            try {
                intent.prepareToLeaveProcess((Context) this);
                ActivityManagerNative.getDefault().broadcastIntent(this.mMainThread.getApplicationThread(), intent, resolvedType, iIntentReceiver, initialCode, initialData, initialExtras, null, -1, null, true, true, user.getIdentifier());
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
            ActivityManagerNative.getDefault().unbroadcastIntent(this.mMainThread.getApplicationThread(), intent, user.getIdentifier());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        return registerReceiver(receiver, filter, null, null);
    }

    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter, String broadcastPermission, Handler scheduler) {
        return registerReceiverInternal(receiver, getUserId(), filter, broadcastPermission, scheduler, getOuterContext());
    }

    public Intent registerReceiverAsUser(BroadcastReceiver receiver, UserHandle user, IntentFilter filter, String broadcastPermission, Handler scheduler) {
        return registerReceiverInternal(receiver, user.getIdentifier(), filter, broadcastPermission, scheduler, getOuterContext());
    }

    private Intent registerReceiverInternal(BroadcastReceiver receiver, int userId, IntentFilter filter, String broadcastPermission, Handler scheduler, Context context) {
        IIntentReceiver iIntentReceiver = null;
        if (receiver != null) {
            if (this.mPackageInfo == null || context == null) {
                if (scheduler == null) {
                    scheduler = this.mMainThread.getHandler();
                }
                iIntentReceiver = new ReceiverDispatcher(receiver, context, scheduler, null, true).getIIntentReceiver();
            } else {
                if (scheduler == null) {
                    scheduler = this.mMainThread.getHandler();
                }
                iIntentReceiver = this.mPackageInfo.getReceiverDispatcher(receiver, context, scheduler, this.mMainThread.getInstrumentation(), true);
            }
        }
        try {
            Intent intent = ActivityManagerNative.getDefault().registerReceiver(this.mMainThread.getApplicationThread(), this.mBasePackageName, iIntentReceiver, filter, broadcastPermission, userId);
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
                ActivityManagerNative.getDefault().unregisterReceiver(this.mPackageInfo.forgetReceiverDispatcher(getOuterContext(), receiver));
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
        return startServiceCommon(service, this.mUser);
    }

    public boolean stopService(Intent service) {
        warnIfCallingFromSystemProcess();
        return stopServiceCommon(service, this.mUser);
    }

    public ComponentName startServiceAsUser(Intent service, UserHandle user) {
        return startServiceCommon(service, user);
    }

    private ComponentName startServiceCommon(Intent service, UserHandle user) {
        try {
            validateServiceIntent(service);
            service.prepareToLeaveProcess((Context) this);
            ComponentName cn = ActivityManagerNative.getDefault().startService(this.mMainThread.getApplicationThread(), service, service.resolveTypeIfNeeded(getContentResolver()), getOpPackageName(), user.getIdentifier());
            if (cn != null) {
                if (cn.getPackageName().equals("!")) {
                    throw new SecurityException("Not allowed to start service " + service + " without permission " + cn.getClassName());
                } else if (cn.getPackageName().equals("!!")) {
                    throw new SecurityException("Unable to start service " + service + ": " + cn.getClassName());
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
            int res = ActivityManagerNative.getDefault().stopService(this.mMainThread.getApplicationThread(), service, service.resolveTypeIfNeeded(getContentResolver()), user.getIdentifier());
            if (res < 0) {
                throw new SecurityException("Not allowed to stop service " + service);
            } else if (res != 0) {
                return true;
            } else {
                return IS_SWITCH_SD_ENABLED;
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
                int res = ActivityManagerNative.getDefault().bindService(this.mMainThread.getApplicationThread(), getActivityToken(), service, service.resolveTypeIfNeeded(getContentResolver()), sd, flags, getOpPackageName(), user.getIdentifier());
                if (res >= 0) {
                    return res != 0 ? true : IS_SWITCH_SD_ENABLED;
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
                ActivityManagerNative.getDefault().unbindService(this.mPackageInfo.forgetServiceDispatcher(getOuterContext(), conn));
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
                arguments.setAllowFds(IS_SWITCH_SD_ENABLED);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
        return ActivityManagerNative.getDefault().startInstrumentation(className, profileFile, 0, arguments, null, null, getUserId(), null);
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
        try {
            return ActivityManagerNative.getDefault().checkPermission(permission, pid, uid);
        } catch (RemoteException e) {
            Flog.e(LocationRequest.POWER_LOW, "checkPermission " + permission + ", for pid " + pid + ", uid " + pid);
            throw e.rethrowFromSystemServer();
        }
    }

    public int checkPermission(String permission, int pid, int uid, IBinder callerToken) {
        if (permission == null) {
            throw new IllegalArgumentException("permission is null");
        }
        try {
            return ActivityManagerNative.getDefault().checkPermissionWithToken(permission, pid, uid, callerToken);
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
        enforce(permission, checkPermission(permission, pid, uid), IS_SWITCH_SD_ENABLED, uid, message);
    }

    public void enforceCallingPermission(String permission, String message) {
        enforce(permission, checkCallingPermission(permission), IS_SWITCH_SD_ENABLED, Binder.getCallingUid(), message);
    }

    public void enforceCallingOrSelfPermission(String permission, String message) {
        enforce(permission, checkCallingOrSelfPermission(permission), true, Binder.getCallingUid(), message);
    }

    public void grantUriPermission(String toPackage, Uri uri, int modeFlags) {
        try {
            ActivityManagerNative.getDefault().grantUriPermission(this.mMainThread.getApplicationThread(), toPackage, ContentProvider.getUriWithoutUserId(uri), modeFlags, resolveUserId(uri));
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void revokeUriPermission(Uri uri, int modeFlags) {
        try {
            ActivityManagerNative.getDefault().revokeUriPermission(this.mMainThread.getApplicationThread(), ContentProvider.getUriWithoutUserId(uri), modeFlags, resolveUserId(uri));
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int checkUriPermission(Uri uri, int pid, int uid, int modeFlags) {
        try {
            return ActivityManagerNative.getDefault().checkUriPermission(ContentProvider.getUriWithoutUserId(uri), pid, uid, modeFlags, resolveUserId(uri), null);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int checkUriPermission(Uri uri, int pid, int uid, int modeFlags, IBinder callerToken) {
        try {
            return ActivityManagerNative.getDefault().checkUriPermission(ContentProvider.getUriWithoutUserId(uri), pid, uid, modeFlags, resolveUserId(uri), callerToken);
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
        if ((uriModeFlags & KeymasterDefs.KM_ALGORITHM_HMAC) != 0) {
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
        enforceForUri(modeFlags, checkUriPermission(uri, pid, uid, modeFlags), IS_SWITCH_SD_ENABLED, uid, uri, message);
    }

    public void enforceCallingUriPermission(Uri uri, int modeFlags, String message) {
        enforceForUri(modeFlags, checkCallingUriPermission(uri, modeFlags), IS_SWITCH_SD_ENABLED, Binder.getCallingUid(), uri, message);
    }

    public void enforceCallingOrSelfUriPermission(Uri uri, int modeFlags, String message) {
        enforceForUri(modeFlags, checkCallingOrSelfUriPermission(uri, modeFlags), true, Binder.getCallingUid(), uri, message);
    }

    public void enforceUriPermission(Uri uri, String readPermission, String writePermission, int pid, int uid, int modeFlags, String message) {
        enforceForUri(modeFlags, checkUriPermission(uri, readPermission, writePermission, pid, uid, modeFlags), IS_SWITCH_SD_ENABLED, uid, uri, message);
    }

    private void warnIfCallingFromSystemProcess() {
        if (Process.myUid() == Process.SYSTEM_UID) {
            Slog.w(TAG, "Calling a method in the system process without a qualified user: " + Debug.getCallers(5));
        }
    }

    public Context createApplicationContext(ApplicationInfo application, int flags) throws NameNotFoundException {
        LoadedApk pi = this.mMainThread.getPackageInfo(application, this.mResources.getCompatibilityInfo(), KeymasterDefs.KM_UINT_REP | flags);
        if (pi != null) {
            ContextImpl c = new ContextImpl(this, this.mMainThread, pi, this.mActivityToken, new UserHandle(UserHandle.getUserId(application.uid)), flags, this.mDisplay, null, -1);
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
        if (packageName.equals(HwThemeManager.HWT_USER_SYSTEM) || packageName.equals(ZenModeConfig.SYSTEM_AUTHORITY)) {
            return new ContextImpl(this, this.mMainThread, this.mPackageInfo, this.mActivityToken, user, flags, this.mDisplay, null, -1);
        } else if (this.mResources == null) {
            throw new NameNotFoundException("Application package " + packageName + " resources not found");
        } else {
            LoadedApk pi = this.mMainThread.getPackageInfo(packageName, this.mResources.getCompatibilityInfo(), KeymasterDefs.KM_UINT_REP | flags, user.getIdentifier());
            if (pi != null) {
                ContextImpl c = new ContextImpl(this, this.mMainThread, pi, this.mActivityToken, user, flags, this.mDisplay, null, -1);
                if (c.mResources != null) {
                    return c;
                }
            }
            throw new NameNotFoundException("Application package " + packageName + " not found");
        }
    }

    public Context createConfigurationContext(Configuration overrideConfiguration) {
        if (overrideConfiguration != null) {
            return new ContextImpl(this, this.mMainThread, this.mPackageInfo, this.mActivityToken, this.mUser, this.mFlags, this.mDisplay, overrideConfiguration, -1);
        }
        throw new IllegalArgumentException("overrideConfiguration must not be null");
    }

    public Context createDisplayContext(Display display) {
        if (display != null) {
            return new ContextImpl(this, this.mMainThread, this.mPackageInfo, this.mActivityToken, this.mUser, this.mFlags, display, null, -1);
        }
        throw new IllegalArgumentException("display must not be null");
    }

    public Context createDeviceProtectedStorageContext() {
        return new ContextImpl(this, this.mMainThread, this.mPackageInfo, this.mActivityToken, this.mUser, (this.mFlags & -17) | 8, this.mDisplay, null, -1);
    }

    public Context createCredentialProtectedStorageContext() {
        return new ContextImpl(this, this.mMainThread, this.mPackageInfo, this.mActivityToken, this.mUser, (this.mFlags & -9) | 16, this.mDisplay, null, -1);
    }

    public boolean isRestricted() {
        return (this.mFlags & 4) != 0 ? true : IS_SWITCH_SD_ENABLED;
    }

    public boolean isDeviceProtectedStorage() {
        return (this.mFlags & 8) != 0 ? true : IS_SWITCH_SD_ENABLED;
    }

    public boolean isCredentialProtectedStorage() {
        return (this.mFlags & 16) != 0 ? true : IS_SWITCH_SD_ENABLED;
    }

    public Display getDisplay() {
        DisplayAdjustments displayAdjustments = this.mResources.getDisplayAdjustments();
        if (this.mDisplay == null) {
            return this.mResourcesManager.getAdjustedDisplay(0, displayAdjustments);
        }
        if (!this.mDisplay.getDisplayAdjustments().equals(displayAdjustments)) {
            this.mDisplay = this.mResourcesManager.getAdjustedDisplay(this.mDisplay.getDisplayId(), displayAdjustments);
        }
        return this.mDisplay;
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
                if (!res.exists() && Process.myUid() == Process.SYSTEM_UID) {
                    Log.wtf(TAG, "Data directory doesn't exist for package " + getPackageName(), new Throwable());
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
            setFilePermissionsFromMode(file.getPath(), mode, IActivityManager.IS_PACKAGE_CLONED_TRANSACTION);
        }
        return file;
    }

    public int getUserId() {
        return this.mUser.getIdentifier();
    }

    static ContextImpl createSystemContext(ActivityThread mainThread) {
        ContextImpl context = new ContextImpl(null, mainThread, new LoadedApk(mainThread), null, null, 0, null, null, -1);
        context.mResources.updateConfiguration(context.mResourcesManager.getConfiguration(), context.mResourcesManager.getDisplayMetrics());
        return context;
    }

    static ContextImpl createAppContext(ActivityThread mainThread, LoadedApk packageInfo) {
        if (packageInfo != null) {
            return new ContextImpl(null, mainThread, packageInfo, null, null, 0, null, null, -1);
        }
        throw new IllegalArgumentException("packageInfo");
    }

    static ContextImpl createActivityContext(ActivityThread mainThread, LoadedApk packageInfo, IBinder activityToken, int displayId, Configuration overrideConfiguration) {
        if (packageInfo != null) {
            return new ContextImpl(null, mainThread, packageInfo, activityToken, null, 0, null, overrideConfiguration, displayId);
        }
        throw new IllegalArgumentException("packageInfo");
    }

    private ContextImpl(ContextImpl container, ActivityThread mainThread, LoadedApk packageInfo, IBinder activityToken, UserHandle user, int flags, Display display, Configuration overrideConfiguration, int createDisplayWithId) {
        this.mThemeResource = 0;
        this.mTheme = null;
        this.mReceiverRestrictedContext = null;
        this.mSync = new Object();
        this.mServiceCache = SystemServiceRegistry.createServiceCache();
        this.mOuterContext = this;
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
        this.mResourcesManager = ResourcesManager.getInstance();
        int displayId = createDisplayWithId != -1 ? createDisplayWithId : display != null ? display.getDisplayId() : 0;
        CompatibilityInfo compatInfo = null;
        if (container != null) {
            compatInfo = container.getDisplayAdjustments(displayId).getCompatibilityInfo();
        }
        if (compatInfo == null) {
            if (displayId == 0) {
                compatInfo = packageInfo.getCompatibilityInfo();
            } else {
                compatInfo = CompatibilityInfo.DEFAULT_COMPATIBILITY_INFO;
            }
        }
        Resources resources = packageInfo.getResources(mainThread);
        if (resources != null) {
            if (displayId == 0 && overrideConfiguration == null) {
                if (!(compatInfo == null || compatInfo.applicationScale == resources.getCompatibilityInfo().applicationScale)) {
                }
            }
            resources = container != null ? this.mResourcesManager.getResources(activityToken, packageInfo.getResDir(), packageInfo.getSplitResDirs(), packageInfo.getOverlayDirs(), packageInfo.getApplicationInfo().sharedLibraryFiles, displayId, overrideConfiguration, compatInfo, packageInfo.getClassLoader()) : this.mResourcesManager.createBaseActivityResources(activityToken, packageInfo.getResDir(), packageInfo.getSplitResDirs(), packageInfo.getOverlayDirs(), packageInfo.getApplicationInfo().sharedLibraryFiles, displayId, overrideConfiguration, compatInfo, packageInfo.getClassLoader());
        }
        this.mResources = resources;
        if (createDisplayWithId != -1) {
            display = this.mResourcesManager.getAdjustedDisplay(displayId, this.mResources.getDisplayAdjustments());
        }
        this.mDisplay = display;
        if (this.mResources != null) {
            this.mResources.getImpl().getHwResourcesImpl().setPackageName(this.mPackageInfo.getPackageName());
            this.mResources.setPackageName(this.mPackageInfo.getPackageName());
        }
        if (container != null) {
            this.mBasePackageName = container.mBasePackageName;
            this.mOpPackageName = container.mOpPackageName;
        } else {
            this.mBasePackageName = packageInfo.mPackageName;
            ApplicationInfo ainfo = packageInfo.getApplicationInfo();
            if (ainfo.uid != Process.SYSTEM_UID || ainfo.uid == Process.myUid()) {
                this.mOpPackageName = this.mBasePackageName;
            } else {
                this.mOpPackageName = ActivityThread.currentPackageName();
            }
        }
        this.mContentResolver = new ApplicationContentResolver(this, mainThread, user);
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

    final IBinder getActivityToken() {
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
        return 1 == SystemProperties.getInt("persist.sys.primarysd", 0) ? true : IS_SWITCH_SD_ENABLED;
    }
}
