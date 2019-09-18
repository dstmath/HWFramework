package com.android.internal.os;

import android.util.Log;

class AndroidPrintStream extends LoggingPrintStream {
    private final int priority;
    private final String tag;

    public AndroidPrintStream(int priority2, String tag2) {
        if (tag2 != null) {
            this.priority = priority2;
            this.tag = tag2;
            return;
        }
        throw new NullPointerException("tag");
    }

    /* access modifiers changed from: protected */
    public void log(String line) {
        Log.println(this.priority, this.tag, line);
    }
}
