package com.huawei.android.database;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;

public class ContentObserverEx {
    private ContentObserverBridge mBridge;

    public ContentObserverEx(Handler handler) {
        this.mBridge = new ContentObserverBridge(handler);
        this.mBridge.setContentObserverEx(this);
    }

    public ContentObserver getContentObserver() {
        return this.mBridge;
    }

    public void onChange(boolean isSelfChange, Uri uri, int userId) {
    }
}
