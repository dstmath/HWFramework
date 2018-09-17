package android.database.sqlite;

import android.content.Context;
import android.database.CursorResourceWrapper;
import android.database.CursorWindow;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteDatabaseEx.DatabaseConnectionExclusiveHandler;
import android.util.Log;
import java.io.File;

public abstract class SQLiteOpenHelper {
    private static final boolean DEBUG_STRICT_READONLY = false;
    private static final String TAG = SQLiteOpenHelper.class.getSimpleName();
    private DatabaseConnectionExclusiveHandler mConnectionExclusiveHandler;
    private final Context mContext;
    private SQLiteDatabase mDatabase;
    private boolean mEnableExclusiveConnection;
    private boolean mEnableWriteAheadLogging;
    private final DatabaseErrorHandler mErrorHandler;
    private final CursorFactory mFactory;
    private boolean mIsInitializing;
    private final int mMinimumSupportedVersion;
    private final String mName;
    private final int mNewVersion;

    public abstract void onCreate(SQLiteDatabase sQLiteDatabase);

    public abstract void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2);

    public SQLiteOpenHelper(Context context, String name, CursorFactory factory, int version) {
        this(context, name, factory, version, null);
    }

    public SQLiteOpenHelper(Context context, String name, CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        this(context, name, factory, version, 0, errorHandler);
    }

    public SQLiteOpenHelper(Context context, String name, CursorFactory factory, int version, int minimumSupportedVersion, DatabaseErrorHandler errorHandler) {
        if (version < 1) {
            throw new IllegalArgumentException("Version must be >= 1, was " + version);
        }
        this.mContext = context;
        this.mName = name;
        this.mFactory = factory;
        this.mNewVersion = version;
        this.mErrorHandler = errorHandler;
        this.mMinimumSupportedVersion = Math.max(0, minimumSupportedVersion);
        if (CursorResourceWrapper.isNeedResProtect(this.mContext)) {
            CursorWindow.setCursorResource(new CursorResourceWrapper(context));
        }
    }

    public String getDatabaseName() {
        return this.mName;
    }

    public void setWriteAheadLoggingEnabled(boolean enabled) {
        synchronized (this) {
            if (this.mEnableWriteAheadLogging != enabled) {
                if (!(this.mDatabase == null || !this.mDatabase.isOpen() || (this.mDatabase.isReadOnly() ^ 1) == 0)) {
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
        if (this.mDatabase != null) {
            if (!this.mDatabase.isOpen()) {
                this.mDatabase = null;
            } else if (!(writable && (this.mDatabase.isReadOnly() ^ 1) == 0)) {
                return this.mDatabase;
            }
        }
        if (this.mIsInitializing) {
            throw new IllegalStateException("getDatabase called recursively");
        }
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
                int i;
                Context context = this.mContext;
                String str = this.mName;
                if (this.mEnableWriteAheadLogging) {
                    i = 8;
                } else {
                    i = 0;
                }
                db = context.openOrCreateDatabase(str, i, this.mFactory, this.mErrorHandler);
                db.enableExclusiveConnection(this.mEnableExclusiveConnection, this.mConnectionExclusiveHandler);
            }
        } catch (SQLiteException ex) {
            if (writable) {
                throw ex;
            }
            Log.e(TAG, "Couldn't open " + this.mName + " for writing (will try read-only):", ex);
            db = SQLiteDatabase.openDatabase(this.mContext.getDatabasePath(this.mName).getPath(), this.mFactory, 1, this.mErrorHandler);
        } catch (Throwable th) {
            this.mIsInitializing = false;
            if (!(db == null || db == this.mDatabase)) {
                db.close();
            }
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
            Log.w(TAG, "Opened " + this.mName + " in read-only mode");
        }
        this.mDatabase = db;
        this.mIsInitializing = false;
        if (!(db == null || db == this.mDatabase)) {
            db.close();
        }
        return db;
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

    public void setExclusiveConnectionEnabled(boolean enabled, DatabaseConnectionExclusiveHandler connectionExclusiveHandler) {
        synchronized (this) {
            this.mEnableExclusiveConnection = enabled;
            this.mConnectionExclusiveHandler = connectionExclusiveHandler;
        }
    }
}
