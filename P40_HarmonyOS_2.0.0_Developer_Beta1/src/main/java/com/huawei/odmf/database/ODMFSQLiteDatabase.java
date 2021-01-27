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

    @Override // com.huawei.odmf.database.DataBase
    public android.database.sqlite.SQLiteDatabase getAndroidSQLiteDatabase() {
        return null;
    }

    public ODMFSQLiteDatabase(SQLiteDatabase sQLiteDatabase) {
        this.mODMFDatabase = sQLiteDatabase;
    }

    @Override // com.huawei.odmf.database.DataBase
    public Cursor rawQuery(String str, String[] strArr) {
        return this.mODMFDatabase.rawQuery(str, strArr);
    }

    @Override // com.huawei.odmf.database.DataBase
    public void execSQL(String str) throws SQLException {
        this.mODMFDatabase.execSQL(str);
    }

    @Override // com.huawei.odmf.database.DataBase
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

    @Override // com.huawei.odmf.database.DataBase
    public void endTransaction() {
        endTransaction(false);
    }

    @Override // com.huawei.odmf.database.DataBase
    public void endTransaction(boolean z) {
        try {
            this.mODMFDatabase.endTransaction();
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
        return this.mODMFDatabase.inTransaction();
    }

    @Override // com.huawei.odmf.database.DataBase
    public void setTransactionSuccessful() {
        this.mODMFDatabase.setTransactionSuccessful();
    }

    @Override // com.huawei.odmf.database.DataBase
    public void execSQL(String str, Object[] objArr) throws SQLException {
        this.mODMFDatabase.execSQL(str, objArr);
    }

    @Override // com.huawei.odmf.database.DataBase
    public Statement compileStatement(String str) {
        return new ODMFSQLiteStatement(this.mODMFDatabase.compileStatement(str));
    }

    @Override // com.huawei.odmf.database.DataBase
    public boolean isDbLockedByCurrentThread() {
        return this.mODMFDatabase.isDbLockedByCurrentThread();
    }

    @Override // com.huawei.odmf.database.DataBase
    public void close() {
        this.mODMFDatabase.close();
    }

    @Override // com.huawei.odmf.database.DataBase
    public boolean isOpen() {
        return this.mODMFDatabase.isOpen();
    }

    @Override // com.huawei.odmf.database.DataBase
    public int delete(String str, String str2, String[] strArr) {
        return this.mODMFDatabase.delete(str, str2, strArr);
    }

    @Override // com.huawei.odmf.database.DataBase
    public long insertOrThrow(String str, String str2, ContentValues contentValues) {
        return this.mODMFDatabase.insertOrThrow(str, str2, contentValues);
    }

    @Override // com.huawei.odmf.database.DataBase
    public Cursor query(boolean z, String str, String[] strArr, String str2, String[] strArr2, String str3, String str4, String str5, String str6) {
        return this.mODMFDatabase.query(z, str, strArr, str2, strArr2, str3, str4, str5, str6, true);
    }

    @Override // com.huawei.odmf.database.DataBase
    public Cursor commonquery(boolean z, String str, String[] strArr, String str2, String[] strArr2, String str3, String str4, String str5, String str6) {
        return this.mODMFDatabase.query(z, str, strArr, str2, strArr2, str3, str4, str5, str6);
    }

    @Override // com.huawei.odmf.database.DataBase
    public Cursor query(String str, String[] strArr, String str2, String[] strArr2, String str3, String str4, String str5, String str6) {
        return this.mODMFDatabase.query(str, strArr, str2, strArr2, str3, str4, str5, str6);
    }

    @Override // com.huawei.odmf.database.DataBase
    public int update(String str, ContentValues contentValues, String str2, String[] strArr) {
        return this.mODMFDatabase.update(str, contentValues, str2, strArr);
    }

    @Override // com.huawei.odmf.database.DataBase
    public String getPath() {
        return this.mODMFDatabase.getPath();
    }

    @Override // com.huawei.odmf.database.DataBase
    public SQLiteDatabase getODMFSQLiteDatabase() {
        return this.mODMFDatabase;
    }

    @Override // com.huawei.odmf.database.DataBase
    public void resetDatabaseEncryptKey(byte[] bArr, byte[] bArr2) {
        try {
            this.mODMFDatabase.changeEncryptKey(bArr, bArr2);
        } catch (SQLiteBusyException unused) {
            throw new ODMFRuntimeException("error happens when changing key for encrypted database");
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new ODMFRuntimeException("error happens when changing key for encrypted database : " + e.getMessage());
        }
    }

    @Override // com.huawei.odmf.database.DataBase
    public void addAttachAlias(String str, String str2, byte[] bArr) throws SQLException {
        this.mODMFDatabase.addAttachAlias(str, str2, bArr);
    }

    @Override // com.huawei.odmf.database.DataBase
    public void removeAttachAlias(String str) {
        this.mODMFDatabase.removeAttachAlias(str);
    }

    @Override // com.huawei.odmf.database.DataBase
    public Cursor rawSelect(String str, String[] strArr, CancellationSignal cancellationSignal, boolean z) {
        return this.mODMFDatabase.rawSelect(str, strArr, cancellationSignal, z);
    }

    @Override // com.huawei.odmf.database.DataBase
    public String[] getSQLTables(String str, int i, CancellationSignal cancellationSignal) {
        return this.mODMFDatabase.getSQLTables(str, i, cancellationSignal);
    }
}
