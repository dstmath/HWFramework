package defpackage;

import com.huawei.android.feature.tasks.Task;
import java.util.ArrayDeque;
import java.util.Queue;

/* renamed from: am  reason: default package */
public final class am<TResult> {
    private final Object E = new Object();
    private Queue<ae<TResult>> O;
    private boolean P;

    am() {
    }

    public final void a(ae<TResult> aeVar) {
        synchronized (this.E) {
            if (this.O == null) {
                this.O = new ArrayDeque();
            }
            this.O.add(aeVar);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0011, code lost:
        r1 = r2.E;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0013, code lost:
        monitor-enter(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:?, code lost:
        r0 = r2.O.poll();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x001c, code lost:
        if (r0 != null) goto L_0x0029;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x001e, code lost:
        r2.P = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0021, code lost:
        monitor-exit(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:?, code lost:
        monitor-exit(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x002a, code lost:
        r0.a(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:?, code lost:
        return;
     */
    public final void b(Task<TResult> task) {
        synchronized (this.E) {
            if (this.O != null && !this.P) {
                this.P = true;
            }
        }
    }
}
