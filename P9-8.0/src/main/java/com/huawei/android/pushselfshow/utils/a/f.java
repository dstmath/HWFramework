package com.huawei.android.pushselfshow.utils.a;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import com.huawei.android.pushagent.a.a.c;

public class f implements c {
    public Cursor a(Context context, Uri uri, String str, String[] strArr) throws Exception {
        return context.getContentResolver().query(uri, null, null, strArr, null);
    }

    public void a(Context context, Uri uri, String str, ContentValues contentValues) throws Exception {
        context.getContentResolver().insert(uri, contentValues);
    }

    public void a(Context context, i iVar) throws Exception {
        if (context == null) {
            c.d("PushSelfShowLog", "context is null");
        } else if (iVar != null) {
            Uri a = iVar.a();
            String c = iVar.c();
            String[] d = iVar.d();
            if (a == null) {
                c.d("PushSelfShowLog", "uri is null");
            } else if (c == null || c.length() == 0) {
                c.d("PushSelfShowLog", "whereClause is null");
            } else if (d == null || d.length == 0) {
                c.d("PushSelfShowLog", "whereArgs is null");
            } else {
                ContentResolver contentResolver = context.getContentResolver();
                if (contentResolver != null) {
                    contentResolver.delete(a, c, d);
                } else {
                    c.d("PushSelfShowLog", "resolver is null");
                }
            }
        } else {
            c.d("PushSelfShowLog", "sqlParam is null");
        }
    }
}
