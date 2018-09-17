package dalvik.system;

import java.io.FileDescriptor;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;

public abstract class SocketTagger {
    private static SocketTagger tagger;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: dalvik.system.SocketTagger.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: dalvik.system.SocketTagger.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: dalvik.system.SocketTagger.<clinit>():void");
    }

    public abstract void tag(FileDescriptor fileDescriptor) throws SocketException;

    public abstract void untag(FileDescriptor fileDescriptor) throws SocketException;

    public final void tag(Socket socket) throws SocketException {
        if (!socket.isClosed()) {
            tag(socket.getFileDescriptor$());
        }
    }

    public final void untag(Socket socket) throws SocketException {
        if (!socket.isClosed()) {
            untag(socket.getFileDescriptor$());
        }
    }

    public final void tag(DatagramSocket socket) throws SocketException {
        if (!socket.isClosed()) {
            tag(socket.getFileDescriptor$());
        }
    }

    public final void untag(DatagramSocket socket) throws SocketException {
        if (!socket.isClosed()) {
            untag(socket.getFileDescriptor$());
        }
    }

    public static synchronized void set(SocketTagger tagger) {
        synchronized (SocketTagger.class) {
            if (tagger == null) {
                throw new NullPointerException("tagger == null");
            }
            tagger = tagger;
        }
    }

    public static synchronized SocketTagger get() {
        SocketTagger socketTagger;
        synchronized (SocketTagger.class) {
            socketTagger = tagger;
        }
        return socketTagger;
    }
}
