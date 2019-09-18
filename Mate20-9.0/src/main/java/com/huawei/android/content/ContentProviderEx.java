package com.huawei.android.content;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.net.Uri;

public class ContentProviderEx {
    public static Uri maybeAddUserId(Uri uri, int userId) {
        return ContentProvider.maybeAddUserId(uri, userId);
    }

    public static ContentProvider acquireProviderAndCoerceToLocalContentProvider(ContentResolver contentResolver, String authority) {
        return ContentProvider.coerceToLocalContentProvider(contentResolver.acquireProvider(authority));
    }

    public static final void setAppOps(ContentProvider provider, int readOp, int writeOp) {
        if (provider != null) {
            provider.setAppOps(readOp, writeOp);
        }
    }
}
