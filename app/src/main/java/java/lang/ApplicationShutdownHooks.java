package java.lang;

import java.util.Collection;
import java.util.IdentityHashMap;

class ApplicationShutdownHooks {
    private static IdentityHashMap<Thread, Thread> hooks;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.lang.ApplicationShutdownHooks.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.lang.ApplicationShutdownHooks.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: java.lang.ApplicationShutdownHooks.<clinit>():void");
    }

    private ApplicationShutdownHooks() {
    }

    static synchronized void add(Thread hook) {
        synchronized (ApplicationShutdownHooks.class) {
            if (hooks == null) {
                throw new IllegalStateException("Shutdown in progress");
            } else if (hook.isAlive()) {
                throw new IllegalArgumentException("Hook already running");
            } else if (hooks.containsKey(hook)) {
                throw new IllegalArgumentException("Hook previously registered");
            } else {
                hooks.put(hook, hook);
            }
        }
    }

    static synchronized boolean remove(Thread hook) {
        boolean z;
        synchronized (ApplicationShutdownHooks.class) {
            if (hooks == null) {
                throw new IllegalStateException("Shutdown in progress");
            } else if (hook == null) {
                throw new NullPointerException();
            } else {
                z = hooks.remove(hook) != null;
            }
        }
        return z;
    }

    static void runHooks() {
        synchronized (ApplicationShutdownHooks.class) {
            Collection<Thread> threads = hooks.keySet();
            hooks = null;
        }
        for (Thread hook : threads) {
            hook.start();
        }
        for (Thread hook2 : threads) {
            try {
                hook2.join();
            } catch (InterruptedException e) {
            }
        }
    }
}
