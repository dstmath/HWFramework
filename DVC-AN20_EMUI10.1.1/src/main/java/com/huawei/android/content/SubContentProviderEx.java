package com.huawei.android.content;

import android.content.ContentProvider;
import android.net.Uri;
import android.os.IBinder;

public abstract class SubContentProviderEx extends ContentProvider {
    /* access modifiers changed from: protected */
    public int enforceReadPermissionInner(Uri uri, String callingPkg, IBinder callerToken) {
        return super.enforceReadPermissionInner(uri, callingPkg, callerToken);
    }

    /* access modifiers changed from: protected */
    public int enforceWritePermissionInner(Uri uri, String callingPkg, IBinder callerToken) {
        return super.enforceWritePermissionInner(uri, callingPkg, callerToken);
    }
}
