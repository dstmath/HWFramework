package com.huawei.nb.utils.logger;

import android.util.Log;

class AndroidLogAdapter implements LogAdapter {
    AndroidLogAdapter() {
    }

    public void v(String tag, String message) {
        Log.v(tag, message);
    }

    public void d(String tag, String message) {
        Log.d(tag, message);
    }

    public void i(String tag, String message) {
        Log.i(tag, message);
    }

    public void w(String tag, String message) {
        Log.w(tag, message);
    }

    public void e(String tag, String message) {
        Log.e(tag, message);
    }
}
