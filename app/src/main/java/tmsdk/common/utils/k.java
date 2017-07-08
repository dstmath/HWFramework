package tmsdk.common.utils;

/* compiled from: Unknown */
public class k {
    public static int dj(String str) {
        return str != null ? (str.startsWith("46000") || str.startsWith("46002") || str.startsWith("46007")) ? 0 : !str.startsWith("46001") ? !str.startsWith("46003") ? -1 : 2 : 1 : -1;
    }
}
