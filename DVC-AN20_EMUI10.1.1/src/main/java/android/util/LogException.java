package android.util;

public interface LogException {
    public static final int LIBLOGPARAM_IS_INBLACKLIST = 2;
    public static final int LIBLOGPARAM_IS_SYSTEMAPP = 1;
    public static final String NO_VALUE = "";

    int cmd(String str, String str2);

    void initLogBlackList();

    boolean isInLogBlackList(String str);

    int msg(String str, int i, int i2, String str2, String str3);

    int msg(String str, int i, String str2, String str3);

    int setliblogparam(int i, String str);
}
