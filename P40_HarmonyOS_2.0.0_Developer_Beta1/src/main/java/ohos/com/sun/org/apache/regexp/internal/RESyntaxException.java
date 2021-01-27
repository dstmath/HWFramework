package ohos.com.sun.org.apache.regexp.internal;

public class RESyntaxException extends RuntimeException {
    public RESyntaxException(String str) {
        super("Syntax error: " + str);
    }
}
