package defpackage;

import java.util.concurrent.Executor;

/* renamed from: ah  reason: default package */
public final class ah implements Executor {
    public final void execute(Runnable runnable) {
        runnable.run();
    }
}
