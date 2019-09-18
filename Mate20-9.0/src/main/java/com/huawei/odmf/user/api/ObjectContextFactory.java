package com.huawei.odmf.user.api;

import android.net.Uri;
import android.os.Looper;
import com.huawei.odmf.core.AObjectContext;

public class ObjectContextFactory {
    public static ObjectContext openObjectContext(Uri path) {
        return AObjectContext.openObjectContext(path);
    }

    public static ObjectContext openObjectContext(Uri path, boolean openCache) {
        return AObjectContext.openObjectContext(path, openCache);
    }

    public static ObjectContext openObjectContext(Uri path, boolean openCache, Looper looper) {
        return AObjectContext.openObjectContext(path, openCache, looper);
    }
}
