package com.huawei.server.security.permissionmanager.util;

import android.database.Cursor;
import com.huawei.android.util.SlogEx;

public class CursorHelper {
    private static final String TAG = "CursorHelper";

    private CursorHelper() {
        SlogEx.i(TAG, "create helper");
    }

    public static boolean checkCursorValid(Cursor cursor) {
        if (cursor == null || cursor.getCount() <= 0) {
            return false;
        }
        return true;
    }

    public static void closeCursor(Cursor cursor) {
        if (cursor != null) {
            cursor.close();
        }
    }
}
