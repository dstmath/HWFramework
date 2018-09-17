package java.nio.channels;

import java.io.IOException;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.channels.spi.SelectorProvider;

public abstract class Pipe {

    public static abstract class SinkChannel extends AbstractSelectableChannel implements WritableByteChannel, GatheringByteChannel {
        protected SinkChannel(SelectorProvider provider) {
            super(provider);
        }

        public final int validOps() {
            return 4;
        }
    }

    public static abstract class SourceChannel extends AbstractSelectableChannel implements ReadableByteChannel, ScatteringByteChannel {
        protected SourceChannel(SelectorProvider provider) {
            super(provider);
        }

        public final int validOps() {
            return 1;
        }
    }

    public abstract SinkChannel sink();

    public abstract SourceChannel source();

    protected Pipe() {
    }

    public static Pipe open() throws IOException {
        return SelectorProvider.provider().openPipe();
    }
}
