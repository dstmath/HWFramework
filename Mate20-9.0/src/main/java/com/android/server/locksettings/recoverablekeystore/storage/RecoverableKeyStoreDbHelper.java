package com.android.server.locksettings.recoverablekeystore.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

class RecoverableKeyStoreDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "recoverablekeystore.db";
    static final int DATABASE_VERSION = 4;
    private static final String SQL_CREATE_KEYS_ENTRY = "CREATE TABLE keys( _id INTEGER PRIMARY KEY,user_id INTEGER,uid INTEGER,alias TEXT,nonce BLOB,wrapped_key BLOB,platform_key_generation_id INTEGER,last_synced_at INTEGER,recovery_status INTEGER,UNIQUE(uid,alias))";
    private static final String SQL_CREATE_RECOVERY_SERVICE_METADATA_ENTRY = "CREATE TABLE recovery_service_metadata (_id INTEGER PRIMARY KEY,user_id INTEGER,uid INTEGER,snapshot_version INTEGER,should_create_snapshot INTEGER,active_root_of_trust TEXT,public_key BLOB,cert_path BLOB,cert_serial INTEGER,secret_types TEXT,counter_id INTEGER,server_params BLOB,UNIQUE(user_id,uid))";
    private static final String SQL_CREATE_ROOT_OF_TRUST_ENTRY = "CREATE TABLE root_of_trust (_id INTEGER PRIMARY KEY,user_id INTEGER,uid INTEGER,root_alias TEST,cert_path BLOB,cert_serial INTEGER,UNIQUE(user_id,uid,root_alias))";
    private static final String SQL_CREATE_USER_METADATA_ENTRY = "CREATE TABLE user_metadata( _id INTEGER PRIMARY KEY,user_id INTEGER UNIQUE,platform_key_generation_id INTEGER)";
    private static final String SQL_DELETE_KEYS_ENTRY = "DROP TABLE IF EXISTS keys";
    private static final String SQL_DELETE_RECOVERY_SERVICE_METADATA_ENTRY = "DROP TABLE IF EXISTS recovery_service_metadata";
    private static final String SQL_DELETE_ROOT_OF_TRUST_ENTRY = "DROP TABLE IF EXISTS root_of_trust";
    private static final String SQL_DELETE_USER_METADATA_ENTRY = "DROP TABLE IF EXISTS user_metadata";
    private static final String TAG = "RecoverableKeyStoreDbHp";

    RecoverableKeyStoreDbHelper(Context context) {
        super(context, DATABASE_NAME, null, 4);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_KEYS_ENTRY);
        db.execSQL(SQL_CREATE_USER_METADATA_ENTRY);
        db.execSQL(SQL_CREATE_RECOVERY_SERVICE_METADATA_ENTRY);
        db.execSQL(SQL_CREATE_ROOT_OF_TRUST_ENTRY);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.e(TAG, "Recreating recoverablekeystore after unexpected version downgrade.");
        dropAllKnownTables(db);
        onCreate(db);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            dropAllKnownTables(db);
            onCreate(db);
            return;
        }
        if (oldVersion < 3 && newVersion >= 3) {
            upgradeDbForVersion3(db);
            oldVersion = 3;
        }
        if (oldVersion < 4 && newVersion >= 4) {
            upgradeDbForVersion4(db);
            oldVersion = 4;
        }
        if (oldVersion != newVersion) {
            Log.e(TAG, "Failed to update recoverablekeystore database to the most recent version");
        }
    }

    private void dropAllKnownTables(SQLiteDatabase db) {
        db.execSQL(SQL_DELETE_KEYS_ENTRY);
        db.execSQL(SQL_DELETE_USER_METADATA_ENTRY);
        db.execSQL(SQL_DELETE_RECOVERY_SERVICE_METADATA_ENTRY);
        db.execSQL(SQL_DELETE_ROOT_OF_TRUST_ENTRY);
    }

    private void upgradeDbForVersion3(SQLiteDatabase db) {
        addColumnToTable(db, "recovery_service_metadata", "cert_path", "BLOB", null);
        addColumnToTable(db, "recovery_service_metadata", "cert_serial", "INTEGER", null);
    }

    private void upgradeDbForVersion4(SQLiteDatabase db) {
        Log.d(TAG, "Updating recoverable keystore database to version 4");
        db.execSQL(SQL_CREATE_ROOT_OF_TRUST_ENTRY);
        addColumnToTable(db, "recovery_service_metadata", "active_root_of_trust", "TEXT", null);
    }

    private static void addColumnToTable(SQLiteDatabase db, String tableName, String column, String columnType, String defaultStr) {
        Log.d(TAG, "Adding column " + column + " to " + tableName + ".");
        String alterStr = "ALTER TABLE " + tableName + " ADD COLUMN " + column + " " + columnType;
        if (defaultStr != null && !defaultStr.isEmpty()) {
            alterStr = alterStr + " DEFAULT " + defaultStr;
        }
        db.execSQL(alterStr + ";");
    }
}
