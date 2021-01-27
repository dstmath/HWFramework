package com.huawei.server;

import com.android.internal.R;
import com.android.server.AttributeCache;

public class AttributeCacheEx {
    public static boolean isFloating(String pkgName, int realTheme, int userId) {
        AttributeCache.Entry ent = AttributeCache.instance().get(pkgName, realTheme, R.styleable.Window, userId);
        if (ent == null || !ent.array.getBoolean(4, false)) {
            return false;
        }
        return true;
    }
}
