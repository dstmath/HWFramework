package java.io;

final class SerialCallbackContext {
    private final ObjectStreamClass desc;
    private final Object obj;
    private Thread thread = Thread.currentThread();

    public SerialCallbackContext(Object obj2, ObjectStreamClass desc2) {
        this.obj = obj2;
        this.desc = desc2;
    }

    public Object getObj() throws NotActiveException {
        checkAndSetUsed();
        return this.obj;
    }

    public ObjectStreamClass getDesc() {
        return this.desc;
    }

    public void check() throws NotActiveException {
        if (this.thread != null && this.thread != Thread.currentThread()) {
            throw new NotActiveException("expected thread: " + this.thread + ", but got: " + Thread.currentThread());
        }
    }

    private void checkAndSetUsed() throws NotActiveException {
        if (this.thread == Thread.currentThread()) {
            this.thread = null;
            return;
        }
        throw new NotActiveException("not in readObject invocation or fields already read");
    }

    public void setUsed() {
        this.thread = null;
    }
}
