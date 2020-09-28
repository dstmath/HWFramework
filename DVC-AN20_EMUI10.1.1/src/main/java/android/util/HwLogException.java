package android.util;

import com.huawei.android.smcs.SmartTrimProcessEvent;

public class HwLogException implements LogException {
    public static final int LEVEL_A = 65;
    public static final int LEVEL_B = 66;
    public static final int LEVEL_C = 67;
    public static final int LEVEL_D = 68;
    private static final int LOG_ID_EXCEPTION = 5;
    private static LogException mLogException = null;

    public static synchronized LogException getInstance() {
        LogException logException;
        synchronized (HwLogException.class) {
            if (mLogException == null) {
                mLogException = new HwLogException();
            }
            logException = mLogException;
        }
        return logException;
    }

    public int cmd(String tag, String contain) {
        return HwLogExceptionInner.println_exception_native(tag, 0, "command", contain);
    }

    public int msg(String category, int level, String header, String body) {
        return HwLogExceptionInner.println_exception_native(category, level, "message", header + '\n' + body);
    }

    public int msg(String category, int level, int mask, String header, String body) {
        return HwLogExceptionInner.println_exception_native(category, level, "message", "mask=" + mask + SmartTrimProcessEvent.ST_EVENT_INTER_STRING_TOKEN + header + '\n' + body);
    }

    public int setliblogparam(int paramid, String val) {
        return HwLogExceptionInner.setliblogparam_native(paramid, val);
    }

    public void initLogBlackList() {
        HwLogExceptionInner.initLogBlackList_static();
    }

    public boolean isInLogBlackList(String packageName) {
        return HwLogExceptionInner.isInLogBlackList_static(packageName);
    }
}
