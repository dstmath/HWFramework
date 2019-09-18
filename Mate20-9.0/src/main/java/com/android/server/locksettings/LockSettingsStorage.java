package com.android.server.locksettings;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.UserInfo;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.os.UserManager;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Slog;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.Preconditions;
import com.android.server.LocalServices;
import com.android.server.PersistentDataBlockManagerInternal;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class LockSettingsStorage {
    private static final String BASE_ZERO_LOCK_PATTERN_FILE = "gatekeeper.gesture.key";
    private static final String CHILD_PROFILE_LOCK_FILE = "gatekeeper.profile.key";
    private static final String[] COLUMNS_FOR_PREFETCH = {"name", COLUMN_VALUE};
    private static final String[] COLUMNS_FOR_QUERY = {COLUMN_VALUE};
    private static final String COLUMN_KEY = "name";
    private static final String COLUMN_USERID = "user";
    private static final String COLUMN_VALUE = "value";
    private static final boolean DEBUG = false;
    /* access modifiers changed from: private */
    public static final Object DEFAULT = new Object();
    private static final String LEGACY_LOCK_PASSWORD_FILE = "password.key";
    private static final String LEGACY_LOCK_PATTERN_FILE = "gesture.key";
    private static final String LOCK_PASSWORD_FILE = "gatekeeper.password.key";
    private static final String LOCK_PATTERN_FILE = "gatekeeper.pattern.key";
    private static final String SYNTHETIC_PASSWORD_DIRECTORY = "spblob/";
    private static final String SYSTEM_DIRECTORY = "/system/";
    private static final String TABLE = "locksettings";
    private static final String TAG = "LockSettingsStorage";
    private final Cache mCache = new Cache();
    private final Context mContext;
    private final Object mFileWriteLock = new Object();
    private final DatabaseHelper mOpenHelper;
    private PersistentDataBlockManagerInternal mPersistentDataBlockManagerInternal;

    private static class Cache {
        private final ArrayMap<CacheKey, Object> mCache;
        private final CacheKey mCacheKey;
        private int mVersion;

        private static final class CacheKey {
            static final int TYPE_FETCHED = 2;
            static final int TYPE_FILE = 1;
            static final int TYPE_KEY_VALUE = 0;
            String key;
            int type;
            int userId;

            private CacheKey() {
            }

            public CacheKey set(int type2, String key2, int userId2) {
                this.type = type2;
                this.key = key2;
                this.userId = userId2;
                return this;
            }

            public boolean equals(Object obj) {
                boolean z = false;
                if (!(obj instanceof CacheKey)) {
                    return false;
                }
                CacheKey o = (CacheKey) obj;
                if (this.userId == o.userId && this.type == o.type && this.key.equals(o.key)) {
                    z = true;
                }
                return z;
            }

            public int hashCode() {
                return (this.key.hashCode() ^ this.userId) ^ this.type;
            }
        }

        private Cache() {
            this.mCache = new ArrayMap<>();
            this.mCacheKey = new CacheKey();
            this.mVersion = 0;
        }

        /* access modifiers changed from: package-private */
        public String peekKeyValue(String key, String defaultValue, int userId) {
            Object cached = peek(0, key, userId);
            return cached == LockSettingsStorage.DEFAULT ? defaultValue : (String) cached;
        }

        /* access modifiers changed from: package-private */
        public boolean hasKeyValue(String key, int userId) {
            return contains(0, key, userId);
        }

        /* access modifiers changed from: package-private */
        public void putKeyValue(String key, String value, int userId) {
            put(0, key, value, userId);
        }

        /* access modifiers changed from: package-private */
        public void putKeyValueIfUnchanged(String key, Object value, int userId, int version) {
            putIfUnchanged(0, key, value, userId, version);
        }

        /* access modifiers changed from: package-private */
        public byte[] peekFile(String fileName) {
            return (byte[]) peek(1, fileName, -1);
        }

        /* access modifiers changed from: package-private */
        public boolean hasFile(String fileName) {
            return contains(1, fileName, -1);
        }

        /* access modifiers changed from: package-private */
        public void putFile(String key, byte[] value) {
            put(1, key, value, -1);
        }

        /* access modifiers changed from: package-private */
        public void putFileIfUnchanged(String key, byte[] value, int version) {
            putIfUnchanged(1, key, value, -1, version);
        }

        /* access modifiers changed from: package-private */
        public void setFetched(int userId) {
            put(2, "isFetched", "true", userId);
        }

        /* access modifiers changed from: package-private */
        public boolean isFetched(int userId) {
            return contains(2, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS, userId);
        }

        private synchronized void put(int type, String key, Object value, int userId) {
            this.mCache.put(new CacheKey().set(type, key, userId), value);
            this.mVersion++;
        }

        private synchronized void putIfUnchanged(int type, String key, Object value, int userId, int version) {
            if (!contains(type, key, userId) && this.mVersion == version) {
                put(type, key, value, userId);
            }
        }

        private synchronized boolean contains(int type, String key, int userId) {
            return this.mCache.containsKey(this.mCacheKey.set(type, key, userId));
        }

        private synchronized Object peek(int type, String key, int userId) {
            return this.mCache.get(this.mCacheKey.set(type, key, userId));
        }

        /* access modifiers changed from: private */
        public synchronized int getVersion() {
            return this.mVersion;
        }

        /* access modifiers changed from: package-private */
        public synchronized void removeUser(int userId) {
            for (int i = this.mCache.size() - 1; i >= 0; i--) {
                if (this.mCache.keyAt(i).userId == userId) {
                    this.mCache.removeAt(i);
                }
            }
            this.mVersion++;
        }

        /* access modifiers changed from: package-private */
        public synchronized void purgePath(String path) {
            for (int i = this.mCache.size() - 1; i >= 0; i--) {
                CacheKey entry = this.mCache.keyAt(i);
                if (entry.type == 1 && entry.key.startsWith(path)) {
                    this.mCache.removeAt(i);
                }
            }
            this.mVersion++;
        }

        /* access modifiers changed from: package-private */
        public synchronized void clear() {
            this.mCache.clear();
            this.mVersion++;
        }
    }

    public interface Callback {
        void initialize(SQLiteDatabase sQLiteDatabase);
    }

    @VisibleForTesting
    public static class CredentialHash {
        static final int VERSION_GATEKEEPER = 1;
        static final int VERSION_LEGACY = 0;
        byte[] hash;
        boolean isBaseZeroPattern;
        int type;
        int version;

        private CredentialHash(byte[] hash2, int type2, int version2) {
            this(hash2, type2, version2, false);
        }

        private CredentialHash(byte[] hash2, int type2, int version2, boolean isBaseZeroPattern2) {
            if (type2 != -1) {
                if (hash2 == null) {
                    throw new RuntimeException("Empty hash for CredentialHash");
                }
            } else if (hash2 != null) {
                throw new RuntimeException("None type CredentialHash should not have hash");
            }
            this.hash = hash2;
            this.type = type2;
            this.version = version2;
            this.isBaseZeroPattern = isBaseZeroPattern2;
        }

        /* access modifiers changed from: private */
        public static CredentialHash createBaseZeroPattern(byte[] hash2) {
            return new CredentialHash(hash2, 1, 1, true);
        }

        static CredentialHash create(byte[] hash2, int type2) {
            if (type2 != -1) {
                return new CredentialHash(hash2, type2, 1);
            }
            throw new RuntimeException("Bad type for CredentialHash");
        }

        static CredentialHash createEmptyHash() {
            return new CredentialHash(null, -1, 1);
        }

        public byte[] toBytes() {
            Preconditions.checkState(!this.isBaseZeroPattern, "base zero patterns are not serializable");
            try {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(os);
                dos.write(this.version);
                dos.write(this.type);
                if (this.hash == null || this.hash.length <= 0) {
                    dos.writeInt(0);
                } else {
                    dos.writeInt(this.hash.length);
                    dos.write(this.hash);
                }
                dos.close();
                return os.toByteArray();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public static CredentialHash fromBytes(byte[] bytes) {
            try {
                DataInputStream is = new DataInputStream(new ByteArrayInputStream(bytes));
                int version2 = is.read();
                int type2 = is.read();
                int hashSize = is.readInt();
                byte[] hash2 = null;
                if (hashSize > 0) {
                    hash2 = new byte[hashSize];
                    is.readFully(hash2);
                }
                return new CredentialHash(hash2, type2, version2);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    static class DatabaseHelper extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "locksettings.db";
        private static final int DATABASE_VERSION = 2;
        private static final int IDLE_CONNECTION_TIMEOUT_MS = 30000;
        private static final String TAG = "LockSettingsDB";
        private Callback mCallback;
        private ILogRecorder mLogger;

        public DatabaseHelper(Context context, ILogRecorder logger) {
            super(context, DATABASE_NAME, null, 2);
            this.mLogger = logger;
            setWriteAheadLoggingEnabled(true);
            setIdleConnectionTimeout(30000);
        }

        public void setCallback(Callback callback) {
            this.mCallback = callback;
        }

        private void createTable(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE locksettings (_id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT,user INTEGER,value TEXT);");
        }

        public void onOpen(SQLiteDatabase db) {
            super.onOpen(db);
            if (this.mLogger != null) {
                this.mLogger.syncDataToXml(db);
                this.mLogger.doLog(TAG, "OpenDatabase ");
            }
        }

        public void onCreate(SQLiteDatabase db) {
            createTable(db);
            if (this.mCallback != null) {
                this.mCallback.initialize(db);
            }
            if (this.mLogger != null) {
                this.mLogger.doLog(TAG, "CreateDatabase !!!");
                this.mLogger.restoreDbData(db);
            }
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int currentVersion) {
            int upgradeVersion = oldVersion;
            if (upgradeVersion == 1) {
                upgradeVersion = 2;
            }
            if (upgradeVersion != 2) {
                Log.w(TAG, "Failed to upgrade database!");
            }
            if (this.mLogger != null) {
                ILogRecorder iLogRecorder = this.mLogger;
                iLogRecorder.doLog(TAG, "UpdateDatabase locksettings.db from " + oldVersion + " to " + upgradeVersion);
            }
        }
    }

    private interface ILogRecorder {
        void doLog(String str, String str2);

        void restoreDbData(SQLiteDatabase sQLiteDatabase);

        void syncDataToXml(SQLiteDatabase sQLiteDatabase);
    }

    public static class PersistentData {
        public static final PersistentData NONE = new PersistentData(0, -10000, 0, null);
        public static final int TYPE_NONE = 0;
        public static final int TYPE_SP = 1;
        public static final int TYPE_SP_WEAVER = 2;
        static final byte VERSION_1 = 1;
        static final int VERSION_1_HEADER_SIZE = 10;
        final byte[] payload;
        final int qualityForUi;
        final int type;
        final int userId;

        private PersistentData(int type2, int userId2, int qualityForUi2, byte[] payload2) {
            this.type = type2;
            this.userId = userId2;
            this.qualityForUi = qualityForUi2;
            this.payload = payload2;
        }

        public static PersistentData fromBytes(byte[] frpData) {
            if (frpData == null || frpData.length == 0) {
                return NONE;
            }
            DataInputStream is = new DataInputStream(new ByteArrayInputStream(frpData));
            try {
                byte version = is.readByte();
                if (version == 1) {
                    int userId2 = is.readInt();
                    int qualityForUi2 = is.readInt();
                    byte[] payload2 = new byte[(frpData.length - 10)];
                    System.arraycopy(frpData, 10, payload2, 0, payload2.length);
                    return new PersistentData(is.readByte() & 255, userId2, qualityForUi2, payload2);
                }
                Slog.wtf(LockSettingsStorage.TAG, "Unknown PersistentData version code: " + version);
                return NONE;
            } catch (IOException e) {
                Slog.wtf(LockSettingsStorage.TAG, "Could not parse PersistentData", e);
                return NONE;
            }
        }

        public static byte[] toBytes(int persistentType, int userId2, int qualityForUi2, byte[] payload2) {
            boolean z = false;
            if (persistentType == 0) {
                if (payload2 == null) {
                    z = true;
                }
                Preconditions.checkArgument(z, "TYPE_NONE must have empty payload");
                return null;
            }
            if (payload2 != null && payload2.length > 0) {
                z = true;
            }
            Preconditions.checkArgument(z, "empty payload must only be used with TYPE_NONE");
            ByteArrayOutputStream os = new ByteArrayOutputStream(10 + payload2.length);
            DataOutputStream dos = new DataOutputStream(os);
            try {
                dos.writeByte(1);
                dos.writeByte(persistentType);
                dos.writeInt(userId2);
                dos.writeInt(qualityForUi2);
                dos.write(payload2);
                return os.toByteArray();
            } catch (IOException e) {
                throw new RuntimeException("ByteArrayOutputStream cannot throw IOException");
            }
        }
    }

    public LockSettingsStorage(Context context) {
        this.mContext = context;
        this.mOpenHelper = new DatabaseHelper(context, new ILogRecorder() {
            public void doLog(String tag, String msg) {
                LockSettingsStorage.this.flog(tag, msg);
            }

            public void restoreDbData(SQLiteDatabase db) {
                LockSettingsStorage.this.restoreDataFromXml(db);
            }

            public void syncDataToXml(SQLiteDatabase db) {
                LockSettingsStorage.this.syncDataToXmlFile(db);
            }
        });
    }

    public void setDatabaseOnCreateCallback(Callback callback) {
        this.mOpenHelper.setCallback(callback);
    }

    public void writeKeyValue(String key, String value, int userId) {
        writeKeyValue(this.mOpenHelper.getWritableDatabase(), key, value, userId);
    }

    public void writeKeyValue(SQLiteDatabase db, String key, String value, int userId) {
        ContentValues cv = new ContentValues();
        cv.put("name", key);
        cv.put(COLUMN_USERID, Integer.valueOf(userId));
        cv.put(COLUMN_VALUE, value);
        db.beginTransaction();
        try {
            db.delete(TABLE, "name=? AND user=?", new String[]{key, Integer.toString(userId)});
            db.insert(TABLE, null, cv);
            db.setTransactionSuccessful();
            this.mCache.putKeyValue(key, value, userId);
        } finally {
            db.endTransaction();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x001a, code lost:
        r0 = DEFAULT;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:?, code lost:
        r2 = r12.mOpenHelper.getReadableDatabase().query(TABLE, COLUMNS_FOR_QUERY, "user=? AND name=?", new java.lang.String[]{java.lang.Integer.toString(r15), r13}, null, null, null);
        r4 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x003f, code lost:
        if (r2 == null) goto L_0x0059;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0045, code lost:
        if (r4.moveToFirst() == false) goto L_0x004c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0047, code lost:
        r0 = r4.getString(0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x004c, code lost:
        r4.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0050, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0051, code lost:
        android.util.Log.w(TAG, "readKeyValue got err:", r2);
     */
    public String readKeyValue(String key, String defaultValue, int userId) {
        int version;
        Object result;
        synchronized (this.mCache) {
            if (this.mCache.hasKeyValue(key, userId)) {
                String peekKeyValue = this.mCache.peekKeyValue(key, defaultValue, userId);
                return peekKeyValue;
            }
            version = this.mCache.getVersion();
        }
        this.mCache.putKeyValueIfUnchanged(key, result, userId, version);
        return result == DEFAULT ? defaultValue : result;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:?, code lost:
        r3 = r11.mOpenHelper.getReadableDatabase().query(TABLE, COLUMNS_FOR_PREFETCH, "user=?", new java.lang.String[]{java.lang.Integer.toString(r12)}, null, null, null);
        r4 = r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0039, code lost:
        if (r3 == null) goto L_0x005c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x003f, code lost:
        if (r4.moveToNext() == false) goto L_0x004f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0041, code lost:
        r11.mCache.putKeyValueIfUnchanged(r4.getString(0), r4.getString(1), r12, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x004f, code lost:
        r4.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0053, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0054, code lost:
        android.util.Log.w(TAG, "prefetchUser got err:", r0);
     */
    public void prefetchUser(int userId) {
        synchronized (this.mCache) {
            if (!this.mCache.isFetched(userId)) {
                this.mCache.setFetched(userId);
                int version = this.mCache.getVersion();
            } else {
                return;
            }
        }
        readCredentialHash(userId);
    }

    private CredentialHash readPasswordHashIfExists(int userId) {
        byte[] stored = readFile(getLockPasswordFilename(userId));
        if (!ArrayUtils.isEmpty(stored)) {
            return new CredentialHash(stored, 2, 1);
        }
        byte[] stored2 = readFile(getLegacyLockPasswordFilename(userId));
        if (!ArrayUtils.isEmpty(stored2)) {
            return new CredentialHash(stored2, 2, 0);
        }
        Log.i(TAG, "readPatternHash , cannot get any PasswordHash");
        return null;
    }

    private CredentialHash readPatternHashIfExists(int userId) {
        byte[] stored = readFile(getLockPatternFilename(userId));
        if (!ArrayUtils.isEmpty(stored)) {
            return new CredentialHash(stored, 1, 1);
        }
        byte[] stored2 = readFile(getBaseZeroLockPatternFilename(userId));
        if (!ArrayUtils.isEmpty(stored2)) {
            return CredentialHash.createBaseZeroPattern(stored2);
        }
        byte[] stored3 = readFile(getLegacyLockPatternFilename(userId));
        if (!ArrayUtils.isEmpty(stored3)) {
            return new CredentialHash(stored3, 1, 0);
        }
        Log.i(TAG, "readPatternHash , cannot get any PatternHash");
        return null;
    }

    public CredentialHash readCredentialHash(int userId) {
        CredentialHash passwordHash = readPasswordHashIfExists(userId);
        CredentialHash patternHash = readPatternHashIfExists(userId);
        if (passwordHash == null || patternHash == null) {
            if (passwordHash != null) {
                return passwordHash;
            }
            if (patternHash != null) {
                return patternHash;
            }
            return CredentialHash.createEmptyHash();
        } else if (passwordHash.version == 1) {
            return passwordHash;
        } else {
            return patternHash;
        }
    }

    public void removeChildProfileLock(int userId) {
        try {
            deleteFile(getChildProfileLockFile(userId));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void writeChildProfileLock(int userId, byte[] lock) {
        writeFile(getChildProfileLockFile(userId), lock);
    }

    public byte[] readChildProfileLock(int userId) {
        return readFile(getChildProfileLockFile(userId));
    }

    public boolean hasChildProfileLock(int userId) {
        return hasFile(getChildProfileLockFile(userId));
    }

    public boolean hasPassword(int userId) {
        return hasFile(getLockPasswordFilename(userId)) || hasFile(getLegacyLockPasswordFilename(userId));
    }

    public boolean hasPattern(int userId) {
        return hasFile(getLockPatternFilename(userId)) || hasFile(getBaseZeroLockPatternFilename(userId)) || hasFile(getLegacyLockPatternFilename(userId));
    }

    public boolean hasCredential(int userId) {
        return hasPassword(userId) || hasPattern(userId);
    }

    /* access modifiers changed from: protected */
    public boolean hasFile(String name) {
        byte[] contents = readFile(name);
        return contents != null && contents.length > 0;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0028, code lost:
        r0 = null;
        r2 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:?, code lost:
        r0 = new java.io.RandomAccessFile(r8, "r");
        r2 = new byte[((int) r0.length())];
        r0.readFully(r2, 0, r2.length);
        r0.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:?, code lost:
        r0.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0048, code lost:
        r3 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0049, code lost:
        r4 = TAG;
        r5 = new java.lang.StringBuilder();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0060, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0062, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:?, code lost:
        android.util.Slog.e(TAG, "Cannot read file " + r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0079, code lost:
        if (r0 != null) goto L_0x007b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:?, code lost:
        r0.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x007f, code lost:
        r3 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0080, code lost:
        r4 = TAG;
        r5 = new java.lang.StringBuilder();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x008e, code lost:
        dumpFileInfo(r8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0097, code lost:
        if (r0 != null) goto L_0x0099;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:?, code lost:
        r0.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x009d, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x009e, code lost:
        android.util.Slog.e(TAG, "Error closing file " + r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00b4, code lost:
        throw r3;
     */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x008e  */
    public byte[] readFile(String name) {
        int version;
        byte[] stored;
        String str;
        StringBuilder sb;
        synchronized (this.mCache) {
            if (this.mCache.hasFile(name)) {
                byte[] cached = this.mCache.peekFile(name);
                if (!ArrayUtils.isEmpty(cached)) {
                    return cached;
                }
                Slog.e(TAG, "read file from cache is empty.");
            }
            version = this.mCache.getVersion();
        }
        sb.append("Error closing file ");
        sb.append(e);
        Slog.e(str, sb.toString());
        if (ArrayUtils.isEmpty(stored)) {
        }
        this.mCache.putFileIfUnchanged(name, stored, version);
        return stored;
    }

    private void dumpFileInfo(String name) {
        File f = new File(name);
        if (f.exists() && f.isFile()) {
            Slog.e(TAG, "size of file:" + name + " = " + f.length() + "; readable: " + f.canRead() + "; last modified " + f.lastModified());
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0026, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0027, code lost:
        r3 = TAG;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:?, code lost:
        r4 = "Error closing file " + r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x003e, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0079, code lost:
        if (r1 != null) goto L_0x007b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:?, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0081, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:?, code lost:
        android.util.Slog.e(TAG, "Error closing file " + r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x0098, code lost:
        throw r2;
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [B:12:0x0022, B:22:0x0043] */
    public void writeFile(String name, byte[] hash) {
        synchronized (this.mFileWriteLock) {
            RandomAccessFile raf = null;
            try {
                RandomAccessFile raf2 = new RandomAccessFile(name, "rws");
                if (hash != null) {
                    if (hash.length != 0) {
                        raf2.write(hash, 0, hash.length);
                        raf2.close();
                        raf2.close();
                        this.mCache.putFile(name, hash);
                    }
                }
                raf2.setLength(0);
                raf2.close();
                raf2.close();
            } catch (IOException e) {
                Slog.e(TAG, "Error writing to file " + e);
                if (raf != null) {
                    try {
                        raf.close();
                    } catch (IOException e2) {
                        String str = TAG;
                        String str2 = "Error closing file " + e2;
                        Slog.e(str, str2);
                        this.mCache.putFile(name, hash);
                    }
                }
            }
            this.mCache.putFile(name, hash);
        }
    }

    /* access modifiers changed from: protected */
    public void deleteFile(String name) {
        synchronized (this.mFileWriteLock) {
            File file = new File(name);
            if (file.exists()) {
                file.delete();
                this.mCache.putFile(name, null);
            }
        }
    }

    public void writeCredentialHash(CredentialHash hash, int userId) {
        byte[] patternHash = null;
        byte[] passwordHash = null;
        if (hash.type == 2) {
            passwordHash = hash.hash;
        } else if (hash.type == 1) {
            patternHash = hash.hash;
        }
        writeFile(getLockPasswordFilename(userId), passwordHash);
        writeFile(getLockPatternFilename(userId), patternHash);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public String getLockPatternFilename(int userId) {
        return getLockCredentialFilePathForUser(userId, LOCK_PATTERN_FILE);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public String getLockPasswordFilename(int userId) {
        return getLockCredentialFilePathForUser(userId, LOCK_PASSWORD_FILE);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public String getLegacyLockPatternFilename(int userId) {
        return getLockCredentialFilePathForUser(userId, LEGACY_LOCK_PATTERN_FILE);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public String getLegacyLockPasswordFilename(int userId) {
        return getLockCredentialFilePathForUser(userId, LEGACY_LOCK_PASSWORD_FILE);
    }

    private String getBaseZeroLockPatternFilename(int userId) {
        return getLockCredentialFilePathForUser(userId, BASE_ZERO_LOCK_PATTERN_FILE);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public String getChildProfileLockFile(int userId) {
        return getLockCredentialFilePathForUser(userId, CHILD_PROFILE_LOCK_FILE);
    }

    /* access modifiers changed from: protected */
    public String getLockCredentialFilePathForUser(int userId, String basename) {
        String dataSystemDirectory = Environment.getDataDirectory().getAbsolutePath() + "/system/";
        if (userId != 0) {
            return new File(Environment.getUserSystemDirectory(userId), basename).getAbsolutePath();
        }
        return dataSystemDirectory + basename;
    }

    public void writeSyntheticPasswordState(int userId, long handle, String name, byte[] data) {
        ensureSyntheticPasswordDirectoryForUser(userId);
        writeFile(getSynthenticPasswordStateFilePathForUser(userId, handle, name), data);
    }

    public byte[] readSyntheticPasswordState(int userId, long handle, String name) {
        return readFile(getSynthenticPasswordStateFilePathForUser(userId, handle, name));
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0027, code lost:
        r5 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x002b, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x002c, code lost:
        r7 = r5;
        r5 = r4;
        r4 = r7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0026, code lost:
        r4 = th;
     */
    public void deleteSyntheticPasswordState(int userId, long handle, String name) {
        RandomAccessFile raf;
        Throwable th;
        Throwable th2;
        String path = getSynthenticPasswordStateFilePathForUser(userId, handle, name);
        File file = new File(path);
        if (file.exists()) {
            try {
                raf = new RandomAccessFile(path, "rws");
                raf.write(new byte[((int) raf.length())]);
                raf.close();
            } catch (Exception e) {
                Slog.w(TAG, "Failed to zeroize " + path, e);
            } catch (Throwable th3) {
                file.delete();
                throw th3;
            }
            file.delete();
            this.mCache.putFile(path, null);
            return;
        }
        return;
        throw th2;
        if (th != null) {
            try {
                raf.close();
            } catch (Throwable th4) {
                th.addSuppressed(th4);
            }
        } else {
            raf.close();
        }
        throw th2;
    }

    public Map<Integer, List<Long>> listSyntheticPasswordHandlesForAllUsers(String stateName) {
        Map<Integer, List<Long>> result = new ArrayMap<>();
        for (UserInfo user : UserManager.get(this.mContext).getUsers(false)) {
            result.put(Integer.valueOf(user.id), listSyntheticPasswordHandlesForUser(stateName, user.id));
        }
        return result;
    }

    public List<Long> listSyntheticPasswordHandlesForUser(String stateName, int userId) {
        File baseDir = getSyntheticPasswordDirectoryForUser(userId);
        List<Long> result = new ArrayList<>();
        File[] files = baseDir.listFiles();
        if (files == null) {
            return result;
        }
        for (File file : files) {
            String[] parts = file.getName().split("\\.");
            if (parts.length == 2 && parts[1].equals(stateName)) {
                try {
                    result.add(Long.valueOf(Long.parseUnsignedLong(parts[0], 16)));
                } catch (NumberFormatException e) {
                    Slog.e(TAG, "Failed to parse handle " + parts[0]);
                }
            }
        }
        return result;
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public File getSyntheticPasswordDirectoryForUser(int userId) {
        return new File(Environment.getDataSystemDeDirectory(userId), SYNTHETIC_PASSWORD_DIRECTORY);
    }

    private void ensureSyntheticPasswordDirectoryForUser(int userId) {
        File baseDir = getSyntheticPasswordDirectoryForUser(userId);
        if (baseDir.exists()) {
            return;
        }
        if (userId == 2147483646) {
            Log.w(TAG, "Parentcontrol doesn't have userinfo, using mkdirs instead!");
            baseDir.mkdirs();
            return;
        }
        baseDir.mkdir();
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public String getSynthenticPasswordStateFilePathForUser(int userId, long handle, String name) {
        return new File(getSyntheticPasswordDirectoryForUser(userId), String.format("%016x.%s", new Object[]{Long.valueOf(handle), name})).getAbsolutePath();
    }

    public void removeUser(int userId) {
        SQLiteDatabase db = this.mOpenHelper.getWritableDatabase();
        if (((UserManager) this.mContext.getSystemService(COLUMN_USERID)).getProfileParent(userId) == null) {
            synchronized (this.mFileWriteLock) {
                String name = getLockPasswordFilename(userId);
                File file = new File(name);
                if (file.exists()) {
                    file.delete();
                    this.mCache.putFile(name, null);
                }
                String name2 = getLockPatternFilename(userId);
                File file2 = new File(name2);
                if (file2.exists()) {
                    file2.delete();
                    this.mCache.putFile(name2, null);
                }
            }
        } else {
            removeChildProfileLock(userId);
        }
        File spStateDir = getSyntheticPasswordDirectoryForUser(userId);
        try {
            db.beginTransaction();
            db.delete(TABLE, "user='" + userId + "'", null);
            db.setTransactionSuccessful();
            this.mCache.removeUser(userId);
            this.mCache.purgePath(spStateDir.getAbsolutePath());
        } finally {
            db.endTransaction();
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void closeDatabase() {
        this.mOpenHelper.close();
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void clearCache() {
        this.mCache.clear();
    }

    public PersistentDataBlockManagerInternal getPersistentDataBlock() {
        if (this.mPersistentDataBlockManagerInternal == null) {
            this.mPersistentDataBlockManagerInternal = (PersistentDataBlockManagerInternal) LocalServices.getService(PersistentDataBlockManagerInternal.class);
        }
        return this.mPersistentDataBlockManagerInternal;
    }

    public void writePersistentDataBlock(int persistentType, int userId, int qualityForUi, byte[] payload) {
        PersistentDataBlockManagerInternal persistentDataBlock = getPersistentDataBlock();
        if (persistentDataBlock != null) {
            persistentDataBlock.setFrpCredentialHandle(PersistentData.toBytes(persistentType, userId, qualityForUi, payload));
        }
    }

    public PersistentData readPersistentDataBlock() {
        PersistentDataBlockManagerInternal persistentDataBlock = getPersistentDataBlock();
        if (persistentDataBlock == null) {
            return PersistentData.NONE;
        }
        try {
            return PersistentData.fromBytes(persistentDataBlock.getFrpCredentialHandle());
        } catch (IllegalStateException e) {
            Slog.e(TAG, "Error reading persistent data block", e);
            return PersistentData.NONE;
        }
    }

    /* access modifiers changed from: protected */
    public String getLockCredentialFilePathForUser2(int userId, String basename) {
        return getLockCredentialFilePathForUser(userId, basename);
    }

    /* access modifiers changed from: package-private */
    public CredentialHash readCredentialHashEx(int userId) {
        return null;
    }

    /* access modifiers changed from: package-private */
    public void writeCredentialHashEx(CredentialHash hash, int userId) {
    }

    /* access modifiers changed from: package-private */
    public void deleteExPasswordFile(int userId) {
    }

    /* access modifiers changed from: package-private */
    public boolean hasSetPassword(int userId) {
        return false;
    }

    /* access modifiers changed from: package-private */
    public void flog(String tag, String msg) {
    }

    /* access modifiers changed from: package-private */
    public void restoreDataFromXml(SQLiteDatabase db) {
    }

    /* access modifiers changed from: package-private */
    public void syncDataToXmlFile(SQLiteDatabase db) {
    }
}
