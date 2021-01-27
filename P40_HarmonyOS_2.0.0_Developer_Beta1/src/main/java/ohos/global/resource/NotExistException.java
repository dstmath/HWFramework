package ohos.global.resource;

public class NotExistException extends Exception {
    private static final long serialVersionUID = 2936836979364235008L;

    public NotExistException(String str) {
        super(str);
    }

    public NotExistException() {
    }
}
