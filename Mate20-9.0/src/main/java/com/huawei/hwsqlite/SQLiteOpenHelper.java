package com.huawei.hwsqlite;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import com.huawei.hwsqlite.SQLiteDatabase;
import java.io.File;
import java.util.Arrays;

public abstract class SQLiteOpenHelper {
    private static final boolean DEBUG_STRICT_READONLY = false;
    private static final String TAG = SQLiteOpenHelper.class.getSimpleName();
    private final Context mContext;
    private SQLiteDatabase mDatabase;
    private boolean mEnableWriteAheadLogging;
    private byte[] mEncryptKey;
    private final SQLiteErrorHandler mErrorHandler;
    private final SQLiteDatabase.CursorFactory mFactory;
    private boolean mIsInitializing;
    private final int mMinimumSupportedVersion;
    private final String mName;
    private final int mNewVersion;
    private int mOpenFlags;

    public abstract void onCreate(SQLiteDatabase sQLiteDatabase);

    public abstract void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2);

    public SQLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        this(context, name, factory, version, null);
    }

    public SQLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, SQLiteErrorHandler errorHandler) {
        this(context, name, factory, version, 0, errorHandler);
    }

    @SuppressLint({"AvoidMax/Min"})
    public SQLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, int minimumSupportedVersion, SQLiteErrorHandler errorHandler) {
        if (version >= 1) {
            this.mContext = context;
            this.mName = name;
            this.mFactory = factory;
            this.mNewVersion = version;
            this.mErrorHandler = errorHandler;
            this.mMinimumSupportedVersion = Math.max(0, minimumSupportedVersion);
            return;
        }
        throw new IllegalArgumentException("Version must be >= 1, was " + version);
    }

    public String getDatabaseName() {
        return this.mName;
    }

    public void setWriteAheadLoggingEnabled(boolean enabled) {
        synchronized (this) {
            if (this.mEnableWriteAheadLogging != enabled) {
                if (this.mDatabase != null && this.mDatabase.isOpen() && !this.mDatabase.isReadOnly()) {
                    if (enabled) {
                        this.mDatabase.enableWriteAheadLogging();
                    } else {
                        this.mDatabase.disableWriteAheadLogging();
                    }
                }
                this.mEnableWriteAheadLogging = enabled;
            }
        }
    }

    public void setDatabaseOpenFlags(int openFlags) {
        if (((~-2130706432) & openFlags) == 0) {
            synchronized (this) {
                if (this.mDatabase != null) {
                    if (this.mDatabase.isOpen()) {
                        throw new IllegalStateException("Set open flags after database opened");
                    }
                }
                this.mOpenFlags |= openFlags;
            }
            return;
        }
        throw new IllegalArgumentException("Invalid open flags");
    }

    public void setDatabaseEncrypted(byte[] key) {
        if (key == null || key.length == 0) {
            throw new IllegalArgumentException("Empty encrypt key");
        }
        synchronized (this) {
            if (this.mDatabase != null) {
                if (this.mDatabase.isOpen()) {
                    throw new IllegalStateException("Set encrypted after database opened");
                }
            }
            this.mEncryptKey = Arrays.copyOf(key, key.length);
        }
    }

    public SQLiteDatabase getWritableDatabase() {
        SQLiteDatabase databaseLocked;
        synchronized (this) {
            databaseLocked = getDatabaseLocked(true);
        }
        return databaseLocked;
    }

    public SQLiteDatabase getReadableDatabase() {
        SQLiteDatabase databaseLocked;
        synchronized (this) {
            databaseLocked = getDatabaseLocked(false);
        }
        return databaseLocked;
    }

    private SQLiteDatabase getDatabaseLocked(boolean writable) {
        String path;
        String path2;
        if (this.mDatabase != null) {
            if (!this.mDatabase.isOpen()) {
                this.mDatabase = null;
            } else if (!writable || !this.mDatabase.isReadOnly()) {
                return this.mDatabase;
            }
        }
        if (!this.mIsInitializing) {
            SQLiteDatabase db = this.mDatabase;
            try {
                this.mIsInitializing = true;
                if (db != null) {
                    if (writable && db.isReadOnly()) {
                        db.reopenReadWrite();
                    }
                } else if (this.mName == null) {
                    db = SQLiteDatabase.create(null);
                } else {
                    if (this.mContext != null) {
                        path2 = this.mContext.getDatabasePath(this.mName).getPath();
                    } else {
                        path2 = this.mName;
                    }
                    int flags = this.mOpenFlags | 268435456;
                    if (this.mEnableWriteAheadLogging) {
                        flags |= 536870912;
                    }
                    if (this.mEncryptKey != null) {
                        flags |= SQLiteDatabase.ENABLE_DATABASE_ENCRYPTION;
                    }
                    db = SQLiteDatabase.openDatabase(path2, this.mFactory, flags, this.mErrorHandler, this.mEncryptKey);
                }
            } catch (SQLiteException ex) {
                if (!writable) {
                    String str = TAG;
                    Log.e(str, "Couldn't open " + this.mName + " for writing (will try read-only):", ex);
                    if (this.mContext != null) {
                        path = this.mContext.getDatabasePath(this.mName).getPath();
                    } else {
                        path = this.mName;
                    }
                    int flags2 = 1 | this.mOpenFlags;
                    if (this.mEncryptKey != null) {
                        flags2 |= SQLiteDatabase.ENABLE_DATABASE_ENCRYPTION;
                    }
                    db = SQLiteDatabase.openDatabase(path, this.mFactory, flags2, this.mErrorHandler, this.mEncryptKey);
                } else {
                    throw ex;
                }
            } catch (Throwable th) {
                this.mIsInitializing = false;
                if (!(db == null || db == this.mDatabase)) {
                    db.close();
                }
                throw th;
            }
            onConfigure(db);
            int version = db.getVersion();
            if (version != this.mNewVersion) {
                if (db.isReadOnly()) {
                    throw new SQLiteException("Can't upgrade read-only database from version " + db.getVersion() + " to " + this.mNewVersion + ": " + this.mName);
                } else if (version <= 0 || version >= this.mMinimumSupportedVersion) {
                    db.beginTransaction();
                    if (version == 0) {
                        onCreate(db);
                    } else if (version > this.mNewVersion) {
                        onDowngrade(db, version, this.mNewVersion);
                    } else {
                        onUpgrade(db, version, this.mNewVersion);
                    }
                    db.setVersion(this.mNewVersion);
                    db.setTransactionSuccessful();
                    db.endTransaction();
                } else {
                    File databaseFile = new File(db.getPath());
                    onBeforeDelete(db);
                    db.close();
                    if (SQLiteDatabase.deleteDatabase(databaseFile)) {
                        this.mIsInitializing = false;
                        SQLiteDatabase databaseLocked = getDatabaseLocked(writable);
                        this.mIsInitializing = false;
                        if (!(db == null || db == this.mDatabase)) {
                            db.close();
                        }
                        return databaseLocked;
                    }
                    throw new IllegalStateException("Unable to delete obsolete database " + this.mName + " with version " + version);
                }
            }
            onOpen(db);
            if (db.isReadOnly()) {
                String str2 = TAG;
                Log.w(str2, "Opened " + this.mName + " in read-only mode");
            }
            this.mDatabase = db;
            this.mIsInitializing = false;
            if (!(db == null || db == this.mDatabase)) {
                db.close();
            }
            return db;
        }
        throw new IllegalStateException("getDatabase called recursively");
    }

    public synchronized void close() {
        if (this.mIsInitializing) {
            throw new IllegalStateException("Closed during initialization");
        } else if (this.mDatabase != null && this.mDatabase.isOpen()) {
            this.mDatabase.close();
            this.mDatabase = null;
        }
    }

    public void onConfigure(SQLiteDatabase db) {
    }

    public void onBeforeDelete(SQLiteDatabase db) {
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        throw new SQLiteException("Can't downgrade database from version " + oldVersion + " to " + newVersion);
    }

    public void onOpen(SQLiteDatabase db) {
    }
}
