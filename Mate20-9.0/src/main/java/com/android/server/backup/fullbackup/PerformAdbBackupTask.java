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
import com.android.server.backup.utils.AppBackupUtils;
import com.android.server.backup.utils.PasswordUtils;
import com.android.server.job.controllers.JobStatus;
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
    private BackupManagerService backupManagerService;
    boolean mAllApps;
    FullBackupEngine mBackupEngine;
    boolean mCompress;
    private final int mCurrentOpToken;
    String mCurrentPassword;
    PackageInfo mCurrentTarget;
    DeflaterOutputStream mDeflater;
    boolean mDoWidgets;
    String mEncryptPassword;
    boolean mIncludeApks;
    boolean mIncludeObbs;
    boolean mIncludeShared;
    boolean mIncludeSystem;
    boolean mKeyValue;
    final AtomicBoolean mLatch;
    ParcelFileDescriptor mOutputFile;
    ArrayList<String> mPackages;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public PerformAdbBackupTask(BackupManagerService backupManagerService2, ParcelFileDescriptor fd, IFullBackupRestoreObserver observer, boolean includeApks, boolean includeObbs, boolean includeShared, boolean doWidgets, String curPassword, String encryptPassword, boolean doAllApps, boolean doSystem, boolean doCompress, boolean doKeyValue, String[] packages, AtomicBoolean latch) {
        super(observer);
        ArrayList<String> arrayList;
        String str = curPassword;
        String str2 = encryptPassword;
        this.backupManagerService = backupManagerService2;
        this.mCurrentOpToken = backupManagerService2.generateRandomIntegerToken();
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
        this.mCurrentPassword = str;
        if (str2 == null || BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS.equals(str2)) {
            this.mEncryptPassword = str;
        } else {
            this.mEncryptPassword = str2;
        }
        this.mCompress = doCompress;
        this.mKeyValue = doKeyValue;
    }

    /* access modifiers changed from: package-private */
    public void addPackagesToSet(TreeMap<String, PackageInfo> set, List<String> pkgNames) {
        for (String pkgName : pkgNames) {
            if (!set.containsKey(pkgName)) {
                try {
                    set.put(pkgName, this.backupManagerService.getPackageManager().getPackageInfo(pkgName, 134217728));
                } catch (PackageManager.NameNotFoundException e) {
                    Slog.w(BackupManagerService.TAG, "Unknown package " + pkgName + ", skipping");
                }
            }
        }
    }

    private OutputStream emitAesBackupHeader(StringBuilder headerbuf, OutputStream ofstream) throws Exception {
        StringBuilder sb = headerbuf;
        byte[] newUserSalt = this.backupManagerService.randomBytes(512);
        SecretKey userKey = PasswordUtils.buildPasswordKey(BackupPasswordManager.PBKDF_CURRENT, this.mEncryptPassword, newUserSalt, 10000);
        byte[] masterPw = new byte[32];
        this.backupManagerService.getRng().nextBytes(masterPw);
        byte[] checksumSalt = this.backupManagerService.randomBytes(512);
        Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec masterKeySpec = new SecretKeySpec(masterPw, "AES");
        c.init(1, masterKeySpec);
        OutputStream finalOutput = new CipherOutputStream(ofstream, c);
        sb.append(PasswordUtils.ENCRYPTION_ALGORITHM_NAME);
        sb.append(10);
        sb.append(PasswordUtils.byteArrayToHex(newUserSalt));
        sb.append(10);
        sb.append(PasswordUtils.byteArrayToHex(checksumSalt));
        sb.append(10);
        sb.append(10000);
        sb.append(10);
        Cipher mkC = Cipher.getInstance("AES/CBC/PKCS5Padding");
        mkC.init(1, userKey);
        sb.append(PasswordUtils.byteArrayToHex(mkC.getIV()));
        sb.append(10);
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
        ByteArrayOutputStream byteArrayOutputStream = blob;
        sb.append(PasswordUtils.byteArrayToHex(mkC.doFinal(blob.toByteArray())));
        sb.append(10);
        return finalOutput;
    }

    private void finalizeBackup(OutputStream out) {
        try {
            out.write(new byte[1024]);
        } catch (IOException e) {
            Slog.w(BackupManagerService.TAG, "Error attempting to finalize backup stream");
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:105:0x023a, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:106:0x023b, code lost:
        r31 = r6;
        r32 = r7;
        r1 = r8;
        r19 = r9;
        r28 = r11;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:107:0x0246, code lost:
        r5 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:110:0x0257, code lost:
        r31 = r6;
        r32 = r7;
        r1 = r8;
        r19 = r9;
        r28 = r11;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:247:0x0576, code lost:
        r2 = r5;
        r31 = r6;
        r32 = r7;
        r27 = r10;
        r28 = r11;
        r33 = r19;
        r19 = r9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:250:0x0585, code lost:
        if (r12.mKeyValue == false) goto L_0x05eb;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:253:?, code lost:
        r4 = r33.iterator();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:255:0x0591, code lost:
        if (r4.hasNext() == false) goto L_0x05ed;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:256:0x0593, code lost:
        r5 = r4.next();
        android.util.Slog.i(com.android.server.backup.BackupManagerService.TAG, "--- Performing key-value backup for package " + r5.packageName + " ---");
        r20 = new com.android.server.backup.KeyValueAdbBackupEngine(r2, r5, r12.backupManagerService, r12.backupManagerService.getPackageManager(), r12.backupManagerService.getBaseStateDir(), r12.backupManagerService.getDataDir());
        sendOnBackupPackage(r5.packageName);
        r20.backupOnePackage();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:257:0x05e0, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:258:0x05e1, code lost:
        r29 = r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:259:0x05e5, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:261:0x05eb, code lost:
        r1 = r33;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:263:?, code lost:
        finalizeBackup(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:264:0x05f0, code lost:
        if (r2 == null) goto L_0x05fb;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:266:?, code lost:
        r2.flush();
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:267:0x05f9, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:269:0x05fb, code lost:
        r12.mOutputFile.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:270:0x0601, code lost:
        android.util.Slog.e(com.android.server.backup.BackupManagerService.TAG, "IO error closing adb backup file: " + r0.getMessage());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:272:0x061e, code lost:
        monitor-enter(r12.mLatch);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:274:?, code lost:
        r12.mLatch.set(true);
        r12.mLatch.notifyAll();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:280:0x0630, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:281:0x0631, code lost:
        r5 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:282:0x0634, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:283:0x0635, code lost:
        r1 = r33;
        r5 = true;
        r29 = r3;
        r3 = r2;
        r2 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:284:0x063e, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:285:0x063f, code lost:
        r1 = r33;
        r5 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:287:0x0645, code lost:
        r1 = r33;
        r5 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:294:0x067b, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:295:0x067c, code lost:
        r31 = r6;
        r32 = r7;
        r1 = r8;
        r19 = r9;
        r27 = r10;
        r28 = r11;
        r5 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:319:0x06e9, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:320:0x06ea, code lost:
        r31 = r6;
        r32 = r7;
        r1 = r8;
        r19 = r9;
        r28 = r11;
        r5 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:324:0x0702, code lost:
        r31 = r6;
        r32 = r7;
        r1 = r8;
        r19 = r9;
        r28 = r11;
        r5 = true;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:101:0x0222 A[SYNTHETIC, Splitter:B:101:0x0222] */
    /* JADX WARNING: Removed duplicated region for block: B:105:0x023a A[ExcHandler: all (th java.lang.Throwable), Splitter:B:116:0x026a] */
    /* JADX WARNING: Removed duplicated region for block: B:109:0x0256 A[ExcHandler: RemoteException (e android.os.RemoteException), Splitter:B:116:0x026a] */
    /* JADX WARNING: Removed duplicated region for block: B:171:0x0368 A[SYNTHETIC, Splitter:B:171:0x0368] */
    /* JADX WARNING: Removed duplicated region for block: B:259:0x05e5 A[ExcHandler: Exception (e java.lang.Exception), Splitter:B:252:0x0589] */
    /* JADX WARNING: Removed duplicated region for block: B:260:0x05e8 A[ExcHandler: RemoteException (e android.os.RemoteException), Splitter:B:252:0x0589] */
    /* JADX WARNING: Removed duplicated region for block: B:300:0x0692 A[SYNTHETIC, Splitter:B:300:0x0692] */
    /* JADX WARNING: Removed duplicated region for block: B:308:0x06bf A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:319:0x06e9 A[ExcHandler: all (th java.lang.Throwable), Splitter:B:101:0x0222] */
    /* JADX WARNING: Removed duplicated region for block: B:323:0x0701 A[ExcHandler: RemoteException (e android.os.RemoteException), Splitter:B:101:0x0222] */
    /* JADX WARNING: Removed duplicated region for block: B:334:0x0733 A[SYNTHETIC, Splitter:B:334:0x0733] */
    /* JADX WARNING: Removed duplicated region for block: B:342:0x0760 A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:355:0x0783 A[SYNTHETIC, Splitter:B:355:0x0783] */
    /* JADX WARNING: Removed duplicated region for block: B:363:0x07b0 A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:377:0x07dd A[SYNTHETIC, Splitter:B:377:0x07dd] */
    /* JADX WARNING: Removed duplicated region for block: B:385:0x080a A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:408:0x0576 A[EDGE_INSN: B:408:0x0576->B:247:0x0576 ?: BREAK  , SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:80:0x01c6  */
    public void run() {
        Iterator<Map.Entry<String, PackageInfo>> iter;
        boolean z;
        OutputStream out;
        Throwable th;
        OutputStream out2;
        boolean z2;
        StringBuilder headerbuf;
        String str;
        ArrayList<PackageInfo> keyValueBackupQueue;
        OutputStream finalOutput;
        boolean z3;
        OutputStream out3;
        int i;
        int N;
        int i2;
        boolean z4;
        boolean isSharedStorage;
        Iterator<Map.Entry<String, PackageInfo>> iter2;
        FullBackupEngine fullBackupEngine;
        PackageInfo pkg;
        int i3;
        OutputStream out4;
        FileOutputStream ofstream;
        ArrayList<PackageInfo> backupQueue;
        FullBackupEngine fullBackupEngine2;
        ArrayList<PackageInfo> keyValueBackupQueue2;
        Iterator<Map.Entry<String, PackageInfo>> iter3;
        StringBuilder headerbuf2;
        PackageManager pm;
        String str2;
        PackageInfo pkg2;
        String includeKeyValue = this.mKeyValue ? ", including key-value backups" : BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
        Slog.i(BackupManagerService.TAG, "--- Performing adb backup" + includeKeyValue + " ---");
        TreeMap<String, PackageInfo> packagesToBackup = new TreeMap<>();
        FullBackupObbConnection obbConnection = new FullBackupObbConnection(this.backupManagerService);
        obbConnection.establish();
        sendStartBackup();
        PackageManager pm2 = this.backupManagerService.getPackageManager();
        if (this.mAllApps) {
            List<PackageInfo> allPackages = pm2.getInstalledPackages(134217728);
            for (int i4 = 0; i4 < allPackages.size(); i4++) {
                PackageInfo pkg3 = allPackages.get(i4);
                if (this.mIncludeSystem || (pkg3.applicationInfo.flags & 1) == 0) {
                    packagesToBackup.put(pkg3.packageName, pkg3);
                }
            }
        }
        if (this.mDoWidgets) {
            List<String> pkgs = AppWidgetBackupBridge.getWidgetParticipants(0);
            if (pkgs != null) {
                addPackagesToSet(packagesToBackup, pkgs);
            }
        }
        if (this.mPackages != null) {
            addPackagesToSet(packagesToBackup, this.mPackages);
        }
        ArrayList<PackageInfo> keyValueBackupQueue3 = new ArrayList<>();
        Iterator<Map.Entry<String, PackageInfo>> iter4 = packagesToBackup.entrySet().iterator();
        while (true) {
            iter = iter4;
            if (!iter.hasNext()) {
                break;
            }
            PackageInfo pkg4 = (PackageInfo) iter.next().getValue();
            if (!AppBackupUtils.appIsEligibleForBackup(pkg4.applicationInfo, pm2) || AppBackupUtils.appIsStopped(pkg4.applicationInfo)) {
                iter.remove();
                Slog.i(BackupManagerService.TAG, "Package " + pkg4.packageName + " is not eligible for backup, removing.");
            } else if (AppBackupUtils.appIsKeyValueOnly(pkg4)) {
                iter.remove();
                Slog.i(BackupManagerService.TAG, "Package " + pkg4.packageName + " is key-value.");
                keyValueBackupQueue3.add(pkg4);
            }
            iter4 = iter;
        }
        ArrayList<PackageInfo> backupQueue2 = new ArrayList<>(packagesToBackup.values());
        FileOutputStream ofstream2 = new FileOutputStream(this.mOutputFile.getFileDescriptor());
        OutputStream out5 = null;
        PackageInfo pkg5 = null;
        try {
            if (this.mEncryptPassword != null) {
                try {
                    if (this.mEncryptPassword.length() > 0) {
                        z2 = true;
                        boolean encrypting = z2;
                        if (this.backupManagerService.deviceIsEncrypted() || encrypting) {
                            OutputStream finalOutput2 = ofstream2;
                            if (this.backupManagerService.backupPasswordMatches(this.mCurrentPassword)) {
                                Slog.w(BackupManagerService.TAG, "Backup password mismatch; aborting");
                                if (out5 != null) {
                                    try {
                                        out5.flush();
                                        out5.close();
                                    } catch (IOException e) {
                                        Slog.e(BackupManagerService.TAG, "IO error closing adb backup file: " + e.getMessage());
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
                                this.backupManagerService.getWakelock().release();
                                return;
                            }
                            try {
                                headerbuf = new StringBuilder(1024);
                                headerbuf.append(BackupManagerService.BACKUP_FILE_HEADER_MAGIC);
                                headerbuf.append(5);
                                if (this.mCompress) {
                                    str = "\n1\n";
                                } else {
                                    str = "\n0\n";
                                }
                                headerbuf.append(str);
                                if (encrypting) {
                                    try {
                                        finalOutput2 = emitAesBackupHeader(headerbuf, finalOutput2);
                                    } catch (Exception e2) {
                                        e = e2;
                                        FileOutputStream fileOutputStream = ofstream2;
                                        ArrayList<PackageInfo> arrayList = backupQueue2;
                                        ArrayList<PackageInfo> arrayList2 = keyValueBackupQueue3;
                                        Iterator<Map.Entry<String, PackageInfo>> it = iter;
                                        StringBuilder sb = headerbuf;
                                        PackageManager packageManager = pm2;
                                        z = true;
                                        try {
                                            Slog.e(BackupManagerService.TAG, "Unable to emit archive header", e);
                                            if (out5 != null) {
                                            }
                                            this.mOutputFile.close();
                                            synchronized (this.mLatch) {
                                            }
                                            sendEndBackup();
                                            obbConnection.tearDown();
                                            Slog.d(BackupManagerService.TAG, "Full backup pass complete.");
                                            this.backupManagerService.getWakelock().release();
                                            return;
                                        } catch (RemoteException e3) {
                                            Slog.e(BackupManagerService.TAG, "App died during full backup");
                                            if (out5 != null) {
                                            }
                                            this.mOutputFile.close();
                                            synchronized (this.mLatch) {
                                            }
                                            sendEndBackup();
                                            obbConnection.tearDown();
                                            Slog.d(BackupManagerService.TAG, "Full backup pass complete.");
                                            this.backupManagerService.getWakelock().release();
                                        } catch (Exception e4) {
                                            e = e4;
                                            try {
                                                Slog.e(BackupManagerService.TAG, "Internal exception during full backup", e);
                                                if (out5 != null) {
                                                }
                                                this.mOutputFile.close();
                                                synchronized (this.mLatch) {
                                                }
                                                sendEndBackup();
                                                obbConnection.tearDown();
                                                Slog.d(BackupManagerService.TAG, "Full backup pass complete.");
                                                this.backupManagerService.getWakelock().release();
                                            } catch (Throwable th2) {
                                                th = th2;
                                                out2 = out5;
                                                th = th;
                                                if (out != null) {
                                                }
                                                this.mOutputFile.close();
                                                synchronized (this.mLatch) {
                                                }
                                                sendEndBackup();
                                                obbConnection.tearDown();
                                                Slog.d(BackupManagerService.TAG, "Full backup pass complete.");
                                                this.backupManagerService.getWakelock().release();
                                                throw th;
                                            }
                                        }
                                    } catch (RemoteException e5) {
                                    } catch (Throwable th3) {
                                    }
                                } else {
                                    headerbuf.append("none\n");
                                }
                                ofstream2.write(headerbuf.toString().getBytes("UTF-8"));
                                if (this.mCompress) {
                                    try {
                                        keyValueBackupQueue = keyValueBackupQueue3;
                                        z3 = true;
                                        try {
                                            finalOutput = new DeflaterOutputStream(finalOutput2, new Deflater(9), true);
                                        } catch (Exception e6) {
                                            e = e6;
                                            FileOutputStream fileOutputStream2 = ofstream2;
                                            ArrayList<PackageInfo> arrayList3 = backupQueue2;
                                            z = true;
                                            StringBuilder sb2 = headerbuf;
                                            PackageManager packageManager2 = pm2;
                                            ArrayList<PackageInfo> arrayList4 = keyValueBackupQueue;
                                            Iterator<Map.Entry<String, PackageInfo>> it2 = iter;
                                            Slog.e(BackupManagerService.TAG, "Unable to emit archive header", e);
                                            if (out5 != null) {
                                                try {
                                                    out5.flush();
                                                    out5.close();
                                                } catch (IOException e7) {
                                                    Slog.e(BackupManagerService.TAG, "IO error closing adb backup file: " + e7.getMessage());
                                                    synchronized (this.mLatch) {
                                                        this.mLatch.set(z);
                                                        this.mLatch.notifyAll();
                                                    }
                                                    sendEndBackup();
                                                    obbConnection.tearDown();
                                                    Slog.d(BackupManagerService.TAG, "Full backup pass complete.");
                                                    this.backupManagerService.getWakelock().release();
                                                    return;
                                                }
                                            }
                                            this.mOutputFile.close();
                                            synchronized (this.mLatch) {
                                            }
                                            sendEndBackup();
                                            obbConnection.tearDown();
                                            Slog.d(BackupManagerService.TAG, "Full backup pass complete.");
                                            this.backupManagerService.getWakelock().release();
                                            return;
                                        } catch (RemoteException e8) {
                                            z = z3;
                                            Slog.e(BackupManagerService.TAG, "App died during full backup");
                                            if (out5 != null) {
                                            }
                                            this.mOutputFile.close();
                                            synchronized (this.mLatch) {
                                            }
                                            sendEndBackup();
                                            obbConnection.tearDown();
                                            Slog.d(BackupManagerService.TAG, "Full backup pass complete.");
                                            this.backupManagerService.getWakelock().release();
                                        } catch (Throwable th4) {
                                            th = th4;
                                            FileOutputStream fileOutputStream3 = ofstream2;
                                            ArrayList<PackageInfo> arrayList5 = backupQueue2;
                                            z = true;
                                            PackageManager packageManager3 = pm2;
                                            ArrayList<PackageInfo> arrayList6 = keyValueBackupQueue;
                                            out2 = out5;
                                            Iterator<Map.Entry<String, PackageInfo>> it3 = iter;
                                            th = th;
                                            if (out != null) {
                                            }
                                            this.mOutputFile.close();
                                            synchronized (this.mLatch) {
                                            }
                                            sendEndBackup();
                                            obbConnection.tearDown();
                                            Slog.d(BackupManagerService.TAG, "Full backup pass complete.");
                                            this.backupManagerService.getWakelock().release();
                                            throw th;
                                        }
                                    } catch (Exception e9) {
                                        e = e9;
                                        FileOutputStream fileOutputStream4 = ofstream2;
                                        ArrayList<PackageInfo> arrayList7 = backupQueue2;
                                        StringBuilder sb3 = headerbuf;
                                        PackageManager packageManager4 = pm2;
                                        ArrayList<PackageInfo> arrayList8 = keyValueBackupQueue3;
                                        z = true;
                                        Iterator<Map.Entry<String, PackageInfo>> it4 = iter;
                                        Slog.e(BackupManagerService.TAG, "Unable to emit archive header", e);
                                        if (out5 != null) {
                                        }
                                        this.mOutputFile.close();
                                        synchronized (this.mLatch) {
                                        }
                                        sendEndBackup();
                                        obbConnection.tearDown();
                                        Slog.d(BackupManagerService.TAG, "Full backup pass complete.");
                                        this.backupManagerService.getWakelock().release();
                                        return;
                                    } catch (RemoteException e10) {
                                        FileOutputStream fileOutputStream5 = ofstream2;
                                        ArrayList<PackageInfo> arrayList9 = backupQueue2;
                                        PackageManager packageManager5 = pm2;
                                        ArrayList<PackageInfo> arrayList10 = keyValueBackupQueue3;
                                        z = true;
                                        Iterator<Map.Entry<String, PackageInfo>> it5 = iter;
                                        Slog.e(BackupManagerService.TAG, "App died during full backup");
                                        if (out5 != null) {
                                        }
                                        this.mOutputFile.close();
                                        synchronized (this.mLatch) {
                                        }
                                        sendEndBackup();
                                        obbConnection.tearDown();
                                        Slog.d(BackupManagerService.TAG, "Full backup pass complete.");
                                        this.backupManagerService.getWakelock().release();
                                    } catch (Throwable th5) {
                                        FileOutputStream fileOutputStream6 = ofstream2;
                                        ArrayList<PackageInfo> arrayList11 = backupQueue2;
                                        PackageManager packageManager6 = pm2;
                                        ArrayList<PackageInfo> arrayList12 = keyValueBackupQueue3;
                                        z = true;
                                        out = out5;
                                        Iterator<Map.Entry<String, PackageInfo>> it6 = iter;
                                        th = th5;
                                        if (out != null) {
                                        }
                                        this.mOutputFile.close();
                                        synchronized (this.mLatch) {
                                        }
                                        sendEndBackup();
                                        obbConnection.tearDown();
                                        Slog.d(BackupManagerService.TAG, "Full backup pass complete.");
                                        this.backupManagerService.getWakelock().release();
                                        throw th;
                                    }
                                } else {
                                    keyValueBackupQueue = keyValueBackupQueue3;
                                    z3 = true;
                                    finalOutput = finalOutput2;
                                }
                                out3 = finalOutput;
                            } catch (RemoteException e11) {
                            } catch (Exception e12) {
                                e = e12;
                                FileOutputStream fileOutputStream7 = ofstream2;
                                ArrayList<PackageInfo> arrayList13 = backupQueue2;
                                ArrayList<PackageInfo> arrayList14 = keyValueBackupQueue3;
                                Iterator<Map.Entry<String, PackageInfo>> it7 = iter;
                                PackageManager packageManager7 = pm2;
                                z = true;
                                Slog.e(BackupManagerService.TAG, "Internal exception during full backup", e);
                                if (out5 != null) {
                                }
                                this.mOutputFile.close();
                                synchronized (this.mLatch) {
                                }
                                sendEndBackup();
                                obbConnection.tearDown();
                                Slog.d(BackupManagerService.TAG, "Full backup pass complete.");
                                this.backupManagerService.getWakelock().release();
                            } catch (Throwable th6) {
                            }
                            try {
                                if (this.mIncludeShared) {
                                    try {
                                        i = 0;
                                        try {
                                            pkg5 = this.backupManagerService.getPackageManager().getPackageInfo(BackupManagerService.SHARED_BACKUP_AGENT_PACKAGE, 0);
                                            backupQueue2.add(pkg5);
                                        } catch (PackageManager.NameNotFoundException e13) {
                                        }
                                    } catch (PackageManager.NameNotFoundException e14) {
                                        i = 0;
                                        try {
                                            Slog.e(BackupManagerService.TAG, "Unable to find shared-storage backup handler");
                                            N = backupQueue2.size();
                                            while (true) {
                                                i2 = i;
                                                if (i2 < N) {
                                                }
                                                out3 = out5;
                                                i = i3 + 1;
                                                iter = iter3;
                                                headerbuf = headerbuf2;
                                                pm2 = pm;
                                                ofstream2 = ofstream;
                                                backupQueue2 = backupQueue;
                                                keyValueBackupQueue = keyValueBackupQueue2;
                                                z3 = true;
                                                pkg5 = pkg2;
                                            }
                                        } catch (RemoteException e15) {
                                            out5 = out3;
                                        } catch (Exception e16) {
                                            e = e16;
                                            out5 = out3;
                                            FileOutputStream fileOutputStream8 = ofstream2;
                                            ArrayList<PackageInfo> arrayList15 = backupQueue2;
                                            z = z3;
                                            PackageManager packageManager8 = pm2;
                                            ArrayList<PackageInfo> arrayList16 = keyValueBackupQueue;
                                            Iterator<Map.Entry<String, PackageInfo>> it8 = iter;
                                            Slog.e(BackupManagerService.TAG, "Internal exception during full backup", e);
                                            if (out5 != null) {
                                            }
                                            this.mOutputFile.close();
                                            synchronized (this.mLatch) {
                                            }
                                            sendEndBackup();
                                            obbConnection.tearDown();
                                            Slog.d(BackupManagerService.TAG, "Full backup pass complete.");
                                            this.backupManagerService.getWakelock().release();
                                        } catch (Throwable th7) {
                                            th = th7;
                                            PackageInfo packageInfo = pkg5;
                                            out = out3;
                                            FileOutputStream fileOutputStream9 = ofstream2;
                                            ArrayList<PackageInfo> arrayList17 = backupQueue2;
                                            z = z3;
                                            PackageManager packageManager9 = pm2;
                                            ArrayList<PackageInfo> arrayList18 = keyValueBackupQueue;
                                            Iterator<Map.Entry<String, PackageInfo>> it9 = iter;
                                            if (out != null) {
                                            }
                                            this.mOutputFile.close();
                                            synchronized (this.mLatch) {
                                            }
                                            sendEndBackup();
                                            obbConnection.tearDown();
                                            Slog.d(BackupManagerService.TAG, "Full backup pass complete.");
                                            this.backupManagerService.getWakelock().release();
                                            throw th;
                                        }
                                        sendEndBackup();
                                        obbConnection.tearDown();
                                        Slog.d(BackupManagerService.TAG, "Full backup pass complete.");
                                        this.backupManagerService.getWakelock().release();
                                    }
                                } else {
                                    i = 0;
                                }
                                N = backupQueue2.size();
                                while (true) {
                                    i2 = i;
                                    if (i2 < N) {
                                        break;
                                    }
                                    try {
                                        pkg5 = backupQueue2.get(i2);
                                        try {
                                            StringBuilder sb4 = new StringBuilder();
                                            try {
                                                sb4.append("--- Performing full backup for package ");
                                                sb4.append(pkg5.packageName);
                                                sb4.append(" ---");
                                                Slog.i(BackupManagerService.TAG, sb4.toString());
                                                isSharedStorage = pkg5.packageName.equals(BackupManagerService.SHARED_BACKUP_AGENT_PACKAGE);
                                                iter2 = iter;
                                            } catch (RemoteException e17) {
                                                PackageInfo packageInfo2 = pkg5;
                                                out5 = out3;
                                                FileOutputStream fileOutputStream10 = ofstream2;
                                                ArrayList<PackageInfo> arrayList19 = backupQueue2;
                                                PackageManager packageManager10 = pm2;
                                                ArrayList<PackageInfo> arrayList20 = keyValueBackupQueue;
                                                Iterator<Map.Entry<String, PackageInfo>> it10 = iter;
                                                ArrayList<PackageInfo> arrayList21 = arrayList20;
                                                z = true;
                                                Slog.e(BackupManagerService.TAG, "App died during full backup");
                                                if (out5 != null) {
                                                }
                                                this.mOutputFile.close();
                                                synchronized (this.mLatch) {
                                                }
                                                sendEndBackup();
                                                obbConnection.tearDown();
                                                Slog.d(BackupManagerService.TAG, "Full backup pass complete.");
                                                this.backupManagerService.getWakelock().release();
                                            } catch (Exception e18) {
                                                e = e18;
                                                PackageInfo packageInfo3 = pkg5;
                                                out5 = out3;
                                                FileOutputStream fileOutputStream11 = ofstream2;
                                                ArrayList<PackageInfo> arrayList22 = backupQueue2;
                                                PackageManager packageManager11 = pm2;
                                                ArrayList<PackageInfo> arrayList23 = keyValueBackupQueue;
                                                Iterator<Map.Entry<String, PackageInfo>> it11 = iter;
                                                ArrayList<PackageInfo> arrayList24 = arrayList23;
                                                z = true;
                                                Slog.e(BackupManagerService.TAG, "Internal exception during full backup", e);
                                                if (out5 != null) {
                                                }
                                                this.mOutputFile.close();
                                                synchronized (this.mLatch) {
                                                }
                                                sendEndBackup();
                                                obbConnection.tearDown();
                                                Slog.d(BackupManagerService.TAG, "Full backup pass complete.");
                                                this.backupManagerService.getWakelock().release();
                                            } catch (Throwable th8) {
                                                th = th8;
                                                FileOutputStream fileOutputStream12 = ofstream2;
                                                ArrayList<PackageInfo> arrayList25 = backupQueue2;
                                                PackageManager packageManager12 = pm2;
                                                ArrayList<PackageInfo> arrayList26 = keyValueBackupQueue;
                                                Iterator<Map.Entry<String, PackageInfo>> it12 = iter;
                                                PackageInfo packageInfo4 = pkg5;
                                                out = out3;
                                                ArrayList<PackageInfo> arrayList27 = arrayList26;
                                                z4 = true;
                                                th = th;
                                                if (out != null) {
                                                }
                                                this.mOutputFile.close();
                                                synchronized (this.mLatch) {
                                                }
                                                sendEndBackup();
                                                obbConnection.tearDown();
                                                Slog.d(BackupManagerService.TAG, "Full backup pass complete.");
                                                this.backupManagerService.getWakelock().release();
                                                throw th;
                                            }
                                        } catch (RemoteException e19) {
                                            PackageInfo packageInfo5 = pkg5;
                                            out5 = out3;
                                            FileOutputStream fileOutputStream13 = ofstream2;
                                            ArrayList<PackageInfo> arrayList28 = backupQueue2;
                                            PackageManager packageManager13 = pm2;
                                            ArrayList<PackageInfo> arrayList29 = keyValueBackupQueue;
                                            Iterator<Map.Entry<String, PackageInfo>> it13 = iter;
                                            z = z3;
                                            ArrayList<PackageInfo> arrayList30 = arrayList29;
                                            Slog.e(BackupManagerService.TAG, "App died during full backup");
                                            if (out5 != null) {
                                            }
                                            this.mOutputFile.close();
                                            synchronized (this.mLatch) {
                                            }
                                            sendEndBackup();
                                            obbConnection.tearDown();
                                            Slog.d(BackupManagerService.TAG, "Full backup pass complete.");
                                            this.backupManagerService.getWakelock().release();
                                        } catch (Exception e20) {
                                            e = e20;
                                            PackageInfo packageInfo6 = pkg5;
                                            out5 = out3;
                                            FileOutputStream fileOutputStream14 = ofstream2;
                                            ArrayList<PackageInfo> arrayList31 = backupQueue2;
                                            PackageManager packageManager14 = pm2;
                                            ArrayList<PackageInfo> arrayList32 = keyValueBackupQueue;
                                            Iterator<Map.Entry<String, PackageInfo>> it14 = iter;
                                            z = z3;
                                            ArrayList<PackageInfo> arrayList33 = arrayList32;
                                            Slog.e(BackupManagerService.TAG, "Internal exception during full backup", e);
                                            if (out5 != null) {
                                            }
                                            this.mOutputFile.close();
                                            synchronized (this.mLatch) {
                                            }
                                            sendEndBackup();
                                            obbConnection.tearDown();
                                            Slog.d(BackupManagerService.TAG, "Full backup pass complete.");
                                            this.backupManagerService.getWakelock().release();
                                        } catch (Throwable th9) {
                                            th = th9;
                                            FileOutputStream fileOutputStream15 = ofstream2;
                                            ArrayList<PackageInfo> arrayList34 = backupQueue2;
                                            PackageManager packageManager15 = pm2;
                                            ArrayList<PackageInfo> arrayList35 = keyValueBackupQueue;
                                            Iterator<Map.Entry<String, PackageInfo>> it15 = iter;
                                            PackageInfo packageInfo7 = pkg5;
                                            out = out3;
                                            z4 = z3;
                                            ArrayList<PackageInfo> arrayList36 = arrayList35;
                                            th = th;
                                            if (out != null) {
                                            }
                                            this.mOutputFile.close();
                                            synchronized (this.mLatch) {
                                            }
                                            sendEndBackup();
                                            obbConnection.tearDown();
                                            Slog.d(BackupManagerService.TAG, "Full backup pass complete.");
                                            this.backupManagerService.getWakelock().release();
                                            throw th;
                                        }
                                    } catch (RemoteException e21) {
                                        out5 = out3;
                                        FileOutputStream fileOutputStream16 = ofstream2;
                                        ArrayList<PackageInfo> arrayList37 = backupQueue2;
                                        PackageManager packageManager16 = pm2;
                                        ArrayList<PackageInfo> arrayList38 = keyValueBackupQueue;
                                        Iterator<Map.Entry<String, PackageInfo>> it16 = iter;
                                        z = z3;
                                        ArrayList<PackageInfo> arrayList39 = arrayList38;
                                        Slog.e(BackupManagerService.TAG, "App died during full backup");
                                        if (out5 != null) {
                                        }
                                        this.mOutputFile.close();
                                        synchronized (this.mLatch) {
                                        }
                                        sendEndBackup();
                                        obbConnection.tearDown();
                                        Slog.d(BackupManagerService.TAG, "Full backup pass complete.");
                                        this.backupManagerService.getWakelock().release();
                                    } catch (Exception e22) {
                                        e = e22;
                                        out5 = out3;
                                        FileOutputStream fileOutputStream17 = ofstream2;
                                        ArrayList<PackageInfo> arrayList40 = backupQueue2;
                                        PackageManager packageManager17 = pm2;
                                        ArrayList<PackageInfo> arrayList41 = keyValueBackupQueue;
                                        Iterator<Map.Entry<String, PackageInfo>> it17 = iter;
                                        z = z3;
                                        ArrayList<PackageInfo> arrayList42 = arrayList41;
                                        Slog.e(BackupManagerService.TAG, "Internal exception during full backup", e);
                                        if (out5 != null) {
                                        }
                                        this.mOutputFile.close();
                                        synchronized (this.mLatch) {
                                        }
                                        sendEndBackup();
                                        obbConnection.tearDown();
                                        Slog.d(BackupManagerService.TAG, "Full backup pass complete.");
                                        this.backupManagerService.getWakelock().release();
                                    } catch (Throwable th10) {
                                        OutputStream outputStream = out3;
                                        FileOutputStream fileOutputStream18 = ofstream2;
                                        ArrayList<PackageInfo> arrayList43 = backupQueue2;
                                        PackageManager packageManager18 = pm2;
                                        ArrayList<PackageInfo> arrayList44 = keyValueBackupQueue;
                                        Iterator<Map.Entry<String, PackageInfo>> it18 = iter;
                                        PackageInfo packageInfo8 = pkg5;
                                        z = z3;
                                        ArrayList<PackageInfo> arrayList45 = arrayList44;
                                        out = outputStream;
                                        th = th10;
                                        if (out != null) {
                                        }
                                        this.mOutputFile.close();
                                        synchronized (this.mLatch) {
                                        }
                                        sendEndBackup();
                                        obbConnection.tearDown();
                                        Slog.d(BackupManagerService.TAG, "Full backup pass complete.");
                                        this.backupManagerService.getWakelock().release();
                                        throw th;
                                    }
                                    try {
                                        fullBackupEngine = fullBackupEngine;
                                        pkg = pkg5;
                                        i3 = i2;
                                        out4 = out3;
                                        ofstream = ofstream2;
                                        backupQueue = backupQueue2;
                                        fullBackupEngine2 = fullBackupEngine;
                                        keyValueBackupQueue2 = keyValueBackupQueue;
                                        iter3 = iter2;
                                        headerbuf2 = headerbuf;
                                        pm = pm2;
                                    } catch (RemoteException e23) {
                                        PackageInfo packageInfo9 = pkg5;
                                        out5 = out3;
                                        FileOutputStream fileOutputStream19 = ofstream2;
                                        ArrayList<PackageInfo> arrayList46 = backupQueue2;
                                        ArrayList<PackageInfo> arrayList47 = keyValueBackupQueue;
                                        Iterator<Map.Entry<String, PackageInfo>> it19 = iter2;
                                        PackageManager packageManager19 = pm2;
                                        ArrayList<PackageInfo> arrayList48 = arrayList47;
                                        z = true;
                                        Slog.e(BackupManagerService.TAG, "App died during full backup");
                                        if (out5 != null) {
                                            try {
                                                out5.flush();
                                                out5.close();
                                            } catch (IOException e24) {
                                                Slog.e(BackupManagerService.TAG, "IO error closing adb backup file: " + e24.getMessage());
                                                synchronized (this.mLatch) {
                                                }
                                                sendEndBackup();
                                                obbConnection.tearDown();
                                                Slog.d(BackupManagerService.TAG, "Full backup pass complete.");
                                                this.backupManagerService.getWakelock().release();
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
                                        this.backupManagerService.getWakelock().release();
                                    } catch (Exception e25) {
                                        e = e25;
                                        PackageInfo packageInfo10 = pkg5;
                                        out5 = out3;
                                        FileOutputStream fileOutputStream20 = ofstream2;
                                        ArrayList<PackageInfo> arrayList49 = backupQueue2;
                                        ArrayList<PackageInfo> arrayList50 = keyValueBackupQueue;
                                        Iterator<Map.Entry<String, PackageInfo>> it20 = iter2;
                                        PackageManager packageManager20 = pm2;
                                        ArrayList<PackageInfo> arrayList51 = arrayList50;
                                        z = true;
                                        Slog.e(BackupManagerService.TAG, "Internal exception during full backup", e);
                                        if (out5 != null) {
                                            try {
                                                out5.flush();
                                                out5.close();
                                            } catch (IOException e26) {
                                                Slog.e(BackupManagerService.TAG, "IO error closing adb backup file: " + e26.getMessage());
                                                synchronized (this.mLatch) {
                                                }
                                                sendEndBackup();
                                                obbConnection.tearDown();
                                                Slog.d(BackupManagerService.TAG, "Full backup pass complete.");
                                                this.backupManagerService.getWakelock().release();
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
                                        this.backupManagerService.getWakelock().release();
                                    } catch (Throwable th11) {
                                        FileOutputStream fileOutputStream21 = ofstream2;
                                        ArrayList<PackageInfo> arrayList52 = backupQueue2;
                                        ArrayList<PackageInfo> arrayList53 = keyValueBackupQueue;
                                        Iterator<Map.Entry<String, PackageInfo>> it21 = iter2;
                                        PackageManager packageManager21 = pm2;
                                        PackageInfo packageInfo11 = pkg5;
                                        out = out3;
                                        ArrayList<PackageInfo> arrayList54 = arrayList53;
                                        z = true;
                                        th = th11;
                                        if (out != null) {
                                            try {
                                                out.flush();
                                                out.close();
                                            } catch (IOException e27) {
                                                Slog.e(BackupManagerService.TAG, "IO error closing adb backup file: " + e27.getMessage());
                                                synchronized (this.mLatch) {
                                                }
                                                sendEndBackup();
                                                obbConnection.tearDown();
                                                Slog.d(BackupManagerService.TAG, "Full backup pass complete.");
                                                this.backupManagerService.getWakelock().release();
                                                throw th;
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
                                        this.backupManagerService.getWakelock().release();
                                        throw th;
                                    }
                                    try {
                                        fullBackupEngine = new FullBackupEngine(this.backupManagerService, out3, null, pkg, this.mIncludeApks, this, JobStatus.NO_LATEST_RUNTIME, this.mCurrentOpToken, 0);
                                        this.mBackupEngine = fullBackupEngine2;
                                        if (isSharedStorage) {
                                            str2 = "Shared storage";
                                            pkg2 = pkg;
                                        } else {
                                            pkg2 = pkg;
                                            try {
                                                str2 = pkg2.packageName;
                                            } catch (RemoteException e28) {
                                                out5 = out4;
                                                pkg5 = pkg2;
                                                ArrayList<PackageInfo> arrayList55 = keyValueBackupQueue2;
                                                z = true;
                                                Slog.e(BackupManagerService.TAG, "App died during full backup");
                                                if (out5 != null) {
                                                }
                                                this.mOutputFile.close();
                                                synchronized (this.mLatch) {
                                                }
                                                sendEndBackup();
                                                obbConnection.tearDown();
                                                Slog.d(BackupManagerService.TAG, "Full backup pass complete.");
                                                this.backupManagerService.getWakelock().release();
                                            } catch (Exception e29) {
                                                e = e29;
                                                out5 = out4;
                                                pkg5 = pkg2;
                                                ArrayList<PackageInfo> arrayList56 = keyValueBackupQueue2;
                                                z = true;
                                                Slog.e(BackupManagerService.TAG, "Internal exception during full backup", e);
                                                if (out5 != null) {
                                                }
                                                this.mOutputFile.close();
                                                synchronized (this.mLatch) {
                                                }
                                                sendEndBackup();
                                                obbConnection.tearDown();
                                                Slog.d(BackupManagerService.TAG, "Full backup pass complete.");
                                                this.backupManagerService.getWakelock().release();
                                            } catch (Throwable th12) {
                                                PackageInfo packageInfo12 = pkg2;
                                                out = out4;
                                                ArrayList<PackageInfo> arrayList57 = keyValueBackupQueue2;
                                                z = true;
                                                th = th12;
                                                if (out != null) {
                                                }
                                                this.mOutputFile.close();
                                                synchronized (this.mLatch) {
                                                }
                                                sendEndBackup();
                                                obbConnection.tearDown();
                                                Slog.d(BackupManagerService.TAG, "Full backup pass complete.");
                                                this.backupManagerService.getWakelock().release();
                                                throw th;
                                            }
                                        }
                                        sendOnBackupPackage(str2);
                                        this.mCurrentTarget = pkg2;
                                        this.mBackupEngine.backupOnePackage();
                                        if (!this.mIncludeObbs || isSharedStorage) {
                                            out5 = out4;
                                        } else {
                                            out5 = out4;
                                            try {
                                                if (!obbConnection.backupObbs(pkg2, out5)) {
                                                    throw new RuntimeException("Failure writing OBB stack for " + pkg2);
                                                }
                                            } catch (RemoteException e30) {
                                                pkg5 = pkg2;
                                                z = true;
                                                Slog.e(BackupManagerService.TAG, "App died during full backup");
                                                if (out5 != null) {
                                                }
                                                this.mOutputFile.close();
                                                synchronized (this.mLatch) {
                                                }
                                                sendEndBackup();
                                                obbConnection.tearDown();
                                                Slog.d(BackupManagerService.TAG, "Full backup pass complete.");
                                                this.backupManagerService.getWakelock().release();
                                            } catch (Exception e31) {
                                                e = e31;
                                                pkg5 = pkg2;
                                                z = true;
                                                Slog.e(BackupManagerService.TAG, "Internal exception during full backup", e);
                                                if (out5 != null) {
                                                }
                                                this.mOutputFile.close();
                                                synchronized (this.mLatch) {
                                                }
                                                sendEndBackup();
                                                obbConnection.tearDown();
                                                Slog.d(BackupManagerService.TAG, "Full backup pass complete.");
                                                this.backupManagerService.getWakelock().release();
                                            } catch (Throwable th13) {
                                                th = th13;
                                                PackageInfo packageInfo13 = pkg2;
                                                out2 = out5;
                                                ArrayList<PackageInfo> arrayList58 = keyValueBackupQueue2;
                                                z = true;
                                                th = th;
                                                if (out != null) {
                                                }
                                                this.mOutputFile.close();
                                                synchronized (this.mLatch) {
                                                }
                                                sendEndBackup();
                                                obbConnection.tearDown();
                                                Slog.d(BackupManagerService.TAG, "Full backup pass complete.");
                                                this.backupManagerService.getWakelock().release();
                                                throw th;
                                            }
                                        }
                                        out3 = out5;
                                        i = i3 + 1;
                                        iter = iter3;
                                        headerbuf = headerbuf2;
                                        pm2 = pm;
                                        ofstream2 = ofstream;
                                        backupQueue2 = backupQueue;
                                        keyValueBackupQueue = keyValueBackupQueue2;
                                        z3 = true;
                                        pkg5 = pkg2;
                                    } catch (RemoteException e32) {
                                        out5 = out4;
                                        pkg5 = pkg;
                                        ArrayList<PackageInfo> arrayList59 = keyValueBackupQueue2;
                                        z = true;
                                        Slog.e(BackupManagerService.TAG, "App died during full backup");
                                        if (out5 != null) {
                                        }
                                        this.mOutputFile.close();
                                        synchronized (this.mLatch) {
                                        }
                                        sendEndBackup();
                                        obbConnection.tearDown();
                                        Slog.d(BackupManagerService.TAG, "Full backup pass complete.");
                                        this.backupManagerService.getWakelock().release();
                                    } catch (Exception e33) {
                                        e = e33;
                                        out5 = out4;
                                        pkg5 = pkg;
                                        ArrayList<PackageInfo> arrayList60 = keyValueBackupQueue2;
                                        z = true;
                                        Slog.e(BackupManagerService.TAG, "Internal exception during full backup", e);
                                        if (out5 != null) {
                                        }
                                        this.mOutputFile.close();
                                        synchronized (this.mLatch) {
                                        }
                                        sendEndBackup();
                                        obbConnection.tearDown();
                                        Slog.d(BackupManagerService.TAG, "Full backup pass complete.");
                                        this.backupManagerService.getWakelock().release();
                                    } catch (Throwable th14) {
                                        PackageInfo packageInfo14 = pkg;
                                        out = out4;
                                        ArrayList<PackageInfo> arrayList61 = keyValueBackupQueue2;
                                        z = true;
                                        th = th14;
                                        if (out != null) {
                                        }
                                        this.mOutputFile.close();
                                        synchronized (this.mLatch) {
                                        }
                                        sendEndBackup();
                                        obbConnection.tearDown();
                                        Slog.d(BackupManagerService.TAG, "Full backup pass complete.");
                                        this.backupManagerService.getWakelock().release();
                                        throw th;
                                    }
                                }
                            } catch (RemoteException e34) {
                                out5 = out3;
                                FileOutputStream fileOutputStream22 = ofstream2;
                                ArrayList<PackageInfo> arrayList62 = backupQueue2;
                                z = z3;
                                PackageManager packageManager22 = pm2;
                                ArrayList<PackageInfo> arrayList63 = keyValueBackupQueue;
                                Iterator<Map.Entry<String, PackageInfo>> it22 = iter;
                                Slog.e(BackupManagerService.TAG, "App died during full backup");
                                if (out5 != null) {
                                }
                                this.mOutputFile.close();
                                synchronized (this.mLatch) {
                                }
                                sendEndBackup();
                                obbConnection.tearDown();
                                Slog.d(BackupManagerService.TAG, "Full backup pass complete.");
                                this.backupManagerService.getWakelock().release();
                            } catch (Exception e35) {
                                e = e35;
                                out5 = out3;
                                FileOutputStream fileOutputStream23 = ofstream2;
                                ArrayList<PackageInfo> arrayList64 = backupQueue2;
                                z = z3;
                                PackageManager packageManager23 = pm2;
                                ArrayList<PackageInfo> arrayList65 = keyValueBackupQueue;
                                Iterator<Map.Entry<String, PackageInfo>> it23 = iter;
                                Slog.e(BackupManagerService.TAG, "Internal exception during full backup", e);
                                if (out5 != null) {
                                }
                                this.mOutputFile.close();
                                synchronized (this.mLatch) {
                                }
                                sendEndBackup();
                                obbConnection.tearDown();
                                Slog.d(BackupManagerService.TAG, "Full backup pass complete.");
                                this.backupManagerService.getWakelock().release();
                            } catch (Throwable th15) {
                                OutputStream outputStream2 = out3;
                                FileOutputStream fileOutputStream24 = ofstream2;
                                ArrayList<PackageInfo> arrayList66 = backupQueue2;
                                z = z3;
                                PackageManager packageManager24 = pm2;
                                ArrayList<PackageInfo> arrayList67 = keyValueBackupQueue;
                                Iterator<Map.Entry<String, PackageInfo>> it24 = iter;
                                PackageInfo packageInfo15 = pkg5;
                                out = outputStream2;
                                th = th15;
                                if (out != null) {
                                }
                                this.mOutputFile.close();
                                synchronized (this.mLatch) {
                                }
                                sendEndBackup();
                                obbConnection.tearDown();
                                Slog.d(BackupManagerService.TAG, "Full backup pass complete.");
                                this.backupManagerService.getWakelock().release();
                                throw th;
                            }
                            sendEndBackup();
                            obbConnection.tearDown();
                            Slog.d(BackupManagerService.TAG, "Full backup pass complete.");
                            this.backupManagerService.getWakelock().release();
                        }
                        Slog.e(BackupManagerService.TAG, "Unencrypted backup of encrypted device; aborting");
                        if (out5 != null) {
                            try {
                                out5.flush();
                                out5.close();
                            } catch (IOException e36) {
                                Slog.e(BackupManagerService.TAG, "IO error closing adb backup file: " + e36.getMessage());
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
                        this.backupManagerService.getWakelock().release();
                        return;
                    }
                } catch (RemoteException e37) {
                    FileOutputStream fileOutputStream25 = ofstream2;
                    ArrayList<PackageInfo> arrayList68 = backupQueue2;
                    ArrayList<PackageInfo> arrayList69 = keyValueBackupQueue3;
                    Iterator<Map.Entry<String, PackageInfo>> it25 = iter;
                    z = true;
                    PackageManager packageManager25 = pm2;
                    Slog.e(BackupManagerService.TAG, "App died during full backup");
                    if (out5 != null) {
                    }
                    this.mOutputFile.close();
                    synchronized (this.mLatch) {
                    }
                    sendEndBackup();
                    obbConnection.tearDown();
                    Slog.d(BackupManagerService.TAG, "Full backup pass complete.");
                    this.backupManagerService.getWakelock().release();
                } catch (Exception e38) {
                    e = e38;
                    FileOutputStream fileOutputStream26 = ofstream2;
                    ArrayList<PackageInfo> arrayList70 = backupQueue2;
                    ArrayList<PackageInfo> arrayList71 = keyValueBackupQueue3;
                    Iterator<Map.Entry<String, PackageInfo>> it26 = iter;
                    z = true;
                    PackageManager packageManager26 = pm2;
                    Slog.e(BackupManagerService.TAG, "Internal exception during full backup", e);
                    if (out5 != null) {
                    }
                    this.mOutputFile.close();
                    synchronized (this.mLatch) {
                    }
                    sendEndBackup();
                    obbConnection.tearDown();
                    Slog.d(BackupManagerService.TAG, "Full backup pass complete.");
                    this.backupManagerService.getWakelock().release();
                } catch (Throwable th16) {
                    th = th16;
                    FileOutputStream fileOutputStream27 = ofstream2;
                    ArrayList<PackageInfo> arrayList72 = backupQueue2;
                    ArrayList<PackageInfo> arrayList73 = keyValueBackupQueue3;
                    Iterator<Map.Entry<String, PackageInfo>> it27 = iter;
                    z = true;
                    PackageManager packageManager27 = pm2;
                    out2 = out5;
                    th = th;
                    if (out != null) {
                    }
                    this.mOutputFile.close();
                    synchronized (this.mLatch) {
                    }
                    sendEndBackup();
                    obbConnection.tearDown();
                    Slog.d(BackupManagerService.TAG, "Full backup pass complete.");
                    this.backupManagerService.getWakelock().release();
                    throw th;
                }
            }
            z2 = false;
            boolean encrypting2 = z2;
            if (this.backupManagerService.deviceIsEncrypted()) {
            }
            OutputStream finalOutput22 = ofstream2;
            if (this.backupManagerService.backupPasswordMatches(this.mCurrentPassword)) {
            }
        } catch (RemoteException e39) {
            FileOutputStream fileOutputStream28 = ofstream2;
            ArrayList<PackageInfo> arrayList74 = backupQueue2;
            ArrayList<PackageInfo> arrayList75 = keyValueBackupQueue3;
            Iterator<Map.Entry<String, PackageInfo>> it28 = iter;
            z = true;
            PackageManager packageManager28 = pm2;
            Slog.e(BackupManagerService.TAG, "App died during full backup");
            if (out5 != null) {
            }
            this.mOutputFile.close();
            synchronized (this.mLatch) {
            }
            sendEndBackup();
            obbConnection.tearDown();
            Slog.d(BackupManagerService.TAG, "Full backup pass complete.");
            this.backupManagerService.getWakelock().release();
        } catch (Exception e40) {
            e = e40;
            FileOutputStream fileOutputStream29 = ofstream2;
            ArrayList<PackageInfo> arrayList76 = backupQueue2;
            ArrayList<PackageInfo> arrayList77 = keyValueBackupQueue3;
            Iterator<Map.Entry<String, PackageInfo>> it29 = iter;
            z = true;
            PackageManager packageManager29 = pm2;
            Slog.e(BackupManagerService.TAG, "Internal exception during full backup", e);
            if (out5 != null) {
            }
            this.mOutputFile.close();
            synchronized (this.mLatch) {
            }
            sendEndBackup();
            obbConnection.tearDown();
            Slog.d(BackupManagerService.TAG, "Full backup pass complete.");
            this.backupManagerService.getWakelock().release();
        } catch (Throwable th17) {
            th = th17;
            FileOutputStream fileOutputStream30 = ofstream2;
            ArrayList<PackageInfo> arrayList78 = backupQueue2;
            ArrayList<PackageInfo> arrayList79 = keyValueBackupQueue3;
            Iterator<Map.Entry<String, PackageInfo>> it30 = iter;
            boolean z5 = true;
            PackageManager packageManager30 = pm2;
            out = out5;
            th = th;
            if (out != null) {
            }
            this.mOutputFile.close();
            synchronized (this.mLatch) {
            }
            sendEndBackup();
            obbConnection.tearDown();
            Slog.d(BackupManagerService.TAG, "Full backup pass complete.");
            this.backupManagerService.getWakelock().release();
            throw th;
        }
    }

    public void execute() {
    }

    public void operationComplete(long result) {
    }

    public void handleCancel(boolean cancelAll) {
        PackageInfo target = this.mCurrentTarget;
        Slog.w(BackupManagerService.TAG, "adb backup cancel of " + target);
        if (target != null) {
            this.backupManagerService.tearDownAgentAndKill(this.mCurrentTarget.applicationInfo);
        }
        this.backupManagerService.removeOperation(this.mCurrentOpToken);
    }
}
