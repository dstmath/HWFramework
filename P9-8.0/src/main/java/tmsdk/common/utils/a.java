package tmsdk.common.utils;

abstract class a {
    a() {
    }

    public void b(String str, String str2, Throwable th) {
        d(5, str, str2 + 10 + f.getStackTraceString(th));
    }

    public void c(String str, String str2, Throwable th) {
        d(6, str, str2 + 10 + f.getStackTraceString(th));
    }

    abstract void d(int i, String str, String str2);

    public void d(String str, String str2) {
        d(3, str, str2);
    }

    public void n(String str, String str2) {
        d(4, str, str2);
    }

    public void o(String str, String str2) {
        d(6, str, str2);
    }

    public void r(String str, String str2) {
        d(2, str, str2);
    }

    public void s(String str, String str2) {
        d(5, str, str2);
    }
}
