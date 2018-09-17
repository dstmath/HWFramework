package android.content;

import android.app.IApplicationThread;
import android.app.IServiceConnection;
import android.content.IntentSender.SendIntentException;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.UserHandle;
import android.view.Display;
import android.view.DisplayAdjustments;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ContextWrapper extends Context {
    Context mBase;

    public ContextWrapper(Context base) {
        this.mBase = base;
    }

    protected void attachBaseContext(Context base) {
        if (this.mBase != null) {
            throw new IllegalStateException("Base context already set");
        }
        this.mBase = base;
    }

    public Context getBaseContext() {
        return this.mBase;
    }

    public AssetManager getAssets() {
        return this.mBase.getAssets();
    }

    public Resources getResources() {
        return this.mBase.getResources();
    }

    public PackageManager getPackageManager() {
        return this.mBase.getPackageManager();
    }

    public ContentResolver getContentResolver() {
        return this.mBase.getContentResolver();
    }

    public Looper getMainLooper() {
        return this.mBase.getMainLooper();
    }

    public Context getApplicationContext() {
        return this.mBase.getApplicationContext();
    }

    public void setTheme(int resid) {
        this.mBase.setTheme(resid);
    }

    public int getThemeResId() {
        return this.mBase.getThemeResId();
    }

    public Theme getTheme() {
        return this.mBase.getTheme();
    }

    public ClassLoader getClassLoader() {
        return this.mBase.getClassLoader();
    }

    public String getPackageName() {
        return this.mBase.getPackageName();
    }

    public String getBasePackageName() {
        return this.mBase.getBasePackageName();
    }

    public String getOpPackageName() {
        return this.mBase.getOpPackageName();
    }

    public ApplicationInfo getApplicationInfo() {
        return this.mBase.getApplicationInfo();
    }

    public String getPackageResourcePath() {
        return this.mBase.getPackageResourcePath();
    }

    public String getPackageCodePath() {
        return this.mBase.getPackageCodePath();
    }

    public SharedPreferences getSharedPreferences(String name, int mode) {
        return this.mBase.getSharedPreferences(name, mode);
    }

    public SharedPreferences getSharedPreferences(File file, int mode) {
        return this.mBase.getSharedPreferences(file, mode);
    }

    public boolean moveSharedPreferencesFrom(Context sourceContext, String name) {
        return this.mBase.moveSharedPreferencesFrom(sourceContext, name);
    }

    public boolean deleteSharedPreferences(String name) {
        return this.mBase.deleteSharedPreferences(name);
    }

    public FileInputStream openFileInput(String name) throws FileNotFoundException {
        return this.mBase.openFileInput(name);
    }

    public FileOutputStream openFileOutput(String name, int mode) throws FileNotFoundException {
        return this.mBase.openFileOutput(name, mode);
    }

    public boolean deleteFile(String name) {
        return this.mBase.deleteFile(name);
    }

    public File getFileStreamPath(String name) {
        return this.mBase.getFileStreamPath(name);
    }

    public File getSharedPreferencesPath(String name) {
        return this.mBase.getSharedPreferencesPath(name);
    }

    public String[] fileList() {
        return this.mBase.fileList();
    }

    public File getDataDir() {
        return this.mBase.getDataDir();
    }

    public File getFilesDir() {
        return this.mBase.getFilesDir();
    }

    public File getNoBackupFilesDir() {
        return this.mBase.getNoBackupFilesDir();
    }

    public File getExternalFilesDir(String type) {
        return this.mBase.getExternalFilesDir(type);
    }

    public File[] getExternalFilesDirs(String type) {
        return this.mBase.getExternalFilesDirs(type);
    }

    public File getObbDir() {
        return this.mBase.getObbDir();
    }

    public File[] getObbDirs() {
        return this.mBase.getObbDirs();
    }

    public File getCacheDir() {
        return this.mBase.getCacheDir();
    }

    public File getCodeCacheDir() {
        return this.mBase.getCodeCacheDir();
    }

    public File getExternalCacheDir() {
        return this.mBase.getExternalCacheDir();
    }

    public File[] getExternalCacheDirs() {
        return this.mBase.getExternalCacheDirs();
    }

    public File[] getExternalMediaDirs() {
        return this.mBase.getExternalMediaDirs();
    }

    public File getDir(String name, int mode) {
        return this.mBase.getDir(name, mode);
    }

    public File getPreloadsFileCache() {
        return this.mBase.getPreloadsFileCache();
    }

    public SQLiteDatabase openOrCreateDatabase(String name, int mode, CursorFactory factory) {
        return this.mBase.openOrCreateDatabase(name, mode, factory);
    }

    public SQLiteDatabase openOrCreateDatabase(String name, int mode, CursorFactory factory, DatabaseErrorHandler errorHandler) {
        return this.mBase.openOrCreateDatabase(name, mode, factory, errorHandler);
    }

    public boolean moveDatabaseFrom(Context sourceContext, String name) {
        return this.mBase.moveDatabaseFrom(sourceContext, name);
    }

    public boolean deleteDatabase(String name) {
        return this.mBase.deleteDatabase(name);
    }

    public File getDatabasePath(String name) {
        return this.mBase.getDatabasePath(name);
    }

    public String[] databaseList() {
        return this.mBase.databaseList();
    }

    @Deprecated
    public Drawable getWallpaper() {
        return this.mBase.getWallpaper();
    }

    @Deprecated
    public Drawable peekWallpaper() {
        return this.mBase.peekWallpaper();
    }

    @Deprecated
    public int getWallpaperDesiredMinimumWidth() {
        return this.mBase.getWallpaperDesiredMinimumWidth();
    }

    @Deprecated
    public int getWallpaperDesiredMinimumHeight() {
        return this.mBase.getWallpaperDesiredMinimumHeight();
    }

    @Deprecated
    public void setWallpaper(Bitmap bitmap) throws IOException {
        this.mBase.setWallpaper(bitmap);
    }

    @Deprecated
    public void setWallpaper(InputStream data) throws IOException {
        this.mBase.setWallpaper(data);
    }

    @Deprecated
    public void clearWallpaper() throws IOException {
        this.mBase.clearWallpaper();
    }

    public void startActivity(Intent intent) {
        this.mBase.startActivity(intent);
    }

    public void startActivityAsUser(Intent intent, UserHandle user) {
        this.mBase.startActivityAsUser(intent, user);
    }

    public void startActivityForResult(String who, Intent intent, int requestCode, Bundle options) {
        this.mBase.startActivityForResult(who, intent, requestCode, options);
    }

    public boolean canStartActivityForResult() {
        return this.mBase.canStartActivityForResult();
    }

    public void startActivity(Intent intent, Bundle options) {
        this.mBase.startActivity(intent, options);
    }

    public void startActivityAsUser(Intent intent, Bundle options, UserHandle user) {
        this.mBase.startActivityAsUser(intent, options, user);
    }

    public void startActivities(Intent[] intents) {
        this.mBase.startActivities(intents);
    }

    public void startActivities(Intent[] intents, Bundle options) {
        this.mBase.startActivities(intents, options);
    }

    public void startActivitiesAsUser(Intent[] intents, Bundle options, UserHandle userHandle) {
        this.mBase.startActivitiesAsUser(intents, options, userHandle);
    }

    public void startIntentSender(IntentSender intent, Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags) throws SendIntentException {
        this.mBase.startIntentSender(intent, fillInIntent, flagsMask, flagsValues, extraFlags);
    }

    public void startIntentSender(IntentSender intent, Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags, Bundle options) throws SendIntentException {
        this.mBase.startIntentSender(intent, fillInIntent, flagsMask, flagsValues, extraFlags, options);
    }

    public void sendBroadcast(Intent intent) {
        this.mBase.sendBroadcast(intent);
    }

    public void sendBroadcast(Intent intent, String receiverPermission) {
        this.mBase.sendBroadcast(intent, receiverPermission);
    }

    public void sendBroadcastMultiplePermissions(Intent intent, String[] receiverPermissions) {
        this.mBase.sendBroadcastMultiplePermissions(intent, receiverPermissions);
    }

    public void sendBroadcast(Intent intent, String receiverPermission, Bundle options) {
        this.mBase.sendBroadcast(intent, receiverPermission, options);
    }

    public void sendBroadcast(Intent intent, String receiverPermission, int appOp) {
        this.mBase.sendBroadcast(intent, receiverPermission, appOp);
    }

    public void sendOrderedBroadcast(Intent intent, String receiverPermission) {
        this.mBase.sendOrderedBroadcast(intent, receiverPermission);
    }

    public void sendOrderedBroadcast(Intent intent, String receiverPermission, BroadcastReceiver resultReceiver, Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {
        this.mBase.sendOrderedBroadcast(intent, receiverPermission, resultReceiver, scheduler, initialCode, initialData, initialExtras);
    }

    public void sendOrderedBroadcast(Intent intent, String receiverPermission, Bundle options, BroadcastReceiver resultReceiver, Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {
        this.mBase.sendOrderedBroadcast(intent, receiverPermission, options, resultReceiver, scheduler, initialCode, initialData, initialExtras);
    }

    public void sendOrderedBroadcast(Intent intent, String receiverPermission, int appOp, BroadcastReceiver resultReceiver, Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {
        this.mBase.sendOrderedBroadcast(intent, receiverPermission, appOp, resultReceiver, scheduler, initialCode, initialData, initialExtras);
    }

    public void sendBroadcastAsUser(Intent intent, UserHandle user) {
        this.mBase.sendBroadcastAsUser(intent, user);
    }

    public void sendBroadcastAsUser(Intent intent, UserHandle user, String receiverPermission) {
        this.mBase.sendBroadcastAsUser(intent, user, receiverPermission);
    }

    public void sendBroadcastAsUser(Intent intent, UserHandle user, String receiverPermission, Bundle options) {
        this.mBase.sendBroadcastAsUser(intent, user, receiverPermission, options);
    }

    public void sendBroadcastAsUser(Intent intent, UserHandle user, String receiverPermission, int appOp) {
        this.mBase.sendBroadcastAsUser(intent, user, receiverPermission, appOp);
    }

    public void sendOrderedBroadcastAsUser(Intent intent, UserHandle user, String receiverPermission, BroadcastReceiver resultReceiver, Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {
        this.mBase.sendOrderedBroadcastAsUser(intent, user, receiverPermission, resultReceiver, scheduler, initialCode, initialData, initialExtras);
    }

    public void sendOrderedBroadcastAsUser(Intent intent, UserHandle user, String receiverPermission, int appOp, BroadcastReceiver resultReceiver, Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {
        this.mBase.sendOrderedBroadcastAsUser(intent, user, receiverPermission, appOp, resultReceiver, scheduler, initialCode, initialData, initialExtras);
    }

    public void sendOrderedBroadcastAsUser(Intent intent, UserHandle user, String receiverPermission, int appOp, Bundle options, BroadcastReceiver resultReceiver, Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {
        this.mBase.sendOrderedBroadcastAsUser(intent, user, receiverPermission, appOp, options, resultReceiver, scheduler, initialCode, initialData, initialExtras);
    }

    @Deprecated
    public void sendStickyBroadcast(Intent intent) {
        this.mBase.sendStickyBroadcast(intent);
    }

    @Deprecated
    public void sendStickyOrderedBroadcast(Intent intent, BroadcastReceiver resultReceiver, Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {
        this.mBase.sendStickyOrderedBroadcast(intent, resultReceiver, scheduler, initialCode, initialData, initialExtras);
    }

    @Deprecated
    public void removeStickyBroadcast(Intent intent) {
        this.mBase.removeStickyBroadcast(intent);
    }

    @Deprecated
    public void sendStickyBroadcastAsUser(Intent intent, UserHandle user) {
        this.mBase.sendStickyBroadcastAsUser(intent, user);
    }

    @Deprecated
    public void sendStickyBroadcastAsUser(Intent intent, UserHandle user, Bundle options) {
        this.mBase.sendStickyBroadcastAsUser(intent, user, options);
    }

    @Deprecated
    public void sendStickyOrderedBroadcastAsUser(Intent intent, UserHandle user, BroadcastReceiver resultReceiver, Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {
        this.mBase.sendStickyOrderedBroadcastAsUser(intent, user, resultReceiver, scheduler, initialCode, initialData, initialExtras);
    }

    @Deprecated
    public void removeStickyBroadcastAsUser(Intent intent, UserHandle user) {
        this.mBase.removeStickyBroadcastAsUser(intent, user);
    }

    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        return this.mBase.registerReceiver(receiver, filter);
    }

    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter, int flags) {
        return this.mBase.registerReceiver(receiver, filter, flags);
    }

    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter, String broadcastPermission, Handler scheduler) {
        return this.mBase.registerReceiver(receiver, filter, broadcastPermission, scheduler);
    }

    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter, String broadcastPermission, Handler scheduler, int flags) {
        return this.mBase.registerReceiver(receiver, filter, broadcastPermission, scheduler, flags);
    }

    public Intent registerReceiverAsUser(BroadcastReceiver receiver, UserHandle user, IntentFilter filter, String broadcastPermission, Handler scheduler) {
        return this.mBase.registerReceiverAsUser(receiver, user, filter, broadcastPermission, scheduler);
    }

    public void unregisterReceiver(BroadcastReceiver receiver) {
        this.mBase.unregisterReceiver(receiver);
    }

    public ComponentName startService(Intent service) {
        return this.mBase.startService(service);
    }

    public ComponentName startForegroundService(Intent service) {
        return this.mBase.startForegroundService(service);
    }

    public boolean stopService(Intent name) {
        return this.mBase.stopService(name);
    }

    public ComponentName startServiceAsUser(Intent service, UserHandle user) {
        return this.mBase.startServiceAsUser(service, user);
    }

    public ComponentName startForegroundServiceAsUser(Intent service, UserHandle user) {
        return this.mBase.startForegroundServiceAsUser(service, user);
    }

    public boolean stopServiceAsUser(Intent name, UserHandle user) {
        return this.mBase.stopServiceAsUser(name, user);
    }

    public boolean bindService(Intent service, ServiceConnection conn, int flags) {
        return this.mBase.bindService(service, conn, flags);
    }

    public boolean bindServiceAsUser(Intent service, ServiceConnection conn, int flags, UserHandle user) {
        return this.mBase.bindServiceAsUser(service, conn, flags, user);
    }

    public boolean bindServiceAsUser(Intent service, ServiceConnection conn, int flags, Handler handler, UserHandle user) {
        return this.mBase.bindServiceAsUser(service, conn, flags, handler, user);
    }

    public void unbindService(ServiceConnection conn) {
        this.mBase.unbindService(conn);
    }

    public boolean startInstrumentation(ComponentName className, String profileFile, Bundle arguments) {
        return this.mBase.startInstrumentation(className, profileFile, arguments);
    }

    public Object getSystemService(String name) {
        return this.mBase.getSystemService(name);
    }

    public String getSystemServiceName(Class<?> serviceClass) {
        return this.mBase.getSystemServiceName(serviceClass);
    }

    public int checkPermission(String permission, int pid, int uid) {
        return this.mBase.checkPermission(permission, pid, uid);
    }

    public int checkPermission(String permission, int pid, int uid, IBinder callerToken) {
        return this.mBase.checkPermission(permission, pid, uid, callerToken);
    }

    public int checkCallingPermission(String permission) {
        return this.mBase.checkCallingPermission(permission);
    }

    public int checkCallingOrSelfPermission(String permission) {
        return this.mBase.checkCallingOrSelfPermission(permission);
    }

    public int checkSelfPermission(String permission) {
        return this.mBase.checkSelfPermission(permission);
    }

    public void enforcePermission(String permission, int pid, int uid, String message) {
        this.mBase.enforcePermission(permission, pid, uid, message);
    }

    public void enforceCallingPermission(String permission, String message) {
        this.mBase.enforceCallingPermission(permission, message);
    }

    public void enforceCallingOrSelfPermission(String permission, String message) {
        this.mBase.enforceCallingOrSelfPermission(permission, message);
    }

    public void grantUriPermission(String toPackage, Uri uri, int modeFlags) {
        this.mBase.grantUriPermission(toPackage, uri, modeFlags);
    }

    public void revokeUriPermission(Uri uri, int modeFlags) {
        this.mBase.revokeUriPermission(uri, modeFlags);
    }

    public void revokeUriPermission(String targetPackage, Uri uri, int modeFlags) {
        this.mBase.revokeUriPermission(targetPackage, uri, modeFlags);
    }

    public int checkUriPermission(Uri uri, int pid, int uid, int modeFlags) {
        return this.mBase.checkUriPermission(uri, pid, uid, modeFlags);
    }

    public int checkUriPermission(Uri uri, int pid, int uid, int modeFlags, IBinder callerToken) {
        return this.mBase.checkUriPermission(uri, pid, uid, modeFlags, callerToken);
    }

    public int checkCallingUriPermission(Uri uri, int modeFlags) {
        return this.mBase.checkCallingUriPermission(uri, modeFlags);
    }

    public int checkCallingOrSelfUriPermission(Uri uri, int modeFlags) {
        return this.mBase.checkCallingOrSelfUriPermission(uri, modeFlags);
    }

    public int checkUriPermission(Uri uri, String readPermission, String writePermission, int pid, int uid, int modeFlags) {
        return this.mBase.checkUriPermission(uri, readPermission, writePermission, pid, uid, modeFlags);
    }

    public void enforceUriPermission(Uri uri, int pid, int uid, int modeFlags, String message) {
        this.mBase.enforceUriPermission(uri, pid, uid, modeFlags, message);
    }

    public void enforceCallingUriPermission(Uri uri, int modeFlags, String message) {
        this.mBase.enforceCallingUriPermission(uri, modeFlags, message);
    }

    public void enforceCallingOrSelfUriPermission(Uri uri, int modeFlags, String message) {
        this.mBase.enforceCallingOrSelfUriPermission(uri, modeFlags, message);
    }

    public void enforceUriPermission(Uri uri, String readPermission, String writePermission, int pid, int uid, int modeFlags, String message) {
        this.mBase.enforceUriPermission(uri, readPermission, writePermission, pid, uid, modeFlags, message);
    }

    public Context createPackageContext(String packageName, int flags) throws NameNotFoundException {
        return this.mBase.createPackageContext(packageName, flags);
    }

    public Context createPackageContextAsUser(String packageName, int flags, UserHandle user) throws NameNotFoundException {
        return this.mBase.createPackageContextAsUser(packageName, flags, user);
    }

    public Context createApplicationContext(ApplicationInfo application, int flags) throws NameNotFoundException {
        return this.mBase.createApplicationContext(application, flags);
    }

    public Context createContextForSplit(String splitName) throws NameNotFoundException {
        return this.mBase.createContextForSplit(splitName);
    }

    public int getUserId() {
        return this.mBase.getUserId();
    }

    public Context createConfigurationContext(Configuration overrideConfiguration) {
        return this.mBase.createConfigurationContext(overrideConfiguration);
    }

    public Context createDisplayContext(Display display) {
        return this.mBase.createDisplayContext(display);
    }

    public boolean isRestricted() {
        return this.mBase.isRestricted();
    }

    public DisplayAdjustments getDisplayAdjustments(int displayId) {
        return this.mBase.getDisplayAdjustments(displayId);
    }

    public Display getDisplay() {
        return this.mBase.getDisplay();
    }

    public void updateDisplay(int displayId) {
        this.mBase.updateDisplay(displayId);
    }

    public Context createDeviceProtectedStorageContext() {
        return this.mBase.createDeviceProtectedStorageContext();
    }

    public Context createCredentialProtectedStorageContext() {
        return this.mBase.createCredentialProtectedStorageContext();
    }

    public boolean isDeviceProtectedStorage() {
        return this.mBase.isDeviceProtectedStorage();
    }

    public boolean isCredentialProtectedStorage() {
        return this.mBase.isCredentialProtectedStorage();
    }

    public boolean canLoadUnsafeResources() {
        return this.mBase.canLoadUnsafeResources();
    }

    public IBinder getActivityToken() {
        return this.mBase.getActivityToken();
    }

    public IServiceConnection getServiceDispatcher(ServiceConnection conn, Handler handler, int flags) {
        return this.mBase.getServiceDispatcher(conn, handler, flags);
    }

    public IApplicationThread getIApplicationThread() {
        return this.mBase.getIApplicationThread();
    }

    public Handler getMainThreadHandler() {
        return this.mBase.getMainThreadHandler();
    }

    public int getNextAutofillId() {
        return this.mBase.getNextAutofillId();
    }
}
