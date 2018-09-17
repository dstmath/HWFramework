package com.android.server.security.securityprofile;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Slog;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

/* compiled from: DatabaseHelper */
class CategoryTableHelper {
    public static final String CATEGORY_TABLE = "category";
    private static final String[] COLUMNS_FOR_QUERY = new String[]{"package", "category"};
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_PACKAGE = "package";
    private SQLiteOpenHelper mOpenHelper = null;

    public CategoryTableHelper(SQLiteOpenHelper helper) {
        this.mOpenHelper = helper;
    }

    public Hashtable<String, Integer> readDatabase() {
        Hashtable<String, Integer> table = new Hashtable();
        try {
            Cursor cursor = null;
            try {
                cursor = this.mOpenHelper.getReadableDatabase().query("category", COLUMNS_FOR_QUERY, null, null, null, null, null);
                if (cursor == null) {
                    Slog.e(DatabaseHelper.TAG, "can not read database.");
                    if (cursor != null) {
                        cursor.close();
                    }
                    return table;
                }
                while (cursor.moveToNext()) {
                    table.put(cursor.getString(cursor.getColumnIndexOrThrow("package")), Integer.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("category"))));
                }
                if (cursor != null) {
                    cursor.close();
                }
                return table;
            } catch (IllegalArgumentException e) {
                Slog.e(DatabaseHelper.TAG, "find format errors in database.");
                table.clear();
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
                throw th;
            }
        } catch (SQLiteException e2) {
            Slog.e(DatabaseHelper.TAG, "can not open readable database.");
            return table;
        }
    }

    public void removeCategoryFromDatabase(List<String> packageList, int category) {
        try {
            SQLiteDatabase db = this.mOpenHelper.getWritableDatabase();
            Iterator packageName$iterator = packageList.iterator();
            while (packageName$iterator.hasNext()) {
                db.delete("category", "package=? and category=?", new String[]{(String) packageName$iterator.next(), String.valueOf(category)});
            }
        } catch (SQLiteException e) {
            Slog.e(DatabaseHelper.TAG, "can not open writable database.");
        }
    }

    public void storeCategoryToDatabase(List<String> packageNameList, int category) {
        try {
            SQLiteDatabase db = this.mOpenHelper.getWritableDatabase();
            db.beginTransaction();
            try {
                for (String packageName : packageNameList) {
                    ContentValues cv = new ContentValues();
                    cv.put("package", packageName);
                    cv.put("category", Integer.valueOf(category));
                    db.delete("category", "package=?", new String[]{packageName});
                    db.insert("category", null, cv);
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        } catch (SQLiteException e) {
            Slog.e(DatabaseHelper.TAG, "can not open writable database.");
        }
    }

    public void eraseBlacklistedFromDatabase(int category) {
        try {
            this.mOpenHelper.getWritableDatabase().delete("category", "category=?", new String[]{String.valueOf(category)});
        } catch (SQLiteException e) {
            Slog.e(DatabaseHelper.TAG, "can not open writable database.");
        }
    }
}
