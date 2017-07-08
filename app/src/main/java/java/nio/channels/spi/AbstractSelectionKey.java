package java.nio.channels.spi;

import java.nio.channels.SelectionKey;

public abstract class AbstractSelectionKey extends SelectionKey {
    private volatile boolean valid;

    protected AbstractSelectionKey() {
        this.valid = true;
    }

    public final boolean isValid() {
        return this.valid;
    }

    void invalidate() {
        this.valid = false;
    }

    public final void cancel() {
        synchronized (this) {
            if (this.valid) {
                this.valid = false;
                ((AbstractSelector) selector()).cancel(this);
            }
        }
    }
}
