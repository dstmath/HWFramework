package android.test.mock;

import android.annotation.SystemApi;
import android.app.IApplicationThread;
import android.app.IServiceConnection;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
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
import java.util.concurrent.Executor;

public class MockContext extends Context {
    @Override // android.content.Context
    public AssetManager getAssets() {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public Resources getResources() {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public PackageManager getPackageManager() {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public ContentResolver getContentResolver() {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public Looper getMainLooper() {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public Executor getMainExecutor() {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public Context getApplicationContext() {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public void setTheme(int resid) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public Resources.Theme getTheme() {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public ClassLoader getClassLoader() {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public String getPackageName() {
        throw new UnsupportedOperationException();
    }

    public String getBasePackageName() {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public String getOpPackageName() {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public ApplicationInfo getApplicationInfo() {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public String getPackageResourcePath() {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public String getPackageCodePath() {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public SharedPreferences getSharedPreferences(String name, int mode) {
        throw new UnsupportedOperationException();
    }

    public SharedPreferences getSharedPreferences(File file, int mode) {
        throw new UnsupportedOperationException();
    }

    public void reloadSharedPreferences() {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public boolean moveSharedPreferencesFrom(Context sourceContext, String name) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public boolean deleteSharedPreferences(String name) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public FileInputStream openFileInput(String name) throws FileNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public FileOutputStream openFileOutput(String name, int mode) throws FileNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public boolean deleteFile(String name) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public File getFileStreamPath(String name) {
        throw new UnsupportedOperationException();
    }

    public File getSharedPreferencesPath(String name) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public String[] fileList() {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public File getDataDir() {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public File getFilesDir() {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public File getNoBackupFilesDir() {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public File getExternalFilesDir(String type) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public File getObbDir() {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public File getCacheDir() {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public File getCodeCacheDir() {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public File getExternalCacheDir() {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public File getDir(String name, int mode) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public SQLiteDatabase openOrCreateDatabase(String file, int mode, SQLiteDatabase.CursorFactory factory) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public SQLiteDatabase openOrCreateDatabase(String file, int mode, SQLiteDatabase.CursorFactory factory, DatabaseErrorHandler errorHandler) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public File getDatabasePath(String name) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public String[] databaseList() {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public boolean moveDatabaseFrom(Context sourceContext, String name) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public boolean deleteDatabase(String name) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public Drawable getWallpaper() {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public Drawable peekWallpaper() {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public int getWallpaperDesiredMinimumWidth() {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public int getWallpaperDesiredMinimumHeight() {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public void setWallpaper(Bitmap bitmap) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public void setWallpaper(InputStream data) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public void clearWallpaper() {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public void startActivity(Intent intent) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public void startActivity(Intent intent, Bundle options) {
        startActivity(intent);
    }

    @Override // android.content.Context
    public void startActivities(Intent[] intents) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public void startActivities(Intent[] intents, Bundle options) {
        startActivities(intents);
    }

    @Override // android.content.Context
    public void startIntentSender(IntentSender intent, Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags) throws IntentSender.SendIntentException {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public void startIntentSender(IntentSender intent, Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags, Bundle options) throws IntentSender.SendIntentException {
        startIntentSender(intent, fillInIntent, flagsMask, flagsValues, extraFlags);
    }

    @Override // android.content.Context
    public void sendBroadcast(Intent intent) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public void sendBroadcast(Intent intent, String receiverPermission) {
        throw new UnsupportedOperationException();
    }

    public void sendBroadcastMultiplePermissions(Intent intent, String[] receiverPermissions) {
        throw new UnsupportedOperationException();
    }

    public void sendBroadcastAsUserMultiplePermissions(Intent intent, UserHandle user, String[] receiverPermissions) {
        throw new UnsupportedOperationException();
    }

    @SystemApi
    public void sendBroadcast(Intent intent, String receiverPermission, Bundle options) {
        throw new UnsupportedOperationException();
    }

    public void sendBroadcast(Intent intent, String receiverPermission, int appOp) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public void sendOrderedBroadcast(Intent intent, String receiverPermission) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public void sendOrderedBroadcast(Intent intent, String receiverPermission, BroadcastReceiver resultReceiver, Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {
        throw new UnsupportedOperationException();
    }

    @SystemApi
    public void sendOrderedBroadcast(Intent intent, String receiverPermission, Bundle options, BroadcastReceiver resultReceiver, Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {
        throw new UnsupportedOperationException();
    }

    public void sendOrderedBroadcast(Intent intent, String receiverPermission, int appOp, BroadcastReceiver resultReceiver, Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public void sendBroadcastAsUser(Intent intent, UserHandle user) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public void sendBroadcastAsUser(Intent intent, UserHandle user, String receiverPermission) {
        throw new UnsupportedOperationException();
    }

    @SystemApi
    public void sendBroadcastAsUser(Intent intent, UserHandle user, String receiverPermission, Bundle options) {
        throw new UnsupportedOperationException();
    }

    public void sendBroadcastAsUser(Intent intent, UserHandle user, String receiverPermission, int appOp) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public void sendOrderedBroadcastAsUser(Intent intent, UserHandle user, String receiverPermission, BroadcastReceiver resultReceiver, Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {
        throw new UnsupportedOperationException();
    }

    public void sendOrderedBroadcastAsUser(Intent intent, UserHandle user, String receiverPermission, int appOp, BroadcastReceiver resultReceiver, Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {
        throw new UnsupportedOperationException();
    }

    public void sendOrderedBroadcastAsUser(Intent intent, UserHandle user, String receiverPermission, int appOp, Bundle options, BroadcastReceiver resultReceiver, Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public void sendStickyBroadcast(Intent intent) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public void sendStickyOrderedBroadcast(Intent intent, BroadcastReceiver resultReceiver, Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public void removeStickyBroadcast(Intent intent) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public void sendStickyBroadcastAsUser(Intent intent, UserHandle user) {
        throw new UnsupportedOperationException();
    }

    public void sendStickyBroadcastAsUser(Intent intent, UserHandle user, Bundle options) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public void sendStickyOrderedBroadcastAsUser(Intent intent, UserHandle user, BroadcastReceiver resultReceiver, Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public void removeStickyBroadcastAsUser(Intent intent, UserHandle user) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter, int flags) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter, String broadcastPermission, Handler scheduler) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter, String broadcastPermission, Handler scheduler, int flags) {
        throw new UnsupportedOperationException();
    }

    public Intent registerReceiverAsUser(BroadcastReceiver receiver, UserHandle user, IntentFilter filter, String broadcastPermission, Handler scheduler) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public void unregisterReceiver(BroadcastReceiver receiver) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public ComponentName startService(Intent service) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public ComponentName startForegroundService(Intent service) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public boolean stopService(Intent service) {
        throw new UnsupportedOperationException();
    }

    public ComponentName startServiceAsUser(Intent service, UserHandle user) {
        throw new UnsupportedOperationException();
    }

    public ComponentName startForegroundServiceAsUser(Intent service, UserHandle user) {
        throw new UnsupportedOperationException();
    }

    public boolean stopServiceAsUser(Intent service, UserHandle user) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public boolean bindService(Intent service, ServiceConnection conn, int flags) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public boolean bindService(Intent service, int flags, Executor executor, ServiceConnection conn) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public boolean bindIsolatedService(Intent service, int flags, String instanceName, Executor executor, ServiceConnection conn) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public boolean bindServiceAsUser(Intent service, ServiceConnection conn, int flags, UserHandle user) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public void updateServiceGroup(ServiceConnection conn, int group, int importance) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public void unbindService(ServiceConnection conn) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public boolean startInstrumentation(ComponentName className, String profileFile, Bundle arguments) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public Object getSystemService(String name) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public String getSystemServiceName(Class<?> cls) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public int checkPermission(String permission, int pid, int uid) {
        throw new UnsupportedOperationException();
    }

    public int checkPermission(String permission, int pid, int uid, IBinder callerToken) {
        return checkPermission(permission, pid, uid);
    }

    @Override // android.content.Context
    public int checkCallingPermission(String permission) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public int checkCallingOrSelfPermission(String permission) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public int checkSelfPermission(String permission) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public void enforcePermission(String permission, int pid, int uid, String message) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public void enforceCallingPermission(String permission, String message) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public void enforceCallingOrSelfPermission(String permission, String message) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public void grantUriPermission(String toPackage, Uri uri, int modeFlags) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public void revokeUriPermission(Uri uri, int modeFlags) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public void revokeUriPermission(String targetPackage, Uri uri, int modeFlags) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public int checkUriPermission(Uri uri, int pid, int uid, int modeFlags) {
        throw new UnsupportedOperationException();
    }

    public int checkUriPermission(Uri uri, int pid, int uid, int modeFlags, IBinder callerToken) {
        return checkUriPermission(uri, pid, uid, modeFlags);
    }

    @Override // android.content.Context
    public int checkCallingUriPermission(Uri uri, int modeFlags) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public int checkCallingOrSelfUriPermission(Uri uri, int modeFlags) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public int checkUriPermission(Uri uri, String readPermission, String writePermission, int pid, int uid, int modeFlags) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public void enforceUriPermission(Uri uri, int pid, int uid, int modeFlags, String message) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public void enforceCallingUriPermission(Uri uri, int modeFlags, String message) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public void enforceCallingOrSelfUriPermission(Uri uri, int modeFlags, String message) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public void enforceUriPermission(Uri uri, String readPermission, String writePermission, int pid, int uid, int modeFlags, String message) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public Context createPackageContext(String packageName, int flags) throws PackageManager.NameNotFoundException {
        throw new UnsupportedOperationException();
    }

    public Context createApplicationContext(ApplicationInfo application, int flags) throws PackageManager.NameNotFoundException {
        return null;
    }

    @Override // android.content.Context
    public Context createContextForSplit(String splitName) throws PackageManager.NameNotFoundException {
        throw new UnsupportedOperationException();
    }

    public void releaseContextForSplit(String splitName) throws PackageManager.NameNotFoundException {
        throw new UnsupportedOperationException();
    }

    public Context createPackageContextAsUser(String packageName, int flags, UserHandle user) throws PackageManager.NameNotFoundException {
        throw new UnsupportedOperationException();
    }

    public int getUserId() {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public Context createConfigurationContext(Configuration overrideConfiguration) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public Context createDisplayContext(Display display) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public boolean isRestricted() {
        throw new UnsupportedOperationException();
    }

    public DisplayAdjustments getDisplayAdjustments(int displayId) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public Display getDisplay() {
        throw new UnsupportedOperationException();
    }

    public int getDisplayId() {
        throw new UnsupportedOperationException();
    }

    public void updateDisplay(int displayId) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public File[] getExternalFilesDirs(String type) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public File[] getObbDirs() {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public File[] getExternalCacheDirs() {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public File[] getExternalMediaDirs() {
        throw new UnsupportedOperationException();
    }

    public File getPreloadsFileCache() {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public Context createDeviceProtectedStorageContext() {
        throw new UnsupportedOperationException();
    }

    @SystemApi
    public Context createCredentialProtectedStorageContext() {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.Context
    public boolean isDeviceProtectedStorage() {
        throw new UnsupportedOperationException();
    }

    @SystemApi
    public boolean isCredentialProtectedStorage() {
        throw new UnsupportedOperationException();
    }

    public boolean canLoadUnsafeResources() {
        throw new UnsupportedOperationException();
    }

    public IBinder getActivityToken() {
        throw new UnsupportedOperationException();
    }

    public IServiceConnection getServiceDispatcher(ServiceConnection conn, Handler handler, int flags) {
        throw new UnsupportedOperationException();
    }

    public IApplicationThread getIApplicationThread() {
        throw new UnsupportedOperationException();
    }

    public Handler getMainThreadHandler() {
        throw new UnsupportedOperationException();
    }
}
