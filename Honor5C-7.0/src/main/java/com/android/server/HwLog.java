package com.android.server;

import android.util.Log;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.UnknownHostException;

public class HwLog {
    private static final String TAG = "Bluetooth_framework";

    public static void v(String tag, String msg) {
        i(tag, msg);
    }

    public static void v(String tag, String msg, Throwable tr) {
        i(tag, msg, tr);
    }

    public static void d(String tag, String msg) {
        i(tag, msg);
    }

    public static void d(String tag, String msg, Throwable tr) {
        i(tag, msg, tr);
    }

    public static void i(String tag, String msg) {
        Log.i(TAG, tag + ":" + msg);
    }

    public static void i(String tag, String msg, Throwable tr) {
        Log.i(TAG, tag + ":" + msg + '\n' + getStackTraceString(tr));
    }

    public static void w(String tag, String msg) {
        Log.w(TAG, tag + ":" + msg);
    }

    public static void w(String tag, String msg, Throwable tr) {
        Log.w(TAG, tag + ":" + msg + '\n' + getStackTraceString(tr));
    }

    public static void w(String tag, Throwable tr) {
        Log.w(TAG, tag + ":" + '\n' + getStackTraceString(tr));
    }

    public static void e(String tag, String msg) {
        Log.e(TAG, tag + ":" + msg);
    }

    public static void e(String tag, String msg, Throwable tr) {
        Log.e(TAG, tag + ":" + msg + '\n' + getStackTraceString(tr));
    }

    public static String getStackTraceString(Throwable tr) {
        if (tr == null) {
            return "";
        }
        for (Throwable t = tr; t != null; t = t.getCause()) {
            if (t instanceof UnknownHostException) {
                return "";
            }
        }
        StringWriter sw = new StringWriter();
        tr.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
