package com.huawei.android.pushagent.a;

import android.content.Context;
import android.provider.Settings.Secure;
import com.huawei.android.pushagent.a.a.c;
import com.huawei.android.pushagent.model.a.g;
import java.util.Arrays;

public class a {
    private static Context appCtx;
    private static final int[] hv = new int[]{30, 31, 32, 33, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 47, 46, 48, 49, 50, 52, 53, 54};
    private static final int[] hw = new int[]{70, 71, 72, 73, 74};
    private static final int[] hx = new int[]{90, 91, 92};
    private static final int[] hy = new int[]{0, 1, 10, 11, 12, 13};
    private static final int[] hz = new int[]{60, 61, 62, 65, 66};
    private static c ia;

    static {
        Arrays.sort(hy);
        Arrays.sort(hx);
        Arrays.sort(hv);
        Arrays.sort(hw);
        Arrays.sort(hz);
    }

    public static void ya(Context context) {
        xz(new b(context));
    }

    private static void xz(Runnable runnable) {
        if (runnable == null) {
            com.huawei.android.pushagent.utils.d.c.sf("PushLog2951", "runnable is null, stop execute");
            return;
        }
        try {
            com.huawei.android.pushagent.utils.threadpool.a.or(runnable);
        } catch (Exception e) {
            com.huawei.android.pushagent.utils.d.c.sf("PushLog2951", "fail to execute runnable!");
        }
    }

    public static void xy(boolean z) {
        xz(new c(z));
    }

    public static void xx(int i, String str) {
        xz(new d(i, str));
    }

    public static void xv(int i) {
        xx(i, "");
    }

    public static void yd() {
        xz(new e());
    }

    private static boolean yb(int i) {
        if (!yc(appCtx)) {
            return false;
        }
        if (Arrays.binarySearch(hy, i) >= 0) {
            if (1 == g.aq(appCtx).cv()) {
                return true;
            }
        } else if (Arrays.binarySearch(hx, i) >= 0) {
            if (1 == g.aq(appCtx).cw()) {
                return true;
            }
        } else if (Arrays.binarySearch(hv, i) >= 0) {
            if (1 == g.aq(appCtx).cx()) {
                return true;
            }
        } else if (Arrays.binarySearch(hw, i) >= 0) {
            if (1 == g.aq(appCtx).cy()) {
                return true;
            }
        } else if (Arrays.binarySearch(hz, i) >= 0) {
            if (1 == g.aq(appCtx).cz()) {
                return true;
            }
        } else if (1 == g.aq(appCtx).da()) {
            return true;
        }
        return false;
    }

    static boolean yc(Context context) {
        int i;
        boolean z;
        if (context != null) {
            try {
                i = Secure.getInt(context.getContentResolver(), "user_experience_involved", 0);
            } catch (Exception e) {
                com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", "getOpenUserExperience exception:" + e.toString());
                i = 0;
            }
        } else {
            i = 0;
        }
        if (i == 1) {
            z = true;
        } else {
            z = false;
        }
        com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", "isUserExperienceInvolved: " + z);
        return z;
    }

    public static String xw(String... strArr) {
        if (strArr == null) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < strArr.length; i++) {
            if (i != 0) {
                stringBuilder.append("^");
            }
            stringBuilder.append(strArr[i]);
        }
        return stringBuilder.toString();
    }
}
