package java.lang;

public class Object {
    private transient Class<?> shadow$_klass_;
    private transient int shadow$_monitor_;

    private static native int identityHashCodeNative(Object obj);

    private native Object internalClone();

    public final native void notify();

    public final native void notifyAll();

    public final native void wait() throws InterruptedException;

    public final native void wait(long j, int i) throws InterruptedException;

    public final Class<?> getClass() {
        return this.shadow$_klass_;
    }

    public int hashCode() {
        return identityHashCode(this);
    }

    static int identityHashCode(Object obj) {
        int lockWord = obj.shadow$_monitor_;
        if ((-1073741824 & lockWord) == Integer.MIN_VALUE) {
            return 268435455 & lockWord;
        }
        return identityHashCodeNative(obj);
    }

    public boolean equals(Object obj) {
        return this == obj;
    }

    protected Object clone() throws CloneNotSupportedException {
        if (this instanceof Cloneable) {
            return internalClone();
        }
        throw new CloneNotSupportedException("Class " + getClass().getName() + " doesn't implement Cloneable");
    }

    public String toString() {
        return getClass().getName() + "@" + Integer.toHexString(hashCode());
    }

    public final void wait(long millis) throws InterruptedException {
        wait(millis, 0);
    }

    protected void finalize() throws Throwable {
    }
}
