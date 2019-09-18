package defpackage;

import com.huawei.android.feature.BuildConfig;

/* renamed from: bb  reason: default package */
public final class bb {
    public static String a(Throwable th) {
        StringBuilder sb = new StringBuilder();
        sb.append("Exception: ").append(th.getClass().getName()).append(10);
        StackTraceElement[] stackTrace = th.getStackTrace();
        if (stackTrace == null) {
            return BuildConfig.FLAVOR;
        }
        for (StackTraceElement stackTraceElement : stackTrace) {
            sb.append(stackTraceElement.toString()).append(10);
        }
        return sb.toString();
    }
}
