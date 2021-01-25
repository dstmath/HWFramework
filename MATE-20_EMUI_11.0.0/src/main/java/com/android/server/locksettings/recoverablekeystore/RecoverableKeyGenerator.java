package com.android.server.locksettings.recoverablekeystore;

import android.util.Log;
import com.android.server.locksettings.recoverablekeystore.storage.RecoverableKeyStoreDb;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class RecoverableKeyGenerator {
    static final int KEY_SIZE_BITS = 256;
    private static final int RESULT_CANNOT_INSERT_ROW = -1;
    private static final String SECRET_KEY_ALGORITHM = "AES";
    private static final String TAG = "PlatformKeyGen";
    private final RecoverableKeyStoreDb mDatabase;
    private final KeyGenerator mKeyGenerator;

    public static RecoverableKeyGenerator newInstance(RecoverableKeyStoreDb database) throws NoSuchAlgorithmException {
        return new RecoverableKeyGenerator(KeyGenerator.getInstance(SECRET_KEY_ALGORITHM), database);
    }

    private RecoverableKeyGenerator(KeyGenerator keyGenerator, RecoverableKeyStoreDb recoverableKeyStoreDb) {
        this.mKeyGenerator = keyGenerator;
        this.mDatabase = recoverableKeyStoreDb;
    }

    public byte[] generateAndStoreKey(PlatformEncryptionKey platformKey, int userId, int uid, String alias, byte[] metadata) throws RecoverableKeyStorageException, KeyStoreException, InvalidKeyException {
        this.mKeyGenerator.init(256);
        SecretKey key = this.mKeyGenerator.generateKey();
        if (this.mDatabase.insertKey(userId, uid, alias, WrappedKey.fromSecretKey(platformKey, key, metadata)) != -1) {
            if (this.mDatabase.setShouldCreateSnapshot(userId, uid, true) < 0) {
                Log.e(TAG, "Failed to set the shoudCreateSnapshot flag in the local DB.");
            }
            return key.getEncoded();
        }
        throw new RecoverableKeyStorageException(String.format(Locale.US, "Failed writing (%d, %s) to database.", Integer.valueOf(uid), alias));
    }

    public void importKey(PlatformEncryptionKey platformKey, int userId, int uid, String alias, byte[] keyBytes, byte[] metadata) throws RecoverableKeyStorageException, KeyStoreException, InvalidKeyException {
        if (this.mDatabase.insertKey(userId, uid, alias, WrappedKey.fromSecretKey(platformKey, new SecretKeySpec(keyBytes, SECRET_KEY_ALGORITHM), metadata)) != -1) {
            this.mDatabase.setShouldCreateSnapshot(userId, uid, true);
            return;
        }
        throw new RecoverableKeyStorageException(String.format(Locale.US, "Failed writing (%d, %s) to database.", Integer.valueOf(uid), alias));
    }
}
