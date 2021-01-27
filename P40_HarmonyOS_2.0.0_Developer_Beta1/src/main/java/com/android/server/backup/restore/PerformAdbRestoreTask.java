package com.android.server.backup.restore;

import android.app.IBackupAgent;
import android.app.backup.BackupAgent;
import android.app.backup.IFullBackupRestoreObserver;
import android.content.pm.ApplicationInfo;
import android.content.pm.Signature;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.Slog;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.Preconditions;
import com.android.server.backup.BackupAgentTimeoutParameters;
import com.android.server.backup.BackupManagerService;
import com.android.server.backup.BackupPasswordManager;
import com.android.server.backup.UserBackupManagerService;
import com.android.server.backup.fullbackup.FullBackupObbConnection;
import com.android.server.backup.utils.FullBackupRestoreObserverUtils;
import com.android.server.backup.utils.PasswordUtils;
import com.android.server.pm.PackageManagerService;
import java.io.FileInputStream;
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
    private final UserBackupManagerService mBackupManagerService;
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
    private final BackupAgent mPackageManagerBackupAgent;
    private final HashMap<String, RestorePolicy> mPackagePolicies = new HashMap<>();
    private ParcelFileDescriptor[] mPipes = null;
    private ApplicationInfo mTargetApp;
    private byte[] mWidgetData = null;

    public PerformAdbRestoreTask(UserBackupManagerService backupManagerService, ParcelFileDescriptor fd, String curPassword, String decryptPassword, IFullBackupRestoreObserver observer, AtomicBoolean latch) {
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
        this.mClearedPackages.add(UserBackupManagerService.SETTINGS_PACKAGE);
    }

    /* JADX WARNING: Removed duplicated region for block: B:114:0x01de A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:96:0x0197 A[SYNTHETIC] */
    @Override // java.lang.Runnable
    public void run() {
        Slog.i(BackupManagerService.TAG, "--- Performing full-dataset restore ---");
        this.mObbConnection.establish();
        this.mObserver = FullBackupRestoreObserverUtils.sendStartRestore(this.mObserver);
        if (Environment.getExternalStorageState().equals("mounted")) {
            this.mPackagePolicies.put(UserBackupManagerService.SHARED_BACKUP_AGENT_PACKAGE, RestorePolicy.ACCEPT);
        }
        FileInputStream rawInStream = null;
        try {
            if (!this.mBackupManagerService.backupPasswordMatches(this.mCurrentPassword)) {
                Slog.w(BackupManagerService.TAG, "Backup password mismatch; aborting");
                if (0 != 0) {
                    try {
                        rawInStream.close();
                    } catch (IOException e) {
                        Slog.w(BackupManagerService.TAG, "Close of restore data pipe threw.");
                    }
                }
                if (this.mInputFile != null) {
                    this.mInputFile.close();
                }
                synchronized (this.mLatchObject) {
                    this.mLatchObject.set(true);
                    this.mLatchObject.notifyAll();
                }
                this.mObbConnection.tearDown();
                this.mObserver = FullBackupRestoreObserverUtils.sendEndRestore(this.mObserver);
                Slog.d(BackupManagerService.TAG, "Full restore pass complete.");
                this.mBackupManagerService.getWakelock().release();
            } else if (this.mInputFile == null) {
                Slog.w(BackupManagerService.TAG, "mInputFile is null");
                if (0 != 0) {
                    try {
                        rawInStream.close();
                    } catch (IOException e2) {
                        Slog.w(BackupManagerService.TAG, "Close of restore data pipe threw.");
                    }
                }
                if (this.mInputFile != null) {
                    this.mInputFile.close();
                }
                synchronized (this.mLatchObject) {
                    this.mLatchObject.set(true);
                    this.mLatchObject.notifyAll();
                }
                this.mObbConnection.tearDown();
                this.mObserver = FullBackupRestoreObserverUtils.sendEndRestore(this.mObserver);
                Slog.d(BackupManagerService.TAG, "Full restore pass complete.");
                this.mBackupManagerService.getWakelock().release();
            } else {
                this.mBytes = 0;
                FileInputStream rawInStream2 = new FileInputStream(this.mInputFile.getFileDescriptor());
                InputStream tarInputStream = parseBackupFileHeaderAndReturnTarStream(rawInStream2, this.mDecryptPassword);
                if (tarInputStream == null) {
                    try {
                        rawInStream2.close();
                        if (this.mInputFile != null) {
                            this.mInputFile.close();
                        }
                    } catch (IOException e3) {
                        Slog.w(BackupManagerService.TAG, "Close of restore data pipe threw.");
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
                new FullRestoreEngineThread(new FullRestoreEngine(this.mBackupManagerService, null, this.mObserver, null, null, true, true, 0, true), tarInputStream).run();
                try {
                    rawInStream2.close();
                    if (this.mInputFile != null) {
                        this.mInputFile.close();
                    }
                } catch (IOException e4) {
                    Slog.w(BackupManagerService.TAG, "Close of restore data pipe threw.");
                }
                synchronized (this.mLatchObject) {
                    this.mLatchObject.set(true);
                    this.mLatchObject.notifyAll();
                }
                this.mObbConnection.tearDown();
                this.mObserver = FullBackupRestoreObserverUtils.sendEndRestore(this.mObserver);
                Slog.d(BackupManagerService.TAG, "Full restore pass complete.");
                this.mBackupManagerService.getWakelock().release();
            }
        } catch (IOException e5) {
            try {
                Slog.e(BackupManagerService.TAG, "Unable to read restore input");
                if (0 != 0) {
                    try {
                        rawInStream.close();
                    } catch (IOException e6) {
                        Slog.w(BackupManagerService.TAG, "Close of restore data pipe threw.");
                        synchronized (this.mLatchObject) {
                            this.mLatchObject.set(true);
                            this.mLatchObject.notifyAll();
                            this.mObbConnection.tearDown();
                            this.mObserver = FullBackupRestoreObserverUtils.sendEndRestore(this.mObserver);
                            Slog.d(BackupManagerService.TAG, "Full restore pass complete.");
                            this.mBackupManagerService.getWakelock().release();
                        }
                    }
                }
                if (this.mInputFile != null) {
                    this.mInputFile.close();
                }
                synchronized (this.mLatchObject) {
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    try {
                        rawInStream.close();
                    } catch (IOException e7) {
                        Slog.w(BackupManagerService.TAG, "Close of restore data pipe threw.");
                        synchronized (this.mLatchObject) {
                        }
                    }
                }
                if (this.mInputFile != null) {
                    this.mInputFile.close();
                }
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
        byte[] streamHeader = new byte[UserBackupManagerService.BACKUP_FILE_HEADER_MAGIC.length()];
        readFullyOrThrow(rawInputStream, streamHeader);
        if (Arrays.equals(UserBackupManagerService.BACKUP_FILE_HEADER_MAGIC.getBytes("UTF-8"), streamHeader)) {
            String s = readHeaderLine(rawInputStream);
            int archiveVersion = Integer.parseInt(s);
            if (archiveVersion <= 5) {
                boolean z = false;
                boolean pbkdf2Fallback = archiveVersion == 1;
                if (Integer.parseInt(readHeaderLine(rawInputStream)) != 0) {
                    z = true;
                }
                compressed = z;
                String s2 = readHeaderLine(rawInputStream);
                if (s2.equals("none")) {
                    okay = true;
                } else if (decryptPassword == null || decryptPassword.length() <= 0) {
                    Slog.w(BackupManagerService.TAG, "Archive is encrypted but no password given");
                } else {
                    preCompressStream = decodeAesHeaderAndInitialize(decryptPassword, s2, pbkdf2Fallback, rawInputStream);
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
        if (okay) {
            return compressed ? new InflaterInputStream(preCompressStream) : preCompressStream;
        }
        Slog.w(BackupManagerService.TAG, "Invalid restore data; aborting.");
        return null;
    }

    private static String readHeaderLine(InputStream in) throws IOException {
        StringBuilder buffer = new StringBuilder(80);
        while (true) {
            int c = in.read();
            if (c < 0 || c == 10) {
                break;
            }
            buffer.append((char) c);
        }
        return buffer.toString();
    }

    /* JADX INFO: Multiple debug info for r15v1 byte: [D('offset' int), D('len' int)] */
    /* JADX INFO: Multiple debug info for r13v4 byte: [D('offset' int), D('len' int)] */
    /* JADX INFO: Multiple debug info for r10v3 byte: [D('offset' int), D('len' int)] */
    /* JADX WARNING: Removed duplicated region for block: B:100:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x00f0  */
    /* JADX WARNING: Removed duplicated region for block: B:67:0x00ff  */
    /* JADX WARNING: Removed duplicated region for block: B:72:0x010e  */
    /* JADX WARNING: Removed duplicated region for block: B:77:0x011d  */
    /* JADX WARNING: Removed duplicated region for block: B:82:0x012c  */
    /* JADX WARNING: Removed duplicated region for block: B:87:0x0139  */
    /* JADX WARNING: Removed duplicated region for block: B:89:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:92:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:94:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:96:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:98:? A[RETURN, SYNTHETIC] */
    private static InputStream attemptMasterKeyDecryption(String decryptPassword, String algorithm, byte[] userSalt, byte[] ckSalt, int rounds, String userIvHex, String masterKeyBlobHex, InputStream rawInStream, boolean doLog) {
        InputStream result;
        InvalidAlgorithmParameterException e;
        byte[] mkBlob;
        int offset;
        byte b;
        InputStream result2;
        try {
            Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
            try {
                c.init(2, new SecretKeySpec(PasswordUtils.buildPasswordKey(algorithm, decryptPassword, userSalt, rounds).getEncoded(), "AES"), new IvParameterSpec(PasswordUtils.hexToByteArray(userIvHex)));
                mkBlob = c.doFinal(PasswordUtils.hexToByteArray(masterKeyBlobHex));
                offset = 0 + 1;
                b = mkBlob[0];
                result = null;
            } catch (InvalidAlgorithmParameterException e2) {
                e = e2;
                result = null;
                if (!doLog) {
                }
            } catch (BadPaddingException e3) {
                result = null;
                if (!doLog) {
                }
            } catch (IllegalBlockSizeException e4) {
                result = null;
                if (!doLog) {
                }
            } catch (NoSuchAlgorithmException e5) {
                result = null;
                if (!doLog) {
                }
            } catch (NoSuchPaddingException e6) {
                result = null;
                if (!doLog) {
                }
            } catch (InvalidKeyException e7) {
                result = null;
                if (!doLog) {
                }
            }
            try {
                byte[] IV = Arrays.copyOfRange(mkBlob, offset, offset + b);
                int offset2 = offset + b;
                int offset3 = offset2 + 1;
                byte b2 = mkBlob[offset2];
                byte[] mk = Arrays.copyOfRange(mkBlob, offset3, offset3 + b2);
                int offset4 = offset3 + b2;
                int offset5 = offset4 + 1;
                try {
                    if (Arrays.equals(PasswordUtils.makeKeyChecksum(algorithm, mk, ckSalt, rounds), Arrays.copyOfRange(mkBlob, offset5, offset5 + mkBlob[offset4]))) {
                        try {
                            c.init(2, new SecretKeySpec(mk, "AES"), new IvParameterSpec(IV));
                            result2 = new CipherInputStream(rawInStream, c);
                        } catch (InvalidAlgorithmParameterException e8) {
                            e = e8;
                            if (!doLog) {
                                return result;
                            }
                            Slog.e(BackupManagerService.TAG, "Needed parameter spec unavailable!", e);
                            return result;
                        } catch (BadPaddingException e9) {
                            if (!doLog) {
                                return result;
                            }
                            Slog.w(BackupManagerService.TAG, "Incorrect password");
                            return result;
                        } catch (IllegalBlockSizeException e10) {
                            if (!doLog) {
                                return result;
                            }
                            Slog.w(BackupManagerService.TAG, "Invalid block size in master key");
                            return result;
                        } catch (NoSuchAlgorithmException e11) {
                            if (!doLog) {
                                return result;
                            }
                            Slog.e(BackupManagerService.TAG, "Needed decryption algorithm unavailable!");
                            return result;
                        } catch (NoSuchPaddingException e12) {
                            if (!doLog) {
                                return result;
                            }
                            Slog.e(BackupManagerService.TAG, "Needed padding mechanism unavailable!");
                            return result;
                        } catch (InvalidKeyException e13) {
                            if (!doLog) {
                                return result;
                            }
                            Slog.w(BackupManagerService.TAG, "Illegal password; aborting");
                            return result;
                        }
                    } else {
                        if (doLog) {
                            Slog.w(BackupManagerService.TAG, "Incorrect password");
                        }
                        result2 = null;
                    }
                    return result2;
                } catch (InvalidAlgorithmParameterException e14) {
                    e = e14;
                    if (!doLog) {
                    }
                } catch (BadPaddingException e15) {
                    if (!doLog) {
                    }
                } catch (IllegalBlockSizeException e16) {
                    if (!doLog) {
                    }
                } catch (NoSuchAlgorithmException e17) {
                    if (!doLog) {
                    }
                } catch (NoSuchPaddingException e18) {
                    if (!doLog) {
                    }
                } catch (InvalidKeyException e19) {
                    if (!doLog) {
                    }
                }
            } catch (InvalidAlgorithmParameterException e20) {
                e = e20;
                if (!doLog) {
                }
            } catch (BadPaddingException e21) {
                if (!doLog) {
                }
            } catch (IllegalBlockSizeException e22) {
                if (!doLog) {
                }
            } catch (NoSuchAlgorithmException e23) {
                if (!doLog) {
                }
            } catch (NoSuchPaddingException e24) {
                if (!doLog) {
                }
            } catch (InvalidKeyException e25) {
                if (!doLog) {
                }
            }
        } catch (InvalidAlgorithmParameterException e26) {
            e = e26;
            result = null;
            if (!doLog) {
            }
        } catch (BadPaddingException e27) {
            result = null;
            if (!doLog) {
            }
        } catch (IllegalBlockSizeException e28) {
            result = null;
            if (!doLog) {
            }
        } catch (NoSuchAlgorithmException e29) {
            result = null;
            if (!doLog) {
            }
        } catch (NoSuchPaddingException e30) {
            result = null;
            if (!doLog) {
            }
        } catch (InvalidKeyException e31) {
            result = null;
            if (!doLog) {
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
                Slog.w(BackupManagerService.TAG, "Unsupported encryption method: " + encryptionName);
            }
        } catch (NumberFormatException e) {
            Slog.w(BackupManagerService.TAG, "Can't parse restore data header");
        } catch (IOException e2) {
            Slog.w(BackupManagerService.TAG, "Can't read input header");
        }
        return result;
    }
}
