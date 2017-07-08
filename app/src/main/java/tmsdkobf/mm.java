package tmsdkobf;

import tmsdk.common.exception.UnauthorizedCallerException;

/* compiled from: Unknown */
public class mm {
    public static void a(Class<?>... clsArr) throws UnauthorizedCallerException {
        Object obj = null;
        for (StackTraceElement stackTraceElement : Thread.currentThread().getStackTrace()) {
            for (Class name : clsArr) {
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
