package java.net;

import java.util.concurrent.atomic.AtomicInteger;

public class SocketCounter {
    private static final long SOCKET_LIMIT = 100;
    private static AtomicInteger count = new AtomicInteger(0);

    private static int getCount() {
        return count.get();
    }

    private static void increment() {
        count.incrementAndGet();
    }

    public static void decrement() {
        count.decrementAndGet();
    }

    private static void dumpStack() {
        System.out.println("dumpStack begin");
        new Throwable("stack dump").printStackTrace();
    }

    public static void incrementAndDumpStackIfOverload() {
        increment();
        if (System.DEBUG && ((long) getCount()) > SOCKET_LIMIT) {
            System.out.println("Socket Total counts = " + getCount());
            dumpStack();
        }
    }
}
