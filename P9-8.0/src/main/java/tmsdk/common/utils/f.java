package tmsdk.common.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public final class f {
    private static boolean LF = false;
    private static a LG = new j();

    public static void Q(boolean z) {
        LF = z;
        LG = !LF ? new j() : new g();
    }

    public static void b(String str, Object obj, Throwable th) {
        LG.c(str, c(obj), th);
    }

    private static String c(Object obj) {
        return obj != null ? !(obj instanceof String) ? !(obj instanceof Throwable) ? obj.toString() : getStackTraceString((Throwable) obj) : (String) obj : null;
    }

    public static void c(String str, Object obj, Throwable th) {
        LG.b(str, c(obj), th);
    }

    public static void d(String str, Object obj) {
        LG.d(str, c(obj));
    }

    public static void e(String str, Object obj) {
        LG.o(str, c(obj));
    }

    public static void f(String str, Object obj) {
        LG.n(str, c(obj));
    }

    public static void g(String str, Object obj) {
        LG.s(str, c(obj));
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

    public static void h(String str, Object obj) {
        LG.r(str, c(obj));
    }
}
