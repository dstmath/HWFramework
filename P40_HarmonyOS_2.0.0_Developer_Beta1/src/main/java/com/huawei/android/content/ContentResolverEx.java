package com.huawei.android.content;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Context;
import android.content.IContentProvider;
import android.content.SyncStatusInfo;
import android.database.ContentObserver;
import android.net.Uri;
import com.huawei.android.app.PackageManagerEx;

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

    public static IContentProviderEx acquireUnstableProvider(Context context, ContentResolver contentResolver, Uri uri) {
        IContentProvider contentProvider;
        if (PackageManagerEx.hasSystemSignaturePermission(context) && (contentProvider = contentResolver.acquireUnstableProvider(uri)) != null) {
            return new IContentProviderEx(context, contentProvider);
        }
        return null;
    }
}
