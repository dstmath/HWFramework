package com.huawei.android.content;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.net.Uri;

public class ContentResolverExt {
    public static void registerContentObserver(ContentResolver contentResolver, Uri uri, boolean notifyForDescendents, ContentObserver observer, int userHandle) {
        if (contentResolver != null) {
            contentResolver.registerContentObserver(uri, notifyForDescendents, observer, userHandle);
        }
    }

    public static boolean isProviderNull(ContentResolver contentResolver, Uri uri) {
        return contentResolver == null || contentResolver.acquireProvider(uri) == null;
    }
}
