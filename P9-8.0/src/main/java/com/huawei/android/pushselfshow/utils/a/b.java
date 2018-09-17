package com.huawei.android.pushselfshow.utils.a;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.huawei.android.pushagent.a.a.c;

public class b extends SQLiteOpenHelper {
    private static b a = null;
    private static b b = null;

    private b(Context context) {
        super(context, "push.db", null, 1);
        c.a("PushSelfShowLog", "DBHelper instance, version is 1");
    }

    private b(Context context, String str) {
        super(context, str, null, 1);
        c.a("PushSelfShowLog", "DBHelper instance, version is 1");
    }

    public static synchronized b a(Context context) {
        synchronized (b.class) {
            b bVar;
            if (a == null) {
                a = new b(context);
                bVar = a;
                return bVar;
            }
            bVar = a;
            return bVar;
        }
    }

    public static synchronized b a(Context context, String str) {
        synchronized (b.class) {
            b bVar;
            if (b == null) {
                b = new b(context, str);
                bVar = b;
                return bVar;
            }
            bVar = b;
            return bVar;
        }
    }

    private void a(SQLiteDatabase sQLiteDatabase) {
        c.a("PushSelfShowLog", "updateVersionFrom0To1");
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put("token", " ".getBytes("UTF-8"));
            sQLiteDatabase.update("pushmsg", contentValues, null, null);
        } catch (Throwable e) {
            c.d("PushSelfShowLog", e.toString(), e);
        }
    }

    private boolean a(SQLiteDatabase sQLiteDatabase, String str) {
        boolean z = false;
        if (sQLiteDatabase == null) {
            return false;
        }
        String str2 = "(tbl_name='" + str + "')";
        Cursor cursor = null;
        try {
            cursor = sQLiteDatabase.query("sqlite_master", null, str2, null, null, null, null);
            if (cursor == null) {
                if (cursor != null) {
                    cursor.close();
                }
                return false;
            }
            cursor.moveToFirst();
            if (cursor.getCount() > 0) {
                z = true;
            }
            if (cursor != null) {
                cursor.close();
            }
            return z;
        } catch (Throwable e) {
            c.d("PushSelfShowLog", e.toString(), e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void onCreate(SQLiteDatabase sQLiteDatabase) {
        c.a("PushSelfShowLog", "onCreate");
        if (a(sQLiteDatabase, "pushmsg")) {
            c.a("PushSelfShowLog", "old table is exist");
            onUpgrade(sQLiteDatabase, 0, 1);
            return;
        }
        try {
            sQLiteDatabase.execSQL("create table notify(url  TEXT  PRIMARY KEY , bmp  BLOB );");
            sQLiteDatabase.execSQL("create table pushmsg( _id INTEGER PRIMARY KEY AUTOINCREMENT, url  TEXT  , token  BLOB ,msg  BLOB );");
        } catch (Throwable e) {
            c.d("PushSelfShowLog", e.toString(), e);
        }
    }

    public void onDowngrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        c.a("PushSelfShowLog", "onDowngrade,oldVersion:" + i + ",newVersion:" + i2);
    }

    public void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        c.a("PushSelfShowLog", "onUpgrade,oldVersion:" + i + ",newVersion:" + i2);
        if (i == 0) {
            a(sQLiteDatabase);
        }
    }
}
