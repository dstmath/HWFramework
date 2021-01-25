package com.android.internal.os;

import android.annotation.UnsupportedAppUsage;
import android.os.DropBoxManager;
import android.util.Log;

/* access modifiers changed from: package-private */
public class AndroidPrintStream extends LoggingPrintStream {
    private final int priority;
    private final String tag;

    @UnsupportedAppUsage
    public AndroidPrintStream(int priority2, String tag2) {
        if (tag2 != null) {
            this.priority = priority2;
            this.tag = tag2;
            return;
        }
        throw new NullPointerException(DropBoxManager.EXTRA_TAG);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.os.LoggingPrintStream
    public void log(String line) {
        Log.println(this.priority, this.tag, line);
    }
}
