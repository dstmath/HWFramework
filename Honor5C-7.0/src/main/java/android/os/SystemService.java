package android.os;

import java.util.HashMap;
import java.util.concurrent.TimeoutException;

public class SystemService {
    private static Object sPropertyLock;
    private static HashMap<String, State> sStates;

    public enum State {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.os.SystemService.State.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.os.SystemService.State.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.os.SystemService.State.<clinit>():void");
        }

        private State(String state) {
            SystemService.sStates.put(state, this);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.os.SystemService.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.os.SystemService.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.os.SystemService.<clinit>():void");
    }

    public SystemService() {
    }

    public static void start(String name) {
        SystemProperties.set("ctl.start", name);
    }

    public static void stop(String name) {
        SystemProperties.set("ctl.stop", name);
    }

    public static void restart(String name) {
        SystemProperties.set("ctl.restart", name);
    }

    public static State getState(String service) {
        State state = (State) sStates.get(SystemProperties.get("init.svc." + service));
        if (state != null) {
            return state;
        }
        return State.STOPPED;
    }

    public static boolean isStopped(String service) {
        return State.STOPPED.equals(getState(service));
    }

    public static boolean isRunning(String service) {
        return State.RUNNING.equals(getState(service));
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void waitForState(String service, State state, long timeoutMillis) throws TimeoutException {
        State currentState;
        long endMillis = SystemClock.elapsedRealtime() + timeoutMillis;
        while (true) {
            synchronized (sPropertyLock) {
                currentState = getState(service);
                if (state.equals(currentState)) {
                    return;
                } else if (SystemClock.elapsedRealtime() >= endMillis) {
                    break;
                } else {
                    try {
                        sPropertyLock.wait(timeoutMillis);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
        throw new TimeoutException("Service " + service + " currently " + currentState + "; waited " + timeoutMillis + "ms for " + state);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void waitForAnyStopped(String... services) {
        while (true) {
            synchronized (sPropertyLock) {
                for (String service : services) {
                    if (State.STOPPED.equals(getState(service))) {
                        return;
                    }
                }
                try {
                    sPropertyLock.wait();
                } catch (InterruptedException e) {
                }
            }
        }
    }
}
