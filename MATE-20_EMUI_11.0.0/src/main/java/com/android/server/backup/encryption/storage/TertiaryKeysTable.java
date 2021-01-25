package com.android.server.backup.encryption.storage;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.ArrayMap;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class TertiaryKeysTable {
    private final BackupEncryptionDbHelper mHelper;

    TertiaryKeysTable(BackupEncryptionDbHelper helper) {
        this.mHelper = helper;
    }

    public long addKey(TertiaryKey tertiaryKey) throws EncryptionDbException {
        SQLiteDatabase db = this.mHelper.getWritableDatabaseSafe();
        ContentValues values = new ContentValues();
        values.put("secondary_key_alias", tertiaryKey.getSecondaryKeyAlias());
        values.put("package_name", tertiaryKey.getPackageName());
        values.put("wrapped_key_bytes", tertiaryKey.getWrappedKeyBytes());
        return db.replace("tertiary_keys", null, values);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0059, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x005a, code lost:
        if (r1 != null) goto L_0x005c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x005c, code lost:
        $closeResource(r2, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x005f, code lost:
        throw r4;
     */
    public Optional<TertiaryKey> getKey(String secondaryKeyAlias, String packageName) throws EncryptionDbException {
        Cursor cursor = this.mHelper.getReadableDatabaseSafe().query("tertiary_keys", new String[]{"_id", "secondary_key_alias", "package_name", "wrapped_key_bytes"}, "secondary_key_alias = ? AND package_name = ?", new String[]{secondaryKeyAlias, packageName}, null, null, null);
        if (cursor.getCount() == 0) {
            Optional<TertiaryKey> empty = Optional.empty();
            $closeResource(null, cursor);
            return empty;
        }
        cursor.moveToFirst();
        Optional<TertiaryKey> of = Optional.of(new TertiaryKey(secondaryKeyAlias, packageName, cursor.getBlob(cursor.getColumnIndexOrThrow("wrapped_key_bytes"))));
        $closeResource(null, cursor);
        return of;
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

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x005f, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0060, code lost:
        if (r1 != null) goto L_0x0062;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0062, code lost:
        $closeResource(r2, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0065, code lost:
        throw r4;
     */
    public Map<String, TertiaryKey> getAllKeys(String secondaryKeyAlias) throws EncryptionDbException {
        Map<String, TertiaryKey> keysByPackageName = new ArrayMap<>();
        Cursor cursor = this.mHelper.getReadableDatabaseSafe().query("tertiary_keys", new String[]{"_id", "secondary_key_alias", "package_name", "wrapped_key_bytes"}, "secondary_key_alias = ?", new String[]{secondaryKeyAlias}, null, null, null);
        while (cursor.moveToNext()) {
            String packageName = cursor.getString(cursor.getColumnIndexOrThrow("package_name"));
            keysByPackageName.put(packageName, new TertiaryKey(secondaryKeyAlias, packageName, cursor.getBlob(cursor.getColumnIndexOrThrow("wrapped_key_bytes"))));
        }
        $closeResource(null, cursor);
        return Collections.unmodifiableMap(keysByPackageName);
    }
}
