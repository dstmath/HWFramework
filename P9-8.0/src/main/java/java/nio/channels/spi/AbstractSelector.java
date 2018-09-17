package java.nio.channels.spi;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import sun.nio.ch.Interruptible;

public abstract class AbstractSelector extends Selector {
    private final Set<SelectionKey> cancelledKeys = new HashSet();
    private Interruptible interruptor = null;
    private final SelectorProvider provider;
    private AtomicBoolean selectorOpen = new AtomicBoolean(true);

    protected abstract void implCloseSelector() throws IOException;

    protected abstract SelectionKey register(AbstractSelectableChannel abstractSelectableChannel, int i, Object obj);

    protected AbstractSelector(SelectorProvider provider) {
        this.provider = provider;
    }

    void cancel(SelectionKey k) {
        synchronized (this.cancelledKeys) {
            this.cancelledKeys.add(k);
        }
    }

    public final void close() throws IOException {
        if (this.selectorOpen.getAndSet(false)) {
            implCloseSelector();
        }
    }

    public final boolean isOpen() {
        return this.selectorOpen.get();
    }

    public final SelectorProvider provider() {
        return this.provider;
    }

    protected final Set<SelectionKey> cancelledKeys() {
        return this.cancelledKeys;
    }

    protected final void deregister(AbstractSelectionKey key) {
        ((AbstractSelectableChannel) key.channel()).removeKey(key);
    }

    protected final void begin() {
        if (this.interruptor == null) {
            this.interruptor = new Interruptible() {
                public void interrupt(Thread ignore) {
                    AbstractSelector.this.wakeup();
                }
            };
        }
        AbstractInterruptibleChannel.blockedOn(this.interruptor);
        Thread me = Thread.currentThread();
        if (me.isInterrupted()) {
            this.interruptor.interrupt(me);
        }
    }

    protected final void end() {
        AbstractInterruptibleChannel.blockedOn(null);
    }
}
