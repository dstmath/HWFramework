package tmsdkobf;

import tmsdk.common.exception.UnauthorizedCallerException;

public class lo {
    public static void a(Class<?>... -l_7_R) throws UnauthorizedCallerException {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        Object obj = null;
        StackTraceElement[] stackTraceElementArr = stackTrace;
        int length = stackTrace.length;
        for (int i = 0; i < length; i++) {
            StackTraceElement stackTraceElement = stackTraceElementArr[i];
            for (Class name : -l_7_R) {
                if (stackTraceElement.getClassName().equals(name.getName())) {
                    obj = 1;
                    break;
                }
            }
        }
        if (obj == null) {
            throw new UnauthorizedCallerException();
        }
    }
}
