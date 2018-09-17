package com.android.server.security.securityprofile;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "securityprofile.db";
    private static final int DATABASE_VERSION = 1;
    public static final String TAG = "SecurityProfileDB";

    private void createTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE category (package TEXT PRIMARY KEY, category INTEGER);");
    }

    public void onCreate(SQLiteDatabase db) {
        createTables(db);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }
}
