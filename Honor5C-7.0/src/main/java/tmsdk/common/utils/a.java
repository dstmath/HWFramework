package tmsdk.common.utils;

/* compiled from: Unknown */
abstract class a {
    a() {
    }

    public void a(String str, String str2, Throwable th) {
        d(4, str, str2 + '\n' + d.getStackTraceString(th));
    }

    public void b(String str, String str2, Throwable th) {
        d(6, str, str2 + '\n' + d.getStackTraceString(th));
    }

    abstract void d(int i, String str, String str2);

    public void d(String str, String str2) {
        d(3, str, str2);
    }

    public void h(String str, String str2) {
        d(10, str, str2);
    }

    public void r(String str, String str2) {
        d(6, str, str2);
    }

    public void s(String str, String str2) {
        d(4, str, str2);
    }

    public void w(String str, String str2) {
        d(2, str, str2);
    }

    public void x(String str, String str2) {
        d(5, str, str2);
    }
}
