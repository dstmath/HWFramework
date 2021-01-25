package ohos.global.icu.impl;

public class InvalidFormatException extends Exception {
    static final long serialVersionUID = 8883328905089345791L;

    public InvalidFormatException() {
    }

    public InvalidFormatException(Throwable th) {
        super(th);
    }

    public InvalidFormatException(String str) {
        super(str);
    }
}
