package com.huawei.android.content;

import android.content.ContentProviderOperation;

public class ContentProviderOperationEx {
    public static final int TYPE_ASSERT = 4;
    public static final int TYPE_DELETE = 3;
    public static final int TYPE_INSERT = 1;
    public static final int TYPE_UPDATE = 2;

    public static int getType(ContentProviderOperation contentProviderOperation) {
        return contentProviderOperation.getType();
    }
}
