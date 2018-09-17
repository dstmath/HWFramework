package com.huawei.android.content;

import android.content.ContentProvider;
import android.net.Uri;

public class ContentProviderEx {
    public static Uri maybeAddUserId(Uri uri, int userId) {
        return ContentProvider.maybeAddUserId(uri, userId);
    }
}
