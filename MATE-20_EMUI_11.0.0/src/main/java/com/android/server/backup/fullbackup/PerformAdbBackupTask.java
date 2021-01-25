package com.android.server.backup.fullbackup;

import android.app.backup.IFullBackupRestoreObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.Slog;
import com.android.server.AppWidgetBackupBridge;
import com.android.server.backup.BackupManagerService;
import com.android.server.backup.BackupPasswordManager;
import com.android.server.backup.BackupRestoreTask;
import com.android.server.backup.KeyValueAdbBackupEngine;
import com.android.server.backup.UserBackupManagerService;
import com.android.server.backup.utils.AppBackupUtils;
import com.android.server.backup.utils.PasswordUtils;
import com.android.server.job.controllers.JobStatus;
import com.android.server.pm.DumpState;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class PerformAdbBackupTask extends FullBackupTask implements BackupRestoreTask {
    private final boolean mAllApps;
    private final boolean mCompress;
    private final int mCurrentOpToken;
    private final String mCurrentPassword;
    private PackageInfo mCurrentTarget;
    private final boolean mDoWidgets;
    private final String mEncryptPassword;
    private final boolean mIncludeApks;
    private final boolean mIncludeObbs;
    private final boolean mIncludeShared;
    private final boolean mIncludeSystem;
    private final boolean mKeyValue;
    private final AtomicBoolean mLatch;
    private final ParcelFileDescriptor mOutputFile;
    private final ArrayList<String> mPackages;
    private final UserBackupManagerService mUserBackupManagerService;

    public PerformAdbBackupTask(UserBackupManagerService backupManagerService, ParcelFileDescriptor fd, IFullBackupRestoreObserver observer, boolean includeApks, boolean includeObbs, boolean includeShared, boolean doWidgets, String curPassword, String encryptPassword, boolean doAllApps, boolean doSystem, boolean doCompress, boolean doKeyValue, String[] packages, AtomicBoolean latch) {
        super(observer);
        ArrayList<String> arrayList;
        this.mUserBackupManagerService = backupManagerService;
        this.mCurrentOpToken = backupManagerService.generateRandomIntegerToken();
        this.mLatch = latch;
        this.mOutputFile = fd;
        this.mIncludeApks = includeApks;
        this.mIncludeObbs = includeObbs;
        this.mIncludeShared = includeShared;
        this.mDoWidgets = doWidgets;
        this.mAllApps = doAllApps;
        this.mIncludeSystem = doSystem;
        if (packages == null) {
            arrayList = new ArrayList<>();
        } else {
            arrayList = new ArrayList<>(Arrays.asList(packages));
        }
        this.mPackages = arrayList;
        this.mCurrentPassword = curPassword;
        if (encryptPassword == null || "".equals(encryptPassword)) {
            this.mEncryptPassword = curPassword;
        } else {
            this.mEncryptPassword = encryptPassword;
        }
        this.mCompress = doCompress;
        this.mKeyValue = doKeyValue;
    }

    private void addPackagesToSet(TreeMap<String, PackageInfo> set, List<String> pkgNames) {
        for (String pkgName : pkgNames) {
            if (!set.containsKey(pkgName)) {
                try {
                    set.put(pkgName, this.mUserBackupManagerService.getPackageManager().getPackageInfo(pkgName, DumpState.DUMP_HWFEATURES));
                } catch (PackageManager.NameNotFoundException e) {
                    Slog.w(BackupManagerService.TAG, "Unknown package " + pkgName + ", skipping");
                }
            }
        }
    }

    private OutputStream emitAesBackupHeader(StringBuilder headerbuf, OutputStream ofstream) throws Exception {
        byte[] newUserSalt = this.mUserBackupManagerService.randomBytes(512);
        SecretKey userKey = PasswordUtils.buildPasswordKey(BackupPasswordManager.PBKDF_CURRENT, this.mEncryptPassword, newUserSalt, 10000);
        byte[] masterPw = new byte[32];
        this.mUserBackupManagerService.getRng().nextBytes(masterPw);
        byte[] checksumSalt = this.mUserBackupManagerService.randomBytes(512);
        Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec masterKeySpec = new SecretKeySpec(masterPw, "AES");
        c.init(1, masterKeySpec);
        OutputStream finalOutput = new CipherOutputStream(ofstream, c);
        headerbuf.append(PasswordUtils.ENCRYPTION_ALGORITHM_NAME);
        headerbuf.append('\n');
        headerbuf.append(PasswordUtils.byteArrayToHex(newUserSalt));
        headerbuf.append('\n');
        headerbuf.append(PasswordUtils.byteArrayToHex(checksumSalt));
        headerbuf.append('\n');
        headerbuf.append(10000);
        headerbuf.append('\n');
        Cipher mkC = Cipher.getInstance("AES/CBC/PKCS5Padding");
        mkC.init(1, userKey);
        headerbuf.append(PasswordUtils.byteArrayToHex(mkC.getIV()));
        headerbuf.append('\n');
        byte[] IV = c.getIV();
        byte[] mk = masterKeySpec.getEncoded();
        byte[] checksum = PasswordUtils.makeKeyChecksum(BackupPasswordManager.PBKDF_CURRENT, masterKeySpec.getEncoded(), checksumSalt, 10000);
        ByteArrayOutputStream blob = new ByteArrayOutputStream(IV.length + mk.length + checksum.length + 3);
        DataOutputStream mkOut = new DataOutputStream(blob);
        mkOut.writeByte(IV.length);
        mkOut.write(IV);
        mkOut.writeByte(mk.length);
        mkOut.write(mk);
        mkOut.writeByte(checksum.length);
        mkOut.write(checksum);
        mkOut.flush();
        headerbuf.append(PasswordUtils.byteArrayToHex(mkC.doFinal(blob.toByteArray())));
        headerbuf.append('\n');
        return finalOutput;
    }

    private void finalizeBackup(OutputStream out) {
        try {
            out.write(new byte[1024]);
        } catch (IOException e) {
            Slog.w(BackupManagerService.TAG, "Error attempting to finalize backup stream");
        }
    }

    /* JADX INFO: Multiple debug info for r4v18 'out'  java.io.OutputStream: [D('i' int), D('out' java.io.OutputStream)] */
    /* JADX WARNING: Code restructure failed: missing block: B:264:0x05fe, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:265:0x05ff, code lost:
        r5 = true;
        r1 = r0;
        r4 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:266:0x060c, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:267:0x060d, code lost:
        r5 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:273:?, code lost:
        r2.flush();
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:275:0x062b, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:276:0x062c, code lost:
        android.util.Slog.e(com.android.server.backup.BackupManagerService.TAG, "IO error closing adb backup file: " + r0.getMessage());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:280:?, code lost:
        r34.mLatch.set(r5);
        r34.mLatch.notifyAll();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:287:0x0659, code lost:
        r5 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:291:?, code lost:
        r2.flush();
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:293:0x0677, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:294:0x0678, code lost:
        android.util.Slog.e(com.android.server.backup.BackupManagerService.TAG, "IO error closing adb backup file: " + r0.getMessage());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:298:?, code lost:
        r34.mLatch.set(r5);
        r34.mLatch.notifyAll();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:300:0x06a0, code lost:
        sendEndBackup();
        r0.tearDown();
        android.util.Slog.d(com.android.server.backup.BackupManagerService.TAG, "Full backup pass complete.");
        r34.mUserBackupManagerService.getWakelock().release();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:308:?, code lost:
        r4.flush();
        r4.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:310:0x06cc, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:311:0x06cd, code lost:
        android.util.Slog.e(com.android.server.backup.BackupManagerService.TAG, "IO error closing adb backup file: " + r0.getMessage());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:315:?, code lost:
        r34.mLatch.set(r5);
        r34.mLatch.notifyAll();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:317:0x06f5, code lost:
        sendEndBackup();
        r0.tearDown();
        android.util.Slog.d(com.android.server.backup.BackupManagerService.TAG, "Full backup pass complete.");
        r34.mUserBackupManagerService.getWakelock().release();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:318:0x070b, code lost:
        throw r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:335:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x012a, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x012b, code lost:
        r1 = r0;
        r4 = null;
        r5 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x0138, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x0139, code lost:
        r5 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x0145, code lost:
        r5 = true;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:247:0x05aa A[SYNTHETIC, Splitter:B:247:0x05aa] */
    /* JADX WARNING: Removed duplicated region for block: B:254:0x05d4 A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:264:0x05fe A[ExcHandler: all (r0v40 'th' java.lang.Throwable A[CUSTOM_DECLARE]), Splitter:B:35:0x011c] */
    /* JADX WARNING: Removed duplicated region for block: B:272:0x061f A[SYNTHETIC, Splitter:B:272:0x061f] */
    /* JADX WARNING: Removed duplicated region for block: B:279:0x0649 A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:286:0x0658 A[ExcHandler: RemoteException (e android.os.RemoteException), Splitter:B:35:0x011c] */
    /* JADX WARNING: Removed duplicated region for block: B:290:0x066b A[SYNTHETIC, Splitter:B:290:0x066b] */
    /* JADX WARNING: Removed duplicated region for block: B:297:0x0695 A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:307:0x06c0 A[SYNTHETIC, Splitter:B:307:0x06c0] */
    /* JADX WARNING: Removed duplicated region for block: B:314:0x06ea A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x012a A[ExcHandler: all (r0v139 'th' java.lang.Throwable A[CUSTOM_DECLARE]), Splitter:B:38:0x0120] */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x0144 A[ExcHandler: RemoteException (e android.os.RemoteException), Splitter:B:38:0x0120] */
    /* JADX WARNING: Removed duplicated region for block: B:71:0x01c0  */
    /* JADX WARNING: Removed duplicated region for block: B:88:0x0218  */
    @Override // java.lang.Runnable
    public void run() {
        boolean z;
        OutputStream out;
        Throwable th;
        Exception e;
        boolean encrypting;
        Exception e2;
        OutputStream finalOutput;
        PackageInfo pkg;
        boolean isSharedStorage;
        String str;
        PackageInfo pkg2;
        List<String> pkgs;
        Slog.i(BackupManagerService.TAG, "--- Performing adb backup" + (this.mKeyValue ? ", including key-value backups" : "") + " ---");
        TreeMap<String, PackageInfo> packagesToBackup = new TreeMap<>();
        FullBackupObbConnection obbConnection = new FullBackupObbConnection(this.mUserBackupManagerService);
        obbConnection.establish();
        sendStartBackup();
        PackageManager pm = this.mUserBackupManagerService.getPackageManager();
        boolean z2 = true;
        if (this.mAllApps) {
            List<PackageInfo> allPackages = pm.getInstalledPackages(DumpState.DUMP_HWFEATURES);
            for (int i = 0; i < allPackages.size(); i++) {
                PackageInfo pkg3 = allPackages.get(i);
                if (this.mIncludeSystem || (pkg3.applicationInfo.flags & 1) == 0) {
                    packagesToBackup.put(pkg3.packageName, pkg3);
                }
            }
        }
        if (this.mDoWidgets && (pkgs = AppWidgetBackupBridge.getWidgetParticipants(0)) != null) {
            addPackagesToSet(packagesToBackup, pkgs);
        }
        List<String> pkgs2 = this.mPackages;
        if (pkgs2 != null) {
            addPackagesToSet(packagesToBackup, pkgs2);
        }
        ArrayList<PackageInfo> keyValueBackupQueue = new ArrayList<>();
        Iterator<Map.Entry<String, PackageInfo>> iter = packagesToBackup.entrySet().iterator();
        while (iter.hasNext()) {
            PackageInfo pkg4 = iter.next().getValue();
            if (!AppBackupUtils.appIsEligibleForBackup(pkg4.applicationInfo, this.mUserBackupManagerService.getUserId()) || AppBackupUtils.appIsStopped(pkg4.applicationInfo)) {
                iter.remove();
                Slog.i(BackupManagerService.TAG, "Package " + pkg4.packageName + " is not eligible for backup, removing.");
            } else if (AppBackupUtils.appIsKeyValueOnly(pkg4)) {
                iter.remove();
                Slog.i(BackupManagerService.TAG, "Package " + pkg4.packageName + " is key-value.");
                keyValueBackupQueue.add(pkg4);
            }
        }
        ArrayList<PackageInfo> backupQueue = new ArrayList<>(packagesToBackup.values());
        FileOutputStream ofstream = new FileOutputStream(this.mOutputFile.getFileDescriptor());
        OutputStream out2 = null;
        try {
            if (this.mEncryptPassword != null) {
                try {
                    if (this.mEncryptPassword.length() > 0) {
                        encrypting = true;
                        if (this.mUserBackupManagerService.deviceIsEncrypted() || encrypting) {
                            OutputStream finalOutput2 = ofstream;
                            if (this.mUserBackupManagerService.backupPasswordMatches(this.mCurrentPassword)) {
                                Slog.w(BackupManagerService.TAG, "Backup password mismatch; aborting");
                                if (0 != 0) {
                                    try {
                                        out2.flush();
                                        out2.close();
                                    } catch (IOException e3) {
                                        Slog.e(BackupManagerService.TAG, "IO error closing adb backup file: " + e3.getMessage());
                                    }
                                }
                                this.mOutputFile.close();
                                synchronized (this.mLatch) {
                                    this.mLatch.set(true);
                                    this.mLatch.notifyAll();
                                }
                                sendEndBackup();
                                obbConnection.tearDown();
                                Slog.d(BackupManagerService.TAG, "Full backup pass complete.");
                                this.mUserBackupManagerService.getWakelock().release();
                                return;
                            }
                            StringBuilder headerbuf = new StringBuilder(1024);
                            headerbuf.append(UserBackupManagerService.BACKUP_FILE_HEADER_MAGIC);
                            headerbuf.append(5);
                            headerbuf.append(this.mCompress ? "\n1\n" : "\n0\n");
                            if (encrypting) {
                                finalOutput2 = emitAesBackupHeader(headerbuf, finalOutput2);
                            } else {
                                headerbuf.append("none\n");
                            }
                            ofstream.write(headerbuf.toString().getBytes("UTF-8"));
                            if (this.mCompress) {
                                finalOutput = new DeflaterOutputStream(finalOutput2, new Deflater(9), true);
                            } else {
                                finalOutput = finalOutput2;
                            }
                            OutputStream out3 = finalOutput;
                            try {
                                if (this.mIncludeShared) {
                                    try {
                                        backupQueue.add(this.mUserBackupManagerService.getPackageManager().getPackageInfo(UserBackupManagerService.SHARED_BACKUP_AGENT_PACKAGE, 0));
                                    } catch (PackageManager.NameNotFoundException e4) {
                                        try {
                                            Slog.e(BackupManagerService.TAG, "Unable to find shared-storage backup handler");
                                        } catch (RemoteException e5) {
                                            out2 = out3;
                                            z = true;
                                            Slog.e(BackupManagerService.TAG, "App died during full backup");
                                            if (out2 != null) {
                                            }
                                            this.mOutputFile.close();
                                            synchronized (this.mLatch) {
                                            }
                                        } catch (Exception e6) {
                                            e = e6;
                                            out2 = out3;
                                            z = true;
                                            try {
                                                Slog.e(BackupManagerService.TAG, "Internal exception during full backup", e);
                                                if (out2 != null) {
                                                }
                                                this.mOutputFile.close();
                                                synchronized (this.mLatch) {
                                                }
                                            } catch (Throwable th2) {
                                                th = th2;
                                                out = out2;
                                                if (out != null) {
                                                }
                                                this.mOutputFile.close();
                                                synchronized (this.mLatch) {
                                                }
                                            }
                                        } catch (Throwable th3) {
                                            th = th3;
                                            out = out3;
                                            z = true;
                                            if (out != null) {
                                            }
                                            this.mOutputFile.close();
                                            synchronized (this.mLatch) {
                                            }
                                        }
                                    }
                                }
                                int N = backupQueue.size();
                                for (int i2 = 0; i2 < N; i2++) {
                                    try {
                                        pkg = backupQueue.get(i2);
                                        try {
                                            StringBuilder sb = new StringBuilder();
                                            try {
                                                sb.append("--- Performing full backup for package ");
                                                sb.append(pkg.packageName);
                                                sb.append(" ---");
                                                Slog.i(BackupManagerService.TAG, sb.toString());
                                                isSharedStorage = pkg.packageName.equals(UserBackupManagerService.SHARED_BACKUP_AGENT_PACKAGE);
                                            } catch (RemoteException e7) {
                                                out2 = out3;
                                                z = true;
                                                Slog.e(BackupManagerService.TAG, "App died during full backup");
                                                if (out2 != null) {
                                                }
                                                this.mOutputFile.close();
                                                synchronized (this.mLatch) {
                                                }
                                            } catch (Exception e8) {
                                                e = e8;
                                                out2 = out3;
                                                z = true;
                                                Slog.e(BackupManagerService.TAG, "Internal exception during full backup", e);
                                                if (out2 != null) {
                                                }
                                                this.mOutputFile.close();
                                                synchronized (this.mLatch) {
                                                }
                                            } catch (Throwable th4) {
                                                out = out3;
                                                th = th4;
                                                z = true;
                                                if (out != null) {
                                                }
                                                this.mOutputFile.close();
                                                synchronized (this.mLatch) {
                                                }
                                            }
                                        } catch (RemoteException e9) {
                                            out2 = out3;
                                            z = z2;
                                            Slog.e(BackupManagerService.TAG, "App died during full backup");
                                            if (out2 != null) {
                                            }
                                            this.mOutputFile.close();
                                            synchronized (this.mLatch) {
                                            }
                                        } catch (Exception e10) {
                                            e = e10;
                                            out2 = out3;
                                            z = z2;
                                            Slog.e(BackupManagerService.TAG, "Internal exception during full backup", e);
                                            if (out2 != null) {
                                            }
                                            this.mOutputFile.close();
                                            synchronized (this.mLatch) {
                                            }
                                        } catch (Throwable th5) {
                                            out = out3;
                                            th = th5;
                                            z = z2;
                                            if (out != null) {
                                            }
                                            this.mOutputFile.close();
                                            synchronized (this.mLatch) {
                                            }
                                        }
                                    } catch (RemoteException e11) {
                                        out2 = out3;
                                        z = z2;
                                        Slog.e(BackupManagerService.TAG, "App died during full backup");
                                        if (out2 != null) {
                                        }
                                        this.mOutputFile.close();
                                        synchronized (this.mLatch) {
                                        }
                                    } catch (Exception e12) {
                                        e = e12;
                                        out2 = out3;
                                        z = z2;
                                        Slog.e(BackupManagerService.TAG, "Internal exception during full backup", e);
                                        if (out2 != null) {
                                        }
                                        this.mOutputFile.close();
                                        synchronized (this.mLatch) {
                                        }
                                    } catch (Throwable th6) {
                                        out = out3;
                                        th = th6;
                                        z = z2;
                                        if (out != null) {
                                        }
                                        this.mOutputFile.close();
                                        synchronized (this.mLatch) {
                                        }
                                    }
                                    try {
                                        try {
                                            FullBackupEngine mBackupEngine = new FullBackupEngine(this.mUserBackupManagerService, out3, null, pkg, this.mIncludeApks, this, JobStatus.NO_LATEST_RUNTIME, this.mCurrentOpToken, 0);
                                            if (isSharedStorage) {
                                                str = "Shared storage";
                                                pkg2 = pkg;
                                            } else {
                                                pkg2 = pkg;
                                                try {
                                                    str = pkg2.packageName;
                                                } catch (RemoteException e13) {
                                                    out2 = out3;
                                                    z = true;
                                                    Slog.e(BackupManagerService.TAG, "App died during full backup");
                                                    if (out2 != null) {
                                                    }
                                                    this.mOutputFile.close();
                                                    synchronized (this.mLatch) {
                                                    }
                                                } catch (Exception e14) {
                                                    e = e14;
                                                    out2 = out3;
                                                    z = true;
                                                    Slog.e(BackupManagerService.TAG, "Internal exception during full backup", e);
                                                    if (out2 != null) {
                                                    }
                                                    this.mOutputFile.close();
                                                    synchronized (this.mLatch) {
                                                    }
                                                } catch (Throwable th7) {
                                                    out = out3;
                                                    th = th7;
                                                    z = true;
                                                    if (out != null) {
                                                    }
                                                    this.mOutputFile.close();
                                                    synchronized (this.mLatch) {
                                                    }
                                                }
                                            }
                                            sendOnBackupPackage(str);
                                            this.mCurrentTarget = pkg2;
                                            mBackupEngine.backupOnePackage();
                                            if (!this.mIncludeObbs || isSharedStorage) {
                                                out = out3;
                                            } else {
                                                out = out3;
                                                try {
                                                    if (!obbConnection.backupObbs(pkg2, out)) {
                                                        throw new RuntimeException("Failure writing OBB stack for " + pkg2);
                                                    }
                                                } catch (RemoteException e15) {
                                                    out2 = out;
                                                    z = true;
                                                    Slog.e(BackupManagerService.TAG, "App died during full backup");
                                                    if (out2 != null) {
                                                    }
                                                    this.mOutputFile.close();
                                                    synchronized (this.mLatch) {
                                                    }
                                                } catch (Exception e16) {
                                                    e = e16;
                                                    out2 = out;
                                                    z = true;
                                                    Slog.e(BackupManagerService.TAG, "Internal exception during full backup", e);
                                                    if (out2 != null) {
                                                    }
                                                    this.mOutputFile.close();
                                                    synchronized (this.mLatch) {
                                                    }
                                                } catch (Throwable th8) {
                                                    th = th8;
                                                    z = true;
                                                    if (out != null) {
                                                    }
                                                    this.mOutputFile.close();
                                                    synchronized (this.mLatch) {
                                                    }
                                                }
                                            }
                                            out3 = out;
                                            pm = pm;
                                            headerbuf = headerbuf;
                                            ofstream = ofstream;
                                            keyValueBackupQueue = keyValueBackupQueue;
                                            backupQueue = backupQueue;
                                            z2 = true;
                                        } catch (RemoteException e17) {
                                            out2 = out3;
                                            z = true;
                                            Slog.e(BackupManagerService.TAG, "App died during full backup");
                                            if (out2 != null) {
                                            }
                                            this.mOutputFile.close();
                                            synchronized (this.mLatch) {
                                            }
                                        } catch (Exception e18) {
                                            e = e18;
                                            out2 = out3;
                                            z = true;
                                            Slog.e(BackupManagerService.TAG, "Internal exception during full backup", e);
                                            if (out2 != null) {
                                            }
                                            this.mOutputFile.close();
                                            synchronized (this.mLatch) {
                                            }
                                        } catch (Throwable th9) {
                                            out = out3;
                                            th = th9;
                                            z = true;
                                            if (out != null) {
                                            }
                                            this.mOutputFile.close();
                                            synchronized (this.mLatch) {
                                            }
                                        }
                                    } catch (RemoteException e19) {
                                        out2 = out3;
                                        z = true;
                                        Slog.e(BackupManagerService.TAG, "App died during full backup");
                                        if (out2 != null) {
                                        }
                                        this.mOutputFile.close();
                                        synchronized (this.mLatch) {
                                        }
                                    } catch (Exception e20) {
                                        e = e20;
                                        out2 = out3;
                                        z = true;
                                        Slog.e(BackupManagerService.TAG, "Internal exception during full backup", e);
                                        if (out2 != null) {
                                        }
                                        this.mOutputFile.close();
                                        synchronized (this.mLatch) {
                                        }
                                    } catch (Throwable th10) {
                                        out = out3;
                                        th = th10;
                                        z = true;
                                        if (out != null) {
                                        }
                                        this.mOutputFile.close();
                                        synchronized (this.mLatch) {
                                        }
                                    }
                                }
                                out = out3;
                                try {
                                    if (this.mKeyValue) {
                                        try {
                                            Iterator<PackageInfo> it = keyValueBackupQueue.iterator();
                                            while (it.hasNext()) {
                                                PackageInfo keyValuePackage = it.next();
                                                Slog.i(BackupManagerService.TAG, "--- Performing key-value backup for package " + keyValuePackage.packageName + " ---");
                                                KeyValueAdbBackupEngine kvBackupEngine = new KeyValueAdbBackupEngine(out, keyValuePackage, this.mUserBackupManagerService, this.mUserBackupManagerService.getPackageManager(), this.mUserBackupManagerService.getBaseStateDir(), this.mUserBackupManagerService.getDataDir());
                                                sendOnBackupPackage(keyValuePackage.packageName);
                                                kvBackupEngine.backupOnePackage();
                                            }
                                        } catch (RemoteException e21) {
                                            out2 = out;
                                            z = true;
                                            Slog.e(BackupManagerService.TAG, "App died during full backup");
                                            if (out2 != null) {
                                            }
                                            this.mOutputFile.close();
                                            synchronized (this.mLatch) {
                                            }
                                        } catch (Exception e22) {
                                            e = e22;
                                            out2 = out;
                                            z = true;
                                            Slog.e(BackupManagerService.TAG, "Internal exception during full backup", e);
                                            if (out2 != null) {
                                            }
                                            this.mOutputFile.close();
                                            synchronized (this.mLatch) {
                                            }
                                        } catch (Throwable th11) {
                                            th = th11;
                                            z = true;
                                            if (out != null) {
                                            }
                                            this.mOutputFile.close();
                                            synchronized (this.mLatch) {
                                            }
                                        }
                                    }
                                    finalizeBackup(out);
                                    if (out != null) {
                                        try {
                                            out.flush();
                                            out.close();
                                        } catch (IOException e23) {
                                            Slog.e(BackupManagerService.TAG, "IO error closing adb backup file: " + e23.getMessage());
                                        }
                                    }
                                    this.mOutputFile.close();
                                    synchronized (this.mLatch) {
                                        this.mLatch.set(true);
                                        this.mLatch.notifyAll();
                                    }
                                    sendEndBackup();
                                    obbConnection.tearDown();
                                    Slog.d(BackupManagerService.TAG, "Full backup pass complete.");
                                    this.mUserBackupManagerService.getWakelock().release();
                                    return;
                                } catch (RemoteException e24) {
                                    z = true;
                                    out2 = out;
                                    Slog.e(BackupManagerService.TAG, "App died during full backup");
                                    if (out2 != null) {
                                    }
                                    this.mOutputFile.close();
                                    synchronized (this.mLatch) {
                                    }
                                } catch (Exception e25) {
                                    e = e25;
                                    z = true;
                                    out2 = out;
                                    Slog.e(BackupManagerService.TAG, "Internal exception during full backup", e);
                                    if (out2 != null) {
                                    }
                                    this.mOutputFile.close();
                                    synchronized (this.mLatch) {
                                    }
                                } catch (Throwable th12) {
                                    z = true;
                                    th = th12;
                                    if (out != null) {
                                    }
                                    this.mOutputFile.close();
                                    synchronized (this.mLatch) {
                                    }
                                }
                            } catch (RemoteException e26) {
                                z = true;
                                out2 = out3;
                                Slog.e(BackupManagerService.TAG, "App died during full backup");
                                if (out2 != null) {
                                }
                                this.mOutputFile.close();
                                synchronized (this.mLatch) {
                                }
                            } catch (Exception e27) {
                                e = e27;
                                z = true;
                                out2 = out3;
                                Slog.e(BackupManagerService.TAG, "Internal exception during full backup", e);
                                if (out2 != null) {
                                }
                                this.mOutputFile.close();
                                synchronized (this.mLatch) {
                                }
                            } catch (Throwable th13) {
                                out = out3;
                                z = true;
                                th = th13;
                                if (out != null) {
                                }
                                this.mOutputFile.close();
                                synchronized (this.mLatch) {
                                }
                            }
                        } else {
                            Slog.e(BackupManagerService.TAG, "Unencrypted backup of encrypted device; aborting");
                            if (0 != 0) {
                                try {
                                    out2.flush();
                                    out2.close();
                                } catch (IOException e28) {
                                    Slog.e(BackupManagerService.TAG, "IO error closing adb backup file: " + e28.getMessage());
                                }
                            }
                            this.mOutputFile.close();
                            synchronized (this.mLatch) {
                                this.mLatch.set(true);
                                this.mLatch.notifyAll();
                            }
                            sendEndBackup();
                            obbConnection.tearDown();
                            Slog.d(BackupManagerService.TAG, "Full backup pass complete.");
                            this.mUserBackupManagerService.getWakelock().release();
                            return;
                        }
                    }
                } catch (Exception e29) {
                    e2 = e29;
                    z = true;
                    try {
                        Slog.e(BackupManagerService.TAG, "Unable to emit archive header", e2);
                        if (0 != 0) {
                            try {
                                out2.flush();
                                out2.close();
                            } catch (IOException e30) {
                                Slog.e(BackupManagerService.TAG, "IO error closing adb backup file: " + e30.getMessage());
                                synchronized (this.mLatch) {
                                }
                            }
                        }
                        this.mOutputFile.close();
                        synchronized (this.mLatch) {
                            this.mLatch.set(z);
                            this.mLatch.notifyAll();
                        }
                        sendEndBackup();
                        obbConnection.tearDown();
                        Slog.d(BackupManagerService.TAG, "Full backup pass complete.");
                        this.mUserBackupManagerService.getWakelock().release();
                        return;
                    } catch (RemoteException e31) {
                        Slog.e(BackupManagerService.TAG, "App died during full backup");
                        if (out2 != null) {
                        }
                        this.mOutputFile.close();
                        synchronized (this.mLatch) {
                        }
                    } catch (Exception e32) {
                        e = e32;
                        Slog.e(BackupManagerService.TAG, "Internal exception during full backup", e);
                        if (out2 != null) {
                        }
                        this.mOutputFile.close();
                        synchronized (this.mLatch) {
                        }
                    }
                } catch (RemoteException e33) {
                } catch (Throwable th14) {
                }
            }
            encrypting = false;
            if (this.mUserBackupManagerService.deviceIsEncrypted()) {
            }
            OutputStream finalOutput22 = ofstream;
            if (this.mUserBackupManagerService.backupPasswordMatches(this.mCurrentPassword)) {
            }
        } catch (Exception e34) {
            e2 = e34;
            z = true;
            Slog.e(BackupManagerService.TAG, "Unable to emit archive header", e2);
            if (0 != 0) {
            }
            this.mOutputFile.close();
            synchronized (this.mLatch) {
            }
        } catch (RemoteException e35) {
        } catch (Throwable th15) {
        }
    }

    @Override // com.android.server.backup.BackupRestoreTask
    public void execute() {
    }

    @Override // com.android.server.backup.BackupRestoreTask
    public void operationComplete(long result) {
    }

    @Override // com.android.server.backup.BackupRestoreTask
    public void handleCancel(boolean cancelAll) {
        PackageInfo target = this.mCurrentTarget;
        Slog.w(BackupManagerService.TAG, "adb backup cancel of " + target);
        if (target != null) {
            this.mUserBackupManagerService.tearDownAgentAndKill(this.mCurrentTarget.applicationInfo);
        }
        this.mUserBackupManagerService.removeOperation(this.mCurrentOpToken);
    }
}
