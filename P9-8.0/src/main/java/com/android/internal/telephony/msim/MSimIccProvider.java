package com.android.internal.telephony.msim;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import com.android.internal.telephony.IccProvider;
import com.huawei.android.util.NoExtAPIException;

public class MSimIccProvider extends IccProvider {
    public Cursor query(Uri url, String[] projection, String selection, String[] selectionArgs, String sort) {
        throw new NoExtAPIException("method not supported.");
    }

    public String getType(Uri url) {
        throw new NoExtAPIException("method not supported.");
    }

    public Uri insert(Uri url, ContentValues initialValues) {
        throw new NoExtAPIException("method not supported.");
    }

    public int delete(Uri url, String where, String[] whereArgs) {
        throw new NoExtAPIException("method not supported.");
    }

    public int update(Uri url, ContentValues values, String where, String[] whereArgs) {
        throw new NoExtAPIException("method not supported.");
    }

    protected void log(String msg) {
        throw new NoExtAPIException("method not supported.");
    }
}
