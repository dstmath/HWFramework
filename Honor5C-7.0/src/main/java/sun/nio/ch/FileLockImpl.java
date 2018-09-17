package sun.nio.ch;

import java.io.IOException;
import java.nio.channels.Channel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

public class FileLockImpl extends FileLock {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    private volatile boolean valid;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.nio.ch.FileLockImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.nio.ch.FileLockImpl.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: sun.nio.ch.FileLockImpl.<clinit>():void");
    }

    FileLockImpl(FileChannel channel, long position, long size, boolean shared) {
        super(channel, position, size, shared);
        this.valid = true;
    }

    public boolean isValid() {
        return this.valid;
    }

    void invalidate() {
        if (-assertionsDisabled || Thread.holdsLock(this)) {
            this.valid = false;
            return;
        }
        throw new AssertionError();
    }

    public synchronized void release() throws IOException {
        Channel ch = acquiredBy();
        if (!ch.isOpen()) {
            throw new ClosedChannelException();
        } else if (this.valid) {
            if (ch instanceof FileChannelImpl) {
                ((FileChannelImpl) ch).release(this);
                this.valid = false;
            } else {
                throw new AssertionError();
            }
        }
    }
}
