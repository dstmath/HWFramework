package com.huawei.server.security.securityprofile;

import android.security.keystore.KeyProtection;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.android.security.keystore.KeyProtectionBuilderEx;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.ProviderException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import org.json.JSONException;
import org.json.JSONObject;

/* access modifiers changed from: package-private */
public class PolicyStorage {
    private static final int AUTH_TAG_LEN = 128;
    private static final boolean DEBUG = SecurityProfileUtils.DEBUG;
    private static final String HMAC_SHA_ALGORITHM = "HmacSHA256";
    private static final int IV_BYTES = 4;
    private static final String KEY_PREFIX = "SecurityProfileDB";
    private static final int MAX_IV_LEN = 64;
    private static final int MIN_IV_LEN = 0;
    private static final Object PERSISTENT_DATA_LOCK = new Object();
    private static final int PIECE_SIZE = 16384;
    private static final String TAG = "SecurityProfileDB";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final Object WORKER_LOCK = new Object();
    private static final long WRITE_DATABASE_FIXED_WAITING_TIME_MS = 5000;
    private static final long WRITE_DATABASE_TIMEOUT_MS = 5000;
    private final String mDefaultContent;
    private Path mFile;
    private SecretKey mKey;
    private String mKeyName = null;
    private String mPersistentData;
    private ThreadPoolExecutor mWatcherThreadPool = SecurityProfileUtils.createSingleWatcherThreadPool();
    private Runnable mWriteDataBaseWatcher = new Runnable() {
        /* class com.huawei.server.security.securityprofile.PolicyStorage.AnonymousClass1 */

        @Override // java.lang.Runnable
        public void run() {
            PolicyStorage.this.writeDatabaseLongTimeBlocking();
        }
    };

    PolicyStorage(String dbName, String content) {
        this.mKeyName = "SecurityProfileDB" + dbName;
        this.mDefaultContent = content;
        this.mFile = Paths.get(dbName, new String[0]);
        initKey();
    }

    private void addPersistentDataThread() {
        this.mWatcherThreadPool.execute(this.mWriteDataBaseWatcher);
    }

    private void setPersistentData(String data) {
        synchronized (PERSISTENT_DATA_LOCK) {
            this.mPersistentData = data;
        }
    }

    private String getAndCleanPersistentData() {
        String data;
        synchronized (PERSISTENT_DATA_LOCK) {
            data = this.mPersistentData;
            this.mPersistentData = null;
        }
        return data;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void persistentData() {
        String data = getAndCleanPersistentData();
        if (!TextUtils.isEmpty(data)) {
            writeDatabaseImpl(data);
        }
    }

    private void initKey() {
        if (!getKey(false)) {
            Log.w("SecurityProfileDB", "initKey get no old key, I must delete file: " + this.mFile);
            try {
                if (Files.exists(this.mFile, new LinkOption[0])) {
                    Files.delete(this.mFile);
                }
            } catch (IOException e) {
                Log.e("SecurityProfileDB", "initKey delete file IO exception: " + e.getMessage());
            }
            if (getKey(true)) {
                Log.i("SecurityProfileDB", "initKey create new key Succ!");
            } else {
                Log.e("SecurityProfileDB", "initKey create new key failed!");
            }
        }
        Log.i("SecurityProfileDB", "initKey end!");
    }

    private boolean getKey(boolean create) {
        if (create) {
            try {
                KeyGenerator kg = KeyGenerator.getInstance("AES");
                kg.init(new SecureRandom());
                KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
                ks.load(null);
                ks.deleteEntry(this.mKeyName);
                ks.setEntry(this.mKeyName, new KeyStore.SecretKeyEntry(kg.generateKey()), KeyProtectionBuilderEx.setCriticalToDeviceEncryption(new KeyProtection.Builder(3).setBlockModes("GCM").setEncryptionPaddings("NoPadding"), true).build());
                this.mKey = (SecretKey) ks.getKey(this.mKeyName, null);
                Log.i("SecurityProfileDB", "getKey create new key end!");
            } catch (IOException | KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException | CertificateException ignore) {
                Log.e("SecurityProfileDB", "getKey exception" + ignore.getMessage());
                this.mKey = null;
                return false;
            }
        } else {
            KeyStore ks2 = KeyStore.getInstance("AndroidKeyStore");
            ks2.load(null);
            this.mKey = (SecretKey) ks2.getKey(this.mKeyName, null);
        }
        return this.mKey != null;
    }

    private byte[] bufferedDoFinal(Cipher cipher, byte[] data) throws IOException, BadPaddingException, IllegalBlockSizeException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        int offset = 0;
        while (offset + PIECE_SIZE <= data.length) {
            try {
                byte[] out = cipher.update(data, offset, PIECE_SIZE);
                if (out != null) {
                    output.write(out);
                }
                offset += PIECE_SIZE;
            } finally {
                try {
                    output.close();
                } catch (IOException e) {
                    Log.w("SecurityProfileDB", "buffed do final ByteArrayOutputStream close failed: " + e.getMessage());
                }
            }
        }
        byte[] out2 = cipher.doFinal(data, offset, data.length - offset);
        if (out2 != null) {
            output.write(out2);
        }
        return output.toByteArray();
    }

    /* access modifiers changed from: package-private */
    public void writeDatabase(@Nullable JSONObject policy) {
        if (policy == null) {
            Log.w("SecurityProfileDB", "writeDatabase but policy null");
            return;
        }
        String policyString = policy.toString();
        if (TextUtils.isEmpty(policyString)) {
            Log.w("SecurityProfileDB", "writeDatabase but policy string empty");
            return;
        }
        setPersistentData(policyString);
        addPersistentDataThread();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void writeDatabaseLongTimeBlocking() {
        synchronized (WORKER_LOCK) {
            Future<Boolean> future = null;
            try {
                Future<Boolean> future2 = SecurityProfileUtils.getWorkerThreadPool().submit(new Callable<Boolean>() {
                    /* class com.huawei.server.security.securityprofile.PolicyStorage.AnonymousClass2 */

                    @Override // java.util.concurrent.Callable
                    public Boolean call() throws Exception {
                        PolicyStorage.this.persistentData();
                        return true;
                    }
                });
                Thread.sleep(5000);
                if (future2.get(5000, TimeUnit.MILLISECONDS) == null) {
                    Log.w("SecurityProfileDB", "Watcher time out, but result is null!");
                }
            } catch (InterruptedException e) {
                Log.e("SecurityProfileDB", "Failed to watch the worker, the watcher was interrupted: " + e.getMessage());
            } catch (ExecutionException e2) {
                Log.e("SecurityProfileDB", "Failed to execute the worker: " + e2.getMessage());
            } catch (TimeoutException e3) {
                boolean isCancelSucc = future.cancel(true);
                Log.e("SecurityProfileDB", "Max write timeout reached, the worker thread canceled: " + isCancelSucc);
            }
        }
    }

    private void writeDatabaseImpl(String policy) {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(1, this.mKey);
            byte[] iv = cipher.getIV();
            if (iv == null) {
                Log.w("SecurityProfileDB", "write database get IV null for given algorithm does not use an IV");
                return;
            }
            byte[] cipherText = bufferedDoFinal(cipher, policy.getBytes(StandardCharsets.UTF_8));
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + 4 + cipherText.length);
            byteBuffer.putInt(iv.length);
            byteBuffer.put(iv);
            byteBuffer.put(cipherText);
            Files.write(this.mFile, byteBuffer.array(), new OpenOption[0]);
            Log.i("SecurityProfileDB", "Write database file end");
        } catch (IOException | InvalidKeyException | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e) {
            Log.e("SecurityProfileDB", "write database exception" + e.getMessage());
        } catch (ProviderException e2) {
            Log.e("SecurityProfileDB", "Failed to connect key store service: " + e2.getMessage());
        } catch (Exception e3) {
            Log.e("SecurityProfileDB", "Failed to write database unknown exception!");
        }
    }

    private String readAndVerifyDatabase() throws SecurityException {
        try {
            ByteBuffer byteBuffer = ByteBuffer.wrap(Files.readAllBytes(this.mFile));
            int ivLength = byteBuffer.getInt();
            if (ivLength < 0 || ivLength > 64) {
                Log.e("SecurityProfileDB", "readAndVerifyDatabase too large IV length err, ivLength: " + ivLength + ", mFile: " + this.mFile);
                throw new InvalidAlgorithmParameterException("Too large IV length: " + ivLength);
            }
            byte[] iv = new byte[ivLength];
            byteBuffer.get(iv);
            byte[] cipherText = new byte[byteBuffer.remaining()];
            byteBuffer.get(cipherText);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(2, this.mKey, new GCMParameterSpec(128, iv));
            return new String(bufferedDoFinal(cipher, cipherText), StandardCharsets.UTF_8);
        } catch (IOException | InvalidAlgorithmParameterException | InvalidKeyException | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e) {
            throw new SecurityException("Failed to read and verify Database: " + e.getMessage());
        } catch (ProviderException e2) {
            throw new SecurityException("Failed to connect key store service: " + e2.getMessage());
        } catch (Exception e3) {
            throw new SecurityException("Failed to read and verify database unknown exception!");
        }
    }

    /* access modifiers changed from: package-private */
    @Nullable
    public JSONObject parseJSON(String json) {
        if (json == null) {
            return null;
        }
        try {
            return new JSONObject(json);
        } catch (JSONException e) {
            Log.e("SecurityProfileDB", "parseJSON err: " + e.getMessage());
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    @Nullable
    public JSONObject readDatabase() {
        if (DEBUG) {
            Log.d("SecurityProfileDB", "readDatabase file begin: " + this.mFile);
        }
        try {
            if (Files.exists(this.mFile, new LinkOption[0])) {
                String json = readAndVerifyDatabase();
                if (DEBUG) {
                    Log.d("SecurityProfileDB", "readDatabase file done: " + this.mFile);
                }
                return parseJSON(json);
            } else if (this.mDefaultContent == null) {
                return null;
            } else {
                JSONObject policy = parseJSON(this.mDefaultContent);
                writeDatabase(policy);
                return policy;
            }
        } catch (SecurityException e) {
            Log.e("SecurityProfileDB", "readDatabase exception: " + e.getMessage());
            return parseJSON(this.mDefaultContent);
        }
    }
}
