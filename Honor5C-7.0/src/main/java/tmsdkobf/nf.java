package tmsdkobf;

/* compiled from: Unknown */
public final class nf {
    public static boolean cJ(String str) {
        if (str == null || str.trim().length() < 3) {
            return false;
        }
        for (char indexOf : new char[]{'/', '#', ',', ';', '.', '(', ')', 'N', '*'}) {
            if (str.indexOf(indexOf) >= 0) {
                return false;
            }
        }
        return true;
    }
}
