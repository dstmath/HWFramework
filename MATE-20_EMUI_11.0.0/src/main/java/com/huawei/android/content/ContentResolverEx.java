package com.huawei.android.content;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.SyncStatusInfo;
import android.database.ContentObserver;
import android.net.Uri;

public class ContentResolverEx {
    public static SyncStatusInfoEx getSyncStatus(Account account, String authority) {
        SyncStatusInfo stausInfo = ContentResolver.getSyncStatus(account, authority);
        if (stausInfo != null) {
            return new SyncStatusInfoEx(stausInfo);
        }
        return null;
    }

    public static void registerContentObserver(ContentResolver contentResolver, Uri uri, boolean notifyForDescendents, ContentObserver observer, int userHandle) {
        if (contentResolver != null) {
            contentResolver.registerContentObserver(uri, notifyForDescendents, observer, userHandle);
        }
    }
}
