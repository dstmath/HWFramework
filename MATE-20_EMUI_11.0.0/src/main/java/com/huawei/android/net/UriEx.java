package com.huawei.android.net;

import android.net.Uri;

public class UriEx {
    public static boolean isPathPrefixMatch(Uri baseUri, Uri prefix) {
        return baseUri.isPathPrefixMatch(prefix);
    }
}
