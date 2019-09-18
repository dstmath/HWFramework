package com.android.server.security.securityprofile;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Slog;
import java.util.ArrayList;
import java.util.List;

class B200DatabaseImporter {
    public static final String CATEGORY_TABLE = "category";
    private static final String[] COLUMNS_FOR_QUERY = {"package", "category"};
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_PACKAGE = "package";
    private static final String DATABASE_NAME = "securityprofile.db";
    private static final String TAG = "SecurityProfileService";

    B200DatabaseImporter() {
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0059, code lost:
        if (r1 != null) goto L_0x005b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x005b, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0069, code lost:
        if (r1 == null) goto L_0x006c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x006c, code lost:
        r2.close();
        r13.deleteDatabase(DATABASE_NAME);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0075, code lost:
        return r0;
     */
    public static List<String> getBlackListAndDeleteDatabase(Context context) {
        List<String> blackList = new ArrayList<>();
        Cursor cursor = null;
        try {
            SQLiteDatabase db = SQLiteDatabase.openDatabase("/data/system/securityprofile.db", null, 1);
            try {
                String[] strArr = COLUMNS_FOR_QUERY;
                cursor = db.query("category", strArr, "category=" + String.valueOf(1), null, null, null, null);
                if (cursor == null) {
                    Slog.e(TAG, "can not read database.");
                    if (cursor != null) {
                        cursor.close();
                    }
                    return blackList;
                }
                while (cursor.moveToNext()) {
                    blackList.add(cursor.getString(cursor.getColumnIndexOrThrow("package")));
                }
            } catch (IllegalArgumentException e) {
                Slog.e(TAG, "find format errors in database.");
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
                throw th;
            }
        } catch (SQLiteException e2) {
            Slog.e(TAG, "can not open readable database.");
            return blackList;
        }
    }
}
