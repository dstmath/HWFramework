package com.android.okhttp.internal.framed;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public final class Ping {
    private final CountDownLatch latch;
    private long received;
    private long sent;

    Ping() {
        this.latch = new CountDownLatch(1);
        this.sent = -1;
        this.received = -1;
    }

    void send() {
        if (this.sent != -1) {
            throw new IllegalStateException();
        }
        this.sent = System.nanoTime();
    }

    void receive() {
        if (this.received != -1 || this.sent == -1) {
            throw new IllegalStateException();
        }
        this.received = System.nanoTime();
        this.latch.countDown();
    }

    void cancel() {
        if (this.received != -1 || this.sent == -1) {
            throw new IllegalStateException();
        }
        this.received = this.sent - 1;
        this.latch.countDown();
    }

    public long roundTripTime() throws InterruptedException {
        this.latch.await();
        return this.received - this.sent;
    }

    public long roundTripTime(long timeout, TimeUnit unit) throws InterruptedException {
        if (this.latch.await(timeout, unit)) {
            return this.received - this.sent;
        }
        return -2;
    }
}
