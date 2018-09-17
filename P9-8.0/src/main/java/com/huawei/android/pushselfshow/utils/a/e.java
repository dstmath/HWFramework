package com.huawei.android.pushselfshow.utils.a;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import com.huawei.android.pushagent.a.a.c;
import com.huawei.android.pushselfshow.utils.a;

public class e {
    private static e a;

    private e() {
    }

    private c a(Context context) {
        if (a.e(context)) {
            c.a("PushSelfShowLog", "operate apk self database");
            return new h();
        } else if (!a.g(context)) {
            c.a("PushSelfShowLog", "operate sdk self database");
            return new h();
        } else if (a.h(context)) {
            c.a("PushSelfShowLog", "operate apk provider database");
            return new f();
        } else {
            c.a("PushSelfShowLog", "operate sdcard database");
            return new g(context);
        }
    }

    public static synchronized e a() {
        e eVar;
        synchronized (e.class) {
            if (a == null) {
                a = new e();
            }
            eVar = a;
        }
        return eVar;
    }

    public Cursor a(Context context, Uri uri, String str, String[] strArr) throws Exception {
        return a(context).a(context, uri, str, strArr);
    }

    public void a(Context context, Uri uri, String str, ContentValues contentValues) throws Exception {
        a(context).a(context, uri, str, contentValues);
    }

    public void a(Context context, i iVar) throws Exception {
        if (context == null || iVar == null) {
            c.d("PushSelfShowLog", "context is null");
        } else {
            a(context).a(context, iVar);
        }
    }
}
