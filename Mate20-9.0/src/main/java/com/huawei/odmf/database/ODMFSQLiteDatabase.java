package com.huawei.odmf.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.database.sqlite.SQLiteDiskIOException;
import android.os.CancellationSignal;
import com.huawei.hwsqlite.SQLiteBusyException;
import com.huawei.hwsqlite.SQLiteDatabase;
import com.huawei.odmf.exception.ODMFRuntimeException;
import com.huawei.odmf.exception.ODMFSQLiteDatabaseCorruptException;
import com.huawei.odmf.exception.ODMFSQLiteDiskIOException;
import com.huawei.odmf.utils.LOG;

public class ODMFSQLiteDatabase implements DataBase {
    private SQLiteDatabase mODMFDatabase = null;

    public ODMFSQLiteDatabase(SQLiteDatabase mODMFDatabase2) {
        this.mODMFDatabase = mODMFDatabase2;
    }

    public Cursor rawQuery(String sql, String[] selectionArgs) {
        return this.mODMFDatabase.rawQuery(sql, selectionArgs);
    }

    public void execSQL(String sql) throws SQLException {
        this.mODMFDatabase.execSQL(sql);
    }

    public void beginTransaction() {
        try {
            this.mODMFDatabase.beginTransaction();
        } catch (SQLiteDatabaseCorruptException | com.huawei.hwsqlite.SQLiteDatabaseCorruptException e) {
            LOG.logE("Begin Transaction failed : A SQLiteDatabaseCorruptException occurred when begin transaction.");
            throw new ODMFSQLiteDatabaseCorruptException("Close database failed : " + e.getMessage(), e);
        } catch (SQLiteDiskIOException | com.huawei.hwsqlite.SQLiteDiskIOException e2) {
            LOG.logE("Begin Transaction failed : A SQLiteDiskIOException occurred when begin transaction..");
            throw new ODMFSQLiteDiskIOException("Close database failed : " + e2.getMessage(), e2);
        } catch (RuntimeException e3) {
            LOG.logE("Begin Transaction failed : A RuntimeException occurred when begin transaction.");
            throw new ODMFRuntimeException("Close database failed : " + e3.getMessage(), e3);
        }
    }

    public void endTransaction() {
        endTransaction(false);
    }

    public void endTransaction(boolean hasCorruptException) {
        try {
            this.mODMFDatabase.endTransaction();
        } catch (SQLiteDatabaseCorruptException | com.huawei.hwsqlite.SQLiteDatabaseCorruptException e) {
            LOG.logE("End Transaction failed : A SQLiteDatabaseCorruptException occurred when end transaction.");
            if (!hasCorruptException) {
                throw new ODMFSQLiteDatabaseCorruptException("End Transaction failed : " + e.getMessage(), e);
            }
        } catch (SQLiteDiskIOException | com.huawei.hwsqlite.SQLiteDiskIOException e2) {
            LOG.logE("End Transaction failed : A SQLiteDiskIOException occurred when end transaction.");
            if (!hasCorruptException) {
                throw new ODMFSQLiteDiskIOException("End Transaction failed : " + e2.getMessage(), e2);
            }
        } catch (RuntimeException e3) {
            LOG.logE("End Transaction failed : A RuntimeException occurred when end transaction.");
            if (!hasCorruptException) {
                throw new ODMFRuntimeException("End Transaction failed : " + e3.getMessage(), e3);
            }
        }
    }

    public boolean inTransaction() {
        return this.mODMFDatabase.inTransaction();
    }

    public void setTransactionSuccessful() {
        this.mODMFDatabase.setTransactionSuccessful();
    }

    public void execSQL(String sql, Object[] bindArgs) throws SQLException {
        this.mODMFDatabase.execSQL(sql, bindArgs);
    }

    public Statement compileStatement(String sql) {
        return new ODMFSQLiteStatement(this.mODMFDatabase.compileStatement(sql));
    }

    public boolean isDbLockedByCurrentThread() {
        return this.mODMFDatabase.isDbLockedByCurrentThread();
    }

    public void close() {
        this.mODMFDatabase.close();
    }

    public boolean isOpen() {
        return this.mODMFDatabase.isOpen();
    }

    public int delete(String table, String whereClause, String[] whereArgs) {
        return this.mODMFDatabase.delete(table, whereClause, whereArgs);
    }

    public long insertOrThrow(String table, String nullColumnHack, ContentValues values) {
        return this.mODMFDatabase.insertOrThrow(table, nullColumnHack, values);
    }

    public Cursor query(boolean distinct, String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
        return this.mODMFDatabase.query(distinct, table, columns, selection, selectionArgs, groupBy, having, orderBy, limit, true);
    }

    public Cursor commonquery(boolean distinct, String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
        return this.mODMFDatabase.query(distinct, table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
    }

    public Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
        return this.mODMFDatabase.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
    }

    public int update(String table, ContentValues values, String whereClause, String[] whereArgs) {
        return this.mODMFDatabase.update(table, values, whereClause, whereArgs);
    }

    public String getPath() {
        return this.mODMFDatabase.getPath();
    }

    public android.database.sqlite.SQLiteDatabase getAndroidSQLiteDatabase() {
        return null;
    }

    public SQLiteDatabase getODMFSQLiteDatabase() {
        return this.mODMFDatabase;
    }

    public void resetDatabaseEncryptKey(byte[] oldKey, byte[] newKey) {
        try {
            this.mODMFDatabase.changeEncryptKey(oldKey, newKey);
        } catch (SQLiteBusyException e) {
            throw new ODMFRuntimeException("error happens when changing key for encrypted database");
        } catch (IllegalArgumentException | IllegalStateException e2) {
            throw new ODMFRuntimeException("error happens when changing key for encrypted database : " + e2.getMessage());
        }
    }

    public void addAttachAlias(String alias, String path, byte[] key) throws SQLException {
        this.mODMFDatabase.addAttachAlias(alias, path, key);
    }

    public void removeAttachAlias(String alias) {
        this.mODMFDatabase.removeAttachAlias(alias);
    }

    public Cursor rawSelect(String sql, String[] selectionArgs, CancellationSignal cancellationSignal, boolean stepQuery) {
        return this.mODMFDatabase.rawSelect(sql, selectionArgs, cancellationSignal, stepQuery);
    }
}
