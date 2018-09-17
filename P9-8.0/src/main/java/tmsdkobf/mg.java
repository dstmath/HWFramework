package tmsdkobf;

public final class mg {
    public static boolean bX(String str) {
        if (str == null || str.trim().length() < 3) {
            return false;
        }
        char[] cArr = new char[]{'/', '#', ',', ';', '.', '(', ')', 'N', '*'};
        char[] cArr2 = cArr;
        int length = cArr.length;
        for (int i = 0; i < length; i++) {
            if (str.indexOf(cArr2[i]) >= 0) {
                return false;
            }
        }
        return true;
    }
}
