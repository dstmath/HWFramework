package com.huawei.android.content;

import android.content.AsyncTaskLoader;

public class AsyncTaskLoaderEx {
    public static void waitForLoader(AsyncTaskLoader loader) {
        loader.waitForLoader();
    }
}
