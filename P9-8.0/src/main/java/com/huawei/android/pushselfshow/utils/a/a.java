package com.huawei.android.pushselfshow.utils.a;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import com.huawei.android.pushagent.a.a.c;
import java.io.File;
import java.util.ArrayList;

public class a {
    public static ArrayList a(Context context, String str) {
        ArrayList arrayList = new ArrayList();
        try {
            String c = c(context, "hwpushApp.db");
            if (TextUtils.isEmpty(c)) {
                c.a("PushSelfShowLog", "database is null,can't queryAppinfo");
                return arrayList;
            }
            c.a("PushSelfShowLog", "dbName path is " + c);
            if (j.a().a(c, "openmarket")) {
                String[] strArr = new String[]{str};
                Cursor a = j.a().a(c, "select * from openmarket where package = ?;", strArr);
                if (a != null) {
                    try {
                        if (a.getCount() > 0) {
                            while (true) {
                                String string = a.getString(a.getColumnIndex("msgid"));
                                arrayList.add(string);
                                c.a("TAG", "msgid and packageName is  " + string + "," + str);
                                if (a.moveToNext()) {
                                }
                            }
                        }
                        try {
                            a.close();
                            break;
                        } catch (Throwable e) {
                            c.e("PushSelfShowLog", "cursor.close() ", e);
                        }
                    } catch (Throwable e2) {
                        c.d("TAG", "queryAppinfo error " + e2.toString(), e2);
                        try {
                            a.close();
                        } catch (Throwable e22) {
                            c.e("PushSelfShowLog", "cursor.close() ", e22);
                        }
                    } catch (Throwable th) {
                        try {
                            a.close();
                        } catch (Throwable e3) {
                            c.e("PushSelfShowLog", "cursor.close() ", e3);
                        }
                        throw th;
                    }
                }
                c.a("PushSelfShowLog", "cursor is null.");
                return arrayList;
            }
            return arrayList;
        } catch (Throwable e4) {
            c.e("PushSelfShowLog", "queryAppinfo error", e4);
        }
    }

    public static void a(Context context, String str, String str2) {
        try {
            if (!context.getDatabasePath("hwpushApp.db").exists()) {
                context.openOrCreateDatabase("hwpushApp.db", 0, null).close();
            }
            String c = c(context, "hwpushApp.db");
            if (TextUtils.isEmpty(c)) {
                c.d("PushSelfShowLog", "database is null,can't insert appinfo into db");
                return;
            }
            c.a("PushSelfShowLog", "dbName path is " + c);
            if (!j.a().a(c, "openmarket")) {
                j.a().a(context, c, "create table openmarket(    _id INTEGER PRIMARY KEY AUTOINCREMENT,     msgid  TEXT,    package TEXT);");
            }
            ContentValues contentValues = new ContentValues();
            contentValues.put("msgid", str);
            contentValues.put("package", str2);
            j.a().a(context, c, "openmarket", contentValues);
        } catch (Throwable e) {
            c.e("PushSelfShowLog", "insertAppinfo error", e);
        }
    }

    public static void b(Context context, String str) {
        try {
            String c = c(context, "hwpushApp.db");
            if (TextUtils.isEmpty(c)) {
                c.d("PushSelfShowLog", "database is null,can't delete appinfo");
                return;
            }
            c.a("PushSelfShowLog", "dbName path is " + c);
            if (j.a().a(c, "openmarket")) {
                j.a().a(c, "openmarket", "package = ?", new String[]{str});
            }
        } catch (Throwable e) {
            c.e("PushSelfShowLog", "Delete Appinfo error", e);
        }
    }

    private static String c(Context context, String str) {
        String str2 = "";
        if (context == null) {
            return str2;
        }
        File databasePath = context.getDatabasePath("hwpushApp.db");
        if (databasePath.exists()) {
            str2 = databasePath.getAbsolutePath();
        }
        return str2;
    }
}
