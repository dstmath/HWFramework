package com.huawei.server.security.securityprofile;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

class B200DatabaseImporter {
    private static final String CATEGORY_BLACKLISTED = "1";
    private static final String CATEGORY_TABLE = "category";
    private static final String[] COLUMNS_FOR_QUERY = {COLUMN_PACKAGE, "category"};
    private static final String COLUMN_CATEGORY = "category";
    private static final String COLUMN_PACKAGE = "package";
    private static final String DATABASE_NAME = "securityprofile.db";
    private static final boolean DEBUG = SecurityProfileUtils.DEBUG;
    private static final int DEFAULT_OLD_BLACK_APPS_CAPACITY = 16;
    private static final String TAG = "SecurityProfileService";

    B200DatabaseImporter() {
    }

    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0058, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0059, code lost:
        if (r3 != null) goto L_0x005b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:?, code lost:
        r3.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x005f, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0060, code lost:
        r4.addSuppressed(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0063, code lost:
        throw r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x006d, code lost:
        if (0 == 0) goto L_0x0070;
     */
    public static List<String> getBlackListAndDeleteDatabase(Context context) {
        List<String> blackList = new ArrayList<>(16);
        Cursor cursor = null;
        try {
            SQLiteDatabase db = SQLiteDatabase.openDatabase("/data/system/securityprofile.db", null, 1);
            cursor = db.query("category", COLUMNS_FOR_QUERY, "category=1", null, null, null, null);
            if (cursor == null) {
                Log.w(TAG, "can not read database.");
                db.close();
                if (cursor != null) {
                    cursor.close();
                }
                return blackList;
            }
            while (cursor.moveToNext()) {
                String packageName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PACKAGE));
                if (packageName != null) {
                    blackList.add(SecurityProfileUtils.replaceLineSeparator(packageName));
                }
            }
            db.close();
            cursor.close();
            context.deleteDatabase(DATABASE_NAME);
            return blackList;
        } catch (SQLiteException e) {
            Log.e(TAG, "can not open readable database.");
            if (0 != 0) {
                cursor.close();
            }
            return blackList;
        } catch (IllegalArgumentException e2) {
            Log.e(TAG, "find format errors in database.");
        } catch (Throwable th) {
            if (0 != 0) {
                cursor.close();
            }
            throw th;
        }
    }
}
