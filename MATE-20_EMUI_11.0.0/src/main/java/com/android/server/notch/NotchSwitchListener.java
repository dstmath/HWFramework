package com.android.server.notch;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;

public abstract class NotchSwitchListener {
    private ContentObserver mObserver = new ContentObserver(new Handler()) {
        /* class com.android.server.notch.NotchSwitchListener.AnonymousClass1 */

        @Override // android.database.ContentObserver
        public void onChange(boolean isSelfChange, Uri uri) {
            NotchSwitchListener.this.onChange();
        }
    };

    public abstract void onChange();

    public ContentObserver getObserver() {
        return this.mObserver;
    }
}
