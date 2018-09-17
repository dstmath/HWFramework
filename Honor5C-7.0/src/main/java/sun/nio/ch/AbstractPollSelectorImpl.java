package sun.nio.ch;

import java.io.IOException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;

abstract class AbstractPollSelectorImpl extends SelectorImpl {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    protected final int INIT_CAP;
    protected SelectionKeyImpl[] channelArray;
    protected int channelOffset;
    private Object closeLock;
    private boolean closed;
    PollArrayWrapper pollWrapper;
    protected int totalChannels;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.nio.ch.AbstractPollSelectorImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.nio.ch.AbstractPollSelectorImpl.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.nio.ch.AbstractPollSelectorImpl.<clinit>():void");
    }

    protected abstract int doSelect(long j) throws IOException;

    protected abstract void implCloseInterrupt() throws IOException;

    AbstractPollSelectorImpl(SelectorProvider sp, int channels, int offset) {
        super(sp);
        this.INIT_CAP = 10;
        this.channelOffset = 0;
        this.closed = false;
        this.closeLock = new Object();
        this.totalChannels = channels;
        this.channelOffset = offset;
    }

    void putEventOps(SelectionKeyImpl sk, int ops) {
        synchronized (this.closeLock) {
            if (this.closed) {
                throw new ClosedSelectorException();
            }
            this.pollWrapper.putEventOps(sk.getIndex(), ops);
        }
    }

    public Selector wakeup() {
        this.pollWrapper.interrupt();
        return this;
    }

    protected void implClose() throws IOException {
        synchronized (this.closeLock) {
            if (this.closed) {
                return;
            }
            this.closed = true;
            for (int i = this.channelOffset; i < this.totalChannels; i++) {
                SelectionKeyImpl ski = this.channelArray[i];
                if (!-assertionsDisabled) {
                    if ((ski.getIndex() != -1 ? 1 : null) == null) {
                        throw new AssertionError();
                    }
                }
                ski.setIndex(-1);
                deregister(ski);
                SelectableChannel selch = this.channelArray[i].channel();
                if (!(selch.isOpen() || selch.isRegistered())) {
                    ((SelChImpl) selch).kill();
                }
            }
            implCloseInterrupt();
            this.pollWrapper.free();
            this.pollWrapper = null;
            this.selectedKeys = null;
            this.channelArray = null;
            this.totalChannels = 0;
        }
    }

    protected int updateSelectedKeys() {
        int numKeysUpdated = 0;
        for (int i = this.channelOffset; i < this.totalChannels; i++) {
            int rOps = this.pollWrapper.getReventOps(i);
            if (rOps != 0) {
                SelectionKeyImpl sk = this.channelArray[i];
                this.pollWrapper.putReventOps(i, 0);
                if (!this.selectedKeys.contains(sk)) {
                    sk.channel.translateAndSetReadyOps(rOps, sk);
                    if ((sk.nioReadyOps() & sk.nioInterestOps()) != 0) {
                        this.selectedKeys.add(sk);
                        numKeysUpdated++;
                    }
                } else if (sk.channel.translateAndSetReadyOps(rOps, sk)) {
                    numKeysUpdated++;
                }
            }
        }
        return numKeysUpdated;
    }

    protected void implRegister(SelectionKeyImpl ski) {
        synchronized (this.closeLock) {
            if (this.closed) {
                throw new ClosedSelectorException();
            }
            if (this.channelArray.length == this.totalChannels) {
                int newSize = this.pollWrapper.totalChannels * 2;
                SelectionKeyImpl[] temp = new SelectionKeyImpl[newSize];
                for (int i = this.channelOffset; i < this.totalChannels; i++) {
                    temp[i] = this.channelArray[i];
                }
                this.channelArray = temp;
                this.pollWrapper.grow(newSize);
            }
            this.channelArray[this.totalChannels] = ski;
            ski.setIndex(this.totalChannels);
            this.pollWrapper.addEntry(ski.channel);
            this.totalChannels++;
            this.keys.add(ski);
        }
    }

    protected void implDereg(SelectionKeyImpl ski) throws IOException {
        Object obj = null;
        int i = ski.getIndex();
        if (!-assertionsDisabled) {
            if (i >= 0) {
                obj = 1;
            }
            if (obj == null) {
                throw new AssertionError();
            }
        }
        if (i != this.totalChannels - 1) {
            SelectionKeyImpl endChannel = this.channelArray[this.totalChannels - 1];
            this.channelArray[i] = endChannel;
            endChannel.setIndex(i);
            this.pollWrapper.release(i);
            PollArrayWrapper.replaceEntry(this.pollWrapper, this.totalChannels - 1, this.pollWrapper, i);
        } else {
            this.pollWrapper.release(i);
        }
        this.channelArray[this.totalChannels - 1] = null;
        this.totalChannels--;
        PollArrayWrapper pollArrayWrapper = this.pollWrapper;
        pollArrayWrapper.totalChannels--;
        ski.setIndex(-1);
        this.keys.remove(ski);
        this.selectedKeys.remove(ski);
        deregister(ski);
        SelectableChannel selch = ski.channel();
        if (!selch.isOpen() && !selch.isRegistered()) {
            ((SelChImpl) selch).kill();
        }
    }
}
