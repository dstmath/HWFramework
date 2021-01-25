package ohos.data.orm;

public class StringUtils {
    private StringUtils() {
    }

    public static String surroundWithFunction(String str, String str2, String[] strArr) {
        StringBuilder sb = new StringBuilder(str);
        sb.append("(");
        boolean z = true;
        for (String str3 : strArr) {
            if (!z) {
                sb.append(" ");
                sb.append(str2);
                sb.append(" ");
            } else {
                z = false;
            }
            sb.append(str3);
        }
        sb.append(")");
        return sb.toString();
    }

    public static String surroundWithQuote(String str, String str2) {
        if (str == null || "".equals(str)) {
            return "";
        }
        return str2 + str + str2;
    }
}
