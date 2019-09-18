package defpackage;

import android.os.Handler;
import android.os.Looper;
import java.util.concurrent.Executor;

/* renamed from: ai  reason: default package */
public final class ai implements Executor {
    private final Handler handler = new Handler(Looper.getMainLooper());

    public final void execute(Runnable runnable) {
        this.handler.post(runnable);
    }
}
