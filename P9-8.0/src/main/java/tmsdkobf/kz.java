package tmsdkobf;

import android.text.TextUtils;
import com.tencent.tcuser.util.a;
import java.util.List;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

public class kz {
    public static ky aJ(int i) {
        boolean z = false;
        Object obj = null;
        switch (i) {
            case 33:
                obj = dZ();
                break;
            case 90:
                obj = dY();
                break;
            case 139:
                obj = ek();
                break;
            case 141:
                obj = dU();
                break;
            case SmsCheckResult.ESCT_146 /*146*/:
                obj = dW();
                break;
            case 150:
                obj = ea();
                break;
            case 151:
                obj = ed();
                break;
            case SmsCheckResult.ESCT_163 /*163*/:
                obj = eg();
                break;
        }
        if (TextUtils.isEmpty(obj)) {
            return null;
        }
        ky kyVar = null;
        try {
            String[] split = obj.split(";");
            ky kyVar2 = new ky();
            try {
                kyVar2.xY = a.av(split[0]);
                if (a.av(split[1]) == 1) {
                    z = true;
                }
                kyVar2.xZ = z;
                if (split.length >= 3) {
                    kyVar2.ya = a.av(split[2]);
                }
                if (split.length >= 4) {
                    kyVar2.yb = split[3];
                }
                kyVar = kyVar2;
            } catch (Throwable th) {
                kyVar = kyVar2;
            }
        } catch (Throwable th2) {
        }
        return kyVar;
    }

    static void bA(String str) {
        new md("b_d_pre").a("f", str, true);
    }

    public static void bB(String str) {
        new md("b_d_pre").a("g", str, true);
    }

    public static void bC(String str) {
        new md("b_d_pre").a("h", str, true);
    }

    public static void bv(String str) {
        new md("b_d_pre").a("aaa", str, true);
    }

    static void bw(String str) {
        new md("b_d_pre").a("b", str, true);
    }

    static void bx(String str) {
        new md("b_d_pre").a("c", str, true);
    }

    static void by(String str) {
        new md("b_d_pre").a("d", str, true);
    }

    static void bz(String str) {
        new md("b_d_pre").a("e", str, true);
    }

    static String dU() {
        return new md("b_d_pre").getString("aaa", "141;1;;;");
    }

    public static long dV() {
        return new md("b_d_pre").getLong("aaar", -1);
    }

    static String dW() {
        return new md("b_d_pre").getString("b", "146;1;;;");
    }

    public static long dX() {
        return new md("b_d_pre").getLong("br", -1);
    }

    static String dY() {
        return new md("b_d_pre").getString("c", "90;0;;;");
    }

    static String dZ() {
        return new md("b_d_pre").getString("d", "33;0;;;");
    }

    static String ea() {
        return new md("b_d_pre").getString("e", "150;0;;;");
    }

    public static long eb() {
        return new md("b_d_pre").getLong("ea", -1);
    }

    public static long ec() {
        return new md("b_d_pre").getLong("eb", -1);
    }

    static String ed() {
        return new md("b_d_pre").getString("f", "151;0;;;");
    }

    public static long ee() {
        return new md("b_d_pre").getLong("fa", -1);
    }

    public static long ef() {
        return new md("b_d_pre").getLong("fb", -1);
    }

    public static String eg() {
        return new md("b_d_pre").getString("g", "163;0;;;");
    }

    public static long eh() {
        return new md("b_d_pre").getLong("ga", -1);
    }

    public static long ei() {
        return new md("b_d_pre").getLong("gb", -1);
    }

    public static long ej() {
        return new md("b_d_pre").getLong("gc", -1);
    }

    static String ek() {
        return new md("b_d_pre").getString("h", "139;0;;;");
    }

    public static void i(List<String> list) {
        if (list != null && !list.isEmpty()) {
            for (String str : list) {
                try {
                    String[] split = str.split(";");
                    switch (a.av(split[0])) {
                        case 33:
                            by(str);
                            if (a.av(split[1]) != 0) {
                                break;
                            }
                            la.bF(lg.getPath());
                            break;
                        case 90:
                            bx(str);
                            if (a.av(split[1]) != 0) {
                                break;
                            }
                            la.bF(lf.getPath());
                            break;
                        case 139:
                            bC(str);
                            break;
                        case 141:
                            bv(str);
                            break;
                        case SmsCheckResult.ESCT_146 /*146*/:
                            bw(str);
                            if (a.av(split[1]) != 0) {
                                break;
                            }
                            la.bF(lh.getPath());
                            break;
                        case 150:
                            bz(str);
                            if (a.av(split[1]) != 0) {
                                break;
                            }
                            la.bF(le.getPath());
                            break;
                        case 151:
                            bA(str);
                            if (a.av(split[1]) == 1) {
                                ld.et();
                                break;
                            }
                            ld.es();
                            la.bF(ld.getPath());
                            break;
                        case SmsCheckResult.ESCT_163 /*163*/:
                            bB(str);
                            if (a.av(split[1]) != 0) {
                                break;
                            }
                            la.bF(lc.getPath());
                            break;
                        default:
                            break;
                    }
                } catch (Throwable th) {
                }
            }
        }
    }

    public static void k(long j) {
        new md("b_d_pre").a("aaar", j, true);
    }

    public static void l(long j) {
        new md("b_d_pre").a("br", j, true);
    }

    public static void m(long j) {
        new md("b_d_pre").a("ea", j, true);
    }

    public static void n(long j) {
        new md("b_d_pre").a("eb", j, true);
    }

    public static void o(long j) {
        new md("b_d_pre").a("fa", j, true);
    }

    public static void p(long j) {
        new md("b_d_pre").a("fb", j, true);
    }

    public static void q(long j) {
        new md("b_d_pre").a("ga", j, true);
    }

    public static void r(long j) {
        new md("b_d_pre").a("gb", j, true);
    }

    public static void s(long j) {
        new md("b_d_pre").a("gc", j, true);
    }
}
