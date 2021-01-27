package ohos.appexecfwk.utils;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogConstString;
import ohos.hiviewdfx.HiLogLabel;

public class AppLog {
    private static final HiLogLabel DEFAULT_LABEL = new HiLogLabel(3, 218108160, "AppExecFwk");

    public static int d(@HiLogConstString String str, Object... objArr) {
        return d(DEFAULT_LABEL, str, objArr);
    }

    public static int d(HiLogLabel hiLogLabel, @HiLogConstString String str, Object... objArr) {
        return HiLog.debug(hiLogLabel, str, objArr);
    }

    public static int i(@HiLogConstString String str, Object... objArr) {
        return i(DEFAULT_LABEL, str, objArr);
    }

    public static int i(HiLogLabel hiLogLabel, @HiLogConstString String str, Object... objArr) {
        return HiLog.info(hiLogLabel, str, objArr);
    }

    public static int w(@HiLogConstString String str, Object... objArr) {
        return w(DEFAULT_LABEL, str, objArr);
    }

    public static int w(HiLogLabel hiLogLabel, @HiLogConstString String str, Object... objArr) {
        return HiLog.warn(hiLogLabel, str, objArr);
    }

    public static int e(@HiLogConstString String str, Object... objArr) {
        return e(DEFAULT_LABEL, str, objArr);
    }

    public static int e(HiLogLabel hiLogLabel, @HiLogConstString String str, Object... objArr) {
        return HiLog.error(hiLogLabel, str, objArr);
    }
}
