package com.huawei.odmf.database;

import android.database.sqlite.SQLiteStatement;

public class AndroidSQLiteStatement implements Statement {
    private SQLiteStatement mSQLiteStatement;

    public AndroidSQLiteStatement(SQLiteStatement statement) {
        this.mSQLiteStatement = statement;
    }

    public long executeInsert() {
        return this.mSQLiteStatement.executeInsert();
    }

    public void execute() {
        this.mSQLiteStatement.execute();
    }

    public void bindLong(int index, long value) {
        this.mSQLiteStatement.bindLong(index, value);
    }

    public void clearBindings() {
        this.mSQLiteStatement.clearBindings();
    }

    public void executeUpdateDelete() {
        this.mSQLiteStatement.executeUpdateDelete();
    }

    public void bindNull(int index) {
        this.mSQLiteStatement.bindNull(index);
    }

    public void bindDouble(int index, double value) {
        this.mSQLiteStatement.bindDouble(index, value);
    }

    public void bindString(int index, String value) {
        this.mSQLiteStatement.bindString(index, value);
    }

    public void bindBlob(int index, byte[] value) {
        this.mSQLiteStatement.bindBlob(index, value);
    }

    public String toString() {
        return "AndroidSQLiteStatement{mSQLiteStatement=" + this.mSQLiteStatement + '}';
    }
}
