package java.nio.channels.spi;

import java.io.IOException;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.Channel;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.InterruptibleChannel;
import sun.nio.ch.Interruptible;

public abstract class AbstractInterruptibleChannel implements Channel, InterruptibleChannel {
    /* access modifiers changed from: private */
    public final Object closeLock = new Object();
    /* access modifiers changed from: private */
    public volatile Thread interrupted;
    private Interruptible interruptor;
    /* access modifiers changed from: private */
    public volatile boolean open = true;

    /* access modifiers changed from: protected */
    public abstract void implCloseChannel() throws IOException;

    protected AbstractInterruptibleChannel() {
    }

    public final void close() throws IOException {
        synchronized (this.closeLock) {
            if (this.open) {
                this.open = false;
                implCloseChannel();
            }
        }
    }

    public final boolean isOpen() {
        return this.open;
    }

    /* access modifiers changed from: protected */
    public final void begin() {
        if (this.interruptor == null) {
            this.interruptor = new Interruptible() {
                public void interrupt(Thread target) {
                    synchronized (AbstractInterruptibleChannel.this.closeLock) {
                        if (AbstractInterruptibleChannel.this.open) {
                            boolean unused = AbstractInterruptibleChannel.this.open = false;
                            Thread unused2 = AbstractInterruptibleChannel.this.interrupted = target;
                            try {
                                AbstractInterruptibleChannel.this.implCloseChannel();
                            } catch (IOException e) {
                            }
                        }
                    }
                }
            };
        }
        blockedOn(this.interruptor);
        Thread me = Thread.currentThread();
        if (me.isInterrupted()) {
            this.interruptor.interrupt(me);
        }
    }

    /* access modifiers changed from: protected */
    public final void end(boolean completed) throws AsynchronousCloseException {
        blockedOn(null);
        Thread interrupted2 = this.interrupted;
        if (interrupted2 != null && interrupted2 == Thread.currentThread()) {
            throw new ClosedByInterruptException();
        } else if (!completed && !this.open) {
            throw new AsynchronousCloseException();
        }
    }

    static void blockedOn(Interruptible intr) {
        Thread.currentThread().blockedOn(intr);
    }
}
