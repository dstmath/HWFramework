package sun.nio.ch;

import java.io.FileDescriptor;
import java.nio.channels.Pipe;
import java.nio.channels.Pipe.SinkChannel;
import java.nio.channels.Pipe.SourceChannel;
import java.nio.channels.spi.SelectorProvider;

class PipeImpl extends Pipe {
    private final SinkChannel sink;
    private final SourceChannel source;

    PipeImpl(SelectorProvider sp) {
        long pipeFds = IOUtil.makePipe(true);
        int readFd = (int) (pipeFds >>> 32);
        int writeFd = (int) pipeFds;
        FileDescriptor sourcefd = new FileDescriptor();
        IOUtil.setfdVal(sourcefd, readFd);
        this.source = new SourceChannelImpl(sp, sourcefd);
        FileDescriptor sinkfd = new FileDescriptor();
        IOUtil.setfdVal(sinkfd, writeFd);
        this.sink = new SinkChannelImpl(sp, sinkfd);
    }

    public SourceChannel source() {
        return this.source;
    }

    public SinkChannel sink() {
        return this.sink;
    }
}
