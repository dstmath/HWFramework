package com.android.server.locksettings.recoverablekeystore.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import com.android.server.locksettings.recoverablekeystore.TestOnlyInsecureCertificateHelper;
import com.android.server.locksettings.recoverablekeystore.WrappedKey;
import com.android.server.net.watchlist.WatchlistLoggingHandler;
import java.io.ByteArrayInputStream;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertPath;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;
import java.util.function.IntConsumer;

public class RecoverableKeyStoreDb {
    private static final String CERT_PATH_ENCODING = "PkiPath";
    private static final int IDLE_TIMEOUT_SECONDS = 30;
    private static final int LAST_SYNCED_AT_UNSYNCED = -1;
    private static final String TAG = "RecoverableKeyStoreDb";
    private final RecoverableKeyStoreDbHelper mKeyStoreDbHelper;
    private final TestOnlyInsecureCertificateHelper mTestOnlyInsecureCertificateHelper = new TestOnlyInsecureCertificateHelper();

    public static RecoverableKeyStoreDb newInstance(Context context) {
        RecoverableKeyStoreDbHelper helper = new RecoverableKeyStoreDbHelper(context);
        helper.setWriteAheadLoggingEnabled(true);
        helper.setIdleConnectionTimeout(30);
        return new RecoverableKeyStoreDb(helper);
    }

    private RecoverableKeyStoreDb(RecoverableKeyStoreDbHelper keyStoreDbHelper) {
        this.mKeyStoreDbHelper = keyStoreDbHelper;
    }

    public long insertKey(int userId, int uid, String alias, WrappedKey wrappedKey) {
        SQLiteDatabase db = this.mKeyStoreDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", Integer.valueOf(userId));
        values.put(WatchlistLoggingHandler.WatchlistEventKeys.UID, Integer.valueOf(uid));
        values.put("alias", alias);
        values.put("nonce", wrappedKey.getNonce());
        values.put("wrapped_key", wrappedKey.getKeyMaterial());
        values.put("last_synced_at", (Integer) -1);
        values.put("platform_key_generation_id", Integer.valueOf(wrappedKey.getPlatformKeyGenerationId()));
        values.put("recovery_status", Integer.valueOf(wrappedKey.getRecoveryStatus()));
        byte[] keyMetadata = wrappedKey.getKeyMetadata();
        if (keyMetadata == null) {
            values.putNull("key_metadata");
        } else {
            values.put("key_metadata", keyMetadata);
        }
        return db.replace("keys", null, values);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x00bb, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x00bd, code lost:
        if (r2 != null) goto L_0x00bf;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x00bf, code lost:
        $closeResource(r0, r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x00c2, code lost:
        throw r0;
     */
    public WrappedKey getKey(int uid, String alias) {
        byte[] keyMetadata;
        Cursor cursor = this.mKeyStoreDbHelper.getReadableDatabase().query("keys", new String[]{"_id", "nonce", "wrapped_key", "platform_key_generation_id", "recovery_status", "key_metadata"}, "uid = ? AND alias = ?", new String[]{Integer.toString(uid), alias}, null, null, null);
        int count = cursor.getCount();
        if (count == 0) {
            $closeResource(null, cursor);
            return null;
        } else if (count > 1) {
            Log.wtf(TAG, String.format(Locale.US, "%d WrappedKey entries found for uid=%d alias='%s'. Should only ever be 0 or 1.", Integer.valueOf(count), Integer.valueOf(uid), alias));
            $closeResource(null, cursor);
            return null;
        } else {
            cursor.moveToFirst();
            byte[] nonce = cursor.getBlob(cursor.getColumnIndexOrThrow("nonce"));
            byte[] keyMaterial = cursor.getBlob(cursor.getColumnIndexOrThrow("wrapped_key"));
            int generationId = cursor.getInt(cursor.getColumnIndexOrThrow("platform_key_generation_id"));
            int recoveryStatus = cursor.getInt(cursor.getColumnIndexOrThrow("recovery_status"));
            int metadataIdx = cursor.getColumnIndexOrThrow("key_metadata");
            if (cursor.isNull(metadataIdx)) {
                keyMetadata = null;
            } else {
                keyMetadata = cursor.getBlob(metadataIdx);
            }
            WrappedKey wrappedKey = new WrappedKey(nonce, keyMaterial, keyMetadata, generationId, recoveryStatus);
            $closeResource(null, cursor);
            return wrappedKey;
        }
    }

    private static /* synthetic */ void $closeResource(Throwable x0, AutoCloseable x1) {
        if (x0 != null) {
            try {
                x1.close();
            } catch (Throwable th) {
                x0.addSuppressed(th);
            }
        } else {
            x1.close();
        }
    }

    public boolean removeKey(int uid, String alias) {
        return this.mKeyStoreDbHelper.getWritableDatabase().delete("keys", "uid = ? AND alias = ?", new String[]{Integer.toString(uid), alias}) > 0;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0059, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x005a, code lost:
        if (r1 != null) goto L_0x005c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x005c, code lost:
        $closeResource(r2, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x005f, code lost:
        throw r4;
     */
    public Map<String, Integer> getStatusForAllKeys(int uid) {
        Cursor cursor = this.mKeyStoreDbHelper.getReadableDatabase().query("keys", new String[]{"_id", "alias", "recovery_status"}, "uid = ?", new String[]{Integer.toString(uid)}, null, null, null);
        HashMap<String, Integer> statuses = new HashMap<>();
        while (cursor.moveToNext()) {
            statuses.put(cursor.getString(cursor.getColumnIndexOrThrow("alias")), Integer.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("recovery_status"))));
        }
        $closeResource(null, cursor);
        return statuses;
    }

    public int setRecoveryStatus(int uid, String alias, int status) {
        SQLiteDatabase db = this.mKeyStoreDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("recovery_status", Integer.valueOf(status));
        return db.update("keys", values, "uid = ? AND alias = ?", new String[]{String.valueOf(uid), alias});
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x00a4, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x00a6, code lost:
        if (r2 != null) goto L_0x00a8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x00a8, code lost:
        $closeResource(r0, r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x00ab, code lost:
        throw r0;
     */
    public Map<String, WrappedKey> getAllKeys(int userId, int recoveryAgentUid, int platformKeyGenerationId) {
        byte[] keyMetadata;
        Cursor cursor = this.mKeyStoreDbHelper.getReadableDatabase().query("keys", new String[]{"_id", "nonce", "wrapped_key", "alias", "recovery_status", "key_metadata"}, "user_id = ? AND uid = ? AND platform_key_generation_id = ?", new String[]{Integer.toString(userId), Integer.toString(recoveryAgentUid), Integer.toString(platformKeyGenerationId)}, null, null, null);
        HashMap<String, WrappedKey> keys = new HashMap<>();
        while (cursor.moveToNext()) {
            byte[] nonce = cursor.getBlob(cursor.getColumnIndexOrThrow("nonce"));
            byte[] keyMaterial = cursor.getBlob(cursor.getColumnIndexOrThrow("wrapped_key"));
            String alias = cursor.getString(cursor.getColumnIndexOrThrow("alias"));
            int recoveryStatus = cursor.getInt(cursor.getColumnIndexOrThrow("recovery_status"));
            int metadataIdx = cursor.getColumnIndexOrThrow("key_metadata");
            if (cursor.isNull(metadataIdx)) {
                keyMetadata = null;
            } else {
                keyMetadata = cursor.getBlob(metadataIdx);
            }
            keys.put(alias, new WrappedKey(nonce, keyMaterial, keyMetadata, platformKeyGenerationId, recoveryStatus));
        }
        $closeResource(null, cursor);
        return keys;
    }

    public long setPlatformKeyGenerationId(int userId, int generationId) {
        SQLiteDatabase db = this.mKeyStoreDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", Integer.valueOf(userId));
        values.put("platform_key_generation_id", Integer.valueOf(generationId));
        String[] selectionArguments = {String.valueOf(userId)};
        ensureUserMetadataEntryExists(userId);
        invalidateKeysForUser(userId);
        return (long) db.update("user_metadata", values, "user_id = ?", selectionArguments);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0053, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0054, code lost:
        if (r1 != null) goto L_0x0056;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0056, code lost:
        $closeResource(r2, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0059, code lost:
        throw r4;
     */
    public Map<Integer, Long> getUserSerialNumbers() {
        Cursor cursor = this.mKeyStoreDbHelper.getReadableDatabase().query("user_metadata", new String[]{"user_id", "user_serial_number"}, null, new String[0], null, null, null);
        Map<Integer, Long> serialNumbers = new ArrayMap<>();
        while (cursor.moveToNext()) {
            serialNumbers.put(Integer.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("user_id"))), Long.valueOf(cursor.getLong(cursor.getColumnIndexOrThrow("user_serial_number"))));
        }
        $closeResource(null, cursor);
        return serialNumbers;
    }

    public long setUserSerialNumber(int userId, long serialNumber) {
        SQLiteDatabase db = this.mKeyStoreDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", Integer.valueOf(userId));
        values.put("user_serial_number", Long.valueOf(serialNumber));
        String[] selectionArguments = {String.valueOf(userId)};
        ensureUserMetadataEntryExists(userId);
        return (long) db.update("user_metadata", values, "user_id = ?", selectionArguments);
    }

    public void invalidateKeysForUser(int userId) {
        SQLiteDatabase db = this.mKeyStoreDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("recovery_status", (Integer) 3);
        db.update("keys", values, "user_id = ?", new String[]{String.valueOf(userId)});
    }

    public void invalidateKeysForUserIdOnCustomScreenLock(int userId) {
        SQLiteDatabase db = this.mKeyStoreDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("recovery_status", (Integer) 3);
        db.update("keys", values, "user_id = ?", new String[]{String.valueOf(userId)});
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0045, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0046, code lost:
        if (r1 != null) goto L_0x0048;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0048, code lost:
        $closeResource(r2, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x004b, code lost:
        throw r4;
     */
    public int getPlatformKeyGenerationId(int userId) {
        Cursor cursor = this.mKeyStoreDbHelper.getReadableDatabase().query("user_metadata", new String[]{"platform_key_generation_id"}, "user_id = ?", new String[]{Integer.toString(userId)}, null, null, null);
        if (cursor.getCount() == 0) {
            $closeResource(null, cursor);
            return -1;
        }
        cursor.moveToFirst();
        int i = cursor.getInt(cursor.getColumnIndexOrThrow("platform_key_generation_id"));
        $closeResource(null, cursor);
        return i;
    }

    public long setRecoveryServicePublicKey(int userId, int uid, PublicKey publicKey) {
        return setBytes(userId, uid, "public_key", publicKey.getEncoded());
    }

    public Long getRecoveryServiceCertSerial(int userId, int uid, String rootAlias) {
        return getLong(userId, uid, rootAlias, "cert_serial");
    }

    public long setRecoveryServiceCertSerial(int userId, int uid, String rootAlias, long serial) {
        return setLong(userId, uid, rootAlias, "cert_serial", serial);
    }

    public CertPath getRecoveryServiceCertPath(int userId, int uid, String rootAlias) {
        byte[] bytes = getBytes(userId, uid, rootAlias, "cert_path");
        if (bytes == null) {
            return null;
        }
        try {
            return decodeCertPath(bytes);
        } catch (CertificateException e) {
            Log.wtf(TAG, String.format(Locale.US, "Recovery service CertPath entry cannot be decoded for userId=%d uid=%d.", Integer.valueOf(userId), Integer.valueOf(uid)), e);
            return null;
        }
    }

    public long setRecoveryServiceCertPath(int userId, int uid, String rootAlias, CertPath certPath) throws CertificateEncodingException {
        if (certPath.getCertificates().size() != 0) {
            return setBytes(userId, uid, rootAlias, "cert_path", certPath.getEncoded(CERT_PATH_ENCODING));
        }
        throw new CertificateEncodingException("No certificate contained in the cert path.");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0050, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0051, code lost:
        if (r1 != null) goto L_0x0053;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0053, code lost:
        $closeResource(r2, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0056, code lost:
        throw r4;
     */
    public List<Integer> getRecoveryAgents(int userId) {
        Cursor cursor = this.mKeyStoreDbHelper.getReadableDatabase().query("recovery_service_metadata", new String[]{WatchlistLoggingHandler.WatchlistEventKeys.UID}, "user_id = ?", new String[]{Integer.toString(userId)}, null, null, null);
        ArrayList<Integer> result = new ArrayList<>(cursor.getCount());
        while (cursor.moveToNext()) {
            result.add(Integer.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(WatchlistLoggingHandler.WatchlistEventKeys.UID))));
        }
        $closeResource(null, cursor);
        return result;
    }

    public PublicKey getRecoveryServicePublicKey(int userId, int uid) {
        byte[] keyBytes = getBytes(userId, uid, "public_key");
        if (keyBytes == null) {
            return null;
        }
        try {
            return decodeX509Key(keyBytes);
        } catch (InvalidKeySpecException e) {
            Log.wtf(TAG, String.format(Locale.US, "Recovery service public key entry cannot be decoded for userId=%d uid=%d.", Integer.valueOf(userId), Integer.valueOf(uid)));
            return null;
        }
    }

    public long setRecoverySecretTypes(int userId, int uid, int[] secretTypes) {
        SQLiteDatabase db = this.mKeyStoreDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        StringJoiner joiner = new StringJoiner(",");
        Arrays.stream(secretTypes).forEach(new IntConsumer(joiner) {
            /* class com.android.server.locksettings.recoverablekeystore.storage.$$Lambda$RecoverableKeyStoreDb$knfkhmVPS_11tGWkGt87bH4xjYg */
            private final /* synthetic */ StringJoiner f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.IntConsumer
            public final void accept(int i) {
                this.f$0.add(Integer.toString(i));
            }
        });
        values.put("secret_types", joiner.toString());
        ensureRecoveryServiceMetadataEntryExists(userId, uid);
        return (long) db.update("recovery_service_metadata", values, "user_id = ? AND uid = ?", new String[]{String.valueOf(userId), String.valueOf(uid)});
    }

    /* JADX WARNING: Code restructure failed: missing block: B:33:0x00ce, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00d0, code lost:
        if (r2 != null) goto L_0x00d2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00d2, code lost:
        $closeResource(r0, r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x00d5, code lost:
        throw r0;
     */
    public int[] getRecoverySecretTypes(int userId, int uid) {
        Cursor cursor = this.mKeyStoreDbHelper.getReadableDatabase().query("recovery_service_metadata", new String[]{"_id", "user_id", WatchlistLoggingHandler.WatchlistEventKeys.UID, "secret_types"}, "user_id = ? AND uid = ?", new String[]{Integer.toString(userId), Integer.toString(uid)}, null, null, null);
        int count = cursor.getCount();
        if (count == 0) {
            int[] iArr = new int[0];
            $closeResource(null, cursor);
            return iArr;
        } else if (count > 1) {
            Log.wtf(TAG, String.format(Locale.US, "%d deviceId entries found for userId=%d uid=%d. Should only ever be 0 or 1.", Integer.valueOf(count), Integer.valueOf(userId), Integer.valueOf(uid)));
            int[] iArr2 = new int[0];
            $closeResource(null, cursor);
            return iArr2;
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndexOrThrow("secret_types");
            if (cursor.isNull(idx)) {
                int[] iArr3 = new int[0];
                $closeResource(null, cursor);
                return iArr3;
            }
            String csv = cursor.getString(idx);
            if (TextUtils.isEmpty(csv)) {
                int[] iArr4 = new int[0];
                $closeResource(null, cursor);
                return iArr4;
            }
            String[] types = csv.split(",");
            int[] result = new int[types.length];
            for (int i = 0; i < types.length; i++) {
                try {
                    result[i] = Integer.parseInt(types[i]);
                } catch (NumberFormatException e) {
                    Log.wtf(TAG, "String format error " + e);
                }
            }
            $closeResource(null, cursor);
            return result;
        }
    }

    public long setActiveRootOfTrust(int userId, int uid, String rootAlias) {
        SQLiteDatabase db = this.mKeyStoreDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("active_root_of_trust", rootAlias);
        ensureRecoveryServiceMetadataEntryExists(userId, uid);
        return (long) db.update("recovery_service_metadata", values, "user_id = ? AND uid = ?", new String[]{String.valueOf(userId), String.valueOf(uid)});
    }

    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0095, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0097, code lost:
        if (r2 != null) goto L_0x0099;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0099, code lost:
        $closeResource(r0, r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x009c, code lost:
        throw r0;
     */
    public String getActiveRootOfTrust(int userId, int uid) {
        Cursor cursor = this.mKeyStoreDbHelper.getReadableDatabase().query("recovery_service_metadata", new String[]{"_id", "user_id", WatchlistLoggingHandler.WatchlistEventKeys.UID, "active_root_of_trust"}, "user_id = ? AND uid = ?", new String[]{Integer.toString(userId), Integer.toString(uid)}, null, null, null);
        int count = cursor.getCount();
        if (count == 0) {
            $closeResource(null, cursor);
            return null;
        } else if (count > 1) {
            Log.wtf(TAG, String.format(Locale.US, "%d deviceId entries found for userId=%d uid=%d. Should only ever be 0 or 1.", Integer.valueOf(count), Integer.valueOf(userId), Integer.valueOf(uid)));
            $closeResource(null, cursor);
            return null;
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndexOrThrow("active_root_of_trust");
            if (cursor.isNull(idx)) {
                $closeResource(null, cursor);
                return null;
            }
            String result = cursor.getString(idx);
            if (TextUtils.isEmpty(result)) {
                $closeResource(null, cursor);
                return null;
            }
            $closeResource(null, cursor);
            return result;
        }
    }

    public long setCounterId(int userId, int uid, long counterId) {
        return setLong(userId, uid, "counter_id", counterId);
    }

    public Long getCounterId(int userId, int uid) {
        return getLong(userId, uid, "counter_id");
    }

    public long setServerParams(int userId, int uid, byte[] serverParams) {
        return setBytes(userId, uid, "server_params", serverParams);
    }

    public byte[] getServerParams(int userId, int uid) {
        return getBytes(userId, uid, "server_params");
    }

    public long setSnapshotVersion(int userId, int uid, long snapshotVersion) {
        return setLong(userId, uid, "snapshot_version", snapshotVersion);
    }

    public Long getSnapshotVersion(int userId, int uid) {
        return getLong(userId, uid, "snapshot_version");
    }

    public long setShouldCreateSnapshot(int userId, int uid, boolean pending) {
        return setLong(userId, uid, "should_create_snapshot", pending ? 1 : 0);
    }

    public boolean getShouldCreateSnapshot(int userId, int uid) {
        Long res = getLong(userId, uid, "should_create_snapshot");
        return (res == null || res.longValue() == 0) ? false : true;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0094, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0096, code lost:
        if (r3 != null) goto L_0x0098;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0098, code lost:
        $closeResource(r0, r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x009b, code lost:
        throw r0;
     */
    private Long getLong(int userId, int uid, String key) {
        Cursor cursor = this.mKeyStoreDbHelper.getReadableDatabase().query("recovery_service_metadata", new String[]{"_id", "user_id", WatchlistLoggingHandler.WatchlistEventKeys.UID, key}, "user_id = ? AND uid = ?", new String[]{Integer.toString(userId), Integer.toString(uid)}, null, null, null);
        int count = cursor.getCount();
        if (count == 0) {
            $closeResource(null, cursor);
            return null;
        } else if (count > 1) {
            Log.wtf(TAG, String.format(Locale.US, "%d entries found for userId=%d uid=%d. Should only ever be 0 or 1.", Integer.valueOf(count), Integer.valueOf(userId), Integer.valueOf(uid)));
            $closeResource(null, cursor);
            return null;
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndexOrThrow(key);
            if (cursor.isNull(idx)) {
                $closeResource(null, cursor);
                return null;
            }
            Long valueOf = Long.valueOf(cursor.getLong(idx));
            $closeResource(null, cursor);
            return valueOf;
        }
    }

    private long setLong(int userId, int uid, String key, long value) {
        SQLiteDatabase db = this.mKeyStoreDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(key, Long.valueOf(value));
        String[] selectionArguments = {Integer.toString(userId), Integer.toString(uid)};
        ensureRecoveryServiceMetadataEntryExists(userId, uid);
        return (long) db.update("recovery_service_metadata", values, "user_id = ? AND uid = ?", selectionArguments);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0090, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0092, code lost:
        if (r3 != null) goto L_0x0094;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0094, code lost:
        $closeResource(r0, r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0097, code lost:
        throw r0;
     */
    private byte[] getBytes(int userId, int uid, String key) {
        Cursor cursor = this.mKeyStoreDbHelper.getReadableDatabase().query("recovery_service_metadata", new String[]{"_id", "user_id", WatchlistLoggingHandler.WatchlistEventKeys.UID, key}, "user_id = ? AND uid = ?", new String[]{Integer.toString(userId), Integer.toString(uid)}, null, null, null);
        int count = cursor.getCount();
        if (count == 0) {
            $closeResource(null, cursor);
            return null;
        } else if (count > 1) {
            Log.wtf(TAG, String.format(Locale.US, "%d entries found for userId=%d uid=%d. Should only ever be 0 or 1.", Integer.valueOf(count), Integer.valueOf(userId), Integer.valueOf(uid)));
            $closeResource(null, cursor);
            return null;
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndexOrThrow(key);
            if (cursor.isNull(idx)) {
                $closeResource(null, cursor);
                return null;
            }
            byte[] blob = cursor.getBlob(idx);
            $closeResource(null, cursor);
            return blob;
        }
    }

    private long setBytes(int userId, int uid, String key, byte[] value) {
        SQLiteDatabase db = this.mKeyStoreDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(key, value);
        String[] selectionArguments = {Integer.toString(userId), Integer.toString(uid)};
        ensureRecoveryServiceMetadataEntryExists(userId, uid);
        return (long) db.update("recovery_service_metadata", values, "user_id = ? AND uid = ?", selectionArguments);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x00a1, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x00a3, code lost:
        if (r4 != null) goto L_0x00a5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x00a5, code lost:
        $closeResource(r0, r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x00a8, code lost:
        throw r0;
     */
    private byte[] getBytes(int userId, int uid, String rootAlias, String key) {
        Cursor cursor = this.mKeyStoreDbHelper.getReadableDatabase().query("root_of_trust", new String[]{"_id", "user_id", WatchlistLoggingHandler.WatchlistEventKeys.UID, "root_alias", key}, "user_id = ? AND uid = ? AND root_alias = ?", new String[]{Integer.toString(userId), Integer.toString(uid), this.mTestOnlyInsecureCertificateHelper.getDefaultCertificateAliasIfEmpty(rootAlias)}, null, null, null);
        int count = cursor.getCount();
        if (count == 0) {
            $closeResource(null, cursor);
            return null;
        } else if (count > 1) {
            Log.wtf(TAG, String.format(Locale.US, "%d entries found for userId=%d uid=%d. Should only ever be 0 or 1.", Integer.valueOf(count), Integer.valueOf(userId), Integer.valueOf(uid)));
            $closeResource(null, cursor);
            return null;
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndexOrThrow(key);
            if (cursor.isNull(idx)) {
                $closeResource(null, cursor);
                return null;
            }
            byte[] blob = cursor.getBlob(idx);
            $closeResource(null, cursor);
            return blob;
        }
    }

    private long setBytes(int userId, int uid, String rootAlias, String key, byte[] value) {
        String rootAlias2 = this.mTestOnlyInsecureCertificateHelper.getDefaultCertificateAliasIfEmpty(rootAlias);
        SQLiteDatabase db = this.mKeyStoreDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(key, value);
        String[] selectionArguments = {Integer.toString(userId), Integer.toString(uid), rootAlias2};
        ensureRootOfTrustEntryExists(userId, uid, rootAlias2);
        return (long) db.update("root_of_trust", values, "user_id = ? AND uid = ? AND root_alias = ?", selectionArguments);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x00a5, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x00a7, code lost:
        if (r4 != null) goto L_0x00a9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x00a9, code lost:
        $closeResource(r0, r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x00ac, code lost:
        throw r0;
     */
    private Long getLong(int userId, int uid, String rootAlias, String key) {
        Cursor cursor = this.mKeyStoreDbHelper.getReadableDatabase().query("root_of_trust", new String[]{"_id", "user_id", WatchlistLoggingHandler.WatchlistEventKeys.UID, "root_alias", key}, "user_id = ? AND uid = ? AND root_alias = ?", new String[]{Integer.toString(userId), Integer.toString(uid), this.mTestOnlyInsecureCertificateHelper.getDefaultCertificateAliasIfEmpty(rootAlias)}, null, null, null);
        int count = cursor.getCount();
        if (count == 0) {
            $closeResource(null, cursor);
            return null;
        } else if (count > 1) {
            Log.wtf(TAG, String.format(Locale.US, "%d entries found for userId=%d uid=%d. Should only ever be 0 or 1.", Integer.valueOf(count), Integer.valueOf(userId), Integer.valueOf(uid)));
            $closeResource(null, cursor);
            return null;
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndexOrThrow(key);
            if (cursor.isNull(idx)) {
                $closeResource(null, cursor);
                return null;
            }
            Long valueOf = Long.valueOf(cursor.getLong(idx));
            $closeResource(null, cursor);
            return valueOf;
        }
    }

    private long setLong(int userId, int uid, String rootAlias, String key, long value) {
        String rootAlias2 = this.mTestOnlyInsecureCertificateHelper.getDefaultCertificateAliasIfEmpty(rootAlias);
        SQLiteDatabase db = this.mKeyStoreDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(key, Long.valueOf(value));
        String[] selectionArguments = {Integer.toString(userId), Integer.toString(uid), rootAlias2};
        ensureRootOfTrustEntryExists(userId, uid, rootAlias2);
        return (long) db.update("root_of_trust", values, "user_id = ? AND uid = ? AND root_alias = ?", selectionArguments);
    }

    public void removeUserFromAllTables(int userId) {
        removeUserFromKeysTable(userId);
        removeUserFromUserMetadataTable(userId);
        removeUserFromRecoveryServiceMetadataTable(userId);
        removeUserFromRootOfTrustTable(userId);
    }

    private boolean removeUserFromKeysTable(int userId) {
        return this.mKeyStoreDbHelper.getWritableDatabase().delete("keys", "user_id = ?", new String[]{Integer.toString(userId)}) > 0;
    }

    private boolean removeUserFromUserMetadataTable(int userId) {
        return this.mKeyStoreDbHelper.getWritableDatabase().delete("user_metadata", "user_id = ?", new String[]{Integer.toString(userId)}) > 0;
    }

    private boolean removeUserFromRecoveryServiceMetadataTable(int userId) {
        return this.mKeyStoreDbHelper.getWritableDatabase().delete("recovery_service_metadata", "user_id = ?", new String[]{Integer.toString(userId)}) > 0;
    }

    private boolean removeUserFromRootOfTrustTable(int userId) {
        return this.mKeyStoreDbHelper.getWritableDatabase().delete("root_of_trust", "user_id = ?", new String[]{Integer.toString(userId)}) > 0;
    }

    private void ensureRecoveryServiceMetadataEntryExists(int userId, int uid) {
        SQLiteDatabase db = this.mKeyStoreDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", Integer.valueOf(userId));
        values.put(WatchlistLoggingHandler.WatchlistEventKeys.UID, Integer.valueOf(uid));
        db.insertWithOnConflict("recovery_service_metadata", null, values, 4);
    }

    private void ensureRootOfTrustEntryExists(int userId, int uid, String rootAlias) {
        SQLiteDatabase db = this.mKeyStoreDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", Integer.valueOf(userId));
        values.put(WatchlistLoggingHandler.WatchlistEventKeys.UID, Integer.valueOf(uid));
        values.put("root_alias", rootAlias);
        db.insertWithOnConflict("root_of_trust", null, values, 4);
    }

    private void ensureUserMetadataEntryExists(int userId) {
        SQLiteDatabase db = this.mKeyStoreDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", Integer.valueOf(userId));
        db.insertWithOnConflict("user_metadata", null, values, 4);
    }

    public void close() {
        this.mKeyStoreDbHelper.close();
    }

    private static PublicKey decodeX509Key(byte[] keyBytes) throws InvalidKeySpecException {
        try {
            return KeyFactory.getInstance("EC").generatePublic(new X509EncodedKeySpec(keyBytes));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static CertPath decodeCertPath(byte[] bytes) throws CertificateException {
        try {
            return CertificateFactory.getInstance("X.509").generateCertPath(new ByteArrayInputStream(bytes), CERT_PATH_ENCODING);
        } catch (CertificateException e) {
            throw new RuntimeException(e);
        }
    }
}
