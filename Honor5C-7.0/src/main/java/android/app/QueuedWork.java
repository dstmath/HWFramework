package android.app;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QueuedWork {
    private static final ConcurrentLinkedQueue<Runnable> sPendingWorkFinishers = null;
    private static ExecutorService sSingleThreadExecutor;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.app.QueuedWork.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.app.QueuedWork.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.app.QueuedWork.<clinit>():void");
    }

    public static ExecutorService singleThreadExecutor() {
        ExecutorService executorService;
        synchronized (QueuedWork.class) {
            if (sSingleThreadExecutor == null) {
                sSingleThreadExecutor = Executors.newSingleThreadExecutor();
            }
            executorService = sSingleThreadExecutor;
        }
        return executorService;
    }

    public static void add(Runnable finisher) {
        sPendingWorkFinishers.add(finisher);
    }

    public static void remove(Runnable finisher) {
        sPendingWorkFinishers.remove(finisher);
    }

    public static void waitToFinish() {
        while (true) {
            Runnable toFinish = (Runnable) sPendingWorkFinishers.poll();
            if (toFinish != null) {
                toFinish.run();
            } else {
                return;
            }
        }
    }

    public static boolean hasPendingWork() {
        return !sPendingWorkFinishers.isEmpty();
    }
}
