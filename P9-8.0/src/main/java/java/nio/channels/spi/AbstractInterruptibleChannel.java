package java.nio.channels.spi;

import java.io.IOException;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.Channel;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.InterruptibleChannel;
import sun.nio.ch.Interruptible;

public abstract class AbstractInterruptibleChannel implements Channel, InterruptibleChannel {
    private final Object closeLock = new Object();
    private volatile Thread interrupted;
    private Interruptible interruptor;
    private volatile boolean open = true;

    protected abstract void implCloseChannel() throws IOException;

    protected AbstractInterruptibleChannel() {
    }

    public final void close() throws IOException {
        synchronized (this.closeLock) {
            if (this.open) {
                this.open = false;
                implCloseChannel();
                return;
            }
        }
    }

    public final boolean isOpen() {
        return this.open;
    }

    protected final void begin() {
        if (this.interruptor == null) {
            this.interruptor = new Interruptible() {
                public void interrupt(Thread target) {
                    synchronized (AbstractInterruptibleChannel.this.closeLock) {
                        if (AbstractInterruptibleChannel.this.open) {
                            AbstractInterruptibleChannel.this.open = false;
                            AbstractInterruptibleChannel.this.interrupted = target;
                            try {
                                AbstractInterruptibleChannel.this.implCloseChannel();
                            } catch (IOException e) {
                            }
                        } else {
                            return;
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

    protected final void end(boolean completed) throws AsynchronousCloseException {
        blockedOn(null);
        Thread interrupted = this.interrupted;
        if (interrupted != null && interrupted == Thread.currentThread()) {
            throw new ClosedByInterruptException();
        } else if (!completed && (this.open ^ 1) != 0) {
            throw new AsynchronousCloseException();
        }
    }

    static void blockedOn(Interruptible intr) {
        Thread.currentThread().blockedOn(intr);
    }
}
