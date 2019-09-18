package com.android.server.backup.restore;

import android.app.IBackupAgent;
import android.app.backup.IBackupManagerMonitor;
import android.app.backup.IFullBackupRestoreObserver;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManagerInternal;
import android.content.pm.Signature;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.Slog;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.Preconditions;
import com.android.server.LocalServices;
import com.android.server.backup.BackupAgentTimeoutParameters;
import com.android.server.backup.BackupManagerService;
import com.android.server.backup.BackupPasswordManager;
import com.android.server.backup.FileMetadata;
import com.android.server.backup.KeyValueAdbRestoreEngine;
import com.android.server.backup.PackageManagerBackupAgent;
import com.android.server.backup.fullbackup.FullBackupObbConnection;
import com.android.server.backup.utils.BytesReadListener;
import com.android.server.backup.utils.FullBackupRestoreObserverUtils;
import com.android.server.backup.utils.PasswordUtils;
import com.android.server.backup.utils.RestoreUtils;
import com.android.server.backup.utils.TarBackupReader;
import com.android.server.pm.PackageManagerService;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.InflaterInputStream;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class PerformAdbRestoreTask implements Runnable {
    private IBackupAgent mAgent;
    private String mAgentPackage;
    private final BackupAgentTimeoutParameters mAgentTimeoutParameters;
    private long mAppVersion;
    private final BackupManagerService mBackupManagerService;
    private long mBytes;
    private final HashSet<String> mClearedPackages = new HashSet<>();
    private final String mCurrentPassword;
    private final String mDecryptPassword;
    private final RestoreDeleteObserver mDeleteObserver = new RestoreDeleteObserver();
    private final ParcelFileDescriptor mInputFile;
    private final AtomicBoolean mLatchObject;
    private final HashMap<String, Signature[]> mManifestSignatures = new HashMap<>();
    private FullBackupObbConnection mObbConnection = null;
    private IFullBackupRestoreObserver mObserver;
    private final HashMap<String, String> mPackageInstallers = new HashMap<>();
    private final PackageManagerBackupAgent mPackageManagerBackupAgent;
    private final HashMap<String, RestorePolicy> mPackagePolicies = new HashMap<>();
    private ParcelFileDescriptor[] mPipes = null;
    private ApplicationInfo mTargetApp;
    private byte[] mWidgetData = null;

    private static class RestoreFinishedRunnable implements Runnable {
        private final IBackupAgent mAgent;
        private final BackupManagerService mBackupManagerService;
        private final int mToken;

        RestoreFinishedRunnable(IBackupAgent agent, int token, BackupManagerService backupManagerService) {
            this.mAgent = agent;
            this.mToken = token;
            this.mBackupManagerService = backupManagerService;
        }

        public void run() {
            try {
                this.mAgent.doRestoreFinished(this.mToken, this.mBackupManagerService.getBackupManagerBinder());
            } catch (RemoteException e) {
            }
        }
    }

    static /* synthetic */ long access$014(PerformAdbRestoreTask x0, long x1) {
        long j = x0.mBytes + x1;
        x0.mBytes = j;
        return j;
    }

    public PerformAdbRestoreTask(BackupManagerService backupManagerService, ParcelFileDescriptor fd, String curPassword, String decryptPassword, IFullBackupRestoreObserver observer, AtomicBoolean latch) {
        this.mBackupManagerService = backupManagerService;
        this.mInputFile = fd;
        this.mCurrentPassword = curPassword;
        this.mDecryptPassword = decryptPassword;
        this.mObserver = observer;
        this.mLatchObject = latch;
        this.mAgent = null;
        this.mPackageManagerBackupAgent = backupManagerService.makeMetadataAgent();
        this.mAgentPackage = null;
        this.mTargetApp = null;
        this.mObbConnection = new FullBackupObbConnection(backupManagerService);
        this.mAgentTimeoutParameters = (BackupAgentTimeoutParameters) Preconditions.checkNotNull(backupManagerService.getAgentTimeoutParameters(), "Timeout parameters cannot be null");
        this.mClearedPackages.add(PackageManagerService.PLATFORM_PACKAGE_NAME);
        this.mClearedPackages.add(BackupManagerService.SETTINGS_PACKAGE);
    }

    /* JADX WARNING: Removed duplicated region for block: B:104:0x01ae A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:85:0x0160 A[SYNTHETIC] */
    public void run() {
        Slog.i(BackupManagerService.TAG, "--- Performing full-dataset restore ---");
        this.mObbConnection.establish();
        this.mObserver = FullBackupRestoreObserverUtils.sendStartRestore(this.mObserver);
        if (Environment.getExternalStorageState().equals("mounted")) {
            this.mPackagePolicies.put(BackupManagerService.SHARED_BACKUP_AGENT_PACKAGE, RestorePolicy.ACCEPT);
        }
        FileInputStream rawInStream = null;
        try {
            if (!this.mBackupManagerService.backupPasswordMatches(this.mCurrentPassword)) {
                Slog.w(BackupManagerService.TAG, "Backup password mismatch; aborting");
                tearDownPipes();
                tearDownAgent(this.mTargetApp, true);
                if (rawInStream != null) {
                    try {
                        rawInStream.close();
                    } catch (IOException e) {
                        Slog.w(BackupManagerService.TAG, "Close of restore data pipe threw", e);
                    }
                }
                this.mInputFile.close();
                synchronized (this.mLatchObject) {
                    this.mLatchObject.set(true);
                    this.mLatchObject.notifyAll();
                }
                this.mObbConnection.tearDown();
                this.mObserver = FullBackupRestoreObserverUtils.sendEndRestore(this.mObserver);
                Slog.d(BackupManagerService.TAG, "Full restore pass complete.");
                this.mBackupManagerService.getWakelock().release();
                return;
            }
            this.mBytes = 0;
            rawInStream = new FileInputStream(this.mInputFile.getFileDescriptor());
            InputStream tarInputStream = parseBackupFileHeaderAndReturnTarStream(rawInStream, this.mDecryptPassword);
            if (tarInputStream == null) {
                tearDownPipes();
                tearDownAgent(this.mTargetApp, true);
                try {
                    rawInStream.close();
                    this.mInputFile.close();
                } catch (IOException e2) {
                    Slog.w(BackupManagerService.TAG, "Close of restore data pipe threw", e2);
                }
                synchronized (this.mLatchObject) {
                    this.mLatchObject.set(true);
                    this.mLatchObject.notifyAll();
                }
                this.mObbConnection.tearDown();
                this.mObserver = FullBackupRestoreObserverUtils.sendEndRestore(this.mObserver);
                Slog.d(BackupManagerService.TAG, "Full restore pass complete.");
                this.mBackupManagerService.getWakelock().release();
                return;
            }
            do {
            } while (restoreOneFile(tarInputStream, false, new byte[32768], null, true, this.mBackupManagerService.generateRandomIntegerToken(), null));
            tearDownPipes();
            tearDownAgent(this.mTargetApp, true);
            try {
                rawInStream.close();
                this.mInputFile.close();
            } catch (IOException e3) {
                Slog.w(BackupManagerService.TAG, "Close of restore data pipe threw", e3);
            }
            synchronized (this.mLatchObject) {
                this.mLatchObject.set(true);
                this.mLatchObject.notifyAll();
            }
            this.mObbConnection.tearDown();
            this.mObserver = FullBackupRestoreObserverUtils.sendEndRestore(this.mObserver);
            Slog.d(BackupManagerService.TAG, "Full restore pass complete.");
            this.mBackupManagerService.getWakelock().release();
        } catch (IOException e4) {
            try {
                Slog.e(BackupManagerService.TAG, "Unable to read restore input");
                tearDownPipes();
                tearDownAgent(this.mTargetApp, true);
                if (rawInStream != null) {
                    try {
                        rawInStream.close();
                    } catch (IOException e5) {
                        Slog.w(BackupManagerService.TAG, "Close of restore data pipe threw", e5);
                        synchronized (this.mLatchObject) {
                        }
                    }
                }
                this.mInputFile.close();
                synchronized (this.mLatchObject) {
                    this.mLatchObject.set(true);
                    this.mLatchObject.notifyAll();
                }
            } catch (Throwable th) {
                tearDownPipes();
                tearDownAgent(this.mTargetApp, true);
                if (rawInStream != null) {
                    try {
                        rawInStream.close();
                    } catch (IOException e6) {
                        Slog.w(BackupManagerService.TAG, "Close of restore data pipe threw", e6);
                        synchronized (this.mLatchObject) {
                            this.mLatchObject.set(true);
                            this.mLatchObject.notifyAll();
                            this.mObbConnection.tearDown();
                            this.mObserver = FullBackupRestoreObserverUtils.sendEndRestore(this.mObserver);
                            Slog.d(BackupManagerService.TAG, "Full restore pass complete.");
                            this.mBackupManagerService.getWakelock().release();
                            throw th;
                        }
                    }
                }
                this.mInputFile.close();
                synchronized (this.mLatchObject) {
                }
            }
        }
    }

    private static void readFullyOrThrow(InputStream in, byte[] buffer) throws IOException {
        int offset = 0;
        while (offset < buffer.length) {
            int bytesRead = in.read(buffer, offset, buffer.length - offset);
            if (bytesRead > 0) {
                offset += bytesRead;
            } else {
                throw new IOException("Couldn't fully read data");
            }
        }
    }

    @VisibleForTesting
    public static InputStream parseBackupFileHeaderAndReturnTarStream(InputStream rawInputStream, String decryptPassword) throws IOException {
        boolean compressed = false;
        InputStream preCompressStream = rawInputStream;
        boolean okay = false;
        byte[] streamHeader = new byte[BackupManagerService.BACKUP_FILE_HEADER_MAGIC.length()];
        readFullyOrThrow(rawInputStream, streamHeader);
        if (Arrays.equals(BackupManagerService.BACKUP_FILE_HEADER_MAGIC.getBytes("UTF-8"), streamHeader)) {
            int archiveVersion = Integer.parseInt(readHeaderLine(rawInputStream));
            if (archiveVersion <= 5) {
                boolean z = false;
                boolean pbkdf2Fallback = archiveVersion == 1;
                if (Integer.parseInt(readHeaderLine(rawInputStream)) != 0) {
                    z = true;
                }
                compressed = z;
                String s = readHeaderLine(rawInputStream);
                if (s.equals("none")) {
                    okay = true;
                } else if (decryptPassword == null || decryptPassword.length() <= 0) {
                    Slog.w(BackupManagerService.TAG, "Archive is encrypted but no password given");
                } else {
                    preCompressStream = decodeAesHeaderAndInitialize(decryptPassword, s, pbkdf2Fallback, rawInputStream);
                    if (preCompressStream != null) {
                        okay = true;
                    }
                }
            } else {
                Slog.w(BackupManagerService.TAG, "Wrong header version: " + s);
            }
        } else {
            Slog.w(BackupManagerService.TAG, "Didn't read the right header magic");
        }
        if (!okay) {
            Slog.w(BackupManagerService.TAG, "Invalid restore data; aborting.");
            return null;
        }
        return compressed ? new InflaterInputStream(preCompressStream) : preCompressStream;
    }

    private static String readHeaderLine(InputStream in) throws IOException {
        StringBuilder buffer = new StringBuilder(80);
        while (true) {
            int read = in.read();
            int c = read;
            if (read >= 0 && c != 10) {
                buffer.append((char) c);
            }
        }
        return buffer.toString();
    }

    /* JADX WARNING: Removed duplicated region for block: B:47:0x00d4  */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x00e7  */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x00f9  */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x010b  */
    /* JADX WARNING: Removed duplicated region for block: B:67:0x011d  */
    /* JADX WARNING: Removed duplicated region for block: B:72:0x012f  */
    /* JADX WARNING: Removed duplicated region for block: B:73:? A[ORIG_RETURN, RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:78:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:80:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:82:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:84:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:86:? A[RETURN, SYNTHETIC] */
    private static InputStream attemptMasterKeyDecryption(String decryptPassword, String algorithm, byte[] userSalt, byte[] ckSalt, int rounds, String userIvHex, String masterKeyBlobHex, InputStream rawInStream, boolean doLog) {
        InputStream result;
        String str = algorithm;
        int i = rounds;
        try {
            Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
            try {
                c.init(2, new SecretKeySpec(PasswordUtils.buildPasswordKey(str, decryptPassword, userSalt, i).getEncoded(), "AES"), new IvParameterSpec(PasswordUtils.hexToByteArray(userIvHex)));
                byte[] mkBlob = c.doFinal(PasswordUtils.hexToByteArray(masterKeyBlobHex));
                int offset = 0 + 1;
                byte len = mkBlob[0];
                byte[] IV = Arrays.copyOfRange(mkBlob, offset, offset + len);
                int offset2 = offset + len;
                int offset3 = offset2 + 1;
                int len2 = mkBlob[offset2];
                byte[] mk = Arrays.copyOfRange(mkBlob, offset3, offset3 + len2);
                int offset4 = offset3 + len2;
                int offset5 = offset4 + 1;
                byte[] mkChecksum = Arrays.copyOfRange(mkBlob, offset5, offset5 + mkBlob[offset4]);
                result = null;
                try {
                    byte[] calculatedCk = PasswordUtils.makeKeyChecksum(str, mk, ckSalt, i);
                    if (Arrays.equals(calculatedCk, mkChecksum)) {
                        byte[] bArr = calculatedCk;
                        try {
                            c.init(2, new SecretKeySpec(mk, "AES"), new IvParameterSpec(IV));
                            return new CipherInputStream(rawInStream, c);
                        } catch (InvalidAlgorithmParameterException e) {
                            e = e;
                            InputStream inputStream = rawInStream;
                            if (doLog) {
                                return result;
                            }
                            Slog.e(BackupManagerService.TAG, "Needed parameter spec unavailable!", e);
                            return result;
                        } catch (BadPaddingException e2) {
                            InputStream inputStream2 = rawInStream;
                            if (doLog) {
                                return result;
                            }
                            Slog.w(BackupManagerService.TAG, "Incorrect password");
                            return result;
                        } catch (IllegalBlockSizeException e3) {
                            InputStream inputStream3 = rawInStream;
                            if (doLog) {
                                return result;
                            }
                            Slog.w(BackupManagerService.TAG, "Invalid block size in master key");
                            return result;
                        } catch (NoSuchAlgorithmException e4) {
                            InputStream inputStream4 = rawInStream;
                            if (doLog) {
                                return result;
                            }
                            Slog.e(BackupManagerService.TAG, "Needed decryption algorithm unavailable!");
                            return result;
                        } catch (NoSuchPaddingException e5) {
                            InputStream inputStream5 = rawInStream;
                            if (doLog) {
                                return result;
                            }
                            Slog.e(BackupManagerService.TAG, "Needed padding mechanism unavailable!");
                            return result;
                        } catch (InvalidKeyException e6) {
                            InputStream inputStream6 = rawInStream;
                            if (doLog) {
                                return result;
                            }
                            Slog.w(BackupManagerService.TAG, "Illegal password; aborting");
                            return result;
                        }
                    } else {
                        byte[] bArr2 = calculatedCk;
                        if (!doLog) {
                            return null;
                        }
                        Slog.w(BackupManagerService.TAG, "Incorrect password");
                        return null;
                    }
                } catch (InvalidAlgorithmParameterException e7) {
                    e = e7;
                    if (doLog) {
                    }
                } catch (BadPaddingException e8) {
                    if (doLog) {
                    }
                } catch (IllegalBlockSizeException e9) {
                    if (doLog) {
                    }
                } catch (NoSuchAlgorithmException e10) {
                    if (doLog) {
                    }
                } catch (NoSuchPaddingException e11) {
                    if (doLog) {
                    }
                } catch (InvalidKeyException e12) {
                    if (doLog) {
                    }
                }
            } catch (InvalidAlgorithmParameterException e13) {
                e = e13;
                result = null;
                byte[] bArr3 = ckSalt;
                if (doLog) {
                }
            } catch (BadPaddingException e14) {
                result = null;
                byte[] bArr4 = ckSalt;
                if (doLog) {
                }
            } catch (IllegalBlockSizeException e15) {
                result = null;
                byte[] bArr5 = ckSalt;
                if (doLog) {
                }
            } catch (NoSuchAlgorithmException e16) {
                result = null;
                byte[] bArr6 = ckSalt;
                if (doLog) {
                }
            } catch (NoSuchPaddingException e17) {
                result = null;
                byte[] bArr7 = ckSalt;
                if (doLog) {
                }
            } catch (InvalidKeyException e18) {
                result = null;
                byte[] bArr8 = ckSalt;
                if (doLog) {
                }
            }
        } catch (InvalidAlgorithmParameterException e19) {
            e = e19;
            String str2 = decryptPassword;
            byte[] bArr9 = userSalt;
            result = null;
            byte[] bArr32 = ckSalt;
            if (doLog) {
            }
        } catch (BadPaddingException e20) {
            String str3 = decryptPassword;
            byte[] bArr10 = userSalt;
            result = null;
            byte[] bArr42 = ckSalt;
            if (doLog) {
            }
        } catch (IllegalBlockSizeException e21) {
            String str4 = decryptPassword;
            byte[] bArr11 = userSalt;
            result = null;
            byte[] bArr52 = ckSalt;
            if (doLog) {
            }
        } catch (NoSuchAlgorithmException e22) {
            String str5 = decryptPassword;
            byte[] bArr12 = userSalt;
            result = null;
            byte[] bArr62 = ckSalt;
            if (doLog) {
            }
        } catch (NoSuchPaddingException e23) {
            String str6 = decryptPassword;
            byte[] bArr13 = userSalt;
            result = null;
            byte[] bArr72 = ckSalt;
            if (doLog) {
            }
        } catch (InvalidKeyException e24) {
            String str7 = decryptPassword;
            byte[] bArr14 = userSalt;
            result = null;
            byte[] bArr82 = ckSalt;
            if (doLog) {
            }
        }
    }

    private static InputStream decodeAesHeaderAndInitialize(String decryptPassword, String encryptionName, boolean pbkdf2Fallback, InputStream rawInStream) {
        InputStream result = null;
        try {
            if (encryptionName.equals(PasswordUtils.ENCRYPTION_ALGORITHM_NAME)) {
                byte[] userSalt = PasswordUtils.hexToByteArray(readHeaderLine(rawInStream));
                byte[] ckSalt = PasswordUtils.hexToByteArray(readHeaderLine(rawInStream));
                int rounds = Integer.parseInt(readHeaderLine(rawInStream));
                String userIvHex = readHeaderLine(rawInStream);
                String masterKeyBlobHex = readHeaderLine(rawInStream);
                result = attemptMasterKeyDecryption(decryptPassword, BackupPasswordManager.PBKDF_CURRENT, userSalt, ckSalt, rounds, userIvHex, masterKeyBlobHex, rawInStream, false);
                if (result == null && pbkdf2Fallback) {
                    result = attemptMasterKeyDecryption(decryptPassword, BackupPasswordManager.PBKDF_FALLBACK, userSalt, ckSalt, rounds, userIvHex, masterKeyBlobHex, rawInStream, true);
                }
            } else {
                Slog.w(BackupManagerService.TAG, "Unsupported encryption method: " + r1);
            }
        } catch (NumberFormatException e) {
            Slog.w(BackupManagerService.TAG, "Can't parse restore data header");
        } catch (IOException e2) {
            Slog.w(BackupManagerService.TAG, "Can't read input header");
        }
        return result;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* JADX WARNING: Removed duplicated region for block: B:121:0x03e8 A[Catch:{ IOException -> 0x04ae }] */
    /* JADX WARNING: Removed duplicated region for block: B:160:0x046b A[Catch:{ IOException -> 0x04f8 }] */
    /* JADX WARNING: Removed duplicated region for block: B:162:0x0475 A[Catch:{ IOException -> 0x04f8 }] */
    /* JADX WARNING: Removed duplicated region for block: B:166:0x04b4 A[Catch:{ IOException -> 0x04f8 }] */
    /* JADX WARNING: Removed duplicated region for block: B:168:0x04bb A[Catch:{ IOException -> 0x04f8 }] */
    /* JADX WARNING: Removed duplicated region for block: B:199:0x0523  */
    /* JADX WARNING: Removed duplicated region for block: B:200:0x0526  */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x017d A[Catch:{ IOException -> 0x04fd }] */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x01bd A[Catch:{ NameNotFoundException | IOException -> 0x021b }] */
    /* JADX WARNING: Removed duplicated region for block: B:71:0x01f5 A[Catch:{ NameNotFoundException | IOException -> 0x021b }] */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x020e A[Catch:{ NameNotFoundException | IOException -> 0x021b }] */
    /* JADX WARNING: Removed duplicated region for block: B:75:0x0211 A[Catch:{ NameNotFoundException | IOException -> 0x021b }] */
    /* JADX WARNING: Removed duplicated region for block: B:81:0x0223 A[Catch:{ IOException -> 0x04fd }] */
    /* JADX WARNING: Removed duplicated region for block: B:88:0x0274 A[Catch:{ IOException -> 0x04fd }] */
    public boolean restoreOneFile(InputStream instream, boolean mustKillAgent, byte[] buffer, PackageInfo onlyPackage, boolean allowApks, int token, IBackupManagerMonitor monitor) {
        FileMetadata info;
        FileMetadata info2;
        boolean z;
        boolean okay;
        boolean okay2;
        boolean okay3;
        byte[] bArr;
        long nRead;
        boolean okay4;
        boolean agentSuccess;
        boolean okay5;
        boolean agentSuccess2;
        boolean okay6;
        boolean okay7;
        int i;
        RestorePolicy restorePolicy;
        InputStream inputStream = instream;
        byte[] bArr2 = buffer;
        BytesReadListener bytesReadListener = new BytesReadListener() {
            public void onBytesRead(long bytesRead) {
                PerformAdbRestoreTask.access$014(PerformAdbRestoreTask.this, bytesRead);
            }
        };
        IBackupManagerMonitor iBackupManagerMonitor = monitor;
        TarBackupReader tarBackupReader = new TarBackupReader(inputStream, bytesReadListener, iBackupManagerMonitor);
        try {
            FileMetadata info3 = tarBackupReader.readTarHeaders();
            if (info3 != null) {
                String pkg = info3.packageName;
                if (!pkg.equals(this.mAgentPackage)) {
                    try {
                        if (!this.mPackagePolicies.containsKey(pkg)) {
                            this.mPackagePolicies.put(pkg, RestorePolicy.IGNORE);
                        }
                        if (this.mAgent != null) {
                            Slog.d(BackupManagerService.TAG, "Saw new package; finalizing old one");
                            tearDownPipes();
                            tearDownAgent(this.mTargetApp, true);
                            this.mTargetApp = null;
                            this.mAgentPackage = null;
                        }
                    } catch (IOException e) {
                        e = e;
                        int i2 = token;
                        InputStream inputStream2 = inputStream;
                        byte[] bArr3 = bArr2;
                        IBackupManagerMonitor iBackupManagerMonitor2 = iBackupManagerMonitor;
                        Slog.w(BackupManagerService.TAG, "io exception on restore socket read", e);
                        info = null;
                        return info != null;
                    }
                }
                if (info3.path.equals(BackupManagerService.BACKUP_MANIFEST_FILENAME)) {
                    Signature[] signatures = tarBackupReader.readAppManifestAndReturnSignatures(info3);
                    this.mAppVersion = info3.version;
                    String pkg2 = pkg;
                    info2 = info3;
                    RestorePolicy restorePolicy2 = tarBackupReader.chooseRestorePolicy(this.mBackupManagerService.getPackageManager(), allowApks, info3, signatures, (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class));
                    this.mManifestSignatures.put(info2.packageName, signatures);
                    this.mPackagePolicies.put(pkg2, restorePolicy2);
                    this.mPackageInstallers.put(pkg2, info2.installerPackageName);
                    tarBackupReader.skipTarPadding(info2.size);
                    this.mObserver = FullBackupRestoreObserverUtils.sendOnRestorePackage(this.mObserver, pkg2);
                    int i3 = token;
                    InputStream inputStream3 = inputStream;
                    byte[] bArr4 = bArr2;
                } else {
                    String pkg3 = pkg;
                    info2 = info3;
                    if (info2.path.equals(BackupManagerService.BACKUP_METADATA_FILENAME)) {
                        tarBackupReader.readMetadata(info2);
                        this.mWidgetData = tarBackupReader.getWidgetData();
                        IBackupManagerMonitor monitor2 = tarBackupReader.getMonitor();
                        try {
                            tarBackupReader.skipTarPadding(info2.size);
                            int i4 = token;
                            IBackupManagerMonitor iBackupManagerMonitor3 = monitor2;
                            InputStream inputStream4 = inputStream;
                            byte[] bArr5 = bArr2;
                            info = info2;
                        } catch (IOException e2) {
                            e = e2;
                            int i5 = token;
                            IBackupManagerMonitor iBackupManagerMonitor4 = monitor2;
                            InputStream inputStream5 = inputStream;
                            byte[] bArr6 = bArr2;
                            Slog.w(BackupManagerService.TAG, "io exception on restore socket read", e);
                            info = null;
                            return info != null;
                        }
                        return info != null;
                    }
                    boolean okay8 = true;
                    RestorePolicy policy = this.mPackagePolicies.get(pkg3);
                    switch (policy) {
                        case IGNORE:
                            z = true;
                            okay8 = false;
                        case ACCEPT_IF_APK:
                            try {
                                if (info2.domain.equals("a")) {
                                    Slog.d(BackupManagerService.TAG, "APK file; installing");
                                    RestorePolicy restorePolicy3 = policy;
                                    boolean isSuccessfullyInstalled = RestoreUtils.installApk(inputStream, this.mBackupManagerService.getContext(), this.mDeleteObserver, this.mManifestSignatures, this.mPackagePolicies, info2, this.mPackageInstallers.get(pkg3), bytesReadListener);
                                    HashMap<String, RestorePolicy> hashMap = this.mPackagePolicies;
                                    if (isSuccessfullyInstalled) {
                                        restorePolicy = RestorePolicy.ACCEPT;
                                    } else {
                                        restorePolicy = RestorePolicy.IGNORE;
                                    }
                                    hashMap.put(pkg3, restorePolicy);
                                    tarBackupReader.skipTarPadding(info2.size);
                                    return true;
                                }
                                z = true;
                                this.mPackagePolicies.put(pkg3, RestorePolicy.IGNORE);
                                okay8 = false;
                            } catch (IOException e3) {
                                e = e3;
                                int i6 = token;
                                byte[] bArr7 = bArr2;
                                InputStream inputStream6 = instream;
                                Slog.w(BackupManagerService.TAG, "io exception on restore socket read", e);
                                info = null;
                                return info != null;
                            }
                            break;
                        case ACCEPT:
                            if (info2.domain.equals("a")) {
                                Slog.d(BackupManagerService.TAG, "apk present but ACCEPT");
                                okay8 = false;
                            }
                            z = true;
                            if (!isCanonicalFilePath(info2.path)) {
                                okay8 = false;
                            }
                            okay = okay8;
                            if (okay && this.mAgent != null) {
                                Slog.i(BackupManagerService.TAG, "Reusing existing agent instance");
                            }
                            if (okay && this.mAgent == null) {
                                Slog.d(BackupManagerService.TAG, "Need to launch agent for " + pkg3);
                                this.mTargetApp = this.mBackupManagerService.getPackageManager().getApplicationInfo(pkg3, 0);
                                if (this.mClearedPackages.contains(pkg3)) {
                                    if (this.mTargetApp.backupAgentName == null) {
                                        Slog.d(BackupManagerService.TAG, "Clearing app data preparatory to full restore");
                                        this.mBackupManagerService.clearApplicationDataSynchronous(pkg3, z);
                                    } else {
                                        Slog.d(BackupManagerService.TAG, "backup agent (" + this.mTargetApp.backupAgentName + ") => no clear");
                                    }
                                    this.mClearedPackages.add(pkg3);
                                } else {
                                    Slog.d(BackupManagerService.TAG, "We've initialized this app already; no clear required");
                                }
                                setUpPipes();
                                BackupManagerService backupManagerService = this.mBackupManagerService;
                                ApplicationInfo applicationInfo = this.mTargetApp;
                                if (!"k".equals(info2.domain)) {
                                    i = 0;
                                } else {
                                    i = 3;
                                }
                                this.mAgent = backupManagerService.bindToAgentSynchronous(applicationInfo, i);
                                this.mAgentPackage = pkg3;
                                if (this.mAgent == null) {
                                    Slog.e(BackupManagerService.TAG, "Unable to create agent for " + pkg3);
                                    okay = false;
                                    tearDownPipes();
                                    this.mPackagePolicies.put(pkg3, RestorePolicy.IGNORE);
                                }
                            }
                            if (okay && !pkg3.equals(this.mAgentPackage)) {
                                Slog.e(BackupManagerService.TAG, "Restoring data for " + pkg3 + " but agent is for " + this.mAgentPackage);
                                okay = false;
                            }
                            okay2 = okay;
                            if (!okay2) {
                                boolean agentSuccess3 = true;
                                long toCopy = info2.size;
                                try {
                                    this.mBackupManagerService.prepareOperationTimeout(token, this.mAgentTimeoutParameters.getRestoreAgentTimeoutMillis(), null, 1);
                                    if ("obb".equals(info2.domain)) {
                                        Slog.d(BackupManagerService.TAG, "Restoring OBB file for " + pkg3 + " : " + info2.path);
                                        okay7 = okay2;
                                        try {
                                            this.mObbConnection.restoreObbFile(pkg3, this.mPipes[0], info2.size, info2.type, info2.path, info2.mode, info2.mtime, token, this.mBackupManagerService.getBackupManagerBinder());
                                        } catch (IOException e4) {
                                            try {
                                                Slog.d(BackupManagerService.TAG, "Couldn't establish restore");
                                                agentSuccess3 = false;
                                                okay6 = false;
                                                okay4 = okay6;
                                                if (okay4) {
                                                }
                                                if (!agentSuccess) {
                                                }
                                                okay3 = okay5;
                                                if (!okay3) {
                                                }
                                                InputStream inputStream7 = instream;
                                                IBackupManagerMonitor iBackupManagerMonitor5 = monitor;
                                                info = info2;
                                            } catch (IOException e5) {
                                                e = e5;
                                                int i7 = token;
                                                byte[] bArr8 = buffer;
                                                InputStream inputStream62 = instream;
                                                Slog.w(BackupManagerService.TAG, "io exception on restore socket read", e);
                                                info = null;
                                                return info != null;
                                            }
                                            return info != null;
                                        } catch (RemoteException e6) {
                                            try {
                                                Slog.e(BackupManagerService.TAG, "Agent crashed during full restore");
                                                agentSuccess3 = false;
                                                okay6 = false;
                                                okay4 = okay6;
                                                if (okay4) {
                                                }
                                                if (!agentSuccess) {
                                                }
                                                okay3 = okay5;
                                                if (!okay3) {
                                                }
                                                InputStream inputStream72 = instream;
                                                IBackupManagerMonitor iBackupManagerMonitor52 = monitor;
                                                info = info2;
                                            } catch (IOException e7) {
                                                e = e7;
                                                int i8 = token;
                                                InputStream inputStream8 = instream;
                                                byte[] bArr9 = buffer;
                                                Slog.w(BackupManagerService.TAG, "io exception on restore socket read", e);
                                                info = null;
                                                return info != null;
                                            }
                                            return info != null;
                                        }
                                    } else {
                                        okay7 = okay2;
                                        if ("k".equals(info2.domain)) {
                                            Slog.d(BackupManagerService.TAG, "Restoring key-value file for " + pkg3 + " : " + info2.path);
                                            info2.version = this.mAppVersion;
                                            KeyValueAdbRestoreEngine keyValueAdbRestoreEngine = new KeyValueAdbRestoreEngine(this.mBackupManagerService, this.mBackupManagerService.getDataDir(), info2, this.mPipes[0], this.mAgent, token);
                                            new Thread(keyValueAdbRestoreEngine, "restore-key-value-runner").start();
                                        } else {
                                            Slog.d(BackupManagerService.TAG, "Invoking agent to restore file " + info2.path);
                                            if (this.mTargetApp.processName.equals("system")) {
                                                Slog.d(BackupManagerService.TAG, "system process agent - spinning a thread");
                                                RestoreFileRunnable restoreFileRunnable = new RestoreFileRunnable(this.mBackupManagerService, this.mAgent, info2, this.mPipes[0], token);
                                                new Thread(restoreFileRunnable, "restore-sys-runner").start();
                                            } else {
                                                this.mAgent.doRestoreFile(this.mPipes[0], info2.size, info2.type, info2.domain, info2.path, info2.mode, info2.mtime, token, this.mBackupManagerService.getBackupManagerBinder());
                                            }
                                        }
                                    }
                                    okay4 = okay7;
                                } catch (IOException e8) {
                                    boolean z2 = okay2;
                                    Slog.d(BackupManagerService.TAG, "Couldn't establish restore");
                                    agentSuccess3 = false;
                                    okay6 = false;
                                    okay4 = okay6;
                                    if (okay4) {
                                    }
                                    if (!agentSuccess) {
                                    }
                                    okay3 = okay5;
                                    if (!okay3) {
                                    }
                                    InputStream inputStream722 = instream;
                                    IBackupManagerMonitor iBackupManagerMonitor522 = monitor;
                                    info = info2;
                                    return info != null;
                                } catch (RemoteException e9) {
                                    boolean z3 = okay2;
                                    Slog.e(BackupManagerService.TAG, "Agent crashed during full restore");
                                    agentSuccess3 = false;
                                    okay6 = false;
                                    okay4 = okay6;
                                    if (okay4) {
                                    }
                                    if (!agentSuccess) {
                                    }
                                    okay3 = okay5;
                                    if (!okay3) {
                                    }
                                    InputStream inputStream7222 = instream;
                                    IBackupManagerMonitor iBackupManagerMonitor5222 = monitor;
                                    info = info2;
                                    return info != null;
                                }
                                if (okay4) {
                                    FileOutputStream pipe = new FileOutputStream(this.mPipes[1].getFileDescriptor());
                                    boolean pipeOkay = true;
                                    long toCopy2 = toCopy;
                                    while (true) {
                                        if (toCopy2 > 0) {
                                            bArr = buffer;
                                            try {
                                                int toRead = toCopy2 > ((long) bArr.length) ? bArr.length : (int) toCopy2;
                                                InputStream inputStream9 = instream;
                                                try {
                                                    int nRead2 = inputStream9.read(bArr, 0, toRead);
                                                    if (nRead2 >= 0) {
                                                        okay5 = okay4;
                                                        agentSuccess2 = agentSuccess3;
                                                        int i9 = toRead;
                                                        this.mBytes += (long) nRead2;
                                                    } else {
                                                        okay5 = okay4;
                                                        agentSuccess2 = agentSuccess3;
                                                        int i10 = toRead;
                                                    }
                                                    if (nRead2 > 0) {
                                                        toCopy2 -= (long) nRead2;
                                                        if (pipeOkay) {
                                                            try {
                                                                pipe.write(bArr, 0, nRead2);
                                                            } catch (IOException e10) {
                                                                IOException iOException = e10;
                                                                Slog.e(BackupManagerService.TAG, "Failed to write to restore pipe", e10);
                                                                pipeOkay = false;
                                                            }
                                                        }
                                                        okay4 = okay5;
                                                        agentSuccess3 = agentSuccess2;
                                                    }
                                                } catch (IOException e11) {
                                                    e = e11;
                                                    int i11 = token;
                                                    InputStream inputStream10 = inputStream9;
                                                    Slog.w(BackupManagerService.TAG, "io exception on restore socket read", e);
                                                    info = null;
                                                    return info != null;
                                                }
                                            } catch (IOException e12) {
                                                e = e12;
                                                int i12 = token;
                                            }
                                        } else {
                                            okay5 = okay4;
                                            boolean z4 = agentSuccess3;
                                            bArr = buffer;
                                        }
                                    }
                                    tarBackupReader.skipTarPadding(info2.size);
                                    try {
                                        long j = toCopy2;
                                        agentSuccess = this.mBackupManagerService.waitUntilOperationComplete(token);
                                    } catch (IOException e13) {
                                        e = e13;
                                        InputStream inputStream622 = instream;
                                        Slog.w(BackupManagerService.TAG, "io exception on restore socket read", e);
                                        info = null;
                                        return info != null;
                                    }
                                } else {
                                    int i13 = token;
                                    okay5 = okay4;
                                    agentSuccess = agentSuccess3;
                                    bArr = buffer;
                                }
                                if (!agentSuccess) {
                                    Slog.d(BackupManagerService.TAG, "Agent failure restoring " + pkg3 + "; now ignoring");
                                    this.mBackupManagerService.getBackupHandler().removeMessages(18);
                                    tearDownPipes();
                                    tearDownAgent(this.mTargetApp, false);
                                    this.mPackagePolicies.put(pkg3, RestorePolicy.IGNORE);
                                }
                                okay3 = okay5;
                            } else {
                                int i14 = token;
                                okay3 = okay2;
                                bArr = bArr2;
                            }
                            if (!okay3) {
                                Slog.d(BackupManagerService.TAG, "[discarding file content]");
                                long bytesToConsume = (info2.size + 511) & -512;
                                while (true) {
                                    if (bytesToConsume > 0) {
                                        try {
                                            long nRead3 = (long) instream.read(bArr, 0, bytesToConsume > ((long) bArr.length) ? bArr.length : (int) bytesToConsume);
                                            if (nRead3 >= 0) {
                                                nRead = nRead3;
                                                this.mBytes += nRead;
                                            } else {
                                                nRead = nRead3;
                                            }
                                            if (nRead > 0) {
                                                bytesToConsume -= nRead;
                                            }
                                        } catch (IOException e14) {
                                            e = e14;
                                            Slog.w(BackupManagerService.TAG, "io exception on restore socket read", e);
                                            info = null;
                                            return info != null;
                                        }
                                    }
                                }
                            }
                            InputStream inputStream72222 = instream;
                            break;
                        default:
                            RestorePolicy restorePolicy4 = policy;
                            z = true;
                            Slog.e(BackupManagerService.TAG, "Invalid policy from manifest");
                            okay8 = false;
                            this.mPackagePolicies.put(pkg3, RestorePolicy.IGNORE);
                    }
                    if (!isCanonicalFilePath(info2.path)) {
                    }
                    okay = okay8;
                    Slog.i(BackupManagerService.TAG, "Reusing existing agent instance");
                    Slog.d(BackupManagerService.TAG, "Need to launch agent for " + pkg3);
                    try {
                        this.mTargetApp = this.mBackupManagerService.getPackageManager().getApplicationInfo(pkg3, 0);
                        if (this.mClearedPackages.contains(pkg3)) {
                        }
                        setUpPipes();
                        BackupManagerService backupManagerService2 = this.mBackupManagerService;
                        ApplicationInfo applicationInfo2 = this.mTargetApp;
                        if (!"k".equals(info2.domain)) {
                        }
                        this.mAgent = backupManagerService2.bindToAgentSynchronous(applicationInfo2, i);
                        this.mAgentPackage = pkg3;
                    } catch (PackageManager.NameNotFoundException | IOException e15) {
                    }
                    if (this.mAgent == null) {
                    }
                    Slog.e(BackupManagerService.TAG, "Restoring data for " + pkg3 + " but agent is for " + this.mAgentPackage);
                    okay = false;
                    okay2 = okay;
                    if (!okay2) {
                    }
                    if (!okay3) {
                    }
                    InputStream inputStream722222 = instream;
                }
            } else {
                int i15 = token;
                info2 = info3;
                InputStream inputStream11 = inputStream;
                byte[] bArr10 = bArr2;
            }
            IBackupManagerMonitor iBackupManagerMonitor52222 = monitor;
            info = info2;
        } catch (IOException e16) {
            e = e16;
            int i16 = token;
            InputStream inputStream12 = inputStream;
            byte[] bArr11 = bArr2;
            Slog.w(BackupManagerService.TAG, "io exception on restore socket read", e);
            info = null;
            return info != null;
        }
        return info != null;
    }

    private static boolean isCanonicalFilePath(String path) {
        if (path.contains("..") || path.contains("//")) {
            return false;
        }
        return true;
    }

    private void setUpPipes() throws IOException {
        this.mPipes = ParcelFileDescriptor.createPipe();
    }

    private void tearDownPipes() {
        if (this.mPipes != null) {
            try {
                this.mPipes[0].close();
                this.mPipes[0] = null;
                this.mPipes[1].close();
                this.mPipes[1] = null;
            } catch (IOException e) {
                Slog.w(BackupManagerService.TAG, "Couldn't close agent pipes", e);
            }
            this.mPipes = null;
        }
    }

    private void tearDownAgent(ApplicationInfo app, boolean doRestoreFinished) {
        if (this.mAgent != null) {
            if (doRestoreFinished) {
                try {
                    int token = this.mBackupManagerService.generateRandomIntegerToken();
                    long fullBackupAgentTimeoutMillis = this.mAgentTimeoutParameters.getFullBackupAgentTimeoutMillis();
                    AdbRestoreFinishedLatch latch = new AdbRestoreFinishedLatch(this.mBackupManagerService, token);
                    this.mBackupManagerService.prepareOperationTimeout(token, fullBackupAgentTimeoutMillis, latch, 1);
                    if (this.mTargetApp.processName.equals("system")) {
                        new Thread(new RestoreFinishedRunnable(this.mAgent, token, this.mBackupManagerService), "restore-sys-finished-runner").start();
                    } else {
                        this.mAgent.doRestoreFinished(token, this.mBackupManagerService.getBackupManagerBinder());
                    }
                    latch.await();
                } catch (RemoteException e) {
                    Slog.d(BackupManagerService.TAG, "Lost app trying to shut down");
                }
            }
            this.mBackupManagerService.tearDownAgentAndKill(app);
            this.mAgent = null;
        }
    }
}
