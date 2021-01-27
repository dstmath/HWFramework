package com.huawei.android.database;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;

public class ContentObserverBridge extends ContentObserver {
    private ContentObserverEx mContentObserverEx;

    public ContentObserverBridge(Handler handler) {
        super(handler);
    }

    public void setContentObserverEx(ContentObserverEx contentObserverEx) {
        this.mContentObserverEx = contentObserverEx;
    }

    @Override // android.database.ContentObserver
    public void onChange(boolean isSelfChange, Uri uri, int userId) {
        ContentObserverEx contentObserverEx = this.mContentObserverEx;
        if (contentObserverEx != null) {
            contentObserverEx.onChange(isSelfChange, uri, userId);
        }
    }
}
