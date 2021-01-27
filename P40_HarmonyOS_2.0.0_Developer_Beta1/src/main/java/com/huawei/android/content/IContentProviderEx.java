package com.huawei.android.content;

import android.content.Context;
import android.content.IContentProvider;
import android.os.Bundle;
import android.os.RemoteException;
import com.huawei.android.app.PackageManagerEx;

public class IContentProviderEx {
    private boolean mHasPermission = false;
    private IContentProvider mIContentProvider;

    public IContentProviderEx(Context context, IContentProvider contentProvider) {
        this.mIContentProvider = contentProvider;
        if (PackageManagerEx.hasSystemSignaturePermission(context)) {
            this.mHasPermission = true;
        }
    }

    public Bundle call(String callingPkg, String authority, String method, String arg, Bundle extras) throws RemoteException {
        if (!this.mHasPermission) {
            return new Bundle();
        }
        return this.mIContentProvider.call(callingPkg, authority, method, arg, extras);
    }
}
