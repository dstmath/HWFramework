package sun.nio.ch;

import java.io.FileDescriptor;
import java.nio.channels.Pipe;
import java.nio.channels.spi.SelectorProvider;

class PipeImpl extends Pipe {
    private final Pipe.SinkChannel sink;
    private final Pipe.SourceChannel source;

    PipeImpl(SelectorProvider sp) {
        long pipeFds = IOUtil.makePipe(true);
        FileDescriptor sourcefd = new FileDescriptor();
        IOUtil.setfdVal(sourcefd, (int) (pipeFds >>> 32));
        this.source = new SourceChannelImpl(sp, sourcefd);
        FileDescriptor sinkfd = new FileDescriptor();
        IOUtil.setfdVal(sinkfd, (int) pipeFds);
        this.sink = new SinkChannelImpl(sp, sinkfd);
    }

    public Pipe.SourceChannel source() {
        return this.source;
    }

    public Pipe.SinkChannel sink() {
        return this.sink;
    }
}
