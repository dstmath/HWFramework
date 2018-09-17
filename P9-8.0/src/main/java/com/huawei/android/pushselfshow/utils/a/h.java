package com.huawei.android.pushselfshow.utils.a;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;
import com.huawei.android.pushagent.a.a.c;
import com.huawei.systemmanager.rainbow.comm.request.util.RainbowRequestBasic.CheckVersionField;

public class h implements c {
    private String a;

    public h() {
        this.a = null;
        this.a = null;
    }

    protected h(String str) {
        this.a = null;
        this.a = str;
    }

    private static void a(Context context, SQLiteDatabase sQLiteDatabase, String str, ContentValues contentValues) throws Exception {
        if (context == null) {
            c.d("PushSelfShowLog", "context is null");
        } else if (sQLiteDatabase == null) {
            c.d("PushSelfShowLog", "db is null");
        } else if (TextUtils.isEmpty(str)) {
            c.d("PushSelfShowLog", "table is null");
        } else {
            Cursor cursor = null;
            try {
                cursor = sQLiteDatabase.query(str, null, null, null, null, null, null);
                if (cursor != null) {
                    int count = cursor.getCount();
                    c.a("PushSelfShowLog", "queryAndInsert, exist rowNumber:" + count);
                    if (count >= CheckVersionField.CHECK_VERSION_MAX_UPDATE_DAY) {
                        c.d("PushSelfShowLog", "queryAndInsert failed");
                    } else {
                        sQLiteDatabase.insert(str, null, contentValues);
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                    sQLiteDatabase.close();
                    return;
                }
                c.d("PushSelfShowLog", "cursor is null");
                if (cursor != null) {
                    cursor.close();
                }
                sQLiteDatabase.close();
            } catch (Throwable e) {
                c.d("PushSelfShowLog", e.toString(), e);
                if (cursor != null) {
                    cursor.close();
                }
                sQLiteDatabase.close();
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
                sQLiteDatabase.close();
                throw th;
            }
        }
    }

    public Cursor a(Context context, Uri uri, String str, String[] strArr) throws Exception {
        SQLiteDatabase readableDatabase = a(context).getReadableDatabase();
        if (readableDatabase != null) {
            try {
                return readableDatabase.rawQuery(str, strArr);
            } catch (Throwable e) {
                c.d("PushSelfShowLog", e.toString(), e);
            }
        }
        return null;
    }

    b a(Context context) {
        return this.a != null ? b.a(context, this.a) : b.a(context);
    }

    public void a(Context context, Uri uri, String str, ContentValues contentValues) throws Exception {
        a(context, a(context).getWritableDatabase(), str, contentValues);
    }

    public void a(Context context, i iVar) throws Exception {
        if (context == null) {
            c.d("PushSelfShowLog", "context is null");
        } else if (iVar != null) {
            String b = iVar.b();
            String c = iVar.c();
            String[] d = iVar.d();
            if (b == null || b.length() == 0) {
                c.d("PushSelfShowLog", "table is null");
            } else if (c == null || c.length() == 0) {
                c.d("PushSelfShowLog", "whereClause is null");
            } else if (d == null || d.length == 0) {
                c.d("PushSelfShowLog", "whereArgs is null");
            } else {
                SQLiteDatabase writableDatabase = a(context).getWritableDatabase();
                if (writableDatabase != null) {
                    try {
                        writableDatabase.delete(b, c, d);
                    } catch (Throwable e) {
                        c.d("PushSelfShowLog", e.toString(), e);
                    } finally {
                        writableDatabase.close();
                    }
                }
            }
        } else {
            c.d("PushSelfShowLog", "sqlParam is null");
        }
    }
}
