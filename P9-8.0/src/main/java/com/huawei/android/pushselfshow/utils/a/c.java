package com.huawei.android.pushselfshow.utils.a;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public interface c {
    Cursor a(Context context, Uri uri, String str, String[] strArr) throws Exception;

    void a(Context context, Uri uri, String str, ContentValues contentValues) throws Exception;

    void a(Context context, i iVar) throws Exception;
}
