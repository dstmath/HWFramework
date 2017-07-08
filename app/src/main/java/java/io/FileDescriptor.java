package java.io;

import android.system.Os;
import android.system.OsConstants;

public final class FileDescriptor {
    public static final FileDescriptor err = null;
    public static final FileDescriptor in = null;
    public static final FileDescriptor out = null;
    private int descriptor;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.io.FileDescriptor.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.io.FileDescriptor.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.io.FileDescriptor.<clinit>():void");
    }

    private static native boolean isSocket(int i);

    public native void sync() throws SyncFailedException;

    public FileDescriptor() {
        this.descriptor = -1;
    }

    private FileDescriptor(int descriptor) {
        this.descriptor = descriptor;
    }

    public boolean valid() {
        return this.descriptor != -1;
    }

    public final int getInt$() {
        return this.descriptor;
    }

    public final void setInt$(int fd) {
        this.descriptor = fd;
    }

    public boolean isSocket$() {
        return isSocket(this.descriptor);
    }

    private static FileDescriptor dupFd(int fd) {
        try {
            return new FileDescriptor(Os.fcntlInt(new FileDescriptor(fd), OsConstants.F_DUPFD_CLOEXEC, 0));
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
