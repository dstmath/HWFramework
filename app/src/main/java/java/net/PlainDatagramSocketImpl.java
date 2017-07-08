package java.net;

import java.io.IOException;

class PlainDatagramSocketImpl extends AbstractPlainDatagramSocketImpl {
    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.net.PlainDatagramSocketImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.net.PlainDatagramSocketImpl.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: java.net.PlainDatagramSocketImpl.<clinit>():void");
    }

    private static native void init();

    protected native synchronized void bind0(int i, InetAddress inetAddress) throws SocketException;

    protected native void connect0(InetAddress inetAddress, int i) throws SocketException;

    protected native void datagramSocketClose();

    protected native void datagramSocketCreate() throws SocketException;

    protected native void disconnect0(int i);

    protected native byte getTTL() throws IOException;

    protected native int getTimeToLive() throws IOException;

    protected native void join(InetAddress inetAddress, NetworkInterface networkInterface) throws IOException;

    protected native void leave(InetAddress inetAddress, NetworkInterface networkInterface) throws IOException;

    protected native synchronized int peek(InetAddress inetAddress) throws IOException;

    protected native synchronized int peekData(DatagramPacket datagramPacket) throws IOException;

    protected native synchronized void receive0(DatagramPacket datagramPacket) throws IOException;

    protected native void send(DatagramPacket datagramPacket) throws IOException;

    protected native void setTTL(byte b) throws IOException;

    protected native void setTimeToLive(int i) throws IOException;

    protected native Object socketGetOption(int i) throws SocketException;

    protected native void socketSetOption(int i, Object obj) throws SocketException;

    PlainDatagramSocketImpl() {
    }
}
