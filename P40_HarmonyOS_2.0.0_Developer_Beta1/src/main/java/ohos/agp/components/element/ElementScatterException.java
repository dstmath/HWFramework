package ohos.agp.components.element;

public class ElementScatterException extends RuntimeException {
    private static final long serialVersionUID = -1827720339475480309L;

    public ElementScatterException(String str, Throwable th) {
        super(str, th);
    }

    public ElementScatterException(String str) {
        super(str);
    }
}
