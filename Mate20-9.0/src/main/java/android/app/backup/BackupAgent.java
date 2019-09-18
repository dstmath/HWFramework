package android.app.backup;

import android.app.IBackupAgent;
import android.app.QueuedWork;
import android.app.backup.FullBackup;
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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParserException;

public abstract class BackupAgent extends ContextWrapper {
    private static final boolean DEBUG = false;
    public static final int FLAG_CLIENT_SIDE_ENCRYPTION_ENABLED = 1;
    public static final int FLAG_DEVICE_TO_DEVICE_TRANSFER = 2;
    public static final int FLAG_FAKE_CLIENT_SIDE_ENCRYPTION_ENABLED = Integer.MIN_VALUE;
    private static final String TAG = "BackupAgent";
    public static final int TYPE_DIRECTORY = 2;
    public static final int TYPE_EOF = 0;
    public static final int TYPE_FILE = 1;
    public static final int TYPE_SYMLINK = 3;
    private final IBinder mBinder = new BackupServiceBinder().asBinder();
    Handler mHandler = null;

    private class BackupServiceBinder extends IBackupAgent.Stub {
        private static final String TAG = "BackupServiceBinder";

        private BackupServiceBinder() {
        }

        /* JADX WARNING: Removed duplicated region for block: B:35:0x00c7  */
        public void doBackup(ParcelFileDescriptor oldState, ParcelFileDescriptor data, ParcelFileDescriptor newState, long quotaBytes, int token, IBackupManager callbackBinder, int transportFlags) throws RemoteException {
            int i = token;
            IBackupManager iBackupManager = callbackBinder;
            long ident = Binder.clearCallingIdentity();
            try {
                try {
                    BackupAgent.this.onBackup(oldState, new BackupDataOutput(data.getFileDescriptor(), quotaBytes, transportFlags), newState);
                    BackupAgent.this.waitForSharedPrefs();
                    Binder.restoreCallingIdentity(ident);
                    try {
                        iBackupManager.opComplete(i, 0);
                    } catch (RemoteException e) {
                    }
                    if (Binder.getCallingPid() != Process.myPid()) {
                        IoUtils.closeQuietly(oldState);
                        IoUtils.closeQuietly(data);
                        IoUtils.closeQuietly(newState);
                    }
                } catch (IOException e2) {
                    ex = e2;
                    Log.d(TAG, "onBackup (" + BackupAgent.this.getClass().getName() + ") threw", ex);
                    throw new RuntimeException(ex);
                } catch (RuntimeException e3) {
                    ex = e3;
                    Log.d(TAG, "onBackup (" + BackupAgent.this.getClass().getName() + ") threw", ex);
                    throw ex;
                }
            } catch (IOException e4) {
                ex = e4;
                ParcelFileDescriptor parcelFileDescriptor = oldState;
                ParcelFileDescriptor parcelFileDescriptor2 = newState;
                Log.d(TAG, "onBackup (" + BackupAgent.this.getClass().getName() + ") threw", ex);
                throw new RuntimeException(ex);
            } catch (RuntimeException e5) {
                ex = e5;
                ParcelFileDescriptor parcelFileDescriptor3 = oldState;
                ParcelFileDescriptor parcelFileDescriptor4 = newState;
                Log.d(TAG, "onBackup (" + BackupAgent.this.getClass().getName() + ") threw", ex);
                throw ex;
            } catch (Throwable th) {
                th = th;
                Throwable th2 = th;
                BackupAgent.this.waitForSharedPrefs();
                Binder.restoreCallingIdentity(ident);
                iBackupManager.opComplete(i, 0);
                if (Binder.getCallingPid() != Process.myPid()) {
                }
                throw th2;
            }
        }

        public void doRestore(ParcelFileDescriptor data, long appVersionCode, ParcelFileDescriptor newState, int token, IBackupManager callbackBinder) throws RemoteException {
            long ident = Binder.clearCallingIdentity();
            BackupAgent.this.waitForSharedPrefs();
            try {
                BackupAgent.this.onRestore(new BackupDataInput(data.getFileDescriptor()), appVersionCode, newState);
                BackupAgent.this.reloadSharedPreferences();
                Binder.restoreCallingIdentity(ident);
                try {
                    callbackBinder.opComplete(token, 0);
                } catch (RemoteException e) {
                }
                if (Binder.getCallingPid() != Process.myPid()) {
                    IoUtils.closeQuietly(data);
                    IoUtils.closeQuietly(newState);
                }
            } catch (IOException ex) {
                Log.d(TAG, "onRestore (" + BackupAgent.this.getClass().getName() + ") threw", ex);
                throw new RuntimeException(ex);
            } catch (RuntimeException ex2) {
                Log.d(TAG, "onRestore (" + BackupAgent.this.getClass().getName() + ") threw", ex2);
                throw ex2;
            } catch (Throwable th) {
                BackupAgent.this.reloadSharedPreferences();
                Binder.restoreCallingIdentity(ident);
                try {
                    callbackBinder.opComplete(token, 0);
                } catch (RemoteException e2) {
                }
                if (Binder.getCallingPid() != Process.myPid()) {
                    IoUtils.closeQuietly(data);
                    IoUtils.closeQuietly(newState);
                }
                throw th;
            }
        }

        public void doFullBackup(ParcelFileDescriptor data, long quotaBytes, int token, IBackupManager callbackBinder, int transportFlags) {
            long ident = Binder.clearCallingIdentity();
            BackupAgent.this.waitForSharedPrefs();
            try {
                BackupAgent.this.onFullBackup(new FullBackupDataOutput(data, quotaBytes, transportFlags));
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
                if (Binder.getCallingPid() != Process.myPid()) {
                    IoUtils.closeQuietly(data);
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
                if (Binder.getCallingPid() != Process.myPid()) {
                    IoUtils.closeQuietly(data);
                }
                throw th;
            }
        }

        public void doMeasureFullBackup(long quotaBytes, int token, IBackupManager callbackBinder, int transportFlags) {
            long ident = Binder.clearCallingIdentity();
            FullBackupDataOutput measureOutput = new FullBackupDataOutput(quotaBytes, transportFlags);
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
                throw th;
            }
        }

        public void doRestoreFile(ParcelFileDescriptor data, long size, int type, String domain, String path, long mode, long mtime, int token, IBackupManager callbackBinder) throws RemoteException {
            int i = token;
            IBackupManager iBackupManager = callbackBinder;
            long ident = Binder.clearCallingIdentity();
            try {
                BackupAgent.this.onRestoreFile(data, size, type, domain, path, mode, mtime);
                BackupAgent.this.waitForSharedPrefs();
                BackupAgent.this.reloadSharedPreferences();
                Binder.restoreCallingIdentity(ident);
                try {
                    iBackupManager.opComplete(i, 0);
                } catch (RemoteException e) {
                }
                if (Binder.getCallingPid() != Process.myPid()) {
                    IoUtils.closeQuietly(data);
                }
            } catch (IOException e2) {
                Log.d(TAG, "onRestoreFile (" + BackupAgent.this.getClass().getName() + ") threw", e2);
                throw new RuntimeException(e2);
            } catch (Throwable th) {
                Throwable th2 = th;
                BackupAgent.this.waitForSharedPrefs();
                BackupAgent.this.reloadSharedPreferences();
                Binder.restoreCallingIdentity(ident);
                try {
                    iBackupManager.opComplete(i, 0);
                } catch (RemoteException e3) {
                }
                if (Binder.getCallingPid() != Process.myPid()) {
                    IoUtils.closeQuietly(data);
                }
                throw th2;
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
                throw th;
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
                throw th;
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
        public final CountDownLatch mLatch = new CountDownLatch(1);

        SharedPrefsSynchronizer() {
        }

        public void run() {
            QueuedWork.waitToFinish();
            this.mLatch.countDown();
        }
    }

    public abstract void onBackup(ParcelFileDescriptor parcelFileDescriptor, BackupDataOutput backupDataOutput, ParcelFileDescriptor parcelFileDescriptor2) throws IOException;

    public abstract void onRestore(BackupDataInput backupDataInput, int i, ParcelFileDescriptor parcelFileDescriptor) throws IOException;

    /* access modifiers changed from: package-private */
    public Handler getHandler() {
        if (this.mHandler == null) {
            this.mHandler = new Handler(Looper.getMainLooper());
        }
        return this.mHandler;
    }

    /* access modifiers changed from: private */
    public void waitForSharedPrefs() {
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
    }

    public void onCreate() {
    }

    public void onDestroy() {
    }

    public void onRestore(BackupDataInput data, long appVersionCode, ParcelFileDescriptor newState) throws IOException {
        onRestore(data, (int) appVersionCode, newState);
    }

    /*  JADX ERROR: NullPointerException in pass: CodeShrinkVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.instructions.args.InsnArg.wrapInstruction(InsnArg.java:117)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.inline(CodeShrinkVisitor.java:119)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.shrinkBlock(CodeShrinkVisitor.java:70)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.shrinkMethod(CodeShrinkVisitor.java:42)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.visit(CodeShrinkVisitor.java:34)
        */
    public void onFullBackup(android.app.backup.FullBackupDataOutput r36) throws java.io.IOException {
        /*
            r35 = this;
            android.app.backup.FullBackup$BackupScheme r1 = android.app.backup.FullBackup.getBackupScheme(r35)
            boolean r0 = r1.isFullBackupContentEnabled()
            if (r0 != 0) goto L_0x000b
            return
        L_0x000b:
            java.util.Map r5 = r1.maybeParseAndGetCanonicalIncludePaths()     // Catch:{ IOException | XmlPullParserException -> 0x01bc }
            android.util.ArraySet r6 = r1.maybeParseAndGetCanonicalExcludePaths()     // Catch:{ IOException | XmlPullParserException -> 0x01bc }
            java.lang.String r0 = r35.getPackageName()
            android.content.pm.ApplicationInfo r14 = r35.getApplicationInfo()
            android.content.Context r15 = r35.createCredentialProtectedStorageContext()
            java.io.File r2 = r15.getDataDir()
            java.lang.String r13 = r2.getCanonicalPath()
            java.io.File r2 = r15.getFilesDir()
            java.lang.String r12 = r2.getCanonicalPath()
            java.io.File r2 = r15.getNoBackupFilesDir()
            java.lang.String r11 = r2.getCanonicalPath()
            java.lang.String r2 = "foo"
            java.io.File r2 = r15.getDatabasePath(r2)
            java.io.File r2 = r2.getParentFile()
            java.lang.String r10 = r2.getCanonicalPath()
            java.lang.String r2 = "foo"
            java.io.File r2 = r15.getSharedPreferencesPath(r2)
            java.io.File r2 = r2.getParentFile()
            java.lang.String r9 = r2.getCanonicalPath()
            java.io.File r2 = r15.getCacheDir()
            java.lang.String r8 = r2.getCanonicalPath()
            java.io.File r2 = r15.getCodeCacheDir()
            java.lang.String r7 = r2.getCanonicalPath()
            android.content.Context r4 = r35.createDeviceProtectedStorageContext()
            java.io.File r2 = r4.getDataDir()
            java.lang.String r3 = r2.getCanonicalPath()
            java.io.File r2 = r4.getFilesDir()
            java.lang.String r2 = r2.getCanonicalPath()
            r16 = r1
            java.io.File r1 = r4.getNoBackupFilesDir()
            java.lang.String r1 = r1.getCanonicalPath()
            r17 = r3
            java.lang.String r3 = "foo"
            java.io.File r3 = r4.getDatabasePath(r3)
            java.io.File r3 = r3.getParentFile()
            java.lang.String r3 = r3.getCanonicalPath()
            r18 = r15
            java.lang.String r15 = "foo"
            java.io.File r15 = r4.getSharedPreferencesPath(r15)
            java.io.File r15 = r15.getParentFile()
            java.lang.String r15 = r15.getCanonicalPath()
            r19 = r13
            java.io.File r13 = r4.getCacheDir()
            java.lang.String r13 = r13.getCanonicalPath()
            r20 = r5
            java.io.File r5 = r4.getCodeCacheDir()
            java.lang.String r5 = r5.getCanonicalPath()
            r21 = r4
            java.lang.String r4 = r14.nativeLibraryDir
            r22 = r6
            if (r4 == 0) goto L_0x00cc
            java.io.File r4 = new java.io.File
            java.lang.String r6 = r14.nativeLibraryDir
            r4.<init>(r6)
            java.lang.String r6 = r4.getCanonicalPath()
            goto L_0x00cd
        L_0x00cc:
            r6 = 0
        L_0x00cd:
            android.util.ArraySet r4 = new android.util.ArraySet
            r4.<init>()
            r4.add(r12)
            r4.add(r11)
            r4.add(r10)
            r4.add(r9)
            r4.add(r8)
            r4.add(r7)
            r4.add(r2)
            r4.add(r1)
            r4.add(r3)
            r4.add(r15)
            r4.add(r13)
            r4.add(r5)
            if (r6 == 0) goto L_0x00fb
            r4.add(r6)
        L_0x00fb:
            java.lang.String r24 = "r"
            r25 = r1
            r1 = r2
            r2 = r35
            r26 = r14
            r27 = r15
            r14 = r17
            r15 = r3
            r3 = r0
            r28 = r4
            r17 = r21
            r4 = r24
            r21 = r7
            r7 = r28
            r24 = r8
            r8 = r36
            r33 = r20
            r20 = r5
            r5 = r33
            r34 = r22
            r22 = r6
            r6 = r34
            r2.applyXmlFiltersAndDoFullBackupForDomain(r3, r4, r5, r6, r7, r8)
            r2 = r19
            r3 = r28
            r3.add(r2)
            java.lang.String r4 = "d_r"
            r7 = r35
            r8 = r0
            r29 = r2
            r2 = r9
            r9 = r4
            r4 = r10
            r10 = r5
            r19 = r11
            r11 = r6
            r30 = r2
            r2 = r12
            r12 = r3
            r28 = r13
            r23 = r29
            r13 = r36
            r7.applyXmlFiltersAndDoFullBackupForDomain(r8, r9, r10, r11, r12, r13)
            r3.add(r14)
            r3.remove(r2)
            java.lang.String r9 = "f"
            r7.applyXmlFiltersAndDoFullBackupForDomain(r8, r9, r10, r11, r12, r13)
            r3.add(r2)
            r3.remove(r1)
            java.lang.String r9 = "d_f"
            r7.applyXmlFiltersAndDoFullBackupForDomain(r8, r9, r10, r11, r12, r13)
            r3.add(r1)
            r3.remove(r4)
            java.lang.String r9 = "db"
            r7.applyXmlFiltersAndDoFullBackupForDomain(r8, r9, r10, r11, r12, r13)
            r3.add(r4)
            r3.remove(r15)
            java.lang.String r9 = "d_db"
            r7.applyXmlFiltersAndDoFullBackupForDomain(r8, r9, r10, r11, r12, r13)
            r3.add(r15)
            r13 = r30
            r3.remove(r13)
            java.lang.String r9 = "sp"
            r31 = r1
            r1 = r13
            r13 = r36
            r7.applyXmlFiltersAndDoFullBackupForDomain(r8, r9, r10, r11, r12, r13)
            r3.add(r1)
            r13 = r27
            r3.remove(r13)
            java.lang.String r9 = "d_sp"
            r32 = r1
            r1 = r13
            r13 = r36
            r7.applyXmlFiltersAndDoFullBackupForDomain(r8, r9, r10, r11, r12, r13)
            r3.add(r1)
            int r7 = android.os.Process.myUid()
            r8 = 1000(0x3e8, float:1.401E-42)
            if (r7 == r8) goto L_0x01bb
            r13 = r35
            r7 = 0
            java.io.File r27 = r13.getExternalFilesDir(r7)
            if (r27 == 0) goto L_0x01bb
            java.lang.String r9 = "ef"
            r7 = r13
            r8 = r0
            r10 = r5
            r11 = r6
            r12 = r3
            r13 = r36
            r7.applyXmlFiltersAndDoFullBackupForDomain(r8, r9, r10, r11, r12, r13)
        L_0x01bb:
            return
        L_0x01bc:
            r0 = move-exception
            r16 = r1
            java.lang.String r1 = "BackupXmlParserLogging"
            r2 = 2
            boolean r1 = android.util.Log.isLoggable(r1, r2)
            if (r1 == 0) goto L_0x01cf
            java.lang.String r1 = "BackupXmlParserLogging"
            java.lang.String r2 = "Exception trying to parse fullBackupContent xml file! Aborting full backup."
            android.util.Log.v(r1, r2, r0)
        L_0x01cf:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: android.app.backup.BackupAgent.onFullBackup(android.app.backup.FullBackupDataOutput):void");
    }

    public void onQuotaExceeded(long backupDataBytes, long quotaBytes) {
    }

    private void applyXmlFiltersAndDoFullBackupForDomain(String packageName, String domainToken, Map<String, Set<FullBackup.BackupScheme.PathWithRequiredFlags>> includeMap, ArraySet<FullBackup.BackupScheme.PathWithRequiredFlags> filterSet, ArraySet<String> traversalExcludeSet, FullBackupDataOutput data) throws IOException {
        String str = domainToken;
        Map<String, Set<FullBackup.BackupScheme.PathWithRequiredFlags>> map = includeMap;
        if (map == null || map.size() == 0) {
            fullBackupFileTree(packageName, str, FullBackup.getBackupScheme(this).tokenToDirectoryPath(str), filterSet, traversalExcludeSet, data);
            return;
        }
        if (map.get(str) != null) {
            for (FullBackup.BackupScheme.PathWithRequiredFlags includeFile : map.get(str)) {
                if (areIncludeRequiredTransportFlagsSatisfied(includeFile.getRequiredFlags(), data.getTransportFlags())) {
                    fullBackupFileTree(packageName, str, includeFile.getPath(), filterSet, traversalExcludeSet, data);
                }
            }
        }
    }

    private boolean areIncludeRequiredTransportFlagsSatisfied(int includeFlags, int transportFlags) {
        return (transportFlags & includeFlags) == includeFlags;
    }

    public final void fullBackupFile(File file, FullBackupDataOutput output) {
        String deviceCodeCacheDir;
        String libDir;
        String efDir;
        String deviceCodeCacheDir2;
        String filePath;
        String domain;
        String rootpath;
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
            Context context = ceContext;
            String deviceCacheDir = deContext.getCacheDir().getCanonicalPath();
            try {
                Context context2 = deContext;
                deviceCodeCacheDir = deContext.getCodeCacheDir().getCanonicalPath();
                libDir = appInfo.nativeLibraryDir == null ? null : new File(appInfo.nativeLibraryDir).getCanonicalPath();
                ApplicationInfo applicationInfo = appInfo;
                if (Process.myUid() != 1000) {
                    try {
                        File efLocation = getExternalFilesDir(null);
                        if (efLocation != null) {
                            efDir = efLocation.getCanonicalPath();
                            deviceCodeCacheDir2 = deviceCodeCacheDir;
                            filePath = file.getCanonicalPath();
                            if (!filePath.startsWith(cacheDir) || filePath.startsWith(codeCacheDir) || filePath.startsWith(nbFilesDir) || filePath.startsWith(deviceCacheDir)) {
                                String str = libDir;
                                String str2 = rootDir;
                                String str3 = deviceCodeCacheDir2;
                            } else {
                                String str4 = deviceCacheDir;
                                String deviceCodeCacheDir3 = deviceCodeCacheDir2;
                                if (filePath.startsWith(deviceCodeCacheDir3) || filePath.startsWith(deviceNbFilesDir)) {
                                    String str5 = libDir;
                                    String str6 = rootDir;
                                } else if (filePath.startsWith(libDir)) {
                                    String str7 = deviceCodeCacheDir3;
                                    String str8 = libDir;
                                    String str9 = rootDir;
                                } else {
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
                                        String str10 = deviceCodeCacheDir3;
                                        String str11 = libDir;
                                        StringBuilder sb = new StringBuilder();
                                        String str12 = rootDir;
                                        sb.append("File ");
                                        sb.append(filePath);
                                        sb.append(" is in an unsupported location; skipping");
                                        Log.w(TAG, sb.toString());
                                        return;
                                    } else {
                                        domain = FullBackup.MANAGED_EXTERNAL_TREE_TOKEN;
                                        rootpath = efDir;
                                    }
                                    FullBackup.backupToTar(getPackageName(), domain, null, rootpath, filePath, output);
                                    return;
                                }
                            }
                            Log.w(TAG, "lib, cache, code_cache, and no_backup files are not backed up");
                        }
                    } catch (IOException e) {
                        Log.w(TAG, "Unable to obtain canonical paths");
                    }
                }
                efDir = null;
            } catch (IOException e2) {
                ApplicationInfo applicationInfo2 = appInfo;
                Log.w(TAG, "Unable to obtain canonical paths");
            }
            try {
                deviceCodeCacheDir2 = deviceCodeCacheDir;
                filePath = file.getCanonicalPath();
                if (!filePath.startsWith(cacheDir)) {
                }
                String str13 = libDir;
                String str22 = rootDir;
                String str32 = deviceCodeCacheDir2;
                Log.w(TAG, "lib, cache, code_cache, and no_backup files are not backed up");
            } catch (IOException e3) {
                String str14 = efDir;
                Log.w(TAG, "Unable to obtain canonical paths");
            }
        } catch (IOException e4) {
            ApplicationInfo applicationInfo3 = appInfo;
            Log.w(TAG, "Unable to obtain canonical paths");
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x00a6  */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x00d2  */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x0028 A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x0028 A[SYNTHETIC] */
    public final void fullBackupFileTree(String packageName, String domain, String startingPath, ArraySet<FullBackup.BackupScheme.PathWithRequiredFlags> manifestExcludes, ArraySet<String> systemExcludes, FullBackupDataOutput output) {
        ArraySet<FullBackup.BackupScheme.PathWithRequiredFlags> arraySet = manifestExcludes;
        ArraySet<String> arraySet2 = systemExcludes;
        String str = domain;
        String domainPath = FullBackup.getBackupScheme(this).tokenToDirectoryPath(str);
        if (domainPath != null) {
            File rootFile = new File(startingPath);
            if (rootFile.exists()) {
                LinkedList linkedList = new LinkedList();
                linkedList.add(rootFile);
                while (linkedList.size() > 0) {
                    File file = (File) linkedList.remove(0);
                    try {
                        StructStat stat = Os.lstat(file.getPath());
                        if (OsConstants.S_ISREG(stat.st_mode) || OsConstants.S_ISDIR(stat.st_mode)) {
                            String filePath = file.getCanonicalPath();
                            if (arraySet != null) {
                                try {
                                    if (manifestExcludesContainFilePath(arraySet, filePath)) {
                                    }
                                } catch (IOException e) {
                                    if (!Log.isLoggable("BackupXmlParserLogging", 2)) {
                                    }
                                } catch (ErrnoException e2) {
                                    e = e2;
                                    if (!Log.isLoggable("BackupXmlParserLogging", 2)) {
                                    }
                                }
                            }
                            if (arraySet2 == null || !arraySet2.contains(filePath)) {
                                if (OsConstants.S_ISDIR(stat.st_mode)) {
                                    File[] contents = file.listFiles();
                                    if (contents != null) {
                                        for (File entry : contents) {
                                            linkedList.add(0, entry);
                                        }
                                    }
                                }
                                FullBackup.backupToTar(packageName, str, null, domainPath, filePath, output);
                            }
                        }
                    } catch (IOException e3) {
                        if (!Log.isLoggable("BackupXmlParserLogging", 2)) {
                            Log.v("BackupXmlParserLogging", "Error canonicalizing path of " + file);
                        }
                    } catch (ErrnoException e4) {
                        e = e4;
                        if (!Log.isLoggable("BackupXmlParserLogging", 2)) {
                            Log.v("BackupXmlParserLogging", "Error scanning file " + file + " : " + e);
                        }
                    }
                }
            }
        }
    }

    private boolean manifestExcludesContainFilePath(ArraySet<FullBackup.BackupScheme.PathWithRequiredFlags> manifestExcludes, String filePath) {
        Iterator<FullBackup.BackupScheme.PathWithRequiredFlags> it = manifestExcludes.iterator();
        while (it.hasNext()) {
            String excludePath = it.next().getPath();
            if (excludePath != null && excludePath.equals(filePath)) {
                return true;
            }
        }
        return false;
    }

    public void onRestoreFile(ParcelFileDescriptor data, long size, File destination, int type, long mode, long mtime) throws IOException {
        File file = destination;
        FullBackup.restoreFile(data, size, type, mode, mtime, isFileEligibleForRestore(file) ? file : null);
    }

    private boolean isFileEligibleForRestore(File destination) throws IOException {
        FullBackup.BackupScheme bs = FullBackup.getBackupScheme(this);
        if (!bs.isFullBackupContentEnabled()) {
            if (Log.isLoggable("BackupXmlParserLogging", 2)) {
                Log.v("BackupXmlParserLogging", "onRestoreFile \"" + destination.getCanonicalPath() + "\" : fullBackupContent not enabled for " + getPackageName());
            }
            return false;
        }
        String destinationCanonicalPath = destination.getCanonicalPath();
        try {
            Map<String, Set<FullBackup.BackupScheme.PathWithRequiredFlags>> includes = bs.maybeParseAndGetCanonicalIncludePaths();
            ArraySet<FullBackup.BackupScheme.PathWithRequiredFlags> excludes = bs.maybeParseAndGetCanonicalExcludePaths();
            if (excludes == null || !isFileSpecifiedInPathList(destination, excludes)) {
                if (includes != null && !includes.isEmpty()) {
                    boolean explicitlyIncluded = false;
                    for (Set<FullBackup.BackupScheme.PathWithRequiredFlags> domainIncludes : includes.values()) {
                        explicitlyIncluded |= isFileSpecifiedInPathList(destination, domainIncludes);
                        if (explicitlyIncluded) {
                            break;
                        }
                    }
                    if (!explicitlyIncluded) {
                        if (Log.isLoggable("BackupXmlParserLogging", 2)) {
                            Log.v("BackupXmlParserLogging", "onRestoreFile: Trying to restore \"" + destinationCanonicalPath + "\" but it isn't specified in the included files; skipping.");
                        }
                        return false;
                    }
                }
                return true;
            }
            if (Log.isLoggable("BackupXmlParserLogging", 2)) {
                Log.v("BackupXmlParserLogging", "onRestoreFile: \"" + destinationCanonicalPath + "\": listed in excludes; skipping.");
            }
            return false;
        } catch (XmlPullParserException e) {
            if (Log.isLoggable("BackupXmlParserLogging", 2)) {
                Log.v("BackupXmlParserLogging", "onRestoreFile \"" + destinationCanonicalPath + "\" : Exception trying to parse fullBackupContent xml file! Aborting onRestoreFile.", e);
            }
            return false;
        }
    }

    private boolean isFileSpecifiedInPathList(File file, Collection<FullBackup.BackupScheme.PathWithRequiredFlags> canonicalPathList) throws IOException {
        for (FullBackup.BackupScheme.PathWithRequiredFlags canonical : canonicalPathList) {
            String canonicalPath = canonical.getPath();
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
        return false;
    }

    /* access modifiers changed from: protected */
    public void onRestoreFile(ParcelFileDescriptor data, long size, int type, String domain, String path, long mode, long mtime) throws IOException {
        long mode2;
        String str = domain;
        String basePath = FullBackup.getBackupScheme(this).tokenToDirectoryPath(str);
        if (str.equals(FullBackup.MANAGED_EXTERNAL_TREE_TOKEN)) {
            mode2 = -1;
        } else {
            mode2 = mode;
        }
        if (basePath != null) {
            File outFile = new File(basePath, path);
            String outPath = outFile.getCanonicalPath();
            if (outPath.startsWith(basePath + File.separatorChar)) {
                String str2 = outPath;
                onRestoreFile(data, size, outFile, type, mode2, mtime);
                return;
            }
        } else {
            String str3 = path;
        }
        FullBackup.restoreFile(data, size, type, mode2, mtime, null);
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
