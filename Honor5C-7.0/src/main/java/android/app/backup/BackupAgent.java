package android.app.backup;

import android.app.IBackupAgent.Stub;
import android.app.QueuedWork;
import android.app.backup.FullBackup.BackupScheme;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.ApplicationInfo;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.os.Process;
import android.os.RemoteException;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.system.StructStat;
import android.util.ArraySet;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import org.xmlpull.v1.XmlPullParserException;

public abstract class BackupAgent extends ContextWrapper {
    private static final boolean DEBUG = false;
    private static final String TAG = "BackupAgent";
    public static final int TYPE_DIRECTORY = 2;
    public static final int TYPE_EOF = 0;
    public static final int TYPE_FILE = 1;
    public static final int TYPE_SYMLINK = 3;
    private final IBinder mBinder;
    Handler mHandler;

    private class BackupServiceBinder extends Stub {
        private static final String TAG = "BackupServiceBinder";

        private BackupServiceBinder() {
        }

        public void doBackup(ParcelFileDescriptor oldState, ParcelFileDescriptor data, ParcelFileDescriptor newState, int token, IBackupManager callbackBinder) throws RemoteException {
            long ident = Binder.clearCallingIdentity();
            try {
                BackupAgent.this.onBackup(oldState, new BackupDataOutput(data.getFileDescriptor()), newState);
                BackupAgent.this.waitForSharedPrefs();
                Binder.restoreCallingIdentity(ident);
                try {
                    callbackBinder.opComplete(token, 0);
                } catch (RemoteException e) {
                }
            } catch (IOException ex) {
                Log.d(TAG, "onBackup (" + BackupAgent.this.getClass().getName() + ") threw", ex);
                throw new RuntimeException(ex);
            } catch (RuntimeException ex2) {
                Log.d(TAG, "onBackup (" + BackupAgent.this.getClass().getName() + ") threw", ex2);
                throw ex2;
            } catch (Throwable th) {
                BackupAgent.this.waitForSharedPrefs();
                Binder.restoreCallingIdentity(ident);
                try {
                    callbackBinder.opComplete(token, 0);
                } catch (RemoteException e2) {
                }
            }
        }

        public void doRestore(ParcelFileDescriptor data, int appVersionCode, ParcelFileDescriptor newState, int token, IBackupManager callbackBinder) throws RemoteException {
            long ident = Binder.clearCallingIdentity();
            try {
                BackupAgent.this.onRestore(new BackupDataInput(data.getFileDescriptor()), appVersionCode, newState);
                BackupAgent.this.waitForSharedPrefs();
                Binder.restoreCallingIdentity(ident);
                try {
                    callbackBinder.opComplete(token, 0);
                } catch (RemoteException e) {
                }
            } catch (IOException ex) {
                Log.d(TAG, "onRestore (" + BackupAgent.this.getClass().getName() + ") threw", ex);
                throw new RuntimeException(ex);
            } catch (RuntimeException ex2) {
                Log.d(TAG, "onRestore (" + BackupAgent.this.getClass().getName() + ") threw", ex2);
                throw ex2;
            } catch (Throwable th) {
                BackupAgent.this.waitForSharedPrefs();
                Binder.restoreCallingIdentity(ident);
                try {
                    callbackBinder.opComplete(token, 0);
                } catch (RemoteException e2) {
                }
            }
        }

        public void doFullBackup(ParcelFileDescriptor data, int token, IBackupManager callbackBinder) {
            long ident = Binder.clearCallingIdentity();
            BackupAgent.this.waitForSharedPrefs();
            try {
                BackupAgent.this.onFullBackup(new FullBackupDataOutput(data));
                BackupAgent.this.waitForSharedPrefs();
                try {
                    new FileOutputStream(data.getFileDescriptor()).write(new byte[4]);
                } catch (IOException e) {
                    Log.e(TAG, "Unable to finalize backup stream!");
                }
                Binder.restoreCallingIdentity(ident);
                try {
                    callbackBinder.opComplete(token, 0);
                } catch (RemoteException e2) {
                }
            } catch (IOException ex) {
                Log.d(TAG, "onFullBackup (" + BackupAgent.this.getClass().getName() + ") threw", ex);
                throw new RuntimeException(ex);
            } catch (RuntimeException ex2) {
                Log.d(TAG, "onFullBackup (" + BackupAgent.this.getClass().getName() + ") threw", ex2);
                throw ex2;
            } catch (Throwable th) {
                BackupAgent.this.waitForSharedPrefs();
                try {
                    new FileOutputStream(data.getFileDescriptor()).write(new byte[4]);
                } catch (IOException e3) {
                    Log.e(TAG, "Unable to finalize backup stream!");
                }
                Binder.restoreCallingIdentity(ident);
                try {
                    callbackBinder.opComplete(token, 0);
                } catch (RemoteException e4) {
                }
            }
        }

        public void doMeasureFullBackup(int token, IBackupManager callbackBinder) {
            long ident = Binder.clearCallingIdentity();
            FullBackupDataOutput measureOutput = new FullBackupDataOutput();
            BackupAgent.this.waitForSharedPrefs();
            try {
                BackupAgent.this.onFullBackup(measureOutput);
                Binder.restoreCallingIdentity(ident);
                try {
                    callbackBinder.opComplete(token, measureOutput.getSize());
                } catch (RemoteException e) {
                }
            } catch (IOException ex) {
                Log.d(TAG, "onFullBackup[M] (" + BackupAgent.this.getClass().getName() + ") threw", ex);
                throw new RuntimeException(ex);
            } catch (RuntimeException ex2) {
                Log.d(TAG, "onFullBackup[M] (" + BackupAgent.this.getClass().getName() + ") threw", ex2);
                throw ex2;
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
                try {
                    callbackBinder.opComplete(token, measureOutput.getSize());
                } catch (RemoteException e2) {
                }
            }
        }

        public void doRestoreFile(ParcelFileDescriptor data, long size, int type, String domain, String path, long mode, long mtime, int token, IBackupManager callbackBinder) throws RemoteException {
            long ident = Binder.clearCallingIdentity();
            try {
                BackupAgent.this.onRestoreFile(data, size, type, domain, path, mode, mtime);
                BackupAgent.this.waitForSharedPrefs();
                Binder.restoreCallingIdentity(ident);
                try {
                    callbackBinder.opComplete(token, 0);
                } catch (RemoteException e) {
                }
            } catch (IOException e2) {
                Log.d(TAG, "onRestoreFile (" + BackupAgent.this.getClass().getName() + ") threw", e2);
                throw new RuntimeException(e2);
            } catch (Throwable th) {
                BackupAgent.this.waitForSharedPrefs();
                Binder.restoreCallingIdentity(ident);
                try {
                    callbackBinder.opComplete(token, 0);
                } catch (RemoteException e3) {
                }
            }
        }

        public void doRestoreFinished(int token, IBackupManager callbackBinder) {
            long ident = Binder.clearCallingIdentity();
            try {
                BackupAgent.this.onRestoreFinished();
                BackupAgent.this.waitForSharedPrefs();
                Binder.restoreCallingIdentity(ident);
                try {
                    callbackBinder.opComplete(token, 0);
                } catch (RemoteException e) {
                }
            } catch (Exception e2) {
                Log.d(TAG, "onRestoreFinished (" + BackupAgent.this.getClass().getName() + ") threw", e2);
                throw e2;
            } catch (Throwable th) {
                BackupAgent.this.waitForSharedPrefs();
                Binder.restoreCallingIdentity(ident);
                try {
                    callbackBinder.opComplete(token, 0);
                } catch (RemoteException e3) {
                }
            }
        }

        public void fail(String message) {
            BackupAgent.this.getHandler().post(new FailRunnable(message));
        }

        public void doQuotaExceeded(long backupDataBytes, long quotaBytes) {
            long ident = Binder.clearCallingIdentity();
            try {
                BackupAgent.this.onQuotaExceeded(backupDataBytes, quotaBytes);
                BackupAgent.this.waitForSharedPrefs();
                Binder.restoreCallingIdentity(ident);
            } catch (Exception e) {
                Log.d(TAG, "onQuotaExceeded(" + BackupAgent.this.getClass().getName() + ") threw", e);
                throw e;
            } catch (Throwable th) {
                BackupAgent.this.waitForSharedPrefs();
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    static class FailRunnable implements Runnable {
        private String mMessage;

        FailRunnable(String message) {
            this.mMessage = message;
        }

        public void run() {
            throw new IllegalStateException(this.mMessage);
        }
    }

    class SharedPrefsSynchronizer implements Runnable {
        public final CountDownLatch mLatch;

        SharedPrefsSynchronizer() {
            this.mLatch = new CountDownLatch(BackupAgent.TYPE_FILE);
        }

        public void run() {
            QueuedWork.waitToFinish();
            this.mLatch.countDown();
        }
    }

    public abstract void onBackup(ParcelFileDescriptor parcelFileDescriptor, BackupDataOutput backupDataOutput, ParcelFileDescriptor parcelFileDescriptor2) throws IOException;

    public abstract void onRestore(BackupDataInput backupDataInput, int i, ParcelFileDescriptor parcelFileDescriptor) throws IOException;

    Handler getHandler() {
        if (this.mHandler == null) {
            this.mHandler = new Handler(Looper.getMainLooper());
        }
        return this.mHandler;
    }

    private void waitForSharedPrefs() {
        Handler h = getHandler();
        SharedPrefsSynchronizer s = new SharedPrefsSynchronizer();
        h.postAtFrontOfQueue(s);
        try {
            s.mLatch.await();
        } catch (InterruptedException e) {
        }
    }

    public BackupAgent() {
        super(null);
        this.mHandler = null;
        this.mBinder = new BackupServiceBinder().asBinder();
    }

    public void onCreate() {
    }

    public void onDestroy() {
    }

    public void onFullBackup(FullBackupDataOutput data) throws IOException {
        BackupScheme backupScheme = FullBackup.getBackupScheme(this);
        if (backupScheme.isFullBackupContentEnabled()) {
            try {
                String canonicalPath;
                Map<String, Set<String>> manifestIncludeMap = backupScheme.maybeParseAndGetCanonicalIncludePaths();
                ArraySet<String> manifestExcludeSet = backupScheme.maybeParseAndGetCanonicalExcludePaths();
                String packageName = getPackageName();
                ApplicationInfo appInfo = getApplicationInfo();
                Context ceContext = createCredentialProtectedStorageContext();
                String rootDir = ceContext.getDataDir().getCanonicalPath();
                String filesDir = ceContext.getFilesDir().getCanonicalPath();
                String noBackupDir = ceContext.getNoBackupFilesDir().getCanonicalPath();
                String databaseDir = ceContext.getDatabasePath("foo").getParentFile().getCanonicalPath();
                String sharedPrefsDir = ceContext.getSharedPreferencesPath("foo").getParentFile().getCanonicalPath();
                String cacheDir = ceContext.getCacheDir().getCanonicalPath();
                String codeCacheDir = ceContext.getCodeCacheDir().getCanonicalPath();
                Context deContext = createDeviceProtectedStorageContext();
                String deviceRootDir = deContext.getDataDir().getCanonicalPath();
                String deviceFilesDir = deContext.getFilesDir().getCanonicalPath();
                String deviceNoBackupDir = deContext.getNoBackupFilesDir().getCanonicalPath();
                String deviceDatabaseDir = deContext.getDatabasePath("foo").getParentFile().getCanonicalPath();
                String deviceSharedPrefsDir = deContext.getSharedPreferencesPath("foo").getParentFile().getCanonicalPath();
                String deviceCacheDir = deContext.getCacheDir().getCanonicalPath();
                String deviceCodeCacheDir = deContext.getCodeCacheDir().getCanonicalPath();
                if (appInfo.nativeLibraryDir != null) {
                    canonicalPath = new File(appInfo.nativeLibraryDir).getCanonicalPath();
                } else {
                    canonicalPath = null;
                }
                ArraySet<String> traversalExcludeSet = new ArraySet();
                traversalExcludeSet.add(filesDir);
                traversalExcludeSet.add(noBackupDir);
                traversalExcludeSet.add(databaseDir);
                traversalExcludeSet.add(sharedPrefsDir);
                traversalExcludeSet.add(cacheDir);
                traversalExcludeSet.add(codeCacheDir);
                traversalExcludeSet.add(deviceFilesDir);
                traversalExcludeSet.add(deviceNoBackupDir);
                traversalExcludeSet.add(deviceDatabaseDir);
                traversalExcludeSet.add(deviceSharedPrefsDir);
                traversalExcludeSet.add(deviceCacheDir);
                traversalExcludeSet.add(deviceCodeCacheDir);
                if (canonicalPath != null) {
                    traversalExcludeSet.add(canonicalPath);
                }
                applyXmlFiltersAndDoFullBackupForDomain(packageName, FullBackup.ROOT_TREE_TOKEN, manifestIncludeMap, manifestExcludeSet, traversalExcludeSet, data);
                traversalExcludeSet.add(rootDir);
                applyXmlFiltersAndDoFullBackupForDomain(packageName, FullBackup.DEVICE_ROOT_TREE_TOKEN, manifestIncludeMap, manifestExcludeSet, traversalExcludeSet, data);
                traversalExcludeSet.add(deviceRootDir);
                traversalExcludeSet.remove(filesDir);
                applyXmlFiltersAndDoFullBackupForDomain(packageName, FullBackup.FILES_TREE_TOKEN, manifestIncludeMap, manifestExcludeSet, traversalExcludeSet, data);
                traversalExcludeSet.add(filesDir);
                traversalExcludeSet.remove(deviceFilesDir);
                applyXmlFiltersAndDoFullBackupForDomain(packageName, FullBackup.DEVICE_FILES_TREE_TOKEN, manifestIncludeMap, manifestExcludeSet, traversalExcludeSet, data);
                traversalExcludeSet.add(deviceFilesDir);
                traversalExcludeSet.remove(databaseDir);
                applyXmlFiltersAndDoFullBackupForDomain(packageName, FullBackup.DATABASE_TREE_TOKEN, manifestIncludeMap, manifestExcludeSet, traversalExcludeSet, data);
                traversalExcludeSet.add(databaseDir);
                traversalExcludeSet.remove(deviceDatabaseDir);
                applyXmlFiltersAndDoFullBackupForDomain(packageName, FullBackup.DEVICE_DATABASE_TREE_TOKEN, manifestIncludeMap, manifestExcludeSet, traversalExcludeSet, data);
                traversalExcludeSet.add(deviceDatabaseDir);
                traversalExcludeSet.remove(sharedPrefsDir);
                applyXmlFiltersAndDoFullBackupForDomain(packageName, FullBackup.SHAREDPREFS_TREE_TOKEN, manifestIncludeMap, manifestExcludeSet, traversalExcludeSet, data);
                traversalExcludeSet.add(sharedPrefsDir);
                traversalExcludeSet.remove(deviceSharedPrefsDir);
                applyXmlFiltersAndDoFullBackupForDomain(packageName, FullBackup.DEVICE_SHAREDPREFS_TREE_TOKEN, manifestIncludeMap, manifestExcludeSet, traversalExcludeSet, data);
                traversalExcludeSet.add(deviceSharedPrefsDir);
                if (!(Process.myUid() == Process.SYSTEM_UID || getExternalFilesDir(null) == null)) {
                    applyXmlFiltersAndDoFullBackupForDomain(packageName, FullBackup.MANAGED_EXTERNAL_TREE_TOKEN, manifestIncludeMap, manifestExcludeSet, traversalExcludeSet, data);
                }
            } catch (Throwable e) {
                if (Log.isLoggable("BackupXmlParserLogging", TYPE_DIRECTORY)) {
                    Log.v("BackupXmlParserLogging", "Exception trying to parse fullBackupContent xml file! Aborting full backup.", e);
                }
            }
        }
    }

    public void onQuotaExceeded(long backupDataBytes, long quotaBytes) {
    }

    private void applyXmlFiltersAndDoFullBackupForDomain(String packageName, String domainToken, Map<String, Set<String>> includeMap, ArraySet<String> filterSet, ArraySet<String> traversalExcludeSet, FullBackupDataOutput data) throws IOException {
        if (includeMap == null || includeMap.size() == 0) {
            fullBackupFileTree(packageName, domainToken, FullBackup.getBackupScheme(this).tokenToDirectoryPath(domainToken), filterSet, traversalExcludeSet, data);
        } else if (includeMap.get(domainToken) != null) {
            for (String includeFile : (Set) includeMap.get(domainToken)) {
                fullBackupFileTree(packageName, domainToken, includeFile, filterSet, traversalExcludeSet, data);
            }
        }
    }

    public final void fullBackupFile(File file, FullBackupDataOutput output) {
        String efDir = null;
        ApplicationInfo appInfo = getApplicationInfo();
        try {
            Context ceContext = createCredentialProtectedStorageContext();
            String rootDir = ceContext.getDataDir().getCanonicalPath();
            String filesDir = ceContext.getFilesDir().getCanonicalPath();
            String nbFilesDir = ceContext.getNoBackupFilesDir().getCanonicalPath();
            String dbDir = ceContext.getDatabasePath("foo").getParentFile().getCanonicalPath();
            String spDir = ceContext.getSharedPreferencesPath("foo").getParentFile().getCanonicalPath();
            String cacheDir = ceContext.getCacheDir().getCanonicalPath();
            String codeCacheDir = ceContext.getCodeCacheDir().getCanonicalPath();
            Context deContext = createDeviceProtectedStorageContext();
            String deviceRootDir = deContext.getDataDir().getCanonicalPath();
            String deviceFilesDir = deContext.getFilesDir().getCanonicalPath();
            String deviceNbFilesDir = deContext.getNoBackupFilesDir().getCanonicalPath();
            String deviceDbDir = deContext.getDatabasePath("foo").getParentFile().getCanonicalPath();
            String deviceSpDir = deContext.getSharedPreferencesPath("foo").getParentFile().getCanonicalPath();
            String deviceCacheDir = deContext.getCacheDir().getCanonicalPath();
            String deviceCodeCacheDir = deContext.getCodeCacheDir().getCanonicalPath();
            String str;
            if (appInfo.nativeLibraryDir == null) {
                str = null;
            } else {
                str = new File(appInfo.nativeLibraryDir).getCanonicalPath();
            }
            if (Process.myUid() != Process.SYSTEM_UID) {
                File efLocation = getExternalFilesDir(null);
                if (efLocation != null) {
                    efDir = efLocation.getCanonicalPath();
                }
            }
            String filePath = file.getCanonicalPath();
            if (filePath.startsWith(cacheDir) || filePath.startsWith(codeCacheDir) || filePath.startsWith(nbFilesDir) || filePath.startsWith(deviceCacheDir) || filePath.startsWith(deviceCodeCacheDir) || filePath.startsWith(deviceNbFilesDir) || filePath.startsWith(r24)) {
                Log.w(TAG, "lib, cache, code_cache, and no_backup files are not backed up");
                return;
            }
            String domain;
            String rootpath;
            if (filePath.startsWith(dbDir)) {
                domain = FullBackup.DATABASE_TREE_TOKEN;
                rootpath = dbDir;
            } else if (filePath.startsWith(spDir)) {
                domain = FullBackup.SHAREDPREFS_TREE_TOKEN;
                rootpath = spDir;
            } else if (filePath.startsWith(filesDir)) {
                domain = FullBackup.FILES_TREE_TOKEN;
                rootpath = filesDir;
            } else if (filePath.startsWith(rootDir)) {
                domain = FullBackup.ROOT_TREE_TOKEN;
                rootpath = rootDir;
            } else if (filePath.startsWith(deviceDbDir)) {
                domain = FullBackup.DEVICE_DATABASE_TREE_TOKEN;
                rootpath = deviceDbDir;
            } else if (filePath.startsWith(deviceSpDir)) {
                domain = FullBackup.DEVICE_SHAREDPREFS_TREE_TOKEN;
                rootpath = deviceSpDir;
            } else if (filePath.startsWith(deviceFilesDir)) {
                domain = FullBackup.DEVICE_FILES_TREE_TOKEN;
                rootpath = deviceFilesDir;
            } else if (filePath.startsWith(deviceRootDir)) {
                domain = FullBackup.DEVICE_ROOT_TREE_TOKEN;
                rootpath = deviceRootDir;
            } else if (efDir == null || !filePath.startsWith(efDir)) {
                Log.w(TAG, "File " + filePath + " is in an unsupported location; skipping");
                return;
            } else {
                domain = FullBackup.MANAGED_EXTERNAL_TREE_TOKEN;
                rootpath = efDir;
            }
            FullBackup.backupToTar(getPackageName(), domain, null, rootpath, filePath, output);
        } catch (IOException e) {
            Log.w(TAG, "Unable to obtain canonical paths");
        }
    }

    protected final void fullBackupFileTree(String packageName, String domain, String startingPath, ArraySet<String> manifestExcludes, ArraySet<String> systemExcludes, FullBackupDataOutput output) {
        String domainPath = FullBackup.getBackupScheme(this).tokenToDirectoryPath(domain);
        if (domainPath != null) {
            File rootFile = new File(startingPath);
            if (rootFile.exists()) {
                LinkedList<File> scanQueue = new LinkedList();
                scanQueue.add(rootFile);
                while (scanQueue.size() > 0) {
                    File file = (File) scanQueue.remove(TYPE_EOF);
                    try {
                        StructStat stat = Os.lstat(file.getPath());
                        if (!OsConstants.S_ISLNK(stat.st_mode)) {
                            String filePath = file.getCanonicalPath();
                            if ((manifestExcludes == null || !manifestExcludes.contains(filePath)) && (systemExcludes == null || !systemExcludes.contains(filePath))) {
                                if (OsConstants.S_ISDIR(stat.st_mode)) {
                                    File[] contents = file.listFiles();
                                    if (contents != null) {
                                        int length = contents.length;
                                        for (int i = TYPE_EOF; i < length; i += TYPE_FILE) {
                                            scanQueue.add(TYPE_EOF, contents[i]);
                                        }
                                    }
                                }
                                FullBackup.backupToTar(packageName, domain, null, domainPath, filePath, output);
                            }
                        }
                    } catch (IOException e) {
                        if (Log.isLoggable("BackupXmlParserLogging", TYPE_DIRECTORY)) {
                            Log.v("BackupXmlParserLogging", "Error canonicalizing path of " + file);
                        }
                    } catch (ErrnoException e2) {
                        if (Log.isLoggable("BackupXmlParserLogging", TYPE_DIRECTORY)) {
                            Log.v("BackupXmlParserLogging", "Error scanning file " + file + " : " + e2);
                        }
                    }
                }
            }
        }
    }

    public void onRestoreFile(ParcelFileDescriptor data, long size, File destination, int type, long mode, long mtime) throws IOException {
        FullBackup.restoreFile(data, size, type, mode, mtime, isFileEligibleForRestore(destination) ? destination : null);
    }

    private boolean isFileEligibleForRestore(File destination) throws IOException {
        BackupScheme bs = FullBackup.getBackupScheme(this);
        if (bs.isFullBackupContentEnabled()) {
            String destinationCanonicalPath = destination.getCanonicalPath();
            try {
                Map<String, Set<String>> includes = bs.maybeParseAndGetCanonicalIncludePaths();
                ArraySet<String> excludes = bs.maybeParseAndGetCanonicalExcludePaths();
                if (excludes == null || !isFileSpecifiedInPathList(destination, excludes)) {
                    if (!(includes == null || includes.isEmpty())) {
                        boolean explicitlyIncluded = DEBUG;
                        for (Set<String> domainIncludes : includes.values()) {
                            explicitlyIncluded |= isFileSpecifiedInPathList(destination, domainIncludes);
                            if (explicitlyIncluded) {
                                break;
                            }
                        }
                        if (!explicitlyIncluded) {
                            if (Log.isLoggable("BackupXmlParserLogging", TYPE_DIRECTORY)) {
                                Log.v("BackupXmlParserLogging", "onRestoreFile: Trying to restore \"" + destinationCanonicalPath + "\" but it isn't specified" + " in the included files; skipping.");
                            }
                            return DEBUG;
                        }
                    }
                    return true;
                }
                if (Log.isLoggable("BackupXmlParserLogging", TYPE_DIRECTORY)) {
                    Log.v("BackupXmlParserLogging", "onRestoreFile: \"" + destinationCanonicalPath + "\": listed in" + " excludes; skipping.");
                }
                return DEBUG;
            } catch (XmlPullParserException e) {
                if (Log.isLoggable("BackupXmlParserLogging", TYPE_DIRECTORY)) {
                    Log.v("BackupXmlParserLogging", "onRestoreFile \"" + destinationCanonicalPath + "\" : Exception trying to parse fullBackupContent xml file!" + " Aborting onRestoreFile.", e);
                }
                return DEBUG;
            }
        }
        if (Log.isLoggable("BackupXmlParserLogging", TYPE_DIRECTORY)) {
            Log.v("BackupXmlParserLogging", "onRestoreFile \"" + destination.getCanonicalPath() + "\" : fullBackupContent not enabled for " + getPackageName());
        }
        return DEBUG;
    }

    private boolean isFileSpecifiedInPathList(File file, Collection<String> canonicalPathList) throws IOException {
        for (String canonicalPath : canonicalPathList) {
            File fileFromList = new File(canonicalPath);
            if (fileFromList.isDirectory()) {
                if (file.isDirectory()) {
                    return file.equals(fileFromList);
                }
                return file.getCanonicalPath().startsWith(canonicalPath);
            } else if (file.equals(fileFromList)) {
                return true;
            }
        }
        return DEBUG;
    }

    protected void onRestoreFile(ParcelFileDescriptor data, long size, int type, String domain, String path, long mode, long mtime) throws IOException {
        String basePath = FullBackup.getBackupScheme(this).tokenToDirectoryPath(domain);
        if (domain.equals(FullBackup.MANAGED_EXTERNAL_TREE_TOKEN)) {
            mode = -1;
        }
        if (basePath != null) {
            File outFile = new File(basePath, path);
            if (outFile.getCanonicalPath().startsWith(basePath + File.separatorChar)) {
                onRestoreFile(data, size, outFile, type, mode, mtime);
                return;
            }
        }
        FullBackup.restoreFile(data, size, type, mode, mtime, null);
    }

    public void onRestoreFinished() {
    }

    public final IBinder onBind() {
        return this.mBinder;
    }

    public void attach(Context context) {
        attachBaseContext(context);
    }
}
