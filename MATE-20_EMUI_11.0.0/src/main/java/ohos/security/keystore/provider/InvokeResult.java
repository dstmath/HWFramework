package ohos.security.keystore.provider;

public class InvokeResult<T> {
    private T result;
    private Throwable throwable;

    public T getResult() {
        Throwable th = this.throwable;
        if (!(th instanceof RuntimeException)) {
            return this.result;
        }
        throw ((RuntimeException) th);
    }

    public Throwable getThrowable() {
        return this.throwable;
    }

    public void setResult(T t) {
        this.result = t;
    }

    public void setThrowable(Throwable th) {
        this.throwable = th;
    }
}
