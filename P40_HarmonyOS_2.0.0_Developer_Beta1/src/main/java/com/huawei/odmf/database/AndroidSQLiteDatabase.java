package com.huawei.odmf.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.database.sqlite.SQLiteDiskIOException;
import android.os.CancellationSignal;
import com.huawei.odmf.exception.ODMFRuntimeException;
import com.huawei.odmf.exception.ODMFSQLiteDatabaseCorruptException;
import com.huawei.odmf.exception.ODMFSQLiteDiskIOException;
import com.huawei.odmf.exception.ODMFUnsupportedOperationException;
import com.huawei.odmf.utils.LOG;

public class AndroidSQLiteDatabase implements DataBase {
    private SQLiteDatabase mSQLiteDatabase = null;

    @Override // com.huawei.odmf.database.DataBase
    public com.huawei.hwsqlite.SQLiteDatabase getODMFSQLiteDatabase() {
        return null;
    }

    public AndroidSQLiteDatabase(SQLiteDatabase sQLiteDatabase) {
        this.mSQLiteDatabase = sQLiteDatabase;
    }

    @Override // com.huawei.odmf.database.DataBase
    public Cursor rawQuery(String str, String[] strArr) {
        return this.mSQLiteDatabase.rawQuery(str, strArr);
    }

    @Override // com.huawei.odmf.database.DataBase
    public void execSQL(String str) throws SQLException {
        this.mSQLiteDatabase.execSQL(str);
    }

    @Override // com.huawei.odmf.database.DataBase
    public void beginTransaction() {
        try {
            this.mSQLiteDatabase.beginTransaction();
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

    @Override // com.huawei.odmf.database.DataBase
    public void endTransaction() {
        endTransaction(false);
    }

    @Override // com.huawei.odmf.database.DataBase
    public void endTransaction(boolean z) {
        try {
            this.mSQLiteDatabase.endTransaction();
        } catch (SQLiteDatabaseCorruptException | com.huawei.hwsqlite.SQLiteDatabaseCorruptException e) {
            LOG.logE("End Transaction failed : A SQLiteDatabaseCorruptException occurred when end transaction.");
            if (!z) {
                throw new ODMFSQLiteDatabaseCorruptException("End Transaction failed : " + e.getMessage(), e);
            }
        } catch (SQLiteDiskIOException | com.huawei.hwsqlite.SQLiteDiskIOException e2) {
            LOG.logE("End Transaction failed : A SQLiteDiskIOException occurred when end transaction.");
            if (!z) {
                throw new ODMFSQLiteDiskIOException("End Transaction failed : " + e2.getMessage(), e2);
            }
        } catch (RuntimeException e3) {
            LOG.logE("End Transaction failed : A RuntimeException occurred when end transaction.");
            if (!z) {
                throw new ODMFRuntimeException("End Transaction failed : " + e3.getMessage(), e3);
            }
        }
    }

    @Override // com.huawei.odmf.database.DataBase
    public boolean inTransaction() {
        return this.mSQLiteDatabase.inTransaction();
    }

    @Override // com.huawei.odmf.database.DataBase
    public void setTransactionSuccessful() {
        this.mSQLiteDatabase.setTransactionSuccessful();
    }

    @Override // com.huawei.odmf.database.DataBase
    public void execSQL(String str, Object[] objArr) throws SQLException {
        this.mSQLiteDatabase.execSQL(str, objArr);
    }

    @Override // com.huawei.odmf.database.DataBase
    public Statement compileStatement(String str) {
        return new AndroidSQLiteStatement(this.mSQLiteDatabase.compileStatement(str));
    }

    @Override // com.huawei.odmf.database.DataBase
    public boolean isDbLockedByCurrentThread() {
        return this.mSQLiteDatabase.isDbLockedByCurrentThread();
    }

    @Override // com.huawei.odmf.database.DataBase
    public void close() {
        this.mSQLiteDatabase.close();
    }

    @Override // com.huawei.odmf.database.DataBase
    public boolean isOpen() {
        return this.mSQLiteDatabase.isOpen();
    }

    @Override // com.huawei.odmf.database.DataBase
    public int delete(String str, String str2, String[] strArr) {
        return this.mSQLiteDatabase.delete(str, str2, strArr);
    }

    @Override // com.huawei.odmf.database.DataBase
    public long insertOrThrow(String str, String str2, ContentValues contentValues) {
        return this.mSQLiteDatabase.insertOrThrow(str, str2, contentValues);
    }

    @Override // com.huawei.odmf.database.DataBase
    public Cursor query(boolean z, String str, String[] strArr, String str2, String[] strArr2, String str3, String str4, String str5, String str6) {
        return this.mSQLiteDatabase.query(z, str, strArr, str2, strArr2, str3, str4, str5, str6);
    }

    @Override // com.huawei.odmf.database.DataBase
    public Cursor commonquery(boolean z, String str, String[] strArr, String str2, String[] strArr2, String str3, String str4, String str5, String str6) {
        return this.mSQLiteDatabase.query(z, str, strArr, str2, strArr2, str3, str4, str5, str6);
    }

    @Override // com.huawei.odmf.database.DataBase
    public Cursor query(String str, String[] strArr, String str2, String[] strArr2, String str3, String str4, String str5, String str6) {
        return this.mSQLiteDatabase.query(str, strArr, str2, strArr2, str3, str4, str5, str6);
    }

    @Override // com.huawei.odmf.database.DataBase
    public int update(String str, ContentValues contentValues, String str2, String[] strArr) {
        return this.mSQLiteDatabase.update(str, contentValues, str2, strArr);
    }

    @Override // com.huawei.odmf.database.DataBase
    public String getPath() {
        return this.mSQLiteDatabase.getPath();
    }

    @Override // com.huawei.odmf.database.DataBase
    public SQLiteDatabase getAndroidSQLiteDatabase() {
        return this.mSQLiteDatabase;
    }

    @Override // com.huawei.odmf.database.DataBase
    public void resetDatabaseEncryptKey(byte[] bArr, byte[] bArr2) {
        throw new ODMFUnsupportedOperationException("Android SQLiteDatabase does not support changing key");
    }

    @Override // com.huawei.odmf.database.DataBase
    public void addAttachAlias(String str, String str2, byte[] bArr) throws SQLException {
        throw new ODMFUnsupportedOperationException("Android SQLiteDatabase does not support attaching a database");
    }

    @Override // com.huawei.odmf.database.DataBase
    public void removeAttachAlias(String str) {
        throw new ODMFUnsupportedOperationException("Android SQLiteDatabase does not support removing a database");
    }

    @Override // com.huawei.odmf.database.DataBase
    public Cursor rawSelect(String str, String[] strArr, CancellationSignal cancellationSignal, boolean z) {
        return this.mSQLiteDatabase.rawQuery(str, strArr, cancellationSignal);
    }

    @Override // com.huawei.odmf.database.DataBase
    public String[] getSQLTables(String str, int i, CancellationSignal cancellationSignal) {
        throw new ODMFUnsupportedOperationException("Android SQLiteDatabase does not support to get tables in a sql.");
    }
}
