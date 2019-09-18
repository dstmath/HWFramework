package android.database.sqlite;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.database.CursorResourceWrapper;
import android.database.CursorWindow;
import android.database.DatabaseErrorHandler;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseEx;
import android.os.FileUtils;
import android.util.Log;
import com.android.internal.util.Preconditions;
import java.io.File;

public abstract class SQLiteOpenHelper {
    private static final String TAG = SQLiteOpenHelper.class.getSimpleName();
    private SQLiteDatabaseEx.DatabaseConnectionExclusiveHandler mConnectionExclusiveHandler;
    private final Context mContext;
    private SQLiteDatabase mDatabase;
    private boolean mEnableExclusiveConnection;
    private boolean mIsInitializing;
    private final int mMinimumSupportedVersion;
    private final String mName;
    private final int mNewVersion;
    private SQLiteDatabase.OpenParams.Builder mOpenParamsBuilder;

    public abstract void onCreate(SQLiteDatabase sQLiteDatabase);

    public abstract void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2);

    public SQLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        this(context, name, factory, version, (DatabaseErrorHandler) null);
    }

    public SQLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        this(context, name, factory, version, 0, errorHandler);
    }

    public SQLiteOpenHelper(Context context, String name, int version, SQLiteDatabase.OpenParams openParams) {
        this(context, name, version, 0, openParams.toBuilder());
    }

    public SQLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, int minimumSupportedVersion, DatabaseErrorHandler errorHandler) {
        this(context, name, version, minimumSupportedVersion, new SQLiteDatabase.OpenParams.Builder());
        this.mOpenParamsBuilder.setCursorFactory(factory);
        this.mOpenParamsBuilder.setErrorHandler(errorHandler);
    }

    private SQLiteOpenHelper(Context context, String name, int version, int minimumSupportedVersion, SQLiteDatabase.OpenParams.Builder openParamsBuilder) {
        Preconditions.checkNotNull(openParamsBuilder);
        if (version >= 1) {
            this.mContext = context;
            this.mName = name;
            this.mNewVersion = version;
            this.mMinimumSupportedVersion = Math.max(0, minimumSupportedVersion);
            this.mOpenParamsBuilder = openParamsBuilder;
            this.mOpenParamsBuilder.addOpenFlags(268435456);
            if (CursorResourceWrapper.isNeedResProtect(this.mContext)) {
                CursorWindow.setCursorResource(new CursorResourceWrapper(context));
            }
            this.mOpenParamsBuilder = openParamsBuilder;
            this.mOpenParamsBuilder.addOpenFlags(268435456);
            return;
        }
        throw new IllegalArgumentException("Version must be >= 1, was " + version);
    }

    public String getDatabaseName() {
        return this.mName;
    }

    public void setWriteAheadLoggingEnabled(boolean enabled) {
        synchronized (this) {
            if (this.mOpenParamsBuilder.isWriteAheadLoggingEnabled() != enabled) {
                if (this.mDatabase != null && this.mDatabase.isOpen() && !this.mDatabase.isReadOnly()) {
                    if (enabled) {
                        this.mDatabase.enableWriteAheadLogging();
                    } else {
                        this.mDatabase.disableWriteAheadLogging();
                    }
                }
                this.mOpenParamsBuilder.setWriteAheadLoggingEnabled(enabled);
            }
            this.mOpenParamsBuilder.addOpenFlags(1073741824);
        }
    }

    public void setLookasideConfig(int slotSize, int slotCount) {
        synchronized (this) {
            if (this.mDatabase != null) {
                if (this.mDatabase.isOpen()) {
                    throw new IllegalStateException("Lookaside memory config cannot be changed after opening the database");
                }
            }
            this.mOpenParamsBuilder.setLookasideConfig(slotSize, slotCount);
        }
    }

    public void setOpenParams(SQLiteDatabase.OpenParams openParams) {
        Preconditions.checkNotNull(openParams);
        synchronized (this) {
            if (this.mDatabase != null) {
                if (this.mDatabase.isOpen()) {
                    throw new IllegalStateException("OpenParams cannot be set after opening the database");
                }
            }
            setOpenParamsBuilder(new SQLiteDatabase.OpenParams.Builder(openParams));
        }
    }

    private void setOpenParamsBuilder(SQLiteDatabase.OpenParams.Builder openParamsBuilder) {
        this.mOpenParamsBuilder = openParamsBuilder;
        this.mOpenParamsBuilder.addOpenFlags(268435456);
    }

    public void setIdleConnectionTimeout(long idleConnectionTimeoutMs) {
        synchronized (this) {
            if (this.mDatabase != null) {
                if (this.mDatabase.isOpen()) {
                    throw new IllegalStateException("Connection timeout setting cannot be changed after opening the database");
                }
            }
            this.mOpenParamsBuilder.setIdleConnectionTimeout(idleConnectionTimeoutMs);
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
        File filePath;
        SQLiteDatabase.OpenParams params;
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
                    db = SQLiteDatabase.createInMemory(this.mOpenParamsBuilder.build());
                } else {
                    filePath = this.mContext.getDatabasePath(this.mName);
                    params = this.mOpenParamsBuilder.build();
                    db = SQLiteDatabase.openDatabase(filePath, params);
                    setFilePermissionsForDb(filePath.getPath());
                    db.enableExclusiveConnection(this.mEnableExclusiveConnection, this.mConnectionExclusiveHandler);
                }
            } catch (SQLException ex) {
                if (!writable) {
                    String str = TAG;
                    Log.e(str, "Couldn't open " + this.mName + " for writing (will try read-only):", ex);
                    db = SQLiteDatabase.openDatabase(filePath, params.toBuilder().addOpenFlags(1).build());
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

    private static void setFilePermissionsForDb(String dbPath) {
        FileUtils.setPermissions(dbPath, DevicePolicyManager.PROFILE_KEYGUARD_FEATURES_AFFECT_OWNER, -1, -1);
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

    public void setExclusiveConnectionEnabled(boolean enabled, SQLiteDatabaseEx.DatabaseConnectionExclusiveHandler connectionExclusiveHandler) {
        synchronized (this) {
            this.mEnableExclusiveConnection = enabled;
            this.mConnectionExclusiveHandler = connectionExclusiveHandler;
        }
    }
}
