package tmsdkobf;

import android.content.Context;

public class qg {
    public static Context Lj = null;
    static String Lk = "";
    private static qf Ll = null;
    static String dq = "";
    static String version = "";

    public static void a(int i, Object... objArr) {
        if (Ll != null) {
            Ll.a(i, objArr);
        }
    }

    public static void b(int i, Object... objArr) {
        if (Ll != null) {
            Ll.b(i, objArr);
        }
    }

    public static boolean bV(int i) {
        return Ll != null ? Ll.bV(i) : false;
    }

    public static void c(int i, Object... objArr) {
        if (Ll != null) {
            Ll.c(i, objArr);
        }
    }

    public static void d(int i, Object... objArr) {
        if (Ll != null) {
            Ll.d(i, objArr);
        }
    }
}
