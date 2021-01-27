package com.huawei.odmf.user.api;

import android.net.Uri;
import android.os.Looper;
import com.huawei.odmf.core.AObjectContext;

public final class ObjectContextFactory {
    private ObjectContextFactory() {
    }

    public static ObjectContext openObjectContext(Uri uri) {
        return AObjectContext.openObjectContext(uri);
    }

    public static ObjectContext openObjectContext(Uri uri, boolean z) {
        return AObjectContext.openObjectContext(uri, z);
    }

    public static ObjectContext openObjectContext(Uri uri, boolean z, Looper looper) {
        return AObjectContext.openObjectContext(uri, z, looper);
    }
}
