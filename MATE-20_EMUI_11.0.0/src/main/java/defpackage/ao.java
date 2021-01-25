package defpackage;

import android.util.Log;
import com.huawei.android.feature.BuildConfig;
import com.huawei.android.os.SystemPropertiesEx;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/* renamed from: ao  reason: default package */
public final class ao {
    public static String a(Throwable th) {
        StringBuilder sb = new StringBuilder();
        sb.append("Exception: ").append(th.getClass().getName()).append('\n');
        StackTraceElement[] stackTrace = th.getStackTrace();
        if (stackTrace == null) {
            return BuildConfig.FLAVOR;
        }
        for (StackTraceElement stackTraceElement : stackTrace) {
            sb.append(stackTraceElement.toString()).append('\n');
        }
        return sb.toString();
    }

    public static byte[] digest(byte[] bArr) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(bArr);
        } catch (NoSuchAlgorithmException e) {
            Log.e("PushLogSys", "NoSuchAlgorithmException" + e.getMessage());
            return new byte[0];
        }
    }

    public static boolean h() {
        return i() <= 24;
    }

    public static int i() {
        int i = SystemPropertiesEx.getInt("ro.build.hw_emui_api_level", 0);
        Log.i("PushLogSys", "the emui api level is ".concat(String.valueOf(i)));
        return i;
    }
}
