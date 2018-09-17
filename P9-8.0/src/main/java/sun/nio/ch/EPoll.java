package sun.nio.ch;

import java.io.IOException;
import sun.misc.Unsafe;

class EPoll {
    static final int EPOLLONESHOT = 1073741824;
    static final int EPOLL_CTL_ADD = 1;
    static final int EPOLL_CTL_DEL = 2;
    static final int EPOLL_CTL_MOD = 3;
    private static final int OFFSETOF_EVENTS = eventsOffset();
    private static final int OFFSETOF_FD = dataOffset();
    private static final int SIZEOF_EPOLLEVENT = eventSize();
    private static final Unsafe unsafe = Unsafe.getUnsafe();

    private static native int dataOffset();

    static native int epollCreate() throws IOException;

    static native int epollCtl(int i, int i2, int i3, int i4);

    static native int epollWait(int i, long j, int i2) throws IOException;

    private static native int eventSize();

    private static native int eventsOffset();

    private EPoll() {
    }

    static long allocatePollArray(int count) {
        return unsafe.allocateMemory((long) (SIZEOF_EPOLLEVENT * count));
    }

    static void freePollArray(long address) {
        unsafe.freeMemory(address);
    }

    static long getEvent(long address, int i) {
        return ((long) (SIZEOF_EPOLLEVENT * i)) + address;
    }

    static int getDescriptor(long eventAddress) {
        return unsafe.getInt(((long) OFFSETOF_FD) + eventAddress);
    }

    static int getEvents(long eventAddress) {
        return unsafe.getInt(((long) OFFSETOF_EVENTS) + eventAddress);
    }
}
