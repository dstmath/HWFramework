package com.android.server;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.os.SystemProperties;
import android.os.UserManager;
import android.os.storage.StorageManager;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Slog;
import com.android.internal.util.ArrayUtils;
import com.android.internal.widget.LockPatternUtils;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

class LockSettingsStorage {
    private static final String BASE_ZERO_LOCK_PATTERN_FILE = "gatekeeper.gesture.key";
    private static final String CHILD_PROFILE_LOCK_FILE = "gatekeeper.profile.key";
    private static final String[] COLUMNS_FOR_PREFETCH = new String[]{COLUMN_KEY, COLUMN_VALUE};
    private static final String[] COLUMNS_FOR_QUERY = new String[]{COLUMN_VALUE};
    private static final String COLUMN_KEY = "name";
    private static final String COLUMN_USERID = "user";
    private static final String COLUMN_VALUE = "value";
    private static final boolean DEBUG = false;
    private static final Object DEFAULT = new Object();
    private static final boolean IS_BUCKUP_PIN_EXIST = SystemProperties.getBoolean("ro.config.hw_backupPin_exist", false);
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

    public interface Callback {
        void initialize(SQLiteDatabase sQLiteDatabase);
    }

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

            /* synthetic */ CacheKey(CacheKey -this0) {
                this();
            }

            private CacheKey() {
            }

            public CacheKey set(int type, String key, int userId) {
                this.type = type;
                this.key = key;
                this.userId = userId;
                return this;
            }

            public boolean equals(Object obj) {
                boolean z = false;
                if (!(obj instanceof CacheKey)) {
                    return false;
                }
                CacheKey o = (CacheKey) obj;
                if (this.userId == o.userId && this.type == o.type) {
                    z = this.key.equals(o.key);
                }
                return z;
            }

            public int hashCode() {
                return (this.key.hashCode() ^ this.userId) ^ this.type;
            }
        }

        /* synthetic */ Cache(Cache -this0) {
            this();
        }

        private Cache() {
            this.mCache = new ArrayMap();
            this.mCacheKey = new CacheKey();
            this.mVersion = 0;
        }

        String peekKeyValue(String key, String defaultValue, int userId) {
            Object cached = peek(0, key, userId);
            return cached == LockSettingsStorage.DEFAULT ? defaultValue : (String) cached;
        }

        boolean hasKeyValue(String key, int userId) {
            return contains(0, key, userId);
        }

        void putKeyValue(String key, String value, int userId) {
            put(0, key, value, userId);
        }

        void putKeyValueIfUnchanged(String key, Object value, int userId, int version) {
            putIfUnchanged(0, key, value, userId, version);
        }

        byte[] peekFile(String fileName) {
            return (byte[]) peek(1, fileName, -1);
        }

        boolean hasFile(String fileName) {
            return contains(1, fileName, -1);
        }

        void putFile(String key, byte[] value) {
            put(1, key, value, -1);
        }

        void putFileIfUnchanged(String key, byte[] value, int version) {
            putIfUnchanged(1, key, value, -1, version);
        }

        void setFetched(int userId) {
            put(2, "isFetched", "true", userId);
        }

        boolean isFetched(int userId) {
            return contains(2, "", userId);
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

        private synchronized int getVersion() {
            return this.mVersion;
        }

        synchronized void removeUser(int userId) {
            for (int i = this.mCache.size() - 1; i >= 0; i--) {
                if (((CacheKey) this.mCache.keyAt(i)).userId == userId) {
                    this.mCache.removeAt(i);
                }
            }
            this.mVersion++;
        }

        synchronized void purgePath(String path) {
            for (int i = this.mCache.size() - 1; i >= 0; i--) {
                CacheKey entry = (CacheKey) this.mCache.keyAt(i);
                if (entry.type == 1 && entry.key.startsWith(path)) {
                    this.mCache.removeAt(i);
                }
            }
            this.mVersion++;
        }

        synchronized void clear() {
            this.mCache.clear();
            this.mVersion++;
        }
    }

    public static class CredentialHash {
        static final int VERSION_GATEKEEPER = 1;
        static final int VERSION_LEGACY = 0;
        byte[] hash;
        boolean isBaseZeroPattern;
        int type;
        int version;

        /* synthetic */ CredentialHash(byte[] hash, int type, int version, CredentialHash -this3) {
            this(hash, type, version);
        }

        private CredentialHash(byte[] hash, int type, int version) {
            if (type != -1) {
                if (hash == null) {
                    throw new RuntimeException("Empty hash for CredentialHash");
                }
            } else if (hash != null) {
                throw new RuntimeException("None type CredentialHash should not have hash");
            }
            this.hash = hash;
            this.type = type;
            this.version = version;
            this.isBaseZeroPattern = false;
        }

        private CredentialHash(byte[] hash, boolean isBaseZeroPattern) {
            this.hash = hash;
            this.type = 1;
            this.version = 1;
            this.isBaseZeroPattern = isBaseZeroPattern;
        }

        static CredentialHash create(byte[] hash, int type) {
            if (type != -1) {
                return new CredentialHash(hash, type, 1);
            }
            throw new RuntimeException("Bad type for CredentialHash");
        }

        static CredentialHash createEmptyHash() {
            return new CredentialHash(null, -1, 1);
        }
    }

    class DatabaseHelper extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "locksettings.db";
        private static final int DATABASE_VERSION = 2;
        private static final String TAG = "LockSettingsDB";
        private Callback mCallback;

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, 2);
            setWriteAheadLoggingEnabled(true);
        }

        public void setCallback(Callback callback) {
            this.mCallback = callback;
        }

        private void createTable(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE locksettings (_id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT,user INTEGER,value TEXT);");
        }

        public void onCreate(SQLiteDatabase db) {
            createTable(db);
            if (this.mCallback != null) {
                this.mCallback.initialize(db);
            }
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int currentVersion) {
            int upgradeVersion = oldVersion;
            if (oldVersion == 1) {
                upgradeVersion = 2;
            }
            if (upgradeVersion != 2) {
                Log.w(TAG, "Failed to upgrade database!");
            }
        }
    }

    public LockSettingsStorage(Context context) {
        this.mContext = context;
        this.mOpenHelper = new DatabaseHelper(context);
    }

    public void setDatabaseOnCreateCallback(Callback callback) {
        this.mOpenHelper.setCallback(callback);
    }

    public void writeKeyValue(String key, String value, int userId) {
        writeKeyValue(this.mOpenHelper.getWritableDatabase(), key, value, userId);
    }

    public void writeKeyValue(SQLiteDatabase db, String key, String value, int userId) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_KEY, key);
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

    /* JADX WARNING: Missing block: B:11:0x001a, code:
            r10 = DEFAULT;
     */
    /* JADX WARNING: Missing block: B:13:?, code:
            r8 = r12.mOpenHelper.getReadableDatabase().query(TABLE, COLUMNS_FOR_QUERY, "user=? AND name=?", new java.lang.String[]{java.lang.Integer.toString(r15), r13}, null, null, null);
     */
    /* JADX WARNING: Missing block: B:14:0x003e, code:
            if (r8 == null) goto L_0x004e;
     */
    /* JADX WARNING: Missing block: B:16:0x0044, code:
            if (r8.moveToFirst() == false) goto L_0x004b;
     */
    /* JADX WARNING: Missing block: B:17:0x0046, code:
            r10 = r8.getString(0);
     */
    /* JADX WARNING: Missing block: B:18:0x004b, code:
            r8.close();
     */
    /* JADX WARNING: Missing block: B:25:0x005b, code:
            r9 = move-exception;
     */
    /* JADX WARNING: Missing block: B:26:0x005c, code:
            android.util.Log.w(TAG, "readKeyValue got err:", r9);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public String readKeyValue(String key, String defaultValue, int userId) {
        int version;
        synchronized (this.mCache) {
            if (this.mCache.hasKeyValue(key, userId)) {
                String peekKeyValue = this.mCache.peekKeyValue(key, defaultValue, userId);
                return peekKeyValue;
            }
            version = this.mCache.getVersion();
        }
        this.mCache.putKeyValueIfUnchanged(key, result, userId, version);
        if (result != DEFAULT) {
            defaultValue = (String) result;
        }
        return defaultValue;
    }

    /* JADX WARNING: Missing block: B:11:?, code:
            r8 = r13.mOpenHelper.getReadableDatabase().query(TABLE, COLUMNS_FOR_PREFETCH, "user=?", new java.lang.String[]{java.lang.Integer.toString(r14)}, null, null, null);
     */
    /* JADX WARNING: Missing block: B:12:0x0038, code:
            if (r8 == null) goto L_0x005a;
     */
    /* JADX WARNING: Missing block: B:14:0x003e, code:
            if (r8.moveToNext() == false) goto L_0x0061;
     */
    /* JADX WARNING: Missing block: B:15:0x0040, code:
            r13.mCache.putKeyValueIfUnchanged(r8.getString(0), r8.getString(1), r14, r12);
     */
    /* JADX WARNING: Missing block: B:17:0x0050, code:
            r9 = move-exception;
     */
    /* JADX WARNING: Missing block: B:18:0x0051, code:
            android.util.Log.w(TAG, "prefetchUser got err:", r9);
     */
    /* JADX WARNING: Missing block: B:25:?, code:
            r8.close();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void prefetchUser(int userId) {
        synchronized (this.mCache) {
            if (this.mCache.isFetched(userId)) {
                return;
            } else {
                this.mCache.setFetched(userId);
                int version = this.mCache.getVersion();
            }
        }
        readCredentialHash(userId);
    }

    private CredentialHash readPasswordHashIfExists(int userId) {
        byte[] stored = readFile(getLockPasswordFilename(userId));
        if (!ArrayUtils.isEmpty(stored)) {
            return new CredentialHash(stored, 2, 1, null);
        }
        stored = readFile(getLegacyLockPasswordFilename(userId));
        if (!ArrayUtils.isEmpty(stored)) {
            return new CredentialHash(stored, 2, 0, null);
        }
        Log.i(TAG, "readPatternHash , cannot get any PasswordHash");
        return null;
    }

    private CredentialHash readPatternHashIfExists(int userId) {
        byte[] stored = readFile(getLockPatternFilename(userId));
        if (!ArrayUtils.isEmpty(stored)) {
            return new CredentialHash(stored, 1, 1, null);
        }
        stored = readFile(getBaseZeroLockPatternFilename(userId));
        if (!ArrayUtils.isEmpty(stored)) {
            return new CredentialHash(stored, true, null);
        }
        stored = readFile(getLegacyLockPatternFilename(userId));
        if (!ArrayUtils.isEmpty(stored)) {
            return new CredentialHash(stored, 1, 0, null);
        }
        Log.i(TAG, "readPatternHash , cannot get any PatternHash");
        return null;
    }

    public CredentialHash readCredentialHash(int userId) {
        CredentialHash passwordHash = readPasswordHashIfExists(userId);
        CredentialHash patternHash = readPatternHashIfExists(userId);
        if (passwordHash != null && patternHash != null) {
            if (IS_BUCKUP_PIN_EXIST) {
                LockPatternUtils lockPatternUtils = new LockPatternUtils(this.mContext);
                if (lockPatternUtils.getActivePasswordQuality(userId) == 65536) {
                    Log.w(TAG, "Currently there is a standby pin code, and the lock screen is pattern");
                    writeFile(getLockPasswordFilename(userId), null);
                    return patternHash;
                } else if (lockPatternUtils.getActivePasswordQuality(userId) != 0) {
                    Log.w(TAG, "Currently there is a standby pin code, and the lock screen is password");
                    writeFile(getLockPatternFilename(userId), null);
                    return passwordHash;
                }
            }
            if (passwordHash.version == 1) {
                return passwordHash;
            }
            return patternHash;
        } else if (passwordHash != null) {
            return passwordHash;
        } else {
            if (patternHash != null) {
                return patternHash;
            }
            return CredentialHash.createEmptyHash();
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
        if (hasFile(getLockPasswordFilename(userId))) {
            return true;
        }
        return hasFile(getLegacyLockPasswordFilename(userId));
    }

    public boolean hasPattern(int userId) {
        if (hasFile(getLockPatternFilename(userId)) || hasFile(getBaseZeroLockPatternFilename(userId))) {
            return true;
        }
        return hasFile(getLegacyLockPatternFilename(userId));
    }

    public boolean hasCredential(int userId) {
        return !hasPassword(userId) ? hasPattern(userId) : true;
    }

    private boolean hasFile(String name) {
        byte[] contents = readFile(name);
        if (contents == null || contents.length <= 0) {
            return false;
        }
        return true;
    }

    /* JADX WARNING: Removed duplicated region for block: B:36:0x0093 A:{SYNTHETIC, Splitter: B:36:0x0093} */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x004e  */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x00b6 A:{SYNTHETIC, Splitter: B:42:0x00b6} */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x004e  */
    /* JADX WARNING: Missing block: B:13:0x0029, code:
            r2 = null;
            r4 = null;
     */
    /* JADX WARNING: Missing block: B:15:?, code:
            r3 = new java.io.RandomAccessFile(r11, "r");
     */
    /* JADX WARNING: Missing block: B:17:?, code:
            r4 = new byte[((int) r3.length())];
            r3.readFully(r4, 0, r4.length);
            r3.close();
     */
    /* JADX WARNING: Missing block: B:18:0x0042, code:
            if (r3 == null) goto L_0x0047;
     */
    /* JADX WARNING: Missing block: B:20:?, code:
            r3.close();
     */
    /* JADX WARNING: Missing block: B:24:0x004e, code:
            dumpFileInfo(r11);
     */
    /* JADX WARNING: Missing block: B:30:0x005a, code:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:31:0x005b, code:
            android.util.Slog.e(TAG, "Error closing file " + r1);
     */
    /* JADX WARNING: Missing block: B:32:0x0076, code:
            r1 = e;
     */
    /* JADX WARNING: Missing block: B:34:?, code:
            android.util.Slog.e(TAG, "Cannot read file " + r1);
     */
    /* JADX WARNING: Missing block: B:35:0x0091, code:
            if (r2 != null) goto L_0x0093;
     */
    /* JADX WARNING: Missing block: B:37:?, code:
            r2.close();
     */
    /* JADX WARNING: Missing block: B:38:0x0097, code:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:39:0x0098, code:
            android.util.Slog.e(TAG, "Error closing file " + r1);
     */
    /* JADX WARNING: Missing block: B:40:0x00b3, code:
            r6 = th;
     */
    /* JADX WARNING: Missing block: B:41:0x00b4, code:
            if (r2 != null) goto L_0x00b6;
     */
    /* JADX WARNING: Missing block: B:43:?, code:
            r2.close();
     */
    /* JADX WARNING: Missing block: B:44:0x00b9, code:
            throw r6;
     */
    /* JADX WARNING: Missing block: B:45:0x00ba, code:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:46:0x00bb, code:
            android.util.Slog.e(TAG, "Error closing file " + r1);
     */
    /* JADX WARNING: Missing block: B:47:0x00d6, code:
            r6 = th;
     */
    /* JADX WARNING: Missing block: B:48:0x00d7, code:
            r2 = r3;
     */
    /* JADX WARNING: Missing block: B:49:0x00d9, code:
            r1 = e;
     */
    /* JADX WARNING: Missing block: B:50:0x00da, code:
            r2 = r3;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected byte[] readFile(String name) {
        int version;
        synchronized (this.mCache) {
            if (this.mCache.hasFile(name)) {
                byte[] cached = this.mCache.peekFile(name);
                if (ArrayUtils.isEmpty(cached)) {
                    Slog.e(TAG, "read file from cache fail.");
                } else {
                    return cached;
                }
            }
            version = this.mCache.getVersion();
        }
        RandomAccessFile randomAccessFile = raf;
        if (ArrayUtils.isEmpty(stored)) {
        }
        this.mCache.putFileIfUnchanged(name, stored, version);
        return stored;
    }

    private void dumpFileInfo(String name) {
        File f = new File(name);
        if (f.exists() && f.isFile()) {
            Slog.e(TAG, "size of file:" + name + " = " + f.length() + "; readable: " + f.canRead() + "; last modified " + f.lastModified());
        } else {
            Slog.e(TAG, "file not exist. " + name);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x001b A:{SYNTHETIC, Splitter: B:12:0x001b} */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x008f A:{SYNTHETIC, Splitter: B:41:0x008f} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void writeFile(String name, byte[] hash) {
        IOException e;
        Throwable th;
        synchronized (this.mFileWriteLock) {
            RandomAccessFile raf = null;
            try {
                RandomAccessFile raf2 = new RandomAccessFile(name, "rws");
                if (hash != null) {
                    try {
                        if (hash.length != 0) {
                            raf2.write(hash, 0, hash.length);
                            raf2.close();
                            if (raf2 != null) {
                                try {
                                    raf2.close();
                                } catch (IOException e2) {
                                    Slog.e(TAG, "Error closing file " + e2);
                                } catch (Throwable th2) {
                                    th = th2;
                                }
                            }
                            raf = raf2;
                            this.mCache.putFile(name, hash);
                            return;
                        }
                    } catch (IOException e3) {
                        e2 = e3;
                        raf = raf2;
                    } catch (Throwable th3) {
                        th = th3;
                        raf = raf2;
                        if (raf != null) {
                            try {
                                raf.close();
                            } catch (IOException e22) {
                                Slog.e(TAG, "Error closing file " + e22);
                            }
                        }
                        throw th;
                    }
                }
                raf2.setLength(0);
                raf2.close();
                if (raf2 != null) {
                }
                raf = raf2;
            } catch (IOException e4) {
                e22 = e4;
                try {
                    Slog.e(TAG, "Error writing to file " + e22);
                    if (raf != null) {
                        try {
                            raf.close();
                        } catch (IOException e222) {
                            Slog.e(TAG, "Error closing file " + e222);
                        } catch (Throwable th4) {
                            th = th4;
                        }
                    }
                    this.mCache.putFile(name, hash);
                    return;
                } catch (Throwable th5) {
                    th = th5;
                    if (raf != null) {
                    }
                    throw th;
                }
            }
            this.mCache.putFile(name, hash);
            return;
        }
        throw th;
    }

    private void deleteFile(String name) {
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

    String getLockPatternFilename(int userId) {
        return getLockCredentialFilePathForUser(userId, LOCK_PATTERN_FILE);
    }

    String getLockPasswordFilename(int userId) {
        return getLockCredentialFilePathForUser(userId, LOCK_PASSWORD_FILE);
    }

    String getLegacyLockPatternFilename(int userId) {
        return getLockCredentialFilePathForUser(userId, LEGACY_LOCK_PATTERN_FILE);
    }

    String getLegacyLockPasswordFilename(int userId) {
        return getLockCredentialFilePathForUser(userId, LEGACY_LOCK_PASSWORD_FILE);
    }

    private String getBaseZeroLockPatternFilename(int userId) {
        return getLockCredentialFilePathForUser(userId, BASE_ZERO_LOCK_PATTERN_FILE);
    }

    String getChildProfileLockFile(int userId) {
        return getLockCredentialFilePathForUser(userId, CHILD_PROFILE_LOCK_FILE);
    }

    private String getLockCredentialFilePathForUser(int userId, String basename) {
        String dataSystemDirectory = Environment.getDataDirectory().getAbsolutePath() + "/system/";
        if (userId == 0) {
            return dataSystemDirectory + basename;
        }
        return new File(Environment.getUserSystemDirectory(userId), basename).getAbsolutePath();
    }

    public void writeSyntheticPasswordState(int userId, long handle, String name, byte[] data) {
        writeFile(getSynthenticPasswordStateFilePathForUser(userId, handle, name), data);
    }

    public byte[] readSyntheticPasswordState(int userId, long handle, String name) {
        return readFile(getSynthenticPasswordStateFilePathForUser(userId, handle, name));
    }

    public void deleteSyntheticPasswordState(int userId, long handle, String name) {
        String path = getSynthenticPasswordStateFilePathForUser(userId, handle, name);
        File file = new File(path);
        if (file.exists()) {
            try {
                ((StorageManager) this.mContext.getSystemService(StorageManager.class)).secdiscard(file.getAbsolutePath());
            } catch (Exception e) {
                Slog.w(TAG, "Failed to secdiscard " + path, e);
            } finally {
                file.delete();
            }
            this.mCache.putFile(path, null);
        }
    }

    protected File getSyntheticPasswordDirectoryForUser(int userId) {
        return new File(Environment.getDataSystemDeDirectory(userId), SYNTHETIC_PASSWORD_DIRECTORY);
    }

    protected String getSynthenticPasswordStateFilePathForUser(int userId, long handle, String name) {
        File baseDir = getSyntheticPasswordDirectoryForUser(userId);
        String baseName = String.format("%016x.%s", new Object[]{Long.valueOf(handle), name});
        if (!baseDir.exists()) {
            baseDir.mkdir();
        }
        return new File(baseDir, baseName).getAbsolutePath();
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
                name = getLockPatternFilename(userId);
                file = new File(name);
                if (file.exists()) {
                    file.delete();
                    this.mCache.putFile(name, null);
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

    void closeDatabase() {
        this.mOpenHelper.close();
    }

    void clearCache() {
        this.mCache.clear();
    }

    protected String getLockCredentialFilePathForUser2(int userId, String basename) {
        return getLockCredentialFilePathForUser(userId, basename);
    }
}
