package com.huawei.odmf.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.CancellationSignal;

public interface DataBase {
    void addAttachAlias(String str, String str2, byte[] bArr) throws SQLException;

    void beginTransaction();

    void close();

    Cursor commonquery(boolean z, String str, String[] strArr, String str2, String[] strArr2, String str3, String str4, String str5, String str6);

    Statement compileStatement(String str);

    int delete(String str, String str2, String[] strArr);

    void endTransaction();

    void endTransaction(boolean z);

    void execSQL(String str) throws SQLException;

    void execSQL(String str, Object[] objArr) throws SQLException;

    SQLiteDatabase getAndroidSQLiteDatabase();

    com.huawei.hwsqlite.SQLiteDatabase getODMFSQLiteDatabase();

    String getPath();

    String[] getSQLTables(String str, int i, CancellationSignal cancellationSignal);

    boolean inTransaction();

    long insertOrThrow(String str, String str2, ContentValues contentValues);

    boolean isDbLockedByCurrentThread();

    boolean isOpen();

    Cursor query(String str, String[] strArr, String str2, String[] strArr2, String str3, String str4, String str5, String str6);

    Cursor query(boolean z, String str, String[] strArr, String str2, String[] strArr2, String str3, String str4, String str5, String str6);

    Cursor rawQuery(String str, String[] strArr);

    Cursor rawSelect(String str, String[] strArr, CancellationSignal cancellationSignal, boolean z);

    void removeAttachAlias(String str);

    void resetDatabaseEncryptKey(byte[] bArr, byte[] bArr2);

    void setTransactionSuccessful();

    int update(String str, ContentValues contentValues, String str2, String[] strArr);
}
