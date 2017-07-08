package java.lang;

import java.io.PrintStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Arrays;
import sun.misc.VM;

public class ThreadGroup implements UncaughtExceptionHandler {
    static final ThreadGroup mainThreadGroup = null;
    static final ThreadGroup systemThreadGroup = null;
    boolean daemon;
    boolean destroyed;
    ThreadGroup[] groups;
    int maxPriority;
    int nUnstartedThreads;
    String name;
    int ngroups;
    int nthreads;
    private final ThreadGroup parent;
    Thread[] threads;
    boolean vmAllowSuspension;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.lang.ThreadGroup.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.lang.ThreadGroup.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: java.lang.ThreadGroup.<clinit>():void");
    }

    private ThreadGroup() {
        this.nUnstartedThreads = 0;
        this.name = "system";
        this.maxPriority = 10;
        this.parent = null;
    }

    public ThreadGroup(String name) {
        this(Thread.currentThread().getThreadGroup(), name);
    }

    public ThreadGroup(ThreadGroup parent, String name) {
        this(checkParentAccess(parent), parent, name);
    }

    private ThreadGroup(Void unused, ThreadGroup parent, String name) {
        this.nUnstartedThreads = 0;
        this.name = name;
        this.maxPriority = parent.maxPriority;
        this.daemon = parent.daemon;
        this.vmAllowSuspension = parent.vmAllowSuspension;
        this.parent = parent;
        parent.add(this);
    }

    private static Void checkParentAccess(ThreadGroup parent) {
        parent.checkAccess();
        return null;
    }

    public final String getName() {
        return this.name;
    }

    public final ThreadGroup getParent() {
        if (this.parent != null) {
            this.parent.checkAccess();
        }
        return this.parent;
    }

    public final int getMaxPriority() {
        return this.maxPriority;
    }

    public final boolean isDaemon() {
        return this.daemon;
    }

    public synchronized boolean isDestroyed() {
        return this.destroyed;
    }

    public final void setDaemon(boolean daemon) {
        checkAccess();
        this.daemon = daemon;
    }

    public final void setMaxPriority(int pri) {
        ThreadGroup[] threadGroupArr;
        synchronized (this) {
            int min;
            checkAccess();
            if (pri < 1) {
                pri = 1;
            }
            if (pri > 10) {
                pri = 10;
            }
            if (this.parent != null) {
                min = Math.min(pri, this.parent.maxPriority);
            } else {
                min = pri;
            }
            this.maxPriority = min;
            int ngroupsSnapshot = this.ngroups;
            if (this.groups != null) {
                threadGroupArr = (ThreadGroup[]) Arrays.copyOf(this.groups, ngroupsSnapshot);
            } else {
                threadGroupArr = null;
            }
        }
        for (int i = 0; i < ngroupsSnapshot; i++) {
            threadGroupArr[i].setMaxPriority(pri);
        }
    }

    public final boolean parentOf(ThreadGroup g) {
        while (g != null) {
            if (g == this) {
                return true;
            }
            g = g.parent;
        }
        return false;
    }

    public final void checkAccess() {
    }

    public int activeCount() {
        synchronized (this) {
            if (this.destroyed) {
                return 0;
            }
            ThreadGroup[] threadGroupArr;
            int result = this.nthreads;
            int ngroupsSnapshot = this.ngroups;
            if (this.groups != null) {
                threadGroupArr = (ThreadGroup[]) Arrays.copyOf(this.groups, ngroupsSnapshot);
            } else {
                threadGroupArr = null;
            }
            for (int i = 0; i < ngroupsSnapshot; i++) {
                result += threadGroupArr[i].activeCount();
            }
            return result;
        }
    }

    public int enumerate(Thread[] list) {
        checkAccess();
        return enumerate(list, 0, true);
    }

    public int enumerate(Thread[] list, boolean recurse) {
        checkAccess();
        return enumerate(list, 0, recurse);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int enumerate(Thread[] list, int n, boolean recurse) {
        Throwable th;
        int ngroupsSnapshot = 0;
        ThreadGroup[] threadGroupArr = null;
        synchronized (this) {
            try {
                if (this.destroyed) {
                    return 0;
                }
                int nt = this.nthreads;
                if (nt > list.length - n) {
                    nt = list.length - n;
                }
                int i = 0;
                int n2 = n;
                while (i < nt) {
                    try {
                        if (this.threads[i].isAlive()) {
                            n = n2 + 1;
                            list[n2] = this.threads[i];
                        } else {
                            n = n2;
                        }
                        i++;
                        n2 = n;
                    } catch (Throwable th2) {
                        th = th2;
                        n = n2;
                    }
                }
                if (recurse) {
                    ngroupsSnapshot = this.ngroups;
                    if (this.groups != null) {
                        threadGroupArr = (ThreadGroup[]) Arrays.copyOf(this.groups, ngroupsSnapshot);
                    } else {
                        threadGroupArr = null;
                    }
                }
                if (recurse) {
                    n = n2;
                    for (i = 0; i < ngroupsSnapshot; i++) {
                        n = threadGroupArr[i].enumerate(list, n, true);
                    }
                } else {
                    n = n2;
                }
                return n;
            } catch (Throwable th3) {
                th = th3;
            }
        }
    }

    public int activeGroupCount() {
        synchronized (this) {
            if (this.destroyed) {
                return 0;
            }
            ThreadGroup[] threadGroupArr;
            int ngroupsSnapshot = this.ngroups;
            if (this.groups != null) {
                threadGroupArr = (ThreadGroup[]) Arrays.copyOf(this.groups, ngroupsSnapshot);
            } else {
                threadGroupArr = null;
            }
            int n = ngroupsSnapshot;
            for (int i = 0; i < ngroupsSnapshot; i++) {
                n += threadGroupArr[i].activeGroupCount();
            }
            return n;
        }
    }

    public int enumerate(ThreadGroup[] list) {
        checkAccess();
        return enumerate(list, 0, true);
    }

    public int enumerate(ThreadGroup[] list, boolean recurse) {
        checkAccess();
        return enumerate(list, 0, recurse);
    }

    private int enumerate(ThreadGroup[] list, int n, boolean recurse) {
        int ngroupsSnapshot = 0;
        ThreadGroup[] threadGroupArr = null;
        synchronized (this) {
            if (this.destroyed) {
                return 0;
            }
            int ng = this.ngroups;
            if (ng > list.length - n) {
                ng = list.length - n;
            }
            if (ng > 0) {
                System.arraycopy(this.groups, 0, (Object) list, n, ng);
                n += ng;
            }
            if (recurse) {
                ngroupsSnapshot = this.ngroups;
                if (this.groups != null) {
                    threadGroupArr = (ThreadGroup[]) Arrays.copyOf(this.groups, ngroupsSnapshot);
                } else {
                    threadGroupArr = null;
                }
            }
            if (recurse) {
                for (int i = 0; i < ngroupsSnapshot; i++) {
                    n = threadGroupArr[i].enumerate(list, n, true);
                }
            }
            return n;
        }
    }

    @Deprecated
    public final void stop() {
        if (stopOrSuspend(false)) {
            Thread.currentThread().stop();
        }
    }

    public final void interrupt() {
        int i;
        ThreadGroup[] threadGroupArr;
        synchronized (this) {
            checkAccess();
            for (i = 0; i < this.nthreads; i++) {
                this.threads[i].interrupt();
            }
            int ngroupsSnapshot = this.ngroups;
            if (this.groups != null) {
                threadGroupArr = (ThreadGroup[]) Arrays.copyOf(this.groups, ngroupsSnapshot);
            } else {
                threadGroupArr = null;
            }
        }
        for (i = 0; i < ngroupsSnapshot; i++) {
            threadGroupArr[i].interrupt();
        }
    }

    @Deprecated
    public final void suspend() {
        if (stopOrSuspend(true)) {
            Thread.currentThread().suspend();
        }
    }

    private boolean stopOrSuspend(boolean suspend) {
        boolean suicide = false;
        Thread us = Thread.currentThread();
        ThreadGroup[] threadGroupArr = null;
        synchronized (this) {
            int i;
            checkAccess();
            for (i = 0; i < this.nthreads; i++) {
                if (this.threads[i] == us) {
                    suicide = true;
                } else if (suspend) {
                    this.threads[i].suspend();
                } else {
                    this.threads[i].stop();
                }
            }
            int ngroupsSnapshot = this.ngroups;
            if (this.groups != null) {
                threadGroupArr = (ThreadGroup[]) Arrays.copyOf(this.groups, ngroupsSnapshot);
            }
        }
        for (i = 0; i < ngroupsSnapshot; i++) {
            if (threadGroupArr[i].stopOrSuspend(suspend)) {
                suicide = true;
            }
        }
        return suicide;
    }

    @Deprecated
    public final void resume() {
        int i;
        ThreadGroup[] threadGroupArr;
        synchronized (this) {
            checkAccess();
            for (i = 0; i < this.nthreads; i++) {
                this.threads[i].resume();
            }
            int ngroupsSnapshot = this.ngroups;
            if (this.groups != null) {
                threadGroupArr = (ThreadGroup[]) Arrays.copyOf(this.groups, ngroupsSnapshot);
            } else {
                threadGroupArr = null;
            }
        }
        for (i = 0; i < ngroupsSnapshot; i++) {
            threadGroupArr[i].resume();
        }
    }

    public final void destroy() {
        ThreadGroup[] threadGroupArr;
        synchronized (this) {
            checkAccess();
            if (this.destroyed || this.nthreads > 0) {
                throw new IllegalThreadStateException();
            }
            int ngroupsSnapshot = this.ngroups;
            if (this.groups != null) {
                threadGroupArr = (ThreadGroup[]) Arrays.copyOf(this.groups, ngroupsSnapshot);
            } else {
                threadGroupArr = null;
            }
            if (this.parent != null) {
                this.destroyed = true;
                this.ngroups = 0;
                this.groups = null;
                this.nthreads = 0;
                this.threads = null;
            }
        }
        for (int i = 0; i < ngroupsSnapshot; i++) {
            threadGroupArr[i].destroy();
        }
        if (this.parent != null) {
            this.parent.remove(this);
        }
    }

    private final void add(ThreadGroup g) {
        synchronized (this) {
            if (this.destroyed) {
                throw new IllegalThreadStateException();
            }
            if (this.groups == null) {
                this.groups = new ThreadGroup[4];
            } else if (this.ngroups == this.groups.length) {
                this.groups = (ThreadGroup[]) Arrays.copyOf(this.groups, this.ngroups * 2);
            }
            this.groups[this.ngroups] = g;
            this.ngroups++;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void remove(ThreadGroup g) {
        synchronized (this) {
            if (this.destroyed) {
                return;
            }
            int i = 0;
            while (true) {
                if (i >= this.ngroups) {
                    break;
                } else if (this.groups[i] == g) {
                    break;
                } else {
                    i++;
                }
            }
            if (this.nthreads == 0) {
                notifyAll();
            }
            if (this.daemon && this.nthreads == 0 && this.nUnstartedThreads == 0 && this.ngroups == 0) {
                destroy();
            }
        }
    }

    void addUnstarted() {
        synchronized (this) {
            if (this.destroyed) {
                throw new IllegalThreadStateException();
            }
            this.nUnstartedThreads++;
        }
    }

    void add(Thread t) {
        synchronized (this) {
            if (this.destroyed) {
                throw new IllegalThreadStateException();
            }
            if (this.threads == null) {
                this.threads = new Thread[4];
            } else if (this.nthreads == this.threads.length) {
                this.threads = (Thread[]) Arrays.copyOf(this.threads, this.nthreads * 2);
            }
            this.threads[this.nthreads] = t;
            this.nthreads++;
            this.nUnstartedThreads--;
        }
    }

    void threadStartFailed(Thread t) {
        synchronized (this) {
            remove(t);
            this.nUnstartedThreads++;
        }
    }

    void threadTerminated(Thread t) {
        synchronized (this) {
            remove(t);
            if (this.nthreads == 0) {
                notifyAll();
            }
            if (this.daemon && this.nthreads == 0 && this.nUnstartedThreads == 0 && this.ngroups == 0) {
                destroy();
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void remove(Thread t) {
        synchronized (this) {
            if (this.destroyed) {
                return;
            }
            int i = 0;
            while (true) {
                if (i >= this.nthreads) {
                    break;
                } else if (this.threads[i] == t) {
                    break;
                } else {
                    i++;
                }
            }
        }
    }

    public void list() {
        list(System.out, 0);
    }

    void list(PrintStream out, int indent) {
        ThreadGroup[] threadGroupArr;
        synchronized (this) {
            int j;
            int i;
            for (j = 0; j < indent; j++) {
                out.print(" ");
            }
            out.println((Object) this);
            indent += 4;
            for (i = 0; i < this.nthreads; i++) {
                for (j = 0; j < indent; j++) {
                    out.print(" ");
                }
                out.println(this.threads[i]);
            }
            int ngroupsSnapshot = this.ngroups;
            if (this.groups != null) {
                threadGroupArr = (ThreadGroup[]) Arrays.copyOf(this.groups, ngroupsSnapshot);
            } else {
                threadGroupArr = null;
            }
        }
        for (i = 0; i < ngroupsSnapshot; i++) {
            threadGroupArr[i].list(out, indent);
        }
    }

    public void uncaughtException(Thread t, Throwable e) {
        if (this.parent != null) {
            this.parent.uncaughtException(t, e);
            return;
        }
        UncaughtExceptionHandler ueh = Thread.getDefaultUncaughtExceptionHandler();
        if (ueh != null) {
            ueh.uncaughtException(t, e);
        } else if (!(e instanceof ThreadDeath)) {
            System.err.print("Exception in thread \"" + t.getName() + "\" ");
            e.printStackTrace(System.err);
        }
    }

    @Deprecated
    public boolean allowThreadSuspension(boolean b) {
        this.vmAllowSuspension = b;
        if (!b) {
            VM.unsuspendSomeThreads();
        }
        return true;
    }

    public String toString() {
        return getClass().getName() + "[name=" + getName() + ",maxpri=" + this.maxPriority + "]";
    }
}
