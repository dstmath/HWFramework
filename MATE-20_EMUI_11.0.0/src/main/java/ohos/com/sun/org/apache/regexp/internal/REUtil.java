package ohos.com.sun.org.apache.regexp.internal;

public class REUtil {
    private static final String complexPrefix = "complex:";

    public static RE createRE(String str, int i) throws RESyntaxException {
        if (str.startsWith(complexPrefix)) {
            return new RE(str.substring(8), i);
        }
        return new RE(RE.simplePatternToFullRegularExpression(str), i);
    }

    public static RE createRE(String str) throws RESyntaxException {
        return createRE(str, 0);
    }
}
