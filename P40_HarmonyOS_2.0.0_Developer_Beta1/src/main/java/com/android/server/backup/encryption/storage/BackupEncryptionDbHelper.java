package com.android.server.backup.encryption.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

class BackupEncryptionDbHelper extends SQLiteOpenHelper {
    static final String DATABASE_NAME = "backupencryption.db";
    private static final int DATABASE_VERSION = 1;
    private static final String SQL_CREATE_TERTIARY_KEYS_ENTRY = "CREATE TABLE tertiary_keys ( _id INTEGER PRIMARY KEY,secondary_key_alias TEXT,package_name TEXT,wrapped_key_bytes BLOB,UNIQUE(secondary_key_alias,package_name))";
    private static final String SQL_DROP_TERTIARY_KEYS_ENTRY = "DROP TABLE IF EXISTS tertiary_keys";

    BackupEncryptionDbHelper(Context context) {
        super(context, DATABASE_NAME, (SQLiteDatabase.CursorFactory) null, 1);
    }

    public void resetDatabase() throws EncryptionDbException {
        SQLiteDatabase db = getWritableDatabaseSafe();
        db.execSQL(SQL_DROP_TERTIARY_KEYS_ENTRY);
        onCreate(db);
    }

    @Override // android.database.sqlite.SQLiteOpenHelper
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TERTIARY_KEYS_ENTRY);
    }

    @Override // android.database.sqlite.SQLiteOpenHelper
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DROP_TERTIARY_KEYS_ENTRY);
        onCreate(db);
    }

    @Override // android.database.sqlite.SQLiteOpenHelper
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DROP_TERTIARY_KEYS_ENTRY);
        onCreate(db);
    }

    public SQLiteDatabase getWritableDatabaseSafe() throws EncryptionDbException {
        try {
            return super.getWritableDatabase();
        } catch (SQLiteException e) {
            throw new EncryptionDbException(e);
        }
    }

    public SQLiteDatabase getReadableDatabaseSafe() throws EncryptionDbException {
        try {
            return super.getReadableDatabase();
        } catch (SQLiteException e) {
            throw new EncryptionDbException(e);
        }
    }
}
