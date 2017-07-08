package com.android.internal.os;

import android.util.Log;

class AndroidPrintStream extends LoggingPrintStream {
    private final int priority;
    private final String tag;

    public AndroidPrintStream(int priority, String tag) {
        if (tag == null) {
            throw new NullPointerException("tag");
        }
        this.priority = priority;
        this.tag = tag;
    }

    protected void log(String line) {
        Log.println(this.priority, this.tag, line);
    }
}
