package java.lang;

import java.io.PrintStream;
import java.lang.Thread;
import java.util.Arrays;
import sun.misc.VM;

public class ThreadGroup implements Thread.UncaughtExceptionHandler {
    static final ThreadGroup mainThreadGroup = new ThreadGroup(systemThreadGroup, "main");
    static final ThreadGroup systemThreadGroup = new ThreadGroup();
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

    private ThreadGroup() {
        this.nUnstartedThreads = 0;
        this.name = "system";
        this.maxPriority = 10;
        this.parent = null;
    }

    public ThreadGroup(String name2) {
        this(Thread.currentThread().getThreadGroup(), name2);
    }

    public ThreadGroup(ThreadGroup parent2, String name2) {
        this(checkParentAccess(parent2), parent2, name2);
    }

    private ThreadGroup(Void unused, ThreadGroup parent2, String name2) {
        this.nUnstartedThreads = 0;
        this.name = name2;
        this.maxPriority = parent2.maxPriority;
        this.daemon = parent2.daemon;
        this.vmAllowSuspension = parent2.vmAllowSuspension;
        this.parent = parent2;
        parent2.add(this);
    }

    private static Void checkParentAccess(ThreadGroup parent2) {
        parent2.checkAccess();
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

    public final void setDaemon(boolean daemon2) {
        checkAccess();
        this.daemon = daemon2;
    }

    public final void setMaxPriority(int pri) {
        int ngroupsSnapshot;
        ThreadGroup[] groupsSnapshot;
        synchronized (this) {
            checkAccess();
            if (pri < 1) {
                pri = 1;
            }
            if (pri > 10) {
                pri = 10;
            }
            this.maxPriority = this.parent != null ? Math.min(pri, this.parent.maxPriority) : pri;
            ngroupsSnapshot = this.ngroups;
            if (this.groups != null) {
                groupsSnapshot = (ThreadGroup[]) Arrays.copyOf((T[]) this.groups, ngroupsSnapshot);
            } else {
                groupsSnapshot = null;
            }
        }
        for (int i = 0; i < ngroupsSnapshot; i++) {
            groupsSnapshot[i].setMaxPriority(pri);
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

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x001c, code lost:
        if (r1 >= r2) goto L_0x0028;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001e, code lost:
        r0 = r0 + r3[r1].activeCount();
        r1 = r1 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0028, code lost:
        return r0;
     */
    public int activeCount() {
        ThreadGroup[] groupsSnapshot;
        synchronized (this) {
            int i = 0;
            if (this.destroyed) {
                return 0;
            }
            int result = this.nthreads;
            int ngroupsSnapshot = this.ngroups;
            if (this.groups != null) {
                groupsSnapshot = (ThreadGroup[]) Arrays.copyOf((T[]) this.groups, ngroupsSnapshot);
            } else {
                groupsSnapshot = null;
            }
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

    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0049, code lost:
        if (r10 == false) goto L_0x0059;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x004c, code lost:
        r9 = r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x004d, code lost:
        if (r9 >= r0) goto L_0x0059;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x004f, code lost:
        r4 = r1[r9].enumerate(r8, r4, true);
        r3 = r9 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0059, code lost:
        return r4;
     */
    private int enumerate(Thread[] list, int n, boolean recurse) {
        ThreadGroup[] groupsSnapshot;
        int ngroupsSnapshot = 0;
        ThreadGroup[] groupsSnapshot2 = null;
        synchronized (this) {
            try {
                int i = 0;
                if (this.destroyed) {
                    return 0;
                }
                int nt = this.nthreads;
                if (nt > list.length - n) {
                    nt = list.length - n;
                }
                int n2 = n;
                int i2 = 0;
                while (i2 < nt) {
                    try {
                        if (this.threads[i2].isAlive()) {
                            int n3 = n2 + 1;
                            try {
                                list[n2] = this.threads[i2];
                                n2 = n3;
                            } catch (Throwable th) {
                                th = th;
                                int i3 = n3;
                                throw th;
                            }
                        }
                        i2++;
                    } catch (Throwable th2) {
                        th = th2;
                        int i4 = n2;
                        throw th;
                    }
                }
                if (recurse) {
                    ngroupsSnapshot = this.ngroups;
                    if (this.groups != null) {
                        groupsSnapshot = (ThreadGroup[]) Arrays.copyOf((T[]) this.groups, ngroupsSnapshot);
                    } else {
                        groupsSnapshot = null;
                    }
                    groupsSnapshot2 = groupsSnapshot;
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0019, code lost:
        r3 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001b, code lost:
        if (r1 >= r0) goto L_0x0027;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x001d, code lost:
        r3 = r3 + r2[r1].activeGroupCount();
        r1 = r1 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0027, code lost:
        return r3;
     */
    public int activeGroupCount() {
        ThreadGroup[] groupsSnapshot;
        synchronized (this) {
            int i = 0;
            if (this.destroyed) {
                return 0;
            }
            int ngroupsSnapshot = this.ngroups;
            if (this.groups != null) {
                groupsSnapshot = (ThreadGroup[]) Arrays.copyOf((T[]) this.groups, ngroupsSnapshot);
            } else {
                groupsSnapshot = null;
            }
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

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0030, code lost:
        if (r8 == false) goto L_0x0040;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0033, code lost:
        r2 = r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0034, code lost:
        if (r2 >= r0) goto L_0x0040;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0036, code lost:
        r7 = r1[r2].enumerate(r6, r7, true);
        r3 = r2 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0040, code lost:
        return r7;
     */
    private int enumerate(ThreadGroup[] list, int n, boolean recurse) {
        int ngroupsSnapshot = 0;
        ThreadGroup[] groupsSnapshot = null;
        synchronized (this) {
            int i = 0;
            if (this.destroyed) {
                return 0;
            }
            int ng = this.ngroups;
            if (ng > list.length - n) {
                ng = list.length - n;
            }
            if (ng > 0) {
                System.arraycopy((Object) this.groups, 0, (Object) list, n, ng);
                n += ng;
            }
            if (recurse) {
                ngroupsSnapshot = this.ngroups;
                if (this.groups != null) {
                    groupsSnapshot = (ThreadGroup[]) Arrays.copyOf((T[]) this.groups, ngroupsSnapshot);
                } else {
                    groupsSnapshot = null;
                }
            }
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
        int i2;
        ThreadGroup[] groupsSnapshot;
        synchronized (this) {
            checkAccess();
            for (int i3 = 0; i3 < this.nthreads; i3++) {
                this.threads[i3].interrupt();
            }
            i2 = this.ngroups;
            if (this.groups != null) {
                groupsSnapshot = (ThreadGroup[]) Arrays.copyOf((T[]) this.groups, i2);
            } else {
                groupsSnapshot = null;
            }
        }
        for (i = 0; i < i2; i++) {
            groupsSnapshot[i].interrupt();
        }
    }

    @Deprecated
    public final void suspend() {
        if (stopOrSuspend(true)) {
            Thread.currentThread().suspend();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:19:0x003d, code lost:
        r5 = r4;
        r4 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x003f, code lost:
        if (r4 >= r0) goto L_0x0053;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0047, code lost:
        if (r2[r4].stopOrSuspend(r8) != false) goto L_0x004e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0049, code lost:
        if (r5 == false) goto L_0x004c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x004c, code lost:
        r6 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x004e, code lost:
        r6 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x004f, code lost:
        r5 = r6;
        r4 = r4 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0053, code lost:
        return r5;
     */
    private boolean stopOrSuspend(boolean suspend) {
        Thread us = Thread.currentThread();
        ThreadGroup[] groupsSnapshot = null;
        synchronized (this) {
            try {
                checkAccess();
                boolean suicide = false;
                int i = 0;
                while (i < this.nthreads) {
                    try {
                        if (this.threads[i] == us) {
                            suicide = true;
                        } else if (suspend) {
                            this.threads[i].suspend();
                        } else {
                            this.threads[i].stop();
                        }
                        i++;
                    } catch (Throwable th) {
                        th = th;
                        throw th;
                    }
                }
                int i2 = this.ngroups;
                if (this.groups != null) {
                    groupsSnapshot = (ThreadGroup[]) Arrays.copyOf((T[]) this.groups, i2);
                }
            } catch (Throwable th2) {
                th = th2;
                throw th;
            }
        }
    }

    @Deprecated
    public final void resume() {
        int i;
        int i2;
        ThreadGroup[] groupsSnapshot;
        synchronized (this) {
            checkAccess();
            for (int i3 = 0; i3 < this.nthreads; i3++) {
                this.threads[i3].resume();
            }
            i2 = this.ngroups;
            if (this.groups != null) {
                groupsSnapshot = (ThreadGroup[]) Arrays.copyOf((T[]) this.groups, i2);
            } else {
                groupsSnapshot = null;
            }
        }
        for (i = 0; i < i2; i++) {
            groupsSnapshot[i].resume();
        }
    }

    public final void destroy() {
        int ngroupsSnapshot;
        ThreadGroup[] groupsSnapshot;
        int i;
        synchronized (this) {
            checkAccess();
            if (this.destroyed || this.nthreads > 0) {
                throw new IllegalThreadStateException();
            }
            ngroupsSnapshot = this.ngroups;
            if (this.groups != null) {
                groupsSnapshot = (ThreadGroup[]) Arrays.copyOf((T[]) this.groups, ngroupsSnapshot);
            } else {
                groupsSnapshot = null;
            }
            i = 0;
            if (this.parent != null) {
                this.destroyed = true;
                this.ngroups = 0;
                this.groups = null;
                this.nthreads = 0;
                this.threads = null;
            }
        }
        while (true) {
            int i2 = i;
            if (i2 >= ngroupsSnapshot) {
                break;
            }
            groupsSnapshot[i2].destroy();
            i = i2 + 1;
        }
        if (this.parent != null) {
            this.parent.remove(this);
        }
    }

    private final void add(ThreadGroup g) {
        synchronized (this) {
            if (!this.destroyed) {
                if (this.groups == null) {
                    this.groups = new ThreadGroup[4];
                } else if (this.ngroups == this.groups.length) {
                    this.groups = (ThreadGroup[]) Arrays.copyOf((T[]) this.groups, this.ngroups * 2);
                }
                this.groups[this.ngroups] = g;
                this.ngroups++;
            } else {
                throw new IllegalThreadStateException();
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:26:0x004a, code lost:
        return;
     */
    private void remove(ThreadGroup g) {
        synchronized (this) {
            if (!this.destroyed) {
                int i = 0;
                while (true) {
                    if (i >= this.ngroups) {
                        break;
                    } else if (this.groups[i] == g) {
                        this.ngroups--;
                        System.arraycopy((Object) this.groups, i + 1, (Object) this.groups, i, this.ngroups - i);
                        this.groups[this.ngroups] = null;
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
    }

    /* access modifiers changed from: package-private */
    public void addUnstarted() {
        synchronized (this) {
            if (!this.destroyed) {
                this.nUnstartedThreads++;
            } else {
                throw new IllegalThreadStateException();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void add(Thread t) {
        synchronized (this) {
            if (!this.destroyed) {
                if (this.threads == null) {
                    this.threads = new Thread[4];
                } else if (this.nthreads == this.threads.length) {
                    this.threads = (Thread[]) Arrays.copyOf((T[]) this.threads, this.nthreads * 2);
                }
                this.threads[this.nthreads] = t;
                this.nthreads++;
                this.nUnstartedThreads--;
            } else {
                throw new IllegalThreadStateException();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void threadStartFailed(Thread t) {
        synchronized (this) {
            remove(t);
            this.nUnstartedThreads++;
        }
    }

    /* access modifiers changed from: package-private */
    public void threadTerminated(Thread t) {
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

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x002e, code lost:
        return;
     */
    private void remove(Thread t) {
        synchronized (this) {
            if (!this.destroyed) {
                int i = 0;
                while (true) {
                    if (i >= this.nthreads) {
                        break;
                    } else if (this.threads[i] == t) {
                        Thread[] threadArr = this.threads;
                        int i2 = this.nthreads - 1;
                        this.nthreads = i2;
                        System.arraycopy((Object) this.threads, i + 1, (Object) threadArr, i, i2 - i);
                        this.threads[this.nthreads] = null;
                        break;
                    } else {
                        i++;
                    }
                }
            }
        }
    }

    public void list() {
        list(System.out, 0);
    }

    /* access modifiers changed from: package-private */
    public void list(PrintStream out, int indent) {
        int i;
        int indent2;
        int i2;
        ThreadGroup[] groupsSnapshot;
        synchronized (this) {
            int j = 0;
            while (j < indent) {
                try {
                    out.print(" ");
                    j++;
                } catch (Throwable th) {
                    while (true) {
                        throw th;
                    }
                }
            }
            out.println((Object) this);
            indent2 = indent + 4;
            for (int i3 = 0; i3 < this.nthreads; i3++) {
                for (int j2 = 0; j2 < indent2; j2++) {
                    out.print(" ");
                }
                out.println((Object) this.threads[i3]);
            }
            i2 = this.ngroups;
            if (this.groups != null) {
                groupsSnapshot = (ThreadGroup[]) Arrays.copyOf((T[]) this.groups, i2);
            } else {
                groupsSnapshot = null;
            }
        }
        for (i = 0; i < i2; i++) {
            groupsSnapshot[i].list(out, indent2);
        }
    }

    public void uncaughtException(Thread t, Throwable e) {
        if (this.parent != null) {
            this.parent.uncaughtException(t, e);
            return;
        }
        Thread.UncaughtExceptionHandler ueh = Thread.getDefaultUncaughtExceptionHandler();
        if (ueh != null) {
            ueh.uncaughtException(t, e);
        } else if (!(e instanceof ThreadDeath)) {
            PrintStream printStream = System.err;
            printStream.print("Exception in thread \"" + t.getName() + "\" ");
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
