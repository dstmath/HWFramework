package ohos.eventhandler;

import android.os.Handler;
import android.os.Looper;

public final class HandlerAdapter {
    private Handler androidHandler;

    public HandlerAdapter() {
        this.androidHandler = null;
        this.androidHandler = new Handler(Looper.myLooper());
    }

    public static boolean checkCurrent() {
        return Looper.myLooper() != null;
    }

    public void postTask(Runnable runnable, long j) {
        Handler handler;
        if (runnable != null && (handler = this.androidHandler) != null) {
            if (j > 0) {
                handler.postDelayed(runnable, j);
            } else {
                handler.postAtFrontOfQueue(runnable);
            }
        }
    }

    public void postTaskAtTime(Runnable runnable, long j) {
        Handler handler;
        if (runnable != null && (handler = this.androidHandler) != null) {
            handler.postAtTime(runnable, j);
        }
    }
}
