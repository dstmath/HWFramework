package sun.nio.ch;

public class NativeThread {
    public static native long current();

    public static native void signal(long j);
}
