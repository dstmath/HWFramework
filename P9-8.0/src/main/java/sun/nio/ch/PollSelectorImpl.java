package sun.nio.ch;

import java.io.IOException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;

class PollSelectorImpl extends AbstractPollSelectorImpl {
    private int fd0;
    private int fd1;
    private Object interruptLock = new Object();
    private boolean interruptTriggered = false;

    PollSelectorImpl(SelectorProvider sp) {
        super(sp, 1, 1);
        long pipeFds = IOUtil.makePipe(false);
        this.fd0 = (int) (pipeFds >>> 32);
        this.fd1 = (int) pipeFds;
        this.pollWrapper = new PollArrayWrapper(10);
        this.pollWrapper.initInterrupt(this.fd0, this.fd1);
        this.channelArray = new SelectionKeyImpl[10];
    }

    protected int doSelect(long timeout) throws IOException {
        if (this.channelArray == null) {
            throw new ClosedSelectorException();
        }
        processDeregisterQueue();
        try {
            begin();
            this.pollWrapper.poll(this.totalChannels, 0, timeout);
            processDeregisterQueue();
            int numKeysUpdated = updateSelectedKeys();
            if (this.pollWrapper.getReventOps(0) != 0) {
                this.pollWrapper.putReventOps(0, 0);
                synchronized (this.interruptLock) {
                    IOUtil.drain(this.fd0);
                    this.interruptTriggered = false;
                }
            }
            return numKeysUpdated;
        } finally {
            end();
        }
    }

    protected void implCloseInterrupt() throws IOException {
        synchronized (this.interruptLock) {
            this.interruptTriggered = true;
        }
        FileDispatcherImpl.closeIntFD(this.fd0);
        FileDispatcherImpl.closeIntFD(this.fd1);
        this.fd0 = -1;
        this.fd1 = -1;
        this.pollWrapper.release(0);
    }

    public Selector wakeup() {
        synchronized (this.interruptLock) {
            if (!this.interruptTriggered) {
                this.pollWrapper.interrupt();
                this.interruptTriggered = true;
            }
        }
        return this;
    }
}
