package com.huawei.odmf.core;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.database.sqlite.SQLiteDiskIOException;
import android.database.sqlite.SQLiteException;
import com.huawei.odmf.database.AndroidSQLiteDatabase;
import com.huawei.odmf.database.DataBase;
import com.huawei.odmf.database.ODMFSQLiteDatabase;
import com.huawei.odmf.exception.ODMFIllegalArgumentException;
import com.huawei.odmf.exception.ODMFIllegalStateException;
import com.huawei.odmf.exception.ODMFRuntimeException;
import com.huawei.odmf.exception.ODMFSQLiteDatabaseCorruptException;
import com.huawei.odmf.exception.ODMFSQLiteDiskIOException;
import com.huawei.odmf.exception.ODMFXmlParserException;
import com.huawei.odmf.model.api.ObjectModelFactory;
import com.huawei.odmf.store.AndroidDatabaseHelper;
import com.huawei.odmf.store.DatabaseHelper;
import com.huawei.odmf.store.ODMFDatabaseHelper;
import com.huawei.odmf.utils.LOG;
import com.huawei.odmf.utils.StringUtil;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

class CrossPersistentStore extends PersistentStore {
    private Context context;
    private DatabaseHelper databaseHelper;
    private List<String> databasePaths;
    private DataBase db;

    CrossPersistentStore(Context context2, String str, String str2, Configuration configuration, List<String> list, List<byte[]> list2) {
        super(configuration.getPath(), configuration.getDatabaseType(), configuration.getStorageMode(), str2);
        if (str == null || str.equals("")) {
            setModel(null);
        } else {
            try {
                setModel(ObjectModelFactory.parse(context2, str));
            } catch (ODMFIllegalArgumentException | ODMFXmlParserException e) {
                LOG.logE("create mObjectModel failed.");
                throw new ODMFRuntimeException("Xml parser failed : " + e.getMessage());
            }
        }
        this.databasePaths = list;
        this.context = context2;
        String path = configuration.getPath();
        path = (path == null || path.equals("")) ? getModel() != null ? getModel().getDatabaseName() : null : path;
        setPath(path);
        init(configuration.getStorageMode() == 401 ? null : path, configuration.isThrowException(), configuration.isDetectDelete());
        if (!attachDatabases(list2)) {
            this.db.close();
            throw new ODMFRuntimeException("error happens when attaching database.");
        }
    }

    private boolean attachDatabases(List<byte[]> list) {
        int size = this.databasePaths.size();
        for (int i = 0; i < size; i++) {
            if (this.databasePaths.get(i) == null || this.databasePaths.get(i).equals("")) {
                throw new ODMFIllegalArgumentException("The database which you want to attached may be a memory database.");
            }
            File databasePath = this.context.getDatabasePath(this.databasePaths.get(i));
            if (!databasePath.exists()) {
                for (int i2 = i - 1; i2 >= 0; i2++) {
                    this.db.removeAttachAlias(this.databasePaths.get(i2));
                }
                LOG.logE("error happens when attaching database!!");
                throw new ODMFIllegalStateException("The database " + this.databasePaths.get(i) + " you want to attached does not exist.");
            }
            try {
                this.db.addAttachAlias(getDatabaseNameFromPath(this.databasePaths.get(i)), databasePath.getCanonicalPath(), list.get(i));
            } catch (SQLException | IOException unused) {
                LOG.logE("error happens when attaching database!!");
                for (int i3 = 0; i3 < i; i3++) {
                    this.db.removeAttachAlias(this.databasePaths.get(i3));
                }
                return false;
            }
        }
        return true;
    }

    private String getDatabaseNameFromPath(String str) {
        if (!str.contains("/")) {
            return str;
        }
        return new File(str.trim()).getName();
    }

    private void init(String str, boolean z, boolean z2) {
        try {
            if (getDatabaseType() == 302) {
                this.databaseHelper = new AndroidDatabaseHelper(this.context, str, getModel());
                this.db = new AndroidSQLiteDatabase(((AndroidDatabaseHelper) this.databaseHelper).getWritableDatabase());
            } else if (getDatabaseType() == 301) {
                this.databaseHelper = new ODMFDatabaseHelper(this.context, str, getModel(), z, z2);
                this.db = new ODMFSQLiteDatabase(((ODMFDatabaseHelper) this.databaseHelper).getWritableDatabase());
            } else {
                throw new ODMFRuntimeException("configuration of database is wrong");
            }
        } catch (SQLiteException unused) {
            throw new ODMFRuntimeException("errors happens when initializing database");
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.odmf.core.PersistentStore
    public void executeRawSQL(String str) {
        try {
            this.db.execSQL(str);
        } catch (SQLiteException | com.huawei.hwsqlite.SQLiteException e) {
            LOG.logE("Execute SQL Failed : A SQLiteException occurred when execute SQL");
            throw new ODMFRuntimeException("Save Failed : " + e.getMessage());
        } catch (IllegalStateException e2) {
            LOG.logE("Execute rawSQL failed : A IllegalStateException occurred when execute SQL.");
            throw new ODMFRuntimeException("Execute rawSQL failed : " + e2.getMessage(), e2);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.odmf.core.PersistentStore
    public Cursor query(boolean z, String str, String[] strArr, String str2, String[] strArr2, String str3, String str4, String str5, String str6) {
        try {
            return DatabaseQueryService.query(this.db, z, str, strArr, str2, strArr2, str3, str4, str5, str6);
        } catch (SQLiteException | com.huawei.hwsqlite.SQLiteException e) {
            LOG.logE("Execute RawSQL Failed : A SQLiteException occurred when execute SQL.");
            throw new ODMFRuntimeException("Execute RawSQL Failed : " + e.getMessage());
        } catch (IllegalStateException e2) {
            LOG.logE("Execute rawSQL failed : A IllegalStateException occurred when execute SQL.");
            throw new ODMFRuntimeException("Execute rawSQL failed : " + e2.getMessage(), e2);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.odmf.core.PersistentStore
    public Cursor executeRawQuerySQL(String str) {
        try {
            return this.db.rawQuery(str, null);
        } catch (SQLiteException | com.huawei.hwsqlite.SQLiteException e) {
            LOG.logE("Raw Query Failed : A SQLiteException occurred when execute rawQuery");
            throw new ODMFRuntimeException("Save Failed : " + e.getMessage());
        } catch (IllegalStateException e2) {
            LOG.logE("Execute rawQuerySQL failed : A IllegalStateException occurred when execute rawQuery.");
            throw new ODMFRuntimeException("Execute rawQuerySQL failed : " + e2.getMessage(), e2);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.odmf.core.PersistentStore
    public void close() {
        try {
            this.databaseHelper.close();
        } catch (SQLiteDatabaseCorruptException | com.huawei.hwsqlite.SQLiteDatabaseCorruptException e) {
            LOG.logE("Close database failed : A SQLiteDatabaseCorruptException occurred when close.");
            throw new ODMFSQLiteDatabaseCorruptException("Close database failed : " + e.getMessage(), e);
        } catch (SQLiteDiskIOException | com.huawei.hwsqlite.SQLiteDiskIOException e2) {
            LOG.logE("Close database failed : A SQLiteDiskIOException occurred when close.");
            throw new ODMFSQLiteDiskIOException("Close database failed : " + e2.getMessage(), e2);
        } catch (RuntimeException e3) {
            LOG.logE("Close database failed : A RuntimeException occurred when close.");
            throw new ODMFRuntimeException("Close database failed : " + e3.getMessage(), e3);
        }
    }

    @Override // com.huawei.odmf.core.PersistentStore
    public Set<String> getTableInvolvedInSQL(String str) {
        return StringUtil.array2Set(this.db.getSQLTables(str, 0, null));
    }

    @Override // com.huawei.odmf.core.PersistentStore
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override // com.huawei.odmf.core.PersistentStore
    public int hashCode() {
        return super.hashCode();
    }
}
