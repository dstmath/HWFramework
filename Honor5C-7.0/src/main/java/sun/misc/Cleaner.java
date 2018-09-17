package sun.misc;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.security.AccessController;
import java.security.PrivilegedAction;

public class Cleaner extends PhantomReference {
    private static final ReferenceQueue dummyQueue = null;
    private static Cleaner first;
    private Cleaner next;
    private Cleaner prev;
    private final Runnable thunk;

    /* renamed from: sun.misc.Cleaner.1 */
    class AnonymousClass1 implements PrivilegedAction<Void> {
        final /* synthetic */ Throwable val$x;

        AnonymousClass1(Throwable val$x) {
            this.val$x = val$x;
        }

        public Void run() {
            if (System.err != null) {
                new Error("Cleaner terminated abnormally", this.val$x).printStackTrace();
            }
            System.exit(1);
            return null;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.misc.Cleaner.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.misc.Cleaner.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: sun.misc.Cleaner.<clinit>():void");
    }

    private static synchronized Cleaner add(Cleaner cl) {
        synchronized (Cleaner.class) {
            if (first != null) {
                cl.next = first;
                first.prev = cl;
            }
            first = cl;
        }
        return cl;
    }

    private static synchronized boolean remove(Cleaner cl) {
        synchronized (Cleaner.class) {
            if (cl.next == cl) {
                return false;
            }
            if (first == cl) {
                if (cl.next != null) {
                    first = cl.next;
                } else {
                    first = cl.prev;
                }
            }
            if (cl.next != null) {
                cl.next.prev = cl.prev;
            }
            if (cl.prev != null) {
                cl.prev.next = cl.next;
            }
            cl.next = cl;
            cl.prev = cl;
            return true;
        }
    }

    private Cleaner(Object referent, Runnable thunk) {
        super(referent, dummyQueue);
        this.next = null;
        this.prev = null;
        this.thunk = thunk;
    }

    public static Cleaner create(Object ob, Runnable thunk) {
        if (thunk == null) {
            return null;
        }
        return add(new Cleaner(ob, thunk));
    }

    public void clean() {
        if (remove(this)) {
            try {
                this.thunk.run();
            } catch (Throwable x) {
                AccessController.doPrivileged(new AnonymousClass1(x));
            }
        }
    }
}
