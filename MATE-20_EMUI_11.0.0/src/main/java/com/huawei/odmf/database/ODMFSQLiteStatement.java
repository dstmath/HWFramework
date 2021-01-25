package com.huawei.odmf.database;

import com.huawei.hwsqlite.SQLiteStatement;

public class ODMFSQLiteStatement implements Statement {
    private SQLiteStatement mODMFStatement;

    public ODMFSQLiteStatement(SQLiteStatement sQLiteStatement) {
        this.mODMFStatement = sQLiteStatement;
    }

    @Override // com.huawei.odmf.database.Statement
    public long executeInsert() {
        return this.mODMFStatement.executeInsert();
    }

    @Override // com.huawei.odmf.database.Statement
    public void execute() {
        this.mODMFStatement.execute();
    }

    @Override // com.huawei.odmf.database.Statement
    public void bindLong(int i, long j) {
        this.mODMFStatement.bindLong(i, j);
    }

    @Override // com.huawei.odmf.database.Statement
    public void clearBindings() {
        this.mODMFStatement.clearBindings();
    }

    @Override // com.huawei.odmf.database.Statement
    public void executeUpdateDelete() {
        this.mODMFStatement.executeUpdateDelete();
    }

    @Override // com.huawei.odmf.database.Statement
    public void close() {
        this.mODMFStatement.close();
    }

    @Override // com.huawei.odmf.database.Statement
    public void bindNull(int i) {
        this.mODMFStatement.bindNull(i);
    }

    @Override // com.huawei.odmf.database.Statement
    public void bindDouble(int i, double d) {
        this.mODMFStatement.bindDouble(i, d);
    }

    @Override // com.huawei.odmf.database.Statement
    public void bindString(int i, String str) {
        this.mODMFStatement.bindString(i, str);
    }

    @Override // com.huawei.odmf.database.Statement
    public void bindBlob(int i, byte[] bArr) {
        this.mODMFStatement.bindBlob(i, bArr);
    }

    public String toString() {
        return "ODMFSQLiteStatement{mODMFStatement=" + this.mODMFStatement + '}';
    }
}
