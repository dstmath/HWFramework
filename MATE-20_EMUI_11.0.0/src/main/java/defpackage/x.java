package defpackage;

import com.huawei.android.feature.tasks.Task;
import java.util.ArrayDeque;
import java.util.Queue;

/* renamed from: x  reason: default package */
public final class x<TResult> {
    private Queue<p<TResult>> D;
    private boolean E;
    private final Object s = new Object();

    x() {
    }

    public final void a(p<TResult> pVar) {
        synchronized (this.s) {
            if (this.D == null) {
                this.D = new ArrayDeque();
            }
            this.D.add(pVar);
        }
    }

    public final void b(Task<TResult> task) {
        p<TResult> poll;
        synchronized (this.s) {
            if (this.D != null && !this.E) {
                this.E = true;
            } else {
                return;
            }
        }
        while (true) {
            synchronized (this.s) {
                poll = this.D.poll();
                if (poll == null) {
                    this.E = false;
                    return;
                }
            }
            poll.a(task);
        }
    }
}
