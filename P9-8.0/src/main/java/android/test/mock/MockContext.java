package android.test.mock;

import android.app.IApplicationThread;
import android.app.IServiceConnection;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
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

public class MockContext extends Context {
    public AssetManager getAssets() {
        throw new UnsupportedOperationException();
    }

    public Resources getResources() {
        throw new UnsupportedOperationException();
    }

    public PackageManager getPackageManager() {
        throw new UnsupportedOperationException();
    }

    public ContentResolver getContentResolver() {
        throw new UnsupportedOperationException();
    }

    public Looper getMainLooper() {
        throw new UnsupportedOperationException();
    }

    public Context getApplicationContext() {
        throw new UnsupportedOperationException();
    }

    public void setTheme(int resid) {
        throw new UnsupportedOperationException();
    }

    public Theme getTheme() {
        throw new UnsupportedOperationException();
    }

    public ClassLoader getClassLoader() {
        throw new UnsupportedOperationException();
    }

    public String getPackageName() {
        throw new UnsupportedOperationException();
    }

    public String getBasePackageName() {
        throw new UnsupportedOperationException();
    }

    public String getOpPackageName() {
        throw new UnsupportedOperationException();
    }

    public ApplicationInfo getApplicationInfo() {
        throw new UnsupportedOperationException();
    }

    public String getPackageResourcePath() {
        throw new UnsupportedOperationException();
    }

    public String getPackageCodePath() {
        throw new UnsupportedOperationException();
    }

    public SharedPreferences getSharedPreferences(String name, int mode) {
        throw new UnsupportedOperationException();
    }

    public SharedPreferences getSharedPreferences(File file, int mode) {
        throw new UnsupportedOperationException();
    }

    public boolean moveSharedPreferencesFrom(Context sourceContext, String name) {
        throw new UnsupportedOperationException();
    }

    public boolean deleteSharedPreferences(String name) {
        throw new UnsupportedOperationException();
    }

    public FileInputStream openFileInput(String name) throws FileNotFoundException {
        throw new UnsupportedOperationException();
    }

    public FileOutputStream openFileOutput(String name, int mode) throws FileNotFoundException {
        throw new UnsupportedOperationException();
    }

    public boolean deleteFile(String name) {
        throw new UnsupportedOperationException();
    }

    public File getFileStreamPath(String name) {
        throw new UnsupportedOperationException();
    }

    public File getSharedPreferencesPath(String name) {
        throw new UnsupportedOperationException();
    }

    public String[] fileList() {
        throw new UnsupportedOperationException();
    }

    public File getDataDir() {
        throw new UnsupportedOperationException();
    }

    public File getFilesDir() {
        throw new UnsupportedOperationException();
    }

    public File getNoBackupFilesDir() {
        throw new UnsupportedOperationException();
    }

    public File getExternalFilesDir(String type) {
        throw new UnsupportedOperationException();
    }

    public File getObbDir() {
        throw new UnsupportedOperationException();
    }

    public File getCacheDir() {
        throw new UnsupportedOperationException();
    }

    public File getCodeCacheDir() {
        throw new UnsupportedOperationException();
    }

    public File getExternalCacheDir() {
        throw new UnsupportedOperationException();
    }

    public File getDir(String name, int mode) {
        throw new UnsupportedOperationException();
    }

    public SQLiteDatabase openOrCreateDatabase(String file, int mode, CursorFactory factory) {
        throw new UnsupportedOperationException();
    }

    public SQLiteDatabase openOrCreateDatabase(String file, int mode, CursorFactory factory, DatabaseErrorHandler errorHandler) {
        throw new UnsupportedOperationException();
    }

    public File getDatabasePath(String name) {
        throw new UnsupportedOperationException();
    }

    public String[] databaseList() {
        throw new UnsupportedOperationException();
    }

    public boolean moveDatabaseFrom(Context sourceContext, String name) {
        throw new UnsupportedOperationException();
    }

    public boolean deleteDatabase(String name) {
        throw new UnsupportedOperationException();
    }

    public Drawable getWallpaper() {
        throw new UnsupportedOperationException();
    }

    public Drawable peekWallpaper() {
        throw new UnsupportedOperationException();
    }

    public int getWallpaperDesiredMinimumWidth() {
        throw new UnsupportedOperationException();
    }

    public int getWallpaperDesiredMinimumHeight() {
        throw new UnsupportedOperationException();
    }

    public void setWallpaper(Bitmap bitmap) throws IOException {
        throw new UnsupportedOperationException();
    }

    public void setWallpaper(InputStream data) throws IOException {
        throw new UnsupportedOperationException();
    }

    public void clearWallpaper() {
        throw new UnsupportedOperationException();
    }

    public void startActivity(Intent intent) {
        throw new UnsupportedOperationException();
    }

    public void startActivity(Intent intent, Bundle options) {
        startActivity(intent);
    }

    public void startActivities(Intent[] intents) {
        throw new UnsupportedOperationException();
    }

    public void startActivities(Intent[] intents, Bundle options) {
        startActivities(intents);
    }

    public void startIntentSender(IntentSender intent, Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags) throws SendIntentException {
        throw new UnsupportedOperationException();
    }

    public void startIntentSender(IntentSender intent, Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags, Bundle options) throws SendIntentException {
        startIntentSender(intent, fillInIntent, flagsMask, flagsValues, extraFlags);
    }

    public void sendBroadcast(Intent intent) {
        throw new UnsupportedOperationException();
    }

    public void sendBroadcast(Intent intent, String receiverPermission) {
        throw new UnsupportedOperationException();
    }

    public void sendBroadcastMultiplePermissions(Intent intent, String[] receiverPermissions) {
        throw new UnsupportedOperationException();
    }

    public void sendBroadcast(Intent intent, String receiverPermission, Bundle options) {
        throw new UnsupportedOperationException();
    }

    public void sendBroadcast(Intent intent, String receiverPermission, int appOp) {
        throw new UnsupportedOperationException();
    }

    public void sendOrderedBroadcast(Intent intent, String receiverPermission) {
        throw new UnsupportedOperationException();
    }

    public void sendOrderedBroadcast(Intent intent, String receiverPermission, BroadcastReceiver resultReceiver, Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {
        throw new UnsupportedOperationException();
    }

    public void sendOrderedBroadcast(Intent intent, String receiverPermission, Bundle options, BroadcastReceiver resultReceiver, Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {
        throw new UnsupportedOperationException();
    }

    public void sendOrderedBroadcast(Intent intent, String receiverPermission, int appOp, BroadcastReceiver resultReceiver, Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {
        throw new UnsupportedOperationException();
    }

    public void sendBroadcastAsUser(Intent intent, UserHandle user) {
        throw new UnsupportedOperationException();
    }

    public void sendBroadcastAsUser(Intent intent, UserHandle user, String receiverPermission) {
        throw new UnsupportedOperationException();
    }

    public void sendBroadcastAsUser(Intent intent, UserHandle user, String receiverPermission, Bundle options) {
        throw new UnsupportedOperationException();
    }

    public void sendBroadcastAsUser(Intent intent, UserHandle user, String receiverPermission, int appOp) {
        throw new UnsupportedOperationException();
    }

    public void sendOrderedBroadcastAsUser(Intent intent, UserHandle user, String receiverPermission, BroadcastReceiver resultReceiver, Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {
        throw new UnsupportedOperationException();
    }

    public void sendOrderedBroadcastAsUser(Intent intent, UserHandle user, String receiverPermission, int appOp, BroadcastReceiver resultReceiver, Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {
        throw new UnsupportedOperationException();
    }

    public void sendOrderedBroadcastAsUser(Intent intent, UserHandle user, String receiverPermission, int appOp, Bundle options, BroadcastReceiver resultReceiver, Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {
        throw new UnsupportedOperationException();
    }

    public void sendStickyBroadcast(Intent intent) {
        throw new UnsupportedOperationException();
    }

    public void sendStickyOrderedBroadcast(Intent intent, BroadcastReceiver resultReceiver, Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {
        throw new UnsupportedOperationException();
    }

    public void removeStickyBroadcast(Intent intent) {
        throw new UnsupportedOperationException();
    }

    public void sendStickyBroadcastAsUser(Intent intent, UserHandle user) {
        throw new UnsupportedOperationException();
    }

    public void sendStickyBroadcastAsUser(Intent intent, UserHandle user, Bundle options) {
        throw new UnsupportedOperationException();
    }

    public void sendStickyOrderedBroadcastAsUser(Intent intent, UserHandle user, BroadcastReceiver resultReceiver, Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {
        throw new UnsupportedOperationException();
    }

    public void removeStickyBroadcastAsUser(Intent intent, UserHandle user) {
        throw new UnsupportedOperationException();
    }

    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        throw new UnsupportedOperationException();
    }

    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter, int flags) {
        throw new UnsupportedOperationException();
    }

    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter, String broadcastPermission, Handler scheduler) {
        throw new UnsupportedOperationException();
    }

    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter, String broadcastPermission, Handler scheduler, int flags) {
        throw new UnsupportedOperationException();
    }

    public Intent registerReceiverAsUser(BroadcastReceiver receiver, UserHandle user, IntentFilter filter, String broadcastPermission, Handler scheduler) {
        throw new UnsupportedOperationException();
    }

    public void unregisterReceiver(BroadcastReceiver receiver) {
        throw new UnsupportedOperationException();
    }

    public ComponentName startService(Intent service) {
        throw new UnsupportedOperationException();
    }

    public ComponentName startForegroundService(Intent service) {
        throw new UnsupportedOperationException();
    }

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

    public boolean bindService(Intent service, ServiceConnection conn, int flags) {
        throw new UnsupportedOperationException();
    }

    public boolean bindServiceAsUser(Intent service, ServiceConnection conn, int flags, UserHandle user) {
        throw new UnsupportedOperationException();
    }

    public void unbindService(ServiceConnection conn) {
        throw new UnsupportedOperationException();
    }

    public boolean startInstrumentation(ComponentName className, String profileFile, Bundle arguments) {
        throw new UnsupportedOperationException();
    }

    public Object getSystemService(String name) {
        throw new UnsupportedOperationException();
    }

    public String getSystemServiceName(Class<?> cls) {
        throw new UnsupportedOperationException();
    }

    public int checkPermission(String permission, int pid, int uid) {
        throw new UnsupportedOperationException();
    }

    public int checkPermission(String permission, int pid, int uid, IBinder callerToken) {
        return checkPermission(permission, pid, uid);
    }

    public int checkCallingPermission(String permission) {
        throw new UnsupportedOperationException();
    }

    public int checkCallingOrSelfPermission(String permission) {
        throw new UnsupportedOperationException();
    }

    public int checkSelfPermission(String permission) {
        throw new UnsupportedOperationException();
    }

    public void enforcePermission(String permission, int pid, int uid, String message) {
        throw new UnsupportedOperationException();
    }

    public void enforceCallingPermission(String permission, String message) {
        throw new UnsupportedOperationException();
    }

    public void enforceCallingOrSelfPermission(String permission, String message) {
        throw new UnsupportedOperationException();
    }

    public void grantUriPermission(String toPackage, Uri uri, int modeFlags) {
        throw new UnsupportedOperationException();
    }

    public void revokeUriPermission(Uri uri, int modeFlags) {
        throw new UnsupportedOperationException();
    }

    public void revokeUriPermission(String targetPackage, Uri uri, int modeFlags) {
        throw new UnsupportedOperationException();
    }

    public int checkUriPermission(Uri uri, int pid, int uid, int modeFlags) {
        throw new UnsupportedOperationException();
    }

    public int checkUriPermission(Uri uri, int pid, int uid, int modeFlags, IBinder callerToken) {
        return checkUriPermission(uri, pid, uid, modeFlags);
    }

    public int checkCallingUriPermission(Uri uri, int modeFlags) {
        throw new UnsupportedOperationException();
    }

    public int checkCallingOrSelfUriPermission(Uri uri, int modeFlags) {
        throw new UnsupportedOperationException();
    }

    public int checkUriPermission(Uri uri, String readPermission, String writePermission, int pid, int uid, int modeFlags) {
        throw new UnsupportedOperationException();
    }

    public void enforceUriPermission(Uri uri, int pid, int uid, int modeFlags, String message) {
        throw new UnsupportedOperationException();
    }

    public void enforceCallingUriPermission(Uri uri, int modeFlags, String message) {
        throw new UnsupportedOperationException();
    }

    public void enforceCallingOrSelfUriPermission(Uri uri, int modeFlags, String message) {
        throw new UnsupportedOperationException();
    }

    public void enforceUriPermission(Uri uri, String readPermission, String writePermission, int pid, int uid, int modeFlags, String message) {
        throw new UnsupportedOperationException();
    }

    public Context createPackageContext(String packageName, int flags) throws NameNotFoundException {
        throw new UnsupportedOperationException();
    }

    public Context createApplicationContext(ApplicationInfo application, int flags) throws NameNotFoundException {
        return null;
    }

    public Context createContextForSplit(String splitName) throws NameNotFoundException {
        throw new UnsupportedOperationException();
    }

    public Context createPackageContextAsUser(String packageName, int flags, UserHandle user) throws NameNotFoundException {
        throw new UnsupportedOperationException();
    }

    public int getUserId() {
        throw new UnsupportedOperationException();
    }

    public Context createConfigurationContext(Configuration overrideConfiguration) {
        throw new UnsupportedOperationException();
    }

    public Context createDisplayContext(Display display) {
        throw new UnsupportedOperationException();
    }

    public boolean isRestricted() {
        throw new UnsupportedOperationException();
    }

    public DisplayAdjustments getDisplayAdjustments(int displayId) {
        throw new UnsupportedOperationException();
    }

    public Display getDisplay() {
        throw new UnsupportedOperationException();
    }

    public void updateDisplay(int displayId) {
        throw new UnsupportedOperationException();
    }

    public File[] getExternalFilesDirs(String type) {
        throw new UnsupportedOperationException();
    }

    public File[] getObbDirs() {
        throw new UnsupportedOperationException();
    }

    public File[] getExternalCacheDirs() {
        throw new UnsupportedOperationException();
    }

    public File[] getExternalMediaDirs() {
        throw new UnsupportedOperationException();
    }

    public File getPreloadsFileCache() {
        throw new UnsupportedOperationException();
    }

    public Context createDeviceProtectedStorageContext() {
        throw new UnsupportedOperationException();
    }

    public Context createCredentialProtectedStorageContext() {
        throw new UnsupportedOperationException();
    }

    public boolean isDeviceProtectedStorage() {
        throw new UnsupportedOperationException();
    }

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
