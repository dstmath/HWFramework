package com.huawei.odmf.database;

import android.database.sqlite.SQLiteStatement;

public class AndroidSQLiteStatement implements Statement {
    private SQLiteStatement mSQLiteStatement;

    public AndroidSQLiteStatement(SQLiteStatement sQLiteStatement) {
        this.mSQLiteStatement = sQLiteStatement;
    }

    @Override // com.huawei.odmf.database.Statement
    public long executeInsert() {
        return this.mSQLiteStatement.executeInsert();
    }

    @Override // com.huawei.odmf.database.Statement
    public void execute() {
        this.mSQLiteStatement.execute();
    }

    @Override // com.huawei.odmf.database.Statement
    public void bindLong(int i, long j) {
        this.mSQLiteStatement.bindLong(i, j);
    }

    @Override // com.huawei.odmf.database.Statement
    public void clearBindings() {
        this.mSQLiteStatement.clearBindings();
    }

    @Override // com.huawei.odmf.database.Statement
    public void executeUpdateDelete() {
        this.mSQLiteStatement.executeUpdateDelete();
    }

    @Override // com.huawei.odmf.database.Statement
    public void close() {
        this.mSQLiteStatement.close();
    }

    @Override // com.huawei.odmf.database.Statement
    public void bindNull(int i) {
        this.mSQLiteStatement.bindNull(i);
    }

    @Override // com.huawei.odmf.database.Statement
    public void bindDouble(int i, double d) {
        this.mSQLiteStatement.bindDouble(i, d);
    }

    @Override // com.huawei.odmf.database.Statement
    public void bindString(int i, String str) {
        this.mSQLiteStatement.bindString(i, str);
    }

    @Override // com.huawei.odmf.database.Statement
    public void bindBlob(int i, byte[] bArr) {
        this.mSQLiteStatement.bindBlob(i, bArr);
    }

    public String toString() {
        return "AndroidSQLiteStatement{mSQLiteStatement=" + this.mSQLiteStatement + '}';
    }
}
