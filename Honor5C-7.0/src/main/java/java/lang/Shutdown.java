package java.lang;

import sun.security.x509.GeneralNameInterface;

public class Shutdown {
    private static final int FINALIZERS = 2;
    private static final int HOOKS = 1;
    private static final int MAX_SYSTEM_HOOKS = 10;
    private static final int RUNNING = 0;
    private static int currentRunningHook;
    private static Object haltLock;
    private static final Runnable[] hooks = null;
    private static Object lock;
    private static boolean runFinalizersOnExit;
    private static int state;

    private static class Lock {
        private Lock() {
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.lang.Shutdown.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.lang.Shutdown.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: java.lang.Shutdown.<clinit>():void");
    }

    static native void halt0(int i);

    private static native void runAllFinalizers();

    static void setRunFinalizersOnExit(boolean run) {
        synchronized (lock) {
            runFinalizersOnExit = run;
        }
    }

    public static void add(int slot, boolean registerShutdownInProgress, Runnable hook) {
        synchronized (lock) {
            if (hooks[slot] != null) {
                throw new InternalError("Shutdown hook at slot " + slot + " already registered");
            }
            if (registerShutdownInProgress) {
                if (state > HOOKS || (state == HOOKS && slot <= currentRunningHook)) {
                    throw new IllegalStateException("Shutdown in progress");
                }
            } else if (state > 0) {
                throw new IllegalStateException("Shutdown in progress");
            }
            hooks[slot] = hook;
        }
    }

    private static void runHooks() {
        for (int i = 0; i < MAX_SYSTEM_HOOKS; i += HOOKS) {
            try {
                Runnable hook;
                synchronized (lock) {
                    currentRunningHook = i;
                    hook = hooks[i];
                }
                if (hook != null) {
                    hook.run();
                }
            } catch (Throwable t) {
                if (t instanceof ThreadDeath) {
                    ThreadDeath threadDeath = (ThreadDeath) t;
                }
            }
        }
    }

    static void halt(int status) {
        synchronized (haltLock) {
            halt0(status);
        }
    }

    private static void sequence() {
        synchronized (lock) {
            if (state != HOOKS) {
                return;
            }
            runHooks();
            synchronized (lock) {
                state = FINALIZERS;
                boolean rfoe = runFinalizersOnExit;
            }
            if (rfoe) {
                runAllFinalizers();
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static void exit(int status) {
        boolean runMoreFinalizers = false;
        synchronized (lock) {
            if (status != 0) {
                runFinalizersOnExit = false;
            }
            switch (state) {
                case GeneralNameInterface.NAME_MATCH /*0*/:
                    state = HOOKS;
                    break;
                case FINALIZERS /*2*/:
                    if (status == 0) {
                        runMoreFinalizers = runFinalizersOnExit;
                        break;
                    } else {
                        halt(status);
                        break;
                    }
            }
        }
        if (runMoreFinalizers) {
            runAllFinalizers();
            halt(status);
        }
        synchronized (Shutdown.class) {
            sequence();
            halt(status);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static void shutdown() {
        synchronized (lock) {
            switch (state) {
                case GeneralNameInterface.NAME_MATCH /*0*/:
                    state = HOOKS;
                    break;
            }
        }
        synchronized (Shutdown.class) {
            sequence();
        }
    }
}
