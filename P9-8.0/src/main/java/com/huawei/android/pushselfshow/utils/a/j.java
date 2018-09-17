package com.huawei.android.pushselfshow.utils.a;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.huawei.android.pushagent.a.a.c;
import java.io.File;

public class j {
    private static j a = new j();

    private j() {
    }

    private SQLiteDatabase a(String str) {
        File file = new File(str);
        if (file.exists()) {
            return SQLiteDatabase.openDatabase(str, null, 0);
        }
        File parentFile = file.getParentFile();
        if (!(parentFile == null || parentFile.exists() || !parentFile.mkdirs())) {
            c.e("PushLogSC2907", "datafiledir.mkdirs true");
        }
        return SQLiteDatabase.openOrCreateDatabase(str, null);
    }

    public static j a() {
        return a;
    }

    private void a(SQLiteDatabase sQLiteDatabase) {
        sQLiteDatabase.close();
    }

    public Cursor a(String str, String str2, String str3) {
        SQLiteDatabase a = a(str);
        if (a == null) {
            return null;
        }
        Cursor query = a.query(str2, null, str3, null, null, null, null);
        query.moveToFirst();
        a(a);
        return query;
    }

    public Cursor a(String str, String str2, String[] strArr) {
        SQLiteDatabase a = a(str);
        if (a == null) {
            return null;
        }
        Cursor rawQuery = a.rawQuery(str2, strArr);
        rawQuery.moveToFirst();
        a(a);
        return rawQuery;
    }

    public void a(Context context, String str, String str2) {
        SQLiteDatabase a = a(str);
        if (a != null) {
            a.execSQL(str2);
            a(a);
        }
    }

    public void a(Context context, String str, String str2, ContentValues contentValues) {
        SQLiteDatabase a = a(str);
        if (a != null) {
            a.insert(str2, null, contentValues);
            a(a);
        }
    }

    public void a(String str, String str2, String str3, String[] strArr) {
        SQLiteDatabase a = a(str);
        if (a != null) {
            a.delete(str2, str3, strArr);
            a(a);
        }
    }

    public boolean a(String str, String str2) {
        boolean z = false;
        Cursor a = a(str, "sqlite_master", "(tbl_name='" + str2 + "')");
        if (a != null) {
            int count = a.getCount();
            a.close();
            if (count > 0) {
                z = true;
            }
            return z;
        }
        c.a("PushLogSC2907", "cursor is null.");
        return false;
    }
}
