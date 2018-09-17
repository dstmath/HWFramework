package java.net;

import java.util.concurrent.atomic.AtomicInteger;

public class SocketCounter {
    private static final long SOCKET_LIMIT = 100;
    private static AtomicInteger count;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.net.SocketCounter.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.net.SocketCounter.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: java.net.SocketCounter.<clinit>():void");
    }

    private static int getCount() {
        return count.get();
    }

    private static void increment() {
        count.incrementAndGet();
    }

    public static void decrement() {
        count.decrementAndGet();
    }

    private static void dumpStack() {
        System.out.println("dumpStack begin");
        new Throwable("stack dump").printStackTrace();
    }

    public static void incrementAndDumpStackIfOverload() {
        increment();
        if (System.DEBUG && ((long) getCount()) > SOCKET_LIMIT) {
            System.out.println("Socket Total counts = " + getCount());
            dumpStack();
        }
    }
}
