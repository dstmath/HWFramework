package com.huawei.ohos.interwork;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import ohos.net.UriConverter;

public class DataAbilityResolver {
    private static final String SCHEME_ANDROID = "content";
    private static final String TAG = "DataAbilityResolver";
    private ContentResolver contentResolver;
    private Context context;

    private DataAbilityResolver(Context context2) {
        this.context = context2;
        this.contentResolver = context2.getContentResolver();
    }

    public static DataAbilityResolver creator(Context context2) {
        if (context2 != null) {
            return new DataAbilityResolver(context2);
        }
        Log.e(TAG, "creator context is null!");
        return null;
    }

    public void release() {
        this.contentResolver = null;
    }

    public void registerObserver(Uri uri, boolean z, ContentObserver contentObserver) throws IllegalArgumentException {
        ContentResolver contentResolver2 = this.contentResolver;
        if (contentResolver2 != null) {
            contentResolver2.registerContentObserver(getRealAndroidContentUir(uri), z, contentObserver);
        }
    }

    public void unregisterObserver(ContentObserver contentObserver) throws IllegalArgumentException {
        ContentResolver contentResolver2 = this.contentResolver;
        if (contentResolver2 != null) {
            contentResolver2.unregisterContentObserver(contentObserver);
        }
    }

    public void notifyChange(Uri uri, ContentObserver contentObserver) {
        ContentResolver contentResolver2 = this.contentResolver;
        if (contentResolver2 != null) {
            contentResolver2.notifyChange(uri, contentObserver);
        }
    }

    public Uri insert(Uri uri, ContentValues contentValues) {
        ContentResolver contentResolver2 = this.contentResolver;
        if (contentResolver2 != null) {
            return contentResolver2.insert(getRealAndroidContentUir(uri), contentValues);
        }
        Log.e(TAG, "insert context get contentResolver failed!");
        return null;
    }

    public int batchInsert(Uri uri, ContentValues[] contentValuesArr) {
        ContentResolver contentResolver2 = this.contentResolver;
        if (contentResolver2 != null) {
            return contentResolver2.bulkInsert(getRealAndroidContentUir(uri), contentValuesArr);
        }
        Log.e(TAG, "batchInsert context get contentResolver failed!");
        return 0;
    }

    public int delete(Uri uri, String str, String[] strArr) {
        ContentResolver contentResolver2 = this.contentResolver;
        if (contentResolver2 != null) {
            return contentResolver2.delete(getRealAndroidContentUir(uri), str, strArr);
        }
        Log.e(TAG, "delete context get contentResolver failed!");
        return 0;
    }

    public int update(Uri uri, ContentValues contentValues, String str, String[] strArr) {
        ContentResolver contentResolver2 = this.contentResolver;
        if (contentResolver2 == null) {
            return 0;
        }
        return contentResolver2.update(getRealAndroidContentUir(uri), contentValues, str, strArr);
    }

    public Cursor query(Uri uri, String[] strArr, String str, String[] strArr2, String str2) {
        ContentResolver contentResolver2 = this.contentResolver;
        if (contentResolver2 == null) {
            return null;
        }
        return contentResolver2.query(getRealAndroidContentUir(uri), strArr, str, strArr2, str2);
    }

    private static Uri getRealAndroidContentUir(Uri uri) {
        if (uri == null) {
            return null;
        }
        return SCHEME_ANDROID.equals(uri.getScheme()) ? uri : UriConverter.convertToAndroidContentUri(UriConverter.convertToZidaneUri(uri));
    }
}
