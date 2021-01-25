package ohos.data;

public class IllegalPredicateException extends IllegalArgumentException {
    private static final long serialVersionUID = -5962988455024156349L;

    public IllegalPredicateException() {
    }

    public IllegalPredicateException(String str) {
        super(str);
    }

    public IllegalPredicateException(Throwable th) {
        super(th);
    }
}
