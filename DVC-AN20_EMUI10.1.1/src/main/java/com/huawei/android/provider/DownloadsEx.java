package com.huawei.android.provider;

import android.net.Uri;
import android.provider.Downloads;

public final class DownloadsEx {

    public static final class Impl {
        public static final String COLUMN_TITLE = "title";
        public static final Uri CONTENT_URI = Downloads.Impl.CONTENT_URI;
        public static final int STATUS_SUCCESS = 200;

        public static final Uri getPubliclyAccessibleDownloadsUri() {
            return Downloads.Impl.PUBLICLY_ACCESSIBLE_DOWNLOADS_URI;
        }

        public static String getImplID() {
            return "_id";
        }
    }
}
