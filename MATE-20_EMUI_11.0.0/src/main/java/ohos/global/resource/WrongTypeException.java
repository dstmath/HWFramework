package ohos.global.resource;

public class WrongTypeException extends Exception {
    private static final long serialVersionUID = 8301149934475361418L;

    public WrongTypeException(String str) {
        super(str);
    }

    public WrongTypeException() {
    }
}
