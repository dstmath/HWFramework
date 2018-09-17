package sun.nio.ch;

import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.AbstractSelectionKey;

class SelectionKeyImpl extends AbstractSelectionKey {
    final SelChImpl channel;
    private int index;
    private volatile int interestOps;
    private int readyOps;
    final SelectorImpl selector;

    java.nio.channels.SelectionKey nioInterestOps(int r1) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.nio.ch.SelectionKeyImpl.nioInterestOps(int):java.nio.channels.SelectionKey
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.nio.ch.SelectionKeyImpl.nioInterestOps(int):java.nio.channels.SelectionKey");
    }

    SelectionKeyImpl(SelChImpl ch, SelectorImpl sel) {
        this.channel = ch;
        this.selector = sel;
    }

    public SelectableChannel channel() {
        return (SelectableChannel) this.channel;
    }

    public Selector selector() {
        return this.selector;
    }

    int getIndex() {
        return this.index;
    }

    void setIndex(int i) {
        this.index = i;
    }

    private void ensureValid() {
        if (!isValid()) {
            throw new CancelledKeyException();
        }
    }

    public int interestOps() {
        ensureValid();
        return this.interestOps;
    }

    public SelectionKey interestOps(int ops) {
        ensureValid();
        return nioInterestOps(ops);
    }

    public int readyOps() {
        ensureValid();
        return this.readyOps;
    }

    void nioReadyOps(int ops) {
        this.readyOps = ops;
    }

    int nioReadyOps() {
        return this.readyOps;
    }

    int nioInterestOps() {
        return this.interestOps;
    }
}
