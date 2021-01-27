package ohos.appexecfwk.utils;

public class StringUtils {
    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static boolean isBlank(String str) {
        if (!isEmpty(str) && !str.matches("\\s+")) {
            return false;
        }
        return true;
    }
}
