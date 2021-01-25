package ohos.global.resource;

public class AccessDeniedException extends Exception {
    private static final long serialVersionUID = -6755325501853334996L;

    public AccessDeniedException(String str) {
        super(str);
    }

    public AccessDeniedException() {
    }
}
