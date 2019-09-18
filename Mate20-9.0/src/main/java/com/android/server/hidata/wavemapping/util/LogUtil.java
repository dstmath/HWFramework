package com.android.server.hidata.wavemapping.util;

import android.text.TextUtils;
import android.util.Log;
import com.android.server.hidata.wavemapping.cons.Constant;

public class LogUtil {
    private static boolean debug_flag = false;
    private static boolean showD = true;
    private static boolean showE = true;
    private static boolean showI = false;
    private static boolean showV = false;
    private static boolean showW = true;
    private static boolean showWTF = true;
    private static final String tagPrefix = "WMapping.19020212.";

    private static String generateTag() {
        String str;
        String tag = "%s.%s(L:%d)";
        try {
            StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[4];
            if (stackTraceElement == null) {
                return tagPrefix;
            }
            String callerClazzName = stackTraceElement.getClassName();
            if (callerClazzName == null) {
                return tagPrefix;
            }
            String tag2 = String.format(tag, new Object[]{callerClazzName.substring(callerClazzName.lastIndexOf(".") + 1), stackTraceElement.getMethodName(), Integer.valueOf(stackTraceElement.getLineNumber())});
            if (TextUtils.isEmpty(tagPrefix)) {
                str = tag2;
            } else {
                str = tagPrefix + tag2;
            }
            tag = str;
            return tag;
        } catch (Exception e) {
            Log.e("PG.LogUtil", " e:" + e);
        }
    }

    public static void v(String msg) {
        try {
            if (showV) {
                Log.v(generateTag(), msg);
            }
        } catch (Exception e) {
            Log.e("PG.LogUtil", " e:" + e);
        }
    }

    public static void v(String msg, Throwable tr) {
        try {
            if (showV) {
                Log.v(generateTag(), msg, tr);
            }
        } catch (Exception e) {
            Log.e("PG.LogUtil", " e:" + e);
        }
    }

    public static void d(String msg) {
        try {
            if (showD) {
                Log.d(generateTag(), msg);
            }
        } catch (Exception e) {
            Log.e("PG.LogUtil", " e:" + e);
        }
    }

    public static void d(String msg, Throwable tr) {
        try {
            if (showD) {
                Log.d(generateTag(), msg, tr);
            }
        } catch (Exception e) {
            Log.e("PG.LogUtil", " e:" + e);
        }
    }

    public static void i(String msg) {
        try {
            if (showI) {
                Log.i(generateTag(), msg);
            }
        } catch (Exception e) {
            Log.e("PG.LogUtil", " e:" + e);
        }
    }

    public static void i(String msg, Throwable tr) {
        try {
            if (showI) {
                Log.i(generateTag(), msg, tr);
            }
        } catch (Exception e) {
            Log.e("PG.LogUtil", " e:" + e);
        }
    }

    public static void w(String msg) {
        try {
            if (showW) {
                Log.w(generateTag(), msg);
            }
        } catch (Exception e) {
            Log.e("PG.LogUtil", " e:" + e);
        }
    }

    public static void w(String msg, Throwable tr) {
        try {
            if (showW) {
                Log.w(generateTag(), msg, tr);
            }
        } catch (Exception e) {
            Log.e("PG.LogUtil", " e:" + e);
        }
    }

    public static void e(String msg) {
        try {
            if (showE) {
                Log.e(generateTag(), msg);
            }
        } catch (Exception e) {
            Log.e("PG.LogUtil", " e:" + e);
        }
    }

    public static void e(String msg, Throwable tr) {
        try {
            if (showE) {
                Log.e(generateTag(), msg, tr);
            }
        } catch (Exception e) {
            Log.e("PG.LogUtil", " e:" + e);
        }
    }

    public static void wtf(String msg) {
        try {
            if (showWTF) {
                Log.wtf(generateTag(), msg);
            }
        } catch (Exception e) {
            Log.e("PG.LogUtil", " e:" + e);
        }
    }

    public static void wtLogFile(String msg) {
        try {
            if (debug_flag) {
                String tag = generateTag();
                String logFilePath = Constant.getLogFilePath();
                FileUtils.writeFile(logFilePath, tag + msg);
                Log.i(tag, msg);
            }
        } catch (Exception e) {
            Log.e("PG.LogUtil", " e:" + e);
        }
    }

    public static void wtf(String msg, Throwable tr) {
        try {
            if (showWTF) {
                Log.wtf(generateTag(), msg, tr);
            }
        } catch (Exception e) {
            Log.e("PG.LogUtil", " e:" + e);
        }
    }

    public static boolean isShowV() {
        return showV;
    }

    public static void setShowV(boolean showV2) {
        showV = showV2;
    }

    public static boolean isShowD() {
        return showD;
    }

    public static void setShowD(boolean showD2) {
        showD = showD2;
    }

    public static boolean isShowI() {
        return showI;
    }

    public static void setShowI(boolean showI2) {
        showI = showI2;
    }

    public static boolean isShowW() {
        return showW;
    }

    public static void setShowW(boolean showW2) {
        showW = showW2;
    }

    public static boolean isShowE() {
        return showE;
    }

    public static void setShowE(boolean showE2) {
        showE = showE2;
    }

    public static boolean getDebug_flag() {
        return debug_flag;
    }

    public static void setDebug_flag(boolean flag) {
        debug_flag = flag;
    }
}
