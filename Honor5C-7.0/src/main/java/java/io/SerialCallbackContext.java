package java.io;

final class SerialCallbackContext {
    private final ObjectStreamClass desc;
    private final Object obj;
    private Thread thread;

    public SerialCallbackContext(Object obj, ObjectStreamClass desc) {
        this.obj = obj;
        this.desc = desc;
        this.thread = Thread.currentThread();
    }

    public Object getObj() throws NotActiveException {
        checkAndSetUsed();
        return this.obj;
    }

    public ObjectStreamClass getDesc() {
        return this.desc;
    }

    private void checkAndSetUsed() throws NotActiveException {
        if (this.thread != Thread.currentThread()) {
            throw new NotActiveException("not in readObject invocation or fields already read");
        }
        this.thread = null;
    }

    public void setUsed() {
        this.thread = null;
    }
}
