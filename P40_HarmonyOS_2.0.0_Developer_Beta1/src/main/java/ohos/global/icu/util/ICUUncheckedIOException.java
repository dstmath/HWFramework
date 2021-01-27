package ohos.global.icu.util;

public class ICUUncheckedIOException extends RuntimeException {
    private static final long serialVersionUID = 1210263498513384449L;

    public ICUUncheckedIOException() {
    }

    public ICUUncheckedIOException(String str) {
        super(str);
    }

    public ICUUncheckedIOException(Throwable th) {
        super(th);
    }

    public ICUUncheckedIOException(String str, Throwable th) {
        super(str, th);
    }
}
