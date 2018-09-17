package android.util;

import android.os.Environment;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

public class HwLogExceptionInner implements LogException {
    public static final int LEVEL_A = 65;
    public static final int LEVEL_B = 66;
    public static final int LEVEL_C = 67;
    public static final int LEVEL_D = 68;
    private static final int LOG_ID_EXCEPTION = 5;
    public static final String TAG = "HwLogExceptionInner";
    private static Set<String> mLogBlackList = new HashSet();
    private static LogException mLogExceptionInner = null;

    public static native int println_exception_native(String str, int i, String str2, String str3);

    public static native int setliblogparam_native(int i, String str);

    static {
        System.loadLibrary("hwlog_jni");
    }

    public static synchronized LogException getInstance() {
        LogException logException;
        synchronized (HwLogExceptionInner.class) {
            if (mLogExceptionInner == null) {
                mLogExceptionInner = new HwLogExceptionInner();
            }
            logException = mLogExceptionInner;
        }
        return logException;
    }

    public int cmd(String tag, String contain) {
        return println_exception_native(tag, 0, "command", contain);
    }

    public int msg(String category, int level, String header, String body) {
        return println_exception_native(category, level, "message", header + 10 + body);
    }

    public int msg(String category, int level, int mask, String header, String body) {
        return println_exception_native(category, level, "message", "mask=" + mask + ";" + header + 10 + body);
    }

    public int setliblogparam(int paramid, String val) {
        return setliblogparam_native(paramid, val);
    }

    public void initLogBlackList() {
        initLogBlackList_static();
    }

    public boolean isInLogBlackList(String packageName) {
        return isInLogBlackList_static(packageName);
    }

    /* JADX WARNING: Removed duplicated region for block: B:22:0x005e A:{SYNTHETIC, Splitter: B:22:0x005e} */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0063 A:{SYNTHETIC, Splitter: B:25:0x0063} */
    /* JADX WARNING: Removed duplicated region for block: B:83:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x0068 A:{SYNTHETIC, Splitter: B:28:0x0068} */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x005e A:{SYNTHETIC, Splitter: B:22:0x005e} */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0063 A:{SYNTHETIC, Splitter: B:25:0x0063} */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x0068 A:{SYNTHETIC, Splitter: B:28:0x0068} */
    /* JADX WARNING: Removed duplicated region for block: B:83:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x00c1 A:{SYNTHETIC, Splitter: B:53:0x00c1} */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x00c6 A:{SYNTHETIC, Splitter: B:56:0x00c6} */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x00cb A:{SYNTHETIC, Splitter: B:59:0x00cb} */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x005e A:{SYNTHETIC, Splitter: B:22:0x005e} */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0063 A:{SYNTHETIC, Splitter: B:25:0x0063} */
    /* JADX WARNING: Removed duplicated region for block: B:83:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x0068 A:{SYNTHETIC, Splitter: B:28:0x0068} */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x00c1 A:{SYNTHETIC, Splitter: B:53:0x00c1} */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x00c6 A:{SYNTHETIC, Splitter: B:56:0x00c6} */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x00cb A:{SYNTHETIC, Splitter: B:59:0x00cb} */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x00c1 A:{SYNTHETIC, Splitter: B:53:0x00c1} */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x00c6 A:{SYNTHETIC, Splitter: B:56:0x00c6} */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x00cb A:{SYNTHETIC, Splitter: B:59:0x00cb} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void initLogBlackList_static() {
        Throwable th;
        File blackListFile = new File(Environment.getRootDirectory().getPath() + "/etc/hiview/log_blacklist.cfg");
        if (blackListFile.isFile() && blackListFile.canRead()) {
            BufferedReader in = null;
            InputStreamReader is = null;
            FileInputStream fi = null;
            try {
                FileInputStream fi2 = new FileInputStream(blackListFile);
                try {
                    InputStreamReader is2 = new InputStreamReader(fi2, "UTF-8");
                    try {
                        BufferedReader in2 = new BufferedReader(is2);
                        while (true) {
                            try {
                                String blackPackageName = in2.readLine();
                                if (blackPackageName == null) {
                                    break;
                                }
                                mLogBlackList.add(blackPackageName);
                            } catch (IOException e) {
                                fi = fi2;
                                is = is2;
                                in = in2;
                                try {
                                    Log.e(TAG, "initLogBlackList_static IOException");
                                    if (fi != null) {
                                    }
                                    if (is != null) {
                                    }
                                    if (in == null) {
                                    }
                                } catch (Throwable th2) {
                                    th = th2;
                                    if (fi != null) {
                                    }
                                    if (is != null) {
                                    }
                                    if (in != null) {
                                    }
                                    throw th;
                                }
                            } catch (Throwable th3) {
                                th = th3;
                                fi = fi2;
                                is = is2;
                                in = in2;
                                if (fi != null) {
                                    try {
                                        fi.close();
                                    } catch (IOException e2) {
                                        Log.e(TAG, "close fi IOException");
                                    }
                                }
                                if (is != null) {
                                    try {
                                        is.close();
                                    } catch (IOException e3) {
                                        Log.e(TAG, "close is IOException");
                                    }
                                }
                                if (in != null) {
                                    try {
                                        in.close();
                                    } catch (IOException e4) {
                                        Log.e(TAG, "close in IOException");
                                    }
                                }
                                throw th;
                            }
                        }
                        if (fi2 != null) {
                            try {
                                fi2.close();
                            } catch (IOException e5) {
                                Log.e(TAG, "close fi IOException");
                            }
                        }
                        if (is2 != null) {
                            try {
                                is2.close();
                            } catch (IOException e6) {
                                Log.e(TAG, "close is IOException");
                            }
                        }
                        if (in2 != null) {
                            try {
                                in2.close();
                            } catch (IOException e7) {
                                Log.e(TAG, "close in IOException");
                            }
                        }
                    } catch (IOException e8) {
                        fi = fi2;
                        is = is2;
                        Log.e(TAG, "initLogBlackList_static IOException");
                        if (fi != null) {
                        }
                        if (is != null) {
                        }
                        if (in == null) {
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        fi = fi2;
                        is = is2;
                        if (fi != null) {
                        }
                        if (is != null) {
                        }
                        if (in != null) {
                        }
                        throw th;
                    }
                } catch (IOException e9) {
                    fi = fi2;
                    Log.e(TAG, "initLogBlackList_static IOException");
                    if (fi != null) {
                    }
                    if (is != null) {
                    }
                    if (in == null) {
                    }
                } catch (Throwable th5) {
                    th = th5;
                    fi = fi2;
                    if (fi != null) {
                    }
                    if (is != null) {
                    }
                    if (in != null) {
                    }
                    throw th;
                }
            } catch (IOException e10) {
                Log.e(TAG, "initLogBlackList_static IOException");
                if (fi != null) {
                    try {
                        fi.close();
                    } catch (IOException e11) {
                        Log.e(TAG, "close fi IOException");
                    }
                }
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e12) {
                        Log.e(TAG, "close is IOException");
                    }
                }
                if (in == null) {
                    try {
                        in.close();
                    } catch (IOException e13) {
                        Log.e(TAG, "close in IOException");
                    }
                }
            }
        }
    }

    public static boolean isInLogBlackList_static(String packageName) {
        return mLogBlackList.contains(packageName);
    }
}
