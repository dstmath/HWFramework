package com.android.commands.monkey;

import android.util.Log;

public abstract class Logger {
    private static final String TAG = "Monkey";
    public static Logger err = new Logger() {
        public void println(String s) {
            if (stdout) {
                System.err.println(s);
            }
            if (logcat) {
                Log.w(Logger.TAG, s);
            }
        }
    };
    public static boolean logcat = true;
    public static Logger out = new Logger() {
        public void println(String s) {
            if (stdout) {
                System.out.println(s);
            }
            if (logcat) {
                Log.i(Logger.TAG, s);
            }
        }
    };
    public static boolean stdout = true;

    public abstract void println(String str);

    public static void error(String msg, Throwable t) {
        err.println(msg);
        err.println(Log.getStackTraceString(t));
    }
}
