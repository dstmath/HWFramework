package com.android.server.incident;

import android.os.Handler;
import android.os.IBinder;
import java.util.ArrayList;

/* access modifiers changed from: package-private */
public class RequestQueue {
    private final Handler mHandler;
    private ArrayList<Rec> mPending = new ArrayList<>();
    private boolean mStarted;
    private final Runnable mWorker = new Runnable() {
        /* class com.android.server.incident.RequestQueue.AnonymousClass1 */

        @Override // java.lang.Runnable
        public void run() {
            ArrayList<Rec> copy = null;
            synchronized (RequestQueue.this.mPending) {
                if (RequestQueue.this.mPending.size() > 0) {
                    copy = new ArrayList<>(RequestQueue.this.mPending);
                    RequestQueue.this.mPending.clear();
                }
            }
            if (copy != null) {
                int size = copy.size();
                for (int i = 0; i < size; i++) {
                    copy.get(i).runnable.run();
                }
            }
        }
    };

    /* access modifiers changed from: private */
    public class Rec {
        public final IBinder key;
        public final Runnable runnable;
        public final boolean value;

        Rec(IBinder key2, boolean value2, Runnable runnable2) {
            this.key = key2;
            this.value = value2;
            this.runnable = runnable2;
        }
    }

    RequestQueue(Handler handler) {
        this.mHandler = handler;
    }

    public void start() {
        synchronized (this.mPending) {
            if (!this.mStarted) {
                if (this.mPending.size() > 0) {
                    this.mHandler.post(this.mWorker);
                }
                this.mStarted = true;
            }
        }
    }

    public void enqueue(IBinder key, boolean value, Runnable runnable) {
        synchronized (this.mPending) {
            boolean skip = false;
            if (!value) {
                try {
                    int i = this.mPending.size() - 1;
                    while (true) {
                        if (i < 0) {
                            break;
                        }
                        Rec r = this.mPending.get(i);
                        if (r.key == key && r.value) {
                            skip = true;
                            this.mPending.remove(i);
                            break;
                        }
                        i--;
                    }
                } catch (Throwable th) {
                    throw th;
                }
            }
            if (!skip) {
                this.mPending.add(new Rec(key, value, runnable));
            }
            if (this.mStarted) {
                this.mHandler.post(this.mWorker);
            }
        }
    }
}
