package tmsdkobf;

import tmsdk.common.ErrorCode;
import tmsdk.common.utils.f;

public class ne {
    public static final int bg(int i) {
        return i % 100;
    }

    public static final int bh(int i) {
        return (i % 10000) - bg(i);
    }

    public static final int bi(int i) {
        return ((i % 1000000) - bh(i)) - bg(i);
    }

    public static int bj(int -l_1_I) {
        if (-l_1_I == 0) {
            return -l_1_I;
        }
        int i;
        if (bg(-l_1_I) == 0) {
            switch (bi(-l_1_I)) {
                case -560000:
                case -170000:
                    i = -l_1_I - 15;
                    break;
                case -500000:
                case -490000:
                case -480000:
                case -470000:
                case -460000:
                case -450000:
                case -440000:
                case -430000:
                case -420000:
                case -410000:
                case -400000:
                case -390000:
                case -380000:
                case -370000:
                case -360000:
                case -350000:
                case -340000:
                case -330000:
                case -320000:
                case -310000:
                case -210000:
                case -190000:
                case -180000:
                case -150000:
                case -140000:
                case -120000:
                case -110000:
                case -100000:
                case -90000:
                case -80000:
                case -70000:
                case -60000:
                case -40000:
                case -10000:
                    i = -l_1_I - 3;
                    break;
                case -300000:
                case -290000:
                    i = -l_1_I - 16;
                    break;
                case -280000:
                case -250000:
                    i = -l_1_I - 5;
                    break;
                case -230000:
                    i = -l_1_I - 7;
                    break;
                case -220000:
                    i = -l_1_I - 2;
                    break;
                case -160000:
                    i = -l_1_I - 6;
                    break;
                case -130000:
                case -50000:
                    i = -l_1_I - 4;
                    break;
                default:
                    i = -l_1_I;
                    break;
            }
            if (i == -l_1_I) {
                switch (bh(-l_1_I)) {
                    case -1500:
                    case -900:
                    case -400:
                    case -300:
                        i = -l_1_I - 5;
                        break;
                    case -1400:
                    case -800:
                        i = -l_1_I - 3;
                        break;
                    case -1300:
                        i = -l_1_I - 9;
                        break;
                    case -1200:
                        i = -l_1_I - 14;
                        break;
                    case -1100:
                    case ErrorCode.ERR_OPEN_CONNECTION /*-1000*/:
                        i = -l_1_I - 13;
                        break;
                }
            }
        }
        i = -l_1_I;
        f.f("ESharkCode", "appendNormalCode: " + -l_1_I + " -> " + i);
        return i;
    }

    public static boolean bk(int i) {
        if (i == 0) {
            return false;
        }
        int bg = bg(i);
        if (bg == -17 || bg == -18 || bg == -7 || bg == -19 || bg == -2) {
            return false;
        }
        int bi = bi(i);
        return (bi == -220000 || bi == -230000 || bi == -160000 || bi == -540000) ? false : true;
    }

    public static final int f(String str, int i) {
        return str != null ? !str.contains("socket failed: EACCES (Permission denied)") ? !str.contains("Permission denied") ? !str.contains("isConnected failed: EHOSTUNREACH (No route to host)") ? !str.contains("No route to host") ? !str.contains("socket failed: ECONNRESET (Connection reset by peer)") ? !str.contains("setsockopt failed: ENOPROTOOPT (Protocol not available)") ? !str.contains("Protocol not available") ? !str.contains("Permission denied (missing INTERNET permission?)") ? !str.contains("failed: ENETUNREACH (Network is unreachable)") ? !str.contains("failed: ENOTSOCK (Socket operation on non-socket)") ? !str.contains("isConnected failed: ECONNREFUSED (Connection refused)") ? !str.contains("isConnected failed: ECONNRESET (Connection reset by peer)") ? !str.contains("connect failed: errno ") ? i : -490000 : -480000 : -470000 : -460000 : -450000 : -430000 : -410000 : -400000 : -390000 : -380000 : -370000 : -360000 : -350000 : i;
    }
}
