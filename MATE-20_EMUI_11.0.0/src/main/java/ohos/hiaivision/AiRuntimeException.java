package ohos.hiaivision;

public class AiRuntimeException extends RuntimeException {
    private static final long serialVersionUID = 1672196516164843234L;

    public AiRuntimeException() {
    }

    public AiRuntimeException(int i) {
        super("got ai rumtime exception,errorCode = " + i);
    }

    public AiRuntimeException(String str) {
        super(str);
    }

    public AiRuntimeException(String str, Throwable th) {
        super(str, th);
    }
}
