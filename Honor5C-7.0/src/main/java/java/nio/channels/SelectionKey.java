package java.nio.channels;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

public abstract class SelectionKey {
    public static final int OP_ACCEPT = 16;
    public static final int OP_CONNECT = 8;
    public static final int OP_READ = 1;
    public static final int OP_WRITE = 4;
    private static final AtomicReferenceFieldUpdater<SelectionKey, Object> attachmentUpdater = null;
    private volatile Object attachment;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.nio.channels.SelectionKey.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.nio.channels.SelectionKey.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.nio.channels.SelectionKey.<clinit>():void");
    }

    public abstract void cancel();

    public abstract SelectableChannel channel();

    public abstract int interestOps();

    public abstract SelectionKey interestOps(int i);

    public abstract boolean isValid();

    public abstract int readyOps();

    public abstract Selector selector();

    protected SelectionKey() {
        this.attachment = null;
    }

    public final boolean isReadable() {
        return (readyOps() & OP_READ) != 0;
    }

    public final boolean isWritable() {
        return (readyOps() & OP_WRITE) != 0;
    }

    public final boolean isConnectable() {
        return (readyOps() & OP_CONNECT) != 0;
    }

    public final boolean isAcceptable() {
        return (readyOps() & OP_ACCEPT) != 0;
    }

    public final Object attach(Object ob) {
        return attachmentUpdater.getAndSet(this, ob);
    }

    public final Object attachment() {
        return this.attachment;
    }
}
