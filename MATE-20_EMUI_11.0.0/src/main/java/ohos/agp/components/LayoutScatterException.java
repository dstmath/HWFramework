package ohos.agp.components;

public class LayoutScatterException extends RuntimeException {
    private static final long serialVersionUID = 2756194206854039042L;

    public LayoutScatterException(String str, Throwable th) {
        super(str, th);
    }

    public LayoutScatterException(String str) {
        super(str);
    }
}
