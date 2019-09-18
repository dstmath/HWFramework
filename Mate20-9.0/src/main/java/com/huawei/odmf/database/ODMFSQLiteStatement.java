package com.huawei.odmf.database;

import com.huawei.hwsqlite.SQLiteStatement;

public class ODMFSQLiteStatement implements Statement {
    private SQLiteStatement mODMFStatement;

    public ODMFSQLiteStatement(SQLiteStatement statement) {
        this.mODMFStatement = statement;
    }

    public long executeInsert() {
        return this.mODMFStatement.executeInsert();
    }

    public void execute() {
        this.mODMFStatement.execute();
    }

    public void bindLong(int index, long value) {
        this.mODMFStatement.bindLong(index, value);
    }

    public void clearBindings() {
        this.mODMFStatement.clearBindings();
    }

    public void executeUpdateDelete() {
        this.mODMFStatement.executeUpdateDelete();
    }

    public void bindNull(int index) {
        this.mODMFStatement.bindNull(index);
    }

    public void bindDouble(int index, double value) {
        this.mODMFStatement.bindDouble(index, value);
    }

    public void bindString(int index, String value) {
        this.mODMFStatement.bindString(index, value);
    }

    public void bindBlob(int index, byte[] value) {
        this.mODMFStatement.bindBlob(index, value);
    }

    public String toString() {
        return "ODMFSQLiteStatement{mODMFStatement=" + this.mODMFStatement + '}';
    }
}
