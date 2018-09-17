package java.nio.channels;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.ByteBuffer;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.channels.spi.SelectorProvider;

public abstract class SocketChannel extends AbstractSelectableChannel implements ByteChannel, ScatteringByteChannel, GatheringByteChannel, NetworkChannel {
    static final /* synthetic */ boolean -assertionsDisabled = false;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.nio.channels.SocketChannel.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.nio.channels.SocketChannel.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.nio.channels.SocketChannel.<clinit>():void");
    }

    public abstract SocketChannel bind(SocketAddress socketAddress) throws IOException;

    public abstract boolean connect(SocketAddress socketAddress) throws IOException;

    public abstract boolean finishConnect() throws IOException;

    public abstract SocketAddress getRemoteAddress() throws IOException;

    public abstract boolean isConnected();

    public abstract boolean isConnectionPending();

    public abstract int read(ByteBuffer byteBuffer) throws IOException;

    public abstract long read(ByteBuffer[] byteBufferArr, int i, int i2) throws IOException;

    public abstract <T> SocketChannel setOption(SocketOption<T> socketOption, T t) throws IOException;

    public abstract SocketChannel shutdownInput() throws IOException;

    public abstract SocketChannel shutdownOutput() throws IOException;

    public abstract Socket socket();

    public abstract int write(ByteBuffer byteBuffer) throws IOException;

    public abstract long write(ByteBuffer[] byteBufferArr, int i, int i2) throws IOException;

    protected SocketChannel(SelectorProvider provider) {
        super(provider);
    }

    public static SocketChannel open() throws IOException {
        return SelectorProvider.provider().openSocketChannel();
    }

    public static SocketChannel open(SocketAddress remote) throws IOException {
        SocketChannel sc = open();
        try {
            sc.connect(remote);
            if (-assertionsDisabled || sc.isConnected()) {
                return sc;
            }
            throw new AssertionError();
        } catch (Throwable suppressed) {
            x.addSuppressed(suppressed);
        }
    }

    public final int validOps() {
        return 13;
    }

    public final long read(ByteBuffer[] dsts) throws IOException {
        return read(dsts, 0, dsts.length);
    }

    public final long write(ByteBuffer[] srcs) throws IOException {
        return write(srcs, 0, srcs.length);
    }
}
