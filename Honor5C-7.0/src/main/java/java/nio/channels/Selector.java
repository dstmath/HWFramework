package java.nio.channels;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.spi.SelectorProvider;
import java.util.Set;

public abstract class Selector implements Closeable {
    public abstract void close() throws IOException;

    public abstract boolean isOpen();

    public abstract Set<SelectionKey> keys();

    public abstract SelectorProvider provider();

    public abstract int select() throws IOException;

    public abstract int select(long j) throws IOException;

    public abstract int selectNow() throws IOException;

    public abstract Set<SelectionKey> selectedKeys();

    public abstract Selector wakeup();

    protected Selector() {
    }

    public static Selector open() throws IOException {
        return SelectorProvider.provider().openSelector();
    }
}
