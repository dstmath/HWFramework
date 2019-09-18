package com.android.server.backup;

import android.content.Context;
import android.util.Slog;
import com.android.server.backup.utils.DataStreamCodec;
import com.android.server.backup.utils.DataStreamFileCodec;
import com.android.server.backup.utils.PasswordUtils;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;

public final class BackupPasswordManager {
    private static final int BACKUP_PW_FILE_VERSION = 2;
    private static final boolean DEBUG = false;
    private static final int DEFAULT_PW_FILE_VERSION = 1;
    private static final String PASSWORD_HASH_FILE_NAME = "pwhash";
    private static final String PASSWORD_VERSION_FILE_NAME = "pwversion";
    public static final String PBKDF_CURRENT = "PBKDF2WithHmacSHA1";
    public static final String PBKDF_FALLBACK = "PBKDF2WithHmacSHA1And8bit";
    private static final String TAG = "BackupPasswordManager";
    private final File mBaseStateDir;
    private final Context mContext;
    private String mPasswordHash;
    private byte[] mPasswordSalt;
    private int mPasswordVersion;
    private final SecureRandom mRng;

    private static final class BackupPasswordHash {
        public String hash;
        public byte[] salt;

        BackupPasswordHash(String hash2, byte[] salt2) {
            this.hash = hash2;
            this.salt = salt2;
        }
    }

    private static final class PasswordHashFileCodec implements DataStreamCodec<BackupPasswordHash> {
        private PasswordHashFileCodec() {
        }

        public void serialize(BackupPasswordHash backupPasswordHash, DataOutputStream dataOutputStream) throws IOException {
            dataOutputStream.writeInt(backupPasswordHash.salt.length);
            dataOutputStream.write(backupPasswordHash.salt);
            dataOutputStream.writeUTF(backupPasswordHash.hash);
        }

        public BackupPasswordHash deserialize(DataInputStream dataInputStream) throws IOException {
            byte[] salt = new byte[dataInputStream.readInt()];
            dataInputStream.readFully(salt);
            return new BackupPasswordHash(dataInputStream.readUTF(), salt);
        }
    }

    private static final class PasswordVersionFileCodec implements DataStreamCodec<Integer> {
        private PasswordVersionFileCodec() {
        }

        public void serialize(Integer integer, DataOutputStream dataOutputStream) throws IOException {
            dataOutputStream.write(integer.intValue());
        }

        public Integer deserialize(DataInputStream dataInputStream) throws IOException {
            return Integer.valueOf(dataInputStream.readInt());
        }
    }

    BackupPasswordManager(Context context, File baseStateDir, SecureRandom secureRandom) {
        this.mContext = context;
        this.mRng = secureRandom;
        this.mBaseStateDir = baseStateDir;
        loadStateFromFilesystem();
    }

    /* access modifiers changed from: package-private */
    public boolean hasBackupPassword() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.BACKUP", "hasBackupPassword");
        return this.mPasswordHash != null && this.mPasswordHash.length() > 0;
    }

    /* access modifiers changed from: package-private */
    public boolean backupPasswordMatches(String password) {
        if (!hasBackupPassword() || passwordMatchesSaved(password)) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean setBackupPassword(String currentPassword, String newPassword) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.BACKUP", "setBackupPassword");
        if (!passwordMatchesSaved(currentPassword)) {
            return false;
        }
        try {
            getPasswordVersionFileCodec().serialize(2);
            this.mPasswordVersion = 2;
            if (newPassword == null || newPassword.isEmpty()) {
                return clearPassword();
            }
            try {
                byte[] salt = randomSalt();
                String newPwHash = PasswordUtils.buildPasswordHash(PBKDF_CURRENT, newPassword, salt, 10000);
                getPasswordHashFileCodec().serialize(new BackupPasswordHash(newPwHash, salt));
                this.mPasswordHash = newPwHash;
                this.mPasswordSalt = salt;
                return true;
            } catch (IOException e) {
                Slog.e(TAG, "Unable to set backup password");
                return false;
            }
        } catch (IOException e2) {
            Slog.e(TAG, "Unable to write backup pw version; password not changed");
            return false;
        }
    }

    private boolean usePbkdf2Fallback() {
        return this.mPasswordVersion < 2;
    }

    private boolean clearPassword() {
        File passwordHashFile = getPasswordHashFile();
        if (!passwordHashFile.exists() || passwordHashFile.delete()) {
            this.mPasswordHash = null;
            this.mPasswordSalt = null;
            return true;
        }
        Slog.e(TAG, "Unable to clear backup password");
        return false;
    }

    private void loadStateFromFilesystem() {
        try {
            this.mPasswordVersion = getPasswordVersionFileCodec().deserialize().intValue();
        } catch (IOException e) {
            Slog.e(TAG, "Unable to read backup pw version");
            this.mPasswordVersion = 1;
        }
        try {
            BackupPasswordHash hash = getPasswordHashFileCodec().deserialize();
            this.mPasswordHash = hash.hash;
            this.mPasswordSalt = hash.salt;
        } catch (IOException e2) {
            Slog.e(TAG, "Unable to read saved backup pw hash");
        }
    }

    private boolean passwordMatchesSaved(String candidatePassword) {
        return passwordMatchesSaved(PBKDF_CURRENT, candidatePassword) || (usePbkdf2Fallback() && passwordMatchesSaved(PBKDF_FALLBACK, candidatePassword));
    }

    private boolean passwordMatchesSaved(String algorithm, String candidatePassword) {
        boolean z = false;
        if (this.mPasswordHash == null) {
            if (candidatePassword == null || candidatePassword.equals(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS)) {
                z = true;
            }
            return z;
        } else if (candidatePassword == null || candidatePassword.length() == 0) {
            return false;
        } else {
            return this.mPasswordHash.equalsIgnoreCase(PasswordUtils.buildPasswordHash(algorithm, candidatePassword, this.mPasswordSalt, 10000));
        }
    }

    private byte[] randomSalt() {
        byte[] array = new byte[(512 / 8)];
        this.mRng.nextBytes(array);
        return array;
    }

    private DataStreamFileCodec<Integer> getPasswordVersionFileCodec() {
        return new DataStreamFileCodec<>(new File(this.mBaseStateDir, PASSWORD_VERSION_FILE_NAME), new PasswordVersionFileCodec());
    }

    private DataStreamFileCodec<BackupPasswordHash> getPasswordHashFileCodec() {
        return new DataStreamFileCodec<>(getPasswordHashFile(), new PasswordHashFileCodec());
    }

    private File getPasswordHashFile() {
        return new File(this.mBaseStateDir, PASSWORD_HASH_FILE_NAME);
    }
}
