package tmsdkobf;

import android.util.Log;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public final class mb {
    public static void a(String str, String str2, Throwable th) {
        if (isEnable()) {
            if (str2 == null) {
                str2 = "(null)";
            }
            Log.println(4, str, str2);
        }
    }

    public static void b(String str, String str2, Throwable th) {
        if (isEnable()) {
            if (str2 == null) {
                str2 = "(null)";
            }
            Log.println(5, str, str2);
        }
    }

    private static String c(Object obj) {
        return obj != null ? !(obj instanceof String) ? !(obj instanceof Throwable) ? obj.toString() : getStackTraceString((Throwable) obj) : (String) obj : null;
    }

    public static void c(String str, String str2, Throwable th) {
        if (isEnable()) {
            if (str2 == null) {
                str2 = "(null)";
            }
            Log.println(6, str, str2);
        }
    }

    public static void d(String str, Object obj) {
        d(str, c(obj));
    }

    public static void d(String str, String str2) {
        if (isEnable()) {
            Log.d(str, str2);
        }
    }

    public static void e(String str, Object obj) {
        o(str, c(obj));
    }

    public static String getStackTraceString(Throwable th) {
        if (th == null) {
            return "(Null stack trace)";
        }
        Writer stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        th.printStackTrace(printWriter);
        printWriter.flush();
        String stringWriter2 = stringWriter.toString();
        printWriter.close();
        return stringWriter2;
    }

    public static boolean isEnable() {
        return false;
    }

    public static void n(String str, String str2) {
        if (isEnable()) {
            Log.i(str, str2);
        }
    }

    public static void o(String str, String str2) {
        if (isEnable()) {
            Log.e(str, str2);
        }
    }

    public static void r(String str, String str2) {
        if (isEnable()) {
            Log.v(str, str2);
        }
    }

    public static void s(String str, String str2) {
        if (isEnable()) {
            Log.w(str, str2);
        }
    }
}
