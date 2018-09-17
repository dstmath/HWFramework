package sun.nio.ch;

class NativeThread {
    static native long current();

    static native void signal(long j);

    NativeThread() {
    }
}
